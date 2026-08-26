# Perbaikan Estimasi Kesehatan Baterai — Metode Arus Masuk

**Status:** Rencana — belum dikerjakan
**Tujuan:** Memperbaiki perhitungan estimasi kapasitas & skor kesehatan baterai agar tidak selalu menunjukkan 100% dan lebih mencerminkan kondisi aktual baterai.

---

## 1. Masalah

### Kondisi Saat Ini
- Estimasi kapasitas & skor kesehatan dihitung di `BatteryCapacityEstimator`
- Menggunakan `chargeMah` dari `BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER`
- **Masalah:** `chargeMah` adalah **muatan yang tersisa di baterai**, bukan muatan yang masuk saat pengisian
- Akibatnya: perhitungan selalu menghasilkan estimasi = kapasitas desain → skor kesehatan selalu 100%

### Contoh Kasus
Saat baterai mengisi dari 20% ke 80% (delta 60%):
- `segmentStartChargeMah` ≈ 20% dari kapasitas (misal 1000 mAh)
- `endChargeMah` ≈ 80% dari kapasitas (misal 4000 mAh)
- `dCharge` = 4000 - 1000 = 3000 mAh
- `estimate = dCharge × 100 / dPercent = 3000 × 100 / 60 = 5000 mAh`
- **Hasil:** estimasi = kapasitas desain (selalu benar secara matematis, tapi salah secara fisika)

### Bukti dari Pengalaman User
- Menggunakan AccuBattery: kesehatan turun ke 60% saat bypass aktif, lalu "naik" ke 73% saat bypass dinonaktifkan
- Kesehatan baterai **tidak bisa naik** secara fisika → bukti bahwa perhitungan tidak akurat
- FTxT dan Battery Guru menggunakan metode yang sama → selalu menunjukkan 100%

---

## 2. Solusi yang Diusulkan

### Metode Baru: Integrasi Arus × Waktu
Daripada menghitung selisih `chargeMah`, kita hitung **total arus yang benar-benar masuk** ke baterai selama pengisian:

1. **Baca arus (`currentMa`)** dari sysfs atau `BATTERY_PROPERTY_CURRENT_NOW` setiap detik
2. **Kalikan arus × waktu** = mAh yang masuk pada interval tersebut
3. **Akumulasikan** selama proses pengisian berlangsung
4. **Bagi total mAh masuk ÷ delta persen × 100** = estimasi kapasitas aktual

### Analogi Sederhana
Bayangkan mengisi ember air:
- **Metode lama:** Lihat level air saat mulai dan selesai, hitung selisihnya
- **Metode baru:** Ukur berapa liter per menit yang mengalir dari keran, kalikan dengan lama mengisi → ketahui total air yang masuk

Metode baru lebih akurat karena langsung mengukur apa yang masuk, bukan mengira-ngira dari perubahan level.

---

## 3. Fase Pengerjaan

### Fase A — Analisis & Desain (±1–2 jam)
1. Identifikasi sumber arus yang paling akurat di berbagai perangkat:
   - `BATTERY_PROPERTY_CURRENT_NOW` dari `BatteryManager` (API 23+)
   - Sysfs `/sys/class/power_supply/battery/current_now` (fallback)
   - Broadcast `ACTION_BATTERY_CHANGED` dengan extra `EXTRA_CURRENT_NOW`
2. Tentukan format data arus (µA, mA, positif/negatif)
3. Desain mekanisme akumulasi arus dalam `BatteryCapacityEstimator`
4. Tentukan kapan sesi dianggap valid (minimum durasi, minimum delta persen, arus cukup besar)

### Fase B — Implementasi Metode Baru (±3–4 jam)
1. Tambah field akumulasi di `BatteryCapacityEstimator`:
   - `accumulatedChargeMah` — total mAh yang masuk selama sesi
   - `lastCurrentMa` — arus terakhir yang terbaca
   - `lastSampleTimeMs` — waktu sample terakhir
2. Modifikasi `onSample()`:
   - Baca arus dari `BatteryReading.Snapshot.currentMa`
   - Hitung mAh masuk pada interval: `currentMa × (deltaMs / 3600000.0)`
   - Akumulasikan ke `accumulatedChargeMah`
