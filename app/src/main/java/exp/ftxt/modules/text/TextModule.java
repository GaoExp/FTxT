package exp.ftxt.modules.text;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.WindowManager;

import exp.ftxt.shared.ui.OverlayDragHandler;
import exp.ftxt.shared.ui.OverlayShadow;
import exp.ftxt.shared.ui.ShadowTextView;

/**
 * Module untuk mengelola overlay teks floating.
 *
 * Membuat, menghapus, dan memperbarui TextView overlay melalui WindowManager.
 *
 * Menggunakan:
 * - OverlayDragHandler → shared/ui/OverlayDragHandler.java (drag-to-move)
 * - OverlayShadow      → shared/ui/OverlayShadow.java (shadow bg + elevation)
 *
 * Dipanggil oleh:
 * - FloatingService → core/FloatingService.java (static delegates)
 * - TextConfig      → modules/text/TextConfig.java (konfigurasi statis)
 */
public class TextModule {

    private ShadowTextView view;
    private WindowManager.LayoutParams params;
    private WindowManager wm;
    private Context context;
    private SharedPreferences prefs;

    public void init(WindowManager windowManager, Context ctx,
                     SharedPreferences sp) {
        wm = windowManager;
        context = ctx;
        prefs = sp;
    }

    public void createOverlay() {
        if (view != null) return;

        view = new ShadowTextView(context);
        view.setShadowConfig(TextConfig.shadow);
        view.setText(TextConfig.text);
        view.setTextSize(TextConfig.size);
        view.setTextColor(TextConfig.color);
        applyBackground();

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

        OverlayShadow.apply(view, params, wm, TextConfig.shadow, 8f);

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

    public void updateShadow() {
        if (view != null) view.setShadowConfig(TextConfig.shadow);
        OverlayShadow.apply(view, params, wm, TextConfig.shadow, 8f);
    }

    public void updateBackground() {
        applyBackground();
    }

    private void applyBackground() {
        if (view == null) return;
        int pad = TextConfig.bgPadding;
        view.setPadding(pad, pad, pad, pad);
        view.setBgEnabled(TextConfig.bgEnabled);
        view.setBgColor(TextConfig.bgColor);
        view.setBgOffsetX(TextConfig.bgOffsetX);
        view.setBgOffsetY(TextConfig.bgOffsetY);
    }

    public void updateTouchFlags() {
        if (params == null || view == null) return;

        if (TextConfig.touchPassthrough) {
            params.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            view.setOnTouchListener(null);
        } else {
            params.flags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            // Gunakan OverlayDragHandler dari shared component
            // Lihat: OverlayDragHandler → shared/ui/OverlayDragHandler.java
            view.setOnTouchListener(new OverlayDragHandler(params, wm,
                    () -> savePosition(prefs)));
        }

        if (wm != null) {
            try { wm.updateViewLayout(view, params); } catch (Exception e) { e.printStackTrace(); }
        }
    }

    public void loadPosition(SharedPreferences prefs) {
        if (params != null) {
            params.x = prefs.getInt("text_x", 100);
            params.y = prefs.getInt("text_y", 300);
        }
    }

    public void savePosition(SharedPreferences prefs) {
        if (params != null) {
            prefs.edit().putInt("text_x", params.x).putInt("text_y", params.y).apply();
        }
    }

    public boolean isActive() {
        return view != null;
    }
}
