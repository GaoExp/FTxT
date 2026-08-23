# Rencana Split Multi-Module FTxT

> **Tanggal:** 2026-08-17
> **Revisi:** 2026-08-23 — disesuaikan dengan kondisi project v4.88.1 (tab Monitor Battery Info, refactor controller, panel Debugging, pemakaian resource lintas fitur)
> **Versi Target:** Setelah v4.88.1
> **Status:** Rencana (belum eksekusi)

---

## 1. Mengapa Multi-Module?

Saat ini FTxT single-module — semua 88 file Java, 21 layout, dan 50 drawable berada dalam satu module `app/`. Beberapa alasan untuk split:

1. **Build lebih cepat** — Gradle cuma compile ulang module yang berubah, bukan seluruh project.
2. **Ekstraksi fitur lebih mudah** — Kalau mau port suatu fitur ke project lain, tinggal copy module-nya, tidak perlu membongkar puzzle yang saling terhubung.
3. **Dependency lebih jelas** — Tiap module declare sendiri apa yang ia butuhkan, dependensi tidak lagi implisit lewat import statement.
4. **Maintenance lebih terarah** — Bug di fitur battery tidak perlu sentuh kode memory atau clock.

---

## 2. Struktur Module yang Direncanakan

```
FTxT/
├── settings.gradle              ← include semua module
├── build.gradle                 ← root build config
│
├── shared/                      ← SHARED LIBRARIES
│   ├── config/                  ← Model data dasar
│   │   └── build.gradle
│   ├── ui/                      ← Komponen UI bersama (+ resource lintas fitur)
│   │   └── build.gradle
│   ├── color/                   ← Color picker + math + warna bersama
│   │   └── build.gradle
│   └── preset/                  ← Sistem preset
│       └── build.gradle
│
├── features/                    ← FITUR-FITUR
│   ├── floating_text/
│   │   └── build.gradle
│   ├── fps/
│   │   └── build.gradle
│   ├── clock/
│   │   └── build.gradle
│   ├── battery/                 ← Gabungan paket fisik battery_stats + battery_bar + tab Monitor
│   │   └── build.gradle
│   ├── network/
│   │   └── build.gradle
│   └── memory/
│       └── build.gradle
│
├── core/                        ← SERVICE LAYER
│   └── build.gradle
│
└── app/                         ← SHELL APLIKASI
    └── build.gradle
```

Catatan: `features/color_picker/` tidak jadi module sendiri — isinya menyatu ke `:shared:color` (lihat 3.3).

---

## 3. Isi Tiap Module

### 3.1 `:shared:config` — Model Data Dasar

**Dependency internal:** 0 (leaf node)

| File | Penjelasan |
|------|-----------|
| `ShadowConfig.java` | Model data shadow (color, blur, offset) |
| `BackgroundConfig.java` | Model data background (enable, color, padding, offset, margin, radius) |

**Lokasi fisik saat ini:** kedua file ada di `exp/ftxt/shared/ui/` (bukan folder sendiri) — ekstraksi = buat folder/package baru `shared/config/`.

**Resource:** Tidak ada (pure Java model).

**Pemicu ekstraksi:** Mudah, zero dependency. Basis untuk semua module lain.

---

### 3.2 `:shared:ui` — Komponen UI Bersama

**Dependency internal:** `:shared:config` (ShadowConfig)

| File | Penjelasan |
|------|-----------|
| `OverlayModule.java` | Interface untuk semua modul overlay |
| `ShadowTextView.java` | Custom TextView dengan shadow + background |
| `OverlayShadow.java` | Apply elevation-based shadow |
| `OverlayDragHandler.java` | Touch listener untuk drag overlay |
| `DpadController.java` | Kontrol D-Pad dengan repeat |
| `SliderPositionController.java` | Slider X/Y normalized 0-1000 |
| `SliderLabelEditor.java` | Dialog edit nilai numerik dari label slider |
| `SectionHelper.java` | Utility collapsible section |
| `PresetPreviewView.java` | Custom View preview posisi overlay |
| `BasePanelFragment.java` | Abstract base Fragment untuk semua panel (fisik: `exp/ftxt/ui/`) |
| `BatteryChartView.java` | Custom View line chart Canvas riwayat metrik baterai (baru v4.88.0, fisik: `exp/ftxt/shared/ui/`) |

