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
import exp.ftxt.features.battery_current.BatteryCurrentConfig;
import exp.ftxt.shared.ui.ColorPickerDialog;
import exp.ftxt.shared.ui.SectionHelper;
import exp.ftxt.utils.PermissionHelper;

public class BatteryCurrentPanelController {

    private final MainActivity activity;

    private CheckBox batCurSwitch;
    private CheckBox batCurValueOnlyCheck;
    private SeekBar batCurSizeSeekBar;
    private View batCurColorPreview;
    private View batCurLabelColorPreview;
    private View batCurSeparatorColorPreview;
    private CheckBox batCurShadowSwitch;
    private LinearLayout batCurShadowConfigContainer;
    private View batCurShadowColorPreview;
    private SeekBar batCurShadowBlurSeekBar;
    private SeekBar batCurShadowOffsetXSeekBar;
    private SeekBar batCurShadowOffsetYSeekBar;
    private CheckBox batCurLockSwitch;
    private CheckBox batCurShowVoltage;
    private CheckBox batCurShowCurrent;
    private CheckBox batCurShowPower;
    private CheckBox batCurSafeArea;
    private CheckBox batCurBgSwitch;
    private LinearLayout batCurBgConfigContainer;
    private View batCurBgColorPreview;
    private SeekBar batCurBgPaddingSeekBar;
    private SeekBar batCurBgOffsetXSeekBar;
    private SeekBar batCurBgOffsetYSeekBar;
    private SeekBar batCurBgMarginSeekBar;
    private SeekBar batCurBgRadiusSeekBar;
    private TextView batCurSizeLabel, batCurBgPaddingLabel, batCurBgOffsetXLabel, batCurBgOffsetYLabel;
    private TextView batCurBgMarginLabel, batCurBgRadiusLabel;
    private TextView batCurShadowBlurLabel, batCurShadowOffsetXLabel, batCurShadowOffsetYLabel;
    private BatteryCurrentPositionController batCurPositionController;
    private TextView batCurIntervalValue;
    private PopupWindow intervalPopup;

    public BatteryCurrentPanelController(MainActivity activity, View rootView) {
        this.activity = activity;
        bindViews(rootView);
        loadConfig();
        setupListeners();
        batCurPositionController = new BatteryCurrentPositionController(activity, rootView);
    }

    public void onPanelShown() {
        if (batCurPositionController != null) {
            batCurPositionController.refresh();
        }
    }

    public void showLoadPresetDialog() {
        if (batCurPositionController != null) {
            batCurPositionController.showLoadPresetDialog();
        }
    }

    public void cleanup() {
        if (batCurPositionController != null) {
            batCurPositionController.cleanup();
            batCurPositionController = null;
        }
    }

