package exp.ftxt.ui;

import android.animation.ObjectAnimator;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import exp.ftxt.MainActivity;
import exp.ftxt.R;
import exp.ftxt.core.FloatingService;
import exp.ftxt.features.memory_stats.MemoryConfig;
import exp.ftxt.features.memory_stats.MemoryMonitor;
import exp.ftxt.shared.ui.ColorPickerDialog;
import exp.ftxt.shared.ui.SectionHelper;
import exp.ftxt.shared.ui.SliderLabelEditor;
import exp.ftxt.utils.PermissionHelper;

public class MemoryPanelController implements DefaultLifecycleObserver {

    private final MainActivity activity;

    private CheckBox memSwitch;
    private MemoryOrderZonesView memoryOrderZones;
    private CheckBox memValueOnlyCheck;
    private CheckBox memLockSwitch;
    private CheckBox memSafeArea;
    private CheckBox memShadowSwitch;
    private CheckBox memBgSwitch;
    private LinearLayout memShadowConfigContainer;
    private LinearLayout memBgConfigContainer;
    private SeekBar memSizeSeekBar;
    private SeekBar memShadowBlurSeekBar;
    private SeekBar memShadowOffsetXSeekBar;
    private SeekBar memShadowOffsetYSeekBar;
    private SeekBar memBgPaddingSeekBar;
    private SeekBar memBgOffsetXSeekBar;
    private SeekBar memBgOffsetYSeekBar;
    private SeekBar memBgMarginSeekBar;
    private SeekBar memBgRadiusSeekBar;
    private View memColorPreview;
    private View memLabelColorPreview;
    private View memSeparatorColorPreview;
    private View memShadowColorPreview;
    private View memBgColorPreview;
    private TextView memSizeLabel;
    private TextView memShadowBlurLabel;
    private TextView memShadowOffsetXLabel;
    private TextView memShadowOffsetYLabel;
    private TextView memBgPaddingLabel;
    private TextView memBgOffsetXLabel;
    private TextView memBgOffsetYLabel;
    private TextView memBgMarginLabel;
    private TextView memBgRadiusLabel;
    private TextView memIntervalValue;
    private PopupWindow intervalPopup;
    private MemoryPositionController memoryPositionController;

    private View memTabMonitor;
    private View memTabOverlay;
    private BottomNavigationView memBottomNav;
    private TextView memMonitorFbiText;
    private TextView memMonitorFbiText2;
    private TextView memMonitorRuntimeText;
    private TextView memMonitorSystemText;
    private TextView memMonitorRamTotalText;
    private TextView memMonitorRamUsedText;
    private TextView memMonitorRamUsedPercentText;
    private ProgressBar memMonitorRamBar;
    private TextView memMonitorStatusBadge;
    private Button memMonitorExportButton;
    private Button memMonitorCopyButton;
    private Button memMonitorToggleButton;
    private SwitchCompat memBgMonitorSwitch;
    private boolean manualMonitorActive = false;
    private int monitorLabelColor;

    private final Handler monitorHandler = new Handler(Looper.getMainLooper());
    private final Runnable monitorRunnable = new Runnable() {
        @Override
        public void run() {
            updateMonitorInfo();
            monitorHandler.postDelayed(this, 1000);
        }
    };