**Resource:**
- Drawable: `ic_arrow_up.xml`, `ic_arrow_down.xml`, `ic_arrow_left.xml`, `ic_arrow_right.xml`, `seekbar_thumb.xml`, `ic_edit.xml`
- Drawable lintas fitur: `ic_monitor.xml`, `ic_overlay.xml` — ikon tab Monitor/Overlay yang **dipakai BERSAMA** oleh panel Battery Info (`menu_battery_bottom_nav.xml`) dan Memory Stats (`menu_memory_bottom_nav.xml`). Harus pindah ke sini agar tidak duplikasi/konflik antar feature module.
- Layout: Tidak ada (base class saja)

**Pemicu ekstraksi:** Sedang. `ShadowTextView` adalah komponen inti yang dipakai oleh hampir semua feature module. Setelah module ini ada, feature modules bisa depend ke sini.

---

### 3.3 `:shared:color` — Color Picker + Math

**Dependency internal:** 0 (leaf node — ColorMath tidak depend ke mana-mana)

| File | Penjelasan |
|------|-----------|
| `ColorMath.java` | Operasi matematika HSV (fisik: `exp/ftxt/shared/color/`) |
| `ColorNameResolver.java` | Deteksi nama warna dari RGB (fisik: `exp/ftxt/shared/color/`) |
| `HSVColorPickerView.java` | Custom View color wheel HSV (fisik: `exp/ftxt/shared/color/`) |
| `TriangleColorPickerView.java` | Custom View segitiga HSV (fisik: `exp/ftxt/features/color_picker/`) |
| `ColorPickerDialog.java` | Dialog color picker gabungan (fisik: `exp/ftxt/shared/ui/`) |
| `ColorPickerPanelController.java` | UI panel color picker (fisik: `exp/ftxt/ui/`) |
| `ColorPickerPanelFragment.java` | Fragment wrapper panel color picker (fisik: `exp/ftxt/ui/fragment/`) |
| `BatteryColors.java` | Helper warna gradien hue baterai, satu sumber untuk Battery Strip & ring gauge (baru v4.88.0, fisik: `exp/ftxt/shared/color/`) |

**Resource:**
- Layout: `panel_color_picker.xml`, `dialog_color_picker.xml`
- Drawable: `ic_edit.xml`, `seekbar_thumb.xml`
- Color: Tidak ada khusus

**Pemicu ekstraksi:** Sedang. `ColorPickerDialog` dipanggil dari beberapa panel controller lain (misal `BatteryBarPanelController`, `TextPanelController`). Setelah split, module ini harus di-depend oleh feature modules yang butuh color picker.

**Catatan:** `features/color_picker/` (isinya hanya TriangleColorPickerView) ikut melebur ke sini — tidak dibuat module tersendiri.

---

### 3.4 `:shared:preset` — Sistem Preset

**Dependency internal:** `:shared:config` (ShadowConfig)

| File | Penjelasan |
|------|-----------|
| `OverlayPreset.java` | Model data preset dengan UUID, metadata, history |
| `PresetManager.java` | CRUD preset (save/load/rename/reorder/search/export/import) |
| `PresetHandler.java` | Delegate pattern per modul |
| `PresetBrowserDialog.java` | DialogFragment browser preset |

**Catatan:**
- `PresetExampleActivity.java` (fisik masih di `exp/ftxt/shared/preset/`) → pindah ke `:app` (depend ke `FloatingService` + `TextConfig`)

