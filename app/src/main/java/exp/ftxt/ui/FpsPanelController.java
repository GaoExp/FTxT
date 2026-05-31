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
import exp.ftxt.features.fps_display.FpsConfig;
import exp.ftxt.shared.ui.ColorPickerDialog;
import exp.ftxt.shared.ui.SliderLabelEditor;
import exp.ftxt.utils.PermissionHelper;

public class FpsPanelController {

    private final MainActivity activity;

    private CheckBox fpsSwitch;
    private SeekBar fpsSizeSeekBar;
    private Button fpsColorButton;
    private CheckBox fpsShadowSwitch;
    private LinearLayout fpsShadowConfigContainer;
    private Button fpsShadowColorButton;
    private SeekBar fpsShadowBlurSeekBar;
    private SeekBar fpsShadowOffsetXSeekBar;
    private SeekBar fpsShadowOffsetYSeekBar;
    private CheckBox fpsLockSwitch;
    private CheckBox fpsValueOnlyCheck;
    private CheckBox fpsBgSwitch;
    private LinearLayout fpsBgConfigContainer;
    private Button fpsBgColorButton;
    private SeekBar fpsBgPaddingSeekBar;
    private SeekBar fpsBgOffsetXSeekBar;
    private SeekBar fpsBgOffsetYSeekBar;
    private SeekBar fpsBgMarginSeekBar;
    private SeekBar fpsBgRadiusSeekBar;
    private TextView fpsSizeLabel, fpsBgPaddingLabel, fpsBgOffsetXLabel, fpsBgOffsetYLabel;
    private TextView fpsBgMarginLabel, fpsBgRadiusLabel;
    private TextView fpsShadowBlurLabel, fpsShadowOffsetXLabel, fpsShadowOffsetYLabel;
    private FpsPositionController fpsPositionController;

    public FpsPanelController(MainActivity activity) {
        this.activity = activity;
        bindViews();
        loadConfig();
        setupListeners();
        fpsPositionController = new FpsPositionController(activity);
    }

    public void onPanelShown() {
        if (fpsPositionController != null) {
            fpsPositionController.refresh();
        }
    }

    public void showLoadPresetDialog() {
        if (fpsPositionController != null) {
            fpsPositionController.showLoadPresetDialog();
        }
    }

    public void cleanup() {
        if (fpsPositionController != null) {
            fpsPositionController.cleanup();
            fpsPositionController = null;
        }
    }

    private void bindViews() {
        fpsSwitch = activity.findViewById(R.id.fpsSwitch);
        fpsSizeSeekBar = activity.findViewById(R.id.fpsSizeSeekBar);
        fpsColorButton = activity.findViewById(R.id.fpsColorButton);
        fpsShadowSwitch = activity.findViewById(R.id.fpsShadowSwitch);
        fpsShadowConfigContainer = activity.findViewById(R.id.shadowConfigFps);
        fpsShadowColorButton = activity.findViewById(R.id.fpsShadowColorButton);
        fpsShadowBlurSeekBar = activity.findViewById(R.id.fpsShadowBlurSeekBar);
        fpsShadowOffsetXSeekBar = activity.findViewById(R.id.fpsShadowOffsetXSeekBar);
        fpsShadowOffsetYSeekBar = activity.findViewById(R.id.fpsShadowOffsetYSeekBar);
        fpsLockSwitch = activity.findViewById(R.id.fpsLockSwitch);
        fpsValueOnlyCheck = activity.findViewById(R.id.fpsValueOnlyCheck);
        fpsBgSwitch = activity.findViewById(R.id.fpsBgSwitch);
        fpsBgConfigContainer = activity.findViewById(R.id.bgConfigFps);
        fpsBgColorButton = activity.findViewById(R.id.fpsBgColorButton);
        fpsBgPaddingSeekBar = activity.findViewById(R.id.fpsBgPaddingSeekBar);
        fpsBgOffsetXSeekBar = activity.findViewById(R.id.fpsBgOffsetXSeekBar);
        fpsBgOffsetYSeekBar = activity.findViewById(R.id.fpsBgOffsetYSeekBar);
        fpsBgMarginSeekBar = activity.findViewById(R.id.fpsBgMarginSeekBar);
        fpsBgRadiusSeekBar = activity.findViewById(R.id.fpsBgRadiusSeekBar);
        fpsSizeLabel = activity.findViewById(R.id.fpsSizeLabel);
        fpsBgPaddingLabel = activity.findViewById(R.id.fpsBgPaddingLabel);
        fpsBgOffsetXLabel = activity.findViewById(R.id.fpsBgOffsetXLabel);
        fpsBgOffsetYLabel = activity.findViewById(R.id.fpsBgOffsetYLabel);
        fpsBgMarginLabel = activity.findViewById(R.id.fpsBgMarginLabel);
        fpsBgRadiusLabel = activity.findViewById(R.id.fpsBgRadiusLabel);
        fpsShadowBlurLabel = activity.findViewById(R.id.fpsShadowBlurLabel);
        fpsShadowOffsetXLabel = activity.findViewById(R.id.fpsShadowOffsetXLabel);
        fpsShadowOffsetYLabel = activity.findViewById(R.id.fpsShadowOffsetYLabel);
    }

