package exp.ftxt.shared.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.appcompat.widget.AppCompatImageView;

import exp.ftxt.shared.config.ShadowConfig;

public class ShadowImageView extends AppCompatImageView {

    private ShadowConfig shadowConfig;
    private boolean tintEnabled = false;
    private int tintColor;
    private boolean bgEnabled;
    private int bgColor;
    private int bgOffsetX;
    private int bgOffsetY;
    private int bgMargin = 0;
    private int bgRadius = 0;
    private final Paint bgPaint;
    private final Paint imagePaint;
    private Paint shadowPaint;
    private Bitmap originalBitmap;
    private Bitmap tintedBitmap;
    private boolean suppressLayout = false;

    private int shadowPadExtra = 0;
    private int basePadLeft, basePadTop, basePadRight, basePadBottom;
    private Bitmap shadowBitmap;
    private int shadowBmpPad;

    public ShadowImageView(Context context) {
        super(context);
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStyle(Paint.Style.FILL);
        imagePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        basePadLeft = left;
        basePadTop = top;
        basePadRight = right;
        basePadBottom = bottom;
        applyEffectivePadding();
    }

    private void applyEffectivePadding() {
        super.setPadding(
                basePadLeft + shadowPadExtra,
                basePadTop + shadowPadExtra,
                basePadRight + shadowPadExtra,
                basePadBottom + shadowPadExtra);
    }

    private void updateShadowPadding() {
        int old = shadowPadExtra;
        if (shadowConfig != null && shadowConfig.enabled) {
            float blur = Math.max(1f, shadowConfig.blur);
            float maxOffset = Math.max(Math.abs(shadowConfig.offsetX),
                    Math.abs(shadowConfig.offsetY));
            shadowPadExtra = (int) Math.ceil(blur + maxOffset);
        } else {
            shadowPadExtra = 0;
        }
        if (old != shadowPadExtra) {
            applyEffectivePadding();
        }
    }

    @Override
    public void setImageResource(int resId) {
        suppressLayout = true;
        super.setImageResource(resId);
        suppressLayout = false;
        cacheOriginal();
        buildTintedBitmap();
        buildShadowBitmap();
        invalidate();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        suppressLayout = true;
        super.setImageDrawable(drawable);
        suppressLayout = false;
        cacheOriginal();
        buildTintedBitmap();
        buildShadowBitmap();
        invalidate();
    }

    public void setShadowConfig(ShadowConfig config) {
        shadowConfig = config;
        buildShadowBitmap();
        updateShadowPadding();
        invalidate();
    }

    public int getShadowPadExtra() {
        return shadowPadExtra;
    }

    public void setTintEnabled(boolean enabled) {
        tintEnabled = enabled;
        buildTintedBitmap();
        invalidate();
    }

    public void setTintColor(int color) {
        tintColor = color;
        if (tintEnabled) {
            buildTintedBitmap();
            invalidate();
        }
    }

    public void setBgEnabled(boolean enabled) { bgEnabled = enabled; invalidate(); }
    public void setBgColor(int color) { bgColor = color; invalidate(); }
    public void setBgOffsetX(int x) { bgOffsetX = x; invalidate(); }
    public void setBgOffsetY(int y) { bgOffsetY = y; invalidate(); }
    public void setBgMargin(int margin) { bgMargin = margin; invalidate(); }
    public void setBgRadius(int radius) { bgRadius = radius; invalidate(); }

    private void updateLayerType() {
        setLayerType(shadowConfig != null && shadowConfig.enabled
                ? LAYER_TYPE_SOFTWARE : LAYER_TYPE_NONE, null);
    }

    private void cacheOriginal() {
        Drawable d = getDrawable();
        if (d == null) { originalBitmap = null; return; }
        if (d instanceof BitmapDrawable) {
            originalBitmap = ((BitmapDrawable) d).getBitmap();
            return;
        }
        int w = d.getIntrinsicWidth();
        int h = d.getIntrinsicHeight();
        if (w > 0 && h > 0) {
            originalBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(originalBitmap);
            d.setBounds(0, 0, w, h);
            d.draw(c);
        } else {
            originalBitmap = null;
        }
    }

