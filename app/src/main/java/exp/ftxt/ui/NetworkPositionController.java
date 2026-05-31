package exp.ftxt.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import exp.ftxt.R;
import exp.ftxt.core.FloatingService;
import exp.ftxt.features.network_stats.NetworkConfig;
import exp.ftxt.features.network_stats.NetworkModule;
import exp.ftxt.shared.preset.OverlayPreset;
import exp.ftxt.shared.preset.PresetManager;
import exp.ftxt.shared.ui.DpadController;
import exp.ftxt.shared.ui.ShadowConfig;
import exp.ftxt.shared.ui.SliderPositionController;

public class NetworkPositionController {

    private final Activity activity;
    private final SharedPreferences prefs;

    private View btnUp, btnDown, btnLeft, btnRight;
    private String currentOrientation;

    private DpadController dpad;
    private SliderPositionController sliderController;
    private TextView coordDisplay;
    private View btnExportImport;
    private int displayWidth, displayHeight;

    private static final String PREFS_NAME = "ftxt_prefs";
    private String activePresetName;

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

        if (btnExportImport != null) {
            btnExportImport.setOnClickListener(v -> showExportImportMenu());
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
        coordDisplay = activity.findViewById(R.id.network_posCoordDisplay);
        btnExportImport = activity.findViewById(R.id.network_btnExportImport);
    }

    private void setupListeners() {
        dpad = new DpadController(btnUp, btnDown, btnLeft, btnRight, (dx, dy) -> {
            onPositionChanged(clamp(NetworkConfig.posX + dx), clamp(NetworkConfig.posY + dy));
        });

        View btnSavePreset = activity.findViewById(R.id.network_btnSavePreset);
        if (btnSavePreset != null) {
            btnSavePreset.setOnClickListener(v -> showSavePresetDialog());
        }

    }

    private void showSavePresetDialog() {
        EditText input = new EditText(activity);
        input.setHint("Nama preset");

        new AlertDialog.Builder(activity)
                .setTitle("Simpan Preset")
                .setMessage("Simpan konfigurasi Network saat ini sebagai preset?")
                .setView(input)
                .setPositiveButton("Simpan", (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(activity, "Nama preset tidak boleh kosong", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    OverlayPreset existing = PresetManager.load(activity, name);
                    if (existing != null) {
                        new AlertDialog.Builder(activity)
                                .setTitle("Timpa Preset")
                                .setMessage("Preset \"" + name + "\" sudah ada. Timpa?")
                                .setPositiveButton("Ya", (d2, w2) -> doSavePreset(name))
                                .setNegativeButton("Batal", null)
                                .show();
                    } else {
                        doSavePreset(name);
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void doSavePreset(String name) {
        OverlayPreset preset = new OverlayPreset();
        preset.posX = NetworkConfig.posX;
        preset.posY = NetworkConfig.posY;
        preset.size = NetworkConfig.size;
        preset.color = NetworkConfig.color;
        ShadowConfig sc = NetworkConfig.shadow;
        preset.shadow = new ShadowConfig(sc.enabled, sc.color, sc.blur, sc.offsetX, sc.offsetY);
        preset.bgEnabled = NetworkConfig.bgEnabled;
        preset.bgColor = NetworkConfig.bgColor;
        preset.bgPadding = NetworkConfig.bgPadding;
        preset.bgOffsetX = NetworkConfig.bgOffsetX;
        preset.bgOffsetY = NetworkConfig.bgOffsetY;
        preset.bgMargin = NetworkConfig.bgMargin;
        preset.bgRadius = NetworkConfig.bgRadius;
        int orientation = activity.getResources().getConfiguration().orientation;
        preset.orientation = (orientation == Configuration.ORIENTATION_LANDSCAPE) ? "landscape" : "portrait";

        PresetManager.save(activity, name, preset);
        Toast.makeText(activity, "Preset \"" + name + "\" tersimpan", Toast.LENGTH_SHORT).show();
    }

    public void showLoadPresetDialog() {
        PresetManager.showLoadPresetDialog(activity, activePresetName, name -> {
            OverlayPreset preset = PresetManager.load(activity, name);
            if (preset == null) {
                Toast.makeText(activity, "Gagal memuat preset", Toast.LENGTH_SHORT).show();
                return;
            }
            activePresetName = name;
            applyPreset(preset);
            Toast.makeText(activity, "Preset \"" + name + "\" diterapkan", Toast.LENGTH_SHORT).show();
        });
    }

    private void applyPreset(OverlayPreset preset) {
        NetworkConfig.posX = preset.posX;
        NetworkConfig.posY = preset.posY;
        NetworkConfig.size = preset.size;
        NetworkConfig.color = preset.color;
        if (preset.shadow != null) {
            NetworkConfig.shadow.enabled = preset.shadow.enabled;
            NetworkConfig.shadow.color = preset.shadow.color;
            NetworkConfig.shadow.blur = preset.shadow.blur;
            NetworkConfig.shadow.offsetX = preset.shadow.offsetX;
            NetworkConfig.shadow.offsetY = preset.shadow.offsetY;
        }
        NetworkConfig.bgEnabled = preset.bgEnabled;
        NetworkConfig.bgColor = preset.bgColor;
        NetworkConfig.bgPadding = preset.bgPadding;
        NetworkConfig.bgOffsetX = preset.bgOffsetX;
        NetworkConfig.bgOffsetY = preset.bgOffsetY;
        NetworkConfig.bgMargin = preset.bgMargin;
        NetworkConfig.bgRadius = preset.bgRadius;

        savePositionToPrefs(currentOrientation);
        syncAll();
        FloatingService.updateNetworkPositionStatic();
        FloatingService.updateNetworkSizeStatic();
        FloatingService.updateNetworkColorStatic();
        FloatingService.updateNetworkShadowStatic();
        FloatingService.updateNetworkBackgroundStatic();
    }

    private void showExportImportMenu() {
        PopupMenu popup = new PopupMenu(activity, btnExportImport);
        popup.getMenu().add("Ekspor ke Clipboard");
        popup.getMenu().add("Impor dari Clipboard");
        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            if (title.equals("Ekspor ke Clipboard")) {
                PresetManager.exportToClipboard(activity);
            } else {
                PresetManager.importFromClipboard(activity);
            }
            return true;
        });
        popup.show();
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

    private void savePositionToPrefs(String orient) {
        String sfx = "_" + orient;
        prefs.edit()
                .putFloat("network_pos_x" + sfx, NetworkConfig.posX)
                .putFloat("network_pos_y" + sfx, NetworkConfig.posY)
                .apply();
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
