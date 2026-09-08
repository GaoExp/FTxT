# Rencana Implementasi — Smart Panel (Overlay Control)

> **Status:** Rencana — belum dieksekusi
> **Dasar fitur:** `_schedule/PRIORITAS/SMART_PANEL_CROSSHAIR.md`
> **Referensi arsitektur:** `_schedule/RENCANA/MULTI_MODULE_SPLIT.md`
> **Revisi:** 2026-09-07 — dibuat dengan prinsip "siap split multi-module sejak awal"

---

## 1. Tujuan

Smart Panel adalah panel kontrol mengambang untuk mengubah posisi & pengaturan modul overlay secara real-time tanpa membuka aplikasi FTxT.

Rencana ini mengutamakan satu hal: **arsitektur yang tidak memaksa Smart Panel menjadi module yang menembus semua feature module** sehingga saat `MULTI_MODULE_SPLIT` dieksekusi, Smart Panel ikut terpisah tanpa refactor besar.

## 2. Kendala & Prinsip (dari RENCANA/MULTI_MODULE_SPLIT.md)

- Feature modules **TIDAK boleh** depend ke feature module lain.
- Feature modules **TIDAK boleh** depend ke core (`FloatingService` dll).
- Core (`:core:service`) **boleh** depend ke semua feature modules.
- Shared modules **TIDAK boleh** depend ke feature/core.
- Package name & prefs key tidak berubah saat split.

Konsekuensi: Smart Panel **tidak boleh jadi `:feature:smart-panel`** (karena ia mengontrol semua modul). Ia ideologinya hidup di **core layer** (di dalam `:core:service` saat split), dan berkomunikasi dengan feature modules lewat **interface yang ditaruh di `:shared:ui`**.

## 3. Prinsip Arsitektur — "Consumer/Implementor"

Tiga peran, arah dependensi hanya satu arah:

```
:shared:ui                          (interface SmartPanelTarget — kontrak, 0 dependensi)
   ▲ implements
:feature:<x>                        (tiap modul overlay menawarkan kontrol dirinya sendiri)
   ▲ (core boleh depend feature)
:core:service                       (SmartPanelModule + Registry — consumer semua target)
   ▲
:app                                (toggle fitur + permulaan start)
```

- **Interface** di `:shared:ui` → feature module implement di dirinya sendiri.
- **Registry & SmartPanel module** di core → hanya berinteraksi lewat interface, tidak menyentuh config/module feature secara tersebar.
- Saat split: file pindah mengikuti module-nya masing-masing; **tidak ada import silang yang harus dirombak** karena arah dependensi sudah benar sejak awal.

## 4. Komponen & Lokasi File

### 4.1 Saat ini (single module `app/`)

```
app/src/main/java/exp/ftxt/
├── shared/ui/
│   └── SmartPanelTarget.java          ← [BARU] interface kontrak (di :shared:ui saat split)
├── features/<x>/                      ← tiap modul overlay implement SmartPanelTarget
│   └── <X>Module.java                 ← CrosshairModule, FpsModule, ClockModule,
│                                         BatteryStatsModule, BatteryBarModule,
│                                         NetworkModule, MemoryModule  +implements
├── core/
│   ├── SmartPanelConfig.java          ← [BARU] config statis + prefs key
│   ├── SmartPanelRegistry.java        ← [BARU] daftar target + tindakan on/off
│   ├── SmartPanelModule.java          ← [BARU] OverlayModule: icon bulat + panel kontrol
│   └── FloatingService.java           ← [UBAH] registrasi registry + start/stop modul
└── SettingsActivity.java / MainActivity   ← [UBAH] toggle "Smart Panel" + status
```

### 4.2 Setelah MULTI_MODULE_SPLIT

| Komponen | Pindah ke | Alasan |
|----------|-----------|--------|
| `SmartPanelTarget.java` | `:shared:ui` | kontrak bersama, zero dependency |
| implement `SmartPanelTarget` di tiap module | `:feature:<x>` (ikut modulnya) | tiap feature membawa kontrolnya sendiri |
| `SmartPanelConfig/Registry/Module` | `:core:service` | consumer semua feature, boleh depend semuanya |
| toggle di Settings | `:app` | UI shell tetap di app |

