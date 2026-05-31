package exp.ftxt;

import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.util.TypedValue;
import android.graphics.Color;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.Gravity;
import android.content.res.Configuration;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.splashscreen.SplashScreen;
import androidx.drawerlayout.widget.DrawerLayout;

import exp.ftxt.core.FloatingService;
import exp.ftxt.features.battery_current.BatteryCurrentConfig;
import exp.ftxt.features.battery_percentage.BatteryPercentageConfig;
import exp.ftxt.features.battery_temperature.BatteryConfig;
import exp.ftxt.features.clock_module.ClockConfig;
import exp.ftxt.features.fps_display.FpsConfig;
import exp.ftxt.features.network_stats.NetworkConfig;
import exp.ftxt.features.floating_text.TextConfig;
import exp.ftxt.ui.BatteryPanelController;
import exp.ftxt.ui.BatteryCurrentPanelController;

import exp.ftxt.ui.ClockPanelController;
import exp.ftxt.ui.FpsPanelController;
import exp.ftxt.ui.NetworkPanelController;
import exp.ftxt.ui.TextPanelController;
import exp.ftxt.utils.PermissionHelper;

/**
 * Activity utama FTxT.
 *
 * Mengelola:
 * - Toolbar & Navigation Drawer (sidebar modular)
 * - Theme toggle (gelap/terang)
 * - Delegasi panel UI → TextPanelController dan FpsPanelController
 * - Delegasi permission → PermissionHelper
 * - Switch tint utility
 *
 * Panel UI didelegasikan ke:
 * - TextPanelController → ui/TextPanelController.java
 * - FpsPanelController  → ui/FpsPanelController.java
 *
 * Permission didelegasikan ke:
 * - PermissionHelper → utils/PermissionHelper.java
 *
 * Service overlay:
 * - FloatingService → core/FloatingService.java
 */
public class MainActivity extends AppCompatActivity {

    View panelText;
    View panelFps;
    View panelClock;
    View panelBattery;
    View panelBatteryCurrent;
    View panelNetwork;
    private TextPanelController textPanel;
    private FpsPanelController fpsPanel;
    private ClockPanelController clockPanel;
    private BatteryPanelController batteryPanel;
    private BatteryCurrentPanelController batteryCurrentPanel;
    private NetworkPanelController networkPanel;
    private View panelCrosshair;
    private View panelWatermark;
    private View panelLogo;

    private LinearLayout navItemContainer;
    private static final String PREFS_SIDEBAR_STATE = "sidebar_state";

    private static final String DEFAULT_SIDEBAR_JSON =
        "[{\"id\":\"navFloatingText\",\"l\":\"Floating Text\"}," +
        "{\"id\":\"navFps\",\"l\":\"FPS Display\"}," +
        "{\"id\":\"navNetwork\",\"l\":\"Network Stats\"}," +
        "{\"id\":\"navBattery\",\"l\":\"Battery Stats\"}," +

        "{\"id\":\"navBatteryCurrent\",\"l\":\"Battery Current\"}," +
        "{\"id\":\"navClock\",\"l\":\"Clock Module\"}," +
        "{\"id\":\"navCrosshair\",\"l\":\"Crosshair (coming soon)\"}," +
        "{\"id\":\"navWatermark\",\"l\":\"Watermark (coming soon)\"}," +
        "{\"id\":\"navLogo\",\"l\":\"Logo Display (coming soon)\"}]";

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
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        panelText = findViewById(R.id.panel_text);
        panelFps = findViewById(R.id.panel_fps);
        panelClock = findViewById(R.id.panel_clock);
        panelBattery = findViewById(R.id.panel_battery);
        panelBatteryCurrent = findViewById(R.id.panel_battery_current);
        panelNetwork = findViewById(R.id.panel_network);
        panelCrosshair = findViewById(R.id.panel_crosshair);
        panelWatermark = findViewById(R.id.panel_watermark);
        panelLogo = findViewById(R.id.panel_logo);
        SharedPreferences prefs = getSharedPreferences("ftxt_prefs", MODE_PRIVATE);
        int savedNavItem = prefs.getInt("nav_selected_item", R.id.navFloatingText);
        if (savedNavItem == R.id.navFps) {
            panelText.setVisibility(View.GONE);
            panelFps.setVisibility(View.VISIBLE);
            getSupportActionBar().setTitle(R.string.nav_fps);
        } else if (savedNavItem == R.id.navBattery) {
            panelText.setVisibility(View.GONE);
            panelBattery.setVisibility(View.VISIBLE);
            getSupportActionBar().setTitle(R.string.nav_battery);
        } else if (savedNavItem == R.id.navClock) {
            panelText.setVisibility(View.GONE);
            panelClock.setVisibility(View.VISIBLE);
            getSupportActionBar().setTitle(R.string.nav_clock);
        } else if (savedNavItem == R.id.navBatteryCurrent) {
            panelText.setVisibility(View.GONE);
            panelBatteryCurrent.setVisibility(View.VISIBLE);
            getSupportActionBar().setTitle(R.string.nav_battery_current);
        } else if (savedNavItem == R.id.navNetwork) {
            panelText.setVisibility(View.GONE);
            panelNetwork.setVisibility(View.VISIBLE);
            getSupportActionBar().setTitle(R.string.nav_network);
        }

