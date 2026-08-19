# [4.87.0] - 2026-08-19 16:22 WITA versionCode 184 ***ONGOING***
### ✨ Fitur Baru
- **Toggle "Tampilkan panel Debugging" & "Tampilkan panel Info Memori" di halaman Konfigurasi** — Dua switch baru di halaman Pengaturan > Konfigurasi, section Modul, untuk menampilkan/menyembunyikan panel Debugging dan Info Memori dari sidebar Navigation Drawer. Kedua switch default OFF (sembunyi). Setting tersimpan otomatis dan berlaku persisten.
- **Proteksi password panel Debugging** — Switch panel Debugging terkunci (disabled) dan membutuhkan kunci password untuk membukanya. Kolom input password + tombol Unlock muncul di bawah switch. Setelah password benar dimasukkan dan tombol diklik, switch terbuka dan tombol Relock muncul di sebelah switch untuk mengunci ulang. Status unlock tersimpan persisten.

### 🔧 Optimasi & Penyesuaian
- **Perbaikan typo label "Debuging" → "Debugging"** — Seluruh label navigasi, judul panel, dan string resource diperbaiki dari "Debuging" menjadi "Debugging".
- **Tombol Relock panel Debugging diperkecil** — Tombol Relock di halaman Konfigurasi diubah dari `Button` menjadi `TextView` (11sp, warna merah) agar tidak mencolok dan konsisten dengan toggle switch di sebelahnya.
- **Urutan section halaman Konfigurasi ditata ulang** — "Ikon Aplikasi" dipindah ke posisi paling atas, "Akses Izin" di bawahnya. Di section Modul: "Info Memori" di atas "Debugging".

### 🐞 Bug Fixes
- **Battery Stats overlay berkedip (flickering)** — Pembacaan data baterai (`registerReceiver` + `readSysfs`) dilakukan di main thread yang memblok UI thread dan menyebabkan micro-stutter/flicker. Kini pembacaan dipindah ke background thread terpisah, dan UI hanya diupdate dari main thread saat data baru tersedia.

### ✏️ File Changed
- 2026-08-19 06:45 — `strings.xml` — Ubah label "Debuging" → "Debugging"
- 2026-08-19 06:45 — `panel_debuging.xml` — Ubah id & judul panel ke "Debugging"
- 2026-08-19 06:45 — `PanelManager.java` — Ubah key map "debuging" → "debugging"
- 2026-08-19 06:45 — `DebugingPanelFragment.java` — Ubah return panelName ke "debugging"
- 2026-08-19 09:14 — `MainActivity.java` — Ubah label & key "Debuging"/"debugging", `rebuildSidebar()` jadi public, filter sidebar berdasarkan setting toggle, filter `navMemory` berdasarkan pref `memory_show_in_sidebar`, default `debugging_show_in_sidebar` ke `false`
- 2026-08-19 09:32 — `activity_settings.xml` — Tambah section "Modul" dengan switch "Tampilkan panel Debugging" (disabled) + tombol Relock + switch "Tampilkan panel Info Memori", kolom input password + tombol Unlock + status teks di bawah switch Debugging
- 2026-08-19 09:32 — `SettingsActivity.java` — Tambah `debuggingSidebarSwitch` (disabled default, proteksi password via tombol Unlock, tombol Relock untuk mengunci ulang, status unlock persisten) + `memorySidebarSwitch` dengan load/save prefs
- 2026-08-19 08:13 — `BatteryStatsModule.java` — Pindahkan `readBatterySnapshot()` ke background thread (`HandlerThread`), pisah `buildDisplayText()` & `applyDisplay()`, `tickRunnable` baca data async
- 2026-08-19 16:22 — `activity_settings.xml` — Tombol Relock diubah dari Button ke TextView (11sp, warna merah); urutan section ditata ulang: Ikon Aplikasi di atas Akses Izin, Info Memori di atas Debugging di section Modul
- 2026-08-19 16:22 — `SettingsActivity.java` — Field `debuggingRelockBtn` diubah dari Button ke TextView

---

# [4.86.0] - 2026-08-19 03:20 WITA versionCode 183 ***PUSH***
### ✨ Fitur Baru
- **Panel Battery Bar digabung ke Battery Info dengan tabbed layout** — Panel Battery Bar tidak lagi berdiri sendiri di sidebar. Kini panel Battery Info memiliki 3 tab: **Monitor** (placeholder), **Overlay** (konfigurasi Battery Info + OrderZones), dan **Battery Strip** (seluruh konfigurasi Battery Bar dari panel terpisah sebelumnya). Pengguna beralih antar tab lewat navigasi bawah panel.
- **OrderZones — urutan info baterai via drag-and-drop** — Di tab Overlay, checkbox °C/% diganti view chip drag dua zona (Aktif/Nonaktif) untuk mengatur urutan dan visibilitas info baterai: °C, %, V, mA, W. Chip bisa diseret antar zona untuk menampilkan/menyembunyikan info, dan diurutkan dalam satu zona.
- **Battery Stats mendukung Voltage, Current, dan Power** — Overlay Battery Info kini bisa menampilkan **Voltase (V)**, **Arus (mA)**, dan **Daya (W)** selain °C dan %. Pembacaan dari BatteryManager dengan fallback sysfs.
- **Panel Memory Stats — modul baru untuk monitoring memori** — Panel baru di sidebar dengan 2 tab: **Monitor** (info real-time 14 nilai: Java Heap, Native Heap, Graphics, Total, Gagal; Execution Time, Execution Time Average; Total Free RAM, Total RAM; Jumlah Proses, Proses Active, Proses Stopped, Proses Cached, Proses Minimum; bar RAM; Snapshot export/copy) dan **Overlay** (konfigurasi ukuran, warna, label, separator, shadow, background, posisi, orientasi, dan opsi tampilan). Mendukung background monitor yang tetap berjalan meski service overlay tidak aktif, serta export/copy snapshot ke clipboard.
- **Crash Logger** — Saat force close, stack trace otomatis ditulis ke `FTxT_crash_*.txt` di folder Download (plus cadangan prefs) agar bug mudah dilaporkan tanpa logcat/adb.

### 🚮 Fitur Dihapus
- **Modul Battery Current dihapus total** — Seluruh lapisan modul (config, module, controller, fragment, panel layout) dihapus. Sidebar item, module management, restore on boot, dan notifikasi cek aktif ikut dihapus. Preset lama dengan tipe "batcur" akan di-ignore oleh guard lintas modul.
- **Sidebar item Battery Bar dihapus** — Entry "Battery Bar" di sidebar Navigation Drawer dihapus. Seluruh konfigurasi Battery Bar kini diakses dari tab Battery Strip di dalam panel Battery Info.

### ♻️ Perubahan Fitur
- **Branding panel Battery** — Label sidebar "Battery Stats" diubah jadi "Battery Info". Label checkbox modul diubah dari "Battery Stats" jadi "Battery Info". Nama preset diubah dari "Battery Bar" jadi "Battery Strip".

### 🔧 Optimasi & Penyesuaian
- **Battery Stats menggunakan urutan info yang bisa diatur** — Urutan info baterai (°C, %, V, mA, W) kini ditentukan oleh posisi chip di OrderZones, bukan urutan hardcode. Pembacaan data baterai dilakukan sekali per update lalu teks dibangun sesuai urutan yang dipilih.
- **Perubahan konfigurasi langsung diterapkan** — Perubahan di panel Battery Info dan Battery Strip langsung diterapkan ke overlay tanpa me-restart modul.
- **Memory Stats menggunakan urutan item yang bisa diatur** — Urutan item memori ditentukan oleh posisi chip di OrderZones, bukan hardcode. Pembacaan data memori dilakukan sekali per update lalu teks dibangun sesuai urutan yang dipilih.
- **Background monitor Memory Stats** — Monitoring memori tetap berjalan meski service overlay tidak aktif (opsional), dengan interval polling yang bisa diatur.
- **Panel callback onPanelHidden()** — BasePanelFragment menambah method `onPanelHidden()` yang dipanggil PanelManager saat panel di-hide, memungkinkan controller menghentikan polling atau resource saat panel tidak terlihat.
- **WakeLock screen-off guard** — WakeLock hanya dipegang saat layar menyala (`pm.isInteractive()`); saat layar mati, WakeLock dilepas untuk menghemat baterai.
- **NotificationHelper caching** — Bitmap ikon suhu di-cache (dibuat ulang hanya saat nilai berubah), RemoteViews + onClick PendingIntent dibuat sekali via `ensureCachedViews()`, update notifikasi di-skip bila suhu & ikon toggle tidak berubah.
- **BatteryBarModule permanent receiver** — BroadcastReceiver baterai didaftarkan permanen di `start()` dan dilepas di `stop()`, dengan cache `batteryLevel`/`batteryScale`/`batteryStatus` sehingga tidak perlu `registerReceiver(null, ...)` berulang. Update display skip jika nilai tidak berubah.
- **BatteryBarView lifecycle animasi** — Animasi (fade, shine, wave, chargeWave) otomatis dihentikan saat overlay invisible dan di-restart saat visible via `onVisibilityChanged()`, mencegah CPU waste saat overlay tidak terlihat.

### 🐞 Bug Fixes
- **CPU Monitor muncul di sidebar padahal belum diimplementasikan** — Menu "CPU Monitor" muncul di sidebar Navigation Drawer tanpa ada panel atau implementasi apapun. Saat diklik, tidak menampilkan apa-apa karena panel belum dibuat. Kini item sidebar dihapus.
- **Tab Overlay di panel Memory terlihat aktif sebelum latar belakang diaktifkan** — Saat tab Overlay ditekan tapi background monitor belum aktif, dialog muncul namun tab Overlay tetap berubah warna biru (terlihat aktif) meskipun kontennya masih menampilkan tab Monitor. Kini tab tetap di posisi Monitor saat seleksi ditolak.

