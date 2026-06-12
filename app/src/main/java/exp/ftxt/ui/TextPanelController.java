package exp.ftxt.ui;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.CheckBox;
import android.widget.TextView;
import exp.ftxt.MainActivity;
import exp.ftxt.R;
import exp.ftxt.core.FloatingService;
import exp.ftxt.features.fps_display.FpsConfig;
import exp.ftxt.features.floating_text.TextConfig;
import exp.ftxt.shared.ui.ColorPickerDialog;
import exp.ftxt.shared.ui.SectionHelper;
import exp.ftxt.shared.ui.SliderLabelEditor;
import exp.ftxt.utils.PermissionHelper;

public class TextPanelController {

    private final MainActivity activity;

    private EditText editText;
    private SeekBar seekBar;
    private Button colorButton;
    private CheckBox overlaySwitch;
    private CheckBox touchPassthroughSwitch;
    private CheckBox textSafeArea;
    private CheckBox shadowSwitch;
    private LinearLayout shadowConfigContainer;
    private Button shadowColorButton;
    private SeekBar shadowBlurSeekBar;
    private SeekBar shadowOffsetXSeekBar;
    private SeekBar shadowOffsetYSeekBar;
    private CheckBox bgSwitch;
    private LinearLayout bgConfigContainer;
    private Button bgColorButton;
    private SeekBar bgPaddingSeekBar;
    private SeekBar bgOffsetXSeekBar;
    private SeekBar bgOffsetYSeekBar;
    private SeekBar bgMarginSeekBar;
    private SeekBar bgRadiusSeekBar;
    private TextView textSizeLabel, bgPaddingLabel, bgOffsetXLabel, bgOffsetYLabel;
    private TextView bgMarginLabel, bgRadiusLabel;
    private TextView shadowBlurLabel, shadowOffsetXLabel, shadowOffsetYLabel;
    private TextPositionController positionController;

