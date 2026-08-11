package exp.ftxt.ui;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import exp.ftxt.MainActivity;
import exp.ftxt.R;
import exp.ftxt.core.FloatingService;
import exp.ftxt.features.battery_bar.BatteryBarConfig;
import exp.ftxt.shared.ui.ColorPickerDialog;
import exp.ftxt.shared.ui.SectionHelper;
import exp.ftxt.utils.PermissionHelper;

public class BatteryBarPanelController {

    private final MainActivity activity;

    private CheckBox barSwitch;
    private CheckBox barQuickModeCheck;
    private TextView barQuickSideValue;
    private CheckBox barHorizontalCheck;
    private SeekBar barThicknessSeekBar;
    private TextView barThicknessLabel;
    private SeekBar barLengthSeekBar;
    private TextView barLengthLabel;
    private View barColorPreview;
    private CheckBox barAutoColorCheck;
    private View barLowColorPreview;
    private SeekBar barLowThresholdSeekBar;
    private TextView barLowThresholdLabel;
    private CheckBox barShowEmptyStripCheck;
    private View barEmptyColorPreview;
    private SeekBar barRadiusSeekBar;
    private TextView barRadiusLabel;
    private SeekBar barFadeSpeedSeekBar;
    private TextView barFadeSpeedLabel;
    private CheckBox barLockSwitch;
    private CheckBox barSafeAreaCheck;

    private View manualSection;
    private TextView manualSectionHeader;

    private BatteryBarPositionController positionController;
    private PopupWindow sidePopup;

    public BatteryBarPanelController(MainActivity activity, View rootView) {
        this.activity = activity;
        bindViews(rootView);
        loadConfig();
        setupListeners();
        positionController = new BatteryBarPositionController(activity, rootView);
        updateManualVisibility();
    }

    public void onPanelShown() {
        if (positionController != null) {
            positionController.refresh();
        }
    }

    public void showLoadPresetDialog() {
        if (positionController != null) {
            positionController.showLoadPresetDialog();
        }
    }

    public void cleanup() {
        if (positionController != null) {
            positionController.cleanup();
            positionController = null;
        }
    }

    private void bindViews(View rootView) {
        barSwitch = rootView.findViewById(R.id.batbarSwitch);
        barQuickModeCheck = rootView.findViewById(R.id.batbarQuickModeCheck);
        barQuickSideValue = rootView.findViewById(R.id.batbarQuickSideValue);
        barHorizontalCheck = rootView.findViewById(R.id.batbarHorizontalCheck);
        barThicknessSeekBar = rootView.findViewById(R.id.batbarThicknessSeekBar);
        barThicknessLabel = rootView.findViewById(R.id.batbarThicknessLabel);
        barLengthSeekBar = rootView.findViewById(R.id.batbarLengthSeekBar);
        barLengthLabel = rootView.findViewById(R.id.batbarLengthLabel);
        barColorPreview = rootView.findViewById(R.id.batbarColorPreview);
        barAutoColorCheck = rootView.findViewById(R.id.batbarAutoColorCheck);
        barLowColorPreview = rootView.findViewById(R.id.batbarLowColorPreview);
        barLowThresholdSeekBar = rootView.findViewById(R.id.batbarLowThresholdSeekBar);
        barLowThresholdLabel = rootView.findViewById(R.id.batbarLowThresholdLabel);
        barShowEmptyStripCheck = rootView.findViewById(R.id.batbarShowEmptyStripCheck);
        barEmptyColorPreview = rootView.findViewById(R.id.batbarEmptyColorPreview);
        barRadiusSeekBar = rootView.findViewById(R.id.batbarRadiusSeekBar);
        barRadiusLabel = rootView.findViewById(R.id.batbarRadiusLabel);
        barFadeSpeedSeekBar = rootView.findViewById(R.id.batbarFadeSpeedSeekBar);
        barFadeSpeedLabel = rootView.findViewById(R.id.batbarFadeSpeedLabel);
        barLockSwitch = rootView.findViewById(R.id.batbarLockSwitch);
        barSafeAreaCheck = rootView.findViewById(R.id.batbarSafeAreaCheck);

        manualSection = rootView.findViewById(R.id.batbar_sectionManual);
        manualSectionHeader = rootView.findViewById(R.id.batbar_sectionManualHeader);
        SectionHelper.setupCollapsible(manualSectionHeader, manualSection);

        SectionHelper.setupCollapsible(rootView.findViewById(R.id.batbar_sectionQuickHeader),
                rootView.findViewById(R.id.batbar_sectionQuick));
        SectionHelper.setupCollapsible(rootView.findViewById(R.id.batbar_sectionAppearanceHeader),
                rootView.findViewById(R.id.batbar_sectionAppearance));
        SectionHelper.setupCollapsible(rootView.findViewById(R.id.batbar_sectionLowBatteryHeader),
                rootView.findViewById(R.id.batbar_sectionLowBattery));
        SectionHelper.setupCollapsible(rootView.findViewById(R.id.batbar_sectionPositionHeader),
                rootView.findViewById(R.id.batbar_sectionPosition));
    }

