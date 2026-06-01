# Changelog - FTxT (FunText)
Dokumen ini mencatat riwayat perubahan project FTxT.

---

## [3.9.3.66.2] - 2026-06-01

### 🐞 Bug Fixes

- **CI build Gagal — AAPT2 not found** — Workflow gagal karena `local.properties` mengarah ke SDK path lokal (`/opt/android_sdk`) yang tidak ada di runner GitHub Actions. Ditambahkan step untuk generate `local.properties` dari `$ANDROID_HOME` sebelum build.

### ✏️️ File Changed

- `app/build.gradle` — versionCode 128→129, versionName 3.9.3.66.1→3.9.3.66.2
- `.github/workflows/release.yml` — Tambah step "Setup Android SDK" untuk set sdk.dir dari $ANDROID_HOME

---

## [3.9.3.66.1] - 2026-06-01

### 🔧 Optimasi & Penyesuaian

- **GitHub Actions Workflow** — Workflow CI/CD untuk build & release APK otomatis saat push tag v*.

### 🗒️ File Added

- `.github/workflows/release.yml` — Workflow: trigger tag v*, Java 17, assembleRelease, debug APK path, upload ke GitHub Release.

### ✏️️ File Changed

- `app/build.gradle` — versionCode 127→128, versionName 3.9.3.66.0→3.9.3.66.1

---

## [3.9.3.66.0] - 2026-06-01

### ✨ Fitur Baru

- **Tombol Muat di Semua Panel** — Tombol "Muat Preset" (Load) kini muncul sebagai button fisik di layout setiap panel, posisinya di antara Simpan dan E/I. Sebelumnya hanya tersedia di menu popup toolbar (gear icon).

### ✏️️ File Changed

- `app/build.gradle` — versionCode 126→127, versionName 3.9.3.65.0→3.9.3.66.0
- `app/src/main/res/layout/activity_main.xml` — Tambah btnLoadPreset di 7 module sections
- All 7 PositionControllers — setupListeners: +btnLoadPreset onClick → showLoadPresetDialog()

---

## [3.9.3.65.0] - 2026-05-31

### ✨ Fitur Baru

- **Preset Full-Konfigurasi** — Preset kini menyimpan SELURUH konfigurasi overlay, bukan hanya styling visual. Setiap preset mencakup: posisi, ukuran, warna, shadow, background, orientasi, touchPassthrough, safeArea, dan toggle display spesifik per modul. Backward compatible: preset lama tetap berfungsi (field baru diabaikan).

### ♻️️ Perubahan Fitur

- **OverlayPreset model diperluas** — 9 field baru ditambahkan ke model data preset: touchPassthrough, safeArea, textContent, showOnlyValue, showTemperature, showPercentage, showVoltage, showCurrent, showPower. Semua menggunakan Boolean wrapper agar null-safe untuk preset lama.

### ✏️️ File Changed

- `app/build.gradle` — versionCode 125→126, versionName 3.9.3.64.0→3.9.3.65.0
- `app/src/main/java/exp/ftxt/shared/preset/OverlayPreset.java` — Tambah 9 field baru
- `app/src/main/java/exp/ftxt/ui/TextPositionController.java` — doSavePreset/applyPreset: text, touchPassthrough, safeArea + updateTextStatic
- `app/src/main/java/exp/ftxt/ui/FpsPositionController.java` — doSavePreset/applyPreset: showOnlyValue, touchPassthrough, safeArea
- `app/src/main/java/exp/ftxt/ui/ClockPositionController.java` — doSavePreset/applyPreset: touchPassthrough, safeArea
- `app/src/main/java/exp/ftxt/ui/BatteryPositionController.java` — doSavePreset/applyPreset: showOnlyValue, showTemperature, showPercentage, touchPassthrough, safeArea
- `app/src/main/java/exp/ftxt/ui/BatteryPercentagePositionController.java` — doSavePreset/applyPreset: touchPassthrough
- `app/src/main/java/exp/ftxt/ui/BatteryCurrentPositionController.java` — doSavePreset/applyPreset: showVoltage, showCurrent, showPower, touchPassthrough, safeArea
- `app/src/main/java/exp/ftxt/ui/NetworkPositionController.java` — doSavePreset/applyPreset: touchPassthrough, safeArea

---

## [3.9.3.64.0] - 2026-05-31

### ✨ Fitur Baru

- **Toast Feedback Ekspor** — Saat preset berhasil diekspor ke file, aplikasi kini menampilkan Toast "Preset diekspor ke Downloads/" sebagai konfirmasi visual.
- **File Picker Impor Preset** — Tombol "Impor dari File" kini membuka file picker system (`ActivityResultContracts.OpenDocument`) untuk memilih file preset. Sebelumnya hanya menampilkan Toast dummy.
- **Bagikan Preset di Semua Panel** — Menu "Bagikan Preset" kini tersedia di semua 7 panel overlay (FPS, Clock, Battery, Battery Percentage, Battery Current, Network), sebelumnya hanya di panel Text.

### 🐞 Bug Fixes

- **Extra brace di TextPositionController** — Brace `}` berlebih di baris 221 menyebabkan class ditutup prematur (28 error kompilasi). Dihapus.

### ✏️️ File Changed

- `app/build.gradle` — versionCode 124→125, versionName 3.9.3.63.0→3.9.3.64.0
- `app/src/main/java/exp/ftxt/ui/TextPositionController.java` — Tambah imports/launcher/Toast, fix extra brace
- `app/src/main/java/exp/ftxt/ui/FpsPositionController.java` — Tambah imports/launcher/Toast + menu Bagikan
- `app/src/main/java/exp/ftxt/ui/ClockPositionController.java` — Tambah imports/launcher/Toast + menu Bagikan
- `app/src/main/java/exp/ftxt/ui/BatteryPositionController.java` — Tambah imports/launcher/Toast + menu Bagikan
- `app/src/main/java/exp/ftxt/ui/BatteryPercentagePositionController.java` — Tambah imports/launcher/Toast + menu Bagikan
- `app/src/main/java/exp/ftxt/ui/BatteryCurrentPositionController.java` — Tambah imports/launcher/Toast + menu Bagikan
- `app/src/main/java/exp/ftxt/ui/NetworkPositionController.java` — Tambah imports/launcher/Toast + menu Bagikan

---

## [3.9.3.63.0] - 2026-05-31

### ✨ Fitur Baru

- **Preset System v2 — UUID-based Index & Metadata** — Sistem penyimpanan preset diupgrade dari name-based keys ke UUID-based storage dengan index metadata yang terurut. Backward compatible: migrasi otomatis dari format lama.
- **Preset Metadata (Tags, Favorite, Timestamps)** — Setiap preset kini menyimpan tags (list), favorite flag, createdAt, dan updatedAt timestamp. Metadata terpisah dari data preset untuk fleksibilitas.
- **Thumbnail Generation** — Saat preset disimpan, aplikasi otomatis generate thumbnail bitmap sederhana berdasarkan warna utama preset (64x64px PNG, disimpan di `context.getFilesDir()/presets/`).
- **Preset Version History** — Setiap preset menyimpan history hingga 10 versi sebelumnya. API `getHistory(name)` dan `revertToHistory(name, index)` untuk restore versi lama.
- **Partial-Apply API** — Method `mergePreset(base, src, flags...)` untuk merge/apply hanya field tertentu dari preset (posisi saja, warna saja, background saja, dll). Berguna untuk selective apply preset.
- **Search & Filter** — Method `searchByNameOrTag(query)` untuk cari preset berdasarkan nama atau tag. Tags dapat di-set via `setTags(name, tags)`.
- **Preset Sharing via Share Intent** — Method `sharePreset(name)` mengekspor preset ke file di Downloads dan membuka native share chooser (Intent ACTION_SEND). Opsi clipboard dihapus (use file-based sharing).
- **Preset Metadata API** — Method `getIndexMetadata(context)` mengembalikan daftar lengkap metadata semua preset (name, uuid, timestamps, tags, favorite, thumbnail path) tanpa load data OverlayPreset lengkap.
- **Favorite & Tag Management** — Method `setFavorite(name, bool)` dan `setTags(name, List)` untuk manage metadata preset.

### 🚮️ Fitur Dihapus

- **Clipboard-based Export/Import** — Method `exportToClipboard(activity)` dan `importFromClipboard(activity)` dihapus dari `PresetManager`. Gunakan file-based export/import (`exportToFile`, `importFromFile`, `sharePreset`) sebagai gantinya.

### ♻️️ Perubahan Fitur

- **Export/Import UI Updated** — Semua tombol Export/Import di position controllers (Text, FPS, Clock, Battery, Network, dll) diganti dari "Ekspor/Impor ke Clipboard" menjadi "Ekspor ke File" / "Impor dari File" dengan guidance menggunakan file picker.
- **Share Menu** — Tambah opsi "Bagikan Preset" di export menu, memanggil `PresetManager.sharePreset(name)` untuk share via native intent.

### 🗒️ File Added

- Tidak ada file baru ditambahkan (perubahan internal di PresetManager)

### ✏️ File Changed

- `app/build.gradle` — versionCode 123→124, versionName 3.9.2.62.1→3.9.3.63.0
- `app/src/main/java/exp/ftxt/shared/preset/PresetManager.java` — Seluruh refactor storage (UUID index, metadata, history, thumbnail, merge, search, share)
- `app/src/main/java/exp/ftxt/ui/TextPositionController.java` — Replace clipboard calls with file-based export/import; tambah "Bagikan Preset" menu item
- `app/src/main/java/exp/ftxt/ui/FpsPositionController.java` — Replace clipboard calls dengan exportToFile
- `app/src/main/java/exp/ftxt/ui/ClockPositionController.java` — Replace clipboard calls dengan exportToFile
- `app/src/main/java/exp/ftxt/ui/BatteryPositionController.java` — Replace clipboard calls dengan exportToFile
- `app/src/main/java/exp/ftxt/ui/BatteryPercentagePositionController.java` — Replace clipboard calls dengan exportToFile
- `app/src/main/java/exp/ftxt/ui/BatteryCurrentPositionController.java` — Replace clipboard calls dengan exportToFile
- `app/src/main/java/exp/ftxt/ui/NetworkPositionController.java` — Replace clipboard calls dengan exportToFile
- `app/src/main/java/exp/ftxt/shared/preset/PresetExampleActivity.java` — Update example methods untuk file-based export/import guidance

### 💡 Catatan

- Backward compatibility: preset lama (name-key based) otomatis dimigrasikan ke UUID index saat diakses pertama kali.
- Thumbnail di-generate saat `save()` berdasarkan warna overlay (OverlayPreset.color). File .png disimpan relatif ke `context.getFilesDir()`.
- History capped 10 items per preset; item lama otomatis di-pop saat limit tercapai.
- Clipboard sharing dihapus untuk mengurangi kompleksitas; file-based sharing lebih reliable di Android 10+.
- Tags dan Favorite flags disimpan di index metadata (SharedPreferences), bukan di dalam OverlayPreset data.

---

## [3.9.2.62.1] - 2026-05-31

### 🐞 Bug Fixes

- **Teks hitam di tema gelap saat pertama buka** — `AppCompatDelegate.setDefaultNightMode()` dipanggil setelah `SplashScreen.installSplashScreen()`, menyebabkan tema gelap tidak diterapkan ke beberapa elemen UI. Diperbaiki dengan memindahkan `setDefaultNightMode()` sebelum `installSplashScreen()`.

### ✏️ File Changed

- `app/build.gradle` — versionCode 122→123, versionName 3.9.2.62.0→3.9.2.62.1
- `app/src/main/java/exp/ftxt/MainActivity.java` — Pindah `setDefaultNightMode()` sebelum `installSplashScreen()`

---

## [3.9.2.62.0] - 2026-05-31

### ♻️️ Perubahan Fitur

- **Tema default mode malam** — Tema aplikasi sekarang default ke mode gelap (malam) saat pertama kali dijalankan.

### ✏️ File Changed

- `app/build.gradle` — versionCode 121→122, versionName 2.9.2.61.0→3.9.2.62.0 (major+1)
- `app/src/main/java/exp/ftxt/MainActivity.java` — Default theme dark mode (false→true), sidebar label "FTxT v..." → "FunText v... Beta"
- `app/src/main/res/layout/nav_header.xml` — Placeholder text "FTxT" → "FunText"
- `README.md` + `README.txt` — Rebranding FTxT (FunText), Beta status, dark mode default
- `PANDUAN.md` + `PANDUAN.txt` — Rebranding FTxT (FunText), dark mode mention
- `DEVELOPMENT.md` + `DEVELOPMENT.txt` — Rebranding FTxT (FunText), Beta phase note
- `TENTANG.md` + `TENTANG.txt` — Rebranding FTxT (FunText)

### 💡 Catatan

- Mulai rilis ini, aplikasi menggunakan label **"FunText"** dan menandai status **Beta**. Seluruh rilis berikutnya akan menggunakan format `FunText vX.X.X.X.X Beta` pada sidebar header.

---

## [2.9.2.60.1] - 2026-05-31

### ♻️️ Perubahan Fitur

- **Placeholder panel Crosshair, Watermark, Logo** — Tiga panel kosong kini menampilkan pesan "Konsep overlay sedang dalam tahap pengerjaan" sebagai pengganti panel Text saat nav item diklik.

### ✨ Fitur Baru

- **Safe Area "Gunakan Area Aman"** — Menambahkan checkbox "Gunakan Area Aman" pada semua 6 panel overlay (Text, FPS, Clock, Battery, Battery Current, Network) untuk mengaktifkan/nonaktifkan pembatasan posisi agar tetap dalam area aman layar.
- **Auto-request izin saat pertama buka** — Aplikasi otomatis meminta semua izin (Overlay, Notifikasi, Optimasi Baterai) saat pertama kali dijalankan.

### 🗒️ File Added

- `app/src/main/res/layout/activity_main.xml` — Tambah 6 checkbox safeArea + 3 panel placeholder (crosshair, watermark, logo)

### ✏️ File Changed

- `app/build.gradle` — versionCode 118→120, versionName 2.9.2.59.0→2.9.2.60.1
- `app/src/main/java/exp/ftxt/ui/TextPanelController.java` — Tambah field, bindViews, loadConfig, listener untuk `textSafeArea`
- `app/src/main/java/exp/ftxt/ui/FpsPanelController.java` — Tambah field, bindViews, loadConfig, listener untuk `fpsSafeArea`
- `app/src/main/java/exp/ftxt/ui/ClockPanelController.java` — Tambah field, bindViews, loadConfig, listener untuk `clockSafeArea`
- `app/src/main/java/exp/ftxt/ui/NetworkPanelController.java` — Tambah field, bindViews, loadConfig, listener untuk `networkSafeArea`
- `app/src/main/java/exp/ftxt/ui/BatteryPanelController.java` — Tambah field, bindViews, loadConfig, listener untuk `batterySafeArea`
- `app/src/main/java/exp/ftxt/ui/BatteryCurrentPanelController.java` — Tambah field, bindViews, loadConfig, listener untuk `batCurSafeArea`
- `app/src/main/java/exp/ftxt/MainActivity.java` — loadConfig safeArea semua overlay, auto-request izin, placeholder panel crosshair/watermark/logo

---

## [2.9.2.59.0] - 2026-05-31

### ✨ Fitur Baru

- **Battery Stats toggles °C / %** — Panel Battery Stats (sebelumnya Battery Temperature) kini memiliki dua checkbox toggle °C dan % untuk mengontrol tampilan suhu dan persentase baterai secara independen. Keduanya bisa aktif bersamaan atau salah satu saja.

### ✏️ File Changed

