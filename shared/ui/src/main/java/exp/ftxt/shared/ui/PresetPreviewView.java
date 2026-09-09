package exp.ftxt.shared.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class PresetPreviewView extends View {

    private float posX = 0.5f;
    private float posY = 0.5f;
    private boolean editMode = false;
    private OnPositionChangedListener listener;

    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint crosshairPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private static final float DOT_RADIUS_DP = 6f;
    private static final float CROSSHAIR_SIZE_DP = 12f;
    private float dotRadius;
    private float crosshairSize;

    public PresetPreviewView(Context context) {
        super(context);
        init();
    }

    public PresetPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        float density = getResources().getDisplayMetrics().density;
        dotRadius = DOT_RADIUS_DP * density;
        crosshairSize = CROSSHAIR_SIZE_DP * density;

        bgPaint.setColor(Color.parseColor("#1A1A2E"));
        bgPaint.setStyle(Paint.Style.FILL);

        gridPaint.setColor(Color.parseColor("#333355"));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(1f);

        dotPaint.setColor(Color.parseColor("#00D2FF"));
        dotPaint.setStyle(Paint.Style.FILL);

        crosshairPaint.setColor(Color.WHITE);
        crosshairPaint.setStyle(Paint.Style.STROKE);
        crosshairPaint.setStrokeWidth(2f);
    }

    public void setPosition(float x, float y) {
        this.posX = Math.max(0, Math.min(1, x));
        this.posY = Math.max(0, Math.min(1, y));
        invalidate();
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
        invalidate();
    }

    public void setOnPositionChangedListener(OnPositionChangedListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth();
        float h = getHeight();
        float pad = 4f;
        float rectLeft = pad;
        float rectTop = pad;
        float rectRight = w - pad;
        float rectBottom = h - pad;
        float rectW = rectRight - rectLeft;
        float rectH = rectBottom - rectTop;

        canvas.drawRoundRect(rectLeft, rectTop, rectRight, rectBottom, 8f, 8f, bgPaint);

        for (int i = 1; i < 3; i++) {
            float gridX = rectLeft + (rectW / 3f) * i;
            canvas.drawLine(gridX, rectTop, gridX, rectBottom, gridPaint);
            float gridY = rectTop + (rectH / 3f) * i;
            canvas.drawLine(rectLeft, gridY, rectRight, gridY, gridPaint);
        }

        float dotX = rectLeft + posX * rectW;
        float dotY = rectTop + posY * rectH;

        canvas.drawCircle(dotX, dotY, dotRadius + 4f, dotPaint);
        dotPaint.setAlpha(60);
        canvas.drawCircle(dotX, dotY, dotRadius + 10f, dotPaint);
        dotPaint.setAlpha(255);

        canvas.drawLine(dotX - crosshairSize, dotY, dotX + crosshairSize, dotY, crosshairPaint);
        canvas.drawLine(dotX, dotY - crosshairSize, dotX, dotY + crosshairSize, crosshairPaint);

        canvas.drawCircle(dotX, dotY, dotRadius, dotPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!editMode) return false;

        float w = getWidth();
        float h = getHeight();
        float pad = 4f;
        float rectW = w - pad * 2;
        float rectH = h - pad * 2;

        float x = (event.getX() - pad) / rectW;
        float y = (event.getY() - pad) / rectH;
        x = Math.max(0, Math.min(1, x));
        y = Math.max(0, Math.min(1, y));

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_DOWN:
                posX = x;
                posY = y;
                invalidate();
                if (listener != null) listener.onPositionChanged(x, y);
                return true;
        }
        return false;
    }

    public interface OnPositionChangedListener {
        void onPositionChanged(float x, float y);
    }
}
