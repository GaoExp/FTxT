package exp.ftxt.features.battery_stats;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.PowerManager;

public class BatteryMonitor {

    /** Interval sampling dinamis hemat baterai (§2.6 rencana rework). */
    private static final long POLL_CHARGING_MS = 1_000;
    private static final long POLL_SCREEN_ON_MS = 1_000;
    private static final long POLL_SCREEN_OFF_MS = 1_000;

    /**
     * Persistensi sampel ke database ditulis tiap 5 detik, sementara pembacaan &
     * perhitungan (estimator kapasitas, discharge tracker) tetap dijalankan tiap
     * detik agar overlay & estimasi real-time. Hasilnya beban query riwayat DB
     * menjadi jauh lebih ringan (1/5 baris) tanpa mengorbankan presisi real-time.
     */
    private static final long DB_WRITE_INTERVAL_MS = 5_000;

    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static boolean running = false;
    private static Context appContext;
    private static BatteryReading.Snapshot lastSnapshot = BatteryReading.Snapshot.empty();
    private static HandlerThread bgThread;
    private static Handler bgHandler;
    private static volatile boolean busy;
    private static int lastActivityStatus = -1;
    private static long lastDbWriteTime = 0L;

    private static final Runnable tick = new Runnable() {
        @Override
        public void run() {
            if (!running) return;
            if (!busy && bgHandler != null) {
                busy = true;
                bgHandler.post(() -> {
                    final BatteryReading.Snapshot s = BatteryReading.read(appContext);
                    // Perhitungan (kapasitas & discharge) selalu tiap sampel (1 detik)
                    // agar estimasi tetap real-time & presisi.
                    BatteryCapacityEstimator.onSample(s);
                    DischargeTracker.onSample(s);

                    int activityStatus;
                    if (s.isCharging()) {
                        activityStatus = 2;
                    } else {
                        try {
                            PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                            activityStatus = (pm != null && pm.isInteractive()) ? 1 : 0;
                        } catch (Exception e) {
                            activityStatus = 0;
                        }
                    }

                    // Persistensi sampel dikurangi frekuensinya (tiap 5 detik) agar
                    // database riwayat tidak membengkak, sehingga query grafik/estimasi
                    // lebih ringan (mengurangi risiko ANR jangka panjang).
                    if (s.time - lastDbWriteTime >= DB_WRITE_INTERVAL_MS) {
                        try {
                            BatteryHistoryDb.get(appContext).insertSample(s);
                        } catch (Exception ignored) {}
                        lastDbWriteTime = s.time;
                    }
                    if (activityStatus != lastActivityStatus) {
                        try {
                            BatteryHistoryDb.get(appContext).insertActivityLog(s.time, activityStatus);
                        } catch (Exception ignored) {}
                        lastActivityStatus = activityStatus;
                    }

                    mainHandler.post(() -> {
                        busy = false;
                        if (!running) return;
                        lastSnapshot = s;
                        scheduleNext(delayFor(s));
                    });
                });
            } else {
                scheduleNext(POLL_CHARGING_MS);
            }
        }
    };

    private BatteryMonitor() {}

    public static synchronized void start(Context context) {
        if (running) return;
        appContext = context != null ? context.getApplicationContext() : appContext;
        if (appContext == null) return;
        BatteryCapacityEstimator.init(appContext);
        DischargeTracker.init(appContext);
        // Rekonstruksi sesi yang belum tersimpan & pruning data lama dipindah ke
        // thread latar agar query berat (querySamples 24 jam + segmentasi) tidak
        // memblokir main thread saat service start (penyebab ANR saat membuka aplikasi).
        // Query + segmentasi dijalankan sekali oleh SessionRebuild lalu hasilnya
        // dibagikan ke estimator & discharge tracker (tanpa duplikasi query).
        new Thread(() -> {
            try {
                SessionRebuild.run(appContext);
            } catch (Exception ignored) {}
            try {
                pruneOldData();
            } catch (Exception ignored) {}
        }, "BatterySessionRebuild").start();
        running = true;
        lastSnapshot = BatteryReading.Snapshot.empty();
        lastActivityStatus = -1;
        bgThread = new HandlerThread("batmon-read");
        bgThread.start();
        bgHandler = new Handler(bgThread.getLooper());
        mainHandler.post(tick);
    }

    public static synchronized void stop() {
        running = false;
        mainHandler.removeCallbacks(tick);
        // onMonitoringStopped() memicu finishSegment() yang melakukan DB write.
        // Dipindah ke bgHandler agar tidak memblokir main thread (penyebab ANR).
        if (bgHandler != null) {
            bgHandler.post(() -> {
                try {
                    BatteryCapacityEstimator.onMonitoringStopped();
                } catch (Exception ignored) {}
                try {
                    DischargeTracker.onMonitoringStopped();
                } catch (Exception ignored) {}
                bgThread.quitSafely();
            });
            bgThread = null;
            bgHandler = null;
        }
        busy = false;
        lastSnapshot = BatteryReading.Snapshot.empty();
    }

    public static synchronized boolean isRunning() {
        return running;
    }

    public static synchronized BatteryReading.Snapshot getLastSnapshot() {
        return lastSnapshot;
    }

    /**
     * Pruning data riwayat yang tidak lagi dibutuhkan. Riwayat metrik sampel &
     * log aktivitas hanya dibutuhkan sejauh 7 hari (estimator kapasitas memakai
     * rentang 7 hari, grafik riwayat maksimal 48 jam), sehingga baris yang lebih
     * tua dihapus agar database tidak membengkak dan query tetap ringan.
     * Berjalan di thread latar (dipanggil dari thread rebuild saat start).
     */
    private static void pruneOldData() {
        long cutoff = System.currentTimeMillis() - 7L * 24 * 3600_000L;
        BatteryHistoryDb db = BatteryHistoryDb.get(appContext);
        db.deleteSamplesOlderThan(cutoff);
        db.deleteActivityLogOlderThan(cutoff);
    }

    private static void scheduleNext(long delayMs) {
        mainHandler.removeCallbacks(tick);
        mainHandler.postDelayed(tick, delayMs);
    }

    private static long delayFor(BatteryReading.Snapshot s) {
        if (s != null && s.isCharging()) return POLL_CHARGING_MS;
        try {
            PowerManager pm =
                    (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
            if (pm != null && pm.isInteractive()) return POLL_SCREEN_ON_MS;
        } catch (Exception ignored) {}
        return POLL_SCREEN_OFF_MS;
    }
}
