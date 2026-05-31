package exp.ftxt.ui;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.CheckBox;
import android.widget.TextView;

import exp.ftxt.MainActivity;
import exp.ftxt.R;
import exp.ftxt.core.FloatingService;
import exp.ftxt.features.battery.BatteryConfig;
import exp.ftxt.shared.ui.ColorPickerDialog;
import exp.ftxt.utils.PermissionHelper;

public class BatteryPanelController {

    private final MainActivity activity;

    private CheckBox batterySwitch;
    private SeekBar batterySizeSeekBar;
    private Button batteryColorButton;
    private CheckBox batteryShadowSwitch;
    private LinearLayout batteryShadowConfigContainer;
    private Button batteryShadowColorButton;
    private SeekBar batteryShadowBlurSeekBar;
    private SeekBar batteryShadowOffsetXSeekBar;
    private SeekBar batteryShadowOffsetYSeekBar;
    private CheckBox batteryLockSwitch;
    private CheckBox batteryValueOnlyCheck;
    private CheckBox batteryBgSwitch;
    private LinearLayout batteryBgConfigContainer;
    private Button batteryBgColorButton;
    private SeekBar batteryBgPaddingSeekBar;
    private SeekBar batteryBgOffsetXSeekBar;
    private SeekBar batteryBgOffsetYSeekBar;
    private SeekBar batteryBgMarginSeekBar;
    private SeekBar batteryBgRadiusSeekBar;
    private TextView batterySizeLabel, batteryBgPaddingLabel, batteryBgOffsetXLabel, batteryBgOffsetYLabel;
    private TextView batteryBgMarginLabel, batteryBgRadiusLabel;
    private TextView batteryShadowBlurLabel, batteryShadowOffsetXLabel, batteryShadowOffsetYLabel;
    private BatteryPositionController batteryPositionController;

    public BatteryPanelController(MainActivity activity) {
        this.activity = activity;
        bindViews();
        loadConfig();
        setupListeners();
        batteryPositionController = new BatteryPositionController(activity);
    }

    public void onPanelShown() {
        if (batteryPositionController != null) {
            batteryPositionController.refresh();
        }
    }

    public void showLoadPresetDialog() {
        if (batteryPositionController != null) {
            batteryPositionController.showLoadPresetDialog();
        }
    }

    public void cleanup() {
        if (batteryPositionController != null) {
            batteryPositionController.cleanup();
            batteryPositionController = null;
        }
    }

    private void bindViews() {
        batterySwitch = activity.findViewById(R.id.batterySwitch);
        batterySizeSeekBar = activity.findViewById(R.id.batterySizeSeekBar);
        batteryColorButton = activity.findViewById(R.id.batteryColorButton);
        batteryShadowSwitch = activity.findViewById(R.id.batteryShadowSwitch);
        batteryShadowConfigContainer = activity.findViewById(R.id.shadowConfigBattery);
        batteryShadowColorButton = activity.findViewById(R.id.batteryShadowColorButton);
        batteryShadowBlurSeekBar = activity.findViewById(R.id.batteryShadowBlurSeekBar);
        batteryShadowOffsetXSeekBar = activity.findViewById(R.id.batteryShadowOffsetXSeekBar);
        batteryShadowOffsetYSeekBar = activity.findViewById(R.id.batteryShadowOffsetYSeekBar);
        batteryLockSwitch = activity.findViewById(R.id.batteryLockSwitch);
        batteryValueOnlyCheck = activity.findViewById(R.id.batteryValueOnlyCheck);
        batteryBgSwitch = activity.findViewById(R.id.batteryBgSwitch);
        batteryBgConfigContainer = activity.findViewById(R.id.bgConfigBattery);
        batteryBgColorButton = activity.findViewById(R.id.batteryBgColorButton);
        batteryBgPaddingSeekBar = activity.findViewById(R.id.batteryBgPaddingSeekBar);
        batteryBgOffsetXSeekBar = activity.findViewById(R.id.batteryBgOffsetXSeekBar);
        batteryBgOffsetYSeekBar = activity.findViewById(R.id.batteryBgOffsetYSeekBar);
        batteryBgMarginSeekBar = activity.findViewById(R.id.batteryBgMarginSeekBar);
        batteryBgRadiusSeekBar = activity.findViewById(R.id.batteryBgRadiusSeekBar);
        batterySizeLabel = activity.findViewById(R.id.batterySizeLabel);
        batteryBgPaddingLabel = activity.findViewById(R.id.batteryBgPaddingLabel);
        batteryBgOffsetXLabel = activity.findViewById(R.id.batteryBgOffsetXLabel);
        batteryBgOffsetYLabel = activity.findViewById(R.id.batteryBgOffsetYLabel);
        batteryBgMarginLabel = activity.findViewById(R.id.batteryBgMarginLabel);
        batteryBgRadiusLabel = activity.findViewById(R.id.batteryBgRadiusLabel);
        batteryShadowBlurLabel = activity.findViewById(R.id.batteryShadowBlurLabel);
        batteryShadowOffsetXLabel = activity.findViewById(R.id.batteryShadowOffsetXLabel);
        batteryShadowOffsetYLabel = activity.findViewById(R.id.batteryShadowOffsetYLabel);
    }

