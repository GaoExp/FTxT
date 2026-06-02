# 📖 Panduan Penggunaan FTxT (FunText)

## 1. Buka Aplikasi
Jalankan aplikasi **FTxT (FunText)**.

## 2. Edit Teks
Isi field:
`Masukkan teks overlay`

## 3. Atur Ukuran Teks
Gunakan **SeekBar Ukuran Teks**.
Range:
`1–150 sp` (Teks) / `5–140 sp` (FPS)
Perubahan langsung diterapkan jika overlay aktif.

## 4. Pilih Warna
Tekan tombol:
`Pilih Warna`
Dialog akan menampilkan:
- Nama warna otomatis
- HEX, HSV, ARGB
- R, G, B, A slider

Klik:
- **OK** → Terapkan
- **Cancel** → Batal

## 5. Aktifkan Overlay
Nyalakan switch:
`Overlay ON`
Jika permission sudah diberikan, teks akan langsung muncul.

**Catatan:** Aplikasi akan otomatis meminta izin dan menjalankan overlay saat dibuka jika sebelumnya sudah aktif.

## 6. Nonaktifkan Overlay
Matikan switch:
`Overlay OFF`
Overlay hilang, konfigurasi tetap tersimpan.

## 7. Toolbar Icons
Ikon di pojok kanan toolbar:
- Gear ⚙️ → Buka Pengaturan (dokumentasi in-app + toggle izin aplikasi)
- Orientasi Layar ↔ → Toggle orientasi layar Potret/Lanskap
- Bulan/Matahari 🌙☀️ → Toggle tema gelap/terang (default malam)

Header toolbar juga menampilkan **versi aplikasi** (contoh: "FunText v3.9.2.62.0 Beta") di navigation drawer header.

## 8. Navigation Drawer
Tap ikon hamburger (☰) di kiri toolbar untuk membuka drawer navigasi.
- Pilih modul overlay yang diinginkan.

## 9. Background Config
Aktifkan switch Background untuk menampilkan latar belakang overlay:
- Pilih warna background (dengan alpha/transparansi via color picker)
- Atur ukuran background (padding dari teks, 0–80 px)
- Atur offset X/Y background (-60 hingga 60 px)
- Atur margin background (0–30 px)
- Atur radius background untuk rounded corner (0–50 px)
- Background dan Shadow adalah fitur terpisah, dapat dikonfigurasi independent
- Klik label untuk edit nilai manual via dialog input

## 10. Preset Full-Konfigurasi (Simpan/Muat/Tagging/Share)

Setiap panel overlay memiliki tombol:
- **Simpan** — Menyimpan seluruh konfigurasi panel saat ini (posisi, ukuran, warna, shadow, background, orientasi, touchPassthrough, safeArea, dan toggle display spesifik) ke dalam preset GSON v2.
- **Muat** — Menampilkan daftar preset tersimpan dengan fitur multi-select, rename, reorder (↑↓), hapus, dan tag. Pilih salah satu untuk menerapkan seluruh konfigurasi ke panel.
- **E/I** (Ekspor/Impor) — Ekspor preset ke file (Downloads), atau impor dari file (file picker).
- **Bagikan** — Bagikan preset via Android share intent (native).

Fitur preset v2:
- Metadata: tags (label cari), favorite flag, timestamp (dibuat/diubah), thumbnail warna.
- Version history: simpan hingga 10 versi lama, bisa revert ke versi sebelumnya.
- Pencarian: cari preset berdasarkan nama atau tag.
- Selective apply: saat muat, pilih opsi apply (posisi saja, warna saja, background saja, dll) untuk merge partial config.

## 11. Orientasi Layar
Ketuk ikon orientasi layar ↔ di toolbar untuk toggle orientasi antara Potret dan Lanskap. Posisi overlay disimpan terpisah per orientasi dan dipulihkan otomatis.

## 12. Network Speed Meter
Aktifkan switch **Network Speed** di sidebar "Network Stats" untuk menampilkan overlay kecepatan internet real-time.
- Format: `↓128KB/s ↑128KB/s`, otomatis berubah ke MB/s saat >1024 KB/s.
- Konfigurasi ukuran, warna, shadow, background, dan kunci posisi tersedia.

## 13. Watermark Overlay
Aktifkan switch **Watermark** di sidebar "Watermark" untuk menampilkan teks watermark di atas semua aplikasi.
- Edit teks watermark sesuai keinginan
- Atur ukuran, warna (default semi-transparan `0x55FFFFFF`)
- Konfigurasi shadow, background, dan kunci posisi tersedia
- Posisi default di tengah layar

### Watermark Seal Pattern
Aktifkan **Mode Segel** di panel Watermark untuk mengubah watermark menjadi pola segel berulang diagonal:
- Atur **Spasi Horizontal** (50–500px) — jarak antar teks di sumbu horizontal
- Atur **Spasi Vertikal** (50–500px) — jarak antar teks di sumbu vertikal  
- Atur **Sudut** (-90° hingga 90°) — rotasi teks, default -30°
- Kontrol posisi tunggal dan safe area otomatis disembunyikan saat mode segel aktif
- Teks, ukuran, dan warna tetap bisa diatur seperti mode biasa

## 14. Shadow Config
Aktifkan switch Shadow untuk menampilkan konfigurasi bayangan teks:
- Pilih warna shadow (dengan alpha/transparansi via color picker)
- Atur blur radius (0–50 px)
- Atur offset X, offset Y (-60 hingga 60 px, default 0)
- Klik label untuk edit nilai manual via dialog input
