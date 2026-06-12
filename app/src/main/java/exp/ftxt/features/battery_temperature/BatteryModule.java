package exp.ftxt.features.battery_temperature;

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

public class BatteryModule {

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
        view.setShadowConfig(BatteryConfig.shadow);
        view.setText(getBatteryTempText());
        view.setTextSize(BatteryConfig.size);
        view.setTextColor(BatteryConfig.color);
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
        params.x = (int)(BatteryConfig.posX * getScreenWidth());
        params.y = (int)(BatteryConfig.posY * getScreenHeight());

        OverlayShadow.apply(view, params, wm, BatteryConfig.shadow, 4f);
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
        BatteryConfig.size = size;
        if (view != null) view.setTextSize(size);
    }

    public void updateColor(int color) {
        BatteryConfig.color = color;
        if (view != null) view.setTextColor(color);
    }

    public void updateShadow() {
        if (view != null) view.setShadowConfig(BatteryConfig.shadow);
        OverlayShadow.apply(view, params, wm, BatteryConfig.shadow, 4f);
    }

    public void updateBackground() {
        applyBackground();
    }

    public void updatePosition() {
        if (view != null && params != null && wm != null) {
            params.x = (int)(BatteryConfig.posX * getScreenWidth());
            params.y = (int)(BatteryConfig.posY * getScreenHeight());
            if (BatteryConfig.safeArea && view.getWidth() > 0 && view.getHeight() > 0) {
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

    private void applyBackground() {
        if (view == null) return;
        if (BatteryConfig.bgEnabled) {
            int pad = BatteryConfig.bgPadding;
            view.setPadding(pad, pad, pad, pad);
        } else {
            view.setPadding(0, 0, 0, 0);
        }
        view.setBgEnabled(BatteryConfig.bgEnabled);
        view.setBgColor(BatteryConfig.bgColor);
        view.setBgOffsetX(BatteryConfig.bgOffsetX);
        view.setBgOffsetY(BatteryConfig.bgOffsetY);
        view.setBgMargin(BatteryConfig.bgMargin);
        view.setBgRadius(BatteryConfig.bgRadius);
    }

    public void updateTouchFlags() {
        if (params == null || view == null || wm == null) return;

        if (BatteryConfig.touchPassthrough) {
            params.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            view.setOnTouchListener(null);
        } else {
            params.flags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            view.setOnTouchListener(new OverlayDragHandler(params, wm,
                    null,
                    () -> {
                        if (BatteryConfig.safeArea && view != null && view.getWidth() > 0 && view.getHeight() > 0) {
                            params.x = Math.max(0, Math.min(params.x, getScreenWidth() - view.getWidth()));
                            params.y = Math.max(0, Math.min(params.y, getScreenHeight() - view.getHeight()));
                        }
                        if (params != null) {
                            BatteryConfig.posX = Math.max(0, Math.min(1, (float) params.x / getScreenWidth()));
                            BatteryConfig.posY = Math.max(0, Math.min(1, (float) params.y / getScreenHeight()));
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
                view.setText(getBatteryTempText());
            }
            handler.postDelayed(this, (long)(BatteryConfig.updateInterval * 1000));
        }
    };

    private String getBatteryTempText() {
        try {
            Intent batteryIntent = context.registerReceiver(null,
                    new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            if (batteryIntent == null) return "N/A";
            StringBuilder sb = new StringBuilder();
            if (BatteryConfig.showTemperature) {
                int temp = batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
                float celsius = temp / 10f;
                int celsiusInt = Math.round(celsius);
                if (BatteryConfig.showOnlyValue) {
                    sb.append(celsiusInt);
                } else {
                    sb.append(String.format("%d°C", celsiusInt));
                }
            }
            if (BatteryConfig.showPercentage) {
                int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                if (sb.length() > 0) sb.append(" ");
                if (BatteryConfig.showOnlyValue) {
                    sb.append(level);
                } else {
                    sb.append(String.format("%d%%", level));
                }
            }
            return sb.length() == 0 ? "" : sb.toString();
        } catch (Exception e) {
            return "ERR";
        }
    }
}
