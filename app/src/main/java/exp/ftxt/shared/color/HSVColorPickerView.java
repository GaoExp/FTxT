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
    private Paint selectorPaint;
    private Paint saturationGradientPaint;

    private float wheelRadius;
    private float wheelCenterX;
    private float wheelCenterY;
    private float selectorRadius = 15f;

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

    public HSVColorPickerView(
            Context context,
            AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HSVColorPickerView(
            Context context,
            AttributeSet attrs,
            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        colorWheelPaint = new Paint(
                Paint.ANTI_ALIAS_FLAG);
        selectorPaint = new Paint(
                Paint.ANTI_ALIAS_FLAG);
        saturationGradientPaint = new Paint(
                Paint.ANTI_ALIAS_FLAG);

        selectorPaint.setStyle(
                Paint.Style.STROKE);
        selectorPaint.setStrokeWidth(3f);
        selectorPaint.setColor(
                Color.WHITE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        wheelCenterX = getWidth() / 2f;
        wheelCenterY = getHeight() / 2f;
        wheelRadius = Math.min(
                wheelCenterX,
                wheelCenterY) - 20f;

        drawColorWheel(canvas);
        drawSaturationGradient(canvas);
        drawSelector(canvas);
    }

    private void drawColorWheel(
            Canvas canvas) {
        int[] colors = new int[361];
        for (int i = 0; i < 361; i++) {
            colors[i] = Color.HSVToColor(
                    new float[]{
                            i,
                            1f,
                            1f
                    }
            );
        }

        Shader shader = new SweepGradient(
                wheelCenterX,
                wheelCenterY,
                colors,
                null
        );

        colorWheelPaint.setShader(shader);
        canvas.drawCircle(
                wheelCenterX,
                wheelCenterY,
                wheelRadius,
                colorWheelPaint
        );
    }

    private void drawSaturationGradient(
            Canvas canvas) {
        float radius = wheelRadius * 0.7f;
        int[] colors = new int[]{
                Color.WHITE,
                Color.HSVToColor(
                        new float[]{
                                hue,
                                1f,
                                1f
                        }
                )
        };

        Shader shader = new android.graphics
                .RadialGradient(
                wheelCenterX,
                wheelCenterY,
                radius,
                colors,
                new float[]{0f, 1f},
                android.graphics.Shader
                .TileMode.CLAMP
        );

        saturationGradientPaint
        .setShader(shader);
        canvas.drawCircle(
                wheelCenterX,
                wheelCenterY,
                radius,
                saturationGradientPaint
        );
    }

    private void drawSelector(
            Canvas canvas) {
        float angle = (float) Math.toRadians(
                hue);
        float radius = wheelRadius *
                saturation * 0.7f;

        float selectorX =
                wheelCenterX +
                (float)(radius *
                Math.cos(angle));

        float selectorY =
                wheelCenterY +
                (float)(radius *
                Math.sin(angle));

        canvas.drawCircle(
                selectorX,
                selectorY,
                selectorRadius,
                selectorPaint
        );
    }

    @Override
    public boolean onTouchEvent(
            MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        float dx = x - wheelCenterX;
        float dy = y - wheelCenterY;
        float distance = (float) Math.sqrt(
                dx * dx + dy * dy);

        float maxRadius = wheelRadius * 0.7f;

        if (distance > maxRadius) {
            float angle = (float) Math.atan2(
                    dy,
                    dx);
            hue = (float) Math.toDegrees(
                    angle);
            if (hue < 0) {
                hue += 360;
            }
            saturation = 1f;
        } else {
            hue = (float) Math.toDegrees(
                    Math.atan2(dy, dx));
            if (hue < 0) {
                hue += 360;
            }
            saturation = distance / maxRadius;
        }

        invalidate();

        if (listener != null) {
            int color = Color.HSVToColor(
                    new float[]{
                            hue,
                            saturation,
                            1f
                    }
            );
            listener.onColorChange(color);
        }

        return true;
    }

    public void setOnColorChangeListener(
            OnColorChangeListener listener) {
        this.listener = listener;
    }

    public int getCurrentColor() {
        return Color.HSVToColor(
                new float[]{
                        hue,
                        saturation,
                        1f
                }
        );
    }

    public void setColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hue = hsv[0];
        saturation = hsv[1];
        invalidate();
    }
}
