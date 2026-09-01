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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import exp.ftxt.R;
import exp.ftxt.features.battery_stats.BatteryHistoryDb;
import exp.ftxt.features.battery_stats.BatteryReading;
import exp.ftxt.shared.ui.ActivityBarView;
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
    /** Buffer di kedua sisi rentang zoom (dikali lebar jendela tampil) agar pan tetap mulus. */
    private static final float VIEWPORT_BUFFER = 1.0f;

    private static final String[] SERIES_TITLES = {
            "Suhu", "Persentase", "Daya", "Tegangan", "Arus"};

    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService queryExecutor = Executors.newSingleThreadExecutor();
    private final SimpleDateFormat fmtTime = new SimpleDateFormat("MM/dd HH:mm", Locale.US);
    private final SimpleDateFormat fmtDetail = new SimpleDateFormat("MM/dd HH:mm:ss", Locale.US);

    private BatteryChartView chartView;
    private ActivityBarView activityBarView;
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
    /** Ujung kanan scrollable (waktu sampel terakhir saat halaman dibuka) — stabil selama zoom/pan. */
    private long anchorEndMs = 0L;
    private long[] pendingViewport;

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
        activityBarView = findViewById(R.id.chartActivityBar);

        accentColor = getResources().getColor(BatteryChartView.seriesColorRes(seriesType));
        titleView.setText(SERIES_TITLES[seriesType]);
        titleView.setTextColor(accentColor);
        statsTitle.setTextColor(accentColor);

        activityBarView.setColors(
                getResources().getColor(R.color.bat_activity_screen_on),
                getResources().getColor(R.color.bat_activity_screen_off),
                getResources().getColor(R.color.bat_activity_charging),
                getResources().getColor(R.color.bat_activity_discharging),
                getResources().getColor(R.color.bat_activity_label));

        findViewById(R.id.chartDetailBack).setOnClickListener(v -> finish());

        chartView.setSeriesType(seriesType);
        chartView.setInteractive(true);
        chartView.setOnScrubListener((index, snapshot) -> {
            if (snapshot == null) return;
            subtitleView.setText(BatteryChartView.formatValue(seriesType,
                    BatteryChartView.valueOf(seriesType, snapshot))
                    + " · " + fmtDetail.format(new Date(snapshot.time)));
        });
        chartView.setOnPanSettledListener(this::refreshViewportAfterPan);

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
                if (zoom <= 1.001f) {
                    if (chartView.hasViewport()) {
                        chartView.clearViewport();
                        queryNow();
                    }
                    return;
                }
                long anchorEnd = ensureAnchorEnd();
                long targetW = Math.min(windowMs,
                        Math.max(BatteryChartView.getMinVisibleMs(), (long) (windowMs / zoom)));
                long spanStart = anchorEnd - windowMs;
                long mid = (chartView.getVisibleStartMs() + chartView.getVisibleEndMs()) / 2L;
                long visStart = Math.max(spanStart,
                        Math.min(anchorEnd - targetW, mid - targetW / 2L));
                requeryViewport(visStart, visStart + targetW);
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
        chartView.clearViewport();
        chartView.setWindowMs(ms);
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
        if (chartView.hasViewport()) {
            requeryViewport(chartView.getVisibleStartMs(), chartView.getVisibleEndMs());
            return;
        }
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

    /**
     * Query ulang data untuk jendela zoom [visStart, visEnd], dengan buffer di kedua
     * sisi (masing-masing selebar VIEWPORT_BUFFER × lebar jendela) agar pan tetap
     * mulus tanpa query berulang. Rentang data dikurung ke [anchorEnd - windowMs, anchorEnd]
     * sehingga tampilan tidak pernah melesat melewati awal/akhir rentang navigasi.
     */
    private void requeryViewport(long visStart, long visEnd) {
        if (visEnd <= visStart) return;
        if (queryInFlight) {
            pendingViewport = new long[]{visStart, visEnd};
            return;
        }
        long hi = ensureAnchorEnd();
        long lo = hi - windowMs;
        long span = visEnd - visStart;
        // Jendela tampil dijepit ke rentang navigasi [lo, hi] agar tidak melesat
        // melewati awal/akhir data walau refresh menambahkan sampel yang lebih baru.
        long vEnd = Math.min(hi, visEnd);
        long vStart = Math.max(lo, Math.min(visStart, vEnd - span));
        long buffer = Math.max(BatteryChartView.getMinVisibleMs(), (long) (span * VIEWPORT_BUFFER));
        final long dataStart = Math.max(lo, vStart - buffer);
        final long dataEnd = Math.min(hi, vEnd + buffer);
        final long start = vStart;
        final long end = vEnd;
        queryInFlight = true;
        queryExecutor.execute(() -> {
            BatteryReading.Snapshot[] data = BatteryHistoryDb.get(BatteryChartDetailActivity.this)
                    .queryChart(dataStart, dataEnd, CHART_QUERY_POINTS);
            uiHandler.post(() -> {
                queryInFlight = false;
                if (isFinishing() || isDestroyed()) return;
                chartView.setViewport(start, end, dataStart, dataEnd);
                chartView.setData(data);
                updateStatsRange(data, start, end);
                updateHeaderLatestRange(data, start, end);
                refreshActivityBar(start, end);
                if (pendingViewport != null) {
                    long[] p = pendingViewport;
                    pendingViewport = null;
                    requeryViewport(p[0], p[1]);
                }
            });
        });
    }

    /** Ujung kanan navigasi (waktu sampel terakhir) — stabil selama zoom/pan. */
    private long ensureAnchorEnd() {
        if (anchorEndMs == 0L) {
            anchorEndMs = chartView.getDataEndTime();
        }
        return anchorEndMs;
    }

    /** Setelah jari dilepas dari pan, pindahkan buffer data mengikuti jendela tampil baru. */
    private void refreshViewportAfterPan() {
        requeryViewport(chartView.getVisibleStartMs(), chartView.getVisibleEndMs());
    }

    private void renderData(BatteryReading.Snapshot[] data) {
        if (data != null && data.length > 0 && anchorEndMs == 0L) {
            anchorEndMs = data[data.length - 1].time;
        }
        chartView.setData(data);
        chartView.setWindowMs(windowMs);
        updateStats(data);
        if (!chartView.hasSelection()) updateHeaderLatest(data);
        refreshActivityBar(chartView.getVisibleStartMs(), chartView.getVisibleEndMs());
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

    /** Nilai "Terakhir" dari titik terakhir dalam rentang tampil (mode zoom/pan). */
    private void updateHeaderLatestRange(BatteryReading.Snapshot[] data, long fromMs, long toMs) {
        BatteryReading.Snapshot last = null;
        if (data != null) {
            for (BatteryReading.Snapshot s : data) {
                if (s.time < fromMs) continue;
                if (s.time > toMs) break;
                last = s;
            }
        }
        if (last == null) {
            subtitleView.setText("Belum ada data");
            return;
        }
        subtitleView.setText("Terakhir " + timeFormat().format(new Date(last.time))
                + " · " + BatteryChartView.formatValue(seriesType,
                BatteryChartView.valueOf(seriesType, last)));
    }

    private void refreshActivityBar(long fromMs, long toMs) {
        if (fromMs >= toMs || isFinishing() || isDestroyed()) return;
        final long f = fromMs, t = toMs;
        queryExecutor.execute(() -> {
            List<ActivityBarView.ActivitySegment> screenOn = new ArrayList<>();
            List<ActivityBarView.ActivitySegment> screenOff = new ArrayList<>();
            List<ActivityBarView.ActivitySegment> charging = new ArrayList<>();
            List<ActivityBarView.ActivitySegment> discharging = new ArrayList<>();
            queryActivitySegments(f, t, screenOn, screenOff, charging, discharging);
            uiHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                activityBarView.setRange(f, t);
                activityBarView.setActivityData(screenOn, screenOff, charging, discharging);
            });
        });
    }

    /**
     * Query segmen aktivitas dari tabel activity_log pada rentang [fromMs, toMs].
     * Tabel ini mencatat titik saat status berubah: 2 = mengisi daya, 1 = layar aktif,
     * 0 = layar mati. Status berlaku dari titiknya sampai titik berikutnya. State yang
     * berlaku tepat di fromMs diambil dari baris terakhir ≤ fromMs (default layar mati).
     * "Pengurasan" = seluruh waktu tidak mengisi (layar aktif ATAU layar mati).
     */
    private void queryActivitySegments(long fromMs, long toMs,
                                      List<ActivityBarView.ActivitySegment> screenOn,
                                      List<ActivityBarView.ActivitySegment> screenOff,
                                      List<ActivityBarView.ActivitySegment> charging,
                                      List<ActivityBarView.ActivitySegment> discharging) {
        BatteryHistoryDb db = BatteryHistoryDb.get(BatteryChartDetailActivity.this);
        List<BatteryHistoryDb.ActivityLog> logs = db.queryActivityLog(Long.MIN_VALUE, toMs);

        int state = ActivityBarView.STATE_SCREEN_OFF;
        long segStart = fromMs;

        for (BatteryHistoryDb.ActivityLog log : logs) {
            if (log.time <= fromMs) {
                state = mapActivityStatus(log.status);
                segStart = fromMs;
                continue;
            }
            if (log.time > toMs) break;
            int next = mapActivityStatus(log.status);
            if (next != state) {
                addSegment(state, segStart, log.time, screenOn, screenOff, charging, discharging);
                state = next;
                segStart = log.time;
            }
        }

        addSegment(state, segStart, toMs, screenOn, screenOff, charging, discharging);
    }

    private static int mapActivityStatus(int activityLogStatus) {
        switch (activityLogStatus) {
            case 2: return ActivityBarView.STATE_CHARGING;
            case 1: return ActivityBarView.STATE_SCREEN_ON;
            default: return ActivityBarView.STATE_SCREEN_OFF;
        }
    }

    private static void addSegment(int state, long start, long end,
                                   List<ActivityBarView.ActivitySegment> screenOn,
                                   List<ActivityBarView.ActivitySegment> screenOff,
                                   List<ActivityBarView.ActivitySegment> charging,
                                   List<ActivityBarView.ActivitySegment> discharging) {
        if (end <= start) return;
        ActivityBarView.ActivitySegment seg = new ActivityBarView.ActivitySegment(start, end, state);
        switch (state) {
            case ActivityBarView.STATE_SCREEN_ON:
                screenOn.add(seg);
                discharging.add(seg);
                break;
            case ActivityBarView.STATE_SCREEN_OFF:
                screenOff.add(seg);
                discharging.add(seg);
                break;
            case ActivityBarView.STATE_CHARGING:
                charging.add(seg);
                break;
        }
    }

    private void updateStats(BatteryReading.Snapshot[] data) {
        updateStatsRange(data,
                data != null && data.length > 0 ? data[0].time : 0L,
                data != null && data.length > 0 ? data[data.length - 1].time : 0L);
    }

    /** Statistik Min / Max / Rata-rata / Δ dari titik-titik dalam rentang [fromMs, toMs]. */
    private void updateStatsRange(BatteryReading.Snapshot[] data, long fromMs, long toMs) {
        String empty = "—";
        statMin.setText(empty);
        statMax.setText(empty);
        statAvg.setText(empty);
        statDelta.setText(empty);
        if (data == null || data.length == 0) return;
        float min = Float.MAX_VALUE;
        float max = -Float.MAX_VALUE;
        double sum = 0;
        int count = 0;
        BatteryReading.Snapshot first = null;
        BatteryReading.Snapshot last = null;
        for (BatteryReading.Snapshot s : data) {
            if (s.time < fromMs || s.time > toMs) continue;
            float v = BatteryChartView.valueOf(seriesType, s);
            if (v < min) min = v;
            if (v > max) max = v;
            sum += v;
            count++;
            if (first == null) first = s;
            last = s;
        }
        if (count == 0) return;
        float avg = (float) (sum / count);
        float delta = BatteryChartView.valueOf(seriesType, last)
                - BatteryChartView.valueOf(seriesType, first);
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
