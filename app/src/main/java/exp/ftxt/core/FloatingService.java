package exp.ftxt.core;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import android.view.WindowManager;

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

            textModule.init(windowManager, this, prefs);
            fpsModule.init(windowManager, this, prefs);

            // Start Clock jika diaktifkan
            if (ClockConfig.enabled) {
                clockModule.start(windowManager, this);
            }

            // Start Battery jika diaktifkan
            if (BatteryConfig.enabled) {
                batteryModule.start(windowManager, this);
            }

            // Start Battery Percentage jika diaktifkan
            if (BatteryPercentageConfig.enabled) {
                batteryPercentageModule.start(windowManager, this);
            }

            // Start Battery Current jika diaktifkan
            if (BatteryCurrentConfig.enabled) {
                batteryCurrentModule.start(windowManager, this);
            }

            // Start Network jika diaktifkan
            if (NetworkConfig.enabled) {
                networkModule.start(windowManager, this);
            }

            // Buat text overlay jika sebelumnya aktif
            if (getSharedPreferences("ftxt_prefs", MODE_PRIVATE)
                    .getBoolean("text_overlay_on", false)) {
                textModule.createOverlay();
            }

            // Wake lock agar CPU tidak tidur
            // Lihat: WakeLockManager → core/WakeLockManager.java
            wakeLockManager = new WakeLockManager();
            wakeLockManager.acquire(this);

            // Start FPS jika diaktifkan
            if (FpsConfig.enabled) {
                fpsModule.start(windowManager, this);
            }
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

    // --- Text Module Visibility ---

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

    // --- Text Module Delegates ---

    public static void updateTextStatic() {
        if (instance != null && instance.textModule != null) {
            instance.textModule.updateText(TextConfig.text);
        }
    }

    public static void updateTextSizeStatic() {
        if (instance != null && instance.textModule != null) {
            instance.textModule.updateSize(TextConfig.size);
        }
    }

    public static void updateTextColorStatic() {
        if (instance != null && instance.textModule != null) {
            instance.textModule.updateColor(TextConfig.color);
        }
    }

    public static void updateTouchFlagsStatic() {
        if (instance != null && instance.textModule != null) {
            instance.textModule.updateTouchFlags();
        }
    }

    public static void updateTextPositionStatic() {
        if (instance != null && instance.textModule != null) {
            instance.textModule.updatePosition();
        }
    }

    public static void setTextOrientationSuffixStatic(String suffix) {
        if (instance != null && instance.textModule != null) {
            instance.textModule.setOrientationSuffix(suffix);
        }
    }

    public static int[] getTextCurrentPosition() {
        if (instance != null && instance.textModule != null) {
            return instance.textModule.getCurrentPosition();
        }
        return null;
    }

    public static void updateShadowStatic() {
        if (instance != null && instance.textModule != null) {
            instance.textModule.updateShadow();
        }
    }

    public static void updateTextBackgroundStatic() {
        if (instance != null && instance.textModule != null) {
            instance.textModule.updateBackground();
        }
    }

    public static void updateTextPatternStatic() {
        if (instance != null && instance.textModule != null) {
            instance.textModule.updatePattern();
        }
    }

    // --- FPS Module Delegates ---

    public static void startFpsStatic() {
        if (instance != null && instance.fpsModule != null) {
            instance.fpsModule.start(instance.windowManager, instance);
        }
    }

    public static void stopFpsStatic() {
        if (instance != null && instance.fpsModule != null) {
            instance.fpsModule.stop();
        }
    }

    public static void updateFpsColorStatic() {
        if (instance != null && instance.fpsModule != null) {
            instance.fpsModule.updateColor(FpsConfig.color);
        }
    }

    public static void updateFpsLabelColorStatic() {
        if (instance != null && instance.fpsModule != null) {
            instance.fpsModule.updateLabelColor(FpsConfig.labelColor);
        }
    }

    public static void updateFpsSizeStatic() {
        if (instance != null && instance.fpsModule != null) {
            instance.fpsModule.updateSize(FpsConfig.size);
        }
    }

    public static void updateFpsShadowStatic() {
        if (instance != null && instance.fpsModule != null) {
            instance.fpsModule.updateShadow();
        }
    }

    public static void updateFpsBackgroundStatic() {
        if (instance != null && instance.fpsModule != null) {
            instance.fpsModule.updateBackground();
        }
    }

    public static void updateFpsTouchFlagsStatic() {
        if (instance != null && instance.fpsModule != null) {
            instance.fpsModule.updateTouchFlags();
        }
    }

    public static void updateFpsDisplayStatic() {
        if (instance != null && instance.fpsModule != null) {
            instance.fpsModule.updateDisplay();
        }
    }

    public static void updateFpsPositionStatic() {
        if (instance != null && instance.fpsModule != null) {
            instance.fpsModule.updatePosition();
        }
    }

    public static void setFpsOrientationSuffixStatic(String suffix) {
        if (instance != null && instance.fpsModule != null) {
            instance.fpsModule.setOrientationSuffix(suffix);
        }
    }

    public static int[] getFpsCurrentPosition() {
        if (instance != null && instance.fpsModule != null) {
            return instance.fpsModule.getCurrentPosition();
        }
        return null;
    }

    public static void updateFpsUpdateIntervalStatic() {
        if (instance != null && instance.fpsModule != null) {
            instance.fpsModule.stop();
            instance.fpsModule.start(instance.windowManager, instance);
        }
    }

    // --- Clock Module Delegates ---

    public static void startClockStatic() {
        if (instance != null && instance.clockModule != null) {
            instance.clockModule.start(instance.windowManager, instance);
        }
    }

    public static void stopClockStatic() {
        if (instance != null && instance.clockModule != null) {
            instance.clockModule.stop();
        }
    }

    public static void updateClockColorStatic() {
        if (instance != null && instance.clockModule != null) {
            instance.clockModule.updateColor(ClockConfig.color);
        }
    }

    public static void updateClockSizeStatic() {
        if (instance != null && instance.clockModule != null) {
            instance.clockModule.updateSize(ClockConfig.size);
        }
    }

    public static void updateClockShadowStatic() {
        if (instance != null && instance.clockModule != null) {
            instance.clockModule.updateShadow();
        }
    }

    public static void updateClockBackgroundStatic() {
        if (instance != null && instance.clockModule != null) {
            instance.clockModule.updateBackground();
        }
    }

    public static void updateClockTouchFlagsStatic() {
        if (instance != null && instance.clockModule != null) {
            instance.clockModule.updateTouchFlags();
        }
    }

    public static void updateClockPositionStatic() {
        if (instance != null && instance.clockModule != null) {
            instance.clockModule.updatePosition();
        }
    }

    public static void setClockOrientationSuffixStatic(String suffix) {
        if (instance != null && instance.clockModule != null) {
            instance.clockModule.setOrientationSuffix(suffix);
        }
    }

    public static int[] getClockCurrentPosition() {
        if (instance != null && instance.clockModule != null) {
            return instance.clockModule.getCurrentPosition();
        }
        return null;
    }

    // --- Network Module Delegates ---

    public static void startNetworkStatic() {
        if (instance != null && instance.networkModule != null) {
            instance.networkModule.start(instance.windowManager, instance);
        }
    }

    public static void stopNetworkStatic() {
        if (instance != null && instance.networkModule != null) {
            instance.networkModule.stop();
        }
    }

    public static void updateNetworkSizeStatic() {
        if (instance != null && instance.networkModule != null) {
            instance.networkModule.updateSize(NetworkConfig.size);
        }
    }

    public static void updateNetworkColorStatic() {
        if (instance != null && instance.networkModule != null) {
            instance.networkModule.updateColor(NetworkConfig.color);
        }
    }

    public static void updateNetworkLabelColorStatic() {
        if (instance != null && instance.networkModule != null) {
            instance.networkModule.updateLabelColor(NetworkConfig.labelColor);
        }
    }

    public static void updateNetworkShadowStatic() {
        if (instance != null && instance.networkModule != null) {
            instance.networkModule.updateShadow();
        }
    }

    public static void updateNetworkBackgroundStatic() {
        if (instance != null && instance.networkModule != null) {
            instance.networkModule.updateBackground();
        }
    }

    public static void updateNetworkTouchFlagsStatic() {
        if (instance != null && instance.networkModule != null) {
            instance.networkModule.updateTouchFlags();
        }
    }

    public static void updateNetworkPositionStatic() {
        if (instance != null && instance.networkModule != null) {
            instance.networkModule.updatePosition();
        }
    }

    public static void setNetworkOrientationSuffixStatic(String suffix) {
        if (instance != null && instance.networkModule != null) {
            instance.networkModule.setOrientationSuffix(suffix);
        }
    }

    public static int[] getNetworkCurrentPosition() {
        if (instance != null && instance.networkModule != null) {
            return instance.networkModule.getCurrentPosition();
        }
        return null;
    }

    public static void updateNetworkUpdateIntervalStatic() {
        if (instance != null && instance.networkModule != null) {
            instance.networkModule.stop();
            instance.networkModule.start(instance.windowManager, instance);
        }
    }

    // --- Battery Percentage Module Delegates ---

    public static void startBatteryPercentageStatic() {
        if (instance != null && instance.batteryPercentageModule != null) {
            instance.batteryPercentageModule.start(instance.windowManager, instance);
        }
    }

    public static void stopBatteryPercentageStatic() {
        if (instance != null && instance.batteryPercentageModule != null) {
            instance.batteryPercentageModule.stop();
        }
    }

    public static void updateBatteryPercentageColorStatic() {
        if (instance != null && instance.batteryPercentageModule != null) {
            instance.batteryPercentageModule.updateColor(BatteryPercentageConfig.color);
        }
    }

    public static void updateBatteryPercentageLabelColorStatic() {
        if (instance != null && instance.batteryPercentageModule != null) {
            instance.batteryPercentageModule.updateLabelColor(BatteryPercentageConfig.labelColor);
        }
    }

    public static void updateBatteryPercentageSizeStatic() {
        if (instance != null && instance.batteryPercentageModule != null) {
            instance.batteryPercentageModule.updateSize(BatteryPercentageConfig.size);
        }
    }

    public static void updateBatteryPercentageShadowStatic() {
        if (instance != null && instance.batteryPercentageModule != null) {
            instance.batteryPercentageModule.updateShadow();
        }
    }

    public static void updateBatteryPercentageBackgroundStatic() {
        if (instance != null && instance.batteryPercentageModule != null) {
            instance.batteryPercentageModule.updateBackground();
        }
    }

    public static void updateBatteryPercentageTouchFlagsStatic() {
        if (instance != null && instance.batteryPercentageModule != null) {
            instance.batteryPercentageModule.updateTouchFlags();
        }
    }

    public static void updateBatteryPercentagePositionStatic() {
        if (instance != null && instance.batteryPercentageModule != null) {
            instance.batteryPercentageModule.updatePosition();
        }
    }

    public static void setBatteryPercentageOrientationSuffixStatic(String suffix) {
        if (instance != null && instance.batteryPercentageModule != null) {
            instance.batteryPercentageModule.setOrientationSuffix(suffix);
        }
    }

    public static int[] getBatteryPercentageCurrentPosition() {
        if (instance != null && instance.batteryPercentageModule != null) {
            return instance.batteryPercentageModule.getCurrentPosition();
        }
        return null;
    }

    // --- Battery Current Module Delegates ---

    public static void startBatteryCurrentStatic() {
        if (instance != null && instance.batteryCurrentModule != null) {
            instance.batteryCurrentModule.start(instance.windowManager, instance);
        }
    }

    public static void stopBatteryCurrentStatic() {
        if (instance != null && instance.batteryCurrentModule != null) {
            instance.batteryCurrentModule.stop();
        }
    }

    public static void updateBatteryCurrentColorStatic() {
        if (instance != null && instance.batteryCurrentModule != null) {
            instance.batteryCurrentModule.updateColor(BatteryCurrentConfig.color);
        }
    }

    public static void updateBatteryCurrentLabelColorStatic() {
        if (instance != null && instance.batteryCurrentModule != null) {
            instance.batteryCurrentModule.updateLabelColor(BatteryCurrentConfig.labelColor);
        }
    }

    public static void updateBatteryCurrentSizeStatic() {
        if (instance != null && instance.batteryCurrentModule != null) {
            instance.batteryCurrentModule.updateSize(BatteryCurrentConfig.size);
        }
    }

    public static void updateBatteryCurrentShadowStatic() {
        if (instance != null && instance.batteryCurrentModule != null) {
            instance.batteryCurrentModule.updateShadow();
        }
    }

    public static void updateBatteryCurrentBackgroundStatic() {
        if (instance != null && instance.batteryCurrentModule != null) {
            instance.batteryCurrentModule.updateBackground();
        }
    }

    public static void updateBatteryCurrentTouchFlagsStatic() {
        if (instance != null && instance.batteryCurrentModule != null) {
            instance.batteryCurrentModule.updateTouchFlags();
        }
    }

    public static void updateBatteryCurrentPositionStatic() {
        if (instance != null && instance.batteryCurrentModule != null) {
            instance.batteryCurrentModule.updatePosition();
        }
    }

    public static void setBatteryCurrentOrientationSuffixStatic(String suffix) {
        if (instance != null && instance.batteryCurrentModule != null) {
            instance.batteryCurrentModule.setOrientationSuffix(suffix);
        }
    }

    public static int[] getBatteryCurrentCurrentPosition() {
        if (instance != null && instance.batteryCurrentModule != null) {
            return instance.batteryCurrentModule.getCurrentPosition();
        }
        return null;
    }

    public static void updateBatteryCurrentUpdateIntervalStatic() {
        if (instance != null && instance.batteryCurrentModule != null) {
            instance.batteryCurrentModule.stop();
            instance.batteryCurrentModule.start(instance.windowManager, instance);
        }
    }

    // --- Battery Module Delegates ---

    public static void startBatteryStatic() {
        if (instance != null && instance.batteryModule != null) {
            instance.batteryModule.start(instance.windowManager, instance);
        }
    }

    public static void stopBatteryStatic() {
        if (instance != null && instance.batteryModule != null) {
            instance.batteryModule.stop();
        }
    }

    public static void updateBatteryColorStatic() {
        if (instance != null && instance.batteryModule != null) {
            instance.batteryModule.updateColor(BatteryConfig.color);
        }
    }

    public static void updateBatteryLabelColorStatic() {
        if (instance != null && instance.batteryModule != null) {
            instance.batteryModule.updateLabelColor(BatteryConfig.labelColor);
        }
    }

    public static void updateBatterySizeStatic() {
        if (instance != null && instance.batteryModule != null) {
            instance.batteryModule.updateSize(BatteryConfig.size);
        }
    }

    public static void updateBatteryShadowStatic() {
        if (instance != null && instance.batteryModule != null) {
            instance.batteryModule.updateShadow();
        }
    }

    public static void updateBatteryBackgroundStatic() {
        if (instance != null && instance.batteryModule != null) {
            instance.batteryModule.updateBackground();
        }
    }

    public static void updateBatteryTouchFlagsStatic() {
        if (instance != null && instance.batteryModule != null) {
            instance.batteryModule.updateTouchFlags();
        }
    }

    public static void updateBatteryPositionStatic() {
        if (instance != null && instance.batteryModule != null) {
            instance.batteryModule.updatePosition();
        }
    }

    public static void setBatteryOrientationSuffixStatic(String suffix) {
        if (instance != null && instance.batteryModule != null) {
            instance.batteryModule.setOrientationSuffix(suffix);
        }
    }

    public static void updateBatteryUpdateIntervalStatic() {
        if (instance != null && instance.batteryModule != null) {
            instance.batteryModule.stop();
            instance.batteryModule.start(instance.windowManager, instance);
        }
    }

    public static int[] getBatteryCurrentPosition() {
        if (instance != null && instance.batteryModule != null) {
            return instance.batteryModule.getCurrentPosition();
        }
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        clockModule.stop();
        batteryModule.stop();
        batteryPercentageModule.stop();
        batteryCurrentModule.stop();
        networkModule.stop();
        fpsModule.stop();
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
