package exp.ftxt.core;

import android.content.Context;
import android.os.PowerManager;

/**
 * Manajemen partial wake lock agar CPU tidak tidur saat overlay aktif.
 *
 * Dipakai oleh:
 * - FloatingService → core/FloatingService.java (field wakeLock, onCreate acquire, onDestroy release)
 */
public class WakeLockManager {

    private PowerManager.WakeLock wakeLock;

    /**
     * Acquire partial wake lock. Aman dipanggil berkali-kali (hanya acquire sekali).
     */
    public void acquire(Context context) {
        if (wakeLock != null && wakeLock.isHeld()) return;
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FTxT:OverlayWakeLock");
        wakeLock.acquire();
    }

    /**
     * Release wake lock jika sedang di-hold.
     */
    public void release() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }
}
