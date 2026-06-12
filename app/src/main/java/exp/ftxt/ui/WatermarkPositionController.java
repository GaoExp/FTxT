package exp.ftxt.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import exp.ftxt.R;
import exp.ftxt.core.FloatingService;
import exp.ftxt.features.watermark.WatermarkConfig;
import exp.ftxt.features.watermark.WatermarkModule;
import exp.ftxt.shared.preset.OverlayPreset;
import exp.ftxt.shared.preset.PresetHandler;
import exp.ftxt.shared.ui.DpadController;
import exp.ftxt.shared.ui.SliderPositionController;

public class WatermarkPositionController {

    private final Activity activity;
    private final SharedPreferences prefs;

    private View btnUp, btnDown, btnLeft, btnRight;
    private String currentOrientation;

    private DpadController dpad;
    private SliderPositionController sliderController;
    private TextView coordDisplay;
    private TextView activePresetLabel;
    private int displayWidth, displayHeight;

    private static final String PREFS_NAME = "ftxt_prefs";
    private final PresetHandler.StringHolder activePresetName = new PresetHandler.StringHolder();

    private final PresetHandler.Delegate delegate = new PresetHandler.Delegate() {
        @Override
        public String moduleLabel() { return "Watermark"; }
        @Override
        public String touchPassthroughPrefKey() { return "watermark_lock"; }
        @Override
        public String safeAreaPrefKey() { return "watermark_safe_area"; }
        @Override
        public String posXPrefKey() { return "watermark_pos_x"; }
        @Override
        public String posYPrefKey() { return "watermark_pos_y"; }

        @Override
        public void saveToPreset(OverlayPreset p) {
            p.posX = WatermarkConfig.posX;
            p.posY = WatermarkConfig.posY;
            p.size = WatermarkConfig.size;
            p.color = WatermarkConfig.color;
            p.shadow = PresetHandler.copyShadow(WatermarkConfig.shadow);
            p.bgEnabled = WatermarkConfig.bg.enabled;
            p.bgColor = WatermarkConfig.bg.color;
            p.bgPadding = WatermarkConfig.bg.padding;
            p.bgOffsetX = WatermarkConfig.bg.offsetX;
            p.bgOffsetY = WatermarkConfig.bg.offsetY;
            p.bgMargin = WatermarkConfig.bg.margin;
            p.bgRadius = WatermarkConfig.bg.radius;
            p.touchPassthrough = WatermarkConfig.touchPassthrough;
            p.safeArea = WatermarkConfig.safeArea;
            p.textContent = WatermarkConfig.text;
        }

        @Override
        public void applyFromPreset(Activity activity, OverlayPreset p, SharedPreferences prefs) {
            WatermarkConfig.posX = p.posX;
            WatermarkConfig.posY = p.posY;
            WatermarkConfig.size = p.size;
            WatermarkConfig.color = p.color;
            if (p.shadow != null) {
                WatermarkConfig.shadow.enabled = p.shadow.enabled;
                WatermarkConfig.shadow.color = p.shadow.color;
                WatermarkConfig.shadow.blur = p.shadow.blur;
                WatermarkConfig.shadow.offsetX = p.shadow.offsetX;
                WatermarkConfig.shadow.offsetY = p.shadow.offsetY;
            }
            WatermarkConfig.bg.enabled = p.bgEnabled;
            WatermarkConfig.bg.color = p.bgColor;
            WatermarkConfig.bg.padding = p.bgPadding;
            WatermarkConfig.bg.offsetX = p.bgOffsetX;
            WatermarkConfig.bg.offsetY = p.bgOffsetY;
            WatermarkConfig.bg.margin = p.bgMargin;
            WatermarkConfig.bg.radius = p.bgRadius;
            if (p.touchPassthrough != null) {
                WatermarkConfig.touchPassthrough = p.touchPassthrough;
                prefs.edit().putBoolean("watermark_lock", WatermarkConfig.touchPassthrough).apply();
            }
            if (p.safeArea != null) {
                WatermarkConfig.safeArea = p.safeArea;
                prefs.edit().putBoolean("watermark_safe_area", WatermarkConfig.safeArea).apply();
            }
            if (p.textContent != null) {
                WatermarkConfig.text = p.textContent;
                prefs.edit().putString("watermark_text", WatermarkConfig.text).apply();
            }
        }

        @Override
        public void syncToService() {
            FloatingService.updateWatermarkPositionStatic();
            FloatingService.updateWatermarkSizeStatic();
            FloatingService.updateWatermarkColorStatic();
            FloatingService.updateWatermarkShadowStatic();
            FloatingService.updateWatermarkBackgroundStatic();
            FloatingService.updateWatermarkTextStatic();
        }
    };

