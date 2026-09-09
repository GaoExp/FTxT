package exp.ftxt.shared.color;

import android.graphics.Color;

public class ColorMath {

    private static int[] hueColors;

    public static int[] generateHueColors() {
        if (hueColors == null) {
            hueColors = new int[361];
            for (int i = 0; i < 361; i++) {
                hueColors[i] = Color.HSVToColor(new float[]{i, 1f, 1f});
            }
        }
        return hueColors;
    }

    public static float calculateAngle(float cx, float cy, float x, float y) {
        float dx = x - cx;
        float dy = y - cy;
        float angle = (float) Math.toDegrees(Math.atan2(dy, dx));
        if (angle < 0) angle += 360;
        return angle;
    }

    public static float calculateDistance(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public static float[] getSelectorPosition(float cx, float cy, float radius,
                                               float hue, float saturation) {
        float angle = (float) Math.toRadians(hue);
        float r = radius * saturation;
        float x = cx + r * (float) Math.cos(angle);
        float y = cy + r * (float) Math.sin(angle);
        return new float[]{x, y};
    }
}
