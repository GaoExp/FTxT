package exp.ftxt.ui;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.Locale;

import exp.ftxt.MainActivity;
import exp.ftxt.R;
import exp.ftxt.features.battery_stats.BatteryCapacityEstimator;
import exp.ftxt.features.battery_stats.BatteryHistoryDb;
import exp.ftxt.features.battery_stats.BatteryMonitor;
import exp.ftxt.features.battery_stats.BatteryReading;
import exp.ftxt.features.battery_stats.BatteryRingView;

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
    private BatteryChartHistoryController charts;
    private BatteryHealthCardController health;
    private BatterySessionHistoryController history;
    private BatterySessionLiveController live;
    private final BatterySnapshotExporter snapshotExporter;

    private View batSubInfoPanel;
    private View batSubLivePanel;
    private View batSubHealthPanel;
    private TextView batSubTabInfo;
    private TextView batSubTabLive;
    private TextView batSubTabHealth;
    private ViewPager2 batSubPager;
    private final SubTabAdapter subTabAdapter = new SubTabAdapter();

    private int currentSubTab = 0;

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
        snapshotExporter = new BatterySnapshotExporter(activity, this);
        BatteryCapacityEstimator.init(activity);

        // Offscreen limit = 2 agar ketiga halaman selalu ter-inflate oleh pager,
        // sehingga page view tersedia untuk bindPanels/setupSubTabs.
        batSubPager.setOffscreenPageLimit(2);
        batSubPager.setAdapter(subTabAdapter);
        batSubPager.post(() -> {
            bindPanels();
            setupSubTabs();
        });
    }

    private static final class SubTabAdapter extends RecyclerView.Adapter<SubTabAdapter.Holder> {

        static final class Holder extends RecyclerView.ViewHolder {
            Holder(@NonNull View itemView) {
                super(itemView);
            }
        }

        private final int[] layouts = {R.layout.panel_bat_sub_info,
                R.layout.panel_bat_sub_live,
                R.layout.panel_bat_sub_health};
        private final View[] pages = new View[3];

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View page = inflater.inflate(layouts[viewType], parent, false);
            pages[viewType] = page;
            return new Holder(page);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
        }

        @Override
        public int getItemCount() {
            return 3;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        View getPage(int position) {
            return pages[position];
        }
    }


    private void setupSubTabs() {
        batSubTabInfo.setOnClickListener(v -> selectSubTab(0));
        batSubTabLive.setOnClickListener(v -> selectSubTab(1));
        batSubTabHealth.setOnClickListener(v -> selectSubTab(2));

        applySubTabState(batSubTabInfo, true);
        applySubTabState(batSubTabLive, false);
        applySubTabState(batSubTabHealth, false);

        batSubPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentSubTab = position;
                refreshTabHighlight(position);
                if (position == 1) live.onPanelShown();
                else live.onPanelHidden();
                if (position == 2) history.refresh();
            }
        });
    }

    private void bindPanels() {
        View infoPage = subTabAdapter.getPage(0);
        View livePage = subTabAdapter.getPage(1);
        View healthPage = subTabAdapter.getPage(2);

        batSubInfoPanel = infoPage.findViewById(R.id.batSubInfoPanel);
        batSubLivePanel = livePage.findViewById(R.id.batSubLivePanel);
        batSubHealthPanel = healthPage.findViewById(R.id.batSubHealthPanel);

        batMonitorRing = infoPage.findViewById(R.id.batMonitorRing);
        batMonitorMetricsText1 = infoPage.findViewById(R.id.batMonitorMetricsText1);
        batMonitorMetricsText2 = infoPage.findViewById(R.id.batMonitorMetricsText2);
        batMonitorConditionBadge = infoPage.findViewById(R.id.batMonitorConditionBadge);

        batMonitorExportButton = healthPage.findViewById(R.id.batMonitorExportButton);
        batMonitorCopyButton = healthPage.findViewById(R.id.batMonitorCopyButton);
        batMonitorExportButton.setOnClickListener(v -> snapshotExporter.exportBatterySnapshot());
        batMonitorCopyButton.setOnClickListener(v -> snapshotExporter.copyToClipboard());

        charts = new BatteryChartHistoryController(activity, infoPage);
        health = new BatteryHealthCardController(activity, healthPage);
        history = new BatterySessionHistoryController(activity, healthPage);
        live = new BatterySessionLiveController(activity, livePage);
    }

    private void refreshTabHighlight(int index) {
        applySubTabState(batSubTabInfo, index == 0);
        applySubTabState(batSubTabLive, index == 1);
        applySubTabState(batSubTabHealth, index == 2);
    }

    private void selectSubTab(int index) {
        if (index == currentSubTab) return;
        batSubPager.setCurrentItem(index, true);
    }

    private void applySubTabState(TextView tv, boolean active) {
        int color = activity.getColor(active
                ? R.color.bat_monitor_header : R.color.bat_monitor_label);
        tv.setTextColor(color);
        tv.setTypeface(active ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        tv.setCompoundDrawableTintList(ColorStateList.valueOf(color));
    }

    private void bindViews(View rootView) {
        batTabMonitorView = rootView.findViewById(R.id.batTabMonitor);
        batSubTabInfo = rootView.findViewById(R.id.batSubTabInfo);
        batSubTabLive = rootView.findViewById(R.id.batSubTabLive);
        batSubTabHealth = rootView.findViewById(R.id.batSubTabHealth);
        batSubPager = rootView.findViewById(R.id.batSubPager);
        monitorLabelColor = activity.getColor(R.color.bat_monitor_label);
    }

    public void onPanelShown() {
        applySnapshotButtonsLock();
        resumeMonitorPolling();
        if (currentSubTab == 1 && live != null) live.onPanelShown();
    }

    private void applySnapshotButtonsLock() {
        if (batMonitorExportButton == null || batMonitorCopyButton == null) return;
        boolean unlocked = activity.getSharedPreferences("ftxt_prefs", android.content.Context.MODE_PRIVATE)
                .getBoolean(exp.ftxt.SettingsActivity.PREF_DEVELOPER_UNLOCKED, false);
        batMonitorExportButton.setEnabled(unlocked);
        batMonitorExportButton.setAlpha(unlocked ? 1f : 0.4f);
        batMonitorCopyButton.setEnabled(unlocked);
        batMonitorCopyButton.setAlpha(unlocked ? 1f : 0.4f);
    }

    public void onPanelHidden() {
        stopMonitorPolling();
        if (live != null) live.onPanelHidden();
    }

    public void cleanup() {
        stopMonitorPolling();
        if (charts != null) charts.cleanup();
        if (history != null) history.cleanup();
        if (live != null) live.cleanup();
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

        if (health != null) health.refresh();
        if (charts != null) charts.refresh();
        if (history != null) history.refresh();
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
