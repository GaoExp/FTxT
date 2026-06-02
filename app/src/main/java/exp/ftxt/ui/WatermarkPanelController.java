package exp.ftxt.ui;

import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.CheckBox;
import android.widget.TextView;

import exp.ftxt.MainActivity;
import exp.ftxt.R;
import exp.ftxt.core.FloatingService;
import exp.ftxt.features.watermark.WatermarkConfig;
import exp.ftxt.shared.ui.ColorPickerDialog;
import exp.ftxt.utils.PermissionHelper;

public class WatermarkPanelController {

    private final MainActivity activity;

    private CheckBox watermarkSwitch;
    private EditText watermarkEditText;
    private SeekBar watermarkSizeSeekBar;
    private Button watermarkColorButton;
    private CheckBox watermarkShadowSwitch;
    private LinearLayout watermarkShadowConfigContainer;
    private Button watermarkShadowColorButton;
    private SeekBar watermarkShadowBlurSeekBar;
    private SeekBar watermarkShadowOffsetXSeekBar;
    private SeekBar watermarkShadowOffsetYSeekBar;
    private CheckBox watermarkLockSwitch;
    private CheckBox watermarkSafeArea;
    private CheckBox watermarkBgSwitch;
    private LinearLayout watermarkBgConfigContainer;
    private Button watermarkBgColorButton;
    private SeekBar watermarkBgPaddingSeekBar;
    private SeekBar watermarkBgOffsetXSeekBar;
    private SeekBar watermarkBgOffsetYSeekBar;
    private SeekBar watermarkBgMarginSeekBar;
    private SeekBar watermarkBgRadiusSeekBar;
    private TextView watermarkSizeLabel, watermarkBgPaddingLabel, watermarkBgOffsetXLabel, watermarkBgOffsetYLabel;
    private TextView watermarkBgMarginLabel, watermarkBgRadiusLabel;
    private TextView watermarkShadowBlurLabel, watermarkShadowOffsetXLabel, watermarkShadowOffsetYLabel;
    private WatermarkPositionController watermarkPositionController;

    private CheckBox watermarkPatternSwitch;
    private SeekBar watermarkPatternSpacingH;
    private SeekBar watermarkPatternSpacingV;
    private SeekBar watermarkPatternAngle;
    private TextView watermarkPatternSpacingHLabel;
    private TextView watermarkPatternSpacingVLabel;
    private TextView watermarkPatternAngleLabel;
    private LinearLayout watermarkPatternContainer;
    private LinearLayout watermarkPositionContainer;

    public WatermarkPanelController(MainActivity activity) {
        this.activity = activity;
        bindViews();
        loadConfig();
        setupListeners();
        watermarkPositionController = new WatermarkPositionController(activity);
    }

    public void onPanelShown() {
        if (watermarkPositionController != null) {
            watermarkPositionController.refresh();
        }
    }

    public void showLoadPresetDialog() {
        if (watermarkPositionController != null) {
            watermarkPositionController.showLoadPresetDialog();
        }
    }

    public void cleanup() {
        if (watermarkPositionController != null) {
            watermarkPositionController.cleanup();
            watermarkPositionController = null;
        }
    }

