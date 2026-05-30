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
import exp.ftxt.features.network.NetworkConfig;
import exp.ftxt.shared.ui.ColorPickerDialog;
import exp.ftxt.utils.PermissionHelper;

public class NetworkPanelController {

    private final MainActivity activity;

    private CheckBox networkSwitch;
    private SeekBar networkSizeSeekBar;
    private Button networkColorButton;
    private CheckBox networkShadowSwitch;
    private LinearLayout networkShadowConfigContainer;
    private Button networkShadowColorButton;
    private SeekBar networkShadowBlurSeekBar;
    private SeekBar networkShadowOffsetXSeekBar;
    private SeekBar networkShadowOffsetYSeekBar;
    private CheckBox networkLockSwitch;
    private CheckBox networkBgSwitch;
    private LinearLayout networkBgConfigContainer;
    private Button networkBgColorButton;
    private SeekBar networkBgPaddingSeekBar;
    private SeekBar networkBgOffsetXSeekBar;
    private SeekBar networkBgOffsetYSeekBar;
    private SeekBar networkBgMarginSeekBar;
    private SeekBar networkBgRadiusSeekBar;
    private TextView networkSizeLabel, networkBgPaddingLabel, networkBgOffsetXLabel, networkBgOffsetYLabel;
    private TextView networkBgMarginLabel, networkBgRadiusLabel;
    private TextView networkShadowBlurLabel, networkShadowOffsetXLabel, networkShadowOffsetYLabel;
    private NetworkPositionController networkPositionController;

    public NetworkPanelController(MainActivity activity) {
        this.activity = activity;
        bindViews();
        loadConfig();
        setupListeners();
        networkPositionController = new NetworkPositionController(activity);
    }

    public void onPanelShown() {
        if (networkPositionController != null) {
            networkPositionController.refresh();
        }
    }

    public void cleanup() {
        if (networkPositionController != null) {
            networkPositionController.cleanup();
            networkPositionController = null;
        }
    }

    private void bindViews() {
        networkSwitch = activity.findViewById(R.id.networkSwitch);
        networkSizeSeekBar = activity.findViewById(R.id.networkSizeSeekBar);
        networkColorButton = activity.findViewById(R.id.networkColorButton);
        networkShadowSwitch = activity.findViewById(R.id.networkShadowSwitch);
        networkShadowConfigContainer = activity.findViewById(R.id.shadowConfigNetwork);
        networkShadowColorButton = activity.findViewById(R.id.networkShadowColorButton);
        networkShadowBlurSeekBar = activity.findViewById(R.id.networkShadowBlurSeekBar);
        networkShadowOffsetXSeekBar = activity.findViewById(R.id.networkShadowOffsetXSeekBar);
        networkShadowOffsetYSeekBar = activity.findViewById(R.id.networkShadowOffsetYSeekBar);
        networkLockSwitch = activity.findViewById(R.id.networkLockSwitch);
        networkBgSwitch = activity.findViewById(R.id.networkBgSwitch);
        networkBgConfigContainer = activity.findViewById(R.id.bgConfigNetwork);
        networkBgColorButton = activity.findViewById(R.id.networkBgColorButton);
        networkBgPaddingSeekBar = activity.findViewById(R.id.networkBgPaddingSeekBar);
        networkBgOffsetXSeekBar = activity.findViewById(R.id.networkBgOffsetXSeekBar);
        networkBgOffsetYSeekBar = activity.findViewById(R.id.networkBgOffsetYSeekBar);
        networkBgMarginSeekBar = activity.findViewById(R.id.networkBgMarginSeekBar);
        networkBgRadiusSeekBar = activity.findViewById(R.id.networkBgRadiusSeekBar);
        networkSizeLabel = activity.findViewById(R.id.networkSizeLabel);
        networkBgPaddingLabel = activity.findViewById(R.id.networkBgPaddingLabel);
        networkBgOffsetXLabel = activity.findViewById(R.id.networkBgOffsetXLabel);
        networkBgOffsetYLabel = activity.findViewById(R.id.networkBgOffsetYLabel);
        networkBgMarginLabel = activity.findViewById(R.id.networkBgMarginLabel);
        networkBgRadiusLabel = activity.findViewById(R.id.networkBgRadiusLabel);
        networkShadowBlurLabel = activity.findViewById(R.id.networkShadowBlurLabel);
        networkShadowOffsetXLabel = activity.findViewById(R.id.networkShadowOffsetXLabel);
        networkShadowOffsetYLabel = activity.findViewById(R.id.networkShadowOffsetYLabel);
    }

