package exp.ftxt.shared.ui;

import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

/**
 * Shared drag-to-move touch listener untuk overlay features.
 *
 * Menggantikan touchListener duplikat di TextModule dan FpsModule.
 *
 * Dipakai oleh:
 * - TextModule  → features/text/TextModule.java (touchListener field)
 * - FpsModule   → features/fps/FpsModule.java (touchListener field)
 *
 * Cara pakai:
 *   view.setOnTouchListener(new OverlayDragHandler(params, wm, () -> savePosition()));
 */
public class OverlayDragHandler implements View.OnTouchListener {

    private final WindowManager.LayoutParams params;
    private final WindowManager wm;
    private final Runnable onSavePosition;
    private int startX, startY;
    private float touchX, touchY;

    public OverlayDragHandler(WindowManager.LayoutParams params, WindowManager wm, Runnable onSavePosition) {
        this.params = params;
        this.wm = wm;
        this.onSavePosition = onSavePosition;
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
                wm.updateViewLayout(v, params);
                return true;
            case MotionEvent.ACTION_UP:
                if (onSavePosition != null) onSavePosition.run();
                return true;
        }
        return false;
    }
}
