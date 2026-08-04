# [4.84.1] - 2026-08-04
### 🐞 Bug Fixes
- **Layar panel kosong saat start** — Saat nilai `nav_selected_item` tidak dikenal, aplikasi tidak menampilkan panel apa pun. Sekarang ada fallback: jika panel tidak ditemukan di `panelIdToName()`, default ke panel **Floating Text** + `R.id.navFloatingText`.
- **Panel Crosshair & Logo tidak bisa diakses** — `navCrosshair` dan `navLogo` tidak terdaftar di `panelIdToName()` sehingga klik sidebar tidak menampilkan panel apa pun. Sekarang keduanya dipetakan ke panel `crosshair` / `logo` + judul toolbar yang benar.
- **Race condition PanelManager.showPanel()** — Transaksi fragment berjalan async sehingga klik cepat antar panel bisa menumpuk transaksi (panel salah tampil). Sekarang `executePendingTransactions()` dipanggil sebelum iterasi, `setReorderingAllowed(true)`, dan hanya fragment yang `isAdded()` & tidak `isHidden()` yang di-hide.
- **"Muat Preset" dari menu gear tidak muncul** — Fragment-fragment panel tidak meng-override `showLoadPresetDialog()` sehingga dialog preset tidak pernah ditampilkan. Semua fragment panel kini meneruskan panggilan ke controller masing-masing.
- **UI panel tidak di-refresh** — `onPanelShown()` tidak dipanggil setelah panel ditampilkan sehingga kontrol tidak sinkron saat pindah panel. PanelManager kini memanggil `onPanelShown()` via `runOnCommit` setiap panel ditampilkan, dan semua fragment meneruskan panggilan ke controller.
- **Crash `BadForegroundServiceNotificationException` — `ImageButton doesn't have method: setText`** — `setTextViewText(R.id.noti_title, ...)` dihapus dari `buildNotificationDynamic()`. Teks suhu baterai tetap tampil via `setContentTitle()` di header notifikasi (`DecoratedCustomViewStyle`), jadi tidak ada lagi perintah `setText` pada RemoteViews yang berisiko menarget view tipe salah.
- **Panel kosong saat memilih modul di sidebar** — Semua layout panel (kecuali `panel_text.xml`) masih membawa `android:visibility="gone"` di root (sisa era View-visibility manual). Setelah refactor ke fragment, tidak ada lagi `setVisibility(VISIBLE)` manual sehingga panel yang dipilih tampil kosong. Sekarang atribut `gone` di root dihapus dari 9 layout panel; visibilitas dikelola sepenuhnya oleh FragmentTransaction.
- **Posisi overlay reset ke default setelah Kill Service / Tutup Aplikasi** — `System.exit(0)` di `NotificationActionReceiver.handleKillService()` dan `MainActivity.forceClose()` membunuh proses secara paksa sebelum `SharedPreferences.apply()` (async) selesai di-flush ke disk, sehingga posisi yang baru disesuaikan hilang dan overlay kembali ke default (0.5/0.8) saat service start ulang. `System.exit(0)` dihapus dari kedua lokasi — `stopService`/`finishAffinity` sudah cukup; data prefs aman dan posisi dipertahankan.
### 🔧 Optimasi & Penyesuaian
- **Hapus dead code konstruktor lama `(Activity)`** — Semua PositionController & PanelController punya konstruktor lama yang sudah tidak dipakai setelah refactor ke fragment. Konstruktor lama berpotensi NPE karena `findViewById(android.R.id.content)` mengembalikan `null`. 15 konstruktor + 13 method `bindViews()` tanpa parameter dihapus dari 15 file controller.
- **Hapus import `CheckBox` ganda** — Import duplikat dihapus dari `FpsPanelController.java` dan `BatteryPercentagePanelController.java`.
### 🗒️ File Added
- `app/src/main/java/exp/ftxt/ui/fragment/CrosshairPanelFragment.java` — Fragment untuk panel Crosshair (placeholder)
- `app/src/main/java/exp/ftxt/ui/fragment/LogoPanelFragment.java` — Fragment untuk panel Logo (placeholder)
- `app/src/debug/res/mipmap-anydpi-v26/ic_launcher.xml` — Ikon launcher alternatif untuk build debug
### ✏️ File Changed
- `app/build.gradle` — versionCode 176, versionName 4.84.1; tambah `applicationIdSuffix ".debug"` + `resValue "app_name"` di build type `debug`
- `app/src/main/java/exp/ftxt/MainActivity.java` — `panelIdToName()` tambah `navCrosshair`→`crosshair`, `navLogo`→`logo`; fallback default ke panel text saat nilai tersimpan tidak dikenal; `updateActionBarTitle()` tambah judul untuk crosshair & logo; hapus `System.exit(0)` di `forceClose()` (mencegah data prefs hilang sebelum `.apply()` ter-flush)
- `app/src/main/java/exp/ftxt/core/NotificationActionReceiver.java` — Hapus `System.exit(0)` di `handleKillService()` (mencegah data prefs hilang sebelum `.apply()` ter-flush)
- `app/src/main/java/exp/ftxt/ui/PanelManager.java` — Guard null & validasi `panelMap` di `showPanel()`, `executePendingTransactions()` sebelum loop, `setReorderingAllowed(true)`, hide hanya fragment yang sedang tampil, `runOnCommit` untuk memanggil `onPanelShown()` setelah panel tampil
- `app/src/main/java/exp/ftxt/ui/fragment/TextPanelFragment.java` — Override `onPanelShown()` & `showLoadPresetDialog()` → delegasi ke controller
- `app/src/main/java/exp/ftxt/ui/fragment/FpsPanelFragment.java` — Override `onPanelShown()` & `showLoadPresetDialog()`
- `app/src/main/java/exp/ftxt/ui/fragment/ClockPanelFragment.java` — Override `onPanelShown()` & `showLoadPresetDialog()`
- `app/src/main/java/exp/ftxt/ui/fragment/BatteryPanelFragment.java` — Override `onPanelShown()` & `showLoadPresetDialog()`
- `app/src/main/java/exp/ftxt/ui/fragment/BatteryPercentagePanelFragment.java` — Override `onPanelShown()` & `showLoadPresetDialog()`
- `app/src/main/java/exp/ftxt/ui/fragment/BatteryCurrentPanelFragment.java` — Override `onPanelShown()` & `showLoadPresetDialog()`
- `app/src/main/java/exp/ftxt/ui/fragment/NetworkPanelFragment.java` — Override `onPanelShown()` & `showLoadPresetDialog()`
- `app/src/main/java/exp/ftxt/ui/fragment/ColorPickerPanelFragment.java` — Override `onPanelShown()` (ColorPicker tidak punya overlay → tidak butuh `showLoadPresetDialog`)
- `app/src/main/java/exp/ftxt/core/NotificationHelper.java` — Hapus `setTextViewText(R.id.noti_title, ...)` di `buildNotificationDynamic()`; teks suhu tetap ditampilkan lewat `setContentTitle()`
- `app/src/main/res/layout/panel_battery.xml`, `panel_battery_current.xml`, `panel_battery_percentage.xml`, `panel_clock.xml`, `panel_fps.xml`, `panel_network.xml`, `panel_text.xml` — Hapus `android:visibility="gone"` di root + tambah `active_preset_label` TextView
- `app/src/main/res/layout/panel_color_picker.xml`, `panel_crosshair.xml`, `panel_logo.xml` — Hapus `android:visibility="gone"` di root
- `app/src/main/java/exp/ftxt/ui/TextPanelController.java` — Hapus konstruktor lama + method `bindViews()` tanpa parameter
- `app/src/main/java/exp/ftxt/ui/FpsPanelController.java` — Hapus konstruktor lama + method `bindViews()` tanpa parameter + hapus import CheckBox ganda
- `app/src/main/java/exp/ftxt/ui/ClockPanelController.java` — Hapus konstruktor lama + method `bindViews()` tanpa parameter
- `app/src/main/java/exp/ftxt/ui/BatteryPanelController.java` — Hapus konstruktor lama + method `bindViews()` tanpa parameter
- `app/src/main/java/exp/ftxt/ui/BatteryCurrentPanelController.java` — Hapus konstruktor lama + method `bindViews()` tanpa parameter
- `app/src/main/java/exp/ftxt/ui/BatteryPercentagePanelController.java` — Hapus konstruktor lama + method `bindViews()` tanpa parameter + hapus import CheckBox ganda
- `app/src/main/java/exp/ftxt/ui/NetworkPanelController.java` — Hapus konstruktor lama + method `bindViews()` tanpa parameter
- `app/src/main/java/exp/ftxt/ui/ColorPickerPanelController.java` — Hapus konstruktor lama + method `bindViews()` tanpa parameter
- `app/src/main/java/exp/ftxt/ui/TextPositionController.java` — Hapus konstruktor lama + method `bindViews()` tanpa parameter
- `app/src/main/java/exp/ftxt/ui/FpsPositionController.java` — Hapus konstruktor lama + method `bindViews()` tanpa parameter
- `app/src/main/java/exp/ftxt/ui/ClockPositionController.java` — Hapus konstruktor lama + method `bindViews()` tanpa parameter
- `app/src/main/java/exp/ftxt/ui/BatteryPositionController.java` — Hapus konstruktor lama + method `bindViews()` tanpa parameter
- `app/src/main/java/exp/ftxt/ui/BatteryCurrentPositionController.java` — Hapus konstruktor lama + method `bindViews()` tanpa parameter
- `app/src/main/java/exp/ftxt/ui/BatteryPercentagePositionController.java` — Hapus konstruktor lama + method `bindViews()` tanpa parameter
- `app/src/main/java/exp/ftxt/ui/NetworkPositionController.java` — Hapus konstruktor lama + method `bindViews()` tanpa parameter
### 🔢 Version
`4.84.0` → `4.84.1`

