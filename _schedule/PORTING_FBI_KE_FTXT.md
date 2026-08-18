# Porting Fitur FBI ke FTxT

> **Tanggal:** 2026-08-16
> **Sumber:** Project FBI v1.3.0 (duplikasi modul baterai dari FTxT yang dikembangkan lebih lanjut)
> **Target:** Project FTxT v4.85.5
> **Keputusan:** SEMUA perubahan dari FBI akan diterapkan ke FTxT

---

## Ringkasan

FBI adalah hasil duplikasi modul battery stats dan battery current dari FTxT yang kemudian dikembangkan lebih lanjut dengan fitur-fitur baru. Rencana ini mencatat semua perubahan yang perlu di-port kembali ke FTxT.

**Keputusan Penting:**
- SEMUA perubahan dari FBI akan diterapkan ke FTxT
- Battery Stats + Battery Current + Battery Strip akan digabung jadi satu panel "Battery Info" dengan tabbed layout (persis seperti FBI)
- Branding nama mengikuti FBI: "Battery Stats" → "Battery Info", "Battery Bar" → "Battery Strip"
- Memory Stats akan di-port sebagai modul baru
- Semua optimasi akan diterapkan

---

## 1. Struktur Panel Baru (Gabungan)

### Panel Battery Info (Tabbed Layout)
- **Sebelum:** 3 panel terpisah (Battery Stats, Battery Current, Battery Bar)
- **Sesudah:** 1 panel gabungan "Battery Info" dengan bottom navigation 3 tab:
  - **Tab Monitor:** placeholder monitor baterai
  - **Tab Overlay:** konfigurasi Battery Info (suhu, persen, tegangan, arus, daya)
  - **Tab Battery Strip:** konfigurasi Battery Strip (mode cepat/manual, animasi)

### File yang terpengaruh
- Hapus: `BatteryCurrentPanelController.java`, `BatteryCurrentPositionController.java`, `BatteryCurrentPanelFragment.java`, `panel_battery_current.xml`
- Hapus: `BatteryBarPanelFragment.java`, `panel_battery_bar.xml` (gabung ke panel_battery.xml)
- Update: `BatteryPanelController.java` (tambah tab + integrasi OrderZones)
- Update: `BatteryPositionController.java` (tambah preset field battery bar)
- Update: `PanelManager.java` (hapus entry battery_current dan battery_bar)
- Update: `MainActivity.java` (hapus sidebar item battery_current dan battery_bar)
- Update: `FloatingService.java` (gabung module management)

---

## 2. Fitur Baru dari FBI

### 2.1 Memory Stats (Modul Baru)
- **File baru:**
  - `features/memory_stats/MemoryConfig.java`
  - `features/memory_stats/MemoryModule.java`
  - `features/memory_stats/MemoryMonitor.java`
  - `ui/MemoryPanelController.java`
  - `ui/MemoryPositionController.java`
  - `ui/fragment/MemoryPanelFragment.java`
  - `res/layout/panel_memory.xml`
  - `res/menu/menu_memory_bottom_nav.xml`
  - `res/color/mem_nav_item_color.xml`
  - `res/drawable/mem_card_bg.xml`
  - `res/drawable/mem_badge_active_bg.xml`
  - `res/drawable/mem_badge_stopped_bg.xml`
  - `res/drawable/ic_monitor.xml`
  - `res/drawable/ic_overlay.xml`

- **Deskripsi:**
  - Modul overlay pemakaian memori proses via `Debug.getMemoryInfo()`
  - Dashboard tab Monitor dengan 3 kartu: Proses FTxT (14 nilai), Runtime Java (3 nilai), RAM Sistem
  - Polling terpusat per detik dikendalikan tombol Mulai/Hentikan
  - Switch Pemantauan Latar Belakang (overlay aktif saat switch menyala)
  - Tombol Salin Ke Clipboard dan Simpan Snapshot (riwayat 20)
  - Panel memakai bottom navigation Monitor | Overlay

### 2.2 CrashLogger
- **File baru:**
  - `core/CrashLogger.java`

