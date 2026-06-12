package exp.ftxt.features.battery_percentage;

import android.graphics.Color;

import exp.ftxt.shared.ui.BackgroundConfig;
import exp.ftxt.shared.ui.ShadowConfig;

public class BatteryPercentageConfig {
    public static boolean enabled = false;
    public static float size = 12f;
    public static int color = Color.GREEN;
    public static ShadowConfig shadow = new ShadowConfig();
    public static BackgroundConfig bg = new BackgroundConfig();
    public static int labelColor = Color.CYAN;
    public static boolean touchPassthrough = true;
    public static float posX = 0.5f;
    public static float posY = 0.5f;
}
