# [4.82.2] - 2026-06-30
### ♻️ Perubahan Fitur
- **Color Picker Dialog digabung** — `ColorPickerDialog` sekarang menggunakan model yang sama dengan panel (`ColorPickerPanelController`): `TriangleColorPickerView` (segitiga) + slider H/S/V/A custom tanpa mode switching. Saved colors key diganti `cp_saved_colors` (share dengan panel). RGB slider tersedia sebagai collapsible section.
### 🐞 Bug Fixes
- **Slider Color Picker tersendat & terintercept scroll** — Slider Hue/Saturation/Value/Alpha dan RGB di panel Color Picker diperbaiki: touch event tidak lagi di-intercept oleh NestedScrollView (`requestDisallowInterceptTouchEvent(true)`), dan koordinat geser menggunakan `getRawX()` agar akurat meskipun jari keluar dari area slider.
- **Triangle & Ring color wheel terintercept scroll** — `TriangleColorPickerView` dan `HSVColorPickerView` sekarang juga mencegah NestedScrollView meng-intercept touch event saat menggeser ring hue atau triangle SV. `HSVColorPickerView` juga tidak lagi mengabaikan sentuhan di luar wheel radius — tetap memproses perubahan hue dari sudut jari.
- **Overlay hilang/off-screen saat orientasi berubah** — Semua 7 PositionController (`Text`, `Fps`, `Clock`, `Battery`, `BatteryPercentage`, `BatteryCurrent`, `Network`) sekarang memanggil `updatePositionStatic()` di akhir konstruktor, sehingga posisi overlay langsung di-recalculate ke dimensi layar baru saat Activity direcreate akibat perubahan orientasi.
### 🔧 Optimasi & Penyesuaian
- **Segitiga HSV Color Wheel rendering dioptimasi** — `TriangleColorPickerView.drawTriangle()` render bitmap segitiga di resolusi 0.5× lalu scale-up ke ukuran asli via `drawBitmap(bitmap, null, dstRect, null)`. Jumlah pixel yang diproses turun 75% (dari ~200k ke ~50k per frame). `ColorMath.generateHueColors()` di-cache static agar tidak alokasi array 361 elemen tiap `onDraw()`.
- **Color Wheel Disk Dialog dioptimasi** — `HSVColorPickerView.drawColorWheel()` cache `SweepGradient` dan `RadialGradient` shader, dibuat ulang hanya saat ukuran view berubah (`onSizeChanged`), bukan tiap `onDraw()`.
### 🔥 File Removed
- `app/src/main/res/layout/dialog_hsv_color_picker.xml` — Tidak dipakai lagi (diganti `dialog_color_picker.xml`)
- `app/src/main/res/layout/dialog_hue_slider_picker.xml` — Tidak dipakai lagi (diganti `dialog_color_picker.xml`)
### ✏️ File Changed
- `app/src/main/java/exp/ftxt/ui/ColorPickerPanelController.java` — Perbaiki `setupSliderTouch()`: tambah `requestDisallowInterceptTouchEvent`, pakai `getRawX()` + `getLocationOnScreen()`; reusable `GradientDrawable` untuk sat/val/alpha (`setColors()` tanpa alokasi baru); cache `checkerDrawable`; optimasi `applySatGradient()` array 101→51; ekstrak `initGradientDrawables()`; aktifkan `colorWheel.setColor()` (sebelumnya di-comment)
- `app/src/main/java/exp/ftxt/features/color_picker/TriangleColorPickerView.java` — Tambah `requestDisallowInterceptTouchEvent` di `onTouchEvent`; optimasi `drawTriangle()` render bitmap di resolusi 0.5× lalu scale-up
- `app/src/main/java/exp/ftxt/shared/color/ColorMath.java` — Cache `generateHueColors()` static
- `app/src/main/java/exp/ftxt/shared/color/HSVColorPickerView.java` — Tambah `requestDisallowInterceptTouchEvent`; hapus batasan `distance > wheelRadius` yang mengabaikan sentuhan di luar wheel; cache `SweepGradient` & `RadialGradient` shader via `onSizeChanged`
- `app/src/main/java/exp/ftxt/shared/ui/ColorPickerDialog.java` — Rewrite total: gunakan `TriangleColorPickerView` + custom slider H/S/V/A/RGB, hapus mode switching, share saved colors key `cp_saved_colors` dengan panel
- `app/src/main/res/layout/dialog_color_picker.xml` — Layout baru gabungan wheel + sliders + saved colors + collapsible RGB
- `app/build.gradle` — versionCode 169, versionName 4.82.2
- `gradle.properties` — JVM args 1024m → 2048m
- `app/src/main/java/exp/ftxt/ui/TextPositionController.java` — Tambah `updatePositionStatic()` di akhir konstruktor
- `app/src/main/java/exp/ftxt/ui/FpsPositionController.java` — Tambah `updatePositionStatic()` di akhir konstruktor
- `app/src/main/java/exp/ftxt/ui/ClockPositionController.java` — Tambah `updatePositionStatic()` di akhir konstruktor
- `app/src/main/java/exp/ftxt/ui/BatteryPositionController.java` — Tambah `updatePositionStatic()` di akhir konstruktor
- `app/src/main/java/exp/ftxt/ui/BatteryPercentagePositionController.java` — Tambah `updatePositionStatic()` di akhir konstruktor
- `app/src/main/java/exp/ftxt/ui/BatteryCurrentPositionController.java` — Tambah `updatePositionStatic()` di akhir konstruktor
- `app/src/main/java/exp/ftxt/ui/NetworkPositionController.java` — Tambah `updatePositionStatic()` di akhir konstruktor
### 🔢 Version
4.82.2

