# [4.89.0] 2608271344 187 ***ONGOING***
### ✨ Fitur Baru
- **Changelog versi lama kini bisa dibaca di dalam aplikasi** — Daftar halaman Dokumentasi bertambah entri OLD-CHANGELOG tepat di bawah CHANGELOG, berisi arsip catatan perubahan versi-versi sebelumnya yang sudah digabung; tampil dengan pembaca markdown dan kontrol zoom yang sama seperti dokumen lainnya.
- **Modul Crosshair: overlay bidikan untuk game** — Panel Crosshair yang semula placeholder kini berisi modul overlay sungguhan dengan 44 gaya bidikan siap pilih dari galeri thumbnail horizontal yang bisa digeser (HorizontalScrollView, cell 48dp). Warna bidikan, preview 56dp, dan warna shadow ditampilkan dalam satu baris di atas gallery (warna | preview | shadow) dipisah divider dari grid thumbnail. Preview selalu diwarnai sesuai warna bidikan aktif (PorterDuff SRC_IN) sehingga pengguna langsung melihat bagaimana tiap gaya terlihat dengan warna yang dipilih. Ukuran bisa diatur 4–160dp lewat slider atau ketuk labelnya untuk mengetik angka persis, opasitas 10–100% agar bidikan bisa dibuat transparan (labelnya juga bisa diketuk), dan rotasi 0–359° langsung diterapkan ke overlay. Shadow (warna, blur, offset) diterapkan tanpa memperkecil bidikan — ukuran view otomatis membesar menyesuaikan kebutuhan shadow. Posisi diatur bebas tiga cara: geser langsung bidikannya di layar, slider X/Y, atau tombol panah D-Pad untuk geser halus perlahan. Posisi yang terpasang mengacu ke titik tengah bidikan (bukan pojok gambarnya), tersimpan otomatis terpisah untuk mode tegak & mendatar, dan muncul kembali sendiri saat HP direstart bila dibiarkan aktif. Opsi "Kunci Posisi" membuat sentuhan menembus ke game (bidikan tak ikut tergeser), opsi "Area Aman" menjaga bidikan tidak melolos keluar tepi layar. Bidikan selalu diwarnai otomatis lewat color picker tanpa perlu switch aktivasi; lengkap dengan section Shadow (warna, blur, offset). Seluruh konfigurasi bisa disimpan & dimuat sebagai preset khusus Crosshair dari menu Muat Preset. Status modul aktif ikut tampil di notifikasi layar mengambang.
- **Ketuk grafik baterai membuka halaman detail fullscreen** — Kelima grafik tab Monitor kini bisa diketuk membuka halaman detail ala aplikasi pemantau baterai: Persentase di kartu Metrik Real-Time serta Suhu, Daya, Tegangan & Arus di kartu Grafik Riwayat. Tiap halaman berisi grafik besar interaktif dengan crosshair yang mengikuti jari (garis vertikal + titik penebal + gelembung nilai & jam titik terpilih) dan tetap menempel setelah jari dilepas, baris label rentang waktu 5m–48j yang berada di atas grafik dan bisa diketuk maupun digeser lewat slider transparan (independen dari slider panel monitor; rentang awal mengikuti rentang aktif panel), serta kartu statistik Min / Max / Rata-rata / Δ dari data periode yang tampil. Header halaman beraksen warna metrik dengan subjudul nilai terkini; data disegarkan tiap 5 detik dan ditunda saat jari sedang menelusuri grafik.
- **Zoom & pan pada grafik halaman detail** — Grafik besar di halaman detail bisa diperbesar lewat slider zoom di bawah grafik (full-width, thumb kontras & garis track abu-abu agar mudah digeser; max zoom tergantung rentang, misal 30 detik pada rentang 5 menit), digeser ke kiri/kanan (pan) saat zoom-in dengan satu jari jika mode Pan aktif, dan diketuk dua kali (double-tap) untuk langsung kembali ke tampilan normal. Mode interaksi dipilih via tombol teks ("Tracker Grafik" / "Geser Grafik") di header halaman: mode Crosshair membuat satu jari menjelajahi titik data (crosshair), mode Pan membuat satu jari menggeser grafik. Zoom otomatis direset saat pengguna mengganti rentang waktu.
- **Tombol reset posisi di modul Crosshair** — Tombol ikon panah refresh di sebelah kanan baris "Kontrol Posisi" pada panel Crosshair. Ketuk untuk langsung mengembalikan posisi overlay ke tengah layar (0.5, 0.5). Desain tombol siap diterapkan ke modul lain yang memiliki kontrol posisi.
- **Estimasi waktu pengisian & pengosongan baterai** — Kartu Metrik Real-Time tab Monitor kini menampilkan estimasi sisa waktu: "Est. Penuh" saat mengisi daya (berdasarkan laju rata-rata 10 menit terakhir) dan "Est. Habis" saat daya turun (berdasarkan laju rata-rata 30 menit terakhir). Estimasi otomatis hilang saat baterai penuh (100%) atau kosong (0%), serta saat data sampel belum cukup untuk menghitung laju.
- **Riwayat sesi pengisian baterai** — Kartu Kesehatan Baterai tab Monitor kini menampilkan daftar 5 sesi pengisian terakhir (format: waktu mulai–selesai, durasi, delta persen, jenis colokan). Riwayat dibangun dari data sampel mentah di database sehingga tidak terpengaruh tombol Reset Data Estimasi. Section riwayat otomatis tersembunyi jika belum ada sesi yang tercatat.
- **Slider ukuran tanggal terpisah di modul Jam Digital** — Section Tampilan panel Jam Digital kini punya dua slider: "Ukuran Teks" untuk jam dan "Ukuran Tanggal" untuk baris tanggal di bawahnya. Pengguna bisa mengatur ukuran tanggal secara independen tanpa mengubah ukuran jam. Teks tanggal selalu rata tengah terhadap baris jam meskipun ukuran berbeda. Nilai tersimpan persisten dan ikut dalam preset.
- **Activity Timeline Bar di kartu Grafik Riwayat** — Bar horizontal warna-warni di bawah grafik Persentase yang menampilkan status aktivitas perangkat seiring waktu: abu gelap untuk layar mati, ungu untuk layar aktif, dan hijau untuk charging. Data dicatat otomatis setiap kali status berubah (screen on/off, mulai/berhenti charging) sehingga pengguna bisa melihat pola penggunaan perangkat yang sebenarnya seiring waktu.