### 🗒️ File Added
- `BatteryOrderZonesView.java` — Custom view zona drag chip
- `ic_monitor.xml`, `ic_overlay.xml`, `ic_battery_strip.xml` — Ikon tab
- `bat_nav_item_color.xml` — Color selector navigasi bawah
- `menu_battery_bottom_nav.xml` — Menu navigasi bawah
- `MemoryConfig.java` — Konfigurasi modul Memory Stats
- `MemoryModule.java` — Modul overlay Memory Stats (refreshDisplay, buildItemPart, readSysf)
- `MemoryMonitor.java` — Background monitor polling memori
- `MemoryPanelController.java` — Controller UI panel Memory Stats (tab Monitor + Overlay, OrderZones, export/copy snapshot)
- `MemoryPositionController.java` — Controller posisi & preset untuk modul Memory
- `MemoryPanelFragment.java` — Fragment panel Memory Stats
- `panel_memory.xml` — Layout panel Memory Stats dengan BottomNavigationView
- `menu_memory_bottom_nav.xml` — Menu navigasi bawah panel Memory
- `mem_nav_item_color.xml` — Color selector navigasi bawah Memory
- `mem_card_bg.xml`, `mem_badge_active_bg.xml`, `mem_badge_stopped_bg.xml` — Drawable panel Memory
- `CrashLogger.java` — Crash logger otomatis saat force close ke folder Download
- `DebugingPanelFragment.java` — Fragment panel Debuging (preview ikon rotasi)
- `panel_debuging.xml` — Layout panel Debuging
- `ic_rotation_variant_1.xml`–`ic_rotation_variant_5.xml` — Ikon varian rotasi untuk panel Debuging

### ✏️ File Changed
- `panel_battery.xml` — Diganti ke tabbed layout dengan BottomNavigationView
- `BatteryPanelFragment.java` — Ditambah tab switching + dual controller
- `BatteryPanelController.java` — Integrasi OrderZones, hapus checkbox °C/%
- `BatteryStatsConfig.java` — Tambah field showVoltage, showCurrent, showPower, itemOrder
- `BatteryStatsModule.java` — Tambah metode refreshDisplay(), pembacaan data v/c/p, urutan itemOrder
- `BatteryBarPositionController.java` — Label diubah ke "Battery Strip", sync in-place
- `PanelManager.java` — Hapus entry battery_bar, tambah panggilan `onPanelHidden()` saat panel di-hide
- `MainActivity.java` — Hapus sidebar item battery_bar, ubah label "Battery Stats" → "Battery Info", tambah load prefs mem_* di loadShadowConfigs(), tambah cek MemoryConfig di isAnyModuleActive(), tambah panelIdToName/updateActionBarTitle/updateNavSelection untuk navMemory, init CrashLogger di onCreate
- `FloatingService.java` — Tambah field memoryModule, ensureMemoryModule(), static memoryModule(), restore on onCreate, setBackgroundMonitorEnabled(), updateMemoryInPlace(), MemoryMonitor.stop() di stopAllModules & onDestroy, MemoryConfig.backgroundMonitor di isAnyModuleActive
- `BootReceiver.java` — Tambah restore MemoryConfig.enabled & MemoryConfig.backgroundMonitor on boot
- `NotificationHelper.java` — Tambah cek MemoryConfig di isAnyModuleActive & getActiveModulesText, caching bitmap icon & RemoteViews, skip update jika nilai tidak berubah
- `OverlayPreset.java` — Tambah field itemOrder, showJavaHeap, showNativeHeap, showGraphics, showTotal
- `BasePanelFragment.java` — Tambah method `onPanelHidden()` untuk callback saat panel disembunyikan
- `WakeLockManager.java` — Tambah screen-off guard: skip acquire jika layar mati
- `BatteryBarView.java` — Tambah `onVisibilityChanged()` untuk stop/start animasi saat overlay visible/invisible
- `BatteryBarModule.java` — Permanent BroadcastReceiver dengan cache batteryLevel/Scale/Status, skip update jika nilai tidak berubah
- `styles.xml` — Tambah style BatNavActiveIndicator dan BatNavTextAppearance, MemNavActiveIndicator dan MemNavTextAppearance
- `colors.xml` — Tambah 12 color values untuk panel Memory
- `strings.xml` — Ubah nav_battery → "Battery Info", hapus nav_cpu, nav_battery_current, nav_battery_bar, tambah nav_memory, nav_debuging
- `drawer_menu.xml` — Hapus nav_cpu, nav_battery_current, nav_battery_bar, tambah nav_memory, nav_debuging
- `ids.xml` — Hapus navBatteryCurrent, navBatteryBar, tambah navMemory, navDebuging

### 🔥 File Removed
- `panel_battery_bar.xml` — Tidak terpakai setelah digabung ke panel_battery.xml
- `panel_battery_current.xml` — Tidak terpakai setelah modul Battery Current dihapus
- `BatteryBarPanelFragment.java` — Tidak terpakai setelah digabung ke BatteryPanelFragment
- `BatteryCurrentPanelFragment.java` — Tidak terpakai setelah modul Battery Current dihapus
- `BatteryCurrentPanelController.java` — Tidak terpakai setelah modul Battery Current dihapus
- `BatteryCurrentPositionController.java` — Tidak terpakai setelah modul Battery Current dihapus
- `BatteryCurrentConfig.java` — Tidak terpakai setelah modul Battery Current dihapus
- `BatteryCurrentModule.java` — Tidak terpakai setelah modul Battery Current dihapus

---

# [4.85.5] - 2026-08-15 versionCode 182
### ✨ Fitur Baru
- **Slider Label Edit untuk panel Jam Digital, Battery Stats, Battery Current, Network Speed, dan Battery Bar** — Klik label slider kini membuka dialog edit nilai manual, sama seperti Floating Text & FPS Display. Nilai offset (Offset X/Y, Shadow X/Y) mendukung angka negatif. Slider posisi (X/Y) tidak termasuk. Khusus Battery Bar: slider Kecepatan (Shine/Fade/Wave) bisa diedit dalam **desimal detik** (0,2–5,0), slider Panjang/Ambang Low/Lebar Band/Intensitas Wave memakai satuan %, serta Ketebalan/Radius Sudut memakai px. Untuk mendukung nilai desimal, `SliderLabelEditor` mendapat helper baru `showSliderEditor` (nilai dengan min/max/offset + suffix) dan `showSecEditor` (input desimal detik).
### ♻️ Perubahan Fitur
- **Kecepatan Shine disetarakan dengan kecepatan Wave** — Sebelumnya pada nilai slider yang sama, animasi shine bergerak **4× lebih cepat** dari wave karena animator shine berjalan `-0,5 → 1,5` (band menyapu 2× panjang bar per durasi) sedangkan wave hanya bergeser setengah panjang bar per siklus (`WAVE_CYCLES = 2`). Durasi animator shine kini dikalikan `2 × WAVE_CYCLES` (×4) sehingga band shine menyapu dengan kecepatan visual yang sama dengan gelombang wave saat nilai slider keduanya sama. Perubahan nilai slider Kecepatan Shine juga kini langsung diterapkan saat animator sedang berjalan (animator di-restart), tanpa perlu menunggu charging berhenti/mulai ulang.
- **Rentang waktu animasi Battery Bar diperpanjang menjadi 5 detik** — Slider **Kecepatan Shine**, **Kecepatan Fade**, **Kecepatan Wave (baterai rendah)**, dan **Kecepatan Wave (saat charging)** kini bisa diatur **0,2–5,0 detik** (sebelumnya maks 2,0 detik), step tetap 0,1 detik (max seekbar `18` → `48`). Default tidak berubah: Shine/Fade 1,8 detik, Wave 1,0 detik. Clamp durasi fade di `BatteryBarView.setFadeSpeed()` ikut diperlonggar (maks 2000 → 5000 ms) agar nilai di atas 2 detik benar-benar diterapkan. Nilai tetap tersimpan di prefs `batbar_shine_speed`, `batbar_fade_speed`, `batbar_wave_speed`, `batbar_charge_wave_speed` dan ikut preset.
- **Label deskriptif animasi Wave disederhanakan** — Teks checkbox "Animasi Wave (gelombang mengalir ke kanan)" di section **Animasi Pengisian Daya** dan "Animasi Wave (kedutan gelombang)" di section **Animasi Baterai Rendah** diubah menjadi **"Animasi Wave"** tanpa deskripsi perilaku di dalam kurung.
- **Pemilih Mode Cepat/Manual jadi RadioButton** — Segment button (2 TextView custom dengan background `bg_segment_*`) diganti **RadioGroup** dua RadioButton ("Mode Cepat" / "Manual"), konsisten dengan pemilih orientasi Horizontal/Vertikal di panel yang sama. Perilaku sama: pilihan menentukan `BatteryBarConfig.quickMode` dan tersimpan di prefs `batbar_quick_mode`.
- **Chevron Mode Manual disembunyikan sepenuhnya saat Mode Cepat aktif** — Saat mode cepat dipilih, **header "▾ Mode Manual" beserta seluruh isi section (panjang + kontrol posisi) hilang total** (GONE), bukan hanya tertutup/diburamkan seperti sebelumnya. Saat mode manual dipilih, header & section tampil kembali dalam keadaan terbuka (▾).
### ✏️ File Changed
- `app/src/main/java/exp/ftxt/shared/ui/SliderLabelEditor.java` — Helper baru `showSliderEditor` (nilai integer dengan min/max/offset + suffix label) dan `showSecEditor` (input desimal detik) untuk edit nilai slider Battery Bar
- `app/src/main/java/exp/ftxt/ui/BatteryBarPanelController.java` — Refactor listener 11 slider ke method `applyX`; listener klik label slider (Ketebalan, Panjang, Radius Sudut, Ambang Low, Lebar Band, Intensitas Wave ×2, Kecepatan Shine/Fade/Wave ×2) untuk edit nilai manual via `SliderLabelEditor`; pemilih Mode Cepat/Manual diganti segment button (TextView) → **RadioGroup** (`updateModeGroup`, listener `batbar_modeGroup`); `updateManualVisibility()` kini menyembunyikan **header & isi section Mode Manual (GONE)** saat mode cepat aktif
- `app/src/main/java/exp/ftxt/ui/ClockPanelController.java` — Listener klik label slider (ukuran, shadow, background) untuk edit nilai manual via `SliderLabelEditor`
- `app/src/main/java/exp/ftxt/ui/BatteryPanelController.java` — Listener klik label slider (ukuran, shadow, background) untuk edit nilai manual via `SliderLabelEditor`
- `app/src/main/java/exp/ftxt/ui/BatteryCurrentPanelController.java` — Listener klik label slider (ukuran, shadow, background) untuk edit nilai manual via `SliderLabelEditor`
- `app/src/main/java/exp/ftxt/ui/NetworkPanelController.java` — Listener klik label slider (ukuran, shadow, background) untuk edit nilai manual via `SliderLabelEditor`
- `app/src/main/java/exp/ftxt/features/battery_bar/BatteryBarView.java` — Clamp durasi fade di `setFadeSpeed()` diperlonggar (maks 2000 → 5000 ms) agar nilai 2–5 detik benar-benar diterapkan; durasi animator shine di `startShine()` dikalikan `2 × WAVE_CYCLES` (×4) agar kecepatan visual sama dengan wave; `setShineConfig()` me-restart animator shine saat berjalan agar perubahan kecepatan langsung diterapkan
- `app/src/main/res/layout/panel_battery_bar.xml` — `android:max` seekbar Kecepatan Shine (`batbarShineSpeedSeekBar`), Kecepatan Fade (`batbarFadeSpeedSeekBar`), Kecepatan Wave low (`batbarWaveSpeedSeekBar`), dan Kecepatan Wave charging (`batbarChargeWaveSpeedSeekBar`) dinaikkan `18` → `48` (rentang 0,2–5,0 detik); teks checkbox `batbarChargeWaveCheck` dan `batbarWaveCheck` disederhanakan menjadi "Animasi Wave"; segment button (LinearLayout + 2 TextView `batbar_segmentQuick`/`batbar_segmentManual`) diganti **RadioGroup** (`batbar_modeGroup` + RadioButton `batbar_modeQuick`/`batbar_modeManual`)
### 🔥 File Removed
- `app/src/main/res/drawable/bg_segment_container.xml` — Background container segment button (tidak terpakai setelah diganti RadioGroup)
- `app/src/main/res/drawable/bg_segment_active.xml` — Background tombol segment aktif (tidak terpakai)
- `app/src/main/res/drawable/bg_segment_inactive.xml` — Background tombol segment non-aktif (tidak terpakai)

