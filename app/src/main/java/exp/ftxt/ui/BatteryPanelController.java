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
import exp.ftxt.features.battery_stats.BatteryStatsConfig;
import exp.ftxt.shared.ui.ColorPickerDialog;
import exp.ftxt.shared.ui.SectionHelper;
import exp.ftxt.utils.PermissionHelper;

public class BatteryPanelController {

    private final MainActivity activity;

    private CheckBox batterySwitch;
    private SeekBar batterySizeSeekBar;
    private View batteryColorPreview;
    private View batteryLabelColorPreview;
    private View batterySeparatorColorPreview;
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

    private void bindViews(View rootView) {
        batterySwitch = rootView.findViewById(R.id.batterySwitch);
        batterySizeSeekBar = rootView.findViewById(R.id.batterySizeSeekBar);
        batteryColorPreview = rootView.findViewById(R.id.batteryColorPreview);
        batteryLabelColorPreview = rootView.findViewById(R.id.batteryLabelColorPreview);
        batterySeparatorColorPreview = rootView.findViewById(R.id.batterySeparatorColorPreview);
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
        batterySwitch.setChecked(BatteryStatsConfig.enabled);
        activity.applyCheckboxTint(batterySwitch, BatteryStatsConfig.enabled);
        batterySizeSeekBar.setProgress((int) BatteryStatsConfig.size);
        batteryColorPreview.setBackgroundColor(BatteryStatsConfig.color);
        batteryLabelColorPreview.setBackgroundColor(BatteryStatsConfig.labelColor);
        batterySeparatorColorPreview.setBackgroundColor(BatteryStatsConfig.separatorColor);
        batteryBgSwitch.setChecked(BatteryStatsConfig.bg.enabled);
        activity.applyCheckboxTint(batteryBgSwitch, BatteryStatsConfig.bg.enabled);
        batteryBgConfigContainer.setVisibility(BatteryStatsConfig.bg.enabled ? View.VISIBLE : View.GONE);
        batteryBgPaddingSeekBar.setProgress(BatteryStatsConfig.bg.padding);
        batteryBgOffsetXSeekBar.setProgress(BatteryStatsConfig.bg.offsetX + 60);
        batteryBgOffsetYSeekBar.setProgress(BatteryStatsConfig.bg.offsetY + 60);
        batteryBgMarginSeekBar.setProgress(BatteryStatsConfig.bg.margin);
        batteryBgRadiusSeekBar.setProgress(BatteryStatsConfig.bg.radius);
        batteryShadowSwitch.setChecked(BatteryStatsConfig.shadow.enabled);
        activity.applyCheckboxTint(batteryShadowSwitch, BatteryStatsConfig.shadow.enabled);
        batteryShadowConfigContainer.setVisibility(BatteryStatsConfig.shadow.enabled ? View.VISIBLE : View.GONE);
        batteryShadowBlurSeekBar.setProgress((int) BatteryStatsConfig.shadow.blur);
        batteryShadowOffsetXSeekBar.setProgress((int) BatteryStatsConfig.shadow.offsetX + 60);
        batteryShadowOffsetYSeekBar.setProgress((int) BatteryStatsConfig.shadow.offsetY + 60);
        batteryLockSwitch.setChecked(BatteryStatsConfig.touchPassthrough);
        activity.applyCheckboxTint(batteryLockSwitch, BatteryStatsConfig.touchPassthrough);
        batteryValueOnlyCheck.setChecked(BatteryStatsConfig.showOnlyValue);
        batteryShowTempCheck.setChecked(BatteryStatsConfig.showTemperature);
        batteryShowPctCheck.setChecked(BatteryStatsConfig.showPercentage);
        batterySafeArea.setChecked(BatteryStatsConfig.safeArea);
        batterySizeLabel.setText("Ukuran Teks: " + (int) BatteryStatsConfig.size);
        batteryBgPaddingLabel.setText("Ukuran Background: " + BatteryStatsConfig.bg.padding);
        batteryBgOffsetXLabel.setText("Offset X: " + BatteryStatsConfig.bg.offsetX);
        batteryBgOffsetYLabel.setText("Offset Y: " + BatteryStatsConfig.bg.offsetY);
        batteryBgColorPreview.setBackgroundColor(BatteryStatsConfig.bg.color);
        batteryBgMarginLabel.setText("Margin: " + BatteryStatsConfig.bg.margin);
        batteryBgRadiusLabel.setText("Radius: " + BatteryStatsConfig.bg.radius);
        batteryShadowBlurLabel.setText("Blur Shadow: " + (int) BatteryStatsConfig.shadow.blur);
        batteryShadowOffsetXLabel.setText("Shadow X: " + (int) BatteryStatsConfig.shadow.offsetX);
        batteryShadowOffsetYLabel.setText("Shadow Y: " + (int) BatteryStatsConfig.shadow.offsetY);
        batteryShadowColorPreview.setBackgroundColor(BatteryStatsConfig.shadow.color);
        batteryIntervalValue.setText(formatIntervalValue(BatteryStatsConfig.updateInterval));
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

            BatteryStatsConfig.enabled = isChecked;
            activity.applyCheckboxTint(batterySwitch, isChecked);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("battery_enabled", isChecked).apply();

            if (isChecked) {
                if (FloatingService.instance != null) {
                    FloatingService.startModule(FloatingService.batteryStatsModule());
                } else {
                    activity.startService(new Intent(activity, FloatingService.class));
                }
            } else {
                FloatingService.stopModule(FloatingService.batteryStatsModule());
                if (!activity.isAnyModuleActive()) {
                    activity.stopService(new Intent(activity, FloatingService.class));
                }
            }
        });

        batterySizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (progress < 5) { progress = 5; sb.setProgress(progress); }
                if (progress > 140) { progress = 140; sb.setProgress(progress); }
                BatteryStatsConfig.size = progress;
                batterySizeLabel.setText("Ukuran Teks: " + progress);
                FloatingService.updateSizeForModule(FloatingService.batteryStatsModule(), BatteryStatsConfig.size);
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        batteryColorPreview.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Pilih Warna Baterai", BatteryStatsConfig.color, color -> {
                BatteryStatsConfig.color = color;
                batteryColorPreview.setBackgroundColor(color);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("battery_color", color).apply();
                FloatingService.updateColorForModule(FloatingService.batteryStatsModule(), BatteryStatsConfig.color);
            });
        });

        batteryLabelColorPreview.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Pilih Warna Label", BatteryStatsConfig.labelColor, color -> {
                BatteryStatsConfig.labelColor = color;
                batteryLabelColorPreview.setBackgroundColor(color);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("battery_label_color", color).apply();
                FloatingService.updateLabelColorForModule(FloatingService.batteryStatsModule(), BatteryStatsConfig.labelColor);
            });
        });

        batterySeparatorColorPreview.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Pilih Warna Pemisah", BatteryStatsConfig.separatorColor, color -> {
                BatteryStatsConfig.separatorColor = color;
                batterySeparatorColorPreview.setBackgroundColor(color);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("battery_separator_color", color).apply();
                FloatingService.updateSeparatorColorForModule(FloatingService.batteryStatsModule(), BatteryStatsConfig.separatorColor);
            });
        });

        batteryBgSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            BatteryStatsConfig.bg.enabled = isChecked;
            activity.applyCheckboxTint(batteryBgSwitch, isChecked);
            batteryBgConfigContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("battery_bg_enabled", isChecked).apply();
            FloatingService.updateBackgroundForModule(FloatingService.batteryStatsModule());
        });

        batteryBgColorPreview.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Warna Background Baterai", BatteryStatsConfig.bg.color, color -> {
                BatteryStatsConfig.bg.color = color;
                batteryBgColorPreview.setBackgroundColor(color);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("battery_bg_color", color).apply();
                FloatingService.updateBackgroundForModule(FloatingService.batteryStatsModule());
            });
        });

        batteryBgPaddingSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (progress < 0) progress = 0;
                BatteryStatsConfig.bg.padding = progress;
                batteryBgPaddingLabel.setText("Ukuran Background: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("battery_bg_padding", progress).apply();
                FloatingService.updateBackgroundForModule(FloatingService.batteryStatsModule());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        batteryBgOffsetXSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                BatteryStatsConfig.bg.offsetX = offset;
                batteryBgOffsetXLabel.setText("Offset X: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("battery_bg_offset_x", offset).apply();
                FloatingService.updateBackgroundForModule(FloatingService.batteryStatsModule());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        batteryBgOffsetYSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                BatteryStatsConfig.bg.offsetY = offset;
                batteryBgOffsetYLabel.setText("Offset Y: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("battery_bg_offset_y", offset).apply();
                FloatingService.updateBackgroundForModule(FloatingService.batteryStatsModule());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        batteryBgMarginSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                BatteryStatsConfig.bg.margin = progress;
                batteryBgMarginLabel.setText("Margin: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("battery_bg_margin", progress).apply();
                FloatingService.updateBackgroundForModule(FloatingService.batteryStatsModule());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        batteryBgRadiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                BatteryStatsConfig.bg.radius = progress;
                batteryBgRadiusLabel.setText("Radius: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("battery_bg_radius", progress).apply();
                FloatingService.updateBackgroundForModule(FloatingService.batteryStatsModule());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        batteryShadowSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            BatteryStatsConfig.shadow.enabled = isChecked;
            activity.applyCheckboxTint(batteryShadowSwitch, isChecked);
            batteryShadowConfigContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("battery_shadow_enabled", isChecked).apply();
            saveBatteryShadowPrefs();
            FloatingService.updateShadowForModule(FloatingService.batteryStatsModule());
        });

        batteryShadowColorPreview.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Warna Shadow Baterai", BatteryStatsConfig.shadow.color, color -> {
                BatteryStatsConfig.shadow.color = color;
                batteryShadowColorPreview.setBackgroundColor(color);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("battery_shadow_color", color).apply();
                FloatingService.updateShadowForModule(FloatingService.batteryStatsModule());
            });
        });

        batteryShadowBlurSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                BatteryStatsConfig.shadow.blur = progress;
                batteryShadowBlurLabel.setText("Blur Shadow: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putFloat("battery_shadow_blur", (float) progress).apply();
                FloatingService.updateShadowForModule(FloatingService.batteryStatsModule());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        batteryShadowOffsetXSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                BatteryStatsConfig.shadow.offsetX = offset;
                batteryShadowOffsetXLabel.setText("Shadow X: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putFloat("battery_shadow_offset_x", (float) offset).apply();
                FloatingService.updateShadowForModule(FloatingService.batteryStatsModule());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        batteryShadowOffsetYSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                BatteryStatsConfig.shadow.offsetY = offset;
                batteryShadowOffsetYLabel.setText("Shadow Y: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putFloat("battery_shadow_offset_y", (float) offset).apply();
                FloatingService.updateShadowForModule(FloatingService.batteryStatsModule());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        batteryLockSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            BatteryStatsConfig.touchPassthrough = isChecked;
            activity.applyCheckboxTint(batteryLockSwitch, isChecked);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("battery_lock", isChecked).apply();
            FloatingService.updateTouchFlagsForModule(FloatingService.batteryStatsModule());
        });

        batteryValueOnlyCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            BatteryStatsConfig.showOnlyValue = isChecked;
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("battery_show_only_value", isChecked).apply();
        });

        batteryShowTempCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            BatteryStatsConfig.showTemperature = isChecked;
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("battery_show_temperature", isChecked).apply();
        });

        batteryShowPctCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            BatteryStatsConfig.showPercentage = isChecked;
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("battery_show_percentage", isChecked).apply();
        });

        batterySafeArea.setOnCheckedChangeListener((buttonView, isChecked) -> {
            BatteryStatsConfig.safeArea = isChecked;
            activity.applyCheckboxTint(batterySafeArea, isChecked);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("battery_safe_area", isChecked).apply();
        });

        setupIntervalListeners();
    }

    private void saveBatteryShadowPrefs() {
        activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE).edit()
                .putInt("battery_shadow_color", BatteryStatsConfig.shadow.color)
                .putFloat("battery_shadow_blur", BatteryStatsConfig.shadow.blur)
                .putFloat("battery_shadow_offset_x", BatteryStatsConfig.shadow.offsetX)
                .putFloat("battery_shadow_offset_y", BatteryStatsConfig.shadow.offsetY)
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
            if (INTERVAL_STEPS[i] == BatteryStatsConfig.updateInterval) {
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
                BatteryStatsConfig.updateInterval = INTERVAL_STEPS[idx];
                updateIntervalDisplay();
                FloatingService.restartModule(FloatingService.batteryStatsModule());
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
        batteryIntervalValue.setText(formatIntervalValue(BatteryStatsConfig.updateInterval));
        activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                .edit().putFloat("battery_update_interval", BatteryStatsConfig.updateInterval).apply();
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
