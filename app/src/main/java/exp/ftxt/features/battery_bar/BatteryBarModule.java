package exp.ftxt.features.battery_bar;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.WindowManager;

import exp.ftxt.shared.ui.OverlayDragHandler;
import exp.ftxt.shared.ui.OverlayModule;

public class BatteryBarModule implements OverlayModule {

    private BatteryBarView view;
    private WindowManager.LayoutParams params;
    private WindowManager wm;
    private Context context;
    private SharedPreferences prefs;
    private boolean running;
    private final Handler handler = new Handler(Looper.getMainLooper());

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

        view = new BatteryBarView(ctx);
        view.setBarConfig(BatteryBarConfig.color, BatteryBarConfig.emptyColor,
                BatteryBarConfig.showEmptyStrip, dp(BatteryBarConfig.radius),
                BatteryBarConfig.autoColor, BatteryBarConfig.lowColor, isHorizontalBar());
        view.setFadeSpeed(BatteryBarConfig.fadeSpeed);

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

        updateTouchFlags();

        try {
            wm.addView(view, params);
        } catch (Exception e) {
            e.printStackTrace();
            view = null;
            return;
        }

        applyLayout();
        updateDisplay();

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
    public boolean isRunning() {
        return running;
    }

    @Override
    public void updateSize(float size) {
        // Ukuran bar diatur via ketebalan/panjang, di-handle restart modul.
    }

    @Override
    public void updateColor(int color) {
        BatteryBarConfig.color = color;
        applyAppearance();
    }

    @Override
    public void updateLabelColor(int color) {
        // Tidak ada label pada bar.
    }

    @Override
    public void updateShadow() {
        // Bar tidak memakai shadow.
    }

    @Override
    public void updateBackground() {
        // Bar tidak punya background terpisah.
    }

    @Override
    public void updatePosition() {
        if (view != null && params != null && wm != null) {
            applyLayout();
            if (onPositionUpdate != null) onPositionUpdate.run();
        }
    }

    @Override
    public void updateTouchFlags() {
        if (params == null || view == null || wm == null) return;

        if (BatteryBarConfig.touchPassthrough) {
            params.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            view.setOnTouchListener(null);
        } else {
            params.flags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            view.setOnTouchListener(new OverlayDragHandler(params, wm,
                    null,
                    () -> {
                        if (BatteryBarConfig.safeArea && view != null && params != null) {
                            params.x = Math.max(0, Math.min(params.x, getScreenWidth() - params.width));
                            params.y = Math.max(0, Math.min(params.y, getScreenHeight() - params.height));
                        }
                        if (params != null) {
                            BatteryBarConfig.posX = Math.max(0, Math.min(1, (float) params.x / getScreenWidth()));
                            BatteryBarConfig.posY = Math.max(0, Math.min(1, (float) params.y / getScreenHeight()));
                        }
                        if (onPositionUpdate != null) onPositionUpdate.run();
                    }));
        }

        try { wm.updateViewLayout(view, params); } catch (Exception e) { e.printStackTrace(); }
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

    public boolean isHorizontalBar() {
        if (BatteryBarConfig.quickMode) {
            return "top".equals(BatteryBarConfig.quickSide) || "bottom".equals(BatteryBarConfig.quickSide);
        }
        return BatteryBarConfig.horizontal;
    }

    public void applyAppearance() {
        if (view == null) return;
        view.setBarConfig(BatteryBarConfig.color, BatteryBarConfig.emptyColor,
                BatteryBarConfig.showEmptyStrip, dp(BatteryBarConfig.radius),
                BatteryBarConfig.autoColor, BatteryBarConfig.lowColor, isHorizontalBar());
        view.setFadeSpeed(BatteryBarConfig.fadeSpeed);
        updateDisplay();
    }

    public void reloadLayout() {
        if (view != null && params != null && wm != null) {
            view.setBarOrientation(isHorizontalBar());
            applyLayout();
        }
    }

    private void applyLayout() {
        if (view == null || params == null || wm == null) return;
        int sw = getScreenWidth();
        int sh = getScreenHeight();
        int thicknessPx = dp(BatteryBarConfig.thickness);
        boolean horizontal = isHorizontalBar();

        if (BatteryBarConfig.quickMode) {
            String side = BatteryBarConfig.quickSide;
            boolean hSide = "top".equals(side) || "bottom".equals(side);
            int lenPx = hSide ? sw : sh;
            params.width = hSide ? lenPx : thicknessPx;
            params.height = hSide ? thicknessPx : lenPx;
            int margin = BatteryBarConfig.safeArea ? dp(8) : 0;
            switch (side) {
                case "bottom":
                    params.x = 0;
                    params.y = sh - (hSide ? thicknessPx : lenPx) - margin;
                    break;
                case "left":
                    params.x = margin;
                    params.y = 0;
                    break;
                case "right":
                    params.x = sw - (hSide ? lenPx : thicknessPx) - margin;
                    params.y = 0;
                    break;
                default:
                    params.x = 0;
                    params.y = margin;
                    break;
            }
        } else {
            int lenPx = Math.round(BatteryBarConfig.length * (horizontal ? sw : sh));
            params.width = horizontal ? lenPx : thicknessPx;
            params.height = horizontal ? thicknessPx : lenPx;
            params.x = Math.round(BatteryBarConfig.posX * sw);
            params.y = Math.round(BatteryBarConfig.posY * sh);
            if (BatteryBarConfig.safeArea) {
                int maxX = Math.max(0, sw - params.width);
                int maxY = Math.max(0, sh - params.height);
                params.x = Math.max(0, Math.min(params.x, maxX));
                params.y = Math.max(0, Math.min(params.y, maxY));
            }
        }

        try {
            wm.updateViewLayout(view, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateDisplay() {
        if (view == null) return;
        int percent = getBatteryPercent();
        boolean charging = isCharging();
        boolean low = percent < BatteryBarConfig.lowThreshold;
        view.updateStatus(percent, charging, low);
    }

    private int getBatteryPercent() {
        try {
            Intent intent = context.registerReceiver(null,
                    new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            if (intent == null) return 0;
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
            return (scale > 0) ? (level * 100) / scale : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private boolean isCharging() {
        try {
            Intent intent = context.registerReceiver(null,
                    new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            if (intent == null) return false;
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN);
            return status == BatteryManager.BATTERY_STATUS_CHARGING
                    || status == BatteryManager.BATTERY_STATUS_FULL;
        } catch (Exception e) {
            return false;
        }
    }

    private String posSuffix() {
        if (orientationSuffix != null) return "_" + orientationSuffix;
        return (context.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE) ? "_land" : "_port";
    }

    public void loadPosition() {
        String sfx = posSuffix();
        BatteryBarConfig.posX = prefs.getFloat("batbar_pos_x" + sfx, 0.05f);
        BatteryBarConfig.posY = prefs.getFloat("batbar_pos_y" + sfx, 0.9f);
        reloadLayout();
    }

    @Override
    public void reloadPosition() {
        orientationSuffix = null;
        loadPosition();
    }

    private final Runnable tickRunnable = new Runnable() {
        @Override
        public void run() {
            if (!running) return;
            updateDisplay();
            handler.postDelayed(this, (long) (BatteryBarConfig.updateInterval * 1000));
        }
    };

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

    private int dp(int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }
}