    public MemoryPanelController(MainActivity activity, View rootView) {
        this.activity = activity;
        bindViews(rootView);
        loadConfig();
        setupListeners();
        memoryPositionController = new MemoryPositionController(activity, rootView);
        activity.getLifecycle().addObserver(this);
    }

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        resumeMonitorPolling();
    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        stopMonitorPolling();
        stopManualMonitor();
    }

    public void onPanelShown() {
        if (memoryPositionController != null) {
            memoryPositionController.refresh();
        }
        if (memoryOrderZones != null) {
            memoryOrderZones.setOrder(MemoryConfig.itemOrder);
        }
        resumeMonitorPolling();
    }

    public void onPanelHidden() {
        stopMonitorPolling();
        stopManualMonitor();
    }

    public void showLoadPresetDialog() {
        if (memoryPositionController != null) {
            memoryPositionController.showLoadPresetDialog();
        }
    }

    public void cleanup() {
        stopMonitorPolling();
        stopManualMonitor();
        activity.getLifecycle().removeObserver(this);
        if (memoryPositionController != null) {
            memoryPositionController.cleanup();
            memoryPositionController = null;
        }
    }

    private void bindViews(View rootView) {
        memSwitch = rootView.findViewById(R.id.memSwitch);
        memoryOrderZones = rootView.findViewById(R.id.memoryOrderZones);
        memValueOnlyCheck = rootView.findViewById(R.id.memValueOnlyCheck);
        memLockSwitch = rootView.findViewById(R.id.memLockSwitch);
        memSafeArea = rootView.findViewById(R.id.memSafeArea);
        memShadowSwitch = rootView.findViewById(R.id.memShadowSwitch);
        memShadowConfigContainer = rootView.findViewById(R.id.shadowConfigMemory);
        memBgSwitch = rootView.findViewById(R.id.memBgSwitch);
        memBgConfigContainer = rootView.findViewById(R.id.bgConfigMemory);
        memSizeSeekBar = rootView.findViewById(R.id.memSizeSeekBar);
        memShadowBlurSeekBar = rootView.findViewById(R.id.memShadowBlurSeekBar);
        memShadowOffsetXSeekBar = rootView.findViewById(R.id.memShadowOffsetXSeekBar);
        memShadowOffsetYSeekBar = rootView.findViewById(R.id.memShadowOffsetYSeekBar);
        memBgPaddingSeekBar = rootView.findViewById(R.id.memBgPaddingSeekBar);
        memBgOffsetXSeekBar = rootView.findViewById(R.id.memBgOffsetXSeekBar);
        memBgOffsetYSeekBar = rootView.findViewById(R.id.memBgOffsetYSeekBar);
        memBgMarginSeekBar = rootView.findViewById(R.id.memBgMarginSeekBar);
        memBgRadiusSeekBar = rootView.findViewById(R.id.memBgRadiusSeekBar);
        memColorPreview = rootView.findViewById(R.id.memColorPreview);
        memLabelColorPreview = rootView.findViewById(R.id.memLabelColorPreview);
        memSeparatorColorPreview = rootView.findViewById(R.id.memSeparatorColorPreview);
        memShadowColorPreview = rootView.findViewById(R.id.memShadowColorPreview);
        memBgColorPreview = rootView.findViewById(R.id.memBgColorPreview);
        memSizeLabel = rootView.findViewById(R.id.memSizeLabel);
        memShadowBlurLabel = rootView.findViewById(R.id.memShadowBlurLabel);
        memShadowOffsetXLabel = rootView.findViewById(R.id.memShadowOffsetXLabel);
        memShadowOffsetYLabel = rootView.findViewById(R.id.memShadowOffsetYLabel);
        memBgPaddingLabel = rootView.findViewById(R.id.memBgPaddingLabel);
        memBgOffsetXLabel = rootView.findViewById(R.id.memBgOffsetXLabel);
        memBgOffsetYLabel = rootView.findViewById(R.id.memBgOffsetYLabel);
        memBgMarginLabel = rootView.findViewById(R.id.memBgMarginLabel);
        memBgRadiusLabel = rootView.findViewById(R.id.memBgRadiusLabel);
        memIntervalValue = rootView.findViewById(R.id.memIntervalValue);

        memTabMonitor = rootView.findViewById(R.id.memTabMonitor);
        memTabOverlay = rootView.findViewById(R.id.memTabOverlay);
        memBottomNav = rootView.findViewById(R.id.memBottomNav);
        memMonitorFbiText = rootView.findViewById(R.id.memMonitorFbiText);
        memMonitorFbiText2 = rootView.findViewById(R.id.memMonitorFbiText2);
        memMonitorRuntimeText = rootView.findViewById(R.id.memMonitorRuntimeText);
        memMonitorSystemText = rootView.findViewById(R.id.memMonitorSystemText);
        memMonitorRamTotalText = rootView.findViewById(R.id.memMonitorRamTotalText);
        memMonitorRamUsedText = rootView.findViewById(R.id.memMonitorRamUsedText);
        memMonitorRamUsedPercentText = rootView.findViewById(R.id.memMonitorRamUsedPercentText);
        memMonitorRamBar = rootView.findViewById(R.id.memMonitorRamBar);
        memMonitorStatusBadge = rootView.findViewById(R.id.memMonitorStatusBadge);
        monitorLabelColor = activity.getColor(R.color.mem_monitor_label);
        memMonitorExportButton = rootView.findViewById(R.id.memMonitorExportButton);
        memMonitorCopyButton = rootView.findViewById(R.id.memMonitorCopyButton);
        memMonitorToggleButton = rootView.findViewById(R.id.memMonitorToggleButton);
        memBgMonitorSwitch = rootView.findViewById(R.id.memBgMonitorSwitch);

        View sectionPosition = rootView.findViewById(R.id.mem_sectionPosition);
        TextView sectionPositionHeader = rootView.findViewById(R.id.mem_sectionPositionHeader);
        SectionHelper.setupCollapsible(sectionPositionHeader, sectionPosition);

        View sectionShadow = rootView.findViewById(R.id.mem_sectionShadow);
        TextView sectionShadowHeader = rootView.findViewById(R.id.mem_sectionShadowHeader);
        SectionHelper.setupCollapsible(sectionShadowHeader, sectionShadow);

        View sectionBackground = rootView.findViewById(R.id.mem_sectionBackground);
        TextView sectionBackgroundHeader = rootView.findViewById(R.id.mem_sectionBackgroundHeader);
        SectionHelper.setupCollapsible(sectionBackgroundHeader, sectionBackground);

        View sectionDisplay = rootView.findViewById(R.id.mem_sectionDisplay);
        TextView sectionDisplayHeader = rootView.findViewById(R.id.mem_sectionDisplayHeader);
        SectionHelper.setupCollapsible(sectionDisplayHeader, sectionDisplay);
    }

    private void loadConfig() {
        memSwitch.setChecked(MemoryConfig.enabled);
        activity.applyCheckboxTint(memSwitch, MemoryConfig.enabled);
        memoryOrderZones.setOrder(MemoryConfig.itemOrder);
        memSizeSeekBar.setProgress((int) MemoryConfig.size);
        memColorPreview.setBackgroundColor(MemoryConfig.color);
        memLabelColorPreview.setBackgroundColor(MemoryConfig.labelColor);
        memSeparatorColorPreview.setBackgroundColor(MemoryConfig.separatorColor);
        memBgSwitch.setChecked(MemoryConfig.bg.enabled);
        activity.applyCheckboxTint(memBgSwitch, MemoryConfig.bg.enabled);
        memBgConfigContainer.setVisibility(MemoryConfig.bg.enabled ? View.VISIBLE : View.GONE);
        memBgPaddingSeekBar.setProgress(MemoryConfig.bg.padding);
        memBgOffsetXSeekBar.setProgress(MemoryConfig.bg.offsetX + 60);
        memBgOffsetYSeekBar.setProgress(MemoryConfig.bg.offsetY + 60);
        memBgMarginSeekBar.setProgress(MemoryConfig.bg.margin);
        memBgRadiusSeekBar.setProgress(MemoryConfig.bg.radius);
        memShadowSwitch.setChecked(MemoryConfig.shadow.enabled);
        activity.applyCheckboxTint(memShadowSwitch, MemoryConfig.shadow.enabled);
        memShadowConfigContainer.setVisibility(MemoryConfig.shadow.enabled ? View.VISIBLE : View.GONE);
        memShadowBlurSeekBar.setProgress((int) MemoryConfig.shadow.blur);
        memShadowOffsetXSeekBar.setProgress((int) MemoryConfig.shadow.offsetX + 60);
        memShadowOffsetYSeekBar.setProgress((int) MemoryConfig.shadow.offsetY + 60);
        memLockSwitch.setChecked(MemoryConfig.touchPassthrough);
        activity.applyCheckboxTint(memLockSwitch, MemoryConfig.touchPassthrough);
        memValueOnlyCheck.setChecked(MemoryConfig.showOnlyValue);
        memSafeArea.setChecked(MemoryConfig.safeArea);
        memBgMonitorSwitch.setChecked(MemoryConfig.backgroundMonitor);
        applySwitchTint(memBgMonitorSwitch, MemoryConfig.backgroundMonitor);
        updateMonitorToggleButton();
        memSizeLabel.setText("Ukuran Teks: " + (int) MemoryConfig.size);
        memBgPaddingLabel.setText("Ukuran Background: " + MemoryConfig.bg.padding);
        memBgOffsetXLabel.setText("Offset X: " + MemoryConfig.bg.offsetX);
        memBgOffsetYLabel.setText("Offset Y: " + MemoryConfig.bg.offsetY);
        memBgColorPreview.setBackgroundColor(MemoryConfig.bg.color);
        memBgMarginLabel.setText("Margin: " + MemoryConfig.bg.margin);
        memBgRadiusLabel.setText("Radius: " + MemoryConfig.bg.radius);
        memShadowBlurLabel.setText("Blur Shadow: " + (int) MemoryConfig.shadow.blur);
        memShadowOffsetXLabel.setText("Shadow X: " + (int) MemoryConfig.shadow.offsetX);
        memShadowOffsetYLabel.setText("Shadow Y: " + (int) MemoryConfig.shadow.offsetY);
        memShadowColorPreview.setBackgroundColor(MemoryConfig.shadow.color);
        memIntervalValue.setText(formatIntervalValue(MemoryConfig.updateInterval));
    }

    private void setupListeners() {
        memSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !PermissionHelper.hasOverlayPermission(activity)) {
                memSwitch.setChecked(false);
                activity.applyCheckboxTint(memSwitch, false);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putBoolean("mem_enabled", false).apply();
                return;
            }

            MemoryConfig.enabled = isChecked;
            activity.applyCheckboxTint(memSwitch, isChecked);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("mem_enabled", isChecked).apply();

            if (isChecked) {
                if (FloatingService.instance != null) {
                    FloatingService.startModule(FloatingService.memoryModule());
                } else {
                    activity.startService(new Intent(activity, FloatingService.class));
                }
            } else {
                FloatingService.stopModule(FloatingService.memoryModule());
            }
        });

        memValueOnlyCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            MemoryConfig.showOnlyValue = isChecked;
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("mem_show_only_value", isChecked).apply();
            FloatingService.updateMemoryInPlace();
        });

        memLockSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            MemoryConfig.touchPassthrough = isChecked;
            activity.applyCheckboxTint(memLockSwitch, isChecked);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("mem_lock", isChecked).apply();
            FloatingService.updateTouchFlagsForModule(FloatingService.memoryModule());
        });

        memSafeArea.setOnCheckedChangeListener((buttonView, isChecked) -> {
            MemoryConfig.safeArea = isChecked;
            activity.applyCheckboxTint(memSafeArea, isChecked);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("mem_safe_area", isChecked).apply();
        });

        memSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (progress < 5) { progress = 5; sb.setProgress(progress); }
                if (progress > 140) { progress = 140; sb.setProgress(progress); }
                MemoryConfig.size = progress;
                memSizeLabel.setText("Ukuran Teks: " + progress);
                FloatingService.updateSizeForModule(FloatingService.memoryModule(), MemoryConfig.size);
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        memColorPreview.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Pilih Warna Nilai", MemoryConfig.color, color -> {
                MemoryConfig.color = color;
                memColorPreview.setBackgroundColor(color);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("mem_color", color).apply();
                FloatingService.updateColorForModule(FloatingService.memoryModule(), MemoryConfig.color);
            });
        });

        memLabelColorPreview.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Pilih Warna Label", MemoryConfig.labelColor, color -> {
                MemoryConfig.labelColor = color;
                memLabelColorPreview.setBackgroundColor(color);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("mem_label_color", color).apply();
                FloatingService.updateLabelColorForModule(FloatingService.memoryModule(), MemoryConfig.labelColor);
            });
        });

        memSeparatorColorPreview.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Pilih Warna Pemisah", MemoryConfig.separatorColor, color -> {
                MemoryConfig.separatorColor = color;
                memSeparatorColorPreview.setBackgroundColor(color);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("mem_separator_color", color).apply();
                FloatingService.updateSeparatorColorForModule(FloatingService.memoryModule(), MemoryConfig.separatorColor);
            });
        });

        memShadowSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            MemoryConfig.shadow.enabled = isChecked;
            activity.applyCheckboxTint(memShadowSwitch, isChecked);
            memShadowConfigContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("mem_shadow_enabled", isChecked).apply();
            saveMemShadowPrefs();
            FloatingService.updateShadowForModule(FloatingService.memoryModule());
        });

        memShadowColorPreview.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Warna Shadow", MemoryConfig.shadow.color, color -> {
                MemoryConfig.shadow.color = color;
                memShadowColorPreview.setBackgroundColor(color);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("mem_shadow_color", color).apply();
                FloatingService.updateShadowForModule(FloatingService.memoryModule());
            });
        });

        memShadowBlurSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                MemoryConfig.shadow.blur = progress;
                memShadowBlurLabel.setText("Blur Shadow: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putFloat("mem_shadow_blur", (float) progress).apply();
                FloatingService.updateShadowForModule(FloatingService.memoryModule());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        memShadowOffsetXSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                MemoryConfig.shadow.offsetX = offset;
                memShadowOffsetXLabel.setText("Shadow X: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putFloat("mem_shadow_offset_x", (float) offset).apply();
                FloatingService.updateShadowForModule(FloatingService.memoryModule());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        memShadowOffsetYSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                MemoryConfig.shadow.offsetY = offset;
                memShadowOffsetYLabel.setText("Shadow Y: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putFloat("mem_shadow_offset_y", (float) offset).apply();
                FloatingService.updateShadowForModule(FloatingService.memoryModule());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        memBgSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            MemoryConfig.bg.enabled = isChecked;
            activity.applyCheckboxTint(memBgSwitch, isChecked);
            memBgConfigContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("mem_bg_enabled", isChecked).apply();
            FloatingService.updateBackgroundForModule(FloatingService.memoryModule());
        });

        memBgColorPreview.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Warna Background", MemoryConfig.bg.color, color -> {
                MemoryConfig.bg.color = color;
                memBgColorPreview.setBackgroundColor(color);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("mem_bg_color", color).apply();
                FloatingService.updateBackgroundForModule(FloatingService.memoryModule());
            });
        });

        memBgPaddingSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (progress < 0) progress = 0;
                MemoryConfig.bg.padding = progress;
                memBgPaddingLabel.setText("Ukuran Background: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("mem_bg_padding", progress).apply();
                FloatingService.updateBackgroundForModule(FloatingService.memoryModule());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        memBgOffsetXSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                MemoryConfig.bg.offsetX = offset;
                memBgOffsetXLabel.setText("Offset X: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("mem_bg_offset_x", offset).apply();
                FloatingService.updateBackgroundForModule(FloatingService.memoryModule());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        memBgOffsetYSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                MemoryConfig.bg.offsetY = offset;
                memBgOffsetYLabel.setText("Offset Y: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("mem_bg_offset_y", offset).apply();
                FloatingService.updateBackgroundForModule(FloatingService.memoryModule());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        memBgMarginSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                MemoryConfig.bg.margin = progress;
                memBgMarginLabel.setText("Margin: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("mem_bg_margin", progress).apply();
                FloatingService.updateBackgroundForModule(FloatingService.memoryModule());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        memBgRadiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                MemoryConfig.bg.radius = progress;
                memBgRadiusLabel.setText("Radius: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("mem_bg_radius", progress).apply();
                FloatingService.updateBackgroundForModule(FloatingService.memoryModule());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        memSizeLabel.setOnClickListener(v ->
                SliderLabelEditor.showSliderEditor(activity, "Ukuran Teks", memSizeSeekBar, 140, memSizeLabel, "Ukuran Teks: "));
        memBgPaddingLabel.setOnClickListener(v ->
                SliderLabelEditor.showSliderEditor(activity, "Ukuran Background", memBgPaddingSeekBar, 80, memBgPaddingLabel, "Ukuran Background: "));
        memBgOffsetXLabel.setOnClickListener(v ->
                SliderLabelEditor.showOffsetEditor(activity, "Offset X", memBgOffsetXSeekBar, memBgOffsetXLabel, "Offset X: "));
        memBgOffsetYLabel.setOnClickListener(v ->
                SliderLabelEditor.showOffsetEditor(activity, "Offset Y", memBgOffsetYSeekBar, memBgOffsetYLabel, "Offset Y: "));
        memBgMarginLabel.setOnClickListener(v ->
                SliderLabelEditor.showSliderEditor(activity, "Margin Background", memBgMarginSeekBar, 30, memBgMarginLabel, "Margin: "));
        memBgRadiusLabel.setOnClickListener(v ->
                SliderLabelEditor.showSliderEditor(activity, "Radius Background", memBgRadiusSeekBar, 50, memBgRadiusLabel, "Radius: "));
        memShadowBlurLabel.setOnClickListener(v ->
                SliderLabelEditor.showSliderEditor(activity, "Blur Shadow", memShadowBlurSeekBar, 50, memShadowBlurLabel, "Blur Shadow: "));
        memShadowOffsetXLabel.setOnClickListener(v ->
                SliderLabelEditor.showOffsetEditor(activity, "Shadow X", memShadowOffsetXSeekBar, memShadowOffsetXLabel, "Shadow X: "));
        memShadowOffsetYLabel.setOnClickListener(v ->
                SliderLabelEditor.showOffsetEditor(activity, "Shadow Y", memShadowOffsetYSeekBar, memShadowOffsetYLabel, "Shadow Y: "));

        setupIntervalListeners();
        setupOrderZones();
        setupMonitorTab();
    }

    private void setupMonitorTab() {
        memBottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.memTabNavMonitor) {
                memTabMonitor.setVisibility(View.VISIBLE);
                memTabOverlay.setVisibility(View.GONE);
                resumeMonitorPolling();
            } else {
                if (!MemoryConfig.backgroundMonitor) {
                    showBackgroundRequiredDialog();
                    blinkBgSwitch();
                    return false;
                }
                memTabOverlay.setVisibility(View.VISIBLE);
                memTabMonitor.setVisibility(View.GONE);
                stopMonitorPolling();
            }
            return true;
        });
        memMonitorToggleButton.setOnClickListener(v -> handleMonitorToggle());
        memBgMonitorSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> handleBgMonitorSwitch(isChecked));
        memMonitorExportButton.setOnClickListener(v -> exportMemorySnapshot());
        memMonitorCopyButton.setOnClickListener(v -> copyToClipboard());
        memBottomNav.setSelectedItemId(R.id.memTabNavMonitor);
    }

    private void handleMonitorToggle() {
        if (MemoryConfig.backgroundMonitor) {
            showStopBackgroundDialog();
        } else if (manualMonitorActive) {
            stopManualMonitor();
        } else {
            startManualMonitor();
        }
    }

    private void startManualMonitor() {
        manualMonitorActive = true;
        MemoryMonitor.start(activity);
        updateMonitorToggleButton();
        updateMonitorInfo();
        resumeMonitorPolling();
    }

    private void stopManualMonitor() {
        if (!manualMonitorActive) return;
        manualMonitorActive = false;
        MemoryMonitor.stop();
        updateMonitorToggleButton();
        updateMonitorInfo();
    }

    private void handleBgMonitorSwitch(boolean isChecked) {
        MemoryConfig.backgroundMonitor = isChecked;
        applySwitchTint(memBgMonitorSwitch, isChecked);
        activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                .edit().putBoolean("mem_bg_monitor", isChecked).apply();

        if (isChecked) {
            manualMonitorActive = false;
            FloatingService.setBackgroundMonitorEnabled(true);
            if (FloatingService.instance == null) {
                activity.startService(new Intent(activity, FloatingService.class));
            }
            updateMonitorToggleButton();
            updateMonitorInfo();
            resumeMonitorPolling();
        } else {
            manualMonitorActive = false;
            FloatingService.setBackgroundMonitorEnabled(false);
            disableOverlayIfRunning();
            updateMonitorToggleButton();
            updateMonitorInfo();
        }
    }

    private void disableOverlayIfRunning() {
        if (!MemoryConfig.enabled) return;
        MemoryConfig.enabled = false;
        activity.applyCheckboxTint(memSwitch, false);
        memSwitch.setChecked(false);
        activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                .edit().putBoolean("mem_enabled", false).apply();
        FloatingService.stopModule(FloatingService.memoryModule());
    }

    private void showStopBackgroundDialog() {
        new AlertDialog.Builder(activity)
                .setTitle("Hentikan Pemantauan")
                .setMessage("Hentikan pemantauan? Pemantauan latar belakang akan dimatikan.")
                .setPositiveButton("Ya", (dialog, which) -> {
                    memBgMonitorSwitch.setChecked(false);
                })
                .setNegativeButton("Tidak", null)
                .show();
    }

    private void showBackgroundRequiredDialog() {
        new AlertDialog.Builder(activity)
                .setTitle("Pemantauan Latar Belakang")
                .setMessage("Pemantauan latar belakang harus dinyalakan")
                .setPositiveButton("OK", null)
                .show();
    }

    private void blinkBgSwitch() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(memBgMonitorSwitch, "alpha", 1f, 0.2f, 1f, 0.2f, 1f);
        animator.setDuration(900);
        animator.start();
    }

    private void updateMonitorToggleButton() {
        if (memMonitorToggleButton == null) return;
        if (MemoryConfig.backgroundMonitor || manualMonitorActive) {
            memMonitorToggleButton.setText("Hentikan Pemantauan");
            memMonitorToggleButton.setBackgroundTintList(
                    ColorStateList.valueOf(activity.getColor(R.color.mem_monitor_stop)));
        } else {
            memMonitorToggleButton.setText("Mulai Pemantauan");
            memMonitorToggleButton.setBackgroundTintList(
                    ColorStateList.valueOf(activity.getColor(R.color.mem_monitor_header)));
        }
        updateMonitorStatusBadge();
    }

    private void updateMonitorStatusBadge() {
        if (memMonitorStatusBadge == null) return;
        boolean running = MemoryConfig.backgroundMonitor || manualMonitorActive;
        memMonitorStatusBadge.setText(running ? "● Berjalan" : "● Berhenti");
        memMonitorStatusBadge.setTextColor(activity.getColor(running
                ? R.color.mem_monitor_active : R.color.mem_monitor_stopped));
        memMonitorStatusBadge.setBackgroundResource(running
                ? R.drawable.mem_badge_active_bg : R.drawable.mem_badge_stopped_bg);
    }

    private void resumeMonitorPolling() {
        if (memTabMonitor == null || memTabMonitor.getVisibility() != View.VISIBLE) return;
        monitorHandler.removeCallbacks(monitorRunnable);
        monitorHandler.post(monitorRunnable);
    }

    private void stopMonitorPolling() {
        monitorHandler.removeCallbacks(monitorRunnable);
    }

    private void updateMonitorInfo() {
        if (memMonitorFbiText == null) return;
        MemoryMonitor.Snapshot s = MemoryMonitor.getLastSnapshot();

        SpannableStringBuilder fbi = new SpannableStringBuilder();
        appendLine(fbi, "Java Heap", formatMb(s.javaKb), 14);
        appendLine(fbi, "Native Heap", formatMb(s.nativeKb), 14);
        appendLine(fbi, "Graphics", formatMb(s.graphicsKb), 14);
        appendLine(fbi, "Other PSS", formatMb(s.otherPssKb), 14);
        appendLine(fbi, "Total Proses", formatMb(s.totalKb), 14);
        appendLine(fbi, "Private Dirty", formatMb(s.privateDirtyKb), 14);
        appendLine(fbi, "Private Clean", formatMb(s.privateCleanKb), 14);
        memMonitorFbiText.setText(fbi);

        SpannableStringBuilder fbi2 = new SpannableStringBuilder();
        appendLine(fbi2, "Shared Dirty", formatMb(s.sharedDirtyKb), 14);
        appendLine(fbi2, "Swapped", formatMb(s.swappedKb), 14);
        appendLine(fbi2, "Code", formatMb(s.codeKb), 14);
        appendLine(fbi2, "Stack", formatMb(s.stackKb), 14);
        appendLine(fbi2, "System", formatMb(s.systemKb), 14);
        appendLine(fbi2, "Private Other", formatMb(s.privateOtherKb), 14);
        appendLine(fbi2, "System Other", formatMb(s.systemOtherKb), 14);
        memMonitorFbiText2.setText(fbi2);

        SpannableStringBuilder runtime = new SpannableStringBuilder();
        appendLine(runtime, "Heap Terpakai", formatMb(s.heapUsedKb));
        appendLine(runtime, "Heap Bebas", formatMb(s.heapFreeKb));
        appendLine(runtime, "Heap Maksimum", formatMb(s.heapMaxKb));
        memMonitorRuntimeText.setText(runtime);

        long totalRam = s.totalRamKb;
        long usedRam = Math.max(0, totalRam - s.availRamKb);
        int ramPercent = totalRam > 0 ? (int) Math.round(usedRam * 100.0 / totalRam) : 0;
        memMonitorRamTotalText.setText(formatMb(s.availRamKb));
        memMonitorRamUsedText.setText(formatMb(usedRam));
        memMonitorRamUsedPercentText.setText(ramPercent + "%");
        memMonitorRamBar.setProgress(ramPercent);

        SpannableStringBuilder system = new SpannableStringBuilder();
        appendLine(system, "Total RAM", formatMb(totalRam));
        appendLine(system, "Cached", s.cachedKb >= 0 ? formatMb(s.cachedKb) : "—");
        memMonitorSystemText.setText(system);
    }

    private void appendLine(SpannableStringBuilder sb, String label, String value) {
        appendLine(sb, label, value, 16);
    }

    private void appendLine(SpannableStringBuilder sb, String label, String value, int padWidth) {
        String padded = String.format(Locale.US, "%-" + padWidth + "s", label);
        int start = sb.length();
        sb.append(padded).append(value).append("\n");
        sb.setSpan(new ForegroundColorSpan(monitorLabelColor),
                start, start + padded.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private String formatMb(long kb) {
        return String.format(Locale.US, "%.1f MB", kb / 1024f);
    }

    private void applySwitchTint(SwitchCompat sw, boolean isChecked) {
        int track = activity.getColor(isChecked
                ? R.color.mem_monitor_active : R.color.mem_monitor_stopped);
        sw.setThumbTintList(ColorStateList.valueOf(activity.getColor(android.R.color.white)));
        sw.setTrackTintList(ColorStateList.valueOf(track));
    }

    private void copyToClipboard() {
        if (memMonitorFbiText == null) return;
        StringBuilder sb = new StringBuilder();
        sb.append("Proses FTxT\n").append(combineFbiColumns());
        sb.append("\n\nRuntime Java\n").append(memMonitorRuntimeText.getText());
        sb.append("\n\nRAM Sistem\n").append(memMonitorSystemText.getText());
        ClipboardManager cm = (ClipboardManager) activity.getSystemService(android.content.Context.CLIPBOARD_SERVICE);
        if (cm == null) return;
        cm.setPrimaryClip(ClipData.newPlainText("FTxT Info Memori", sb.toString()));
        Toast.makeText(activity, "Disalin ke clipboard", Toast.LENGTH_SHORT).show();
    }

    private String combineFbiColumns() {
        String left = memMonitorFbiText.getText().toString().trim();
        String right = memMonitorFbiText2.getText().toString().trim();
        if (left.isEmpty()) return right;
        if (right.isEmpty()) return left;
        return left + "\n" + right;
    }

    private void exportMemorySnapshot() {
        String exportTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                .format(new Date());
        MemoryMonitor.Snapshot[] history = MemoryMonitor.getHistory();
        StringBuilder sb = new StringBuilder();
        sb.append("FTxT - Info Memori (Riwayat 20 Detik)\n");
        sb.append("Ekspor: ").append(exportTime).append("\n");
        sb.append("Jumlah snapshot: ").append(history.length).append("\n\n");
        int index = 1;
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        for (MemoryMonitor.Snapshot snap : history) {
            String time = timeFormat.format(new Date(snap.time));
            sb.append("--- Snapshot ").append(index++)
                    .append("/").append(history.length)
                    .append(" (").append(time).append(") ---\n");
            sb.append("Java Heap (Dalvik): ").append(formatMb(snap.javaKb)).append("\n");
            sb.append("Native Heap       : ").append(formatMb(snap.nativeKb)).append("\n");
            sb.append("Graphics          : ").append(formatMb(snap.graphicsKb)).append("\n");
            sb.append("Other PSS         : ").append(formatMb(snap.otherPssKb)).append("\n");
            sb.append("Total Proses (PSS): ").append(formatMb(snap.totalKb)).append("\n");
            sb.append("Private Dirty     : ").append(formatMb(snap.privateDirtyKb)).append("\n");
            sb.append("Private Clean     : ").append(formatMb(snap.privateCleanKb)).append("\n");
            sb.append("Shared Dirty      : ").append(formatMb(snap.sharedDirtyKb)).append("\n");
            sb.append("Swapped           : ").append(formatMb(snap.swappedKb)).append("\n");
            sb.append("Code              : ").append(formatMb(snap.codeKb)).append("\n");
            sb.append("Stack             : ").append(formatMb(snap.stackKb)).append("\n");
            sb.append("System            : ").append(formatMb(snap.systemKb)).append("\n");
            sb.append("Private Other     : ").append(formatMb(snap.privateOtherKb)).append("\n");
            sb.append("System Other      : ").append(formatMb(snap.systemOtherKb)).append("\n");
            sb.append("Heap Terpakai     : ").append(formatMb(snap.heapUsedKb)).append("\n");
            sb.append("Heap Bebas        : ").append(formatMb(snap.heapFreeKb)).append("\n");
            sb.append("Heap Maksimum     : ").append(formatMb(snap.heapMaxKb)).append("\n");
            sb.append("RAM Total         : ").append(formatMb(snap.totalRamKb)).append("\n");
            sb.append("RAM Tersedia      : ").append(formatMb(snap.availRamKb)).append("\n");
            sb.append("RAM Cached        : ").append(snap.cachedKb >= 0 ? formatMb(snap.cachedKb) : "—").append("\n\n");
        }

        String fileName = "FTxT_memori_" + System.currentTimeMillis() + ".txt";
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

    private void setupOrderZones() {
        memoryOrderZones.setListener(this::onOrderChanged);
        memoryOrderZones.setOrder(MemoryConfig.itemOrder);
    }

    private void onOrderChanged(String order, boolean java, boolean nativeHeap, boolean graphics, boolean total) {
        MemoryConfig.itemOrder = order;
        MemoryConfig.showJavaHeap = java;
        MemoryConfig.showNativeHeap = nativeHeap;
        MemoryConfig.showGraphics = graphics;
        MemoryConfig.showTotal = total;
        activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                .edit()
                .putString("mem_item_order", order)
                .putBoolean("mem_show_java", java)
                .putBoolean("mem_show_native", nativeHeap)
                .putBoolean("mem_show_graphics", graphics)
                .putBoolean("mem_show_total", total)
                .apply();
        FloatingService.updateMemoryInPlace();
    }

    private void saveMemShadowPrefs() {
        activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE).edit()
                .putInt("mem_shadow_color", MemoryConfig.shadow.color)
                .putFloat("mem_shadow_blur", MemoryConfig.shadow.blur)
                .putFloat("mem_shadow_offset_x", MemoryConfig.shadow.offsetX)
                .putFloat("mem_shadow_offset_y", MemoryConfig.shadow.offsetY)
                .apply();
    }

    private static final float[] INTERVAL_STEPS = {0.2f, 0.5f, 0.75f, 1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f};

    private void setupIntervalListeners() {
        memIntervalValue.setOnClickListener(v -> showIntervalPopup(v));
    }

    private void showIntervalPopup(View anchor) {
        if (intervalPopup != null && intervalPopup.isShowing()) {
            intervalPopup.dismiss();
            return;
        }

        int currentIdx = -1;
        for (int i = 0; i < INTERVAL_STEPS.length; i++) {
            if (INTERVAL_STEPS[i] == MemoryConfig.updateInterval) {
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
                MemoryConfig.updateInterval = INTERVAL_STEPS[idx];
                updateIntervalDisplay();
                FloatingService.updateMemoryInPlace();
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
        memIntervalValue.setText(formatIntervalValue(MemoryConfig.updateInterval));
        activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                .edit().putFloat("mem_update_interval", MemoryConfig.updateInterval).apply();
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
