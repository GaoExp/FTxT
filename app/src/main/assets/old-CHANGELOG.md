---

# [4.85.1–4.86.0] - 2026-08-19 03:20 WITA versionCode 183 ***PUSH***
### ✨ Fitur Baru
- **Modul Battery Strip** — Modul overlay baru `features/battery_bar` (BatteryBarConfig + BatteryBarView + BatteryBarModule) menampilkan bar baterai sebagai strip di layar. Dua mode: **Mode Cepat** (snap ke sisi atas/bawah/kiri/kanan dengan panjang penuh sisi, pemilihan sisi lewat popup) dan **Mode Manual** (panjang 0–100% + posisi bebas per orientasi `_port`/`_land`). Orientasi bar **Horizontal/Vertikal** dipilih lewat radio button. Konfigurasi panel: ketebalan, radius sudut, warna isi, strip kosong + warnanya, skema warna level, warna & ambang low, animasi efek, kontrol Invert, shadow, kunci posisi (touch passthrough), area aman, serta preset khusus `moduleType "battery_bar"` dengan field bar tersendiri.
- **Skema warna level Battery Strip** — Tombol pemilih skema di section Tampilan: **Tanpa Skema** (nonaktif), **Klasik 3-warna** (hijau >20%, kuning ≤20%, merah ≤10%), **Hue Gradien** (warna dihitung per segmen level: 0–20% merah muda; 21–50% kuning→hijau; 51–100% hijau→teal→biru dengan saturasi meningkat). Saat skema aktif, pemilih **Warna Low** dinonaktifkan ("Warna Low (tidak aktif saat skema)"). Tersimpan di prefs `batbar_color_scheme` (migrasi otomatis dari `batbar_auto_color` lama) dan ikut preset.
- **Animasi Battery Strip** — Empat efek, semuanya **default OFF**: **Animasi Shine** saat charging (slider Kecepatan 0,2–5,0 detik & Lebar Band 2–98% dari panjang bar), **Wave saat charging** (kedutan gelombang sinus yang menjalar searah pengisian; Kecepatan 0,2–5,0 detik & Intensitas 10–100%), **Animasi Fade** berkedip saat baterai rendah (Kecepatan 0,2–5,0 detik), dan **Animasi Wave** saat baterai rendah (gelombang 2 siklus menjalar sepanjang bar; Kecepatan & Intensitas sendiri). Kecepatan Shine dikalibrasi sama cepat dengan Wave pada nilai slider yang sama, dan perubahan kecepatan langsung diterapkan tanpa menunggu charging berhenti/mulai ulang. Semua nilai tersimpan di prefs `batbar_*` dan ikut preset.
- **Kontrol Invert arah pengisian Battery Strip** — Checkbox di sebelah opsi Vertikal; saat aktif arah pengisian dibalik (horizontal kanan→kiri, vertikal atas→bawah) dan animasi shine ikut searah pengisian. Tersimpan di prefs `batbar_invert` dan ikut preset (`barInvert`).
- **Slider Label Edit untuk panel Jam Digital, Battery Info, Network Speed & Battery Strip** — Klik label slider membuka dialog edit nilai manual seperti Floating Text & FPS; nilai offset mendukung angka negatif. Khusus Battery Strip: Kecepatan (Shine/Fade/Wave) diedit dalam **desimal detik** (0,2–5,0), Panjang/Ambang Low/Lebar Band/Intensitas Wave memakai satuan %, Ketebalan/Radius Sudut memakai px. `SliderLabelEditor` mendapat helper `showSliderEditor` (nilai min/max/offset + suffix) dan `showSecEditor` (input desimal detik).
- **Panel Battery Strip digabung ke Battery Info dengan tabbed layout** — Panel Battery Bar tidak lagi berdiri sendiri di sidebar. Panel Battery Info kini memiliki 3 tab: **Monitor** (placeholder), **Overlay** (konfigurasi Battery Info + OrderZones), dan **Battery Strip** (seluruh konfigurasi Battery Strip), berpindah lewat navigasi bawah panel.
- **OrderZones — urutan info baterai via drag-and-drop** — Di tab Overlay, checkbox °C/% diganti view chip drag dua zona (Aktif/Nonaktif) untuk mengatur urutan dan visibilitas info baterai: °C, %, V, mA, W. Chip bisa diseret antar zona untuk menampilkan/menyembunyikan info dan mengurutkannya dalam satu zona.
- **Battery Info mendukung Voltage, Current & Power** — Overlay Battery Info kini bisa menampilkan **Voltase (V)**, **Arus (mA)**, dan **Daya (W)** selain °C dan %. Pembacaan dari BatteryManager dengan fallback sysfs.
- **Panel Memory Stats — modul baru untuk monitoring memori** — Panel baru di sidebar dengan 2 tab: **Monitor** (info real-time 14 nilai: Java Heap, Native Heap, Graphics, Total, Gagal; Execution Time & rata-rata; Total Free RAM, Total RAM; jumlah proses aktif/stopped/cached/minimum; bar RAM; export/copy snapshot) dan **Overlay** (konfigurasi ukuran, warna, label, separator, shadow, background, posisi, orientasi, opsi tampilan). Mendukung background monitor yang tetap berjalan meski service overlay tidak aktif.
- **Crash Logger** — Saat force close, stack trace otomatis ditulis ke `FTxT_crash_*.txt` di folder Download (plus cadangan prefs) agar bug mudah dilaporkan tanpa logcat/adb.

