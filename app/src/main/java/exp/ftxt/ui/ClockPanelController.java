package exp.ftxt.ui;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.CheckBox;
import android.widget.TextView;

import exp.ftxt.MainActivity;
import exp.ftxt.R;
import exp.ftxt.core.FloatingService;
import exp.ftxt.features.clock.ClockConfig;
import exp.ftxt.shared.ui.ColorPickerDialog;
import exp.ftxt.utils.PermissionHelper;

public class ClockPanelController {

    private final MainActivity activity;

    private CheckBox clockSwitch;
    private SeekBar clockSizeSeekBar;
    private Button clockColorButton;
    private CheckBox clockShadowSwitch;
    private LinearLayout clockShadowConfigContainer;
    private Button clockShadowColorButton;
    private SeekBar clockShadowBlurSeekBar;
    private SeekBar clockShadowOffsetXSeekBar;
    private SeekBar clockShadowOffsetYSeekBar;
    private CheckBox clockLockSwitch;
    private CheckBox clockBgSwitch;
    private LinearLayout clockBgConfigContainer;
    private Button clockBgColorButton;
    private SeekBar clockBgPaddingSeekBar;
    private SeekBar clockBgOffsetXSeekBar;
    private SeekBar clockBgOffsetYSeekBar;
    private SeekBar clockBgMarginSeekBar;
    private SeekBar clockBgRadiusSeekBar;
    private TextView clockSizeLabel, clockBgPaddingLabel, clockBgOffsetXLabel, clockBgOffsetYLabel;
    private TextView clockBgMarginLabel, clockBgRadiusLabel;
    private TextView clockShadowBlurLabel, clockShadowOffsetXLabel, clockShadowOffsetYLabel;
    private ClockPositionController clockPositionController;

    public ClockPanelController(MainActivity activity) {
        this.activity = activity;
        bindViews();
        loadConfig();
        setupListeners();
        clockPositionController = new ClockPositionController(activity);
    }

    public void onPanelShown() {
        if (clockPositionController != null) {
            clockPositionController.refresh();
        }
    }

    public void showLoadPresetDialog() {
        if (clockPositionController != null) {
            clockPositionController.showLoadPresetDialog();
        }
    }

    public void cleanup() {
        if (clockPositionController != null) {
            clockPositionController.cleanup();
            clockPositionController = null;
        }
    }

    private void bindViews() {
        clockSwitch = activity.findViewById(R.id.clockSwitch);
        clockSizeSeekBar = activity.findViewById(R.id.clockSizeSeekBar);
        clockColorButton = activity.findViewById(R.id.clockColorButton);
        clockShadowSwitch = activity.findViewById(R.id.clockShadowSwitch);
        clockShadowConfigContainer = activity.findViewById(R.id.shadowConfigClock);
        clockShadowColorButton = activity.findViewById(R.id.clockShadowColorButton);
        clockShadowBlurSeekBar = activity.findViewById(R.id.clockShadowBlurSeekBar);
        clockShadowOffsetXSeekBar = activity.findViewById(R.id.clockShadowOffsetXSeekBar);
        clockShadowOffsetYSeekBar = activity.findViewById(R.id.clockShadowOffsetYSeekBar);
        clockLockSwitch = activity.findViewById(R.id.clockLockSwitch);
        clockBgSwitch = activity.findViewById(R.id.clockBgSwitch);
        clockBgConfigContainer = activity.findViewById(R.id.bgConfigClock);
        clockBgColorButton = activity.findViewById(R.id.clockBgColorButton);
        clockBgPaddingSeekBar = activity.findViewById(R.id.clockBgPaddingSeekBar);
        clockBgOffsetXSeekBar = activity.findViewById(R.id.clockBgOffsetXSeekBar);
        clockBgOffsetYSeekBar = activity.findViewById(R.id.clockBgOffsetYSeekBar);
        clockBgMarginSeekBar = activity.findViewById(R.id.clockBgMarginSeekBar);
        clockBgRadiusSeekBar = activity.findViewById(R.id.clockBgRadiusSeekBar);
        clockSizeLabel = activity.findViewById(R.id.clockSizeLabel);
        clockBgPaddingLabel = activity.findViewById(R.id.clockBgPaddingLabel);
        clockBgOffsetXLabel = activity.findViewById(R.id.clockBgOffsetXLabel);
        clockBgOffsetYLabel = activity.findViewById(R.id.clockBgOffsetYLabel);
        clockBgMarginLabel = activity.findViewById(R.id.clockBgMarginLabel);
        clockBgRadiusLabel = activity.findViewById(R.id.clockBgRadiusLabel);
        clockShadowBlurLabel = activity.findViewById(R.id.clockShadowBlurLabel);
        clockShadowOffsetXLabel = activity.findViewById(R.id.clockShadowOffsetXLabel);
        clockShadowOffsetYLabel = activity.findViewById(R.id.clockShadowOffsetYLabel);
    }