        TextView navTitle = findViewById(R.id.navHeaderTitle);
        try {
            String v = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            navTitle.setText("FunText v" + v + " Beta");
        } catch (PackageManager.NameNotFoundException e) {
            navTitle.setText("FunText Beta");
        }

        navItemContainer = findViewById(R.id.navItemContainer);

        rebuildSidebar();

        loadShadowConfigs();

        textPanel = new TextPanelController(this);
        fpsPanel = new FpsPanelController(this);
        clockPanel = new ClockPanelController(this);
        batteryPanel = new BatteryPanelController(this);
        batteryCurrentPanel = new BatteryCurrentPanelController(this);
        networkPanel = new NetworkPanelController(this);

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
        if (textPanel != null && panelText.getVisibility() == View.VISIBLE) {
            textPanel.onPanelShown();
        }
        if (fpsPanel != null && panelFps.getVisibility() == View.VISIBLE) {
            fpsPanel.onPanelShown();
        }
        if (clockPanel != null && panelClock.getVisibility() == View.VISIBLE) {
            clockPanel.onPanelShown();
        }
        if (batteryPanel != null && panelBattery.getVisibility() == View.VISIBLE) {
            batteryPanel.onPanelShown();
        }
        if (batteryCurrentPanel != null && panelBatteryCurrent.getVisibility() == View.VISIBLE) {
            batteryCurrentPanel.onPanelShown();
        }
        if (networkPanel != null && panelNetwork.getVisibility() == View.VISIBLE) {
            networkPanel.onPanelShown();
        }
        autoRequestAndStart();
        requestRemainingPermissions();
        FloatingService.updateTextPositionStatic();
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
        SharedPreferences prefs = getSharedPreferences("ftxt_prefs", MODE_PRIVATE);
        boolean wasOverlayOn = prefs.getBoolean("text_overlay_on", false);
        if (!wasOverlayOn && !FpsConfig.enabled) return;
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

    // ========================================================================
    // CheckBox tint utility — dipanggil oleh PanelControllers
    // ========================================================================
    public void applyCheckboxTint(CheckBox cb, boolean isChecked) {
        if (isChecked) {
            cb.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#2196F3")));
        } else {
            cb.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#E53935")));
        }
    }

    // ========================================================================
    // Permission helpers — dipanggil oleh TextPanelController saat toggle overlay
    // Delegasi ke: PermissionHelper → utils/PermissionHelper.java
    // ========================================================================

    /**
     * Periksa dan minta izin overlay. Return true jika perlu handling (izin belum diberikan).
     */
    public boolean checkOverlayPermission() {
        if (!PermissionHelper.hasOverlayPermission(this)) {
            PermissionHelper.requestOverlayPermission(this);
            return true;
        }
        return false;
    }

    /**
     * Periksa dan minta izin notifikasi (Android 13+). Return true jika perlu handling.
     */
    public boolean checkNotificationPermission() {
        if (!PermissionHelper.hasNotificationPermission(this)) {
            PermissionHelper.requestNotificationPermission(this);
            return true;
        }
        return false;
    }

