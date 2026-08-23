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
 * Ring gauge baterai melingkar: track lingkaran + arc progress berwarna
 * gradien hue (skema Battery Strip), dengan tiga baris teks di dalamnya —
 * kapasitas, level %, dan status pengisian.
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

        TypedValue tv = new TypedValue();
        if (getContext().getTheme().resolveAttribute(android.R.attr.textColorPrimary, tv, true)) {
            primaryTextColor = getContext().getColor(tv.resourceId != 0
                    ? tv.resourceId : tv.data);
        }
    }

    public void setBatteryData(int percent, String capacityText, String statusText) {
        this.levelPercent = Math.max(0, Math.min(100, percent));
        this.capacityText = capacityText != null ? capacityText : "—";
        this.statusText = statusText != null ? statusText : "—";
        invalidate();
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
        if (levelPercent > 0) {
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

    private String fit(Paint paint, String text, float maxWidth) {
        if (paint.measureText(text) <= maxWidth) return text;
        String out = text;
        while (out.length() > 1 && paint.measureText(out + "…") > maxWidth) {
            out = out.substring(0, out.length() - 1);
        }
        return out + "…";
    }
}
