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

public class PresetManager {

    private static final String PREFS_NAME = "ftxt_presets";

    private static final String KEY_INDEX = "preset_index_v2";

    private static final String KEY_INDEX_OLD = "preset_name_list";

    private static final String KEY_PREFIX = "preset_data_";

    private static final String KEY_HISTORY_PREFIX = "preset_history_";

    private static class PresetIndexItem {
        String uuid;
        String name;
        long createdAt;
        long updatedAt;
        List<String> tags;
        boolean favorite;
        int color;
        String thumbnailPath;

        PresetIndexItem() {
            tags = new ArrayList<>();
        }
    }

    private static class PresetVersion {
        long ts;
        OverlayPreset preset;

        PresetVersion() {}

        PresetVersion(long ts, OverlayPreset preset) {
            this.ts = ts;
            this.preset = preset;
        }
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private static Gson getGson() {
        return new GsonBuilder().setPrettyPrinting().create();
    }

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
        return migrateFromOldFormat(prefs);
    }

    private static void saveIndex(SharedPreferences prefs, List<PresetIndexItem> list) {
        Gson gson = getGson();
        prefs.edit().putString(KEY_INDEX, gson.toJson(list)).apply();
    }

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

                String oldJson = prefs.getString(KEY_PREFIX + name, null);
                if (oldJson != null) {
                    prefs.edit().putString(KEY_PREFIX + it.uuid, oldJson).remove(KEY_PREFIX + name).apply();
                }
            }
            prefs.edit().remove(KEY_INDEX_OLD).putString(KEY_INDEX, getGson().toJson(list)).apply();
        }
        return list;
    }

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

    public static void save(Context context, String name, OverlayPreset preset) {
        SharedPreferences prefs = getPrefs(context);
        Gson gson = getGson();

        List<PresetIndexItem> index = getIndex(prefs);
        long now = System.currentTimeMillis();

        PresetIndexItem item = findByName(index, name);
        boolean isNew = false;
        if (item == null) {
            item = new PresetIndexItem();
            item.uuid = UUID.randomUUID().toString();
            item.name = name;
            item.createdAt = now;
            item.updatedAt = now;
            index.add(0, item);
            isNew = true;
        } else {
            index.remove(item);
            index.add(0, item);
            item.updatedAt = now;
            try {
                String existingJson = prefs.getString(KEY_PREFIX + item.uuid, null);
                if (existingJson != null) {
                    Type listType = new TypeToken<List<PresetVersion>>() {}.getType();
                    List<PresetVersion> history = gson.fromJson(prefs.getString(KEY_HISTORY_PREFIX + item.uuid, null), listType);
                    if (history == null) history = new ArrayList<>();
                    OverlayPreset prev = gson.fromJson(existingJson, OverlayPreset.class);
                    history.add(new PresetVersion(System.currentTimeMillis(), prev));
                    if (history.size() > 10) history.remove(0);
                    prefs.edit().putString(KEY_HISTORY_PREFIX + item.uuid, gson.toJson(history)).apply();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String json = gson.toJson(preset);
        prefs.edit().putString(KEY_PREFIX + item.uuid, json).apply();

        item.color = preset.color;

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

        saveIndex(prefs, index);

        if (!isNew) {
            prefs.edit().remove(KEY_PREFIX + name).apply();
        }
    }

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

        String oldJson = prefs.getString(KEY_PREFIX + name, null);
        if (oldJson != null) {
            OverlayPreset p = gson.fromJson(oldJson, OverlayPreset.class);
            save(context, name, p);
            return p;
        }

        return null;
    }

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

    public static List<String> getAllNames(Context context) {
        List<String> out = new ArrayList<>();
        List<PresetIndexItem> index = getIndex(getPrefs(context));
        for (PresetIndexItem it : index) out.add(it.name);
        return out;
    }

    public static void delete(Context context, String name) {
        SharedPreferences prefs = getPrefs(context);
        List<PresetIndexItem> index = getIndex(prefs);
        PresetIndexItem item = findByName(index, name);
        if (item == null) return;

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
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/FunText");

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
                        Toast.makeText(activity, "Diekspor ke Downloads/FunText/" + filename, Toast.LENGTH_SHORT).show();
                        return true;
                    }
                }
            } else {
                File dir = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS), "FunText"
                );
                dir.mkdirs();
                File file = new File(dir, filename);
                FileOutputStream fos = new FileOutputStream(file);
                OutputStreamWriter writer = new OutputStreamWriter(fos);
                writer.write(jsonContent);
                writer.flush();
                writer.close();
                Toast.makeText(activity, "Diekspor ke Downloads/FunText/" + filename, Toast.LENGTH_SHORT).show();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

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
        save(context, item.name, preset);
        return true;
    }

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

    public interface OnPresetSelectedListener {
        void onPresetSelected(String name);
    }

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

    public static String getThumbnailPath(Context context, String name) {
        List<PresetIndexItem> index = getIndex(getPrefs(context));
        PresetIndexItem item = findByName(index, name);
        if (item == null || item.thumbnailPath == null) return null;
        File f = new File(context.getFilesDir(), item.thumbnailPath);
        return f.exists() ? f.getAbsolutePath() : null;
    }

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

    public static boolean reorder(Context context, String name, int toIndex) {
        SharedPreferences prefs = getPrefs(context);
        List<PresetIndexItem> list = getIndex(prefs);
        int fromIndex = -1;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).name.equals(name)) { fromIndex = i; break; }
        }
        if (fromIndex < 0 || toIndex < 0 || toIndex >= list.size() || fromIndex == toIndex) return false;
        PresetIndexItem it = list.remove(fromIndex);
        list.add(toIndex, it);
        saveIndex(prefs, list);
        return true;
    }

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
            m.put("color", it.color);
            m.put("thumbnail", it.thumbnailPath);
            out.add(m);
        }
        return out;
    }

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
