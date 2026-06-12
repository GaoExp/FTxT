package exp.ftxt.features.watermark;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import exp.ftxt.shared.ui.OverlayDragHandler;
import exp.ftxt.shared.ui.OverlayShadow;
import exp.ftxt.shared.ui.ShadowTextView;

public class WatermarkModule {

    private ShadowTextView view;
    private SealPatternView sealView;
    private WindowManager.LayoutParams params;
    private WindowManager wm;
    private Context context;
    private boolean running;
    private boolean patternMode;

    public static Runnable onPositionUpdate;
    private String orientationSuffix;

    public void setOrientationSuffix(String suffix) {
        this.orientationSuffix = suffix;
    }

    public void start(WindowManager windowManager, Context ctx) {
        if (running) return;
        wm = windowManager;
        context = ctx;
        patternMode = WatermarkConfig.patternEnabled;

        if (patternMode) {
            startPattern();
        } else {
            startSingle();
        }

        running = true;
    }

    private void startSingle() {
        view = new ShadowTextView(context);
        view.setShadowConfig(WatermarkConfig.shadow);
        view.setText(WatermarkConfig.text);
        view.setTextSize(WatermarkConfig.size);
        view.setTextColor(WatermarkConfig.color);
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
        params.x = (int)(WatermarkConfig.posX * getScreenWidth());
        params.y = (int)(WatermarkConfig.posY * getScreenHeight());

        OverlayShadow.apply(view, params, wm, WatermarkConfig.shadow, 4f);
        updateTouchFlags();

        try {
            wm.addView(view, params);
        } catch (Exception e) {
            e.printStackTrace();
            view = null;
            return;
        }

        view.post(this::updatePosition);
    }

    private void startPattern() {
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

    public void stop() {
        running = false;
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

    public void updateText(String text) {
        WatermarkConfig.text = text;
        if (view != null) view.setText(text);
        if (sealView != null) sealView.invalidate();
    }

    public void updateSize(float size) {
        WatermarkConfig.size = size;
        if (view != null) view.setTextSize(size);
        if (sealView != null) sealView.invalidate();
    }

    public void updateColor(int color) {
        WatermarkConfig.color = color;
        if (view != null) view.setTextColor(color);
        if (sealView != null) sealView.invalidate();
    }

    public void updateShadow() {
        if (view != null) view.setShadowConfig(WatermarkConfig.shadow);
        OverlayShadow.apply(view, params, wm, WatermarkConfig.shadow, 4f);
    }

    public void updateBackground() {
        applyBackground();
    }

    public void updatePosition() {
        if (view != null && params != null && wm != null) {
            params.x = (int)(WatermarkConfig.posX * getScreenWidth());
            params.y = (int)(WatermarkConfig.posY * getScreenHeight());
            if (WatermarkConfig.safeArea && view.getWidth() > 0 && view.getHeight() > 0) {
                int maxX = Math.max(0, getScreenWidth() - view.getWidth());
                int maxY = Math.max(0, getScreenHeight() - view.getHeight());
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

    public void updatePattern() {
        boolean newMode = WatermarkConfig.patternEnabled;
        if (newMode != patternMode && running) {
            stop();
            start(wm, context);
        } else if (sealView != null) {
            sealView.invalidate();
        }
    }

    public boolean isRunning() {
        return running;
    }

    public int[] getCurrentPosition() {
        if (params != null) return new int[]{params.x, params.y};
        return null;
    }

    private void applyBackground() {
        if (view == null) return;
        if (WatermarkConfig.bg.enabled) {
            int pad = WatermarkConfig.bg.padding;
            view.setPadding(pad, pad, pad, pad);
        } else {
            view.setPadding(0, 0, 0, 0);
        }
        view.setBgEnabled(WatermarkConfig.bg.enabled);
        view.setBgColor(WatermarkConfig.bg.color);
        view.setBgOffsetX(WatermarkConfig.bg.offsetX);
        view.setBgOffsetY(WatermarkConfig.bg.offsetY);
        view.setBgMargin(WatermarkConfig.bg.margin);
        view.setBgRadius(WatermarkConfig.bg.radius);
    }

    public void updateTouchFlags() {
        if (params == null || view == null || wm == null) return;

        if (WatermarkConfig.touchPassthrough) {
            params.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            view.setOnTouchListener(null);
        } else {
            params.flags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            view.setOnTouchListener(new OverlayDragHandler(params, wm,
                    null,
                    () -> {
                        if (WatermarkConfig.safeArea && view != null && view.getWidth() > 0 && view.getHeight() > 0) {
                            params.x = Math.max(0, Math.min(params.x, getScreenWidth() - view.getWidth()));
                            params.y = Math.max(0, Math.min(params.y, getScreenHeight() - view.getHeight()));
                        }
                        if (params != null) {
                            WatermarkConfig.posX = Math.max(0, Math.min(1, (float) params.x / getScreenWidth()));
                            WatermarkConfig.posY = Math.max(0, Math.min(1, (float) params.y / getScreenHeight()));
                        }
                        if (onPositionUpdate != null) onPositionUpdate.run();
                    }));
        }

        try { wm.updateViewLayout(view, params); } catch (Exception e) { e.printStackTrace(); }
    }

    private int getScreenWidth() {
        android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(metrics);
        return metrics.widthPixels;
    }

    private int getScreenHeight() {
        android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(metrics);
        return metrics.heightPixels;
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

            String text = WatermarkConfig.text;
            if (text == null || text.isEmpty()) return;

            float textSize = WatermarkConfig.size;
            int color = WatermarkConfig.patternColor;
            float spacingH = WatermarkConfig.patternSpacingH;
            float spacingV = WatermarkConfig.patternSpacingV;
            float angle = WatermarkConfig.patternAngle;

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
