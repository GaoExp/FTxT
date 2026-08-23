# Battery Monitor v4.88.0 — Kondisi Terkini & Rencana Aktif

> **Diperbarui:** 2026-08-23 19:47 WITA
> **Versi Target:** v4.88.0 (entry CHANGELOG berjalan, versionCode 185)
> **Status:** fitur inti & rework kartu tab Monitor TEREALISASI — §3 (ring gauge +
> collapse grafik) MENUNGGU EKSEKUSI

---

## 1. Kondisi Terkini (v4.88.0)

- Pemantauan baterai **selalu aktif** via `core/BatteryMonitorService` (foreground,
  notifikasi minimal prioritas rendah): auto-start saat app dibuka dan saat boot.
  Tidak ada kontrol mulai/hentikan/jeda/reset apa pun — semua kontrol manual yang pernah
  ada SUDAH DIHAPUS dan tidak boleh dikembalikan.
- Sampling dinamis hemat baterai: ±1 detik saat charging (WAJIB dipertahankan untuk
  presisi estimator), ±10 detik layar nyala idle, ±60 detik layar mati.
- Riwayat di SQLite `features/battery_stats/BatteryHistoryDb` (`SQLiteOpenHelper`
  framework, TANPA Room/kapt/KSP): tabel sampel metrik time-series + tabel sesi
  pengisian. Migrasi file JSON lama otomatis. Tanpa auto-trim pembuang data.
- **Satu sumber data**: `features/battery_stats/BatteryReading` — overlay Battery Info
  (`BatteryStatsModule`) dan tab Monitor sama-sama membaca dari sini. Preseden bug
  faktor ×1000 pada rumus daya → RUMUS PERHITUNGAN TIDAK BOLEH DIDUPLIKASI; satu
  rumus = satu tempat.
- Estimasi kapasitas & kesehatan ala AccuBattery
  (`BatteryCapacityEstimator`): median lintas sesi charging, prioritas sesi layar-mati
  (min. 3 sesi layar-mati, jika belum ada semua sesi dipakai); skor hanya muncul jika
  kapasitas desain terisi (input manual via ketuk kartu Kesehatan).
- Grafik riwayat digambar dari query DB per rentang di background thread
  (`BatteryChartView`, custom Canvas tanpa library).

### Batasan Android yang Mengikat

- Arus (`current_now`) tidak akurat/kosong di sebagian device → fallback berjenjang di
  `BatteryReading`; tampil "—" apa adanya.
- Cycle count API 34+/vendor tertentu → sembunyikan baris metrik, bukan tampil 0.
- Kapasitas desain tidak tersedia via API publik → input manual user.

---

## 2. Tab Monitor Saat Ini (hasil rework kartu)

3 kartu + baris tombol:

```
[Kartu 1] Metrik Real-Time   — header kanan: badge kondisi suhu (batMonitorConditionBadge)
                               grid monospace 2 kolom (batMonitorMetricsText1/2):
                               % Level | Suhu / Voltase / Daya ... Kapasitas |
                               Cycle Count | Teknologi
                               bawah grid: status pengisian + sumber
                               (batMonitorStatusText)
[Kartu 2] Grafik Riwayat     — header + label rentang aktif (batChartRangeLabel);
                               RadioGroup 5 Menit–24 Jam (batChartRangeGroup);
                               chart Suhu/Persentase/Daya @140dp dengan sub-header
                               mini beraksen warna, dipisah divider
[Kartu 3] Kesehatan Baterai  — estimasi, skor, input kapasitas desain via ketuk;
                               tidak berubah
[Tombol ] Salin | Simpan Snapshot
```

ID yang sudah dihapus (jangan direferensikan lagi): `batMonitorPercentText`,
`batMonitorLevelBar`, `batMonitorChargeText`; warna `bat_monitor_bar_track` sudah
bersih dari kedua colors.xml. ID lain semuanya masih hidup dan dipakai controller.

---

## 3. RENCANA AKTIF — Ring Gauge Melingkar & Collapse Grafik Riwayat (BELUM EKSEKUSI)

> Permintaan user 2026-08-23: elemen visual level baterai kembali dalam bentuk
> **ring gauge melingkar** sebagai bagian kartu Metrik Real-Time (bukan kartu baru),
> menggantikan konsep strip/bar panjang eks-kartu Level Baterai.

### 3.1 Keputusan User — FINAL

1. **Ring gauge DI KIRI, grid metrik DI KANAN** — layout horizontal di dalam kartu
   Metrik Real-Time.
2. **Isi di dalam lingkaran** (bertumpuk, atas → bawah):
   - kapasitas aktual, contoh `5000 mAh`
   - level `%` besar
   - status singkat: `Charging•AC` / `Charging•USB` / `Charging•Wireless` / `Discharging`
3. **Warna arc mengikuti gradien HUE Battery Strip** (`SCHEME_HUE`) — bukan threshold
   statis hijau/kuning/merah.
4. **Grid kanan dikurangi**: baris "% Level", "Kapasitas", dan blok
   `batMonitorStatusText` DIBUANG (semua sudah tampil di ring).
   Sisa grid: **Suhu, Voltase, Arus, Daya, Cycle Count, Teknologi**.
