package exp.ftxt.shared.ui;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

public class DpadController {

    private final Handler repeatHandler = new Handler();
    private Runnable repeatRunnable;
    private OnDpadActionListener listener;

    private static final float STEP = 0.01f;
    private static final int REPEAT_INTERVAL = 100;

    public DpadController(View btnUp, View btnDown, View btnLeft, View btnRight, OnDpadActionListener listener) {
        this.listener = listener;
        setupButton(btnUp, 0, -STEP);
        setupButton(btnDown, 0, STEP);
        setupButton(btnLeft, -STEP, 0);
        setupButton(btnRight, STEP, 0);
    }

    private void setupButton(View button, float dx, float dy) {
        button.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (listener != null) listener.onDpadAction(dx, dy);
                    startRepeat(dx, dy);
                    v.setPressed(true);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    stopRepeat();
                    v.setPressed(false);
                    return true;
            }
            return false;
        });
    }

    private void startRepeat(float dx, float dy) {
        stopRepeat();
        repeatRunnable = () -> {
            if (listener != null) listener.onDpadAction(dx, dy);
            repeatHandler.postDelayed(repeatRunnable, REPEAT_INTERVAL);
        };
        repeatHandler.postDelayed(repeatRunnable, REPEAT_INTERVAL);
    }

    private void stopRepeat() {
        if (repeatRunnable != null) {
            repeatHandler.removeCallbacks(repeatRunnable);
            repeatRunnable = null;
        }
    }

    public void cleanup() {
        stopRepeat();
    }

    public interface OnDpadActionListener {
        void onDpadAction(float dx, float dy);
    }
}
