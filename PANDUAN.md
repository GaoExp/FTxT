# Panduan Penggunaan FTxT (FunText)

## Daftar Isi

- [Persyaratan](#persyaratan)
- [Memulai](#memulai)
- [Navigasi](#navigasi)
- [Toolbar](#toolbar)
- [Fitur Overlay](#fitur-overlay)
- [Pengaturan](#pengaturan)
- [Izin](#izin)

## Persyaratan

- Android 8.0 (API 26) atau lebih baru
- Izin overlay aplikasi lain (SYSTEM_ALERT_WINDOW)

## Memulai

1. Install APK FTxT (FunText)
2. Saat pertama dibuka, aplikasi akan meminta izin overlay, notifikasi, dan optimasi baterai
3. Berikan izin yang diminta
4. Aplikasi akan otomatis menjalankan overlay teks default
5. Pilih modul overlay dari navigation drawer

## Navigasi

Aplikasi menggunakan **Navigation Drawer** (sidebar) yang bisa dibuka dengan:
- Tap ikon hamburger (☰) di kiri toolbar
- Swipe dari tepi kiri layar

Drawer terdiri dari menu berikut:

| Menu | Ikon | Fungsi |
|------|------|--------|
| Floating Text | T | Teks overlay kustom |
| FPS Display | FPS | Counter FPS real-time |
| Jam Digital | Jam | Waktu real-time 24 jam |
| Suhu Baterai | °C | Suhu baterai dalam Celsius |
| Battery Percentage | % | Persentase baterai |
| Battery Current | V/A | Tegangan, arus, daya baterai |
| Network Stats | ↓↑ | Kecepatan internet real-time |
| Watermark | Watermark | Teks watermark transparan |
| Pengaturan | Gear | Pengaturan & izin aplikasi |
| Kill Service | X | Hentikan semua layanan overlay + tutup app |
| Keluar | → | Tutup UI aplikasi, overlay tetap berjalan |

## Toolbar

Ikon di pojok kanan toolbar:

| Ikon | Fungsi |
|------|--------|
| Gear ⚙️ | Buka Pengaturan (izin + dokumentasi) |
| Orientasi ↔ | Toggle layar Potret/Lanskap |
| Bulan/Matahari 🌙☀️ | Toggle tema gelap/terang |

Header toolbar menampilkan navigasi drawer dan judul modul yang aktif.

## Fitur Overlay

Setiap panel overlay memiliki pengaturan yang dikelompokkan dalam **section collapsible**:
- **▾ Tampilan** — Ukuran teks, warna, dan opsi tampilan
- **▾ Posisi** — Kontrol posisi (slider X/Y, D-Pad, preset, safe area)
- **▾ Shadow** — Konfigurasi shadow (toggle, warna, blur, offset)
- **▾ Background** — Konfigurasi background (toggle, warna, padding, offset, margin, radius)

Klik header section untuk membuka/tutup grup. Semua section terbuka secara default.

### Floating Text
- Masukkan teks kustom di kolom input
- Atur ukuran (1–150sp), warna, shadow, background
- Overlay muncul di atas semua aplikasi
- Bisa digeser (drag) dan dikunci posisinya

### FPS Display
- Menampilkan frame rate real-time
- Opsi "Hanya Nilai" untuk menyembunyikan label FPS
- **Warna Label Terpisah** — Tombol "Warna Label" untuk warna teks "FPS", sementara "Pilih Warna" untuk nilai angka
- Ukuran 5–140sp, dengan shadow, background, dan kontrol posisi
- Interval update bisa diatur 0.2–10 detik (default 1d)

### Jam Digital
- Format 24 jam `HH:mm:ss`, update tiap 1 detik
- Ukuran default 48sp, bisa diatur warna, shadow, background
- Kontrol posisi lengkap (slider, D-Pad, preset)

### Suhu Baterai
- Menampilkan suhu baterai dalam °C dan/atau persentase
- **Warna Label Terpisah** — Tombol "Warna Label" untuk warna satuan °C/% terpisah dari nilai angka
- Opsi tampilkan hanya nilai suhu, persentase
- Interval update bisa diatur 0.2–10 detik (default 5d)

### Battery Percentage
- Menampilkan persentase baterai dalam %
- **Warna Label Terpisah** — Tombol "Warna Label" untuk warna label % terpisah dari nilai angka
- Konfigurasi ukuran, warna, shadow, background, kontrol posisi

### Battery Current
- Menampilkan tegangan (mV), arus (mA), dan daya (W) baterai
- **Warna Label Terpisah** — Tombol "Warna Label" untuk warna satuan mV/mA/W terpisah dari nilai angka
- Opsi tampilkan mV, mA, W (toggle)
- Interval update bisa diatur 0.2–10 detik (default 1d)

### Network Speed
- Kecepatan internet real-time format `↓128KB/s ↑128KB/s`
- **Warna Label Terpisah** — Tombol "Warna Label" untuk warna indikator ↓↑ dan satuan KB/MB/s terpisah dari nilai angka
- Otomatis berubah ke MB/s saat >1024 KB/s
- Polling via TrafficStats, interval update bisa diatur 0.2–10 detik (default 1d)

### Watermark
- Teks watermark kustom dengan opacity semi-transparan
- Ukuran 5–200sp, warna, shadow, background, kontrol posisi
- Posisi default di tengah layar

#### Watermark Seal Pattern
- Aktifkan **Mode Segel** untuk pola teks berulang diagonal
- Atur **Spasi Horizontal** (50–500px) — jarak antar teks horizontal
- Atur **Spasi Vertikal** (50–500px) — jarak antar teks vertikal
- Atur **Sudut** (-90° hingga 90°) — rotasi teks, default -30°
- Kontrol posisi dan safe area disembunyikan saat mode segel aktif

### Color Picker
- Color wheel full disk dengan crosshair
- Slider ARGB (Alpha, Red, Green, Blue) — 0–255
- Two-way sync: wheel ↔ slider
- Nama warna otomatis, HEX 8 digit (#AARRGGBB)
- Edit HEX manual, klik label ARGB untuk edit numerik
- Long-press nilai warna untuk salin ke clipboard

### Background & Shadow
- Background: warna (dengan alpha), ukuran/padding (0–80px), offset X/Y (-60–60px), margin (0–30px), radius rounded corner (0–50px)
- Shadow: warna (dengan alpha), blur radius (0–50px), offset X/Y (-60–60px, default 0)
- Background dan Shadow adalah fitur terpisah, bisa diatur independen
- Klik label slider untuk edit nilai manual via dialog

### Preset System (v2)
Akses preset dari **icon gear → "Muat Preset"**. Dialog preset terbuka dengan tombol:
- **Simpan** — Simpan seluruh konfigurasi panel aktif (posisi, ukuran, warna, shadow, background, orientasi, touchPassthrough, safeArea, toggle display)
- Pilih preset dari daftar untuk **Muat** (search, filter, favorite, rename, delete, reorder, selective apply)
- **E/I** — Ekspor ke file (Downloads) / Impor dari file (file picker)
- **Bagikan** — Bagikan preset via Android share intent

Metadata: tags, favorite, timestamp, thumbnail warna. Version history hingga 10 versi.

### Kontrol Posisi
- **Slider X/Y** — Posisi horizontal dan vertikal (persentase 0.0–1.0)
- **D-Pad** — Tombol arah ↑↓←→ dengan tahan untuk repeat
- **Safe Area** — Batasi agar overlay tidak masuk area notch/cutout
- **Touch Passthrough** — Kunci posisi agar sentuhan tembus ke aplikasi belakang
- Posisi tersimpan otomatis per orientasi layar

## Pengaturan

- **Izin Aplikasi** — Kelola izin overlay, notifikasi, dan optimasi baterai
- **Dokumentasi** — Baca dokumentasi in-app (README, PANDUAN, CHANGELOG)

## Izin

| Izin | Fungsi |
|------|--------|
| `SYSTEM_ALERT_WINDOW` | Menampilkan overlay di atas aplikasi lain |
| `POST_NOTIFICATIONS` (13+) | Notifikasi kontrol foreground service |
| `FOREGROUND_SERVICE` | Menjalankan service overlay di latar depan |
| `FOREGROUND_SERVICE_SPECIAL_USE` | Service overlay khusus |
| `WAKE_LOCK` | Mencegah CPU tidur saat overlay aktif |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Mencegah overlay dihentikan sistem |

Izin diminta otomatis saat pertama aplikasi dibuka. Kelola izin bisa dilakukan di **Pengaturan > Izin Aplikasi**.

---

