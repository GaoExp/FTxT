# Rencana Refactor: Pecah BatteryChartView.java (871 baris)

> Dokumen rencana pengerjaan — **belum ada perubahan kode**.
> Berlaku untuk FTxT versi berjalan.
> **Tanggal:** 2026-09-09

---

## 1. Latar Belakang & Masalah

`BatteryChartView` (871 baris) adalah custom View `extends View` yang mencampur **tiga peran** dalam satu kelas:

1. **Gesture & viewport** — `onTouchEvent`, `selectNearest`, `notifyScrub`, `applyPinchZoom`, `panBy`, listener `ScaleGestureDetector`, konstanta `GESTURE_*`, state viewport (`setViewport`, `clearViewport`, `getVisible*`, `getZoomLevel`, `setZoomLevel`).
2. **Rendering** — `onDraw` (±200 baris), `drawCrosshair`, `drawTimeLabels`, `applySeriesStyle`, grid & sumbu.
3. **Helper statis** — `valueOf`, `formatValue`, `computeTempColor`, cache LUT gradien suhu (`tempColorLut`, `tempAnchors`), `seriesColorRes`.

Dua kelompok pertama (gesture 1) dan helper statis (3) bisa diekstrak sebagai class terpisah, menyisakan View yang fokus pada `onDraw()`.

---

## 2. Target Pemisahan

### 2.1 `ChartSeriesUtil.java` (baru, ±120 baris) — helper statis murni

Folder: `app/src/main/java/exp/ftxt/ui/ChartSeriesUtil.java`

Pindah (semua `static`, tanpa state instance):
- `valueOf(int seriesType, BatteryReading.Snapshot s)`
- `formatValue(int seriesType, float v)`
- `seriesColorRes(int seriesType)`
- `computeTempColor(float c, int[] anchors)`
- Cache gradien suhu: `tempColorLut`, `tempAnchors`, `tempColorLut(Context)`

**Risiko:** Rendah — fungsi deterministik murni, tidak menyentuh canvas/state. Pemanggil di class lain di-update ke `ChartSeriesUtil.xxx(...)`.

### 2.2 `ChartGestureController.java` (baru, ±300 baris) — gesture & viewport

Folder: `app/src/main/java/exp/ftxt/ui/ChartGestureController.java`

Pindah:
- Konstanta `GESTURE_NONE/TAP_OR_PAN/TRACKER/PAN/TRACKER_DRAG`, `MIN_ZOOM`, `MIN_VISIBLE_MS`
- `onTouchEvent(MotionEvent)` logic, `applyPinchZoom`, `panBy`, `selectNearest`, `notifyScrub`
- Listener `ScaleGestureDetector.OnScaleGestureListener`
- State viewport (`visibleStartT`, `visibleEndT`, `dataStartT`, `dataEndT`, zoom) + semua getter/setter

**Interaksi:** Controller memanggil balik View untuk invalidate (`View.invalidate()`) dan membaca data titik untuk `selectNearest`. Pass `View` via konstruktor; firing `notifyScrub` tetap tersambung ke listener View.

### 2.3 Yang tetap di BatteryChartView (±450 baris)

- `onDraw()` lengkap (grid, garis seri, sumbu, area plot)
- `drawCrosshair(...)`, `drawTimeLabels(...)`, `applySeriesStyle()`
- Painjpaint state (gridPaint, linePaint, dotPaint, crosshairPaint, dll)
- Construktor & `init(Context)`
- Delegasi getter viewport → `ChartGestureController`

---

## 3. Aturan Eksekusi (self-check)

1. **Tidak ada perubahan perilaku** — logika gesture/rendering identik; hanya pindah lokasi.
2. **Signature publik dipertahankan** — `BatteryChartDetailActivity` memakai banyak getter viewport (`getVisibleStartMs`, `getVisibleEndMs`, `getZoomLevel`, `setViewport`, `setZoomLevel`, `hasViewport`, dll). Semua method publik View **tetap ada** sebagai delegasi ke `ChartGestureController`.
3. **`getMinVisibleMs()`** — jika dipakai luar, pertahankan sebagai delegasi ke konstan controller.
4. **Build hijau** setelah tiap bagian diekstrak.
5. **Regresi manual** — halaman detail grafik: pinch zoom (fokus jari terjaga), pan, tracker menempel di jari (crosshair), statistik Min/Max/Δ mengikuti rentang tampil, warna segmen mengikuti kondisi, ganjalan data (gap >5 menit) tetap memutus garis.

---

## 4. Daftar File yang Terlibat

| File | Aksi |
|------|------|
| `app/src/main/java/exp/ftxt/ui/BatteryChartView.java` | Ubah — potong gesture & helper, delegasi |
| `app/src/main/java/exp/ftxt/ui/ChartSeriesUtil.java` | Baru — helper seri statis + cache LUT suhu |
| `app/src/main/java/exp/ftxt/ui/ChartGestureController.java` | Baru — gesture & viewport |

---

## 5. Urutan Pengerjaan

1. Ekstrak `ChartSeriesUtil` (risiko rendah) → build hijau.
2. Ekstrak `ChartGestureController` → build hijau → uji zoom/pan/tracker.
3. Rapikan `BatteryChartView` (delegasi viewport, hapus kode lama).
4. Verifikasi manual §3.
5. Update CHANGELOG (entry berjalan — satu poin hasil akhir).

> ⚠️ Sesuai AGENTS/agent-rules: jangan build/commit/tag/push sebelum perintah eksplisit. Eksekusi menunggu persetujuan desain.