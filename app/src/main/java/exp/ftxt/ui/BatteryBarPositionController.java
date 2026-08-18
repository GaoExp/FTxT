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
import exp.ftxt.features.battery_bar.BatteryBarConfig;
import exp.ftxt.features.battery_bar.BatteryBarModule;
import exp.ftxt.shared.preset.OverlayPreset;
import exp.ftxt.shared.preset.PresetHandler;
import exp.ftxt.shared.preset.PresetManager;
import exp.ftxt.shared.ui.DpadController;
import exp.ftxt.shared.ui.SliderPositionController;

public class BatteryBarPositionController {

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
        public String moduleLabel() { return "Battery Strip"; }
        @Override
        public String moduleType() { return "battery_bar"; }
        @Override
        public String touchPassthroughPrefKey() { return "batbar_lock"; }
        @Override
        public String safeAreaPrefKey() { return "batbar_safe_area"; }
        @Override
        public String posXPrefKey() { return "batbar_pos_x"; }
        @Override
        public String posYPrefKey() { return "batbar_pos_y"; }

        @Override
        public void saveToPreset(OverlayPreset p) {
            p.moduleType = moduleType();
            p.posX = BatteryBarConfig.posX;
            p.posY = BatteryBarConfig.posY;
            p.touchPassthrough = BatteryBarConfig.touchPassthrough;
            p.safeArea = BatteryBarConfig.safeArea;
            p.quickMode = BatteryBarConfig.quickMode;
            p.quickSide = BatteryBarConfig.quickSide;
            p.barHorizontal = BatteryBarConfig.horizontal;
            p.barInvert = BatteryBarConfig.invert;
            p.barLength = BatteryBarConfig.length;
            p.barThickness = BatteryBarConfig.thickness;
            p.color = BatteryBarConfig.color;
            p.autoColor = BatteryBarConfig.isAutoColor();
            p.barColorScheme = BatteryBarConfig.colorScheme;
            p.lowColor = BatteryBarConfig.lowColor;
            p.lowThreshold = BatteryBarConfig.lowThreshold;
            p.showEmptyStrip = BatteryBarConfig.showEmptyStrip;
            p.emptyColor = BatteryBarConfig.emptyColor;
            p.barRadius = BatteryBarConfig.radius;
            p.fadeSpeed = BatteryBarConfig.fadeSpeed;
            p.fadeEnabled = BatteryBarConfig.fadeEnabled;
            p.shineEnabled = BatteryBarConfig.shineEnabled;
            p.shineSpeed = BatteryBarConfig.shineSpeed;
            p.shineWidth = BatteryBarConfig.shineWidth;
            p.waveEnabled = BatteryBarConfig.waveEnabled;
            p.waveSpeed = BatteryBarConfig.waveSpeed;
            p.waveAmplitude = BatteryBarConfig.waveAmplitude;
            p.chargeWaveEnabled = BatteryBarConfig.chargeWaveEnabled;
            p.chargeWaveSpeed = BatteryBarConfig.chargeWaveSpeed;
            p.chargeWaveAmplitude = BatteryBarConfig.chargeWaveAmplitude;
        }

