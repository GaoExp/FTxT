# AGENTS.md — Aturan AI Project FTxT

**⚠️ ATURAN PALING PENTING — BACA DULU:**
- **BACA ulang file ini setiap mulai bekerja.**
- **JANGAN commit / tag / push tanpa perintah user.**
- **Eksekusi hanya setelah perintah eksplisit — diskusi belum selesai = berhenti.**
- **JANGAN refactor / ubah file di luar scope.**
- **Tanya dulu saat ragu — jangan asal improvisasi.**
- **Tindakan destruktif (hapus/revert/ubah besar) WAJIB konfirmasi eksplisit.**
- **JANGAN build; JANGAN menulis laporan section/bullet list di chat.**
- **Gunakan bahasa Indonesia untuk thinking dan respons.**
- **Tidak perlu sinkronisasi dokumen di assets, Sinkronisasi Gradle akan menanganinya secara otomatis saat build.**
---

## 1. Versioning (`app/build.gradle`)

Format: **semver** `major.minor.patch`

| Komponen | Naik saat | Reset |
|----------|-----------|-------|
| **major** | milestone besar, arsitektur, breaking change | `minor=0, patch=0` |
| **minor** | fitur baru / fitur dihapus / fitur dipulihkan | `patch=0` |
| **patch** | bugfix, optimasi, maintenance | — |

**Algoritma:**
- Fitur baru / dihapus → minor+1, patch=0
- Bugfix / optimasi / maintenance → patch+1
- Breaking change / arsitektur → major+1, minor=0, patch=0

---

## 2. CHANGELOG

Entry dicatat di **versi berjalan** (bukan entry baru). Urutan section WAJIB:

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

**Format judul entry:** `# [x.x.x] - yyyy-mm-dd HH:mm WITA versionCode xxx ***STATUS***` — versionName & versionCode dicatat di judul; jam memakai WITA (UTC+8) format 24 jam. **STATUS** adalah salah satu dari:
- `***ONGOING***` — entry versi berjalan sedang dikerjakan; tanggal & jam dijudul diperbarui setiap kali entry diubah (pada iterasi kerja berikutnya), bukan per file/per edit.
- `***PUSH***` — dicapai saat commit tanpa tag; ditandai segera saat commit (push menyusul oleh user), tanggal & jam diperbarui saat itu.
- `***RELEASE***` — dicapai saat commit + tag; ditandai segera (push menyusul), tanggal & jam diperbarui saat itu. Jika status sudah `***PUSH***` lalu di-tag, diubah jadi `***RELEASE***`.

**Entry yang di-merge** (beberapa versi digabung dalam satu entry): section **🗒️ File Added**, **✏️ File Changed**, dan **🔥 File Removed** diabaikan (tidak ditulis).

**WAJIB:** cek git log untuk tahu status commit. **JANGAN** buat entry baru sebelum versi sebelumnya di-commit (push menyusul oleh user).

**WAJIB dapat waktu WITA:** Jalankan perintah berikut untuk mendapatkan waktu WITA yang benar sebelum menulis/memperbarui jam di judul entry CHANGELOG:
```
TZ=Asia/Makassar date +"%Y-%m-%d %H:%M WITA"
```
**JANGAN asal pilih waktu** — selalu verifikasi dulu dengan perintah di atas.

**Aturan penulisan isi entry (CHANGELOG untuk pembaca user, bukan riwayat kerja):**
- **Entry dicatat satu kali setelah seluruh perubahan file untuk iterasi itu selesai — jangan per file/per edit.** Jam pada judul dicatat saat entry terakhir ditulis; jangan memperbarui judul setiap kali satu file selesai diedit.
- **Satu perubahan = satu poin, tulis hasil akhir.** Jika sesuatu dirombak berkali-kali selama development, catat hanya kondisi final — jangan tiap iterasi/tahap kerja.
- **Jangan menumpuk poin yang saling menimpa.** Saat mencatat perubahan yang sudah ada poinnya di entry berjalan, perbarui/tulis ulang poin itu jadi hasil akhir, bukan tambah poin baru.
- **Jangan catat detail internal.** Detail teknis implementasi (nama helper, cara polling, key prefs, dll) tidak ditulis kecuali berpengaruh ke perilaku user.
- **Cek konsistensi sebelum selesai:** tidak boleh ada poin dalam satu entry yang saling bertentangan atau menduplikasi.
- **Saat merapikan entry lama, jangan mengubah fakta** — hanya menggabungkan poin yang saling menimpa atau menghapus jejak iterasi yang sudah obsolete.
- **Section 🗒️ File Added, ✏️ File Changed, 🔥 File Removed wajib disertakan waktu perubahan** — Format: `- yyyy-mm-dd hh:mm — \`filename\` — deskripsi perubahan`. Waktu dicatat berdasarkan urutan agent mengerjakan perubahan tersebut.

