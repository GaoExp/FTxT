# Desain — Riwayat Sesi Pengisian & Pengosongan (Tab Monitor)

**Status:** ✅ DESAIN FINAL (disepakati)
**Tanggal:** 2026-08-28 03:43 WITA
**Tujuan:** Merancang ulang tampilan tab Monitor pada panel Battery Info agar terbagi menjadi sub-tab, serta menambahkan riwayat sesi pengisian & pengosongan (dengan mAh) yang bisa dibaca di aplikasi — mengikuti pola desain tab History AccuBattery.

---

## 1. Latar Belakang & Masalah

Kondisi riwayat sesi pengisian saat ini (dari source code):

- Riwayat sesi pengisian ditampilkan di **kartu Kesehatan Baterai** (`BatteryHealthCardController.refreshSessionHistory()`), dibaca dari tabel `samples` mentah via `queryChargingSessions()` (`BatteryHistoryDb.java:315`).
- Hanya menampilkan: waktu mulai–selesai, durasi, delta %, jenis colokan.
- **TIDAK menampilkan:** mAh terisi, suhu min/max/avg, dan tidak memakai data estimasi sesungguhnya (tabel `sessions`).
- **Tidak ada** pencatatan/tampilan sesi **pengosongan** sama sekali.

Masalah yang ingin diselesaikan:
1. Menampilkan **mAh terisi** pada riwayat sesi pengisian.
2. Menambahkan **riwayat sesi pengosongan** (discharge) dengan mAh terpakai.

---

## 2. Desain Tampilan (Struktur Tab)

### 2.1 Struktur panel Battery Info

```
Panel Battery Info
├── Tab bawah:  Monitor | Overlay | Battery Strip
└── (di dalam tab Monitor) Tab atas:
    ├── Info & Grafik      ← metrik real-time + grafik riwayat (isi yang sekarang)
    ├── Kondisi Baterai    ← isi kartu Kesehatan Baterai dipindah ke sini
    └── Riwayat Sesi       ← BARU, mengikuti pola AccuBattery History
```

### 2.2 Isi tab "Riwayat Sesi" (pola AccuBattery History)

```
Riwayat Sesi
├── Filter periode (kapsul):  [ Daily ] [ Weekly ] [ Monthly ]
├── Grafik BATANG (bar chart) sesi
│     • sumbu X  = hari/tanggal (setiap batang = 1 hari)
│     • sumbu Y  = skala otomatis mengikuti data agregat periode
│     • ketuk batang → sorot hari tsb → periode berubah
│       (mis. ketuk batang X=25 → "Selasa, 25 Agustus 2026")
├── Teks rentang periode yang disorot  (mis. "23/08/26 to 29/08/26")
├── Kartu ringkasan periode:
│     🔵 Telah terisi           (total % terisi pada periode disorot)
│        Penggunaan Baterai     (total % terpakai pada periode disorot)
│        Pengikisan Baterai     (Siklus, mis. 16,3 siklus)
│     🟢 Efficiency             (%, + indikator daun/titik penuh–kosong)
└── Kapsul filter sesi:
      [ Pengisian ] [ Pengosongan ] [ Semua Sesi ]
      ↓ daftar sesi sesuai tombol aktif & sesuai PERIODE yang disorot
```

### 2.3 Perilaku kapsul sesi
- **Pengisian** → hanya daftar sesi pengisian pada periode yang disorot.
- **Pengosongan** → hanya daftar sesi pengosongan pada periode yang disorot.
- **Semua Sesi** → semua sesi (charge & discharge) sesuai urutan tercatat, dibatasi **periode yang disorot** (jika grafik menyorot satu hari, maka semua sesi hari itu; begitu juga weekly/monthly mengikuti rentang aktif).

### 2.4 Interaksi grafik batang
- Setiap **batang = satu hari (tanggal)**.
- **Mengetuk batang** → periode (highlight) berpindah ke tanggal tersebut; kartu ringkasan & daftar sesi ikut menyesuaikan.
- Filter Daily/Weekly/Monthly menentukan **rentang/agregasi** yang ditampilkan pada grafik.

Observasi dari AccuBattery (referensi):
- Skala Y contoh: Daily ≈ 400, Weekly ≈ 1800, Monthly ≈ 7000 → **otomatis** mengikuti nilai maks agregat, bukan hardcode.
- **Satuan sumbu X berdasarkan mode:**
  - **Daily** = per-hari/tanggal (KONFIRMASI FINAL — bukan per-jam)
  - **Weekly** = per-hari/tanggal (batang 30–34 = lima hari, rentang mis. "26/07/26 to 01/08/26")
  - **Monthly** = per-bulan (12 bulan terakhir, dari 9=Sep … 8=Agu; batang di 7=Jul & 8=Agu)
- Indikator efficiency = % + deretan indikator (titik/daun) berjumlah dinamis (mis. 3, 5, atau 6 buah) penuh/parsial/kosong.

---

## 3. Data yang Dibutuhkan & Sumber

Data mentah tersedia di tabel `samples` (`BatteryReading.Snapshot`): time, temp_c, percent, voltage_v, current_ma, power_w, **charge_mah** (dari charge counter /1000), cycle_count, status, plugged, technology.

Catatan penting:
- **`charge_mah` sering `-1`** bila device tidak menyediakan `CHARGE_COUNTER` (`BatteryReading.read()`, hanya set bila `c > 0`).
- `BatteryCapacityEstimator.finishSegment` sudah menghitung **`estimate` (kapasitas penuh ekstrapolasi)** dan menyimpannya ke tabel `sessions`, TAPI **tidak** menyimpan "mAh terisi sesi ini" (`accumulatedChargeMah` hilang, tidak disimpan).