    public WatermarkPositionController(Activity activity) {
        this.activity = activity;
        this.prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        int orientation = activity.getResources().getConfiguration().orientation;
        currentOrientation = (orientation == Configuration.ORIENTATION_LANDSCAPE) ? "land" : "port";
        loadPositionFromPrefs(currentOrientation);

        FloatingService.setWatermarkOrientationSuffixStatic(currentOrientation);

        bindViews();

        WindowManager wm = activity.getWindowManager();
        DisplayMetrics realMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(realMetrics);
        displayWidth = realMetrics.widthPixels;
        displayHeight = realMetrics.heightPixels;

        WatermarkModule.onPositionUpdate = this::syncAll;

        sliderController = new SliderPositionController(
                activity.findViewById(R.id.watermark_posXSeekBar),
                activity.findViewById(R.id.watermark_posYSeekBar),
                activity.findViewById(R.id.watermark_posXLabel),
                activity.findViewById(R.id.watermark_posYLabel),
                (x, y) -> onPositionChanged(x, y)
        );
        setupListeners();
        syncAll();
    }

    private void bindViews() {
        btnUp = activity.findViewById(R.id.watermark_btnUp);
        btnDown = activity.findViewById(R.id.watermark_btnDown);
        btnLeft = activity.findViewById(R.id.watermark_btnLeft);
        btnRight = activity.findViewById(R.id.watermark_btnRight);
        coordDisplay = activity.findViewById(R.id.watermark_posCoordDisplay);
        activePresetLabel = activity.findViewById(R.id.watermark_txtActivePreset);
    }

    private void setupListeners() {
        dpad = new DpadController(btnUp, btnDown, btnLeft, btnRight, (dx, dy) -> {
            onPositionChanged(clamp(WatermarkConfig.posX + dx), clamp(WatermarkConfig.posY + dy));
        });

        View btnSavePreset = activity.findViewById(R.id.watermark_btnSavePreset);
        if (btnSavePreset != null) {
            btnSavePreset.setOnClickListener(v -> PresetHandler.showSavePresetDialog(activity, delegate));
        }

        View btnLoadPreset = activity.findViewById(R.id.watermark_btnLoadPreset);
        if (btnLoadPreset != null) {
            btnLoadPreset.setOnClickListener(v -> showLoadPresetDialog());
        }
    }

    public void showLoadPresetDialog() {
        PresetHandler.showLoadPresetDialog(activity, delegate, activePresetName, this::syncAll);
    }

    private static float clamp(float val) {
        return Math.max(0, Math.min(1, val));
    }

    private void onPositionChanged(float x, float y) {
        WatermarkConfig.posX = x;
        WatermarkConfig.posY = y;
        syncAll();
        PresetHandler.savePositionToPrefs(prefs, delegate, currentOrientation, x, y);
        FloatingService.updateWatermarkPositionStatic();
    }

    private void loadPositionFromPrefs(String orient) {
        String sfx = "_" + orient;
        WatermarkConfig.posX = prefs.getFloat("watermark_pos_x" + sfx, 0.5f);
        WatermarkConfig.posY = prefs.getFloat("watermark_pos_y" + sfx, 0.5f);
    }

    public void cleanup() {
        WatermarkModule.onPositionUpdate = null;
        if (dpad != null) dpad.cleanup();
    }

    public void refresh() {
        syncAll();
    }

    public void syncAll() {
        sliderController.sync(WatermarkConfig.posX, WatermarkConfig.posY);
        updateCoordDisplay();
        updateActivePresetLabel();
    }

    private void updateActivePresetLabel() {
        if (activePresetLabel == null) return;
        String name = activePresetName.value;
        if (name != null && !name.isEmpty()) {
            activePresetLabel.setText("Aktif: " + name);
            activePresetLabel.setVisibility(View.VISIBLE);
        } else {
            activePresetLabel.setVisibility(View.GONE);
        }
    }

    private void updateCoordDisplay() {
        if (coordDisplay == null) return;
        int px, py;
        int[] pos = FloatingService.getWatermarkCurrentPosition();
        if (pos != null) {
            px = pos[0];
            py = pos[1];
        } else {
            px = Math.round(WatermarkConfig.posX * displayWidth);
            py = Math.round(WatermarkConfig.posY * displayHeight);
        }
        coordDisplay.setText(px + "X" + py);
    }
}
