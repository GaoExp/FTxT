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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;

import exp.ftxt.R;
import exp.ftxt.features.color_picker.TriangleColorPickerView;
import exp.ftxt.shared.color.ColorNameResolver;

public class ColorPickerDialog {

    public interface ColorCallback {
        void onColorSelected(int color);
    }

    private static final String PREFS_NAME = "ftxt_prefs";
    private static final String SAVED_COLORS_KEY = "cp_saved_colors";
    private static final int MAX_SAVED_COLORS = 16;

    private static Drawable sCheckerDrawable;
    private static GradientDrawable sSatGd, sValGd, sAlphaGd;
    private static final int[] SAT_COLORS = new int[51];
    private static final int[] ALPHA_COLORS = new int[2];

    public static void show(Activity activity, String title, int initialColor, ColorCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View dialogView = activity.getLayoutInflater()
                .inflate(R.layout.dialog_color_picker, null);

        TriangleColorPickerView colorWheel = dialogView.findViewById(R.id.cp_colorWheel);
        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        TextView colorPreview = dialogView.findViewById(R.id.cp_colorPreview);
        TextView hexValue = dialogView.findViewById(R.id.cp_hexValue);
        TextView hsvValue = dialogView.findViewById(R.id.cp_hsvValue);
        TextView rgbValue = dialogView.findViewById(R.id.cp_rgbValue);
        ImageButton hexEditButton = dialogView.findViewById(R.id.cp_hexEditButton);

        View hueThumb = dialogView.findViewById(R.id.cp_hueThumb);
        View saturationThumb = dialogView.findViewById(R.id.cp_saturationThumb);
        View valueThumb = dialogView.findViewById(R.id.valueThumb);
        View alphaThumb = dialogView.findViewById(R.id.cp_alphaThumb);
        View hueTouchArea = dialogView.findViewById(R.id.cp_hueTouchArea);
        View saturationTouchArea = dialogView.findViewById(R.id.cp_saturationTouchArea);
        View valueTouchArea = dialogView.findViewById(R.id.valueTouchArea);
        View alphaTouchArea = dialogView.findViewById(R.id.cp_alphaTouchArea);
        View hueGradientBg = dialogView.findViewById(R.id.cp_hueGradientBg);
        View saturationGradientBg = dialogView.findViewById(R.id.cp_saturationGradientBg);
        View valueGradientBg = dialogView.findViewById(R.id.valueGradientBg);
        View alphaGradientBg = dialogView.findViewById(R.id.cp_alphaGradientBg);
        TextView hueLabel = dialogView.findViewById(R.id.cp_hueLabel);
        TextView saturationLabel = dialogView.findViewById(R.id.cp_saturationLabel);
        TextView valueLabel = dialogView.findViewById(R.id.valueLabel);
        TextView alphaLabel = dialogView.findViewById(R.id.cp_alphaLabel);

        View redThumb = dialogView.findViewById(R.id.redThumb);
        View greenThumb = dialogView.findViewById(R.id.greenThumb);
        View blueThumb = dialogView.findViewById(R.id.blueThumb);
        View redTouchArea = dialogView.findViewById(R.id.redTouchArea);
        View greenTouchArea = dialogView.findViewById(R.id.greenTouchArea);
        View blueTouchArea = dialogView.findViewById(R.id.blueTouchArea);
        View redGradientBg = dialogView.findViewById(R.id.redGradientBg);
        View greenGradientBg = dialogView.findViewById(R.id.greenGradientBg);
        View blueGradientBg = dialogView.findViewById(R.id.blueGradientBg);
        TextView redValLabel = dialogView.findViewById(R.id.redValLabel);
        TextView greenValLabel = dialogView.findViewById(R.id.greenValLabel);
        TextView blueValLabel = dialogView.findViewById(R.id.blueValLabel);

        GridLayout savedColorsGrid = dialogView.findViewById(R.id.cp_savedColorsGrid);
        TextView savedColorsCount = dialogView.findViewById(R.id.cp_savedColorsCount);
        TextView addSavedColor = dialogView.findViewById(R.id.cp_addSavedColor);
        TextView collapseToggle = dialogView.findViewById(R.id.cp_collapseToggle);
        View savedColorsHeader = dialogView.findViewById(R.id.savedColorsHeader);
        View rgbSliderBody = dialogView.findViewById(R.id.rgbSliderBody);
        View rgbHeader = dialogView.findViewById(R.id.rgbHeader);

        Button okButton = dialogView.findViewById(R.id.okButton);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);

