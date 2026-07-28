# Panel Navigation — Refactor ke Fragment

## Masalah Saat Ini
Panel navigation pakai View visibility manual:
- 9 variable View terpisah (panelText, panelFps, panelClock, dll)
- `hideAllPanels()` hardcode 9 baris `setVisibility(GONE)`
- Sidebar click handler hardcode 9 if-else `setVisibility(VISIBLE)`
- `showSettingsPopup()` hardcode 9 if-else cek visibility
- Tambah panel baru = update di 3 tempat

## Solusi: Fragment-based Navigation

### Konsep
Setiap panel jadi Fragment terpisah. Panel manager mengelola show/hide/replace fragment.

### Kelebihan
- Tambah panel baru tinggal buat Fragment baru, tidak perlu ubah panel manager
- Fragment bisa di-load/unload sesuai kebutuhan (hemat memory)
- Lebih mudah di-maintain karena kode terisolasi per panel
- Support back navigation secara default
- Lebih mudah test unit per panel

### Kekurangan
- Refactor besar karena harus convert semua panel ke Fragment
- Perlu ubah activity_main.xml ke FrameLayout container
- Perlu handle lifecycle Fragment (onCreateView, onDestroyView)
- Perlu koordinasi antar Fragment untuk shared state

---

## Langkah Implementasi

### 1. Buat Base Fragment

**File baru:** `app/src/main/java/exp/ftxt/ui/BasePanelFragment.java`

```java
public abstract class BasePanelFragment extends Fragment {
    protected abstract int getLayoutResId();
    protected abstract String getPanelName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(getLayoutResId(), container, false);
    }
}
```

### 2. Convert Panel ke Fragment

Contoh untuk Text Panel:

**File baru:** `app/src/main/java/exp/ftxt/ui/fragment/TextPanelFragment.java`

```java
public class TextPanelFragment extends BasePanelFragment {
    @Override
    protected int getLayoutResId() {
        return R.layout.panel_text;
    }

    @Override
    protected String getPanelName() {
        return "text";
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Init TextPanelController di sini
        // Semua binding pake view.findViewById()
    }
}
```

Ulangi untuk semua panel:
- TextPanelFragment → panel_text.xml
- FpsPanelFragment → panel_fps.xml
- ClockPanelFragment → panel_clock.xml
- BatteryPanelFragment → panel_battery.xml
- BatteryPercentagePanelFragment → panel_battery_percentage.xml
- BatteryCurrentPanelFragment → panel_battery_current.xml
- NetworkPanelFragment → panel_network.xml
- ColorPickerPanelFragment → panel_color_picker.xml

### 3. Buat Panel Manager

**File baru:** `app/src/main/java/exp/ftxt/ui/PanelManager.java`

```java
public class PanelManager {
    private final FragmentManager fragmentManager;
    private final int containerId;
    private String currentPanel = null;

    private final Map<String, Class<? extends BasePanelFragment>> panelMap = new HashMap<>();

    public PanelManager(FragmentActivity activity, int containerId) {
        this.fragmentManager = activity.getSupportFragmentManager();
        this.containerId = containerId;

        // Register semua panel di satu tempat
        panelMap.put("text", TextPanelFragment.class);
        panelMap.put("fps", FpsPanelFragment.class);
        panelMap.put("clock", ClockPanelFragment.class);
        panelMap.put("battery", BatteryPanelFragment.class);
        panelMap.put("battery_pct", BatteryPercentagePanelFragment.class);
        panelMap.put("battery_cur", BatteryCurrentPanelFragment.class);
        panelMap.put("network", NetworkPanelFragment.class);
        panelMap.put("color_picker", ColorPickerPanelFragment.class);
    }

    public void showPanel(String name) {
        if (name.equals(currentPanel)) return;

        FragmentTransaction ft = fragmentManager.beginTransaction();

        // Sembunyikan semua fragment yang visible
        for (Fragment f : fragmentManager.getFragments()) {
            if (f instanceof BasePanelFragment) {
                ft.hide(f);
            }
        }

        // Cek apakah fragment sudah ada di back stack
        Fragment target = fragmentManager.findFragmentByTag(name);
        if (target != null) {
            ft.show(target);
        } else {
            // Buat fragment baru
            Class<? extends BasePanelFragment> cls = panelMap.get(name);
            if (cls != null) {
                try {
                    target = cls.newInstance();
                    ft.add(containerId, target, name);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        ft.commit();
        currentPanel = name;
    }

    public String getCurrentPanel() {
        return currentPanel;
    }

    public boolean hasPanel(String name) {
        return panelMap.containsKey(name);
    }
}
```

### 4. Update activity_main.xml