### 🚮 Fitur Dihapus
- **Modul Battery Current dihapus total** — Seluruh lapisan modul (config, module, controller, fragment, panel layout) dihapus. Sidebar item, module management, restore on boot, dan notifikasi cek aktif ikut dihapus. Preset lama dengan tipe "batcur" di-ignore oleh guard lintas modul.
- **Sidebar item Battery Bar dihapus** — Seluruh konfigurasi Battery Strip kini diakses dari tab Battery Strip di dalam panel Battery Info.

### ♻️ Perubahan Fitur
- **Preset terpisah per modul** — `OverlayPreset` kini punya field `moduleType`; setiap PositionController mengisinya saat menyimpan preset. Browser preset dan metadata index memfilter daftar preset per panel sehingga preset modul lain tidak tumpang tindih. Preset lama tanpa `moduleType` tetap kompatibel dan ditolak saat di-apply ke modul yang salah.
- **Guard lintas modul saat apply preset** — `PresetHandler.applyPreset()` menolak preset yang `moduleType`-nya tidak cocok dengan panel aktif (toast "Preset ini untuk modul ..."), mencegah preset tidak sengaja mengubah modul lain.
- **Posisi preset dihormati per orientasi** — Apply preset menulis posisi ke prefs orientasi yang tersimpan di preset (`_land`/`_port`), bukan selalu orientasi layar saat ini. Format orientasi preset disamakan `land`/`port` dengan normalisasi otomatis untuk preset lama.
- **UI pemilih mode Cepat/Manual Battery Strip** — **RadioGroup** dua RadioButton ("Mode Cepat"/"Manual"). Saat Mode Cepat: pemilih Posisi Sisi (Atas/Bawah/Kiri/Kanan) tampil, dan header beserta seluruh isi section Mode Manual **hilang total (GONE)** dengan kontrol posisi terkunci; saat Manual: pemilih posisi sisi disembunyikan dan section Mode Manual tampil terbuka. Urutan panel menjadi panjang → posisi → tampilan → baterai rendah.
- **Area Aman Battery Strip selalu terkunci aktif di kedua mode** — Checkbox Area Aman tercentang & disabled (alpha 30%): Mode Cepat tanpa margin tambahan sehingga bar menempel penuh ke sisi layar, Mode Manual clamp posisi agar bar tidak masuk area notch/cutout. Checkbox dipindah ke section Tampilan sebaris "Tampilkan Strip Kosong" (label disingkat "Area Aman") agar bisa terlihat di kedua mode. Preset selalu memaksakan `true` saat di-apply.
- **Checkbox "Kunci Posisi" Battery Strip terkunci default** — Disabled & diburamkan dengan label "(tidak tersedia)": bar tidak bisa di-drag, posisi diandalkan slider + D-Pad, sentuhan selalu tembus (touch passthrough). Listener dihapus; nilai `batbar_lock` tetap dibaca dari prefs.
- **Section animasi Battery Strip** — Section "Baterai Rendah" kini menjadi dua chevron: **"Animasi Pengisian Daya"** (Animasi Shine, Kecepatan Shine, Lebar Band, Wave saat charging) dan **"Animasi Baterai Rendah"** (Warna Low, Ambang Low, Kecepatan Fade, Animasi Wave + Kecepatan & Intensitas Wave). Label checkbox wave disederhanakan menjadi **"Animasi Wave"** tanpa deskripsi perilaku.
- **Branding panel Battery** — Label sidebar "Battery Stats" diubah jadi "Battery Info", label checkbox modul ikut berganti, dan nama preset "Battery Bar" diubah jadi "Battery Strip".

