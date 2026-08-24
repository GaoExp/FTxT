# [4.89.0] - 2026-08-24 21:23 WITA versionCode 187 ***ONGOING***
### ✨ Fitur Baru
- **Modul Crosshair: overlay bidikan untuk game** — Panel Crosshair yang semula placeholder kini berisi modul overlay sungguhan dengan 44 gaya bidikan siap pilih dari galeri thumbnail (pilihan tersorot garis aksen cyan). Ukuran bisa diatur 24–160dp lewat slider atau ketuk labelnya untuk mengetik angka persis, opasitas 10–100% agar bidikan bisa dibuat transparan, dan posisi diatur bebas tiga cara: geser langsung bidikannya di layar, slider X/Y, atau tombol panah D-Pad untuk geser halus perlahan. Posisi yang terpasang mengacu ke titik tengah bidikan (bukan pojok gambarnya), tersimpan otomatis terpisah untuk mode tegak & mendatar, dan muncul kembali sendiri saat HP direstart bila dibiarkan aktif. Opsi "Kunci Posisi" membuat sentuhan menembus ke game (bidikan tak ikut tergeser), opsi "Area Aman" menjaga bidikan tidak melolos keluar tepi layar. Status modul aktif ikut tampil di notifikasi layar mengambang.
- **Ketuk grafik baterai membuka halaman detail fullscreen** — Kelima grafik tab Monitor kini bisa diketuk membuka halaman detail ala aplikasi pemantau baterai: Persentase di kartu Metrik Real-Time serta Suhu, Daya, Tegangan & Arus di kartu Grafik Riwayat. Tiap halaman berisi grafik besar interaktif dengan crosshair yang mengikuti jari (garis vertikal + titik penebal + gelembung nilai & jam titik terpilih) dan tetap menempel setelah jari dilepas, baris label rentang waktu 2m–24j yang bisa diketuk maupun digeser lewat slider transparan (independen dari slider panel monitor; rentang awal mengikuti rentang aktif panel), serta kartu statistik Min / Max / Rata-rata / Δ dari data periode yang tampil. Header halaman beraksen warna metrik dengan subjudul nilai terkini; data disegarkan tiap 5 detik dan ditunda saat jari sedang menelusuri grafik.

### 🚮 Fitur Dihapus
- **Preview pilihan ikon orientasi dihapus dari panel Debugging** — Panel Debugging yang semula menampilkan judul beserta grid 5 varian ikon orientasi layar untuk dipilih (Material Default, Ponsel + Panah, Tablet + Panah, Panah Sederhana, Lingkaran + P) kini kosong dan hanya menampilkan placeholder "Konsep overlay sedang dalam tahap pengerjaan / Penerapan akan segera diluncurkan." ala panel Logo Display. Kelima drawable ikon varian rotasi yang tak terpakai ikut dihapus.

### ♻️ Perubahan Fitur
- **Garis grafik Suhu berwarna gradien mengikuti nilai suhu** — Pada grafik Suhu (kartu Grafik Riwayat maupun halaman detail), warna tiap segmen garis kini mengikuti suhu titik datanya sebagai gradien mulus: 5°C ke bawah putih, 5–24° memutih memudar ke ice blue (makin dingin makin putih), 25–34° ice blue → hijau, 35–39° hijau → oranye, 40–45°+ oranye → merah. Titik crosshair di halaman detail ikut mewarisi warna suhu titik yang dipilih.
- **Garis grafik Persentase/Tegangan/Arus berwarna sesuai status pengisian baterai** — Pada ketiga grafik itu (kartu Metrik Real-Time maupun halaman detail), garis kini digambar dua warna dalam satu garis nyambung: segmen saat baterai sedang mengisi digambar hijau, segmen saat tidak mengisi digambar merah; warna berganti tepat di titik data tempat status baterai berubah. Titik crosshair di halaman detail ikut mewarisi warna status titik yang dipilih. Grafik Daya tidak berubah.
- **Kunci password jadi "Fitur Developer" yang mengatur panel Info Memori, Debugging, dan Salin/Simpan Snapshot** — Proteksi password yang sebelumnya hanya menempel pada switch panel Debugging kini menjadi satu saklar "Fitur Developer" di halaman Konfigurasi: label statusnya berganti "Fitur Developer • Terkunci" (merah) / "• Terbuka" (hijau), dengan kolom kunci + tombol Unlock/Kunci Ulang tepat di bawahnya. Saat Terkunci, switch "Tampilkan panel Info Memori" & "Tampilkan panel Debugging" tidak bisa dinyalakan; tombol Kunci Ulang mematikan kedua panel sekaligus (overlay Memory yang berjalan ikut berhenti, panel aktif dialihkan, keduanya hilang dari sidebar). Tombol Salin & Simpan Snapshot di tab Monitor Battery Info juga terkunci — tampak buram dan tidak bisa ditekan sampai Fitur Developer dibuka, dicek ulang setiap tab Monitor ditampilkan. Section "Modul" di halaman Konfigurasi diganti nama jadi "Developer".
- **Grafik riwayat Suhu/Daya/Tegangan/Arus tersusun grid 2×2** — Empat grafik di kartu Grafik Riwayat yang sebelumnya tampil utuh empat baris ke bawah kini disusun seperti tabel dua kolom: baris atas Suhu & Daya, baris bawah Tegangan & Arus. Tiap sel tetap punya sub-header aksen warna sendiri dan menggambar elemen yang sama persis dengan grafik level baterai (label sumbu Y max/tengah/min, label waktu awal–akhir, nilai terkini di ujung garis); tinggi tiap grafik 140→130dp. Divider pemisah antar grafik diganti jarak antar sel grid. Chevron pelipat kartu dihapus — isi kartu kini selalu tampil karena grid tidak lagi memakan ruang layar.

