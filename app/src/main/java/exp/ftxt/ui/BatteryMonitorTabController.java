package exp.ftxt.ui;

import android.os.BatteryManager;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

import exp.ftxt.MainActivity;
import exp.ftxt.R;
import exp.ftxt.features.battery_stats.BatteryCapacityEstimator;
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
    private final BatterySnapshotExporter snapshotExporter;

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
        snapshotExporter = new BatterySnapshotExporter(activity, this);
        BatteryCapacityEstimator.init(activity);
        batMonitorExportButton.setOnClickListener(v -> snapshotExporter.exportBatterySnapshot());
        batMonitorCopyButton.setOnClickListener(v -> snapshotExporter.copyToClipboard());
        rootView.findViewById(R.id.batChartPercentView).setOnClickListener(v ->
                BatteryChartDetailActivity.start(activity,
                        BatteryChartView.SERIES_PERCENT, charts.getCurrentWindowMs()));
    }

    private void bindViews(View rootView) {
        batTabMonitorView = rootView.findViewById(R.id.batTabMonitor);
        batMonitorRing = rootView.findViewById(R.id.batMonitorRing);
        batMonitorMetricsText1 = rootView.findViewById(R.id.batMonitorMetricsText1);
        batMonitorMetricsText2 = rootView.findViewById(R.id.batMonitorMetricsText2);
        batMonitorConditionBadge = rootView.findViewById(R.id.batMonitorConditionBadge);
        batMonitorExportButton = rootView.findViewById(R.id.batMonitorExportButton);
        batMonitorCopyButton = rootView.findViewById(R.id.batMonitorCopyButton);
        monitorLabelColor = activity.getColor(R.color.bat_monitor_label);
    }

    public void onPanelShown() {
        applySnapshotButtonsLock();
        resumeMonitorPolling();
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
    }

    public void cleanup() {
        stopMonitorPolling();
        charts.cleanup();
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
}
