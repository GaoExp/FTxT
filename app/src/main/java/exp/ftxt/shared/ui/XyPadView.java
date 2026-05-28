package exp.ftxt.shared.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class XyPadView extends View {

    private float posX = 0.5f;
    private float posY = 0.5f;
    private OnPositionChangeListener listener;

    private final Paint bgPaint;
    private final Paint dotPaint;
    private final Paint borderPaint;

    private static final float DOT_RADIUS = 28f;
    private static final float PADDING = DOT_RADIUS + 12f;
    private static final int BG_COLOR = 0x14000000;
    private static final int BORDER_COLOR = 0x26000000;
    private static final int DOT_COLOR = 0xFF2196F3;

    public XyPadView(Context context) {
        super(context);
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(BG_COLOR);
        bgPaint.setStyle(Paint.Style.FILL);

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(BORDER_COLOR);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2f);

        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setColor(DOT_COLOR);
        dotPaint.setStyle(Paint.Style.FILL);
    }

    public XyPadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(BG_COLOR);
        bgPaint.setStyle(Paint.Style.FILL);

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(BORDER_COLOR);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2f);

        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setColor(DOT_COLOR);
        dotPaint.setStyle(Paint.Style.FILL);
    }

    public void setPosition(float x, float y) {
        posX = Math.max(0, Math.min(1, x));
        posY = Math.max(0, Math.min(1, y));
        invalidate();
    }

    public void setOnPositionChangeListener(OnPositionChangeListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth();
        float h = getHeight();

        canvas.drawRoundRect(PADDING, PADDING, w - PADDING, h - PADDING, 12f, 12f, bgPaint);
        canvas.drawRoundRect(PADDING, PADDING, w - PADDING, h - PADDING, 12f, 12f, borderPaint);

        float areaW = w - 2 * PADDING;
        float areaH = h - 2 * PADDING;
        float dotX = PADDING + posX * areaW;
        float dotY = PADDING + posY * areaH;

        canvas.drawCircle(dotX, dotY, DOT_RADIUS, dotPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float w = getWidth();
        float h = getHeight();
        float areaW = w - 2 * PADDING;
        float areaH = h - 2 * PADDING;

        float x = (event.getX() - PADDING) / areaW;
        float y = (event.getY() - PADDING) / areaH;
        x = Math.max(0, Math.min(1, x));
        y = Math.max(0, Math.min(1, y));

        if (event.getAction() == MotionEvent.ACTION_DOWN
                || event.getAction() == MotionEvent.ACTION_MOVE) {
            setPosition(x, y);
            if (listener != null) {
                listener.onPositionChanged(posX, posY);
            }
        }
        return true;
    }

    public interface OnPositionChangeListener {
        void onPositionChanged(float x, float y);
    }
}
