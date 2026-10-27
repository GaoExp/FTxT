# Data Usage & Alat Jaringan — Hasil Kajian Data Monitor

**Status:** Rencana — belum dikerjakan
**Tujuan:** Menerapkan fitur pemakaian data dan alat jaringan yang terbukti berguna dari `anomali.datamonitor` (Data Monitor) dan `com.andcreate.app.internetspeedmonitor` (Internet Speed Monitor), tanpa meniru iklan/premium dan tanpa menambah beban overlay.

---

## 1. Kondisi Saat Ini

- Panel Network sudah punya footer **Monitor** dan **Overlay**.
- Tab Monitor menampilkan pemakaian hari ini, rata-rata per jam, puncak, dan grafik 7 hari.
- Sumber data masih `TrafficStats.getTotalRxBytes()/getTotalTxBytes()`, yaitu **total perangkat** (WiFi + seluler digabung), disimpan di `SharedPreferences` (`usage_*`) via `DataUsageTracker`.
- Belum ada rincian per aplikasi, pemisahan WiFi/seluler, kuota, notifikasi, widget, atau alat jaringan.
- `PACKAGE_USAGE_STATS` sudah dideklarasikan, tetapi belum dipakai. `POST_NOTIFICATIONS` juga sudah ada.
- APK hasil dekompilasi tersedia di `_temp/apk1/`: `DataMonitor_1.0.223/` (`anomali.datamonitor` v1.0.223) dan `base2/` (`com.andcreate.app.internetspeedmonitor` v1.2.1), masing-masing berisi dump `smali`, manifest, dan badging.

---

## 2. Prinsip Penerapan

1. **Perluas yang sudah ada**, jangan buat panel data usage kedua.
2. **Tanpa root/Shizuku** untuk semua fitur inti.
3. **Izin bertahap**: fitur tetap tampil terbatas bila izin belum diberikan, dengan tombol menuju pengaturan izin.
4. **Hindari polling berlebihan**; manfaatkan query sistem dan cache harian.
5. **Jangan menduplikasi overlay**: status bar meter dan widget bersifat opsional karena FTxT sudah punya overlay kecepatan.

---

## 3. Fitur yang Layak Diterapkan

### 3.1 Rincian per Aplikasi (prioritas tinggi)

**Yang dilakukan aplikasi acuan:** satu query `NetworkStatsManager.queryDetails(type, subscriberId, start, end)` untuk periode hari ini, lalu bucket dijumlahkan per `uid`; `queryDetailsForUid()` hanya dipakai saat membuka detail satu aplikasi.

**Penerapan di FTxT:**
- Tambah sub-tab/segmented control di Monitor: **Total | WiFi | Seluler**.
- Daftar "Per aplikasi hari ini": ikon, nama, total RX+TX, bar proporsi, urut terbesar.
- Ketuk item → detail: RX, TX, WiFi vs seluler, pemakaian 7 hari (query on-demand).
- Label khusus: `Android System` (uid 1000), aplikasi tanpa paket, aplikasi yang sudah di-uninstall.
- Mapping UID → paket memakai `PackageManager.getPackagesForUid()`; tidak perlu `QUERY_ALL_PACKAGES`.

**Catatan teknis:**
- Android 10+ (`SDK >= 29`): `subscriberId = null` untuk query seluler.
- Android 8–9: butuh `READ_PHONE_STATE` + `TelephonyManager.getSubscriberId()`; bila ditolak, tampilkan pesan "data seluler per aplikasi tidak tersedia" dan tetap tampilkan WiFi.
- Data sistem bisa terlambat beberapa menit dan tidak selalu mencakup VPN/tethering; jangan tampilkan sebagai angka absolut yang "pasti".

**Estimasi:** 5–7 jam.

### 3.2 Pemisahan WiFi/Seluler + Riwayat Harian (prioritas tinggi)

