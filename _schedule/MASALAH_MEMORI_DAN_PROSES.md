# Masalah Proses Berjalan yang Memakan Banyak Memori

## Ringkasan
Aplikasi FTxT memiliki footprint memori dan CPU yang tinggi untuk sebuah aplikasi overlay. Proses berjalan (running processes) memakan lebih dari yang seharusnya, bahkan saat tidak ada overlay yang aktif.

---

## Temuan Utama

### 1. WakeLock Selalu Aktif
**Lokasi:** `FloatingService.java:126`

```java
wakeLockManager = new WakeLockManager();
wakeLockManager.acquire(this);
```

WakeLock dipanggil di `onCreate()` tanpa syarat. WakeLock menahan CPU agar tetap aktif (partial wake lock), artinya HP tidak pernah benar-benar tidur selama service berjalan. Auto-renew setiap 4 menit memastikan wake lock tidak pernah lepas.

**Dampak:**
- CPU tetap bekerja meskipun HP idle
- Baterai terkuras lebih cepat
- Proses tetap aktif di background

### 2. Semua 7 Module Diinstansiasi Tanpa Cek Enabled
**Lokasi:** `FloatingService.java:68-74`

```java
textModule = new TextModule();
fpsModule = new FpsModule();
clockModule = new ClockModule();
batteryModule = new BatteryModule();
batteryPercentageModule = new BatteryPercentageModule();
batteryCurrentModule = new BatteryCurrentModule();
networkModule = new NetworkModule();
```

Semua module dibuat di `onCreate()` meskipun `Config.enabled = false`. Object-module tetap ada di memory, memakan heap space.

### 3. TextModule dan FpsModule Selalu Di-Init
**Lokasi:** `FloatingService.java:89-90`

```java
textModule.init(windowManager, this, prefs);
fpsModule.init(windowManager, this, prefs);
```

`init()` dipanggil tanpa cek apakah modul aktif atau tidak. Method `init()` membaca SharedPreferences, menghitung screen metrics, dan mempersiapkan state — semua percuma jika modul tidak digunakan.

### 4. Foreground Service Selalu Berjalan
**Lokasi:** `FloatingService.java:79-84`

```java
startForeground(NotificationHelper.NOTIFICATION_ID,
        NotificationHelper.buildNotification(this));
```

Foreground service tetap berjalan dengan notifikasi permanen, meskipun semua overlay dimatikan. Service ini memakan RAM dan menjaga proses tetap hidup di background.

### 5. BroadcastReceiver Selalu Didafarkan
**Lokasi:** `FloatingService.java:128-138`

```java
configChangeReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && Intent.ACTION_CONFIGURATION_CHANGED.equals(intent.getAction())) {
            reloadAllPositions();
        }
    }
};
registerReceiver(configChangeReceiver, filter);
```

Listener `ACTION_CONFIGURATION_CHANGED` tetap aktif meskipun tidak ada overlay yang perlu di-reload posisinya.

### 6. Handler Per Module Berjalan Terus
Setiap module yang aktif punya `Handler` + `Runnable` (tickRunnable) yang berjalan berkala:

- **ClockModule** — update tiap 1 detik
- **BatteryModule** — update sesuai interval (default 5 detik)
- **BatteryPercentageModule** — update sesuai interval
- **BatteryCurrentModule** — update sesuai interval
- **NetworkModule** — update sesuai interval
- **FpsModule** — `Choreographer.FrameCallback` dipanggil setiap frame

Ketika semua modul aktif, ada 6+ Handler loop yang berjalan bersamaan di main thread.

### 7. Static Instance FloatingService
**Lokasi:** `FloatingService.java:50`

```java
public static FloatingService instance;
```

Static reference ke service membuat object tidak bisa di-GC (garbage collected) meskipun seharusnya sudah tidak dipakai. Ini potensi memory leak.

---

## Analisis Dampak

