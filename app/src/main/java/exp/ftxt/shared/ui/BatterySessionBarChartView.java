package exp.ftxt.shared.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import exp.ftxt.R;
import exp.ftxt.features.battery_stats.BatteryHistoryDb;

/**
 * Bar chart riwayat sesi: satu batang per slot waktu (hari/minggu/bulan) pada
 * rentang tetap. Setiap batang terdiri dari dua segmen — hijau (pengisian %)
 * dan oranye (pengosongan %) — sehingga skala sumbu Y otomatis mengikuti nilai
 * agregat maksimum. Slot dihitung berdasarkan posisi waktu (slotCount + rentang),
 * bukan jumlah hari yang berisi data, sehingga hari tanpa data tampil kosong dan
 * grafik tidak melebar saat data masih sedikit. Batang bisa diketuk untuk memilih
 * slot tersebut; batang terpilih disorot stroke aksen.
 */
public class BatterySessionBarChartView extends View {

    public interface OnBarClickListener {
        void onBarClicked(int index, BatteryHistoryDb.BarAggregate aggregate);
    }

    private final Paint chargePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dischargePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint emptyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final SimpleDateFormat fmtDay = new SimpleDateFormat("d", Locale.US);
    private final SimpleDateFormat fmtMonth = new SimpleDateFormat("M", Locale.US);

    private ArrayList<BatteryHistoryDb.BarAggregate> data = new ArrayList<>();
    private int selectedIndex = -1;
    private int mode = BatteryHistoryDb.MODE_DAILY;
    private long rangeStartMs;
    private long rangeEndMs;
    private int slotCount = 0;
    private OnBarClickListener barClickListener;

    public BatterySessionBarChartView(Context context) {
        this(context, null);
    }

    public BatterySessionBarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        int accent = context.getResources().getColor(R.color.bat_monitor_header);
        int labelColor = context.getResources().getColor(R.color.bat_monitor_label);
        int gridColor = context.getResources().getColor(R.color.bat_chart_grid);
        int chargeColor = context.getResources().getColor(R.color.bat_chart_percent_charging);
        int dischargeColor = context.getResources().getColor(R.color.bat_chart_power);

        chargePaint.setColor(chargeColor);
        dischargePaint.setColor(dischargeColor);
        gridPaint.setColor(gridColor);
        gridPaint.setStyle(Paint.Style.STROKE);
        textPaint.setColor(labelColor);
        textPaint.setTextSize(dp(10));
        textPaint.setTextAlign(Paint.Align.CENTER);
        strokePaint.setColor(accent);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(dp(2));
        emptyPaint.setColor(labelColor);
        emptyPaint.setTextSize(dp(12));
        emptyPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setData(ArrayList<BatteryHistoryDb.BarAggregate> aggregates) {
        this.data = aggregates != null ? aggregates : new ArrayList<>();
        if (selectedIndex >= slotCount) selectedIndex = -1;
        invalidate();
    }

    /** Mengatur rentang sumbu waktu yang ditampilkan (slot tetap ber-rolling). */
    public void setRange(long startMs, long endMs, int mode, int slotCount) {
        this.rangeStartMs = startMs;
        this.rangeEndMs = Math.max(startMs, endMs);
        this.mode = mode;
        this.slotCount = Math.max(1, slotCount);
        invalidate();
    }

    public void setSelectedIndex(int index) {
        selectedIndex = index;
        invalidate();
    }

    /** Menyorot slot yang berisi bucket tertentu (berdasarkan posisi waktunya). */
    public void setSelectedBucket(long bucketStart) {
        setSelectedIndex(slotIndexFor(bucketStart));
    }

