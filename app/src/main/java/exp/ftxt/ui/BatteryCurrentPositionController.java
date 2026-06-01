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
import exp.ftxt.features.battery_current.BatteryCurrentConfig;
import exp.ftxt.features.battery_current.BatteryCurrentModule;
import exp.ftxt.shared.preset.OverlayPreset;
import exp.ftxt.shared.preset.PresetHandler;
import exp.ftxt.shared.preset.PresetManager;
import exp.ftxt.shared.ui.DpadController;
import exp.ftxt.shared.ui.SliderPositionController;

public class BatteryCurrentPositionController {

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
        public String moduleLabel() { return "Battery Current"; }
        @Override
        public String touchPassthroughPrefKey() { return "batcur_lock"; }
        @Override
        public String safeAreaPrefKey() { return "batcur_safe_area"; }
        @Override
        public String posXPrefKey() { return "batcur_pos_x"; }
        @Override
        public String posYPrefKey() { return "batcur_pos_y"; }

        @Override
        public void saveToPreset(OverlayPreset p) {
            p.posX = BatteryCurrentConfig.posX;
            p.posY = BatteryCurrentConfig.posY;
            p.size = BatteryCurrentConfig.size;
            p.color = BatteryCurrentConfig.color;
            p.shadow = PresetHandler.copyShadow(BatteryCurrentConfig.shadow);
            p.bgEnabled = BatteryCurrentConfig.bgEnabled;
            p.bgColor = BatteryCurrentConfig.bgColor;
            p.bgPadding = BatteryCurrentConfig.bgPadding;
            p.bgOffsetX = BatteryCurrentConfig.bgOffsetX;
            p.bgOffsetY = BatteryCurrentConfig.bgOffsetY;
            p.bgMargin = BatteryCurrentConfig.bgMargin;
            p.bgRadius = BatteryCurrentConfig.bgRadius;
            p.touchPassthrough = BatteryCurrentConfig.touchPassthrough;
            p.safeArea = BatteryCurrentConfig.safeArea;
            p.showVoltage = BatteryCurrentConfig.showVoltage;
            p.showCurrent = BatteryCurrentConfig.showCurrent;
            p.showPower = BatteryCurrentConfig.showPower;
        }

        @Override
        public void applyFromPreset(Activity activity, OverlayPreset p, SharedPreferences prefs) {
            BatteryCurrentConfig.posX = p.posX;
            BatteryCurrentConfig.posY = p.posY;
            BatteryCurrentConfig.size = p.size;
            BatteryCurrentConfig.color = p.color;
            if (p.shadow != null) {
                BatteryCurrentConfig.shadow.enabled = p.shadow.enabled;
                BatteryCurrentConfig.shadow.color = p.shadow.color;
                BatteryCurrentConfig.shadow.blur = p.shadow.blur;
                BatteryCurrentConfig.shadow.offsetX = p.shadow.offsetX;
                BatteryCurrentConfig.shadow.offsetY = p.shadow.offsetY;
            }
            BatteryCurrentConfig.bgEnabled = p.bgEnabled;
            BatteryCurrentConfig.bgColor = p.bgColor;
            BatteryCurrentConfig.bgPadding = p.bgPadding;
            BatteryCurrentConfig.bgOffsetX = p.bgOffsetX;
            BatteryCurrentConfig.bgOffsetY = p.bgOffsetY;
            BatteryCurrentConfig.bgMargin = p.bgMargin;
            BatteryCurrentConfig.bgRadius = p.bgRadius;
            if (p.touchPassthrough != null) {
                BatteryCurrentConfig.touchPassthrough = p.touchPassthrough;
                prefs.edit().putBoolean("batcur_lock", BatteryCurrentConfig.touchPassthrough).apply();
            }
            if (p.safeArea != null) {
                BatteryCurrentConfig.safeArea = p.safeArea;
                prefs.edit().putBoolean("batcur_safe_area", BatteryCurrentConfig.safeArea).apply();
            }
            if (p.showVoltage != null) {
                BatteryCurrentConfig.showVoltage = p.showVoltage;
                prefs.edit().putBoolean("batcur_show_voltage", BatteryCurrentConfig.showVoltage).apply();
            }
            if (p.showCurrent != null) {
                BatteryCurrentConfig.showCurrent = p.showCurrent;
                prefs.edit().putBoolean("batcur_show_current", BatteryCurrentConfig.showCurrent).apply();
            }
            if (p.showPower != null) {
                BatteryCurrentConfig.showPower = p.showPower;
                prefs.edit().putBoolean("batcur_show_power", BatteryCurrentConfig.showPower).apply();
            }
        }