    private void loadConfig() {
        clockSwitch.setChecked(ClockConfig.enabled);
        activity.applyCheckboxTint(clockSwitch, ClockConfig.enabled);
        clockSizeSeekBar.setProgress((int) ClockConfig.size);
        clockBgSwitch.setChecked(ClockConfig.bgEnabled);
        activity.applyCheckboxTint(clockBgSwitch, ClockConfig.bgEnabled);
        clockBgConfigContainer.setVisibility(ClockConfig.bgEnabled ? View.VISIBLE : View.GONE);
        clockBgPaddingSeekBar.setProgress(ClockConfig.bgPadding);
        clockBgOffsetXSeekBar.setProgress(ClockConfig.bgOffsetX + 60);
        clockBgOffsetYSeekBar.setProgress(ClockConfig.bgOffsetY + 60);
        clockBgMarginSeekBar.setProgress(ClockConfig.bgMargin);
        clockBgRadiusSeekBar.setProgress(ClockConfig.bgRadius);
        clockShadowSwitch.setChecked(ClockConfig.shadow.enabled);
        activity.applyCheckboxTint(clockShadowSwitch, ClockConfig.shadow.enabled);
        clockShadowConfigContainer.setVisibility(ClockConfig.shadow.enabled ? View.VISIBLE : View.GONE);
        clockShadowBlurSeekBar.setProgress((int) ClockConfig.shadow.blur);
        clockShadowOffsetXSeekBar.setProgress((int) ClockConfig.shadow.offsetX + 60);
        clockShadowOffsetYSeekBar.setProgress((int) ClockConfig.shadow.offsetY + 60);
        clockLockSwitch.setChecked(ClockConfig.touchPassthrough);
        activity.applyCheckboxTint(clockLockSwitch, ClockConfig.touchPassthrough);
        clockSizeLabel.setText("Ukuran Teks: " + (int) ClockConfig.size);
        clockBgPaddingLabel.setText("Ukuran Background: " + ClockConfig.bgPadding);
        clockBgOffsetXLabel.setText("Offset X: " + ClockConfig.bgOffsetX);
        clockBgOffsetYLabel.setText("Offset Y: " + ClockConfig.bgOffsetY);
        clockBgMarginLabel.setText("Margin: " + ClockConfig.bgMargin);
        clockBgRadiusLabel.setText("Radius: " + ClockConfig.bgRadius);
        clockShadowBlurLabel.setText("Blur Shadow: " + (int) ClockConfig.shadow.blur);
        clockShadowOffsetXLabel.setText("Shadow X: " + (int) ClockConfig.shadow.offsetX);
        clockShadowOffsetYLabel.setText("Shadow Y: " + (int) ClockConfig.shadow.offsetY);
    }

    private void setupListeners() {
        clockSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !PermissionHelper.hasOverlayPermission(activity)) {
                clockSwitch.setChecked(false);
                activity.applyCheckboxTint(clockSwitch, false);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putBoolean("clock_enabled", false).apply();
                return;
            }

            ClockConfig.enabled = isChecked;
            activity.applyCheckboxTint(clockSwitch, isChecked);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("clock_enabled", isChecked).apply();

