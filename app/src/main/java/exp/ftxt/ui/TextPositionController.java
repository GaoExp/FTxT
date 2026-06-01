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
import exp.ftxt.features.floating_text.TextConfig;
import exp.ftxt.features.floating_text.TextModule;
import exp.ftxt.shared.preset.OverlayPreset;
import exp.ftxt.shared.preset.PresetManager;
import exp.ftxt.shared.ui.DpadController;
import exp.ftxt.shared.ui.ShadowConfig;
import exp.ftxt.shared.ui.SliderPositionController;

public class TextPositionController {

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

    public TextPositionController(Activity activity) {
        this.activity = activity;
        this.prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        int orientation = activity.getResources().getConfiguration().orientation;
        currentOrientation = (orientation == Configuration.ORIENTATION_LANDSCAPE) ? "land" : "port";
        loadPositionFromPrefs(currentOrientation);

        FloatingService.setTextOrientationSuffixStatic(currentOrientation);

        bindViews();

        WindowManager wm = activity.getWindowManager();
        DisplayMetrics realMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(realMetrics);
        displayWidth = realMetrics.widthPixels;
        displayHeight = realMetrics.heightPixels;

        TextModule.onPositionUpdate = this::syncAll;

        if (btnExportImport != null) {
            btnExportImport.setOnClickListener(v -> showExportImportMenu());
        }
        sliderController = new SliderPositionController(
                activity.findViewById(R.id.posXSeekBar),
                activity.findViewById(R.id.posYSeekBar),
                activity.findViewById(R.id.posXLabel),
                activity.findViewById(R.id.posYLabel),
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
        btnUp = activity.findViewById(R.id.btnUp);
        btnDown = activity.findViewById(R.id.btnDown);
        btnLeft = activity.findViewById(R.id.btnLeft);
        btnRight = activity.findViewById(R.id.btnRight);
        coordDisplay = activity.findViewById(R.id.posCoordDisplay);
        btnExportImport = activity.findViewById(R.id.btnExportImport);
    }

    private void setupListeners() {
        dpad = new DpadController(btnUp, btnDown, btnLeft, btnRight, (dx, dy) -> {
            onPositionChanged(clamp(TextConfig.posX + dx), clamp(TextConfig.posY + dy));
        });

        View btnSavePreset = activity.findViewById(R.id.btnSavePreset);
        if (btnSavePreset != null) {
            btnSavePreset.setOnClickListener(v -> showSavePresetDialog());
        }

        View btnLoadPreset = activity.findViewById(R.id.btnLoadPreset);
        if (btnLoadPreset != null) {
            btnLoadPreset.setOnClickListener(v -> showLoadPresetDialog());
        }

    }

    private void showSavePresetDialog() {
        EditText input = new EditText(activity);
        input.setHint("Nama preset");

        new AlertDialog.Builder(activity)
                .setTitle("Simpan Preset")
                .setMessage("Simpan konfigurasi overlay saat ini sebagai preset?")
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
        preset.posX = TextConfig.posX;
        preset.posY = TextConfig.posY;
        preset.size = TextConfig.size;
        preset.color = TextConfig.color;
        ShadowConfig sc = TextConfig.shadow;
        preset.shadow = new ShadowConfig(sc.enabled, sc.color, sc.blur, sc.offsetX, sc.offsetY);
        preset.bgEnabled = TextConfig.bgEnabled;
        preset.bgColor = TextConfig.bgColor;
        preset.bgPadding = TextConfig.bgPadding;
        preset.bgOffsetX = TextConfig.bgOffsetX;
        preset.bgOffsetY = TextConfig.bgOffsetY;
        preset.bgMargin = TextConfig.bgMargin;
        preset.bgRadius = TextConfig.bgRadius;
        int orientation = activity.getResources().getConfiguration().orientation;
        preset.orientation = (orientation == Configuration.ORIENTATION_LANDSCAPE) ? "landscape" : "portrait";
        preset.touchPassthrough = TextConfig.touchPassthrough;
        preset.safeArea = TextConfig.safeArea;
        preset.textContent = TextConfig.text;

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
        TextConfig.posX = preset.posX;
        TextConfig.posY = preset.posY;
        TextConfig.size = preset.size;
        TextConfig.color = preset.color;
        if (preset.shadow != null) {
            TextConfig.shadow.enabled = preset.shadow.enabled;
            TextConfig.shadow.color = preset.shadow.color;
            TextConfig.shadow.blur = preset.shadow.blur;
            TextConfig.shadow.offsetX = preset.shadow.offsetX;
            TextConfig.shadow.offsetY = preset.shadow.offsetY;
        }
        TextConfig.bgEnabled = preset.bgEnabled;
        TextConfig.bgColor = preset.bgColor;
        TextConfig.bgPadding = preset.bgPadding;
        TextConfig.bgOffsetX = preset.bgOffsetX;
        TextConfig.bgOffsetY = preset.bgOffsetY;
        TextConfig.bgMargin = preset.bgMargin;
        TextConfig.bgRadius = preset.bgRadius;
        if (preset.touchPassthrough != null) {
            TextConfig.touchPassthrough = preset.touchPassthrough;
            prefs.edit().putBoolean("text_lock", TextConfig.touchPassthrough).apply();
        }
        if (preset.safeArea != null) {
            TextConfig.safeArea = preset.safeArea;
            prefs.edit().putBoolean("text_safe_area", TextConfig.safeArea).apply();
        }
        if (preset.textContent != null && !preset.textContent.isEmpty()) {
            TextConfig.text = preset.textContent;
        }

        savePositionToPrefs(currentOrientation);
        if (preset.textContent != null && !preset.textContent.isEmpty()) {
            FloatingService.updateTextStatic();
        }
        syncAll();
        FloatingService.updateTextPositionStatic();
        FloatingService.updateTextSizeStatic();
        FloatingService.updateTextColorStatic();
        FloatingService.updateShadowStatic();
        FloatingService.updateTextBackgroundStatic();
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
        TextConfig.posX = x;
        TextConfig.posY = y;
        syncAll();
        savePositionToPrefs(currentOrientation);
        FloatingService.updateTextPositionStatic();
    }

    private void savePositionToPrefs(String orient) {
        String sfx = "_" + orient;
        prefs.edit()
                .putFloat("text_pos_x" + sfx, TextConfig.posX)
                .putFloat("text_pos_y" + sfx, TextConfig.posY)
                .apply();
    }

    private void loadPositionFromPrefs(String orient) {
        String sfx = "_" + orient;
        TextConfig.posX = prefs.getFloat("text_pos_x" + sfx, 0.5f);
        TextConfig.posY = prefs.getFloat("text_pos_y" + sfx, 0.8f);
    }

    // ====================================================================

    public void cleanup() {
        TextModule.onPositionUpdate = null;
        if (dpad != null) dpad.cleanup();
    }

    public void refresh() {
        syncAll();
    }

    public void syncAll() {
        sliderController.sync(TextConfig.posX, TextConfig.posY);
        updateCoordDisplay();
    }

    private void updateCoordDisplay() {
        if (coordDisplay == null) return;
        int px, py;
        int[] pos = FloatingService.getTextCurrentPosition();
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