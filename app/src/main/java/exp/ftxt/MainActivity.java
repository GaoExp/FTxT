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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
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
    private Button btnTambahGrup;
    private static final String PREFS_SIDEBAR_STATE = "sidebar_state";

    private boolean isDeleteMode = false;
    private final List<View> markedViews = new ArrayList<>();
    private View deleteFooterBar;
    private Button btnHapus;
    private Button btnBatal;
    private Button btnDeleteToggle;

    private static final String DEFAULT_SIDEBAR_JSON =
        "[{\"n\":\"Overlay\",\"b\":true,\"i\":[" +
            "{\"id\":\"navFloatingText\",\"l\":\"Floating Text\"}," +
            "{\"id\":\"navFps\",\"l\":\"FPS Display\"}]}," +
        "{\"n\":\"Fitur\",\"b\":true,\"i\":[" +
            "{\"id\":\"navNetwork\",\"l\":\"Network Stats\"}," +
            "{\"id\":\"navBattery\",\"l\":\"Battery Monitor\"}," +
            "{\"id\":\"navClock\",\"l\":\"Clock Module\"}," +
            "{\"id\":\"navCpu\",\"l\":\"CPU Monitor\"}," +
            "{\"id\":\"navCrosshair\",\"l\":\"Crosshair\"}," +
            "{\"id\":\"navWatermark\",\"l\":\"Watermark\"}," +
            "{\"id\":\"navLogo\",\"l\":\"Logo Display\"}]}]";

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
        btnTambahGrup = findViewById(R.id.btnTambahGrup);
        btnTambahGrup.setOnClickListener(v -> showAddGroupDialog());
        btnDeleteToggle = findViewById(R.id.btnDeleteMode);
        btnDeleteToggle.setOnClickListener(v -> toggleDeleteMode());

        deleteFooterBar = findViewById(R.id.deleteFooterBar);
        btnHapus = findViewById(R.id.btnHapus);
        btnBatal = findViewById(R.id.btnBatal);
        btnHapus.setOnClickListener(v -> executeDeletion());
        btnBatal.setOnClickListener(v -> toggleDeleteMode());

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

        saveSidebarState();
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
        header.setBackgroundResource(resolveSelectableItemBackground());

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

        section.addView(content);

        for (String item : items) {
            addItemToGroup(content, item);
        }

        setupDragTarget(content);

        // Collapse/expand toggle
        header.setOnClickListener(v -> {
            boolean visible = content.getVisibility() == View.VISIBLE;
            content.setVisibility(visible ? View.GONE : View.VISIBLE);
            indicator.setText(visible ? "+" : "−");
        });

        return section;
    }

    private void addItemToGroup(LinearLayout content, String itemText) {
        TextView item = new TextView(this);
        item.setText(itemText);
        item.setTextSize(16);
        item.setPadding(dp(32), dp(12), dp(16), dp(12));
        item.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        item.setBackgroundResource(resolveSelectableItemBackground());
        item.setClickable(true);
        item.setFocusable(true);

        makeDraggable(item);
        content.addView(item);

        if (!isLoadingGroups) saveSidebarState();
    }

    // ========================================================================
    // Drag-to-reorder — long-press untuk memindahkan item
    // ========================================================================

    private void setupDrawerAllItemsDrag() {
        // Setup drag on all built-in items in Overlay group
        LinearLayout overlayContent = findViewById(R.id.groupOverlayContent);
        for (int i = 0; i < overlayContent.getChildCount(); i++) {
            View child = overlayContent.getChildAt(i);
            makeDraggable(child);
        }
        setupDragTarget(overlayContent);

        // Setup drag on all built-in items in Fitur group
        LinearLayout fiturContent = findViewById(R.id.groupFiturContent);
        for (int i = 0; i < fiturContent.getChildCount(); i++) {
            View child = fiturContent.getChildAt(i);
            makeDraggable(child);
        }
        setupDragTarget(fiturContent);
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
    // Persistensi grup kustom ke SharedPreferences (JSON)
    // ========================================================================

    private void saveSidebarState() {
        JSONArray arr = new JSONArray();
        try {
            // Overlay group
            LinearLayout overlayContent = findViewById(R.id.groupOverlayContent);
            TextView overlayInd = findViewById(R.id.groupOverlayIndicator);
            JSONObject overlayGrp = buildGroupJson("Overlay", overlayContent,
                    overlayInd != null && "−".equals(overlayInd.getText().toString()), true);
            arr.put(overlayGrp);

            // Fitur group
            LinearLayout fiturContent = findViewById(R.id.groupFiturContent);
            TextView fiturInd = findViewById(R.id.groupFiturIndicator);
            JSONObject fiturGrp = buildGroupJson("Fitur", fiturContent,
                    fiturInd != null && "−".equals(fiturInd.getText().toString()), true);
            arr.put(fiturGrp);

            // Custom groups
            for (int i = 0; i < navItemContainer.getChildCount(); i++) {
                View child = navItemContainer.getChildAt(i);
                if ("custom_group".equals(child.getTag()) && child instanceof LinearLayout) {
                    LinearLayout section = (LinearLayout) child;
                    if (section.getChildCount() >= 3) {
                        LinearLayout header = (LinearLayout) section.getChildAt(1);
                        LinearLayout content = (LinearLayout) section.getChildAt(2);
                        if (header.getChildCount() >= 1 && header.getChildAt(0) instanceof TextView) {
                            String name = ((TextView) header.getChildAt(0)).getText().toString();
                            JSONObject grp = buildGroupJson(name, content,
                                    content.getVisibility() == View.VISIBLE, false);
                            arr.put(grp);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // skip
        }
        getSharedPreferences(PREFS_SIDEBAR_STATE, MODE_PRIVATE)
                .edit().putString("sidebar_json", arr.toString()).apply();
    }

    private JSONObject buildGroupJson(String name, LinearLayout content, boolean expanded, boolean builtIn) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("n", name);
            obj.put("b", expanded);
            JSONArray itemsArr = new JSONArray();
            for (int j = 0; j < content.getChildCount(); j++) {
                View v = content.getChildAt(j);
                if (v instanceof TextView) {
                    String text = ((TextView) v).getText().toString();
                    if (text.startsWith("☐ ") || text.startsWith("☑ ")) {
                        text = text.substring(2);
                    }
                    JSONObject itemObj = new JSONObject();
                    itemObj.put("l", text);
                    if (builtIn) {
                        int id = v.getId();
                        if (id != View.NO_ID) {
                            String resName = getResources().getResourceEntryName(id);
                            itemObj.put("id", resName);
                        }
                    }
                    itemsArr.put(itemObj);
                }
            }
            obj.put("i", itemsArr);
        } catch (Exception e) { /* skip */ }
        return obj;
    }

    private String loadSidebarState() {
        String json = getSharedPreferences(PREFS_SIDEBAR_STATE, MODE_PRIVATE)
                .getString("sidebar_json", null);
        return json != null ? json : DEFAULT_SIDEBAR_JSON;
    }

    // ========================================================================
    // Rebuild sidebar — baca state, hapus & buat ulang semua grup + item
    // ========================================================================

    private void rebuildSidebar() {
        isLoadingGroups = true;

        for (int i = navItemContainer.getChildCount() - 1; i >= 0; i--) {
            if ("custom_group".equals(navItemContainer.getChildAt(i).getTag())) {
                navItemContainer.removeViewAt(i);
            }
        }

        LinearLayout overlayContent = findViewById(R.id.groupOverlayContent);
        LinearLayout fiturContent = findViewById(R.id.groupFiturContent);
        overlayContent.removeAllViews();
        fiturContent.removeAllViews();

        try {
            JSONArray arr = new JSONArray(loadSidebarState());
            for (int i = 0; i < arr.length(); i++) {
                JSONObject g = arr.getJSONObject(i);
                String name = g.getString("n");
                boolean expanded = g.getBoolean("b");
                JSONArray items = g.getJSONArray("i");

                if ("Overlay".equals(name)) {
                    repopulateGroup(overlayContent, items);
                    applyExpandedState(R.id.groupOverlayHeader, overlayContent, R.id.groupOverlayIndicator, expanded);
                } else if ("Fitur".equals(name)) {
                    repopulateGroup(fiturContent, items);
                    applyExpandedState(R.id.groupFiturHeader, fiturContent, R.id.groupFiturIndicator, expanded);
                } else {
                    String[] labels = new String[items.length()];
                    for (int j = 0; j < items.length(); j++) {
                        labels[j] = items.getJSONObject(j).getString("l");
                    }
                    LinearLayout section = createDynamicGroup(name, labels);
                    View content = section.getChildAt(2);
                    if (content != null) {
                        content.setVisibility(expanded ? View.VISIBLE : View.GONE);
                        View header = section.getChildAt(1);
                        if (header instanceof LinearLayout) {
                            LinearLayout headerLayout = (LinearLayout) header;
                            if (headerLayout.getChildCount() >= 2 && headerLayout.getChildAt(1) instanceof TextView) {
                                ((TextView) headerLayout.getChildAt(1)).setText(expanded ? "−" : "+");
                            }
                        }
                    }
                    navItemContainer.addView(section);
                }
            }
        } catch (Exception e) {
            // fallback — state rusak, rebuild dari default
            try {
                JSONArray def = new JSONArray(DEFAULT_SIDEBAR_JSON);
                for (int i = 0; i < def.length(); i++) {
                    JSONObject g = def.getJSONObject(i);
                    JSONArray items = g.getJSONArray("i");
                    if ("Overlay".equals(g.getString("n"))) {
                        repopulateGroup(overlayContent, items);
                    } else if ("Fitur".equals(g.getString("n"))) {
                        repopulateGroup(fiturContent, items);
                    }
                }
            } catch (Exception e2) { /* ignore */ }
        }

        setupBuiltInGroupToggle(R.id.groupOverlayHeader, overlayContent, R.id.groupOverlayIndicator);
        setupBuiltInGroupToggle(R.id.groupFiturHeader, fiturContent, R.id.groupFiturIndicator);
        setupDrawerAllItemsDrag();

        if (isDeleteMode) {
            applyDeleteModeToContainer(overlayContent);
            applyDeleteModeToContainer(fiturContent);
            for (int i = 0; i < navItemContainer.getChildCount(); i++) {
                View child = navItemContainer.getChildAt(i);
                if ("custom_group".equals(child.getTag()) && child instanceof LinearLayout) {
                    LinearLayout section = (LinearLayout) child;
                    if (section.getChildCount() >= 3 && section.getChildAt(2) instanceof LinearLayout) {
                        applyDeleteModeToContainer((LinearLayout) section.getChildAt(2));
                    }
                }
            }
        }

        isLoadingGroups = false;
    }

    private void repopulateGroup(LinearLayout container, JSONArray items) {
        container.removeAllViews();
        for (int i = 0; i < items.length(); i++) {
            try {
                JSONObject item = items.getJSONObject(i);
                String label = item.getString("l");
                String id = item.optString("id", null);

                TextView tv = new TextView(this);
                tv.setText(label);
                tv.setTextSize(16);
                tv.setPadding(dp(32), dp(12), dp(16), dp(12));
                tv.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                tv.setBackgroundResource(resolveSelectableItemBackground());
                tv.setClickable(true);
                tv.setFocusable(true);

                if (id != null) {
                    int idRes = getResources().getIdentifier(id, "id", getPackageName());
                    if (idRes != 0) tv.setId(idRes);
                }

                makeDraggable(tv);
                container.addView(tv);
            } catch (Exception e) { /* skip */ }
        }
    }

    private void applyExpandedState(int headerId, ViewGroup content, int indicatorId, boolean expanded) {
        View indicator = findViewById(indicatorId);
        if (indicator instanceof TextView) {
            ((TextView) indicator).setText(expanded ? "−" : "+");
        }
        content.setVisibility(expanded ? View.VISIBLE : View.GONE);
    }

    private void setupBuiltInGroupToggle(int headerId, ViewGroup content, int indicatorId) {
        View header = findViewById(headerId);
        TextView indicator = findViewById(indicatorId);
        if (header != null && indicator != null) {
            header.setOnClickListener(v -> {
                boolean visible = content.getVisibility() == View.VISIBLE;
                content.setVisibility(visible ? View.GONE : View.VISIBLE);
                indicator.setText(visible ? "+" : "−");
                saveSidebarState();
            });
        }
    }

    // ========================================================================
    // Delete mode — CheckBox + footer [Batal][Hapus]
    // ========================================================================

    private void toggleDeleteMode() {
        isDeleteMode = !isDeleteMode;
        deleteFooterBar.setVisibility(isDeleteMode ? View.VISIBLE : View.GONE);
        btnDeleteToggle.setText(isDeleteMode ? "Batal" : "Hapus");

        if (!isDeleteMode) markedViews.clear();

        applyDeleteModeToContainer(findViewById(R.id.groupOverlayContent));
        applyDeleteModeToContainer(findViewById(R.id.groupFiturContent));

        for (int i = 0; i < navItemContainer.getChildCount(); i++) {
            View child = navItemContainer.getChildAt(i);
            if ("custom_group".equals(child.getTag()) && child instanceof LinearLayout) {
                LinearLayout section = (LinearLayout) child;
                if (section.getChildCount() >= 3 && section.getChildAt(2) instanceof LinearLayout) {
                    applyDeleteModeToContainer((LinearLayout) section.getChildAt(2));
                }
            }
        }
    }

    private void toggleItemMark(TextView tv) {
        if (markedViews.contains(tv)) {
            markedViews.remove(tv);
            tv.setBackgroundResource(resolveSelectableItemBackground());
            String t = tv.getText().toString();
            if (t.startsWith("☑ ")) tv.setText("☐ " + t.substring(2));
        } else {
            markedViews.add(tv);
            tv.setBackgroundColor(0xFFFFEBEE);
            String t = tv.getText().toString();
            tv.setText((t.startsWith("☐ ") ? "☑ " : "☑ ") + (t.startsWith("☐ ") || t.startsWith("☑ ") ? t.substring(2) : t));
        }
    }

    private void applyDeleteModeToContainer(LinearLayout container) {
        if (container == null) return;
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof TextView) {
                TextView tv = (TextView) child;
                if (isDeleteMode) {
                    String t = tv.getText().toString();
                    if (!t.startsWith("☐ ") && !t.startsWith("☑ ")) tv.setText("☐ " + t);
                    tv.setOnClickListener(v -> toggleItemMark(tv));
                } else {
                    String t = tv.getText().toString();
                    if (t.startsWith("☐ ") || t.startsWith("☑ ")) tv.setText(t.substring(2));
                    tv.setOnClickListener(null);
                    tv.setBackgroundResource(resolveSelectableItemBackground());
                }
            }
        }
    }

    private void executeDeletion() {
        if (markedViews.isEmpty()) {
            Toast.makeText(this, "Pilih item yang ingin dihapus", Toast.LENGTH_SHORT).show();
            return;
        }

        for (View v : markedViews) {
            ViewGroup parent = (ViewGroup) v.getParent();
            if (parent != null) parent.removeView(v);
        }
        markedViews.clear();
        isDeleteMode = false;
        btnDeleteToggle.setText("Hapus");
        deleteFooterBar.setVisibility(View.GONE);

        saveSidebarState();
        rebuildSidebar();
    }

    private int dp(float dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
