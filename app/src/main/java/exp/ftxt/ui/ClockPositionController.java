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
import exp.ftxt.features.clock.ClockConfig;
import exp.ftxt.features.clock.ClockModule;
import exp.ftxt.shared.ui.DpadController;
import exp.ftxt.shared.ui.PositionPresetManager;
import exp.ftxt.shared.ui.SliderPositionController;

public class ClockPositionController {

    private final Activity activity;
    private final SharedPreferences prefs;

    private View btnUp, btnDown, btnLeft, btnRight;
    private View btnPortrait, btnLandscape;
    private String currentOrientation;

    private DpadController dpad;
    private PositionPresetManager presetManager;
    private SliderPositionController sliderController;
    private TextView coordDisplay;
    private int displayWidth, displayHeight;

    private View btnPosTL, btnPosTC, btnPosTR, btnPosML, btnPosC, btnPosMR, btnPosBL, btnPosBC, btnPosBR;

    private static final String PREFS_NAME = "ftxt_prefs";

    public ClockPositionController(Activity activity) {
        this.activity = activity;
        this.prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        int orientation = activity.getResources().getConfiguration().orientation;
        currentOrientation = (orientation == Configuration.ORIENTATION_LANDSCAPE) ? "land" : "port";
        loadPositionFromPrefs(currentOrientation);

        FloatingService.setClockOrientationSuffixStatic(currentOrientation);

        bindViews();

        WindowManager wm = activity.getWindowManager();
        DisplayMetrics realMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(realMetrics);
        displayWidth = realMetrics.widthPixels;
        displayHeight = realMetrics.heightPixels;

        ClockModule.onPositionUpdate = this::updateCoordDisplay;

        presetManager = new PositionPresetManager(activity, (x, y) -> onPositionChanged(x, y));
        sliderController = new SliderPositionController(
                activity.findViewById(R.id.clock_posXSeekBar),
                activity.findViewById(R.id.clock_posYSeekBar),
                activity.findViewById(R.id.clock_posXLabel),
                activity.findViewById(R.id.clock_posYLabel),
                (x, y) -> onPositionChanged(x, y)
        );
        setupListeners();
        syncAll();
    }

    private void bindViews() {
        btnUp = activity.findViewById(R.id.clock_btnUp);
        btnDown = activity.findViewById(R.id.clock_btnDown);
        btnLeft = activity.findViewById(R.id.clock_btnLeft);
        btnRight = activity.findViewById(R.id.clock_btnRight);
        btnPortrait = activity.findViewById(R.id.clock_btnPortrait);
        btnLandscape = activity.findViewById(R.id.clock_btnLandscape);
        coordDisplay = activity.findViewById(R.id.clock_posCoordDisplay);
        btnPosTL = activity.findViewById(R.id.clock_btnPosTL);
        btnPosTC = activity.findViewById(R.id.clock_btnPosTC);
        btnPosTR = activity.findViewById(R.id.clock_btnPosTR);
        btnPosML = activity.findViewById(R.id.clock_btnPosML);
        btnPosC = activity.findViewById(R.id.clock_btnPosC);
        btnPosMR = activity.findViewById(R.id.clock_btnPosMR);
        btnPosBL = activity.findViewById(R.id.clock_btnPosBL);
        btnPosBC = activity.findViewById(R.id.clock_btnPosBC);
        btnPosBR = activity.findViewById(R.id.clock_btnPosBR);
    }

    private void setupListeners() {
        dpad = new DpadController(btnUp, btnDown, btnLeft, btnRight, (dx, dy) -> {
            onPositionChanged(clamp(ClockConfig.posX + dx), clamp(ClockConfig.posY + dy));
        });

        View btnSavePreset = activity.findViewById(R.id.clock_btnSavePreset);
        View btnLoadPreset = activity.findViewById(R.id.clock_btnLoadPreset);
        View btnResetPos = activity.findViewById(R.id.clock_btnResetPos);
        if (btnSavePreset != null) {
            btnSavePreset.setOnClickListener(v -> presetManager.showSavePresetDialog(ClockConfig.posX, ClockConfig.posY));
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

        setupGridButton(btnPosTL, 0f, 0f);
        setupGridButton(btnPosTC, 0.5f, 0f);
        setupGridButton(btnPosTR, 1f, 0f);
        setupGridButton(btnPosML, 0f, 0.5f);
        setupGridButton(btnPosC, 0.5f, 0.5f);
        setupGridButton(btnPosMR, 1f, 0.5f);
        setupGridButton(btnPosBL, 0f, 1f);
        setupGridButton(btnPosBC, 0.5f, 1f);
        setupGridButton(btnPosBR, 1f, 1f);
    }

    private void setupGridButton(View btn, float x, float y) {
        btn.setOnClickListener(v -> onPositionChanged(x, y));
    }

    private static float clamp(float val) {
        return Math.max(0, Math.min(1, val));
    }

    private void onPositionChanged(float x, float y) {
        ClockConfig.posX = x;
        ClockConfig.posY = y;
        syncAll();
        savePositionToPrefs(currentOrientation);
        FloatingService.updateClockPositionStatic();
    }

    private void resetPosition() {
        onPositionChanged(0.5f, 0.5f);
    }

    private void setOrientationMode(String mode) {
        if (mode.equals(currentOrientation)) return;

        savePositionToPrefs(currentOrientation);

        currentOrientation = mode;
        FloatingService.setClockOrientationSuffixStatic(mode);
        loadPositionFromPrefs(mode);

        syncAll();
        FloatingService.updateClockPositionStatic();
        updateOrientationButtons();
    }

    private void savePositionToPrefs(String orient) {
        String sfx = "_" + orient;
        prefs.edit()
                .putFloat("clock_pos_x" + sfx, ClockConfig.posX)
                .putFloat("clock_pos_y" + sfx, ClockConfig.posY)
                .apply();
    }

    private void loadPositionFromPrefs(String orient) {
        String sfx = "_" + orient;
        ClockConfig.posX = prefs.getFloat("clock_pos_x" + sfx, 0.5f);
        ClockConfig.posY = prefs.getFloat("clock_pos_y" + sfx, 0.5f);
    }

    private void updateOrientationButtons() {
        btnPortrait.setSelected("port".equals(currentOrientation));
        btnLandscape.setSelected("land".equals(currentOrientation));
    }

    public void cleanup() {
        ClockModule.onPositionUpdate = null;
        if (dpad != null) dpad.cleanup();
        if (presetManager != null) presetManager.cleanup();
    }

    public void refresh() {
        syncAll();
    }

    public void syncAll() {
        sliderController.sync(ClockConfig.posX, ClockConfig.posY);
        updateCoordDisplay();
    }

    private void updateCoordDisplay() {
        if (coordDisplay == null) return;
        int px, py;
        int[] pos = FloatingService.getClockCurrentPosition();
        if (pos != null) {
            px = pos[0];
            py = pos[1];
        } else {
            px = Math.round(ClockConfig.posX * displayWidth);
            py = Math.round(ClockConfig.posY * displayHeight);
        }
        coordDisplay.setText(px + "X" + py);
    }
}
