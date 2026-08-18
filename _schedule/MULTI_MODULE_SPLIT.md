# Rencana Split Multi-Module FTxT

> **Tanggal:** 2026-08-17
> **Versi Target:** Setelah v4.86.0
> **Status:** Rencana (belum eksekusi)

---

## 1. Mengapa Multi-Module?

Saat ini FTxT single-module — semua 72 file Java, 21 layout, dan 34 drawable berada dalam satu module `app/`. Beberapa alasan untuk split:

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
│   ├── ui/                      ← Komponen UI bersama
│   │   └── build.gradle
│   ├── color/                   ← Color picker + math
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
│   ├── battery/
│   │   └── build.gradle
│   ├── network/
│   │   └── build.gradle
│   ├── memory/
│   │   └── build.gradle
│   └── color_picker/
│       └── build.gradle
│
├── core/                        ← SERVICE LAYER
│   └── build.gradle
│
└── app/                         ← SHELL APLIKASI
    └── build.gradle
```

---

## 3. Isi Tiap Module

### 3.1 `:shared:config` — Model Data Dasar

**Dependency internal:** 0 (leaf node)

| File | Penjelasan |
|------|-----------|
| `ShadowConfig.java` | Model data shadow (color, blur, offset) |
| `BackgroundConfig.java` | Model data background (enable, color, padding, offset, margin, radius) |

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
| `BasePanelFragment.java` | Abstract base Fragment untuk semua panel |

**Resource:**
- Drawable: `ic_arrow_up.xml`, `ic_arrow_down.xml`, `ic_arrow_left.xml`, `ic_arrow_right.xml`, `seekbar_thumb.xml`, `ic_edit.xml`
- Layout: Tidak ada (base class saja)

**Pemicu ekstraksi:** Sedang. `ShadowTextView` adalah komponen inti yang dipakai oleh hampir semua feature module. Setelah module ini ada, feature modules bisa depend ke sini.

---

### 3.3 `:shared:color` — Color Picker + Math

**Dependency internal:** 0 (leaf node — ColorMath tidak depend ke mana-mana)

| File | Penjelasan |
|------|-----------|
| `ColorMath.java` | Operasi matematika HSV |
| `ColorNameResolver.java` | Deteksi nama warna dari RGB |
| `HSVColorPickerView.java` | Custom View color wheel HSV |
| `TriangleColorPickerView.java` | Custom View segitiga HSV |
| `ColorPickerDialog.java` | Dialog color picker gabungan |
| `ColorPickerPanelController.java` | UI panel color picker |

**Resource:**
- Layout: `panel_color_picker.xml`, `dialog_color_picker.xml`
- Drawable: `ic_edit.xml`, `seekbar_thumb.xml`
- Color: Tidak ada khusus

**Pemicu ekstraksi:** Sedang. `ColorPickerDialog` dipanggil dari beberapa panel controller lain (misal `BatteryBarPanelController`, `TextPanelController`). Setelah split, module ini harus di-depend oleh feature modules yang butuh color picker.

---

### 3.4 `:shared:preset` — Sistem Preset

**Dependency internal:** `:shared:config` (ShadowConfig)

| File | Penjelasan |
|------|-----------|
| `OverlayPreset.java` | Model data preset dengan UUID |
| `PresetManager.java` | CRUD preset (save/load/rename/reorder) |
| `PresetHandler.java` | Delegate pattern per modul |
| `PresetBrowserDialog.java` | DialogFragment browser preset |

**Catatan:**
- `PresetExampleActivity.java` → pindah ke `:app` (depend ke `FloatingService` + `TextConfig`)

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
| `NotificationHelper.java` | Custom notification + ikon dinamis |
| `BootReceiver.java` | Restore module saat boot |
| `NotificationActionReceiver.java` | Handle aksi notifikasi |
| `WakeLockManager.java` | Partial wake lock |
| `CrashLogger.java` | Crash logger ke folder Download |

**Catatan penting:**
- `FloatingService` adalah "God class" — depend ke 7 feature modules + configs.
- `NotificationHelper` dan `BootReceiver` juga depend ke semua config modules.
- Module ini TIDAK bisa di-extract duluan — harus menunggu semua feature modules ada.

**Resource:**
- Layout: `notification_custom.xml`
- Drawable: `ic_notification_*.xml`, `ic_close.xml`
- Menu: `drawer_menu.xml` (referensi nav IDs)

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

#### `:feature:battery`
- **Dependency:** `:shared:ui`, `:shared:config`, `:shared:preset`, `:shared:color`
- **File:** BatteryStatsConfig, BatteryStatsModule, BatteryBarConfig, BatteryBarView, BatteryBarModule, BatteryOrderZonesView, BatteryPanelController, BatteryBarPanelController, BatteryPositionController, BatteryBarPositionController, BatteryPanelFragment
- **Resource:** `panel_battery.xml`, `menu_battery_bottom_nav.xml`, `bat_nav_item_color.xml`, `ic_battery_strip.xml`
- **Catatan:** Battery Info dan Battery Bar sudah digabung dalam satu panel tabbed — tidak perlu dipecah lagi.

#### `:feature:network`
- **Dependency:** `:shared:ui`, `:shared:config`, `:shared:preset`
- **File:** NetworkConfig, NetworkModule, NetworkPanelController, NetworkPositionController, NetworkPanelFragment
- **Resource:** `panel_network.xml`

#### `:feature:memory`
- **Dependency:** `:shared:ui`, `:shared:config`, `:shared:preset`
- **File:** MemoryConfig, MemoryModule, MemoryMonitor, MemoryPanelController, MemoryPositionController, MemoryPanelFragment
- **Resource:** `panel_memory.xml`, `menu_memory_bottom_nav.xml`, `mem_nav_item_color.xml`, `mem_card_bg.xml`, `mem_badge_*.xml`

#### `:feature:color-picker`
- **Dependency:** `:shared:ui`, `:shared:color`
- **File:** ColorPickerPanelFragment (sudah termasuk di `:shared:color` — pertimbangkan gabung)
- **Resource:** `panel_color_picker.xml`

**Catatan:** Color picker panel bisa digabung ke `:shared:color` karena saling berkaitan erat.

---

### 3.7 `:app` — Shell Aplikasi

**Dependency:** Semua feature modules + `:core:service` + `:shared:*`

| File | Penjelasan |
|------|-----------|
| `MainActivity.java` | Activity utama, toolbar, nav drawer, panel system |
| `SettingsActivity.java` | Manajemen izin |
| `DocumentationActivity.java` | Dokumentasi in-app |
| `PanelManager.java` | Kelola show/hide Fragment panel |
| `PresetExampleActivity.java` | Contoh integrasi preset (dari `:shared:preset`) |

**Resource:**
- Layout: `activity_main.xml`, `activity_settings.xml`, `activity_documentation.xml`, `drawer_content.xml`, `nav_header.xml`, `toolbar_zoom.xml`
- Menu: `drawer_menu.xml`, `main_menu.xml`
- Drawable: `main_bg.xml`, `toolbar_bg.xml`, `drawer_bg.xml`, `drawer_header_bg.xml`, `ic_settings.xml`, `ic_screen_rotation.xml`, `ic_sun.xml`, `ic_theme.xml`
- Color: `drawer_background`, `drawer_header_background`, `drawer_header_text` (+ night variants)
- Values: `ids.xml`, `strings.xml`, `colors.xml`, `styles.xml`, `themes.xml`
- Manifest: `AndroidManifest.xml`

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
└── Semua :shared:*
```

