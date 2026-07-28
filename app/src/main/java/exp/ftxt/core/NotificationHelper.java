package exp.ftxt.core;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.List;

import exp.ftxt.R;
import exp.ftxt.features.battery_current.BatteryCurrentConfig;
import exp.ftxt.features.battery_percentage.BatteryPercentageConfig;
import exp.ftxt.features.battery_temperature.BatteryConfig;
import exp.ftxt.features.clock_module.ClockConfig;
import exp.ftxt.features.fps_display.FpsConfig;
import exp.ftxt.features.network_stats.NetworkConfig;

/**
 * Helper untuk membuat channel notifikasi dan notification foreground.
 *
 * Dipakai oleh:
 * - FloatingService → core/FloatingService.java (onCreate, createNotificationChannel)
 */
public class NotificationHelper {

    public static final String CHANNEL_ID = "ftxt_overlay";
    public static final int NOTIFICATION_ID = 1;

    /**
     * Buat notification channel untuk Android O+.
     */
    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "FTxT Overlay", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Notifikasi overlay FTxT");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    /**
     * Bangun Notification untuk foreground service dengan custom RemoteViews layout.
     */
    public static Notification buildNotification(Context context) {
        boolean allHidden = FloatingService.areAllOverlaysHidden();
        int toggleIcon = allHidden ? R.drawable.ic_notification_invisible : R.drawable.ic_notification_visible;

        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_custom);

        // Toggle Overlay
        Intent toggleIntent = new Intent(context, NotificationActionReceiver.class);
        toggleIntent.setAction(NotificationActionReceiver.ACTION_TOGGLE_OVERLAY);
        PendingIntent togglePending = PendingIntent.getBroadcast(
                context, 0, toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Kill Service
        Intent killIntent = new Intent(context, NotificationActionReceiver.class);
        killIntent.setAction(NotificationActionReceiver.ACTION_KILL_SERVICE);
        PendingIntent killPending = PendingIntent.getBroadcast(
                context, 1, killIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Open App
        Intent openIntent = new Intent(context, NotificationActionReceiver.class);
        openIntent.setAction(NotificationActionReceiver.ACTION_OPEN_APP);
        PendingIntent openPending = PendingIntent.getBroadcast(
                context, 2, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Set icon & click pada RemoteViews
        contentView.setImageViewResource(R.id.noti_toggle_btn, toggleIcon);
        contentView.setOnClickPendingIntent(R.id.noti_toggle_btn, togglePending);

        contentView.setImageViewResource(R.id.noti_close_btn, R.drawable.ic_close);
        contentView.setOnClickPendingIntent(R.id.noti_close_btn, killPending);

        contentView.setImageViewResource(R.id.noti_open_btn, R.drawable.ic_notification_open);
        contentView.setOnClickPendingIntent(R.id.noti_open_btn, openPending);

        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_toggle)
                .setCustomContentView(contentView)
                .setOngoing(true)
                .build();
    }

    /**
     * Update notifikasi yang sedang berjalan.
     */
    public static void updateNotification(Context context) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(NOTIFICATION_ID, buildNotification(context));
    }

    /**
     * Cek apakah ada module yang aktif.
     */
    private static boolean isAnyModuleActive(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("ftxt_prefs", Context.MODE_PRIVATE);
        
        if (prefs.getBoolean("text_overlay_on", false)) return true;
        if (FpsConfig.enabled) return true;
        if (ClockConfig.enabled) return true;
        if (BatteryConfig.enabled) return true;
        if (BatteryPercentageConfig.enabled) return true;
        if (BatteryCurrentConfig.enabled) return true;
        if (NetworkConfig.enabled) return true;
        
        return false;
    }

    /**
     * Ambil daftar module yang aktif untuk ditampilkan di notifikasi.
     */
    private static String getActiveModulesText(Context context) {
        List<String> active = new ArrayList<>();
        
        SharedPreferences prefs = context.getSharedPreferences("ftxt_prefs", Context.MODE_PRIVATE);
        if (prefs.getBoolean("text_overlay_on", false)) active.add("Text");
        if (FpsConfig.enabled) active.add("FPS");
        if (ClockConfig.enabled) active.add("Clock");
        if (BatteryConfig.enabled) active.add("Battery");
        if (BatteryPercentageConfig.enabled) active.add("Battery%");
        if (BatteryCurrentConfig.enabled) active.add("Current");
        if (NetworkConfig.enabled) active.add("Network");

        if (active.isEmpty()) return "Tidak ada overlay aktif";
        return String.join(", ", active) + " aktif";
    }
}