        if (dialogTitle != null) dialogTitle.setText(title);

        View previewContainer = dialogView.findViewById(R.id.previewContainer);
        setupTransparencyChecker(previewContainer);

        final int[] hueProg = {0};
        final int[] satProg = {100};
        final int[] valProg = {100};
        final int[] opaProg = {255};
        final int[] redProg = {255};
        final int[] greenProg = {125};
        final int[] blueProg = {255};
        final boolean[] isUpdating = {false};
        final int[] lastSatHue = {-1};
        final int[] lastValHue = {-1};
        final int[] lastAlphaColor = {0};
        final int[] lastWheelArgb = {0};

        initGradientDrawables(saturationGradientBg, valueGradientBg, alphaGradientBg);

        applyHueGradient(hueGradientBg);
        applySatGradient(saturationGradientBg, hueProg[0]);
        applyValueGradient(valueGradientBg, hueProg[0], satProg[0] / 100f);
        setThumbPos(hueThumb, hueProg[0], 360);
        setThumbPos(saturationThumb, satProg[0], 100);
        setThumbPos(valueThumb, valProg[0], 100);
        setThumbPos(alphaThumb, opaProg[0], 255);

        Runnable updateSliderOutput = () -> {
            if (isUpdating[0]) return;
            isUpdating[0] = true;
            int color = hsvToColor(hueProg[0], satProg[0], valProg[0], opaProg[0]);
            updateDisplays(color, colorWheel, colorPreview, hexValue, hsvValue, rgbValue,
                    hueLabel, saturationLabel, valueLabel, alphaLabel,
                    hueThumb, saturationThumb, valueThumb, alphaThumb,
                    hueProg, satProg, valProg, opaProg,
                    saturationGradientBg, valueGradientBg, alphaGradientBg,
                    lastSatHue, lastValHue, lastAlphaColor, lastWheelArgb,
                    rgbSliderBody, redValLabel, greenValLabel, blueValLabel,
                    redThumb, greenThumb, blueThumb, redProg, greenProg, blueProg,
                    redGradientBg, greenGradientBg, blueGradientBg, isUpdating);
            isUpdating[0] = false;
        };

        Runnable updateFromRgb = () -> {
            if (isUpdating[0]) return;
            isUpdating[0] = true;
            int color = Color.argb(opaProg[0], redProg[0], greenProg[0], blueProg[0]);
            updateDisplays(color, colorWheel, colorPreview, hexValue, hsvValue, rgbValue,
                    hueLabel, saturationLabel, valueLabel, alphaLabel,
                    hueThumb, saturationThumb, valueThumb, alphaThumb,
                    hueProg, satProg, valProg, opaProg,
                    saturationGradientBg, valueGradientBg, alphaGradientBg,
                    lastSatHue, lastValHue, lastAlphaColor, lastWheelArgb,
                    rgbSliderBody, redValLabel, greenValLabel, blueValLabel,
                    redThumb, greenThumb, blueThumb, redProg, greenProg, blueProg,
                    redGradientBg, greenGradientBg, blueGradientBg, isUpdating);
            isUpdating[0] = false;
        };

        colorWheel.setOnColorChangeListener(color -> {
            if (isUpdating[0]) return;
            isUpdating[0] = true;
            int a = opaProg[0];
            color = Color.argb(a, Color.red(color), Color.green(color), Color.blue(color));
            updateDisplays(color, colorWheel, colorPreview, hexValue, hsvValue, rgbValue,
                    hueLabel, saturationLabel, valueLabel, alphaLabel,
                    hueThumb, saturationThumb, valueThumb, alphaThumb,
                    hueProg, satProg, valProg, opaProg,
                    saturationGradientBg, valueGradientBg, alphaGradientBg,
                    lastSatHue, lastValHue, lastAlphaColor, lastWheelArgb,
                    rgbSliderBody, redValLabel, greenValLabel, blueValLabel,
                    redThumb, greenThumb, blueThumb, redProg, greenProg, blueProg,
                    redGradientBg, greenGradientBg, blueGradientBg, isUpdating);
            isUpdating[0] = false;
        });