# [4.82.1] - 2026-06-29
### 🚮 Fitur Dihapus
- **XY Pad dihapus total** — Fitur XY Pad (2D drag area) dihapus dari FPS dan Floating Text. Kontrol posisi kembali ke slider X/Y (SeekBar) + D-Pad seperti sedia kala.
### ♻️ Perubahan Fitur
- **Color Picker panel digabung** — Color Wheel dan Hue/Saturation/Value/Alpha slider tidak lagi dipisah mode, melainkan tampil bersamaan dalam satu panel. Tombol swap mode dan pengaturan "Model Color Picker" di Konfigurasi dihapus.
### 🔥 File Removed
- `app/src/main/java/exp/ftxt/shared/ui/XyPadView.java` — Dihapus
### ✏️ File Changed
- `app/src/main/java/exp/ftxt/ui/FpsPositionController.java` — Hapus semua kode XY Pad (import, field, binding, toggle, listener, sync)
- `app/src/main/java/exp/ftxt/ui/TextPositionController.java` — Hapus semua kode XY Pad (import, field, binding, listener, sync)
- `app/src/main/res/layout/panel_fps.xml` — Hapus `fps_xyPadRow`, `fps_btnSwap`, `fps_xyXSeek`, `fps_xyYSeek`, `fps_xyPad`
- `app/src/main/res/layout/panel_text.xml` — Hapus `text_xyPadRow`, `text_xyPad`
- `app/build.gradle` — versionCode 168, versionName 4.82.1
- `app/src/main/java/exp/ftxt/ui/ColorPickerPanelController.java` — Hapus mode switching (isSliderMode, toggleMode, setupMode, switchModeBtn, hueSeekBar); gabung wheel + sliders selalu visible
- `app/src/main/res/layout/panel_color_picker.xml` — Hapus mode header (label + tombol swap); sliderPanel selalu visible; hapus hueSeekBar tak terpakai
- `app/src/main/java/exp/ftxt/SettingsActivity.java` — Hapus RadioGroup color picker mode
- `app/src/main/res/layout/activity_settings.xml` — Hapus Model Color Picker section
- `PANDUAN.md` — Update deskripsi Color Picker panel gabungan
- `STRUKTUR.md` — Update statistik (Java source 58→56, Layout XML 20→21, Drawable PNG 13→14)
- `README.md` — Update versi ke 4.82.1, tanggal 2026-06-29
### 🔢 Version
4.82.1

