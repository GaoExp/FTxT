package exp.ftxt.features.network_stats;

import android.content.Context;
import android.graphics.PixelFormat;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.WindowManager;

import exp.ftxt.shared.ui.OverlayDragHandler;
import exp.ftxt.shared.ui.OverlayShadow;
import exp.ftxt.shared.ui.ShadowTextView;

public class NetworkModule {

    private ShadowTextView view;
    private WindowManager.LayoutParams params;
    private WindowManager wm;
    private Context context;
    private boolean running;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private long lastRxBytes;
    private long lastTxBytes;
    private long lastTimestamp;

    public static Runnable onPositionUpdate;
    private String orientationSuffix;

    public void setOrientationSuffix(String suffix) {
        this.orientationSuffix = suffix;
    }

    public void start(WindowManager windowManager, Context ctx) {
        if (running) return;
        wm = windowManager;
        context = ctx;

        view = new ShadowTextView(ctx);
        view.setShadowConfig(NetworkConfig.shadow);

        lastRxBytes = TrafficStats.getTotalRxBytes();
        lastTxBytes = TrafficStats.getTotalTxBytes();
        lastTimestamp = System.currentTimeMillis();

        view.setText(formatSpeed(0, 0));
        view.setTextSize(NetworkConfig.size);
        view.setTextColor(NetworkConfig.color);
        applyBackground();

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = (int)(NetworkConfig.posX * getScreenWidth());
        params.y = (int)(NetworkConfig.posY * getScreenHeight());

        OverlayShadow.apply(view, params, wm, NetworkConfig.shadow, 4f);
        updateTouchFlags();

        try {
            wm.addView(view, params);
        } catch (Exception e) {
            e.printStackTrace();
            view = null;
            return;
        }

        view.post(this::updatePosition);
        running = true;
        handler.post(tickRunnable);
    }

    public void stop() {
        running = false;
        handler.removeCallbacks(tickRunnable);
        if (view != null && wm != null) {
            try {
                wm.removeView(view);
            } catch (Exception e) {
                e.printStackTrace();
            }
            view = null;
        }
    }

    public void updateSize(float size) {
        NetworkConfig.size = size;
        if (view != null) view.setTextSize(size);
    }

    public void updateColor(int color) {
        NetworkConfig.color = color;
        if (view != null) view.setTextColor(color);
    }

    public void updateShadow() {
        if (view != null) view.setShadowConfig(NetworkConfig.shadow);
        OverlayShadow.apply(view, params, wm, NetworkConfig.shadow, 4f);
    }

    public void updateBackground() {
        applyBackground();
    }

    public void updatePosition() {
        if (view != null && params != null && wm != null) {
            params.x = (int)(NetworkConfig.posX * getScreenWidth());
            params.y = (int)(NetworkConfig.posY * getScreenHeight());
            if (NetworkConfig.safeArea && view.getWidth() > 0 && view.getHeight() > 0) {
                int maxX = Math.max(0, getScreenWidth() - view.getWidth());
                int maxY = Math.max(0, getScreenHeight() - view.getHeight());
                params.x = Math.max(0, Math.min(params.x, maxX));
                params.y = Math.max(0, Math.min(params.y, maxY));
            }
            try {
                wm.updateViewLayout(view, params);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (onPositionUpdate != null) onPositionUpdate.run();
        }
    }

    public boolean isRunning() {
        return running;
    }

    public int[] getCurrentPosition() {
        if (params != null) return new int[]{params.x, params.y};
        return null;
    }

    public void updateTouchFlags() {
        if (params == null || view == null || wm == null) return;

        if (NetworkConfig.touchPassthrough) {
            params.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            view.setOnTouchListener(null);
        } else {
            params.flags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            view.setOnTouchListener(new OverlayDragHandler(params, wm,
                    null,
                    () -> {
                        if (NetworkConfig.safeArea && view != null && view.getWidth() > 0 && view.getHeight() > 0) {
                            params.x = Math.max(0, Math.min(params.x, getScreenWidth() - view.getWidth()));
                            params.y = Math.max(0, Math.min(params.y, getScreenHeight() - view.getHeight()));
                        }
                        if (params != null) {
                            NetworkConfig.posX = Math.max(0, Math.min(1, (float) params.x / getScreenWidth()));
                            NetworkConfig.posY = Math.max(0, Math.min(1, (float) params.y / getScreenHeight()));
                        }
                        if (onPositionUpdate != null) onPositionUpdate.run();
                    }));
        }

        try { wm.updateViewLayout(view, params); } catch (Exception e) { e.printStackTrace(); }
    }

    private void applyBackground() {
        if (view == null) return;
        if (NetworkConfig.bgEnabled) {
            int pad = NetworkConfig.bgPadding;
            view.setPadding(pad, pad, pad, pad);
        } else {
            view.setPadding(0, 0, 0, 0);
        }
        view.setBgEnabled(NetworkConfig.bgEnabled);
        view.setBgColor(NetworkConfig.bgColor);
        view.setBgOffsetX(NetworkConfig.bgOffsetX);
        view.setBgOffsetY(NetworkConfig.bgOffsetY);
        view.setBgMargin(NetworkConfig.bgMargin);
        view.setBgRadius(NetworkConfig.bgRadius);
    }

    private int getScreenWidth() {
        android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(metrics);
        return metrics.widthPixels;
    }

    private int getScreenHeight() {
        android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(metrics);
        return metrics.heightPixels;
    }

    private final Runnable tickRunnable = new Runnable() {
        @Override
        public void run() {
            if (!running) return;
            long now = System.currentTimeMillis();
            long rxBytes = TrafficStats.getTotalRxBytes();
            long txBytes = TrafficStats.getTotalTxBytes();

            long elapsed = now - lastTimestamp;
            if (elapsed > 0 && view != null) {
                long rxSpeed = ((rxBytes - lastRxBytes) * 1000) / elapsed;
                long txSpeed = ((txBytes - lastTxBytes) * 1000) / elapsed;
                view.setText(formatSpeed(rxSpeed, txSpeed));
            }

            lastRxBytes = rxBytes;
            lastTxBytes = txBytes;
            lastTimestamp = now;

            handler.postDelayed(this, (long)(NetworkConfig.updateInterval * 1000));
        }
    };

    private String formatSpeed(long rxBytesPerSec, long txBytesPerSec) {
        return "↓" + formatUnit(rxBytesPerSec) + " ↑" + formatUnit(txBytesPerSec);
    }

    private String formatUnit(long bytesPerSec) {
        if (bytesPerSec >= 1048576) {
            return String.format(java.util.Locale.US, "%.2fMB/s", bytesPerSec / 1048576.0);
        }
        return (bytesPerSec / 1024) + "KB/s";
    }
}
