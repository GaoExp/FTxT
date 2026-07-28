package exp.ftxt.features.fps_display;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.Choreographer;
import android.view.Gravity;
import android.view.WindowManager;

import exp.ftxt.shared.ui.OverlayDragHandler;
import exp.ftxt.shared.ui.OverlayShadow;
import exp.ftxt.shared.ui.ShadowTextView;

public class FpsModule {

    private static final int FRAME_WINDOW_SIZE = 60;

    private ShadowTextView view;
    private WindowManager.LayoutParams params;
    private WindowManager wm;
    private Context context;
    private SharedPreferences prefs;
    private boolean running = false;
    private Choreographer choreographer;
    private long lastDisplayTime = 0;
    private float fpsValue = 0;
    private int screenWidth;
    private int screenHeight;
    private String orientationSuffix;
    private int posCalibrationY;

    private final long[] frameTimes = new long[FRAME_WINDOW_SIZE];
    private int frameIndex = 0;
    private int frameFilled = 0;

    public static Runnable onPositionUpdate;

    public void setOrientationSuffix(String suffix) {
        this.orientationSuffix = suffix;
    }

    public void init(WindowManager windowManager, Context ctx,
                     SharedPreferences sp) {
        wm = windowManager;
        context = ctx;
        prefs = sp;
        orientationSuffix = null;
        posCalibrationY = 0;
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;
        loadPosition(prefs);
    }

    public void start(WindowManager windowManager, Context ctx) {
        if (running) return;
        wm = windowManager;
        context = ctx;

        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;

        view = new ShadowTextView(ctx);
        view.setShadowConfig(FpsConfig.shadow);
        view.setTextSize(FpsConfig.size);
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
        params.x = (int)(FpsConfig.posX * screenWidth);
        params.y = (int)(FpsConfig.posY * screenHeight) + posCalibrationY;

        OverlayShadow.apply(view, params, wm, FpsConfig.shadow, 4f);
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
        lastDisplayTime = 0;
        frameIndex = 0;
        frameFilled = 0;
        choreographer = Choreographer.getInstance();
        choreographer.postFrameCallback(frameCallback);
    }

    public void stop() {
        running = false;
        if (choreographer != null) {
            choreographer.removeFrameCallback(frameCallback);
        }
        savePosition();
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
        FpsConfig.size = size;
        if (view != null) view.setTextSize(size);
    }

    public void updateColor(int color) {
        FpsConfig.color = color;
        updateDisplay();
    }

    public void updateLabelColor(int color) {
        FpsConfig.labelColor = color;
        updateDisplay();
    }

    public void updateShadow() {
        if (view != null) view.setShadowConfig(FpsConfig.shadow);
        OverlayShadow.apply(view, params, wm, FpsConfig.shadow, 4f);
    }

    public void updateBackground() {
        applyBackground();
    }

