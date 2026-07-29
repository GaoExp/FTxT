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
    private static final String SHOW_NAME_KEY = "cp_show_color_name";
    private static final int MAX_SAVED_COLORS = 16;

    private TriangleColorPickerView colorWheel;

    private View previewContainer;
    private TextView colorPreview;
    private TextView hexValue, hsvValue, rgbValue;
    private ImageButton hexEditButton;

    private SeekBar alphaSeek;

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
    private int lastNamedColor = 0;
    private String lastColorName = "";
    private int lastSatHue = -1;
    private int lastValHue = -1;
    private int lastAlphaColor = 0;
    private int lastWheelArgb = 0;

    private Drawable checkerDrawable;
    private GradientDrawable satGd, valGd, alphaGd;
    private final int[] satColors = new int[51];
    private final int[] alphaColors = new int[2];

    public ColorPickerPanelController(MainActivity activity) {
        this.activity = activity;
        bindViews();
        loadConfig();
        setupListeners();
    }

    public ColorPickerPanelController(MainActivity activity, View rootView) {
        this.activity = activity;
        bindViews(rootView);
        loadConfig();
        setupListeners();
    }

    private void bindViews() {
        bindViews(activity.findViewById(android.R.id.content));
    }

    private void bindViews(View rootView) {
        colorWheel = rootView.findViewById(R.id.cp_colorWheel);

        previewContainer = rootView.findViewById(R.id.previewContainer);
        colorPreview = rootView.findViewById(R.id.cp_colorPreview);
        hexValue = rootView.findViewById(R.id.cp_hexValue);
        hsvValue = rootView.findViewById(R.id.cp_hsvValue);
        rgbValue = rootView.findViewById(R.id.cp_rgbValue);
        hexEditButton = rootView.findViewById(R.id.cp_hexEditButton);

        nameToggle = rootView.findViewById(R.id.nameToggle);

        alphaSeek = rootView.findViewById(R.id.alphaSeek);

        hueThumb = rootView.findViewById(R.id.cp_hueThumb);
        saturationThumb = rootView.findViewById(R.id.cp_saturationThumb);
        valueThumb = rootView.findViewById(R.id.valueThumb);
        alphaThumb = rootView.findViewById(R.id.cp_alphaThumb);
        hueTouchArea = rootView.findViewById(R.id.cp_hueTouchArea);
        saturationTouchArea = rootView.findViewById(R.id.cp_saturationTouchArea);
        valueTouchArea = rootView.findViewById(R.id.valueTouchArea);
        alphaTouchArea = rootView.findViewById(R.id.cp_alphaTouchArea);
        hueGradientBg = rootView.findViewById(R.id.cp_hueGradientBg);
        saturationGradientBg = rootView.findViewById(R.id.cp_saturationGradientBg);
        valueGradientBg = rootView.findViewById(R.id.valueGradientBg);
        alphaGradientBg = rootView.findViewById(R.id.cp_alphaGradientBg);
        hueLabel = rootView.findViewById(R.id.cp_hueLabel);
        saturationLabel = rootView.findViewById(R.id.cp_saturationLabel);
        valueLabel = rootView.findViewById(R.id.valueLabel);
        alphaLabel = rootView.findViewById(R.id.cp_alphaLabel);

        redThumb = rootView.findViewById(R.id.redThumb);
        greenThumb = rootView.findViewById(R.id.greenThumb);
        blueThumb = rootView.findViewById(R.id.blueThumb);
        redTouchArea = rootView.findViewById(R.id.redTouchArea);
        greenTouchArea = rootView.findViewById(R.id.greenTouchArea);
        blueTouchArea = rootView.findViewById(R.id.blueTouchArea);
        redGradientBg = rootView.findViewById(R.id.redGradientBg);
        greenGradientBg = rootView.findViewById(R.id.greenGradientBg);
        blueGradientBg = rootView.findViewById(R.id.blueGradientBg);
        redValLabel = rootView.findViewById(R.id.redValLabel);
        greenValLabel = rootView.findViewById(R.id.greenValLabel);
        blueValLabel = rootView.findViewById(R.id.blueValLabel);

        savedColorsGrid = rootView.findViewById(R.id.cp_savedColorsGrid);
        savedColorsCount = rootView.findViewById(R.id.cp_savedColorsCount);
        addSavedColor = rootView.findViewById(R.id.cp_addSavedColor);
        collapseToggle = rootView.findViewById(R.id.cp_collapseToggle);
        savedColorsHeader = rootView.findViewById(R.id.savedColorsHeader);
        savedColorsGrid.setVisibility(View.GONE);
        collapseToggle.setText("\u25B2");
        rgbSliderBody = rootView.findViewById(R.id.rgbSliderBody);
        rgbHeader = rootView.findViewById(R.id.rgbHeader);
    }

    private void loadConfig() {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        showColorName = prefs.getBoolean(SHOW_NAME_KEY, true);
        nameToggle.setChecked(showColorName);

        initGradientDrawables();

        int initialColor = Color.rgb(255, 0, 255);
        setColorValues(initialColor);

        setupSliders();
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
            if (isUpdating) return;
            isUpdating = true;
            int a = alphaSeek.getProgress();
            color = Color.argb(a, Color.red(color), Color.green(color), Color.blue(color));
            updateFromColor(color);
            isUpdating = false;
        });

        setupSliderTouch(hueTouchArea, hueThumb, 360, hueProg, this::updateSliderOutput);

        setupSliderTouch(saturationTouchArea, saturationThumb, 100, satProg, this::updateSliderOutput);
        setupSliderTouch(valueTouchArea, valueThumb, 100, valProg, this::updateSliderOutput);
        setupSliderTouch(alphaTouchArea, alphaThumb, 255, opaProg, this::updateSliderOutput);

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

    private void initGradientDrawables() {
        satGd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, satColors);
        saturationGradientBg.setBackground(satGd);
        valGd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{Color.BLACK, Color.BLACK});
        valueGradientBg.setBackground(valGd);
        alphaGd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, alphaColors);
        alphaGradientBg.setBackground(new LayerDrawable(new Drawable[]{createCheckerboard(), alphaGd}));
    }

    private void setupSliders() {
        int color = hsvToColor(hueProg[0], satProg[0], valProg[0], opaProg[0]);
        applyHueGradient(hueGradientBg);
        applySatGradient(saturationGradientBg, hueProg[0]);
        applyValueGradient(valueGradientBg, hueProg[0], satProg[0] / 100f);
        applyAlphaGradient(alphaGradientBg, color);
        setThumbPos(hueThumb, hueProg[0], 360);
        setThumbPos(saturationThumb, satProg[0], 100);
        setThumbPos(valueThumb, valProg[0], 100);
        setThumbPos(alphaThumb, opaProg[0], 255);
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
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        int hue = Math.round(hsv[0]);
        int sat = Math.round(hsv[1] * 100);
        int val = Math.round(hsv[2] * 100);

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
        if (color != lastNamedColor) {
            lastNamedColor = color;
            lastColorName = ColorNameResolver.getName(color);
        }
        colorPreview.setText(showColorName ? lastColorName : "");
        int textColor = (r * 0.299 + g * 0.587 + b * 0.114) > 128 ? Color.BLACK : Color.WHITE;
        colorPreview.setTextColor(textColor);

        hexValue.setText("HEX: #" + hexColor(a, r, g, b));
        hsvValue.setText("HSV: " + hue + "\u00B0, " + sat + "%, " + val + "%");
        rgbValue.setText("ARGB: " + a + ", " + r + ", " + g + ", " + b);

        if (color != lastWheelArgb) {
            colorWheel.setColor(color);
            lastWheelArgb = color;
        }

        alphaLabel.setText(String.valueOf(a));
        setThumbPos(alphaThumb, a, 255);
        if (color != lastAlphaColor) {
            applyAlphaGradient(alphaGradientBg, color);
            lastAlphaColor = color;
        }

        hueLabel.setText(hue + "\u00B0");
        saturationLabel.setText(sat + "%");
        valueLabel.setText(val + "%");

        setThumbPos(hueThumb, hue, 360);
        setThumbPos(saturationThumb, sat, 100);
        setThumbPos(valueThumb, val, 100);

        if (hue != lastSatHue) {
            applySatGradient(saturationGradientBg, hue);
            lastSatHue = hue;
        }
        if (hue != lastValHue) {
            applyValueGradient(valueGradientBg, hue, sat / 100f);
            lastValHue = hue;
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
        checkerDrawable = checker;
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

    private Drawable createCheckerboard() {
        if (checkerDrawable != null) return checkerDrawable;
        int size = dpToPx(activity, 8);
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
        BitmapDrawable d = new BitmapDrawable(activity.getResources(), bmp);
        d.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        d.setFilterBitmap(false);
        checkerDrawable = d;
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
        for (int i = 0; i <= 50; i++) {
            satColors[i] = hsvToColor(hue, i * 2, 100, 255);
        }
        satGd.setColors(satColors);
    }

    private void applyValueGradient(View bar, int hue, float sat) {
        int fullColor = hsvToColor(hue, Math.round(sat * 100), 100, 255);
        valGd.setColors(new int[]{Color.BLACK, fullColor});
    }

    private void applyAlphaGradient(View bar, int color) {
        alphaColors[0] = color & 0x00FFFFFF;
        alphaColors[1] = color | 0xFF000000;
        alphaGd.setColors(alphaColors);
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

    private static String hexColor(int a, int r, int g, int b) {
        return String.format("%02X%02X%02X%02X", a, r, g, b);
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