---

## 3. Aturan Kerja Universal

1. **Kerjakan hanya setelah user memberi perintah eksplisit.** Bertanya, menjelaskan, atau menampilkan rencana BUKAN perintah eksekusi.
2. **Diskusi belum selesai = JANGAN mengerjakan.** Kalau user masih bertanya/membahas, berhenti dan tunggu arahan.
3. **Jangan pernah membuat daftar "Next Steps"/rencana lanjutan lalu langsung mengeksekusinya sendiri.** Rencana apa pun menunggu persetujuan user.
4. **Tindakan destruktif/berisiko** (hapus file, revert, pindah/potong isi file, mengubah banyak file sekaligus) **WAJIB meminta konfirmasi eksplisit dulu**, walaupun sudah direncanakan.
5. **Tanya hanya jika benar-benar tidak bisa ditebak dan berdampak** — tujuan/scope tidak jelas, keputusan destruktif, atau instruksi bertentangan dengan kode.
6. **Hal kecil yang bisa ditafsirkan (nama, warna, tata letak) → ambil yang paling konsisten dengan pola yang sudah ada, kerjakan, sebutkan asumsinya** — jangan tanya dulu.
7. **Selesai mengerjakan sesuai perintah, berhenti.** Jangan lanjut ke pekerjaan tambahan yang tidak diminta.
8. **Self-check WAJIB sebelum selesai:** baca ulang hasil kerja (`git diff` / file yang diubah) — pastikan sesuai permintaan, tidak ada file di luar scope yang tersentuh, dan tidak ada yang terlewat. Baru nyatakan selesai.

---

## 4. Workflow

### Edit Biasa
1. update kode
2. catat di CHANGELOG entry versi berjalan — tulis **sekali setelah seluruh perubahan file untuk iterasi itu selesai** (bukan per file), perbarui tanggal & jam WITA pada judul
3. self-check: baca ulang hasil perubahan (`git diff`/file) — sesuai permintaan & tidak menyentuh file di luar scope
4. **JANGAN commit / tag / push**
5. ulang sampai user perintah **commit & tag**

### Pre-release
1. periksa semua dokumen (README, STRUKTUR, PANDUAN, CHANGELOG)
2. pastikan status judul entry masih `***ONGOING***` dengan tanggal & jam WITA terbaru
3. bilang user siap di-commit & tag

### Rilis (hanya jika diperintah)
1. `git add -A && git commit -m "vX.X.X deskripsi"`
2. **JANGAN ubah status CHANGELOG** — status tetap `***ONGOING***` sampai user bilang push
3. `git tag vX.X.X` bila versi akan di-tag
4. **JANGAN push** — user yang push menyusul setelah commit

### Setelah Push (langsung buat judul entry versi baru)
1. versionCode +1
2. versionName akan disesuaikan setelah ada perubahan (major.minor.patch)
3. ubah status judul entry → `***PUSH***` (commit tanpa tag) atau `***RELEASE***` (commit + tag), perbarui tanggal & jam WITA saat itu
4. buat entry CHANGELOG baru (paling atas dengan label `***ONGOING***`) — dibuat saat sesi kerja berikutnya dimulai setelah commit (push menyusul oleh user)
5. kembali ke Edit Biasa

---

## 5. Perilaku AI

| ✅ Lakukan | ❌ JANGAN |
|------------|-----------|
| baca agents.md dulu | refactor tanpa diminta |
| cek git status/log | ubah file di luar scope |
| perubahan minimal & fokus | audit project tanpa diminta |
| jawab singkat & actionable | checklist panjang |
| Bahasa Indonesia | build |
| self-check hasil kerja sebelum selesai | revert perubahan yang dilakukan user tanpa konfirmasi |

---

## 6. Catatan Komunikasi

- **JANGAN gunakan tabel markdown di chat** — tabel tidak berfungsi di UI chat ini. Gunakan list atau paragraf biasa.
- *DILARANG* tulis laporan section seperti Accomplished, Constraints & Preferences, Progress, In Progress, Key Decisions, Next Steps, Critical Context, Relevant Files, Ringkasan, Planning, atau Checklist apapun di chat — langsung ke inti.
- PENTING: Saya programmer, tetapi jangan menjelaskan seolah saya AI yang sudah mengetahui seluruh ekosistem Android. Saya ingin penjelasan yang manusiawi dan praktis.
