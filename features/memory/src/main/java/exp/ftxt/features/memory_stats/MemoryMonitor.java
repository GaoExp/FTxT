package exp.ftxt.features.memory_stats;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayDeque;

public class MemoryMonitor {
    public static final int HISTORY_SIZE = 20;
    private static final long POLL_MS = 1000;

    private static final Handler handler = new Handler(Looper.getMainLooper());
    private static final ArrayDeque<Snapshot> history = new ArrayDeque<>(HISTORY_SIZE);
    private static boolean running = false;
    private static Context appContext;
    private static Snapshot lastSnapshot = emptySnapshot();

    public static class Snapshot {
        public final long time;
        public final int javaKb;
        public final int nativeKb;
        public final int graphicsKb;
        public final int otherPssKb;
        public final int totalKb;
        public final int privateDirtyKb;
        public final int privateCleanKb;
        public final int sharedDirtyKb;
        public final int swappedKb;
        public final int codeKb;
        public final int stackKb;
        public final int systemKb;
        public final int privateOtherKb;
        public final int systemOtherKb;
        public final long heapUsedKb;
        public final long heapFreeKb;
        public final long heapMaxKb;
        public final long totalRamKb;
        public final long availRamKb;
        public final long cachedKb;

        Snapshot(long time, int javaKb, int nativeKb, int graphicsKb, int otherPssKb, int totalKb,
                 int privateDirtyKb, int privateCleanKb, int sharedDirtyKb, int swappedKb,
                 int codeKb, int stackKb, int systemKb, int privateOtherKb, int systemOtherKb,
                 long heapUsedKb, long heapFreeKb, long heapMaxKb,
                 long totalRamKb, long availRamKb, long cachedKb) {
            this.time = time;
            this.javaKb = javaKb;
            this.nativeKb = nativeKb;
            this.graphicsKb = graphicsKb;
            this.otherPssKb = otherPssKb;
            this.totalKb = totalKb;
            this.privateDirtyKb = privateDirtyKb;
            this.privateCleanKb = privateCleanKb;
            this.sharedDirtyKb = sharedDirtyKb;
            this.swappedKb = swappedKb;
            this.codeKb = codeKb;
            this.stackKb = stackKb;
            this.systemKb = systemKb;
            this.privateOtherKb = privateOtherKb;
            this.systemOtherKb = systemOtherKb;
            this.heapUsedKb = heapUsedKb;
            this.heapFreeKb = heapFreeKb;
            this.heapMaxKb = heapMaxKb;
            this.totalRamKb = totalRamKb;
            this.availRamKb = availRamKb;
            this.cachedKb = cachedKb;
        }
    }

    private static final Runnable tick = new Runnable() {
        @Override
        public void run() {
            if (!running) return;
            readSnapshot();
            handler.postDelayed(this, POLL_MS);
        }
    };

    private MemoryMonitor() {}

    public static synchronized void start(Context context) {
        if (running) return;
        appContext = context != null ? context.getApplicationContext() : appContext;
        running = true;
        history.clear();
        resetValues();
        readSnapshot();
        handler.postDelayed(tick, POLL_MS);
    }

    public static synchronized void stop() {
        running = false;
        handler.removeCallbacks(tick);
        history.clear();
        resetValues();
    }

    public static synchronized boolean isRunning() {
        return running;
    }

    public static synchronized Snapshot getLastSnapshot() {
        return lastSnapshot;
    }

    public static synchronized Snapshot[] getHistory() {
        return history.toArray(new Snapshot[0]);
    }

    private static void resetValues() {
        lastSnapshot = emptySnapshot();
    }

    private static Snapshot emptySnapshot() {
        return new Snapshot(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, -1);
    }

    private static void readSnapshot() {
        try {
            Debug.MemoryInfo info = new Debug.MemoryInfo();
            Debug.getMemoryInfo(info);
            long[] system = readSystemMemory();
            lastSnapshot = new Snapshot(System.currentTimeMillis(),
                    info.dalvikPss, info.nativePss, memStatKb(info, "summary.graphics"),
                    info.otherPss, info.getTotalPss(),
                    info.getTotalPrivateDirty(), info.getTotalPrivateClean(),
                    info.getTotalSharedDirty(), memStatKb(info, "summary.total-swap"),
                    memStatKb(info, "summary.code"), memStatKb(info, "summary.stack"),
                    memStatKb(info, "summary.system"), memStatKb(info, "summary.private-other"),
                    memStatKb(info, "summary.system-other"),
                    heapUsedKb(), heapFreeKb(), heapMaxKb(),
                    system[0], system[1], system[2]);
            history.addLast(lastSnapshot);
            while (history.size() > HISTORY_SIZE) {
                history.removeFirst();
            }
        } catch (Exception ignored) {}
    }

    private static long[] readSystemMemory() {
        long total = 0;
        long avail = 0;
        long cached = -1;
        try {
            if (appContext != null) {
                ActivityManager am = (ActivityManager) appContext.getSystemService(Context.ACTIVITY_SERVICE);
                if (am != null) {
                    ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
                    am.getMemoryInfo(mi);
                    total = mi.totalMem / 1024;
                    avail = mi.availMem / 1024;
                }
            }
        } catch (Exception ignored) {}
        cached = readCachedKb();
        return new long[]{total, avail, cached};
    }

    private static long readCachedKb() {
        try (BufferedReader br = new BufferedReader(new FileReader("/proc/meminfo"))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("Cached:")) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length >= 2) {
                        return Long.parseLong(parts[1]);
                    }
                }
            }
        } catch (Exception ignored) {}
        return -1;
    }

    private static long heapUsedKb() {
        Runtime rt = Runtime.getRuntime();
        return (rt.totalMemory() - rt.freeMemory()) / 1024;
    }

    private static long heapFreeKb() {
        return Runtime.getRuntime().freeMemory() / 1024;
    }

    private static long heapMaxKb() {
        return Runtime.getRuntime().maxMemory() / 1024;
    }

    private static int memStatKb(Debug.MemoryInfo info, String stat) {
        String s = info.getMemoryStat(stat);
        if (s == null) return 0;
        try {
            int end = s.indexOf(' ');
            if (end > 0) {
                return Integer.parseInt(s.substring(0, end));
            }
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