### 🔧 Optimasi & Penyesuaian
- **Pencatatan baterai saat mengisi daya diringankan ke ±5 detik** — Interval sampling monitor baterai saat charging dilonggarkan dari ±1 detik menjadi ±5 detik sehingga pencatatan lebih hemat; grafik riwayat & estimasi kesehatan tetap berjalan normal.
- **Angka nilai di ujung garis grafik diberi chip latar agar tetap terbaca** — Teks nilai terkini di ujung garis pada kelima grafik baterai (panel maupun halaman detail) kini berada di atas chip kecil gelap transparan dengan angka putih, sehingga tetap terbaca saat garis-garis grafik menumpuk tanpa menutupi bentuk grafik di bawahnya; sebelumnya teks abu polos yang mudah tertimpa garis. Posisi chip otomatis membalik ke bawah titik saat grafik mepet batas atas.
- **Pemilih rentang waktu grafik jadi baris label yang bisa diketuk & digeser** — Baris lima radio button (5 Menit/15 Menit/1 Jam/6 Jam/24 Jam) diganti deretan label singkat 2m/5m/10m/15m/30m/1j/3j/6j/12j/24j yang berperilaku layaknya tombol: slider transparan menimpa baris label sehingga ketuk sebuah label langsung memilih interval itu dan menggeser jari pindah antar interval, tiap langkah slider jatuh tepat di tengah labelnya. Garis pembatas kecil di bawah label dihapus; label aktif disorot tebal berwarna sebagai indikator posisi. Label rentang aktif tetap tampil di header kartu; saat label diketuk/digeser pratinjau ikut berubah, data grafik baru di-query saat jari dilepas (default 5 Menit).
- **Kode tab Monitor Battery Info dipecah menjadi beberapa file** — BatteryPanelController yang menampung seluruh panel (1.020 baris) kini hanya mengurus tab Overlay (±560 baris); isi tab Monitor dipindah utuh ke empat controller terpisah: BatteryMonitorTabController (ring gauge, metrik real-time, polling 1 detik & tombol Salin/Simpan Snapshot), BatteryChartHistoryController (kartu Grafik Riwayat lengkap dengan slider rentang), BatteryHealthCardController (kartu Kesehatan Baterai + dialog kapasitas desain), dan BatterySnapshotExporter (pembuat teks salinan/berkas ekspor). Perilaku fitur tidak berubah.

### 🐞 Bug Fixes
- **Warna status pengisian grafik riwayat kini benar di semua rentang** — Pada rentang 15 Menit ke atas, garis Persentase/Tegangan/Arus seluruhnya tergambar merah padahal baterai sedang mengisi daya (rentang 2–10 Menit normal). Kini kondisi pengisian tiap titik ikut terhitung pada rentang panjang sehingga segmen hijau/merah kembali sesuai keadaan aslinya hingga rentang 24 Jam.
- **Skala grafik Suhu kini maksimal tetap 45°C** — Sebelumnya skala Y Suhu otomatis simetris ±2°C dari nilai tengah data sehingga bentuk garis nyaris tidak terbaca saat suhu stabil. Kini batas atas skala selalu 45°C (melebar otomatis hanya bila suhu melebihi 45° agar garis tidak hilang) dan batas bawah otomatis mengikuti data terkecil.