    public void setOnBarClickListener(OnBarClickListener listener) {
        this.barClickListener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        float padL = dp(30), padR = dp(6), padT = dp(14), padB = dp(18);
        float left = padL, right = w - padR;
        float top = padT, bottom = h - padB;
        float chartH = bottom - top;

        if (data.isEmpty() || slotCount <= 0) {
            emptyPaint.setTextSize(dp(12));
            canvas.drawText("Belum ada data", (left + right) / 2f, (top + bottom) / 2f, emptyPaint);
            return;
        }

        float maxVal = 1f;
        for (BatteryHistoryDb.BarAggregate a : data) {
            float total = a.chargePercent + a.dischargePercent;
            if (total > maxVal) maxVal = total;
        }

        // Sumbu Y: nilai max di garis teratas, 0 di garis terbawah, dengan satuan %
        gridPaint.setStrokeWidth(dp(1));
        for (int i = 0; i <= 2; i++) {
            float ratio = i / 2f;
            float y = top + chartH * ratio;
            canvas.drawLine(left, y, right, y, gridPaint);
            String label = (i == 0 ? fmt((int) Math.round(maxVal))
                    : i == 1 ? fmt((int) Math.round(maxVal / 2f)) : "0") + "%";
            textPaint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(label, left - dp(4), y + dp(3), textPaint);
        }
        textPaint.setTextAlign(Paint.Align.CENTER);

        float slotW = (right - left) / slotCount;
        float barW = slotW * 0.7f;

        for (BatteryHistoryDb.BarAggregate a : data) {
            long bucket = a.bucketStartMs;
            if (bucket >= rangeEndMs) continue;
            int slot = slotIndexFor(bucket);
            if (slot < 0) slot = 0;
            if (slot >= slotCount) slot = slotCount - 1;

            float cx = left + slot * slotW + slotW / 2f;
            float x0 = cx - barW / 2f;
            float x1 = cx + barW / 2f;

            float chargeH = chartH * (a.chargePercent / maxVal);
            float dischargeH = chartH * (a.dischargePercent / maxVal);
            float yBase = bottom;

            if (a.chargePercent > 0f) {
                canvas.drawRect(new RectF(x0, yBase - chargeH, x1, yBase), chargePaint);
            }
            if (a.dischargePercent > 0f) {
                canvas.drawRect(new RectF(x0, yBase - chargeH - dischargeH,
                        x1, yBase - chargeH), dischargePaint);
            }

            if (slot == selectedIndex && a.chargePercent + a.dischargePercent > 0f) {
                canvas.drawRect(new RectF(x0 - dp(1), bottom - chargeH - dischargeH,
                        x1 + dp(1), bottom), strokePaint);
            }
        }

        // Label sumbu X: ditulis untuk semua slot agar konsisten & tidak tumpang tindih.
        for (int slot = 0; slot < slotCount; slot++) {
            long bucket = slotBucketStart(slot);
            String label = slotLabel(bucket);
            float cx = left + slot * slotW + slotW / 2f;
            canvas.drawText(label, cx, h - dp(4), textPaint);
        }
    }

    @Override
    public boolean onTouchEvent(android.view.MotionEvent event) {
        if (data.isEmpty() || slotCount <= 0) return super.onTouchEvent(event);
        if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
            float padL = dp(30), padR = dp(6);
            float left = padL, right = getWidth() - padR;
            if (right <= left) return true;
            float slotW = (right - left) / slotCount;
            int slot = (int) ((event.getX() - left) / slotW);
            if (slot < 0) slot = 0;
            if (slot >= slotCount) slot = slotCount - 1;
            IntentForClick(slot);
            return true;
        }
        return true;
    }

    private void IntentForClick(int slot) {
        BatteryHistoryDb.BarAggregate found = null;
        for (BatteryHistoryDb.BarAggregate a : data) {
            if (slotIndexFor(a.bucketStartMs) == slot) {
                found = a;
                break;
            }
        }
        if (found == null) return;
        setSelectedIndex(slot);
        if (barClickListener != null) {
            barClickListener.onBarClicked(slot, found);
        }
    }

    private int slotIndexFor(long bucket) {
        if (slotCount <= 0) return -1;
        int slot;
        if (mode == BatteryHistoryDb.MODE_MONTHLY) {
            java.util.Calendar cb = java.util.Calendar.getInstance();
            cb.setTimeInMillis(bucket);
            java.util.Calendar cr = java.util.Calendar.getInstance();
            cr.setTimeInMillis(rangeStartMs);
            slot = (cb.get(java.util.Calendar.YEAR) - cr.get(java.util.Calendar.YEAR)) * 12
                    + (cb.get(java.util.Calendar.MONTH) - cr.get(java.util.Calendar.MONTH));
        } else if (mode == BatteryHistoryDb.MODE_WEEKLY) {
            slot = (int) ((bucket - rangeStartMs) / WEEK_MS);
        } else {
            slot = (int) ((bucket - rangeStartMs) / DAY_MS);
        }
        if (slot < 0) return 0;
        if (slot >= slotCount) return slotCount - 1;
        return slot;
    }

    private static final long DAY_MS = 24L * 60L * 60L * 1000L;
    private static final long WEEK_MS = 7L * DAY_MS;

    /** Waktu mulai (bucket) untuk slot ke-0..slotCount-1 pada rentang aktif. */
    private long slotBucketStart(int slot) {
        if (mode == BatteryHistoryDb.MODE_MONTHLY) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(rangeStartMs);
            c.add(Calendar.MONTH, slot);
            return c.getTimeInMillis();
        }
        long unit = mode == BatteryHistoryDb.MODE_WEEKLY ? WEEK_MS : DAY_MS;
        return rangeStartMs + slot * unit;
    }

    /** Label sumbu X untuk sebuah bucket: harian = tanggal, mingguan = nomor minggu ISO, bulanan = nomor bulan. */
    private String slotLabel(long bucket) {
        if (mode == BatteryHistoryDb.MODE_MONTHLY) {
            return fmtMonth.format(new Date(bucket));
        }
        if (mode == BatteryHistoryDb.MODE_WEEKLY) {
            return String.valueOf(isoWeekNumber(bucket));
        }
        return fmtDay.format(new Date(bucket));
    }

    /** Nomor minggu menurut ISO 8601 (minggu mulai Senin, minggu-1 berisi minimal 4 hari). */
    private static int isoWeekNumber(long ms) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ms);
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.setMinimalDaysInFirstWeek(4);
        return c.get(Calendar.WEEK_OF_YEAR);
    }

    private String fmt(int v) {
        return String.valueOf(v);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
