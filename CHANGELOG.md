## [3.11.3.78.1] - 2026-06-13
### 🔧 Optimasi & Penyesuaian
- **Drag & drop reorder preset** — Migrasi ListView ke RecyclerView + ItemTouchHelper untuk animasi drag & drop yang smooth.
- **Drag & drop drawer sidebar** — Migrasi `LinearLayout`+`DragEvent` ke `RecyclerView`+`ItemTouchHelper` untuk animasi drag & drop smooth di navigasi drawer.
- **Lint `MissingDefaultResource` di drawable-night** — Nonaktifkan lint `MissingDefaultResource` yang menyebabkan `lintVitalRelease` gagal karena drawable malam tanpa deklarasi di folder dasar.
### 🗒️ File Added
- `app/src/main/res/values/ids.xml` — Deklarasi ID `R.id.nav*` untuk drawer (hilang setelah hapus `TextView` statik)
### ✏️ File Changed
- `app/build.gradle` — Tambah dependency `androidx.recyclerview:recyclerview:1.3.2`; tambah `lint { disable 'MissingDefaultResource' }`
- `app/src/main/res/layout/dialog_preset_browser.xml` — `ListView` → `RecyclerView`
- `app/src/main/res/layout/drawer_content.xml` — `ScrollView`+`LinearLayout`+static `TextView` → `RecyclerView`
- `app/src/main/java/exp/ftxt/shared/preset/PresetBrowserDialog.java` — `ListView`+`BaseAdapter` → `RecyclerView`+`ItemTouchHelper`; hapus `ClipData`/`DragEvent`, tambah `LinearLayoutManager`/`ViewHolder`
- `app/src/main/java/exp/ftxt/MainActivity.java` — `LinearLayout`+`DragEvent`+`makeDraggable`/`setupDragTarget`/`buildSidebarItem` → `RecyclerView`+`ItemTouchHelper`+`SidebarAdapter`; hapus `ClipData`/`DragEvent`, tambah `RecyclerView`/`ItemTouchHelper`/`LinearLayoutManager`
### 🔢 Version
versionCode: 161
versionName: 3.11.3.78.1
---
## [3.11.3.78.0] - 2026-06-13
### 🚮 Fitur Dihapus
- **Tombol Ekspor Semua di dialog preset** — Tombol "Ekspor Semua" dihapus dari bottom bar dialog browser preset.
### ✨ Fitur Baru
- **Tombol Tandai di header** — Mode pilih: tap Tandai → checkbox muncul di tiap item → tap item untuk centang.
- **Tombol Tandai Semua di header** — Centang atau hapus centang semua item.
- **Tombol aksi batch di bottom bar** — Saat mode Tandai aktif, tombol Simpan/Impor berubah menjadi Hapus, Favorit, Bagikan, Ekspor.
- **Opsi Gunakan Preset di menu item** — Saat mengetuk preset, muncul menu dengan opsi "Gunakan Preset" di urutan pertama.
- **Drag & drop reorder preset** — Ketuk lama preset untuk drag & drop menukar posisi preset dalam daftar.
### ♻️ Perubahan Fitur
- **Title "Pilih Preset" pindah ke header layout** — Sebelumnya via `AlertDialog.setTitle()`, sekarang langsung di XML header.
- **CheckBox di setiap item preset** — Muncul hanya saat mode Tandai aktif.
- **Preset yang disimpan diletakkan di paling atas** — Preset baru maupun hasil update langsung berada di urutan teratas daftar.
### 🐞 Bug Fixes
- **Preset baru tidak muncul di daftar setelah simpan** — `onSaveClick` async menyebabkan `refreshData()` kepanggil sebelum save selesai. Diperbaiki dengan mengubah `onSaveClick` dari `Runnable` ke `Consumer<Runnable>`, sehingga `refreshData()` dipanggil via callback setelah save benar-benar selesai.
### ✏️ File Changed
- `app/src/main/res/layout/dialog_preset_browser.xml` — Tambah header (title + btnTandaiSemua + btnTandai); tambah btnHapus/btnFavorit/btnBagikan/btnEkspor di bottom + divider vertikal; divider ListView; hapus btnExportAll; FrameLayout wrapper ListView + maxHeight 280dp; hapus Batal
- `app/src/main/java/exp/ftxt/shared/preset/PresetBrowserDialog.java` — Select mode, checkedSet, handler btnTandai/btnTandaiSemua + aksi batch (Hapus/Favorit/Bagikan/Ekspor); handler Gunakan Preset; drag & drop reorder; hapus setTitle; onSaveClick Runnable → Consumer\<Runnable\>
- `app/src/main/java/exp/ftxt/shared/preset/PresetHandler.java` — showSavePresetDialog + doSavePreset tambah parameter onSaved; showLoadPresetDialog ubah onSaveClick Runnable → Consumer\<Runnable\>
- `app/src/main/java/exp/ftxt/shared/preset/PresetManager.java` — Tambah method `reorder`; save() pindahkan item ke index 0
- `app/src/main/res/layout/preset_browser_item.xml` — Tambah chkSelect CheckBox; padding & ukuran dikecilkan; height checkbox disamakan (32dp)
- `app/src/main/res/drawable/vertical_divider.xml` — Divider vertikal untuk bottom bar
- `app/src/main/java/exp/ftxt/ui/BatteryPositionController.java` — update lambda onSaveClick
- `app/src/main/java/exp/ftxt/ui/BatteryPercentagePositionController.java` — update lambda onSaveClick
- `app/src/main/java/exp/ftxt/ui/BatteryCurrentPositionController.java` — update lambda onSaveClick
- `app/src/main/java/exp/ftxt/ui/NetworkPositionController.java` — update lambda onSaveClick
- `app/src/main/java/exp/ftxt/ui/WatermarkPositionController.java` — update lambda onSaveClick
- `app/src/main/java/exp/ftxt/ui/ClockPositionController.java` — update lambda onSaveClick
- `app/src/main/java/exp/ftxt/ui/FpsPositionController.java` — update lambda onSaveClick
- `app/src/main/java/exp/ftxt/ui/TextPositionController.java` — update lambda onSaveClick
### 🔢 Version
versionCode: 160
versionName: 3.11.3.78.0
---
## [3.10.3.77.0] - 2026-06-13
### 🚮 Fitur Dihapus
- **Tombol Simpan/Muat Preset dari panel posisi** — Tombol `btnSavePreset`, `btnLoadPreset`, dan label `txtActivePreset` dihapus dari semua 8 panel posisi (Battery, Battery%, BatteryCurrent, Clock, FPS, Network, Text, Watermark). Fungsi preset tetap bisa diakses via icon gear → "Muat Preset".
### ♻️ Perubahan Fitur
- **Dialog preset terpusat** — Dialog preset (PresetBrowserDialog) sekarang punya tombol Simpan di dalamnya. Setiap panel posisi panggil overload `showLoadPresetDialog` 5-param dengan `onSaveClick`.
### ✏️ File Changed
- `app/src/main/java/exp/ftxt/shared/preset/PresetHandler.java` — Tambah overload `showLoadPresetDialog` 5-param (`onSaveClick`)
- `app/src/main/java/exp/ftxt/shared/preset/PresetBrowserDialog.java` — Dukungan tombol Simpan di dialog
- `app/src/main/res/layout/dialog_preset_browser.xml` — Tambah tombol Simpan
- `app/src/main/java/exp/ftxt/ui/BatteryPositionController.java` — Hapus binding btnSave/btnLoad/txtActivePreset; update showLoadPresetDialog
- `app/src/main/java/exp/ftxt/ui/BatteryPercentagePositionController.java` — Sama
- `app/src/main/java/exp/ftxt/ui/BatteryCurrentPositionController.java` — Sama
- `app/src/main/java/exp/ftxt/ui/NetworkPositionController.java` — Sama
- `app/src/main/java/exp/ftxt/ui/WatermarkPositionController.java` — Sama
- `app/src/main/java/exp/ftxt/ui/ClockPositionController.java` — Hapus binding btnSave/btnLoad
- `app/src/main/java/exp/ftxt/ui/FpsPositionController.java` — Sama
- `app/src/main/java/exp/ftxt/ui/TextPositionController.java` — Sama
- `app/src/main/java/exp/ftxt/MainActivity.java` — Update komentar popup settings; fix duplikasi kode
### 🔢 Version
versionCode: 159
versionName: 3.10.3.77.0
---
## [3.9.3.76.0] - 2026-06-13
### ✨ Fitur Baru
- **Opsi Ganti Ikon Aplikasi** — Pengaturan ikon aplikasi di Konfigurasi: toggle Default/Alternatif. Dua varian ikon aplikasi bisa dipilih langsung dari dalam aplikasi.
- **Background Antarmuka Tema Gelap & Terang** — Drawer navigation, header, toolbar, dan layar utama masing-masing punya background gambar sendiri untuk tema gelap dan terang.
### 🗒️ File Added
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher_alt.xml`
- `app/src/main/res/drawable/ic_launcher_foreground_alt.png`
- `app/src/main/res/drawable/ic_launcher_bg.png`
- `app/src/main/res/drawable-night/bg_alt.png`
- `app/src/main/res/drawable/bg_alt_light.png`
- `app/src/main/res/drawable/bg_alt_light2.png`
- `app/src/main/res/drawable/bg_main_light.png`
- `app/src/main/res/drawable/bg_main_light2.png`
- `app/src/main/res/drawable-night/bg_main_dark.png`
- `app/src/main/res/drawable/appbar_light.png`
- `app/src/main/res/drawable-night/appbar_dark.png`
- `app/src/main/res/drawable/drawbar_light.png`
- `app/src/main/res/drawable-night/drawbar_dark.png`
- `app/src/main/res/drawable/toolbar_bg.xml`
- `app/src/main/res/drawable-night/toolbar_bg.xml`
- `app/src/main/res/drawable/drawer_bg.xml`
- `app/src/main/res/drawable-night/drawer_bg.xml`
- `app/src/main/res/drawable/drawer_header_bg.xml`
- `app/src/main/res/drawable-night/drawer_header_bg.xml`
- `app/src/main/res/drawable/main_bg.xml`
- `app/src/main/res/drawable-night/main_bg.xml`
### ✏️ File Changed
- `app/src/main/AndroidManifest.xml` — Tambah activity-alias `MainActivityDefault` dan `MainActivityAlt`
- `app/src/main/java/exp/ftxt/SettingsActivity.java` — Tambah toggle ikon + `setIcon()`
- `app/src/main/res/layout/activity_settings.xml` — Tambah section Ikon Aplikasi
- `app/src/main/res/values/strings.xml` — Tambah string icon
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` — Background pakai `ic_launcher_bg`
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher_alt.xml` — Background pakai `ic_launcher_bg`
- `app/src/main/res/layout/drawer_content.xml` — Background ganti `@color/drawer_background` → `@drawable/drawer_bg`
- `app/src/main/res/layout/nav_header.xml` — Background ganti `@color/drawer_header_background` → `@drawable/drawer_header_bg`; height `70dp` + gravity center_vertical; textColor hitam
- `app/src/main/res/layout/activity_main.xml` — Toolbar `#2196F3` → `@drawable/toolbar_bg`; LinearLayout tambah `@drawable/main_bg`
- `app/src/main/res/drawable/drawer_bg.xml` — Ganti src ke `bg_alt_light2`
- `app/src/main/res/drawable-night/drawer_bg.xml` — Ganti src ke `bg_alt`; flip 180° via rotate drawable
- `app/src/main/res/drawable/drawer_header_bg.xml` — Ganti src ke `drawbar_light`
- `app/src/main/res/drawable-night/drawer_header_bg.xml` — Ganti src ke `drawbar_dark`
- `app/src/main/res/drawable/main_bg.xml` — Ganti src ke `bg_main_light2`
- `app/src/main/java/exp/ftxt/MainActivity.java` — Hapus opsi "Muat Preset" dari popup settings (gear)
### 🔢 Version
versionCode: 158
versionName: 3.9.3.76.0
---
## [3.9.3.75.0] - 2026-06-12
### ✨ Fitur Baru
- **148 CSS Color Names** — ColorNameResolver di-upgrade dengan 148 warna CSS standar. Pencocokan warna menggunakan Euclidean distance untuk hasil yang lebih akurat.
- **254 Material Design Colors** — ColorNameResolver diperluas dengan 254 warna Material Design (Red, Pink, Purple, Deep Purple, Indigo, Blue, Light Blue, Cyan, Teal, Green, Light Green, Lime, Yellow, Amber, Orange, Deep Orange, Brown, Grey, Blue Grey) masing-masing dengan shade 50–900 + accent A100–A700.
- **949 XKCD Color Survey Colors** — ColorNameResolver diperluas dengan 949 warna dari survei warna XKCD. Total warna yang dikenali: 1.351 (148 CSS + 254 Material + 949 XKCD).
### ✏️ File Changed
- `app/src/main/java/exp/ftxt/shared/color/ColorNameResolver.java` — Restruktur: CSS colors dipisah ke `loadCssColors()`, Material colors via `loadMaterialColors()`, XKCD colors via `loadXkcdColors()`; total 1.351 warna (148 CSS + 254 Material + 949 XKCD)
### 🔢 Version
versionCode: 157
versionName: 3.9.3.75.0
---
## [3.9.3.74.2] - 2026-06-12
### ♻️ Perubahan Fitur
- **Tutup Aplikasi pindah ke navigation drawer** — Tombol Kill Service dipindahkan dari popup settings (gear) ke bagian paling bawah navigation drawer, dengan ikon close di sebelah kiri.
- **Konfirmasi Keluar jadi default** — Checkbox Konfirmasi Keluar di Konfigurasi dihapus. Behavior ketuk dua kali untuk keluar sekarang aktif secara default tanpa perlu toggle.
- **Tombol Keluar di navigation drawer** — Tambah tombol Keluar (kanan, ikon exit) — hanya tutup UI Activity, overlay tetap berjalan. Kill Service (kiri, ikon close) — force close semua layanan + app.
### 🐞 Bug Fixes
- **Fix overlay tidak otomatis hidup setelah Kill Service** — `autoRequestAndStart()` sekarang pakai `isAnyModuleActive()` yang ngecek semua modul (bukan cuma text+FPS), jadi overlay otomatis restart saat aplikasi dibuka kembali.
### ✏️ File Changed
- `app/src/main/res/layout/drawer_content.xml` — Tambah View divider + row horizontal berisi Kill Service (kiri) dan Keluar (kanan) dengan ikon exit
- `app/src/main/res/drawable/ic_close.xml` — Vector drawable ikon close/X baru
- `app/src/main/res/drawable/ic_exit.xml` — Vector drawable ikon exit (arrow keluar)
- `app/src/main/java/exp/ftxt/MainActivity.java` — Tambah listener navTutupAplikasi→killService() dan navKeluar→forceClose(); hapus Tutup Aplikasi dari settings popup; hapus conditional confirm_exit dari onBackPressed (double-tap jadi default); ekstrak method killService() dari forceClose()
- `app/src/main/java/exp/ftxt/SettingsActivity.java` — Hapus referensi confirmExitCheck
- `app/src/main/res/layout/activity_settings.xml` — Hapus CheckBox Konfirmasi Keluar
- `app/src/main/res/values/strings.xml` — Hapus string confirm_exit_title, confirm_exit_summary
- `app/src/main/res/layout/activity_settings.xml` — Hapus section "Lainnya" (kosong)
### 🔢 Version
versionCode: 156
versionName: 3.9.3.74.2
---
## [3.9.3.74.1] - 2026-06-12
### ♻️ Perubahan Fitur
- **Semua preview warna dalam satu baris horizontal** — Setiap modul: semua preview warna (Warna/Nilai, Label, Shadow, Background) disusun dalam satu baris horizontal rata. Shadow/bg preview dipindahkan dari section Shadow/Background ke section Display.
- **Semua tombol warna jadi preview kotak 30×30** — Tombol Pilih Warna, Warna Label, Shadow, dan Background di semua 8 panel diganti preview kotak 30×30. Tap preview → color picker.
- **Preview warna 40dp→30dp** — Ukuran preview yang sebelumnya 40×40 diperkecil ke 30×30.
### ✏️ File Changed
- `app/src/main/res/layout/panel_text.xml` — Semua color button jadi preview; susun 1 baris: Warna|Shadow|Background
- `app/src/main/res/layout/panel_fps.xml` — Susun 1 baris: Warna|Label|Shadow|Background; preview 40→30dp
- `app/src/main/res/layout/panel_clock.xml` — Semua color button jadi preview; susun 1 baris: Warna|Shadow|Background
- `app/src/main/res/layout/panel_battery.xml` — Susun 1 baris: Warna|Label|Shadow|Background; preview 40→30dp
- `app/src/main/res/layout/panel_battery_percentage.xml` — Semua color button jadi preview; susun 1 baris: Warna|Label|Shadow|Background
- `app/src/main/res/layout/panel_battery_current.xml` — Susun 1 baris: Warna|Label|Shadow|Background; preview 40→30dp
- `app/src/main/res/layout/panel_network.xml` — Susun 1 baris: Warna|Label|Shadow|Background; preview 40→30dp
- `app/src/main/res/layout/panel_watermark.xml` — Semua color button jadi preview; susun 1 baris: Warna|Shadow|Background
- Semua 8 controller Java — Field Button→View; setBackgroundColor di loadConfig & callback color picker
### 🔢 Version
versionCode: 155
versionName: 3.9.3.74.1
---
## [3.9.3.74.0] - 2026-06-12
### ✨ Fitur Baru
- **Sembunyikan Label untuk Battery Current** — Opsi baru "Sembunyikan Label" di panel Battery Current untuk menyembunyikan label mV/mA/W dan hanya menampilkan angka. Mode value-only menampilkan voltase dalam volt (V) bukan milivolt.
### ♻️ Perubahan Fitur
- **Layout ulang panel Battery Current** — Urutan kontrol di section Tampilan: Sembunyikan Label+Interval → V/mA/W → Ukuran Teks → Warna/Label.
- **Ubah satuan voltase mV→V** — Tampilan voltase diubah dari `3900mV` ke `3.9V` (mode normal) dan `3.9` (mode value-only) agar konsisten dan tidak ambigu dengan daya (W).
### 🐞 Bug Fixes
- **Fix duplicate ID XML** — Hapus duplikasi `batCurIntervalValue` yang menyebabkan crash findViewById.
- **Fix checkbox V/mA/W tidak refresh overlay** — Setiap toggle sekarang memanggil `FloatingService.updateBatteryCurrentColorStatic()`.
- **Fix Network showOnlyValue** — Refactor pakai `FloatingService.updateNetworkColorStatic()` ganti inline null check.
### ✏️ File Changed
- `app/src/main/java/exp/ftxt/features/battery_current/BatteryCurrentConfig.java` — Tambah field `showOnlyValue`
- `app/src/main/java/exp/ftxt/features/battery_current/BatteryCurrentModule.java` — Tambah method `getBatteryCurrentValueOnly()`; update `updateDisplay()`; ubah format voltase ke V
- `app/src/main/java/exp/ftxt/ui/BatteryCurrentPanelController.java` — Tambah handler checkbox Sembunyikan Label; tambah call refresh untuk V/mA/W toggle
- `app/src/main/java/exp/ftxt/ui/NetworkPanelController.java` — Refactor panggil `FloatingService.updateNetworkColorStatic()`
- `app/src/main/res/layout/panel_battery_current.xml` — Tambah checkbox Sembunyikan Label + interval inline; reorder kontrol Display; ubah label mV→V
### 🔢 Version
versionCode: 154
versionName: 3.9.3.74.0
---
## [3.9.3.73.0] - 2026-06-12
### ✨ Fitur Baru
- **Sembunyikan Label untuk Network Stats** — Opsi baru "Sembunyikan Label" di panel Network Stats untuk menyembunyikan label ↓↑MB/s dan hanya menampilkan angka kecepatan.
### ♻️ Perubahan Fitur
- **Rename "Hanya Tampilkan Nilai" jadi "Sembunyikan Label"** — FPS dan Battery: teks opsi diganti agar lebih jelas maksudnya.
- **Interval inline pindah ke samping checkbox** — Kontrol `Update= [X]s` dipindah dari bawah baris warna ke samping kanan checkbox "Sembunyikan Label" di panel FPS, Battery, dan Network.
### ✏️ File Changed
- `app/src/main/java/exp/ftxt/features/network_stats/NetworkConfig.java` — Tambah field `showOnlyValue`
- `app/src/main/java/exp/ftxt/features/network_stats/NetworkModule.java` — Implementasi logika `showOnlyValue`
- `app/src/main/java/exp/ftxt/ui/NetworkPanelController.java` — Tambah checkbox handler & UI untuk showOnlyValue
- `app/src/main/res/layout/panel_network.xml` — Tambah checkbox "Sembunyikan Label"; interval inline pindah ke samping checkbox
- `app/src/main/res/layout/panel_fps.xml` — Rename teks checkbox; interval inline pindah ke samping checkbox
- `app/src/main/res/layout/panel_battery.xml` — Rename teks checkbox; interval inline pindah ke samping checkbox
### 🔢 Version
versionCode: 153
versionName: 3.9.3.73.0
---
## [3.9.3.72.1] - 2026-06-12
### ♻️ Perubahan Fitur
- **Kontrol Interval semua modul jadi single button + dialog** — Tombol interval FPS, Battery Temperature, Battery Current, dan Network Stats diubah dari 3 elemen ([-] label [+]) menjadi 1 tombol yang membuka dialog daftar pilihan interval. Method `findIntervalIndex()` dihapus.
- **Tombol Pilih Warna jadi preview kotak** — Semua modul (FPS, Battery, Battery Current, Network): tombol "Pilih Warna" dan "Warna Label" diganti preview kotak 40×40 yang menunjukkan warna aktif. Tap preview → color picker.
- **Kontrol interval inline PopupWindow** — FPS: uji coba kontrol interval `Update= [1]s` dengan popup scroll 5 item (PopupWindow). Setelah sukses, diterapkan ke semua modul. Item terpilih di-highlight biru.
### ✏️ File Changed
- `app/src/main/res/layout/panel_fps.xml` — Ganti `fpsIntervalMinus`/`Plus`/`Label` dengan `fpsIntervalButton`; lalu ganti jadi `fpsIntervalValue` inline + hapus `fpsColorButton`/`fpsLabelColorButton` → `fpsColorPreview`/`fpsLabelColorPreview`
- `app/src/main/res/layout/panel_battery.xml` — Ganti `batteryColorButton`/`batteryLabelColorButton`/`batteryIntervalButton` → color previews + interval inline
- `app/src/main/res/layout/panel_battery_current.xml` — Ganti `batCurColorButton`/`batCurLabelColorButton`/`batCurIntervalButton` → color previews + interval inline
- `app/src/main/res/layout/panel_network.xml` — Ganti `networkColorButton`/`networkLabelColorButton`/`networkIntervalButton` → color previews + interval inline
- `app/src/main/java/exp/ftxt/ui/FpsPanelController.java` — AlertDialog → PopupWindow; Button → View preview; hapus `formatIntervalLabel`
- `app/src/main/java/exp/ftxt/ui/BatteryPanelController.java` — AlertDialog → PopupWindow; Button → View preview; hapus `formatIntervalLabel`
- `app/src/main/java/exp/ftxt/ui/BatteryCurrentPanelController.java` — AlertDialog → PopupWindow; Button → View preview; hapus `formatIntervalLabel`
- `app/src/main/java/exp/ftxt/ui/NetworkPanelController.java` — AlertDialog → PopupWindow; Button → View preview; hapus `formatIntervalLabel`
### 🔢 Version
versionCode: 152
versionName: 3.9.3.72.1
---
## [3.9.3.72.0] - 2026-06-12
### ✨ Fitur Baru
- **Warna Label FPS Terpisah** — Nilai FPS dan teks "FPS" kini bisa diwarnai berbeda. Button "Warna Label" di panel FPS untuk mengatur warna teks label, sementara "Pilih Warna" untuk nilai angka. Menggunakan SpannableString dengan ForegroundColorSpan. Disimpan di preset dan SharedPreferences (key: `fps_label_color`).
- **Warna Label Battery Temperature Terpisah** — Nilai suhu dan satuan °C/% bisa diwarnai berbeda. Button "Warna Label" di panel Battery Temperature. Menggunakan SpannableString dengan ForegroundColorSpan. Disimpan di preset dan SharedPreferences (key: `battery_label_color`).
- **Warna Label Battery Percentage Terpisah** — Nilai persen dan label % bisa diwarnai berbeda. Button "Warna Label" di panel Battery Percentage. Menggunakan SpannableString dengan ForegroundColorSpan. Disimpan di preset dan SharedPreferences (key: `battpct_label_color`).
- **Warna Label Battery Current Terpisah** — Nilai tegangan/arus/daya dan satuan mV/mA/W bisa diwarnai berbeda. Button "Warna Label" di panel Battery Current. Menggunakan SpannableString dengan ForegroundColorSpan per karakter label (`m`/`V`/`A`/`W`). Disimpan di preset dan SharedPreferences (key: `batcur_label_color`).
- **Warna Label Network Stats Terpisah** — Nilai kecepatan dan label ↓↑MB/KB/s bisa diwarnai berbeda. Button "Warna Label" di panel Network Stats. Menggunakan SpannableString dengan ForegroundColorSpan per karakter label (`↓`/`↑`/`M`/`B`/`K`/`/`/`s`). Disimpan di preset dan SharedPreferences (key: `network_label_color`).
### 🐞 Bug Fixes
- **Battery Temperature labelColor span kelebihan** — Sebelumnya `setSpan(i, text.length())` dari karakter label pertama sampai akhir string, menyebabkan angka kedua (persentase) ikut kena labelColor. Diperbaiki jadi `setSpan(i, i+1)` per karakter label (`°`/`C`/`%`).
### 🔧 Optimasi & Penyesuaian
- **BackgroundConfig.java** — Pisahkan konfigurasi background ke file sendiri (`shared/ui/BackgroundConfig.java`) seperti pola ShadowConfig. Semua module (8 module) ganti inline bg fields dengan `BackgroundConfig bg = new BackgroundConfig(padding)`.
### 🗒️ File Added
- `app/src/main/java/exp/ftxt/shared/ui/BackgroundConfig.java`
- `STRUKTUR.md`
- `app/src/main/assets/STRUKTUR.txt`
### ✏️ File Changed
- `app/src/main/java/exp/ftxt/features/fps_display/FpsConfig.java` — Tambah `labelColor`; ganti inline bg fields dgn `BackgroundConfig`
- `app/src/main/java/exp/ftxt/features/fps_display/FpsModule.java` — `updateDisplay()` base color + SpannableString; `updateLabelColor()`; `start()` panggil `updateDisplay()`
- `app/src/main/java/exp/ftxt/core/FloatingService.java` — Tambah `updateFpsLabelColorStatic()`, `updateBatteryLabelColorStatic()`, `updateBatteryPercentageLabelColorStatic()`, `updateBatteryCurrentLabelColorStatic()`, `updateNetworkLabelColorStatic()`
- `app/src/main/java/exp/ftxt/MainActivity.java` — Load `fps_label_color`, `battery_label_color`, `battpct_label_color`, `batcur_label_color`, `network_label_color`; bg field access via `Config.bg.field`
- `app/src/main/java/exp/ftxt/ui/FpsPanelController.java` — `fpsLabelColorButton`; bg field access via `Config.bg.field`
- `app/src/main/java/exp/ftxt/ui/FpsPositionController.java` — `labelColor` di preset; bg field access via `Config.bg.field`
- `app/src/main/java/exp/ftxt/shared/preset/OverlayPreset.java` — Tambah field `labelColor`
- `app/src/main/res/layout/panel_fps.xml` — Tombol "Warna Label"
- `app/src/main/res/layout/panel_battery.xml` — Tombol "Warna Label"
- `app/src/main/res/layout/panel_battery_percentage.xml` — Tombol "Warna Label"
- `app/src/main/res/layout/panel_battery_current.xml` — Tombol "Warna Label"
- `app/src/main/res/layout/panel_network.xml` — Tombol "Warna Label"
- `app/src/main/java/exp/ftxt/features/battery_temperature/BatteryConfig.java` — Tambah `labelColor`; ganti inline bg fields dgn `BackgroundConfig`
- `app/src/main/java/exp/ftxt/features/battery_temperature/BatteryModule.java` — `updateDisplay()` base color + SpannableString; `updateLabelColor()`; bg field access via `Config.bg.field`
- `app/src/main/java/exp/ftxt/ui/BatteryPanelController.java` — `batteryLabelColorButton`; bg field access via `Config.bg.field`
- `app/src/main/java/exp/ftxt/ui/BatteryPositionController.java` — `labelColor` di preset; bg field access via `Config.bg.field`
- `app/src/main/java/exp/ftxt/features/battery_percentage/BatteryPercentageConfig.java` — Tambah `labelColor`; ganti inline bg fields dgn `BackgroundConfig`
- `app/src/main/java/exp/ftxt/features/battery_percentage/BatteryPercentageModule.java` — `updateDisplay()` base color + SpannableString; `updateLabelColor()`; bg field access via `Config.bg.field`
- `app/src/main/java/exp/ftxt/ui/BatteryPercentagePanelController.java` — `batPctLabelColorButton`; bg field access via `Config.bg.field`
- `app/src/main/java/exp/ftxt/ui/BatteryPercentagePositionController.java` — `labelColor` di preset; bg field access via `Config.bg.field`
- `app/src/main/java/exp/ftxt/features/floating_text/TextConfig.java` — Ganti inline bg fields dgn `BackgroundConfig`
- `app/src/main/java/exp/ftxt/features/clock_module/ClockConfig.java` — Ganti inline bg fields dgn `BackgroundConfig`
- `app/src/main/java/exp/ftxt/features/network_stats/NetworkConfig.java` — Tambah `labelColor`; ganti inline bg fields dgn `BackgroundConfig`
- `app/src/main/java/exp/ftxt/features/battery_current/BatteryCurrentConfig.java` — Tambah `labelColor`; ganti inline bg fields dgn `BackgroundConfig`
- `app/src/main/java/exp/ftxt/features/watermark/WatermarkConfig.java` — Ganti inline bg fields dgn `BackgroundConfig`
- `app/src/main/java/exp/ftxt/features/floating_text/TextModule.java` — bg field access via `Config.bg.field`
- `app/src/main/java/exp/ftxt/features/clock_module/ClockModule.java` — bg field access via `Config.bg.field`
- `app/src/main/java/exp/ftxt/features/network_stats/NetworkModule.java` — `updateDisplay()` SpannableString per label char; `updateLabelColor()`; `start()`/`tickRunnable` pakai `updateDisplay()`; bg field access via `Config.bg.field`
- `app/src/main/java/exp/ftxt/features/battery_current/BatteryCurrentModule.java` — `updateDisplay()` SpannableString per label char; `updateLabelColor()`; `start()`/`tickRunnable` pakai `updateDisplay()`; bg field access via `Config.bg.field`
- `app/src/main/java/exp/ftxt/features/watermark/WatermarkModule.java` — bg field access via `Config.bg.field`
- `app/src/main/java/exp/ftxt/ui/TextPanelController.java` — bg field access via `Config.bg.field`
- `app/src/main/java/exp/ftxt/ui/ClockPanelController.java` — bg field access via `Config.bg.field`
- `app/src/main/java/exp/ftxt/ui/NetworkPanelController.java` — `networkLabelColorButton`; bg field access via `Config.bg.field`
- `app/src/main/java/exp/ftxt/ui/BatteryCurrentPanelController.java` — `batCurLabelColorButton`; bg field access via `Config.bg.field`
- `app/src/main/java/exp/ftxt/ui/WatermarkPanelController.java` — bg field access via `Config.bg.field`
- `app/src/main/java/exp/ftxt/ui/TextPositionController.java` — bg field access via `Config.bg.field`
- `app/src/main/java/exp/ftxt/ui/ClockPositionController.java` — bg field access via `Config.bg.field`
- `app/src/main/java/exp/ftxt/ui/NetworkPositionController.java` — `labelColor` di preset; bg field access via `Config.bg.field`
- `app/src/main/java/exp/ftxt/ui/BatteryCurrentPositionController.java` — `labelColor` di preset; bg field access via `Config.bg.field`
- `app/src/main/java/exp/ftxt/ui/WatermarkPositionController.java` — bg field access via `Config.bg.field`
- `app/src/main/java/exp/ftxt/shared/preset/PresetExampleActivity.java` — bg field access via `Config.bg.field`
- `README.md` — Pindahkan struktur project ke `STRUKTUR.md` terpisah; tambah link ke STRUKTUR.md
- `README.txt` — Pindahkan struktur project ke `STRUKTUR.txt` terpisah; tambah link ke STRUKTUR.txt
- `PANDUAN.md` — Tambah info label color dan collapsible sections
- `PANDUAN.txt` — Sinkronkan dengan PANDUAN.md; tambah label color dan collapsible sections
### 🔢 Version
versionCode: 151
versionName: 3.9.3.72.0
---
## [3.9.3.71.0] - 2026-06-12
### ✨ Fitur Baru
- **Kontrol Interval Update (float, 0.2-10s)** — Semua modul (FPS, Network, Battery Current, Battery Temperature) kini mendukung interval update float dengan step: 0.2s, 0.5s, 0.75s, 1-10s. Tombol -/+ di panel masing-masing.
### 🔧 Optimasi & Penyesuaian
- **updateInterval int→float** — Semua Config berubah dari `int` ke `float`, module pakai `(long)(updateInterval * 1000)`.
- **readFloatPref migration** — `MainActivity.readFloatPref()` handle migrasi SP dari int ke float agar tidak crash.
### ✏️ File Changed
- `app/src/main/java/exp/ftxt/features/battery_temperature/BatteryConfig.java` — `updateInterval` int→float (default 5f)
- `app/src/main/java/exp/ftxt/features/battery_temperature/BatteryModule.java` — `* 1000L` → `(long)(* 1000)`
- `app/src/main/java/exp/ftxt/features/fps_display/FpsConfig.java` — `updateInterval` int→float (default 1f)
- `app/src/main/java/exp/ftxt/features/fps_display/FpsModule.java` — `* 1000L` → `(long)(* 1000)`
- `app/src/main/java/exp/ftxt/features/network_stats/NetworkConfig.java` — `updateInterval` int→float (default 1f)
- `app/src/main/java/exp/ftxt/features/network_stats/NetworkModule.java` — `* 1000L` → `(long)(* 1000)`
- `app/src/main/java/exp/ftxt/features/battery_current/BatteryCurrentConfig.java` — `updateInterval` int→float (default 1f)
- `app/src/main/java/exp/ftxt/features/battery_current/BatteryCurrentModule.java` — `* 1000L` → `(long)(* 1000)`
- `app/src/main/java/exp/ftxt/core/FloatingService.java` — Tambah 4 method update interval static
- `app/src/main/java/exp/ftxt/MainActivity.java` — `readFloatPref()` migrasi SP int→float + load 4 key interval
- `app/src/main/java/exp/ftxt/ui/BatteryPanelController.java` — Interval float steps + `formatIntervalLabel()`
- `app/src/main/java/exp/ftxt/ui/FpsPanelController.java` — Interval float steps + `formatIntervalLabel()`
- `app/src/main/java/exp/ftxt/ui/NetworkPanelController.java` — Interval float steps + `formatIntervalLabel()`
- `app/src/main/java/exp/ftxt/ui/BatteryCurrentPanelController.java` — Interval float steps + `formatIntervalLabel()`
- `app/src/main/res/layout/panel_battery.xml` — Tambah interval controls
- `app/src/main/res/layout/panel_fps.xml` — Tambah interval controls
- `app/src/main/res/layout/panel_network.xml` — Tambah interval controls
- `app/src/main/res/layout/panel_battery_current.xml` — Tambah interval controls
### 🔢 Version
versionCode: 149
versionName: 3.9.3.71.0
---
## [3.9.3.70.0] - 2026-06-11
### ✨ Fitur Baru
- **Kontrol Interval Update Battery Stats** — Tambah tombol - dan + di panel Battery Stats untuk mengatur interval update dari 1-10 detik. Label menampilkan interval aktif (contoh: "Update: 5s"). Interval disimpan di SharedPreferences dan diaplikasikan langsung.
### 🔧 Optimasi & Penyesuaian
- **BatteryModule update interval dinamis** — tickRunnable kini menggunakan `BatteryConfig.updateInterval * 1000L` daripada hardcoded 5000ms.
### 🗒️ File Added
- UI kontrol interval di `app/src/main/res/layout/panel_battery.xml`
### ✏️ File Changed
- `app/src/main/java/exp/ftxt/features/battery_temperature/BatteryConfig.java` — Tambah field `updateInterval` (default 5)
- `app/src/main/java/exp/ftxt/features/battery_temperature/BatteryModule.java` — Update tickRunnable untuk gunakan `BatteryConfig.updateInterval`
- `app/src/main/java/exp/ftxt/core/FloatingService.java` — Tambah method `updateBatteryUpdateIntervalStatic()`
- `app/src/main/java/exp/ftxt/MainActivity.java` — Load `battery_update_interval` dari SharedPreferences di `loadShadowConfigs()`
- `app/src/main/java/exp/ftxt/ui/BatteryPanelController.java` — Tambah fields, binding, loadConfig, listener untuk interval buttons + setup method
- `app/src/main/res/layout/panel_battery.xml` — Tambah LinearLayout dengan tombol -, label interval, dan tombol + setelah color button
### 🔢 Version
versionCode: 147
versionName: 3.9.3.69.9
---
## [3.9.3.69.8] - 2026-06-11
### 🐞 Bug Fixes
- **Semua modul ikut ter-nonaktifkan saat satu modul dimatikan** — Logic di semua panel controller hanya check 1-2 modul lainnya sebelum stop service, menyebabkan modul aktif lainnya ikut mati. Diperbaiki dengan helper function `isAnyModuleActive()` di MainActivity yang check SEMUA 7 modul (Text overlay, FPS, Clock, Battery, Battery%, Battery Current, Network, Watermark).
### 🔧 Optimasi & Penyesuaian
- **Centralized module state check** — Ekstrak logic cek modul aktif dari 8 panel controller ke method `isAnyModuleActive()` di MainActivity untuk consistency dan maintainability.
### ✏️ File Changed
- `app/src/main/java/exp/ftxt/MainActivity.java` — Tambah method `isAnyModuleActive()` yang check semua 7 modul
- `app/src/main/java/exp/ftxt/ui/TextPanelController.java` — Ganti logic check 1 modul dengan `isAnyModuleActive()`
- `app/src/main/java/exp/ftxt/ui/FpsPanelController.java` — Ganti logic check 1 modul dengan `isAnyModuleActive()`
- `app/src/main/java/exp/ftxt/ui/ClockPanelController.java` — Ganti logic check 3 modul dengan `isAnyModuleActive()`
- `app/src/main/java/exp/ftxt/ui/BatteryPanelController.java` — Ganti logic check 3 modul dengan `isAnyModuleActive()`
- `app/src/main/java/exp/ftxt/ui/BatteryPercentagePanelController.java` — Ganti logic check 5 modul dengan `isAnyModuleActive()`
- `app/src/main/java/exp/ftxt/ui/BatteryCurrentPanelController.java` — Ganti logic check 6 modul dengan `isAnyModuleActive()`
- `app/src/main/java/exp/ftxt/ui/NetworkPanelController.java` — Ganti logic check 3 modul dengan `isAnyModuleActive()`
- `app/src/main/java/exp/ftxt/ui/WatermarkPanelController.java` — Ganti logic check 7 modul dengan `isAnyModuleActive()`
### 🔢 Version
versionCode: 146
versionName: 3.9.3.69.8
---
## [3.9.3.69.7] - 2026-06-11
### 🐞 Bug Fixes
- **CI: signing path dobel `app/app/`** — `storeFile=app/release.jks` di keystore.properties, tapi build.gradle resolve path relatif ke direktori `app/`. Diubah jadi `storeFile=release.jks`.
### ✏️ File Changed
- `.github/workflows/release.yml` — storeFile path: `app/release.jks` → `release.jks`
### 🔢 Version
versionCode: 145
versionName: 3.9.3.69.7
---
## [3.9.3.69.6] - 2026-06-11
### 🐞 Bug Fixes
- **APK release tidak signed** — Workflow decode keystore ke `app/release.jks` tapi tidak bikin `keystore.properties`, sehingga signing config di build.gradle tidak aktif. Ditambahkan step generate properties setelah decode.
### ✏️ File Changed
- `.github/workflows/release.yml` — Tambah step Generate keystore.properties setelah decode
### 🔢 Version
versionCode: 144
versionName: 3.9.3.69.6
---
## [3.9.3.69.5] - 2026-06-11
### 🔧 Optimasi & Penyesuaian
- **README.md** — Deskripsi lengkap untuk semua file di struktur project (131 file, 42 direktori)
### ✏️ File Changed
- `README.md` — Struktur Project: semua file + direktori kini punya deskripsi; statistik diupdate
### 🔢 Version
versionCode: 143
versionName: 3.9.3.69.5
---
## [3.9.3.69.4] - 2026-06-11
### 🐞 Bug Fixes
- **release.yml dikembalikan ke versi kerja sebelumnya** — Agent AI sebelumnya merusak workflow: mengganti Java 17→21, mengganti secret names, path keystore, dan struktur workflow yang menyebabkan build gagal. Dikembalikan ke versi `sementara/release.yml` yang terbukti berhasil.
### ✏️ File Changed
- `.github/workflows/release.yml` — Restore dari `sementara/release.yml` (Java 17, secret names `KEYSTORE_BASE64`/`KEYSTORE_PASSWORD`/`KEY_ALIAS`/`KEY_PASSWORD`, decode ke `app/release.jks`, env variables, extract release notes dari CHANGELOG)
- `app/build.gradle` — versionCode 141→142, versionName 3.9.3.69.3→3.9.3.69.4
### 🔢 Version
versionCode: 142
versionName: 3.9.3.69.4
---
## [3.9.3.69.3] - 2026-06-11
### 🔧 Optimasi & Penyesuaian
- **Refactor layout** — Ekstrak semua panel tersisa (Battery Current, Network, Crosshair, Watermark, Logo) dari `activity_main.xml` ke file terpisah menggunakan `<include>`; hapus leftover konten duplikat battery & battery_percentage
- **release.yml** — Upgrade Java 17→21 untuk kompatibilitas keystore (Tag number over 30)
### ✏️ File Changed
- `app/build.gradle` — versionCode 140→141, versionName 3.9.3.69.2→3.9.3.69.3
- `.github/workflows/release.yml` — Java 17→21
- `app/src/main/res/layout/activity_main.xml` — Ekstrak semua panel ke file terpisah; hapus inline content (2620 baris → 41 baris)
### 🗒️ File Added
- `app/src/main/res/layout/panel_battery_current.xml`
- `app/src/main/res/layout/panel_network.xml`
- `app/src/main/res/layout/panel_crosshair.xml`
- `app/src/main/res/layout/panel_watermark.xml`
- `app/src/main/res/layout/panel_logo.xml`
### 🔢 Version
versionCode: 141
versionName: 3.9.3.69.3
---
## [3.9.3.69.2] - 2026-06-11
### 🔧 Optimasi & Penyesuaian
- **activity_main.xml** — Seragamkan format header collapsible section Shadow & Background di panel Battery Percentage dan Battery Current (bold, 16sp, paddingVertical)
- **Refactor layout** — Ekstrak 3 panel pertama (Text, FPS, Clock) dari `activity_main.xml` ke file terpisah (`panel_text.xml`, `panel_fps.xml`, `panel_clock.xml`) menggunakan `<include>` untuk mempercepat build AAPT2
- **PANDUAN.txt, README.txt** — Konversi dari markdown ke plain text untuk in-app display
### ✏️ File Changed
- `app/build.gradle` — versionCode 139→140, versionName 3.9.3.69.1→3.9.3.69.2
- `app/src/main/res/layout/activity_main.xml` — Seragamkan 4 header shadow/background batPct & batCur; ekstrak panel Text, FPS, Clock ke file terpisah (2706 baris, -1504 baris)
### 🗒️ File Added
- `app/src/main/res/layout/panel_text.xml`
- `app/src/main/res/layout/panel_fps.xml`
- `app/src/main/res/layout/panel_clock.xml`
### 🔢 Version
versionCode: 140
versionName: 3.9.3.69.2
---
## [3.9.3.69.1] - 2026-06-11
### ♻️ Perubahan Fitur
- **CHANGELOG.md** — Tambah entry header [1.3.1.9.0 - 1.0.0.0.0] untuk Catatan Major 1; hapus duplikasi
- **release.yml** — Tambah step decode keystore dari org secrets; tambah `mkdir -p key` untuk buat folder di CI runner
### 🔧 Optimasi & Penyesuaian
- **Dokumentasi** — Konsolidasi: STRUKTUR/DEVELOPMENT/TENTANG dihapus, konten di-merge ke README all-in-one
- **build.gradle** — syncDocs hanya sync 3 file; hapus preBuild.dependsOn (txt dikelola manual)
- **activity_documentation.xml / DocumentationActivity.java** — Kurangi daftar dokumen dari 6 ke 3
- **.gitignore** — Hapus `/key`, tambah gitignore spesifik untuk file sensitif di `key/`
- **key/.gitkeep** — File baru agar folder key tetap ter-track walau kosong
- **Refinemen UI — Collapsible Section Grouping** — Semua 8 panel overlay kini dikelompokkan dalam section collapsible (Tampilan, Posisi, Shadow, Background) dengan header clickable ▾/▸. Divider antar section untuk pemisahan visual yang jelas. SectionHelper utility untuk setup reusable.
### 🔥 File Removed
- `STRUKTUR.md`, `DEVELOPMENT.md`, `TENTANG.md` (root)
- `app/src/main/assets/STRUKTUR.txt`, `DEVELOPMENT.txt`, `TENTANG.txt`
- `key/` — 4 sampel dokumentasi
### 🗒️ File Added
- `key/.gitkeep` — Agar folder key tetap ter-track di git
- `app/src/main/java/exp/ftxt/shared/ui/SectionHelper.java` — Utility setup collapsible section header dengan toggle ▾/▸
### ✏️ File Changed
- `.github/workflows/release.yml` — Tambah step decode keystore; tambah `mkdir -p key`
- `.gitignore` — Hapus `/key`, tambah gitignore spesifik untuk file sensitif
- `CHANGELOG.md` — Tambah entry header Catatan; hapus duplikasi
- `app/src/main/assets/CHANGELOG.txt` — Strip section file; tambah entry header Catatan
- `app/src/main/res/layout/activity_documentation.xml` — Hapus 3 button
- `app/src/main/java/exp/ftxt/DocumentationActivity.java` — Hapus 3 referensi
- `app/build.gradle` — syncDocs: 3 file; hapus preBuild.dependsOn
- `app/src/main/res/layout/activity_main.xml` — Tambah section headers collapsible + wrapper container Tampilan, Posisi, Shadow, Background + divider di 8 panel
- `app/src/main/java/exp/ftxt/ui/TextPanelController.java` — Tambah setup collapsible section
- `app/src/main/java/exp/ftxt/ui/FpsPanelController.java` — Tambah setup collapsible section
- `app/src/main/java/exp/ftxt/ui/ClockPanelController.java` — Tambah setup collapsible section
- `app/src/main/java/exp/ftxt/ui/BatteryPanelController.java` — Tambah setup collapsible section
- `app/src/main/java/exp/ftxt/ui/BatteryPercentagePanelController.java` — Tambah setup collapsible section
- `app/src/main/java/exp/ftxt/ui/BatteryCurrentPanelController.java` — Tambah setup collapsible section
- `app/src/main/java/exp/ftxt/ui/NetworkPanelController.java` — Tambah setup collapsible section
- `app/src/main/java/exp/ftxt/ui/WatermarkPanelController.java` — Tambah setup collapsible section
### 🔢 Version
- versionCode: 139
- versionName: 3.9.3.69.1
---
## [3.9.3.69.0] - 2026-06-02
### ✨ Fitur Baru
- **Watermark Seal Pattern** — Mode segel pada watermark: teks diulang diagonal melayar penuh dengan kontrol spasi horizontal, spasi vertikal, dan sudut (angle). Canvas custom view dengan Paint anti-aliased. Aktif via toggle "Mode Segel" di panel watermark.
### ✏️ File Changed
- `app/src/main/java/exp/ftxt/features/watermark/WatermarkConfig.java` — Tambah field pattern (patternEnabled, patternSpacingH, patternSpacingV, patternAngle, patternColor)
- `app/src/main/java/exp/ftxt/features/watermark/WatermarkModule.java` — Tambah SealPatternView inner class, startPattern/startSingle split, updatePattern()
- `app/src/main/java/exp/ftxt/ui/WatermarkPanelController.java` — Tambah kontrol pattern (switch, spacing H/V, angle slider), updatePatternVisibility()
- `app/src/main/res/layout/activity_main.xml` — Tambah watermarkPositionContainer wrapper, pattern controls UI
- `app/src/main/java/exp/ftxt/core/FloatingService.java` — Tambah updateWatermarkPatternStatic delegate
### 🔢 Version
- versionCode: 138
- versionName: 3.9.3.69.0
---
## [3.9.3.68.0] - 2026-06-02
### ✨ Fitur Baru
- **Watermark Overlay** — Modul watermark teks baru dengan teks kustom, warna semi-transparan default (0x55FFFFFF), ukuran (5–200sp), shadow, background, kontrol posisi (slider X/Y, D-Pad, preset full-config), touch passthrough, dan safe area. Panel dapat diakses dari sidebar navigasi.
### 🗒️ File Added
- `app/src/main/java/exp/ftxt/features/watermark/WatermarkConfig.java` — Konfigurasi watermark (teks, ukuran, warna, shadow, background, posisi)
- `app/src/main/java/exp/ftxt/features/watermark/WatermarkModule.java` — Module overlay watermark (ShadowTextView, WindowManager)
- `app/src/main/java/exp/ftxt/ui/WatermarkPanelController.java` — Panel kontrol UI watermark (switch, text, size, color, shadow, background)
- `app/src/main/java/exp/ftxt/ui/WatermarkPositionController.java` — Kontrol posisi watermark (slider, D-Pad, preset)
### ✏️ File Changed
- `app/src/main/java/exp/ftxt/core/FloatingService.java` — Tambah watermarkModule + 11 static delegate methods (start, stop, updateText, updateSize, updateColor, updateShadow, updateBackground, updateTouchFlags, updatePosition, setOrientationSuffix, getPosition)
- `app/src/main/java/exp/ftxt/MainActivity.java` — Tambah WatermarkPanelController, init di onCreate, onResume, nav handler, cleanup; tambah load WatermarkConfig di loadShadowConfigs
- `app/src/main/res/layout/activity_main.xml` — Ganti placeholder "coming soon" dengan panel Watermark lengkap (switch, EditText, size, color, position, shadow, background)
- `app/src/main/res/layout/drawer_content.xml` — Ubah "Watermark (coming soon)" menjadi "Watermark"
### 🔢 Version
- versionCode: 137
- versionName: 3.9.3.68.0
---
## [3.9.3.67.2] - 2026-06-01
### ♻️ Perubahan Fitur
- **Tampilan FPS Display diseragamkan** — Hapus gaya neumorphism (neu_bg, DpadButton, FpsActionButton, PresetButton, ClickableLabel dll) pada panel FPS Display agar konsisten dengan panel fitur lainnya. Semua button dan kontrol kini menggunakan gaya default/inline seperti panel Text, Clock, Battery, dll.
### 🔧 Optimasi & Penyesuaian
- **Bersihkan styles & colors** — Hapus 6 style dan 5 color yang tidak terpakai (FpsActionButton, DpadButton, PresetButton, ActivePresetLabel, ClickableLabel, PanelSectionTitle; neu_bg, neu_bg_pressed, neu_shadow_light, neu_shadow_dark, neu_text).
### ✏️ File Changed
- `app/src/main/res/layout/activity_main.xml` — Hapus `android:background="@color/neu_bg"` dan 20 referensi style neumorphism di panel FPS; ganti dengan inline attributes.
- `app/src/main/res/values/styles.xml` — Hapus 6 style tidak terpakai.
- `app/src/main/res/values/colors.xml` — Hapus 5 color neumorphism tidak terpakai.
### 🔢 Version
- versionCode: 136
- versionName: 3.9.3.67.2
---
## [3.9.3.67.1] - 2026-06-01
### 🐞 Bug Fixes
- **CI build gagal — SDK license & platform 35** — Workflow release.yml tidak accept SDK licenses dan tidak install platform 35 (compileSdk). Ditambahkan step `sdkmanager --licenses` dan install `platforms;android-35` + `build-tools;35.0.0`. Juga fallback `ANDROID_SDK_ROOT` jika `ANDROID_HOME` tidak diset.
### ✏️ File Changed
- `.github/workflows/release.yml` — Tambah step Install SDK components & accept licenses; `local.properties` pakai fallback `ANDROID_HOME`/`ANDROID_SDK_ROOT`; `./gradlew` pakai `--no-daemon`
### 🔢 Version
- versionCode: 135
- versionName: 3.9.3.67.1
---
## [3.9.3.67.0] - 2026-06-01
### ✨ Fitur Baru
- **PresetBrowserDialog** — Dialog browser preset modern dengan search, filter, color thumbnail, favorite, rename, delete, reorder, dan export/import. Menggantikan AlertDialog radio-list lama.
- **Active Preset Label** — Setiap panel (7 module) menampilkan label preset aktif di atas tombol preset.
### 🔧 Optimasi & Penyesuaian
- **Java 8 → Java 17** — sourceCompatibility/targetCompatibility dinaikkan ke Java 17 menghilangkan deprecation warning dari JDK 21.
- **Ekstrak Logika Preset ke PresetHandler** — `showSavePresetDialog`, `doSavePreset`, `showLoadPresetDialog`, `applyPreset`, dan `showExportImportMenu` diekstrak dari semua 7 PositionController ke `shared/preset/PresetHandler.java` dengan pola Delegate interface. Menghapus ~800 baris kode duplikat.
- **GitHub Actions Workflow** — Workflow CI/CD untuk build & release APK otomatis saat push tag v\*.
### 🐞 Bug Fixes
- **Import FC di PresetBrowserDialog** — `registerForActivityResult` dipindah ke `onCreate` Fragment lifecycle, bukan lazy di click handler.
- **CI build Gagal — AAPT2 not found** — Workflow gagal karena `local.properties` mengarah ke SDK path lokal yang tidak ada di runner. Ditambahkan step generate dari `$ANDROID_HOME`.
- **CI build Gagal — AAPT2 masih not found** — `android.aapt2FromMavenOverride` di `gradle.properties` merujuk ke path tidak ada. Dihapus.
### ♻️ Perubahan Fitur
- **Tombol E/I dihapus** dari 7 panel — fungsi ekspor/impor/bagikan sudah ada di PresetBrowserDialog. Metode `PresetHandler.showExportImportMenu()` dihapus.
### 🗒️ File Added
- `app/src/main/java/exp/ftxt/shared/preset/PresetBrowserDialog.java` — DialogFragment browser preset dengan search, list, favorite, rename, delete, reorder.
- `app/src/main/res/layout/dialog_preset_browser.xml` — Layout dialog browser preset (search bar + list + bottom bar).
- `app/src/main/res/layout/preset_browser_item.xml` — Layout per-item preset (color thumbnail + nama + tags + favorite star).
- `app/src/main/java/exp/ftxt/shared/preset/PresetHandler.java` — Class baru dengan `Delegate` interface + static methods untuk handle semua operasi preset.
- `.github/workflows/release.yml` — Workflow: trigger tag v\*, Java 17, assembleRelease, upload ke GitHub Release.
### ✏️ File Changed
- `app/src/main/java/exp/ftxt/shared/preset/PresetManager.java` — Tambah `color` di index metadata untuk quick thumbnail.
- `app/src/main/java/exp/ftxt/shared/preset/PresetHandler.java` — `showLoadPresetDialog()` pakai `PresetBrowserDialog`; hapus `showExportImportMenu`.
- `app/src/main/java/exp/ftxt/shared/preset/PresetBrowserDialog.java` — `importLauncher` pakai `registerForActivityResult` di `onCreate`.
- `app/src/main/res/layout/activity_main.xml` — Tambah `TextView` active preset label di 7 panel; hapus tombol E/I dari 7 panel.
- All 7 PositionControllers — Tambah `activePresetLabel`, update di `syncAll()`; hapus `btnExportImport`, `fileImportLauncher`, E/I listener; gunakan PresetHandler.Delegate.
- `gradle.properties` — Hapus `android.aapt2FromMavenOverride`.
- `STRUKTUR.md` + `STRUKTUR.txt` — Tambah `PresetBrowserDialog.java`, `dialog_preset_browser.xml`, `preset_browser_item.xml`
- `DEVELOPMENT.md` + `DEVELOPMENT.txt` — Java requirement 1.8+→17+, "Java 1.8 compatible"→"Java 17 compatible"
- `.github/workflows/release.yml` — Tambah step "Setup Android SDK"; hapus `android.enableAapt2FromMaven` (obsolete di AGP 8.12)
### 🔢 Version
- versionCode: 134
- versionName: 3.9.3.67.0
---
## [3.9.3.66.0] - 2026-06-01
### ✨ Fitur Baru
- **Tombol Muat di Semua Panel** — Tombol "Muat Preset" (Load) kini muncul sebagai button fisik di layout setiap panel, posisinya di antara Simpan dan E/I. Sebelumnya hanya tersedia di menu popup toolbar (gear icon).
### ✏️ File Changed
- `app/src/main/res/layout/activity_main.xml` — Tambah btnLoadPreset di 7 module sections
- All 7 PositionControllers — setupListeners: +btnLoadPreset onClick → showLoadPresetDialog()
### 🔢 Version
- versionCode: 127
- versionName: 3.9.3.66.0
---
## [3.9.3.65.0 – 3.9.3.63.0] - 2026-05-31–06-01
Menggabungkan 3 release.
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
- **Toast Feedback Ekspor** — Saat preset berhasil diekspor ke file, aplikasi kini menampilkan Toast "Preset diekspor ke Downloads/" sebagai konfirmasi visual.
- **File Picker Impor Preset** — Tombol "Impor dari File" kini membuka file picker system (`ActivityResultContracts.OpenDocument`) untuk memilih file preset.
- **Bagikan Preset di Semua Panel** — Menu "Bagikan Preset" kini tersedia di semua 7 panel overlay, sebelumnya hanya di panel Text.
- **Preset Full-Konfigurasi** — Preset kini menyimpan SELURUH konfigurasi overlay (posisi, ukuran, warna, shadow, background, orientasi, touchPassthrough, safeArea, toggle display spesifik per modul). Backward compatible.
### ♻️ Perubahan Fitur
- **Export/Import UI Updated** — Semua tombol Export/Import di position controllers diganti dari "Ekspor/Impor ke Clipboard" menjadi "Ekspor ke File" / "Impor dari File".
- **Share Menu** — Tambah opsi "Bagikan Preset" di export menu, memanggil `PresetManager.sharePreset(name)`.
- **OverlayPreset model diperluas** — 9 field baru: touchPassthrough, safeArea, textContent, showOnlyValue, showTemperature, showPercentage, showVoltage, showCurrent, showPower.
### 🚮 Fitur Dihapus
- **Clipboard-based Export/Import** — Method `exportToClipboard(activity)` dan `importFromClipboard(activity)` dihapus.
### 🐞 Bug Fixes
- **Extra brace di TextPositionController** — Brace `}` berlebih di baris 221 menyebabkan class ditutup prematur (28 error kompilasi).
- **Teks hitam di tema gelap saat pertama buka** — `AppCompatDelegate.setDefaultNightMode()` dipanggil setelah `SplashScreen`.
### 💡 Catatan
- Backward compatibility: preset lama (name-key based) otomatis dimigrasikan ke UUID index.
- Thumbnail di-generate saat `save()` berdasarkan warna overlay (64x64px PNG).
- History capped 10 items per preset; item lama otomatis di-pop.
- Clipboard sharing dihapus; file-based sharing lebih reliable di Android 10+.
- Tags dan Favorite flags disimpan di index metadata (SharedPreferences).
### 🔢 Version
- versionCode: 126
- versionName: 3.9.3.63.0
---
## [3.9.2.62.0 – 2.8.2.56.0] - 2026-05-16–06-01
Menggabungkan 5 release.
### ✨ Fitur Baru
- **Safe Area "Gunakan Area Aman"** — Menambahkan checkbox "Gunakan Area Aman" pada semua 6 panel overlay (Text, FPS, Clock, Battery, Battery Current, Network) untuk mengaktifkan/nonaktifkan pembatasan posisi agar tetap dalam area aman layar.
- **Auto-request izin saat pertama buka** — Aplikasi otomatis meminta semua izin (Overlay, Notifikasi, Optimasi Baterai) saat pertama kali dijalankan.
- **Battery Stats toggles °C / %** — Panel Battery Stats (sebelumnya Battery Temperature) kini memiliki dua checkbox toggle °C dan % untuk mengontrol tampilan suhu dan persentase baterai secara independen. Keduanya bisa aktif bersamaan atau salah satu saja.
- **Battery Percentage Overlay** — Fitur baru untuk menampilkan persentase baterai sebagai overlay. Update tiap 5 detik, mendukung semua konfigurasi: ukuran, warna, shadow, background, kontrol posisi, preset, dan orientasi.
- **Battery Current Overlay** — Fitur baru untuk menampilkan tegangan (mV), arus (mA), dan daya (W) baterai sebagai overlay. Update tiap 5 detik, mendukung semua konfigurasi: ukuran, warna, shadow, background, kontrol posisi, preset, dan orientasi.
- **Sistem Preset Full-Konfigurasi (GSON) terintegrasi ke UI** — Tombol Simpan, Muat, dan E/I pada semua 5 panel (Text, FPS, Clock, Battery, Network) sekarang menyimpan/memuat FULL konfigurasi overlay (posisi, ukuran, warna, shadow, background, orientasi) via PresetManager+OverlayPreset (GSON), bukan hanya posisi X/Y seperti sebelumnya.
### ♻️ Perubahan Fitur
- **Tema default mode malam** — Tema aplikasi sekarang default ke mode gelap (malam) saat pertama kali dijalankan.
- **Placeholder panel Crosshair, Watermark, Logo** — Tiga panel kosong kini menampilkan pesan "Konsep overlay sedang dalam tahap pengerjaan" sebagai pengganti panel Text saat nav item diklik.
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
- **Tombol Potret/Lanskap dipindah ke toolbar**: Tombol orientasi Potret dan Lanskap dihapus dari panel kontrol posisi (Text, FPS, Clock, Battery, Network) dan digantikan dengan ikon orientasi layar di toolbar samping ikon tema. Ketuk ikon untuk toggle orientasi layar antara potret dan lanskap.
### 🚮 Fitur Dihapus
- **Tombol Potret/Lanskap per panel**: Button `btnPortrait`/`btnLandscape` dihapus dari kelima panel overlay.
- **PositionPresetManager** — Sistem preset posisi X/Y lama digantikan total oleh PresetManager berbasis GSON. File dipindahkan ke `_karantina/` untuk referensi jika diperlukan kembali.
### 💡 Catatan
- Mulai rilis ini, aplikasi menggunakan label **"FunText"** dan menandai status **Beta**. Seluruh rilis berikutnya akan menggunakan format `FunText vX.X.X.X.X Beta` pada sidebar header.
### 🔢 Version
- versionCode: 122
- versionName: 2.8.2.56.0
---
## [2.7.2.54.0 – 2.7.2.49.0] - 2026-05-16–06-01
Menggabungkan 5 release.
### ✨ Fitur Baru
- **Sistem Manajemen Preset Overlay (GSON)** — Sistem preset baru berbasis GSON yang menyimpan seluruh konfigurasi overlay (posisi, ukuran, warna, shadow, background, orientasi) dalam format JSON. Mendukung Save, Load, Rename, Select, Delete, Export (clipboard/file), dan Import (clipboard/file).
- **Network Speed Meter Overlay** — Memantau kecepatan internet real-time (↓ upload / ↑ download) dengan format otomatis KB/s ↔ MB/s, polling tiap 1 detik via TrafficStats.
- **Network Speed Position Controls** — Slider X/Y, D-Pad, preset save/load, orientasi Potret/Lanskap untuk Network Speed overlay.
### ♻️ Perubahan Fitur
- **Tombol preset jadi satu baris**: Simpan, Muat, dan E/I dalam satu baris horizontal. Reset dihapus.
- **Ubah teks button**: "Simpan Preset" → "Simpan", "Muat Preset" → "Muat".
- **Semua toggle Switch → CheckBox**: `overlaySwitch`, `touchPassthroughSwitch`, `shadowSwitch`, `bgSwitch` dan varian fps/clock/battery diubah dari Switch menjadi CheckBox di semua 4 panel.
- **Overlay & Kunci Posisi sejajar horizontal**: Setiap panel kini menampilkan CheckBox overlay dan kunci posisi dalam satu baris horizontal.
- **Urutan komponen diubah semua panel**: Kontrol Posisi dipindah ke bawah Pilih Warna; Background dipindah ke paling bawah (di bawah Shadow).
- **Label teks Switch dihapus**: Teks langsung di `android:text` CheckBox, TextView label samping dihapus.
### 🚮 Fitur Dihapus
- **Grid Posisi 3×3 FPS/Clock/Battery**: Tombol grid 3×3 untuk quick preset posisi dihapus dari panel FPS Display, Jam Digital, dan Suhu Baterai — melanjutkan penghapusan yang sudah dilakukan di panel Floating Text.
- **Auto Preset Aplikasi FPS**: Fitur auto-switch preset berdasarkan aplikasi di FPS dihapus (sudah dihapus dari Text di release sebelumnya).
### 🔧 Optimasi & Penyesuaian
- **Warna sidebar ikut tema terang/gelap**: Drawer background dan header tidak lagi hardcoded. Pakai `@color/drawer_background` dan `@color/drawer_header_background` dengan variant `values-night/colors.xml` untuk mode gelap.
- **Gradle namespace syntax**: `namespace "exp.ftxt"` diganti `namespace = "exp.ftxt"` untuk kompatibilitas Gradle 10.
### 🐞 Bug Fixes
- **TextPositionController crash**: Grid 3×3 buttons (`btnPosTL`–`btnPosBR`), orientation buttons (`btnPortrait`/`btnLandscape`), dan auto-preset switch (`autoPresetSwitch`) dihapus dari layout Text panel di release sebelumnya, tapi kode Java masih mereferensinya — menyebabkan `cannot find symbol` saat kompilasi. Semua referensi dihapus dari `TextPositionController.java`.
### 🔢 Version
- versionCode: 113
- versionName: 2.7.2.49.0
---
## [2.6.2.48.0 – 2.6.1.43.0] - 2026-05-16–06-01
Menggabungkan 5 release.
### ✨ Fitur Baru
- **Preset Preview**: Tampilan mini-map posisi pada daftar preset saat memuat.
- **Preset Export/Import**: Ekspor preset ke clipboard (JSON), impor dari clipboard.
- **More Preset Slots**: Jumlah maksimal preset ditingkatkan dari 10 menjadi 50.
- **Preset Rename**: Ubah nama preset langsung dari opsi long-press (sudah ada, ditingkatkan).
- **Visual Preset Editor**: Editor posisi visual dengan drag dot pada mini-map.
- **Visual Preset Indicator**: Indikator preset aktif ditampilkan di panel kontrol posisi.
- **Jam Digital Overlay**: Overlay waktu real-time 24 jam format `HH:mm:ss`, update tiap 1 detik. Dapat diatur warna, ukuran, background, shadow, posisi (drag atau kunci), plus kontrol posisi lengkap (slider X/Y, D-Pad, grid 3×3, preset, orientasi). Default ukuran 48sp.
- **Suhu Baterai Overlay**: Overlay suhu baterai dalam Celsius (°C), update tiap 5 detik. Membaca via `BatteryManager.EXTRA_TEMPERATURE`. Opsi tampilkan hanya nilai. Dilengkapi kontrol posisi lengkap (slider X/Y, D-Pad, grid 3×3, preset, orientasi).
- **Kontrol Posisi FPS Display**: Tambah Slider X/Y, D-Pad, preset posisi, dan tombol orientasi (Potret/Lanskap) pada panel FPS Display — menyamai yang sudah ada di Floating Text. Posisi disimpan terpisah per orientasi dengan sistem normalized 0.0–1.0.
- **Tampilan Koordinat Posisi Real-time**: Label "Kontrol Posisi" kini menampilkan koordinat pixel sesungguhnya (contoh: `540X1200`) yang berubah langsung saat slider, D-Pad, preset, atau drag overlay — menggunakan `getRealMetrics()` untuk akurasi full screen.
### 🚮 Fitur Dihapus
- **Grid Posisi 3×3**: Tombol grid 3×3 untuk quick preset posisi dihapus karena kurang berguna. User dapat menggunakan slider X/Y, D-Pad, atau preset system yang lebih flexible.
- **Auto Preset Aplikasi**: Fitur auto-switch preset berdasarkan orientasi dihapus. Orientasi mode (Potret/Lanskap) masih tersedia melalui kontrol posisi manual.
### 🔧 Optimasi & Penyesuaian
- **Layout Kontrol Posisi**: D-Pad dipindah ke kanan sejajar dengan Slider X/Y dalam satu baris horizontal. Tombol preset (Simpan Preset, Muat Preset, Reset) diberi `singleLine` agar teks tidak wrapping dan sejajar sempurna.
### 🐞 Bug Fixes
- **Slider Posisi**: Slider X/Y tidak tersinkronisasi saat overlay dipindahkan dengan mode drag. Kini slider mengikuti posisi overlay secara real-time.
- **Sinkronisasi semua overlay**: Perbaikan untuk Text, FPS, Battery, dan Clock overlay.
- **Padding Background Mendorong Teks**: `applyBackground()` selalu set padding (`bgPadding=25`) meski background mati, mendorong teks menjauh dari tepi view — menyebabkan gap kecil di X=0, Y=0. Sekarang padding cuma diterapkan saat `bgEnabled=true`.
- **Hal yang sama diterapkan di `FpsModule.applyBackground()` untuk konsistensi.
### 🔢 Version
- versionCode: 106
- versionName: 2.6.2.48.0
---
## [2.6.1.42.0 – 2.4.1.38.0] - 2026-05-16–06-01
Menggabungkan 5 release.
### ✨ Fitur Baru
- **Orientasi Mode Posisi**: Tambah tombol [Potret] [Lanskap] di kontrol posisi untuk mengatur posisi overlay secara terpisah per orientasi. Setiap perubahan posisi otomatis tersimpan ke orientasi yang sedang aktif.
- **Hapus Preset Posisi**: Tahan preset 2 detik di dialog Muat Preset untuk memunculkan dialog konfirmasi hapus dengan tombol [Ya] [Batal].
- **Classic Color Wheel (Full Disk)**: Color Picker ARGB Sliders kini dilengkapi Color Wheel penuh dengan indikator crosshair. Saturation mencakup seluruh area wheel (full disk), bukan hanya area dalam.
- **Two-Way Synchronization**: Pergerakan crosshair di Color Wheel otomatis menyesuaikan slider R, G, B secara real-time, dan sebaliknya. Dilengkapi flag `isUpdating` untuk mencegah infinite loop.
- **Drag Cross-Container**: Item sidebar kini bisa di-drag bebas antar grup (Overly, Fitur, grup kustom). Pindahkan item dari satu grup ke grup lain dengan long-press dan drop.
- **Mode Hapus**: Tombol "Hapus" di sidebar mengaktifkan mode delete. Setiap item mendapat CheckBox (☐/☑). Pilih item yang ingin dihapus, lalu tekan [Hapus] di footer untuk eksekusi. [Batal] untuk membatalkan.
- **Penyimpanan State Penuh**: Semua state sidebar (grup built-in + kustom, urutan item, status collapse/expand) disimpan dan dipulihkan sepenuhnya.
### ♻️ Perubahan Fitur
- **PositionController**: Referensi XY Pad dihapus — turun dari 159 ke 153 baris. Sekarang hanya mengelola Slider + D-Pad + Preset + Orientasi.
- **Penyimpanan Posisi Langsung**: `onPositionChanged()` kini langsung menyimpan posisi ke SharedPreferences, tidak hanya saat drag berhenti.
- **Tata Letak Kontrol Posisi**: XY Pad diubah menjadi vertikal (250dp, `layout_weight="1"`) dan D-Pad dipindah ke samping kanan dalam satu baris horizontal, mengisi ruang kosong yang sebelumnya terbuang.
- **Refactor Package Structure**: Rename folder `modules` → `features` untuk naming yang lebih konsisten dan semantik. Semua import statements dan dokumentasi internal telah diperbarui.
- **Drag-to-Reorder**: Sekarang mendukung cross-container drag. Item bisa dipindah antar grup manapun.
- **rebuildSidebar()**: Method baru untuk membangun ulang seluruh sidebar dari state JSON. Dipanggil saat `onCreate()` dan setelah operasi penghapusan.
- **saveSidebarState() / loadSidebarState()**: Menggantikan `saveCustomGroups()` / `loadCustomGroups()`. Menyimpan state penuh semua grup (built-in + kustom).
- **Collapse/Expand Built-in**: Grup Overlay dan Fitur kini dapat di-collapse/expand (sebelumnya hanya grup kustom yang bisa).
### 🚮 Fitur Dihapus
- **Karantina XY Pad**: `XyPadView.java` dipindahkan dari `shared/ui/` ke `_karantina/` untuk diarsipkan. XY Pad tidak lagi aktif di UI. Dapat dipulihkan kembali di masa depan dengan mengembalikan file ke `shared/ui/` dan menghubungkan kembali di `PositionController` + layout.
- **Hapus Sistem Grup Sidebar**: Navigasi sidebar tidak lagi menggunakan grup (Overlay, Fitur). Semua item ditampilkan flat dalam satu daftar. Tombol "Tambah Grup" dihapus. Grup kustom yang tersimpan otomatis dimigrasi ke format flat.
- **Hapus Tombol "Hapus" Sidebar**: Tombol dan mode hapus item di footer sidebar dihapus untuk menyederhanakan navigasi.
- **Hapus "Tambah Item" Per-Grup**: Setiap grup kustom tidak lagi memiliki EditText + Button "+" untuk menambah item. Item hanya bisa ditambah melalui mekanisme internal (drag antar grup atau restore state).
### 🔧 Optimasi & Penyesuaian
- **Ekstrak D-Pad ke Shared Component**: Logika D-Pad (touch listener dengan repeat) diekstrak dari `PositionController.java` ke `shared/ui/DpadController.java` — mengurangi duplikasi dan meningkatkan modularitas, mengikuti pola `XyPadView` dan `SliderLabelEditor`.
- **Ekstrak Preset Posisi ke Shared Component**: Sistem preset posisi (simpan/load/hapus dengan long-press) diekstrak dari `PositionController.java` ke `shared/ui/PositionPresetManager.java` — mengurangi kompleksitas `PositionController` dari 350 menjadi 159 baris.
- **Ekstrak Slider X/Y ke Shared Component**: Slider SeekBar posisi X dan Y (dengan label) diekstrak ke `shared/ui/SliderPositionController.java` — masing-masing dengan `isUpdating` guard sendiri untuk mencegah infinite loop saat sinkronisasi.
- **Auto-Apply Posisi Orientasi**: Posisi overlay kini otomatis diterapkan saat orientasi layar berubah, tanpa perlu menekan tombol [Potret]/[Lanskap] secara manual.
- **Hapus Entry Dokumentasi dari Sidebar**: Navigasi "Dokumentasi" dihapus dari drawer kiri karena sudah ada akses "Lihat Dokumentasi" melalui popup Pengaturan (ikon gear).
- **Hapus module files deprecated**: Folder `modules/` yang sudah tidak dipakai (digantikan `features/`) dihapus dari filesystem.
### 🐞 Bug Fixes
- **Overlay Baru Pakai Dimensi Layar Lama**: `TextModule.createOverlay()` menggunakan `screenWidth`/`screenHeight` yang disimpan saat `init()`. Jika overlay dihapus lalu dibuat ulang setelah rotasi layar, posisinya dihitung dengan dimensi lama. Sekarang `createOverlay()` juga refresh dimensi dari `WindowManager`.
- **Suffix Orientasi Tidak Sinkron**: `TextModule.posSuffix()` selalu membaca orientasi perangkat fisik, sementara `PositionController` bisa dioverride manual (tombol Potret/Lanskap). Akibatnya, drag overlay menyimpan posisi ke key orientasi yang salah. Sekarang `TextModule` punya `orientationSuffix` yang bisa di-set dari `PositionController`, dan di-reset tiap `PositionController` dibuat atau berganti mode.
- **Posisi Layar Landscape Tidak Akurat**: `screenWidth`/`screenHeight` di `TextModule` menggunakan dimensi layar saat service start (tidak update saat orientasi berubah). Akibatnya slider, D-Pad, dan XY Pad menghitung posisi dengan ukuran layar yang salah. Sekarang `updatePosition()`, `loadPosition()`, dan `savePosition()` selalu mengambil dimensi layar terkini.
- **XyPad Tidak Bisa Di-drag**: `NestedScrollView` meng-intercept sentuhan pada `XyPadView` sehingga dot tidak bisa digerakkan. Ditambahkan `requestDisallowInterceptTouchEvent(true)` pada ACTION_DOWN dan `false` pada ACTION_UP/CANCEL.
- **Handler Memory Leak**: `Handler` pada `PositionController` (repeatHandler untuk D-pad dan holdHandler untuk long-press hapus preset) tidak pernah dibersihkan saat Activity di-destroy. Ditambahkan metode `cleanup()` yang dipanggil di `MainActivity.onDestroy()`.
- **ScrollView pada Panel**: Panel Floating Text dan FPS Display tidak bisa di-scroll saat konten melebihi layar (misal konfigurasi Background/Shadow terbuka). Ditambahkan `ScrollView` dengan `fillViewport="true"` pada kedua panel.
- **Sidebar Tidak Responsif**: Item sidebar yang dibuat ulang secara programmatic oleh `rebuildSidebar()` tidak memiliki `OnClickListener`, sehingga tidak bisa dipilih. Ditambahkan listener yang meng-handle navigasi panel, update title, simpan state, dan tutup drawer.
### 🔥 File Removed
- `app/src/main/java/exp/ftxt/shared/ui/XyPadView.java` → dipindah ke `_karantina/exp/ftxt/shared/ui/XyPadView.java`
- app/src/main/java/exp/ftxt/modules/text/TextConfig.java
- app/src/main/java/exp/ftxt/modules/text/TextModule.java
- app/src/main/java/exp/ftxt/modules/fps/FpsConfig.java
- app/src/main/java/exp/ftxt/modules/fps/FpsModule.java
- app/src/main/java/exp/ftxt/modules/text/TextConfig.java (deprecated)
- app/src/main/java/exp/ftxt/modules/text/TextModule.java (deprecated)
- app/src/main/java/exp/ftxt/modules/fps/FpsConfig.java (deprecated)
- app/src/main/java/exp/ftxt/modules/fps/FpsModule.java (deprecated)
### 🔢 Version
- versionCode: 96
- versionName: 2.6.1.42.0
---
## [2.3.1.37.0 – 2.3.1.32.0] - 2026-05-16–06-01
Menggabungkan 5 release.
### ✨ Fitur Baru
- **Tambah Grup**: Tombol "+ Tambah Grup" di sidebar untuk membuat grup kustom baru. Setiap grup memiliki header collapsible dan bisa ditambahi item sendiri.
- **Drag-to-Reorder**: Long-press item sidebar (built-in maupun kustom) lalu drag ke posisi lain untuk mengurutkan ulang.
- **Penyimpanan Grup**: Grup kustom dan urutannya disimpan otomatis ke SharedPreferences dan dipulihkan saat aplikasi dibuka kembali.
- **Sidebar Collapsible**: NavigationView diganti dengan drawer custom. Grup "Overlay" (Floating Text, FPS Display) dan "Fitur" (Coming Soon) bisa di‑expand/collapse dengan toggle −/+.
- **Kategori Fitur**: 7 modul Coming Soon (Network, Battery, Clock, CPU, Crosshair, Watermark, Logo) dikelompokkan dalam grup "Fitur" yang bisa ditutup.
- **DocumentationActivity**: Antarmuka khusus dokumentasi dengan daftar 6 dokumen (README, CHANGELOG, PANDUAN, STRUKTUR, DEVELOPMENT, TENTANG) — bukan lagi dialog tengah, melainkan halaman penuh seperti Settings.
- **Drawer → Dokumentasi**: Entry "Dokumentasi" ditambahkan di navigation drawer (di luar grup module) untuk akses cepat tanpa melalui popup settings.
- **Konfirmasi Keluar**: Opsi checkbox di Konfigurasi — ketika aktif, tombol kembali harus ditekan dua kali untuk keluar dari aplikasi. Toast "Tekan kembali lagi untuk keluar" muncul pada tekan pertama.
- **Kontrol Posisi 3-in-1**: Tambah sistem kontrol posisi untuk Floating Text dengan 3 metode input sinkron: Slider X/Y, D-Pad (↑↓←→, tahan untuk repeat), dan XY Pad (drag area 2D). State posisi menggunakan nilai float 0.0–1.0 yang dibagikan.
- **Shared State**: Semua kontrol membaca/menulis ke state `TextConfig.posX`/`posY` yang sama dengan guard `isUpdating` untuk hindari infinite loop.
- **Position Migration**: Migrasi posisi dari pixel absolut (`text_x`/`text_y`) ke persentase layar (`text_pos_x`/`text_pos_y`) dengan backward compatibility otomatis.
- **XyPadView**: Custom View 2D drag area di `shared/ui/XyPadView.java`.
- **PositionController**: Controller modular untuk semua kontrol posisi di `ui/PositionController.java`.
- **Potret/Landscape Auto-Config**: Posisi overlay disimpan terpisah untuk mode portrait dan landscape, otomatis berganti saat orientasi berubah.
- **Preset Posisi**: Simpan/load posisi favorit (max 10 preset) dengan nama kustom.
- **Reset Posisi**: Tombol untuk mengembalikan posisi ke tengah layar (0.5, 0.5).
- **Background Margin & Radius**: Tambah slider margin (0–30 px) dan radius/rounded corner (0–50 px) pada konfigurasi background untuk modul Floating Text dan FPS Display.
- **Direct Label Edit**: Klik label margin dan radius untuk mengedit nilai langsung via dialog input.
- **Label Slider Menampilkan Nilai**: Semua label slider di panel Floating Text dan FPS Display kini menampilkan nilai numerik terkini (contoh: "Ukuran Teks: 20", "Offset X: 0").
- **Edit Nilai Slider Manual**: Klik label slider untuk mengedit nilai numerik langsung via dialog input—slider menyesuaikan otomatis.
### 🔧 Optimasi & Penyesuaian
- **Tampilan Dokumentasi**: Entry dokumen diubah dari tombol (button) menjadi daftar teks dengan garis pemisah — lebih bersih, seperti daftar.
- **Toolbar Biru**: Toolbar SettingsActivity dan DocumentationActivity kini menggunakan warna biru `#2196F3` (sama seperti toolbar utama), bukan hitam.
- **Judul Toolbar**: SettingsActivity berganti judul dari "Pengaturan" menjadi "Konfigurasi".
- **Popup di Bawah Ikon**: Popup settings kini muncul sebagai dropdown di bawah ikon pengaturan (PopupMenu + Gravity.END), bukan dialog tengah.
- **Tutup Aplikasi Pindah ke Popup**: Tombol "Tutup Aplikasi" dipindah dari SettingsActivity ke popup settings, bisa diakses dari mana saja.
- **Popup Settings**: Tombol pengaturan (gear) kini menampilkan popup dengan dua opsi: "Konfigurasi" (izin + Tutup Aplikasi) dan "Lihat Dokumentasi" (6 dokumen).
- **Dokumentasi via Popup**: Dokumentasi dipindah dari SettingsActivity ke popup di MainActivity — akses lebih cepat tanpa buka halaman baru.
- **SettingsActivity Ringkas**: Hanya berisi Konfigurasi (izin overlay/notifikasi/baterai + Tutup Aplikasi). Tombol dokumentasi dihapus.
- **Dokumentasi Dipisah**: README.md dipecah menjadi 4 file terpisah untuk modularitas:
  - `STRUKTUR.md` — Struktur project & deskripsi file
  - `PANDUAN.md` — Panduan penggunaan lengkap
  - `DEVELOPMENT.md` — Info teknis, environment, versioning
  - `TENTANG.md` — Lisensi, author, support
