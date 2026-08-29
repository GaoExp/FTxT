package exp.ftxt.features.battery_stats;

import android.content.Context;
import android.os.PowerManager;

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

    private static boolean isScreenOn() {
        try {
            PowerManager pm = appContext.getSystemService(PowerManager.class);
            return pm != null && pm.isInteractive();
        } catch (Exception ignored) {
            return false;
        }
    }
}