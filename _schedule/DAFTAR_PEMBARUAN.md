# Daftar Pembaruan — Urutan Prioritas

---

## Prioritas (Urgent → Tidak Urgent)

### 1. MASALAH_MEMORI_DAN_PROSES
**Status:** Paling Urgent
**Dampak:** Stabilitas & performa aplikasi
**Masalah:**
- WakeLock selalu aktif → menguras baterai
- Semua 7 modul di-init tanpa cek enabled
- Foreground service jalan terus meskipun tidak ada overlay aktif
- Static instance mencegah garbage collection

**Solusi:**
- Conditional WakeLock (hanya aktif jika ada modul aktif)
- Lazy initialization modul
- Cleanup modul saat stop
- Conditional BroadcastReceiver

---

### 2. REFACTOR_FLOATING_SERVICE
**Status:** Urgent
**Dampak:** Maintainability & scalability
**Masalah:**
- 717 baris kode dengan 70+ method delegasi statis duplikasi
- Setiap modul punya ~10 method dengan struktur sama
- Tambahan modul baru = copy-paste 10+ method

**Solusi:**
- Buat interface `OverlayModule`
- Implement di semua modul (7 modul)
- Ganti delegasi statis dengan generic method
- Estimasi: -400 baris kode

---

### 3. REFACTOR_COLOR_PICKER_DIALOG
**Status:** Urgent
**Dampak:** Maintainability & readability
**Masalah:**
- 928 baris kode
- Method `show()` dengan 20-30 parameter
- Parameter passing panjang = rawan salah argument order

**Solusi:**
- Buat class `ColorPickerConfig` dengan builder pattern
- Update method `show()` signature
- Update 7 panel controller pemanggil

---

### 4. NOTIFICATION_ACTIONS
**Status:** Penting
**Dampak:** User experience
**Manfaat:**
- Toggle overlay tanpa buka aplikasi
- Quick preset switch
- Stop service langsung dari notifikasi

**Implementasi:**
- Buat `NotificationActionReceiver.java`
- Update `NotificationHelper.buildNotification()`
- Register receiver di AndroidManifest.xml
- Maksimal 3 aksi per notifikasi (batasan Android)

---

### 5. PANEL_NAVIGATION_FRAGMENT
**Status:** Penting tapi Tidak Urgent
**Dampak:** Code organization
**Masalah:**
- 9 variable View terpisah
- `hideAllPanels()` hardcode 9 baris
- Sidebar click handler hardcode 9 if-else
- Tambah panel baru = update di 3 tempat

**Solusi:**
- Convert panel ke Fragment
- Buat `PanelManager` untuk navigasi
- Buat `BasePanelFragment` sebagai base class
- Update `activity_main.xml` ke FrameLayout container

---

### 6. GROUPED_OVERLAY_POSITION
**Status:** Fitur Baru
**Dampak:** User experience (opsional)
**Konsep:**
- Beberapa overlay digabung posisinya
- Saat satu digeser, yang lain ikut bergerak
- Offset antar overlay dalam group tetap

**Contoh:**
- BatteryPercentage + BatteryTemperature digabung
- NetworkStats + Clock digabung

---

### 7. SHIZUKU_FPS_READER
**Status:** Eksperimental
**Dampak:** Fitur khusus (gamer)
**Kompleksitas:**
- Integrasi Shizuku SDK
- Deteksi aplikasi foreground (UsageStats)
- Baca FPS via `dumpsys gfxinfo`
- Parsing output yang bervariasi tiap device
- Butuh Shizuku aktif + izin khusus

---

## Ringkasan Eksekusi

**Tahap 1: Stabilitas**
- [ ] Optimasi memori & baterai (MASALAH_MEMORI_DAN_PROSES)
- [ ] Refactor FloatingService (interface OverlayModule)

**Tahap 2: Maintainability**
- [ ] Refactor ColorPickerDialog (ColorPickerConfig builder)

**Tahap 3: Fitur Baru**
- [✓] Notification Actions (tombol aksi di notifikasi)

**Tahap 4: Refactor Lanjutan**
- [ ] Panel Navigation Fragment (jika diperlukan)

**Tahap 5: Fitur Opsional**
- [ ] Grouped Overlay Position
- [ ] Shizuku FPS Reader (eksperimental)