→ Oleh karena itu perlu **menyimpan metadata sesi lengkap** (bukan rekonstruksi dari sampel mentah) agar mAh terisi/dipakai dan suhu min/max/avg rapi dan tahan restart.

---

## 4. Keputusan Final — Metrik mAh yang Ditampilkan ("Use it all")

Semua metrik mAh berikut digunakan & ditampilkan:

1. **mAh terisi/dipakai per sesi** — dihitung dua cara: dari `charge_mah` (counter) **dan** integral `current_ma` (dua sumber, dibandingkan).
2. **Estimasi kapasitas penuh** (`capacityMah` ekstrapolasi) — "kapasitas bila diisi 0→100%".
3. **Δ charge_mah aktual** — selisih `charge_mah` akhir–awal sesi (mAh aktual masuk, dari counter, tanpa ekstrapolasi).
4. **Kapasitas desain vs estimasi** — skor kesehatan (sudah ada di tab Kondisi Baterai).
5. **Efficiency discharge** — % mAh yang benar-benar terpakai vs kapasitas.

**Tempat perbandingan mAh tampil (ketiga lokasi):**
- **Kartu ringkasan periode** (atas) — mis. "Counter: X mAh · Integral: Y mAh".
- **Daftar sesi** (per baris) — dua nilai mAh bersebelahan.
- **Halaman detail sesi** (sesi bisa diklik) — perbandingan lengkap: counter vs integral vs estimasi.

---

## 5. Rencana Teknis (belum dieksekusi)

### A. Database (`BatteryHistoryDb`)
- Perluas tabel `sessions` agar menyimpan metadata sesi pengisian lengkap: `startTime`, `endTime`, `startPercent`, `endPercent`, `mAhTerisiCounter`, `mAhTerisiIntegral`, `deltaChargeMah`, `capacityMah`, `suhuMin`, `suhuMax`, `suhuAvg`, `layarOffDominant`.
- Tambah tabel baru `discharge_sessions` untuk pengosongan (kolom serupa: waktu, delta %, mAh terpakai counter & integral, kondisi layar, suhu, efficiency discharge).
- Naikkan `DB_VERSION` + `onUpgrade`.
- Tambah query untuk filter pengisian / pengosongan / semua, per rentang periode & agregasi bar chart.

### B. Pencatatan
- `BatteryCapacityEstimator`: simpan metadata segmen lengkap saat selesai (termasuk mAh counter & integral, Δ charge_mah, suhu).
- Tambah `DischargeTracker` (baru): merekam sesi discharge (mAh terpakai counter & integral, layar on/off, suhu, efficiency discharge).
- Hook di `BatteryMonitor` (titik masuk sudah ada).

### C. UI
- `panel_battery.xml`: tambah tab atas di dalam tab Monitor (Info & Grafik / Kondisi Baterai / Riwayat Sesi).
- `BatteryHealthCardController` → jadi controller "Kondisi Baterai".
- `BatterySessionHistoryController` (baru) → controller "Riwayat Sesi": filter periode kapsul, **bar chart**, kartu ringkasan, kapsul sesi, daftar.
- `SessionDetailActivity` (baru) → halaman detail sesi dengan perbandingan lengkap.
- Komponen **bar chart** baru (yang ada sekarang `BatteryChartView` = line chart).
- Pindah isi kartu Kesehatan agar tampil di tab "Kondisi Baterai".

---

## 6. Referensi (AccuBattery History — pola dasar, disesuaikan)

Pola dasar dari tab History AccuBattery yang **diikuti**:
- Filter periode atas: **Daily | Weekly | Monthly**.
- **Grafik batang** (bukan garis): sumbu Y skala otomatis, sumbu X = hari/tanggal (batang diketuk = pilih tanggal → periode berubah, mis. "Selasa, 25 Agustus 2026").
- **Kartu ringkasan agregat** per periode disorot: Telah terisi (%), Penggunaan Baterai (%), Pengikisan (siklus), Efficiency (%).

Yang **dibedakan / disederhanakan** (tidak plagiat penuh, sengaja):
- Efficiency = **persen saja**, tanpa ikon daun/titik.
- **Tanpa** baris kepadatan aktivitas di bawah grafik.
- Filter periode atas diganti **tombol kapsul**.
- Dua tombol aksi AccuBattery ("Tampilkan sesi" / "Show last 100") diganti **kapsul** Pengisian/Pengosongan/Semua.
- Tambah perbandingan metrik mAh (counter vs integral vs estimasi) di ringkasan, daftar sesi, dan halaman detail sesi.

---

## 7. Pertanyaan Tunda — Status Resolusi

1. **Sumber mAh** → **SELESAI**: pakai semua metrik (counter, integral, Δ charge, estimasi, skor, efficiency discharge). Perbandingan tampil di ringkasan, daftar sesi, dan detail sesi.
2. **Urutan "Semua Sesi"** → **SELESAI (OK)**: gabung charge & discharge, urut waktu, dengan tanda tipe per baris.
3. **Konfirmasi scope** → **SELESAI**: user membebaskan scope seluas yang diperlukan (termasuk perubahan struktur DB, bar chart baru, tab atas, halaman detail sesi).
4. **Indikator efficiency** → **SELESAI**: cukup persen saja, tanpa ikon daun.
5. **Baris kepadatan aktivitas** → **SELESAI**: tidak disertakan.
