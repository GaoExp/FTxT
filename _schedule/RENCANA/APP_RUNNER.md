# App Runner — Jaga Aplikasi Berjalan di Latar

**Status:** Rencana — belum dikerjakan
**Tujuan:** Memilih aplikasi mana yang akan "dijaga" tetap hidup di latar, supaya tidak langsung dibunuh atau di-hibernate sistem saat FTxT dipakai untuk aktivitas lain.

---

## 1. Gambaran

Pengguna memilih aplikasi dari daftar (semua aplikasi yang punya ikon launcher), lalu FTxT menjaga prosesnya tetap hidup: FTxT memantau, dan bila proses aplikasi itu mati, FTxT meluncurkannya kembali. Aplikasi tidak harus dibuka tiap kali — cukup aktifkan sekali, lalu pakai HP untuk hal lain.

Berkaitan dengan rencana lain:

- `APP_MANAGER.md` — memakai sumber data yang sama (daftar aplikasi terinstall). Kalau `APP_MANAGER` dikerjakan lebih dulu, adapter/utility daftar aplikasi harus dipakai ulang, jangan diduplikasi.
- `BYPASS_CHARGING_SHIZUKU.md` & `SHIZUKU_FPS_READER.md` — infra Shizuku (SDK, provider, helper eksekusi shell) akan sama; kerjakan sekali, pakai bersama.

---

## 2. Batas Platform (baca dulu sebelum implementasi)

Fitur ini **bukan** replika Background-Run. Aplikasi `Background-Run` bekerja dengan cara menjalankan daemon di luar Android framework memakai shell UID (Root / Shizuku / ADB wireless debugging) sehingga proses app lain benar-benar dianggap foreground. Source code-nya tidak dipublikasikan (repo publiknya hanya berisi `res/strings.xml`), jadi tidak ada kode yang bisa dipelajari langsung.

Tanpa shell UID, aplikasi biasa **tidak bisa** memaksa proses app lain naik ke prioritas "foreground". Yang bisa dilakukan:

- **Menahan proses dari dibunuh** — inilah yang FTxT kerjakan, dan ini yang paling bernilai.
- **Menjaga dari doze/hybrid** — butuh Shizuku (Tier B).
- **Menjaga agar app yang `onPause` tidak berhenti** — dibatasi oleh Android, hanya mungkin lewat Tier C (eksperimental).

Konsekuensi yang harus diterima di desain: bila aplikasi target sengaja berhenti saat `onPause` (banyak game auto-battle), prosesnya masih hidup tetapi aktivitasnya tetap berhenti. Fitur ini menjaga *proses*, bukan *aktivitas*.

---

## 3. Fitur

### 3.1 Halaman "App Runner"
- Daftar aplikasi yang punya launcher (ikon + nama), dengan search bar
- Sort nama; tandai app yang sedang dijaga
- Toggle "Jaga" per aplikasi
- Batas jumlah app yang bisa dijaga bersamaan (default 1, maksimal 3) — biar hemat resource
- Tombol "Buka" untuk membuka aplikasi itu secara normal

### 3.2 Supervisor (inti fitur)
- `FloatingService` yang sudah ada menjadi tempat supervisor berjalan
- Timer berkala (default 60 detik, bisa diubah 30/60/300 detik) memeriksa apakah proses aplikasi yang dijaga masih hidup
- Kalau mati → relaunch activity aplikasi tersebut
- Backoff & batas restart: maksimal N restart per jam (default 10) per aplikasi; kalau batas ini tercapai, tandai "gagal dijaga" dan beri tahu user
- Hanya aktif saat minimal satu aplikasi ditandai "jaga" (agar tidak menambah beban saat fitur tidak dipakai)

### 3.3 Deteksi proses aplikasi
- **Tanpa Shizuku:** pakai `UsageStatsManager` (izin `PACKAGE_USAGE_STATS` sudah ada) — event `ACTIVITY_RESUMED` / `ACTIVITY_STOPPED` untuk tahu aplikasi berhenti atau pindah
- **Dengan Shizuku (Tier B):** `pidof <package>` lebih akurat dan jauh lebih murah
- Catatan: `ActivityManager.getRunningAppProcesses()` sejak Android 8.1 hanya mengembalikan proses milik aplikasi sendiri, jadi tidak bisa dipakai untuk mendeteksi proses app lain

