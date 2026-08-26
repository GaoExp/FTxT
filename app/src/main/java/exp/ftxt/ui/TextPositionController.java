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
import exp.ftxt.features.floating_text.TextConfig;
import exp.ftxt.features.floating_text.TextModule;
import exp.ftxt.shared.preset.OverlayPreset;
import exp.ftxt.shared.preset.PresetHandler;
import exp.ftxt.shared.ui.DpadController;
import exp.ftxt.shared.ui.SliderPositionController;

public class TextPositionController {

    private final Activity activity;
    private final SharedPreferences prefs;

    private View btnUp, btnDown, btnLeft, btnRight, btnMinus, btnPlus, dpadCenter;
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
        public String moduleLabel() { return "teks"; }
        @Override
        public String moduleType() { return "text"; }
        @Override
        public String touchPassthroughPrefKey() { return "text_lock"; }
        @Override
        public String safeAreaPrefKey() { return "text_safe_area"; }
        @Override
        public String posXPrefKey() { return "text_pos_x"; }
        @Override
        public String posYPrefKey() { return "text_pos_y"; }

        @Override
        public void saveToPreset(OverlayPreset p) {
            p.moduleType = moduleType();
            p.posX = TextConfig.posX;
            p.posY = TextConfig.posY;
            p.size = TextConfig.size;
            p.color = TextConfig.color;
            p.shadow = PresetHandler.copyShadow(TextConfig.shadow);
            p.bgEnabled = TextConfig.bg.enabled;
            p.bgColor = TextConfig.bg.color;
            p.bgPadding = TextConfig.bg.padding;
            p.bgOffsetX = TextConfig.bg.offsetX;
            p.bgOffsetY = TextConfig.bg.offsetY;
            p.bgMargin = TextConfig.bg.margin;
            p.bgRadius = TextConfig.bg.radius;
            p.touchPassthrough = TextConfig.touchPassthrough;
            p.safeArea = TextConfig.safeArea;
            p.textContent = TextConfig.text;
        }

        @Override
        public void applyFromPreset(Activity activity, OverlayPreset p, SharedPreferences prefs) {
            TextConfig.posX = p.posX;
            TextConfig.posY = p.posY;
            TextConfig.size = p.size;
            TextConfig.color = p.color;
            if (p.shadow != null) {
                TextConfig.shadow.enabled = p.shadow.enabled;
                TextConfig.shadow.color = p.shadow.color;
                TextConfig.shadow.blur = p.shadow.blur;
                TextConfig.shadow.offsetX = p.shadow.offsetX;
                TextConfig.shadow.offsetY = p.shadow.offsetY;
            }
            TextConfig.bg.enabled = p.bgEnabled;
            TextConfig.bg.color = p.bgColor;
            TextConfig.bg.padding = p.bgPadding;
            TextConfig.bg.offsetX = p.bgOffsetX;
            TextConfig.bg.offsetY = p.bgOffsetY;
            TextConfig.bg.margin = p.bgMargin;
            TextConfig.bg.radius = p.bgRadius;
            if (p.touchPassthrough != null) {
                TextConfig.touchPassthrough = p.touchPassthrough;
                prefs.edit().putBoolean("text_lock", TextConfig.touchPassthrough).apply();
            }
            if (p.safeArea != null) {
                TextConfig.safeArea = p.safeArea;
                prefs.edit().putBoolean("text_safe_area", TextConfig.safeArea).apply();
            }
            if (p.textContent != null && !p.textContent.isEmpty()) {
                TextConfig.text = p.textContent;
                prefs.edit().putString("text_content", p.textContent).apply();
                FloatingService.updateTextStatic();
            }
        }

        @Override
        public void syncToService() {
            FloatingService.updatePositionForModule(FloatingService.textModule());
            FloatingService.updateSizeForModule(FloatingService.textModule(), TextConfig.size);
            FloatingService.updateColorForModule(FloatingService.textModule(), TextConfig.color);
            FloatingService.updateShadowForModule(FloatingService.textModule());
            FloatingService.updateBackgroundForModule(FloatingService.textModule());
        }
    };

    public TextPositionController(Activity activity, View rootView) {
        this.activity = activity;
        this.prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        int orientation = activity.getResources().getConfiguration().orientation;
        currentOrientation = (orientation == Configuration.ORIENTATION_LANDSCAPE) ? "land" : "port";
        loadPositionFromPrefs(currentOrientation);

        FloatingService.setOrientationSuffixForModule(FloatingService.textModule(), currentOrientation);

        bindViews(rootView);

        WindowManager wm = activity.getWindowManager();
        DisplayMetrics realMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(realMetrics);
        displayWidth = realMetrics.widthPixels;
        displayHeight = realMetrics.heightPixels;

        TextModule.onPositionUpdate = this::syncAll;

        sliderController = new SliderPositionController(
                rootView.findViewById(R.id.posXSeekBar),
                rootView.findViewById(R.id.posYSeekBar),
                rootView.findViewById(R.id.posXLabel),
                rootView.findViewById(R.id.posYLabel),
                (x, y) -> onPositionChanged(x, y)
        );
        setupListeners();
        syncAll();
        FloatingService.updatePositionForModule(FloatingService.textModule());
    }

    private void bindViews(View rootView) {
        btnUp = rootView.findViewById(R.id.btnUp);
        btnDown = rootView.findViewById(R.id.btnDown);
        btnLeft = rootView.findViewById(R.id.btnLeft);
        btnRight = rootView.findViewById(R.id.btnRight);
        btnMinus = rootView.findViewById(R.id.btnMinus);
        btnPlus = rootView.findViewById(R.id.btnPlus);
        coordDisplay = rootView.findViewById(R.id.posCoordDisplay);
        activePresetLabel = rootView.findViewById(R.id.active_preset_label);
        dpadCenter = rootView.findViewById(R.id.dpadInterval);
    }

    private void setupListeners() {
        dpad = new DpadController(btnUp, btnDown, btnLeft, btnRight, btnMinus, btnPlus, dpadCenter, displayWidth, displayHeight, (dx, dy) -> {
            onPositionChanged(clamp(TextConfig.posX + dx), clamp(TextConfig.posY + dy));
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
        TextConfig.posX = x;
        TextConfig.posY = y;
        syncAll();
        PresetHandler.savePositionToPrefs(prefs, delegate, currentOrientation, x, y);
        FloatingService.updatePositionForModule(FloatingService.textModule());
    }

    private void loadPositionFromPrefs(String orient) {
        String sfx = "_" + orient;
        TextConfig.posX = prefs.getFloat("text_pos_x" + sfx, 0.5f);
        TextConfig.posY = prefs.getFloat("text_pos_y" + sfx, 0.8f);
    }

    public void cleanup() {
        TextModule.onPositionUpdate = null;
        FloatingService.setOrientationSuffixForModule(FloatingService.textModule(), null);
        if (dpad != null) dpad.cleanup();
    }

    public void refresh() {
        syncAll();
    }

    public void syncAll() {
        sliderController.sync(TextConfig.posX, TextConfig.posY);
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
        int[] pos = FloatingService.getCurrentPositionForModule(FloatingService.textModule());
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
