# Rencana Refactor: Pecah BatteryHistoryDb.java (993 baris)

> Dokumen rencana pengerjaan — **belum ada perubahan kode**.
> Berlaku untuk FTxT versi berjalan.
> **Tanggal:** 2026-09-09

---

## 1. Latar Belakang & Masalah

`BatteryHistoryDb` (993 baris) adalah satu kelas `SQLiteOpenHelper` yang merangkap **empat-tugas** dalam satu file:

1. **Schema & lifecycle** — konstanta tabel (`T_SAMPLES`, `T_SESSIONS`, `T_DISCHARGE`, `T_ACTIVITY`, `T_META`), singleton, `onConfigure`, `onCreate`, `onUpgrade` (±165 baris, bertambah tiap migrasi).
2. **Model rows** — enam inner class publik: `SessionRow`, `DischargeSession`, `SessionEntry`, `BarAggregate`, `ChargingSession`, `ActivityLog` (±230 baris). Dipakai luar sebagai `BatteryHistoryDb.X` di ±7 file.
3. **Query & mutasi sampel/chart** — `insertSample`, `querySamples`, `queryLastSamples`, `queryChart`, `queryChartRaw`, `resampleUniform`, `query`, `estimateTimeRemaining`, `deleteSamplesOlderThan`.
4. **Query & mutasi sesi/discharge/agregat/activity/meta** — `insertSession(+Full)`, `getSessions`, `deleteAll*(...)`, `queryChargeSessions`, `insertDischargeSession`, `queryDischargeSessions`, `querySessionEntries`, `lastChargeSessionEnd`, `lastDischargeSessionEnd`, `queryBarAggregates`, `bucketStart`, `screenOnOracle`, `queryActivityLog`, `insertActivityLog`, `setMeta`, `getMeta`, `deleteActivityLogOlderThan`, `pluggedLabel`.

Target: `BatteryHistoryDb` menjadi **facade** ±250 baris (schema + singleton + delegasi), dengan model row dan query dipindah ke file per kelompok. API publik `BatteryHistoryDb.get(ctx).xxx(...)` **dipertahankan** via delegasi sehingga pemanggil luar tidak berubah.

---

## 2. Target Pemisahan

### 2.1 `BatteryModels.java` (baru, ±230 baris) — model row

Folder: `features/battery/src/main/java/exp/ftxt/features/battery_stats/BatteryModels.java`

Pindah ke `public final class BatteryModels` (nested `public static final class`):
- `SessionRow`, `DischargeSession`, `SessionEntry` (beserta `durationMs()`), `BarAggregate`, `ChargingSession`, `ActivityLog`.

**Update referensi luar** (rename mekanis `BatteryHistoryDb.X` → `BatteryModels.X`):
- `DischargeTracker.java` (membuat `DischargeSession`)
- `BatteryCapacityEstimator.java` (membuat/menimpa `SessionRow`, `DischargeSession`) — catatan: file ini sendiri direncanakan dipecah (dokumen terpisah); koordinasi eksekusi agar tidak bentrok.
- `BatterySessionBarChartView.java`, `BatterySessionHistoryController.java` (`BarAggregate`, `SessionEntry`)
- `SessionListActivity.java`, `SessionDetailActivity.java` (`SessionEntry`)
- `BatteryChartDetailActivity.java` (`ActivityLog`)

**Risiko:** Rendah–sedang — perubahan murni mekanis (import + qualification), tanpa ubah logika. Build error jika ada referensi yang terlewat, mudah dilacak.

### 2.2 `SampleQueryDao.java` (baru, ±220 baris) — sampel & chart

Folder: `features/battery/src/main/java/exp/ftxt/features/battery_stats/SampleQueryDao.java`

Pindah:
- `insertSample(Snapshot)`, `querySamples(long)`, `queryLastSamples(int)`
- `queryChart(long, long, int)`, `queryChartRaw(...)`, `resampleUniform(...)`, `query(...)` (private)
- `estimateTimeRemaining(boolean)`
- `deleteSamplesOlderThan(long)`

**Interaksi:** DAO menerima `BatteryHistoryDb` (pemakaian `getReadableDatabase()`/`getWritableDatabase()`) atau `SQLiteDatabase` per pemanggilan — keputusan saat eksekusi. Mutasi yang tadinya `synchronized` tetap `synchronized`.

### 2.3 `SessionQueryDao.java` (baru, ±250 baris) — sesi & discharge

