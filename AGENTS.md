# AGENTS.md — Aturan AI Project FTxT

> **Penting:** Baca ulang file ini setiap mulai bekerja. JANGAN commit/tag/push tanpa perintah user. Eksekusi hanya setelah perintah eksplisit. Tindakan destruktif WAJIB konfirmasi. JANGAN build — rule gradle di opencode diset `deny`, build tidak akan pernah bisa dijalankan. Gunakan bahasa Indonesia untuk thinking & respons.

---

## 1. Versioning (`app/build.gradle`)

Semver `major.minor.patch`:

- **major** — milestone besar / arsitektur / breaking change → `minor=0, patch=0`
- **minor** — fitur baru / dihapus / dipulihkan → `patch=0`
- **patch** — bugfix / optimasi / maintenance

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
- `***PUSH***` — saat commit tanpa tag.
- `***RELEASE***` — saat commit + tag (bisa berawal dari PUSH lalu di-upgrade).

### 2.1b Kriteria section

- **✨ Fitur Baru / 🚮 Fitur Dihapus / ♻️ Perubahan Fitur / 🔧 Optimasi / 🐞 Bug Fixes** hanya untuk **fitur yang sudah rilis** (versi berstatus `***PUSH***`/`***RELEASE***`).
- Untuk versi `***ONGOING***` yang masih dikerjakan, tiap perubahan ditulis sebagai **satu poin hasil akhir di ✨ Fitur Baru** — tidak dikotak-kotakkan ke dalam ♻️/🔧/🐞 dulu. Section ♻️/🔧/🐞 baru boleh dipakai pada entry berstatus PUSH/RELEASE (mis. saat merapikan/upgrade entry lama atau memindahkan poin saat rilis).
- Poin yang menyangkut fitur masih ONGOING tidak boleh tampil di ♻️/🔧/🐞.

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
- **Dokumen root & kerja internal:** `AGENTS.md`, `README.md`, `CHANGELOG.md`, `PANDUAN.md`, `STRUKTUR.md`, `_schedule/`, `_temp/`, `local.properties`.

Konsekuensi: topik yang lahir dari file tersebut (bukan build/CI/signing/rilis, bump version, restrukturisasi internal/ganti import) juga tidak diumbar.

### 2.8 Sumber dokumen = root (jangan sentuh assets)

Perubahan pada dokumen **hanya dilakukan di file root** (`README.md`, `CHANGELOG.md`, `PANDUAN.md`, `STRUKTUR.md`), **tidak perlu & jangan menyentuh** salinannya di `app/src/main/assets/`. Saat build, task `syncDocs` (`app/build.gradle`) otomatis menimpa `assets/` dari sumber root — mengubah `assets/` manual itu sia-sia (akan tertimpa) dan hanya membuang waktu.

---

## 3. Aturan Kerja Universal

1. **Kerjakan hanya setelah perintah eksplisit.**
2. **Diskusi belum selesai = JANGAN mengerjakan.**
3. **Jangan buat daftar "Next Steps" lalu mengeksekusinya sendiri** — rencana menunggu persetujuan user.
4. **Tindakan destruktif/berisiko** (hapus, revert, pindah/potong isi, ubah banyak file sekaligus) **WAJIB konfirmasi eksplisit dulu**.
5. **Tanya hanya jika tidak bisa ditebak & berdampak** — scope tidak jelas, keputusan destruktif, atau instruksi bertentangan dengan kode.
6. **Hal kecil yang bisa ditafsirkan** (nama, warna, tata letak) → ambil yang paling konsisten dengan pola existing, kerjakan, sebutkan asumsinya.
7. **Selesai sesuai perintah, berhenti.**
8. **Self-check WAJIB** sebelum selesai: `git diff`/file — sesuai permintaan, tidak menyentuh file di luar scope, tidak ada yang terlewat.

---

## 4. Workflow

### Edit Biasa
1. Update kode — **tidak perlu mencatat per file**.
2. Rampungkan CHANGELOG entry berjalan — isi entry (poin deskripsi hasil akhir) **setelah seluruh perubahan untuk iterasi selesai**; **tetapkan tanggal & jam judul di akhir**.
3. Naikkan versionCode +1 saat entry diperbarui (lihat § 2.3b).
4. Self-check hasil perubahan.
5. **JANGAN commit / tag / push.**
6. Ulang sampai user perintah commit/tag.

### Pre-release
1. Periksa semua dokumen.
2. Pastikan status judul `***ONGOING***` dengan tanggal & jam WITA terbaru.
3. Bilang user siap di-commit & tag.

### Rilis (hanya jika diperintah)
1. `git add -A && git commit -m "vX.X.X deskripsi"`.
2. **JANGAN ubah status CHANGELOG** — tetap `***ONGOING***` sampai user bilang push.
3. `git tag vX.X.X` bila akan di-tag.
4. **JANGAN push** — user menyusul.

### Setelah Push (langsung buat judul entry versi baru)
1. Ubah status judul → `***PUSH***` / `***RELEASE***` dan **naikkan versionCode +1** (lihat § 2.3b).
2. versionName disesuaikan setelah ada perubahan.
3. Perbarui tanggal & jam WITA saat itu.
4. Buat entry CHANGELOG baru (teratas, `***ONGOING***`) di sesi kerja berikutnya.
5. Kembali ke Edit Biasa.

---

## 5. Perilaku AI

**Lakukan:**

- Baca AGENTS dulu.
- Cek git status/log.
- Perubahan minimal & fokus.
- Jawab singkat & actionable.
- Bahasa Indonesia.
- Self-check sebelum selesai.

**JANGAN:**

- Refactor tanpa diminta.
- Ubah file di luar scope.
- Audit project tanpa diminta.
- Buat checklist panjang.
- Build / revert tanpa konfirmasi — khusus build: rule gradle di opencode sudah diset `deny`, sehingga perintah build apa pun pasti ditolak. Verifikasi kode cukup via inspeksi (grep/diff), bukan build.

---

## 6. Catatan Komunikasi

- JANGAN gunakan tabel markdown di chat.
- DILARANG tulis laporan section (Accomplished, Next Steps, Ringkasan, dll) di chat — langsung ke inti.
- Bicaralah manusiawi & praktis (saya programmer, tapi bukan AI yang hafal seluruh ekosistem Android).
