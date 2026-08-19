package exp.ftxt;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.widget.CheckBox;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;

public class SettingsActivity extends AppCompatActivity {

    private Switch overlaySwitch;
    private Switch notificationSwitch;
    private Switch batterySwitch;
    private Switch iconSwitch;
    private Switch debuggingSidebarSwitch;
    private Switch memorySidebarSwitch;

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
        boolean showDebugging = prefs.getBoolean("debugging_show_in_sidebar", false);
        debuggingSidebarSwitch.setChecked(showDebugging);
        applySwitchTint(debuggingSidebarSwitch, showDebugging);

        debuggingSidebarSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            applySwitchTint(debuggingSidebarSwitch, isChecked);
            prefs.edit().putBoolean("debugging_show_in_sidebar", isChecked).apply();
        });

        memorySidebarSwitch = findViewById(R.id.memorySidebarSwitch);
        boolean showMemory = prefs.getBoolean("memory_show_in_sidebar", false);
        memorySidebarSwitch.setChecked(showMemory);
        applySwitchTint(memorySidebarSwitch, showMemory);

        memorySidebarSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            applySwitchTint(memorySidebarSwitch, isChecked);
            prefs.edit().putBoolean("memory_show_in_sidebar", isChecked).apply();
        });

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

    private void setIcon(boolean useAlt) {
        PackageManager pm = getPackageManager();
        ComponentName def = new ComponentName(this, "exp.ftxt.MainActivityDefault");
        ComponentName alt = new ComponentName(this, "exp.ftxt.MainActivityAlt");
        int defState = useAlt ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED : PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
        int altState = useAlt ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        pm.setComponentEnabledSetting(def, defState, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(alt, altState, PackageManager.DONT_KILL_APP);
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