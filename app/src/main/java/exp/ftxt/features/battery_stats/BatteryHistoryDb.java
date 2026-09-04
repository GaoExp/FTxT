package exp.ftxt.features.battery_stats;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.BatteryManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Database riwayat baterai (SQLiteOpenHelper bawaan framework, tanpa Room).
 * Empat tabel: sampel metrik time-series (sumber kartu grafik & export snapshot),
 * sesi pengisian daya (metadata lengkap bahan estimator & riwayat sesi),
 * sesi pengosongan (discharge tracker) dan activity log.
 * Tanpa auto-trim — biarkan riwayat hidup; ukuran per baris sangat kecil.
 */
public class BatteryHistoryDb extends SQLiteOpenHelper {

    private static final String DB_NAME = "battery_history.db";
    private static final int DB_VERSION = 3;

    private static final String T_SAMPLES = "samples";
    private static final String T_SESSIONS = "sessions";
    private static final String T_DISCHARGE = "discharge_sessions";
    private static final String T_ACTIVITY = "activity_log";
    private static final String T_META = "meta";

    private static volatile BatteryHistoryDb instance;

    public static BatteryHistoryDb get(Context context) {
        if (instance == null) {
            synchronized (BatteryHistoryDb.class) {
                if (instance == null) {
                    instance = new BatteryHistoryDb(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private BatteryHistoryDb(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * Aktifkan Write-Ahead Logging (WAL) sejak awal. WAL membuat operasi baca tidak
     * pernah diblokir oleh operasi tulis (reader mendapat snapshot), sehingga query
     * grafik/estimasi dari thread UI/background tidak saling mengantre saat sampling
     * menulis setiap beberapa detik. Murni konfigurasi — tidak mengubah skema.
     */
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        try {
            db.enableWriteAheadLogging();
        } catch (Exception ignored) {}
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + T_SAMPLES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "time INTEGER NOT NULL," +
                "temp_c REAL NOT NULL," +
                "percent INTEGER NOT NULL," +
                "voltage_v REAL NOT NULL," +
                "current_ma INTEGER NOT NULL," +
                "power_w REAL NOT NULL," +
                "charge_mah INTEGER NOT NULL," +
                "cycle_count INTEGER NOT NULL," +
                "status INTEGER NOT NULL," +
                "plugged INTEGER NOT NULL," +
                "technology TEXT)");
        db.execSQL("CREATE INDEX idx_samples_time ON " + T_SAMPLES + "(time)");

        db.execSQL("CREATE TABLE " + T_SESSIONS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "time INTEGER NOT NULL," +
                "capacity_mah REAL NOT NULL," +
                "mostly_screen_off INTEGER NOT NULL," +
                "sample_count INTEGER NOT NULL," +
                "start_time INTEGER," +
                "end_time INTEGER," +
                "start_percent INTEGER," +
                "end_percent INTEGER," +
                "charge_mah_counter REAL," +
                "charge_mah_integral REAL," +
                "delta_charge_mah REAL," +
                "temp_min REAL," +
                "temp_max REAL," +
                "temp_avg REAL)");

        db.execSQL("CREATE TABLE " + T_DISCHARGE + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "start_time INTEGER NOT NULL," +
                "end_time INTEGER NOT NULL," +
                "start_percent INTEGER NOT NULL," +
                "end_percent INTEGER NOT NULL," +
                "used_mah_counter REAL NOT NULL," +
                "used_mah_integral REAL NOT NULL," +
                "capacity_mah REAL," +
                "efficiency_discharge REAL," +
                "screen_off_dominant INTEGER NOT NULL," +
                "temp_min REAL," +
                "temp_max REAL," +
                "temp_avg REAL," +
                "sample_count INTEGER NOT NULL)");
        db.execSQL("CREATE INDEX idx_discharge_time ON " + T_DISCHARGE + "(start_time)");

        db.execSQL("CREATE TABLE " + T_ACTIVITY + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "time INTEGER NOT NULL," +
                "status INTEGER NOT NULL)");
        db.execSQL("CREATE INDEX idx_activity_time ON " + T_ACTIVITY + "(time)");

        db.execSQL("CREATE TABLE " + T_META + " (" +
                "key TEXT PRIMARY KEY," +
                "value TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("CREATE TABLE " + T_ACTIVITY + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "time INTEGER NOT NULL," +
                    "status INTEGER NOT NULL)");
            db.execSQL("CREATE INDEX idx_activity_time ON " + T_ACTIVITY + "(time)");
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + T_SESSIONS + " ADD COLUMN start_time INTEGER");
            db.execSQL("ALTER TABLE " + T_SESSIONS + " ADD COLUMN end_time INTEGER");
            db.execSQL("ALTER TABLE " + T_SESSIONS + " ADD COLUMN start_percent INTEGER");
            db.execSQL("ALTER TABLE " + T_SESSIONS + " ADD COLUMN end_percent INTEGER");
            db.execSQL("ALTER TABLE " + T_SESSIONS + " ADD COLUMN charge_mah_counter REAL");
            db.execSQL("ALTER TABLE " + T_SESSIONS + " ADD COLUMN charge_mah_integral REAL");
            db.execSQL("ALTER TABLE " + T_SESSIONS + " ADD COLUMN delta_charge_mah REAL");
            db.execSQL("ALTER TABLE " + T_SESSIONS + " ADD COLUMN temp_min REAL");
            db.execSQL("ALTER TABLE " + T_SESSIONS + " ADD COLUMN temp_max REAL");
            db.execSQL("ALTER TABLE " + T_SESSIONS + " ADD COLUMN temp_avg REAL");
            db.execSQL("CREATE TABLE " + T_DISCHARGE + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "start_time INTEGER NOT NULL," +
                    "end_time INTEGER NOT NULL," +
                    "start_percent INTEGER NOT NULL," +
                    "end_percent INTEGER NOT NULL," +
                    "used_mah_counter REAL NOT NULL," +
                    "used_mah_integral REAL NOT NULL," +
                    "capacity_mah REAL," +
                    "efficiency_discharge REAL," +
                    "screen_off_dominant INTEGER NOT NULL," +
                    "temp_min REAL," +
                    "temp_max REAL," +
                    "temp_avg REAL," +
                    "sample_count INTEGER NOT NULL)");
            db.execSQL("CREATE INDEX idx_discharge_time ON " + T_DISCHARGE + "(start_time)");
        }
    }

    /** Baris sesi pengisian daya hasil estimasi (metadata lengkap). */
    public static final class SessionRow {
        public final long time;
        public final float capacityMah;
        public final boolean mostlyScreenOff;
        public final int sampleCount;
        public final long startTime;
        public final long endTime;
        public final int startPercent;
        public final int endPercent;
        public final double mAhCounter;
        public final double mAhIntegral;
        public final double deltaChargeMah;
        public final float tempMin;
        public final float tempMax;
        public final float tempAvg;

        /** Sesi tanpa metadata (mis. migrasi JSON lama atau data v2). */
        SessionRow(long time, float capacityMah, boolean mostlyScreenOff, int sampleCount) {
            this(time, capacityMah, mostlyScreenOff, sampleCount,
                    time, time, -1, -1, 0d, 0d, 0d, 0f, 0f, 0f);
        }

        SessionRow(long time, float capacityMah, boolean mostlyScreenOff, int sampleCount,
                   long startTime, long endTime, int startPercent, int endPercent,
                   double mAhCounter, double mAhIntegral, double deltaChargeMah,
                   float tempMin, float tempMax, float tempAvg) {
            this.time = time;
            this.capacityMah = capacityMah;
            this.mostlyScreenOff = mostlyScreenOff;
            this.sampleCount = sampleCount;
            this.startTime = startTime;
            this.endTime = endTime;
            this.startPercent = startPercent;
            this.endPercent = endPercent;
            this.mAhCounter = mAhCounter;
            this.mAhIntegral = mAhIntegral;
            this.deltaChargeMah = deltaChargeMah;
            this.tempMin = tempMin;
            this.tempMax = tempMax;
            this.tempAvg = tempAvg;
        }
    }

    /** Baris sesi pengosongan daya (discharge tracker). */
    public static final class DischargeSession {
        public final long startTime;
        public final long endTime;
        public final int startPercent;
        public final int endPercent;
        public final double usedMahCounter;
        public final double usedMahIntegral;
        public final float capacityMah;
        public final float efficiencyPercent;
        public final boolean screenOffDominant;
        public final float tempMin;
        public final float tempMax;
        public final float tempAvg;
        public final int sampleCount;

        DischargeSession(long startTime, long endTime, int startPercent, int endPercent,
                         double usedMahCounter, double usedMahIntegral, float capacityMah,
                         float efficiencyPercent, boolean screenOffDominant,
                         float tempMin, float tempMax, float tempAvg, int sampleCount) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.startPercent = startPercent;
            this.endPercent = endPercent;
            this.usedMahCounter = usedMahCounter;
            this.usedMahIntegral = usedMahIntegral;
            this.capacityMah = capacityMah;
            this.efficiencyPercent = efficiencyPercent;
            this.screenOffDominant = screenOffDominant;
            this.tempMin = tempMin;
            this.tempMax = tempMax;
            this.tempAvg = tempAvg;
            this.sampleCount = sampleCount;
        }
    }

    /** Entri sesi gabungan (pengisian/pengosongan) untuk daftar & detail sesi. */
    public static final class SessionEntry {
        public final boolean isCharge;
        public final long startTime;
        public final long endTime;
        public final int startPercent;
        public final int endPercent;
        public final double mAhCounter;
        public final double mAhIntegral;
        public final double deltaChargeMah;
        public final float capacityMah;
        public final float efficiencyPercent;
        public final boolean screenOffDominant;
        public final float tempMin;
        public final float tempMax;
        public final float tempAvg;
        public final int sampleCount;

        SessionEntry(SessionRow r) {
            this.isCharge = true;
            this.startTime = r.startTime;
            this.endTime = r.endTime;
            this.startPercent = r.startPercent;
            this.endPercent = r.endPercent;
            this.mAhCounter = r.mAhCounter;
            this.mAhIntegral = r.mAhIntegral;
            this.deltaChargeMah = r.deltaChargeMah;
            this.capacityMah = r.capacityMah;
            this.efficiencyPercent = -1f;
            this.screenOffDominant = r.mostlyScreenOff;
            this.tempMin = r.tempMin;
            this.tempMax = r.tempMax;
            this.tempAvg = r.tempAvg;
            this.sampleCount = r.sampleCount;
        }

        SessionEntry(DischargeSession d) {
            this.isCharge = false;
            this.startTime = d.startTime;
            this.endTime = d.endTime;
            this.startPercent = d.startPercent;
            this.endPercent = d.endPercent;
            this.mAhCounter = d.usedMahCounter;
            this.mAhIntegral = d.usedMahIntegral;
            this.deltaChargeMah = 0d;
            this.capacityMah = d.capacityMah;
            this.efficiencyPercent = d.efficiencyPercent;
            this.screenOffDominant = d.screenOffDominant;
            this.tempMin = d.tempMin;
            this.tempMax = d.tempMax;
            this.tempAvg = d.tempAvg;
            this.sampleCount = d.sampleCount;
        }

        public long durationMs() {
            return Math.max(0L, endTime - startTime);
        }
    }

    /** Agregasi harian/bulanan untuk grafik batang riwayat sesi. */
    public static final class BarAggregate {
        public final long bucketStartMs;
        public final float chargePercent;
        public final float dischargePercent;
        public final int chargeCount;
        public final int dischargeCount;

        BarAggregate(long bucketStartMs, float chargePercent, float dischargePercent,
                     int chargeCount, int dischargeCount) {
            this.bucketStartMs = bucketStartMs;
            this.chargePercent = chargePercent;
            this.dischargePercent = dischargePercent;
            this.chargeCount = chargeCount;
            this.dischargeCount = dischargeCount;
        }
    }

    // ---------- sampel metrik ----------

    public synchronized void insertSample(BatteryReading.Snapshot s) {
        ContentValues v = new ContentValues();
        v.put("time", s.time);
        v.put("temp_c", s.tempC);
        v.put("percent", s.percent);
        v.put("voltage_v", s.voltageV);
        v.put("current_ma", s.currentMa);
        v.put("power_w", s.powerW);
        v.put("charge_mah", s.chargeMah);
        v.put("cycle_count", s.cycleCount);
        v.put("status", s.statusInt);
        v.put("plugged", s.pluggedInt);
        if (s.technology != null) v.put("technology", s.technology);
        getWritableDatabase().insert(T_SAMPLES, null, v);
    }

    /** Semua sampel sejak fromMs, urut waktu naik. */
    public BatteryReading.Snapshot[] querySamples(long fromMs) {
        return query(fromMs, Long.MAX_VALUE, -1);
    }

    /** N sampel terakhir, urut waktu naik. */
    public BatteryReading.Snapshot[] queryLastSamples(int limit) {
        return query(Long.MIN_VALUE, Long.MAX_VALUE, limit);
    }

    /**
     * Sampel untuk grafik pada rentang [fromMs, toMs]. Bila jumlah baris melebihi
     * targetPoints, baris dirata-rata per bucket waktu agar hasil ringan digambar.
     * Hasil akhirnya di-resample ke grid waktu SERAGAM (step tetap) sehingga
     * kepadatan titik grafik konsisten — pola garis tidak berubah mengikuti
     * interval sampling yang sedang aktif saat data direkam (charging 1 dtk vs idle).
     */
    public BatteryReading.Snapshot[] queryChart(long fromMs, long toMs, int targetPoints) {
        BatteryReading.Snapshot[] raw = queryChartRaw(fromMs, toMs, targetPoints);
        return resampleUniform(raw, fromMs, toMs, targetPoints);
    }

    private BatteryReading.Snapshot[] queryChartRaw(long fromMs, long toMs, int targetPoints) {
        SQLiteDatabase db = getReadableDatabase();
        long count = 0;
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + T_SAMPLES
                + " WHERE time >= ? AND time <= ?", new String[]{
                String.valueOf(fromMs), String.valueOf(toMs)});
        if (c.moveToFirst()) count = c.getLong(0);
        c.close();
        if (count <= targetPoints) return query(fromMs, toMs, -1);

        long span = Math.max(1L, toMs - fromMs);
        long bucketMs = Math.max(1000L, span / targetPoints);
        ArrayList<BatteryReading.Snapshot> out = new ArrayList<>();
        Cursor cur = db.rawQuery("SELECT MIN(time), AVG(temp_c), AVG(percent), AVG(voltage_v),"
                        + " AVG(current_ma), AVG(power_w), AVG(charge_mah),"
                        + " AVG(status IN (" + BatteryManager.BATTERY_STATUS_CHARGING
                        + "," + BatteryManager.BATTERY_STATUS_FULL + "))"
                        + " FROM " + T_SAMPLES + " WHERE time >= ? AND time <= ?"
                        + " GROUP BY (time - ?) / ? ORDER BY MIN(time) ASC",
                new String[]{String.valueOf(fromMs), String.valueOf(toMs),
                        String.valueOf(fromMs), String.valueOf(bucketMs)});
        try {
            while (cur.moveToNext()) {
                int status = cur.getFloat(7) >= 0.5f
                        ? BatteryManager.BATTERY_STATUS_CHARGING
                        : BatteryManager.BATTERY_STATUS_DISCHARGING;
                out.add(new BatteryReading.Snapshot(
                        cur.getLong(0),
                        cur.getFloat(1),
                        Math.round(cur.getFloat(2)),
                        cur.getFloat(3),
                        (int) cur.getFloat(4),
                        cur.getDouble(5),
                        Math.round(cur.getFloat(6)),
                        -1, status, 0, null));
            }
        } finally {
            cur.close();
        }
        return out.toArray(new BatteryReading.Snapshot[0]);
    }