        setupSliderTouch(hueTouchArea, hueThumb, 360, hueProg, () -> {
            applySatGradient(saturationGradientBg, hueProg[0]);
            updateSliderOutput.run();
        });
        setupSliderTouch(saturationTouchArea, saturationThumb, 100, satProg, updateSliderOutput);
        setupSliderTouch(valueTouchArea, valueThumb, 100, valProg, updateSliderOutput);
        setupSliderTouch(alphaTouchArea, alphaThumb, 255, opaProg, updateSliderOutput);

        setupSliderTouch(redTouchArea, redThumb, 255, redProg, updateFromRgb);
        setupSliderTouch(greenTouchArea, greenThumb, 255, greenProg, updateFromRgb);
        setupSliderTouch(blueTouchArea, blueThumb, 255, blueProg, updateFromRgb);

        rgbHeader.setOnClickListener(v -> {
            boolean expanded = rgbSliderBody.getVisibility() == View.VISIBLE;
            rgbSliderBody.setVisibility(expanded ? View.GONE : View.VISIBLE);
            if (!expanded) {
                isUpdating[0] = true;
                int c = hsvToColor(hueProg[0], satProg[0], valProg[0], opaProg[0]);
                updateDisplays(c, colorWheel, colorPreview, hexValue, hsvValue, rgbValue,
                        hueLabel, saturationLabel, valueLabel, alphaLabel,
                        hueThumb, saturationThumb, valueThumb, alphaThumb,
                        hueProg, satProg, valProg, opaProg,
                        saturationGradientBg, valueGradientBg, alphaGradientBg,
                        lastSatHue, lastValHue, lastAlphaColor, lastWheelArgb,
                        rgbSliderBody, redValLabel, greenValLabel, blueValLabel,
                        redThumb, greenThumb, blueThumb, redProg, greenProg, blueProg,
                        redGradientBg, greenGradientBg, blueGradientBg, isUpdating);
                isUpdating[0] = false;
            }
        });

