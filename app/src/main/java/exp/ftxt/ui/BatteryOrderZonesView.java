package exp.ftxt.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import exp.ftxt.features.battery_stats.BatteryStatsConfig;

public class BatteryOrderZonesView extends LinearLayout {

    public interface OnOrderChangedListener {
        void onOrderChanged(String order, boolean temp, boolean pct, boolean volt, boolean cur, boolean power);
    }

    private static final int ZONE_ACTIVE = 0;
    private static final int ZONE_INACTIVE = 1;

    private final List<String> activeIds = new ArrayList<>();
    private final List<String> inactiveIds = new ArrayList<>();
    private final List<String> activeLabels = new ArrayList<>();
    private final List<String> inactiveLabels = new ArrayList<>();

    private LinearLayout activeContainer;
    private LinearLayout inactiveContainer;
    private OnOrderChangedListener listener;

    private final int touchSlop;
    private boolean night;

    private boolean dragging;
    private String dragId;
    private View dragChipView;
    private View dragView;
    private int dragStartRawX;
    private int dragStartRawY;
    private int dragChipW;
    private int dragChipH;
    private int lastZone = -1;
    private int lastIndex = -1;

    private List<String> snapshotActiveIds;
    private List<String> snapshotInactiveIds;
    private List<String> snapshotActiveLabels;
    private List<String> snapshotInactiveLabels;

    public BatteryOrderZonesView(Context context) {
        this(context, null);
    }

