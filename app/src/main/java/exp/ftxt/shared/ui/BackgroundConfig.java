package exp.ftxt.shared.ui;

public class BackgroundConfig {
    public boolean enabled = false;
    public int color = 0xCC000000;
    public int padding = 8;
    public int offsetX = 0;
    public int offsetY = 0;
    public int margin = 0;
    public int radius = 0;

    public BackgroundConfig() {}

    public BackgroundConfig(int padding) {
        this.padding = padding;
    }

    public BackgroundConfig(boolean enabled, int color, int padding,
                            int offsetX, int offsetY, int margin, int radius) {
        this.enabled = enabled;
        this.color = color;
        this.padding = padding;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.margin = margin;
        this.radius = radius;
    }
}
