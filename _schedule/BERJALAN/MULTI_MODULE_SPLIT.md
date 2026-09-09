# Rencana Split Multi-Module FTxT

> **Tanggal:** 2026-09-09 (revisi penuh)
> **Revisi sebelumnya:** 2026-08-23 — target v4.88.1 → **usang** (asumsi `BatteryMonitorService` & struktur `features/` salah)
> **Versi baseline:** v4.92.1 (versionCode 278)
> **Status:** Rencana aktif — P0, Fase 1–5 selesai; Fase 6 (`:core:service`) berikutnya
> **Pilihan jalur:** **Jalur Y** (pragmatis) — feature module murni Java (tanpa resource & tampilan)

---

## 1. Kondisi Aktual (Audit v4.92.1)

Hasil audit menyeluruh — bukan asumsi dari versi lama:

- **102 file Java, ±28.411 baris.** UI layer (`exp/ftxt/ui/`, termasuk subfolder `fragment`) = 11.861 baris (**42%** dari total). Semua kode masih di **`app/src/main/java/exp/ftxt/`**; belum ada folder `features/`, `core/`, `shared/` di root.
- **`settings.gradle` sudah `include ':app'` + `':shared:config'`** — Fase 1 **SELESAI**. `shared/config/build.gradle` aktif (namespace `exp.ftxt.shared.config`, minSdk 26, Java 17), terverifikasi menghasilkan `config-debug.aar`.
- **TIDAK ada `BatteryMonitorService`.** Monitor baterai full-aktif ditampung `FloatingService` (dikonfirmasi komentar `core/BootReceiver.java` baris 33–35). Poin "service sendiri masuk `:feature:battery`" di dokumen lama **salah** — perlu dicatat sebagai divergensi.
- **4 pelanggaran aturan "shared tidak boleh depend ke features":**
  1. `shared/ui/BatteryChartView.java` → import `BatteryHistoryDb`, `BatteryReading`
  2. `shared/ui/BatterySessionBarChartView.java` → import `BatteryReading`
  3. `shared/ui/ColorPickerDialog.java` → import `TriangleColorPickerView` (`features/color_picker`)
  4. `shared/preset/PresetExampleActivity.java` → import `TextConfig` (+ `core/FloatingService`)
- **Feature modules TIDAK saling depend** dan **tidak import core/ui/utils** — bersih.
- Feature → shared hanya ke `shared.config` + `shared.ui`; khusus battery tambah `shared.color` (`BatteryColors`). Tidak ada feature yang memakai `shared.preset` — preset hanya dipakai controller di `:app`.
- `ColorPickerDialog` dipakai **8 panel controller** (BatteryBar, Battery, Clock, Crosshair, Fps, Memory, Network, Text) → wajib tetap tinggal disatu lapisan berbagi.
- `DpadController` & `PresetManager` memakai **`android.R.layout.simple_list_item_1` / `simple_list_item_single_choice`** (resource framework, bukan app) → tidak menahan resource app.

### 1b. Fakta resource penting

