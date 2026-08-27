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

    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static boolean running = false;
    private static Context appContext;
    private static BatteryReading.Snapshot lastSnapshot = BatteryReading.Snapshot.empty();
    private static HandlerThread bgThread;
    private static Handler bgHandler;
    private static volatile boolean busy;
    private static int lastActivityStatus = -1;

    private static final Runnable tick = new Runnable() {
        @Override
        public void run() {
            if (!running) return;
            if (!busy && bgHandler != null) {
                busy = true;
                bgHandler.post(() -> {
                    final BatteryReading.Snapshot s = BatteryReading.read(appContext);
                    BatteryHistoryDb.get(appContext).insertSample(s);
                    BatteryCapacityEstimator.onSample(s);

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
                    if (activityStatus != lastActivityStatus) {
                        BatteryHistoryDb.get(appContext).insertActivityLog(s.time, activityStatus);
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
        BatteryCapacityEstimator.onMonitoringStopped();
        if (bgThread != null) {
            bgThread.quitSafely();
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
