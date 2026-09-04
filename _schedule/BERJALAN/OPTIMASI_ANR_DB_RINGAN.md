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

**Catatan keputusan penting:** sumber ANR sudah dituntaskan oleh Fase 1 (rekonstruksi dipindah ke background + query dijalankan sekali). Fase 3 lanjutan & 4 bukan lagi kebutuhan untuk mencegah ANR — jadi tidak dieksekusi pada sesi ini demi tidak merusak fitur working.

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
- Verifikasi: seluruh akses DB dari UI sudah dalam thread background/executor
  (`BatterySnapshotExporter.exporterExecutor`, `BatterySessionLiveController.executor`,
  `BatteryChartHistoryController` AsyncTask, dll). Tidak ada query berat di main thread.
- Keputusan: **tidak menambah executor global** — write sudah serial (Fase 1) & I/O sudah
  latar belakang. Menambah executor hanya menambah latency tanpa manfaat.

Status: **selesai.**

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

## Perbandingan Dengan Referensi
| Aspek | AccuBattery | FTxT (target) |
|-------|-------------|---------------|
| Sampling | event-driven (`ACTION_BATTERY_CHANGED`) | polling 1 dtk (kebutuhan overlay real-time) |
| Rekonstruksi crash | tidak ada (state persist kontinu) | rekonstruksi dipindah background & di-share (Fase 1) |
| Thread | kernel kerja di proses service/background | semua I/O di background executor |
| DB | baca tak diblokir tulis (WAL / single writer) | write serial (Fase 1) + WAL (Fase 2) |

## Progres Pengerjaan
- [x] Fase 1: gabung query rebuild + single-writer (selesai & tervalidasi)
- [x] Fase 2: WAL + verifikasi I/O tidak di main thread (selesai)
- [ ] Fase 3: persist segment aktif kontinu (DITUNDA — butuh sesi test perangkat)
- [x] Fase 4: dibatalkan (dokumentasi)
- [x] CHANGELOG + versionCode (241)
- [x] Self-check diff

## Catatan
- Waktu: 2026/09/04 (sesi pengerjaan)
- Build tidak bisa diverifikasi (rule gradle `deny`); verifikasi via inspeksi diff/grep.