---

# [4.84.0] - 2026-07-28
### ✨ Fitur Baru
- **Tanggal di Bawah Jam — Tampilan dua baris** — Clock module sekarang menampilkan tanggal di bawah jam dalam format dua baris (`HH:mm:ss` di atas, `MMM dd EE` di bawah). Diaktifkan secara default. Bisa di-toggle via panel jam (checkbox "Tanggal").
### ♻️ Perubahan Fitur
- **Refactor Panel Navigation — Fragment-based** — Ubah sistem navigasi panel dari View visibility manual ke Fragment-based. Setiap panel (text, fps, clock, battery, battery_pct, battery_cur, network, color_picker) punya Fragment sendiri + PanelManager untuk mengelola show/hide. Semua PanelController & PositionController mendapat overload konstruktor dengan `(Activity, View rootView)` untuk binding di Fragment. bindViews() lama delegasi ke bindViews(rootView). MainActivity di-refactor: hapus semua field View panel, field controller, hideAllPanels(), dan if-else visibility — ganti dengan PanelManager.
### ✏️ File Changed
- `app/build.gradle` — versionCode 175, versionName 4.84.0
- `app/src/main/java/exp/ftxt/features/clock_module/ClockConfig.java` — Tambah field `showDate` (default `true`)
- `app/src/main/java/exp/ftxt/features/clock_module/ClockModule.java` — Update `getCurrentTime()` untuk tampilkan tanggal via `\n` saat `showDate` aktif, tambah `setLineSpacing` & `setIncludeFontPadding(false)` untuk tampilan dua baris
- `app/src/main/res/layout/panel_clock.xml` — Tambah checkbox "Tanggal" (`clockShowDateSwitch`)
- `app/src/main/java/exp/ftxt/ui/ClockPanelController.java` — Bind + listener untuk `clockShowDateSwitch`, simpan ke SharedPreferences
- `app/src/main/java/exp/ftxt/ui/PanelManager.java` — Tambah method `onPanelShown()`, `showLoadPresetDialog()`
- `app/src/main/java/exp/ftxt/ui/BasePanelFragment.java` — Tambah method `onPanelShown()`, `showLoadPresetDialog()`
- `app/src/main/java/exp/ftxt/MainActivity.java` — Refactor: hapus 9 field View panel + 7 field controller + hideAllPanels() + cleanup di onDestroy, ganti dengan PanelManager; tambah helper panelIdToName() & updateActionBarTitle()
- `app/src/main/java/exp/ftxt/ui/BatteryPercentagePanelController.java` — Tambah konstruktor `(MainActivity, View)` + overload bindViews(View)
- `app/src/main/java/exp/ftxt/ui/BatteryPercentagePositionController.java` — Tambah konstruktor `(Activity, View)` + overload bindViews(View)
- `app/src/main/java/exp/ftxt/ui/BatteryCurrentPanelController.java` — Tambah konstruktor `(MainActivity, View)` + overload bindViews(View)
- `app/src/main/java/exp/ftxt/ui/BatteryCurrentPositionController.java` — Tambah konstruktor `(Activity, View)` + overload bindViews(View)
- `app/src/main/java/exp/ftxt/ui/NetworkPanelController.java` — Tambah konstruktor `(MainActivity, View)` + overload bindViews(View)
- `app/src/main/java/exp/ftxt/ui/NetworkPositionController.java` — Tambah konstruktor `(Activity, View)` + overload bindViews(View)
- `app/src/main/java/exp/ftxt/ui/ColorPickerPanelController.java` — Tambah konstruktor `(MainActivity, View)` + overload bindViews(View)
- `app/src/main/java/exp/ftxt/ui/fragment/BatteryPercentagePanelFragment.java` — Isi lifecycle: init controller di onViewCreated, cleanup di onDestroyView
- `app/src/main/java/exp/ftxt/ui/fragment/BatteryCurrentPanelFragment.java` — Isi lifecycle
- `app/src/main/java/exp/ftxt/ui/fragment/NetworkPanelFragment.java` — Isi lifecycle
- `app/src/main/java/exp/ftxt/ui/fragment/ColorPickerPanelFragment.java` — Isi lifecycle
### 🔢 Version
`4.83.2` → `4.84.0`

