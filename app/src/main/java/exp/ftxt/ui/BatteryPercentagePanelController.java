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
import exp.ftxt.features.battery_percentage.BatteryPercentageConfig;
import exp.ftxt.shared.ui.ColorPickerDialog;
import exp.ftxt.shared.ui.SectionHelper;
import exp.ftxt.utils.PermissionHelper;

public class BatteryPercentagePanelController {

    private final MainActivity activity;

    private CheckBox batPctSwitch;
    private SeekBar batPctSizeSeekBar;
    private View batPctColorPreview;
    private View batPctLabelColorPreview;
    private CheckBox batPctShadowSwitch;
    private LinearLayout batPctShadowConfigContainer;
    private View batPctShadowColorPreview;
    private SeekBar batPctShadowBlurSeekBar;
    private SeekBar batPctShadowOffsetXSeekBar;
    private SeekBar batPctShadowOffsetYSeekBar;
    private CheckBox batPctLockSwitch;
    private CheckBox batPctBgSwitch;
    private LinearLayout batPctBgConfigContainer;
    private View batPctBgColorPreview;
    private SeekBar batPctBgPaddingSeekBar;
    private SeekBar batPctBgOffsetXSeekBar;
    private SeekBar batPctBgOffsetYSeekBar;
    private SeekBar batPctBgMarginSeekBar;
    private SeekBar batPctBgRadiusSeekBar;
    private TextView batPctSizeLabel, batPctBgPaddingLabel, batPctBgOffsetXLabel, batPctBgOffsetYLabel;
    private TextView batPctBgMarginLabel, batPctBgRadiusLabel;
    private TextView batPctShadowBlurLabel, batPctShadowOffsetXLabel, batPctShadowOffsetYLabel;
    private BatteryPercentagePositionController batPctPositionController;

    public BatteryPercentagePanelController(MainActivity activity) {
        this.activity = activity;
        bindViews();
        loadConfig();
        setupListeners();
        batPctPositionController = new BatteryPercentagePositionController(activity);
    }

    public void onPanelShown() {
        if (batPctPositionController != null) {
            batPctPositionController.refresh();
        }
    }

    public void showLoadPresetDialog() {
        if (batPctPositionController != null) {
            batPctPositionController.showLoadPresetDialog();
        }
    }

    public void cleanup() {
        if (batPctPositionController != null) {
            batPctPositionController.cleanup();
            batPctPositionController = null;
        }
    }

