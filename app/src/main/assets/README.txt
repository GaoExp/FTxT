FTxT (FunText) - Floating Text Overlay

Current Release: 3.9.3.69.2 Beta
Last Updated: 2026-06-11

FTxT (FunText) adalah aplikasi Android overlay yang memungkinkan Anda menampilkan teks floating di atas aplikasi lain dengan fitur kustomisasi lengkap untuk ukuran, warna, transparansi, posisi, dan kontrol sentuhan.

Fitur Utama

- Floating Text Overlay - Teks custom di atas semua aplikasi, real-time update
- FPS Display Overlay - FPS counter draggable dengan opsi hanya angka
- Jam Digital Overlay - Waktu real-time 24 jam HH:mm:ss, update tiap 1 detik
- Suhu Baterai Overlay - Suhu baterai dalam C, update tiap 5 detik
- Battery Percentage Overlay - Persentase baterai dalam %, update tiap 5 detik
- Battery Current Overlay - Tegangan (mV), arus (mA), dan daya (W) baterai, update tiap 5 detik
- Watermark Overlay - Teks watermark kustom dengan opacity default semi-transparan, ukuran 5-200sp, warna, shadow, background, dan kontrol posisi lengkap
- Watermark Seal Pattern - Mode segel: teks diulang diagonal melayar penuh dengan kontrol spasi horizontal, spasi vertikal, dan sudut (angle)
- Network Speed Meter Overlay - Kecepatan internet real-time (↓ ↑) KB/s MB/s, polling tiap 1 detik
- Classic Color Wheel + ARGB Sliders - Full disk color wheel dengan crosshair, ARGB slider, two-way sync, color name auto-detection, HEX edit manual
- Safe Area - Batasi posisi overlay agar tidak masuk area notch/cutout
- Touch Passthrough - Kunci posisi agar sentuhan tembus ke aplikasi belakang (default ON)
- Position Control Lengkap - Slider X/Y, D-Pad, preset posisi (hingga 50 slot), orientasi otomatis per mode layar
- Preset Full-Konfigurasi (v2) - Simpan/muat seluruh config overlay (posisi, ukuran, warna, shadow, background, touchPassthrough, safeArea, toggle display spesifik) dengan metadata (tags, favorite, thumbnail, version history). UUID-based storage, backward compatible.
- Selective Preset Apply - Opsi apply preset: posisi saja, warna saja, background saja, dll. Merge partial config tanpa timpa pengaturan lain.
- Preset Search and Tagging - Cari preset berdasarkan nama atau tag; favorite flag untuk quick access.
- Preset Share via Intent - Bagikan preset via native Android share intent (file-based, tidak clipboard).
- Configurable Background - Warna, ukuran, offset, margin, radius (independen dari shadow)
- Configurable Shadow - Warna, blur, offset X/Y per modul
- Slider Label Edit - Klik label slider untuk edit nilai via dialog
- Screen Orientation Toggle - Ikon orientasi layar di toolbar, toggle Potret/Lanskap sekali ketuk
- Dark/Light Theme - Toggle tema (default malam), tersimpan otomatis
- Overlay Toggle - Auto-start, permission handling, WakeLock, foreground service
- Android SplashScreen - SplashScreen API resmi tanpa fake loading

Dokumentasi Terkait

- PANDUAN.txt - Panduan penggunaan lengkap
- CHANGELOG.txt - Riwayat perubahan lengkap

Dokumentasi juga tersedia di dalam aplikasi melalui Pengaturan > tombol dokumentasi.

Lisensi dan Klarifikasi

Belum ada lisensi resmi yang ditetapkan untuk project ini.

Sebagian besar pengembangan dibantu AI, sementara pengembang menangani pengujian, penyesuaian implementasi, revisi, dan debugging sambil ngopi.

Silakan gunakan, modifikasi, fork, atau kustomisasi sesuai kebutuhan.

Author

Developed by GaoZhan.

Aplikasi overlay teks Android FTxT (FunText) dengan fokus pada customization, real-time updates, dan lightweight overlay behavior.

Support

Laporan bug, issue, atau permintaan fitur: Silakan buat issue atau hubungi pengembang.

Respons tidak dijamin cepat, karena project ini berkembang mengikuti eksperimen, suasana hati, waktu luang, dan secangkir kopi.

Development

Environment:
- Build System: Gradle + AGP
- Java: Java 17 (source/target)
- Min SDK: 26
- Target SDK: 35
- Compile SDK: 35
- Namespace: exp.ftxt
- Application ID: exp.ftxt

Versioning:

Project ini TIDAK menggunakan Semantic Versioning standar. Format khusus: major.removed.restored.minor.patch

- major: Milestone besar / arsitektur
- removed: Counter fitur yang dihapus (tidak turun)
- restored: Counter fitur yang dipulihkan (tdk turun)
- minor: Feature release counter
- patch: Bugfix / optimization / maintenance

Aturan:
- patch reset ke 0 saat minor naik
- minor +1 saat major, removed, atau restored naik

Dependencies:
- AndroidX AppCompat 1.7.1 - UI compatibility
- Material Design 1.12.0 - Material 3 components
- ConstraintLayout 2.2.1 - Layout
- Core SplashScreen 1.0.1 - SplashScreen API
- GSON 2.10.1 - JSON serialization
- JUnit 4.13.2 - Testing
- AndroidX Test JUnit 1.2.1 - Instrumented testing
- Espresso Core 3.6.1 - UI testing

Architecture:

MVC dengan service-based overlay:
- Model - Config classes (TextConfig, FpsConfig, dll), OverlayPreset, SharedPreferences
- View - Activity utama + panel controllers + overlay modules (ShadowTextView)
- Service - FloatingService (foreground service + WindowManager)

Build:

# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

Permission:

Dideklarasikan di AndroidManifest.xml:
- SYSTEM_ALERT_WINDOW - Izin overlay aplikasi lain
- FOREGROUND_SERVICE - Layanan latar depan
- FOREGROUND_SERVICE_SPECIAL_USE - Layanan overlay
- POST_NOTIFICATIONS - Notifikasi kontrol (API 33+)
- WAKE_LOCK - Jaga CPU tetap aktif
- REQUEST_IGNORE_BATTERY_OPTIMIZATIONS - Nonaktifkan optimasi baterai

Izin diminta otomatis saat pertama aplikasi dibuka. Pengguna juga bisa mengelola izin melalui menu Pengaturan.
