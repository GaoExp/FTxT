package exp.ftxt.shared.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.widget.TextView;

public class ShadowTextView extends TextView {

    private ShadowConfig shadowConfig;
    private boolean bgEnabled;
    private int bgColor;
    private int bgOffsetX;
    private int bgOffsetY;
    private int bgMargin = 0;
    private int bgRadius = 0;
    private final Paint bgPaint;

    public ShadowTextView(Context context) {
        super(context);
        bgPaint = new Paint();
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setAntiAlias(true);
    }

    public void setShadowConfig(ShadowConfig config) {
        shadowConfig = config;
        invalidate();
    }

    public void setBgEnabled(boolean enabled) {
        bgEnabled = enabled;
        invalidate();
    }

    public void setBgColor(int color) {
        bgColor = color;
        bgPaint.setColor(color);
        invalidate();
    }

    public void setBgOffsetX(int x) {
        bgOffsetX = x;
        invalidate();
    }

    public void setBgOffsetY(int y) {
        bgOffsetY = y;
        invalidate();
    }

    public void setBgMargin(int margin) {
        bgMargin = margin;
        invalidate();
    }

    public void setBgRadius(int radius) {
        bgRadius = radius;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (bgEnabled) {
            bgPaint.setColor(bgColor);
            canvas.save();
            canvas.translate(bgOffsetX, bgOffsetY);
            
            float left = -bgMargin;
            float top = -bgMargin;
            float right = getWidth() + bgMargin;
            float bottom = getHeight() + bgMargin;
            
            if (bgRadius > 0) {
                canvas.drawRoundRect(left, top, right, bottom, bgRadius, bgRadius, bgPaint);
            } else {
                canvas.drawRect(left, top, right, bottom, bgPaint);
            }
            
            canvas.restore();
        }

        if (shadowConfig != null && shadowConfig.enabled) {
            getPaint().setShadowLayer(shadowConfig.blur, shadowConfig.offsetX,
                    shadowConfig.offsetY, shadowConfig.color);
        }
        super.onDraw(canvas);
        if (shadowConfig != null && shadowConfig.enabled) {
            getPaint().setShadowLayer(0, 0, 0, 0);
        }
    }
}
