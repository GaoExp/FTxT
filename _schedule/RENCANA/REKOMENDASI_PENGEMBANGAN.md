# Rekomendasi Pengembangan Masa Depan FTxT

---

## 1. Prioritas Tinggi (Perlu Segera)

### 1.1 foregroundServiceType untuk Android 14+
Android 14+ (API 34) mewajibkan foreground service mendeklarasikan type. Tanpa ini, app bisa crash di HP baru. Tambahkan `android:foregroundServiceType="specialUse"` di AndroidManifest.xml.

### 1.2 Ganti deprecated API getRealMetrics()
`getDefaultDisplay().getRealMetrics()` sudah deprecated di API 30. Gunakan `WindowMetrics` untuk Android 11+. Ini mencegah behavior unpredictable di HP baru.

---

## 2. Kekuatan Project yang Perlu Dipertahankan

### 2.1 Modularitas yang Baik
Setiap fitur (Text, FPS, Clock, Battery, Network) sudah terpisah dalam package sendiri dengan pola Config + Module yang konsisten. Ini memudahkan penambahan fitur baru.

### 2.2 Preset System yang Komprehensif
PresetManager sudah mature: CRUD, import/export JSON, history, search, favorite, reorder, thumbnail. Ini salah satu fitur terbaik di project ini.

### 2.3 Color Picker yang Powerful
TriangleColorPickerView + HSV/RGB sliders + saved colors + hex editor + color name resolution. Sudah setara color picker di aplikasi desain.

### 2.4 Orientation-aware Positioning
Posisi overlay tersimpan per orientasi (portrait/landscape), sehingga tidak berantakan saat device dirotasi.

---

## 3. Area yang Perlu Perbaikan

### 3.1 FloatingService Terlalu Panjang
FloatingService punya ~700 baris yang hampir seluruhnya delegasi statis repetitif untuk 7 module. Setiap module punya ~10 delegasi statis (start, stop, updateColor, updateSize, updateShadow, updateBackground, updateTouchFlags, updatePosition, setOrientationSuffix, getCurrentPosition). Total 70+ static methods yang strukturnya identik. Bisa digeneralisasi dengan interface atau base class.

### 3.2 Code Duplication di Module
TextModule, FpsModule, dan BatteryCurrentModule punya kode sangat mirip untuk: pembuatan overlay params, safe area calculation, updatePosition(), loadPosition(), savePosition(), posSuffix(), dan updateTouchFlags(). Kandidat kuat untuk abstract base class.

### 3.3 SharedPreferences Reading Manual
`loadShadowConfigs()` di MainActivity membaca 100+ key dari SharedPreferences secara manual baris per baris. Error-prone dan sulit maintain. Sebaiknya pakai model class dengan Gson.

### 3.4 Config Fields Statis Mutable
Semua Config class pakai public static fields yang dimutasi langsung. State sulit di-trace dan rentan bugs. Tidak bisa membedakan default value dari user value tanpa SharedPreferences lookup manual.

### 3.5 e.printStackTrace() Berlebihan
Sangat banyak `e.printStackTrace()` tanpa logging atau fallback bermakna. Di production, ini banjiri logcat tanpa informasi yang bisa di-action.

### 3.6 ColorPickerDialog Terlalu Panjang
928 baris dengan method-parameter passing 20-30 parameter. Indikasi method harus di-refactor jadi inner state class.

---

## 4. Potensi Fitur Baru yang Realistis

### 4.1 Multi-Overlay Layout Preset
Preset saat ini hanya menyimpan konfigurasi per-module. Potensi untuk menyimpan "layout" yang mengatur posisi relatif semua overlay sekaligus (misal: mode gaming, mode monitoring, mode productivity).

### 4.2 Overlay Opacity/Transparency Global
Belum ada kontrol transparansi global untuk semua overlay. Fitur ini bisa menambah UX tanpa effort besar.

### 4.3 Scheduled Overlay (Time-based)
Aktifkan/mematikan module berdasarkan waktu atau kondisi (misal: FPS display hanya aktif saat game detected, battery monitoring hanya saat charging).

