# Rencana Refactor: Pecah MemoryPanelController.java (958 baris)

> Dokumen rencana pengerjaan — **belum ada perubahan kode**.
> Berlaku untuk FTxT versi berjalan.
> **Tanggal:** 2026-09-09

---

## 1. Latar Belakang & Masalah

`MemoryPanelController` (958 baris) mengelola **dua tab** dalam satu file:
- Tab **Monitor** — pemantauan memori: ram bar, metrik real-time, status badge, polling, copy/export snapshot.
- Tab **Overlay** — konfigurasi: switch, size, warna, shadow, background, interval, OrderZones.

Campuran ini membuat:
- Field 20-an `memMonitor*` + `memTabMonitor` + `memBottomNav` (tab Monitor) bercampur dengan field konfig overlay (`memSwitch`, `memSize*`, `memShadow*`, `memBg*`) di satu kelas.
- Method tab Monitor ±280 baris (`setupMonitorTab` → `exportMemorySnapshot`) sulit dinavigasi terpisah dari logika overlay.

> Pola pemisahan ini **sudah ada** di codebase: battery memisahkan `BatteryMonitorTabController` dari `BatteryPanelController`. Memory akan mengikuti pola yang sama.

---

## 2. Target: `MemoryMonitorTabController.java` (baru, ±350 baris)

Folder: `app/src/main/java/exp/ftxt/ui/MemoryMonitorTabController.java`

### Konten yang pindah dari MemoryPanelController

**Field:**
- `memTabMonitor`, `memBottomNav`
- Semua `memMonitor*` (fbi text, runtime, system, ram total/used/percent, ram bar, status badge, export/copy button)

**Method:**
- `setupMonitorTab()`
- `handleMonitorToggle()`, `startManualMonitor()`, `stopManualMonitor()`
- `handleBgMonitorSwitch(boolean)`, `disableOverlayIfRunning()`
- `showStopBackgroundDialog()`, `showBackgroundRequiredDialog()`, `blinkBgSwitch()`
- `updateMonitorToggleButton()`, `updateMonitorStatusBadge()`
- `resumeMonitorPolling()`, `stopMonitorPolling()`, `updateMonitorInfo()`
- `appendLine(...)` (×2 overload)
- `copyToClipboard()`, `exportMemorySnapshot()`

**Interaksi:**
- `MemoryPanelController.setupMonitorTab()` → cukup instansiasi `MemoryMonitorTabController` + delegasi.
- `MemoryMonitorTabController` membutuhkan akses: `MainActivity`, `MemoryConfig`, `MemoryMonitor`, prefs, dan callback ke panel overlay (mis. saat switch background diaktifkan → `disableOverlayIfRunning` di panel utama). Pass via konstruktor / listener.

### Yang tetap di MemoryPanelController (±600 baris)

- Tab Overlay: `bindViews` (bagian overlay), `loadConfig`, `setupListeners` (size/warna/shadow/bg/interval), `saveMemShadowPrefs`, `setupOrderZones`/`onOrderChanged`, preset, `onResume/onPause/onPanelShown/onPanelHidden/cleanup`.

---

## 3. Aturan Eksekusi (self-check)

1. **Tidak ada perubahan perilaku** — kode hasil ekstraksi identik logikanya; hanya pindah lokasi.
2. **Signature publik dipertahankan** — `onPanelShown/Hidden`, `cleanup`, `showLoadPresetDialog` tetap ada di panel utama dan mendelegasikan ke controller Monitor bila perlu.
3. **State sharing** — `MemoryConfig`, `MemoryMonitor`, prefs dipassing via konstruktor/getter; jangan ekspos field Activity `public`.
4. **Build hijau** setelah pemindahan (satu langkah, build, cek).
5. **Regresi manual** — buka panel Memory: tab Monitor (polling real-time, badge status, copy/export snapshot) & tab Overlay (switch background, OrderZones, shadow/bg/size, interval) tetap berfungsi; Fitur Developer lock tetap mengunci tombol salin/ekspor.

---

## 4. Daftar File yang Terlibat

| File | Aksi |
|------|------|
| `app/src/main/java/exp/ftxt/ui/MemoryPanelController.java` | Ubah — potong tab Monitor, delegasi |
| `app/src/main/java/exp/ftxt/ui/MemoryMonitorTabController.java` | Baru — tab Monitor + snapshot |

---

## 5. Urutan Pengerjaan

1. Buat `MemoryMonitorTabController` (pindahkan field + method tab Monitor).
2. `MemoryPanelController` delegasi ke controller baru.
3. Build hijau + verifikasi manual §3.
4. Update CHANGELOG (entry berjalan — satu poin hasil akhir).

> ⚠️ Sesuai AGENTS/agent-rules: jangan build/commit/tag/push sebelum perintah eksplisit. Eksekusi menunggu persetujuan desain.