    private void loadConfig() {
        batterySwitch.setChecked(BatteryConfig.enabled);
        activity.applyCheckboxTint(batterySwitch, BatteryConfig.enabled);
        batterySizeSeekBar.setProgress((int) BatteryConfig.size);
        batteryBgSwitch.setChecked(BatteryConfig.bgEnabled);
        activity.applyCheckboxTint(batteryBgSwitch, BatteryConfig.bgEnabled);
        batteryBgConfigContainer.setVisibility(BatteryConfig.bgEnabled ? View.VISIBLE : View.GONE);
        batteryBgPaddingSeekBar.setProgress(BatteryConfig.bgPadding);
        batteryBgOffsetXSeekBar.setProgress(BatteryConfig.bgOffsetX + 60);
        batteryBgOffsetYSeekBar.setProgress(BatteryConfig.bgOffsetY + 60);
        batteryBgMarginSeekBar.setProgress(BatteryConfig.bgMargin);
        batteryBgRadiusSeekBar.setProgress(BatteryConfig.bgRadius);
        batteryShadowSwitch.setChecked(BatteryConfig.shadow.enabled);
        activity.applyCheckboxTint(batteryShadowSwitch, BatteryConfig.shadow.enabled);
        batteryShadowConfigContainer.setVisibility(BatteryConfig.shadow.enabled ? View.VISIBLE : View.GONE);
        batteryShadowBlurSeekBar.setProgress((int) BatteryConfig.shadow.blur);
        batteryShadowOffsetXSeekBar.setProgress((int) BatteryConfig.shadow.offsetX + 60);
        batteryShadowOffsetYSeekBar.setProgress((int) BatteryConfig.shadow.offsetY + 60);
        batteryLockSwitch.setChecked(BatteryConfig.touchPassthrough);
        activity.applyCheckboxTint(batteryLockSwitch, BatteryConfig.touchPassthrough);
        batteryValueOnlyCheck.setChecked(BatteryConfig.showOnlyValue);
        batterySizeLabel.setText("Ukuran Teks: " + (int) BatteryConfig.size);
        batteryBgPaddingLabel.setText("Ukuran Background: " + BatteryConfig.bgPadding);
        batteryBgOffsetXLabel.setText("Offset X: " + BatteryConfig.bgOffsetX);
        batteryBgOffsetYLabel.setText("Offset Y: " + BatteryConfig.bgOffsetY);
        batteryBgMarginLabel.setText("Margin: " + BatteryConfig.bgMargin);
        batteryBgRadiusLabel.setText("Radius: " + BatteryConfig.bgRadius);
        batteryShadowBlurLabel.setText("Blur Shadow: " + (int) BatteryConfig.shadow.blur);
        batteryShadowOffsetXLabel.setText("Shadow X: " + (int) BatteryConfig.shadow.offsetX);
        batteryShadowOffsetYLabel.setText("Shadow Y: " + (int) BatteryConfig.shadow.offsetY);
    }

    private void setupListeners() {
        batterySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !PermissionHelper.hasOverlayPermission(activity)) {
                batterySwitch.setChecked(false);
                activity.applyCheckboxTint(batterySwitch, false);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putBoolean("battery_enabled", false).apply();
                return;
            }

            BatteryConfig.enabled = isChecked;
            activity.applyCheckboxTint(batterySwitch, isChecked);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("battery_enabled", isChecked).apply();