**Aturan penting:**
- Shared modules TIDAK boleh depend ke feature modules atau core
- Feature modules boleh depend ke shared modules, tapi TIDAK boleh depend ke feature modules lain
- Core service depend ke semua feature modules (tidak bisa dihindari karena FloatingService manage semua)
- App depend ke semua modules (shell only)

---

## 5. Fase Pengerjaan

### Fase 0: Persiapan

- [ ] Backup project
- [ ] Buat branch `multi-module-split`
- [ ] Update `settings.gradle` untuk include semua module baru
- [ ] Setup root `build.gradle` dengan dependency management

### Fase 1: `:shared:config` (Mudah, 0 dependency)

- [ ] Buat folder `shared/config/` + `build.gradle`
- [ ] Pindah `ShadowConfig.java`, `BackgroundConfig.java`
- [ ] Update package declaration
- [ ] Update semua import di file lain
- [ ] Test: build harus berhasil

**Risiko:** Rendah. Hanya 2 file, zero dependency.

### Fase 2: `:shared:ui` (Sedang, depend ke `:shared:config`)

- [ ] Buat folder `shared/ui/` + `build.gradle`
- [ ] Pindah 10 file (OverlayModule, ShadowTextView, OverlayShadow, OverlayDragHandler, DpadController, SliderPositionController, SliderLabelEditor, SectionHelper, PresetPreviewView, BasePanelFragment)
- [ ] Pindah drawable shared (ic_arrow_*.xml, seekbar_thumb.xml, ic_edit.xml)
- [ ] Update package declaration
- [ ] Update semua import di file lain
- [ ] Test: build harus berhasil

**Risiko:** Sedang. `ShadowTextView` dipakai oleh 6 feature modules — satu typo di import = error di mana-mana.