### 🔧 Optimasi & Penyesuaian
- **Slider kecepatan animasi Battery Strip memakai satuan detik** — Slider Fade & Shine (serta Wave) memakai rentang **0,2–5,0 detik** step 0,1 detik (disimpan internal sebagai ms 200–5000; clamp durasi fade diperlonggar hingga 5000 ms). Preset lama dengan skala 1–20 di-clamp ke rentang baru.
- **Urutan info baterai mengikuti posisi chip OrderZones** — Pembacaan data baterai dilakukan sekali per update lalu teks dibangun sesuai urutan chip yang dipilih, bukan urutan hardcode.
- **Perubahan konfigurasi langsung diterapkan** — Perubahan di panel Battery Info dan Battery Strip langsung diterapkan ke overlay tanpa me-restart modul.
- **Memory Stats menggunakan urutan item yang bisa diatur** — Urutan item memori ditentukan posisi chip di OrderZones; pembacaan data memori sekali per update lalu teks dibangun sesuai urutan.
- **Background monitor Memory Stats** — Monitoring memori tetap berjalan meski service overlay tidak aktif (opsional), dengan interval polling yang bisa diatur.
- **Panel callback onPanelHidden()** — BasePanelFragment menambah method yang dipanggil PanelManager saat panel di-hide, memungkinkan controller menghentikan polling atau resource saat panel tidak terlihat.
- **WakeLock screen-off guard** — WakeLock hanya dipegang saat layar menyala; saat layar mati WakeLock dilepas untuk menghemat baterai.
- **NotificationHelper caching** — Bitmap ikon suhu di-cache (dibuat ulang hanya saat nilai berubah), RemoteViews + onClick PendingIntent dibuat sekali, update notifikasi di-skip bila suhu & ikon toggle tidak berubah.
- **BatteryBarModule permanent receiver** — BroadcastReceiver baterai didaftarkan permanen di `start()` dan dilepas di `stop()` dengan cache level/scale/status, tanpa `registerReceiver(null, ...)` berulang; update display di-skip jika nilai tidak berubah.
- **BatteryBarView lifecycle animasi** — Animasi (fade, shine, wave, chargeWave) otomatis berhenti saat overlay invisible dan restart saat visible via `onVisibilityChanged()`, mencegah CPU waste saat overlay tidak terlihat.

### 🐞 Bug Fixes
- **Item sidebar modul baru tidak muncul** — Sidebar dimuat dari state tersimpan yang belum berisi item baru seperti `navBatteryBar`. Kini `parseSidebarJson()` menggabungkan item default yang belum ada di state tersimpan, sehingga modul baru otomatis muncul walau sidebar pernah di-reorder.
- **Posisi overlay reset ke default setelah service restart / kill service** — Modul Clock, Battery Stats, Battery Current, dan Network tidak membaca posisi tersimpan saat dibuat. Kini keempat modul memanggil `loadPosition()` di `init()`, konsisten dengan pola Text & FPS.
- **Posisi salah saat rotasi layar** — `reloadAllPositions()` kini memanggil `reloadPosition()` per modul yang membaca ulang posisi dari prefs sesuai orientasi baru, bukan memakai nilai Config dari orientasi lama.
- **Kontrol "Animasi Wave" saat charging & "Animasi Fade" tidak berfungsi** — Kedua checkbox hanya dideklarasikan tanpa binding, load, dan listener. Kini lengkap: prefs `batbar_charge_wave_enabled`/`batbar_charge_wave_speed`/`batbar_charge_wave_amplitude` dan `batbar_fade_enabled`, serta ikut preset (`chargeWave*` + `fadeEnabled`).
- **Arah animasi shine vertikal terbalik** — Band shine pada bar vertikal kini menyapu dari bawah ke atas, searah dengan pengisian bar.
- **Teks Floating Text kembali ke default "FunText" secara tiba-tiba** — Teks kini disimpan ke prefs `text_content` setiap kali berubah (editing, overlay aktif, autoStart, apply preset) dan dimuat ulang di panel maupun MainActivity, sehingga panel dan overlay konsisten saat Activity di-recreate atau service restart.
- **Efek shine charging hanya terlihat di area bar kosong** — Band shine dipotong per segmen berdasarkan level: bagian di area terisi digambar `PorterDuff.Mode.SCREEN` alpha 100% (jelas di atas warna fill), area kosong alpha 40%; shine menyapu seluruh bar dengan intensitas berbeda per area.
- **CPU Monitor muncul di sidebar padahal belum diimplementasikan** — Item sidebar dihapus.
- **Tab Overlay di panel Memory terlihat aktif sebelum latar belakang diaktifkan** — Saat seleksi tab Overlay ditolak (background monitor belum aktif), tab kini tetap di posisi Monitor.

