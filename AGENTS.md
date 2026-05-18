# Pedoman untuk AI Agents

Dokumen ini menjelaskan konvensi yang digunakan di project **FTxT** agar AI agent lain bisa konsisten saat membaca/menulis kode, changelog, readme, dan versi.

---

## 📦 Versioning (`app/build.gradle`)

```
versionCode → integer, selalu +1 setiap update (tidak pernah reset)
versionName → "major.minor"
```

### Aturan `versionName`:

| Komponen | Arti | Kapan naik? |
|----------|------|-------------|
| **major** | Penambahan fitur baru | Naik +1, **minor di-reset ke 1** |
| **minor** | Perbaikan/bug fix | Naik +1 |

### Contoh:

```
8.1  → fitur baru (HSV Color Picker)
8.2  → perbaikan (foreground service, dependencies, dll)
8.3  → perbaikan (text size real-time)
9.1  → fitur baru berikutnya (minor di-reset!)
9.2  → perbaikan setelah fitur baru
```

### Cara update:

```groovy
// app/build.gradle
versionCode 29    // selalu +1
versionName "8.3" // ikut aturan major.minor
```

---

## 📝 CHANGELOG.md

### Format entry baru:

```markdown
## [major.minor] - YYYY-MM-DD

### ✨ Fitur Baru
- **Nama Fitur**: Penjelasan singkat
  - Detail teknis jika perlu

### 🔧 Perbaikan
- **Nama Perbaikan**: Penjelasan

### 🐛 Bug Fixes
- **Nama Bug**: Penjelasan dan apa yang ditambahkan/diubah

### 📝 File yang Dibuat & Diubah

#### ✅ NamaFile.java (NEW/UPDATED)
- Perubahan 1
- Perubahan 2

#### ✅ app/build.gradle (UPDATED)
- versionCode: X → Y
- versionName: A.B → C.D

### 📊 Version Numbering
- **major** = Alasan kenaikan major
- **minor** = Alasan kenaikan minor
```

### Aturan:
- Entry baru ditaruh **di paling atas** (di atas entry sebelumnya)
- Setiap file yang diubah/dibuat dicatat di section "File yang Dibuat & Diubah"
- Gunakan icon: `✨` fitur baru, `🔧` perbaikan, `🐛` bug fix, `🔄` optimasi

---

## 📖 README.md

### Yang perlu diupdate:

| Bagian | Kapan diupdate |
|--------|----------------|
| `Current Version` | Setiap update versi |
| `Latest Changes` | Setiap update — tulis perubahan utama |
| Daftar fitur | Hanya saat **fitur baru** ditambahkan |
| Struktur project | Jika ada file baru/dihapus |
| `Last Updated` | Setiap update |

### Cara pengisian `Latest Changes`:

```
### Current Version: X.Y

**Latest Changes (vX.Y)**
- ✨ Fitur baru: <nama fitur>
- 🐛 Bug fix: <nama bug>
```

### Catatan:
- README adalah **dokumen pengguna akhir** — tulis cara penggunaan, bukan teknis implementasi
- Detail implementasi simpan di CHANGELOG

---

## 🚀 Alur kerja untuk AI agent

1. **Baca AGENTS.md** dulu sebelum mengerjakan task
2. Cek `app/build.gradle` untuk versi saat ini
3. Cek `CHANGELOG.md` untuk entry terakhir
4. Tentukan versi baru (major/minor naik sesuai jenis perubahan)
5. Lakukan perubahan kode
6. Update `versionCode` (+1) dan `versionName` di `app/build.gradle`
7. Tambah entry di `CHANGELOG.md` (paling atas)
8. Update `README.md` jika perlu (fitur baru → tambah section, selalu update current version)
