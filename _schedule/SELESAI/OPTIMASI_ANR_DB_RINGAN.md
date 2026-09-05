# OPTIMASI ANR & RINGANNYA DB — FTxT

> Dokumen kerja (bukan changelog). Lokasi: `_schedule/BERJALAN/`.
> Status: **BERJALAN**

## Tujuan
Membuat FTxT bebas ANR saat buka & senyamannya app referensi (AccuBattery / Battery Guru):
- Operasi berat tidak pernah menyumbat main/UI thread proses aplikasi.
- Database riwayat baterai ringan untuk dimuat & di-query.
- Sampling real-time tetap terjaga (overlay & estimasi 1 detik).

## Latar Belakang Singkat
- Sumber ANR awal: `FloatingService.onCreate()` → `BatteryMonitor.start()` memanggil
  `rebuildPendingSessions()` (query 24 jam + segmentasi) **sinkron di main thread**.
- Setelah dipindah ke background thread (`BatterySessionRebuild`), ANR hilang tapi
  masih sempat muncul sekejap sebelum muat selesai.
- Referensi (AccuBattery) tidak ANR karena: kerja berat di background thread &
  dijadwalkan tertunda; service berjalan terpisah dari beban UI.

## Analisis ANR Ulang — 2026/09/04 (sesi ini)

Verifikasi Fase 2 sebelumnya menyatakan "seluruh akses DB dari UI sudah dalam thread
background/executor" — **ternyata TIDAK SEPENUHNYA BENAR**. Masih ada jalur DB write
yang terlewat di main thread.

### DITEMUKAN: 4 jalur DB write di main thread — SEMUA SUDAH DIPINDAHKAN (Fase 5 & 6)

| No | Jalur | File | Status |
|----|-------|------|--------|
| 1 | `BatteryMonitor.stop()` → `BatteryCapacityEstimator.onMonitoringStopped()` | `BatteryCapacityEstimator.java:128` | ✅ Dipindah ke `bgHandler.post()` (Fase 5) |
| 2 | `BatteryMonitor.stop()` → `DischargeTracker.onMonitoringStopped()` | `DischargeTracker.java:82` | ✅ Dipindah ke `bgHandler.post()` (Fase 5) |
| 3 | Dialog "Kapasitas Desain" → `BatteryHealthCardController` | `BatteryCapacityEstimator.java:252` | ✅ Dibungkus `healthExecutor.execute()` (Fase 6) |
| 4 | Dialog "Reset Data" → `BatteryHealthCardController` | `BatteryCapacityEstimator.java:258` | ✅ Dibungkus `healthExecutor.execute()` (Fase 6) |

### DITEMUKAN: Heavy computation di main thread (bukan DB, tapi ANR juga)

| No | Lokasi | Detail | Status |
|----|--------|--------|--------|
| 5 | `BatteryChartView.onDraw()` seri suhu | `ColorUtils.blendARGB()` dipanggil 600-800× per frame. Tiap panggilan = interpolasi 4 channel ARGB. | ✅ Selesai — warna suhu di-cache via LUT (`tempColorLut`), `onDraw()` jauh lebih ringan. |
| 6 | `PresetManager.save()` | Gson serialize + thumbnail PNG + file I/O — semua di main thread. | ✅ Selesai — dipindah ke `saveExecutor` (background thread). |
| 7 | `BatterySessionLiveController.render()` | Rebuild 50-70 view setiap 1 detik (removeAllViews + create baru). | ✅ Selesai — query di executor, render memakai view statis tanpa rebuild berulang. |
| 8 | `SessionListActivity.renderSessions()` | XML inflate loop untuk 50+ sesi di main thread. | ⏳ Masih di UI thread (dipanggil setelah query di executor selesai). Bukan jalur startup — prioritas rendah. |
| 9 | `MemoryMonitor.tick()` (bila background monitor aktif) | `Debug.getMemoryInfo()` + membaca `/proc/meminfo` setiap 1 detik di main thread. | ⏳ Masih di UI thread — beban kecil, bukan jalur startup. Opsional dipindah ke background. |

### Yang SUDAH AMAN (tidak perlu diubah)
- Sampling (`BatteryMonitor.tick()`) → background thread ✅
- Grafik query (`queryChart`, `queryBarAggregates`) → background executor ✅
- Estimator `getResult()` → healthExecutor / monitorExecutor ✅
- SharedPreferences → `.apply()` semua ✅
- `SessionRebuild.run()` → background thread ✅

## Analisis ANR Ulang Lagi — 2026/09/05 (perbaikan notifikasi)

**Gejala:** `TransactionTooLargeException: data parcel size ~1.039.400 bytes` saat
`NotificationManager.notify()` (crash di main thread), dan ANR/perlambatan yang
dirasakan saat aplikasi dibuka.

