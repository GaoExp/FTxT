# Analisis Cara Reference App (Internet) Menangani Data

**Tanggal:** 2026/09/04
**Metode:** Ekstraksi & dekompilasi statis APK dengan `jadx` (Java), dianalisis dari skema SQL (Room) dan struktur WorkManager/service/receiver di manifest serta source dekompilasi.
**Sumber:**
- `_temp/sample_apk/Internet/Data Usage Monitor.apk` (`com.andcreate.app.trafficmonitor`, v1.23.3054)
- `_temp/sample_apk/Internet/Internet Speed Monitor.apk` (`com.andcreate.app.internetspeedmonitor`, v1.1.7.2)

> Catatan: kedua APK dipakai R8 + obfuscation (nama class/field banyak yang diganti). Nilai numerik (interval, ambang retensi) terbaca sebagian; pola arsitektur & skema database terbaca jelas. Dokumen ini fokus pada **strategi penanganan data**, bukan nilai nominal.

---

## Ringkasan Eksekutif

- **Data Usage Monitor** menangani data pemakaian paket serupa caranya memakai **Room + WorkManager** sebagai mesin utamanya: **pengukuran berdasar selisih counter (delta)** antara dua titik waktu, satu tabel agregat total dan satu tabel per-app, dieksekusi berkala via **Worker**, plus **pembersihan log** sebagai Worker berkala terpisah.
- **Internet Speed Monitor** menangani data kecepatan memakai **Room** dengan konsep **segmen** (blok rentang waktu berisi rata-rata & maks down/up), dikumpulkan dari **foreground service overlay** (polling kontinu), dan **melakukan pruning segmen lama**.
- Keduanya memakai **Room** (bukan SQLiteOpenHelper langsung) dan **melakukan retensi/pruning data secara berkala** — pola yang relevan bagi FTxT.

---

## 1. Data Usage Monitor (`com.andcreate.app.trafficmonitor`)

### 1.1 Pendekatan Umum: Room + WorkManager

Data pemakaian ditangani oleh **dua DAO** (`o3a`=total, `l5a`=per-app) di DB `TrafficsDatabase`. Perekaman & pemrosesan dijalankan **sebagian besar oleh WorkManager** (bukan foreground service):

**Worker berkala (WorkManager):**
- `TrafficRecordWorker` — tag "Traffic Record", **setiap 15 menit** → merekam sampel pemakaian ke DB.
- `TrafficInfoNotificationWorker` — tag "Traffic Info Notification", **setiap 15 menit** → notifikasi info kuota.
- `CarryOverTrafficsResetWorker` — **setiap 15 menit** → reset/realokasi carry-over kuota.
- `TrafficLogDeleteWorker` — tag "Traffic Log Delete", **setiap 1 hari** → **pembersihan log** (pruning).
- `TrafficLastMonthReportWorker` — per jam → laporan pemakaian bulan lalu.

Ekstra: `BeforeDateChangeTrafficRecordService` (service) dipicu menjelang ganti tanggal untuk menangkap snapshot batas tanggal/bulan.

### 1.2 Skema Database (Room)

Dua tabel:

**`TOTAL_TRAFFICS`** (pemakaian agregat per sesi pengukuran):
`_id, PREVIOUS_MEASURE_TIME, MEASURE_TIME, RX_BYTES, TX_BYTES, MOBILE_RX_BYTES, MOBILE_TX_BYTES, COMPRESSED_FIRST_TIME, COMPRESSED_SECOND_TIME, SSID`

**`TRAFFICS`** (pemakaian per proses/aplikasi):
`_id, PROCESS_NAME, PREVIOUS_MEASURE_TIME, MEASURE_TIME, RX_BYTES, TX_BYTES, MOBILE_RX_BYTES, MOBILE_TX_BYTES, COMPRESSED_FIRST_TIME, COMPRESSED_SECOND_TIME`

### 1.3 Konsep Kunci: Pengukuran Berbasis Delta (Counter)

Kolom `PREVIOUS_MEASURE_TIME` + `MEASURE_TIME` dan `RX_BYTES`/`TX_BYTES` menunjukkan pola penanganan yang penting:
- Pemakaian diambil sebagai **selisih counter paket** antara dua momen pengukuran (`MEASURE_TIME - PREVIOUS_MEASURE_TIME`), bukan nilai kumulatif mentah.
- Ada `COMPRESSED_FIRST_TIME` / `COMPRESSED_SECOND_TIME` — menandai **sesi/kompresi** pengukuran (mis. kapan interval mulai/berakhir diagregasi).
- `SSID` di tabel total → pemisahan pemakaian Wi-Fi vs seluler.
- `PROCESS_NAME` di tabel `TRAFFICS` → **pemecahan per aplikasi/proses** (dari data per-UID / `NetworkStats`).

