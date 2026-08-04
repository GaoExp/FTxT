# Bug — Refactor Panel Navigation ke Fragment

Analisa setelah refactor v4.84.0 (`0c25c15`). Banyak bug muncul karena perubahan sistem navigasi panel dari **View visibility manual (sinkron, deterministik)** ke **Fragment hide/show (async, bergantung FragmentManager)**.

Dasar analisa: `MainActivity.java`, `PanelManager.java`, `BasePanelFragment.java`, semua `*PanelFragment`, semua `*PanelController`, semua `*PositionController`, `FloatingService.java`, diff commit v4.84.0, dan versi lama v4.83.2 (`80a05b7`).

---

## Status Perbaikan (v4.84.1 — menunggu konfirmasi user)

> ⚠️ Status "SELESAI" dari agent sebelumnya **belum dikonfirmasi** user. Analisa ulang kode 2026-08-04: item 1, 2, 4, 7 kode sudah ada tapi **belum diuji manual**; item 12 **belum sepenuhnya clear** (ColorPicker masih tidak bisa muat preset). Semua item yang diklaim selesai diubah ke **❄️ MENUNGGU KONFIRMASI** sampai lolos uji manual.

| # | Item | Status |
|---|------|--------|
| 1 | Crosshair/Logo tidak ditangani | ✅ SELESAI (dikonfirmasi user 2026-08-04) |
| 2 | Layar kosong saat start | ✅ SELESAI (dikonfirmasi user 2026-08-04) |
| 3 | Battery Percentage sidebar | 📍 pre-existing, belum |
| 4 | Panel bertumpuk (race condition) | ✅ SELESAI (dikonfirmasi user 2026-08-04) |
| 5 | IllegalStateException commit | ❄️ tidak muncul, `commit()` dipertahankan |
| 6 | cleanup saat pindah panel | ❄️ didesain tetap (hide/show, tidak di-remove) |
| 7 | onPanelShown tidak dipanggil | ✅ SELESAI (dikonfirmasi user 2026-08-04) |
| 8 | Fragment menumpuk setelah recreate | ❄️ sengaja dibiarkan (perilaku setara v4.83.2) |
| 9 | Konstruktor lama NPE-prone | ✅ SELESAI (dikonfirmasi user 2026-08-04) |
| 10 | Import CheckBox ganda | ✅ SELESAI (dikonfirmasi user 2026-08-04) |
| 11 | activePresetLabel | ℹ️ TIDAK PERLU DIPERBAIKI (pre-existing, bukan bug refactor, user putuskan tidak perlu) |
| 12 | showLoadPresetDialog tidak di-override | ✅ DIPUTUSKAN TIDAK PERLU untuk ColorPicker (7 lainnya sudah override, dikonfirmasi user 2026-08-04) |

---

## A. Regresi Navigasi (bug fungsional yang pasti terlihat)

### ✅ SELESAI 1. `navCrosshair` & `navLogo` tidak ditangani lagi (dikonfirmasi user 2026-08-04)
- **File:** `app/src/main/java/exp/ftxt/MainActivity.java:665-675` (`panelIdToName`)
- **Masalah:** Mengembalikan `null` untuk `navCrosshair` dan `navLogo`. Sebelumnya (v4.83.2) klik kedua item menampilkan panel placeholder (`panelCrosshair` / `panelLogo` `setVisibility(VISIBLE)`).
- **Efek:** Klik "Crosshair (coming soon)" / "Logo Display (coming soon)" di sidebar tidak melakukan apa-apa (panel tidak tampil, title tidak berubah).
- **Hasil verifikasi kode (2026-08-04):** `panelIdToName()` sudah memetakan `navCrosshair`→`crosshair`, `navLogo`→`logo`; `PanelManager` punya `crosshair`→`CrosshairPanelFragment`, `logo`→`LogoPanelFragment`; kedua fragment set `view.setVisibility(VISIBLE)` (layout root `android:visibility="gone"` diatasi); `updateActionBarTitle()` punya judul untuk keduanya. **Kode OK — tinggal uji manual.**

