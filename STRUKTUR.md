# 📁 Struktur Project FTxT

```
FTxT/
├── AGENTS.md
├── CHANGELOG.md
├── README.md
├── STRUKTUR.md
├── _karantina/
│   └── exp/ftxt/shared/ui/
│       ├── XyPadView.java
│       └── PositionPresetManager.java
├── PANDUAN.md
├── DEVELOPMENT.md
├── TENTANG.md
├── settings.gradle
├── gradle.properties
├── app/
│   ├── proguard-rules.pro
│   ├── libs/
│   ├── src/main/
│   │   ├── java/exp/ftxt/
│   │   │   ├── core/
│   │   │   │   ├── FloatingService.java
│   │   │   │   ├── NotificationHelper.java
│   │   │   │   └── WakeLockManager.java
│   │   │   │
│   │   │   ├── features/
│   │   │   │   ├── text/
│   │   │   │   │   ├── TextConfig.java
│   │   │   │   │   └── TextModule.java
│   │   │   │   ├── fps/
│   │   │   │   │   ├── FpsConfig.java
│   │   │   │   │   └── FpsModule.java
│   │   │   │   ├── battery/
│   │   │   │   │   ├── BatteryConfig.java
│   │   │   │   │   └── BatteryModule.java
│   │   │   │   ├── clock/
│   │   │   │   │   ├── ClockConfig.java
│   │   │   │   │   └── ClockModule.java
│   │   │   │   ├── network/
│   │   │   │   │   ├── NetworkConfig.java
│   │   │   │   │   └── NetworkModule.java
│   │   │   │   ├── crosshair/   (empty)
│   │   │   │   ├── cpu/         (empty)
│   │   │   │   ├── logo/        (empty)
│   │   │   │   └── watermark/   (empty)
│   │   │   │
│   │   │   ├── shared/
│   │   │   │   ├── color/
│   │   │   │   │   ├── ColorMath.java
│   │   │   │   │   ├── ColorNameResolver.java
│   │   │   │   │   └── HSVColorPickerView.java
│   │   │   │   ├── preset/
│   │   │   │   │   ├── OverlayPreset.java
│   │   │   │   │   ├── PresetManager.java
│   │   │   │   │   └── PresetExampleActivity.java
│   │   │   │   └── ui/
│   │   │   │       ├── AppPresetWatcher.java
│   │   │   │       ├── ColorPickerDialog.java
│   │   │   │       ├── DpadController.java
│   │   │   │       ├── OverlayDragHandler.java
│   │   │   │       ├── OverlayShadow.java

│   │   │   │       ├── PresetPreviewView.java
│   │   │   │       ├── ShadowConfig.java
│   │   │   │       ├── ShadowTextView.java
│   │   │   │       ├── SliderLabelEditor.java
│   │   │   │       └── SliderPositionController.java
│   │   │   │
│   │   │   ├── ui/
│   │   │   │   ├── TextPanelController.java
│   │   │   │   ├── TextPositionController.java
│   │   │   │   ├── FpsPanelController.java
│   │   │   │   ├── FpsPositionController.java
│   │   │   │   ├── ClockPanelController.java
│   │   │   │   ├── ClockPositionController.java
│   │   │   │   ├── BatteryPanelController.java
│   │   │   │   ├── BatteryPositionController.java
│   │   │   │   ├── NetworkPanelController.java
│   │   │   │   └── NetworkPositionController.java
│   │   │   │
│   │   │   ├── utils/
│   │   │   │   └── PermissionHelper.java
│   │   │   │
│   │   │   ├── DocumentationActivity.java
│   │   │   ├── MainActivity.java
│   │   │   └── SettingsActivity.java
│   │   │
│   │   ├── assets/
│   │   │   ├── CHANGELOG.txt
│   │   │   ├── DEVELOPMENT.txt
│   │   │   ├── PANDUAN.txt
│   │   │   ├── README.txt
│   │   │   ├── STRUKTUR.txt
│   │   │   └── TENTANG.txt
│   │   │
│   │   ├── res/
│   │   │   ├── anim/
│   │   │   │   ├── settings_popup_enter.xml
│   │   │   │   └── settings_popup_exit.xml
│   │   │   ├── layout/
│   │   │   │   ├── activity_documentation.xml
│   │   │   │   ├── activity_main.xml
│   │   │   │   ├── activity_settings.xml
│   │   │   │   ├── dialog_hsv_color_picker.xml
│   │   │   │   ├── drawer_content.xml
│   │   │   │   ├── nav_header.xml
│   │   │   │   └── preset_list_item.xml
│   │   │   ├── drawable/
│   │   │   │   ├── ic_edit.xml
│   │   │   │   ├── ic_launcher_background.xml
│   │   │   │   ├── ic_launcher_foreground.png
│   │   │   │   ├── ic_settings.xml
│   │   │   │   ├── ic_sun.xml
│   │   │   │   ├── ic_theme.xml
│   │   │   │   └── splash_screen.xml
│   │   │   ├── menu/
│   │   │   │   ├── drawer_menu.xml
│   │   │   │   └── main_menu.xml
│   │   │   ├── mipmap-anydpi-v26/
│   │   │   │   └── ic_launcher.xml
│   │   │   ├── values/
│   │   │   │   ├── colors.xml
│   │   │   │   ├── strings.xml
│   │   │   │   ├── styles.xml
│   │   │   │   └── themes.xml
│   │   │   ├── values-night/
│   │   │   │   └── colors.xml
│   │   │   └── values-v31/
│   │   │       └── themes.xml
│   │   │
│   │   └── AndroidManifest.xml
│   │
│   ├── src/test/
│   │   └── java/exp/
│   │       ├── ftxt/
│   │       │   └── ExampleUnitTest.java
│   │       └── ftxy/
│   │           └── ExampleUnitTest.java
│   │
│   ├── src/androidTest/
│   │   └── java/exp/
│   │       ├── ftxt/
│   │       │   └── ExampleInstrumentedTest.java
│   │       └── ftxy/
│   │           └── ExampleInstrumentedTest.java
│   │
│   └── build.gradle
│
└── build.gradle
```