- `app/build.gradle` — versionCode 117→118, versionName 2.9.2.58.0→2.9.2.59.0
- `app/src/main/java/exp/ftxt/features/battery_temperature/BatteryConfig.java` — Tambah `showTemperature`, `showPercentage`
- `app/src/main/java/exp/ftxt/features/battery_temperature/BatteryModule.java` — Update `getBatteryTempText()` pakai flags showTemperature/showPercentage
- `app/src/main/res/layout/activity_main.xml` — Tambah checkbox °C (`batteryShowTempCheck`) dan % (`batteryShowPctCheck`) di panel battery
- `app/src/main/java/exp/ftxt/ui/BatteryPanelController.java` — Bind + listener untuk checkbox °C/%
- `app/src/main/java/exp/ftxt/MainActivity.java` — loadConfig prefs `battery_show_temperature`, `battery_show_percentage`

---

## [2.9.2.58.0] - 2026-05-31

### ✨ Fitur Baru

- **Battery Percentage Overlay** — Fitur baru untuk menampilkan persentase baterai sebagai overlay. Update tiap 5 detik, mendukung semua konfigurasi: ukuran, warna, shadow, background, kontrol posisi, preset, dan orientasi.
- **Battery Current Overlay** — Fitur baru untuk menampilkan tegangan (mV), arus (mA), dan daya (W) baterai sebagai overlay. Update tiap 5 detik, mendukung semua konfigurasi: ukuran, warna, shadow, background, kontrol posisi, preset, dan orientasi.

### 🗒️ File Added

- `app/src/main/java/exp/ftxt/features/battery_percentage/BatteryPercentageConfig.java` — Konfigurasi statis modul Battery Percentage
- `app/src/main/java/exp/ftxt/features/battery_percentage/BatteryPercentageModule.java` — Module logic overlay persentase baterai
- `app/src/main/java/exp/ftxt/ui/BatteryPercentagePanelController.java` — Controller panel settings Battery Percentage
- `app/src/main/java/exp/ftxt/ui/BatteryPercentagePositionController.java` — Controller kontrol posisi Battery Percentage
- `app/src/main/java/exp/ftxt/features/battery_current/BatteryCurrentConfig.java` — Konfigurasi statis modul Battery Current
- `app/src/main/java/exp/ftxt/features/battery_current/BatteryCurrentModule.java` — Module logic overlay arus baterai
- `app/src/main/java/exp/ftxt/ui/BatteryCurrentPanelController.java` — Controller panel settings Battery Current
- `app/src/main/java/exp/ftxt/ui/BatteryCurrentPositionController.java` — Controller kontrol posisi Battery Current

### ✏️ File Changed

- `app/build.gradle` — versionCode 116→117, versionName 2.9.2.57.1→2.9.2.58.0
- `app/src/main/res/values/strings.xml` — Tambah `nav_battery_percentage`, `nav_battery_current`
- `app/src/main/res/menu/drawer_menu.xml` — Tambah item `nav_battery_percentage`, `nav_battery_current`
- `app/src/main/res/layout/drawer_content.xml` — Tambah `navBatteryPercentage`, `navBatteryCurrent`
- `app/src/main/res/layout/activity_main.xml` — Tambah panel `panel_battery_percentage`, `panel_battery_current`
- `app/src/main/java/exp/ftxt/MainActivity.java` — Registrasi panel, nav handler, loadConfig, sidebar
- `app/src/main/java/exp/ftxt/core/FloatingService.java` — Module + delegates Battery Percentage & Battery Current

---

## [2.9.2.57.1] - 2026-05-31

### ♻️ Perubahan Fitur

- **"Battery Monitor" → "Battery Temperature"**: Nama tampilan modul baterai diubah dari "Battery Monitor" menjadi "Battery Temperature" di navigasi drawer, toolbar, dan label modul.
- **Restrukturisasi folder features**: Semua folder fitur di `features/` diubah namanya agar sesuai dengan label yang ditampilkan di aplikasi:
  - `features/battery/` → `features/battery_temperature/`
  - `features/clock/` → `features/clock_module/`
  - `features/cpu/` → `features/cpu_monitor/`
  - `features/fps/` → `features/fps_display/`
  - `features/logo/` → `features/logo_display/`
  - `features/network/` → `features/network_stats/`
  - `features/text/` → `features/floating_text/`
  - `features/crosshair/` — tetap
  - `features/watermark/` — tetap

### ✏️ File Changed

- `app/build.gradle` — versionCode 115→116, versionName 2.9.2.57.0→2.9.2.57.1
- `app/src/main/res/values/strings.xml` — nav_battery: "Battery Monitor"→"Battery Temperature"
- `app/src/main/res/layout/drawer_content.xml` — Label navigasi "Battery Monitor"→"Battery Temperature"
- `app/src/main/java/exp/ftxt/MainActivity.java` — JSON nav entry battery & imports update
- `app/src/main/java/exp/ftxt/core/FloatingService.java` — Imports & komentar path fitur
- `app/src/main/java/exp/ftxt/ui/BatteryPanelController.java` — Imports update
- `app/src/main/java/exp/ftxt/ui/BatteryPositionController.java` — Imports update
- `app/src/main/java/exp/ftxt/ui/ClockPanelController.java` — Imports update
- `app/src/main/java/exp/ftxt/ui/ClockPositionController.java` — Imports update
- `app/src/main/java/exp/ftxt/ui/FpsPanelController.java` — Imports update
- `app/src/main/java/exp/ftxt/ui/FpsPositionController.java` — Imports update
- `app/src/main/java/exp/ftxt/ui/NetworkPanelController.java` — Imports update
- `app/src/main/java/exp/ftxt/ui/NetworkPositionController.java` — Imports update
- `app/src/main/java/exp/ftxt/ui/TextPanelController.java` — Imports update
- `app/src/main/java/exp/ftxt/ui/TextPositionController.java` — Imports update
- `app/src/main/java/exp/ftxt/shared/preset/PresetExampleActivity.java` — Imports update
- `app/src/main/java/exp/ftxt/features/battery/BatteryConfig.java` — Package + pindah ke features/battery_temperature/
- `app/src/main/java/exp/ftxt/features/battery/BatteryModule.java` — Package + pindah ke features/battery_temperature/
- `app/src/main/java/exp/ftxt/features/clock/ClockConfig.java` — Package + pindah ke features/clock_module/
- `app/src/main/java/exp/ftxt/features/clock/ClockModule.java` — Package + pindah ke features/clock_module/
- `app/src/main/java/exp/ftxt/features/fps/FpsConfig.java` — Package + pindah ke features/fps_display/
- `app/src/main/java/exp/ftxt/features/fps/FpsModule.java` — Package + pindah ke features/fps_display/
- `app/src/main/java/exp/ftxt/features/network/NetworkConfig.java` — Package + pindah ke features/network_stats/
- `app/src/main/java/exp/ftxt/features/network/NetworkModule.java` — Package + pindah ke features/network_stats/
- `app/src/main/java/exp/ftxt/features/text/TextConfig.java` — Package + pindah ke features/floating_text/
- `app/src/main/java/exp/ftxt/features/text/TextModule.java` — Package + pindah ke features/floating_text/
- `app/src/main/java/exp/ftxt/features/cpu/` — Pindah ke features/cpu_monitor/
- `app/src/main/java/exp/ftxt/features/logo/` — Pindah ke features/logo_display/

---

## [2.9.2.57.0] - 2026-05-31

### ♻️ Perubahan Fitur

- **Tombol Potret/Lanskap dipindah ke toolbar**: Tombol orientasi Potret dan Lanskap dihapus dari panel kontrol posisi (Text, FPS, Clock, Battery, Network) dan digantikan dengan ikon orientasi layar di toolbar samping ikon tema. Ketuk ikon untuk toggle orientasi layar antara potret dan lanskap.

### 🚮️ Fitur Dihapus

- **Tombol Potret/Lanskap per panel**: Button `btnPortrait`/`btnLandscape` dihapus dari kelima panel overlay.

### 🗒️ File Added

- `app/src/main/res/drawable/ic_screen_rotation.xml` — Ikon orientasi layar untuk toolbar.

### ✏️ File Changed

- `app/build.gradle` — versionCode 114→115, versionName 2.8.2.56.0→2.9.2.57.0
- `app/src/main/res/menu/main_menu.xml` — Tambah item `action_orientation` dengan ikon screen rotation
- `app/src/main/java/exp/ftxt/MainActivity.java` — Handler `action_orientation` toggle orientation via `setRequestedOrientation()`
- `app/src/main/java/exp/ftxt/ui/TextPositionController.java` — Hapus btnPortrait/btnLandscape
- `app/src/main/java/exp/ftxt/ui/FpsPositionController.java` — Hapus btnPortrait/btnLandscape
- `app/src/main/java/exp/ftxt/ui/ClockPositionController.java` — Hapus btnPortrait/btnLandscape
- `app/src/main/java/exp/ftxt/ui/BatteryPositionController.java` — Hapus btnPortrait/btnLandscape
- `app/src/main/java/exp/ftxt/ui/NetworkPositionController.java` — Hapus btnPortrait/btnLandscape
- `app/src/main/res/layout/activity_main.xml` — Hapus blok Potret/Lanskap dari 5 panel

---

## [2.8.2.56.0] - 2026-05-31

### ✨ Fitur Baru

- **Sistem Preset Full-Konfigurasi (GSON) terintegrasi ke UI** — Tombol Simpan, Muat, dan E/I pada semua 5 panel (Text, FPS, Clock, Battery, Network) sekarang menyimpan/memuat FULL konfigurasi overlay (posisi, ukuran, warna, shadow, background, orientasi) via PresetManager+OverlayPreset (GSON), bukan hanya posisi X/Y seperti sebelumnya.

### 🚮️ Fitur Dihapus

- **PositionPresetManager** — Sistem preset posisi X/Y lama digantikan total oleh PresetManager berbasis GSON. File dipindahkan ke `_karantina/` untuk referensi jika diperlukan kembali.

### ✏️ File Changed

- `app/build.gradle` — versionCode 113→114, versionName 2.7.2.54.0→2.8.2.56.0
- `app/src/main/java/exp/ftxt/ui/TextPositionController.java` — Replace PositionPresetManager → PresetManager+OverlayPreset, Simpan/Muat full config via GSON
- `app/src/main/java/exp/ftxt/ui/FpsPositionController.java` — Same replacement
- `app/src/main/java/exp/ftxt/ui/ClockPositionController.java` — Same replacement
- `app/src/main/java/exp/ftxt/ui/BatteryPositionController.java` — Same replacement
- `app/src/main/java/exp/ftxt/ui/NetworkPositionController.java` — Same replacement

### 🔥 File Removed

- `app/src/main/java/exp/ftxt/shared/ui/PositionPresetManager.java` → dipindah ke `_karantina/exp/ftxt/shared/ui/PositionPresetManager.java`

---

## [2.7.2.54.0] - 2026-05-30

### ✨ Fitur Baru

- **Sistem Manajemen Preset Overlay (GSON)** — Sistem preset baru berbasis GSON yang menyimpan seluruh konfigurasi overlay (posisi, ukuran, warna, shadow, background, orientasi) dalam format JSON. Mendukung Save, Load, Rename, Select, Delete, Export (clipboard/file), dan Import (clipboard/file).

### 🗒️ File Added

- `app/src/main/java/exp/ftxt/shared/preset/OverlayPreset.java` — Model data preset dengan field posisi, ukuran, warna, shadow, background, orientasi.
- `app/src/main/java/exp/ftxt/shared/preset/PresetManager.java` — Manager static method CRUD: save, load, rename, getAllNames, delete, deleteAll, exportToJson, exportAllToJson, exportToFile, importFromJson, importManyFromJson, importFromFile, showDeleteConfirmDialog, showPresetListDialog, exportToClipboard, importFromClipboard.
- `app/src/main/java/exp/ftxt/shared/preset/PresetExampleActivity.java` — Contoh implementasi OnClickListener untuk integrasi di MainActivity.

### ✏️ File Changed

- `app/build.gradle` — versionCode 112→113, versionName 2.7.2.53.0→2.7.2.54.0, tambah dependensi `com.google.code.gson:gson:2.10.1`.

---

## [2.7.2.53.0] - 2026-05-30

### ✨ Fitur Baru

- **Network Speed Meter Overlay** — Memantau kecepatan internet real-time (↓ upload / ↑ download) dengan format otomatis KB/s ↔ MB/s, polling tiap 1 detik via TrafficStats.
- **Network Speed Position Controls** — Slider X/Y, D-Pad, preset save/load, orientasi Potret/Lanskap untuk Network Speed overlay.

### 🗒️ File Added

- `app/src/main/java/exp/ftxt/features/network/NetworkConfig.java` — Konfigurasi network speed meter.
- `app/src/main/java/exp/ftxt/features/network/NetworkModule.java` — Modul logic polling TrafficStats + format speed.
- `app/src/main/java/exp/ftxt/ui/NetworkPanelController.java` — Controller panel UI network speed.
- `app/src/main/java/exp/ftxt/ui/NetworkPositionController.java` — Controller kontrol posisi (slider, d-pad, preset, orientasi).

### ✏️ File Changed

- `app/build.gradle` — versionCode 110→112, versionName 2.7.2.51.0→2.7.2.53.0
- `app/src/main/java/exp/ftxt/core/FloatingService.java` — Tambah NetworkModule field, init, start/stop, 8 delegate static methods + setNetworkOrientationSuffixStatic + getNetworkCurrentPosition.
- `app/src/main/java/exp/ftxt/MainActivity.java` — Integrasi panel network: field, init, nav handler, show/hide, lifecycle.
- `app/src/main/res/layout/activity_main.xml` — Tambah panel network lengkap (CheckBox switch + lock, SeekBar size, Button color, position controls, Shadow config, Background config).

## [2.7.2.51.0] - 2026-05-30

### ♻️ Perubahan Fitur

- **Tombol preset jadi satu baris**: Simpan, Muat, dan E/I dalam satu baris horizontal. Reset dihapus.
- **Ubah teks button**: "Simpan Preset" → "Simpan", "Muat Preset" → "Muat".

### 🔧 Optimasi & Penyesuaian

- **Warna sidebar ikut tema terang/gelap**: Drawer background dan header tidak lagi hardcoded. Pakai `@color/drawer_background` dan `@color/drawer_header_background` dengan variant `values-night/colors.xml` untuk mode gelap.

### ✏️ File Changed

- `app/build.gradle` — versionCode 109→110, versionName 2.7.2.50.0→2.7.2.51.0
- `app/src/main/res/layout/activity_main.xml` — restruktur preset buttons di 4 panel
- `app/src/main/res/values/colors.xml` — tambah `drawer_background`, `drawer_header_background`, `drawer_header_text`
- `app/src/main/res/values-night/colors.xml` — FILE BARU, warna drawer untuk mode gelap
- `app/src/main/res/layout/drawer_content.xml` — `#FFFFFF` → `@color/drawer_background`
- `app/src/main/res/layout/nav_header.xml` — `#222222` → `@color/drawer_header_background`, `#FFFFFF` → `@color/drawer_header_text`

---

## [2.7.2.50.0] - 2026-05-30

### ♻️ Perubahan Fitur

- **Semua toggle Switch → CheckBox**: `overlaySwitch`, `touchPassthroughSwitch`, `shadowSwitch`, `bgSwitch` dan varian fps/clock/battery diubah dari Switch menjadi CheckBox di semua 4 panel.
- **Overlay & Kunci Posisi sejajar horizontal**: Setiap panel kini menampilkan CheckBox overlay dan kunci posisi dalam satu baris horizontal.
- **Urutan komponen diubah semua panel**: Kontrol Posisi dipindah ke bawah Pilih Warna; Background dipindah ke paling bawah (di bawah Shadow).
- **Label teks Switch dihapus**: Teks langsung di `android:text` CheckBox, TextView label samping dihapus.

