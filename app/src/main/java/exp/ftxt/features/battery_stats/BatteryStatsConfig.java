package exp.ftxt.features.battery_stats;

import android.graphics.Color;

import exp.ftxt.shared.ui.BackgroundConfig;
import exp.ftxt.shared.ui.ShadowConfig;

public class BatteryStatsConfig {
    public static boolean enabled = false;
    public static float size = 12f;
    public static int color = Color.GREEN;
    public static int labelColor = Color.CYAN;
    public static int separatorColor = Color.GRAY;
    public static ShadowConfig shadow = new ShadowConfig();
    public static BackgroundConfig bg = new BackgroundConfig();
    public static boolean touchPassthrough = true;
    public static boolean safeArea = true;
    public static boolean showTemperature = true;
    public static boolean showPercentage = false;
    public static boolean showVoltage = true;
    public static boolean showCurrent = true;
    public static boolean showPower = true;
    public static boolean showOnlyValue = false;
    public static String itemOrder = "temp,pct,volt,cur,power";
    public static float posX = 0.05f;
    public static float posY = 0.8f;
    public static float updateInterval = 5f;
}