- README.md kini ringkas dengan ringkasan fitur dan referensi ke file dokumentasi terkait.
- Semua file memiliki versi .md (root) dan .txt (assets) yang disinkronkan secara manual.
- SettingsActivity mendapat 4 tombol baru untuk membaca file dokumentasi baru.
- Gradle task `syncDocs` diperbarui untuk menyertakan semua file dokumentasi baru.
- **Slider Label Editor Shared Utility**: Ekstrak `showSliderEditor()` dan `showOffsetEditor()` ke `shared/ui/SliderLabelEditor.java` untuk menghilangkan duplikasi kode antara `TextPanelController` dan `FpsPanelController`.
- **Android SplashScreen API**: Implementasi SplashScreen API resmi Android menggunakan icon launcher aplikasi. Splash screen menampilkan icon app dengan background warna tema, tanpa custom splash activity atau fake loading delay.
- **Offset Range -60 hingga 60**: Semua offset (Background X/Y dan Shadow X/Y) di panel Floating Text dan FPS Display sekarang memiliki range -60 hingga 60, dengan posisi default 0 di tengah. SeekBar memetakan progress 0-120 ke offset -60..60.
- **Default Shadow Offset**: Nilai default shadow offset X/Y diubah dari 3 menjadi 0.
### 🔢 Version
- versionCode: 81
- versionName: 2.3.1.37.0
---
## [2.3.1.31.0 – 2.3.1.26.0] - 2026-05-16–06-01
Menggabungkan 5 release.
### ✨ Fitur Baru
- **Hanya Tampilkan Nilai FPS**: Tambah toggle di panel FPS untuk menyembunyikan teks "FPS" dan hanya menampilkan angka.
- **Versi di Sidebar**: Tambah nomor versi terkini di samping nama aplikasi di header navigation drawer (contoh: FTxT v2.3.1.30.0).
- **Edit HEX Manual**: Tambah ikon sunting di samping nilai HEX untuk mengedit warna secara manual via input teks.
- **Edit ARGB Langsung**: Klik label R, G, B, A untuk mengedit nilai numerik—slider menyesuaikan otomatis.
- **Salin Nilai Warna**: Tekan lama nilai HEX, HSV, atau ARGB untuk menyalin ke clipboard.
- **Crosshair & Watermark**: Tambah entry modul Crosshair dan Watermark di navigation drawer (Coming Soon).
- **Hapus Temperature**: Hapus entry modul Temperature dari navigation drawer.
- **Akses Izin di Pengaturan**: Toggle switch untuk semua izin aplikasi (Overlay, Notifikasi, Optimasi Baterai) di halaman Settings, di atas CHANGELOG/README.
  - Izin Overlay: buka halaman izin overlay sistem.
  - Izin Notifikasi: minta izin runtime (Android 13+).
  - Optimasi Baterai: buka halaman nonaktifkan optimasi baterai.
  - Status switch otomatis sinkron saat halaman dibuka/kembali.
