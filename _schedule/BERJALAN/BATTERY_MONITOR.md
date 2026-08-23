# Battery Monitor — Kondisi Terkini

> **Diperbarui:** 2026-08-24 01:07 WITA
> **Versi Target:** entry CHANGELOG berjalan `[4.88.1]` (versionCode 186) — pekerjaan
> lanjutan monitor baterai masuk ke sini sampai di-commit & push
> **Status:** DOKUMEN TETAP DI `BERJALAN` sampai fitur Battery Monitor dirilis dan
> dinyatakan selesai. v4.88.0 (fondasi monitoring) sudah PUSH (commit `fade934`);
> iterasi berjalan (v4.88.1): slider rentang, grid 2×2 grafik, dan pemecahan controller.

---

## 1. Kondisi Terkini (v4.88.1)

- Pemantauan baterai **selalu aktif** via `core/BatteryMonitorService` (foreground,
  notifikasi minimal prioritas rendah): auto-start saat app dibuka dan saat boot.
  Tidak ada kontrol mulai/hentikan/jeda/reset apa pun — kontrol manual SUDAH DIHAPUS
  dan tidak boleh dikembalikan.
- Sampling dinamis hemat baterai: ±1 detik saat charging (WAJIB dipertahankan untuk
  presisi estimator), ±5 detik layar nyala idle, ±30 detik layar mati.
- Riwayat di SQLite `features/battery_stats/BatteryHistoryDb` (`SQLiteOpenHelper`
  framework, TANPA Room/kapt/KSP): tabel sampel metrik time-series + tabel sesi
  pengisian. Migrasi file JSON lama otomatis. Tanpa auto-trim pembuang data.
- **Satu sumber data**: `features/battery_stats/BatteryReading` — overlay Battery Info
  (`BatteryStatsModule`) dan tab Monitor sama-sama membaca dari sini. Preseden bug
  faktor ×1000 pada rumus daya → RUMUS PERHITUNGAN TIDAK BOLEH DIDUPLIKASI; rumus
  gradien hue juga satu sumber di `shared/color/BatteryColors.hueColor()` (dipakai
  BatteryBarView & BatteryRingView).
- Estimasi kapasitas & kesehatan ala AccuBattery (`BatteryCapacityEstimator`):
  median lintas sesi charging, prioritas sesi layar-mati; skor hanya muncul jika
  kapasitas desain terisi (input manual via ketuk).
- Grafik riwayat digambar dari query DB per rentang di background thread
  (`BatteryChartView`, custom Canvas tanpa library). Hasil query di-resample ke grid
  waktu seragam (`BatteryHistoryDb.resampleUniform`, interpolasi linear) agar kepadatan
  nilai tengah; skala Y Suhu kini MAKSIMAL TETAP 50°C (melebar otomatis hanya bila
  suhu data melebihi 50°) dengan batas bawah otomatis mengikuti data.
- **Struktur kode tab Monitor sudah dipecah** (v4.88.1) — `ui/BatteryPanelController`
  hanya mengurus tab Overlay (±560 baris); isi tab Monitor pindah utuh ke:
  - `ui/BatteryMonitorTabController` — ring gauge, metrik real-time, badge kondisi,
    polling 1 detik (hanya saat tab tampil), tombol Salin/Simpan Snapshot;
  - `ui/BatteryChartHistoryController` — kartu Grafik Riwayat + slider rentang;
  - `ui/BatteryHealthCardController` — kartu Kesehatan Baterai + dialog kapasitas desain;
  - `ui/BatterySnapshotExporter` — teks salinan clipboard & ekspor Download.
  `BatteryPanelFragment` tidak berubah (masih fasad ke `BatteryPanelController`).

### Batasan Android yang Mengikat

- Arus (`current_now`) tidak akurat/kosong di sebagian device → grafik/nilai arus bisa
  kosong atau datar; fallback berjenjang di `BatteryReading`, tampil "—" apa adanya.
- Cycle count API 34+/vendor tertentu → sembunyikan baris metrik, bukan tampil 0.
- Kapasitas desain tidak tersedia via API publik → input manual user.

---

## 2. Tab Monitor Saat Ini (hasil seluruh iterasi)

```
[Kartu Metrik Real-Time]
 header: judul ................ [badge kondisi suhu]
 body horizontal: [ring gauge 130dp] [grafik Persentase weight=1, 130dp]
   - ring: arc gradien hue sesuai level; di dalamnya kapasitas mAh,
     level % besar, status Charging•AC/USB/Wireless / Full / Discharging
 bawah: grid monospace 2 kolom:
   Suhu | Voltase / Arus | Daya / Cycle Count | Teknologi

[Kartu Grafik Riwayat]
 header: judul ... label rentang aktif (tanpa chevron — konten SELALU tampil)
 baris slider full width:
   deretan pembatas + label singkat di ATAS slider: 2m 5m 10m 15m 30m 1j 3j 6j 12j 24j
   langkah aktif disorot (tebal + warna header)
 chart grid 2×2 @130dp tanpa divider:
   baris 1: Suhu | Daya   —   baris 2: Tegangan | Arus
   tiap sel punya sub-header aksen warna sendiri

[Kartu Kesehatan Baterai]
 teks hasil estimasi/skor/sesi/keyakinan/status pengumpulan;
 input kapasitas desain via ketuk;
 baris tombol Salin | Simpan Snapshot (pindahan dari bawah tab)
```