**Resource:**
- Layout: `dialog_preset_browser.xml`, `preset_browser_item.xml`
- Drawable: `ic_star_filled.xml`, `ic_star_outline.xml`, `vertical_divider.xml`

**Pemicu ekstraksi:** Sedang. `OverlayPreset` punya banyak field yang refer ke feature-specific data (battery bar fields, memory fields), tapi semua dalam bentuk primitive — tidak depend ke feature module.

---

### 3.5 `:core:service` — Service Layer

**Dependency internal:** Semua feature modules + `:shared:ui`

| File | Penjelasan |
|------|-----------|
| `FloatingService.java` | Foreground service, kelola semua overlay |
| `NotificationHelper.java` | Custom notification + ikon dinamis Bitmap suhu (caching) |
| `BootReceiver.java` | Restore module overlay saat boot |
| `NotificationActionReceiver.java` | Handle aksi notifikasi |
| `WakeLockManager.java` | Partial wake lock (screen-off guard) |
| `CrashLogger.java` | Crash logger ke folder Download |

**Catatan penting:**
- `FloatingService` adalah "God class" — depend ke semua feature modules + configs.
- Sejak v4.88.0, lifecycle monitor baterai **sudah tidak lagi** lewat FloatingService/NotificationHelper/BootReceiver-syarat — `BatteryMonitorService` dikelola langsung oleh `MainActivity` & `BootReceiver` (yang ada di `:app`). Service itu sendiri masuk `:feature:battery` (lihat 3.6), sehingga `:core:service` tetap bisa diekstrak tanpa menarik detail monitor.
- Module ini TIDAK bisa di-extract duluan — harus menunggu semua feature modules ada.

**Resource:**
- Layout: `notification_custom.xml`
- Drawable: `ic_notification_*.xml`, `ic_close.xml`

**Pemicu ekstraksi:** Sulit. Ini yang paling tightly coupled. Ekstraksi harus dilakukan **terakhir**.

---

### 3.6 Feature Modules (masing-masing)

Semua feature modules mengikuti pola yang sama:

```
features/<name>/
├── <Name>Config.java        ← Konfigurasi statis
├── <Name>Module.java        ← Logic overlay (update, render)
├── <Name>PanelController.java   ← UI panel handler
├── <Name>PositionController.java ← Posisi + preset
├── <Name>PanelFragment.java     ← Fragment wrapper
└── panel_<name>.xml             ← Layout panel
```

#### `:feature:floating-text`
- **Dependency:** `:shared:ui`, `:shared:config`, `:shared:preset`, `:shared:color`
- **File:** TextConfig, TextModule, TextPanelController, TextPositionController, TextPanelFragment
- **Resource:** `panel_text.xml`

#### `:feature:fps`
- **Dependency:** `:shared:ui`, `:shared:config`, `:shared:preset`
- **File:** FpsConfig, FpsModule, FpsPanelController, FpsPositionController, FpsPanelFragment
- **Resource:** `panel_fps.xml`

#### `:feature:clock`
- **Dependency:** `:shared:ui`, `:shared:config`, `:shared:preset`
- **File:** ClockConfig, ClockModule, ClockPanelController, ClockPositionController, ClockPanelFragment
- **Resource:** `panel_clock.xml`

#### `:feature:battery` (paling kompleks — gabungan paket fisik `battery_stats` + `battery_bar`)
- **Dependency:** `:shared:ui`, `:shared:config`, `:shared:preset`, `:shared:color`
- **Overlay inti (fisik: `features/battery_stats/`, `features/battery_bar/`):**
  - BatteryStatsConfig, BatteryStatsModule
  - BatteryBarConfig, BatteryBarView, BatteryBarModule
  - BatteryReading — util pembaca metrik tunggal (intent + property + sysfs) untuk overlay & tab Monitor
