package exp.ftxt.shared.color;

import android.graphics.Color;

/**
 * HSV color math utilities yang diekstrak dari HSVColorPickerView.
 *
 * Dipakai oleh:
 * - HSVColorPickerView → shared/color/HSVColorPickerView.java (drawColorWheel, onTouchEvent, drawSelector)
 * - ColorPickerDialog  → shared/ui/ColorPickerDialog.java (getFinalColor)
 */
public class ColorMath {

    /**
     * Generate 361 warna hue (0–360) untuk SweepGradient color wheel.
     */
    public static int[] generateHueColors() {
        int[] colors = new int[361];
        for (int i = 0; i < 361; i++) {
            colors[i] = Color.HSVToColor(new float[]{i, 1f, 1f});
        }
        return colors;
    }

    /**
     * Hitung sudut (derajat) dari titik pusat (cx,cy) ke titik (x,y).
     * Hasil: 0–360 derajat.
     */
    public static float calculateAngle(float cx, float cy, float x, float y) {
        float dx = x - cx;
        float dy = y - cy;
        float angle = (float) Math.toDegrees(Math.atan2(dy, dx));
        if (angle < 0) angle += 360;
        return angle;
    }

    /**
     * Hitung jarak Euclidean antara dua titik.
     */
    public static float calculateDistance(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Dapatkan posisi (x, y) selector pada color wheel berdasarkan hue dan saturation.
     *
     * @param cx         pusat X wheel
     * @param cy         pusat Y wheel
     * @param radius     radius wheel
     * @param hue        hue dalam derajat (0–360)
     * @param saturation saturation (0–1)
     * @return float[]{x, y}
     */
    public static float[] getSelectorPosition(float cx, float cy, float radius,
                                               float hue, float saturation) {
        float angle = (float) Math.toRadians(hue);
        float r = radius * saturation;
        float x = cx + r * (float) Math.cos(angle);
        float y = cy + r * (float) Math.sin(angle);
        return new float[]{x, y};
    }
}
