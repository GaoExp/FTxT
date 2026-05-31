# 📖 Panduan Penggunaan FTxT

## 1. Buka Aplikasi
Jalankan aplikasi **FTxT**.

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
- Bulan 🌙 → Toggle tema gelap/terang

Header toolbar juga menampilkan **versi aplikasi** (contoh: "FTxT v2.3.1.33.0") di samping nama aplikasi di navigation drawer header.

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

## 10. Preset Full-Konfigurasi (Simpan/Muat)

Setiap panel overlay memiliki tombol:
- **Simpan** — Menyimpan seluruh konfigurasi panel saat ini (posisi, ukuran, warna, shadow, background, orientasi) ke dalam preset GSON.
- **Muat** — Menampilkan daftar preset tersimpan. Pilih salah satu untuk menerapkan seluruh konfigurasi ke panel.
- **E/I** (Ekspor/Impor) — Ekspor preset ke clipboard sebagai JSON, atau impor dari clipboard.

## 11. Orientasi Layar
Ketuk ikon orientasi layar ↔ di toolbar untuk toggle orientasi antara Potret dan Lanskap. Posisi overlay disimpan terpisah per orientasi dan dipulihkan otomatis.

## 12. Network Speed Meter
Aktifkan switch **Network Speed** di sidebar "Network Stats" untuk menampilkan overlay kecepatan internet real-time.
- Format: `↓128KB/s ↑128KB/s`, otomatis berubah ke MB/s saat >1024 KB/s.
- Konfigurasi ukuran, warna, shadow, background, dan kunci posisi tersedia.

## 13. Shadow Config
Aktifkan switch Shadow untuk menampilkan konfigurasi bayangan teks:
- Pilih warna shadow (dengan alpha/transparansi via color picker)
- Atur blur radius (0–50 px)
- Atur offset X, offset Y (-60 hingga 60 px, default 0)
- Klik label untuk edit nilai manual via dialog input