    private void bindViews() {
        batPctSwitch = activity.findViewById(R.id.batPctSwitch);
        batPctSizeSeekBar = activity.findViewById(R.id.batPctSizeSeekBar);
        batPctColorPreview = activity.findViewById(R.id.batPctColorPreview);
        batPctLabelColorPreview = activity.findViewById(R.id.batPctLabelColorPreview);
        batPctShadowSwitch = activity.findViewById(R.id.batPctShadowSwitch);
        batPctShadowConfigContainer = activity.findViewById(R.id.shadowConfigBatteryPercentage);
        batPctShadowColorPreview = activity.findViewById(R.id.batPctShadowColorPreview);
        batPctShadowBlurSeekBar = activity.findViewById(R.id.batPctShadowBlurSeekBar);
        batPctShadowOffsetXSeekBar = activity.findViewById(R.id.batPctShadowOffsetXSeekBar);
        batPctShadowOffsetYSeekBar = activity.findViewById(R.id.batPctShadowOffsetYSeekBar);
        batPctLockSwitch = activity.findViewById(R.id.batPctLockSwitch);
        batPctBgSwitch = activity.findViewById(R.id.batPctBgSwitch);
        batPctBgConfigContainer = activity.findViewById(R.id.bgConfigBatteryPercentage);
        batPctBgColorPreview = activity.findViewById(R.id.batPctBgColorPreview);
        batPctBgPaddingSeekBar = activity.findViewById(R.id.batPctBgPaddingSeekBar);
        batPctBgOffsetXSeekBar = activity.findViewById(R.id.batPctBgOffsetXSeekBar);
        batPctBgOffsetYSeekBar = activity.findViewById(R.id.batPctBgOffsetYSeekBar);
        batPctBgMarginSeekBar = activity.findViewById(R.id.batPctBgMarginSeekBar);
        batPctBgRadiusSeekBar = activity.findViewById(R.id.batPctBgRadiusSeekBar);
        batPctSizeLabel = activity.findViewById(R.id.batPctSizeLabel);
        batPctBgPaddingLabel = activity.findViewById(R.id.batPctBgPaddingLabel);
        batPctBgOffsetXLabel = activity.findViewById(R.id.batPctBgOffsetXLabel);
        batPctBgOffsetYLabel = activity.findViewById(R.id.batPctBgOffsetYLabel);
        batPctBgMarginLabel = activity.findViewById(R.id.batPctBgMarginLabel);
        batPctBgRadiusLabel = activity.findViewById(R.id.batPctBgRadiusLabel);
        batPctShadowBlurLabel = activity.findViewById(R.id.batPctShadowBlurLabel);
        batPctShadowOffsetXLabel = activity.findViewById(R.id.batPctShadowOffsetXLabel);
        batPctShadowOffsetYLabel = activity.findViewById(R.id.batPctShadowOffsetYLabel);

        View sectionDisplay = activity.findViewById(R.id.batPct_sectionDisplay);
        TextView sectionDisplayHeader = activity.findViewById(R.id.batPct_sectionDisplayHeader);
        SectionHelper.setupCollapsible(sectionDisplayHeader, sectionDisplay);

        View sectionPosition = activity.findViewById(R.id.batPct_sectionPosition);
        TextView sectionPositionHeader = activity.findViewById(R.id.batPct_sectionPositionHeader);
        SectionHelper.setupCollapsible(sectionPositionHeader, sectionPosition);

        View sectionShadow = activity.findViewById(R.id.batPct_sectionShadow);
        TextView sectionShadowHeader = activity.findViewById(R.id.batPct_sectionShadowHeader);
        SectionHelper.setupCollapsible(sectionShadowHeader, sectionShadow);

        View sectionBackground = activity.findViewById(R.id.batPct_sectionBackground);
        TextView sectionBackgroundHeader = activity.findViewById(R.id.batPct_sectionBackgroundHeader);
        SectionHelper.setupCollapsible(sectionBackgroundHeader, sectionBackground);
    }

