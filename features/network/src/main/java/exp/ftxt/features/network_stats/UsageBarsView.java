package exp.ftxt.features.network_stats;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class UsageBarsView extends View {

    private final Paint rxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint txPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint baselinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final List<DataUsageTracker.DayUsage> days = new ArrayList<>();

    public UsageBarsView(Context context) {
        super(context);
        init();
    }

    public UsageBarsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public UsageBarsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        rxPaint.setColor(0xFF4CAF50);
        txPaint.setColor(0xFF03A9F4);
        labelPaint.setColor(0xFF888888);
        labelPaint.setTextSize(dp(10));
        labelPaint.setTextAlign(Paint.Align.CENTER);
        baselinePaint.setColor(0x33888888);
        baselinePaint.setStrokeWidth(dp(1));
    }

    public void setData(List<DataUsageTracker.DayUsage> data) {
        days.clear();
        if (data != null) days.addAll(data);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (days.isEmpty()) return;

        float labelHeight = dp(16);
        float chartTop = dp(6);
        float chartBottom = getHeight() - labelHeight;
        float chartHeight = chartBottom - chartTop;
        if (chartHeight <= 0) return;

        long max = 0;
        for (DataUsageTracker.DayUsage day : days) {
            max = Math.max(max, Math.max(day.rx, day.tx));
        }
        if (max <= 0) max = 1;

        canvas.drawLine(0, chartBottom, getWidth(), chartBottom, baselinePaint);

        float groupWidth = (float) getWidth() / days.size();
        float barWidth = Math.max(dp(3), groupWidth * 0.26f);
        float gap = dp(2);

        for (int i = 0; i < days.size(); i++) {
            DataUsageTracker.DayUsage day = days.get(i);
            float center = groupWidth * i + groupWidth / 2f;

            float rxHeight = (float) day.rx / max * chartHeight;
            float txHeight = (float) day.tx / max * chartHeight;

            if (day.hasData) {
                float rxLeft = center - gap / 2f - barWidth;
                float txLeft = center + gap / 2f;
                canvas.drawRect(rxLeft, chartBottom - rxHeight, rxLeft + barWidth, chartBottom, rxPaint);
                canvas.drawRect(txLeft, chartBottom - txHeight, txLeft + barWidth, chartBottom, txPaint);
            }

            String label = day.date.length() >= 10
                    ? day.date.substring(8, 10) + "/" + day.date.substring(5, 7)
                    : day.date;
            canvas.drawText(label, center, getHeight() - dp(4), labelPaint);
        }
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
