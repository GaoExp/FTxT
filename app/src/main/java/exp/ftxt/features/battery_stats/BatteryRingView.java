package exp.ftxt.features.battery_stats;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import exp.ftxt.R;
import exp.ftxt.shared.color.BatteryColors;

/**
 * Ring gauge baterai melingkar.
 *
 * <p>Dua mode tampilan arc:
 * <ul>
 *   <li>Mode level (default, dipakai tab Info &amp; Grafik): track lingkaran +
 *       arc progress warna gradien hue sesuai level.</li>
 *   <li>{@link #setSessionData}: arc sesi dengan skema warna putih–hijau–merah
 *       untuk pengisian/pengosongan — putih = level sekarang, hijau = bagian
 *       yang bertambah saat mengisi, merah = bagian yang terpakai saat
 *       mengosongkan (putih = sisa level terakhir).</li>
 * </ul>
 * Di dalamnya tiga baris teks: kapasitas, level %, dan status.
 */
public class BatteryRingView extends View {

    private final Paint arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint levelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcRect = new RectF();

    private int levelPercent = 0;
    private String capacityText = "—";
    private String statusText = "—";
    private float strokePx;
    private int primaryTextColor = 0xFF888888;

    private boolean sessionMode = false;
    private int sessionStartPct = 0;
    private boolean sessionCharging = true;
    private int sessionWhite;
    private int sessionGreen;
    private int sessionRed;

    public BatteryRingView(Context context) {
        super(context);
        init();
    }

    public BatteryRingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        strokePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f,
                getResources().getDisplayMetrics());

        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(strokePx);
        trackPaint.setColor(getContext().getColor(R.color.bat_monitor_stroke));

        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeWidth(strokePx);
        arcPaint.setStrokeCap(Paint.Cap.ROUND);

        levelPaint.setTypeface(Typeface.DEFAULT_BOLD);
        levelPaint.setTextAlign(Paint.Align.CENTER);

        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setColor(getContext().getColor(R.color.bat_monitor_label));

        sessionWhite = getContext().getColor(R.color.bat_session_white);
        sessionGreen = getContext().getColor(R.color.bat_monitor_active);
        sessionRed = getContext().getColor(R.color.bat_monitor_stop);

        TypedValue tv = new TypedValue();
        if (getContext().getTheme().resolveAttribute(android.R.attr.textColorPrimary, tv, true)) {
            primaryTextColor = getContext().getColor(tv.resourceId != 0
                    ? tv.resourceId : tv.data);
        }
    }

    /** Mode level (hue): dipakai tab Info &amp; Grafik. */
    public void setBatteryData(int percent, String capacityText, String statusText) {
        sessionMode = false;
        this.levelPercent = Math.max(0, Math.min(100, percent));
        this.capacityText = capacityText != null ? capacityText : "—";
        this.statusText = statusText != null ? statusText : "—";
        invalidate();
    }

    /**
     * Mode sesi (putih–hijau–merah) untuk panel sesi berjalan & detail sesi.
     *
     * @param percent     level sekarang (akhir sesi untuk detail) 0–100
     * @param startPct    level di awal sesi 0–100
     * @param charging    true = pengisian, false = pengosongan
     * @param capacityText teks di baris atas (mis. mAh / "0 mAh")
     * @param statusText  teks di baris bawah (mis. "Mengisi"/"Menguras")
     */
    public void setSessionData(int percent, int startPct, boolean charging,
                               String capacityText, String statusText) {
        sessionMode = true;
        this.levelPercent = clampPct(percent);
        this.sessionStartPct = clampPct(startPct);
        this.sessionCharging = charging;
        this.capacityText = capacityText != null ? capacityText : "—";
        this.statusText = statusText != null ? statusText : "—";
        invalidate();
    }

    private static int clampPct(int v) {
        return Math.max(0, Math.min(100, v));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        float size = Math.min(w, h);
        float cx = w / 2f;
        float cy = h / 2f;
        float radius = size / 2f - strokePx / 2f - strokePx * 0.2f;
        float innerDiameter = radius * 2f - strokePx;

        arcRect.set(cx - radius, cy - radius, cx + radius, cy + radius);
        canvas.drawArc(arcRect, 0f, 360f, false, trackPaint);

        if (sessionMode) {
            drawSessionArc(canvas);
        } else if (levelPercent > 0) {
            arcPaint.setColor(BatteryColors.hueColor(levelPercent));
            canvas.drawArc(arcRect, -90f, levelPercent * 3.6f, false, arcPaint);
        }

        float bigSize = size * 0.21f;
        float smallSize = size * 0.105f;

        labelPaint.setTextSize(smallSize);
        levelPaint.setTextSize(bigSize);
        levelPaint.setColor(primaryTextColor);

        canvas.drawText(fit(labelPaint, capacityText, innerDiameter),
                cx, cy - bigSize * 0.45f, labelPaint);
        canvas.drawText(levelPercent + "%", cx, cy + bigSize * 0.38f, levelPaint);
        canvas.drawText(fit(labelPaint, statusText, innerDiameter),
                cx, cy + bigSize * 1.15f, labelPaint);
    }

    /**
     * Arc sesi: putih = level sekarang (sisa). Saat pengisian, hijau menyusul
     * dari titik awal = level awal (menutup bagian yang bertambah, putih tetap
     * di level sekarang). Saat pengosongan, merah membuka dari titik awal =
     * level awal ke atas (putih menyusut ke sisa level sekarang).
     */
    private void drawSessionArc(Canvas canvas) {
        if (levelPercent <= 0) return;

        if (sessionCharging) {
            // Putih = dari 0 sampai level sekarang; hijau = level awal -> sekarang.
            arcPaint.setColor(sessionWhite);
            canvas.drawArc(arcRect, -90f, levelPercent * 3.6f, false, arcPaint);

            int gainStart = Math.min(levelPercent, sessionStartPct);
            int gainEnd = levelPercent;
            int gain = gainEnd - gainStart;
            if (gain > 0) {
                arcPaint.setColor(sessionGreen);
                canvas.drawArc(arcRect, -90f + gainStart * 3.6f, gain * 3.6f, false, arcPaint);
            }
        } else {
            // Putih = sisa level sekarang (0..sekarang). Merah = bagian yang
            // terpakai sesi ini (sekarang..level awal), menutup dari bawah ke atas.
            arcPaint.setColor(sessionWhite);
            canvas.drawArc(arcRect, -90f, levelPercent * 3.6f, false, arcPaint);

            int usedLow = Math.min(levelPercent, sessionStartPct);
            int usedHigh = Math.max(levelPercent, sessionStartPct);
            int used = usedHigh - usedLow;
            if (used > 0) {
                arcPaint.setColor(sessionRed);
                canvas.drawArc(arcRect, -90f + usedLow * 3.6f, used * 3.6f, false, arcPaint);
            }
        }
    }

    private String fit(Paint paint, String text, float maxWidth) {
        if (paint.measureText(text) <= maxWidth) return text;
        String out = text;
        while (out.length() > 1 && paint.measureText(out + "…") > maxWidth) {
            out = out.substring(0, out.length() - 1);
        }
        return out + "…";
    }
}
