# Panduan Penggunaan FTxT (FunText)

---

## Daftar Isi

- [Persyaratan](#persyaratan)
- [Memulai](#memulai)
- [Navigasi](#navigasi)
- [Toolbar](#toolbar)
- [Fitur Overlay](#fitur-overlay)
- [Pengaturan](#pengaturan)
- [Izin](#izin)

---

## Persyaratan

- Android 8.0 (API 26) atau lebih baru
- Izin overlay aplikasi lain (SYSTEM_ALERT_WINDOW)

---

## Memulai

1. Install APK FTxT (FunText)
2. Saat pertama dibuka, aplikasi akan meminta izin overlay, notifikasi, dan optimasi baterai
3. Berikan izin yang diminta
4. Aplikasi akan otomatis menjalankan overlay teks default
5. Pilih modul overlay dari navigation drawer

---

## Navigasi

Aplikasi menggunakan **Navigation Drawer** (sidebar) yang bisa dibuka dengan:
- Tap ikon hamburger (☰) di kiri toolbar
- Swipe dari tepi kiri layar

Drawer terdiri dari menu berikut (urutan default; item panel bisa di-reorder dengan long-press lalu drag):

| Menu | Fungsi |
|------|--------|
| Floating Text | Teks overlay kustom |
| FPS Display | Counter FPS real-time |
| Network Stats | Kecepatan internet real-time |
| Battery Info | Info baterai (suhu, persentase, voltase, arus, daya) + Battery Strip |
| Clock Module | Waktu real-time 24 jam (Jam Digital) |
| Crosshair | Placeholder (coming soon) |
| Info Memori | Monitoring memori real-time |
| Logo Display | Placeholder (coming soon) |
| Color Picker | Color picker wheel + slider H/S/V/RGB |
| Debuging | Panel debugging |
| Kill Service | Hentikan semua layanan overlay + tutup app |
| Keluar | Tutup UI aplikasi, overlay tetap berjalan |

Pengaturan & izin aplikasi diakses melalui ikon gear (⚙️) di toolbar, bukan dari drawer.

---

## Toolbar

Ikon di pojok kanan toolbar:

| Ikon | Fungsi |
|------|--------|
| Gear ⚙️ | Buka Pengaturan (izin + dokumentasi) |
| Orientasi ↔ | Toggle layar Potret/Lanskap |
| Bulan/Matahari 🌙☀️ | Toggle tema gelap/terang |

Header toolbar menampilkan navigasi drawer dan judul modul yang aktif.

---

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
- **Mode Segel** — Aktifkan pola teks berulang diagonal. Atur **Spasi Horizontal** (50–500px), **Spasi Vertikal** (50–500px), dan **Sudut** (-180° hingga 180°). Kontrol posisi disembunyikan dan Kunci Posisi dinonaktifkan saat mode segel aktif.

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

### Battery Info
Panel Battery Info memiliki **3 tab** di bagian bawah: **Monitor**, **Overlay**, dan **Battery Strip**.

**Tab Monitor** — Placeholder untuk monitoring baterai (coming soon).

**Tab Overlay** — Konfigurasi info baterai:
- Menampilkan **suhu (°C)**, **persentase (%)**, **voltase (V)**, **arus (mA)**, dan **daya (W)** dalam satu overlay
- **OrderZones** — Atur urutan dan visibilitas info baterai via drag-and-drop chip. Chip bisa diseret antar zona Aktif/Nonaktif untuk menampilkan/menyembunyikan info, dan diurutkan dalam satu zona
- **Warna Label Terpisah** — Tombol "Warna Label" untuk warna satuan terpisah dari nilai angka
- **Warna Pemisah** — Komponen dipisahkan tanda `|` (misal `37.4°C | 87% | 4.1V`) yang warnanya bisa diatur sendiri
- Opsi toggle tampilkan komponen tertentu, atau hanya nilai tanpa label
- Konfigurasi ukuran, warna, shadow, background, kontrol posisi
- Interval update bisa diatur 0.2–10 detik (default 5d)

**Tab Battery Strip** — Konfigurasi bar baterai (sebelumnya panel Battery Bar terpisah):
- Menampilkan bar baterai sebagai strip di layar (level mengikuti persentase baterai)
- **Mode Cepat** — Bar menempel penuh di salah satu sisi layar (Atas/Bawah/Kiri/Kanan). Posisi & panjang otomatis mengikuti sisi yang dipilih. Kontrol posisi manual nonaktif dalam mode ini. Bar menempel penuh di tepi tanpa jarak — **margin 8dp dihilangkan** sehingga tidak ada gap antara bar dan sisi layar, namun **Area Aman tetap terkunci aktif** (checkbox tercentang, disabled).
- **Mode Manual** — Matikan "Mode Cepat" untuk mengatur panjang bar (0–100%) dan posisi bebas (slider X/Y, D-Pad) per orientasi layar. **Area Aman otomatis terkunci aktif** dalam mode ini — bar tidak bisa masuk area notch/cutout dan posisi selalu di-clamp dalam batas layar. Checkbox Area Aman **selalu terkunci aktif** (tercentang, disabled) di kedua mode.
- **Warna Level** — Pemilih skema warna otomatis berdasarkan level baterai: **Tanpa Skema** (warna tetap pilihan user), **Klasik 3-warna** (hijau >20%, kuning ≤20%, merah ≤10%), **Hue Gradien** (warna bergradasi per segmen: 0–20% hue `1°` S70%, 21–50% hue `2°→100°` S70%, 51–100% hue `102°→260°` dengan saturasi naik `71%→100%`). Saat skema dipilih, langsung aktif.
- **Animasi Pengisian Daya** — Efek saat charging: **Animasi Shine** (on/off, default nonaktif), **Kecepatan Shine** (0,2–5,0 detik per sapuan, step 0,1 detik, default 1,8 detik), **Lebar Band** (2–98% dari panjang bar, default 25%), dan **Animasi Wave saat charging** (gelombang mengalir sepanjang bar): on/off, kecepatan (0,2–5,0 detik), intensitas (10–100%, default 60%).
- **Animasi Baterai Rendah** — Saat level di bawah ambang low, bar menampilkan **animasi Wave** (kedutan gelombang) yang berjalan bersamaan dengan animasi fade: on/off, kecepatan (0,2–5,0 detik), intensitas (10–100%, default 60%). Section ini juga berisi Warna Low, Ambang Low, **Animasi Fade** (on/off, default nonaktif), dan Kecepatan Fade.
- **Warna Low hanya berfungsi saat skema Tanpa Skema** — jika skema Klasik 3-warna atau Hue Gradien aktif, pemilih Warna Low diburamkan & nonaktif karena skema sudah memetakan warna di level rendah.
- Pengaturan lain: ketebalan, radius sudut, strip kosong, shadow, kunci posisi, area aman.

### Network Speed
- Kecepatan internet real-time format `↓128KB/s ↑128KB/s`
- **Warna Label Terpisah** — Tombol "Warna Label" untuk warna indikator ↓↑ dan satuan KB/MB/s terpisah dari nilai angka
- Otomatis berubah ke MB/s saat >1024 KB/s
- Polling via TrafficStats, interval update bisa diatur 0.2–10 detik (default 1d)

### Memory Stats
Panel Memory Stats memiliki **2 tab**: **Monitor** dan **Overlay**.

**Tab Monitor** — Info memori real-time 14 nilai:
- Java Heap, Native Heap, Graphics, Total, Gagal (MB)
- Execution Time, Execution Time Average (ms)
- Total Free RAM, Total RAM (MB)
- Jumlah Proses, Proses Active, Proses Stopped, Proses Cached, Proses Minimum
- Bar visualisasi RAM
- Tombol **Export** dan **Copy** untuk snapshot

**Tab Overlay** — Konfigurasi overlay Memory:
- OrderZones untuk mengatur urutan item via drag-and-drop
- Toggle visibilitas tiap item (Java Heap, Native Heap, dll)
- Konfigurasi ukuran, warna, label, separator, shadow, background, posisi, orientasi
- Opsi **Background Monitor** — Monitoring memori tetap berjalan meski service overlay tidak aktif (opsional), dengan interval polling yang bisa diatur

### Color Picker
Color Wheel dan Hue/Saturation/Value/Alpha slider tampil bersamaan dalam satu panel:

- **Color Wheel** — Full disk dengan crosshair, sentuh untuk pilih warna
- **Slider H/S/V** — Hue (0–360°), Saturation (0–100%), Brightness (0–100%), masing-masing dengan gradient background dinamis
- **Slider Alpha** — Opacity (0–255) dengan checkerboard transparansi
- **Slider RGB** — Klik "Tampilkan slider RGB" untuk edit Red/Green/Blue (0–255)
- **Two-way sync** — Wheel, slider H/S/V, slider RGB saling sinkron

**Informasi & kontrol:**
- Nama warna otomatis, HEX 8 digit (#AARRGGBB)
- Edit HEX manual via ikon pensil
- Long-press nilai warna untuk salin ke clipboard
- Grid Saved Colors (simpan/load/hapus, maks 16 warna)

### Background & Shadow
- Background: warna (dengan alpha), ukuran/padding (0–80px), offset X/Y (-60–60px), margin (0–30px), radius rounded corner (0–50px)
- Shadow: warna (dengan alpha), blur radius (0–50px), offset X/Y (-60–60px, default 0)
- Background dan Shadow adalah fitur terpisah, bisa diatur independen
- **Slider Label Edit** — Klik label slider untuk edit nilai manual via dialog. Berlaku di semua panel: ukuran, shadow, dan background (nilai offset mendukung angka negatif), serta slider animasi Battery Bar (kecepatan dalam detik desimal, panjang/ambang/lebar band/intensitas dalam %, ketebalan/radius dalam px). Slider posisi X/Y tidak termasuk.

### Preset System (v2)
Akses preset dari **icon gear → "Muat Preset"**. Dialog preset terbuka dengan:
- **Search bar** — Cari preset berdasarkan nama atau tag
- **Header**: tombol **Tandai** — Aktifkan mode pilih, checkbox muncul di tiap item
- **Header**: tombol **Tandai Semua** — Centang/hapus centang semua item
- **Bottom bar (normal)**: **Simpan** — Simpan seluruh konfigurasi panel aktif; **Impor** — Impor dari file
- **Bottom bar (Tandai)**: **Hapus**, **Favorit**, **Bagikan**, **Ekspor** — Aksi batch untuk item terpilih
- **Tap item**: menu dengan **Gunakan Preset** di urutan pertama
- **Long-press item**: **Drag & drop** — Tukar posisi preset dalam daftar

Metadata: tags, favorite, timestamp, thumbnail warna. Version history hingga 10 versi. Preset baru diletakkan di urutan teratas.

### Kontrol Posisi
- **Slider X/Y** — Posisi horizontal dan vertikal (persentase 0.0–1.0)
- **D-Pad** — Tombol arah ↑↓←→ dengan tahan untuk repeat
- **Safe Area** — Batasi agar overlay tidak masuk area notch/cutout
- **Touch Passthrough** — Kunci posisi agar sentuhan tembus ke aplikasi belakang
- Posisi tersimpan otomatis per orientasi layar

---

## Pengaturan

- **Izin Aplikasi** — Kelola izin overlay, notifikasi, dan optimasi baterai
- **Dokumentasi** — Baca dokumentasi in-app (README, CHANGELOG, PANDUAN) dengan render Markdown penuh via Markwon
- **Crash Logger** — Saat force close, stack trace otomatis ditulis ke `FTxT_crash_*.txt` di folder Download (plus cadangan prefs) agar bug mudah dilaporkan tanpa logcat/adb

---

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

