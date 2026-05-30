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
import exp.ftxt.features.network.NetworkConfig;
import exp.ftxt.features.network.NetworkModule;
import exp.ftxt.shared.ui.DpadController;
import exp.ftxt.shared.ui.PositionPresetManager;
import exp.ftxt.shared.ui.SliderPositionController;

public class NetworkPositionController {

    private final Activity activity;
    private final SharedPreferences prefs;

    private View btnUp, btnDown, btnLeft, btnRight;
    private View btnPortrait, btnLandscape;
    private String currentOrientation;

    private DpadController dpad;
    private PositionPresetManager presetManager;
    private SliderPositionController sliderController;
    private TextView coordDisplay;
    private View btnExportImport;
    private int displayWidth, displayHeight;

    private static final String PREFS_NAME = "ftxt_prefs";

    public NetworkPositionController(Activity activity) {
        this.activity = activity;
        this.prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        int orientation = activity.getResources().getConfiguration().orientation;
        currentOrientation = (orientation == Configuration.ORIENTATION_LANDSCAPE) ? "land" : "port";
        loadPositionFromPrefs(currentOrientation);

        FloatingService.setNetworkOrientationSuffixStatic(currentOrientation);

        bindViews();

        WindowManager wm = activity.getWindowManager();
        DisplayMetrics realMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(realMetrics);
        displayWidth = realMetrics.widthPixels;
        displayHeight = realMetrics.heightPixels;

        NetworkModule.onPositionUpdate = this::syncAll;

        presetManager = new PositionPresetManager(activity, (x, y) -> onPositionChanged(x, y));
        if (btnExportImport != null) {
            btnExportImport.setOnClickListener(v -> presetManager.showExportImportDialog());
        }
        sliderController = new SliderPositionController(
                activity.findViewById(R.id.network_posXSeekBar),
                activity.findViewById(R.id.network_posYSeekBar),
                activity.findViewById(R.id.network_posXLabel),
                activity.findViewById(R.id.network_posYLabel),
                (x, y) -> onPositionChanged(x, y)
        );
        setupListeners();
        syncAll();
    }

    private void bindViews() {
        btnUp = activity.findViewById(R.id.network_btnUp);
        btnDown = activity.findViewById(R.id.network_btnDown);
        btnLeft = activity.findViewById(R.id.network_btnLeft);
        btnRight = activity.findViewById(R.id.network_btnRight);
        btnPortrait = activity.findViewById(R.id.network_btnPortrait);
        btnLandscape = activity.findViewById(R.id.network_btnLandscape);
        coordDisplay = activity.findViewById(R.id.network_posCoordDisplay);
        btnExportImport = activity.findViewById(R.id.network_btnExportImport);
    }

    private void setupListeners() {
        dpad = new DpadController(btnUp, btnDown, btnLeft, btnRight, (dx, dy) -> {
            onPositionChanged(clamp(NetworkConfig.posX + dx), clamp(NetworkConfig.posY + dy));
        });

        View btnSavePreset = activity.findViewById(R.id.network_btnSavePreset);
        View btnLoadPreset = activity.findViewById(R.id.network_btnLoadPreset);
        if (btnSavePreset != null) {
            btnSavePreset.setOnClickListener(v -> presetManager.showSavePresetDialog(NetworkConfig.posX, NetworkConfig.posY));
        }
        if (btnLoadPreset != null) {
            btnLoadPreset.setOnClickListener(v -> presetManager.showLoadPresetDialog());
        }

        btnPortrait.setOnClickListener(v -> setOrientationMode("port"));
        btnLandscape.setOnClickListener(v -> setOrientationMode("land"));
        updateOrientationButtons();
    }

    private static float clamp(float val) {
        return Math.max(0, Math.min(1, val));
    }

    private void onPositionChanged(float x, float y) {
        NetworkConfig.posX = x;
        NetworkConfig.posY = y;
        syncAll();
        savePositionToPrefs(currentOrientation);
        FloatingService.updateNetworkPositionStatic();
    }

    private void setOrientationMode(String mode) {
        if (mode.equals(currentOrientation)) return;

        savePositionToPrefs(currentOrientation);

        currentOrientation = mode;
        FloatingService.setNetworkOrientationSuffixStatic(mode);
        loadPositionFromPrefs(mode);

        syncAll();
        FloatingService.updateNetworkPositionStatic();
        updateOrientationButtons();
    }

    private void savePositionToPrefs(String orient) {
        String sfx = "_" + orient;
        prefs.edit()
                .putFloat("network_pos_x" + sfx, NetworkConfig.posX)
                .putFloat("network_pos_y" + sfx, NetworkConfig.posY)
                .apply();
    }

    private void loadPositionFromPrefs(String orient) {
        String sfx = "_" + orient;
        NetworkConfig.posX = prefs.getFloat("network_pos_x" + sfx, 0.5f);
        NetworkConfig.posY = prefs.getFloat("network_pos_y" + sfx, 0.5f);
    }

    private void updateOrientationButtons() {
        btnPortrait.setSelected("port".equals(currentOrientation));
        btnLandscape.setSelected("land".equals(currentOrientation));
    }

    public void cleanup() {
        NetworkModule.onPositionUpdate = null;
        if (dpad != null) dpad.cleanup();
        if (presetManager != null) presetManager.cleanup();
    }

    public void refresh() {
        syncAll();
    }

    public void syncAll() {
        sliderController.sync(NetworkConfig.posX, NetworkConfig.posY);
        updateCoordDisplay();
    }

    private void updateCoordDisplay() {
        if (coordDisplay == null) return;
        int px, py;
        int[] pos = FloatingService.getNetworkCurrentPosition();
        if (pos != null) {
            px = pos[0];
            py = pos[1];
        } else {
            px = Math.round(NetworkConfig.posX * displayWidth);
            py = Math.round(NetworkConfig.posY * displayHeight);
        }
        coordDisplay.setText(px + "X" + py);
    }

}