- Label statis "Pemantauan real-time baterai perangkat" di atas tab DIHAPUS.
- Tombol Salin & Simpan Snapshot berada di dalam kartu Kesehatan Baterai:
  - Salin = kondisi saat ini (level, kapasitas, status) + grid metrik +
    catatan kesehatan (estimasi, skor, sesi, keyakinan).
  - Simpan Snapshot = file teks di Download berisi catatan kesehatan +
    20 sampel terakhir dari database (waktu + semua metrik per baris).
- Grafik Persentase tinggal SATU instance dan berada di kartu Metrik Real-Time
  (ID tetap `batChartPercentView`, ikut rentang global).
- **Pemilih rentang = slider** (v4.88.1): 10 langkah tetap — 2 Menit, 5 Menit,
  10 Menit, 15 Menit, 30 Menit, 1 Jam, 3 Jam, 6 Jam, 12 Jam, 24 Jam (default
  5 Menit). Tanpa label statis "Rentang" — slider memanjang penuh; di atasnya
  ada deretan pembatas berlabel singkat (2m/5m/10m/…/24j) dan langkah aktif
  disorot. Geser = pratinjau label header + sorot langkah; query DB + render
  baru jalan saat jari dilepas.
- **Chevron pelipat kartu Grafik Riwayat DIHAPUS** (v4.88.1) — grid 2×2 tidak
  lagi memakan ruang, isi kartu selalu tampil; ID `batChartHeader`,
  `batChartCollapseToggle`, `batChartContent` sudah hilang permanen.
- Warna chart: suhu merah, persentase hijau, daya amber, tegangan indigo
  (`bat_chart_voltage`), arus teal (`bat_chart_current`) — lengkap varian night.

---

## 3. Keputusan Desain yang Mengikat

- Ala Battery Guru/AccuBattery; tanpa alarm charging.
- Ring gauge menggantikan konsep strip/bar level; arc mengikuti gradien HUE Battery
  Strip (`SCHEME_HUE`) — bukan threshold statis.
- Monitor full-aktif tanpa kontrol manual; grafik bersumber DB bukan buffer memori.
- Rumus tidak boleh duplikasi (preseden bug ×1000); helper hue tunggal di
  `shared/color/BatteryColors`.
- Rentang grafik pakai slider opsi diskrit tetap (bukan slider kontinu/non-linear)
  dengan pembatas berlabel di atasnya; query dieksekusi saat lepas jari agar tidak
  menumpuk puluhan query DB per gesekan.
- Skala Y grafik Suhu: atas tetap 50°C (ambang panas baterai), bawah otomatis.
- Satu panel = banyak file kecil, bukan satu file ribuan baris: controller per
  kartu/tab dipisah; fragment tetap fasad tunggal.

---

## 4. Versioning

- Fondasi monitoring masuk **entry v4.88.0** (versionCode 185) — sudah commit & tag
  `v4.88.0` (commit `fade934`, status PUSH).
- Iterasi berjalan masuk **entry `[4.88.1]`** (versionCode 186, status ONGOING):
  slider rentang, grid 2×2 grafik, pemecahan controller. Entry ini yang akan
  di-commit/push berikutnya sesuai alur AGENTS.md.
- Dokumen ini **tetap di `_schedule/BERJALAN/`** sampai fitur Battery Monitor rilis
  dan dinyatakan selesai — baru pindah ke `_schedule/SELESAI/`.

---

## 5. Risiko Ringkas

- Interval 1 detik SAAT CHARGING wajib dipertahankan — else estimasi kapasitas melenceng.
- Database tumbuh tapi per baris sangat kecil (ratusan KB/hari) — aman tanpa trim.
- Overlay vs tab Monitor harus menampilkan angka identik persis (sumber tunggal).
- Arus device-dependent (kernel): grafik Arus bisa datar/kosong — perilaku diketahui,
  bukan bug.
- Regresi binding: ID grid/badge/chart dipertahankan lintas refactor pemecahan file
  (semua ID lama masih ada di layout); yang sudah hilang permanen:
  `batMonitorPercentText`, `batMonitorLevelBar`, `batMonitorChargeText`,
  `batMonitorStatusText`.
- Label waktu sumbu-X rentang ≥6 jam masih format panjang `dd/MM HH:mm` — berpotensi
  sempit di sel grid 2×2; pantau saat review di aplikasi.
