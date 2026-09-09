# Rencana Refactor: Pecah BatteryCapacityEstimator.java (568 baris)

> Dokumen rencana pengerjaan — **belum ada perubahan kode**.
> Berlaku untuk FTxT versi berjalan.
> **Tanggal:** 2026-09-09

---

## 1. Latar Belakang & Masalah

`BatteryCapacityEstimator` (568 baris) adalah class **static** yang mencampur dua peran dalam satu kelas:

1. **Pelacak segmen pengisian live** — 17 field static segmen (`segmentActive`, `segmentStartMs`, `segmentTotalMs`, `accumulatedChargeMah`, `segTemp*`, `segmentAnyPlugged`, dll), konstanta `MIN_*`/`MAX_*`, `onSample()`, `onMonitoringStopped()`, `isSegmentActive()`, `finishSegment()`, `setActiveSegment()`, `segmentToRow()`, `invalidReasonFor()`, `isScreenOn()`.
2. **Statistik & cache kesehatan** — `designMah`, `sessions`, `loaded`, cache/`computing`, `init()`, `getResult()`, `computeResult()`, `fallbackFromSessions()`, `median()`, `loadFromDb()`, `setDesignCapacity()`, `resetEstimationData()`, `rebuildPendingSessions()`, `migrateLegacyJsonIfNeeded()`.

Kedua peran saling berbagi konstanta dan `sessions`, tapi batas tanggung jawabnya jelas. `BatteryCapacityEstimator` **wajib tetap ada sebagai facade publik** karena dipakai luas dengan prefix `BatteryCapacityEstimator.xxx`:

- `BatteryMonitor.java` → `init`, `onSample`, `onMonitoringStopped`
- `SessionRebuild.java` → `rebuildPendingSessions`
- `DischargeTracker.java` → `getResult`, `HealthResult`
- `BatteryMonitorTabController.java`, `BatteryHealthCardController.java`, `BatterySessionLiveController.java`, `BatterySnapshotExporter.java` → `getResult`, `HealthResult`, `setDesignCapacity`, `resetEstimationData`

Target: `BatteryCapacityEstimator` menjadi facade tipis ±70 baris (delegasi + `HealthResult`), isi dipindah ke dua class baru.

---

## 2. Target Pemisahan

### 2.1 `BatteryCapacityEstimator.java` (facade, ±70 baris) — API publik tetap

Folder: `features/battery/src/main/java/exp/ftxt/features/battery_stats/BatteryCapacityEstimator.java`

Tetap di sini:
- **`public static final class HealthResult`** (tidak dipindah — pemakai UI memanggil sebagai `BatteryCapacityEstimator.HealthResult` di banyak file; mempertahankan ini menghindari perubahan import di ±5 file UI).
- Semua method publik static sebagai **delegasi 1-liner** ke `ChargeSegmentTracker` / `HealthEstimator`:
  - `init(Context)` → `HealthEstimator.init(ctx)`
  - `onSample(Snapshot)` → `ChargeSegmentTracker.onSample(s)`
  - `onMonitoringStopped()` → `ChargeSegmentTracker.onMonitoringStopped()`
  - `isSegmentActive()` → `ChargeSegmentTracker.isActive()`
  - `getResult()` → `HealthEstimator.getResult()`
  - `setDesignCapacity(int)` → `HealthEstimator.setDesignCapacity(mah)`
  - `resetEstimationData()` → `HealthEstimator.resetEstimationData()`
  - `rebuildPendingSessions(long, ArrayList<Segment>)` → `HealthEstimator.rebuildPendingSessions(...)`

**Risiko:** Rendah — tidak ada perubahan pada pemanggil luar (semua referensi `BatteryCapacityEstimator.xxx` tetap valid).

### 2.2 `ChargeSegmentTracker.java` (baru, ±230 baris) — segmen pengisian live

Folder: `features/battery/src/main/java/exp/ftxt/features/battery_stats/ChargeSegmentTracker.java`

Pindah:
- 17 field static segmen (`segmentActive`, `segmentStart*`, `segmentScreenOnMs`, `segmentTotalMs`, `segmentSamples`, `last*`, `accumulatedChargeMah`, `segTemp*`, `segmentAnyPlugged`).
- `onSample(Snapshot)` — kunci state segmen.
- `onMonitoringStopped()` — `finishSegment` + reset `lastSampleTime`.
- `isActive()`.
- `finishSegment(int endPercent)` — hitung valid/invalid, bangun `SessionRow`, **tidak menyentuh DB langsung**; hasil diserahkan ke `HealthEstimator.onSegmentClosed(row)` yang meng-INSERT + `sessions.add`.
- `setActiveSegment(Segment)` — sambung segmen berjalan dari rebuild.
- `segmentToRow(Segment, ...)`.
- `invalidReasonFor(...)`, `isScreenOn()`.