- **Tab Monitor (baru v4.88.0–v4.88.1):**
  - BatteryMonitor — polling & pencatatan metrik ke database (sampling dinamis)
  - BatteryHistoryDb — database SQLite riwayat baterai (time-series + sesi pengisian + resample)
  - BatteryCapacityEstimator — estimasi kapasitas & skor kesehatan ala AccuBattery
  - BatteryRingView — custom view ring gauge melingkar
  - BatteryMonitorTabController — orkestrasi tab Monitor (ring, metrik real-time, polling)
  - BatteryChartHistoryController — kartu Grafik Riwayat (grid 2×2 + slider rentang)
  - BatteryHealthCardController — kartu Kesehatan Baterai + dialog kapasitas desain
  - BatterySnapshotExporter — pembuat teks salinan/ekspor snapshot
  - BatteryMonitorService — foreground service ringan monitor full-aktif (auto-start app & boot); CUMA depend ke battery_stats + R, jadi ikut module ini (bukan `:core:service`)
- **UI panel (fisik: `exp/ftxt/ui/`):**
  - BatteryOrderZonesView, BatteryPanelController, BatteryBarPanelController, BatteryPositionController, BatteryBarPositionController, BatteryPanelFragment
- **Resource:** `panel_battery.xml` (tabbed Monitor/Overlay/Battery Strip), `menu_battery_bottom_nav.xml`, `bat_nav_item_color.xml`, `bat_card_bg.xml`, `ic_battery_strip.xml`
- **Catatan:** Ikon tab `ic_monitor.xml` & `ic_overlay.xml` TIDAK ikut module ini — dipakai bersama Memory, sudah dipindah ke `:shared:ui`.
- **Warna:** color values khusus tab Monitor (`colors.xml` + night variants) ikut pindah ke module ini.

#### `:feature:network`
- **Dependency:** `:shared:ui`, `:shared:config`, `:shared:preset`
- **File:** NetworkConfig, NetworkModule, NetworkPanelController, NetworkPositionController, NetworkPanelFragment
- **Resource:** `panel_network.xml`

#### `:feature:memory`
- **Dependency:** `:shared:ui`, `:shared:config`, `:shared:preset`
- **File:** MemoryConfig, MemoryModule, MemoryMonitor, MemoryOrderZonesView, MemoryPanelController, MemoryPositionController, MemoryPanelFragment
- **Resource:** `panel_memory.xml`, `menu_memory_bottom_nav.xml`, `mem_nav_item_color.xml`, `mem_card_bg.xml`, `mem_badge_active_bg.xml`, `mem_badge_stopped_bg.xml`
- **Catatan:** Sama seperti battery — ikon tab `ic_monitor.xml`/`ic_overlay.xml` diambil dari `:shared:ui`. Color values khusus memory (12 values + night) ikut pindah ke module ini.

---

### 3.7 `:app` — Shell Aplikasi

**Dependency:** Semua feature modules + `:core:service` + `:shared:*`

| File | Penjelasan |
|------|-----------|
| `MainActivity.java` | Activity utama, toolbar, nav drawer, panel system, auto-start BatteryMonitorService |
| `SettingsActivity.java` | Manajemen izin + Konfigurasi (Fitur Developer lock) |
| `DocumentationActivity.java` | Dokumentasi in-app |
| `PanelManager.java` | Kelola show/hide Fragment panel |
| `PermissionHelper.java` | Helper izin (fisik: `exp/ftxt/utils/`) |
| `PresetExampleActivity.java` | Contoh integrasi preset (dari `:shared:preset`) |
| `CrosshairPanelFragment.java` | Placeholder panel Crosshair (coming soon) |
| `LogoPanelFragment.java` | Placeholder panel Logo Display (coming soon) |
| `DebugingPanelFragment.java` | Panel Debugging (preview ikon rotasi notifikasi) |

