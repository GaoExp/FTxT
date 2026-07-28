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
import exp.ftxt.features.clock_module.ClockConfig;
import exp.ftxt.shared.ui.ColorPickerDialog;
import exp.ftxt.shared.ui.SectionHelper;
import exp.ftxt.utils.PermissionHelper;

public class ClockPanelController {

    private final MainActivity activity;

    private CheckBox clockSwitch;
    private SeekBar clockSizeSeekBar;
    private View clockColorPreview;
    private CheckBox clockShadowSwitch;
    private LinearLayout clockShadowConfigContainer;
    private View clockShadowColorPreview;
    private SeekBar clockShadowBlurSeekBar;
    private SeekBar clockShadowOffsetXSeekBar;
    private SeekBar clockShadowOffsetYSeekBar;
    private CheckBox clockLockSwitch;
    private CheckBox clockSafeArea;
    private CheckBox clockBgSwitch;
    private LinearLayout clockBgConfigContainer;
    private View clockBgColorPreview;
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
        clockColorPreview = activity.findViewById(R.id.clockColorPreview);
        clockShadowSwitch = activity.findViewById(R.id.clockShadowSwitch);
        clockShadowConfigContainer = activity.findViewById(R.id.shadowConfigClock);
        clockShadowColorPreview = activity.findViewById(R.id.clockShadowColorPreview);
        clockShadowBlurSeekBar = activity.findViewById(R.id.clockShadowBlurSeekBar);
        clockShadowOffsetXSeekBar = activity.findViewById(R.id.clockShadowOffsetXSeekBar);
        clockShadowOffsetYSeekBar = activity.findViewById(R.id.clockShadowOffsetYSeekBar);
        clockLockSwitch = activity.findViewById(R.id.clockLockSwitch);
        clockSafeArea = activity.findViewById(R.id.clockSafeArea);
        clockBgSwitch = activity.findViewById(R.id.clockBgSwitch);
        clockBgConfigContainer = activity.findViewById(R.id.bgConfigClock);
        clockBgColorPreview = activity.findViewById(R.id.clockBgColorPreview);
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

        View sectionDisplay = activity.findViewById(R.id.clock_sectionDisplay);
        TextView sectionDisplayHeader = activity.findViewById(R.id.clock_sectionDisplayHeader);
        SectionHelper.setupCollapsible(sectionDisplayHeader, sectionDisplay);

        View sectionPosition = activity.findViewById(R.id.clock_sectionPosition);
        TextView sectionPositionHeader = activity.findViewById(R.id.clock_sectionPositionHeader);
        SectionHelper.setupCollapsible(sectionPositionHeader, sectionPosition);

        View sectionShadow = activity.findViewById(R.id.clock_sectionShadow);
        TextView sectionShadowHeader = activity.findViewById(R.id.clock_sectionShadowHeader);
        SectionHelper.setupCollapsible(sectionShadowHeader, sectionShadow);

