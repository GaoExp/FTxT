# Rencana Perbaikan: Bug Riwayat Sesi (Charge & Discharge) + Estimasi Kesehatan

> Dokumen rencana pengerjaan saja — **belum ada perubahan kode**.
> Berlaku untuk FTxT versi berjalan (`CHANGELOG` 4.89.0, status `***ONGOING***`).

---

## 1. Latar Belakang

Dari analisis data nyata (`_sample/battery_history (1).db`, 151081 sampel 24/08 16:39 → 29/08 08:34) ditemukan dua gejala yang saling terkait:

1. **Segmen discharge hilang** — segmen 04:17–06:46 (8748 sampel discharge) tidak tersimpan di tabel discharge. Segmen baru 06:46 terbentuk setelah proses restart (START_STICKY).
2. **Sesi charge terpecah** — 1 pengisian tersimpan menjadi 2 sesi charge (`2→71` @ 1654 mAh dan `71→100` @ 761 mAh).
3. **Estimasi kesehatan under-estimate** — kartu Kesehatan memakai 2 sesi charge tersebut (median 1208 mAh, skor 24%), padahal rekonstruksi dari `samples` mentah (tahan mati) memberi ±1450 mAh (skor ±29%).

Ketiganya berpangkal pada **satu akar masalah bersama**: segmen sesi hanya diakumulasi di static memory dan ditulis ke DB hanya pada saat tertentu, sehingga rapuh terhadap proses dibunuh sistem.

---

## 2. Akar Masalah

### 2.1 Pola persist yang rapuh (charge & discharge sama)

Kedua tracker memakai pola yang sama:

| Komponen | Charge (`BatteryCapacityEstimator`) | Discharge (`DischargeTracker`) |
|---|---|---|
| Akumulasi segmen | static fields (`segmentActive`, `accumulatedChargeMah`, `segmentTotalMs`, ...) | static fields (`active`, `useIntegral`, `totalMs`, ...) |
| Satu-satunya jalan tulis DB | `finishSegment()` (di `onSample` saat `!charging`, atau `onMonitoringStopped()`) | `finishSegment()` (saat status berbalik, atau `onMonitoringStopped()`) |
| Dipanggil dari | `BatteryMonitor.tick` → `onSample`, dan `BatteryMonitor.stop()` → `onMonitoringStopped()` | sama |

- `BatteryMonitor.stop()` hanya dipanggil dari `BatteryMonitorService.onDestroy()` (lihat `BatteryMonitorService.java:43-45`).
- **Saat sistem membunuh proses** (kekurangan memori, restart START_STICKY), Android **tidak menjamin** `onDestroy()`/`stop()` dipanggil → `finishSegment()` tak sempat jalan → segmen yang sedang berjalan **hilang dari DB**.
- `samples` mentah **tidak hilang** (di-insert ke DB tiap `tick` → `insertSample`), hanya segmen ringkasan yang hilang. Ini celah yang bisa dimanfaatkan untuk rekonstruksi.

### 2.2 Mengapa sesi charge terpecah

`finishSegment()` charge memotong saat ada sampel `!charging` (lihat `BatteryCapacityEstimator.onSample` baris 75-81). Jika selama pengisian muncul sampel tidak-berstatus-charging (mis. jeda deteksi / persen sempat turun / status berubah sesaat), segmen dipecah tanpa menyatakan bahwa itu sebenarnya pengisian yang sama.

### 2.3 Mengapa estimasi kesehatan under-estimate

`getResult()` (`BatteryCapacityEstimator.getResult`) menghitung median `capacityMah` dari tabel `sessions` (baris 132-144). Tabel itu rapuh terhadap bug §2.1 dan §2.2, sehingga median rendah. Rekonstruksi dari `samples` mentah justru konsisten & akurat.

### 2.4 Alur "sesi berjalan → masuk riwayat" saat ini

Alur yang **sudah ada** untuk memindahkan sesi berjalan ke riwayat:

