package exp.ftxt.core;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

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
     * Bangun Notification untuk foreground service.
     */
    public static Notification buildNotification(Context context) {
        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("FTxT")
                .setContentText("Overlay sedang aktif")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setOngoing(true)
                .build();
    }
}