**Akar masalah:** `NotificationHelper` menyimpan satu objek `RemoteViews` statis
(`cachedContentView`) dan **memodifikasinya langsung** setiap detik
(`setImageViewResource` + `setTextViewText`) untuk menyegarkan notifikasi overlay.
RemoteViews bekerja sebagai daftar aksi: setiap panggilan setter **menambah aksi,
bukan menimpa**. Aksi menumpuk ±2/detik (≈150 B/aksi), parcel membengkak perlahan
hingga menembus batas binder ~1 MB setelah ±1 jam service berjalan → main thread
macet/crash.

**Perbaikan:** `NotificationHelper` diubah agar `cachedContentView` hanya menjadi
**template bersih** (1× dibangun: PendingIntent + ikon statis), lalu setiap build
notifikasi menyalinnya via `RemoteViews.clone()` dan meng-set nilai yang berubah di
salinan (`buildContentView()`). Ukuran parcel per notify sekarang konstan & kecil,
tidak menumpuk.

Status: **selesai.** Ini juga jawaban untuk ANR "saat app dibuka": beban notify 1
detik yang menumpuk ikut menyendat main thread selama startup.

## Keputusan Scope (hasil eksplorasi kode)

### DITEMUKAN: 228 panggilan static `FloatingService.*` dari UI
Eksplorasi menunjukkan UI (`exp/ftxt/ui/*`) melakukan **228 panggilan static** langsung
ke `FloatingService.instance` / `FloatingService.textModule()` / dsb. (`grep` di folder `ui/`).

**Implikasi:** memindahkan `FloatingService` ke proses terpisah (`android:process=":battery"`)
akan membuat seluruh static tersebut menjadi `null` dari proses UI — karena static antar-proses
tidak dibagikan. Ini butuh IPC (Binder) untuk 228 operasi agar fitur overlay/panel tetap jalan.

### Keputusan Fase
| Fase | Rencana semula | Keputusan akhir | Alasan |
|------|----------------|-----------------|--------|
| 1 | Opsi 1 (gabung query rebuild) + Opsi 4 (single-writer DB) | **EKSEKUSI ✅** | Mengurangi beban start ~50%, write serial. Risiko rendah. |
| 2 | single-Executor DB + WAL | **EKSEKUSI (WAL saja) ✅** | WAL diaktifkan. "Single-executor" TIDAK ditambahkan eksplisit karena sudah terpenuhi: seluruh tulis sudah `synchronized` (Fase 1) & seluruh I/O berat sudah di thread background/executor (terverifikasi). Executor global tambahan = over-engineering & berisiko. |
| 3 | event-driven sampling + hapus rekonstruksi | **DITUNDA (belum dieksekusi)** | Event-driven murni TIDAK cocok (overlay butuh polling 1 dtk). Bagian "persist segment aktif" punya manfaat kecil sekarang (query start sudah di background Fase 1) tapi mengubah logika inti estimator tanpa kemampuan build-test — direkomendasikan dikerjakan saat ada sesi test perangkat. |
| 4 | service di proses terpisah | **DIBATALKAN** | 228 panggilan static UI akan putus; butuh IPC masif berisiko merusak puluhan fitur. Tidak sebanding untuk tujuan ANR. |
| 5 | Pindah `onMonitoringStopped()` DB write ke background | **EKSEKUSI ✅** | `BatteryMonitor.stop()` memanggil `onMonitoringStopped()` via `bgHandler.post()`. Lihat Fase 5 di bawah. |
| 6 | Pindah dialog `setDesignCapacity`/`resetEstimationData` ke background | **EKSEKUSI ✅** | Kedua aksi dialog dibungkus `healthExecutor.execute()`. Lihat Fase 6 di bawah. |

**Catatan keputusan penting:** Fase 1 & 2 sudah menuntaskan sumber ANR utama awal
(rekonstruksi dipindah ke background + WAL). Verifikasi Fase 2 terlewat 4 jalur
DB write di main thread — seluruhnya sudah dieksekusi lewat Fase 5 & 6. Penyebab
ANR/perlambatan terbaru (notifikasi yang menumpuk) sudah diperbaiki di sesi
2026/09/05 (lihat Analisis ANR Ulang Lagi).

## Fase 1 — Gabung query rebuild + single-writer DB ✅ (selesai)
- `SessionRebuild.java` (baru): koordinator query 24 jam + segmentasi **sekali**, lalu
  berbagi ke `BatteryCapacityEstimator` & `DischargeTracker`.
- `BatteryCapacityEstimator.rebuildPendingSessions(long lastEnd, List<Segment>)`:
  menerima segmen precomputed (tidak query ulang).
- `DischargeTracker.rebuildPendingSessions(long lastEnd, List<Segment>)`: sama.
- `BatteryMonitor.start()` memanggil `SessionRebuild.run()` sekali di thread `BatterySessionRebuild`.
- `BatteryHistoryDb`: semua method tulis (`insertSample`, `insertSessionFull`,
  `insertDischargeSession`, `insertActivityLog`, `setMeta`, delete*) di-`synchronized`
  pada instance singleton → write serial antar thread (single-writer).

Status: **selesai & tervalidasi via inspeksi (diff).**

