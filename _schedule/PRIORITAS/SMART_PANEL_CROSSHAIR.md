# Smart Panel — Overlay Control

**Status:** Rencana — belum dikerjakan
**Tujuan:** Membuat panel mengambang (floating) untuk mengontrol posisi dan pengaturan semua modul overlay (Crosshair, FPS, Suhu, Baterai, CPU/RAM) secara real-time.

---

## 1. Konsep

Smart Panel adalah panel kontrol mengambang yang bisa diakses dari mana saja di layar. Satu panel bisa digunakan untuk mengontrol semua modul overlay — pengguna tinggal pilih modul mana yang ingin dikontrol.

**Prinsip:**
- Satu icon bulat mengambang
- Satu panel kontrol, tapi kontennya dinamis sesuai modul aktif
- Setiap modul punya kontrol spesifik sesuai kemampuannya
- Pengguna bisa switch antar modul dari dalam panel

---

## 2. Modul yang Bisa Dikontrol

### 2.1 Crosshair
- D-pad (geser posisi)
- Opacity, Size, Color, Lock

### 2.2 FPS Overlay
- D-pad (geser posisi)
- Format: Angka saja atau Graph
- Ukuran teks
- Warna teks

### 2.3 Suhu Overlay
- D-pad (geser posisi)
- Format: Celsius / Fahrenheit
- Warna teks

### 2.4 Baterai Overlay
- D-pad (geser posisi)
- Format: Persen saja atau Persen + Estimasi
- Warna teks

### 2.5 CPU/RAM Overlay
- D-pad (geser posisi)
- Format: Persen / Bar / Graph
- Warna teks

---

## 3. Icon Bulat Mengambang

### 2.1 Tampilan
- Bentuk: bulat (circle) dengan diameter ±48dp
- Ikon: crosshair atau icon custom
- Warna: semi-transparan (opacity 70-80%)
- Posisi default: edge kiri/kanan layar, tengah vertikal

### 2.2 Perilaku
- **Drag:** Bisa di-drag ke posisi mana saja di layar
- **Snap:** Saat dilepas di edge, snap ke edge terdekat
- **Double tap:** Sembunyikan icon (hanya bisa ditampilkan ulang dari notifikasi)
- **Long press:** Buka pengaturan Smart Panel

### 2.3 Animasi
- Saat idle: sedikit bergerak/bernapas (breathing effect) agar terlihat hidup
- Saat disentuh: scale up sedikit (1.1x) sebagai feedback
- Saat expand: icon berubah jadi panel kontrol dengan animasi scale + fade

---

## 4. Panel Kontrol

### 4.1 Struktur Panel
Saat icon diketuk, muncul panel berisi:

**Baris 0: Label Modul**
```
[Crosshair ▼]
```
- Label menunjukkan modul yang sedang dikontrol
- Ketuk label → muncul popup daftar modul
- Pilih modul → panel berubah jadi kontrol modul tersebut

**Baris 1: D-Pad (5 tombol)**
```
        [▲]
   [◄]   [●]   [►]
        [▼]
```
- ▲: Geser modul ke atas (1px per ketuk, 10px per long press)
- ▼: Geser modul ke bawah
- ◄: Geser modul ke kiri
- ►: Geser modul ke kanan
- ● (Center): Reset posisi modul ke tengah layar

**Baris 2: Kontrol Spesifik Modul**
(Kontrol berbeda sesuai modul yang dipilih — lihat bagian 5)

**Baris 3: Toggle**
```
[On/Off] [Lock]
```
- On/Off: Toggle modul visible/invisible
- Lock: Lock posisi modul (tidak bisa digeser)

### 4.2 Switch Modul
Ketuk label modul → popup muncul:
```
[Crosshair] [FPS] [Suhu] [Baterai] [CPU/RAM]
```
- Pilih modul → panel update kontennya
- Label berubah nama modul baru
- Kontrol di baris 2 berubah sesuai modul

### 4.3 Ukuran Panel
- Lebar: ±200dp
- Tinggi: ±180dp (bervariasi sesuai modul)
- Sudut membulat (corner radius: 16dp)
- Background: semi-transparan gelap (blur effect jika device mendukung)

---

## 5. Kontrol Spesifik Per Modul

### 5.1 Crosshair
**Baris 2:**
```
[Opacity] [Size] [Reset]
```
- Opacity: Slider transparansi crosshair (0-100%)
- Size: Slider ukuran crosshair (50-200%)
- Reset: Reset semua pengaturan ke default

### 5.2 FPS Overlay
**Baris 2:**
```
[Format] [Size] [Reset]
```
- Format: Toggle antara "Angka" atau "Graph"
- Size: Slider ukuran teks (50-200%)
- Reset: Reset semua pengaturan ke default

### 5.3 Suhu Overlay
**Baris 2:**
```
[Format] [Reset]
```
- Format: Toggle antara "Celsius" atau "Fahrenheit"
- Reset: Reset semua pengaturan ke default

### 5.4 Baterai Overlay
**Baris 2:**
```
[Format] [Reset]
```
- Format: Toggle antara "Persen" atau "Persen + Estimasi"
- Reset: Reset semua pengaturan ke default

### 5.5 CPU/RAM Overlay
**Baris 2:**
```
[Format] [Reset]
```
- Format: Toggle antara "Persen", "Bar", atau "Graph"
- Reset: Reset semua pengaturan ke default