Dengan ini **tidak ada fase baru yang perlu ditambahkan** ke MULTI_MODULE_SPLIT — Smart Panel terdistribusi alami ke module yang sudah direncanakan.

## 5. Kontrak Interface `SmartPanelTarget` (draft)

Berada di `exp.ftxt.shared.ui` (saat ini) / `:shared:ui` (nanti). Menyediakan **kemampuan modul yang tidak membutuhkan service** — semua aksi memakai config static + instance module itu sendiri.

```java
public interface SmartPanelTarget {
    /** Nama tampil, mis. "Crosshair", "FPS Display". */
    String getTitle();

    /** Status aktif dibaca dari config statis modul (mis. CrosshairConfig.enabled). */
    boolean isEnabled();

    /** Geser relatif dalam fraksi layar (dx px / layarW, dy px / layarH) —
     *  seragam dengan DpadController. Implement mengubah posX/posY modul
     *  lalu memanggil updatePosition() sendiri. */
    void moveBy(float dxNorm, float dyNorm);

    /** Kembalikan posisi ke tengah layar. */
    void resetPosition();

    /** "Lock" = touch passthrough aktif (posisi tidak bisa digeser langsung). */
    boolean isPositionLocked();
    void setPositionLocked(boolean locked);

    // ── Kontrol spesifik opsional ──
    default boolean canSetOpacity() { return false; }
    default int getOpacity() { return 100; }
    default void setOpacity(int percent) {}

    default boolean canSetSize() { return false; }
    default float getSize() { return 0f; }
    default void setSize(float size) {}

    default boolean canResetConfig() { return false; }
    default void resetConfig() {}
}
```

Tidak ada `on/off` di interface (perlu service) → ditangani Registry di core (bagian 6).

## 6. Registry di Core — Titik Tunggal On/Off

`FloatingService` (core) mendaftarkan tiap modul yang didukung ke `SmartPanelRegistry` lewat `Entry`:

```java
class Entry {
    String id;                       // "crosshair", "fps", ...
    String title;
    Supplier<SmartPanelTarget> target;   // resolve module instance (dari FloatingService.static accessor)
    BooleanSupplier enabledGetter;        // baca config statis
    Runnable enableAction;                // set config+prefs + FloatingService.startModule(...)
    Runnable disableAction;               // set config+prefs + FloatingService.stopModule(...)
}
```

- Fitur module hanya "menawarkan" kemampuan lewat `SmartPanelTarget`; **on/off dirakit di core** karena di situlah kewenangan start/stop (sudah dilakukan `FloatingService.startModule/stopModule`).
- Penambahan/pengurangan modul cukup satu blok `register(...)` di `FloatingService` — tidak ada hardcode tersebar.

## 7. `SmartPanelModule` — Overlay Icon + Panel

Pola mengikuti modul overlay existing (CrosshairModule): buat view sendiri, add ke `WindowManager` via `FloatingService`, tanpa service/notifikasi tambahan.

- **Window**: satu `LinearLayout` root berisi:
  - **Icon** (48dp, bulat, semi-transparan, setengah tahan sentuh) — draggable + snap edge.
  - **Panel** (≈200×180dp, rounded 16dp, background gelap semi-transparan) — default GONE.
- Kondisi muncul: hanya saat ada modul aktif yang didukung (atau tetap tampil dengan daftar modul; keputusan dapat ditunda ke implementasi, lihat asumsi §10).
- Interaksi (sesuai SMART_PANEL_CROSSHAIR.md):
  - tap icon → toogle panel; tap di luar panel → tutup.
  - D-Pad 4 arah (+ tombol interval 1–20px) & tombol tengah reset — menyambung ke `target.moveBy(...)` / `target.resetPosition()`.
  - tombol On/Off → `entry.enableAction()/disableAction()`.
  - tombol Lock → `target.setPositionLocked(...)`.
  - kontrol spesifik (Opacity/Size/Reset) — tampil hanya bila `canSetOpacity()/canSetSize()/canResetConfig()`.
  - dropdown modul → set `smart_panel_active_module` + replace target aktif.
