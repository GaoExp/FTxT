# App Manager

**Status:** Rencana — belum dikerjakan
**Tujuan:** Mengelola aplikasi yang terinstall di perangkat — lihat detail, kelola storage, backup/restore, dan aksi umum.

---

## 1. Fitur

### 1.1 Daftar Aplikasi
- List semua aplikasi terinstall (user apps + system apps)
- Filter: User apps / System apps / Semua
- Search: cari berdasarkan nama aplikasi
- Sort: nama, ukuran, tanggal install, terakhir dibuka

### 1.2 Detail Aplikasi
- Nama aplikasi
- Package name
- Versi (versionCode + versionName)
- Ukuran (APK, data, cache, total)
- Tanggal install
- Tanggal update terakhir
- Permission yang diminta
- Activity utama (launch activity)
- Target SDK / Min SDK

### 1.3 Aksi
- **Open** — buka aplikasi
- **Uninstall** — hapus aplikasi (user apps)
- **Force Stop** — paksa berhenti
- **Clear Cache** — hapus cache
- **Clear Data** — hapus semua data (perlu konfirmasi)
- **App Info** — buka system app info settings
- **Share APK** — bagikan file APK (opsional, untuk user apps)

### 1.4 Storage Info
- Ukuran APK
- Ukuran data
- Ukuran cache
- Total ukuran
- Storage bar visual

### 1.5 Backup & Restore (Opsional)
- Backup APK ke external storage
- Backup APK + data (root only)
- Restore dari backup

---

## 2. Sumber Data

### 2.1 Daftar Aplikasi
- `PackageManager.getInstalledPackages()` — daftar semua package
- `PackageInfo` — info detail per package
- `ApplicationInfo` — info tambahan (flags, sourceDir, dataDir)

### 2.2 Ukuran
- `PackageManager.getPackageSizeInfo()` — API 23+ (deprecated, tapi masih jalan)
- Atau hitung manual dari `sourceDir`, `dataDir`, `cacheDir`
- Fallback: tampilkan ukuran APK saja

### 2.3 Permission
- `PackageInfo.requestedPermissions` — daftar permission
- `PackageManager.getPermissionInfo()` — detail permission
- Group permission: STORAGE, CAMERA, LOCATION, dll

### 2.4 Storage
- `Context.getCacheDir()` — lokasi cache
- `Context.getDataDir()` — lokasi data (API 24+)
- `ApplicationInfo.sourceDir` — lokasi APK

---

## 3. UI/UX

### 3.1 Daftar Aplikasi
- RecyclerView dengan item layout:
  - Icon aplikasi
  - Nama aplikasi
  - Package name (opsional)
  - Ukuran total
  - Tombol aksi (dropdown atau ikon)
- Floating search bar di atas
- Filter chip: User / System / Semua

### 3.2 Detail Aplikasi
- Halaman baru atau bottom sheet
- Header: icon, nama, versi
- Card Storage: bar visual + angka
- Card Permission: daftar permission dengan toggle (opsional)
- Card Aksi: tombol Open, Uninstall, Force Stop, Clear Cache, Clear Data
- Card Info: package name, SDK, tanggal install

### 3.3 Konfirmasi
- Uninstall: "Hapus [nama aplikasi]?"
- Clear Data: "Hapus semua data [nama aplikasi]? Tindakan ini tidak dapat dibatalkan."
- Force Stop: "Paksa berhenti [nama aplikasi]?"

---

## 4. Implementasi

### 4.1 Activity/Fragment
- `AppManagerActivity.java` — halaman utama daftar aplikasi
- `AppDetailActivity.java` — halaman detail aplikasi

### 4.2 Permission
- Tidak perlu permission khusus untuk `getInstalledPackages()`
- Android 11+ perlu `<queries>` di AndroidManifest untuk lihat semua package:
  ```xml
  <queries>
      <intent>
          <action android:name="android.intent.action.MAIN" />
      </intent>
  </queries>
  ```
- Atau minta `QUERY_ALL_PACKAGES` (ini berat, hanya jika benar-benar perlu)

### 4.3 Uninstall
- `Intent(ACTION_DELETE, Uri.parse("package:" + packageName))`
- System akan handle konfirmasi uninstall

### 4.4 Force Stop
- Memerlukan `android.permission.FORCE_STOP_PACKAGES` (system only)
- Atau arahkan ke Settings: `Settings.ACTION_APPLICATION_DETAILS_SETTINGS`

### 4.5 Clear Cache
- `Context.getCacheDir()` → hapus isi folder
- Atau arahkan ke Settings (lebih aman)

### 4.6 Backup APK
- Baca `ApplicationInfo.sourceDir` → copy ke external storage
- Perlu permission `WRITE_EXTERNAL_STORAGE` (Android 9 ke bawah)

---

## 5. Batasan

### 5.1 System Apps
- Tidak bisa uninstall system apps
- Force stop dan clear data terbatas
- Beberapa aksi memerlukan root

### 5.2 Android 11+ (Scoped Storage)
- Akses ke file APK aplikasi lain terbatas
- Backup APK mungkin tidak selalu berhasil

### 5.3 Tidak Semua Data Terlihat
- Ukuran data mungkin tidak akurat untuk beberapa aplikasi
- Permission detail tergantung Android version

---

## 6. File yang Perlu Dibuat

| File | Keterangan |
|------|------------|
| `AppManagerActivity.java` (baru) | Halaman utama daftar aplikasi |
| `AppDetailActivity.java` (baru) | Halaman detail aplikasi |
| `AppListAdapter.java` (baru) | Adapter untuk RecyclerView |
| `AppInfo.java` (baru) | Model data aplikasi |
| `AppManagerUtils.java` (baru) | Helper untuk aksi (uninstall, clear cache, dll) |
| `activity_app_manager.xml` (baru) | Layout halaman utama |
| `activity_app_detail.xml` (baru) | Layout halaman detail |
| `item_app.xml` (baru) | Layout item daftar aplikasi |
| `AndroidManifest.xml` | Registrasi activity + queries |

---

## 7. Estimasi Waktu Pengerjaan

- **AppManagerActivity + Adapter:** ±4–6 jam
- **AppDetailActivity:** ±3–4 jam
- **Utils (uninstall, clear cache, backup):** ±2–3 jam
- **UI/UX (search, filter, sort):** ±2–3 jam
- **Testing:** ±2–3 jam
- **Total:** ±13–19 jam (≈2–2,5 hari)

---

## 8. Status

- [ ] AppManagerActivity
- [ ] AppDetailActivity
- [ ] AppListAdapter
- [ ] AppInfo model
- [ ] AppManagerUtils
- [ ] Layout files
- [ ] AndroidManifest update
- [ ] Testing
