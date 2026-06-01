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
import exp.ftxt.shared.ui.ShadowConfig;

/**
 * CONTOH IMPLEMENTASI — Cara menggunakan PresetManager + OverlayPreset
 * di Activity utama.
 *
 * Method-method di bawah ini bisa ditaruh langsung di MainActivity.java
 * dan dipanggil dari OnClickListener tombol masing-masing.
 *
 * === Panduan Integrasi ===
 *
 * 1. Di layout activity_main.xml, tambahkan tombol:
 *    - btnPresetSave     → Simpan preset
 *    - btnPresetLoad     → Muat preset
 *    - btnPresetRename   → Ganti nama preset
 *    - btnPresetDelete   → Hapus preset
 *    - btnPresetExport   → Ekspor ke clipboard
 *    - btnPresetImport   → Impor dari clipboard
 *    - btnPresetExportFile → Ekspor ke file
 *    - btnPresetImportFile → Impor dari file
 *
 * 2. Di MainActivity.java, binding tombol:
 *    findViewById(R.id.btnPresetSave).setOnClickListener(v -> showSavePresetDialog());
 *    findViewById(R.id.btnPresetLoad).setOnClickListener(v -> showLoadPresetDialog());
 *    // ... dan seterusnya
 *
 * 3. Untuk import dari file, butuh ActivityResultLauncher:
 *    ActivityResultLauncher<String> filePicker =
 *         registerForActivityResult(new OpenDocument(), uri -> {
 *             int count = PresetManager.importFromFile(MainActivity.this, uri);
 *             Toast.makeText(this, "Impor " + count + " preset", Toast.LENGTH_SHORT).show();
 *         });
 *
 *    Lalu panggil: filePicker.launch(new String[]{"text/plain", "*\\/*"});
 */
public class PresetExampleActivity {

    // ====================================================================
    // 1. SIMPAN PRESET — tombol "Simpan Preset"
    // ====================================================================

