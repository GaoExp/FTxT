package exp.ftxt.shared.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import exp.ftxt.R;
import exp.ftxt.features.battery_stats.BatteryReading;

/**
 * Line chart riwayat satu metrik baterai (Suhu / Persen / Daya) digambar via Canvas.
 * Setiap metrik memakai instance terpisah ({@link #setSeriesType(int)}); sumbu Y
 * dinormalisasi otomatis (Persen selalu 0–100), label nilai max/tengah/min di kiri,
 * nilai terkini di ujung garis, sumbu waktu di bawah.
 */
public class BatteryChartView extends View {

    public static final long WINDOW_2M = 2L * 60_000L;
    public static final long WINDOW_5M = 5L * 60_000L;
    public static final long WINDOW_10M = 10L * 60_000L;
    public static final long WINDOW_15M = 15L * 60_000L;
    public static final long WINDOW_30M = 30L * 60_000L;
    public static final long WINDOW_1H = 60L * 60_000L;
    public static final long WINDOW_3H = 3L * 60L * 60_000L;
    public static final long WINDOW_6H = 6L * 60L * 60_000L;
    public static final long WINDOW_12H = 12L * 60L * 60_000L;
    public static final long WINDOW_24H = 24L * 60L * 60_000L;

    public static final int SERIES_TEMP = 0;
    public static final int SERIES_PERCENT = 1;
    public static final int SERIES_POWER = 2;
    public static final int SERIES_VOLTAGE = 3;
    public static final int SERIES_CURRENT = 4;

    private BatteryReading.Snapshot[] samples = new BatteryReading.Snapshot[0];
    private long windowMs = WINDOW_5M;
    private int seriesType = SERIES_TEMP;

    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint emptyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final SimpleDateFormat timeShortFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
    private final SimpleDateFormat timeMediumFormat = new SimpleDateFormat("HH:mm", Locale.US);
    private final SimpleDateFormat timeLongFormat = new SimpleDateFormat("dd/MM HH:mm", Locale.US);

    public BatteryChartView(Context context) {
        this(context, null);
    }

    public BatteryChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        gridPaint.setColor(getResources().getColor(R.color.bat_chart_grid));
        gridPaint.setStrokeWidth(dp(1));

        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeWidth(dp(1.6f));

        dotPaint.setStyle(Paint.Style.FILL);

        labelPaint.setColor(getResources().getColor(R.color.bat_monitor_label));
        labelPaint.setTextSize(sp(9));

        emptyPaint.setColor(getResources().getColor(R.color.bat_monitor_label));
        emptyPaint.setTextSize(sp(12));
        emptyPaint.setTextAlign(Paint.Align.CENTER);

