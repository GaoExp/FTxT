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
import exp.ftxt.shared.ui.SectionHelper;
import exp.ftxt.utils.PermissionHelper;

public class WatermarkPanelController {

    private final MainActivity activity;

    private CheckBox watermarkSwitch;
    private EditText watermarkEditText;
    private SeekBar watermarkSizeSeekBar;
    private View watermarkColorPreview;
    private CheckBox watermarkShadowSwitch;
    private LinearLayout watermarkShadowConfigContainer;
    private View watermarkShadowColorPreview;
    private SeekBar watermarkShadowBlurSeekBar;
    private SeekBar watermarkShadowOffsetXSeekBar;
    private SeekBar watermarkShadowOffsetYSeekBar;
    private CheckBox watermarkLockSwitch;
    private CheckBox watermarkSafeArea;
    private CheckBox watermarkBgSwitch;
    private LinearLayout watermarkBgConfigContainer;
    private View watermarkBgColorPreview;
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
        watermarkColorPreview = activity.findViewById(R.id.watermarkColorPreview);
        watermarkShadowSwitch = activity.findViewById(R.id.watermarkShadowSwitch);
        watermarkShadowConfigContainer = activity.findViewById(R.id.shadowConfigWatermark);
        watermarkShadowColorPreview = activity.findViewById(R.id.watermarkShadowColorPreview);
        watermarkShadowBlurSeekBar = activity.findViewById(R.id.watermarkShadowBlurSeekBar);
        watermarkShadowOffsetXSeekBar = activity.findViewById(R.id.watermarkShadowOffsetXSeekBar);
        watermarkShadowOffsetYSeekBar = activity.findViewById(R.id.watermarkShadowOffsetYSeekBar);
        watermarkLockSwitch = activity.findViewById(R.id.watermarkLockSwitch);
        watermarkSafeArea = activity.findViewById(R.id.watermarkSafeArea);
        watermarkBgSwitch = activity.findViewById(R.id.watermarkBgSwitch);
        watermarkBgConfigContainer = activity.findViewById(R.id.bgConfigWatermark);
        watermarkBgColorPreview = activity.findViewById(R.id.watermarkBgColorPreview);
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

        View sectionDisplay = activity.findViewById(R.id.watermark_sectionDisplay);
        TextView sectionDisplayHeader = activity.findViewById(R.id.watermark_sectionDisplayHeader);
        SectionHelper.setupCollapsible(sectionDisplayHeader, sectionDisplay);

        // watermarkPositionContainer already exists, reuse it for position section
        View sectionPosition = activity.findViewById(R.id.watermarkPositionContainer);
        TextView sectionPositionHeader = activity.findViewById(R.id.watermark_sectionPositionHeader);
        SectionHelper.setupCollapsible(sectionPositionHeader, sectionPosition);

        View sectionShadow = activity.findViewById(R.id.watermark_sectionShadow);
        TextView sectionShadowHeader = activity.findViewById(R.id.watermark_sectionShadowHeader);
        SectionHelper.setupCollapsible(sectionShadowHeader, sectionShadow);

        View sectionBackground = activity.findViewById(R.id.watermark_sectionBackground);
        TextView sectionBackgroundHeader = activity.findViewById(R.id.watermark_sectionBackgroundHeader);
        SectionHelper.setupCollapsible(sectionBackgroundHeader, sectionBackground);
    }

    private void loadConfig() {
        watermarkSwitch.setChecked(WatermarkConfig.enabled);
        activity.applyCheckboxTint(watermarkSwitch, WatermarkConfig.enabled);
        watermarkEditText.setText(WatermarkConfig.text);
        watermarkSizeSeekBar.setProgress((int) WatermarkConfig.size);
        watermarkBgSwitch.setChecked(WatermarkConfig.bg.enabled);
        activity.applyCheckboxTint(watermarkBgSwitch, WatermarkConfig.bg.enabled);
        watermarkBgConfigContainer.setVisibility(WatermarkConfig.bg.enabled ? View.VISIBLE : View.GONE);
        watermarkBgPaddingSeekBar.setProgress(WatermarkConfig.bg.padding);
        watermarkBgOffsetXSeekBar.setProgress(WatermarkConfig.bg.offsetX + 60);
        watermarkBgOffsetYSeekBar.setProgress(WatermarkConfig.bg.offsetY + 60);
        watermarkBgMarginSeekBar.setProgress(WatermarkConfig.bg.margin);
        watermarkBgRadiusSeekBar.setProgress(WatermarkConfig.bg.radius);
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
        watermarkColorPreview.setBackgroundColor(WatermarkConfig.color);
        watermarkBgPaddingLabel.setText("Ukuran Background: " + WatermarkConfig.bg.padding);
        watermarkBgOffsetXLabel.setText("Offset X: " + WatermarkConfig.bg.offsetX);
        watermarkBgOffsetYLabel.setText("Offset Y: " + WatermarkConfig.bg.offsetY);
        watermarkBgColorPreview.setBackgroundColor(WatermarkConfig.bg.color);
        watermarkBgMarginLabel.setText("Margin: " + WatermarkConfig.bg.margin);
        watermarkBgRadiusLabel.setText("Radius: " + WatermarkConfig.bg.radius);
        watermarkShadowBlurLabel.setText("Blur Shadow: " + (int) WatermarkConfig.shadow.blur);
        watermarkShadowOffsetXLabel.setText("Shadow X: " + (int) WatermarkConfig.shadow.offsetX);
        watermarkShadowOffsetYLabel.setText("Shadow Y: " + (int) WatermarkConfig.shadow.offsetY);
        watermarkShadowColorPreview.setBackgroundColor(WatermarkConfig.shadow.color);

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
                if (!activity.isAnyModuleActive()) {
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

        watermarkColorPreview.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Pilih Warna Watermark", WatermarkConfig.color, color -> {
                WatermarkConfig.color = color;
                watermarkColorPreview.setBackgroundColor(color);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("watermark_color", color).apply();
                FloatingService.updateWatermarkColorStatic();
            });
        });

        watermarkBgSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            WatermarkConfig.bg.enabled = isChecked;
            activity.applyCheckboxTint(watermarkBgSwitch, isChecked);
            watermarkBgConfigContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("watermark_bg_enabled", isChecked).apply();
            FloatingService.updateWatermarkBackgroundStatic();
        });

        watermarkBgColorPreview.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Warna Background Watermark", WatermarkConfig.bg.color, color -> {
                WatermarkConfig.bg.color = color;
                watermarkBgColorPreview.setBackgroundColor(color);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("watermark_bg_color", color).apply();
                FloatingService.updateWatermarkBackgroundStatic();
            });
        });

        watermarkBgPaddingSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (progress < 0) progress = 0;
                WatermarkConfig.bg.padding = progress;
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
                WatermarkConfig.bg.offsetX = offset;
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
                WatermarkConfig.bg.offsetY = offset;
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
                WatermarkConfig.bg.margin = progress;
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
                WatermarkConfig.bg.radius = progress;
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

        watermarkShadowColorPreview.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Warna Shadow Watermark", WatermarkConfig.shadow.color, color -> {
                WatermarkConfig.shadow.color = color;
                watermarkShadowColorPreview.setBackgroundColor(color);
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