5. **Chevron collapse pada kartu Grafik Riwayat** — klik header menyembunyikan/
   menampilkan isi (RadioGroup rentang + 3 chart). State RUNTIME SAJA (tidak ke prefs);
   panel dibuka = grafik terbuka.

### 3.2 Detail Teknis

**A. Custom view ring — 🗒️ `features/battery_stats/BatteryRingView.java` (baru)**

- Custom view Canvas tanpa library (pola seperti `BatteryChartView`): track lingkaran
  abu + arc progress rounded-cap mulai atas (-90°), sweep proporsional level.
- Ukuran tetap ±130dp agar layout horizontal stabil; padding track cukup lega untuk
  3 baris teks.
- Hierarki teks tengah: level ±22sp bold, kapasitas ±11sp, status ±10sp (boleh wrap).
- Setter (`setLevel`, `setCapacityMah`, `setStatus`) → `invalidate()`; dipanggil dari
  `BatteryPanelController.updateMonitorInfo()` memakai `BatteryReading.Snapshot`
  yang sudah ada — TIDAK membaca sensor sendiri.

**B. Satu sumber rumus warna hue — WAJIB**

Rumus ada di `features/battery_bar/BatteryBarView.java` (blok `SCHEME_HUE`,
±baris 274–288):

```
percent <= 20 : hue = 1, sat = 0.7
percent <= 50 : t = (percent - 21f) / 29f ; hue = 2 + 98*t ; sat = 0.7
else          : t = (percent - 51f) / 49f ; hue = 102 + 158*t ; sat = 0.71 + 0.29*t
warna = Color.HSVToColor(255, {hue, sat, 1f})
```

Tidak boleh diduplikasi (presenden bug ×1000) → ekstrak helper statis tunggal
(mis. `hueColor(float percent)` di `BatteryReading` atau util kecil di `shared/`),
dipakai oleh `BatteryBarView` DAN `BatteryRingView`. Perilaku Battery Strip harus
identik sedikit pun.

**C. Status format**

- Mengisi: `Charging•<sumber>` dari `BatteryReading.Snapshot` (AC / USB / Wireless).
- Tidak mengisi: `Discharging`.
- Full: ikuti helper status existing di `BatteryReading` (mis. `Full` tanpa sumber) —
  jangan bikin mapping baru.

**D. Layout kartu Metrik Real-Time (`panel_battery.xml`)**

```
[Kartu Metrik Real-Time]
 header: judul ................ [badge kondisi suhu] (tetap)
 body horizontal:
   [BatteryRingView]     |  grid monospace 2 kolom:
   ring gauge            |  Suhu    | Voltase
                         |  Arus    | Daya
                         |  Cycle Count | Teknologi
```

**E. Chevron kartu Grafik Riwayat**

- Chevron di kanan header, toggle visibilitas isi kartu (RadioGroup + 3 chart + divider).
- Animasi rotate chevron opsional (±150ms) — skip jika menambah kerumitan.

### 3.3 File Terdampak

- 🗒️ `features/battery_stats/BatteryRingView.java` — custom view ring (baru)
- ✏️ `panel_battery.xml` — body kartu Metrik horizontal (ring + grid);
  `batMonitorStatusText` dihapus; chevron + container collapsible di kartu Grafik
- ✏️ `ui/BatteryPanelController.java` — binding ring + toggle chevron; builder teks
  grid dikurangi (% Level & Kapasitas keluar); pengisian `batMonitorStatusText` dihapus
- ✏️ `features/battery_bar/BatteryBarView.java` — hanya pemanggilan helper hue
  (refactor netral perilaku)
- ✏️ `values/colors.xml` & `values-night/colors.xml` — warna track ring abu bila perlu
  resource; audit warna tak terpakai
- ✏️ `CHANGELOG.md` — masuk entry 4.88.0 ONGOING yang sama (versionCode 185), poin
  direvisi ke hasil akhir

---

## 4. Versioning

- Seluruh pekerjaan monitor baterai masuk **satu entry v4.88.0** (versionCode 185)
  sampai entry tersebut di-commit & push; setelah push barulah entry versi baru dibuat
  sesuai alur AGENTS.md.

---

## 5. Risiko Ringkas

- Interval 1 detik SAAT CHARGING wajib dipertahankan — else estimasi kapasitas melenceng.
- Database tumbuh tapi per baris sangat kecil (ratusan KB/hari) — aman tanpa trim.
- Overlay vs tab Monitor harus menampilkan angka identik persis (sumber tunggal).
- Teks dalam lingkaran sempit — uji dengan status terpanjang `Charging•Wireless`;
  diameter efektif = size − strokeWidth×2.
- Scope §3: jangan sentuh polling/database/estimator/tab Overlay; `BatteryBarView`
  hanya disentuh untuk pemanggilan helper.
- Regresi binding: hanya `batMonitorStatusText` yang hilang — pastikan seluruh
  referensi controller ikut dibersihkan.
