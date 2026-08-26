package exp.ftxt.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import exp.ftxt.R;
import exp.ftxt.core.FloatingService;
import exp.ftxt.features.crosshair.CrosshairConfig;
import exp.ftxt.features.crosshair.CrosshairModule;
import exp.ftxt.shared.preset.OverlayPreset;
import exp.ftxt.shared.preset.PresetHandler;
import exp.ftxt.shared.ui.DpadController;
import exp.ftxt.shared.ui.SliderPositionController;

public class CrosshairPositionController {

    private final Activity activity;
    private final SharedPreferences prefs;

    private View btnUp, btnDown, btnLeft, btnRight, btnMinus, btnPlus, dpadCenter;
    private ImageButton btnReset;
    private String currentOrientation;

    private DpadController dpad;
    private SliderPositionController sliderController;
    private TextView coordDisplay;
    private TextView activePresetLabel;
    private int displayWidth, displayHeight;

    private final PresetHandler.StringHolder activePresetName = new PresetHandler.StringHolder();

    private final PresetHandler.Delegate delegate = new PresetHandler.Delegate() {

        @Override
        public String moduleLabel() { return "Crosshair"; }

        @Override
        public String moduleType() { return "crosshair"; }

        @Override
        public String touchPassthroughPrefKey() { return "crosshair_lock"; }

        @Override
        public String safeAreaPrefKey() { return "crosshair_safe_area"; }

        @Override
        public String posXPrefKey() { return "crosshair_pos_x"; }

        @Override
        public String posYPrefKey() { return "crosshair_pos_y"; }

        @Override
        public void saveToPreset(OverlayPreset p) {
            p.posX = CrosshairConfig.posX;
            p.posY = CrosshairConfig.posY;
            p.size = CrosshairConfig.size;
            p.color = CrosshairConfig.color;
            p.bgEnabled = CrosshairConfig.bg.enabled;
            p.bgColor = CrosshairConfig.bg.color;
            p.bgPadding = CrosshairConfig.bg.padding;
            p.bgOffsetX = CrosshairConfig.bg.offsetX;
            p.bgOffsetY = CrosshairConfig.bg.offsetY;
            p.bgMargin = CrosshairConfig.bg.margin;
            p.bgRadius = CrosshairConfig.bg.radius;
            p.touchPassthrough = CrosshairConfig.touchPassthrough;
            p.safeArea = CrosshairConfig.safeArea;
            p.crosshairStyleIndex = CrosshairConfig.styleIndex;
            p.crosshairOpacity = CrosshairConfig.opacity;
            p.crosshairColorEnabled = CrosshairConfig.colorEnabled;
            p.crosshairColor = CrosshairConfig.color;
        }

        @Override
        public void applyFromPreset(Activity activity, OverlayPreset p, SharedPreferences prefs) {
            CrosshairConfig.posX = p.posX;
            CrosshairConfig.posY = p.posY;
            CrosshairConfig.size = p.size;
            CrosshairConfig.color = p.color;
            CrosshairConfig.bg.enabled = p.bgEnabled;
            CrosshairConfig.bg.color = p.bgColor;
            CrosshairConfig.bg.padding = p.bgPadding;
            CrosshairConfig.bg.offsetX = p.bgOffsetX;
            CrosshairConfig.bg.offsetY = p.bgOffsetY;
            CrosshairConfig.bg.margin = p.bgMargin;
            CrosshairConfig.bg.radius = p.bgRadius;
            saveBgPrefs(prefs);
            if (p.touchPassthrough != null) {
                CrosshairConfig.touchPassthrough = p.touchPassthrough;
                prefs.edit().putBoolean("crosshair_lock", CrosshairConfig.touchPassthrough).apply();
            }
            if (p.safeArea != null) {
                CrosshairConfig.safeArea = p.safeArea;
                prefs.edit().putBoolean("crosshair_safe_area", CrosshairConfig.safeArea).apply();
            }
            if (p.crosshairStyleIndex != null) {
                CrosshairConfig.styleIndex = Math.max(1, Math.min(44, p.crosshairStyleIndex));
                prefs.edit().putInt("crosshair_style", CrosshairConfig.styleIndex).apply();
            }
            if (p.crosshairOpacity != null) {
                CrosshairConfig.opacity = Math.max(10, Math.min(100, p.crosshairOpacity));
                prefs.edit().putInt("crosshair_opacity", CrosshairConfig.opacity).apply();
            }
            if (p.crosshairColorEnabled != null) {
                CrosshairConfig.colorEnabled = p.crosshairColorEnabled;
                prefs.edit().putBoolean("crosshair_color_enabled", CrosshairConfig.colorEnabled).apply();
            }
            if (p.crosshairColor != null) {
                CrosshairConfig.color = p.crosshairColor;
                prefs.edit().putInt("crosshair_color", CrosshairConfig.color).apply();
            }
        }

        @Override
        public void syncToService() {
            FloatingService.updatePositionForModule(FloatingService.crosshairModule());
            FloatingService.updateSizeForModule(FloatingService.crosshairModule(), CrosshairConfig.size);
            FloatingService.updateTouchFlagsForModule(FloatingService.crosshairModule());
            FloatingService.updateColorForModule(FloatingService.crosshairModule(), CrosshairConfig.color);
            FloatingService.updateBackgroundForModule(FloatingService.crosshairModule());
            FloatingService.crosshairModule().applyStyle();
            FloatingService.crosshairModule().applyOpacity();
        }

        private void saveBgPrefs(SharedPreferences prefs) {
            prefs.edit()
                    .putBoolean("crosshair_bg_enabled", CrosshairConfig.bg.enabled)
                    .putInt("crosshair_bg_color", CrosshairConfig.bg.color)
                    .putInt("crosshair_bg_padding", CrosshairConfig.bg.padding)
                    .putInt("crosshair_bg_offset_x", CrosshairConfig.bg.offsetX)
                    .putInt("crosshair_bg_offset_y", CrosshairConfig.bg.offsetY)
                    .putInt("crosshair_bg_margin", CrosshairConfig.bg.margin)
                    .putInt("crosshair_bg_radius", CrosshairConfig.bg.radius)
                    .apply();
        }
    };

