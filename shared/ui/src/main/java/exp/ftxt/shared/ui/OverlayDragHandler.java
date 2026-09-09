package exp.ftxt.shared.ui;

import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class OverlayDragHandler implements View.OnTouchListener {

    private final WindowManager.LayoutParams params;
    private final WindowManager wm;
    private final Runnable onSavePosition;
    private final Runnable onDragMove;
    private final boolean safeArea;
    private final int screenWidth;
    private final int screenHeight;
    private int startX, startY;
    private float touchX, touchY;

    public OverlayDragHandler(WindowManager.LayoutParams params, WindowManager wm, Runnable onSavePosition) {
        this(params, wm, onSavePosition, null, false);
    }

    public OverlayDragHandler(WindowManager.LayoutParams params, WindowManager wm, Runnable onSavePosition, Runnable onDragMove) {
        this(params, wm, onSavePosition, onDragMove, false);
    }

    public OverlayDragHandler(WindowManager.LayoutParams params, WindowManager wm, Runnable onSavePosition, Runnable onDragMove, boolean safeArea) {
        this.params = params;
        this.wm = wm;
        this.onSavePosition = onSavePosition;
        this.onDragMove = onDragMove;
        this.safeArea = safeArea;
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = params.x;
                startY = params.y;
                touchX = event.getRawX();
                touchY = event.getRawY();
                return true;
            case MotionEvent.ACTION_MOVE:
                params.x = startX + (int) (event.getRawX() - touchX);
                params.y = startY + (int) (event.getRawY() - touchY);
                if (safeArea && v.getWidth() > 0 && v.getHeight() > 0) {
                    int maxX = Math.max(0, screenWidth - v.getWidth());
                    int maxY = Math.max(0, screenHeight - v.getHeight());
                    params.x = Math.max(0, Math.min(params.x, maxX));
                    params.y = Math.max(0, Math.min(params.y, maxY));
                }
                wm.updateViewLayout(v, params);
                if (onDragMove != null) onDragMove.run();
                return true;
            case MotionEvent.ACTION_UP:
                if (onSavePosition != null) onSavePosition.run();
                if (onDragMove != null) onDragMove.run();
                return true;
        }
        return false;
    }
}