    private void loadConfig() {
        fpsSwitch.setChecked(FpsConfig.enabled);
        activity.applyCheckboxTint(fpsSwitch, FpsConfig.enabled);
        fpsSizeSeekBar.setProgress((int) FpsConfig.size);
        fpsBgSwitch.setChecked(FpsConfig.bgEnabled);
        activity.applyCheckboxTint(fpsBgSwitch, FpsConfig.bgEnabled);
        fpsBgConfigContainer.setVisibility(FpsConfig.bgEnabled ? View.VISIBLE : View.GONE);
        fpsBgPaddingSeekBar.setProgress(FpsConfig.bgPadding);
        fpsBgOffsetXSeekBar.setProgress(FpsConfig.bgOffsetX + 60);
        fpsBgOffsetYSeekBar.setProgress(FpsConfig.bgOffsetY + 60);
        fpsBgMarginSeekBar.setProgress(FpsConfig.bgMargin);
        fpsBgRadiusSeekBar.setProgress(FpsConfig.bgRadius);
        fpsShadowSwitch.setChecked(FpsConfig.shadow.enabled);
        activity.applyCheckboxTint(fpsShadowSwitch, FpsConfig.shadow.enabled);
        fpsShadowConfigContainer.setVisibility(FpsConfig.shadow.enabled ? View.VISIBLE : View.GONE);
        fpsShadowBlurSeekBar.setProgress((int) FpsConfig.shadow.blur);
        fpsShadowOffsetXSeekBar.setProgress((int) FpsConfig.shadow.offsetX + 60);
        fpsShadowOffsetYSeekBar.setProgress((int) FpsConfig.shadow.offsetY + 60);
        fpsLockSwitch.setChecked(FpsConfig.touchPassthrough);
        activity.applyCheckboxTint(fpsLockSwitch, FpsConfig.touchPassthrough);
        fpsValueOnlyCheck.setChecked(FpsConfig.showOnlyValue);
        fpsSizeLabel.setText("Ukuran Teks: " + (int) FpsConfig.size);
        fpsBgPaddingLabel.setText("Ukuran Background: " + FpsConfig.bgPadding);
        fpsBgOffsetXLabel.setText("Offset X: " + FpsConfig.bgOffsetX);
        fpsBgOffsetYLabel.setText("Offset Y: " + FpsConfig.bgOffsetY);
        fpsBgMarginLabel.setText("Margin: " + FpsConfig.bgMargin);
        fpsBgRadiusLabel.setText("Radius: " + FpsConfig.bgRadius);
        fpsShadowBlurLabel.setText("Blur Shadow: " + (int) FpsConfig.shadow.blur);
        fpsShadowOffsetXLabel.setText("Shadow X: " + (int) FpsConfig.shadow.offsetX);
        fpsShadowOffsetYLabel.setText("Shadow Y: " + (int) FpsConfig.shadow.offsetY);
    }