**Resource:**
- Layout: `activity_main.xml`, `activity_settings.xml`, `activity_documentation.xml`, `drawer_content.xml`, `nav_header.xml`, `toolbar_zoom.xml`, `panel_debuging.xml`, `panel_crosshair.xml`, `panel_logo.xml`
- Menu: `drawer_menu.xml`, `main_menu.xml`
- Drawable: `main_bg.xml`, `toolbar_bg.xml`, `drawer_bg.xml`, `drawer_header_bg.xml`, `ic_settings.xml`, `ic_screen_rotation.xml`, `ic_sun.xml`, `ic_theme.xml`, `ic_dots_vertical.xml`, `ic_exit.xml`, `ic_rotation_variant_1..5.xml` (preview panel Debugging)
- Color: `drawer_background`, `drawer_header_background`, `drawer_header_text` (+ night variants)
- Values: `ids.xml`, `strings.xml`, `styles.xml` (warna umum & theme tetap di `:app`; warna spesifik fitur pindah ke module masing-masing)
- Manifest: `AndroidManifest.xml` (semua activity/service/receiver tetap terdaftar di sini)

---

## 4. Dependency Graph Antar Module

```
:app
├── :core:service
│   ├── :feature:floating-text
│   │   ├── :shared:ui
│   │   │   └── :shared:config
│   │   ├── :shared:preset
│   │   │   └── :shared:config
│   │   └── :shared:color
│   ├── :feature:fps
│   │   ├── :shared:ui
│   │   └── :shared:preset
│   ├── :feature:clock
│   │   ├── :shared:ui
│   │   └── :shared:preset
│   ├── :feature:battery
│   │   ├── :shared:ui
│   │   ├── :shared:preset
│   │   └── :shared:color
│   ├── :feature:network
│   │   ├── :shared:ui
│   │   └── :shared:preset
│   └── :feature:memory
│       ├── :shared:ui
│       └── :shared:preset
├── :feature:battery          ← BatteryMonitorService di-start dari :app (bukan lewat core)
└── Semua :shared:*
```

**Aturan penting:**
- Shared modules TIDAK boleh depend ke feature modules atau core
- Feature modules boleh depend ke shared modules, tapi TIDAK boleh depend ke feature modules lain
- Core service depend ke semua feature modules (tidak bisa dihindari karena FloatingService manage semua)
- App depend ke semua modules (shell only); start/stop BatteryMonitorService langsung dari `:app` (MainActivity, BootReceiver)

---

## 5. Fase Pengerjaan

Eksekusi dilakukan **satu module per iterasi**: kerjakan satu fase sampai tuntas, build verifikasi, commit, baru lanjut fase berikutnya. Berhenti di tengah selalu aman karena tiap fase meninggalkan project dalam kondisi utuh.

### Fase 0: Persiapan

- [ ] Backup project
- [ ] Buat branch `multi-module-split`
- [ ] Update `settings.gradle` untuk include semua module baru
- [ ] Setup root `build.gradle` dengan dependency management

### Fase 1: `:shared:config` (Mudah, 0 dependency)

- [ ] Buat folder `shared/config/` + `build.gradle`
- [ ] Pindah `ShadowConfig.java`, `BackgroundConfig.java` (dari `exp/ftxt/shared/ui/`)
- [ ] Update package declaration
- [ ] Update semua import di file lain
- [ ] Test: build harus berhasil

**Risiko:** Rendah. Hanya 2 file, zero dependency.

### Fase 2: `:shared:ui` (Sedang, depend ke `:shared:config`)

- [ ] Buat folder `shared/ui/` + `build.gradle`
- [ ] Pindah 11 file (OverlayModule, ShadowTextView, OverlayShadow, OverlayDragHandler, DpadController, SliderPositionController, SliderLabelEditor, SectionHelper, PresetPreviewView, BasePanelFragment, BatteryChartView)
- [ ] Pindah drawable shared (ic_arrow_*.xml, seekbar_thumb.xml, ic_edit.xml) + drawable lintas fitur (`ic_monitor.xml`, `ic_overlay.xml`)
- [ ] Update package declaration
- [ ] Update semua import di file lain
- [ ] Test: build harus berhasil

