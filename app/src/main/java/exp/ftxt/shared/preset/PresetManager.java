package exp.ftxt.shared.preset;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Manager untuk seluruh operasi CRUD preset overlay.
 *
 * Semua method static agar mudah dipanggil dari mana saja tanpa perlu instance.
 * Menggunakan GSON untuk serialize/deserialize JSON + SharedPreferences untuk storage.
 *
 * Struktur penyimpanan SharedPreferences (v2):
 * - KEY_INDEX          = JSON array of PresetIndexItem (ordered)
 * - KEY_PREFIX + uuid  = String JSON dari objek OverlayPreset
 * - KEY_HISTORY_<uuid> = JSON array of PresetVersion (history)
 */
public class PresetManager {

    // ====================================================================
    // KONSTANTA
    // ====================================================================

    /** Nama file SharedPreferences */
    private static final String PREFS_NAME = "ftxt_presets";

    /** Key index (v2) — array of PresetIndexItem */
    private static final String KEY_INDEX = "preset_index_v2";

    /** Key lama — StringSet (tanpa urutan). Untuk migrasi otomatis. */
    private static final String KEY_INDEX_OLD = "preset_name_list";

    /** Prefix key untuk data tiap preset (disimpan per-uuid) */
    private static final String KEY_PREFIX = "preset_data_";

    /** Prefix key untuk history per preset (per-uuid) */
    private static final String KEY_HISTORY_PREFIX = "preset_history_";

    // ====================================================================
    // MODELS (inner static)
    // ====================================================================

    /** Index item metadata untuk satu preset */
    private static class PresetIndexItem {
        String uuid;
        String name;
        long createdAt;
        long updatedAt;
        List<String> tags;
        boolean favorite;
        String thumbnailPath; // relative to filesDir, e.g. "presets/thumb_<uuid>.png"

        PresetIndexItem() {
            tags = new ArrayList<>();
        }
    }

    /** Preset version entry for history */
    private static class PresetVersion {
        long ts;
        OverlayPreset preset;

        PresetVersion() {}

        PresetVersion(long ts, OverlayPreset preset) {
            this.ts = ts;
            this.preset = preset;
        }
    }

    // ====================================================================
    // UTILITY — Ambil SharedPreferences
    // ====================================================================

    /** Mengembalikan objek SharedPreferences milik PresetManager */
    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** Mengembalikan instance GSON dengan pretty printing untuk human-readable JSON */
    private static Gson getGson() {
        return new GsonBuilder().setPrettyPrinting().create();
    }

    // ====================================================================
    // UTILITY — Index handling
    // ====================================================================