# [4.82.0] - 2026-06-18
### ♻️ Perubahan Fitur
- **Color Piker dari Activity jadi Panel Modul** — Color Picker tidak lagi Activity terpisah, melainkan panel di MainActivity seperti modul lainnya (Floating Text, FPS, dll). Navigasi sidebar langsung menampilkan panel tanpa buka activity baru. Tombol silang dihilangkan, ikon navigasi drawer (garis 3) yang muncul.
- **XY Pad toggle di kontrol posisi FPS** — Tombol swap (ic_swap) untuk menukar mode D-Pad dengan XY Pad (2D drag area). Mode tersimpan di SharedPreferences.
- **XY Pad FPS dengan slider X/Y tepi terintegrasi** — Saat mode XY Pad aktif, XyPadView menampilkan slider X horizontal di tepi atas dan slider Y vertikal di tepi kiri. Slider X hanya menggeser sumbu X, slider Y hanya sumbu Y, drag dot di area tengah tetap gerak 2D bebas. Kedua slider dan dot saling sinkron. Slider X/Y existing (SeekBar) disembunyikan saat mode XY Pad.
### 🔧 Optimasi & Penyesuaian
- **XY Pad persegi (1:1) dari API sistem** — `onMeasure` XyPadView ambil lebar layar via `WindowManager.getDefaultDisplay().getMetrics()` dan set lebar = tinggi = 20% lebar layar. Persegi agar ringkas dan konsisten di semua device.
- **XY Pad ukuran proporsional (rasio layar)** — `onMeasure` XyPadView pakai `getRealMetrics()` untuk ukuran layar fisik (bukan area pakai). Tinggi dihitung proporsional sesuai rasio layar. Tambah `setFixedSize()` untuk opsi ukuran tetap (216×492).
- **XY Pad diperbesar 20%→40% lebar layar** — Skala XyPadView dinaikkan jadi 40% lebar layar agar area gerak lebih luas dan presisi. Floating Text tidak lagi pakai fixed 216×492, ikut proporsional 40%.
- **PADDING proporsional 8% per dimensi** — Padding absolut 40px (sama di lebar & tinggi) diganti PAD_RATIO=0.08 agar rasio area aktif persis sama dengan rasio layar. DOT_RADIUS juga proporsional (60% dari padding terkecil).
### ✨ Fitur Baru
- **Menu Color Picker di Navigasi Drawer** — Menu baru "Color Picker" di sidebar. Adaptasi dari project ColorPicker (Mini ColPic): Color Disk (hue ring + SV triangle) dan Hue Slider mode (H/S/V/A custom slider), RGB slider collapsible, saved colors (16 slot dengan simpan/hapus), HEX/HSV/ARGB display dengan copy ke clipboard, HEX editor, color name resolver, toggle nama warna, checkerboard transparansi.
- **XY Pad di Floating Text** — XY Pad ukuran tetap 216×492 sebagai pembanding di panel Floating Text.
### 🗒️ File Added
- `app/src/main/res/layout/panel_color_picker.xml` — Panel color picker untuk MainActivity
- `app/src/main/java/exp/ftxt/ui/ColorPickerPanelController.java` — Controller panel color picker
- `app/src/main/java/exp/ftxt/shared/ui/XyPadView.java` — Custom View XY Pad 2D drag
### ✏️ File Changed
- `app/src/main/java/exp/ftxt/MainActivity.java` — Tambah panelColorPicker field, binding, init controller, handler sidebar show panel (ganti startActivity), hideAllPanels, onResume, onDestroy cleanup
- `app/src/main/res/layout/activity_main.xml` — Tambah `<include layout="@layout/panel_color_picker"/>`
- `app/src/main/AndroidManifest.xml` — Hapus deklarasi `ColorPickerActivity`
- `app/src/main/res/values/ids.xml` — Tambah `navColorPicker`
- `app/src/main/res/values/strings.xml` — Tambah string `nav_color_picker`
- `app/src/main/java/exp/ftxt/ui/TextPositionController.java` — Tambah reset `orientationSuffix` di `cleanup()`; tambah XY Pad binding, setFixedSize(216,492), listener, sync
- `app/src/main/java/exp/ftxt/ui/FpsPositionController.java` — Tambah reset `orientationSuffix` di `cleanup()`; integrasi XY Pad toggle (binding, listener, mode swap)
- `app/src/main/res/layout/panel_fps.xml` — Tambah FrameLayout untuk D-Pad/XY Pad + tombol swap ic_swap
- `app/src/main/res/layout/panel_text.xml` — Tambah XY Pad (text_xyPadRow, text_xyPad) di section posisi untuk perbandingan
- `app/src/main/java/exp/ftxt/shared/ui/XyPadView.java` — Ganti `getMetrics()` ke `getRealMetrics()`; tambah `setFixedSize()`; ukuran proporsional rasio layar (bukan persegi 1:1)
- `app/src/main/java/exp/ftxt/ui/ClockPositionController.java` — Tambah reset `orientationSuffix` di `cleanup()`
- `app/src/main/java/exp/ftxt/ui/BatteryPositionController.java` — Tambah reset `orientationSuffix` di `cleanup()`
- `app/src/main/java/exp/ftxt/ui/BatteryPercentagePositionController.java` — Tambah reset `orientationSuffix` di `cleanup()`
- `app/src/main/java/exp/ftxt/ui/BatteryCurrentPositionController.java` — Tambah reset `orientationSuffix` di `cleanup()`
- `app/src/main/java/exp/ftxt/ui/NetworkPositionController.java` — Tambah reset `orientationSuffix` di `cleanup()`
- `app/src/main/java/exp/ftxt/shared/ui/XyPadView.java` — Tambah mode slider tepi: slider X horizontal (atas) + slider Y vertikal (kiri), flag `showSliders`, touch handling per area (X slider / Y slider / XY Pad)
- `app/src/main/java/exp/ftxt/ui/FpsPositionController.java` — Aktifkan `xyPad.setShowSliders(true)` saat mode XY Pad via `updatePadVisibility()`; sembunyikan `fps_sliderGroup` saat mode XY Pad
- `app/src/main/res/layout/panel_fps.xml` — Tambah `android:id="@+id/fps_sliderGroup"` pada container slider X/Y
### 🔥 File Removed
- `app/src/main/java/exp/ftxt/features/color_picker/ColorPickerActivity.java` — Digantikan panel ColorPickerPanelController
- `app/src/main/res/layout/activity_color_picker.xml` — Digantikan panel_color_picker.xml
### 🔧 Optimasi & Penyesuaian
- **Fix deprecation Gradle signingConfig** — Ganti `signingConfig signingConfigs.release` jadi `signingConfig = signingConfigs.release` (Gradle 10 siap).
### 🔢 Version
`166` → `167`
`3.12.4.81.0` → `4.82.0` (konversi ke format major.minor.patch)