    /** Interpolasi linear ke titik-titik berjarak waktu sama (maks. targetPoints). */
    private static BatteryReading.Snapshot[] resampleUniform(
            BatteryReading.Snapshot[] raw, long fromMs, long toMs, int targetPoints) {
        int n = raw.length;
        if (n < 2) return raw;
        long start = Math.max(fromMs, raw[0].time);
        long end = Math.min(toMs, raw[n - 1].time);
        long step = Math.max(1000L, (end - start) / targetPoints);
        ArrayList<BatteryReading.Snapshot> out = new ArrayList<>();
        int j = 0;
        for (long t = start; t <= end && out.size() < targetPoints; t += step) {
            while (j + 1 < n && raw[j + 1].time <= t) j++;
            BatteryReading.Snapshot a = raw[j];
            int k = Math.min(j + 1, n - 1);
            BatteryReading.Snapshot b = raw[k];
            if (b.time <= a.time) {
                out.add(a);
                continue;
            }
            float frac = (t - a.time) / (float) (b.time - a.time);
            out.add(new BatteryReading.Snapshot(
                    t,
                    a.tempC + (b.tempC - a.tempC) * frac,
                    Math.round(a.percent + (b.percent - a.percent) * frac),
                    a.voltageV + (b.voltageV - a.voltageV) * frac,
                    Math.round(a.currentMa + (b.currentMa - a.currentMa) * frac),
                    a.powerW + (b.powerW - a.powerW) * frac,
                    Math.round(a.chargeMah + (b.chargeMah - a.chargeMah) * frac),
                    a.cycleCount, a.statusInt, a.pluggedInt, a.technology));
        }
        if (out.isEmpty()) return raw;
        return out.toArray(new BatteryReading.Snapshot[0]);
    }

