package exp.ftxt.shared.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;

import exp.ftxt.R;
import exp.ftxt.shared.color.ColorNameResolver;
import exp.ftxt.shared.color.HSVColorPickerView;

public class ColorPickerDialog {

    public interface ColorCallback {
        void onColorSelected(int color);
    }

    private static final String SAVED_COLORS_KEY = "saved_colors";
    private static final int MAX_SAVED_COLORS = 16;

    public static void show(Activity activity, String title, int initialColor, ColorCallback callback) {
        SharedPreferences prefs = activity.getSharedPreferences("ftxt_prefs", Context.MODE_PRIVATE);
        boolean isSliderMode = prefs.getString("color_picker_mode", "slider").equals("slider");

        if (isSliderMode) {
            showSliderMode(activity, title, initialColor, callback);
        } else {
            showDiskMode(activity, title, initialColor, callback);
        }
    }

    // ========================================================================
    // Disk mode (color wheel)
    // ========================================================================
    private static void showDiskMode(Activity activity, String title, int initialColor, ColorCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View dialogView = activity.getLayoutInflater()
                .inflate(R.layout.dialog_hsv_color_picker, null);

        HSVColorPickerView colorWheel = dialogView.findViewById(R.id.colorWheel);
        TextView colorPreview = dialogView.findViewById(R.id.colorPreview);
        TextView hexValue = dialogView.findViewById(R.id.hexValue);
        TextView hsvValue = dialogView.findViewById(R.id.hsvValue);
        TextView rgbValue = dialogView.findViewById(R.id.rgbValue);
        ImageButton hexEditButton = dialogView.findViewById(R.id.hexEditButton);
        SeekBar redSeekBar = dialogView.findViewById(R.id.redSeekBar);
        SeekBar greenSeekBar = dialogView.findViewById(R.id.greenSeekBar);
        SeekBar blueSeekBar = dialogView.findViewById(R.id.blueSeekBar);
        SeekBar alphaSeekBar = dialogView.findViewById(R.id.alphaSeekBar);
        SeekBar hueSeekBar = dialogView.findViewById(R.id.hueSeekBar);
        TextView redLabel = dialogView.findViewById(R.id.redLabel);
        TextView greenLabel = dialogView.findViewById(R.id.greenLabel);
        TextView blueLabel = dialogView.findViewById(R.id.blueLabel);
        TextView alphaLabel = dialogView.findViewById(R.id.alphaLabel);
        Button okButton = dialogView.findViewById(R.id.okButton);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);

        redSeekBar.setProgress(Color.red(initialColor));
        greenSeekBar.setProgress(Color.green(initialColor));
        blueSeekBar.setProgress(Color.blue(initialColor));
        alphaSeekBar.setProgress(Color.alpha(initialColor));

        float[] initHsv = new float[3];
        Color.colorToHSV(initialColor, initHsv);
        hueSeekBar.setProgress(Math.round(initHsv[0]));

        colorWheel.setColor(initialColor);

        boolean isSliderMode = activity.getSharedPreferences("ftxt_prefs", Context.MODE_PRIVATE)
                .getString("color_picker_mode", "slider").equals("slider");
        if (isSliderMode) {
            colorWheel.setVisibility(View.GONE);
            hueSeekBar.setVisibility(View.VISIBLE);
        }

        updateDiskDisplay(colorPreview, hexValue, hsvValue, rgbValue,
                redLabel, greenLabel, blueLabel, alphaLabel,
                redSeekBar, greenSeekBar, blueSeekBar, alphaSeekBar);

        final boolean[] isUpdating = {false};