    private void buildTintedBitmap() {
        if (originalBitmap == null || !tintEnabled) {
            tintedBitmap = null;
            return;
        }
        int w = originalBitmap.getWidth();
        int h = originalBitmap.getHeight();
        int[] px = new int[w * h];
        originalBitmap.getPixels(px, 0, w, 0, 0, w, h);

        int tr = (tintColor >> 16) & 0xFF;
        int tg = (tintColor >> 8) & 0xFF;
        int tb = tintColor & 0xFF;

        for (int i = 0; i < px.length; i++) {
            int a = (px[i] >> 24) & 0xFF;
            if (a > 0) {
                px[i] = (a << 24) | (tr << 16) | (tg << 8) | tb;
            }
        }

        tintedBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        tintedBitmap.setPixels(px, 0, w, 0, 0, w, h);
    }

    private void buildShadowBitmap() {
        if (shadowBitmap != null) {
            shadowBitmap.recycle();
            shadowBitmap = null;
        }
        if (originalBitmap == null || shadowConfig == null || !shadowConfig.enabled) {
            return;
        }
        int w = originalBitmap.getWidth();
        int h = originalBitmap.getHeight();
        float blur = Math.max(1f, shadowConfig.blur);
        int pad = (int) Math.ceil(blur) + 2;

        int[] px = new int[w * h];
        originalBitmap.getPixels(px, 0, w, 0, 0, w, h);
        int sr = (shadowConfig.color >> 16) & 0xFF;
        int sg = (shadowConfig.color >> 8) & 0xFF;
        int sb = shadowConfig.color & 0xFF;
        int sa = (shadowConfig.color >> 24) & 0xFF;
        if (sa == 0) sa = 255;
        for (int i = 0; i < px.length; i++) {
            int a = (px[i] >> 24) & 0xFF;
            if (a > 0) {
                px[i] = (a * sa / 255 << 24) | (sr << 16) | (sg << 8) | sb;
            } else {
                px[i] = 0;
            }
        }
        Bitmap sil = Bitmap.createBitmap(px, w, h, Bitmap.Config.ARGB_8888);

        int bw = w + pad * 2;
        int bh = h + pad * 2;
        Bitmap padded = Bitmap.createBitmap(bw, bh, Bitmap.Config.ARGB_8888);
        new Canvas(padded).drawBitmap(sil, pad, pad, null);
        sil.recycle();

        Paint bp = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        bp.setMaskFilter(new BlurMaskFilter(blur, BlurMaskFilter.Blur.NORMAL));
        shadowBitmap = Bitmap.createBitmap(bw, bh, Bitmap.Config.ARGB_8888);
        new Canvas(shadowBitmap).drawBitmap(padded, 0, 0, bp);
        padded.recycle();

        shadowBmpPad = pad;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (bgEnabled) {
            bgPaint.setColor(bgColor);
            canvas.save();
            canvas.translate(bgOffsetX, bgOffsetY);
            float l = -bgMargin, t = -bgMargin;
            float r = getWidth() + bgMargin, b = getHeight() + bgMargin;
            if (bgRadius > 0) {
                canvas.drawRoundRect(l, t, r, b, bgRadius, bgRadius, bgPaint);
            } else {
                canvas.drawRect(l, t, r, b, bgPaint);
            }
            canvas.restore();
        }

        Bitmap drawSrc = (tintEnabled && tintedBitmap != null) ? tintedBitmap : originalBitmap;

        if (drawSrc != null) {
            Rect dst = new Rect(getPaddingLeft(), getPaddingTop(),
                    getWidth() - getPaddingRight(), getHeight() - getPaddingBottom());

            if (shadowBitmap != null && shadowConfig != null && shadowConfig.enabled
                    && originalBitmap != null && dst.width() > 0 && dst.height() > 0) {
                float sx = (float) dst.width() / originalBitmap.getWidth();
                float sy = (float) dst.height() / originalBitmap.getHeight();
                canvas.save();
                canvas.translate(
                        dst.left + shadowConfig.offsetX - shadowBmpPad * sx,
                        dst.top + shadowConfig.offsetY - shadowBmpPad * sy);
                canvas.scale(sx, sy);
                canvas.drawBitmap(shadowBitmap, 0, 0, null);
                canvas.restore();
            }

            canvas.drawBitmap(drawSrc, null, dst, imagePaint);
            return;
        }

        super.onDraw(canvas);
    }
}
