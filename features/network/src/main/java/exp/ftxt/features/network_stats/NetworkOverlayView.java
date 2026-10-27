package exp.ftxt.features.network_stats;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.widget.LinearLayout;

import java.util.Locale;

import exp.ftxt.shared.config.BackgroundConfig;
import exp.ftxt.shared.config.ShadowConfig;
import exp.ftxt.shared.ui.ShadowTextView;

public class NetworkOverlayView extends LinearLayout {

    private static final int GAP_DP = 8;
    private static final double BAR_MAX_BYTES = 10d * 1024d * 1024d;
    private static final double BAR_MAX_LOG = Math.log10(BAR_MAX_BYTES);

    private final BadgeView downBadge;
    private final BadgeView upBadge;
    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int gapPx;

    private int textColor = NetworkConfig.color;
    private int labelColor = NetworkConfig.labelColor;
    private boolean textBold = NetworkConfig.textBold;
    private boolean textItalic = NetworkConfig.textItalic;
    private int barColorDown = NetworkConfig.barColorDown;
    private int barColorUp = NetworkConfig.barColorUp;
    private boolean barEnabled = NetworkConfig.barEnabled;
    private boolean barInverted = NetworkConfig.barInverted;
    private boolean vertical = NetworkConfig.verticalLayout;
    private int badgeWidthDp = NetworkConfig.displayWidthDp;
    private float downFraction;
    private float upFraction;

    private boolean bgEnabled;
    private int bgColor;
    private int bgOffsetX;
    private int bgOffsetY;
    private int bgMargin;
    private int bgRadius;

    public NetworkOverlayView(Context context) {
        super(context);
        gapPx = dpToPx(GAP_DP);
        setWillNotDraw(false);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        downBadge = new BadgeView(context);
        upBadge = new BadgeView(context);
        addView(downBadge);
        addView(upBadge);

        setBarColors(barColorDown, barColorUp);
        setBarEnabled(barEnabled);
        setTextStyleConfig(textBold, textItalic);
        applyBadgeLayout();
    }

    public void setOverlayTextSize(float size) {
        downBadge.setTextSize(size);
        upBadge.setTextSize(size);
        requestLayout();
    }

    public void setTextColors(int color, int labelColor) {
        this.textColor = color;
        this.labelColor = labelColor;
        downBadge.setTextColor(color);
        upBadge.setTextColor(color);
    }

    public void setTextStyleConfig(boolean bold, boolean italic) {
        textBold = bold;
        textItalic = italic;
        downBadge.setTextStyle(bold, italic);
        upBadge.setTextStyle(bold, italic);
        requestLayout();
    }

    public void setShadowConfig(ShadowConfig config) {
        downBadge.setShadowConfig(config);
        upBadge.setShadowConfig(config);
    }

    public void setBadgeWidthDp(int widthDp) {
        int clamped = Math.max(NetworkConfig.MIN_WIDTH_DP,
                Math.min(NetworkConfig.MAX_WIDTH_DP, widthDp));
        if (badgeWidthDp == clamped) return;
        badgeWidthDp = clamped;
        applyBadgeLayout();
    }

    public void setVerticalLayout(boolean vertical) {
        if (this.vertical == vertical) return;
        this.vertical = vertical;
        applyBadgeLayout();
    }

    public void setBarEnabled(boolean enabled) {
        barEnabled = enabled;
        invalidate();
    }

    public void setBarInverted(boolean inverted) {
        if (barInverted == inverted) return;
        barInverted = inverted;
        invalidate();
    }

    public void setBarColors(int downColor, int upColor) {
        barColorDown = downColor;
        barColorUp = upColor;
        invalidate();
    }

    public void setBackgroundConfig(BackgroundConfig config) {
        bgEnabled = config.enabled;
        bgColor = config.color;
        bgOffsetX = config.offsetX;
        bgOffsetY = config.offsetY;
        bgMargin = config.margin;
        bgRadius = config.radius;
        int pad = config.enabled ? config.padding : 0;
        setPadding(pad, pad, pad, pad);
        requestLayout();
        invalidate();
    }

