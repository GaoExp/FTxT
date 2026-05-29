package exp.ftxt.features.fps;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.view.Choreographer;
import android.view.Gravity;
import android.view.WindowManager;

import exp.ftxt.shared.ui.OverlayDragHandler;
import exp.ftxt.shared.ui.OverlayShadow;
import exp.ftxt.shared.ui.ShadowTextView;

/**
 * Module untuk mengelola overlay FPS Display.
 *
 * Membuat, menghapus, dan memperbarui TextView FPS counter melalui WindowManager.
 * Menggunakan Choreographer.FrameCallback untuk menghitung FPS real-time.
 *
 * Menggunakan:
 * - OverlayDragHandler → shared/ui/OverlayDragHandler.java (drag-to-move)
 * - OverlayShadow      → shared/ui/OverlayShadow.java (shadow bg + elevation)
 *
 * Dipanggil oleh:
 * - FloatingService → core/FloatingService.java (static delegates)
 * - FpsConfig       → features/fps/FpsConfig.java (konfigurasi statis)
 */
public class FpsModule {

    private ShadowTextView view;
    private WindowManager.LayoutParams params;
    private WindowManager wm;
    private SharedPreferences prefs;
    private boolean running = false;
    private Choreographer choreographer;
    private long lastFrameTime = 0;
    private int frameCount = 0;
    private float fpsValue = 0;

    public void init(SharedPreferences sp) {
        prefs = sp;
    }

    public void start(WindowManager windowManager, Context context) {
        if (running) return;
        wm = windowManager;

        view = new ShadowTextView(context);
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
        params.x = prefs.getInt("fps_x", 16);
        params.y = prefs.getInt("fps_y", 16);

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
            // Gunakan OverlayDragHandler dari shared component
            // Lihat: OverlayDragHandler → shared/ui/OverlayDragHandler.java
            view.setOnTouchListener(new OverlayDragHandler(params, wm,
                    this::savePosition));
        }

        try { wm.updateViewLayout(view, params); } catch (Exception e) { e.printStackTrace(); }
    }

    public boolean isRunning() {
        return running;
    }

    private void savePosition() {
        if (params != null && prefs != null) {
            prefs.edit().putInt("fps_x", params.x).putInt("fps_y", params.y).apply();
        }
    }

    // Choreographer FrameCallback untuk menghitung FPS real-time
    // Menghitung frameCount dalam interval 1 detik
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