## Fase 2 — WAL (selesai) + keputusan single-executor
- `BatteryHistoryDb.onConfigure()` menyalakan `enableWriteAheadLogging()` → baca tidak
  pernah diblokir tulis (reader mendapat snapshot).
- ~~Verifikasi: seluruh akses DB dari UI sudah dalam thread background/executor~~ → **VERIFIKASI INI TIDAK LENGKAP** (lihat Analisis ANR Ulang di atas).
- Keputusan: **tidak menambah executor global** — write sudah serial (Fase 1) & I/O sudah
  latar belakang. Menambah executor hanya menambah latency tanpa manfaat.

Status: **selesai (WAL aktif).**

## Fase 3 (disesuaikan) — Persist segment aktif, hilangkan rekonstruksi 24 jam
**Status: DITUNDA.** Tidak dieksekusi pada sesi ini.

**Alasan:**
1. Rekonstruksi 24 jam sudah dipindah ke background (Fase 1) → bukan lagi sumber ANR.
2. Persist segment aktif mengubah logika inti estimator kapasitas & discharge tracker.
3. Build tidak bisa diverifikasi (rule gradle `deny`) → risiko regresi tak terdeteksi.
4. Manfaat saat ini kecil (hanya mengurangi beban start yang sudah non-blocking).

**Rekomendasi lanjutan:** kerjakan saat ada sesi test perangkat. Desain: simpan state
segment aktif (startTime, startPercent, accumulated mAh, suhu, lastSample) ke tabel `meta`
tiap beberapa detik, muat saat start, resume tanpa query 24 jam.

## Fase 4 — DIBATALKAN
Alasan teknis & data 228 panggilan static di atas. Tidak dieksekusi.

## Fase 5 — Pindah `onMonitoringStopped()` DB write ke background ✅ (selesai)
**Status: SELESAI.**

**Implementasi:** `BatteryMonitor.stop()` sekarang tidak lagi melakukan DB write di
main thread. Seluruh pemanggilan `onMonitoringStopped()` (estimator & discharge
tracker → `finishSegment()` → `insertSessionFull` / `insertDischargeSession`)
dibungkus `bgHandler.post(() -> { ... })` pada thread `batmon-read`, lalu
`bgThread.quitSafely()` setelah selesai. Tidak ada yang menunggu hasil write di main
thread; serialisasi `synchronized` di `BatteryHistoryDb` tetap menangani pesaing
antarthread.

## Fase 6 — Pindah dialog DB write ke background ✅ (selesai)
**Status: SELESAI.**

**Implementasi:** `BatteryHealthCardController` membungkus kedua aksi dialog dengan
`healthExecutor.execute()`:
- `setDesignCapacity()` → jalankan di executor, setelah selesai `runOnUiThread(refresh)`.
- `resetEstimationData()` → jalankan di executor, setelah selesai `runOnUiThread`
  (refresh + toast).
Dialog Reset juga memuat jumlah sesi via `BatteryCapacityEstimator.getResult()`
di executor sebelum menampilkan konfirmasi.

## Perbandingan Dengan Referensi
| Aspek | AccuBattery | FTxT (target) |
|-------|-------------|---------------|
| Sampling | event-driven (`ACTION_BATTERY_CHANGED`) | polling 1 dtk (kebutuhan overlay real-time) |
| Rekonstruksi crash | tidak ada (state persist kontinu) | rekonstruksi dipindah background & di-share (Fase 1) |
| Thread | kernel kerja di proses service/background | sebagian besar I/O di background executor |
| DB | baca tak diblokir tulis (WAL / single writer) | write serial (Fase 1) + WAL (Fase 2) |

## Progres Pengerjaan
- [x] Fase 1: gabung query rebuild + single-writer (selesai & tervalidasi)
- [x] Fase 2: WAL (selesai)
- [ ] Fase 3: persist segment aktif kontinu (DITUNDA — butuh sesi test perangkat)
- [x] Fase 4: dibatalkan (dokumentasi)
- [x] Fase 5: pindah `onMonitoringStopped()` DB write ke background (selesai)
- [x] Fase 6: pindah dialog DB write ke background (selesai)
- [x] Item 5-6: LUT suhu grafik & PresetManager di executor (selesai)
- [x] Item 7: view kartu sesi statis tanpa rebuild (selesai)
- [x] Notifikasi: template RemoteViews + clone (perbaikan TransactionTooLarge, 2026/09/05)
- [ ] Item 8: `SessionListActivity.renderSessions()` pindah ke background/recycler (opsional — bukan jalur startup)
- [ ] Item 9: `MemoryMonitor` polling pindah ke background (opsional — beban kecil)
- [x] Self-check diff

## Catatan
- Waktu: 2026/09/04 (sesi pengerjaan) & 2026/09/05 (perbaikan notifikasi, verifikasi Fase 5/6 selesai)
- Build tidak bisa diverifikasi (rule gradle `deny`); verifikasi via inspeksi diff/grep.
