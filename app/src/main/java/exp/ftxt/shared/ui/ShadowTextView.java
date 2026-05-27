package exp.ftxt.shared.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.widget.TextView;

public class ShadowTextView extends TextView {

    private ShadowConfig shadowConfig;

    public ShadowTextView(Context context) {
        super(context);
    }

    public void setShadowConfig(ShadowConfig config) {
        shadowConfig = config;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (shadowConfig != null && shadowConfig.enabled) {
            getPaint().setShadowLayer(shadowConfig.blur, shadowConfig.offsetX,
                    shadowConfig.offsetY, shadowConfig.color);
        }
        super.onDraw(canvas);
        if (shadowConfig != null && shadowConfig.enabled) {
            getPaint().setShadowLayer(0, 0, 0, 0);
        }
    }
}
