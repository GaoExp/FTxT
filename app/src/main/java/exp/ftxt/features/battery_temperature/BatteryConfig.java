package exp.ftxt.features.battery_temperature;

import android.graphics.Color;

import exp.ftxt.shared.ui.BackgroundConfig;
import exp.ftxt.shared.ui.ShadowConfig;

public class BatteryConfig {
    public static boolean enabled = false;
    public static float size = 12f;
    public static int color = Color.GREEN;
    public static ShadowConfig shadow = new ShadowConfig();
    public static BackgroundConfig bg = new BackgroundConfig();
    public static boolean touchPassthrough = true;
    public static boolean safeArea = true;
    public static boolean showOnlyValue = false;
    public static int labelColor = Color.CYAN;
    public static boolean showTemperature = true;
    public static boolean showPercentage = false;
    public static float posX = 0.05f;
    public static float posY = 0.8f;
    public static float updateInterval = 5f; // detik: 0.2, 0.5, 0.75, 1-10
}