---

# [3.12.4.80.0] - 2026-06-13
### 🚮 Fitur Dihapus
- **Material Design Colors** — ~270 warna Material Design dihapus dari `ColorNameResolver.java`. Method `loadMaterialColors()` dan pemanggilannya dicabut. Tersisa CSS Colors (148) dan XKCD Colors (~950) sebagai sumber nama warna.
### ✨ Fitur Baru
- **Opsi Model Color Picker** — Di Konfigurasi, tersedia pilihan model Color Picker: **Color Wheel** atau **Hue Slider**.
- **Mode Hue Slider** — UI color picker alternatif dengan slider Hue (0–360°), Saturation (0–100%), Brightness (0–100%), Opacity (0–100%). Dilengkapi swatch Current/Previous (dengan nama warna di dalamnya), info AHEX/HSV/ARGB, dan grid Saved Colors (maks 8) dengan tombol [+]. Masing-masing slider memiliki gradient background dinamis yang merepresentasikan rentang nilai.
- **Saved Colors** — Warna dapat disimpan (max 16, via [+] hijau di header atau tap slot kosong hijau), di-load via tap (dialog Apply dengan Hapus merah). Grid 2×8 selalu tampil penuh. Data disimpan di SharedPreferences. Bagian ini bisa ditutup/dibuka via toggle.
### ♻️ Perubahan Fitur
- **Hue Slider layout redesain** — Info warna AHEX/HSV/ARGB pindah ke atas (kiri), preview Current/Previous digabung horizontal (kanan) dengan pembatas, label slider dipersingkat jadi H:/S:/V:/A:.
- **Tombol switch mode color picker** — Tombol ⇄ (ic_swap) ditambahkan di title bar Color Wheel dan Hue Slider untuk menukar mode color picker tanpa perlu buka Konfigurasi.
- **"Lihat Dokumentasi" → "Dokumentasi"** — Label menu popup pengaturan (gear) diubah dari "Lihat Dokumentasi" menjadi "Dokumentasi".
- **STRUKTUR.md ditambahkan ke daftar dokumentasi in-app** — Tombol STRUKTUR ditambahkan di daftar dokumen, bisa dibaca seperti README/CHANGELOG/PANDUAN.
- **Slider height diperkecil** — Tinggi slider Hue/Saturation/Brightness/Opacity dari 16dp jadi 8dp.
- **Hapus label colorName** — Nama warna tidak lagi ditampilkan sebagai teks terpisah di bawah info ARGB karena sudah ada di dalam swatch Current/Previous.
- **Saved Colors grid 2×8 + drag-reorder + anti-flicker** — Slot warna tersimpan diubah dari horizontal row jadi GridLayout 2 baris × 8 kolom. Semua 16 slot selalu tampil. Slot kosong: border hijau [ ] bukan hijau penuh. [+] di header jadi hijau, simpan di slot pertama (geser warna lain ke kanan). Long-press pada slot terisi → drop di slot lain → swap warna. Update in-place (no removeAllViews) agar tidak berkedip.
- **Animasi geser saat simpan via [+] ** — Tombol [+] sekarang: simpan warna → set sel ke posisi kiri (translationX -cellStep) → update konten via loadSavedColors → animasi semua sel geser ke kanan ke posisi final (300ms AccelerateDecelerate). Warna baru muncul dari kiri, warna lama bergeser ke kanan.
- **Animasi geser saat hapus warna** — Tombol Hapus sekarang: hapus warna dari preferences → set sel dari idx hapus hingga akhir ke kanan (translationX +cellStep) → update konten via loadSavedColors → animasi sel yang terdampak geser ke kiri ke posisi final (300ms AccelerateDecelerate). Warna setelah slot terhapus bergeser ke kiri mengisi celah.
- **Thumb slider jadi lingkaran** — Thumb Hue/Saturation/Brightness/Opacity diubah dari rectangle 2×10dp jadi lingkaran 12×12dp (shape oval + layout_width/layout_height 12dp).
- **Info warna pindah ke bawah swatch** — Teks AHEX/HSV/ARGB dipindah dari bawah slider Opacity ke bawah swatch Current/Previous.
- **Checkerboard pattern untuk area transparan** — Swatch Current/Previous dan slider Opacity kini memiliki background pola kotak-kotak (abu terang/gelap) yang menampilkan area transparan/alpha channel. Menggunakan `BitmapDrawable` dengan `TileMode.REPEAT` + `LayerDrawable` (checkerboard di bawah, warna/gradien di atas).
### 🔧 Optimasi & Penyesuaian
- **Jarak antar elemen Hue Slider dirapatkan** — Padding root 16→12dp, marginBottom swatch 12→6dp, marginBottom info warna 8→4dp, marginBottom slider 8→4dp, marginBottom opacity slider 12→6dp, marginBottom grid saved 12→6dp.
### 🐞 Bug Fixes
- **Slider mode color picker crash (NPE)** — Layout `dialog_hue_slider_picker.xml` menggunakan custom slider (FrameLayout+gradientBg+thumb+touchArea) tapi Java code `ColorPickerDialog.java` mengharapkan `SeekBar`. Diperbaiki dengan implementasi custom slider: touch handling via `OnTouchListener` + `MotionEvent`, posisi thumb via `setTranslationX()`, dan mapping progress manual untuk keempat slider (Hue 0-360, Saturation/Brightness/Opacity 0-100).
- **Build gagal: cannot find symbol Animator** — `ColorPickerDialog.java` menggunakan `List<Animator>` tanpa mengimpor `android.animation.Animator`. Ditambahkan import yang hilang.
- **Edit HEX di Hue Slider mode force close (NPE)** — Saat user klik ikon edit HEX dan OK, method `setThumbPos()` memanggil `parent.post()` tanpa cek null pada thumb dan thumb.getParent() di dalam lambda. Diperbaiki dengan menambahkan null check pada thumb sebelum getParent(), dan cek ulang thumb.getParent() di dalam lambda sebelum mengakses parent.
- **Posisi overlay antar orientasi saling menimpa** — `orientationSuffix` di module tidak di-reset saat `cleanup()` PositionController, menyebabkan posisi tersimpan ke orientasi yang salah saat drag overlay dari panel lain. Diperbaiki dengan reset `orientationSuffix` ke `null` di `cleanup()` semua 7 PositionController, sehingga `posSuffix()` fallback ke orientasi fisik yang benar.
### ✏️ File Changed
- `app/src/main/java/exp/ftxt/shared/color/ColorNameResolver.java` — Hapus `loadMaterialColors()` dan seluruh data Material Design colors dari static block
- `app/src/main/java/exp/ftxt/MainActivity.java` — Ubah "Lihat Dokumentasi" jadi "Dokumentasi" di popup menu pengaturan
- `app/src/main/java/exp/ftxt/DocumentationActivity.java` — Tambah onClickListener untuk `docStrukturButton`
- `app/src/main/res/layout/activity_documentation.xml` — Tambah `docStrukturButton` TextView setelah PANDUAN
- `app/src/main/res/layout/activity_settings.xml` — Tambah RadioGroup (Color Wheel / Hue Slider) di Model Color Picker
- `app/src/main/res/layout/dialog_hue_slider_picker.xml` — Redesain layout: title row + mode label + switch button; info AHEX/HSV/ARGB di kiri, swatch Current/Previous horizontal di kanan; label slider pendek H:/S:/V:/A:; Header Saved Colors [+] hijau di kiri, label tengah, toggle collapse ʌ di kanan; GridLayout 8×2; thumb 12×12dp
- `app/src/main/res/drawable/seekbar_thumb.xml` — Shape rectangle 2×10dp → oval 12×12dp (lingkaran)
- `app/src/main/res/layout/dialog_hsv_color_picker.xml` — Wheel dibungkus FrameLayout + tambah `hueSeekBar`; tambah title row + mode label + switch button
- `app/src/main/res/values/strings.xml` — Tambah string color_picker_section, color_picker_disk, color_picker_slider
- `app/src/main/java/exp/ftxt/SettingsActivity.java` — Tambah RadioGroup listener + load/save `color_picker_mode` preference
- `app/src/main/java/exp/ftxt/shared/ui/ColorPickerDialog.java` — Refactor: `show()` routing ke `showDiskMode()` atau `showSliderMode()`; slider mode dengan H/S/B/O slider, gradient background, Saved Colors (simpan/muat), swatch current/previous; ganti SeekBar dengan custom slider: `setThumbPos()` + `setupSliderTouch()` + progress arrays, hapus import SeekBar/ColorDrawable, tambah MotionEvent; Saved Colors: ganti LinearLayout→GridLayout, 16 slot selalu tampil (filled/empty hijau), empty slot tap→simpan, filled tap→dialog Apply+Hapus, drag-reorder+DragEvent; [+] simpan warna: set sel ke posisi kiri → loadSavedColors update konten → animasi ObjectAnimator geser sel ke kanan (300ms AccelerateDecelerate); tambah import `android.animation.Animator`; tambah `createCheckerboard()` + `setSwatchBg()` + checkerboard di swatch dan slider Opacity via LayerDrawable; tambah handler switch mode button di kedua mode; ubah label slider ke format pendek H:/S:/V:/A:; tambah null check di method `setThumbPos()`
### 🗒️ File Added
- `app/src/main/res/drawable/ic_swap.xml` — Icon swap untuk tombol tukar mode color picker
### 🔢 Version
`3.11.4.79.0` → `3.12.4.80.0` (minor+1, patch=0)

