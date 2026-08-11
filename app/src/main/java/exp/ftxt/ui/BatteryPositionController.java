package exp.ftxt.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import exp.ftxt.R;
import exp.ftxt.core.FloatingService;
import exp.ftxt.features.battery_stats.BatteryStatsConfig;
import exp.ftxt.features.battery_stats.BatteryStatsModule;
import exp.ftxt.shared.preset.OverlayPreset;
import exp.ftxt.shared.preset.PresetHandler;
import exp.ftxt.shared.preset.PresetManager;
import exp.ftxt.shared.ui.DpadController;
import exp.ftxt.shared.ui.SliderPositionController;

public class BatteryPositionController {

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
        public String moduleLabel() { return "Battery Stats"; }
        @Override
        public String moduleType() { return "battery"; }
        @Override
        public String touchPassthroughPrefKey() { return "battery_lock"; }
        @Override
        public String safeAreaPrefKey() { return "battery_safe_area"; }
        @Override
        public String posXPrefKey() { return "battery_pos_x"; }
        @Override
        public String posYPrefKey() { return "battery_pos_y"; }

        @Override
        public void saveToPreset(OverlayPreset p) {
            p.moduleType = moduleType();
            p.posX = BatteryStatsConfig.posX;
            p.posY = BatteryStatsConfig.posY;
            p.size = BatteryStatsConfig.size;
            p.color = BatteryStatsConfig.color;
            p.shadow = PresetHandler.copyShadow(BatteryStatsConfig.shadow);
            p.bgEnabled = BatteryStatsConfig.bg.enabled;
            p.bgColor = BatteryStatsConfig.bg.color;
            p.bgPadding = BatteryStatsConfig.bg.padding;
            p.bgOffsetX = BatteryStatsConfig.bg.offsetX;
            p.bgOffsetY = BatteryStatsConfig.bg.offsetY;
            p.bgMargin = BatteryStatsConfig.bg.margin;
            p.bgRadius = BatteryStatsConfig.bg.radius;
            p.touchPassthrough = BatteryStatsConfig.touchPassthrough;
            p.safeArea = BatteryStatsConfig.safeArea;
            p.showOnlyValue = BatteryStatsConfig.showOnlyValue;
            p.labelColor = BatteryStatsConfig.labelColor;
            p.separatorColor = BatteryStatsConfig.separatorColor;
            p.showTemperature = BatteryStatsConfig.showTemperature;
            p.showPercentage = BatteryStatsConfig.showPercentage;
        }

        @Override
        public void applyFromPreset(Activity activity, OverlayPreset p, SharedPreferences prefs) {
            BatteryStatsConfig.posX = p.posX;
            BatteryStatsConfig.posY = p.posY;
            BatteryStatsConfig.size = p.size;
            BatteryStatsConfig.color = p.color;
            if (p.shadow != null) {
                BatteryStatsConfig.shadow.enabled = p.shadow.enabled;
                BatteryStatsConfig.shadow.color = p.shadow.color;
                BatteryStatsConfig.shadow.blur = p.shadow.blur;
                BatteryStatsConfig.shadow.offsetX = p.shadow.offsetX;
                BatteryStatsConfig.shadow.offsetY = p.shadow.offsetY;
            }
            BatteryStatsConfig.bg.enabled = p.bgEnabled;
            BatteryStatsConfig.bg.color = p.bgColor;
            BatteryStatsConfig.bg.padding = p.bgPadding;
            BatteryStatsConfig.bg.offsetX = p.bgOffsetX;
            BatteryStatsConfig.bg.offsetY = p.bgOffsetY;
            BatteryStatsConfig.bg.margin = p.bgMargin;
            BatteryStatsConfig.bg.radius = p.bgRadius;
            if (p.touchPassthrough != null) {
                BatteryStatsConfig.touchPassthrough = p.touchPassthrough;
                prefs.edit().putBoolean("battery_lock", BatteryStatsConfig.touchPassthrough).apply();
            }
            if (p.safeArea != null) {
                BatteryStatsConfig.safeArea = p.safeArea;
                prefs.edit().putBoolean("battery_safe_area", BatteryStatsConfig.safeArea).apply();
            }
            if (p.showOnlyValue != null) {
                BatteryStatsConfig.showOnlyValue = p.showOnlyValue;
                prefs.edit().putBoolean("battery_show_only_value", BatteryStatsConfig.showOnlyValue).apply();
            }
            if (p.labelColor != null) {
                BatteryStatsConfig.labelColor = p.labelColor;
                prefs.edit().putInt("battery_label_color", BatteryStatsConfig.labelColor).apply();
            }
            if (p.separatorColor != null) {
                BatteryStatsConfig.separatorColor = p.separatorColor;
                prefs.edit().putInt("battery_separator_color", BatteryStatsConfig.separatorColor).apply();
            }
            if (p.showTemperature != null) {
                BatteryStatsConfig.showTemperature = p.showTemperature;
                prefs.edit().putBoolean("battery_show_temperature", BatteryStatsConfig.showTemperature).apply();
            }
            if (p.showPercentage != null) {
                BatteryStatsConfig.showPercentage = p.showPercentage;
                prefs.edit().putBoolean("battery_show_percentage", BatteryStatsConfig.showPercentage).apply();
            }
        }

