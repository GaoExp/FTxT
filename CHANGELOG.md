# Changelog - FTxT (Floating Text)

## [9.4] - 2026-05-18

### ✨ Fitur Baru
- **Text Shadow (Bayangan Teks)**: Tambahkan bayangan pada teks overlay untuk meningkatkan readability
  - Toggle switch untuk enable/disable shadow
  - Shadow layer: 5px blur, offset 2x2, warna hitam
  - Real-time apply tanpa restart overlay

### 📝 File yang Diubah

#### ✅ MainActivity.java (UPDATED)
- Tambah variabel static `isShadowEnabled = true`
- Tambah widget `shadowSwitch` di findViewById
- Tambah listener untuk shadow switch
- Tambah call `applySwitchTint(shadowSwitch, ...)` untuk styling

#### ✅ activity_main.xml (UPDATED)
- Tambah LinearLayout dengan shadow switch
- Switch `shadowSwitch` dengan status `android:checked="true"`
- Label "Bayangan"

#### ✅ FloatingService.java (UPDATED)
- Tambah method `updateShadowStatic()` - wrapper untuk static access
- Tambah method `applyShadow()` - apply shadow layer ke textview
  - Saat enable: `setShadowLayer(5f, 2f, 2f, BLACK)`
  - Saat disable: `setShadowLayer(0f, 0f, 0f, TRANSPARENT)`
- Call `applyShadow()` di `onCreate()` setelah setup textview

#### ✅ app/build.gradle (UPDATED)
- versionCode: 32 → 33
- versionName: 9.3 → 9.4

### 📊 Version Numbering
- **9** = Fitur utama (sejak 9.1)
- **4** = Perbaikan & fitur minor ke-4

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

#### ✅ app/build.gradle (UPDATED)
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

### 🔔 Launcher Icon (implementation detail)
- The Gradle task now copies `ic_launcher_foreground.png` into `build/generated/res/launcher/<variant>/` instead of directly into `app/src/main/res/drawable`.
- The generated folder is registered with the Android Gradle plugin using `variant.registerGeneratedResFolders(...)` and the copy task is set as the builder (`builtBy`). This avoids implicit dependency errors reported by Gradle and guarantees the correct task ordering.

### 📊 Version Numbering
- **8** = Fitur baru (HSV Color Picker)
- **1** = Versi pertama dari fitur ini

---

## [7.4] - 2026-05-17

### 🐛 Bug Fixes
- **Touch Passthrough Real-Time**: Penguncian sentuhan sekarang langsung berfungsi tanpa restart overlay
  - Tambah method static wrapper `updateTouchFlagsStatic()`
  - Instant apply FLAG_NOT_TOUCHABLE saat switch diubah

- **Position Persistence**: Posisi teks tidak lagi ter-reset saat on/off overlay
  - Integrasikan SharedPreferences untuk save/load position
  - Auto-save saat user selesai drag (ACTION_UP)
  - Auto-load saat service onCreate()

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

- **Position Lock**: Fitur untuk mengunci posisi teks (kemudian di-deprecate di v7.2)
  - ON: Teks tidak dapat dipindahkan
  - OFF: Teks dapat dipindahkan

### 📝 File yang Dibuat & Diubah

#### ✅ MainActivity.java (UPDATED)
- Tambah variabel static `isPositionLocked` & `isTouchPassthrough`
- Tambah widgets: `lockPositionSwitch`, `touchPassthroughSwitch`
- Tambah event listeners untuk kedua switch

#### ✅ FloatingService.java (UPDATED)
- Tambah method `updateTouchFlags()` untuk manage FLAG_NOT_TOUCHABLE
- Modifikasi touch listener untuk check `isPositionLocked` & `isTouchPassthrough`
- Panggil `updateTouchFlags()` saat service onCreate()

#### ✅ activity_main.xml (UPDATED)
- Tambah 2 Switch elements untuk kontrol fitur baru

#### ✅ app/build.gradle (UPDATED)
- versionCode: 22 → 23
- versionName: 7.0 → 7.1

---

## [7.0] - Previous Release

### Features
- Overlay floating text display
- Customizable text size with SeekBar
- Color selection for text
- Text edit capability
- Overlay enable/disable toggle