### 🐞 Bug Fixes
- **Dokumentasi Color Model**: Perbaiki dokumentasi model warna di README dari HSV (lama) menjadi ARGB sesuai implementasi color picker saat ini.
- **Switch Modul Tidak Minta Izin Mandiri**: Switch Floating Text dan FPS Display sekarang hanya mati diam-diam tanpa meminta izin overlay. Izin harus dinyalakan dulu lewat Settings > Akses Izin > Izin Overlay.
- **Overlay Permission Gate**: Switch Floating Text dan FPS Display sekarang menolak menyala jika izin Overlay belum diberikan. Switch otomatis kembali ke OFF dan meminta izin.
### 🔥 File Removed
- `app/src/main/java/exp/ftxt/modules/temp/`
### 🔢 Version
- versionCode: 65
- versionName: 2.3.1.31.0
---
## [2.3.1.25.0 – 2.3.1.21.0] - 2026-05-16–06-01
Menggabungkan 5 release.
### ✨ Fitur Baru
- **Force Close**: Tombol "Tutup Aplikasi" dipindah ke SettingsActivity (di bawah CHANGELOG/README, pojok kiri bawah).
  - Hapus `nav_exit` dari navigation drawer.
  - `forceClose()`: stop service → destroy overlay → `finishAffinity()` → `System.exit(0)`.
  - Service berhenti total, overlay hilang dari layar, proses aplikasi mati.
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
- **Background Ukuran & Offset**: Tambah kontrol ukuran (padding) dan posisi X/Y background.
  - `bgPadding` — slider ukuran background (0–80px), mengatur jarak tepi background ke teks.
  - `bgOffsetX` — slider offset horizontal background (0–60px).
  - `bgOffsetY` — slider offset vertikal background (0–60px).
  - Background digambar langsung di `onDraw()` via `ShadowTextView` dengan canvas translate.
