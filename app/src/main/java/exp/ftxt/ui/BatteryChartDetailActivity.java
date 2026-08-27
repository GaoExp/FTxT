package exp.ftxt.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import exp.ftxt.R;
import exp.ftxt.features.battery_stats.BatteryHistoryDb;
import exp.ftxt.features.battery_stats.BatteryReading;
import exp.ftxt.shared.ui.BatteryChartView;

/**
 * Halaman detail satu grafik baterai: chart besar interaktif (crosshair),
 * baris label rentang waktu independen dari panel monitor (dikontrol seekbar
 * transparan di atasnya), dan kartu statistik (Min / Max / Rata-rata / Δ)
 * yang dihitung dari data periode tampil.
 */
public class BatteryChartDetailActivity extends AppCompatActivity {

    private static final String EXTRA_SERIES_TYPE = "seriesType";
    private static final String EXTRA_WINDOW_MS = "windowMs";
    private static final String STATE_WINDOW_MS = "stateWindowMs";
    private static final long REFRESH_INTERVAL_MS = 5000L;
    private static final int CHART_QUERY_POINTS = 800;

    private static final String[] SERIES_TITLES = {
            "Suhu", "Persentase", "Daya", "Tegangan", "Arus"};

    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService queryExecutor = Executors.newSingleThreadExecutor();
    private final SimpleDateFormat fmtTime = new SimpleDateFormat("MM/dd HH:mm", Locale.US);
    private final SimpleDateFormat fmtDetail = new SimpleDateFormat("MM/dd HH:mm:ss", Locale.US);

    private BatteryChartView chartView;
    private TextView titleView;
    private TextView subtitleView;
    private TextView statsTitle;
    private TextView statMin;
    private TextView statMax;
    private TextView statAvg;
    private TextView statDelta;
    private TextView[] rangeTicks;
    private SeekBar rangeSeek;

    private int seriesType;
    private long windowMs;
    private int accentColor;
    private boolean queryInFlight = false;
    private boolean resumed = false;

    /** Buka halaman detail untuk satu metrik; rentang awal bebas (tidak mengubah panel). */
    public static void start(Context context, int seriesType, long initialWindowMs) {
        Intent intent = new Intent(context, BatteryChartDetailActivity.class);
        intent.putExtra(EXTRA_SERIES_TYPE, seriesType);
        intent.putExtra(EXTRA_WINDOW_MS, initialWindowMs);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery_chart_detail);

        seriesType = getIntent().getIntExtra(EXTRA_SERIES_TYPE, BatteryChartView.SERIES_PERCENT);
        if (seriesType < BatteryChartView.SERIES_TEMP || seriesType > BatteryChartView.SERIES_CURRENT) {
            seriesType = BatteryChartView.SERIES_PERCENT;
        }
        long initialWindow = getIntent().getLongExtra(EXTRA_WINDOW_MS, BatteryChartView.WINDOW_5M);
        windowMs = savedInstanceState != null
                ? savedInstanceState.getLong(STATE_WINDOW_MS, initialWindow)
                : initialWindow;
        if (!isKnownWindow(windowMs)) windowMs = initialWindow;

        titleView = findViewById(R.id.chartDetailTitle);
        subtitleView = findViewById(R.id.chartDetailSubtitle);
        statsTitle = findViewById(R.id.chartStatsTitle);
        statMin = findViewById(R.id.chartStatMinValue);
        statMax = findViewById(R.id.chartStatMaxValue);
        statAvg = findViewById(R.id.chartStatAvgValue);
        statDelta = findViewById(R.id.chartStatDeltaValue);
        chartView = findViewById(R.id.chartDetailView);

        accentColor = getResources().getColor(BatteryChartView.seriesColorRes(seriesType));
        titleView.setText(SERIES_TITLES[seriesType]);
        titleView.setTextColor(accentColor);
        statsTitle.setTextColor(accentColor);

        findViewById(R.id.chartDetailBack).setOnClickListener(v -> finish());

        chartView.setSeriesType(seriesType);
        chartView.setInteractive(true);
        chartView.setOnScrubListener((index, snapshot) -> {
            if (snapshot == null) return;
            subtitleView.setText(BatteryChartView.formatValue(seriesType,
                    BatteryChartView.valueOf(seriesType, snapshot))
                    + " · " + fmtDetail.format(new Date(snapshot.time)));
        });

        TextView radCrosshair = findViewById(R.id.radCrosshair);
        TextView radPan = findViewById(R.id.radPan);
        int labelColor = getResources().getColor(R.color.bat_monitor_label);

        radCrosshair.setOnClickListener(v -> {
            chartView.setMode(BatteryChartView.MODE_CROSSHAIR);
            radCrosshair.setTextColor(accentColor);
            radCrosshair.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            radPan.setTextColor(labelColor);
            radPan.setTypeface(android.graphics.Typeface.DEFAULT);
        });

        radPan.setOnClickListener(v -> {
            chartView.setMode(BatteryChartView.MODE_PAN);
            radPan.setTextColor(accentColor);
            radPan.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            radCrosshair.setTextColor(labelColor);
            radCrosshair.setTypeface(android.graphics.Typeface.DEFAULT);
        });

        radCrosshair.setTextColor(accentColor);
        radCrosshair.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        radPan.setTextColor(labelColor);
        radPan.setTypeface(android.graphics.Typeface.DEFAULT);

