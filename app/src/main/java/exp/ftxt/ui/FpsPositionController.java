package exp.ftxt.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Switch;
import android.widget.TextView;

import exp.ftxt.R;
import exp.ftxt.core.FloatingService;
import exp.ftxt.features.fps.FpsConfig;
import exp.ftxt.features.fps.FpsModule;
import exp.ftxt.shared.ui.AppPresetWatcher;
import exp.ftxt.shared.ui.DpadController;
import exp.ftxt.shared.ui.PositionPresetManager;
import exp.ftxt.shared.ui.SliderPositionController;

public class FpsPositionController {

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
    private Switch autoPresetSwitch;
    private AppPresetWatcher autoWatcher;

    private static final String PREFS_NAME = "ftxt_prefs";

    public FpsPositionController(Activity activity) {
        this.activity = activity;
        this.prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        int orientation = activity.getResources().getConfiguration().orientation;
        currentOrientation = (orientation == Configuration.ORIENTATION_LANDSCAPE) ? "land" : "port";
        loadPositionFromPrefs(currentOrientation);

        FloatingService.setFpsOrientationSuffixStatic(currentOrientation);

        bindViews();

        WindowManager wm = activity.getWindowManager();
        DisplayMetrics realMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(realMetrics);
        displayWidth = realMetrics.widthPixels;
        displayHeight = realMetrics.heightPixels;

        FpsModule.onPositionUpdate = this::updateCoordDisplay;

        presetManager = new PositionPresetManager(activity, (x, y) -> onPositionChanged(x, y));
        sliderController = new SliderPositionController(
                activity.findViewById(R.id.fps_posXSeekBar),
                activity.findViewById(R.id.fps_posYSeekBar),
                activity.findViewById(R.id.fps_posXLabel),
                activity.findViewById(R.id.fps_posYLabel),
                (x, y) -> onPositionChanged(x, y)
        );
        setupListeners();
        syncAll();
    }

    private void bindViews() {
        btnUp = activity.findViewById(R.id.fps_btnUp);
        btnDown = activity.findViewById(R.id.fps_btnDown);
        btnLeft = activity.findViewById(R.id.fps_btnLeft);
        btnRight = activity.findViewById(R.id.fps_btnRight);
        btnPortrait = activity.findViewById(R.id.fps_btnPortrait);
        btnLandscape = activity.findViewById(R.id.fps_btnLandscape);
        coordDisplay = activity.findViewById(R.id.fps_posCoordDisplay);
        btnPosTL = activity.findViewById(R.id.fps_btnPosTL);
        btnPosTC = activity.findViewById(R.id.fps_btnPosTC);
        btnPosTR = activity.findViewById(R.id.fps_btnPosTR);
        btnPosML = activity.findViewById(R.id.fps_btnPosML);
        btnPosC = activity.findViewById(R.id.fps_btnPosC);
        btnPosMR = activity.findViewById(R.id.fps_btnPosMR);
        btnPosBL = activity.findViewById(R.id.fps_btnPosBL);
        btnPosBC = activity.findViewById(R.id.fps_btnPosBC);
        btnPosBR = activity.findViewById(R.id.fps_btnPosBR);
        autoPresetSwitch = activity.findViewById(R.id.fpsAutoPresetSwitch);
    }

    private void setupListeners() {
        dpad = new DpadController(btnUp, btnDown, btnLeft, btnRight, (dx, dy) -> {
            onPositionChanged(clamp(FpsConfig.posX + dx), clamp(FpsConfig.posY + dy));
        });

        View btnSavePreset = activity.findViewById(R.id.fps_btnSavePreset);
        View btnLoadPreset = activity.findViewById(R.id.fps_btnLoadPreset);
        View btnResetPos = activity.findViewById(R.id.fps_btnResetPos);
        if (btnSavePreset != null) {
            btnSavePreset.setOnClickListener(v -> presetManager.showSavePresetDialog(FpsConfig.posX, FpsConfig.posY));
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

        autoWatcher = new AppPresetWatcher(activity, "fps_", (oldPkg, newPkg, savedX, savedY) -> {
            autoWatcher.saveCurrentForApp(oldPkg, FpsConfig.posX, FpsConfig.posY);
            if (savedX >= 0 && savedY >= 0) {
                FpsConfig.posX = savedX;
                FpsConfig.posY = savedY;
                syncAll();
                savePositionToPrefs(currentOrientation);
                FloatingService.updateFpsPositionStatic();
            }
        });
        autoPresetSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                autoWatcher.setOrientationSuffix("_" + currentOrientation);
                autoWatcher.start();
            } else {
                autoWatcher.stop();
            }
        });
    }

    private void setupGridButton(View btn, float x, float y) {
        btn.setOnClickListener(v -> onPositionChanged(x, y));
    }

    private static float clamp(float val) {
        return Math.max(0, Math.min(1, val));
    }

    private void onPositionChanged(float x, float y) {
        FpsConfig.posX = x;
        FpsConfig.posY = y;
        syncAll();
        savePositionToPrefs(currentOrientation);
        FloatingService.updateFpsPositionStatic();
        if (autoWatcher != null && autoWatcher.isRunning()) {
            autoWatcher.saveCurrentForApp(autoWatcher.getCurrentPackage(), x, y);
        }
    }

    private void resetPosition() {
        onPositionChanged(0.5f, 0.5f);
    }

    private void setOrientationMode(String mode) {
        if (mode.equals(currentOrientation)) return;

        savePositionToPrefs(currentOrientation);

        currentOrientation = mode;
        FloatingService.setFpsOrientationSuffixStatic(mode);
        loadPositionFromPrefs(mode);

        syncAll();
        FloatingService.updateFpsPositionStatic();
        updateOrientationButtons();
        if (autoWatcher != null) autoWatcher.setOrientationSuffix("_" + currentOrientation);
    }

    private void savePositionToPrefs(String orient) {
        String sfx = "_" + orient;
        prefs.edit()
                .putFloat("fps_pos_x" + sfx, FpsConfig.posX)
                .putFloat("fps_pos_y" + sfx, FpsConfig.posY)
                .apply();
    }

    private void loadPositionFromPrefs(String orient) {
        String sfx = "_" + orient;
        FpsConfig.posX = prefs.getFloat("fps_pos_x" + sfx, 0.5f);
        FpsConfig.posY = prefs.getFloat("fps_pos_y" + sfx, 0.5f);
    }

    private void updateOrientationButtons() {
        btnPortrait.setSelected("port".equals(currentOrientation));
        btnLandscape.setSelected("land".equals(currentOrientation));
    }

    public void cleanup() {
        FpsModule.onPositionUpdate = null;
        if (dpad != null) dpad.cleanup();
        if (presetManager != null) presetManager.cleanup();
        if (autoWatcher != null) autoWatcher.cleanup();
    }

    public void refresh() {
        syncAll();
    }

    public void syncAll() {
        sliderController.sync(FpsConfig.posX, FpsConfig.posY);
        updateCoordDisplay();
    }

    private void updateCoordDisplay() {
        if (coordDisplay == null) return;
        int px, py;
        int[] pos = FloatingService.getFpsCurrentPosition();
        if (pos != null) {
            px = pos[0];
            py = pos[1];
        } else {
            px = Math.round(FpsConfig.posX * displayWidth);
            py = Math.round(FpsConfig.posY * displayHeight);
        }
        coordDisplay.setText(px + "X" + py);
    }
}