    private void loadConfig() {
        networkSwitch.setChecked(NetworkConfig.enabled);
        activity.applyCheckboxTint(networkSwitch, NetworkConfig.enabled);
        networkSizeSeekBar.setProgress((int) NetworkConfig.size);
        networkBgSwitch.setChecked(NetworkConfig.bgEnabled);
        activity.applyCheckboxTint(networkBgSwitch, NetworkConfig.bgEnabled);
        networkBgConfigContainer.setVisibility(NetworkConfig.bgEnabled ? View.VISIBLE : View.GONE);
        networkBgPaddingSeekBar.setProgress(NetworkConfig.bgPadding);
        networkBgOffsetXSeekBar.setProgress(NetworkConfig.bgOffsetX + 60);
        networkBgOffsetYSeekBar.setProgress(NetworkConfig.bgOffsetY + 60);
        networkBgMarginSeekBar.setProgress(NetworkConfig.bgMargin);
        networkBgRadiusSeekBar.setProgress(NetworkConfig.bgRadius);
        networkShadowSwitch.setChecked(NetworkConfig.shadow.enabled);
        activity.applyCheckboxTint(networkShadowSwitch, NetworkConfig.shadow.enabled);
        networkShadowConfigContainer.setVisibility(NetworkConfig.shadow.enabled ? View.VISIBLE : View.GONE);
        networkShadowBlurSeekBar.setProgress((int) NetworkConfig.shadow.blur);
        networkShadowOffsetXSeekBar.setProgress((int) NetworkConfig.shadow.offsetX + 60);
        networkShadowOffsetYSeekBar.setProgress((int) NetworkConfig.shadow.offsetY + 60);
        networkLockSwitch.setChecked(NetworkConfig.touchPassthrough);
        activity.applyCheckboxTint(networkLockSwitch, NetworkConfig.touchPassthrough);
        networkSizeLabel.setText("Ukuran Teks: " + (int) NetworkConfig.size);
        networkBgPaddingLabel.setText("Ukuran Background: " + NetworkConfig.bgPadding);
        networkBgOffsetXLabel.setText("Offset X: " + NetworkConfig.bgOffsetX);
        networkBgOffsetYLabel.setText("Offset Y: " + NetworkConfig.bgOffsetY);
        networkBgMarginLabel.setText("Margin: " + NetworkConfig.bgMargin);
        networkBgRadiusLabel.setText("Radius: " + NetworkConfig.bgRadius);
        networkShadowBlurLabel.setText("Blur Shadow: " + (int) NetworkConfig.shadow.blur);
        networkShadowOffsetXLabel.setText("Shadow X: " + (int) NetworkConfig.shadow.offsetX);
        networkShadowOffsetYLabel.setText("Shadow Y: " + (int) NetworkConfig.shadow.offsetY);
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
                    FloatingService.startNetworkStatic();
                } else {
                    activity.startService(new Intent(activity, FloatingService.class));
                }
            } else {
                FloatingService.stopNetworkStatic();
                if (!activity.isTextOverlayOn() && !exp.ftxt.features.fps.FpsConfig.enabled
                        && !exp.ftxt.features.clock.ClockConfig.enabled
                        && !exp.ftxt.features.battery.BatteryConfig.enabled) {
                    activity.stopService(new Intent(activity, FloatingService.class));
                }
            }
        });

        networkSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (progress < 8) { progress = 8; sb.setProgress(progress); }
                if (progress > 100) { progress = 100; sb.setProgress(progress); }
                NetworkConfig.size = progress;
                networkSizeLabel.setText("Ukuran Teks: " + progress);
                FloatingService.updateNetworkSizeStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        networkColorButton.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Pilih Warna", NetworkConfig.color, color -> {
                NetworkConfig.color = color;
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("network_color", color).apply();
                FloatingService.updateNetworkColorStatic();
            });
        });

        networkBgSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            NetworkConfig.bgEnabled = isChecked;
            activity.applyCheckboxTint(networkBgSwitch, isChecked);
            networkBgConfigContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("network_bg_enabled", isChecked).apply();
            FloatingService.updateNetworkBackgroundStatic();
        });

        networkBgColorButton.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Warna Background", NetworkConfig.bgColor, color -> {
                NetworkConfig.bgColor = color;
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("network_bg_color", color).apply();
                FloatingService.updateNetworkBackgroundStatic();
            });
        });

        networkBgPaddingSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (progress < 0) progress = 0;
                NetworkConfig.bgPadding = progress;
                networkBgPaddingLabel.setText("Ukuran Background: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("network_bg_padding", progress).apply();
                FloatingService.updateNetworkBackgroundStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        networkBgOffsetXSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                NetworkConfig.bgOffsetX = offset;
                networkBgOffsetXLabel.setText("Offset X: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("network_bg_offset_x", offset).apply();
                FloatingService.updateNetworkBackgroundStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        networkBgOffsetYSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                NetworkConfig.bgOffsetY = offset;
                networkBgOffsetYLabel.setText("Offset Y: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("network_bg_offset_y", offset).apply();
                FloatingService.updateNetworkBackgroundStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        networkBgMarginSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                NetworkConfig.bgMargin = progress;
                networkBgMarginLabel.setText("Margin: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("network_bg_margin", progress).apply();
                FloatingService.updateNetworkBackgroundStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        networkBgRadiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                NetworkConfig.bgRadius = progress;
                networkBgRadiusLabel.setText("Radius: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("network_bg_radius", progress).apply();
                FloatingService.updateNetworkBackgroundStatic();
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
            FloatingService.updateNetworkShadowStatic();
        });

        networkShadowColorButton.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Warna Shadow", NetworkConfig.shadow.color, color -> {
                NetworkConfig.shadow.color = color;
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("network_shadow_color", color).apply();
                FloatingService.updateNetworkShadowStatic();
            });
        });

        networkShadowBlurSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                NetworkConfig.shadow.blur = progress;
                networkShadowBlurLabel.setText("Blur Shadow: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putFloat("network_shadow_blur", (float) progress).apply();
                FloatingService.updateNetworkShadowStatic();
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
                FloatingService.updateNetworkShadowStatic();
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
                FloatingService.updateNetworkShadowStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        networkLockSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            NetworkConfig.touchPassthrough = isChecked;
            activity.applyCheckboxTint(networkLockSwitch, isChecked);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("network_lock", isChecked).apply();
            FloatingService.updateNetworkTouchFlagsStatic();
        });
    }

    private void saveNetworkShadowPrefs() {
        activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE).edit()
                .putInt("network_shadow_color", NetworkConfig.shadow.color)
                .putFloat("network_shadow_blur", NetworkConfig.shadow.blur)
                .putFloat("network_shadow_offset_x", NetworkConfig.shadow.offsetX)
                .putFloat("network_shadow_offset_y", NetworkConfig.shadow.offsetY)
                .apply();
    }
}
