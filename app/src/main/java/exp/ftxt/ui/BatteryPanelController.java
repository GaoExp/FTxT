package exp.ftxt.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import exp.ftxt.MainActivity;
import exp.ftxt.R;
import exp.ftxt.core.FloatingService;
import exp.ftxt.features.battery_stats.BatteryCapacityEstimator;
import exp.ftxt.features.battery_stats.BatteryHistoryDb;
import exp.ftxt.features.battery_stats.BatteryMonitor;
import exp.ftxt.features.battery_stats.BatteryReading;
import exp.ftxt.features.battery_stats.BatteryStatsConfig;
import exp.ftxt.shared.ui.BatteryChartView;
import exp.ftxt.shared.ui.ColorPickerDialog;
import exp.ftxt.shared.ui.SectionHelper;
import exp.ftxt.shared.ui.SliderLabelEditor;
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
    private BatteryOrderZonesView batteryOrderZones;
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

    private View batTabMonitorView;
    private TextView batMonitorPercentText;
    private ProgressBar batMonitorLevelBar;
    private TextView batMonitorChargeText;
    private TextView batMonitorMetricsText1;
    private TextView batMonitorMetricsText2;
    private TextView batMonitorStatusText;
    private TextView batMonitorConditionBadge;
    private Button batMonitorExportButton;
    private Button batMonitorCopyButton;
    private int monitorLabelColor;

    private BatteryChartView batChartTempView;
    private BatteryChartView batChartPercentView;
    private BatteryChartView batChartPowerView;
    private RadioGroup batChartRangeGroup;
    private RadioButton batChartRange5m;
    private RadioButton batChartRange15m;
    private RadioButton batChartRange1h;
    private RadioButton batChartRange6h;
    private RadioButton batChartRange24h;
    private TextView batChartRangeLabel;
    private long chartWindowMs = BatteryChartView.WINDOW_5M;

    private TextView batHealthText;
    private TextView batHealthDesignText;
    private TextView batHealthSessionBadge;

    private final Handler monitorHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService chartExecutor = Executors.newSingleThreadExecutor();
    private boolean chartQueryInFlight = false;
    private final Runnable monitorRunnable = new Runnable() {
        @Override
        public void run() {
            updateMonitorInfo();
            monitorHandler.postDelayed(this, 1000);
        }
    };

    public BatteryPanelController(MainActivity activity, View rootView) {
        this.activity = activity;
        bindViews(rootView);
        loadConfig();
        setupListeners();
        batteryPositionController = new BatteryPositionController(activity, rootView);
    }

    public void onPanelShown() {
        refreshOrderList();
        if (batteryPositionController != null) {
            batteryPositionController.refresh();
        }
        resumeMonitorPolling();
    }

    public void onPanelHidden() {
        stopMonitorPolling();
    }

    private void refreshOrderList() {
        if (batteryOrderZones == null) return;
        batteryOrderZones.setOrder(BatteryStatsConfig.itemOrder);
    }

    public void showLoadPresetDialog() {
        if (batteryPositionController != null) {
            batteryPositionController.showLoadPresetDialog();
        }
    }

    public void cleanup() {
        stopMonitorPolling();
        chartExecutor.shutdown();
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
        batteryOrderZones = rootView.findViewById(R.id.batteryOrderZones);
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

        batTabMonitorView = rootView.findViewById(R.id.batTabMonitor);
        batMonitorPercentText = rootView.findViewById(R.id.batMonitorPercentText);
        batMonitorLevelBar = rootView.findViewById(R.id.batMonitorLevelBar);
        batMonitorChargeText = rootView.findViewById(R.id.batMonitorChargeText);
        batMonitorMetricsText1 = rootView.findViewById(R.id.batMonitorMetricsText1);
        batMonitorMetricsText2 = rootView.findViewById(R.id.batMonitorMetricsText2);
        batMonitorStatusText = rootView.findViewById(R.id.batMonitorStatusText);
        batMonitorConditionBadge = rootView.findViewById(R.id.batMonitorConditionBadge);
        batMonitorExportButton = rootView.findViewById(R.id.batMonitorExportButton);
        batMonitorCopyButton = rootView.findViewById(R.id.batMonitorCopyButton);
        monitorLabelColor = activity.getColor(R.color.bat_monitor_label);

        batChartTempView = rootView.findViewById(R.id.batChartTempView);
        batChartPercentView = rootView.findViewById(R.id.batChartPercentView);
        batChartPowerView = rootView.findViewById(R.id.batChartPowerView);
        batChartRangeGroup = rootView.findViewById(R.id.batChartRangeGroup);
        batChartRange5m = rootView.findViewById(R.id.batChartRange5m);
        batChartRange15m = rootView.findViewById(R.id.batChartRange15m);
        batChartRange1h = rootView.findViewById(R.id.batChartRange1h);
        batChartRange6h = rootView.findViewById(R.id.batChartRange6h);
        batChartRange24h = rootView.findViewById(R.id.batChartRange24h);
        batChartRangeLabel = rootView.findViewById(R.id.batChartRangeLabel);

        batHealthText = rootView.findViewById(R.id.batHealthText);
        batHealthDesignText = rootView.findViewById(R.id.batHealthDesignText);
        batHealthSessionBadge = rootView.findViewById(R.id.batHealthSessionBadge);

        View sectionPosition = rootView.findViewById(R.id.battery_sectionPosition);
        TextView sectionPositionHeader = rootView.findViewById(R.id.battery_sectionPositionHeader);
        SectionHelper.setupCollapsible(sectionPositionHeader, sectionPosition);

        View sectionShadow = rootView.findViewById(R.id.battery_sectionShadow);
        TextView sectionShadowHeader = rootView.findViewById(R.id.battery_sectionShadowHeader);
        SectionHelper.setupCollapsible(sectionShadowHeader, sectionShadow);

        View sectionBackground = rootView.findViewById(R.id.battery_sectionBackground);
        TextView sectionBackgroundHeader = rootView.findViewById(R.id.battery_sectionBackgroundHeader);
        SectionHelper.setupCollapsible(sectionBackgroundHeader, sectionBackground);

        View sectionOrder = rootView.findViewById(R.id.battery_sectionOrder);
        TextView sectionOrderHeader = rootView.findViewById(R.id.battery_sectionOrderHeader);
        SectionHelper.setupCollapsible(sectionOrderHeader, sectionOrder);
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
        updateMonitorInfo();
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

        batterySafeArea.setOnCheckedChangeListener((buttonView, isChecked) -> {
            BatteryStatsConfig.safeArea = isChecked;
            activity.applyCheckboxTint(batterySafeArea, isChecked);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("battery_safe_area", isChecked).apply();
        });

        batterySizeLabel.setOnClickListener(v ->
                SliderLabelEditor.showSliderEditor(activity, "Ukuran Teks", batterySizeSeekBar, 140, batterySizeLabel, "Ukuran Teks: "));
        batteryBgPaddingLabel.setOnClickListener(v ->
                SliderLabelEditor.showSliderEditor(activity, "Ukuran Background", batteryBgPaddingSeekBar, 80, batteryBgPaddingLabel, "Ukuran Background: "));
        batteryBgOffsetXLabel.setOnClickListener(v ->
                SliderLabelEditor.showOffsetEditor(activity, "Offset X", batteryBgOffsetXSeekBar, batteryBgOffsetXLabel, "Offset X: "));
        batteryBgOffsetYLabel.setOnClickListener(v ->
                SliderLabelEditor.showOffsetEditor(activity, "Offset Y", batteryBgOffsetYSeekBar, batteryBgOffsetYLabel, "Offset Y: "));
        batteryBgMarginLabel.setOnClickListener(v ->
                SliderLabelEditor.showSliderEditor(activity, "Margin Background", batteryBgMarginSeekBar, 30, batteryBgMarginLabel, "Margin: "));
        batteryBgRadiusLabel.setOnClickListener(v ->
                SliderLabelEditor.showSliderEditor(activity, "Radius Background", batteryBgRadiusSeekBar, 50, batteryBgRadiusLabel, "Radius: "));
        batteryShadowBlurLabel.setOnClickListener(v ->
                SliderLabelEditor.showSliderEditor(activity, "Blur Shadow", batteryShadowBlurSeekBar, 50, batteryShadowBlurLabel, "Blur Shadow: "));
        batteryShadowOffsetXLabel.setOnClickListener(v ->
                SliderLabelEditor.showOffsetEditor(activity, "Shadow X", batteryShadowOffsetXSeekBar, batteryShadowOffsetXLabel, "Shadow X: "));
        batteryShadowOffsetYLabel.setOnClickListener(v ->
                SliderLabelEditor.showOffsetEditor(activity, "Shadow Y", batteryShadowOffsetYSeekBar, batteryShadowOffsetYLabel, "Shadow Y: "));

        setupIntervalListeners();
        setupOrderZones();
        setupMonitorTab();
    }

    private void setupMonitorTab() {
        BatteryCapacityEstimator.init(activity);
        batMonitorExportButton.setOnClickListener(v -> exportBatterySnapshot());
        batMonitorCopyButton.setOnClickListener(v -> copyToClipboard());
        batHealthDesignText.setOnClickListener(v -> showDesignCapacityDialog());
        setupChartControls();
    }

    private void setupChartControls() {
        batChartTempView.setSeriesType(BatteryChartView.SERIES_TEMP);
        batChartPercentView.setSeriesType(BatteryChartView.SERIES_PERCENT);
        batChartPowerView.setSeriesType(BatteryChartView.SERIES_POWER);

        batChartRangeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            long window;
            String label;
            if (checkedId == R.id.batChartRange15m) {
                window = BatteryChartView.WINDOW_15M;
                label = "15 Menit Terakhir";
            } else if (checkedId == R.id.batChartRange1h) {
                window = BatteryChartView.WINDOW_1H;
                label = "1 Jam Terakhir";
            } else if (checkedId == R.id.batChartRange6h) {
                window = BatteryChartView.WINDOW_6H;
                label = "6 Jam Terakhir";
            } else if (checkedId == R.id.batChartRange24h) {
                window = BatteryChartView.WINDOW_24H;
                label = "24 Jam Terakhir";
            } else {
                window = BatteryChartView.WINDOW_5M;
                label = "5 Menit Terakhir";
            }
            chartWindowMs = window;
            batChartRangeLabel.setText(label);
            batChartTempView.setWindowMs(window);
            batChartPercentView.setWindowMs(window);
            batChartPowerView.setWindowMs(window);
            refreshChart();
        });
    }

    private void refreshChart() {
        if (batChartTempView == null || chartQueryInFlight) return;
        final long now = System.currentTimeMillis();
        final long from = now - chartWindowMs;
        chartQueryInFlight = true;
        chartExecutor.execute(() -> {
            BatteryReading.Snapshot[] data =
                    BatteryHistoryDb.get(activity).queryChart(from, now, 600);
            monitorHandler.post(() -> {
                chartQueryInFlight = false;
                if (batChartTempView == null) return;
                batChartTempView.setData(data);
                batChartPercentView.setData(data);
                batChartPowerView.setData(data);
            });
        });
    }

    private void resumeMonitorPolling() {
        if (batTabMonitorView == null || batTabMonitorView.getVisibility() != View.VISIBLE) {
            stopMonitorPolling();
            return;
        }
        monitorHandler.removeCallbacks(monitorRunnable);
        monitorHandler.post(monitorRunnable);
    }

    private void stopMonitorPolling() {
        monitorHandler.removeCallbacks(monitorRunnable);
    }

    private void updateMonitorInfo() {
        if (batMonitorPercentText == null) return;
        BatteryReading.Snapshot s = BatteryMonitor.getLastSnapshot();

        batMonitorPercentText.setText(s.percent + "%");
        batMonitorLevelBar.setProgress(s.percent);

        SpannableStringBuilder charge = new SpannableStringBuilder();
        appendLine(charge, "Kapasitas Tersisa",
                s.chargeMah >= 0 ? s.chargeMah + " mAh" : "—");
        batMonitorChargeText.setText(charge);

        SpannableStringBuilder col1 = new SpannableStringBuilder();
        appendLine(col1, "Suhu", String.format(Locale.US, "%.1f°C", s.tempC));
        appendLine(col1, "Voltase", String.format(Locale.US, "%.3fV", s.voltageV));
        appendLine(col1, "Arus", s.currentMa != 0
                ? String.format(Locale.US, "%+d mA", s.currentMa) : "—");
        appendLine(col1, "Daya", s.powerW > 0
                ? String.format(Locale.US, "%.2fW", s.powerW) : "—");
        batMonitorMetricsText1.setText(col1);

        SpannableStringBuilder col2 = new SpannableStringBuilder();
        appendLine(col2, "Kapasitas", s.chargeMah >= 0 ? s.chargeMah + " mAh" : "—");
        appendLine(col2, "Cycle Count", s.cycleCount >= 0 ? String.valueOf(s.cycleCount) : "—");
        appendLine(col2, "Teknologi", s.technology != null ? s.technology : "—");
        batMonitorMetricsText2.setText(col2);

        SpannableStringBuilder status = new SpannableStringBuilder();
        appendLine(status, "Status", s.chargingText());
        switch (s.pluggedInt) {
            case BatteryManager.BATTERY_PLUGGED_AC: appendLine(status, "Sumber Daya", "AC"); break;
            case BatteryManager.BATTERY_PLUGGED_USB: appendLine(status, "Sumber Daya", "USB"); break;
            case BatteryManager.BATTERY_PLUGGED_WIRELESS: appendLine(status, "Sumber Daya", "Wireless"); break;
            default: appendLine(status, "Sumber Daya", "Baterai"); break;
        }
        batMonitorStatusText.setText(status);

        int condLevel = s.conditionLevel();
        batMonitorConditionBadge.setText("● " + s.conditionText());
        int condColor = activity.getColor(condLevel > 0 ? R.color.bat_monitor_hot
                : condLevel < 0 ? R.color.bat_monitor_cold : R.color.bat_monitor_active);
        batMonitorConditionBadge.setTextColor(condColor);

        refreshHealthCard();
        refreshChart();
    }

    private void refreshHealthCard() {
        if (batHealthText == null) return;
        BatteryCapacityEstimator.HealthResult r = BatteryCapacityEstimator.getResult();

        SpannableStringBuilder sb = new SpannableStringBuilder();
        appendLine(sb, "Estimasi Kapasitas",
                r.medianMah > 0 ? String.format(Locale.US, "%.0f mAh", r.medianMah) : "—");

        int scoreColor;
        String score;
        if (r.designMah <= 0) {
            score = "Isi kapasitas desain";
            scoreColor = monitorLabelColor;
        } else if (r.medianMah <= 0) {
            score = "Belum ada data";
            scoreColor = monitorLabelColor;
        } else {
            float pctScore = r.medianMah / r.designMah * 100f;
            score = String.format(Locale.US, "%.1f%%", pctScore);
            scoreColor = activity.getColor(pctScore >= 80f ? R.color.bat_monitor_active
                    : pctScore >= 50f ? R.color.bat_monitor_header : R.color.bat_monitor_stop);
        }
        appendLineColored(sb, "Skor Kesehatan", score, scoreColor);

        appendLine(sb, "Sesi Tercatat", String.valueOf(r.sessionCount));
        String confidence = r.totalSamples > 0
                ? (r.fromScreenOffSessions
                        ? r.totalSamples + " sampel (layar mati)"
                        : r.totalSamples + " sampel")
                : "—";
        appendLineColored(sb, "Keyakinan", confidence,
                r.totalSamples <= 0 ? monitorLabelColor : null);

        boolean collecting = BatteryCapacityEstimator.isSegmentActive();
        String status = collecting
                ? "Mengumpulkan saat mengisi…"
                : "Menunggu pengisian daya";
        appendLineColored(sb, "Status", status,
                collecting ? activity.getColor(R.color.bat_monitor_active) : monitorLabelColor);

        batHealthText.setText(sb);
        batHealthSessionBadge.setText(r.sessionCount + " sesi");
        batHealthDesignText.setText(r.designMah > 0
                ? "Kapasitas Desain: " + r.designMah + " mAh · Ketuk untuk mengatur"
                : "Kapasitas Desain: Belum diatur · Ketuk untuk mengatur");
    }

    private void appendLineColored(SpannableStringBuilder sb, String label, String value, Integer valueColor) {
        String padded = String.format(Locale.US, "%-17s", label);
        int start = sb.length();
        sb.append(padded).append(value).append("\n");
        sb.setSpan(new ForegroundColorSpan(monitorLabelColor),
                start, start + padded.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (valueColor != null) {
            sb.setSpan(new ForegroundColorSpan(valueColor),
                    start + padded.length(), sb.length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private void showDesignCapacityDialog() {
        final EditText input = new EditText(activity);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        int current = BatteryCapacityEstimator.getResult().designMah;
        input.setText(current > 0 ? String.valueOf(current) : "");
        input.setHint("mis. 5000");

        new AlertDialog.Builder(activity)
                .setTitle("Kapasitas Desain")
                .setMessage("Masukkan kapasitas desain baterai (mAh) sesuai spesifikasi pabrik. "
                        + "Skor kesehatan hanya dihitung jika kolom ini terisi. Kosongkan untuk menghapus.")
                .setView(input)
                .setPositiveButton("Simpan", (dialog, which) -> {
                    String txt = input.getText().toString().trim();
                    int value = 0;
                    if (!txt.isEmpty()) {
                        try {
                            value = Integer.parseInt(txt);
                        } catch (NumberFormatException ignored) {}
                    }
                    if (value != 0 && (value < 500 || value > 30000)) {
                        Toast.makeText(activity, "Kapasitas harus 500–30000 mAh", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    BatteryCapacityEstimator.setDesignCapacity(value);
                    refreshHealthCard();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void appendLine(SpannableStringBuilder sb, String label, String value) {
        String padded = String.format(Locale.US, "%-17s", label);
        int start = sb.length();
        sb.append(padded).append(value).append("\n");
        sb.setSpan(new ForegroundColorSpan(monitorLabelColor),
                start, start + padded.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void copyToClipboard() {
        if (batMonitorMetricsText1 == null) return;
        StringBuilder sb = new StringBuilder();
        sb.append("Baterai Perangkat\n");
        sb.append(batMonitorPercentText.getText()).append("\n\n");
        sb.append(batMonitorChargeText.getText().toString().trim()).append("\n\n");
        sb.append("Metrik Real-Time\n").append(combineMetricColumns());
        sb.append("\n\nStatus Pengisian\n").append(batMonitorStatusText.getText());
        ClipboardManager cm = (ClipboardManager) activity.getSystemService(android.content.Context.CLIPBOARD_SERVICE);
        if (cm == null) return;
        cm.setPrimaryClip(ClipData.newPlainText("FTxT Monitor Baterai", sb.toString()));
        Toast.makeText(activity, "Disalin ke clipboard", Toast.LENGTH_SHORT).show();
    }

    private String combineMetricColumns() {
        String left = batMonitorMetricsText1.getText().toString().trim();
        String right = batMonitorMetricsText2.getText().toString().trim();
        if (left.isEmpty()) return right;
        if (right.isEmpty()) return left;
        return left + "\n" + right;
    }

    private void exportBatterySnapshot() {
        String exportTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                .format(new Date());
        BatteryReading.Snapshot[] history =
                BatteryHistoryDb.get(activity).queryLastSamples(20);
        StringBuilder sb = new StringBuilder();
        sb.append("FTxT - Monitor Baterai (Riwayat 20 Snapshot Terakhir)\n");
        sb.append("Ekspor: ").append(exportTime).append("\n");
        sb.append("Jumlah snapshot: ").append(history.length).append("\n\n");
        int index = 1;
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        for (BatteryReading.Snapshot snap : history) {
            String time = timeFormat.format(new Date(snap.time));
            sb.append("--- Snapshot ").append(index++)
                    .append("/").append(history.length)
                    .append(" (").append(time).append(") ---\n");
            sb.append("Persentase       : ").append(snap.percent).append("%\n");
            sb.append("Suhu             : ").append(String.format(Locale.US, "%.1f°C", snap.tempC)).append("\n");
            sb.append("Voltase          : ").append(String.format(Locale.US, "%.3fV", snap.voltageV)).append("\n");
            sb.append("Arus             : ").append(snap.currentMa != 0
                    ? String.format(Locale.US, "%+d mA", snap.currentMa) : "—").append("\n");
            sb.append("Daya             : ").append(snap.powerW > 0
                    ? String.format(Locale.US, "%.2fW", snap.powerW) : "—").append("\n");
            sb.append("Kapasitas Tersisa: ").append(snap.chargeMah >= 0 ? snap.chargeMah + " mAh" : "—").append("\n");
            sb.append("Cycle Count      : ").append(snap.cycleCount >= 0 ? String.valueOf(snap.cycleCount) : "—").append("\n");
            sb.append("Status           : ").append(snap.chargingText()).append("\n");
            sb.append("Teknologi        : ").append(snap.technology != null ? snap.technology : "—").append("\n");
            sb.append("Kondisi          : ").append(snap.conditionText()).append("\n\n");
        }

        String fileName = "FTxT_baterai_" + System.currentTimeMillis() + ".txt";
        try {
            if (writeSnapshotToDownload(sb.toString(), fileName)) {
                Toast.makeText(activity, "Tersimpan: Download/" + fileName +
                        " (" + history.length + " snapshot)", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(activity, "Gagal menyimpan snapshot", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(activity, "Gagal menyimpan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean writeSnapshotToDownload(String content, String fileName) throws Exception {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
            Uri uri = activity.getContentResolver().insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri == null) return false;
            try (OutputStream os = activity.getContentResolver().openOutputStream(uri)) {
                if (os == null) return false;
                os.write(content.getBytes("UTF-8"));
            }
            return true;
        } else {
            File dir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS);
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, fileName);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(content.getBytes("UTF-8"));
            }
            return true;
        }
    }

    private void saveBatteryShadowPrefs() {
        activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE).edit()
                .putInt("battery_shadow_color", BatteryStatsConfig.shadow.color)
                .putFloat("battery_shadow_blur", BatteryStatsConfig.shadow.blur)
                .putFloat("battery_shadow_offset_x", BatteryStatsConfig.shadow.offsetX)
                .putFloat("battery_shadow_offset_y", BatteryStatsConfig.shadow.offsetY)
                .apply();
    }

    private void setupOrderZones() {
        batteryOrderZones.setListener(this::onOrderChanged);
        batteryOrderZones.setOrder(BatteryStatsConfig.itemOrder);
    }

    private void onOrderChanged(String order, boolean temp, boolean pct, boolean volt, boolean cur, boolean power) {
        BatteryStatsConfig.itemOrder = order;
        BatteryStatsConfig.showTemperature = temp;
        BatteryStatsConfig.showPercentage = pct;
        BatteryStatsConfig.showVoltage = volt;
        BatteryStatsConfig.showCurrent = cur;
        BatteryStatsConfig.showPower = power;
        activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                .edit()
                .putString("battery_item_order", order)
                .putBoolean("battery_show_temperature", temp)
                .putBoolean("battery_show_percentage", pct)
                .putBoolean("battery_show_voltage", volt)
                .putBoolean("battery_show_current", cur)
                .putBoolean("battery_show_power", power)
                .apply();
        FloatingService.updateBatteryStatsInPlace();
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
                FloatingService.updateBatteryStatsInPlace();
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
