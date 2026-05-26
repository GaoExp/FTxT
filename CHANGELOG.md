# Changelog - FTxT (Floating Text)
Dokumen ini mencatat riwayat perubahan project FTxT.

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