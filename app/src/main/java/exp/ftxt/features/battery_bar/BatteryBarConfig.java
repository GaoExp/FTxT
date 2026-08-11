package exp.ftxt.features.battery_bar;

import android.graphics.Color;

public class BatteryBarConfig {
    public static boolean enabled = false;
    public static boolean quickMode = true;
    public static String quickSide = "top";
    public static boolean horizontal = true;
    public static float length = 0.5f;
    public static int thickness = 8;
    public static int color = Color.GREEN;
    public static boolean autoColor = false;
    public static int lowColor = Color.YELLOW;
    public static int lowThreshold = 40;
    public static boolean showEmptyStrip = true;
    public static int emptyColor = 0x66000000;
    public static int radius = 8;
    public static float updateInterval = 1f;
    public static int fadeSpeed = 5;
    public static boolean touchPassthrough = true;
    public static boolean safeArea = true;
    public static float posX = 0.05f;
    public static float posY = 0.9f;
}