    public BatteryOrderZonesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        night = isNight(context);
        init();
    }

    private void init() {
        setOrientation(VERTICAL);
        addView(buildZoneRow(ZONE_ACTIVE));
        addView(buildZoneRow(ZONE_INACTIVE));

        TextView hint = new TextView(getContext());
        hint.setText("Seret chip untuk urutan, geser antar zona Aktif/Nonaktif");
        hint.setTextSize(11);
        hint.setTextColor(night ? 0xFF90A4AE : 0xFF78909C);
        hint.setPadding(0, dp(2), 0, 0);
        addView(hint);
    }

    private LinearLayout buildZoneRow(int zone) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(3), 0, dp(3));

        TextView header = new TextView(getContext());
        header.setText(zone == ZONE_ACTIVE ? "Aktif:" : "Nonaktif:");
        header.setTextSize(13);
        header.setTextColor(zone == ZONE_ACTIVE ? 0xFF4A90D9 : (night ? 0xFF90A4AE : 0xFF757575));
        LinearLayout.LayoutParams hlp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        hlp.setMarginEnd(dp(8));
        row.addView(header, hlp);

        LinearLayout container = new LinearLayout(getContext());
        container.setOrientation(HORIZONTAL);
        container.setGravity(Gravity.CENTER_VERTICAL);
        container.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(container);

        if (zone == ZONE_ACTIVE) {
            activeContainer = container;
        } else {
            inactiveContainer = container;
        }
        return row;
    }

    public void setListener(OnOrderChangedListener listener) {
        this.listener = listener;
    }

    public void setOrder(String order) {
        night = isNight(getContext());
        activeIds.clear();
        inactiveIds.clear();
        activeLabels.clear();
        inactiveLabels.clear();
        if (order == null || order.isEmpty()) {
            order = "temp,pct,volt,cur,power";
        }
        for (String id : order.split(",")) {
            id = id.trim();
            if (id.isEmpty()) continue;
            if (isActive(id)) {
                activeIds.add(id);
                activeLabels.add(labelFor(id));
            } else {
                inactiveIds.add(id);
                inactiveLabels.add(labelFor(id));
            }
        }
        renderZones();
    }

    private void renderZones() {
        renderZone(activeContainer, activeIds, activeLabels);
        renderZone(inactiveContainer, inactiveIds, inactiveLabels);
    }

    private void renderZone(LinearLayout container, List<String> ids, List<String> labels) {
        container.removeAllViews();
        boolean active = container == activeContainer;
        for (int i = 0; i < ids.size(); i++) {
            container.addView(createChip(ids.get(i), labels.get(i), active));
        }
    }

    private TextView createChip(String id, String label, boolean active) {
        TextView chip = new TextView(getContext());
        chip.setText(label);
        chip.setTextSize(15);
        chip.setGravity(Gravity.CENTER);
        chip.setPadding(dp(14), dp(8), dp(14), dp(8));
        chip.setTextColor(active
                ? (night ? 0xFFECEFF1 : 0xFF263238)
                : (night ? 0xFF90A4AE : 0xFF78909C));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(active
                ? (night ? 0xFF455A64 : 0xFFCFD8DC)
                : (night ? 0xFF263238 : 0xFFECEFF1));
        bg.setCornerRadius(dp(12));
        chip.setBackground(bg);

        chip.setAlpha(active ? 1f : 0.55f);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMarginEnd(dp(6));
        chip.setLayoutParams(lp);

        chip.setTag(id);
        chip.setOnTouchListener((v, e) -> handleChipTouch(v, e, id));
        return chip;
    }

    private boolean handleChipTouch(View v, MotionEvent e, String id) {
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (dragging) return false;
                dragId = id;
                dragStartRawX = (int) e.getRawX();
                dragStartRawY = (int) e.getRawY();
                requestDisallowIntercept(v, true);
                return true;
            case MotionEvent.ACTION_MOVE:
                if (!dragging) {
                    int dx = (int) e.getRawX() - dragStartRawX;
                    int dy = (int) e.getRawY() - dragStartRawY;
                    if (Math.abs(dx) > touchSlop || Math.abs(dy) > touchSlop) {
                        startDrag(v);
                    }
                }
                if (dragging) {
                    updateDrag(e.getRawX(), e.getRawY());
                }
                return true;
            case MotionEvent.ACTION_UP:
                requestDisallowIntercept(v, false);
                if (dragging) {
                    finishDrag(e.getRawX(), e.getRawY());
                }
                return true;
            case MotionEvent.ACTION_CANCEL:
                requestDisallowIntercept(v, false);
                if (dragging) {
                    cancelDrag();
                }
                return true;
            default:
                return false;
        }
    }

    private void requestDisallowIntercept(View v, boolean disallow) {
        ViewParent parent = v.getParent();
        while (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallow);
            parent = parent.getParent();
        }
    }

    private void startDrag(View chip) {
        if (chip.getParent() == null) return;
        dragging = true;
        dragId = (String) chip.getTag();
        snapshotState();

        dragChipView = chip;
        chip.setAlpha(0f);

        ViewGroup root = (ViewGroup) getRootView();
        dragView = createChipViewForDrag(chip);
        root.addView(dragView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        dragChipW = chip.getWidth();
        dragChipH = chip.getHeight();

        lastZone = -1;
        lastIndex = -1;
        updateDrag(dragStartRawX, dragStartRawY);
    }

    private TextView createChipViewForDrag(View chip) {
        TextView d = new TextView(getContext());
        if (chip instanceof TextView) {
            d.setText(((TextView) chip).getText());
        }
        d.setTextSize(15);
        d.setGravity(Gravity.CENTER);
        d.setPadding(dp(14), dp(8), dp(14), dp(8));
        d.setTextColor(night ? 0xFFECEFF1 : 0xFF263238);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(night ? 0xFF455A64 : 0xFFCFD8DC);
        bg.setCornerRadius(dp(12));
        d.setBackground(bg);
        d.setAlpha(0.9f);
        d.setElevation(dp(6));
        return d;
    }

    private void updateDrag(float rawX, float rawY) {
        if (dragView == null) return;
        ViewGroup root = (ViewGroup) getRootView();
        int[] rootLoc = new int[2];
        root.getLocationOnScreen(rootLoc);
        dragView.setX(rawX - rootLoc[0] - dragChipW / 2f);
        dragView.setY(rawY - rootLoc[1] - dragChipH / 2f);

        int zone = zoneAt(rawX, rawY);
        int index = indexInZone(zone, rawX);
        if (zone != lastZone || index != lastIndex) {
            lastZone = zone;
            lastIndex = index;
            applyTarget(zone, index);
        }
    }

    private void finishDrag(float rawX, float rawY) {
        int zone = zoneAt(rawX, rawY);
        int index = indexInZone(zone, rawX);
        applyTarget(zone, index);
        removeDragView();
        dragging = false;
        dragId = null;
        dragChipView = null;
        renderZones();

        BatteryStatsConfig.showTemperature = activeIds.contains("temp");
        BatteryStatsConfig.showPercentage = activeIds.contains("pct");
        BatteryStatsConfig.showVoltage = activeIds.contains("volt");
        BatteryStatsConfig.showCurrent = activeIds.contains("cur");
        BatteryStatsConfig.showPower = activeIds.contains("power");
        BatteryStatsConfig.itemOrder = buildOrder();

        if (listener != null) {
            listener.onOrderChanged(BatteryStatsConfig.itemOrder,
                    BatteryStatsConfig.showTemperature,
                    BatteryStatsConfig.showPercentage,
                    BatteryStatsConfig.showVoltage,
                    BatteryStatsConfig.showCurrent,
                    BatteryStatsConfig.showPower);
        }
    }

    private void cancelDrag() {
        restoreSnapshot();
        removeDragView();
        dragging = false;
        dragId = null;
        dragChipView = null;
        renderZones();
    }

    private void removeDragView() {
        if (dragView != null) {
            ViewGroup root = (ViewGroup) getRootView();
            root.removeView(dragView);
            dragView = null;
        }
    }

    private void snapshotState() {
        snapshotActiveIds = new ArrayList<>(activeIds);
        snapshotInactiveIds = new ArrayList<>(inactiveIds);
        snapshotActiveLabels = new ArrayList<>(activeLabels);
        snapshotInactiveLabels = new ArrayList<>(inactiveLabels);
    }

    private void restoreSnapshot() {
        activeIds.clear();
        activeIds.addAll(snapshotActiveIds);
        inactiveIds.clear();
        inactiveIds.addAll(snapshotInactiveIds);
        activeLabels.clear();
        activeLabels.addAll(snapshotActiveLabels);
        inactiveLabels.clear();
        inactiveLabels.addAll(snapshotInactiveLabels);
    }

    private void applyTarget(int targetZone, int targetIndex) {
        if (dragId == null) return;
        removeChipData(dragId);

        if (targetZone == ZONE_ACTIVE) {
            if (targetIndex < 0) targetIndex = 0;
            if (targetIndex > activeIds.size()) targetIndex = activeIds.size();
            activeIds.add(targetIndex, dragId);
            activeLabels.add(targetIndex, labelFor(dragId));
        } else {
            if (targetIndex < 0) targetIndex = 0;
            if (targetIndex > inactiveIds.size()) targetIndex = inactiveIds.size();
            inactiveIds.add(targetIndex, dragId);
            inactiveLabels.add(targetIndex, labelFor(dragId));
        }
    }

    private void removeChipData(String id) {
        int i = activeIds.indexOf(id);
        if (i >= 0) {
            activeIds.remove(i);
            activeLabels.remove(i);
            return;
        }
        i = inactiveIds.indexOf(id);
        if (i >= 0) {
            inactiveIds.remove(i);
            inactiveLabels.remove(i);
        }
    }

    private int zoneAt(float rawX, float rawY) {
        int[] loc = new int[2];
        activeContainer.getLocationOnScreen(loc);
        int activeTop = loc[1];
        int activeBottom = loc[1] + activeContainer.getHeight();
        inactiveContainer.getLocationOnScreen(loc);
        int inactiveTop = loc[1];
        int inactiveBottom = loc[1] + inactiveContainer.getHeight();

        if (rawY >= activeTop && rawY <= activeBottom) return ZONE_ACTIVE;
        if (rawY >= inactiveTop && rawY <= inactiveBottom) return ZONE_INACTIVE;
        int activeMid = (activeTop + activeBottom) / 2;
        int inactiveMid = (inactiveTop + inactiveBottom) / 2;
        return Math.abs(rawY - activeMid) <= Math.abs(rawY - inactiveMid)
                ? ZONE_ACTIVE : ZONE_INACTIVE;
    }

    private int indexInZone(int zone, float rawX) {
        LinearLayout container = zone == ZONE_ACTIVE ? activeContainer : inactiveContainer;
        int idx = 0;
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child == dragChipView) continue;
            int[] loc = new int[2];
            child.getLocationOnScreen(loc);
            if (rawX > loc[0] + child.getWidth() / 2f) {
                idx++;
            } else {
                break;
            }
        }
        return idx;
    }

    private String buildOrder() {
        StringBuilder sb = new StringBuilder();
        for (String id : activeIds) {
            if (sb.length() > 0) sb.append(',');
            sb.append(id);
        }
        for (String id : inactiveIds) {
            if (sb.length() > 0) sb.append(',');
            sb.append(id);
        }
        return sb.toString();
    }

    private boolean isActive(String id) {
        switch (id) {
            case "temp":
                return BatteryStatsConfig.showTemperature;
            case "pct":
                return BatteryStatsConfig.showPercentage;
            case "volt":
                return BatteryStatsConfig.showVoltage;
            case "cur":
                return BatteryStatsConfig.showCurrent;
            case "power":
                return BatteryStatsConfig.showPower;
            default:
                return true;
        }
    }

    private String labelFor(String id) {
        if (id == null) return "";
        switch (id) {
            case "temp":
                return "°C";
            case "pct":
                return "%";
            case "volt":
                return "V";
            case "cur":
                return "mA";
            case "power":
                return "W";
            default:
                return id;
        }
    }

    private boolean isNight(Context context) {
        return (context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }
}