    private void bindViews(View rootView) {
        batCurSwitch = rootView.findViewById(R.id.batCurSwitch);
        batCurValueOnlyCheck = rootView.findViewById(R.id.batCurValueOnlyCheck);
        batCurSizeSeekBar = rootView.findViewById(R.id.batCurSizeSeekBar);
        batCurColorPreview = rootView.findViewById(R.id.batCurColorPreview);
        batCurLabelColorPreview = rootView.findViewById(R.id.batCurLabelColorPreview);
        batCurSeparatorColorPreview = rootView.findViewById(R.id.batCurSeparatorColorPreview);
        batCurShadowSwitch = rootView.findViewById(R.id.batCurShadowSwitch);
        batCurShadowConfigContainer = rootView.findViewById(R.id.shadowConfigBatteryCurrent);
        batCurShadowColorPreview = rootView.findViewById(R.id.batCurShadowColorPreview);
        batCurShadowBlurSeekBar = rootView.findViewById(R.id.batCurShadowBlurSeekBar);
        batCurShadowOffsetXSeekBar = rootView.findViewById(R.id.batCurShadowOffsetXSeekBar);
        batCurShadowOffsetYSeekBar = rootView.findViewById(R.id.batCurShadowOffsetYSeekBar);
        batCurLockSwitch = rootView.findViewById(R.id.batCurLockSwitch);
        batCurShowVoltage = rootView.findViewById(R.id.batCurShowVoltage);
        batCurShowCurrent = rootView.findViewById(R.id.batCurShowCurrent);
        batCurShowPower = rootView.findViewById(R.id.batCurShowPower);
        batCurSafeArea = rootView.findViewById(R.id.batCurSafeArea);
        batCurBgSwitch = rootView.findViewById(R.id.batCurBgSwitch);
        batCurBgConfigContainer = rootView.findViewById(R.id.bgConfigBatteryCurrent);
        batCurBgColorPreview = rootView.findViewById(R.id.batCurBgColorPreview);
        batCurBgPaddingSeekBar = rootView.findViewById(R.id.batCurBgPaddingSeekBar);
        batCurBgOffsetXSeekBar = rootView.findViewById(R.id.batCurBgOffsetXSeekBar);
        batCurBgOffsetYSeekBar = rootView.findViewById(R.id.batCurBgOffsetYSeekBar);
        batCurBgMarginSeekBar = rootView.findViewById(R.id.batCurBgMarginSeekBar);
        batCurBgRadiusSeekBar = rootView.findViewById(R.id.batCurBgRadiusSeekBar);
        batCurSizeLabel = rootView.findViewById(R.id.batCurSizeLabel);
        batCurBgPaddingLabel = rootView.findViewById(R.id.batCurBgPaddingLabel);
        batCurBgOffsetXLabel = rootView.findViewById(R.id.batCurBgOffsetXLabel);
        batCurBgOffsetYLabel = rootView.findViewById(R.id.batCurBgOffsetYLabel);
        batCurBgMarginLabel = rootView.findViewById(R.id.batCurBgMarginLabel);
        batCurBgRadiusLabel = rootView.findViewById(R.id.batCurBgRadiusLabel);
        batCurShadowBlurLabel = rootView.findViewById(R.id.batCurShadowBlurLabel);
        batCurShadowOffsetXLabel = rootView.findViewById(R.id.batCurShadowOffsetXLabel);
        batCurShadowOffsetYLabel = rootView.findViewById(R.id.batCurShadowOffsetYLabel);
        batCurIntervalValue = rootView.findViewById(R.id.batCurIntervalValue);

        View sectionDisplay = rootView.findViewById(R.id.batCur_sectionDisplay);
        TextView sectionDisplayHeader = rootView.findViewById(R.id.batCur_sectionDisplayHeader);
        SectionHelper.setupCollapsible(sectionDisplayHeader, sectionDisplay);

        View sectionPosition = rootView.findViewById(R.id.batCur_sectionPosition);
        TextView sectionPositionHeader = rootView.findViewById(R.id.batCur_sectionPositionHeader);
        SectionHelper.setupCollapsible(sectionPositionHeader, sectionPosition);

        View sectionShadow = rootView.findViewById(R.id.batCur_sectionShadow);
        TextView sectionShadowHeader = rootView.findViewById(R.id.batCur_sectionShadowHeader);
        SectionHelper.setupCollapsible(sectionShadowHeader, sectionShadow);

        View sectionBackground = rootView.findViewById(R.id.batCur_sectionBackground);
        TextView sectionBackgroundHeader = rootView.findViewById(R.id.batCur_sectionBackgroundHeader);
        SectionHelper.setupCollapsible(sectionBackgroundHeader, sectionBackground);
    }

