# Rencana Refactor: Pecah SettingsActivity.java (703 baris)

> Dokumen rencana pengerjaan — **belum ada perubahan kode**.
> Berlaku untuk FTxT versi berjalan.
> **Tanggal:** 2026-09-09

---

## 1. Latar Belakang & Masalah

`SettingsActivity.java` (703 baris) mencampur beberapa domain dalam satu Activity:

1. **Lifecycle & bind view** — `onCreate` (±227 baris), `onResume`, `onPause`.
2. **Sistem Status Bar Preview (±113 baris)** — `startStatusBarPreview`, `getSelectedStatusBarMode`, `updateStatusBarPreview`, `bindStatusBarScale`, `showDateFormatPopup`, `dp`, `applyStatusBarScaleVisibility` + ±20 field terkait (SeekBar/TextView/RadioGroup preview ikon status bar).
3. **Icon launcher picker (±77 baris)** — `setIcon`, `indexOfIconKey`, `updateIconSelectionUi`, `showIconPicker` + konstanta `ICON_KEYS/NAMES/RES/COMPONENTS`.
4. **Developer lock state (±36 baris)** — `applyDeveloperState`, `turnOffMemoryPanel`, `sendPanelHiddenBroadcast`.
5. **Export database (±60 baris)** — `exportDatabases`, `copyDbToDocuments`.
6. **Permission switches** — `updatePermissionSwitches`.

Masalah:
- File panjang, sulit dinavigasi.
- Pencampuran tanggung jawab (UI icon picker, status bar preview, developer lock, ekspor file) dalam satu Activity.
- Kelompok 2-5 sangat kohesif & sudah jelas batasnya (field + method saling berpasangan).

> Catatan: Mengikuti pola existing `*Controller` / helper di codebase, dan konsisten dengan rencana pemecahan `MainActivity` (`_schedule/SPLIT_FILE/REFACTOR_MAIN_ACTIVITY_SPLIT.md`).

---

## 2. Target Pemisahan

### 2.1 `StatusBarPreviewController.java` (baru) — ±113 baris + ±20 field

Folder: `app/src/main/java/exp/ftxt/ui/StatusBarPreviewController.java`

Menampung seluruh sistem preview ikon status bar:
- Field: `statusBarModeGroup`, `statusBarPreview`, `statusBar*ScaleGroup`, `statusBar*ScaleBar`, `statusBar*ScaleValue`, `statusBarDateTop/Bottom*`, `statusBarPercentRing/Num*`, `statusBarDateLangGroup`, `statusBarDateFormatValue`, `statusBarFormatPopup`, `statusBarPreviewHandler`.
- Method: `startStatusBarPreview()`, `getSelectedStatusBarMode()`, `updateStatusBarPreview()`, `bindStatusBarScale(...)`, `showDateFormatPopup(...)`, `dp(int)`, `applyStatusBarScaleVisibility()`.

```java
statusBarController = new StatusBarPreviewController(this);
statusBarController.bind(rootView);
statusBarController.start();
```

### 2.2 `IconPickerController.java` (baru) — ±77 baris + konstanta

Folder: `app/src/main/java/exp/ftxt/ui/IconPickerController.java`

Menampung seluruh sistem pemilih ikon launcher:
- Konstanta: `ICON_KEYS`, `ICON_NAMES`, `ICON_RES`, `ICON_COMPONENTS`.
- Method: `setIcon(String)`, `indexOfIconKey(String)`, `updateIconSelectionUi(int)`, `showIconPicker(SharedPreferences)`.
- Field: `iconSelectorRow`, `iconSelectionPreview`, `iconSelectionLabel`, `iconSelectionIndex`, `iconPickerDialog`.

```java
iconController = new IconPickerController(this);
iconController.bind(rootView);
```

### 2.3 `DeveloperLockController.java` (baru) — ±36 baris + konstanta password

Folder: `app/src/main/java/exp/ftxt/ui/DeveloperLockController.java`

