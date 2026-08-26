package exp.ftxt.shared.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.graphics.ColorUtils;

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

    public static final long WINDOW_5M = 5L * 60_000L;
    public static final long WINDOW_10M = 10L * 60_000L;
    public static final long WINDOW_30M = 30L * 60_000L;
    public static final long WINDOW_1H = 60L * 60_000L;
    public static final long WINDOW_3H = 3L * 60L * 60_000L;
    public static final long WINDOW_6H = 6L * 60L * 60_000L;
    public static final long WINDOW_12H = 12L * 60L * 60_000L;
    public static final long WINDOW_24H = 24L * 60L * 60_000L;
    public static final long WINDOW_36H = 36L * 60L * 60_000L;
    public static final long WINDOW_48H = 48L * 60L * 60_000L;

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

    /** Callback saat titik data terpilih berubah lewat sentuhan (crosshair). */
    public interface OnScrubListener {
        void onScrub(int index, BatteryReading.Snapshot snapshot);
    }

    private boolean interactive = false;
    private boolean scrubbing = false;
    private int selectedIndex = -1;
    private OnScrubListener scrubListener;

    // Geometri render terakhir — dipakai memetakan posisi sentuhan ke titik data.
    private float lastPadLeft, lastPlotW, lastPadTop, lastPlotH;
    private long lastBaseT;
    private float lastSpanMs = 1f;
    private int lastFirst, lastCount, lastN;
    private float lastMin, lastVRange = 1f;

    private final Paint crosshairPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint crosshairTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

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

        crosshairPaint.setStyle(Paint.Style.STROKE);
        crosshairPaint.setStrokeWidth(dp(1));
        crosshairPaint.setColor(0x668A8A8E);

        bubblePaint.setStyle(Paint.Style.FILL);

        crosshairTextPaint.setColor(Color.WHITE);
        crosshairTextPaint.setTextSize(sp(10));
        crosshairTextPaint.setTextAlign(Paint.Align.CENTER);

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

    /** Aktifkan interaksi sentuhan (crosshair) pada grafik ini — default mati. */
    public void setInteractive(boolean interactive) {
        this.interactive = interactive;
        if (!interactive) {
            selectedIndex = -1;
            scrubbing = false;
        }
        invalidate();
    }

    /** True saat jari sedang menekan grafik (pembaruan data sebaiknya ditunda). */
    public boolean isScrubbing() {
        return scrubbing;
    }

    /** True bila crosshair sedang menampilkan titik data terpilih. */
    public boolean hasSelection() {
        return interactive && selectedIndex >= 0;
    }

    public void setOnScrubListener(OnScrubListener listener) {
        this.scrubListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!interactive || lastCount < 2 || lastPlotW <= 0f) return super.onTouchEvent(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                scrubbing = true;
                if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(true);
                selectNearest(event.getX());
                return true;
            case MotionEvent.ACTION_MOVE:
                selectNearest(event.getX());
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                scrubbing = false;
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    private void selectNearest(float touchX) {
        float x = Math.max(lastPadLeft, Math.min(lastPadLeft + lastPlotW, touchX));
        long target = lastBaseT + (long) ((x - lastPadLeft) / lastPlotW * lastSpanMs);
        int best = -1;
        long bestDiff = Long.MAX_VALUE;
        for (int i = lastFirst; i < lastFirst + lastCount && i < lastN; i++) {
            long diff = Math.abs(samples[i].time - target);
            if (diff < bestDiff) {
                bestDiff = diff;
                best = i;
            }
        }
        if (best >= 0 && best != selectedIndex) {
            selectedIndex = best;
            invalidate();
            notifyScrub();
        }
    }

    private void notifyScrub() {
        if (scrubListener == null) return;
        scrubListener.onScrub(selectedIndex,
                selectedIndex >= 0 && selectedIndex < samples.length ? samples[selectedIndex] : null);
    }

    private void applySeriesStyle() {
        linePaint.setColor(getResources().getColor(seriesColorRes(seriesType)));
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
            lastCount = 0;
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
            lastCount = 0;
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
                if (max < 43f) max = 43f;
                if (min < 33f) min = 33f;
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

        lastPadLeft = padLeft;
        lastPlotW = plotW;
        lastPadTop = padTop;
        lastPlotH = plotH;
        lastBaseT = baseT;
        lastSpanMs = spanMs;
        lastFirst = first;
        lastCount = count;
        lastN = n;
        lastMin = min;
        lastVRange = vRange;
        if (interactive) {
            if (selectedIndex < first) selectedIndex = first;
            else if (selectedIndex >= first + count) selectedIndex = first + count - 1;
        }

        float lastX = 0f;
        float lastY = 0f;
        if (seriesType == SERIES_TEMP) {
            // Garis Suhu: warna tiap segmen mengikuti nilai suhu titik datanya
            // (putih dingin ekstrem → ice blue → hijau → oranye → merah panas).
            path.rewind();
            float prevX = padLeft + plotW * ((samples[first].time - baseT) / spanMs);
            float prevY = pointY(first, min, vRange, padTop, plotH);
            for (int k = 1; k < count; k++) {
                int idx = first + k;
                float x = padLeft + plotW * ((samples[idx].time - baseT) / spanMs);
                float y = pointY(idx, min, vRange, padTop, plotH);
                linePaint.setColor(tempColor(valueAt(idx - 1)));
                canvas.drawLine(prevX, prevY, x, y, linePaint);
                prevX = x;
                prevY = y;
                lastX = x;
                lastY = y;
            }
            linePaint.setColor(tempColor(valueAt(first + count - 1)));
        } else if (isStatusColored()) {
            // Garis Persentase/Tegangan/Arus diwarnai per segmen sesuai status baterai
            // titik data: hijau saat mengisi, merah saat tidak. Segmen transisi ikut run sebelumnya.
            boolean charging = samples[first].isCharging();
            linePaint.setColor(lineColor(charging));
            path.rewind();
            path.moveTo(padLeft + plotW * ((samples[first].time - baseT) / spanMs),
                    pointY(first, min, vRange, padTop, plotH));
            for (int k = 1; k < count; k++) {
                int idx = first + k;
                float x = padLeft + plotW * ((samples[idx].time - baseT) / spanMs);
                float y = pointY(idx, min, vRange, padTop, plotH);
                path.lineTo(x, y);
                lastX = x;
                lastY = y;
                boolean c = samples[idx].isCharging();
                if (c != charging) {
                    canvas.drawPath(path, linePaint);
                    path.rewind();
                    path.moveTo(x, y);
                    charging = c;
                    linePaint.setColor(lineColor(charging));
                }
            }
        } else {
            path.rewind();
            boolean started = false;
            for (int k = 0; k < count; k++) {
                int idx = first + k;
                float x = padLeft + plotW * ((samples[idx].time - baseT) / spanMs);
                float y = pointY(idx, min, vRange, padTop, plotH);
                if (!started) {
                    path.moveTo(x, y);
                    started = true;
                } else {
                    path.lineTo(x, y);
                }
                lastX = x;
                lastY = y;
            }
        }
        canvas.drawPath(path, linePaint);

        dotPaint.setColor(linePaint.getColor());
        canvas.drawCircle(lastX, lastY, dp(2.5f), dotPaint);

        valueLabelPaint.setTextAlign(Paint.Align.RIGHT);
        valueLabelPaint.setTextSize(sp(10));
        String txt = fmt(valueAt(first + count - 1));
        float tx = Math.max(lastX, padLeft + plotW) - dp(5);
        float ty = (lastY - dp(28) < padTop) ? lastY + dp(17) : lastY - dp(12);
        // Chip latar gelap semi-opaque agar angka tetap terbaca saat ditumpuk garis.
        float tw = valueLabelPaint.measureText(txt);
        Paint.FontMetrics fm = valueLabelPaint.getFontMetrics();
        valueTagBgPaint.setColor(0x66202124);
        canvas.drawRoundRect(tx - tw - dp(4), ty + fm.ascent - dp(2),
                tx + dp(4), ty + fm.descent + dp(2), dp(4), dp(4), valueTagBgPaint);
        valueLabelPaint.setColor(Color.WHITE);
        canvas.drawText(txt, tx, ty, valueLabelPaint);

        if (interactive && selectedIndex >= 0) drawCrosshair(canvas);
    }

    private final Paint valueLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valueTagBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    /** Nilai numerik mentah satu snapshot sesuai jenis seri — dipakai juga halaman detail. */
    public static float valueOf(int seriesType, BatteryReading.Snapshot s) {
        switch (seriesType) {
            case SERIES_TEMP: return s.tempC;
            case SERIES_PERCENT: return s.percent;
            case SERIES_VOLTAGE: return s.voltageV;
            case SERIES_CURRENT: return s.currentMa;
            default: return (float) s.powerW;
        }
    }

    private float valueAt(int idx) {
        return valueOf(seriesType, samples[idx]);
    }

    private float pointY(int idx, float min, float vRange, float padTop, float plotH) {
        float norm = (valueAt(idx) - min) / vRange;
        norm = Math.max(0f, Math.min(1f, norm));
        return padTop + plotH * (1f - norm);
    }

    /** Seri yang garisnya diwarnai sesuai status pengisian titik data. */
    private boolean isStatusColored() {
        return seriesType == SERIES_PERCENT || seriesType == SERIES_VOLTAGE
                || seriesType == SERIES_CURRENT;
    }

    private int[] tempAnchors;

    /** Anchor warna gradien Suhu: ice blue → hijau → oranye → merah. */
    private int[] tempAnchors() {
        if (tempAnchors == null) {
            tempAnchors = new int[] {
                    getResources().getColor(R.color.bat_chart_temp_ice),
                    getResources().getColor(R.color.bat_chart_percent),
                    getResources().getColor(R.color.bat_chart_power),
                    getResources().getColor(R.color.bat_chart_temp)};
        }
        return tempAnchors;
    }

    /**
     * Warna garis Suhu per nilai (°C): ≤5° putih, 5–24° putih→ice blue,
     * 25–34° ice blue→hijau, 35–39° hijau→oranye, 40–45°+ oranye→merah.
     */
    private int tempColor(float c) {
        int[] a = tempAnchors();
        if (c <= 5f) return Color.WHITE;
        if (c < 24f) return ColorUtils.blendARGB(Color.WHITE, a[0], (c - 5f) / 19f);
        if (c < 25f) return a[0];
        if (c < 34f) return ColorUtils.blendARGB(a[0], a[1], (c - 25f) / 9f);
        if (c < 35f) return a[1];
        if (c < 39f) return ColorUtils.blendARGB(a[1], a[2], (c - 35f) / 4f);
        if (c < 40f) return a[2];
        if (c < 45f) return ColorUtils.blendARGB(a[2], a[3], (c - 40f) / 5f);
        return a[3];
    }

    /** Warna garis sesuai status pengisian titik data (hijau isi, merah tidak). */
    private int lineColor(boolean charging) {
        return getResources().getColor(charging
                ? R.color.bat_chart_percent_charging
                : R.color.bat_chart_percent_discharging);
    }

    /** Format nilai sesuai jenis seri — dipakai juga halaman detail. */
    public static String formatValue(int seriesType, float v) {
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

    private String fmt(float v) {
        return formatValue(seriesType, v);
    }

    /** Resource warna aksen per jenis seri — dipakai juga halaman detail. */
    public static int seriesColorRes(int seriesType) {
        switch (seriesType) {
            case SERIES_TEMP: return R.color.bat_chart_temp;
            case SERIES_PERCENT: return R.color.bat_chart_percent;
            case SERIES_VOLTAGE: return R.color.bat_chart_voltage;
            case SERIES_CURRENT: return R.color.bat_chart_current;
            default: return R.color.bat_chart_power;
        }
    }

    private void drawCrosshair(Canvas canvas) {
        int idx = selectedIndex;
        if (idx < lastFirst || idx >= lastFirst + lastCount || idx >= samples.length) return;
        float x = lastPadLeft + lastPlotW * ((samples[idx].time - lastBaseT) / lastSpanMs);
        float norm = (valueAt(idx) - lastMin) / lastVRange;
        norm = Math.max(0f, Math.min(1f, norm));
        float y = lastPadTop + lastPlotH * (1f - norm);

        canvas.drawLine(x, lastPadTop, x, lastPadTop + lastPlotH, crosshairPaint);

        if (isStatusColored()) {
            dotPaint.setColor(lineColor(samples[idx].isCharging()));
        } else if (seriesType == SERIES_TEMP) {
            dotPaint.setColor(tempColor(valueAt(idx)));
        } else {
            dotPaint.setColor(linePaint.getColor());
        }
        canvas.drawCircle(x, y, dp(4), dotPaint);

        String txt = fmt(valueAt(idx)) + " · " + activeTimeFormat().format(new Date(samples[idx].time));
        float tw = crosshairTextPaint.measureText(txt);
        float bw = tw + dp(14);
        float bh = dp(20);
        float bx = x - bw / 2f;
        if (bx < 0) bx = 0;
        if (bx + bw > getWidth()) bx = getWidth() - bw;
        float by = y - bh - dp(10);
        if (by < lastPadTop) by = y + dp(12);
        canvas.drawRoundRect(bx, by, bx + bw, by + bh, dp(9), dp(9), bubblePaint);
        Paint.FontMetrics fm = crosshairTextPaint.getFontMetrics();
        canvas.drawText(txt, bx + bw / 2f,
                by + bh / 2f - (fm.ascent + fm.descent) / 2f, crosshairTextPaint);
    }

    private SimpleDateFormat activeTimeFormat() {
        if (windowMs >= WINDOW_6H) return timeLongFormat;
        if (windowMs >= WINDOW_1H) return timeMediumFormat;
        return timeShortFormat;
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
