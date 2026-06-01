# 🛠️ Development — FTxT (FunText)

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
FTxT (FunText) menggunakan model warna ARGB dengan slider.

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

### Preset System (GSON + Metadata v2)
Preset full-konfigurasi disimpan menggunakan SharedPreferences dengan UUID-based index + metadata.

Struktur penyimpanan:
```
KEY_INDEX         = JSON array of PresetIndexItem (metadata, terurut)
KEY_PREFIX+uuid   = String JSON dari objek OverlayPreset (data config)
KEY_HISTORY_<uuid> = JSON array PresetVersion (history hingga 10 items)
```

PresetIndexItem mencakup:
- uuid (UUID unik)
- name (editable)
- createdAt, updatedAt (timestamp)
- tags (List<String>)
- favorite (boolean)
- thumbnailPath (relative to filesDir)

API penting:
- save(Context, name, OverlayPreset) — simpan/overwrite (UUID auto-generate atau keep existing)
- load(Context, name) → OverlayPreset
- rename(Context, oldName, newName)
- delete(Context, name) / deleteMultiple / deleteAll
- moveUp/moveDown — reorder list
- getHistory(Context, name) → List<OverlayPreset>
- revertToHistory(Context, name, historyIndex)
- searchByNameOrTag(Context, query) → List<String>
- setTags(Context, name, tags) / setFavorite(Context, name, bool)
- getThumbnailPath(Context, name) → path (atau null)
- getIndexMetadata(Context) → List<Map> (metadata semua preset)
- mergePreset(base, src, flags) → OverlayPreset (partial apply)
- exportToJson / exportAllToJson / exportToFile
- importFromJson / importManyFromJson / importFromFile
- sharePreset(Activity, name) — export ke file + share via intent

Backward compatibility: format lama (name-key storage) auto-migrate ke UUID index saat diakses pertama.

Thumbnail: 64x64px PNG di-generate saat save berdasarkan OverlayPreset.color, disimpan di filesDir/presets/thumb_<uuid>.png.

History: capped 10 items; item lama di-pop otomatis saat limit tercapai.

---

## 🔢 Versioning

Format:

```
major.removed.restored.minor.patch
```

Contoh: `3.9.2.62.0`

- major = milestone besar / generasi project
- removed = histori fitur dihapus
- restored = histori fitur dipulihkan
- minor = feature release counter
- patch = maintenance / bugfix / optimasi

Project saat ini dalam fase **Beta** — lihat [CHANGELOG.md](CHANGELOG.md) untuk riwayat perubahan lengkap.

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
