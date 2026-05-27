package exp.ftxt.modules.fps;

import android.graphics.Color;

/**
 * Konfigurasi statis untuk modul FPS Display.
 *
 * Nilai-nilai ini diubah oleh:
 * - FpsPanelController → ui/FpsPanelController.java (listener panel)
 * - FpsModule          → modules/fps/FpsModule.java (updateSize, updateShadow, dll)
 *
 * Dibaca oleh:
 * - FpsModule          → modules/fps/FpsModule.java (start, updateTouchFlags)
 * - MainActivity       → MainActivity.java (loadConfigs)
 * - FloatingService    → core/FloatingService.java (start FPS jika enabled)
 */
public class FpsConfig {
    public static boolean enabled = false;
    public static float size = 14f;
    public static int color = Color.WHITE;
    public static boolean shadow = false;
    public static boolean touchPassthrough = false;
}
