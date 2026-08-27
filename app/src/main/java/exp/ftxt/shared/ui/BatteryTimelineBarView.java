package exp.ftxt.shared.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

import exp.ftxt.features.battery_stats.BatteryHistoryDb;

/**
 * Bar horizontal yang menampilkan status aktivitas perangkat seiring waktu.
 * Status:
 * - 0: layar mati (abu gelap)
 * - 1: layar aktif (biru/ungu)
 * - 2: charging (hijau)
 */
public class BatteryTimelineBarView extends View {

    public static final int STATUS_SCREEN_OFF = 0;
    public static final int STATUS_SCREEN_ON = 1;
    public static final int STATUS_CHARGING = 2;

    private static final int COLOR_SCREEN_OFF = 0xFF3A3A3A;
    private static final int COLOR_SCREEN_ON = 0xFF7C4DFF;
    private static final int COLOR_CHARGING = 0xFF4CAF50;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    private ArrayList<BatteryHistoryDb.ActivityLog> logs = new ArrayList<>();
    private long fromMs = 0;
    private long toMs = 0;

    public BatteryTimelineBarView(Context context) {
        super(context);
        init();
    }

    public BatteryTimelineBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BatteryTimelineBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setBackgroundColor(Color.TRANSPARENT);
    }

    public void setTimeWindow(long fromMs, long toMs) {
        this.fromMs = fromMs;
        this.toMs = toMs;
        invalidate();
    }

    public void setActivityLogs(ArrayList<BatteryHistoryDb.ActivityLog> logs) {
        this.logs = logs != null ? logs : new ArrayList<>();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (logs.isEmpty() || fromMs >= toMs) return;

        float w = getWidth();
        float h = getHeight();
        long span = toMs - fromMs;

        rect.set(0, 0, w, h);
        paint.setColor(COLOR_SCREEN_OFF);
        canvas.drawRoundRect(rect, 4f, 4f, paint);

        for (int i = 0; i < logs.size(); i++) {
            BatteryHistoryDb.ActivityLog log = logs.get(i);

            float startX = Math.max(0, ((float) (log.time - fromMs) / span) * w);
            float endX;

            if (i + 1 < logs.size()) {
                BatteryHistoryDb.ActivityLog next = logs.get(i + 1);
                endX = Math.min(w, ((float) (next.time - fromMs) / span) * w);
            } else {
                endX = w;
            }

            if (endX <= startX) continue;

            int color;
            switch (log.status) {
                case STATUS_SCREEN_ON:
                    color = COLOR_SCREEN_ON;
                    break;
                case STATUS_CHARGING:
                    color = COLOR_CHARGING;
                    break;
                default:
                    color = COLOR_SCREEN_OFF;
            }

            rect.set(startX, 0, endX, h);
            paint.setColor(color);
            canvas.drawRect(rect, paint);
        }
    }
}