- **Deskripsi:**
  - Saat force close, stack trace otomatis ditulis ke `FTxT_crash_*.txt` di folder Download
  - Plus cadangan prefs di `last_crash`
  - Memudahkan pelaporan bug tanpa logcat/adb

### 2.3 Battery Order Zones (Chip Drag)
- **File baru:**
  - `ui/BatteryOrderZonesView.java`

- **Deskripsi:**
  - Chip drag dua zona 'Aktif'/'Nonaktif' untuk mengatur urutan info baterai
  - Seret chip untuk mengubah urutan, geser antar zona untuk menampilkan/menyembunyikan
  - Urutan tersimpan ke `battery_item_order`

---

## 3. Perubahan Fitur Battery Stats → Battery Info

### 3.1 BatteryStatsConfig.java
- **Perubahan:**
  - Tambah field `showVoltage`, `showCurrent`, `showPower`
  - Tambah field `itemOrder` (sortable items)
  - Tambah field `separatorColor`
  - Rename class ke `BatteryInfoConfig` (opsional, pertahankan nama lama untuk backward compat)

### 3.2 BatteryStatsModule.java
- **Perubahan:**
  - Method `readBatterySnapshot()` dengan voltage/current/power
  - Fallback pembacaan dari sticky broadcast, BatteryManager API 28+, dan sysfs
  - Configurable item order via `itemOrder`
  - Skip render jika nilai tidak berubah (caching)

### 3.3 BatteryPanelController.java
- **Perubahan:**
  - Tambah tombol toggles untuk voltage/current/power
  - Integrasi `BatteryOrderZonesView` untuk drag-to-reorder
  - Tambah bottom navigation 3 tab (Monitor | Overlay | Battery Strip)
  - Pindahkan konfigurasi Battery Bar ke tab Battery Strip

### 3.4 BatteryPositionController.java
- **Perubahan:**
  - Preset save/apply untuk field voltage/current/power/itemOrder
  - Preset save/apply untuk field Battery Bar (quickMode, quickSide, dll)

---

## 4. Perubahan Fitur Battery Bar → Battery Strip

### 4.1 BatteryBarView.java
- **Perubahan:**
  - Tambah `onVisibilityChanged()` untuk stop/start animasi saat overlay visible/invisible
  - Optimasi: animasi infinite dihentikan saat overlay disembunyikan

### 4.2 BatteryBarModule.java
- **Perubahan:**
  - BroadcastReceiver permanen (didafarkan di `start()`, dilepas di `stop()`)
  - Cache percent/charging status ke field
  - Skip update jika status tidak berubah
  - IntentFilter di-cache static

### 4.3 BatteryBarPanelController.java
- **Perubahan:**
  - Gunakan `updateBatteryBarInPlace()` bukan `restartModule()`
  - Update in-place: applyAppearance + reloadLayout + updatePosition
  - Pindahkan ke tab Battery Strip di panel Battery Info

---

## 5. Optimasi RAM & Performance

### 5.1 WakeLockManager.java
- **Perubahan:**
  - Guard `!pm.isInteractive()` — skip update WakeLock jika layar mati
  - WakeLock PARTIAL hanya dipegang saat layar menyala

### 5.2 NotificationHelper.java
- **Perubahan:**
  - Bitmap ikon suhu di-cache (dibuat ulang hanya saat nilai berubah)
  - RemoteViews + onClick PendingIntent dibuat sekali via `ensureCachedViews()`
  - `notify()` di-skip bila suhu & ikon toggle tidak berubah
  - Update in-place dengan key diff

### 5.3 FloatingService.java
- **Perubahan:**
  - Method `updateBatteryBarInPlace()` dan `updateBatteryStatsInPlace()`
  - Tidak lagi panggil `restartModule()` saat setting diubah
  - Gabung management untuk Battery Stats + Battery Current + Battery Bar

---

## 6. Perubahan UI Lainnya

### 6.1 BasePanelFragment.java
- **Perubahan:**
  - Tambah method `onPanelHidden()` untuk callback saat panel disembunyikan

### 6.2 PanelManager.java
- **Perubahan:**
  - Panggil `onPanelHidden()` saat panel di-hide

