# FTxT (FunText) — Floating Text Overlay

**Current Release:** `3.9.3.69.6` **Beta**
**Last Updated:** `2026-06-11`

FTxT (FunText) adalah aplikasi Android overlay yang memungkinkan Anda menampilkan teks floating di atas aplikasi lain dengan fitur kustomisasi lengkap untuk ukuran, warna, transparansi, posisi, dan kontrol sentuhan.

---

## ✨ Fitur Utama

- **Floating Text Overlay** — Teks custom di atas semua aplikasi, real-time update
- **FPS Display Overlay** — FPS counter draggable dengan opsi hanya angka
- **Jam Digital Overlay** — Waktu real-time 24 jam `HH:mm:ss`, update tiap 1 detik
- **Suhu Baterai Overlay** — Suhu baterai dalam °C, update tiap 5 detik
- **Battery Percentage Overlay** — Persentase baterai dalam %, update tiap 5 detik
- **Battery Current Overlay** — Tegangan (mV), arus (mA), dan daya (W) baterai, update tiap 5 detik
- **Watermark Overlay** — Teks watermark kustom dengan opacity default semi-transparan, ukuran 5–200sp, warna, shadow, background, dan kontrol posisi lengkap
- **Watermark Seal Pattern** — Mode segel: teks diulang diagonal melayar penuh dengan kontrol spasi horizontal, spasi vertikal, dan sudut (angle)
- **Network Speed Meter Overlay** — Kecepatan internet real-time (↓ ↑) KB/s ↔ MB/s, polling tiap 1 detik
- **Classic Color Wheel + ARGB Sliders** — Full disk color wheel dengan crosshair, ARGB slider, two-way sync, color name auto-detection, HEX edit manual
- **Safe Area** — Batasi posisi overlay agar tidak masuk area notch/cutout
- **Touch Passthrough** — Kunci posisi agar sentuhan tembus ke aplikasi belakang (default ON)
- **Position Control Lengkap** — Slider X/Y, D-Pad, preset posisi (hingga 50 slot), orientasi otomatis per mode layar
- **Preset Full-Konfigurasi (v2)** — Simpan/muat seluruh config overlay (posisi, ukuran, warna, shadow, background, touchPassthrough, safeArea, toggle display spesifik) dengan metadata (tags, favorite, thumbnail, version history). UUID-based storage, backward compatible.
- **Selective Preset Apply** — Opsi apply preset: posisi saja, warna saja, background saja, dll. Merge partial config tanpa timpa pengaturan lain.
- **Preset Search & Tagging** — Cari preset berdasarkan nama atau tag; favorite flag untuk quick access.
- **Preset Share via Intent** — Bagikan preset via native Android share intent (file-based, tidak clipboard).
- **Configurable Background** — Warna, ukuran, offset, margin, radius (independen dari shadow)
- **Configurable Shadow** — Warna, blur, offset X/Y per modul
- **Slider Label Edit** — Klik label slider untuk edit nilai via dialog
- **Screen Orientation Toggle** — Ikon orientasi layar di toolbar, toggle Potret/Lanskap sekali ketuk
- **Dark/Light Theme** — Toggle tema (default malam), tersimpan otomatis
- **Collapsible Panel Sections** — Setiap panel overlay dikelompokkan dalam section collapsible: Tampilan, Posisi, Shadow, Background. Klik header ▾/▸ untuk toggle
- **Overlay Toggle** — Auto-start, permission handling, WakeLock, foreground service
- **Android SplashScreen** — SplashScreen API resmi tanpa fake loading

---

## 📚 Dokumentasi Terkait

| File | Isi |
|------|-----|
| [PANDUAN.md](PANDUAN.md) | Panduan penggunaan lengkap |
| [CHANGELOG.md](CHANGELOG.md) | Riwayat perubahan lengkap |

Dokumentasi juga tersedia di dalam aplikasi melalui **Pengaturan > tombol dokumentasi**.

---

## 📝 Lisensi & Klarifikasi

Belum ada lisensi resmi yang ditetapkan untuk project ini.

Sebagian besar pengembangan dibantu AI, sementara pengembang menangani pengujian, penyesuaian implementasi, revisi, dan debugging sambil ngopi.

Silakan gunakan, modifikasi, fork, atau kustomisasi sesuai kebutuhan.