### Deskripsi File

| File | Deskripsi |
|------|-----------|
| DocumentationActivity.java | Activity dokumentasi in-app dengan daftar 6 dokumen |
| MainActivity.java | Activity utama & kontrol UI (delegasi ke panel controllers) |
| SettingsActivity.java | Settings dengan toggle izin aplikasi |
| core/FloatingService.java | Overlay service, touch handling, persistence |
| core/NotificationHelper.java | Helper channel & notifikasi foreground |
| core/WakeLockManager.java | Manajemen partial wake lock |
| features/text/TextConfig.java | Konfigurasi teks overlay |
| features/text/TextModule.java | Module logic teks overlay |
| features/fps/FpsConfig.java | Konfigurasi FPS overlay |
| features/fps/FpsModule.java | Module logic FPS overlay |
| features/clock/ClockConfig.java | Konfigurasi jam digital overlay |
| features/clock/ClockModule.java | Module logic jam digital (update tiap 1 detik) |
| features/battery/BatteryConfig.java | Konfigurasi suhu baterai overlay |
| features/battery/BatteryModule.java | Module logic suhu baterai (update tiap 5 detik) |
| features/network/NetworkConfig.java | Konfigurasi network speed meter overlay |
| features/network/NetworkModule.java | Module logic network speed (TrafficStats, polling 1 detik) |
| shared/color/HSVColorPickerView.java | Custom HSV circular picker |
| shared/color/ColorMath.java | Utilitas HSV color math |
| shared/color/ColorNameResolver.java | Konversi warna ke nama |
| shared/preset/OverlayPreset.java | Model data preset overlay (posisi, warna, shadow, background, orientasi) |
| shared/preset/PresetManager.java | Manager preset CRUD: Save/Load/Rename/Select/Delete/Export/Import via GSON |
| shared/preset/PresetExampleActivity.java | Contoh implementasi OnClickListener preset di MainActivity |
| shared/ui/AppPresetWatcher.java | Auto-switch preset berdasarkan orientasi |
| shared/ui/ColorPickerDialog.java | Dialog wrapper color picker (ARGB) |
| shared/ui/DpadController.java | Shared D-Pad controller dengan touch repeat |
| shared/ui/OverlayDragHandler.java | Shared drag-to-move touch listener |
| shared/ui/OverlayShadow.java | Shared shadow bg + elevation |
| _karantina/exp/ftxt/shared/ui/PositionPresetManager.java | Manager preset posisi X/Y lama — dipindah ke _karantina, digantikan oleh PresetManager GSON |
| shared/ui/PresetPreviewView.java | Custom View mini-map posisi preset |
| shared/ui/ShadowConfig.java | Konfigurasi shadow modular |
| shared/ui/ShadowTextView.java | Custom TextView dengan text shadow di onDraw() |
| shared/ui/SliderLabelEditor.java | Shared utility edit nilai slider via dialog |
| shared/ui/SliderPositionController.java | Shared controller slider X/Y posisi |
| _karantina/exp/ftxt/shared/ui/XyPadView.java | Custom View 2D drag area — DIKARANTINA |
| ui/TextPanelController.java | Controller panel Floating Text |
| ui/TextPositionController.java | Controller kontrol posisi (slider, d-pad, preset GSON full-config, orientasi) |
| ui/FpsPositionController.java | Controller kontrol posisi (slider, d-pad, preset GSON full-config, orientasi) FPS |
| ui/ClockPositionController.java | Controller kontrol posisi (slider, d-pad, preset GSON full-config, orientasi) |
| ui/BatteryPositionController.java | Controller kontrol posisi (slider, d-pad, preset GSON full-config, orientasi) |
| ui/NetworkPositionController.java | Controller kontrol posisi (slider, d-pad, preset GSON full-config, orientasi) |
| utils/PermissionHelper.java | Helper permission overlay/notifikasi/baterai |
| activity_documentation.xml | Layout halaman dokumentasi |
| activity_main.xml | Layout utama |
| activity_settings.xml | Layout settings menu |
| dialog_hsv_color_picker.xml | Layout dialog color picker |
| drawer_content.xml | Layout sidebar drawer konten |
| nav_header.xml | Header navigation drawer |
| preset_list_item.xml | Layout item daftar preset dengan preview |
