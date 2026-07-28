package exp.ftxt.features.clock_module;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.WindowManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import exp.ftxt.shared.ui.OverlayDragHandler;
import exp.ftxt.shared.ui.OverlayShadow;
import exp.ftxt.shared.ui.ShadowTextView;

public class ClockModule {

    private ShadowTextView view;
    private WindowManager.LayoutParams params;
    private WindowManager wm;
    private Context context;
    private SharedPreferences prefs;
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
        prefs = ctx.getSharedPreferences("ftxt_prefs", Context.MODE_PRIVATE);

        view = new ShadowTextView(ctx);
        view.setShadowConfig(ClockConfig.shadow);
        view.setText(getCurrentTime());
        view.setTextSize(ClockConfig.size);
        view.setTextColor(ClockConfig.color);
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
        params.x = (int)(ClockConfig.posX * getScreenWidth());
        params.y = (int)(ClockConfig.posY * getScreenHeight());

        OverlayShadow.apply(view, params, wm, ClockConfig.shadow, 4f);
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
        ClockConfig.size = size;
        if (view != null) view.setTextSize(size);
    }

    public void updateColor(int color) {
        ClockConfig.color = color;
        if (view != null) view.setTextColor(color);
    }

    public void updateShadow() {
        if (view != null) view.setShadowConfig(ClockConfig.shadow);
        OverlayShadow.apply(view, params, wm, ClockConfig.shadow, 4f);
    }

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
        ClockConfig.posX = prefs.getFloat("clock_pos_x" + sfx, 0.5f);
        ClockConfig.posY = prefs.getFloat("clock_pos_y" + sfx, 0.05f);
        updatePosition();
    }

    public void reloadPosition() {
        orientationSuffix = null;
        loadPosition();
    }

    public void updatePosition() {
        if (view != null && params != null && wm != null) {
            params.x = (int)(ClockConfig.posX * getScreenWidth());
            params.y = (int)(ClockConfig.posY * getScreenHeight());
            if (ClockConfig.safeArea && view.getWidth() > 0 && view.getHeight() > 0) {
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

    /**
     * Sembunyikan overlay tanpa stop module.
     */
    public void hide() {
        if (view != null) view.setVisibility(android.view.View.GONE);
    }

    /**
     * Tampilkan overlay kembali.
     */
    public void show() {
        if (view != null) view.setVisibility(android.view.View.VISIBLE);
    }

    /**
     * Cek apakah overlay sedang tersembunyi.
     */
    public boolean isHidden() {
        if (view != null) return view.getVisibility() == android.view.View.GONE;
        return false;
    }

    public int[] getCurrentPosition() {
        if (params != null) return new int[]{params.x, params.y};
        return null;
    }

    private void applyBackground() {
        if (view == null) return;
        if (ClockConfig.bg.enabled) {
            int pad = ClockConfig.bg.padding;
            view.setPadding(pad, pad, pad, pad);
        } else {
            view.setPadding(0, 0, 0, 0);
        }
        view.setBgEnabled(ClockConfig.bg.enabled);
        view.setBgColor(ClockConfig.bg.color);
        view.setBgOffsetX(ClockConfig.bg.offsetX);
        view.setBgOffsetY(ClockConfig.bg.offsetY);
        view.setBgMargin(ClockConfig.bg.margin);
        view.setBgRadius(ClockConfig.bg.radius);
    }

    public void updateTouchFlags() {
        if (params == null || view == null || wm == null) return;

        if (ClockConfig.touchPassthrough) {
            params.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            view.setOnTouchListener(null);
        } else {
            params.flags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            view.setOnTouchListener(new OverlayDragHandler(params, wm,
                    null,
                    () -> {
                        if (ClockConfig.safeArea && view != null && view.getWidth() > 0 && view.getHeight() > 0) {
                            params.x = Math.max(0, Math.min(params.x, getScreenWidth() - view.getWidth()));
                            params.y = Math.max(0, Math.min(params.y, getScreenHeight() - view.getHeight()));
                        }
                        if (params != null) {
                            ClockConfig.posX = Math.max(0, Math.min(1, (float) params.x / getScreenWidth()));
                            ClockConfig.posY = Math.max(0, Math.min(1, (float) params.y / getScreenHeight()));
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
                view.setText(getCurrentTime());
            }
            handler.postDelayed(this, 1000);
        }
    };

    private String getCurrentTime() {
        return new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
    }
}