**Risiko:** Sedang. `ShadowTextView` dipakai oleh semua feature modules — satu typo di import = error di mana-mana.

### Fase 3: `:shared:color` (Mudah, 0 internal dependency)

- [ ] Buat folder `shared/color/` + `build.gradle`
- [ ] Pindah 8 file (ColorMath, ColorNameResolver, HSVColorPickerView, BatteryColors, TriangleColorPickerView, ColorPickerDialog, ColorPickerPanelController, ColorPickerPanelFragment)
- [ ] Pindah layout: `panel_color_picker.xml`, `dialog_color_picker.xml`
- [ ] Update package declaration
- [ ] Update semua import
- [ ] Test: build harus berhasil

**Risiko:** Rendah. Color picker cukup independen.

### Fase 4: `:shared:preset` (Sedang, depend ke `:shared:config`)

- [ ] Buat folder `shared/preset/` + `build.gradle`
- [ ] Pindah 4 file (OverlayPreset, PresetManager, PresetHandler, PresetBrowserDialog)
- [ ] Pindah `PresetExampleActivity.java` ke `:app`
- [ ] Pindah layout: `dialog_preset_browser.xml`, `preset_browser_item.xml`
- [ ] Pindah drawable: `ic_star_*.xml`, `vertical_divider.xml`
- [ ] Update package declaration
- [ ] Update semua import
- [ ] Test: build harus berhasil

**Risiko:** Sedang. `OverlayPreset` punya banyak field tapi semuanya primitive.

### Fase 5: Feature Modules (satu per satu)

Urutan ekstraksi berdasarkan kompleksitas:

#### 5.1 `:feature:clock` (paling simpel)
- [ ] Pindah ClockConfig, ClockModule, ClockPanelController, ClockPositionController, ClockPanelFragment
- [ ] Pindah `panel_clock.xml`
- [ ] Update import di FloatingService, NotificationHelper, BootReceiver, MainActivity
- [ ] Test: clock overlay berfungsi

#### 5.2 `:feature:fps`
- [ ] Pindah FpsConfig, FpsModule, FpsPanelController, FpsPositionController, FpsPanelFragment
- [ ] Pindah `panel_fps.xml`
- [ ] Update import di FloatingService, NotificationHelper, BootReceiver, MainActivity
- [ ] Test: FPS overlay berfungsi

#### 5.3 `:feature:network`
- [ ] Pindah NetworkConfig, NetworkModule, NetworkPanelController, NetworkPositionController, NetworkPanelFragment
- [ ] Pindah `panel_network.xml`
- [ ] Update import di FloatingService, NotificationHelper, BootReceiver, MainActivity
- [ ] Test: network overlay berfungsi

#### 5.4 `:feature:floating-text`
- [ ] Pindah TextConfig, TextModule, TextPanelController, TextPositionController, TextPanelFragment
- [ ] Pindah `panel_text.xml`
- [ ] Update import di FloatingService, NotificationHelper, BootReceiver, MainActivity
- [ ] Test: floating text overlay berfungsi

#### 5.5 `:feature:memory`
- [ ] Pindah MemoryConfig, MemoryModule, MemoryMonitor, MemoryOrderZonesView, MemoryPanelController, MemoryPositionController, MemoryPanelFragment
- [ ] Pindah `panel_memory.xml`, `menu_memory_bottom_nav.xml`, `mem_nav_item_color.xml`
- [ ] Pindah drawable: `mem_card_bg.xml`, `mem_badge_*.xml`
- [ ] Pindah 12 color values khusus memory (+ night variants)
- [ ] Update import di FloatingService, NotificationHelper, BootReceiver, MainActivity
- [ ] Test: memory overlay + monitor berfungsi