### 1.4 Retensi / Pembersihan

- `TrafficLogDeleteWorker` (harian) membersihkan log lama agar DB tidak membengkak — pola retensi terkontrol seperti Battery Guru.

---

## 2. Internet Speed Monitor (`com.andcreate.app.internetspeedmonitor`)

### 2.1 Pendekatan Umum: Room + Foreground Overlay Service

Data kecepatan ditangani oleh **satu DAO (`wo7`)** pada DB `AppDatabase`, diisi oleh **`OverlayService`** (foreground service + overlay `SYSTEM_ALERT_WINDOW`) yang **melakukan polling kecepatan jaring secara kontinu** dan menulis hasilnya sebagai segmen.

Receiver: `BootReceiver` (`BOOT_COMPLETED`), `MyPackageReplacedReceiver`, `StatsAppInstallReceiver` — memulai/mengatur ulang monitoring.

### 2.2 Skema Database (Room)

**`speed_segments`** (`SpeedSegmentEntity`):
`id, startTimeMs, endTimeMs, networkType, ssid, avgDownBps, avgUpBps, maxDownBps, maxUpBps, count`

### 2.3 Konsep Kunci: Penyimpanan Sebagai Segmen (Agregat Rentang Waktu)

- Kecepatan **tidak disimpan per-sampling mentah**, melainkan dikelompokkan menjadi **segmen waktu** (`startTimeMs`–`endTimeMs`).
- Tiap segmen menyimpan **rata-rata & maksimum** `DownBps`/`UpBps` (+ `count` sampel penyusun) serta `networkType`/`ssid` — efisien untuk grafik/riwayat dan hemat ruang.
- DAO `wo7` (`bp7.java`) menyediakan:
  - `SELECT * FROM speed_segments WHERE startTimeMs >= ? AND endTimeMs <= ? ORDER BY startTimeMs ASC` — ambil segmen dalam rentang (untuk grafik/riwayat).
  - `SELECT MIN(startTimeMs) FROM speed_segments` — waktu mulai data paling awal.

### 2.4 Retensi / Pembersihan

- DAO berisi `DELETE FROM speed_segments WHERE endTimeMs < ?` — **penghapusan segmen lama** (retensi terkontrol), dipicu oleh kode aplikasi pada ambang waktu tertentu.

---

## 3. Perbandingan & Relevansi untuk FTxT

| Aspek | Data Usage Monitor | Internet Speed Monitor | FTxT (BatteryHistoryDb) |
|------|--------------------|------------------------|--------------------------|
| Mekanisme | Room + WorkManager | Room + Foreground Overlay | SQLiteOpenHelper |
| Unit data | Sesi delta (prev↔measure) | Segmen waktu (agg rate) | Sampel time-series (`samples`) |
| Pemisahan konteks | Per app (`PROCESS_NAME`) + Wi-Fi/mobile (`SSID`,`MOBILE_*`) | `networkType`/`ssid` per segmen | status/plugged/technology per sampel |
| Retensi/pruning | `TrafficLogDeleteWorker` harian | `DELETE ... endTimeMs < ?` | ❌ **tidak** (sengaja) |
| Pemrosesan berkala | WorkManager (15 mnt, 1 hari) | Foreground service + delete | handler/executor + manual |
| Aggregasi grafik | — | segmen avg/max | resample uniform per bucket (`queryChart`) |

### Hal yang bisa dipertimbangkan untuk FTxT

1. **Pruning / retensi data.** Semua reference app Internet & Battery (Guru) melakukan pembersihan data lama secara berkala; hanya FTxT yang sengaja tidak. Menambah retensi pada `samples`/`activity_log` (di background) berpotensi mengecilkan query riwayat yang berkaitan dengan isu freeze/ANR.
2. **Pengukuran berbasis delta vs integral.** Data Usage Monitor menyimpan selisih counter per interval; FTxT sudah mencatat `charge_mah` / integral (`mAh_counter`, `mAh_integral`) untuk sesi — konsep serupa.
3. **Aggregasi segmen vs per-sampel mentah.** Internet Speed Monitor menyimpan rata-rata/maks per segmen; FTxT memakai pendekatan berbeda (simpan sampel mentah + resample per bucket saat baca). Tiap pendekatan punya trade-off (ruang vs fleksibilitas grafik).

---

## Lampiran: Command Analisis

```
apt-get install -y default-jre-headless   # java
# unduh jadx via node (curl/wget bermasalah di sandbox)
JAVA_HOME=/opt/java/jdk-21.0.12+8 jadx/bin/jadx --no-res -d OUT APK.apk
aapt dump badging "APK.apk"
aapt dump xmltree "APK.apk" AndroidManifest.xml
```
