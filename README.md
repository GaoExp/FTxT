# FTxT — Floating Text Overlay

**Current Release:** `2.3.1.32.0`        
**Last Updated:** `2026-05-28`

FTxT adalah aplikasi Android overlay yang memungkinkan Anda menampilkan teks floating di atas aplikasi lain dengan fitur kustomisasi lengkap untuk ukuran, warna, transparansi, posisi, dan kontrol sentuhan.

---

## ✨ Fitur Utama

### Floating Text Overlay 🎯
Tampilkan teks custom di atas semua aplikasi lain.

Fitur:
- Dukungan Android 8.0+ (API 26+)
- Posisi bebas (X & Y)
- Posisi tersimpan otomatis
- Overlay toggle
- Update real-time


### Customizable Text Size 📏
Atur ukuran teks secara langsung tanpa restart overlay.

- Range ukuran: **1–150 sp** (Floating Text) / **5–140 sp** (FPS Display)
- Real-time preview
- Instant apply


### RGB Color Picker 🎨
Color picker dengan kontrol ARGB slider dan info warna lengkap.

Fitur:
- Color name auto-detection
- Live preview
- HEX value display (8 digit #AARRGGBB), bisa diedit manual
- HSV value display, tekan lama untuk salin
- ARGB value display, tekan lama untuk salin
- R, G, B, A sliders (0–255), label bisa diklik untuk edit manual
- Real-time apply


### Touch Passthrough ✋
Kontrol perilaku sentuhan overlay. **Default: ON** (Kunci Posisi aktif).

**OFF**
- Overlay menerima sentuhan
- Overlay dapat dipindahkan

**ON**
- Sentuhan diteruskan ke aplikasi belakang
- Overlay tidak mengganggu interaksi aplikasi lain


### Dark/Light Theme Toggle 🌙
Toggle tema gelap atau terang dari toolbar.
- Tema tersimpan otomatis
- Berlaku untuk seluruh app
- NavigationView ikut menyesuaikan tema

### Configurable Background 🎨
Konfigurasi latar belakang overlay dengan warna kustom (termasuk alpha/transparansi).

Fitur:
- Enable/disable background
- Pilih warna background via color picker (ARGB dengan alpha slider)
- Ukuran background (padding dari teks, 0–80px)
- Offset X/Y background (0–60px)
- Berlaku untuk Floating Text dan FPS Display
- Real-time update

### Configurable Shadow 🌑

Konfigurasi shadow teks untuk setiap modul overlay.

Fitur:
- Enable/disable shadow
- Warna shadow (color picker with alpha)
- Blur radius (0–50)
- Offset X & Y (0–30)
- Diterapkan real-time via `setShadowLayer()`

### FPS Display Overlay 📊
Tampilkan FPS counter sebagai overlay yang bisa digeser.

- FPS overlay draggable (bebas dipindah)
- Posisi bisa dikunci
- Range FPS limit: 5–140 FPS
- Opsi hanya tampilkan angka (sembunyikan teks "FPS")

### Slider Labels with Edit 🏷️
Semua slider menampilkan nilai numerik terkini pada labelnya dan bisa diklik untuk mengedit nilai secara langsung via dialog input—slider menyesuaikan otomatis.

### Overlay Toggle 🔘
Aktifkan atau nonaktifkan overlay dengan satu switch.

Termasuk:
- Permission handling
- Request notifikasi Android 13+
- Battery optimization request
- WakeLock selama overlay berjalan

---

## 💻 Environments

### System Requirements
- Android 8.0+ (API 26+)
- Target SDK 35
- Compile SDK 35
- Java 1.8+

### Required Permissions
- android.permission.SYSTEM_ALERT_WINDOW
- android.permission.FOREGROUND_SERVICE
- android.permission.POST_NOTIFICATIONS
- android.permission.WAKE_LOCK
- android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS

Catatan:
- `POST_NOTIFICATIONS` diminta saat runtime pada Android 13+.
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` membuka pengaturan baterai jika diperlukan.

---

## 📖 Cara Penggunaan

### 1. Buka Aplikasi
Jalankan aplikasi **FTxT**.

### 2. Edit Teks
Isi field:
`Masukkan teks overlay`

### 3. Atur Ukuran Teks
Gunakan **SeekBar Ukuran Teks**.
Range:
`1–150 sp` (Teks) / `5–140 sp` (FPS)
Perubahan langsung diterapkan jika overlay aktif.

### 4. Pilih Warna
Tekan tombol:
`Pilih Warna`
Dialog akan menampilkan:
- Nama warna otomatis
- HEX, HSV, ARGB
- R, G, B, A slider

Klik:
- **OK** → Terapkan
- **Cancel** → Batal

### 5. Aktifkan Overlay
Nyalakan switch:
`Overlay ON`
Jika permission sudah diberikan, teks akan langsung muncul.

**Catatan:** Aplikasi akan otomatis meminta izin dan menjalankan overlay saat dibuka jika sebelumnya sudah aktif.

### 6. Nonaktifkan Overlay
Matikan switch:
`Overlay OFF`
Overlay hilang, konfigurasi tetap tersimpan.

### 7. Toolbar Icons
Ikon di pojok kanan toolbar:
- Gear ⚙️ → Buka Pengaturan (dokumentasi in-app + toggle izin aplikasi)
- Bulan 🌙 → Toggle tema gelap/terang
- **Tutup Aplikasi** → Keluar dari aplikasi sepenuhnya.

### 8. Navigation Drawer
Tap ikon hamburger (☰) di kiri toolbar untuk membuka drawer navigasi.
- Pilih modul overlay yang diinginkan.

### 9. Background Config
Aktifkan switch Background untuk menampilkan latar belakang overlay:
- Pilih warna background (dengan alpha/transparansi via color picker)
- Atur ukuran background (padding dari teks)
- Atur offset X/Y background
- Background dan Shadow adalah fitur terpisah

### 10. Shadow Config
Aktifkan switch Shadow untuk menampilkan konfigurasi bayangan teks:
- Pilih warna shadow (dengan alpha/transparansi via color picker)
- Atur blur, offset X, offset Y

---

## 🎯 Dokumentasi Teknis

### ARGB Color Model
FTxT menggunakan model warna ARGB dengan slider.

```txt
Alpha (0–255) = Transparansi
Red (0–255)   = Komponen merah
Green (0–255) = Komponen hijau
Blue (0–255)  = Komponen biru
```

Informasi HSV juga ditampilkan sebagai referensi tambahan.

---

### Real-Time Updates
Semua perubahan diterapkan tanpa restart overlay.

```txt
Color Change → updateTextColorStatic()
Text Change  → updateTextStatic()
Size Change  → updateTextSizeStatic()
Touch Mode   → updateTouchFlagsStatic()
Background   → updateTextBackgroundStatic() / updateFpsBackgroundStatic()
Shadow       → updateShadowStatic()
```

---

### Position Persistence
Posisi overlay disimpan menggunakan SharedPreferences.

Behaviour:
```txt
ACTION_UP     → Simpan posisi drag
onDestroy()   → Simpan posisi akhir
onCreate()    → Restore posisi terakhir
```

SharedPreferences key:
```txt
text_x
text_y
shadow_enabled
```

---

## 📁 Struktur Project

```txt
FTxT/
├── CHANGELOG.md
├── README.md
├── app/
│   ├── src/main/
│   │   ├── java/exp/ftxt/
│   │   │   ├── core/
│   │   │   │   ├── FloatingService.java
│   │   │   │   ├── NotificationHelper.java
│   │   │   │   └── WakeLockManager.java
│   │   │   │
│   │   │   ├── modules/
│   │   │   │   ├── text/
│   │   │   │   │   ├── TextConfig.java
│   │   │   │   │   └── TextModule.java
│   │   │   │   ├── fps/
│   │   │   │   │   ├── FpsConfig.java
│   │   │   │   │   └── FpsModule.java
│   │   │   │   ├── cpu/         (empty)
│   │   │   │   ├── clock/       (empty)
│   │   │   │   ├── crosshair/   (empty)
│   │   │   │   ├── logo/        (empty)
│   │   │   │   └── watermark/   (empty)
│   │   │   │
│   │   │   ├── shared/
│   │   │   │   ├── color/
│   │   │   │   │   ├── ColorMath.java
│   │   │   │   │   ├── ColorNameResolver.java
│   │   │   │   │   └── HSVColorPickerView.java
│   │   │   │   └── ui/
│   │   │   │       ├── ColorPickerDialog.java
│   │   │   │       ├── OverlayDragHandler.java
│   │   │   │       ├── OverlayShadow.java
│   │   │   │       └── ShadowTextView.java
│   │   │   │
│   │   │   ├── ui/
│   │   │   │   ├── TextPanelController.java
│   │   │   │   └── FpsPanelController.java
│   │   │   │
│   │   │   ├── utils/
│   │   │   │   └── PermissionHelper.java
│   │   │   │
│   │   │   ├── MainActivity.java
│   │   │   └── SettingsActivity.java
│   │   │
│   │   ├── assets/
│   │   │   ├── AGENTS.txt 
│   │   │   ├── CHANGELOG.txt
│   │   │   └── README.txt
│   │   │
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   ├── activity_main.xml
│   │   │   │   ├── activity_settings.xml
│   │   │   │   ├── dialog_hsv_color_picker.xml
│   │   │   │   └── nav_header.xml
│   │   │   ├── drawable/
│   │   │   │   ├── ic_launcher_background.xml
│   │   │   │   ├── ic_launcher_foreground.png
│   │   │   │   ├── ic_settings.xml
│   │   │   │   └── ic_theme.xml
│   │   │   ├── menu/
│   │   │   │   ├── drawer_menu.xml
│   │   │   │   └── main_menu.xml
│   │   │   ├── mipmap-anydpi-v26/
│   │   │   │   └── ic_launcher.xml
│   │   │   └── values/
│   │   │       ├── colors.xml
│   │   │       ├── strings.xml
│   │   │       └── styles.xml
│   │   │
│   │   └── AndroidManifest.xml
│   │
│   └── build.gradle
│
└── build.gradle
```

### Deskripsi File

| File | Deskripsi |
|------|-----------|
| MainActivity.java | Activity utama & kontrol UI (delegasi ke panel controllers) |
| SettingsActivity.java | Settings dengan dokumentasi in-app |
| core/FloatingService.java | Overlay service, touch handling, persistence |
| core/NotificationHelper.java | Helper channel & notifikasi foreground |
| core/WakeLockManager.java | Manajemen partial wake lock |
| modules/text/TextConfig.java | Konfigurasi teks overlay |
| modules/text/TextModule.java | Module logic teks overlay |
| modules/fps/FpsConfig.java | Konfigurasi FPS overlay |
| modules/fps/FpsModule.java | Module logic FPS overlay |
| shared/color/HSVColorPickerView.java | Custom HSV circular picker |
| shared/color/ColorMath.java | Utilitas HSV color math |
| shared/color/ColorNameResolver.java | Konversi warna ke nama |
| shared/ui/ColorPickerDialog.java | Dialog wrapper HSV picker |
| shared/ui/OverlayDragHandler.java | Shared drag-to-move touch listener |
| shared/ui/OverlayShadow.java | Shared shadow bg + elevation |
| shared/ui/ShadowTextView.java | Custom TextView dengan text shadow di onDraw() |
| ui/TextPanelController.java | Controller panel Floating Text |
| ui/FpsPanelController.java | Controller panel FPS Display |
| utils/PermissionHelper.java | Helper permission overlay/notifikasi/baterai |
| activity_main.xml | Layout utama |
| activity_settings.xml | Layout settings menu |
| dialog_hsv_color_picker.xml | Layout dialog picker |
| nav_header.xml | Header navigation drawer |

---

## 🔢 Versioning

Format:

```version
major.removed.restored.minor.patch
```

Contoh:

```txt
1.0.0.12.2
```

- major = milestone besar / generasi project
- removed = histori fitur dihapus
- restored = histori fitur dipulihkan
- minor = feature release counter
- patch = maintenance / bugfix / optimasi

Lihat `CHANGELOG.txt` untuk riwayat perubahan lengkap.

---

## 🛠️ Development Notes

### Code Style
- Java 1.8 compatible
- XML → snake_case
- Class → CamelCase
- Method → camelCase

### Static Helper Methods
Untuk komunikasi real-time antara Activity dan Service.

```java
FloatingService.updateTextColorStatic();
FloatingService.updateTextStatic();
FloatingService.updateTextSizeStatic();
FloatingService.updateTouchFlagsStatic();
FloatingService.updateShadowStatic();
```

### Settings Documentation
File dokumentasi dibaca langsung dari assets folder pada saat runtime:
- assets/CHANGELOG.txt
- assets/README.txt

---

## 📝 Lisensi & Klarifikasi

Belum ada lisensi resmi yang ditetapkan untuk project ini.

Sebagian besar pengembangan dibantu AI, sementara pengembang menangani pengujian, penyesuaian implementasi, revisi, dan debugging sambil ngopi.

Silakan gunakan, modifikasi, fork, atau kustomisasi sesuai kebutuhan.

---

## 👨‍💻 Author

Developed by **GaoZhan**.

Android floating text overlay dengan fokus pada customization, real-time updates, dan lightweight overlay behavior.

---

## 📧 Support

Laporan bug, issue, atau permintaan fitur:
Silakan buat issue atau hubungi pengembang.

Respons tidak dijamin cepat, karena project ini berkembang mengikuti eksperimen, suasana hati, waktu luang, dan secangkir kopi.
