package exp.ftxt.features.memory_stats;

import android.graphics.Color;

import exp.ftxt.shared.ui.BackgroundConfig;
import exp.ftxt.shared.ui.ShadowConfig;

public class MemoryConfig {
    public static boolean enabled = false;
    public static float size = 12f;
    public static int color = Color.WHITE;
    public static int labelColor = Color.CYAN;
    public static int separatorColor = Color.GRAY;
    public static ShadowConfig shadow = new ShadowConfig();
    public static BackgroundConfig bg = new BackgroundConfig();
    public static boolean touchPassthrough = true;
    public static boolean safeArea = true;
    public static boolean showJavaHeap = true;
    public static boolean showNativeHeap = true;
    public static boolean showGraphics = true;
    public static boolean showTotal = true;
    public static boolean showOnlyValue = false;
    public static String itemOrder = "java,native,graphics,total";
    public static boolean backgroundMonitor = false;
    public static float posX = 0.05f;
    public static float posY = 0.6f;
    public static float updateInterval = 1f;
}
