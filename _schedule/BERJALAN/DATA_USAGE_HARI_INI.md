# Data Usage Hari Ini — Rekap Konsumsi Data

**Status:** Rencana siap dikerjakan (revisi) — belum ada implementasi
**Tujuan:** Menampilkan rekap konsumsi data harian (unduh/unggah) di panel Network, plus opsi tampil ringkas di overlay, memakai sumber data yang sudah dipakai modul Network saat ini.

---

## 1. Masalah & Ide

Modul **Network Speed** sekarang hanya menampilkan kecepatan saat ini (bytes/detik). Tidak ada catatan berapa data yang terpakai sepanjang hari — padahal ini angka yang paling sering dicari pengguna (mis. "hari ini aku pakai 1,2 GB atau belum?").

Ide: rekam total bytes yang terpakai per hari dari counter `TrafficStats` yang **sudah dipakai** `NetworkModule`, lalu tampilkan sebagai kartu baru di panel Network.

Sifat fitur: **tambahan** (additive) — tidak mengubah perilaku modul Network yang sudah ada, tidak ada database baru, tidak ada izin baru, tidak ada dependency baru.

---

## 2. Sumber Data

`android.net.TrafficStats`:

- `getTotalRxBytes()` / `getTotalTxBytes()` → **counter global perangkat** (semua aplikasi, semua interface), monotonik naik sejak boot, **tanpa izin**.
- Sudah dipakai di `features/network/.../NetworkModule.java`: baris 62 (baseline saat `start()`) dan baris 352 (tick kecepatan), jadi sumber ini terbukti bekerja di perangkat target.
- **Bisa mengembalikan `UNSUPPORTED` (-1)** di sebagian perangkat/ROM. Semua pembacaan wajib dicek; jika tidak valid, observasi dilewati (tidak mengubah baseline) dan UI menampilkan "Tidak didukung perangkat ini".

Kenapa bukan `NetworkStatsManager`?

- `querySummary()` butuh `READ_PHONE_STATES` + `subscriberId` → gesekan izin, tidak cocok untuk target "cepat & aman".
- Ambang ini setara dengan yang dipakai referensi app *Data Usage Monitor* yang mengukur **delta counter** (lihat `_schedule/ANALISIS_PENANGANAN_DATA_INTERNET.md`).

Batasan yang harus diterima (dijelaskan ke user di UI):

- Angkanya **total seluruh aplikasi di perangkat**, bukan per aplikasi. Label UI harus eksplisit, mis. "Total perangkat".
- Counter device **reset tiap reboot** (bukan reset aplikasi) — ditangani di logika akumulasi.
- Traffic **sebelum fitur pertama kali berjalan tidak dihitung** (baseline diambil dari counter saat observasi pertama).

---

## 3. Mekanika Penghitungan

### 3.1 Inisialisasi & baseline

Tracker adalah **singleton** di modul `features:network` (dipakai `NetworkModule` dan `MainActivity`). Ada dua kondisi awal:

- **Pertama kali** (`usage_last_rx`/`usage_last_tx` belum ada): ambil counter saat ini sebagai baseline, total hari = 0, `dayKey` = hari ini, `dayStart` = sekarang. Jangan menghitung traffic sejak boot sebagai pemakaian hari ini.
- **Proses aplikasi dibuka ulang** (prefs sudah ada): baca baseline, total, peak, `dayKey`, `dayStart`, dan riwayat dari prefs. Jangan reset.

### 3.2 Akumulasi delta

```
if (rx < 0 || tx < 0) return;              // UNSUPPORTED → lewati
deltaRx = rx - lastRx;
deltaTx = tx - lastTx;
if (deltaRx < 0) deltaRx = 0;              // counter reset (reboot)
if (deltaTx < 0) deltaTx = 0;
dayRx += deltaRx;
dayTx += deltaTx;
lastRx = rx;
lastTx = tx;
```

Karena counter monotonik, delta yang terlewat (modul mati/offline beberapa jam) tetap terhitung utuh saat diamati berikutnya — selama tidak ada reboot.

### 3.3 Titik pemanggilan `observe()`

1. Tiap tick modul Network (interval 0,2–10 detik).
2. Saat app dibuka/dikembalikan ke depan — panggil di `MainActivity.onResume()`, bukan hanya `onCreate()`, agar sesi yang lama di latar belakang ikut tercatat saat kembali.

