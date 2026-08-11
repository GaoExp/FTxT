package exp.ftxt.features.battery_bar;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class BatteryBarView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    private int fillColor = Color.GREEN;
    private int emptyColor = 0x66000000;
    private boolean showEmptyStrip = true;
    private float radiusPx = 8;
    private float percent = 0f;
    private boolean charging = false;
    private boolean low = false;
    private int lowColor = Color.YELLOW;
    private boolean autoColor = false;
    private boolean horizontal = true;

    private float fillAlpha = 255f;
    private float shinePos = -1f;

    private ValueAnimator fadeAnimator;
    private ValueAnimator shineAnimator;

    public BatteryBarView(Context context) {
        super(context);
    }

    public BatteryBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setBarConfig(int fillColor, int emptyColor, boolean showEmptyStrip, float radiusPx,
                             boolean autoColor, int lowColor, boolean horizontal) {
        this.fillColor = fillColor;
        this.emptyColor = emptyColor;
        this.showEmptyStrip = showEmptyStrip;
        this.radiusPx = radiusPx;
        this.autoColor = autoColor;
        this.lowColor = lowColor;
        this.horizontal = horizontal;
        invalidate();
    }

    public void setBarOrientation(boolean horizontal) {
        this.horizontal = horizontal;
        invalidate();
    }

    public void updateStatus(float percent, boolean charging, boolean low) {
        this.percent = Math.max(0f, Math.min(100f, percent));
        this.charging = charging;
        this.low = low;
        updateAnimators();
        invalidate();
    }

    private void updateAnimators() {
        if (charging) {
            startShine();
        } else {
            stopShine();
        }
        if (low && !charging) {
            startFade();
        } else {
            stopFade();
            fillAlpha = 255f;
        }
    }

    private void startShine() {
        if (shineAnimator != null) return;
        shineAnimator = ValueAnimator.ofFloat(-0.5f, 1.5f);
        shineAnimator.setDuration(1800);
        shineAnimator.setRepeatCount(ValueAnimator.INFINITE);
        shineAnimator.setInterpolator(new LinearInterpolator());
        shineAnimator.addUpdateListener(a -> {
            shinePos = (float) a.getAnimatedValue();
            invalidate();
        });
        shinePos = -0.5f;
        shineAnimator.start();
    }

    private void stopShine() {
        if (shineAnimator != null) {
            shineAnimator.cancel();
            shineAnimator = null;
        }
        shinePos = -1f;
    }

    private void startFade() {
        if (fadeAnimator != null) return;
        long duration = Math.max(400, 2400 - (long) fadeSpeed * 120);
        fadeAnimator = ValueAnimator.ofFloat(1f, 0.25f);
        fadeAnimator.setDuration(duration);
        fadeAnimator.setRepeatMode(ValueAnimator.REVERSE);
        fadeAnimator.setRepeatCount(ValueAnimator.INFINITE);
        fadeAnimator.addUpdateListener(a -> {
            fillAlpha = 255f * (float) a.getAnimatedValue();
            invalidate();
        });
        fadeAnimator.start();
    }

    private void stopFade() {
        if (fadeAnimator != null) {
            fadeAnimator.cancel();
            fadeAnimator = null;
        }
    }

    private int fadeSpeed = 5;

    public void setFadeSpeed(int speed) {
        this.fadeSpeed = Math.max(1, Math.min(20, speed));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        float radius = Math.min(radiusPx, Math.min(w, h) / 2f);
        rect.set(0, 0, w, h);

        if (showEmptyStrip) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(emptyColor);
            canvas.drawRoundRect(rect, radius, radius, paint);
        }

        int barColor = fillColor;
        if (autoColor) {
            if (percent <= 10f) barColor = Color.RED;
            else if (percent <= 20f) barColor = Color.YELLOW;
            else barColor = Color.GREEN;
        } else if (low) {
            barColor = lowColor;
        }

        if (percent > 0f) {
            paint.setColor(barColor);
            paint.setAlpha((int) fillAlpha);
            if (horizontal) {
                float fillW = Math.max(0, w * (percent / 100f));
                rect.set(0, 0, fillW, h);
            } else {
                float fillH = Math.max(0, h * (percent / 100f));
                rect.set(0, h - fillH, w, h);
            }
            canvas.drawRoundRect(rect, radius, radius, paint);
            paint.setAlpha(255);
        }

        if (charging && shinePos >= -0.5f) {
            float bandWidth = Math.max(10f, (horizontal ? w : h) * 0.25f);
            if (horizontal) {
                float left = shinePos * w - bandWidth / 2f;
                rect.set(left, 0, left + bandWidth, h);
            } else {
                float top = shinePos * h - bandWidth / 2f;
                rect.set(0, top, w, top + bandWidth);
            }
            paint.setColor(0x66FFFFFF);
            canvas.drawRoundRect(rect, radius, radius, paint);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopFade();
        stopShine();
    }
}