```
status berubah (charge↔discharge)  ──┐
                                      ├─► finishSegment() ──► INSERT ke tabel
BatteryMonitor.stop()                │      (discharge / sessions)
  └─► onMonitoringStopped()          │
       (dari BatteryMonitorService   ┘
        .onDestroy)
```

- **Stop normal** (lewat UI / service `onDestroy`): `stop()` → `onMonitoringStopped()` → `finishSegment()` → **sesi masuk riwayat**. ✔
- **Status berubah saat runtime** (mis. lepas dari charger): sampel berikutnya ber-status `!charging` → `finishSegment()` → **sesi masuk riwayat**. ✔
- **Proses dibunuh sistem** (memori rendah / restart START_STICKY): Android **tidak menjamin** memanggil `onDestroy()`/`stop()`; tidak ada `BroadcastReceiver`, `onTrimMemory`, atau checkpoint → `finishSegment()` **tidak pernah jalan** → sesi berjalan **tidak masuk riwayat**. ✘

**Kesimpulan:** "stop → masuk riwayat" hanya benar untuk stop yang **disengaja**. Untuk kematian proses yang tak disengaja, sesi lenyap. Ini satu-satunya celah yang membuat segmen bisa hilang, dan justru seluruh masalah (discharge hilang, charge terpecah, estimasi rendah) bersumber dari sini.

### 2.5 Titik-titik yang WAJIB dipertahankan (regresi)

Perbaikan persist TIDAK boleh menghapus perilaku berikut — ini menjadi acuan self-check saat eksekusi:

1. **Stop normal tetap masuk riwayat** — saat user mematikan monitor, sesi berjalan tetap harus tersimpan lengkap.
2. **Status berubah tetap memecah/merapikan benar** — saat lepas charger / sambung charger, batas segmen benar (mulai = % awal, akhir = % akhir), bukan terpotong di tengah.
3. **Tidak dobel** — segmen hasil rekonstruksi/penyelamatan tidak boleh tercatat dua kali (duplikat dengan segmen yang sudah tersimpan).
4. **Sesi charge yang sama tidak bias disalah-pecah** — pengisian berkelanjutan muncul sebagai satu sesi (lihat §2.2).
5. **Estimasi kesehatan stabil** — hasil skor kartu Kesehatan tidak berubah liar akibat berubahnya metode, hanya menjadi lebih akurat.

---

## 3. Solusi yang Direncanakan

### A. Perbaiki persist segmen (charge & discharge) — pembenahan sisi-tulis

Tujuan: segmen berjalan **tidak hilang** meski proses dibunuh sistem.

**Prinsip kunci:** `samples` mentah adalah **sumber kebenaran** — selalu tersimpan di DB tiap detik, tidak pernah hilang. Yang hilang hanya *ringkasan segmen* (tabel session). Maka arah perbaikannya adalah **menyelamatkan sesi dari `samples`**, bukan hanya menambal konter statis.

Strategi (menggabungkan dua lapis pertahanan):

1. **Checkpoint berkala ke DB.** Simpan state segmen aktif (start_time, start_percent, start_charge_mah, akumulasi integral, suhu, dsb.) ke lokasi persisten setiap N menit (mis. tiap sampel, atau tiap ±60 detik). Karena menulis SQLite tiap detik itu mahal, gunakan interval rendah (mis. 30–60 detik) atau hanya saat ada perubahan signifikan. Alternatif hemat: `SharedPreferences` untuk snapshot kecil.

2. **Rekonstruksi segmen berjalan saat `BatteryMonitor.start()` — mekanisme utama penyelamatan.** Karena `samples` mentah disimpan lengkap di DB, saat proses hidup kembali: baca sampel terakhir, jika statusnya masih-berlangsung (sedang discharge / sedang charge), rekonstruksi segmen dari `samples` sehingga segmen yang sempat "hilang" tersambung kembali, lalu lanjut akumulasi. Inilah yang benar-benar memastikan sesi tidak hilang bila checkpoint kelewat (proses tiba-tiba mati).

