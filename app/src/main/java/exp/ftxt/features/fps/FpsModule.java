package exp.ftxt.features.fps;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.view.Choreographer;
import android.view.Gravity;
import android.view.WindowManager;

import exp.ftxt.shared.ui.OverlayDragHandler;
import exp.ftxt.shared.ui.OverlayShadow;
import exp.ftxt.shared.ui.ShadowTextView;

public class FpsModule {

    private ShadowTextView view;
    private WindowManager.LayoutParams params;
    private WindowManager wm;
    private Context context;
    private SharedPreferences prefs;
    private boolean running = false;
    private Choreographer choreographer;
    private long lastFrameTime = 0;
    private int frameCount = 0;
    private float fpsValue = 0;
    private int screenWidth;
    private int screenHeight;
    private String orientationSuffix;
    private int posCalibrationY;

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
        view.setText(FpsConfig.showOnlyValue ? "0.0" : "0.0 FPS");
        view.setTextSize(FpsConfig.size);
        view.setTextColor(FpsConfig.color);
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

        running = true;
        lastFrameTime = 0;
        frameCount = 0;
        choreographer = Choreographer.getInstance();
        choreographer.postFrameCallback(frameCallback);
    }

    public void stop() {
        running = false;
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
        if (view != null) view.setTextColor(color);
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
            view.setText(FpsConfig.showOnlyValue
                    ? String.format("%.1f", fpsValue)
                    : String.format("%.1f FPS", fpsValue));
        }
    }

    private void applyBackground() {
        if (view == null) return;
        if (FpsConfig.bgEnabled) {
            int pad = FpsConfig.bgPadding;
            view.setPadding(pad, pad, pad, pad);
        } else {
            view.setPadding(0, 0, 0, 0);
        }
        view.setBgEnabled(FpsConfig.bgEnabled);
        view.setBgColor(FpsConfig.bgColor);
        view.setBgOffsetX(FpsConfig.bgOffsetX);
        view.setBgOffsetY(FpsConfig.bgOffsetY);
        view.setBgMargin(FpsConfig.bgMargin);
        view.setBgRadius(FpsConfig.bgRadius);
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

    public boolean isRunning() {
        return running;
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
            FpsConfig.posX = 0.5f;
            FpsConfig.posY = 0.5f;
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
            if (lastFrameTime == 0) {
                lastFrameTime = frameTimeNanos;
            }
            frameCount++;
            long elapsed = (frameTimeNanos - lastFrameTime) / 1000000;
            if (elapsed >= 1000) {
                fpsValue = (float) frameCount * 1000 / elapsed;
                frameCount = 0;
                lastFrameTime = frameTimeNanos;
                if (view != null) {
                    view.setText(FpsConfig.showOnlyValue
                            ? String.format("%.1f", fpsValue)
                            : String.format("%.1f FPS", fpsValue));
                }
            }
            if (running && FpsConfig.enabled) {
                choreographer.postFrameCallback(frameCallback);
            }
        }
    };
}