---

# [4.85.4] - 2026-08-13 version code 181
## 💡 Catatan
- **Mulai saat ini perubahan file apapun yang tidak berkaitan dengan konten utama projec seperti Dokumen, file dan folder root, build/release dll tidak lagi disertakan dalam changelog**

---

# [4.85.3] - 2026-08-13 versionCode 180
### 🔧 Optimasi & Penyesuaian
- **Fix rilis GitHub gagal karena override aapt2** — Property `android.aapt2FromMavenOverride=/usr/bin/aapt2` (ditambahkan di 4.85.1 untuk build lokal AndroidIDE aarch64) merujuk file yang **tidak ada di GitHub runner** (ubuntu x86_64), sehingga `assembleRelease` gagal dan release v4.85.2 tidak jadi terbit. Override kini hanya diterapkan saat sistem `aarch64` (via `app/build.gradle`); di CI AGP memakai aapt2 Maven default.

---

# [4.85.2] - 2026-08-11 versionCode 179
### ✨ Fitur Baru
- **Kontrol animasi Shine Battery Bar** — Efek shine saat charging kini bisa disesuaikan dari section **Animasi Pengisian Daya**: **Checkbox "Animasi Shine"** untuk menyalakan/mematikan efek (default nonaktif — harus diaktifkan manual); **slider Kecepatan Shine** 0,2–2,0 detik (step 0,1 detik, default 1,8 detik, durasi satu sapuan band); **slider Lebar Band** 2–98% dari panjang bar (default 25%). Nilai tersimpan di prefs `batbar_shine_enabled`, `batbar_shine_speed`, `batbar_shine_width` dan ikut serta dalam preset (`shineEnabled`/`shineSpeed`/`shineWidth`).
- **Animasi Wave (kedutan gelombang) saat baterai rendah** — Efek animasi baru di section **Animasi Baterai Rendah**: saat level baterai di bawah ambang low (dan tidak charging), warna isi bar digambar berlapis dengan pola gelombang sinus yang **menjalar** sepanjang bar (2 siklus), terlihat seperti denyut energi yang merambat. Dikontrol lewat **Checkbox "Animasi Wave"** (default nonaktif — harus diaktifkan manual), **slider Kecepatan Wave** 0,2–2,0 detik (step 0,1 detik, default 1,0 detik, durasi satu siklus gelombang), dan **slider Intensitas Wave** 10–100% (default 60%, menentukan kontras gelap-terang gelombang). Berjalan bersamaan dengan animasi fade low. Nilai tersimpan di prefs `batbar_wave_enabled`, `batbar_wave_speed`, `batbar_wave_amplitude` dan ikut serta dalam preset (`waveEnabled`/`waveSpeed`/`waveAmplitude`).
### ♻️ Perubahan Fitur
- **Area Aman selalu terkunci aktif di kedua mode Battery Bar** — Checkbox **Area Aman** kini **selalu terkunci aktif** (tercentang, disabled + diburamkan alpha 30%) di kedua mode: `safeArea` dipaksa `true` di Mode Cepat maupun Mode Manual, tersimpan ke prefs `batbar_safe_area`, dan preset selalu memaksanya `true` saat di-apply. Khusus di **Mode Cepat**, margin 8dp yang sebelumnya menyisakan celah antara bar dan tepi layar **dihilangkan** (`applyLayout` memakai margin 0) sehingga bar menempel penuh ke sisi layar tanpa gap — area aman tetap aktif namun tidak lagi menambah jarak. Di **Mode Manual**, area aman tetap berfungsi clamp posisi agar bar tidak masuk area notch/cutout.
- **UI Mode Cepat/Manual Battery Bar di-redesign** — Checkbox "Mode Cepat" diubah berurutan menjadi **satu Switch** di antara dua label **"Mode Cepat" (kiri)** dan **"Manual" (kanan)**, lalu akhirnya menjadi **segment button**: dua tombol dalam satu kontrol terkelompok (tombol aktif background biru `#4A90D9` dengan teks putih, tombol non-aktif transparan dengan teks abu-abu). Perilaku akhir: klik "Mode Cepat" = Mode Cepat aktif, klik "Manual" = Mode Manual aktif. Saat Mode Cepat aktif: pemilih **Posisi Sisi** (Atas/Bawah/Kiri/Kanan) tampil, section **Mode Manual** otomatis tertutup (chevron ▸) dan kontrol manual diburamkan (alpha 30%) + kontrol posisi dikunci. Saat Manual aktif: pemilih posisi sisi disembunyikan dan section Mode Manual terbuka (chevron ▾). **Chevron "Posisi" dihapus dan kontrol posisi dipindah ke bawah panjang bar** (slider X/Y + D-Pad + area aman), sehingga urutan panel menjadi panjang → posisi → tampilan → baterai rendah.
- **Checkbox "Gunakan Area Aman" dipindah ke section Tampilan** — Sebelumnya checkbox area aman berada di dalam section **Mode Manual** sehingga tidak tampil saat Mode Cepat aktif, padahal margin safe area (8dp) tetap diterapkan di mode cepat tanpa bisa diubah. Kini checkbox dipindah ke section **Tampilan**, sebaris dengan **"Tampilkan Strip Kosong"** (label disingkat "Area Aman"), sehingga bisa diatur di kedua mode.
- **Checkbox "Kunci Posisi" Battery Bar terkunci default** — Checkbox Kunci Posisi kini **disabled & diburamkan** (alpha 30%) dengan label `Kunci Posisi (tidak tersedia)`, sesuai keputusan desain: bar tidak bisa di-drag sehingga posisi diandalkan ke slider + D-Pad dan sentuhan selalu tembus (touch passthrough). Listener checkbox dihapus, nilai `batbar_lock` tetap tersimpan/terbaca dari prefs namun tidak lagi bisa diubah user lewat panel.
- **Pemilih orientasi bar jadi radio button** — Checkbox "Horizontal" diganti **RadioGroup** dengan dua opsi **Horizontal** / **Vertikal** di section Tampilan. Perilaku sama: pilihan menentukan `BatteryBarConfig.horizontal`, tersimpan di prefs `batbar_horizontal`, dan bar langsung di-restart (orientasi vertikal mengisi dari bawah ke atas).
- **Kontrol "Invert" arah pengisian Battery Bar** — Checkbox **Invert** ditambahkan di baris yang sama dengan opsi orientasi (sebelah "Vertikal") di section Tampilan. Saat aktif, arah pengisian bar dibalik: horizontal mengisi dari **kanan ke kiri** (normal: kiri → kanan), vertikal mengisi dari **atas ke bawah** (normal: bawah → atas). Animasi shine saat charging ikut searah dengan pengisian: horizontal menyapu kanan → kiri, vertikal menyapu atas → bawah. Tersimpan di prefs `batbar_invert` dan disertakan ke preset (`barInvert`).
- **Section "Baterai Rendah" diganti nama jadi "Efek dan Animasi"** — Header collapsible section (chevron) di panel Battery Bar yang sebelumnya bertuliskan **"Baterai Rendah"** kini **"Efek dan Animasi"**, karena isi section (warna otomatis, ambang low, kecepatan fade, strip kosong, animasi shine charging) mencakup seluruh efek & animasi bar, bukan hanya perilaku baterai rendah. id & perilaku collapsible tidak berubah.
- **Fitur "Warna Level" jadi pemilih skema warna otomatis** — Checkbox on/off diganti **tombol pemilih skema** di section Tampilan (label `Warna Level:` + tombol menampilkan skema aktif, style seperti tombol Posisi Sisi). Klik → popup daftar skema: **Tanpa Skema** (paling atas, nonaktif), **Klasik 3-warna** (hijau >20%, kuning ≤20%, merah ≤10% — perilaku lama), **Hue Gradien** (warna dihitung per segmen level, lihat bullet "Penyesuaian rentang warna Hue Gradien" di bawah). Saat skema dipilih langsung aktif. Disimpan di prefs `batbar_color_scheme` (migrasi otomatis dari `batbar_auto_color` lama) dan ikut preset (`barColorScheme`). `BatteryBarConfig.autoColor` diganti `colorScheme` + konstanta `SCHEME_NONE`/`SCHEME_CLASSIC`/`SCHEME_HUE` + helper `isAutoColor()`.
- **Penyesuaian rentang warna skema Hue Gradien** — Gradien Hue tidak lagi linier penuh `hue = level × 2` (0–200°) melainkan dibagi 3 segmen: **0–20%**: hue tetap `1°`, saturasi 70% (merah muda); **21–50%**: hue linear `2° → 100°` (kuning → hijau) dengan saturasi tetap 70%; **51–100%**: hue linear `102° → 260°` (hijau → teal → biru) dengan saturasi naik linear `71% → 100%`. Brightness tetap 100% di semua segmen.
- **Section animasi Battery Bar dipecah menjadi dua** — Section **"Efek dan Animasi"** dipecah menjadi dua chevron: **"Animasi Pengisian Daya"** (id `batbar_sectionChargeHeader`/`batbar_sectionCharge`) berisi kontrol animasi saat charging (Animasi Shine, Kecepatan Shine, Lebar Band, Wave saat charging), dan **"Animasi Baterai Rendah"** (id `batbar_sectionLowAnimHeader`/`batbar_sectionLowAnim`) berisi kontrol terkait baterai rendah (Warna Low, Ambang Low, Kecepatan Fade, Animasi Wave + Kecepatan & Intensitas Wave). Penamaan section kini mencerminkan isi, tidak lagi mengelompokkan semua efek dalam satu section.
- **Kontrol Warna Low dinonaktifkan saat skema warna aktif** — Saat skema **Klasik 3-warna** atau **Hue Gradien** dipilih, pemilih **Warna Low** (preview + label) diburamkan (alpha 30%) dan tidak bisa diklik, karena warna low tidak dipakai oleh skema (skema sudah memetakan warna merah di level rendah). Label berubah jadi **"Warna Low (tidak aktif saat skema)"**. Warna low kembali aktif saat skema **Tanpa Skema** dipilih.
- **Arah gelombang Animasi Wave saat charging dibalik** — Gelombang sinus yang menjalar pada animasi **Wave saat charging** sebelumnya bergerak berlawanan arah pengisian (misal horizontal bergerak kanan→kiri); kini arah offset dibalik sehingga gelombang menjalar **searah pengisian** (horizontal kiri→kanan, vertikal menyesuaikan).
- **Default animasi charging & baterai rendah jadi nonaktif** — Semua animasi efek di Battery Bar sekarang **default OFF** (harus diaktifkan manual): **Animasi Shine** (`shineEnabled`), **Wave saat charging** (`chargeWaveEnabled`), **Animasi Fade** (`fadeEnabled`), dan **Wave saat low** (`waveEnabled`). Mencegah animasi saling menimpa satu sama lain saat aplikasi pertama kali digunakan.
### 🔧 Optimasi & Penyesuaian
- **Slider Kecepatan Fade & Shine memakai satuan detik** — Kedua slider animasi (Fade & Shine) diubah ke satuan **detik** dengan rentang **0,2–2,0 detik** dan **step 0,1 detik** per geseran (sebelumnya Fade memakai skala abstrak 1–20, Shine memakai 400–4000ms — sulit memilih nilai presisi). Nilai disimpan internal sebagai durasi ms (200–2000). Preset lama dengan `fadeSpeed` skala 1–20 akan di-clamp ke rentang baru (nilai <200ms menjadi 200ms).
### 🐞 Bug Fixes
- **Kontrol "Animasi Wave" saat charging & "Animasi Fade" tidak berfungsi** — Checkbox **Animasi Wave (gelombang mengalir ke kanan)** di section **Animasi Pengisian Daya** dan checkbox **Animasi Fade** di section **Animasi Baterai Rendah** hanya dideklarasikan sebagai field di `BatteryBarPanelController` tanpa binding, load, dan listener — sehingga tampil di panel tapi tidak membaca nilai tersimpan maupun menyimpan perubahan. Kini keduanya di-bind, di-load, dan diberi listener lengkap: checkbox + slider Kecepatan/Intensitas Wave charging tersimpan di prefs `batbar_charge_wave_enabled`/`batbar_charge_wave_speed`/`batbar_charge_wave_amplitude`, checkbox Animasi Fade tersimpan di `batbar_fade_enabled`. Keduanya ikut serta dalam preset (`chargeWaveEnabled`/`chargeWaveSpeed`/`chargeWaveAmplitude` + `fadeEnabled`).
- **Arah animasi shine vertikal terbalik** — Pada bar vertikal, band shine sebelumnya menyapu dari **atas ke bawah** (`top = shinePos * h`), berlawanan dengan arah pengisian bar yang **dari bawah ke atas**. Sekarang posisi band dihitung dengan `(1f - shinePos) * h` sehingga menyapu dari **bawah ke atas**, searah dengan pengisian. Pemotongan segmen area terisi (alpha 100%) / kosong (alpha 40%) tetap sama.
- **Teks Floating Text kembali ke default "FunText" secara tiba-tiba** — `TextConfig.text` tidak pernah disimpan ke SharedPreferences saat diedit dan `editText` tidak pernah diisi ulang dari nilai tersimpan. Akibatnya saat Activity/panel dibuat ulang (rotasi layar, buka dokumentasi, atau proses di-recreate) atau service overlay di-restart, teks kembali ke nilai default. Sekarang teks **disimpan ke prefs `text_content`** setiap kali berubah (onTextChanged, saat overlay diaktifkan, autoStart, dan saat preset di-apply) serta **dimuat ulang** di `TextPanelController.loadConfig()` dan `MainActivity.loadShadowConfigs()` — EditText ikut diisi nilai tersimpan sehingga panel dan overlay konsisten.
- **Efek shine charging hanya terlihat di area bar kosong** — Band shine digambar dengan blending normal (putih 40% alpha) sehingga di atas warna fill terang (misal hijau) hampir tidak terlihat; efek hanya jelas di strip kosong. Sekarang band shine dipotong per segmen berdasarkan level: bagian yang menimpa area **terisi** digambar `PorterDuff.Mode.SCREEN` alpha **100%** (putih pekat, jelas di atas warna fill), sedangkan bagian di area **kosong** tetap alpha 40%. Shine tampak menyapu seluruh bar dari awal sampai akhir dengan intensitas berbeda per area.
### 🗒️ File Added
- `app/src/main/res/drawable/bg_segment_container.xml` — Background container segment button (rounded, `#444444`)
- `app/src/main/res/drawable/bg_segment_active.xml` — Background tombol segment aktif (rounded, `#4A90D9`)
- `app/src/main/res/drawable/bg_segment_inactive.xml` — Background tombol segment non-aktif (transparan, rounded)
### ✏️ File Changed
- `app/src/main/java/exp/ftxt/features/battery_bar/BatteryBarConfig.java` — Field `invert` (arah pengisian dibalik); ganti `autoColor` → `colorScheme` (konstanta `SCHEME_NONE`/`SCHEME_CLASSIC`/`SCHEME_HUE`) + helper `isAutoColor()`; field `shineEnabled`/`shineSpeed`/`shineWidth` (kontrol animasi shine); `fadeSpeed` diubah makna jadi durasi ms (default 1800); tambah field `waveEnabled`/`waveSpeed`/`waveAmplitude`; default animasi (`fadeEnabled`/`shineEnabled`/`waveEnabled`/`chargeWaveEnabled`) jadi `false`, default `safeArea` tetap `true`
- `app/src/main/java/exp/ftxt/features/battery_bar/BatteryBarView.java` — Method `setInvert()`; logika fill & band shine dibalik saat invert (horizontal isi kanan→kiri, vertikal isi atas→bawah, shine searah); band shine dipotong per segmen sesuai level: area terisi `PorterDuff.Mode.SCREEN` alpha 100%, area kosong alpha 40%; arah shine vertikal dibalik menyapu bawah → atas (searah pengisian); render warna skema **Klasik** (ambang 20/10) & **Hue Gradien** 3 segmen (0–20% hue 1° S70%; 21–50% hue 2°→100° S70%; 51–100% hue 102°→260° S71%→100%); method `setShineConfig()` + durasi shine dinamis (`shineSpeed`) + lebar band dinamis (`shineWidthPercent`); `setFadeSpeed()`/`startFade()` memakai durasi langsung ms (clamp 200–2000); tambah `setWaveConfig()`; animator wave (0–1, durasi `waveSpeed`, INFINITE); render fill berlapis saat low: dasar alpha `fillAlpha×(1-amp)` + overlay segmen gelombang sinus menjalar (`drawWaveFill()`, 2 siklus, alpha boost per segmen, arah mengikuti orientasi & invert); `stopWave()` di `onDetachedFromWindow()`; arah gelombang wave charging dibalik (offset negatif, menjalar searah pengisian)
- `app/src/main/java/exp/ftxt/features/battery_bar/BatteryBarModule.java` — Teruskan `BatteryBarConfig.invert` & `colorScheme` ke view di `start()`/`applyAppearance()`/`reloadLayout()`; teruskan `shineEnabled`/`shineSpeed`/`shineWidth`; teruskan `setWaveConfig()` di `start()`/`applyAppearance()`; margin 8dp dihapus di Mode Cepat (margin 0, bar menempel penuh ke sisi)
- `app/src/main/java/exp/ftxt/ui/BatteryBarPanelController.java` — Switch pemilih mode diganti **segment button** (klik "Mode Cepat" = quickMode ON, klik "Manual" = quickMode OFF), hapus collapsible section Posisi, checkbox Kunci Posisi dinonaktifkan (disabled + alpha 30%, listener dihapus), CheckBox "Horizontal" diganti **RadioGroup Horizontal/Vertikal**, tambah **Checkbox Invert** (bind, load, listener `batbar_invert`), checkbox Warna Level diganti **tombol pemilih skema** (`batbarSchemeSelector` + popup Tanpa Skema/Klasik/Hue, simpan `batbar_color_scheme`); tambah binding & listener **Checkbox Shine** + **slider Kecepatan/Lebar Shine** (`batbar_shine_*`); pemilih **Warna Low** diburamkan + dinonaktifkan saat skema warna aktif (`updateLowColorEnabled()`, label "Warna Low (tidak aktif saat skema)"); slider Fade & Shine memakai satuan detik (`formatSec()`, nilai = 200 + progress×100); lengkapi bind + load + listener **Checkbox Animasi Fade** (`batbar_fade_enabled`) dan kontrol **Wave saat charging** (`batbarChargeWaveCheck`/`batbarChargeWaveSpeedSeekBar`/`batbarChargeWaveAmplitudeSeekBar` → prefs `batbar_charge_wave_*`); **Area Aman terkunci aktif di kedua mode** (`updateSafeAreaLock()`: paksa `safeArea=true` + simpan prefs, checkbox disabled + alpha 30%); binding + load + listener kontrol Wave (`batbar_wave_*`)
- `app/src/main/java/exp/ftxt/ui/BatteryBarPositionController.java` — Kontrol posisi diburamkan (alpha) saat dinonaktifkan di Mode Cepat; preset simpan/terapkan `barInvert` + `barColorScheme` (fallback `autoColor` preset lama) + `shineEnabled`/`shineSpeed`/`shineWidth` + `waveEnabled`/`waveSpeed`/`waveAmplitude` + `chargeWaveEnabled`/`chargeWaveSpeed`/`chargeWaveAmplitude` + `fadeEnabled`; preset apply selalu paksa `safeArea` `true`
- `app/src/main/java/exp/ftxt/ui/TextPanelController.java` — Muat teks tersimpan ke `editText` + `TextConfig.text` di `loadConfig()`, simpan teks ke prefs `text_content` saat diedit / overlay aktif / autoStart
- `app/src/main/java/exp/ftxt/ui/TextPositionController.java` — Simpan `text_content` ke prefs saat preset teks di-apply
- `app/src/main/java/exp/ftxt/MainActivity.java` — Muat `TextConfig.text` dari prefs `text_content` di `loadShadowConfigs()`; muat prefs `batbar_invert` ke `BatteryBarConfig.invert`; load `batbar_color_scheme` dengan migrasi otomatis dari `batbar_auto_color` lama; load prefs `batbar_shine_enabled`/`batbar_shine_speed`/`batbar_shine_width`; default `batbar_fade_speed` diubah ke 1800; load prefs `batbar_wave_enabled`/`batbar_wave_speed`/`batbar_wave_amplitude`; default load prefs animasi jadi `false`; `safeArea` selalu dipaksa `true`
- `app/src/main/java/exp/ftxt/shared/preset/OverlayPreset.java` — Field `barInvert` + `barColorScheme` untuk preset Battery Bar + `shineEnabled`/`shineSpeed`/`shineWidth` + `waveEnabled`/`waveSpeed`/`waveAmplitude`
- `app/src/main/res/layout/panel_battery_bar.xml` — Baris switch label dua sisi "Mode Cepat [switch] Manual" diganti **segment button** dua tombol, chevron Posisi dihapus, kontrol posisi dipindah ke bawah panjang bar, checkbox Kunci Posisi disabled + alpha 30% dengan label "(tidak tersedia)", CheckBox "Horizontal" diganti **RadioGroup Horizontal/Vertikal**, **Checkbox Invert** diletakkan sebaris di samping opsi Vertikal, header section **"Baterai Rendah" → "Efek dan Animasi"**, checkbox **Warna Level** diganti **label + tombol pemilih skema** (`batbarColorSchemeLabel` + `batbarSchemeSelector`); tambah kontrol **Animasi Shine** di section Efek dan Animasi (`batbarShineCheck`, `batbarShineSpeedLabel`+`batbarShineSpeedSeekBar` 0,2–2,0 dtk, `batbarShineWidthLabel`+`batbarShineWidthSeekBar` 2–98%); slider `batbarFadeSpeedSeekBar` & `batbarShineSpeedSeekBar` max 18 (0,2–2,0 detik step 0,1); tambah id `batbarLowColorLabel` untuk label Warna Low; checkbox `batbarSafeAreaCheck` dipindah dari section Mode Manual ke section Tampilan sebaris dengan `batbarShowEmptyStripCheck` (label "Area Aman"); tambah kontrol **Animasi Wave** di section **Animasi Baterai Rendah** (`batbarWaveCheck`, `batbarWaveSpeedLabel`+`batbarWaveSpeedSeekBar` 0,2–2,0 dtk, `batbarWaveAmplitudeLabel`+`batbarWaveAmplitudeSeekBar` 10–100%); tambah kontrol **Wave saat charging** di section **Animasi Pengisian Daya** (`batbarChargeWaveCheck`, `batbarChargeWaveSpeedLabel`+`batbarChargeWaveSpeedSeekBar` 0,2–2,0 dtk, `batbarChargeWaveAmplitudeLabel`+`batbarChargeWaveAmplitudeSeekBar` 10–100%) + checkbox **Animasi Fade** (`batbarFadeCheck`); section **"Efek dan Animasi"** dipecah jadi dua chevron: **"Animasi Pengisian Daya"** (`batbar_sectionChargeHeader`/`batbar_sectionCharge` — shine check + kecepatan + lebar band + charge wave) dan **"Animasi Baterai Rendah"** (`batbar_sectionLowAnimHeader`/`batbar_sectionLowAnim` — warna low, ambang low, fade, wave)

