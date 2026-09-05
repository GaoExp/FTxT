package exp.ftxt.ui;

import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableStringBuilder;
import android.view.View;
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

    private int period = PERIOD_DAILY;
    private long selectedBucket = -1;
    private long lastRefreshTime = 0;
    private boolean queryInFlight = false;

    public BatterySessionHistoryController(MainActivity activity, View pageView) {
        this.activity = activity;
        bindViews(pageView);
        bindTabs();
        highlightTabs(periodTabs, PERIOD_DAILY);
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
        highlightTabs(periodTabs, p);
        reloadChart();
    }

    private void setFilter(int f) {
        if (selectedBucket < 0) {
            highlightTabs(filterTabs, f);
            return;
        }
        highlightTabs(filterTabs, f);
        long end = bucketEnd(selectedBucket);
        SessionListActivity.start(activity, selectedBucket, end, f);
    }

    private void highlightTabs(TextView[] tabs, int active) {
        for (int i = 0; i < tabs.length; i++) {
            boolean isActive = (i == active);
            tabs[i].setTextColor(activity.getColor(
                    isActive ? R.color.bat_monitor_header : R.color.bat_monitor_label));
            tabs[i].setTypeface(isActive ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            tabs[i].setBackgroundResource(isActive
                    ? R.drawable.bat_badge_active_bg : R.drawable.bat_badge_stopped_bg);
        }
    }

    private long[] rangeForPeriod() {
        long now = System.currentTimeMillis();
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(now);
        switch (period) {
            case PERIOD_WEEKLY:
                trimToWeek(c);
                c.add(Calendar.DAY_OF_YEAR, -77);
                break;
            case PERIOD_MONTHLY:
                c.set(Calendar.DAY_OF_MONTH, 1);
                trimToDay(c);
                c.add(Calendar.MONTH, -11);
                break;
            default:
                trimToDay(c);
                c.add(Calendar.DAY_OF_YEAR, -11);
                break;
        }
        return new long[] {c.getTimeInMillis(), now};
    }

    private void trimToDay(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
    }

    private void trimToWeek(Calendar c) {
        int dow = c.get(Calendar.DAY_OF_WEEK);
        if (dow != Calendar.MONDAY) {
            c.add(Calendar.DAY_OF_YEAR, -(dow - Calendar.MONDAY));
        }
        trimToDay(c);
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
        final int mode = period == PERIOD_MONTHLY ? BatteryHistoryDb.MODE_MONTHLY
                : period == PERIOD_WEEKLY ? BatteryHistoryDb.MODE_WEEKLY
                : BatteryHistoryDb.MODE_DAILY;
        final int slotCount = 12;
        queryInFlight = true;
        sessionExecutor.execute(() -> {
            final ArrayList<BatteryHistoryDb.BarAggregate> aggregates =
                    BatteryHistoryDb.get(activity).queryBarAggregates(range[0], range[1], mode);
            uiHandler.post(() -> {
                queryInFlight = false;
                chartView.setRange(range[0], range[1], mode, slotCount);
                chartView.setData(aggregates);
                updatePeriodRangeLabel(range);
                if (!aggregates.isEmpty()) {
                    if (selectedBucket < 0 || !containsBucket(aggregates, selectedBucket)) {
                        selectedBucket = aggregates.get(aggregates.size() - 1).bucketStartMs;
                    }
                } else {
                    selectedBucket = -1;
                }
                chartView.setSelectedBucket(selectedBucket);
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

    private void updatePeriodRangeLabel(long[] range) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.US);
        String periodLabel = period == PERIOD_DAILY ? "12 hari terakhir"
                : period == PERIOD_WEEKLY ? "12 minggu terakhir" : "12 bulan terakhir";
        rangeText.setText(String.format(Locale.US, "%s · %s – %s", periodLabel,
                sdf.format(new Date(range[0])), sdf.format(new Date(range[1]))));
    }

    private void updateSelectedRangeText() {
        String periodLabel = period == PERIOD_DAILY ? "12 hari terakhir"
                : period == PERIOD_WEEKLY ? "12 minggu terakhir" : "12 bulan terakhir";
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
        double chargeMah = 0d, dischargeMah = 0d;
        double effSum = 0d;
        int effCount = 0;
        for (BatteryHistoryDb.SessionEntry e : entries) {
            if (e.isCharge) {
                charges++;
                chargeMah += e.mAhCounter > 0 ? e.mAhCounter : e.mAhIntegral;
            } else {
                discharges++;
                dischargeMah += e.mAhCounter > 0 ? e.mAhCounter : e.mAhIntegral;
                if (e.efficiencyPercent >= 0) {
                    effSum += e.efficiencyPercent;
                    effCount++;
                }
            }
        }
        SpannableStringBuilder sb = new SpannableStringBuilder();
        sb.append(String.format(Locale.US, "Sesi: %d isi · %d buang\n",
                charges, discharges));
        sb.append(String.format(Locale.US, "Terisi: %s mAh\n",
                fmtMah(chargeMah)));
        sb.append(String.format(Locale.US, "Terpakai: %s mAh\n",
                fmtMah(dischargeMah)));
        if (chargeMah > 0 && dischargeMah > 0) {
            float efficiency = (float) (chargeMah / dischargeMah * 100.0);
            sb.append(String.format(Locale.US, "Efisiensi: %.0f%%\n", efficiency));
        }
        if (effCount > 0) {
            sb.append(String.format(Locale.US, "Wear: %.1f%%\n", effSum / effCount));
        }
        summaryText.setText(sb);
    }

    private String fmtMah(double v) {
        return v > 0 ? String.format(Locale.US, "%.0f", v) : "—";
    }

    private long bucketEnd(long bucketStart) {
        if (period == PERIOD_MONTHLY) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(bucketStart);
            c.add(Calendar.MONTH, 1);
            return c.getTimeInMillis();
        }
        if (period == PERIOD_WEEKLY) {
            return bucketStart + 7L * DAY_MS;
        }
        return bucketStart + DAY_MS;
    }

    private void refreshSessionsForSelection() {
        if (selectedBucket < 0) {
            updateSelectedRangeText();
            updateSummary(new ArrayList<>());
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
                updateSelectedRangeText();
                updateSummary(entries);
            });
        });
    }

    public void cleanup() {
        sessionExecutor.shutdown();
    }
}