    private BatteryReading.Snapshot[] query(long fromMs, long toMs, int limit) {
        ArrayList<BatteryReading.Snapshot> out = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        StringBuilder sql = new StringBuilder("SELECT time, temp_c, percent, voltage_v,"
                + " current_ma, power_w, charge_mah, cycle_count, status, plugged, technology"
                + " FROM " + T_SAMPLES);
        boolean hasFrom = fromMs != Long.MIN_VALUE;
        boolean hasTo = toMs != Long.MAX_VALUE;
        if (hasFrom || hasTo) {
            sql.append(" WHERE");
            if (hasFrom) sql.append(" time >= ").append(fromMs);
            if (hasFrom && hasTo) sql.append(" AND");
            if (hasTo) sql.append(" time <= ").append(toMs);
        }
        sql.append(" ORDER BY time ");
        sql.append(limit > 0 ? "DESC" : "ASC");
        if (limit > 0) sql.append(" LIMIT ").append(limit);

        Cursor c = db.rawQuery(sql.toString(), null);
        try {
            while (c.moveToNext()) {
                out.add(new BatteryReading.Snapshot(
                        c.getLong(0), c.getFloat(1), c.getInt(2), c.getFloat(3),
                        c.getInt(4), c.getDouble(5), c.getLong(6), c.getInt(7),
                        c.getInt(8), c.getInt(9), c.isNull(10) ? null : c.getString(10)));
            }
        } finally {
            c.close();
        }
        return out.toArray(new BatteryReading.Snapshot[0]);
    }

