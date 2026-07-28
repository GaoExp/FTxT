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

/**
 * Foreground service untuk mengelola overlay Floating Text dan FPS Display.
 *
 * Menggunakan:
 * - NotificationHelper → core/NotificationHelper.java (channel + notification)
 * - WakeLockManager   → core/WakeLockManager.java (partial wake lock)
 * - TextModule         → features/floating_text/TextModule.java
 * - FpsModule          → features/fps_display/FpsModule.java
 *
 * Dipanggil oleh:
 * - MainActivity       → MainActivity.java (start/stop service via TextPanelController & FpsPanelController)
 * - TextPanelController → ui/TextPanelController.java
 * - FpsPanelController  → ui/FpsPanelController.java
 */
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

    public static TextModule textModule() { return instance != null ? instance.textModule : null; }
    public static FpsModule fpsModule() { return instance != null ? instance.fpsModule : null; }
    public static ClockModule clockModule() { return instance != null ? instance.clockModule : null; }
    public static BatteryModule batteryModule() { return instance != null ? instance.batteryModule : null; }
    public static BatteryPercentageModule batteryPercentageModule() { return instance != null ? instance.batteryPercentageModule : null; }
    public static BatteryCurrentModule batteryCurrentModule() { return instance != null ? instance.batteryCurrentModule : null; }
    public static NetworkModule networkModule() { return instance != null ? instance.networkModule : null; }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
        prefs = getSharedPreferences("ftxt_prefs", MODE_PRIVATE);

        textModule = new TextModule();
        fpsModule = new FpsModule();
        clockModule = new ClockModule();
        batteryModule = new BatteryModule();
        batteryPercentageModule = new BatteryPercentageModule();
        batteryCurrentModule = new BatteryCurrentModule();
        networkModule = new NetworkModule();

        allModules.add(textModule);
        allModules.add(fpsModule);
        allModules.add(clockModule);
        allModules.add(batteryModule);
        allModules.add(batteryPercentageModule);
        allModules.add(batteryCurrentModule);
        allModules.add(networkModule);

        // Notification channel + foreground service
        // Lihat: NotificationHelper → core/NotificationHelper.java
        NotificationHelper.createChannel(this);
        try {
            startForeground(NotificationHelper.NOTIFICATION_ID,
                    NotificationHelper.buildNotification(this));
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

            for (OverlayModule module : allModules) {
                module.init(windowManager, this, prefs);
            }

            // Start module yang enabled
            if (ClockConfig.enabled) clockModule.start(windowManager, this);
            if (BatteryConfig.enabled) batteryModule.start(windowManager, this);
            if (BatteryPercentageConfig.enabled) batteryPercentageModule.start(windowManager, this);
            if (BatteryCurrentConfig.enabled) batteryCurrentModule.start(windowManager, this);
            if (NetworkConfig.enabled) networkModule.start(windowManager, this);
            if (FpsConfig.enabled) fpsModule.start(windowManager, this);

            // Buat text overlay jika sebelumnya aktif
            if (prefs.getBoolean("text_overlay_on", false)) {
                textModule.createOverlay();
            }

            // Wake lock agar CPU tidak tidur
            // Lihat: WakeLockManager → core/WakeLockManager.java
            wakeLockManager = new WakeLockManager();
            wakeLockManager.acquire(this);

            // Register receiver untuk deteksi perubahan orientasi sistem
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

        } catch (Exception e) {
            e.printStackTrace();
            stopSelf();
        }
    }

    // ========================================================================
    // Static delegates — dipanggil oleh MainActivity/panel controllers
    // TextModule methods → modules/text/TextModule.java
    // FpsModule methods  → modules/fps/FpsModule.java
    // ========================================================================

    // --- Text Module Specific ---

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

    // --- Generic Module Delegates ---

    public static void startModule(OverlayModule module) {
        if (instance != null && module != null) {
            module.start(instance.windowManager, instance);
        }
    }

    public static void stopModule(OverlayModule module) {
        if (instance != null && module != null) {
            module.stop();
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

    /**
     * Update notifikasi foreground service.
     * Dipanggil oleh NotificationActionReceiver saat user toggle overlay.
     */
    public static void updateNotification() {
        if (instance != null) {
            NotificationHelper.updateNotification(instance);
        }
    }

    /**
     * Stop semua modul overlay.
     * Dipanggil oleh NotificationActionReceiver.
     */
    public static void stopAllModules() {
        if (instance == null) return;
        for (OverlayModule module : instance.allModules) {
            module.stop();
        }
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

        if (configChangeReceiver != null) {
            unregisterReceiver(configChangeReceiver);
            configChangeReceiver = null;
        }

        for (OverlayModule module : allModules) {
            module.stop();
        }

        textModule.savePosition(prefs);

        wakeLockManager.release();
        textModule.destroyOverlay();

        instance = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