### 💡 Memo
- Mulai saat ini perubahan file apapun yang tidak berkaitan dengan konten utama project seperti Dokumen, file dan folder root, build/release dll tidak lagi disertakan dalam changelog.
- seluruh catatan versi sebelumnya berada di old-CHANGELOG.md

---

# [4.83.0–4.85.0] - 2026-08-04 versionCode 177
### ✨ Fitur Baru
- **Notification Actions — aksi langsung dari notifikasi** — Tiga tombol aksi di notifikasi foreground service: **Toggle** (show/hide semua overlay tanpa mengubah status modul), **Kill** (hentikan service + tutup app), **Buka** (launch aplikasi). Overlay bisa dikontrol tanpa membuka aplikasi.
- **Ikon Notifikasi Dinamis — nilai suhu baterai di status bar** — Ikon notifikasi foreground service menampilkan nilai suhu baterai aktual (misal `37°`) yang di-generate dinamis sebagai Bitmap, update setiap 10 detik. Title notifikasi juga menampilkan suhu (misal `FTxT 37°C`).
- **Tanggal di bawah Jam — tampilan dua baris** — Modul Jam menampilkan tanggal di bawah jam (`HH:mm:ss` di atas, `MMM dd EE` di bawah), aktif secara default dan bisa dimatikan via checkbox "Tanggal" di panel jam.
- **Battery Stats — suhu & persentase jadi satu kesatuan modul** — Modul `features/battery_stats` (BatteryStatsConfig + BatteryStatsModule) menggabungkan komponen suhu (°C) dan persentase (%) menjadi satu modul utuh, persis pola Battery Current: satu overlay, satu panel, satu konfigurasi (warna, label, shadow, background, posisi, safe area, interval 0,2–10 detik), satu preset, satu key prefs (`battery_*`). Checkbox °C dan % mengontrol komponen yang tampil; keduanya bisa tampil bersamaan dalam satu overlay.

### 🚮 Fitur Dihapus
- **Modul Battery Percentage terpisah dihapus** — Seluruh lapisan `features/battery_percentage` (BatteryPercentageConfig, BatteryPercentageModule), `BatteryPercentagePanelController`, `BatteryPercentagePositionController`, `BatteryPercentagePanelFragment`, dan `panel_battery_percentage.xml` dihapus total. Fungsinya menjadi bagian dari modul Battery Stats.
- **Modul Battery Temperature dihapus** — Seluruh lapisan `features/battery_temperature` (BatteryConfig, BatteryModule) dihapus, digantikan modul Battery Stats yang meneruskan perilakunya.

### ♻️ Perubahan Fitur
- **Navigasi panel refactor ke Fragment** — Sistem navigasi panel diubah dari View visibility manual menjadi Fragment-based: setiap panel punya Fragment sendiri + PanelManager untuk mengelola show/hide; semua PanelController & PositionController mendapat overload konstruktor `(Activity, View rootView)` untuk binding di Fragment; MainActivity dirombak (field View panel, field controller, hideAllPanels(), dan if-else visibility diganti PanelManager).
- **Desimal suhu hanya tampil saat data tersedia** — Nilai mentah `EXTRA_TEMPERATURE` (0,1°C) ditampilkan tanpa desimal (misal `37°C`) saat kelipatan 10, dan dengan 1 desimal (misal `37.4°C`) saat sensor mengirim nilai non-kelipatan-10. Berlaku untuk mode label maupun value-only.
- **Pemisah "|" antar komponen overlay baterai** — Suhu & persentase (Battery Stats) serta tegangan/arus/daya (Battery Current) kini dipisahkan tanda `|` (misal `37.4°C | 87%`, `4.1V | +120mA | 0.5W`), menggantikan pemisah spasi.
- **Warna pemisah bisa diatur sendiri** — Tombol warna "Pemisah" baru di panel Battery Stats & Battery Current (default abu-abu), tersimpan di prefs `battery_separator_color` / `batcur_separator_color` dan ikut serta dalam preset.

