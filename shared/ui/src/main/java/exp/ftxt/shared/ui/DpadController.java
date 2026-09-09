package exp.ftxt.shared.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

public class DpadController {

    private final Handler repeatHandler = new Handler();
    private Runnable repeatRunnable;
    private OnDpadActionListener listener;
    private final int screenWidth;
    private final int screenHeight;
    private final TextView centerView;

    private static final int REPEAT_INTERVAL = 100;
    private static final int MIN_STEP = 1;
    private static final int MAX_STEP = 20;
    private static final int VISIBLE_ITEMS = 5;

    private static int stepPx = 2;

    public DpadController(View btnUp, View btnDown, View btnLeft, View btnRight,
                          View btnMinus, View btnPlus,
                          View centerView, int screenWidth, int screenHeight,
                          OnDpadActionListener listener) {
        this.listener = listener;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.centerView = (centerView instanceof TextView) ? (TextView) centerView : null;

        setupButton(btnUp, 0, -1);
        setupButton(btnDown, 0, 1);
        setupButton(btnLeft, -1, 0);
        setupButton(btnRight, 1, 0);

        if (btnMinus != null) {
            btnMinus.setOnClickListener(v -> {
                stepPx = Math.max(MIN_STEP, stepPx - 1);
                updateCenterDisplay();
            });
        }
        if (btnPlus != null) {
            btnPlus.setOnClickListener(v -> {
                stepPx = Math.min(MAX_STEP, stepPx + 1);
                updateCenterDisplay();
            });
        }

        setupCenterTap();
        updateCenterDisplay();
    }

    private void setupButton(View button, int dirX, int dirY) {
        button.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (listener != null) {
                        listener.onDpadAction(
                                (dirX * stepPx) / (float) screenWidth,
                                (dirY * stepPx) / (float) screenHeight
                        );
                    }
                    startRepeat(dirX, dirY);
                    v.setPressed(true);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    stopRepeat();
                    v.setPressed(false);
                    return true;
            }
            return false;
        });
    }

    private void startRepeat(int dirX, int dirY) {
        stopRepeat();
        repeatRunnable = () -> {
            if (listener != null) {
                listener.onDpadAction(
                        (dirX * stepPx) / (float) screenWidth,
                        (dirY * stepPx) / (float) screenHeight
                );
            }
            repeatHandler.postDelayed(repeatRunnable, REPEAT_INTERVAL);
        };
        repeatHandler.postDelayed(repeatRunnable, REPEAT_INTERVAL);
    }

    private void setupCenterTap() {
        if (centerView == null) return;
        centerView.setOnClickListener(v -> showIntervalPopup(v));
    }

    private void showIntervalPopup(View anchor) {
        Context ctx = anchor.getContext();
        String[] items = new String[MAX_STEP];
        for (int i = 0; i < MAX_STEP; i++) {
            items[i] = (i + 1) + " px";
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(ctx,
                android.R.layout.simple_list_item_1, items);

        ListView listView = new ListView(ctx);
        listView.setAdapter(adapter);
        listView.setDividerHeight(0);
        listView.setDivider(null);

        int selected = stepPx - 1;
        listView.setItemChecked(selected, true);

        int itemHeight = (int) (40 * ctx.getResources().getDisplayMetrics().density);
        int popupHeight = itemHeight * VISIBLE_ITEMS;
        int popupWidth = anchor.getWidth() + (int) (16 * ctx.getResources().getDisplayMetrics().density);

        PopupWindow popup = new PopupWindow(listView, popupWidth, popupHeight, true);
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setOutsideTouchable(true);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            stepPx = position + 1;
            updateCenterDisplay();
            popup.dismiss();
        });

        listView.post(() -> {
            int[] loc = new int[2];
            anchor.getLocationOnScreen(loc);
            int x = loc[0] + anchor.getWidth() / 2 - popupWidth / 2;
            int y = loc[1] + anchor.getHeight() / 2 - popupHeight / 2;
            popup.showAtLocation(anchor, Gravity.START | Gravity.TOP, x, y);

            listView.setSelectionFromTop(selected, popupHeight / 2 - itemHeight / 2);
        });
    }

    private void updateCenterDisplay() {
        if (centerView != null) {
            centerView.setText(stepPx + " px");
        }
    }

    private void stopRepeat() {
        if (repeatRunnable != null) {
            repeatHandler.removeCallbacks(repeatRunnable);
            repeatRunnable = null;
        }
    }

    public void cleanup() {
        stopRepeat();
        if (centerView != null) {
            centerView.setOnClickListener(null);
        }
    }

    public interface OnDpadActionListener {
        void onDpadAction(float dx, float dy);
    }
}