    private void loadConfig() {
        batPctSwitch.setChecked(BatteryPercentageConfig.enabled);
        activity.applyCheckboxTint(batPctSwitch, BatteryPercentageConfig.enabled);
        batPctSizeSeekBar.setProgress((int) BatteryPercentageConfig.size);
        batPctBgSwitch.setChecked(BatteryPercentageConfig.bg.enabled);
        activity.applyCheckboxTint(batPctBgSwitch, BatteryPercentageConfig.bg.enabled);
        batPctBgConfigContainer.setVisibility(BatteryPercentageConfig.bg.enabled ? View.VISIBLE : View.GONE);
        batPctBgPaddingSeekBar.setProgress(BatteryPercentageConfig.bg.padding);
        batPctBgOffsetXSeekBar.setProgress(BatteryPercentageConfig.bg.offsetX + 60);
        batPctBgOffsetYSeekBar.setProgress(BatteryPercentageConfig.bg.offsetY + 60);
        batPctBgMarginSeekBar.setProgress(BatteryPercentageConfig.bg.margin);
        batPctBgRadiusSeekBar.setProgress(BatteryPercentageConfig.bg.radius);
        batPctShadowSwitch.setChecked(BatteryPercentageConfig.shadow.enabled);
        activity.applyCheckboxTint(batPctShadowSwitch, BatteryPercentageConfig.shadow.enabled);
        batPctShadowConfigContainer.setVisibility(BatteryPercentageConfig.shadow.enabled ? View.VISIBLE : View.GONE);
        batPctShadowBlurSeekBar.setProgress((int) BatteryPercentageConfig.shadow.blur);
        batPctShadowOffsetXSeekBar.setProgress((int) BatteryPercentageConfig.shadow.offsetX + 60);
        batPctShadowOffsetYSeekBar.setProgress((int) BatteryPercentageConfig.shadow.offsetY + 60);
        batPctLockSwitch.setChecked(BatteryPercentageConfig.touchPassthrough);
        activity.applyCheckboxTint(batPctLockSwitch, BatteryPercentageConfig.touchPassthrough);
        batPctSizeLabel.setText("Ukuran Teks: " + (int) BatteryPercentageConfig.size);
        batPctColorPreview.setBackgroundColor(BatteryPercentageConfig.color);
        batPctLabelColorPreview.setBackgroundColor(BatteryPercentageConfig.labelColor);
        batPctBgPaddingLabel.setText("Ukuran Background: " + BatteryPercentageConfig.bg.padding);
        batPctBgOffsetXLabel.setText("Offset X: " + BatteryPercentageConfig.bg.offsetX);
        batPctBgOffsetYLabel.setText("Offset Y: " + BatteryPercentageConfig.bg.offsetY);
        batPctBgColorPreview.setBackgroundColor(BatteryPercentageConfig.bg.color);
        batPctBgMarginLabel.setText("Margin: " + BatteryPercentageConfig.bg.margin);
        batPctBgRadiusLabel.setText("Radius: " + BatteryPercentageConfig.bg.radius);
        batPctShadowBlurLabel.setText("Blur Shadow: " + (int) BatteryPercentageConfig.shadow.blur);
        batPctShadowOffsetXLabel.setText("Shadow X: " + (int) BatteryPercentageConfig.shadow.offsetX);
        batPctShadowOffsetYLabel.setText("Shadow Y: " + (int) BatteryPercentageConfig.shadow.offsetY);
        batPctShadowColorPreview.setBackgroundColor(BatteryPercentageConfig.shadow.color);
    }

    private void setupListeners() {
        batPctSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !PermissionHelper.hasOverlayPermission(activity)) {
                batPctSwitch.setChecked(false);
                activity.applyCheckboxTint(batPctSwitch, false);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putBoolean("battpct_enabled", false).apply();
                return;
            }

            BatteryPercentageConfig.enabled = isChecked;
            activity.applyCheckboxTint(batPctSwitch, isChecked);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("battpct_enabled", isChecked).apply();

