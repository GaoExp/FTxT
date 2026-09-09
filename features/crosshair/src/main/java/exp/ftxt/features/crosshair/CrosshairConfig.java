package exp.ftxt.features.crosshair;

import android.graphics.Color;

import exp.ftxt.shared.config.BackgroundConfig;

public class CrosshairConfig {
    public static boolean enabled = false;
    public static int styleIndex = 1;
    public static float size = 32f;
    public static int opacity = 100;
    public static boolean touchPassthrough = true;
    public static boolean safeArea = true;
    public static float posX = 0.5f;
    public static float posY = 0.5f;
    public static boolean colorEnabled = true;
    public static int color = Color.WHITE;
    public static float rotation = 0f;
    public static BackgroundConfig bg = new BackgroundConfig();
}
