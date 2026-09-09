package exp.ftxt.shared.color;

import android.graphics.Color;

/**
 * Palet warna baterai bersama.
 * {@link #hueColor(float)} adalah SATU-SATUNYA tempat rumus gradien hue
 * (skema Hue Gradien Battery Strip) — dipakai BatteryBarView & BatteryRingView.
 */
public final class BatteryColors {

    private BatteryColors() {}

    public static int hueColor(float percent) {
        float hue, sat;
        if (percent <= 20f) {
            hue = 1f;
            sat = 0.7f;
        } else if (percent <= 50f) {
            float t = (percent - 21f) / 29f;
            hue = 2f + 98f * t;
            sat = 0.7f;
        } else {
            float t = (percent - 51f) / 49f;
            hue = 102f + 158f * t;
            sat = 0.71f + 0.29f * t;
        }
        return Color.HSVToColor(255, new float[]{hue, sat, 1f});
    }
}
