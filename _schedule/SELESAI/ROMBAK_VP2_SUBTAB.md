# Rombak Sub-Tab Monitor ke ViewPager2 (anti-swipe-lengket)

> Dokumen kerja — status pengerjaan rombak sub-tab Info/Live/Health di tab Monitor.
> Dibuat: 2608302138 (WITA). **Diperbarui & SELESAI: 2608302148 (WITA).**
> Berlaku FTxT versi berjalan `***ONGOING***` (`# [4.89.0] 2608302148 210`).

---

## 1. Tujuan

Tiga sub-tab Monitor (Info / Live / Health) sebelumnya numpang satu NestedScrollView
`batSubScroll` di `panel_battery.xml`, dan perpindahannya memakai swipe manual di
`BatteryMonitorTabController` (VelocityTracker + translate panel) yang **kadang lengket**.
Rombak jadi **tiga layout terpisah** yang dibungkus `ViewPager2`, supaya swipe antar
sub-tab ditangani bawaan pager (anti-lengket).

---

## 2. Desain Baru (kondisi akhir)

- `panel_battery.xml`: area sub-tab (3 NestedScrollView lama) diganti satu
  `<androidx.viewpager2.widget.ViewPager2 android:id="@+id/batSubPager" .../>`.
  Header label sub-tab (`batSubTabInfo/Live/Health`) tetap di panel_battery.
- 3 layout halaman baru, masing-masing ber-root `NestedScrollView` (biar scroll vertikal
  tiap halaman tetap jalan):
  - `panel_bat_sub_info.xml` → root `batSubInfoScroll` (+ `batSubInfoPanel`).
  - `panel_bat_sub_live.xml` → root `batSubLiveScroll` (+ `batSubLivePanel`).
  - `panel_bat_sub_health.xml` → root `batSubHealthScroll` (+ `batSubHealthPanel`).
- `BatteryMonitorTabController`:
  - Simpan referensi 3 page view hasil inflate adapter.
  - Bind semua view & controller turunan DARI page view masing-masing (bukan `rootView`),
    karena id-nya sudah pindah keluar dari tree `panel_battery`.
  - Hapus total mekanisme swipe manual (`setupSubTabSwipe`, `finishSwipe`,
    `selectSubTabFromTap`, `activeSubPanel`, constant `SWIPE_HORIZONTAL_RATIO`,
    `SUBTAB_ANIM_MS`, field `swipeVelocityTracker`/`swipeDownX/Y`/`swipeLastDeltaX`/
    `swipeHorizontalActive`/`subTabSwitching`).
  - Pindah/refresh sub-tab lewat `pager.setCurrentItem()` + `pager.registerOnPageChangeCallback`
    (`onPageSelected` → highlight label + panggil `live.onPanelShown/Hidden` + `history.refresh()`).
- Adapter `ViewPager2` (inner class di `BatteryMonitorTabController`, atau class terpisah):
  `onCreateViewHolder` meng-inflate tiga layout, `onBind` nothing; page view di-ekspos.

---

## 3. Pemetaan id → halaman (penting untuk bind)

| Halaman | Isi id (dipakai controller) |
|---|---|
| **Info** | `batMonitorRing`, `batMonitorMetricsText1/2`, `batMonitorConditionBadge` (tab controller); `batChartPercentView`, `batChartTempView`, `batChartPowerView`, `batChartVoltageView`, `batChartCurrentView`, `batTimelineBarView`, `batChartRangeSeek`, `batChartRangeLabel`, `batChartTick0..9` (charts) |
| **Live** | `batLiveBadge`, `batLiveStatusText`, `batLiveStatusSub`, `batLiveStatusDetail`, `batLiveRing`, `batLiveEstSection`, `batLiveEstOn`, `batLiveEstOff`, `batLiveEstBoth`, `batLiveContent` (live) |
| **Health** | `batHealthText`, `batHealthDesignText`, `batHealthSessionBadge`, `batHealthInfoButton`, `batHealthResetButton` (health); `batHistInfoButton`, `batHistPeriodDaily/Weekly/Monthly`, `batSessionBarChart`, `batHistRangeText`, `batHistSummaryText`, `batHistFilterAll/Charge/Discharge`, `batSessionList` (history); `batMonitorCopyButton`, `batMonitorExportButton` (tab controller `applySnapshotButtonsLock`) |

Perhatikan: `batMonitorCopy/ExportButton` TIDAK lagi di panel_battery (pindah ke Health page).

---

## 4. Yang SUDAH dikerjakan