**Penerapan:**
- Total harian WiFi dan seluler dari `querySummaryForDevice(type, subscriberId, start, end)`; cadangan `TrafficStats` bila query gagal.
- Simpan riwayat ke SQLite (`usage_day(date, type, rx, tx)`), bukan menambah key prefs.
- Migrasi data `usage_*` yang ada sebagai total (type `ALL`) agar grafik lama tidak hilang.
- Retensi 90 hari (cukup untuk tren; bukan 365).
- Grafik 7/30 hari dengan mode Total/WiFi/Seluler.

**Alasan pakai DB:** rincian per aplikasi per hari tidak muat secara wajar di `SharedPreferences`, dan query sistem tidak menyimpan riwayat selamanya.

**Estimasi:** 6–8 jam.

### 3.3 Kuota & Siklus Tagihan (prioritas tinggi)

**Penerapan:**
- Kartu kuota di atas tab Monitor (hanya muncul bila kuota diisi).
- Input: ukuran kuota, tanggal mulai siklus (1–28), mode reset (tanggal tetap / setiap N hari).
- Tampilan: terpakai / kuota, sisa, persentase, progress bar, perkiraan habis.
- Peringatan 80% dan 90%, maksimal sekali per hari per ambang.
- Reset manual + opsi "hitung dari awal siklus".
- Carry-over **ditunda** (butuh aturan khusus dan rawan salah hitung).

**Sumber:** total pemakaian dari §3.2 (bisa pilih WiFi+seluler atau seluler saja).

**Estimasi:** 4–6 jam.

### 3.4 Notifikasi & Status Bar (prioritas sedang)

**Penerapan:**
- Notifikasi ringkas: pemakaian hari ini, sisa kuota, kecepatan live (opsional).
- Aksi: buka panel Network, jeda notifikasi 1 jam.
- Status bar netmeter **hanya** jika pengguna mematikan overlay; jangan aktif bersamaan secara default.
- Jangan buat notification listener (`NLService`) seperti aplikasi acuan — tidak relevan untuk FTxT.

**Estimasi:** 3–4 jam.

### 3.5 Widget (prioritas rendah–sedang)

**Penerapan:**
- Satu widget 2x1 dan satu 1x1: pemakaian hari ini + sisa kuota.
- `AppWidgetProvider` + `RemoteViews`; update tiap 30 menit dan saat kuota berubah.
- Konfigurasi minimal: network (total/seluler), unit, tema terang/gelap.
- Tidak perlu 6 variasi widget seperti aplikasi acuan.

**Estimasi:** 6–8 jam.

### 3.6 Alat Jaringan (prioritas sedang, header tab "Alat")

Ditempatkan sebagai **header tab di dalam tab Monitor**, bukan footer dan bukan Activity terpisah.

**Fitur yang diambil:**
- Ping berkelanjutan: min/avg/max/mdev, packet loss, grafik sederhana.
- DNS lookup (`InetAddress.getAllByName`) dan reverse lookup.
- Traceroute sederhana (bila binary tersedia; jika tidak, tampilkan "tidak didukung").
- Info WiFi: SSID, BSSID, RSSI, frekuensi, channel, link speed, IP/gateway/DNS.
- Info koneksi: tipe aktif, IP lokal, operator, roaming, status SIM.

**Batasan:**
- `/system/bin/ping` ada di hampir semua perangkat, tetapi eksekusi `ProcessBuilder` bisa diblokir pada sebagian ROM.
- WHOIS butuh koneksi TCP port 43; sering diblokir operator — jadikan opsional.
- SSID/BSSID butuh izin lokasi (Android 8.1+) dan `NEARBY_WIFI_DEVICES` untuk sebagian fungsi di Android 13+.
- IMEI tidak diambil (butuh izin privileged); cukup tampilkan yang tersedia publik.

**Estimasi:** 8–12 jam.

### 3.7 Info SIM & Koneksi (prioritas rendah)

- Operator, jumlah slot, status data, roaming, tipe jaringan.
- `SubscriptionManager` butuh `READ_PHONE_STATE`; tampilkan versi terbatas tanpa izin.
- Tempatkan di header tab "Alat" bersama ping/DNS/WiFi, bukan di footer.

**Estimasi:** 3–4 jam.

---

## 4. Yang Tidak Diterapkan