    // ---------- estimasi waktu ----------

    /**
     * Mengembalikan estimasi waktu (ms) hingga baterai penuh atau habis.
     * Charging: rate dihitung dari 10 menit terakhir, lalu extrapolate ke 100%.
     * Discharging: rate dihitung dari 30 menit terakhir, lalu extrapolate ke 0%.
     * Mengembalikan -1 jika data tidak cukup.
     */
    public long estimateTimeRemaining(boolean charging) {
        long now = System.currentTimeMillis();
        long windowMs = charging ? 600_000L : 1_800_000L;
        BatteryReading.Snapshot[] samples = query(now - windowMs, now, -1);
        if (samples.length < 2) return -1;

        long tFirst = samples[0].time;
        long tLast = samples[samples.length - 1].time;
        long dt = tLast - tFirst;
        if (dt < 30_000L) return -1;

        int pFirst = samples[0].percent;
        int pLast = samples[samples.length - 1].percent;
        double ratePerMs = (double) (pLast - pFirst) / dt;
        if (Math.abs(ratePerMs) < 1e-9) return -1;

        if (charging) {
            int remaining = 100 - pLast;
            if (remaining <= 0) return 0;
            return (long) (remaining / ratePerMs);
        } else {
            if (pLast <= 0) return 0;
            return (long) (pLast / -ratePerMs);
        }
    }

