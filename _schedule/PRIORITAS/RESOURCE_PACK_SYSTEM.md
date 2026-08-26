# Sistem Resource Pack

**Status:** Rencana — belum dikerjakan
**Tujuan:** Memungkinkan pengguna mengimport resource custom (font, reticle, logo) ke FTxT menggunakan file dengan ekstensi khusus.

---

## 1. Ekstensi File

| Ekstensi | Nama | Isi | Contoh |
|----------|------|-----|--------|
| `.ffont` | Font Pack | File font (`.ttf`/`.otf`) | `my_fonts.ffont` |
| `.fimgl` | Logo Pack | File gambar (`.png`/`.jpg`/`.webp`) | `my_logos.fimgl` |
| `.frtc` | Reticle Pack | Reticle (`.png`/`.svg`) | `my_crosshairs.frtc` |
| `.faddon` | Addon Pack | Gabungan semua (fonts + reticles + logos) | `my_addon.faddon` |

**Catatan:** Semua file di atas sebenarnya adalah file ZIP dengan ekstensi khusus.

---

## 2. Struktur File ZIP

### 2.1 `.ffont` (Font Pack)
```
my_fonts.ffont (ZIP)
├── font1.ttf
├── font2.otf
└── font3.ttf
```
- Isi: langsung file font
- Tidak perlu folder di dalam ZIP
- Ekstensi yang diizinkan: `.ttf`, `.otf`

### 2.2 `.fimgl` (Logo Pack)
```
my_logos.fimgl (ZIP)
├── logo1.png
├── logo2.jpg
└── logo3.webp
```
- Isi: langsung file gambar
- Ekstensi yang diizinkan: `.png`, `.jpg`, `.jpeg`, `.webp`

### 2.3 `.frtc` (Reticle Pack)
```
my_crosshairs.frtc (ZIP)
├── crosshair1.png
├── crosshair2.svg
└── crosshair3.png
```
- Isi: langsung file reticle
- Ekstensi yang diizinkan: `.png`, `.svg`

### 2.4 `.faddon` (Addon Pack)
```
my_addon.faddon (ZIP)
├── fonts/
│   ├── font1.ttf
│   └── font2.otf
├── reticles/
│   ├── crosshair1.png
│   └── crosshair2.svg
└── logos/
    ├── logo1.png
    └── logo2.jpg
```
- Isi: 3 folder (`fonts/`, `reticles/`, `logos/`)
- Salah satu folder boleh kosong (misal tidak ada logo)
- Struktur folder wajib sesuai (case-sensitive)

---

## 3. Folder Resource Internal

Setelah import, file disimpan di:
```
/data/data/exp.ftxt/files/resource/
├── fonts/          ← dari .ffont atau .faddon
│   ├── font1.ttf
│   ├── font2.otf
│   └── ...
├── reticles/       ← dari .frtc atau .faddon
│   ├── crosshair1.png
│   ├── crosshair2.svg
│   └── ...
└── logos/          ← dari .fimgl atau .faddon
    ├── logo1.png
    ├── logo2.jpg
    └── ...
```

**Catatan:** Folder ini hanya bisa diakses oleh aplikasi FTxT (internal storage).

---

## 4. Validasi

### 4.1 Validasi File
- File harus valid ZIP (bukan corrupt atau password-protected)
- Ekstensi file dalam ZIP harus sesuai dengan tipe pack
- Tidak boleh ada file ekstensi yang tidak diizinkan

### 4.2 Validasi Ukuran
- Max ukuran per file dalam ZIP: 10MB
- Max total ukuran ZIP: 100MB
- Max jumlah file dalam ZIP: 50 file

### 4.3 Validasi Nama File
- Nama file tidak boleh mengandung karakter khusus: `/\:*?"<>|`
- Nama file tidak boleh kosong
- Nama file harus unik dalam satu pack (tidak ada duplikat)

---

## 5. Proses Import

