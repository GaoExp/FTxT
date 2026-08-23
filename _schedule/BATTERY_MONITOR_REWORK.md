# Rencana Rework Monitor Baterai — Full-Aktif, Database & Satu Sumber Data

> **Tanggal:** 2026-08-22 (direvisi 2026-08-23)
> **Versi Target:** v4.89.0+ (setelah v4.88.0 rilis)
> **Status:** Rencana (belum eksekusi)
> **Asal diskusi:** sesi pengerjaan Tahap 3 BATTERY_MONITOR.md (estimasi kapasitas), hasil uji device user, + koreksi arah desain oleh user (2026-08-23)

---

## 1. Latar Belakang — Masalah yang Sudah Terjadi

**a) Dua jalur pembacaan baterai yang saling lepas**

- **Overlay Battery Info** (`features/battery_stats/BatteryStatsModule.java`) — kode lama, membaca sendiri (battery intent + `BATTERY_PROPERTY_CURRENT_NOW` + sysfs), update mengikuti interval overlay (0,2–10 detik).
- **Tab Monitor** (`features/battery_stats/BatteryMonitor.java`) — komponen baru v4.88.0, polling sendiri tiap 1 detik, punya buffer riwayat sendiri (20 snapshot export + 3600 titik grafik).

Konsekuensi nyata: rumus daya ditulis dua kali dan salah satunya meleset.
`BatteryStatsModule` menghitung `(mV/1000) × (mA/1000)` = volt×ampere (benar, tampil 8.0W),
sedangkan `BatteryMonitor` menghitung `voltageV × absMa / 1_000_000` (salah faktor 1000,
tampil 0.008W). Device user sama-sama sumber data tapi angkanya 8.0W vs ±0.1W.
Sudah diperbaiki di BatteryMonitor (`/1000d`) pada 2026-08-22, tapi akar masalahnya
(logika diduplikasi di dua file) masih ada.

**b) Monitor tidak otomatis aktif**

Saat ini monitor baru jalan setelah user menekan tombol Mulai Pemantauan (atau menyalakan
switch Latar Belakang), dan berhenti saat panel ditutup (`onPanelHidden()` → `stopManualMonitor()`).
Akibatnya grafik riwayat & estimasi kapasitas cuma punya data saat user ingat menyalakannya —
padahal tujuan fitur ala AccuBattery/Battery Guru adalah mencatat otomatis tanpa diurus.

Kenapa monitor tidak bisa sekadar "menumpang" loop update overlay:
1. Service overlay bisa dimatikan kapan saja (Kill Service / tidak pernah dinyalakan) → pencatatan ikut mati, riwayat bolong.
2. Interval beda kebutuhan: overlay bebas 0,2–10 detik (diatur user), pencatatan butuh sampling konsisten agar sumbu waktu akurat.

**c) Desain v4.88.0 melenceng dari arahan user**

Implementasi agent menambahkan kontrol-kontrol yang tidak pernah diminta:
tombol Mulai/Hentikan Pemantauan, switch Latar Belakang, tombol Jeda & Reset Grafik,
dan Reset Data Estimasi — padahal konsep user: monitor **selalu aktif**, tidak ada acara
menyalakan/mematikan/jeda/reset. Selain itu:
- Buffer grafik = memori ±1 jam (3600 titik) → hilang begitu aplikasi ditutup, bukan riwayat sungguhan.
- Persistensi estimasi kapasitas = file JSON → tidak layak menampung riwayat sesi yang terus bertambah.

---

## 2. Keputusan User — FINAL (2026-08-23)

Keputusan ini MENGGANTIKAN seluruh keputusan tanggal 2026-08-22 pada versi dokumen sebelumnya.

1. **Monitor FULL AKTIF** — merekam otomatis tanpa henti: overlay dipakai atau tidak,
   panel dibuka atau tidak, aplikasi ditutup sekalipun. Tidak ada kondisi "berhenti".
2. **Semua kontrol manual DIHAPUS:**
   - Tombol Mulai/Hentikan Pemantauan
   - Switch Latar Belakang (+ prefs `bat_bg_monitor`, restore di BootReceiver, cek di
     `FloatingService.isAnyModuleActive()` / `MainActivity` / `NotificationHelper`)
   - Tombol Jeda & Reset Grafik
   - Tombol Reset Data Estimasi
   - Badge status Aktif/Berhenti (tidak relevan lagi karena tidak ada kondisi berhenti)
