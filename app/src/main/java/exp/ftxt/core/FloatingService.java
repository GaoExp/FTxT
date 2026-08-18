package exp.ftxt.core;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;

import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

import exp.ftxt.R;
import exp.ftxt.features.battery_bar.BatteryBarConfig;
import exp.ftxt.features.battery_bar.BatteryBarModule;
import exp.ftxt.features.battery_stats.BatteryStatsConfig;
import exp.ftxt.features.battery_stats.BatteryStatsModule;
import exp.ftxt.features.clock_module.ClockConfig;
import exp.ftxt.features.clock_module.ClockModule;
import exp.ftxt.features.fps_display.FpsConfig;
import exp.ftxt.features.fps_display.FpsModule;
import exp.ftxt.features.network_stats.NetworkConfig;
import exp.ftxt.features.network_stats.NetworkModule;
import exp.ftxt.features.memory_stats.MemoryConfig;
import exp.ftxt.features.memory_stats.MemoryModule;
import exp.ftxt.features.memory_stats.MemoryMonitor;
import exp.ftxt.features.floating_text.TextConfig;
import exp.ftxt.features.floating_text.TextModule;
import exp.ftxt.shared.ui.OverlayModule;

public class FloatingService extends Service {

    private WindowManager windowManager;
    private WakeLockManager wakeLockManager;
    private SharedPreferences prefs;

    public static FloatingService instance;

    private TextModule textModule;
    private FpsModule fpsModule;
    private ClockModule clockModule;
    private BatteryStatsModule batteryStatsModule;
    private NetworkModule networkModule;
    private BatteryBarModule batteryBarModule;
    private MemoryModule memoryModule;
    private final List<OverlayModule> allModules = new ArrayList<>();
    private BroadcastReceiver configChangeReceiver;

    public TextModule getTextModule() { return textModule; }
    public FpsModule getFpsModule() { return fpsModule; }
    public ClockModule getClockModule() { return clockModule; }
    public BatteryStatsModule getBatteryStatsModule() { return batteryStatsModule; }
    public NetworkModule getNetworkModule() { return networkModule; }
    public BatteryBarModule getBatteryBarModule() { return batteryBarModule; }

    public static TextModule textModule() {
        if (instance != null) instance.ensureTextModule();
        return instance != null ? instance.textModule : null;
    }
    public static FpsModule fpsModule() {
        if (instance != null) instance.ensureFpsModule();
        return instance != null ? instance.fpsModule : null;
    }
    public static ClockModule clockModule() {
        if (instance != null) instance.ensureClockModule();
        return instance != null ? instance.clockModule : null;
    }
    public static BatteryStatsModule batteryStatsModule() {
        if (instance != null) instance.ensureBatteryStatsModule();
        return instance != null ? instance.batteryStatsModule : null;
    }
    public static NetworkModule networkModule() {
        if (instance != null) instance.ensureNetworkModule();
        return instance != null ? instance.networkModule : null;
    }
    public static BatteryBarModule batteryBarModule() {
        if (instance != null) instance.ensureBatteryBarModule();
        return instance != null ? instance.batteryBarModule : null;
    }
    public static MemoryModule memoryModule() {
        if (instance != null) instance.ensureMemoryModule();
        return instance != null ? instance.memoryModule : null;
    }

    private void ensureTextModule() {
        if (textModule == null) {
            textModule = new TextModule();
            allModules.add(textModule);
            textModule.init(windowManager, this, prefs);
        }
    }

    private void ensureFpsModule() {
        if (fpsModule == null) {
            fpsModule = new FpsModule();
            allModules.add(fpsModule);
            fpsModule.init(windowManager, this, prefs);
        }
    }

    private void ensureClockModule() {
        if (clockModule == null) {
            clockModule = new ClockModule();
            allModules.add(clockModule);
            clockModule.init(windowManager, this, prefs);
        }
    }

    private void ensureBatteryStatsModule() {
        if (batteryStatsModule == null) {
            batteryStatsModule = new BatteryStatsModule();
            allModules.add(batteryStatsModule);
            batteryStatsModule.init(windowManager, this, prefs);
        }
    }

    private void ensureNetworkModule() {
        if (networkModule == null) {
            networkModule = new NetworkModule();
            allModules.add(networkModule);
            networkModule.init(windowManager, this, prefs);
        }
    }

