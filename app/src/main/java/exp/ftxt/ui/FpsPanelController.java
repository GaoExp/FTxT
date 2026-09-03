package exp.ftxt.ui;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import exp.ftxt.MainActivity;
import exp.ftxt.R;
import exp.ftxt.core.FloatingService;
import exp.ftxt.features.fps_display.FpsConfig;
import exp.ftxt.shared.ui.ColorPickerDialog;
import exp.ftxt.shared.ui.SliderLabelEditor;
import exp.ftxt.shared.ui.SectionHelper;
import exp.ftxt.utils.PermissionHelper;

public class FpsPanelController {

    private final MainActivity activity;

    private CheckBox fpsSwitch;
    private SeekBar fpsSizeSeekBar;
    private View fpsColorPreview;
    private View fpsLabelColorPreview;
    private CheckBox fpsShadowSwitch;
    private LinearLayout fpsShadowConfigContainer;
    private View fpsShadowColorPreview;
    private SeekBar fpsShadowBlurSeekBar;
    private SeekBar fpsShadowOffsetXSeekBar;
    private SeekBar fpsShadowOffsetYSeekBar;
    private CheckBox fpsLockSwitch;
    private CheckBox fpsValueOnlyCheck;
    private CheckBox fpsSafeArea;
    private CheckBox fpsBgSwitch;
    private LinearLayout fpsBgConfigContainer;
    private View fpsBgColorPreview;
    private SeekBar fpsBgPaddingSeekBar;
    private SeekBar fpsBgOffsetXSeekBar;
    private SeekBar fpsBgOffsetYSeekBar;
    private SeekBar fpsBgMarginSeekBar;
    private SeekBar fpsBgRadiusSeekBar;
    private TextView fpsSizeLabel, fpsBgPaddingLabel, fpsBgOffsetXLabel, fpsBgOffsetYLabel;
    private TextView fpsBgMarginLabel, fpsBgRadiusLabel;
    private TextView fpsShadowBlurLabel, fpsShadowOffsetXLabel, fpsShadowOffsetYLabel;
    private FpsPositionController fpsPositionController;
    private TextView fpsIntervalValue;
    private PopupWindow intervalPopup;