- `CrosshairModule.java:255` memakai `getIdentifier("crosshair_" + styleIndex, "drawable", ctx.getPackageName())` → **44 PNG `crosshair_*.png` di `drawable-nodpi/` tidak bisa lepas dari `:app`** (lookup dinamis nama resource; hanya `CrosshairModule` yang memakainya secara dinamis).
- `ic_monitor`/`ic_overlay` dipakai `MainActivity` (case `navFps`) + `menu_battery_bottom_nav` + `menu_memory_bottom_nav` — semua penggunanya di `:app` (menu tidak ikut pindah) → **tetap di app** (divergensi dari rencana lama yang memindah ke `:shared:ui`).
- `dialog_color_picker.xml` & `panel_color_picker.xml` sama-sama pakai `@drawable/ic_edit` & `@drawable/seekbar_thumb`. Karena `dialog_color_picker` pindah ke `:shared:color`, kedua drawable ikut ke sana; `panel_color_picker` (di `:app`) tetap resolve via merger resource library.
- `dialog_preset_browser.xml` merefer `@color/colorAccent` (didefinisikan di `app/values/colors.xml`) & style Material (`Widget.MaterialComponents.*`) → saat pindah ke `:shared:preset`, module harus menyediakan `colorAccent` sendiri (atau ganti referensi ke `?attr/colorAccent`) + dependensi `com.google.android.material:material`.
- `notification_custom.xml` + `ic_notification_{invisible,open,toggle,visible}` + `ic_close` dipakai `core/NotificationHelper` → ikut `:core:service`. `ic_close` juga dipakai `drawer_content.xml` (`:app`) — tetap resolve via merger (app depend ke core).
- `ic_notification_stop.xml`, `ic_notification_toggle_off.xml`, `preset_list_item.xml` = **resource mati** (tidak dirujuk kode/layout manapun) → tidak ikut pindah, tetap di app.
- `tooltip_info.xml` + style `TooltipFadeAnimation` dipakai `InfoTooltip` yang pindah ke `:app` → resource tetap di app.
- `BatteryRingView` adalah satu-satunya file di paket `features/*` yang import `exp.ftxt.R` (warna `bat_monitor_*`, `bat_session_*`) → tidak bisa masuk feature module murni Java.

---

## 2. Pilihan Jalur Y (Keputusan)

Dua jalur pernah dievaluasi. **Jalur Y dipilih user.**

| Jalur | Isi feature module | Konsekuensi |
|-------|--------------------|-------------|
| **X (penuh)** | Config + Module + DB/monitor/estimator + **views, panel controllers, fragments, layout** | Mensyaratkan pemutusan cycle `feature → core/FloatingService` & `feature → app/MainActivity`. Risiko tinggi, melebihi estimasi lama. **Ditolak.** |
| **Y (pragmatis) ✅** | Config + Module overlay + DB/monitor/estimator — **murni Java, tanpa resource** | Views custom, panel controllers, fragments, layout **tetap di `:app`**. Tidak ada cycle → tiap fase bisa di-build & diuji individual. Smart Panel ditunda — split didahulukan. |

Konsekuensi: feature module tidak membawa UI. `:app` yang menyatukan UI dengan modul-modulnya. `ui/` di app tetap 42% dari total — itu hasil yang disepakati.

---

## 3. Struktur Module Target (Jalur Y)

```
FTxT/
├── settings.gradle                 ← include app + shared:* + feature:* + core
├── build.gradle                    ← root build config (sudah ada)
│
├── shared/
│   ├── config/                     ✅ SELESAI (Fase 1)
│   ├── ui/                         ← 10 file pure Java
│   ├── color/                      ← 6 file + dialog color picker
│   └── preset/                     ← 4 file + dialog preset browser
│
├── features/                       (baru di root — file dari app/src/main/java/exp/ftxt/features/*)
│   ├── floating-text/
│   ├── fps/
│   ├── clock/
│   ├── battery/                    ← gabungan battery_stats + battery_bar (inti, tanpa RingView)
│   ├── memory/
│   ├── network/
│   └── crosshair/
│
├── core/                           ← :core:service (7 file)
│
└── app/                            ← shell + seluruh UI controller/view/fragment/layout
```

Paket Java (`exp.ftxt.*`) **tidak berubah** — hanya lokasi folder fisik yang berpindah. Import di file luar modul tidak berubah; cukup tambah dependensi module di `build.gradle`.

---

## 4. Isi Tiap Module

### 4.1 `:shared:config` — DONE

**Dependency:** 0 (leaf)

| File | Paket |
|------|-------|
| `BackgroundConfig.java` | `exp.ftxt.shared.config` |
| `ShadowConfig.java` | `exp.ftxt.shared.config` |

Resource: tidak ada. Status: selesai (Fase 1).

### 4.2 `:shared:ui` — Komponen UI Shared (pure Java)

**Dependency:** `:shared:config` + `androidx.appcompat:appcompat` (kelas-kelasnya memakai widget AppCompat).

