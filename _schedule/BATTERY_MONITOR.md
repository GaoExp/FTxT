# Rencana Battery Monitor — Tab Monitor Panel Battery Info

> **Tanggal:** 2026-08-21
> **Versi Target:** v4.88.0 – v4.90.0 (setelah v4.87.0)
> **Status:** Rencana (belum eksekusi)

---

## 1. Latar Belakang

Tab **Monitor** di panel Battery Info saat ini masih placeholder. Rencana ini mengisinya dengan fitur monitoring baterai ala **Battery Guru / AccuBattery**, mengikuti pola yang sudah ada di Memory Stats (background monitor, snapshot export/copy, OrderZones).

Tanpa alarm charging (dikesampingkan atas keputusan user).

---

## 2. Ruang Lingkup & Batasan Android

**Yang bisa dilakukan tanpa root:**
- Baca metrik via `BatteryManager`: suhu, %, voltase, arus, daya, charge counter (mAh), cycle count (API 34+, tergantung vendor), status & tipe charging, teknologi baterai.
- Estimasi kapasitas riil dari laju pengisian (Δcharge counter vs Δ% selama charging) — akumulasi statistik, butuh beberapa sesi charging sebelum stabil.
- Grafik riwayat sampling berkala.

**Batasan:**
- Arus (`current_now`) tidak akurat/kosong di sebagian device (tergantung kernel).
- Tidak ada API resmi "kesehatan baterai" di mayoritas device — hanya estimasi.
- Kapasitas desain tidak tersedia via API publik → input manual user (default kosong; skor kesehatan hanya muncul jika diisi).

---

## 3. Tahap 1 — Monitoring Real-Time (v4.88.0)

Fondasi fitur. Mengisi tab Monitor dengan daftar metrik lengkap + kondisi baterai.

**Metrik yang ditampilkan:**
- Suhu (°C), Persentase (%), Voltase (V), Arus (mA), Daya (W) — sumber bacaan sama dengan overlay
- Kapasitas tersisa (mAh dari `BATTERY_PROPERTY_CHARGE_COUNTER`)
- Cycle count (API 34+, sembunyikan jika tidak tersedia)
- Status charging: penuh / mengisi (AC / USB / Wireless / Cepat) / tidak mengisi
- Teknologi baterai (Li-ion / Li-Po dari `EXTRA_TECHNOLOGY`)
- Indikator kondisi berdasar ambang suhu: Normal / Panas / Dingin (badge berwarna)

**Perilaku:**
- Update real-time saat panel terlihat (pola `onPanelShown()`/`onPanelHidden()`)
- Background monitor opsional (toggle) yang tetap jalan meski service overlay mati — pola `MemoryMonitor`
- Tombol Export/Copy snapshot seperti tab Monitor Memory Stats

**File yang direncanakan:**
- 🗒️ `features/battery_stats/BatteryMonitor.java` — background polling + snapshot builder
- ✏️ `ui/BatteryPanelFragment.java` — hidupkan tab Monitor (hapus placeholder)
- ✏️ `res/layout/panel_battery.xml` — layout tab Monitor (daftar metrik, badge kondisi, bar level)
- ✏️ `core/FloatingService.java`, `MainActivity.java` — wiring background monitor (pola memory)

---

## 4. Tahap 2 — Grafik Riwayat (v4.89.0)

Line chart riwayat metrik selama monitoring aktif.

**Fitur:**
- Custom view Canvas multi-seri: Suhu / % / Daya (pilih seri yang tampil), sumbu waktu otomatis
- Sampling 1 detik saat monitor aktif, buffer memori ±1 jam (3600 titik), auto-trim
- Kontrol: pause/resume, reset, pilih rentang (5m / 15m / 1j)
- Tanpa library chart eksternal — konsisten dengan prinsip minim dependency project

**File yang direncanakan:**
- 🗒️ `shared/ui/BatteryChartView.java` — custom view line chart
- ✏️ `features/battery_stats/BatteryMonitor.java` — buffer sampling + akses data untuk chart
- ✏️ `res/layout/panel_battery.xml`, `ui/BatteryPanelFragment.java` — integrasi chart + kontrol

---

## 5. Tahap 3 — Estimasi Kapasitas & Kesehatan (v4.90.0)

Fitur ala AccuBattery: estimasi kapasitas aktual & skor kesehatan.

**Cara kerja:**
1. Selama device charging, catat sampel (charge counter, %, waktu) tiap interval
2. Per segmen pengisian: ΔmAh vs Δ% → estimasi kapasitas penuh sesi tersebut
3. Akumulasi lintas sesi → ambil median (tahan outlier), simpan persisten (JSON di internal storage)
4. Skor kesehatan = estimasi median ÷ kapasitas desain × 100%

**Syarat & tampilan:**
- Input manual kapasitas desain (mAh) di tab Monitor — skor kesehatan hanya dihitung jika terisi
- Kartu hasil: estimasi kapasitas, kapasitas desain, skor kesehatan, jumlah sesi charging terkumpul, indikator keyakinan (jumlah sampel)
- Reset data akumulasi (dengan konfirmasi)

**File yang direncanakan:**
- 🗒️ `features/battery_stats/BatteryCapacityEstimator.java` — akumulasi sesi charging + persistensi JSON
- ✏️ `features/battery_stats/BatteryMonitor.java` — hook event charging start/stop
- ✏️ `res/layout/panel_battery.xml`, `ui/BatteryPanelFragment.java` — kartu hasil + input kapasitas desain

---

## 6. Catatan Versioning

| Tahap | Versi | Alasan |
|-------|-------|--------|
| 1 | v4.88.0 | Fitur baru (monitoring real-time) |
| 2 | v4.89.0 | Fitur baru (grafik riwayat) |
| 3 | v4.90.0 | Fitur baru (estimasi kapasitas & kesehatan) |

Tiap tahap di-commit & tag terpisah agar mudah di-roll-back jika ada masalah di device tertentu.

---

## 7. Risiko & Mitigasi

- **Arus tidak tersedia di sebagian device** → tampilkan "—" dan tetap hitung daya dari voltase × arus alternatif (charge counter delta) bila memungkinkan.
- **Cycle count hanya API 34+ / vendor tertentu** → sembunyikan baris metrik, bukan tampil 0.
- **Estimasi kapasitas bias saat layar nyala / beban tinggi** → tandai sampel yang diambil saat layar menyala, beri bobot lebih pada sampel layar mati (mirip pendekatan AccuBattery).
- **Polling boros baterai** → background monitor default OFF, interval bisa diatur, stop otomatis saat battery low (opsional).
