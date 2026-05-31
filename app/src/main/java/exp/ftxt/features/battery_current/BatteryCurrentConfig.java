package exp.ftxt.features.battery_current;

import android.graphics.Color;

import exp.ftxt.shared.ui.ShadowConfig;

public class BatteryCurrentConfig {
    public static boolean enabled = false;
    public static float size = 12f;
    public static int color = Color.GREEN;
    public static ShadowConfig shadow = new ShadowConfig();
    public static boolean touchPassthrough = true;
    public static boolean safeArea = true;
    public static boolean bgEnabled = false;
    public static int bgColor = 0xCC000000;
    public static int bgPadding = 8;
    public static int bgOffsetX = 0;
    public static int bgOffsetY = 0;
    public static int bgMargin = 0;
    public static int bgRadius = 0;
    public static boolean showVoltage = true;
    public static boolean showCurrent = true;
    public static boolean showPower = true;
    public static float posX = 0.75f;
    public static float posY = 0.85f;
}