---

# [4.83.2] - 2026-07-28
### ✨ Fitur Baru
- **Ikon Notifikasi Dinamis — nilai suhu baterai di status bar** — Ikon notifikasi foreground service sekarang menampilkan nilai suhu baterai aktual (misal `37°`) yang di-generate secara dinamis sebagai Bitmap. Update setiap 10 detik. Title notifikasi juga menampilkan suhu (misal `FTxT 37°C`).
### 🔧 Optimasi & Penyesuaian
- **Optimasi Memori & Proses — Lazy Init, Conditional Resources** — Hemat memori dan baterai dengan 5 perubahan: (1) **Lazy Init Module** — 7 module overlay tidak lagi diinstansiasi semua di `onCreate()`. Module baru dibuat saat pertama diaktifkan. (2) **Cleanup Module saat Stop** — Null-kan `params` dan `choreographer` saat module di-stop agar bisa di-GC. (3) **Conditional WakeLock** — WakeLock hanya diambil jika ada module yang aktif. Saat semua overlay mati, CPU bisa tidur. (4) **Conditional BroadcastReceiver** — Receiver `CONFIG_CHANGED` hanya aktif saat ada overlay berjalan. (5) **Conditional Service Stop** — Service otomatis `stopSelf()` saat module terakhir di-stop.
### ✏️ File Changed
- `app/build.gradle` — versionCode 174, versionName 4.83.2
- `app/src/main/java/exp/ftxt/core/NotificationHelper.java` — Tambah `generateIcon()` (Bitmap dinamis dari teks), `getBatteryTemp()` (baca suhu baterai), `buildNotificationDynamic()` (notifikasi dengan `Icon.createWithBitmap` + `DecoratedCustomViewStyle`), `startIconCycling()`/`stopIconCycling()` (update setiap 10 detik)
- `app/src/main/java/exp/ftxt/core/FloatingService.java` — Lazy init module (`ensure*Module()`), conditional WakeLock (`acquireWakeLockIfNeeded`/`releaseWakeLockIfEmpty`), conditional BroadcastReceiver (`registerConfigReceiver`/`unregisterConfigReceiver`), conditional service stop (`stopSelfIfEmpty`), panggil `NotificationHelper.startIconCycling()` di `onCreate()` dan `stopIconCycling()` di `onDestroy()`
- `app/src/main/java/exp/ftxt/core/WakeLockManager.java` — Tambah method `isHeld()`
- `app/src/main/java/exp/ftxt/features/floating_text/TextModule.java` — Tambah `params = null` di `destroyOverlay()`
- `app/src/main/java/exp/ftxt/features/fps_display/FpsModule.java` — Tambah `choreographer = null` + `params = null` di `stop()`
- `app/src/main/java/exp/ftxt/features/clock_module/ClockModule.java` — Tambah `params = null` di `stop()`
- `app/src/main/java/exp/ftxt/features/battery_temperature/BatteryModule.java` — Tambah `params = null` di `stop()`
- `app/src/main/java/exp/ftxt/features/battery_percentage/BatteryPercentageModule.java` — Tambah `params = null` di `stop()`
- `app/src/main/java/exp/ftxt/features/battery_current/BatteryCurrentModule.java` — Tambah `params = null` di `stop()`
- `app/src/main/java/exp/ftxt/features/network_stats/NetworkModule.java` — Tambah `params = null` di `stop()`
### 🔢 Version
`4.83.1` → `4.83.2`