    /**
     * Periksa dan minta nonaktifkan optimasi baterai jika perlu.
     */
    public void checkBatteryOptimization() {
        if (!PermissionHelper.isIgnoringBatteryOptimizations(this)) {
            PermissionHelper.requestDisableBatteryOptimization(this);
        }
    }

    /**
     * Cek apakah switch text overlay sedang ON.
     * Dipanggil oleh FpsPanelController saat FPS dimatikan.
     */
    public boolean isTextOverlayOn() {
        return getSharedPreferences("ftxt_prefs", MODE_PRIVATE)
                .getBoolean("text_overlay_on", false);
    }

    // ========================================================================
    // Shadow config loading — dipanggil saat onCreate
    // ========================================================================

    // ========================================================================
    // Settings popup — gear icon → dropdown: Konfigurasi, Dokumentasi, Tutup
    // ========================================================================
    private void showSettingsPopup() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        PopupMenu popup = new PopupMenu(this, toolbar, Gravity.END, 0, R.style.SettingsPopupMenu);
        popup.getMenu().add("Muat Preset");
        popup.getMenu().add("Konfigurasi");
        popup.getMenu().add("Lihat Dokumentasi");
        popup.getMenu().add("Tutup Aplikasi");
        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            if (title.equals("Muat Preset")) {
                if (panelFps.getVisibility() == View.VISIBLE) {
                    fpsPanel.showLoadPresetDialog();
                } else if (panelClock.getVisibility() == View.VISIBLE) {
                    clockPanel.showLoadPresetDialog();
                } else if (panelBattery.getVisibility() == View.VISIBLE) {
                    batteryPanel.showLoadPresetDialog();
                } else if (panelBatteryCurrent.getVisibility() == View.VISIBLE) {
                    batteryCurrentPanel.showLoadPresetDialog();
                } else if (panelNetwork.getVisibility() == View.VISIBLE) {
                    networkPanel.showLoadPresetDialog();
                } else {
                    textPanel.showLoadPresetDialog();
                }
            } else if (title.equals("Konfigurasi")) {
                startActivity(new Intent(this, SettingsActivity.class));
            } else if (title.equals("Lihat Dokumentasi")) {
                startActivity(new Intent(this, DocumentationActivity.class));
            } else if (title.equals("Tutup Aplikasi")) {
                forceClose();
            }
            return true;
        });
        popup.show();
    }

    private long backPressedTime;

    @Override
    public void onBackPressed() {
        boolean confirmExit = getSharedPreferences("ftxt_prefs", MODE_PRIVATE)
                .getBoolean("confirm_exit", false);
        if (confirmExit) {
            if (backPressedTime + 2000 > System.currentTimeMillis()) {
                super.onBackPressed();
            } else {
                backPressedTime = System.currentTimeMillis();
                Toast.makeText(this, "Tekan kembali lagi untuk keluar", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onBackPressed();
        }
    }

    private void forceClose() {
        if (FloatingService.instance != null) {
            stopService(new Intent(this, FloatingService.class));
        }
        finishAffinity();
        System.exit(0);
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
        TextConfig.bgEnabled = prefs.getBoolean("text_bg_enabled", false);
        TextConfig.bgColor = prefs.getInt("text_bg_color", 0xCC000000);
        TextConfig.bgPadding = prefs.getInt("text_bg_padding", 25);
        TextConfig.bgOffsetX = prefs.getInt("text_bg_offset_x", 0);
        TextConfig.bgOffsetY = prefs.getInt("text_bg_offset_y", 0);
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
        FpsConfig.bgEnabled = prefs.getBoolean("fps_bg_enabled", false);
        FpsConfig.bgColor = prefs.getInt("fps_bg_color", 0xCC000000);
        FpsConfig.bgPadding = prefs.getInt("fps_bg_padding", 10);
        FpsConfig.bgOffsetX = prefs.getInt("fps_bg_offset_x", 0);
        FpsConfig.bgOffsetY = prefs.getInt("fps_bg_offset_y", 0);
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
        ClockConfig.bgEnabled = prefs.getBoolean("clock_bg_enabled", false);
        ClockConfig.bgColor = prefs.getInt("clock_bg_color", 0xCC000000);
        ClockConfig.bgPadding = prefs.getInt("clock_bg_padding", 10);

        NetworkConfig.enabled = prefs.getBoolean("network_enabled", false);
        NetworkConfig.color = prefs.getInt("network_color", Color.GREEN);
        NetworkConfig.shadow.enabled = prefs.getBoolean("network_shadow_enabled", false);
        NetworkConfig.shadow.color = prefs.getInt("network_shadow_color", Color.BLACK);
        NetworkConfig.shadow.blur = prefs.getFloat("network_shadow_blur", 5f);
        NetworkConfig.shadow.offsetX = prefs.getFloat("network_shadow_offset_x", 3f);
        NetworkConfig.shadow.offsetY = prefs.getFloat("network_shadow_offset_y", 3f);
        NetworkConfig.safeArea = prefs.getBoolean("network_safe_area", true);
        NetworkConfig.touchPassthrough = prefs.getBoolean("network_lock", true);
        NetworkConfig.bgEnabled = prefs.getBoolean("network_bg_enabled", false);
        NetworkConfig.bgColor = prefs.getInt("network_bg_color", 0xCC000000);
        NetworkConfig.bgPadding = prefs.getInt("network_bg_padding", 8);

        BatteryCurrentConfig.enabled = prefs.getBoolean("batcur_enabled", false);
        BatteryCurrentConfig.color = prefs.getInt("batcur_color", Color.GREEN);
        BatteryCurrentConfig.shadow.enabled = prefs.getBoolean("batcur_shadow_enabled", false);
        BatteryCurrentConfig.shadow.color = prefs.getInt("batcur_shadow_color", Color.BLACK);
        BatteryCurrentConfig.shadow.blur = prefs.getFloat("batcur_shadow_blur", 5f);
        BatteryCurrentConfig.shadow.offsetX = prefs.getFloat("batcur_shadow_offset_x", 3f);
        BatteryCurrentConfig.shadow.offsetY = prefs.getFloat("batcur_shadow_offset_y", 3f);
        BatteryCurrentConfig.safeArea = prefs.getBoolean("batcur_safe_area", true);
        BatteryCurrentConfig.touchPassthrough = prefs.getBoolean("batcur_lock", true);
        BatteryCurrentConfig.bgEnabled = prefs.getBoolean("batcur_bg_enabled", false);
        BatteryCurrentConfig.bgColor = prefs.getInt("batcur_bg_color", 0xCC000000);
        BatteryCurrentConfig.bgPadding = prefs.getInt("batcur_bg_padding", 8);
        BatteryCurrentConfig.showVoltage = prefs.getBoolean("batcur_show_voltage", true);
        BatteryCurrentConfig.showCurrent = prefs.getBoolean("batcur_show_current", true);
        BatteryCurrentConfig.showPower = prefs.getBoolean("batcur_show_power", true);

        BatteryConfig.enabled = prefs.getBoolean("battery_enabled", false);
        BatteryConfig.color = prefs.getInt("battery_color", Color.GREEN);
        BatteryConfig.shadow.enabled = prefs.getBoolean("battery_shadow_enabled", false);
        BatteryConfig.shadow.color = prefs.getInt("battery_shadow_color", Color.BLACK);
        BatteryConfig.shadow.blur = prefs.getFloat("battery_shadow_blur", 5f);
        BatteryConfig.shadow.offsetX = prefs.getFloat("battery_shadow_offset_x", 3f);
        BatteryConfig.shadow.offsetY = prefs.getFloat("battery_shadow_offset_y", 3f);
        BatteryConfig.safeArea = prefs.getBoolean("battery_safe_area", true);
        BatteryConfig.touchPassthrough = prefs.getBoolean("battery_lock", true);
        BatteryConfig.showOnlyValue = prefs.getBoolean("battery_show_only_value", false);
        BatteryConfig.showTemperature = prefs.getBoolean("battery_show_temperature", true);
        BatteryConfig.showPercentage = prefs.getBoolean("battery_show_percentage", false);
        BatteryConfig.bgEnabled = prefs.getBoolean("battery_bg_enabled", false);
        BatteryConfig.bgColor = prefs.getInt("battery_bg_color", 0xCC000000);
        BatteryConfig.bgPadding = prefs.getInt("battery_bg_padding", 8);
    }

    private void updateNavSelection(int selectedId) {
        int[] allIds = {R.id.navFloatingText, R.id.navFps, R.id.navNetwork, R.id.navBattery,
                R.id.navBatteryCurrent, R.id.navClock, R.id.navCrosshair, R.id.navWatermark, R.id.navLogo};
        for (int id : allIds) {
            View v = findViewById(id);
            if (v != null) {
                v.setSelected(v.getId() == selectedId);
            }
        }
    }

    // ========================================================================
    // Sidebar — flat list item (tanpa grup)
    // ========================================================================

    private void setupDrawerAllItemsDrag() {
        for (int i = 0; i < navItemContainer.getChildCount(); i++) {
            View child = navItemContainer.getChildAt(i);
            makeDraggable(child);
        }
        setupDragTarget(navItemContainer);
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

    private void makeDraggable(View view) {
        view.setOnLongClickListener(v -> {
            ClipData data = ClipData.newPlainText("", "");
            View.DragShadowBuilder shadow = new View.DragShadowBuilder(v);
            v.startDragAndDrop(data, shadow, v, 0);
            v.setVisibility(View.INVISIBLE);
            return true;
        });
    }

    private void setupDragTarget(ViewGroup container) {
        container.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return true;

                case DragEvent.ACTION_DRAG_ENTERED:
                case DragEvent.ACTION_DRAG_EXITED:
                    return true;

                case DragEvent.ACTION_DROP: {
                    View dragged = (View) event.getLocalState();
                    if (dragged == null) return true;
                    ViewGroup dropTarget = (ViewGroup) v;
                    ViewGroup oldParent = (ViewGroup) dragged.getParent();
                    if (oldParent == null) return true;

                    float dropY = event.getY();
                    int targetIdx = -1;
                    for (int i = 0; i < dropTarget.getChildCount(); i++) {
                        View child = dropTarget.getChildAt(i);
                        float midY = child.getY() + child.getHeight() / 2f;
                        if (dropY < midY) {
                            targetIdx = i;
                            break;
                        }
                    }
                    if (targetIdx == -1) {
                        targetIdx = dropTarget.getChildCount();
                    }

                    if (oldParent == dropTarget) {
                        int draggedIdx = dropTarget.indexOfChild(dragged);
                        if (draggedIdx >= 0 && draggedIdx != targetIdx) {
                            if (draggedIdx < targetIdx) targetIdx--;
                            dropTarget.removeView(dragged);
                            dropTarget.addView(dragged, targetIdx);
                        }
                    } else {
                        oldParent.removeView(dragged);
                        dropTarget.addView(dragged, targetIdx);
                    }
                    saveSidebarState();
                    dragged.setVisibility(View.VISIBLE);
                    dragged.setAlpha(1f);
                    return true;
                }

                case DragEvent.ACTION_DRAG_ENDED: {
                    View dv = (View) event.getLocalState();
                    if (dv != null) {
                        dv.setVisibility(View.VISIBLE);
                        dv.setAlpha(1f);
                    }
                    return true;
                }
            }
            return false;
        });
    }

    // ========================================================================
    // Persistensi sidebar — flat JSON array
    // ========================================================================

    private void saveSidebarState() {
        JSONArray arr = new JSONArray();
        for (int i = 0; i < navItemContainer.getChildCount(); i++) {
            View v = navItemContainer.getChildAt(i);
            if (v instanceof TextView) {
                try {
                    JSONObject item = new JSONObject();
                    String text = ((TextView) v).getText().toString();
                    int id = v.getId();
                    if (id != View.NO_ID) {
                        String resName = getResources().getResourceEntryName(id);
                        item.put("id", resName);
                    }
                    item.put("l", text);
                    arr.put(item);
                } catch (Exception e) { /* skip */ }
            }
        }
        getSharedPreferences(PREFS_SIDEBAR_STATE, MODE_PRIVATE)
                .edit().putString("sidebar_json", arr.toString()).apply();
    }

    private String loadSidebarState() {
        String json = getSharedPreferences(PREFS_SIDEBAR_STATE, MODE_PRIVATE)
                .getString("sidebar_json", null);
        return json != null ? json : DEFAULT_SIDEBAR_JSON;
    }

    private void rebuildSidebar() {
        navItemContainer.removeAllViews();

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

            for (int i = 0; i < arr.length(); i++) {
                JSONObject item = arr.getJSONObject(i);
                String label = item.getString("l");
                String id = item.optString("id", null);
                navItemContainer.addView(buildSidebarItem(label, id));
            }
        } catch (Exception e) {
            try {
                JSONArray def = new JSONArray(DEFAULT_SIDEBAR_JSON);
                for (int i = 0; i < def.length(); i++) {
                    JSONObject item = def.getJSONObject(i);
                    String label = item.getString("l");
                    String id = item.optString("id", null);
                    navItemContainer.addView(buildSidebarItem(label, id));
                }
            } catch (Exception e2) { /* ignore */ }
        }

        setupDrawerAllItemsDrag();
    }

    private TextView buildSidebarItem(String label, String id) {
        TextView tv = new TextView(this);
        tv.setText(label);
        tv.setTextSize(16);
        tv.setPadding(dp(16), dp(12), dp(16), dp(12));
        tv.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tv.setBackgroundResource(resolveSelectableItemBackground());
        tv.setClickable(true);
        tv.setFocusable(true);

        if (id != null) {
            int idRes = getResources().getIdentifier(id, "id", getPackageName());
            if (idRes != 0) tv.setId(idRes);
        }

        tv.setOnClickListener(v -> {
            int itemId = v.getId();
            updateNavSelection(itemId);

            getSharedPreferences("ftxt_prefs", MODE_PRIVATE)
                    .edit().putInt("nav_selected_item", itemId).apply();

            hideAllPanels();
            if (itemId == R.id.navFps) {
                panelFps.setVisibility(View.VISIBLE);
                getSupportActionBar().setTitle(R.string.nav_fps);
            } else if (itemId == R.id.navBattery) {
                panelBattery.setVisibility(View.VISIBLE);
                getSupportActionBar().setTitle(R.string.nav_battery);
            } else if (itemId == R.id.navBatteryCurrent) {
                panelBatteryCurrent.setVisibility(View.VISIBLE);
                getSupportActionBar().setTitle(R.string.nav_battery_current);
            } else if (itemId == R.id.navClock) {
                panelClock.setVisibility(View.VISIBLE);
                getSupportActionBar().setTitle(R.string.nav_clock);
            } else if (itemId == R.id.navNetwork) {
                panelNetwork.setVisibility(View.VISIBLE);
                getSupportActionBar().setTitle(R.string.nav_network);
            } else if (itemId == R.id.navCrosshair) {
                panelCrosshair.setVisibility(View.VISIBLE);
                getSupportActionBar().setTitle("Crosshair");
            } else if (itemId == R.id.navWatermark) {
                panelWatermark.setVisibility(View.VISIBLE);
                getSupportActionBar().setTitle("Watermark");
            } else if (itemId == R.id.navLogo) {
                panelLogo.setVisibility(View.VISIBLE);
                getSupportActionBar().setTitle("Logo Display");
            } else {
                panelText.setVisibility(View.VISIBLE);
                getSupportActionBar().setTitle(R.string.nav_floating_text);
            }

            DrawerLayout drawer = findViewById(R.id.drawerLayout);
            drawer.closeDrawers();
        });

        makeDraggable(tv);
        return tv;
    }

    private void hideAllPanels() {
        panelText.setVisibility(View.GONE);
        panelFps.setVisibility(View.GONE);
        panelClock.setVisibility(View.GONE);
        panelBattery.setVisibility(View.GONE);
        panelBatteryCurrent.setVisibility(View.GONE);
        panelNetwork.setVisibility(View.GONE);
        panelCrosshair.setVisibility(View.GONE);
        panelWatermark.setVisibility(View.GONE);
        panelLogo.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textPanel != null) textPanel.cleanup();
        if (fpsPanel != null) fpsPanel.cleanup();
        if (clockPanel != null) clockPanel.cleanup();
        if (batteryPanel != null) batteryPanel.cleanup();
        if (batteryCurrentPanel != null) batteryCurrentPanel.cleanup();
        if (networkPanel != null) networkPanel.cleanup();
    }

    private int dp(float dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
