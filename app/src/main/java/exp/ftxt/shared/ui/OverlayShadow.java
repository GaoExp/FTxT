package exp.ftxt.shared.ui;

import android.os.Build;
import android.view.View;
import android.view.WindowManager;

public class OverlayShadow {

    public static void apply(View view, WindowManager.LayoutParams params, WindowManager wm,
                             ShadowConfig config, float elevation) {
        if (view == null) return;

        if (config.enabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                view.setElevation(elevation);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                view.setElevation(0f);
        }

        if (wm != null && params != null && view.isAttachedToWindow()) {
            try { wm.updateViewLayout(view, params); } catch (Exception e) { e.printStackTrace(); }
        }
    }
}