#### 5.6 `:feature:battery` (paling kompleks — gabungan `battery_stats` + `battery_bar` + tab Monitor)
- [ ] Pindah overlay inti: BatteryStatsConfig, BatteryStatsModule, BatteryBarConfig, BatteryBarView, BatteryBarModule, BatteryReading
- [ ] Pindah tab Monitor: BatteryMonitor, BatteryHistoryDb, BatteryCapacityEstimator, BatteryRingView, BatteryMonitorService, BatteryMonitorTabController, BatteryChartHistoryController, BatteryHealthCardController, BatterySnapshotExporter
- [ ] Pindah UI panel: BatteryOrderZonesView, BatteryPanelController, BatteryBarPanelController, BatteryPositionController, BatteryBarPositionController, BatteryPanelFragment
- [ ] Pindah `panel_battery.xml`, `menu_battery_bottom_nav.xml`, `bat_nav_item_color.xml`, `bat_card_bg.xml`, `ic_battery_strip.xml`
- [ ] Pindah color values khusus battery/tab Monitor (+ night variants)
- [ ] Update import di FloatingService, NotificationHelper, BootReceiver, MainActivity
- [ ] Pastikan start/stop `BatteryMonitorService` dari MainActivity & BootReceiver masih resolve (cross-module class reference)
- [ ] Test: battery info (3 tab) berfungsi, battery strip berfungsi, monitor baterai full-aktif berjalan

**Risiko per fase feature:** Sedang. Pattern-nya sama semua — pindah file, update import, test. Tapi satu import yang salah = compile error di banyak tempat.

### Fase 6: `:core:service` (Paling Sulit)

- [ ] Buat folder `core/` + `build.gradle`
- [ ] Pindah FloatingService, NotificationHelper, BootReceiver, NotificationActionReceiver, WakeLockManager, CrashLogger
- [ ] Pindah `notification_custom.xml` + notification drawables
- [ ] Update import di `:app` (MainActivity, PanelManager, SettingsActivity)
- [ ] Test: service start/stop, notifikasi, boot receiver, crash logger

**Risiko:** Tinggi. FloatingService depend ke semua feature modules. Satu import yang salah = semua overlay mati.

### Fase 7: `:app` Cleanup

- [ ] Sisa file pindah ke `:app` (MainActivity, SettingsActivity, DocumentationActivity, PanelManager, PermissionHelper, PresetExampleActivity, Crosshair/Logo/Debuging fragment)
- [ ] Pindah semua resource yang belum dipindah (activity layouts, drawer, panel_debuging/crosshair/logo, themes, ids, strings, colors/styles umum, ic_rotation_variant_*)
- [ ] Pindah `AndroidManifest.xml`
- [ ] Update `settings.gradle` final
- [ ] Update `build.gradle` root
- [ ] Hapus folder kosong di lokasi lama
- [ ] Test menyeluruh semua fitur

### Fase 8: Optimasi Build

- [ ] Setup shared `build.gradle` dengan common config (compileSdk, minSdk, Java version)
- [ ] Setup dependency management (version catalog atau ext block)
- [ ] Test incremental build (edit satu module, pastikan module lain tidak recompile)
- [ ] Bandingkan waktu build sebelum vs sesudah split

### Fase 9: Testing Akhir

- [ ] Test semua overlay: Floating Text, FPS, Clock, Battery Info (3 tab), Network, Memory
- [ ] Test tab Monitor Battery Info: ring gauge, grid metrik real-time, grafik riwayat (slider rentang 2m–24j), kartu Kesehatan Baterai (input kapasitas desain), Salin/Simpan Snapshot
- [ ] Test monitor baterai full-aktif: nyala otomatis saat app dibuka, restart saat boot, sampling dinamis (charging/idle/layar mati)
- [ ] Test Fitur Developer lock: kunci/buka switch panel Info Memori & Debugging, tombol Salin/Simpan Snapshot terkunci
- [ ] Test preset: save, load, search, tag, share, selective apply
- [ ] Test auto-start overlay saat boot
- [ ] Test notifikasi (ikon dinamis suhu, aksi toggle/kill/open)
- [ ] Test CrashLogger
- [ ] Test wake lock (layar on/off)
- [ ] Test dark/light theme (semua module punya warna night variant masing-masing)
- [ ] Test orientasi layar (portrait/landscape)
- [ ] Test backward compatibility (preset lama)
- [ ] Test install fresh + upgrade dari versi lama

