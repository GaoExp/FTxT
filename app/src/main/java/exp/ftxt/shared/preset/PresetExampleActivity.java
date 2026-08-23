package exp.ftxt.shared.preset;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import exp.ftxt.core.FloatingService;
import exp.ftxt.features.floating_text.TextConfig;
import exp.ftxt.shared.config.ShadowConfig;

public class PresetExampleActivity {

    public static void showSavePresetDialog(Activity activity) {
        EditText input = new EditText(activity);
        input.setHint("Nama preset");

        new AlertDialog.Builder(activity)
                .setTitle("Simpan Preset")
                .setMessage("Simpan konfigurasi overlay saat ini sebagai preset?")
                .setView(input)
                .setPositiveButton("Simpan", (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(activity, "Nama preset tidak boleh kosong",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    OverlayPreset existing = PresetManager.load(activity, name);
                    if (existing != null) {
                        new AlertDialog.Builder(activity)
                                .setTitle("Timpa Preset")
                                .setMessage("Preset \"" + name + "\" sudah ada. Timpa?")
                                .setPositiveButton("Ya", (d2, w2) -> {
                                    doSavePreset(activity, name);
                                })
                                .setNegativeButton("Batal", null)
                                .show();
                    } else {
                        doSavePreset(activity, name);
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private static void doSavePreset(Activity activity, String name) {
        OverlayPreset preset = new OverlayPreset();

        preset.posX = TextConfig.posX;
        preset.posY = TextConfig.posY;
        preset.size = TextConfig.size;

        preset.color = TextConfig.color;

        ShadowConfig sc = TextConfig.shadow;
        preset.shadow = new ShadowConfig(sc.enabled, sc.color, sc.blur, sc.offsetX, sc.offsetY);

        preset.bgEnabled = TextConfig.bg.enabled;
        preset.bgColor = TextConfig.bg.color;
        preset.bgPadding = TextConfig.bg.padding;
        preset.bgOffsetX = TextConfig.bg.offsetX;
        preset.bgOffsetY = TextConfig.bg.offsetY;
        preset.bgMargin = TextConfig.bg.margin;
        preset.bgRadius = TextConfig.bg.radius;

        int orientation = activity.getResources().getConfiguration().orientation;
        preset.orientation = (orientation == 2) ? "landscape" : "portrait";

        PresetManager.save(activity, name, preset);

        Toast.makeText(activity,
                "Preset \"" + name + "\" tersimpan",
                Toast.LENGTH_SHORT).show();
    }

    public static void showLoadPresetDialog(Activity activity) {
        PresetManager.showPresetListDialog(activity, "Muat Preset", name -> {
            OverlayPreset preset = PresetManager.load(activity, name);

            if (preset == null) {
                Toast.makeText(activity, "Gagal memuat preset",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            applyPresetToConfig(activity, preset);

            syncPresetToOverlay();

            Toast.makeText(activity,
                    "Preset \"" + name + "\" diterapkan",
                    Toast.LENGTH_SHORT).show();
        });
    }

    private static void applyPresetToConfig(Activity activity, OverlayPreset preset) {
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

        TextConfig.bg.enabled = preset.bgEnabled;
        TextConfig.bg.color = preset.bgColor;
        TextConfig.bg.padding = preset.bgPadding;
        TextConfig.bg.offsetX = preset.bgOffsetX;
        TextConfig.bg.offsetY = preset.bgOffsetY;
        TextConfig.bg.margin = preset.bgMargin;
        TextConfig.bg.radius = preset.bgRadius;
    }

    private static void syncPresetToOverlay() {
        FloatingService.updatePositionForModule(FloatingService.textModule());
        FloatingService.updateSizeForModule(FloatingService.textModule(), TextConfig.size);
        FloatingService.updateColorForModule(FloatingService.textModule(), TextConfig.color);
        FloatingService.updateShadowForModule(FloatingService.textModule());
        FloatingService.updateBackgroundForModule(FloatingService.textModule());
    }

    public static void showRenamePresetDialog(Activity activity) {
        PresetManager.showPresetListDialog(activity, "Pilih Preset", oldName -> {
            EditText input = new EditText(activity);
            input.setText(oldName);
            input.setSelectAllOnFocus(true);

            new AlertDialog.Builder(activity)
                    .setTitle("Ganti Nama Preset")
                    .setView(input)
                    .setPositiveButton("Simpan", (d, w) -> {
                        String newName = input.getText().toString().trim();
                        if (newName.isEmpty()) {
                            Toast.makeText(activity, "Nama tidak boleh kosong",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        boolean success = PresetManager.rename(activity, oldName, newName);
                        if (success) {
                            Toast.makeText(activity,
                                    "Preset diganti: \"" + oldName + "\" → \"" + newName + "\"",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(activity,
                                    "Gagal mengganti nama preset",
                                    Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });
    }

    public static void showDeletePresetDialog(Activity activity) {
        PresetManager.showPresetListDialog(activity, "Pilih Preset untuk Dihapus", name -> {
            PresetManager.showDeleteConfirmDialog(activity, name, () -> {
            });
        });
    }

    public static void exportPresets(Activity activity) {
        String filename = "ftxt_presets_" + System.currentTimeMillis() + ".txt";
        boolean success = PresetManager.exportToFile(activity, filename);
        if (success) {
            Toast.makeText(activity,
                    "Preset diekspor ke Downloads/" + filename,
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(activity,
                    "Gagal mengekspor file",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public static void openFilePickerForImport(Activity activity) {
        Toast.makeText(activity,
                "Gunakan ActivityResultLauncher — lihat komentar di kode",
                Toast.LENGTH_LONG).show();
    }
}