    /**
     * Menampilkan dialog input nama, lalu menyimpan konfigurasi overlay
     * saat ini sebagai preset baru.
     *
     * Data yang diambil dari config class (TextConfig, ShadowConfig, dll):
     * - posX, posY        → Posisi overlay
     * - size              → Ukuran teks
     * - color             → Warna teks
     * - shadow            → Konfigurasi shadow
     * - bgEnabled..bgRadius → Konfigurasi background
     * - orientation       → Mode potret/lanskap
     *
     * Panggil dari: buttonSave.setOnClickListener(v -> showSavePresetDialog());
     */
    public static void showSavePresetDialog(Activity activity) {
        // Buat EditText untuk input nama preset
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

                    // Cek apakah nama sudah digunakan
                    OverlayPreset existing = PresetManager.load(activity, name);
                    if (existing != null) {
                        // Konfirmasi timpa
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

    /**
     * Eksekusi penyimpanan preset ke SharedPreferences via PresetManager.
     * Mengambil data dari TextConfig dan ShadowConfig sebagai contoh.
     */
    private static void doSavePreset(Activity activity, String name) {
        // Buat objek OverlayPreset dari konfigurasi saat ini
        OverlayPreset preset = new OverlayPreset();

        // 1. Posisi & Ukuran — ambil dari TextConfig (contoh)
        preset.posX = TextConfig.posX;
        preset.posY = TextConfig.posY;
        preset.size = TextConfig.size;

        // 2. Tampilan Teks — warna overlay
        preset.color = TextConfig.color;

        // 3. Shadow — copy dari config saat ini
        ShadowConfig sc = TextConfig.shadow;
        preset.shadow = new ShadowConfig(sc.enabled, sc.color, sc.blur, sc.offsetX, sc.offsetY);

        // 4. Background
        preset.bgEnabled = TextConfig.bgEnabled;
        preset.bgColor = TextConfig.bgColor;
        preset.bgPadding = TextConfig.bgPadding;
        preset.bgOffsetX = TextConfig.bgOffsetX;
        preset.bgOffsetY = TextConfig.bgOffsetY;
        preset.bgMargin = TextConfig.bgMargin;
        preset.bgRadius = TextConfig.bgRadius;

        // 5. Orientasi — deteksi orientasi perangkat
        int orientation = activity.getResources().getConfiguration().orientation;
        preset.orientation = (orientation == 2) ? "landscape" : "portrait";

        // Simpan via PresetManager
        PresetManager.save(activity, name, preset);

        Toast.makeText(activity,
                "Preset \"" + name + "\" tersimpan",
                Toast.LENGTH_SHORT).show();
    }

    // ====================================================================
    // 2. MUAT PRESET — tombol "Muat Preset"
    // ====================================================================

    /**
     * Menampilkan daftar preset untuk dipilih, lalu menerapkannya
     * ke overlay secara real-time.
     *
     * Panggil dari: buttonLoad.setOnClickListener(v -> showLoadPresetDialog());
     */
    public static void showLoadPresetDialog(Activity activity) {
        // Tampilkan dialog daftar preset yang tersedia
        PresetManager.showPresetListDialog(activity, "Muat Preset", name -> {
            // Load data preset dari SharedPreferences
            OverlayPreset preset = PresetManager.load(activity, name);

            if (preset == null) {
                Toast.makeText(activity, "Gagal memuat preset",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Terapkan ke konfigurasi overlay (contoh: TextConfig)
            applyPresetToConfig(activity, preset);

            // Sinkronisasi ke overlay yang sedang berjalan
            syncPresetToOverlay();

            Toast.makeText(activity,
                    "Preset \"" + name + "\" diterapkan",
                    Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Menerapkan data OverlayPreset ke dalam TextConfig (dan config class lain).
     */
    private static void applyPresetToConfig(Activity activity, OverlayPreset preset) {
        // Posisi
        TextConfig.posX = preset.posX;
        TextConfig.posY = preset.posY;

        // Ukuran
        TextConfig.size = preset.size;

        // Warna teks
        TextConfig.color = preset.color;

        // Shadow
        if (preset.shadow != null) {
            TextConfig.shadow.enabled = preset.shadow.enabled;
            TextConfig.shadow.color = preset.shadow.color;
            TextConfig.shadow.blur = preset.shadow.blur;
            TextConfig.shadow.offsetX = preset.shadow.offsetX;
            TextConfig.shadow.offsetY = preset.shadow.offsetY;
        }

        // Background
        TextConfig.bgEnabled = preset.bgEnabled;
        TextConfig.bgColor = preset.bgColor;
        TextConfig.bgPadding = preset.bgPadding;
        TextConfig.bgOffsetX = preset.bgOffsetX;
        TextConfig.bgOffsetY = preset.bgOffsetY;
        TextConfig.bgMargin = preset.bgMargin;
        TextConfig.bgRadius = preset.bgRadius;
    }

    /**
     * Menyinkronkan perubahan konfigurasi ke overlay yang sedang berjalan.
     * Memanggil static delegate method di FloatingService.
     */
    private static void syncPresetToOverlay() {
        FloatingService.updateTextPositionStatic();
        FloatingService.updateTextSizeStatic();
        FloatingService.updateTextColorStatic();
        FloatingService.updateShadowStatic();
        FloatingService.updateTextBackgroundStatic();
    }

    // ====================================================================
    // 3. RENAME PRESET — tombol "Ganti Nama Preset"
    // ====================================================================

    /**
     * Menampilkan dialog daftar preset untuk dipilih, lalu dialog
     * input nama baru.
     *
     * Panggil dari: buttonRename.setOnClickListener(v -> showRenamePresetDialog());
     */
    public static void showRenamePresetDialog(Activity activity) {
        PresetManager.showPresetListDialog(activity, "Pilih Preset", oldName -> {
            // Tampilkan dialog input nama baru
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

    // ====================================================================
    // 4. HAPUS PRESET — tombol "Hapus Preset"
    // ====================================================================

    /**
     * Menampilkan daftar preset untuk dipilih, lalu konfirmasi hapus.
     *
     * Panggil dari: buttonDelete.setOnClickListener(v -> showDeletePresetDialog());
     */
    public static void showDeletePresetDialog(Activity activity) {
        PresetManager.showPresetListDialog(activity, "Pilih Preset untuk Dihapus", name -> {
            // Tampilkan dialog konfirmasi hapus
            PresetManager.showDeleteConfirmDialog(activity, name, () -> {
                // Callback setelah berhasil dihapus (bisa refresh UI di sini)
            });
        });
    }

    // ====================================================================
    // 5. EKSPOR KE FILE — tombol "Ekspor Preset"
    // ====================================================================

    /**
     * Mengekspor semua preset ke file teks di penyimpanan eksternal.
     *
     * Panggil dari: buttonExportFile.setOnClickListener(v -> exportPresetsToFile());
     */
    public static void exportPresets(Activity activity) {
        // use exportToFile example
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

    // ====================================================================
    // 6. IMPOR DARI FILE — tombol "Impor dari File"
    // ====================================================================

    /**
     * Membuka file picker untuk memilih file preset, lalu mengimpornya.
     * Method ini membutuhkan ActivityResultLauncher (lihat panduan di atas).
     *
     * Contoh penggunaan di MainActivity:
     *
     * // Declare launcher
     * ActivityResultLauncher<String> fileImportLauncher =
     *     registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
     *         if (uri != null) {
     *             int count = PresetManager.importFromFile(MainActivity.this, uri);
     *             Toast.makeText(MainActivity.this,
     *                     "Berhasil impor " + count + " preset",
     *                     Toast.LENGTH_SHORT).show();
     *         }
     *     });
     *
     * // Panggil dari tombol:
     * buttonImportFile.setOnClickListener(v ->
     *     fileImportLauncher.launch(new String[]{"text/plain", "*\\/*"})
     * );
     */
    public static void openFilePickerForImport(Activity activity) {
        // Catatan: Method ini hanya panduan.
        // Implementasi penuh butuh ActivityResultLauncher.
        // Lihat komentar di atas untuk contoh lengkap.
        Toast.makeText(activity,
                "Gunakan ActivityResultLauncher — lihat komentar di kode",
                Toast.LENGTH_LONG).show();
    }

    // ====================================================================
    // LAMPIRAN: Layout XML untuk tombol-tombol preset
    // ====================================================================

    /*
     * Tambahkan tombol-tombol berikut di activity_main.xml:
     *
     * <Button
     *     android:id="@+id/btnPresetSave"
     *     android:layout_width="match_parent"
     *     android:layout_height="wrap_content"
     *     android:text="Simpan Preset" />
     *
     * <Button
     *     android:id="@+id/btnPresetLoad"
     *     android:layout_width="match_parent"
     *     android:layout_height="wrap_content"
     *     android:text="Muat Preset" />
     *
     * <Button
     *     android:id="@+id/btnPresetRename"
     *     android:layout_width="match_parent"
     *     android:layout_height="wrap_content"
     *     android:text="Ganti Nama Preset" />
     *
     * <Button
     *     android:id="@+id/btnPresetDelete"
     *     android:layout_width="match_parent"
     *     android:layout_height="wrap_content"
     *     android:text="Hapus Preset" />
     *
     * <Button
     *     android:id="@+id/btnPresetExport"
     *     android:layout_width="match_parent"
     *     android:layout_height="wrap_content"
     *     android:text="Ekspor ke Clipboard" />
     *
     * <Button
     *     android:id="@+id/btnPresetImport"
     *     android:layout_width="match_parent"
     *     android:layout_height="wrap_content"
     *     android:text="Impor dari Clipboard" />
     *
     * <Button
     *     android:id="@+id/btnPresetExportFile"
     *     android:layout_width="match_parent"
     *     android:layout_height="wrap_content"
     *     android:text="Ekspor ke File" />
     *
     * <Button
     *     android:id="@+id/btnPresetImportFile"
     *     android:layout_width="match_parent"
     *     android:layout_height="wrap_content"
     *     android:text="Impor dari File" />
     */
}