- **Background Config**: Konfigurasi background overlay terpisah dari shadow.
  - Background bisa diaktifkan/dimatikan via switch sendiri.
  - Pilih warna background dengan color picker (ARGB, alpha slider tersedia).
  - Tidak lagi pakai hardcoded black semi-transparent (`0x88000000`).
  - Berlaku untuk Floating Text dan FPS Display.
- **Alpha Slider**: Tambah slider Alpha/Transparansi (0–255) di dialog color picker RGB.
  - Kontrol penuh ARGB (Alpha, Red, Green, Blue).
  - Tampilan HEX jadi 8 digit (#AARRGGBB).
  - Tampilan RGB jadi ARGB (Alpha, R, G, B).
- **Kill App Button**: Tambah menu "Tutup Aplikasi" di navigation drawer.
  - Panggil `finishAffinity()` untuk menutup semua aktivitas dan menghentikan proses aplikasi.
### 🚮 Fitur Dihapus
- **Hardcoded Background**: Hapus background hitam otomatis dari `OverlayShadow.apply()`.
  - Background shadow dulu otomatis `0x88000000` saat shadow diaktifkan.
  - Sekarang background dan shadow adalah fitur terpisah.
- **Shadow Opacity**: Hapus konfigurasi opacity shadow terpisah.
  - Sekarang alpha langsung diatur via color picker warna shadow.
  - `ShadowConfig.opacity` dan `getColorWithOpacity()` dihapus.
  - `ShadowTextView` pakai `shadowConfig.color` langsung.
  - Seekbar opacity shadow dihapus dari UI (Text & FPS).
  - Pref `shadow_opacity` / `fps_shadow_opacity` tidak lagi dimuat.
### 🔢 Version
- versionCode: 56
- versionName: 2.3.1.25.0
---
## [2.3.1.20.0 – 2.3.1.16.0] - 2026-05-16–06-01
Menggabungkan 5 release.
### ✨ Fitur Baru
- **Shadow Config**: Konfigurasi shadow modular dengan Enable, Warna, Blur, X/Y Offset, Opacity.
  - `ShadowConfig.java` — class konfigurasi shadow reusable untuk semua modul.
  - `OverlayShadow.java` — apply background + elevation (text shadow via `ShadowTextView`).
  - `ShadowTextView.java` — custom TextView terapkan shadow di `onDraw()` setiap frame (reliable untuk overlay).
  - Shadow dapat dikonfigurasi terpisah per modul (Text & FPS).
  - Kontrol UI: Shadow switch → container config muncul (color button, opacity/blur/offset seekbars).
- Dialog Settings: tambah tombol −/+ untuk zoom ukuran teks (4–60sp), default 14sp.
- **Dynamic Title Bar**: Toolbar title berubah sesuai modul yang dipilih di drawer.
- **Modul Baru**: CPU Monitor, Temperature, dan Logo Display ditambahkan ke drawer (Coming Soon).
- **String Resource**: Semua nama modul dipindah ke strings.xml.
- **FPS Draggable**: FPS overlay bisa digeser (drag) dan dikunci posisinya.
- Limit SeekBar: min 5 → max 120 (sebelumnya min 10 → max 60).
### 🔧 Optimasi & Penyesuaian
- Floating Text size range diperluas: 1–150 sp (sebelumnya 5–120).
- FPS Display size range diperluas: 5–140 sp (sebelumnya 5–120).
- Padding dialog ditingkatkan 24px → 25dp (density-independent).
- Ukuran teks dialog ditingkatkan 12sp → 40sp.
### 🐞 Bug Fixes
- Tombol kembali (back arrow) di SettingsActivity tidak berfungsi — tambah `setNavigationOnClickListener`.
### 🔢 Version
- versionCode: 51
- versionName: 2.3.1.20.0
---
## [2.3.1.15.0 – 2.3.1.10.0] - 2026-05-16–06-01
Menggabungkan 5 release.
### ✨ Fitur Baru
- Toggle tema (gelap/terang) via ikon bulan di toolbar kanan.
- Tema tersimpan dan bertahan setelah app ditutup.
- Tambah menu pengaturan dengan opsi untuk melihat CHANGELOG dan README dalam dialog scrollable.
- SettingsActivity membaca langsung dari file assets (single source of truth).
- Hapus duplikasi file: CHANGELOG.md, README.md, dan AGENTS.md hanya ada di assets folder.
### 🔧 Optimasi & Penyesuaian
- Persiapan struktur modular untuk module overlay.
- HSVColorPickerView dipindah ke shared component.
- Update import package setelah refactor namespace.
### 🐞 Bug Fixes
- **FPS tidak tampil**: Service kini bisa start tanpa text overlay (FPS standalone).
- **TextConfig.size**: Fix posisi overlay tidak termuat dari SharedPreferences.
### 🔢 Version
- versionName: 2.3.1.15.0 (merged entry)
---
## [1.3.1.9.0 - 1.0.0.0.0] - Pra-2026
### 💡 Catatan
- Major 1 kebawah telah dipisahkan dari Project
