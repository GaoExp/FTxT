package exp.ftxt.features.battery_stats;

import android.content.Context;

import java.util.ArrayList;

/**
 * Koordinator rekonstruksi sesi yang belum tersimpan saat proses dibunuh (§7.6 — Solusi A).
 *
 * <p>Sebelumnya {@link BatteryCapacityEstimator} dan {@link DischargeTracker} masing-masing
 * menjalankan query sampel 24 jam + screen-on oracle + segmentasi secara duplikat saat start.
 * Koordinator ini menjalankan query & segmentasi <b>sekali</b> lalu membagikan hasilnya ke kedua
 * tracker (beda arah: CHARGE vs DISCHARGE), sehingga beban query berat saat start berkurang
 * hingga setengahnya.</p>
 *
 * <p>Berjalan di thread latar — dipanggil dari {@code BatteryMonitor.start()}.</p>
 */
final class SessionRebuild {

    private SessionRebuild() {}

    /**
     * Menjalankan query sampel + segmentasi sekali, lalu merekonstruksi sesi pengisian
     * (estimator) dan pengosongan (discharge tracker) dari segmen hasil yang dibagi.
     * Mengembalikan segmen hasil (untuk kebutuhan khusus bila diperlukan).
     */
    static void run(Context context) {
        BatteryHistoryDb db = BatteryHistoryDb.get(context);
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

        BatteryCapacityEstimator.rebuildPendingSessions(db.lastChargeSessionEnd(), segs);
        DischargeTracker.rebuildPendingSessions(db.lastDischargeSessionEnd(), segs);
    }
}