    // ---------- riwayat sesi pengisian ----------

    /** Hasil analisis satu sesi pengisian dari data sampel. */
    public static final class ChargingSession {
        public final long startTime;
        public final long endTime;
        public final int startPercent;
        public final int endPercent;
        public final long durationMs;
        public final String pluggedType;

        ChargingSession(long startTime, long endTime, int startPercent, int endPercent,
                        long durationMs, String pluggedType) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.startPercent = startPercent;
            this.endPercent = endPercent;
            this.durationMs = durationMs;
            this.pluggedType = pluggedType;
        }
    }

    /**
     * Mengembalikan 5 sesi pengisian terakhir dari tabel sampel.
     * Sesi = rangkaian sampel berurutan dengan status charging/full.
     * Sesi baru dimulai setelah gap >15 menit tanpa data charging.
     */
    public ArrayList<ChargingSession> queryChargingSessions(int maxSessions) {
        long now = System.currentTimeMillis();
        long fromMs = now - 7L * 24 * 3600_000L;
        BatteryReading.Snapshot[] all = query(fromMs, now, -1);
        if (all.length < 2) return new ArrayList<>();

        ArrayList<ChargingSession> sessions = new ArrayList<>();
        long segStart = -1;
        int segStartPercent = -1;
        long segLastTime = -1;
        int segLastPercent = -1;
        String segPlugged = null;
        long GAP_MS = 900_000L;

        for (BatteryReading.Snapshot s : all) {
            boolean charging = s.statusInt == android.os.BatteryManager.BATTERY_STATUS_CHARGING
                    || s.statusInt == android.os.BatteryManager.BATTERY_STATUS_FULL;
            if (charging) {
                if (segStart < 0) {
                    segStart = s.time;
                    segStartPercent = s.percent;
                    segPlugged = pluggedLabel(s.pluggedInt);
                }
                segLastTime = s.time;
                segLastPercent = s.percent;
            } else {
                if (segStart >= 0 && segLastPercent > segStartPercent) {
                    sessions.add(new ChargingSession(
                            segStart, segLastTime, segStartPercent, segLastPercent,
                            segLastTime - segStart, segPlugged));
                }
                segStart = -1;
                segStartPercent = -1;
                segPlugged = null;
            }
        }
        if (segStart >= 0 && segLastPercent > segStartPercent) {
            sessions.add(new ChargingSession(
                    segStart, segLastTime, segStartPercent, segLastPercent,
                    segLastTime - segStart, segPlugged));
        }

        ArrayList<ChargingSession> result = new ArrayList<>();
        for (int i = sessions.size() - 1; i >= 0 && result.size() < maxSessions; i--) {
            result.add(sessions.get(i));
        }
        return result;
    }

    private static String pluggedLabel(int plugged) {
        switch (plugged) {
            case android.os.BatteryManager.BATTERY_PLUGGED_AC: return "AC";
            case android.os.BatteryManager.BATTERY_PLUGGED_USB: return "USB";
            case android.os.BatteryManager.BATTERY_PLUGGED_WIRELESS: return "Wireless";
            default: return "";
        }
    }

    // ---------- sesi pengisian (estimator) ----------

    public void insertSession(long time, float capacityMah, boolean mostlyScreenOff, int sampleCount) {
        insertSessionFull(new SessionRow(time, capacityMah, mostlyScreenOff, sampleCount));
    }

    public synchronized void insertSessionFull(SessionRow r) {
        ContentValues v = new ContentValues();
        v.put("time", r.time);
        v.put("capacity_mah", r.capacityMah);
        v.put("mostly_screen_off", r.mostlyScreenOff ? 1 : 0);
        v.put("sample_count", r.sampleCount);
        if (r.startTime > 0) v.put("start_time", r.startTime);
        if (r.endTime > 0) v.put("end_time", r.endTime);
        if (r.startPercent >= 0) v.put("start_percent", r.startPercent);
        if (r.endPercent >= 0) v.put("end_percent", r.endPercent);
        v.put("charge_mah_counter", r.mAhCounter);
        v.put("charge_mah_integral", r.mAhIntegral);
        v.put("delta_charge_mah", r.deltaChargeMah);
        v.put("temp_min", r.tempMin);
        v.put("temp_max", r.tempMax);
        v.put("temp_avg", r.tempAvg);
        getWritableDatabase().insert(T_SESSIONS, null, v);
    }

    public ArrayList<SessionRow> getSessions() {
        ArrayList<SessionRow> out = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT time, capacity_mah, mostly_screen_off, sample_count,"
                        + " start_time, end_time, start_percent, end_percent,"
                        + " charge_mah_counter, charge_mah_integral, delta_charge_mah,"
                        + " temp_min, temp_max, temp_avg"
                        + " FROM " + T_SESSIONS + " ORDER BY time ASC", null);
        try {
            while (c.moveToNext()) {
                out.add(new SessionRow(c.getLong(0), c.getFloat(1),
                        c.getInt(2) == 1, c.getInt(3),
                        c.isNull(4) ? c.getLong(0) : c.getLong(4),
                        c.isNull(5) ? c.getLong(0) : c.getLong(5),
                        c.isNull(6) ? -1 : c.getInt(6),
                        c.isNull(7) ? -1 : c.getInt(7),
                        c.isNull(8) ? 0d : c.getDouble(8),
                        c.isNull(9) ? 0d : c.getDouble(9),
                        c.isNull(10) ? 0d : c.getDouble(10),
                        c.isNull(11) ? 0f : c.getFloat(11),
                        c.isNull(12) ? 0f : c.getFloat(12),
                        c.isNull(13) ? 0f : c.getFloat(13)));
            }
        } finally {
            c.close();
        }
        return out;
    }

    public synchronized void deleteAllSessions() {
        getWritableDatabase().delete(T_SESSIONS, null, null);
    }

    public synchronized void deleteAllDischargeSessions() {
        getWritableDatabase().delete(T_DISCHARGE, null, null);
    }

    /** Hapus sampel metrik yang lebih tua dari {@code beforeMs} (pruning data lama agar riwayat tidak membengkak). */
    public synchronized void deleteSamplesOlderThan(long beforeMs) {
        getWritableDatabase().delete(T_SAMPLES, "time < ?",
                new String[]{String.valueOf(beforeMs)});
    }

    /** Hapus log aktivitas layar/status yang lebih tua dari {@code beforeMs}. */
    public synchronized void deleteActivityLogOlderThan(long beforeMs) {
        getWritableDatabase().delete(T_ACTIVITY, "time < ?",
                new String[]{String.valueOf(beforeMs)});
    }

    /** Sesi pengisian (tabel serial resmi estimator) pada rentang [fromMs, toMs]. */
    public ArrayList<SessionRow> queryChargeSessions(long fromMs, long toMs) {
        ArrayList<SessionRow> out = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT time, capacity_mah, mostly_screen_off, sample_count,"
                        + " start_time, end_time, start_percent, end_percent,"
                        + " charge_mah_counter, charge_mah_integral, delta_charge_mah,"
                        + " temp_min, temp_max, temp_avg"
                        + " FROM " + T_SESSIONS
                        + " WHERE end_time >= ? AND start_time <= ?"
                        + " ORDER BY start_time ASC",
                new String[]{String.valueOf(fromMs), String.valueOf(toMs)});
        try {
            while (c.moveToNext()) {
                out.add(new SessionRow(c.getLong(0), c.getFloat(1),
                        c.getInt(2) == 1, c.getInt(3),
                        c.isNull(4) ? c.getLong(0) : c.getLong(4),
                        c.isNull(5) ? c.getLong(0) : c.getLong(5),
                        c.isNull(6) ? -1 : c.getInt(6),
                        c.isNull(7) ? -1 : c.getInt(7),
                        c.isNull(8) ? 0d : c.getDouble(8),
                        c.isNull(9) ? 0d : c.getDouble(9),
                        c.isNull(10) ? 0d : c.getDouble(10),
                        c.isNull(11) ? 0f : c.getFloat(11),
                        c.isNull(12) ? 0f : c.getFloat(12),
                        c.isNull(13) ? 0f : c.getFloat(13)));
            }
        } finally {
            c.close();
        }
        return out;
    }

    // ---------- sesi pengosongan (discharge tracker) ----------

    public synchronized void insertDischargeSession(DischargeSession s) {
        ContentValues v = new ContentValues();
        v.put("start_time", s.startTime);
        v.put("end_time", s.endTime);
        v.put("start_percent", s.startPercent);
        v.put("end_percent", s.endPercent);
        v.put("used_mah_counter", s.usedMahCounter);
        v.put("used_mah_integral", s.usedMahIntegral);
        v.put("capacity_mah", s.capacityMah);
        v.put("efficiency_discharge", s.efficiencyPercent);
        v.put("screen_off_dominant", s.screenOffDominant ? 1 : 0);
        v.put("temp_min", s.tempMin);
        v.put("temp_max", s.tempMax);
        v.put("temp_avg", s.tempAvg);
        v.put("sample_count", s.sampleCount);
        getWritableDatabase().insert(T_DISCHARGE, null, v);
    }

    /** Sesi pengosongan pada rentang [fromMs, toMs], urut mulai naik. */
    public ArrayList<DischargeSession> queryDischargeSessions(long fromMs, long toMs) {
        ArrayList<DischargeSession> out = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT start_time, end_time, start_percent, end_percent,"
                        + " used_mah_counter, used_mah_integral, capacity_mah,"
                        + " efficiency_discharge, screen_off_dominant,"
                        + " temp_min, temp_max, temp_avg, sample_count"
                        + " FROM " + T_DISCHARGE
                        + " WHERE end_time >= ? AND start_time <= ?"
                        + " ORDER BY start_time ASC",
                new String[]{String.valueOf(fromMs), String.valueOf(toMs)});
        try {
            while (c.moveToNext()) {
                out.add(new DischargeSession(c.getLong(0), c.getLong(1),
                        c.getInt(2), c.getInt(3),
                        c.getDouble(4), c.getDouble(5),
                        c.isNull(6) ? 0f : c.getFloat(6),
                        c.isNull(7) ? -1f : c.getFloat(7),
                        c.getInt(8) == 1,
                        c.isNull(9) ? 0f : c.getFloat(9),
                        c.isNull(10) ? 0f : c.getFloat(10),
                        c.isNull(11) ? 0f : c.getFloat(11),
                        c.getInt(12)));
            }
        } finally {
            c.close();
        }
        return out;
    }

    /** Gabungan sesi pengisian & pengosongan pada rentang, urut waktu naik. */
    public ArrayList<SessionEntry> querySessionEntries(long fromMs, long toMs) {
        ArrayList<SessionEntry> out = new ArrayList<>();
        for (SessionRow r : queryChargeSessions(fromMs, toMs)) out.add(new SessionEntry(r));
        for (DischargeSession d : queryDischargeSessions(fromMs, toMs)) out.add(new SessionEntry(d));
        Collections.sort(out, new Comparator<SessionEntry>() {
            @Override
            public int compare(SessionEntry a, SessionEntry b) {
                return Long.compare(a.startTime, b.startTime);
            }
        });
        return out;
    }

