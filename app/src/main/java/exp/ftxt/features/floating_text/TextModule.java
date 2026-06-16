package exp.ftxt.features.floating_text;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
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
 * - TextConfig      → features/floating_text/TextConfig.java (konfigurasi statis)
 */
public class TextModule {

    private ShadowTextView view;
    private SealPatternView sealView;
    private WindowManager.LayoutParams params;
    private WindowManager wm;
    private Context context;
    private SharedPreferences prefs;
    private int screenWidth;
    private int screenHeight;
    private String orientationSuffix;
    private int posCalibrationY;
    private boolean patternMode;

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

    public void createOverlay() {
        if (view != null) return;
        patternMode = TextConfig.patternEnabled;

        if (patternMode) {
            createPatternOverlay();
            return;
        }

        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(metrics);
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
        params.y = (int)(TextConfig.posY * screenHeight) + posCalibrationY;

        OverlayShadow.apply(view, params, wm, TextConfig.shadow, 8f);

        updateTouchFlags();

        wm.addView(view, params);
        view.post(this::updatePosition);
    }

    private void createPatternOverlay() {
        sealView = new SealPatternView(context);

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 0;

        try {
            wm.addView(sealView, params);
        } catch (Exception e) {
            e.printStackTrace();
            sealView = null;
        }
    }

    public void destroyOverlay() {
        if (sealView != null && wm != null) {
            try {
                wm.removeView(sealView);
            } catch (Exception e) {
                e.printStackTrace();
            }
            sealView = null;
        }
        if (view != null && wm != null) {
            try {
                wm.removeView(view);
            } catch (Exception e) {
                e.printStackTrace();
            }
            view = null;
        }
    }

    public void updatePattern() {
        boolean newMode = TextConfig.patternEnabled;
        if (newMode != patternMode) {
            destroyOverlay();
            createOverlay();
        } else if (sealView != null) {
            sealView.invalidate();
        }
    }

    public void updateText(String text) {
        if (view != null) view.setText(text);
        if (sealView != null) sealView.invalidate();
    }

    public void updateSize(float size) {
        if (view != null) view.setTextSize(size);
        if (sealView != null) sealView.invalidate();
    }

    public void updateColor(int color) {
        if (view != null) view.setTextColor(color);
        if (sealView != null) sealView.invalidate();
    }

    public void updateShadow() {
        if (view != null) view.setShadowConfig(TextConfig.shadow);
        OverlayShadow.apply(view, params, wm, TextConfig.shadow, 8f);
    }

    public void updatePosition() {
        if (view != null && params != null && wm != null) {
            DisplayMetrics metrics = new DisplayMetrics();
            wm.getDefaultDisplay().getRealMetrics(metrics);
            screenWidth = metrics.widthPixels;
            screenHeight = metrics.heightPixels;
            params.x = (int)(TextConfig.posX * screenWidth);
            params.y = (int)(TextConfig.posY * screenHeight) + posCalibrationY;
            if (TextConfig.safeArea && view.getWidth() > 0 && view.getHeight() > 0) {
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

    public void updateBackground() {
        applyBackground();
    }

    private void applyBackground() {
        if (view == null) return;
        if (TextConfig.bg.enabled) {
            int pad = TextConfig.bg.padding;
            view.setPadding(pad, pad, pad, pad);
        } else {
            view.setPadding(0, 0, 0, 0);
        }
        view.setBgEnabled(TextConfig.bg.enabled);
        view.setBgColor(TextConfig.bg.color);
        view.setBgOffsetX(TextConfig.bg.offsetX);
        view.setBgOffsetY(TextConfig.bg.offsetY);
        view.setBgMargin(TextConfig.bg.margin);
        view.setBgRadius(TextConfig.bg.radius);
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
                    () -> {
                        if (TextConfig.safeArea && view != null && view.getWidth() > 0 && view.getHeight() > 0) {
                            params.x = Math.max(0, Math.min(params.x, screenWidth - view.getWidth()));
                            params.y = Math.max(0, Math.min(params.y, screenHeight - view.getHeight()));
                        }
                        if (params != null) {
                            DisplayMetrics metrics = new DisplayMetrics();
                            wm.getDefaultDisplay().getRealMetrics(metrics);
                            screenWidth = metrics.widthPixels;
                            screenHeight = metrics.heightPixels;
                            if (params.y < posCalibrationY) posCalibrationY = params.y;
                            TextConfig.posX = (float) params.x / screenWidth;
                            TextConfig.posY = (float)(params.y - posCalibrationY) / screenHeight;
                        }
                        if (onPositionUpdate != null) onPositionUpdate.run();
                    }));
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
            TextConfig.posY = 0.8f;
        }
        if (params != null) {
            DisplayMetrics metrics = new DisplayMetrics();
            wm.getDefaultDisplay().getRealMetrics(metrics);
            screenWidth = metrics.widthPixels;
            screenHeight = metrics.heightPixels;
            params.x = (int)(TextConfig.posX * screenWidth);
            params.y = (int)(TextConfig.posY * screenHeight) + posCalibrationY;
        }
    }

    public void savePosition(SharedPreferences prefs) {
        if (params != null) {
            DisplayMetrics metrics = new DisplayMetrics();
            wm.getDefaultDisplay().getRealMetrics(metrics);
            screenWidth = metrics.widthPixels;
            screenHeight = metrics.heightPixels;
            if (params.y < posCalibrationY) posCalibrationY = params.y;
            TextConfig.posX = (float) params.x / screenWidth;
            TextConfig.posY = (float)(params.y - posCalibrationY) / screenHeight;
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

    private class SealPatternView extends View {
        private final Paint paint;

        SealPatternView(Context context) {
            super(context);
            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setSubpixelText(true);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            String text = TextConfig.text;
            if (text == null || text.isEmpty()) return;

            float textSize = TextConfig.size;
            int color = TextConfig.color;
            float spacingH = TextConfig.patternSpacingH;
            float spacingV = TextConfig.patternSpacingV;
            float angle = TextConfig.patternAngle;

            paint.setTextSize(textSize);
            paint.setColor(color);
            paint.setAlpha(Color.alpha(color));

            float textWidth = paint.measureText(text);
            Paint.FontMetrics fm = paint.getFontMetrics();
            float textHeight = fm.descent - fm.ascent;

            int w = getWidth();
            int h = getHeight();
            float diagonal = (float) Math.sqrt(w * w + h * h);

            canvas.save();
            canvas.rotate(angle, w / 2f, h / 2f);

            float startX = -diagonal;
            float startY = -diagonal + textHeight;

            for (float x = startX; x < diagonal; x += spacingH) {
                for (float y = startY; y < diagonal; y += spacingV) {
                    canvas.drawText(text, x, y, paint);
                }
            }

            canvas.restore();
        }
    }
}
