# Rencana Organisasi Sub-Folder FTxT

> **Tanggal:** 2026-08-17
> **Status:** Alternatif dari Multi-Module

---

## 1. Apa Itu Sub-Folder Approach?

Tidak ada perubahan build system. Semua kode tetap dalam satu module `app/`. Yang berubah hanya **struktur folder** — file-file dipindah ke sub-folder yang lebih rapi.

**Sebelum (FTxT sekarang):**
```
app/src/main/java/exp/ftxt/
├── core/
│   ├── FloatingService.java
│   ├── NotificationHelper.java
│   ├── BootReceiver.java
│   ├── NotificationActionReceiver.java
│   ├── WakeLockManager.java
│   └── CrashLogger.java
├── features/
│   ├── floating_text/
│   │   ├── TextConfig.java
│   │   └── TextModule.java
│   ├── fps_display/
│   │   ├── FpsConfig.java
│   │   └── FpsModule.java
│   ├── clock_module/
│   │   ├── ClockConfig.java
│   │   └── ClockModule.java
│   ├── battery_stats/
│   │   ├── BatteryStatsConfig.java
│   │   └── BatteryStatsModule.java
│   ├── battery_current/    ← sudah dihapus
│   ├── battery_bar/
│   │   ├── BatteryBarConfig.java
│   │   ├── BatteryBarView.java
│   │   └── BatteryBarModule.java
│   ├── network_stats/
│   │   ├── NetworkConfig.java
│   │   └── NetworkModule.java
│   ├── memory_stats/
│   │   ├── MemoryConfig.java
│   │   ├── MemoryModule.java
│   │   └── MemoryMonitor.java
│   ├── color_picker/
│   │   └── TriangleColorPickerView.java
│   ├── crosshair/    (placeholder)
│   ├── cpu_monitor/  (placeholder)
│   └── logo_display/ (placeholder)
├── shared/
│   ├── color/
│   │   ├── ColorMath.java
│   │   ├── ColorNameResolver.java
│   │   └── HSVColorPickerView.java
│   ├── preset/
│   │   ├── OverlayPreset.java
│   │   ├── PresetManager.java
│   │   ├── PresetHandler.java
│   │   ├── PresetBrowserDialog.java
│   │   └── PresetExampleActivity.java
│   └── ui/
│       ├── BackgroundConfig.java
│       ├── ShadowConfig.java
│       ├── ShadowTextView.java
│       ├── OverlayModule.java
│       ├── OverlayDragHandler.java
│       ├── OverlayShadow.java
│       ├── ColorPickerDialog.java
│       ├── DpadController.java
│       ├── SliderPositionController.java
│       ├── SliderLabelEditor.java
│       ├── SectionHelper.java
│       └── PresetPreviewView.java
├── ui/
│   ├── TextPanelController.java
│   ├── TextPositionController.java
│   ├── FpsPanelController.java
│   ├── FpsPositionController.java
│   ├── ClockPanelController.java
│   ├── ClockPositionController.java
│   ├── BatteryPanelController.java
│   ├── BatteryPositionController.java
│   ├── BatteryBarPanelController.java
│   ├── BatteryBarPositionController.java
│   ├── NetworkPanelController.java
│   ├── NetworkPositionController.java
│   ├── MemoryPanelController.java
│   ├── MemoryPositionController.java
│   ├── ColorPickerPanelController.java
│   ├── BatteryOrderZonesView.java
│   ├── BasePanelFragment.java
│   ├── PanelManager.java
│   └── fragment/
│       ├── TextPanelFragment.java
│       ├── FpsPanelFragment.java
│       ├── ClockPanelFragment.java
│       ├── BatteryPanelFragment.java
│       ├── BatteryBarPanelFragment.java  ← sudah dihapus
│       ├── NetworkPanelFragment.java
│       ├── MemoryPanelFragment.java
│       ├── ColorPickerPanelFragment.java
│       ├── CrosshairPanelFragment.java
│       └── LogoPanelFragment.java
├── utils/
│   └── PermissionHelper.java
├── MainActivity.java
├── SettingsActivity.java
└── DocumentationActivity.java
```

---

## 2. Yang Bisa Diperbaiki dengan Sub-Folder

Masalah utama FTxT sekarang bukan jumlah file, tapi **penempatan file yang kurang konsisten**:

### 2.1 `shared/ui/` terlalu campur aduk
Isinya 12 file dengan fungsi sangat berbeda:
- Model data: `BackgroundConfig`, `ShadowConfig`
- Custom View: `ShadowTextView`, `PresetPreviewView`
- Interface: `OverlayModule`
- Utility: `OverlayDragHandler`, `OverlayShadow`, `DpadController`, `SliderPositionController`, `SliderLabelEditor`, `SectionHelper`
- Dialog: `ColorPickerDialog`
- Base class: `BasePanelFragment`

