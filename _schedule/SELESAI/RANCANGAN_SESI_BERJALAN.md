# Desain — Tab "Sesi Berjalan" (Live Session)

**Status:** ✅ SELESAI — desain inti sudah diimplementasikan di v4.89.0 (`BatterySessionLiveController`, sub-tab tengah tab Monitor)
**Tanggal:** 2026-08-29
**Catatan:** Redesign visual ala AccuBattery (mengikuti `_sample/contoh_sample_desain_sesi_berjalan`) belum dikerjakan; ide tersebut tersimpan untuk iterasi berikutnya.
**Tujuan:** Menambah sub-tab "Sesi Berjalan" pada tab Monitor (Battery Info) yang menampilkan satu sesi aktif real-time — pengosongan maupun pengisian — yang otomatis berpindah menjadi riwayat saat status berubah.

> Catatan: `DESAIN_RIWAYAT_SESI.md` lama direncanakan 3 sub-tab (Info & Grafik | Kondisi Baterai | Riwayat Sesi) tapi sudah **tidak relevan** — implementasi nyata menggabung "Kondisi Baterai" & "Riwayat Sesi" jadi satu tab "Kondisi & Riwayat" (total 2 sub-tab).

---

## 1. Latar Belakang & Masalah

- Riwayat Sesi saat ini hanya menampilkan sesi yang **sudah selesai/berhenti** (`finishSegment`).
- Sesi yang sedang berjalan **tidak terlihat** sampai ia berhenti.
- Jika proses dibunuh di tengah (bug pembunuhan proses), sesi yang berjalan **hilang** dan tak pernah muncul di riwayat.
- Tidak ada cara melihat kondisi discharge/charge **secara live**.

**Solusi:** sub-tab "Sesi Berjalan" yang membaca langsung dari **data mentah `samples`** (tahan mati/akurat) dan menampilkan satu sesi aktif yang diperbarui berkala.

---

## 2. Konsep Perilaku (disepakati user)

- Sub-tab "Sesi Berjalan" menampilkan **satu sesi aktif**.
- Sesi aktif bisa berupa **pengosongan (discharge)** atau **pengisian (charge)**, otomatis mengikuti status aktif terkini (`status` di `samples`).
- **Saat status berubah** (mis. discharge → charge), sesi yang sedang berjalan **tutup → langsung menjadi riwayat sesi** (masuk daftar Riwayat Sesi), lalu sesi baru mulai di tab "Sesi Berjalan".
- **Sumber data:** rekonstruksi dari `samples` mentah (bukan tabel ringkasan `sessions`/`discharge_sessions` yang bisa terpotong).

---

## 3. Data yang Ditampilkan

Sumber tersedia di `samples` (`BatteryReading.Snapshot`): `time`, `status`, `percent`, `voltage_v`, `current_ma`, `power_w`, `charge_mah`, `temp_c`.

Usulan daftar info yang tampil:

| Info | Discharge | Charge | Sumber |
|---|---|---|---|
| Jenis sesi (badge/warna) | Pengosongan | Pengisian | `status` |
| Waktu mulai aktif | ⏱ | ⏱ | sample pertama segmen |
| Durasi berjalan (live) | naik terus | naik terus | now − start |
| Persen (mulai → sekarang) | 100→68 | 20→45 | `percent` |
| Penurunan/kenaikan % | −32% | +25% | Δ `percent` |
| Arus (live) | −690 mA | +1200 mA | `current_ma` |
| mAh terpakai/terisi | integral | integral | Σ `current`×dt |
| Suhu (min/max/avg) | min/max/avg | min/max/avg | `temp_c` |
| Voltase | V | V | `voltage_v` |

**Catatan pembatas "mulai aktif":** karena sesi discharge bisa bertahan berjam-jam, perlu definisi batas mulai (mis. mulai = sample pertama dari status non-charge setelah status charge terakhir terdeteksi dalam rentang yang masih sama). Ini butuh konfirmasi.

---

## 4. Update Interval (disepakati user)

- Interval pembaruan dapat **dipilih user**: **1 detik / 2 detik / 5 detik**.
- Terapan: selector di dalam tab "Sesi Berjalan" (mis. kapsul 1s / 2s / 5s).
- Semakin cepat interval → data lebih mulus tapi lebih berat (query `samples`).

---

## 5. Struktur Tab (KEPUTUSAN USER)

Mengikuti pola existing (`panel_battery.xml` + `BatteryMonitorTabController`). Sub-tab saat ini **2**: `Info & Grafik` (`batSubTabInfo`) dan `Kondisi & Riwayat` (`batSubTabHealth`), dengan panel `batSubInfoPanel` & `batSubHealthPanel` yang di-toggle via `selectSubTab(index)`/`showSubPanel(index)`.

**Keputusan final:** tambah sub-tab ke-3 **"Sesi Berjalan"** di **TENGAH**:

```
Panel Battery Info
└── Tab Monitor → sub-tab atas:
    ├── Info & Grafik
    ├── Sesi Berjalan      ← BARU (tengah)
    └── Kondisi & Riwayat
```

- Sub-tab baru: `batSubTabLive` (TextView, urutan tengah).
- Panel baru: `batSubLivePanel` (kontainer live).
- `BatteryMonitorTabController`: loop `selectSubTab`/`showSubPanel` jadi 3, tambah controller live + `cleanup()`.

---

## 6. Rencana Teknis (BELUM dieksekusi)

### A. Sumber data
- Query `samples` untuk mendeteksi sesi aktif saat ini (status non-charge atau charge terbaru), rekonstruksi mulai aktif + akumulasi mAh (integral) dari data mentah.

### B. Controller
- `BatterySessionHistoryController` atau controller/layout baru untuk sub-tab "Sesi Berjalan".
- Refresh berkala sesuai interval terpilih; hentikan saat tab tidak aktif.

### C. UI
- Tambah sub-tab "Sesi Berjalan" pada layout (`panel_battery.xml`) + kontainer tampilan live.
- Selector interval 1s/2s/5s.

---

## 7. Pertanyaan Tunda / Keputusan yang Belum Diambil

1. **Lokasi sub-tab** — total 4 sub-tab, atau digabung dengan Riwayat Sesi? → TUNDA
2. **Batas "mulai aktif"** — bagaimana menentukan awal sesi discharge yang sudah lama berjalan. → TUNDA
3. **Daftar info yang ditampilkan** — mana yang penting/dibuang dari tabel §3. → TUNDA (user sedang memetakan)
4. **Tampilan** — kartu besar 1 sesi (grid) vs mirip baris riwayat dengan detail. → TUNDA
5. **Penyimpanan saat berubah ke riwayat** — cukup ditulis otomatis ke tabel sesi seperti sekarang, atau perlu penyesuaian. → TUNDA

---

*Desain inti selesai & terimplementasi di v4.89.0 (status `***ONGOING***` di CHANGELOG).*
