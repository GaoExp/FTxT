package exp.ftxt.shared.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import exp.ftxt.R;
import exp.ftxt.features.battery_stats.BatteryHistoryDb;

/**
 * Bar chart riwayat sesi: satu batang per hari/bulan. Setiap batang terdiri
 * dari dua segmen — hijau (pengisian %) dan oranye (pengosongan %) — sehingga
 * skala sumbu Y otomatis mengikuti nilai agregat maksimum periode. Batang bisa
 * diketuk untuk memilih tanggal itu; batang terpilih disorot stroke aksen.
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

    private final SimpleDateFormat fmtDay = new SimpleDateFormat("dd/MM", Locale.US);
    private final SimpleDateFormat fmtMonth = new SimpleDateFormat("MM/yy", Locale.US);

    private ArrayList<BatteryHistoryDb.BarAggregate> data = new ArrayList<>();
    private int selectedIndex = -1;
    private boolean monthly = false;
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
        if (selectedIndex >= data.size()) selectedIndex = -1;
        invalidate();
    }

    public void setSelectedIndex(int index) {
        selectedIndex = index;
        invalidate();
    }

    public void setMonthly(boolean monthly) {
        this.monthly = monthly;
        invalidate();
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

        float padL = dp(6), padR = dp(6), padT = dp(14), padB = dp(18);
        float left = padL, right = w - padR;
        float top = padT, bottom = h - padB;
        float chartH = bottom - top;

        if (data.isEmpty()) {
            emptyPaint.setTextSize(dp(12));
            canvas.drawText("Belum ada data", w / 2f, h / 2f, emptyPaint);
            return;
        }

        float maxVal = 1f;
        for (BatteryHistoryDb.BarAggregate a : data) {
            float total = a.chargePercent + a.dischargePercent;
            if (total > maxVal) maxVal = total;
        }

        gridPaint.setStrokeWidth(dp(1));
        for (int i = 0; i <= 2; i++) {
            float y = top + chartH * (1f - i / 2f);
            canvas.drawLine(left, y, right, y, gridPaint);
            String label = i == 0 ? fmt((int) maxVal)
                    : i == 1 ? fmt((int) (maxVal / 2f)) : "0";
            textPaint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(label, dp(2), y - dp(3), textPaint);
        }
        textPaint.setTextAlign(Paint.Align.CENTER);

        int n = data.size();
        float slot = (right - left) / n;
        for (int i = 0; i < n; i++) {
            BatteryHistoryDb.BarAggregate a = data.get(i);
            float x0 = left + slot * i + slot * 0.15f;
            float x1 = left + slot * (i + 1) - slot * 0.15f;

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

            String dayLabel = monthly
                    ? fmtMonth.format(new Date(a.bucketStartMs))
                    : fmtDay.format(new Date(a.bucketStartMs));
            canvas.drawText(dayLabel, left + slot * i + slot / 2f, h - dp(4), textPaint);

            if (i == selectedIndex && a.chargePercent + a.dischargePercent > 0f) {
                canvas.drawRect(new RectF(x0 - dp(1), bottom - chargeH - dischargeH,
                        x1 + dp(1), bottom), strokePaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(android.view.MotionEvent event) {
        if (data.isEmpty()) return super.onTouchEvent(event);
        if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
            float padL = dp(6), padR = dp(6);
            float left = padL, right = getWidth() - padR;
            int n = data.size();
            if (n <= 0 || right <= left) return true;
            int index = (int) ((event.getX() - left) / ((right - left) / n));
            if (index < 0) index = 0;
            if (index >= n) index = n - 1;
            setSelectedIndex(index);
            if (barClickListener != null) {
                barClickListener.onBarClicked(index, data.get(index));
            }
            return true;
        }
        return true;
    }

    private String fmt(int v) {
        return String.valueOf(v);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}