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
import exp.ftxt.features.battery_temperature.BatteryConfig;
import exp.ftxt.shared.ui.ColorPickerDialog;
import exp.ftxt.shared.ui.SectionHelper;
import exp.ftxt.utils.PermissionHelper;

public class BatteryPanelController {

    private final MainActivity activity;

    private CheckBox batterySwitch;
    private SeekBar batterySizeSeekBar;
    private View batteryColorPreview;
    private View batteryLabelColorPreview;
    private CheckBox batteryShadowSwitch;
    private LinearLayout batteryShadowConfigContainer;
    private View batteryShadowColorPreview;
    private SeekBar batteryShadowBlurSeekBar;
    private SeekBar batteryShadowOffsetXSeekBar;
    private SeekBar batteryShadowOffsetYSeekBar;
    private CheckBox batteryLockSwitch;
    private CheckBox batteryValueOnlyCheck;
    private CheckBox batteryShowTempCheck;
    private CheckBox batteryShowPctCheck;
    private CheckBox batterySafeArea;
    private CheckBox batteryBgSwitch;
    private LinearLayout batteryBgConfigContainer;
    private View batteryBgColorPreview;
    private SeekBar batteryBgPaddingSeekBar;
    private SeekBar batteryBgOffsetXSeekBar;
    private SeekBar batteryBgOffsetYSeekBar;
    private SeekBar batteryBgMarginSeekBar;
    private SeekBar batteryBgRadiusSeekBar;
    private TextView batterySizeLabel, batteryBgPaddingLabel, batteryBgOffsetXLabel, batteryBgOffsetYLabel;
    private TextView batteryBgMarginLabel, batteryBgRadiusLabel;
    private TextView batteryShadowBlurLabel, batteryShadowOffsetXLabel, batteryShadowOffsetYLabel;
    private BatteryPositionController batteryPositionController;
    private TextView batteryIntervalValue;
    private PopupWindow intervalPopup;

    public BatteryPanelController(MainActivity activity) {
        this.activity = activity;
        bindViews();
        loadConfig();
        setupListeners();
        batteryPositionController = new BatteryPositionController(activity);
    }

    public BatteryPanelController(MainActivity activity, View rootView) {
        this.activity = activity;
        bindViews(rootView);
        loadConfig();
        setupListeners();
        batteryPositionController = new BatteryPositionController(activity, rootView);
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
        bindViews(activity.findViewById(android.R.id.content));
    }