        @Override
        public void applyFromPreset(Activity activity, OverlayPreset p, SharedPreferences prefs) {
            BatteryBarConfig.posX = p.posX;
            BatteryBarConfig.posY = p.posY;
            if (p.touchPassthrough != null) {
                BatteryBarConfig.touchPassthrough = p.touchPassthrough;
                prefs.edit().putBoolean("batbar_lock", BatteryBarConfig.touchPassthrough).apply();
            }
            if (p.safeArea != null) {
                BatteryBarConfig.safeArea = p.safeArea;
                prefs.edit().putBoolean("batbar_safe_area", BatteryBarConfig.safeArea).apply();
            }
            if (p.quickMode != null) {
                BatteryBarConfig.quickMode = p.quickMode;
                prefs.edit().putBoolean("batbar_quick_mode", BatteryBarConfig.quickMode).apply();
            }
            BatteryBarConfig.safeArea = true;
            prefs.edit().putBoolean("batbar_safe_area", true).apply();
            if (p.quickSide != null) {
                BatteryBarConfig.quickSide = p.quickSide;
                prefs.edit().putString("batbar_quick_side", BatteryBarConfig.quickSide).apply();
            }
            if (p.barHorizontal != null) {
                BatteryBarConfig.horizontal = p.barHorizontal;
                prefs.edit().putBoolean("batbar_horizontal", BatteryBarConfig.horizontal).apply();
            }
            if (p.barInvert != null) {
                BatteryBarConfig.invert = p.barInvert;
                prefs.edit().putBoolean("batbar_invert", BatteryBarConfig.invert).apply();
            }
            if (p.barLength != null) {
                BatteryBarConfig.length = p.barLength;
                prefs.edit().putFloat("batbar_length", BatteryBarConfig.length).apply();
            }
            if (p.barThickness != null) {
                BatteryBarConfig.thickness = p.barThickness;
                prefs.edit().putInt("batbar_thickness", BatteryBarConfig.thickness).apply();
            }
            BatteryBarConfig.color = p.color;
            prefs.edit().putInt("batbar_color", BatteryBarConfig.color).apply();
            if (p.barColorScheme != null) {
                BatteryBarConfig.colorScheme = p.barColorScheme;
                prefs.edit().putInt("batbar_color_scheme", BatteryBarConfig.colorScheme).apply();
            } else if (p.autoColor != null) {
                BatteryBarConfig.colorScheme = p.autoColor ? BatteryBarConfig.SCHEME_CLASSIC : BatteryBarConfig.SCHEME_NONE;
                prefs.edit().putInt("batbar_color_scheme", BatteryBarConfig.colorScheme).apply();
            }
            if (p.lowColor != null) {
                BatteryBarConfig.lowColor = p.lowColor;
                prefs.edit().putInt("batbar_low_color", BatteryBarConfig.lowColor).apply();
            }
            if (p.lowThreshold != null) {
                BatteryBarConfig.lowThreshold = p.lowThreshold;
                prefs.edit().putInt("batbar_low_threshold", BatteryBarConfig.lowThreshold).apply();
            }
            if (p.showEmptyStrip != null) {
                BatteryBarConfig.showEmptyStrip = p.showEmptyStrip;
                prefs.edit().putBoolean("batbar_show_empty_strip", BatteryBarConfig.showEmptyStrip).apply();
            }
            if (p.emptyColor != null) {
                BatteryBarConfig.emptyColor = p.emptyColor;
                prefs.edit().putInt("batbar_empty_color", BatteryBarConfig.emptyColor).apply();
            }
            if (p.barRadius != null) {
                BatteryBarConfig.radius = p.barRadius;
                prefs.edit().putInt("batbar_radius", BatteryBarConfig.radius).apply();
            }
            if (p.fadeSpeed != null) {
                BatteryBarConfig.fadeSpeed = p.fadeSpeed;
                prefs.edit().putInt("batbar_fade_speed", BatteryBarConfig.fadeSpeed).apply();
            }
            if (p.fadeEnabled != null) {
                BatteryBarConfig.fadeEnabled = p.fadeEnabled;
                prefs.edit().putBoolean("batbar_fade_enabled", BatteryBarConfig.fadeEnabled).apply();
            }
            if (p.shineEnabled != null) {
                BatteryBarConfig.shineEnabled = p.shineEnabled;
                prefs.edit().putBoolean("batbar_shine_enabled", BatteryBarConfig.shineEnabled).apply();
            }
            if (p.shineSpeed != null) {
                BatteryBarConfig.shineSpeed = p.shineSpeed;
                prefs.edit().putInt("batbar_shine_speed", BatteryBarConfig.shineSpeed).apply();
            }
            if (p.shineWidth != null) {
                BatteryBarConfig.shineWidth = p.shineWidth;
                prefs.edit().putInt("batbar_shine_width", BatteryBarConfig.shineWidth).apply();
            }
            if (p.waveEnabled != null) {
                BatteryBarConfig.waveEnabled = p.waveEnabled;
                prefs.edit().putBoolean("batbar_wave_enabled", BatteryBarConfig.waveEnabled).apply();
            }
            if (p.waveSpeed != null) {
                BatteryBarConfig.waveSpeed = p.waveSpeed;
                prefs.edit().putInt("batbar_wave_speed", BatteryBarConfig.waveSpeed).apply();
            }
            if (p.waveAmplitude != null) {
                BatteryBarConfig.waveAmplitude = p.waveAmplitude;
                prefs.edit().putInt("batbar_wave_amplitude", BatteryBarConfig.waveAmplitude).apply();
            }
            if (p.chargeWaveEnabled != null) {
                BatteryBarConfig.chargeWaveEnabled = p.chargeWaveEnabled;
                prefs.edit().putBoolean("batbar_charge_wave_enabled", BatteryBarConfig.chargeWaveEnabled).apply();
            }
            if (p.chargeWaveSpeed != null) {
                BatteryBarConfig.chargeWaveSpeed = p.chargeWaveSpeed;
                prefs.edit().putInt("batbar_charge_wave_speed", BatteryBarConfig.chargeWaveSpeed).apply();
            }
            if (p.chargeWaveAmplitude != null) {
                BatteryBarConfig.chargeWaveAmplitude = p.chargeWaveAmplitude;
                prefs.edit().putInt("batbar_charge_wave_amplitude", BatteryBarConfig.chargeWaveAmplitude).apply();
            }
        }

