# Changelog - FTxT (Floating Text)

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

