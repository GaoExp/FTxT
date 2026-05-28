package exp.ftxt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.util.TypedValue;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.splashscreen.SplashScreen;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

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

        NavigationView navView = findViewById(R.id.navView);
        SharedPreferences prefs = getSharedPreferences("ftxt_prefs", MODE_PRIVATE);
        int savedNavItem = prefs.getInt("nav_selected_item", R.id.nav_floating_text);
        navView.setCheckedItem(savedNavItem);
        if (savedNavItem == R.id.nav_fps) {
            panelText.setVisibility(View.GONE);
            panelFps.setVisibility(View.VISIBLE);
            getSupportActionBar().setTitle(R.string.nav_fps);
        }

        TextView navTitle = navView.getHeaderView(0).findViewById(R.id.navHeaderTitle);
        try {
            String v = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            navTitle.setText("FTxT v" + v);
        } catch (PackageManager.NameNotFoundException e) {
            navTitle.setText("FTxT");
        }

        // Navigation drawer: switch antar panel modular
        // Panel baru bisa ditambahkan di sini + drawer_menu.xml
        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            getSharedPreferences("ftxt_prefs", MODE_PRIVATE).edit()
                    .putInt("nav_selected_item", id).apply();

            if (id == R.id.nav_floating_text) {
                panelText.setVisibility(View.VISIBLE);
                panelFps.setVisibility(View.GONE);
                getSupportActionBar().setTitle(R.string.nav_floating_text);
                drawerLayout.closeDrawers();
                textPanel.onPanelShown();
                return true;
            }

            if (id == R.id.nav_fps) {
                panelText.setVisibility(View.GONE);
                panelFps.setVisibility(View.VISIBLE);
                getSupportActionBar().setTitle(R.string.nav_fps);
                drawerLayout.closeDrawers();
                return true;
            }

            if (id == R.id.nav_network || id == R.id.nav_battery || id == R.id.nav_clock
                    || id == R.id.nav_cpu || id == R.id.nav_crosshair || id == R.id.nav_watermark || id == R.id.nav_logo) {
                Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show();
                drawerLayout.closeDrawers();
                return true;
            }

            return false;
        });

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
                showDocumentationDialog();
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

    private void showDocumentationDialog() {
        String[] docs = {"README", "CHANGELOG", "PANDUAN", "STRUKTUR", "DEVELOPMENT", "TENTANG"};
        String[] files = {"README.txt", "CHANGELOG.txt", "PANDUAN.txt", "STRUKTUR.txt", "DEVELOPMENT.txt", "TENTANG.txt"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Lihat Dokumentasi");
        builder.setItems(docs, (dialog, which) -> {
            String content = readAssetFile(files[which]);
            showContentDialog(docs[which], content);
        });
        builder.setNegativeButton("Tutup", null);
        builder.create().show();
    }

    private float currentDocTextSize = 14;

    private void showContentDialog(String title, String content) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);

        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        controls.setPadding(16, 8, 16, 4);

        Button minusBtn = new Button(this);
        minusBtn.setText("−");
        minusBtn.setTextSize(18);
        controls.addView(minusBtn);

        TextView sizeLabel = new TextView(this);
        sizeLabel.setText(String.format("%.0f sp", currentDocTextSize));
        sizeLabel.setPadding(16, 0, 16, 0);
        sizeLabel.setGravity(android.view.Gravity.CENTER);
        sizeLabel.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        controls.addView(sizeLabel);

        Button plusBtn = new Button(this);
        plusBtn.setText("+");
        plusBtn.setTextSize(18);
        controls.addView(plusBtn);

        ScrollView scrollView = new ScrollView(this);
        TextView textView = new TextView(this);
        textView.setText(content);
        int paddingPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, getResources().getDisplayMetrics());
        textView.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
        textView.setTextSize(currentDocTextSize);
        scrollView.addView(textView);

        root.addView(controls);
        root.addView(scrollView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));

        minusBtn.setOnClickListener(v -> {
            if (currentDocTextSize > 4) {
                currentDocTextSize -= 2;
                textView.setTextSize(currentDocTextSize);
                sizeLabel.setText(String.format("%.0f sp", currentDocTextSize));
            }
        });

        plusBtn.setOnClickListener(v -> {
            if (currentDocTextSize < 60) {
                currentDocTextSize += 2;
                textView.setTextSize(currentDocTextSize);
                sizeLabel.setText(String.format("%.0f sp", currentDocTextSize));
            }
        });

        builder.setView(root);
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private String readAssetFile(String filename) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(getAssets().open(filename)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            content.append("Error reading file: ").append(e.getMessage());
        }
        return content.toString();
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
}
