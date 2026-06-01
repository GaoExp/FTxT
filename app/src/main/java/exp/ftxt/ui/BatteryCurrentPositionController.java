package exp.ftxt.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import exp.ftxt.R;
import exp.ftxt.core.FloatingService;
import exp.ftxt.features.battery_current.BatteryCurrentConfig;
import exp.ftxt.features.battery_current.BatteryCurrentModule;
import exp.ftxt.shared.preset.OverlayPreset;
import exp.ftxt.shared.preset.PresetManager;
import exp.ftxt.shared.ui.DpadController;
import exp.ftxt.shared.ui.ShadowConfig;
import exp.ftxt.shared.ui.SliderPositionController;

public class BatteryCurrentPositionController {

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
    private ActivityResultLauncher<String[]> fileImportLauncher;

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

        if (btnExportImport != null) {
            btnExportImport.setOnClickListener(v -> showExportImportMenu());
        }
        sliderController = new SliderPositionController(
                activity.findViewById(R.id.batCurPosXSeekBar),
                activity.findViewById(R.id.batCurPosYSeekBar),
                activity.findViewById(R.id.batCurPosXLabel),
                activity.findViewById(R.id.batCurPosYLabel),
                (x, y) -> onPositionChanged(x, y)
        );
        setupListeners();
        syncAll();

