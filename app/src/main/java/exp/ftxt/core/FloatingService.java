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
import exp.ftxt.features.battery_current.BatteryCurrentConfig;
import exp.ftxt.features.battery_current.BatteryCurrentModule;
import exp.ftxt.features.battery_percentage.BatteryPercentageConfig;
import exp.ftxt.features.battery_percentage.BatteryPercentageModule;
import exp.ftxt.features.battery_temperature.BatteryConfig;
import exp.ftxt.features.battery_temperature.BatteryModule;
import exp.ftxt.features.clock_module.ClockConfig;
import exp.ftxt.features.clock_module.ClockModule;
import exp.ftxt.features.fps_display.FpsConfig;
import exp.ftxt.features.fps_display.FpsModule;
import exp.ftxt.features.network_stats.NetworkConfig;
import exp.ftxt.features.network_stats.NetworkModule;
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
    private BatteryModule batteryModule;
    private BatteryPercentageModule batteryPercentageModule;
    private BatteryCurrentModule batteryCurrentModule;
    private NetworkModule networkModule;
    private final List<OverlayModule> allModules = new ArrayList<>();
    private BroadcastReceiver configChangeReceiver;

    public TextModule getTextModule() { return textModule; }
    public FpsModule getFpsModule() { return fpsModule; }
    public ClockModule getClockModule() { return clockModule; }
    public BatteryModule getBatteryModule() { return batteryModule; }
    public BatteryPercentageModule getBatteryPercentageModule() { return batteryPercentageModule; }
    public BatteryCurrentModule getBatteryCurrentModule() { return batteryCurrentModule; }
    public NetworkModule getNetworkModule() { return networkModule; }

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
    public static BatteryModule batteryModule() {
        if (instance != null) instance.ensureBatteryModule();
        return instance != null ? instance.batteryModule : null;
    }
    public static BatteryPercentageModule batteryPercentageModule() {
        if (instance != null) instance.ensureBatteryPercentageModule();
        return instance != null ? instance.batteryPercentageModule : null;
    }
    public static BatteryCurrentModule batteryCurrentModule() {
        if (instance != null) instance.ensureBatteryCurrentModule();
        return instance != null ? instance.batteryCurrentModule : null;
    }
    public static NetworkModule networkModule() {
        if (instance != null) instance.ensureNetworkModule();
        return instance != null ? instance.networkModule : null;
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

    private void ensureBatteryModule() {
        if (batteryModule == null) {
            batteryModule = new BatteryModule();
            allModules.add(batteryModule);
            batteryModule.init(windowManager, this, prefs);
        }
    }

    private void ensureBatteryPercentageModule() {
        if (batteryPercentageModule == null) {
            batteryPercentageModule = new BatteryPercentageModule();
            allModules.add(batteryPercentageModule);
            batteryPercentageModule.init(windowManager, this, prefs);
        }
    }

    private void ensureBatteryCurrentModule() {
        if (batteryCurrentModule == null) {
            batteryCurrentModule = new BatteryCurrentModule();
            allModules.add(batteryCurrentModule);
            batteryCurrentModule.init(windowManager, this, prefs);
        }
    }

    private void ensureNetworkModule() {
        if (networkModule == null) {
            networkModule = new NetworkModule();
            allModules.add(networkModule);
            networkModule.init(windowManager, this, prefs);
        }
    }

    private boolean isAnyModuleActive() {
        for (OverlayModule module : allModules) {
            if (module.isRunning()) return true;
        }
        return false;
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
            if (BatteryConfig.enabled) { ensureBatteryModule(); batteryModule.start(windowManager, this); }
            if (BatteryPercentageConfig.enabled) { ensureBatteryPercentageModule(); batteryPercentageModule.start(windowManager, this); }
            if (BatteryCurrentConfig.enabled) { ensureBatteryCurrentModule(); batteryCurrentModule.start(windowManager, this); }
            if (NetworkConfig.enabled) { ensureNetworkModule(); networkModule.start(windowManager, this); }
            if (FpsConfig.enabled) { ensureFpsModule(); fpsModule.start(windowManager, this); }

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

    private void reloadAllPositions() {
        for (OverlayModule module : allModules) {
            if (module.isRunning()) {
                module.setOrientationSuffix(null);
                module.updatePosition();
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
