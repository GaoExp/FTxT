package exp.ftxt.ui;

import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import exp.ftxt.MainActivity;
import exp.ftxt.R;
import exp.ftxt.features.battery_stats.BatteryHistoryDb;
import exp.ftxt.shared.ui.BatterySessionBarChartView;
import exp.ftxt.shared.ui.InfoTooltip;

/** Kontrol panel "Riwayat Sesi": grafik batang per hari/bulan + ringkasan + daftar. */
public class BatterySessionHistoryController {

    private static final int PERIOD_DAILY = 0;
    private static final int PERIOD_WEEKLY = 1;
    private static final int PERIOD_MONTHLY = 2;

    private static final int FILTER_ALL = 0;
    private static final int FILTER_CHARGE = 1;
    private static final int FILTER_DISCHARGE = 2;

    private static final long DAY_MS = 86_400_000L;
    private static final long MIN_REFRESH_MS = 4_000L;

    private final MainActivity activity;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService sessionExecutor = Executors.newSingleThreadExecutor();

    private TextView[] periodTabs;
    private BatterySessionBarChartView chartView;
    private TextView rangeText;
    private TextView summaryText;
    private TextView[] filterTabs;
    private LinearLayout sessionList;
    private int monitorLabelColor;

    private int period = PERIOD_DAILY;
    private int filter = FILTER_ALL;
    private long selectedBucket = -1;
    private long lastRefreshTime = 0;
    private boolean queryInFlight = false;
    private ArrayList<BatteryHistoryDb.SessionEntry> sessionEntriesCache = new ArrayList<>();

    public BatterySessionHistoryController(MainActivity activity, View rootView) {
        this.activity = activity;
        bindViews(rootView);
        bindTabs();
        setFilter(FILTER_ALL);
        chartView.setOnBarClickListener((index, aggregate) -> {
            selectedBucket = aggregate.bucketStartMs;
            refreshSessionsForSelection();
        });
    }

    private void bindViews(View rootView) {
        periodTabs = new TextView[] {
                rootView.findViewById(R.id.batHistPeriodDaily),
                rootView.findViewById(R.id.batHistPeriodWeekly),
                rootView.findViewById(R.id.batHistPeriodMonthly)};
        chartView = rootView.findViewById(R.id.batSessionBarChart);
        rangeText = rootView.findViewById(R.id.batHistRangeText);
        summaryText = rootView.findViewById(R.id.batHistSummaryText);
        monitorLabelColor = activity.getColor(R.color.bat_monitor_label);
        rootView.findViewById(R.id.batHistInfoButton)
                .setOnClickListener(v -> InfoTooltip.show(activity, v,
                        "Meter mAh — Riwayat Sesi",
                        "C = muatan yang dilaporkan perangkat (Coulomb counter).\n"
                                + "I = integrasi arus × waktu yang diukur tiap sampel.\n\n"
                                + "Keduanya mengukur energi masuk/pakai per sesi, bukan "
                                + "kapasitas baterai.\n"
                                + "Estimasi kapasitas di kartu Kesehatan memakai I."));
        filterTabs = new TextView[] {
                rootView.findViewById(R.id.batHistFilterAll),
                rootView.findViewById(R.id.batHistFilterCharge),
                rootView.findViewById(R.id.batHistFilterDischarge)};
        sessionList = rootView.findViewById(R.id.batSessionList);
    }

    private void bindTabs() {
        periodTabs[PERIOD_DAILY].setOnClickListener(v -> setPeriod(PERIOD_DAILY));
        periodTabs[PERIOD_WEEKLY].setOnClickListener(v -> setPeriod(PERIOD_WEEKLY));
        periodTabs[PERIOD_MONTHLY].setOnClickListener(v -> setPeriod(PERIOD_MONTHLY));
        filterTabs[FILTER_ALL].setOnClickListener(v -> setFilter(FILTER_ALL));
        filterTabs[FILTER_CHARGE].setOnClickListener(v -> setFilter(FILTER_CHARGE));
        filterTabs[FILTER_DISCHARGE].setOnClickListener(v -> setFilter(FILTER_DISCHARGE));
    }

