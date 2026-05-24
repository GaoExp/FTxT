package exp.ftxt.modules.text;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class TextModule {

    private TextView view;
    private WindowManager.LayoutParams params;
    private WindowManager wm;
    private Context context;
    private android.content.SharedPreferences prefs;
    private int startX, startY;
    private float touchX, touchY;

    public void init(WindowManager windowManager, Context ctx,
                     android.content.SharedPreferences sp) {
        wm = windowManager;
        context = ctx;
        prefs = sp;
    }

    public void createOverlay() {
        if (view != null) return;

        view = new TextView(context);
        view.setText(TextConfig.text);
        view.setTextSize(TextConfig.size);
        view.setTextColor(TextConfig.color);
        view.setPadding(25, 20, 25, 20);

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = prefs.getInt("text_x", 100);
        params.y = prefs.getInt("text_y", 300);

        updateShadow();
        updateTouchFlags();

        wm.addView(view, params);
    }

    public void destroyOverlay() {
        if (view != null && wm != null) {
            try {
                wm.removeView(view);
            } catch (Exception e) {
                e.printStackTrace();
            }
            view = null;
        }
    }

    public void updateText(String text) {
        if (view != null) view.setText(text);
    }

    public void updateSize(float size) {
        if (view != null) view.setTextSize(size);
    }

    public void updateColor(int color) {
        if (view != null) view.setTextColor(color);
    }

    public void updateShadow(boolean enabled) {
        TextConfig.shadow = enabled;
        if (view != null) {
            if (enabled) {
                view.setBackgroundColor(0x88000000);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
                    view.setElevation(8f);
            } else {
                view.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
                    view.setElevation(0f);
            }
            if (wm != null) {
                try { wm.updateViewLayout(view, params); } catch (Exception e) { e.printStackTrace(); }
            }
        }
    }

    private void updateShadow() {
        if (TextConfig.shadow) {
            view.setBackgroundColor(0x88000000);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
                view.setElevation(8f);
        }
    }

    public void updateTouchFlags() {
        if (params == null || view == null) return;

        if (TextConfig.touchPassthrough) {
            params.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            view.setOnTouchListener(null);
        } else {
            params.flags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            view.setOnTouchListener(touchListener);
        }

        if (wm != null) {
            try { wm.updateViewLayout(view, params); } catch (Exception e) { e.printStackTrace(); }
        }
    }

    public void loadPosition(android.content.SharedPreferences prefs) {
        if (params != null) {
            params.x = prefs.getInt("text_x", 100);
            params.y = prefs.getInt("text_y", 300);
        }
    }

    public void savePosition(android.content.SharedPreferences prefs) {
        if (params != null) {
            prefs.edit().putInt("text_x", params.x).putInt("text_y", params.y).apply();
        }
    }

    public boolean isActive() {
        return view != null;
    }

    private View.OnTouchListener touchListener = new View.OnTouchListener() {
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
                    wm.updateViewLayout(view, params);
                    return true;
                case MotionEvent.ACTION_UP:
                    savePosition(prefs);
                    return true;
            }
            return false;
        }
    };
}
