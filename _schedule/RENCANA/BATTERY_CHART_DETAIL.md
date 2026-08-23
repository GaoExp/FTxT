# Rencana Halaman Detail Grafik Baterai FTxT

> **Tanggal:** 2026-08-24
> **Referensi Desain:** Battery Guru (Material Design 3, grafik besar + crosshair + chip rentang + kartu statistik)
> **Status:** Rencana (belum eksekusi)

---

## 1. Ringkasan

Kelima grafik riwayat baterai di tab Monitor panel Battery Info — **Persentase** (di kartu Metrik Real-Time) serta **Suhu, Daya, Tegangan, Arus** (grid 2×2 di kartu Grafik Riwayat) — saat ini hanya bisa dilihat dalam ukuran kecil tanpa interaksi. Rencana ini menambahkan **halaman detail fullscreen per metrik**: ketuk salah satu grafik → halaman tersendiri berisi grafik besar, crosshair sentuhan (nilai + jam tepat pada titik data), pemilih rentang waktu milik halaman, dan kartu statistik periode (Min / Max / Rata-rata / Δ).

Implementasi dikerjakan **satu grafik per tahap**, dimulai dari **Persentase** sebagai pembuktian pola; empat grafik grid menyusul mereplikasi pola yang sama.

---

## 2. Keputusan Desain (terkunci dari diskusi)

| # | Keputusan | Alasan |
|---|-----------|--------|
| 1 | Halaman detail berupa **Activity fullscreen terpisah** | Konsisten dengan DocumentationActivity; bebas konflik gesture scroll panel |
| 2 | Rentang waktu di halaman detail **independen** dari slider global panel | Konteks halaman = bedah satu metrik mendalam |
| 3 | **Pinch-zoom ditunda** ke iterasi lanjutan | Iterasi pertama fokus ke crosshair + statistik |
| 4 | **Tanpa library eksternal** — extend `BatteryChartView` (Canvas) | Mengikuti pola project (grafik existing juga Canvas murni) |
| 5 | Visual terinspirasi Battery Guru: header beraksen warna metrik, grafik besar ±40% tinggi layar, chip rentang, kartu statistik monospace | Referensi desain user |

---

## 3. Kondisi Teknis Saat Ini (titik integrasi)

Komponen yang relevan:

- `exp/ftxt/shared/ui/BatteryChartView.java` — custom View line chart Canvas. API: `setSeriesType(int)`, `setData(BatteryReading.Snapshot[])`, `setWindowMs(long)`. Belum punya touch handling sama sekali. Konstanta window: `WINDOW_2M … WINDOW_24H`; konstanta seri: `SERIES_TEMP/SERIES_PERCENT/SERIES_POWER/SERIES_VOLTAGE/SERIES_CURRENT`.
- `exp/ftxt/ui/BatteryChartHistoryController.java` — binding kelima grafik + slider rentang global (`CHART_WINDOWS` 10 pilihan, query `BatteryHistoryDb.queryChart(from, now, 600)` via executor tunggal `chartExecutor`, hasil diposting ke UI handler).
- `exp/ftxt/ui/BatteryMonitorTabController.java` — orkestrasi tab Monitor (ring gauge, metrik real-time, polling 1 detik saat tab tampil); memegang controller-chart & health.
- `panel_battery.xml` — ID grafik: `batChartPercentView` (kartu Metrik Real-Time, line ~90); `batChartTempView`, `batChartPowerView`, `batChartVoltageView`, `batChartCurrentView` (grid 2×2, line ~463+).
- `BatteryHistoryDb.queryChart(fromMs, toMs, targetPoints)` — sudah downsampling bucket + resample grid seragam; aman dipanggil ulang untuk halaman detail.
- Skala Y per seri sudah ditangani view (Persen 0–100; Suhu patok bawah 45° melebar otomatis; lainnya auto + padding 8%).

Implikasi: **logika skala, format nilai (`fmt`), dan warna seri sudah milik `BatteryChartView`** — halaman detail cukup reuse instance view yang sama dengan ukuran lebih besar; tidak perlu duplikasi rumus.

---

## 4. Rancangan Halaman Detail

### 4.1 Struktur Layar (`BatteryChartDetailActivity`)