3. **Finalisasi pada titik daya-hidup yang bisa diandalkan.** Tambahkan persist saat `onTrimMemory(TRIM_MEMORY_*)` di service/application dan pastikan `BatteryMonitorService` memanggil `BatteryMonitor.stop()` dengan benar. `Application.onTerminate()` tidak dapat diandalkan, sehingga lapisan #1–#2 adalah andalan utama.

Keputusan desain yang perlu dimatangkan saat eksekusi:
- Lokasi penyimpanan checkpoint: meta table DB (`setMeta`) vs `SharedPreferences` vs tabel baru.
- Interval checkpoint yang seimbang (keakuratan vs beban tulis/CPU).
- Cara memastikan segmen hasil rekonstruksi tidak dobel dengan segmen yang sudah tersimpan (dedup saat rekonstruksi, mis. cek overlap waktu/percent dengan session terakhir di DB).
- Batas rekonstruksi: berapa lama "masih sesi aktif" layak disambung vs dianggap sesi baru (mis. jeda > X menit → sesi baru).
- Pada titik mana rekonstruksi dijalankan (di `BatteryMonitor.start()` sebelum polling jalan) agar state tracker siap sebelum sampel baru masuk.

### B. Perbaiki penggabungan sesi charge yang terpecah

Tujuan: 1 pengisian muncul sebagai 1 sesi di riwayat.

Ide:
- Jangan langsung melepas segmen saat satu sampel `!charging` muncul di tengah proses pengisian; beri toleransi jeda pendek (mis. gap < X detik & status kembali charge) agar segmen tetap satu.
- Atau, saat menampilkan riwayat, gabungkan segmen charge yang berdekatan (gap kecil) sebagai satu sesi — **tanpa mengubah data mentah** (bisa dilakukan di lapisan query/tampilan).

### C. Rombak sumber estimasi kesehatan ke `samples` mentah

Tujuan: skor Kesehatan selaras dengan rekonstruksi akurat (±1450 mAh / ±29%), bukan median tabel `sessions` yang rapuh.

Ide:
- Untuk estimasi kapasitas, gunakan rekonstruksi dari `samples` (konsisten dengan `BatterySessionLiveController` / simulasi): integrasi mAh antara dua titik %, bagi selisih % → kapasitas penuh.
- Tidak mengubah data riwayat sesi yang tampil; hanya sumber perhitungan skor kartu Kesehatan yang dialihkan.
- Pertimbangkan agar hasil tetap stabil (median dari banyak segmen valid, filter segmen pendek/ekstrem).

---

## 4. File yang Terlibat

| File | Peran |
|---|---|
| `app/src/main/java/exp/ftxt/features/battery_stats/BatteryCapacityEstimator.java` | Tracker charge; hapus persisten mem-pandemic, tambah checkpoint + rekonstruksi; bakal jadi dasar estimator baru |
| `app/src/main/java/exp/ftxt/features/battery_stats/DischargeTracker.java` | Tracker discharge; tambah checkpoint + rekonstruksi segmen |
| `app/src/main/java/exp/ftxt/features/battery_stats/BatteryHistoryDb.java` | Menyediakan `samples` mentah (`querySamples`/`queryLastSamples`) untuk rekonstruksi; mungkin dukung meta checkpoint |
| `app/src/main/java/exp/ftxt/features/battery_stats/BatteryMonitor.java` | Titik hidup `start`/`stop`; lokasi panggil rekonstruksi & finalisasi |
| `app/src/main/java/exp/ftxt/core/BatteryMonitorService.java` | Menyediakan sinyal `onTrimMemory`; pastikan `stop()` benar saat service mati |
| `app/src/main/java/exp/ftxt/ui/BatterySessionLiveController.java` | (Referensi) logika rekonstruksi segmen dari `samples` yang sudah terbukti |

---

## 5. Langkah Eksekusi (berurutan, tunggu konfirmasi user)