---

# [4.83.1] - 2026-07-28
### 🔧 Optimasi & Penyesuaian
- **Refactor FloatingService — Hapus Duplikasi Kode (Langkah 1-4)** — Buat interface `OverlayModule` untuk menyeragamkan semua modul overlay. Implement di 7 modul. Hapus ~430 baris static delegates di FloatingService, ganti dengan method generik berbasis loop (`startModule`, `stopModule`, `updateColorForModule`, `updateSizeForModule`, dll). FloatingService turun dari 785 → 351 baris (-55%). Update semua 14 UI controllers (7 PanelController + 7 PositionController) untuk pakai method generik baru.
### 🗒️ File Added
- `app/src/main/java/exp/ftxt/shared/ui/OverlayModule.java` — Interface untuk menyeragamkan method semua modul overlay
### ✏️ File Changed
- `app/build.gradle` — versionCode 173, versionName 4.83.1
- `app/src/main/java/exp/ftxt/core/FloatingService.java` — Hapus ~430 baris static delegates, tambah method generik + static getters, pakai loop di onCreate/onDestroy
- `app/src/main/java/exp/ftxt/features/floating_text/TextModule.java` — Implement OverlayModule: +start() +stop() +isRunning() +updateLabelColor() +@Override
- `app/src/main/java/exp/ftxt/features/fps_display/FpsModule.java` — Implement OverlayModule: +init() overload +@Override
- `app/src/main/java/exp/ftxt/features/clock_module/ClockModule.java` — Implement OverlayModule: +init() +updateLabelColor() +@Override
- `app/src/main/java/exp/ftxt/features/battery_temperature/BatteryModule.java` — Implement OverlayModule: +init() +@Override
- `app/src/main/java/exp/ftxt/features/battery_percentage/BatteryPercentageModule.java` — Implement OverlayModule: +init() +@Override
- `app/src/main/java/exp/ftxt/features/battery_current/BatteryCurrentModule.java` — Implement OverlayModule: +init() +@Override
- `app/src/main/java/exp/ftxt/features/network_stats/NetworkModule.java` — Implement OverlayModule: +init() +@Override
- `app/src/main/java/exp/ftxt/ui/TextPanelController.java` — Ganti static delegates ke method generik
- `app/src/main/java/exp/ftxt/ui/TextPositionController.java` — Ganti static delegates ke method generik
- `app/src/main/java/exp/ftxt/ui/FpsPanelController.java` — Ganti static delegates ke method generik
- `app/src/main/java/exp/ftxt/ui/FpsPositionController.java` — Ganti static delegates ke method generik
- `app/src/main/java/exp/ftxt/ui/ClockPanelController.java` — Ganti static delegates ke method generik
- `app/src/main/java/exp/ftxt/ui/ClockPositionController.java` — Ganti static delegates ke method generik
- `app/src/main/java/exp/ftxt/ui/BatteryPanelController.java` — Ganti static delegates ke method generik
- `app/src/main/java/exp/ftxt/ui/BatteryPositionController.java` — Ganti static delegates ke method generik
- `app/src/main/java/exp/ftxt/ui/BatteryPercentagePanelController.java` — Ganti static delegates ke method generik
- `app/src/main/java/exp/ftxt/ui/BatteryPercentagePositionController.java` — Ganti static delegates ke method generik
- `app/src/main/java/exp/ftxt/ui/BatteryCurrentPanelController.java` — Ganti static delegates ke method generik
- `app/src/main/java/exp/ftxt/ui/BatteryCurrentPositionController.java` — Ganti static delegates ke method generik
- `app/src/main/java/exp/ftxt/ui/NetworkPanelController.java` — Ganti static delegates ke method generik
- `app/src/main/java/exp/ftxt/ui/NetworkPositionController.java` — Ganti static delegates ke method generik
### 🔢 Version
`4.83.0` → `4.83.1`

---

