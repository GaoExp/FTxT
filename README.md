# FTxT — Floating Text Overlay                                                                                              
                                                                                              
**Current Release:** `2.3.1.9.5`        
**Last Updated:** `2026-05-23`                                                                                       
                                                                                              
FTxT adalah aplikasi Android overlay yang memungkinkan Anda menampilkan teks floating di atas aplikasi lain dengan fitur kustomisasi lengkap untuk ukuran, warna, transparansi, posisi, dan kontrol sentuhan.                                                                                              
                                                                                              
---                                                                                              
                                                                                              
## 📋 Daftar Isi                                                                                              
- [Fitur Utama](#-fitur-utama)                                                                                              
- [Environments](#-environments)                                                                                              
- [Cara Penggunaan](#-cara-penggunaan)                                                                                              
- [Launcher Icon](#-launcher-icon)                                                                                              
- [Dokumentasi Teknis](#-dokumentasi-teknis)                                                                                              
- [Struktur Project](#-struktur-project)                                                                                              
- [Versioning](#-versioning)                                                                  
- [Development Notes](#-development-notes)                           
- [Lisensi & Klarifikasi](#-lisensi-klarifikasi)                         
                                                                                              
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
```xml                                                                                              
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>                                                                                              
<uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>                                                                                              
<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>                                                                                              
<uses-permission android:name="android.permission.WAKE_LOCK"/>                                                                                              
<uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS"/>                                                                                              
```                                                                                              
                                                                                              
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
                                                                                              
---                                                                                              
                                                                                              
## 🖼️ Launcher Icon                                                                                              
                                                                                              
FTxT menggunakan **Adaptive Icon** untuk Android 8.0+.                                                                                              
                                                                                              
File terkait:                                                                                              
```txt                                                                                              
Foreground:                                                                                              
app/src/main/res/drawable/ic_launcher_foreground.png                                                                                              
                                                                                              
Background:                                                                                              
app/src/main/res/drawable/ic_launcher_background.xml                                                                                              
                                                                                              
Adaptive XML:                                                                                              
app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml                                                                                              
```                                                                                              
                                                                                              
Untuk mengganti icon:                                                                                              
Cukup replace:                                                                                              
```txt                                                                                              
ic_launcher_foreground.png                                                                                              
```                                                                                              
                                                                                              
Rekomendasi:                                                                                              
Android Studio →                                                                                                
`File > New > Image Asset`                                                                                              
                                                                                              
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
│   │   │   └── MainActivity.java
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
|------|------------|
| MainActivity.java | Activity utama & kontrol UI |
| core/FloatingService.java | Overlay service, touch handling, persistence |
| shared/color/HSVColorPickerView.java | Custom HSV circular picker |
| modules/ | Struktur awal module overlay |
| activity_main.xml | Layout utama |
| dialog_hsv_color_picker.xml | Layout dialog picker |          
                                                                                    
---                                                                                              
                                                                                              
## 🔢 Versioning                                                                                                                             
                                                                                              
Format:  
  
```txt  
major.removed.restored.minor.patch  
```  
  
Contoh:  
  
```txt  
2.3.1.9.4  
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
```                                                                                              
                                                                                              
---                                                                                              
                                                                                              
## 📝 Lisensi & Klarifikasi                                                                                          
                                                                                              
Belum ada lisensi resmi yang ditetapkan untuk project ini.                                              
Sebagian besar pengembangan dibantu AI, sementara pengembang menangani pengujian, penyesuaian implementasi, revisi, dan debugging sambil ngopi.                                                                                      
                                                                                      
Silakan gunakan, modifikasi, fork,                                                                                      
atau kustomisasi sesuai kebutuhan.                                                                                      
                                                                                              
---                                                                                              
                                                                                              
## 👨‍💻 Author                                                                                              
                                                                                              
Developed by **GaoZhan**.                                        
                                                                      
Android floating text overlay dengan fokus pada customization, real-time updates, dan lightweight overlay behavior.                                                                                              
                                                                                              
---                                                                                              
                                                                                              
## 📧 Support                                                                                              
                                                                                              
Laporan bug, issue, atau permintaan fitur:                                                                                              
Silakan buat issue atau hubungi pengembang.                                                           
                                                                                   
Respons tidak dijamin cepat, karena project ini berkembang mengikuti eksperimen, suasana hati, waktu luang, dan secangkir kopi.