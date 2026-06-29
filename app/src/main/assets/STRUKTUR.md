# 📁 Struktur Project FTxT

```
FTxT/
├── .gitignore                    — Git ignore rules (build, gradle, local)
├── AGENTS.md                     — Pedoman AI agent
├── CHANGELOG.md                  — Riwayat perubahan per release
├── PANDUAN.md                    — Panduan penggunaan lengkap
├── README.md                     — Ringkasan project & fitur
├── STRUKTUR.md                   — Struktur project (file ini)
├── build.gradle                  — Root Gradle (AGP 8.12.0)
├── gradle.properties             — Gradle: AndroidX, JVM args
├── gradlew / gradlew.bat         — Gradle wrapper scripts
├── settings.gradle               — Settings Gradle (include :app)
├── keystore.properties           — Konfigurasi signing release
├── local.properties              — Local SDK/NDK path
│
├── .github/workflows/
│   └── release.yml               — GitHub Actions CI: build APK & GitHub Release
│
├── gradle/wrapper/
│   └── gradle-wrapper.properties — Versi Gradle wrapper
│
├── key/
│   └── audiplay-release-key.jks  — Keystore signing release
│
└── app/
    ├── build.gradle              — Module: minSdk 26, compileSdk/targetSdk 35, Java 17
    ├── proguard-rules.pro        — Aturan ProGuard (custom rules)
    │
    ├── src/main/
    │   ├── AndroidManifest.xml   — Permission: overlay, notif, baterai, wake lock
    │   │
    │   ├── assets/
    │   │   ├── CHANGELOG.md     — Riwayat perubahan (in-app display, sync via Gradle)
    │   │   ├── PANDUAN.md       — Panduan penggunaan (in-app display, sync via Gradle)
    │   │   ├── README.md        — Ringkasan project (in-app display, sync via Gradle)
    │   │   └── STRUKTUR.md      — Struktur project (in-app display, sync via Gradle)
    │   │
    │   ├── java/exp/ftxt/
    │   │   ├── core/
    │   │   │   ├── FloatingService.java     — Foreground service: kelola semua overlay via WindowManager
    │   │   │   ├── NotificationHelper.java  — Notifikasi foreground service
    │   │   │   └── WakeLockManager.java     — Partial wake lock biar CPU tetap aktif
    │   │   │
    │   │   ├── features/
    │   │   │   ├── floating_text/
    │   │   │   │   ├── TextConfig.java      — Konfigurasi statis Text overlay
    │   │   │   │   └── TextModule.java      — Buat/hapus/update Text overlay window
    │   │   │   ├── fps_display/
    │   │   │   │   ├── FpsConfig.java       — Konfigurasi statis FPS overlay
    │   │   │   │   └── FpsModule.java       — FPS counter via Choreographer frame callback
    │   │   │   ├── clock_module/
    │   │   │   │   ├── ClockConfig.java     — Konfigurasi statis Clock overlay
    │   │   │   │   └── ClockModule.java     — Jam real-time HH:mm:ss, update tiap 1 detik
    │   │   │   ├── battery_temperature/
    │   │   │   │   ├── BatteryConfig.java   — Konfigurasi statis Battery overlay
    │   │   │   │   └── BatteryModule.java   — Suhu baterai °C + %, baca dari sticky broadcast
    │   │   │   ├── battery_percentage/
    │   │   │   │   ├── BatteryPercentageConfig.java  — Konfigurasi Battery % overlay
    │   │   │   │   └── BatteryPercentageModule.java  — Persentase baterai, update 5 detik
    │   │   │   ├── battery_current/
    │   │   │   │   ├── BatteryCurrentConfig.java     — Konfigurasi Battery Current overlay
    │   │   │   │   └── BatteryCurrentModule.java     — Tegangan/arus/daya dari 3 sumber (broadcast, API 28+, sysfs)
    │   │   │   ├── network_stats/
    │   │   │   │   ├── NetworkConfig.java  — Konfigurasi statis Network overlay
    │   │   │   │   └── NetworkModule.java  — Kecepatan internet ↓↑ via TrafficStats, 1 detik
    │   │   │   ├── color_picker/
    │   │   │   │   └── TriangleColorPickerView.java — Custom View segitiga HSV untuk Color Picker
    │   │   │   ├── crosshair/      (placeholder — coming soon)
    │   │   │   ├── cpu_monitor/    (placeholder — coming soon)
    │   │   │   └── logo_display/   (placeholder — coming soon)
    │   │   │
    │   │   ├── shared/
    │   │   │   ├── color/
    │   │   │   │   ├── ColorMath.java         — Operasi matematika HSV: gradient, angle, selector posisi
    │   │   │   │   ├── ColorNameResolver.java — Deteksi nama warna dari RGB
    │   │   │   │   └── HSVColorPickerView.java— Custom View: color wheel HSV + crosshair
    │   │   │   ├── preset/
    │   │   │   │   ├── OverlayPreset.java     — Model data preset dengan UUID, metadata, history
    │   │   │   │   ├── PresetManager.java     — CRUD preset: save/load/rename/reorder/search/export/import
    │   │   │   │   ├── PresetHandler.java     — Delegate pattern: save/load dialog infrastructure per modul
    │   │   │   │   ├── PresetBrowserDialog.java   — DialogFragment browser preset dengan search & filter
    │   │   │   │   └── PresetExampleActivity.java — Contoh integrasi sistem preset (referensi dev)
    │   │   │   └── ui/
    │   │   │       ├── BackgroundConfig.java      — Model data background (enable, color, padding, offset, margin, radius)
    │   │   │       ├── ShadowConfig.java          — Model data shadow (enable, color, blur, offset)
    │   │   │       ├── ShadowTextView.java        — Custom TextView dengan shadow + background di onDraw()
    │   │   │       ├── OverlayDragHandler.java    — Touch listener untuk drag overlay
    │   │   │       ├── OverlayShadow.java         — Apply elevation-based shadow ke overlay
    │   │   │       ├── ColorPickerDialog.java     — Dialog color picker: wheel, sliders, HEX/ARGB
    │   │   │       ├── DpadController.java        — Kontrol D-Pad dengan repeat untuk fine position
    │   │   │       ├── SliderPositionController.java  — Slider X/Y posisi normalized 0-1000
    │   │   │       ├── SliderLabelEditor.java     — Dialog edit nilai numerik dari label slider
    │   │   │       ├── SectionHelper.java         — Utility collapsible section toggle ▸/▾
    │   │   │       └── PresetPreviewView.java     — Custom View preview posisi overlay di grid
    │   │   │
    │   │   ├── ui/
    │   │   │   ├── TextPanelController.java            — UI panel Text: input, size, color, shadow, bg, toggle
    │   │   │   ├── TextPositionController.java         — Kontrol posisi Text: D-Pad, slider, preset, koordinat
    │   │   │   ├── FpsPanelController.java             — UI panel FPS: switch, size, color, shadow, bg, value-only
    │   │   │   ├── FpsPositionController.java          — Kontrol posisi FPS dengan preset
    │   │   │   ├── ClockPanelController.java           — UI panel Clock: switch, size, color, shadow, bg
    │   │   │   ├── ClockPositionController.java        — Kontrol posisi Clock dengan preset
    │   │   │   ├── BatteryPanelController.java         — UI panel Battery: temp/pct toggle, size, color
    │   │   │   ├── BatteryPositionController.java      — Kontrol posisi Battery dengan preset
    │   │   │   ├── BatteryPercentagePanelController.java   — UI panel Battery %
    │   │   │   ├── BatteryPercentagePositionController.java— Kontrol posisi Battery %
    │   │   │   ├── BatteryCurrentPanelController.java      — UI panel Battery Current: volt, current, power toggle
    │   │   │   ├── BatteryCurrentPositionController.java   — Kontrol posisi Battery Current
    │   │   │   ├── NetworkPanelController.java             — UI panel Network: switch, size, color, shadow
    │   │   │   ├── NetworkPositionController.java          — Kontrol posisi Network
    │   │   │   ├── ColorPickerPanelController.java         — UI panel Color Picker: wheel, sliders, saved colors
    │   │   │
    │   │   ├── utils/
    │   │   │   └── PermissionHelper.java    — Helper izin: overlay, notifikasi, optimasi baterai
    │   │   │
    │   │   ├── MainActivity.java           — Activity utama: toolbar, nav drawer, panel system, theme toggle
    │   │   ├── SettingsActivity.java        — Manajemen izin: overlay, notifikasi, baterai
    │   │   └── DocumentationActivity.java   — Baca dokumentasi in-app dari assets (.md) via Markwon
    │   │
    │   └── res/
    │       ├── anim/
    │       │   ├── settings_popup_enter.xml — Animasi masuk popup settings
    │       │   └── settings_popup_exit.xml  — Animasi keluar popup settings
    │       ├── drawable/
    │       │   ├── ic_arrow_down.xml        — Ikon panah bawah untuk D-Pad
    │       │   ├── ic_arrow_left.xml        — Ikon panah kiri untuk D-Pad
    │       │   ├── ic_arrow_right.xml       — Ikon panah kanan untuk D-Pad
    │       │   ├── ic_arrow_up.xml          — Ikon panah atas untuk D-Pad
    │       │   ├── ic_close.xml             — Ikon close/X untuk Kill Service
    │       │   ├── ic_dots_vertical.xml     — Ikon tiga titik vertikal (overflow menu)
    │       │   ├── ic_edit.xml              — Ikon pensil untuk edit HEX/nilai
    │       │   ├── ic_exit.xml              — Ikon exit/keluar untuk tombol Keluar
    │       │   ├── ic_launcher_background.xml   — Background launcher adaptive icon
    │       │   ├── ic_launcher_foreground.png   — Foreground launcher adaptive icon
    │       │   ├── ic_launcher_bg.png       — Background ikon aplikasi
    │       │   ├── ic_launcher_foreground_alt.png — Foreground ikon alternatif
    │       │   ├── ic_screen_rotation.xml   — Ikon orientasi layar (toolbar)
    │       │   ├── ic_settings.xml          — Ikon gear untuk settings
    │       │   ├── ic_star_filled.xml       — Ikon bintang solid (favorit)
    │       │   ├── ic_star_outline.xml      — Ikon bintang outline (non-favorit)
    │       │   ├── ic_sun.xml               — Ikon matahari untuk tema terang
    │       │   ├── ic_swap.xml              — Ikon swap untuk tukar mode color picker
    │       │   ├── ic_theme.xml             — Ikon tema gelap/terang
    │       │   ├── seekbar_thumb.xml        — Thumb slider lingkaran 12×12dp
    │       │   ├── splash_screen.xml        — Splash screen drawable
    │       │   ├── bg_alt_light2.png        — Background drawer tema terang
    │       │   ├── bg_main_light2.png       — Background layar utama tema terang
    │       │   ├── appbar_light.png         — Background toolbar tema terang
    │       │   ├── drawbar_light.png        — Background header drawer tema terang
    │       │   ├── drawer_bg.xml            — Drawable wrapper drawer bg terang
    │       │   ├── drawer_header_bg.xml     — Drawable wrapper header drawer terang
    │       │   ├── toolbar_bg.xml           — Drawable wrapper toolbar bg terang
    │       │   ├── vertical_divider.xml     — Divider vertikal untuk bottom bar preset
    │       │   ├── divider_horizontal.xml   — Divider horizontal untuk daftar dokumen
    │       │   └── main_bg.xml              — Drawable wrapper main bg terang
    │       ├── drawable-night/
    │       │   ├── bg_alt.png               — Background drawer tema gelap
    │       │   ├── bg_main_dark.png         — Background layar utama tema gelap
    │       │   ├── appbar_dark.png          — Background toolbar tema gelap
    │       │   ├── drawbar_dark.png         — Background header drawer tema gelap
    │       │   ├── drawer_bg.xml            — Drawable wrapper drawer bg gelap (flip 180°)
    │       │   ├── drawer_header_bg.xml     — Drawable wrapper header drawer gelap
    │       │   ├── toolbar_bg.xml           — Drawable wrapper toolbar bg gelap
    │       │   └── main_bg.xml              — Drawable wrapper main bg gelap
    │       ├── layout/
    │       │   ├── activity_main.xml            — Layout utama dengan DrawerLayout + CoordinatorLayout
    │       │   ├── activity_settings.xml        — Layout halaman pengaturan izin
    │       │   ├── activity_documentation.xml   — Layout halaman dokumentasi in-app
    │       │   ├── dialog_hsv_color_picker.xml  — Dialog color picker mode Color Wheel
    │       │   ├── dialog_hue_slider_picker.xml — Dialog color picker mode Hue Slider
    │       │   ├── dialog_preset_browser.xml    — Dialog browser preset dengan search & list
    │       │   ├── drawer_content.xml           — Konten navigation drawer: RecyclerView item list
    │   │   ├── nav_header.xml               — Header navigation drawer: logo + versi
    │   │   ├── panel_text.xml               — Panel konfigurasi Floating Text
    │   │   ├── panel_fps.xml                — Panel konfigurasi FPS Display
    │   │   ├── panel_clock.xml              — Panel konfigurasi Jam Digital
    │   │   ├── panel_battery.xml            — Panel konfigurasi Suhu Baterai
    │   │   ├── panel_battery_percentage.xml — Panel konfigurasi Battery Percentage
    │   │   ├── panel_battery_current.xml    — Panel konfigurasi Battery Current
    │   │   ├── panel_network.xml            — Panel konfigurasi Network Speed
    │   │   ├── panel_crosshair.xml          — Placeholder Crosshair (coming soon)
    │   │   ├── panel_logo.xml               — Placeholder Logo Display (coming soon)
    │   │   ├── panel_color_picker.xml       — Panel konfigurasi Color Picker
    │   │   ├── preset_browser_item.xml      — Item layout untuk daftar preset
    │   │   ├── preset_list_item.xml         — Item layout alternate untuk preset
    │   │   └── toolbar_zoom.xml             — Kontrol zoom − [+] di toolbar dokumentasi
    │       ├── menu/
    │       │   ├── drawer_menu.xml   — Menu navigation drawer: daftar panel
    │       │   └── main_menu.xml     — Menu toolbar: settings, theme, exit
    │       ├── mipmap-anydpi-v26/
    │       │   ├── ic_launcher.xml   — Adaptive icon launcher (default)
    │       │   └── ic_launcher_alt.xml   — Adaptive icon launcher (alternatif)
    │       ├── values/
    │       │   ├── colors.xml        — Warna: primary, accent, drawer, bg
    │       │   ├── strings.xml       — Semua string UI Bahasa Indonesia
    │       │   ├── styles.xml        — Style: AppTheme, popup animation, popup menu
    │       │   ├── themes.xml        — Theme: SplashScreen
    │       │   └── ids.xml           — ID tetap `R.id.nav*` untuk drawer
    │       ├── values-night/
    │       │   └── colors.xml        — Warna mode gelap: drawer bg, drawer header
    │       └── values-v31/
    │           └── themes.xml        — SplashScreen theme untuk API 31+
    │
    ├── src/test/java/exp/ftxt/
    │   └── ExampleUnitTest.java      — Contoh unit test (JVM)
    │
    └── src/androidTest/java/exp/ftxt/
        └── ExampleInstrumentedTest.java  — Contoh instrumented test (Android)
```

## Statistik Project

| Kategori | Jumlah |
|----------|-------:|
| Java source | 58 |
| Layout XML | 20 |
| Drawable XML | 24 |
| Drawable PNG | 13 |
| Values XML | 7 |
| Mipmap XML | 2 |
| Menu XML | 2 |
| Anim XML | 2 |
| XML lainnya (Manifest) | 1 |
| Assets (md) | 4 |
| Root dokumen | 5 |
| Root konfigurasi | 7 |
| Gradle & wrapper | 5 |
| CI/CD | 1 |
| **Total file** | **~151** |
| **Total direktori** | **~44** |
