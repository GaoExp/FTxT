package exp.ftxt;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.android.material.navigation.NavigationView;

import exp.ftxt.core.FloatingService;
import exp.ftxt.modules.fps.FpsConfig;
import exp.ftxt.modules.text.TextConfig;
import exp.ftxt.shared.ui.ColorPickerDialog;

public class MainActivity extends AppCompatActivity {

    EditText editText;
    SeekBar seekBar;
    Button colorButton;
    Switch overlaySwitch;
    Switch touchPassthroughSwitch;
    Switch shadowSwitch;

    // FPS views
    Switch fpsSwitch;
    SeekBar fpsSizeSeekBar;
    Button fpsColorButton;
    Switch fpsShadowSwitch;

    // panels
    View panelText;
    View panelFps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        navView.setCheckedItem(R.id.nav_floating_text);

        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_floating_text) {
                panelText.setVisibility(View.VISIBLE);
                panelFps.setVisibility(View.GONE);
                drawerLayout.closeDrawers();
                return true;
            }

            if (id == R.id.nav_fps) {
                panelText.setVisibility(View.GONE);
                panelFps.setVisibility(View.VISIBLE);
                drawerLayout.closeDrawers();
                return true;
            }

            if (id == R.id.nav_network || id == R.id.nav_battery || id == R.id.nav_clock) {
                Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show();
                drawerLayout.closeDrawers();
                return true;
            }

            return false;
        });

        // === Load saved configs ===
        TextConfig.shadow = getSharedPreferences("ftxt_prefs", MODE_PRIVATE)
                .getBoolean("shadow_enabled", false);
        TextConfig.touchPassthrough = false;

        FpsConfig.enabled = getSharedPreferences("ftxt_prefs", MODE_PRIVATE)
                .getBoolean("fps_enabled", false);
        FpsConfig.color = getSharedPreferences("ftxt_prefs", MODE_PRIVATE)
                .getInt("fps_color", Color.WHITE);
        FpsConfig.shadow = getSharedPreferences("ftxt_prefs", MODE_PRIVATE)
                .getBoolean("fps_shadow", false);

        // === Bind text views ===
        editText = findViewById(R.id.editText);
        seekBar = findViewById(R.id.textSizeSeekBar);
        colorButton = findViewById(R.id.colorButton);
        overlaySwitch = findViewById(R.id.overlaySwitch);
        touchPassthroughSwitch = findViewById(R.id.touchPassthroughSwitch);
        shadowSwitch = findViewById(R.id.shadowSwitch);

        // === Bind FPS views ===
        fpsSwitch = findViewById(R.id.fpsSwitch);
        fpsSizeSeekBar = findViewById(R.id.fpsSizeSeekBar);
        fpsColorButton = findViewById(R.id.fpsColorButton);
        fpsShadowSwitch = findViewById(R.id.fpsShadowSwitch);

        // === Text controls ===

        editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextConfig.text = s.toString().trim();
                if (TextConfig.text.isEmpty()) TextConfig.text = "FTxT AKTIF";
                FloatingService.updateTextStatic();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        seekBar.setProgress(20);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (progress < 10) { progress = 10; sb.setProgress(progress); }
                TextConfig.size = progress;
                FloatingService.updateTextSizeStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        colorButton.setOnClickListener(v -> {
            ColorPickerDialog.show(this, "Pilih Warna", TextConfig.color, color -> {
                TextConfig.color = color;
                FloatingService.updateTextColorStatic();
            });
        });

        overlaySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            applySwitchTint(overlaySwitch, isChecked);

            getSharedPreferences("ftxt_prefs", MODE_PRIVATE)
                    .edit().putBoolean("text_overlay_on", isChecked).apply();

            if (isChecked) {
                TextConfig.text = editText.getText().toString().trim();
                if (TextConfig.text.isEmpty()) TextConfig.text = "FTxT AKTIF";

                // If service already running (FPS), just create text overlay
                if (FloatingService.instance != null) {
                    FloatingService.createTextOverlayStatic();
                    return;
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        && !Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "Izinkan overlay di pengaturan", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName())));
                    overlaySwitch.setChecked(false);
                    return;
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                        && ContextCompat.checkSelfPermission(this,
                        Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
                    overlaySwitch.setChecked(false);
                    return;
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
                    if (!pm.isIgnoringBatteryOptimizations(getPackageName())) {
                        Toast.makeText(this, "Nonaktifkan optimasi baterai", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                                Uri.parse("package:" + getPackageName())));
                    }
                }

                startService(new Intent(this, FloatingService.class));
            } else {
                FloatingService.destroyTextOverlayStatic();
                if (!FpsConfig.enabled) {
                    stopService(new Intent(this, FloatingService.class));
                }
            }
        });

        touchPassthroughSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            applySwitchTint(touchPassthroughSwitch, isChecked);
            TextConfig.touchPassthrough = isChecked;
            FloatingService.updateTouchFlagsStatic();
        });

        shadowSwitch.setChecked(TextConfig.shadow);

        shadowSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            applySwitchTint(shadowSwitch, isChecked);
            TextConfig.shadow = isChecked;
            getSharedPreferences("ftxt_prefs", MODE_PRIVATE)
                    .edit().putBoolean("shadow_enabled", isChecked).apply();
            FloatingService.updateShadowStatic();
        });

        // initial tints
        applySwitchTint(overlaySwitch, overlaySwitch.isChecked());
        applySwitchTint(touchPassthroughSwitch, touchPassthroughSwitch.isChecked());
        applySwitchTint(shadowSwitch, shadowSwitch.isChecked());

        // === FPS controls ===

        fpsSwitch.setChecked(FpsConfig.enabled);
        applySwitchTint(fpsSwitch, FpsConfig.enabled);

        fpsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            FpsConfig.enabled = isChecked;
            applySwitchTint(fpsSwitch, isChecked);
            getSharedPreferences("ftxt_prefs", MODE_PRIVATE)
                    .edit().putBoolean("fps_enabled", isChecked).apply();

            if (isChecked) {
                if (FloatingService.instance != null) {
                    FloatingService.startFpsStatic();
                } else {
                    // Start service for FPS only
                    startService(new Intent(this, FloatingService.class));
                }
            } else {
                FloatingService.stopFpsStatic();
                // Stop service if text overlay also off
                if (!overlaySwitch.isChecked()) {
                    stopService(new Intent(this, FloatingService.class));
                }
            }
        });

        fpsSizeSeekBar.setProgress((int) FpsConfig.size);

        fpsSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (progress < 10) { progress = 10; sb.setProgress(progress); }
                FpsConfig.size = progress;
                FloatingService.updateFpsSizeStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        fpsColorButton.setOnClickListener(v -> {
            ColorPickerDialog.show(this, "Pilih Warna FPS", FpsConfig.color, color -> {
                FpsConfig.color = color;
                getSharedPreferences("ftxt_prefs", MODE_PRIVATE)
                        .edit().putInt("fps_color", color).apply();
                FloatingService.updateFpsColorStatic();
            });
        });

        fpsShadowSwitch.setChecked(FpsConfig.shadow);
        applySwitchTint(fpsShadowSwitch, FpsConfig.shadow);

        fpsShadowSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            FpsConfig.shadow = isChecked;
            applySwitchTint(fpsShadowSwitch, isChecked);
            getSharedPreferences("ftxt_prefs", MODE_PRIVATE)
                    .edit().putBoolean("fps_shadow", isChecked).apply();
            FloatingService.updateFpsShadowStatic();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
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
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Izin notifikasi diberikan", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Izin notifikasi diperlukan", Toast.LENGTH_LONG).show();
            }
        }
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
}