### ✏️ File Changed

- `app/build.gradle` — versionCode 108→109, versionName 2.7.2.49.0→2.7.2.50.0
- `app/src/main/res/layout/activity_main.xml` — restruktur urutan & Switch→CheckBox di semua 4 panel
- `app/src/main/java/exp/ftxt/MainActivity.java` — `applySwitchTint` → `applyCheckboxTint`, parameter Switch→CheckBox
- `app/src/main/java/exp/ftxt/ui/TextPanelController.java` — Switch→CheckBox, applySwitchTint→applyCheckboxTint
- `app/src/main/java/exp/ftxt/ui/FpsPanelController.java` — same
- `app/src/main/java/exp/ftxt/ui/ClockPanelController.java` — same
- `app/src/main/java/exp/ftxt/ui/BatteryPanelController.java` — same

---

## [2.7.2.49.0] - 2026-05-30

### 🚮️ Fitur Dihapus

- **Grid Posisi 3×3 FPS/Clock/Battery**: Tombol grid 3×3 untuk quick preset posisi dihapus dari panel FPS Display, Jam Digital, dan Suhu Baterai — melanjutkan penghapusan yang sudah dilakukan di panel Floating Text.
- **Auto Preset Aplikasi FPS**: Fitur auto-switch preset berdasarkan aplikasi di FPS dihapus (sudah dihapus dari Text di release sebelumnya).

### ✏️ File Changed

- `app/build.gradle` — versionCode 107→108, versionName 2.6.2.48.1→2.7.2.49.0
- `app/src/main/java/exp/ftxt/ui/FpsPositionController.java` — hapus grid 3×3, auto preset, AppPresetWatcher
- `app/src/main/java/exp/ftxt/ui/BatteryPositionController.java` — hapus grid 3×3
- `app/src/main/java/exp/ftxt/ui/ClockPositionController.java` — hapus grid 3×3
- `app/src/main/res/layout/activity_main.xml` — hapus grid 3×3 (FPS, Clock, Battery) & auto preset FPS

---

## [2.6.2.48.1] - 2026-05-30

### 🐞 Bug Fixes

- **TextPositionController crash**: Grid 3×3 buttons (`btnPosTL`–`btnPosBR`), orientation buttons (`btnPortrait`/`btnLandscape`), dan auto-preset switch (`autoPresetSwitch`) dihapus dari layout Text panel di release sebelumnya, tapi kode Java masih mereferensinya — menyebabkan `cannot find symbol` saat kompilasi. Semua referensi dihapus dari `TextPositionController.java`.

### 🔧 Optimasi & Penyesuaian

- **Gradle namespace syntax**: `namespace "exp.ftxt"` diganti `namespace = "exp.ftxt"` untuk kompatibilitas Gradle 10.

### ✏️ File Changed

- `app/build.gradle` — versionCode 106→107, versionName 2.6.2.48.0→2.6.2.48.1
- `app/src/main/java/exp/ftxt/ui/TextPositionController.java` — hapus grid 3x3, orientation buttons, auto-preset watcher

---

## [2.6.2.48.0] - 2026-05-30

### 🚮️ Fitur Dihapus

- **Grid Posisi 3×3**: Tombol grid 3×3 untuk quick preset posisi dihapus karena kurang berguna. User dapat menggunakan slider X/Y, D-Pad, atau preset system yang lebih flexible.
- **Auto Preset Aplikasi**: Fitur auto-switch preset berdasarkan orientasi dihapus. Orientasi mode (Potret/Lanskap) masih tersedia melalui kontrol posisi manual.

### ✏️ File Changed

- `app/build.gradle` — versionCode 105→106, versionName 2.6.1.47.0→2.6.2.48.0
- `app/src/main/res/layout/activity_main.xml` — hapus grid buttons (9 buttons) dan auto preset switch/buttons

---

## [2.6.1.47.0] - 2026-05-30

### ✨ Fitur Baru

- **Preset Preview**: Tampilan mini-map posisi pada daftar preset saat memuat.
- **Preset Export/Import**: Ekspor preset ke clipboard (JSON), impor dari clipboard.
- **More Preset Slots**: Jumlah maksimal preset ditingkatkan dari 10 menjadi 50.
- **Preset Rename**: Ubah nama preset langsung dari opsi long-press (sudah ada, ditingkatkan).
- **Visual Preset Editor**: Editor posisi visual dengan drag dot pada mini-map.
- **Visual Preset Indicator**: Indikator preset aktif ditampilkan di panel kontrol posisi.

### 🗒️ File Added

- `app/src/main/java/exp/ftxt/shared/ui/PresetPreviewView.java` — Custom View mini-map posisi
- `app/src/main/res/layout/preset_list_item.xml` — Layout item daftar preset dengan preview

### ✏️ File Changed

- `app/build.gradle` — versionCode 104→105, versionName 2.6.1.46.1→2.6.1.47.0
- `app/src/main/java/exp/ftxt/shared/ui/PositionPresetManager.java` — overhaul fitur preset
- `app/src/main/res/layout/activity_main.xml` — tambah preset indicator & export/import button (4 panel)
- `app/src/main/java/exp/ftxt/ui/TextPositionController.java` — wiring preset indicator & export/import
- `app/src/main/java/exp/ftxt/ui/FpsPositionController.java` — wiring preset indicator & export/import
- `app/src/main/java/exp/ftxt/ui/ClockPositionController.java` — wiring preset indicator & export/import
- `app/src/main/java/exp/ftxt/ui/BatteryPositionController.java` — wiring preset indicator & export/import

---

## [2.6.1.46.1] - 2026-05-30

### 🐞 Bug Fixes

- **Slider Posisi**: Slider X/Y tidak tersinkronisasi saat overlay dipindahkan dengan mode drag. Kini slider mengikuti posisi overlay secara real-time.
- **Sinkronisasi semua overlay**: Perbaikan untuk Text, FPS, Battery, dan Clock overlay.

### ✏️ File Changed

- `app/build.gradle` — versionCode 103→104, versionName 2.6.1.46.0→2.6.1.46.1
- `app/src/main/java/exp/ftxt/features/battery/BatteryModule.java` — update Config saat drag
- `app/src/main/java/exp/ftxt/features/clock/ClockModule.java` — update Config saat drag
- `app/src/main/java/exp/ftxt/features/text/TextModule.java` — update Config saat drag
- `app/src/main/java/exp/ftxt/features/fps/FpsModule.java` — update Config saat drag
- `app/src/main/java/exp/ftxt/ui/BatteryPositionController.java` — onPositionUpdate syncAll
- `app/src/main/java/exp/ftxt/ui/ClockPositionController.java` — onPositionUpdate syncAll
- `app/src/main/java/exp/ftxt/ui/TextPositionController.java` — onPositionUpdate syncAll
- `app/src/main/java/exp/ftxt/ui/FpsPositionController.java` — onPositionUpdate syncAll

---

## [2.6.1.46.0] - 2026-05-30

### ✨ Fitur Baru

- **Jam Digital Overlay**: Overlay waktu real-time 24 jam format `HH:mm:ss`, update tiap 1 detik. Dapat diatur warna, ukuran, background, shadow, posisi (drag atau kunci), plus kontrol posisi lengkap (slider X/Y, D-Pad, grid 3×3, preset, orientasi). Default ukuran 48sp.
- **Suhu Baterai Overlay**: Overlay suhu baterai dalam Celsius (°C), update tiap 5 detik. Membaca via `BatteryManager.EXTRA_TEMPERATURE`. Opsi tampilkan hanya nilai. Dilengkapi kontrol posisi lengkap (slider X/Y, D-Pad, grid 3×3, preset, orientasi).

### 🗒️ File Added

- `app/src/main/java/exp/ftxt/features/clock/ClockConfig.java`
- `app/src/main/java/exp/ftxt/features/clock/ClockModule.java`
- `app/src/main/java/exp/ftxt/features/battery/BatteryConfig.java`
- `app/src/main/java/exp/ftxt/features/battery/BatteryModule.java`
- `app/src/main/java/exp/ftxt/ui/ClockPanelController.java`
- `app/src/main/java/exp/ftxt/ui/ClockPositionController.java`
- `app/src/main/java/exp/ftxt/ui/BatteryPanelController.java`
- `app/src/main/java/exp/ftxt/ui/BatteryPositionController.java`

### ✏️️ File Changed

- app/build.gradle
- app/src/main/java/exp/ftxt/features/clock/ClockModule.java
- app/src/main/java/exp/ftxt/features/battery/BatteryModule.java
- app/src/main/java/exp/ftxt/core/FloatingService.java
- app/src/main/java/exp/ftxt/ui/ClockPanelController.java
- app/src/main/java/exp/ftxt/ui/BatteryPanelController.java
- app/src/main/java/exp/ftxt/MainActivity.java
- app/src/main/res/layout/activity_main.xml
- CHANGELOG.md
- app/src/main/assets/CHANGELOG.txt
- STRUKTUR.md
- app/src/main/assets/STRUKTUR.txt
- README.md
- app/src/main/assets/README.txt

### 🔢 Version

- versionCode: 102 → 103
- versionName: 2.6.1.45.0 → 2.6.1.46.0

---

## [2.6.1.44.0] - 2026-05-29

### ✨ Fitur Baru

- **Kontrol Posisi FPS Display**: Tambah Slider X/Y, D-Pad, preset posisi, dan tombol orientasi (Potret/Lanskap) pada panel FPS Display — menyamai yang sudah ada di Floating Text. Posisi disimpan terpisah per orientasi dengan sistem normalized 0.0–1.0.

### 🗒️ File Added

- `app/src/main/java/exp/ftxt/ui/FpsPositionController.java`

### ✏️️ File Changed

- app/build.gradle
- app/src/main/java/exp/ftxt/MainActivity.java
- app/src/main/java/exp/ftxt/core/FloatingService.java
- app/src/main/java/exp/ftxt/features/fps/FpsConfig.java
- app/src/main/java/exp/ftxt/features/fps/FpsModule.java
- app/src/main/java/exp/ftxt/ui/FpsPanelController.java
- app/src/main/res/layout/activity_main.xml
- CHANGELOG.md
- app/src/main/assets/CHANGELOG.txt

### 🔢 Version

- versionCode: 100 → 101
- versionName: 2.6.1.43.2 → 2.6.1.44.0

---

## [2.6.1.43.0] - 2026-05-29

### ✨ Fitur Baru

- **Tampilan Koordinat Posisi Real-time**: Label "Kontrol Posisi" kini menampilkan koordinat pixel sesungguhnya (contoh: `540X1200`) yang berubah langsung saat slider, D-Pad, preset, atau drag overlay — menggunakan `getRealMetrics()` untuk akurasi full screen.

### 🔧 Optimasi & Penyesuaian

- **Layout Kontrol Posisi**: D-Pad dipindah ke kanan sejajar dengan Slider X/Y dalam satu baris horizontal. Tombol preset (Simpan Preset, Muat Preset, Reset) diberi `singleLine` agar teks tidak wrapping dan sejajar sempurna.

### 🐞 Bug Fixes

- **Padding Background Mendorong Teks**: `applyBackground()` selalu set padding (`bgPadding=25`) meski background mati, mendorong teks menjauh dari tepi view — menyebabkan gap kecil di X=0, Y=0. Sekarang padding cuma diterapkan saat `bgEnabled=true`.
- **Hal yang sama diterapkan di `FpsModule.applyBackground()` untuk konsistensi.

### 🗒️ File Added

- `OverlayDragHandler`: konstruktor baru dengan parameter `onDragMove` callback untuk update posisi real-time saat drag.

### ✏️️ File Changed

- app/build.gradle
- app/src/main/res/layout/activity_main.xml
- app/src/main/java/exp/ftxt/features/text/TextModule.java
- app/src/main/java/exp/ftxt/features/fps/FpsModule.java
- app/src/main/java/exp/ftxt/ui/PositionController.java
- app/src/main/java/exp/ftxt/core/FloatingService.java
- app/src/main/java/exp/ftxt/shared/ui/OverlayDragHandler.java
- CHANGELOG.md
- app/src/main/assets/CHANGELOG.txt

### 🔢 Version

- versionCode: 97 → 98
- versionName: 2.6.1.42.1 → 2.6.1.43.0

---

## [2.6.1.42.0] - 2026-05-29

### 🚮️ Fitur Dihapus

- **Karantina XY Pad**: `XyPadView.java` dipindahkan dari `shared/ui/` ke `_karantina/` untuk diarsipkan. XY Pad tidak lagi aktif di UI. Dapat dipulihkan kembali di masa depan dengan mengembalikan file ke `shared/ui/` dan menghubungkan kembali di `PositionController` + layout.

### ♻️️ Perubahan Fitur

- **PositionController**: Referensi XY Pad dihapus — turun dari 159 ke 153 baris. Sekarang hanya mengelola Slider + D-Pad + Preset + Orientasi.

### ✏️️ File Changed

- app/build.gradle
- app/src/main/java/exp/ftxt/ui/PositionController.java
- app/src/main/res/layout/activity_main.xml
- CHANGELOG.md
- app/src/main/assets/CHANGELOG.txt
- STRUKTUR.md
- app/src/main/assets/STRUKTUR.txt
- README.md
- app/src/main/assets/README.txt

### 🔥️ File Removed

- `app/src/main/java/exp/ftxt/shared/ui/XyPadView.java` → dipindah ke `_karantina/exp/ftxt/shared/ui/XyPadView.java`

### 🗒️ File Added

- `_karantina/exp/ftxt/shared/ui/XyPadView.java`

### 🔢 Version

- versionCode: 95 → 96
- versionName: 2.5.1.41.4 → 2.6.1.42.0

---

## [2.5.1.41.4] - 2026-05-29

### 🔧 Optimasi & Penyesuaian

- **Ekstrak D-Pad ke Shared Component**: Logika D-Pad (touch listener dengan repeat) diekstrak dari `PositionController.java` ke `shared/ui/DpadController.java` — mengurangi duplikasi dan meningkatkan modularitas, mengikuti pola `XyPadView` dan `SliderLabelEditor`.

- **Ekstrak Preset Posisi ke Shared Component**: Sistem preset posisi (simpan/load/hapus dengan long-press) diekstrak dari `PositionController.java` ke `shared/ui/PositionPresetManager.java` — mengurangi kompleksitas `PositionController` dari 350 menjadi 159 baris.

- **Ekstrak Slider X/Y ke Shared Component**: Slider SeekBar posisi X dan Y (dengan label) diekstrak ke `shared/ui/SliderPositionController.java` — masing-masing dengan `isUpdating` guard sendiri untuk mencegah infinite loop saat sinkronisasi.

### 🗒️ File Added

- `app/src/main/java/exp/ftxt/shared/ui/DpadController.java`
- `app/src/main/java/exp/ftxt/shared/ui/PositionPresetManager.java`
- `app/src/main/java/exp/ftxt/shared/ui/SliderPositionController.java`

### ✏️️ File Changed

- app/build.gradle
- app/src/main/java/exp/ftxt/ui/PositionController.java
- CHANGELOG.md
- app/src/main/assets/CHANGELOG.txt
- STRUKTUR.md
- app/src/main/assets/STRUKTUR.txt
- README.md
- app/src/main/assets/README.txt

### 🔢 Version

- versionCode: 94 → 95
- versionName: 2.5.1.41.3 → 2.5.1.41.4

---

## [2.5.1.41.3] - 2026-05-29

### 🐞 Bug Fixes