    private void ensureBatteryBarModule() {
        if (batteryBarModule == null) {
            batteryBarModule = new BatteryBarModule();
            allModules.add(batteryBarModule);
            batteryBarModule.init(windowManager, this, prefs);
        }
    }

    private void ensureMemoryModule() {
        if (memoryModule == null) {
            memoryModule = new MemoryModule();
            allModules.add(memoryModule);
            memoryModule.init(windowManager, this, prefs);
        }
    }

    private boolean isAnyModuleActive() {
        for (OverlayModule module : allModules) {
            if (module.isRunning()) return true;
        }
        return MemoryConfig.backgroundMonitor;
    }

    private void registerConfigReceiver() {
        if (configChangeReceiver != null) return;
        configChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null && Intent.ACTION_CONFIGURATION_CHANGED.equals(intent.getAction())) {
                    reloadAllPositions();
                }
            }
        };
        IntentFilter filter = new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED);
        registerReceiver(configChangeReceiver, filter);
    }

    private void unregisterConfigReceiver() {
        if (configChangeReceiver == null) return;
        unregisterReceiver(configChangeReceiver);
        configChangeReceiver = null;
    }

    private void acquireWakeLockIfNeeded() {
        if (isAnyModuleActive() && (wakeLockManager == null || !wakeLockManager.isHeld())) {
            if (wakeLockManager == null) {
                wakeLockManager = new WakeLockManager();
            }
            wakeLockManager.acquire(this);
        }
    }

    private void releaseWakeLockIfEmpty() {
        if (!isAnyModuleActive() && wakeLockManager != null && wakeLockManager.isHeld()) {
            wakeLockManager.release();
        }
    }

    private void stopSelfIfEmpty() {
        if (!isAnyModuleActive()) {
            stopSelf();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
        prefs = getSharedPreferences("ftxt_prefs", MODE_PRIVATE);

        NotificationHelper.createChannel(this);
        try {
            startForeground(NotificationHelper.NOTIFICATION_ID,
                    NotificationHelper.buildNotification(this));
            NotificationHelper.startIconCycling(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

            if (prefs.getBoolean("text_overlay_on", false)) {
                ensureTextModule();
                textModule.createOverlay();
            }
            if (ClockConfig.enabled) { ensureClockModule(); clockModule.start(windowManager, this); }
            if (BatteryStatsConfig.enabled) { ensureBatteryStatsModule(); batteryStatsModule.start(windowManager, this); }
            if (NetworkConfig.enabled) { ensureNetworkModule(); networkModule.start(windowManager, this); }
            if (FpsConfig.enabled) { ensureFpsModule(); fpsModule.start(windowManager, this); }
            if (BatteryBarConfig.enabled) { ensureBatteryBarModule(); batteryBarModule.start(windowManager, this); }
            if (MemoryConfig.enabled) { ensureMemoryModule(); memoryModule.start(windowManager, this); }
            if (MemoryConfig.backgroundMonitor) {
                MemoryMonitor.start(this);
            }

            acquireWakeLockIfNeeded();
            if (isAnyModuleActive()) registerConfigReceiver();

        } catch (Exception e) {
            e.printStackTrace();
            stopSelf();
        }
    }

    public static void createTextOverlayStatic() {
        if (instance != null && instance.textModule != null) {
            instance.textModule.createOverlay();
        }
    }

    public static void destroyTextOverlayStatic() {
        if (instance != null && instance.textModule != null) {
            instance.textModule.destroyOverlay();
        }
    }

    public static void updateTextStatic() {
        if (instance != null && instance.textModule != null) {
            instance.textModule.updateText(TextConfig.text);
        }
    }

    public static void updateTextPatternStatic() {
        if (instance != null && instance.textModule != null) {
            instance.textModule.updatePattern();
        }
    }

    public static void startModule(OverlayModule module) {
        if (instance != null && module != null) {
            module.start(instance.windowManager, instance);
            instance.acquireWakeLockIfNeeded();
            instance.registerConfigReceiver();
        }
    }

    public static void stopModule(OverlayModule module) {
        if (instance != null && module != null) {
            module.stop();
            instance.releaseWakeLockIfEmpty();
            instance.unregisterConfigReceiver();
            instance.stopSelfIfEmpty();
        }
    }

    public static void updateColorForModule(OverlayModule module, int color) {
        if (instance != null && module != null && module.isRunning()) {
            module.updateColor(color);
        }
    }

    public static void updateSizeForModule(OverlayModule module, float size) {
        if (instance != null && module != null && module.isRunning()) {
            module.updateSize(size);
        }
    }

    public static void updateLabelColorForModule(OverlayModule module, int color) {
        if (instance != null && module != null && module.isRunning()) {
            module.updateLabelColor(color);
        }
    }

    public static void updateSeparatorColorForModule(OverlayModule module, int color) {
        if (instance != null && module != null && module.isRunning()) {
            module.updateSeparatorColor(color);
        }
    }

    public static void updateShadowForModule(OverlayModule module) {
        if (instance != null && module != null && module.isRunning()) {
            module.updateShadow();
        }
    }

    public static void updateBackgroundForModule(OverlayModule module) {
        if (instance != null && module != null && module.isRunning()) {
            module.updateBackground();
        }
    }

    public static void updatePositionForModule(OverlayModule module) {
        if (instance != null && module != null && module.isRunning()) {
            module.updatePosition();
        }
    }

    public static void updateTouchFlagsForModule(OverlayModule module) {
        if (instance != null && module != null && module.isRunning()) {
            module.updateTouchFlags();
        }
    }

    public static void setOrientationSuffixForModule(OverlayModule module, String suffix) {
        if (instance != null && module != null) {
            module.setOrientationSuffix(suffix);
        }
    }

    public static int[] getCurrentPositionForModule(OverlayModule module) {
        if (instance != null && module != null) {
            return module.getCurrentPosition();
        }
        return null;
    }

    public static void restartModule(OverlayModule module) {
        if (instance != null && module != null && module.isRunning()) {
            module.stop();
            module.start(instance.windowManager, instance);
        }
    }

    public static void updateBatteryBarInPlace() {
        if (instance != null && instance.batteryBarModule != null && instance.batteryBarModule.isRunning()) {
            instance.batteryBarModule.applyAppearance();
            instance.batteryBarModule.reloadLayout();
            instance.batteryBarModule.updatePosition();
        }
    }

    public static void updateBatteryStatsInPlace() {
        if (instance != null && instance.batteryStatsModule != null && instance.batteryStatsModule.isRunning()) {
            instance.batteryStatsModule.refreshDisplay();
        }
    }

    public static void setBackgroundMonitorEnabled(boolean enabled) {
        if (enabled) {
            if (instance != null) {
                MemoryMonitor.start(instance);
            }
        } else {
            MemoryMonitor.stop();
            if (instance != null) {
                instance.stopSelfIfEmpty();
            }
        }
    }

    public static void updateMemoryInPlace() {
        if (instance != null && instance.memoryModule != null && instance.memoryModule.isRunning()) {
            instance.memoryModule.refreshDisplay();
        }
    }

    private void reloadAllPositions() {
        for (OverlayModule module : allModules) {
            if (module.isRunning()) {
                module.reloadPosition();
            }
        }
    }

    public static void updateNotification() {
        if (instance != null) {
            NotificationHelper.updateNotification(instance);
        }
    }

    public static void stopAllModules() {
        if (instance == null) return;
        for (OverlayModule module : instance.allModules) {
            module.stop();
        }
        MemoryMonitor.stop();
        instance.releaseWakeLockIfEmpty();
        instance.unregisterConfigReceiver();
        instance.stopSelfIfEmpty();
    }

    public static void hideAllOverlays() {
        if (instance == null) return;
        for (OverlayModule module : instance.allModules) {
            module.hide();
        }
    }

    public static void showAllOverlays() {
        if (instance == null) return;
        for (OverlayModule module : instance.allModules) {
            module.show();
        }
    }

    public static boolean areAllOverlaysHidden() {
        if (instance == null) return true;
        for (OverlayModule module : instance.allModules) {
            if (module.isRunning() && !module.isHidden()) return false;
        }
        return true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        NotificationHelper.stopIconCycling();
        unregisterConfigReceiver();
        MemoryMonitor.stop();

        for (OverlayModule module : allModules) {
            module.stop();
        }
        allModules.clear();

        if (textModule != null) {
            textModule.savePosition(prefs);
            textModule.destroyOverlay();
        }

        if (wakeLockManager != null) {
            wakeLockManager.release();
            wakeLockManager = null;
        }

        instance = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
