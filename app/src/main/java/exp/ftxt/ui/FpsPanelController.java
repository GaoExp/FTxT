package exp.ftxt.ui;

import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;

import exp.ftxt.MainActivity;
import exp.ftxt.R;
import exp.ftxt.core.FloatingService;
import exp.ftxt.modules.fps.FpsConfig;
import exp.ftxt.shared.ui.ColorPickerDialog;

import android.content.Intent;

/**
 * Controller untuk panel FPS Display di MainActivity.
 *
 * Mengekstrak semua binding view, listener, dan konfigurasi FPS
 * yang sebelumnya berada di MainActivity.onCreate().
 *
 * Dipakai oleh:
 * - MainActivity → MainActivity.java (onCreate — FPS panel section)
 *
 * Terkait dengan:
 * - TextPanelController → ui/TextPanelController.java (panel saudara di drawer)
 * - PermissionHelper    → utils/PermissionHelper.java (permission overlay)
 * - FloatingService     → core/FloatingService.java (startFpsStatic, stopFpsStatic)
 */
public class FpsPanelController {

    private final MainActivity activity;

    private Switch fpsSwitch;
    private SeekBar fpsSizeSeekBar;
    private Button fpsColorButton;
    private Switch fpsShadowSwitch;
    private Switch fpsLockSwitch;

    public FpsPanelController(MainActivity activity) {
        this.activity = activity;
        bindViews();
        loadConfig();
        setupListeners();
    }

    private void bindViews() {
        fpsSwitch = activity.findViewById(R.id.fpsSwitch);
        fpsSizeSeekBar = activity.findViewById(R.id.fpsSizeSeekBar);
        fpsColorButton = activity.findViewById(R.id.fpsColorButton);
        fpsShadowSwitch = activity.findViewById(R.id.fpsShadowSwitch);
        fpsLockSwitch = activity.findViewById(R.id.fpsLockSwitch);
    }

    private void loadConfig() {
        fpsSwitch.setChecked(FpsConfig.enabled);
        activity.applySwitchTint(fpsSwitch, FpsConfig.enabled);
        fpsSizeSeekBar.setProgress((int) FpsConfig.size);
        fpsShadowSwitch.setChecked(FpsConfig.shadow);
        activity.applySwitchTint(fpsShadowSwitch, FpsConfig.shadow);
        fpsLockSwitch.setChecked(FpsConfig.touchPassthrough);
        activity.applySwitchTint(fpsLockSwitch, FpsConfig.touchPassthrough);
    }

    private void setupListeners() {
        fpsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            FpsConfig.enabled = isChecked;
            activity.applySwitchTint(fpsSwitch, isChecked);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("fps_enabled", isChecked).apply();

            if (isChecked) {
                if (FloatingService.instance != null) {
                    FloatingService.startFpsStatic();
                } else {
                    // Start service for FPS only (or alongside text)
                    activity.startService(new Intent(activity, FloatingService.class));
                }
            } else {
                FloatingService.stopFpsStatic();
                // Stop service if text overlay is also off
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

        fpsShadowSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            FpsConfig.shadow = isChecked;
            activity.applySwitchTint(fpsShadowSwitch, isChecked);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("fps_shadow", isChecked).apply();
            FloatingService.updateFpsShadowStatic();
        });

        fpsLockSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            FpsConfig.touchPassthrough = isChecked;
            activity.applySwitchTint(fpsLockSwitch, isChecked);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("fps_lock", isChecked).apply();
            FloatingService.updateFpsTouchFlagsStatic();
        });
    }
}