```xml
<!-- Sebelumnya: semua panel di-include langsung -->
<!-- <include layout="@layout/panel_text"/> -->
<!-- <include layout="@layout/panel_fps"/> -->
<!-- ... -->

<!-- Sesudahnya: satu container Fragment -->
<FrameLayout
    android:id="@+id/panel_container"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

### 5. Update MainActivity

```java
public class MainActivity extends AppCompatActivity {
    private PanelManager panelManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inisialisasi panel manager
        panelManager = new PanelManager(this, R.id.panel_container);

        // Default panel: Text
        panelManager.showPanel("text");
    }

    // Sidebar click handler
    private void onSidebarItemClick(String panelName) {
        panelManager.showPanel(panelName);
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    // Settings popup
    private String getCurrentPanelForPreset() {
        return panelManager.getCurrentPanel();
    }
}
```

### 6. Update SidebarAdapter

```java
// Di ViewHolder.onClick:
String panelName = getPanelNameForPosition(getAdapterPosition());
panelManager.showPanel(panelName);
```

---

## Mapping Panel Name ke Fragment

| Panel Name | Fragment Class | Layout |
|------------|---------------|--------|
| text | TextPanelFragment | panel_text.xml |
| fps | FpsPanelFragment | panel_fps.xml |
| clock | ClockPanelFragment | panel_clock.xml |
| battery | BatteryPanelFragment | panel_battery.xml |
| battery_pct | BatteryPercentagePanelFragment | panel_battery_percentage.xml |
| battery_cur | BatteryCurrentPanelFragment | panel_battery_current.xml |
| network | NetworkPanelFragment | panel_network.xml |
| color_picker | ColorPickerPanelFragment | panel_color_picker.xml |
| crosshair | CrosshairPanelFragment | panel_crosshair.xml |
| logo | LogoPanelFragment | panel_logo.xml |

---

## File yang Perlu Diubah

| File | Aksi |
|------|------|
| `PanelManager.java` | Baru — Kelola fragment navigation |
| `BasePanelFragment.java` | Baru — Base class untuk semua panel fragment |
| `TextPanelFragment.java` | Baru — Convert TextPanel |
| `FpsPanelFragment.java` | Baru — Convert FpsPanel |
| `ClockPanelFragment.java` | Baru — Convert ClockPanel |
| `BatteryPanelFragment.java` | Baru — Convert BatteryPanel |
| `BatteryPercentagePanelFragment.java` | Baru — Convert BatteryPercentagePanel |
| `BatteryCurrentPanelFragment.java` | Baru — Convert BatteryCurrentPanel |
| `NetworkPanelFragment.java` | Baru — Convert NetworkPanel |
| `ColorPickerPanelFragment.java` | Baru — Convert ColorPickerPanel |
| `activity_main.xml` | Ubah — Ganti includes jadi FrameLayout |
| `MainActivity.java` | Ubah — Ganti panel logic ke PanelManager |
| `SidebarAdapter.java` | Ubah — Panggil PanelManager.showPanel() |

---

## Tantangan

### 1. Panel Controllers
Setiap panel punya PanelController (TextPanelController, FpsPanelController, dll). Saat convert ke Fragment, controller harus di-init di `onViewCreated()` bukan di `onCreate()` Activity. Controller juga perlu cleanup di `onDestroyView()`.

### 2. Shared State
Beberapa panel berbagi state (misal: warna dari ColorPicker). Fragment isolation bisa bikin sharing lebih sulit. Perlu shared ViewModel atau event bus.

### 3. Position Controllers
PositionController terpisah dari PanelController. Perlu dipastikan position controller juga di-init dengan benar di Fragment lifecycle.

### 4. Panel Visibility Check
Saat ini `showSettingsPopup()` cek visibility untuk tentukan panel aktif. Dengan Fragment, ganti jadi `panelManager.getCurrentPanel()`.

### 5. Slide Animation
Fragment transaction bisa ditambah animasi slide saat ganti panel.

---

## Estimasi Kompleksitas
- Tinggi: convert 8-10 panel ke Fragment
- Sedang: buat PanelManager dan BasePanelFragment
- Sedang: update MainActivity dan SidebarAdapter
- Rendah: update activity_main.xml

---

## Alternatif: Map (Lebih Simpel)
Kalau refactor Fragment terlalu besar, bisa mulai dari Map dulu:

```java
Map<String, View> panels = new HashMap<>();
panels.put("text", panelText);
panels.put("fps", panelFps);
// ...

private void showPanel(String name) {
    for (View p : panels.values()) p.setVisibility(View.GONE);
    View target = panels.get(name);
    if (target != null) target.setVisibility(View.VISIBLE);
}
```

Ini bisa dilakukan sebagai langkah perantara sebelum refactor ke Fragment.
