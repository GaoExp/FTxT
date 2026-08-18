package exp.ftxt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.util.TypedValue;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.content.res.Configuration;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.splashscreen.SplashScreen;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import exp.ftxt.core.CrashLogger;
import exp.ftxt.core.FloatingService;
import exp.ftxt.features.battery_bar.BatteryBarConfig;
import exp.ftxt.features.battery_stats.BatteryStatsConfig;
import exp.ftxt.features.clock_module.ClockConfig;
import exp.ftxt.features.memory_stats.MemoryConfig;
import exp.ftxt.features.fps_display.FpsConfig;
import exp.ftxt.features.network_stats.NetworkConfig;
import exp.ftxt.features.floating_text.TextConfig;
import exp.ftxt.ui.PanelManager;
import exp.ftxt.utils.PermissionHelper;

public class MainActivity extends AppCompatActivity {

    private PanelManager panelManager;

    private RecyclerView navItemContainer;
    private SidebarAdapter sidebarAdapter;
    private ItemTouchHelper sidebarTouchHelper;
    private static final String PREFS_SIDEBAR_STATE = "sidebar_state";

    private static final String DEFAULT_SIDEBAR_JSON =
        "[{\"id\":\"navFloatingText\",\"l\":\"Floating Text\"}," +
        "{\"id\":\"navFps\",\"l\":\"FPS Display\"}," +
        "{\"id\":\"navNetwork\",\"l\":\"Network Stats\"}," +
        "{\"id\":\"navBattery\",\"l\":\"Battery Info\"}," +
        "{\"id\":\"navClock\",\"l\":\"Clock Module\"}," +
        "{\"id\":\"navCrosshair\",\"l\":\"Crosshair (coming soon)\"}," +
        "{\"id\":\"navMemory\",\"l\":\"Info Memori\"}," +
        "{\"id\":\"navLogo\",\"l\":\"Logo Display (coming soon)\"}," +
        "{\"id\":\"navColorPicker\",\"l\":\"Color Picker\"}," +
        "{\"id\":\"navDebuging\",\"l\":\"Debugging\"}]";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean isDark = getSharedPreferences("ftxt_prefs", MODE_PRIVATE)
                .getBoolean("theme_dark", true);

        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        CrashLogger.init(this);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        panelManager = new PanelManager(this, R.id.panel_container);

        SharedPreferences prefs = getSharedPreferences("ftxt_prefs", MODE_PRIVATE);
        int savedNavItem = prefs.getInt("nav_selected_item", R.id.navFloatingText);
        String savedPanel = panelIdToName(savedNavItem);
        if (savedPanel == null) {
            savedPanel = "text";
            savedNavItem = R.id.navFloatingText;
        }
        panelManager.showPanel(savedPanel);
        updateActionBarTitle(savedNavItem);

        TextView navTitle = findViewById(R.id.navHeaderTitle);
        try {
            String v = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            navTitle.setText("FunText v" + v + " Beta");
        } catch (PackageManager.NameNotFoundException e) {
            navTitle.setText("FunText Beta");
        }

        navItemContainer = findViewById(R.id.navItemContainer);

        rebuildSidebar();

        findViewById(R.id.navTutupAplikasi).setOnClickListener(v -> forceClose());
        findViewById(R.id.navKeluar).setOnClickListener(v -> finishAffinity());

        loadShadowConfigs();

