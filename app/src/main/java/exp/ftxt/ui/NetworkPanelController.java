package exp.ftxt.ui;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.CheckBox;
import android.widget.TextView;

import exp.ftxt.MainActivity;
import exp.ftxt.R;
import exp.ftxt.core.FloatingService;
import exp.ftxt.features.network_stats.NetworkConfig;
import exp.ftxt.shared.ui.ColorPickerDialog;
import exp.ftxt.shared.ui.SectionHelper;
import exp.ftxt.shared.ui.SliderLabelEditor;
import exp.ftxt.utils.PermissionHelper;

public class NetworkPanelController {

    private final MainActivity activity;

    private CheckBox networkSwitch;
    private CheckBox networkValueOnlyCheck;
    private SeekBar networkSizeSeekBar;
    private View networkColorPreview;
    private View networkLabelColorPreview;
    private CheckBox networkShadowSwitch;
    private LinearLayout networkShadowConfigContainer;
    private View networkShadowColorPreview;
    private SeekBar networkShadowBlurSeekBar;
    private SeekBar networkShadowOffsetXSeekBar;
    private SeekBar networkShadowOffsetYSeekBar;
    private CheckBox networkLockSwitch;
    private CheckBox networkSafeArea;
    private CheckBox networkBgSwitch;
    private LinearLayout networkBgConfigContainer;
    private View networkBgColorPreview;
    private SeekBar networkBgPaddingSeekBar;
    private SeekBar networkBgOffsetXSeekBar;
    private SeekBar networkBgOffsetYSeekBar;
    private SeekBar networkBgMarginSeekBar;
    private SeekBar networkBgRadiusSeekBar;
    private TextView networkSizeLabel, networkBgPaddingLabel, networkBgOffsetXLabel, networkBgOffsetYLabel;
    private TextView networkBgMarginLabel, networkBgRadiusLabel;
    private TextView networkShadowBlurLabel, networkShadowOffsetXLabel, networkShadowOffsetYLabel;
    private NetworkPositionController networkPositionController;
    private TextView networkIntervalValue;
    private PopupWindow intervalPopup;

    public NetworkPanelController(MainActivity activity, View rootView) {
        this.activity = activity;
        bindViews(rootView);
        loadConfig();
        setupListeners();
        networkPositionController = new NetworkPositionController(activity, rootView);
    }

    public void onPanelShown() {
        if (networkPositionController != null) {
            networkPositionController.refresh();
        }
    }

    public void showLoadPresetDialog() {
        if (networkPositionController != null) {
            networkPositionController.showLoadPresetDialog();
        }
    }

    public void cleanup() {
        if (networkPositionController != null) {
            networkPositionController.cleanup();
            networkPositionController = null;
        }
    }

