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
import exp.ftxt.features.memory_stats.MemoryConfig;
import exp.ftxt.features.memory_stats.MemoryModule;
import exp.ftxt.shared.preset.OverlayPreset;
import exp.ftxt.shared.preset.PresetHandler;
import exp.ftxt.shared.ui.DpadController;
import exp.ftxt.shared.ui.SliderPositionController;

public class MemoryPositionController {

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
        public String moduleLabel() { return "Info Memori"; }
        @Override
        public String moduleType() { return "memory"; }
        @Override
        public String touchPassthroughPrefKey() { return "mem_lock"; }
        @Override
        public String safeAreaPrefKey() { return "mem_safe_area"; }
        @Override
        public String posXPrefKey() { return "mem_pos_x"; }
        @Override
        public String posYPrefKey() { return "mem_pos_y"; }

        @Override
        public void saveToPreset(OverlayPreset p) {
            p.moduleType = moduleType();
            p.posX = MemoryConfig.posX;
            p.posY = MemoryConfig.posY;
            p.size = MemoryConfig.size;
            p.color = MemoryConfig.color;
            p.shadow = PresetHandler.copyShadow(MemoryConfig.shadow);
            p.bgEnabled = MemoryConfig.bg.enabled;
            p.bgColor = MemoryConfig.bg.color;
            p.bgPadding = MemoryConfig.bg.padding;
            p.bgOffsetX = MemoryConfig.bg.offsetX;
            p.bgOffsetY = MemoryConfig.bg.offsetY;
            p.bgMargin = MemoryConfig.bg.margin;
            p.bgRadius = MemoryConfig.bg.radius;
            p.touchPassthrough = MemoryConfig.touchPassthrough;
            p.safeArea = MemoryConfig.safeArea;
            p.showOnlyValue = MemoryConfig.showOnlyValue;
            p.labelColor = MemoryConfig.labelColor;
            p.separatorColor = MemoryConfig.separatorColor;
            p.showJavaHeap = MemoryConfig.showJavaHeap;
            p.showNativeHeap = MemoryConfig.showNativeHeap;
            p.showGraphics = MemoryConfig.showGraphics;
            p.showTotal = MemoryConfig.showTotal;
            p.itemOrder = MemoryConfig.itemOrder;
        }

        @Override
        public void applyFromPreset(Activity activity, OverlayPreset p, SharedPreferences prefs) {
            MemoryConfig.posX = p.posX;
            MemoryConfig.posY = p.posY;
            MemoryConfig.size = p.size;
            MemoryConfig.color = p.color;
            if (p.shadow != null) {
                MemoryConfig.shadow.enabled = p.shadow.enabled;
                MemoryConfig.shadow.color = p.shadow.color;
                MemoryConfig.shadow.blur = p.shadow.blur;
                MemoryConfig.shadow.offsetX = p.shadow.offsetX;
                MemoryConfig.shadow.offsetY = p.shadow.offsetY;
            }
            MemoryConfig.bg.enabled = p.bgEnabled;
            MemoryConfig.bg.color = p.bgColor;
            MemoryConfig.bg.padding = p.bgPadding;
            MemoryConfig.bg.offsetX = p.bgOffsetX;
            MemoryConfig.bg.offsetY = p.bgOffsetY;
            MemoryConfig.bg.margin = p.bgMargin;
            MemoryConfig.bg.radius = p.bgRadius;
            if (p.touchPassthrough != null) {
                MemoryConfig.touchPassthrough = p.touchPassthrough;
                prefs.edit().putBoolean("mem_lock", MemoryConfig.touchPassthrough).apply();
            }
            if (p.safeArea != null) {
                MemoryConfig.safeArea = p.safeArea;
                prefs.edit().putBoolean("mem_safe_area", MemoryConfig.safeArea).apply();
            }
            if (p.showOnlyValue != null) {
                MemoryConfig.showOnlyValue = p.showOnlyValue;
                prefs.edit().putBoolean("mem_show_only_value", MemoryConfig.showOnlyValue).apply();
            }
            if (p.labelColor != null) {
                MemoryConfig.labelColor = p.labelColor;
                prefs.edit().putInt("mem_label_color", MemoryConfig.labelColor).apply();
            }
            if (p.separatorColor != null) {
                MemoryConfig.separatorColor = p.separatorColor;
                prefs.edit().putInt("mem_separator_color", MemoryConfig.separatorColor).apply();
            }
            if (p.showJavaHeap != null) {
                MemoryConfig.showJavaHeap = p.showJavaHeap;
                prefs.edit().putBoolean("mem_show_java", MemoryConfig.showJavaHeap).apply();
            }
            if (p.showNativeHeap != null) {
                MemoryConfig.showNativeHeap = p.showNativeHeap;
                prefs.edit().putBoolean("mem_show_native", MemoryConfig.showNativeHeap).apply();
            }
            if (p.showGraphics != null) {
                MemoryConfig.showGraphics = p.showGraphics;
                prefs.edit().putBoolean("mem_show_graphics", MemoryConfig.showGraphics).apply();
            }
            if (p.showTotal != null) {
                MemoryConfig.showTotal = p.showTotal;
                prefs.edit().putBoolean("mem_show_total", MemoryConfig.showTotal).apply();
            }
            if (p.itemOrder != null && !p.itemOrder.isEmpty()) {
                MemoryConfig.itemOrder = p.itemOrder;
                prefs.edit().putString("mem_item_order", MemoryConfig.itemOrder).apply();
            }
        }

