# FTxT - Floating Text

**FTxT** adalah aplikasi Android overlay yang memungkinkan Anda menampilkan teks floating di atas aplikasi lain. Dengan fitur customization lengkap, Anda dapat mengubah ukuran, warna, transparansi, dan kontrol sentuhan teks overlay.

## 📋 Daftar Isi

- [Fitur Utama](#fitur-utama)
- [Requirement](#requirement)
- [Instalasi & Build](#instalasi--build)
- [Cara Penggunaan](#cara-penggunaan)
- [Dokumentasi Fitur](#dokumentasi-fitur)
- [Struktur Project](#struktur-project)
- [Version History](#version-history)

---

## ✨ Fitur Utama

### 1. **Floating Text Overlay** 🎯
Tampilkan teks custom di layar utama yang overlay ke atas semua aplikasi lain.
- Dukungan untuk semua versi Android 8.0+ (API 26+)
- Positioning yang dapat dikustomisasi (X, Y coordinates)
- Persistent position (tersimpan otomatis)

### 2. **Customizable Text Size** 📏
Atur ukuran teks dengan slider yang mudah digunakan.
- Range: 10 - 60 sp
- Real-time preview
- Instant apply tanpa restart overlay

### 3. **Advanced HSV Color Picker** 🎨
Color picker profesional dengan kontrol penuh atas warna, brightness, dan transparansi.

**Fitur:**
- **Circular Color Wheel**: Pilih hue dengan memutar di sekeliling lingkaran
- **Saturation Picker**: Geser ke arah pusat untuk ubah kejenuhan warna
- **Brightness Slider**: Kontrol intensitas cahaya (0-100%)
- **Alpha/Transparency Slider**: Kontrol transparansi (0-255)
- **Live Color Preview**: Pratinjau warna secara real-time

**Cara Penggunaan:**
1. Buka aplikasi
2. Ketuk tombol "Pilih Warna"
3. Dialog color picker akan muncul dengan:
   - Roda warna circular di tengah
   - Slider brightness di bawah
   - Slider alpha/transparansi
   - Preview warna 60x60dp
4. Klik OK untuk apply atau Cancel untuk batal

### 4. **Touch Passthrough (Sentuhan Lewati)** ✋
Kontrol apakah sentuhan melewati teks overlay atau tertangkap oleh teks.

**Mode:**
- **Teks Bergerak (OFF)**: Teks dapat disentuh dan dipindahkan
- **Teks Terkunci (ON)**: Sentuhan melewati teks ke aplikasi di belakang

**Keuntungan:**
- Saat diaktifkan: Teks tidak menutupi sentuhan, Anda bisa gunakan app di belakang normally
- Saat dinonaktifkan: Teks dapat digerakkan ke posisi yang diinginkan

### 5. **Overlay Toggle** 🔘
Nyalakan/matikan overlay dengan satu switch.
- **Overlay ON**: Teks floating ditampilkan
- **Overlay OFF**: Teks tersembunyi (tapi tetap dalam memori)
- Perlu permission "Display over other apps" di Android 6.0+

---

## 💻 Requirement

- **Android Version**: 8.0+ (API Level 26+)
- **Target SDK**: 35
- **Compile SDK**: 35
- **Java Version**: 1.8+

### Required Permissions
```xml
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
<uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
```

> **Catatan**: `POST_NOTIFICATIONS` diminta saat runtime di Android 13+ (API 33+) saat overlay diaktifkan pertama kali.

---

## 🔧 Instalasi & Build

### Prerequisites
- Android Studio (versi terbaru)
- Gradle 8.13+
- JDK 1.8+

### Build Steps

1. **Clone atau buka project**
```bash
cd /path/to/FTxT
```

2. **Build Debug APK**
```bash
./gradlew clean assembleDebug
```

3. **Build Release APK**
```bash
./gradlew clean assembleRelease
```

4. **Install ke device**
```bash
./gradlew installDebug
```

5. **Run Gradle Wrapper Test** (compile only)
```bash
./gradlew :app:compileDebugJavaWithJavac
```

---

## 📖 Cara Penggunaan

### 1. Buka Aplikasi
- Ketuk icon FTxT di home screen
- Aplikasi main activity akan terbuka

### 2. Edit Teks
- Ketuk field "Masukkan teks overlay"
- Ketik teks yang ingin ditampilkan

### 3. Atur Ukuran Teks
- Gunakan **SeekBar "Ukuran Teks"** untuk mengubah ukuran
- Range 10-60 sp (smaller to larger)
- Perubahan langsung ditampilkan jika overlay sedang aktif

### 4. Pilih Warna
- Ketuk tombol **"Pilih Warna"**
- Dialog HSV Color Picker akan terbuka
- **Di roda warna**: Drag untuk pilih hue
- **Slider Brightness**: Atur terang-gelap
- **Slider Alpha**: Atur transparansi
- **Preview**: Lihat warna di box kecil (60x60dp)
- Klik **OK** untuk apply, **Cancel** untuk batal

### 5. Aktifkan Overlay
- Switch **"Overlay OFF"** untuk aktifkan
- Akan berubah menjadi **"Overlay ON"**
- Teks akan muncul di layar (jika permission sudah diberikan)
- Teks akan tersimpan di posisi terakhir digerakkan

### 6. Atur Mode Sentuhan
- Gunakan switch **"Teks Bergerak/Teks Terkunci"**
- **Teks Bergerak (OFF)**: Drag teks ke posisi baru
- **Teks Terkunci (ON)**: Sentuhan melewati ke app di belakang

### 7. Deaktifkan Overlay
- Switch **"Overlay ON"** kembali untuk nonaktifkan
- Teks akan hilang dari layar (tapi tetap tersimpan)

---

## 🖼️ Launcher Icon

FTxT menggunakan adaptive icon (Android 8.0+) dengan foreground custom PNG.

- **Foreground**: `app/src/main/res/drawable/ic_launcher_foreground.png`
- **Background**: `app/src/main/res/drawable/ic_launcher_background.xml` (solid `@color/colorPrimary`)
- **Adaptive Icon XML**: `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`

Untuk mengganti icon, cukup replace file `ic_launcher_foreground.png` di `app/src/main/res/drawable/`.

Untuk hasil terbaik pada semua ukuran layar, gunakan Android Studio: `File > New > Image Asset` dan pilih gambar sebagai foreground.


---

## 🎯 Dokumentasi Fitur

### HSV Color Model

FTxT menggunakan HSV (Hue, Saturation, Value) color model untuk flexibility maksimal:

```
Hue (0-360°)       = Jenis warna (merah, hijau, biru, dll)
Saturation (0-100) = Kejenuhan warna (putih ← → pure color)
Value (0-100)      = Brightness (hitam ← → terang)
Alpha (0-255)      = Transparansi (transparent ← → opaque)
```

### Real-Time Updates

Semua perubahan (warna, ukuran, teks, touch mode) langsung diterapkan ke floating text tanpa perlu restart overlay:

```
Color Change    → updateTextColorStatic()
Text Change     → updateTextStatic()
Size Change     → updateTextSizeStatic()
Touch Mode      → updateTouchFlagsStatic()
```

### Position Persistence

Posisi teks otomatis tersimpan ke SharedPreferences:
- **Saat drag selesai** (ACTION_UP): Position disimpan
- **Saat service onDestroy** (overlay matikan): Position final disimpan
- **Saat service onCreate** (overlay nyalakan): Position di-restore

Data tersimpan di:
```
SharedPreferences key: "text_x" (X coordinate)
SharedPreferences key: "text_y" (Y coordinate)
```

---

## 📁 Struktur Project

```
FTxT/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/exp/ftxt/
│   │       │   ├── MainActivity.java          (Main Activity & UI Control)
│   │       │   ├── FloatingService.java       (Overlay Service & Logic)
│   │       │   └── HSVColorPickerView.java    (Custom Color Picker View)
│   │       ├── res/
│   │       │   ├── drawable/
│   │       │   │   ├── ic_launcher_background.xml
│   │       │   │   └── ic_launcher_foreground.png
│   │       │   ├── layout/
│   │       │   │   ├── activity_main.xml      (Main Activity Layout)
│   │       │   │   └── dialog_hsv_color_picker.xml  (Color Picker Dialog)
│   │       │   ├── mipmap-anydpi-v26/
│   │       │   │   └── ic_launcher.xml
│   │       │   └── values/
│   │       └── AndroidManifest.xml
│   └── build.gradle                          (App Build Config)
├── CHANGELOG.md                              (Version History)
├── README.md                                 (This File)
└── build.gradle                              (Root Build Config)
```

### File Descriptions

| File | Deskripsi |
|------|-----------|
| `MainActivity.java` | Activity utama, UI controls (seekbar, switch, buttons), color picker dialog management |
| `FloatingService.java` | Service untuk overlay window, touch handling, position saving/loading, real-time updates |
| `HSVColorPickerView.java` | Custom View untuk circular HSV color picker wheel dengan gradient & touch support |
| `activity_main.xml` | Layout untuk activity: text input, size seekbar, color button, toggles |
| `dialog_hsv_color_picker.xml` | Layout untuk color picker dialog: wheel, sliders, preview, buttons |

---

## 🔄 Version History

Lihat file [CHANGELOG.md](./CHANGELOG.md) untuk riwayat lengkap perubahan.

### Current Version: 8.2

**Latest Changes (v8.2)**
- ✨ Fitur baru: Adaptive Launcher Icon, Real-Time Text Update, Foreground Service
- 🔧 Perbaikan: Launcher icon path, dependencies update, deprecated API cleanup
- 🐛 Bug fix: Service crash di API 26+, foreground service type compatibility

**Previous Versions**
- v7.4: Position persistence & touch responsiveness fixes
- v7.3: Touch passthrough implementation fixes
- v7.2: Code optimization (remove redundant lock position feature)
- v7.1: Fitur touch passthrough & position lock (awal)

---

## 🚀 Development & Contribution

### Code Style
- Java 1.8 compatible
- XML resource naming: snake_case (activity_main.xml)
- Java naming: CamelCase (MainActivity.java)
- Method naming: camelCase (updateTextColorStatic)

### Building Custom Views
Jika ingin extend color picker atau membuat custom overlay components:

1. Extend `View` class
2. Override `onDraw(Canvas canvas)` untuk rendering
3. Override `onTouchEvent(MotionEvent event)` untuk touch handling
4. Implement custom listeners untuk callbacks

Contoh:
```java
public class HSVColorPickerView extends View {
    // Implement rendering & touch logic
    @Override
    protected void onDraw(Canvas canvas) { ... }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) { ... }
}
```

### Static Helper Methods
Untuk real-time updates dari Activity ke running Service, gunakan static wrapper methods:

```java
// Di FloatingService.java
public static void updateTextColorStatic() {
    if(instance != null) {
        instance.floatingView.setTextColor(MainActivity.currentColor);
    }
}

// Di MainActivity.java
FloatingService.updateTextColorStatic();  // Instant update
```

---

## 📝 License

TBD (Tentukan lisensi sesuai kebutuhan)

---

## 👨‍💻 Author

Dikembangkan sebagai aplikasi floating text overlay Android dengan fitur customization canggih.

## 📧 Support

Untuk bug reports atau feature requests, silakan buat issue atau hubungi developer.

---

**Last Updated**: May 17, 2026 (v8.2)