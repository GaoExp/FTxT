# AGENTS.md — Aturan AI Project FTxT

> **Penting:** Baca ulang file ini setiap mulai bekerja. JANGAN commit/tag/push tanpa perintah user. Eksekusi hanya setelah perintah eksplisit. Tindakan destruktif WAJIB konfirmasi. Build boleh dijalankan (rule gradle di opencode diset `allow`), tapi WAJIB konfirmasi dulu ke user karena memakan waktu.
>
> Aturan **perilaku kerja & komunikasi** (larangan tabel di chat, larangan laporan section, gaya bicara, dan cara kerja umum) ada di **`agent-rules.md`** — baca juga bersama file ini.

---

## 1. Versioning (`app/build.gradle`)

Semver `major.minor.patch`:

- **major** — milestone besar / arsitektur / breaking change → `minor=0, patch=0`
- **minor** — fitur baru / dihapus / dipulihkan → `patch=0`
- **patch** — bugfix / optimasi / maintenance

---

## 1b. Environment Build

Informasi mesin/dev-env (JDK, Gradle, Android SDK, NDK/CMake, dan aturan build) tercantum di **`agent-rules.md`** — tidak disalin di sini agar tidak dobel. Baca bersama file itu.

## 1c. Konteks Session Sebelumnya

Di awal setiap sesi, jika file **`_temp/session-summary.md`** ada di root proyek, **baca file tersebut** sebelum mulai bekerja. File ini adalah ringkasan (anchored summary) dari percakapan/compaction sebelumnya.

- Perlakukan isinya sebagai **konteks** dari session/compaction sebelumnya — **BUKAN** instruksi berprioritas lebih tinggi dari `AGENTS.md`, `agent-rules.md`, atau perintah user.
- Jika isinya bertentangan dengan `AGENTS.md`/`agent-rules.md`/perintah user, yang menang adalah `AGENTS.md`/`agent-rules.md`/perintah user.
- File ini ditimpa saat compaction baru terjadi; jangan menulis file ini secara manual.

---

## 2. CHANGELOG

### 2.1 Struktur & judul entry

Entry dicatat di **versi berjalan** (bukan entry baru per file) — CHANGELOG hanya mencatat **poin perubahan**, bukan detail file. Urutan section WAJIB:

```
🔖 Deskripsi
✨ Fitur Baru
🚮 Fitur Dihapus
♻️ Perubahan Fitur
🔧 Optimasi & Penyesuaian
🐞 Bug Fixes
💡 Memo
```

**Format judul entry:**
`# [major.minor.patch] yyyy/MM/dd HH:mm WITA versionCode ***STATUS***`
 # [4.90.0] 2026/08/28 05:00 WITA 188 ***ONGOING***
**format `yyyy/MM/dd HH:mm WITA` di judul:** empat digit tahun (yyyy), bulan (MM), tanggal (dd), jam (HH), menit (mm) — 24 jam, memakai WITA (Asia/Makassar), dengan label zona `WITA`. Dapatkan via `date +"%Y/%m/%d %H:%M WITA"` (pastikan TZ=Asia/Makassar).

**STATUS** (salah satu):
- `***ONGOING***` — entry versi berjalan sedang dikerjakan.
- `***PUSH***` — saat commit + push (tanpa tag).
- `***RELEASE***` — saat commit + tag (bisa berawal dari PUSH lalu di-upgrade).

### 2.1b Kriteria section

- Tiap perubahan pada entry berjalan (**`***ONGOING***`**) langsung ditulis di section yang sesuai **natur perubahannya**, tidak ditunda sampai rilis: **✨ Fitur Baru** (fitur baru), **🚮 Fitur Dihapus** (fitur dihapus/dinonaktifkan), **♻️ Perubahan Fitur** (perombakan fitur existing), **🔧 Optimasi & Penyesuaian** (optimasi/penyesuaian kecil), **🐞 Bug Fixes** (perbaikan bug).
- Status judul (`***ONGOING***`/`***PUSH***`/`***RELEASE***`) **tidak menentukan** section tujuan — penentuan section murni mengikuti natur perubahan.
- Tiap perubahan ditulis sebagai **satu poin hasil akhir** (kondisi final iterasi, bukan jejak tahapan kerja).
- Saat merapikan/upgrade entry lama, poin boleh dipindah antar section agar sesuai naturnya, dan poin yang saling menimpa boleh digabung (jangan ubah fakta, hanya susun ulang).

