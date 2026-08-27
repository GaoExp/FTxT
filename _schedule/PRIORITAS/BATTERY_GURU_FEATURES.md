# Fitur Tambahan dari Battery Guru & AccuBattery

**Status:** Sebagian selesai (3 fitur di-[?]-tandai — kemungkinan ada masalah yang perlu dicek, 3 belum)
**Tujuan:** Menambahkan beberapa fitur dari Battery Guru & AccuBattery ke FTxT: riwayat sesi pengisian, suhu min/max/avg per sesi, estimasi waktu pengisian/pengosongan, Activity Timeline Bar, dan Battery Usage by App.

---

## 1. Fitur yang Diusulkan

### 1.1 Riwayat Sesi Pengisian
Menampilkan daftar sesi pengisian sebelumnya dengan informasi:
- Waktu mulai & selesai
- Durasi pengisian
- Delta persen (misal 20% → 80%)
- Estimasi kapasitas yang terisi (mAh)
- Apakah mayoritas layar mati

**Manfaat:** Pengguna bisa melihat pola pengisian dan membandingkan sesi lama vs baru.

### 1.2 Suhu Min/Max/Avg per Sesi
Menampilkan statistik suhu selama sesi pengisian:
- Suhu minimum
- Suhu maksimum
- Suhu rata-rata

**Manfaat:** Pengguna tahu apakah baterai terlalu panas selama pengisian (berpengaruh ke kesehatan jangka panjang).

### 1.3 Estimasi Waktu Pengisian & Pengosongan
Menampilkan estimasi sisa waktu:
- Saat charging: "Est. penuh: ±45 menit"
- Saat discharging: "Est. habis: ±3 jam 20 menit"

**Manfaat:** Pengguna tahu kapan baterai akan penuh atau habis tanpa melihat persentase saja.

### 1.4 Activity Timeline Bar
Bar horizontal yang sejajar dengan grafik persentase baterai, menampilkan status perangkat seiring waktu:
- **Layar aktif** (screen on) — warna cerah
- **Layar mati** (screen off) — warna gelap
- **Status pengisian** (charging) — warna hijau

**Manfaat:** Pengguna bisa lihat hubungan antara aktivitas perangkat dengan drainase baterai. Contoh: "Baterai turun dari 80% ke 50% karena layar aktif selama 2 jam."

### 1.5 Battery Usage by App
Menampilkan penggunaan baterai per aplikasi:
- Persentase konsumsi baterai per aplikasi
- Konsumsi foreground (saat aplikasi aktif)
- Konsumsi background (saat aplikasi di latar belakang)
- Waktu CPU per aplikasi

**Manfaat:** Pengguna tahu aplikasi mana yang paling boros baterai, baik saat digunakan maupun di latar belakang.

### 1.6 Backup & Restore Database
Menyimpan dan memulihkan database riwayat baterai:
- **Export:** Simpan database ke folder `Documents/FTxT/` sebagai file `.db`
- **Import:** Baca database dari folder `Documents/FTxT/` dan gunakan kembali
- Format file: `ftxt_battery_history_YYYYMMDD_HHmmss.db`

**Manfaat:** Jika aplikasi di-uninstall dan diinstall ulang, riwayat baterai tidak hilang. Pengguna bisa restore data dari backup.

---

## 2. Penempatan di UI

### 2.6 Backup & Restore Database
**Lokasi:** Tombol "Backup" dan "Restore" di panel baterai (sejajar dengan tombol Salin, Simpan, Reset)

**Tampilan:**
```
[panel_battery.xml]
  [Salin] [Simpan] [Reset] [Backup] [Restore]
```

**Backup:**
- Ketuk "Backup" → muncul dialog konfirmasi
- Database disimpan ke `/storage/emulated/0/Documents/FTxT/ftxt_battery_history_YYYYMMDD_HHmmss.db`
- Toast: "Database berhasil di-backup ke Documents/FTxT/"

**Restore:**
- Ketuk "Restore" → tampilkan dialog pilih file (dari folder `Documents/FTxT/`)
- Konfirmasi: "Ganti data saat ini dengan backup dari [tanggal]?"
- Database lama di-backup dulu ke internal, lalu diganti dengan file yang dipilih
- Toast: "Database berhasil di-restore!"

### 2.1 Riwayat Sesi Pengisian
**Lokasi:** Kartu Kesehatan Baterai (tab Monitor)