---

# [3.11.4.79.0] - 2026-06-13
### 🚮 Fitur Dihapus
- **Watermark Module** — Seluruh modul Watermark dihapus total: WatermarkConfig, WatermarkModule, WatermarkPanelController, WatermarkPositionController, panel_watermark.xml, seluruh referensi dari FloatingService, MainActivity, layout, menu, strings, dan ids. Mode Segel sudah dipindahkan ke Floating Text di versi yang sama.
### 📥 Fitur Dipulihkan
- **Mode Segel dipulihkan dari Watermark ke Floating Text** — Fitur Mode Segel (pola teks diulang diagonal) dipulihkan dari modul Watermark ke modul Floating Text. Konfigurasi pattern, UI kontrol, dan logika overlay pattern sekarang ada di Floating Text.
### ♻️ Perubahan Fitur
- **Dokumentasi in-app: AlertDialog → Activity penuh** — Dokumentasi tidak lagi ditampilkan dalam AlertDialog, melainkan halaman activity dengan daftar dokumen (README, CHANGELOG, PANDUAN). Tap item menampilkan konten markdown full screen dengan zoom ± di toolbar.
- **Renderer markdown dengan Markwon** — TextView `.setText()` diganti dengan Markwon + plugin Table dan TaskList, sehingga tabel dan daftar tugas di dokumen .md tampil rapi.
- **Zoom ± di toolbar** — Kontrol zoom ukuran teks dipindah dari bar di dalam dialog ke pojok kanan toolbar (− [ukuran] +) saat melihat dokumen.
- **Pembatas garis daftar dokumen** — Divider horizontal tipis di antara item daftar dokumen, sidebar navigasi, dan daftar preset menggunakan `?android:attr/listDivider` yang adaptif terhadap tema.
- **Divider daftar menu sidebar + preset** — `DividerItemDecoration` pada RecyclerView sidebar dan preset list agar garis pemisah konsisten di semua daftar.
### 🔧 Optimasi & Penyesuaian
- **Release notes GitHub otomatis strip section file** — Workflow `release.yml` sekarang menyaring section `🗒️ File Added`, `✏️ File Changed`, `🔥 File Removed` dari deskripsi release GitHub.
- **Sync dokumen .md root → assets saat build** — Gradle task `syncDocs` otomatis menyalin `README.md`, `CHANGELOG.md`, `PANDUAN.md`, `STRUKTUR.md` dari root ke `app/src/main/assets/` setiap `preBuild`. File `.txt` di assets dihapus.
### 🗒️ File Added
- `app/src/main/res/drawable/divider_horizontal.xml` — Divider horizontal tipis untuk daftar dokumen
- `app/src/main/res/layout/toolbar_zoom.xml` — Layout kontrol zoom (− [ukuran] +) di toolbar
- `app/src/main/assets/CHANGELOG.md` — Sinkronisasi dari root via syncDocs
- `app/src/main/assets/PANDUAN.md` — Sinkronisasi dari root via syncDocs
- `app/src/main/assets/README.md` — Sinkronisasi dari root via syncDocs
- `app/src/main/assets/STRUKTUR.md` — Sinkronisasi dari root via syncDocs
### ✏️ File Changed
- `app/src/main/java/exp/ftxt/features/floating_text/TextConfig.java` — Tambah field pattern (patternEnabled, patternSpacingH/V, patternAngle, patternColor)
- `app/src/main/java/exp/ftxt/features/floating_text/TextModule.java` — Tambah SealPatternView inner class, createPatternOverlay(), updatePattern(); update text/size/color untuk pattern
- `app/src/main/java/exp/ftxt/ui/TextPanelController.java` — Tambah pattern UI bindings, loadConfig, listeners, updatePatternVisibility(); offset sudut dari 90 jadi 180
- `app/src/main/res/layout/panel_text.xml` — Tambah checkbox Mode Segel + pattern container; SeekBar sudut max 180 jadi 360 (-180° sampai 180°)
- `app/src/main/java/exp/ftxt/core/FloatingService.java` — Tambah updateTextPatternStatic(), hapus updateWatermarkPatternStatic(); hapus seluruh watermark delegates + watermarkModule.stop()
- `app/src/main/java/exp/ftxt/MainActivity.java` — Pindah loading SP pattern dari WatermarkConfig ke TextConfig; hapus semua referensi Watermark (import, field, panel controller, nav handler, cleanup)
- `app/src/main/res/layout/activity_main.xml` — Hapus `<include layout="@layout/panel_watermark"/>`
- `app/src/main/res/menu/drawer_menu.xml` — Hapus item nav_watermark
- `app/src/main/res/values/strings.xml` — Hapus string nav_watermark
- `app/src/main/res/values/ids.xml` — Hapus id navWatermark
- `README.md` — Hapus Watermark dari fitur; pindah Mode Segel ke Floating Text
- `PANDUAN.md` — Hapus Watermark dari navigasi dan panduan; tambah Mode Segel di Floating Text
- `STRUKTUR.md` — Hapus watermark dari struktur folder; update statistik
- `.github/workflows/release.yml` — Filter section file dari release notes pada saat extract
- `app/build.gradle` — Tambah dependency Markwon (core, ext-tables, ext-tasklist); tambah task `syncDocs`; hapus sync `.txt`
- `app/src/main/java/exp/ftxt/DocumentationActivity.java` — Markwon renderer; full screen list/content toggle; toolbar zoom; baca .md bukan .txt
- `app/src/main/res/layout/activity_documentation.xml` — Redesain: daftar dokumen + konten full screen; divider `?android:attr/listDivider`; hapus zoom bar
- `app/src/main/java/exp/ftxt/shared/preset/PresetBrowserDialog.java` — Tambah `DividerItemDecoration` di preset list
- `app/src/main/res/layout/panel_text.xml` — Pindahkan checkbox Mode Segel ke baris Overlay + Kunci Posisi
- `app/src/main/java/exp/ftxt/ui/TextPanelController.java` — Kunci Posisi dinonaktifkan (bukan disembunyikan) saat Mode Segel aktif
### 🔥 File Removed
- `app/src/main/java/exp/ftxt/features/watermark/WatermarkConfig.java`
- `app/src/main/java/exp/ftxt/features/watermark/WatermarkModule.java`
- `app/src/main/java/exp/ftxt/ui/WatermarkPanelController.java`
- `app/src/main/java/exp/ftxt/ui/WatermarkPositionController.java`
- `app/src/main/res/layout/panel_watermark.xml`
- `app/src/main/assets/CHANGELOG.txt` — Digantikan CHANGELOG.md dari syncDocs
- `app/src/main/assets/PANDUAN.txt` — Digantikan PANDUAN.md dari syncDocs
- `app/src/main/assets/README.txt` — Digantikan README.md dari syncDocs
- `app/src/main/assets/STRUKTUR.txt` — Digantikan STRUKTUR.md dari syncDocs
### 🔢 Version
versionCode: 163
versionName: 3.11.4.79.0