### 🗒️ File Added
- 2026-08-24 10:58 — `crosshair_1.png` … `crosshair_44.png` — 44 aset gambar gaya bidikan (sumber `_schedule/drawable-nodpi-v4/chr_1..44.png`)
- 2026-08-24 11:00 — `CrosshairConfig.java` — Konfigurasi statis modul Crosshair
- 2026-08-24 11:01 — `CrosshairModule.java` — Modul overlay ImageView: posisi titik tengah, drag + safe area, apply style/opacity
- 2026-08-24 11:03 — `CrosshairPanelController.java` — UI panel Crosshair: grid 44 gaya, size/opacity, switch Material
- 2026-08-24 11:03 — `CrosshairPositionController.java` — Kontrol posisi Crosshair: slider X/Y + D-Pad + koordinat
- 2026-08-24 11:04 — `bg_style_item.xml`, `bg_style_item_selected.xml` — Sel grid gaya bidikan default & terpilih
- 2026-08-24 05:12 — `BatteryChartView.java` — Mode interaktif crosshair (garis vertikal + titik + gelembung nilai & jam) dengan flag default mati agar grafik panel tak berubah perilaku
- 2026-08-24 05:41 — `BatteryChartDetailActivity.java` — Halaman detail satu grafik: chart besar interaktif, baris label rentang independen 2m–24j dikontrol seekbar transparan, kartu statistik Min/Max/Rata-rata/Δ, refresh 5 detik yang ditunda saat crosshair aktif
- 2026-08-24 06:19 — `activity_battery_chart_detail.xml` — Layout halaman detail: header aksen metrik + subjudul nilai terkini, chart 220dp, baris label rentang dengan seekbar transparan overlay (tinggi menyesuaikan label), kartu statistik 4 kolom
- 2026-08-24 01:37 — `BatteryChartHistoryController.java` — Controller kartu Grafik Riwayat tab Monitor: binding 5 chart + pemilih rentang berbasis label & slider transparan (pratinjau saat digeser, query saat lepas jari), query database via executor latar
- 2026-08-24 00:32 — `BatteryHealthCardController.java` — Controller kartu Kesehatan Baterai tab Monitor: estimasi kapasitas, skor kesehatan berwarna, keyakinan, status pengumpulan, dialog kapasitas desain
- 2026-08-24 00:38 — `BatterySnapshotExporter.java` — Pembuat teks salinan clipboard & ekspor 20 snapshot terakhir ke folder Download (MediaStore/API lama), menyertakan catatan kesehatan baterai
- 2026-08-24 00:42 — `BatteryMonitorTabController.java` — Controller orkestrasi tab Monitor: ring gauge, dua kolom metrik real-time, badge kondisi suhu, polling 1 detik hanya saat tab tampil, memegang tiga controller di atas

