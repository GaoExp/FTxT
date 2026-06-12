package exp.ftxt.features.clock_module;

import android.graphics.Color;

import exp.ftxt.shared.ui.BackgroundConfig;
import exp.ftxt.shared.ui.ShadowConfig;

public class ClockConfig {
    public static boolean enabled = false;
    public static float size = 12f;
    public static int color = Color.GREEN;
    public static ShadowConfig shadow = new ShadowConfig();
    public static BackgroundConfig bg = new BackgroundConfig(10);
    public static boolean touchPassthrough = true;
    public static boolean safeArea = true;
    public static float posX = 0.5f;
    public static float posY = 0.05f;
}
