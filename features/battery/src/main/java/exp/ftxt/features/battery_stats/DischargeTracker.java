package exp.ftxt.features.battery_stats;

import android.content.Context;
import android.os.PowerManager;

import java.util.ArrayList;

/** Pencatat sesi pengosongan baterai (mAh terpakai, suhu, efisiensi discharge). */
public class DischargeTracker {

    private static final float MIN_DELTA_PERCENT = 1f;
    private static final long MIN_SEGMENT_MS = 60_000L;
    /** Ambang minimal durasi sesi invalid agar layak direkam (≈1 sampel DB). */
    private static final long INVALID_MIN_MS = 5_000L;

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
    /** Bukti kabel benar-benar lepas (plugged == 0) selama segmen berlangsung. */
    private static boolean segAnyUnplugged;

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
            segAnyUnplugged = s.pluggedInt == 0;
        } else {
            if (s.pluggedInt == 0) segAnyUnplugged = true;
            totalMs += deltaMs;
            if (deltaMs > 0 && isScreenOn()) screenOnMs += deltaMs;
            samples++;
            if (deltaMs > 0 && Math.abs(s.currentMa) > 0) {
                useIntegral += Math.abs(s.currentMa) * (deltaMs / 3600000.0);
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
            long dur = totalMs;
            boolean shortSess = dur < MIN_SEGMENT_MS;
            boolean smallDelta = dPercent < MIN_DELTA_PERCENT;
            boolean valid = !shortSess && !smallDelta;

            double usedCounter = 0d;
            if (segStartChargeMah > 0 && lastChargeMah > 0) {
                usedCounter = Math.max(0d, (double) (segStartChargeMah - lastChargeMah));
            }
            boolean screenOffDominant = screenOnMs * 2 < dur;
            int sampleCount = Math.max(samples, 1);
            float cap = valid ? capacityForEfficiency() : 0f;
            float efficiency = cap > 0f && valid ? (float) (usedCounter * 100.0 / cap) : -1f;
            float tMin = tempMin == Float.MAX_VALUE ? 0f : tempMin;
            float tMax = tempMax == Float.MIN_VALUE ? 0f : tempMax;
            float tAvg = tempCount > 0 ? (float) (tempSum / tempCount) : 0f;
            long now = System.currentTimeMillis();
            long endMs = lastSampleTime > 0 ? lastSampleTime : now;

            if (valid) {
                BatteryHistoryDb.get(appContext).insertDischargeSession(
                        new BatteryHistoryDb.DischargeSession(
                                segStartMs, endMs, segStartPercent, lastPercent,
                                usedCounter, useIntegral, cap, efficiency,
                                screenOffDominant, tMin, tMax, tAvg, sampleCount,
                                true, null));
                return;
            }
            /* Sesi lahir-invalid (colok/lepas singkat): rekam bila benar-benar
             * bukti kabel lepas (plugged == 0) dan cukup panjang, jangan dibuang. */
            if (dur < INVALID_MIN_MS) return;
            if (!segAnyUnplugged) return;
            BatteryHistoryDb.get(appContext).insertDischargeSession(
                    new BatteryHistoryDb.DischargeSession(
                            segStartMs, endMs, segStartPercent, lastPercent,
                            usedCounter, useIntegral, 0f, -1f,
                            screenOffDominant, tMin, tMax, tAvg, sampleCount,
                            false, invalidReasonFor(smallDelta, shortSess)));
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
            segAnyUnplugged = false;
        }
    }

    private static String invalidReasonFor(boolean smallDelta, boolean shortSess) {
        StringBuilder sb = new StringBuilder();
        if (shortSess) sb.append("durasi < 1 menit");
        if (smallDelta) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("Δ% < ").append((int) MIN_DELTA_PERCENT).append("%");
        }
        return sb.toString();
    }

    private static float capacityForEfficiency() {
        BatteryCapacityEstimator.HealthResult r = BatteryCapacityEstimator.getResult();
        if (r.medianMah > 0f) return r.medianMah;
        if (r.designMah > 0) return r.designMah;
        return 0f;
    }

    /**
     * Rekonstruksi segmen pengosongan yang belum sempat tersimpan saat proses
     * dibunuh (§7.6 — Solusi A). Dipanggil dari {@code BatteryMonitor.start()}
     * dengan segmen yang sudah dihitung sekali oleh {@link SessionRebuild}
     * (shared dengan {@link BatteryCapacityEstimator} agar query 24 jam +
     * segmentasi tidak dijalankan dua kali saat start).
     * Sesi yang berakhir saat proses mati di-INSERT (dedup dengan endTime > sesi
     * terakhir tersimpan); sesi yang masih berjalan disambungkan ke state live.
     */
    public static synchronized void rebuildPendingSessions(
            long lastEnd, ArrayList<SessionSegmentBuilder.Segment> segs) {
        if (appContext == null) return;
        BatteryHistoryDb db = BatteryHistoryDb.get(appContext);
        if (segs.isEmpty()) return;

        if (lastEnd > 0) {
            for (int k = 0; k < segs.size() - 1; k++) {
                SessionSegmentBuilder.Segment seg = segs.get(k);
                if (seg.direction != SessionSegmentBuilder.Direction.DISCHARGE) continue;
                if (seg.endTime <= lastEnd) continue;
                float dPercent = seg.startPercent - seg.endPercent;
                long dur = seg.durationMs();
                boolean valid = dPercent >= MIN_DELTA_PERCENT
                        && dur >= MIN_SEGMENT_MS && seg.mAhIntegral > 0;
                if (valid) {
                    db.insertDischargeSession(toDischargeRow(seg, true, null));
                } else if (dur >= INVALID_MIN_MS) {
                    boolean smallDelta = dPercent < MIN_DELTA_PERCENT;
                    boolean shortSess = dur < MIN_SEGMENT_MS;
                    db.insertDischargeSession(
                            toDischargeRow(seg, false, invalidReasonFor(smallDelta, shortSess)));
                }
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
            SessionSegmentBuilder.Segment seg, boolean valid, String invalidReason) {
        double usedCounter = Math.max(0d, seg.deltaChargeMah);
        float cap = valid ? capacityForEfficiency() : 0f;
        float efficiency = cap > 0f && valid ? (float) (usedCounter * 100.0 / cap) : -1f;
        boolean screenOffDominant = seg.screenOnMs * 2 < seg.durationMs();
        return new BatteryHistoryDb.DischargeSession(
                seg.startTime, seg.endTime, seg.startPercent, seg.endPercent,
                usedCounter, seg.mAhIntegral, cap, efficiency, screenOffDominant,
                seg.tempMin, seg.tempMax, seg.tempAvg, seg.sampleCount,
                valid, invalidReason);
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