---

# [4.85.1] - 2026-08-11 versionCode 178
### ✨ Fitur Baru
- **Modul Battery Bar** — Modul overlay baru `features/battery_bar` (BatteryBarConfig + BatteryBarView + BatteryBarModule) menampilkan bar baterai sebagai strip di layar. Dua mode: **Mode Cepat** (snap ke sisi atas/bawah/kiri/kanan dengan panjang penuh sisi, pilih sisi lewat popup) dan **Mode Manual** (panjang 0–100% + posisi bebas per orientasi `_port`/`_land`). Orientasi bar (horizontal/vertikal) otomatis mengikuti mode. Fitur panel: ketebalan, warna fill, strip kosong + warna, radius sudut, auto-color (hijau→kuning→merah mengikuti level), warna low + ambang low (fade berkedip saat rendah), kecepatan fade, animasi shine saat charging, shadow, kunci posisi (touch passthrough), area aman, dan preset khusus `moduleType "battery_bar"` dengan field bar tersendiri.
### ♻️ Perubahan Fitur
- **Preset terpisah per modul** — `OverlayPreset` kini punya field `moduleType` (text, fps, clock, battery, batcur, network). Setiap `PositionController` mengisi `moduleType` saat menyimpan preset. Browser preset (`PresetBrowserDialog`) dan `PresetManager.getAllNames/getIndexMetadata` memfilter daftar preset per panel, sehingga preset dari modul lain tidak lagi muncul/tumpang tindih di panel berbeda. Preset lama tanpa `moduleType` tetap tampil di semua panel (backward compatible) dan ditolak saat di-apply ke modul yang salah.
- **Guard lintas modul saat apply preset** — `PresetHandler.applyPreset()` menolak preset yang `moduleType`-nya tidak cocok dengan panel aktif (toast "Preset ini untuk modul ..."), mencegah konfigurasi modul lain menimpa modul aktif. Tidak ada lagi "preset tidak sengaja mengubah semua overlay".
- **Posisi preset dihormati per orientasi** — Saat apply preset, posisi kini ditulis ke prefs orientasi yang tersimpan di preset (`_land`/`_port`), bukan selalu orientasi layar saat ini. Format orientasi preset disamakan menjadi `land`/`port` (sebelumnya `landscape`/`portrait`), dengan normalisasi otomatis untuk preset lama.
### 🔧 Optimasi & Penyesuaian
- **Filter preset per panel di dialog** — `PresetBrowserDialog` menerima `moduleType` dari `PresetHandler.Delegate.moduleType()` dan hanya menampilkan preset milik modul tersebut (plus preset lama tanpa modul). Metadata index preset menyimpan `moduleType` agar filter tanpa harus me-load seluruh payload.
### 🐞 Bug Fixes
- **Item sidebar modul baru tidak muncul** — Sidebar dimuat dari state tersimpan (`sidebar_state`) yang belum berisi item baru seperti `navBatteryBar`, sehingga "Battery Bar" tidak tampil walau sudah ada di `DEFAULT_SIDEBAR_JSON`. Sekarang `parseSidebarJson()` menggabungkan (merge) item default yang belum ada di state tersimpan, jadi setiap modul baru otomatis muncul di drawer walau sidebar pernah di-reorder.
- **Posisi overlay reset ke default setelah service restart / kill service** — Modul Clock, Battery Stats, Battery Current, dan Network tidak membaca posisi tersimpan (`*_pos_x/_port`, `*_pos_y/_port`, dan varian `_land`) saat modul dibuat (`init()`), sehingga memakai nilai default Config sampai panel modul dibuka di activity. Sekarang keempat modul memanggil `loadPosition()` di `init()`, konsisten dengan pola Text & FPS.
- **Posisi salah saat rotasi layar (reload pakai nilai orientasi lama)** — `reloadAllPositions()` di `FloatingService` sebelumnya hanya `setOrientationSuffix(null)` + `updatePosition()` yang memakai `Config.posX/posY` dari orientasi lama. Sekarang `reloadAllPositions()` memanggil `reloadPosition()` per modul yang membaca ulang posisi dari prefs sesuai orientasi baru (`_land`/`_port`).
### 🗒️ File Added
- `app/src/main/java/exp/ftxt/features/battery_bar/BatteryBarConfig.java` — Config modul (enabled, quickMode, quickSide, horizontal, length, thickness, color, autoColor, lowColor, lowThreshold, showEmptyStrip, emptyColor, radius, fadeSpeed, shadow, touchPassthrough, safeArea, posX, posY, updateInterval)
- `app/src/main/java/exp/ftxt/features/battery_bar/BatteryBarView.java` — Custom View rendering bar horizontal/vertikal + empty strip + fade saat low + shine saat charging
- `app/src/main/java/exp/ftxt/features/battery_bar/BatteryBarModule.java` — Modul overlay (quick snap & manual position, drag handler, tick update interval, baca level/status via `ACTION_BATTERY_CHANGED`)
- `app/src/main/java/exp/ftxt/ui/BatteryBarPanelController.java` — Controller panel Battery Bar
- `app/src/main/java/exp/ftxt/ui/BatteryBarPositionController.java` — Controller posisi + preset (`moduleType "battery_bar"`)
- `app/src/main/java/exp/ftxt/ui/fragment/BatteryBarPanelFragment.java` — Fragment panel
- `app/src/main/res/layout/panel_battery_bar.xml` — Layout panel
### ✏️ File Changed
- `app/src/main/java/exp/ftxt/core/FloatingService.java` — `reloadAllPositions()` panggil `module.reloadPosition()`; field/getter/`ensureBatteryBarModule()`/start modul Battery Bar
- `app/src/main/java/exp/ftxt/MainActivity.java` — Load prefs `batbar_*`, import `BatteryBarConfig`, item sidebar `navBatteryBar`, `panelIdToName` → `battery_bar`, judul toolbar, `isAnyModuleActive()` cek `BatteryBarConfig`, merge item default yang hilang ke sidebar tersimpan (`addMissingDefaultItems()`)
- `app/src/main/java/exp/ftxt/shared/preset/OverlayPreset.java` — Tambah field `moduleType` + field preset Battery Bar (quickMode, quickSide, barHorizontal, barLength, barThickness, autoColor, lowColor, lowThreshold, showEmptyStrip, emptyColor, barRadius, fadeSpeed)
- `app/src/main/java/exp/ftxt/shared/preset/PresetHandler.java` — Method `moduleType()` di interface `Delegate`; simpan orientasi preset `land`/`port`; hormati `preset.orientation` saat apply + normalisasi orientasi lama; guard lintas modul; teruskan `moduleType` ke `PresetBrowserDialog`
- `app/src/main/java/exp/ftxt/shared/preset/PresetManager.java` — Field `moduleType` di `PresetIndexItem`; `getAllNames()`/`getIndexMetadata()` overload dengan filter `moduleType`
- `app/src/main/java/exp/ftxt/shared/preset/PresetBrowserDialog.java` — Parameter `moduleType` (overload konstruktor) + filter daftar preset
- `app/src/main/java/exp/ftxt/ui/TextPositionController.java` — Implement `moduleType()` ("text") + `p.moduleType` di `saveToPreset()`
- `app/src/main/java/exp/ftxt/ui/FpsPositionController.java` — Implement `moduleType()` ("fps") + `p.moduleType` di `saveToPreset()`
- `app/src/main/java/exp/ftxt/ui/ClockPositionController.java` — Implement `moduleType()` ("clock") + `p.moduleType` di `saveToPreset()`
- `app/src/main/java/exp/ftxt/ui/BatteryPositionController.java` — Implement `moduleType()` ("battery") + `p.moduleType` di `saveToPreset()`
- `app/src/main/java/exp/ftxt/ui/BatteryCurrentPositionController.java` — Implement `moduleType()` ("batcur") + `p.moduleType` di `saveToPreset()`
- `app/src/main/java/exp/ftxt/ui/NetworkPositionController.java` — Implement `moduleType()` ("network") + `p.moduleType` di `saveToPreset()`
- `app/src/main/java/exp/ftxt/features/clock_module/ClockModule.java` — Panggil `loadPosition()` di `init()`
- `app/src/main/java/exp/ftxt/features/battery_stats/BatteryStatsModule.java` — Panggil `loadPosition()` di `init()`
- `app/src/main/java/exp/ftxt/features/battery_current/BatteryCurrentModule.java` — Panggil `loadPosition()` di `init()`
- `app/src/main/java/exp/ftxt/features/network_stats/NetworkModule.java` — Panggil `loadPosition()` di `init()`
- `app/src/main/java/exp/ftxt/ui/PanelManager.java` — Daftarkan `battery_bar` → `BatteryBarPanelFragment`
- `app/src/main/res/values/ids.xml` + `values/strings.xml` + `menu/drawer_menu.xml` — Item `nav_battery_bar` / string `nav_battery_bar`
- `app/src/main/java/exp/ftxt/core/BootReceiver.java` — Load `batbar_enabled` + cek aktif
- `app/src/main/java/exp/ftxt/core/NotificationHelper.java` — Cek aktif & label "Bar"
- `app/src/main/java/exp/ftxt/shared/ui/OverlayShadow.java` — Parameter `View` (sebelumnya `TextView`) agar cocok dengan BatteryBarView