`MainActivity.onPause()` memanggil `flush()` agar data tidak menunggu throttle.

### 3.4 Persistensi (anti dobel/hilang)

Total dan baseline **wajib di-flush bersamaan dalam satu `SharedPreferences.Editor`**, karena keduanya saling bergantung:

- State di memori diperbarui setiap observasi.
- Flush ke prefs paling sering tiap ±30 detik, plus **segera** saat pergantian hari, reset, `MainActivity.onPause()`, dan saat modul/service berhenti.
- Jangan pernah menulis baseline lebih baru daripada total (atau sebaliknya). Jika tidak sinkron, proses yang dibunuh bisa membuat data hilang atau terhitung dua kali.
- Semua akses lewat singleton dan dijalankan di main thread (tick `NetworkModule` dan `onResume` sama-sama main thread); tambahkan `synchronized` pada `observe()`/`flush()` untuk aman.

### 3.5 Pergantian hari

Bandingkan `LocalDate.now()` (format ISO `yyyy-MM-dd`) dengan `usage_day_key`:

- **Sama** → akumulasi seperti biasa.
- **Lebih baru** → catat total hari lama ke riwayat, reset total & peak, set `dayKey` baru, `dayStart` = sekarang, lalu delta yang teramati masuk **seluruhnya** ke hari baru. Hari-hari di antaranya **tidak dibuatkan entri palsu** (dibiarkan bolong).
- **Lebih lama** (jam perangkat dimundurkan) → **jangan rotasi**. Tetap pakai `dayKey` tersimpan dan lanjut akumulasi sampai waktu kembali melewati tanggal tersebut. Ini mencegah data hari berjalan terhapus karena koreksi jam/zona waktu.

Konsekuensi yang diterima: delta yang melintasi tengah malam dihitung utuh ke hari baru (pendekatan kasar, ditampilkan sebagai pembulatan).

### 3.6 Reboot

- Delta negatif di-clamp 0 → total hari ini **tidak** di-nolkan; baseline langsung diganti counter baru.
- **Yang hilang:** seluruh traffic sejak observasi terakhir sampai reboot (bukan hanya setelah reboot). Semakin lama app tidak dibuka sebelum reboot, semakin besar yang hilang dan tidak bisa dipulihkan.

### 3.7 Peak (kecepatan tertinggi hari ini)

- Peak dihitung dari tick modul Network (`noteSpeed(rxSpeed, txSpeed)`), bukan dari `observe()` di `MainActivity` yang jaraknya bisa berjam-jam.
- Kecepatan negatif (reboot di tengah interval) di-clamp 0 sebelum dibandingkan.
- Peak di-reset saat pergantian hari/reset.
- Nilai peak **bergantung pada interval sampling** (0,2–10 detik), jadi bukan angka absolut. Label UI menyebut interval yang sedang dipakai, mis. "Puncak (sampling 1s)".
- Peak hanya akurat selama modul Network aktif.

### 3.8 Rata-rata per jam

`dayTotal / max(1, jam sejak usage_day_start)`. `dayStart` diisi saat akumulasi hari itu mulai (inisialisasi pertama, rollover, atau reset), bukan tengah malam — supaya pembagi sesuai data yang benar-benar tercatat. Tampilkan "—" bila baru berjalan kurang dari 1 jam agar tidak menyesatkan.

### 3.9 Reset Hari Ini

- Nol-kan `dayRx`/`dayTx` dan peak.
- Set `dayKey` = hari ini, `dayStart` = sekarang.
- **Set baseline `lastRx`/`lastTx` ke counter saat ini** (baca ulang `TrafficStats`); tanpa ini, delta sejak observasi terakhir akan masuk lagi tepat setelah reset.
- Riwayat hari sebelumnya **tidak** dihapus.

---

## 4. Data yang Disimpan

Disimpan di `SharedPreferences` `ftxt_prefs` (pola prefs yang sudah dipakai modul ini):

