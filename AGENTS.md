# AGENTS.md — Aturan AI Project FTxT

> **Penting:** Baca ulang file ini setiap mulai bekerja. JANGAN commit/tag/push tanpa perintah user. Eksekusi hanya setelah perintah eksplisit. Tindakan destruktif WAJIB konfirmasi. JANGAN build. Gunakan bahasa Indonesia untuk thinking & respons.

---

## 1. Versioning (`app/build.gradle`)

Semver `major.minor.patch`:

- **major** — milestone besar / arsitektur / breaking change → `minor=0, patch=0`
- **minor** — fitur baru / dihapus / dipulihkan → `patch=0`
- **patch** — bugfix / optimasi / maintenance

---

## 2. CHANGELOG

### 2.1 Struktur & judul entry

Entry dicatat di **versi berjalan** (bukan entry baru per file). Urutan section WAJIB:

```
✨ Fitur Baru
🚮 Fitur Dihapus
♻️ Perubahan Fitur
🔧 Optimasi & Penyesuaian
🐞 Bug Fixes
🗒️ File Added
✏️ File Changed
🔥 File Removed
💡 Memo
```

**Format judul entry:**
`# [major.minor.patch] yyMMddHHmm versionCode ***STATUS***`
 # [4.90.0] 2608280500 188 ***ONGOING***
**Status `yyMMddHHmm`:** dua digit tahun (yy), bulan (MM), tanggal (dd), jam (HH), menit (mm) — semua memakai WITA (Asia/Makassar), tanpa label zona. Berlaku juga untuk timestamp di section File (baris `🗒️`/`✏️`/`🔥`). Dapatkan via `TZ=Asia/Makassar date +"%y%m%d%H%M"`.

**STATUS** (salah satu):
- `***ONGOING***` — entry versi berjalan sedang dikerjakan.
- `***PUSH***` — saat commit tanpa tag.
- `***RELEASE***` — saat commit + tag (bisa berawal dari PUSH lalu di-upgrade).

Hubungan status ↔ pencatatan waktu — lihat § 2.3.

### 2.1b Kriteria section

- **✨ Fitur Baru / 🚮 Fitur Dihapus / ♻️ Perubahan Fitur / 🔧 Optimasi / 🐞 Bug Fixes** hanya untuk **fitur yang sudah rilis** (versi berstatus `***PUSH***`/`***RELEASE***`).
- Untuk versi `***ONGOING***` yang masih dikerjakan, tiap perubahan ditulis sebagai **satu poin hasil akhir di ✨ Fitur Baru** — tidak dikotak-kotakkan ke dalam ♻️/🔧/🐞 dulu. Section ♻️/🔧/🐞 baru boleh dipakai pada entry berstatus PUSH/RELEASE (mis. saat merapikan/upgrade entry lama atau memindahkan poin saat rilis).
- Poin yang menyangkut fitur masih ONGOING tidak boleh tampil di ♻️/🔧/🐞.

### 2.2 Dua jenis waktu yang dicatat — JANGAN TERTUKAR

Ada **dua jam berbeda** di setiap entry, dan keduanya punya asal yang berbeda:

1. **Waktu di section File** (`🗒️` / `✏️` / `🔥`)
   - = waktu **saat suatu file selesai diedit**.
   - Diambil dengan menjalankan `TZ=Asia/Makassar date +"%y%m%d%H%M"` **tepat saat file itu selesai**.
   - Jam ini TETAP tidak berubah meski file tersebut nanti diedit lagi di iterasi yang sama — tiap iterasi edit adalah baris terpisah (kecuali dimerger, lihat § 2.5).

2. **Waktu di judul entry**
   - = waktu **saat seluruh perubahan file untuk iterasi itu selesai** dikerjakan.
   - Ditetapkan **di akhir**, setelah semua baris section File & semua poin deskripsi ditulis/dirampungkan.
   - Diperbarui **hanya ketika iterasi kerja berikutnya** menyentuh entry yang sama, bukan per file / per edit.

> Ringkas: **waktu section File menyusul jalannya edit per file; waktu judul ditetapkan satu kali di akhir.**

### 2.3 Alur pencatatan yang benar

1. **Saat satu file selesai diedit** → langsung jalankan perintah waktu, lalu **tulis baris file-nya ke CHANGELOG** memakai jam itu.
2. **Jika saat itu belum bisa menulis** barisnya (mis. belum ada entry, atau sedang buru-buru) → **simpan dulu jamnya**, lalu tulis barisnya kemudian. Yang penting: **jam di baris = saat file selesai diedit**, bukan saat penulisannya baru sempat dilakukan.
3. **Setelah seluruh perubahan file untuk iterasi itu selesai** → rampungkan isi entry (poin deskripsi + semua baris file), lalu **tetapkan tanggal & jam judul** di akhir dengan `TZ=Asia/Makassar date +"%y%m%d%H%M"`.