        @Override
        public void syncToService() {
            FloatingService.updateBatteryBarInPlace();
        }
    };

    public BatteryBarPositionController(Activity activity, View rootView) {
        this.activity = activity;
        this.prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        int orientation = activity.getResources().getConfiguration().orientation;
        currentOrientation = (orientation == Configuration.ORIENTATION_LANDSCAPE) ? "land" : "port";
        loadPositionFromPrefs(currentOrientation);

        FloatingService.setOrientationSuffixForModule(FloatingService.batteryBarModule(), currentOrientation);

        bindViews(rootView);

        WindowManager wm = activity.getWindowManager();
        DisplayMetrics realMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(realMetrics);
        displayWidth = realMetrics.widthPixels;
        displayHeight = realMetrics.heightPixels;

        BatteryBarModule.onPositionUpdate = this::syncAll;

        sliderController = new SliderPositionController(
                rootView.findViewById(R.id.batbar_posXSeekBar),
                rootView.findViewById(R.id.batbar_posYSeekBar),
                rootView.findViewById(R.id.batbar_posXLabel),
                rootView.findViewById(R.id.batbar_posYLabel),
                (x, y) -> onPositionChanged(x, y)
        );
        setupListeners();
        syncAll();
        FloatingService.updatePositionForModule(FloatingService.batteryBarModule());
    }

    private void bindViews(View rootView) {
        btnUp = rootView.findViewById(R.id.batbar_btnUp);
        btnDown = rootView.findViewById(R.id.batbar_btnDown);
        btnLeft = rootView.findViewById(R.id.batbar_btnLeft);
        btnRight = rootView.findViewById(R.id.batbar_btnRight);
        coordDisplay = rootView.findViewById(R.id.batbar_posCoordDisplay);
        activePresetLabel = rootView.findViewById(R.id.active_preset_label_batbar);
    }

    private void setupListeners() {
        dpad = new DpadController(btnUp, btnDown, btnLeft, btnRight, (dx, dy) -> {
            onPositionChanged(clamp(BatteryBarConfig.posX + dx), clamp(BatteryBarConfig.posY + dy));
        });
    }

    public void showLoadPresetDialog() {
        PresetHandler.showLoadPresetDialog(activity, delegate, activePresetName, this::syncAll,
                (onSaved) -> PresetHandler.showSavePresetDialog(activity, delegate, onSaved));
    }

    public void setPositionControlsEnabled(boolean enabled) {
        if (sliderController == null) return;
        float alpha = enabled ? 1f : 0.3f;
        View xSeek = rootViewId(R.id.batbar_posXSeekBar);
        View ySeek = rootViewId(R.id.batbar_posYSeekBar);
        View xLabel = rootViewId(R.id.batbar_posXLabel);
        View yLabel = rootViewId(R.id.batbar_posYLabel);
        setViewEnabled(xSeek, enabled);
        setViewEnabled(ySeek, enabled);
        if (xSeek != null) xSeek.setAlpha(alpha);
        if (ySeek != null) ySeek.setAlpha(alpha);
        if (xLabel != null) xLabel.setAlpha(alpha);
        if (yLabel != null) yLabel.setAlpha(alpha);
        btnUp.setEnabled(enabled);
        btnDown.setEnabled(enabled);
        btnLeft.setEnabled(enabled);
        btnRight.setEnabled(enabled);
        btnUp.setAlpha(alpha);
        btnDown.setAlpha(alpha);
        btnLeft.setAlpha(alpha);
        btnRight.setAlpha(alpha);
    }

    private View rootViewId(int id) {
        if (activity instanceof AppCompatActivity) {
            return ((AppCompatActivity) activity).findViewById(id);
        }
        return null;
    }

    private void setViewEnabled(View v, boolean enabled) {
        if (v != null) v.setEnabled(enabled);
    }

    private static float clamp(float val) {
        return Math.max(0, Math.min(1, val));
    }

    private void onPositionChanged(float x, float y) {
        BatteryBarConfig.posX = x;
        BatteryBarConfig.posY = y;
        syncAll();
        PresetHandler.savePositionToPrefs(prefs, delegate, currentOrientation, x, y);
        FloatingService.updatePositionForModule(FloatingService.batteryBarModule());
    }

    private void loadPositionFromPrefs(String orient) {
        String sfx = "_" + orient;
        BatteryBarConfig.posX = prefs.getFloat("batbar_pos_x" + sfx, 0.05f);
        BatteryBarConfig.posY = prefs.getFloat("batbar_pos_y" + sfx, 0.9f);
    }

    public void cleanup() {
        BatteryBarModule.onPositionUpdate = null;
        FloatingService.setOrientationSuffixForModule(FloatingService.batteryBarModule(), null);
        if (dpad != null) dpad.cleanup();
    }

    public void refresh() {
        syncAll();
    }

    public void syncAll() {
        sliderController.sync(BatteryBarConfig.posX, BatteryBarConfig.posY);
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
        int[] pos = FloatingService.getCurrentPositionForModule(FloatingService.batteryBarModule());
        if (pos != null) {
            px = pos[0];
            py = pos[1];
        } else {
            px = Math.round(BatteryBarConfig.posX * displayWidth);
            py = Math.round(BatteryBarConfig.posY * displayHeight);
        }
        coordDisplay.setText(px + "X" + py);
    }
}