---

# [4.85.0] - 2026-08-04 versionCode 177
### ✨ Fitur Baru
- **Battery Stats — suhu & persentase jadi satu kesatuan modul** — Modul `features/battery_stats` (BatteryStatsConfig + BatteryStatsModule) menggabungkan komponen suhu (°C) dan persentase (%) menjadi satu modul utuh, persis pola Battery Current yang menggabungkan tegangan/arus/daya. Satu overlay, satu panel, satu konfigurasi (warna, label, shadow, background, posisi, safe area, interval 0.2–10 detik), satu preset, satu key prefs (`battery_*`). Checkbox **°C** dan **%** di panel mengontrol komponen yang tampil; keduanya bisa tampil bersamaan dalam satu overlay.
### 🚮 Fitur Dihapus
- **Modul Battery Percentage terpisah dihapus** — Seluruh lapisan `features/battery_percentage` (BatteryPercentageConfig, BatteryPercentageModule), `BatteryPercentagePanelController`, `BatteryPercentagePositionController`, `BatteryPercentagePanelFragment`, dan `panel_battery_percentage.xml` dihapus total. Fungsinya kini menjadi bagian dari modul Battery Stats.
- **Modul Battery Temperature dihapus** — Seluruh lapisan `features/battery_temperature` (BatteryConfig, BatteryModule) dihapus, digantikan modul Battery Stats yang meneruskan perilakunya.
### ♻️ Perubahan Fitur
- **Desimal suhu hanya tampil saat data tersedia** — Nilai mentah `EXTRA_TEMPERATURE` (0,1°C) ditampilkan tanpa desimal (misal `37°C`) saat kelipatan 10, dan dengan 1 desimal (misal `37.4°C`) saat sensor mengirim nilai non-kelipatan-10. Overlay tetap bersih di perangkat yang sensornya hanya melaporkan step 1°C. Berlaku untuk mode label maupun value-only.
- **Pemisah "|" antara suhu dan persentase** — Saat komponen suhu dan persentase tampil bersamaan, keduanya kini dipisahkan tanda `|` (misal `37.4°C | 87%`), menggantikan pemisah spasi.
- **Warna pemisah bisa diatur sendiri** — Tombol warna "Pemisah" baru di panel Battery Stats (di samping Warna, Label, Shadow, Background). Pemisah `|` kini punya warna terpisah (`separatorColor`, default abu-abu) yang tersimpan di prefs `battery_separator_color` dan ikut serta dalam preset.
- **Pemisah "|" + warna di Battery Current** — Pemisah antar komponen tegangan/arus/daya diganti dari spasi menjadi `|` (misal `4.1V | +120mA | 0.5W`), konsisten dengan Battery Stats. Tombol warna "Pemisah" baru di panel Battery Current (`separatorColor`, default abu-abu), tersimpan di prefs `batcur_separator_color` dan ikut serta dalam preset.
### 🐞 Bug Fixes
- **Panel Battery Percentage tidak bisa diakses dari sidebar** — `battery_pct` terdaftar di `PanelManager` tanpa id sidebar (`navBatteryPercentage`), sehingga tidak pernah bisa diakses dari navigasi. Karena suhu & persen kini digabung menjadi satu panel "Battery Stats" (`navBattery`), panel duplikat `battery_pct` dihapus total — penyebab bug hilang bersama modulnya.
### 🗒️ File Added
- `app/src/main/java/exp/ftxt/features/battery_stats/BatteryStatsConfig.java` — Config modul gabungan (enabled, size, color, labelColor, shadow, bg, safeArea, showTemperature, showPercentage, showOnlyValue, posX, posY, updateInterval)
- `app/src/main/java/exp/ftxt/features/battery_stats/BatteryStatsModule.java` — Modul overlay satu kesatuan: tampilkan suhu °C dan/atau persen %, update per interval, dukungan safe area & drag
- `app/src/debug/res/values/strings.xml` — Resource `app_name` "FTxTdebug" untuk build type debug
### ✏️ File Changed
- `app/src/main/java/exp/ftxt/features/battery_stats/BatteryStatsModule.java` — Format suhu dinamis: integer tanpa desimal saat nilai kelipatan 10, 1 desimal `%.1f°C` saat non-kelipatan-10
- `app/src/main/java/exp/ftxt/features/battery_stats/BatteryStatsConfig.java` — Tambah field `separatorColor` (default abu-abu)
- `app/src/main/java/exp/ftxt/features/battery_stats/BatteryStatsModule.java` — Render pemisah `|` dengan warna `separatorColor` + method `updateSeparatorColor()`
- `app/src/main/java/exp/ftxt/shared/ui/OverlayModule.java` — Tambah default method `updateSeparatorColor(int)`
- `app/src/main/java/exp/ftxt/core/FloatingService.java` — Tambah `updateSeparatorColorForModule()`
- `app/src/main/java/exp/ftxt/MainActivity.java` — Load prefs `battery_separator_color`
- `app/src/main/java/exp/ftxt/shared/preset/OverlayPreset.java` — Tambah field `separatorColor`
- `app/src/main/java/exp/ftxt/ui/BatteryPositionController.java` — Preset save/apply + sync warna pemisah
- `app/src/main/java/exp/ftxt/ui/BatteryPanelController.java` — Binding & listener tombol warna pemisah
- `app/src/main/res/layout/panel_battery.xml` — Tombol warna "Pemisah"
- `app/src/main/java/exp/ftxt/features/battery_current/BatteryCurrentConfig.java` — Tambah field `separatorColor` (default abu-abu)
- `app/src/main/java/exp/ftxt/features/battery_current/BatteryCurrentModule.java` — Pemisah `|` antar tegangan/arus/daya + render warna `separatorColor` + method `updateSeparatorColor()`
- `app/src/main/java/exp/ftxt/MainActivity.java` — Load prefs `batcur_separator_color`
- `app/src/main/java/exp/ftxt/ui/BatteryCurrentPositionController.java` — Preset save/apply + sync warna pemisah
- `app/src/main/java/exp/ftxt/ui/BatteryCurrentPanelController.java` — Binding & listener tombol warna pemisah
- `app/src/main/res/layout/panel_battery_current.xml` — Tombol warna "Pemisah"
- `app/src/main/java/exp/ftxt/features/battery_stats/BatteryStatsModule.java` — Pemisah `|` antara suhu & persentase (ganti spasi)
- `app/src/main/java/exp/ftxt/core/FloatingService.java` — Ganti `batteryModule` + `batteryPercentageModule` menjadi satu `batteryStatsModule` (`batteryStatsModule()`, `ensureBatteryStatsModule()`)
- `app/src/main/java/exp/ftxt/MainActivity.java` — Import & `isAnyModuleActive()` pakai `BatteryStatsConfig`; `loadShadowConfigs()`: blok `battpct_*` dihapus, blok `BatteryConfig` → `BatteryStatsConfig` + lengkapi load `size`, `bg.offsetX/Y`, `bg.margin`, `bg.radius`
- `app/src/main/java/exp/ftxt/core/BootReceiver.java` — Cek aktif pakai `BatteryStatsConfig`
- `app/src/main/java/exp/ftxt/core/NotificationHelper.java` — Cek aktif & label notifikasi pakai `BatteryStatsConfig` (label "Battery")
- `app/src/main/java/exp/ftxt/ui/PanelManager.java` — Hapus entry `battery_pct` dari `panelMap` + import `BatteryPercentagePanelFragment`
- `app/src/main/java/exp/ftxt/ui/BatteryPanelController.java` — `BatteryConfig` → `BatteryStatsConfig`, `batteryModule()` → `batteryStatsModule()`
- `app/src/main/java/exp/ftxt/ui/BatteryPositionController.java` — `BatteryConfig`/`BatteryModule` → `BatteryStatsConfig`/`BatteryStatsModule`, `moduleLabel` → "Battery Stats"
- `app/src/main/res/layout/panel_battery.xml` — Label checkbox modul "Suhu Baterai" → "Battery Stats"
- `app/src/main/res/values/strings.xml` — Hapus string `nav_battery_percentage`
- `app/src/main/res/menu/drawer_menu.xml` — Hapus item `nav_battery_percentage`
### 🔥 File Removed
- `app/src/main/java/exp/ftxt/features/battery_temperature/BatteryConfig.java`
- `app/src/main/java/exp/ftxt/features/battery_temperature/BatteryModule.java`
- `app/src/main/java/exp/ftxt/features/battery_percentage/BatteryPercentageConfig.java`
- `app/src/main/java/exp/ftxt/features/battery_percentage/BatteryPercentageModule.java`
- `app/src/main/java/exp/ftxt/ui/BatteryPercentagePanelController.java`
- `app/src/main/java/exp/ftxt/ui/BatteryPercentagePositionController.java`
- `app/src/main/java/exp/ftxt/ui/fragment/BatteryPercentagePanelFragment.java`
- `app/src/main/res/layout/panel_battery_percentage.xml`