        requestAllPermissionsOnFirstLaunch();
    }

    private void requestAllPermissionsOnFirstLaunch() {
        SharedPreferences prefs = getSharedPreferences("ftxt_prefs", MODE_PRIVATE);
        if (!prefs.getBoolean("first_launch", true)) return;
        prefs.edit().putBoolean("first_launch", false).apply();

        boolean needsOverlay = !PermissionHelper.hasOverlayPermission(this);
        boolean needsNotification = !PermissionHelper.hasNotificationPermission(this);
        boolean needsBattery = !PermissionHelper.isIgnoringBatteryOptimizations(this);

        if (!needsOverlay && !needsNotification && !needsBattery) return;

        StringBuilder msg = new StringBuilder("Aplikasi membutuhkan izin berikut:");
        if (needsOverlay) msg.append("\n• Tampilan di atas aplikasi lain");
        if (needsNotification) msg.append("\n• Notifikasi");
        if (needsBattery) msg.append("\n• Nonaktifkan optimasi baterai");
        Toast.makeText(this, msg.toString(), Toast.LENGTH_LONG).show();

        if (needsOverlay) PermissionHelper.requestOverlayPermission(this);
        if (needsNotification) PermissionHelper.requestNotificationPermission(this);
        if (needsBattery) PermissionHelper.requestDisableBatteryOptimization(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        rebuildSidebar();
        if (panelManager != null) {
            panelManager.onPanelShown();
        }
        autoRequestAndStart();
        requestRemainingPermissions();
        FloatingService.updatePositionForModule(FloatingService.textModule());
    }

    private void requestRemainingPermissions() {
        if (!PermissionHelper.hasOverlayPermission(this)) {
            PermissionHelper.requestOverlayPermission(this);
            return;
        }
        if (!PermissionHelper.hasNotificationPermission(this)) {
            PermissionHelper.requestNotificationPermission(this);
        }
        if (!PermissionHelper.isIgnoringBatteryOptimizations(this)) {
            PermissionHelper.requestDisableBatteryOptimization(this);
        }
    }

    private void autoRequestAndStart() {
        if (!isAnyModuleActive()) return;
        if (FloatingService.instance != null) return;

        if (!PermissionHelper.hasOverlayPermission(this)) {
            PermissionHelper.requestOverlayPermission(this);
            return;
        }

        if (!PermissionHelper.hasNotificationPermission(this)) {
            PermissionHelper.requestNotificationPermission(this);
        }

        if (!PermissionHelper.isIgnoringBatteryOptimizations(this)) {
            PermissionHelper.requestDisableBatteryOptimization(this);
        }

        startService(new Intent(this, FloatingService.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        boolean isDark = getSharedPreferences("ftxt_prefs", MODE_PRIVATE)
                .getBoolean("theme_dark", true);
        MenuItem themeItem = menu.findItem(R.id.action_theme);
        themeItem.setIcon(isDark ? R.drawable.ic_theme : R.drawable.ic_sun);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            showSettingsPopup();
            return true;
        }

        if (id == R.id.action_orientation) {
            int current = getResources().getConfiguration().orientation;
            if (current == Configuration.ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
            return true;
        }

        if (id == R.id.action_theme) {
        boolean isDark = getSharedPreferences("ftxt_prefs", MODE_PRIVATE)
                .getBoolean("theme_dark", true);
            boolean newDark = !isDark;
            getSharedPreferences("ftxt_prefs", MODE_PRIVATE)
                    .edit().putBoolean("theme_dark", newDark).apply();

            if (newDark) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

            recreate();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PermissionHelper.NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Izin notifikasi diberikan", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Izin notifikasi diperlukan", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void applyCheckboxTint(CheckBox cb, boolean isChecked) {
        if (isChecked) {
            cb.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#2196F3")));
        } else {
            cb.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#E53935")));
        }
    }

    public boolean checkOverlayPermission() {
        if (!PermissionHelper.hasOverlayPermission(this)) {
            PermissionHelper.requestOverlayPermission(this);
            return true;
        }
        return false;
    }

    public boolean checkNotificationPermission() {
        if (!PermissionHelper.hasNotificationPermission(this)) {
            PermissionHelper.requestNotificationPermission(this);
            return true;
        }
        return false;
    }

    public void checkBatteryOptimization() {
        if (!PermissionHelper.isIgnoringBatteryOptimizations(this)) {
            PermissionHelper.requestDisableBatteryOptimization(this);
        }
    }

    public boolean isTextOverlayOn() {
        return getSharedPreferences("ftxt_prefs", MODE_PRIVATE)
                .getBoolean("text_overlay_on", false);
    }

    public boolean isAnyModuleActive() {
        if (getSharedPreferences("ftxt_prefs", MODE_PRIVATE).getBoolean("text_overlay_on", false)) {
            return true;
        }

        if (FpsConfig.enabled) return true;
        if (ClockConfig.enabled) return true;
        if (BatteryStatsConfig.enabled) return true;
        if (NetworkConfig.enabled) return true;
        if (BatteryBarConfig.enabled) return true;
        if (MemoryConfig.enabled) return true;
        if (MemoryConfig.backgroundMonitor) return true;

        return false;
    }

    private void showSettingsPopup() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        PopupMenu popup = new PopupMenu(this, toolbar, Gravity.END, 0, R.style.SettingsPopupMenu);
        popup.getMenu().add("Muat Preset");
        popup.getMenu().add("Konfigurasi");
        popup.getMenu().add("Dokumentasi");
        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            if (title.equals("Muat Preset")) {
                if (panelManager != null) {
                    panelManager.showLoadPresetDialog();
                }
            } else if (title.equals("Konfigurasi")) {
                startActivity(new Intent(this, SettingsActivity.class));
            } else if (title.equals("Dokumentasi")) {
                startActivity(new Intent(this, DocumentationActivity.class));
            }
            return true;
        });
        popup.show();
    }

    private long backPressedTime;

    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
        } else {
            backPressedTime = System.currentTimeMillis();
            Toast.makeText(this, "Tekan kembali lagi untuk keluar", Toast.LENGTH_SHORT).show();
        }
    }

    private void killService() {
        if (FloatingService.instance != null) {
            stopService(new Intent(this, FloatingService.class));
        }
    }

    private void forceClose() {
        killService();
        finishAffinity();
    }

    private void loadShadowConfigs() {
        SharedPreferences prefs = getSharedPreferences("ftxt_prefs", MODE_PRIVATE);

        TextConfig.touchPassthrough = prefs.getBoolean("text_lock", true);
        TextConfig.safeArea = prefs.getBoolean("text_safe_area", true);

        TextConfig.shadow.enabled = prefs.getBoolean("shadow_enabled", false);
        TextConfig.shadow.color = prefs.getInt("shadow_color", Color.BLACK);
        TextConfig.shadow.blur = prefs.getFloat("shadow_blur", 5f);
        TextConfig.shadow.offsetX = prefs.getFloat("shadow_offset_x", 3f);
        TextConfig.shadow.offsetY = prefs.getFloat("shadow_offset_y", 3f);
        TextConfig.bg.enabled = prefs.getBoolean("text_bg_enabled", false);
        TextConfig.bg.color = prefs.getInt("text_bg_color", 0xCC000000);
        TextConfig.bg.padding = prefs.getInt("text_bg_padding", 25);
        TextConfig.bg.offsetX = prefs.getInt("text_bg_offset_x", 0);
        TextConfig.bg.offsetY = prefs.getInt("text_bg_offset_y", 0);
        FpsConfig.enabled = prefs.getBoolean("fps_enabled", false);
        FpsConfig.safeArea = prefs.getBoolean("fps_safe_area", true);
        FpsConfig.color = prefs.getInt("fps_color", Color.GREEN);

        FpsConfig.shadow.enabled = prefs.getBoolean("fps_shadow_enabled", false);
        FpsConfig.shadow.color = prefs.getInt("fps_shadow_color", Color.BLACK);
        FpsConfig.shadow.blur = prefs.getFloat("fps_shadow_blur", 5f);
        FpsConfig.shadow.offsetX = prefs.getFloat("fps_shadow_offset_x", 3f);
        FpsConfig.shadow.offsetY = prefs.getFloat("fps_shadow_offset_y", 3f);
        FpsConfig.touchPassthrough = prefs.getBoolean("fps_lock", true);
        FpsConfig.showOnlyValue = prefs.getBoolean("fps_show_only_value", false);
        FpsConfig.labelColor = prefs.getInt("fps_label_color", Color.CYAN);
        FpsConfig.updateInterval = readFloatPref(prefs, "fps_update_interval", 1f);
        FpsConfig.bg.enabled = prefs.getBoolean("fps_bg_enabled", false);
        FpsConfig.bg.color = prefs.getInt("fps_bg_color", 0xCC000000);
        FpsConfig.bg.padding = prefs.getInt("fps_bg_padding", 10);
        FpsConfig.bg.offsetX = prefs.getInt("fps_bg_offset_x", 0);
        FpsConfig.bg.offsetY = prefs.getInt("fps_bg_offset_y", 0);
        FpsConfig.posX = prefs.getFloat("fps_pos_x_port", 0.05f);
        FpsConfig.posY = prefs.getFloat("fps_pos_y_port", 0.05f);

        ClockConfig.enabled = prefs.getBoolean("clock_enabled", false);
        ClockConfig.color = prefs.getInt("clock_color", Color.GREEN);
        ClockConfig.shadow.enabled = prefs.getBoolean("clock_shadow_enabled", false);
        ClockConfig.shadow.color = prefs.getInt("clock_shadow_color", Color.BLACK);
        ClockConfig.shadow.blur = prefs.getFloat("clock_shadow_blur", 5f);
        ClockConfig.shadow.offsetX = prefs.getFloat("clock_shadow_offset_x", 3f);
        ClockConfig.shadow.offsetY = prefs.getFloat("clock_shadow_offset_y", 3f);
        ClockConfig.safeArea = prefs.getBoolean("clock_safe_area", true);
        ClockConfig.touchPassthrough = prefs.getBoolean("clock_lock", true);
        ClockConfig.showDate = prefs.getBoolean("clock_show_date", true);
        ClockConfig.bg.enabled = prefs.getBoolean("clock_bg_enabled", false);
        ClockConfig.bg.color = prefs.getInt("clock_bg_color", 0xCC000000);
        ClockConfig.bg.padding = prefs.getInt("clock_bg_padding", 10);

        NetworkConfig.enabled = prefs.getBoolean("network_enabled", false);
        NetworkConfig.color = prefs.getInt("network_color", Color.GREEN);
        NetworkConfig.labelColor = prefs.getInt("network_label_color", Color.CYAN);
        NetworkConfig.shadow.enabled = prefs.getBoolean("network_shadow_enabled", false);
        NetworkConfig.shadow.color = prefs.getInt("network_shadow_color", Color.BLACK);
        NetworkConfig.shadow.blur = prefs.getFloat("network_shadow_blur", 5f);
        NetworkConfig.shadow.offsetX = prefs.getFloat("network_shadow_offset_x", 3f);
        NetworkConfig.shadow.offsetY = prefs.getFloat("network_shadow_offset_y", 3f);
        NetworkConfig.safeArea = prefs.getBoolean("network_safe_area", true);
        NetworkConfig.touchPassthrough = prefs.getBoolean("network_lock", true);
        NetworkConfig.bg.enabled = prefs.getBoolean("network_bg_enabled", false);
        NetworkConfig.bg.color = prefs.getInt("network_bg_color", 0xCC000000);
        NetworkConfig.bg.padding = prefs.getInt("network_bg_padding", 8);
        NetworkConfig.updateInterval = readFloatPref(prefs, "network_update_interval", 1f);

        BatteryStatsConfig.enabled = prefs.getBoolean("battery_enabled", false);
        BatteryStatsConfig.size = prefs.getFloat("battery_size", 12f);
        BatteryStatsConfig.color = prefs.getInt("battery_color", Color.GREEN);
        BatteryStatsConfig.labelColor = prefs.getInt("battery_label_color", Color.CYAN);
        BatteryStatsConfig.separatorColor = prefs.getInt("battery_separator_color", Color.GRAY);
        BatteryStatsConfig.shadow.enabled = prefs.getBoolean("battery_shadow_enabled", false);
        BatteryStatsConfig.shadow.color = prefs.getInt("battery_shadow_color", Color.BLACK);
        BatteryStatsConfig.shadow.blur = prefs.getFloat("battery_shadow_blur", 5f);
        BatteryStatsConfig.shadow.offsetX = prefs.getFloat("battery_shadow_offset_x", 3f);
        BatteryStatsConfig.shadow.offsetY = prefs.getFloat("battery_shadow_offset_y", 3f);
        BatteryStatsConfig.safeArea = prefs.getBoolean("battery_safe_area", true);
        BatteryStatsConfig.touchPassthrough = prefs.getBoolean("battery_lock", true);
        BatteryStatsConfig.showOnlyValue = prefs.getBoolean("battery_show_only_value", false);
        BatteryStatsConfig.showTemperature = prefs.getBoolean("battery_show_temperature", true);
        BatteryStatsConfig.showPercentage = prefs.getBoolean("battery_show_percentage", false);
        BatteryStatsConfig.bg.enabled = prefs.getBoolean("battery_bg_enabled", false);
        BatteryStatsConfig.bg.color = prefs.getInt("battery_bg_color", 0xCC000000);
        BatteryStatsConfig.bg.padding = prefs.getInt("battery_bg_padding", 8);
        BatteryStatsConfig.bg.offsetX = prefs.getInt("battery_bg_offset_x", 0);
        BatteryStatsConfig.bg.offsetY = prefs.getInt("battery_bg_offset_y", 0);
        BatteryStatsConfig.bg.margin = prefs.getInt("battery_bg_margin", 0);
        BatteryStatsConfig.bg.radius = prefs.getInt("battery_bg_radius", 0);
        BatteryStatsConfig.updateInterval = readFloatPref(prefs, "battery_update_interval", 5f);

        BatteryBarConfig.enabled = prefs.getBoolean("batbar_enabled", false);
        BatteryBarConfig.quickMode = prefs.getBoolean("batbar_quick_mode", true);
        BatteryBarConfig.quickSide = prefs.getString("batbar_quick_side", "top");
        BatteryBarConfig.horizontal = prefs.getBoolean("batbar_horizontal", true);
        BatteryBarConfig.invert = prefs.getBoolean("batbar_invert", false);
        BatteryBarConfig.length = prefs.getFloat("batbar_length", 0.5f);
        BatteryBarConfig.thickness = prefs.getInt("batbar_thickness", 8);
        BatteryBarConfig.color = prefs.getInt("batbar_color", Color.GREEN);
        if (prefs.contains("batbar_color_scheme")) {
            BatteryBarConfig.colorScheme = prefs.getInt("batbar_color_scheme", BatteryBarConfig.SCHEME_NONE);
        } else {
            BatteryBarConfig.colorScheme = prefs.getBoolean("batbar_auto_color", false)
                    ? BatteryBarConfig.SCHEME_CLASSIC : BatteryBarConfig.SCHEME_NONE;
        }
        BatteryBarConfig.lowColor = prefs.getInt("batbar_low_color", Color.YELLOW);
        BatteryBarConfig.lowThreshold = prefs.getInt("batbar_low_threshold", 40);
        BatteryBarConfig.showEmptyStrip = prefs.getBoolean("batbar_show_empty_strip", true);
        BatteryBarConfig.emptyColor = prefs.getInt("batbar_empty_color", 0x66000000);
        BatteryBarConfig.radius = prefs.getInt("batbar_radius", 8);
        BatteryBarConfig.fadeSpeed = prefs.getInt("batbar_fade_speed", 1800);
        BatteryBarConfig.fadeEnabled = prefs.getBoolean("batbar_fade_enabled", false);
        BatteryBarConfig.shineEnabled = prefs.getBoolean("batbar_shine_enabled", false);
        BatteryBarConfig.shineSpeed = prefs.getInt("batbar_shine_speed", 1800);
        BatteryBarConfig.shineWidth = prefs.getInt("batbar_shine_width", 25);
        BatteryBarConfig.waveEnabled = prefs.getBoolean("batbar_wave_enabled", false);
        BatteryBarConfig.waveSpeed = prefs.getInt("batbar_wave_speed", 1000);
        BatteryBarConfig.waveAmplitude = prefs.getInt("batbar_wave_amplitude", 60);
        BatteryBarConfig.chargeWaveEnabled = prefs.getBoolean("batbar_charge_wave_enabled", false);
        BatteryBarConfig.chargeWaveSpeed = prefs.getInt("batbar_charge_wave_speed", 1000);
        BatteryBarConfig.chargeWaveAmplitude = prefs.getInt("batbar_charge_wave_amplitude", 60);
        BatteryBarConfig.updateInterval = readFloatPref(prefs, "batbar_update_interval", 1f);
        BatteryBarConfig.touchPassthrough = prefs.getBoolean("batbar_lock", true);
        BatteryBarConfig.safeArea = true;

        TextConfig.patternEnabled = prefs.getBoolean("text_pattern_enabled", false);
        TextConfig.patternSpacingH = prefs.getFloat("text_pattern_spacing_h", 180f);
        TextConfig.patternSpacingV = prefs.getFloat("text_pattern_spacing_v", 220f);
        TextConfig.patternAngle = prefs.getFloat("text_pattern_angle", -30f);

        MemoryConfig.enabled = prefs.getBoolean("mem_enabled", false);
        MemoryConfig.backgroundMonitor = prefs.getBoolean("mem_bg_monitor", false);
        MemoryConfig.size = prefs.getFloat("mem_size", 12f);
        MemoryConfig.color = prefs.getInt("mem_color", Color.WHITE);
        MemoryConfig.labelColor = prefs.getInt("mem_label_color", Color.CYAN);
        MemoryConfig.separatorColor = prefs.getInt("mem_separator_color", Color.GRAY);
        MemoryConfig.shadow.enabled = prefs.getBoolean("mem_shadow_enabled", false);
        MemoryConfig.shadow.color = prefs.getInt("mem_shadow_color", Color.BLACK);
        MemoryConfig.shadow.blur = prefs.getFloat("mem_shadow_blur", 5f);
        MemoryConfig.shadow.offsetX = prefs.getFloat("mem_shadow_offset_x", 3f);
        MemoryConfig.shadow.offsetY = prefs.getFloat("mem_shadow_offset_y", 3f);
        MemoryConfig.safeArea = prefs.getBoolean("mem_safe_area", true);
        MemoryConfig.touchPassthrough = prefs.getBoolean("mem_lock", true);
        MemoryConfig.showOnlyValue = prefs.getBoolean("mem_show_only_value", false);
        MemoryConfig.showJavaHeap = prefs.getBoolean("mem_show_java", true);
        MemoryConfig.showNativeHeap = prefs.getBoolean("mem_show_native", true);
        MemoryConfig.showGraphics = prefs.getBoolean("mem_show_graphics", true);
        MemoryConfig.showTotal = prefs.getBoolean("mem_show_total", true);
        MemoryConfig.itemOrder = prefs.getString("mem_item_order", "java,native,graphics,total");
        MemoryConfig.bg.enabled = prefs.getBoolean("mem_bg_enabled", false);
        MemoryConfig.bg.color = prefs.getInt("mem_bg_color", 0xCC000000);
        MemoryConfig.bg.padding = prefs.getInt("mem_bg_padding", 8);
        MemoryConfig.bg.offsetX = prefs.getInt("mem_bg_offset_x", 0);
        MemoryConfig.bg.offsetY = prefs.getInt("mem_bg_offset_y", 0);
        MemoryConfig.bg.margin = prefs.getInt("mem_bg_margin", 0);
        MemoryConfig.bg.radius = prefs.getInt("mem_bg_radius", 0);
        MemoryConfig.updateInterval = readFloatPref(prefs, "mem_update_interval", 1f);
        MemoryConfig.posX = prefs.getFloat("mem_pos_x_port", 0.05f);
        MemoryConfig.posY = prefs.getFloat("mem_pos_y_port", 0.6f);

        String savedText = prefs.getString("text_content", "FunText");
        if (!savedText.isEmpty()) TextConfig.text = savedText;
    }

    private float readFloatPref(SharedPreferences prefs, String key, float defaultVal) {
        try {
            return prefs.getFloat(key, defaultVal);
        } catch (ClassCastException e) {
            int old = prefs.getInt(key, (int) defaultVal);
            prefs.edit().remove(key).putFloat(key, old).apply();
            return old;
        }
    }

    private void updateNavSelection(int selectedId) {
        int[] allIds = {R.id.navFloatingText, R.id.navFps, R.id.navNetwork, R.id.navBattery,
                R.id.navClock, R.id.navCrosshair, R.id.navLogo, R.id.navColorPicker, R.id.navMemory,
                R.id.navDebuging};
        for (int id : allIds) {
            View v = findViewById(id);
            if (v != null) {
                v.setSelected(v.getId() == selectedId);
            }
        }
    }

    private int selectableBgResId = -1;

    private int resolveSelectableItemBackground() {
        if (selectableBgResId == -1) {
            TypedValue out = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.selectableItemBackground, out, true);
            selectableBgResId = out.resourceId;
        }
        return selectableBgResId;
    }

    private void saveSidebarState() {
        JSONArray arr = new JSONArray();
        for (SidebarItem item : sidebarAdapter.getItems()) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("id", item.id);
                obj.put("l", item.label);
                arr.put(obj);
            } catch (Exception e) { }
        }
        getSharedPreferences(PREFS_SIDEBAR_STATE, MODE_PRIVATE)
                .edit().putString("sidebar_json", arr.toString()).apply();
    }

    private String loadSidebarState() {
        String json = getSharedPreferences(PREFS_SIDEBAR_STATE, MODE_PRIVATE)
                .getString("sidebar_json", null);
        return json != null ? json : DEFAULT_SIDEBAR_JSON;
    }

    private List<SidebarItem> parseSidebarJson() {
        List<SidebarItem> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(loadSidebarState());
            if (arr.length() > 0) {
                JSONObject first = arr.getJSONObject(0);
                if (first.has("n")) {
                    JSONArray flat = new JSONArray();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONArray items = arr.getJSONObject(i).getJSONArray("i");
                        for (int j = 0; j < items.length(); j++) {
                            flat.put(items.getJSONObject(j));
                        }
                    }
                    arr = flat;
                }
            }
            SharedPreferences prefs = getSharedPreferences("ftxt_prefs", MODE_PRIVATE);
            boolean showDebugging = prefs.getBoolean("debugging_show_in_sidebar", true);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject item = arr.getJSONObject(i);
                String id = item.optString("id", null);
                if ("navDebuging".equals(id) && !showDebugging) {
                    continue;
                }
                list.add(new SidebarItem(item.getString("l"), id));
            }
        } catch (Exception e) {
            try {
                JSONArray def = new JSONArray(DEFAULT_SIDEBAR_JSON);
                SharedPreferences prefs = getSharedPreferences("ftxt_prefs", MODE_PRIVATE);
                boolean showDebugging = prefs.getBoolean("debugging_show_in_sidebar", true);
                for (int i = 0; i < def.length(); i++) {
                    JSONObject item = def.getJSONObject(i);
                    String id = item.optString("id", null);
                    if ("navDebuging".equals(id) && !showDebugging) {
                        continue;
                    }
                    list.add(new SidebarItem(item.getString("l"), id));
                }
            } catch (Exception e2) { }
        }
        removeStaleItems(list);
        addMissingDefaultItems(list);
        return list;
    }

    private void addMissingDefaultItems(List<SidebarItem> list) {
        List<String> existing = new ArrayList<>();
        for (SidebarItem i : list) existing.add(i.id);
        SharedPreferences prefs = getSharedPreferences("ftxt_prefs", MODE_PRIVATE);
        boolean showDebugging = prefs.getBoolean("debugging_show_in_sidebar", true);
        try {
            JSONArray def = new JSONArray(DEFAULT_SIDEBAR_JSON);
            for (int i = 0; i < def.length(); i++) {
                JSONObject o = def.getJSONObject(i);
                String id = o.optString("id", null);
                if (id != null && !existing.contains(id)) {
                    if ("navDebuging".equals(id) && !showDebugging) {
                        continue;
                    }
                    list.add(new SidebarItem(o.getString("l"), id));
                    existing.add(id);
                }
            }
        } catch (Exception e) { }
    }

    private void removeStaleItems(List<SidebarItem> list) {
        Set<String> validIds = new HashSet<>();
        try {
            JSONArray def = new JSONArray(DEFAULT_SIDEBAR_JSON);
            for (int i = 0; i < def.length(); i++) {
                String id = def.getJSONObject(i).optString("id", null);
                if (id != null) validIds.add(id);
            }
        } catch (Exception e) { }
        Iterator<SidebarItem> it = list.iterator();
        while (it.hasNext()) {
            SidebarItem item = it.next();
            if (item.id == null || !validIds.contains(item.id)) {
                it.remove();
            }
        }
    }

    public void rebuildSidebar() {
        List<SidebarItem> items = parseSidebarJson();

        navItemContainer.setLayoutManager(new LinearLayoutManager(this));
        navItemContainer.addItemDecoration(new androidx.recyclerview.widget.DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        sidebarAdapter = new SidebarAdapter(items);
        navItemContainer.setAdapter(sidebarAdapter);

        ItemTouchHelper.SimpleCallback cb = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(RecyclerView rv, RecyclerView.ViewHolder vh, RecyclerView.ViewHolder target) {
                int from = vh.getAdapterPosition();
                int to = target.getAdapterPosition();
                if (from < 0 || to < 0 || from == to) return false;
                sidebarAdapter.moveItem(from, to);
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder vh, int dir) {}

            @Override
            public void clearView(RecyclerView rv, RecyclerView.ViewHolder vh) {
                super.clearView(rv, vh);
                saveSidebarState();
            }
        };
        ItemTouchHelper ith = new ItemTouchHelper(cb);
        ith.attachToRecyclerView(navItemContainer);
        sidebarTouchHelper = ith;
    }

    private static class SidebarItem {
        final String label;
        final String id;

        SidebarItem(String label, String id) {
            this.label = label;
            this.id = id;
        }
    }

    private class SidebarAdapter extends RecyclerView.Adapter<SidebarAdapter.ViewHolder> {
        private final List<SidebarItem> items;

        SidebarAdapter(List<SidebarItem> items) {
            this.items = items;
        }

        List<SidebarItem> getItems() { return items; }

        void moveItem(int from, int to) {
            SidebarItem item = items.remove(from);
            items.add(to, item);
            notifyItemMoved(from, to);
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView textView;

            ViewHolder(TextView tv) {
                super(tv);
                textView = tv;
                tv.setOnClickListener(v -> {
                    int itemId = v.getId();
                    updateNavSelection(itemId);
                    getSharedPreferences("ftxt_prefs", MODE_PRIVATE)
                            .edit().putInt("nav_selected_item", itemId).apply();
                    String panelName = panelIdToName(itemId);
                    if (panelName != null) {
                        panelManager.showPanel(panelName);
                        updateActionBarTitle(itemId);
                    }
                    DrawerLayout drawer = findViewById(R.id.drawerLayout);
                    drawer.closeDrawers();
                });
                tv.setOnLongClickListener(v -> {
                    sidebarTouchHelper.startDrag(this);
                    return true;
                });
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView tv = new TextView(MainActivity.this);
            tv.setTextSize(16);
            tv.setPadding(dp(16), dp(12), dp(16), dp(12));
            tv.setLayoutParams(new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            tv.setBackgroundResource(resolveSelectableItemBackground());
            tv.setClickable(true);
            tv.setFocusable(true);
            return new ViewHolder(tv);
        }

        @Override
        public void onBindViewHolder(ViewHolder h, int pos) {
            SidebarItem item = items.get(pos);
            h.textView.setText(item.label);
            if (item.id != null) {
                int idRes = getResources().getIdentifier(item.id, "id", getPackageName());
                if (idRes != 0) h.textView.setId(idRes);
            }
        }

        @Override
        public int getItemCount() { return items.size(); }
    }

    @Nullable
    private String panelIdToName(int itemId) {
        if (itemId == R.id.navFloatingText) return "text";
        if (itemId == R.id.navFps) return "fps";
        if (itemId == R.id.navClock) return "clock";
        if (itemId == R.id.navBattery) return "battery";
        if (itemId == R.id.navNetwork) return "network";
        if (itemId == R.id.navColorPicker) return "color_picker";
        if (itemId == R.id.navCrosshair) return "crosshair";
        if (itemId == R.id.navLogo) return "logo";
        if (itemId == R.id.navMemory) return "memory";
        if (itemId == R.id.navDebuging) return "debugging";
        return null;
    }

    private void updateActionBarTitle(int itemId) {
        if (itemId == R.id.navFps) {
            getSupportActionBar().setTitle(R.string.nav_fps);
        } else if (itemId == R.id.navBattery) {
            getSupportActionBar().setTitle(R.string.nav_battery);
        } else if (itemId == R.id.navClock) {
            getSupportActionBar().setTitle(R.string.nav_clock);
        } else if (itemId == R.id.navNetwork) {
            getSupportActionBar().setTitle(R.string.nav_network);
        } else if (itemId == R.id.navColorPicker) {
            getSupportActionBar().setTitle(R.string.nav_color_picker);
        } else if (itemId == R.id.navCrosshair) {
            getSupportActionBar().setTitle(R.string.nav_crosshair);
        } else if (itemId == R.id.navLogo) {
            getSupportActionBar().setTitle(R.string.nav_logo);
        } else if (itemId == R.id.navMemory) {
            getSupportActionBar().setTitle(R.string.nav_memory);
        } else if (itemId == R.id.navDebuging) {
            getSupportActionBar().setTitle(R.string.nav_debuging);
        } else {
            getSupportActionBar().setTitle(R.string.nav_floating_text);
        }
    }

    private int dp(float dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