    private void loadConfig() {
        batCurSwitch.setChecked(BatteryCurrentConfig.enabled);
        activity.applyCheckboxTint(batCurSwitch, BatteryCurrentConfig.enabled);
        batCurValueOnlyCheck.setChecked(BatteryCurrentConfig.showOnlyValue);
        batCurSizeSeekBar.setProgress((int) BatteryCurrentConfig.size);
        batCurColorPreview.setBackgroundColor(BatteryCurrentConfig.color);
        batCurLabelColorPreview.setBackgroundColor(BatteryCurrentConfig.labelColor);
        batCurSeparatorColorPreview.setBackgroundColor(BatteryCurrentConfig.separatorColor);
        batCurBgSwitch.setChecked(BatteryCurrentConfig.bg.enabled);
        activity.applyCheckboxTint(batCurBgSwitch, BatteryCurrentConfig.bg.enabled);
        batCurBgConfigContainer.setVisibility(BatteryCurrentConfig.bg.enabled ? View.VISIBLE : View.GONE);
        batCurBgPaddingSeekBar.setProgress(BatteryCurrentConfig.bg.padding);
        batCurBgOffsetXSeekBar.setProgress(BatteryCurrentConfig.bg.offsetX + 60);
        batCurBgOffsetYSeekBar.setProgress(BatteryCurrentConfig.bg.offsetY + 60);
        batCurBgMarginSeekBar.setProgress(BatteryCurrentConfig.bg.margin);
        batCurBgRadiusSeekBar.setProgress(BatteryCurrentConfig.bg.radius);
        batCurShadowSwitch.setChecked(BatteryCurrentConfig.shadow.enabled);
        activity.applyCheckboxTint(batCurShadowSwitch, BatteryCurrentConfig.shadow.enabled);
        batCurShadowConfigContainer.setVisibility(BatteryCurrentConfig.shadow.enabled ? View.VISIBLE : View.GONE);
        batCurShadowBlurSeekBar.setProgress((int) BatteryCurrentConfig.shadow.blur);
        batCurShadowOffsetXSeekBar.setProgress((int) BatteryCurrentConfig.shadow.offsetX + 60);
        batCurShadowOffsetYSeekBar.setProgress((int) BatteryCurrentConfig.shadow.offsetY + 60);
        batCurLockSwitch.setChecked(BatteryCurrentConfig.touchPassthrough);
        activity.applyCheckboxTint(batCurLockSwitch, BatteryCurrentConfig.touchPassthrough);
        batCurShowVoltage.setChecked(BatteryCurrentConfig.showVoltage);
        activity.applyCheckboxTint(batCurShowVoltage, BatteryCurrentConfig.showVoltage);
        batCurShowCurrent.setChecked(BatteryCurrentConfig.showCurrent);
        activity.applyCheckboxTint(batCurShowCurrent, BatteryCurrentConfig.showCurrent);
        batCurShowPower.setChecked(BatteryCurrentConfig.showPower);
        activity.applyCheckboxTint(batCurShowPower, BatteryCurrentConfig.showPower);
        batCurSafeArea.setChecked(BatteryCurrentConfig.safeArea);
        batCurSizeLabel.setText("Ukuran Teks: " + (int) BatteryCurrentConfig.size);
        batCurBgPaddingLabel.setText("Ukuran Background: " + BatteryCurrentConfig.bg.padding);
        batCurBgOffsetXLabel.setText("Offset X: " + BatteryCurrentConfig.bg.offsetX);
        batCurBgOffsetYLabel.setText("Offset Y: " + BatteryCurrentConfig.bg.offsetY);
        batCurBgColorPreview.setBackgroundColor(BatteryCurrentConfig.bg.color);
        batCurBgMarginLabel.setText("Margin: " + BatteryCurrentConfig.bg.margin);
        batCurBgRadiusLabel.setText("Radius: " + BatteryCurrentConfig.bg.radius);
        batCurShadowBlurLabel.setText("Blur Shadow: " + (int) BatteryCurrentConfig.shadow.blur);
        batCurShadowOffsetXLabel.setText("Shadow X: " + (int) BatteryCurrentConfig.shadow.offsetX);
        batCurShadowOffsetYLabel.setText("Shadow Y: " + (int) BatteryCurrentConfig.shadow.offsetY);
        batCurShadowColorPreview.setBackgroundColor(BatteryCurrentConfig.shadow.color);
        batCurIntervalValue.setText(formatIntervalValue(BatteryCurrentConfig.updateInterval));
    }

    private void setupListeners() {
        batCurSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !PermissionHelper.hasOverlayPermission(activity)) {
                batCurSwitch.setChecked(false);
                activity.applyCheckboxTint(batCurSwitch, false);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putBoolean("batcur_enabled", false).apply();
                return;
            }

            BatteryCurrentConfig.enabled = isChecked;
            activity.applyCheckboxTint(batCurSwitch, isChecked);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("batcur_enabled", isChecked).apply();