        @Override
        public void syncToService() {
            FloatingService.updateBatteryCurrentPositionStatic();
            FloatingService.updateBatteryCurrentSizeStatic();
            FloatingService.updateBatteryCurrentColorStatic();
            FloatingService.updateBatteryCurrentShadowStatic();
            FloatingService.updateBatteryCurrentBackgroundStatic();
        }
    };

    public BatteryCurrentPositionController(Activity activity) {
        this.activity = activity;
        this.prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        int orientation = activity.getResources().getConfiguration().orientation;
        currentOrientation = (orientation == Configuration.ORIENTATION_LANDSCAPE) ? "land" : "port";
        loadPositionFromPrefs(currentOrientation);

        FloatingService.setBatteryCurrentOrientationSuffixStatic(currentOrientation);

        bindViews();

        WindowManager wm = activity.getWindowManager();
        DisplayMetrics realMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(realMetrics);
        displayWidth = realMetrics.widthPixels;
        displayHeight = realMetrics.heightPixels;

        BatteryCurrentModule.onPositionUpdate = this::syncAll;

        sliderController = new SliderPositionController(
                activity.findViewById(R.id.batCurPosXSeekBar),
                activity.findViewById(R.id.batCurPosYSeekBar),
                activity.findViewById(R.id.batCurPosXLabel),
                activity.findViewById(R.id.batCurPosYLabel),
                (x, y) -> onPositionChanged(x, y)
        );
        setupListeners();
        syncAll();
    }

    private void bindViews() {
        btnUp = activity.findViewById(R.id.batCurBtnUp);
        btnDown = activity.findViewById(R.id.batCurBtnDown);
        btnLeft = activity.findViewById(R.id.batCurBtnLeft);
        btnRight = activity.findViewById(R.id.batCurBtnRight);
        coordDisplay = activity.findViewById(R.id.batCurPosCoordDisplay);
        activePresetLabel = activity.findViewById(R.id.batCurTxtActivePreset);
    }

    private void setupListeners() {
        dpad = new DpadController(btnUp, btnDown, btnLeft, btnRight, (dx, dy) -> {
            onPositionChanged(clamp(BatteryCurrentConfig.posX + dx), clamp(BatteryCurrentConfig.posY + dy));
        });

        View btnSavePreset = activity.findViewById(R.id.batCurBtnSavePreset);
        if (btnSavePreset != null) {
            btnSavePreset.setOnClickListener(v -> PresetHandler.showSavePresetDialog(activity, delegate));
        }

        View btnLoadPreset = activity.findViewById(R.id.batCurBtnLoadPreset);
        if (btnLoadPreset != null) {
            btnLoadPreset.setOnClickListener(v -> showLoadPresetDialog());
        }
    }

    public void showLoadPresetDialog() {
        PresetHandler.showLoadPresetDialog(activity, delegate, activePresetName, this::syncAll);
    }

    private static float clamp(float val) {
        return Math.max(0, Math.min(1, val));
    }

    private void onPositionChanged(float x, float y) {
        BatteryCurrentConfig.posX = x;
        BatteryCurrentConfig.posY = y;
        syncAll();
        PresetHandler.savePositionToPrefs(prefs, delegate, currentOrientation, x, y);
        FloatingService.updateBatteryCurrentPositionStatic();
    }

    private void loadPositionFromPrefs(String orient) {
        String sfx = "_" + orient;
        BatteryCurrentConfig.posX = prefs.getFloat("batcur_pos_x" + sfx, 0.75f);
        BatteryCurrentConfig.posY = prefs.getFloat("batcur_pos_y" + sfx, 0.85f);
    }

    public void cleanup() {
        BatteryCurrentModule.onPositionUpdate = null;
        if (dpad != null) dpad.cleanup();
    }

    public void refresh() {
        syncAll();
    }

    public void syncAll() {
        sliderController.sync(BatteryCurrentConfig.posX, BatteryCurrentConfig.posY);
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
        int[] pos = FloatingService.getBatteryCurrentCurrentPosition();
        if (pos != null) {
            px = pos[0];
            py = pos[1];
        } else {
            px = Math.round(BatteryCurrentConfig.posX * displayWidth);
            py = Math.round(BatteryCurrentConfig.posY * displayHeight);
        }
        coordDisplay.setText(px + "X" + py);
    }
}