3. **Riwayat disimpan di DATABASE SQLite lokal** (bukan file JSON, bukan buffer memori):
   - Implementasi pakai `SQLiteOpenHelper` bawaan framework — **TANPA Room/kapt/KSP**
     (stabil di toolchain build AndroidIDE aarch64, minim dependency, konsisten prinsip project).
   - Tabel **sampel metrik** (time-series: waktu, suhu, persen, voltase, arus, daya, status charging, dll)
     → sumber kartu grafik.
   - Tabel **sesi pengisian** (mulai, selesai, durasi, ΔmAh, Δ%, estimasi kapasitas sesi, fraksi layar mati)
     → bahan estimator kapasitas/kesehatan.
   - Persistensi JSON `BatteryCapacityEstimator` dimigrasi ke database (import sekali) lalu kode JSON dihapus.
   - **Tanpa auto-trim pembuang riwayat** — biarkan data hidup; ukuran per baris sangat kecil.
4. **Kartu grafik TETAP ADA** (permintaan user), tapi sumber datanya **database** —
   riwayat lama tetap terlihat, bukan cuma sejak aplikasi nyala. Buffer memori 3600 titik dihapus.
   Pemilih rentang diperluas: **5 Menit / 15 Menit / 1 Jam / 6 Jam / 24 Jam**.
5. **Estimasi kapasitas & skor kesehatan tetap** (median lintas sesi, prioritas sesi layar-mati);
   input manual kapasitas desain tetap.
6. **Sampling dinamis hemat baterai** dipertahankan sebagai keputusan teknis internal
   (konstanta di satu tempat, mudah diubah): charging = 1 detik (presisi estimator),
   layar nyala idle ≈10 detik, layar mati ≈60 detik. Angka final ditetapkan saat implementasi.

---

## 3. Tahap 1 — Satu Sumber Data (Single Source of Truth)

Masih valid, dikerjakan lebih dulu.

Ekstrak seluruh logika baca metrik baterai dari `BatteryMonitor.readSnapshot()`
(battery intent: suhu/level/scale/voltase/status/plugged/teknologi, reflection
`EXTRA_CURRENT_NOW`, `BATTERY_PROPERTY_CURRENT_NOW`, sysfs fallback, konversi satuan,
perhitungan powerW) menjadi util statis terpisah.

**Konsep penting:** yang disatukan adalah **kode membaca**, bukan siklus hidup.
Overlay dan monitor TETAP dua komponen independen; mereka hanya sama-sama memanggil
pembaca tunggal. Dengan begitu:
- Bug faktor 1000 seperti tadi mustahil terulang (rumus cuma ada di satu tempat).
- Overlay tetap bisa update walau monitoring dimatikan.
- Monitor tetap bisa mencatat walau overlay mati.

**File yang direncanakan:**
- 🗒️ `features/battery_stats/BatteryReading.java` (nama bebas) — util statis:
  model snapshot + `read(Context)` + semua konversi satuan + helper status/kondisi.
- ✏️ `features/battery_stats/BatteryMonitor.java` — buang `readSnapshot()` internal,
  panggil pembaca tunggal.
- ✏️ `features/battery_stats/BatteryStatsModule.java` — ganti blok baca mandiri
  (registerReceiver + property + sysfs) dengan pembaca tunggal.
- ✏️ `shared/ui/BatteryChartView.java` dsb. — hanya jika nama field model berubah.

**Catatan migrasi:** cari semua pemakaian `BatteryMonitor.Snapshot` dan perbarui referensinya
secara menyeluruh.

---

## 4. Tahap 2 — Database Riwayat + Monitor Full-Aktif

### 4.1 Database riwayat (inti rework)

- 🗒️ Kelas helper DB baru (mis. `BatteryHistoryDb.java`) — `SQLiteOpenHelper`,
  2 tabel: sampel metrik + sesi pengisian. Insert batch ringan per polling;
  query per rentang waktu untuk grafik; query agregat untuk estimator.
- ♻️ `BatteryCapacityEstimator.java` — sumber data pindah dari JSON ke tabel sesi;
  migrasi: file JSON lama (bila ada) di-import sekali ke DB lalu dihapus;
  seluruh kode persistensi JSON dibuang.