**JANGAN:**
- Meng-update judul setiap kali satu file selesai.
- Menulis isi/deskripsi entry per tahap kerja (hanya hasil akhir, lihat § 2.4).
- Menggunakan waktu "saat menulis CHANGELOG" sebagai waktu section File.

### 2.4 Isi entry (untuk pembaca user, bukan riwayat kerja)

- **Satu perubahan = satu poin, tulis hasil akhir.** Jika dirombak berkali-kali, catat kondisi final saja.
- **Jangan menumpuk poin yang saling menimpa** — perbarui/tulis ulang poin yang sudah ada, jangan tambah poin baru yang berkonflik.
- **Jangan catat detail internal** (nama helper, cara polling, key prefs, dll) kecuali memengaruhi perilaku user.
- **Cek konsistensi** sebelum selesai: tidak boleh ada poin yang bertentangan/menduplikasi dalam satu entry.
- **Merapikan entry lama** = jangan ubah fakta; hanya gabungkan poin yang saling menimpa atau hapus jejak iterasi yang obsolete.

### 2.5 Entry yang di-merge

Beberapa versi digabung dalam satu entry → section `🗒️ File Added`, `✏️ File Changed`, `🔥 File Removed` **diabaikan** (tidak ditulis).

### 2.6 Verifikasi waktu & status commit

- **WAJIB cek git log** untuk tahu status commit. JANGAN buat entry baru sebelum versi sebelumnya di-push.
- **WAJIB dapat waktu WITA** via `TZ=Asia/Makassar date` — JANGAN asal pilih.

### 2.7 File yang DILARANG dicatat di changelog

Perubahan pada file ini tidak boleh muncul di entry manapun (baik baris file maupun poin deskripsi):

- **Build system:** `build.gradle` (root, `app/`, module `shared/*/build.gradle`), `settings.gradle`, `gradle.properties`, `gradle/wrapper/`, `gradlew`, `gradlew.bat`, `proguard-rules.pro`, folder `build/`.
- **CI/CD & signing:** `.github/workflows/`, `keystore.properties`, folder `key/`.
- **Dokumen root & kerja internal:** `AGENTS.md`, `README.md`, `CHANGELOG.md`, `PANDUAN.md`, `STRUKTUR.md`, `_schedule/`, `_temp/`, `local.properties`.

Konsekuensi: topik yang lahir dari file tersebut (bukn build/CI/signing/rilis, bump version, restrukturisasi internal/ganti import) juga tidak diumbar.

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
9. **Catat waktu WITA aktual saat mengedit file** — lihat § 2.2–2.3 (waktu section File = waktu edit, bukan waktu menulis CHANGELOG).

---

## 4. Workflow

### Edit Biasa
1. Update kode — **saat tiap file selesai diedit**: ambil waktu WITA → tulis baris file ke CHANGELOG (atau simpan dulu waktunya bila belum bisa menulis).
2. Rampungkan CHANGELOG entry berjalan — isi entry + semua baris section File **setelah seluruh perubahan file untuk iterasi selesai**; **tetapkan tanggal & jam judul di akhir**.
3. Self-check hasil perubahan.
4. **JANGAN commit / tag / push.**
5. Ulang sampai user perintah commit/tag.

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
1. versionCode +1.
2. versionName disesuaikan setelah ada perubahan.
3. Ubah status judul → `***PUSH***` / `***RELEASE***`, perbarui tanggal & jam WITA saat itu.
4. Buat entry CHANGELOG baru (teratas, `***ONGOING***`) di sesi kerja berikutnya.
5. Kembali ke Edit Biasa.

---

## 5. Perilaku AI

| ✅ Lakukan | ❌ JANGAN |
|------------|-----------|
| baca AGENTS dulu | refactor tanpa diminta |
| cek git status/log | ubah file di luar scope |
| perubahan minimal & fokus | audit project tanpa diminta |
| jawab singkat & actionable | checklist panjang |
| Bahasa Indonesia | build / revert tanpa konfirmasi |
| self-check sebelum selesai | — |

---

## 6. Catatan Komunikasi

- JANGAN gunakan tabel markdown di chat.
- DILARANG tulis laporan section (Accomplished, Next Steps, Ringkasan, dll) di chat — langsung ke inti.
- Bicaralah manusiawi & praktis (saya programmer, tapi bukan AI yang hafal seluruh ekosistem Android).
