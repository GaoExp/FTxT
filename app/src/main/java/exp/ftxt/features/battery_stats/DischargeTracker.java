package exp.ftxt.features.battery_stats;

import android.content.Context;
import android.os.PowerManager;

import java.util.ArrayList;

/** Pencatat sesi pengosongan baterai (mAh terpakai, suhu, efisiensi discharge). */
public class DischargeTracker {

    private static final float MIN_DELTA_PERCENT = 1f;
    private static final long MIN_SEGMENT_MS = 60_000L;

    private static Context appContext;
    private static boolean active = false;
    private static long segStartMs;
    private static long segStartChargeMah = -1L;
    private static int segStartPercent = -1;
    private static long screenOnMs;
    private static long totalMs;
    private static int samples;
    private static long lastChargeMah = -1L;
    private static int lastPercent = -1;
    private static long lastSampleTime = -1L;
    private static double useIntegral = 0.0;
    private static float tempMin = Float.MAX_VALUE;
    private static float tempMax = Float.MIN_VALUE;
    private static double tempSum;
    private static int tempCount;

    private DischargeTracker() {}

    public static synchronized void init(Context context) {
        if (context == null) return;
        if (appContext == null) {
            appContext = context.getApplicationContext();
        }
    }

    public static synchronized void onSample(BatteryReading.Snapshot s) {
        if (s == null || appContext == null) return;
        long deltaMs = lastSampleTime > 0 ? Math.max(0L, s.time - lastSampleTime) : 0L;
        lastSampleTime = s.time;

        if (s.isCharging()) {
            finishSegment();
            lastChargeMah = s.chargeMah;
            lastPercent = s.percent;
            return;
        }
        if (!active) {
            active = true;
            segStartMs = s.time;
            segStartChargeMah = s.chargeMah;
            segStartPercent = s.percent;
            screenOnMs = 0;
            totalMs = 0;
            samples = 0;
            useIntegral = 0.0;
            tempMin = s.tempC;
            tempMax = s.tempC;
            tempSum = 0;
            tempCount = 0;
        } else {
            totalMs += deltaMs;
            if (deltaMs > 0 && isScreenOn()) screenOnMs += deltaMs;
            samples++;
            if (deltaMs > 0 && s.currentMa > 0) {
                useIntegral += s.currentMa * (deltaMs / 3600000.0);
            }
            if (s.tempC > 0f) {
                tempSum += s.tempC;
                tempCount++;
                if (s.tempC < tempMin) tempMin = s.tempC;
                if (s.tempC > tempMax) tempMax = s.tempC;
            }
        }
        lastChargeMah = s.chargeMah;
        lastPercent = s.percent;
    }

    public static synchronized void onMonitoringStopped() {
        finishSegment();
        lastSampleTime = -1L;
    }

    private static void finishSegment() {
        if (!active) return;
        active = false;
        try {
            if (segStartPercent < 0 || lastPercent < 0) return;
            float dPercent = segStartPercent - lastPercent;
            if (dPercent < MIN_DELTA_PERCENT) return;
            if (totalMs < MIN_SEGMENT_MS) return;

            double usedCounter = 0d;
            if (segStartChargeMah > 0 && lastChargeMah > 0) {
                usedCounter = Math.max(0d, (double) (segStartChargeMah - lastChargeMah));
            }
            boolean screenOffDominant = screenOnMs * 2 < totalMs;
            int sampleCount = Math.max(samples, 1);
            float cap = capacityForEfficiency();
            float efficiency = cap > 0f ? (float) (usedCounter * 100.0 / cap) : -1f;
            float tMin = tempMin == Float.MAX_VALUE ? 0f : tempMin;
            float tMax = tempMax == Float.MIN_VALUE ? 0f : tempMax;
            float tAvg = tempCount > 0 ? (float) (tempSum / tempCount) : 0f;
            long now = System.currentTimeMillis();
            long endMs = lastSampleTime > 0 ? lastSampleTime : now;

            BatteryHistoryDb.get(appContext).insertDischargeSession(
                    new BatteryHistoryDb.DischargeSession(
                            segStartMs, endMs, segStartPercent, lastPercent,
                            usedCounter, useIntegral, cap, efficiency,
                            screenOffDominant, tMin, tMax, tAvg, sampleCount));
        } finally {
            segStartChargeMah = -1L;
            segStartPercent = -1;
            screenOnMs = 0;
            totalMs = 0;
            samples = 0;
            segStartMs = 0;
            useIntegral = 0.0;
            tempMin = Float.MAX_VALUE;
            tempMax = Float.MIN_VALUE;
            tempSum = 0;
            tempCount = 0;
        }
    }

