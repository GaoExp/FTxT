package exp.ftxt.shared.ui;

import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;

/**
 * Shared shadow utility untuk overlay modules.
 *
 * Menggantikan applyShadowToView() di FpsModule dan updateShadow() di TextModule.
 *
 * Dipakai oleh:
 * - TextModule → modules/text/TextModule.java (updateShadow, private updateShadow)
 * - FpsModule  → modules/fps/FpsModule.java (applyShadowToView)
 */
public class OverlayShadow {

    /**
     * Terapkan atau hapus shadow (background semi-transparan + elevation) pada overlay view.
     *
     * @param view    TextView overlay
     * @param params  LayoutParams overlay
     * @param wm      WindowManager untuk update layout
     * @param enabled true untuk mengaktifkan shadow
     * @param elevation  elevation dalam dp (8f untuk TextModule, 4f untuk FpsModule)
     */
    public static void apply(View view, WindowManager.LayoutParams params, WindowManager wm,
                             boolean enabled, float elevation) {
        if (view == null) return;
        if (enabled) {
            view.setBackgroundColor(0x88000000);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                view.setElevation(elevation);
        } else {
            view.setBackgroundColor(Color.TRANSPARENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                view.setElevation(0f);
        }
        if (wm != null && params != null) {
            try { wm.updateViewLayout(view, params); } catch (Exception e) { e.printStackTrace(); }
        }
    }
}