### 4.4 Custom Font Selection
Saat ini hanya pakai system default. Menambah font picker bisa jadi fitur menarik untuk personalisasi.

### 4.5 Notification Actions
Foreground service notification bisa ditambah aksi: toggle module, quick preset switch, atau stop service tanpa buka app.

### 4.6 Widget Android
Shortcut di home screen untuk toggle module tertentu tanpa membuka app.

### 4.7 Shizuku FPS Reader
Baca FPS dari game lain pakai Shizuku + dumpsys gfxinfo. Kompleks tapi unik.

---

## 5. Optimasi Performa

### 5.1 Cache Screen Metrics
Banyak method panggil `getRealMetrics()` berulang kali. Cache screen dimensions dan invalidate hanya saat `ACTION_CONFIGURATION_CHANGED`.

### 5.2 SealPatternView Drawing
`onDraw()` lakukan `drawText()` dalam nested loop untuk setiap frame. Untuk text panjang atau area besar, bisa berat. Pertimbangkan off-screen Bitmap caching untuk pattern statis.

### 5.3 SharedPreferences Batch Write
Banyak `apply()` dipanggil berurutan. Bisa di-batch ke satu `edit()` block untuk mengurangi I/O.

### 5.4 BatteryCurrentModule I/O
`readSysfs()` lakukan file I/O setiap interval update. Cache path yang valid pada startup untuk mengurangi I/O.

---

## 6. UI/UX yang Bisa Ditingkatkan

### 6.1 Panel Navigation
Panel switching pakai if-else chain panjang yang diulang di 3 tempat. Sebaiknya pakai map atau fragment-based navigation.

### 6.2 Empty States
Tidak ada placeholder untuk sidebar kosong atau preset list kosong. Lebih baik tampilkan visual placeholder yang guided.

### 6.3 Accessibility
Tidak ada content description untuk controls. TalkBack users tidak bisa pakai app dengan baik.

### 6.4 Haptic Feedback untuk Drag
Overlay drag handler tidak berikan haptic feedback saat mulai/mengakhiri drag. Menambah `performHapticFeedback()` meningkatkan perceived quality.

### 6.5 Color Picker Integration
Tidak ada cara mudah untuk apply color dari picker ke overlay tertentu secara langsung. User harus copy hex lalu paste di panel overlay.

---

## 7. Testing

### 7.1 Unit Tests
Saat ini hanya boilerplate default. Perlu tambah test untuk:
- PresetManager (logika migrasi, CRUD)
- Color conversion logic
- Parsing output (jika ada GPS reader)
- Shadow dan background config

### 7.2 Instrumented Tests
- Test integrasi Shizuku (jika ditambahkan)
- Test overlay positioning di berbagai screen size
- Test orientation change behavior

---

## 8. Rekomendasi Prioritas Pengerjaan

### Tahap 1: Stabilitas & Kompatibilitas
1. Tambah foregroundServiceType untuk Android 14+
2. Ganti deprecated API getRealMetrics()
3. Tambah unit tests untuk komponen kritis

### Tahap 2: Refactor & Optimasi
1. Generalisasi module dengan interface/base class
2. Refactor SharedPreferences reading pakai Gson
3. Cache screen metrics

### Tahap 3: Fitur Baru
1. Scheduled Overlay (time-based)
2. Multi-Overlay Layout Preset
3. Notification Actions

### Tahap 4: UI/UX Enhancement
1. Panel navigation pakai map/fragment
2. Empty states
3. Haptic feedback
4. Accessibility

### Tahap 5: Fitur Eksperimental
1. Shizuku FPS Reader
2. Custom Font Selection
3. Widget Android

---

## 9. Risiko & Pertimbangan

### Kompatibilitas
- Test di Android 8, 10, 12, 14, 15
- Handle perbedaan behavior per merk HP
- Pastikan overlay tidak ganggu app lain

### Performa
- Jangan bikin HP panas/lambat
- Optimasi battery usage
- Hindari memory leak dari static references

### Maintainability
- Setiap perubahan besar perlu dokumentasi
- Pertimbangkan backward compatibility untuk preset lama
- Jangan breaking changes tanpa plan migrasi