### Memory
- 7 module object × ~10-50KB per object = ~70-350KB untuk object saja
- SharedPreferences cache di memory
- Screen metrics yang di-cache di setiap module
- ShadowTextView instances untuk module yang aktif
- WakeLock reference

### CPU
- WakeLock menahan CPU tetap aktif
- Handler loops untuk setiap modul aktif
- Choreographer callback untuk FPS (60x per detik)
- Broadcast receiver listener

### Baterai
- WakeLock adalah konsumen baterai terbesar
- Foreground service menjaga proses tetap hidup
- Update loop berkala membangunkan CPU dari sleep

---

## Solusi yang Direkomendasikan

### Prioritas 1: Lazy Initialization
Jangan buat semua module di `onCreate()`. Buat module hanya saat pertama kali diaktifkan:

```java
// Contoh: ClockModule
if (ClockConfig.enabled && clockModule == null) {
    clockModule = new ClockModule();
}
if (clockModule != null) {
    clockModule.start(windowManager, this);
}
```

### Prioritas 2: Conditional WakeLock
WakeLock hanya diambil jika ada module yang aktif:

```java
if (isAnyModuleActive()) {
    wakeLockManager.acquire(this);
}
```

Update juga saat module start/stop:
```java
// Saat module start
if (isAnyModuleActive() && wakeLockManager == null) {
    wakeLockManager = new WakeLockManager();
    wakeLockManager.acquire(this);
}

// Saat module stop
if (!isAnyModuleActive() && wakeLockManager != null) {
    wakeLockManager.release();
    wakeLockManager = null;
}
```

### Prioritas 3: Conditional Service Start
Jangan langsung start foreground service. Mulai service hanya saat ada module yang diaktifkan:

```java
// Di MainActivity, saat user toggle module pertama kali
if (!isServiceRunning()) {
    startForegroundService(intent);
}
```

### Prioritas 4: Cleanup Module Saat Stop
Saat module di-stop, null-kan referensi agar bisa di-GC:

```java
public void stop() {
    running = false;
    handler.removeCallbacks(tickRunnable);
    if (view != null) {
        wm.removeView(view);
        view = null;  // ← Penting: nullkan view
    }
    params = null;
    choreographer = null;
}
```

### Prioritas 5: Conditional BroadcastReceiver
Hanya register receiver saat ada overlay yang perlu di-reload:

```java
if (isAnyModuleActive()) {
    registerReceiver(configChangeReceiver, filter);
}
```

### Prioritas 6: Hapus Static Instance
Ganti static instance dengan Application-level singleton yang lebih aman:

```java
// Daripada:
public static FloatingService instance;

// Gunakan:
public class FxtxApp extends Application {
    private FloatingService service;
    public void setService(FloatingService s) { this.service = s; }
    public FloatingService getService() { return service; }
}
```

---

## Estimasi Penghematan

| Solusi | Estimasi Penghematan |
|--------|---------------------|
| Lazy initialization | ~20-40% memory saat idle |
| Conditional WakeLock | ~30-50% baterai saat idle |
| Conditional service | ~20-30% memory |
| Cleanup module stop | ~10-20% memory |
| Conditional receiver | ~5-10% CPU |

---

## Risiko

- **Lazy initialization** — Perlu handle case saat module belum di-init tapi user mencoba update
- **Conditional WakeLock** — Perlu koordinasi antar module untuk tahu siapa yang aktif
- **Hapus static instance** — Perlu refactor besar-besaran di semua static delegates

---

## Kesimpulan

Masalah utama bukan di satu bagian, tapi akumulasi dari beberapa keputusan desain:
1. Semua module di-init sekaligus (wasteful)
2. WakeLock selalu aktif (baterai)
3. Service tetap jalan meskipun kosong (memory)
4. Static references mencegah garbage collection (leak)

Solusi paling berdampak dan相对 mudah diimplementasi adalah **conditional WakeLock** dan **lazy initialization**.