    private void bindViews(View rootView) {
        networkSwitch = rootView.findViewById(R.id.networkSwitch);
        networkValueOnlyCheck = rootView.findViewById(R.id.networkValueOnlyCheck);
        networkSizeSeekBar = rootView.findViewById(R.id.networkSizeSeekBar);
        networkColorPreview = rootView.findViewById(R.id.networkColorPreview);
        networkLabelColorPreview = rootView.findViewById(R.id.networkLabelColorPreview);
        networkShadowSwitch = rootView.findViewById(R.id.networkShadowSwitch);
        networkShadowConfigContainer = rootView.findViewById(R.id.shadowConfigNetwork);
        networkShadowColorPreview = rootView.findViewById(R.id.networkShadowColorPreview);
        networkShadowBlurSeekBar = rootView.findViewById(R.id.networkShadowBlurSeekBar);
        networkShadowOffsetXSeekBar = rootView.findViewById(R.id.networkShadowOffsetXSeekBar);
        networkShadowOffsetYSeekBar = rootView.findViewById(R.id.networkShadowOffsetYSeekBar);
        networkLockSwitch = rootView.findViewById(R.id.networkLockSwitch);
        networkSafeArea = rootView.findViewById(R.id.networkSafeArea);
        networkBgSwitch = rootView.findViewById(R.id.networkBgSwitch);
        networkBgConfigContainer = rootView.findViewById(R.id.bgConfigNetwork);
        networkBgColorPreview = rootView.findViewById(R.id.networkBgColorPreview);
        networkBgPaddingSeekBar = rootView.findViewById(R.id.networkBgPaddingSeekBar);
        networkBgOffsetXSeekBar = rootView.findViewById(R.id.networkBgOffsetXSeekBar);
        networkBgOffsetYSeekBar = rootView.findViewById(R.id.networkBgOffsetYSeekBar);
        networkBgMarginSeekBar = rootView.findViewById(R.id.networkBgMarginSeekBar);
        networkBgRadiusSeekBar = rootView.findViewById(R.id.networkBgRadiusSeekBar);
        networkSizeLabel = rootView.findViewById(R.id.networkSizeLabel);
        networkBgPaddingLabel = rootView.findViewById(R.id.networkBgPaddingLabel);
        networkBgOffsetXLabel = rootView.findViewById(R.id.networkBgOffsetXLabel);
        networkBgOffsetYLabel = rootView.findViewById(R.id.networkBgOffsetYLabel);
        networkBgMarginLabel = rootView.findViewById(R.id.networkBgMarginLabel);
        networkBgRadiusLabel = rootView.findViewById(R.id.networkBgRadiusLabel);
        networkShadowBlurLabel = rootView.findViewById(R.id.networkShadowBlurLabel);
        networkShadowOffsetXLabel = rootView.findViewById(R.id.networkShadowOffsetXLabel);
        networkShadowOffsetYLabel = rootView.findViewById(R.id.networkShadowOffsetYLabel);
        networkIntervalValue = rootView.findViewById(R.id.networkIntervalValue);

        View sectionDisplay = rootView.findViewById(R.id.network_sectionDisplay);
        TextView sectionDisplayHeader = rootView.findViewById(R.id.network_sectionDisplayHeader);
        SectionHelper.setupCollapsible(sectionDisplayHeader, sectionDisplay);

        View sectionPosition = rootView.findViewById(R.id.network_sectionPosition);
        TextView sectionPositionHeader = rootView.findViewById(R.id.network_sectionPositionHeader);
        SectionHelper.setupCollapsible(sectionPositionHeader, sectionPosition);

        View sectionShadow = rootView.findViewById(R.id.network_sectionShadow);
        TextView sectionShadowHeader = rootView.findViewById(R.id.network_sectionShadowHeader);
        SectionHelper.setupCollapsible(sectionShadowHeader, sectionShadow);

        View sectionBackground = rootView.findViewById(R.id.network_sectionBackground);
        TextView sectionBackgroundHeader = rootView.findViewById(R.id.network_sectionBackgroundHeader);
        SectionHelper.setupCollapsible(sectionBackgroundHeader, sectionBackground);
    }