- `usage_day_key` — String `yyyy-MM-dd`, penanda hari berjalan
- `usage_day_start` — long (epoch ms), waktu mulai akumulasi hari berjalan (untuk rata-rata/jam)
- `usage_day_rx` / `usage_day_tx` — long, total bytes hari berjalan
- `usage_last_rx` / `usage_last_tx` — long, counter terakhir yang diamati (baseline delta)
- `usage_peak_rx` / `usage_peak_tx` — long, peak bytes/detik hari berjalan
- `usage_history` — String `"yyyy-MM-dd:rx:tx;..."`, urut lama → baru, maks 30 hari (tanpa Gson, string delimited). Saat dibaca, entri yang tidak valid dilewati.
- `network_show_usage_in_overlay` — boolean, default **false**

Aturan flush ada di §3.4. `usage_history` hanya ditulis saat rollover (bukan tiap tick).

---

## 5. Tampilan

### 5.1 Panel Network — section baru "Data Usage"

Section collapsible (pola `SectionHelper`) di `panel_network.xml`, berisi label "Total perangkat" agar jelas cakupannya:

```
Hari ini        ↓ 245 MB   ↑ 38 MB
Rata-rata       ↓ 11 MB/jam
Puncak          ↓ 3.2 MB/s   ↑ 210 KB/s   (sampling 1s)
[Reset Hari Ini]
```

- Formatter total harian **terpisah** dari formatter kecepatan: KB/MB/GB tanpa `/s` (formatter `formatUnit` yang ada selalu menambah `/s` dan belum punya GB).
- Panel diperbarui **live** saat terlihat: `DataUsageTracker` menyediakan listener; `NetworkPanelController` mendaftar di `onPanelShown()` dan melepas di `onPanelHidden()`. Karena `NetworkPanelFragment` belum punya `onPanelHidden()`, tambahkan override tersebut (dipanggil `PanelManager`).
- Jika `TrafficStats` tidak didukung: tampilkan pesan "Tidak didukung perangkat ini", sembunyikan kartu angka, nonaktifkan checkbox overlay.
- Grafik batang **7 hari terakhir** (termasuk hari ini, rx & tx side-by-side) memakai custom view baru. Hari tanpa data ditampilkan sebagai batang kosong/0.

### 5.2 Overlay (opsional, default OFF)

Checkbox baru "Tampilkan Usage Harian di Overlay" di section Tampilan. Saat aktif, overlay jadi dua baris:

```
↓1.2MB/s ↑340KB/s
Hari ini ↓245MB ↑38MB
```

- Baris pertama **tidak berubah**, termasuk perilaku `showOnlyValue` (sembunyikan label) yang hanya berlaku untuk baris kecepatan.
- Baris kedua selalu memakai format lengkap `Hari ini ↓… ↑…` dan **tidak** ikut diproses oleh logika `showOnlyValue`.
- Render dua baris sebagai dua `SpannableString` terpisah yang digabung newline, span warna label diterapkan per baris (jangan mengandalkan loop karakter `↓↑M B K / s` pada seluruh teks gabungan).
- Saat checkbox mati, perilaku overlay lama tetap utuh.

### 5.3 Grafik batang 7 hari

Custom view baru `UsageBarsView` — batang sederhana tanpa dependensi tambahan, label tanggal di bawah, tinggi batang proporsional terhadap nilai tertinggi. Cosmetik, bukan data presisi.

---

## 6. File yang Perlu Dibuat / Diubah

| File | Keterangan |
|------|------------|
| `features/network/.../DataUsageTracker.java` (baru) | Singleton: baseline, akumulasi delta, throttle flush, rollover, peak, riwayat, listener, guard `UNSUPPORTED` |
| `features/network/.../UsageBarsView.java` (baru) | Custom view grafik batang 7 hari |
| `features/network/.../NetworkModule.java` (ubah) | `tracker.observe()` + `noteSpeed()` di tick; `tracker.flush()` di `stop()`; baris kedua overlay bila toggle aktif; render per baris |
| `features/network/.../NetworkConfig.java` (ubah) | Tambah field `showUsageInOverlay` |
| `app/src/main/java/exp/ftxt/MainActivity.java` (ubah) | Load config baru di `loadShadowConfigs` (pola yang sama seperti `network_update_interval`); `observe()` di `onResume()`, `flush()` di `onPause()` |
| `app/src/main/java/exp/ftxt/ui/NetworkPanelController.java` (ubah) | Section Data Usage, kartu ringkasan, grafik, tombol reset, checkbox overlay, listener live, state unsupported |
| `app/src/main/java/exp/ftxt/ui/fragment/NetworkPanelFragment.java` (ubah) | Override `onPanelHidden()` untuk melepas listener |
| `app/src/main/res/layout/panel_network.xml` (ubah) | Section baru + view container |

