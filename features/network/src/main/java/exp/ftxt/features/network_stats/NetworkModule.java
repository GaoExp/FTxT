package exp.ftxt.features.network_stats;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.WindowManager;

import exp.ftxt.shared.ui.OverlayDragHandler;
import exp.ftxt.shared.ui.OverlayModule;
import exp.ftxt.shared.ui.OverlayShadow;
import exp.ftxt.shared.ui.ShadowTextView;

public class NetworkModule implements OverlayModule {

    private ShadowTextView view;
    private WindowManager.LayoutParams params;
    private WindowManager wm;
    private Context context;
    private SharedPreferences prefs;
    private boolean running;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private long lastRxBytes;
    private long lastTxBytes;
    private long lastTimestamp;

    public static Runnable onPositionUpdate;
    private String orientationSuffix;

    @Override
    public void setOrientationSuffix(String suffix) {
        this.orientationSuffix = suffix;
    }

    @Override
    public void init(WindowManager windowManager, Context ctx, SharedPreferences sp) {
        wm = windowManager;
        context = ctx;
        prefs = sp;
        orientationSuffix = null;
        loadPosition();
    }

    @Override
    public void start(WindowManager windowManager, Context ctx) {
        if (running) return;
        wm = windowManager;
        context = ctx;
        prefs = ctx.getSharedPreferences("ftxt_prefs", Context.MODE_PRIVATE);

        view = new ShadowTextView(ctx);
        view.setShadowConfig(NetworkConfig.shadow);

        lastRxBytes = TrafficStats.getTotalRxBytes();
        lastTxBytes = TrafficStats.getTotalTxBytes();
        lastTimestamp = System.currentTimeMillis();

        view.setTextSize(NetworkConfig.size);
        view.setGravity(Gravity.CENTER_HORIZONTAL);
        applyBackground();
        updateDisplay();

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

    @Override
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
        params = null;
    }

    @Override
    public void updateSize(float size) {
        NetworkConfig.size = size;
        if (view != null) view.setTextSize(size);
    }

    @Override
    public void updateColor(int color) {
        NetworkConfig.color = color;
        updateDisplay();
    }

    @Override
    public void updateLabelColor(int color) {
        NetworkConfig.labelColor = color;
        updateDisplay();
    }

    @Override
    public void updateShadow() {
        if (view != null) view.setShadowConfig(NetworkConfig.shadow);
        OverlayShadow.apply(view, params, wm, NetworkConfig.shadow, 4f);
    }

    @Override
    public void updateBackground() {
        applyBackground();
    }

    private String posSuffix() {
        if (orientationSuffix != null) return "_" + orientationSuffix;
        return (context.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE) ? "_land" : "_port";
    }

    public void loadPosition() {
        String sfx = posSuffix();
        NetworkConfig.posX = prefs.getFloat("network_pos_x" + sfx, 0.75f);
        NetworkConfig.posY = prefs.getFloat("network_pos_y" + sfx, 0.05f);
        updatePosition();
    }

    @Override
    public void reloadPosition() {
        orientationSuffix = null;
        loadPosition();
    }

    @Override
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

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void hide() {
        if (view != null) view.setVisibility(android.view.View.GONE);
    }

    @Override
    public void show() {
        if (view != null) view.setVisibility(android.view.View.VISIBLE);
    }

    @Override
    public boolean isHidden() {
        if (view != null) return view.getVisibility() == android.view.View.GONE;
        return false;
    }

    @Override
    public int[] getCurrentPosition() {
        if (params != null) return new int[]{params.x, params.y};
        return null;
    }

    @Override
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
        if (NetworkConfig.bg.enabled) {
            int pad = NetworkConfig.bg.padding;
            view.setPadding(pad, pad, pad, pad);
        } else {
            view.setPadding(0, 0, 0, 0);
        }
        view.setBgEnabled(NetworkConfig.bg.enabled);
        view.setBgColor(NetworkConfig.bg.color);
        view.setBgOffsetX(NetworkConfig.bg.offsetX);
        view.setBgOffsetY(NetworkConfig.bg.offsetY);
        view.setBgMargin(NetworkConfig.bg.margin);
        view.setBgRadius(NetworkConfig.bg.radius);
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

    private void updateDisplay() {
        if (view == null) return;
        String text = getCurrentSpeedText();
        view.setTextColor(NetworkConfig.color);
        if (NetworkConfig.showOnlyValue) {
            view.setText(text.replace("↓", "").replace("↑", "").replace("MB/s", "").replace("KB/s", ""));
        } else {
            SpannableString spannable = new SpannableString(text);
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '↓' || c == '↑' || c == 'M' || c == 'B' || c == 'K' || c == '/' || c == 's') {
                    spannable.setSpan(new ForegroundColorSpan(NetworkConfig.labelColor),
                            i, i + 1, 0);
                }
            }
            view.setText(spannable);
        }
    }

    private final Runnable tickRunnable = new Runnable() {
        @Override
        public void run() {
            if (!running) return;
            long now = System.currentTimeMillis();
            long rxBytes = TrafficStats.getTotalRxBytes();
            long txBytes = TrafficStats.getTotalTxBytes();

            long elapsed = now - lastTimestamp;
            if (elapsed > 0) {
                long rxSpeed = ((rxBytes - lastRxBytes) * 1000) / elapsed;
                long txSpeed = ((txBytes - lastTxBytes) * 1000) / elapsed;
                lastRxSpeed = rxSpeed;
                lastTxSpeed = txSpeed;
                updateDisplay();
            }

            lastRxBytes = rxBytes;
            lastTxBytes = txBytes;
            lastTimestamp = now;

            handler.postDelayed(this, (long)(NetworkConfig.updateInterval * 1000));
        }
    };

    private long lastRxSpeed;
    private long lastTxSpeed;

    private String getCurrentSpeedText() {
        return formatSpeed(lastRxSpeed, lastTxSpeed);
    }

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