- Iklan, age-gate, premium/billing, dan promosi aplikasi.
- `NLService` (notification listener) untuk snooze notifikasi.
- `ConfigReaderActivity` dan menu debug internal.
- Enam varian widget, tema bawaan, serta penggantian launcher icon.
- Query `TrafficStats.getUidRxBytes()` sebagai sumber utama: sejak Android 7 hasilnya sering `UNSUPPORTED`; hanya dipakai sebagai cadangan.
- Pembacaan `/proc/net/xt_qtaguid/stats`: sudah tidak tersedia di perangkat modern.

---

## 5. Penempatan UI

**Tidak perlu modul Gradle baru.** Semua fitur berada di panel Network; pembagiannya hanya pada level navigasi.

- **Footer** khusus untuk hal besar/utama: **Monitor | Overlay**. Ini konsisten dengan panel Battery (Monitor/Overlay/Battery Strip) dan Memory (Monitor/Overlay).
- **Header tab** dipakai untuk isi di dalam Monitor, mengikuti pola Battery Monitor (Info & Grafik / Sesi Berjalan / Kondisi & Riwayat).
- **Alat jaringan** jadi header tab **"Alat"**, bukan item footer keempat. Footer tidak ditambah agar tetap khusus.
- Data usage, per aplikasi, kuota, notifikasi, dan widget memakai `DataUsageTracker` yang diperluas; tidak ada lifecycle overlay baru.

**Susunan yang disarankan:**
```
Panel Network
├── Footer: Monitor | Overlay
└── Monitor
    ├── Header tab: Pemakaian | Per Aplikasi | Alat
    ├── Pemakaian
    │   ├── Kartu kuota (opsional)
    │   ├── Segmented: Total | WiFi | Seluler
    │   └── Angka hari ini + grafik 7/30 hari + top 5 aplikasi
    ├── Per Aplikasi
    │   ├── Filter Total/WiFi/Seluler + urut pemakaian
    │   └── Daftar lengkap, ketuk untuk detail 7 hari
    └── Alat
        ├── Ping / DNS / traceroute
        ├── Info WiFi (sinyal, channel, IP)
        └── Info SIM & koneksi
```

Header tab hanya menukar konten di dalam Monitor, sehingga footer tetap bersih dan tab Overlay tidak tercampur utilitas.

Implementasi mengikuti pola header Battery Monitor yang sudah ada: deretan `TextView` (`batSubTabInfo`/`batSubTabLive`/`batSubTabHealth`) + `ViewPager2` (`batSubPager`), bukan `TabLayout` baru. Warna aktif/nonaktif memakai selector yang sama polanya dengan `bat_monitor_header`/`bat_monitor_label`.

---

## 6. Sumber Data & Izin

| Fitur | API utama | Izin |
|-------|-----------|------|
| Per aplikasi | `NetworkStatsManager.queryDetails()` | `PACKAGE_USAGE_STATS` (app-op) |
| Seluler per aplikasi (Android 8–9) | `queryDetails(TYPE_MOBILE, subscriberId, ...)` | `READ_PHONE_STATE` (opsional) |
| Total WiFi/seluler | `querySummaryForDevice()` | `PACKAGE_USAGE_STATS` |
| Kecepatan live | `TrafficStats.getTotalRxBytes()` | tidak ada |
| Kuota | data §3.2 + prefs | tidak ada |
| Notifikasi | `NotificationCompat` | `POST_NOTIFICATIONS` (sudah ada) |
| Widget | `AppWidgetProvider`, `RemoteViews` | tidak ada |
| Ping/DNS | `ProcessBuilder`, `InetAddress` | `INTERNET` (sudah ada) |
| Info WiFi | `WifiManager`, `WifiInfo` | `ACCESS_WIFI_STATE`, `ACCESS_FINE_LOCATION`, `NEARBY_WIFI_DEVICES` |
| Info SIM | `TelephonyManager`, `SubscriptionManager` | `READ_PHONE_STATE` |

Izin harus diminta dari layar pengaturan FTxT, bukan langsung saat panel dibuka. Untuk `PACKAGE_USAGE_STATS`, arahkan ke `Settings.ACTION_USAGE_ACCESS_SETTINGS` karena izin ini bukan runtime permission biasa.

