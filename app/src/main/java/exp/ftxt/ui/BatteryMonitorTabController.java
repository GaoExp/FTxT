package exp.ftxt.ui;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.widget.NestedScrollView;

import java.util.Locale;

import exp.ftxt.MainActivity;
import exp.ftxt.R;
import exp.ftxt.features.battery_stats.BatteryCapacityEstimator;
import exp.ftxt.features.battery_stats.BatteryHistoryDb;
import exp.ftxt.features.battery_stats.BatteryMonitor;
import exp.ftxt.features.battery_stats.BatteryReading;
import exp.ftxt.features.battery_stats.BatteryRingView;
import exp.ftxt.shared.ui.BatteryChartView;

public class BatteryMonitorTabController {

    private final MainActivity activity;

    View batTabMonitorView;
    private BatteryRingView batMonitorRing;
    TextView batMonitorMetricsText1;
    TextView batMonitorMetricsText2;
    private TextView batMonitorConditionBadge;
    private Button batMonitorExportButton;
    private Button batMonitorCopyButton;
    private int monitorLabelColor;

    private final Handler monitorHandler = new Handler(Looper.getMainLooper());
    private final BatteryChartHistoryController charts;
    private final BatteryHealthCardController health;
    private final BatterySessionHistoryController history;
    private final BatterySessionLiveController live;
    private final BatterySnapshotExporter snapshotExporter;

    private View batSubInfoPanel;
    private View batSubLivePanel;
    private View batSubHealthPanel;
    private TextView batSubTabInfo;
    private TextView batSubTabLive;
    private TextView batSubTabHealth;
    private NestedScrollView batSubScroll;

    private final Runnable monitorRunnable = new Runnable() {
        @Override
        public void run() {
            updateMonitorInfo();
            monitorHandler.postDelayed(this, 1000);
        }
    };

    public BatteryMonitorTabController(MainActivity activity, View rootView) {
        this.activity = activity;
        bindViews(rootView);
        charts = new BatteryChartHistoryController(activity, rootView);
        health = new BatteryHealthCardController(activity, rootView);
        history = new BatterySessionHistoryController(activity, rootView);
        live = new BatterySessionLiveController(activity, rootView);
        snapshotExporter = new BatterySnapshotExporter(activity, this);
        BatteryCapacityEstimator.init(activity);
        batMonitorExportButton.setOnClickListener(v -> snapshotExporter.exportBatterySnapshot());
        batMonitorCopyButton.setOnClickListener(v -> snapshotExporter.copyToClipboard());
        rootView.findViewById(R.id.batChartPercentView).setOnClickListener(v ->
                BatteryChartDetailActivity.start(activity,
                        BatteryChartView.SERIES_PERCENT, charts.getCurrentWindowMs()));
        setupSubTabs();
    }

    private void setupSubTabs() {
        batSubTabInfo.setOnClickListener(v -> selectSubTab(0));
        batSubTabLive.setOnClickListener(v -> selectSubTab(1));
        batSubTabHealth.setOnClickListener(v -> selectSubTab(2));
        setupSubTabSwipe();
        refreshSubTabState();
        showSubPanel(0);
    }