    private static float capacityForEfficiency() {
        BatteryCapacityEstimator.HealthResult r = BatteryCapacityEstimator.getResult();
        if (r.medianMah > 0f) return r.medianMah;
        if (r.designMah > 0) return r.designMah;
        return 0f;
    }

    /**
     * Rekonstruksi segmen pengosongan yang belum sempat tersimpan saat proses
     * dibunuh (§7.6 — Solusi A). Dipanggil dari {@code BatteryMonitor.start()}.
     * Sesi yang berakhir saat proses mati di-INSERT (dedup dengan endTime > sesi
     * terakhir tersimpan); sesi yang masih berjalan disambungkan ke state live.
     */
    public static synchronized void rebuildPendingSessions() {
        if (appContext == null) return;
        BatteryHistoryDb db = BatteryHistoryDb.get(appContext);
        long lastEnd = db.lastDischargeSessionEnd();
        long now = System.currentTimeMillis();
        long from = now - 24L * 3600_000L;

        BatteryReading.Snapshot[] asc;
        try {
            asc = db.querySamples(from);
        } catch (Exception e) {
            return;
        }
        if (asc == null || asc.length == 0) return;

        BatteryReading.Snapshot[] desc = SessionSegmentBuilder.toDesc(asc);
        SessionSegmentBuilder.ScreenOnOracle oracle = db.screenOnOracle(from, now);
        ArrayList<SessionSegmentBuilder.Segment> segs =
                SessionSegmentBuilder.buildSegments(desc, SessionSegmentBuilder.WINDOW_MS,
                        SessionSegmentBuilder.SESSION_GAP_MS, oracle);
        if (segs.isEmpty()) return;

        if (lastEnd > 0) {
            for (int k = 0; k < segs.size() - 1; k++) {
                SessionSegmentBuilder.Segment seg = segs.get(k);
                if (seg.direction != SessionSegmentBuilder.Direction.DISCHARGE) continue;
                if (seg.endTime <= lastEnd) continue;
                float dPercent = seg.startPercent - seg.endPercent;
                if (dPercent < MIN_DELTA_PERCENT) continue;
                if (seg.durationMs() < MIN_SEGMENT_MS) continue;
                if (seg.mAhIntegral <= 0) continue;
                db.insertDischargeSession(toDischargeRow(seg));
            }
        }

        SessionSegmentBuilder.Segment last = segs.get(segs.size() - 1);
        if (last.direction == SessionSegmentBuilder.Direction.DISCHARGE) {
            setActiveSegment(last);
        }
    }

    private static void setActiveSegment(SessionSegmentBuilder.Segment seg) {
        active = true;
        segStartMs = seg.startTime;
        segStartChargeMah = -1L;
        segStartPercent = seg.startPercent;
        screenOnMs = seg.screenOnMs;
        totalMs = seg.durationMs();
        samples = seg.sampleCount;
        useIntegral = seg.mAhIntegral;
        tempMin = seg.tempMin > 0f ? seg.tempMin : Float.MAX_VALUE;
        tempMax = seg.tempMax > 0f ? seg.tempMax : Float.MIN_VALUE;
        tempSum = seg.tempAvg > 0f ? seg.tempAvg * seg.sampleCount : 0;
        tempCount = seg.tempAvg > 0f ? seg.sampleCount : 0;
        lastChargeMah = -1L;
        lastPercent = seg.endPercent;
        lastSampleTime = -1L;
    }

    private static BatteryHistoryDb.DischargeSession toDischargeRow(
            SessionSegmentBuilder.Segment seg) {
        double usedCounter = Math.max(0d, seg.deltaChargeMah);
        float cap = capacityForEfficiency();
        float efficiency = cap > 0f ? (float) (usedCounter * 100.0 / cap) : -1f;
        boolean screenOffDominant = seg.screenOnMs * 2 < seg.durationMs();
        return new BatteryHistoryDb.DischargeSession(
                seg.startTime, seg.endTime, seg.startPercent, seg.endPercent,
                usedCounter, seg.mAhIntegral, cap, efficiency, screenOffDominant,
                seg.tempMin, seg.tempMax, seg.tempAvg, seg.sampleCount);
    }

    private static boolean isScreenOn() {
        try {
            PowerManager pm = appContext.getSystemService(PowerManager.class);
            return pm != null && pm.isInteractive();
        } catch (Exception ignored) {
            return false;
        }
    }
}