- [x] Tambah dependensi `androidx.viewpager2:viewpager2:1.1.0` di `app/build.gradle`.
- [x] Ekstrak 3 layout terpisah (`panel_bat_sub_info/live/health.xml`), root NestedScrollView
      ber-id `batSubInfoScroll`/`batSubLiveScroll`/`batSubHealthScroll`.
- [x] Verifikasi keseimbangan tag ketiga file (LinearLayout/FrameLayout/NestedScrollView seimbang).
- [x] Ganti area sub-tab di `panel_battery.xml` menjadi
      `<androidx.viewpager2.widget.ViewPager2 android:id="@+id/batSubPager"/>`.
      `panel_battery.xml` 1496 baris; tag seimbang (LinearLayout 48/48,
      NestedScrollView 2/2 untuk Overlay & Battery Strip, ViewPager2 1).
- [x] `BatteryHealthCardController` & `BatteryChartHistoryController`: konstruktor terima
      `pageView`; bind dari page (tambahan: klik grafik Persentase dipulihkan di charts controller).
- [x] `BatterySessionLiveController` & `BatterySessionHistoryController`: konstruktor terima
      `pageView` (Live / Health page).
- [x] `BatteryMonitorTabController`: rombak penuh — hapus swipe manual (VelocityTracker,
      `setupSubTabSwipe`/`finishSwipe`/`selectSubTabFromTap`/`activeSubPanel`/`showSubPanel`,
      flag `subTabSwitching`, constant `SWIPE_HORIZONTAL_RATIO`/`SUBTAB_ANIM_MS`), tambah
      inner adapter ViewPager2 (`SubTabAdapter` — `onCreateViewHolder` inflate 3 layout,
      simpan ke `pages[]`, ekspos `getPage(pos)`), bind view & controller turunan dari
      page masing-masing di `bindPanels()`, pindah sub-tab via `setCurrentItem` +
      `registerOnPageChangeCallback` (`onPageSelected` → sync highlight label + `live`/`history`),
      null-guard untuk `onPanelShown`/`onPanelHidden`/`cleanup`/`updateMonitorInfo`
      (controller turunan baru siap setelah `bindPanels`). `setOffscreenPageLimit(2)` memastikan
      ketiga page ter-inflate sehingga `bindPanels`/`setupSubTabs` dijalankan di dalam
      `batSubPager.post()` aman mengambil page view.
- [x] CHANGELOG versi berjalan: poin sub-tab diperbarui ke ViewPager2, `✏️ File Changed`
      untuk semua file yang diubah, `🗒️ File Added` untuk 3 layout, judul & versionCode
      naik (209→210).

## 5. Langkah verifikasi (belum sempat dijalankan — butuh build)

Kode sudah SELESAI; verifikasi aktual masih menunggu build (dilarang oleh AGENTS.md).
Langkah yang disarankan untuk agent/developer berikutnya:

- [ ] Build/lint untuk memastikan kompilasi hijau (ada perubahan besar di `BatteryMonitorTabController`
      & keempat controller turunan).
- [ ] Jalankan manual: buka tab Monitor → ketuk & geser ketiga sub-tab; pastikan:
      - geser horizontal anti-lengket & beranimasi bawaan pager,
      - scroll vertikal tiap sub-tab independen,
      - highlight label sinkron dengan halaman aktif,
      - "Sesi Berjalan" (Live) & "Kondisi & Riwayat" (Health) ter-refresh saat halaman aktif,
      - tombol Salin/Simpan Snapshot (kini di Health page) tetap berfungsi & terkunci saat
        Fitur Developer tertutup,
      - ring/metrik/badge Info tampil & ter-polling tiap detik.
- [ ] Jika build menunjukkan error compile, perbaiki sesuai pola di atas (bind dari page view,
      pastikan `getPage()` terisi sebelum dipakai — via offscreen limit + `post`).

## 6. Rangkuman keputusan implementasi (untuk agent lain)

- Hidupkan 3 halaman via `SubTabAdapter` (ViewPager2) di dalam `BatteryMonitorTabController`.
- Semua `findViewById` yang dulunya `rootView` kini memakai page view dari `bindPanels()`:
  - Info page → charts + health + field ring/metrics/badge,
  - Live page → live,
  - Health page → history + tombol export/copy.
- Alur konstruktor: `bindViews(pager+label)` → `setOffscreenPageLimit(2)` → `setAdapter` →
  `post { bindPanels(); setupSubTabs(); }` (menunggu page ter-inflate).
- JANGAN pakai `rootView.findViewById` untuk id di layout page terpisah — akan null (page
  ter-inflate lazy oleh RecyclerView). Selalu lewat page view dari adapter.

