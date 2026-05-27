package exp.ftxt.ui;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;

import exp.ftxt.MainActivity;
import exp.ftxt.R;
import exp.ftxt.core.FloatingService;
import exp.ftxt.modules.fps.FpsConfig;
import exp.ftxt.modules.text.TextConfig;
import exp.ftxt.shared.ui.ColorPickerDialog;
import exp.ftxt.utils.PermissionHelper;

public class TextPanelController {

    private final MainActivity activity;

    private EditText editText;
    private SeekBar seekBar;
    private Button colorButton;
    private Switch overlaySwitch;
    private Switch touchPassthroughSwitch;
    private Switch shadowSwitch;
    private LinearLayout shadowConfigContainer;
    private Button shadowColorButton;
    private SeekBar shadowBlurSeekBar;
    private SeekBar shadowOffsetXSeekBar;
    private SeekBar shadowOffsetYSeekBar;
    private Switch bgSwitch;
    private LinearLayout bgConfigContainer;
    private Button bgColorButton;
    private SeekBar bgPaddingSeekBar;
    private SeekBar bgOffsetXSeekBar;
    private SeekBar bgOffsetYSeekBar;

    public TextPanelController(MainActivity activity) {
        this.activity = activity;
        bindViews();
        loadConfig();
        setupListeners();
        applyInitialTints();
    }

    public void autoStart() {
        boolean overlayOn = activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                .getBoolean("text_overlay_on", false);
        if (!overlayOn) return;

        TextConfig.text = editText.getText().toString().trim();
        if (TextConfig.text.isEmpty()) TextConfig.text = "FTxT AKTIF";

        if (FloatingService.instance != null) {
            FloatingService.createTextOverlayStatic();
        } else {
            activity.startService(new android.content.Intent(activity, FloatingService.class));
        }
    }

    private void bindViews() {
        editText = activity.findViewById(R.id.editText);
        seekBar = activity.findViewById(R.id.textSizeSeekBar);
        colorButton = activity.findViewById(R.id.colorButton);
        overlaySwitch = activity.findViewById(R.id.overlaySwitch);
        touchPassthroughSwitch = activity.findViewById(R.id.touchPassthroughSwitch);
        shadowSwitch = activity.findViewById(R.id.shadowSwitch);
        shadowConfigContainer = activity.findViewById(R.id.shadowConfigText);
        shadowColorButton = activity.findViewById(R.id.shadowColorButton);
        shadowBlurSeekBar = activity.findViewById(R.id.shadowBlurSeekBar);
        shadowOffsetXSeekBar = activity.findViewById(R.id.shadowOffsetXSeekBar);
        shadowOffsetYSeekBar = activity.findViewById(R.id.shadowOffsetYSeekBar);
        bgSwitch = activity.findViewById(R.id.bgSwitch);
        bgConfigContainer = activity.findViewById(R.id.bgConfigText);
        bgColorButton = activity.findViewById(R.id.bgColorButton);
        bgPaddingSeekBar = activity.findViewById(R.id.bgPaddingSeekBar);
        bgOffsetXSeekBar = activity.findViewById(R.id.bgOffsetXSeekBar);
        bgOffsetYSeekBar = activity.findViewById(R.id.bgOffsetYSeekBar);
    }

    private void loadConfig() {
        boolean overlayOn = activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                .getBoolean("text_overlay_on", false);
        overlaySwitch.setChecked(overlayOn);
        touchPassthroughSwitch.setChecked(TextConfig.touchPassthrough);
        bgSwitch.setChecked(TextConfig.bgEnabled);
        bgConfigContainer.setVisibility(TextConfig.bgEnabled ? View.VISIBLE : View.GONE);
        bgPaddingSeekBar.setProgress(TextConfig.bgPadding);
        bgOffsetXSeekBar.setProgress(TextConfig.bgOffsetX);
        bgOffsetYSeekBar.setProgress(TextConfig.bgOffsetY);
        shadowSwitch.setChecked(TextConfig.shadow.enabled);
        shadowConfigContainer.setVisibility(TextConfig.shadow.enabled ? View.VISIBLE : View.GONE);
        shadowBlurSeekBar.setProgress((int) TextConfig.shadow.blur);
        shadowOffsetXSeekBar.setProgress((int) TextConfig.shadow.offsetX);
        shadowOffsetYSeekBar.setProgress((int) TextConfig.shadow.offsetY);
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
            if (isChecked && !PermissionHelper.hasOverlayPermission(activity)) {
                overlaySwitch.setChecked(false);
                activity.applySwitchTint(overlaySwitch, false);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putBoolean("text_overlay_on", false).apply();
                return;
            }

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
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("text_lock", isChecked).apply();
            FloatingService.updateTouchFlagsStatic();
        });

        bgSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            activity.applySwitchTint(bgSwitch, isChecked);
            TextConfig.bgEnabled = isChecked;
            bgConfigContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("text_bg_enabled", isChecked).apply();
            FloatingService.updateTextBackgroundStatic();
        });

        bgColorButton.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Warna Background", TextConfig.bgColor, color -> {
                TextConfig.bgColor = color;
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("text_bg_color", color).apply();
                FloatingService.updateTextBackgroundStatic();
            });
        });

        bgPaddingSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (progress < 0) progress = 0;
                TextConfig.bgPadding = progress;
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("text_bg_padding", progress).apply();
                FloatingService.updateTextBackgroundStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        bgOffsetXSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                TextConfig.bgOffsetX = progress;
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("text_bg_offset_x", progress).apply();
                FloatingService.updateTextBackgroundStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        bgOffsetYSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                TextConfig.bgOffsetY = progress;
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("text_bg_offset_y", progress).apply();
                FloatingService.updateTextBackgroundStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        shadowSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            activity.applySwitchTint(shadowSwitch, isChecked);
            TextConfig.shadow.enabled = isChecked;
            shadowConfigContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("shadow_enabled", isChecked).apply();
            saveShadowPrefs();
            FloatingService.updateShadowStatic();
        });

        shadowColorButton.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Warna Shadow", TextConfig.shadow.color, color -> {
                TextConfig.shadow.color = color;
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("shadow_color", color).apply();
                FloatingService.updateShadowStatic();
            });
        });

        shadowBlurSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                TextConfig.shadow.blur = progress;
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putFloat("shadow_blur", (float) progress).apply();
                FloatingService.updateShadowStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        shadowOffsetXSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                TextConfig.shadow.offsetX = progress;
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putFloat("shadow_offset_x", (float) progress).apply();
                FloatingService.updateShadowStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        shadowOffsetYSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                TextConfig.shadow.offsetY = progress;
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putFloat("shadow_offset_y", (float) progress).apply();
                FloatingService.updateShadowStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });
    }

    private void applyInitialTints() {
        activity.applySwitchTint(overlaySwitch, overlaySwitch.isChecked());
        activity.applySwitchTint(touchPassthroughSwitch, touchPassthroughSwitch.isChecked());
        activity.applySwitchTint(bgSwitch, bgSwitch.isChecked());
        activity.applySwitchTint(shadowSwitch, shadowSwitch.isChecked());
    }

    private void saveShadowPrefs() {
        activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE).edit()
                .putInt("shadow_color", TextConfig.shadow.color)
                .putFloat("shadow_blur", TextConfig.shadow.blur)
                .putFloat("shadow_offset_x", TextConfig.shadow.offsetX)
                .putFloat("shadow_offset_y", TextConfig.shadow.offsetY)
                .apply();
    }
}
