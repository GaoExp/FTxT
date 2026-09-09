# Info Aplikasi & Pembaruan — Tentang (About) Halaman Penuh

> **Status:** Rencana — belum dieksekusi
> **Revisi:** 2026-09-09 — disusun dari diskusi konsep cek pembaruan FTxT

---

## 1. Tujuan

Menyediakan halaman **"Tentang Aplikasi"** (About) berisi info aplikasi yang sedang terpasang serta fitur **cek pembaruan** ke GitHub. Halaman dibuat **fullscreen (halaman penuh)**, bukan dialog — konsisten dengan pola halaman Konfigurasi / Dokumentasi yang sudah ada.

## 2. Alasan Bentuk "Halaman Penuh"

- **Bukan dialog** — dialog dipakai untuk opsi cepat yang langsung hilang (contoh: pemilih ikon launcher, pilih format).
- **Bukan item sidebar** — sidebar berisi modul overlay; tentang aplikasi bukan modul overlay.
- **Bukan section di Konfigurasi** — halaman Konfigurasi sudah panjang; info aplikasi & pembaruan punya tempat sendiri.
- Halaman bisa diakses dari **dua pintu** yang menampilkan halaman yang sama:
  1. **Popup ikon gear (Pengaturan)** — ditambahkan sebagai item baru, satu baris di bawah *Dokumentasi*.
  2. **Header Navigation Drawer** — tulisan "FunText vX Beta" di atas sidebar yang selama ini hanya judul, dibuat bisa diketuk.

## 3. Alur Akses

```
Pintu 1: Toolbar > Ikon Gear (Pengaturan)
  → Popup: [Muat Preset] [Konfigurasi] [Dokumentasi] [Tentang Aplikasi]
  → Ketuk "Tentang Aplikasi"

Pintu 2: Buka Sidebar > Ketuk Header "FunText vX Beta"
  → (sebelumnya hanya tulisan, kini klikabel)

Keduanya → halaman baru fullscreen (Activity) yang sama, dengan toolbar + tombol kembali
```

## 4. Isi Halaman

### 4.1 Header
- Ikon launcher FTxT (pakai launcher icon saat ini) + nama **FunText (FTxT)** + label **v4.92.1 Beta**.

### 4.2 Kartu Info Aplikasi (statis, dari `BuildConfig` / `PackageManager`)
- Status rilis: **Beta**
- Versi terpasang: `4.92.1` (dari `versionName`)
- Version code: `278` (dari `versionCode`)
- Tampil polos sebagai baris label — tanpa tombol edit. (Daftar fitur utama **tidak** dicantumkan di kartu ini.)

### 4.3 Section Pembaruan

- **Toggle "Periksa otomatis"** — switch yang mengaktifkan pemeriksaan pembaruan otomatis saat aplikasi dibuka/dimulai (**default: OFF**).
- **Frekuensi pemeriksaan otomatis** — baris pilihan (ada di bawah toggle, aktif saat toggle ON), dipilih lewat popup dialog: **setiap buka aplikasi, 12 jam, 1 hari, 3 hari, 7 hari** (default **1 hari**). Tersimpan di prefs.
- **Tombol "Periksa Pembaruan Sekarang"** — langsung cek ke GitHub Releases API di thread latar (tidak memblok UI), tombol menampilkan progres singkat ("Memeriksa…").
- **Baris status hasil cek** (di bawah tombol periksa):
  - Ada pembaruan → teks menonjol **"Pembaruan tersedia: FTxT v4.93.0 Beta"** — **bisa diketuk** → memunculkan kembali dialog pembaruan (Lihat Informasi / Unduh / Nanti Saja) kapan pun, tidak harus menunggu dialog otomatis.
  - Tidak ada pembaruan / sudah terbaru → teks **"Sudah menggunakan versi terbaru"**.
  - Belum pernah dicek → kondisi awal, hanya tampil tombol periksa.