    private void setupListeners() {
        fpsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !PermissionHelper.hasOverlayPermission(activity)) {
                fpsSwitch.setChecked(false);
                activity.applyCheckboxTint(fpsSwitch, false);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putBoolean("fps_enabled", false).apply();
                return;
            }

            FpsConfig.enabled = isChecked;
            activity.applyCheckboxTint(fpsSwitch, isChecked);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("fps_enabled", isChecked).apply();

            if (isChecked) {
                if (FloatingService.instance != null) {
                    FloatingService.startFpsStatic();
                } else {
                    activity.startService(new Intent(activity, FloatingService.class));
                }
            } else {
                FloatingService.stopFpsStatic();
                if (!activity.isTextOverlayOn()) {
                    activity.stopService(new Intent(activity, FloatingService.class));
                }
            }
        });

        fpsSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (progress < 5) { progress = 5; sb.setProgress(progress); }
                if (progress > 140) { progress = 140; sb.setProgress(progress); }
                FpsConfig.size = progress;
                fpsSizeLabel.setText("Ukuran Teks: " + progress);
                FloatingService.updateFpsSizeStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        fpsColorButton.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Pilih Warna FPS", FpsConfig.color, color -> {
                FpsConfig.color = color;
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("fps_color", color).apply();
                FloatingService.updateFpsColorStatic();
            });
        });

        fpsBgSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            FpsConfig.bgEnabled = isChecked;
            activity.applyCheckboxTint(fpsBgSwitch, isChecked);
            fpsBgConfigContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("fps_bg_enabled", isChecked).apply();
            FloatingService.updateFpsBackgroundStatic();
        });

        fpsBgColorButton.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Warna Background FPS", FpsConfig.bgColor, color -> {
                FpsConfig.bgColor = color;
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("fps_bg_color", color).apply();
                FloatingService.updateFpsBackgroundStatic();
            });
        });

        fpsBgPaddingSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (progress < 0) progress = 0;
                FpsConfig.bgPadding = progress;
                fpsBgPaddingLabel.setText("Ukuran Background: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("fps_bg_padding", progress).apply();
                FloatingService.updateFpsBackgroundStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        fpsBgOffsetXSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                FpsConfig.bgOffsetX = offset;
                fpsBgOffsetXLabel.setText("Offset X: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("fps_bg_offset_x", offset).apply();
                FloatingService.updateFpsBackgroundStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        fpsBgOffsetYSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                FpsConfig.bgOffsetY = offset;
                fpsBgOffsetYLabel.setText("Offset Y: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("fps_bg_offset_y", offset).apply();
                FloatingService.updateFpsBackgroundStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        fpsBgMarginSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                FpsConfig.bgMargin = progress;
                fpsBgMarginLabel.setText("Margin: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("fps_bg_margin", progress).apply();
                FloatingService.updateFpsBackgroundStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        fpsBgRadiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                FpsConfig.bgRadius = progress;
                fpsBgRadiusLabel.setText("Radius: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("fps_bg_radius", progress).apply();
                FloatingService.updateFpsBackgroundStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        fpsShadowSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            FpsConfig.shadow.enabled = isChecked;
            activity.applyCheckboxTint(fpsShadowSwitch, isChecked);
            fpsShadowConfigContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("fps_shadow_enabled", isChecked).apply();
            saveFpsShadowPrefs();
            FloatingService.updateFpsShadowStatic();
        });

        fpsShadowColorButton.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Warna Shadow FPS", FpsConfig.shadow.color, color -> {
                FpsConfig.shadow.color = color;
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("fps_shadow_color", color).apply();
                FloatingService.updateFpsShadowStatic();
            });
        });

        fpsShadowBlurSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                FpsConfig.shadow.blur = progress;
                fpsShadowBlurLabel.setText("Blur Shadow: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putFloat("fps_shadow_blur", (float) progress).apply();
                FloatingService.updateFpsShadowStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        fpsShadowOffsetXSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                FpsConfig.shadow.offsetX = offset;
                fpsShadowOffsetXLabel.setText("Shadow X: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putFloat("fps_shadow_offset_x", (float) offset).apply();
                FloatingService.updateFpsShadowStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        fpsShadowOffsetYSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                FpsConfig.shadow.offsetY = offset;
                fpsShadowOffsetYLabel.setText("Shadow Y: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putFloat("fps_shadow_offset_y", (float) offset).apply();
                FloatingService.updateFpsShadowStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        fpsLockSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            FpsConfig.touchPassthrough = isChecked;
            activity.applyCheckboxTint(fpsLockSwitch, isChecked);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("fps_lock", isChecked).apply();
            FloatingService.updateFpsTouchFlagsStatic();
        });

        fpsValueOnlyCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            FpsConfig.showOnlyValue = isChecked;
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("fps_show_only_value", isChecked).apply();
            FloatingService.updateFpsDisplayStatic();
        });

        fpsSizeLabel.setOnClickListener(v ->
                SliderLabelEditor.showSliderEditor(activity, "Ukuran Teks", fpsSizeSeekBar, 140, fpsSizeLabel, "Ukuran Teks: "));
        fpsBgPaddingLabel.setOnClickListener(v ->
                SliderLabelEditor.showSliderEditor(activity, "Ukuran Background", fpsBgPaddingSeekBar, 80, fpsBgPaddingLabel, "Ukuran Background: "));
        fpsBgOffsetXLabel.setOnClickListener(v ->
                SliderLabelEditor.showOffsetEditor(activity, "Offset X", fpsBgOffsetXSeekBar, fpsBgOffsetXLabel, "Offset X: "));
        fpsBgOffsetYLabel.setOnClickListener(v ->
                SliderLabelEditor.showOffsetEditor(activity, "Offset Y", fpsBgOffsetYSeekBar, fpsBgOffsetYLabel, "Offset Y: "));
        fpsBgMarginLabel.setOnClickListener(v ->
                SliderLabelEditor.showSliderEditor(activity, "Margin Background", fpsBgMarginSeekBar, 30, fpsBgMarginLabel, "Margin: "));
        fpsBgRadiusLabel.setOnClickListener(v ->
                SliderLabelEditor.showSliderEditor(activity, "Radius Background", fpsBgRadiusSeekBar, 50, fpsBgRadiusLabel, "Radius: "));
        fpsShadowBlurLabel.setOnClickListener(v ->
                SliderLabelEditor.showSliderEditor(activity, "Blur Shadow", fpsShadowBlurSeekBar, 50, fpsShadowBlurLabel, "Blur Shadow: "));
        fpsShadowOffsetXLabel.setOnClickListener(v ->
                SliderLabelEditor.showOffsetEditor(activity, "Shadow X", fpsShadowOffsetXSeekBar, fpsShadowOffsetXLabel, "Shadow X: "));
        fpsShadowOffsetYLabel.setOnClickListener(v ->
                SliderLabelEditor.showOffsetEditor(activity, "Shadow Y", fpsShadowOffsetYSeekBar, fpsShadowOffsetYLabel, "Shadow Y: "));
    }

    private void saveFpsShadowPrefs() {
        activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE).edit()
                .putInt("fps_shadow_color", FpsConfig.shadow.color)
                .putFloat("fps_shadow_blur", FpsConfig.shadow.blur)
                .putFloat("fps_shadow_offset_x", FpsConfig.shadow.offsetX)
                .putFloat("fps_shadow_offset_y", FpsConfig.shadow.offsetY)
                .apply();
    }
}