- Gesture root ikut pola `OverlayDragHandler` untuk memindahkan icon.

## 8. Preferensi (prefs key)

Semua di `ftxt_prefs`, konsisten prefix `smart_panel_`:

| Key | Tipe | Default |
|-----|------|---------|
| `smart_panel_enabled` | boolean | false |
| `smart_panel_icon_visible` | boolean | true |
| `smart_panel_icon_x` | int | 0 |
| `smart_panel_icon_y` | int | -1 (tengah vertikal) |
| `smart_panel_icon_edge` | string | "left" |
| `smart_panel_active_module` | string | "crosshair" |

## 9. Dukungan Modul (tahapan)

| Modul | Posisi | Lock | Opacity | Size | Reset Config |
|-------|--------|------|---------|------|--------------|
| Crosshair | ✓ | ✓ (touchPassthrough) | ✓ | ✓ | (opsional) |
| FPS Display | ✓ | ✓ | — | ✓ (ukuran teks) | (opsional) |
| Jam Digital | ✓ | ✓ | — | ✓ | (opsional) |
| Battery Info | ✓ | ✓ | — | ✓ | (opsional) |
| Battery Strip | ✓ (manual mode) | ✓ | — | — | (opsional) |
| Network Speed | ✓ | ✓ | — | ✓ | (opsional) |
| Memory Stats | ✓ | ✓ | — | ✓ | (opsional) |

Fase 1: hanya Posisi + Lock + On/Off (generik, cepat rampung).
Fase 2: Opacity/Size khusu Crosshair.
Fase 3: Opacity/Size/Reset untuk modul lain (opsional, bertahap).

## 10. Tahapan Eksekusi

1. **Interface & registry** — `SmartPanelTarget` + `SmartPanelRegistry` + `SmartPanelConfig`.
2. **Implement target per modul** — module overlay existing `implements SmartPanelTarget` (crosshair dulu, lalu fps, clock, battery, network, memory, battery bar).
3. **`SmartPanelModule`** — ikon + panel + D-Pad + toggle + lock.
4. **Integrasi** — FloatingService: `register(...)` semua modul, buat/start modul saat `smart_panel_enabled`, stop bersih saat off.
5. **Toggle di UI** — `SettingsActivity` (+ status aktif) & handle restart.
6. **Kontrol spesifik** — Crosshair opacity/size (fase 2), lalu modul lain.
7. **Dokumen** — README/PANDUAN/STRUKTUR + CHANGELOG (minor bump: **4.93.0**).

## 11. Keputusan & Asumsi (untuk konfirmasi)

1. Smart Panel hidup di **core layer** (bukan feature module sendiri) — prasyarat ramah-split.
2. **Tanpa service/notifikasi baru** — tetap satu `FloatingService` foreground (mengikuti arsitektur sekarang).
3. Toggle fitur ada di **halaman Konfigurasi** (`SettingsActivity`).
4. Fitur tidak menambahkan layout/drawable XML baru — UI ikon & panel dibangun di kode (konsisten CrosshairModule, dan bebas konflik resource saat split).
5. Modul Logo Display & Color Picker & Floating Text **tidak** masuk daftar kontrol tahap awal (placeholder / tidak relevan di-overlay).
6. `smart_panel_*` dipakai sebagai namespace prefs; key lama di dokumen asli diadaptasi.

## 12. Risiko

| Risiko | Mitigasi |
|--------|----------|
| Interface `SmartPanelTarget` kurang lengkap → sering revisi | Mulai dengan method generik (fase 1); tambah only if needed |
| module overlay tidak punya cara `moveBy` yang seragam | Semua modul sudah punya posX/posY normalized + `updatePosition()`; implementasi per modul tipis |
| Update posisi real-time memicu invalidate berlebihan | Panggil `updatePosition()` hanya saat aksi; tidak ada loop |
| Gesture D-Pad + drag icon overlap | Icon & panel berada di `View` yang sama dengan touch handling dipisah per child |
| Duplikasi logika on/off modul | Dipusatkan di `SmartPanelRegistry` (satu tempat), bukan tersebar |