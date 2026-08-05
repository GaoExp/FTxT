# Konsep: Battery Bar (Bar Baterai Fleksibel)

**Status:** Konsep — belum diputuskan untuk dikerjakan
**Inspirasi awal:** Fitur garis baterai horizontal di Redmi Note 5A (Xiaomi)

---

## 1. Gambaran

Modul overlay baru yang menampilkan **batangan baterai** sebagai modul terpisah dari teks angka. Bar ini **fleksibel**: bisa ditempatkan di mana pun di layar, bisa **horizontal atau vertikal**, dan **ukuran panjang/lebar bisa disesuaikan**. Bagian bar terisi sesuai persentase baterai.

Penempatan bar punya **dua mode**: **Cepat** (snap otomatis ke salah satu dari 4 sisi layar dengan panjang maksimal) dan **Manual** (posisi & ukuran bebas diatur user, bahkan di tengah layar).

Konsep menggabungkan kesederhanaan garis Redmi dengan kustomisasi bebas khas FTxT.

---

## 2. Referensi Perilaku (Redmi Note 5A) & Arah Pengembangan

- **Redmi:** garis tipis horizontal di bagian paling atas layar, terisi dari kiri sesuai persen, tanpa opsi kustomisasi.
- **Arah FTxT:** bentuk tetap "bar", tetapi posisi bebas + orientasi H/V + ukuran bebas + warna bisa diatur, plus mode Cepat untuk gaya ala Redmi.

---

## 3. Konsep Visual

Contoh sisa baterai 62%:

**Horizontal** (isi dari kiri ke kanan):
```
▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓░░░░░░░
└──────── 62% ────────┘└ 38% ┘
```

**Vertikal** (isi dari bawah ke atas):
```
░
░
░
░
▓
▓
▓
▓
▓
▓
```

- Bar berupa **kotak tipis** dengan **panjang & ketebalan yang bisa diatur**.
- **Bagian terisi** = warna utama (default hijau; opsi otomatis: hijau >20%, kuning 11–20%, merah ≤10%).
- **Bagian kosong** = strip samar (abu-abu gelap semi-transparan) supaya batas isi terlihat.
- Ujung bar membulat (radius).
- **Indikasi status** (konsep): animasi bar saat **mengisi daya** dan saat **baterai rendah**, supaya status baterai mudah terlihat.

---

## 4. Mode Penempatan: Cepat vs Manual

### Mode Cepat (Quick)
- User memilih salah satu **4 sisi layar**: Atas, Bawah, Kiri, Kanan.
- Bar otomatis menempel sisi tersebut dengan **panjang maksimal** (lebar layar untuk atas/bawah, tinggi layar untuk kiri/kanan).
- Orientasi bar ditentukan sisi: atas/bawah → horizontal, kiri/kanan → vertikal.
- Kontrol posisi & panjang manual disembunyikan/nonaktifkan.
- Ketebalan & warna tetap bisa diatur (opsional).

### Mode Manual (Kustom)
- Posisi bebas via slider X/Y (persentase layar) — bisa di tengah.
- **Orientasi bar** bebas: horizontal atau vertikal.
- **Panjang & ketebalan** diatur via slider.
- Semua kontrol posisi/ukuran aktif.

**Switch** di panel untuk beralih antara Mode Cepat dan Mode Manual.

**Default saat pertama diaktifkan:** Mode Cepat, sisi **Atas**.

---

## 5. Kontrol Overlay

### Section Tampilan
- Switch **Aktif** — nyalakan/matikan overlay.
- Switch **Mode Cepat/Manual** — beralih cara penempatan.
- (Cepat) **Pilihan Sisi** — Atas / Bawah / Kiri / Kanan.
- (Manual) Toggle **Orientasi Bar** — Horizontal / Vertikal.
- (Manual) Slider **Panjang** — ukuran bar mengikuti arahnya.
- Slider **Ketebalan** — tebal bar (berlaku di kedua mode).
- **Warna Bar** — color picker untuk bagian isi.
- Opsi **Auto Color** — otomatis hijau/kuning/merah sesuai level (atau manual).
- **Warna Strip Kosong** + toggle tampilkan strip.
- **Ambang Baterai Rendah** — slider, default **40%** (bar berubah warna + animasi fade saat level < ambang).
- **Warna Baterai Rendah** — color picker untuk warna bar saat level < ambang.
- **Interval Fade** — slider kecepatan animasi memudar (fade pelan).
- **Interval update** — popup 0.2–10 detik.

