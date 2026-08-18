package exp.ftxt.shared.preset;

import exp.ftxt.shared.ui.ShadowConfig;

public class OverlayPreset {

    public float posX;
    public float posY;
    public float size;

    public int color;

    public ShadowConfig shadow;

    public boolean bgEnabled;
    public int bgColor;
    public int bgPadding;
    public int bgOffsetX;
    public int bgOffsetY;
    public int bgMargin;
    public int bgRadius;

    public String orientation;

    public String moduleType;

    public Boolean touchPassthrough;
    public Boolean safeArea;

    public String textContent;

    public Integer labelColor;
    public Integer separatorColor;

    public Boolean showOnlyValue;

    public Boolean showTemperature;
    public Boolean showPercentage;

    public Boolean showVoltage;
    public Boolean showCurrent;
    public Boolean showPower;

    public String itemOrder;

    public Boolean showJavaHeap;
    public Boolean showNativeHeap;
    public Boolean showGraphics;
    public Boolean showTotal;

    public Boolean quickMode;
    public String quickSide;
    public Boolean barHorizontal;
    public Boolean barInvert;
    public Float barLength;
    public Integer barThickness;
    public Boolean autoColor;
    public Integer barColorScheme;
    public Integer lowColor;
    public Integer lowThreshold;
    public Boolean showEmptyStrip;
    public Integer emptyColor;
    public Integer barRadius;
    public Integer fadeSpeed;
    public Boolean fadeEnabled;
    public Boolean shineEnabled;
    public Integer shineSpeed;
    public Integer shineWidth;
    public Boolean waveEnabled;
    public Integer waveSpeed;
    public Integer waveAmplitude;
    public Boolean chargeWaveEnabled;
    public Integer chargeWaveSpeed;
    public Integer chargeWaveAmplitude;

    public OverlayPreset() {
        shadow = new ShadowConfig();
    }

    public OverlayPreset(
            float posX, float posY, float size,
            int color,
            ShadowConfig shadow,
            boolean bgEnabled, int bgColor, int bgPadding,
            int bgOffsetX, int bgOffsetY, int bgMargin, int bgRadius,
            String orientation
    ) {
        this.posX = posX;
        this.posY = posY;
        this.size = size;
        this.color = color;
        this.shadow = (shadow != null) ? shadow : new ShadowConfig();
        this.bgEnabled = bgEnabled;
        this.bgColor = bgColor;
        this.bgPadding = bgPadding;
        this.bgOffsetX = bgOffsetX;
        this.bgOffsetY = bgOffsetY;
        this.bgMargin = bgMargin;
        this.bgRadius = bgRadius;
        this.orientation = orientation;
    }
}
