package exp.ftxt.core;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import exp.ftxt.R;
import exp.ftxt.features.battery_stats.BatteryMonitor;

/**
 * Foreground service ringan agar pemantauan baterai full-aktif tetap merekam
 * walau aplikasi ditutup / service overlay mati. Notifikasi minimal
 * (prioritas rendah) dan tanpa kontrol apa pun — monitor memang tidak punya
 * kondisi berhenti.
 */
public class BatteryMonitorService extends Service {

    public static final String CHANNEL_ID = "ftxt_battery_monitor";
    private static final int NOTIFICATION_ID = 2;

    @Override
    public void onCreate() {
        super.onCreate();
        startForegroundCompat();
        BatteryMonitor.start(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!BatteryMonitor.isRunning()) {
            BatteryMonitor.start(this);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        BatteryMonitor.stop();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startForegroundCompat() {
        NotificationManager nm =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null && nm.getNotificationChannel(CHANNEL_ID) == null) {
            NotificationChannel ch = new NotificationChannel(CHANNEL_ID,
                    "Pemantauan Baterai", NotificationManager.IMPORTANCE_MIN);
            ch.setDescription("Menjaga pencatatan riwayat baterai tetap berjalan");
            ch.setShowBadge(false);
            nm.createNotificationChannel(ch);
        }
        Notification notif = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_toggle)
                .setContentTitle("Pemantauan baterai aktif")
                .setContentText("Riwayat baterai terus direkam di latar belakang")
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setOngoing(true)
                .setShowWhen(false)
                .build();
        try {
            startForeground(NOTIFICATION_ID, notif);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
