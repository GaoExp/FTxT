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
import exp.ftxt.features.battery_percentage.BatteryPercentageConfig;
import exp.ftxt.features.battery_percentage.BatteryPercentageModule;
import exp.ftxt.shared.preset.OverlayPreset;
import exp.ftxt.shared.preset.PresetHandler;
import exp.ftxt.shared.preset.PresetManager;
import exp.ftxt.shared.ui.DpadController;
import exp.ftxt.shared.ui.SliderPositionController;

public class BatteryPercentagePositionController {

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
        public String moduleLabel() { return "Battery %"; }
        @Override
        public String touchPassthroughPrefKey() { return "battpct_lock"; }
        @Override
        public String safeAreaPrefKey() { return null; }
        @Override
        public String posXPrefKey() { return "battpct_pos_x"; }
        @Override
        public String posYPrefKey() { return "battpct_pos_y"; }

        @Override
        public void saveToPreset(OverlayPreset p) {
            p.posX = BatteryPercentageConfig.posX;
            p.posY = BatteryPercentageConfig.posY;
            p.size = BatteryPercentageConfig.size;
            p.color = BatteryPercentageConfig.color;
            p.shadow = PresetHandler.copyShadow(BatteryPercentageConfig.shadow);
            p.bgEnabled = BatteryPercentageConfig.bg.enabled;
            p.bgColor = BatteryPercentageConfig.bg.color;
            p.bgPadding = BatteryPercentageConfig.bg.padding;
            p.bgOffsetX = BatteryPercentageConfig.bg.offsetX;
            p.bgOffsetY = BatteryPercentageConfig.bg.offsetY;
            p.bgMargin = BatteryPercentageConfig.bg.margin;
            p.bgRadius = BatteryPercentageConfig.bg.radius;
            p.labelColor = BatteryPercentageConfig.labelColor;
            p.touchPassthrough = BatteryPercentageConfig.touchPassthrough;
        }

        @Override
        public void applyFromPreset(Activity activity, OverlayPreset p, SharedPreferences prefs) {
            BatteryPercentageConfig.posX = p.posX;
            BatteryPercentageConfig.posY = p.posY;
            BatteryPercentageConfig.size = p.size;
            BatteryPercentageConfig.color = p.color;
            if (p.shadow != null) {
                BatteryPercentageConfig.shadow.enabled = p.shadow.enabled;
                BatteryPercentageConfig.shadow.color = p.shadow.color;
                BatteryPercentageConfig.shadow.blur = p.shadow.blur;
                BatteryPercentageConfig.shadow.offsetX = p.shadow.offsetX;
                BatteryPercentageConfig.shadow.offsetY = p.shadow.offsetY;
            }
            BatteryPercentageConfig.bg.enabled = p.bgEnabled;
            BatteryPercentageConfig.bg.color = p.bgColor;
            BatteryPercentageConfig.bg.padding = p.bgPadding;
            BatteryPercentageConfig.bg.offsetX = p.bgOffsetX;
            BatteryPercentageConfig.bg.offsetY = p.bgOffsetY;
            BatteryPercentageConfig.bg.margin = p.bgMargin;
            BatteryPercentageConfig.bg.radius = p.bgRadius;
            if (p.labelColor != null) {
                BatteryPercentageConfig.labelColor = p.labelColor;
                prefs.edit().putInt("battpct_label_color", BatteryPercentageConfig.labelColor).apply();
            }
            if (p.touchPassthrough != null) {
                BatteryPercentageConfig.touchPassthrough = p.touchPassthrough;
                prefs.edit().putBoolean("battpct_lock", BatteryPercentageConfig.touchPassthrough).apply();
            }
        }