    private void loadConfig() {
        networkSwitch.setChecked(NetworkConfig.enabled);
        activity.applyCheckboxTint(networkSwitch, NetworkConfig.enabled);
        networkSizeSeekBar.setProgress((int) NetworkConfig.size);
        networkColorPreview.setBackgroundColor(NetworkConfig.color);
        networkLabelColorPreview.setBackgroundColor(NetworkConfig.labelColor);
        networkBgSwitch.setChecked(NetworkConfig.bg.enabled);
        activity.applyCheckboxTint(networkBgSwitch, NetworkConfig.bg.enabled);
        networkBgConfigContainer.setVisibility(NetworkConfig.bg.enabled ? View.VISIBLE : View.GONE);
        networkBgPaddingSeekBar.setProgress(NetworkConfig.bg.padding);
        networkBgOffsetXSeekBar.setProgress(NetworkConfig.bg.offsetX + 60);
        networkBgOffsetYSeekBar.setProgress(NetworkConfig.bg.offsetY + 60);
        networkBgMarginSeekBar.setProgress(NetworkConfig.bg.margin);
        networkBgRadiusSeekBar.setProgress(NetworkConfig.bg.radius);
        networkShadowSwitch.setChecked(NetworkConfig.shadow.enabled);
        activity.applyCheckboxTint(networkShadowSwitch, NetworkConfig.shadow.enabled);
        networkShadowConfigContainer.setVisibility(NetworkConfig.shadow.enabled ? View.VISIBLE : View.GONE);
        networkShadowBlurSeekBar.setProgress((int) NetworkConfig.shadow.blur);
        networkShadowOffsetXSeekBar.setProgress((int) NetworkConfig.shadow.offsetX + 60);
        networkShadowOffsetYSeekBar.setProgress((int) NetworkConfig.shadow.offsetY + 60);
        networkLockSwitch.setChecked(NetworkConfig.touchPassthrough);
        activity.applyCheckboxTint(networkLockSwitch, NetworkConfig.touchPassthrough);
        networkValueOnlyCheck.setChecked(NetworkConfig.showOnlyValue);
        networkSafeArea.setChecked(NetworkConfig.safeArea);
        networkSizeLabel.setText("Ukuran Teks: " + (int) NetworkConfig.size);
        networkBgPaddingLabel.setText("Ukuran Background: " + NetworkConfig.bg.padding);
        networkBgOffsetXLabel.setText("Offset X: " + NetworkConfig.bg.offsetX);
        networkBgOffsetYLabel.setText("Offset Y: " + NetworkConfig.bg.offsetY);
        networkBgColorPreview.setBackgroundColor(NetworkConfig.bg.color);
        networkBgMarginLabel.setText("Margin: " + NetworkConfig.bg.margin);
        networkBgRadiusLabel.setText("Radius: " + NetworkConfig.bg.radius);
        networkShadowBlurLabel.setText("Blur Shadow: " + (int) NetworkConfig.shadow.blur);
        networkShadowOffsetXLabel.setText("Shadow X: " + (int) NetworkConfig.shadow.offsetX);
        networkShadowOffsetYLabel.setText("Shadow Y: " + (int) NetworkConfig.shadow.offsetY);
        networkShadowColorPreview.setBackgroundColor(NetworkConfig.shadow.color);
        networkIntervalValue.setText(formatIntervalValue(NetworkConfig.updateInterval));
    }

    private String formatIntervalValue(float v) {
        if (v == (long) v) return String.valueOf((long) v);
        String s = String.format("%.2f", v).replaceAll("0$", "").replaceAll("\\.$", "");
        return s;
    }

    private void setupListeners() {
        networkSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !PermissionHelper.hasOverlayPermission(activity)) {
                networkSwitch.setChecked(false);
                activity.applyCheckboxTint(networkSwitch, false);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putBoolean("network_enabled", false).apply();
                return;
            }

            NetworkConfig.enabled = isChecked;
            activity.applyCheckboxTint(networkSwitch, isChecked);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("network_enabled", isChecked).apply();