---

## 👨‍💻 Author

Developed by **GaoZhan**.

Aplikasi overlay teks Android FTxT (FunText) dengan fokus pada customization, real-time updates, dan lightweight overlay behavior.

---

## 📧 Support

Laporan bug, issue, atau permintaan fitur:
Silakan buat issue atau hubungi pengembang.

Respons tidak dijamin cepat, karena project ini berkembang mengikuti eksperimen, suasana hati, waktu luang, dan secangkir kopi.

---

## 💻 Development

### Environment

| Item | Detail |
|------|--------|
| Build System | Gradle + AGP |
| Java | Java 17 (source/target) |
| Min SDK | 26 |
| Target SDK | 35 |
| Compile SDK | 35 |
| Namespace | exp.ftxt |
| Application ID | exp.ftxt |

### Versioning

Project ini **TIDAK** menggunakan Semantic Versioning standar. Format khusus: `major.removed.restored.minor.patch`

| Komponen | Arti |
|----------|------|
| major | Milestone besar / arsitektur |
| removed | Counter fitur yang dihapus (tidak turun) |
| restored | Counter fitur yang dipulihkan (tdk turun) |
| minor | Feature release counter |
| patch | Bugfix / optimization / maintenance |

Aturan:
- `patch` reset ke 0 saat `minor` naik
- `minor` +1 saat `major`, `removed`, atau `restored` naik

### Section Changelog

| Section | Deskripsi |
|---------|-----------|
| ✨ Fitur Baru | Fitur baru ditambahkan |
| 🚮 Fitur Dihapus | Fitur dihapus/dinonaktifkan |
| 📥 Fitur Dipulihkan | Fitur lama dikembalikan |
| ♻️ Perubahan Fitur | Perubahan fitur existing |
| 🔧 Optimasi & Penyesuaian | Optimasi, refactor, maintenance |
| 🐞 Bug Fixes | Perbaikan bug |
| 💡 Catatan | Informasi tambahan |
| 🗒️ File Added | File baru |
| ✏️ File Changed | File diubah |
| 🔥 File Removed | File dihapus |
| 🔢 Version | versionCode & versionName |

### Dependencies

| Library | Versi | Fungsi |
|---------|-------|--------|
| AndroidX AppCompat | 1.7.1 | UI compatibility |
| Material Design | 1.12.0 | Material 3 components |
| ConstraintLayout | 2.2.1 | Layout |
| Core SplashScreen | 1.0.1 | SplashScreen API |
| GSON | 2.10.1 | JSON serialization |
| JUnit | 4.13.2 | Testing |
| AndroidX Test JUnit | 1.2.1 | Instrumented testing |
| Espresso Core | 3.6.1 | UI testing |

### Architecture

MVC dengan service-based overlay:

- **Model** — Config classes (TextConfig, FpsConfig, dll), OverlayPreset, SharedPreferences
- **View** — Activity utama + panel controllers + overlay modules (ShadowTextView)
- **Service** — FloatingService (foreground service + WindowManager)

### Build

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease
```

### Permission

Dideklarasikan di AndroidManifest.xml:

- `SYSTEM_ALERT_WINDOW` — Izin overlay aplikasi lain
- `FOREGROUND_SERVICE` — Layanan latar depan
- `FOREGROUND_SERVICE_SPECIAL_USE` — Layanan overlay
- `POST_NOTIFICATIONS` — Notifikasi kontrol (API 33+)
- `WAKE_LOCK` — Jaga CPU tetap aktif
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` — Nonaktifkan optimasi baterai

Izin diminta otomatis saat pertama aplikasi dibuka. Pengguna juga bisa mengelola izin melalui menu **Pengaturan**.

---

## 📁 Struktur Project