- **Overlay Baru Pakai Dimensi Layar Lama**: `TextModule.createOverlay()` menggunakan `screenWidth`/`screenHeight` yang disimpan saat `init()`. Jika overlay dihapus lalu dibuat ulang setelah rotasi layar, posisinya dihitung dengan dimensi lama. Sekarang `createOverlay()` juga refresh dimensi dari `WindowManager`.
- **Suffix Orientasi Tidak Sinkron**: `TextModule.posSuffix()` selalu membaca orientasi perangkat fisik, sementara `PositionController` bisa dioverride manual (tombol Potret/Lanskap). Akibatnya, drag overlay menyimpan posisi ke key orientasi yang salah. Sekarang `TextModule` punya `orientationSuffix` yang bisa di-set dari `PositionController`, dan di-reset tiap `PositionController` dibuat atau berganti mode.

### ✏️️ File Changed

- app/build.gradle
- app/src/main/java/exp/ftxt/features/text/TextModule.java
- app/src/main/java/exp/ftxt/core/FloatingService.java
- app/src/main/java/exp/ftxt/ui/PositionController.java
- CHANGELOG.md
- app/src/main/assets/CHANGELOG.txt

### 🔢 Version

- versionCode: 93 → 94
- versionName: 2.5.1.41.2 → 2.5.1.41.3

---

## [2.5.1.41.2] - 2026-05-29

### 🐞 Bug Fixes

- **Posisi Layar Landscape Tidak Akurat**: `screenWidth`/`screenHeight` di `TextModule` menggunakan dimensi layar saat service start (tidak update saat orientasi berubah). Akibatnya slider, D-Pad, dan XY Pad menghitung posisi dengan ukuran layar yang salah. Sekarang `updatePosition()`, `loadPosition()`, dan `savePosition()` selalu mengambil dimensi layar terkini.

### ✏️️ File Changed

- app/build.gradle
- app/src/main/java/exp/ftxt/features/text/TextModule.java
- CHANGELOG.md
- app/src/main/assets/CHANGELOG.txt

### 🔢 Version

- versionCode: 92 → 93
- versionName: 2.5.1.41.1 → 2.5.1.41.2

---

## [2.5.1.41.1] - 2026-05-29

### 🔧 Optimasi & Penyesuaian

- **Auto-Apply Posisi Orientasi**: Posisi overlay kini otomatis diterapkan saat orientasi layar berubah, tanpa perlu menekan tombol [Potret]/[Lanskap] secara manual.

### ✏️️ File Changed

- app/build.gradle
- app/src/main/java/exp/ftxt/MainActivity.java
- CHANGELOG.md
- app/src/main/assets/CHANGELOG.txt

### 🔢 Version

- versionCode: 91 → 92
- versionName: 2.5.1.41.0 → 2.5.1.41.1

---

## [2.5.1.41.0] - 2026-05-29

### ✨ Fitur Baru

- **Orientasi Mode Posisi**: Tambah tombol [Potret] [Lanskap] di kontrol posisi untuk mengatur posisi overlay secara terpisah per orientasi. Setiap perubahan posisi otomatis tersimpan ke orientasi yang sedang aktif.

### ♻️️ Perubahan Fitur

- **Penyimpanan Posisi Langsung**: `onPositionChanged()` kini langsung menyimpan posisi ke SharedPreferences, tidak hanya saat drag berhenti.

### 🗒️ File Added

- Tombol `btnPortrait` / `btnLandscape` di `activity_main.xml`

### ✏️️ File Changed

- app/build.gradle
- app/src/main/res/layout/activity_main.xml
- app/src/main/java/exp/ftxt/ui/PositionController.java
- CHANGELOG.md
- app/src/main/assets/CHANGELOG.txt

### 🔢 Version

- versionCode: 90 → 91
- versionName: 2.5.1.40.1 → 2.5.1.41.0

---

## [2.5.1.40.1] - 2026-05-29

### 🐞 Bug Fixes

- **XyPad Tidak Bisa Di-drag**: `NestedScrollView` meng-intercept sentuhan pada `XyPadView` sehingga dot tidak bisa digerakkan. Ditambahkan `requestDisallowInterceptTouchEvent(true)` pada ACTION_DOWN dan `false` pada ACTION_UP/CANCEL.

- **Handler Memory Leak**: `Handler` pada `PositionController` (repeatHandler untuk D-pad dan holdHandler untuk long-press hapus preset) tidak pernah dibersihkan saat Activity di-destroy. Ditambahkan metode `cleanup()` yang dipanggil di `MainActivity.onDestroy()`.

## [2.5.1.40.0] - 2026-05-29

### ✨ Fitur Baru

- **Hapus Preset Posisi**: Tahan preset 2 detik di dialog Muat Preset untuk memunculkan dialog konfirmasi hapus dengan tombol [Ya] [Batal].

### 🐞 Bug Fixes

- **Sidebar Tidak Responsif**: Item sidebar yang dibuat ulang secara programmatic oleh `rebuildSidebar()` tidak memiliki `OnClickListener`, sehingga tidak bisa dipilih. Ditambahkan listener yang meng-handle navigasi panel, update title, simpan state, dan tutup drawer.

- **ScrollView pada Panel**: Panel Floating Text dan FPS Display tidak bisa di-scroll saat konten melebihi layar (misal konfigurasi Background/Shadow terbuka). Ditambahkan `ScrollView` dengan `fillViewport="true"` pada kedua panel.

### ♻️️ Perubahan Fitur

- **Tata Letak Kontrol Posisi**: XY Pad diubah menjadi vertikal (250dp, `layout_weight="1"`) dan D-Pad dipindah ke samping kanan dalam satu baris horizontal, mengisi ruang kosong yang sebelumnya terbuang.

### ✏️️ File Changed

- app/build.gradle
- app/src/main/java/exp/ftxt/MainActivity.java
- app/src/main/java/exp/ftxt/ui/PositionController.java
- app/src/main/res/layout/activity_main.xml
- CHANGELOG.md
- app/src/main/assets/CHANGELOG.txt

### 🔢 Version

- versionCode: 88 → 89
- versionName: 2.5.1.39.2 → 2.5.1.40.0

---

## [2.5.1.39.2] - 2026-05-29

### 🐞 Bug Fixes

- **Sidebar Tidak Responsif**: Item sidebar yang dibuat ulang secara programmatic oleh `rebuildSidebar()` tidak memiliki `OnClickListener`, sehingga tidak bisa dipilih. Ditambahkan listener yang meng-handle navigasi panel, update title, simpan state, dan tutup drawer.

### ♻️️ Perubahan Fitur

- **Tata Letak Kontrol Posisi**: XY Pad diubah menjadi vertikal (250dp, `layout_weight="1"`) dan D-Pad dipindah ke samping kanan dalam satu baris horizontal, mengisi ruang kosong yang sebelumnya terbuang.

### ✏️️ File Changed

- app/build.gradle
- app/src/main/java/exp/ftxt/MainActivity.java
- app/src/main/res/layout/activity_main.xml
- CHANGELOG.md
- app/src/main/assets/CHANGELOG.txt

### 🔢 Version

- versionCode: 87 → 88
- versionName: 2.5.1.39.1 → 2.5.1.39.2

---

## [2.5.1.39.1] - 2026-05-29

### ✨ Fitur Baru

- **Classic Color Wheel (Full Disk)**: Color Picker ARGB Sliders kini dilengkapi Color Wheel penuh dengan indikator crosshair. Saturation mencakup seluruh area wheel (full disk), bukan hanya area dalam.

- **Two-Way Synchronization**: Pergerakan crosshair di Color Wheel otomatis menyesuaikan slider R, G, B secara real-time, dan sebaliknya. Dilengkapi flag `isUpdating` untuk mencegah infinite loop.

### ✏️️ File Changed

- app/build.gradle
- app/src/main/java/exp/ftxt/shared/color/HSVColorPickerView.java
- app/src/main/java/exp/ftxt/shared/ui/ColorPickerDialog.java
- app/src/main/res/layout/dialog_hsv_color_picker.xml
- CHANGELOG.md
- app/src/main/assets/CHANGELOG.txt

### 🔢 Version

- versionCode: 86 → 87
- versionName: 2.5.1.39.0 → 2.5.1.39.1

---

## [2.5.1.39.0] - 2026-05-29

### 🚮️ Fitur Dihapus

- **Hapus Sistem Grup Sidebar**: Navigasi sidebar tidak lagi menggunakan grup (Overlay, Fitur). Semua item ditampilkan flat dalam satu daftar. Tombol "Tambah Grup" dihapus. Grup kustom yang tersimpan otomatis dimigrasi ke format flat.

- **Hapus Tombol "Hapus" Sidebar**: Tombol dan mode hapus item di footer sidebar dihapus untuk menyederhanakan navigasi.

### ✏️️ File Changed

- app/build.gradle
- app/src/main/res/layout/drawer_content.xml
- app/src/main/java/exp/ftxt/MainActivity.java
- CHANGELOG.md
- app/src/main/assets/CHANGELOG.txt

### 🔢 Version

- versionCode: 85 → 86
- versionName: 2.4.1.38.2 → 2.5.1.39.0

---

## [2.4.1.38.2] - 2026-05-29

### 🔧 Optimasi & Penyesuaian

- **Hapus Entry Dokumentasi dari Sidebar**: Navigasi "Dokumentasi" dihapus dari drawer kiri karena sudah ada akses "Lihat Dokumentasi" melalui popup Pengaturan (ikon gear).

- **Hapus module files deprecated**: Folder `modules/` yang sudah tidak dipakai (digantikan `features/`) dihapus dari filesystem.

### ✏️️ File Changed

- app/build.gradle
- app/src/main/res/layout/drawer_content.xml
- app/src/main/res/values/strings.xml
- app/src/main/java/exp/ftxt/MainActivity.java
- app/src/main/res/menu/drawer_menu.xml
- CHANGELOG.md
- app/src/main/assets/CHANGELOG.txt

### 🔥️ File Removed

- app/src/main/java/exp/ftxt/modules/text/TextConfig.java
- app/src/main/java/exp/ftxt/modules/text/TextModule.java
- app/src/main/java/exp/ftxt/modules/fps/FpsConfig.java
- app/src/main/java/exp/ftxt/modules/fps/FpsModule.java

### 🔢 Version

- versionCode: 84 → 85
- versionName: 2.4.1.38.1 → 2.4.1.38.2

---

## [2.4.1.38.1] - 2026-05-28

### ♻️️ Perubahan Fitur

- **Refactor Package Structure**: Rename folder `modules` → `features` untuk naming yang lebih konsisten dan semantik. Semua import statements dan dokumentasi internal telah diperbarui.

### ✏️️ File Changed

- app/build.gradle
- app/src/main/java/exp/ftxt/MainActivity.java
- app/src/main/java/exp/ftxt/core/FloatingService.java
- app/src/main/java/exp/ftxt/ui/PositionController.java
- app/src/main/java/exp/ftxt/ui/FpsPanelController.java
- app/src/main/java/exp/ftxt/ui/TextPanelController.java
- app/src/main/java/exp/ftxt/shared/ui/OverlayDragHandler.java
- STRUKTUR.md
- app/src/main/assets/STRUKTUR.txt
- CHANGELOG.md
- app/src/main/assets/CHANGELOG.txt

### 🗒️ File Added

- app/src/main/java/exp/ftxt/features/text/TextConfig.java
- app/src/main/java/exp/ftxt/features/text/TextModule.java
- app/src/main/java/exp/ftxt/features/fps/FpsConfig.java
- app/src/main/java/exp/ftxt/features/fps/FpsModule.java

### 🔥️ File Removed

- app/src/main/java/exp/ftxt/modules/text/TextConfig.java (deprecated)
- app/src/main/java/exp/ftxt/modules/text/TextModule.java (deprecated)
- app/src/main/java/exp/ftxt/modules/fps/FpsConfig.java (deprecated)
- app/src/main/java/exp/ftxt/modules/fps/FpsModule.java (deprecated)

### 🔢 Version

- versionCode: 83 → 84
- versionName: 2.4.1.38.0 → 2.4.1.38.1

---

## [2.4.1.38.0] - 2026-05-28

### 🚮️ Fitur Dihapus

- **Hapus "Tambah Item" Per-Grup**: Setiap grup kustom tidak lagi memiliki EditText + Button "+" untuk menambah item. Item hanya bisa ditambah melalui mekanisme internal (drag antar grup atau restore state).

### ✨ Fitur Baru

- **Drag Cross-Container**: Item sidebar kini bisa di-drag bebas antar grup (Overly, Fitur, grup kustom). Pindahkan item dari satu grup ke grup lain dengan long-press dan drop.
- **Mode Hapus**: Tombol "Hapus" di sidebar mengaktifkan mode delete. Setiap item mendapat CheckBox (☐/☑). Pilih item yang ingin dihapus, lalu tekan [Hapus] di footer untuk eksekusi. [Batal] untuk membatalkan.
- **Penyimpanan State Penuh**: Semua state sidebar (grup built-in + kustom, urutan item, status collapse/expand) disimpan dan dipulihkan sepenuhnya.

### ♻️️ Perubahan Fitur

- **Drag-to-Reorder**: Sekarang mendukung cross-container drag. Item bisa dipindah antar grup manapun.
- **rebuildSidebar()**: Method baru untuk membangun ulang seluruh sidebar dari state JSON. Dipanggil saat `onCreate()` dan setelah operasi penghapusan.
- **saveSidebarState() / loadSidebarState()**: Menggantikan `saveCustomGroups()` / `loadCustomGroups()`. Menyimpan state penuh semua grup (built-in + kustom).
- **Collapse/Expand Built-in**: Grup Overlay dan Fitur kini dapat di-collapse/expand (sebelumnya hanya grup kustom yang bisa).

### ✏️️ File Changed

- app/build.gradle
- app/src/main/java/exp/ftxt/MainActivity.java
- app/src/main/res/layout/drawer_content.xml
- CHANGELOG.md
- app/src/main/assets/CHANGELOG.txt
- README.md
- app/src/main/assets/README.txt

### 🔢 Version

- versionCode: 82 → 83
- versionName: 2.3.1.37.1 → 2.4.1.38.0

---

## [2.3.1.37.0] - 2026-05-28

### ✨ Fitur Baru

- **Tambah Grup**: Tombol "+ Tambah Grup" di sidebar untuk membuat grup kustom baru. Setiap grup memiliki header collapsible dan bisa ditambahi item sendiri.
- **Drag-to-Reorder**: Long-press item sidebar (built-in maupun kustom) lalu drag ke posisi lain untuk mengurutkan ulang.
- **Penyimpanan Grup**: Grup kustom dan urutannya disimpan otomatis ke SharedPreferences dan dipulihkan saat aplikasi dibuka kembali.
- **Sidebar Collapsible**: NavigationView diganti dengan drawer custom. Grup "Overlay" (Floating Text, FPS Display) dan "Fitur" (Coming Soon) bisa di‑expand/collapse dengan toggle −/+.
- **Kategori Fitur**: 7 modul Coming Soon (Network, Battery, Clock, CPU, Crosshair, Watermark, Logo) dikelompokkan dalam grup "Fitur" yang bisa ditutup.

### ✏️️ File Changed

- app/build.gradle
- app/src/main/java/exp/ftxt/MainActivity.java
- app/src/main/res/layout/activity_main.xml
- app/src/main/res/layout/drawer_content.xml
- CHANGELOG.md
- app/src/main/assets/CHANGELOG.txt

### 🗒️ File Added

- `app/src/main/res/layout/drawer_content.xml`

### 🔢 Version

- versionCode: 79 → 81
- versionName: 2.3.1.36.0 → 2.3.1.37.0

---

## [2.3.1.35.1] - 2026-05-28

### 🔧 Optimasi & Penyesuaian

