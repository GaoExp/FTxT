# Refactor ColorPickerDialog — Kurangi Parameter Passing

## Masalah
ColorPickerDialog.java punya 928 baris. Beberapa method punya parameter passing 20-30 parameter. Ini bikin kode sulit dibaca, mudah salah, dan susah di-maintain.

## Solusi
Buat inner class `ColorPickerConfig` untuk mengganti parameter passing panjang.

---

## Contoh Masalah

### Sebelumnya (20+ parameter)
```java
public void show(Context context,
    int initialColor,
    boolean showAlpha,
    boolean showSavedColors,
    boolean showHexEditor,
    boolean showColorName,
    boolean showRgbSliders,
    int maxSavedColors,
    OnColorSelectedListener listener,
    Runnable onDismiss,
    Runnable onSaved,
    SharedPreferences prefs,
    String savedColorsKey,
    // ... parameter lain
) {
    // ... 300+ baris kode
}
```

Masalah:
- Parameter ke-12 apa fungsinya? Susah tahu tanpa baca definisi method
- Urutan parameter bisa tertukar tanpa error compile
- Tambah fitur baru harus tambah parameter di semua pemanggilan
- Test method harus isi semua parameter meskipun tidak perlu

### Sesudahnya (1 parameter object)
```java
public void show(Context context, ColorPickerConfig config) {
    // ... 300+ baris kode, akses via config.getInitialColor(), config.isShowAlpha(), dll
}
```

---

## Langkah 1: Buat Class ColorPickerConfig

**File baru:** `app/src/main/java/exp/ftxt/shared/ui/ColorPickerConfig.java`

```java
public class ColorPickerConfig {
    private int initialColor = Color.WHITE;
    private boolean showAlpha = true;
    private boolean showSavedColors = true;
    private boolean showHexEditor = true;
    private boolean showColorName = true;
    private boolean showRgbSliders = false;
    private int maxSavedColors = 16;
    private OnColorSelectedListener listener;
    private Runnable onDismiss;
    private Runnable onSaved;
    private SharedPreferences prefs;
    private String savedColorsKey = "cp_saved_colors";

    // Builder pattern untuk kemudahan
    public static ColorPickerConfig.Builder builder() {
        return new ColorPickerConfig.Builder();
    }

    // Getters
    public int getInitialColor() { return initialColor; }
    public boolean isShowAlpha() { return showAlpha; }
    public boolean isShowSavedColors() { return showSavedColors; }
    public boolean isShowHexEditor() { return showHexEditor; }
    public boolean isShowColorName() { return showColorName; }
    public boolean isShowRgbSliders() { return showRgbSliders; }
    public int getMaxSavedColors() { return maxSavedColors; }
    public OnColorSelectedListener getListener() { return listener; }
    public Runnable getOnDismiss() { return onDismiss; }
    public Runnable getOnSaved() { return onSaved; }
    public SharedPreferences getPrefs() { return prefs; }
    public String getSavedColorsKey() { return savedColorsKey; }

    // Setters
    public void setInitialColor(int color) { this.initialColor = color; }
    public void setShowAlpha(boolean show) { this.showAlpha = show; }
    public void setShowSavedColors(boolean show) { this.showSavedColors = show; }
    public void setShowHexEditor(boolean show) { this.showHexEditor = show; }
    public void setShowColorName(boolean show) { this.showColorName = show; }
    public void setShowRgbSliders(boolean show) { this.showRgbSliders = show; }
    public void setMaxSavedColors(int max) { this.maxSavedColors = max; }
    public void setListener(OnColorSelectedListener listener) { this.listener = listener; }
    public void setOnDismiss(Runnable onDismiss) { this.onDismiss = onDismiss; }
    public void setOnSaved(Runnable onSaved) { this.onSaved = onSaved; }
    public void setPrefs(SharedPreferences prefs) { this.prefs = prefs; }
    public void setSavedColorsKey(String key) { this.savedColorsKey = key; }

    // Builder class
    public static class Builder {
        private final ColorPickerConfig config = new ColorPickerConfig();

        public Builder initialColor(int color) { config.setInitialColor(color); return this; }
        public Builder showAlpha(boolean show) { config.setShowAlpha(show); return this; }
        public Builder showSavedColors(boolean show) { config.setShowSavedColors(show); return this; }
        public Builder showHexEditor(boolean show) { config.setShowHexEditor(show); return this; }
        public Builder showColorName(boolean show) { config.setShowColorName(show); return this; }
        public Builder showRgbSliders(boolean show) { config.setShowRgbSliders(show); return this; }
        public Builder maxSavedColors(int max) { config.setMaxSavedColors(max); return this; }
        public Builder listener(OnColorSelectedListener listener) { config.setListener(listener); return this; }
        public Builder onDismiss(Runnable onDismiss) { config.setOnDismiss(onDismiss); return this; }
        public Builder onSaved(Runnable onSaved) { config.setOnSaved(onSaved); return this; }
        public Builder prefs(SharedPreferences prefs) { config.setPrefs(prefs); return this; }
        public Builder savedColorsKey(String key) { config.setSavedColorsKey(key); return this; }

        public ColorPickerConfig build() { return config; }
    }
}
```

## Langkah 2: Update ColorPickerDialog.show()

