# Refactor FloatingService — Hapus Duplikasi Kode

## Masalah
FloatingService punya 700+ baris kode yang hampir seluruhnya duplikasi. Setiap modul punya ~10 method delegasi statis dengan struktur sama. Total 70+ method yang diulang untuk 7 modul.

## Solusi
Gunakan interface `OverlayModule` agar semua modul implement method yang sama. FloatingService tinggal panggil method dari interface.

---

## Langkah 1: Buat Interface OverlayModule

**File baru:** `app/src/main/java/exp/ftxt/shared/ui/OverlayModule.java`

```java
public interface OverlayModule {
    void start(WindowManager wm, Context ctx);
    void stop();
    boolean isRunning();
    void updateSize(float size);
    void updateColor(int color);
    void updateLabelColor(int color);
    void updateShadow();
    void updateBackground();
    void updatePosition();
    void updateTouchFlags();
    void setOrientationSuffix(String suffix);
    int[] getCurrentPosition();
}
```

## Langkah 2: Implement Interface di Semua Modul

Setiap modul (TextModule, FpsModule, ClockModule, BatteryModule, BatteryPercentageModule, BatteryCurrentModule, NetworkModule) tambahkan:

```java
public class TextModule implements OverlayModule {
    // ... kode yang sudah ada ...

    @Override
    public boolean isRunning() {
        return view != null;
    }

    @Override
    public void updateLabelColor(int color) {
        TextConfig.labelColor = color;
        updateDisplay();
    }

    // ... method lain sudah ada, tinggal tambah @Override ...
}
```

**Catatan:** Sebagian besar method sudah ada di modul. Yang perlu ditambah hanya `isRunning()` dan `updateLabelColor()` untuk modul yang belum punya.

## Langkah 3: Ubah FloatingService

### 3.1 Ganti Instance Fields

```java
// Sebelumnya:
private TextModule textModule;
private FpsModule fpsModule;
private ClockModule clockModule;
private BatteryModule batteryModule;
private BatteryPercentageModule batteryPercentageModule;
private BatteryCurrentModule batteryCurrentModule;
private NetworkModule networkModule;

// Sesudahnya:
private final List<OverlayModule> allModules = new ArrayList<>();
private TextModule textModule;
private FpsModule fpsModule;
// ... tetap simpan reference spesifik untuk method khusus ...
```

### 3.2 Register Module di onCreate()

```java
@Override
public void onCreate() {
    super.onCreate();
    // ... existing code ...

    textModule = new TextModule();
    fpsModule = new FpsModule();
    clockModule = new ClockModule();
    batteryModule = new BatteryModule();
    batteryPercentageModule = new BatteryPercentageModule();
    batteryCurrentModule = new BatteryCurrentModule();
    networkModule = new NetworkModule();

    allModules.add(textModule);
    allModules.add(fpsModule);
    allModules.add(clockModule);
    allModules.add(batteryModule);
    allModules.add(batteryPercentageModule);
    allModules.add(batteryCurrentModule);
    allModules.add(networkModule);

    // Init semua module
    for (OverlayModule module : allModules) {
        module.init(windowManager, this, prefs);  // perlu tambah init() di interface
    }

    // Start hanya module yang enabled
    if (TextConfig.enabled) textModule.start(windowManager, this);
    if (FpsConfig.enabled) fpsModule.start(windowManager, this);
    if (ClockConfig.enabled) clockModule.start(windowManager, this);
    if (BatteryConfig.enabled) batteryModule.start(windowManager, this);
    if (BatteryPercentageConfig.enabled) batteryPercentageModule.start(windowManager, this);
    if (BatteryCurrentConfig.enabled) batteryCurrentModule.start(windowManager, this);
    if (NetworkConfig.enabled) networkModule.start(windowManager, this);
}
```

### 3.3 Ganti Static Delegates