### 3.4 Integrasi ke yang sudah ada
- Notifikasi foreground: tampilkan "Menjaga: [nama app]" + tombol aksi (hentikan jaga / buka FTxT) — memakai `NotificationActionReceiver` yang sudah ada
- Halaman baru di navigasi utama, sejajar dengan halaman non-overlay yang sudah ada (`SessionListActivity`, `DocumentationActivity`, `AboutActivity`)
- Widget Smart Panel: tidak ikut (bukan `OverlayModule`, tidak punya window overlay)

### 3.5 Tier B — Shizuku (opsional, tahap berikutnya)
- Shizuku **opsional**: fitur tetap berfungsi tanpa Shizuku, hanya lebih lemah
- Saat Shizuku terpasang & diizinkan:
  - `cmd deviceidle whitelist +<package>` — keluar dari doze
  - `am set-inactive <package> false` — cegah masuk cached state
  - `pidof <package>` — deteksi proses yang akurat
- Kalau Shizuku tidak aktif → otomatis kembali ke Tier A, tanpa mengganggu user

### 3.6 Tier C — Virtual Display (eksperimental, belum jadi target)
- Menjalankan activity aplikasi target di `VirtualDisplay` supaya jendela-nya dianggap "visible" tanpa tampil di layar
- Potensinya paling besar (game tidak `onPause`), tapi implementasinya berat (butuh `Surface`/`MediaProjection`) dan perilakunya sangat berbeda antar OEM serta versi Android
- Hold — hanya kalau Tier A+B ternyata tidak cukup

---

## 4. Struktur & File

Fitur ini bukan overlay, jadi **tidak butuh module Gradle baru** dan tidak mengimplementasikan `OverlayModule`. Logic service diletakkan di `core` (sudah jadi dependensi semua fitur), UI-nya di `app`.

| File | Keterangan |
|------|------------|
| `core/.../core/apprunner/BackgroundAppManager.java` (baru) | Penyimpan daftar app yang dijaga + state, baca/tulis `ftxt_prefs` |
| `core/.../core/apprunner/AppSupervisor.java` (baru) | Timer pemeriksaan + relaunch + backoff |
| `core/.../core/apprunner/InstalledApps.java` (baru) | Query daftar aplikasi launcher, ikon, nama |
| `core/.../core/apprunner/BackgroundAppConfig.java` (baru) | Interval pemeriksaan, batas restart, batas jumlah app |
| `ui/AppRunnerActivity.java` (baru) | Halaman daftar aplikasi + search + toggle |
| `ui/AppRunnerAdapter.java` (baru) | Adapter RecyclerView |
| `ui/BackgroundAppGhostActivity.java` (baru) | Activity transparan 1×1 untuk memindahkan FTxT ke belakang tanpa mematikan activity target |
| `res/layout/activity_app_runner.xml` (baru) | Layout halaman |
| `res/layout/item_background_app.xml` (baru) | Layout item |
| `core/.../core/FloatingService.java` (ubah) | Integrasi supervisor; wake lock tetap aktif selama ada app yang dijaga |
| `core/.../core/NotificationHelper.java` (ubah) | Teks & tombol notifikasi untuk status penjagaan |
| `app/src/main/AndroidManifest.xml` (ubah) | `<queries>`, registrasi activity baru, action baru di receiver |

---

## 5. Detail Teknis Penting

### 5.1 Daftar aplikasi (Android 11+ package visibility)
Harus ada `<queries>` di manifest, kalau tidak daftarnya kosong:

```xml
<queries>
    <intent>
        <action android:name="android.intent.action.MAIN"/>
        <category android:name="android.intent.category.LAUNCHER"/>
    </intent>
</queries>
```

Query: `queryIntentActivities(Intent(ACTION_MAIN).addCategory(CATEGORY_LAUNCHER), MATCH_DEFAULT_ONLY)`.
**Jangan** pakai `QUERY_ALL_PACKAGES` — permission-nya berat dan bisa ditolak GOOGLE Play.

### 5.2 Menjalankan aplikasi
```java
Intent launch = pm.getLaunchIntentForPackage(pkg);
if (launch != null) {
    launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(launch);
}
```