- ♻️ `BatteryMonitor.java` — buffer memori 20 snapshot export & 3600 titik grafik
  dihapus; tiap sampel polling langsung masuk DB; finalisasi segmen pengisian menulis
  baris sesi ke DB. Export/copy snapshot tab Monitor membaca dari DB.

### 4.2 Monitor full-aktif

- ♻️ `BatteryMonitor.java` — auto-start tanpa kondisi: saat aplikasi dibuka,
  saat boot (BootReceiver), dan berjalan terus di belakang tanpa bergantung panel/overlay.
  Karena harus hidup setelah aplikasi ditutup → foreground service ringan sendiri
  (notifikasi minimal/prioritas rendah; detail finalisasi saat implementasi).
- ♻️ `core/FloatingService.java` — lifecycle monitor dilepas dari service overlay
  (`start/stop BatteryMonitor`, `setBackgroundBatteryMonitorEnabled()`,
  cek `backgroundMonitor` di `isAnyModuleActive()` dibersihkan).
- ♻️ `MainActivity.java`, `BootReceiver.java`, `NotificationHelper.java` —
  seluruh jejak pref `bat_bg_monitor` & logika backgroundMonitor opsional dibersihkan.
- ♻️ `BatteryStatsConfig.java` — field `backgroundMonitor` dihapus.

### 4.3 Pembersihan UI tab Monitor

- ✏️ `res/layout/panel_battery.xml` — hapus: tombol Mulai/Hentikan, switch Latar Belakang,
  badge status monitor, tombol Jeda & Reset grafik, tombol Reset Data Estimasi.
  Tetap: kartu level, kartu metrik real-time, kartu status pengisian, kartu grafik
  (kontrol tinggal pemilih rentang), kartu kesehatan (tinggal hasil + input kapasitas desain).
- ✏️ `ui/BatteryPanelController.java` — buang listener/toggle/badge terkait;
  polling UI cukup membaca state terkini; grafik digambar dari query DB per rentang.
- ✏️ `values/colors.xml` & `values-night/colors.xml` — warna badge status monitor
  yang tak terpakai dibersihkan.
- ✏️ `strings.xml` — string kontrol yang tak terpakai dibersihkan.

### 4.4 Grafik dari database

- ♻️ `shared/ui/BatteryChartView.java` — render tetap Canvas tanpa library eksternal;
  sumber data jadi deret dari DB; rentang 5 Menit–24 Jam; label sumbu waktu menyesuaikan
  rentang panjang (jam/tanggal).

---

## 5. Urutan Kerja

1. Tahap 1 (penyatuan sumber data) — refactor murni, perilaku tidak berubah.
2. Tahap 2 (database + full-aktif + pembersihan UI) — satu tarikan karena saling terkait.

---

## 6. Catatan Versioning

| Tahap | Isi | Alasan versi |
|-------|-----|--------------|
| 1 | Penyatuan sumber data | 🔧 refactor internal |
| 2 | Database riwayat, monitor full-aktif, kontrol manual dihapus | ✨/♻️/🚮 fitur — minor+1 |

Disarankan dua commit terpisah agar mudah roll-back. Versi final mengikuti isi saat commit
(diskusikan saat itu; v4.88.0 masih ONGOING — rework ini masuk entry berjalan atau entry baru
setelah 4.88.0 rilis).

---

## 7. Risiko & Mitigasi

- **Polling selalu-on boros baterai** → sampling dinamis (§2.6): 1 detik hanya saat charging
  (justru saat charging hemat energi tidak krusial); idle & layar mati interval longgar.
- **Ukuran database tumbuh** → per baris sangat kecil; contoh kasar: 24 jam pemakaian normal
  (mayoritas layar mati @60 detik + charging @1 detik) ≈ ribuan baris/hari ≈ ratusan KB.
  SQLite sanggup jauh melampaui itu. Tidak perlu trim.
- **Device dengan `current_now` tidak akurat/kosong** → fallback berjenjang di pembaca
  tunggal; nilai "—" ditampilkan apa adanya.
- **Regresi overlay** setelah ganti sumber data → uji paralel: angka overlay vs tab Monitor
  harus identik persis (itulah tujuan akhirnya).
- **Foreground service tambahan** → notifikasi minimal agar tidak mengganggu; cek dampak
  battery optimization di device uji user.
- **Estimasi kapasitas** bergantung sampel charging rapat → interval dinamis WAJIB tetap
  1 detik SAAT charging, else segmen Δ% kasar dan estimasi melenceng.