---

# [4.84.1] - 2026-08-04 versionCode 176
### 🔧 Optimasi & Penyesuaian
- **Hapus dead code konstruktor lama `(Activity)`** — Semua PositionController & PanelController punya konstruktor lama yang sudah tidak dipakai setelah refactor ke fragment. Konstruktor lama berpotensi NPE karena `findViewById(android.R.id.content)` mengembalikan `null`. 15 konstruktor + 13 method `bindViews()` tanpa parameter dihapus dari 15 file controller.
- **Hapus import `CheckBox` ganda** — Import duplikat dihapus dari `FpsPanelController.java` dan `BatteryPercentagePanelController.java`.
### 🐞 Bug Fixes
- **Layar panel kosong saat start** — Saat nilai `nav_selected_item` tidak dikenal, aplikasi tidak menampilkan panel apa pun. Sekarang ada fallback: jika panel tidak ditemukan di `panelIdToName()`, default ke panel **Floating Text** + `R.id.navFloatingText`.
- **Panel Crosshair & Logo tidak bisa diakses** — `navCrosshair` dan `navLogo` tidak terdaftar di `panelIdToName()` sehingga klik sidebar tidak menampilkan panel apa pun. Sekarang keduanya dipetakan ke panel `crosshair` / `logo` + judul toolbar yang benar.
- **Race condition PanelManager.showPanel()** — Transaksi fragment berjalan async sehingga klik cepat antar panel bisa menumpuk transaksi (panel salah tampil). Sekarang `executePendingTransactions()` dipanggil sebelum iterasi, `setReorderingAllowed(true)`, dan hanya fragment yang `isAdded()` & tidak `isHidden()` yang di-hide.
- **"Muat Preset" dari menu gear tidak muncul** — Fragment-fragment panel tidak meng-override `showLoadPresetDialog()` sehingga dialog preset tidak pernah ditampilkan. Semua fragment panel kini meneruskan panggilan ke controller masing-masing.
- **UI panel tidak di-refresh** — `onPanelShown()` tidak dipanggil setelah panel ditampilkan sehingga kontrol tidak sinkron saat pindah panel. PanelManager kini memanggil `onPanelShown()` via `runOnCommit` setiap panel ditampilkan, dan semua fragment meneruskan panggilan ke controller.
- **Crash `BadForegroundServiceNotificationException` — `ImageButton doesn't have method: setText`** — `setTextViewText(R.id.noti_title, ...)` dihapus dari `buildNotificationDynamic()`. Teks suhu baterai tetap tampil via `setContentTitle()` di header notifikasi (`DecoratedCustomViewStyle`), jadi tidak ada lagi perintah `setText` pada RemoteViews yang berisiko menarget view tipe salah.
- **Panel kosong saat memilih modul di sidebar** — Semua layout panel (kecuali `panel_text.xml`) masih membawa `android:visibility="gone"` di root (sisa era View-visibility manual). Setelah refactor ke fragment, tidak ada lagi `setVisibility(VISIBLE)` manual sehingga panel yang dipilih tampil kosong. Sekarang atribut `gone` di root dihapus dari 9 layout panel; visibilitas dikelola sepenuhnya oleh FragmentTransaction.
- **Posisi overlay reset ke default setelah Kill Service / Tutup Aplikasi** — `System.exit(0)` di `NotificationActionReceiver.handleKillService()` dan `MainActivity.forceClose()` membunuh proses secara paksa sebelum `SharedPreferences.apply()` (async) selesai di-flush ke disk, sehingga posisi yang baru disesuaikan hilang dan overlay kembali ke default (0.5/0.8) saat service start ulang. `System.exit(0)` dihapus dari kedua lokasi — `stopService`/`finishAffinity` sudah cukup; data prefs aman dan posisi dipertahankan.
### 🗒️ File Added
- `app/src/main/java/exp/ftxt/ui/fragment/CrosshairPanelFragment.java` — Fragment untuk panel Crosshair (placeholder)
- `app/src/main/java/exp/ftxt/ui/fragment/LogoPanelFragment.java` — Fragment untuk panel Logo (placeholder)
- `app/src/debug/res/mipmap-anydpi-v26/ic_launcher.xml` — Ikon launcher alternatif untuk build debug
### ✏️ File Changed
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