        @Override
        public void syncToService() {
            FloatingService.updatePositionForModule(FloatingService.batteryStatsModule());
            FloatingService.updateSizeForModule(FloatingService.batteryStatsModule(), BatteryStatsConfig.size);
            FloatingService.updateColorForModule(FloatingService.batteryStatsModule(), BatteryStatsConfig.color);
            FloatingService.updateLabelColorForModule(FloatingService.batteryStatsModule(), BatteryStatsConfig.labelColor);
            FloatingService.updateSeparatorColorForModule(FloatingService.batteryStatsModule(), BatteryStatsConfig.separatorColor);
            FloatingService.updateShadowForModule(FloatingService.batteryStatsModule());
            FloatingService.updateBackgroundForModule(FloatingService.batteryStatsModule());
        }
    };

    public BatteryPositionController(Activity activity, View rootView) {
        this.activity = activity;
        this.prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        int orientation = activity.getResources().getConfiguration().orientation;
        currentOrientation = (orientation == Configuration.ORIENTATION_LANDSCAPE) ? "land" : "port";
        loadPositionFromPrefs(currentOrientation);

        FloatingService.setOrientationSuffixForModule(FloatingService.batteryStatsModule(), currentOrientation);

        bindViews(rootView);

        WindowManager wm = activity.getWindowManager();
        DisplayMetrics realMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(realMetrics);
        displayWidth = realMetrics.widthPixels;
        displayHeight = realMetrics.heightPixels;

        BatteryStatsModule.onPositionUpdate = this::syncAll;

        sliderController = new SliderPositionController(
                rootView.findViewById(R.id.battery_posXSeekBar),
                rootView.findViewById(R.id.battery_posYSeekBar),
                rootView.findViewById(R.id.battery_posXLabel),
                rootView.findViewById(R.id.battery_posYLabel),
                (x, y) -> onPositionChanged(x, y)
        );
        setupListeners();
        syncAll();
        FloatingService.updatePositionForModule(FloatingService.batteryStatsModule());
    }

    private void bindViews(View rootView) {
        btnUp = rootView.findViewById(R.id.battery_btnUp);
        btnDown = rootView.findViewById(R.id.battery_btnDown);
        btnLeft = rootView.findViewById(R.id.battery_btnLeft);
        btnRight = rootView.findViewById(R.id.battery_btnRight);
        coordDisplay = rootView.findViewById(R.id.battery_posCoordDisplay);
        activePresetLabel = rootView.findViewById(R.id.active_preset_label);
    }

    private void setupListeners() {
        dpad = new DpadController(btnUp, btnDown, btnLeft, btnRight, (dx, dy) -> {
            onPositionChanged(clamp(BatteryStatsConfig.posX + dx), clamp(BatteryStatsConfig.posY + dy));
        });
    }

    public void showLoadPresetDialog() {
        PresetHandler.showLoadPresetDialog(activity, delegate, activePresetName, this::syncAll,
                (onSaved) -> PresetHandler.showSavePresetDialog(activity, delegate, onSaved));
    }

    private static float clamp(float val) {
        return Math.max(0, Math.min(1, val));
    }

    private void onPositionChanged(float x, float y) {
        BatteryStatsConfig.posX = x;
        BatteryStatsConfig.posY = y;
        syncAll();
        PresetHandler.savePositionToPrefs(prefs, delegate, currentOrientation, x, y);
        FloatingService.updatePositionForModule(FloatingService.batteryStatsModule());
    }

    private void loadPositionFromPrefs(String orient) {
        String sfx = "_" + orient;
        BatteryStatsConfig.posX = prefs.getFloat("battery_pos_x" + sfx, 0.05f);
        BatteryStatsConfig.posY = prefs.getFloat("battery_pos_y" + sfx, 0.8f);
    }

    public void cleanup() {
        BatteryStatsModule.onPositionUpdate = null;
        FloatingService.setOrientationSuffixForModule(FloatingService.batteryStatsModule(), null);
        if (dpad != null) dpad.cleanup();
    }

    public void refresh() {
        syncAll();
    }

    public void syncAll() {
        sliderController.sync(BatteryStatsConfig.posX, BatteryStatsConfig.posY);
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
        int[] pos = FloatingService.getCurrentPositionForModule(FloatingService.batteryStatsModule());
        if (pos != null) {
            px = pos[0];
            py = pos[1];
        } else {
            px = Math.round(BatteryStatsConfig.posX * displayWidth);
            py = Math.round(BatteryStatsConfig.posY * displayHeight);
        }
        coordDisplay.setText(px + "X" + py);
    }
}
