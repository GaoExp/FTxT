package exp.ftxt.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.view.View;

import exp.ftxt.R;
import exp.ftxt.core.FloatingService;
import exp.ftxt.features.text.TextConfig;
import exp.ftxt.shared.ui.DpadController;
import exp.ftxt.shared.ui.PositionPresetManager;
import exp.ftxt.shared.ui.SliderPositionController;

public class PositionController {

    private final Activity activity;
    private final SharedPreferences prefs;

    private View btnUp, btnDown, btnLeft, btnRight;
    private View btnPortrait, btnLandscape;
    private String currentOrientation;

    private DpadController dpad;
    private PositionPresetManager presetManager;
    private SliderPositionController sliderController;

    private static final String PREFS_NAME = "ftxt_prefs";

    public PositionController(Activity activity) {
        this.activity = activity;
        this.prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        int orientation = activity.getResources().getConfiguration().orientation;
        currentOrientation = (orientation == Configuration.ORIENTATION_LANDSCAPE) ? "land" : "port";
        loadPositionFromPrefs(currentOrientation);

        FloatingService.setTextOrientationSuffixStatic(currentOrientation);

        bindViews();
        presetManager = new PositionPresetManager(activity, (x, y) -> onPositionChanged(x, y));
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
        btnPortrait = activity.findViewById(R.id.btnPortrait);
        btnLandscape = activity.findViewById(R.id.btnLandscape);
    }

    private void setupListeners() {
        dpad = new DpadController(btnUp, btnDown, btnLeft, btnRight, (dx, dy) -> {
            onPositionChanged(clamp(TextConfig.posX + dx), clamp(TextConfig.posY + dy));
        });

        View btnSavePreset = activity.findViewById(R.id.btnSavePreset);
        View btnLoadPreset = activity.findViewById(R.id.btnLoadPreset);
        View btnResetPos = activity.findViewById(R.id.btnResetPos);
        if (btnSavePreset != null) {
            btnSavePreset.setOnClickListener(v -> presetManager.showSavePresetDialog(TextConfig.posX, TextConfig.posY));
        }
        if (btnLoadPreset != null) {
            btnLoadPreset.setOnClickListener(v -> presetManager.showLoadPresetDialog());
        }
        if (btnResetPos != null) {
            btnResetPos.setOnClickListener(v -> resetPosition());
        }

        btnPortrait.setOnClickListener(v -> setOrientationMode("port"));
        btnLandscape.setOnClickListener(v -> setOrientationMode("land"));
        updateOrientationButtons();
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

    private void resetPosition() {
        onPositionChanged(0.5f, 0.5f);
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
        updateOrientationButtons();
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

    private void updateOrientationButtons() {
        btnPortrait.setSelected("port".equals(currentOrientation));
        btnLandscape.setSelected("land".equals(currentOrientation));
    }

    // ====================================================================

    public void cleanup() {
        if (dpad != null) dpad.cleanup();
        if (presetManager != null) presetManager.cleanup();
    }

    public void refresh() {
        syncAll();
    }

    public void syncAll() {
        sliderController.sync(TextConfig.posX, TextConfig.posY);
    }
}
