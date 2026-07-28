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
import exp.ftxt.features.battery_temperature.BatteryConfig;
import exp.ftxt.features.battery_temperature.BatteryModule;
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
        public String moduleLabel() { return "Baterai"; }
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
            p.posX = BatteryConfig.posX;
            p.posY = BatteryConfig.posY;
            p.size = BatteryConfig.size;
            p.color = BatteryConfig.color;
            p.shadow = PresetHandler.copyShadow(BatteryConfig.shadow);
            p.bgEnabled = BatteryConfig.bg.enabled;
            p.bgColor = BatteryConfig.bg.color;
            p.bgPadding = BatteryConfig.bg.padding;
            p.bgOffsetX = BatteryConfig.bg.offsetX;
            p.bgOffsetY = BatteryConfig.bg.offsetY;
            p.bgMargin = BatteryConfig.bg.margin;
            p.bgRadius = BatteryConfig.bg.radius;
            p.touchPassthrough = BatteryConfig.touchPassthrough;
            p.safeArea = BatteryConfig.safeArea;
            p.showOnlyValue = BatteryConfig.showOnlyValue;
            p.labelColor = BatteryConfig.labelColor;
            p.showTemperature = BatteryConfig.showTemperature;
            p.showPercentage = BatteryConfig.showPercentage;
        }

        @Override
        public void applyFromPreset(Activity activity, OverlayPreset p, SharedPreferences prefs) {
            BatteryConfig.posX = p.posX;
            BatteryConfig.posY = p.posY;
            BatteryConfig.size = p.size;
            BatteryConfig.color = p.color;
            if (p.shadow != null) {
                BatteryConfig.shadow.enabled = p.shadow.enabled;
                BatteryConfig.shadow.color = p.shadow.color;
                BatteryConfig.shadow.blur = p.shadow.blur;
                BatteryConfig.shadow.offsetX = p.shadow.offsetX;
                BatteryConfig.shadow.offsetY = p.shadow.offsetY;
            }
            BatteryConfig.bg.enabled = p.bgEnabled;
            BatteryConfig.bg.color = p.bgColor;
            BatteryConfig.bg.padding = p.bgPadding;
            BatteryConfig.bg.offsetX = p.bgOffsetX;
            BatteryConfig.bg.offsetY = p.bgOffsetY;
            BatteryConfig.bg.margin = p.bgMargin;
            BatteryConfig.bg.radius = p.bgRadius;
            if (p.touchPassthrough != null) {
                BatteryConfig.touchPassthrough = p.touchPassthrough;
                prefs.edit().putBoolean("battery_lock", BatteryConfig.touchPassthrough).apply();
            }
            if (p.safeArea != null) {
                BatteryConfig.safeArea = p.safeArea;
                prefs.edit().putBoolean("battery_safe_area", BatteryConfig.safeArea).apply();
            }
            if (p.showOnlyValue != null) {
                BatteryConfig.showOnlyValue = p.showOnlyValue;
                prefs.edit().putBoolean("battery_show_only_value", BatteryConfig.showOnlyValue).apply();
            }
            if (p.labelColor != null) {
                BatteryConfig.labelColor = p.labelColor;
                prefs.edit().putInt("battery_label_color", BatteryConfig.labelColor).apply();
            }
            if (p.showTemperature != null) {
                BatteryConfig.showTemperature = p.showTemperature;
                prefs.edit().putBoolean("battery_show_temperature", BatteryConfig.showTemperature).apply();
            }
            if (p.showPercentage != null) {
                BatteryConfig.showPercentage = p.showPercentage;
                prefs.edit().putBoolean("battery_show_percentage", BatteryConfig.showPercentage).apply();
            }
        }

        @Override
        public void syncToService() {
            FloatingService.updatePositionForModule(FloatingService.batteryModule());
            FloatingService.updateSizeForModule(FloatingService.batteryModule(), BatteryConfig.size);
            FloatingService.updateColorForModule(FloatingService.batteryModule(), BatteryConfig.color);
            FloatingService.updateLabelColorForModule(FloatingService.batteryModule(), BatteryConfig.labelColor);
            FloatingService.updateShadowForModule(FloatingService.batteryModule());
            FloatingService.updateBackgroundForModule(FloatingService.batteryModule());
        }
    };

    public BatteryPositionController(Activity activity) {
        this.activity = activity;
        this.prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        int orientation = activity.getResources().getConfiguration().orientation;
        currentOrientation = (orientation == Configuration.ORIENTATION_LANDSCAPE) ? "land" : "port";
        loadPositionFromPrefs(currentOrientation);

        FloatingService.setOrientationSuffixForModule(FloatingService.batteryModule(), currentOrientation);

        bindViews();

        WindowManager wm = activity.getWindowManager();
        DisplayMetrics realMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(realMetrics);
        displayWidth = realMetrics.widthPixels;
        displayHeight = realMetrics.heightPixels;

        BatteryModule.onPositionUpdate = this::syncAll;

        sliderController = new SliderPositionController(
                activity.findViewById(R.id.battery_posXSeekBar),
                activity.findViewById(R.id.battery_posYSeekBar),
                activity.findViewById(R.id.battery_posXLabel),
                activity.findViewById(R.id.battery_posYLabel),
                (x, y) -> onPositionChanged(x, y)
        );
        setupListeners();
        syncAll();
        FloatingService.updatePositionForModule(FloatingService.batteryModule());
    }

    private void bindViews() {
        btnUp = activity.findViewById(R.id.battery_btnUp);
        btnDown = activity.findViewById(R.id.battery_btnDown);
        btnLeft = activity.findViewById(R.id.battery_btnLeft);
        btnRight = activity.findViewById(R.id.battery_btnRight);
        coordDisplay = activity.findViewById(R.id.battery_posCoordDisplay);
    }

    private void setupListeners() {
        dpad = new DpadController(btnUp, btnDown, btnLeft, btnRight, (dx, dy) -> {
            onPositionChanged(clamp(BatteryConfig.posX + dx), clamp(BatteryConfig.posY + dy));
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
        BatteryConfig.posX = x;
        BatteryConfig.posY = y;
        syncAll();
        PresetHandler.savePositionToPrefs(prefs, delegate, currentOrientation, x, y);
        FloatingService.updatePositionForModule(FloatingService.batteryModule());
    }

    private void loadPositionFromPrefs(String orient) {
        String sfx = "_" + orient;
        BatteryConfig.posX = prefs.getFloat("battery_pos_x" + sfx, 0.05f);
        BatteryConfig.posY = prefs.getFloat("battery_pos_y" + sfx, 0.8f);
    }

    public void cleanup() {
        BatteryModule.onPositionUpdate = null;
        FloatingService.setOrientationSuffixForModule(FloatingService.batteryModule(), null);
        if (dpad != null) dpad.cleanup();
    }

    public void refresh() {
        syncAll();
    }

    public void syncAll() {
        sliderController.sync(BatteryConfig.posX, BatteryConfig.posY);
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
        int[] pos = FloatingService.getCurrentPositionForModule(FloatingService.batteryModule());
        if (pos != null) {
            px = pos[0];
            py = pos[1];
        } else {
            px = Math.round(BatteryConfig.posX * displayWidth);
            py = Math.round(BatteryConfig.posY * displayHeight);
        }
        coordDisplay.setText(px + "X" + py);
    }
}
