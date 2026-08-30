package exp.ftxt;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import exp.ftxt.core.FloatingService;
import exp.ftxt.features.memory_stats.MemoryConfig;
import exp.ftxt.features.memory_stats.MemoryMonitor;
import exp.ftxt.features.memory_stats.MemoryModule;

public class SettingsActivity extends AppCompatActivity {

    private Switch overlaySwitch;
    private Switch notificationSwitch;
    private Switch batterySwitch;
    private Switch iconSwitch;
    private Switch debuggingSidebarSwitch;
    private EditText debuggingPasswordInput;
    private TextView developerStatusLabel;
    private TextView debuggingUnlockBtn;
    private TextView debuggingRelockBtn;
    private Switch memorySidebarSwitch;
    private TextView exportDbBtn;

    private static final String DEBUGGING_PASSWORD = "01000110 01010100 01111000 01010100";
    public static final String PREF_DEVELOPER_UNLOCKED = "developer_unlocked";

    public static final String ACTION_PANEL_VISIBILITY_CHANGED = "exp.ftxt.PANEL_VISIBILITY_CHANGED";
    public static final String EXTRA_PANEL_ID = "panel_id";
    public static final String EXTRA_PANEL_VISIBLE = "panel_visible";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Konfigurasi");
        toolbar.setNavigationOnClickListener(v -> finish());

        overlaySwitch = findViewById(R.id.overlayPermissionSwitch);
        notificationSwitch = findViewById(R.id.notificationPermissionSwitch);
        batterySwitch = findViewById(R.id.batteryPermissionSwitch);
        iconSwitch = findViewById(R.id.iconSwitch);

        SharedPreferences prefs = getSharedPreferences("ftxt_prefs", MODE_PRIVATE);

        updatePermissionSwitches();

        overlaySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            applySwitchTint(overlaySwitch, isChecked);
            if (isChecked && !Settings.canDrawOverlays(this)) {
                startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName())));
            }
        });

        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            applySwitchTint(notificationSwitch, isChecked);
            if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
                }
            }
        });

        batterySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            applySwitchTint(batterySwitch, isChecked);
            if (isChecked) {
                PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
                if (!pm.isIgnoringBatteryOptimizations(getPackageName())) {
                    startActivity(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                            Uri.parse("package:" + getPackageName())));
                }
            }
        });

        boolean useAltIcon = prefs.getBoolean("alt_icon", false);
        iconSwitch.setChecked(useAltIcon);
        applySwitchTint(iconSwitch, useAltIcon);

        iconSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            applySwitchTint(iconSwitch, isChecked);
            prefs.edit().putBoolean("alt_icon", isChecked).apply();
            setIcon(isChecked);
        });

        debuggingSidebarSwitch = findViewById(R.id.debuggingSidebarSwitch);
        debuggingPasswordInput = findViewById(R.id.debuggingPasswordInput);
        developerStatusLabel = findViewById(R.id.developerStatusLabel);
        debuggingUnlockBtn = findViewById(R.id.debuggingUnlockBtn);
        debuggingRelockBtn = findViewById(R.id.debuggingRelockBtn);
        memorySidebarSwitch = findViewById(R.id.memorySidebarSwitch);

        exportDbBtn = findViewById(R.id.exportDbBtn);
        exportDbBtn.setOnClickListener(v -> exportDatabases());

        boolean developerUnlocked = prefs.getBoolean(PREF_DEVELOPER_UNLOCKED, false);
        boolean showDebugging = developerUnlocked && prefs.getBoolean("debugging_show_in_sidebar", false);
        boolean showMemory = developerUnlocked && prefs.getBoolean("memory_show_in_sidebar", false);

        memorySidebarSwitch.setChecked(showMemory);
        applySwitchTint(memorySidebarSwitch, showMemory);

        memorySidebarSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            applySwitchTint(memorySidebarSwitch, isChecked);
            prefs.edit().putBoolean("memory_show_in_sidebar", isChecked).apply();
            if (!isChecked) turnOffMemoryPanel(prefs);
        });

        debuggingSidebarSwitch.setChecked(showDebugging);
        applySwitchTint(debuggingSidebarSwitch, showDebugging);

        debuggingSidebarSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            applySwitchTint(debuggingSidebarSwitch, isChecked);
            prefs.edit().putBoolean("debugging_show_in_sidebar", isChecked).apply();
            if (!isChecked) sendPanelHiddenBroadcast("debugging");
        });

        debuggingUnlockBtn.setOnClickListener(v -> {
            String input = debuggingPasswordInput.getText().toString();
            if (input.equals(DEBUGGING_PASSWORD)) {
                debuggingPasswordInput.setText("");
                prefs.edit().putBoolean(PREF_DEVELOPER_UNLOCKED, true).apply();
                applyDeveloperState(true);
            } else {
                Toast.makeText(this, "Kunci salah", Toast.LENGTH_SHORT).show();
            }
        });

        debuggingRelockBtn.setOnClickListener(v -> {
            memorySidebarSwitch.setChecked(false);
            debuggingSidebarSwitch.setChecked(false);
            prefs.edit()
                    .putBoolean(PREF_DEVELOPER_UNLOCKED, false)
                    .putBoolean("memory_show_in_sidebar", false)
                    .putBoolean("debugging_show_in_sidebar", false)
                    .apply();
            applyDeveloperState(false);
        });

        applyDeveloperState(developerUnlocked);

    }

    @Override
    protected void onResume() {
        super.onResume();
        updatePermissionSwitches();
    }

    private void applySwitchTint(Switch sw, boolean isChecked) {
        if (isChecked) {
            sw.setThumbTintList(ColorStateList.valueOf(Color.parseColor("#2196F3")));
            sw.setTrackTintList(ColorStateList.valueOf(Color.parseColor("#90CAF9")));
        } else {
            sw.setThumbTintList(ColorStateList.valueOf(Color.parseColor("#E53935")));
            sw.setTrackTintList(ColorStateList.valueOf(Color.parseColor("#EF9A9A")));
        }
    }

    private void applyDeveloperState(boolean unlocked) {
        String status = unlocked ? "Terbuka" : "Terkunci";
        int color = Color.parseColor(unlocked ? "#4CAF50" : "#E53935");
        SpannableString label = new SpannableString("Fitur Developer • " + status);
        label.setSpan(new ForegroundColorSpan(color),
                18, label.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        developerStatusLabel.setText(label);
        debuggingPasswordInput.setVisibility(unlocked ? android.view.View.GONE : android.view.View.VISIBLE);
        debuggingUnlockBtn.setVisibility(unlocked ? android.view.View.GONE : android.view.View.VISIBLE);
        debuggingRelockBtn.setVisibility(unlocked ? android.view.View.VISIBLE : android.view.View.GONE);
        memorySidebarSwitch.setEnabled(unlocked);
        debuggingSidebarSwitch.setEnabled(unlocked);
        exportDbBtn.setEnabled(unlocked);
    }

    private void turnOffMemoryPanel(SharedPreferences prefs) {
        if (MemoryConfig.enabled) {
            MemoryConfig.enabled = false;
            prefs.edit().putBoolean("mem_enabled", false).apply();
            if (FloatingService.instance != null) {
                MemoryModule memoryModule = FloatingService.memoryModule();
                if (memoryModule != null && memoryModule.isRunning()) {
                    FloatingService.stopModule(memoryModule);
                }
            }
        }
        if (MemoryConfig.backgroundMonitor) {
            MemoryConfig.backgroundMonitor = false;
            prefs.edit().putBoolean("mem_bg_monitor", false).apply();
            MemoryMonitor.stop();
        }
        sendPanelHiddenBroadcast("memory");
    }

    private void sendPanelHiddenBroadcast(String panelId) {
        Intent intent = new Intent(ACTION_PANEL_VISIBILITY_CHANGED);
        intent.putExtra(EXTRA_PANEL_ID, panelId);
        intent.putExtra(EXTRA_PANEL_VISIBLE, false);
        sendBroadcast(intent);
    }

    private void setIcon(boolean useAlt) {
        PackageManager pm = getPackageManager();
        ComponentName def = new ComponentName(this, "exp.ftxt.MainActivityDefault");
        ComponentName alt = new ComponentName(this, "exp.ftxt.MainActivityAlt");
        int defState = useAlt ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED : PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
        int altState = useAlt ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        pm.setComponentEnabledSetting(def, defState, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(alt, altState, PackageManager.DONT_KILL_APP);
    }

    private void exportDatabases() {
        File dbDir = getDatabasePath("ftxt_db").getParentFile();
        if (dbDir == null || !dbDir.isDirectory()) {
            Toast.makeText(this, "Folder database tidak ditemukan", Toast.LENGTH_SHORT).show();
            return;
        }
        File[] files = dbDir.listFiles();
        if (files == null || files.length == 0) {
            Toast.makeText(this, "Tidak ada database untuk diekstrak", Toast.LENGTH_SHORT).show();
            return;
        }
        int ok = 0;
        for (File f : files) {
            if (f.isFile() && f.canRead() && f.length() > 0) {
                if (copyDbToDocuments(f)) ok++;
            }
        }
        Toast.makeText(this, ok > 0
                        ? "Diekstrak " + ok + " file ke Documents/FTxT/extract/db"
                        : "Gagal mengekstrak database",
                Toast.LENGTH_LONG).show();
    }

    private boolean copyDbToDocuments(File src) {
        try {
            String relPath = Environment.DIRECTORY_DOCUMENTS + "/FTxT/extract/db/";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, src.getName());
                values.put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, relPath);
                Uri uri = getContentResolver().insert(
                        MediaStore.Files.getContentUri("external"), values);
                if (uri == null) return false;
                try (OutputStream os = getContentResolver().openOutputStream(uri);
                     FileInputStream is = new FileInputStream(src)) {
                    if (os == null) return false;
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = is.read(buf)) > 0) os.write(buf, 0, n);
                }
                return true;
            } else {
                File dir = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOCUMENTS), "FTxT/extract/db");
                if (!dir.exists() && !dir.mkdirs()) return false;
                File dst = new File(dir, src.getName());
                try (FileInputStream is = new FileInputStream(src);
                     FileOutputStream os = new FileOutputStream(dst)) {
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = is.read(buf)) > 0) os.write(buf, 0, n);
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void updatePermissionSwitches() {
        boolean overlayOk = Settings.canDrawOverlays(this);
        overlaySwitch.setChecked(overlayOk);
        applySwitchTint(overlaySwitch, overlayOk);

        boolean notifOk;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifOk = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        } else {
            notifOk = true;
        }
        notificationSwitch.setChecked(notifOk);
        applySwitchTint(notificationSwitch, notifOk);

        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        boolean batteryOk = pm.isIgnoringBatteryOptimizations(getPackageName());
        batterySwitch.setChecked(batteryOk);
        applySwitchTint(batterySwitch, batteryOk);
    }

}