            if (isChecked) {
                if (FloatingService.instance != null) {
                    FloatingService.startModule(FloatingService.batteryCurrentModule());
                } else {
                    activity.startService(new Intent(activity, FloatingService.class));
                }
            } else {
                FloatingService.stopModule(FloatingService.batteryCurrentModule());
                if (!activity.isAnyModuleActive()) {
                    activity.stopService(new Intent(activity, FloatingService.class));
                }
            }
        });

        batCurValueOnlyCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            BatteryCurrentConfig.showOnlyValue = isChecked;
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("batcur_show_only_value", isChecked).apply();
            FloatingService.updateColorForModule(FloatingService.batteryCurrentModule(), BatteryCurrentConfig.color);
        });

        batCurSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (progress < 5) { progress = 5; sb.setProgress(progress); }
                if (progress > 140) { progress = 140; sb.setProgress(progress); }
                BatteryCurrentConfig.size = progress;
                batCurSizeLabel.setText("Ukuran Teks: " + progress);
                FloatingService.updateSizeForModule(FloatingService.batteryCurrentModule(), BatteryCurrentConfig.size);
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        batCurColorPreview.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Pilih Warna Bat Current", BatteryCurrentConfig.color, color -> {
                BatteryCurrentConfig.color = color;
                batCurColorPreview.setBackgroundColor(color);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("batcur_color", color).apply();
                FloatingService.updateColorForModule(FloatingService.batteryCurrentModule(), BatteryCurrentConfig.color);
            });
        });

        batCurLabelColorPreview.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Warna Label Bat Current", BatteryCurrentConfig.labelColor, color -> {
                BatteryCurrentConfig.labelColor = color;
                batCurLabelColorPreview.setBackgroundColor(color);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("batcur_label_color", color).apply();
                FloatingService.updateLabelColorForModule(FloatingService.batteryCurrentModule(), BatteryCurrentConfig.labelColor);
            });
        });

        batCurSeparatorColorPreview.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Warna Pemisah Bat Current", BatteryCurrentConfig.separatorColor, color -> {
                BatteryCurrentConfig.separatorColor = color;
                batCurSeparatorColorPreview.setBackgroundColor(color);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("batcur_separator_color", color).apply();
                FloatingService.updateSeparatorColorForModule(FloatingService.batteryCurrentModule(), BatteryCurrentConfig.separatorColor);
            });
        });

        batCurBgSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            BatteryCurrentConfig.bg.enabled = isChecked;
            activity.applyCheckboxTint(batCurBgSwitch, isChecked);
            batCurBgConfigContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("batcur_bg_enabled", isChecked).apply();
            FloatingService.updateBackgroundForModule(FloatingService.batteryCurrentModule());
        });

        batCurBgColorPreview.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Warna Background Bat Cur", BatteryCurrentConfig.bg.color, color -> {
                BatteryCurrentConfig.bg.color = color;
                batCurBgColorPreview.setBackgroundColor(color);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("batcur_bg_color", color).apply();
                FloatingService.updateBackgroundForModule(FloatingService.batteryCurrentModule());
            });
        });

        batCurBgPaddingSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (progress < 0) progress = 0;
                BatteryCurrentConfig.bg.padding = progress;
                batCurBgPaddingLabel.setText("Ukuran Background: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("batcur_bg_padding", progress).apply();
                FloatingService.updateBackgroundForModule(FloatingService.batteryCurrentModule());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        batCurBgOffsetXSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                BatteryCurrentConfig.bg.offsetX = offset;
                batCurBgOffsetXLabel.setText("Offset X: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("batcur_bg_offset_x", offset).apply();
                FloatingService.updateBackgroundForModule(FloatingService.batteryCurrentModule());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        batCurBgOffsetYSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                BatteryCurrentConfig.bg.offsetY = offset;
                batCurBgOffsetYLabel.setText("Offset Y: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("batcur_bg_offset_y", offset).apply();
                FloatingService.updateBackgroundForModule(FloatingService.batteryCurrentModule());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        batCurBgMarginSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                BatteryCurrentConfig.bg.margin = progress;
                batCurBgMarginLabel.setText("Margin: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("batcur_bg_margin", progress).apply();
                FloatingService.updateBackgroundForModule(FloatingService.batteryCurrentModule());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        batCurBgRadiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                BatteryCurrentConfig.bg.radius = progress;
                batCurBgRadiusLabel.setText("Radius: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("batcur_bg_radius", progress).apply();
                FloatingService.updateBackgroundForModule(FloatingService.batteryCurrentModule());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        batCurShadowSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            BatteryCurrentConfig.shadow.enabled = isChecked;
            activity.applyCheckboxTint(batCurShadowSwitch, isChecked);
            batCurShadowConfigContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("batcur_shadow_enabled", isChecked).apply();
            saveBatCurShadowPrefs();
            FloatingService.updateShadowForModule(FloatingService.batteryCurrentModule());
        });

        batCurShadowColorPreview.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Warna Shadow Bat Cur", BatteryCurrentConfig.shadow.color, color -> {
                BatteryCurrentConfig.shadow.color = color;
                batCurShadowColorPreview.setBackgroundColor(color);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("batcur_shadow_color", color).apply();
                FloatingService.updateShadowForModule(FloatingService.batteryCurrentModule());
            });
        });

        batCurShadowBlurSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                BatteryCurrentConfig.shadow.blur = progress;
                batCurShadowBlurLabel.setText("Blur Shadow: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putFloat("batcur_shadow_blur", (float) progress).apply();
                FloatingService.updateShadowForModule(FloatingService.batteryCurrentModule());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        batCurShadowOffsetXSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                BatteryCurrentConfig.shadow.offsetX = offset;
                batCurShadowOffsetXLabel.setText("Shadow X: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putFloat("batcur_shadow_offset_x", (float) offset).apply();
                FloatingService.updateShadowForModule(FloatingService.batteryCurrentModule());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        batCurShadowOffsetYSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                BatteryCurrentConfig.shadow.offsetY = offset;
                batCurShadowOffsetYLabel.setText("Shadow Y: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putFloat("batcur_shadow_offset_y", (float) offset).apply();
                FloatingService.updateShadowForModule(FloatingService.batteryCurrentModule());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        batCurLockSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            BatteryCurrentConfig.touchPassthrough = isChecked;
            activity.applyCheckboxTint(batCurLockSwitch, isChecked);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("batcur_lock", isChecked).apply();
            FloatingService.updateTouchFlagsForModule(FloatingService.batteryCurrentModule());
        });

        batCurShowVoltage.setOnCheckedChangeListener((buttonView, isChecked) -> {
            BatteryCurrentConfig.showVoltage = isChecked;
            activity.applyCheckboxTint(batCurShowVoltage, isChecked);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("batcur_show_voltage", isChecked).apply();
            FloatingService.updateColorForModule(FloatingService.batteryCurrentModule(), BatteryCurrentConfig.color);
        });

        batCurShowCurrent.setOnCheckedChangeListener((buttonView, isChecked) -> {
            BatteryCurrentConfig.showCurrent = isChecked;
            activity.applyCheckboxTint(batCurShowCurrent, isChecked);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("batcur_show_current", isChecked).apply();
            FloatingService.updateColorForModule(FloatingService.batteryCurrentModule(), BatteryCurrentConfig.color);
        });

        batCurShowPower.setOnCheckedChangeListener((buttonView, isChecked) -> {
            BatteryCurrentConfig.showPower = isChecked;
            activity.applyCheckboxTint(batCurShowPower, isChecked);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("batcur_show_power", isChecked).apply();
            FloatingService.updateColorForModule(FloatingService.batteryCurrentModule(), BatteryCurrentConfig.color);
        });

        batCurSafeArea.setOnCheckedChangeListener((buttonView, isChecked) -> {
            BatteryCurrentConfig.safeArea = isChecked;
            activity.applyCheckboxTint(batCurSafeArea, isChecked);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("batcur_safe_area", isChecked).apply();
        });

        setupIntervalListeners();
    }

    private void saveBatCurShadowPrefs() {
        activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE).edit()
                .putInt("batcur_shadow_color", BatteryCurrentConfig.shadow.color)
                .putFloat("batcur_shadow_blur", BatteryCurrentConfig.shadow.blur)
                .putFloat("batcur_shadow_offset_x", BatteryCurrentConfig.shadow.offsetX)
                .putFloat("batcur_shadow_offset_y", BatteryCurrentConfig.shadow.offsetY)
                .apply();
    }

    private static final float[] INTERVAL_STEPS = {0.2f, 0.5f, 0.75f, 1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f};

    private void setupIntervalListeners() {
        batCurIntervalValue.setOnClickListener(v -> showIntervalPopup(v));
    }

    private void showIntervalPopup(View anchor) {
        if (intervalPopup != null && intervalPopup.isShowing()) {
            intervalPopup.dismiss();
            return;
        }

        int currentIdx = -1;
        for (int i = 0; i < INTERVAL_STEPS.length; i++) {
            if (INTERVAL_STEPS[i] == BatteryCurrentConfig.updateInterval) {
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
                BatteryCurrentConfig.updateInterval = INTERVAL_STEPS[idx];
                updateIntervalDisplay();
                FloatingService.restartModule(FloatingService.batteryCurrentModule());
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
        batCurIntervalValue.setText(formatIntervalValue(BatteryCurrentConfig.updateInterval));
        activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                .edit().putFloat("batcur_update_interval", BatteryCurrentConfig.updateInterval).apply();
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
