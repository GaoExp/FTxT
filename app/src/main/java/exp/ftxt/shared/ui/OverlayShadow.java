package exp.ftxt.shared.ui;

import android.graphics.Color;
import android.os.Build;
import android.view.WindowManager;
import android.widget.TextView;

public class OverlayShadow {

    public static void apply(TextView view, WindowManager.LayoutParams params, WindowManager wm,
                             ShadowConfig config, float elevation) {
        if (view == null) return;

        if (config.enabled) {
            view.setBackgroundColor(0x88000000);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                view.setElevation(elevation);
        } else {
            view.setBackgroundColor(Color.TRANSPARENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                view.setElevation(0f);
        }

        if (wm != null && params != null && view.isAttachedToWindow()) {
            try { wm.updateViewLayout(view, params); } catch (Exception e) { e.printStackTrace(); }
        }
    }
}