3. Modifikasi `finishSegment()`:
   - Gunakan `accumulatedChargeMah` sebagai dasar perhitungan estimasi
   - `estimate = accumulatedChargeMah * 100f / dPercent`
4. Pastikan arus negatif (discharging) tidak diakumulasikan saat charging

### Fase C — Penanganan Kasus Khusus (±2–3 jam)
1. **Bypass charging:**
   - Deteksi arus masuk sangat kecil (< 100mA) tapi status charging
   - Abaikan sesi ini atau beri flag khusus
2. **Arus tidak terbaca (device tertentu):**
   - Fallback ke metode lama (chargeMah) dengan penanda data tidak akurat
   - Tampilkan disclaimer di UI
3. **Sampling rate:**
   - Pastikan interval sampling tetap ±1 detik saat charging (kritikal untuk akurasi)
4. **Validasi data:**
   - Filter sesi dengan delta persen < 5% atau durasi < 1 menit
   - Filter sesi dengan estimasi di luar rentang wajar (500–30000 mAh)

### Fase D — Testing & Dokumentasi (±2–3 jam)
1. Build + uji manual di beberapa kondisi:
   - Charging normal (AC, USB, Wireless)
   - Bypass charging (jika device mendukung)
   - Device lama dengan arus tidak akurat
2. Bandingkan hasil dengan AccuBattery (jika ada)
3. Update CHANGELOG, README, PANDUAN, STRUKTUR + sinkron ke `app/src/main/assets/`

---

## 4. Estimasi Total

**±8–12 jam kerja aktif (≈1,5–2 hari)** — belum termasuk waktu testing perangkat dan build.

---

## 5. Komponen yang Diperkirakan

| File | Keterangan |
|------|------------|
| `BatteryCapacityEstimator.java` (ubah) | Metode akumulasi arus baru |
| `BatteryReading.java` (ubah) | Pastikan `currentMa` selalu terisi (fallback lebih agresif) |
| `BatteryHealthCardController.java` (ubah) | Tambah indikator kualitas data (akurat/tidak) |
| `BatteryMonitorTabController.java` (ubah) | Pastikan polling tetap 1 detik saat charging |
| `BatteryMonitor.java` (ubah) | Pastikan sample rate kritis dipertahankan |
| Dokumen (ubah) | CHANGELOG, README, PANDUAN, STRUKTUR |

---

## 6. Risiko & Pertimbangan

- **Device-dependent:** Arus (`current_now`) tidak akurat/kosong di sebagian device → perlu fallback yang robust
- **Bypass charging:** Tidak semua device mendukung deteksi bypass → perlu heuristic sederhana
- **Akurasi terbatas:** Bahkan dengan metode baru, hasilnya masih perkiraan (bukan pengukuran laboratorium)
- **Regresi:** Pastikan overlay Battery Info dan tab Monitor tetap menampilkan angka identik (sumber tunggal)
- **Performa:** Akumulasi arus setiap detik tidak boleh membebani CPU/baterai

---

## 7. Alternatif

### Alternatif 1: Perbaikan Ringan (Tanpa Ubah Metode)
- Pertahankan metode `chargeMah` tapi tambahkan validasi dan filter
- Kurangi dampak anomali dengan moving average atau filter outlier
- Estimasi ±2–3 jam, hasil sedikit lebih baik tapi belum optimal

### Alternatif 2: Metode Hybrid
- Gunakan arus dari sysfs saat tersedia, fallback ke `chargeMah` saat tidak
- Kombinasikan kedua metode dengan weighted average
- Estimasi ±5–6 jam, hasil cukup baik untuk mayoritas device

### Alternatif 3: Metode Penuh (Diusulkan)
- Integrasi arus × waktu seperti di atas
- Estimasi ±8–12 jam, hasil terbaik tapi butuh testing lebih banyak

---

## 8. Status

- [ ] Fase A — Analisis & desain
- [ ] Fase B — Implementasi metode baru
- [ ] Fase C — Penanganan kasus khusus
- [ ] Fase D — Testing & dokumentasi