        SeekBar.OnSeekBarChangeListener sliderListener = new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int p, boolean f) {
                if (isUpdating[0]) return;
                isUpdating[0] = true;
                int color = Color.argb(alphaSeekBar.getProgress(),
                        redSeekBar.getProgress(),
                        greenSeekBar.getProgress(), blueSeekBar.getProgress());
                colorWheel.setColor(color);
                float[] hsv = new float[3];
                Color.colorToHSV(color, hsv);
                hueSeekBar.setProgress(Math.round(hsv[0]));
                updateDiskDisplay(colorPreview, hexValue, hsvValue, rgbValue,
                        redLabel, greenLabel, blueLabel, alphaLabel,
                        redSeekBar, greenSeekBar, blueSeekBar, alphaSeekBar);
                callback.onColorSelected(color);
                isUpdating[0] = false;
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        };

        redSeekBar.setOnSeekBarChangeListener(sliderListener);
        greenSeekBar.setOnSeekBarChangeListener(sliderListener);
        blueSeekBar.setOnSeekBarChangeListener(sliderListener);
        alphaSeekBar.setOnSeekBarChangeListener(sliderListener);

        colorWheel.setOnColorChangeListener(color -> {
            if (isUpdating[0]) return;
            isUpdating[0] = true;
            redSeekBar.setProgress(Color.red(color));
            greenSeekBar.setProgress(Color.green(color));
            blueSeekBar.setProgress(Color.blue(color));
            float[] hsv = new float[3];
            Color.colorToHSV(color, hsv);
            hueSeekBar.setProgress(Math.round(hsv[0]));
            updateDiskDisplay(colorPreview, hexValue, hsvValue, rgbValue,
                    redLabel, greenLabel, blueLabel, alphaLabel,
                    redSeekBar, greenSeekBar, blueSeekBar, alphaSeekBar);
            callback.onColorSelected(color);
            isUpdating[0] = false;
        });

        hueSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int p, boolean f) {
                if (isUpdating[0]) return;
                isUpdating[0] = true;
                int current = Color.argb(alphaSeekBar.getProgress(),
                        redSeekBar.getProgress(),
                        greenSeekBar.getProgress(), blueSeekBar.getProgress());
                float[] hsv = new float[3];
                Color.colorToHSV(current, hsv);
                int color = Color.HSVToColor(alphaSeekBar.getProgress(),
                        new float[]{p, hsv[1], hsv[2]});
                colorWheel.setColor(color);
                redSeekBar.setProgress(Color.red(color));
                greenSeekBar.setProgress(Color.green(color));
                blueSeekBar.setProgress(Color.blue(color));
                updateDiskDisplay(colorPreview, hexValue, hsvValue, rgbValue,
                        redLabel, greenLabel, blueLabel, alphaLabel,
                        redSeekBar, greenSeekBar, blueSeekBar, alphaSeekBar);
                callback.onColorSelected(color);
                isUpdating[0] = false;
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        hexEditButton.setOnClickListener(v -> {
            String currentHex = String.format("#%02X%02X%02X%02X",
                    alphaSeekBar.getProgress(), redSeekBar.getProgress(),
                    greenSeekBar.getProgress(), blueSeekBar.getProgress());

            EditText input = new EditText(activity);
            input.setText(currentHex);
            input.setSelection(input.length());

            new AlertDialog.Builder(activity)
                    .setTitle("Edit HEX")
                    .setView(input)
                    .setPositiveButton("OK", (d, w) -> {
                        try {
                            int color = parseHex(input.getText().toString().trim());
                            isUpdating[0] = true;
                            alphaSeekBar.setProgress(Color.alpha(color));
                            redSeekBar.setProgress(Color.red(color));
                            greenSeekBar.setProgress(Color.green(color));
                            blueSeekBar.setProgress(Color.blue(color));
                            colorWheel.setColor(color);
                            float[] hsv = new float[3];
                            Color.colorToHSV(color, hsv);
                            hueSeekBar.setProgress(Math.round(hsv[0]));
                            updateDiskDisplay(colorPreview, hexValue, hsvValue, rgbValue,
                                    redLabel, greenLabel, blueLabel, alphaLabel,
                                    redSeekBar, greenSeekBar, blueSeekBar, alphaSeekBar);
                            isUpdating[0] = false;
                        } catch (IllegalArgumentException e) {
                            Toast.makeText(activity, "HEX tidak valid", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });

        hexValue.setOnLongClickListener(v -> {
            copyToClipboard(activity, String.format("#%02X%02X%02X%02X",
                    alphaSeekBar.getProgress(), redSeekBar.getProgress(),
                    greenSeekBar.getProgress(), blueSeekBar.getProgress()));
            Toast.makeText(activity, "HEX disalin", Toast.LENGTH_SHORT).show();
            return true;
        });

        hsvValue.setOnLongClickListener(v -> {
            copyToClipboard(activity, hsvValue.getText().toString());
            Toast.makeText(activity, "HSV disalin", Toast.LENGTH_SHORT).show();
            return true;
        });

        rgbValue.setOnLongClickListener(v -> {
            copyToClipboard(activity, rgbValue.getText().toString());
            Toast.makeText(activity, "ARGB disalin", Toast.LENGTH_SHORT).show();
            return true;
        });

        redLabel.setOnClickListener(v -> showDiskValueEditor(activity, "R", redSeekBar, 255,
                colorPreview, hexValue, hsvValue, rgbValue,
                redLabel, greenLabel, blueLabel, alphaLabel,
                redSeekBar, greenSeekBar, blueSeekBar, alphaSeekBar,
                colorWheel, hueSeekBar, isUpdating));

        greenLabel.setOnClickListener(v -> showDiskValueEditor(activity, "G", greenSeekBar, 255,
                colorPreview, hexValue, hsvValue, rgbValue,
                redLabel, greenLabel, blueLabel, alphaLabel,
                redSeekBar, greenSeekBar, blueSeekBar, alphaSeekBar,
                colorWheel, hueSeekBar, isUpdating));

        blueLabel.setOnClickListener(v -> showDiskValueEditor(activity, "B", blueSeekBar, 255,
                colorPreview, hexValue, hsvValue, rgbValue,
                redLabel, greenLabel, blueLabel, alphaLabel,
                redSeekBar, greenSeekBar, blueSeekBar, alphaSeekBar,
                colorWheel, hueSeekBar, isUpdating));

        alphaLabel.setOnClickListener(v -> showDiskValueEditor(activity, "A", alphaSeekBar, 255,
                colorPreview, hexValue, hsvValue, rgbValue,
                redLabel, greenLabel, blueLabel, alphaLabel,
                redSeekBar, greenSeekBar, blueSeekBar, alphaSeekBar,
                colorWheel, hueSeekBar, isUpdating));

        builder.setView(dialogView);

        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        if (dialogTitle != null) dialogTitle.setText(title);

        ImageButton switchModeBtn = dialogView.findViewById(R.id.switchModeButton);
        AlertDialog dialog = builder.create();
        switchModeBtn.setOnClickListener(v -> {
            int color = Color.argb(alphaSeekBar.getProgress(),
                    redSeekBar.getProgress(),
                    greenSeekBar.getProgress(), blueSeekBar.getProgress());
            SharedPreferences prefs = activity.getSharedPreferences("ftxt_prefs", Context.MODE_PRIVATE);
            prefs.edit().putString("color_picker_mode", "slider").apply();
            dialog.dismiss();
            showSliderMode(activity, title, color, callback);
        });

        okButton.setOnClickListener(v -> {
            int color = Color.argb(alphaSeekBar.getProgress(),
                    redSeekBar.getProgress(),
                    greenSeekBar.getProgress(), blueSeekBar.getProgress());
            callback.onColorSelected(color);
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> {
            callback.onColorSelected(initialColor);
            dialog.dismiss();
        });

        dialog.show();
    }

    // ========================================================================
    // Slider mode (Hue/Saturation/Brightness/Alpha sliders)
    // ========================================================================
    private static void setThumbPos(View thumb, int progress, int max) {
        if (thumb == null) return;
        ViewGroup parent = (ViewGroup) thumb.getParent();
        if (parent == null) return;
        parent.post(() -> {
            if (thumb.getParent() == null) return;
            float pw = parent.getWidth();
            if (pw <= 0) return;
            float tw = thumb.getWidth();
            float ratio = Math.max(0, Math.min(1, progress / (float) max));
            thumb.setTranslationX(ratio * (pw - tw));
        });
    }

    private static void setupSliderTouch(View touchArea, View thumb, int max,
                                          int[] progHolder, Runnable onUpdate) {
        touchArea.setOnTouchListener((v, event) -> {
            int a = event.getActionMasked();
            if (a == MotionEvent.ACTION_DOWN || a == MotionEvent.ACTION_MOVE) {
                float x = event.getX();
                float w = v.getWidth();
                if (w <= 0) return true;
                float ratio = Math.max(0, Math.min(1, x / w));
                int prog = Math.round(ratio * max);
                progHolder[0] = prog;
                float tw = thumb.getWidth();
                thumb.setTranslationX(Math.max(0, Math.min(w - tw, x - tw / 2)));
                if (onUpdate != null) onUpdate.run();
                return true;
            }
            return false;
        });
    }

    private static void showSliderMode(Activity activity, String title, int initialColor, ColorCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View dialogView = activity.getLayoutInflater()
                .inflate(R.layout.dialog_hue_slider_picker, null);

        TextView currentSwatch = dialogView.findViewById(R.id.currentColorSwatch);
        TextView previousSwatch = dialogView.findViewById(R.id.previousColorSwatch);
        View hueThumb = dialogView.findViewById(R.id.hueThumb);
        View saturationThumb = dialogView.findViewById(R.id.saturationThumb);
        View brightnessThumb = dialogView.findViewById(R.id.brightnessThumb);
        View alphaThumb = dialogView.findViewById(R.id.alphaThumb);
        View hueTouchArea = dialogView.findViewById(R.id.hueTouchArea);
        View saturationTouchArea = dialogView.findViewById(R.id.saturationTouchArea);
        View brightnessTouchArea = dialogView.findViewById(R.id.brightnessTouchArea);
        View alphaTouchArea = dialogView.findViewById(R.id.alphaTouchArea);
        View hueGradientBg = dialogView.findViewById(R.id.hueGradientBg);
        View saturationGradientBg = dialogView.findViewById(R.id.saturationGradientBg);
        View brightnessGradientBg = dialogView.findViewById(R.id.brightnessGradientBg);
        View alphaGradientBg = dialogView.findViewById(R.id.alphaGradientBg);
        TextView hueLabel = dialogView.findViewById(R.id.hueLabel);
        TextView saturationLabel = dialogView.findViewById(R.id.saturationLabel);
        TextView brightnessLabel = dialogView.findViewById(R.id.brightnessLabel);
        TextView alphaLabel = dialogView.findViewById(R.id.alphaLabel);
        TextView hexValue = dialogView.findViewById(R.id.hexValue);
        TextView hsvValue = dialogView.findViewById(R.id.hsvValue);
        TextView rgbValue = dialogView.findViewById(R.id.rgbValue);
        ImageButton hexEditButton = dialogView.findViewById(R.id.hexEditButton);
        GridLayout savedColorsGrid = dialogView.findViewById(R.id.savedColorsGrid);
        TextView addSavedColor = dialogView.findViewById(R.id.addSavedColor);
        TextView collapseToggle = dialogView.findViewById(R.id.collapseToggle);
        Button applyButton = dialogView.findViewById(R.id.applyButton);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);

        float[] initHsv = new float[3];
        Color.colorToHSV(initialColor, initHsv);

        setSwatchBg(previousSwatch, initialColor);
        previousSwatch.setText(ColorNameResolver.getName(initialColor));
        previousSwatch.setTextColor(textColorForBg(initialColor));

        final int[] hueProg = {Math.round(initHsv[0])};
        final int[] satProg = {Math.round(initHsv[1] * 100)};
        final int[] briProg = {Math.round(initHsv[2] * 100)};
        final int[] opaProg = {Color.alpha(initialColor)};

        applyHueGradient(hueGradientBg);
        applySatGradient(saturationGradientBg, hueProg[0]);
        applyBrightnessGradient(brightnessGradientBg, hueProg[0], initHsv[1]);
        applyAlphaGradient(alphaGradientBg, initialColor);

        setThumbPos(hueThumb, hueProg[0], 360);
        setThumbPos(saturationThumb, satProg[0], 100);
        setThumbPos(brightnessThumb, briProg[0], 100);
        setThumbPos(alphaThumb, opaProg[0], 255);

        Runnable updateSlider = () -> {
            int h = hueProg[0];
            float s = satProg[0] / 100f;
            float v = briProg[0] / 100f;
            int a = opaProg[0];
            int color = Color.HSVToColor(a, new float[]{h, s, v});

            setSwatchBg(currentSwatch, color);
            currentSwatch.setText(ColorNameResolver.getName(color));
            currentSwatch.setTextColor(textColorForBg(color));
            applyBrightnessGradient(brightnessGradientBg, h, s);
            applyAlphaGradient(alphaGradientBg, color);

            hueLabel.setText(String.format("H: %d\u00B0", h));
            saturationLabel.setText(String.format("S: %d%%", satProg[0]));
            brightnessLabel.setText(String.format("V: %d%%", briProg[0]));
            alphaLabel.setText(String.format("A: %d", opaProg[0]));

            hexValue.setText(String.format("AHEX: #%02X%02X%02X%02X",
                    a, Color.red(color), Color.green(color), Color.blue(color)));
            hsvValue.setText(String.format("HSV: %d\u00B0, %d%%, %d%%",
                    h, Math.round(s * 100), Math.round(v * 100)));
            rgbValue.setText(String.format("ARGB: %d, %d, %d, %d",
                    a, Color.red(color), Color.green(color), Color.blue(color)));
            callback.onColorSelected(color);
        };

        setupSliderTouch(hueTouchArea, hueThumb, 360, hueProg, () -> {
            applySatGradient(saturationGradientBg, hueProg[0]);
            updateSlider.run();
        });

        setupSliderTouch(saturationTouchArea, saturationThumb, 100, satProg, updateSlider);
        setupSliderTouch(brightnessTouchArea, brightnessThumb, 100, briProg, updateSlider);
        setupSliderTouch(alphaTouchArea, alphaThumb, 255, opaProg, updateSlider);

        updateSlider.run();

        // --- Fixed: compute color first, then format using ARGB components ---
        hexValue.setOnLongClickListener(v -> {
            int color = Color.HSVToColor(opaProg[0],
                    new float[]{hueProg[0], satProg[0] / 100f, briProg[0] / 100f});
            copyToClipboard(activity, String.format("#%02X%02X%02X%02X",
                    opaProg[0], Color.red(color), Color.green(color), Color.blue(color)));
            Toast.makeText(activity, "HEX disalin", Toast.LENGTH_SHORT).show();
            return true;
        });

        hsvValue.setOnLongClickListener(v -> {
            copyToClipboard(activity, hsvValue.getText().toString());
            Toast.makeText(activity, "HSV disalin", Toast.LENGTH_SHORT).show();
            return true;
        });

        rgbValue.setOnLongClickListener(v -> {
            copyToClipboard(activity, rgbValue.getText().toString());
            Toast.makeText(activity, "ARGB disalin", Toast.LENGTH_SHORT).show();
            return true;
        });

        hexEditButton.setOnClickListener(v -> {
            int color = Color.HSVToColor(opaProg[0],
                    new float[]{hueProg[0], satProg[0] / 100f, briProg[0] / 100f});
            String currentHex = String.format("#%02X%02X%02X%02X",
                    opaProg[0], Color.red(color), Color.green(color), Color.blue(color));

            EditText input = new EditText(activity);
            input.setText(currentHex);
            input.setSelection(input.length());

            new AlertDialog.Builder(activity)
                    .setTitle("Edit HEX")
                    .setView(input)
                    .setPositiveButton("OK", (d, w) -> {
                        try {
                            int color2 = parseHex(input.getText().toString().trim());
                            float[] hsv = new float[3];
                            Color.colorToHSV(color2, hsv);
                            hueProg[0] = Math.round(hsv[0]);
                            satProg[0] = Math.round(hsv[1] * 100);
                            briProg[0] = Math.round(hsv[2] * 100);
                            opaProg[0] = Color.alpha(color2);
                            setThumbPos(hueThumb, hueProg[0], 360);
                            setThumbPos(saturationThumb, satProg[0], 100);
                            setThumbPos(brightnessThumb, briProg[0], 100);
                            setThumbPos(alphaThumb, opaProg[0], 255);
                            applySatGradient(saturationGradientBg, hueProg[0]);
                            updateSlider.run();
                        } catch (IllegalArgumentException e) {
                            Toast.makeText(activity, "HEX tidak valid", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });

        loadSavedColors(activity, savedColorsGrid,
                hueProg, satProg, briProg, opaProg,
                hueThumb, saturationThumb, brightnessThumb, alphaThumb,
                saturationGradientBg, updateSlider);

        addSavedColor.setOnClickListener(btn -> {
            int h = hueProg[0];
            float s = satProg[0] / 100f;
            float val = briProg[0] / 100f;
            int a = opaProg[0];
            int color = Color.HSVToColor(a, new float[]{h, s, val});
            saveColor(activity, color, true);

            int cellStep = savedColorsGrid.getWidth() / 8;
            savedColorsGrid.setClipChildren(false);
            for (int i = 0; i < savedColorsGrid.getChildCount(); i++) {
                ((ViewGroup) savedColorsGrid.getChildAt(i)).setClipChildren(false);
                savedColorsGrid.getChildAt(i).setTranslationX(-cellStep);
            }

            loadSavedColors(activity, savedColorsGrid,
                    hueProg, satProg, briProg, opaProg,
                    hueThumb, saturationThumb, brightnessThumb, alphaThumb,
                    saturationGradientBg, updateSlider);

            List<Animator> anims = new ArrayList<>();
            for (int i = 0; i < savedColorsGrid.getChildCount(); i++) {
                anims.add(ObjectAnimator.ofFloat(
                        savedColorsGrid.getChildAt(i), "translationX", -cellStep, 0));
            }
            AnimatorSet set = new AnimatorSet();
            set.playTogether(anims);
            set.setDuration(300);
            set.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
            set.start();
        });

        collapseToggle.setOnClickListener(v -> {
            boolean expanded = savedColorsGrid.getVisibility() == View.VISIBLE;
            savedColorsGrid.setVisibility(expanded ? View.GONE : View.VISIBLE);
            collapseToggle.setText(expanded ? "▼" : "▲");

        });

        builder.setView(dialogView);

        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        if (dialogTitle != null) dialogTitle.setText(title);

        AlertDialog dialog = builder.create();

        ImageButton switchModeBtn = dialogView.findViewById(R.id.switchModeButton);
        switchModeBtn.setOnClickListener(v -> {
            int color = Color.HSVToColor(opaProg[0],
                    new float[]{hueProg[0], satProg[0] / 100f, briProg[0] / 100f});
            SharedPreferences prefs = activity.getSharedPreferences("ftxt_prefs", Context.MODE_PRIVATE);
            prefs.edit().putString("color_picker_mode", "disk").apply();
            dialog.dismiss();
            showDiskMode(activity, title, color, callback);
        });

        applyButton.setOnClickListener(btn -> {
            int color = Color.HSVToColor(opaProg[0],
                    new float[]{hueProg[0], satProg[0] / 100f, briProg[0] / 100f});
            callback.onColorSelected(color);
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> {
            callback.onColorSelected(initialColor);
            dialog.dismiss();
        });
        dialog.show();
    }

    private static Drawable createCheckerboard(Context context) {
        int size = dpToPx(context, 8);
        Bitmap bmp = Bitmap.createBitmap(size * 2, size * 2, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint();
        int light = Color.rgb(200, 200, 200);
        int dark = Color.rgb(155, 155, 155);
        paint.setColor(light);
        canvas.drawRect(0, 0, size, size, paint);
        canvas.drawRect(size, size, size * 2, size * 2, paint);
        paint.setColor(dark);
        canvas.drawRect(size, 0, size * 2, size, paint);
        canvas.drawRect(0, size, size, size * 2, paint);
        BitmapDrawable d = new BitmapDrawable(context.getResources(), bmp);
        d.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        d.setFilterBitmap(false);
        return d;
    }

    private static void setSwatchBg(TextView swatch, int color) {
        LayerDrawable layers = new LayerDrawable(new Drawable[]{
                createCheckerboard(swatch.getContext()),
                new ColorDrawable(color)
        });
        swatch.setBackground(layers);
    }

    private static void applyHueGradient(View bar) {
        int[] colors = new int[361];
        for (int i = 0; i <= 360; i++) {
            colors[i] = Color.HSVToColor(new float[]{i, 1f, 1f});
        }
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
        bar.setBackground(gd);
    }

    private static void applySatGradient(View bar, int hue) {
        int[] colors = new int[101];
        for (int i = 0; i <= 100; i++) {
            colors[i] = Color.HSVToColor(new float[]{hue, i / 100f, 1f});
        }
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
        bar.setBackground(gd);
    }

    private static void applyBrightnessGradient(View bar, int hue, float sat) {
        int fullColor = Color.HSVToColor(new float[]{hue, sat, 1f});
        int[] colors = new int[]{Color.BLACK, fullColor};
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
        bar.setBackground(gd);
    }

    private static void applyAlphaGradient(View bar, int color) {
        int opaque = color | 0xFF000000;
        int[] colors = new int[]{color & 0x00FFFFFF, opaque};
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
        LayerDrawable layers = new LayerDrawable(new Drawable[]{
                createCheckerboard(bar.getContext()),
                gd
        });
        bar.setBackground(layers);
    }

    private static void loadSavedColors(Activity activity, GridLayout grid,
                                         int[] hueProg, int[] satProg, int[] briProg, int[] opaProg,
                                         View hueThumb, View satThumb, View briThumb, View opaThumb,
                                         View saturationGradientBg,
                                         Runnable updateSlider) {
        SharedPreferences prefs = activity.getSharedPreferences("ftxt_prefs", Context.MODE_PRIVATE);
        String saved = prefs.getString(SAVED_COLORS_KEY, "");
        String[] hexes = saved.isEmpty() ? new String[0] : saved.split(",");
        TextView countView = grid.getRootView().findViewById(R.id.savedColorsCount);
        if (countView != null) countView.setText(hexes.length + "/16");
        int margin = dpToPx(activity, 1);
        boolean init = grid.getChildCount() == 0;

        for (int i = 0; i < MAX_SAVED_COLORS; i++) {
            FrameLayout cell;
            if (init) {
                cell = new FrameLayout(activity);
                GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
                lp.width = 0; lp.height = 0;
                lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, GridLayout.FILL, 1f);
                lp.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, GridLayout.FILL, 1f);
                lp.setMargins(margin, margin, margin, margin);
                cell.setLayoutParams(lp);
                View inner = new View(activity);
                inner.setLayoutParams(new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                cell.addView(inner);
                grid.addView(cell);
            } else {
                cell = (FrameLayout) grid.getChildAt(i);
            }

            View inner = cell.getChildAt(0);
            final int idx = i;

            if (i < hexes.length && !hexes[i].isEmpty()) {
                try {
                    int color = parseHex(hexes[i]);
                    inner.setBackgroundColor(color);
                    final int fc = color;
                    inner.setOnClickListener(v -> {
                        AlertDialog dlg = new AlertDialog.Builder(activity)
                                .setTitle("Apply Color")
                                .setMessage("Gunakan warna ini?")
                                .setPositiveButton("Apply", (d, w) -> {
                                    float[] hsv = new float[3];
                                    Color.colorToHSV(fc, hsv);
                                    hueProg[0] = Math.round(hsv[0]);
                                    satProg[0] = Math.round(hsv[1] * 100);
                                    briProg[0] = Math.round(hsv[2] * 100);
                                    opaProg[0] = Color.alpha(fc);
                                    setThumbPos(hueThumb, hueProg[0], 360);
                                    setThumbPos(satThumb, satProg[0], 100);
                                    setThumbPos(briThumb, briProg[0], 100);
                                    setThumbPos(opaThumb, opaProg[0], 255);
                                    applySatGradient(saturationGradientBg, hueProg[0]);
                                    updateSlider.run();
                                })
                                .setNegativeButton("Batal", null)
                                .setNeutralButton("Hapus", (d, w) -> {
                                    String cur = prefs.getString(SAVED_COLORS_KEY, "");
                                    String[] all = cur.split(",");
                                    int oldLen = all.length;
                                    StringBuilder sb = new StringBuilder();
                                    for (int j = 0; j < all.length; j++) {
                                        if (j != idx && !all[j].isEmpty()) {
                                            if (sb.length() > 0) sb.append(",");
                                            sb.append(all[j]);
                                        }
                                    }
                                    prefs.edit().putString(SAVED_COLORS_KEY, sb.toString()).apply();
                                    int cellStep = grid.getWidth() / 8;
                                    grid.setClipChildren(false);
                                    int lastAffected = Math.min(oldLen, MAX_SAVED_COLORS);
                                    for (int k = idx; k < lastAffected; k++) {
                                        View child = grid.getChildAt(k);
                                        if (child != null) {
                                            ((ViewGroup) child).setClipChildren(false);
                                            child.setTranslationX(cellStep);
                                        }
                                    }
                                    loadSavedColors(activity, grid,
                                            hueProg, satProg, briProg, opaProg,
                                            hueThumb, satThumb, briThumb, opaThumb,
                                            saturationGradientBg, updateSlider);
                                    List<Animator> anims = new ArrayList<>();
                                    for (int k = idx; k < lastAffected; k++) {
                                        View child = grid.getChildAt(k);
                                        if (child != null) {
                                            anims.add(ObjectAnimator.ofFloat(
                                                    child, "translationX", cellStep, 0));
                                        }
                                    }
                                    if (!anims.isEmpty()) {
                                        AnimatorSet set = new AnimatorSet();
                                        set.playTogether(anims);
                                        set.setDuration(300);
                                        set.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
                                        set.start();
                                    }
                                })
                                .create();
                        dlg.setOnShowListener(s -> {
                            Button hapusBtn = dlg.getButton(AlertDialog.BUTTON_NEUTRAL);
                            hapusBtn.setTextColor(Color.RED);
                        });
                        dlg.show();
                    });
                    inner.setOnLongClickListener(v -> {
                        ClipData data = ClipData.newPlainText("grid_color_idx",
                                String.valueOf(idx));
                        v.startDragAndDrop(data,
                                new View.DragShadowBuilder(v), v, 0);
                        return true;
                    });
                } catch (IllegalArgumentException e) {
                    setEmptyInner(activity, inner, grid, prefs, idx,
                            hueProg, satProg, briProg, opaProg,
                            hueThumb, satThumb, briThumb, opaThumb,
                            saturationGradientBg, updateSlider);
                }
            } else {
                setEmptyInner(activity, inner, grid, prefs, idx,
                        hueProg, satProg, briProg, opaProg,
                        hueThumb, satThumb, briThumb, opaThumb,
                        saturationGradientBg, updateSlider);
            }
        }

        if (init) {
            grid.post(() -> {
                int w = grid.getWidth();
                if (w <= 0) return;
                int cellH = (w - margin * 2 * 8) / 8;
                if (cellH < 10) return;
                for (int ci = 0; ci < grid.getChildCount(); ci++) {
                    View child = grid.getChildAt(ci);
                    GridLayout.LayoutParams p = (GridLayout.LayoutParams) child.getLayoutParams();
                    p.height = cellH;
                    child.setLayoutParams(p);
                }
            });
        }

        grid.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED: {
                    View dragged = (View) event.getLocalState();
                    if (dragged != null) dragged.setAlpha(0.4f);
                    return true;
                }
                case DragEvent.ACTION_DROP: {
                    ClipData clip = event.getClipData();
                    if (clip == null || clip.getItemCount() == 0) return false;
                    try {
                        int fromIdx = Integer.parseInt(
                                clip.getItemAt(0).getText().toString());
                        float dx = event.getX();
                        float dy = event.getY();
                        int cc = grid.getChildCount();
                        int toIdx = -1;
                        for (int ci = 0; ci < cc; ci++) {
                            View child = grid.getChildAt(ci);
                            if (dx >= child.getLeft() && dx <= child.getRight()
                                    && dy >= child.getTop() && dy <= child.getBottom()) {
                                toIdx = ci;
                                break;
                            }
                        }
                        if (toIdx >= 0 && fromIdx != toIdx) {
                            String cur = prefs.getString(SAVED_COLORS_KEY, "");
                            String[] arr = cur.split(",");
                            if (fromIdx < arr.length && toIdx < arr.length) {
                                String tmp = arr[fromIdx];
                                arr[fromIdx] = arr[toIdx];
                                arr[toIdx] = tmp;
                                StringBuilder sb = new StringBuilder();
                                for (String s : arr) {
                                    if (!s.isEmpty()) {
                                        if (sb.length() > 0) sb.append(",");
                                        sb.append(s);
                                    }
                                }
                                prefs.edit().putString(SAVED_COLORS_KEY,
                                        sb.toString()).apply();
                                loadSavedColors(activity, grid,
                                        hueProg, satProg, briProg, opaProg,
                                        hueThumb, satThumb, briThumb, opaThumb,
                                        saturationGradientBg, updateSlider);
                            }
                        }
                    } catch (Exception ignored) {}
                    return true;
                }
                case DragEvent.ACTION_DRAG_ENDED: {
                    for (int ci = 0; ci < grid.getChildCount(); ci++) {
                        FrameLayout cell = (FrameLayout) grid.getChildAt(ci);
                        cell.getChildAt(0).setAlpha(1f);
                    }
                    return true;
                }
            }
            return false;
        });
    }

    private static void setEmptyInner(Activity activity, View inner, GridLayout grid,
                                       SharedPreferences prefs, int idx,
                                       int[] hueProg, int[] satProg, int[] briProg, int[] opaProg,
                                       View hueThumb, View satThumb, View briThumb, View opaThumb,
                                       View saturationGradientBg,
                                       Runnable updateSlider) {
        GradientDrawable border = new GradientDrawable();
        border.setShape(GradientDrawable.RECTANGLE);
        border.setStroke(dpToPx(activity, 2), Color.rgb(76, 175, 80));
        border.setColor(Color.TRANSPARENT);
        inner.setBackground(border);
        inner.setOnClickListener(v -> {
            int h = hueProg[0];
            float s = satProg[0] / 100f;
            float val = briProg[0] / 100f;
            int a = opaProg[0];
            int c = Color.HSVToColor(a, new float[]{h, s, val});
            saveColor(activity, c, false);
            loadSavedColors(activity, grid,
                    hueProg, satProg, briProg, opaProg,
                    hueThumb, satThumb, briThumb, opaThumb,
                    saturationGradientBg, updateSlider);
        });
    }

    private static void saveColor(Activity activity, int color, boolean prepend) {
        SharedPreferences prefs = activity.getSharedPreferences("ftxt_prefs", Context.MODE_PRIVATE);
        String hex = String.format("#%02X%02X%02X%02X",
                Color.alpha(color), Color.red(color), Color.green(color), Color.blue(color));
        String saved = prefs.getString(SAVED_COLORS_KEY, "");
        String[] existing = saved.isEmpty() ? new String[0] : saved.split(",");

        for (String s : existing) {
            if (s.equals(hex)) {
                Toast.makeText(activity, "Warna sudah tersimpan", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (existing.length >= MAX_SAVED_COLORS) {
            Toast.makeText(activity, "Maksimal " + MAX_SAVED_COLORS + " warna tersimpan", Toast.LENGTH_SHORT).show();
            return;
        }

        String updated;
        if (prepend) {
            updated = saved.isEmpty() ? hex : hex + "," + saved;
        } else {
            updated = saved.isEmpty() ? hex : saved + "," + hex;
        }
        prefs.edit().putString(SAVED_COLORS_KEY, updated).apply();
        Toast.makeText(activity, "Warna tersimpan", Toast.LENGTH_SHORT).show();
    }

    // ========================================================================
    // Shared utilities
    // ========================================================================
    private static int dpToPx(Context context, int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    private static int parseHex(String hex) {
        if (hex.startsWith("#")) hex = hex.substring(1);
        if (hex.length() == 6) hex = "FF" + hex;
        if (hex.length() != 8) throw new IllegalArgumentException();
        return (int) Long.parseLong(hex, 16);
    }

    private static int textColorForBg(int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return (r * 0.299 + g * 0.587 + b * 0.114) > 128 ? Color.BLACK : Color.WHITE;
    }

    private static void copyToClipboard(Context context, String text) {
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("color", text));
    }

    private static void updateDiskDisplay(TextView preview, TextView hex, TextView hsv, TextView rgb,
                                           TextView rLabel, TextView gLabel, TextView bLabel, TextView aLabel,
                                           SeekBar rBar, SeekBar gBar, SeekBar bBar, SeekBar aBar) {
        int a = aBar.getProgress();
        int r = rBar.getProgress();
        int g = gBar.getProgress();
        int b = bBar.getProgress();
        int color = Color.argb(a, r, g, b);

        preview.setBackgroundColor(color);
        preview.setText(ColorNameResolver.getName(color));

        int textColor = (r * 0.299 + g * 0.587 + b * 0.114) > 128 ? Color.BLACK : Color.WHITE;
        preview.setTextColor(textColor);

        hex.setText(String.format("HEX: #%02X%02X%02X%02X", a, r, g, b));

        float[] hsvArr = new float[3];
        Color.colorToHSV(color, hsvArr);
        hsv.setText(String.format("HSV (%.0f\u00B0, %.0f%%, %.0f%%)",
                hsvArr[0], hsvArr[1] * 100, hsvArr[2] * 100));

        rgb.setText(String.format("ARGB (%d, %d, %d, %d)", a, r, g, b));

        rLabel.setText(String.format("R:%d", r));
        gLabel.setText(String.format("G:%d", g));
        bLabel.setText(String.format("B:%d", b));
        aLabel.setText(String.format("A:%d", a));
    }

    private static void showDiskValueEditor(Activity activity, String label, SeekBar bar, int max,
                                              TextView colorPreview, TextView hexValue, TextView hsvValue, TextView rgbValue,
                                              TextView redLabel, TextView greenLabel, TextView blueLabel, TextView alphaLabel,
                                              SeekBar redSeekBar, SeekBar greenSeekBar, SeekBar blueSeekBar, SeekBar alphaSeekBar,
                                              HSVColorPickerView colorWheel, SeekBar hueSeekBar, boolean[] isUpdating) {
        EditText input = new EditText(activity);
        input.setText(String.valueOf(bar.getProgress()));
        input.setSelection(input.length());
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

        new AlertDialog.Builder(activity)
                .setTitle("Edit " + label)
                .setView(input)
                .setPositiveButton("OK", (d, w) -> {
                    try {
                        int val = Integer.parseInt(input.getText().toString().trim());
                        if (val < 0 || val > max) {
                            Toast.makeText(activity, "Nilai harus 0-" + max, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        isUpdating[0] = true;
                        bar.setProgress(val);
                        int color = Color.argb(alphaSeekBar.getProgress(),
                                redSeekBar.getProgress(),
                                greenSeekBar.getProgress(), blueSeekBar.getProgress());
                        colorWheel.setColor(color);
                        float[] hsv = new float[3];
                        Color.colorToHSV(color, hsv);
                        hueSeekBar.setProgress(Math.round(hsv[0]));
                        updateDiskDisplay(colorPreview, hexValue, hsvValue, rgbValue,
                                redLabel, greenLabel, blueLabel, alphaLabel,
                                redSeekBar, greenSeekBar, blueSeekBar, alphaSeekBar);
                        isUpdating[0] = false;
                    } catch (NumberFormatException e) {
                        Toast.makeText(activity, "Nilai tidak valid", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }
}