---

# [3.11.3.78.1] - 2026-06-13
### 🔧 Optimasi & Penyesuaian
- **Drag & drop reorder preset** — Migrasi ListView ke RecyclerView + ItemTouchHelper untuk animasi drag & drop yang smooth.
- **Drag & drop drawer sidebar** — Migrasi `LinearLayout`+`DragEvent` ke `RecyclerView`+`ItemTouchHelper` untuk animasi drag & drop smooth di navigasi drawer.
- **Lint `MissingDefaultResource` di drawable-night** — Nonaktifkan lint `MissingDefaultResource` yang menyebabkan `lintVitalRelease` gagal karena drawable malam tanpa deklarasi di folder dasar.
### 🗒️ File Added
- `app/src/main/res/values/ids.xml` — Deklarasi ID `R.id.nav*` untuk drawer (hilang setelah hapus `TextView` statik)
### ✏️ File Changed
- `app/build.gradle` — Tambah dependency `androidx.recyclerview:recyclerview:1.3.2`; tambah `lint { disable 'MissingDefaultResource' }`
- `app/src/main/res/layout/dialog_preset_browser.xml` — `ListView` → `RecyclerView`
- `app/src/main/res/layout/drawer_content.xml` — `ScrollView`+`LinearLayout`+static `TextView` → `RecyclerView`
- `app/src/main/java/exp/ftxt/shared/preset/PresetBrowserDialog.java` — `ListView`+`BaseAdapter` → `RecyclerView`+`ItemTouchHelper`; hapus `ClipData`/`DragEvent`, tambah `LinearLayoutManager`/`ViewHolder`
- `app/src/main/java/exp/ftxt/MainActivity.java` — `LinearLayout`+`DragEvent`+`makeDraggable`/`setupDragTarget`/`buildSidebarItem` → `RecyclerView`+`ItemTouchHelper`+`SidebarAdapter`; hapus `ClipData`/`DragEvent`, tambah `RecyclerView`/`ItemTouchHelper`/`LinearLayoutManager`
### 🔢 Version
versionCode: 161
versionName: 3.11.3.78.1

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

