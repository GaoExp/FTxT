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
import exp.ftxt.features.battery.BatteryConfig;
import exp.ftxt.features.battery.BatteryModule;
import exp.ftxt.shared.ui.DpadController;
import exp.ftxt.shared.ui.PositionPresetManager;
import exp.ftxt.shared.ui.SliderPositionController;

public class BatteryPositionController {

    private final Activity activity;
    private final SharedPreferences prefs;

    private View btnUp, btnDown, btnLeft, btnRight;
    private View btnPortrait, btnLandscape;
    private String currentOrientation;

    private DpadController dpad;
    private PositionPresetManager presetManager;
    private SliderPositionController sliderController;
    private TextView coordDisplay;
    private TextView presetIndicator;
    private View btnExportImport;
    private int displayWidth, displayHeight;

    private static final String PREFS_NAME = "ftxt_prefs";

    public BatteryPositionController(Activity activity) {
        this.activity = activity;
        this.prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        int orientation = activity.getResources().getConfiguration().orientation;
        currentOrientation = (orientation == Configuration.ORIENTATION_LANDSCAPE) ? "land" : "port";
        loadPositionFromPrefs(currentOrientation);

        FloatingService.setBatteryOrientationSuffixStatic(currentOrientation);

        bindViews();

        WindowManager wm = activity.getWindowManager();
        DisplayMetrics realMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(realMetrics);
        displayWidth = realMetrics.widthPixels;
        displayHeight = realMetrics.heightPixels;

        BatteryModule.onPositionUpdate = this::syncAll;

        presetManager = new PositionPresetManager(activity, (x, y) -> onPositionChanged(x, y));
        presetManager.setOnPresetLoadedListener(new PositionPresetManager.OnPresetLoadedListener() {
            @Override
            public void onPresetLoaded(int idx) {
                String name = presetManager.getPresetName(idx);
                if (presetIndicator != null) presetIndicator.setText("Preset: " + name);
            }

            @Override
            public void onPresetChanged() {
                updatePresetIndicator();
            }
        });
        if (btnExportImport != null) {
            btnExportImport.setOnClickListener(v -> presetManager.showExportImportDialog());
        }
        sliderController = new SliderPositionController(
                activity.findViewById(R.id.battery_posXSeekBar),
                activity.findViewById(R.id.battery_posYSeekBar),
                activity.findViewById(R.id.battery_posXLabel),
                activity.findViewById(R.id.battery_posYLabel),
                (x, y) -> onPositionChanged(x, y)
        );
        setupListeners();
        syncAll();
    }

    private void bindViews() {
        btnUp = activity.findViewById(R.id.battery_btnUp);
        btnDown = activity.findViewById(R.id.battery_btnDown);
        btnLeft = activity.findViewById(R.id.battery_btnLeft);
        btnRight = activity.findViewById(R.id.battery_btnRight);
        btnPortrait = activity.findViewById(R.id.battery_btnPortrait);
        btnLandscape = activity.findViewById(R.id.battery_btnLandscape);
        coordDisplay = activity.findViewById(R.id.battery_posCoordDisplay);
        presetIndicator = activity.findViewById(R.id.battery_presetIndicator);
        btnExportImport = activity.findViewById(R.id.battery_btnExportImport);
    }

    private void setupListeners() {
        dpad = new DpadController(btnUp, btnDown, btnLeft, btnRight, (dx, dy) -> {
            onPositionChanged(clamp(BatteryConfig.posX + dx), clamp(BatteryConfig.posY + dy));
        });

        View btnSavePreset = activity.findViewById(R.id.battery_btnSavePreset);
        View btnLoadPreset = activity.findViewById(R.id.battery_btnLoadPreset);
        View btnResetPos = activity.findViewById(R.id.battery_btnResetPos);
        if (btnSavePreset != null) {
            btnSavePreset.setOnClickListener(v -> presetManager.showSavePresetDialog(BatteryConfig.posX, BatteryConfig.posY));
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
        BatteryConfig.posX = x;
        BatteryConfig.posY = y;
        syncAll();
        savePositionToPrefs(currentOrientation);
        FloatingService.updateBatteryPositionStatic();
    }

    private void resetPosition() {
        onPositionChanged(0.5f, 0.5f);
    }

    private void setOrientationMode(String mode) {
        if (mode.equals(currentOrientation)) return;

        savePositionToPrefs(currentOrientation);

        currentOrientation = mode;
        FloatingService.setBatteryOrientationSuffixStatic(mode);
        loadPositionFromPrefs(mode);

        syncAll();
        FloatingService.updateBatteryPositionStatic();
        updateOrientationButtons();
    }

    private void savePositionToPrefs(String orient) {
        String sfx = "_" + orient;
        prefs.edit()
                .putFloat("battery_pos_x" + sfx, BatteryConfig.posX)
                .putFloat("battery_pos_y" + sfx, BatteryConfig.posY)
                .apply();
    }

    private void loadPositionFromPrefs(String orient) {
        String sfx = "_" + orient;
        BatteryConfig.posX = prefs.getFloat("battery_pos_x" + sfx, 0.5f);
        BatteryConfig.posY = prefs.getFloat("battery_pos_y" + sfx, 0.5f);
    }

    private void updateOrientationButtons() {
        btnPortrait.setSelected("port".equals(currentOrientation));
        btnLandscape.setSelected("land".equals(currentOrientation));
    }

    public void cleanup() {
        BatteryModule.onPositionUpdate = null;
        if (dpad != null) dpad.cleanup();
        if (presetManager != null) presetManager.cleanup();
    }

    public void refresh() {
        syncAll();
    }

    public void syncAll() {
        sliderController.sync(BatteryConfig.posX, BatteryConfig.posY);
        updateCoordDisplay();
    }

    private void updateCoordDisplay() {
        if (coordDisplay == null) return;
        int px, py;
        int[] pos = FloatingService.getBatteryCurrentPosition();
        if (pos != null) {
            px = pos[0];
            py = pos[1];
        } else {
            px = Math.round(BatteryConfig.posX * displayWidth);
            py = Math.round(BatteryConfig.posY * displayHeight);
        }
        coordDisplay.setText(px + "X" + py);
    }

    private void updatePresetIndicator() {
        if (presetIndicator == null) return;
        String name = presetManager.getActivePresetName();
        if (name != null) {
            presetIndicator.setText("Preset: " + name);
        } else {
            presetIndicator.setText("");
        }
    }
}