        @Override
        public void syncToService() {
            FloatingService.updateMemoryInPlace();
        }
    };

    public MemoryPositionController(Activity activity, View rootView) {
        this.activity = activity;
        this.prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        int orientation = activity.getResources().getConfiguration().orientation;
        currentOrientation = (orientation == Configuration.ORIENTATION_LANDSCAPE) ? "land" : "port";
        loadPositionFromPrefs(currentOrientation);

        FloatingService.setOrientationSuffixForModule(FloatingService.memoryModule(), currentOrientation);

        bindViews(rootView);

        WindowManager wm = activity.getWindowManager();
        DisplayMetrics realMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(realMetrics);
        displayWidth = realMetrics.widthPixels;
        displayHeight = realMetrics.heightPixels;

        MemoryModule.onPositionUpdate = this::syncAll;

        sliderController = new SliderPositionController(
                rootView.findViewById(R.id.mem_posXSeekBar),
                rootView.findViewById(R.id.mem_posYSeekBar),
                rootView.findViewById(R.id.mem_posXLabel),
                rootView.findViewById(R.id.mem_posYLabel),
                (x, y) -> onPositionChanged(x, y)
        );
        setupListeners();
        syncAll();
        FloatingService.updatePositionForModule(FloatingService.memoryModule());
    }

    private void bindViews(View rootView) {
        btnUp = rootView.findViewById(R.id.mem_btnUp);
        btnDown = rootView.findViewById(R.id.mem_btnDown);
        btnLeft = rootView.findViewById(R.id.mem_btnLeft);
        btnRight = rootView.findViewById(R.id.mem_btnRight);
        coordDisplay = rootView.findViewById(R.id.mem_posCoordDisplay);
        activePresetLabel = rootView.findViewById(R.id.active_preset_label);
    }

    private void setupListeners() {
        dpad = new DpadController(btnUp, btnDown, btnLeft, btnRight, (dx, dy) -> {
            onPositionChanged(clamp(MemoryConfig.posX + dx), clamp(MemoryConfig.posY + dy));
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
        MemoryConfig.posX = x;
        MemoryConfig.posY = y;
        syncAll();
        PresetHandler.savePositionToPrefs(prefs, delegate, currentOrientation, x, y);
        FloatingService.updatePositionForModule(FloatingService.memoryModule());
    }

    private void loadPositionFromPrefs(String orient) {
        String sfx = "_" + orient;
        MemoryConfig.posX = prefs.getFloat("mem_pos_x" + sfx, 0.05f);
        MemoryConfig.posY = prefs.getFloat("mem_pos_y" + sfx, 0.6f);
    }

    public void cleanup() {
        MemoryModule.onPositionUpdate = null;
        FloatingService.setOrientationSuffixForModule(FloatingService.memoryModule(), null);
        if (dpad != null) dpad.cleanup();
    }

    public void refresh() {
        syncAll();
    }

    public void syncAll() {
        sliderController.sync(MemoryConfig.posX, MemoryConfig.posY);
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
        int[] pos = FloatingService.getCurrentPositionForModule(FloatingService.memoryModule());
        if (pos != null) {
            px = pos[0];
            py = pos[1];
        } else {
            px = Math.round(MemoryConfig.posX * displayWidth);
            py = Math.round(MemoryConfig.posY * displayHeight);
        }
        coordDisplay.setText(px + "X" + py);
    }
}
