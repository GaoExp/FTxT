# 📁 Struktur Project FTxT

```
FTxT/
├── CHANGELOG.md
├── README.md
├── STRUKTUR.md
├── PANDUAN.md
├── DEVELOPMENT.md
├── TENTANG.md
├── app/
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
│   │   │   │   ├── cpu/         (empty)
│   │   │   │   ├── clock/       (empty)
│   │   │   │   ├── crosshair/   (empty)
│   │   │   │   ├── logo/        (empty)
│   │   │   │   └── watermark/   (empty)
│   │   │   │
│   │   │   ├── shared/
│   │   │   │   ├── color/
│   │   │   │   │   ├── ColorMath.java
│   │   │   │   │   ├── ColorNameResolver.java
│   │   │   │   │   └── HSVColorPickerView.java
│   │   │   │   └── ui/
│   │   │   │       ├── ColorPickerDialog.java
│   │   │   │       ├── OverlayDragHandler.java
│   │   │   │       ├── OverlayShadow.java
│   │   │   │       ├── ShadowConfig.java
│   │   │   │       ├── ShadowTextView.java
│   │   │   │       ├── SliderLabelEditor.java
│   │   │   │       └── XyPadView.java
│   │   │   │
│   │   │   ├── ui/
│   │   │   │   ├── TextPanelController.java
│   │   │   │   ├── PositionController.java
│   │   │   │   └── FpsPanelController.java
│   │   │   │
│   │   │   ├── utils/
│   │   │   │   └── PermissionHelper.java
│   │   │   │
│   │   │   ├── MainActivity.java
│   │   │   └── SettingsActivity.java
│   │   │
│   │   ├── assets/
│   │   │   ├── AGENTS.txt
│   │   │   ├── CHANGELOG.txt
│   │   │   ├── README.txt
│   │   │   ├── STRUKTUR.txt
│   │   │   ├── PANDUAN.txt
│   │   │   ├── DEVELOPMENT.txt
│   │   │   └── TENTANG.txt
│   │   │
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   ├── activity_main.xml
│   │   │   │   ├── activity_settings.xml
│   │   │   │   ├── dialog_hsv_color_picker.xml
│   │   │   │   └── nav_header.xml
│   │   │   ├── drawable/
│   │   │   │   ├── ic_edit.xml
│   │   │   │   ├── ic_launcher_background.xml
│   │   │   │   ├── ic_launcher_foreground.png
│   │   │   │   ├── ic_settings.xml
│   │   │   │   ├── ic_sun.xml
│   │   │   │   ├── ic_theme.xml
│   │   │   │   └── splash_screen.xml
│   │   │   ├── menu/
│   │   │   │   └── drawer_menu.xml
│   │   │   ├── mipmap-anydpi-v26/
│   │   │   │   └── ic_launcher.xml
│   │   │   └── values/
│   │   │       ├── colors.xml
│   │   │       ├── strings.xml
│   │   │       ├── styles.xml
│   │   │       ├── themes.xml
│   │   │       └── values-v31/
│   │   │           └── themes.xml
│   │   │
│   │   └── AndroidManifest.xml
│   │
│   └── build.gradle
│
└── build.gradle
```

### Deskripsi File

| File | Deskripsi |
|------|-----------|
| MainActivity.java | Activity utama & kontrol UI (delegasi ke panel controllers) |
| SettingsActivity.java | Settings dengan dokumentasi in-app & toggle izin |
| core/FloatingService.java | Overlay service, touch handling, persistence |
| core/NotificationHelper.java | Helper channel & notifikasi foreground |
| core/WakeLockManager.java | Manajemen partial wake lock |
| modules/text/TextConfig.java | Konfigurasi teks overlay |
| modules/text/TextModule.java | Module logic teks overlay |
| modules/fps/FpsConfig.java | Konfigurasi FPS overlay |
| modules/fps/FpsModule.java | Module logic FPS overlay |
| shared/color/HSVColorPickerView.java | Custom HSV circular picker |
| shared/color/ColorMath.java | Utilitas HSV color math |
| shared/color/ColorNameResolver.java | Konversi warna ke nama |
| shared/ui/ColorPickerDialog.java | Dialog wrapper color picker (ARGB) |
| shared/ui/OverlayDragHandler.java | Shared drag-to-move touch listener |
| shared/ui/OverlayShadow.java | Shared shadow bg + elevation |
| shared/ui/ShadowConfig.java | Konfigurasi shadow modular |
| shared/ui/ShadowTextView.java | Custom TextView dengan text shadow di onDraw() |
| shared/ui/SliderLabelEditor.java | Shared utility edit nilai slider via dialog |
| shared/ui/XyPadView.java | Custom View 2D drag area untuk kontrol posisi |
| ui/TextPanelController.java | Controller panel Floating Text |
| ui/PositionController.java | Controller kontrol posisi (slider, d-pad, xy-pad) |
| ui/FpsPanelController.java | Controller panel FPS Display |
| utils/PermissionHelper.java | Helper permission overlay/notifikasi/baterai |
| activity_main.xml | Layout utama |
| activity_settings.xml | Layout settings menu |
| dialog_hsv_color_picker.xml | Layout dialog color picker |
| nav_header.xml | Header navigation drawer |