| File | Keterangan |
|------|-----------|
| `OverlayModule.java` | Interface overlay module |
| `ShadowTextView.java` | Custom TextView shadow + background |
| `ShadowImageView.java` | Custom ImageView shadow |
| `OverlayShadow.java` | Apply elevation-based shadow |
| `OverlayDragHandler.java` | Touch listener drag |
| `DpadController.java` | Kontrol D-Pad (pakai `android.R.layout`) |
| `SliderPositionController.java` | Slider X/Y normalized 0–1000 |
| `SliderLabelEditor.java` | Dialog edit nilai slider |
| `SectionHelper.java` | Utility collapsible section |
| `PresetPreviewView.java` | Preview posisi overlay |

**Resource:** TIDAK ada (semua murni Java; `DpadController` pakai resource framework Android).

**Divergensi dari rencana lama:** `BasePanelFragment`, `BatteryChartView`, `BatterySessionBarChartView`, `ActivityBarView`, `InfoTooltip`, `ColorPickerDialog` **tidak** masuk sini. Yang pertama lima → `:app` (pakai app R + features), `ColorPickerDialog` → `:shared:color`, `BasePanelFragment` tetap di app.

### 4.3 `:shared:color` — Color Picker + Math

**Dependency:** `androidx.appcompat:appcompat` (`ColorPickerDialog` memakai `AlertDialog`).

| File | Asal |
|------|------|
| `ColorMath.java` | sudah `shared/color` |
| `ColorNameResolver.java` | sudah `shared/color` |
| `HSVColorPickerView.java` | sudah `shared/color` |
| `BatteryColors.java` | sudah `shared/color` |
| `TriangleColorPickerView.java` | **ekspor dari `features/color_picker`** (pra-langkah P0) |
| `ColorPickerDialog.java` | **ekspor dari `shared/ui`** |

**Resource:**
- Layout: `dialog_color_picker.xml`
- Drawable: `ic_edit.xml`, `seekbar_thumb.xml`

**Catatan:** `panel_color_picker.xml` tetap di `:app` dan memakai `@drawable/ic_edit`/`seekbar_thumb` dari module ini via merger. Namespace: `exp.ftxt.shared.color`. `features/color_picker/` selanjutnya dihapus.

### 4.4 `:shared:preset` — Sistem Preset

**Dependency:** `:shared:config` + `androidx.appcompat:appcompat` + `androidx.recyclerview:recyclerview` + `com.google.android.material:material` + `com.google.code.gson:gson` (`PresetManager` memakai Gson; `PresetBrowserDialog` pakai AlertDialog/RecyclerView/TextInputEditText).

| File | Keterangan |
|------|-----------|
| `OverlayPreset.java` | Model preset |
| `PresetManager.java` | CRUD preset (pakai `android.R.layout`) |
| `PresetHandler.java` | Delegate preset per modul |
| `PresetBrowserDialog.java` | DialogFragment browser preset |

**Pindah keluar:** `PresetExampleActivity.java` → `:app` (import `core.FloatingService` + `features.floating_text.TextConfig`; tidak terdaftar di manifest — file contoh demo).

**Resource:**
- Layout: `dialog_preset_browser.xml`, `preset_browser_item.xml`
- Drawable: `ic_star_filled.xml`, `ic_star_outline.xml`, `vertical_divider.xml`

**Catatan build:** `dialog_preset_browser.xml` merefer `@color/colorAccent` → dituntaskan dengan **definisi lokal** `colorAccent` (nilai app `#D81B60`) di `values/colors.xml` module + dependensi `com.google.android.material:material` (`Widget.MaterialComponents.*`). Namespace: `exp.ftxt.shared.preset`.

### 4.5 Feature Modules (semua `:feature:*`)

Pola umum — **murni Java, TANPA resource, TANPA view/controller/fragment**:

```
features/<name>/
├── <Name>Config.java
└── <Name>Module.java
```

Dependency dasar semua feature: `:shared:config`, `:shared:ui`.