### 🔧 Optimasi & Penyesuaian
- **Refactor FloatingService — interface OverlayModule** — Interface `shared/ui/OverlayModule` menyeragamkan method semua modul overlay, diimplement di 7 modul. Sekitar 430 baris static delegates di FloatingService dihapus, diganti method generik berbasis loop (`startModule`, `stopModule`, `updateColorForModule`, dll); FloatingService turun 785 → 351 baris. 14 UI controller (7 PanelController + 7 PositionController) ikut dimigrasi ke method generik.
- **Optimasi Memori & Proses — Lazy Init & Conditional Resources** — Lima perubahan hemat memori & baterai: (1) Lazy init module — modul overlay dibuat saat pertama diaktifkan, bukan semua di `onCreate()`; (2) cleanup module saat stop — `params` dan `choreographer` dinull-kan agar bisa di-GC; (3) conditional WakeLock — hanya diambil jika ada modul aktif; (4) conditional BroadcastReceiver `CONFIG_CHANGED` — hanya aktif saat ada overlay berjalan; (5) conditional service stop — service otomatis `stopSelf()` saat modul terakhir di-stop.
- **Hapus dead code konstruktor lama** — Konstruktor `(Activity)` lama yang sudah tidak dipakai pasca-refactor fragment (berpotensi NPE) beserta method `bindViews()` tanpa parameter dihapus dari 15 file controller; import `CheckBox` ganda dihapus dari 2 file.

### 🐞 Bug Fixes
- **Layar panel kosong saat start** — Nilai `nav_selected_item` yang tidak dikenal kini fallback ke panel Floating Text + `R.id.navFloatingText`.
- **Panel Crosshair & Logo tidak bisa diakses** — `navCrosshair` dan `navLogo` kini terdaftar di `panelIdToName()` dengan judul toolbar yang benar, sehingga klik sidebar menampilkan panelnya.
- **Race condition PanelManager.showPanel()** — Klik cepat antar panel tidak lagi menumpuk transaksi fragment (panel salah tampil): `executePendingTransactions()` dipanggil sebelum iterasi, `setReorderingAllowed(true)`, dan hanya fragment yang `isAdded()` & tidak `isHidden()` yang di-hide.
- **"Muat Preset" dari menu gear tidak muncul** — Semua fragment panel kini meng-override dan meneruskan panggilan `showLoadPresetDialog()` ke controller masing-masing.
- **UI panel tidak di-refresh** — `onPanelShown()` kini dipanggil setiap panel ditampilkan (via `runOnCommit`) sehingga kontrol selalu sinkron saat pindah panel.
- **Crash `BadForegroundServiceNotificationException` — ImageButton doesn't have method: setText** — `setTextViewText(R.id.noti_title, ...)` dihapus dari custom notification; teks suhu tetap tampil via `setContentTitle()` dengan `DecoratedCustomViewStyle`.
- **Panel kosong saat memilih modul di sidebar** — Atribut `android:visibility="gone"` sisa era View-visibility manual dihapus dari root 9 layout panel; visibilitas kini dikelola sepenuhnya oleh FragmentTransaction.
- **Posisi overlay reset ke default setelah Kill Service / Tutup Aplikasi** — `System.exit(0)` di `handleKillService()` dan `forceClose()` membunuh proses sebelum `SharedPreferences.apply()` selesai di-flush sehingga posisi hilang. `System.exit(0)` dihapus dari kedua lokasi — `stopService`/`finishAffinity` sudah cukup dan data prefs aman.
- **Panel Battery Percentage tidak bisa diakses dari sidebar** — `battery_pct` terdaftar di PanelManager tanpa id sidebar sehingga tak pernah bisa diakses; panel duplikat dihapus total bersama modulnya sehingga penyebab bug hilang.


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
### 🐞 Bug Fixes
- **Slider mode color picker crash (NPE)** — Custom slider tanpa cek null. Diperbaiki dengan null check.
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

---

# [3.9.3.69.5 – 3.9.3.69.1] - 2026-06-11
### ♻️ Perubahan Fitur
- **Collapsible Section Grouping** — Semua 8 panel: section Tampilan, Posisi, Shadow, Background collapsible dengan SectionHelper.
### 🔧 Optimasi & Penyesuaian
- **Refactor layout ekstrak panel** — Pisahkan 8 panel dari `activity_main.xml` ke file `<include>` terpisah.
### 🗒️ File Added
- `panel_text.xml`, `panel_fps.xml`, `panel_clock.xml`, `panel_battery_current.xml`, `panel_network.xml`, `panel_crosshair.xml`, `panel_watermark.xml`, `panel_logo.xml`
- `SectionHelper.java`

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
- **Ekstrak PresetHandler** — ~800 baris kode duplikat dari 7 PositionController ke shared class.
- **Bersihkan styles & colors** — Hapus 6 style + 5 color neumorphism tidak terpakai.
### 🐞 Bug Fixes
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