Pindah:
- `insertSession(...)`, `insertSessionFull(SessionRow)`, `getSessions()`
- `deleteAllSessions()`, `deleteAllDischargeSessions()`
- `queryChargeSessions(long, long)`, `insertDischargeSession(DischargeSession)`, `queryDischargeSessions(long, long)`, `querySessionEntries(long, long)`
- `lastChargeSessionEnd()`, `lastDischargeSessionEnd()`
- `pluggedLabel(int)` (helper private)

### 2.4 `AggregateQueryDao.java` (baru, ±160 baris) — agregat, activity, meta

Pindah:
- `queryBarAggregates(long, long, int)` + `bucketStart(long, int)` (helper)
- `insertActivityLog(long, int)`, `queryActivityLog(long, long)`, `deleteActivityLogOlderThan(long)`
- `setMeta(String, String)`, `getMeta(String)`
- `screenOnOracle(long, long)` → struktur `SessionSegmentBuilder.ScreenOnOracle` tetap sama (partial `SQLiteDatabase.Cursor` yang dibungkus oracle tetap dibangun di sini).

### 2.5 Yang tetap di `BatteryHistoryDb` (±250 baris) — facade

- Konstanta `DB_NAME`, `DB_VERSION`, `T_SAMPLES` ... `T_META`
- Singleton `get(Context)`, konstruktor, `onConfigure`, `onCreate`, `onUpgrade`
- **Delegasi 1-liner** untuk seluruh method publik yang dipindah (agar pemakai luar `BatteryHistoryDb.get(ctx).xxx(...)` tidak berubah)

---

## 3. Aturan Eksekusi (self-check)

1. **Tidak ada perubahan perilaku** — isi query identik; hanya pindah lokasi.
2. **API publik dipertahankan** — semua method yang dipindah tetap tersedia via `BatteryHistoryDb` (delegasi); pemakai luar `BatteryHistoryDb.get(ctx).xxx()` **tidak diubah** kecuali referensi model (dokumen §2.1).
3. **Thread-safety dijaga** — method `synchronized` (`insertSample`, `insertSessionFull`, `deleteAllSessions`, `deleteAllDischargeSessions`, `deleteSamplesOlderThan`, `deleteActivityLogOlderThan`, `insertActivityLog`, `setMeta`) **tetap `synchronized`** di lokasi barunya.
4. **Schema utuh** — `onCreate`/`onUpgrade`/`DB_VERSION` tidak diubah; tidak ada migrasi karena ini murni refactor.
5. **Build hijau** setelah tiap langkah diekstrak (model → sample → session → aggregate).
6. **Regresi manual:**
   - Halaman grafik Monitor (chart 5 mnt/1 jam/Harian/Bulanan): titik & garis sama seperti sebelum refactor.
   - Riwayat Sesi (harian/bulanan, list bar): agregat per bucket sama; klik bar membuka detail.
   - Detail sesi: durasi, Δ%, suhu min/max/avg, status valid/invalid + alasan.
   - Discharge sessions: daftar pengosongan & estimasi kesehatan tetap muncul.
   - `estimateTimeRemaining`: estimasi waktu tersisa saat charging/discharging konsisten.
   - Dialog Reset Data: hapus semua → bar kosong → terisi lagi setelah ada segmen baru.

---

## 4. Daftar File yang Terlibat

| File | Aksi |
|------|------|
| `features/battery/.../features/battery_stats/BatteryHistoryDb.java` | Ubah — jadi facade schema + delegasi |
| `features/battery/.../features/battery_stats/BatteryModels.java` | Baru — 6 model row |
| `features/battery/.../features/battery_stats/SampleQueryDao.java` | Baru — sampel & chart |
| `features/battery/.../features/battery_stats/SessionQueryDao.java` | Baru — sesi & discharge |
| `features/battery/.../features/battery_stats/AggregateQueryDao.java` | Baru — agregat, activity, meta |
| 7 file pemakai model (lihat §2.1) | Ubah — rename `BatteryHistoryDb.X` → `BatteryModels.X` |

---

## 5. Urutan Pengerjaan

1. Ekstrak `BatteryModels` + update referensi luar → build hijau.
2. Ekstrak `SampleQueryDao` + delegasi → build hijau.
3. Ekstrak `SessionQueryDao` + delegasi → build hijau.
4. Ekstrak `AggregateQueryDao` + delegasi → build hijau.
5. Rapikan facade (`BatteryHistoryDb`); pastikan seluruh delegasi ada → build hijau.
6. Verifikasi manual §3.
7. Update CHANGELOG (entry berjalan — satu poin hasil akhir).

> ⚠️ Sesuai AGENTS/agent-rules: jangan build/commit/tag/push sebelum perintah eksplisit. Eksekusi menunggu persetujuan desain.