| Module | Isi | Tambahan |
|--------|-----|----------|
| `:feature:floating-text` | `TextConfig`, `TextModule` | — |
| `:feature:fps` | `FpsConfig`, `FpsModule` | — |
| `:feature:clock` | `ClockConfig`, `ClockModule` | — |
| `:feature:memory` | `MemoryConfig`, `MemoryModule`, `MemoryMonitor` | — |
| `:feature:network` | `NetworkConfig`, `NetworkModule` | — |
| `:feature:crosshair` | `CrosshairConfig`, `CrosshairModule` | PNG crosshair **tetap di `:app`** |
| `:feature:battery` | `BatteryStatsConfig`, `BatteryStatsModule`, `BatteryBarConfig`, `BatteryBarModule`, `BatteryBarView`, `BatteryReading`, `BatteryHistoryDb`, `BatteryMonitor`, `BatteryCapacityEstimator`, `DischargeTracker`, `SessionRebuild`, `SessionSegmentBuilder` | + `(:shared:color)` untuk `BatteryColors` |

**Divergensi battery dari rencana lama:**
- `BatteryRingView` → pindah ke `:app` (`ui/`), karena import `exp.ftxt.R` (warna `bat_monitor_*`, `bat_session_*`) dan dipakai `BatteryMonitorTabController`, `BatterySessionLiveController`, `SessionDetailActivity` (semua di app).
- `BatteryBarView` → **ikut `:feature:battery`** (dipakai `BatteryBarModule` yang dipanggil `FloatingService`; hanya import `shared.color.BatteryColors`, tanpa R).
- Panel controller, tab Monitor, grafik, aktivitas battery (~13 file `ui/Battery*` + `Session*`) → **tetap di `:app`**.
- Tidak ada `BatteryMonitorService` → monitor baterai tetap ditarik dari `FloatingService` (`:core:service`) yang depend ke `:feature:battery`.

**Resource feature:** tidak ada. Namespace contoh: `exp.ftxt.features.battery` (opsional, tidak ada R di module feature).

### 4.6 `:core:service`

**Dependency:** semua `:feature:*` + `:shared:ui`

| File |
|------|
| `FloatingService.java` (God class — kelola semua overlay + monitor baterai full-aktif) |
| `NotificationHelper.java` |
| `BootReceiver.java` |
| `NotificationActionReceiver.java` |
| `WakeLockManager.java` |
| `CrashLogger.java` |
| `AnrWatcher.java` (ada sejak v4.88) |

**Resource:**
- Layout: `notification_custom.xml`
- Drawable: `ic_notification_invisible.xml`, `ic_notification_open.xml`, `ic_notification_toggle.xml`, `ic_notification_visible.xml`, `ic_close.xml`

**Nota:**
- `FloatingService` & `NotificationHelper` mengubah import `exp.ftxt.R` → `exp.ftxt.core.R`. (`FloatingService` saat ini import `exp.ftxt.R` yang **tidak terpakai** → hapus.)
- `drawer_content.xml` (`:app`) tetap dapat `@drawable/ic_close` via merger resource library.
- Component (service/receiver) **tetap terdaftar di manifest `:app`** — manifest tidak pindah ke sini.
- Module terakhir yang diekstrak (paling coupled).

### 4.7 `:app` — Shell + UI Layer

**Dependency:** semua module.

| Kelompok | Isi |
|----------|-----|
| Activity | `MainActivity`, `SettingsActivity`, `DocumentationActivity` + `ui/` `BatteryChartDetailActivity`, `SessionDetailActivity`, `SessionListActivity` |
| Panel/UI | semua `ui/*PanelController`, `ui/*PositionController`, `ui/*OrderZonesView`, `BasePanelFragment`, `PanelManager`, `ui/fragment/*` (10 fragment), `PermissionHelper` (`utils/`) |
| Views ekspor masuk | `ActivityBarView`, `BatteryChartView`, `BatterySessionBarChartView`, `InfoTooltip` (dari `shared/ui`) + `BatteryRingView` (dari `features/battery_stats`) |
| Demo | `PresetExampleActivity` (dari `shared/preset`) |
| Resource tersisa | semua layout (kecuali dialog pindahan), menu, crosshair PNG (44), sebagian besar drawable, `colors.xml` (`bat_monitor_*`, `bat_session_*`, `mem_badge_*`, dll), ids, strings, styles |
| Manifest | `AndroidManifest.xml` (semua activity/service/receiver tetap di sini) |

