package exp.ftxt.ui;

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
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import exp.ftxt.MainActivity;
import exp.ftxt.R;
import exp.ftxt.features.color_picker.TriangleColorPickerView;
import exp.ftxt.shared.color.ColorNameResolver;

public class ColorPickerPanelController {

    private final MainActivity activity;
    private static final String PREFS_NAME = "ftxt_prefs";
    private static final String SAVED_COLORS_KEY = "cp_saved_colors";
    private static final String MODE_KEY = "color_picker_mode";
    private static final String SHOW_NAME_KEY = "cp_show_color_name";
    private static final int MAX_SAVED_COLORS = 16;

    private boolean isSliderMode = false;

    private View wheelContainer;
    private TriangleColorPickerView colorWheel;
    private View sliderPanel;
    private ImageButton switchModeBtn;
    private TextView modeLabel;

    private View previewContainer;
    private TextView colorPreview;
    private TextView hexValue, hsvValue, rgbValue;
    private ImageButton hexEditButton;

    private SeekBar alphaSeek;

    private SeekBar hueSeekBar;

    private CheckBox nameToggle;
    private boolean showColorName = true;

    private View hueThumb, saturationThumb, valueThumb, alphaThumb;
    private View hueTouchArea, saturationTouchArea, valueTouchArea, alphaTouchArea;
    private View hueGradientBg, saturationGradientBg, valueGradientBg, alphaGradientBg;
    private TextView hueLabel, saturationLabel, valueLabel, alphaLabel;

    private View redThumb, greenThumb, blueThumb;
    private View redTouchArea, greenTouchArea, blueTouchArea;
    private View redGradientBg, greenGradientBg, blueGradientBg;
    private TextView redValLabel, greenValLabel, blueValLabel;

    private GridLayout savedColorsGrid;
    private TextView savedColorsCount, addSavedColor, collapseToggle;
    private View rgbSliderBody, rgbHeader;
    private View savedColorsHeader;

    private final int[] hueProg = {0};
    private final int[] satProg = {100};
    private final int[] valProg = {100};
    private final int[] opaProg = {255};
    private final int[] redProg = {255};
    private final int[] greenProg = {125};
    private final int[] blueProg = {255};

    private boolean isUpdating = false;

    public ColorPickerPanelController(MainActivity activity) {
        this.activity = activity;
        bindViews();
        loadConfig();
        setupListeners();
    }

    private void bindViews() {
        wheelContainer = activity.findViewById(R.id.cp_wheelContainer);
        colorWheel = activity.findViewById(R.id.cp_colorWheel);
        sliderPanel = activity.findViewById(R.id.sliderPanel);
        switchModeBtn = activity.findViewById(R.id.cp_switchModeButton);
        modeLabel = activity.findViewById(R.id.cp_modeLabel);

        previewContainer = activity.findViewById(R.id.previewContainer);
        colorPreview = activity.findViewById(R.id.cp_colorPreview);
        hexValue = activity.findViewById(R.id.cp_hexValue);
        hsvValue = activity.findViewById(R.id.cp_hsvValue);
        rgbValue = activity.findViewById(R.id.cp_rgbValue);
        hexEditButton = activity.findViewById(R.id.cp_hexEditButton);

        nameToggle = activity.findViewById(R.id.nameToggle);

        alphaSeek = activity.findViewById(R.id.alphaSeek);

        hueSeekBar = activity.findViewById(R.id.cp_hueSeekBar);

        hueThumb = activity.findViewById(R.id.cp_hueThumb);
        saturationThumb = activity.findViewById(R.id.cp_saturationThumb);
        valueThumb = activity.findViewById(R.id.valueThumb);
        alphaThumb = activity.findViewById(R.id.cp_alphaThumb);
        hueTouchArea = activity.findViewById(R.id.cp_hueTouchArea);
        saturationTouchArea = activity.findViewById(R.id.cp_saturationTouchArea);
        valueTouchArea = activity.findViewById(R.id.valueTouchArea);
        alphaTouchArea = activity.findViewById(R.id.cp_alphaTouchArea);
        hueGradientBg = activity.findViewById(R.id.cp_hueGradientBg);
        saturationGradientBg = activity.findViewById(R.id.cp_saturationGradientBg);
        valueGradientBg = activity.findViewById(R.id.valueGradientBg);
        alphaGradientBg = activity.findViewById(R.id.cp_alphaGradientBg);
        hueLabel = activity.findViewById(R.id.cp_hueLabel);
        saturationLabel = activity.findViewById(R.id.cp_saturationLabel);
        valueLabel = activity.findViewById(R.id.valueLabel);
        alphaLabel = activity.findViewById(R.id.cp_alphaLabel);

        redThumb = activity.findViewById(R.id.redThumb);
        greenThumb = activity.findViewById(R.id.greenThumb);
        blueThumb = activity.findViewById(R.id.blueThumb);
        redTouchArea = activity.findViewById(R.id.redTouchArea);
        greenTouchArea = activity.findViewById(R.id.greenTouchArea);
        blueTouchArea = activity.findViewById(R.id.blueTouchArea);
        redGradientBg = activity.findViewById(R.id.redGradientBg);
        greenGradientBg = activity.findViewById(R.id.greenGradientBg);
        blueGradientBg = activity.findViewById(R.id.blueGradientBg);
        redValLabel = activity.findViewById(R.id.redValLabel);
        greenValLabel = activity.findViewById(R.id.greenValLabel);
        blueValLabel = activity.findViewById(R.id.blueValLabel);

        savedColorsGrid = activity.findViewById(R.id.cp_savedColorsGrid);
        savedColorsCount = activity.findViewById(R.id.cp_savedColorsCount);
        addSavedColor = activity.findViewById(R.id.cp_addSavedColor);
        collapseToggle = activity.findViewById(R.id.cp_collapseToggle);
        savedColorsHeader = activity.findViewById(R.id.savedColorsHeader);
        savedColorsGrid.setVisibility(View.GONE);
        collapseToggle.setText("\u25B2");
        rgbSliderBody = activity.findViewById(R.id.rgbSliderBody);
        rgbHeader = activity.findViewById(R.id.rgbHeader);
    }