        applySeriesStyle();
    }

    public void setData(BatteryReading.Snapshot[] samples) {
        this.samples = samples != null ? samples : new BatteryReading.Snapshot[0];
        invalidate();
    }

    public void setWindowMs(long windowMs) {
        this.windowMs = windowMs;
        invalidate();
    }

    public void setSeriesType(int seriesType) {
        this.seriesType = seriesType;
        applySeriesStyle();
        invalidate();
    }

    private void applySeriesStyle() {
        int colorRes;
        switch (seriesType) {
            case SERIES_TEMP: colorRes = R.color.bat_chart_temp; break;
            case SERIES_PERCENT: colorRes = R.color.bat_chart_percent; break;
            case SERIES_VOLTAGE: colorRes = R.color.bat_chart_voltage; break;
            case SERIES_CURRENT: colorRes = R.color.bat_chart_current; break;
            default: colorRes = R.color.bat_chart_power; break;
        }
        linePaint.setColor(getResources().getColor(colorRes));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();
        float padLeft = dp(40);
        float padRight = dp(8);
        float padTop = dp(6);
        float padBottom = dp(18);
        float plotW = w - padLeft - padRight;
        float plotH = h - padTop - padBottom;
        if (plotW <= 0 || plotH <= 0) return;

        for (int i = 0; i < 4; i++) {
            float y = padTop + plotH * i / 3f;
            canvas.drawLine(padLeft, y, padLeft + plotW, y, gridPaint);
        }

        int n = samples.length;
        long endT = n > 0 ? samples[n - 1].time : System.currentTimeMillis();
        if (n == 0) {
            drawTimeLabels(canvas, padLeft, plotW, h, padBottom, endT - windowMs, endT);
            canvas.drawText("Belum ada data grafik", padLeft + plotW / 2f, h / 2f, emptyPaint);
            return;
        }

        long startT = endT - windowMs;
        int first = n;
        while (first > 0 && samples[first - 1].time >= startT) first--;
        int count = n - first;
        drawTimeLabels(canvas, padLeft, plotW, h, padBottom,
                count >= 2 ? samples[first].time : startT, endT);
        if (count < 2) {
            canvas.drawText("Belum ada data grafik", padLeft + plotW / 2f, h / 2f, emptyPaint);
            return;
        }

        float min = Float.MAX_VALUE;
        float max = -Float.MAX_VALUE;
        for (int k = 0; k < count; k++) {
            float v = valueAt(first + k);
            if (v < min) min = v;
            if (v > max) max = v;
        }
        boolean fixedZero100 = seriesType == SERIES_PERCENT;
        if (fixedZero100) {
            min = 0f;
            max = 100f;
        } else {
            float range = max - min;
            if (seriesType == SERIES_TEMP) {
                if (max < 50f) max = 50f;
                if (min >= max) min = max - 1f;
            } else if (range < 1e-3f) {
                min -= Math.max(1f, Math.abs(min) * 0.1f);
                max += Math.max(1f, Math.abs(max) * 0.1f);
            } else {
                min -= range * 0.08f;
                max += range * 0.08f;
            }
        }
        float vRange = max - min;
        if (vRange <= 0f) vRange = 1f;

        labelPaint.setTextAlign(Paint.Align.LEFT);
        float axisX = dp(2);
        float labelH = labelPaint.getTextSize();
        canvas.drawText(fmt(max), axisX, padTop + labelH, labelPaint);
        canvas.drawText(fmt((min + max) / 2f), axisX, padTop + plotH / 2f + labelH / 3f, labelPaint);
        canvas.drawText(fmt(min), axisX, padTop + plotH, labelPaint);

        float spanMs = Math.max(1f, endT - samples[first].time);
        long baseT = samples[first].time;
        path.rewind();
        boolean started = false;
        float lastX = 0f;
        float lastY = 0f;
        for (int k = 0; k < count; k++) {
            int idx = first + k;
            float x = padLeft + plotW * ((samples[idx].time - baseT) / spanMs);
            float norm = (valueAt(idx) - min) / vRange;
            norm = Math.max(0f, Math.min(1f, norm));
            float y = padTop + plotH * (1f - norm);
            if (!started) {
                path.moveTo(x, y);
                started = true;
            } else {
                path.lineTo(x, y);
            }
            lastX = x;
            lastY = y;
        }
        canvas.drawPath(path, linePaint);

        dotPaint.setColor(linePaint.getColor());
        canvas.drawCircle(lastX, lastY, dp(2.5f), dotPaint);

        valueLabelPaint.setTextAlign(Paint.Align.RIGHT);
        valueLabelPaint.setTextSize(sp(10));
        String txt = fmt(valueAt(first + count - 1));
        float ty = (lastY - dp(16) < padTop) ? lastY + dp(15) : lastY - dp(7);
        valueLabelPaint.setColor(labelPaint.getColor());
        canvas.drawText(txt, Math.max(lastX, padLeft + plotW) - dp(5), ty, valueLabelPaint);
    }

    private final Paint valueLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float valueAt(int idx) {
        switch (seriesType) {
            case SERIES_TEMP: return samples[idx].tempC;
            case SERIES_PERCENT: return samples[idx].percent;
            case SERIES_VOLTAGE: return samples[idx].voltageV;
            case SERIES_CURRENT: return samples[idx].currentMa;
            default: return (float) samples[idx].powerW;
        }
    }

    private String fmt(float v) {
        switch (seriesType) {
            case SERIES_TEMP: return String.format(Locale.US, "%.1f°", v);
            case SERIES_PERCENT: return String.format(Locale.US, "%.0f%%", v);
            case SERIES_VOLTAGE: return String.format(Locale.US, "%.2fV", v);
            case SERIES_CURRENT: return Math.abs(v) >= 1000f
                    ? String.format(Locale.US, "%.2fA", v / 1000f)
                    : String.format(Locale.US, "%.0fmA", v);
            default: return String.format(Locale.US, v < 10f ? "%.2fW" : "%.1fW", v);
        }
    }

    private void drawTimeLabels(Canvas canvas, float padLeft, float plotW, float h,
                                float padBottom, long startT, long endT) {
        SimpleDateFormat format = windowMs >= WINDOW_6H ? timeLongFormat
                : windowMs >= WINDOW_1H ? timeMediumFormat
                : timeShortFormat;
        float y = h - padBottom / 2f + labelPaint.getTextSize() / 3f;
        labelPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(format.format(new Date(startT)), padLeft, y, labelPaint);
        labelPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(format.format(new Date(endT)), padLeft + plotW, y, labelPaint);
    }

    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }

    private float sp(float v) {
        return v * getResources().getDisplayMetrics().scaledDensity;
    }
}