- **Tampilan Dokumentasi**: Entry dokumen diubah dari tombol (button) menjadi daftar teks dengan garis pemisah — lebih bersih, seperti daftar.
- **Toolbar Biru**: Toolbar SettingsActivity dan DocumentationActivity kini menggunakan warna biru `#2196F3` (sama seperti toolbar utama), bukan hitam.
- **Judul Toolbar**: SettingsActivity berganti judul dari "Pengaturan" menjadi "Konfigurasi".

### ✏️️ File Changed

- app/build.gradle
- app/src/main/res/layout/activity_documentation.xml
- app/src/main/res/layout/activity_settings.xml
- CHANGELOG.md
- app/src/main/assets/CHANGELOG.txt

### 🔢 Version

- versionCode: 78 → 79
- versionName: 2.3.1.35.0 → 2.3.1.35.1

---

## [2.3.1.35.0] - 2026-05-28

### ✨ Fitur Baru

- **DocumentationActivity**: Antarmuka khusus dokumentasi dengan daftar 6 dokumen (README, CHANGELOG, PANDUAN, STRUKTUR, DEVELOPMENT, TENTANG) — bukan lagi dialog tengah, melainkan halaman penuh seperti Settings.
- **Drawer → Dokumentasi**: Entry "Dokumentasi" ditambahkan di navigation drawer (di luar grup module) untuk akses cepat tanpa melalui popup settings.

### ✏️️ File Changed

- app/build.gradle
- app/src/main/java/exp/ftxt/MainActivity.java
- app/src/main/res/menu/drawer_menu.xml
- app/src/main/res/values/strings.xml
- AndroidManifest.xml
- CHANGELOG.md
- app/src/main/assets/CHANGELOG.txt

### 🗒️ File Added

- `app/src/main/java/exp/ftxt/DocumentationActivity.java`
- `app/src/main/res/layout/activity_documentation.xml`

### 🔢 Version

- versionCode: 77 → 78
- versionName: 2.3.1.34.0 → 2.3.1.35.0

---

## [2.3.1.34.0] - 2026-05-28

### ✨ Fitur Baru

- **Konfirmasi Keluar**: Opsi checkbox di Konfigurasi — ketika aktif, tombol kembali harus ditekan dua kali untuk keluar dari aplikasi. Toast "Tekan kembali lagi untuk keluar" muncul pada tekan pertama.

### 🔧 Optimasi & Penyesuaian

- **Popup di Bawah Ikon**: Popup settings kini muncul sebagai dropdown di bawah ikon pengaturan (PopupMenu + Gravity.END), bukan dialog tengah.
- **Tutup Aplikasi Pindah ke Popup**: Tombol "Tutup Aplikasi" dipindah dari SettingsActivity ke popup settings, bisa diakses dari mana saja.

### ✏️️ File Changed

- app/build.gradle
- app/src/main/java/exp/ftxt/MainActivity.java
- app/src/main/java/exp/ftxt/SettingsActivity.java
- app/src/main/res/layout/activity_settings.xml
- app/src/main/res/values/strings.xml
- CHANGELOG.md
- app/src/main/assets/CHANGELOG.txt

### 🔢 Version

- versionCode: 76 → 77
- versionName: 2.3.1.33.2 → 2.3.1.34.0

---

## [2.3.1.33.2] - 2026-05-28

### 🔧 Optimasi & Penyesuaian

- **Popup Settings**: Tombol pengaturan (gear) kini menampilkan popup dengan dua opsi: "Konfigurasi" (izin + Tutup Aplikasi) dan "Lihat Dokumentasi" (6 dokumen).
- **Dokumentasi via Popup**: Dokumentasi dipindah dari SettingsActivity ke popup di MainActivity — akses lebih cepat tanpa buka halaman baru.
- **SettingsActivity Ringkas**: Hanya berisi Konfigurasi (izin overlay/notifikasi/baterai + Tutup Aplikasi). Tombol dokumentasi dihapus.

### ✏️️ File Changed

- app/build.gradle
- app/src/main/java/exp/ftxt/MainActivity.java
- app/src/main/java/exp/ftxt/SettingsActivity.java
- app/src/main/res/layout/activity_settings.xml
- CHANGELOG.md
- app/src/main/assets/CHANGELOG.txt

### 🔢 Version

- versionCode: 75 → 76
- versionName: 2.3.1.33.1 → 2.3.1.33.2

---

## [2.3.1.33.1] - 2026-05-28

### 🔧 Optimasi & Penyesuaian

- **Dokumentasi Dipisah**: README.md dipecah menjadi 4 file terpisah untuk modularitas:
  - `STRUKTUR.md` — Struktur project & deskripsi file
  - `PANDUAN.md` — Panduan penggunaan lengkap
  - `DEVELOPMENT.md` — Info teknis, environment, versioning
  - `TENTANG.md` — Lisensi, author, support
- README.md kini ringkas dengan ringkasan fitur dan referensi ke file dokumentasi terkait.
- Semua file memiliki versi .md (root) dan .txt (assets) yang disinkronkan secara manual.
- SettingsActivity mendapat 4 tombol baru untuk membaca file dokumentasi baru.
- Gradle task `syncDocs` diperbarui untuk menyertakan semua file dokumentasi baru.

### ✏️️ File Changed

- CHANGELOG.md
- README.md
- app/build.gradle
- app/src/main/java/exp/ftxt/SettingsActivity.java
- app/src/main/res/layout/activity_settings.xml
- app/src/main/assets/CHANGELOG.txt
- app/src/main/assets/README.txt

### 🗒️ File Added

- `STRUKTUR.md` + `app/src/main/assets/STRUKTUR.txt`
- `PANDUAN.md` + `app/src/main/assets/PANDUAN.txt`
- `DEVELOPMENT.md` + `app/src/main/assets/DEVELOPMENT.txt`
- `TENTANG.md` + `app/src/main/assets/TENTANG.txt`

### 🔢 Version

- versionCode: 74 → 75
- versionName: 2.3.1.33.0 → 2.3.1.33.1

---

## [2.3.1.33.0] - 2026-05-28

### ✨ Fitur Baru

- **Kontrol Posisi 3-in-1**: Tambah sistem kontrol posisi untuk Floating Text dengan 3 metode input sinkron: Slider X/Y, D-Pad (↑↓←→, tahan untuk repeat), dan XY Pad (drag area 2D). State posisi menggunakan nilai float 0.0–1.0 yang dibagikan.
- **Shared State**: Semua kontrol membaca/menulis ke state `TextConfig.posX`/`posY` yang sama dengan guard `isUpdating` untuk hindari infinite loop.
- **Position Migration**: Migrasi posisi dari pixel absolut (`text_x`/`text_y`) ke persentase layar (`text_pos_x`/`text_pos_y`) dengan backward compatibility otomatis.
- **XyPadView**: Custom View 2D drag area di `shared/ui/XyPadView.java`.
- **PositionController**: Controller modular untuk semua kontrol posisi di `ui/PositionController.java`.
- **Potret/Landscape Auto-Config**: Posisi overlay disimpan terpisah untuk mode portrait dan landscape, otomatis berganti saat orientasi berubah.
- **Preset Posisi**: Simpan/load posisi favorit (max 10 preset) dengan nama kustom.
- **Reset Posisi**: Tombol untuk mengembalikan posisi ke tengah layar (0.5, 0.5).

### ✏️️ File Changed

- app/build.gradle
- app/src/main/java/exp/ftxt/modules/text/TextConfig.java
- app/src/main/java/exp/ftxt/modules/text/TextModule.java
- app/src/main/java/exp/ftxt/core/FloatingService.java
- app/src/main/res/layout/activity_main.xml
- app/src/main/java/exp/ftxt/ui/TextPanelController.java
- app/src/main/java/exp/ftxt/ui/PositionController.java
- CHANGELOG.md
- app/src/main/assets/CHANGELOG.txt
- README.md
- app/src/main/assets/README.txt

### 🗒️ File Added

- `app/src/main/java/exp/ftxt/shared/ui/XyPadView.java`
- `app/src/main/java/exp/ftxt/ui/PositionController.java`

### 🔢 Version

- versionCode: 73 → 74
- versionName: 2.3.1.32.7 → 2.3.1.33.0

---

## [2.3.1.32.5] - 2026-05-28

### 🐛 Perbaikan

- **Shadow config flicker**: Tambah `android:visibility="gone"` pada `shadowConfigText` dan `shadowConfigFps` agar tidak tampil sesaat sebelum controller set visibility.
- **Panel reset saat ganti tema**: Simpan panel terpilih (`nav_selected_item`) ke SharedPreferences agar tidak kembali ke Floating Text setelah `recreate()`.

### 🧹 Bersih-Bersih

- **Hapus `nav_exit`**: String `nav_exit` yang sudah tidak dipakai (Force Close sudah dipindah ke Settings).

### ✏️️ File Changed

- app/build.gradle
- app/src/main/res/layout/activity_main.xml
- app/src/main/res/values/strings.xml
- app/src/main/java/exp/ftxt/MainActivity.java
- CHANGELOG.md
- app/src/main/assets/CHANGELOG.txt
- README.md
- app/src/main/assets/README.txt

### 🔢 Version

- versionCode: 70 → 71
- versionName: 2.3.1.32.4 → 2.3.1.32.5

---

## [2.3.1.32.4] - 2026-05-28

### 🔧 Optimasi & Penyesuaian

- **Slider Label Editor Shared Utility**: Ekstrak `showSliderEditor()` dan `showOffsetEditor()` ke `shared/ui/SliderLabelEditor.java` untuk menghilangkan duplikasi kode antara `TextPanelController` dan `FpsPanelController`.

### 🗒️ File Added

- `app/src/main/java/exp/ftxt/shared/ui/SliderLabelEditor.java`

### ✏️️ File Changed

- app/build.gradle
- app/src/main/java/exp/ftxt/ui/TextPanelController.java
- app/src/main/java/exp/ftxt/ui/FpsPanelController.java
- CHANGELOG.md
- app/src/main/assets/CHANGELOG.txt
- README.md
- app/src/main/assets/README.txt

### 🔢 Version

- versionCode: 69 → 70
- versionName: 2.3.1.32.3 → 2.3.1.32.4

---

## [2.3.1.32.3] - 2026-05-28

### ✨ Fitur Baru

- **Background Margin & Radius**: Tambah slider margin (0–30 px) dan radius/rounded corner (0–50 px) pada konfigurasi background untuk modul Floating Text dan FPS Display.
- **Direct Label Edit**: Klik label margin dan radius untuk mengedit nilai langsung via dialog input.

### ✏️️ File Changed

- app/build.gradle
- app/src/main/java/exp/ftxt/modules/text/TextConfig.java
- app/src/main/java/exp/ftxt/modules/fps/FpsConfig.java
- app/src/main/java/exp/ftxt/modules/text/TextModule.java
- app/src/main/java/exp/ftxt/modules/fps/FpsModule.java
- app/src/main/java/exp/ftxt/shared/ui/ShadowTextView.java
- app/src/main/res/layout/activity_main.xml
- app/src/main/java/exp/ftxt/ui/TextPanelController.java
- app/src/main/java/exp/ftxt/ui/FpsPanelController.java
- README.md
- app/src/main/assets/README.txt
- CHANGELOG.md
- app/src/main/assets/CHANGELOG.txt

### 🔢 Version

- versionCode: 68 → 69
- versionName: 2.3.1.32.2 → 2.3.1.32.3

---

## [2.3.1.32.2] - 2026-05-28

### 🔧 Optimasi & Penyesuaian

- **Android SplashScreen API**: Implementasi SplashScreen API resmi Android menggunakan icon launcher aplikasi. Splash screen menampilkan icon app dengan background warna tema, tanpa custom splash activity atau fake loading delay.

### 🗒️ File Added

- app/src/main/res/values/themes.xml
- app/src/main/res/values-v31/themes.xml
- app/src/main/res/drawable/splash_screen.xml

### ✏️️ File Changed

- app/build.gradle
- app/src/main/AndroidManifest.xml
- app/src/main/java/exp/ftxt/MainActivity.java
- CHANGELOG.md
- app/src/main/assets/CHANGELOG.txt

### 🔢 Version

- versionCode: 67 → 68
- versionName: 2.3.1.32.1 → 2.3.1.32.2

---

## [2.3.1.32.1] - 2026-05-28

### 🔧 Optimasi & Penyesuaian

- **Offset Range -60 hingga 60**: Semua offset (Background X/Y dan Shadow X/Y) di panel Floating Text dan FPS Display sekarang memiliki range -60 hingga 60, dengan posisi default 0 di tengah. SeekBar memetakan progress 0-120 ke offset -60..60.
- **Default Shadow Offset**: Nilai default shadow offset X/Y diubah dari 3 menjadi 0.

### ✏️️ File Changed

- app/build.gradle
- app/src/main/res/layout/activity_main.xml
- app/src/main/java/exp/ftxt/shared/ui/ShadowConfig.java
- app/src/main/java/exp/ftxt/ui/TextPanelController.java
- app/src/main/java/exp/ftxt/ui/FpsPanelController.java
- CHANGELOG.md
- app/src/main/assets/CHANGELOG.txt

### 🔢 Version

- versionCode: 66 → 67
- versionName: 2.3.1.32.0 → 2.3.1.32.1

---

## [2.3.1.32.0] - 2026-05-28

### ✨ Fitur Baru

- **Label Slider Menampilkan Nilai**: Semua label slider di panel Floating Text dan FPS Display kini menampilkan nilai numerik terkini (contoh: "Ukuran Teks: 20", "Offset X: 0").
- **Edit Nilai Slider Manual**: Klik label slider untuk mengedit nilai numerik langsung via dialog input—slider menyesuaikan otomatis.

### ✏️️ File Changed

- app/build.gradle
- app/src/main/res/layout/activity_main.xml
- app/src/main/java/exp/ftxt/ui/TextPanelController.java
- app/src/main/java/exp/ftxt/ui/FpsPanelController.java
- README.md
- app/src/main/assets/README.txt
- CHANGELOG.md
- app/src/main/assets/CHANGELOG.txt

### 🔢 Version

- versionCode: 65 → 66
- versionName: 2.3.1.31.0 → 2.3.1.32.0

---

## [2.3.1.31.0] - 2026-05-27

### ✨ Fitur Baru

- **Hanya Tampilkan Nilai FPS**: Tambah toggle di panel FPS untuk menyembunyikan teks "FPS" dan hanya menampilkan angka.

### ✏️️ File Changed

- app/build.gradle
- app/src/main/res/layout/activity_main.xml
- app/src/main/java/exp/ftxt/modules/fps/FpsConfig.java
- app/src/main/java/exp/ftxt/modules/fps/FpsModule.java
- app/src/main/java/exp/ftxt/core/FloatingService.java
- app/src/main/java/exp/ftxt/ui/FpsPanelController.java
- README.md
- app/src/main/assets/README.txt
- CHANGELOG.md
- app/src/main/assets/CHANGELOG.txt

### 🔢 Version

- versionCode: 64 → 65
- versionName: 2.3.1.30.0 → 2.3.1.31.0

---

## [2.3.1.30.0] - 2026-05-27

### ✨ Fitur Baru

- **Versi di Sidebar**: Tambah nomor versi terkini di samping nama aplikasi di header navigation drawer (contoh: FTxT v2.3.1.30.0).

### ✏️️ File Changed

- app/build.gradle
- app/src/main/res/layout/nav_header.xml
- app/src/main/java/exp/ftxt/MainActivity.java
- README.md
- app/src/main/assets/README.txt
- CHANGELOG.md
- app/src/main/assets/CHANGELOG.txt

### 🔢 Version

- versionCode: 63 → 64
- versionName: 2.3.1.29.0 → 2.3.1.30.0

---

## [2.3.1.29.0] - 2026-05-27

### ✨ Fitur Baru

