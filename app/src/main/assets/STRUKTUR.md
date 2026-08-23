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
    │   │   │   ├── NotificationHelper.java  — Notifikasi foreground service (custom RemoteViews + ikon dinamis Bitmap suhu, caching)
    │   │   │   ├── NotificationActionReceiver.java — Handle aksi notifikasi (toggle, kill, open)
    │   │   │   ├── WakeLockManager.java     — Partial wake lock biar CPU tetap aktif
    │   │   │   ├── BootReceiver.java        — Restore overlay aktif saat boot
    │   │   │   ├── BatteryMonitorService.java — Foreground service ringan monitor baterai full-aktif (notifikasi minimal prioritas rendah, auto-start app & boot)
    │   │   │   └── CrashLogger.java         — Crash logger otomatis: stack trace ke folder Download saat force close
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
    │   │   │   ├── battery_stats/
    │   │   │   │   ├── BatteryStatsConfig.java   — Konfigurasi Battery Stats overlay (suhu, %, voltase, arus, daya, urutan item)
    │   │   │   │   ├── BatteryStatsModule.java   — Info baterai gabungan: °C + % + V + mA + W, baca via BatteryReading
    │   │   │   │   ├── BatteryMonitor.java       — Polling & pencatatan metrik baterai ke database (sampling dinamis: charging/layar nyala/layar mati)
    │   │   │   │   ├── BatteryReading.java       — Util pembaca metrik baterai tunggal (battery intent + property + sysfs fallback) untuk overlay & tab Monitor
    │   │   │   │   ├── BatteryHistoryDb.java     — Database SQLite riwayat baterai (sampel time-series + sesi pengisian, query per rentang dengan resample seragam)
    │   │   │   │   ├── BatteryCapacityEstimator.java — Estimasi kapasitas & skor kesehatan baterai dari segmen pengisian daya (agregasi median lintas sesi)
    │   │   │   │   └── BatteryRingView.java      — Custom view ring gauge baterai melingkar (arc gradien hue, teks level/kapasitas/status di dalam lingkaran)
    │   │   │   ├── battery_bar/
    │   │   │   │   ├── BatteryBarConfig.java         — Konfigurasi Battery Bar overlay
    │   │   │   │   ├── BatteryBarView.java           — Custom View bar baterai H/V (empty strip, fade, shine, wave)
    │   │   │   │   └── BatteryBarModule.java         — Bar baterai: mode cepat (snap sisi) & manual, update interval
    │   │   │   ├── memory_stats/
    │   │   │   │   ├── MemoryConfig.java      — Konfigurasi modul Memory Stats
    │   │   │   │   ├── MemoryModule.java      — Modul overlay Memory Stats (refreshDisplay, buildItemPart, readSysfs)
    │   │   │   │   └── MemoryMonitor.java     — Background monitor polling memori
    │   │   │   ├── network_stats/
    │   │   │   │   ├── NetworkConfig.java  — Konfigurasi statis Network overlay
    │   │   │   │   └── NetworkModule.java  — Kecepatan internet ↓↑ via TrafficStats, 1 detik
    │   │   │   └── color_picker/
    │   │   │       └── TriangleColorPickerView.java — Custom View segitiga HSV untuk Color Picker
    │   │   │
    │   │   ├── shared/
    │   │   │   ├── color/
    │   │   │   │   ├── ColorMath.java         — Operasi matematika HSV: gradient, angle, selector posisi
    │   │   │   │   ├── ColorNameResolver.java — Deteksi nama warna dari RGB
    │   │   │   │   ├── BatteryColors.java     — Helper warna bersama: rumus gradien hue baterai untuk Battery Strip & ring gauge
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
    │   │   │       ├── BatteryChartView.java      — Custom View line chart Canvas riwayat metrik baterai (sumbu Y otomatis, label waktu adaptif)
    │   │   │       ├── OverlayDragHandler.java    — Touch listener untuk drag overlay
    │   │   │       ├── OverlayModule.java         — Interface untuk menyeragamkan method semua modul overlay
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
    │   │   │   ├── BatteryPanelController.java         — UI panel Battery Info (tab Overlay): OrderZones, toggle °C/%/V/mA/W, warna
    │   │   │   ├── BatteryPositionController.java      — Kontrol posisi Battery Info + preset
    │   │   │   ├── BatteryBarPanelController.java      — UI panel Battery Strip (tab Battery Strip): quick/manual mode, warna, animasi
    │   │   │   ├── BatteryBarPositionController.java   — Kontrol posisi Battery Strip + preset
    │   │   │   ├── BatteryOrderZonesView.java          — Custom view zona drag chip untuk urutan info baterai
    │   │   │   ├── MemoryOrderZonesView.java           — Custom view zona drag chip dua zona untuk urutan item Memory Stats
    │   │   │   ├── MemoryPanelController.java          — UI panel Memory Stats (tab Monitor + Overlay): OrderZones, export/copy
    │   │   │   ├── MemoryPositionController.java       — Kontrol posisi Memory Stats + preset
    │   │   │   ├── NetworkPanelController.java         — UI panel Network: switch, size, color, shadow
    │   │   │   ├── NetworkPositionController.java      — Kontrol posisi Network
    │   │   │   ├── ColorPickerPanelController.java     — UI panel Color Picker: wheel, sliders, saved colors
    │   │   │   ├── BasePanelFragment.java              — Abstract base Fragment untuk semua panel
    │   │   │   ├── PanelManager.java                   — Kelola show/hide Fragment panel + onPanelHidden callback
    │   │   │   ├── fragment/
    │   │   │   │   ├── TextPanelFragment.java              — Fragment Floating Text
    │   │   │   │   ├── FpsPanelFragment.java               — Fragment FPS Display
    │   │   │   │   ├── ClockPanelFragment.java             — Fragment Jam Digital
    │   │   │   │   ├── BatteryPanelFragment.java           — Fragment Battery Info (tabbed: Monitor/Overlay/Battery Strip)
    │   │   │   │   ├── MemoryPanelFragment.java            — Fragment Memory Stats (tabbed: Monitor/Overlay)
    │   │   │   │   ├── NetworkPanelFragment.java           — Fragment Network Speed
    │   │   │   │   ├── ColorPickerPanelFragment.java       — Fragment Color Picker
    │   │   │   │   ├── CrosshairPanelFragment.java         — Fragment Crosshair (placeholder)
    │   │   │   │   ├── LogoPanelFragment.java              — Fragment Logo Display (placeholder)
    │   │   │   │   └── DebugingPanelFragment.java          — Fragment panel Debugging (preview ikon rotasi)
    │   │   │   │
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
    │       │   ├── ic_battery_strip.xml     — Ikon tab Battery Strip
    │       │   ├── ic_close.xml             — Ikon close/X untuk Kill Service
    │       │   ├── ic_dots_vertical.xml     — Ikon tiga titik vertikal (overflow menu)
    │       │   ├── ic_edit.xml              — Ikon pensil untuk edit HEX/nilai
    │       │   ├── ic_exit.xml              — Ikon exit/keluar untuk tombol Keluar
    │       │   ├── ic_launcher_background.xml   — Background launcher adaptive icon
    │       │   ├── ic_launcher_foreground.png   — Foreground launcher adaptive icon
    │       │   ├── ic_launcher_bg.png       — Background ikon aplikasi
    │       │   ├── ic_launcher_foreground_alt.png — Foreground ikon alternatif
    │       │   ├── ic_monitor.xml           — Ikon tab Monitor
    │       │   ├── ic_notification_invisible.xml — Ikon mata tertutup untuk toggle hide
    │       │   ├── ic_notification_open.xml — Ikon buka aplikasi untuk notifikasi
    │       │   ├── ic_notification_stop.xml — Ikon kill service untuk notifikasi
    │       │   ├── ic_notification_toggle.xml — Ikon toggle untuk notifikasi
    │       │   ├── ic_notification_toggle_off.xml — Ikon toggle off untuk notifikasi
    │       │   ├── ic_notification_visible.xml — Ikon mata terbuka untuk toggle show
    │       │   ├── ic_overlay.xml           — Ikon tab Overlay
    │       │   ├── ic_rotation_variant_1.xml — Ikon varian rotasi 1 (panel Debugging)
    │       │   ├── ic_rotation_variant_2.xml — Ikon varian rotasi 2
    │       │   ├── ic_rotation_variant_3.xml — Ikon varian rotasi 3
    │       │   ├── ic_rotation_variant_4.xml — Ikon varian rotasi 4
    │       │   ├── ic_rotation_variant_5.xml — Ikon varian rotasi 5
    │       │   ├── ic_screen_rotation.xml   — Ikon orientasi layar (toolbar)
    │       │   ├── ic_settings.xml          — Ikon gear untuk settings
    │       │   ├── ic_star_filled.xml       — Ikon bintang solid (favorit)
    │       │   ├── ic_star_outline.xml      — Ikon bintang outline (non-favorit)
    │       │   ├── ic_sun.xml               — Ikon matahari untuk tema terang
    │       │   ├── ic_swap.xml              — Ikon swap untuk tukar mode color picker
    │       │   ├── ic_theme.xml             — Ikon tema gelap/terang
    │       │   ├── mem_badge_active_bg.xml  — Background badge proses aktif (panel Memory)
    │       │   ├── mem_badge_stopped_bg.xml — Background badge proses stopped (panel Memory)
    │       │   ├── mem_card_bg.xml          — Background card panel Memory
    │       │   ├── seekbar_thumb.xml        — Thumb slider lingkaran 12×12dp
    │       │   ├── splash_screen.xml        — Splash screen drawable
    │       │   ├── bat_card_bg.xml          — Background card tab Monitor Battery Info
    │       │   ├── drawer_bg.xml            — Drawable wrapper drawer bg terang
    │       │   ├── drawer_header_bg.xml     — Drawable wrapper header drawer terang
    │       │   ├── toolbar_bg.xml           — Drawable wrapper toolbar bg terang
    │       │   ├── vertical_divider.xml     — Divider vertikal untuk bottom bar preset
    │       │   ├── divider_horizontal.xml   — Divider horizontal untuk daftar dokumen
    │       │   └── main_bg.xml              — Drawable wrapper main bg terang
    │       ├── drawable-night/
    │       │   ├── drawer_bg.xml            — Drawable wrapper drawer bg gelap (flip 180°)
    │       │   ├── drawer_header_bg.xml     — Drawable wrapper header drawer gelap
    │       │   ├── toolbar_bg.xml           — Drawable wrapper toolbar bg gelap
    │       │   └── main_bg.xml              — Drawable wrapper main bg gelap
    │       ├── layout/
    │       │   ├── activity_main.xml            — Layout utama dengan DrawerLayout + CoordinatorLayout
    │       │   ├── activity_settings.xml        — Layout halaman pengaturan izin
    │       │   ├── activity_documentation.xml   — Layout halaman dokumentasi in-app
    │       │   ├── dialog_color_picker.xml      — Dialog color picker gabungan wheel + sliders
    │       │   ├── dialog_preset_browser.xml    — Dialog browser preset dengan search & list
    │       │   ├── drawer_content.xml           — Konten navigation drawer: RecyclerView item list
    │       │   ├── notification_custom.xml      — Custom notification layout (RemoteViews + ImageButton)
    │   │   ├── nav_header.xml               — Header navigation drawer: logo + versi
    │   │   ├── panel_text.xml               — Panel konfigurasi Floating Text
    │   │   ├── panel_fps.xml                — Panel konfigurasi FPS Display
    │   │   ├── panel_clock.xml              — Panel konfigurasi Jam Digital
    │   │   ├── panel_battery.xml            — Panel konfigurasi Battery Info (tabbed: Monitor/Overlay/Battery Strip)
    │   │   ├── panel_memory.xml             — Panel konfigurasi Memory Stats (tabbed: Monitor/Overlay)
    │   │   ├── panel_network.xml            — Panel konfigurasi Network Speed
    │   │   ├── panel_crosshair.xml          — Placeholder Crosshair (coming soon)
    │   │   ├── panel_logo.xml               — Placeholder Logo Display (coming soon)
    │   │   ├── panel_debuging.xml           — Panel konfigurasi Debugging (preview ikon rotasi)
    │   │   ├── panel_color_picker.xml       — Panel konfigurasi Color Picker
    │   │   ├── preset_browser_item.xml      — Item layout untuk daftar preset
    │   │   ├── preset_list_item.xml         — Item layout alternate untuk preset
    │   │   └── toolbar_zoom.xml             — Kontrol zoom − [+] di toolbar dokumentasi
    │       ├── menu/
    │       │   ├── drawer_menu.xml           — Menu navigation drawer: daftar panel
    │       │   ├── main_menu.xml             — Menu toolbar: settings, theme, exit
    │       │   ├── menu_battery_bottom_nav.xml — Navigasi bawah panel Battery Info (Monitor/Overlay/Battery Strip)
    │       │   └── menu_memory_bottom_nav.xml  — Navigasi bawah panel Memory Stats (Monitor/Overlay)
    │       ├── mipmap-anydpi-v26/
    │       │   ├── ic_launcher.xml           — Adaptive icon launcher (default)
    │       │   └── ic_launcher_alt.xml       — Adaptive icon launcher (alternatif)
    │       ├── color/
    │       │   ├── bat_nav_item_color.xml    — Color selector navigasi bawah Battery Info
    │       │   └── mem_nav_item_color.xml    — Color selector navigasi bawah Memory Stats
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
| Java source | 84 |
| Layout XML | 21 |
| Drawable XML | 47 |
| Drawable PNG | 3 |
| Color XML | 2 |
| Values XML | 7 |
| Mipmap XML | 2 |
| Menu XML | 4 |
| Anim XML | 2 |
| XML lainnya (Manifest) | 1 |
| Assets (md) | 4 |
| Root dokumen | 5 |
| Root konfigurasi | 5 |
| Gradle & wrapper | 4 |
| CI/CD | 1 |
| **Total file** | **~261** |
| **Total direktori** | **~59** |