    private void loadConfig() {
        barSwitch.setChecked(BatteryBarConfig.enabled);
        activity.applyCheckboxTint(barSwitch, BatteryBarConfig.enabled);
        barQuickModeCheck.setChecked(BatteryBarConfig.quickMode);
        activity.applyCheckboxTint(barQuickModeCheck, BatteryBarConfig.quickMode);
        barQuickSideValue.setText(sideLabel(BatteryBarConfig.quickSide));
        barHorizontalCheck.setChecked(BatteryBarConfig.horizontal);
        activity.applyCheckboxTint(barHorizontalCheck, BatteryBarConfig.horizontal);
        barThicknessSeekBar.setProgress(BatteryBarConfig.thickness);
        barLengthSeekBar.setProgress((int) (BatteryBarConfig.length * 100));
        barColorPreview.setBackgroundColor(BatteryBarConfig.color);
        barAutoColorCheck.setChecked(BatteryBarConfig.autoColor);
        activity.applyCheckboxTint(barAutoColorCheck, BatteryBarConfig.autoColor);
        barLowColorPreview.setBackgroundColor(BatteryBarConfig.lowColor);
        barLowThresholdSeekBar.setProgress(BatteryBarConfig.lowThreshold);
        barShowEmptyStripCheck.setChecked(BatteryBarConfig.showEmptyStrip);
        activity.applyCheckboxTint(barShowEmptyStripCheck, BatteryBarConfig.showEmptyStrip);
        barEmptyColorPreview.setBackgroundColor(BatteryBarConfig.emptyColor);
        barRadiusSeekBar.setProgress(BatteryBarConfig.radius);
        barFadeSpeedSeekBar.setProgress(BatteryBarConfig.fadeSpeed);
        barLockSwitch.setChecked(BatteryBarConfig.touchPassthrough);
        activity.applyCheckboxTint(barLockSwitch, BatteryBarConfig.touchPassthrough);
        barSafeAreaCheck.setChecked(BatteryBarConfig.safeArea);
        activity.applyCheckboxTint(barSafeAreaCheck, BatteryBarConfig.safeArea);

        barThicknessLabel.setText("Ketebalan: " + BatteryBarConfig.thickness + "px");
        barLengthLabel.setText("Panjang: " + Math.round(BatteryBarConfig.length * 100) + "%");
        barLowThresholdLabel.setText("Ambang Low: " + BatteryBarConfig.lowThreshold + "%");
        barRadiusLabel.setText("Radius Sudut: " + BatteryBarConfig.radius + "px");
        barFadeSpeedLabel.setText("Kecepatan Fade: " + BatteryBarConfig.fadeSpeed);
    }

