package exp.ftxt.features.battery_percentage;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.WindowManager;

import exp.ftxt.shared.ui.OverlayDragHandler;
import exp.ftxt.shared.ui.OverlayShadow;
import exp.ftxt.shared.ui.ShadowTextView;

public class BatteryPercentageModule {

    private ShadowTextView view;
    private WindowManager.LayoutParams params;
    private WindowManager wm;
    private Context context;
    private boolean running;
    private final Handler handler = new Handler(Looper.getMainLooper());

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
        view.setShadowConfig(BatteryPercentageConfig.shadow);
        view.setText(getBatteryPercentText());
        view.setTextSize(BatteryPercentageConfig.size);
        view.setTextColor(BatteryPercentageConfig.color);
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
        params.x = (int)(BatteryPercentageConfig.posX * getScreenWidth());
        params.y = (int)(BatteryPercentageConfig.posY * getScreenHeight());

        OverlayShadow.apply(view, params, wm, BatteryPercentageConfig.shadow, 4f);
        updateTouchFlags();

        try {
            wm.addView(view, params);
        } catch (Exception e) {
            e.printStackTrace();
            view = null;
            return;
        }

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
        BatteryPercentageConfig.size = size;
        if (view != null) view.setTextSize(size);
    }

    public void updateColor(int color) {
        BatteryPercentageConfig.color = color;
        if (view != null) view.setTextColor(color);
    }

    public void updateShadow() {
        if (view != null) view.setShadowConfig(BatteryPercentageConfig.shadow);
        OverlayShadow.apply(view, params, wm, BatteryPercentageConfig.shadow, 4f);
    }

    public void updateBackground() {
        applyBackground();
    }

    public void updatePosition() {
        if (view != null && params != null && wm != null) {
            params.x = (int)(BatteryPercentageConfig.posX * getScreenWidth());
            params.y = (int)(BatteryPercentageConfig.posY * getScreenHeight());
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

    private void applyBackground() {
        if (view == null) return;
        if (BatteryPercentageConfig.bgEnabled) {
            int pad = BatteryPercentageConfig.bgPadding;
            view.setPadding(pad, pad, pad, pad);
        } else {
            view.setPadding(0, 0, 0, 0);
        }
        view.setBgEnabled(BatteryPercentageConfig.bgEnabled);
        view.setBgColor(BatteryPercentageConfig.bgColor);
        view.setBgOffsetX(BatteryPercentageConfig.bgOffsetX);
        view.setBgOffsetY(BatteryPercentageConfig.bgOffsetY);
        view.setBgMargin(BatteryPercentageConfig.bgMargin);
        view.setBgRadius(BatteryPercentageConfig.bgRadius);
    }

    public void updateTouchFlags() {
        if (params == null || view == null || wm == null) return;

        if (BatteryPercentageConfig.touchPassthrough) {
            params.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            view.setOnTouchListener(null);
        } else {
            params.flags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            view.setOnTouchListener(new OverlayDragHandler(params, wm,
                    null,
                    () -> {
                        if (params != null) {
                            BatteryPercentageConfig.posX = Math.max(0, Math.min(1, (float) params.x / getScreenWidth()));
                            BatteryPercentageConfig.posY = Math.max(0, Math.min(1, (float) params.y / getScreenHeight()));
                        }
                        if (onPositionUpdate != null) onPositionUpdate.run();
                    }));
        }

        try { wm.updateViewLayout(view, params); } catch (Exception e) { e.printStackTrace(); }
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
            if (view != null) {
                view.setText(getBatteryPercentText());
            }
            handler.postDelayed(this, 5000);
        }
    };

    private String getBatteryPercentText() {
        try {
            Intent batteryIntent = context.registerReceiver(null,
                    new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            if (batteryIntent == null) return "N/A";
            int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
            int percent = (level * 100) / scale;
            return String.format("%d%%", percent);
        } catch (Exception e) {
            return "ERR";
        }
    }
}