---

# [4.84.0] - 2026-07-28 versionCode 175
### ✨ Fitur Baru
- **Tanggal di Bawah Jam — Tampilan dua baris** — Clock module sekarang menampilkan tanggal di bawah jam dalam format dua baris (`HH:mm:ss` di atas, `MMM dd EE` di bawah). Diaktifkan secara default. Bisa di-toggle via panel jam (checkbox "Tanggal").
### ♻️ Perubahan Fitur
- **Refactor Panel Navigation — Fragment-based** — Ubah sistem navigasi panel dari View visibility manual ke Fragment-based. Setiap panel (text, fps, clock, battery, battery_pct, battery_cur, network, color_picker) punya Fragment sendiri + PanelManager untuk mengelola show/hide. Semua PanelController & PositionController mendapat overload konstruktor dengan `(Activity, View rootView)` untuk binding di Fragment. bindViews() lama delegasi ke bindViews(rootView). MainActivity di-refactor: hapus semua field View panel, field controller, hideAllPanels(), dan if-else visibility — ganti dengan PanelManager.
### ✏️ File Changed
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

---

# [4.83.2] - 2026-07-28 versionCode 174
### ✨ Fitur Baru
- **Ikon Notifikasi Dinamis — nilai suhu baterai di status bar** — Ikon notifikasi foreground service sekarang menampilkan nilai suhu baterai aktual (misal `37°`) yang di-generate secara dinamis sebagai Bitmap. Update setiap 10 detik. Title notifikasi juga menampilkan suhu (misal `FTxT 37°C`).
### 🔧 Optimasi & Penyesuaian
- **Optimasi Memori & Proses — Lazy Init, Conditional Resources** — Hemat memori dan baterai dengan 5 perubahan: (1) **Lazy Init Module** — 7 module overlay tidak lagi diinstansiasi semua di `onCreate()`. Module baru dibuat saat pertama diaktifkan. (2) **Cleanup Module saat Stop** — Null-kan `params` dan `choreographer` saat module di-stop agar bisa di-GC. (3) **Conditional WakeLock** — WakeLock hanya diambil jika ada module yang aktif. Saat semua overlay mati, CPU bisa tidur. (4) **Conditional BroadcastReceiver** — Receiver `CONFIG_CHANGED` hanya aktif saat ada overlay berjalan. (5) **Conditional Service Stop** — Service otomatis `stopSelf()` saat module terakhir di-stop.
### ✏️ File Changed
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

