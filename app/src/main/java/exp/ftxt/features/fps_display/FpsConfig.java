package exp.ftxt.features.fps_display;

import android.graphics.Color;

import exp.ftxt.shared.config.BackgroundConfig;
import exp.ftxt.shared.config.ShadowConfig;

public class FpsConfig {
    public static boolean enabled = false;
    public static float size = 12f;
    public static int color = Color.GREEN;
    public static ShadowConfig shadow = new ShadowConfig();
    public static BackgroundConfig bg = new BackgroundConfig(10);
    public static boolean touchPassthrough = true;
    public static boolean safeArea = true;
    public static boolean showOnlyValue = false;
    public static int labelColor = Color.CYAN;
    public static float posX = 0.05f;
    public static float posY = 0.05f;
    public static float updateInterval = 1f;
}