```
┌──────────────────────────────────┐
│ ←  Suhu                          │  Header: tombol back + judul metrik,
│    45.0° · maksimum skala 45°C   │  subjudul nilai terkini (warna aksen metrik)
├──────────────────────────────────┤
│                                  │
│         GRAFIK BESAR             │  BatteryChartView, lebar penuh,
│      (±40% tinggi layar)         │  tinggi ±200dp, mode interaktif ON
│                                  │
├──────────────────────────────────┤
│ [2m][5m][10m][15m][30m][1j][3j]… │  Chip rentang waktu (milik halaman,
│                                  │  independen dari slider global panel)
├──────────────────────────────────┤
│ ┌──────────────────────────────┐ │
│ │ Min     Max     Rata-rata  Δ │ │  Kartu statistik periode tampil,
│ │ 31.2°   38.9°   34.7°  −2.1° │ │  angka monospace ala tab Monitor
│ └──────────────────────────────┘ │
└──────────────────────────────────┘
```

- Judul & warna header mengikuti seri (`SERIES_PERCENT` → "Persentase", dst) — warna aksen pakai color resource yang sama dengan garis chart (`bat_chart_percent`, `bat_chart_temp`, …).
- Subjudul header menampilkan nilai terkini ter-format (reuse format `fmt`).

### 4.2 Interaksi Crosshair

- Ditambahkan **di dalam** `BatteryChartView` (digambar sendiri via Canvas — tanpa popup View terpisah):
  - `ACTION_DOWN/MOVE` → cari sampel dengan X terdekat dari jari → simpan `selectedIndex`.
  - `onDraw` menggambar: garis vertikal tipis pada titik terpilih, dot penebal, bubble label **nilai + jam** (`HH:mm:ss`) dekat titik (auto flip kiri/kanan agar tak terpotong tepi).
  - `ACTION_UP/CANCEL` → crosshair tetap menempel di titik terakhir (readout statis) — konsisten dengan gaya readout Battery Guru.
- Mode interaksi dikontrol flag baru `setInteractive(boolean)`:
  - **OFF (default)** — perilaku persis seperti sekarang; dipakai oleh kelima grafik di grid/kartu Metrik.
  - **ON** — hanya di halaman detail.
- Listener opsional `setOnScrubListener((index, snapshot) -> …)` untuk halaman detail (misal menampilkan nilai terpilih di header); tidak wajib bagi fungsi inti.

### 4.3 Chip Rentang Waktu

- Pilihan identik `CHART_WINDOWS` (2 Menit – 24 Jam), dirender sebagai deretan chip horizontal-scrollable (LinearLayout horizontal di HorizontalScrollView) — bukan seekbar, meniru gaya chip Battery Guru.
- State rentang halaman **independen**; default saat membuka halaman = window yang sedang aktif di panel (dikirim via Intent extra) supaya konteks nyambung.
- Ganti chip → re-query database (executor halaman sendiri) → redraw + hitung ulang statistik.

### 4.4 Kartu Statistik Periode

- Dihitung **dari data yang sedang tampil** (bukan query tambahan): `Min`, `Max`, `Rata-rata`, `Δ` (nilai akhir − nilai awal periode; bertanda +/−).
- Format angka reuse logika `fmt` per seri (°C, %, V, mA/A, W).
- Layout: satu kartu (`bat_card_bg`) berisi baris label + baris angka monospace 4 kolom, sama gayanya dengan grid metrik real-time.

### 4.5 Live Update

- Selama halaman terbuka dan activity resumed: re-query `queryChart` tiap **5 detik** (handler + executor halaman) sehingga titik terbaru ikut.
- Saat crosshair sedang aktif (jari menekan), refresh ditunda sampai jari dilepas — mencegah titik bergeser di bawah crosshair.
- `selectedIndex` di-clamp ulang ke rentang data baru setelah refresh.

---

## 5. Komponen Baru & Yang Diubah

**File baru:**

| File | Isi |
|------|-----|
| `app/src/main/java/exp/ftxt/ui/BatteryChartDetailActivity.java` | Halaman detail fullscreen: binding, chip rentang, executor + polling 5 detik, kartu statistik |
| `app/src/main/res/layout/activity_battery_chart_detail.xml` | Layout halaman (header, chart besar, chip scroll, kartu statistik) |

**File diubah:**

| File | Perubahan |
|------|-----------|
| `BatteryChartView.java` | Flag `interactive`, touch handling crosshair + penggambaran bubble, listener scrub opsional |
| `AndroidManifest.xml` | Registrasi `BatteryChartDetailActivity` |
| `BatteryChartHistoryController.java` | Pasang click-listener pada 4 grafik grid → buka detail |
| `BatteryMonitorTabController.java` | Pasang click-listener pada grafik Persentase → buka detail |