    private static List<PresetIndexItem> getIndex(SharedPreferences prefs) {
        String json = prefs.getString(KEY_INDEX, null);
        if (json != null) {
            try {
                Gson gson = getGson();
                Type t = new TypeToken<List<PresetIndexItem>>() {}.getType();
                List<PresetIndexItem> list = gson.fromJson(json, t);
                if (list != null) return list;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Fallback — migrate from old format
        return migrateFromOldFormat(prefs);
    }

    private static void saveIndex(SharedPreferences prefs, List<PresetIndexItem> list) {
        Gson gson = getGson();
        prefs.edit().putString(KEY_INDEX, gson.toJson(list)).apply();
    }

    /** Migrasi dari format lama (StringSet of names) ke index sederhana */
    private static List<PresetIndexItem> migrateFromOldFormat(SharedPreferences prefs) {
        Set<String> oldSet = prefs.getStringSet(KEY_INDEX_OLD, null);
        List<PresetIndexItem> list = new ArrayList<>();
        if (oldSet != null && !oldSet.isEmpty()) {
            long now = System.currentTimeMillis();
            for (String name : oldSet) {
                PresetIndexItem it = new PresetIndexItem();
                it.uuid = UUID.nameUUIDFromBytes(name.getBytes()).toString();
                it.name = name;
                it.createdAt = now;
                it.updatedAt = now;
                list.add(it);

                // Migrate old data if exists under KEY_PREFIX+name
                String oldJson = prefs.getString(KEY_PREFIX + name, null);
                if (oldJson != null) {
                    prefs.edit().putString(KEY_PREFIX + it.uuid, oldJson).remove(KEY_PREFIX + name).apply();
                }
            }
            prefs.edit().remove(KEY_INDEX_OLD).putString(KEY_INDEX, getGson().toJson(list)).apply();
        }
        return list;
    }

    // ====================================================================
    // HELPERS — find by name/uuid
    // ====================================================================

    private static PresetIndexItem findByName(List<PresetIndexItem> index, String name) {
        if (index == null) return null;
        for (PresetIndexItem it : index) if (it.name.equals(name)) return it;
        return null;
    }

    private static PresetIndexItem findByUuid(List<PresetIndexItem> index, String uuid) {
        if (index == null) return null;
        for (PresetIndexItem it : index) if (it.uuid.equals(uuid)) return it;
        return null;
    }

    // ====================================================================
    // 1. SIMPAN PRESET (v2)
    // ====================================================================

    /**
     * Menyimpan satu preset ke SharedPreferences (v2 index + uuid storage).
     * Jika nama sudah ada, update entry dan simpan history (versi sebelumnya).
     * Juga generate thumbnail warna sederhana berdasarkan warna utama.
     */
    public static void save(Context context, String name, OverlayPreset preset) {
        SharedPreferences prefs = getPrefs(context);
        Gson gson = getGson();

        List<PresetIndexItem> index = getIndex(prefs);
        long now = System.currentTimeMillis();

        PresetIndexItem item = findByName(index, name);
        boolean isNew = false;
        if (item == null) {
            // New preset: create uuid and index item
            item = new PresetIndexItem();
            item.uuid = UUID.randomUUID().toString();
            item.name = name;
            item.createdAt = now;
            item.updatedAt = now;
            index.add(item);
            isNew = true;
        } else {
            // existing: push current data to history
            item.updatedAt = now;
            // Save previous version to history
            try {
                String existingJson = prefs.getString(KEY_PREFIX + item.uuid, null);
                if (existingJson != null) {
                    Type listType = new TypeToken<List<PresetVersion>>() {}.getType();
                    List<PresetVersion> history = gson.fromJson(prefs.getString(KEY_HISTORY_PREFIX + item.uuid, null), listType);
                    if (history == null) history = new ArrayList<>();
                    // push current as previous
                    OverlayPreset prev = gson.fromJson(existingJson, OverlayPreset.class);
                    history.add(new PresetVersion(System.currentTimeMillis(), prev));
                    // cap history size
                    if (history.size() > 10) history.remove(0);
                    prefs.edit().putString(KEY_HISTORY_PREFIX + item.uuid, gson.toJson(history)).apply();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Serialize OverlayPreset → JSON string (store under uuid)
        String json = gson.toJson(preset);
        prefs.edit().putString(KEY_PREFIX + item.uuid, json).apply();

        // Generate simple color thumbnail (fill with preset.color) and save to filesDir
        try {
            String thumbPath = "presets/thumb_" + item.uuid + ".png";
            File out = new File(context.getFilesDir(), thumbPath);
            out.getParentFile().mkdirs();
            int thumbSize = 64;
            Bitmap bmp = Bitmap.createBitmap(thumbSize, thumbSize, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(bmp);
            Paint p = new Paint();
            p.setStyle(Paint.Style.FILL);
            p.setColor(preset.color != 0 ? preset.color : Color.BLACK);
            c.drawRect(0, 0, thumbSize, thumbSize, p);
            FileOutputStream fos = new FileOutputStream(out);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.flush();
            fos.close();
            item.thumbnailPath = thumbPath;
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Save updated index
        saveIndex(prefs, index);

        // Remove legacy storage by name if exists
        if (!isNew) {
            prefs.edit().remove(KEY_PREFIX + name).apply();
        }
    }

    // ====================================================================
    // 2. MUAT PRESET (support name lookup → uuid)
    // ====================================================================

    public static OverlayPreset load(Context context, String name) {
        SharedPreferences prefs = getPrefs(context);
        Gson gson = getGson();

        List<PresetIndexItem> index = getIndex(prefs);
        PresetIndexItem item = findByName(index, name);
        if (item != null) {
            String json = prefs.getString(KEY_PREFIX + item.uuid, null);
            if (json == null) return null;
            return gson.fromJson(json, OverlayPreset.class);
        }

        // Fallback: old key by name
        String oldJson = prefs.getString(KEY_PREFIX + name, null);
        if (oldJson != null) {
            // migrate: create new uuid index entry
            OverlayPreset p = gson.fromJson(oldJson, OverlayPreset.class);
            save(context, name, p);
            return p;
        }

        return null;
    }

    // ====================================================================
    // 3. RENAME PRESET
    // ====================================================================

    public static boolean rename(Context context, String oldName, String newName) {
        SharedPreferences prefs = getPrefs(context);
        List<PresetIndexItem> index = getIndex(prefs);
        PresetIndexItem item = findByName(index, oldName);
        if (item == null) return false;
        item.name = newName;
        item.updatedAt = System.currentTimeMillis();
        saveIndex(prefs, index);
        return true;
    }

    // ====================================================================
    // 4. SELECT — Mendapatkan daftar semua nama preset
    // ====================================================================

    public static List<String> getAllNames(Context context) {
        List<String> out = new ArrayList<>();
        List<PresetIndexItem> index = getIndex(getPrefs(context));
        for (PresetIndexItem it : index) out.add(it.name);
        return out;
    }

    // ====================================================================
    // 5. HAPUS PRESET
    // ====================================================================

    public static void delete(Context context, String name) {
        SharedPreferences prefs = getPrefs(context);
        List<PresetIndexItem> index = getIndex(prefs);
        PresetIndexItem item = findByName(index, name);
        if (item == null) return;

        // remove data & history & thumbnail
        prefs.edit().remove(KEY_PREFIX + item.uuid).remove(KEY_HISTORY_PREFIX + item.uuid).apply();
        if (item.thumbnailPath != null) {
            File f = new File(context.getFilesDir(), item.thumbnailPath);
            if (f.exists()) f.delete();
        }

        index.remove(item);
        saveIndex(prefs, index);
    }

    public static void deleteMultiple(Context context, List<String> names) {
        for (String name : names) delete(context, name);
    }

    public static void deleteAll(Context context) {
        SharedPreferences prefs = getPrefs(context);
        List<PresetIndexItem> index = getIndex(prefs);
        for (PresetIndexItem item : index) {
            prefs.edit().remove(KEY_PREFIX + item.uuid).remove(KEY_HISTORY_PREFIX + item.uuid).apply();
            if (item.thumbnailPath != null) {
                File f = new File(context.getFilesDir(), item.thumbnailPath);
                if (f.exists()) f.delete();
            }
        }
        prefs.edit().remove(KEY_INDEX).apply();
    }

    // ====================================================================
    // 5b. REORDER — Pindah Atas / Bawah
    // ====================================================================

    public static boolean moveUp(Context context, String name) {
        SharedPreferences prefs = getPrefs(context);
        List<PresetIndexItem> list = getIndex(prefs);
        int idx = -1;
        for (int i = 0; i < list.size(); i++) if (list.get(i).name.equals(name)) { idx = i; break; }
        if (idx <= 0) return false;
        PresetIndexItem it = list.remove(idx);
        list.add(idx - 1, it);
        saveIndex(prefs, list);
        return true;
    }

    public static boolean moveDown(Context context, String name) {
        SharedPreferences prefs = getPrefs(context);
        List<PresetIndexItem> list = getIndex(prefs);
        int idx = -1;
        for (int i = 0; i < list.size(); i++) if (list.get(i).name.equals(name)) { idx = i; break; }
        if (idx < 0 || idx >= list.size() - 1) return false;
        PresetIndexItem it = list.remove(idx);
        list.add(idx + 1, it);
        saveIndex(prefs, list);
        return true;
    }

    // ====================================================================
    // 6. EXPORT — Preset ke JSON string / File / Share
    // ====================================================================

    public static String exportToJson(Context context, String name) {
        OverlayPreset preset = load(context, name);
        if (preset == null) return null;
        return getGson().toJson(preset);
    }

    public static String exportAllToJson(Context context) {
        List<String> names = getAllNames(context);
        Gson gson = getGson();
        List<OverlayPreset> presetList = new ArrayList<>();

        for (String name : names) {
            OverlayPreset preset = load(context, name);
            if (preset != null) presetList.add(preset);
        }

        return gson.toJson(presetList);
    }

    public static boolean exportToFile(Activity activity, String filename) {
        try {
            String jsonContent = exportAllToJson(activity);

            if (android.os.Build.VERSION.SDK_INT >= 29) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, filename);
                values.put(MediaStore.Downloads.MIME_TYPE, "text/plain");

                Uri uri = activity.getContentResolver().insert(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI, values
                );

                if (uri != null) {
                    OutputStream os = activity.getContentResolver().openOutputStream(uri);
                    if (os != null) {
                        OutputStreamWriter writer = new OutputStreamWriter(os);
                        writer.write(jsonContent);
                        writer.flush();
                        writer.close();
                        return true;
                    }
                }
            } else {
                File dir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS
                );
                File file = new File(dir, filename);
                FileOutputStream fos = new FileOutputStream(file);
                OutputStreamWriter writer = new OutputStreamWriter(fos);
                writer.write(jsonContent);
                writer.flush();
                writer.close();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Share single preset via external share intent (writes preset to Downloads and fires share) */
    public static boolean sharePreset(Activity activity, String name) {
        try {
            String json = exportToJson(activity, name);
            if (json == null) return false;
            String filename = "ftxt_preset_" + System.currentTimeMillis() + ".txt";
            if (android.os.Build.VERSION.SDK_INT >= 29) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, filename);
                values.put(MediaStore.Downloads.MIME_TYPE, "text/plain");

                Uri uri = activity.getContentResolver().insert(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI, values
                );
                if (uri == null) return false;
                OutputStream os = activity.getContentResolver().openOutputStream(uri);
                if (os == null) return false;
                OutputStreamWriter writer = new OutputStreamWriter(os);
                writer.write(json);
                writer.flush();
                writer.close();

                // share via ACTION_SEND
                android.content.Intent share = new android.content.Intent(android.content.Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(android.content.Intent.EXTRA_STREAM, uri);
                share.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                activity.startActivity(android.content.Intent.createChooser(share, "Bagikan preset"));
                return true;
            } else {
                File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File file = new File(dir, filename);
                FileOutputStream fos = new FileOutputStream(file);
                OutputStreamWriter writer = new OutputStreamWriter(fos);
                writer.write(json);
                writer.flush();
                writer.close();

                Uri uri = Uri.fromFile(file);
                android.content.Intent share = new android.content.Intent(android.content.Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(android.content.Intent.EXTRA_STREAM, uri);
                activity.startActivity(android.content.Intent.createChooser(share, "Bagikan preset"));
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ====================================================================
    // 7. IMPORT — Preset dari JSON string / File
    // ====================================================================

    public static String importFromJson(Context context, String json, String name) {
        try {
            Gson gson = getGson();
            OverlayPreset preset = gson.fromJson(json, OverlayPreset.class);

            if (preset == null) return null;

            if (name == null || name.isEmpty()) {
                name = "Imported_" + System.currentTimeMillis();
            }

            save(context, name, preset);
            return name;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int importManyFromJson(Context context, String jsonArrayStr) {
        try {
            Gson gson = getGson();
            Type listType = new TypeToken<List<OverlayPreset>>() {}.getType();
            List<OverlayPreset> presetList = gson.fromJson(jsonArrayStr, listType);

            if (presetList == null || presetList.isEmpty()) return 0;

            int count = 0;
            for (OverlayPreset preset : presetList) {
                String name = "Imported_" + System.currentTimeMillis() + "_" + count;
                save(context, name, preset);
                count++;
            }

            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int importFromFile(Activity activity, Uri uri) {
        try {
            InputStream is = activity.getContentResolver().openInputStream(uri);
            if (is == null) return 0;

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            String jsonContent = sb.toString().trim();

            if (jsonContent.startsWith("[")) {
                return importManyFromJson(activity, jsonContent);
            } else {
                String name = importFromJson(activity, jsonContent, null);
                return (name != null) ? 1 : 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // ====================================================================
    // HISTORY — get and revert
    // ====================================================================

    public static List<OverlayPreset> getHistory(Context context, String name) {
        SharedPreferences prefs = getPrefs(context);
        List<PresetIndexItem> index = getIndex(prefs);
        PresetIndexItem item = findByName(index, name);
        if (item == null) return new ArrayList<>();
        String json = prefs.getString(KEY_HISTORY_PREFIX + item.uuid, null);
        if (json == null) return new ArrayList<>();
        Type listType = new TypeToken<List<PresetVersion>>() {}.getType();
        List<PresetVersion> hist = getGson().fromJson(json, listType);
        List<OverlayPreset> out = new ArrayList<>();
        if (hist != null) for (PresetVersion pv : hist) out.add(pv.preset);
        return out;
    }

    public static boolean revertToHistory(Context context, String name, int historyIndex) {
        SharedPreferences prefs = getPrefs(context);
        List<PresetIndexItem> index = getIndex(prefs);
        PresetIndexItem item = findByName(index, name);
        if (item == null) return false;
        String json = prefs.getString(KEY_HISTORY_PREFIX + item.uuid, null);
        if (json == null) return false;
        Type listType = new TypeToken<List<PresetVersion>>() {}.getType();
        List<PresetVersion> hist = getGson().fromJson(json, listType);
        if (hist == null || historyIndex < 0 || historyIndex >= hist.size()) return false;
        OverlayPreset preset = hist.get(historyIndex).preset;
        // overwrite current (push current to history will be handled by save())
        save(context, item.name, preset);
        return true;
    }

    // ====================================================================
    // SEARCH / INDEX UTILITIES
    // ====================================================================

    public static List<String> searchByNameOrTag(Context context, String query) {
        List<String> out = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) return getAllNames(context);
        String q = query.toLowerCase();
        List<PresetIndexItem> index = getIndex(getPrefs(context));
        for (PresetIndexItem it : index) {
            if (it.name.toLowerCase().contains(q)) {
                out.add(it.name);
                continue;
            }
            for (String t : it.tags) {
                if (t.toLowerCase().contains(q)) { out.add(it.name); break; }
            }
        }
        return out;
    }

    // ====================================================================
    // INTERFACE — Callback
    // ====================================================================

    /** Callback saat user memilih preset dari dialog daftar */
    public interface OnPresetSelectedListener {
        /**
         * Dipanggil saat item preset dipilih.
         *
         * @param name Nama preset yang dipilih
         */
        void onPresetSelected(String name);
    }

    // ====================================================================
    // ===== Deprecated: clipboard methods removed (clipboard-based sharing disabled)
    // ====================================================================

    /**
     * Merge helper untuk partial apply.
     * Jika flag true maka field dari `src` akan menggantikan `base`.
     */
    public static OverlayPreset mergePreset(OverlayPreset base, OverlayPreset src,
                                           boolean applyPosition, boolean applyAppearance,
                                           boolean applyBackground, boolean applyShadow,
                                           boolean applySize) {
        if (base == null) base = new OverlayPreset();
        if (src == null) return base;
        OverlayPreset out = new OverlayPreset(
                base.posX, base.posY, base.size, base.color,
                (base.shadow != null) ? base.shadow : null,
                base.bgEnabled, base.bgColor, base.bgPadding,
                base.bgOffsetX, base.bgOffsetY, base.bgMargin, base.bgRadius,
                base.orientation
        );

        if (applyPosition) {
            out.posX = src.posX;
            out.posY = src.posY;
            out.orientation = src.orientation;
        }
        if (applySize) {
            out.size = src.size;
        }
        if (applyAppearance) {
            out.color = src.color;
        }
        if (applyShadow && src.shadow != null) {
            out.shadow = src.shadow;
        }
        if (applyBackground) {
            out.bgEnabled = src.bgEnabled;
            out.bgColor = src.bgColor;
            out.bgPadding = src.bgPadding;
            out.bgOffsetX = src.bgOffsetX;
            out.bgOffsetY = src.bgOffsetY;
            out.bgMargin = src.bgMargin;
            out.bgRadius = src.bgRadius;
        }
        return out;
    }

    /**
     * Get thumbnail absolute path (filesDir) for a preset name, or null.
     */
    public static String getThumbnailPath(Context context, String name) {
        List<PresetIndexItem> index = getIndex(getPrefs(context));
        PresetIndexItem item = findByName(index, name);
        if (item == null || item.thumbnailPath == null) return null;
        File f = new File(context.getFilesDir(), item.thumbnailPath);
        return f.exists() ? f.getAbsolutePath() : null;
    }

    /**
     * Set tags for a preset (replace existing tags)
     */
    public static boolean setTags(Context context, String name, List<String> tags) {
        SharedPreferences prefs = getPrefs(context);
        List<PresetIndexItem> index = getIndex(prefs);
        PresetIndexItem item = findByName(index, name);
        if (item == null) return false;
        item.tags = (tags != null) ? new ArrayList<>(tags) : new ArrayList<>();
        item.updatedAt = System.currentTimeMillis();
        saveIndex(prefs, index);
        return true;
    }

    public static boolean setFavorite(Context context, String name, boolean fav) {
        SharedPreferences prefs = getPrefs(context);
        List<PresetIndexItem> index = getIndex(prefs);
        PresetIndexItem item = findByName(index, name);
        if (item == null) return false;
        item.favorite = fav;
        item.updatedAt = System.currentTimeMillis();
        saveIndex(prefs, index);
        return true;
    }

    /**
     * Return full index metadata (read-only copy)
     */
    public static List<java.util.Map<String, Object>> getIndexMetadata(Context context) {
        List<java.util.Map<String, Object>> out = new ArrayList<>();
        List<PresetIndexItem> idx = getIndex(getPrefs(context));
        for (PresetIndexItem it : idx) {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("name", it.name);
            m.put("uuid", it.uuid);
            m.put("createdAt", it.createdAt);
            m.put("updatedAt", it.updatedAt);
            m.put("tags", new ArrayList<>(it.tags));
            m.put("favorite", it.favorite);
            m.put("thumbnail", it.thumbnailPath);
            out.add(m);
        }
        return out;
    }

    // ====================================================================
    // DIALOG — Show Preset List (Load / Rename / Delete)
    // ====================================================================

    /**
     * Tampilkan dialog daftar preset dengan radio button untuk single select.
     * Klik item → panggil listener dengan nama preset.
     */
    public static void showLoadPresetDialog(Activity activity, String activePresetName,
                                            OnPresetSelectedListener listener) {
        List<String> names = getAllNames(activity);
        if (names.isEmpty()) {
            Toast.makeText(activity, "Belum ada preset tersimpan", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity,
                android.R.layout.simple_list_item_single_choice, names);
        ListView listView = new ListView(activity);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle("Pilih Preset")
                .setView(listView)
                .setNegativeButton("Batal", null)
                .show();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (listener != null) {
                listener.onPresetSelected(names.get(position));
            }
            dialog.dismiss();
        });
    }

    /**
     * Tampilkan dialog daftar preset (legacy method for backward compatibility).
     */
    public static void showPresetListDialog(Activity activity, String title,
                                            OnPresetSelectedListener listener) {
        List<String> names = getAllNames(activity);
        if (names.isEmpty()) {
            Toast.makeText(activity, "Belum ada preset tersimpan", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity,
                android.R.layout.simple_list_item_1, names);
        ListView listView = new ListView(activity);
        listView.setAdapter(adapter);

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle(title)
                .setView(listView)
                .setNegativeButton("Batal", null)
                .show();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (listener != null) {
                listener.onPresetSelected(names.get(position));
            }
            dialog.dismiss();
        });
    }

    /**
     * Tampilkan dialog konfirmasi hapus preset.
     */
    public static void showDeleteConfirmDialog(Activity activity, String name, Runnable callback) {
        new AlertDialog.Builder(activity)
                .setTitle("Hapus Preset")
                .setMessage("Yakin ingin menghapus preset \"" + name + "\"?")
                .setPositiveButton("Ya", (d, w) -> {
                    delete(activity, name);
                    Toast.makeText(activity, "Preset \"" + name + "\" dihapus", Toast.LENGTH_SHORT).show();
                    if (callback != null) callback.run();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

}