### ♻️ Perubahan Fitur
- **Teks semua modul overlay rata tengah** — Teks pada overlay Jam Digital, Floating Text, FPS Display, Battery Info, Network Speed, dan Memory Stats kini rata tengah (center horizontal) secara default, sehingga tampilan lebih konsisten dan rapi terutama saat ukuran teks atau konten berubah.
- **Garis grafik Suhu berwarna gradien mengikuti nilai suhu** — Pada grafik Suhu (kartu Grafik Riwayat maupun halaman detail), warna tiap segmen garis kini mengikuti suhu titik datanya sebagai gradien mulus: 5°C ke bawah putih, 5–24° memutih memudar ke ice blue (makin dingin makin putih), 25–34° ice blue → hijau, 35–39° hijau → oranye, 40–45°+ oranye → merah. Titik crosshair di halaman detail ikut mewarisi warna suhu titik yang dipilih.
- **Garis grafik Persentase/Tegangan/Arus berwarna sesuai status pengisian baterai** — Pada ketiga grafik itu (kartu Metrik Real-Time maupun halaman detail), garis kini digambar dua warna dalam satu garis nyambung: segmen saat baterai sedang mengisi digambar hijau, segmen saat tidak mengisi digambar merah; warna berganti tepat di titik data tempat status baterai berubah. Titik crosshair di halaman detail ikut mewarisi warna status titik yang dipilih. Grafik Daya tidak berubah.
- **Kunci password jadi "Fitur Developer" yang mengatur panel Info Memori, Debugging, dan Salin/Simpan Snapshot** — Proteksi password yang sebelumnya hanya menempel pada switch panel Debugging kini menjadi satu saklar "Fitur Developer" di halaman Konfigurasi: label statusnya berganti "Fitur Developer • Terkunci" (merah) / "• Terbuka" (hijau), dengan kolom kunci + tombol Unlock/Kunci Ulang tepat di bawahnya. Saat Terkunci, switch "Tampilkan panel Info Memori" & "Tampilkan panel Debugging" tidak bisa dinyalakan; tombol Kunci Ulang mematikan kedua panel sekaligus (overlay Memory yang berjalan ikut berhenti, panel aktif dialihkan, keduanya hilang dari sidebar). Tombol Salin & Simpan Snapshot di tab Monitor Battery Info juga terkunci — tampak buram dan tidak bisa ditekan sampai Fitur Developer dibuka, dicek ulang setiap tab Monitor ditampilkan. Section "Modul" di halaman Konfigurasi diganti nama jadi "Developer".
- **Grafik riwayat Suhu/Daya/Tegangan/Arus tersusun grid 2×2** — Empat grafik di kartu Grafik Riwayat yang sebelumnya tampil utuh empat baris ke bawah kini disusun seperti tabel dua kolom: baris atas Suhu & Daya, baris bawah Tegangan & Arus. Tiap sel tetap punya sub-header aksen warna sendiri dan menggambar elemen yang sama persis dengan grafik level baterai (label sumbu Y max/tengah/min, label waktu awal–akhir, nilai terkini di ujung garis); tinggi tiap grafik 140→130dp. Divider pemisah antar grafik diganti jarak antar sel grid. Chevron pelipat kartu dihapus — isi kartu kini selalu tampil karena grid tidak lagi memakan ruang layar.
- **D-Pad posisi pakai pixel bukan normalisasi** — Tombol panah D-Pad yang sebelumnya bergerak 0.01 (normalisasi 0–1, setara ~10–23 px bergantung resolusi) kini bergerak dalam satuan pixel mentah (1–20 px per langkah). Tiga tombol di baris atas: ➖ menurunkan interval, 🔼 geser posisi ke atas, ➕ menaikkan interval. Ketuk angka interval di tengah untuk memunculkan popup daftar vertikal 1–20 px (maksimal 5 item terlihat, scroll untuk sisanya). Nilai default 2 px, berlaku global untuk semua modul.
- **Rentang waktu grafik riwayat baterai: interval 2m & 15m dihapus, 36j & 48j ditambahkan** — Pilihan rentang pada kartu Grafik Riwayat tab Monitor maupun halaman detail grafik kini: 5m/10m/30m/1j/3j/6j/12j/24j/36j/48j. Interval 2 Menit & 15 Menit dihapus; 36 Jam & 48 Jam ditambahkan di ujung kanan.
- **Overlay Battery Info: posisi default berubah ke kiri bawah** — Posisi awal overlay bergeser dari (0.05, 0.8) ke (0.04, 1.0) agar menempel di pojok kiri bawah layar. Pengguna yang sudah pernah menggeser posisi tidak terdampak (tersimpan di prefs).
- **Battery Strip: ketebalan default 8→2px, skema warna default Hue Gradien** — Strip baterai tampil lebih tipis secara default; skema warna level berubah dari Tanpa Skema menjadi Hue Gradien sehingga warna strip otomatis gradien merah→kuning→hijau→biru mengikuti level baterai.
- **Dialog Reset Data Estimasi Baterai perlu konfirmasi ketik "RESET"** — Tombol Reset di dialog kini berwarna abu-abu dan tidak bisa ditekan sampai pengguna mengetik "RESET" tepat di kolom input yang tersedia; mencegah reset tidak sengaja.

### 🔧 Optimasi & Penyesuaian
- **Pencatatan sampel baterai dirapatkan ke ±1 detik di semua kondisi** — Interval sampling monitor baterai kini ±1 detik baik saat mengisi daya, layar menyala, maupun layar mati, menggantikan pola sebelumnya (±5 detik saat mengisi/layar nyala, ±30 detik layar mati) sehingga aktivitas & riwayat tercatat lebih detail dan presisi.
- **Angka nilai di ujung garis grafik diberi chip latar agar tetap terbaca** — Teks nilai terkini di ujung garis pada kelima grafik baterai (panel maupun halaman detail) kini berada di atas chip kecil gelap transparan dengan angka putih, sehingga tetap terbaca saat garis-garis grafik menumpuk tanpa menutupi bentuk grafik di bawahnya; sebelumnya teks abu polos yang mudah tertimpa garis. Posisi chip otomatis membalik ke bawah titik saat grafik mepet batas atas.
- **Pemilih rentang waktu grafik jadi baris label yang bisa diketuk & digeser** — Baris lima radio button (5 Menit/15 Menit/1 Jam/6 Jam/24 Jam) diganti deretan label singkat 5m/10m/30m/1j/3j/6j/12j/24j/36j/48j yang berperilaku layaknya tombol: slider transparan menimpa baris label sehingga ketuk sebuah label langsung memilih interval itu dan menggeser jari pindah antar interval, tiap langkah slider jatuh tepat di tengah labelnya. Garis pembatas kecil di bawah label dihapus; label aktif disorot tebal berwarna sebagai indikator posisi. Label rentang aktif tetap tampil di header kartu; saat label diketuk/digeser pratinjau ikut berubah, data grafik baru di-query saat jari dilepas (default 5 Menit).
- **Kode tab Monitor Battery Info dipecah menjadi beberapa file** — BatteryPanelController yang menampung seluruh panel (1.020 baris) kini hanya mengurus tab Overlay (±560 baris); isi tab Monitor dipindah utuh ke empat controller terpisah: BatteryMonitorTabController (ring gauge, metrik real-time, polling 1 detik & tombol Salin/Simpan Snapshot), BatteryChartHistoryController (kartu Grafik Riwayat lengkap dengan slider rentang), BatteryHealthCardController (kartu Kesehatan Baterai + dialog kapasitas desain), dan BatterySnapshotExporter (pembuat teks salinan/berkas ekspor). Perilaku fitur tidak berubah.