    private void setupSubTabSwipe() {
        GestureDetector detector = new GestureDetector(activity,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy) {
                        if (e1 == null || e2 == null) return false;
                        float dx = e2.getX() - e1.getX();
                        float dy = Math.abs(e2.getY() - e1.getY());
                        if (Math.abs(dx) > 100 && Math.abs(dx) > dy * 1.5f) {
                            if (dx < 0) swipeTo(currentSubTab + 1);
                            else swipeTo(currentSubTab - 1);
                            return true;
                        }
                        return false;
                    }
                });
        batSubScroll.setOnTouchListener((v, event) -> {
            detector.onTouchEvent(event);
            return false;
        });
    }

    private void swipeTo(int index) {
        if (index < 0 || index > 2) return;
        selectSubTab(index);
    }

    private void selectSubTab(int index) {
        currentSubTab = index;
        boolean liveShown = index == 1;
        TextView[] tabs = {batSubTabInfo, batSubTabLive, batSubTabHealth};
        for (int i = 0; i < tabs.length; i++) {
            applySubTabState(tabs[i], i == index);
        }
        showSubPanel(index);
        if (index == 2) history.refresh();
        if (liveShown) live.onPanelShown();
        else live.onPanelHidden();
    }

    private void refreshSubTabState() {
        TextView[] tabs = {batSubTabInfo, batSubTabLive, batSubTabHealth};
        for (int i = 0; i < tabs.length; i++) {
            applySubTabState(tabs[i], i == 0);
        }
    }

    private void applySubTabState(TextView tv, boolean active) {
        int color = activity.getColor(active
                ? R.color.bat_monitor_header : R.color.bat_monitor_label);
        tv.setTextColor(color);
        tv.setTypeface(active ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        tv.setCompoundDrawableTintList(ColorStateList.valueOf(color));
    }

    private void showSubPanel(int index) {
        batSubInfoPanel.setVisibility(index == 0 ? View.VISIBLE : View.GONE);
        batSubLivePanel.setVisibility(index == 1 ? View.VISIBLE : View.GONE);
        batSubHealthPanel.setVisibility(index == 2 ? View.VISIBLE : View.GONE);
    }

    private void bindViews(View rootView) {
        batTabMonitorView = rootView.findViewById(R.id.batTabMonitor);
        batMonitorRing = rootView.findViewById(R.id.batMonitorRing);
        batMonitorMetricsText1 = rootView.findViewById(R.id.batMonitorMetricsText1);
        batMonitorMetricsText2 = rootView.findViewById(R.id.batMonitorMetricsText2);
        batMonitorConditionBadge = rootView.findViewById(R.id.batMonitorConditionBadge);
        batMonitorExportButton = rootView.findViewById(R.id.batMonitorExportButton);
        batMonitorCopyButton = rootView.findViewById(R.id.batMonitorCopyButton);
        batSubTabInfo = rootView.findViewById(R.id.batSubTabInfo);
        batSubTabLive = rootView.findViewById(R.id.batSubTabLive);
        batSubTabHealth = rootView.findViewById(R.id.batSubTabHealth);
        batSubInfoPanel = rootView.findViewById(R.id.batSubInfoPanel);
        batSubLivePanel = rootView.findViewById(R.id.batSubLivePanel);
        batSubHealthPanel = rootView.findViewById(R.id.batSubHealthPanel);
        batSubScroll = rootView.findViewById(R.id.batSubScroll);
        monitorLabelColor = activity.getColor(R.color.bat_monitor_label);
    }

    private int currentSubTab = 0;

    public void onPanelShown() {
        applySnapshotButtonsLock();
        resumeMonitorPolling();
        if (currentSubTab == 1) live.onPanelShown();
    }

    private void applySnapshotButtonsLock() {
        boolean unlocked = activity.getSharedPreferences("ftxt_prefs", android.content.Context.MODE_PRIVATE)
                .getBoolean(exp.ftxt.SettingsActivity.PREF_DEVELOPER_UNLOCKED, false);
        batMonitorExportButton.setEnabled(unlocked);
        batMonitorExportButton.setAlpha(unlocked ? 1f : 0.4f);
        batMonitorCopyButton.setEnabled(unlocked);
        batMonitorCopyButton.setAlpha(unlocked ? 1f : 0.4f);
    }

    public void onPanelHidden() {
        stopMonitorPolling();
        live.onPanelHidden();
    }

    public void cleanup() {
        stopMonitorPolling();
        charts.cleanup();
        history.cleanup();
        live.cleanup();
    }

    public void refreshNow() {
        updateMonitorInfo();
    }

    private void resumeMonitorPolling() {
        if (batTabMonitorView == null || batTabMonitorView.getVisibility() != View.VISIBLE) {
            stopMonitorPolling();
            return;
        }
        monitorHandler.removeCallbacks(monitorRunnable);
        monitorHandler.post(monitorRunnable);
    }

    private void stopMonitorPolling() {
        monitorHandler.removeCallbacks(monitorRunnable);
    }

    private void updateMonitorInfo() {
        if (batMonitorMetricsText1 == null) return;
        BatteryReading.Snapshot s = BatteryMonitor.getLastSnapshot();

        SpannableStringBuilder col1 = new SpannableStringBuilder();
        appendLine(col1, "Suhu", String.format(Locale.US, "%.1f°C", s.tempC));
        appendLine(col1, "Voltase", String.format(Locale.US, "%.3fV", s.voltageV));
        appendLine(col1, "Arus", s.currentMa != 0
                ? String.format(Locale.US, "%+d mA", s.currentMa) : "—");
        batMonitorMetricsText1.setText(col1);

        SpannableStringBuilder col2 = new SpannableStringBuilder();
        appendLine(col2, "Daya", s.powerW > 0
                ? String.format(Locale.US, "%.2fW", s.powerW) : "—");
        appendLine(col2, "Cycle Count", s.cycleCount >= 0 ? String.valueOf(s.cycleCount) : "—");
        appendLine(col2, "Teknologi", s.technology != null ? s.technology : "—");

        boolean charging = s.statusInt == android.os.BatteryManager.BATTERY_STATUS_CHARGING
                || s.statusInt == android.os.BatteryManager.BATTERY_STATUS_FULL;
        boolean discharging = s.statusInt == android.os.BatteryManager.BATTERY_STATUS_DISCHARGING;
        String estLabel = null;
        if (charging && s.percent < 100) {
            long estMs = BatteryHistoryDb.get(activity).estimateTimeRemaining(true);
            estLabel = estMs >= 0 ? formatDuration(estMs) : "—";
            appendLine(col2, "Est. Penuh", estLabel);
        } else if (discharging && s.percent > 0) {
            long estMs = BatteryHistoryDb.get(activity).estimateTimeRemaining(false);
            estLabel = estMs >= 0 ? formatDuration(estMs) : "—";
            appendLine(col2, "Est. Habis", estLabel);
        }

        batMonitorMetricsText2.setText(col2);

        batMonitorRing.setBatteryData(s.percent,
                s.chargeMah >= 0 ? s.chargeMah + " mAh" : "—",
                shortChargingStatus(s));

        int condLevel = s.conditionLevel();
        batMonitorConditionBadge.setText("● " + s.conditionText());
        int condColor = activity.getColor(condLevel > 0 ? R.color.bat_monitor_hot
                : condLevel < 0 ? R.color.bat_monitor_cold : R.color.bat_monitor_active);
        batMonitorConditionBadge.setTextColor(condColor);

        health.refresh();
        charts.refresh();
        history.refresh();
    }

    static String shortChargingStatus(BatteryReading.Snapshot s) {
        if (s.statusInt == BatteryManager.BATTERY_STATUS_FULL) return "Full";
        if (s.statusInt == BatteryManager.BATTERY_STATUS_CHARGING) {
            switch (s.pluggedInt) {
                case BatteryManager.BATTERY_PLUGGED_AC: return "Charging•AC";
                case BatteryManager.BATTERY_PLUGGED_USB: return "Charging•USB";
                case BatteryManager.BATTERY_PLUGGED_WIRELESS: return "Charging•Wireless";
                default: return "Charging";
            }
        }
        return "Discharging";
    }

    private void appendLine(SpannableStringBuilder sb, String label, String value) {
        String padded = String.format(Locale.US, "%-17s", label);
        int start = sb.length();
        sb.append(padded).append(value).append("\n");
        sb.setSpan(new ForegroundColorSpan(monitorLabelColor),
                start, start + padded.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    static String formatDuration(long ms) {
        if (ms < 0) return "—";
        long totalMin = (ms + 59_000L) / 60_000L;
        if (totalMin < 1) return "<1 mnt";
        if (totalMin < 60) return totalMin + " mnt";
        long jam = totalMin / 60;
        long sisaMin = totalMin % 60;
        if (sisaMin == 0) return jam + " jam";
        return jam + "j " + sisaMin + "m";
    }
}
