package exp.ftxt.core;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;

public class WakeLockManager {

    private static final long LOCK_TIMEOUT_MS = 5 * 60 * 1000;
    private static final long RENEW_INTERVAL_MS = 4 * 60 * 1000;

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

    public void acquire(Context ctx) {
        if (wakeLock != null && wakeLock.isHeld()) return;
        this.context = ctx.getApplicationContext();
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (pm != null && !pm.isInteractive()) return;
        acquireInternal();
        handler.postDelayed(renewRunnable, RENEW_INTERVAL_MS);
    }

    private void acquireInternal() {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FTxT:OverlayWakeLock");
        wakeLock.acquire(LOCK_TIMEOUT_MS);
    }

    public void release() {
        handler.removeCallbacks(renewRunnable);
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    public boolean isHeld() {
        return wakeLock != null && wakeLock.isHeld();
    }
}
