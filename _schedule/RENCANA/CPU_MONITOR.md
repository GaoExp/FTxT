# CPU Monitor — DevInfo/DevCheck Style

**Status:** Rencana — belum dikerjakan
**Tujuan:** Menampilkan informasi CPU perangkat secara real-time, mirip DevInfo/DevCheck/CPU-Z.

---

## 1. Fitur yang Ditampilkan

### 1.1 Info Dasar CPU
- Nama SoC (misal: "MediaTek Dimensity 920")
- Jumlah core (misal: 8 core)
- Arsitektur (misal: 4x Cortex-A78 @2.5GHz + 4x Cortex-A55 @2.0GHz)
- Proses fabrikasi (misal: 6nm)
- GPIO/Board (opsional)

### 1.2 Clock Speed Real-Time
- Frekuensi tiap core (dalam MHz/GHz)
- Update real-time (setiap 1-2 detik)
- Tampilkan apakah core aktif atau sleep

### 1.3 Penggunaan CPU
- Penggunaan total (persentase)
- Penggunaan per core (persentase)
- Load average (1 menit, 5 menit, 15 menit)

### 1.4 Suhu CPU
- Suhu dari thermal zone (jika tersedia)
- Suhu dari BatteryManager (fallback)
- Min/Max/Avg

### 1.5 GPU (Jika Tersedia)
- Nama GPU (misal: Mali-G68 MC4)
- Clock speed GPU
- Penggunaan GPU (jika ada API)

### 1.6 Informasi Lain
- Uptime perangkat
- Kernel version
- Total RAM / Available RAM

---

## 2. Sumber Data

### 2.1 Clock Speed
- Path: `/sys/devices/system/cpu/cpu{N}/cpufreq/scaling_cur_freq`
- Atau: `/sys/devices/system/cpu/cpu{N}/cpufreq/stats/time_in_state`
- Konversi: kHz → MHz/GHz

### 2.2 Penggunaan CPU
- Path: `/proc/stat` → baca idle dan total time per core
- Hitung delta untuk dapat persentase
- Atau: `ActivityManager.getProcessCpuTime()` (API 23+)

### 2.3 Suhu CPU
- Path: `/sys/class/thermal/thermal_zone{N}/temp`
- Atau: `BatteryManager.BATTERY_PROPERTY_TEMPERATURE`
- Konversi: millidegree → derajat Celsius

### 2.4 GPU
- Path: `/sys/class/kgsl/kgsl-3d0/gpuclk` (Qualcomm)
- Atau: `/sys/devices/platform/gpu/power_state` (beberapa device)
- Tidak semua device menyediakan akses

### 2.5 Info SoC
- `Build.HARDWARE` → identifikasi SoC
- `/proc/cpuinfo` → detail core dan arsitektur
- Database SoC (hardcode) untuk mapping nama

---

## 3. Arsitektur Panel

### 3.1 Struktur Panel
```
Panel Home (ringkas)
├── Info Perangkat (model, manufacturer, Android)
├── Ringkasan CPU → ketuk buka Panel CPU Monitor
└── Ringkasan Baterai → ketuk buka Panel Battery Info

Panel Battery Info (sudah ada)
└── Detail baterai lengkap

Panel CPU Monitor (baru)
├── Info dasar SoC
├── Clock Speed per core
├── Penggunaan per core
├── Suhu CPU
├── GPU info (jika tersedia)
└── Load average, uptime, kernel
```

### 3.2 Panel Home
- Tampilan ringkas semua info perangkat
- Card-style layout (mirip tab Monitor)
- Card CPU: nama SoC, clock speed, suhu, persentase penggunaan
- Card Baterai: persentase, suhu, status, estimasi
- Ketuk card → buka panel detail masing-masing

### 3.3 Panel CPU Monitor
- Header: Nama SoC + jumlah core + arsitektur
- Card Clock Speed: bar atau angka per core
- Card Penggunaan: bar progress per core
- Card Suhu: dengan indikator warna (hijau normal, kuning panas, merah kritis)
- Card Info Tambahan: load average, uptime, kernel version, GPU
- Auto-refresh setiap 1-2 detik

### 3.4 Navigasi
- Panel Home bisa diakses dari menu utama atau icon khusus
- Panel CPU Monitor bisa diakses dari Panel Home (ketuk card CPU)
- Panel Battery Info tetap diakses dari tab Monitor seperti sekarang

---

## 4. Implementasi

### 4.1 Foreground Service
- Gunakan service yang sudah ada atau buat service baru
- Polling setiap 1-2 detik saat aktif
- Optimasi: caching path yang valid, skip path yang tidak bisa dibaca

### 4.2 Permission
- Tidak perlu permission khusus untuk baca `/sys/` dan `/proc/`
- Beberapa device mungkin memblokir akses (fallback ke data tersedia)

### 4.3 Kompatibilitas
- Android 7.0+ (API 24)
- Handle device yang tidak menyediakan semua data
- Graceful degradation: tampilkan apa yang tersedia

---

## 5. File yang Perlu Dibuat/Diubah

| File | Keterangan |
|------|------------|
| `CpuMonitorService.java` (baru) | Service untuk baca data CPU |
| `CpuMonitorConfig.java` (baru) | Konfigurasi tampilan |
| `CpuMonitorModule.java` (baru) | Integrasi dengan sistem overlay/tab |
| `CpuMonitorView.java` (baru) | View untuk tampilkan data CPU |
| `panel_cpu.xml` (baru) | Layout panel CPU |
| `MainActivity.java` | Tambah akses ke CPU Monitor |

---

## 6. Estimasi Waktu Pengerjaan

- **CpuMonitorService:** ±4–6 jam
- **CpuMonitorView:** ±4–6 jam
- **Integrasi dengan UI:** ±3–4 jam
- **Testing & optimasi:** ±2–3 jam
- **Total:** ±13–19 jam (≈2–2,5 hari)

---

## 7. Status

- [ ] CpuMonitorService
- [ ] CpuMonitorConfig
- [ ] CpuMonitorModule
- [ ] CpuMonitorView
- [ ] panel_cpu.xml
- [ ] Integrasi dengan MainActivity
- [ ] Testing
