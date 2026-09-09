# Rencana Refactor: Pecah ColorPickerPanelController.java (729 baris)

> Dokumen rencana pengerjaan — **belum ada perubahan kode**.
> Berlaku untuk FTxT versi berjalan.
> **Tanggal:** 2026-09-09

---

## 1. Latar Belakang & Masalah

`ColorPickerPanelController` (729 baris) mencampur beberapa kelompok field & method yang **cohesif** (saling berpasangan) dalam satu kelas:

1. **RGB slider group** — `redThumb`/`greenThumb`/`blueThumb`, `redTouchArea`..., `redGradientBg`..., `redValLabel`...; `onRgbChanged`, `applyRed/Green/BlueGradient`.
2. **Saved colors** — `savedColorsGrid`, `savedColorsCount`, `addSavedColor`, `collapseToggle`; `loadSavedColors`, `saveColor`, `removeSavedColor`, `setEmptyCell`, `recalcGridCellSizes`.
3. **HSL/alpha slider + wheel** — `hueThumb`...`alphaThumb`, touch areas, gradient bg, labels; `setupSliders`, `setupSliderTouch`, `applyHue/Sat/Value/AlphaGradient`, `initGradientDrawables`.

Kelompok 2 (saved colors grid) dan mekanisme slider (lewat `setupSliderTouch`) adalah dua abstraksi mandiri yang bisa dipisah bersih.

---

## 2. Target Pemisahan

### 2.1 `ColorSliderGroup.java` (baru, ±180 baris)

Folder: `app/src/main/java/exp/ftxt/ui/ColorSliderGroup.java`

Abstraksi umum untuk slider **RGB** dan **HSL/alpha**:
- `setupSliderTouch(View touchArea, View thumb, int max, ...)` (mekanisme seret thumb → progress)
- `setThumbPos(View thumb, int progress, int max)`
- `applyHueGradient(View bar)`, `applySatGradient(View bar, int hue)`, `applyValueGradient(View bar, int hue, float sat)`, `applyAlphaGradient(View bar, int color)`, `applyRed/Green/BlueGradient(View bar)`
- Field thumbs/touchareas/gradients/labels yang berkaitan
- `dpToPx(Context, int)`

**Interaksi:** Panel utama mem-pass callback onchange (perubahan slider → panel menghitung & meng-update warna), sehingga kalkulasi warna tetap terpusat di panel. `ColorSliderGroup` hanya menangani seret thumb + menerapkan gradient bar.

### 2.2 `SavedColorsGrid.java` (baru, ±130 baris)

Folder: `app/src/main/java/exp/ftxt/ui/SavedColorsGrid.java`

Seutuhnya mandiri:
- Field `savedColorsGrid`, `savedColorsCount`, `addSavedColor`, `collapseToggle`
- `loadSavedColors()`, `saveColor(int, boolean)`, `removeSavedColor(int)`, `setEmptyCell(View, int)`, `recalcGridCellSizes()`
- Callback ke panel: ketika sel warna diketuk → panel menerapkan warna (`setColorValues`); ketika hapus/simpan → panel refresh.

### 2.3 Yang tetap di ColorPickerPanelController (±420 baris)

- Wheel: `colorWheel`, array `{hue,sat,val,opa,red,green,blue}Prog`, `isUpdating`, `lastNamedColor`, `lastColorName`, cache warna
- Preview, HEX editor (`showHexEditor`), name toggle (`nameToggle`, `showColorName`)
- `updateDisplays(int)`, `setColorValues(int)`, `updateFromColor(int)`, `updateSliderOutput`
- `initGradientDrawables`, `setupTransparencyChecker`, `setupListeners`
- `onPanelShown`, `cleanup`
- Wire `ColorSliderGroup` + `SavedColorsGrid` di `bindViews`/`setupListeners`

---

## 3. Aturan Eksekusi (self-check)

1. **Tidak ada perubahan perilaku** — logika identik; hanya pindah lokasi.
2. **Signature publik dipertahankan** — `onPanelShown`, `cleanup`, dan method yang dipanggil dari luar tetap ada di panel utama.
3. **State sharing** — warna aktif & Progress array tetap di panel utama; kelompok slider & saved colors hanya bekerja via callback.
4. **Build hijau** setelah tiap bagian diekstrak.
5. **Regresi manual** — panel Color Picker & dialog color picker: wheel sync dengan slider HSL & RGB, hex editor, name auto-detect, saved colors (simpan/hapus/tap sel/hitung ulang grid saat resize), collapse header.

---

## 4. Daftar File yang Terlibat

| File | Aksi |
|------|------|
| `app/src/main/java/exp/ftxt/ui/ColorPickerPanelController.java` | Ubah — potong slider group & saved colors |
| `app/src/main/java/exp/ftxt/ui/ColorSliderGroup.java` | Baru — mekanisme slider HSV + RGB |
| `app/src/main/java/exp/ftxt/ui/SavedColorsGrid.java` | Baru — grid saved colors |

---

## 5. Urutan Pengerjaan

1. Ekstrak `ColorSliderGroup` → build hijau.
2. Ekstrak `SavedColorsGrid` → build hijau → uji simpan/hapus warna.
3. Rapikan `ColorPickerPanelController` (delegasi, hapus kode lama).
4. Verifikasi manual §3.
5. Update CHANGELOG (entry berjalan — satu poin hasil akhir).

> ⚠️ Sesuai AGENTS/agent-rules: jangan build/commit/tag/push sebelum perintah eksplisit. Eksekusi menunggu persetujuan desain.