Menampung sistem kunci Fitur Developer:
- Konstanta `DEBUGGING_PASSWORD`.
- Method: `applyDeveloperState(boolean)`, `turnOffMemoryPanel(SharedPreferences)`, `sendPanelHiddenBroadcast(String)`.
- Field: `debuggingSidebarSwitch`, `debuggingPasswordInput`, `developerStatusLabel`, `debuggingUnlockBtn`, `debuggingRelockBtn`, `memorySidebarSwitch`.

### 2.4 `DbExportHelper.java` (baru) — ±60 baris

Folder: `app/src/main/java/exp/ftxt/utils/DbExportHelper.java`

Menampung logika ekspor database (murni, tanpa UI — memakai Activity hanya untuk konteks & path Documents):
- `exportDatabases()`, `copyDbToDocuments(File)`.

```java
DbExportHelper.export(context);
```

---

## 3. Yang Tetap di SettingsActivity

Setelah refactor, SettingsActivity menjadi shell:
- `onCreate` (bind view utama + wire controller)
- `onResume` / `onPause`
- `applySwitchTint(Switch, boolean)` (dipakai banyak tempat)
- `applyNotifIntervalEnabled(boolean)`
- `updatePermissionSwitches()`
- `notifCustomSwitch`, `notifIntervalRow`, `notifIntervalGroup` (notifikasi kustom)

Estimasi SettingsActivity akhir: **±250–300 baris** (dari 703).

> Catatan: `notifCustomSwitch`/`notifInterval*` bisa juga diekstrak kemudian ke `NotifCustomController` bila dirasa perlu — tapi area ini kecil (±30 baris) dan masih masuk akal untuk bertahan di Activity.

---

## 4. Daftar File yang Terlibat

| File | Aksi |
|------|------|
| `app/src/main/java/exp/ftxt/SettingsActivity.java` | Ubah — potong, delegasi ke controller baru |
| `app/src/main/java/exp/ftxt/ui/StatusBarPreviewController.java` | Baru — preview ikon status bar |
| `app/src/main/java/exp/ftxt/ui/IconPickerController.java` | Baru — pemilih ikon launcher |
| `app/src/main/java/exp/ftxt/ui/DeveloperLockController.java` | Baru — kunci Fitur Developer |
| `app/src/main/java/exp/ftxt/utils/DbExportHelper.java` | Baru — ekspor database |

---

## 5. Aturan Eksekusi (self-check)

1. **Tidak ada perubahan perilaku** — kode hasil ekstraksi identik logikanya. Hanya pindah lokasi, bukan tulis ulang.
2. **Akses field** — `private` field yang dipakai controller ikut dipindah atau di-pass via konstruktor. Jangan ekspos field Activity `public`.
3. **Context** — controller memakai `Activity`/`Context` via konstruktor.
4. **`dp(int)` & `applySwitchTint`** — jika dipakai banyak controller, pindah ke shared util (`UiUtil`) agar tidak duplikat; jika private hanya dipakai dalam satu controller, ikut controller tersebut.
5. **Build setelah tiap file diekstrak** — verifikasi compile hijau bertahap.
6. **Regresi manual** — buka Pengaturan, cek: preview ikon status bar (3 mode + slider + format & bahasa tanggal), pemilih ikon launcher (5 varian), Fitur Developer lock/unlock, saklar panel Info Memori/Debugging, ekspor database ke Documents/FTxT, permission switches, notifikasi kustom.

---

## 6. Urutan Pengerjaan

1. Ekstrak `DbExportHelper` → build hijau.
2. Ekstrak `DeveloperLockController` → build hijau.
3. Ekstrak `IconPickerController` → build hijau.
4. Ekstrak `StatusBarPreviewController` (paling besar) → build hijau.
5. Rapikan SettingsActivity (hapus kode lama, delegasi).
6. Self-check §5 + verifikasi manual.
7. Update CHANGELOG (entry berjalan — satu poin hasil akhir).

> ⚠️ Sesuai AGENTS/agent-rules: jangan build/commit/tag/push sebelum perintah eksplisit. Eksekusi menunggu persetujuan desain.