---

## 5. Dependency Graph

```
:app
├── :core:service
│   ├── :feature:floating-text ──> :shared:ui ──> :shared:config
│   ├── :feature:fps            ──> :shared:ui ──> :shared:config
│   ├── :feature:clock          ──> :shared:ui ──> :shared:config
│   ├── :feature:network        ──> :shared:ui ──> :shared:config
│   ├── :feature:memory         ──> :shared:ui ──> :shared:config
│   ├── :feature:crosshair      ──> :shared:ui ──> :shared:config
│   ├── :feature:battery        ──> :shared:ui ──> :shared:config
│   │                            └─> :shared:color
│   └── :shared:ui
├── :shared:config
├── :shared:ui         ──> :shared:config
├── :shared:color      (leaf)
├── :shared:preset     ──> :shared:config
└── (semua :feature:* di atas, langsung dari app)
```

**Aturan:**
- Shared (config/ui/color/preset) TIDAK boleh depend ke feature/core.
- Feature TIDAK boleh depend ke feature lain, dan TIDAK boleh ke app/core/ui-controller.
- Core depend ke semua feature (tidak bisa dihindari; `FloatingService` mengelola semua overlay + monitor baterai).
- App depend ke semua module (shell + UI).
- Semua leaf memiliki 0 dependency — bisa dibangun terpisah kapan saja.

---

## 6. Fase Pengerjaan (Jalur Y)

Satu fase per iterasi: selesai → build verifikasi → commit → fase berikutnya. Tiap fase meninggalkan project dalam kondisi buildable. Build hanya dijalankan dengan izin user (aturan kerja: konfirmasi dulu).

### Pra-langkah P0: Bedah UI dalam `:app` (kondisi tetap buildable)

Tujuan: menghilangkan semua pelanggaran "shared → features" SEBELUM module dipecah, dan menyiapkan pembagian file.

- [x] Pindah `shared/ui/ActivityBarView`, `BatteryChartView`, `BatterySessionBarChartView`, `InfoTooltip` → paket `ui/` (app). Update import pemakai:
  - `BatteryChartDetailActivity` (ActivityBarView, BatteryChartView)
  - `BatteryChartHistoryController` (BatteryChartView)
  - `BatterySessionHistoryController` (BatterySessionBarChartView, InfoTooltip)
  - `BatteryHealthCardController` (InfoTooltip), `SessionListActivity` (InfoTooltip)
- [x] Pindah `features/battery_stats/BatteryRingView` → paket `ui/`. Update import di `BatteryMonitorTabController`, `BatterySessionLiveController`, `SessionDetailActivity`.
- [x] Pindah `shared/preset/PresetExampleActivity` → app (paket `exp.ftxt` atau `ui`).
- [x] Pindah `features/color_picker/TriangleColorPickerView` → paket `shared/color`. Update import di `ColorPickerDialog` & `ColorPickerPanelController`.
- [x] Pindah `shared/ui/ColorPickerDialog` → paket `shared/color` (penetapan tempat awal). Update import di 8 panel controller.
- [x] Build verifikasi — `assembleDebug` sukses.

**Hasil:** paket `shared/ui` tinggal 10 file murni Java; `shared/preset` tinggal 4 file; `features/color_picker` sudah kosong (folder masih ada, dihapus di Fase 7).

### Fase 1: `:shared:config` — ✅ SELESAI

Tidak ada pekerjaan. Hanya verifikasi build/config.

### Fase 2: `:shared:ui` — ✅ SELESAI

- [x] Buat `shared/ui/build.gradle` (namespace `exp.ftxt.shared.ui`, minSdk 26, Java 17, depend `:shared:config` + `androidx.appcompat:appcompat`).
- [x] `include ':shared:ui'` di `settings.gradle`; `:app` menambahkan dependensi `project(':shared:ui')`.
- [x] Pindah **10 file** dari `app/src/main/java/exp/ftxt/shared/ui/` ke `shared/ui/src/main/java/exp/ftxt/shared/ui/` (murni Java, tanpa `import exp.ftxt.R`).
- [x] Build verifikasi — `assembleDebug` sukses (`ui-debug.aar` + apk `FTxT-v4.92.1-Beta.apk`). Tidak ada perubahan source setelah build terakhir.

