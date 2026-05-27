package exp.ftxt.modules.text;

import android.graphics.Color;

/**
 * Konfigurasi statis untuk modul Floating Text.
 *
 * Nilai-nilai ini diubah oleh:
 * - TextPanelController → ui/TextPanelController.java (listener panel)
 * - TextModule          → modules/text/TextModule.java (updateShadow, dll)
 *
 * Dibaca oleh:
 * - TextModule          → modules/text/TextModule.java (createOverlay)
 * - MainActivity        → MainActivity.java (loadConfigs)
 */
public class TextConfig {
    public static String text = "FTxT AKTIF";
    public static float size = 20f;
    public static int color = Color.WHITE;
    public static boolean touchPassthrough = false;
    public static boolean shadow = false;
}
