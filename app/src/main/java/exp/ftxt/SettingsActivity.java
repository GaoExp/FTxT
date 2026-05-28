package exp.ftxt;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        overlaySwitch = findViewById(R.id.overlayPermissionSwitch);
        notificationSwitch = findViewById(R.id.notificationPermissionSwitch);
        batterySwitch = findViewById(R.id.batteryPermissionSwitch);
        CheckBox confirmExitCheck = findViewById(R.id.confirmExitCheck);

        SharedPreferences prefs = getSharedPreferences("ftxt_prefs", MODE_PRIVATE);
        confirmExitCheck.setChecked(prefs.getBoolean("confirm_exit", false));
        confirmExitCheck.setOnCheckedChangeListener((buttonView, isChecked) ->
                prefs.edit().putBoolean("confirm_exit", isChecked).apply());

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

