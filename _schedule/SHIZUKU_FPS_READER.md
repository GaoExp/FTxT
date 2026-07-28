# Implementasi Shizuku FPS Reader

## Gambaran
Membaca FPS dari aplikasi/game yang sedang aktif di layar menggunakan Shizuku dan dumpsys gfxinfo.

---

## Fase 1: Persiapan & Integrasi Shizuku

### 1.1 Setup Shizuku SDK
- Download Shizuku SDK dari GitHub: `dev.rikka.shizuku`
- Tambahkan dependency ke `app/build.gradle`:
  - `shizuku-api`
  - `shizuku-processor` (annotation processor)
- Update `gradle.properties` jika perlu

### 1.2 Daftarkan Shizuku Provider
- Tambah `<provider>` di `AndroidManifest.xml` untuk Shizuku
- Konfigurasi authority dengan package name

### 1.3 Minta Izin Shizuku di Aplikasi
- Cek apakah Shizuku sedang berjalan: `Shizuku.pingBinder()`
- Minta izin: `Shizuku.requestPermission(REQUEST_CODE)`
- Handle hasil: granted / denied / not running
- Tampilkan ke user jika Shizuku belum aktif

---

## Fase 2: Deteksi Aplikasi Foreground

### 2.1 Minta Izin UsageStats
- Minta izin `PACKAGE_USAGE_STATS` ke user
- Arahkan ke Settings jika belum aktif

### 2.2 Deteksi Aplikasi Aktif
- Gunakan `UsageStatsManager` untuk ambil aplikasi foreground
- Ambil package name dari aplikasi yang sedang aktif
- Filter: hanya aplikasi dengan kategori `GAME` atau semua aplikasi

### 2.3 Looping Deteksi
- Polling setiap 1 detik untuk cek aplikasi aktif
- Simpan package name terakhir yang terdeteksi
- Jika berubah, update FPS reader ke package baru

---

## Fase 3: Baca FPS via dumpsys

### 3.1 Jalankan Perintah via Shizuku
- Format perintah: `dumpsys gfxinfo <package_name>`
- Kirim via `Shizuku.newProcess()` atau `Runtime.exec()`
- Tangkap output (stdout)

### 3.2 Parse Output gfxinfo
- Cari section "Total frames rendered"
- Cari section "Janky frames"
- Cari section "Number Missed Vsync"
- Hitung FPS dari data frame timing

### 3.3 Hitung FPS
- Cara 1: Total frames / waktu rendering
- Cara 2: Gunakan "Frame stats" untuk data lebih detail
- Cara 3: Hitung dari selisih timestamp frame

---

## Fase 4: Modul Overlay FPS Game

### 4.1 Buat Config Baru
- `GameFpsConfig.java` — Konfigurasi untuk FPS game
- Fields: package name, interval, show label, colors, size, position

### 4.2 Buat Module Baru
- `GameFpsModule.java` — Modul overlay untuk FPS game
- Method: start(), stop(), update()
- Panggil Shizuku untuk baca FPS
- Update overlay text dengan nilai FPS

### 4.3 Buat Position Controller
- `GameFpsPositionController.java` — Kontrol posisi FPS game
- Reuse dari FPS Position Controller yang sudah ada

### 4.4 Buat Panel Controller
- `GameFpsPanelController.java` — UI panel untuk FPS game
- Tampilkan package name yang terdeteksi
- Kontrol: interval, ukuran warna, shadow, background
- Tombol toggle FPS game on/off

---

## Fase 5: Integrasi ke MainActivity

### 5.1 Tambah Menu di Navigation Drawer
- Tambah item "Game FPS" di drawer menu
- Tambah icon dan string resource

### 5.2 Tambah Panel di Activity Main
- Include layout panel_game_fps.xml
- Tambah field dan binding di MainActivity
- Init controller di onCreate

### 5.3 Tambah di FloatingService
- Register GameFpsModule di FloatingService
- Tambah method start/stop/update Game FPS
- Handle lifecycle service

---

## Fase 6: Layout XML

### 6.1 panel_game_fps.xml
- Checkbox: Aktifkan FPS Game
- Toggle: Auto-detect aplikasi aktif
- Input: Package name manual (jika auto off)
- Section Tampilan: Ukuran, warna value, warna label, show label
- Section Posisi: Slider, D-Pad, preset, safe area
- Section Shadow: Toggle, warna, blur, offset
- Section Background: Toggle, warna, padding, offset, margin, radius

---

## Fase 7: Error Handling & Edge Cases

### 7.1 Shizuku Tidak Aktif
- Tampilkan pesan: "Shizuku tidak aktif"
- Berikan tombol untuk buka Shizuku

### 7.2 Izin Shizuku Ditolak
- Tampilkan pesan: "Izin Shizuku ditolak"
- Nonaktifkan fitur FPS Game

### 7.3 dumpsys Gagal
- Handle timeout
- Handle output kosong
- Handle error dari Shizuku

### 7.4 Tidak Ada Aplikasi Foreground
- Jika tidak ada aplikasi game yang aktif
- Tampilkan "---" atau "N/A"

### 7.5 Kompatibilitas
- Test di Android 8, 10, 12, 14
- Handle perbedaan output dumpsys per merk HP
- Fallback jika fitur tidak tersedia

---

## Fase 8: Testing & Debug

### 8.1 Unit Test
- Test parsing output dumpsys
- Test hitung FPS dari frame data

### 8.2 Instrumented Test
- Test integrasi Shizuku
- Test deteksi aplikasi foreground

### 8.3 Manual Test
- Test dengan game berbeda (PUBG, Mobile Legends, dll)
- Test perubahan orientasi
- Test aplikasi berpindah dari foreground ke background
- Test performa (tidak bikin HP panas/lambat)

---

## Estimasi Komponen Baru

| File | Keterangan |
|------|------------|
| `GameFpsConfig.java` | Konfigurasi FPS game |
| `GameFpsModule.java` | Modul overlay FPS game |
| `GameFpsPanelController.java` | UI panel FPS game |
| `GameFpsPositionController.java` | Kontrol posisi FPS game |
| `panel_game_fps.xml` | Layout panel FPS game |

---

## Urutan Pengerjaan

1. Fase 1 (Shizuku setup) — Wajib duluan
2. Fase 3 (dumpsys reader) — Core logic
3. Fase 2 (auto-detect) — Membantu user
4. Fase 4 (modul overlay) — Tampilan
5. Fase 6 (layout) — UI
6. Fase 5 (integrasi) — Konek ke app
7. Fase 7 (error handling) — Stabilitas
8. Fase 8 (testing) — Kualitas

---

## Risiko & Tantangan

- **Output dumpsys berbeda tiap HP** — Perlu penyesuaian parsing
- **Shizuku tidak selalu aktif** — User harus aktifkan manual
- **Izin UsageStats sulit didapat** — Harus ke Settings
- **Performa** — Polling dumpsys terus-menerus bisa bikin HP panas
- **Kompatibilitas** — Belum tentu jalan di semua versi Android

---

## Alternatif Jika Shizuku Tidak Memungkinkan

1. **Game Mode API (Android 12+)** — API resmi tapi terbatas
2. **AccessibilityService** — Bisa tapi banyak keterbatasan
3. **Root + dumpsys langsung** — Paling mudah tapi butuh root
4. **Tanpa FPS game** — Fokus ke fitur lain yang lebih realistis