    private void bindViews() {
        watermarkSwitch = activity.findViewById(R.id.watermarkSwitch);
        watermarkEditText = activity.findViewById(R.id.watermarkEditText);
        watermarkSizeSeekBar = activity.findViewById(R.id.watermarkSizeSeekBar);
        watermarkColorButton = activity.findViewById(R.id.watermarkColorButton);
        watermarkShadowSwitch = activity.findViewById(R.id.watermarkShadowSwitch);
        watermarkShadowConfigContainer = activity.findViewById(R.id.shadowConfigWatermark);
        watermarkShadowColorButton = activity.findViewById(R.id.watermarkShadowColorButton);
        watermarkShadowBlurSeekBar = activity.findViewById(R.id.watermarkShadowBlurSeekBar);
        watermarkShadowOffsetXSeekBar = activity.findViewById(R.id.watermarkShadowOffsetXSeekBar);
        watermarkShadowOffsetYSeekBar = activity.findViewById(R.id.watermarkShadowOffsetYSeekBar);
        watermarkLockSwitch = activity.findViewById(R.id.watermarkLockSwitch);
        watermarkSafeArea = activity.findViewById(R.id.watermarkSafeArea);
        watermarkBgSwitch = activity.findViewById(R.id.watermarkBgSwitch);
        watermarkBgConfigContainer = activity.findViewById(R.id.bgConfigWatermark);
        watermarkBgColorButton = activity.findViewById(R.id.watermarkBgColorButton);
        watermarkBgPaddingSeekBar = activity.findViewById(R.id.watermarkBgPaddingSeekBar);
        watermarkBgOffsetXSeekBar = activity.findViewById(R.id.watermarkBgOffsetXSeekBar);
        watermarkBgOffsetYSeekBar = activity.findViewById(R.id.watermarkBgOffsetYSeekBar);
        watermarkBgMarginSeekBar = activity.findViewById(R.id.watermarkBgMarginSeekBar);
        watermarkBgRadiusSeekBar = activity.findViewById(R.id.watermarkBgRadiusSeekBar);
        watermarkSizeLabel = activity.findViewById(R.id.watermarkSizeLabel);
        watermarkBgPaddingLabel = activity.findViewById(R.id.watermarkBgPaddingLabel);
        watermarkBgOffsetXLabel = activity.findViewById(R.id.watermarkBgOffsetXLabel);
        watermarkBgOffsetYLabel = activity.findViewById(R.id.watermarkBgOffsetYLabel);
        watermarkBgMarginLabel = activity.findViewById(R.id.watermarkBgMarginLabel);
        watermarkBgRadiusLabel = activity.findViewById(R.id.watermarkBgRadiusLabel);
        watermarkShadowBlurLabel = activity.findViewById(R.id.watermarkShadowBlurLabel);
        watermarkShadowOffsetXLabel = activity.findViewById(R.id.watermarkShadowOffsetXLabel);
        watermarkShadowOffsetYLabel = activity.findViewById(R.id.watermarkShadowOffsetYLabel);
        watermarkPatternSwitch = activity.findViewById(R.id.watermarkPatternSwitch);
        watermarkPatternSpacingH = activity.findViewById(R.id.watermarkPatternSpacingH);
        watermarkPatternSpacingV = activity.findViewById(R.id.watermarkPatternSpacingV);
        watermarkPatternAngle = activity.findViewById(R.id.watermarkPatternAngle);
        watermarkPatternSpacingHLabel = activity.findViewById(R.id.watermarkPatternSpacingHLabel);
        watermarkPatternSpacingVLabel = activity.findViewById(R.id.watermarkPatternSpacingVLabel);
        watermarkPatternAngleLabel = activity.findViewById(R.id.watermarkPatternAngleLabel);
        watermarkPatternContainer = activity.findViewById(R.id.watermarkPatternContainer);
        watermarkPositionContainer = activity.findViewById(R.id.watermarkPositionContainer);
    }