            if (isChecked) {
                if (FloatingService.instance != null) {
                    FloatingService.startBatteryPercentageStatic();
                } else {
                    activity.startService(new Intent(activity, FloatingService.class));
                }
            } else {
                FloatingService.stopBatteryPercentageStatic();
                if (!activity.isAnyModuleActive()) {
                    activity.stopService(new Intent(activity, FloatingService.class));
                }
            }
        });

        batPctSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (progress < 5) { progress = 5; sb.setProgress(progress); }
                if (progress > 140) { progress = 140; sb.setProgress(progress); }
                BatteryPercentageConfig.size = progress;
                batPctSizeLabel.setText("Ukuran Teks: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putFloat("battpct_size", (float) progress).apply();
                FloatingService.updateBatteryPercentageSizeStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        batPctColorPreview.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Pilih Warna Battery %", BatteryPercentageConfig.color, color -> {
                BatteryPercentageConfig.color = color;
                batPctColorPreview.setBackgroundColor(color);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("battpct_color", color).apply();
                FloatingService.updateBatteryPercentageColorStatic();
            });
        });

        batPctLabelColorPreview.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Pilih Warna Label", BatteryPercentageConfig.labelColor, color -> {
                BatteryPercentageConfig.labelColor = color;
                batPctLabelColorPreview.setBackgroundColor(color);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("battpct_label_color", color).apply();
                FloatingService.updateBatteryPercentageLabelColorStatic();
            });
        });

        batPctBgSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            BatteryPercentageConfig.bg.enabled = isChecked;
            activity.applyCheckboxTint(batPctBgSwitch, isChecked);
            batPctBgConfigContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("battpct_bg_enabled", isChecked).apply();
            FloatingService.updateBatteryPercentageBackgroundStatic();
        });

        batPctBgColorPreview.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Warna Background Bat %", BatteryPercentageConfig.bg.color, color -> {
                BatteryPercentageConfig.bg.color = color;
                batPctBgColorPreview.setBackgroundColor(color);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("battpct_bg_color", color).apply();
                FloatingService.updateBatteryPercentageBackgroundStatic();
            });
        });

        batPctBgPaddingSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (progress < 0) progress = 0;
                BatteryPercentageConfig.bg.padding = progress;
                batPctBgPaddingLabel.setText("Ukuran Background: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("battpct_bg_padding", progress).apply();
                FloatingService.updateBatteryPercentageBackgroundStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        batPctBgOffsetXSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                BatteryPercentageConfig.bg.offsetX = offset;
                batPctBgOffsetXLabel.setText("Offset X: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("battpct_bg_offset_x", offset).apply();
                FloatingService.updateBatteryPercentageBackgroundStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        batPctBgOffsetYSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                BatteryPercentageConfig.bg.offsetY = offset;
                batPctBgOffsetYLabel.setText("Offset Y: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("battpct_bg_offset_y", offset).apply();
                FloatingService.updateBatteryPercentageBackgroundStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        batPctBgMarginSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                BatteryPercentageConfig.bg.margin = progress;
                batPctBgMarginLabel.setText("Margin: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("battpct_bg_margin", progress).apply();
                FloatingService.updateBatteryPercentageBackgroundStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        batPctBgRadiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                BatteryPercentageConfig.bg.radius = progress;
                batPctBgRadiusLabel.setText("Radius: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("battpct_bg_radius", progress).apply();
                FloatingService.updateBatteryPercentageBackgroundStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        batPctShadowSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            BatteryPercentageConfig.shadow.enabled = isChecked;
            activity.applyCheckboxTint(batPctShadowSwitch, isChecked);
            batPctShadowConfigContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("battpct_shadow_enabled", isChecked).apply();
            saveBatPctShadowPrefs();
            FloatingService.updateBatteryPercentageShadowStatic();
        });

        batPctShadowColorPreview.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Warna Shadow Bat %", BatteryPercentageConfig.shadow.color, color -> {
                BatteryPercentageConfig.shadow.color = color;
                batPctShadowColorPreview.setBackgroundColor(color);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("battpct_shadow_color", color).apply();
                FloatingService.updateBatteryPercentageShadowStatic();
            });
        });

        batPctShadowBlurSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                BatteryPercentageConfig.shadow.blur = progress;
                batPctShadowBlurLabel.setText("Blur Shadow: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putFloat("battpct_shadow_blur", (float) progress).apply();
                FloatingService.updateBatteryPercentageShadowStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        batPctShadowOffsetXSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                BatteryPercentageConfig.shadow.offsetX = offset;
                batPctShadowOffsetXLabel.setText("Shadow X: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putFloat("battpct_shadow_offset_x", (float) offset).apply();
                FloatingService.updateBatteryPercentageShadowStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        batPctShadowOffsetYSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                BatteryPercentageConfig.shadow.offsetY = offset;
                batPctShadowOffsetYLabel.setText("Shadow Y: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putFloat("battpct_shadow_offset_y", (float) offset).apply();
                FloatingService.updateBatteryPercentageShadowStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        batPctLockSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            BatteryPercentageConfig.touchPassthrough = isChecked;
            activity.applyCheckboxTint(batPctLockSwitch, isChecked);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("battpct_lock", isChecked).apply();
            FloatingService.updateBatteryPercentageTouchFlagsStatic();
        });
    }

    private void saveBatPctShadowPrefs() {
        activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE).edit()
                .putInt("battpct_shadow_color", BatteryPercentageConfig.shadow.color)
                .putFloat("battpct_shadow_blur", BatteryPercentageConfig.shadow.blur)
                .putFloat("battpct_shadow_offset_x", BatteryPercentageConfig.shadow.offsetX)
                .putFloat("battpct_shadow_offset_y", BatteryPercentageConfig.shadow.offsetY)
                .apply();
    }
}