### Section Posisi
- (Manual) Slider **X/Y** — posisi persentase layar.
- (Manual) **D-Pad** — geser halus.
- (Manual) **Preset posisi** + koordinat.
- **Safe Area** — batasi area notch.
- **Checkbox "Kunci Posisi"** — tampil seperti modul lain, tetapi **disabled** (abu-abu, tidak bisa diakses) dengan label `[✓] Kunci Posisi (tidak tersedia)` — karena drag tidak diprioritaskan, posisi diandalkan ke slider + D-Pad.

### Section Shadow (opsional)
- Toggle, warna, blur, offset X/Y.

### Section Background
- Untuk bar tipis kurang relevan — kemungkinan tidak dibuat.

---

## 6. Perbedaan dengan Overlay Lain

| Aspek | Modul lain (Text, FPS, Battery Stats) | Battery Bar |
|-------|---------------------------------------|-------------|
| Posisi | Bebas (drag + slider X/Y) | Bebas + Mode Cepat (snap 4 sisi) |
| Lebar/Tinggi | WRAP_CONTENT mengikuti teks | Diatur user (panjang & ketebalan) |
| Isi visual | Teks | Bar persegi terisi persen |
| Orientasi bar | Tidak ada | Horizontal / Vertikal (fitur unik) |
| Orientasi layar | Posisi disimpan per orientasi layar | Posisi disimpan per orientasi layar |

---

## 7. Keputusan Desain

**Sudah diputuskan:**
- **Area sentuh** — tidak perlu area sentuh khusus; posisi diandalkan ke slider + D-Pad (bar tipis sulit di-drag).
- **Notch/cutout** — diabaikan (dianggap masalah device pengguna); solusi alternatif sudah ada lewat Mode Manual.
- **Default awal** — Mode Cepat, sisi **Atas**.
- **Indikasi status** — menarik untuk diimplementasikan: animasi saat mengisi daya & baterai rendah (detail menyusul).
- **Animasi charging** — **flash putih bergerak kiri → kanan** di atas bar (efek shine). Teknik: `ValueAnimator` menggerakkan gradien putih semi-transparan di atas bar + `invalidate()` tiap frame; animator berhenti saat charging berhenti. Untuk indikator baterai rendah (hijau/kuning/merah) + animasi menyusul.
- **Baterai rendah** — animasi **fade pelan** (memudar transparan berulang) aktif saat level **< ambang**. Ambang default **40%** bisa diatur slider. Warna bar saat rendah bisa dipilih via color picker. Interval/kecepatan fade bisa disesuaikan.
- **Data tidak tersedia** — bar tampil **strip kosong penuh** (tanpa isi), pulih otomatis saat data kembali (Opsi A — sederhana, karena kasus jarang terjadi).

**Belum diputuskan:**
- **Arah pengisian vertikal:** dari bawah ke atas, atau dari atas ke bawah?
- **Warna (normal, saat level ≥ ambang):** otomatis berdasarkan level, manual (1 warna user), atau keduanya? (Warna baterai rendah sudah diputuskan: color picker sendiri.)
- **Strip kosong:** ditampilkan samar, atau hanya bagian isi tanpa latar?
- **Hubungan dengan Battery Stats:** modul independen, atau tersambung (sumber persen + interval sama)?
- **Interval update:** interval sendiri atau mengikuti Battery Stats (default 5d)?
- **Batas ukuran maksimal bar (mode manual):** dibatasi (misal maks 50% layar) atau bebas sampai penuh layar?
- **Ketebalan di Mode Cepat:** tetap bisa diatur user, atau ikut default?
- **Radius:** bisa diatur, atau tetap?

