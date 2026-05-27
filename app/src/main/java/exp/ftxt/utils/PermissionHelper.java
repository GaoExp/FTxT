package exp.ftxt.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Permission request helpers untuk MainActivity.
 *
 * Memisahkan semua logic permission overlay, notifikasi, dan baterai
 * dari MainActivity agar lebih terfokus.
 *
 * Dipakai oleh:
 * - MainActivity → MainActivity.java (overlaySwitch, fpsSwitch listener)
 */
public class PermissionHelper {

    public static final int NOTIFICATION_PERMISSION_CODE = 100;

    /**
     * Periksa apakah izin overlay (SYSTEM_ALERT_WINDOW) sudah diberikan.
     */
    public static boolean hasOverlayPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(activity);
        }
        return true;
    }

    /**
     * Minta izin overlay dengan membuka halaman pengaturan.
     */
    public static void requestOverlayPermission(Activity activity) {
        Toast.makeText(activity, "Izinkan overlay di pengaturan", Toast.LENGTH_LONG).show();
        activity.startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + activity.getPackageName())));
    }

    /**
     * Periksa apakah izin notifikasi (POST_NOTIFICATIONS) sudah diberikan (Android 13+).
     */
    public static boolean hasNotificationPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    /**
     * Minta izin notifikasi secara runtime (Android 13+).
     */
    public static void requestNotificationPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
    }

    /**
     * Periksa apakah aplikasi sudah dikecualikan dari optimasi baterai.
     */
    public static boolean isIgnoringBatteryOptimizations(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) activity.getSystemService(Activity.POWER_SERVICE);
            return pm.isIgnoringBatteryOptimizations(activity.getPackageName());
        }
        return true;
    }

    /**
     * Minta pengguna menonaktifkan optimasi baterai untuk aplikasi ini.
     */
    public static void requestDisableBatteryOptimization(Activity activity) {
        Toast.makeText(activity, "Nonaktifkan optimasi baterai", Toast.LENGTH_LONG).show();
        activity.startActivity(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Uri.parse("package:" + activity.getPackageName())));
    }
}