**Tampilan:**
```
[Kartu Kesehatan Baterai]
  ...
  Riwayat Sesi (5 terakhir):
  ┌─────────────────────────────────────────────┐
  │ 26 Aug 14:30–15:15 │ 45m │ 20→80% │ 3200 mAh │
  │ 25 Aug 22:00–23:30 │ 90m │ 10→100%│ 4100 mAh │
  │ 25 Aug 08:15–08:45 │ 30m │ 45→75% │ 1500 mAh │
  │ ...                                         │
  └─────────────────────────────────────────────┘
  [Lihat Semua Riwayat] ← optional, buka halaman detail
```

### 2.2 Suhu Min/Max/Avg per Sesi
**Lokasi:** Dua tempat

**A. Kartu Metrik Real-Time (tab Monitor)**
Tampil di baris suhu yang sudah ada:
```
Suhu                34.2°C
  (min 21°C · max 43°C · avg 33°C)
```

**B. Panel Grafik (tab Monitor, di bawah kartu Metrik)**
Tampil di kartu Grafik Riwayat, khusus untuk grafik Suhu:
```
[Kartu Grafik Riwayat]
  Suhu
  min 21°C · max 43°C · avg 33°C
  [grafik Suhu di sini]
```
Atau di sub-header grafik Suhu, tambah label min/max/avg sebagai ringkasan sesi aktif.

**Cara kerja:**
- Saat sesi charging dimulai, reset min/max/avg
- Setiap sample: update min, max, akumulasi avg
- Saat sesi selesai: simpan hasil ke database
- Tampilan di kartu Metrik = sesi aktif (real-time)
- Tampilan di grafik = ringkasan sesi terakhir atau sesi aktif

### 2.3 Estimasi Waktu Pengisian & Pengosongan
**Lokasi:** Kartu Metrik Real-Time (tab Monitor)

**Tampilan saat charging:**
```
Status              Charging•AC
Est. Penuh          ±45 menit
```

**Tampilan saat discharging:**
```
Status              Discharging
Est. Habis          ±3 jam 20 menit
```

### 2.4 Activity Timeline Bar
**Lokasi:** Di bawah grafik Persentase (kartu Metrik Real-Time) atau di bawah setiap grafik di kartu Grafik Riwayat

**Tampilan:**
```
[Grafik Persentase]
  ─────────────────────────────────────────────────
[Activity Timeline Bar]
  ██░░░░░░████████░░░░░░░░░░░████████████░░░░░░░
  ↑       ↑        ↑           ↑           ↑
  08:00   10:00    12:00       14:00       16:00

Legend:
  ██ = Layar Aktif (screen on)
  ░░ = Layar Mati (screen off)
  ██ (hijau) = Charging
```

**Warna:**
- Layar aktif: biru/ungu
- Layar mati: abu gelap
- Charging: hijau

**Cara kerja:**
- Bar mengikuti sumbu waktu grafik persentase
- Setiap perubahan status dicatat: waktu + status baru
- Data tersimpan di database (tabel `activity_log`)
- Bar digambar sebagai sequence rectanglle berwarna

### 2.5 Battery Usage by App
**Lokasi:** Kartu baru "Penggunaan Aplikasi" di tab Monitor (di bawah kartu Kesehatan Baterai)

**Tampilan:**
```
[Kartu Penggunaan Aplikasi]
  Penggunaan Baterai (sejak充电 terakhir):
  ┌─────────────────────────────────────────────┐
  │ 📱 Chrome          23.5%   (FG: 18.2%, BG: 5.3%) │
  │ 📱 Instagram       15.2%   (FG: 14.8%, BG: 0.4%) │
  │ 📱 WhatsApp        12.8%   (FG: 8.1%, BG: 4.7%)  │
  │ 📱 YouTube         10.1%   (FG: 9.9%, BG: 0.2%)  │
  │ 📱 Sistem          38.4%   (FG: 12.0%, BG: 26.4%) │
  └─────────────────────────────────────────────┘
  Total: 100% (sejak充电 terakhir)
  [Atur Ulang Statistik]
```

Atau bisa juga ditampilkan sebagai bar horizontal per aplikasi:
```
Chrome     ████████████████████████ 23.5%
Instagram  ████████████████ 15.2%
WhatsApp   █████████████ 12.8%
YouTube    ██████████ 10.1%
Sistem     ████████████████████████████████████████ 38.4%
```

---

## 3. Cara Kerja

### 3.1 Riwayat Sesi Pengisian
- Data sudah tersedia di tabel `sessions` di database (`BatteryHistoryDb`)
- Tambah field: `startTime`, `endTime`, `startPercent`, `endPercent`, `avgTempC`
- Query 5 sesi terakhir untuk ditampilkan
- Optional: tombol "Lihat Semua" buka halaman detail