---

## 7. File Kandidat

| File | Keterangan |
|------|------------|
| `features/network/.../DataUsageTracker.java` | Tambah mode WiFi/seluler, integrasi DB, migrasi prefs |
| `features/network/.../NetworkStatsSource.java` (baru) | Query `NetworkStatsManager`, agregasi per UID |
| `features/network/.../UsageDatabase.java` (baru) | SQLite riwayat harian + per aplikasi |
| `features/network/.../QuotaManager.java` (baru) | Siklus, sisa kuota, ambang peringatan |
| `features/network/.../AppUsageAdapter.java` (baru) | Daftar per aplikasi |
| `app/.../ui/NetworkPanelController.java` | Segmented control, daftar per aplikasi, kartu kuota |
| `app/src/main/res/layout/panel_network.xml` | Kartu kuota, segmented, container daftar |
| `app/src/main/res/layout/item_app_usage.xml` (baru) | Item daftar aplikasi |
| `app/.../ui/NetworkToolsView.java` (baru) | Konten header tab "Alat": ping, DNS, WiFi, SIM |
| `app/src/main/res/layout/view_network_tools.xml` (baru) | Layout konten alat jaringan |
| `app/src/main/res/xml/network_widget_info.xml` (baru) | Metadata widget |
| `app/.../widget/NetworkUsageWidget.java` (baru) | Provider widget |
| `app/src/main/AndroidManifest.xml` | Receiver widget + izin tambahan (tanpa Activity alat) |

Catatan: paket/lokasi final mengikuti struktur `features/network` dan `app/src/main/java/exp/ftxt/ui` yang ada.

---

## 8. Risiko & Batasan

- **Ketersediaan data:** sebagian perangkat/ROM tidak mengembalikan data per UID dengan lengkap; selalu sediakan fallback dan pesan jelas.
- **Dual SIM:** pemisahan per SIM tidak akurat tanpa `subscriberId`; cukup agregat "seluler" pada tahap awal.
- **Keterlambatan:** `NetworkStatsManager` bisa terlambat beberapa menit dari pemakaian nyata; jangan tampilkan sebagai real-time.
- **VPN/tethering:** pemakaian bisa tidak terhitung atau terhitung ganda tergantung perangkat.
- **Baterai:** query riwayat 30 hari per aplikasi berat; lakukan on-demand dan cache hasilnya.
- **Package visibility (Android 11+):** jangan minta `QUERY_ALL_PACKAGES`; cukup paket yang muncul dari UID.
- **Exec ping:** bisa diblokir ROM; sediakan opsi DNS sebagai alternatif yang lebih andal.
- **Widget:** update background dibatasi Doze; akui interval ≥30 menit.

---

## 9. Estimasi Total

| Pekerjaan | Estimasi |
|-----------|----------|
| Per aplikasi | 5–7 jam |
| WiFi/seluler + DB riwayat | 6–8 jam |
| Kuota & siklus | 4–6 jam |
| Notifikasi/status bar | 3–4 jam |
| Widget | 6–8 jam |
| Alat jaringan | 8–12 jam |
| Info SIM/koneksi | 3–4 jam |
| Testing & penyempurnaan | 6–8 jam |
| **Total** | **≈41–57 jam (5–7 hari kerja)** |

Bisa dipecah menjadi tiga rilis: (1) per aplikasi + split WiFi/seluler, (2) kuota + notifikasi, (3) alat jaringan + widget.

---

## 10. Status

- [ ] `NetworkStatsSource` + query per aplikasi
- [ ] Pemisahan WiFi/seluler
- [ ] DB riwayat + migrasi prefs
- [ ] Kartu kuota & siklus tagihan
- [ ] Peringatan 80%/90%
- [ ] Notifikasi ringkas
- [ ] Widget 2x1 / 1x1
- [ ] Header tab Monitor (Pemakaian / Per Aplikasi / Alat)
- [ ] Header tab Alat (ping/DNS/WiFi)
- [ ] Info SIM/koneksi
- [ ] Layar izin terpadu
- [ ] Testing lintas ROM & Android 8–15