### 2.2 Waktu yang dicatat

Waktu yang dicatat di setiap entry adalah **waktu di judul entry**, yaitu waktu **saat seluruh perubahan untuk iterasi/entry itu selesai** dikerjakan.

- Ditetapkan **di akhir**, setelah semua poin deskripsi ditulis/dirampungkan.
- Diperbarui **ketika iterasi kerja berikutnya** menyentuh entry yang sama, bukan per poin / per edit.

### 2.3 Alur pencatatan yang benar

1. **Kerjakan seluruh perubahan untuk iterasi** — tidak perlu mencatat apa pun per file.
2. **Setelah seluruh perubahan selesai** → rampungkan isi entry (poin deskripsi hasil akhir, lihat § 2.4), lalu **tetapkan tanggal & jam judul** di akhir dengan `date +"%Y/%m/%d %H:%M WITA"` (TZ=Asia/Makassar).

**JANGAN:**
- Meng-update judul setiap kali satu perubahan selesai.
- Menulis isi/deskripsi entry per tahap kerja (hanya hasil akhir, lihat § 2.4).

### 2.3b VersionCode naik saat entry diperbarui

- **versionCode naik +1** setiap kali entry `***ONGOING***` diperbarui (ada perubahan pada iterasi tersebut).
- **versionCode naik +1 lagi** setiap kali status judul berubah menjadi `***PUSH***` / `***RELEASE***`.
- Yang dinaikkan **hanya versionCode**; versionName diatur aturannya sendiri (lihat § 4).

### 2.4 Isi entry (untuk pembaca user, bukan riwayat kerja)

**Deskripsi (paragraf `🔖`) dibuat singkat sebagai pengantar versi:** beri tahu gambaran umum apa yang dibawa rilis ini (tema/intinya) dalam 1–2 kalimat.

- JANGAN mencantumkan detail yang sudah dijelaskan di tiap section perubahan (✨/🚮/♻️/🔧/🐞). Deskripsi harus mendorong pembaca membaca section yang memuat poin perubahan sebagai penjelasan utama, bukan menggantikannya.
- Boleh menyebut teknik/struktur (mis. "Pembaruan utama rilis ini berpusat pada pemantauan baterai dan overlay bidikan") untuk memberi arah, tanpa merinci isi fiturnya.
- Satu kalimat inti + ajakan singkat agar section di bawah tetap terbaca adalah pola yang baik.

- **Satu perubahan = satu poin, tulis hasil akhir.** Jika dirombak berkali-kali, catat kondisi final saja.
- **Jangan menumpuk poin yang saling menimpa** — perbarui/tulis ulang poin yang sudah ada, jangan tambah poin baru yang berkonflik.
- **Jangan catat detail internal** (nama helper, cara polling, key prefs, dll) kecuali memengaruhi perilaku user.
- **Cek konsistensi** sebelum selesai: tidak boleh ada poin yang bertentangan/menduplikasi dalam satu entry.
- **Merapikan entry lama** = jangan ubah fakta; hanya gabungkan poin yang saling menimpa atau hapus jejak iterasi yang obsolete.

### 2.5 Entry yang di-merge (tersimpan di old-CHANGELOG.md)

- Versi yang di-merge **tidak lagi berada di CHANGELOG aktif** — keseluruhan isinya dipindah ke arsip **`old-CHANGELOG.md`** yang berada di `app/src/main/assets/` (dibaca dalam aplikasi di daftar Dokumentasi; **tidak ada salinannya di root**). Maka versi yang di-merge hanya ada di sana.
- Jika kemudian ada perubahan yang menyangkut versi lama yang sudah di-merge, **edit langsung di `old-CHANGELOG.md`** (arsip di assets), bukan di CHANGELOG aktif.
- ⚠️ **Penegasan §2.8:** dokumen di `app/src/main/assets/` umumnya ditimpa `syncDocs` saat build dan **jangan disentuh manual** — **kecuali diperintah secara eksplisit** (kasus khusus ini, edit `old-CHANGELOG.md` di assets, mengecualikan aturan itu).