    public void updateSpeeds(long rxBytesPerSec, long txBytesPerSec, boolean valueOnly) {
        long rx = Math.max(0, rxBytesPerSec);
        long tx = Math.max(0, txBytesPerSec);

        String down = formatUnit(rx);
        String up = formatUnit(tx);

        if (valueOnly) {
            setBadgeText(downBadge, stripUnit(down));
            setBadgeText(upBadge, stripUnit(up));
        } else {
            setBadgeText(downBadge, "↓" + down);
            setBadgeText(upBadge, "↑" + up);
        }

        downFraction = fraction(rx);
        upFraction = fraction(tx);
        invalidate();
    }

    private void setBadgeText(BadgeView badge, String text) {
        badge.setTextColor(textColor);
        if (text.indexOf('↓') < 0 && text.indexOf('↑') < 0) {
            badge.setText(text);
            return;
        }
        SpannableString spannable = new SpannableString(text);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '↓' || c == '↑' || c == 'M' || c == 'B' || c == 'K' || c == '/' || c == 's') {
                spannable.setSpan(new ForegroundColorSpan(labelColor), i, i + 1, 0);
            }
        }
        badge.setText(spannable);
    }

    private void applyBadgeLayout() {
        int width = dpToPx(badgeWidthDp);
        LayoutParams downParams = new LayoutParams(width, LayoutParams.WRAP_CONTENT);
        LayoutParams upParams = new LayoutParams(width, LayoutParams.WRAP_CONTENT);
        if (vertical) {
            setOrientation(VERTICAL);
            setGravity(Gravity.CENTER_HORIZONTAL);
            upParams.topMargin = gapPx;
        } else {
            setOrientation(HORIZONTAL);
            setGravity(Gravity.CENTER_VERTICAL);
            upParams.setMarginStart(gapPx);
        }
        downBadge.setLayoutParams(downParams);
        upBadge.setLayoutParams(upParams);
        requestLayout();
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private static String formatUnit(long bytesPerSec) {
        if (bytesPerSec >= 1048576L) {
            return String.format(Locale.US, "%.2fMB/s", bytesPerSec / 1048576.0);
        }
        return (bytesPerSec / 1024L) + "KB/s";
    }

    private static String stripUnit(String value) {
        return value.replace("MB/s", "").replace("KB/s", "");
    }

    private static float fraction(long bytesPerSec) {
        if (bytesPerSec <= 0) return 0f;
        double ratio = Math.log10(bytesPerSec) / BAR_MAX_LOG;
        ratio = Math.max(0d, Math.min(1d, ratio));
        return (float) (ratio * ratio * ratio * ratio);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (bgEnabled) {
            bgPaint.setColor(bgColor);
            canvas.save();
            canvas.translate(bgOffsetX, bgOffsetY);
            float left = -bgMargin;
            float top = -bgMargin;
            float right = getWidth() + bgMargin;
            float bottom = getHeight() + bgMargin;
            if (bgRadius > 0) {
                canvas.drawRoundRect(left, top, right, bottom, bgRadius, bgRadius, bgPaint);
            } else {
                canvas.drawRect(left, top, right, bottom, bgPaint);
            }
            canvas.restore();
        }
        super.onDraw(canvas);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (barEnabled) {
            drawBar(canvas, downBadge, downFraction, barColorDown);
            drawBar(canvas, upBadge, upFraction, barColorUp);
        }
        super.dispatchDraw(canvas);
    }

    private void drawBar(Canvas canvas, BadgeView badge, float fraction, int color) {
        int width = badge.getWidth();
        int height = badge.getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }
        int left = badge.getLeft();
        int top = badge.getTop();
        int right = left + width;
        int bottom = top + height;

        canvas.save();
        canvas.clipRect(left, top, right, bottom);
        if (fraction > 0f) {
            float barWidth = width * fraction;
            float start = barInverted ? right - barWidth : left;
            float end = barInverted ? right : left + barWidth;
            barPaint.setColor(color);
            canvas.drawRect(start, top, end, bottom, barPaint);
        }
        canvas.restore();
    }

    private static class BadgeView extends ShadowTextView {

        private final Typeface baseTypeface;

        BadgeView(Context context) {
            super(context);
            baseTypeface = getTypeface();
            setGravity(Gravity.CENTER);
            setSingleLine(true);
            setIncludeFontPadding(false);
        }

        void setTextStyle(boolean bold, boolean italic) {
            int style = Typeface.NORMAL;
            if (bold && italic) {
                style = Typeface.BOLD_ITALIC;
            } else if (bold) {
                style = Typeface.BOLD;
            } else if (italic) {
                style = Typeface.ITALIC;
            }
            setTypeface(baseTypeface, style);
        }
    }
}