Helper statis peluncur: `BatteryChartDetailActivity.start(activity, seriesType, initialWindowMs)` agar wiring tiap grafik cuma satu baris.

---

## 6. Sistem Kerja Tahap demi Tahap

Prinsip: **satu grafik = satu tahap = satu unit kerja yang bisa diuji sendiri** sebelum lanjut. Tahap berikutnya tidak dimulai sebelum tahap sebelumnya dinyatakan oke oleh user.

### ✅ Tahap 1 — Persentase (kartu Metrik Real-Time) — *pembuktian pola*

Seluruh infrastruktur dibangun di tahap ini; grafik Persentase jadi integrator pertama.

- [ ] `BatteryChartView`: tambahkan flag `interactive` + touch handling crosshair + penggambaran garis/bubble/dot + listener scrub
- [ ] Buat `activity_battery_chart_detail.xml` (header, chart besar, chip rentang scrollable, kartu statistik)
- [ ] Buat `BatteryChartDetailActivity` (binding, helper `start()`, chip rentang independen, query via executor, polling 5 detik, perhitungan statistik Min/Max/Rata-rata/Δ)
- [ ] Daftarkan activity di `AndroidManifest.xml`
- [ ] Wiring klik `batChartPercentView` → buka detail `SERIES_PERCENT` (window awal = window panel aktif)
- [ ] Uji manual: buka/tutup, ganti rentang, crosshair, statistik masuk akal, tema gelap/terang, rotasi layar tidak crash

**Kriteria selesai:** ketuk grafik Persentase → halaman detail hidup penuh; kelima perilaku di atas lulus; grafik lain belum berubah perilaku.

### ⬜ Tahap 2 — Suhu (grid 2×2)

- [ ] Wiring klik `batChartTempView` → detail `SERIES_TEMP`
- [ ] Verifikasi skala khas Suhu (patok 45°, melebar otomatis) tampil benar di ukuran besar; format °C di crosshair & statistik
- [ ] Uji manual serupa Tahap 1

### ⬜ Tahap 3 — Daya (grid 2×2)

- [ ] Wiring klik `batChartPowerView` → detail `SERIES_POWER`
- [ ] Verifikasi format W di crosshair & statistik (desimal 2 angka untuk <10W)

### ⬜ Tahap 4 — Tegangan (grid 2×2)

- [ ] Wiring klik `batChartVoltageView` → detail `SERIES_VOLTAGE`
- [ ] Verifikasi format V di crosshair & statistik

### ⬜ Tahap 5 — Arus (grid 2×2)

- [ ] Wiring klik `batChartCurrentView` → detail `SERIES_CURRENT`
- [ ] Verifikasi format mA↔A otomatis di crosshair & statistik

### 📌 Tahap lanjut (ditunda, lihat §7)

- Pinch-zoom + pan pada grafik halaman detail.

**Aturan antar-tahap:** setiap tahap selesai → self-check `git diff` (scope hanya file daftar §5 + wiring grafik itu) → laporkan ke user → tunggu arahan lanjut. CHANGELOG dicatat sekali per iterasi kerja sesuai AGENTS.md (tanpa file build/dokumen internal).

---

## 7. Yang Ditunda (iterasi lanjutan)

- **Pinch-zoom & pan** di grafik halaman detail (dua jari perbesar rentang waktu, geser menjelajah, double-tap reset) — butuh transformasi sumbu-X sendiri di `BatteryChartView`.
- Opsi jenis grafik (garis/bar) ala Battery Guru — evaluasi setelah pola detail stabil.
- Ekspor/share gambar grafik.

---

## 8. Risiko & Catatan

1. **Konflik gesture tidak relevan** di halaman detail (layar sendiri) — inilah alasan jalur Activity dipilih.
2. **Rotasi layar**: activity akan recreate — state (rentang chip) disimpan ulang via `onSaveInstanceState` agar tidak reset ke default.
3. **Executor halaman** terpisah dari `chartExecutor` panel; wajib `shutdown()` di `onDestroy()` agar tidak bocor thread.
4. **Bubble crosshair di tepi**: label auto-flip kiri/kanan & atas/bawah relatif titik agar tak terpotong padding view.
5. **Data kosong**: halaman detail tetap tampil dengan pesan "Belum ada data grafik" + statistik strip (—) — reuse pesan empty existing.
6. **Tema gelap/terang**: semua warna baru wajib punya pasangan `values-night` (mengikuti pola `bat_monitor_*`).
