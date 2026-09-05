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
│   ├── gradle-wrapper.properties — Versi Gradle wrapper
│   └── gradle-wrapper.jar        — Gradle wrapper JAR
│
├── key/
│   └── audiplay-release-key.jks  — Keystore signing release
│
├── shared/
│   └── config/                    — Module library bersama (include ':shared:config')
│       ├── build.gradle           — Library module: namespace exp.ftxt.shared.config, Java 17
│       └── src/main/java/exp/ftxt/shared/config/
│           ├── BackgroundConfig.java — Model data background (enable, color, padding, offset, margin, radius)
│           └── ShadowConfig.java     — Model data shadow (enable, color, blur, offset)
│
└── app/
    ├── build.gradle              — Module: minSdk 26, compileSdk/targetSdk 35, Java 17
    ├── proguard-rules.pro        — Aturan ProGuard (custom rules)
    │
    ├── src/main/
    │   ├── AndroidManifest.xml   — Permission: overlay, notif, baterai, wake lock
    │   │
    │   ├── assets/
    │   │   ├── CHANGELOG.md      — Riwayat perubahan (in-app display, sync via Gradle)
    │   │   ├── OLD-CHANGELOG.md  — Arsip changelog versi lama (in-app display, sync via Gradle)
    │   │   ├── PANDUAN.md        — Panduan penggunaan (in-app display, sync via Gradle)
    │   │   ├── README.md         — Ringkasan project (in-app display, sync via Gradle)
    │   │   └── STRUKTUR.md       — Struktur project (in-app display, sync via Gradle)
    │   │
    │   ├── java/exp/ftxt/
    │   │   ├── MainActivity.java         — Activity utama: toolbar, nav drawer, panel system, theme toggle
    │   │   ├── SettingsActivity.java     — Manajemen izin: overlay, notifikasi, baterai
    │   │   ├── DocumentationActivity.java— Baca dokumentasi in-app dari assets (.md) via Markwon
    │   │   │
    │   │   ├── core/
    │   │   │   ├── FloatingService.java     — Foreground service: kelola semua overlay via WindowManager
    │   │   │   ├── NotificationHelper.java  — Notifikasi foreground service (custom RemoteViews + ikon dinamis Bitmap suhu, caching)
    │   │   │   ├── NotificationActionReceiver.java — Handle aksi notifikasi (toggle, kill, open)
    │   │   │   ├── WakeLockManager.java     — Partial wake lock biar CPU tetap aktif
    │   │   │   ├── BootReceiver.java        — Restore overlay & pemantau baterai aktif saat boot
    │   │   │   ├── CrashLogger.java         — Crash logger otomatis: stack trace ke Documents/FTxT/Log_Crash saat force close
    │   │   │   └── AnrWatcher.java          — Deteksi ANR: trace seluruh thread ke Documents/FTxT/Log_ANR saat main thread macet
    │   │   │
    │   │   ├── features/
    │   │   │   ├── floating_text/
    │   │   │   │   ├── TextConfig.java      — Konfigurasi statis Text overlay
    │   │   │   │   └── TextModule.java      — Buat/hapus/update Text overlay window
    │   │   │   ├── fps_display/
    │   │   │   │   ├── FpsConfig.java       — Konfigurasi statis FPS overlay
    │   │   │   │   └── FpsModule.java       — FPS counter via Choreographer frame callback
    │   │   │   ├── crosshair/
    │   │   │   │   ├── CrosshairConfig.java — Konfigurasi statis Crosshair overlay (style, size, opacity, posisi)
    │   │   │   │   └── CrosshairModule.java — Overlay bidikan ImageView 44 gaya, posisi titik tengah, drag + safe area
    │   │   │   ├── clock_module/
    │   │   │   │   ├── ClockConfig.java     — Konfigurasi statis Clock overlay
    │   │   │   │   └── ClockModule.java     — Jam real-time HH:mm:ss, update tiap 1 detik
    │   │   │   ├── battery_stats/
    │   │   │   │   ├── BatteryStatsConfig.java   — Konfigurasi Battery Stats overlay (suhu, %, voltase, arus, daya, urutan item)
    │   │   │   │   ├── BatteryStatsModule.java   — Info baterai gabungan: °C + % + V + mA + W, baca via BatteryReading
    │   │   │   │   ├── BatteryMonitor.java       — Polling & pencatatan metrik baterai ke database (sampling ±1 detik, seragam)
    │   │   │   │   ├── BatteryReading.java       — Util pembaca metrik baterai tunggal (battery intent + property + sysfs fallback)
    │   │   │   │   ├── BatteryHistoryDb.java     — Database SQLite riwayat baterai (sampel time-series + aktivitas + sesi, WAL)
    │   │   │   │   ├── BatteryCapacityEstimator.java — Estimasi kapasitas & skor kesehatan baterai (median 3 sumber, reset keseluruhan)
    │   │   │   │   ├── BatteryRingView.java      — Custom view ring gauge baterai melingkar (arc gradien hue, warna putih-hijau-merah)
    │   │   │   │   ├── DischargeTracker.java     — Pelacak sesi pengosongan aktif (reaksi langsung status colok/cabut)
    │   │   │   │   ├── SessionSegmentBuilder.java— Segmentasi sampel menjadi sesi pengisian & pengosongan
    │   │   │   │   └── SessionRebuild.java       — Rekonstruksi sesi tak tersimpan di thread latar
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
    │   │   │   │   ├── PresetManager.java     — CRUD preset: save/load/rename/reorder/search/export/import (async)
    │   │   │   │   ├── PresetHandler.java     — Delegate pattern: save/load dialog infrastructure per modul
    │   │   │   │   ├── PresetBrowserDialog.java   — DialogFragment browser preset dengan search & filter
    │   │   │   │   └── PresetExampleActivity.java — Contoh integrasi sistem preset (referensi dev)
    │   │   │   └── ui/
    │   │   │       ├── ShadowTextView.java        — Custom TextView dengan shadow + background di onDraw()
    │   │   │       ├── ShadowImageView.java       — ImageView dengan shadow (dipakai modul Crosshair)
    │   │   │       ├── BatteryChartView.java      — Custom View line chart Canvas riwayat metrik baterai (warna segmen kondisi, LUT suhu)
    │   │   │       ├── BatterySessionBarChartView.java — Custom View grafik batang riwayat sesi (segmen hijau terisi / oranye terpakai)
    │   │   │       ├── ActivityBarView.java       — Custom View bar aktivitas Layar Aktif/Mati/Mengisi di halaman detail grafik
    │   │   │       ├── InfoTooltip.java           — Tooltip melayang ℹ️ kustom (fade, menutup saat ketuk luar)
    │   │   │       ├── OverlayDragHandler.java    — Touch listener untuk drag overlay
    │   │   │       ├── OverlayModule.java         — Interface untuk menyeragamkan method semua modul overlay
    │   │   │       ├── OverlayShadow.java         — Apply elevation-based shadow ke overlay
    │   │   │       ├── ColorPickerDialog.java     — Dialog color picker: wheel, sliders, HEX/ARGB
    │   │   │       ├── DpadController.java        — Kontrol D-Pad dengan repeat untuk fine position (interval pixel 1–20)
    │   │   │       ├── SliderPositionController.java  — Slider X/Y posisi normalized 0-1000
    │   │   │       ├── SliderLabelEditor.java     — Dialog edit nilai numerik dari label slider
    │   │   │       ├── SectionHelper.java         — Utility collapsible section toggle ▸/▾
    │   │   │       └── PresetPreviewView.java     — Custom View preview posisi overlay di grid
    │   │   │
    │   │   ├── ui/
    │   │   │   ├── BasePanelFragment.java              — Abstract base Fragment untuk semua panel
    │   │   │   ├── PanelManager.java                   — Kelola show/hide Fragment panel + onPanelHidden callback
    │   │   │   ├── TextPanelController.java            — UI panel Text: input, size, color, shadow, bg, toggle
    │   │   │   ├── TextPositionController.java         — Kontrol posisi Text: D-Pad, slider, preset, koordinat
    │   │   │   ├── FpsPanelController.java             — UI panel FPS: switch, size, color, shadow, bg, value-only
    │   │   │   ├── FpsPositionController.java          — Kontrol posisi FPS dengan preset
    │   │   │   ├── ClockPanelController.java           — UI panel Clock: switch, size, color, shadow, bg, ukuran tanggal
    │   │   │   ├── ClockPositionController.java        — Kontrol posisi Clock dengan preset
    │   │   │   ├── BatteryPanelController.java         — UI panel Battery Info (tabbed: Monitor/Overlay/Battery Strip)
    │   │   │   ├── BatteryPositionController.java      — Kontrol posisi Battery Info + preset
    │   │   │   ├── BatteryBarPanelController.java      — UI panel Battery Strip: quick/manual mode, warna, animasi
    │   │   │   ├── BatteryBarPositionController.java   — Kontrol posisi Battery Strip + preset
    │   │   │   ├── BatteryMonitorTabController.java    — Controller tab Monitor: 3 sub-tab (Info & Grafik, Sesi Berjalan, Kondisi & Riwayat)
    │   │   │   ├── BatteryChartHistoryController.java  — Controller kartu Grafik Riwayat (grid 2×2, 5 grafik, pemilih rentang 5m–48j)
    │   │   │   ├── BatteryHealthCardController.java    — Controller kartu Kesehatan Baterai (estimasi, reset, salin/ekspor snapshot)
    │   │   │   ├── BatterySessionLiveController.java   — Controller panel Sesi Berjalan real-time (refresh 1 detik, view statis)
    │   │   │   ├── BatterySessionHistoryController.java— Controller kartu Riwayat Sesi periode (grafik batang, daftar, filter)
    │   │   │   ├── BatterySnapshotExporter.java        — Salin/ekspor snapshot tab Monitor ke Download
    │   │   │   ├── BatteryOrderZonesView.java          — Custom view zona drag chip untuk urutan info baterai
    │   │   │   ├── BatteryChartDetailActivity.java     — Halaman detail grafik fullscreen (zoom & pan, crosshair, statistik)
    │   │   │   ├── SessionListActivity.java            — Halaman daftar sesi periode (hari/minggu/bulan) dengan filter
    │   │   │   ├── SessionDetailActivity.java          — Halaman detail satu sesi (struktur sama dengan panel Sesi Berjalan)
    │   │   │   ├── MemoryPanelController.java          — UI panel Memory Stats (tab Monitor + Overlay): OrderZones, export/copy
    │   │   │   ├── MemoryPositionController.java       — Kontrol posisi Memory Stats + preset
    │   │   │   ├── MemoryOrderZonesView.java           — Custom view zona drag chip dua zona untuk urutan item Memory Stats
    │   │   │   ├── NetworkPanelController.java         — UI panel Network: switch, size, color, shadow
    │   │   │   ├── NetworkPositionController.java      — Kontrol posisi Network
    │   │   │   ├── ColorPickerPanelController.java     — UI panel Color Picker: wheel, sliders, saved colors
    │   │   │   ├── CrosshairPanelController.java       — UI panel Crosshair: galeri 44 gaya, size/opacity/rotasi, lock/safe area
    │   │   │   ├── CrosshairPositionController.java    — Kontrol posisi Crosshair: slider X/Y + D-Pad + koordinat
    │   │   │   ├── fragment/
    │   │   │   │   ├── TextPanelFragment.java              — Fragment Floating Text
    │   │   │   │   ├── FpsPanelFragment.java               — Fragment FPS Display
    │   │   │   │   ├── ClockPanelFragment.java             — Fragment Jam Digital
    │   │   │   │   ├── BatteryPanelFragment.java           — Fragment Battery Info (tabbed: Monitor/Overlay/Battery Strip)
    │   │   │   │   ├── MemoryPanelFragment.java            — Fragment Memory Stats (tabbed: Monitor/Overlay)
    │   │   │   │   ├── NetworkPanelFragment.java           — Fragment Network Speed
    │   │   │   │   ├── ColorPickerPanelFragment.java       — Fragment Color Picker
    │   │   │   │   ├── CrosshairPanelFragment.java         — Fragment Crosshair
    │   │   │   │   ├── LogoPanelFragment.java              — Fragment Logo Display (placeholder)
    │   │   │   │   └── DebugingPanelFragment.java          — Fragment panel Debugging (preview ikon rotasi)
    │   │   │
    │   │   ├── utils/
    │   │   │   └── PermissionHelper.java    — Helper izin: overlay, notifikasi, optimasi baterai
    │   │
    │   └── res/
    │       ├── anim/
    │       │   ├── settings_popup_enter.xml — Animasi masuk popup settings
    │       │   ├── settings_popup_exit.xml  — Animasi keluar popup settings
    │       │   ├── tooltip_fade_in.xml      — Animasi fade masuk tooltip ℹ️
    │       │   └── tooltip_fade_out.xml     — Animasi fade keluar tooltip ℹ️
    │       ├── color/
    │       │   ├── sidebar_nav_text.xml     — Color selector teks item navigasi sidebar
    │       │   ├── bat_nav_item_color.xml   — Color selector navigasi bawah Battery Info
    │       │   └── mem_nav_item_color.xml   — Color selector navigasi bawah Memory Stats
    │       ├── drawable/
    │       │   ├── ic_arrow_down.xml        — Ikon panah bawah untuk D-Pad
    │       │   ├── ic_arrow_left.xml        — Ikon panah kiri untuk D-Pad
    │       │   ├── ic_arrow_right.xml       — Ikon panah kanan untuk D-Pad
    │       │   ├── ic_arrow_up.xml          — Ikon panah atas untuk D-Pad
    │       │   ├── ic_bolt.xml              — Ikon petir status pengisian
    │       │   ├── ic_build.xml             — Ikon modul Debugging/Developer
    │       │   ├── ic_battery_full.xml      — Ikon modul Battery Info
    │       │   ├── ic_battery_strip.xml     — Ikon tab Battery Strip
    │       │   ├── ic_close.xml             — Ikon close/X untuk Kill Service
    │       │   ├── ic_crosshair.xml         — Ikon modul Crosshair
    │       │   ├── ic_dots_vertical.xml     — Ikon tiga titik vertikal (overflow menu)
    │       │   ├── ic_drag_handle.xml       — Ikon grip (garis hamburger) untuk drag item sidebar
    │       │   ├── ic_edit.xml              — Ikon pensil untuk edit HEX/nilai
    │       │   ├── ic_exit.xml              — Ikon exit/keluar untuk tombol Keluar
    │       │   ├── ic_image.xml             — Ikon modul Logo Display
    │       │   ├── ic_info.xml              — Ikon info ℹ️ untuk tooltip label kartu
    │       │   ├── ic_memory.xml            — Ikon modul Info Memori
    │       │   ├── ic_minus.xml             — Ikon minus (−) kontrol zoom dokumen
    │       │   ├── ic_monitor.xml           — Ikon tab Monitor
    │       │   ├── ic_notification_invisible.xml — Ikon mata tertutup untuk toggle hide
    │       │   ├── ic_notification_open.xml — Ikon buka aplikasi untuk notifikasi
    │       │   ├── ic_notification_stop.xml — Ikon kill service untuk notifikasi
    │       │   ├── ic_notification_toggle.xml — Ikon toggle untuk notifikasi
    │       │   ├── ic_notification_toggle_off.xml — Ikon toggle off untuk notifikasi
    │       │   ├── ic_notification_visible.xml — Ikon mata terbuka untuk toggle show
    │       │   ├── ic_overlay.xml           — Ikon tab Overlay
    │       │   ├── ic_palette.xml           — Ikon modul Color Picker
    │       │   ├── ic_plus.xml              — Ikon plus (+) kontrol zoom dokumen
    │       │   ├── ic_reset.xml             — Ikon reset (hapus data estimasi/kesehatan)
    │       │   ├── ic_schedule.xml          — Ikon riwayat/sesi monitor
    │       │   ├── ic_screen_rotation.xml   — Ikon orientasi layar (toolbar)
    │       │   ├── ic_settings.xml          — Ikon gear untuk settings
    │       │   ├── ic_signal_wifi.xml       — Ikon modul Network Speed
    │       │   ├── ic_star_filled.xml       — Ikon bintang solid (favorit)
    │       │   ├── ic_star_outline.xml      — Ikon bintang outline (non-favorit)
    │       │   ├── ic_subtab_history.xml    — Ikon sub-tab Kondisi & Riwayat Monitor
    │       │   ├── ic_subtab_info.xml       — Ikon sub-tab Info & Grafik Monitor
    │       │   ├── ic_subtab_session.xml    — Ikon sub-tab Sesi Berjalan Monitor
    │       │   ├── ic_sun.xml               — Ikon matahari untuk tema terang
    │       │   ├── ic_swap.xml              — Ikon swap untuk tukar mode color picker
    │       │   ├── ic_text_format.xml       — Ikon modul Floating Text
    │       │   ├── ic_theme.xml             — Ikon tema gelap/terang
    │       │   ├── ic_launcher_background.xml   — Background launcher adaptive icon
    │       │   ├── ic_launcher_foreground.png   — Foreground launcher adaptive icon
    │       │   ├── ic_launcher_bg.png       — Background ikon aplikasi
    │       │   ├── ic_launcher_foreground_alt.png — Foreground ikon alternatif
    │       │   ├── bat_badge_active_bg.xml  — Badge status aktif sesi baterai
    │       │   ├── bat_badge_stopped_bg.xml — Badge status berhenti sesi baterai
    │       │   ├── bat_card_bg.xml          — Background card tab Monitor Battery Info
    │       │   ├── bg_style_item.xml        — Sel grid gaya bidikan (default)
    │       │   ├── bg_style_item_selected.xml — Sel grid gaya bidikan (terpilih, stroke aksen)
    │       │   ├── divider_horizontal.xml   — Divider horizontal untuk daftar dokumen
    │       │   ├── drawer_bg.xml            — Drawable wrapper drawer bg terang
    │       │   ├── drawer_header_bg.xml     — Drawable wrapper header drawer terang
    │       │   ├── main_bg.xml              — Drawable wrapper main bg terang
    │       │   ├── mem_badge_active_bg.xml  — Background badge proses aktif (panel Memory)
    │       │   ├── mem_badge_stopped_bg.xml — Background badge proses stopped (panel Memory)
    │       │   ├── mem_card_bg.xml          — Background card panel Memory
    │       │   ├── seekbar_thumb.xml        — Thumb slider lingkaran 12×12dp
    │       │   ├── sidebar_item_bg.xml      — Background item sidebar (default)
    │       │   ├── sidebar_item_selected_bg.xml — Background item sidebar terpilih (latar membulat aksen)
    │       │   ├── splash_screen.xml        — Splash screen drawable
    │       │   ├── toolbar_bg.xml           — Drawable wrapper toolbar bg terang
    │       │   ├── tooltip_bg.xml           — Background rounded tooltip ℹ️
    │       │   └── vertical_divider.xml     — Divider vertikal untuk bottom bar preset
    │       ├── drawable-night/
    │       │   ├── drawer_bg.xml            — Drawable wrapper drawer bg gelap (flip 180°)
    │       │   ├── drawer_header_bg.xml     — Drawable wrapper header drawer gelap
    │       │   ├── toolbar_bg.xml           — Drawable wrapper toolbar bg gelap
    │       │   ├── main_bg.xml              — Drawable wrapper main bg gelap
    │       │   └── sidebar_item_selected_bg.xml — Background item sidebar terpilih (mode gelap)
    │       ├── drawable-nodpi/
    │       │   └── crosshair_1..44.png      — 44 PNG gaya bidikan modul Crosshair
    │       ├── layout/
    │       │   ├── activity_main.xml            — Layout utama dengan DrawerLayout + CoordinatorLayout
    │       │   ├── activity_settings.xml        — Layout halaman pengaturan izin
    │       │   ├── activity_documentation.xml   — Layout halaman dokumentasi in-app
    │       │   ├── activity_battery_chart_detail.xml — Layout halaman detail grafik fullscreen
    │       │   ├── activity_session_list.xml    — Layout halaman daftar sesi periode
    │       │   ├── activity_session_detail.xml  — Layout halaman detail satu sesi
    │       │   ├── item_session_row.xml         — Item baris padat daftar sesi
    │       │   ├── dialog_color_picker.xml      — Dialog color picker gabungan wheel + sliders
    │       │   ├── dialog_preset_browser.xml    — Dialog browser preset dengan search & list
    │       │   ├── drawer_content.xml           — Konten navigation drawer: RecyclerView item list
    │       │   ├── nav_header.xml               — Header navigation drawer: logo + versi
    │       │   ├── notification_custom.xml      — Layout RemoteViews notifikasi foreground custom
    │       │   ├── panel_text.xml               — Panel konfigurasi Floating Text
    │       │   ├── panel_fps.xml                — Panel konfigurasi FPS Display
    │       │   ├── panel_clock.xml              — Panel konfigurasi Jam Digital
    │       │   ├── panel_battery.xml            — Panel konfigurasi Battery Info (tabbed: Monitor/Overlay/Battery Strip)
    │       │   ├── panel_bat_sub_info.xml       — Sub-tab Info & Grafik Monitor
    │       │   ├── panel_bat_sub_live.xml       — Sub-tab Sesi Berjalan Monitor
    │       │   ├── panel_bat_sub_health.xml     — Sub-tab Kondisi & Riwayat Monitor
    │       │   ├── panel_memory.xml             — Panel konfigurasi Memory Stats (tabbed: Monitor/Overlay)
    │       │   ├── panel_network.xml            — Panel konfigurasi Network Speed
    │       │   ├── panel_crosshair.xml          — Panel konfigurasi Crosshair (gaya bidikan, ukuran, opasitas, posisi)
    │       │   ├── panel_logo.xml               — Placeholder Logo Display (coming soon)
    │       │   ├── panel_debuging.xml           — Panel konfigurasi Debugging (preview ikon rotasi)
    │       │   ├── panel_color_picker.xml       — Panel konfigurasi Color Picker
    │       │   ├── preset_browser_item.xml      — Item layout untuk daftar preset
    │       │   ├── preset_list_item.xml         — Item layout alternate untuk preset
    │       │   ├── tooltip_info.xml             — Layout tooltip melayang ℹ️
    │       │   └── toolbar_zoom.xml             — Kontrol zoom − [+] di toolbar dokumentasi
    │       ├── menu/
    │       │   ├── drawer_menu.xml           — Menu navigation drawer: daftar panel
    │       │   ├── main_menu.xml             — Menu toolbar: settings, theme, exit
    │       │   ├── menu_battery_bottom_nav.xml — Navigasi bawah panel Battery Info (Monitor/Overlay/Battery Strip)
    │       │   └── menu_memory_bottom_nav.xml  — Navigasi bawah panel Memory Stats (Monitor/Overlay)
    │       ├── mipmap-anydpi-v26/
    │       │   ├── ic_launcher.xml           — Adaptive icon launcher (default)
    │       │   └── ic_launcher_alt.xml       — Adaptive icon launcher (alternatif)
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
    ├── src/test/java/
    │   ├── exp/ftxt/ExampleUnitTest.java      — Contoh unit test (JVM)
    │   └── exp/ftxy/ExampleUnitTest.java      — Contoh unit test (paket sisa, tidak dipakai)
    │
    └── src/androidTest/java/
        ├── exp/ftxt/ExampleInstrumentedTest.java  — Contoh instrumented test (Android)
        └── exp/ftxy/ExampleInstrumentedTest.java  — Contoh instrumented test (paket sisa, tidak dipakai)
```

## Statistik Project

| Kategori | Jumlah |
|----------|-------:|
| Java source | 104 |
| Layout XML | 29 |
| Drawable XML | 62 |
| Drawable PNG | 3 |
| Crosshair PNG (nodpi) | 44 |
| Drawable-night XML | 5 |
| Color XML | 3 |
| Values XML | 7 |
| Mipmap XML | 2 |
| Menu XML | 4 |
| Anim XML | 4 |
| XML lainnya (Manifest) | 1 |
| Assets (md) | 5 |
| Test Java | 4 |
| Root dokumen | 5 |
| Root konfigurasi | 6 |
| Module build script | 2 |
| Gradle & wrapper | 4 |
| CI/CD | 1 |
| **Total file** | **~321** |
| **Total direktori** | **~48** |