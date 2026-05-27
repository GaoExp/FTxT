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

/**
 * Custom View untuk circular HSV color picker wheel.
 *
 * Menggambar color wheel (hue sweep), saturation gradient radial,
 * dan selector circle. Touch event menghitung hue & saturation
 * dari posisi sentuhan.
 *
 * Menggunakan:
 * - ColorMath → shared/color/ColorMath.java (utilitas perhitungan warna/posisi)
 *
 * Dipanggil oleh:
 * - ColorPickerDialog → shared/ui/ColorPickerDialog.java (show method)
 *
 * Terkait dengan:
 * - ColorMath  → shared/color/ColorMath.java (utilitas HSV)
 */
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
        selectorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        saturationGradientPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        selectorPaint.setStyle(Paint.Style.STROKE);
        selectorPaint.setStrokeWidth(3f);
        selectorPaint.setColor(Color.WHITE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        wheelCenterX = getWidth() / 2f;
        wheelCenterY = getHeight() / 2f;
        wheelRadius = Math.min(wheelCenterX, wheelCenterY) - 20f;

        drawColorWheel(canvas);
        drawSaturationGradient(canvas);
        drawSelector(canvas);
    }

    // Draw ring warna (hue sweep 0–360)
    // Menggunakan ColorMath.generateHueColors() untuk array warna
    // Lihat: ColorMath → shared/color/ColorMath.java
    private void drawColorWheel(Canvas canvas) {
        int[] colors = ColorMath.generateHueColors();

        Shader shader = new SweepGradient(wheelCenterX, wheelCenterY, colors, null);
        colorWheelPaint.setShader(shader);
        canvas.drawCircle(wheelCenterX, wheelCenterY, wheelRadius, colorWheelPaint);
    }

    // Draw gradient saturation radial di dalam wheel
    // Dari putih (pusat) ke warna hue murni (tepi)
    private void drawSaturationGradient(Canvas canvas) {
        float radius = wheelRadius * 0.7f;
        int[] colors = new int[]{
                Color.WHITE,
                Color.HSVToColor(new float[]{hue, 1f, 1f})
        };

        Shader shader = new android.graphics.RadialGradient(
                wheelCenterX, wheelCenterY, radius,
                colors, new float[]{0f, 1f},
                android.graphics.Shader.TileMode.CLAMP);

        saturationGradientPaint.setShader(shader);
        canvas.drawCircle(wheelCenterX, wheelCenterY, radius, saturationGradientPaint);
    }

    // Draw selector circle pada posisi hue + saturation
    // Menggunakan ColorMath.getSelectorPosition()
    // Lihat: ColorMath → shared/color/ColorMath.java
    private void drawSelector(Canvas canvas) {
        float[] pos = ColorMath.getSelectorPosition(
                wheelCenterX, wheelCenterY, wheelRadius * 0.7f, hue, saturation);

        canvas.drawCircle(pos[0], pos[1], selectorRadius, selectorPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        float distance = ColorMath.calculateDistance(wheelCenterX, wheelCenterY, x, y);
        float maxRadius = wheelRadius * 0.7f;

        if (distance > maxRadius) {
            // Sentuhan di luar area saturation → hanya set hue
            hue = ColorMath.calculateAngle(wheelCenterX, wheelCenterY, x, y);
            saturation = 1f;
        } else {
            // Sentuhan di dalam saturation area → hue + saturation
            hue = ColorMath.calculateAngle(wheelCenterX, wheelCenterY, x, y);
            saturation = distance / maxRadius;
        }

        invalidate();

        if (listener != null) {
            int color = Color.HSVToColor(new float[]{hue, saturation, 1f});
            listener.onColorChange(color);
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