    private void loadConfig() {
        watermarkSwitch.setChecked(WatermarkConfig.enabled);
        activity.applyCheckboxTint(watermarkSwitch, WatermarkConfig.enabled);
        watermarkEditText.setText(WatermarkConfig.text);
        watermarkSizeSeekBar.setProgress((int) WatermarkConfig.size);
        watermarkBgSwitch.setChecked(WatermarkConfig.bgEnabled);
        activity.applyCheckboxTint(watermarkBgSwitch, WatermarkConfig.bgEnabled);
        watermarkBgConfigContainer.setVisibility(WatermarkConfig.bgEnabled ? View.VISIBLE : View.GONE);
        watermarkBgPaddingSeekBar.setProgress(WatermarkConfig.bgPadding);
        watermarkBgOffsetXSeekBar.setProgress(WatermarkConfig.bgOffsetX + 60);
        watermarkBgOffsetYSeekBar.setProgress(WatermarkConfig.bgOffsetY + 60);
        watermarkBgMarginSeekBar.setProgress(WatermarkConfig.bgMargin);
        watermarkBgRadiusSeekBar.setProgress(WatermarkConfig.bgRadius);
        watermarkShadowSwitch.setChecked(WatermarkConfig.shadow.enabled);
        activity.applyCheckboxTint(watermarkShadowSwitch, WatermarkConfig.shadow.enabled);
        watermarkShadowConfigContainer.setVisibility(WatermarkConfig.shadow.enabled ? View.VISIBLE : View.GONE);
        watermarkShadowBlurSeekBar.setProgress((int) WatermarkConfig.shadow.blur);
        watermarkShadowOffsetXSeekBar.setProgress((int) WatermarkConfig.shadow.offsetX + 60);
        watermarkShadowOffsetYSeekBar.setProgress((int) WatermarkConfig.shadow.offsetY + 60);
        watermarkLockSwitch.setChecked(WatermarkConfig.touchPassthrough);
        activity.applyCheckboxTint(watermarkLockSwitch, WatermarkConfig.touchPassthrough);
        watermarkSafeArea.setChecked(WatermarkConfig.safeArea);
        watermarkSizeLabel.setText("Ukuran Teks: " + (int) WatermarkConfig.size);
        watermarkBgPaddingLabel.setText("Ukuran Background: " + WatermarkConfig.bgPadding);
        watermarkBgOffsetXLabel.setText("Offset X: " + WatermarkConfig.bgOffsetX);
        watermarkBgOffsetYLabel.setText("Offset Y: " + WatermarkConfig.bgOffsetY);
        watermarkBgMarginLabel.setText("Margin: " + WatermarkConfig.bgMargin);
        watermarkBgRadiusLabel.setText("Radius: " + WatermarkConfig.bgRadius);
        watermarkShadowBlurLabel.setText("Blur Shadow: " + (int) WatermarkConfig.shadow.blur);
        watermarkShadowOffsetXLabel.setText("Shadow X: " + (int) WatermarkConfig.shadow.offsetX);
        watermarkShadowOffsetYLabel.setText("Shadow Y: " + (int) WatermarkConfig.shadow.offsetY);

        watermarkPatternSwitch.setChecked(WatermarkConfig.patternEnabled);
        activity.applyCheckboxTint(watermarkPatternSwitch, WatermarkConfig.patternEnabled);
        watermarkPatternSpacingH.setProgress((int) WatermarkConfig.patternSpacingH);
        watermarkPatternSpacingV.setProgress((int) WatermarkConfig.patternSpacingV);
        watermarkPatternAngle.setProgress((int) WatermarkConfig.patternAngle + 90);
        watermarkPatternSpacingHLabel.setText("Spasi Horizontal: " + (int) WatermarkConfig.patternSpacingH);
        watermarkPatternSpacingVLabel.setText("Spasi Vertikal: " + (int) WatermarkConfig.patternSpacingV);
        watermarkPatternAngleLabel.setText("Sudut: " + (int) WatermarkConfig.patternAngle + "\u00B0");
        updatePatternVisibility();
    }

    private void setupListeners() {
        watermarkSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !PermissionHelper.hasOverlayPermission(activity)) {
                watermarkSwitch.setChecked(false);
                activity.applyCheckboxTint(watermarkSwitch, false);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putBoolean("watermark_enabled", false).apply();
                return;
            }

            WatermarkConfig.enabled = isChecked;
            activity.applyCheckboxTint(watermarkSwitch, isChecked);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("watermark_enabled", isChecked).apply();

