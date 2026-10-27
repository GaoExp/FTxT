package exp.ftxt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.net.TrafficStats;
import android.net.Uri;
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

import exp.ftxt.core.ConfigLoader;
import exp.ftxt.core.CrashLogger;
import exp.ftxt.core.FloatingService;
import exp.ftxt.features.network_stats.DataUsageTracker;
import exp.ftxt.ui.PanelManager;
import exp.ftxt.utils.ApkDownloadHelper;
import exp.ftxt.utils.PermissionHelper;
import exp.ftxt.utils.UpdateChecker;

public class MainActivity extends AppCompatActivity {

    private PanelManager panelManager;

    private RecyclerView navItemContainer;
    private SidebarAdapter sidebarAdapter;
    private ItemTouchHelper sidebarTouchHelper;
    private static final String PREFS_SIDEBAR_STATE = "sidebar_state";

    private BroadcastReceiver panelVisibilityReceiver;

    private static final String DEFAULT_SIDEBAR_JSON =
        "[{\"id\":\"navFloatingText\",\"l\":\"Floating Text\"}," +
        "{\"id\":\"navFps\",\"l\":\"FPS Display\"}," +
        "{\"id\":\"navNetwork\",\"l\":\"Network Stats\"}," +
        "{\"id\":\"navBattery\",\"l\":\"Battery Info\"}," +
        "{\"id\":\"navClock\",\"l\":\"Clock Module\"}," +
        "{\"id\":\"navCrosshair\",\"l\":\"Crosshair\"}," +
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

        findViewById(R.id.navHeaderLayout).setOnClickListener(v -> {
            startActivity(new Intent(this, AboutActivity.class));
            DrawerLayout drawer = findViewById(R.id.drawerLayout);
            drawer.closeDrawers();
        });

        navItemContainer = findViewById(R.id.navItemContainer);

        initSidebar();
        updateNavSelection(savedNavItem);

        findViewById(R.id.navTutupAplikasi).setOnClickListener(v -> forceClose());
        findViewById(R.id.navKeluar).setOnClickListener(v -> finishAffinity());

        loadShadowConfigs();

        panelVisibilityReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (SettingsActivity.ACTION_PANEL_VISIBILITY_CHANGED.equals(intent.getAction())) {
                    String panelId = intent.getStringExtra(SettingsActivity.EXTRA_PANEL_ID);
                    if (panelId != null && panelManager != null) {
                        String currentPanel = panelManager.getCurrentPanel();
                        if (panelId.equals(currentPanel)) {
                            panelManager.showPanel("text");
                            getSharedPreferences("ftxt_prefs", MODE_PRIVATE)
                                    .edit().putInt("nav_selected_item", R.id.navFloatingText).apply();
                            updateNavSelection(R.id.navFloatingText);
                            updateActionBarTitle(R.id.navFloatingText);
                        }
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter(SettingsActivity.ACTION_PANEL_VISIBILITY_CHANGED);
        registerReceiver(panelVisibilityReceiver, filter, Context.RECEIVER_NOT_EXPORTED);

        requestAllPermissionsOnFirstLaunch();

        triggerAutoUpdateCheck();
    }

    private void triggerAutoUpdateCheck() {
        String latest = getSharedPreferences("ftxt_prefs", MODE_PRIVATE)
                .getString(UpdateChecker.PREF_LATEST_VERSION, null);
        boolean alreadyAware = latest != null && !latest.isEmpty();
        UpdateChecker.autoCheckIfNeeded(this, () -> {
            if (!alreadyAware) {
                showAutoUpdateDialog();
            }
        });
    }

    private void showAutoUpdateDialog() {
        SharedPreferences prefs = getSharedPreferences("ftxt_prefs", MODE_PRIVATE);
        String latest = prefs.getString(UpdateChecker.PREF_LATEST_VERSION, null);
        String url = prefs.getString(UpdateChecker.PREF_LATEST_URL, UpdateChecker.RELEASES_PAGE);
        if (latest == null || latest.isEmpty()) return;
        new android.app.AlertDialog.Builder(this)
                .setTitle("Pembaruan tersedia")
                .setMessage("FTxT v" + latest + " Beta tersedia untuk diunduh.")
                .setPositiveButton("Lihat Informasi", (d, w) ->
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                android.net.Uri.parse(url == null ? UpdateChecker.RELEASES_PAGE : url))))
                .setNegativeButton("Unduh", (d, w) -> startApkDownload())
                .setNeutralButton("Nanti Saja", null)
                .show();
    }

    private void startApkDownload() {
        SharedPreferences prefs = getSharedPreferences("ftxt_prefs", MODE_PRIVATE);
        String apkUrl = prefs.getString(UpdateChecker.PREF_LATEST_DOWNLOAD, null);
        if (apkUrl == null || apkUrl.isEmpty()) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    android.net.Uri.parse(prefs.getString(UpdateChecker.PREF_LATEST_URL,
                            UpdateChecker.RELEASES_PAGE))));
            return;
        }
        ApkDownloadHelper.downloadAndInstall(this, apkUrl);
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
        DataUsageTracker.get(this).observe(
                TrafficStats.getTotalRxBytes(), TrafficStats.getTotalTxBytes());
        refreshSidebar();
        if (panelManager != null) {
            panelManager.onPanelShown();
            String currentPanel = panelManager.getCurrentPanel();
            if (currentPanel != null) {
                SharedPreferences prefs = getSharedPreferences("ftxt_prefs", MODE_PRIVATE);
                boolean isDebuggingPanel = "debugging".equals(currentPanel);
                boolean isMemoryPanel = "memory".equals(currentPanel);
                boolean debuggingAllowed = prefs.getBoolean("debugging_show_in_sidebar", false);
                boolean memoryAllowed = prefs.getBoolean("memory_show_in_sidebar", false);
                if ((isDebuggingPanel && !debuggingAllowed) || (isMemoryPanel && !memoryAllowed)) {
                    panelManager.showPanel("text");
                    prefs.edit().putInt("nav_selected_item", R.id.navFloatingText).apply();
                    updateNavSelection(R.id.navFloatingText);
                    updateActionBarTitle(R.id.navFloatingText);
                }
            }
        }
        autoRequestAndStart();
        requestRemainingPermissions();
        FloatingService.updatePositionForModule(FloatingService.textModule());
    }

    @Override
    protected void onPause() {
        super.onPause();
        DataUsageTracker.get(this).flush();
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

    private void showSettingsPopup() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        PopupMenu popup = new PopupMenu(this, toolbar, Gravity.END, 0, R.style.SettingsPopupMenu);
        popup.getMenu().add("Muat Preset");
        popup.getMenu().add("Konfigurasi");
        popup.getMenu().add("Dokumentasi");
        popup.getMenu().add("Tentang Aplikasi");
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
            } else if (title.equals("Tentang Aplikasi")) {
                startActivity(new Intent(this, AboutActivity.class));
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
        // Hentikan seluruh overlay; pemantau baterai full-aktif tetap berjalan
        // seperti sebelumnya, sehingga FloatingService tidak di-stop paksa.
        FloatingService.stopAllModules();
    }

    private void forceClose() {
        killService();
        finishAffinity();
    }

    @Override
    protected void onDestroy() {
        if (panelVisibilityReceiver != null) {
            unregisterReceiver(panelVisibilityReceiver);
            panelVisibilityReceiver = null;
        }
        super.onDestroy();
    }

    private void loadShadowConfigs() {
        ConfigLoader.load(this);
    }

    private void updateNavSelection(int selectedId) {
        if (sidebarAdapter != null) {
            sidebarAdapter.setSelectedItemId(selectedId);
        }
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
            boolean showDebugging = prefs.getBoolean("debugging_show_in_sidebar", false);
            boolean showMemory = prefs.getBoolean("memory_show_in_sidebar", false);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject item = arr.getJSONObject(i);
                String id = item.optString("id", null);
                if ("navDebuging".equals(id) && !showDebugging) {
                    continue;
                }
                if ("navMemory".equals(id) && !showMemory) {
                    continue;
                }
                list.add(new SidebarItem(item.getString("l"), id, getIconForSidebarId(id)));
            }
        } catch (Exception e) {
            try {
                JSONArray def = new JSONArray(DEFAULT_SIDEBAR_JSON);
                SharedPreferences prefs = getSharedPreferences("ftxt_prefs", MODE_PRIVATE);
                boolean showDebugging = prefs.getBoolean("debugging_show_in_sidebar", false);
                boolean showMemory = prefs.getBoolean("memory_show_in_sidebar", false);
                for (int i = 0; i < def.length(); i++) {
                    JSONObject item = def.getJSONObject(i);
                    String id = item.optString("id", null);
                    if ("navDebuging".equals(id) && !showDebugging) {
                        continue;
                    }
                    if ("navMemory".equals(id) && !showMemory) {
                        continue;
                    }
                    list.add(new SidebarItem(item.getString("l"), id, getIconForSidebarId(id)));
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
        boolean showDebugging = prefs.getBoolean("debugging_show_in_sidebar", false);
        boolean showMemory = prefs.getBoolean("memory_show_in_sidebar", false);
        try {
            JSONArray def = new JSONArray(DEFAULT_SIDEBAR_JSON);
            for (int i = 0; i < def.length(); i++) {
                JSONObject o = def.getJSONObject(i);
                String id = o.optString("id", null);
                if (id != null && !existing.contains(id)) {
                    if ("navDebuging".equals(id) && !showDebugging) {
                        continue;
                    }
                    if ("navMemory".equals(id) && !showMemory) {
                        continue;
                    }
                    list.add(new SidebarItem(o.getString("l"), id, getIconForSidebarId(id)));
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

    private void initSidebar() {
        navItemContainer.setLayoutManager(new LinearLayoutManager(this));
        navItemContainer.addItemDecoration(
                new androidx.recyclerview.widget.DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        sidebarAdapter = new SidebarAdapter(parseSidebarJson());
        navItemContainer.setAdapter(sidebarAdapter);

        ItemTouchHelper.SimpleCallback cb = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(RecyclerView rv, RecyclerView.ViewHolder vh, RecyclerView.ViewHolder target) {
                int from = vh.getBindingAdapterPosition();
                int to = target.getBindingAdapterPosition();
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
        sidebarTouchHelper = new ItemTouchHelper(cb);
        sidebarTouchHelper.attachToRecyclerView(navItemContainer);
    }

    private void refreshSidebar() {
        sidebarAdapter.setItems(parseSidebarJson());
    }

    private static class SidebarItem {
        final String label;
        final String id;
        final int iconRes;

        SidebarItem(String label, String id, int iconRes) {
            this.label = label;
            this.id = id;
            this.iconRes = iconRes;
        }
    }

    private int getIconForSidebarId(String id) {
        if (id == null) return 0;
        switch (id) {
            case "navFloatingText": return R.drawable.ic_text_format;
            case "navFps": return R.drawable.ic_monitor;
            case "navNetwork": return R.drawable.ic_signal_wifi;
            case "navBattery": return R.drawable.ic_battery_full;
            case "navClock": return R.drawable.ic_schedule;
            case "navCrosshair": return R.drawable.ic_crosshair;
            case "navMemory": return R.drawable.ic_memory;
            case "navLogo": return R.drawable.ic_image;
            case "navColorPicker": return R.drawable.ic_palette;
            case "navDebuging": return R.drawable.ic_build;
            default: return 0;
        }
    }

    private class SidebarAdapter extends RecyclerView.Adapter<SidebarAdapter.ViewHolder> {
        private final List<SidebarItem> items;
        private int selectedItemId = -1;

        SidebarAdapter(List<SidebarItem> items) {
            this.items = items;
        }

        List<SidebarItem> getItems() { return items; }

        void setSelectedItemId(int selectedId) {
            this.selectedItemId = selectedId;
            notifyDataSetChanged();
        }

        void setItems(List<SidebarItem> newItems) {
            items.clear();
            items.addAll(newItems);
            notifyDataSetChanged();
        }

        void moveItem(int from, int to) {
            SidebarItem item = items.remove(from);
            items.add(to, item);
            notifyItemMoved(from, to);
        }

        private boolean isInDragZone(View v, android.view.MotionEvent event) {
            float x = event.getX();
            float right = v.getWidth();
            return x >= right - dp(48);
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
                tv.setOnTouchListener((v, event) -> {
                    if (event.getActionMasked() == android.view.MotionEvent.ACTION_DOWN
                            && SidebarAdapter.this.isInDragZone(v, event)) {
                        sidebarTouchHelper.startDrag(ViewHolder.this);
                    }
                    return false;
                });
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView tv = new TextView(MainActivity.this);
            tv.setTextSize(16);
            tv.setPadding(dp(16), dp(12), dp(16), dp(12));
            tv.setCompoundDrawablePadding(dp(12));
            tv.setGravity(android.view.Gravity.CENTER_VERTICAL);
            tv.setLayoutParams(new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            tv.setBackgroundResource(R.drawable.sidebar_item_bg);
            tv.setClickable(true);
            tv.setFocusable(true);
            return new ViewHolder(tv);
        }

        @Override
        public void onBindViewHolder(ViewHolder h, int pos) {
            SidebarItem item = items.get(pos);
            h.textView.setText(item.label);
            h.textView.setTextColor(getColorStateList(R.color.sidebar_nav_text));
            h.textView.setCompoundDrawableTintList(
                    androidx.core.content.ContextCompat.getColorStateList(MainActivity.this, R.color.sidebar_nav_text));
            if (item.iconRes != 0) {
                h.textView.setCompoundDrawablesRelativeWithIntrinsicBounds(item.iconRes, 0, R.drawable.ic_drag_handle, 0);
            } else {
                h.textView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_drag_handle, 0);
            }
            if (item.id != null) {
                int idRes = getResources().getIdentifier(item.id, "id", getPackageName());
                if (idRes != 0) {
                    h.textView.setId(idRes);
                    h.textView.setSelected(idRes == selectedItemId);
                }
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

    public void setToolbarSubtitle(CharSequence subtitle) {
        setToolbarSubtitle(subtitle, 1f);
    }

    public void setToolbarSubtitle(CharSequence subtitle, float sizeRatio) {
        if (getSupportActionBar() != null) {
            if (subtitle != null && sizeRatio < 1f) {
                android.text.SpannableString span = new android.text.SpannableString(subtitle);
                span.setSpan(new android.text.style.RelativeSizeSpan(sizeRatio),
                        0, subtitle.length(), android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                getSupportActionBar().setSubtitle(span);
            } else {
                getSupportActionBar().setSubtitle(subtitle);
            }
        }
    }

    private void updateActionBarTitle(int itemId) {
        setToolbarSubtitle(null);
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