**Solusi:** Pecah jadi beberapa sub-folder.

### 2.2 `ui/` campur aduk
15+ controller + fragment di satu folder. Tidak ada grouping per fitur.

**Solusi:** Group per fitur di dalam `ui/`.

### 2.3 Placeholder folders kosong
`crosshair/`, `cpu_monitor/`, `logo_display/` di `features/` hanya berisi file kosong atau tidak ada. Membingungkan.

**Solusi:** Hapus atau pindah ke folder `placeholder/`.

### 2.4 `shared/preset/PresetExampleActivity.java`
Activity contoh tidak termasuk "shared" — ini cuma referensi dev.

**Solusi:** Pindah ke root `exp/ftxt/` bersama activity lain.

---

## 3. Struktur Sub-Folder yang Direncanakan

```
app/src/main/java/exp/ftxt/
│
├── core/
│   ├── FloatingService.java
│   ├── NotificationHelper.java
│   ├── BootReceiver.java
│   ├── NotificationActionReceiver.java
│   ├── WakeLockManager.java
│   └── CrashLogger.java
│
├── features/
│   ├── floating_text/
│   │   ├── TextConfig.java
│   │   └── TextModule.java
│   ├── fps/
│   │   ├── FpsConfig.java
│   │   └── FpsModule.java
│   ├── clock/
│   │   ├── ClockConfig.java
│   │   └── ClockModule.java
│   ├── battery/
│   │   ├── BatteryStatsConfig.java
│   │   ├── BatteryStatsModule.java
│   │   ├── BatteryBarConfig.java
│   │   ├── BatteryBarView.java
│   │   ├── BatteryBarModule.java
│   │   └── BatteryOrderZonesView.java
│   ├── network/
│   │   ├── NetworkConfig.java
│   │   └── NetworkModule.java
│   ├── memory/
│   │   ├── MemoryConfig.java
│   │   ├── MemoryModule.java
│   │   └── MemoryMonitor.java
│   └── color_picker/
│       └── TriangleColorPickerView.java
│
├── shared/
│   ├── config/
│   │   ├── ShadowConfig.java
│   │   └── BackgroundConfig.java
│   ├── ui/
│   │   ├── ShadowTextView.java
│   │   ├── OverlayModule.java
│   │   ├── OverlayDragHandler.java
│   │   ├── OverlayShadow.java
│   │   ├── DpadController.java
│   │   ├── SliderPositionController.java
│   │   ├── SliderLabelEditor.java
│   │   ├── SectionHelper.java
│   │   └── PresetPreviewView.java
│   ├── color/
│   │   ├── ColorMath.java
│   │   ├── ColorNameResolver.java
│   │   ├── HSVColorPickerView.java
│   │   └── ColorPickerDialog.java
│   └── preset/
│       ├── OverlayPreset.java
│       ├── PresetManager.java
│       ├── PresetHandler.java
│       └── PresetBrowserDialog.java
│
├── ui/
│   ├── BasePanelFragment.java
│   ├── PanelManager.java
│   ├── text/
│   │   ├── TextPanelController.java
│   │   ├── TextPositionController.java
│   │   └── TextPanelFragment.java
│   ├── fps/
│   │   ├── FpsPanelController.java
│   │   ├── FpsPositionController.java
│   │   └── FpsPanelFragment.java
│   ├── clock/
│   │   ├── ClockPanelController.java
│   │   ├── ClockPositionController.java
│   │   └── ClockPanelFragment.java
│   ├── battery/
│   │   ├── BatteryPanelController.java
│   │   ├── BatteryBarPanelController.java
│   │   ├── BatteryPositionController.java
│   │   ├── BatteryBarPositionController.java
│   │   └── BatteryPanelFragment.java
│   ├── network/
│   │   ├── NetworkPanelController.java
│   │   ├── NetworkPositionController.java
│   │   └── NetworkPanelFragment.java
│   ├── memory/
│   │   ├── MemoryPanelController.java
│   │   ├── MemoryPositionController.java
│   │   └── MemoryPanelFragment.java
│   └── color_picker/
│       ├── ColorPickerPanelController.java
│       └── ColorPickerPanelFragment.java
│
├── utils/
│   └── PermissionHelper.java
│
├── MainActivity.java
├── SettingsActivity.java
├── DocumentationActivity.java
└── PresetExampleActivity.java
```

---

## 4. Perubahan yang Dilakukan