    private void setPeriod(int p) {
        if (period == p) return;
        period = p;
        selectedBucket = -1;
        refresh();
    }

    private void setFilter(int f) {
        filter = f;
        for (int i = 0; i < filterTabs.length; i++) {
            setBadge(filterTabs[i], i == f);
        }
        renderSessionRows(sessionEntriesCache);
    }

    private void setBadge(TextView tv, boolean active) {
        tv.setTextColor(activity.getColor(active ? R.color.bat_monitor_header : R.color.bat_monitor_label));
        tv.setTypeface(active ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        tv.setBackgroundResource(active ? R.drawable.bat_badge_active_bg : R.drawable.bat_badge_stopped_bg);
    }

    private long[] rangeForPeriod() {
        long now = System.currentTimeMillis();
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(now);
        switch (period) {
            case PERIOD_WEEKLY:
                c.add(Calendar.DAY_OF_YEAR, -56);
                break;
            case PERIOD_MONTHLY:
                c.add(Calendar.MONTH, -12);
                break;
            default:
                c.add(Calendar.DAY_OF_YEAR, -30);
                break;
        }
        return new long[] {c.getTimeInMillis(), now};
    }

    public void refresh() {
        long now = System.currentTimeMillis();
        if (now - lastRefreshTime < MIN_REFRESH_MS) return;
        lastRefreshTime = now;
        reloadChart();
    }

    private void reloadChart() {
        if (queryInFlight) return;
        final long[] range = rangeForPeriod();
        final boolean monthly = period == PERIOD_MONTHLY;
        queryInFlight = true;
        sessionExecutor.execute(() -> {
            final ArrayList<BatteryHistoryDb.BarAggregate> aggregates =
                    BatteryHistoryDb.get(activity).queryBarAggregates(range[0], range[1], monthly);
            uiHandler.post(() -> {
                queryInFlight = false;
                chartView.setMonthly(monthly);
                chartView.setData(aggregates);
                updatePeriodRangeLabel(range);
                if (!aggregates.isEmpty()) {
                    if (selectedBucket < 0 || !containsBucket(aggregates, selectedBucket)) {
                        selectedBucket = aggregates.get(aggregates.size() - 1).bucketStartMs;
                    }
                } else {
                    selectedBucket = -1;
                    sessionEntriesCache = new ArrayList<>();
                }
                chartView.setSelectedIndex(bucketIndex(aggregates, selectedBucket));
                refreshSessionsForSelection();
            });
        });
    }

    private boolean containsBucket(ArrayList<BatteryHistoryDb.BarAggregate> a, long bucket) {
        for (BatteryHistoryDb.BarAggregate b : a) {
            if (b.bucketStartMs == bucket) return true;
        }
        return false;
    }

    private int bucketIndex(ArrayList<BatteryHistoryDb.BarAggregate> a, long bucket) {
        for (int i = 0; i < a.size(); i++) {
            if (a.get(i).bucketStartMs == bucket) return i;
        }
        return a.size() - 1;
    }

    private void updatePeriodRangeLabel(long[] range) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.US);
        String periodLabel = period == PERIOD_DAILY ? "30 hari terakhir"
                : period == PERIOD_WEEKLY ? "8 minggu terakhir" : "12 bulan terakhir";
        rangeText.setText(String.format(Locale.US, "%s · %s – %s", periodLabel,
                sdf.format(new Date(range[0])), sdf.format(new Date(range[1]))));
    }

    private void updateSelectedRangeText() {
        String periodLabel = period == PERIOD_DAILY ? "30 hari terakhir"
                : period == PERIOD_WEEKLY ? "8 minggu terakhir" : "12 bulan terakhir";
        if (selectedBucket < 0) {
            rangeText.setText(periodLabel);
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(
                period == PERIOD_MONTHLY ? "MM/yy" : "EEE, dd/MM/yy", Locale.US);
        rangeText.setText(String.format(Locale.US, "%s · Disorot: %s", periodLabel,
                sdf.format(new Date(selectedBucket))));
    }

    private void updateSummary(ArrayList<BatteryHistoryDb.SessionEntry> entries) {
        if (entries.isEmpty()) {
            summaryText.setText("Tidak ada sesi di periode ini");
            return;
        }
        int charges = 0, discharges = 0;
        float chargePct = 0f, dischargePct = 0f;
        double mAhCounter = 0d, mAhIntegral = 0d;
        float effSum = 0f;
        int effCount = 0;
        for (BatteryHistoryDb.SessionEntry e : entries) {
            mAhCounter += e.mAhCounter;
            mAhIntegral += e.mAhIntegral;
            if (e.isCharge) {
                charges++;
                if (e.startPercent >= 0 && e.endPercent >= 0) {
                    chargePct += e.endPercent - e.startPercent;
                }
            } else {
                discharges++;
                if (e.startPercent >= 0 && e.endPercent >= 0) {
                    dischargePct += e.startPercent - e.endPercent;
                }
                if (e.efficiencyPercent >= 0) {
                    effSum += e.efficiencyPercent;
                    effCount++;
                }
            }
        }
        SpannableStringBuilder sb = new SpannableStringBuilder();
        sb.append(String.format(Locale.US, "Sesi (periode ini): %d isi · %d buang\n",
                charges, discharges));
        sb.append(String.format(Locale.US, "Telah terisi: %s%% · Penggunaan: %s%%\n",
                fmtPct(chargePct), fmtPct(dischargePct)));
        sb.append(String.format(Locale.US, "Pengikisan: %.1f siklus\n", dischargePct / 100f));
        if (effCount > 0) {
            sb.append(String.format(Locale.US, "Efficiency: %.0f%%\n", effSum / effCount));
        }
        appendEnergyMah(sb,
                mAhCounter, mAhIntegral,
                activity.getColor(R.color.bat_monitor_header));
        summaryText.setText(sb);
    }

    private void appendEnergyMah(SpannableStringBuilder sb, double counter, double integral, int accent) {
        int labelStart = sb.length();
        sb.append("Energi masuk: ");
        int cLabelStart = sb.length();
        sb.append("Coulomb ");
        int cValStart = sb.length();
        String cVal = fmtMah(counter);
        sb.append(cVal).append(" mAh");
        sb.append(" · ");
        int iLabelStart = sb.length();
        sb.append("Integral ");
        int iValStart = sb.length();
        String iVal = fmtMah(integral);
        sb.append(iVal).append(" mAh");

        sb.setSpan(new ForegroundColorSpan(monitorLabelColor),
                labelStart, cLabelStart, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.setSpan(new StyleSpan(Typeface.BOLD), cLabelStart, cValStart, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.setSpan(new ForegroundColorSpan(accent), cValStart, cValStart + cVal.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.setSpan(new StyleSpan(Typeface.BOLD), iLabelStart, iValStart, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.setSpan(new ForegroundColorSpan(accent), iValStart, iValStart + iVal.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private String fmtMah(double v) {
        return v > 0 ? String.format(Locale.US, "%.0f", v) : "—";
    }

    private String fmtPct(float v) {
        return v == Math.floor(v) ? String.valueOf((int) v) : String.format(Locale.US, "%.1f", v);
    }

    private long bucketEnd(long bucketStart) {
        if (period == PERIOD_MONTHLY) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(bucketStart);
            c.add(Calendar.MONTH, 1);
            return c.getTimeInMillis();
        }
        return bucketStart + DAY_MS;
    }

    private void refreshSessionsForSelection() {
        if (selectedBucket < 0) {
            sessionEntriesCache = new ArrayList<>();
            updateSelectedRangeText();
            updateSummary(sessionEntriesCache);
            renderSessionRows(sessionEntriesCache);
            return;
        }
        if (queryInFlight) return;
        final long from = selectedBucket;
        final long to = bucketEnd(selectedBucket);
        queryInFlight = true;
        sessionExecutor.execute(() -> {
            final ArrayList<BatteryHistoryDb.SessionEntry> entries =
                    BatteryHistoryDb.get(activity).querySessionEntries(from, to);
            uiHandler.post(() -> {
                queryInFlight = false;
                sessionEntriesCache = entries;
                updateSelectedRangeText();
                updateSummary(entries);
                renderSessionRows(entries);
            });
        });
    }

    private void renderSessionRows(ArrayList<BatteryHistoryDb.SessionEntry> entries) {
        sessionList.removeAllViews();
        ArrayList<BatteryHistoryDb.SessionEntry> shown = new ArrayList<>();
        for (BatteryHistoryDb.SessionEntry e : entries) {
            if (filter == FILTER_ALL) {
                shown.add(e);
            } else if (filter == FILTER_CHARGE && e.isCharge) {
                shown.add(e);
            } else if (filter == FILTER_DISCHARGE && !e.isCharge) {
                shown.add(e);
            }
        }
        if (shown.isEmpty()) {
            TextView empty = smallText("Tidak ada sesi");
            sessionList.addView(empty);
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm", Locale.US);
        for (BatteryHistoryDb.SessionEntry e : shown) {
            sessionList.addView(entryRow(e, sdf));
        }
    }

    private View entryRow(final BatteryHistoryDb.SessionEntry e,
                          SimpleDateFormat sdf) {
        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, dp(6), 0, dp(6));
        row.setOnClickListener(v -> SessionDetailActivity.start(activity, e));

        int accent = activity.getColor(e.isCharge
                ? R.color.bat_monitor_active : R.color.bat_chart_power);
        String typeTag = e.isCharge ? "PENGISIAN" : "PENGOSONGAN";
        String time = sdf.format(new Date(e.startTime)) + "–" + sdf.format(new Date(e.endTime));

        TextView head = new TextView(activity);
        head.setTextSize(12);
        head.setText(typeTag + "  " + time);
        head.setTextColor(accent);
        head.setTextSize(12);
        head.setTypeface(Typeface.DEFAULT_BOLD);
        row.addView(head);

        String dur = BatteryMonitorTabController.formatDuration(e.durationMs());
        StringBuilder detail = new StringBuilder();
        detail.append(String.format(Locale.US, "%d→%d%%", e.startPercent, e.endPercent));
        detail.append(" · ").append(dur);
        if (e.mAhCounter > 0 || e.mAhIntegral > 0) {
            detail.append(String.format(Locale.US, " · C %.0f · I %.0f mAh",
                    e.mAhCounter, e.mAhIntegral));
        }
        if (e.tempMax > 0) {
            detail.append(String.format(Locale.US, " · %.1f–%.1f°C", e.tempMin, e.tempMax));
        }
        if (!e.isCharge) {
            String eff = e.efficiencyPercent >= 0
                    ? String.format(Locale.US, "%.0f%%", e.efficiencyPercent) : "—";
            detail.append(" · efisiensi ").append(eff);
        }

        TextView body = new TextView(activity);
        body.setTextSize(11);
        body.setTypeface(Typeface.MONOSPACE);
        body.setTextColor(activity.getColor(R.color.bat_monitor_label));
        body.setText("  " + detail);
        row.addView(body);

        return row;
    }

    private TextView smallText(String text) {
        TextView tv = new TextView(activity);
        tv.setText(text);
        tv.setTextSize(12);
        tv.setTextColor(activity.getColor(R.color.bat_monitor_label));
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(0, dp(8), 0, dp(8));
        return tv;
    }

    private int dp(float value) {
        return (int) (value * activity.getResources().getDisplayMetrics().density);
    }

    public void cleanup() {
        sessionExecutor.shutdown();
    }
}