### ✅ SELESAI 2. Layar kosong saat start app (paling parah) (dikonfirmasi user 2026-08-04)
- **File:** `app/src/main/java/exp/ftxt/MainActivity.java:98-103`
- **Masalah:** Jika `nav_selected_item` tersimpan = `navCrosshair`/`navLogo` (hasil klik di versi lama, disimpan tanpa filter di `SidebarAdapter` baris 621-622), maka `panelIdToName` → `null` → `showPanel()` tidak pernah dipanggil → `panel_container` kosong.
- **Perbandingan:** Versi lama tetap menampilkan panel Text sebagai default saat nilai tidak dikenali.
- **Efek:** App tampak "mati" / layar konten kosong setelah update.
- **Hasil verifikasi kode (2026-08-04):** `onCreate` fallback ke panel `text` + `R.id.navFloatingText` saat `panelIdToName()` mengembalikan `null` (baris 100-103). Semua nama hasil `panelIdToName()` (text, fps, clock, battery, battery_cur, network, color_picker, crosshair, logo) ada di `panelMap`. **Kode OK — tinggal uji manual. Catatan: `battery_pct` ada di `panelMap` tapi tidak ada di `panelIdToName` (ini item #3, pre-existing).**
- **⚠️ Temuan terkait saat uji manual (2026-08-04) — SUDAH DIPERBAIKI & DIKONFIRMASI SELESAI:** Saat memilih modul di sidebar, panel tampil **kosong**. Penyebab: **semua layout panel kecuali `panel_text.xml` masih punya `android:visibility="gone"` di root** (sisa era View-visibility manual, `hideAllPanels()`/`setVisibility(VISIBLE)` yang dulu menghapusnya sudah dibuang saat refactor v4.84.0). Hanya `panel_crosshair`/`panel_logo` yang tampil karena fragment-nya set `view.setVisibility(VISIBLE)`. Perbaikan: `android:visibility="gone"` dihapus dari root 9 layout panel (battery, battery_cur, battery_pct, clock, color_picker, crosshair, fps, logo, network); visibilitas kini murni dikelola FragmentTransaction. **Terverifikasi 2026-08-04: fallback `onCreate` (MainActivity.java:100-103) ke panel `text` saat `nav_selected_item` tidak dikenal + semua nama ada di `panelMap` (PanelManager.java:33-43) + sisa `gone` hanya di elemen dalam section, bukan root — dikonfirmasi selesai oleh user.**

### 📍 BELUM 3. Battery Percentage tidak bisa diakses dari sidebar
- **File:** `app/src/main/java/exp/ftxt/ui/PanelManager.java:35`
- **Masalah:** Panel `battery_pct` terdaftar di `PanelManager` tapi tidak ada `navBatteryPercentage` di `res/values/ids.xml` maupun `DEFAULT_SIDEBAR_JSON`, dan `panelIdToName` tidak memetakannya.
- **Catatan:** Pre-existing (bukan dari refactor), tapi jadi lebih terlihat.

---

## B. Race Condition Fragment (bug intermiten)

### ✅ SELESAI 4. Panel bertumpuk saat ganti panel cepat (dikonfirmasi user 2026-08-04)
- **File:** `app/src/main/java/exp/ftxt/ui/PanelManager.java:41-69`
- **Masalah:** `ft.commit()` bersifat async. Loop `fragmentManager.getFragments()` diambil **saat `beginTransaction()`** (snapshot).
  - Klik "FPS" → transaksi A (`add fps`) di-commit tapi belum dieksekusi.
  - Klik cepat "Clock" sebelum A dijalankan → transaksi B dibuat; saat itu `getFragments()` belum berisi `fps` → B tidak sempat `hide(fps)`.
  - Hasil akhir: `fps` dan `clock` tampil **bertumpuk** sampai user klik panel lain.
- **Efek:** Tampilan acak/berantakan yang tidak terjadi di versi View-based.
- **Kekurangan lain:** Tidak ada `setReorderingAllowed(true)`, tidak serialisasi transaksi.
- **Hasil verifikasi kode (2026-08-04):** `showPanel()` kini memanggil `executePendingTransactions()` sebelum loop, `setReorderingAllowed(true)`, dan hide hanya fragment `isAdded()` & `!isHidden()` (baris 49-58). Race utama sudah ditangani. **Dikonfirmasi selesai oleh user 2026-08-04.** Catatan: transaksi tetap `ft.commit()` (bukan `commitAllowingStateLoss`) → potensi crash terkait #5 yang belum ditangani.

### ❄️ TIDAK MUNCUL 5. Potensi `IllegalStateException` terkait lifecycle state
- **File:** `app/src/main/java/exp/ftxt/ui/PanelManager.java:67` (`ft.commit()`)
- **Masalah:** Tidak ada `commitAllowingStateLoss`. Jika `showPanel` dipanggil dari konteks setelah `onSaveInstanceState` (callback async, onResume, dll) berpotensi crash.

---

## C. Lifecycle & State Fragment Tidak Dikelola

### ℹ️ KEPUTUSAN 6. `cleanup()` tidak pernah dipanggil saat pindah panel
- **File:** semua `*PanelFragment.onDestroyView` (mis. `TextPanelFragment.java:34-39`)
- **Masalah:** `ft.hide()` TIDAK memicu `onDestroyView` → `controller.cleanup()` hanya berjalan saat Activity mati.
- **Efek:**
  - Semua `DpadController` (timer repeat), listener, dan callback `*Module.onPositionUpdate` dari panel tersembunyi tetap hidup selama Activity hidup (boros, state tidak terkendali).
  - Saat `recreate()` (toggle tema / orientasi), **semua fragment yang pernah dibuka di-restore sekaligus** dan `onViewCreated` dijalankan untuk semua (termasuk yang hidden) → controller menumpuk.

### ✅ SELESAI 7. `onPanelShown()` tidak dipanggil setelah `showPanel()` (dikonfirmasi user 2026-08-04)
- **File:** `app/src/main/java/exp/ftxt/MainActivity.java:148-156` + `PanelManager.java:79-84`
- **Masalah:** `onPanelShown()` hanya dipanggil saat `onResume`, bukan saat pindah panel via sidebar.
- **Efek:** UI panel yang baru dibuka tidak di-refresh (`positionController.refresh()` tidak dijalankan).
- **Hasil verifikasi kode (2026-08-04):** `PanelManager.showPanel()` memanggil `onPanelShown()` via `ft.runOnCommit` (baris 76-81); `MainActivity.onResume` → `panelManager.onPanelShown()` (baris 152-154); semua 8 fragment panel meng-override `onPanelShown()` → delegasi ke controller. **Kode OK — tinggal uji manual.** (Crosshair/Logo placeholder tidak perlu controller.)

### ℹ️ KEPUTUSAN 8. Fragment tidak pernah di-remove → menumpuk setelah recreate
- **File:** `app/src/main/java/exp/ftxt/ui/PanelManager.java:41-69` (tidak ada remove / backstack)
- **Masalah:** Semua fragment yang pernah dibuka disimpan dalam state FragmentManager dan direstore setiap kali Activity di-recreate, tanpa pernah di-remove. View untuk fragment hidden tetap dibuat (boros memori) selama app hidup.

### ✅ KEPUTUSAN 12. `showLoadPresetDialog()` & `onPanelShown()` tidak di-override fragment (dikonfirmasi user 2026-08-04)
- **File:** `BasePanelFragment.java:27-29` + semua `*PanelFragment`
- **Masalah:** Method default di `BasePanelFragment` kosong dan **tidak ada satu pun fragment yang meng-override** kedua method ini. Akibatnya `PanelManager.showLoadPresetDialog()` → method kosong → dialog "Muat Preset" dari menu gear **tidak pernah muncul**; dan `PanelManager.onPanelShown()` → method kosong → controller `onPanelShown()`/`refresh()` tidak pernah dipanggil.
- **Perbaikan (v4.84.1):** semua fragment panel meng-override kedua method → delegasi ke controller. PanelManager memanggil `onPanelShown()` via `runOnCommit` setelah panel ditampilkan.
- **Hasil verifikasi kode (2026-08-04):**
  - 7 fragment (Text, Fps, Clock, Battery, Battery%, BatteryCurrent, Network) sudah meng-override **kedua** method → OK. `onPanelShown()` juga sudah terverifikasi lengkap (lihat item 7).
  - **`ColorPickerPanelFragment` HANYA meng-override `onPanelShown()`, TIDAK `showLoadPresetDialog()`.** `ColorPickerPanelController` juga tidak punya method `showLoadPresetDialog()`.
  - **KEPUTUSAN (dikonfirmasi user 2026-08-04): ColorPicker TIDAK PERLU `showLoadPresetDialog()`** — Color Picker adalah panel tool mandiri tanpa overlay (tidak punya PositionController / PresetHandler / config overlay yang disimpan sebagai preset). Preset hanya relevan untuk modul yang punya overlay. Pre-existing sejak v4.83.2 (showSettingsPopup juga tidak menangani color picker) → bukan bug/regresi.
  - CHANGELOG v4.84.1 menulis `ColorPickerPanelFragment — Override onPanelShown() & showLoadPresetDialog()` → **klaim SALAH/overclaim, SUDAH DIKOREKSI 2026-08-04** (hanya `onPanelShown()`).
  - Crosshair/Logo: placeholder tanpa controller → tidak perlu override (wajar).

---

## D. Dead Code Berbahaya

### ✅ SELESAI 9. Konstruktor lama `(Activity)` semua controller kini NPE-prone (dikonfirmasi user 2026-08-04)
- **File contoh:** `TextPositionController.java:113-143`, `FpsPositionController.java:119-149`, `ClockPositionController.java:114-144`, dst. (semua PositionController + PanelController)
- **Masalah:** Konstruktor lama memakai `bindViews()` → `activity.findViewById(android.R.id.content)` (mis. `TextPositionController.java:177-179`). Panel tidak lagi berada di content root (ada di fragment container) → `findViewById` mengembalikan `null` → NPE di `loadConfig`/`setupListeners` jika terpanggil.
- **Status:** Saat ini dead code (fragment selalu pakai konstruktor `(Activity, View)`), tapi rawan dipanggil ulang.

---

## E. Minor

### ✅ SELESAI 10. Import `CheckBox` ganda (dikonfirmasi user 2026-08-04)
- **File:** `FpsPanelController.java:14`, `BatteryPercentagePanelController.java:9`

### ℹ️ TIDAK PERLU 11. `activePresetLabel` tidak pernah di-bind (user putuskan tidak perlu diperbaiki)
- **File:** semua `*PositionController` (field dideklarasikan tapi tidak di-bind di `bindViews`)
- **Efek:** Label "Aktif: [nama preset]" tidak pernah muncul (pre-existing, bukan bug refactor).

---

## Rekomendasi Perbaikan (prioritas)

1. 📍 **Fix navigasi** (kode ada di v4.84.1, belum diuji) — `panelIdToName`: tambah fallback default di `onCreate` (selalu tampilkan minimal 1 panel), buat fragment placeholder untuk Crosshair/Logo, atau hapus item dari sidebar.
2. 📍 **Fix race condition** `PanelManager.showPanel()` (kode ada di v4.84.1, belum diuji) — pakai `ft.setReorderingAllowed(true)`, panggil `fragmentManager.executePendingTransactions()` sebelum loop hide, atau pola single-fragment `replace()` + guard state.
3. 📍 **Kelola lifecycle** (kode ada di v4.84.1, belum diuji) — panggil `onPanelShown()` pada fragment yang ditampilkan saat `showPanel`; pastikan cleanup benar di `onDestroyView`; pertimbangkan remove/backstack agar fragment tidak menumpuk setelah recreate.
4. **Selesaikan item 12 untuk ColorPicker** — tambahkan `showLoadPresetDialog()` di `ColorPickerPanelController` + override di `ColorPickerPanelFragment` (atau putuskan color picker memang tidak perlu preset, lalu koreksi klaim CHANGELOG v4.84.1).
5. **Hapus / isolasi konstruktor lama** `(Activity)` agar tidak terpanggil ulang.
