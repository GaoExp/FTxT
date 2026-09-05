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
| Crosshair | Overlay bidikan untuk game |
| Info Memori | Monitoring memori real-time (default tersembunyi) |
| Logo Display | Placeholder (coming soon) |
| Color Picker | Color picker wheel + slider H/S/V/RGB |
| Debugging | Panel debugging (default tersembunyi & terkunci password) |
| Kill Service | Hentikan semua layanan overlay + tutup app |
| Keluar | Tutup UI aplikasi, overlay tetap berjalan |

Pengaturan & izin aplikasi diakses melalui ikon gear (⚙️) di toolbar, bukan dari drawer.

Panel **Info Memori** dan **Debugging** tidak tampil di drawer secara default. Keduanya bisa ditampilkan lewat **Pengaturan > Konfigurasi** (lihat section Pengaturan).

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
- **▾ Posisi** — Kontrol posisi (slider X/Y, D-Pad pixel-based 1–20px, preset, safe area)
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

### Crosshair
Overlay bidikan untuk game dengan 44 gaya siap pilih:
- **Gaya Bidikan** — Galeri thumbnail horizontal (48dp cell) dengan 44 pilihan, bisa digeser. Preview berwarna sesuai warna bidikan aktif
- **Ukuran** — 4–160dp, bisa diatur lewat slider atau ketuk label untuk ketik angka persis
- **Opasitas** — 10–100%, label bisa diketuk untuk edit manual
- **Rotasi** — 0–359° langsung diterapkan ke overlay
- **Shadow** — Warna, blur, offset (tidak memperkecil bidikan, ukuran view otomatis membesar)
- **Posisi** — Geser langsung bidikannya, slider X/Y, atau D-Pad. Tombol reset untuk kembali ke tengah layar
- **Kunci Posisi** — Sentuhan menembus ke game (bidikan tak ikut tergeser)
- **Area Aman** — Bidikan tidak melolos keluar tepi layar
- **Preset** — Konfigurasi bisa disimpan/dimuat sebagai preset khusus Crosshair

### Battery Info
Panel Battery Info memiliki **3 tab** di bagian bawah: **Monitor**, **Overlay**, dan **Battery Strip**.

**Tab Monitor** — Pemantauan baterai perangkat real-time dalam 3 kartu:
- **Metrik Real-Time** — Ring gauge melingkar di kiri: arc berwarna gradien hue mengikuti level baterai (skema warna yang sama dengan Battery Strip), di dalam lingkaran tampil level %, kapasitas tersisa (mAh), dan status pengisian singkat (Charging•AC/USB/Wireless / Full / Discharging). Grafik riwayat Persentase berada di sebelah kanan ring, dan grid metrik monospace berjajar di bawahnya: suhu, voltase, arus, daya, cycle count, teknologi baterai (cycle count hanya tampil jika perangkat melaporkan). Badge kondisi suhu (Normal/Panas/Dingin) tampil di header kartu.
- **Grafik Riwayat** — Empat chart Suhu, Daya, Tegangan, dan Arus dengan pemilih rentang 5m/10m/30m/1j/3j/6j/12j/24j/36j/48j (berlaku global untuk semua chart). Tiap chart punya sumbu Y sendiri (skala otomatis mengikuti data) dan angka nilai terkini di ujung garis. Chevron di header kartu untuk melipat/membuka seluruh isi grafik. Data bersumber dari database sehingga riwayat lama tetap terlihat walau aplikasi sempat ditutup.
- **Kesehatan Baterai** — Estimasi kapasitas penuh ala AccuBattery: saat perangkat mengisi daya, segmen pengisian dicatat lalu estimasi diagregasi dengan median lintas sesi (sesi mayoritas waktu layar mati diprioritaskan agar lebih akurat). Tampil: estimasi kapasitas, skor kesehatan (hijau ≥80% / oranye ≥50% / merah <50%), jumlah sesi tercatat, indikator keyakinan, dan status pengumpulan data. Ketuk kartu untuk input kapasitas desain (mAh) — skor kesehatan baru muncul jika kapasitas desain terisi. Tombol **Salin** & **Simpan Snapshot** ada di dalam kartu ini; hasilnya menyertakan catatan kesehatan baterai. Tombol **Reset** untuk menghapus data estimasi dan memulai dari awal.
- Pemantauan berjalan **otomatis full-aktif**: tanpa tombol mulai/hentikan — pencatatan tetap jalan meski overlay mati, panel tertutup, atau aplikasi ditutup, lewat foreground service ringan dengan notifikasi minimal prioritas rendah. Nyala otomatis saat aplikasi dibuka dan direstart otomatis saat boot. Sampling dinamis hemat baterai: rapat ±1 detik saat mengisi daya, ±5 detik saat layar nyala idle, ±30 detik saat layar mati.

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
- **Konfigurasi** — Pengaturan tampilan aplikasi:
  - **Ikon Aplikasi** — Ganti ikon launcher (varian default/alternatif)
  - **Tampilkan panel Info Memori** — Toggle untuk menampilkan/menyembunyikan panel Info Memori dari Navigation Drawer (default OFF)
  - **Tampilkan panel Debugging** — Toggle untuk menampilkan/menyembunyikan panel Debugging dari Navigation Drawer (default OFF). Switch terkunci password: masukkan password lalu tap Unlock untuk membuka, tombol Relock untuk mengunci ulang. Status unlock tersimpan persisten.
- **Dokumentasi** — Baca dokumentasi in-app (README, CHANGELOG, PANDUAN) dengan render Markdown penuh via Markwon
- **Crash Logger** — Saat force close, stack trace otomatis ditulis ke `FTxT_crash_*.txt` di Documents/FTxT/Log_Crash (plus cadangan prefs) agar bug mudah dilaporkan tanpa logcat/adb
- **Log ANR** — Saat main thread macet ≥5 detik (ANR), trace seluruh thread otomatis ditulis ke `FTxT_anr_*.txt` di Documents/FTxT/Log_ANR (plus cadangan prefs) agar peristiwa ANR bisa dilaporkan tanpa logcat/adb

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