1. **Confirmasi desain** — putuskan lokasi checkpoint & interval; apakah pemecahan charge digabung di lapisan data atau tampilan; apakah estimator baru menggantikan/menambah `getResult()`.
2. Implementasi **A (persist/rekonstruksi)** → ubah `DischargeTracker` & `BatteryCapacityEstimator`.
3. Implementasi **B (penggabungan sesi charge)**.
4. Implementasi **C (estimator dari samples)** → ubah `BatteryCapacityEstimator.getResult`/kartu Kesehatan.
5. Verifikasi dengan analisis `_sample` DB (simulasi skenario proses-mati) sebelum build — uji terhadap semua titik di §2.5:
   - meniru proses mati di tengah discharge → setelah "restart", sesi berjalan tersambung/tersimpan, tidak hilang;
   - meniru proses mati di tengah charge → pengisian tidak terpecah jadi beberapa sesi;
   - memastikan tidak ada dobel segmen & estimasi tidak melebar liar.
6. Catat CHANGELOG sesuai §2.4 (satu poin hasil akhir per iterasi, singkat) + naikkan versionCode +1 tiap entry diperbarui.

> ⚠️ Sesuai AGENTS §1/§3/§4: **jangan build, commit, tag, atau push** sebelum perintah eksplisit. Eksekusi menunggu persetujuan desain di langkah 1.

---

## 6. Catatan / Resiko

- `samples` mentah tetap disimpan tiap detik → **sumber kebenaran yang tahan mati**. Selama ini ada, rekonstruksi segmen bisa mengembalikan data yang hampir utuh.
- Risiko dobel-pencatatan saat rekonstruksi (perlu dedup dengan segmen tersimpan). Mesti diverifikasi di langkah 5.
- Perubahan estimator kesehatan mengubah angka yang tampil di kartu — bukan hanya internal. Konfirmasi ke user sebelum rilis.
- `build.gradle`, `app/src/main/assets/AssetsDoc.docx`, dsb. (daftar §2.7) tidak dicatat di CHANGELOG.

---

## 7. Keputusan Desain Final (hasil diskusi, 29/08/2026)

> Lampiran keputusan — disepakati user; menjadi acuan implementasi saat eksekusi.

### 7.1 Sumber kebenaran & otoritas arah sesi

- **`samples` mentah = sumber kebenaran** (`time`, `percent`, `current_ma`, `charge_mah`, `temp_c`, dll). Segmen ringkasan (tabel `sessions`/`discharge_sessions`) hanya turunan dan dapat dibangun ulang.
- **Otoritas penentu arah sesi = pergerakan persen**, bukan `status` Android (terbukti tidak stabil: bisa CHARGING tapi arus ≤0; ada jeda deteksi yang status-nya berbalik ke DISCHARGING padahal charger tetap tersambung).
- **Jeda pengamatan: 10 menit = 1%.** Arah dianggap bergeser jika dalam jendela 10 menit persen berubah ≥1%:
  - naik ≥1% → charging;
  - turun ≥1% → discharge;
  - persen diam → bukan sesi baru (sesi yang berjalan tetap berlanjut).
- Tampilan cepat/di tab "Sesi Berjalan" boleh memakai status/arus sesaat sebagai perantara, tetapi **keabsahan & penutupan sesi hanya oleh aturan persen** di atas.

### 7.2 Sesi charging (berlaku untuk pemecahan yang tadinya "charge terpecah")

- Sesi charging **tidak diputus** oleh persen diam (termasuk melambat di atas 80% yang bisa >10 menit per 1%).
- Periode persen-diam dalam sesi charging dicatat sebagai **daftar interval bypass**: `[mulai lambat, selesai lambat]`. Interval tersebut tetap **terhitung masuk durasi sesi charging** (bukan sesi terpisah).
- Contoh target: 1 pengisian nyata `2→100%` (dengan jeda deteksi 171 detik di titik 71%) harus tersimpan sebagai **1 sesi**, bukan 2 (`2→71`, `71→100`).

### 7.3 Sesi discharge

- Discharge bersifat menurun berkelanjutan; **tidak ada konsep label bypass** di discharge.
- Persen diam sesaat pada discharge = efek pembulatan 1%, **tetap bagian sesi yang sama** (jangan dipotong jadi sesi-sesi kecil).
- Sesi discharge berakhir saat arah **berbalik naik** (mulai charging), atau persen mencapai 0%, atau perangkat mati.

