package exp.ftxt.features.battery_stats;

import android.content.Context;
import android.os.PowerManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;

/** Estimasi kapasitas baterai dari segmen pengisian daya (ala AccuBattery). */
public class BatteryCapacityEstimator {

    private static final String LEGACY_JSON = "battery_health.json";
    private static final float MIN_DELTA_PERCENT = 5f;
    private static final float MIN_ESTIMATE_MAH = 500f;
    private static final float MAX_ESTIMATE_MAH = 30000f;
    private static final long MIN_SEGMENT_MS = 60_000L;

    public static class HealthResult {
        /** Median gabungan (pengisian + pengosongan) — sumber skor kesehatan. */
        public float medianMah = -1f;
        /** Median hanya dari segmen pengisian; -1 bila belum ada sesi valid. */
        public float chargeMedianMah = -1f;
        /** Median hanya dari segmen pengosongan; -1 bila belum ada sesi valid. */
        public float dischargeMedianMah = -1f;
        public int designMah = 0;
        public int sessionCount = 0;
        public long totalSamples = 0;
        public boolean fromScreenOffSessions = false;
    }

    private static Context appContext;
    private static boolean loaded = false;
    private static int designMah = 0;
    private static final ArrayList<BatteryHistoryDb.SessionRow> sessions = new ArrayList<>();

    private static long healthCacheUntil = 0L;
    private static HealthResult healthCache;
    /** Menandai satu thread sedang menghitung getResult agar tidak dobel hitung. */
    private static boolean computing = false;

    private static boolean segmentActive = false;
    private static long segmentStartMs;
    private static long segmentStartChargeMah = -1L;
    private static int segmentStartPercent = -1;
    private static long segmentScreenOnMs;
    private static long segmentTotalMs;
    private static int segmentSamples;
    private static long lastChargeMah = -1L;
    private static int lastPercent = -1;
    private static long lastSampleTime = -1L;
    private static double accumulatedChargeMah = 0.0;
    private static int lastCurrentMa = 0;
    private static float segTempMin = Float.MAX_VALUE;
    private static float segTempMax = Float.MIN_VALUE;
    private static double segTempSum;
    private static int segTempCount;

    private BatteryCapacityEstimator() {}

    public static synchronized void init(Context context) {
        if (context == null) return;
        if (appContext == null) {
            appContext = context.getApplicationContext();
        }
        if (!loaded) {
            loaded = true;
            new Thread(() -> {
                migrateLegacyJsonIfNeeded();
                loadFromDb();
                getResult();
            }, "BatteryEstimatorInit").start();
        }
    }

    public static synchronized void onSample(BatteryReading.Snapshot s) {
        if (s == null || !loaded || appContext == null) return;
        boolean charging = s.isCharging();

        long deltaMs = lastSampleTime > 0 ? Math.max(0L, s.time - lastSampleTime) : 0L;
        lastSampleTime = s.time;

        if (!charging) {
            finishSegment(lastPercent);
            lastChargeMah = s.chargeMah;
            lastPercent = s.percent;
            lastCurrentMa = s.currentMa;
            return;
        }
        if (!segmentActive) {
            segmentActive = true;
            segmentStartMs = s.time;
            segmentStartChargeMah = s.chargeMah;
            segmentStartPercent = s.percent;
            segmentScreenOnMs = 0;
            segmentTotalMs = 0;
            segmentSamples = 0;
            accumulatedChargeMah = 0.0;
            segTempMin = s.tempC;
            segTempMax = s.tempC;
            segTempSum = 0;
            segTempCount = 0;
        } else {
            segmentTotalMs += deltaMs;
            if (deltaMs > 0 && isScreenOn()) segmentScreenOnMs += deltaMs;
            segmentSamples++;
            if (deltaMs > 0) {
                int absMa = Math.abs(s.currentMa);
                if (absMa > 0) {
                    accumulatedChargeMah += absMa * (deltaMs / 3600000.0);
                }
            }
            if (s.tempC > 0f) {
                segTempSum += s.tempC;
                segTempCount++;
                if (s.tempC < segTempMin) segTempMin = s.tempC;
                if (s.tempC > segTempMax) segTempMax = s.tempC;
            }
        }
        lastChargeMah = s.chargeMah;
        lastPercent = s.percent;
        lastCurrentMa = s.currentMa;
    }

