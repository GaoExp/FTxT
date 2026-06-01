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
import exp.ftxt.features.clock_module.ClockConfig;
import exp.ftxt.features.clock_module.ClockModule;
import exp.ftxt.shared.preset.OverlayPreset;
import exp.ftxt.shared.preset.PresetManager;
import exp.ftxt.shared.ui.DpadController;
import exp.ftxt.shared.ui.ShadowConfig;
import exp.ftxt.shared.ui.SliderPositionController;

public class ClockPositionController {

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

    public ClockPositionController(Activity activity) {
        this.activity = activity;
        this.prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        int orientation = activity.getResources().getConfiguration().orientation;
        currentOrientation = (orientation == Configuration.ORIENTATION_LANDSCAPE) ? "land" : "port";
        loadPositionFromPrefs(currentOrientation);

        FloatingService.setClockOrientationSuffixStatic(currentOrientation);

        bindViews();

        WindowManager wm = activity.getWindowManager();
        DisplayMetrics realMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(realMetrics);
        displayWidth = realMetrics.widthPixels;
        displayHeight = realMetrics.heightPixels;

        ClockModule.onPositionUpdate = this::syncAll;

        if (btnExportImport != null) {
            btnExportImport.setOnClickListener(v -> showExportImportMenu());
        }
        sliderController = new SliderPositionController(
                activity.findViewById(R.id.clock_posXSeekBar),
                activity.findViewById(R.id.clock_posYSeekBar),
                activity.findViewById(R.id.clock_posXLabel),
                activity.findViewById(R.id.clock_posYLabel),
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
        btnUp = activity.findViewById(R.id.clock_btnUp);
        btnDown = activity.findViewById(R.id.clock_btnDown);
        btnLeft = activity.findViewById(R.id.clock_btnLeft);
        btnRight = activity.findViewById(R.id.clock_btnRight);
        coordDisplay = activity.findViewById(R.id.clock_posCoordDisplay);
        btnExportImport = activity.findViewById(R.id.clock_btnExportImport);
    }

    private void setupListeners() {
        dpad = new DpadController(btnUp, btnDown, btnLeft, btnRight, (dx, dy) -> {
            onPositionChanged(clamp(ClockConfig.posX + dx), clamp(ClockConfig.posY + dy));
        });

        View btnSavePreset = activity.findViewById(R.id.clock_btnSavePreset);
        if (btnSavePreset != null) {
            btnSavePreset.setOnClickListener(v -> showSavePresetDialog());
        }

    }

    private void showSavePresetDialog() {
        EditText input = new EditText(activity);
        input.setHint("Nama preset");

        new AlertDialog.Builder(activity)
                .setTitle("Simpan Preset")
                .setMessage("Simpan konfigurasi Jam saat ini sebagai preset?")
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
        preset.posX = ClockConfig.posX;
        preset.posY = ClockConfig.posY;
        preset.size = ClockConfig.size;
        preset.color = ClockConfig.color;
        ShadowConfig sc = ClockConfig.shadow;
        preset.shadow = new ShadowConfig(sc.enabled, sc.color, sc.blur, sc.offsetX, sc.offsetY);
        preset.bgEnabled = ClockConfig.bgEnabled;
        preset.bgColor = ClockConfig.bgColor;
        preset.bgPadding = ClockConfig.bgPadding;
        preset.bgOffsetX = ClockConfig.bgOffsetX;
        preset.bgOffsetY = ClockConfig.bgOffsetY;
        preset.bgMargin = ClockConfig.bgMargin;
        preset.bgRadius = ClockConfig.bgRadius;
        int orientation = activity.getResources().getConfiguration().orientation;
        preset.orientation = (orientation == Configuration.ORIENTATION_LANDSCAPE) ? "landscape" : "portrait";
        preset.touchPassthrough = ClockConfig.touchPassthrough;
        preset.safeArea = ClockConfig.safeArea;

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
        ClockConfig.posX = preset.posX;
        ClockConfig.posY = preset.posY;
        ClockConfig.size = preset.size;
        ClockConfig.color = preset.color;
        if (preset.shadow != null) {
            ClockConfig.shadow.enabled = preset.shadow.enabled;
            ClockConfig.shadow.color = preset.shadow.color;
            ClockConfig.shadow.blur = preset.shadow.blur;
            ClockConfig.shadow.offsetX = preset.shadow.offsetX;
            ClockConfig.shadow.offsetY = preset.shadow.offsetY;
        }
        ClockConfig.bgEnabled = preset.bgEnabled;
        ClockConfig.bgColor = preset.bgColor;
        ClockConfig.bgPadding = preset.bgPadding;
        ClockConfig.bgOffsetX = preset.bgOffsetX;
        ClockConfig.bgOffsetY = preset.bgOffsetY;
        ClockConfig.bgMargin = preset.bgMargin;
        ClockConfig.bgRadius = preset.bgRadius;
        if (preset.touchPassthrough != null) {
            ClockConfig.touchPassthrough = preset.touchPassthrough;
            prefs.edit().putBoolean("clock_lock", ClockConfig.touchPassthrough).apply();
        }
        if (preset.safeArea != null) {
            ClockConfig.safeArea = preset.safeArea;
            prefs.edit().putBoolean("clock_safe_area", ClockConfig.safeArea).apply();
        }

        savePositionToPrefs(currentOrientation);
        syncAll();
        FloatingService.updateClockPositionStatic();
        FloatingService.updateClockSizeStatic();
        FloatingService.updateClockColorStatic();
        FloatingService.updateClockShadowStatic();
        FloatingService.updateClockBackgroundStatic();
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
        ClockConfig.posX = x;
        ClockConfig.posY = y;
        syncAll();
        savePositionToPrefs(currentOrientation);
        FloatingService.updateClockPositionStatic();
    }

    private void savePositionToPrefs(String orient) {
        String sfx = "_" + orient;
        prefs.edit()
                .putFloat("clock_pos_x" + sfx, ClockConfig.posX)
                .putFloat("clock_pos_y" + sfx, ClockConfig.posY)
                .apply();
    }

    private void loadPositionFromPrefs(String orient) {
        String sfx = "_" + orient;
        ClockConfig.posX = prefs.getFloat("clock_pos_x" + sfx, 0.5f);
        ClockConfig.posY = prefs.getFloat("clock_pos_y" + sfx, 0.05f);
    }

    public void cleanup() {
        ClockModule.onPositionUpdate = null;
        if (dpad != null) dpad.cleanup();
    }

    public void refresh() {
        syncAll();
    }

    public void syncAll() {
        sliderController.sync(ClockConfig.posX, ClockConfig.posY);
        updateCoordDisplay();
    }

    private void updateCoordDisplay() {
        if (coordDisplay == null) return;
        int px, py;
        int[] pos = FloatingService.getClockCurrentPosition();
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