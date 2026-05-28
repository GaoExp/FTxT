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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.splashscreen.SplashScreen;
import androidx.drawerlayout.widget.DrawerLayout;

import exp.ftxt.core.FloatingService;
import exp.ftxt.modules.fps.FpsConfig;
import exp.ftxt.modules.text.TextConfig;
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
    private Button btnTambahGrup;
    private static final String PREFS_CUSTOM_GROUPS = "custom_groups";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize SplashScreen API (must be called before setContentView)
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

        // Navigation drawer header
        TextView navTitle = findViewById(R.id.navHeaderTitle);
        try {
            String v = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            navTitle.setText("FTxT v" + v);
        } catch (PackageManager.NameNotFoundException e) {
            navTitle.setText("FTxT");
        }

        // Collapsible groups
        LinearLayout overlayContent = findViewById(R.id.groupOverlayContent);
        TextView overlayIndicator = findViewById(R.id.groupOverlayIndicator);
        LinearLayout fiturContent = findViewById(R.id.groupFiturContent);
        TextView fiturIndicator = findViewById(R.id.groupFiturIndicator);

        findViewById(R.id.groupOverlayHeader).setOnClickListener(v -> {
            boolean visible = overlayContent.getVisibility() == View.VISIBLE;
            overlayContent.setVisibility(visible ? View.GONE : View.VISIBLE);
            overlayIndicator.setText(visible ? "+" : "−");
        });

        findViewById(R.id.groupFiturHeader).setOnClickListener(v -> {
            boolean visible = fiturContent.getVisibility() == View.VISIBLE;
            fiturContent.setVisibility(visible ? View.GONE : View.VISIBLE);
            fiturIndicator.setText(visible ? "+" : "−");
        });

        // Drawer item click listener
        View.OnClickListener navItemListener = v -> {
            int id = v.getId();
            prefs.edit().putInt("nav_selected_item", id).apply();
            updateNavSelection(id);

            if (id == R.id.navFloatingText) {
                panelText.setVisibility(View.VISIBLE);
                panelFps.setVisibility(View.GONE);
                getSupportActionBar().setTitle(R.string.nav_floating_text);
                drawerLayout.closeDrawers();
                textPanel.onPanelShown();
                return;
            }

            if (id == R.id.navFps) {
                panelText.setVisibility(View.GONE);
                panelFps.setVisibility(View.VISIBLE);
                getSupportActionBar().setTitle(R.string.nav_fps);
                drawerLayout.closeDrawers();
                return;
            }

            if (id == R.id.navDokumentasi) {
                drawerLayout.closeDrawers();
                startActivity(new Intent(this, DocumentationActivity.class));
                return;
            }

            if (id == R.id.navNetwork || id == R.id.navBattery || id == R.id.navClock
                    || id == R.id.navCpu || id == R.id.navCrosshair || id == R.id.navWatermark || id == R.id.navLogo) {
                Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show();
                drawerLayout.closeDrawers();
            }
        };

        findViewById(R.id.navFloatingText).setOnClickListener(navItemListener);
        findViewById(R.id.navFps).setOnClickListener(navItemListener);
        findViewById(R.id.navNetwork).setOnClickListener(navItemListener);
        findViewById(R.id.navBattery).setOnClickListener(navItemListener);
        findViewById(R.id.navClock).setOnClickListener(navItemListener);
        findViewById(R.id.navCpu).setOnClickListener(navItemListener);
        findViewById(R.id.navCrosshair).setOnClickListener(navItemListener);
        findViewById(R.id.navWatermark).setOnClickListener(navItemListener);
        findViewById(R.id.navLogo).setOnClickListener(navItemListener);
        findViewById(R.id.navDokumentasi).setOnClickListener(navItemListener);

        updateNavSelection(savedNavItem);

        navItemContainer = findViewById(R.id.navItemContainer);
        btnTambahGrup = findViewById(R.id.btnTambahGrup);
        btnTambahGrup.setOnClickListener(v -> showAddGroupDialog());
        setupDrawerAllItemsDrag();
        loadCustomGroups();

        loadShadowConfigs();

        // Init panel controllers
        // TextPanelController: binding + listener untuk Floating Text panel
        // FpsPanelController:  binding + listener untuk FPS Display panel
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
                R.id.navClock, R.id.navCpu, R.id.navCrosshair, R.id.navWatermark, R.id.navLogo, R.id.navDokumentasi};
        for (int id : allIds) {
            View v = findViewById(id);
            if (v != null) {
                v.setSelected(v.getId() == selectedId);
            }
        }
    }

    // ========================================================================
    // Tambah Grup — dialog untuk membuat grup kustom di sidebar
    // ========================================================================

    private void showAddGroupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tambah Grup Baru");

        EditText input = new EditText(this);
        input.setHint("Nama grup");
        int pad = dp(20);
        input.setPadding(pad, pad / 2, pad, pad / 2);
        builder.setView(input);

        builder.setPositiveButton("Tambah", (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (!name.isEmpty()) {
                addCustomGroup(name);
            }
        });
        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    private void addCustomGroup(String name) {
        LinearLayout groupSection = createDynamicGroup(name, new String[0]);
        navItemContainer.addView(groupSection);

        saveCustomGroups();
    }

    private boolean isLoadingGroups = false;

    private LinearLayout createDynamicGroup(String name, String[] items) {
        int groupId = View.generateViewId();
        int contentId = View.generateViewId();
        int indicatorId = View.generateViewId();

        LinearLayout section = new LinearLayout(this);
        section.setOrientation(LinearLayout.VERTICAL);
        section.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Divider
        View divider = new View(this);
        divider.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 1));
        divider.setBackgroundColor(0xFFE0E0E0);
        section.addView(divider);
        section.setTag("custom_group");

        // Group header
        LinearLayout header = new LinearLayout(this);
        header.setId(groupId);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setPadding(dp(16), dp(16), dp(16), dp(16));
        header.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        header.setClickable(true);
        header.setFocusable(true);
        header.setBackgroundResource(android.R.attr.selectableItemBackground);

        TextView title = new TextView(this);
        title.setText(name);
        title.setTextSize(14);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setTextColor(0xFF333333);
        title.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        TextView indicator = new TextView(this);
        indicator.setId(indicatorId);
        indicator.setText("−");
        indicator.setTextSize(16);
        indicator.setTextColor(0xFF999999);

        header.addView(title);
        header.addView(indicator);
        section.addView(header);

        // Content
        LinearLayout content = new LinearLayout(this);
        content.setId(contentId);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        for (String item : items) {
            addItemToGroup(content, item);
        }

        // Add item button inside group
        LinearLayout addRow = new LinearLayout(this);
        addRow.setOrientation(LinearLayout.HORIZONTAL);
        addRow.setPadding(dp(32), dp(4), dp(16), dp(12));

        EditText itemInput = new EditText(this);
        itemInput.setHint("Tambah item...");
        itemInput.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        itemInput.setTextSize(14);

        Button addBtn = new Button(this);
        addBtn.setText("+");
        addBtn.setTextSize(14);
        addBtn.setOnClickListener(v -> {
            String itemName = itemInput.getText().toString().trim();
            if (!itemName.isEmpty()) {
                addItemToGroup(content, itemName);
                itemInput.setText("");
                saveCustomGroups();
            }
        });

        addRow.addView(itemInput);
        addRow.addView(addBtn);
        content.addView(addRow);

        section.addView(content);

        // Collapse/expand toggle
        header.setOnClickListener(v -> {
            boolean visible = content.getVisibility() == View.VISIBLE;
            content.setVisibility(visible ? View.GONE : View.VISIBLE);
            indicator.setText(visible ? "+" : "−");
        });

        return section;
    }

    private void addItemToGroup(LinearLayout content, String itemText) {
        int insertPos = content.getChildCount() - 1; // before add-item row
        TextView item = new TextView(this);
        item.setText(itemText);
        item.setTextSize(16);
        item.setPadding(dp(32), dp(12), dp(16), dp(12));
        item.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        item.setBackgroundResource(android.R.attr.selectableItemBackground);
        item.setClickable(true);
        item.setFocusable(true);
        item.setLongClickable(true);

        makeDraggable(item);
        content.addView(item, insertPos);

        if (!isLoadingGroups) saveCustomGroups();
    }

    // ========================================================================
    // Drag-to-reorder — long-press untuk memindahkan item
    // ========================================================================

    private void setupDrawerAllItemsDrag() {
        // Setup drag on all built-in items in Overlay group
        LinearLayout overlayContent = findViewById(R.id.groupOverlayContent);
        for (int i = 0; i < overlayContent.getChildCount(); i++) {
            View child = overlayContent.getChildAt(i);
            child.setLongClickable(true);
            makeDraggable(child);
        }

        // Setup drag on all built-in items in Fitur group
        LinearLayout fiturContent = findViewById(R.id.groupFiturContent);
        for (int i = 0; i < fiturContent.getChildCount(); i++) {
            View child = fiturContent.getChildAt(i);
            child.setLongClickable(true);
            makeDraggable(child);
        }
    }

    private void makeDraggable(View view) {
        view.setOnLongClickListener(v -> {
            ClipData data = ClipData.newPlainText("", "");
            View.DragShadowBuilder shadow = new View.DragShadowBuilder(v);
            v.startDragAndDrop(data, shadow, v, 0);
            v.setVisibility(View.INVISIBLE);
            return true;
        });

        view.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setAlpha(0.5f);
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    v.setAlpha(1f);
                    return true;
                case DragEvent.ACTION_DROP: {
                    View dragged = (View) event.getLocalState();
                    ViewGroup parent = (ViewGroup) v.getParent();
                    if (parent != null && dragged.getParent() == parent) {
                        int draggedIdx = parent.indexOfChild(dragged);
                        int targetIdx = parent.indexOfChild(v);
                        if (draggedIdx >= 0 && targetIdx >= 0 && draggedIdx != targetIdx) {
                            parent.removeView(dragged);
                            parent.addView(dragged, targetIdx);
                            saveCustomGroups();
                        }
                    }
                    dragged.setVisibility(View.VISIBLE);
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
    // Persistensi grup kustom ke SharedPreferences (JSON)
    // ========================================================================

    private void saveCustomGroups() {
        JSONArray arr = new JSONArray();
        for (int i = 0; i < navItemContainer.getChildCount(); i++) {
            View child = navItemContainer.getChildAt(i);
            if ("custom_group".equals(child.getTag()) && child instanceof LinearLayout) {
                LinearLayout section = (LinearLayout) child;
                if (section.getChildCount() >= 3) {
                    LinearLayout header = (LinearLayout) section.getChildAt(1);
                    if (header.getChildCount() >= 1 && header.getChildAt(0) instanceof TextView) {
                        String name = ((TextView) header.getChildAt(0)).getText().toString();
                        LinearLayout content = (LinearLayout) section.getChildAt(2);
                        JSONArray itemsArr = new JSONArray();
                        for (int j = 0; j < content.getChildCount() - 1; j++) {
                            View itemView = content.getChildAt(j);
                            if (itemView instanceof TextView) {
                                itemsArr.put(((TextView) itemView).getText().toString());
                            }
                        }
                        try {
                            JSONObject obj = new JSONObject();
                            obj.put("name", name);
                            obj.put("items", itemsArr);
                            arr.put(obj);
                        } catch (Exception e) {
                            // skip
                        }
                    }
                }
            }
        }

        getSharedPreferences("ftxt_prefs", MODE_PRIVATE)
                .edit().putString(PREFS_CUSTOM_GROUPS, arr.toString()).apply();
    }

    private void loadCustomGroups() {
        isLoadingGroups = true;
        String json = getSharedPreferences("ftxt_prefs", MODE_PRIVATE)
                .getString(PREFS_CUSTOM_GROUPS, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                String name = obj.getString("name");
                JSONArray itemsArr = obj.getJSONArray("items");
                String[] items = new String[itemsArr.length()];
                for (int j = 0; j < itemsArr.length(); j++) {
                    items[j] = itemsArr.getString(j);
                }
                LinearLayout section = createDynamicGroup(name, items);
                navItemContainer.addView(section);
            }
        } catch (Exception e) {
            // ignore
        }
        isLoadingGroups = false;
    }

    private int dp(float dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
