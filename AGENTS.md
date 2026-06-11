# AGENTS.md — Pedoman AI Project
 
Dokumen ini adalah aturan kerja untuk AI agent yang memodifikasi project. 
 
Gunakan bahasa Indonesia. 
 
Tujuan: 
- konsistensi versi 
- konsistensi dokumentasi (CHANGELOG, README, STRUKTUR, PANDUAN)
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
 
`1.1.1.15.4`  
   
### IMPORTANT  
      
Project ini TIDAK menggunakan semantic versioning standar.
      
JANGAN menerapkan aturan semantic versioning standar pada project ini.
      
Contoh yang VALID:
      
`1.1.1.15.4` → `2.1.1.16.0`
      
BUKAN:
      
`1.1.1.15.4` → `2.0.0.0.0`

### Arti Komponen 

major: 
- milestone besar 
- generasi project 
- perubahan arsitektur besar 
- boleh naik kapan diperlukan 
- selalu dianggap sebagai feature release
    
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
- major naik
- removed naik
- restored naik
- perubahan feature-level besar

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
- minor +1 saat major naik 
- removed TIDAK reset 
- restored TIDAK reset 

### Contoh 

Awal: 

`1.1.1.15.4` 

arti: 
- major 1 
- 1 fitur pernah dihapus 
- 1 fitur pernah dipulihkan 
- feature release ke-15 
- patch 4

Tambah fitur: 

`1.1.1.16.0` 

minor +1
patch reset 

Fitur dihapus: 

`1.2.1.17.0` 

removed +1
minor +1
patch reset 

Fitur kembali: 

`1.2.2.18.0` 

restored +1
minor +1
patch reset 

Bugfix: 

`1.2.2.18.1` 

patch +1 

Major release: 

`3.2.2.19.0` 

major +1
minor +1
patch reset
    
### Algoritma Increment

Feature baru:
minor+1
patch=0

Feature removed:
removed+1
minor+1
patch=0

Feature restored:
restored+1
minor+1
patch=0

Bugfix:
patch+1

Major release:
major+1
minor+1
patch=0
---

## 2. CHANGELOG

CHANGELOG adalah riwayat perubahan release.

File:
- `CHANGELOG.md` (root) — markdown, untuk GitHub
- `app/src/main/assets/CHANGELOG.txt` — plain text, untuk in-app

Catat perubahan di **entry versi yang sedang berjalan** (bukan entry baru).

Gunakan section sesuai kebutuhan. Urutan section WAJIB mengikuti urutan berikut:

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
🔢 Version
```

### Aturan 

WAJIB: 
- semua file yang diubah dicatat
- section WAJIB sesuai urutan di atas

JANGAN: 
- membuat entry versi baru
- menambah changelog untuk perubahan trivial
- mencatat `app/build.gradle` di `✏️ File Changed` jika hanya bump version — 🔢 Version sudah mewakili
- mengubah urutan section
- menulis penjelasan panjang

Ringkas, faktual, langsung ke perubahan.

### Khusus CHANGELOG.txt (in-app)

Section `🗒️ File Added`, `✏️ File Changed`, `🔥 File Removed` **PENCATATAN DIKECUALIKAN** — tidak relevan untuk dokumentasi di dalam aplikasi.
 
--- 
 
## 3. Dokumentasi

Dokumentasi hanya diupdate **saat diperintah user**, dan itu pun dilakukan **sebelum commit**.

Tidak perlu update dokumentasi setiap kali ada perubahan kode — bisa ditumpuk sekaligus nanti.

--- 
 
## 4. Workflow

### 1. Edit Biasa (looping)

1. update kode
2. catat perubahan di `CHANGELOG.md` entry versi berjalan
3. **JANGAN** commit / tag / push
4. ulang sampai user perintah **commit & tag**

### 2. Pre-release (sebelum commit & tag)

1. periksa semua dokumen (README, STRUKTUR, PANDUAN, CHANGELOG)
2. update revisi / perbaiki jika ada yang tidak sinkron
3. pastikan semua sudah sesuai
4. beri tahu user bahwa siap di-commit & tag

### 3. Rilis (hanya saat diperintahkan)

1. `git add -A && git commit`
2. `git tag vX.X.X.X.X`
3. **JANGAN** push — user yang akan push sendiri

### 4. Setelah Push (entry versi baru)

Saat user sudah push dan mulai mengerjakan perubahan baru:

1. update versionCode (+1)
2. update versionName sesuai Algoritma Increment (Section 1)
3. buat entry CHANGELOG baru (paling atas) dengan versi baru
4. kembali ke **Edit Biasa**
 
--- 
 
## 5. Perilaku AI 

Penerapan berlaku kepada semua Agent AI yang terlibat dalam pengerjaan Project, terutama untuk model Agent GitHub CoPilot (important) 
Kerjakan hanya sesuai request user. 

JANGAN: 
- melakukan refactor tanpa diminta 
- mengubah file di luar scope request 
- mengaudit seluruh project tanpa diminta 
- membuat checklist panjang maupun singkat untuk request sederhana (IMPORTANT) 
- memberi penjelasan panjang bila tidak diminta (IMPORTANT)

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

### Chat Response Rules 

Default response style: 
- singkat 
- fokus 
- actionable 
- tulis ulang chat di file talkhere.txt jika user melaporkan chat terpotong / terputus
- dalam talkhere.txt letakkan entry chat baru di atas, biarkan chat lama berada di bawah

JANGAN: 
- menjelaskan langkah yang tidak dilakukan 
- membuat audit project tanpa diminta 
- menjelaskan teori panjang untuk perubahan sederhana 
- menambahkan rekomendasi besar di luar request tanpa diminta

Jika request sederhana = jawab sederhana.