    public TextPanelController(MainActivity activity) {
        this.activity = activity;
        bindViews();
        loadConfig();
        setupListeners();
        applyInitialTints();
        positionController = new TextPositionController(activity);
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

    public void autoStart() {
        boolean overlayOn = activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                .getBoolean("text_overlay_on", false);
        if (!overlayOn) return;

        TextConfig.text = editText.getText().toString().trim();
        if (TextConfig.text.isEmpty()) TextConfig.text = "FunText";

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
        textSafeArea = activity.findViewById(R.id.textSafeArea);
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
        bgMarginSeekBar = activity.findViewById(R.id.bgMarginSeekBar);
        bgRadiusSeekBar = activity.findViewById(R.id.bgRadiusSeekBar);
        textSizeLabel = activity.findViewById(R.id.textSizeLabel);
        bgPaddingLabel = activity.findViewById(R.id.bgPaddingLabel);
        bgOffsetXLabel = activity.findViewById(R.id.bgOffsetXLabel);
        bgOffsetYLabel = activity.findViewById(R.id.bgOffsetYLabel);
        bgMarginLabel = activity.findViewById(R.id.bgMarginLabel);
        bgRadiusLabel = activity.findViewById(R.id.bgRadiusLabel);
        shadowBlurLabel = activity.findViewById(R.id.shadowBlurLabel);
        shadowOffsetXLabel = activity.findViewById(R.id.shadowOffsetXLabel);
        shadowOffsetYLabel = activity.findViewById(R.id.shadowOffsetYLabel);

        View sectionDisplay = activity.findViewById(R.id.text_sectionDisplay);
        TextView sectionDisplayHeader = activity.findViewById(R.id.text_sectionDisplayHeader);
        SectionHelper.setupCollapsible(sectionDisplayHeader, sectionDisplay);

        View sectionPosition = activity.findViewById(R.id.text_sectionPosition);
        TextView sectionPositionHeader = activity.findViewById(R.id.text_sectionPositionHeader);
        SectionHelper.setupCollapsible(sectionPositionHeader, sectionPosition);

        View sectionShadow = activity.findViewById(R.id.text_sectionShadow);
        TextView sectionShadowHeader = activity.findViewById(R.id.text_sectionShadowHeader);
        SectionHelper.setupCollapsible(sectionShadowHeader, sectionShadow);

        View sectionBackground = activity.findViewById(R.id.text_sectionBackground);
        TextView sectionBackgroundHeader = activity.findViewById(R.id.text_sectionBackgroundHeader);
        SectionHelper.setupCollapsible(sectionBackgroundHeader, sectionBackground);
    }

    private void loadConfig() {
        boolean overlayOn = activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                .getBoolean("text_overlay_on", false);
        overlaySwitch.setChecked(overlayOn);
        touchPassthroughSwitch.setChecked(TextConfig.touchPassthrough);
        textSafeArea.setChecked(TextConfig.safeArea);
        bgSwitch.setChecked(TextConfig.bg.enabled);
        bgConfigContainer.setVisibility(TextConfig.bg.enabled ? View.VISIBLE : View.GONE);
        bgPaddingSeekBar.setProgress(TextConfig.bg.padding);
        bgOffsetXSeekBar.setProgress(TextConfig.bg.offsetX + 60);
        bgOffsetYSeekBar.setProgress(TextConfig.bg.offsetY + 60);
        shadowSwitch.setChecked(TextConfig.shadow.enabled);
        shadowConfigContainer.setVisibility(TextConfig.shadow.enabled ? View.VISIBLE : View.GONE);
        shadowBlurSeekBar.setProgress((int) TextConfig.shadow.blur);
        shadowOffsetXSeekBar.setProgress((int) TextConfig.shadow.offsetX + 60);
        shadowOffsetYSeekBar.setProgress((int) TextConfig.shadow.offsetY + 60);
        textSizeLabel.setText("Ukuran Teks: " + (int) TextConfig.size);
        bgPaddingLabel.setText("Ukuran Background: " + TextConfig.bg.padding);
        bgOffsetXLabel.setText("Offset X: " + TextConfig.bg.offsetX);
        bgOffsetYLabel.setText("Offset Y: " + TextConfig.bg.offsetY);
        shadowBlurLabel.setText("Blur Shadow: " + (int) TextConfig.shadow.blur);
        shadowOffsetXLabel.setText("Shadow X: " + (int) TextConfig.shadow.offsetX);
        shadowOffsetYLabel.setText("Shadow Y: " + (int) TextConfig.shadow.offsetY);
    }

    private void setupListeners() {
        editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextConfig.text = s.toString().trim();
                if (TextConfig.text.isEmpty()) TextConfig.text = "FunText";
                FloatingService.updateTextStatic();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        seekBar.setProgress(20);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (progress < 1) { progress = 1; sb.setProgress(progress); }
                TextConfig.size = progress;
                textSizeLabel.setText("Ukuran Teks: " + progress);
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
                activity.applyCheckboxTint(overlaySwitch, false);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putBoolean("text_overlay_on", false).apply();
                return;
            }

            activity.applyCheckboxTint(overlaySwitch, isChecked);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("text_overlay_on", isChecked).apply();

            if (isChecked) {
                TextConfig.text = editText.getText().toString().trim();
                if (TextConfig.text.isEmpty()) TextConfig.text = "FunText";

                if (FloatingService.instance != null) {
                    FloatingService.createTextOverlayStatic();
                    return;
                }

                if (activity.checkNotificationPermission()) return;
                activity.checkBatteryOptimization();

                activity.startService(new android.content.Intent(activity, FloatingService.class));
            } else {
                FloatingService.destroyTextOverlayStatic();
                if (!activity.isAnyModuleActive()) {
                    activity.stopService(new android.content.Intent(activity, FloatingService.class));
                }
            }
        });

        touchPassthroughSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            activity.applyCheckboxTint(touchPassthroughSwitch, isChecked);
            TextConfig.touchPassthrough = isChecked;
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("text_lock", isChecked).apply();
            FloatingService.updateTouchFlagsStatic();
        });

        textSafeArea.setOnCheckedChangeListener((buttonView, isChecked) -> {
            activity.applyCheckboxTint(textSafeArea, isChecked);
            TextConfig.safeArea = isChecked;
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("text_safe_area", isChecked).apply();
        });

        bgSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            activity.applyCheckboxTint(bgSwitch, isChecked);
            TextConfig.bg.enabled = isChecked;
            bgConfigContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("text_bg_enabled", isChecked).apply();
            FloatingService.updateTextBackgroundStatic();
        });

        bgColorButton.setOnClickListener(v -> {
            ColorPickerDialog.show(activity, "Warna Background", TextConfig.bg.color, color -> {
                TextConfig.bg.color = color;
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("text_bg_color", color).apply();
                FloatingService.updateTextBackgroundStatic();
            });
        });

        bgPaddingSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (progress < 0) progress = 0;
                TextConfig.bg.padding = progress;
                bgPaddingLabel.setText("Ukuran Background: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("text_bg_padding", progress).apply();
                FloatingService.updateTextBackgroundStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        bgOffsetXSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                TextConfig.bg.offsetX = offset;
                bgOffsetXLabel.setText("Offset X: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("text_bg_offset_x", offset).apply();
                FloatingService.updateTextBackgroundStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        bgOffsetYSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                TextConfig.bg.offsetY = offset;
                bgOffsetYLabel.setText("Offset Y: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("text_bg_offset_y", offset).apply();
                FloatingService.updateTextBackgroundStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        bgMarginSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                TextConfig.bg.margin = progress;
                bgMarginLabel.setText("Margin: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("text_bg_margin", progress).apply();
                FloatingService.updateTextBackgroundStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        bgRadiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                TextConfig.bg.radius = progress;
                bgRadiusLabel.setText("Radius: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("text_bg_radius", progress).apply();
                FloatingService.updateTextBackgroundStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        shadowSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            activity.applyCheckboxTint(shadowSwitch, isChecked);
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
                shadowBlurLabel.setText("Blur Shadow: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putFloat("shadow_blur", (float) progress).apply();
                FloatingService.updateShadowStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        shadowOffsetXSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                TextConfig.shadow.offsetX = offset;
                shadowOffsetXLabel.setText("Shadow X: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putFloat("shadow_offset_x", (float) offset).apply();
                FloatingService.updateShadowStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        shadowOffsetYSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int offset = progress - 60;
                TextConfig.shadow.offsetY = offset;
                shadowOffsetYLabel.setText("Shadow Y: " + offset);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putFloat("shadow_offset_y", (float) offset).apply();
                FloatingService.updateShadowStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        textSizeLabel.setOnClickListener(v ->
                SliderLabelEditor.showSliderEditor(activity, "Ukuran Teks", seekBar, 150, textSizeLabel, "Ukuran Teks: "));
        bgPaddingLabel.setOnClickListener(v ->
                SliderLabelEditor.showSliderEditor(activity, "Ukuran Background", bgPaddingSeekBar, 80, bgPaddingLabel, "Ukuran Background: "));
        bgOffsetXLabel.setOnClickListener(v ->
                SliderLabelEditor.showOffsetEditor(activity, "Offset X", bgOffsetXSeekBar, bgOffsetXLabel, "Offset X: "));
        bgOffsetYLabel.setOnClickListener(v ->
                SliderLabelEditor.showOffsetEditor(activity, "Offset Y", bgOffsetYSeekBar, bgOffsetYLabel, "Offset Y: "));
        bgMarginLabel.setOnClickListener(v ->
                SliderLabelEditor.showSliderEditor(activity, "Margin Background", bgMarginSeekBar, 30, bgMarginLabel, "Margin: "));
        bgRadiusLabel.setOnClickListener(v ->
                SliderLabelEditor.showSliderEditor(activity, "Radius Background", bgRadiusSeekBar, 50, bgRadiusLabel, "Radius: "));
        shadowBlurLabel.setOnClickListener(v ->
                SliderLabelEditor.showSliderEditor(activity, "Blur Shadow", shadowBlurSeekBar, 50, shadowBlurLabel, "Blur Shadow: "));
        shadowOffsetXLabel.setOnClickListener(v ->
                SliderLabelEditor.showOffsetEditor(activity, "Shadow X", shadowOffsetXSeekBar, shadowOffsetXLabel, "Shadow X: "));
        shadowOffsetYLabel.setOnClickListener(v ->
                SliderLabelEditor.showOffsetEditor(activity, "Shadow Y", shadowOffsetYSeekBar, shadowOffsetYLabel, "Shadow Y: "));
    }

    private void applyInitialTints() {
        activity.applyCheckboxTint(overlaySwitch, overlaySwitch.isChecked());
        activity.applyCheckboxTint(touchPassthroughSwitch, touchPassthroughSwitch.isChecked());
        activity.applyCheckboxTint(textSafeArea, textSafeArea.isChecked());
        activity.applyCheckboxTint(bgSwitch, bgSwitch.isChecked());
        activity.applyCheckboxTint(shadowSwitch, shadowSwitch.isChecked());
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