- **Edit HEX Manual**: Tambah ikon sunting di samping nilai HEX untuk mengedit warna secara manual via input teks.
- **Edit ARGB Langsung**: Klik label R, G, B, A untuk mengedit nilai numerik—slider menyesuaikan otomatis.
- **Salin Nilai Warna**: Tekan lama nilai HEX, HSV, atau ARGB untuk menyalin ke clipboard.

### 🗒️ File Added

- `app/src/main/res/drawable/ic_edit.xml`

### ✏️️ File Changed

- app/build.gradle
- app/src/main/res/layout/dialog_hsv_color_picker.xml
- app/src/main/java/exp/ftxt/shared/ui/ColorPickerDialog.java
- README.md
- app/src/main/assets/README.txt
- CHANGELOG.md
- app/src/main/assets/CHANGELOG.txt

### 🔢 Version

- versionCode: 61 → 63
- versionName: 2.3.1.27.0 → 2.3.1.29.0

---

## [2.3.1.27.0] - 2026-05-27

### ✨ Fitur Baru

- **Crosshair & Watermark**: Tambah entry modul Crosshair dan Watermark di navigation drawer (Coming Soon).
- **Hapus Temperature**: Hapus entry modul Temperature dari navigation drawer.

### 🗒️ File Added

- `app/src/main/java/exp/ftxt/modules/crosshair/`
- `app/src/main/java/exp/ftxt/modules/watermark/`

### ✏️️ File Changed

- app/build.gradle
- app/src/main/res/menu/drawer_menu.xml
- app/src/main/res/values/strings.xml
- app/src/main/java/exp/ftxt/MainActivity.java
- README.md
- app/src/main/assets/README.txt
- CHANGELOG.md
- app/src/main/assets/CHANGELOG.txt

### 🔥️ File Removed

- `app/src/main/java/exp/ftxt/modules/temp/`

### 🔢 Version

- versionCode: 60 → 61
- versionName: 2.3.1.26.3 → 2.3.1.27.0

---

## [2.3.1.26.3] - 2026-05-27

### 🐞 Bug Fixes

- **Dokumentasi Color Model**: Perbaiki dokumentasi model warna di README dari HSV (lama) menjadi ARGB sesuai implementasi color picker saat ini.

### ✏️️ File Changed

- README.md
- app/src/main/assets/README.txt
- CHANGELOG.md
- app/src/main/assets/CHANGELOG.txt
- app/build.gradle

### 🔢 Version

- versionCode: 59 → 60
- versionName: 2.3.1.26.2 → 2.3.1.26.3

---

## [2.3.1.26.2] - 2026-05-27

### 🐞 Bug Fixes

- **Switch Modul Tidak Minta Izin Mandiri**: Switch Floating Text dan FPS Display sekarang hanya mati diam-diam tanpa meminta izin overlay. Izin harus dinyalakan dulu lewat Settings > Akses Izin > Izin Overlay.

### ♻️️ Lifecycle Changes

- `TextPanelController.java` — hapus `PermissionHelper.requestOverlayPermission()` dari switch handler.
- `FpsPanelController.java` — hapus `PermissionHelper.requestOverlayPermission()` dari switch handler.

### ✏️️ File Changed

- app/build.gradle
- app/src/main/java/exp/ftxt/ui/TextPanelController.java
- app/src/main/java/exp/ftxt/ui/FpsPanelController.java
- CHANGELOG.md
- app/src/main/assets/CHANGELOG.txt

### 🔢 Version

- versionCode: 58 → 59
- versionName: 2.3.1.26.1 → 2.3.1.26.2

---

## [2.3.1.26.1] - 2026-05-27

### 🐞 Bug Fixes

- **Overlay Permission Gate**: Switch Floating Text dan FPS Display sekarang menolak menyala jika izin Overlay belum diberikan. Switch otomatis kembali ke OFF dan meminta izin.

### ♻️️ Lifecycle Changes

- `TextPanelController.java` — tambah cek `PermissionHelper.hasOverlayPermission()` sebelum switch ON.
- `FpsPanelController.java` — tambah cek `PermissionHelper.hasOverlayPermission()` sebelum switch ON.

### ✏️️ File Changed

- app/build.gradle
- app/src/main/java/exp/ftxt/ui/TextPanelController.java
- app/src/main/java/exp/ftxt/ui/FpsPanelController.java
- CHANGELOG.md
- app/src/main/assets/CHANGELOG.txt

### 🔢 Version

- versionCode: 57 → 58
- versionName: 2.3.1.26.0 → 2.3.1.26.1

---

## [2.3.1.26.0] - 2026-05-27

### ✨ Fitur Baru

- **Akses Izin di Pengaturan**: Toggle switch untuk semua izin aplikasi (Overlay, Notifikasi, Optimasi Baterai) di halaman Settings, di atas CHANGELOG/README.
  - Izin Overlay: buka halaman izin overlay sistem.
  - Izin Notifikasi: minta izin runtime (Android 13+).
  - Optimasi Baterai: buka halaman nonaktifkan optimasi baterai.
  - Status switch otomatis sinkron saat halaman dibuka/kembali.

### ♻️️ Lifecycle Changes

- `activity_settings.xml` — tambah 3 switch + label di atas tombol CHANGELOG.
- `SettingsActivity.java` — bind switch, listener permission request, `updatePermissionSwitches()` di `onResume()`.

### ✏️️ File Changed

- app/build.gradle
- app/src/main/java/exp/ftxt/SettingsActivity.java
- app/src/main/res/layout/activity_settings.xml
- CHANGELOG.md
- README.md
- app/src/main/assets/CHANGELOG.txt
- app/src/main/assets/README.txt

### 🔢 Version

- versionCode: 56 → 57
- versionName: 2.3.1.25.0 → 2.3.1.26.0

---

## [2.3.1.25.0] - 2026-05-27

### ✨ Fitur Baru

- **Force Close**: Tombol "Tutup Aplikasi" dipindah ke SettingsActivity (di bawah CHANGELOG/README, pojok kiri bawah).
  - Hapus `nav_exit` dari navigation drawer.
  - `forceClose()`: stop service → destroy overlay → `finishAffinity()` → `System.exit(0)`.
  - Service berhenti total, overlay hilang dari layar, proses aplikasi mati.

### ♻️️ Lifecycle Changes

- `activity_settings.xml` — tambah Button `exitButton` dengan layout_weight push ke bawah.
- `SettingsActivity.java` — handler `exitButton` panggil `forceClose()`.
- `drawer_menu.xml` — hapus item `nav_exit`.
- `MainActivity.java` — hapus handler `nav_exit` dari drawer listener.

### ✏️️ File Changed

- app/build.gradle
- app/src/main/java/exp/ftxt/SettingsActivity.java
- app/src/main/java/exp/ftxt/MainActivity.java
- app/src/main/res/layout/activity_settings.xml
- app/src/main/res/menu/drawer_menu.xml
- CHANGELOG.md
- README.md
- app/src/main/assets/CHANGELOG.txt
- app/src/main/assets/README.txt

### 🔢 Version

- versionCode: 55 → 56
- versionName: 2.3.1.24.0 → 2.3.1.25.0

---

## [2.3.1.24.0] - 2026-05-27

### ✨ Fitur Baru

- **Auto-Start & Permission**: Aplikasi langsung minta izin dan auto-start overlay saat dibuka.
  - `onResume()` cek izin overlay, notif, dan baterai secara otomatis.
  - Jika izin overlay sudah diberikan, service langsung start tanpa perlu toggle manual.
  - `FloatingService.onCreate()` restore overlay text dari pref `text_overlay_on`.
  - Kembali dari Settings izin → `onResume()` lanjutkan auto-start.

- **Kunci Posisi Default ON**: `touchPassthrough` sekarang default `true`.
  - `TextConfig.touchPassthrough = true` (sebelumnya `false`).
  - `FpsConfig.touchPassthrough = true` (sebelumnya `false`).
  - `TextPanelController.loadConfig()` restore switch state dari pref `text_lock`.
  - Nilai default untuk `text_lock` dan `fps_lock` diubah jadi `true`.

### ♻️️ Lifecycle Changes

- `TextPanelController.loadConfig()` — restore `overlaySwitch` dan `touchPassthroughSwitch` dari saved prefs.
- `TextPanelController.touchPassthroughSwitch` listener — simpan pref `text_lock`.
- `TextPanelController.autoStart()` — method baru untuk start service dari MainActivity.

### ✏️️ File Changed

- app/build.gradle
- app/src/main/java/exp/ftxt/MainActivity.java
- app/src/main/java/exp/ftxt/modules/text/TextConfig.java
- app/src/main/java/exp/ftxt/modules/fps/FpsConfig.java
- app/src/main/java/exp/ftxt/ui/TextPanelController.java
- CHANGELOG.md
- README.md
- app/src/main/assets/CHANGELOG.txt
- app/src/main/assets/README.txt

### 🔢 Version

- versionCode: 54 → 55
- versionName: 2.3.1.23.0 → 2.3.1.24.0

---

## [2.3.1.23.0] - 2026-05-27

### ✨ Fitur Baru

- **Background Ukuran & Offset**: Tambah kontrol ukuran (padding) dan posisi X/Y background.
  - `bgPadding` — slider ukuran background (0–80px), mengatur jarak tepi background ke teks.
  - `bgOffsetX` — slider offset horizontal background (0–60px).
  - `bgOffsetY` — slider offset vertikal background (0–60px).
  - Background digambar langsung di `onDraw()` via `ShadowTextView` dengan canvas translate.

### ♻️️ Lifecycle Changes

- `ShadowTextView.java` — draw background rect langsung di `onDraw()` dengan Paint; pakai `canvas.translate()` untuk offset.
- Hapus `setPadding()` hardcoded dari `TextModule.createOverlay()` dan `FpsModule.start()` — padding diatur via `applyBackground()`.
- Hapus `view.setBackgroundColor()` — background digambar manual via `ShadowTextView.onDraw()`.

### ✏️️ File Changed

- app/build.gradle
- app/src/main/java/exp/ftxt/MainActivity.java
- app/src/main/java/exp/ftxt/modules/text/TextConfig.java
- app/src/main/java/exp/ftxt/modules/text/TextModule.java
- app/src/main/java/exp/ftxt/modules/fps/FpsConfig.java
- app/src/main/java/exp/ftxt/modules/fps/FpsModule.java
- app/src/main/java/exp/ftxt/shared/ui/ShadowTextView.java
- app/src/main/java/exp/ftxt/ui/TextPanelController.java
- app/src/main/java/exp/ftxt/ui/FpsPanelController.java
- app/src/main/res/layout/activity_main.xml
- CHANGELOG.md
- README.md
- app/src/main/assets/CHANGELOG.txt
- app/src/main/assets/README.txt

### 🔢 Version

- versionCode: 53 → 54
- versionName: 2.3.1.22.0 → 2.3.1.23.0

---

## [2.3.1.22.0] - 2026-05-27

### ✨ Fitur Baru

- **Background Config**: Konfigurasi background overlay terpisah dari shadow.
  - Background bisa diaktifkan/dimatikan via switch sendiri.
  - Pilih warna background dengan color picker (ARGB, alpha slider tersedia).
  - Tidak lagi pakai hardcoded black semi-transparent (`0x88000000`).
  - Berlaku untuk Floating Text dan FPS Display.

### 🚮️ Fitur Dihapus

- **Hardcoded Background**: Hapus background hitam otomatis dari `OverlayShadow.apply()`.
  - Background shadow dulu otomatis `0x88000000` saat shadow diaktifkan.
  - Sekarang background dan shadow adalah fitur terpisah.

### ♻️️ Lifecycle Changes

- `OverlayShadow.java` tidak lagi set background color — hanya handle elevation.
- Background diterapkan langsung via `view.setBackgroundColor()` di module masing-masing.
- `TextModule.updateBackground()` / `FpsModule.updateBackground()` — method baru.
- `FloatingService.updateTextBackgroundStatic()` / `updateFpsBackgroundStatic()` — delegate baru.

### ✏️️ File Changed

- app/build.gradle
- app/src/main/java/exp/ftxt/MainActivity.java
- app/src/main/java/exp/ftxt/modules/text/TextConfig.java
- app/src/main/java/exp/ftxt/modules/text/TextModule.java
- app/src/main/java/exp/ftxt/modules/fps/FpsConfig.java
- app/src/main/java/exp/ftxt/modules/fps/FpsModule.java
- app/src/main/java/exp/ftxt/core/FloatingService.java
- app/src/main/java/exp/ftxt/shared/ui/OverlayShadow.java
- app/src/main/java/exp/ftxt/ui/TextPanelController.java
- app/src/main/java/exp/ftxt/ui/FpsPanelController.java
- app/src/main/res/layout/activity_main.xml
- CHANGELOG.md
- README.md
- app/src/main/assets/CHANGELOG.txt
- app/src/main/assets/README.txt

### 🔢 Version

- versionCode: 52 → 53
- versionName: 2.3.1.21.0 → 2.3.1.22.0

---

## [2.3.1.21.0] - 2026-05-27

### ✨ Fitur Baru

