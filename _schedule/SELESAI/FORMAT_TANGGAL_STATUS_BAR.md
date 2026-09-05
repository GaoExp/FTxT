# Rencana Konsep: Opsi Format Tanggal pada Ikon Status Bar

> **Status: SELESAI** — diimplementasikan di FTxT 4.89.1 (entry berjalan).
> Berlaku untuk FTxT versi berjalan (4.89.1), pengembangan lanjutan dari fitur isi ikon status bar (mode Suhu / Persen / Tanggal).
> Dokumen ini dibuat agar konsep tidak hilang saat sesi kerja terputus (AndroidIDE force close / disconnect).

---

## 1. Latar Belakang

Fitur **Isi Ikon Status Bar** (entry `4.89.1`) sudah berjalan: user bisa memilih isi ikon notifikasi status bar **Suhu** / **Persen** / **Tanggal**, dengan slider "Ukuran Isi Ikon" per mode.

Mode **Tanggal** semula hanya menampilkan dua baris: `d` (tanggal tanpa nol depan) + nama hari 3 huruf **bahasa Inggris** (`6` / `Sun`). Struktur blok, pemusatan baris, dan jarak antar baris sudah diperbaiki (baris hari tidak lagi terpotong / ada ruang kosong).

Pengembangan yang dikerjakan: **format tanggal bisa dipilih** (8 macam kombinasi tanggal + nama hari/bulan) dan **bahasa nama hari/bulan** bisa `in` (Indonesia) atau `eng` (Inggris), plus **ukuran tiap baris bisa diatur sendiri**.

## 2. Rancangan UI

Ditempatkan dalam blok mode Tanggal di halaman Konfigurasi (di dalam `statusBarDateScaleGroup`, **di atas** slider "Ukuran Isi Ikon"), hanya tampil saat mode isi = **Tanggal**:

1. **Bahasa Nama Hari/Bulan** (Radio `in` / `eng`) — label tombol persis `in` dan `eng`.
2. **Format** (Dropdown/Spinner) — 8 pilihan format.
3. **Tiga slider ukuran**: "Ukuran Tanggal" (baris atas), "Ukuran Hari" (baris bawah), dan "Ukuran Isi Ikon (Tanggal)" (skala keseluruhan).

### 2.1 Daftar 8 Format

Label format memakai **placeholder simbol**, bukan contoh angka aktual. Karena UI berbahasa Indonesia, nama memakai `Hari` / `Bulan`:

1. `d + Hari`
2. `dd + Hari`
3. `d + Bulan`
4. `dd + Bulan`
5. `d/M + Hari`
6. `dd/MM + Hari`
7. `Hari + d/M`
8. `Hari + dd/MM`

### 2.2 Contoh Hasil Tampilan

Untuk tanggal **6 Sep 2026** (dua baris atas/bawah):

| Format | Baris atas (eng) | Baris bawah (eng) | Baris atas (in) | Baris bawah (in) |
|---|---|---|---|---|
| `d + Hari` | `6` | `Sun` | `6` | `Min` |
| `dd + Hari` | `06` | `Sun` | `06` | `Min` |
| `d + Bulan` | `6` | `Sep` | `6` | `Sep` |
| `dd + Bulan` | `06` | `Sep` | `06` | `Sep` |
| `d/M + Hari` | `6/9` | `Sun` | `6/9` | `Min` |
| `dd/MM + Hari` | `06/09` | `Sun` | `06/09` | `Min` |
| `Hari + d/M` | `Sun` | `6/9` | `Min` | `6/9` |
| `Hari + dd/MM` | `Sun` | `06/09` | `Min` | `06/09` |

### 2.3 Bahasa Nama Hari / Bulan

Radio `in` (**Indonesia**) dan `eng` (**Inggris**):

| Bahasa | Hari | Bulan |
|---|---|---|
| `in` | `Min Sen Sel Rab Kam Jum Sab` | `Jan Feb Mar Apr Mei Jun Jul Agu Sep Okt Nov Des` |
| `eng` | `Sun Mon Tue Wed Thu Fri Sat` | `Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec` |

- `in` → hari: `Min Sen Sel Rab Kam Jum Sab`; bulan: `Jan Feb Mar Apr Mei Jun Jul Agu Sep Okt Nov Des`.
- `eng` → hari: `Sun Mon Tue Wed Thu Fri Sat`; bulan: `Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec`.

## 3. Aturan / Asumsi Implementasi

- **Default:** bahasa = `eng` (perilaku saat ini yang menampilkan `Sun`), format = `d + Hari`.
- **Penyimpanan (SharedPreferences):**
  - `status_bar_date_format` → int 0–7 (indeks format di atas).
  - `status_bar_date_lang` → `"in"` / `"eng"`.
  - `status_bar_date_top_size` → skala baris tanggal (float 0.6–1.6, default 1.0).
  - `status_bar_date_bottom_size` → skala baris hari (float 0.6–1.6, default 1.0).
- **Pratinjau di Settings & ikon notifikasi langsung mengikuti pilihan** (format & bahasa), tanpa perlu membangun ulang notifikasi manual.
- Mode Tanggal tetap **dua baris**: baris atas isi tanggal (sesuai format), baris bawah nama hari/atau bulan (sesuai format).
- Ukuran baris atas & bawah bisa berbeda (diatur lewat slider masing-masing), skala keseluruhan mempengaruhi keduanya.
- Perilaku existing yang TIDAK boleh berubah (regresi wajib dijaga):
  - Ikon direfresh hanya saat nilai berubah (optimasi 4.89.1).
  - Blok dua baris tetap dipusatkan di tengah bitmap (perbaikan bug: baris hari tidak terpotong saat diperbesar).
  - Jarak antar baris tetap rapat dengan jarak tipis konsisten (bukan ruang kosong besar).
  - Slider ukuran isi tetap berfungsi penuh untuk mode Tanggal (perbesaran menyebar simetris dari tengah).

## 4. File yang Terlibat

| File | Peran |
|---|---|
| `app/src/main/java/exp/ftxt/SettingsActivity.java` | Halaman Konfigurasi: blok mode Tanggal (`statusBarDateScaleGroup`), dropdown format, radio bahasa, tiga slider ukuran |
| `app/src/main/res/layout/activity_settings.xml` | Layout blok mode Tanggal: dropdown format + radio `in`/`eng` + tiga slider di atas blok lain |
| `app/src/main/java/exp/ftxt/core/NotificationHelper.java` | Generate bitmap ikon status bar mode Tanggal: terapkan format, bahasa, dan skala per baris |

## 5. Status

- [x] Konfirmasi rancangan dari user (default format `d + Hari`, bahasa `eng`).
- [x] Implementasi UI dropdown format + radio bahasa di Settings.
- [x] Implementasi render bitmap ikon mengikuti format & bahasa.
- [x] Implementasi tiga slider ukuran (baris tanggal, baris hari, skala keseluruhan).
- [x] Self-check regresi (lihat §3) & rampungkan CHANGELOG entry berjalan.