**Risiko:** Rendah. Import antar file dalam paket sama → nyaris tanpa edit.

### Fase 3: `:shared:color` — ✅ SELESAI

- [x] Buat `shared/color/build.gradle` (namespace `exp.ftxt.shared.color`, minSdk 26, Java 17, depend `androidx.appcompat:appcompat`).
- [x] `include ':shared:color'` di `settings.gradle`; `:app` menambahkan dependensi `project(':shared:color')`.
- [x] Pindah 6 file (ColorMath, ColorNameResolver, HSVColorPickerView, BatteryColors, TriangleColorPickerView, ColorPickerDialog) ke `shared/color/src/main/java/exp/ftxt/shared/color/`.
- [x] Pindah resource: `dialog_color_picker.xml`, `ic_edit.xml`, `seekbar_thumb.xml`.
- [x] Update import `exp.ftxt.R` → `exp.ftxt.shared.color.R` di `ColorPickerDialog`.
- [x] Hapus folder `features/color_picker/` (kosong). Build verifikasi — `assembleDebug` sukses (`color-debug.aar` + apk terbaru).

**Risiko:** Rendah.

### Fase 4: `:shared:preset` — ✅ SELESAI

- [x] Buat `shared/preset/build.gradle` (namespace `exp.ftxt.shared.preset`, depend `:shared:config` + appcompat + recyclerview + material + gson).
- [x] `include ':shared:preset'`; tambah dependensi dari `:app`.
- [x] Pindah 4 file (OverlayPreset, PresetManager, PresetHandler, PresetBrowserDialog) ke `shared/preset/src/main/java/exp/ftxt/shared/preset/`.
- [x] Pindah resource: `dialog_preset_browser.xml`, `preset_browser_item.xml`, `ic_star_filled.xml`, `ic_star_outline.xml`, `vertical_divider.xml`.
- [x] Update import `exp.ftxt.R` → `exp.ftxt.shared.preset.R`; referensi `@color/colorAccent` dituntaskan definisi lokal (`#D81B60`) di `values/colors.xml`. Build verifikasi — `assembleDebug` sukses (`preset-debug.aar` + apk terbaru).

**Risiko:** Sedang. Layout merefer warna & style dari app.

### Fase 5: Feature Modules — ✅ SELESAI

Urutan: **clock → fps → network → floating-text → memory → crosshair → battery.**

- [x] `:feature:clock`, `:feature:fps`, `:feature:network`, `:feature:floating-text`, `:feature:memory`, `:feature:crosshair` — masing-masing `build.gradle` (namespace `exp.ftxt.features.<name>`, depend `:shared:config` + `:shared:ui`) + 2–3 file Config/Module dipindah.
- [x] `:feature:battery` — gabungan `battery_bar` (3 file: Config, Module, BarView) + `battery_stats` (9 file: Config, Module, Reading, HistoryDb, Monitor, CapacityEstimator, DischargeTracker, SessionRebuild, SessionSegmentBuilder); depend tambah `:shared:color` (untuk `BatteryColors`).
- [x] `include ':features:<name>'` (7 module) di `settings.gradle`; `:app` menambahkan dependensi ke 7 module feature (`:core:service` belum ada — dependensi core ditambahkan di Fase 6).
- [x] 25 file Java dipindah via `git mv` dari `app/src/main/java/exp/ftxt/features/...` → `features/<name>/src/main/java/exp/ftxt/features/...`; paket Java tidak berubah. Folder `app/.../features/` kini kosong.
- [x] Build verifikasi — `assembleDebug` sukses: 7 AAR feature (`*-debug.aar`) + APK `FTxT-v4.92.1-Beta.apk`. 0 error.