### 4.3b Cek Otomatis Saat Buka Aplikasi
- Saat `update_check_auto = ON` dan frekuensi sudah lewat sejak `update_last_checked_at` → cek diam-diam di latar saat aplikasi dibuka.
- Jika ketahuan ada versi baru → **muncul dialog langsung** berisi pilihan:
  - **"Lihat Informasi"** — tombol utama dengan sub-teks kecil di bawahnya *"Buka halaman rilis"* → buka browser ke release GitHub.
  - **"Unduh"** — masuk alur unduh & instal (bagian 5.6).
  - **"Nanti Saja"** — menutup dialog (nasibnya dibahas di bagian 5.7).
- Dialog hanya muncul saat hasilnya benar-benar versi baru (bukan saat "Terkini" / gagal).
- Hasil disimpan ke prefs; status tampil saat halaman About dibuka.

### 4.4 Section Tentang Pengembang + Tautan & Akses
Pendamping section pembaruan, berisi dua blok dalam satu section:
- **Tentang pengembang:** Author **GaoZhan** + satu kalimat klarifikasi santai (pengembangan dibantu AI, developer menangani testing/manual/debug sambil ngopi — ala README).
- **Tautan & akses:**
  - **"Baca README"** (baris terpisah) → buka halaman Dokumentasi pada entri README.
  - **"Baca PANDUAN"** (baris terpisah) → buka halaman Dokumentasi pada entri PANDUAN.
  - **"Kunjungi GitHub"** → buka repo `GaoExp/FTxT`.
  - **"Lihat Riwayat Perubahan (CHANGELOG)"** → buka halaman Dokumentasi pada entri CHANGELOG.
  - Catatan **lisensi**: "Belum ada lisensi resmi — boleh dipakai, dimodifikasi, di-fork".

### 4.5 Section Support
- Memuat isi section **📧 Support** dari README: "Laporan bug, issue, atau permintaan fitur — silakan buat issue atau hubungi pengembang."
- Tombol/baris **"Buat Issue di GitHub"** → buka halaman *Issues* repo `GaoExp/FTxT`.
- Catatan kecil ala README: "Respons tidak dijamin cepat, karena project ini berkembang mengikuti eksperimen, suasana hati, dan secangkir kopi."

### 4.6 Section Footer
- Kalimat kecil: "Dibuat dengan eksperimen, suasana hati, dan secangkir kopi ☕".

### 4.7 Desain Visual
- **Gaya:** konsisten dengan tema FTxT — biru aksen `#2196F3` (seperti toolbar) dan kartu abu gelap `#37474F` (seperti preview ikon launcher); menyesuaikan mode gelap/terang.
- **Susunan halaman:** header besar di atas (ikon launcher + nama + status beta), lalu kartu-kartu section (Info Aplikasi, Pembaruan, Pengembang & Tautan), footer di bawah.
- **Elemen dekoratif:** aksen gradien pada nama "FunText" / titik-titik warna (mewakili modul overlay) supaya tidak terkesan polos.
- **Dialog pembaruan:** judul "Pembaruan tersedia", isi "FTxT v4.93.0 Beta tersedia untuk diunduh.", tiga pilihan: "Lihat Informasi" (dengan sub-teks "Buka halaman rilis"), "Unduh", "Nanti Saja".

## 5. Mekanisme Cek Pembaruan (GitHub Releases API)

### 5.1 Endpoint
```
GET https://api.github.com/repos/GaoExp/FTxT/releases/latest
```
Repo: `GaoExp/FTxT` (dari `git remote`).

### 5.2 Data yang Dipakai dari Response JSON
- `tag_name` — mis. `"v4.92.1"` → sumber versi remote.
- `html_url` — link halaman release untuk tombol "Buka Halaman Rilis".
- `published_at` — (opsional) bisa ditampilkan sebagai info tambahan.

