package exp.ftxt.ui;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;

import exp.ftxt.MainActivity;
import exp.ftxt.R;
import exp.ftxt.core.FloatingService;
import exp.ftxt.modules.fps.FpsConfig;
import exp.ftxt.modules.text.TextConfig;
import exp.ftxt.shared.ui.ColorPickerDialog;

/**
 * Controller untuk panel Floating Text di MainActivity.
 *
 * Mengekstrak semua binding view, listener, dan konfigurasi
 * yang sebelumnya berada di MainActivity.onCreate().
 *
 * Dipakai oleh:
 * - MainActivity → MainActivity.java (onCreate — text panel section)
 *
 * Terkait dengan:
 * - FpsPanelController → ui/FpsPanelController.java (panel saudara di drawer)
 * - PermissionHelper   → utils/PermissionHelper.java (permission overlay)
 */
public class TextPanelController {

    private final MainActivity activity;

    private EditText editText;
    private SeekBar seekBar;
    private Button colorButton;
    private Switch overlaySwitch;
    private Switch touchPassthroughSwitch;
    private Switch shadowSwitch;

    public TextPanelController(MainActivity activity) {
        this.activity = activity;
        bindViews();
        loadConfig();
        setupListeners();
        applyInitialTints();
    }

    private void bindViews() {
        editText = activity.findViewById(R.id.editText);
        seekBar = activity.findViewById(R.id.textSizeSeekBar);
        colorButton = activity.findViewById(R.id.colorButton);
        overlaySwitch = activity.findViewById(R.id.overlaySwitch);
        touchPassthroughSwitch = activity.findViewById(R.id.touchPassthroughSwitch);
        shadowSwitch = activity.findViewById(R.id.shadowSwitch);
    }

    private void loadConfig() {
        shadowSwitch.setChecked(TextConfig.shadow);
    }

    private void setupListeners() {
        editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextConfig.text = s.toString().trim();
                if (TextConfig.text.isEmpty()) TextConfig.text = "FTxT AKTIF";
                FloatingService.updateTextStatic();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        seekBar.setProgress(20);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (progress < 1) { progress = 1; sb.setProgress(progress); }
                TextConfig.size = progress;
                FloatingService.updateTextSizeStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        colorButton.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Pilih Warna", TextConfig.color, color -> {
                TextConfig.color = color;
                FloatingService.updateTextColorStatic();
            });
        });

        overlaySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            activity.applySwitchTint(overlaySwitch, isChecked);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("text_overlay_on", isChecked).apply();

            if (isChecked) {
                TextConfig.text = editText.getText().toString().trim();
                if (TextConfig.text.isEmpty()) TextConfig.text = "FTxT AKTIF";

                if (FloatingService.instance != null) {
                    FloatingService.createTextOverlayStatic();
                    return;
                }

                if (activity.checkOverlayPermission()) return;
                if (activity.checkNotificationPermission()) return;
                activity.checkBatteryOptimization();

                activity.startService(new android.content.Intent(activity, FloatingService.class));
            } else {
                FloatingService.destroyTextOverlayStatic();
                if (!FpsConfig.enabled) {
                    activity.stopService(new android.content.Intent(activity, FloatingService.class));
                }
            }
        });

        touchPassthroughSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            activity.applySwitchTint(touchPassthroughSwitch, isChecked);
            TextConfig.touchPassthrough = isChecked;
            FloatingService.updateTouchFlagsStatic();
        });

        shadowSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            activity.applySwitchTint(shadowSwitch, isChecked);
            TextConfig.shadow = isChecked;
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("shadow_enabled", isChecked).apply();
            FloatingService.updateShadowStatic();
        });
    }

    private void applyInitialTints() {
        activity.applySwitchTint(overlaySwitch, overlaySwitch.isChecked());
        activity.applySwitchTint(touchPassthroughSwitch, touchPassthroughSwitch.isChecked());
        activity.applySwitchTint(shadowSwitch, shadowSwitch.isChecked());
    }
}
