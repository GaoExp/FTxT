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
import exp.ftxt.features.network_stats.NetworkConfig;
import exp.ftxt.features.network_stats.NetworkModule;
import exp.ftxt.shared.preset.OverlayPreset;
import exp.ftxt.shared.preset.PresetHandler;
import exp.ftxt.shared.preset.PresetManager;
import exp.ftxt.shared.ui.DpadController;
import exp.ftxt.shared.ui.SliderPositionController;

public class NetworkPositionController {

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
        public String moduleLabel() { return "Network"; }
        @Override
        public String touchPassthroughPrefKey() { return "network_lock"; }
        @Override
        public String safeAreaPrefKey() { return "network_safe_area"; }
        @Override
        public String posXPrefKey() { return "network_pos_x"; }
        @Override
        public String posYPrefKey() { return "network_pos_y"; }

        @Override
        public void saveToPreset(OverlayPreset p) {
            p.posX = NetworkConfig.posX;
            p.posY = NetworkConfig.posY;
            p.size = NetworkConfig.size;
            p.color = NetworkConfig.color;
            p.shadow = PresetHandler.copyShadow(NetworkConfig.shadow);
            p.bgEnabled = NetworkConfig.bg.enabled;
            p.bgColor = NetworkConfig.bg.color;
            p.bgPadding = NetworkConfig.bg.padding;
            p.bgOffsetX = NetworkConfig.bg.offsetX;
            p.bgOffsetY = NetworkConfig.bg.offsetY;
            p.bgMargin = NetworkConfig.bg.margin;
            p.bgRadius = NetworkConfig.bg.radius;
            p.labelColor = NetworkConfig.labelColor;
            p.touchPassthrough = NetworkConfig.touchPassthrough;
            p.safeArea = NetworkConfig.safeArea;
        }

        @Override
        public void applyFromPreset(Activity activity, OverlayPreset p, SharedPreferences prefs) {
            NetworkConfig.posX = p.posX;
            NetworkConfig.posY = p.posY;
            NetworkConfig.size = p.size;
            NetworkConfig.color = p.color;
            if (p.shadow != null) {
                NetworkConfig.shadow.enabled = p.shadow.enabled;
                NetworkConfig.shadow.color = p.shadow.color;
                NetworkConfig.shadow.blur = p.shadow.blur;
                NetworkConfig.shadow.offsetX = p.shadow.offsetX;
                NetworkConfig.shadow.offsetY = p.shadow.offsetY;
            }
            NetworkConfig.bg.enabled = p.bgEnabled;
            NetworkConfig.bg.color = p.bgColor;
            NetworkConfig.bg.padding = p.bgPadding;
            NetworkConfig.bg.offsetX = p.bgOffsetX;
            NetworkConfig.bg.offsetY = p.bgOffsetY;
            NetworkConfig.bg.margin = p.bgMargin;
            NetworkConfig.bg.radius = p.bgRadius;
            if (p.labelColor != null) {
                NetworkConfig.labelColor = p.labelColor;
                prefs.edit().putInt("network_label_color", NetworkConfig.labelColor).apply();
            }
            if (p.touchPassthrough != null) {
                NetworkConfig.touchPassthrough = p.touchPassthrough;
                prefs.edit().putBoolean("network_lock", NetworkConfig.touchPassthrough).apply();
            }
            if (p.safeArea != null) {
                NetworkConfig.safeArea = p.safeArea;
                prefs.edit().putBoolean("network_safe_area", NetworkConfig.safeArea).apply();
            }
        }

        @Override
        public void syncToService() {
            FloatingService.updateNetworkPositionStatic();
            FloatingService.updateNetworkSizeStatic();
            FloatingService.updateNetworkColorStatic();
            FloatingService.updateNetworkLabelColorStatic();
            FloatingService.updateNetworkShadowStatic();
            FloatingService.updateNetworkBackgroundStatic();
        }
    };

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
        coordDisplay = activity.findViewById(R.id.network_posCoordDisplay);
        activePresetLabel = activity.findViewById(R.id.network_txtActivePreset);
    }

    private void setupListeners() {
        dpad = new DpadController(btnUp, btnDown, btnLeft, btnRight, (dx, dy) -> {
            onPositionChanged(clamp(NetworkConfig.posX + dx), clamp(NetworkConfig.posY + dy));
        });

        View btnSavePreset = activity.findViewById(R.id.network_btnSavePreset);
        if (btnSavePreset != null) {
            btnSavePreset.setOnClickListener(v -> PresetHandler.showSavePresetDialog(activity, delegate));
        }

        View btnLoadPreset = activity.findViewById(R.id.network_btnLoadPreset);
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
        NetworkConfig.posX = x;
        NetworkConfig.posY = y;
        syncAll();
        PresetHandler.savePositionToPrefs(prefs, delegate, currentOrientation, x, y);
        FloatingService.updateNetworkPositionStatic();
    }

    private void loadPositionFromPrefs(String orient) {
        String sfx = "_" + orient;
        NetworkConfig.posX = prefs.getFloat("network_pos_x" + sfx, 0.75f);
        NetworkConfig.posY = prefs.getFloat("network_pos_y" + sfx, 0.05f);
    }

    public void cleanup() {
        NetworkModule.onPositionUpdate = null;
        if (dpad != null) dpad.cleanup();
    }

    public void refresh() {
        syncAll();
    }

    public void syncAll() {
        sliderController.sync(NetworkConfig.posX, NetworkConfig.posY);
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