- **Alpha Slider**: Tambah slider Alpha/Transparansi (0–255) di dialog color picker RGB.
  - Kontrol penuh ARGB (Alpha, Red, Green, Blue).
  - Tampilan HEX jadi 8 digit (#AARRGGBB).
  - Tampilan RGB jadi ARGB (Alpha, R, G, B).

- **Kill App Button**: Tambah menu "Tutup Aplikasi" di navigation drawer.
  - Panggil `finishAffinity()` untuk menutup semua aktivitas dan menghentikan proses aplikasi.

### 🚮️ Fitur Dihapus

- **Shadow Opacity**: Hapus konfigurasi opacity shadow terpisah.
  - Sekarang alpha langsung diatur via color picker warna shadow.
  - `ShadowConfig.opacity` dan `getColorWithOpacity()` dihapus.
  - `ShadowTextView` pakai `shadowConfig.color` langsung.
  - Seekbar opacity shadow dihapus dari UI (Text & FPS).
  - Pref `shadow_opacity` / `fps_shadow_opacity` tidak lagi dimuat.

### ✏️️ File Changed

- app/build.gradle
- app/src/main/java/exp/ftxt/shared/ui/ShadowConfig.java
- app/src/main/java/exp/ftxt/shared/ui/ShadowTextView.java
- app/src/main/java/exp/ftxt/shared/ui/ColorPickerDialog.java
- app/src/main/res/layout/dialog_hsv_color_picker.xml
- app/src/main/res/layout/activity_main.xml
- app/src/main/res/menu/drawer_menu.xml
- app/src/main/res/values/strings.xml
- app/src/main/java/exp/ftxt/MainActivity.java
- app/src/main/java/exp/ftxt/ui/TextPanelController.java
- app/src/main/java/exp/ftxt/ui/FpsPanelController.java
- CHANGELOG.md
- README.md
- app/src/main/assets/CHANGELOG.txt
- app/src/main/assets/README.txt

### 🔢 Version

- versionCode: 51 → 52
- versionName: 2.3.1.20.0 → 2.3.1.21.0

---

## [2.3.1.20.0] - 2026-05-27

### 🔁️ Perubahan Fitur

- **Color Picker**: Circular HSV wheel diganti dengan RGB slider + color info display.
  - Menampilkan nama warna, HEX, HSV, RGB.
  - Kontrol R, G, B via 3 seekbar (0–255).
  - Live preview + color naming otomatis.
  - `ColorNameResolver.java` — utility untuk konversi warna ke nama.

### 🗒️ File Added

- `app/src/main/java/exp/ftxt/shared/color/ColorNameResolver.java`

### ✏️️ File Changed

- app/build.gradle
- app/src/main/java/exp/ftxt/shared/ui/ColorPickerDialog.java
- app/src/main/res/layout/dialog_hsv_color_picker.xml
- CHANGELOG.md
- README.md
- app/src/main/assets/CHANGELOG.txt
- app/src/main/assets/README.txt

### 🔥️ File Removed (de facto)

- `shared/color/HSVColorPickerView.java` — tidak lagi dipakai (code dipertahankan)
- `shared/color/ColorMath.java` — tidak lagi dipakai (code dipertahankan)

### 🔢 Version

- versionCode: 50 → 51
- versionName: 2.3.1.19.0 → 2.3.1.20.0

---

## [2.3.1.19.0] - 2026-05-27

### ✨ Fitur Baru

- **Shadow Config**: Konfigurasi shadow modular dengan Enable, Warna, Blur, X/Y Offset, Opacity.
  - `ShadowConfig.java` — class konfigurasi shadow reusable untuk semua modul.
  - `OverlayShadow.java` — apply background + elevation (text shadow via `ShadowTextView`).
  - `ShadowTextView.java` — custom TextView terapkan shadow di `onDraw()` setiap frame (reliable untuk overlay).
  - Shadow dapat dikonfigurasi terpisah per modul (Text & FPS).
  - Kontrol UI: Shadow switch → container config muncul (color button, opacity/blur/offset seekbars).

### 🗒️ File Added

- `app/src/main/java/exp/ftxt/shared/ui/ShadowConfig.java`
- `app/src/main/java/exp/ftxt/shared/ui/ShadowTextView.java`

### ✏️️ File Changed

- app/build.gradle
- app/src/main/assets/CHANGELOG.txt
- app/src/main/assets/README.txt
- CHANGELOG.md
- README.md
- app/src/main/res/layout/activity_main.xml
- app/src/main/java/exp/ftxt/MainActivity.java
- app/src/main/java/exp/ftxt/core/FloatingService.java
- app/src/main/java/exp/ftxt/modules/text/TextConfig.java
- app/src/main/java/exp/ftxt/modules/text/TextModule.java
- app/src/main/java/exp/ftxt/modules/fps/FpsConfig.java
- app/src/main/java/exp/ftxt/modules/fps/FpsModule.java
- app/src/main/java/exp/ftxt/shared/ui/OverlayShadow.java
- app/src/main/java/exp/ftxt/ui/TextPanelController.java
- app/src/main/java/exp/ftxt/ui/FpsPanelController.java

### 🔢 Version

- versionCode: 49 → 50
- versionName: 2.3.1.18.1 → 2.3.1.19.0

---

## [2.3.1.18.1] - 2026-05-27

### ♻️ Lifecycle Changes

- **Refactor modular (pemecahan file)**: 5 file besar dipecah menjadi file-file kecil terfokus.
  - `MainActivity` (381→247) → `TextPanelController` + `FpsPanelController` + `PermissionHelper`
  - `FloatingService` (201→196) → `NotificationHelper` + `WakeLockManager`
  - `TextModule` + `FpsModule` → shared `OverlayDragHandler` + `OverlayShadow`
  - `HSVColorPickerView` (242→169) → `ColorMath`
- Semua kode diberi komentar cross-reference ke file terkait (lokasi + fungsi).
- Aturan workflow: CHANGELOG.md + README.md (root) dan .txt (assets) wajib sinkron setiap update.

### 🗒️ File Added

- `app/src/main/java/exp/ftxt/ui/TextPanelController.java`
- `app/src/main/java/exp/ftxt/ui/FpsPanelController.java`
- `app/src/main/java/exp/ftxt/utils/PermissionHelper.java`
- `app/src/main/java/exp/ftxt/core/NotificationHelper.java`
- `app/src/main/java/exp/ftxt/core/WakeLockManager.java`
- `app/src/main/java/exp/ftxt/shared/ui/OverlayDragHandler.java`
- `app/src/main/java/exp/ftxt/shared/ui/OverlayShadow.java`
- `app/src/main/java/exp/ftxt/shared/color/ColorMath.java`

### ✏️️ File Changed

- app/build.gradle
- app/src/main/java/exp/ftxt/MainActivity.java
- app/src/main/java/exp/ftxt/core/FloatingService.java
- app/src/main/java/exp/ftxt/modules/text/TextModule.java
- app/src/main/java/exp/ftxt/modules/fps/FpsModule.java
- app/src/main/java/exp/ftxt/shared/color/HSVColorPickerView.java
- app/src/main/java/exp/ftxt/shared/ui/ColorPickerDialog.java
- app/src/main/java/exp/ftxt/modules/text/TextConfig.java
- app/src/main/java/exp/ftxt/modules/fps/FpsConfig.java
- app/src/main/java/exp/ftxt/SettingsActivity.java
- app/src/main/assets/AGENTS.txt
- app/src/main/assets/CHANGELOG.txt
- app/src/main/assets/README.txt

### 🔢 Version

- versionCode: 48 → 49
- versionName: 2.3.1.18.0 → 2.3.1.18.1

---

## [2.3.1.18.0] - 2026-05-26

### ✨ Fitur Baru

- Dialog Settings: tambah tombol −/+ untuk zoom ukuran teks (4–60sp), default 14sp.

### ✏️ File Changed

- app/build.gradle
- app/src/main/java/exp/ftxt/SettingsActivity.java

### 🔢 Version

- versionCode: 47 → 48
- versionName: 2.3.1.17.4 → 2.3.1.18.0

---

## [2.3.1.17.4] - 2026-05-26

### 🔧 Optimasi & Penyesuaian

- Floating Text size range diperluas: 1–150 sp (sebelumnya 5–120).
- FPS Display size range diperluas: 5–140 sp (sebelumnya 5–120).

### ✏️ File Changed

- app/build.gradle
- app/src/main/java/exp/ftxt/MainActivity.java
- app/src/main/res/layout/activity_main.xml

### 🔢 Version

- versionCode: 46 → 47
- versionName: 2.3.1.17.3 → 2.3.1.17.4

---

## [2.3.1.17.3] - 2026-05-26

### 🐞 Bug Fixes

- Tombol kembali (back arrow) di SettingsActivity tidak berfungsi — tambah `setNavigationOnClickListener`.

### 🔧 Optimasi & Penyesuaian

- Padding dialog ditingkatkan 24px → 25dp (density-independent).
- Ukuran teks dialog ditingkatkan 12sp → 40sp.

### ✏️ File Changed

- app/build.gradle
- app/src/main/java/exp/ftxt/SettingsActivity.java

### 🔢 Version

- versionCode: 45 → 46
- versionName: 2.3.1.17.2 → 2.3.1.17.3

---

## [2.3.1.17.2] - 2026-05-26

### ♻️ Lifecycle Changes

- Assets dokumentasi diganti ekstensi .md → .txt (AGENTS, CHANGELOG, README).
- SettingsActivity diperbarui baca file .txt dari assets.
- Task Gradle syncDocs ditambahkan dengan rename .md → .txt otomatis saat build.

### ✏️ File Changed

- app/build.gradle
- app/src/main/assets/AGENTS.txt
- app/src/main/assets/CHANGELOG.txt
- app/src/main/assets/README.txt
- app/src/main/java/exp/ftxt/SettingsActivity.java
- README.md

### 🔢 Version

- versionCode: 44 → 45
- versionName: 2.3.1.17.1 → 2.3.1.17.2

---

## [2.3.1.17.1] - 2026-05-24

### 🔧 Optimasi & Penyesuaianan

- Toolbar color diubah menjadi biru muda (#2196F3).
- Icon tema sekarang dinamis: bulan (mode gelap) / matahari (mode terang).

### ✏️️ File Changed

- app/build.gradle
- app/src/main/res/layout/activity_main.xml
- app/src/main/java/exp/ftxt/MainActivity.java

### 🗒️ File Addeed

- app/src/main/res/drawable/ic_sun.xml

### 🔢 Version

- versionCode: 43 → 44
- versionName: 2.3.1.17.0 → 2.3.1.17.1

---

## [2.3.1.17.0] - 2026-05-24

### ✨ Fitur Baru

- **Dynamic Title Bar**: Toolbar title berubah sesuai modul yang dipilih di drawer.
- **Modul Baru**: CPU Monitor, Temperature, dan Logo Display ditambahkan ke drawer (Coming Soon).
- **String Resource**: Semua nama modul dipindah ke strings.xml.

### 🔧 Optimasi & Penyesuaianan

- Floating Text: Shadow switch dipindah ke bawah tombol Pilih Warna.
- FPS Display: Kunci Posisi switch dipindah ke atas SeekBar ukuran.

### ✏️️ File Changed

- CHANGELOG.md (root)
- README.md (root)
- app/build.gradle
- app/src/main/java/exp/ftxt/MainActivity.java
- app/src/main/res/layout/activity_main.xml
- app/src/main/res/menu/drawer_menu.xml
- app/src/main/res/values/strings.xml

### 🔢 Version

- versionCode: 42 → 43
- versionName: 2.3.1.16.1 → 2.3.1.17.0

---

## [2.3.1.16.1] - 2026-05-24

### 🔧 Optimasi & Penyesuaianan

- Sinkronisasi struktur project & deskripsi file di README.md dengan filesystem aktual.

### ✏️️ File Changed

- app/build.gradle
- app/src/main/assets/README.md

### 🔢 Version

- versionCode: 41 → 42
- versionName: 2.3.1.16.0 → 2.3.1.16.1

---

## [2.3.1.16.0] - 2026-05-24

### ✨ Fitur Baru

- **FPS Draggable**: FPS overlay bisa digeser (drag) dan dikunci posisinya.
- Limit SeekBar: min 5 → max 120 (sebelumnya min 10 → max 60).

### ✏️️ File Changed

- app/build.gradle 
- app/src/main/java/exp/ftxt/modules/fps/FpsConfig.java
- app/src/main/java/exp/ftxt/modules/fps/FpsModule.java
- app/src/main/java/exp/ftxt/core/FloatingService.java
- app/src/main/java/exp/ftxt/MainActivity.java
- app/src/main/res/layout/activity_main.xml

### 🔢 Version

- versionCode: 40 → 41
- versionName: 2.3.1.15.0 → 2.3.1.16.0

---

## [2.3.1.15.0] - 2026-05-24

### ♻️ Lifecycle Changes

- **Refactor modular**: Semua modul dipisah ke package sendiri.
  - `modules/text/` → TextConfig + TextModule
  - `modules/fps/` → FpsConfig + FpsModule
  - `shared/ui/` → ColorPickerDialog (HSV picker reusable)
  - `core/` → FloatingService didelegasikan ke module
  - `shared/color/` → HSVColorPickerView tetap
- FloatingService pindah ke `core/`, update AndroidManifest.
- HSVColorPickerView di root dihapus (duplikat).

### 🐞 Bug Fixes

- **FPS tidak tampil**: Service kini bisa start tanpa text overlay (FPS standalone).
- **TextConfig.size**: Fix posisi overlay tidak termuat dari SharedPreferences.

### 🔢 Version

- versionCode: 39 → 40
- versionName: 2.3.1.14.0 → 2.3.1.15.0

---

## [2.3.1.14.0] - 2026-05-24

### ✨ Fitur Baru

### 🔧 Optimasi & Penyesuaianan (Perubahan Sidebar)

- Header sidebar: ikon & teks "Pengaturan" diganti dengan header "FTxT" (sejajar toolbar).
- Daftar modul: Floating Text, FPS Display, Network Stats, Battery Monitor, Clock Module.
- Klik item sidebar → toast "Coming Soon" (modul belum diimplementasi).
- Default selection ke "Floating Text".

### ✏️️ File Changed

- app/build.gradle
- app/src/main/java/exp/ftxt/MainActivity.java
- app/src/main/res/layout/activity_main.xml
- app/src/main/res/menu/drawer_menu.xml

### 🗒️ File Added

- app/src/main/res/layout/nav_header.xml

### 🔢 Version

- versionCode: 37 → 38
- versionName: 2.3.1.12.0 → 2.3.1.13.0

---

## [2.3.1.12.0] - 2026-05-24

### ✨ Fitur Baru

- Toggle tema (gelap/terang) via ikon bulan di toolbar kanan.
- Tema tersimpan dan bertahan setelah app ditutup.

### 🔧 Optimasi & Penyesuaianan

- Kembalikan ikon Pengaturan ke toolbar kanan.
- NavigationView tidak pakai hardcoded color agar otomatis ikut tema.

### ✏️️ File Changed

- app/build.gradle
- app/src/main/java/exp/ftxt/MainActivity.java
- app/src/main/res/layout/activity_main.xml
- app/src/main/res/menu/main_menu.xml

### 🗒️ File Added

- app/src/main/res/drawable/ic_theme.xml

### 🔢 Version

- versionCode: 36 → 37
- versionName: 2.3.1.11.0 → 2.3.1.12.0

---

## [2.3.1.11.0] - 2026-05-24

### 🔧 Optimasi & Penyesuaianan

- Ubah toolbar atas menjadi navigation drawer (sidebar kiri) dengan hamburger menu.
- Menu Pengaturan dipindah dari toolbar icon ke NavigationView di drawer.

### ✏️️ File Changed

- app/build.gradle
- app/src/main/java/exp/ftxt/MainActivity.java
- app/src/main/res/layout/activity_main.xml
- app/src/main/res/values/strings.xml

### 🗒️ File Added

- app/src/main/res/menu/drawer_menu.xml

### ️🔥 File Removed

- app/src/main/res/menu/main_menu.xml

### 🔢 Version

- versionCode: 35 → 36
- versionName: 2.3.1.10.0 → 2.3.1.11.0

---

## [2.3.1.10.0] - 2026-05-24

### ✨ Fitur Baru

- Tambah menu pengaturan dengan opsi untuk melihat CHANGELOG dan README dalam dialog scrollable.
- SettingsActivity membaca langsung dari file assets (single source of truth).
- Hapus duplikasi file: CHANGELOG.md, README.md, dan AGENTS.md hanya ada di assets folder.

### ✏️️ File Changed

- app/build.gradle
- app/src/main/AndroidManifest.xml
- app/src/main/java/exp/ftxt/MainActivity.java
- app/src/main/java/exp/ftxt/SettingsActivity.java

### 🗒️ File Added

- app/src/main/assets/CHANGELOG.md
- app/src/main/assets/README.md
- app/src/main/assets/AGENTS.md
- app/src/main/res/layout/activity_settings.xml

### ️🔥 File Removed (Duplikasi)

- app/src/main/res/raw/changelog.txt
- app/src/main/res/raw/readme.txt

### 🔢 Version

- versionCode: 34 → 35
- versionName: 2.3.1.9.6 → 2.3.1.10.0

### 💡 Catatan

Mulai sekarang, semua dokumentasi (CHANGELOG, README, AGENTS) diletakkan di folder assets. Update hanya dilakukan di assets, tidak ada duplikasi file lagi.

---

## [2.3.1.9.6] - 2026-05-24

### 🔧 Optimasi & Penyesuaianan

- Perbaiki typo default teks overlay ("MTxT AKTIF" ↔ "FTxT AKTIF").
- Pastikan SeekBar ukuran teks memiliki batas minimum 10 dan sinkronisasi UI saat progress di-bawah batas.

### ✏️️ File Changed

- app/build.gradle
- app/src/main/java/exp/ftxt/MainActivity.java

### 🔢 Version

- versionCode: 33 → 34
- versionName: 2.3.1.9.5 → 2.3.1.9.6

---

## [2.3.1.9.5] - 2026-05-24

### 🔧 Optimasi & Penyesuaian

- Persiapan struktur modular untuk module overlay.
- HSVColorPickerView dipindah ke shared component.
- Update import package setelah refactor namespace.

### ✏️️ File Changed

- app/build.gradle
- app/src/main/java/exp/ftxt/HSVColorPickerView.java
- app/src/main/java/exp/ftxt/MainActivity.java
- app/src/main/res/layout/dialog_hsv_color_picker.xml

### 🗒️ File Added

- app/src/main/java/exp/ftxt/modules/clock/
- app/src/main/java/exp/ftxt/modules/cpu/
- app/src/main/java/exp/ftxt/modules/fps/
- app/src/main/java/exp/ftxt/modules/logo/
- app/src/main/java/exp/ftxt/modules/temp/
- app/src/main/java/exp/ftxt/modules/text/
- app/src/main/java/exp/ftxt/shared/color/

### 🔢 Version

- versionCode: 33 → 34
- versionName: 2.3.1.9.4 → 2.3.1.9.5

---

## [2.3.1.9.4] - 2026-05-23

### ✨ Fitur Baru

- **Shadow Toggle**
  Tambah switch "Shadow" untuk menyalakan / mematikan latar semi-transparent di belakang teks overlay.

### 🔧 Optimasi & Penyesuaianan

- Version bump & maintenance.
- Smoke build verification.

### ✏️️ File Diubah

#### 🔢 app/build.gradle (UPDATED)

- versionCode: 32 → 33
- versionName: 9.3 → 2.3.1.9.4

#### ✅ app/src/main/res/layout/activity_main.xml (UPDATED)

- Tambah `shadowSwitch`.

#### ✅ app/src/main/java/exp/ftxt/MainActivity.java (UPDATED)

- Tambah handler `shadowSwitch`.
- Simpan preferensi `shadow_enabled`.

#### ✅ app/src/main/java/exp/ftxt/FloatingService.java (UPDATED)

- Tambah state `shadowEnabled`.
- Tambah `applyShadow()`.
- Tambah `updateShadowStatic()` untuk apply/remove background.

---

## Format Update Baru

Mulai update berikutnya, gunakan format CHANGELOG sesuai pedoman terbaru pada `AGENTS.md`.

Aturan utama:

- Entry baru ditaruh di paling atas
- Gunakan section seperlunya
- Ringkas, faktual, langsung ke perubahan
- Hindari penjelasan terlalu panjang
- Catat hanya file yang benar-benar berubah

Section yang tersedia:

```md
### 🔢 Version
### ✨ Fitur Baru
### 🚮️ Fitur Dihapus
### 📥️ Fitur Dipulihkan
### ♻️️ Lifecycle Changes
### 🔁️ Perubahan Fitur
### 🗒️ File Added
### ✏️️ File Changed
### 🔥️ File Removed
### 🔧 Optimasi & Penyesuaian
### 🐞 Bug Fixes
### 💡 Catatan
```
---
```📌
Entry lama dapat memakai format sebelumnya.
Tidak wajib dimigrasikan retroaktif.
```
---

## [9.3] - 2026-05-17

### 🔧 Perbaikan
- **WakeLock**: CPU tidak tidur saat overlay aktif — service tetap jalan
- **Battery Optimization**: Saat overlay diaktifkan, aplikasi minta dikecualikan dari optimasi baterai

### 📝 File yang Diubah

#### ✅ AndroidManifest.xml (UPDATED)
- Tambah `WAKE_LOCK` permission
- Tambah `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` permission

#### ✅ FloatingService.java (UPDATED)
- Import `android.os.PowerManager`
- Tambah field `wakeLock`
- `onCreate()`: acquire `PARTIAL_WAKE_LOCK`
- `onDestroy()`: release wake lock

#### ✅ MainActivity.java (UPDATED)
- Import `android.os.PowerManager`
- Saat overlay ON, cek `isIgnoringBatteryOptimizations()`
- Jika tidak, kirim intent `ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`

#### 🔢 app/build.gradle (UPDATED)
- versionCode: 31 → 32
- versionName: 9.2 → 9.3

### 📊 Version Numbering
- **9** = Fitur baru (sejak 9.1)
- **3** = Perbaikan ke-3 (WakeLock + Battery Optimization)

---

## [9.2] - 2026-05-17

### 🔧 Perbaikan dan Perubahan UI
- **Hapus Transparency Slider**: Slider transparansi dihapus karena sudah ada kontrol alpha di HSV Color Picker
- **Layout Ulang**: Switch Overlay dan Lock dipindahkan ke baris terpisah di atas kolom teks
- **Posisi Switch**: Switch di kiri, label teks di kanan
- **Warna Switch**: Biru saat aktif, merah pudar saat mati
- **Rename Label**: "Teks Terkunci" → "Kunci Posisi"

### 📝 File yang Diubah

#### ✅ activity_main.xml (UPDATED)
- Hapus `TextView` "Transparansi" dan `SeekBar` transparansi
- Setiap switch dibungkus `LinearLayout` horizontal: `[Switch] [TextView label]`

#### ✅ MainActivity.java (UPDATED)
- Hapus `currentAlpha` variable dan `transparencySeekBar`
- Tambah `import android.content.res.ColorStateList`
- Tambah method `applySwitchTint()` untuk warna biru/merah

#### ✅ app/build.gradle (UPDATED)
- versionCode: 30 → 31
- versionName: 9.1 → 9.2

### 📊 Version Numbering
- **9** = Periode fitur (sejak 9.1)
- **2** = Perbaikan UI dan penghapusan fitur redundan

---

## [8.3] - 2026-05-17

### 🐛 Bug Fixes
- **Text Size Real-Time Update**: Ukuran teks sekarang langsung berubah saat slider digeser, tanpa perlu toggle overlay
  - Tambah `FloatingService.updateTextSizeStatic()` di `SeekBar.onProgressChanged()`

### 📝 File yang Diubah

#### ✅ MainActivity.java (UPDATED)
- Tambah `FloatingService.updateTextSizeStatic()` di `onProgressChanged()`

#### ✅ app/build.gradle (UPDATED)
- versionCode: 28 → 29
- versionName: 8.2 → 8.3

### 📊 Version Numbering
- **8** = Generasi fitur HSV Color Picker
- **3** = Perbaikan bug ke-3 (real-time text size update)

---

## [8.2] - 2026-05-17

### ✨ Fitur Baru
- **Adaptive Launcher Icon**: Ikon aplikasi menggunakan adaptive icon dengan foreground custom PNG
- **Real-Time Text Update**: Teks overlay langsung berubah saat diketik, tanpa perlu toggle overlay
- **Foreground Service**: Service berjalan sebagai foreground service dengan notification (tidak di-kill sistem)

### 🔧 Perbaikan
- **Launcher Icon**: Pindah PNG langsung ke `app/src/main/res/drawable/`, hapus Gradle copy task yang kompleks
- **AndroidManifest**: Hapus `package` attribute (AGP 8+ sudah pakai `namespace` di build.gradle)
- **Dependencies**: Update `appcompat:1.2.0` → `1.7.1`, `constraintlayout:2.0.4` → `2.2.1`
- **Deprecated API**: Ganti `PreferenceManager` dari `android.preference` → `getSharedPreferences()`
- **Permission Feedback**: Tambah Toast saat izin overlay diperlukan
- **POST_NOTIFICATIONS**: Tambah runtime permission request untuk Android 13+ (API 33+)

### 🐛 Bug Fixes
- **Service Crash di API 26+**: Ganti `startService()` → `startForegroundService()` + notification
- **Foreground Service Type**: Set `foregroundServiceType="dataSync"` untuk kompatibilitas API 35
- **startForeground() Sebelum addView()**: Cegah crash saat overlay gagal ditambahkan

### 📝 File yang Dibuat & Diubah

#### ✅ app/build.gradle (UPDATED)
- Hapus blok copy task launcher icon
- versionCode: 27 → 28
- versionName: 8.1 → 8.2
- Update dependency versi

#### ✅ AndroidManifest.xml (UPDATED)
- Hapus `package="exp.ftxt"`
- Tambah `FOREGROUND_SERVICE` dan `POST_NOTIFICATIONS` permission
- Tambah `android:foregroundServiceType="dataSync"` ke service

#### ✅ FloatingService.java (UPDATED)
- Tambah foreground service: `startForeground()` + notification channel
- Ganti `PreferenceManager` → `getSharedPreferences()` langsung
- Hapus if-else overlay type (langsung `TYPE_APPLICATION_OVERLAY`)
- Panggil `startForeground()` sebelum `addView()`
- Wrapping `startForeground()` dalam try-catch

#### ✅ MainActivity.java (UPDATED)
- Tambah `TextWatcher` untuk real-time text update
- Tambah Toast feedback saat izin overlay diminta
- Ganti `startService()` → `startForegroundService()`
- Tambah runtime permission request `POST_NOTIFICATIONS` (API 33+)
- Tambah `onRequestPermissionsResult()` handler

#### ✅ ic_launcher_foreground.png (MOVED)
- Dari: root project `/ic_launcher_foreground.png`
- Ke: `app/src/main/res/drawable/ic_launcher_foreground.png`

#### ✅ ic_launcher_foreground.png (DELETED)
- Hapus file dari root project

### 📊 Version Numbering
- **8** = Fitur utama (HSV Color Picker di 8.1, Foreground Service di 8.2)
- **2** = Versi kedua dari iterasi ini

---

## [8.1] - 2026-05-17

### ✨ Fitur Baru
- **Circular HSV Color Picker Wheel**: Color picker berbentuk lingkaran dengan HSV color model
  - Pilih Hue dengan memutar di sekeliling lingkaran
  - Pilih Saturation dengan menggeser ke arah pusat
  - Kontrol brightness dengan slider 0-100%
  - Kontrol transparency/alpha dengan slider 0-255
  - Live preview warna real-time (60x60dp)

### 🐛 Bug Fixes
- **Color Selection Real-Time**: Warna baru sekarang langsung diterapkan tanpa perlu on/off overlay
  - Tambah method static `updateTextColorStatic()` untuk instant update

### 📝 File yang Dibuat & Diubah

#### ✅ HSVColorPickerView.java (NEW)
- Custom View untuk circular color picker wheel
- Implementasi HSV color model (Hue, Saturation, Value)
- Gradient shader untuk smooth color transitions
- Touch event handling real-time
- Support ARGB full color (dengan alpha channel)

#### ✅ dialog_hsv_color_picker.xml (NEW)
- Layout dialog dengan:
  - Color picker wheel (280x280dp)
  - Brightness slider (0-100%)
  - Alpha/Transparency slider (0-255)
  - Live color preview (60x60dp)
  - OK dan Cancel buttons

#### ✅ MainActivity.java (UPDATED)
- Import `android.view.View` untuk dialog support
- Replace old simple color picker dengan `showHSVColorPickerDialog()`
- Tambah method `showHSVColorPickerDialog()` - dialog management
- Tambah method `updateColorPreview()` - live preview update
- Tambah method `getColorWithAlpha()` - HSV + brightness + alpha calculation

#### ✅ FloatingService.java (UPDATED)
- Tambah method `updateTextColorStatic()` - instant color update
- Tambah method `updateTextStatic()` - instant text update
- Tambah method `updateTextSizeStatic()` - instant text size update
- Tambah method `updateTouchFlagsStatic()` - instant touch passthrough flag update

#### ✅ app/build.gradle (UPDATED)
- versionCode: 26 → 27
- versionName: 7.4 → 8.1

### 📊 Version Numbering
- **8** = Fitur baru (HSV Color Picker)
- **1** = Versi pertama dari fitur ini

---

## [7.4] - 2026-05-17

### 🐛 Bug Fixes
- **Touch Passthrough Real-Time**: Penguncian sentuhan sekarang langsung berfungsi tanpa restart overlay
  - Tambah method static wrapper `updateTouchFlagsStatic()`
  - Instant apply FLAG_NOT_TOUCHABLE saat switch diubah

### 📝 File yang Diubah

#### ✅ FloatingService.java (UPDATED)
- Import `SharedPreferences` dan `PreferenceManager`
- Tambah property `prefs` untuk SharedPreferences instance
- Tambah property `public static FloatingService instance` untuk static access
- Tambah method `updateTouchFlagsStatic()` - wrapper untuk real-time flag update
- Tambah method `savePosition()` - simpan X,Y ke SharedPreferences
- Tambah method `loadPosition()` - load X,Y dari SharedPreferences
- Update touch listener untuk ACTION_UP case yang memanggil `savePosition()`
- Update onCreate() dengan instance initialization dan loadPosition()
- Update onDestroy() dengan savePosition() dan cleanup

#### ✅ MainActivity.java (UPDATED)
- Update touch passthrough switch listener untuk memanggil `FloatingService.updateTouchFlagsStatic()`

#### ✅ app/build.gradle (UPDATED)
- versionCode: 25 → 26
- versionName: 7.3 → 7.4

### 📊 Version Numbering
- **7** = Fitur Touch Passthrough
- **4** = Penyesuaian ke-4 pada fitur:
  - 7.1 = Penambahan fitur (kunci posisi + opsi sentuhan)
  - 7.2 = Penghapusan fitur redundan (kunci posisi)
  - 7.3 = Perbaikan touch passthrough logic
  - 7.4 = Perbaikan bug responsivitas & persistent position ✓

---

## [7.3] - 2026-05-16

### 🐛 Bug Fixes
- **Touch Passthrough Implementation**: Sentuhan sekarang benar-benar melewati teks overlay
  - Implementasi FLAG_NOT_TOUCHABLE yang proper pada WindowManager params
  - Refactor touch listener menjadi property class untuk dynamic assignment
  - Saat `isTouchPassthrough = true`: Hapus listener & apply flag
  - Saat `isTouchPassthrough = false`: Attach listener & hapus flag

### 📝 File yang Diubah

#### ✅ FloatingService.java (UPDATED)
- Refactor `updateTouchFlags()` untuk proper FLAG_NOT_TOUCHABLE handling
- Ubah touch listener menjadi `private View.OnTouchListener touchListener` property
- Touch listener sekarang dapat di-attach/detach secara dinamis
- Update onCreate() untuk memanggil `updateTouchFlags()` saat setup

#### ✅ MainActivity.java (UPDATED)
- Update touch passthrough switch label untuk clarity:
  - OFF: "Teks Bergerak"
  - ON: "Teks Terkunci"

#### ✅ app/build.gradle (UPDATED)
- versionCode: 24 → 25
- versionName: 7.2 → 7.3

---

## [7.2] - 2026-05-16

### 🔄 Optimizations
- **Fitur Redundan Dihapus**: Penguncian posisi manual dihapus karena otomatis tertutup oleh sentuhan lewati
- Hapus variabel `isPositionLocked`
- Hapus Switch `lockPositionSwitch` dari UI
- Simplifikasi logic dengan hanya fokus pada touch passthrough

### 📝 File yang Diubah

#### ✅ MainActivity.java (UPDATED)
- Hapus variabel static `isPositionLocked`
- Hapus widget `lockPositionSwitch` dari onCreate() dan findViewById
- Hapus event listener untuk lock position switch
- Revise touch passthrough behavior description

#### ✅ FloatingService.java (UPDATED)
- Hapus pengecekan `isPositionLocked` dari touch listener
- Sederhanakan logic untuk fokus hanya pada `isTouchPassthrough`

#### ✅ activity_main.xml (UPDATED)
- Hapus Switch element `lockPositionSwitch`

#### ✅ app/build.gradle (UPDATED)
- versionCode: 23 → 24
- versionName: 7.1 → 7.2

---

## [7.1] - 2026-05-16

### ✨ Fitur Baru
- **Touch Passthrough (Sentuhan Lewati)**: Fitur untuk membuat sentuhan melewati teks overlay
  - ON: Teks tidak dapat disentuh, sentuhan langsung ke aplikasi di belakang
  - OFF: Teks dapat disentuh dan dipindahkan

### 📝 File