package exp.ftxt.shared.preset;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
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

/**
 * Manager untuk seluruh operasi CRUD preset overlay.
 *
 * Semua method static agar mudah dipanggil dari mana saja tanpa perlu instance.
 * Menggunakan GSON untuk serialize/deserialize JSON + SharedPreferences untuk storage.
 *
 * Struktur penyimpanan SharedPreferences:
 * - KEY_INDEX = Set<String> berisi daftar semua nama preset yang tersimpan
 * - "preset_{name}" = String JSON dari objek OverlayPreset
 */
public class PresetManager {

    // ====================================================================
    // KONSTANTA
    // ====================================================================

    /** Nama file SharedPreferences */
    private static final String PREFS_NAME = "ftxt_presets";

    /** Key penyimpanan daftar nama preset (format JSON array, urut terjamin) */
    private static final String KEY_ORDER = "preset_name_order";

    /** Key lama — StringSet (tanpa urutan). Untuk migrasi otomatis. */
    private static final String KEY_INDEX_OLD = "preset_name_list";

    /** Prefix key untuk data tiap preset */
    private static final String KEY_PREFIX = "preset_data_";

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
    // UTILITY — Daftar urut nama preset
    // ====================================================================

    /** Mengembalikan daftar nama preset yang terurut. */
    private static List<String> getNameOrder(SharedPreferences prefs) {
        String json = prefs.getString(KEY_ORDER, null);
        if (json != null) {
            try {
                Gson gson = getGson();
                return gson.fromJson(json, new TypeToken<List<String>>() {}.getType());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Fallback — coba migrasi dari format lama (Set<String>)
        return migrateFromOldFormat(prefs);
    }

    /** Menyimpan daftar urut nama preset. */
    private static void saveNameOrder(SharedPreferences prefs, List<String> list) {
        Gson gson = getGson();
        prefs.edit().putString(KEY_ORDER, gson.toJson(list)).apply();
    }

    /**
     * Migrasi dari format lama (StringSet) ke format baru (JSON array).
     * Dipanggil otomatis saat pertama kali membaca data lama.
     */
    private static List<String> migrateFromOldFormat(SharedPreferences prefs) {
        Set<String> oldSet = prefs.getStringSet(KEY_INDEX_OLD, null);
        if (oldSet == null || oldSet.isEmpty()) {
            return new ArrayList<>();
        }
        // Konversi Set → List (urutan tidak dijamin, tapi lebih baik dari kehilangan data)
        List<String> list = new ArrayList<>(oldSet);
        prefs.edit()
                .remove(KEY_INDEX_OLD)
                .putString(KEY_ORDER, getGson().toJson(list))
                .apply();
        return list;
    }

    // ====================================================================
    // 1. SIMPAN PRESET
    // ====================================================================

    /**
     * Menyimpan satu preset ke SharedPreferences.
     * Data di-serialize ke JSON menggunakan GSON.
     * Nama preset ditambahkan ke daftar urut (JSON array).
     *
     * @param context Context aplikasi
     * @param name    Nama preset (key identifier)
     * @param preset  Objek OverlayPreset yang akan disimpan
     */
    public static void save(Context context, String name, OverlayPreset preset) {
        SharedPreferences prefs = getPrefs(context);
        Gson gson = getGson();

        // Ambil daftar nama yang sudah tersimpan (terurut)
        List<String> nameList = getNameOrder(prefs);
        if (!nameList.contains(name)) {
            nameList.add(name);
        }

        // Serialize OverlayPreset → JSON string
        String json = gson.toJson(preset);

        // Simpan index dan data JSON ke SharedPreferences
        prefs.edit()
                .putString(KEY_ORDER, gson.toJson(nameList))
                .putString(KEY_PREFIX + name, json)
                .apply();
    }

    // ====================================================================
    // 2. MUAT PRESET
    // ====================================================================

    /**
     * Membaca satu preset dari SharedPreferences berdasarkan nama.
     * JSON string di-deserialize kembali menjadi objek OverlayPreset.
     *
     * @param context Context aplikasi
     * @param name    Nama preset yang akan dimuat
     * @return Objek OverlayPreset, atau null jika tidak ditemukan
     */
    public static OverlayPreset load(Context context, String name) {
        SharedPreferences prefs = getPrefs(context);
        Gson gson = getGson();

        // Baca JSON string dari SharedPreferences
        String json = prefs.getString(KEY_PREFIX + name, null);

        if (json == null) {
            return null; // Preset tidak ditemukan
        }

        // Deserialize JSON → objek OverlayPreset
        return gson.fromJson(json, OverlayPreset.class);
    }

    // ====================================================================
    // 3. RENAME PRESET
    // ====================================================================

    /**
     * Mengubah nama preset tanpa merusak data di dalamnya.
     * Proses: Load data lama → Simpan dengan nama baru → Hapus nama lama.
     *
     * @param context Context aplikasi
     * @param oldName Nama preset lama
     * @param newName Nama preset baru
     * @return true jika berhasil, false jika preset lama tidak ditemukan
     */
    public static boolean rename(Context context, String oldName, String newName) {
        OverlayPreset preset = load(context, oldName);
        if (preset == null) {
            return false;
        }

        SharedPreferences prefs = getPrefs(context);
        Gson gson = getGson();

        // Simpan data dengan key baru
        prefs.edit()
                .putString(KEY_PREFIX + newName, gson.toJson(preset))
                .remove(KEY_PREFIX + oldName)
                .apply();

        // Update nama di daftar urut (pertahankan posisi)
        List<String> nameList = getNameOrder(prefs);
        int idx = nameList.indexOf(oldName);
        if (idx >= 0) {
            nameList.set(idx, newName);
        } else {
            nameList.remove(oldName);
            nameList.add(newName);
        }
        saveNameOrder(prefs, nameList);

        return true;
    }

    // ====================================================================
    // 4. SELECT — Mendapatkan daftar semua nama preset
    // ====================================================================

    /**
     * Mengembalikan daftar semua nama preset yang tersimpan (terurut).
     *
     * @param context Context aplikasi
     * @return List<String> berisi nama-nama preset
     */
    public static List<String> getAllNames(Context context) {
        return getNameOrder(getPrefs(context));
    }

    // ====================================================================
    // 5. HAPUS PRESET
    // ====================================================================

    /**
     * Menghapus satu preset dari SharedPreferences.
     * Menghapus dari daftar urut dan menghapus data JSON-nya.
     *
     * @param context Context aplikasi
     * @param name    Nama preset yang akan dihapus
     */
    public static void delete(Context context, String name) {
        SharedPreferences prefs = getPrefs(context);

        // Ambil daftar nama yang tersimpan
        List<String> nameList = getNameOrder(prefs);

        // Hapus nama dari index
        nameList.remove(name);

        // Hapus index dan data JSON
        prefs.edit()
                .putString(KEY_ORDER, getGson().toJson(nameList))
                .remove(KEY_PREFIX + name)
                .apply();
    }

    /**
     * Menghapus beberapa preset sekaligus.
     *
     * @param context Context aplikasi
     * @param names   Daftar nama preset yang akan dihapus
     */
    public static void deleteMultiple(Context context, List<String> names) {
        SharedPreferences prefs = getPrefs(context);
        List<String> nameList = getNameOrder(prefs);
        SharedPreferences.Editor editor = prefs.edit();

        for (String name : names) {
            nameList.remove(name);
            editor.remove(KEY_PREFIX + name);
        }

        editor.putString(KEY_ORDER, getGson().toJson(nameList));
        editor.apply();
    }

    /**
     * Menghapus semua preset yang tersimpan.
     * Membersihkan index dan seluruh data preset.
     *
     * @param context Context aplikasi
     */
    public static void deleteAll(Context context) {
        SharedPreferences prefs = getPrefs(context);

        // Ambil daftar semua nama untuk dihapus satu per satu
        List<String> nameList = getNameOrder(prefs);

        SharedPreferences.Editor editor = prefs.edit();

        // Hapus data JSON tiap preset
        for (String name : nameList) {
            editor.remove(KEY_PREFIX + name);
        }

        // Kosongkan index
        editor.remove(KEY_ORDER);
        editor.apply();
    }

    // ====================================================================
    // 5b. REORDER — Pindah Atas / Bawah
    // ====================================================================

    /**
     * Memindahkan satu preset satu langkah ke atas dalam daftar.
     *
     * @param context Context aplikasi
     * @param name    Nama preset yang akan dipindah
     * @return true jika berhasil dipindah
     */
    public static boolean moveUp(Context context, String name) {
        SharedPreferences prefs = getPrefs(context);
        List<String> list = getNameOrder(prefs);
        int idx = list.indexOf(name);
        if (idx <= 0) return false;
        list.remove(idx);
        list.add(idx - 1, name);
        saveNameOrder(prefs, list);
        return true;
    }

    /**
     * Memindahkan satu preset satu langkah ke bawah dalam daftar.
     *
     * @param context Context aplikasi
     * @param name    Nama preset yang akan dipindah
     * @return true jika berhasil dipindah
     */
    public static boolean moveDown(Context context, String name) {
        SharedPreferences prefs = getPrefs(context);
        List<String> list = getNameOrder(prefs);
        int idx = list.indexOf(name);
        if (idx < 0 || idx >= list.size() - 1) return false;
        list.remove(idx);
        list.add(idx + 1, name);
        saveNameOrder(prefs, list);
        return true;
    }

    // ====================================================================
    // 6. EXPORT — Preset ke JSON string / File
    // ====================================================================

    /**
     * Mengekspor SATU preset ke format JSON string (untuk clipboard atau dibagikan).
     *
     * @param context Context aplikasi
     * @param name    Nama preset yang akan diekspor
     * @return String JSON dari preset tersebut, atau null jika gagal
     */
    public static String exportToJson(Context context, String name) {
        OverlayPreset preset = load(context, name);
        if (preset == null) return null;

        Gson gson = getGson();
        return gson.toJson(preset);
    }

    /**
     * Mengekspor SEMUA preset ke dalam satu JSON array string.
     *
     * @param context Context aplikasi
     * @return String JSON array dari seluruh preset, atau "[]" jika kosong
     */
    public static String exportAllToJson(Context context) {
        List<String> names = getAllNames(context);
        Gson gson = getGson();
        List<OverlayPreset> presetList = new ArrayList<>();

        for (String name : names) {
            OverlayPreset preset = load(context, name);
            if (preset != null) {
                presetList.add(preset);
            }
        }

        return gson.toJson(presetList);
    }

    /**
     * Mengekspor SEMUA preset ke file teks (.txt) di penyimpanan eksternal (Downloads).
     * Berguna untuk berbagi preset antar perangkat.
     *
     * @param activity Activity yang memanggil (untuk intent)
     * @param filename Nama file tujuan (misal "ftxt_presets.txt")
     * @return true jika berhasil, false jika gagal
     */
    public static boolean exportToFile(Activity activity, String filename) {
        try {
            // Buat konten JSON dari semua preset
            String jsonContent = exportAllToJson(activity);

            // Untuk Android 10+, gunakan MediaStore agar file bisa diakses publik
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
                // Android 9 ke bawah — simpan ke direktori Downloads
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

    // ====================================================================
    // 7. IMPORT — Preset dari JSON string / File
    // ====================================================================

    /**
     * Mengimpor SATU preset dari JSON string.
     * Berguna saat menerima preset dari clipboard atau pesan teks.
     *
     * @param context Context aplikasi
     * @param json    String JSON dari satu objek OverlayPreset
     * @param name    Nama preset untuk disimpan (jika null, akan generate otomatis)
     * @return Nama preset yang disimpan, atau null jika gagal
     */
    public static String importFromJson(Context context, String json, String name) {
        try {
            Gson gson = getGson();
            OverlayPreset preset = gson.fromJson(json, OverlayPreset.class);

            if (preset == null) return null;

            // Jika nama tidak disediakan, buat nama otomatis
            if (name == null || name.isEmpty()) {
                name = "Imported_" + System.currentTimeMillis();
            }

            // Simpan ke SharedPreferences
            save(context, name, preset);
            return name;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Mengimpor BANYAK preset dari JSON array string.
     *
     * @param context Context aplikasi
     * @param jsonArrayStr String JSON array dari List<OverlayPreset>
     * @return Jumlah preset yang berhasil diimpor
     */
    public static int importManyFromJson(Context context, String jsonArrayStr) {
        try {
            Gson gson = getGson();
            Type listType = new TypeToken<List<OverlayPreset>>() {}.getType();
            List<OverlayPreset> presetList = gson.fromJson(jsonArrayStr, listType);

            if (presetList == null || presetList.isEmpty()) return 0;

            int count = 0;
            for (OverlayPreset preset : presetList) {
                // Generate nama unik berdasarkan timestamp
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

    /**
     * Mengimpor preset dari file teks (URI).
     *
     * @param activity Activity yang memanggil
     * @param uri      URI file yang dipilih (dari file picker)
     * @return Jumlah preset yang berhasil diimpor
     */
    public static int importFromFile(Activity activity, Uri uri) {
        try {
            // Baca isi file sebagai string
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

            // Coba sebagai JSON array (banyak preset)
            if (jsonContent.startsWith("[")) {
                return importManyFromJson(activity, jsonContent);
            } else {
                // Coba sebagai single preset
                String name = importFromJson(activity, jsonContent, null);
                return (name != null) ? 1 : 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // ====================================================================
    // DIALOG — Konfirmasi Hapus (single)
    // ====================================================================

    /** Dimensi density untuk padding/kalkulasi UI */
    private static float dp(Activity a) {
        return a.getResources().getDisplayMetrics().density;
    }

    /**
     * Menampilkan dialog konfirmasi sebelum menghapus preset.
     *
     * @param activity Activity yang memanggil
     * @param name     Nama preset yang akan dihapus
     * @param callback Callback setelah berhasil dihapus (bisa null)
     */
    public static void showDeleteConfirmDialog(
            Activity activity, String name, Runnable callback
    ) {
        new AlertDialog.Builder(activity)
                .setTitle("Hapus Preset")
                .setMessage("Yakin ingin menghapus preset \"" + name + "\"?")
                .setPositiveButton("Ya", (d, w) -> {
                    delete(activity, name);
                    Toast.makeText(activity,
                            "Preset \"" + name + "\" dihapus",
                            Toast.LENGTH_SHORT).show();
                    if (callback != null) callback.run();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    // ====================================================================
    // DIALOG — Pilih / Muat Preset (Enhanced)
    // ====================================================================

    /**
     * Menampilkan dialog daftar preset dengan radio button (single select),
     * tombol ⋮ per item, long-press context menu, dan mode multi-select.
     *
     * Mode normal: ● / ○ radio button, klik item → panggil listener.
     * Tombol ⋮ → PopupMenu: Rename, Hapus, Pindah ke Atas, Pindah ke Bawah.
     * Long-press item → PopupMenu yang sama.
     * Mode multi-select: ☑ / ☐ checkbox, count di header, [Hapus] di footer.
     *
     * @param activity         Activity yang memanggil
     * @param activePresetName Nama preset yang sedang aktif (untuk indikator ●), atau null
     * @param listener         Callback saat item dipilih (single select)
     */
    public static void showLoadPresetDialog(
            Activity activity, String activePresetName, OnPresetSelectedListener listener
    ) {
        List<String> names = getAllNames(activity);
        if (names.isEmpty()) {
            Toast.makeText(activity, "Belum ada preset tersimpan", Toast.LENGTH_SHORT).show();
            return;
        }

        final PresetListAdapter adapter = new PresetListAdapter(activity, names, activePresetName);
        ListView listView = new ListView(activity);
        listView.setAdapter(adapter);

        // Header mode multi-select
        TextView headerCount = new TextView(activity);
        headerCount.setPadding(24, 16, 24, 16);
        headerCount.setTextSize(14);
        headerCount.setTypeface(null, Typeface.BOLD);
        headerCount.setTextColor(Color.WHITE);
        headerCount.setVisibility(View.GONE);

        // Footer — tombol Hapus (mode multi-select)
        Button btnDeleteSelected = new Button(activity);
        btnDeleteSelected.setText("Hapus");
        btnDeleteSelected.setVisibility(View.GONE);
        btnDeleteSelected.setOnClickListener(v -> {
            List<String> selected = adapter.getSelectedNames();
            if (selected.isEmpty()) return;
            String msg = "Hapus " + selected.size() + " preset terpilih?";
            new AlertDialog.Builder(activity)
                    .setTitle("Hapus Preset")
                    .setMessage(msg)
                    .setPositiveButton("Ya", (d, w) -> {
                        deleteMultiple(activity, selected);
                        Toast.makeText(activity,
                                selected.size() + " preset dihapus",
                                Toast.LENGTH_SHORT).show();
                        adapter.removeItems(selected);
                        headerCount.setVisibility(View.GONE);
                        btnDeleteSelected.setVisibility(View.GONE);
                        adapter.setMultiSelectMode(false);
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });

        LinearLayout footer = new LinearLayout(activity);
        footer.setOrientation(LinearLayout.HORIZONTAL);
        footer.setPadding(16, 8, 16, 8);
        footer.addView(btnDeleteSelected, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout container = new LinearLayout(activity);
        container.setOrientation(LinearLayout.VERTICAL);
        container.addView(headerCount);
        container.addView(listView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        container.addView(footer);

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle("PILIH PRESET")
                .setView(container)
                .setNegativeButton("Batal", null)
                .show();

        adapter.setupListeners(listView, dialog, headerCount, btnDeleteSelected, listener);
    }

    /**
     * Adapter untuk daftar preset dengan radio button, ⋮, dan multi-select.
     */
    private static class PresetListAdapter extends BaseAdapter {
        private final Activity activity;
        private final List<String> names;
        private boolean multiSelectMode = false;
        private boolean[] checked;
        private int selectedRadioIndex = -1;
        private OnPresetSelectedListener listener;
        private AlertDialog dialog;
        private TextView headerCount;
        private Button btnDelete;

        PresetListAdapter(Activity activity, List<String> names, String activePresetName) {
            this.activity = activity;
            this.names = new ArrayList<>(names);
            this.checked = new boolean[names.size()];
            if (activePresetName != null) {
                selectedRadioIndex = names.indexOf(activePresetName);
            }
        }

        void setMultiSelectMode(boolean mode) {
            multiSelectMode = mode;
            if (!mode) {
                for (int i = 0; i < checked.length; i++) checked[i] = false;
            }
            notifyDataSetChanged();
        }

        List<String> getSelectedNames() {
            List<String> sel = new ArrayList<>();
            for (int i = 0; i < checked.length; i++) {
                if (checked[i]) sel.add(names.get(i));
            }
            return sel;
        }

        void rebuildCheckedArray() {
            if (checked.length == names.size()) return;
            boolean[] tmp = new boolean[names.size()];
            System.arraycopy(checked, 0, tmp, 0, Math.min(checked.length, names.size()));
            checked = tmp;
        }

        void removeItems(List<String> toRemove) {
            names.removeAll(toRemove);
            rebuildCheckedArray();
            notifyDataSetChanged();
        }

        void setupListeners(ListView listView, AlertDialog dialog,
                            TextView headerCount, Button btnDelete,
                            OnPresetSelectedListener listener) {
            this.dialog = dialog;
            this.headerCount = headerCount;
            this.btnDelete = btnDelete;
            this.listener = listener;

            listView.setOnItemClickListener((parent, view, position, id) -> {
                if (multiSelectMode) {
                    checked[position] = !checked[position];
                    updateMultiSelectHeader();
                    notifyDataSetChanged();
                } else {
                    selectedRadioIndex = position;
                    notifyDataSetChanged();
                    if (listener != null) {
                        listener.onPresetSelected(names.get(position));
                    }
                    dialog.dismiss();
                }
            });
        }

        void updateMultiSelectHeader() {
            int count = 0;
            for (boolean b : checked) if (b) count++;
            if (count > 0) {
                headerCount.setText(count + " DIPILIH");
                headerCount.setVisibility(View.VISIBLE);
                btnDelete.setVisibility(View.VISIBLE);
            } else {
                headerCount.setVisibility(View.GONE);
                btnDelete.setVisibility(View.GONE);
            }
        }

        void showItemMenu(View anchor, String name, int position) {
            PopupMenu popup = new PopupMenu(activity, anchor);
            popup.getMenu().add(0, 1, 0, "Rename");
            popup.getMenu().add(0, 2, 0, "Hapus");
            popup.getMenu().add(0, 3, 0, "Pindah ke Atas");
            popup.getMenu().add(0, 4, 0, "Pindah ke Bawah");
            popup.getMenu().add(0, 5, 0, "Pilih Banyak");

            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 1:
                        showRenameDialog(name, (newName) -> {
                            int idx = names.indexOf(name);
                            if (idx >= 0 && newName != null) {
                                names.set(idx, newName);
                            }
                            notifyDataSetChanged();
                        });
                        return true;
                    case 2:
                        showDeleteConfirmDialog(activity, name, () -> {
                            names.remove(name);
                            rebuildCheckedArray();
                            if (selectedRadioIndex >= names.size()) {
                                selectedRadioIndex = names.size() - 1;
                            }
                            notifyDataSetChanged();
                            if (names.isEmpty()) dialog.dismiss();
                        });
                        return true;
                    case 3:
                        if (moveUp(activity, name)) {
                            refreshNames();
                            notifyDataSetChanged();
                        }
                        return true;
                    case 4:
                        if (moveDown(activity, name)) {
                            refreshNames();
                            notifyDataSetChanged();
                        }
                        return true;
                    case 5:
                        multiSelectMode = true;
                        selectedRadioIndex = -1;
                        notifyDataSetChanged();
                        return true;
                }
                return false;
            });

            popup.show();
        }

        void showRenameDialog(String oldName, RenameCallback callback) {
            EditText input = new EditText(activity);
            input.setText(oldName);
            input.setSelectAllOnFocus(true);
            input.setInputType(InputType.TYPE_CLASS_TEXT);

            new AlertDialog.Builder(activity)
                    .setTitle("Ganti Nama Preset")
                    .setView(input)
                    .setPositiveButton("Simpan", (d, w) -> {
                        String newName = input.getText().toString().trim();
                        if (newName.isEmpty()) {
                            Toast.makeText(activity, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (rename(activity, oldName, newName)) {
                            Toast.makeText(activity,
                                    "Diganti: \"" + oldName + "\" → \"" + newName + "\"",
                                    Toast.LENGTH_SHORT).show();
                            if (callback != null) callback.onRenamed(newName);
                        }
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        }

        private interface RenameCallback {
            void onRenamed(String newName);
        }

        void refreshNames() {
            names.clear();
            names.addAll(getNameOrder(getPrefs(activity)));
        }

        @Override
        public int getCount() {
            return names.size();
        }

        @Override
        public String getItem(int position) {
            return names.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                LinearLayout row = new LinearLayout(activity);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                int p = (int)(16 * dp(activity));
                row.setPadding(p, (int)(8 * dp(activity)), p, (int)(8 * dp(activity)));

                RadioButton radio = new RadioButton(activity);
                radio.setFocusable(false);
                radio.setClickable(false);

                CheckBox check = new CheckBox(activity);
                check.setFocusable(false);
                check.setClickable(false);
                check.setVisibility(View.GONE);

                TextView tvName = new TextView(activity);
                tvName.setLayoutParams(new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                tvName.setTextSize(16);
                tvName.setTextColor(Color.WHITE);
                tvName.setPadding((int)(12 * dp(activity)), 0, (int)(12 * dp(activity)), 0);

                Button btnMenu = new Button(activity);
                btnMenu.setText("\u22EE");
                btnMenu.setTextSize(20);
                btnMenu.setTextColor(Color.LTGRAY);
                btnMenu.setBackgroundColor(Color.TRANSPARENT);
                btnMenu.setMinimumWidth((int)(64 * dp(activity)));
                btnMenu.setMinimumHeight((int)(40 * dp(activity)));
                btnMenu.setFocusable(false);
                btnMenu.setClickable(true);
                btnMenu.setAllCaps(false);

                row.addView(radio);
                row.addView(check);
                row.addView(tvName);
                row.addView(btnMenu);

                convertView = row;
                holder = new ViewHolder(radio, check, tvName, btnMenu);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            String name = names.get(position);
            holder.name.setText(name);

            if (multiSelectMode) {
                holder.radio.setVisibility(View.GONE);
                holder.check.setVisibility(View.VISIBLE);
                holder.check.setChecked(checked[position]);
            } else {
                holder.check.setVisibility(View.GONE);
                holder.radio.setVisibility(View.VISIBLE);
                holder.radio.setChecked(position == selectedRadioIndex);
            }

            holder.btnMenu.setOnClickListener(v -> showItemMenu(holder.btnMenu, name, position));

            return convertView;
        }

        private static class ViewHolder {
            final RadioButton radio;
            final CheckBox check;
            final TextView name;
            final Button btnMenu;

            ViewHolder(RadioButton radio, CheckBox check, TextView name, Button btnMenu) {
                this.radio = radio;
                this.check = check;
                this.name = name;
                this.btnMenu = btnMenu;
            }
        }
    }

    /**
     * Versi lama showPresetListDialog — memanggil showLoadPresetDialog jika title "Muat Preset",
     * atau fallback ke dialog sederhana untuk kasus lain.
     * Dipertahankan untuk backward compatibility.
     */
    public static void showPresetListDialog(
            Activity activity, String title, OnPresetSelectedListener listener
    ) {
        if ("Muat Preset".equals(title)) {
            showLoadPresetDialog(activity, null, listener);
            return;
        }
        List<String> names = getAllNames(activity);
        if (names.isEmpty()) {
            Toast.makeText(activity, "Belum ada preset tersimpan", Toast.LENGTH_SHORT).show();
            return;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                activity, android.R.layout.simple_list_item_1, names);
        ListView listView = new ListView(activity);
        listView.setAdapter(adapter);
        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setView(listView)
                .setNegativeButton("Batal", null)
                .show();
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (listener != null) listener.onPresetSelected(names.get(position));
        });
    }

    // ====================================================================
    // DIALOG — Ekspor/Impor via Clipboard
    // ====================================================================

    /**
     * Menyalin JSON semua preset ke clipboard sistem.
     *
     * @param activity Activity yang memanggil
     */
    public static void exportToClipboard(Activity activity) {
        String json = exportAllToJson(activity);

        if (json == null || json.equals("[]") || json.equals("")) {
            Toast.makeText(activity, "Tidak ada preset untuk diekspor", Toast.LENGTH_SHORT).show();
            return;
        }

        ClipboardManager clipboard = (ClipboardManager)
                activity.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("FTxT Presets", json);
        clipboard.setPrimaryClip(clip);

        int count = getAllNames(activity).size();
        Toast.makeText(activity,
                count + " preset disalin ke clipboard",
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Membaca JSON dari clipboard dan mengimpornya sebagai preset.
     *
     * @param activity Activity yang memanggil
     */
    public static void importFromClipboard(Activity activity) {
        ClipboardManager clipboard = (ClipboardManager)
                activity.getSystemService(Context.CLIPBOARD_SERVICE);

        if (!clipboard.hasPrimaryClip()) {
            Toast.makeText(activity, "Clipboard kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
        if (item.getText() == null) {
            Toast.makeText(activity, "Clipboard tidak berisi teks", Toast.LENGTH_SHORT).show();
            return;
        }

        String json = item.getText().toString();
        int count;

        if (json.startsWith("[")) {
            count = importManyFromJson(activity, json);
        } else {
            String name = importFromJson(activity, json, null);
            count = (name != null) ? 1 : 0;
        }

        if (count > 0) {
            Toast.makeText(activity,
                    "Berhasil impor " + count + " preset dari clipboard",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(activity,
                    "Gagal impor: format JSON tidak valid",
                    Toast.LENGTH_SHORT).show();
        }
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
}
