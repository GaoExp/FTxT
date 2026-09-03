package exp.ftxt.ui;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import exp.ftxt.MainActivity;
import exp.ftxt.R;
import exp.ftxt.core.FloatingService;
import exp.ftxt.features.crosshair.CrosshairConfig;
import exp.ftxt.shared.ui.ColorPickerDialog;
import exp.ftxt.shared.ui.SectionHelper;
import exp.ftxt.shared.ui.SliderLabelEditor;
import exp.ftxt.utils.PermissionHelper;

public class CrosshairPanelController {

    private static final int STYLE_COUNT = 44;

    private final MainActivity activity;

    private CheckBox crosshairSwitch;
    private CheckBox crosshairLockSwitch;
    private CheckBox crosshairSafeAreaSwitch;
    private LinearLayout crosshairStyleGrid;
    private ImageView crosshairPreviewImage;
    private SeekBar crosshairSizeSeekBar;
    private SeekBar crosshairOpacitySeekBar;
    private SeekBar crosshairRotationSeekBar;
    private TextView crosshairSizeLabel;
    private TextView crosshairOpacityLabel;
    private TextView crosshairRotationLabel;
    private View crosshairColorPreview;
    private CrosshairPositionController positionController;
    private View selectedStyleView;

    public CrosshairPanelController(MainActivity activity, View rootView) {
        this.activity = activity;
        bindViews(rootView);
        loadConfig();
        setupListeners();
        buildStyleGrid();
        positionController = new CrosshairPositionController(activity, rootView);
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
        crosshairSwitch = rootView.findViewById(R.id.crosshairSwitch);
        crosshairLockSwitch = rootView.findViewById(R.id.crosshairLockSwitch);
        crosshairSafeAreaSwitch = rootView.findViewById(R.id.crosshairSafeAreaSwitch);
        crosshairStyleGrid = rootView.findViewById(R.id.crosshairStyleGrid);
        crosshairPreviewImage = rootView.findViewById(R.id.crosshairPreviewImage);
        crosshairSizeSeekBar = rootView.findViewById(R.id.crosshairSizeSeekBar);
        crosshairOpacitySeekBar = rootView.findViewById(R.id.crosshairOpacitySeekBar);
        crosshairRotationSeekBar = rootView.findViewById(R.id.crosshairRotationSeekBar);
        crosshairSizeLabel = rootView.findViewById(R.id.crosshairSizeLabel);
        crosshairOpacityLabel = rootView.findViewById(R.id.crosshairOpacityLabel);
        crosshairRotationLabel = rootView.findViewById(R.id.crosshairRotationLabel);
        crosshairColorPreview = rootView.findViewById(R.id.crosshairColorPreview);

        View sectionDisplay = rootView.findViewById(R.id.crosshair_sectionDisplay);
        TextView sectionDisplayHeader = rootView.findViewById(R.id.crosshair_sectionDisplayHeader);
        SectionHelper.setupCollapsible(sectionDisplayHeader, sectionDisplay);

        View sectionPosition = rootView.findViewById(R.id.crosshair_sectionPosition);
        TextView sectionPositionHeader = rootView.findViewById(R.id.crosshair_sectionPositionHeader);
        SectionHelper.setupCollapsible(sectionPositionHeader, sectionPosition);
    }

    private void loadConfig() {
        crosshairSwitch.setChecked(CrosshairConfig.enabled);
        activity.applyCheckboxTint(crosshairSwitch, CrosshairConfig.enabled);
        crosshairLockSwitch.setChecked(CrosshairConfig.touchPassthrough);
        activity.applyCheckboxTint(crosshairLockSwitch, CrosshairConfig.touchPassthrough);
        crosshairSafeAreaSwitch.setChecked(CrosshairConfig.safeArea);
        activity.applyCheckboxTint(crosshairSafeAreaSwitch, CrosshairConfig.safeArea);
        crosshairSizeSeekBar.setProgress((int) CrosshairConfig.size);
        crosshairOpacitySeekBar.setProgress(CrosshairConfig.opacity);
        crosshairSizeLabel.setText("Ukuran: " + (int) CrosshairConfig.size);
        crosshairOpacityLabel.setText("Opasitas: " + CrosshairConfig.opacity + "%");
        crosshairRotationSeekBar.setProgress((int) CrosshairConfig.rotation);
        crosshairRotationLabel.setText("Rotasi: " + (int) CrosshairConfig.rotation + "°");
        crosshairColorPreview.setBackgroundColor(CrosshairConfig.color);
    }

    private void setupListeners() {
        crosshairSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !PermissionHelper.hasOverlayPermission(activity)) {
                crosshairSwitch.setChecked(false);
                activity.applyCheckboxTint(crosshairSwitch, false);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putBoolean("crosshair_enabled", false).apply();
                return;
            }

            CrosshairConfig.enabled = isChecked;
            activity.applyCheckboxTint(crosshairSwitch, isChecked);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("crosshair_enabled", isChecked).apply();