### 5.3 Tidak mematikan activity target saat FTxT kembali ke depan
Kalau FTxT_activity langsung `finish()` setelah membuka app target, activity ikut ikut mati. Solusinya activity transparan 1×1: buka activity target, lalu panggil `moveToBack(true)` pada activity FTxT — FTxT turun ke belakang, activity target tetap hidup. 만약 perlu, coba `taskLock` (non-SDK, jangan Andalkan).

### 5.4 Fase & urutan pengerjaan

1. **Fase 1 — Fondasi** — `InstalledApps`, `BackgroundAppConfig`, `BackgroundAppManager`, `<queries>` di manifest
2. **Fase 2 — Supervisor** — `AppSupervisor` (timer, deteksi via UsageStats, relaunch, backoff) + integrasi `FloatingService`
3. **Fase 3 — UI** — `AppRunnerActivity` + adapter + layout, search, toggle, status
4. **Fase 4 — Notifikasi** — status "Menjaga: X" + tombol aksi
5. **Fase 5 — Error handling** — Shizuku/tidak ada, app tidak bisa dilaunch, service mati, restart loop
6. **Fase 6 — Shizuku (Tier B)** — provider, izin, `deviceidle whitelist` / `set-inactive` / `pidof` (pakai bersama rencana Shizuku yang sudah ada)
7. **Fase 7 — Testing** — device variety, OEM killer, reboot, service di-kill

### 5.5 Pengaruh ke fitur lain
- Selama ada app yang dijaga, `FloatingService` harus tetap hidup → **pemantauan baterai & riwayat sesi tidak terputus**, ini keuntungan.
- Justru sebaliknya, kalau user cuma memakai FTxT tanpa menjaga app apa pun, fitur ini tidak menambah beban sama sekali (supervisor nonaktif).

---

## 6. Risiko & Edge Cases

- **Restart_loop** — aplikasi yang crash karena alasan lain akan terus dilaunch → harus ada batas restart per jam + backoff + penanda gagal
- **State hilang saat relaunch** — login, posisi game, atau playback akan mulai ulang; harus ada peringatan di UI
- **OEM killer** — Xiaomi/Oppo/Vivo/Realme membatasi proses background; perluTampirkan panduan whitelist
- **Beban resource** — supervision = proses target hidup terus → lebih banyak RAM & baterai terpakai; ini konsekuensi yang tidak bisa dihindari, pengguna wajib diberi tahu
- **Ketidakjelasan** — "dijaga" ≠ "tetap berjalan"; teks UI harus jujur, jangan menjanjikan app tidak akan `onPause`
- **Depolusi** — bisa disalahgunakan untuk menjaga proses yang tidak sehat; tidak ada solusi, cukup pengaturan default yang hemat
- **Perilaku berbeda tiap OEM** — perlu uji di minimal 3 merek

---

## 7. Testing

- **Unit:** logika backoff/batas restart, parsing event `UsageStats`
- **Manual:** aplikasi chat (ringan), game (berat), YouTube/live stream; ganti-ganti aplikasi 30 menit; reboot saat app dijaga; service di-kill paksa; device dengan OEM killer aktif; Android 8 / 10 / 12 / 14 / 15
- **Dampak samping:** pastikan pemantauan baterai tetap jalan & sesi pengisian daya benar setelah app dilaunch ulang

---

## 8. Estimasi Waktu

- **Fase 1 (Fondasi):** ±1–2 jam
- **Fase 2 (Supervisor):** ±2–3 jam
- **Fase 3 (UI):** ±3–4 jam
- **Fase 4 (Notifikasi):** ±1 jam
- **Fase 5 (Error handling):** ±1–2 jam
- **Fase 6 (Shizuku):** ±3–5 jam (bisa dipakai bersama `BYPASS_CHARGING_SHIZUKU`)
- **Fase 7 (Testing):** ±2–3 jam
- **Total:** ±13–20 jam

---

## 9. Status

- [ ] Fase 1 — Fondasi
- [ ] Fase 2 — Supervisor
- [ ] Fase 3 — UI halaman App Runner
- [ ] Fase 4 — Notifikasi
- [ ] Fase 5 — Error handling
- [ ] Fase 6 — Tier B Shizuku
- [ ] Fase 7 — Testing
- [ ] Tier C Virtual Display (eksperimental, hold)