### 🐞 Bug Fixes
- **Chevron "Tampilkan slider RGB" kini ikut berubah saat section dibuka/ditutup** — Ikon panah di header slider RGB yang selama ini selalu statis ▼ kini berganti ▲ ketika slider RGB dilipat dan kembali ▼ saat dibuka, di panel Color Picker maupun dialog color picker. Arah awal di dialog juga ikut diperbaiki — semula menampilkan ▼ padahal section dalam keadaan tertutup.
- **Warna status pengisian grafik riwayat kini benar di semua rentang** — Pada rentang 15 Menit ke atas, garis Persentase/Tegangan/Arus seluruhnya tergambar merah padahal baterai sedang mengisi daya (rentang 2–10 Menit normal). Kini kondisi pengisian tiap titik ikut terhitung pada rentang panjang sehingga segmen hijau/merah kembali sesuai keadaan aslinya hingga rentang 24 Jam.
- **Skala grafik Suhu default 35°C–40°C** — Batas atas skala selalu 40°C, batas bawah selalu 35°C agar variasi suhu tipis tetap terlihat jelas. Melebar otomatis hanya bila suhu melebihi 40°C atau turun di bawah 35°C.
- **Garis grafik riwayat terputus saat ada gap waktu lama** — Grafik Suhu/Persentase/Daya/Tegangan/Arus kini memutus garis saat jeda antar titik data melebihi 5 menit (misalnya ponsel mati karena baterai habis lalu dinyalakan kembali setelah dicharge). Sebelumnya garis selalu menyambung dari titik terakhir sebelum mati ke titik pertama setelah menyala, menciptakan ilusi kenaikan/penurunan bertahap yang tidak terjadi.

### 🗒️ File Added
- 2608271344 — `BatteryTimelineBarView.java` — Custom view baru: bar horizontal warna-warni menampilkan status aktivitas perangkat seiring waktu
- 2608261835 — `ic_reset.xml` — Ikon vector panah refresh untuk tombol reset posisi
- 2608261819 — `ic_minus.xml` — Ikon vector garis horizontal untuk tombol turunkan interval D-Pad
- 2608261819 — `ic_plus.xml` — Ikon vector tanda plus untuk tombol naikkan interval D-Pad
- 2608250050 — `ShadowImageView.java` — Custom ImageView padanan ShadowTextView: menggambar shadow (blur + offset) & background (warna, offset, margin, radius) di onDraw() untuk overlay bergambar
- 2608241058 — `crosshair_1.png` … `crosshair_44.png` — 44 aset gambar gaya bidikan (sumber `_schedule/drawable-nodpi-v4/chr_1..44.png`)
- 2608241100 — `CrosshairConfig.java` — Konfigurasi statis modul Crosshair
- 2608241101 — `CrosshairModule.java` — Modul overlay ImageView: posisi titik tengah, drag + safe area, apply style/opacity
- 2608241103 — `CrosshairPanelController.java` — UI panel Crosshair: grid 44 gaya, size/opacity, switch Material
- 2608241103 — `CrosshairPositionController.java` — Kontrol posisi Crosshair: slider X/Y + D-Pad + koordinat
- 2608241104 — `bg_style_item.xml`, `bg_style_item_selected.xml` — Sel grid gaya bidikan default & terpilih
- 2608240512 — `BatteryChartView.java` — Mode interaktif crosshair (garis vertikal + titik + gelembung nilai & jam) dengan flag default mati agar grafik panel tak berubah perilaku
- 2608240541 — `BatteryChartDetailActivity.java` — Halaman detail satu grafik: chart besar interaktif, baris label rentang independen 2m–24j dikontrol seekbar transparan, kartu statistik Min/Max/Rata-rata/Δ, refresh 5 detik yang ditunda saat crosshair aktif
- 2608240619 — `activity_battery_chart_detail.xml` — Layout halaman detail: header aksen metrik + subjudul nilai terkini, chart 220dp, baris label rentang dengan seekbar transparan overlay (tinggi menyesuaikan label), kartu statistik 4 kolom
- 2608240137 — `BatteryChartHistoryController.java` — Controller kartu Grafik Riwayat tab Monitor: binding 5 chart + pemilih rentang berbasis label & slider transparan (pratinjau saat digeser, query saat lepas jari), query database via executor latar
- 2608240032 — `BatteryHealthCardController.java` — Controller kartu Kesehatan Baterai tab Monitor: estimasi kapasitas, skor kesehatan berwarna, keyakinan, status pengumpulan, dialog kapasitas desain
- 2608240038 — `BatterySnapshotExporter.java` — Pembuat teks salinan clipboard & ekspor 20 snapshot terakhir ke folder Download (MediaStore/API lama), menyertakan catatan kesehatan baterai
- 2608240042 — `BatteryMonitorTabController.java` — Controller orkestrasi tab Monitor: ring gauge, dua kolom metrik real-time, badge kondisi suhu, polling 1 detik hanya saat tab tampil, memegang tiga controller di atas

