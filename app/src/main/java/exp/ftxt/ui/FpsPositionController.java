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
import exp.ftxt.features.fps.FpsConfig;
import exp.ftxt.features.fps.FpsModule;
import exp.ftxt.shared.preset.OverlayPreset;
import exp.ftxt.shared.preset.PresetManager;
import exp.ftxt.shared.ui.DpadController;
import exp.ftxt.shared.ui.ShadowConfig;
import exp.ftxt.shared.ui.SliderPositionController;

public class FpsPositionController {

    private final Activity activity;
    private final SharedPreferences prefs;

    private View btnUp, btnDown, btnLeft, btnRight;
    private View btnPortrait, btnLandscape;
    private String currentOrientation;

    private DpadController dpad;
    private SliderPositionController sliderController;
    private TextView coordDisplay;
    private View btnExportImport;
    private int displayWidth, displayHeight;

    private static final String PREFS_NAME = "ftxt_prefs";
    private String activePresetName;

    public FpsPositionController(Activity activity) {
        this.activity = activity;
        this.prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        int orientation = activity.getResources().getConfiguration().orientation;
        currentOrientation = (orientation == Configuration.ORIENTATION_LANDSCAPE) ? "land" : "port";
        loadPositionFromPrefs(currentOrientation);

        FloatingService.setFpsOrientationSuffixStatic(currentOrientation);

        bindViews();

        WindowManager wm = activity.getWindowManager();
        DisplayMetrics realMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(realMetrics);
        displayWidth = realMetrics.widthPixels;
        displayHeight = realMetrics.heightPixels;

        FpsModule.onPositionUpdate = this::syncAll;

        if (btnExportImport != null) {
            btnExportImport.setOnClickListener(v -> showExportImportMenu());
        }
        sliderController = new SliderPositionController(
                activity.findViewById(R.id.fps_posXSeekBar),
                activity.findViewById(R.id.fps_posYSeekBar),
                activity.findViewById(R.id.fps_posXLabel),
                activity.findViewById(R.id.fps_posYLabel),
                (x, y) -> onPositionChanged(x, y)
        );
        setupListeners();
        syncAll();
    }

    private void bindViews() {
        btnUp = activity.findViewById(R.id.fps_btnUp);
        btnDown = activity.findViewById(R.id.fps_btnDown);
        btnLeft = activity.findViewById(R.id.fps_btnLeft);
        btnRight = activity.findViewById(R.id.fps_btnRight);
        btnPortrait = activity.findViewById(R.id.fps_btnPortrait);
        btnLandscape = activity.findViewById(R.id.fps_btnLandscape);
        coordDisplay = activity.findViewById(R.id.fps_posCoordDisplay);
        btnExportImport = activity.findViewById(R.id.fps_btnExportImport);
    }

    private void setupListeners() {
        dpad = new DpadController(btnUp, btnDown, btnLeft, btnRight, (dx, dy) -> {
            onPositionChanged(clamp(FpsConfig.posX + dx), clamp(FpsConfig.posY + dy));
        });

        View btnSavePreset = activity.findViewById(R.id.fps_btnSavePreset);
        if (btnSavePreset != null) {
            btnSavePreset.setOnClickListener(v -> showSavePresetDialog());
        }

        btnPortrait.setOnClickListener(v -> setOrientationMode("port"));
        btnLandscape.setOnClickListener(v -> setOrientationMode("land"));
        updateOrientationButtons();
    }