        fileImportLauncher = ((AppCompatActivity) activity).registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) {
                        int count = PresetManager.importFromFile(activity, uri);
                        Toast.makeText(activity, "Berhasil impor " + count + " preset", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void bindViews() {
        btnUp = activity.findViewById(R.id.batCurBtnUp);
        btnDown = activity.findViewById(R.id.batCurBtnDown);
        btnLeft = activity.findViewById(R.id.batCurBtnLeft);
        btnRight = activity.findViewById(R.id.batCurBtnRight);
        coordDisplay = activity.findViewById(R.id.batCurPosCoordDisplay);
        btnExportImport = activity.findViewById(R.id.batCurBtnExportImport);
    }

    private void setupListeners() {
        dpad = new DpadController(btnUp, btnDown, btnLeft, btnRight, (dx, dy) -> {
            onPositionChanged(clamp(BatteryCurrentConfig.posX + dx), clamp(BatteryCurrentConfig.posY + dy));
        });

        View btnSavePreset = activity.findViewById(R.id.batCurBtnSavePreset);
        if (btnSavePreset != null) {
            btnSavePreset.setOnClickListener(v -> showSavePresetDialog());
        }
    }

    private void showSavePresetDialog() {
        EditText input = new EditText(activity);
        input.setHint("Nama preset");

        new AlertDialog.Builder(activity)
                .setTitle("Simpan Preset")
                .setMessage("Simpan konfigurasi Battery Current saat ini sebagai preset?")
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
        preset.posX = BatteryCurrentConfig.posX;
        preset.posY = BatteryCurrentConfig.posY;
        preset.size = BatteryCurrentConfig.size;
        preset.color = BatteryCurrentConfig.color;
        ShadowConfig sc = BatteryCurrentConfig.shadow;
        preset.shadow = new ShadowConfig(sc.enabled, sc.color, sc.blur, sc.offsetX, sc.offsetY);
        preset.bgEnabled = BatteryCurrentConfig.bgEnabled;
        preset.bgColor = BatteryCurrentConfig.bgColor;
        preset.bgPadding = BatteryCurrentConfig.bgPadding;
        preset.bgOffsetX = BatteryCurrentConfig.bgOffsetX;
        preset.bgOffsetY = BatteryCurrentConfig.bgOffsetY;
        preset.bgMargin = BatteryCurrentConfig.bgMargin;
        preset.bgRadius = BatteryCurrentConfig.bgRadius;
        int orientation = activity.getResources().getConfiguration().orientation;
        preset.orientation = (orientation == Configuration.ORIENTATION_LANDSCAPE) ? "landscape" : "portrait";
        preset.touchPassthrough = BatteryCurrentConfig.touchPassthrough;
        preset.safeArea = BatteryCurrentConfig.safeArea;
        preset.showVoltage = BatteryCurrentConfig.showVoltage;
        preset.showCurrent = BatteryCurrentConfig.showCurrent;
        preset.showPower = BatteryCurrentConfig.showPower;

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
        BatteryCurrentConfig.posX = preset.posX;
        BatteryCurrentConfig.posY = preset.posY;
        BatteryCurrentConfig.size = preset.size;
        BatteryCurrentConfig.color = preset.color;
        if (preset.shadow != null) {
            BatteryCurrentConfig.shadow.enabled = preset.shadow.enabled;
            BatteryCurrentConfig.shadow.color = preset.shadow.color;
            BatteryCurrentConfig.shadow.blur = preset.shadow.blur;
            BatteryCurrentConfig.shadow.offsetX = preset.shadow.offsetX;
            BatteryCurrentConfig.shadow.offsetY = preset.shadow.offsetY;
        }
        BatteryCurrentConfig.bgEnabled = preset.bgEnabled;
        BatteryCurrentConfig.bgColor = preset.bgColor;
        BatteryCurrentConfig.bgPadding = preset.bgPadding;
        BatteryCurrentConfig.bgOffsetX = preset.bgOffsetX;
        BatteryCurrentConfig.bgOffsetY = preset.bgOffsetY;
        BatteryCurrentConfig.bgMargin = preset.bgMargin;
        BatteryCurrentConfig.bgRadius = preset.bgRadius;
        if (preset.touchPassthrough != null) {
            BatteryCurrentConfig.touchPassthrough = preset.touchPassthrough;
            prefs.edit().putBoolean("batcur_lock", BatteryCurrentConfig.touchPassthrough).apply();
        }
        if (preset.safeArea != null) {
            BatteryCurrentConfig.safeArea = preset.safeArea;
            prefs.edit().putBoolean("batcur_safe_area", BatteryCurrentConfig.safeArea).apply();
        }
        if (preset.showVoltage != null) {
            BatteryCurrentConfig.showVoltage = preset.showVoltage;
            prefs.edit().putBoolean("batcur_show_voltage", BatteryCurrentConfig.showVoltage).apply();
        }
        if (preset.showCurrent != null) {
            BatteryCurrentConfig.showCurrent = preset.showCurrent;
            prefs.edit().putBoolean("batcur_show_current", BatteryCurrentConfig.showCurrent).apply();
        }
        if (preset.showPower != null) {
            BatteryCurrentConfig.showPower = preset.showPower;
            prefs.edit().putBoolean("batcur_show_power", BatteryCurrentConfig.showPower).apply();
        }

        savePositionToPrefs(currentOrientation);
        syncAll();
        FloatingService.updateBatteryCurrentPositionStatic();
        FloatingService.updateBatteryCurrentSizeStatic();
        FloatingService.updateBatteryCurrentColorStatic();
        FloatingService.updateBatteryCurrentShadowStatic();
        FloatingService.updateBatteryCurrentBackgroundStatic();
    }

    private void showExportImportMenu() {
        PopupMenu popup = new PopupMenu(activity, btnExportImport);
        popup.getMenu().add("Ekspor ke File");
        popup.getMenu().add("Bagikan Preset");
        popup.getMenu().add("Impor dari File");
        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            if (title.equals("Ekspor ke File")) {
                String filename = "ftxt_presets_" + System.currentTimeMillis() + ".txt";
                if (PresetManager.exportToFile(activity, filename)) {
                    Toast.makeText(activity, "Semua preset diekspor ke Downloads/" + filename, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(activity, "Gagal mengekspor preset", Toast.LENGTH_SHORT).show();
                }
            } else if (title.equals("Bagikan Preset")) {
                String toShare = activePresetName;
                if (toShare == null || toShare.isEmpty()) {
                    java.util.List<String> names = PresetManager.getAllNames(activity);
                    if (!names.isEmpty()) toShare = names.get(0);
                }
                if (toShare == null || toShare.isEmpty()) {
                    Toast.makeText(activity, "Tidak ada preset untuk dibagikan", Toast.LENGTH_SHORT).show();
                } else {
                    PresetManager.sharePreset(activity, toShare);
                }
            } else if (title.equals("Impor dari File")) {
                fileImportLauncher.launch(new String[]{"text/plain"});
            }
            return true;
        });
        popup.show();
    }

    private static float clamp(float val) {
        return Math.max(0, Math.min(1, val));
    }

    private void onPositionChanged(float x, float y) {
        BatteryCurrentConfig.posX = x;
        BatteryCurrentConfig.posY = y;
        syncAll();
        savePositionToPrefs(currentOrientation);
        FloatingService.updateBatteryCurrentPositionStatic();
    }

    private void savePositionToPrefs(String orient) {
        String sfx = "_" + orient;
        prefs.edit()
                .putFloat("batcur_pos_x" + sfx, BatteryCurrentConfig.posX)
                .putFloat("batcur_pos_y" + sfx, BatteryCurrentConfig.posY)
                .apply();
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