### 4.1 `shared/` → pecah jadi 4 sub-folder

**Sebelum:** 12 file campur di `shared/ui/`

**Sesudah:**
- `shared/config/` → 2 file model data (ShadowConfig, BackgroundConfig)
- `shared/ui/` → 7 file komponen UI (ShadowTextView, OverlayModule, dll)
- `shared/color/` → 4 file (ColorMath, ColorNameResolver, HSVColorPickerView, ColorPickerDialog)
- `shared/preset/` → 4 file (OverlayPreset, PresetManager, PresetHandler, PresetBrowserDialog)

### 4.2 `features/` → rename & gabung

**Sebelum:**
- `fps_display/` (2 file)
- `clock_module/` (2 file)
- `battery_stats/` (2 file)
- `battery_bar/` (3 file)
- `network_stats/` (2 file)
- `memory_stats/` (3 file)
- `crosshair/` (placeholder kosong)
- `cpu_monitor/` (placeholder kosong)
- `logo_display/` (placeholder kosong)

**Sesudah:**
- `fps/` (2 file) — rename dari `fps_display`
- `clock/` (2 file) — rename dari `clock_module`
- `battery/` (6 file) — gabung `battery_stats` + `battery_bar` + `BatteryOrderZonesView`
- `network/` (2 file) — rename dari `network_stats`
- `memory/` (3 file) — rename dari `memory_stats`
- `color_picker/` (1 file) — pindah dari `features/color_picker`
- Hapus: `crosshair/`, `cpu_monitor/`, `logo_display/`

### 4.3 `ui/` → group per fitur

**Sebelum:** 15+ file campur di satu folder + `fragment/` terpisah

**Sesudah:**
- `ui/BasePanelFragment.java` — tetap di root
- `ui/PanelManager.java` — tetap di root
- `ui/text/` → controller + position + fragment (3 file)
- `ui/fps/` → controller + position + fragment (3 file)
- `ui/clock/` → controller + position + fragment (3 file)
- `ui/battery/` → controller + position + fragment (5 file, karena ada BatteryInfo + BatteryBar)
- `ui/network/` → controller + position + fragment (3 file)
- `ui/memory/` → controller + position + fragment (3 file)
- `ui/color_picker/` → controller + fragment (2 file)

### 4.4 File yang dipindah lokasi

| File | Dari | Ke |
|------|------|----|
| `BackgroundConfig.java` | `shared/ui/` | `shared/config/` |
| `ShadowConfig.java` | `shared/ui/` | `shared/config/` |
| `ColorPickerDialog.java` | `shared/ui/` | `shared/color/` |
| `PresetExampleActivity.java` | `shared/preset/` | root `exp/ftxt/` |
| `BatteryOrderZonesView.java` | `ui/` | `features/battery/` |
| `ColorPickerPanelController.java` | `ui/` | `ui/color_picker/` |

### 4.5 File yang dihapus

| File | Alasan |
|------|--------|
| `features/crosshair/` | Placeholder kosong |
| `features/cpu_monitor/` | Placeholder kosong |
| `features/logo_display/` | Placeholder kosong |
| `features/battery_current/` | Sudah dihapus di porting FBI |
| `ui/fragment/BatteryBarPanelFragment.java` | Sudah digabung ke BatteryPanelFragment |

---

## 5. Perubahan Package Declaration

Karena ini **bukan multi-module**, package name **TIDAK berubah**. Hanya lokasi file fisik yang berpindah.

Contoh: `ShadowConfig.java` pindah dari `shared/ui/` ke `shared/config/`, tapi package tetap `exp.ftxt.shared.ui`. Ini karena Android build system mengizinkan file berada di folder berbeda dari package declaration (meskipun tidak ideal).

**Alternatif:** Update package declaration juga. Tapi ini berarti:
- Semua import di semua file harus diupdate
- SharedPreferences keys tidak berubah (tidak terpengaruh)
- Preset format tidak berubah (tidak terpengaruh)

**Rekomendasi:** Update package declaration juga agar konsisten. Folder = package.

---

## 6. Kelebihan Sub-Folder vs Multi-Module

| Aspek | Sub-Folder | Multi-Module |
|-------|-----------|-------------|
| Setup | Tidak ada, langsung pindah file | Buat build.gradle per module, update settings.gradle |
| Package name | Bisa berubah (opsional) | Tidak berubah |
| Build performance | Tidak ada perubahan | Lebih cepat (incremental per module) |
| Ekstraksi fitur | Masih perlu bongkar manual | Copy module langsung jalan |
| Dependency jelas | Tidak — semua file dalam satu scope | Ya — tiap module declare dependency sendiri |
| Kompleksitas | Rendah | Sedang |
| Risiko error | Rendah — hanya pindah file | Sedang — import typo, resource conflict |
| Git conflict | Lebih sering (satu folder besar) | Lebih jarang (file terpisah per module) |

