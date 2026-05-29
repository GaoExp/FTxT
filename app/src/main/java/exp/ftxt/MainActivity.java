package exp.ftxt;

import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.util.TypedValue;
import android.graphics.Color;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Switch;
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
import exp.ftxt.features.fps.FpsConfig;
import exp.ftxt.features.text.TextConfig;
import exp.ftxt.ui.FpsPanelController;
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

    private TextPanelController textPanel;
    private FpsPanelController fpsPanel;

    private LinearLayout navItemContainer;
    private static final String PREFS_SIDEBAR_STATE = "sidebar_state";

    private static final String DEFAULT_SIDEBAR_JSON =
        "[{\"id\":\"navFloatingText\",\"l\":\"Floating Text\"}," +
        "{\"id\":\"navFps\",\"l\":\"FPS Display\"}," +
        "{\"id\":\"navNetwork\",\"l\":\"Network Stats\"}," +
        "{\"id\":\"navBattery\",\"l\":\"Battery Monitor\"}," +
        "{\"id\":\"navClock\",\"l\":\"Clock Module\"}," +
        "{\"id\":\"navCpu\",\"l\":\"CPU Monitor\"}," +
        "{\"id\":\"navCrosshair\",\"l\":\"Crosshair\"}," +
        "{\"id\":\"navWatermark\",\"l\":\"Watermark\"}," +
        "{\"id\":\"navLogo\",\"l\":\"Logo Display\"}]";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        boolean isDark = getSharedPreferences("ftxt_prefs", MODE_PRIVATE)
                .getBoolean("theme_dark", false);

        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

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

        SharedPreferences prefs = getSharedPreferences("ftxt_prefs", MODE_PRIVATE);
        int savedNavItem = prefs.getInt("nav_selected_item", R.id.navFloatingText);
        if (savedNavItem == R.id.navFps) {
            panelText.setVisibility(View.GONE);
            panelFps.setVisibility(View.VISIBLE);
            getSupportActionBar().setTitle(R.string.nav_fps);
        }

        TextView navTitle = findViewById(R.id.navHeaderTitle);
        try {
            String v = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            navTitle.setText("FTxT v" + v);
        } catch (PackageManager.NameNotFoundException e) {
            navTitle.setText("FTxT");
        }

        navItemContainer = findViewById(R.id.navItemContainer);

        rebuildSidebar();

        loadShadowConfigs();

        textPanel = new TextPanelController(this);
        fpsPanel = new FpsPanelController(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (textPanel != null && panelText.getVisibility() == View.VISIBLE) {
            textPanel.onPanelShown();
        }
        autoRequestAndStart();
        FloatingService.updateTextPositionStatic();
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
                .getBoolean("theme_dark", false);
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

        if (id == R.id.action_theme) {
            boolean isDark = getSharedPreferences("ftxt_prefs", MODE_PRIVATE)
                    .getBoolean("theme_dark", false);
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
    // Switch tint utility — dipanggil oleh TextPanelController & FpsPanelController
    // Lihat: TextPanelController → ui/TextPanelController.java
    // Lihat: FpsPanelController  → ui/FpsPanelController.java
    // ========================================================================
    public void applySwitchTint(Switch sw, boolean isChecked) {
        if (isChecked) {
            sw.setThumbTintList(ColorStateList.valueOf(Color.parseColor("#2196F3")));
            sw.setTrackTintList(ColorStateList.valueOf(Color.parseColor("#90CAF9")));
        } else {
            sw.setThumbTintList(ColorStateList.valueOf(Color.parseColor("#E53935")));
            sw.setTrackTintList(ColorStateList.valueOf(Color.parseColor("#EF9A9A")));
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
        popup.getMenu().add("Konfigurasi");
        popup.getMenu().add("Lihat Dokumentasi");
        popup.getMenu().add("Tutup Aplikasi");
        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            if (title.equals("Konfigurasi")) {
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
        FpsConfig.color = prefs.getInt("fps_color", Color.WHITE);

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
    }

    private void updateNavSelection(int selectedId) {
        int[] allIds = {R.id.navFloatingText, R.id.navFps, R.id.navNetwork, R.id.navBattery,
                R.id.navClock, R.id.navCpu, R.id.navCrosshair, R.id.navWatermark, R.id.navLogo};
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

            if (itemId == R.id.navFps) {
                panelText.setVisibility(View.GONE);
                panelFps.setVisibility(View.VISIBLE);
                getSupportActionBar().setTitle(R.string.nav_fps);
            } else {
                panelText.setVisibility(View.VISIBLE);
                panelFps.setVisibility(View.GONE);
                getSupportActionBar().setTitle(R.string.nav_floating_text);
            }

            DrawerLayout drawer = findViewById(R.id.drawerLayout);
            drawer.closeDrawers();
        });

        makeDraggable(tv);
        return tv;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textPanel != null) textPanel.cleanup();
    }

    private int dp(float dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
