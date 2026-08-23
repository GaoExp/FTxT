package exp.ftxt.features.battery_stats;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Database riwayat baterai (SQLiteOpenHelper bawaan framework, tanpa Room).
 * Dua tabel: sampel metrik time-series (sumber kartu grafik & export snapshot)
 * dan sesi pengisian daya (bahan estimator kapasitas/kesehatan).
 * Tanpa auto-trim — biarkan riwayat hidup; ukuran per baris sangat kecil.
 */
public class BatteryHistoryDb extends SQLiteOpenHelper {

    private static final String DB_NAME = "battery_history.db";
    private static final int DB_VERSION = 1;

    private static final String T_SAMPLES = "samples";
    private static final String T_SESSIONS = "sessions";
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
                "sample_count INTEGER NOT NULL)");

        db.execSQL("CREATE TABLE " + T_META + " (" +
                "key TEXT PRIMARY KEY," +
                "value TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // versi 1 — belum ada upgrade
    }

    /** Baris sesi pengisian daya hasil estimasi. */
    public static final class SessionRow {
        public final long time;
        public final float capacityMah;
        public final boolean mostlyScreenOff;
        public final int sampleCount;

        SessionRow(long time, float capacityMah, boolean mostlyScreenOff, int sampleCount) {
            this.time = time;
            this.capacityMah = capacityMah;
            this.mostlyScreenOff = mostlyScreenOff;
            this.sampleCount = sampleCount;
        }
    }

    // ---------- sampel metrik ----------

    public void insertSample(BatteryReading.Snapshot s) {
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
     */
    public BatteryReading.Snapshot[] queryChart(long fromMs, long toMs, int targetPoints) {
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
                        + " AVG(current_ma), AVG(power_w), AVG(charge_mah)"
                        + " FROM " + T_SAMPLES + " WHERE time >= ? AND time <= ?"
                        + " GROUP BY (time - ?) / ? ORDER BY MIN(time) ASC",
                new String[]{String.valueOf(fromMs), String.valueOf(toMs),
                        String.valueOf(fromMs), String.valueOf(bucketMs)});
        try {
            while (cur.moveToNext()) {
                out.add(new BatteryReading.Snapshot(
                        cur.getLong(0),
                        cur.getFloat(1),
                        Math.round(cur.getFloat(2)),
                        cur.getFloat(3),
                        (int) cur.getFloat(4),
                        cur.getDouble(5),
                        Math.round(cur.getFloat(6)),
                        -1, 0, 0, null));
            }
        } finally {
            cur.close();
        }
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

    // ---------- sesi pengisian ----------

    public void insertSession(long time, float capacityMah, boolean mostlyScreenOff, int sampleCount) {
        ContentValues v = new ContentValues();
        v.put("time", time);
        v.put("capacity_mah", capacityMah);
        v.put("mostly_screen_off", mostlyScreenOff ? 1 : 0);
        v.put("sample_count", sampleCount);
        getWritableDatabase().insert(T_SESSIONS, null, v);
    }

    public ArrayList<SessionRow> getSessions() {
        ArrayList<SessionRow> out = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT time, capacity_mah, mostly_screen_off, sample_count FROM "
                        + T_SESSIONS + " ORDER BY time ASC", null);
        try {
            while (c.moveToNext()) {
                out.add(new SessionRow(c.getLong(0), c.getFloat(1),
                        c.getInt(2) == 1, c.getInt(3)));
            }
        } finally {
            c.close();
        }
        return out;
    }

    public void deleteAllSessions() {
        getWritableDatabase().delete(T_SESSIONS, null, null);
    }

    // ---------- meta ----------

    public void setMeta(String key, String value) {
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
