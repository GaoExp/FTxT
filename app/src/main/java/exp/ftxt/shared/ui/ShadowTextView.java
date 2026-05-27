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
    private final Paint bgPaint;

    public ShadowTextView(Context context) {
        super(context);
        bgPaint = new Paint();
        bgPaint.setStyle(Paint.Style.FILL);
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

    @Override
    protected void onDraw(Canvas canvas) {
        if (bgEnabled) {
            bgPaint.setColor(bgColor);
            canvas.save();
            canvas.translate(bgOffsetX, bgOffsetY);
            canvas.drawRect(0, 0, getWidth(), getHeight(), bgPaint);
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