### ✏️ File Changed
- 2608271344 — `BatteryHistoryDb.java` — Naikkan DB_VERSION ke 2; tambah tabel activity_log (time, status) + index; tambah method insertActivityLog() & queryActivityLog()
- 2608271344 — `BatteryMonitor.java` — Log status perubahan (screen on/off, charging) ke activity_log saat status berubah
- 2608271344 — `panel_battery.xml` — Tambah BatteryTimelineBarView + legenda warna (Mati/Aktif/Charging) di bawah grafik Persentase
- 2608271344 — `BatteryChartHistoryController.java` — Bind BatteryTimelineBarView, query activity_log saat refresh()
- 2608271308 — `TextModule.java` — Tambah setGravity(CENTER_HORIZONTAL) agar teks overlay rata tengah
- 2608271308 — `FpsModule.java` — Sama: tambah setGravity(CENTER_HORIZONTAL)
- 2608271308 — `BatteryStatsModule.java` — Sama: tambah setGravity(CENTER_HORIZONTAL)
- 2608271308 — `NetworkModule.java` — Sama: tambah setGravity(CENTER_HORIZONTAL)
- 2608271308 — `MemoryModule.java` — Sama: tambah setGravity(CENTER_HORIZONTAL)
- 2608271254 — `ClockModule.java` — Gunakan SpannableString dengan AbsoluteSizeSpan untuk ukuran tanggal terpisah; tambah method updateDateSize(); tambah setGravity(CENTER_HORIZONTAL)
- 2608271254 — `panel_clock.xml` — Tambah slider "Ukuran Tanggal" (clockDateSizeSeekBar, range 6–100) di bawah slider Ukuran Teks
- 2608271254 — `ClockPanelController.java` — Bind slider tanggal, load/save pref clock_date_size, listener + SliderLabelEditor
- 2608271254 — `ClockPositionController.java` — Save/load dateSize di preset + syncToService()
- 2608271254 — `OverlayPreset.java` — Tambah field dateSize
- 2608271254 — `MainActivity.java` — Load pref clock_size dan clock_date_size saat startup
- 2608271254 — `ClockConfig.java` — Tambah field dateSize = 10f
- 2608271241 — `BatteryChartView.java` — Tambah konstanta GAP_THRESHOLD_MS (5 menit) untuk deteksi gap waktu; memutus garis grafik saat jeda > 5 menit (ponsel mati/lama tidak aktif); ubah batas default grafik Suhu dari 33–43°C ke 35–40°C
- 2608271219 — `activity_battery_chart_detail.xml` — Ubah RadioGroup/RadioButton jadi LinearLayout/TextView teks button "Tracker Grafik"/"Geser Grafik"
- 2608271219 — `BatteryChartDetailActivity.java` — Hapus import RadioGroup/RadioButton; ganti binding ke TextView dengan click listener manual + logic warna aktif/nonaktif
- 2608271103 — `activity_battery_chart_detail.xml` — Tambah RadioGroup (Crosshair/Pan) di header + SeekBar zoom di antara chart dan label rentang
- 2608271119 — `activity_battery_chart_detail.xml` — Pindah baris label rentang ke atas grafik; slider zoom jadi full-width (hapus margin kiri/kanan)
- 2608271127 — `colors.xml` — Tambah bat_slider_thumb & bat_slider_track (tema terang)
- 2608271127 — `values-night/colors.xml` — Tambah bat_slider_thumb & bat_slider_track (tema gelap)
- 2608271127 — `activity_battery_chart_detail.xml` — Slider zoom: thumbTint → bat_slider_thumb, progressBackgroundTint → bat_slider_track (track terlihat)
- 2608271103 — `BatteryChartDetailActivity.java` — Bind RadioGroup ke chartView.setMode(), bind zoom SeekBar ke chartView.setZoomLevel(), reset zoom SeekBar saat rentang diganti
- 2608270940 — `BatteryHistoryDb.java` — Tambah method estimateTimeRemaining() untuk kalkulasi estimasi waktu penuh/habis dari laju sampel; tambah class ChargingSession & method queryChargingSessions() untuk analisis sesi pengisian dari data sampel mentah
- 2608270940 — `BatteryMonitorTabController.java` — Tampilkan "Est. Penuh" atau "Est. Habis" di grid metrik real-time berdasarkan status charging/discharging; tambah helper formatDuration() untuk format waktu ke jam/menit
- 2608270940 — `BatteryHealthCardController.java` — Tambah section riwayat 5 sesi pengisian terakhir (waktu, durasi, delta%, colokan) di kartu Kesehatan Baterai; query di-cache 30 detik agar tidak berat
- 2608270940 — `panel_battery.xml` — Tambah TextView batSessionHistoryText di kartu Kesehatan Baterai untuk menampilkan riwayat sesi pengisian
- 2608270146 — `old-CHANGELOG.md` — Entry v4.85.1–4.86.0 dipindah dari CHANGELOG.md
- 2608270136 — `BatteryHealthCardController.java` — Tambah kolom konfirmasi teks di dialog Reset agar tombol Reset hanya aktif jika user mengetik "RESET"
- 2608270109 — `BatteryCapacityEstimator.java` — Ganti metode estimasi dari selisih chargeMah ke akumulasi arus × waktu (currentMa × dt); tambah method resetEstimationData()
- 2608270109 — `panel_battery.xml` — Tambah tombol Reset sejajar dengan tombol Salin dan Simpan
- 2608261912 — `BatteryChartView.java` — Hapus WINDOW_2M & WINDOW_15M; tambah WINDOW_36H & WINDOW_48H; skala default Suhu: max 45→43°C, tambah min tetap 33°C
- 2608261912 — `BatteryChartHistoryController.java` — Update CHART_WINDOWS/labels: hapus 2m/15m, tambah 36j/48j; komentar padding diperbarui
- 2608261912 — `BatteryStatsConfig.java` — Default posX 0.05→0.04, posY 0.8→1.0
- 2608261912 — `BatteryStatsModule.java` — Fallback defaults posX/posY menyesuaikan config
- 2608261912 — `BatteryBarConfig.java` — Default thickness 8→2px, colorScheme SCHEME_NONE→SCHEME_HUE
- 2608261912 — `panel_battery.xml` — Tick label grafik riwayat: hapus 2m/15m, tambah 36j/48j
- 2608261912 — `activity_battery_chart_detail.xml` — Tick label rentang: hapus 2m/15m, tambah 36j/48j
- 2608261912 — `BatteryChartDetailActivity.java` — Komentar padding diperbarui
- 2608260759 — `DpadController.java` — Ganti step normalisasi 0.01 jadi pixel-based 1–20 px, tambah center view dengan popup tap ubah interval, konversi pixel ke normalisasi pakai screenWidth/screenHeight; popup muncul di tengah interval, limit 5 item; tambah tombol −/+ untuk naik/turunkan interval
- 2608260759 — `TextPositionController.java` — Pass dpadCenter + displayWidth/displayHeight ke DpadController
- 2608260759 — `FpsPositionController.java` — Pass dpadCenter + btnMinus/btnPlus + displayWidth/displayHeight ke DpadController
- 2608260759 — `ClockPositionController.java` — Sama: pass dpadCenter + btnMinus/btnPlus + displayWidth/displayHeight
- 2608260759 — `BatteryPositionController.java` — Sama: pass dpadCenter + btnMinus/btnPlus + displayWidth/displayHeight
- 2608260759 — `BatteryBarPositionController.java` — Sama: pass dpadCenter + btnMinus/btnPlus + displayWidth/displayHeight
- 2608260759 — `MemoryPositionController.java` — Sama: pass dpadCenter + btnMinus/btnPlus + displayWidth/displayHeight
- 2608260759 — `NetworkPositionController.java` — Sama: pass dpadCenter + btnMinus/btnPlus + displayWidth/displayHeight
- 2608260759 — `CrosshairPositionController.java` — Sama: pass dpadCenter + btnMinus/btnPlus + displayWidth/displayHeight
- 2608261835 — `CrosshairPositionController.java` — Tambah tombol reset posisi (bind + handler onPositionChanged ke center 0.5f, 0.5f)
- 2608260759 — `panel_text.xml` — Space tengah D-Pad diganti TextView dpadInterval; tambah tombol −/+ di baris atas
- 2608260759 — `panel_fps.xml` — Sama: Space → TextView fps_dpadInterval; tambah tombol −/+
- 2608260759 — `panel_clock.xml` — Sama: Space → TextView clock_dpadInterval; tambah tombol −/+
- 2608260759 — `panel_battery.xml` — Sama: dua Space → TextView battery/batbar_dpadInterval; tambah tombol −/+ di kedua D-Pad
- 2608260759 — `panel_memory.xml` — Sama: Space → TextView mem_dpadInterval; tambah tombol −/+
- 2608260759 — `panel_network.xml` — Sama: Space → TextView network_dpadInterval; tambah tombol −/+
- 2608260759 — `panel_crosshair.xml` — Sama: Space → TextView crosshair_dpadInterval; tambah tombol −/+
- 2608261835 — `panel_crosshair.xml` — Tambah tombol reset posisi (ImageButton) di baris Kontrol Posisi
- 2608260637 — `ShadowImageView.java` — Pre-render shadow bitmap via BlurMaskFilter pada Canvas offscreen saat config berubah; tidak perlu LAYER_TYPE_SOFTWARE lagi; onDraw tinggal gambar cache shadow + crosshair di atasnya
- 2608260637 — `CrosshairModule.java` — Tambah updatePosition() setelah expandForShadow() agar posisi crosshair tetap di tengah saat ukuran view berubah karena shadow
- 2608260315 — `ShadowImageView.java` — Tambah getShadowPadExtra() untuk akses padding shadow dari module
- 2608260315 — `CrosshairModule.java` — Shadow expand view params agar reticle tidak menyusut (sizePx + shadowPadExtra*2), terapkan di start(), updateSize(), applyShadow()
- 2608260315 — `panel_crosshair.xml` — Susun ulang: warna|preview|shadow di atas gallery, divider di antara, preview 56dp
- 2608260244 — `CrosshairPanelController.java` — Gallery horizontal scroll (LinearLayout 48dp, margin 6dp), preview berwarna, hapus checkbox Warnai Bidikan & section Background, min size 4dp, listener rotasi + SliderLabelEditor
- 2608260244 — `CrosshairModule.java` — Tambah applyRotation() + terapkan saat module start
- 2608260244 — `CrosshairConfig.java` — Default colorEnabled true (warna selalu aktif)
- 2608260244 — `MainActivity.java` — colorEnabled selalu true
- 2608260244 — `bg_style_item_selected.xml` — Background selected semi-transparan accent (#3326C6DA)
- 2608250153 — `panel_crosshair.xml` — Baris preview Warna/Shadow/Background, switch "Warnai Bidikan", section Shadow & Background collapsible, label preset aktif
- 2608250147 — `CrosshairPanelFragment.java` — Teruskan Muat Preset dari menu/PanelManager ke controller
- 2608250140 — `CrosshairPanelController.java` — Binding & listener lengkap: warna bidikan + shadow + background (ColorPickerDialog), slider label edit Opasitas/Shadow/Background, checkbox tint via applyCheckboxTint
- 2608250133 — `CrosshairPositionController.java` — Integrasi PresetHandler penuh: delegate moduleType "crosshair", simpan/muat preset gaya+opasitas+warna+shadow+background+posisi, label preset aktif
- 2608250126 — `MainActivity.java` — Muat prefs baru Crosshair: warna, tint aktif, shadow & background
- 2608250112 — `CrosshairModule.java` — View ganti ke ShadowImageView; apply warna/shadow/background; updateColor/updateShadow/updateBackground terhubung
- 2608250105 — `CrosshairConfig.java` — Field colorEnabled, color, shadow, background
- 2608242256 — `activity_documentation.xml` — Tambah entri daftar dokumen OLD-CHANGELOG setelah CHANGELOG
- 2608242256 — `DocumentationActivity.java` — Buka arsip `old-CHANGELOG.md` dari assets saat entri OLD-CHANGELOG diketuk
- 2608242229 — `ColorPickerPanelController.java` — Chevron header slider RGB ikut diganti ▲/▼ saat section dilipat/dibuka
- 2608242229 — `ColorPickerDialog.java` — Sama: chevron header slider RGB ikut diganti saat header diketuk
- 2608242229 — `dialog_color_picker.xml` — Arah awal chevron RGB disesuaikan keadaan tertutup (▲)
- 2608242111 — `BatteryMonitor.java` — Interval sampling saat mengisi daya dilonggarkan ±1 dtk → ±5 dtk
- 2608242112 — `BatteryHistoryDb.java` — Query grafik jalur agregasi kini ikut menghitung status pengisian per bucket agar warna garis rentang panjang benar
- 2608241138 — `panel_crosshair.xml` — Placeholder "coming soon" diganti panel lengkap mengikuti pola panel overlay lain (switch + Kunci Posisi, section Tampilan & Posisi collapsible)
- 2608241106 — `CrosshairPanelFragment.java` — Memasang controller panel & siklus hidup seperti panel lain
- 2608241107 — `FloatingService.java` — Registrasi CrosshairModule: field, accessor statis, ensure, start saat layanan hidup
- 2608241107 — `BootReceiver.java` — Pulihkan status aktif Crosshair saat boot & ikutkan dalam cek modul aktif
- 2608241108 — `NotificationHelper.java` — Crosshair ikut menandai notifikasi ada modul aktif ("Xhair")
- 2608241108 — `MainActivity.java` — Muat konfigurasi Crosshair dari preferensi, ikutkan dalam cek modul aktif, label sidebar "(coming soon)" dihapus
- 2608241138 — `CrosshairPanelController.java` — Switch Material diganti CheckBox, section Gaya Bidikan jadi Tampilan; kontrol Kunci Posisi pindah ke atas bersama switch, Area Aman pindah ke section Posisi
- 2608241138 — `colors.xml`, `values-night/colors.xml` — Warna latar sel galeri gaya bidikan & aksen Crosshair (#26C6DA) untuk tema terang & gelap
- 2608240848 — `panel_debuging.xml` — Isi preview ikon rotasi (judul, sub-judul, grid 5 ikon, info) diganti placeholder "coming soon" dua baris
- 2608240848 — `DebugingPanelFragment.java` — Logika klik ikon varian & Toast dihapus; fragment kembali hanya inflate layout
- 2608240831 — `BatteryChartView.java` — Alpha chip latar nilai terkini diturunkan (0xCC → 0x66) agar tidak lagi menutupi tampilan grafik di bawahnya
- 2608240758 — `BatteryChartView.java` — Garis grafik Suhu diwarnai gradien per segmen mengikuti nilai suhu (putih→ice blue→hijau→oranye→merah); titik crosshair ikut warna titik terpilih
- 2608240758 — `colors.xml`, `values-night/colors.xml` — Tambah warna anchor ice blue gradien Suhu (#33F1FF) untuk tema terang & gelap
- 2608240739 — `BatteryChartView.java` — Garis grafik Persentase/Tegangan/Arus diwarnai per segmen sesuai status pengisian (hijau isi / merah tidak); titik crosshair ikut warna titik terpilih
- 2608240732 — `colors.xml`, `values-night/colors.xml` — Tambah pasangan warna garis charging/discharging (hijau/merah) untuk tema terang & gelap
- 2608240718 — `BatteryChartHistoryController.java` — Klik grafik Suhu, Daya, Tegangan & Arus membuka halaman detail metrik terkait (rentang awal mengikuti slider panel)
- 2608240619 — `panel_battery.xml` — Pemilih rentang Grafik Riwayat: garis pembatas di bawah label dihapus, slider jadi transparan dan diposkan menimpa baris label agar label bisa diketuk/digeser layaknya tombol, tinggi area menyesuaikan baris label (tanpa ruang sisa), label interval diperbesar
- 2608240604 — `BatteryChartHistoryController.java` — Padding dinamis 5% lebar pada slider transparan supaya tiap langkah jatuh tepat di tengah labelnya
- 2608240530 — `AndroidManifest.xml` — Daftarkan halaman detail grafik ke manifest
- 2608240528 — `BatteryMonitorTabController.java` — Ketuk grafik Persentase membuka halaman detail dengan rentang awal mengikuti slider panel
- 2608240526 — `BatteryChartHistoryController.java` — Buka akses daftar pilihan rentang & getter rentang aktif untuk halaman detail
- 2608240449 — `BatteryChartView.java` — Batas atas tetap skala Y grafik Suhu diturunkan 50°C → 45°C; skala tetap melebar otomatis bila suhu melebihi 45°
- 2608240217 — `activity_settings.xml` — Section "Modul" jadi "Developer": label status "Fitur Developer • Terkunci/Terbuka" + kolom kunci & tombol Unlock/Kunci Ulang dipindah ke atas, switch Info Memori ikut disabled default
- 2608240217 — `SettingsActivity.java` — Logika Fitur Developer: satu pref kunci untuk kedua switch panel, Kunci Ulang mematikan Memory overlay/monitor + broadcast pengalihan panel, status unlock pindah ke label berwarna (pesan kunci salah via Toast)
- 2608240217 — `BatteryMonitorTabController.java` — Tombol Salin & Simpan Snapshot disabled + buram saat Fitur Developer terkunci, dicek ulang tiap tab Monitor ditampilkan
- 2608240107 — `panel_battery.xml` — Kartu Grafik Riwayat: RadioGroup 5 pilihan diganti slider full width tanpa label statis + deretan pembatas berlabel singkat 2m–24j di atasnya; chevron pelipat kartu dihapus (isi selalu tampil); empat grafik disusun grid 2×2 (baris 1: Suhu/Daya, baris 2: Tegangan/Arus) dengan sub-header aksen per sel, tinggi chart 130dp, divider antar chart dihapus
- 2608240107 — `BatteryChartView.java` — Konstanta window baru WINDOW_2M, WINDOW_10M, WINDOW_30M, WINDOW_3H, WINDOW_12H; skala Y Suhu maksimal tetap 50°C dengan batas bawah otomatis mengikuti data
- 2608240046 — `BatteryPanelController.java` — Seluruh isi tab Monitor dipindah ke controller terpisah; kini hanya mengurus tab Overlay (1.020 → ±560 baris), membuat BatteryMonitorTabController di konstruktor dan mendelegasikan onPanelShown/onPanelHidden/cleanup/refresh awal

### 🔥 File Removed
- 2608240848 — `ic_rotation_variant_1.xml`–`ic_rotation_variant_5.xml` — Lima drawable ikon varian rotasi tak terpakai setelah isi panel Debugging dikosongkan

---

# [4.88.0] 2608232156 185 ***PUSH***
### ✨ Fitur Baru
- **Tab Monitor di panel Battery Info — monitoring real-time baterai perangkat dengan ring gauge melingkar** — Tab Monitor (sebelumnya placeholder) kini berisi pemantauan baterai ala aplikasi Battery Guru/AccuBattery dalam satu kartu Metrik Real-Time: ring gauge melingkar di sebelah kiri yang arc-nya berwarna gradien hue mengikuti level baterai (skema warna Battery Strip), dengan level %, kapasitas tersisa (mAh), dan status pengisian singkat (Charging•AC/USB/Wireless, Full, Discharging) tampil di dalam lingkaran; grafik riwayat Persentase berada di sebelah kanan ring, grid monospace suhu/voltase/arus/daya/cycle count/teknologi baterai berjajar di bawahnya; badge kondisi suhu (Normal/Panas/Dingin) tampil di header kartu. Cycle count hanya tampil jika device melaporkan (API 34+/vendor tertentu).
- **Grafik Riwayat di tab Monitor panel Battery Info — satu kartu empat chart yang bisa dilipat** — Riwayat pemantauan digambar sebagai line chart via Canvas (tanpa library eksternal) dalam satu kartu Grafik Riwayat: pemilih rentang 5 Menit / 15 Menit / 1 Jam / 6 Jam / 24 Jam (berlaku global untuk semua chart) di bagian atas, lalu empat chart Suhu, Daya, Tegangan, dan Arus berjajar ke bawah dengan sub-header beraksen warna masing-masing yang dipisah divider. Chevron di header kartu untuk menyembunyikan/menampilkan seluruh isi grafik. Tiap chart punya sumbu Y sendiri dengan label nilai max/tengah/min di kiri (skala otomatis mengikuti data) dan angka nilai terkini di ujung garis; label sumbu waktu menyesuaikan rentang panjang (detik/menit/jam-tanggal). Data grafik bersumber dari database sehingga riwayat lama tetap terlihat walau aplikasi sempat ditutup.
- **Estimasi Kapasitas & Kesehatan Baterai di tab Monitor panel Battery Info** — Fitur ala AccuBattery: saat perangkat mengisi daya, aplikasi mencatat segmen pengisian (Δkapasitas mAh vs Δpersen) lalu menghitung estimasi kapasitas penuh per sesi. Estimasi lintas sesi diagregasi dengan median (tahan outlier) dan tersimpan persisten di database. Sesi dengan mayoritas waktu layar mati diberi prioritas dalam perhitungan median (hasil lebih akurat, butuh minimal 3 sesi layar-mati; jika belum ada, semua sesi dipakai). Kartu Kesehatan Baterai menampilkan: estimasi kapasitas, skor kesehatan (estimasi ÷ kapasitas desain × 100%, berwarna hijau ≥80% / oranye ≥50% / merah <50%), jumlah sesi tercatat, indikator keyakinan (jumlah sampel), dan status pengumpulan data real-time. Kapasitas desain (mAh) diinput manual via dialog ketuk — skor kesehatan baru muncul jika terisi. Segmen dengan Δpersen <5% atau durasi <1 menit otomatis dibuang agar estimasi tidak noise. Tombol Salin & Simpan Snapshot berada di dalam kartu ini; hasil salinan/berkas ekspornya menyertakan catatan kesehatan baterai (estimasi kapasitas, skor, jumlah sesi, keyakinan).
- **Pemantauan baterai full-aktif — merekam otomatis tanpa henti** — Monitor baterai kini selalu aktif tanpa tombol mulai/hentikan: overlay dipakai atau tidak, panel dibuka atau tidak, aplikasi ditutup sekalipun, pencatatan tetap berjalan lewat foreground service ringan tersendiri dengan notifikasi minimal prioritas rendah. Otomatis nyala saat aplikasi dibuka dan direstart otomatis saat boot. Sampling dinamis hemat baterai: rapat (±1 detik) hanya saat mengisi daya (presisi estimator), ±5 detik saat layar nyala idle, ±30 detik saat layar mati.
- **Database riwayat baterai lokal (SQLite)** — Riwayat metrik (time-series) dan sesi pengisian kini disimpan di database SQLite bawaan framework (tanpa Room/kapt/KSP): buffer memori grafik ±1 jam dan persistensi JSON dihapus. Kartu grafik, export/copy snapshot, dan estimasi kesehatan semuanya membaca dari database; tanpa auto-trim pembuang riwayat (ukuran per baris sangat kecil). Data JSON estimasi versi lama dimigrasikan otomatis sekali ke database lalu filenya dihapus.

### 🔧 Optimasi & Penyesuaian
- **Pembacaan data baterai disatukan ke satu sumber** — Overlay Battery Info dan tab Monitor kini membaca metrik dari util pembaca tunggal (`BatteryReading`), bukan masing-masing punya logika sendiri. Menghilangkan duplikasi rumus yang pernah membuat angka Daya overlay dan tab Monitor berbeda (8W vs 0,008W) karena salah faktor konversi.

### 🗒️ File Added
- 2608212045 — `BatteryMonitor.java` — Background monitor polling data baterai (snapshot metrik lengkap, riwayat 20 snapshot, helper status/kondisi)
- 2608212048 — `bat_card_bg.xml` — Drawable card tab Monitor Battery
- 2608212048 — `bat_badge_active_bg.xml`, `bat_badge_stopped_bg.xml` — Drawable badge status monitor
- 2608222041 — `BatteryChartView.java` — Custom view line chart Canvas satu metrik per instance (Suhu/Persen/Daya), label sumbu Y max/tengah/min di kiri, angka nilai terkini di ujung garis, rentang waktu 5 menit–1 jam, tanpa library eksternal
- 2608221858 — `BatteryCapacityEstimator.java` — Akumulasi estimasi kapasitas dari segmen pengisian daya + persistensi JSON internal storage
- 2608231216 — `BatteryReading.java` — Util pembaca metrik baterai tunggal (battery intent + property + sysfs fallback, konversi satuan, helper status/kondisi) untuk overlay & tab Monitor
- 2608231251 — `BatteryHistoryDb.java` — Database SQLite riwayat baterai (tabel sampel metrik time-series + sesi pengisian + meta, query per rentang dengan downsampling otomatis)
- 2608231251 — `BatteryMonitorService.java` — Foreground service ringan monitor baterai full-aktif (notifikasi minimal prioritas rendah, auto-start app & boot)
- 2608232001 — `BatteryRingView.java` — Custom view ring gauge baterai melingkar (track + arc gradien hue, teks level/kapasitas/status di dalam lingkaran)
- 2608232001 — `BatteryColors.java` — Helper warna bersama: rumus gradien hue baterai satu sumber untuk Battery Strip & ring gauge

### ✏️ File Changed
- 2608212050 — `BatteryStatsConfig.java` — Tambah field backgroundMonitor
- 2608212052 — `panel_battery.xml` — Ganti placeholder tab Monitor dengan UI monitoring lengkap (kartu level, metrik, status pengisian, toggle, switch latar belakang, salin/simpan snapshot)
- 2608212055 — `colors.xml` — Tambah 13 color values untuk tab Monitor Battery
- 2608212056 — `BatteryPanelController.java` — Tambah logika tab Monitor: polling update UI, toggle manual, switch latar belakang, badge status/kondisi, export/copy snapshot
- 2608212057 — `BatteryPanelFragment.java` — Panggil onPanelShown() controller di semua tab + onPanelHidden() saat panel disembunyikan
- 2608212058 — `FloatingService.java` — Start/stop BatteryMonitor sesuai backgroundMonitor di onCreate/stopAllModules/onDestroy, tambah setBackgroundBatteryMonitorEnabled(), cek backgroundMonitor di isAnyModuleActive()
- 2608212058 — `MainActivity.java` — Load pref bat_bg_monitor, cek BatteryStatsConfig.backgroundMonitor di isAnyModuleActive()
- 2608212058 — `NotificationHelper.java` — Cek backgroundMonitor di isAnyModuleActive, tambah label "BatMon" di teks modul aktif notifikasi
- 2608212058 — `BootReceiver.java` — Restore pref bat_bg_monitor saat boot
- 2608221835 — `BatteryMonitor.java` — Tambah buffer grafik ±1 jam (3600 titik, auto-trim, terpisah dari riwayat snapshot export, bertahan lintas start/stop pemantauan) + akses data & reset grafik
- 2608222041 — `panel_battery.xml` — Kartu Grafik Riwayat dipecah: kartu kontrol (rentang/Jeda/Reset) + tiga kartu chart terpisah untuk Suhu, Persentase, dan Daya; checkbox seri dihapus
- 2608221835 — `colors.xml` — Tambah 4 color values untuk grafik (garis suhu/persen/daya + grid)
- 2608222041 — `BatteryPanelController.java` — Binding & kontrol tiga kartu chart terpisah: pilih rentang (berlaku global), jeda/lanjut (sampling tetap jalan), reset dengan dialog konfirmasi; chart ikut diperbarui loop polling 1 detik
- 2608221858 — `BatteryMonitor.java` — Hook estimasi kapasitas: init estimator saat start, kirim sampel tiap polling, finalize segmen & simpan saat stop
- 2608221858 — `panel_battery.xml` — Tambah kartu Kesehatan Baterai di tab Monitor (teks hasil monospace, input kapasitas desain via ketuk, badge jumlah sesi, tombol Reset Data Estimasi)
- 2608221858 — `BatteryPanelController.java` — Kartu Kesehatan: refresh real-time (estimasi, skor berwarna, keyakinan, status), dialog input kapasitas desain (validasi 500–30000 mAh), reset data dengan konfirmasi
- 2608221926 — `values-night/colors.xml` — Varian gelap 17 warna tab Monitor Battery (kartu, badge, level bar, grafik) agar kartu tidak putih di mode gelap
- 2608231216 — `BatteryMonitor.java` — Logika pembacaan snapshot dipindah ke `BatteryReading`; helper status/kondisi jadi method pada Snapshot
- 2608231216 — `BatteryStatsModule.java` — Blok baca mandiri (battery intent + sysfs) diganti panggilan `BatteryReading.read()`, format teks overlay tidak berubah
- 2608231216 — `BatteryChartView.java` — Tipe data seri grafik mengikuti model `BatteryReading.Snapshot`
- 2608231216 — `BatteryCapacityEstimator.java` — Sumber sampel & cek status charging via `BatteryReading.Snapshot`
- 2608231216 — `BatteryPanelController.java` — Referensi tipe snapshot & pemanggilan helper status/kondisi menyesuaikan model baru
- 2608231251 — `BatteryMonitor.java` — Buffer memori riwayat 20 snapshot & grafik 3600 titik dihapus; sampel polling langsung masuk database; interval sampling dinamis (1 dtk charging / 10 dtk layar nyala / 60 dtk layar mati)
- 2608231251 — `BatteryCapacityEstimator.java` — Persistensi JSON diganti tabel sesi di database (migrasi file JSON lama sekali otomatis lalu dihapus); durasi segmen memakai delta waktu aktual; trim maksimum 60 sesi dibuang
- 2608231251 — `MainActivity.java` — Auto-start BatteryMonitorService saat aplikasi dibuka; jejak pref bat_bg_monitor dibersihkan
- 2608231251 — `BootReceiver.java` — BatteryMonitorService selalu direstart saat boot; pref bat_bg_monitor dibersihkan dari syarat restore overlay
- 2608231251 — `FloatingService.java` — Lifecycle monitor baterai dilepas dari service overlay (start/stop, setBackgroundBatteryMonitorEnabled, cek isAnyModuleActive)
- 2608231251 — `NotificationHelper.java` — Cek backgroundMonitor & label "BatMon" pada notifikasi dibuang
- 2608231251 — `BatteryStatsConfig.java` — Field backgroundMonitor dihapus
- 2608231251 — `panel_battery.xml` — Kontrol manual tab Monitor dihapus (tombol Mulai/Hentikan, switch Latar Belakang, badge status, tombol Jeda/Reset Grafik, tombol Reset Data Estimasi); pilihan rentang grafik diperluas 6 Jam & 24 Jam
- 2608231251 — `BatteryPanelController.java` — Seluruh listener kontrol manual dihapus; grafik digambar dari query database per rentang secara background thread; export snapshot membaca 20 sampel terakhir dari database
- 2608231251 — `BatteryChartView.java` — Konstanta rentang 6 Jam & 24 Jam; format label sumbu waktu adaptif (HH:mm:ss / HH:mm / dd/MM HH:mm)
- 2608231251 — `AndroidManifest.xml` — Deklarasi BatteryMonitorService (foregroundServiceType specialUse)
- 2608231251 — `colors.xml`, `values-night/colors.xml` — Warna badge status monitor yang tak terpakai dibersihkan
- 2608231837 — `panel_battery.xml` — Tab Monitor diringkas jadi 3 kartu: kartu Level Baterai & Status Pengisian dilebur ke Metrik Real-Time (% Level masuk grid, badge kondisi suhu pindah ke header), kontrol rentang + 3 kartu chart digabung jadi satu kartu Grafik Riwayat
- 2608231837 — `BatteryPanelController.java` — Binding & pengisian kartu Level dihapus, "% Level" masuk grid metrik real-time, isi Salin snapshot disesuaikan
- 2608231837 — `colors.xml`, `values-night/colors.xml` — Warna track bar level (`bat_monitor_bar_track`) tak terpakai dibersihkan
- 2608232001 — `panel_battery.xml` — Kartu Metrik Real-Time berisi ring gauge melingkar (level, kapasitas, status) di kiri + grid metrik monospace di kanan; blok status pengisian dihapus; kartu Grafik Riwayat dapat chevron collapse untuk menyembunyikan isi
- 2608232001 — `BatteryPanelController.java` — Binding & pengisian ring gauge; baris % Level & Kapasitas keluar dari grid metrik (pindah ke ring); status pengisian singkat format Charging•AC/USB/Wireless / Discharging / Full; tombol lipat grafik riwayat; isi Salin snapshot menyesuaikan
- 2608232028 — `panel_battery.xml` — Label statis "Pemantauan real-time baterai perangkat" di atas tab Monitor dihapus; tombol Salin & Simpan Snapshot pindah ke dalam kartu Kesehatan Baterai
- 2608232049 — `panel_battery.xml` — Grafik Persentase pindah ke kartu Metrik Real-Time (sejajar ring, grid metrik turun ke bawah); kartu Grafik Riwayat kini Suhu/Daya/Tegangan/Arus
- 2608232028 — `BatteryPanelController.java` — Isi Salin & Simpan Snapshot menyertakan catatan kesehatan baterai (estimasi kapasitas, skor, sesi, keyakinan)
- 2608232049 — `BatteryPanelController.java` — Binding & pengisian grafik Tegangan & Arus
- 2608232049 — `BatteryChartView.java` — Seri baru Tegangan (V) & Arus (mA/A) dengan skala otomatis dan format nilai sendiri
- 2608232049 — `colors.xml`, `values-night/colors.xml` — Warna aksen chart Tegangan & Arus
- 2608232119 — `BatteryMonitor.java` — Interval sampling layar nyala & layar mati dirapatkan: 10→5 detik dan 60→30 detik
- 2608232119 — `BatteryHistoryDb.java` — Data grafik di-resample ke grid waktu seragam (interpolasi linear) sehingga kepadatan garis konsisten dan tidak berubah pola saat interval sampling berganti
- 2608232119 — `BatteryChartView.java` — Skala sumbu Y grafik Suhu memiliki rentang minimum ±2°C dari nilai tengah agar variasi kecil tidak menggambar garis ekstrem
- 2608232001 — `BatteryBarView.java` — Rumus gradien hue diekstrak ke helper bersama `BatteryColors` (perilaku identik)

### 🔥 File Removed
- 2608231251 — `bat_badge_active_bg.xml` — Drawable badge status monitor tak terpakai setelah kontrol manual dihapus

---

# [4.87.0] 2608212028 184 ***RELEASE***
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
- 2608191759 — `MemoryOrderZonesView.java` — Custom view zona drag chip dua zona (Aktif/Nonaktif) untuk urutan & visibilitas item Memory Stats

### ✏️ File Changed
- 2608190645 — `strings.xml` — Ubah label "Debuging" → "Debugging"
- 2608190645 — `panel_debuging.xml` — Ubah id & judul panel ke "Debugging"
- 2608190645 — `PanelManager.java` — Ubah key map "debuging" → "debugging"
- 2608190645 — `DebugingPanelFragment.java` — Ubah return panelName ke "debugging"
- 2608190914 — `MainActivity.java` — Ubah label & key "Debuging"/"debugging", `rebuildSidebar()` jadi public, filter sidebar berdasarkan setting toggle, filter `navMemory` berdasarkan pref `memory_show_in_sidebar`, default `debugging_show_in_sidebar` ke `false`
- 2608190932 — `activity_settings.xml` — Tambah section "Modul" dengan switch "Tampilkan panel Debugging" (disabled) + tombol Relock + switch "Tampilkan panel Info Memori", kolom input password + tombol Unlock + status teks di bawah switch Debugging
- 2608190932 — `SettingsActivity.java` — Tambah `debuggingSidebarSwitch` (disabled default, proteksi password via tombol Unlock, tombol Relock untuk mengunci ulang, status unlock persisten) + `memorySidebarSwitch` dengan load/save prefs
- 2608190813 — `BatteryStatsModule.java` — Pindahkan `readBatterySnapshot()` ke background thread (`HandlerThread`), pisah `buildDisplayText()` & `applyDisplay()`, `tickRunnable` baca data async
- 2608191622 — `activity_settings.xml` — Tombol Relock diubah dari Button ke TextView (11sp, warna merah); urutan section ditata ulang: Ikon Aplikasi di atas Akses Izin, Info Memori di atas Debugging di section Modul
- 2608191622 — `SettingsActivity.java` — Field `debuggingRelockBtn` diubah dari Button ke TextView
- 2608191818 — `MemoryPanelController.java` — Ganti 4 CheckBox (Heap, Native, Graphics, Total) dengan `MemoryOrderZonesView`, hapus listener individual, tambah `setupOrderZones()` + `onOrderChanged()` callback
- 2608191818 — `panel_memory.xml` — Ganti 4 CheckBox dalam 2 LinearLayout dengan `MemoryOrderZonesView` (`memoryOrderZones`)
- 2608191835 — `MainActivity.java` — Pisahkan `rebuildSidebar()` jadi `initSidebar()` (setup infrastruktur sekali) + `refreshSidebar()` (update data di `onResume()`); tambah `setItems()` di SidebarAdapter; ganti `getAdapterPosition()` → `getBindingAdapterPosition()`
- 2608192013 — `SettingsActivity.java` — Tambah broadcast `ACTION_PANEL_VISIBILITY_CHANGED` saat switch Debugging/Memory di-off, termasuk tombol Relock
- 2608192013 — `MainActivity.java` — Daftarkan `panelVisibilityReceiver` untuk alihkan panel aktif ke "text" saat panel disembunyikan; tambah pengecekan di `onResume()` agar panel tidak valid otomatis dialihkan