    public static synchronized void onMonitoringStopped() {
        finishSegment(lastPercent);
        lastSampleTime = -1L;
    }

    public static synchronized boolean isSegmentActive() {
        return segmentActive;
    }

    /**
     * Hasil estimasi kapasitas/kesehatan baterai (cache 60 detik).
     * Query & rekonstruksi segmen (berat) dijalankan DI LUAR monitor class agar
     * lock estimator tidak dipegang lama — sebelumnya query riwayat 7 hari penuh
     * memonopoli lock dan membuat pemanggil lain (termasuk main thread di init())
     * menunggu hingga memicu ANR. State dibaca di dalam blok sinkron singkat,
     * lalu komputasi dilakukan tanpa lock; bila thread lain sedang menghitung,
     * pemanggil menunggu hasilnya (wait/notify) tanpa ikut menghitung dobel.
     */
    public static HealthResult getResult() {
        int design;
        boolean isLoaded;
        Context ctx;
        ArrayList<BatteryHistoryDb.SessionRow> sessionsCopy;
        synchronized (BatteryCapacityEstimator.class) {
            long now = System.currentTimeMillis();
            if (healthCache != null && now < healthCacheUntil) return healthCache;
            while (computing) {
                try {
                    BatteryCapacityEstimator.class.wait(250L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                now = System.currentTimeMillis();
                if (healthCache != null && now < healthCacheUntil) return healthCache;
            }
            computing = true;
            design = designMah;
            isLoaded = loaded;
            ctx = appContext;
            sessionsCopy = new ArrayList<>(sessions);
        }

        HealthResult r;
        try {
            r = computeResult(ctx, isLoaded, design, sessionsCopy, System.currentTimeMillis());
        } catch (Throwable t) {
            r = new HealthResult();
            r.designMah = design;
        }

        synchronized (BatteryCapacityEstimator.class) {
            healthCache = r;
            healthCacheUntil = System.currentTimeMillis() + 60_000L;
            computing = false;
            BatteryCapacityEstimator.class.notifyAll();
        }
        return r;
    }

    /**
     * Estimasi kapasitas dari data mentah samples (sumber kebenaran, §7).
     * Rekonstruksi segmen pengisian & pengosongan → kapasitas = mAh integral ÷ Δ% × 100.
     * Segmen pendek/ekstrem dibuang (Δ% ≥ 5, durasi ≥ 1 menit). Mengembalikan
     * null bila belum ada segmen valid sehingga tidak mengganggu fallback.
     */
    private static HealthResult computeResult(Context ctx, boolean isLoaded, int design,
                                              ArrayList<BatteryHistoryDb.SessionRow> sessionsCopy,
                                              long now) {
        if (ctx == null) {
            HealthResult empty = new HealthResult();
            empty.designMah = design;
            return empty;
        }
        if (!isLoaded) return fallbackFromSessions(ctx, sessionsCopy, design);

        HealthResult r = new HealthResult();
        r.designMah = design;

        BatteryHistoryDb db = BatteryHistoryDb.get(ctx);
        long span = 7L * 24 * 3600_000L;
        BatteryReading.Snapshot[] asc;
        try {
            asc = db.querySamples(now - span);
        } catch (Exception e) {
            return fallbackFromSessions(ctx, sessionsCopy, design);
        }
        if (asc == null || asc.length == 0) return fallbackFromSessions(ctx, sessionsCopy, design);

        BatteryReading.Snapshot[] desc = SessionSegmentBuilder.toDesc(asc);
        ArrayList<SessionSegmentBuilder.Segment> segments =
                SessionSegmentBuilder.buildSegments(desc);

        ArrayList<Float> chargeEstimates = new ArrayList<>();
        ArrayList<Float> dischargeEstimates = new ArrayList<>();
        ArrayList<Float> combinedEstimates = new ArrayList<>();
        int validSamples = 0;
        int screenOffCount = 0;
        for (SessionSegmentBuilder.Segment seg : segments) {
            if (seg.direction == SessionSegmentBuilder.Direction.FLAT) continue;
            float dPercent = Math.abs(seg.endPercent - seg.startPercent);
            if (dPercent < MIN_DELTA_PERCENT) continue;
            if (seg.durationMs() < MIN_SEGMENT_MS) continue;
            if (seg.mAhIntegral <= 0) continue;
            float estimate = (float) (seg.mAhIntegral * 100.0 / dPercent);
            if (estimate < MIN_ESTIMATE_MAH || estimate > MAX_ESTIMATE_MAH) continue;
            combinedEstimates.add(estimate);
            if (seg.direction == SessionSegmentBuilder.Direction.CHARGE) {
                chargeEstimates.add(estimate);
            } else {
                dischargeEstimates.add(estimate);
            }
            validSamples += seg.sampleCount;
            if (seg.screenOnMs * 2 < seg.durationMs()) screenOffCount++;
        }
        if (combinedEstimates.isEmpty()) return fallbackFromSessions(ctx, sessionsCopy, design);

        r.sessionCount = combinedEstimates.size();
        r.totalSamples = validSamples;
        r.medianMah = median(combinedEstimates);
        r.chargeMedianMah = chargeEstimates.isEmpty() ? -1f : median(chargeEstimates);
        r.dischargeMedianMah = dischargeEstimates.isEmpty() ? -1f : median(dischargeEstimates);
        r.fromScreenOffSessions = screenOffCount * 2 >= combinedEstimates.size();
        return r;
    }

    /** Fallback: bila belum ada segmen valid dari samples, pakai tabel sessions & discharge_sessions. */
    private static HealthResult fallbackFromSessions(Context ctx,
            ArrayList<BatteryHistoryDb.SessionRow> sessionsCopy, int design) {
        HealthResult r = new HealthResult();
        r.designMah = design;

        ArrayList<Float> chargePool = new ArrayList<>();
        ArrayList<Float> dischargePool = new ArrayList<>();
        ArrayList<Float> offPool = new ArrayList<>();
        for (BatteryHistoryDb.SessionRow sn : sessionsCopy) {
            if (sn.capacityMah <= 0f) continue;
            chargePool.add(sn.capacityMah);
            r.totalSamples += sn.sampleCount;
            if (sn.mostlyScreenOff) offPool.add(sn.capacityMah);
        }
        try {
            BatteryHistoryDb db = BatteryHistoryDb.get(ctx);
            for (BatteryHistoryDb.DischargeSession d : db.queryDischargeSessions(0L, Long.MAX_VALUE)) {
                float dPercent = d.startPercent - d.endPercent;
                if (dPercent < MIN_DELTA_PERCENT) continue;
                if (d.usedMahIntegral <= 0) continue;
                float estimate = (float) (d.usedMahIntegral * 100.0 / dPercent);
                if (estimate < MIN_ESTIMATE_MAH || estimate > MAX_ESTIMATE_MAH) continue;
                dischargePool.add(estimate);
                r.totalSamples += d.sampleCount;
                if (d.screenOffDominant) offPool.add(estimate);
            }
        } catch (Exception ignored) {}

        ArrayList<Float> combinedPool = new ArrayList<>(chargePool);
        combinedPool.addAll(dischargePool);
        r.sessionCount = combinedPool.size();
        r.chargeMedianMah = chargePool.isEmpty() ? -1f : median(chargePool);
        r.dischargeMedianMah = dischargePool.isEmpty() ? -1f : median(dischargePool);
        if (offPool.size() >= 3) {
            r.medianMah = median(offPool);
            r.fromScreenOffSessions = true;
        } else if (!combinedPool.isEmpty()) {
            r.medianMah = median(combinedPool);
            r.fromScreenOffSessions = false;
        }
        return r;
    }

    public static synchronized void setDesignCapacity(int mah) {
        if (mah < 0) mah = 0;
        designMah = mah;
        BatteryHistoryDb.get(appContext).setMeta("design_mah", String.valueOf(designMah));
    }

    public static synchronized void resetEstimationData() {
        sessions.clear();
        BatteryHistoryDb db = BatteryHistoryDb.get(appContext);
        db.deleteAllSessions();
        db.deleteAllDischargeSessions();
        healthCache = null;
    }

    private static void finishSegment(int endPercent) {
        if (!segmentActive) return;
        segmentActive = false;
        try {
            if (segmentStartPercent < 0 || endPercent < 0) return;
            float dPercent = endPercent - segmentStartPercent;
            if (dPercent < MIN_DELTA_PERCENT) return;
            if (segmentTotalMs < MIN_SEGMENT_MS) return;
            if (accumulatedChargeMah <= 0) return;
            float estimate = (float) (accumulatedChargeMah * 100.0 / dPercent);
            if (estimate < MIN_ESTIMATE_MAH || estimate > MAX_ESTIMATE_MAH) return;
            boolean screenOffDominant = segmentScreenOnMs * 2 < segmentTotalMs;
            int samples = Math.max(segmentSamples, 1);
            long now = System.currentTimeMillis();
            long endMs = lastSampleTime > 0 ? lastSampleTime : now;
            double deltaChargeMah = 0d;
            if (segmentStartChargeMah > 0 && lastChargeMah > 0) {
                deltaChargeMah = (double) (lastChargeMah - segmentStartChargeMah);
            }
            double mAhCounter = Math.max(0d, deltaChargeMah);
            float tempMin = segTempMin == Float.MAX_VALUE ? 0f : segTempMin;
            float tempMax = segTempMax == Float.MIN_VALUE ? 0f : segTempMax;
            float tempAvg = segTempCount > 0
                    ? (float) (segTempSum / segTempCount) : 0f;
            BatteryHistoryDb.SessionRow row = new BatteryHistoryDb.SessionRow(
                    now, estimate, screenOffDominant, samples,
                    segmentStartMs, endMs, segmentStartPercent, endPercent,
                    mAhCounter, accumulatedChargeMah, deltaChargeMah,
                    tempMin, tempMax, tempAvg);
            BatteryHistoryDb.get(appContext).insertSessionFull(row);
            sessions.add(row);
        } finally {
            segmentStartChargeMah = -1L;
            segmentStartPercent = -1;
            segmentScreenOnMs = 0;
            segmentTotalMs = 0;
            segmentSamples = 0;
            segmentStartMs = 0;
            accumulatedChargeMah = 0.0;
            segTempMin = Float.MAX_VALUE;
            segTempMax = Float.MIN_VALUE;
            segTempSum = 0;
            segTempCount = 0;
        }
    }

    private static float median(ArrayList<Float> values) {
        Collections.sort(values);
        int n = values.size();
        if (n % 2 == 1) return values.get(n / 2);
        return (values.get(n / 2 - 1) + values.get(n / 2)) / 2f;
    }

    private static boolean isScreenOn() {
        try {
            PowerManager pm = appContext.getSystemService(PowerManager.class);
            return pm != null && pm.isInteractive();
        } catch (Exception ignored) {
            return false;
        }
    }

    private static synchronized void loadFromDb() {
        BatteryHistoryDb db = BatteryHistoryDb.get(appContext);
        String savedDesign = db.getMeta("design_mah");
        try {
            designMah = savedDesign != null ? Integer.parseInt(savedDesign) : 0;
        } catch (NumberFormatException ignored) {
            designMah = 0;
        }
        sessions.clear();
        sessions.addAll(db.getSessions());
    }

    /**
     * Rekonstruksi segmen pengisian yang belum sempat tersimpan saat proses
     * dibunuh (§7.6 — Solusi A). Dipanggil dari {@code BatteryMonitor.start()}
     * dengan segmen yang sudah dihitung sekali oleh {@link SessionRebuild}
     * (shared dengan {@link DischargeTracker} agar query 24 jam + segmentasi
     * tidak dijalankan dua kali saat start).
     * Sesi yang benar-benar berakhir saat proses mati di-INSERT (dedup dengan
     * endTime > sesi terakhir tersimpan); sesi yang masih berjalan disambungkan
     * ke state live agar akumulasi berlanjut. Tidak dobel: segmen yang masih
     * berjalan belum pernah tersimpan, dan segmen lama yang overlap diabaikan.
     */
    public static synchronized void rebuildPendingSessions(
            long lastEnd, ArrayList<SessionSegmentBuilder.Segment> segs) {
        if (appContext == null || !loaded) return;
        BatteryHistoryDb db = BatteryHistoryDb.get(appContext);
        if (segs.isEmpty()) return;

        if (lastEnd > 0) {
            for (int k = 0; k < segs.size() - 1; k++) {
                SessionSegmentBuilder.Segment seg = segs.get(k);
                if (seg.direction != SessionSegmentBuilder.Direction.CHARGE) continue;
                if (seg.endTime <= lastEnd) continue;
                float dPercent = seg.endPercent - seg.startPercent;
                if (dPercent < MIN_DELTA_PERCENT) continue;
                if (seg.durationMs() < MIN_SEGMENT_MS) continue;
                if (seg.mAhIntegral <= 0) continue;
                float estimate = (float) (seg.mAhIntegral * 100.0 / dPercent);
                if (estimate < MIN_ESTIMATE_MAH || estimate > MAX_ESTIMATE_MAH) continue;
                db.insertSessionFull(segmentToRow(seg, estimate));
            }
        }

        SessionSegmentBuilder.Segment last = segs.get(segs.size() - 1);
        if (last.direction == SessionSegmentBuilder.Direction.CHARGE) {
            setActiveSegment(last);
        }
    }

    private static void setActiveSegment(SessionSegmentBuilder.Segment seg) {
        segmentActive = true;
        segmentStartMs = seg.startTime;
        segmentStartChargeMah = -1L;
        segmentStartPercent = seg.startPercent;
        segmentScreenOnMs = seg.screenOnMs;
        segmentTotalMs = seg.durationMs();
        segmentSamples = seg.sampleCount;
        accumulatedChargeMah = seg.mAhIntegral;
        segTempMin = seg.tempMin > 0f ? seg.tempMin : Float.MAX_VALUE;
        segTempMax = seg.tempMax > 0f ? seg.tempMax : Float.MIN_VALUE;
        segTempSum = seg.tempAvg > 0f ? seg.tempAvg * seg.sampleCount : 0;
        segTempCount = seg.tempAvg > 0f ? seg.sampleCount : 0;
        lastChargeMah = -1L;
        lastPercent = seg.endPercent;
        lastSampleTime = -1L;
        lastCurrentMa = 0;
    }

    private static BatteryHistoryDb.SessionRow segmentToRow(
            SessionSegmentBuilder.Segment seg, float estimate) {
        boolean screenOffDominant = seg.screenOnMs * 2 < seg.durationMs();
        return new BatteryHistoryDb.SessionRow(System.currentTimeMillis(), estimate,
                screenOffDominant, seg.sampleCount,
                seg.startTime, seg.endTime, seg.startPercent, seg.endPercent,
                Math.max(0d, seg.deltaChargeMah), seg.mAhIntegral, seg.deltaChargeMah,
                seg.tempMin, seg.tempMax, seg.tempAvg);
    }

    /** Import sekali file JSON lama ke database lalu file dihapus. */
    private static synchronized void migrateLegacyJsonIfNeeded() {
        try {
            BatteryHistoryDb db = BatteryHistoryDb.get(appContext);
            if ("1".equals(db.getMeta("json_migrated"))) return;
            db.setMeta("json_migrated", "1");

            File f = new File(appContext.getFilesDir(), LEGACY_JSON);
            if (!f.exists()) return;
            byte[] buf = new byte[(int) f.length()];
            try (FileInputStream fis = new FileInputStream(f)) {
                int read = fis.read(buf);
                if (read <= 0) {
                    f.delete();
                    return;
                }
            }
            JSONObject o = new JSONObject(new String(buf, StandardCharsets.UTF_8));
            int legacyDesign = o.optInt("design_mah", 0);
            if (legacyDesign > 0 && o.optJSONArray("sessions") != null
                    && db.getMeta("design_mah") == null) {
                db.setMeta("design_mah", String.valueOf(legacyDesign));
            }
            JSONArray arr = o.optJSONArray("sessions");
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject e = arr.optJSONObject(i);
                    if (e == null) continue;
                    float cap = (float) e.optDouble("cap", -1d);
                    if (cap <= 0) continue;
                    db.insertSession(e.optLong("t", 0L), cap,
                            e.optBoolean("screen_off", false), e.optInt("samples", 0));
                }
            }
            f.delete();
        } catch (Exception ignored) {}
    }
}