# [4.83.0] - 2026-07-23
### ✨ Fitur Baru
- **Notification Actions — aksi langsung dari notifikasi** — Tambahkan 3 tombol aksi di notifikasi foreground service: **Toggle** (show/hide semua overlay), **Kill** (hentikan service + tutup app), **Buka** (launch aplikasi). User bisa kontrol overlay tanpa membuka aplikasi. Toggle hanya menyembunyikan/menampilkan overlay tanpa mengubah status modul.
### 🐞 Bug Fixes
- **Ikon aksi notifikasi tidak tampil** — Ganti dari `NotificationCompat.Action` ke **custom notification layout** dengan `RemoteViews` + `ImageButton`. Ikon vector drawable sekarang tampil dengan benar. Toggle menggunakan ikon mata (visible/invisible), Kill pakai X, Buka pakai ikon open. Terinspirasi dari sampel res aplikasi Crosshair Hero.
### 🗒️ File Added
- `app/src/main/java/exp/ftxt/core/NotificationActionReceiver.java` — Handle aksi notifikasi (toggle, kill, open app)
- `app/src/main/res/layout/notification_custom.xml` — Custom notification layout dengan RemoteViews + ImageButton
- `app/src/main/res/drawable/ic_notification_visible.xml` — Ikon mata terbuka untuk toggle show (dari sampel Crosshair Hero)
- `app/src/main/res/drawable/ic_notification_invisible.xml` — Ikon mata tertutup untuk toggle hide (dari sampel Crosshair Hero)
- `app/src/main/res/drawable/ic_notification_toggle.xml` — Icon toggle overlay untuk notifikasi (mata terbuka)
- `app/src/main/res/drawable/ic_notification_toggle_off.xml` — Icon toggle off untuk notifikasi (mata tertutup)
- `app/src/main/res/drawable/ic_notification_stop.xml` — Icon kill service untuk notifikasi
- `app/src/main/res/drawable/ic_notification_open.xml` — Icon buka aplikasi untuk notifikasi
### ✏️ File Changed
- `app/build.gradle` — versionCode 172, versionName 4.83.0
- `CHANGELOG.md` — Entry 4.83.0 baru (notification actions)
- `app/src/main/AndroidManifest.xml` — Register NotificationActionReceiver
- `app/src/main/java/exp/ftxt/core/FloatingService.java` — Tambah updateNotification() + stopAllModules() + hideAllOverlays() + showAllOverlays() + areAllOverlaysHidden()
- `app/src/main/java/exp/ftxt/core/NotificationHelper.java` — Ganti ke custom RemoteViews layout + onClickPendingIntent + setImageViewResource + hapus addAction
- `app/src/main/java/exp/ftxt/features/floating_text/TextModule.java` — Tambah hide() + show() + isHidden()
- `app/src/main/java/exp/ftxt/features/fps_display/FpsModule.java` — Tambah hide() + show() + isHidden()
- `app/src/main/java/exp/ftxt/features/clock_module/ClockModule.java` — Tambah hide() + show() + isHidden()
- `app/src/main/java/exp/ftxt/features/battery_temperature/BatteryModule.java` — Tambah hide() + show() + isHidden()
- `app/src/main/java/exp/ftxt/features/battery_percentage/BatteryPercentageModule.java` — Tambah hide() + show() + isHidden()
- `app/src/main/java/exp/ftxt/features/battery_current/BatteryCurrentModule.java` — Tambah hide() + show() + isHidden()
- `app/src/main/java/exp/ftxt/features/network_stats/NetworkModule.java` — Tambah hide() + show() + isHidden()
- `app/src/main/res/drawable/ic_notification_toggle.xml` — Hapus tint textColorPrimary
- `app/src/main/res/drawable/ic_notification_toggle_off.xml` — Hapus tint textColorPrimary
- `app/src/main/res/drawable/ic_notification_stop.xml` — Hapus tint textColorPrimary
- `app/src/main/res/drawable/ic_notification_open.xml` — Hapus tint textColorPrimary
- `README.md` — Update versi ke 4.83.0, tanggal 2026-07-28
- `STRUKTUR.md` — Update statistik (Java 57, Layout 21, Drawable XML 30) + tree (NotificationActionReceiver, notification_custom, ic_notification visible/invisible)
- `.gitignore` — Tambah `/_temp/`
- `app/src/main/assets/CHANGELOG.md` — Sync dari root
- `app/src/main/assets/README.md` — Sync dari root
- `app/src/main/assets/STRUKTUR.md` — Sync dari root
### 🔢 Version
`4.82.4` → `4.83.0`

---

# [4.82.0–4.82.4] - 2026-06-18 — 2026-07-12
### ✨ Fitur Baru
- **Menu Color Picker di Navigasi Drawer** — Panel Color Picker baru di sidebar. Color Disk (hue ring + SV triangle) + Hue/Saturation/Brightness/Alpha slider, RGB slider collapsible, saved colors (16 slot), HEX/HSV/ARGB display, HEX editor, color name resolver, checkerboard transparansi.
- **BootReceiver — auto-start overlay setelah reboot** — `BroadcastReceiver` otomatis memulai `FloatingService` saat HP reboot jika sebelumnya ada modul overlay yang aktif.
### 🚮 Fitur Dihapus
- **XY Pad dihapus total** — Fitur XY Pad (2D drag area) dihapus dari FPS dan Floating Text. Kontrol posisi kembali ke slider X/Y + D-Pad.
### ♻️ Perubahan Fitur
- **Color Picker Dialog digabung** — `ColorPickerDialog` menggunakan model yang sama dengan panel: `TriangleColorPickerView` + custom slider, share saved colors.
- **Color Picker panel digabung** — Color Wheel dan slider tampil bersamaan dalam satu panel. Mode switching dan "Model Color Picker" di Konfigurasi dihapus.
- **FPS rolling average** — FPS dihitung dari 60 frame terakhir (circular buffer), hasil lebih akurat.
- **WakeLock auto-renew** — `WakeLockManager` menggunakan `acquire(5 menit)` dengan auto-renew setiap 4 menit.
- **FloatingService START_STICKY** — `onStartCommand()` return `START_STICKY` agar Android me-restart service jika ter-kills.
### 🔧 Optimasi & Penyesuaian
- **Segitiga HSV Color Wheel rendering dioptimasi** — Render bitmap segitiga di resolusi 0.5× lalu scale-up. Pixel turun 75%.
- **Color Wheel Disk Dialog dioptimasi** — Cache shader `SweepGradient` & `RadialGradient`, dibuat ulang hanya saat ukuran view berubah.
### 🐞 Bug Fixes
- **FPS 2x lipat saat ubah interval** — Choreographer callback lama tidak di-cancel saat stop+start. Ditambahkan `removeFrameCallback()`.
- **Foreground service crash di Android 14+ (targetSdk 35)** — Ganti `foregroundServiceType="dataSync"` ke `specialUse` + izin `FOREGROUND_SERVICE_SPECIAL_USE`.
- **Overlay tidak mengikuti perubahan orientasi sistem** — Ditambahkan `BroadcastReceiver ACTION_CONFIGURATION_CHANGED` yang me-reset suffix dan me-load posisi ulang.
- **Slider Color Picker tersendat & terintercept scroll** — Tambah `requestDisallowInterceptTouchEvent`, pakai `getRawX()`.
- **Triangle & Ring color wheel terintercept scroll** — `TriangleColorPickerView` & `HSVColorPickerView` mencegah NestedScrollView meng-intercept touch event.
- **Overlay hilang/off-screen saat orientasi berubah** — Semua PositionController panggil `updatePositionStatic()` di akhir konstruktor.
### 🔢 Version
`3.12.4.80.0` → `4.82.4`

