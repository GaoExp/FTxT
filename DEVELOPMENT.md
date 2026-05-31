# 🛠️ Development — FTxT

## 💻 Environments

### System Requirements
- Android 8.0+ (API 26+)
- Target SDK 35
- Compile SDK 35
- Java 1.8+

### Required Permissions
- `android.permission.SYSTEM_ALERT_WINDOW`
- `android.permission.FOREGROUND_SERVICE`
- `android.permission.POST_NOTIFICATIONS`
- `android.permission.WAKE_LOCK`
- `android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`

Catatan:
- `POST_NOTIFICATIONS` diminta saat runtime pada Android 13+.
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` membuka pengaturan baterai jika diperlukan.

---

## 🎯 Dokumentasi Teknis

### ARGB Color Model
FTxT menggunakan model warna ARGB dengan slider.

```
Alpha (0–255) = Transparansi
Red (0–255)   = Komponen merah
Green (0–255) = Komponen hijau
Blue (0–255)  = Komponen biru
```

Informasi HSV juga ditampilkan sebagai referensi tambahan.

### Real-Time Updates
Semua perubahan diterapkan tanpa restart overlay.

```
Color Change → updateTextColorStatic()
Text Change  → updateTextStatic()
Size Change  → updateTextSizeStatic()
Touch Mode   → updateTouchFlagsStatic()
Background   → updateTextBackgroundStatic() / updateFpsBackgroundStatic() / updateNetworkBackgroundStatic()
Shadow       → updateShadowStatic()
```

### Position Persistence
Posisi overlay disimpan menggunakan SharedPreferences dengan pemisahan orientasi.

Behaviour:
```
ACTION_UP     → Simpan posisi drag
onDestroy()   → Simpan posisi akhir
onCreate()    → Restore posisi terakhir
```

SharedPreferences key:
```
text_pos_x_port   → Posisi X mode potret
text_pos_y_port   → Posisi Y mode potret
text_pos_x_land   → Posisi X mode landscape
text_pos_y_land   → Posisi Y mode landscape
shadow_enabled
```

### Preset System (GSON)
Preset full-konfigurasi disimpan sebagai file JSON di `context.getFilesDir()/presets/<nama>.json`.

Setiap preset mencakup seluruh field `OverlayPreset`: posisi X/Y, ukuran teks, warna (ARGB int), shadow (warna, blur, offset X/Y), background (enabled, warna, padding, offset X/Y, margin, radius), dan orientasi.

Manager: `PresetManager` (static methods) — Save, Load, Rename, Delete, DeleteAll, Export (clipboard/file), Import (clipboard/file), ListAll.

---

## 🔢 Versioning

Format:

```
major.removed.restored.minor.patch
```

Contoh: `1.0.0.12.2`

- major = milestone besar / generasi project
- removed = histori fitur dihapus
- restored = histori fitur dipulihkan
- minor = feature release counter
- patch = maintenance / bugfix / optimasi

Lihat [CHANGELOG.md](CHANGELOG.md) untuk riwayat perubahan lengkap.

---

## 🛠️ Catatan Development

### Code Style
- Java 1.8 compatible
- XML → snake_case
- Class → CamelCase
- Method → camelCase

### Static Helper Methods
Untuk komunikasi real-time antara Activity dan Service.

```java
FloatingService.updateTextColorStatic();
FloatingService.updateTextStatic();
FloatingService.updateTextSizeStatic();
FloatingService.updateTouchFlagsStatic();
FloatingService.updateShadowStatic();
```

### Settings Documentation
File dokumentasi disimpan di folder `assets/` dan dibaca langsung saat runtime:
- `assets/CHANGELOG.txt`
- `assets/README.txt`
- `assets/STRUKTUR.txt`
- `assets/PANDUAN.txt`
- `assets/DEVELOPMENT.txt`
- `assets/TENTANG.txt`