### 7.4 Peran ambang arus (hasil catatan diskusi)

- Ambang arus (`<-150 mA` = discharge, `>+25 mA` = charging, di antaranya = bypass) **TIDAK dipakai untuk memecah sesi**: analisis data membuktikan 9 jeda deteksi palsu dalam pengisian semuanya berarus rata-rata `< -150 mA` (mis. `-829 mA` pada gap 171 detik yang memecah `2→71`/`71→100`), sehingga ambang arus murni tidak bisa membedakan jeda palsu dengan discharge asli.
- Ambang arus tetap dipakai sebagai **label/kategori cepat** (tampilan), mis. menandai periode bypass/top-up saat asupan ~0, dan untuk **filter estimator** (buang segmen mAh ≈ 0).

### 7.5 Estimasi kesehatan (keputusan C)

- Estimasi kapasitas & skor kesehatan dihitung **dari data mentah `samples`** (integrasi mAh antara dua titik %, bagi selisih % → kapasitas penuh), konsisten dengan `BatterySessionLiveController`, bukan median tabel `sessions` yang rapuh.
- Tetap pertahankan filter stabilitas: buang segmen pendek/ekstrem (Δ% < 5 %, durasi < 1 menit) dan agregasi median lintas sesi valid.

### 7.6 Kesepakatan solusi lain

- **A (persist)** disetujui: samples selalu tersimpan → saat proses hidup kembali, segmen revisi direkonstruksi dari `samples` di titik `BatteryMonitor.start()`; checkpoint berkala opsional; perlu dedup agar tidak dobel dengan segmen yang sudah tersimpan.
- Titik-titik regresi `§2.5` tetap berlaku sebagai acuan self-check saat eksekusi.

---

## 8. Redesign Visual Tab "Sesi Berjalan" (disepakati user, 29/08/2026)

> Umpan balik user (29/08): desain visual ala AccuBattery juga diminta — **ikut dikerjakan** pada iterasi eksekusi. Jangan terlewat.

- **Referensi tampilan:** `_sample/contoh_sample_desain_sesi_berjalan` (dua layar: mode discharge & charge ala AccuBattery Pro). Sketsa ASCII turunan sudah ada di `_temp/sketsa_sesi_berjalan.txt` dan dipakai sebagai acuan layout.
- **Komponen yang dikehendaki (mode discharge):**
  - Ring persen besar di tengah atas (mis. `68%`).
  - Baris estimasi bertahan 3 kondisi: layar hidup / layar mati / kombinasi (mis. `🟠 3j 6m`, `🟡 2j 12m`, `🟣 35j 27m`).
  - Kartu "Status pelepasan": masa pakai (W/%jam), penggunaan rata-rata (%/jam), temperatur, tegangan.
  - Kartu "Penggunaan baterai": total vs terpakai sesi ini, lalu layar hidup vs layar mati (mAh).
  - Kartu "Kecepatan penggunaan": kombinasi (%/jam) + arus (mA).
  - Kartu "Rincian sesi": mulai, durasi live, progress bar daya/tegangan/kecepatan/suhu, siklus.
- **Komponen mode charge:** ring %, kapsul status, kartu "Status pengisian" (arus W/mA, tegangan, kecepatan rata-rata, temperatur — semua dengan progress bar), "Perkiraan waktu pengisian" (sisa baterai & waktu hingga 100%), "Telah terisi" (total + sedang diisi), "Kecepatan pengisian" (rata-rata %/jam vs sekarang mA), "Rincian sesi" (mulai, durasi, colokan/layar, siklus).
- **Dikecualikan** (tidak punya datanya): penggunaan baterai per-aplikasi dan breakdown awake/deep sleep (catatan sketsa).
- **Status sub-tab:** tetap di tengah `Info & Grafik | Sesi Berjalan | Kondisi & Riwayat`, dengan selector interval `1s/2s/5s` di baris atas.
- Estimasi "bertahan sampai" per kondisi layar memanfaatkan data laju per kondisi dari `activity_log` (layar nyala/mati) — detail teknis diputuskan saat eksekusi.