        @Override
        public void syncToService() {
            FloatingService.updatePositionForModule(FloatingService.batteryPercentageModule());
            FloatingService.updateSizeForModule(FloatingService.batteryPercentageModule(), BatteryPercentageConfig.size);
            FloatingService.updateColorForModule(FloatingService.batteryPercentageModule(), BatteryPercentageConfig.color);
            FloatingService.updateLabelColorForModule(FloatingService.batteryPercentageModule(), BatteryPercentageConfig.labelColor);
            FloatingService.updateShadowForModule(FloatingService.batteryPercentageModule());
            FloatingService.updateBackgroundForModule(FloatingService.batteryPercentageModule());
        }
    };

    public BatteryPercentagePositionController(Activity activity, View rootView) {
        this.activity = activity;
        this.prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        int orientation = activity.getResources().getConfiguration().orientation;
        currentOrientation = (orientation == Configuration.ORIENTATION_LANDSCAPE) ? "land" : "port";
        loadPositionFromPrefs(currentOrientation);

        FloatingService.setOrientationSuffixForModule(FloatingService.batteryPercentageModule(), currentOrientation);

        bindViews(rootView);

        WindowManager wm = activity.getWindowManager();
        DisplayMetrics realMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(realMetrics);
        displayWidth = realMetrics.widthPixels;
        displayHeight = realMetrics.heightPixels;

        BatteryPercentageModule.onPositionUpdate = this::syncAll;

        sliderController = new SliderPositionController(
                rootView.findViewById(R.id.batPctPosXSeekBar),
                rootView.findViewById(R.id.batPctPosYSeekBar),
                rootView.findViewById(R.id.batPctPosXLabel),
                rootView.findViewById(R.id.batPctPosYLabel),
                (x, y) -> onPositionChanged(x, y)
        );
        setupListeners();
        syncAll();
        FloatingService.updatePositionForModule(FloatingService.batteryPercentageModule());
    }

    private void bindViews(View rootView) {
        btnUp = rootView.findViewById(R.id.batPctBtnUp);
        btnDown = rootView.findViewById(R.id.batPctBtnDown);
        btnLeft = rootView.findViewById(R.id.batPctBtnLeft);
        btnRight = rootView.findViewById(R.id.batPctBtnRight);
        coordDisplay = rootView.findViewById(R.id.batPctPosCoordDisplay);
        activePresetLabel = rootView.findViewById(R.id.active_preset_label);
    }

    private void setupListeners() {
        dpad = new DpadController(btnUp, btnDown, btnLeft, btnRight, (dx, dy) -> {
            onPositionChanged(clamp(BatteryPercentageConfig.posX + dx), clamp(BatteryPercentageConfig.posY + dy));
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
        BatteryPercentageConfig.posX = x;
        BatteryPercentageConfig.posY = y;
        syncAll();
        PresetHandler.savePositionToPrefs(prefs, delegate, currentOrientation, x, y);
        FloatingService.updatePositionForModule(FloatingService.batteryPercentageModule());
    }

    private void loadPositionFromPrefs(String orient) {
        String sfx = "_" + orient;
        BatteryPercentageConfig.posX = prefs.getFloat("battpct_pos_x" + sfx, 0.5f);
        BatteryPercentageConfig.posY = prefs.getFloat("battpct_pos_y" + sfx, 0.5f);
    }

    public void cleanup() {
        BatteryPercentageModule.onPositionUpdate = null;
        FloatingService.setOrientationSuffixForModule(FloatingService.batteryPercentageModule(), null);
        if (dpad != null) dpad.cleanup();
    }

    public void refresh() {
        syncAll();
    }

    public void syncAll() {
        sliderController.sync(BatteryPercentageConfig.posX, BatteryPercentageConfig.posY);
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
        int[] pos = FloatingService.getCurrentPositionForModule(FloatingService.batteryPercentageModule());
        if (pos != null) {
            px = pos[0];
            py = pos[1];
        } else {
            px = Math.round(BatteryPercentageConfig.posX * displayWidth);
            py = Math.round(BatteryPercentageConfig.posY * displayHeight);
        }
        coordDisplay.setText(px + "X" + py);
    }
}