/**
      * Agregat sesi per hari (MODE_DAILY), per minggu (MODE_WEEKLY), atau
      * per bulan (MODE_MONTHLY) pada rentang [fromMs, toMs] untuk grafik
      * batang. Urut naik.
      */
    public static final int MODE_DAILY = 0;
    public static final int MODE_WEEKLY = 1;
    public static final int MODE_MONTHLY = 2;

    public ArrayList<BarAggregate> queryBarAggregates(long fromMs, long toMs, int mode) {
        Map<Long, BarAggregate> map = new HashMap<>();
        for (SessionRow r : queryChargeSessions(fromMs, toMs)) {
            long bucket = bucketStart(r.endTime, mode);
            BarAggregate a = map.get(bucket);
            if (a == null) {
                a = new BarAggregate(bucket, 0f, 0f, 0, 0);
                map.put(bucket, a);
            }
            map.put(bucket, new BarAggregate(a.bucketStartMs,
                    a.chargePercent + (float) (r.endPercent >= 0 && r.startPercent >= 0
                            ? (r.endPercent - r.startPercent) : 0),
                    a.dischargePercent, a.chargeCount + 1, a.dischargeCount));
        }
        for (DischargeSession d : queryDischargeSessions(fromMs, toMs)) {
            long bucket = bucketStart(d.endTime, mode);
            BarAggregate a = map.get(bucket);
            if (a == null) {
                a = new BarAggregate(bucket, 0f, 0f, 0, 0);
                map.put(bucket, a);
            }
            map.put(bucket, new BarAggregate(a.bucketStartMs, a.chargePercent,
                    a.dischargePercent + (float) (d.startPercent - d.endPercent),
                    a.chargeCount, a.dischargeCount + 1));
        }
        ArrayList<BarAggregate> out = new ArrayList<>(map.values());
        Collections.sort(out, new Comparator<BarAggregate>() {
            @Override
            public int compare(BarAggregate a, BarAggregate b) {
                return Long.compare(a.bucketStartMs, b.bucketStartMs);
            }
        });
        return out;
    }

    private static long bucketStart(long ms, int mode) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ms);
        if (mode == MODE_MONTHLY) {
            c.set(Calendar.DAY_OF_MONTH, 1);
        } else if (mode == MODE_WEEKLY) {
            int dow = c.get(Calendar.DAY_OF_WEEK);
            if (dow != Calendar.MONDAY) {
                c.add(Calendar.DAY_OF_YEAR, -(dow - Calendar.MONDAY));
            }
        }
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    // ---------- activity log ----------

    /** Akhir sesi pengisian terakhir yang tersimpan (end_time maks); -1 bila kosong/tanpa waktu. */
    public long lastChargeSessionEnd() {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT MAX(end_time) FROM " + T_SESSIONS, null);
        try {
            return c.moveToFirst() && !c.isNull(0) ? c.getLong(0) : -1L;
        } finally {
            c.close();
        }
    }

    /** Akhir sesi pengosongan terakhir yang tersimpan (end_time maks); -1 bila kosong. */
    public long lastDischargeSessionEnd() {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT MAX(end_time) FROM " + T_DISCHARGE, null);
        try {
            return c.moveToFirst() && !c.isNull(0) ? c.getLong(0) : -1L;
        } finally {
            c.close();
        }
    }

    /** Oracle layar-nyala pada rentang [fromMs, toMs] dari activity log. */
    public SessionSegmentBuilder.ScreenOnOracle screenOnOracle(long fromMs, long toMs) {
        ArrayList<ActivityLog> logs = queryActivityLog(fromMs, toMs);
        int n = logs.size();
        long[] times = new long[n];
        boolean[] on = new boolean[n];
        for (int i = 0; i < n; i++) {
            times[i] = logs.get(i).time;
            on[i] = logs.get(i).status == 1;
        }
        return SessionSegmentBuilder.screenOnOracle(times, on);
    }

    public static final class ActivityLog {
        public final long time;
        public final int status;

        public ActivityLog(long time, int status) {
            this.time = time;
            this.status = status;
        }
    }

    public synchronized void insertActivityLog(long time, int status) {
        ContentValues v = new ContentValues();
        v.put("time", time);
        v.put("status", status);
        getWritableDatabase().insert(T_ACTIVITY, null, v);
    }

    /** Query activity log pada rentang [fromMs, toMs], urut waktu naik. */
    public ArrayList<ActivityLog> queryActivityLog(long fromMs, long toMs) {
        ArrayList<ActivityLog> out = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT time, status FROM " + T_ACTIVITY
                        + " WHERE time >= ? AND time <= ? ORDER BY time ASC",
                new String[]{String.valueOf(fromMs), String.valueOf(toMs)});
        try {
            while (c.moveToNext()) {
                out.add(new ActivityLog(c.getLong(0), c.getInt(1)));
            }
        } finally {
            c.close();
        }
        return out;
    }

    // ---------- meta ----------

    public synchronized void setMeta(String key, String value) {
        ContentValues v = new ContentValues();
        v.put("key", key);
        v.put("value", value);
        getWritableDatabase().insertWithOnConflict(T_META, null, v,
                SQLiteDatabase.CONFLICT_REPLACE);
    }

    public String getMeta(String key) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT value FROM " + T_META + " WHERE key = ?", new String[]{key});
        try {
            return c.moveToFirst() ? c.getString(0) : null;
        } finally {
            c.close();
        }
    }
}
