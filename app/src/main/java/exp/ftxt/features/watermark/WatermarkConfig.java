package exp.ftxt.features.watermark;

import android.graphics.Color;

import exp.ftxt.shared.ui.ShadowConfig;

public class WatermarkConfig {
    public static boolean enabled = false;
    public static String text = "Watermark";
    public static float size = 24f;
    public static int color = 0x55FFFFFF;
    public static ShadowConfig shadow = new ShadowConfig();
    public static boolean touchPassthrough = true;
    public static boolean safeArea = true;
    public static boolean bgEnabled = false;
    public static int bgColor = 0xCC000000;
    public static int bgPadding = 10;
    public static int bgOffsetX = 0;
    public static int bgOffsetY = 0;
    public static int bgMargin = 0;
    public static int bgRadius = 0;
    public static float posX = 0.5f;
    public static float posY = 0.5f;

    // Seal pattern mode
    public static boolean patternEnabled = false;
    public static float patternSpacingH = 180f;
    public static float patternSpacingV = 220f;
    public static float patternAngle = -30f;
    public static int patternColor = 0x22FFFFFF;
}
