package exp.ftxt.features.battery_bar;

import android.graphics.Color;

public class BatteryBarConfig {
    public static final int SCHEME_NONE = 0;
    public static final int SCHEME_CLASSIC = 1;
    public static final int SCHEME_HUE = 2;

    public static boolean enabled = false;
    public static boolean quickMode = true;
    public static String quickSide = "top";
    public static boolean horizontal = true;
    public static boolean invert = false;
    public static float length = 0.5f;
    public static int thickness = 8;
    public static int color = Color.GREEN;
    public static int colorScheme = SCHEME_NONE;
    public static int lowColor = Color.YELLOW;
    public static int lowThreshold = 40;
    public static boolean showEmptyStrip = true;
    public static int emptyColor = 0x66000000;
    public static int radius = 8;
    public static float updateInterval = 1f;
    public static int fadeSpeed = 1800;
    public static boolean fadeEnabled = false;
    public static boolean shineEnabled = false;
    public static int shineSpeed = 1800;
    public static int shineWidth = 25;
    public static boolean waveEnabled = false;
    public static int waveSpeed = 1000;
    public static int waveAmplitude = 60;
    public static boolean chargeWaveEnabled = false;
    public static int chargeWaveSpeed = 1000;
    public static int chargeWaveAmplitude = 60;
    public static boolean touchPassthrough = true;
    public static boolean safeArea = true;
    public static float posX = 0.05f;
    public static float posY = 0.9f;

    public static boolean isAutoColor() {
        return colorScheme != SCHEME_NONE;
    }
}
