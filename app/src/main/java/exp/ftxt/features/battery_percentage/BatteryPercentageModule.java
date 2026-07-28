package exp.ftxt.features.battery_percentage;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.BatteryManager;
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

public class BatteryPercentageModule implements OverlayModule {

    private ShadowTextView view;
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
    }

    @Override
    public void start(WindowManager windowManager, Context ctx) {
        if (running) return;
        wm = windowManager;
        context = ctx;
        prefs = ctx.getSharedPreferences("ftxt_prefs", Context.MODE_PRIVATE);

        view = new ShadowTextView(ctx);
        view.setShadowConfig(BatteryPercentageConfig.shadow);
        view.setTextSize(BatteryPercentageConfig.size);
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
    }

    @Override
    public void updateSize(float size) {
        BatteryPercentageConfig.size = size;
        if (view != null) view.setTextSize(size);
    }

    @Override
    public void updateColor(int color) {
        BatteryPercentageConfig.color = color;
        updateDisplay();
    }

    @Override
    public void updateLabelColor(int color) {
        BatteryPercentageConfig.labelColor = color;
        updateDisplay();
    }

    @Override
    public void updateShadow() {
        if (view != null) view.setShadowConfig(BatteryPercentageConfig.shadow);
        OverlayShadow.apply(view, params, wm, BatteryPercentageConfig.shadow, 4f);
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
        BatteryPercentageConfig.posX = prefs.getFloat("battpct_pos_x" + sfx, 0.5f);
        BatteryPercentageConfig.posY = prefs.getFloat("battpct_pos_y" + sfx, 0.5f);
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

    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * Sembunyikan overlay tanpa stop module.
     */
    @Override
    public void hide() {
        if (view != null) view.setVisibility(android.view.View.GONE);
    }

    /**
     * Tampilkan overlay kembali.
     */
    @Override
    public void show() {
        if (view != null) view.setVisibility(android.view.View.VISIBLE);
    }

    /**
     * Cek apakah overlay sedang tersembunyi.
     */
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

    private void applyBackground() {
        if (view == null) return;
        if (BatteryPercentageConfig.bg.enabled) {
            int pad = BatteryPercentageConfig.bg.padding;
            view.setPadding(pad, pad, pad, pad);
        } else {
            view.setPadding(0, 0, 0, 0);
        }
        view.setBgEnabled(BatteryPercentageConfig.bg.enabled);
        view.setBgColor(BatteryPercentageConfig.bg.color);
        view.setBgOffsetX(BatteryPercentageConfig.bg.offsetX);
        view.setBgOffsetY(BatteryPercentageConfig.bg.offsetY);
        view.setBgMargin(BatteryPercentageConfig.bg.margin);
        view.setBgRadius(BatteryPercentageConfig.bg.radius);
    }

    @Override
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

    private void updateDisplay() {
        if (view == null) return;
        String text = getBatteryPercentText();
        view.setTextColor(BatteryPercentageConfig.color);
        SpannableString spannable = new SpannableString(text);
        int pctIndex = text.indexOf('%');
        if (pctIndex > 0) {
            spannable.setSpan(new ForegroundColorSpan(BatteryPercentageConfig.labelColor),
                    pctIndex, text.length(), 0);
        }
        view.setText(spannable);
    }

    private final Runnable tickRunnable = new Runnable() {
        @Override
        public void run() {
            if (!running) return;
            updateDisplay();
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