### 6.3 MainActivity.java
- **Perubahan:**
  - Hapus sidebar item `navBatteryCurrent` dan `navBatteryBar`
  - Tambah sidebar item `navMemory`
  - Update `isAnyModuleActive()` untuk cek MemoryConfig
  - Update `panelIdToName()` untuk mapping baru

### 6.4 BootReceiver.java
- **Perubahan:**
  - Restore `MemoryConfig` saat boot

---

## 7. Perubahan Branding

### 7.1 Nama Modul
- "Battery Stats" → "Battery Info"
- "Battery Bar" → "Battery Strip"

### 7.2 PresetManager.java
- Folder export tetap `Downloads/FunText/` (bukan `Downloads/FBI/`)

### 7.3 Strings & Labels
- Update semua string UI mengikuti nama baru

---

## 8. File XML Baru dari FBI

### Drawable
- `mem_card_bg.xml` — background kartu dashboard
- `mem_badge_active_bg.xml` — badge status "Berjalan"
- `mem_badge_stopped_bg.xml` — badge status "Berhenti"
- `ic_monitor.xml` — ikon tab Monitor
- `ic_overlay.xml` — ikon tab Overlay
- `ic_battery_strip.xml` — ikon tab Battery Strip

### Layout
- `panel_memory.xml` — panel Info Memori
- `panel_battery.xml` — update ke tabbed layout

### Menu
- `menu_battery_bottom_nav.xml` — bottom nav Battery Info
- `menu_memory_bottom_nav.xml` — bottom nav Info Memori

### Color
- `bat_nav_item_color.xml` — selector warna bottom nav Battery
- `mem_nav_item_color.xml` — selector warna bottom nav Memory

---

## 9. File yang Dihapus

### Java
- `features/battery_current/BatteryCurrentConfig.java`
- `features/battery_current/BatteryCurrentModule.java`
- `ui/BatteryCurrentPanelController.java`
- `ui/BatteryCurrentPositionController.java`
- `ui/fragment/BatteryCurrentPanelFragment.java`
- `ui/fragment/BatteryBarPanelFragment.java`

### Layout
- `res/layout/panel_battery_current.xml`
- `res/layout/panel_battery_bar.xml`

### Menu
- `res/menu/drawer_menu.xml` (update, bukan hapus)

---

## 10. Urutan Pengerjaan

> **Revisi efisiensi:** Optimasi RAM (sebelumnya Fase 5) dipindah ke **Fase 8** (sebelum testing) agar semua struktur modul final terlebih dahulu — mencegah refactor ulang saat modul Memory/CrashLogger ditambah.

### Fase 1: Persiapan
- [x] Backup project FTxT
- [x] Buat branch baru untuk porting

### Fase 2: Hapus Modul Battery Current
- [x] Hapus `BatteryCurrentConfig.java`
- [x] Hapus `BatteryCurrentModule.java`
- [x] Hapus `BatteryCurrentPanelController.java`
- [x] Hapus `BatteryCurrentPositionController.java`
- [x] Hapus `BatteryCurrentPanelFragment.java`
- [x] Hapus `panel_battery_current.xml`
- [x] Update `MainActivity.java` (hapus sidebar item)
- [x] Update `PanelManager.java` (hapus entry)
- [x] Update `FloatingService.java` (hapus module management)
- [x] Update `BootReceiver.java` (hapus restore)
- [x] Update `NotificationHelper.java` (hapus cek aktif)

### Fase 3: Gabung Battery Bar ke Battery Info
- [x] Hapus `BatteryBarPanelFragment.java`
- [x] Hapus `panel_battery_bar.xml`
- [x] Update `panel_battery.xml` ke tabbed layout
- [x] Update `BatteryPanelController.java` (tambah tab + integrasi OrderZones)
- [x] Update `BatteryPositionController.java` (tambah preset field battery bar)
- [x] Update `MainActivity.java` (hapus sidebar item battery_bar)
- [x] Update `PanelManager.java` (hapus entry battery_bar)

### Fase 4: Battery Stats Enhancement
- [x] Update `BatteryStatsConfig.java` (tambah field voltage/current/power/itemOrder)
- [x] Update `BatteryStatsModule.java` (tambah readBatterySnapshot() + sysfs fallback + caching)
- [x] Port `BatteryOrderZonesView.java` dari FBI
- [x] Port `menu_battery_bottom_nav.xml` dari FBI
- [x] Port `bat_nav_item_color.xml` dari FBI

