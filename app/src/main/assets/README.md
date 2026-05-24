# FTxT — Floating Text Overlay

**Current Release:** `2.3.1.11.0`        
**Last Updated:** `2026-05-24`

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

- Range ukuran: **10–60 sp**
- Real-time preview
- Instant apply


### Advanced HSV Color Picker 🎨
Color picker berbasis HSV dengan kontrol warna penuh.

Fitur:
- Circular color wheel
- Saturation adjustment
- Brightness slider
- Alpha / transparency slider
- Live preview
- Real-time apply


### Touch Passthrough ✋
Kontrol perilaku sentuhan overlay.

**OFF**
- Overlay menerima sentuhan
- Overlay dapat dipindahkan

**ON**
- Sentuhan diteruskan ke aplikasi belakang
- Overlay tidak mengganggu interaksi aplikasi lain


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
`10–60 sp`
Perubahan langsung diterapkan jika overlay aktif.

### 4. Pilih Warna
Tekan tombol:
`Pilih Warna`
Dialog HSV akan menampilkan:
- Color wheel
- Brightness slider
- Alpha slider
- Live preview

Klik:
- **OK** → Terapkan
- **Cancel** → Batal

### 5. Aktifkan Overlay
Nyalakan switch:
`Overlay ON`
Jika permission sudah diberikan, teks akan langsung muncul.

### 6. Nonaktifkan Overlay
Matikan switch:
`Overlay OFF`
Overlay hilang, konfigurasi tetap tersimpan.

### 7. Akses Settings Menu
Tap ikon hamburger (☰) di toolbar, lalu pilih "Pengaturan" untuk membuka dokumentasi in-app.

---

## 🎯 Dokumentasi Teknis

### HSV Color Model
FTxT menggunakan model warna HSV.

```txt
Hue (0–360°)       = Jenis warna
Saturation (0–100) = Intensitas warna
Value (0–100)      = Brightness
Alpha (0–255)      = Transparansi
```

---

### Real-Time Updates
Semua perubahan diterapkan tanpa restart overlay.

```txt
Color Change → updateTextColorStatic()
Text Change  → updateTextStatic()
Size Change  → updateTextSizeStatic()
Touch Mode   → updateTouchFlagsStatic()
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
├── app/
│   ├── src/main/
│   │   ├── java/exp/ftxt/
│   │   │   ├── core/
│   │   │   │   └── FloatingService.java
│   │   │   │
│   │   │   ├── modules/
│   │   │   │   ├── text/
│   │   │   │   ├── fps/
│   │   │   │   ├── cpu/
│   │   │   │   ├── clock/
│   │   │   │   ├── temp/
│   │   │   │   └── logo/
│   │   │   │
│   │   │   ├── shared/
│   │   │   │   └── color/
│   │   │   │       └── HSVColorPickerView.java
│   │   │   │
│   │   │   ├── MainActivity.java
│   │   │   └── SettingsActivity.java
│   │   │
│   │   ├── assets/
│   │   │   ├── CHANGELOG.md
│   │   │   └── README.md
│   │   │
│   │   ├── res/
│   │   └── AndroidManifest.xml
│   │
│   └── build.gradle
│
├── CHANGELOG.md
├── README.md
└── build.gradle
```

### Deskripsi File

| File | Deskripsi |
|------|-----------|
| MainActivity.java | Activity utama & kontrol UI |
| SettingsActivity.java | Settings dengan dokumentasi in-app |
| core/FloatingService.java | Overlay service, touch handling, persistence |
| shared/color/HSVColorPickerView.java | Custom HSV circular picker |
| modules/ | Struktur awal module overlay |
| activity_main.xml | Layout utama |
| dialog_hsv_color_picker.xml | Layout dialog picker |
| activity_settings.xml | Layout settings menu |

---

## 🔢 Versioning

Format:

```txt
major.removed.restored.minor.patch
```

Contoh:

```txt
2.3.1.10.0
```

- major = milestone besar / generasi project
- removed = histori fitur dihapus
- restored = histori fitur dipulihkan
- minor = feature release counter
- patch = maintenance / bugfix / optimasi

Lihat `CHANGELOG.md` untuk riwayat perubahan lengkap.

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
- assets/CHANGELOG.md
- assets/README.md

Tidak perlu duplikasi file, update sekali dan semua reflect otomatis di app.

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
