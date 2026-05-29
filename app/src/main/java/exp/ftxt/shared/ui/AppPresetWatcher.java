package exp.ftxt.shared.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AppPresetWatcher {

    public interface OnAppChangeListener {
        void onAppChanged(String oldPackage, String newPackage, float savedX, float savedY);
    }

    private final Context context;
    private final SharedPreferences prefs;
    private final OnAppChangeListener listener;
    private final String prefPrefix;

    private Handler handler;
    private String currentPackage;
    private boolean running;
    private String orientationSuffix;

    public AppPresetWatcher(Context context, String prefPrefix, OnAppChangeListener listener) {
        this.context = context;
        this.prefs = context.getSharedPreferences("ftxt_prefs", Context.MODE_PRIVATE);
        this.listener = listener;
        this.prefPrefix = prefPrefix;
        this.orientationSuffix = "_port";
    }

    public void setOrientationSuffix(String suffix) {
        this.orientationSuffix = suffix;
    }

    public void start() {
        if (running) return;
        running = true;
        handler = new Handler();
        currentPackage = getForegroundPackage();
        handler.post(pollRunnable);
    }

    public void stop() {
        running = false;
        if (handler != null) handler.removeCallbacks(pollRunnable);
    }

    public void cleanup() {
        stop();
        handler = null;
    }

    public boolean isRunning() {
        return running;
    }

    public String getCurrentPackage() {
        return currentPackage;
    }

    public void saveCurrentForApp(String pkg, float x, float y) {
        if (pkg == null) return;
        prefs.edit()
                .putFloat(prefPrefix + "auto_" + pkg + orientationSuffix + "_x", x)
                .putFloat(prefPrefix + "auto_" + pkg + orientationSuffix + "_y", y)
                .apply();
    }

    private final Runnable pollRunnable = new Runnable() {
        @Override
        public void run() {
            if (!running) return;
            String pkg = getForegroundPackage();
            if (pkg != null && !pkg.equals(currentPackage)) {
                String oldPkg = currentPackage;
                currentPackage = pkg;
                float sx = prefs.getFloat(prefPrefix + "auto_" + pkg + orientationSuffix + "_x", -1f);
                float sy = prefs.getFloat(prefPrefix + "auto_" + pkg + orientationSuffix + "_y", -1f);
                if (listener != null) listener.onAppChanged(oldPkg, pkg, sx, sy);
            }
            handler.postDelayed(this, 2000);
        }
    };

    private String getForegroundPackage() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return null;
        UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        if (usm == null) return null;
        long now = System.currentTimeMillis();
        List<UsageStats> stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, now - 5000, now);
        if (stats == null || stats.isEmpty()) return null;
        Collections.sort(stats, (a, b) -> Long.compare(b.getLastTimeUsed(), a.getLastTimeUsed()));
        return stats.get(0).getPackageName();
    }
}