**Catatan fix build (dependensi bocor):** Langkah pertama build gagal (35 error: `ShadowImageView cannot be converted to View` / `cannot access AppCompatImageView`) di `:feature:crosshair` karena `:shared:ui` mendeklarasikan `androidx.appcompat` sebagai `implementation`, padahal kelas publiknya (`ShadowTextView`, `ShadowImageView`, dll.) mengekspos tipe AppCompat ke consumer. Diperbaiki `:shared/ui/build.gradle` menjadi `api` untuk `:shared:config` + appcompat (API leaking). Consumer feature mana pun yang memakai `ShadowTextView`/`ShadowImageView` kini mendapat appcompat di compile classpath.

**Risiko per fase:** Rendah–Sedang. Paket Java tidak berubah → sebagian besar tanpa edit import; `:app` cukup ditambah dependensi.

### Fase 6: `:core:service`

- [ ] Buat `core/build.gradle` (namespace `exp.ftxt.core`, depend semua `:feature:*` + `:shared:ui`).
- [ ] `include ':core'`; `:app` menambahkan dependensi.
- [ ] Pindah 7 file core ke `core/src/main/java/exp/ftxt/core/`.
- [ ] Pindah resource: `notification_custom.xml` + 5 drawable notifikasi.
- [ ] Update import `exp.ftxt.R` → `exp.ftxt.core.R` di `NotificationHelper`; hapus import R tak terpakai di `FloatingService`. Build verifikasi.

**Risiko:** Tinggi. `FloatingService` depend ke semua feature — verifikasi semua referensi `features.*` tersedia di classpath.

### Fase 7: `:app` Cleanup

- [ ] Pastikan `MainActivity`, `SettingsActivity`, `DocumentationActivity`, `PanelManager`, seluruh `ui/`, `utils/PermissionHelper`, `PresetExampleActivity`, `BatteryRingView` (+ view ekspor) berada di app.
- [ ] Hapus file Java yang sudah kosong di `app/src/main/java/exp/ftxt/shared/` & `features/` (yang tidak dipindahkan).
- [ ] Hapus resource mati yang tidak ikut pindah (`ic_notification_stop.xml`, `ic_notification_toggle_off.xml`, `preset_list_item.xml`) dan resource dialog yang sudah pindah.
- [ ] Update `settings.gradle` final + cek semua dependensi. Build + test menyeluruh.

### Fase 8: Optimasi Build

- [ ] Setup common config (ext block) untuk `compileSdk`/`minSdk`/Java version di root `build.gradle`.
- [ ] Cek incremental build (edit satu module → module lain tidak compile ulang).

### Fase 9: Testing Akhir

- [ ] Semua overlay (Text, FPS, Clock, Battery Info 3 tab, Battery Strip, Network, Memory, Crosshair) berfungsi.
- [ ] Tab Monitor: ring gauge, grafik riwayat (slider rentang), kartu kesehatan, snapshot.
- [ ] Monitor baterai full-aktif: nyala saat app dibuka, restart saat boot, sampling dinamis.
- [ ] Fitur Developer lock, preset (save/load/search/tag/share/selective apply), auto-start boot, notifikasi (ikon dinamis suhu + aksi), CrashLogger, wake lock, dark/light theme, orientasi, backward compatibility preset lama, install fresh + upgrade.

### (Di luar scope) `:shared:resource`

Resource Pack System belum dibangun di kode (hanya dokumen `_schedule/PRIORITAS/RESOURCE_PACK_SYSTEM.md`). Tidak ikut fase split. Jika nanti diimplementasikan, tambahkan sebagai module shared baru — tidak mengganggu struktur Jalur Y.

---

## 7. Risiko & Mitigasi