    private void bindViews(View rootView) {
        batterySwitch = rootView.findViewById(R.id.batterySwitch);
        batterySizeSeekBar = rootView.findViewById(R.id.batterySizeSeekBar);
        batteryColorPreview = rootView.findViewById(R.id.batteryColorPreview);
        batteryLabelColorPreview = rootView.findViewById(R.id.batteryLabelColorPreview);
        batteryShadowSwitch = rootView.findViewById(R.id.batteryShadowSwitch);
        batteryShadowConfigContainer = rootView.findViewById(R.id.shadowConfigBattery);
        batteryShadowColorPreview = rootView.findViewById(R.id.batteryShadowColorPreview);
        batteryShadowBlurSeekBar = rootView.findViewById(R.id.batteryShadowBlurSeekBar);
        batteryShadowOffsetXSeekBar = rootView.findViewById(R.id.batteryShadowOffsetXSeekBar);
        batteryShadowOffsetYSeekBar = rootView.findViewById(R.id.batteryShadowOffsetYSeekBar);
        batteryLockSwitch = rootView.findViewById(R.id.batteryLockSwitch);
        batteryValueOnlyCheck = rootView.findViewById(R.id.batteryValueOnlyCheck);
        batteryShowTempCheck = rootView.findViewById(R.id.batteryShowTempCheck);
        batteryShowPctCheck = rootView.findViewById(R.id.batteryShowPctCheck);
        batterySafeArea = rootView.findViewById(R.id.batterySafeArea);
        batteryBgSwitch = rootView.findViewById(R.id.batteryBgSwitch);
        batteryBgConfigContainer = rootView.findViewById(R.id.bgConfigBattery);
        batteryBgColorPreview = rootView.findViewById(R.id.batteryBgColorPreview);
        batteryBgPaddingSeekBar = rootView.findViewById(R.id.batteryBgPaddingSeekBar);
        batteryBgOffsetXSeekBar = rootView.findViewById(R.id.batteryBgOffsetXSeekBar);
        batteryBgOffsetYSeekBar = rootView.findViewById(R.id.batteryBgOffsetYSeekBar);
        batteryBgMarginSeekBar = rootView.findViewById(R.id.batteryBgMarginSeekBar);
        batteryBgRadiusSeekBar = rootView.findViewById(R.id.batteryBgRadiusSeekBar);
        batterySizeLabel = rootView.findViewById(R.id.batterySizeLabel);
        batteryBgPaddingLabel = rootView.findViewById(R.id.batteryBgPaddingLabel);
        batteryBgOffsetXLabel = rootView.findViewById(R.id.batteryBgOffsetXLabel);
        batteryBgOffsetYLabel = rootView.findViewById(R.id.batteryBgOffsetYLabel);
        batteryBgMarginLabel = rootView.findViewById(R.id.batteryBgMarginLabel);
        batteryBgRadiusLabel = rootView.findViewById(R.id.batteryBgRadiusLabel);
        batteryShadowBlurLabel = rootView.findViewById(R.id.batteryShadowBlurLabel);
        batteryShadowOffsetXLabel = rootView.findViewById(R.id.batteryShadowOffsetXLabel);
        batteryShadowOffsetYLabel = rootView.findViewById(R.id.batteryShadowOffsetYLabel);
        batteryIntervalValue = rootView.findViewById(R.id.batteryIntervalValue);

        View sectionDisplay = rootView.findViewById(R.id.battery_sectionDisplay);
        TextView sectionDisplayHeader = rootView.findViewById(R.id.battery_sectionDisplayHeader);
        SectionHelper.setupCollapsible(sectionDisplayHeader, sectionDisplay);

        View sectionPosition = rootView.findViewById(R.id.battery_sectionPosition);
        TextView sectionPositionHeader = rootView.findViewById(R.id.battery_sectionPositionHeader);
        SectionHelper.setupCollapsible(sectionPositionHeader, sectionPosition);

        View sectionShadow = rootView.findViewById(R.id.battery_sectionShadow);
        TextView sectionShadowHeader = rootView.findViewById(R.id.battery_sectionShadowHeader);
        SectionHelper.setupCollapsible(sectionShadowHeader, sectionShadow);

        View sectionBackground = rootView.findViewById(R.id.battery_sectionBackground);
        TextView sectionBackgroundHeader = rootView.findViewById(R.id.battery_sectionBackgroundHeader);
        SectionHelper.setupCollapsible(sectionBackgroundHeader, sectionBackground);
    }