        hexEditButton.setOnClickListener(v -> {
            int color = hsvToColor(hueProg[0], satProg[0], valProg[0], opaProg[0]);
            String currentHex = String.format("#%02X%02X%02X%02X",
                    Color.alpha(color), Color.red(color), Color.green(color), Color.blue(color));

            EditText input = new EditText(activity);
            input.setText(currentHex);
            input.setSelection(input.length());

            new AlertDialog.Builder(activity)
                    .setTitle("Edit HEX")
                    .setView(input)
                    .setPositiveButton("OK", (d, w) -> {
                        try {
                            int parsed = parseHex(input.getText().toString().trim());
                            isUpdating[0] = true;
                            updateDisplays(parsed, colorWheel, colorPreview, hexValue, hsvValue, rgbValue,
                                    hueLabel, saturationLabel, valueLabel, alphaLabel,
                                    hueThumb, saturationThumb, valueThumb, alphaThumb,
                                    hueProg, satProg, valProg, opaProg,
                                    saturationGradientBg, valueGradientBg, alphaGradientBg,
                                    lastSatHue, lastValHue, lastAlphaColor, lastWheelArgb,
                                    rgbSliderBody, redValLabel, greenValLabel, blueValLabel,
                                    redThumb, greenThumb, blueThumb, redProg, greenProg, blueProg,
                                    redGradientBg, greenGradientBg, blueGradientBg, isUpdating);
                            isUpdating[0] = false;
                        } catch (IllegalArgumentException e) {
                            Toast.makeText(activity, "HEX tidak valid", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });

        hexValue.setOnLongClickListener(v -> {
            int color = hsvToColor(hueProg[0], satProg[0], valProg[0], opaProg[0]);
            copyToClipboard(activity, String.format("#%02X%02X%02X%02X",
                    Color.alpha(color), Color.red(color), Color.green(color), Color.blue(color)));
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

        savedColorsGrid.setVisibility(View.GONE);
        collapseToggle.setText("\u25BC");

        savedColorsHeader.setOnClickListener(v -> {
            boolean expanded = savedColorsGrid.getVisibility() == View.VISIBLE;
            savedColorsGrid.setVisibility(expanded ? View.GONE : View.VISIBLE);
            collapseToggle.setText(expanded ? "\u25BC" : "\u25B2");
            if (!expanded) {
                savedColorsGrid.post(() -> recalcGridCellSizes(savedColorsGrid, activity));
            }
        });

        addSavedColor.setOnClickListener(v -> {
            int color = hsvToColor(hueProg[0], satProg[0], valProg[0], opaProg[0]);
            saveColor(activity, color, true);
            loadSavedColors(activity, savedColorsGrid, savedColorsCount,
                    hueProg, satProg, valProg, opaProg,
                    hueThumb, saturationThumb, valueThumb, alphaThumb,
                    saturationGradientBg, valueGradientBg, alphaGradientBg,
                    lastSatHue, lastValHue, lastAlphaColor, lastWheelArgb,
                    colorWheel, colorPreview, hexValue, hsvValue, rgbValue,
                    hueLabel, saturationLabel, valueLabel, alphaLabel,
                    rgbSliderBody, redValLabel, greenValLabel, blueValLabel,
                    redThumb, greenThumb, blueThumb, redProg, greenProg, blueProg,
                    redGradientBg, greenGradientBg, blueGradientBg, isUpdating);
        });

        loadSavedColors(activity, savedColorsGrid, savedColorsCount,
                hueProg, satProg, valProg, opaProg,
                hueThumb, saturationThumb, valueThumb, alphaThumb,
                saturationGradientBg, valueGradientBg, alphaGradientBg,
                lastSatHue, lastValHue, lastAlphaColor, lastWheelArgb,
                colorWheel, colorPreview, hexValue, hsvValue, rgbValue,
                hueLabel, saturationLabel, valueLabel, alphaLabel,
                rgbSliderBody, redValLabel, greenValLabel, blueValLabel,
                redThumb, greenThumb, blueThumb, redProg, greenProg, blueProg,
                redGradientBg, greenGradientBg, blueGradientBg, isUpdating);

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        okButton.setOnClickListener(v -> {
            int color = hsvToColor(hueProg[0], satProg[0], valProg[0], opaProg[0]);
            callback.onColorSelected(color);
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> {
            callback.onColorSelected(initialColor);
            dialog.dismiss();
        });

        dialog.show();

        isUpdating[0] = true;
        colorWheel.setColor(initialColor);
        updateDisplays(initialColor, colorWheel, colorPreview, hexValue, hsvValue, rgbValue,
                hueLabel, saturationLabel, valueLabel, alphaLabel,
                hueThumb, saturationThumb, valueThumb, alphaThumb,
                hueProg, satProg, valProg, opaProg,
                saturationGradientBg, valueGradientBg, alphaGradientBg,
                lastSatHue, lastValHue, lastAlphaColor, lastWheelArgb,
                rgbSliderBody, redValLabel, greenValLabel, blueValLabel,
                redThumb, greenThumb, blueThumb, redProg, greenProg, blueProg,
                redGradientBg, greenGradientBg, blueGradientBg, isUpdating);
        isUpdating[0] = false;
    }

    private static void updateDisplays(int color,
                                        TriangleColorPickerView colorWheel,
                                        TextView colorPreview, TextView hexValue,
                                        TextView hsvValue, TextView rgbValue,
                                        TextView hueLabel, TextView saturationLabel,
                                        TextView valueLabel, TextView alphaLabel,
                                        View hueThumb, View saturationThumb,
                                        View valueThumb, View alphaThumb,
                                        int[] hueProg, int[] satProg,
                                        int[] valProg, int[] opaProg,
                                        View saturationGradientBg, View valueGradientBg,
                                        View alphaGradientBg,
                                        int[] lastSatHue, int[] lastValHue,
                                        int[] lastAlphaColor, int[] lastWheelArgb,
                                        View rgbSliderBody,
                                        TextView redValLabel, TextView greenValLabel,
                                        TextView blueValLabel,
                                        View redThumb, View greenThumb, View blueThumb,
                                        int[] redProg, int[] greenProg, int[] blueProg,
                                        View redGradientBg, View greenGradientBg,
                                        View blueGradientBg,
                                        boolean[] isUpdating) {
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        boolean rgbVisible = rgbSliderBody.getVisibility() == View.VISIBLE;
        if (rgbVisible) {
            redProg[0] = r;
            greenProg[0] = g;
            blueProg[0] = b;
        }

        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        int hue = Math.round(hsv[0]);
        int sat = Math.round(hsv[1] * 100);
        int val = Math.round(hsv[2] * 100);

        if (val == 0) { hue = hueProg[0]; sat = satProg[0]; }
        if (sat == 0) { hue = hueProg[0]; }

        hueProg[0] = hue;
        satProg[0] = sat;
        valProg[0] = val;
        opaProg[0] = a;

        if (rgbVisible) {
            redValLabel.setText(String.valueOf(r));
            greenValLabel.setText(String.valueOf(g));
            blueValLabel.setText(String.valueOf(b));
            setThumbPos(redThumb, r, 255);
            setThumbPos(greenThumb, g, 255);
            setThumbPos(blueThumb, b, 255);
            applyRedGradient(redGradientBg);
            applyGreenGradient(greenGradientBg);
            applyBlueGradient(blueGradientBg);
        }

        colorPreview.setBackgroundColor(color);
        colorPreview.setText(ColorNameResolver.getName(color));
        int textColor = (r * 0.299 + g * 0.587 + b * 0.114) > 128 ? Color.BLACK : Color.WHITE;
        colorPreview.setTextColor(textColor);

        hexValue.setText("HEX: #" + hexColor(a, r, g, b));
        hsvValue.setText("HSV: " + hue + "\u00B0, " + sat + "%, " + val + "%");
        rgbValue.setText("ARGB: " + a + ", " + r + ", " + g + ", " + b);

        if (color != lastWheelArgb[0]) {
            colorWheel.setColor(color);
            lastWheelArgb[0] = color;
        }

        alphaLabel.setText(String.valueOf(a));
        setThumbPos(alphaThumb, a, 255);
        if (color != lastAlphaColor[0]) {
            applyAlphaGradient(alphaGradientBg, color);
            lastAlphaColor[0] = color;
        }

        hueLabel.setText(hue + "\u00B0");
        saturationLabel.setText(sat + "%");
        valueLabel.setText(val + "%");

        setThumbPos(hueThumb, hue, 360);
        setThumbPos(saturationThumb, sat, 100);
        setThumbPos(valueThumb, val, 100);

        if (hue != lastSatHue[0]) {
            applySatGradient(saturationGradientBg, hue);
            lastSatHue[0] = hue;
        }
        if (hue != lastValHue[0]) {
            applyValueGradient(valueGradientBg, hue, sat / 100f);
            lastValHue[0] = hue;
        }
    }

    private static void setupTransparencyChecker(View container) {
        int tileSize = dpToPx(container.getContext(), 8);
        Bitmap bitmap = Bitmap.createBitmap(tileSize * 2, tileSize * 2, Bitmap.Config.ARGB_8888);
        int light = Color.rgb(204, 204, 204);
        int dark = Color.rgb(153, 153, 153);
        for (int y = 0; y < tileSize * 2; y++) {
            for (int x = 0; x < tileSize * 2; x++) {
                bitmap.setPixel(x, y, ((x / tileSize) + (y / tileSize)) % 2 == 0 ? light : dark);
            }
        }
        BitmapDrawable checker = new BitmapDrawable(container.getContext().getResources(), bitmap);
        checker.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        container.setBackground(checker);
    }

    private static void initGradientDrawables(View satBar, View valBar, View alphaBar) {
        sSatGd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, SAT_COLORS);
        satBar.setBackground(sSatGd);
        sValGd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Color.BLACK, Color.BLACK});
        valBar.setBackground(sValGd);
        sAlphaGd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, ALPHA_COLORS);
        alphaBar.setBackground(new LayerDrawable(new Drawable[]{
                createCheckerboard(alphaBar.getContext()), sAlphaGd}));
    }

    private static void setThumbPos(View thumb, int progress, int max) {
        if (thumb == null) return;
        ViewGroup parent = (ViewGroup) thumb.getParent();
        if (parent == null) return;
        parent.post(() -> {
            if (thumb.getParent() == null) return;
            float pw = parent.getWidth();
            if (pw <= 0) return;
            float tw = thumb.getWidth();
            if (tw <= 0) return;
            float ratio = Math.max(0, Math.min(1, progress / (float) max));
            thumb.setTranslationX(ratio * (pw - tw));
        });
    }

    private static void setupSliderTouch(View touchArea, View thumb, int max,
                                          int[] progHolder, Runnable onUpdate) {
        touchArea.setOnTouchListener((v, event) -> {
            int a = event.getActionMasked();
            if (a == MotionEvent.ACTION_DOWN || a == MotionEvent.ACTION_MOVE) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                float rawX = event.getRawX();
                int[] loc = new int[2];
                v.getLocationOnScreen(loc);
                float x = rawX - loc[0];
                float w = v.getWidth();
                if (w <= 0) return true;
                float clampedX = Math.max(0, Math.min(w, x));
                float ratio = clampedX / w;
                int prog = Math.round(ratio * max);
                progHolder[0] = prog;
                float tw = thumb.getWidth();
                thumb.setTranslationX(clampedX - tw / 2f);
                if (onUpdate != null) onUpdate.run();
                return true;
            }
            if (a == MotionEvent.ACTION_UP || a == MotionEvent.ACTION_CANCEL) {
                v.getParent().requestDisallowInterceptTouchEvent(false);
                return true;
            }
            return false;
        });
    }

    private static void loadSavedColors(Activity activity, GridLayout grid, TextView countView,
                                         int[] hueProg, int[] satProg, int[] valProg, int[] opaProg,
                                         View hueThumb, View satThumb, View valThumb, View opaThumb,
                                         View saturationGradientBg, View valueGradientBg,
                                         View alphaGradientBg,
                                         int[] lastSatHue, int[] lastValHue,
                                         int[] lastAlphaColor, int[] lastWheelArgb,
                                         TriangleColorPickerView colorWheel,
                                         TextView colorPreview, TextView hexValue,
                                         TextView hsvValue, TextView rgbValue,
                                         TextView hueLabel, TextView saturationLabel,
                                         TextView valueLabel, TextView alphaLabel,
                                         View rgbSliderBody,
                                         TextView redValLabel, TextView greenValLabel,
                                         TextView blueValLabel,
                                         View redThumb, View greenThumb, View blueThumb,
                                         int[] redProg, int[] greenProg, int[] blueProg,
                                         View redGradientBg, View greenGradientBg,
                                         View blueGradientBg,
                                         boolean[] isUpdating) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String saved = prefs.getString(SAVED_COLORS_KEY, "");
        String[] hexes = saved.isEmpty() ? new String[0] : saved.split(",");
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
                                .setTitle("Gunakan Warna")
                                .setMessage(ColorNameResolver.getName(fc) + "?")
                                .setPositiveButton("Apply", (d, w) -> {
                                    isUpdating[0] = true;
                                    updateDisplays(fc, colorWheel, colorPreview, hexValue, hsvValue, rgbValue,
                                            hueLabel, saturationLabel, valueLabel, alphaLabel,
                                            hueThumb, satThumb, valThumb, opaThumb,
                                            hueProg, satProg, valProg, opaProg,
                                            saturationGradientBg, valueGradientBg, alphaGradientBg,
                                            lastSatHue, lastValHue, lastAlphaColor, lastWheelArgb,
                                            rgbSliderBody, redValLabel, greenValLabel, blueValLabel,
                                            redThumb, greenThumb, blueThumb, redProg, greenProg, blueProg,
                                            redGradientBg, greenGradientBg, blueGradientBg, isUpdating);
                                    isUpdating[0] = false;
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
                                    loadSavedColors(activity, grid, countView,
                                            hueProg, satProg, valProg, opaProg,
                                            hueThumb, satThumb, valThumb, opaThumb,
                                            saturationGradientBg, valueGradientBg, alphaGradientBg,
                                            lastSatHue, lastValHue, lastAlphaColor, lastWheelArgb,
                                            colorWheel, colorPreview, hexValue, hsvValue, rgbValue,
                                            hueLabel, saturationLabel, valueLabel, alphaLabel,
                                            rgbSliderBody, redValLabel, greenValLabel, blueValLabel,
                                            redThumb, greenThumb, blueThumb, redProg, greenProg, blueProg,
                                            redGradientBg, greenGradientBg, blueGradientBg, isUpdating);
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
                        ClipData data = ClipData.newPlainText("grid_color_idx", String.valueOf(idx));
                        v.startDragAndDrop(data, new View.DragShadowBuilder(v), v, 0);
                        return true;
                    });
                } catch (IllegalArgumentException e) {
                    setEmptyInner(activity, inner, grid, prefs, idx,
                            hueProg, satProg, valProg, opaProg,
                            hueThumb, satThumb, valThumb, opaThumb,
                            saturationGradientBg, valueGradientBg, alphaGradientBg,
                            lastSatHue, lastValHue, lastAlphaColor, lastWheelArgb,
                            colorWheel, colorPreview, hexValue, hsvValue, rgbValue,
                            hueLabel, saturationLabel, valueLabel, alphaLabel,
                            rgbSliderBody, redValLabel, greenValLabel, blueValLabel,
                            redThumb, greenThumb, blueThumb, redProg, greenProg, blueProg,
                            redGradientBg, greenGradientBg, blueGradientBg, isUpdating);
                }
            } else {
                setEmptyInner(activity, inner, grid, prefs, idx,
                        hueProg, satProg, valProg, opaProg,
                        hueThumb, satThumb, valThumb, opaThumb,
                        saturationGradientBg, valueGradientBg, alphaGradientBg,
                        lastSatHue, lastValHue, lastAlphaColor, lastWheelArgb,
                        colorWheel, colorPreview, hexValue, hsvValue, rgbValue,
                        hueLabel, saturationLabel, valueLabel, alphaLabel,
                        rgbSliderBody, redValLabel, greenValLabel, blueValLabel,
                        redThumb, greenThumb, blueThumb, redProg, greenProg, blueProg,
                        redGradientBg, greenGradientBg, blueGradientBg, isUpdating);
            }
        }

        if (init) {
            grid.post(() -> recalcGridCellSizes(grid, activity));
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
                                prefs.edit().putString(SAVED_COLORS_KEY, sb.toString()).apply();
                                loadSavedColors(activity, grid, countView,
                                        hueProg, satProg, valProg, opaProg,
                                        hueThumb, satThumb, valThumb, opaThumb,
                                        saturationGradientBg, valueGradientBg, alphaGradientBg,
                                        lastSatHue, lastValHue, lastAlphaColor, lastWheelArgb,
                                        colorWheel, colorPreview, hexValue, hsvValue, rgbValue,
                                        hueLabel, saturationLabel, valueLabel, alphaLabel,
                                        rgbSliderBody, redValLabel, greenValLabel, blueValLabel,
                                        redThumb, greenThumb, blueThumb, redProg, greenProg, blueProg,
                                        redGradientBg, greenGradientBg, blueGradientBg, isUpdating);
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
                                       int[] hueProg, int[] satProg, int[] valProg, int[] opaProg,
                                       View hueThumb, View satThumb, View valThumb, View opaThumb,
                                       View saturationGradientBg, View valueGradientBg,
                                       View alphaGradientBg,
                                       int[] lastSatHue, int[] lastValHue,
                                       int[] lastAlphaColor, int[] lastWheelArgb,
                                       TriangleColorPickerView colorWheel,
                                       TextView colorPreview, TextView hexValue,
                                       TextView hsvValue, TextView rgbValue,
                                       TextView hueLabel, TextView saturationLabel,
                                       TextView valueLabel, TextView alphaLabel,
                                       View rgbSliderBody,
                                       TextView redValLabel, TextView greenValLabel,
                                       TextView blueValLabel,
                                       View redThumb, View greenThumb, View blueThumb,
                                       int[] redProg, int[] greenProg, int[] blueProg,
                                       View redGradientBg, View greenGradientBg,
                                       View blueGradientBg,
                                       boolean[] isUpdating) {
        GradientDrawable border = new GradientDrawable();
        border.setShape(GradientDrawable.RECTANGLE);
        border.setStroke(dpToPx(activity, 2), Color.rgb(76, 175, 80));
        border.setColor(Color.TRANSPARENT);
        inner.setBackground(border);
        inner.setOnClickListener(v -> {
            int color = hsvToColor(hueProg[0], satProg[0], valProg[0], opaProg[0]);
            saveColor(activity, color, false);
            loadSavedColors(activity, grid, null,
                    hueProg, satProg, valProg, opaProg,
                    hueThumb, satThumb, valThumb, opaThumb,
                    saturationGradientBg, valueGradientBg, alphaGradientBg,
                    lastSatHue, lastValHue, lastAlphaColor, lastWheelArgb,
                    colorWheel, colorPreview, hexValue, hsvValue, rgbValue,
                    hueLabel, saturationLabel, valueLabel, alphaLabel,
                    rgbSliderBody, redValLabel, greenValLabel, blueValLabel,
                    redThumb, greenThumb, blueThumb, redProg, greenProg, blueProg,
                    redGradientBg, greenGradientBg, blueGradientBg, isUpdating);
        });
    }

    private static void recalcGridCellSizes(GridLayout grid, Context context) {
        int w = grid.getWidth();
        if (w <= 0) return;
        int margin = dpToPx(context, 1);
        int cellH = (w - margin * 2 * 8) / 8;
        if (cellH < 10) return;
        for (int i = 0; i < grid.getChildCount(); i++) {
            View child = grid.getChildAt(i);
            GridLayout.LayoutParams p = (GridLayout.LayoutParams) child.getLayoutParams();
            p.height = cellH;
            child.setLayoutParams(p);
        }
    }

    private static void saveColor(Activity activity, int color, boolean prepend) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
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

    private static void applyHueGradient(View bar) {
        int[] colors = new int[361];
        for (int i = 0; i <= 360; i++) {
            colors[i] = Color.HSVToColor(new float[]{i, 1f, 1f});
        }
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
        bar.setBackground(gd);
    }

    private static void applySatGradient(View bar, int hue) {
        for (int i = 0; i <= 50; i++) {
            SAT_COLORS[i] = Color.HSVToColor(new float[]{hue, (i * 2) / 100f, 1f});
        }
        if (sSatGd == null) {
            sSatGd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, SAT_COLORS);
            bar.setBackground(sSatGd);
        } else {
            sSatGd.setColors(SAT_COLORS);
        }
    }

    private static void applyValueGradient(View bar, int hue, float sat) {
        int fullColor = Color.HSVToColor(new float[]{hue, sat, 1f});
        if (sValGd == null) {
            sValGd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                    new int[]{Color.BLACK, fullColor});
            bar.setBackground(sValGd);
        } else {
            sValGd.setColors(new int[]{Color.BLACK, fullColor});
        }
    }

    private static void applyAlphaGradient(View bar, int color) {
        ALPHA_COLORS[0] = color & 0x00FFFFFF;
        ALPHA_COLORS[1] = color | 0xFF000000;
        if (sAlphaGd == null) {
            sAlphaGd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, ALPHA_COLORS);
            bar.setBackground(new LayerDrawable(new Drawable[]{
                    createCheckerboard(bar.getContext()), sAlphaGd}));
        } else {
            sAlphaGd.setColors(ALPHA_COLORS);
        }
    }

    private static void applyRedGradient(View bar) {
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Color.BLACK, Color.RED});
        bar.setBackground(gd);
    }

    private static void applyGreenGradient(View bar) {
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Color.BLACK, Color.GREEN});
        bar.setBackground(gd);
    }

    private static void applyBlueGradient(View bar) {
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Color.BLACK, Color.BLUE});
        bar.setBackground(gd);
    }

    private static Drawable createCheckerboard(Context context) {
        if (sCheckerDrawable != null) return sCheckerDrawable;
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
        sCheckerDrawable = d;
        return d;
    }

    private static int hsvToColor(int hue, int sat, int val, int alpha) {
        return Color.HSVToColor(alpha, new float[]{hue, sat / 100f, val / 100f});
    }

    private static String hexColor(int a, int r, int g, int b) {
        return String.format("%02X%02X%02X%02X", a, r, g, b);
    }

    private static int parseHex(String hex) {
        if (hex.startsWith("#")) hex = hex.substring(1);
        if (hex.length() == 6) hex = "FF" + hex;
        if (hex.length() != 8) throw new IllegalArgumentException();
        return (int) Long.parseLong(hex, 16);
    }

    private static void copyToClipboard(Context context, String text) {
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("color", text));
    }

    private static int dpToPx(Context context, int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }
}
