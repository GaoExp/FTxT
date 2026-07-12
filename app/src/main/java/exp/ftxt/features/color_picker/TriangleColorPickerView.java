package exp.ftxt.features.color_picker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import exp.ftxt.shared.color.ColorMath;

public class TriangleColorPickerView extends View {

    private Paint ringPaint;
    private Paint trackerStroke;
    private Paint trackerFill;
    private Paint crosshairStroke;
    private Paint crosshairLight;
    private Paint crosshairDot;

    private float cx, cy;
    private float outerRadius;
    private float innerRadius;
    private float triRadius;

    private float hue = 0f;
    private float saturation = 1f;
    private float value = 1f;

    private Bitmap triangleBmp;
    private boolean triangleDirty = true;
    private boolean touchOnRing = false;

    private OnColorChangeListener listener;

    public interface OnColorChangeListener {
        void onColorChange(int color);
    }

    public TriangleColorPickerView(Context context) {
        super(context); init();
    }

    public TriangleColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs); init();
    }

    public TriangleColorPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr); init();
    }

    private void init() {
        ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ringPaint.setStyle(Paint.Style.STROKE);

        trackerStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        trackerStroke.setStyle(Paint.Style.STROKE);
        trackerStroke.setStrokeWidth(3f);
        trackerStroke.setColor(Color.BLACK);

        trackerFill = new Paint(Paint.ANTI_ALIAS_FLAG);
        trackerFill.setStyle(Paint.Style.FILL);
        trackerFill.setColor(Color.WHITE);

        crosshairStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        crosshairStroke.setStyle(Paint.Style.STROKE);
        crosshairStroke.setStrokeWidth(4f);
        crosshairStroke.setColor(Color.BLACK);

        crosshairLight = new Paint(Paint.ANTI_ALIAS_FLAG);
        crosshairLight.setStyle(Paint.Style.STROKE);
        crosshairLight.setStrokeWidth(2f);
        crosshairLight.setColor(Color.WHITE);

        crosshairDot = new Paint(Paint.ANTI_ALIAS_FLAG);
        crosshairDot.setStyle(Paint.Style.FILL);
        crosshairDot.setColor(Color.WHITE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        cx = getWidth() / 2f;
        cy = getHeight() / 2f;
        float maxRad = Math.min(cx, cy) - 16f;
        outerRadius = maxRad;
        innerRadius = outerRadius * 0.78f;
        triRadius = innerRadius * 0.88f;

        drawRing(canvas);
        drawTriangle(canvas);
        drawTracker(canvas);
        drawCrosshair(canvas);
    }

    private void drawRing(Canvas canvas) {
        ringPaint.setShader(new SweepGradient(cx, cy, ColorMath.generateHueColors(), null));
        ringPaint.setStrokeWidth(outerRadius - innerRadius);
        float midR = (outerRadius + innerRadius) / 2f;
        canvas.drawCircle(cx, cy, midR, ringPaint);
    }

    private void drawTracker(Canvas canvas) {
        float midR = (outerRadius + innerRadius) / 2f;
        float a = (float) Math.toRadians(hue);
        float px = cx + midR * (float) Math.cos(a);
        float py = cy + midR * (float) Math.sin(a);
        float r = (outerRadius - innerRadius) * 0.45f;
        canvas.drawCircle(px, py, r + 1, trackerStroke);
        canvas.drawCircle(px, py, r, trackerFill);
    }

    private void drawTriangle(Canvas canvas) {
        float scale = 0.5f;
        int bmpSize = (int) ((triRadius * 2 + 4) * scale);
        if (bmpSize <= 0) return;

        if (triangleBmp == null || triangleBmp.getWidth() != bmpSize || triangleDirty) {
            triangleBmp = Bitmap.createBitmap(bmpSize, bmpSize, Bitmap.Config.ARGB_8888);

            float triR = triRadius * scale;
            float bmpCx = bmpSize / 2f;
            float bmpCy = bmpSize / 2f;

            float ax = bmpCx, ay = bmpCy - triR;
            float bx = bmpCx - triR * 0.866f, by = bmpCy + triR * 0.5f;
            float cxv = bmpCx + triR * 0.866f, cyv = bmpCy + triR * 0.5f;

            float v0x = bx - ax, v0y = by - ay;
            float v1x = cxv - ax, v1y = cyv - ay;

            float dot00 = v0x * v0x + v0y * v0y;
            float dot01 = v0x * v1x + v0y * v1y;
            float dot11 = v1x * v1x + v1y * v1y;
            float invDenom = 1f / (dot00 * dot11 - dot01 * dot01);

            float hueFraction = hue / 60f;
            int hueIndex = ((int) hueFraction) % 6;
            float hueFrac = hueFraction - hueIndex;
            if (hueFrac < 0) hueFrac += 1f;

            int[] pixels = new int[bmpSize * bmpSize];
            for (int py = 0; py < bmpSize; py++) {
                for (int px = 0; px < bmpSize; px++) {
                    float v2x = px - ax, v2y = py - ay;
                    float dot02 = v0x * v2x + v0y * v2y;
                    float dot12 = v1x * v2x + v1y * v2y;

                    float u = (dot11 * dot02 - dot01 * dot12) * invDenom;
                    float v = (dot00 * dot12 - dot01 * dot02) * invDenom;

                    if (u >= 0 && v >= 0 && (u + v) <= 1f) {
                        float s = 1f - v;
                        float vl = 1f - u;

                        float p = vl * (1f - s);
                        float q = vl * (1f - s * hueFrac);
                        float t = vl * (1f - s * (1f - hueFrac));

                        float rf, gf, bf;
                        switch (hueIndex) {
                            case 0: rf = vl; gf = t; bf = p; break;
                            case 1: rf = q; gf = vl; bf = p; break;
                            case 2: rf = p; gf = vl; bf = t; break;
                            case 3: rf = p; gf = q; bf = vl; break;
                            case 4: rf = t; gf = p; bf = vl; break;
                            default: rf = vl; gf = p; bf = q; break;
                        }
                        int ri = (int) (rf * 255);
                        int gi = (int) (gf * 255);
                        int bi = (int) (bf * 255);
                        pixels[py * bmpSize + px] = 0xFF000000 | (ri << 16) | (gi << 8) | bi;
                    } else {
                        pixels[py * bmpSize + px] = 0;
                    }
                }
            }
            triangleBmp.setPixels(pixels, 0, bmpSize, 0, 0, bmpSize, bmpSize);
            triangleDirty = false;
        }

        float drawSize = bmpSize / scale;
        canvas.drawBitmap(triangleBmp, null,
                new RectF(cx - drawSize / 2f, cy - drawSize / 2f,
                        cx + drawSize / 2f, cy + drawSize / 2f), null);
    }

    private void drawCrosshair(Canvas canvas) {
        float ax = cx, ay = cy - triRadius;
        float bx = cx - triRadius * 0.866f, by = cy + triRadius * 0.5f;
        float cxv = cx + triRadius * 0.866f, cyv = cy + triRadius * 0.5f;

        float u = 1f - value;
        float v = 1f - saturation;
        u = Math.max(0, Math.min(1, u));
        v = Math.max(0, Math.min(1 - u, v));

        float px = ax + u * (bx - ax) + v * (cxv - ax);
        float py = ay + u * (by - ay) + v * (cyv - ay);

        float cross = Math.min(innerRadius * 0.12f, 18f);
        float ring = Math.min(innerRadius * 0.07f, 10f);

        crosshairStroke.setStrokeWidth(5f);
        canvas.drawLine(px - cross, py, px + cross, py, crosshairStroke);
        canvas.drawLine(px, py - cross, px, py + cross, crosshairStroke);
        canvas.drawCircle(px, py, ring, crosshairStroke);

        crosshairLight.setStrokeWidth(3f);
        canvas.drawLine(px - cross, py, px + cross, py, crosshairLight);
        canvas.drawLine(px, py - cross, px, py + cross, crosshairLight);
        canvas.drawCircle(px, py, ring, crosshairLight);

        canvas.drawCircle(px, py, 3f, crosshairDot);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        float x = event.getX();
        float y = event.getY();
        float dx = x - cx;
        float dy = y - cy;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
            if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(true);
        }

        if (action == MotionEvent.ACTION_DOWN) {
            touchOnRing = dist > innerRadius;
        }

        if (action != MotionEvent.ACTION_DOWN && action != MotionEvent.ACTION_MOVE) {
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(false);
            }
            return true;
        }

        if (touchOnRing) {
            float newHue = (float) Math.toDegrees(Math.atan2(dy, dx));
            if (newHue < 0) newHue += 360;
            if (newHue != hue) {
                hue = newHue;
                triangleDirty = true;
            }
        } else {
            float ax = cx, ay = cy - triRadius;
            float bx = cx - triRadius * 0.866f, by = cy + triRadius * 0.5f;
            float cxv = cx + triRadius * 0.866f, cyv = cy + triRadius * 0.5f;

            float v0x = bx - ax, v0y = by - ay;
            float v1x = cxv - ax, v1y = cyv - ay;
            float v2x = x - ax, v2y = y - ay;

            float dot00 = v0x * v0x + v0y * v0y;
            float dot01 = v0x * v1x + v0y * v1y;
            float dot11 = v1x * v1x + v1y * v1y;
            float invDenom = 1f / (dot00 * dot11 - dot01 * dot01);
            float dot02 = v0x * v2x + v0y * v2y;
            float dot12 = v1x * v2x + v1y * v2y;

            float u = (dot11 * dot02 - dot01 * dot12) * invDenom;
            float v = (dot00 * dot12 - dot01 * dot02) * invDenom;

            u = Math.max(0, Math.min(1, u));
            v = Math.max(0, Math.min(1, v));
            if (u + v > 1f) {
                float s = 1f / (u + v);
                u *= s;
                v *= s;
            }
            saturation = 1f - v;
            value = 1f - u;
        }

        invalidate();

        if (listener != null) {
            listener.onColorChange(Color.HSVToColor(
                    new float[]{hue, saturation, value}));
        }
        return true;
    }

    public void setOnColorChangeListener(OnColorChangeListener l) {
        listener = l;
    }

    public int getCurrentColor() {
        return Color.HSVToColor(new float[]{hue, saturation, value});
    }

    public void setColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        boolean achromatic = hsv[1] == 0f || hsv[2] == 0f;
        if (!achromatic && hsv[0] != hue) triangleDirty = true;
        if (!achromatic) hue = hsv[0];
        saturation = Math.min(hsv[1], 1f);
        value = Math.min(hsv[2], 1f);
        invalidate();
    }
}