### Fase 3: `:shared:color` (Mudah, 0 internal dependency)

- [ ] Buat folder `shared/color/` + `build.gradle`
- [ ] Pindah 6 file (ColorMath, ColorNameResolver, HSVColorPickerView, TriangleColorPickerView, ColorPickerDialog, ColorPickerPanelController)
- [ ] Pindah layout: `panel_color_picker.xml`, `dialog_color_picker.xml`
- [ ] Pindah ColorPickerPanelFragment ke `:shared:color` atau `:app`
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
- [ ] Pindah MemoryConfig, MemoryModule, MemoryMonitor, MemoryPanelController, MemoryPositionController, MemoryPanelFragment
- [ ] Pindah `panel_memory.xml`, `menu_memory_bottom_nav.xml`, `mem_nav_item_color.xml`
- [ ] Pindah drawable: `mem_card_bg.xml`, `mem_badge_*.xml`
- [ ] Pindah 12 color values khusus memory
- [ ] Update import di FloatingService, NotificationHelper, BootReceiver, MainActivity
- [ ] Test: memory overlay + monitor berfungsi

#### 5.6 `:feature:battery` (paling kompleks)
- [ ] Pindah BatteryStatsConfig, BatteryStatsModule, BatteryBarConfig, BatteryBarView, BatteryBarModule
- [ ] Pindah BatteryOrderZonesView, BatteryPanelController, BatteryBarPanelController
- [ ] Pindah BatteryPositionController, BatteryBarPositionController, BatteryPanelFragment
- [ ] Pindah `panel_battery.xml`, `menu_battery_bottom_nav.xml`, `bat_nav_item_color.xml`
- [ ] Pindah drawable: `ic_battery_strip.xml`
- [ ] Update import di FloatingService, NotificationHelper, BootReceiver, MainActivity
- [ ] Test: battery info (3 tab) berfungsi, battery strip berfungsi

**Risiko per fase feature:** Sedang. Pattern-nya sama semua — pindah file, update import, test. Tapi satu import yang salah = compile error di banyak tempat.

### Fase 6: `:core:service` (Paling Sulit)

- [ ] Buat folder `core/` + `build.gradle`
- [ ] Pindah FloatingService, NotificationHelper, BootReceiver, NotificationActionReceiver, WakeLockManager, CrashLogger
- [ ] Pindah `notification_custom.xml` + notification drawables
- [ ] Update import di `:app` (MainActivity, PanelManager)
- [ ] Test: service start/stop, notifikasi, boot receiver, crash logger

**Risiko:** Tinggi. FloatingService depend ke semua feature modules. Satu import yang salah = semua overlay mati.

### Fase 7: `:app` Cleanup

- [ ] Sisa file pindah ke `:app` (MainActivity, SettingsActivity, DocumentationActivity, PanelManager)
- [ ] Pindah semua resource yang belum dipindah (activity layouts, drawer, themes, ids, strings, colors, styles)
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
- [ ] Test preset: save, load, search, tag, share
- [ ] Test auto-start saat boot
- [ ] Test notifikasi
- [ ] Test CrashLogger
- [ ] Test wake lock (layar on/off)
- [ ] Test dark/light theme
- [ ] Test orientasi layar (portrait/landscape)
- [ ] Test backward compatibility (preset lama)
- [ ] Test install fresh + upgrade dari versi lama

---

## 6. Risiko & Mitigasi

| Risiko | Dampak | Mitigasi |
|--------|--------|----------|
| Import typo saat pindah file | Compile error di banyak tempat | Pindah satu module, test build langsung |
| Resource ID conflict antar module | Runtime crash | Pakai resource prefix per module (`:shared:ui` → `shared_ui_*`) |
| Package name berubah | Breaking preset lama (prefs key) | Package name TIDAK berubah — hanya lokasi file fisik yang berubah |
| FloatingService terlalu coupled | Sulit di-maintain | long-term: refactor ke interface/abstraction |
| Build lebih lambat karena banyak module | Kebalikan tujuan awal | Pastikan parallel build aktif, cek dependency cycle |
| Git conflict lebih sering | Merge conflict saat多人开发 | Kurangi dengan module yang independence tinggi |

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
| Fase 5: Feature modules | 2-3 jam | Repetitive tapi many files |
| Fase 6: Core service | 30-60 menit | Paling tricky |
| Fase 7: App cleanup | 30 menit | Sisa file |
| Fase 8: Optimasi | 30 menit | Setup shared config |
| Fase 9: Testing | 1-2 jam | Test semua fitur |
| **Total** | **5-8 jam** | Tergantung kompleksitas debug |

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
