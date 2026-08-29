package exp.ftxt.shared.ui;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import exp.ftxt.R;

/**
 * Tooltip info kustom yang tampil melayang di dekat ikon ℹ️ (bukan dialog tengah layar).
 * Bertema kartu Monitor (terang/gelap otomatis), rounded, dengan strip aksen pada judul.
 */
public class InfoTooltip {

    private final PopupWindow popup;

    public InfoTooltip(Activity activity, View anchor, String title, String body) {
        View content = LayoutInflater.from(activity).inflate(R.layout.tooltip_info, null);
        ((TextView) content.findViewById(R.id.tooltipTitle)).setText(title);
        ((TextView) content.findViewById(R.id.tooltipBody)).setText(body);

        popup = new PopupWindow(content,
                dp(activity, 280), ViewGroup.LayoutParams.WRAP_CONTENT);
        popup.setTouchable(true);
        popup.setFocusable(true);
        popup.setOutsideTouchable(true);
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setAnimationStyle(R.style.TooltipFadeAnimation);

        popup.setOnDismissListener(() -> {});
        popup.showAsDropDown(anchor, 0, dp(activity, 6), Gravity.NO_GRAVITY);
    }

    public static void show(Activity activity, View anchor, String title, String body) {
        new InfoTooltip(activity, anchor, title, body);
    }

    private static int dp(Activity activity, int value) {
        return (int) (value * activity.getResources().getDisplayMetrics().density);
    }
}
