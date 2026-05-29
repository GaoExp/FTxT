package exp.ftxt.core;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import android.view.WindowManager;

import exp.ftxt.R;
import exp.ftxt.features.fps.FpsConfig;
import exp.ftxt.features.fps.FpsModule;
import exp.ftxt.features.text.TextConfig;
import exp.ftxt.features.text.TextModule;

/**
 * Foreground service untuk mengelola overlay Floating Text dan FPS Display.
 *
 * Menggunakan:
 * - NotificationHelper → core/NotificationHelper.java (channel + notification)
 * - WakeLockManager   → core/WakeLockManager.java (partial wake lock)
 * - TextModule         → features/text/TextModule.java
 * - FpsModule          → features/fps/FpsModule.java
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

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
        prefs = getSharedPreferences("ftxt_prefs", MODE_PRIVATE);

        textModule = new TextModule();
        fpsModule = new FpsModule();

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

    @Override
    public void onDestroy() {
        super.onDestroy();

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