### ✏️ File Changed
- 2026-08-24 21:11 — `BatteryMonitor.java` — Interval sampling saat mengisi daya dilonggarkan ±1 dtk → ±5 dtk
- 2026-08-24 21:12 — `BatteryHistoryDb.java` — Query grafik jalur agregasi kini ikut menghitung status pengisian per bucket agar warna garis rentang panjang benar
- 2026-08-24 11:38 — `panel_crosshair.xml` — Placeholder "coming soon" diganti panel lengkap mengikuti pola panel overlay lain (switch + Kunci Posisi, section Tampilan & Posisi collapsible)
- 2026-08-24 11:06 — `CrosshairPanelFragment.java` — Memasang controller panel & siklus hidup seperti panel lain
- 2026-08-24 11:07 — `FloatingService.java` — Registrasi CrosshairModule: field, accessor statis, ensure, start saat layanan hidup
- 2026-08-24 11:07 — `BootReceiver.java` — Pulihkan status aktif Crosshair saat boot & ikutkan dalam cek modul aktif
- 2026-08-24 11:08 — `NotificationHelper.java` — Crosshair ikut menandai notifikasi ada modul aktif ("Xhair")
- 2026-08-24 11:08 — `MainActivity.java` — Muat konfigurasi Crosshair dari preferensi, ikutkan dalam cek modul aktif, label sidebar "(coming soon)" dihapus
- 2026-08-24 11:38 — `CrosshairPanelController.java` — Switch Material diganti CheckBox, section Gaya Bidikan jadi Tampilan; kontrol Kunci Posisi pindah ke atas bersama switch, Area Aman pindah ke section Posisi
- 2026-08-24 11:38 — `colors.xml`, `values-night/colors.xml` — Warna latar sel galeri gaya bidikan & aksen Crosshair (#26C6DA) untuk tema terang & gelap
- 2026-08-24 08:48 — `panel_debuging.xml` — Isi preview ikon rotasi (judul, sub-judul, grid 5 ikon, info) diganti placeholder "coming soon" dua baris
- 2026-08-24 08:48 — `DebugingPanelFragment.java` — Logika klik ikon varian & Toast dihapus; fragment kembali hanya inflate layout
- 2026-08-24 08:31 — `BatteryChartView.java` — Alpha chip latar nilai terkini diturunkan (0xCC → 0x66) agar tidak lagi menutupi tampilan grafik di bawahnya
- 2026-08-24 07:58 — `BatteryChartView.java` — Garis grafik Suhu diwarnai gradien per segmen mengikuti nilai suhu (putih→ice blue→hijau→oranye→merah); titik crosshair ikut warna titik terpilih
- 2026-08-24 07:58 — `colors.xml`, `values-night/colors.xml` — Tambah warna anchor ice blue gradien Suhu (#33F1FF) untuk tema terang & gelap
- 2026-08-24 07:39 — `BatteryChartView.java` — Garis grafik Persentase/Tegangan/Arus diwarnai per segmen sesuai status pengisian (hijau isi / merah tidak); titik crosshair ikut warna titik terpilih
- 2026-08-24 07:32 — `colors.xml`, `values-night/colors.xml` — Tambah pasangan warna garis charging/discharging (hijau/merah) untuk tema terang & gelap
- 2026-08-24 07:18 — `BatteryChartHistoryController.java` — Klik grafik Suhu, Daya, Tegangan & Arus membuka halaman detail metrik terkait (rentang awal mengikuti slider panel)
- 2026-08-24 06:19 — `panel_battery.xml` — Pemilih rentang Grafik Riwayat: garis pembatas di bawah label dihapus, slider jadi transparan dan diposkan menimpa baris label agar label bisa diketuk/digeser layaknya tombol, tinggi area menyesuaikan baris label (tanpa ruang sisa), label interval diperbesar
- 2026-08-24 06:04 — `BatteryChartHistoryController.java` — Padding dinamis 5% lebar pada slider transparan supaya tiap langkah jatuh tepat di tengah labelnya
- 2026-08-24 05:30 — `AndroidManifest.xml` — Daftarkan halaman detail grafik ke manifest
- 2026-08-24 05:28 — `BatteryMonitorTabController.java` — Ketuk grafik Persentase membuka halaman detail dengan rentang awal mengikuti slider panel
- 2026-08-24 05:26 — `BatteryChartHistoryController.java` — Buka akses daftar pilihan rentang & getter rentang aktif untuk halaman detail
- 2026-08-24 04:49 — `BatteryChartView.java` — Batas atas tetap skala Y grafik Suhu diturunkan 50°C → 45°C; skala tetap melebar otomatis bila suhu melebihi 45°
- 2026-08-24 02:17 — `activity_settings.xml` — Section "Modul" jadi "Developer": label status "Fitur Developer • Terkunci/Terbuka" + kolom kunci & tombol Unlock/Kunci Ulang dipindah ke atas, switch Info Memori ikut disabled default
- 2026-08-24 02:17 — `SettingsActivity.java` — Logika Fitur Developer: satu pref kunci untuk kedua switch panel, Kunci Ulang mematikan Memory overlay/monitor + broadcast pengalihan panel, status unlock pindah ke label berwarna (pesan kunci salah via Toast)
- 2026-08-24 02:17 — `BatteryMonitorTabController.java` — Tombol Salin & Simpan Snapshot disabled + buram saat Fitur Developer terkunci, dicek ulang tiap tab Monitor ditampilkan
- 2026-08-24 01:07 — `panel_battery.xml` — Kartu Grafik Riwayat: RadioGroup 5 pilihan diganti slider full width tanpa label statis + deretan pembatas berlabel singkat 2m–24j di atasnya; chevron pelipat kartu dihapus (isi selalu tampil); empat grafik disusun grid 2×2 (baris 1: Suhu/Daya, baris 2: Tegangan/Arus) dengan sub-header aksen per sel, tinggi chart 130dp, divider antar chart dihapus
- 2026-08-24 01:07 — `BatteryChartView.java` — Konstanta window baru WINDOW_2M, WINDOW_10M, WINDOW_30M, WINDOW_3H, WINDOW_12H; skala Y Suhu maksimal tetap 50°C dengan batas bawah otomatis mengikuti data
- 2026-08-24 00:46 — `BatteryPanelController.java` — Seluruh isi tab Monitor dipindah ke controller terpisah; kini hanya mengurus tab Overlay (1.020 → ±560 baris), membuat BatteryMonitorTabController di konstruktor dan mendelegasikan onPanelShown/onPanelHidden/cleanup/refresh awal

### 🔥 File Removed
- 2026-08-24 08:48 — `ic_rotation_variant_1.xml`–`ic_rotation_variant_5.xml` — Lima drawable ikon varian rotasi tak terpakai setelah isi panel Debugging dikosongkan

---

# [4.88.0] - 2026-08-23 21:56 WITA versionCode 185 ***PUSH***
### ✨ Fitur Baru
- **Tab Monitor di panel Battery Info — monitoring real-time baterai perangkat dengan ring gauge melingkar** — Tab Monitor (sebelumnya placeholder) kini berisi pemantauan baterai ala aplikasi Battery Guru/AccuBattery dalam satu kartu Metrik Real-Time: ring gauge melingkar di sebelah kiri yang arc-nya berwarna gradien hue mengikuti level baterai (skema warna Battery Strip), dengan level %, kapasitas tersisa (mAh), dan status pengisian singkat (Charging•AC/USB/Wireless, Full, Discharging) tampil di dalam lingkaran; grafik riwayat Persentase berada di sebelah kanan ring, grid monospace suhu/voltase/arus/daya/cycle count/teknologi baterai berjajar di bawahnya; badge kondisi suhu (Normal/Panas/Dingin) tampil di header kartu. Cycle count hanya tampil jika device melaporkan (API 34+/vendor tertentu).
- **Grafik Riwayat di tab Monitor panel Battery Info — satu kartu empat chart yang bisa dilipat** — Riwayat pemantauan digambar sebagai line chart via Canvas (tanpa library eksternal) dalam satu kartu Grafik Riwayat: pemilih rentang 5 Menit / 15 Menit / 1 Jam / 6 Jam / 24 Jam (berlaku global untuk semua chart) di bagian atas, lalu empat chart Suhu, Daya, Tegangan, dan Arus berjajar ke bawah dengan sub-header beraksen warna masing-masing yang dipisah divider. Chevron di header kartu untuk menyembunyikan/menampilkan seluruh isi grafik. Tiap chart punya sumbu Y sendiri dengan label nilai max/tengah/min di kiri (skala otomatis mengikuti data) dan angka nilai terkini di ujung garis; label sumbu waktu menyesuaikan rentang panjang (detik/menit/jam-tanggal). Data grafik bersumber dari database sehingga riwayat lama tetap terlihat walau aplikasi sempat ditutup.
- **Estimasi Kapasitas & Kesehatan Baterai di tab Monitor panel Battery Info** — Fitur ala AccuBattery: saat perangkat mengisi daya, aplikasi mencatat segmen pengisian (Δkapasitas mAh vs Δpersen) lalu menghitung estimasi kapasitas penuh per sesi. Estimasi lintas sesi diagregasi dengan median (tahan outlier) dan tersimpan persisten di database. Sesi dengan mayoritas waktu layar mati diberi prioritas dalam perhitungan median (hasil lebih akurat, butuh minimal 3 sesi layar-mati; jika belum ada, semua sesi dipakai). Kartu Kesehatan Baterai menampilkan: estimasi kapasitas, skor kesehatan (estimasi ÷ kapasitas desain × 100%, berwarna hijau ≥80% / oranye ≥50% / merah <50%), jumlah sesi tercatat, indikator keyakinan (jumlah sampel), dan status pengumpulan data real-time. Kapasitas desain (mAh) diinput manual via dialog ketuk — skor kesehatan baru muncul jika terisi. Segmen dengan Δpersen <5% atau durasi <1 menit otomatis dibuang agar estimasi tidak noise. Tombol Salin & Simpan Snapshot berada di dalam kartu ini; hasil salinan/berkas ekspornya menyertakan catatan kesehatan baterai (estimasi kapasitas, skor, jumlah sesi, keyakinan).
- **Pemantauan baterai full-aktif — merekam otomatis tanpa henti** — Monitor baterai kini selalu aktif tanpa tombol mulai/hentikan: overlay dipakai atau tidak, panel dibuka atau tidak, aplikasi ditutup sekalipun, pencatatan tetap berjalan lewat foreground service ringan tersendiri dengan notifikasi minimal prioritas rendah. Otomatis nyala saat aplikasi dibuka dan direstart otomatis saat boot. Sampling dinamis hemat baterai: rapat (±1 detik) hanya saat mengisi daya (presisi estimator), ±5 detik saat layar nyala idle, ±30 detik saat layar mati.
- **Database riwayat baterai lokal (SQLite)** — Riwayat metrik (time-series) dan sesi pengisian kini disimpan di database SQLite bawaan framework (tanpa Room/kapt/KSP): buffer memori grafik ±1 jam dan persistensi JSON dihapus. Kartu grafik, export/copy snapshot, dan estimasi kesehatan semuanya membaca dari database; tanpa auto-trim pembuang riwayat (ukuran per baris sangat kecil). Data JSON estimasi versi lama dimigrasikan otomatis sekali ke database lalu filenya dihapus.

### 🔧 Optimasi & Penyesuaian
- **Pembacaan data baterai disatukan ke satu sumber** — Overlay Battery Info dan tab Monitor kini membaca metrik dari util pembaca tunggal (`BatteryReading`), bukan masing-masing punya logika sendiri. Menghilangkan duplikasi rumus yang pernah membuat angka Daya overlay dan tab Monitor berbeda (8W vs 0,008W) karena salah faktor konversi.

### 🗒️ File Added
- 2026-08-21 20:45 — `BatteryMonitor.java` — Background monitor polling data baterai (snapshot metrik lengkap, riwayat 20 snapshot, helper status/kondisi)
- 2026-08-21 20:48 — `bat_card_bg.xml` — Drawable card tab Monitor Battery
- 2026-08-21 20:48 — `bat_badge_active_bg.xml`, `bat_badge_stopped_bg.xml` — Drawable badge status monitor
- 2026-08-22 20:41 — `BatteryChartView.java` — Custom view line chart Canvas satu metrik per instance (Suhu/Persen/Daya), label sumbu Y max/tengah/min di kiri, angka nilai terkini di ujung garis, rentang waktu 5 menit–1 jam, tanpa library eksternal
- 2026-08-22 18:58 — `BatteryCapacityEstimator.java` — Akumulasi estimasi kapasitas dari segmen pengisian daya + persistensi JSON internal storage
- 2026-08-23 12:16 — `BatteryReading.java` — Util pembaca metrik baterai tunggal (battery intent + property + sysfs fallback, konversi satuan, helper status/kondisi) untuk overlay & tab Monitor
- 2026-08-23 12:51 — `BatteryHistoryDb.java` — Database SQLite riwayat baterai (tabel sampel metrik time-series + sesi pengisian + meta, query per rentang dengan downsampling otomatis)
- 2026-08-23 12:51 — `BatteryMonitorService.java` — Foreground service ringan monitor baterai full-aktif (notifikasi minimal prioritas rendah, auto-start app & boot)
- 2026-08-23 20:01 — `BatteryRingView.java` — Custom view ring gauge baterai melingkar (track + arc gradien hue, teks level/kapasitas/status di dalam lingkaran)
- 2026-08-23 20:01 — `BatteryColors.java` — Helper warna bersama: rumus gradien hue baterai satu sumber untuk Battery Strip & ring gauge

### ✏️ File Changed
- 2026-08-21 20:50 — `BatteryStatsConfig.java` — Tambah field backgroundMonitor
- 2026-08-21 20:52 — `panel_battery.xml` — Ganti placeholder tab Monitor dengan UI monitoring lengkap (kartu level, metrik, status pengisian, toggle, switch latar belakang, salin/simpan snapshot)
- 2026-08-21 20:55 — `colors.xml` — Tambah 13 color values untuk tab Monitor Battery
- 2026-08-21 20:56 — `BatteryPanelController.java` — Tambah logika tab Monitor: polling update UI, toggle manual, switch latar belakang, badge status/kondisi, export/copy snapshot
- 2026-08-21 20:57 — `BatteryPanelFragment.java` — Panggil onPanelShown() controller di semua tab + onPanelHidden() saat panel disembunyikan
- 2026-08-21 20:58 — `FloatingService.java` — Start/stop BatteryMonitor sesuai backgroundMonitor di onCreate/stopAllModules/onDestroy, tambah setBackgroundBatteryMonitorEnabled(), cek backgroundMonitor di isAnyModuleActive()
- 2026-08-21 20:58 — `MainActivity.java` — Load pref bat_bg_monitor, cek BatteryStatsConfig.backgroundMonitor di isAnyModuleActive()
- 2026-08-21 20:58 — `NotificationHelper.java` — Cek backgroundMonitor di isAnyModuleActive, tambah label "BatMon" di teks modul aktif notifikasi
- 2026-08-21 20:58 — `BootReceiver.java` — Restore pref bat_bg_monitor saat boot
- 2026-08-22 18:35 — `BatteryMonitor.java` — Tambah buffer grafik ±1 jam (3600 titik, auto-trim, terpisah dari riwayat snapshot export, bertahan lintas start/stop pemantauan) + akses data & reset grafik
- 2026-08-22 20:41 — `panel_battery.xml` — Kartu Grafik Riwayat dipecah: kartu kontrol (rentang/Jeda/Reset) + tiga kartu chart terpisah untuk Suhu, Persentase, dan Daya; checkbox seri dihapus
- 2026-08-22 18:35 — `colors.xml` — Tambah 4 color values untuk grafik (garis suhu/persen/daya + grid)
- 2026-08-22 20:41 — `BatteryPanelController.java` — Binding & kontrol tiga kartu chart terpisah: pilih rentang (berlaku global), jeda/lanjut (sampling tetap jalan), reset dengan dialog konfirmasi; chart ikut diperbarui loop polling 1 detik
- 2026-08-22 18:58 — `BatteryMonitor.java` — Hook estimasi kapasitas: init estimator saat start, kirim sampel tiap polling, finalize segmen & simpan saat stop
- 2026-08-22 18:58 — `panel_battery.xml` — Tambah kartu Kesehatan Baterai di tab Monitor (teks hasil monospace, input kapasitas desain via ketuk, badge jumlah sesi, tombol Reset Data Estimasi)
- 2026-08-22 18:58 — `BatteryPanelController.java` — Kartu Kesehatan: refresh real-time (estimasi, skor berwarna, keyakinan, status), dialog input kapasitas desain (validasi 500–30000 mAh), reset data dengan konfirmasi
- 2026-08-22 19:26 — `values-night/colors.xml` — Varian gelap 17 warna tab Monitor Battery (kartu, badge, level bar, grafik) agar kartu tidak putih di mode gelap
- 2026-08-23 12:16 — `BatteryMonitor.java` — Logika pembacaan snapshot dipindah ke `BatteryReading`; helper status/kondisi jadi method pada Snapshot
- 2026-08-23 12:16 — `BatteryStatsModule.java` — Blok baca mandiri (battery intent + sysfs) diganti panggilan `BatteryReading.read()`, format teks overlay tidak berubah
- 2026-08-23 12:16 — `BatteryChartView.java` — Tipe data seri grafik mengikuti model `BatteryReading.Snapshot`
- 2026-08-23 12:16 — `BatteryCapacityEstimator.java` — Sumber sampel & cek status charging via `BatteryReading.Snapshot`
- 2026-08-23 12:16 — `BatteryPanelController.java` — Referensi tipe snapshot & pemanggilan helper status/kondisi menyesuaikan model baru
- 2026-08-23 12:51 — `BatteryMonitor.java` — Buffer memori riwayat 20 snapshot & grafik 3600 titik dihapus; sampel polling langsung masuk database; interval sampling dinamis (1 dtk charging / 10 dtk layar nyala / 60 dtk layar mati)
- 2026-08-23 12:51 — `BatteryCapacityEstimator.java` — Persistensi JSON diganti tabel sesi di database (migrasi file JSON lama sekali otomatis lalu dihapus); durasi segmen memakai delta waktu aktual; trim maksimum 60 sesi dibuang
- 2026-08-23 12:51 — `MainActivity.java` — Auto-start BatteryMonitorService saat aplikasi dibuka; jejak pref bat_bg_monitor dibersihkan
- 2026-08-23 12:51 — `BootReceiver.java` — BatteryMonitorService selalu direstart saat boot; pref bat_bg_monitor dibersihkan dari syarat restore overlay
- 2026-08-23 12:51 — `FloatingService.java` — Lifecycle monitor baterai dilepas dari service overlay (start/stop, setBackgroundBatteryMonitorEnabled, cek isAnyModuleActive)
- 2026-08-23 12:51 — `NotificationHelper.java` — Cek backgroundMonitor & label "BatMon" pada notifikasi dibuang
- 2026-08-23 12:51 — `BatteryStatsConfig.java` — Field backgroundMonitor dihapus
- 2026-08-23 12:51 — `panel_battery.xml` — Kontrol manual tab Monitor dihapus (tombol Mulai/Hentikan, switch Latar Belakang, badge status, tombol Jeda/Reset Grafik, tombol Reset Data Estimasi); pilihan rentang grafik diperluas 6 Jam & 24 Jam
- 2026-08-23 12:51 — `BatteryPanelController.java` — Seluruh listener kontrol manual dihapus; grafik digambar dari query database per rentang secara background thread; export snapshot membaca 20 sampel terakhir dari database
- 2026-08-23 12:51 — `BatteryChartView.java` — Konstanta rentang 6 Jam & 24 Jam; format label sumbu waktu adaptif (HH:mm:ss / HH:mm / dd/MM HH:mm)
- 2026-08-23 12:51 — `AndroidManifest.xml` — Deklarasi BatteryMonitorService (foregroundServiceType specialUse)
- 2026-08-23 12:51 — `colors.xml`, `values-night/colors.xml` — Warna badge status monitor yang tak terpakai dibersihkan
- 2026-08-23 18:37 — `panel_battery.xml` — Tab Monitor diringkas jadi 3 kartu: kartu Level Baterai & Status Pengisian dilebur ke Metrik Real-Time (% Level masuk grid, badge kondisi suhu pindah ke header), kontrol rentang + 3 kartu chart digabung jadi satu kartu Grafik Riwayat
- 2026-08-23 18:37 — `BatteryPanelController.java` — Binding & pengisian kartu Level dihapus, "% Level" masuk grid metrik real-time, isi Salin snapshot disesuaikan
- 2026-08-23 18:37 — `colors.xml`, `values-night/colors.xml` — Warna track bar level (`bat_monitor_bar_track`) tak terpakai dibersihkan
- 2026-08-23 20:01 — `panel_battery.xml` — Kartu Metrik Real-Time berisi ring gauge melingkar (level, kapasitas, status) di kiri + grid metrik monospace di kanan; blok status pengisian dihapus; kartu Grafik Riwayat dapat chevron collapse untuk menyembunyikan isi
- 2026-08-23 20:01 — `BatteryPanelController.java` — Binding & pengisian ring gauge; baris % Level & Kapasitas keluar dari grid metrik (pindah ke ring); status pengisian singkat format Charging•AC/USB/Wireless / Discharging / Full; tombol lipat grafik riwayat; isi Salin snapshot menyesuaikan
- 2026-08-23 20:28 — `panel_battery.xml` — Label statis "Pemantauan real-time baterai perangkat" di atas tab Monitor dihapus; tombol Salin & Simpan Snapshot pindah ke dalam kartu Kesehatan Baterai
- 2026-08-23 20:49 — `panel_battery.xml` — Grafik Persentase pindah ke kartu Metrik Real-Time (sejajar ring, grid metrik turun ke bawah); kartu Grafik Riwayat kini Suhu/Daya/Tegangan/Arus
- 2026-08-23 20:28 — `BatteryPanelController.java` — Isi Salin & Simpan Snapshot menyertakan catatan kesehatan baterai (estimasi kapasitas, skor, sesi, keyakinan)
- 2026-08-23 20:49 — `BatteryPanelController.java` — Binding & pengisian grafik Tegangan & Arus
- 2026-08-23 20:49 — `BatteryChartView.java` — Seri baru Tegangan (V) & Arus (mA/A) dengan skala otomatis dan format nilai sendiri
- 2026-08-23 20:49 — `colors.xml`, `values-night/colors.xml` — Warna aksen chart Tegangan & Arus
- 2026-08-23 21:19 — `BatteryMonitor.java` — Interval sampling layar nyala & layar mati dirapatkan: 10→5 detik dan 60→30 detik
- 2026-08-23 21:19 — `BatteryHistoryDb.java` — Data grafik di-resample ke grid waktu seragam (interpolasi linear) sehingga kepadatan garis konsisten dan tidak berubah pola saat interval sampling berganti
- 2026-08-23 21:19 — `BatteryChartView.java` — Skala sumbu Y grafik Suhu memiliki rentang minimum ±2°C dari nilai tengah agar variasi kecil tidak menggambar garis ekstrem
- 2026-08-23 20:01 — `BatteryBarView.java` — Rumus gradien hue diekstrak ke helper bersama `BatteryColors` (perilaku identik)

### 🔥 File Removed
- 2026-08-23 12:51 — `bat_badge_active_bg.xml` — Drawable badge status monitor tak terpakai setelah kontrol manual dihapus

---

# [4.87.0] - 2026-08-21 20:28 WITA versionCode 184 ***RELEASE***
### ✨ Fitur Baru
- **Toggle "Tampilkan panel Debugging" & "Tampilkan panel Info Memori" di halaman Konfigurasi** — Dua switch baru di halaman Pengaturan > Konfigurasi, section Modul, untuk menampilkan/menyembunyikan panel Debugging dan Info Memori dari sidebar Navigation Drawer. Kedua switch default OFF (sembunyi). Setting tersimpan otomatis dan berlaku persisten.
- **Proteksi password panel Debugging** — Switch panel Debugging terkunci (disabled) dan membutuhkan kunci password untuk membukanya. Kolom input password + tombol Unlock muncul di bawah switch. Setelah password benar dimasukkan dan tombol diklik, switch terbuka dan tombol Relock muncul di sebelah switch untuk mengunci ulang. Status unlock tersimpan persisten.

### 🔧 Optimasi & Penyesuaian
- **Perbaikan typo label "Debuging" → "Debugging"** — Seluruh label navigasi, judul panel, dan string resource diperbaiki dari "Debuging" menjadi "Debugging".
- **Tombol Relock panel Debugging diperkecil** — Tombol Relock di halaman Konfigurasi diubah dari `Button` menjadi `TextView` (11sp, warna merah) agar tidak mencolok dan konsisten dengan toggle switch di sebelahnya.
- **Urutan section halaman Konfigurasi ditata ulang** — "Ikon Aplikasi" dipindah ke posisi paling atas, "Akses Izin" di bawahnya. Di section Modul: "Info Memori" di atas "Debugging".

### ♻️ Perubahan Fitur
- **Urutan item Memory Stats via OrderZones drag-and-drop** — Pemilihan item Memory Stats (Heap, Native, Graphics, Total) diubah dari 4 checkbox individual menjadi `MemoryOrderZonesView` drag-and-drop dua zona (Aktif/Nonaktif), konsisten dengan panel Battery Info. Pengguna bisa menyeret chip antar zona untuk menampilkan/menyembunyikan item dan mengurutkannya dalam satu zona.

### 🐞 Bug Fixes
- **Sidebar drag-and-drop tidak stabil (item tumpuk, ruang kosong, tidak berubah)** — `rebuildSidebar()` dipanggil di `onCreate()` DAN `onResume()`, menyebabkan DividerItemDecoration bertambah terus (ruang kosong), seluruh infrastruktur RecyclerView di-rebuild setiap kali (animasi aneh, drag state hilang), dan `getAdapterPosition()` deprecated yang bisa gagal diam-diam. Kini dipisah: `initSidebar()` (sekali di `onCreate()`) untuk setup LayoutManager + DividerItemDecoration + ItemTouchHelper, dan `refreshSidebar()` (di `onResume()`) hanya update data adapter. `getAdapterPosition()` diganti `getBindingAdapterPosition()`.
- **Battery Stats overlay berkedip (flickering)** — Pembacaan data baterai (`registerReceiver` + `readSysfs`) dilakukan di main thread yang memblok UI thread dan menyebabkan micro-stutter/flicker. Kini pembacaan dipindah ke background thread terpisah, dan UI hanya diupdate dari main thread saat data baru tersedia.
- **Switch "Tampilkan panel Info Memori" di Settings tidak menghentikan overlay & background monitor** — Saat switch dimatikan, panel tersembunyi dari sidebar namun overlay Memory dan background monitor tetap berjalan. Kini saat switch OFF, overlay dihentikan via `FloatingService.stopModule()`, `MemoryConfig.enabled` diset `false`, dan `MemoryMonitor.stop()` dipanggil jika background monitor aktif.
- **Panel Memory/Debugging masih terlihat & bisa diakses saat switch panel dimatikan** — Saat switch "Tampilkan panel" di Settings dimatikan, panel Memory atau Debugging yang sedang aktif tetap terlihat di UI dan masih bisa diakses bahkan setelah menutup & membuka ulang aplikasi. Kini saat switch OFF, panel aktif otomatis dialihkan ke Floating Text, dan `onResume()` mengecek apakah panel aktif masih valid sebelum menampilkannya.

### 🗒️ File Added
- 2026-08-19 17:59 — `MemoryOrderZonesView.java` — Custom view zona drag chip dua zona (Aktif/Nonaktif) untuk urutan & visibilitas item Memory Stats

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
- 2026-08-19 18:18 — `MemoryPanelController.java` — Ganti 4 CheckBox (Heap, Native, Graphics, Total) dengan `MemoryOrderZonesView`, hapus listener individual, tambah `setupOrderZones()` + `onOrderChanged()` callback
- 2026-08-19 18:18 — `panel_memory.xml` — Ganti 4 CheckBox dalam 2 LinearLayout dengan `MemoryOrderZonesView` (`memoryOrderZones`)
- 2026-08-19 18:35 — `MainActivity.java` — Pisahkan `rebuildSidebar()` jadi `initSidebar()` (setup infrastruktur sekali) + `refreshSidebar()` (update data di `onResume()`); tambah `setItems()` di SidebarAdapter; ganti `getAdapterPosition()` → `getBindingAdapterPosition()`
- 2026-08-19 20:13 — `SettingsActivity.java` — Tambah broadcast `ACTION_PANEL_VISIBILITY_CHANGED` saat switch Debugging/Memory di-off, termasuk tombol Relock
- 2026-08-19 20:13 — `MainActivity.java` — Daftarkan `panelVisibilityReceiver` untuk alihkan panel aktif ke "text" saat panel disembunyikan; tambah pengecekan di `onResume()` agar panel tidak valid otomatis dialihkan

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