            if (isChecked) {
                if (FloatingService.instance != null) {
                    FloatingService.startClockStatic();
                } else {
                    activity.startService(new Intent(activity, FloatingService.class));
                }
            } else {
                FloatingService.stopClockStatic();
                if (!activity.isTextOverlayOn() && !exp.ftxt.features.fps.FpsConfig.enabled
                        && !exp.ftxt.features.battery.BatteryConfig.enabled) {
                    activity.stopService(new Intent(activity, FloatingService.class));
                }
            }
        });

        clockSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (progress < 10) { progress = 10; sb.setProgress(progress); }
                if (progress > 200) { progress = 200; sb.setProgress(progress); }
                ClockConfig.size = progress;
                clockSizeLabel.setText("Ukuran Teks: " + progress);
                FloatingService.updateClockSizeStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        clockColorButton.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Pilih Warna Jam", ClockConfig.color, color -> {
                ClockConfig.color = color;
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("clock_color", color).apply();
                FloatingService.updateClockColorStatic();
            });
        });

        clockBgSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ClockConfig.bgEnabled = isChecked;
            activity.applyCheckboxTint(clockBgSwitch, isChecked);
            clockBgConfigContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("clock_bg_enabled", isChecked).apply();
            FloatingService.updateClockBackgroundStatic();
        });

        clockBgColorButton.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Warna Background Jam", ClockConfig.bgColor, color -> {
                ClockConfig.bgColor = color;
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("clock_bg_color", color).apply();
                FloatingService.updateClockBackgroundStatic();
            });
        });

        clockBgPaddingSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (progress < 0) progress = 0;
                ClockConfig.bgPadding = progress;
                clockBgPaddingLabel.setText("Ukuran Background: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("clock_bg_padding", progress).apply();
                FloatingService.updateClockBackgroundStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        clockBgOffsetXSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                ClockConfig.bgOffsetX = offset;
                clockBgOffsetXLabel.setText("Offset X: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("clock_bg_offset_x", offset).apply();
                FloatingService.updateClockBackgroundStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        clockBgOffsetYSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                ClockConfig.bgOffsetY = offset;
                clockBgOffsetYLabel.setText("Offset Y: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("clock_bg_offset_y", offset).apply();
                FloatingService.updateClockBackgroundStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        clockBgMarginSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                ClockConfig.bgMargin = progress;
                clockBgMarginLabel.setText("Margin: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("clock_bg_margin", progress).apply();
                FloatingService.updateClockBackgroundStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        clockBgRadiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                ClockConfig.bgRadius = progress;
                clockBgRadiusLabel.setText("Radius: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("clock_bg_radius", progress).apply();
                FloatingService.updateClockBackgroundStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        clockShadowSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ClockConfig.shadow.enabled = isChecked;
            activity.applyCheckboxTint(clockShadowSwitch, isChecked);
            clockShadowConfigContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("clock_shadow_enabled", isChecked).apply();
            saveClockShadowPrefs();
            FloatingService.updateClockShadowStatic();
        });

        clockShadowColorButton.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Warna Shadow Jam", ClockConfig.shadow.color, color -> {
                ClockConfig.shadow.color = color;
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("clock_shadow_color", color).apply();
                FloatingService.updateClockShadowStatic();
            });
        });

        clockShadowBlurSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                ClockConfig.shadow.blur = progress;
                clockShadowBlurLabel.setText("Blur Shadow: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putFloat("clock_shadow_blur", (float) progress).apply();
                FloatingService.updateClockShadowStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        clockShadowOffsetXSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                ClockConfig.shadow.offsetX = offset;
                clockShadowOffsetXLabel.setText("Shadow X: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putFloat("clock_shadow_offset_x", (float) offset).apply();
                FloatingService.updateClockShadowStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        clockShadowOffsetYSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                ClockConfig.shadow.offsetY = offset;
                clockShadowOffsetYLabel.setText("Shadow Y: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putFloat("clock_shadow_offset_y", (float) offset).apply();
                FloatingService.updateClockShadowStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        clockLockSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ClockConfig.touchPassthrough = isChecked;
            activity.applyCheckboxTint(clockLockSwitch, isChecked);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("clock_lock", isChecked).apply();
            FloatingService.updateClockTouchFlagsStatic();
        });
    }

    private void saveClockShadowPrefs() {
        activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE).edit()
                .putInt("clock_shadow_color", ClockConfig.shadow.color)
                .putFloat("clock_shadow_blur", ClockConfig.shadow.blur)
                .putFloat("clock_shadow_offset_x", ClockConfig.shadow.offsetX)
                .putFloat("clock_shadow_offset_y", ClockConfig.shadow.offsetY)
                .apply();
    }
}
