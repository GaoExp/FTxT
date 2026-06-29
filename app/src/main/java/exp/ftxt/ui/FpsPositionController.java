package exp.ftxt.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import exp.ftxt.R;
import exp.ftxt.core.FloatingService;
import exp.ftxt.features.fps_display.FpsConfig;
import exp.ftxt.features.fps_display.FpsModule;
import exp.ftxt.shared.preset.OverlayPreset;
import exp.ftxt.shared.preset.PresetHandler;
import exp.ftxt.shared.preset.PresetManager;
import exp.ftxt.shared.ui.DpadController;
import exp.ftxt.shared.ui.SliderPositionController;

public class FpsPositionController {

    private final Activity activity;
    private final SharedPreferences prefs;

    private View btnUp, btnDown, btnLeft, btnRight;
    private String currentOrientation;

    private DpadController dpad;
    private SliderPositionController sliderController;
    private TextView coordDisplay;
    private TextView activePresetLabel;
    private int displayWidth, displayHeight;

    private static final String PREFS_NAME = "ftxt_prefs";
    private final PresetHandler.StringHolder activePresetName = new PresetHandler.StringHolder();

    private final PresetHandler.Delegate delegate = new PresetHandler.Delegate() {
        @Override
        public String moduleLabel() { return "FPS"; }
        @Override
        public String touchPassthroughPrefKey() { return "fps_lock"; }
        @Override
        public String safeAreaPrefKey() { return "fps_safe_area"; }
        @Override
        public String posXPrefKey() { return "fps_pos_x"; }
        @Override
        public String posYPrefKey() { return "fps_pos_y"; }

        @Override
        public void saveToPreset(OverlayPreset p) {
            p.posX = FpsConfig.posX;
            p.posY = FpsConfig.posY;
            p.size = FpsConfig.size;
            p.color = FpsConfig.color;
            p.shadow = PresetHandler.copyShadow(FpsConfig.shadow);
            p.bgEnabled = FpsConfig.bg.enabled;
            p.bgColor = FpsConfig.bg.color;
            p.bgPadding = FpsConfig.bg.padding;
            p.bgOffsetX = FpsConfig.bg.offsetX;
            p.bgOffsetY = FpsConfig.bg.offsetY;
            p.bgMargin = FpsConfig.bg.margin;
            p.bgRadius = FpsConfig.bg.radius;
            p.touchPassthrough = FpsConfig.touchPassthrough;
            p.safeArea = FpsConfig.safeArea;
            p.showOnlyValue = FpsConfig.showOnlyValue;
            p.labelColor = FpsConfig.labelColor;
        }

        @Override
        public void applyFromPreset(Activity activity, OverlayPreset p, SharedPreferences prefs) {
            FpsConfig.posX = p.posX;
            FpsConfig.posY = p.posY;
            FpsConfig.size = p.size;
            FpsConfig.color = p.color;
            if (p.shadow != null) {
                FpsConfig.shadow.enabled = p.shadow.enabled;
                FpsConfig.shadow.color = p.shadow.color;
                FpsConfig.shadow.blur = p.shadow.blur;
                FpsConfig.shadow.offsetX = p.shadow.offsetX;
                FpsConfig.shadow.offsetY = p.shadow.offsetY;
            }
            FpsConfig.bg.enabled = p.bgEnabled;
            FpsConfig.bg.color = p.bgColor;
            FpsConfig.bg.padding = p.bgPadding;
            FpsConfig.bg.offsetX = p.bgOffsetX;
            FpsConfig.bg.offsetY = p.bgOffsetY;
            FpsConfig.bg.margin = p.bgMargin;
            FpsConfig.bg.radius = p.bgRadius;
            if (p.touchPassthrough != null) {
                FpsConfig.touchPassthrough = p.touchPassthrough;
                prefs.edit().putBoolean("fps_lock", FpsConfig.touchPassthrough).apply();
            }
            if (p.safeArea != null) {
                FpsConfig.safeArea = p.safeArea;
                prefs.edit().putBoolean("fps_safe_area", FpsConfig.safeArea).apply();
            }
            if (p.showOnlyValue != null) {
                FpsConfig.showOnlyValue = p.showOnlyValue;
                prefs.edit().putBoolean("fps_show_only_value", FpsConfig.showOnlyValue).apply();
            }
            if (p.labelColor != null) {
                FpsConfig.labelColor = p.labelColor;
                prefs.edit().putInt("fps_label_color", FpsConfig.labelColor).apply();
            }
        }

        @Override
        public void syncToService() {
            FloatingService.updateFpsPositionStatic();
            FloatingService.updateFpsSizeStatic();
            FloatingService.updateFpsColorStatic();
            FloatingService.updateFpsShadowStatic();
            FloatingService.updateFpsBackgroundStatic();
            FloatingService.updateFpsLabelColorStatic();
        }
    };

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

        FpsModule.onPositionUpdate = this::syncAll;

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
        coordDisplay = activity.findViewById(R.id.fps_posCoordDisplay);
    }

    private void setupListeners() {
        dpad = new DpadController(btnUp, btnDown, btnLeft, btnRight, (dx, dy) -> {
            onPositionChanged(clamp(FpsConfig.posX + dx), clamp(FpsConfig.posY + dy));
        });
    }

    public void showLoadPresetDialog() {
        PresetHandler.showLoadPresetDialog(activity, delegate, activePresetName, this::syncAll,
                (onSaved) -> PresetHandler.showSavePresetDialog(activity, delegate, onSaved));
    }

    private static float clamp(float val) {
        return Math.max(0, Math.min(1, val));
    }

    private void onPositionChanged(float x, float y) {
        FpsConfig.posX = x;
        FpsConfig.posY = y;
        syncAll();
        PresetHandler.savePositionToPrefs(prefs, delegate, currentOrientation, x, y);
        FloatingService.updateFpsPositionStatic();
    }

    private void loadPositionFromPrefs(String orient) {
        String sfx = "_" + orient;
        FpsConfig.posX = prefs.getFloat("fps_pos_x" + sfx, 0.05f);
        FpsConfig.posY = prefs.getFloat("fps_pos_y" + sfx, 0.05f);
    }

    public void cleanup() {
        FpsModule.onPositionUpdate = null;
        FloatingService.setFpsOrientationSuffixStatic(null);
        if (dpad != null) dpad.cleanup();
    }

    public void refresh() {
        syncAll();
    }

    public void syncAll() {
        sliderController.sync(FpsConfig.posX, FpsConfig.posY);
        updateCoordDisplay();
        updateActivePresetLabel();
    }

    private void updateActivePresetLabel() {
        if (activePresetLabel == null) return;
        String name = activePresetName.value;
        if (name != null && !name.isEmpty()) {
            activePresetLabel.setText("Aktif: " + name);
            activePresetLabel.setVisibility(View.VISIBLE);
        } else {
            activePresetLabel.setVisibility(View.GONE);
        }
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