```java
// Sebelumnya (70+ method):
public static void updateTextColorStatic() {
    if (instance != null && instance.textModule != null) {
        instance.textModule.updateColor(TextConfig.color);
    }
}
public static void updateFpsColorStatic() {
    if (instance != null && instance.fpsModule != null) {
        instance.fpsModule.updateColor(FpsConfig.color);
    }
}
// ... 60+ method lagi ...

// Sesudahnya (7 method generic):
public static void updateColorForModule(OverlayModule module, int color) {
    if (instance != null && module != null && module.isRunning()) {
        module.updateColor(color);
    }
}

public static void updateSizeForModule(OverlayModule module, float size) {
    if (instance != null && module != null && module.isRunning()) {
        module.updateSize(size);
    }
}

// ... 5 method lagi dengan pola sama ...
```

### 3.4 Method Spesifik Tetap Ada

Beberapa method tidak bisa digeneralisasi karena beda modul punya beda behavior:

```java
// Text khusus:
public static void createTextOverlayStatic() {
    if (instance != null) instance.textModule.createOverlay();
}
public static void destroyTextOverlayStatic() {
    if (instance != null) instance.textModule.destroyOverlay();
}
public static void updateTextPatternStatic() {
    if (instance != null) instance.textModule.updatePattern();
}

// BatteryCurrent khusus:
public static void toggleVoltageStatic(boolean show) {
    if (instance != null) instance.batteryCurrentModule.toggleVoltage(show);
}
public static void toggleCurrentStatic(boolean show) {
    if (instance != null) instance.batteryCurrentModule.toggleCurrent(show);
}
public static void togglePowerStatic(boolean show) {
    if (instance != null) instance.batteryCurrentModule.togglePower(show);
}
```

### 3.5 isAnyModuleActive()

```java
public static boolean isAnyModuleActive() {
    if (instance == null) return false;
    for (OverlayModule module : instance.allModules) {
        if (module.isRunning()) return true;
    }
    return false;
}
```

### 3.6 Stop Semua Module

```java
public static void stopAllModules() {
    if (instance == null) return;
    for (OverlayModule module : instance.allModules) {
        module.stop();
    }
}
```

### 3.7 reloadAllPositions()

```java
private void reloadAllPositions() {
    for (OverlayModule module : allModules) {
        module.setOrientationSuffix(null);
        module.updatePosition();
    }
}
```

## Langkah 4: Update Panel Controllers

Setiap panel controller (TextPanelController, FpsPanelController, dll) panggil static delegates yang baru:

```java
// Sebelumnya:
FloatingService.updateTextColorStatic();

// Sesudahnya:
FloatingService.updateColorForModule(TextModule.getInstance(), TextConfig.color);
```

**Catatan:** Perlu tambah `getInstance()` statis di setiap modul untuk akses dari panel controller. Atau alternatif lain: panel controller sudah punya reference ke modul masing-masing, jadi bisa panggil langsung.

## Langkah 5: Test

- Pastikan semua modul bisa start/stop dengan benar
- Pastikan update color/size/shadow/background berfungsi untuk semua modul
- Pastikan drag overlay berfungsi
- Pastikan orientasi berubah dengan benar
- Pastikan preset load/save berfungsi
- Test di Android 8, 10, 12, 14

---

## Estimasi Perubahan

| File | Estimasi Baris Berubah |
|------|----------------------|
| OverlayModule.java (baru) | ~20 baris |
| FloatingService.java | -400 baris (dari 700 jadi ~300) |
| TextModule.java | +5 baris (implement interface) |
| FpsModule.java | +5 baris |
| ClockModule.java | +5 baris |
| BatteryModule.java | +5 baris |
| BatteryPercentageModule.java | +5 baris |
| BatteryCurrentModule.java | +10 baris |
| NetworkModule.java | +5 baris |
| 7 PositionControllers | ~5 baris each |

**Total:** ~60 baris ditambah, ~400 baris dihapus. Bersih -340 baris.

---

## Risiko

- **Breaking changes** — Semua static delegates berubah nama. Perlu update semua panel controllers.
- **Method khusus** — Beberapa modul punya method unik yang tidak bisa digeneralisasi. Tetap perlu method spesifik.
- **Testing** — Perlu test menyeluruh karena ini refactor di core application.

---

## Urutan Pengerjaan

1. Buat interface `OverlayModule`
2. Implement di semua modul (tambah @Override + isRunning())
3. Ubah FloatingService: register modules, ganti delegates
4. Update semua panel controllers
5. Test menyeluruh
6. Update CHANGELOG