    private void showSavePresetDialog() {
        EditText input = new EditText(activity);
        input.setHint("Nama preset");

        new AlertDialog.Builder(activity)
                .setTitle("Simpan Preset")
                .setMessage("Simpan konfigurasi FPS saat ini sebagai preset?")
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
        preset.posX = FpsConfig.posX;
        preset.posY = FpsConfig.posY;
        preset.size = FpsConfig.size;
        preset.color = FpsConfig.color;
        ShadowConfig sc = FpsConfig.shadow;
        preset.shadow = new ShadowConfig(sc.enabled, sc.color, sc.blur, sc.offsetX, sc.offsetY);
        preset.bgEnabled = FpsConfig.bgEnabled;
        preset.bgColor = FpsConfig.bgColor;
        preset.bgPadding = FpsConfig.bgPadding;
        preset.bgOffsetX = FpsConfig.bgOffsetX;
        preset.bgOffsetY = FpsConfig.bgOffsetY;
        preset.bgMargin = FpsConfig.bgMargin;
        preset.bgRadius = FpsConfig.bgRadius;
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
        FpsConfig.posX = preset.posX;
        FpsConfig.posY = preset.posY;
        FpsConfig.size = preset.size;
        FpsConfig.color = preset.color;
        if (preset.shadow != null) {
            FpsConfig.shadow.enabled = preset.shadow.enabled;
            FpsConfig.shadow.color = preset.shadow.color;
            FpsConfig.shadow.blur = preset.shadow.blur;
            FpsConfig.shadow.offsetX = preset.shadow.offsetX;
            FpsConfig.shadow.offsetY = preset.shadow.offsetY;
        }
        FpsConfig.bgEnabled = preset.bgEnabled;
        FpsConfig.bgColor = preset.bgColor;
        FpsConfig.bgPadding = preset.bgPadding;
        FpsConfig.bgOffsetX = preset.bgOffsetX;
        FpsConfig.bgOffsetY = preset.bgOffsetY;
        FpsConfig.bgMargin = preset.bgMargin;
        FpsConfig.bgRadius = preset.bgRadius;

        savePositionToPrefs(currentOrientation);
        syncAll();
        FloatingService.updateFpsPositionStatic();
        FloatingService.updateFpsSizeStatic();
        FloatingService.updateFpsColorStatic();
        FloatingService.updateFpsShadowStatic();
        FloatingService.updateFpsBackgroundStatic();
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
        FpsConfig.posX = x;
        FpsConfig.posY = y;
        syncAll();
        savePositionToPrefs(currentOrientation);
        FloatingService.updateFpsPositionStatic();
    }

    private void setOrientationMode(String mode) {
        if (mode.equals(currentOrientation)) return;

        savePositionToPrefs(currentOrientation);

        currentOrientation = mode;
        FloatingService.setFpsOrientationSuffixStatic(mode);
        loadPositionFromPrefs(mode);

        syncAll();
        FloatingService.updateFpsPositionStatic();
        updateOrientationButtons();
    }

    private void savePositionToPrefs(String orient) {
        String sfx = "_" + orient;
        prefs.edit()
                .putFloat("fps_pos_x" + sfx, FpsConfig.posX)
                .putFloat("fps_pos_y" + sfx, FpsConfig.posY)
                .apply();
    }

    private void loadPositionFromPrefs(String orient) {
        String sfx = "_" + orient;
        FpsConfig.posX = prefs.getFloat("fps_pos_x" + sfx, 0.5f);
        FpsConfig.posY = prefs.getFloat("fps_pos_y" + sfx, 0.5f);
    }

    private void updateOrientationButtons() {
        btnPortrait.setSelected("port".equals(currentOrientation));
        btnLandscape.setSelected("land".equals(currentOrientation));
    }

    public void cleanup() {
        FpsModule.onPositionUpdate = null;
        if (dpad != null) dpad.cleanup();
    }

    public void refresh() {
        syncAll();
    }

    public void syncAll() {
        sliderController.sync(FpsConfig.posX, FpsConfig.posY);
        updateCoordDisplay();
    }

    private void updateCoordDisplay() {
        if (coordDisplay == null) return;
        int px, py;
        int[] pos = FloatingService.getFpsCurrentPosition();
        if (pos != null) {
            px = pos[0];
            py = pos[1];
        } else {
            px = Math.round(FpsConfig.posX * displayWidth);
            py = Math.round(FpsConfig.posY * displayHeight);
        }
        coordDisplay.setText(px + "X" + py);
    }

}
