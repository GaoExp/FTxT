# AGENTS.md — Aturan AI Project FTxT

**⚠️ ATURAN PALING PENTING — BACA DULU:**
- **JANGAN commit / tag / push tanpa perintah user.**
- **Kerjakan hanya sesuai request user.**
- **JANGAN refactor / ubah file di luar scope.**
- **BACA ulang file ini setiap mulai bekerja.**
- **JANGAN menulis laporan section apapun.**
- ** Gunakan bahasa Indonesia untuk thinking dan respons.**

---

## 1. Versioning (`app/build.gradle`)
 
Format: **semver** `major.minor.patch`

| Komponen | Naik saat | Reset |
|----------|-----------|-------|
| **major** | milestone besar, arsitektur, breaking change | `minor=0, patch=0` |
| **minor** | fitur baru / fitur dihapus / fitur dipulihkan | `patch=0` |
| **patch** | bugfix, optimasi, maintenance | — |

**Algoritma:**
- Fitur baru/dihapus/dipulihkan → minor+1, patch=0
- Bugfix/optimasi/maintenance → patch+1
- Breaking change / arsitektur → major+1, minor=0, patch=0
- Removed/restored dicatat di CHANGELOG, tidak perlu komponen versi sendiri

---

## 2. CHANGELOG

Entry dicatat di **versi berjalan** (bukan entry baru). Urutan section WAJIB:

```
✨ Fitur Baru
🚮 Fitur Dihapus
📥 Fitur Dipulihkan
♻️ Perubahan Fitur
🔧 Optimasi & Penyesuaian
🐞 Bug Fixes
💡 Catatan
🗒️ File Added
✏️ File Changed
🔥 File Removed
```

**Format judul entry:** `# [x.x.x] - yyyy-mm-dd versionCode xxx` — versionName & versionCode dicatat di judul.

**Entry yang di-merge** (beberapa versi digabung dalam satu entry): section **🗒️ File Added**, **✏️ File Changed**, dan **🔥 File Removed** diabaikan (tidak ditulis).

**WAJIB:** cek git log untuk tahu status push. **JANGAN** buat entry baru sebelum commit di-push.

Sinkron: `cp CHANGELOG.md app/src/main/assets/` jika tidak build.

---

## 3. Workflow

### Edit Biasa
1. update kode
2. catat di CHANGELOG entry versi berjalan
3. **JANGAN commit / tag / push**
4. ulang sampai user perintah **commit & tag**

### Pre-release
1. periksa semua dokumen (README, STRUKTUR, PANDUAN, CHANGELOG)
2. hapus label ***ONGOING*** pada judul entry
3. bilang user siap di-commit & tag

### Rilis (hanya jika diperintah)
1. `git add -A && git commit -m "vX.X.X deskripsi"`
2. `git tag vX.X.X`
3. **JANGAN push** — user yang push

### Setelah Push ( langsung buat judul entry versi baru)
1. versionCode +1
2. versionName akan disesuaikan setelah ada perubahan (major.minor.patch)
3. buat entry CHANGELOG baru (paling atas dengan label ***ONGOING***)
4. kembali ke Edit Biasa

---

## 4. Perilaku AI

| ✅ Lakukan | ❌ JANGAN |
|------------|-----------|
| baca agents.md dulu | refactor tanpa diminta |
| cek git status/log | ubah file di luar scope |
| perubahan minimal & fokus | audit project tanpa diminta |
| jawab singkat & actionable | build |
| Bahasa Indonesia | checklist panjang |
| | push / commit tanpa izin |
| | revert perubahan yang dilakukan user tanpa konfirmasi |

**CHAT RULES:**
- **JANGAN gunakan tabel markdown** di chat — tabel tidak berfungsi di UI chat ini. Gunakan list atau paragraf biasa.

- *DILARANG* tulis laporan section seperti Accomplished, Progress, Critical Context, Planning, atau ringkasan/checklist apapun di chat — langsung ke inti.

- **Jangan pernah lakukan build** *apapun alasannya*
 
 **Ingat: JANGAN push / commit / tag tanpa perintah user dan JANGAN menulis laporan section apapun yang DILARANG.**