---

## 7. Kapan Sub-Folder Lebih Baik dari Multi-Module?

1. **Project masih kecil** — FTxT ~72 file. Multi-module overkill untuk ukuran ini.
2. **Solo developer** — Tidak perlu pisah tanggung jawab antar developer.
3. **Ingin hasil cepat** — Sub-folder selesai dalam 30-60 menit. Multi-module 5-8 jam.
4. **Tidak butuh build performance** — Build FTxT sekarang mungkin sudah cepat.
5. **Ingin ekstraksi nanti** — Sub-folder bisa "upgrade" ke multi-module kapan saja. Struktur folder yang rapi mempermigrasi ke module.

---

## 8. Kapan Multi-Module Lebih Baik?

1. **Build lambat** — Kalau build > 2 menit.
2. **Ingin reuse kode** — Misalnya preset system mau dipakai di project lain.
3. **Tim besar** — Multiple developer kerja di bagian berbeda.
4. **Ingin feature module Android** — Bundle on demand dari Play Store.

---

## 9. Rekomendasi

Untuk FTxT saat ini: **mulai dengan sub-folder**. Alasannya:

1. Project masih manageable (72 file)
2. Solo developer
3. Hasil cepat (30-60 menit)
4. Struktur folder rapi mempermigrasi ke multi-module nanti
5. Resiko minimal — hanya pindah file, tidak ubah build system

Kalau nanti project sudah 150+ file atau mulai team development, baru upgrade ke multi-module. Struktur sub-folder yang sudah rapi akan mempermigrasi proses itu.

---

## 10. Fase Pengerjaan Sub-Folder

### Fase 1: Persiapan (5 menit)
- [ ] Backup project
- [ ] Buat branch `subfolder-restructure`

### Fase 2: `shared/` restructure (15 menit)
- [ ] Buat `shared/config/`, pindah ShadowConfig + BackgroundConfig
- [ ] Buat `shared/color/`, pindah ColorMath + ColorNameResolver + HSVColorPickerView + ColorPickerDialog
- [ ] Pindah `PresetExampleActivity.java` ke root
- [ ] Update package declaration (opsional)
- [ ] Update semua import (opsional)
- [ ] Test: build harus berhasil

### Fase 3: `features/` rename & gabung (15 menit)
- [ ] Rename `fps_display/` → `fps/`
- [ ] Rename `clock_module/` → `clock/`
- [ ] Gabung `battery_stats/` + `battery_bar/` → `battery/`
- [ ] Rename `network_stats/` → `network/`
- [ ] Rename `memory_stats/` → `memory/`
- [ ] Pindah `BatteryOrderZonesView.java` ke `features/battery/`
- [ ] Hapus placeholder folders (crosshair, cpu_monitor, logo_display)
- [ ] Update import di FloatingService, MainActivity, NotificationHelper, BootReceiver
- [ ] Test: build harus berhasil

### Fase 4: `ui/` group per fitur (15 menit)
- [ ] Buat sub-folder: `ui/text/`, `ui/fps/`, `ui/clock/`, `ui/battery/`, `ui/network/`, `ui/memory/`, `ui/color_picker/`
- [ ] Pindah controller + position + fragment ke sub-folder masing-masing
- [ ] Pindah `ColorPickerPanelController` ke `ui/color_picker/`
- [ ] Update import (opsional)
- [ ] Test: build harus berhasil

### Fase 5: Cleanup (10 menit)
- [ ] Hapus folder kosong
- [ ] Cek tidak ada file yang tertinggal
- [ ] Test menyeluruh semua fitur

### Fase 6: Testing (30 menit)
- [ ] Test semua overlay berfungsi
- [ ] Test preset berfungsi
- [ ] Test auto-start boot
- [ ] Test notifikasi
- [ ] Test crash logger

**Total estimasi: 1-1.5 jam**

---

## 11. Contoh Import Setelah Sub-Folder (jika package diupdate)

```java
// Sebelum
import exp.ftxt.shared.ui.ShadowConfig;
import exp.ftxt.features.battery_bar.BatteryBarConfig;

// Sesudah
import exp.ftxt.shared.config.ShadowConfig;
import exp.ftxt.features.battery.BatteryBarConfig;
```

Kalau package TIDAK diupdate, tidak ada perubahan import — hanya lokasi file fisik di IDE yang berubah.