    public void updatePosition() {
        if (view != null && params != null && wm != null) {
            DisplayMetrics metrics = new DisplayMetrics();
            wm.getDefaultDisplay().getRealMetrics(metrics);
            screenWidth = metrics.widthPixels;
            screenHeight = metrics.heightPixels;
            params.x = (int)(FpsConfig.posX * screenWidth);
            params.y = (int)(FpsConfig.posY * screenHeight) + posCalibrationY;
            if (FpsConfig.safeArea && view.getWidth() > 0 && view.getHeight() > 0) {
                int maxX = Math.max(0, screenWidth - view.getWidth());
                int maxY = Math.max(0, screenHeight - view.getHeight());
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

    public void updateDisplay() {
        if (view != null) {
            if (FpsConfig.showOnlyValue) {
                view.setTextColor(FpsConfig.color);
                view.setText(String.format("%.1f", fpsValue));
            } else {
                view.setTextColor(FpsConfig.color);
                String text = String.format("%.1f FPS", fpsValue);
                SpannableString spannable = new SpannableString(text);
                int labelStart = text.indexOf(" FPS");
                if (labelStart > 0) {
                    spannable.setSpan(new ForegroundColorSpan(FpsConfig.labelColor),
                            labelStart, text.length(), 0);
                }
                view.setText(spannable);
            }
        }
    }

    private void applyBackground() {
        if (view == null) return;
        if (FpsConfig.bg.enabled) {
            int pad = FpsConfig.bg.padding;
            view.setPadding(pad, pad, pad, pad);
        } else {
            view.setPadding(0, 0, 0, 0);
        }
        view.setBgEnabled(FpsConfig.bg.enabled);
        view.setBgColor(FpsConfig.bg.color);
        view.setBgOffsetX(FpsConfig.bg.offsetX);
        view.setBgOffsetY(FpsConfig.bg.offsetY);
        view.setBgMargin(FpsConfig.bg.margin);
        view.setBgRadius(FpsConfig.bg.radius);
    }

    public void updateTouchFlags() {
        if (params == null || view == null || wm == null) return;

        if (FpsConfig.touchPassthrough) {
            params.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            view.setOnTouchListener(null);
        } else {
            params.flags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            view.setOnTouchListener(new OverlayDragHandler(params, wm,
                    this::savePosition,
                    () -> {
                        if (FpsConfig.safeArea && view != null && view.getWidth() > 0 && view.getHeight() > 0) {
                            params.x = Math.max(0, Math.min(params.x, screenWidth - view.getWidth()));
                            params.y = Math.max(0, Math.min(params.y, screenHeight - view.getHeight()));
                        }
                        if (params != null) {
                            DisplayMetrics metrics = new DisplayMetrics();
                            wm.getDefaultDisplay().getRealMetrics(metrics);
                            screenWidth = metrics.widthPixels;
                            screenHeight = metrics.heightPixels;
                            if (params.y < posCalibrationY) posCalibrationY = params.y;
                            FpsConfig.posX = (float) params.x / screenWidth;
                            FpsConfig.posY = (float)(params.y - posCalibrationY) / screenHeight;
                        }
                        if (onPositionUpdate != null) onPositionUpdate.run();
                    }));
        }

        try { wm.updateViewLayout(view, params); } catch (Exception e) { e.printStackTrace(); }
    }

    public void reloadPosition() {
        orientationSuffix = null;
        loadPosition(prefs);
        updatePosition();
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

    private String posSuffix() {
        if (orientationSuffix != null) return "_" + orientationSuffix;
        return (context.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE) ? "_land" : "_port";
    }

    public void loadPosition(SharedPreferences prefs) {
        String sfx = posSuffix();
        String keyX = "fps_pos_x" + sfx;
        String keyY = "fps_pos_y" + sfx;

        if (prefs.contains(keyX) && prefs.contains(keyY)) {
            FpsConfig.posX = prefs.getFloat(keyX, 0.5f);
            FpsConfig.posY = prefs.getFloat(keyY, 0.5f);
        } else if (prefs.contains("fps_x")) {
            FpsConfig.posX = (float) prefs.getInt("fps_x", 16) / screenWidth;
            FpsConfig.posY = (float) prefs.getInt("fps_y", 16) / screenHeight;
            prefs.edit()
                    .putFloat(keyX, FpsConfig.posX)
                    .putFloat(keyY, FpsConfig.posY)
                    .remove("fps_x")
                    .remove("fps_y")
                    .apply();
        } else {
            FpsConfig.posX = 0.05f;
            FpsConfig.posY = 0.05f;
        }
        if (params != null) {
            DisplayMetrics metrics = new DisplayMetrics();
            wm.getDefaultDisplay().getRealMetrics(metrics);
            screenWidth = metrics.widthPixels;
            screenHeight = metrics.heightPixels;
            params.x = (int)(FpsConfig.posX * screenWidth);
            params.y = (int)(FpsConfig.posY * screenHeight) + posCalibrationY;
        }
    }

    private void savePosition() {
        if (params != null && prefs != null) {
            DisplayMetrics metrics = new DisplayMetrics();
            wm.getDefaultDisplay().getRealMetrics(metrics);
            screenWidth = metrics.widthPixels;
            screenHeight = metrics.heightPixels;
            if (params.y < posCalibrationY) posCalibrationY = params.y;
            FpsConfig.posX = (float) params.x / screenWidth;
            FpsConfig.posY = (float)(params.y - posCalibrationY) / screenHeight;
            String sfx = posSuffix();
            prefs.edit()
                    .putFloat("fps_pos_x" + sfx, FpsConfig.posX)
                    .putFloat("fps_pos_y" + sfx, FpsConfig.posY)
                    .apply();
        }
    }

    private Choreographer.FrameCallback frameCallback = new Choreographer.FrameCallback() {
        @Override
        public void doFrame(long frameTimeNanos) {
            frameTimes[frameIndex] = frameTimeNanos;
            frameIndex = (frameIndex + 1) % FRAME_WINDOW_SIZE;
            if (frameFilled < FRAME_WINDOW_SIZE) frameFilled++;

            long now = System.nanoTime();
            long elapsedDisplay = (now - lastDisplayTime) / 1000000;
            if (lastDisplayTime == 0 || elapsedDisplay >= (long)(FpsConfig.updateInterval * 1000)) {
                if (frameFilled >= 2) {
                    int newest = (frameIndex - 1 + FRAME_WINDOW_SIZE) % FRAME_WINDOW_SIZE;
                    int oldest = (frameIndex) % FRAME_WINDOW_SIZE;
                    long spanNanos = frameTimes[newest] - frameTimes[oldest];
                    if (spanNanos > 0) {
                        fpsValue = (float)(frameFilled - 1) * 1_000_000_000f / spanNanos;
                    }
                }
                lastDisplayTime = now;
                updateDisplay();
            }
            if (running && FpsConfig.enabled) {
                choreographer.postFrameCallback(frameCallback);
            }
        }
    };
}
