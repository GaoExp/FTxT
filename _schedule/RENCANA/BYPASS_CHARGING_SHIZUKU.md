# Deteksi Mode Pengisian Bypass — via Shizuku

**Status:** Rencana — belum dikerjakan
**Tujuan:** Mendeteksi saat perangkat dalam mode pengisian bypass (baterai tidak menerima arus meski adaptor terhubung, umum di HP gaming) dan menampilkan indikatornya di modul Battery Bar / Battery Stats.

---

## 1. Gambaran

Tidak ada API Android publik untuk mendeteksi bypass charging. Deteksi dilakukan **heuristik** dengan menggabungkan 3 sumber:

1. **Broadcast** (`ACTION_BATTERY_CHANGED`) — gratis, tanpa izin: `plugged`, `status`, `level`.
2. **sysfs** (`/sys/class/power_supply/battery/...`) — `current_now`, `charge_type`, dll. Sebagian perangkat melindungi path ini (root-only).
3. **`dumpsys battery` / `dumpsys power`** — sumber otoritatif tapi butuh izin shell (`READ_BATTERY_STATS`), hanya bisa lewat **Shizuku** (UID shell) tanpa root.

### Heuristik utama
- `plugged != 0` (AC/USB) + `status == NOT_CHARGING` + `level < 100` → indikasi kuat bypass (normalnya `NOT_CHARGING` hanya saat baterai penuh).
- Level stagnan dalam beberapa interval saat adaptor terhubung.
- `current_now` ≈ 0 (atau positif kecil) saat status charging → baterai tidak menerima arus.
- Node sysfs vendor (misal `charge_type` bernilai "Bypass" di sebagian kernel gaming).

---

## 2. Fase Pengerjaan

### Fase A — Setup Dasar Shizuku (±1–2 jam)
1. Tambah dependency `dev.rikka.shizuku:shizuku-api` (+ provider) di `app/build.gradle`
2. Deklarasi `<provider>` Shizuku di `AndroidManifest.xml`
3. Helper koneksi: `Shizuku.pingBinder()`, `Shizuku.addBinderReceivedListener()`
4. Flow permintaan izin: `Shizuku.requestPermission(REQUEST_CODE)` + hasil via ActivityResult
5. UI/state handling: Shizuku tidak terpasang / tidak berjalan / izin ditolak

### Fase B — Eksekusi & Parsing Shell (±2–3 jam)
1. Helper jalankan perintah via `Shizuku.newProcess(String[])` (API 13+), baca stdout/stderr, timeout, error handling
2. Parser output `dumpsys battery` (field `status`, `level`, `technology`, dll)
3. Parser output `dumpsys power` (state charging/bypass jika ada)
4. Baca sysfs via `cat` untuk perangkat yang memproteksi path-nya (fallback ke baca langsung jika readable)

### Fase C — Logika Deteksi + Integrasi Modul (±2–3 jam)
1. Gabungkan 3 sumber dengan prioritas: broadcast (selalu jalan) → sysfs → dumpsys (saat Shizuku aktif)
2. Indikator bypass di modul **Battery Bar** / **Battery Stats**: field config baru, tampilan (misal label/ikon), update logic interval
3. Ikutkan dalam preset (`OverlayPreset` + `PositionController`/panel) jika relevan
4. Fallback: saat Shizuku nonaktif, deteksi tetap berjalan dengan heuristik broadcast saja

### Fase D — Testing & Dokumentasi (±1–2 jam)
1. Build + uji manual (butuh device yang benar-benar mendukung bypass + Shizuku terpasang)
2. Verifikasi akurasi di kondisi: normal charging, penuh, bypass, perangkat tanpa fitur bypass
3. Update CHANGELOG, README, PANDUAN, STRUKTUR + sinkron ke `app/src/main/assets/`

---

## 3. Estimasi Total

**±6–10 jam kerja aktif (≈1–1,5 hari)** — belum termasuk waktu testing perangkat dan build.

Catatan efisiensi: infra Fase A+B bersifat generik dan bisa dipakai bersama untuk **Shizuku FPS Reader** (`_schedule/SHIZUKU_FPS_READER.md`).

---

## 4. Komponen yang Diperkirakan

| File | Keterangan |
|------|------------|
| `utils/ShizukuHelper.java` (baru) | Koneksi, izin, eksekusi perintah shell |
| `utils/BypassDetector.java` (baru) | Logika gabungan deteksi bypass |
| `AndroidManifest.xml` (ubah) | Provider Shizuku |
| `app/build.gradle` (ubah) | Dependency Shizuku |
| Modul Battery Bar / Battery Stats (ubah) | Indikator bypass + config |
| Dokumen (ubah) | CHANGELOG, README, PANDUAN, STRUKTUR |

---

## 5. Risiko & Pertimbangan

- **Perangkat uji** — wajib ada device pendukung bypass (ROG, Xiaomi gaming, Lenovo Legion, dll) untuk verifikasi; tanpanya hasil tidak tuntas.
- **Shizuku wajib terpasang & diizinkan user** — tambah flow UX (dialog izin, panduan install Shizuku).
- **Variasi output `dumpsys`** antar vendor/versi Android — parser perlu toleran.
- **Privasi/keamanan** — jangan log output mentah berlebihan; batasi cakupan perintah.

---

## 6. Alternatif Ringan (Tanpa Shizuku)

- Deteksi hanya dari broadcast (`plugged` + `status` + `level`) + sysfs yang readable.
- Estimasi ±1–2 jam, akurasi cukup untuk mayoritas perangkat, tapi tidak bisa membaca `dumpsys` dan sysfs yang diproteksi.
- Disarankan sebagai tahap pertama jika ingin hasil cepat; Shizuku menyusul untuk akurasi penuh.

---

## 7. Status

- [ ] Fase A — Setup dasar Shizuku
- [ ] Fase B — Eksekusi & parsing shell
- [ ] Fase C — Logika deteksi + integrasi modul
- [ ] Fase D — Testing & dokumentasi