# [2.6.1.42.0 – 2.3.1.16.0] - 2026-05-16–06-01
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
### 🐞 Bug Fixes
- **Overlay dimensi layar lama** — Refresh dari WindowManager.
- **Handler memory leak** — Cleanup di onDestroy.
- **XyPad tidak bisa drag** — DisallowInterceptTouchEvent.
- **SettingsActivity back arrow**.
### 🔢 Version
versionCode: 51 → 96
versionName: 2.3.1.16.0 → 2.6.1.42.0

---

# [2.3.1.15.0 – 1.0.0.0.0] - Pra-2026–2026-05
Menggabungkan 2 entry sisa (1 merger + 1 catatan).
### ✨ Fitur Baru
- Toggle tema (gelap/terang) via ikon bulan di toolbar kanan.
- SettingsActivity membaca assets sebagai single source of truth.
### 🔧 Optimasi & Penyesuaian
- Persiapan struktur modular, HSVColorPickerView ke shared component.
### 🐞 Bug Fixes
- **FPS tidak tampil** — Service standalone tanpa text overlay.
- **TextConfig.size** — Posisi overlay gagal termuat.
### 💡 Catatan
> Major 1 kebawah telah dipisahkan dari Project.
### 🔢 Version
versionCode: earliest
versionName: 1.0.0.0.0 → 2.3.1.15.0