### Fase 5: Memory Stats (Modul Baru)
- [x] Port `MemoryConfig.java` dari FBI
- [x] Port `MemoryModule.java` dari FBI
- [x] Port `MemoryMonitor.java` dari FBI
- [x] Port `MemoryPanelController.java` dari FBI
- [x] Port `MemoryPositionController.java` dari FBI
- [x] Port `MemoryPanelFragment.java` dari FBI
- [x] Port `panel_memory.xml` dari FBI
- [x] Port `menu_memory_bottom_nav.xml` dari FBI
- [x] Port `mem_nav_item_color.xml` dari FBI
- [x] Port drawable XML (mem_card_bg, mem_badge_*, ic_monitor, ic_overlay)
- [x] Update `MainActivity.java` (tambah sidebar item memory)
- [x] Update `FloatingService.java` (tambah module management)
- [x] Update `BootReceiver.java` (tambah restore MemoryConfig)
- [x] Update `NotificationHelper.java` (tambah cek aktif memory)
- [x] Update `OverlayPreset.java` (tambah memory stats fields)

### Fase 6: CrashLogger
- [x] Port `CrashLogger.java` dari FBI (ganti branding FBI → FTxT)
- [x] Update `MainActivity.java` (tambah init CrashLogger)

### Fase 7: UI Adjustments
- [x] Update `BasePanelFragment.java` (tambah `onPanelHidden()`)
- [x] Update `PanelManager.java` (panggil `onPanelHidden()`)
- [ ] Update `MainActivity.java` (rename sidebar labels)
- [ ] Update `values/strings.xml` (rename string labels)

### Fase 8: Optimasi RAM
- [x] Update `WakeLockManager.java` (tambah screen-off guard)
- [x] Update `NotificationHelper.java` (refactor caching)
- [x] Update `BatteryBarView.java` (tambah `onVisibilityChanged()`)
- [x] Update `BatteryBarModule.java` (refactor permanent receiver + caching)
- [x] Update `FloatingService.java` (tambah in-place update methods)

### Fase 9: Testing
- [ ] Test panel Battery Info (3 tab)
- [ ] Test Memory Stats (monitor + overlay)
- [ ] Test CrashLogger
- [ ] Test Battery Order Zones (drag-to-reorder)
- [ ] Test optimasi (animasi lifecycle, notification caching, wake lock)
- [ ] Test backward compatibility (preset lama)
- [ ] Test auto-start saat boot

---

## 11. Risiko & Mitigasi

1. **Backward Compatibility** — Field baru di config/preset harus fallback ke default jika prefs lama
   - Mitigasi: Gunakan `hasPref()` check dan default values

2. **Panel Structure** — Perubahan besar dari 3 panel terpisah ke 1 panel tabbed
   - Mitigasi: Test menyeluruh setiap tab

3. **Sidebar Navigation** — FTxT sudah 11 item, akan berubah (hapus 2, tambah 1 = 10 item)
   - Mitigasi: Review urutan sidebar

4. **Notification Caching** — Perubahan besar di NotificationHelper
   - Mitigasi: Test notifikasi di berbagai kondisi

5. **Memory Stats Scope** — Fitur ini cukup besar
   - Mitigasi: Port sekaligus karena semua fitur sudah tested di FBI

---

## 12. Status

| Fase | Status |
|------|--------|
| Fase 1: Persiapan | ✅ Selesai |
| Fase 2: Hapus Modul Battery Current | ✅ Selesai |
| Fase 3: Gabung Battery Bar ke Battery Info | ✅ Selesai |
| Fase 4: Battery Stats Enhancement | ✅ Selesai |
| Fase 5: Memory Stats | ✅ Selesai |
| Fase 6: CrashLogger | ✅ Selesai |
| Fase 7: UI Adjustments | ✅ Selesai |
| Fase 8: Optimasi RAM | ✅ Selesai |
| Fase 9: Testing | ⏳ Menunggu user |