    public FpsPanelController(MainActivity activity, View rootView) {
        this.activity = activity;
        bindViews(rootView);
        loadConfig();
        setupListeners();
        fpsPositionController = new FpsPositionController(activity, rootView);
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

    private void bindViews(View rootView) {
        fpsSwitch = rootView.findViewById(R.id.fpsSwitch);
        fpsSizeSeekBar = rootView.findViewById(R.id.fpsSizeSeekBar);
        fpsColorPreview = rootView.findViewById(R.id.fpsColorPreview);
        fpsLabelColorPreview = rootView.findViewById(R.id.fpsLabelColorPreview);
        fpsShadowSwitch = rootView.findViewById(R.id.fpsShadowSwitch);
        fpsShadowConfigContainer = rootView.findViewById(R.id.shadowConfigFps);
        fpsShadowColorPreview = rootView.findViewById(R.id.fpsShadowColorPreview);
        fpsShadowBlurSeekBar = rootView.findViewById(R.id.fpsShadowBlurSeekBar);
        fpsShadowOffsetXSeekBar = rootView.findViewById(R.id.fpsShadowOffsetXSeekBar);
        fpsShadowOffsetYSeekBar = rootView.findViewById(R.id.fpsShadowOffsetYSeekBar);
        fpsLockSwitch = rootView.findViewById(R.id.fpsLockSwitch);
        fpsValueOnlyCheck = rootView.findViewById(R.id.fpsValueOnlyCheck);
        fpsSafeArea = rootView.findViewById(R.id.fpsSafeArea);
        fpsBgSwitch = rootView.findViewById(R.id.fpsBgSwitch);
        fpsBgConfigContainer = rootView.findViewById(R.id.bgConfigFps);
        fpsBgColorPreview = rootView.findViewById(R.id.fpsBgColorPreview);
        fpsBgPaddingSeekBar = rootView.findViewById(R.id.fpsBgPaddingSeekBar);
        fpsBgOffsetXSeekBar = rootView.findViewById(R.id.fpsBgOffsetXSeekBar);
        fpsBgOffsetYSeekBar = rootView.findViewById(R.id.fpsBgOffsetYSeekBar);
        fpsBgMarginSeekBar = rootView.findViewById(R.id.fpsBgMarginSeekBar);
        fpsBgRadiusSeekBar = rootView.findViewById(R.id.fpsBgRadiusSeekBar);
        fpsSizeLabel = rootView.findViewById(R.id.fpsSizeLabel);
        fpsBgPaddingLabel = rootView.findViewById(R.id.fpsBgPaddingLabel);
        fpsBgOffsetXLabel = rootView.findViewById(R.id.fpsBgOffsetXLabel);
        fpsBgOffsetYLabel = rootView.findViewById(R.id.fpsBgOffsetYLabel);
        fpsBgMarginLabel = rootView.findViewById(R.id.fpsBgMarginLabel);
        fpsBgRadiusLabel = rootView.findViewById(R.id.fpsBgRadiusLabel);
        fpsShadowBlurLabel = rootView.findViewById(R.id.fpsShadowBlurLabel);
        fpsShadowOffsetXLabel = rootView.findViewById(R.id.fpsShadowOffsetXLabel);
        fpsShadowOffsetYLabel = rootView.findViewById(R.id.fpsShadowOffsetYLabel);
        fpsIntervalValue = rootView.findViewById(R.id.fpsIntervalValue);

        View sectionDisplay = rootView.findViewById(R.id.fps_sectionDisplay);
        TextView sectionDisplayHeader = rootView.findViewById(R.id.fps_sectionDisplayHeader);
        SectionHelper.setupCollapsible(sectionDisplayHeader, sectionDisplay);

        View sectionPosition = rootView.findViewById(R.id.fps_sectionPosition);
        TextView sectionPositionHeader = rootView.findViewById(R.id.fps_sectionPositionHeader);
        SectionHelper.setupCollapsible(sectionPositionHeader, sectionPosition);

        View sectionShadow = rootView.findViewById(R.id.fps_sectionShadow);
        TextView sectionShadowHeader = rootView.findViewById(R.id.fps_sectionShadowHeader);
        SectionHelper.setupCollapsible(sectionShadowHeader, sectionShadow);

        View sectionBackground = rootView.findViewById(R.id.fps_sectionBackground);
        TextView sectionBackgroundHeader = rootView.findViewById(R.id.fps_sectionBackgroundHeader);
        SectionHelper.setupCollapsible(sectionBackgroundHeader, sectionBackground);
    }

    private void loadConfig() {
        fpsSwitch.setChecked(FpsConfig.enabled);
        activity.applyCheckboxTint(fpsSwitch, FpsConfig.enabled);
        fpsSizeSeekBar.setProgress((int) FpsConfig.size);
        fpsColorPreview.setBackgroundColor(FpsConfig.color);
        fpsLabelColorPreview.setBackgroundColor(FpsConfig.labelColor);
        fpsBgSwitch.setChecked(FpsConfig.bg.enabled);
        activity.applyCheckboxTint(fpsBgSwitch, FpsConfig.bg.enabled);
        fpsBgConfigContainer.setVisibility(FpsConfig.bg.enabled ? View.VISIBLE : View.GONE);
        fpsBgPaddingSeekBar.setProgress(FpsConfig.bg.padding);
        fpsBgOffsetXSeekBar.setProgress(FpsConfig.bg.offsetX + 60);
        fpsBgOffsetYSeekBar.setProgress(FpsConfig.bg.offsetY + 60);
        fpsBgMarginSeekBar.setProgress(FpsConfig.bg.margin);
        fpsBgRadiusSeekBar.setProgress(FpsConfig.bg.radius);
        fpsShadowSwitch.setChecked(FpsConfig.shadow.enabled);
        activity.applyCheckboxTint(fpsShadowSwitch, FpsConfig.shadow.enabled);
        fpsShadowConfigContainer.setVisibility(FpsConfig.shadow.enabled ? View.VISIBLE : View.GONE);
        fpsShadowBlurSeekBar.setProgress((int) FpsConfig.shadow.blur);
        fpsShadowOffsetXSeekBar.setProgress((int) FpsConfig.shadow.offsetX + 60);
        fpsShadowOffsetYSeekBar.setProgress((int) FpsConfig.shadow.offsetY + 60);
        fpsLockSwitch.setChecked(FpsConfig.touchPassthrough);
        activity.applyCheckboxTint(fpsLockSwitch, FpsConfig.touchPassthrough);
        fpsValueOnlyCheck.setChecked(FpsConfig.showOnlyValue);
        fpsSafeArea.setChecked(FpsConfig.safeArea);
        fpsSizeLabel.setText("Ukuran Teks: " + (int) FpsConfig.size);
        fpsBgPaddingLabel.setText("Ukuran Background: " + FpsConfig.bg.padding);
        fpsBgOffsetXLabel.setText("Offset X: " + FpsConfig.bg.offsetX);
        fpsBgOffsetYLabel.setText("Offset Y: " + FpsConfig.bg.offsetY);
        fpsBgColorPreview.setBackgroundColor(FpsConfig.bg.color);
        fpsBgMarginLabel.setText("Margin: " + FpsConfig.bg.margin);
        fpsBgRadiusLabel.setText("Radius: " + FpsConfig.bg.radius);
        fpsShadowBlurLabel.setText("Blur Shadow: " + (int) FpsConfig.shadow.blur);
        fpsShadowOffsetXLabel.setText("Shadow X: " + (int) FpsConfig.shadow.offsetX);
        fpsShadowOffsetYLabel.setText("Shadow Y: " + (int) FpsConfig.shadow.offsetY);
        fpsShadowColorPreview.setBackgroundColor(FpsConfig.shadow.color);
        fpsIntervalValue.setText(formatIntervalValue(FpsConfig.updateInterval));
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
                    FloatingService.startModule(FloatingService.fpsModule());
                } else {
                    activity.startService(new Intent(activity, FloatingService.class));
                }
            } else {
                FloatingService.stopModule(FloatingService.fpsModule());
            }
        });

        fpsSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (progress < 5) { progress = 5; sb.setProgress(progress); }
                if (progress > 140) { progress = 140; sb.setProgress(progress); }
                FpsConfig.size = progress;
                fpsSizeLabel.setText("Ukuran Teks: " + progress);
                FloatingService.updateSizeForModule(FloatingService.fpsModule(), FpsConfig.size);
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        fpsColorPreview.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Pilih Warna FPS", FpsConfig.color, color -> {
                FpsConfig.color = color;
                fpsColorPreview.setBackgroundColor(color);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("fps_color", color).apply();
                FloatingService.updateColorForModule(FloatingService.fpsModule(), FpsConfig.color);
            });
        });

        fpsLabelColorPreview.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Pilih Warna Label", FpsConfig.labelColor, color -> {
                FpsConfig.labelColor = color;
                fpsLabelColorPreview.setBackgroundColor(color);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("fps_label_color", color).apply();
                FloatingService.updateLabelColorForModule(FloatingService.fpsModule(), FpsConfig.labelColor);
            });
        });

        fpsBgSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            FpsConfig.bg.enabled = isChecked;
            activity.applyCheckboxTint(fpsBgSwitch, isChecked);
            fpsBgConfigContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("fps_bg_enabled", isChecked).apply();
            FloatingService.updateBackgroundForModule(FloatingService.fpsModule());
        });

        fpsBgColorPreview.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Warna Background FPS", FpsConfig.bg.color, color -> {
                FpsConfig.bg.color = color;
                fpsBgColorPreview.setBackgroundColor(color);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("fps_bg_color", color).apply();
                FloatingService.updateBackgroundForModule(FloatingService.fpsModule());
            });
        });

        fpsBgPaddingSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (progress < 0) progress = 0;
                FpsConfig.bg.padding = progress;
                fpsBgPaddingLabel.setText("Ukuran Background: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("fps_bg_padding", progress).apply();
                FloatingService.updateBackgroundForModule(FloatingService.fpsModule());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        fpsBgOffsetXSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                FpsConfig.bg.offsetX = offset;
                fpsBgOffsetXLabel.setText("Offset X: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("fps_bg_offset_x", offset).apply();
                FloatingService.updateBackgroundForModule(FloatingService.fpsModule());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        fpsBgOffsetYSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                FpsConfig.bg.offsetY = offset;
                fpsBgOffsetYLabel.setText("Offset Y: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("fps_bg_offset_y", offset).apply();
                FloatingService.updateBackgroundForModule(FloatingService.fpsModule());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        fpsBgMarginSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                FpsConfig.bg.margin = progress;
                fpsBgMarginLabel.setText("Margin: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("fps_bg_margin", progress).apply();
                FloatingService.updateBackgroundForModule(FloatingService.fpsModule());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        fpsBgRadiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                FpsConfig.bg.radius = progress;
                fpsBgRadiusLabel.setText("Radius: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("fps_bg_radius", progress).apply();
                FloatingService.updateBackgroundForModule(FloatingService.fpsModule());
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
            FloatingService.updateShadowForModule(FloatingService.fpsModule());
        });

        fpsShadowColorPreview.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Warna Shadow FPS", FpsConfig.shadow.color, color -> {
                FpsConfig.shadow.color = color;
                fpsShadowColorPreview.setBackgroundColor(color);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("fps_shadow_color", color).apply();
                FloatingService.updateShadowForModule(FloatingService.fpsModule());
            });
        });

        fpsShadowBlurSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                FpsConfig.shadow.blur = progress;
                fpsShadowBlurLabel.setText("Blur Shadow: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putFloat("fps_shadow_blur", (float) progress).apply();
                FloatingService.updateShadowForModule(FloatingService.fpsModule());
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
                FloatingService.updateShadowForModule(FloatingService.fpsModule());
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
                FloatingService.updateShadowForModule(FloatingService.fpsModule());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        fpsLockSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            FpsConfig.touchPassthrough = isChecked;
            activity.applyCheckboxTint(fpsLockSwitch, isChecked);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("fps_lock", isChecked).apply();
            FloatingService.updateTouchFlagsForModule(FloatingService.fpsModule());
        });

        fpsValueOnlyCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            FpsConfig.showOnlyValue = isChecked;
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("fps_show_only_value", isChecked).apply();
            FloatingService.fpsModule().updateDisplay();
        });

        fpsSafeArea.setOnCheckedChangeListener((buttonView, isChecked) -> {
            FpsConfig.safeArea = isChecked;
            activity.applyCheckboxTint(fpsSafeArea, isChecked);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("fps_safe_area", isChecked).apply();
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

        setupIntervalListeners();
    }

    private void saveFpsShadowPrefs() {
        activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE).edit()
                .putInt("fps_shadow_color", FpsConfig.shadow.color)
                .putFloat("fps_shadow_blur", FpsConfig.shadow.blur)
                .putFloat("fps_shadow_offset_x", FpsConfig.shadow.offsetX)
                .putFloat("fps_shadow_offset_y", FpsConfig.shadow.offsetY)
                .apply();
    }

    private static final float[] INTERVAL_STEPS = {0.2f, 0.5f, 0.75f, 1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f};

    private void setupIntervalListeners() {
        fpsIntervalValue.setOnClickListener(v -> showIntervalPopup(v));
    }

    private void showIntervalPopup(View anchor) {
        if (intervalPopup != null && intervalPopup.isShowing()) {
            intervalPopup.dismiss();
            return;
        }

        int currentIdx = -1;
        for (int i = 0; i < INTERVAL_STEPS.length; i++) {
            if (INTERVAL_STEPS[i] == FpsConfig.updateInterval) {
                currentIdx = i;
                break;
            }
        }

        LinearLayout content = new LinearLayout(activity);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setBackgroundColor(0xFFFFFFFF);

        for (int i = 0; i < INTERVAL_STEPS.length; i++) {
            TextView item = new TextView(activity);
            item.setText(formatIntervalValue(INTERVAL_STEPS[i]) + "s");
            item.setPadding(dp(16), dp(10), dp(16), dp(10));
            item.setTextSize(14);
            item.setGravity(Gravity.CENTER_VERTICAL);
            item.setTextColor(0xFF222222);
            item.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            if (i == currentIdx) {
                item.setBackgroundColor(0xFF4A90D9);
                item.setTextColor(0xFFFFFFFF);
            }

            final int idx = i;
            item.setOnClickListener(v -> {
                FpsConfig.updateInterval = INTERVAL_STEPS[idx];
                updateIntervalDisplay();
                FloatingService.restartModule(FloatingService.fpsModule());
                if (intervalPopup != null) intervalPopup.dismiss();
            });
            content.addView(item);
        }

        ScrollView scrollView = new ScrollView(activity);
        scrollView.addView(content);

        intervalPopup = new PopupWindow(scrollView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                dp(200), true);
        intervalPopup.setBackgroundDrawable(new ColorDrawable(0xFFFFFFFF));
        intervalPopup.setOutsideTouchable(true);
        intervalPopup.setElevation(dp(4));
        intervalPopup.showAsDropDown(anchor, 0, dp(2));
    }

    private void updateIntervalDisplay() {
        fpsIntervalValue.setText(formatIntervalValue(FpsConfig.updateInterval));
        activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                .edit().putFloat("fps_update_interval", FpsConfig.updateInterval).apply();
    }

    private String formatIntervalValue(float v) {
        if (v == (long) v) return String.valueOf((long) v);
        String s = String.format("%.2f", v).replaceAll("0$", "").replaceAll("\\.$", "");
        return s;
    }

    private int dp(int dp) {
        return (int) (dp * activity.getResources().getDisplayMetrics().density);
    }
}