### 5.3 Pengecekan & Pembandingan Versi
1. App membangun URL endpoint, fetch di thread latar (`HttpURLConnection` bawaan; tidak perlu tambah dependency).
2. Parse `tag_name` → ambil bagian angka (mis. `v4.92.1` → `4.92.1`).
3. Pecah jadi `major.minor.patch`, bandingkan dengan `BuildConfig.VERSION_NAME` versi terpasang.
4. Hasil disimpan ke `ftxt_prefs`:
   - `update_last_checked_at` (waktu cek terakhir)
   - `update_latest_version` (versi remote yang terdeteksi, kosong jika tidak ada/terkini)
   - `update_latest_url` (html_url, dipakai saat tombol "Buka Halaman Rilis")
   - `update_check_auto` (boolean toggle pemeriksaan otomatis)
   - `update_check_interval` (frekuensi otomatis dalam ms / nilai pilihan popup)

### 5.4 Pemeriksaan Otomatis Saat Buka Aplikasi
- Dipicu saat aplikasi dibuka, hanya jika `update_check_auto = ON` **dan** selisih `now - update_last_checked_at` ≥ `update_check_interval`.
- Berjalan di thread latar, hasil ditulis ke prefs.
- Jika hasilnya **vari remote lebih baru** → tampilkan dialog "Pembaruan tersedia" langsung ([Lihat Rilis] / [Nanti Saja]).
- Jika "Terkini" / gagal → tidak ada dialog; status tersimpan untuk dilihat di halaman About.

### 5.5 Skenario Hasil
- **Versi remote lebih baru** → status "Pembaruan tersedia" + tombol buka rilis.
- **Versi sama** → status "Terkini".
- **Versi remote lebih lama / tidak terbaca** → abaikan, status "Terkini" (data korup tidak menyesatkan user).
- **Gagal (offline / rate limit / timeout)** → status tidak berubah, tampil pesan singkat "Gagal memeriksa (periksa koneksi)". Tidak perlu crash.

### 5.6 Alur Unduh & Instal (dari dialog pembaruan)
- Dialog awal pembaruan menyajikan tiga pilihan: **"Lihat Informasi"** (sub-teks "Buka halaman rilis"), **"Unduh"**, dan **"Nanti Saja"**.
- Saat **"Unduh"** ditekan → dialog menampilkan **progress unduhan** (bar persen).
- Setelah unduhan selesai → pilihan berubah menjadi **"Install"** → memicu prompt instal Android bawaan (perlu izin "install unknown apps" bila belum ada).
- Sumber APK diambil dari asset rilis GitHub (endpoint `releases`). **Keputusan: satu APK universal** — release hanya berisi satu asset APK general (`app-universal` / tanpa ABI). Tujuannya supaya tidak perlu bikin varian per arsitektur.
  - **Pemilihan asset:** ambil satu-satunya asset `.apk` dari release. (Sistem deteksi ABI tidak dipakai — tidak ada varian.)
- File APK disimpan sementara (cache/Download FTxT), Checksum opsional.

### 5.7 "Nanti Saja" — kapan muncul lagi?
- **Keputusan:** "Nanti Saja" = dialog versi itu tidak muncul lagi otomatis (tidak menggoda).
- **Tetap sadar tanpa dialog:** pada halaman About, baris status selalu menampilkan **"Pembaruan tersedia: FTxT v4.93.0 Beta"** (klikabel → membuka dialog lagi). Penanda pasif yang senantiasa terlihat saat user membuka About.
- Baris status berubah menjadi **"Sudah menggunakan versi terbaru"** otomatis begitu versi terpasang == versi remote (user sudah update).
- Dialog otomatis muncul lagi hanya saat ketahuan **versi yang lebih baru lagi** (bukan versi yang sama di-tunda ulang).

## 6. File yang Perlu Dibuat/Diubah

