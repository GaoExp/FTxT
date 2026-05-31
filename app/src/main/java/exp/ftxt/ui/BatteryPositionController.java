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
import exp.ftxt.features.battery_temperature.BatteryConfig;
import exp.ftxt.features.battery_temperature.BatteryModule;
import exp.ftxt.shared.preset.OverlayPreset;
import exp.ftxt.shared.preset.PresetManager;
import exp.ftxt.shared.ui.DpadController;
import exp.ftxt.shared.ui.ShadowConfig;
import exp.ftxt.shared.ui.SliderPositionController;

public class BatteryPositionController {

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

        if (btnExportImport != null) {
            btnExportImport.setOnClickListener(v -> showExportImportMenu());
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
        coordDisplay = activity.findViewById(R.id.battery_posCoordDisplay);
        btnExportImport = activity.findViewById(R.id.battery_btnExportImport);
    }

    private void setupListeners() {
        dpad = new DpadController(btnUp, btnDown, btnLeft, btnRight, (dx, dy) -> {
            onPositionChanged(clamp(BatteryConfig.posX + dx), clamp(BatteryConfig.posY + dy));
        });

        View btnSavePreset = activity.findViewById(R.id.battery_btnSavePreset);
        if (btnSavePreset != null) {
            btnSavePreset.setOnClickListener(v -> showSavePresetDialog());
        }

    }

    private void showSavePresetDialog() {
        EditText input = new EditText(activity);
        input.setHint("Nama preset");

        new AlertDialog.Builder(activity)
                .setTitle("Simpan Preset")
                .setMessage("Simpan konfigurasi Baterai saat ini sebagai preset?")
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
        preset.posX = BatteryConfig.posX;
        preset.posY = BatteryConfig.posY;
        preset.size = BatteryConfig.size;
        preset.color = BatteryConfig.color;
        ShadowConfig sc = BatteryConfig.shadow;
        preset.shadow = new ShadowConfig(sc.enabled, sc.color, sc.blur, sc.offsetX, sc.offsetY);
        preset.bgEnabled = BatteryConfig.bgEnabled;
        preset.bgColor = BatteryConfig.bgColor;
        preset.bgPadding = BatteryConfig.bgPadding;
        preset.bgOffsetX = BatteryConfig.bgOffsetX;
        preset.bgOffsetY = BatteryConfig.bgOffsetY;
        preset.bgMargin = BatteryConfig.bgMargin;
        preset.bgRadius = BatteryConfig.bgRadius;
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
        BatteryConfig.posX = preset.posX;
        BatteryConfig.posY = preset.posY;
        BatteryConfig.size = preset.size;
        BatteryConfig.color = preset.color;
        if (preset.shadow != null) {
            BatteryConfig.shadow.enabled = preset.shadow.enabled;
            BatteryConfig.shadow.color = preset.shadow.color;
            BatteryConfig.shadow.blur = preset.shadow.blur;
            BatteryConfig.shadow.offsetX = preset.shadow.offsetX;
            BatteryConfig.shadow.offsetY = preset.shadow.offsetY;
        }
        BatteryConfig.bgEnabled = preset.bgEnabled;
        BatteryConfig.bgColor = preset.bgColor;
        BatteryConfig.bgPadding = preset.bgPadding;
        BatteryConfig.bgOffsetX = preset.bgOffsetX;
        BatteryConfig.bgOffsetY = preset.bgOffsetY;
        BatteryConfig.bgMargin = preset.bgMargin;
        BatteryConfig.bgRadius = preset.bgRadius;

        savePositionToPrefs(currentOrientation);
        syncAll();
        FloatingService.updateBatteryPositionStatic();
        FloatingService.updateBatterySizeStatic();
        FloatingService.updateBatteryColorStatic();
        FloatingService.updateBatteryShadowStatic();
        FloatingService.updateBatteryBackgroundStatic();
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
        BatteryConfig.posX = x;
        BatteryConfig.posY = y;
        syncAll();
        savePositionToPrefs(currentOrientation);
        FloatingService.updateBatteryPositionStatic();
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
        BatteryConfig.posX = prefs.getFloat("battery_pos_x" + sfx, 0.05f);
        BatteryConfig.posY = prefs.getFloat("battery_pos_y" + sfx, 0.8f);
    }

    public void cleanup() {
        BatteryModule.onPositionUpdate = null;
        if (dpad != null) dpad.cleanup();
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

}