**Ditunda:**
- **Penamaan & urutan di drawer** — ditunda, karena user berencana mengubah beberapa hal di drawer.
- **Aksesibilitas (TalkBack/deskripsi konten)** — dibahas lain waktu.

---

## 8. Arsitektur Komponen

```
features/battery_bar/
├── BatteryBarConfig.java          — Konfigurasi statis modul
├── BatteryBarModule.java          — WindowManager: buat/update/hapus bar
└── BatteryBarPositionController   — Kontrol posisi + ukuran + orientasi + mode
```

- **BatteryBarConfig** — `enabled`, `mode` (QUICK/MANUAL), `quickSide` (TOP/BOTTOM/LEFT/RIGHT), `orientation` (H/V), `size` (panjang), `thickness`, `color`, `autoColor`, `emptyStripColor`, `radius`, `posX`, `posY`, `updateInterval`, dll.
- **BatteryBarModule** — implement `OverlayModule`. Membuat window dengan ukuran dari config. Di Mode Cepat: panjang = dimensi layar sesuai sisi. Di Mode Manual: panjang & posisi dari config. Membaca persen via `ACTION_BATTERY_CHANGED`. Saat rotasi layar, ukur ulang & simpan posisi per orientasi.
- **Rendering bar** — custom view `onDraw()`: gambar strip kosong (full) lalu bagian terisi (persen) dengan radius; arah isi mengikuti orientasi bar (H: kiri→kanan, V: bawah→atas atau atas→bawah).
- **Kontrol** — Mode Cepat: tombol sisi. Mode Manual: slider X/Y + panjang + ketebalan + toggle orientasi.

---

## 9. Fase Pengerjaan

### Fase 1 — Modul Inti
1. Buat `BatteryBarConfig.java`
2. Buat `BatteryBarModule.java` + custom view rendering (H & V, arah isi, mode cepat)
3. Tambah lazy init + delegasi di `FloatingService` (pola modul lain)
4. Load config di `MainActivity.loadShadowConfigs()`

### Fase 2 — UI Panel
5. Buat `panel_battery_bar.xml` (mode cepat/manual, sisi, orientasi, slider, warna)
6. Buat `BatteryBarPanelController.java`
7. Buat `BatteryBarPositionController.java`
8. Buat `BatteryBarPanelFragment.java`
9. Daftarkan di `PanelManager` + `panelIdToName()` + toolbar title + drawer menu

### Fase 3 — Preset & Integrasi
10. Simpan/terapkan config di preset (`OverlayPreset` + delegate)
11. Perilaku orientasi layar: posisi per orientasi
12. Cek konsistensi `NotificationHelper`, `BootReceiver`, `isAnyModuleActive()`

### Fase 4 — Dokumentasi
13. Update STRUKTUR.md, README.md, PANDUAN.md, CHANGELOG.md + sinkron ke `app/src/main/assets/`

---

## 10. Risiko & Tantangan

- **Rendering vertikal** — arah pengisian dan perhitungan persen harus benar untuk orientasi V.
- **Ukuran tidak proporsional** — saat panjang bar kecil, perbedaan persen (misal 60% vs 65%) hampir tidak terlihat.
- **Dua jenis orientasi** — orientasi bar (H/V) vs orientasi layar (potret/lanskap) harus dibedakan jelas di UI.
- **Mode Cepat di layar berbeda** — panjang maksimal dihitung ulang saat orientasi layar berubah.
- **Animasi status** — animasi saat charging/rendah harus ringan dan jelas, tidak mengganggu.
- **Redraw efisien** — redraw bar hanya saat persen/status berubah, hindari redraw tiap interval jika nilai sama.
- **Notch/cutout** — diabaikan (keputusan: masalah device pengguna; Mode Manual sebagai alternatif).
- **Performa** — update persen tiap interval via sticky broadcast; biaya kecil tapi tetap efisien.

---

## 11. Estimasi Komponen Baru

