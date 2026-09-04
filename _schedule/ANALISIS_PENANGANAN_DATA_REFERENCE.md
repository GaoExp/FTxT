# Analisis Cara Reference App Menangani Data Baterai

**Tanggal:** 2026/09/04
**Metode:** Ekstraksi & dekompilasi statis APK dengan `jadx` (Java). Output dianalisis dari skema SQL (Room / SQLite) dan struktur service/receiver di manifest serta source dekompilasi.
**Sumber:**
- `_temp/sample_apk/Battery/AccuBattery [2.1.8].apk` (`com.digibites.accubattery`)
- `_temp/sample_apk/Battery/Battery Guru.apk` (`com.paget96.batteryguru`, v2.5.0.7)

> Catatan: kedua APK dipakai R8 + obfuscation (nama class/field banyak diganti, mis. `IconCompatParcelizer`). Detail nilai numerik (interval sampling, ambang prune) tidak selalu utuh, tetapi pola arsitektur & skema database jelas terbaca. Dokumen ini fokus pada **strategi penanganan data**, bukan nilai nominal.

---

## Ringkasan Eksekutif

- **Battery Guru** memakai **Room** dengan **tabel eksplisit per konteks** (history / charging / discharging / idle), monitoring di **proses terpisah `:battery_service`**, dan **melakukan pruning data lama** agar DB tidak membengkak.
- **AccuBattery** memakai **SQLite langsung** dengan **time-series dikemas sebagai BLOB** (`timeseries`) plus **tabel kalibrasi hardware** (`deviceconfig`) dan pelacakan **event layar/app** untuk hitung drain.
- **FTxT** sudah meniru pola keduanya (SQLiteOpenHelper, tabel per konteks, sesi charging/discharge, activity log). Dua hal yang FTxT belum lakukan tapi dilakukan reference dan relevan dengan isu freeze/ANR: **pruning/retensi data** dan (opsional) **isolasi proses monitoring**.

---

## 1. Battery Guru (`com.paget96.batteryguru`)

### 1.1 Service & Proses

- **`BatteryInfoService`** (foreground service) berjalan di **proses terpisah `:battery_service`** (`android:process=":battery_service"`, `android:foregroundServiceType` di-set). Isolasi proses ini melindungi proses utama (UI) dari pembebanan monitoring.
- Memakai **Kotlin coroutine scope** (`serviceIoScope`, `statsScope`) untuk kerja asinkron.
- Komponen internal (nama asli tidak utuh karena obfuscation): `batteryInfoHelper`, `batteryStatsManager`, `batterySessionCoordinator`, `serviceStateController`, `serviceDataFlowManager`, `wg6.serviceOverlayController`, `serviceAodController`, `wakelockUtils`.
- **Receiver yang didaftarkan service:**
  - `SCREEN_ON` / `SCREEN_OFF`
  - `DEVICE_IDLE_MODE_CHANGED` / `LIGHT_DEVICE_IDLE_MODE_CHANGED`
  - `ACTION_SHUTDOWN` / `REBOOT`
  - `RESET_BATTERY_STATS` (custom)
  - Alarm/broadcast konfigurasi: `BATTERY_LEVEL_ALARM`, `TEMPERATURE_ALARM`, `BATTERY_DRAINING_ALARM`, `FULL_CHARGING_ALARM`, `OVERLAYS_CONFIGURATION`.
- Receiver statis lain: `BootReceiver` (`BOOT_COMPLETED`, `LOCKED_BOOT_COMPLETED`), `PackageReplacedReceiver`, `PowerDisconnectedReceiver`.

### 1.2 Skema Database (Room) — 6 tabel

Dari `BatteryInfoDatabase_Impl` + query baku di DAO:

- **`BatteryHistoryEntity`** — snapshot berkala per sampling:
  `timeStamp, electric_current, battery_level, battery_voltage, temperature, foreground_app, is_plugged, is_screen_on, battery_status, charger_type`
  → mencatat **konteks layar, charger, dan app depan** pada tiap sampel.
- **`ChargingHistoryEntity`** — sesi pengisian:
  `timeStamp, start_level, end_level, charging_start_time, charging_end_time, charging_time, charging_type, charged_percentage, mah_added, estimated_mah, plug_type`
- **`DischargingHistoryEntity`** — sesi pengosongan, **dipecah screen-on vs screen-off**:
  `discharging_start/end_percentage, discharging_start/end_time, mAh_drained, average_discharge_screen_on, discharging_screen_on_percentage_drain, discharging_runtime_screen_on, average_discharge_screen_off, discharging_screen_off_percentage_drain, discharging_runtime_screen_off, deep_sleep_time, deep_sleep_time_percentage, awake_time, awake_time_percentage, app_usage_data`
  + tabel agregat **`DischargingHistorySummaryEntity`**: `sessions, average_percentage_per_hour_screen_on/off, average_screen_on_time, average_screen_off_time`.
- **`IdleLogEntity`** — log saat idle.
- **`InboxEntity`** — pesan dalam aplikasi.

> Struktur granular ini (mis. `soc_bucket_screen_on_ms`, `cc_start_uah`/`cc_end_uah`, `bypass_ms`) menunjukkan penanganan yang sangat detail — termasuk **SOC bucket** dan **bypass charging**.

### 1.3 Pruning / Retensi Data

DAO (`uj.java`) memuat query penghapusan data lama:
- `DELETE from batteryHistoryEntity where timeStamp <= ?`
- `DELETE from idlelogentity where timeStamp <= ?`
- `DELETE FROM charginghistoryentity WHERE start_time = ?`
- `DELETE FROM discharginghistoryentity WHERE start_time = ?`

→ Guru **menghapus data lama secara berkala** (retensi terkontrol), sehingga query riwayat tidak membengkak.