---

# [3.11.3.78.1–3.12.4.80.0] - 2026-06-13
### ✨ Fitur Baru
- **Mode Hue Slider** — UI color picker alternatif dengan slider Hue/Saturation/Brightness/Opacity, swatch Current/Previous, info AHEX/HSV/ARGB, saved colors (16 slot), checkerboard transparansi.
- **Opsi Model Color Picker** — Pilihan Color Wheel atau Hue Slider di Konfigurasi.
- **Saved Colors** — Grid 2×8, drag-reorder, animasi geser saat simpan/hapus.
### 🚮 Fitur Dihapus
- **Watermark Module** — Seluruh modul Watermark dihapus total. Mode Segel dipindahkan ke Floating Text.
- **Material Design Colors** — ~270 warna Material Design dihapus dari `ColorNameResolver.java`. Tersisa CSS (148) + XKCD (~950).
### 📥 Fitur Dipulihkan
- **Mode Segel dipulihkan dari Watermark ke Floating Text** — Fitur pola teks diulang diagonal dipulihkan ke modul Floating Text.
### ♻️ Perubahan Fitur
- **Dokumentasi in-app: AlertDialog → Activity penuh** — Dokumentasi pindah ke activity dengan daftar dokumen, markdown renderer via Markwon, zoom ± di toolbar.
- **Drag & drop reorder preset** — Migrasi ListView ke RecyclerView + ItemTouchHelper.
- **Drag & drop drawer sidebar** — Migrasi LinearLayout+DragEvent ke RecyclerView+ItemTouchHelper.
- **Hue Slider layout redesain** — Info warna di atas, swatch horizontal, label slider pendek.
- **Tombol switch mode color picker** — Tombol ⇄ di title bar untuk menukar mode.
- **Thumb slider jadi lingkaran** — Dari rectangle 2×10dp ke oval 12×12dp.
- **Saved Colors drag-reorder + animasi** — Long-press swap, animasi geser 300ms.
### 🔧 Optimasi & Penyesuaian
- **Release notes GitHub strip section file** — Workflow `release.yml` menyaring section file dari deskripsi release.
- **Sync dokumen .md root → assets saat build** — Gradle task `syncDocs` otomatis menyalin dokumen.
- **Lint `MissingDefaultResource` dinonaktifkan** — Agar `lintVitalRelease` tidak gagal karena drawable malam.
### 🐞 Bug Fixes
- **Slider mode color picker crash (NPE)** — Custom slider tanpa cek null. Diperbaiki dengan null check.
- **Build gagal: cannot find symbol Animator** — Import `android.animation.Animator` ditambahkan.
- **Edit HEX di Hue Slider mode force close (NPE)** — Null check di `setThumbPos()`.
- **Posisi overlay antar orientasi saling menimpa** — Reset `orientationSuffix` di `cleanup()` semua PositionController.
### 🔢 Version
`3.11.3.78.0` → `3.12.4.80.0`

---

# [3.11.3.78.0 – 3.9.3.74.2] - 2026-06-12–13
Menggabungkan 5 release.
### 🚮 Fitur Dihapus
- **Tombol Ekspor Semua di dialog preset** — Dihapus dari bottom bar browser preset.
- **Tombol Simpan/Muat Preset dari panel posisi** — Semua 8 panel: tombol Simpan/Muat/label preset dihapus. Fungsi via gear → "Muat Preset".
### ✨ Fitur Baru
- **Mode Tandai & aksi batch preset** — Tombol Tandai/Tandai Semua, aksi batch (Hapus/Favorit/Bagikan/Ekspor), opsi Gunakan Preset, drag reorder.
- **Opsi Ganti Ikon Aplikasi** — Toggle Default/Alternatif di Konfigurasi.
- **Background tema gelap & terang** — Drawer, header, toolbar, layar utama punya background gambar sendiri per tema.
- **1.351 Color Names** — ColorNameResolver: 148 CSS + 254 Material + 949 XKCD colors.
### ♻️ Perubahan Fitur
- **Dialog preset terpusat** — PresetBrowserDialog punya tombol Simpan, title di XML header, preset baru di urutan teratas.
- **Tutup Aplikasi pindah nav drawer** — Kill Service + Keluar di drawer; Konfirmasi Keluar jadi default.
### 🐞 Bug Fixes
- **Preset baru tidak muncul** — `onSaveClick` async diperbaiki dengan `Consumer<Runnable>` callback.
- **Overlay tidak restart setelah Kill Service** — `isAnyModuleActive()` sekarang cek semua modul.
### 🔢 Version
versionCode: 156 → 160
versionName: 3.9.3.74.2 → 3.11.3.78.0

---