| Risiko | Dampak | Mitigasi |
|--------|--------|----------|
| `dialog_preset_browser.xml` merefer `@color/colorAccent` dari app | Compile error di `:shared:preset` | Definisi `colorAccent` lokal di module atau ubah ke `?attr/colorAccent` (Fase 4) |
| `panel_color_picker.xml` memakai drawable yang pindah ke library | Resource hilang saat app di-build tanpa module | Pastikan `:app` depend `:shared:color`; merger library menyediakan resource |
| `BatteryRingView` import `exp.ftxt.R` | Tidak bisa masuk feature module | Dijadwalkan pindah ke `ui/` (P0) |
| `CrosshairModule` lookup dinamis `crosshair_*` | Crosshair kosong jika PNG ikut modul | PNG tetap di `:app`; module hanya Config+Module |
| `FloatingService` referensi semua `features.*` | Compile error di `:core:service` | Core dibuat terakhir; verifikasi classpath sebelum commit |
| `ic_close` dipakai app & core | Duplikat atau error | Cukup di core; app resolve via merger |
| Import typo saat pindah file | Compile error menyebar | Pindah per fase, build langsung, paket Java tidak diganti → minimal impor berubah |
| Package name berubah | Breaking preset lama (prefs key) | Package name **TIDAK berubah** — hanya lokasi fisik |
| Resource ID conflict antar module | Crash runtime | Resource yang pindah unik per module; yang berbagi dibatasi (`ic_edit`, `ic_close`) via merger |
| Build lebih lambat karena banyak module | Kebalikan tujuan | Parallel build aktif (`gradle.properties`); verifikasi incremental di Fase 8 |

---

## 8. Yang TIDAK Berubah

- **Package names** — `exp.ftxt.shared.ui.*`, `exp.ftxt.features.battery_stats.*`, dll. tetap sama; hanya lokasi fisik berpindah.
- **SharedPreferences keys** — `battery_*`, `batbar_*`, `mem_*`, `crosshair_*`, dll. tetap sama.
- **Preset format** — JSON `OverlayPreset` tetap sama.
- **User-facing behavior** — tidak ada perubahan fitur.
- **AndroidManifest** — tetap di `:app`; semua activity/service/receiver terdaftar di sana.
- Versi tetap **4.92.x / 4.93.x** mengikuti aturan CHANGELOG — split tidak menaikkan major.

---

## 9. Estimasi Waktu (Jalur Y)

| Fase | Estimasi | Keterangan |
|------|----------|------------|
| P0: Bedah UI dalam app | 1–2 jam | 8 file pindah antar paket + update import |
| Fase 1: `:shared:config` | selesai | terverifikasi |
| Fase 2: `:shared:ui` | 30–45 mnt | 10 file, tanpa edit import antar-paket |
| Fase 3: `:shared:color` | 30–60 mnt | 6 file + resource |
| Fase 4: `:shared:preset` | 30–60 mnt | 4 file + resource + warna/style |
| Fase 5: 7 feature modules | 3–5 jam | repetitive; battery paling besar (12 file) → selesai; 1 fix build (`api` di `:shared:ui`) |
| Fase 6: `:core:service` | 45–60 mnt | paling tricky |
| Fase 7: app cleanup | 30–45 mnt | hapus sisa + resource mati |
| Fase 8: optimasi | 30 mnt | common config + incremental check |
| Fase 9: testing | 1–2 jam | menyeluruh |
| **Total** | **8–13 jam** | gampar: ±sepuluh jam |

Berbeda dari estimasi lama (13–21 jam) karena: Jalur Y lebih ringan (tanpa mengekstrak UI), `:shared:resource` dikeluarkan dari scope, dan Fase 1 sudah selesai.

---

## 10. Kapan Harus Dihentikan?

Jangan lanjut split jika:
1. Build gagal di fase manapun dan tidak bisa diperbaiki dalam 30 menit.
2. Ada bug regression yang muncul setelah split.
3. Waktu tersisa tidak cukup untuk testing menyeluruh.

Lebih baik single-module yang berfungsi daripada multi-module yang rusak.

---

## 11. Referensi

- [Android Official: Multi-module apps](https://developer.android.com/modularize)
- [Gradle Multi-project Builds](https://docs.gradle.org/current/userguide/multi_project_builds.html)
- Dokumen terkait: `_schedule/PRIORITAS/SMART_PANEL_CROSSHAIR.md` (fitur ditunda setelah split), `_schedule/PRIORITAS/RESOURCE_PACK_SYSTEM.md` (belum dibangun)