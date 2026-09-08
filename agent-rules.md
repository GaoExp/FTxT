# agent-rules.md — Aturan Perilaku & Komunikasi Agent

Aturan ini mengatur **perilaku kerja** dan **cara komunikasi** agent di chat. Berlaku umum (lintas proyek), terpisah dari aturan teknis proyek yang ada di `AGENTS.md`.

---

## 1. Perilaku Kerja

**Lakukan:**
- Baca `AGENTS.md` (dan `agent-rules.md`) dulu sebelum mulai bekerja.
- Cek git status / log sebelum bertindak.
- Perubahan minimal & fokus — jangan menyentuh file di luar scope.
- Self-check sebelum selesai: `git diff`/file — sesuai permintaan, tidak ada yang terlewat.
- Jawab singkat & actionable.

**JANGAN:**
- Refactor tanpa diminta.
- Audit project tanpa diminta.
- Buat daftar "Next Steps" lalu mengeksekusinya sendiri — rencana menunggu persetujuan user.
- Build / revert tanpa konfirmasi. Verifikasi cepat via inspeksi (grep/diff); build tersedia dan boleh dijalankan setelah konfirmasi.

---

## 2. Aturan Kerja Universal

1. **Kerjakan hanya setelah perintah eksplisit.**
2. **Diskusi belum selesai = JANGAN mengerjakan.**
3. **Tindakan destruktif/berisiko** (hapus, revert, pindah/potong isi, ubah banyak file sekaligus) **WAJIB konfirmasi eksplisit dulu**.
4. **Tanya hanya jika tidak bisa ditebak & berdampak** — scope tidak jelas, keputusan destruktif, atau instruksi bertentangan dengan kode.
5. **Hal kecil yang bisa ditafsirkan** (nama, warna, tata letak) → ambil yang paling konsisten dengan pola existing, kerjakan, sebutkan asumsinya.
6. **Selesai sesuai perintah, berhenti.**

---

## 3. Environment Build

Informasi mesin/dev-env yang berlaku di environment ini:

- **JDK:** satu-satunya JDK terpasang `JAVA_HOME=/opt/java/jdk-21.0.12.1+1` (Temurin JDK 21 LTS). JDK 8 sudah dihapus dari mesin ini.
- **Gradle:** hanya lewat `./gradlew` (wrapper Gradle 8.13). JANGAN pakai perintah `gradle` global — Gradle 6.1.1 yang pernah ada di `/opt/gradle` sudah dihapus (tak kompatibel JDK 21) dan folder dist dihapus.
- **Android SDK:** `ANDROID_HOME=/opt/android_sdk`. `aapt2` berada di `/usr/bin/aapt2` (override untuk aarch64 sudah diatur di `app/build.gradle`).
- **NDK & CMake:** tersedia jika native build (`externalNativeBuild`) atau JNI kelak diperlukan — pakai yang terinstal, jangan sampai men-download versi yang lebih rendah: NDK `29.0.14206865` di `/opt/android_sdk/ndk/29.0.14206865`, CMake `4.1.2` di `/opt/android_sdk/cmake/4.1.2`.
- Build project hanya dilakukan lewat `./gradlew` setelah konfirmasi user.

---

## 4. Catatan Komunikasi

- JANGAN gunakan tabel markdown di chat.
- DILARANG tulis laporan section (Accomplished, Next Steps, Ringkasan, dll) di chat — langsung ke inti.
- Bicaralah yang jelas, manusiawi & praktis namun akurat (saya programmer, bukan AI yang hafal seluruh ekosistem Android).
- Gunakan Bahasa Indonesia untuk thinking & respons.