# [3.9.3.74.1 – 3.9.3.72.0] - 2026-06-12
Menggabungkan 5 release.
### ✨ Fitur Baru
- **Sembunyikan Label Battery Current** — Opsi value-only di panel Battery Current, voltase dalam V bukan mV.
- **Sembunyikan Label Network Stats** — Opsi value-only di panel Network Stats.
- **Warna Label Terpisah** — FPS, Battery Temp, Battery%, Battery Current, Network: nilai dan label bisa diwarnai berbeda via SpannableString.
### ♻️ Perubahan Fitur
- **Preview warna dalam satu baris + jadi kotak 30×30** — Semua 8 panel: color button diganti preview kotak 30×30, disusun horizontal.
- **Kontrol Interval single button + PopupWindow** — FPS, Battery, Battery Current, Network: interval pilih dari daftar popup, bukan tombol -/+.
- **Layout ulang Battery Current** — Sembunyikan Label+Interval → V/mA/W → Ukuran → Warna.
- **Rename "Hanya Tampilkan Nilai" → "Sembunyikan Label"** — FPS & Battery.
- **Satuan mV→V** — Voltase baterai dari `3900mV` jadi `3.9V`.
- **Interval inline pindah ke samping checkbox** — FPS, Battery, Network.
### 🔧 Optimasi & Penyesuaian
- **BackgroundConfig.java** — Ekstrak konfigurasi background ke file sendiri, semua 8 module pakai `BackgroundConfig`.
### 🐞 Bug Fixes
- **Duplicate ID XML** — Hapus duplikasi `batCurIntervalValue`.
- **Checkbox V/mA/W tidak refresh overlay** — Setiap toggle panggil `updateBatteryCurrentColorStatic()`.
- **Battery Temperature labelColor span kelebihan** — Perbaiki setSpan per karakter label bukan sampai akhir.
- **Network showOnlyValue** — Refactor pakai `updateNetworkColorStatic()`.
### 🔢 Version
versionCode: 151 → 155
versionName: 3.9.3.72.0 → 3.9.3.74.1

---