    private void loadConfig() {
        batterySwitch.setChecked(BatteryConfig.enabled);
        activity.applyCheckboxTint(batterySwitch, BatteryConfig.enabled);
        batterySizeSeekBar.setProgress((int) BatteryConfig.size);
        batteryColorPreview.setBackgroundColor(BatteryConfig.color);
        batteryLabelColorPreview.setBackgroundColor(BatteryConfig.labelColor);
        batteryBgSwitch.setChecked(BatteryConfig.bg.enabled);
        activity.applyCheckboxTint(batteryBgSwitch, BatteryConfig.bg.enabled);
        batteryBgConfigContainer.setVisibility(BatteryConfig.bg.enabled ? View.VISIBLE : View.GONE);
        batteryBgPaddingSeekBar.setProgress(BatteryConfig.bg.padding);
        batteryBgOffsetXSeekBar.setProgress(BatteryConfig.bg.offsetX + 60);
        batteryBgOffsetYSeekBar.setProgress(BatteryConfig.bg.offsetY + 60);
        batteryBgMarginSeekBar.setProgress(BatteryConfig.bg.margin);
        batteryBgRadiusSeekBar.setProgress(BatteryConfig.bg.radius);
        batteryShadowSwitch.setChecked(BatteryConfig.shadow.enabled);
        activity.applyCheckboxTint(batteryShadowSwitch, BatteryConfig.shadow.enabled);
        batteryShadowConfigContainer.setVisibility(BatteryConfig.shadow.enabled ? View.VISIBLE : View.GONE);
        batteryShadowBlurSeekBar.setProgress((int) BatteryConfig.shadow.blur);
        batteryShadowOffsetXSeekBar.setProgress((int) BatteryConfig.shadow.offsetX + 60);
        batteryShadowOffsetYSeekBar.setProgress((int) BatteryConfig.shadow.offsetY + 60);
        batteryLockSwitch.setChecked(BatteryConfig.touchPassthrough);
        activity.applyCheckboxTint(batteryLockSwitch, BatteryConfig.touchPassthrough);
        batteryValueOnlyCheck.setChecked(BatteryConfig.showOnlyValue);
        batteryShowTempCheck.setChecked(BatteryConfig.showTemperature);
        batteryShowPctCheck.setChecked(BatteryConfig.showPercentage);
        batterySafeArea.setChecked(BatteryConfig.safeArea);
        batterySizeLabel.setText("Ukuran Teks: " + (int) BatteryConfig.size);
        batteryBgPaddingLabel.setText("Ukuran Background: " + BatteryConfig.bg.padding);
        batteryBgOffsetXLabel.setText("Offset X: " + BatteryConfig.bg.offsetX);
        batteryBgOffsetYLabel.setText("Offset Y: " + BatteryConfig.bg.offsetY);
        batteryBgColorPreview.setBackgroundColor(BatteryConfig.bg.color);
        batteryBgMarginLabel.setText("Margin: " + BatteryConfig.bg.margin);
        batteryBgRadiusLabel.setText("Radius: " + BatteryConfig.bg.radius);
        batteryShadowBlurLabel.setText("Blur Shadow: " + (int) BatteryConfig.shadow.blur);
        batteryShadowOffsetXLabel.setText("Shadow X: " + (int) BatteryConfig.shadow.offsetX);
        batteryShadowOffsetYLabel.setText("Shadow Y: " + (int) BatteryConfig.shadow.offsetY);
        batteryShadowColorPreview.setBackgroundColor(BatteryConfig.shadow.color);
        batteryIntervalValue.setText(formatIntervalValue(BatteryConfig.updateInterval));
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
                    FloatingService.startModule(FloatingService.batteryModule());
                } else {
                    activity.startService(new Intent(activity, FloatingService.class));
                }
            } else {
                FloatingService.stopModule(FloatingService.batteryModule());
                if (!activity.isAnyModuleActive()) {
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
                FloatingService.updateSizeForModule(FloatingService.batteryModule(), BatteryConfig.size);
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        batteryColorPreview.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Pilih Warna Baterai", BatteryConfig.color, color -> {
                BatteryConfig.color = color;
                batteryColorPreview.setBackgroundColor(color);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("battery_color", color).apply();
                FloatingService.updateColorForModule(FloatingService.batteryModule(), BatteryConfig.color);
            });
        });

        batteryLabelColorPreview.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Pilih Warna Label", BatteryConfig.labelColor, color -> {
                BatteryConfig.labelColor = color;
                batteryLabelColorPreview.setBackgroundColor(color);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("battery_label_color", color).apply();
                FloatingService.updateLabelColorForModule(FloatingService.batteryModule(), BatteryConfig.labelColor);
            });
        });

        batteryBgSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            BatteryConfig.bg.enabled = isChecked;
            activity.applyCheckboxTint(batteryBgSwitch, isChecked);
            batteryBgConfigContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("battery_bg_enabled", isChecked).apply();
            FloatingService.updateBackgroundForModule(FloatingService.batteryModule());
        });

        batteryBgColorPreview.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Warna Background Baterai", BatteryConfig.bg.color, color -> {
                BatteryConfig.bg.color = color;
                batteryBgColorPreview.setBackgroundColor(color);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("battery_bg_color", color).apply();
                FloatingService.updateBackgroundForModule(FloatingService.batteryModule());
            });
        });

        batteryBgPaddingSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (progress < 0) progress = 0;
                BatteryConfig.bg.padding = progress;
                batteryBgPaddingLabel.setText("Ukuran Background: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("battery_bg_padding", progress).apply();
                FloatingService.updateBackgroundForModule(FloatingService.batteryModule());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        batteryBgOffsetXSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                BatteryConfig.bg.offsetX = offset;
                batteryBgOffsetXLabel.setText("Offset X: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("battery_bg_offset_x", offset).apply();
                FloatingService.updateBackgroundForModule(FloatingService.batteryModule());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        batteryBgOffsetYSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                BatteryConfig.bg.offsetY = offset;
                batteryBgOffsetYLabel.setText("Offset Y: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("battery_bg_offset_y", offset).apply();
                FloatingService.updateBackgroundForModule(FloatingService.batteryModule());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        batteryBgMarginSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                BatteryConfig.bg.margin = progress;
                batteryBgMarginLabel.setText("Margin: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("battery_bg_margin", progress).apply();
                FloatingService.updateBackgroundForModule(FloatingService.batteryModule());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        batteryBgRadiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                BatteryConfig.bg.radius = progress;
                batteryBgRadiusLabel.setText("Radius: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("battery_bg_radius", progress).apply();
                FloatingService.updateBackgroundForModule(FloatingService.batteryModule());
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
            FloatingService.updateShadowForModule(FloatingService.batteryModule());
        });

        batteryShadowColorPreview.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Warna Shadow Baterai", BatteryConfig.shadow.color, color -> {
                BatteryConfig.shadow.color = color;
                batteryShadowColorPreview.setBackgroundColor(color);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("battery_shadow_color", color).apply();
                FloatingService.updateShadowForModule(FloatingService.batteryModule());
            });
        });

        batteryShadowBlurSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                BatteryConfig.shadow.blur = progress;
                batteryShadowBlurLabel.setText("Blur Shadow: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putFloat("battery_shadow_blur", (float) progress).apply();
                FloatingService.updateShadowForModule(FloatingService.batteryModule());
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
                FloatingService.updateShadowForModule(FloatingService.batteryModule());
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
                FloatingService.updateShadowForModule(FloatingService.batteryModule());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        batteryLockSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            BatteryConfig.touchPassthrough = isChecked;
            activity.applyCheckboxTint(batteryLockSwitch, isChecked);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("battery_lock", isChecked).apply();
            FloatingService.updateTouchFlagsForModule(FloatingService.batteryModule());
        });

        batteryValueOnlyCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            BatteryConfig.showOnlyValue = isChecked;
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("battery_show_only_value", isChecked).apply();
        });

        batteryShowTempCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            BatteryConfig.showTemperature = isChecked;
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("battery_show_temperature", isChecked).apply();
        });

        batteryShowPctCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            BatteryConfig.showPercentage = isChecked;
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("battery_show_percentage", isChecked).apply();
        });

        batterySafeArea.setOnCheckedChangeListener((buttonView, isChecked) -> {
            BatteryConfig.safeArea = isChecked;
            activity.applyCheckboxTint(batterySafeArea, isChecked);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("battery_safe_area", isChecked).apply();
        });

        setupIntervalListeners();
    }

    private void saveBatteryShadowPrefs() {
        activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE).edit()
                .putInt("battery_shadow_color", BatteryConfig.shadow.color)
                .putFloat("battery_shadow_blur", BatteryConfig.shadow.blur)
                .putFloat("battery_shadow_offset_x", BatteryConfig.shadow.offsetX)
                .putFloat("battery_shadow_offset_y", BatteryConfig.shadow.offsetY)
                .apply();
    }

    private static final float[] INTERVAL_STEPS = {0.2f, 0.5f, 0.75f, 1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f};

    private void setupIntervalListeners() {
        batteryIntervalValue.setOnClickListener(v -> showIntervalPopup(v));
    }

    private void showIntervalPopup(View anchor) {
        if (intervalPopup != null && intervalPopup.isShowing()) {
            intervalPopup.dismiss();
            return;
        }

        int currentIdx = -1;
        for (int i = 0; i < INTERVAL_STEPS.length; i++) {
            if (INTERVAL_STEPS[i] == BatteryConfig.updateInterval) {
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
            item.setTextColor(0xFF222222);
            item.setGravity(Gravity.CENTER_VERTICAL);
            item.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            if (i == currentIdx) {
                item.setBackgroundColor(0xFF4A90D9);
                item.setTextColor(0xFFFFFFFF);
            }

            final int idx = i;
            item.setOnClickListener(v -> {
                BatteryConfig.updateInterval = INTERVAL_STEPS[idx];
                updateIntervalDisplay();
                FloatingService.restartModule(FloatingService.batteryModule());
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
        batteryIntervalValue.setText(formatIntervalValue(BatteryConfig.updateInterval));
        activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                .edit().putFloat("battery_update_interval", BatteryConfig.updateInterval).apply();
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