| File | Keterangan |
|------|------------|
| `AboutActivity.java` (baru, `exp.ftxt`) | Activity halaman penuh: toolbar kembali + konten info aplikasi + section pembaruan (toggle otomatis, frekuensi popup, tombol periksa/buka rilis) + pengembang & tautan + footer |
| `activity_about.xml` (baru, `res/layout`) | Layout halaman (pola serupa SettingsActivity / DocumentationActivity) |
| `UpdateChecker.java` (baru `/ utils/`) | Util: fetch GitHub API + parse JSON + bandingkan versi + simpan prefs + logika cek otomatis saat buka app |
| `MainActivity.java` (ubah) | Tambah item menu `"Tentang Aplikasi"` di `showSettingsPopup()` + intent ke `AboutActivity`; buat header sidebar (`navHeaderTitle`) klikabel + intent ke `AboutActivity`; picu cek otomatis saat buka app (jika aktif & interval terlewat) |
| `nav_header.xml` (ubah) | Tambah `clickable`/`focusable` + hint visual (mis. chevron ›/info) agar terlihat bisa diketuk |
| `DocumentationActivity.java` (ubah) | Dukung intent extra nama dokumen (mis. `EXTRA_DOC = "README"/"PANDUAN"/"CHANGELOG"`) agar bisa langsung membuka dokumen tertentu dari halaman About |
| `AndroidManifest.xml` (ubah) | Daftarkan `AboutActivity` (tema mengikuti pola activity lain) |

## 7. Keputusan & Asumsi (untuk konfirmasi)

1. **Lokasi akses:** dua pintu menuju halaman yang sama — item baru di popup ikon gear (di bawah *Dokumentasi*) plus header sidebar "FunText vX Beta" yang dibuat klikabel.
2. **Bentuk:** halaman penuh (Activity baru) — sudah disepakati.
3. **Cek pembaruan:** dua mode — **manual** lewat tombol "Periksa Pembaruan Sekarang", dan **otomatis** saat aplikasi dibuka dengan toggle (**default OFF**) + frekuensi popup (**setiap buka app, 12 jam, 1 hari, 3 hari, 7 hari**; default **1 hari**).
4. **Sumber data:** GitHub Releases API `releases/latest` — tanpa file JSON kustom, tanpa FCM.
5. **Cara update:** download & instal APK dari asset rilis — **satu APK universal**, ambil satu-satunya asset `.apk`, lalu prompt instal Android bawaan.
6. **Tidak menambah dependency** — pakai `org.json` + `HttpURLConnection` bawaan.
7. **Nama item menu** memakai "Tentang Aplikasi" (masih bisa diganti).
8. Info aplikasi (versi, versionCode) dibaca dari `BuildConfig`, bukan hardcode.

## 8. Estimasi Waktu Pengerjaan

- **AboutActivity + layout (termasuk toggle & popup frekuensi):** ±2–3,5 jam
- **UpdateChecker (fetch + parse + banding + logika otomatis):** ±2,5–3,5 jam
- **Popup gear + header drawer klikabel + cek otomatis saat buka app + manifest + integrasi:** ±1–2 jam
- **Uji skenario (online/offline, versi baru/sama, toggle otomatis & interval):** ±1,5–2 jam
- **Total:** ±7–11 jam (±1,5–2 hari)

## 9. Status

- [ ] Activity tentang aplikasi (header, kartu info versi/versionCode)
- [ ] Util UpdateChecker (GitHub API + simpan prefs)
- [ ] Section pembaruan: toggle otomatis + popup frekuensi + tombol periksa/buka rilis + status
- [ ] Cek otomatis saat aplikasi dibuka (hormati interval)
- [ ] Item popup "Tentang Aplikasi"
- [ ] Header sidebar "FunText vX Beta" klikabel
- [ ] Section pengembang + tautan & akses + footer
- [ ] Integrasi + manifest
- [ ] Uji skenario cek pembaruan
- [ ] Dokumen: README/PANDUAN/STRUKTUR + CHANGELOG (minor bump: **4.93.0**)