| File | Keterangan |
|------|------------|
| `BatteryBarConfig.java` | Config modul bar |
| `BatteryBarModule.java` | Modul overlay + custom rendering bar |
| `BatteryBarPanelController.java` | UI panel bar |
| `BatteryBarPositionController.java` | Kontrol posisi + ukuran + orientasi + mode |
| `BatteryBarPanelFragment.java` | Fragment panel bar |
| `panel_battery_bar.xml` | Layout panel bar |

Total ~6 komponen baru + modifikasi (FloatingService, MainActivity, PanelManager, drawer menu, preset, dokumen). Skala pengerjaan mirip penambahan modul penuh — effort **cukup besar**.

---

## 12. Urutan Pengerjaan

1. Fase 1 (modul inti) — wajib duluan
2. Fase 2 (UI panel) — bisa diakses user
3. Fase 3 (preset & orientasi) — stabilitas
4. Fase 4 (dokumentasi) — sinkron

Sebelum mulai: putuskan 9 pertanyaan di bagian **Keputusan Desain**.

---

## 13. Catatan Diskusi

- **[2026-08-04] Ide awal** — Bar tipis membentang penuh di tepi atas layar (inspirasi Redmi Note 5A), panjang mengikuti lebar layar, menyesuaikan orientasi.
- **[2026-08-04] Revisi arah** — Bar ingin fleksibel: posisi bebas di mana pun (bukan hanya tepi), opsi horizontal/vertikal (kiri/kanan/atas/bawah), ukuran panjang & lebar bisa diatur dengan seekbar, bisa diletakkan di tengah layar.
- **[2026-08-04] Daftar kontrol** — Susunan kontrol overlay disepakati: Tampilan (aktif, mode, sisi, orientasi, panjang, ketebalan, warna, auto color, strip, interval), Posisi (X/Y, D-Pad, preset, safe area, touch passthrough), Shadow opsional, Background tidak dibuat.
- **[2026-08-04] Mode Cepat vs Manual** — Ditambahkan switch untuk beralih antara **Mode Cepat** (snap ke salah satu dari 4 sisi layar dengan panjang maksimal) dan **Mode Manual** (kustomisasi bebas posisi & ukuran).
- **[2026-08-04] Keputusan desain** — (1) Animasi indikator status saat mengisi daya & baterai rendah menarik dan mudah terlihat. (2) "Data tidak tersedia" — perlu dijelaskan lebih lanjut. (3) Redraw efisien bisa diterapkan juga ke modul lain (rencana optimasi terpisah). (4) Area sentuh tidak perlu — posisi via slider/D-Pad. (5) Notch diabaikan, Mode Manual jadi solusi alternatif. (6) Default awal: Mode Cepat, sisi Atas. (7) Penamaan & urutan drawer ditunda (rencana ubah drawer). (8) Aksesibilitas ditunda. (9) Hal yang bisa disamakan dengan modul lain → terapkan pola yang sama.
- **[2026-08-04] Checkbox Kunci Posisi** — Tetap tampil di panel (pola modul lain) tapi **disabled** (abu-abu, tidak bisa diakses) dengan label `[✓] Kunci Posisi (tidak tersedia)`. Label "Kunci Posisi" saat ini hardcoded di layout modul lain, bukan string resource.
- **[2026-08-04] Data tidak tersedia** — Diputuskan **Opsi A**: bar tampil strip kosong penuh (tanpa isi), pulih otomatis. Ide lain (teks merah "Invalid⚠️" berulang sepanjang bar) dipertimbangkan tapi ditolak — tidak praktis dan bisa terlihat seperti dekorasi.
- **[2026-08-04] Animasi charging** — Flash putih bergerak kiri → kanan (efek shine) di atas bar saat mengisi daya. Teknik ValueAnimator + invalidate per frame; berhenti saat charging berhenti.
- **[2026-08-04] Baterai rendah** — Animasi **fade pelan** (memudar transparan berulang, bukan berkedip tegas) saat level **< ambang**. Ambang default **40%** diatur slider; warna bar saat rendah dipilih via color picker; interval/kecepatan fade bisa disesuaikan. Aktif hanya saat tidak mengisi daya.