Tidak ada perubahan di `AndroidManifest.xml`, tidak ada database, tidak ada dependensi baru.

---

## 7. Batasan yang Diketahui (dijelaskan di UI)

- Total **semua aplikasi & interface** di perangkat, bukan per aplikasi; tidak bisa dipisah Wi-Fi vs seluler tanpa `NetworkStatsManager`.
- Traffic sebelum observasi pertama (install/update baru) tidak dihitung.
- Data baru masuk saat aplikasi dibuka atau saat overlay Network aktif. Selama tidak ada observasi sama sekali, pemakaian tidak tercatat.
- Gap antar-observasi bisa dipulihkan utuh **hanya jika tidak ada reboot**.
- Reboot menghilangkan seluruh traffic sejak observasi terakhir sampai reboot.
- Jika app tidak dibuka beberapa hari, hari-hari di antaranya **bolong**; delta yang terlewat masuk ke hari saat app akhirnya dibuka.
- Perubahan tanggal mengikuti waktu lokal perangkat. Jam mundur tidak mereset data (lihat §3.5).
- Delta yang melintasi tengah malam masuk utuh ke hari baru.
- Rata-rata per jam adalah pendekatan kasar (lihat §3.8); peak bergantung interval sampling.
- Reset hari tidak menghapus riwayat 30 hari.

---

## 8. Estimasi Waktu Pengerjaan

- `DataUsageTracker` + persistensi + edge case (rollover, reboot, unsupported): ±1,5 jam
- Integrasi `NetworkModule` (observe, peak, baris overlay, formatter): ±1 jam
- Section panel + kartu + tombol reset + checkbox + live listener: ±1,5 jam
- `UsageBarsView` (grafik batang): ±0,5–1 jam
- Load config di `MainActivity` + hook `onResume`/`onPause`: ±0,5 jam
- Verifikasi manual edge case: ±1 jam
- **Total: ±5–6 jam kerja aktif**

---

## 9. Status

- [ ] `DataUsageTracker` — baseline awal, akumulasi delta, guard `UNSUPPORTED`
- [ ] Persistensi atomik (total + baseline satu flush) + flush saat pause/stop
- [ ] Rollover harian, penanganan jam mundur, gap multi-hari, riwayat 30 hari
- [ ] Peak dari tick + clamp kecepatan negatif + label interval
- [ ] Integrasi tick `NetworkModule` + `observe()` di `MainActivity.onResume()`
- [ ] Section "Data Usage" di `panel_network.xml` + `NetworkPanelController` + listener live
- [ ] Override `onPanelHidden()` di `NetworkPanelFragment`
- [ ] `UsageBarsView` — grafik batang 7 hari
- [ ] Formatter total harian terpisah (KB/MB/GB tanpa `/s`)
- [ ] Opsi overlay dua baris (default OFF, aman terhadap `showOnlyValue`)
- [ ] Tombol "Reset Hari Ini" (termasuk reset baseline)
- [ ] Load config baru di `MainActivity`
- [ ] Verifikasi manual: install baru, proses dibunuh, reboot, tengah malam, gap multi-hari, jam/zona waktu diubah, reset, modul Network mati/nyala, interval diubah, perangkat `UNSUPPORTED`
- [ ] Update dokumen (CHANGELOG, README, PANDUAN, STRUKTUR) — hanya di file root

---

## 10. Perubahan dari Draf Sebelumnya

- Menambah baseline saat pertama kali berjalan agar traffic lama sejak boot tidak ikut terhitung.
- Menegaskan flush total + baseline harus atomik, plus flush saat pause/stop.
- Reset hari kini juga mereset baseline counter.
- Rollover/jam mundur/gap multi-hari diberi aturan eksplisit; loss saat reboot dikoreksi (sejak observasi terakhir, bukan hanya setelah reboot).
- Peak di-clamp, hanya dari tick, dan diberi label interval sampling.
- Formatter total harian dipisah dari formatter kecepatan (tanpa `/s`, dukung GB).
- Ditambah guard `UNSUPPORTED`, listener live panel, `onPanelHidden()`, dan penanganan `showOnlyValue` pada overlay dua baris.
