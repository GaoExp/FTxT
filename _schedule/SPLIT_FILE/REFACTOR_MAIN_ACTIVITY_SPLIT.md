# Rencana Refactor: Pecah MainActivity.java (934 baris)

> Dokumen rencana pengerjaan — **belum ada perubahan kode**.
> Berlaku untuk FTxT versi berjalan.
> **Tanggal:** 2026-09-09

---

## 1. Latar Belakang & Masalah

`MainActivity.java` (934 baris) mengemban terlalu banyak tanggung jawab dalam satu file:

1. **Lifecycle & service** — `onCreate`, `onResume`, `onDestroy`, `autoRequestAndStart`, `killService`, `forceClose`, dll.
2. **Izin aplikasi** — `requestAllPermissionsOnFirstLaunch`, `requestRemainingPermissions`, `checkOverlayPermission`, `checkNotificationPermission`, `checkBatteryOptimization`, `applyCheckboxTint`.
3. **`loadShadowConfigs()` (±190 baris)** — muat seluruh SharedPreferences semua modul (Text, Fps, Clock, Battery, Network, Memory, Crosshair).
4. **Sistem Sidebar (±300 baris)** — `initSidebar`, `refreshSidebar`, `updateNavSelection`, `saveSidebarState`, `addMissingDefaultItems`, `removeStaleItems`, inner class `SidebarItem`, `SidebarAdapter` + `ViewHolder`, `setToolbarSubtitle`, `updateActionBarTitle`.
5. **Settings popup** — `showSettingsPopup`.

Masalah:
- Sulit dibaca & dinavigasi (file terlalu panjang).
- Pencampuran tanggung jawab (transisi service, manajemen izin, konfigurasi, dan UI sidebar dalam satu Activity).
- Kode kandidat ekstraksi jelas dikelompokkan ke 3 domain terpisah.

> Catatan: Codebase sudah punya pola memecah logika ke controller terpisah (`PanelManager`, `*PanelController`, `*PositionController`). Refactor ini mengikuti pola existing yang sama.

---

## 2. Target Pemisahan

### 2.1 `SidebarController.java` (baru) — ±300 baris diekstrak

Folder: `app/src/main/java/exp/ftxt/ui/SidebarController.java`

Menampung seluruh sistem sidebar dari MainActivity:
- `initSidebar()`, `refreshSidebar()`, `updateNavSelection(int)`
- `saveSidebarState()`, `addMissingDefaultItems(List)`, `removeStaleItems(List)`
- Inner class `SidebarItem`
- `SidebarAdapter` + `ViewHolder` (RecyclerView.Adapter)
- `setToolbarSubtitle(CharSequence)` & overload berukuran
- `updateActionBarTitle(int)`

MainActivity cukup memanggil:
```java
sidebarController = new SidebarController(this, recyclerView, toolbar);
sidebarController.init();
```

Kembalikan seleksi item nav ke MainActivity via `OnItemSelectedListener` (interception untuk memantau module switch — perilaku sama dengan refactor `PanelManager`).

### 2.2 `ConfigLoader.java` (baru) — ±190 baris diekstrak

Folder: `app/src/main/java/exp/ftxt/utils/ConfigLoader.java`

Menampung `loadShadowConfigs()` — memuat seluruh prefs semua modul (TextConfig, FpsConfig, ClockConfig, Battery*, NetworkConfig, MemoryConfig, CrosshairConfig, dll).

```java
ConfigLoader.loadAll(Context context);
```

String-kunci dan nilai default **tidak berubah** — hanya lokasi kode yang pindah.

### 2.3 `PermissionHandler.java` (baru) — ±160 baris diekstrak

Folder: `app/src/main/java/exp/ftxt/utils/PermissionHandler.java`

Menampung seluruh manajemen izin:
- `requestAllPermissionsOnFirstLaunch()`, `requestRemainingPermissions()`
- `autoRequestAndStart()`, `checkOverlayPermission()`, `checkNotificationPermission()`, `checkBatteryOptimization()`
- `applyCheckboxTint(CheckBox, boolean)`
- `onRequestPermissionsResult` logic

MainActivity mendelegasikan: `permissionHandler.handleRequestResult(requestCode, permissions, grantResults)`.

---

## 3. Yang Tetap di MainActivity

Setelah refactor, MainActivity menjadi shell yang ramping:
- `onCreate` (obatkan panel, deklarasi view, Wire controller)
- `onResume` / `onPause` / `onDestroy` / `onBackPressed`
- `onCreateOptionsMenu` / `onOptionsItemSelected` (menu toolbar: settings, theme, exit)
- `showSettingsPopup()`, `killService()`, `forceClose()`
- Wiring `PanelManager`, controller baru
- Delegasi ke `SidebarController`, `ConfigLoader`, `PermissionHandler`

Estimasi MainActivity akhir: **±300–350 baris** (dari 934).

---

## 4. Daftar File yang Terlibat

| File | Aksi |
|------|------|
| `app/src/main/java/exp/ftxt/MainActivity.java` | Ubah — potong, delegasi ke controller baru |
| `app/src/main/java/exp/ftxt/ui/SidebarController.java` | Baru — sistem sidebar + adapter |
| `app/src/main/java/exp/ftxt/utils/ConfigLoader.java` | Baru — muat seluruh config prefs |
| `app/src/main/java/exp/ftxt/utils/PermissionHandler.java` | Baru — manajemen izin |

---

## 5. Aturan Eksekusi (self-check)

1. **Tidak ada perubahan perilaku** — kode hasil ekstraksi harus identik logikanya. Hanya pindah lokasi, bukan tulis ulang.
2. **Akses field** — semua `private` field yang dipakai controller hasil ekstraksi (prefs, view refs, toolbar, selection state) ikut dipindah atau di-pass via konstruktor/getter. Jangan mengekspos field MainActivity `public`.
3. **Context** — controller memakai `Activity`/`Context` via konstruktor.
4. **Toolbar title/subtitle** — tetap divisualkan dari MainActivity method (`setToolbarSubtitle(CharSequence, float)` masih dipanggil panel lain — pertahankan delegasi).
5. **Build setelah tiap file diekstrak** — verifikasi compile hijau bertahap (1 file per langkah + build), baru lanjut file berikutnya.
6. **Regresi manual** — setelah selesai: buka app, ganti modul via sidebar (title/subtitle ikut berubah), drag-reorder sidebar, keluar melalui tombol Keluar, auto-start, permintaan izin pertama, toggle tema.

---

## 6. Urutan Pengerjaan

1. Ekstrak `ConfigLoader` → build hijau.
2. Ekstrak `PermissionHandler` → build hijau.
3. Ekstrak `SidebarController` (paling besar) → build hijau.
4. Rapikan MainActivity (hapus kode lama, delegasi).
5. Self-check §5 + verifikasi manual.
6. Update CHANGELOG (entry berjalan, sesuai §2.4 — satu poin hasil akhir).

> ⚠️ Sesuai AGENTS/agent-rules: jangan build/commit/tag/push sebelum perintah eksplisit. Eksekusi menunggu persetujuan desain.