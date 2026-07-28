# Refactor FloatingService — Langkah 3: Eksekusi

## Checklist Tahapan

### 3.1 Tambah Import
- [ ] Tambah `import java.util.ArrayList;`
- [ ] Tambah `import java.util.List;`

### 3.2 Tambah Field `allModules`
- [ ] Tambah `private final List<OverlayModule> allModules = new ArrayList<>();` di bawah field modul

### 3.3 Register Module di `onCreate()`
- [ ] Setelah instansiasi semua modul, masukkan ke `allModules` (textModule, fpsModule, clockModule, batteryModule, batteryPercentageModule, batteryCurrentModule, networkModule)
- [ ] Loop `init()` untuk semua modul: `for (OverlayModule m : allModules) m.init(windowManager, this, prefs);`
- [ ] Loop start module yang enabled: `for (OverlayModule m : allModules) { if (isEnabled(m)) m.start(...); }`
- [ ] Pastikan TextModule tetap pakai `createOverlay()` (bukan `start()`) karena behavior berbeda
- [ ] Pertahankan logic start Clock, Battery, BatteryPercentage, BatteryCurrent, Network, FPS yang sudah ada (jangan dihapus dulu, pastikan loop benar)

### 3.4 Hapus Static Delegates Lama
- [ ] Hapus semua method `updateTextStatic()`, `updateTextSizeStatic()`, `updateTextColorStatic()`, `updateTouchFlagsStatic()`, `updateTextPositionStatic()`, `setTextOrientationSuffixStatic()`, `getTextCurrentPosition()`, `updateShadowStatic()`, `updateTextBackgroundStatic()`, `updateTextPatternStatic()`
- [ ] Hapus semua method FPS: `startFpsStatic()`, `stopFpsStatic()`, `updateFpsColorStatic()`, `updateFpsLabelColorStatic()`, `updateFpsSizeStatic()`, `updateFpsShadowStatic()`, `updateFpsBackgroundStatic()`, `updateFpsTouchFlagsStatic()`, `updateFpsDisplayStatic()`, `updateFpsPositionStatic()`, `setFpsOrientationSuffixStatic()`, `getFpsCurrentPosition()`, `updateFpsUpdateIntervalStatic()`
- [ ] Hapus semua method Clock: `startClockStatic()`, `stopClockStatic()`, `updateClockColorStatic()`, `updateClockSizeStatic()`, `updateClockShadowStatic()`, `updateClockBackgroundStatic()`, `updateClockTouchFlagsStatic()`, `updateClockPositionStatic()`, `setClockOrientationSuffixStatic()`, `getClockCurrentPosition()`
- [ ] Hapus semua method Network: `startNetworkStatic()`, `stopNetworkStatic()`, `updateNetworkSizeStatic()`, `updateNetworkColorStatic()`, `updateNetworkLabelColorStatic()`, `updateNetworkShadowStatic()`, `updateNetworkBackgroundStatic()`, `updateNetworkTouchFlagsStatic()`, `updateNetworkPositionStatic()`, `setNetworkOrientationSuffixStatic()`, `getNetworkCurrentPosition()`, `updateNetworkUpdateIntervalStatic()`
- [ ] Hapus semua method BatteryPercentage: `startBatteryPercentageStatic()`, `stopBatteryPercentageStatic()`, `updateBatteryPercentageColorStatic()`, `updateBatteryPercentageLabelColorStatic()`, `updateBatteryPercentageSizeStatic()`, `updateBatteryPercentageShadowStatic()`, `updateBatteryPercentageBackgroundStatic()`, `updateBatteryPercentageTouchFlagsStatic()`, `updateBatteryPercentagePositionStatic()`, `setBatteryPercentageOrientationSuffixStatic()`, `getBatteryPercentageCurrentPosition()`
- [ ] Hapus semua method BatteryCurrent: `startBatteryCurrentStatic()`, `stopBatteryCurrentStatic()`, `updateBatteryCurrentColorStatic()`, `updateBatteryCurrentLabelColorStatic()`, `updateBatteryCurrentSizeStatic()`, `updateBatteryCurrentShadowStatic()`, `updateBatteryCurrentBackgroundStatic()`, `updateBatteryCurrentTouchFlagsStatic()`, `updateBatteryCurrentPositionStatic()`, `setBatteryCurrentOrientationSuffixStatic()`, `getBatteryCurrentCurrentPosition()`, `updateBatteryCurrentUpdateIntervalStatic()`
- [ ] Hapus semua method Battery: `startBatteryStatic()`, `stopBatteryStatic()`, `updateBatteryColorStatic()`, `updateBatteryLabelColorStatic()`, `updateBatterySizeStatic()`, `updateBatteryShadowStatic()`, `updateBatteryBackgroundStatic()`, `updateBatteryTouchFlagsStatic()`, `updateBatteryPositionStatic()`, `setBatteryOrientationSuffixStatic()`, `updateBatteryUpdateIntervalStatic()`, `getBatteryCurrentPosition()`

### 3.5 Tambah Method Generik Baru
- [ ] `public static void updateColorForModule(OverlayModule module, int color)`
- [ ] `public static void updateSizeForModule(OverlayModule module, float size)`
- [ ] `public static void updateLabelColorForModule(OverlayModule module, int color)`
- [ ] `public static void updateShadowForModule(OverlayModule module)`
- [ ] `public static void updateBackgroundForModule(OverlayModule module)`
- [ ] `public static void updatePositionForModule(OverlayModule module)`
- [ ] `public static void updateTouchFlagsForModule(OverlayModule module)`
- [ ] Pertahankan method spesifik: `createTextOverlayStatic()`, `destroyTextOverlayStatic()`, `updateTextPatternStatic()`, `updateTextStatic()`
- [ ] Pertahankan method spesifik BatteryCurrent: `toggleVoltageStatic()`, `toggleCurrentStatic()`, `togglePowerStatic()`

### 3.6 Update Method Existing ke Loop
- [ ] `stopAllModules()` — ganti 7 if-statements jadi loop `allModules`
- [ ] `hideAllOverlays()` — ganti 7 if-statements jadi loop `allModules`
- [ ] `showAllOverlays()` — ganti 7 if-statements jadi loop `allModules`
- [ ] `areAllOverlaysHidden()` — ganti 7 if-statements jadi loop `allModules`
- [ ] `reloadAllPositions()` — ganti 7 if-statements jadi loop `allModules`

### 3.7 Update `onDestroy()` + Fix Referensi
- [ ] Ganti 6 stop calls di `onDestroy()` jadi loop `allModules` (kecuali `textModule.savePosition()` dan `textModule.destroyOverlay()`)
- [ ] Ganti `textModule.isActive()` → `textModule.isRunning()` di `reloadAllPositions()` dan `areAllOverlaysHidden()`

---

## Estimasi
- **Baris dihapus:** ~430 baris
- **Baris ditambah:** ~50 baris
- **Net:** -380 baris
