# Battery Monitor v4.88.0 — Kondisi Terkini

> **Diperbarui:** 2026-08-23 21:19 WITA
> **Versi Target:** v4.88.0 (entry CHANGELOG berjalan, versionCode 185)
> **Status:** seluruh rencana TEREALISASI di kode (belum commit) — dokumen pindah ke
> `SELESAI` setelah v4.88.0 rilis & push

---

## 1. Kondisi Terkini (v4.88.0)

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
  garis konsisten lintas rezim sampling; skala Y Suhu punya rentang minimum ±2°C dari
  nilai tengah.

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
 header: judul ... label rentang aktif [chevron ▾/▸ collapse]
 RadioGroup rentang: 5 Menit–24 Jam
 chart Suhu / Daya / Tegangan / Arus @140dp, sub-header beraksen warna,
 dipisah divider; state collapse RUNTIME SAJA

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
  (ID tetap `batChartPercentView`, ikut RadioGroup rentang global).
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

---

## 4. Versioning

- Seluruh pekerjaan monitor baterai masuk **satu entry v4.88.0** (versionCode 185)
  sampai entry tersebut di-commit & push; setelah push barulah entry versi baru dibuat
  sesuai alur AGENTS.md.
- Setelah push: dokumen ini pindah ke `_schedule/SELESAI/`.

---

## 5. Risiko Ringkas

- Interval 1 detik SAAT CHARGING wajib dipertahankan — else estimasi kapasitas melenceng.
- Database tumbuh tapi per baris sangat kecil (ratusan KB/hari) — aman tanpa trim.
- Overlay vs tab Monitor harus menampilkan angka identik persis (sumber tunggal).
- Arus device-dependent (kernel): grafik Arus bisa datar/kosong — perilaku diketahui,
  bukan bug.
- Regresi binding: ID grid/badge/chart dipertahankan; yang sudah hilang permanen:
  `batMonitorPercentText`, `batMonitorLevelBar`, `batMonitorChargeText`,
  `batMonitorStatusText`.
