package exp.ftxt.ui;

import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import exp.ftxt.MainActivity;
import exp.ftxt.R;
import exp.ftxt.features.battery_stats.BatteryHistoryDb;
import exp.ftxt.features.battery_stats.BatteryReading;
import exp.ftxt.shared.ui.BatteryChartView;

public class BatteryChartHistoryController {

    private final MainActivity activity;

    private BatteryChartView batChartTempView;
    private BatteryChartView batChartPercentView;
    private BatteryChartView batChartPowerView;
    private BatteryChartView batChartVoltageView;
    private BatteryChartView batChartCurrentView;
    private SeekBar batChartRangeSeek;
    private TextView batChartRangeLabel;
    private final TextView[] rangeTicks = new TextView[10];
    private long chartWindowMs = BatteryChartView.WINDOW_5M;

    static final long[] CHART_WINDOWS = {
            BatteryChartView.WINDOW_2M, BatteryChartView.WINDOW_5M,
            BatteryChartView.WINDOW_10M, BatteryChartView.WINDOW_15M,
            BatteryChartView.WINDOW_30M, BatteryChartView.WINDOW_1H,
            BatteryChartView.WINDOW_3H, BatteryChartView.WINDOW_6H,
            BatteryChartView.WINDOW_12H, BatteryChartView.WINDOW_24H};

    private static final String[] CHART_WINDOW_LABELS = {
            "2 Menit", "5 Menit", "10 Menit", "15 Menit", "30 Menit",
            "1 Jam", "3 Jam", "6 Jam", "12 Jam", "24 Jam"};

    private static final int CHART_WINDOW_DEFAULT = 1;

    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService chartExecutor = Executors.newSingleThreadExecutor();
    private boolean chartQueryInFlight = false;

    public BatteryChartHistoryController(MainActivity activity, View rootView) {
        this.activity = activity;
        bindViews(rootView);
        setupChartControls();
    }

    private void bindViews(View rootView) {
        batChartTempView = rootView.findViewById(R.id.batChartTempView);
        batChartPercentView = rootView.findViewById(R.id.batChartPercentView);
        batChartPowerView = rootView.findViewById(R.id.batChartPowerView);
        batChartVoltageView = rootView.findViewById(R.id.batChartVoltageView);
        batChartCurrentView = rootView.findViewById(R.id.batChartCurrentView);
        batChartRangeSeek = rootView.findViewById(R.id.batChartRangeSeek);
        batChartRangeLabel = rootView.findViewById(R.id.batChartRangeLabel);
        int[] tickIds = {R.id.batChartTick0, R.id.batChartTick1, R.id.batChartTick2,
                R.id.batChartTick3, R.id.batChartTick4, R.id.batChartTick5,
                R.id.batChartTick6, R.id.batChartTick7, R.id.batChartTick8,
                R.id.batChartTick9};
        for (int i = 0; i < tickIds.length; i++) {
            rangeTicks[i] = rootView.findViewById(tickIds[i]);
        }
    }

    private void setupChartControls() {
        batChartTempView.setSeriesType(BatteryChartView.SERIES_TEMP);
        batChartPercentView.setSeriesType(BatteryChartView.SERIES_PERCENT);
        batChartPowerView.setSeriesType(BatteryChartView.SERIES_POWER);
        batChartVoltageView.setSeriesType(BatteryChartView.SERIES_VOLTAGE);
        batChartCurrentView.setSeriesType(BatteryChartView.SERIES_CURRENT);

        batChartTempView.setOnClickListener(v ->
                BatteryChartDetailActivity.start(activity, BatteryChartView.SERIES_TEMP, chartWindowMs));
        batChartPowerView.setOnClickListener(v ->
                BatteryChartDetailActivity.start(activity, BatteryChartView.SERIES_POWER, chartWindowMs));
        batChartVoltageView.setOnClickListener(v ->
                BatteryChartDetailActivity.start(activity, BatteryChartView.SERIES_VOLTAGE, chartWindowMs));
        batChartCurrentView.setOnClickListener(v ->
                BatteryChartDetailActivity.start(activity, BatteryChartView.SERIES_CURRENT, chartWindowMs));

        batChartRangeSeek.setMax(CHART_WINDOWS.length - 1);
        batChartRangeSeek.setProgress(CHART_WINDOW_DEFAULT);
        batChartRangeLabel.setText(CHART_WINDOW_LABELS[CHART_WINDOW_DEFAULT] + " Terakhir");
        highlightRangeTick(CHART_WINDOW_DEFAULT);
        // Slider transparan menimpa baris label: padding 5% lebar membuat setiap
        // langkah jatuh tepat di tengah labelnya (10 label = lebar 10% per sel).
        batChartRangeSeek.addOnLayoutChangeListener((v, left, top, right, bottom,
                oldLeft, oldTop, oldRight, oldBottom) -> {
            int pad = (right - left) / 20;
            if (pad > 0 && batChartRangeSeek.getPaddingLeft() != pad) {
                batChartRangeSeek.setPadding(pad, 0, pad, 0);
            }
        });
        batChartRangeSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;
                batChartRangeLabel.setText(CHART_WINDOW_LABELS[progress] + " Terakhir");
                highlightRangeTick(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                applyChartWindow(seekBar.getProgress());
            }
        });
    }

    private void highlightRangeTick(int index) {
        for (int i = 0; i < rangeTicks.length; i++) {
            if (rangeTicks[i] == null) continue;
            boolean active = i == index;
            rangeTicks[i].setTextColor(activity.getResources().getColor(
                    active ? R.color.bat_monitor_header : R.color.bat_monitor_label));
            rangeTicks[i].setTypeface(active ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        }
    }

    public long getCurrentWindowMs() {
        return chartWindowMs;
    }

    private void applyChartWindow(int index) {
        if (index < 0 || index >= CHART_WINDOWS.length) index = CHART_WINDOW_DEFAULT;
        long window = CHART_WINDOWS[index];
        chartWindowMs = window;
        batChartRangeLabel.setText(CHART_WINDOW_LABELS[index] + " Terakhir");
        highlightRangeTick(index);
        batChartTempView.setWindowMs(window);
        batChartPercentView.setWindowMs(window);
        batChartPowerView.setWindowMs(window);
        batChartVoltageView.setWindowMs(window);
        batChartCurrentView.setWindowMs(window);
        refresh();
    }

    public void refresh() {
        if (batChartTempView == null || chartQueryInFlight) return;
        final long now = System.currentTimeMillis();
        final long from = now - chartWindowMs;
        chartQueryInFlight = true;
        chartExecutor.execute(() -> {
            BatteryReading.Snapshot[] data =
                    BatteryHistoryDb.get(activity).queryChart(from, now, 600);
            uiHandler.post(() -> {
                chartQueryInFlight = false;
                if (batChartTempView == null) return;
                batChartTempView.setData(data);
                batChartPercentView.setData(data);
                batChartPowerView.setData(data);
                batChartVoltageView.setData(data);
                batChartCurrentView.setData(data);
            });
        });
    }

    public void cleanup() {
        chartExecutor.shutdown();
    }
}