```
FTxT/
├── .gitignore                    — Git ignore rules (build, gradle, local)
├── AGENTS.md                     — Pedoman AI agent
├── CHANGELOG.md                  — Riwayat perubahan per release
├── PANDUAN.md                    — Panduan penggunaan lengkap
├── README.md                     — Ringkasan project & fitur
├── build.gradle                  — Root Gradle (AGP 8.12.0)
├── gradle.properties             — Gradle: AndroidX, JVM args
├── gradlew / gradlew.bat         — Gradle wrapper scripts
├── settings.gradle               — Settings Gradle (include :app)
├── keystore.properties           — Konfigurasi signing release
├── local.properties              — Local SDK/NDK path
│
├── _karantina/                   — File deprecated/arsip
│   └── exp/ftxt/shared/ui/
│       ├── XyPadView.java        — XY Pad 2D drag (diganti Slider + D-Pad)
│       └── PositionPresetManager.java  — Preset posisi lama (diganti PresetManager GSON)
│
├── .github/workflows/
│   └── release.yml               — GitHub Actions CI: build APK & GitHub Release
│
├── gradle/wrapper/
│   └── gradle-wrapper.properties — Versi Gradle wrapper
│
├── key/
│   └── audiplay-release-key.jks  — Keystore signing release
│
└── app/
    ├── build.gradle              — Module: minSdk 26, compileSdk/targetSdk 35, Java 17
    ├── proguard-rules.pro        — Aturan ProGuard (custom rules)
    │
    ├── src/main/
    │   ├── AndroidManifest.xml   — Permission: overlay, notif, baterai, wake lock
    │   │
    │   ├── assets/
    │   │   ├── CHANGELOG.txt     — Riwayat perubahan (in-app display)
    │   │   ├── PANDUAN.txt       — Panduan penggunaan (in-app display)
    │   │   └── README.txt        — Ringkasan project (in-app display)
    │   │
    │   ├── java/exp/ftxt/
    │   │   ├── core/
    │   │   │   ├── FloatingService.java     — Foreground service: kelola semua overlay via WindowManager
    │   │   │   ├── NotificationHelper.java  — Notifikasi foreground service
    │   │   │   └── WakeLockManager.java     — Partial wake lock biar CPU tetap aktif
    │   │   │
    │   │   ├── features/
    │   │   │   ├── floating_text/
    │   │   │   │   ├── TextConfig.java      — Konfigurasi statis Text overlay
    │   │   │   │   └── TextModule.java      — Buat/hapus/update Text overlay window
    │   │   │   ├── fps_display/
    │   │   │   │   ├── FpsConfig.java       — Konfigurasi statis FPS overlay
    │   │   │   │   └── FpsModule.java       — FPS counter via Choreographer frame callback
    │   │   │   ├── clock_module/
    │   │   │   │   ├── ClockConfig.java     — Konfigurasi statis Clock overlay
    │   │   │   │   └── ClockModule.java     — Jam real-time HH:mm:ss, update tiap 1 detik
    │   │   │   ├── battery_temperature/
    │   │   │   │   ├── BatteryConfig.java   — Konfigurasi statis Battery overlay
    │   │   │   │   └── BatteryModule.java   — Suhu baterai °C + %, baca dari sticky broadcast
    │   │   │   ├── battery_percentage/
    │   │   │   │   ├── BatteryPercentageConfig.java  — Konfigurasi Battery % overlay
    │   │   │   │   └── BatteryPercentageModule.java  — Persentase baterai, update 5 detik
    │   │   │   ├── battery_current/
    │   │   │   │   ├── BatteryCurrentConfig.java     — Konfigurasi Battery Current overlay
    │   │   │   │   └── BatteryCurrentModule.java     — Tegangan/arus/daya dari 3 sumber (broadcast, API 28+, sysfs)
    │   │   │   ├── network_stats/
    │   │   │   │   ├── NetworkConfig.java  — Konfigurasi statis Network overlay
    │   │   │   │   └── NetworkModule.java  — Kecepatan internet ↓↑ via TrafficStats, 1 detik
    │   │   │   ├── watermark/
    │   │   │   │   ├── WatermarkConfig.java      — Konfigurasi Watermark + pattern mode
    │   │   │   │   └── WatermarkModule.java      — Watermark teks tunggal / pattern segel diagonal
    │   │   │   ├── crosshair/      (placeholder — coming soon)
    │   │   │   ├── cpu_monitor/    (placeholder — coming soon)
    │   │   │   └── logo_display/   (placeholder — coming soon)
    │   │   │
    │   │   ├── shared/
    │   │   │   ├── color/
    │   │   │   │   ├── ColorMath.java         — Operasi matematika HSV: gradient, angle, selector posisi
    │   │   │   │   ├── ColorNameResolver.java — Deteksi nama warna dari RGB
    │   │   │   │   └── HSVColorPickerView.java— Custom View: color wheel HSV + crosshair
    │   │   │   ├── preset/
    │   │   │   │   ├── OverlayPreset.java     — Model data preset dengan UUID, metadata, history
    │   │   │   │   ├── PresetManager.java     — CRUD preset: save/load/rename/reorder/search/export/import
    │   │   │   │   ├── PresetHandler.java     — Delegate pattern: save/load dialog infrastructure per modul
    │   │   │   │   ├── PresetBrowserDialog.java   — DialogFragment browser preset dengan search & filter
    │   │   │   │   └── PresetExampleActivity.java — Contoh integrasi sistem preset (referensi dev)
    │   │   │   └── ui/
    │   │   │       ├── ShadowConfig.java          — Model data shadow (enable, color, blur, offset)
    │   │   │       ├── ShadowTextView.java        — Custom TextView dengan shadow + background di onDraw()
    │   │   │       ├── OverlayDragHandler.java    — Touch listener untuk drag overlay
    │   │   │       ├── OverlayShadow.java         — Apply elevation-based shadow ke overlay
    │   │   │       ├── ColorPickerDialog.java     — Dialog color picker: wheel, sliders, HEX/ARGB
    │   │   │       ├── DpadController.java        — Kontrol D-Pad dengan repeat untuk fine position
    │   │   │       ├── SliderPositionController.java  — Slider X/Y posisi normalized 0-1000
    │   │   │       ├── SliderLabelEditor.java     — Dialog edit nilai numerik dari label slider
    │   │   │       ├── SectionHelper.java         — Utility collapsible section toggle ▸/▾
    │   │   │       └── PresetPreviewView.java     — Custom View preview posisi overlay di grid
    │   │   │
    │   │   ├── ui/
    │   │   │   ├── TextPanelController.java            — UI panel Text: input, size, color, shadow, bg, toggle
    │   │   │   ├── TextPositionController.java         — Kontrol posisi Text: D-Pad, slider, preset, koordinat
    │   │   │   ├── FpsPanelController.java             — UI panel FPS: switch, size, color, shadow, bg, value-only
    │   │   │   ├── FpsPositionController.java          — Kontrol posisi FPS dengan preset
    │   │   │   ├── ClockPanelController.java           — UI panel Clock: switch, size, color, shadow, bg
    │   │   │   ├── ClockPositionController.java        — Kontrol posisi Clock dengan preset
    │   │   │   ├── BatteryPanelController.java         — UI panel Battery: temp/pct toggle, size, color
    │   │   │   ├── BatteryPositionController.java      — Kontrol posisi Battery dengan preset
    │   │   │   ├── BatteryPercentagePanelController.java   — UI panel Battery %
    │   │   │   ├── BatteryPercentagePositionController.java— Kontrol posisi Battery %
    │   │   │   ├── BatteryCurrentPanelController.java      — UI panel Battery Current: volt, current, power toggle
    │   │   │   ├── BatteryCurrentPositionController.java   — Kontrol posisi Battery Current
    │   │   │   ├── NetworkPanelController.java             — UI panel Network: switch, size, color, shadow
    │   │   │   ├── NetworkPositionController.java          — Kontrol posisi Network
    │   │   │   ├── WatermarkPanelController.java           — UI panel Watermark: teks, pattern, spacing, angle
    │   │   │   └── WatermarkPositionController.java        — Kontrol posisi Watermark
    │   │   │
    │   │   ├── utils/
    │   │   │   └── PermissionHelper.java    — Helper izin: overlay, notifikasi, optimasi baterai
    │   │   │
    │   │   ├── MainActivity.java           — Activity utama: toolbar, nav drawer, panel system, theme toggle
    │   │   ├── SettingsActivity.java        — Manajemen izin: overlay, notifikasi, baterai
    │   │   └── DocumentationActivity.java   — Baca dokumentasi in-app dari assets (txt)
    │   │
    │   └── res/
    │       ├── anim/
    │       │   ├── settings_popup_enter.xml — Animasi masuk popup settings
    │       │   └── settings_popup_exit.xml  — Animasi keluar popup settings
    │       ├── drawable/
    │       │   ├── ic_dots_vertical.xml     — Ikon tiga titik vertikal (overflow menu)
    │       │   ├── ic_edit.xml              — Ikon pensil untuk edit HEX/nilai
    │       │   ├── ic_launcher_background.xml   — Background launcher adaptive icon
    │       │   ├── ic_launcher_foreground.png   — Foreground launcher adaptive icon
    │       │   ├── ic_screen_rotation.xml   — Ikon orientasi layar (toolbar)
    │       │   ├── ic_settings.xml          — Ikon gear untuk settings
    │       │   ├── ic_star_filled.xml       — Ikon bintang solid (favorit)
    │       │   ├── ic_star_outline.xml      — Ikon bintang outline (non-favorit)
    │       │   ├── ic_sun.xml               — Ikon matahari untuk tema terang
    │       │   ├── ic_theme.xml             — Ikon tema gelap/terang
    │       │   └── splash_screen.xml        — Splash screen drawable
    │       ├── layout/
    │       │   ├── activity_main.xml            — Layout utama dengan DrawerLayout + CoordinatorLayout
    │       │   ├── activity_settings.xml        — Layout halaman pengaturan izin
    │       │   ├── activity_documentation.xml   — Layout halaman dokumentasi in-app
    │       │   ├── dialog_hsv_color_picker.xml  — Dialog color picker dengan wheel & ARGB sliders
    │       │   ├── dialog_preset_browser.xml    — Dialog browser preset dengan search & list
    │       │   ├── drawer_content.xml           — Konten navigation drawer: scroll + item list
    │       │   ├── nav_header.xml               — Header navigation drawer: logo + versi
    │       │   ├── panel_text.xml               — Panel konfigurasi Floating Text
    │       │   ├── panel_fps.xml                — Panel konfigurasi FPS Display
    │       │   ├── panel_clock.xml              — Panel konfigurasi Jam Digital
    │       │   ├── panel_battery.xml            — Panel konfigurasi Suhu Baterai
    │       │   ├── panel_battery_percentage.xml — Panel konfigurasi Battery Percentage
    │       │   ├── panel_battery_current.xml    — Panel konfigurasi Battery Current
    │       │   ├── panel_network.xml            — Panel konfigurasi Network Speed
    │       │   ├── panel_watermark.xml          — Panel konfigurasi Watermark
    │       │   ├── panel_crosshair.xml          — Placeholder Crosshair (coming soon)
    │       │   ├── panel_logo.xml               — Placeholder Logo Display (coming soon)
    │       │   ├── preset_browser_item.xml      — Item layout untuk daftar preset
    │       │   └── preset_list_item.xml         — Item layout alternate untuk preset
    │       ├── menu/
    │       │   ├── drawer_menu.xml   — Menu navigation drawer: daftar panel
    │       │   └── main_menu.xml     — Menu toolbar: settings, theme, exit
    │       ├── mipmap-anydpi-v26/
    │       │   └── ic_launcher.xml   — Adaptive icon launcher
    │       ├── values/
    │       │   ├── colors.xml        — Warna: primary, accent, drawer, bg
    │       │   ├── strings.xml       — Semua string UI Bahasa Indonesia
    │       │   ├── styles.xml        — Style: AppTheme, popup animation, popup menu
    │       │   └── themes.xml        — Theme: SplashScreen
    │       ├── values-night/
    │       │   └── colors.xml        — Warna mode gelap: drawer bg, drawer header
    │       └── values-v31/
    │           └── themes.xml        — SplashScreen theme untuk API 31+
    │
    ├── src/test/java/exp/ftxt/
    │   └── ExampleUnitTest.java      — Contoh unit test (JVM)
    │
    └── src/androidTest/java/exp/ftxt/
        └── ExampleInstrumentedTest.java  — Contoh instrumented test (Android)
```

### Statistik Project

| Kategori | Jumlah |
|----------|-------:|
| Java source | 66 |
| Layout XML | 20 |
| Drawable XML | 11 |
| Values XML | 6 |
| Mipmap XML | 1 |
| Menu XML | 2 |
| Anim XML | 2 |
| XML lainnya (Manifest, backup) | 2 |
| Assets (txt) | 3 |
| Root dokumen | 5 |
| Root konfigurasi | 7 |
| Gradle & wrapper | 5 |
| CI/CD | 1 |
| **Total file** | **~131** |
| **Total direktori** | **~42** |