**Interaksi:** Tracker **murni state + klasifikasi**; tidak pegang `appContext`, `sessions`, atau DB. `Context` di-pass saat `init()` (dipanggil dari `HealthEstimator.init`). Konstanta bareng (`MIN_DELTA_PERCENT`, `MIN_ESTIMATE_MAH`, `MAX_ESTIMATE_MAH`, `MIN_SEGMENT_MS`, `INVALID_MIN_MS`) dipakai dari `HealthEstimator` (package-visible).

### 2.3 `HealthEstimator.java` (baru, ±290 baris) — statistik, cache, persistensi

Folder: `features/battery/src/main/java/exp/ftxt/features/battery_stats/HealthEstimator.java`

Pindah:
- Field static `appContext`, `loaded`, `designMah`, `sessions`, `healthCache`, `healthCacheUntil`, `computing`, konstanta `MIN_*`/`MAX_*`/`INVALID_MIN_MS`/`LEGACY_JSON`.
- `init(Context)` → load dari DB + migrate + `getResult()`; sekaligus `ChargeSegmentTracker.init(ctx)`.
- `getResult()` (blok `synchronized` + `wait/notify` utk `computing` **dipertahankan**) → `computeResult()` / `fallbackFromSessions()` / `median()`.
- `loadFromDb()`, `migrateLegacyJsonIfNeeded()`, `setDesignCapacity()`, `resetEstimationData()` (clear `sessions` + hapus DB + cache).
- `rebuildPendingSessions(long, ArrayList<Segment>)` — bagian INSERT DB; segmen berjalan diserahkan ke `ChargeSegmentTracker.setActiveSegment(last)`.
- `onSegmentClosed(SessionRow)` — INSERT ke DB + `sessions.add` (caller: tracker, perilaku identik dengan `finishSegment` asli).

**Catatan perilaku yang dipertahankan:** `onSegmentClosed` **tidak** meng-clear cache (di kode asli, segmen baru tak langsung terlihat sampai TTL 60 detik). Jangan menambah pembaruan cache — itu mengubah perilaku.

---

## 3. Aturan Eksekusi (self-check)

1. **Tidak ada perubahan perilaku** — logika segmen & kalkulasi identik; hanya pindah lokasi.
2. **Signature publik dipertahankan** — `BatteryCapacityEstimator.xxx` (termasuk `HealthResult`) tetap ada sebagai delegasi; pemakai luar **tidak perlu diubah**.
3. **Lock tidak diganti** — `HealthEstimator.getResult()` memakai sinkronisasi + `notifyAll` yang sama seperti sekarang; method `synchronized` (`init`, `onSample`, `onMonitoringStopped`, `setDesignCapacity`, `resetEstimationData`, `rebuildPendingSessions`) tetap `synchronized` di lokasi barunya.
4. **Build hijau** setelah tiap bagian diekstrak (facade delegasi dulu, lalu pindah isi).
5. **Regresi manual:**
   - Kartu Kesehatan: median, design (otomatis & set manual), confidence, `fromScreenOffSessions`.
   - Snapshot Export: nilai health & confidence cocok dengan kartu.
   - Monitor saat charging: segmen aktif (`isSegmentActive`) berjalan saat mengisi.
   - Cabut charger → dalam ±60 detik (TTL cache) hasil segmen baru masuk ke daftar sesi.
   - Bunuh proses tengah charge → rebuild (`SessionRebuild`) menyambung/menulis sesi & tidak dobel.
   - Dialog Reset Data: hapus semua sesi + discharge + healthy → hitung ulang.

---

## 4. Daftar File yang Terlibat

| File | Aksi |
|------|------|
| `features/battery/.../features/battery_stats/BatteryCapacityEstimator.java` | Ubah — jadi facade delegasi + `HealthResult` |
| `features/battery/.../features/battery_stats/ChargeSegmentTracker.java` | Baru — segmen pengisian live |
| `features/battery/.../features/battery_stats/HealthEstimator.java` | Baru — statistik, cache, persistensi |

> Pemanggil luar (`BatteryMonitor`, `SessionRebuild`, `DischargeTracker`, controller UI) **tidak berubah**.

---

## 5. Urutan Pengerjaan

1. Buat `ChargeSegmentTracker` + delegasi `onSample`/`onMonitoringStopped`/`isSegmentActive` di facade → build hijau.
2. Buat `HealthEstimator` + delegasi `init`/`getResult`/`setDesignCapacity`/`resetEstimationData`/`rebuildPendingSessions` → build hijau.
3. Rapikan facade (hapus sisa logika, petakan konstanta bareng ke `HealthEstimator`).
4. Verifikasi manual §3.
5. Update CHANGELOG (entry berjalan — satu poin hasil akhir).

> ⚠️ Sesuai AGENTS/agent-rules: jangan build/commit/tag/push sebelum perintah eksplisit. Eksekusi menunggu persetujuan desain.