```java
// Sebelumnya:
public void show(Context context,
    int initialColor,
    boolean showAlpha,
    boolean showSavedColors,
    // ... 20 parameter lain
) {
    // ...
}

// Sesudahnya:
public void show(Context context, ColorPickerConfig config) {
    int initialColor = config.getInitialColor();
    boolean showAlpha = config.isShowAlpha();
    boolean showSavedColors = config.isShowSavedColors();
    // ... akses lain dari config

    // ... sisa kode sama
}
```

## Langkah 3: Update Semua Pemanggilan

### Di Panel Controllers

```java
// Sebelumnya:
ColorPickerDialog.show(activity,
    TextConfig.color,
    true,   // showAlpha
    true,   // showSavedColors
    true,   // showHexEditor
    true,   // showColorName
    false,  // showRgbSliders
    16,     // maxSavedColors
    color -> {
        TextConfig.color = color;
        FloatingService.updateTextColorStatic();
    },
    null,   // onDismiss
    null,   // onSaved
    prefs,
    "cp_saved_colors"
);

// Sesudahnya:
ColorPickerConfig config = ColorPickerConfig.builder()
    .initialColor(TextConfig.color)
    .showAlpha(true)
    .showSavedColors(true)
    .showHexEditor(true)
    .showColorName(true)
    .showRgbSliders(false)
    .maxSavedColors(16)
    .listener(color -> {
        TextConfig.color = color;
        FloatingService.updateTextColorStatic();
    })
    .prefs(prefs)
    .savedColorsKey("cp_saved_colors")
    .build();

ColorPickerDialog.show(activity, config);
```

### Di ColorPickerPanelController

```java
// Sebelumnya:
dialog.show(context,
    currentColor,
    true, true, true, true, false, 16,
    listener, onDismiss, onSaved,
    prefs, "cp_saved_colors"
);

// Sesudahnya:
ColorPickerConfig config = ColorPickerConfig.builder()
    .initialColor(currentColor)
    .showAlpha(true)
    .showSavedColors(true)
    .showHexEditor(true)
    .showColorName(true)
    .showRgbSliders(false)
    .maxSavedColors(16)
    .listener(listener)
    .onDismiss(onDismiss)
    .onSaved(onSaved)
    .prefs(prefs)
    .savedColorsKey("cp_saved_colors")
    .build();

dialog.show(context, config);
```

---

## Kelebihan

### 1. Readability
```java
// Sebelumnya: apa parameter ke-5?
.show(ctx, 0xFF0000, true, true, true, true, false, 16, listener, null, null, prefs, "key")

// Sesudahnya: jelas
ColorPickerConfig.builder()
    .initialColor(Color.RED)
    .showAlpha(true)
    .listener(listener)
    .build()
```

### 2. Flexibility
Tambah parameter baru tinggal tambah field di Config. Tidak perlu ubah method signature.

### 3. Default Values
Config punya default values. Hanya perlu set parameter yang berbeda dari default.

### 4. Testability
Test ColorPickerDialog hanya perlu buat Config dengan parameter tertentu, tidak perlu isi semua parameter.

### 5. Backward Compatibility
Bisa buat overloaded method lama yang internally panggil method baru:
```java
// Method lama tetap bisa dipakai (temporary)
public void show(Context context, int color, boolean alpha, ...) {
    ColorPickerConfig config = ColorPickerConfig.builder()
        .initialColor(color)
        .showAlpha(alpha)
        // ...
        .build();
    show(context, config);
}
```

---

## File yang Perlu Diubah

| File | Aksi |
|------|------|
| `ColorPickerConfig.java` | Baru — Config class dengan builder |
| `ColorPickerDialog.java` | Ubah — Ganti method show() signature |
| `ColorPickerPanelController.java` | Ubah — Update pemanggilan show() |
| `TextPanelController.java` | Ubah — Update pemanggilan show() |
| `FpsPanelController.java` | Ubah — Update pemanggilan show() |
| `ClockPanelController.java` | Ubah — Update pemanggilan show() |
| `BatteryPanelController.java` | Ubah — Update pemanggilan show() |
| `BatteryPercentagePanelController.java` | Ubah — Update pemanggilan show() |
| `BatteryCurrentPanelController.java` | Ubah — Update pemanggilan show() |
| `NetworkPanelController.java` | Ubah — Update pemanggilan show() |

---

## Estimasi Perubahan

| File | Perubahan |
|------|-----------|
| ColorPickerConfig.java (baru) | ~80 baris |
| ColorPickerDialog.java | -20 baris (ganti signature) |
| 7 Panel Controllers | ~10 baris each (update pemanggilan) |

**Total:** +80 baris (config), -20 baris (dialog), +70 baris (controllers update). Bersih +130 baris, tapi kode jauh lebih readable dan maintainable.

---

## Risiko

- **Breaking changes** — Semua pemanggilan show() harus diupdate
- **Temporary backward compatibility** — Bisa buat method lama sebagai wrapper untuk transisi
- **Testing** — Perlu test ulang semua integrasi color picker

---

## Urutan Pengerjaan

1. Buat class ColorPickerConfig dengan builder
2. Tambah overloaded method show(Context, ColorPickerConfig) di dialog
3. Update satu panel controller sebagai test case
4. Update semua panel controllers
5. Hapus method lama jika sudah tidak ada yang pakai
6. Test menyeluruh
7. Update CHANGELOG
