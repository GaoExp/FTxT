package exp.ftxt.core;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;

import androidx.core.app.NotificationCompat;

import android.view.WindowManager;

import exp.ftxt.R;
import exp.ftxt.modules.fps.FpsConfig;
import exp.ftxt.modules.fps.FpsModule;
import exp.ftxt.modules.text.TextConfig;
import exp.ftxt.modules.text.TextModule;

public class FloatingService extends Service {

    private WindowManager windowManager;
    private PowerManager.WakeLock wakeLock;
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

        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, "ftxt_overlay")
                .setContentTitle("FTxT")
                .setContentText("Overlay sedang aktif")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setOngoing(true)
                .build();

        try {
            startForeground(1, notification);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

            // Init modules
            textModule.init(windowManager, this, prefs);
            fpsModule.init(prefs);

            // Create text overlay only if text module is active
            if (getSharedPreferences("ftxt_prefs", MODE_PRIVATE)
                    .getBoolean("text_overlay_on", false)) {
                textModule.createOverlay();
            }

            // Acquire wake lock
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FTxT:OverlayWakeLock");
            wakeLock.acquire();

            // Start FPS if enabled
            if (FpsConfig.enabled) {
                fpsModule.start(windowManager, this);
            }
        } catch (Exception e) {
            e.printStackTrace();
            stopSelf();
        }
    }

    // === Text Module Visibility ===

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

    // === Text Module Delegates ===

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

    public static void updateShadowStatic() {
        if (instance != null && instance.textModule != null) {
            boolean shadow = instance.prefs.getBoolean("shadow_enabled", false);
            instance.textModule.updateShadow(shadow);
        }
    }

    // === FPS Module Delegates ===

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
            instance.fpsModule.updateShadow(FpsConfig.shadow);
        }
    }

    public static void updateFpsTouchFlagsStatic() {
        if (instance != null && instance.fpsModule != null) {
            instance.fpsModule.updateTouchFlags();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "ftxt_overlay", "FTxT Overlay", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Notifikasi overlay FTxT");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        fpsModule.stop();
        textModule.savePosition(prefs);

        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }

        textModule.destroyOverlay();

        instance = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