            if (isChecked) {
                if (FloatingService.instance != null) {
                    FloatingService.startModule(FloatingService.networkModule());
                } else {
                    activity.startService(new Intent(activity, FloatingService.class));
                }
            } else {
                FloatingService.stopModule(FloatingService.networkModule());
            }
        });

        networkSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (progress < 8) { progress = 8; sb.setProgress(progress); }
                if (progress > 100) { progress = 100; sb.setProgress(progress); }
                NetworkConfig.size = progress;
                networkSizeLabel.setText("Ukuran Teks: " + progress);
                FloatingService.updateSizeForModule(FloatingService.networkModule(), NetworkConfig.size);
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        networkColorPreview.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Pilih Warna", NetworkConfig.color, color -> {
                NetworkConfig.color = color;
                networkColorPreview.setBackgroundColor(color);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("network_color", color).apply();
                FloatingService.updateColorForModule(FloatingService.networkModule(), NetworkConfig.color);
            });
        });

        networkLabelColorPreview.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Warna Label", NetworkConfig.labelColor, color -> {
                NetworkConfig.labelColor = color;
                networkLabelColorPreview.setBackgroundColor(color);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("network_label_color", color).apply();
                FloatingService.updateLabelColorForModule(FloatingService.networkModule(), NetworkConfig.labelColor);
            });
        });

        networkBgSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            NetworkConfig.bg.enabled = isChecked;
            activity.applyCheckboxTint(networkBgSwitch, isChecked);
            networkBgConfigContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("network_bg_enabled", isChecked).apply();
            FloatingService.updateBackgroundForModule(FloatingService.networkModule());
        });

        networkBgColorPreview.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Warna Background", NetworkConfig.bg.color, color -> {
                NetworkConfig.bg.color = color;
                networkBgColorPreview.setBackgroundColor(color);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("network_bg_color", color).apply();
                FloatingService.updateBackgroundForModule(FloatingService.networkModule());
            });
        });

        networkBgPaddingSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (progress < 0) progress = 0;
                NetworkConfig.bg.padding = progress;
                networkBgPaddingLabel.setText("Ukuran Background: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("network_bg_padding", progress).apply();
                FloatingService.updateBackgroundForModule(FloatingService.networkModule());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        networkBgOffsetXSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                NetworkConfig.bg.offsetX = offset;
                networkBgOffsetXLabel.setText("Offset X: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("network_bg_offset_x", offset).apply();
                FloatingService.updateBackgroundForModule(FloatingService.networkModule());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        networkBgOffsetYSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                NetworkConfig.bg.offsetY = offset;
                networkBgOffsetYLabel.setText("Offset Y: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("network_bg_offset_y", offset).apply();
                FloatingService.updateBackgroundForModule(FloatingService.networkModule());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        networkBgMarginSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                NetworkConfig.bg.margin = progress;
                networkBgMarginLabel.setText("Margin: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("network_bg_margin", progress).apply();
                FloatingService.updateBackgroundForModule(FloatingService.networkModule());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        networkBgRadiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                NetworkConfig.bg.radius = progress;
                networkBgRadiusLabel.setText("Radius: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("network_bg_radius", progress).apply();
                FloatingService.updateBackgroundForModule(FloatingService.networkModule());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        networkShadowSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            NetworkConfig.shadow.enabled = isChecked;
            activity.applyCheckboxTint(networkShadowSwitch, isChecked);
            networkShadowConfigContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("network_shadow_enabled", isChecked).apply();
            saveNetworkShadowPrefs();
            FloatingService.updateShadowForModule(FloatingService.networkModule());
        });

        networkShadowColorPreview.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Warna Shadow", NetworkConfig.shadow.color, color -> {
                NetworkConfig.shadow.color = color;
                networkShadowColorPreview.setBackgroundColor(color);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("network_shadow_color", color).apply();
                FloatingService.updateShadowForModule(FloatingService.networkModule());
            });
        });

        networkShadowBlurSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                NetworkConfig.shadow.blur = progress;
                networkShadowBlurLabel.setText("Blur Shadow: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putFloat("network_shadow_blur", (float) progress).apply();
                FloatingService.updateShadowForModule(FloatingService.networkModule());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        networkShadowOffsetXSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                NetworkConfig.shadow.offsetX = offset;
                networkShadowOffsetXLabel.setText("Shadow X: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putFloat("network_shadow_offset_x", (float) offset).apply();
                FloatingService.updateShadowForModule(FloatingService.networkModule());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        networkShadowOffsetYSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                NetworkConfig.shadow.offsetY = offset;
                networkShadowOffsetYLabel.setText("Shadow Y: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putFloat("network_shadow_offset_y", (float) offset).apply();
                FloatingService.updateShadowForModule(FloatingService.networkModule());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        networkLockSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            NetworkConfig.touchPassthrough = isChecked;
            activity.applyCheckboxTint(networkLockSwitch, isChecked);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("network_lock", isChecked).apply();
            FloatingService.updateTouchFlagsForModule(FloatingService.networkModule());
        });

        networkValueOnlyCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            NetworkConfig.showOnlyValue = isChecked;
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("network_show_only_value", isChecked).apply();
            FloatingService.updateColorForModule(FloatingService.networkModule(), NetworkConfig.color);
        });

        networkSafeArea.setOnCheckedChangeListener((buttonView, isChecked) -> {
            NetworkConfig.safeArea = isChecked;
            activity.applyCheckboxTint(networkSafeArea, isChecked);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("network_safe_area", isChecked).apply();
        });

        networkSizeLabel.setOnClickListener(v ->
                SliderLabelEditor.showSliderEditor(activity, "Ukuran Teks", networkSizeSeekBar, 100, networkSizeLabel, "Ukuran Teks: "));
        networkBgPaddingLabel.setOnClickListener(v ->
                SliderLabelEditor.showSliderEditor(activity, "Ukuran Background", networkBgPaddingSeekBar, 80, networkBgPaddingLabel, "Ukuran Background: "));
        networkBgOffsetXLabel.setOnClickListener(v ->
                SliderLabelEditor.showOffsetEditor(activity, "Offset X", networkBgOffsetXSeekBar, networkBgOffsetXLabel, "Offset X: "));
        networkBgOffsetYLabel.setOnClickListener(v ->
                SliderLabelEditor.showOffsetEditor(activity, "Offset Y", networkBgOffsetYSeekBar, networkBgOffsetYLabel, "Offset Y: "));
        networkBgMarginLabel.setOnClickListener(v ->
                SliderLabelEditor.showSliderEditor(activity, "Margin Background", networkBgMarginSeekBar, 30, networkBgMarginLabel, "Margin: "));
        networkBgRadiusLabel.setOnClickListener(v ->
                SliderLabelEditor.showSliderEditor(activity, "Radius Background", networkBgRadiusSeekBar, 50, networkBgRadiusLabel, "Radius: "));
        networkShadowBlurLabel.setOnClickListener(v ->
                SliderLabelEditor.showSliderEditor(activity, "Blur Shadow", networkShadowBlurSeekBar, 50, networkShadowBlurLabel, "Blur Shadow: "));
        networkShadowOffsetXLabel.setOnClickListener(v ->
                SliderLabelEditor.showOffsetEditor(activity, "Shadow X", networkShadowOffsetXSeekBar, networkShadowOffsetXLabel, "Shadow X: "));
        networkShadowOffsetYLabel.setOnClickListener(v ->
                SliderLabelEditor.showOffsetEditor(activity, "Shadow Y", networkShadowOffsetYSeekBar, networkShadowOffsetYLabel, "Shadow Y: "));

        setupIntervalListeners();
    }

    private void saveNetworkShadowPrefs() {
        activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE).edit()
                .putInt("network_shadow_color", NetworkConfig.shadow.color)
                .putFloat("network_shadow_blur", NetworkConfig.shadow.blur)
                .putFloat("network_shadow_offset_x", NetworkConfig.shadow.offsetX)
                .putFloat("network_shadow_offset_y", NetworkConfig.shadow.offsetY)
                .apply();
    }

    private static final float[] INTERVAL_STEPS = {0.2f, 0.5f, 0.75f, 1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f};

    private void setupIntervalListeners() {
        networkIntervalValue.setOnClickListener(v -> showIntervalPopup(v));
    }

    private void showIntervalPopup(View anchor) {
        if (intervalPopup != null && intervalPopup.isShowing()) {
            intervalPopup.dismiss();
            return;
        }

        int currentIdx = -1;
        for (int i = 0; i < INTERVAL_STEPS.length; i++) {
            if (INTERVAL_STEPS[i] == NetworkConfig.updateInterval) {
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
                NetworkConfig.updateInterval = INTERVAL_STEPS[idx];
                updateIntervalDisplay();
                FloatingService.restartModule(FloatingService.networkModule());
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
        networkIntervalValue.setText(formatIntervalValue(NetworkConfig.updateInterval));
        activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                .edit().putFloat("network_update_interval", NetworkConfig.updateInterval).apply();
    }

    private int dp(int dp) {
        return (int) (dp * activity.getResources().getDisplayMetrics().density);
    }
}