---

## 2. AccuBattery (`com.digibites.accubattery`)

### 2.1 Service & Proses

- **`StatsService`** (foreground, `FOREGROUND_SERVICE_SPECIAL_USE`). Deskripsi manifest literal:
  > *"Foreground service needed to retrieve sensor data (charge/discharge speed, voltage, current, temperature) while the app is not in the foreground. This data is only available on a polling basis and will be lost if the service is not running."*
- **Polling berkala** via `Handler.postDelayed` (dikonfirmasi di kode wrapper service).
- Receiver:
  - `SystemReceiver`: `BOOT_COMPLETED`, `ACTION_SHUTDOWN`, `MY_PACKAGE_REPLACED`
  - `PowerStateWatcher$Receiver`: `ACTION_POWER_CONNECTED` / `ACTION_POWER_DISCONNECTED`
  - `ChargeAlarmReceiver`: alarm pengisian saat baterai penuh
- **`PowerStateWatcher`** merekam `elapsedRealtime + uptime + powerSource` saat transisi colok/cabut untuk menghitung durasi jumlah colok.

### 2.2 Skema Database (SQLite langsung) — time-series BLOB

- **`timeseries (id, time INTEGER, type TEXT, bytes BLOB)`** — data time-series **disimpan sebagai payload BLOB serialized** (bukan kolom eksplisit). Kompak & hemat ruang, tapi isinya tidak self-describing.
- **`powercycleinfo (id, time UNIQUE, charging TINYINT, json TEXT)`** — info per siklus daya.
- **`deviceconfig (id, sync_id, timestamp, android_sdk, android_build, device_brand, device_model, inverse_polarity BOOL, current_multiplier, current_display_multiplier, voltage_divisor)`** — **kalibrasi pembacaan mA/V** (setiap perangkat punya offset/polaritas berbeda).
- **Pelacakan event layar & app:**
  - `appusageevent`, `appforegroundevent`, `appinteractivityevent` (paket, kelas, foreground/interactive) — dasar hitung drain per-app dan screen on/off.
  - `stringpool` — normalisasi string (dedup) untuk event app.

---

## 3. Perbandingan dengan FTxT (`BatteryHistoryDb.java`)

FTxT memakai `SQLiteOpenHelper` (bukan Room) dengan **5 tabel**: `samples`, `sessions`, `discharge_sessions`, `activity_log`, `meta`.

| Aspek | Battery Guru | AccuBattery | FTxT |
|------|--------------|-------------|------|
| Mekanisme | Room | SQLite langsung | SQLiteOpenHelper |
| Snapshot berkala | `BatteryHistoryEntity` (eksplisit) | `timeseries` (BLOB) | `samples` (eksplisit) |
| Sesi charging | `ChargingHistoryEntity` | (via powercycle) | `sessions` |
| Sesi discharge | `DischargingHistoryEntity` (screen on/off) | (time-series) | `discharge_sessions` |
| Screen/konteks | `is_screen_on`, `foreground_app` | event app | `activity_log` + `ScreenOnOracle` |
| Kalibrasi hardware | — | `deviceconfig` | — (mengandalkan pembacaan mentah) |
| Pruning data lama | ✅ ya | (tidak terlihat eksplisit) | ❌ **tidak** (sengaja) |
| Isolasi proses monitoring | ✅ `:battery_service` | tidak | ❌ |

### Hal yang FTxT belum lakukan tapi dilakukan reference

1. **Pruning / retensi data (Guru).** FTxT sengaja tidak auto-trim (komentar di `BatteryHistoryDb.java:21`). Bila `samples` tumbuh besar, query riwayat (`query`, `queryChart`, `getResult`) bisa lambat — kandidat penyebab ANR yang sedang diperbaiki. Perlu dipertimbangkan menambah ambang retensi (mis. simpan N hari) atau pembersihan berkala di background.
2. **Isolasi proses monitoring (Guru).** Jalankan polling/data-capture di proses terpisah `:battery_service` agar beban di proses utama minimal. Ini trade-off (memori proses tambahan), layak dievaluasi bila beban sampling tinggi.

### Yang FTxT sudah lebih baik / sejajar

- `samples` mencatat lebih banyak metrik per baris daripada `BatteryHistoryEntity` Guru: menambah `power_w`, `charge_mah`, `cycle_count`.
- Sudah punya `activity_log` + `ScreenOnOracle` yang setara dengan pelacakan layar AccuBattery/Guru.
- Sudah memecah sesi pengisian vs pengosongan seperti Guru.

---

## 4. Implikasi untuk FTxT (isu freeze/ANR)

- **Sumber ANR** (ditemukan saat perbaikan iterasi sebelumnya) adalah pemanggilan `getResult()`/query DB berat di thread utama. Reference app menghindari masalah ini dengan: kerja asinkron (coroutine/executor) **dan** pruning agar data tidak membengkak.
- **Rekomendasi lanjutan (belum dieksekusi):** menambahkan retensi/pruning pada `samples`/`activity_log` di background, dengan tetap menjaga riwayat sesi (`sessions`, `discharge_sessions`) karena ukurannya kecil dan merupakan hasil akhir.

---

## Lampiran: Command Analisis

Instal tool (lingkungan ini):
```
apt-get install -y default-jre-headless curl
# java tersedia; unduh jadx via node (curl/wget bermasalah di sandbox ini)
node -e "fetch('URL').then(r=>...)"  # unduh jadx.zip
unzip jadx.zip -d jadx
JAVA_HOME=/opt/java/jdk-21.0.12+8 jadx/bin/jadx --no-res -d OUT APK.apk
```

Cek manifest:
```
aapt dump badging "*.apk"
aapt dump xmltree "*.apk" AndroidManifest.xml
```
