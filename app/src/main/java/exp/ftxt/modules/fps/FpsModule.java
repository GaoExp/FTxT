package exp.ftxt.modules.fps;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Choreographer;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;

public class FpsModule {

    private TextView view;
    private WindowManager.LayoutParams params;
    private WindowManager wm;
    private boolean running = false;
    private Choreographer choreographer;
    private long lastFrameTime = 0;
    private int frameCount = 0;
    private float fpsValue = 0;

    public void start(WindowManager windowManager, Context context) {
        wm = windowManager;

        view = new TextView(context);
        view.setText("0.0 FPS");
        view.setTextSize(FpsConfig.size);
        view.setTextColor(FpsConfig.color);
        view.setPadding(10, 8, 10, 8);

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 16;
        params.y = 16;

        updateShadow();

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

    public void updateShadow(boolean enabled) {
        FpsConfig.shadow = enabled;
        if (view != null) {
            if (enabled) {
                view.setBackgroundColor(0x88000000);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    view.setElevation(4f);
            } else {
                view.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    view.setElevation(0f);
            }
            if (wm != null) {
                try { wm.updateViewLayout(view, params); } catch (Exception e) { e.printStackTrace(); }
            }
        }
    }

    private void updateShadow() {
        if (FpsConfig.shadow) {
            view.setBackgroundColor(0x88000000);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                view.setElevation(4f);
        }
    }

    public boolean isRunning() {
        return running;
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
                    view.setText(String.format("%.1f FPS", fpsValue));
                }
            }
            if (running && FpsConfig.enabled) {
                choreographer.postFrameCallback(frameCallback);
            }
        }
    };
}
