# Refactor Panel Navigation ke Fragment

Berdasarkan: `_schedule/PANEL_NAVIGATION_FRAGMENT.md`

---

## Tahapan Pengerjaan

### Tahap 1: Infrastruktur
- [x] **1.1** Buat `BasePanelFragment.java` — abstract class dengan `getLayoutResId()` + lifecycle
- [x] **1.2** Buat `PanelManager.java` — registrasi 8 panel, show/hide via FragmentTransaction
- [x] **1.3** Update `activity_main.xml` — hapus semua `<include>`, ganti `<FrameLayout android:id="@+id/panel_container"/>`

### Tahap 2: Konversi Panel (Text & FPS)
- [x] **2.1** Buat `TextPanelFragment.java` — bungkus TextPanelController, binding via `view.findViewById()`
- [x] **2.2** Buat `FpsPanelFragment.java` — bungkus FpsPanelController
- [x] **2.3** SidebarAdapter (inner class di MainActivity) — navigation click panggil `panelManager.showPanel()`

### Tahap 3: Konversi Panel (Clock & Battery)
- [x] **3.1** Buat `ClockPanelFragment.java`
- [x] **3.2** Buat `BatteryPanelFragment.java`

### Tahap 4: Konversi Panel (Battery% & Battery Current)
- [x] **4.1** Buat `BatteryPercentagePanelFragment.java`
- [x] **4.2** Buat `BatteryCurrentPanelFragment.java`

### Tahap 5: Konversi Panel (Network & ColorPicker)
- [x] **5.1** Buat `NetworkPanelFragment.java`
- [x] **5.2** Buat `ColorPickerPanelFragment.java`

### Tahap 6: Integrasi MainActivity
- [x] **6.1** Hapus 9 field panel View + `hideAllPanels()` + if-else sidebar
- [x] **6.2** Init `PanelManager` di `onCreate()`
- [x] **6.3** Ganti `showSettingsPopup()` pakai `panelManager.showLoadPresetDialog()`
- [x] **6.4** Bersihkan unused import & method

### Tahap 7: Testing & Finalisasi
- [x] **7.1** Test navigasi semua panel — OK
- [x] **7.2** Test lifecycle — OK
- [x] **7.3** Test preset — OK
- [x] **7.4** Test settings popup — OK

---

## Catatan

- Setelah selesai, update CHANGELOG entry versi berjalan
- Update STRUKTUR.md jika ada file baru
- Hapus file `PANEL_NAVIGATION_FRAGMENT.md` dan ganti dengan `REFACTOR_PANEL_FRAGMENT.md` jika dokumen ini sudah final