---

## 4. Animasi Panel

### 4.1 Expand (Icon → Panel)
- Durasi: 200ms
- Easing: ease-out (cepat di awal, lambat di akhir)
- Icon scale down → panel scale up dari posisi icon

### 4.2 Collapse (Panel → Icon)
- Durasi: 150ms
- Easing: ease-in
- Panel scale down → icon scale up

### 4.3 Dismiss (Ketuk di luar)
- Panel fade out + scale down
- Icon muncul kembali dengan bounce effect

---

## 5. Posisi Panel

### 5.1 Default
- Muncul di sebelah icon (di sisi yang ada ruang)
- Jika icon di kiri → panel muncul di kanan icon
- Jika icon di kanan → panel muncul di kiri icon

### 5.3 Boundary Check
- Panel tidak boleh keluar dari layar
- Jika tidak ada ruang cukup, panel muncul di posisi lain (atas/bawah icon)
- Panel selalu fully visible (tidak terpotong)

---

## 6. Notifikasi Persistent

### 6.1 Isi Notifikasi
- Icon: crosshair
- Text: "Crosshair Smart Panel — Ketuk untuk show/hide"
- Action buttons:
  - "Show Panel" → tampilkan icon
  - "Hide Panel" → sembunyikan icon
  - "Settings" → buka pengaturan Crosshair

### 6.2 Prioritas
- Low priority (tidak mengganggu)
- Ongoing (tidak bisa di-swipe away)
- Bisa di-collapse ke icon saja (Android 8+)

---

## 7. Accessibility

### 7.1 Touch Target
- Minimal touch target: 48x48dp (standar Android)
- Spacing antar tombol D-pad: minimal 8dp

### 7.2 Content Description
- Semua tombol punya content description untuk screen reader
- Contoh: "Geser crosshair ke atas", "Reset posisi crosshair"

---

## 8. Preferensi yang Disimpan

| Key | Tipe | Default | Keterangan |
|-----|------|---------|------------|
**Smart Panel:**
- `smart_panel_icon_visible` — Icon Smart Panel terlihat (boolean, default: true)
- `smart_panel_icon_x` — Posisi X icon dari edge (int, default: 0)
- `smart_panel_icon_y` — Posisi Y icon (-1 = tengah) (int, default: -1)
- `smart_panel_icon_edge` — Icon menempel di edge mana (string, default: "left")
- `smart_panel_active_module` — Modul yang sedang aktif dikontrol (string, default: "crosshair")

**Crosshair (diatur dari Settings, bukan Smart Panel):**
- `crosshair_offset_x` — Offset X crosshair dari center (int, default: 0)
- `crosshair_offset_y` — Offset Y crosshair dari center (int, default: 0)
- `crosshair_opacity` — Transparansi crosshair (int, default: 100)
- `crosshair_scale` — Ukuran crosshair (int, default: 100)
- `crosshair_color` — Warna crosshair (string, default: "white")
- `crosshair_locked` — Posisi crosshair terkunci (boolean, default: false)

**Overlay Lain (diatur dari Settings):**
- Setiap overlay punya preferensi posisi, format, warna masing-masing
- Warnanya diatur dari menu Settings, bukan dari Smart Panel

---

## 9. File yang Perlu Dibuat/Diubah

| File | Keterangan |
|------|------------|
| `SmartPanelService.java` (baru) | Foreground service untuk Smart Panel |
| `SmartPanelView.java` (baru) | Custom View untuk icon + panel |
| `SmartPanelModuleManager.java` (baru) | Kelola switch antar modul (Crosshair, FPS, Suhu, dll) |
| `SmartPanelCrosshairController.java` (baru) | Handle D-pad, settings, toggle untuk Crosshair |
| `SmartPanelOverlayController.java` (baru) | Handle kontrol untuk overlay lain (FPS, Suhu, dll) |
| `SmartPanelPreferences.java` (baru) | Kelola preferensi Smart Panel |
| `CrosshairOverlayService.java` | Integrasi dengan Smart Panel |
| `OverlayService.java` | Integrasi dengan Smart Panel untuk overlay lain |
| `AndroidManifest.xml` | Tambah service + permission |
| `settings_crosshair.xml` | Tambah opsi Smart Panel |

---

## 10. Estimasi Waktu Pengerjaan

- **SmartPanelService:** ±4–6 jam
- **SmartPanelView:** ±6–8 jam
- **SmartPanelModuleManager:** ±3–4 jam
- **SmartPanelCrosshairController:** ±4–6 jam
- **SmartPanelOverlayController:** ±4–6 jam
- **Integrasi dengan Overlay Services:** ±4–6 jam
- **Notifikasi + Preferensi:** ±2–3 jam
- **Total:** ±27–39 jam (≈3,5–5 hari)

---

## 11. Status

- [ ] SmartPanelService
- [ ] SmartPanelView
- [ ] SmartPanelModuleManager
- [ ] SmartPanelCrosshairController
- [ ] SmartPanelOverlayController
- [ ] SmartPanelPreferences
- [ ] Integrasi dengan CrosshairOverlay
- [ ] Integrasi dengan Overlay lain
- [ ] Notifikasi persistent
- [ ] Preferensi settings
