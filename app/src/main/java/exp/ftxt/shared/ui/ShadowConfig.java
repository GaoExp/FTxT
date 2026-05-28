package exp.ftxt.shared.ui;

import android.graphics.Color;

public class ShadowConfig {
    public boolean enabled = false;
    public int color = Color.BLACK;
    public float blur = 5f;
    public float offsetX = 0f;
    public float offsetY = 0f;

    public ShadowConfig() {}

    public ShadowConfig(boolean enabled, int color, float blur, float offsetX, float offsetY) {
        this.enabled = enabled;
        this.color = color;
        this.blur = blur;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }
}
