package exp.ftxt.shared.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.appcompat.content.res.AppCompatResources;

import java.util.ArrayList;
import java.util.List;

import exp.ftxt.R;

/**
 * Custom view bar aktivitas di bawah grafik baterai (halaman detail).
 * Dua bar: "Layar" (ikon 🔆) menampilkan segmen layar aktif (putih) & layar mati (navy);
 * "Daya" (ikon ⚡) menampilkan segmen mengisi (hijau) & menguras (merah).
 * Tiap bar: ikon + label di atas, segmen bar di bawah — sejajar presisi dengan
 * area plot grafik (BatteryChartView): kiri 40dp, kanan width-8dp.
 */
public class ActivityBarView extends View {

    public static final int STATE_SCREEN_OFF = 0;
    public static final int STATE_SCREEN_ON = 1;
    public static final int STATE_CHARGING = 2;
    public static final int STATE_DISCHARGING = 3;
    private static final int NUM_BARS = 2;

    public static final class ActivitySegment {
        public final long startMs;
        public final long endMs;
        public final int state;

        public ActivitySegment(long startMs, long endMs, int state) {
            this.startMs = startMs;
            this.endMs = endMs;
            this.state = state;
        }
    }

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    private float barRowH;
    private float labelRowH;
    private float gap;
    private float barLeft;
    private float barRight;
    private float iconSize;

    private final List<ActivitySegment> screenOnSegments = new ArrayList<>();
    private final List<ActivitySegment> screenOffSegments = new ArrayList<>();
    private final List<ActivitySegment> chargingSegments = new ArrayList<>();
    private final List<ActivitySegment> dischargingSegments = new ArrayList<>();

    private long rangeStartMs;
    private long rangeEndMs;

    private int colorScreenOn;
    private int colorScreenOff;
    private int colorCharging;
    private int colorDischarging;
    private int colorLabel;
    private int colorBg;

    private Drawable sunIcon;
    private Drawable boltIcon;

    private static final String[] LABELS = {"Layar", "Daya"};

    public ActivityBarView(Context context) {
        super(context);
        init();
    }

    public ActivityBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ActivityBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        float density = getResources().getDisplayMetrics().density;
        barRowH = 14 * density;
        labelRowH = 16 * density;
        gap = 2 * density;
        iconSize = 12 * density;
        barLeft = 40 * density;

        barPaint.setStyle(Paint.Style.FILL);

        bgPaint.setStyle(Paint.Style.FILL);

        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextAlign(Paint.Align.LEFT);

        colorScreenOn = 0xFFFFFFFF;
        colorScreenOff = 0xFF0A2C4A;
        colorCharging = 0xFF4CAF50;
        colorDischarging = 0xFFF44336;
        colorLabel = 0xFF8A8A8E;
        colorBg = 0x1A000000;

        sunIcon = AppCompatResources.getDrawable(getContext(), R.drawable.ic_sun);
        boltIcon = AppCompatResources.getDrawable(getContext(), R.drawable.ic_bolt);
    }

    public void setColors(int screenOn, int screenOff, int charging, int discharging,
                          int label) {
        this.colorScreenOn = screenOn;
        this.colorScreenOff = screenOff;
        this.colorCharging = charging;
        this.colorDischarging = discharging;
        this.colorLabel = label;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int totalHeight = (int) (getPaddingTop()
                + NUM_BARS * (labelRowH + barRowH + gap)
                + getPaddingBottom());
        setMeasuredDimension(w, totalHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float density = getResources().getDisplayMetrics().density;
        barLeft = 40 * density;
        barRight = w - 8 * density;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.clipRect(getPaddingLeft(), getPaddingTop(),
                getWidth() - getPaddingRight(), getHeight() - getPaddingBottom());

        float density = getResources().getDisplayMetrics().density;
        barLeft = 40 * density;
        barRight = getWidth() - 8 * density;

        drawBar(canvas, 0, LABELS[0], sunIcon,
                screenOnSegments, colorScreenOn,
                screenOffSegments, colorScreenOff);
        drawBar(canvas, 1, LABELS[1], boltIcon,
                chargingSegments, colorCharging,
                dischargingSegments, colorDischarging);
    }

    private void drawBar(Canvas canvas, int barIndex, String label, Drawable icon,
                         List<ActivitySegment> segA, int colorA,
                         List<ActivitySegment> segB, int colorB) {
        float density = getResources().getDisplayMetrics().density;
        float top = getPaddingTop() + barIndex * (labelRowH + barRowH + gap);

        // Label (ikon + teks) di atas, di kiri.
        float iconTop = top + (labelRowH - iconSize) / 2f;
        if (icon != null) {
            icon.setBounds((int) (getPaddingLeft() + 2 * density),
                    (int) iconTop,
                    (int) (getPaddingLeft() + 2 * density + iconSize),
                    (int) (iconTop + iconSize));
            icon.setTint(colorLabel);
            icon.draw(canvas);
        }
        textPaint.setColor(colorLabel);
        textPaint.setTextSize(11 * density);
        float textX = getPaddingLeft() + 2 * density + iconSize + 4 * density;
        float textY = top + labelRowH / 2f - (textPaint.ascent() + textPaint.descent()) / 2f;
        canvas.drawText(label, textX, textY, textPaint);

        // Bar segmen di bawah label, sejajar plot grafik.
        float barTop = top + labelRowH + gap;
        float barBottom = barTop + barRowH;

        bgPaint.setColor(colorBg);
        canvas.drawRect(barLeft, barTop, barRight, barBottom, bgPaint);

        drawSegments(canvas, segA, colorA, barTop, barBottom);
        drawSegments(canvas, segB, colorB, barTop, barBottom);
    }

    private void drawSegments(Canvas canvas, List<ActivitySegment> segments,
                              int color, float top, float bottom) {
        if (segments.isEmpty() || rangeEndMs <= rangeStartMs) return;
        barPaint.setColor(color);
        for (ActivitySegment seg : segments) {
            if (seg.endMs <= rangeStartMs || seg.startMs >= rangeEndMs) continue;
            float x1 = timeToX(Math.max(seg.startMs, rangeStartMs));
            float x2 = timeToX(Math.min(seg.endMs, rangeEndMs));
            if (x2 > x1) {
                rect.set(x1, top + 1, x2, bottom - 1);
                canvas.drawRoundRect(rect, 2 * getResources().getDisplayMetrics().density,
                        2 * getResources().getDisplayMetrics().density, barPaint);
            }
        }
    }

    private float timeToX(long timeMs) {
        if (rangeEndMs <= rangeStartMs) return barLeft;
        float fraction = (float) (timeMs - rangeStartMs) / (float) (rangeEndMs - rangeStartMs);
        return barLeft + fraction * (barRight - barLeft);
    }

    public void setActivityData(List<ActivitySegment> screenOn,
                                List<ActivitySegment> screenOff,
                                List<ActivitySegment> charging,
                                List<ActivitySegment> discharging) {
        screenOnSegments.clear();
        screenOffSegments.clear();
        chargingSegments.clear();
        dischargingSegments.clear();
        if (screenOn != null) screenOnSegments.addAll(screenOn);
        if (screenOff != null) screenOffSegments.addAll(screenOff);
        if (charging != null) chargingSegments.addAll(charging);
        if (discharging != null) dischargingSegments.addAll(discharging);
        invalidate();
    }

    public void setRange(long startMs, long endMs) {
        this.rangeStartMs = startMs;
        this.rangeEndMs = endMs;
        invalidate();
    }
}