# [3.9.3.71.0 – 3.9.3.69.6] - 2026-06-11–12
Menggabungkan 5 release.
### ✨ Fitur Baru
- **Interval Update float 0.2–10s** — FPS, Network, Battery Current, Battery Temperature: step 0.2s, 0.5s, 0.75s, 1–10s.
- **Kontrol Interval Battery Stats** — Tombol -/+ di panel Battery, interval 1–10 detik.
### 🔧 Optimasi & Penyesuaian
- **updateInterval int→float** — Semua Config & module pakai float, `readFloatPref()` untuk migrasi SP.
- **Centralized isAnyModuleActive()** — Ekstrak logic cek modul aktif ke method terpusat di MainActivity.
### 🐞 Bug Fixes
- **Semua modul ikut nonaktif saat satu dimatikan** — `isAnyModuleActive()` cek semua 7 modul, bukan 1–2.
- **CI signing path dobel app/app/** — storeFile `app/release.jks` → `release.jks`.
- **APK release tidak signed** — Tambah step generate keystore.properties di workflow.
### 🔢 Version
versionCode: 144 → 149
versionName: 3.9.3.69.6 → 3.9.3.71.0

---

# [3.9.3.69.5 – 3.9.3.69.1] - 2026-06-11
Menggabungkan 5 release.
### ♻️ Perubahan Fitur
- **Collapsible Section Grouping** — Semua 8 panel: section Tampilan, Posisi, Shadow, Background collapsible dengan SectionHelper.
- **release.yml decode keystore** — Tambah step decode & mkdir untuk CI.
- **CHANGELOG.md entry header** — Tambah catatan Major 1; hapus duplikasi.
### 🔧 Optimasi & Penyesuaian
- **Refactor layout ekstrak panel** — Pisahkan 8 panel dari `activity_main.xml` ke file `<include>` terpisah.
- **Dokumentasi konsolidasi** — STRUKTUR/DEVELOPMENT/TENTANG dihapus, merge ke README.
- **README.md struktur lengkap** — Semua file + direktori punya deskripsi.
- **syncDocs 3 file** — Hanya README/CHANGELOG/PANDUAN; txt dikelola manual.
### 🐞 Bug Fixes
- **release.yml restore** — Kembalikan workflow ke versi kerja (Java 17, secret names benar).
### 🗒️ File Added
- `panel_text.xml`, `panel_fps.xml`, `panel_clock.xml`, `panel_battery_current.xml`, `panel_network.xml`, `panel_crosshair.xml`, `panel_watermark.xml`, `panel_logo.xml`
- `SectionHelper.java`, `key/.gitkeep`
### 🔥 File Removed
- `STRUKTUR.md`, `DEVELOPMENT.md`, `TENTANG.md` + assets .txt
### 🔢 Version
versionCode: 139 → 143
versionName: 3.9.3.69.1 → 3.9.3.69.5

---

# [3.9.3.69.0 – 3.9.3.67.0] - 2026-06-01–02
Menggabungkan 5 release.
### ✨ Fitur Baru
- **Watermark Overlay** — Modul watermark teks kustom (ukuran 5–200sp, shadow, bg, posisi, touch passthrough, safe area).
- **Watermark Seal Pattern** — Mode segel: teks diulang diagonal dengan kontrol spasi H/V dan sudut.
- **PresetBrowserDialog** — Dialog modern dengan search, filter, favorite, rename, delete, reorder, export/import.
- **Active Preset Label** — Setiap panel tampilkan label preset aktif.
### ♻️ Perubahan Fitur
- **Tombol E/I dihapus** dari 7 panel — fungsi ekspor/impor sudah ada di PresetBrowserDialog.
- **Tampilan FPS diseragamkan** — Hapus gaya neumorphism, ganti ke inline style default.
### 🔧 Optimasi & Penyesuaian
- **Java 8 → Java 17** — sourceCompatibility/targetCompatibility dinaikkan.
- **Ekstrak PresetHandler** — ~800 baris kode duplikat dari 7 PositionController ke shared class.
- **GitHub Actions Workflow** — CI/CD build & release APT otomatis.
- **Bersihkan styles & colors** — Hapus 6 style + 5 color neumorphism tidak terpakai.
### 🐞 Bug Fixes
- **CI build gagal** — SDK license, platform 35, AAPT2, local.properties.
- **Import FC PresetBrowserDialog** — `registerForActivityResult` pindah ke `onCreate`.
### 🔢 Version
versionCode: 134 → 138
versionName: 3.9.3.67.0 → 3.9.3.69.0

---

# [3.9.3.66.0 – 2.6.2.48.0] - 2026-05-16–06-01
Menggabungkan 5 entry CHANGELOG (1 individual + 4 merger).
### ✨ Fitur Baru
- **Preset System v2** — UUID-based index, metadata (tags, favorite, timestamps), thumbnail generation, version history (10), partial-apply API, search/filter, share intent, file picker import.
- **Safe Area** — Checkbox "Gunakan Area Aman" di 6 panel overlay.
- **Battery Percentage & Battery Current Overlay** — Dua modul baru: persentase baterai dan tegangan/arus/daya.
- **Network Speed Meter** — Kecepatan internet real-time ↓↑ dengan kontrol posisi.
- **Jam Digital & Suhu Baterai Overlay** — Waktu 24 jam HH:mm:ss dan suhu °C.
- **Preset GSON full-config** — Simpan/muat seluruh konfigurasi overlay via PresetManager.
- **Tombol Muat di Semua Panel** — Tombol "Muat Preset" fisik di layout setiap panel.
- **Auto-request izin saat pertama buka**.
### ♻️ Perubahan Fitur
- **Restruktur folder features** — 9 folder di-rename (battery→battery_temperature, dll).
- **Tema default mode malam**.
- **Semua Switch → CheckBox**; overlay & kunci posisi sejajar horizontal.
- **Tombol orientasi pindah ke toolbar**, hapus dari panel posisi.
- **Export/Import clipboard → file**; Bagikan Preset di semua panel.
- **Battery Monitor → Battery Temperature**.
### 🚮 Fitur Dihapus
- **Grid Posisi 3×3** — Dari FPS, Clock, Battery, Text.
- **Auto Preset Aplikasi** — Berdasarkan orientasi/aplikasi.
- **PositionPresetManager** — Digantikan PresetManager GSON.
- **Clipboard export/import**.
### 🐞 Bug Fixes
- **TextPositionController extra brace** — 28 error kompilasi.
- **Slider posisi tidak sinkron** — Saat drag.
- **Padding background mendorong teks** — Padding hanya saat bg enabled.
### 🔢 Version
versionCode: 106 → 127
versionName: 2.6.2.48.0 → 3.9.3.66.0

---

# [2.6.1.42.0 – 2.3.1.15.0] - 2026-05-16–06-01
Menggabungkan 5 entry CHANGELOG (masing-masing sudah merger 5 release = ~25 release).
### ✨ Fitur Baru
- **Classic Color Wheel** — Full disk + crosshair, two-way sync dengan slider ARGB.
- **Kontrol Posisi 3-in-1** — Slider X/Y, D-Pad, XY Pad dengan shared state float 0.0–1.0.
- **Shadow Config modular** — ShadowConfig, OverlayShadow, ShadowTextView reusable.
- **Background Config** — Ukuran/padding, offset X/Y, margin, radius, switch sendiri, terpisah dari shadow.
- **Alpha Slider** — Kontrol ARGB penuh, HEX 8 digit.
- **Sidebar Grup & Drag** — Grup kustom collapsible, drag-to-reorder, mode hapus.
- **DocumentationActivity** — Halaman dokumentasi penuh dengan 6 dokumen.
- **Konfirmasi Keluar** — Double-tap back, opsi di Konfigurasi.
- **FPS value-only** — Sembunyikan label "FPS".
- **Edit HEX/ARGB Manual** — Ikon sunting, klik label slider, salin nilai warna.
- **Akses Izin di Pengaturan** — Toggle overlay, notifikasi, optimasi baterai.
- **Auto-Start & Permission** — Minta izin dan start overlay otomatis saat buka.
- **Force Close / Kill App** — Tutup semua service + app.
- **FPS Draggable** — FPS bisa digeset dan dikunci.
- **Position Migration** — Pixel absolut → persentase layar, backward compatible.
- **Preset Posisi** — Simpan/load 10 preset dengan nama; reset posisi.
- **Android SplashScreen API**.
- **Toggle tema gelap/terang** — Via ikon bulan di toolbar kanan.
- **SettingsActivity membaca assets** — Assets sebagai single source of truth.
### ♻️ Perubahan Fitur
- **modules/ → features/** — Refactor package structure.
- **Popup settings di bawah ikon** — PopupMenu + Gravity.END.
- **SettingsActivity → Konfigurasi** — Ringkas, hanya izin.
- **Dokumentasi via popup** — Dipindah dari Settings.
- **README dipecah** — STRUKTUR, PANDUAN, DEVELOPMENT, TENTANG.
- **Offset range -60–60**; default shadow offset 0.
### 🚮 Fitur Dihapus
- **XY Pad → karantina** — Digantikan Slider + D-Pad.
- **Sistem Grup Sidebar** — Flat list.
- **Hardcoded background** — Background dan shadow terpisah.
- **Shadow Opacity** — Alpha via color picker.
- **Module temp/** — Hapus folder deprecated.
### 🔧 Optimasi & Penyesuaian
- **Ekstrak shared component** — DPadController, PositionPresetManager, SliderPositionController, SliderLabelEditor.
- **Ukuran teks diperluas** — Text 1–150sp, FPS 5–140sp.
- **Toolbar biru #2196F3**.
- **HSVColorPickerView ke shared component** — Persiapan struktur modular.
### 🐞 Bug Fixes
- **Overlay dimensi layar lama** — Refresh dari WindowManager.
- **Handler memory leak** — Cleanup di onDestroy.
- **XyPad tidak bisa drag** — DisallowInterceptTouchEvent.
- **SettingsActivity back arrow**.
- **FPS tidak tampil** — Service standalone tanpa text overlay.
- **TextConfig.size** — Posisi overlay gagal termuat.
### 🔢 Version
versionCode: 51 → 96
versionName: 2.3.1.15.0 → 2.6.1.42.0

---

# 💡 Catatan
> Major 2 kebawah telah dipisahkan dari Project.
