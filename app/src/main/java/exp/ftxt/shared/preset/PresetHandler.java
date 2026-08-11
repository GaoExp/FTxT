package exp.ftxt.shared.preset;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.view.View;
import android.widget.EditText;

import java.util.List;
import java.util.function.Consumer;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import exp.ftxt.shared.ui.ShadowConfig;

public class PresetHandler {

    public interface Delegate {
        String moduleLabel();
        String moduleType();
        String touchPassthroughPrefKey();
        String safeAreaPrefKey();
        String posXPrefKey();
        String posYPrefKey();

        void saveToPreset(OverlayPreset preset);
        void applyFromPreset(Activity activity, OverlayPreset preset, SharedPreferences prefs);
        void syncToService();
    }

    public static void showSavePresetDialog(Activity activity, Delegate delegate, Runnable onSaved) {
        EditText input = new EditText(activity);
        input.setHint("Nama preset");

        new AlertDialog.Builder(activity)
                .setTitle("Simpan Preset")
                .setMessage("Simpan konfigurasi " + delegate.moduleLabel() + " saat ini sebagai preset?")
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
                                .setPositiveButton("Ya", (d2, w2) -> doSavePreset(activity, name, delegate, onSaved))
                                .setNegativeButton("Batal", null)
                                .show();
                    } else {
                        doSavePreset(activity, name, delegate, onSaved);
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private static void doSavePreset(Activity activity, String name, Delegate delegate, Runnable onSaved) {
        OverlayPreset preset = new OverlayPreset();
        delegate.saveToPreset(preset);
        preset.orientation = getCurrentOrientation(activity);
        PresetManager.save(activity, name, preset);
        Toast.makeText(activity, "Preset \"" + name + "\" tersimpan", Toast.LENGTH_SHORT).show();
        if (onSaved != null) onSaved.run();
    }

    public static void showLoadPresetDialog(Activity activity, Delegate delegate,
                                            StringHolder activePresetName, Runnable postApply) {
        showLoadPresetDialog(activity, delegate, activePresetName, postApply, null);
    }

    public static void showLoadPresetDialog(Activity activity, Delegate delegate,
                                            StringHolder activePresetName, Runnable postApply, Consumer<Runnable> onSaveClick) {
        if (!(activity instanceof FragmentActivity)) {
            PresetManager.showLoadPresetDialog(activity, activePresetName.value, name -> {
                doLoadPreset(activity, name, delegate, activePresetName, postApply);
            });
            return;
        }
        new PresetBrowserDialog(activity, name -> {
            doLoadPreset(activity, name, delegate, activePresetName, postApply);
        }, null, onSaveClick, delegate.moduleType()).show(((FragmentActivity) activity).getSupportFragmentManager(), "PresetBrowserDialog");
    }

    private static void doLoadPreset(Activity activity, String name, Delegate delegate,
                                      StringHolder activePresetName, Runnable postApply) {
        List<String> names = PresetManager.getAllNames(activity);
        if (!names.contains(name)) {
            Toast.makeText(activity, "Preset \"" + name + "\" tidak ditemukan", Toast.LENGTH_SHORT).show();
            return;
        }
        OverlayPreset preset = PresetManager.load(activity, name);
        if (preset == null) {
            Toast.makeText(activity, "Gagal memuat preset", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!applyPreset(activity, preset, delegate)) {
            return;
        }
        activePresetName.value = name;
        if (postApply != null) postApply.run();
        Toast.makeText(activity, "Preset \"" + name + "\" diterapkan", Toast.LENGTH_SHORT).show();
    }

    private static boolean applyPreset(Activity activity, OverlayPreset preset, Delegate delegate) {
        SharedPreferences prefs = activity.getSharedPreferences("ftxt_prefs", Context.MODE_PRIVATE);
        String delegateType = delegate.moduleType();
        String presetType = preset.moduleType;
        if (presetType != null && !presetType.isEmpty() && !presetType.equals(delegateType)) {
            Toast.makeText(activity, "Preset ini untuk modul \"" + presetType + "\"", Toast.LENGTH_SHORT).show();
            return false;
        }
        delegate.applyFromPreset(activity, preset, prefs);
        String orient = normalizeOrientation(preset.orientation);
        if (orient == null) orient = getCurrentOrientation(activity);
        savePositionToPrefs(prefs, delegate, orient, preset.posX, preset.posY);
        delegate.syncToService();
        return true;
    }

    private static String normalizeOrientation(String orient) {
        if ("land".equals(orient) || "landscape".equals(orient)) return "land";
        if ("port".equals(orient) || "portrait".equals(orient)) return "port";
        return null;
    }

    public static void savePositionToPrefs(SharedPreferences prefs, Delegate delegate,
                                           String orient, float posX, float posY) {
        String sfx = "_" + orient;
        prefs.edit()
                .putFloat(delegate.posXPrefKey() + sfx, posX)
                .putFloat(delegate.posYPrefKey() + sfx, posY)
                .apply();
    }

    public static String getCurrentOrientation(Activity activity) {
        int orientation = activity.getResources().getConfiguration().orientation;
        return (orientation == Configuration.ORIENTATION_LANDSCAPE) ? "land" : "port";
    }

    public static class StringHolder {
        public String value;
        public StringHolder() {}
        public StringHolder(String value) { this.value = value; }
    }

    public static ShadowConfig copyShadow(ShadowConfig src) {
        if (src == null) return new ShadowConfig();
        return new ShadowConfig(src.enabled, src.color, src.blur, src.offsetX, src.offsetY);
    }
}