            if (isChecked) {
                if (FloatingService.instance != null) {
                    FloatingService.startWatermarkStatic();
                } else {
                    activity.startService(new Intent(activity, FloatingService.class));
                }
            } else {
                FloatingService.stopWatermarkStatic();
                if (!activity.isTextOverlayOn() && !exp.ftxt.features.fps_display.FpsConfig.enabled
                        && !exp.ftxt.features.clock_module.ClockConfig.enabled
                        && !exp.ftxt.features.network_stats.NetworkConfig.enabled
                        && !exp.ftxt.features.battery_temperature.BatteryConfig.enabled
                        && !exp.ftxt.features.battery_percentage.BatteryPercentageConfig.enabled
                        && !exp.ftxt.features.battery_current.BatteryCurrentConfig.enabled) {
                    activity.stopService(new Intent(activity, FloatingService.class));
                }
            }
        });

        watermarkEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                String text = s.toString();
                WatermarkConfig.text = text;
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putString("watermark_text", text).apply();
                FloatingService.updateWatermarkTextStatic();
            }
        });

        watermarkSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (progress < 5) { progress = 5; sb.setProgress(progress); }
                if (progress > 200) { progress = 200; sb.setProgress(progress); }
                WatermarkConfig.size = progress;
                watermarkSizeLabel.setText("Ukuran Teks: " + progress);
                FloatingService.updateWatermarkSizeStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        watermarkColorButton.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Pilih Warna Watermark", WatermarkConfig.color, color -> {
                WatermarkConfig.color = color;
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("watermark_color", color).apply();
                FloatingService.updateWatermarkColorStatic();
            });
        });

        watermarkBgSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            WatermarkConfig.bgEnabled = isChecked;
            activity.applyCheckboxTint(watermarkBgSwitch, isChecked);
            watermarkBgConfigContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("watermark_bg_enabled", isChecked).apply();
            FloatingService.updateWatermarkBackgroundStatic();
        });

        watermarkBgColorButton.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Warna Background Watermark", WatermarkConfig.bgColor, color -> {
                WatermarkConfig.bgColor = color;
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("watermark_bg_color", color).apply();
                FloatingService.updateWatermarkBackgroundStatic();
            });
        });

        watermarkBgPaddingSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (progress < 0) progress = 0;
                WatermarkConfig.bgPadding = progress;
                watermarkBgPaddingLabel.setText("Ukuran Background: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("watermark_bg_padding", progress).apply();
                FloatingService.updateWatermarkBackgroundStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        watermarkBgOffsetXSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                WatermarkConfig.bgOffsetX = offset;
                watermarkBgOffsetXLabel.setText("Offset X: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("watermark_bg_offset_x", offset).apply();
                FloatingService.updateWatermarkBackgroundStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        watermarkBgOffsetYSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                WatermarkConfig.bgOffsetY = offset;
                watermarkBgOffsetYLabel.setText("Offset Y: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("watermark_bg_offset_y", offset).apply();
                FloatingService.updateWatermarkBackgroundStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        watermarkBgMarginSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                WatermarkConfig.bgMargin = progress;
                watermarkBgMarginLabel.setText("Margin: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("watermark_bg_margin", progress).apply();
                FloatingService.updateWatermarkBackgroundStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        watermarkBgRadiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                WatermarkConfig.bgRadius = progress;
                watermarkBgRadiusLabel.setText("Radius: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("watermark_bg_radius", progress).apply();
                FloatingService.updateWatermarkBackgroundStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        watermarkShadowSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            WatermarkConfig.shadow.enabled = isChecked;
            activity.applyCheckboxTint(watermarkShadowSwitch, isChecked);
            watermarkShadowConfigContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("watermark_shadow_enabled", isChecked).apply();
            saveWatermarkShadowPrefs();
            FloatingService.updateWatermarkShadowStatic();
        });

        watermarkShadowColorButton.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Warna Shadow Watermark", WatermarkConfig.shadow.color, color -> {
                WatermarkConfig.shadow.color = color;
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("watermark_shadow_color", color).apply();
                FloatingService.updateWatermarkShadowStatic();
            });
        });

        watermarkShadowBlurSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                WatermarkConfig.shadow.blur = progress;
                watermarkShadowBlurLabel.setText("Blur Shadow: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putFloat("watermark_shadow_blur", (float) progress).apply();
                FloatingService.updateWatermarkShadowStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        watermarkShadowOffsetXSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                WatermarkConfig.shadow.offsetX = offset;
                watermarkShadowOffsetXLabel.setText("Shadow X: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putFloat("watermark_shadow_offset_x", (float) offset).apply();
                FloatingService.updateWatermarkShadowStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        watermarkShadowOffsetYSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                WatermarkConfig.shadow.offsetY = offset;
                watermarkShadowOffsetYLabel.setText("Shadow Y: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putFloat("watermark_shadow_offset_y", (float) offset).apply();
                FloatingService.updateWatermarkShadowStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        watermarkLockSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            WatermarkConfig.touchPassthrough = isChecked;
            activity.applyCheckboxTint(watermarkLockSwitch, isChecked);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("watermark_lock", isChecked).apply();
            FloatingService.updateWatermarkTouchFlagsStatic();
        });

        watermarkSafeArea.setOnCheckedChangeListener((buttonView, isChecked) -> {
            WatermarkConfig.safeArea = isChecked;
            activity.applyCheckboxTint(watermarkSafeArea, isChecked);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("watermark_safe_area", isChecked).apply();
        });

        watermarkPatternSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            WatermarkConfig.patternEnabled = isChecked;
            activity.applyCheckboxTint(watermarkPatternSwitch, isChecked);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("watermark_pattern_enabled", isChecked).apply();
            updatePatternVisibility();
            FloatingService.updateWatermarkPatternStatic();
        });

        watermarkPatternSpacingH.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (progress < 50) progress = 50;
                WatermarkConfig.patternSpacingH = progress;
                watermarkPatternSpacingHLabel.setText("Spasi Horizontal: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putFloat("watermark_pattern_spacing_h", (float) progress).apply();
                FloatingService.updateWatermarkPatternStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        watermarkPatternSpacingV.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (progress < 50) progress = 50;
                WatermarkConfig.patternSpacingV = progress;
                watermarkPatternSpacingVLabel.setText("Spasi Vertikal: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putFloat("watermark_pattern_spacing_v", (float) progress).apply();
                FloatingService.updateWatermarkPatternStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        watermarkPatternAngle.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int angle = progress - 90;
                WatermarkConfig.patternAngle = angle;
                watermarkPatternAngleLabel.setText("Sudut: " + angle + "\u00B0");
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putFloat("watermark_pattern_angle", (float) angle).apply();
                FloatingService.updateWatermarkPatternStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });
    }

    private void updatePatternVisibility() {
        boolean pattern = WatermarkConfig.patternEnabled;
        if (watermarkPositionContainer != null)
            watermarkPositionContainer.setVisibility(pattern ? View.GONE : View.VISIBLE);
        if (watermarkPatternContainer != null)
            watermarkPatternContainer.setVisibility(pattern ? View.VISIBLE : View.GONE);
        if (watermarkLockSwitch != null)
            watermarkLockSwitch.setVisibility(pattern ? View.GONE : View.VISIBLE);
    }

    private void saveWatermarkShadowPrefs() {
        activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE).edit()
                .putInt("watermark_shadow_color", WatermarkConfig.shadow.color)
                .putFloat("watermark_shadow_blur", WatermarkConfig.shadow.blur)
                .putFloat("watermark_shadow_offset_x", WatermarkConfig.shadow.offsetX)
                .putFloat("watermark_shadow_offset_y", WatermarkConfig.shadow.offsetY)
                .apply();
    }
}
