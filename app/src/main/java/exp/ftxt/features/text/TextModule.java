package exp.ftxt.features.text;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
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
 * - TextConfig      → features/text/TextConfig.java (konfigurasi statis)
 */
public class TextModule {

    private ShadowTextView view;
    private WindowManager.LayoutParams params;
    private WindowManager wm;
    private Context context;
    private SharedPreferences prefs;
    private int screenWidth;
    private int screenHeight;
    private String orientationSuffix;

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
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;
        loadPosition(prefs);
    }

    public void createOverlay() {
        if (view != null) return;

        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;

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
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = (int)(TextConfig.posX * screenWidth);
        params.y = (int)(TextConfig.posY * screenHeight);

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

    public void updatePosition() {
        if (view != null && params != null && wm != null) {
            DisplayMetrics metrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(metrics);
            screenWidth = metrics.widthPixels;
            screenHeight = metrics.heightPixels;
            params.x = (int)(TextConfig.posX * screenWidth);
            params.y = (int)(TextConfig.posY * screenHeight);
            try {
                wm.updateViewLayout(view, params);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (onPositionUpdate != null) onPositionUpdate.run();
        }
    }

    public void updateBackground() {
        applyBackground();
    }

    private void applyBackground() {
        if (view == null) return;
        if (TextConfig.bgEnabled) {
            int pad = TextConfig.bgPadding;
            view.setPadding(pad, pad, pad, pad);
        } else {
            view.setPadding(0, 0, 0, 0);
        }
        view.setBgEnabled(TextConfig.bgEnabled);
        view.setBgColor(TextConfig.bgColor);
        view.setBgOffsetX(TextConfig.bgOffsetX);
        view.setBgOffsetY(TextConfig.bgOffsetY);
        view.setBgMargin(TextConfig.bgMargin);
        view.setBgRadius(TextConfig.bgRadius);
    }

    public void updateTouchFlags() {
        if (params == null || view == null) return;

        if (TextConfig.touchPassthrough) {
            params.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            view.setOnTouchListener(null);
        } else {
            params.flags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            view.setOnTouchListener(new OverlayDragHandler(params, wm,
                    () -> savePosition(prefs),
                    onPositionUpdate));
        }

        if (wm != null) {
            try { wm.updateViewLayout(view, params); } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private String posSuffix() {
        if (orientationSuffix != null) return "_" + orientationSuffix;
        return (context.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE) ? "_land" : "_port";
    }

    public void loadPosition(SharedPreferences prefs) {
        String sfx = posSuffix();
        String keyX = "text_pos_x" + sfx;
        String keyY = "text_pos_y" + sfx;

        if (prefs.contains(keyX) && prefs.contains(keyY)) {
            TextConfig.posX = prefs.getFloat(keyX, 0.5f);
            TextConfig.posY = prefs.getFloat(keyY, 0.5f);
        } else if (prefs.contains("text_pos_x")) {
            TextConfig.posX = prefs.getFloat("text_pos_x", 0.5f);
            TextConfig.posY = prefs.getFloat("text_pos_y", 0.5f);
            prefs.edit()
                    .putFloat(keyX, TextConfig.posX)
                    .putFloat(keyY, TextConfig.posY)
                    .remove("text_pos_x")
                    .remove("text_pos_y")
                    .apply();
        } else if (prefs.contains("text_x")) {
            TextConfig.posX = (float) prefs.getInt("text_x", 100) / screenWidth;
            TextConfig.posY = (float) prefs.getInt("text_y", 300) / screenHeight;
            prefs.edit()
                    .putFloat(keyX, TextConfig.posX)
                    .putFloat(keyY, TextConfig.posY)
                    .remove("text_x")
                    .remove("text_y")
                    .apply();
        } else {
            TextConfig.posX = 0.5f;
            TextConfig.posY = 0.5f;
        }
        if (params != null) {
            DisplayMetrics metrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(metrics);
            screenWidth = metrics.widthPixels;
            screenHeight = metrics.heightPixels;
            params.x = (int)(TextConfig.posX * screenWidth);
            params.y = (int)(TextConfig.posY * screenHeight);
        }
    }

    public void savePosition(SharedPreferences prefs) {
        if (params != null) {
            DisplayMetrics metrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(metrics);
            screenWidth = metrics.widthPixels;
            screenHeight = metrics.heightPixels;
            TextConfig.posX = (float) params.x / screenWidth;
            TextConfig.posY = (float) params.y / screenHeight;
            String sfx = posSuffix();
            prefs.edit()
                    .putFloat("text_pos_x" + sfx, TextConfig.posX)
                    .putFloat("text_pos_y" + sfx, TextConfig.posY)
                    .apply();
        }
    }

    public boolean isActive() {
        return view != null;
    }

    public int[] getCurrentPosition() {
        if (params != null) return new int[]{params.x, params.y};
        return null;
    }
}
