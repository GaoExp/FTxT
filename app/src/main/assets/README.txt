# FTxT (FunText) — Floating Text Overlay

**Current Release:** `3.9.3.67.1` **Beta**
**Last Updated:** `2026-06-01`

FTxT (FunText) adalah aplikasi Android overlay yang memungkinkan Anda menampilkan teks floating di atas aplikasi lain dengan fitur kustomisasi lengkap untuk ukuran, warna, transparansi, posisi, dan kontrol sentuhan.

---

## ✨ Fitur Utama

- **Floating Text Overlay** — Teks custom di atas semua aplikasi, real-time update
- **FPS Display Overlay** — FPS counter draggable dengan opsi hanya angka
- **Jam Digital Overlay** — Waktu real-time 24 jam `HH:mm:ss`, update tiap 1 detik
- **Suhu Baterai Overlay** — Suhu baterai dalam °C, update tiap 5 detik
- **Battery Percentage Overlay** — Persentase baterai dalam %, update tiap 5 detik
- **Battery Current Overlay** — Tegangan (mV), arus (mA), dan daya (W) baterai, update tiap 5 detik
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
- **Overlay Toggle** — Auto-start, permission handling, WakeLock, foreground service
- **Android SplashScreen** — SplashScreen API resmi tanpa fake loading

---

## 📚 Dokumentasi Terkait

| File | Isi |
|------|-----|
| [PANDUAN.md](PANDUAN.md) | Cara penggunaan lengkap (11 langkah) |
| [STRUKTUR.md](STRUKTUR.md) | Struktur project & deskripsi file |
| [DEVELOPMENT.md](DEVELOPMENT.md) | Info teknis, environment, versioning |
| [TENTANG.md](TENTANG.md) | Lisensi, author, support |
| [CHANGELOG.md](CHANGELOG.md) | Riwayat perubahan lengkap |

Dokumentasi juga tersedia di dalam aplikasi melalui **Pengaturan > tombol dokumentasi**.
