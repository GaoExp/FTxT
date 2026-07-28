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
import exp.ftxt.features.clock_module.ClockConfig;
import exp.ftxt.features.clock_module.ClockModule;
import exp.ftxt.shared.preset.OverlayPreset;
import exp.ftxt.shared.preset.PresetHandler;
import exp.ftxt.shared.preset.PresetManager;
import exp.ftxt.shared.ui.DpadController;
import exp.ftxt.shared.ui.SliderPositionController;

public class ClockPositionController {

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
        public String moduleLabel() { return "Jam"; }
        @Override
        public String touchPassthroughPrefKey() { return "clock_lock"; }
        @Override
        public String safeAreaPrefKey() { return "clock_safe_area"; }
        @Override
        public String posXPrefKey() { return "clock_pos_x"; }
        @Override
        public String posYPrefKey() { return "clock_pos_y"; }

        @Override
        public void saveToPreset(OverlayPreset p) {
            p.posX = ClockConfig.posX;
            p.posY = ClockConfig.posY;
            p.size = ClockConfig.size;
            p.color = ClockConfig.color;
            p.shadow = PresetHandler.copyShadow(ClockConfig.shadow);
            p.bgEnabled = ClockConfig.bg.enabled;
            p.bgColor = ClockConfig.bg.color;
            p.bgPadding = ClockConfig.bg.padding;
            p.bgOffsetX = ClockConfig.bg.offsetX;
            p.bgOffsetY = ClockConfig.bg.offsetY;
            p.bgMargin = ClockConfig.bg.margin;
            p.bgRadius = ClockConfig.bg.radius;
            p.touchPassthrough = ClockConfig.touchPassthrough;
            p.safeArea = ClockConfig.safeArea;
        }

        @Override
        public void applyFromPreset(Activity activity, OverlayPreset p, SharedPreferences prefs) {
            ClockConfig.posX = p.posX;
            ClockConfig.posY = p.posY;
            ClockConfig.size = p.size;
            ClockConfig.color = p.color;
            if (p.shadow != null) {
                ClockConfig.shadow.enabled = p.shadow.enabled;
                ClockConfig.shadow.color = p.shadow.color;
                ClockConfig.shadow.blur = p.shadow.blur;
                ClockConfig.shadow.offsetX = p.shadow.offsetX;
                ClockConfig.shadow.offsetY = p.shadow.offsetY;
            }
            ClockConfig.bg.enabled = p.bgEnabled;
            ClockConfig.bg.color = p.bgColor;
            ClockConfig.bg.padding = p.bgPadding;
            ClockConfig.bg.offsetX = p.bgOffsetX;
            ClockConfig.bg.offsetY = p.bgOffsetY;
            ClockConfig.bg.margin = p.bgMargin;
            ClockConfig.bg.radius = p.bgRadius;
            if (p.touchPassthrough != null) {
                ClockConfig.touchPassthrough = p.touchPassthrough;
                prefs.edit().putBoolean("clock_lock", ClockConfig.touchPassthrough).apply();
            }
            if (p.safeArea != null) {
                ClockConfig.safeArea = p.safeArea;
                prefs.edit().putBoolean("clock_safe_area", ClockConfig.safeArea).apply();
            }
        }

        @Override
        public void syncToService() {
            FloatingService.updatePositionForModule(FloatingService.clockModule());
            FloatingService.updateSizeForModule(FloatingService.clockModule(), ClockConfig.size);
            FloatingService.updateColorForModule(FloatingService.clockModule(), ClockConfig.color);
            FloatingService.updateShadowForModule(FloatingService.clockModule());
            FloatingService.updateBackgroundForModule(FloatingService.clockModule());
        }
    };

    public ClockPositionController(Activity activity) {
        this.activity = activity;
        this.prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        int orientation = activity.getResources().getConfiguration().orientation;
        currentOrientation = (orientation == Configuration.ORIENTATION_LANDSCAPE) ? "land" : "port";
        loadPositionFromPrefs(currentOrientation);

        FloatingService.setOrientationSuffixForModule(FloatingService.clockModule(), currentOrientation);

        bindViews();

        WindowManager wm = activity.getWindowManager();
        DisplayMetrics realMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(realMetrics);
        displayWidth = realMetrics.widthPixels;
        displayHeight = realMetrics.heightPixels;

        ClockModule.onPositionUpdate = this::syncAll;

        sliderController = new SliderPositionController(
                activity.findViewById(R.id.clock_posXSeekBar),
                activity.findViewById(R.id.clock_posYSeekBar),
                activity.findViewById(R.id.clock_posXLabel),
                activity.findViewById(R.id.clock_posYLabel),
                (x, y) -> onPositionChanged(x, y)
        );
        setupListeners();
        syncAll();
        FloatingService.updatePositionForModule(FloatingService.clockModule());
    }

    private void bindViews() {
        btnUp = activity.findViewById(R.id.clock_btnUp);
        btnDown = activity.findViewById(R.id.clock_btnDown);
        btnLeft = activity.findViewById(R.id.clock_btnLeft);
        btnRight = activity.findViewById(R.id.clock_btnRight);
        coordDisplay = activity.findViewById(R.id.clock_posCoordDisplay);
    }

    private void setupListeners() {
        dpad = new DpadController(btnUp, btnDown, btnLeft, btnRight, (dx, dy) -> {
            onPositionChanged(clamp(ClockConfig.posX + dx), clamp(ClockConfig.posY + dy));
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
        ClockConfig.posX = x;
        ClockConfig.posY = y;
        syncAll();
        PresetHandler.savePositionToPrefs(prefs, delegate, currentOrientation, x, y);
        FloatingService.updatePositionForModule(FloatingService.clockModule());
    }

    private void loadPositionFromPrefs(String orient) {
        String sfx = "_" + orient;
        ClockConfig.posX = prefs.getFloat("clock_pos_x" + sfx, 0.5f);
        ClockConfig.posY = prefs.getFloat("clock_pos_y" + sfx, 0.05f);
    }

    public void cleanup() {
        ClockModule.onPositionUpdate = null;
        FloatingService.setOrientationSuffixForModule(FloatingService.clockModule(), null);
        if (dpad != null) dpad.cleanup();
    }

    public void refresh() {
        syncAll();
    }

    public void syncAll() {
        sliderController.sync(ClockConfig.posX, ClockConfig.posY);
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
        int[] pos = FloatingService.getCurrentPositionForModule(FloatingService.clockModule());
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
