# FTxT (FunText) — Floating Text Overlay

>**Current Release:** `4.89.1` **Beta** 
**Last Updated:** `2026-09-06`

>>FTxT (FunText) adalah aplikasi Android overlay yang memungkinkan Anda menampilkan teks floating di atas aplikasi lain dengan fitur kustomisasi lengkap untuk ukuran, warna, transparansi, posisi, dan kontrol sentuhan.

---

## ✨ Fitur Utama

- **Floating Text Overlay** — Teks custom di atas semua aplikasi, real-time update, dengan Mode Segel (pola teks diulang diagonal, kontrol spasi H/V, sudut -180° hingga 180°)
- **FPS Display Overlay** — FPS counter draggable dengan warna nilai & label terpisah
- **Jam Digital Overlay** — Waktu real-time 24 jam `HH:mm:ss`, update tiap 1 detik
- **Battery Info Overlay** — Info baterai (suhu °C, persentase %, voltase V, arus mA, daya W) dengan warna nilai & label terpisah, urutan info via drag-and-drop (OrderZones), interval update bisa diatur
- **Battery Strip Overlay** — Bar baterai fleksibel di dalam panel Battery Info (tab Battery Strip): Mode Cepat (snap ke sisi atas/bawah/kiri/kanan) atau Mode Manual (panjang & posisi bebas), warna fill + strip kosong, skema warna level (Klasik 3-warna / Hue Gradien), animasi fade + wave (kedutan gelombang) saat low & shine saat charging (kecepatan, lebar band, intensitas bisa diatur)
- **Battery Monitor** — Pemantauan baterai perangkat di tab Monitor panel Battery Info: ring gauge level bergradien hue + grid metrik real-time, grafik riwayat Suhu/Daya/Tegangan/Arus/Persentase rentang 5 menit–24 jam dari database SQLite lokal, estimasi kapasitas & skor kesehatan ala AccuBattery; pencatatan otomatis full-aktif via foreground service ringan (nyala saat aplikasi dibuka & saat boot, sampling dinamis hemat baterai)
- **Network Speed Meter Overlay** — Kecepatan internet real-time (↓ ↑) dengan warna nilai & label terpisah
- **Memory Stats Overlay** — Monitoring memori real-time 14 nilai (Java Heap, Native Heap, Graphics, Total, Gagal, Execution Time, Free RAM, Total RAM, jumlah proses, dll) dengan urutan item via OrderZones, background monitor yang tetap berjalan meski service tidak aktif, export/copy snapshot
- **Crash Logger** — Saat force close, stack trace otomatis ditulis ke Documents/FTxT/Log_Crash (plus cadangan prefs) agar bug mudah dilaporkan tanpa logcat/adb
- **Log ANR** — Saat main thread macet ≥5 detik (ANR), trace seluruh thread otomatis ditulis ke Documents/FTxT/Log_ANR (plus cadangan prefs) agar peristiwa ANR mudah dilaporkan tanpa logcat/adb
- **Konfigurasi Panel Sidebar** — Toggle tampil/sembunyi panel Info Memori & Debugging dari Navigation Drawer via Pengaturan > Konfigurasi; switch Debugging terkunci password dengan tombol Unlock/Relock
- **Color Wheel & Hue Slider** — Dua mode color picker: Classic Color Wheel dengan crosshair + ARGB slider, atau Hue/Saturation/Brightness/Alpha slider. Two-way sync, color name auto-detection, HEX edit manual, saved colors
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
- **Ikon Notifikasi Dinamis** — Ikon notifikasi status bar bisa menampilkan suhu baterai, persen baterai, atau tanggal + nama hari (bisa 2 baris), yang di-generate sebagai Bitmap dan diperbarui otomatis hanya saat nilainya berubah; pilihan isi bisa diatur di Konfigurasi
- **Android SplashScreen** — SplashScreen API resmi tanpa fake loading

---

## 📚 Dokumentasi Terkait

| File | Isi |
|------|-----|
| [PANDUAN.md](PANDUAN.md) | Panduan penggunaan lengkap |
| [CHANGELOG.md](CHANGELOG.md) | Riwayat perubahan lengkap |
| [STRUKTUR.md](STRUKTUR.md) | Struktur project lengkap |

Dokumentasi juga tersedia di dalam aplikasi melalui **Pengaturan > tombol Dokumentasi**.

---

## 📝 Lisensi & Klarifikasi

Belum ada lisensi resmi yang ditetapkan untuk project ini.

>Sebagian besar pengembangan dibantu AI, sementara pengembang menangani pengujian, penyesuaian implementasi, revisi, dan debugging sambil ngopi.

>>Silakan gunakan, modifikasi, fork, atau kustomisasi sesuai kebutuhan.

---

## 👨‍💻 Author

<mark> Developed by <u>***GaoZhan.***</u> </mark>

Aplikasi overlay teks Android FTxT (FunText) dengan fokus pada customization, real-time updates, dan lightweight overlay behavior.

---

## 📧 Support

Laporan bug, issue, atau permintaan fitur:
Silakan buat issue atau hubungi pengembang.

>Respons tidak dijamin cepat, karena project ini berkembang mengikuti eksperimen, suasana hati, waktu luang, dan secangkir kopi.

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

### 🔢 Versioning

Project ini menggunakan Semantic Versioning: `major.minor.patch`

| Komponen | Naik saat | Reset |
|----------|-----------|-------|
| **major** | milestone besar, arsitektur, breaking change | `minor=0, patch=0` |
| **minor** | fitur baru / fitur dihapus | `patch=0` |
| **patch** | bugfix, optimasi, maintenance | — |

### Section Changelog

| Section | Deskripsi |
|---------|-----------|
| ✨ Fitur Baru | Fitur baru ditambahkan |
| 🚮 Fitur Dihapus | Fitur dihapus/dinonaktifkan |
| ♻️ Perubahan Fitur | Perubahan fitur existing |
| 🔧 Optimasi & Penyesuaian | Optimasi, refactor, maintenance |
| 🐞 Bug Fixes | Perbaikan bug |
| 💡 Catatan | Informasi tambahan |
| 🗒️ File Added | File baru |
| ✏️ File Changed | File diubah |
| 🔥 File Removed | File dihapus |

Format judul entry: `# [x.x.x] - yyyy-mm-dd versionCode xxx` — versionName & versionCode dicatat di judul, tanpa section 🔢 Version.

Entry yang di-merge (beberapa versi digabung dalam satu entry): section 🗒️ File Added, ✏️ File Changed, dan 🔥 File Removed diabaikan (tidak ditulis).

### Dependencies

| Library | Versi | Fungsi |
|---------|-------|--------|
| AndroidX AppCompat | 1.7.1 | UI compatibility |
| Material Design | 1.12.0 | Material 3 components |
| ConstraintLayout | 2.2.1 | Layout |
| Core SplashScreen | 1.0.1 | SplashScreen API |
| RecyclerView | 1.3.2 | Drag & drop animasi |
| GSON | 2.10.1 | JSON serialization |
| Markwon | 4.6.2 | Markdown renderer (core, ext-tables, ext-tasklist) |
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

Lihat [STRUKTUR.md](STRUKTUR.md) untuk struktur project lengkap beserta statistik.