        SeekBar zoomSeek = findViewById(R.id.chartDetailZoomSeek);
        zoomSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;
                float maxZoom = Math.max(1f, (float) windowMs / 30_000f);
                float zoom = 1f + (progress / 100f) * (maxZoom - 1f);
                chartView.setZoomLevel(zoom);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        setupRangeControls();
        applyWindow(windowMs, false);
    }

    /** Baris label rentang berperilaku layaknya tombol: seekbar transparan menimpanya. */
    private void setupRangeControls() {
        LinearLayout labelRow = findViewById(R.id.chartRangeLabelRow);
        rangeTicks = new TextView[labelRow.getChildCount()];
        for (int i = 0; i < labelRow.getChildCount(); i++) {
            rangeTicks[i] = (TextView) labelRow.getChildAt(i);
        }
        rangeSeek = findViewById(R.id.chartDetailRangeSeek);
        rangeSeek.setMax(BatteryChartHistoryController.CHART_WINDOWS.length - 1);
        // Slider transparan menimpa baris label: padding 5% lebar membuat setiap
        // langkah jatuh tepat di tengah labelnya (10 label = lebar 10% per sel).
        rangeSeek.addOnLayoutChangeListener((v, left, top, right, bottom,
                oldLeft, oldTop, oldRight, oldBottom) -> {
            int pad = (right - left) / 20;
            if (pad > 0 && rangeSeek.getPaddingLeft() != pad) {
                rangeSeek.setPadding(pad, 0, pad, 0);
            }
        });
        rangeSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;
                highlightTick(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                applyWindow(BatteryChartHistoryController.CHART_WINDOWS[seekBar.getProgress()], true);
            }
        });
    }

    private void highlightTick(int index) {
        int labelColor = getResources().getColor(R.color.bat_monitor_label);
        for (int i = 0; i < rangeTicks.length; i++) {
            boolean active = i == index;
            rangeTicks[i].setTextColor(active ? accentColor : labelColor);
            rangeTicks[i].setTypeface(active ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        }
    }

    private boolean isKnownWindow(long ms) {
        for (long w : BatteryChartHistoryController.CHART_WINDOWS) {
            if (w == ms) return true;
        }
        return false;
    }

    private void applyWindow(long ms, boolean requery) {
        windowMs = ms;
        chartView.resetZoom();
        SeekBar zoomSeek = findViewById(R.id.chartDetailZoomSeek);
        if (zoomSeek != null) zoomSeek.setProgress(0);
        for (int i = 0; i < BatteryChartHistoryController.CHART_WINDOWS.length; i++) {
            if (BatteryChartHistoryController.CHART_WINDOWS[i] == ms) {
                rangeSeek.setProgress(i);
                highlightTick(i);
                break;
            }
        }
        if (requery) queryNow();
    }

    private void queryNow() {
        if (queryInFlight || isFinishing() || isDestroyed()) return;
        queryInFlight = true;
        final long now = System.currentTimeMillis();
        final long from = now - windowMs;
        queryExecutor.execute(() -> {
            BatteryReading.Snapshot[] data =
                    BatteryHistoryDb.get(BatteryChartDetailActivity.this).queryChart(from, now, CHART_QUERY_POINTS);
            uiHandler.post(() -> {
                queryInFlight = false;
                if (isFinishing() || isDestroyed()) return;
                renderData(data);
            });
        });
    }

    private void renderData(BatteryReading.Snapshot[] data) {
        chartView.setData(data);
        chartView.setWindowMs(windowMs);
        updateStats(data);
        if (!chartView.hasSelection()) updateHeaderLatest(data);
    }

    private void updateHeaderLatest(BatteryReading.Snapshot[] data) {
        if (data == null || data.length == 0) {
            subtitleView.setText("Belum ada data");
            return;
        }
        BatteryReading.Snapshot last = data[data.length - 1];
        subtitleView.setText("Terakhir " + timeFormat().format(new Date(last.time))
                + " · " + BatteryChartView.formatValue(seriesType,
                BatteryChartView.valueOf(seriesType, last)));
    }

    private void updateStats(BatteryReading.Snapshot[] data) {
        String empty = "—";
        statMin.setText(empty);
        statMax.setText(empty);
        statAvg.setText(empty);
        statDelta.setText(empty);
        if (data == null || data.length == 0) return;
        float min = Float.MAX_VALUE;
        float max = -Float.MAX_VALUE;
        double sum = 0;
        for (BatteryReading.Snapshot s : data) {
            float v = BatteryChartView.valueOf(seriesType, s);
            if (v < min) min = v;
            if (v > max) max = v;
            sum += v;
        }
        float avg = (float) (sum / data.length);
        float delta = BatteryChartView.valueOf(seriesType, data[data.length - 1])
                - BatteryChartView.valueOf(seriesType, data[0]);
        statMin.setText(BatteryChartView.formatValue(seriesType, min));
        statMax.setText(BatteryChartView.formatValue(seriesType, max));
        statAvg.setText(BatteryChartView.formatValue(seriesType, avg));
        statDelta.setText((delta >= 0 ? "+" : "") + BatteryChartView.formatValue(seriesType, delta));
    }

    private SimpleDateFormat timeFormat() {
        return fmtTime;
    }

    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (!resumed) return;
            // Tunda pembaruan saat jari sedang menelusuri grafik.
            if (!chartView.isScrubbing() && !queryInFlight) queryNow();
            uiHandler.postDelayed(this, REFRESH_INTERVAL_MS);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        resumed = true;
        queryNow();
        uiHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL_MS);
    }

    @Override
    protected void onPause() {
        super.onPause();
        resumed = false;
        uiHandler.removeCallbacks(refreshRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        queryExecutor.shutdown();
        uiHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onSaveInstanceState(@Nullable Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(STATE_WINDOW_MS, windowMs);
    }
}