### 5.1 Flow Import
```
User klik file .ffont/.frtc/.fimgl/.faddon
→ Android tawarkan "Buka dengan FTxT"
→ FTxT terima intent
→ Baca file sebagai ZIP
→ Validasi isi
→ Extract ke folder resource/ yang sesuai
→ Simpan metadata ke database
→ Tampilkan notifikasi sukses/gagal
→ Tampilkan daftar resource yang berhasil diimport
```

### 5.2 Metadata yang Disimpan
Untuk setiap file yang berhasil diimport:
- Nama file asli
- Ukuran file
- Tipe (font/reticle/logo)
- Tanggal import
- Lokasi di folder resource
- Status (aktif/nonaktif)

---

## 6. Penggunaan Resource

### 6.1 Font
- Dipakai untuk teks overlay (nilai FPS, suhu, dll)
- Dipakai untuk teks notifikasi
- User bisa pilih font mana yang aktif

### 6.2 Reticle
- Dipakai untuk crosshair overlay
- Mendukung PNG (raster) dan SVG (vector)
- User bisa pilih reticle mana yang aktif

### 6.3 Logo
- Dipakai untuk watermark overlay
- Belum diimplementasi (coming soon)

---

## 7. Intent Filter (AndroidManifest.xml)

Untuk membuka file dengan FTxT dari file manager:
```xml
<intent-filter>
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <data android:mimeType="application/zip" />
    <data android:scheme="file" />
    <data android:scheme="content" />
    <data android:pathPattern=".*\\.ffont" />
</intent-filter>
```
(Peruslu research lebih lanjut untuk path pattern yang tepat)

---

## 8. UI Settings (Rencana)

### 8.1 Menu Resource Pack
```
[Settings]
├── Resource Pack
│   ├── Fonts (3 tersedia, 1 aktif)
│   ├── Reticles (7 tersedia, 2 aktif)
│   └── Logos (2 tersedia, 0 aktif)
├── Import .ffont
├── Import .frtc
├── Import .fimgl
└── Import .faddon
```

### 8.2 Detail Font
```
[Font Pack]
├── Font 1 (aktif) [Pilih] [Hapus]
├── Font 2 [Pilih] [Hapus]
└── Font 3 [Pilih] [Hapus]
[Import .ffont]
```

---

## 9. Pertanyaan yang Belum Diputuskan

1. **Import via tombol di app** atau **klik file langsung dari file manager**?
2. **Resource langsung aktif** atau **pilih dulu lalu aktifkan**?
3. **Font/reticle bisa dipakai di mana saja** (overlay, notifikasi, dll) atau **hanya di tempat tertentu**?
4. **Bagaimana handle resource duplikat** (nama file sama)?
5. **Apakah perlu fitur share resource pack** ke pengguna lain?

---

## 10. File yang Perlu Dibuat/Diubah

| File | Keterangan |
|------|------------|
| `ResourcePackManager.java` (baru) | Kelola import, validasi, dan akses resource |
| `ResourcePackDb.java` (baru) | Database metadata resource pack |
| `ResourcePackActivity.java` (baru) | UI untuk kelola resource pack |
| `AndroidManifest.xml` | Tambah intent filter untuk file .ffont/.frtc/.fimgl/.faddon |
| `SettingsActivity.java` | Tambah menu Resource Pack |

---

## 11. Estimasi Waktu Pengerjaan

- **ResourcePackManager:** ±4–6 jam
- **ResourcePackDb:** ±2–3 jam
- **ResourcePackActivity:** ±4–6 jam
- **Intent Filter & Validasi:** ±2–3 jam
- **Integrasi dengan Overlay:** ±3–4 jam
- **Total:** ±15–22 jam (≈2–3 hari)

---

## 12. Status

- [ ] ResourcePackManager
- [ ] ResourcePackDb
- [ ] ResourcePackActivity
- [ ] Intent Filter
- [ ] Validasi file
- [ ] Integrasi font dengan overlay
- [ ] Integrasi reticle dengan crosshair
- [ ] Logo (coming soon)