    private void loadConfig() {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        showColorName = prefs.getBoolean(SHOW_NAME_KEY, true);
        nameToggle.setChecked(showColorName);

        isSliderMode = prefs.getString(MODE_KEY, "disk").equals("slider");
        setupMode(isSliderMode);

        int initialColor = Color.rgb(255, 0, 255);
        setColorValues(initialColor);

        loadSavedColors();
        setupTransparencyChecker();
    }

    private void setupListeners() {
        SeekBar.OnSeekBarChangeListener rgbListener = new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int p, boolean f) { onRgbChanged(); }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        };

        alphaSeek.setOnSeekBarChangeListener(rgbListener);

        setupSliderTouch(redTouchArea, redThumb, 255, redProg, this::onRgbChanged);
        setupSliderTouch(greenTouchArea, greenThumb, 255, greenProg, this::onRgbChanged);
        setupSliderTouch(blueTouchArea, blueThumb, 255, blueProg, this::onRgbChanged);

        colorWheel.setOnColorChangeListener(color -> {
            if (isUpdating || isSliderMode) return;
            isUpdating = true;
            int a = alphaSeek.getProgress();
            color = Color.argb(a, Color.red(color), Color.green(color), Color.blue(color));
            updateFromColor(color);
            isUpdating = false;
        });

        hueSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int p, boolean f) {
                if (isUpdating || !isSliderMode) return;
                isUpdating = true;
                hueProg[0] = p;
                int color = hsvToColor(hueProg[0], satProg[0], valProg[0], opaProg[0]);
                updateFromColor(color);
                isUpdating = false;
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        setupSliderTouch(hueTouchArea, hueThumb, 360, hueProg, this::updateSliderOutput);

        setupSliderTouch(saturationTouchArea, saturationThumb, 100, satProg, this::updateSliderOutput);
        setupSliderTouch(valueTouchArea, valueThumb, 100, valProg, this::updateSliderOutput);
        setupSliderTouch(alphaTouchArea, alphaThumb, 255, opaProg, this::updateSliderOutput);

        switchModeBtn.setOnClickListener(v -> toggleMode());

        hexEditButton.setOnClickListener(v -> showHexEditor());

        hexValue.setOnLongClickListener(v -> {
            copyToClipboard(hexValue.getText().toString());
            Toast.makeText(activity, "HEX disalin", Toast.LENGTH_SHORT).show();
            return true;
        });

        hsvValue.setOnLongClickListener(v -> {
            copyToClipboard(hsvValue.getText().toString());
            Toast.makeText(activity, "HSV disalin", Toast.LENGTH_SHORT).show();
            return true;
        });

        rgbValue.setOnLongClickListener(v -> {
            copyToClipboard(rgbValue.getText().toString());
            Toast.makeText(activity, "ARGB disalin", Toast.LENGTH_SHORT).show();
            return true;
        });

        addSavedColor.setOnClickListener(v -> {
            int color = getCurrentColor();
            saveColor(color, true);
            loadSavedColors();
        });

        savedColorsHeader.setOnClickListener(v -> {
            boolean expanded = savedColorsGrid.getVisibility() == View.VISIBLE;
            savedColorsGrid.setVisibility(expanded ? View.GONE : View.VISIBLE);
            collapseToggle.setText(expanded ? "\u25BC" : "\u25B2");
            if (!expanded) {
                savedColorsGrid.post(this::recalcGridCellSizes);
            }
        });

        rgbHeader.setOnClickListener(v -> {
            boolean expanded = rgbSliderBody.getVisibility() == View.VISIBLE;
            rgbSliderBody.setVisibility(expanded ? View.GONE : View.VISIBLE);
            if (!expanded) {
                isUpdating = true;
                updateDisplays(getCurrentColor());
                isUpdating = false;
            }
        });

        nameToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            showColorName = isChecked;
            activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit().putBoolean(SHOW_NAME_KEY, isChecked).apply();
            int color = getCurrentColor();
            updateDisplays(color);
        });
    }

    public void onPanelShown() {
    }

    public void cleanup() {
        colorWheel = null;
    }

    private void toggleMode() {
        isSliderMode = !isSliderMode;
        activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putString(MODE_KEY, isSliderMode ? "slider" : "disk").apply();
        int color = getCurrentColor();
        setupMode(isSliderMode);
        setColorValues(color);
    }

    private void setupMode(boolean slider) {
        wheelContainer.setVisibility(slider ? View.GONE : View.VISIBLE);
        sliderPanel.setVisibility(slider ? View.VISIBLE : View.GONE);
        modeLabel.setText(slider ? "Hue Slider" : "Color Wheel");
        int color = hsvToColor(hueProg[0], satProg[0], valProg[0], opaProg[0]);
        applyAlphaGradient(alphaGradientBg, color);
        setThumbPos(alphaThumb, opaProg[0], 255);
        if (slider) {
            applyHueGradient(hueGradientBg);
            applySatGradient(saturationGradientBg, hueProg[0]);
            applyValueGradient(valueGradientBg, hueProg[0], satProg[0] / 100f);
            setThumbPos(hueThumb, hueProg[0], 360);
            setThumbPos(saturationThumb, satProg[0], 100);
            setThumbPos(valueThumb, valProg[0], 100);
        }
    }

    private void onRgbChanged() {
        if (isUpdating) return;
        isUpdating = true;
        int color = Color.argb(alphaSeek.getProgress(),
                redProg[0], greenProg[0], blueProg[0]);
        updateDisplays(color);
        isUpdating = false;
    }

    private void updateSliderOutput() {
        if (isUpdating) return;
        isUpdating = true;
        int color = hsvToColor(hueProg[0], satProg[0], valProg[0], opaProg[0]);
        updateDisplays(color);
        isUpdating = false;
    }

    private void updateFromColor(int color) {
        updateDisplays(color);
    }

    private void updateDisplays(int color) {
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
        alphaSeek.setProgress(a);

        int val = computeValue(color);
        int hue = computeHue(color);
        int sat = computeSaturation(color);

        if (val == 0) {
            hue = hueProg[0];
            sat = satProg[0];
        }
        if (sat == 0) {
            hue = hueProg[0];
        }

        hueProg[0] = hue;
        satProg[0] = sat;
        valProg[0] = val;
        opaProg[0] = a;

        if (rgbVisible) {
            redValLabel.setText(String.format("%d", r));
            greenValLabel.setText(String.format("%d", g));
            blueValLabel.setText(String.format("%d", b));
            setThumbPos(redThumb, r, 255);
            setThumbPos(greenThumb, g, 255);
            setThumbPos(blueThumb, b, 255);
        }

        colorPreview.setBackgroundColor(color);
        String name = ColorNameResolver.getName(color);
        colorPreview.setText(showColorName ? name : "");
        int textColor = (r * 0.299 + g * 0.587 + b * 0.114) > 128 ? Color.BLACK : Color.WHITE;
        colorPreview.setTextColor(textColor);

        hexValue.setText(String.format("HEX: #%02X%02X%02X%02X", a, r, g, b));
        hsvValue.setText(String.format("HSV: %d\u00B0, %d%%, %d%%", hue, sat, val));
        rgbValue.setText(String.format("ARGB: %d, %d, %d, %d", a, r, g, b));

        colorWheel.setColor(color);

        hueSeekBar.setProgress(hue);

        alphaLabel.setText(String.format("%d", a));
        setThumbPos(alphaThumb, a, 255);
        applyAlphaGradient(alphaGradientBg, color);

        if (isSliderMode) {
            hueLabel.setText(String.format("%d\u00B0", hue));
            saturationLabel.setText(String.format("%d%%", sat));
            valueLabel.setText(String.format("%d%%", val));

            setThumbPos(hueThumb, hue, 360);
            setThumbPos(saturationThumb, sat, 100);
            setThumbPos(valueThumb, val, 100);

            applySatGradient(saturationGradientBg, hue);
            applyValueGradient(valueGradientBg, hue, sat / 100f);
        }
        if (rgbVisible) {
            applyRedGradient(redGradientBg);
            applyGreenGradient(greenGradientBg);
            applyBlueGradient(blueGradientBg);
        }
    }

    private void setColorValues(int color) {
        isUpdating = true;
        updateDisplays(color);
        isUpdating = false;
    }

    private int getCurrentColor() {
        return hsvToColor(hueProg[0], satProg[0], valProg[0], opaProg[0]);
    }

    private void showHexEditor() {
        int color = getCurrentColor();
        String currentHex = String.format("#%02X%02X%02X%02X",
                Color.alpha(color), Color.red(color),
                Color.green(color), Color.blue(color));

        EditText input = new EditText(activity);
        input.setText(currentHex);
        input.setSelection(input.length());

        new AlertDialog.Builder(activity)
                .setTitle("Edit HEX")
                .setView(input)
                .setPositiveButton("OK", (d, w) -> {
                    try {
                        int parsed = parseHex(input.getText().toString().trim());
                        isUpdating = true;
                        updateDisplays(parsed);
                        isUpdating = false;
                    } catch (IllegalArgumentException e) {
                        Toast.makeText(activity, "HEX tidak valid", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void setupTransparencyChecker() {
        int tileSize = dpToPx(activity, 8);
        Bitmap bitmap = Bitmap.createBitmap(tileSize * 2, tileSize * 2, Bitmap.Config.ARGB_8888);
        int valht = Color.rgb(204, 204, 204);
        int dark = Color.rgb(153, 153, 153);
        for (int y = 0; y < tileSize * 2; y++) {
            for (int x = 0; x < tileSize * 2; x++) {
                bitmap.setPixel(x, y, ((x / tileSize) + (y / tileSize)) % 2 == 0 ? valht : dark);
            }
        }
        BitmapDrawable checker = new BitmapDrawable(activity.getResources(), bitmap);
        checker.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        previewContainer.setBackground(checker);
    }

    private void loadSavedColors() {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String saved = prefs.getString(SAVED_COLORS_KEY, "");
        String[] hexes = saved.isEmpty() ? new String[0] : saved.split(",");
        savedColorsCount.setText(hexes.length + "/16");
        int margin = dpToPx(activity, 2);
        boolean init = savedColorsGrid.getChildCount() == 0;

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
                savedColorsGrid.addView(cell);
            } else {
                cell = (FrameLayout) savedColorsGrid.getChildAt(i);
            }

            View inner = cell.getChildAt(0);
            final int idx = i;

            if (i < hexes.length && !hexes[i].isEmpty()) {
                try {
                    int color = parseHex(hexes[i]);
                    inner.setBackgroundColor(color);
                    final int fc = color;
                    inner.setOnClickListener(v -> {
                        new AlertDialog.Builder(activity)
                                .setTitle("Gunakan Warna")
                                .setMessage(ColorNameResolver.getName(fc) + "?")
                                .setPositiveButton("Apply", (di, w) -> {
                                    isUpdating = true;
                                    updateDisplays(fc);
                                    isUpdating = false;
                                })
                                .setNegativeButton("Batal", null)
                                .setNeutralButton("Hapus", (di, w) -> {
                                    removeSavedColor(idx);
                                })
                                .show();
                    });
                } catch (IllegalArgumentException e) {
                    setEmptyCell(inner, idx);
                }
            } else {
                setEmptyCell(inner, idx);
            }
        }

        if (init && savedColorsGrid.getWidth() > 0) {
            recalcGridCellSizes();
        }
    }

    private void setEmptyCell(View inner, int idx) {
        GradientDrawable border = new GradientDrawable();
        border.setShape(GradientDrawable.RECTANGLE);
        border.setStroke(dpToPx(activity, 2), Color.rgb(76, 175, 80));
        border.setColor(Color.TRANSPARENT);
        inner.setBackground(border);
        inner.setOnClickListener(v -> {
            int color = getCurrentColor();
            saveColor(color, false);
            loadSavedColors();
        });
    }

    private void recalcGridCellSizes() {
        int w = savedColorsGrid.getWidth();
        if (w <= 0) return;
        int margin = dpToPx(activity, 2);
        int cellH = (w - margin * 2 * 8) / 8;
        if (cellH < 10) return;
        for (int i = 0; i < savedColorsGrid.getChildCount(); i++) {
            View child = savedColorsGrid.getChildAt(i);
            GridLayout.LayoutParams p = (GridLayout.LayoutParams) child.getLayoutParams();
            p.height = cellH;
            child.setLayoutParams(p);
        }
    }

    private void saveColor(int color, boolean prepend) {
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
            Toast.makeText(activity, "Maksimal 16 warna tersimpan", Toast.LENGTH_SHORT).show();
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

    private void removeSavedColor(int index) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String cur = prefs.getString(SAVED_COLORS_KEY, "");
        String[] all = cur.split(",");
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < all.length; j++) {
            if (j != index && !all[j].isEmpty()) {
                if (sb.length() > 0) sb.append(",");
                sb.append(all[j]);
            }
        }
        prefs.edit().putString(SAVED_COLORS_KEY, sb.toString()).apply();
        loadSavedColors();
    }

    private void setThumbPos(View thumb, int progress, int max) {
        if (thumb == null) return;
        ViewGroup parent = (ViewGroup) thumb.getParent();
        if (parent == null) return;
        float pw = parent.getWidth();
        float tw = thumb.getWidth();
        if (pw <= 0 || tw <= 0) {
            parent.post(() -> setThumbPos(thumb, progress, max));
            return;
        }
        float ratio = Math.max(0, Math.min(1, progress / (float) max));
        float transX = ratio * pw - tw / 2f;
        transX = Math.max(0, Math.min(pw - tw, transX));
        thumb.setTranslationX(transX);
    }

    private void setupSliderTouch(View touchArea, View thumb, int max,
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

    private static Drawable createCheckerboard(Context context) {
        int size = dpToPx(context, 8);
        Bitmap bmp = Bitmap.createBitmap(size * 2, size * 2, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint();
        int valht = Color.rgb(200, 200, 200);
        int dark = Color.rgb(155, 155, 155);
        paint.setColor(valht);
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

    private int hsvToColor(int hue, int sat, int val, int alpha) {
        return Color.HSVToColor(alpha, new float[]{hue, sat / 100f, val / 100f});
    }

    private int computeValue(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        return Math.round(hsv[2] * 100);
    }

    private int computeSaturation(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        return Math.round(hsv[1] * 100);
    }

    private int computeHue(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        return Math.round(hsv[0]);
    }

    private void applyHueGradient(View bar) {
        int[] colors = new int[361];
        for (int i = 0; i <= 360; i++) {
            colors[i] = Color.HSVToColor(new float[]{i, 1f, 1f});
        }
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
        bar.setBackground(gd);
    }

    private void applySatGradient(View bar, int hue) {
        int[] colors = new int[101];
        for (int i = 0; i <= 100; i++) {
            colors[i] = hsvToColor(hue, i, 100, 255);
        }
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
        bar.setBackground(gd);
    }

    private void applyValueGradient(View bar, int hue, float sat) {
        int fullColor = hsvToColor(hue, Math.round(sat * 100), 100, 255);
        int[] colors = new int[]{Color.BLACK, fullColor};
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
        bar.setBackground(gd);
    }

    private void applyAlphaGradient(View bar, int color) {
        int opaque = color | 0xFF000000;
        int[] colors = new int[]{color & 0x00FFFFFF, opaque};
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
        LayerDrawable layers = new LayerDrawable(new Drawable[]{
                createCheckerboard(activity),
                gd
        });
        bar.setBackground(layers);
    }

    private void applyRedGradient(View bar) {
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Color.BLACK, Color.RED});
        bar.setBackground(gd);
    }

    private void applyGreenGradient(View bar) {
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Color.BLACK, Color.GREEN});
        bar.setBackground(gd);
    }

    private void applyBlueGradient(View bar) {
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Color.BLACK, Color.BLUE});
        bar.setBackground(gd);
    }

    private static int parseHex(String hex) {
        if (hex.startsWith("#")) hex = hex.substring(1);
        if (hex.length() == 6) hex = "FF" + hex;
        if (hex.length() != 8) throw new IllegalArgumentException();
        return (int) Long.parseLong(hex, 16);
    }

    private void copyToClipboard(String text) {
        ClipboardManager cm = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("color", text));
    }

    private static int dpToPx(Context context, int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }
}