---

# [4.83.1] - 2026-07-28
### 🔧 Optimasi & Penyesuaian
- **Refactor FloatingService — Hapus Duplikasi Kode (Langkah 1-4)** — Buat interface `OverlayModule` untuk menyeragamkan semua modul overlay. Implement di 7 modul. Hapus ~430 baris static delegates di FloatingService, ganti dengan method generik berbasis loop (`startModule`, `stopModule`, `updateColorForModule`, `updateSizeForModule`, dll). FloatingService turun dari 785 → 351 baris (-55%). Update semua 14 UI controllers (7 PanelController + 7 PositionController) untuk pakai method generik baru.
### 🗒️ File Added
- `app/src/main/java/exp/ftxt/shared/ui/OverlayModule.java` — Interface untuk menyeragamkan method semua modul overlay
### ✏️ File Changed
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

---

# [3.11.3.78.0 – 3.9.3.74.2] - 2026-06-12–13
### ✨ Fitur Baru
- **Mode Tandai & aksi batch preset** — Tombol Tandai/Tandai Semua, aksi batch (Hapus/Favorit/Bagikan/Ekspor), opsi Gunakan Preset, drag reorder.
- **Opsi Ganti Ikon Aplikasi** — Toggle Default/Alternatif di Konfigurasi.
- **Background tema gelap & terang** — Drawer, header, toolbar, layar utama punya background gambar sendiri per tema.
- **1.351 Color Names** — ColorNameResolver: 148 CSS + 254 Material + 949 XKCD colors.
### 🚮 Fitur Dihapus
- **Tombol Ekspor Semua di dialog preset** — Dihapus dari bottom bar browser preset.
- **Tombol Simpan/Muat Preset dari panel posisi** — Semua 8 panel: tombol Simpan/Muat/label preset dihapus. Fungsi via gear → "Muat Preset".
### ♻️ Perubahan Fitur
- **Dialog preset terpusat** — PresetBrowserDialog punya tombol Simpan, title di XML header, preset baru di urutan teratas.
- **Tutup Aplikasi pindah nav drawer** — Kill Service + Keluar di drawer; Konfirmasi Keluar jadi default.
### 🐞 Bug Fixes
- **Preset baru tidak muncul** — `onSaveClick` async diperbaiki dengan `Consumer<Runnable>` callback.
- **Overlay tidak restart setelah Kill Service** — `isAnyModuleActive()` sekarang cek semua modul.

---

# [3.9.3.74.1 – 3.9.3.72.0] - 2026-06-12
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

---

# [3.9.3.71.0 – 3.9.3.69.6] - 2026-06-11–12
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

---

# [3.9.3.69.5 – 3.9.3.69.1] - 2026-06-11
### ♻️ Perubahan Fitur
- **Collapsible Section Grouping** — Semua 8 panel: section Tampilan, Posisi, Shadow, Background collapsible dengan SectionHelper.
- **release.yml decode keystore** — Tambah step decode & mkdir untuk CI.
### 🔧 Optimasi & Penyesuaian
- **Refactor layout ekstrak panel** — Pisahkan 8 panel dari `activity_main.xml` ke file `<include>` terpisah.
### 🐞 Bug Fixes
- **release.yml restore** — Kembalikan workflow ke versi kerja (Java 17, secret names benar).
### 🗒️ File Added
- `panel_text.xml`, `panel_fps.xml`, `panel_clock.xml`, `panel_battery_current.xml`, `panel_network.xml`, `panel_crosshair.xml`, `panel_watermark.xml`, `panel_logo.xml`
- `SectionHelper.java`, `key/.gitkeep`

---

# [3.9.3.69.0 – 3.9.3.67.0] - 2026-06-01–02
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

---

# [3.9.3.66.0 – 2.6.2.48.0] - 2026-05-16–06-01
### ✨ Fitur Baru
- **Preset System v2** — UUID-based index, metadata (tags, favorite, timestamps), thumbnail generation, version history (10), partial-apply API, search/filter, share intent, file picker import.
- **Safe Area** — Checkbox "Gunakan Area Aman" di 6 panel overlay.
- **Battery Percentage & Battery Current Overlay** — Dua modul baru: persentase baterai dan tegangan/arus/daya.
- **Network Speed Meter** — Kecepatan internet real-time ↓↑ dengan kontrol posisi.
- **Jam Digital & Suhu Baterai Overlay** — Waktu 24 jam HH:mm:ss dan suhu °C.
- **Preset GSON full-config** — Simpan/muat seluruh konfigurasi overlay via PresetManager.
- **Tombol Muat di Semua Panel** — Tombol "Muat Preset" fisik di layout setiap panel.
- **Auto-request izin saat pertama buka**.
### 🚮 Fitur Dihapus
- **Grid Posisi 3×3** — Dari FPS, Clock, Battery, Text.
- **Auto Preset Aplikasi** — Berdasarkan orientasi/aplikasi.
- **PositionPresetManager** — Digantikan PresetManager GSON.
- **Clipboard export/import**.
### ♻️ Perubahan Fitur
- **Restruktur folder features** — 9 folder di-rename (battery→battery_temperature, dll).
- **Tema default mode malam**.
- **Semua Switch → CheckBox**; overlay & kunci posisi sejajar horizontal.
- **Tombol orientasi pindah ke toolbar**, hapus dari panel posisi.
- **Export/Import clipboard → file**; Bagikan Preset di semua panel.
- **Battery Monitor → Battery Temperature**.
### 🐞 Bug Fixes
- **TextPositionController extra brace** — 28 error kompilasi.
- **Slider posisi tidak sinkron** — Saat drag.
- **Padding background mendorong teks** — Padding hanya saat bg enabled.

---

# [2.6.1.42.0 – 2.3.1.15.0] - 2026-05-16–06-01
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
### 🚮 Fitur Dihapus
- **XY Pad → karantina** — Digantikan Slider + D-Pad.
- **Sistem Grup Sidebar** — Flat list.
- **Hardcoded background** — Background dan shadow terpisah.
- **Shadow Opacity** — Alpha via color picker.
- **Module temp/** — Hapus folder deprecated.
### ♻️ Perubahan Fitur
- **modules/ → features/** — Refactor package structure.
- **Popup settings di bawah ikon** — PopupMenu + Gravity.END.
- **SettingsActivity → Konfigurasi** — Ringkas, hanya izin.
- **Dokumentasi via popup** — Dipindah dari Settings.
- **Offset range -60–60**; default shadow offset 0.
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

---

# 💡 Catatan
> Major 2 kebawah telah dipisahkan dari Project.