### 2.6 Verifikasi waktu & status commit

- **WAJIB cek git log** untuk tahu status commit. JANGAN buat entry baru sebelum versi sebelumnya di-push.
- **WAJIB dapat waktu WITA** via `TZ=Asia/Makassar date` — JANGAN asal pilih.

### 2.7 File yang DILARANG dicatat di changelog

Perubahan pada file ini tidak boleh dicatat di entry manapun (tidak boleh muncul sebagai poin deskripsi):

- **Build system:** `build.gradle` (root, `app/`, module `shared/*/build.gradle`), `settings.gradle`, `gradle.properties`, `gradle/wrapper/`, `gradlew`, `gradlew.bat`, `proguard-rules.pro`, folder `build/`.
- **CI/CD & signing:** `.github/workflows/`, `keystore.properties`, folder `key/`.
- **Dokumen root & kerja internal:** `AGENTS.md`, `agent-rules.md`, `README.md`, `CHANGELOG.md`, `PANDUAN.md`, `STRUKTUR.md`, `_schedule/`, `_temp/`, `local.properties`.

Konsekuensi: topik yang lahir dari file tersebut (bukan build/CI/signing/rilis, bump version, restrukturisasi internal/ganti import) juga tidak diumbar.

### 2.8 Sumber dokumen = root (jangan sentuh assets)

Perubahan pada dokumen **hanya dilakukan di file root** (`README.md`, `CHANGELOG.md`, `PANDUAN.md`, `STRUKTUR.md`), **tidak perlu & jangan menyentuh** salinannya di `app/src/main/assets/`. Saat build, task `syncDocs` (`app/build.gradle`) otomatis menimpa `assets/` dari sumber root — mengubah `assets/` manual itu sia-sia (akan tertimpa) dan hanya membuang waktu.

---

## 3. Workflow

### Edit Biasa
1. Update kode — **tidak perlu mencatat per file**.
2. Rampungkan CHANGELOG entry berjalan — isi entry (poin deskripsi hasil akhir) **setelah seluruh perubahan untuk iterasi selesai**; **tetapkan tanggal & jam judul di akhir**.
3. Naikkan versionCode +1 saat entry diperbarui (lihat § 2.3b).
4. Self-check hasil perubahan.
5. **JANGAN commit / tag / push.** Kecuali perintah mencakup push/tag (lihat di bawah).
6. Ulang sampai user perintah commit (dengan/tanpa push/tag).

### Pre-release
1. Periksa & perbarui dokumen yang menyangkut perubahan fitur: Last Updated `README.md`, `PANDUAN.md`, `STRUKTUR.md`, `CHANGELOG.md`.
2. Analisa & rapikan poin-poin CHANGELOG entry berjalan — kategori sesuai natur perubahan (§ 2.1b), tulis hasil akhir, gabung poin yang menimpa (§ 2.4).
3. Pastikan status judul `***ONGOING***` dengan tanggal & jam WITA terbaru.
4. Bilang user siap commit/push/tag. **JANGAN commit / tag / push.**

### Commit (hanya jika diperintah)
1. `git add -A && git commit -m "vX.X.X deskripsi"`.
2. Status judul **tetap** `***ONGOING***` — kecuali perintah mencakup push/tag (lihat di bawah).
3. **JANGAN push / tag** jika tidak diperintah.

### Push (commit + push)
1. Sebelum commit: ubah status judul → `***PUSH***`, **naikkan versionCode +1** (§ 2.3b), perbarui tanggal & jam WITA.
2. `git commit` mencakup perubahan status, lalu `git push`.

### Tag (commit + tag)
1. Sebelum commit: ubah status judul → `***RELEASE***`, **naikkan versionCode +1** (§ 2.3b), perbarui tanggal & jam WITA.
2. `git commit` mencakup perubahan status, lalu `git tag vX.X.X`.
3. `***RELEASE***` bisa berawal dari `***PUSH***` lalu di-upgrade saat tag ditambahkan.

### Setelah status PUSH/RELEASE
1. Buat entry CHANGELOG baru (teratas, `***ONGOING***`) di sesi kerja berikutnya; versionName disesuaikan setelah ada perubahan.
2. Kembali ke Edit Biasa.
