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
import exp.ftxt.features.crosshair.CrosshairConfig;
import exp.ftxt.features.crosshair.CrosshairModule;
import exp.ftxt.shared.ui.DpadController;
import exp.ftxt.shared.ui.SliderPositionController;

public class CrosshairPositionController {

    private final Activity activity;
    private final SharedPreferences prefs;

    private View btnUp, btnDown, btnLeft, btnRight;
    private String currentOrientation;

    private DpadController dpad;
    private SliderPositionController sliderController;
    private TextView coordDisplay;
    private int displayWidth, displayHeight;

    private static final String PREFS_NAME = "ftxt_prefs";

    public CrosshairPositionController(Activity activity, View rootView) {
        this.activity = activity;
        this.prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        int orientation = activity.getResources().getConfiguration().orientation;
        currentOrientation = (orientation == Configuration.ORIENTATION_LANDSCAPE) ? "land" : "port";
        loadPositionFromPrefs(currentOrientation);

        FloatingService.setOrientationSuffixForModule(FloatingService.crosshairModule(), currentOrientation);

        bindViews(rootView);

        WindowManager wm = activity.getWindowManager();
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        displayWidth = dm.widthPixels;
        displayHeight = dm.heightPixels;

        CrosshairModule.onPositionUpdate = this::syncAll;

        sliderController = new SliderPositionController(
                rootView.findViewById(R.id.crosshair_posXSeekBar),
                rootView.findViewById(R.id.crosshair_posYSeekBar),
                rootView.findViewById(R.id.crosshair_posXLabel),
                rootView.findViewById(R.id.crosshair_posYLabel),
                (x, y) -> onPositionChanged(x, y)
        );
        setupListeners();
        syncAll();
        FloatingService.updatePositionForModule(FloatingService.crosshairModule());
    }

    private void bindViews(View rootView) {
        btnUp = rootView.findViewById(R.id.crosshair_btnUp);
        btnDown = rootView.findViewById(R.id.crosshair_btnDown);
        btnLeft = rootView.findViewById(R.id.crosshair_btnLeft);
        btnRight = rootView.findViewById(R.id.crosshair_btnRight);
        coordDisplay = rootView.findViewById(R.id.crosshair_posCoordDisplay);
    }

    private void setupListeners() {
        dpad = new DpadController(btnUp, btnDown, btnLeft, btnRight, (dx, dy) -> {
            onPositionChanged(clamp(CrosshairConfig.posX + dx), clamp(CrosshairConfig.posY + dy));
        });
    }

    private static float clamp(float val) {
        return Math.max(0, Math.min(1, val));
    }

    private void onPositionChanged(float x, float y) {
        CrosshairConfig.posX = x;
        CrosshairConfig.posY = y;
        syncAll();
        prefs.edit()
                .putFloat("crosshair_pos_x_" + currentOrientation, x)
                .putFloat("crosshair_pos_y_" + currentOrientation, y)
                .apply();
        FloatingService.updatePositionForModule(FloatingService.crosshairModule());
    }

    private void loadPositionFromPrefs(String orient) {
        String sfx = "_" + orient;
        CrosshairConfig.posX = prefs.getFloat("crosshair_pos_x" + sfx, 0.5f);
        CrosshairConfig.posY = prefs.getFloat("crosshair_pos_y" + sfx, 0.5f);
    }

    public void cleanup() {
        CrosshairModule.onPositionUpdate = null;
        FloatingService.setOrientationSuffixForModule(FloatingService.crosshairModule(), null);
        if (dpad != null) dpad.cleanup();
    }

    public void refresh() {
        syncAll();
    }

    public void syncAll() {
        sliderController.sync(CrosshairConfig.posX, CrosshairConfig.posY);
        updateCoordDisplay();
    }

    private void updateCoordDisplay() {
        if (coordDisplay == null) return;
        int px, py;
        int[] pos = FloatingService.getCurrentPositionForModule(FloatingService.crosshairModule());
        if (pos != null) {
            px = pos[0];
            py = pos[1];
        } else {
            px = Math.round(CrosshairConfig.posX * displayWidth);
            py = Math.round(CrosshairConfig.posY * displayHeight);
        }
        coordDisplay.setText(px + "X" + py);
    }
}