    private void setupListeners() {
        barSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !PermissionHelper.hasOverlayPermission(activity)) {
                barSwitch.setChecked(false);
                activity.applyCheckboxTint(barSwitch, false);
                prefs().edit().putBoolean("batbar_enabled", false).apply();
                return;
            }
            BatteryBarConfig.enabled = isChecked;
            activity.applyCheckboxTint(barSwitch, isChecked);
            prefs().edit().putBoolean("batbar_enabled", isChecked).apply();
            if (isChecked) {
                if (FloatingService.instance != null) {
                    FloatingService.startModule(FloatingService.batteryBarModule());
                } else {
                    activity.startService(new Intent(activity, FloatingService.class));
                }
            } else {
                FloatingService.stopModule(FloatingService.batteryBarModule());
                if (!activity.isAnyModuleActive()) {
                    activity.stopService(new Intent(activity, FloatingService.class));
                }
            }
        });

        barQuickModeCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            BatteryBarConfig.quickMode = isChecked;
            activity.applyCheckboxTint(barQuickModeCheck, isChecked);
            prefs().edit().putBoolean("batbar_quick_mode", isChecked).apply();
            updateManualVisibility();
            restart();
        });

        barQuickSideValue.setOnClickListener(v -> showSidePopup(v));

        barHorizontalCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            BatteryBarConfig.horizontal = isChecked;
            activity.applyCheckboxTint(barHorizontalCheck, isChecked);
            prefs().edit().putBoolean("batbar_horizontal", isChecked).apply();
            restart();
        });

        barThicknessSeekBar.setOnSeekBarChangeListener(simpleSeekBar(progress -> {
            BatteryBarConfig.thickness = progress;
            barThicknessLabel.setText("Ketebalan: " + progress + "px");
            prefs().edit().putInt("batbar_thickness", progress).apply();
            restart();
        }));

        barLengthSeekBar.setOnSeekBarChangeListener(simpleSeekBar(progress -> {
            BatteryBarConfig.length = progress / 100f;
            barLengthLabel.setText("Panjang: " + progress + "%");
            prefs().edit().putFloat("batbar_length", BatteryBarConfig.length).apply();
            restart();
        }));

        barColorPreview.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Pilih Warna Bar", BatteryBarConfig.color, color -> {
                BatteryBarConfig.color = color;
                barColorPreview.setBackgroundColor(color);
                prefs().edit().putInt("batbar_color", color).apply();
                restart();
            });
        });

        barAutoColorCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            BatteryBarConfig.autoColor = isChecked;
            activity.applyCheckboxTint(barAutoColorCheck, isChecked);
            prefs().edit().putBoolean("batbar_auto_color", isChecked).apply();
            restart();
        });

        barLowColorPreview.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Warna Saat Low", BatteryBarConfig.lowColor, color -> {
                BatteryBarConfig.lowColor = color;
                barLowColorPreview.setBackgroundColor(color);
                prefs().edit().putInt("batbar_low_color", color).apply();
                restart();
            });
        });

        barLowThresholdSeekBar.setOnSeekBarChangeListener(simpleSeekBar(progress -> {
            BatteryBarConfig.lowThreshold = progress;
            barLowThresholdLabel.setText("Ambang Low: " + progress + "%");
            prefs().edit().putInt("batbar_low_threshold", progress).apply();
            restart();
        }));

        barShowEmptyStripCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            BatteryBarConfig.showEmptyStrip = isChecked;
            activity.applyCheckboxTint(barShowEmptyStripCheck, isChecked);
            prefs().edit().putBoolean("batbar_show_empty_strip", isChecked).apply();
            restart();
        });

        barEmptyColorPreview.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Warna Strip Kosong", BatteryBarConfig.emptyColor, color -> {
                BatteryBarConfig.emptyColor = color;
                barEmptyColorPreview.setBackgroundColor(color);
                prefs().edit().putInt("batbar_empty_color", color).apply();
                restart();
            });
        });

        barRadiusSeekBar.setOnSeekBarChangeListener(simpleSeekBar(progress -> {
            BatteryBarConfig.radius = progress;
            barRadiusLabel.setText("Radius Sudut: " + progress + "px");
            prefs().edit().putInt("batbar_radius", progress).apply();
            restart();
        }));

        barFadeSpeedSeekBar.setOnSeekBarChangeListener(simpleSeekBar(progress -> {
            BatteryBarConfig.fadeSpeed = progress;
            barFadeSpeedLabel.setText("Kecepatan Fade: " + progress);
            prefs().edit().putInt("batbar_fade_speed", progress).apply();
            restart();
        }));

        barLockSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            BatteryBarConfig.touchPassthrough = isChecked;
            activity.applyCheckboxTint(barLockSwitch, isChecked);
            prefs().edit().putBoolean("batbar_lock", isChecked).apply();
            FloatingService.updateTouchFlagsForModule(FloatingService.batteryBarModule());
        });

        barSafeAreaCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            BatteryBarConfig.safeArea = isChecked;
            activity.applyCheckboxTint(barSafeAreaCheck, isChecked);
            prefs().edit().putBoolean("batbar_safe_area", isChecked).apply();
            restart();
        });
    }

    private void showSidePopup(View anchor) {
        if (sidePopup != null && sidePopup.isShowing()) {
            sidePopup.dismiss();
            return;
        }
        String[] sides = {"top", "bottom", "left", "right"};
        LinearLayout content = new LinearLayout(activity);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setBackgroundColor(0xFFFFFFFF);
        for (String side : sides) {
            TextView item = new TextView(activity);
            item.setText(sideLabel(side));
            item.setPadding(dp(16), dp(10), dp(16), dp(10));
            item.setTextSize(14);
            item.setTextColor(0xFF222222);
            item.setGravity(Gravity.CENTER_VERTICAL);
            item.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            if (side.equals(BatteryBarConfig.quickSide)) {
                item.setBackgroundColor(0xFF4A90D9);
                item.setTextColor(0xFFFFFFFF);
            }
            item.setOnClickListener(v -> {
                BatteryBarConfig.quickSide = side;
                barQuickSideValue.setText(sideLabel(side));
                prefs().edit().putString("batbar_quick_side", side).apply();
                if (positionController != null) positionController.syncAll();
                restart();
                if (sidePopup != null) sidePopup.dismiss();
            });
            content.addView(item);
        }
        ScrollView scrollView = new ScrollView(activity);
        scrollView.addView(content);
        sidePopup = new PopupWindow(scrollView,
                ViewGroup.LayoutParams.WRAP_CONTENT, dp(160), true);
        sidePopup.setBackgroundDrawable(new ColorDrawable(0xFFFFFFFF));
        sidePopup.setOutsideTouchable(true);
        sidePopup.setElevation(dp(4));
        sidePopup.showAsDropDown(anchor, 0, dp(2));
    }

    private void updateManualVisibility() {
        boolean manual = !BatteryBarConfig.quickMode;
        manualSection.setVisibility(manual ? View.VISIBLE : View.GONE);
        if (positionController != null) {
            positionController.setPositionControlsEnabled(manual);
        }
    }

    private SeekBar.OnSeekBarChangeListener simpleSeekBar(OnProgress handler) {
        return new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (fromUser) handler.run(progress);
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        };
    }

    private void restart() {
        FloatingService.restartModule(FloatingService.batteryBarModule());
    }

    private String sideLabel(String side) {
        switch (side) {
            case "top": return "Atas (Top)";
            case "bottom": return "Bawah (Bottom)";
            case "left": return "Kiri (Left)";
            default: return "Kanan (Right)";
        }
    }

    private android.content.SharedPreferences prefs() {
        return activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE);
    }

    private int dp(int dp) {
        return (int) (dp * activity.getResources().getDisplayMetrics().density);
    }

    private interface OnProgress {
        void run(int progress);
    }
}