        View sectionBackground = activity.findViewById(R.id.clock_sectionBackground);
        TextView sectionBackgroundHeader = activity.findViewById(R.id.clock_sectionBackgroundHeader);
        SectionHelper.setupCollapsible(sectionBackgroundHeader, sectionBackground);
    }

    private void loadConfig() {
        clockSwitch.setChecked(ClockConfig.enabled);
        activity.applyCheckboxTint(clockSwitch, ClockConfig.enabled);
        clockSizeSeekBar.setProgress((int) ClockConfig.size);
        clockBgSwitch.setChecked(ClockConfig.bg.enabled);
        activity.applyCheckboxTint(clockBgSwitch, ClockConfig.bg.enabled);
        clockBgConfigContainer.setVisibility(ClockConfig.bg.enabled ? View.VISIBLE : View.GONE);
        clockBgPaddingSeekBar.setProgress(ClockConfig.bg.padding);
        clockBgOffsetXSeekBar.setProgress(ClockConfig.bg.offsetX + 60);
        clockBgOffsetYSeekBar.setProgress(ClockConfig.bg.offsetY + 60);
        clockBgMarginSeekBar.setProgress(ClockConfig.bg.margin);
        clockBgRadiusSeekBar.setProgress(ClockConfig.bg.radius);
        clockShadowSwitch.setChecked(ClockConfig.shadow.enabled);
        activity.applyCheckboxTint(clockShadowSwitch, ClockConfig.shadow.enabled);
        clockShadowConfigContainer.setVisibility(ClockConfig.shadow.enabled ? View.VISIBLE : View.GONE);
        clockShadowBlurSeekBar.setProgress((int) ClockConfig.shadow.blur);
        clockShadowOffsetXSeekBar.setProgress((int) ClockConfig.shadow.offsetX + 60);
        clockShadowOffsetYSeekBar.setProgress((int) ClockConfig.shadow.offsetY + 60);
        clockLockSwitch.setChecked(ClockConfig.touchPassthrough);
        activity.applyCheckboxTint(clockLockSwitch, ClockConfig.touchPassthrough);
        clockSafeArea.setChecked(ClockConfig.safeArea);
        clockSizeLabel.setText("Ukuran Teks: " + (int) ClockConfig.size);
        clockColorPreview.setBackgroundColor(ClockConfig.color);
        clockBgPaddingLabel.setText("Ukuran Background: " + ClockConfig.bg.padding);
        clockBgOffsetXLabel.setText("Offset X: " + ClockConfig.bg.offsetX);
        clockBgOffsetYLabel.setText("Offset Y: " + ClockConfig.bg.offsetY);
        clockBgColorPreview.setBackgroundColor(ClockConfig.bg.color);
        clockBgMarginLabel.setText("Margin: " + ClockConfig.bg.margin);
        clockBgRadiusLabel.setText("Radius: " + ClockConfig.bg.radius);
        clockShadowBlurLabel.setText("Blur Shadow: " + (int) ClockConfig.shadow.blur);
        clockShadowOffsetXLabel.setText("Shadow X: " + (int) ClockConfig.shadow.offsetX);
        clockShadowOffsetYLabel.setText("Shadow Y: " + (int) ClockConfig.shadow.offsetY);
        clockShadowColorPreview.setBackgroundColor(ClockConfig.shadow.color);
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
                    FloatingService.startModule(FloatingService.clockModule());
                } else {
                    activity.startService(new Intent(activity, FloatingService.class));
                }
            } else {
                FloatingService.stopModule(FloatingService.clockModule());
                if (!activity.isAnyModuleActive()) {
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
                FloatingService.updateSizeForModule(FloatingService.clockModule(), ClockConfig.size);
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        clockColorPreview.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Pilih Warna Jam", ClockConfig.color, color -> {
                ClockConfig.color = color;
                clockColorPreview.setBackgroundColor(color);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("clock_color", color).apply();
                FloatingService.updateColorForModule(FloatingService.clockModule(), ClockConfig.color);
            });
        });

        clockBgSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ClockConfig.bg.enabled = isChecked;
            activity.applyCheckboxTint(clockBgSwitch, isChecked);
            clockBgConfigContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("clock_bg_enabled", isChecked).apply();
            FloatingService.updateBackgroundForModule(FloatingService.clockModule());
        });

        clockBgColorPreview.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Warna Background Jam", ClockConfig.bg.color, color -> {
                ClockConfig.bg.color = color;
                clockBgColorPreview.setBackgroundColor(color);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("clock_bg_color", color).apply();
                FloatingService.updateBackgroundForModule(FloatingService.clockModule());
            });
        });

        clockBgPaddingSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (progress < 0) progress = 0;
                ClockConfig.bg.padding = progress;
                clockBgPaddingLabel.setText("Ukuran Background: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("clock_bg_padding", progress).apply();
                FloatingService.updateBackgroundForModule(FloatingService.clockModule());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        clockBgOffsetXSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                ClockConfig.bg.offsetX = offset;
                clockBgOffsetXLabel.setText("Offset X: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("clock_bg_offset_x", offset).apply();
                FloatingService.updateBackgroundForModule(FloatingService.clockModule());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        clockBgOffsetYSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                ClockConfig.bg.offsetY = offset;
                clockBgOffsetYLabel.setText("Offset Y: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("clock_bg_offset_y", offset).apply();
                FloatingService.updateBackgroundForModule(FloatingService.clockModule());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        clockBgMarginSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                ClockConfig.bg.margin = progress;
                clockBgMarginLabel.setText("Margin: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("clock_bg_margin", progress).apply();
                FloatingService.updateBackgroundForModule(FloatingService.clockModule());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        clockBgRadiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                ClockConfig.bg.radius = progress;
                clockBgRadiusLabel.setText("Radius: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("clock_bg_radius", progress).apply();
                FloatingService.updateBackgroundForModule(FloatingService.clockModule());
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
            FloatingService.updateShadowForModule(FloatingService.clockModule());
        });

        clockShadowColorPreview.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Warna Shadow Jam", ClockConfig.shadow.color, color -> {
                ClockConfig.shadow.color = color;
                clockShadowColorPreview.setBackgroundColor(color);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("clock_shadow_color", color).apply();
                FloatingService.updateShadowForModule(FloatingService.clockModule());
            });
        });

        clockShadowBlurSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                ClockConfig.shadow.blur = progress;
                clockShadowBlurLabel.setText("Blur Shadow: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putFloat("clock_shadow_blur", (float) progress).apply();
                FloatingService.updateShadowForModule(FloatingService.clockModule());
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
                FloatingService.updateShadowForModule(FloatingService.clockModule());
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
                FloatingService.updateShadowForModule(FloatingService.clockModule());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        clockLockSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ClockConfig.touchPassthrough = isChecked;
            activity.applyCheckboxTint(clockLockSwitch, isChecked);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("clock_lock", isChecked).apply();
            FloatingService.updateTouchFlagsForModule(FloatingService.clockModule());
        });

        clockSafeArea.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ClockConfig.safeArea = isChecked;
            activity.applyCheckboxTint(clockSafeArea, isChecked);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("clock_safe_area", isChecked).apply();
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
