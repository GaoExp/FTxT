package exp.ftxt.core;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;

/**
 * Manajemen partial wake lock agar CPU tidak tidur saat overlay aktif.
 * Auto-renew setiap 4 menit agar tetap aktif tanpa timeout.
 *
 * Dipakai oleh:
 * - FloatingService → core/FloatingService.java (field wakeLock, onCreate acquire, onDestroy release)
 */
public class WakeLockManager {

    private static final long LOCK_TIMEOUT_MS = 5 * 60 * 1000; // 5 menit
    private static final long RENEW_INTERVAL_MS = 4 * 60 * 1000; // renew setiap 4 menit

    private Context context;
    private PowerManager.WakeLock wakeLock;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable renewRunnable = new Runnable() {
        @Override
        public void run() {
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
                acquireInternal();
            }
            handler.postDelayed(this, RENEW_INTERVAL_MS);
        }
    };

    /**
     * Acquire partial wake lock dengan timeout + auto-renew.
     * Aman dipanggil berkali-kali (hanya acquire sekali).
     */
    public void acquire(Context ctx) {
        if (wakeLock != null && wakeLock.isHeld()) return;
        this.context = ctx.getApplicationContext();
        acquireInternal();
        handler.postDelayed(renewRunnable, RENEW_INTERVAL_MS);
    }

    private void acquireInternal() {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FTxT:OverlayWakeLock");
        wakeLock.acquire(LOCK_TIMEOUT_MS);
    }

    /**
     * Release wake lock jika sedang di-hold, dan hentikan auto-renew.
     */
    public void release() {
        handler.removeCallbacks(renewRunnable);
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }
}