### 3.2 Suhu Min/Max/Avg per Sesi
- Saat sesi charging dimulai, reset min/max/avg
- Setiap sample: update min, max, akumulasi avg
- Saat sesi selesai: simpan hasil ke database
- Tampilkan di kartu Metrik Real-Time

### 3.3 Estimasi Waktu Pengisian & Pengosongan
**Saat charging:**
- Hitung laju pengisian: `deltaPersen / deltaWaktu` (% per menit)
- Estimasi sisa waktu: `(100 - persenSekarang) / laju` menit
- Gunakan rata-rata laju dari 5 menit terakhir (bukan sejak awal)

**Saat discharging:**
- Hitung laju pengosongan: `deltaPersen / deltaWaktu` (% per menit)
- Estimasi sisa waktu: `persenSekarang / laju` menit
- Gunakan rata-rata laju dari 15 menit terakhir

### 3.4 Activity Timeline Bar
- Setiap sample (±1 detik saat charging, ±5 detik idle, ±30 detik layar mati):
  - Cek status: `isScreenOn()` + `isCharging()`
  - Jika status berubah dari sample sebelumnya, catat ke database
  - Format: `{time, status}` — status: 0=off, 1=on, 2=charging
- Query data untuk rentang waktu grafik yang sedang ditampilkan
- Gambar bar sebagai sequence rectangle berwarna mengikuti sumbu waktu
- Bar harus sejajar persis dengan grafik persentase (share sumbu X yang sama)

### 3.5 Battery Usage by App
- Android menyediakan `BatteryUsageStats` (API 23+) untuk mendapatkan penggunaan baterai per aplikasi
- Query saat tab Monitor ditampilkan atau saat user refresh
- Hitung total waktu sejak charging terakhir
- Tampilkan aplikasi dengan konsumsi terbesar di atas
- Pisahkan konsumsi foreground vs background
- Tombol "Atur Ulang Statistik" untuk reset counter

### 3.6 Backup & Restore Database
**Export (Backup):**
- Baca database dari `/data/data/exp.ftxt/databases/battery_history.db`
- Copy ke `/storage/emulated/0/Documents/FTxT/ftxt_battery_history_YYYYMMDD_HHmmss.db`
- Pakai `MediaStore` API untuk akses folder Documents (Android 10+)
- Atau pakai `MANAGE_EXTERNAL_STORAGE` permission (lebih fleksibel)

**Import (Restore):**
- Baca list file `.db` dari folder `Documents/FTxT/`
- User pilih file → konfirmasi
- Backup database lama ke internal (sebagai safety)
- Replace database dengan file yang dipilih
- Restart `BatteryHistoryDb` instance untuk load data baru

---

## 4. File yang Perlu Diubah

| File | Keterangan |
|------|------------|
| `BatteryHistoryDb.java` | Tambah tabel `activity_log` + field sesi: startTime, endTime, startPercent, endPercent, avgTempC |
| `BatteryCapacityEstimator.java` | Catat suhu min/max/avg selama sesi |
| `BatteryMonitor.java` | Log status perubahan (screen on/off, charging) ke database |
| `BatteryMonitorTabController.java` | Tampilkan riwayat sesi, suhu stats, estimasi waktu |
| `BatteryChartView.java` | Gambar Activity Timeline Bar di bawah grafik |
| `BatteryUsageController.java` (baru) | Query & tampilkan penggunaan baterai per aplikasi |
| `panel_battery.xml` | Update layout kartu Metrik Real-Time, Kesehatan, & tambah kartu Penggunaan Aplikasi |
| `BatteryHistoryDb.java` | Tambah method backup() dan restore() |
| `BatteryMonitorTabController.java` | Tambah handler untuk tombol Backup dan Restore |

---

## 5. Estimasi Waktu Pengerjaan

- **Riwayat sesi:** ±2–3 jam
- **Suhu min/max/avg:** ±1–2 jam
- **Estimasi waktu:** ±2–3 jam
- **Activity Timeline Bar:** ±3–4 jam
- **Battery Usage by App:** ±2–3 jam
- **Backup & Restore Database:** ±1–2 jam
- **Total:** ±11–17 jam (≈1,5–2 hari)

---

## 6. Status

- [?] Riwayat sesi pengisian
- [ ] Suhu min/max/avg per sesi
- [?] Estimasi waktu pengisian & pengosongan
- [?] Activity Timeline Bar
- [ ] Battery Usage by App
- [ ] Backup & Restore Database
