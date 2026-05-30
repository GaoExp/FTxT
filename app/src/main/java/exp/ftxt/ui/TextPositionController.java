package exp.ftxt.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import exp.ftxt.R;
import exp.ftxt.core.FloatingService;
import exp.ftxt.features.text.TextConfig;
import exp.ftxt.features.text.TextModule;
import exp.ftxt.shared.ui.DpadController;
import exp.ftxt.shared.ui.PositionPresetManager;
import exp.ftxt.shared.ui.SliderPositionController;

public class TextPositionController {

    private final Activity activity;
    private final SharedPreferences prefs;

    private View btnUp, btnDown, btnLeft, btnRight;
    private String currentOrientation;

    private DpadController dpad;
    private PositionPresetManager presetManager;
    private SliderPositionController sliderController;
    private TextView coordDisplay;
    private View btnExportImport;
    private int displayWidth, displayHeight;

    private static final String PREFS_NAME = "ftxt_prefs";

    public TextPositionController(Activity activity) {
        this.activity = activity;
        this.prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        int orientation = activity.getResources().getConfiguration().orientation;
        currentOrientation = (orientation == Configuration.ORIENTATION_LANDSCAPE) ? "land" : "port";
        loadPositionFromPrefs(currentOrientation);

        FloatingService.setTextOrientationSuffixStatic(currentOrientation);

        bindViews();

        WindowManager wm = activity.getWindowManager();
        DisplayMetrics realMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(realMetrics);
        displayWidth = realMetrics.widthPixels;
        displayHeight = realMetrics.heightPixels;

        TextModule.onPositionUpdate = this::syncAll;

        presetManager = new PositionPresetManager(activity, (x, y) -> onPositionChanged(x, y));
        if (btnExportImport != null) {
            btnExportImport.setOnClickListener(v -> presetManager.showExportImportDialog());
        }
        sliderController = new SliderPositionController(
                activity.findViewById(R.id.posXSeekBar),
                activity.findViewById(R.id.posYSeekBar),
                activity.findViewById(R.id.posXLabel),
                activity.findViewById(R.id.posYLabel),
                (x, y) -> onPositionChanged(x, y)
        );
        setupListeners();
        syncAll();
    }

    private void bindViews() {
        btnUp = activity.findViewById(R.id.btnUp);
        btnDown = activity.findViewById(R.id.btnDown);
        btnLeft = activity.findViewById(R.id.btnLeft);
        btnRight = activity.findViewById(R.id.btnRight);
        coordDisplay = activity.findViewById(R.id.posCoordDisplay);
        btnExportImport = activity.findViewById(R.id.btnExportImport);
    }

    private void setupListeners() {
        dpad = new DpadController(btnUp, btnDown, btnLeft, btnRight, (dx, dy) -> {
            onPositionChanged(clamp(TextConfig.posX + dx), clamp(TextConfig.posY + dy));
        });

        View btnSavePreset = activity.findViewById(R.id.btnSavePreset);
        View btnLoadPreset = activity.findViewById(R.id.btnLoadPreset);
        if (btnSavePreset != null) {
            btnSavePreset.setOnClickListener(v -> presetManager.showSavePresetDialog(TextConfig.posX, TextConfig.posY));
        }
        if (btnLoadPreset != null) {
            btnLoadPreset.setOnClickListener(v -> presetManager.showLoadPresetDialog());
        }
    }

    private static float clamp(float val) {
        return Math.max(0, Math.min(1, val));
    }

    private void onPositionChanged(float x, float y) {
        TextConfig.posX = x;
        TextConfig.posY = y;
        syncAll();
        savePositionToPrefs(currentOrientation);
        FloatingService.updateTextPositionStatic();
    }

    // ====================================================================
    // Orientation mode — Potret / Lanskap
    // ====================================================================

    private void setOrientationMode(String mode) {
        if (mode.equals(currentOrientation)) return;

        savePositionToPrefs(currentOrientation);

        currentOrientation = mode;
        FloatingService.setTextOrientationSuffixStatic(mode);
        loadPositionFromPrefs(mode);

        syncAll();
        FloatingService.updateTextPositionStatic();
    }

    private void savePositionToPrefs(String orient) {
        String sfx = "_" + orient;
        prefs.edit()
                .putFloat("text_pos_x" + sfx, TextConfig.posX)
                .putFloat("text_pos_y" + sfx, TextConfig.posY)
                .apply();
    }

    private void loadPositionFromPrefs(String orient) {
        String sfx = "_" + orient;
        TextConfig.posX = prefs.getFloat("text_pos_x" + sfx, 0.5f);
        TextConfig.posY = prefs.getFloat("text_pos_y" + sfx, 0.5f);
    }

    // ====================================================================

    public void cleanup() {
        TextModule.onPositionUpdate = null;
        if (dpad != null) dpad.cleanup();
        if (presetManager != null) presetManager.cleanup();
    }

    public void refresh() {
        syncAll();
    }

    public void syncAll() {
        sliderController.sync(TextConfig.posX, TextConfig.posY);
        updateCoordDisplay();
    }

    private void updateCoordDisplay() {
        if (coordDisplay == null) return;
        int px, py;
        int[] pos = FloatingService.getTextCurrentPosition();
        if (pos != null) {
            px = pos[0];
            py = pos[1];
        } else {
            px = Math.round(TextConfig.posX * displayWidth);
            py = Math.round(TextConfig.posY * displayHeight);
        }
        coordDisplay.setText(px + "X" + py);
    }

}