            if (isChecked) {
                if (FloatingService.instance != null) {
                    FloatingService.startBatteryStatic();
                } else {
                    activity.startService(new Intent(activity, FloatingService.class));
                }
            } else {
                FloatingService.stopBatteryStatic();
                if (!activity.isTextOverlayOn() && !exp.ftxt.features.fps.FpsConfig.enabled
                        && !exp.ftxt.features.clock.ClockConfig.enabled) {
                    activity.stopService(new Intent(activity, FloatingService.class));
                }
            }
        });

        batterySizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (progress < 5) { progress = 5; sb.setProgress(progress); }
                if (progress > 140) { progress = 140; sb.setProgress(progress); }
                BatteryConfig.size = progress;
                batterySizeLabel.setText("Ukuran Teks: " + progress);
                FloatingService.updateBatterySizeStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        batteryColorButton.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Pilih Warna Baterai", BatteryConfig.color, color -> {
                BatteryConfig.color = color;
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("battery_color", color).apply();
                FloatingService.updateBatteryColorStatic();
            });
        });

        batteryBgSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            BatteryConfig.bgEnabled = isChecked;
            activity.applyCheckboxTint(batteryBgSwitch, isChecked);
            batteryBgConfigContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("battery_bg_enabled", isChecked).apply();
            FloatingService.updateBatteryBackgroundStatic();
        });

        batteryBgColorButton.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Warna Background Baterai", BatteryConfig.bgColor, color -> {
                BatteryConfig.bgColor = color;
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("battery_bg_color", color).apply();
                FloatingService.updateBatteryBackgroundStatic();
            });
        });

        batteryBgPaddingSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (progress < 0) progress = 0;
                BatteryConfig.bgPadding = progress;
                batteryBgPaddingLabel.setText("Ukuran Background: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("battery_bg_padding", progress).apply();
                FloatingService.updateBatteryBackgroundStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        batteryBgOffsetXSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                BatteryConfig.bgOffsetX = offset;
                batteryBgOffsetXLabel.setText("Offset X: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("battery_bg_offset_x", offset).apply();
                FloatingService.updateBatteryBackgroundStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        batteryBgOffsetYSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                BatteryConfig.bgOffsetY = offset;
                batteryBgOffsetYLabel.setText("Offset Y: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("battery_bg_offset_y", offset).apply();
                FloatingService.updateBatteryBackgroundStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        batteryBgMarginSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                BatteryConfig.bgMargin = progress;
                batteryBgMarginLabel.setText("Margin: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("battery_bg_margin", progress).apply();
                FloatingService.updateBatteryBackgroundStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        batteryBgRadiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                BatteryConfig.bgRadius = progress;
                batteryBgRadiusLabel.setText("Radius: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("battery_bg_radius", progress).apply();
                FloatingService.updateBatteryBackgroundStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        batteryShadowSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            BatteryConfig.shadow.enabled = isChecked;
            activity.applyCheckboxTint(batteryShadowSwitch, isChecked);
            batteryShadowConfigContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("battery_shadow_enabled", isChecked).apply();
            saveBatteryShadowPrefs();
            FloatingService.updateBatteryShadowStatic();
        });

        batteryShadowColorButton.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Warna Shadow Baterai", BatteryConfig.shadow.color, color -> {
                BatteryConfig.shadow.color = color;
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("battery_shadow_color", color).apply();
                FloatingService.updateBatteryShadowStatic();
            });
        });

        batteryShadowBlurSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                BatteryConfig.shadow.blur = progress;
                batteryShadowBlurLabel.setText("Blur Shadow: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putFloat("battery_shadow_blur", (float) progress).apply();
                FloatingService.updateBatteryShadowStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        batteryShadowOffsetXSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                BatteryConfig.shadow.offsetX = offset;
                batteryShadowOffsetXLabel.setText("Shadow X: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putFloat("battery_shadow_offset_x", (float) offset).apply();
                FloatingService.updateBatteryShadowStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        batteryShadowOffsetYSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                BatteryConfig.shadow.offsetY = offset;
                batteryShadowOffsetYLabel.setText("Shadow Y: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putFloat("battery_shadow_offset_y", (float) offset).apply();
                FloatingService.updateBatteryShadowStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        batteryLockSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            BatteryConfig.touchPassthrough = isChecked;
            activity.applyCheckboxTint(batteryLockSwitch, isChecked);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("battery_lock", isChecked).apply();
            FloatingService.updateBatteryTouchFlagsStatic();
        });

        batteryValueOnlyCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            BatteryConfig.showOnlyValue = isChecked;
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("battery_show_only_value", isChecked).apply();
        });
    }

    private void saveBatteryShadowPrefs() {
        activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE).edit()
                .putInt("battery_shadow_color", BatteryConfig.shadow.color)
                .putFloat("battery_shadow_blur", BatteryConfig.shadow.blur)
                .putFloat("battery_shadow_offset_x", BatteryConfig.shadow.offsetX)
                .putFloat("battery_shadow_offset_y", BatteryConfig.shadow.offsetY)
                .apply();
    }
}
