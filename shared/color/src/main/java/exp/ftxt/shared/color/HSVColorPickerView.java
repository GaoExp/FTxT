package exp.ftxt.shared.color;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class HSVColorPickerView extends View {

    private Paint colorWheelPaint;
    private Paint crosshairDarkPaint;
    private Paint crosshairLightPaint;
    private Paint centerDotPaint;

    private float wheelRadius;
    private float wheelCenterX;
    private float wheelCenterY;

    private Shader wheelShader;
    private Shader saturationShader;

    private float hue = 0f;
    private float saturation = 1f;

    private OnColorChangeListener listener;

    public interface OnColorChangeListener {
        void onColorChange(int color);
    }

    public HSVColorPickerView(Context context) {
        super(context);
        init();
    }

    public HSVColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HSVColorPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        colorWheelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        crosshairDarkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        crosshairDarkPaint.setStyle(Paint.Style.STROKE);
        crosshairDarkPaint.setStrokeWidth(4f);
        crosshairDarkPaint.setColor(Color.BLACK);

        crosshairLightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        crosshairLightPaint.setStyle(Paint.Style.STROKE);
        crosshairLightPaint.setStrokeWidth(2f);
        crosshairLightPaint.setColor(Color.WHITE);

        centerDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerDotPaint.setStyle(Paint.Style.FILL);
        centerDotPaint.setColor(Color.WHITE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        wheelCenterX = w / 2f;
        wheelCenterY = h / 2f;
        wheelRadius = Math.min(wheelCenterX, wheelCenterY) - 20f;
        wheelShader = null;
        saturationShader = null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawColorWheel(canvas);
        drawSaturationOverlay(canvas);
        drawCrosshair(canvas);
    }

    private void drawColorWheel(Canvas canvas) {
        if (wheelShader == null) {
            int[] colors = ColorMath.generateHueColors();
            wheelShader = new SweepGradient(wheelCenterX, wheelCenterY, colors, null);
        }
        colorWheelPaint.setShader(wheelShader);
        canvas.drawCircle(wheelCenterX, wheelCenterY, wheelRadius, colorWheelPaint);
    }

    private void drawSaturationOverlay(Canvas canvas) {
        if (saturationShader == null) {
            int[] colors = new int[]{
                    0xFFFFFFFF,
                    0x00FFFFFF
            };
            saturationShader = new android.graphics.RadialGradient(
                    wheelCenterX, wheelCenterY, wheelRadius,
                    colors, new float[]{0f, 1f},
                    android.graphics.Shader.TileMode.CLAMP);
        }
        colorWheelPaint.setShader(saturationShader);
        canvas.drawCircle(wheelCenterX, wheelCenterY, wheelRadius, colorWheelPaint);
    }

    private void drawCrosshair(Canvas canvas) {
        float[] pos = ColorMath.getSelectorPosition(
                wheelCenterX, wheelCenterY, wheelRadius, hue, saturation);
        float cx = pos[0];
        float cy = pos[1];
        float crossSize = Math.min(wheelRadius * 0.15f, 24f);
        float ringRadius = Math.min(wheelRadius * 0.08f, 14f);

        crosshairDarkPaint.setStrokeWidth(5f);
        canvas.drawLine(cx - crossSize, cy, cx + crossSize, cy, crosshairDarkPaint);
        canvas.drawLine(cx, cy - crossSize, cx, cy + crossSize, crosshairDarkPaint);
        canvas.drawCircle(cx, cy, ringRadius, crosshairDarkPaint);

        crosshairLightPaint.setStrokeWidth(3f);
        canvas.drawLine(cx - crossSize, cy, cx + crossSize, cy, crosshairLightPaint);
        canvas.drawLine(cx, cy - crossSize, cx, cy + crossSize, crosshairLightPaint);
        canvas.drawCircle(cx, cy, ringRadius, crosshairLightPaint);

        canvas.drawCircle(cx, cy, 3f, centerDotPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        float x = event.getX();
        float y = event.getY();

        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
            if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(true);
        }

        float distance = ColorMath.calculateDistance(wheelCenterX, wheelCenterY, x, y);
        hue = ColorMath.calculateAngle(wheelCenterX, wheelCenterY, x, y);
        saturation = Math.min(distance / wheelRadius, 1f);

        invalidate();

        if (listener != null) {
            int color = Color.HSVToColor(new float[]{hue, saturation, 1f});
            listener.onColorChange(color);
        }

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(false);
        }

        return true;
    }

    public void setOnColorChangeListener(OnColorChangeListener listener) {
        this.listener = listener;
    }

    public int getCurrentColor() {
        return Color.HSVToColor(new float[]{hue, saturation, 1f});
    }

    public void setColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hue = hsv[0];
        saturation = hsv[1];
        invalidate();
    }
}