    private static final String PREFS_NAME = "ftxt_prefs";

    public CrosshairPositionController(Activity activity, View rootView) {
        this.activity = activity;
        this.prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        int orientation = activity.getResources().getConfiguration().orientation;
        currentOrientation = (orientation == Configuration.ORIENTATION_LANDSCAPE) ? "land" : "port";
        loadPositionFromPrefs(currentOrientation);

        FloatingService.setOrientationSuffixForModule(FloatingService.crosshairModule(), currentOrientation);

        bindViews(rootView);

        WindowManager wm = activity.getWindowManager();
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        displayWidth = dm.widthPixels;
        displayHeight = dm.heightPixels;

        CrosshairModule.onPositionUpdate = this::syncAll;

        sliderController = new SliderPositionController(
                rootView.findViewById(R.id.crosshair_posXSeekBar),
                rootView.findViewById(R.id.crosshair_posYSeekBar),
                rootView.findViewById(R.id.crosshair_posXLabel),
                rootView.findViewById(R.id.crosshair_posYLabel),
                (x, y) -> onPositionChanged(x, y)
        );
        setupListeners();
        syncAll();
        FloatingService.updatePositionForModule(FloatingService.crosshairModule());
    }

private void bindViews(View rootView) {
    btnUp = rootView.findViewById(R.id.crosshair_btnUp);
    btnDown = rootView.findViewById(R.id.crosshair_btnDown);
    btnLeft = rootView.findViewById(R.id.crosshair_btnLeft);
    btnRight = rootView.findViewById(R.id.crosshair_btnRight);
    btnMinus = rootView.findViewById(R.id.crosshair_btnMinus);
    btnPlus = rootView.findViewById(R.id.crosshair_btnPlus);
    btnReset = rootView.findViewById(R.id.crosshair_btnReset);
    coordDisplay = rootView.findViewById(R.id.crosshair_posCoordDisplay);
    activePresetLabel = rootView.findViewById(R.id.active_preset_label);
    dpadCenter = rootView.findViewById(R.id.crosshair_dpadInterval);
}

private void setupListeners() {
    dpad = new DpadController(btnUp, btnDown, btnLeft, btnRight, btnMinus, btnPlus, dpadCenter, displayWidth, displayHeight, (dx, dy) -> {
        onPositionChanged(clamp(CrosshairConfig.posX + dx), clamp(CrosshairConfig.posY + dy));
    });
    if (btnReset != null) {
        btnReset.setOnClickListener(v -> onPositionChanged(0.5f, 0.5f));
    }
}

    public void showLoadPresetDialog() {
        PresetHandler.showLoadPresetDialog(activity, delegate, activePresetName, this::syncAll,
                (onSaved) -> PresetHandler.showSavePresetDialog(activity, delegate, onSaved));
    }

    private static float clamp(float val) {
        return Math.max(0, Math.min(1, val));
    }

    private void onPositionChanged(float x, float y) {
        CrosshairConfig.posX = x;
        CrosshairConfig.posY = y;
        syncAll();
        PresetHandler.savePositionToPrefs(prefs, delegate, currentOrientation, x, y);
        FloatingService.updatePositionForModule(FloatingService.crosshairModule());
    }

    private void loadPositionFromPrefs(String orient) {
        String sfx = "_" + orient;
        CrosshairConfig.posX = prefs.getFloat("crosshair_pos_x" + sfx, 0.5f);
        CrosshairConfig.posY = prefs.getFloat("crosshair_pos_y" + sfx, 0.5f);
    }

    public void cleanup() {
        CrosshairModule.onPositionUpdate = null;
        FloatingService.setOrientationSuffixForModule(FloatingService.crosshairModule(), null);
        if (dpad != null) dpad.cleanup();
    }

    public void refresh() {
        syncAll();
    }

    public void syncAll() {
        sliderController.sync(CrosshairConfig.posX, CrosshairConfig.posY);
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
        int[] pos = FloatingService.getCurrentPositionForModule(FloatingService.crosshairModule());
        if (pos != null) {
            px = pos[0];
            py = pos[1];
        } else {
            px = Math.round(CrosshairConfig.posX * displayWidth);
            py = Math.round(CrosshairConfig.posY * displayHeight);
        }
        coordDisplay.setText(px + "X" + py);
    }
}