            if (isChecked) {
                if (FloatingService.instance != null) {
                    FloatingService.startModule(FloatingService.crosshairModule());
                } else {
                    activity.startService(new Intent(activity, FloatingService.class));
                }
            } else {
                FloatingService.stopModule(FloatingService.crosshairModule());
            }
        });

        crosshairSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (progress < 4) { progress = 4; sb.setProgress(progress); }
                CrosshairConfig.size = progress;
                crosshairSizeLabel.setText("Ukuran: " + progress);
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putFloat("crosshair_size", (float) progress).apply();
                FloatingService.updateSizeForModule(FloatingService.crosshairModule(), CrosshairConfig.size);
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        crosshairOpacitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (progress < 10) { progress = 10; sb.setProgress(progress); }
                CrosshairConfig.opacity = progress;
                crosshairOpacityLabel.setText("Opasitas: " + progress + "%");
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putInt("crosshair_opacity", progress).apply();
                FloatingService.crosshairModule().applyOpacity();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        crosshairRotationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                CrosshairConfig.rotation = progress;
                crosshairRotationLabel.setText("Rotasi: " + progress + "°");
                activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                        .edit().putFloat("crosshair_rotation", (float) progress).apply();
                FloatingService.crosshairModule().applyRotation();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        crosshairSizeLabel.setOnClickListener(v ->
                SliderLabelEditor.showSliderEditor(activity, "Ukuran", crosshairSizeSeekBar, 160, crosshairSizeLabel, "Ukuran: "));

        crosshairOpacityLabel.setOnClickListener(v ->
                SliderLabelEditor.showSliderEditor(activity, "Opasitas", crosshairOpacitySeekBar, 100, crosshairOpacityLabel, "Opasitas: "));

        crosshairRotationLabel.setOnClickListener(v ->
                SliderLabelEditor.showSliderEditor(activity, "Rotasi", crosshairRotationSeekBar, 359, crosshairRotationLabel, "Rotasi: "));

        crosshairLockSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            CrosshairConfig.touchPassthrough = isChecked;
            activity.applyCheckboxTint(crosshairLockSwitch, isChecked);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("crosshair_lock", isChecked).apply();
            FloatingService.updateTouchFlagsForModule(FloatingService.crosshairModule());
        });

        crosshairSafeAreaSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            CrosshairConfig.safeArea = isChecked;
            activity.applyCheckboxTint(crosshairSafeAreaSwitch, isChecked);
            activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                    .edit().putBoolean("crosshair_safe_area", isChecked).apply();
            FloatingService.updatePositionForModule(FloatingService.crosshairModule());
        });

        crosshairColorPreview.setOnClickListener(v ->
                ColorPickerDialog.show(activity, "Warna Bidikan", CrosshairConfig.color, color -> {
                    CrosshairConfig.color = color;
                    crosshairColorPreview.setBackgroundColor(color);
                    activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                            .edit().putInt("crosshair_color", color).apply();
                    applyGalleryTint();
                    FloatingService.updateColorForModule(FloatingService.crosshairModule(), CrosshairConfig.color);
                }));
    }

    private void buildStyleGrid() {
        int cell = dp(48);
        int pad = dp(4);

        for (int i = 1; i <= STYLE_COUNT; i++) {
            final int index = i;
            int resId = styleResId(i);
            if (resId == 0) continue;
            ImageView item = new ImageView(activity);
            item.setImageResource(resId);
            item.setPadding(pad, pad, pad, pad);
            item.setScaleType(ImageView.ScaleType.FIT_XY);
            item.setContentDescription(null);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(cell, cell);
            lp.setMarginEnd(dp(6));
            item.setLayoutParams(lp);

            item.setOnClickListener(v -> selectStyle(index));
            crosshairStyleGrid.addView(item);
            if (isSelected(index)) {
                item.setBackgroundResource(R.drawable.bg_style_item_selected);
                selectedStyleView = item;
            } else {
                item.setBackgroundResource(0);
            }
        }
        updatePreview();
        applyGalleryTint();
    }

    private void applyGalleryTint() {
        int count = crosshairStyleGrid.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = crosshairStyleGrid.getChildAt(i);
            if (child instanceof ImageView) {
                ImageView iv = (ImageView) child;
                iv.setColorFilter(CrosshairConfig.color, PorterDuff.Mode.SRC_IN);
            }
        }
        crosshairPreviewImage.setColorFilter(CrosshairConfig.color, PorterDuff.Mode.SRC_IN);
    }

    private void updatePreview() {
        int resId = styleResId(CrosshairConfig.styleIndex);
        if (resId != 0) {
            crosshairPreviewImage.setImageResource(resId);
        }
    }

    private boolean isSelected(int index) {
        return index == Math.max(1, Math.min(STYLE_COUNT, CrosshairConfig.styleIndex));
    }

    private void selectStyle(int index) {
        CrosshairConfig.styleIndex = index;
        activity.getSharedPreferences("ftxt_prefs", MainActivity.MODE_PRIVATE)
                .edit().putInt("crosshair_style", index).apply();

        if (selectedStyleView != null) {
            selectedStyleView.setBackgroundResource(0);
        }
        ImageView newSelected = findStyleChild(index);
        if (newSelected != null) {
            newSelected.setBackgroundResource(R.drawable.bg_style_item_selected);
            selectedStyleView = newSelected;
        }

        updatePreview();
        applyGalleryTint();
        FloatingService.crosshairModule().applyStyle();
    }

    private ImageView findStyleChild(int index) {
        int seen = 0;
        for (int i = 1; i <= STYLE_COUNT; i++) {
            if (styleResId(i) == 0) continue;
            seen++;
            if (i == index) {
                return (ImageView) crosshairStyleGrid.getChildAt(seen - 1);
            }
        }
        return null;
    }

    private int styleResId(int index) {
        String name = "crosshair_" + index;
        return activity.getResources().getIdentifier(name, "drawable", activity.getPackageName());
    }

    private int dp(int dp) {
        return (int) (dp * activity.getResources().getDisplayMetrics().density);
    }
}
