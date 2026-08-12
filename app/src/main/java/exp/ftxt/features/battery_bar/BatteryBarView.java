package exp.ftxt.features.battery_bar;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class BatteryBarView extends View {

    private static final float WAVE_CYCLES = 2f;

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
    private int colorScheme = BatteryBarConfig.SCHEME_NONE;
    private boolean horizontal = true;
    private boolean invert = false;

    private float fillAlpha = 255f;
    private float shinePos = -1f;

    private boolean shineEnabled = true;
    private int shineSpeed = 1800;
    private int shineWidthPercent = 25;

    private boolean fadeEnabled = true;

    private boolean waveEnabled = true;
    private int waveSpeed = 1000;
    private int waveAmplitudePercent = 60;
    private float waveOffset = 0f;

    private boolean chargeWaveEnabled = true;
    private int chargeWaveSpeed = 1000;
    private int chargeWaveAmplitudePercent = 60;
    private float chargeWaveOffset = 0f;

    private ValueAnimator fadeAnimator;
    private ValueAnimator shineAnimator;
    private ValueAnimator waveAnimator;
    private ValueAnimator chargeWaveAnimator;

    public BatteryBarView(Context context) {
        super(context);
    }

    public BatteryBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setBarConfig(int fillColor, int emptyColor, boolean showEmptyStrip, float radiusPx,
                             int colorScheme, int lowColor, boolean horizontal) {
        this.fillColor = fillColor;
        this.emptyColor = emptyColor;
        this.showEmptyStrip = showEmptyStrip;
        this.radiusPx = radiusPx;
        this.colorScheme = colorScheme;
        this.lowColor = lowColor;
        this.horizontal = horizontal;
        invalidate();
    }

    public void setBarOrientation(boolean horizontal) {
        this.horizontal = horizontal;
        invalidate();
    }

    public void setInvert(boolean invert) {
        this.invert = invert;
        invalidate();
    }

    public void setShineConfig(boolean enabled, int speedMs, int widthPercent) {
        this.shineEnabled = enabled;
        this.shineSpeed = Math.max(200, speedMs);
        this.shineWidthPercent = Math.max(2, Math.min(98, widthPercent));
        updateAnimators();
        invalidate();
    }

    public void setWaveConfig(boolean enabled, int speedMs, int amplitudePercent) {
        this.waveEnabled = enabled;
        this.waveSpeed = Math.max(200, speedMs);
        this.waveAmplitudePercent = Math.max(10, Math.min(100, amplitudePercent));
        updateAnimators();
        invalidate();
    }

    public void setChargeWaveConfig(boolean enabled, int speedMs, int amplitudePercent) {
        this.chargeWaveEnabled = enabled;
        this.chargeWaveSpeed = Math.max(200, speedMs);
        this.chargeWaveAmplitudePercent = Math.max(10, Math.min(100, amplitudePercent));
        updateAnimators();
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
        if (charging && shineEnabled) {
            startShine();
        } else {
            stopShine();
        }
        if (chargeWaveActive()) {
            startChargeWave();
        } else {
            stopChargeWave();
        }
        if (waveActive()) {
            startWave();
        } else {
            stopWave();
        }
        if (low && !charging && fadeEnabled) {
            startFade();
        } else {
            stopFade();
            fillAlpha = 255f;
        }
    }

    private void startShine() {
        if (shineAnimator != null) return;
        shineAnimator = ValueAnimator.ofFloat(-0.5f, 1.5f);
        shineAnimator.setDuration(shineSpeed);
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

    private void startWave() {
        if (waveAnimator != null) return;
        waveAnimator = ValueAnimator.ofFloat(0f, 1f);
        waveAnimator.setDuration(waveSpeed);
        waveAnimator.setRepeatCount(ValueAnimator.INFINITE);
        waveAnimator.setInterpolator(new LinearInterpolator());
        waveAnimator.addUpdateListener(a -> {
            waveOffset = (float) a.getAnimatedValue();
            invalidate();
        });
        waveAnimator.start();
    }

    private void stopWave() {
        if (waveAnimator != null) {
            waveAnimator.cancel();
            waveAnimator = null;
        }
        waveOffset = 0f;
    }

    private boolean waveActive() {
        return low && !charging && waveEnabled;
    }

    private boolean chargeWaveActive() {
        return charging && chargeWaveEnabled;
    }

    private void startChargeWave() {
        if (chargeWaveAnimator != null) return;
        chargeWaveAnimator = ValueAnimator.ofFloat(0f, 1f);
        chargeWaveAnimator.setDuration(chargeWaveSpeed);
        chargeWaveAnimator.setRepeatCount(ValueAnimator.INFINITE);
        chargeWaveAnimator.setInterpolator(new LinearInterpolator());
        chargeWaveAnimator.addUpdateListener(a -> {
            chargeWaveOffset = (float) a.getAnimatedValue();
            invalidate();
        });
        chargeWaveAnimator.start();
    }

    private void stopChargeWave() {
        if (chargeWaveAnimator != null) {
            chargeWaveAnimator.cancel();
            chargeWaveAnimator = null;
        }
        chargeWaveOffset = 0f;
    }

    private float waveFactor(float t, float offset) {
        float wave = (float) Math.sin(2 * Math.PI * (t * WAVE_CYCLES + offset));
        return (wave + 1f) / 2f;
    }

    private void startFade() {
        if (fadeAnimator != null) return;
        long duration = fadeSpeed;
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

    private int fadeSpeed = 1800;

    public void setFadeSpeed(int speed) {
        this.fadeSpeed = Math.max(200, Math.min(2000, speed));
    }

    public void setFadeEnabled(boolean enabled) {
        this.fadeEnabled = enabled;
        updateAnimators();
        invalidate();
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
        if (colorScheme == BatteryBarConfig.SCHEME_HUE) {
            float hue, sat;
            if (percent <= 20f) {
                hue = 1f;
                sat = 0.7f;
            } else if (percent <= 50f) {
                float t = (percent - 21f) / 29f;
                hue = 2f + 98f * t;
                sat = 0.7f;
            } else {
                float t = (percent - 51f) / 49f;
                hue = 102f + 158f * t;
                sat = 0.71f + 0.29f * t;
            }
            barColor = Color.HSVToColor(255, new float[]{hue, sat, 1f});
        } else if (colorScheme == BatteryBarConfig.SCHEME_CLASSIC) {
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
                if (invert) {
                    rect.set(w - fillW, 0, w, h);
                } else {
                    rect.set(0, 0, fillW, h);
                }
            } else {
                float fillH = Math.max(0, h * (percent / 100f));
                if (invert) {
                    rect.set(0, 0, w, fillH);
                } else {
                    rect.set(0, h - fillH, w, h);
                }
            }
            if (chargeWaveActive()) {
                drawWaveFill(canvas, radius, barColor, w, h, true, chargeWaveAmplitudePercent / 100f, -chargeWaveOffset);
            } else if (waveActive()) {
                drawWaveFill(canvas, radius, barColor, w, h, false, waveAmplitudePercent / 100f, waveOffset);
            } else {
                canvas.drawRoundRect(rect, radius, radius, paint);
            }
            paint.setAlpha(255);
        }

        if (charging && shinePos >= -0.5f) {
            float bandWidth = Math.max(10f, (horizontal ? w : h) * (shineWidthPercent / 100f));
            if (horizontal) {
                float fillEdge = invert ? w - w * (percent / 100f) : w * (percent / 100f);
                float bandStart = (invert ? (1f - shinePos) : shinePos) * w - bandWidth / 2f;
                float bandEnd = bandStart + bandWidth;
                if (invert) {
                    drawShineSegment(canvas, radius, Math.max(bandStart, fillEdge), 0, Math.min(bandEnd, w), h, 0xFFFFFFFF);
                    drawShineSegment(canvas, radius, Math.max(bandStart, 0f), 0, Math.min(bandEnd, fillEdge), h, 0x66FFFFFF);
                } else {
                    drawShineSegment(canvas, radius, Math.max(bandStart, 0f), 0, Math.min(bandEnd, fillEdge), h, 0xFFFFFFFF);
                    drawShineSegment(canvas, radius, Math.max(bandStart, fillEdge), 0, Math.min(bandEnd, w), h, 0x66FFFFFF);
                }
            } else {
                float fillEdge = invert ? h * (percent / 100f) : h - h * (percent / 100f);
                float bandStart = (invert ? shinePos : (1f - shinePos)) * h - bandWidth / 2f;
                float bandEnd = bandStart + bandWidth;
                if (invert) {
                    drawShineSegment(canvas, radius, 0, Math.max(bandStart, 0f), w, Math.min(bandEnd, fillEdge), 0xFFFFFFFF);
                    drawShineSegment(canvas, radius, 0, Math.max(bandStart, fillEdge), w, Math.min(bandEnd, h), 0x66FFFFFF);
                } else {
                    drawShineSegment(canvas, radius, 0, Math.max(bandStart, fillEdge), w, Math.min(bandEnd, h), 0xFFFFFFFF);
                    drawShineSegment(canvas, radius, 0, Math.max(bandStart, 0f), w, Math.min(bandEnd, fillEdge), 0x66FFFFFF);
                }
            }
        }
    }

    private void drawWaveFill(Canvas canvas, float radius, int barColor, int w, int h,
                              boolean forceRight, float amp, float offset) {
        paint.setColor(barColor);
        paint.setAlpha(Math.round(fillAlpha * (1f - amp)));
        canvas.drawRoundRect(rect, radius, radius, paint);

        float fillLen = horizontal ? w * (percent / 100f) : h * (percent / 100f);
        int n = Math.max(8, (int) (fillLen / 4f));
        float segLen = fillLen / n;
        for (int i = 0; i < n; i++) {
            float t = i / (float) n;
            float boost = amp * waveFactor(t, offset);
            if (boost <= 0.001f) continue;
            paint.setAlpha(Math.round(fillAlpha * boost));
            if (horizontal) {
                float x0, x1;
                if (forceRight) {
                    x0 = segLen * i;
                    x1 = segLen * (i + 1);
                } else if (invert) {
                    x0 = w - segLen * (i + 1);
                    x1 = w - segLen * i;
                } else {
                    x0 = segLen * i;
                    x1 = segLen * (i + 1);
                }
                rect.set(x0, 0, x1, h);
            } else {
                float y0, y1;
                if (forceRight || !invert) {
                    y0 = h - segLen * (i + 1);
                    y1 = h - segLen * i;
                } else {
                    y0 = segLen * i;
                    y1 = segLen * (i + 1);
                }
                rect.set(0, y0, w, y1);
            }
            canvas.drawRect(rect, paint);
        }
        paint.setAlpha(255);
    }

    private void drawShineSegment(Canvas canvas, float radius, float l, float t, float r, float b, int color) {
        if (l >= r || t >= b) return;
        rect.set(l, t, r, b);
        paint.setColor(color);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN));
        canvas.drawRoundRect(rect, radius, radius, paint);
        paint.setXfermode(null);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopFade();
        stopShine();
        stopWave();
        stopChargeWave();
    }
}
