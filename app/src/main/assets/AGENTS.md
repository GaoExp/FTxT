# AGENTS.md — Pedoman AI Project FTxT

Dokumen ini adalah aturan kerja untuk AI agent yang memodifikasi project FTxT.

Gunakan bahasa Indonesia.

Tujuan:
- konsistensi versi
- konsistensi CHANGELOG
- konsistensi README
- mudah dibaca manusia
- mengurangi perilaku AI yang terlalu verbose atau over-engineering

---

## 1. Versioning

File:
`app/build.gradle`

### versionCode

WAJIB:
- integer
- selalu +1 setiap update
- tidak pernah reset

### versionName

Format:

`major.removed.restored.minor.patch`

Contoh:

`1.0.0.12.2`

### Arti Komponen

major:
- milestone besar
- generasi project
- perubahan arsitektur besar
- boleh naik kapan diperlukan

removed:

Counter historis fitur yang pernah dihapus, deprecated, dinonaktifkan, atau dipensiunkan.

NAIK saat:
- fitur dihapus
- fitur deprecated
- fitur disable permanen
- fitur diganti total

TIDAK PERNAH TURUN.

restored:

Counter historis fitur yang pernah dikembalikan setelah sebelumnya dihapus atau dinonaktifkan.

NAIK saat:
- fitur lama kembali
- fitur deprecated diaktifkan lagi
- fitur retired dipulihkan

TIDAK PERNAH TURUN.

minor:

Counter feature release.

NAIK saat:
- fitur baru ditambahkan
- fitur besar ditambahkan
- fitur lama kembali
- perubahan feature-level besar
- perubahan lifecycle fitur

CATATAN:

minor tetap naik meskipun removed atau restored berubah.

removed/restored tidak menggantikan minor.

patch:

Digunakan untuk:
- bugfix
- optimization
- maintenance
- refactor kecil
- dependency update
- UI cleanup

### Aturan

- patch reset → `0` saat minor naik
- minor TIDAK reset saat major naik
- removed TIDAK reset
- restored TIDAK reset

### Contoh

Awal:

`1.0.0.12.2`

arti:
- major 1
- 0 fitur pernah dihapus
- 0 fitur pernah dipulihkan
- feature release ke-12
- patch 2

Tambah fitur:

`1.0.0.13.0`

minor +1  
patch reset

Fitur dihapus:

`1.1.0.14.0`

removed +1  
minor +1  
patch reset

Fitur kembali:

`1.1.1.15.0`

restored +1  
minor +1  
patch reset

Bugfix:

`1.1.1.15.1`

patch +1

Major release:

`2.1.1.15.0`

major +1

---

## 2. CHANGELOG.md

CHANGELOG adalah riwayat perubahan release.

Entry baru:

WAJIB ditaruh di paling atas.

Format:

```md
## [X.X.X.X.X] - YYYY-MM-DD
```

Gunakan section sesuai kebutuhan:

```md
### ✨ Fitur Baru
### ♻️ Lifecycle Changes
### 🔧 Perbaikan
### 🐛 Bug Fixes
### 🔄 Optimasi
```

### Aturan

WAJIB:
- semua file yang benar-benar diubah dicatat
- update versionCode dicatat
- update versionName dicatat

Gunakan:

`♻️ Lifecycle Changes`

bila:
- fitur dihapus
- fitur dipulihkan
- fitur deprecated
- fitur disable
- perubahan lifecycle fitur

JANGAN:
- menambah changelog untuk perubahan trivial
- menulis penjelasan terlalu panjang
- membuat subsection yang tidak perlu
- mengulang detail implementasi kecil

Ringkas, faktual, langsung ke perubahan.

---

## 3. README.md

README adalah dokumentasi pengguna akhir.

JANGAN isi:
- detail implementasi teknis
- refactor internal
- cleanup kecil
- dependency update
- patch-only changes
- perubahan invisible bagi user

WAJIB update bila:
- ada fitur baru
- ada fitur dihapus
- ada fitur dipulihkan
- ada perubahan UI/UX besar
- Current Version berubah
- Latest Changes berubah
- Last Updated berubah

Update hanya jika relevan:
- daftar fitur
- struktur project
- requirement
- permission

JANGAN update README hanya karena:
- bugfix kecil
- optimization
- maintenance
- cleanup kode

Detail teknis → CHANGELOG.md

---

## 4. Workflow AI

Sebelum mengedit:

1. baca AGENTS.md
2. cek `build.gradle`
3. cek entry terbaru CHANGELOG

Sesudah mengedit:

1. update kode
2. update versionCode
3. update versionName
4. tambah CHANGELOG entry bila relevan
5. update README bila relevan

---

## 5. Perilaku AI

Kerjakan hanya sesuai request user.

JANGAN:
- melakukan refactor tanpa diminta
- mengubah file di luar scope request
- mengaudit seluruh project tanpa diminta
- mengecek dependency, permission, README, atau CHANGELOG bila tidak relevan
- membuat checklist panjang untuk request sederhana
- memberi penjelasan panjang bila tidak diminta
- melakukan over-engineering

Utamakan:
- perubahan minimal
- perubahan terfokus
- jawaban singkat
- edit seperlunya
- solusi langsung bisa dipakai

Default behavior:

jangan terlalu rajin.

Jika user meminta perubahan kecil,
kerjakan perubahan kecil.

JANGAN memperluas scope sendiri.

### Chat Response Rules

Default response style:
- singkat
- fokus
- actionable

JANGAN:
- menjelaskan langkah yang tidak dilakukan
- membuat audit project tanpa diminta
- menjelaskan teori panjang untuk perubahan sederhana
- menambahkan rekomendasi besar di luar request

Jika request sederhana:

jawab sederhana.