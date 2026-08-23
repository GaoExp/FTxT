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
        public float medianMah = -1f;
        public int designMah = 0;
        public int sessionCount = 0;
        public long totalSamples = 0;
        public boolean fromScreenOffSessions = false;
    }

    private static Context appContext;
    private static boolean loaded = false;
    private static int designMah = 0;
    private static final ArrayList<BatteryHistoryDb.SessionRow> sessions = new ArrayList<>();

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

    private BatteryCapacityEstimator() {}

    public static synchronized void init(Context context) {
        if (context == null) return;
        if (appContext == null) {
            appContext = context.getApplicationContext();
        }
        if (!loaded) {
            loaded = true;
            migrateLegacyJsonIfNeeded();
            loadFromDb();
        }
    }

    public static synchronized void onSample(BatteryReading.Snapshot s) {
        if (s == null || !loaded || appContext == null) return;
        boolean charging = s.isCharging();

        long deltaMs = lastSampleTime > 0 ? Math.max(0L, s.time - lastSampleTime) : 0L;
        lastSampleTime = s.time;

        if (!charging) {
            finishSegment(lastChargeMah, lastPercent);
            lastChargeMah = s.chargeMah;
            lastPercent = s.percent;
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
        } else {
            segmentTotalMs += deltaMs;
            if (deltaMs > 0 && isScreenOn()) segmentScreenOnMs += deltaMs;
            segmentSamples++;
        }
        lastChargeMah = s.chargeMah;
        lastPercent = s.percent;
    }

    public static synchronized void onMonitoringStopped() {
        finishSegment(lastChargeMah, lastPercent);
        lastSampleTime = -1L;
    }

    public static synchronized boolean isSegmentActive() {
        return segmentActive;
    }

    public static synchronized HealthResult getResult() {
        HealthResult r = new HealthResult();
        r.designMah = designMah;
        r.sessionCount = sessions.size();
        for (BatteryHistoryDb.SessionRow sn : sessions) r.totalSamples += sn.sampleCount;

        ArrayList<Float> offPool = new ArrayList<>();
        for (BatteryHistoryDb.SessionRow sn : sessions) {
            if (sn.mostlyScreenOff) offPool.add(sn.capacityMah);
        }
        if (offPool.size() >= 3) {
            r.medianMah = median(offPool);
            r.fromScreenOffSessions = true;
        } else if (!sessions.isEmpty()) {
            ArrayList<Float> all = new ArrayList<>();
            for (BatteryHistoryDb.SessionRow sn : sessions) all.add(sn.capacityMah);
            r.medianMah = median(all);
            r.fromScreenOffSessions = false;
        }
        return r;
    }

    public static synchronized void setDesignCapacity(int mah) {
        if (mah < 0) mah = 0;
        designMah = mah;
        BatteryHistoryDb.get(appContext).setMeta("design_mah", String.valueOf(designMah));
    }

    private static void finishSegment(long endChargeMah, int endPercent) {
        if (!segmentActive) return;
        segmentActive = false;
        try {
            if (segmentStartChargeMah <= 0 || endChargeMah <= 0) return;
            if (segmentStartPercent < 0 || endPercent < 0) return;
            float dPercent = endPercent - segmentStartPercent;
            float dCharge = endChargeMah - segmentStartChargeMah;
            if (dPercent < MIN_DELTA_PERCENT || dCharge <= 0) return;
            if (segmentTotalMs < MIN_SEGMENT_MS) return;
            float estimate = dCharge * 100f / dPercent;
            if (estimate < MIN_ESTIMATE_MAH || estimate > MAX_ESTIMATE_MAH) return;
            boolean screenOffDominant = segmentScreenOnMs * 2 < segmentTotalMs;
            int samples = Math.max(segmentSamples, 1);
            BatteryHistoryDb.get(appContext).insertSession(
                    System.currentTimeMillis(), estimate, screenOffDominant, samples);
            sessions.add(new BatteryHistoryDb.SessionRow(
                    System.currentTimeMillis(), estimate, screenOffDominant, samples));
        } finally {
            segmentStartChargeMah = -1L;
            segmentStartPercent = -1;
            segmentScreenOnMs = 0;
            segmentTotalMs = 0;
            segmentSamples = 0;
            segmentStartMs = 0;
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

    private static void loadFromDb() {
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

    /** Import sekali file JSON lama ke database lalu file dihapus. */
    private static void migrateLegacyJsonIfNeeded() {
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