---

## 6. Risiko & Mitigasi

| Risiko | Dampak | Mitigasi |
|--------|--------|----------|
| Import typo saat pindah file | Compile error di banyak tempat | Pindah satu module, test build langsung |
| Resource ID conflict antar module | Runtime crash | Pakai resource prefix per module (`:shared:ui` → `shared_ui_*`) |
| Resource dipakai lintas feature (`ic_monitor`, `ic_overlay` dipakai Battery & Memory) | Duplikasi atau compile error saat feature dipecah | Sudah diantisipasi: kedua ikon pindah ke `:shared:ui` di Fase 2, feature modules ambil dari sana |
| Package name berubah | Breaking preset lama (prefs key) | Package name TIDAK berubah — hanya lokasi file fisik yang berubah |
| FloatingService terlalu coupled | Sulit di-maintain | long-term: refactor ke interface/abstraction |
| BatteryMonitorService cross-module reference (di-start dari `:app`) | Compile error jika salah taruh module | Service ikut `:feature:battery` (dependensinya murni battery_stats), start dari `:app` yang depend semuanya |
| Warna/values tercampur saat pecah per module | Warna hilang / fallback aneh | Pindahkan color values spesifik fitur (+ night variant) bersama module-nya; sisakan umum di `:app` |
| Build lebih lambat karena banyak module | Kebalikan tujuan awal | Pastikan parallel build aktif, cek dependency cycle |
| Git conflict lebih sering | Merge conflict saat multi-dev | Kurangi dengan module yang independence tinggi |

---

## 7. Yang TIDAK Berubah

- **Package names** — `exp.ftxt.features.battery_bar.BatteryBarModule` tetap sama, hanya lokasi file fisik berpindah
- **SharedPreferences keys** — Semua `batbar_*`, `battery_*`, `mem_*` tetap sama
- **Preset format** — OverlayPreset JSON format tetap sama
- **User-facing behavior** — Tidak ada perubahan fitur
- **AndroidManifest** — Tetap di `:app`, semua activity/service/receiver tetap terdaftar di sana

---

## 8. Estimasi Waktu

| Fase | Estimasi | Keterangan |
|------|----------|------------|
| Fase 0: Persiapan | 15 menit | Setup build files |
| Fase 1-4: Shared modules | 1-2 jam | Relatif straightforward |
| Fase 5: Feature modules | 3-4 jam | Repetitive tapi many files; battery membengkak sejak v4.88 (±25 file) |
| Fase 6: Core service | 30-60 menit | Paling tricky |
| Fase 7: App cleanup | 30 menit | Sisa file |
| Fase 8: Optimasi | 30 menit | Setup shared config |
| Fase 9: Testing | 1-2 jam | Test semua fitur |
| **Total** | **6-9 jam** | Tergantung kompleksitas debug |

---

## 9. Kapan Harus Dihentikan?

Jangan lanjut split jika:
1. Build gagal di fase manapun dan tidak bisa di-fix dalam 30 menit
2. Ada bug regression yang muncul setelah split
3. Waktu yang tersedia tidak cukup untuk testing menyeluruh

Lebih baik memiliki single-module yang funciona daripada multi-module yang broken.

---

## 10. Referensi

- [Android Official: Multi-module apps](https://developer.android.com/modularize)
- [Gradle Multi-project Builds](https://docs.gradle.org/current/userguide/multi_project_builds.html)
- [Feature modules in Android](https://developer.android.com/guide/navigation/navigation-implementing#support-multiple-back-stacks)
