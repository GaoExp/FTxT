package exp.ftxt;

import androidx.appcompat.widget.Toolbar;
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
import exp.ftxt.shared.color.HSVColorPickerView;
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
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.android.material.navigation.NavigationView;

public class MainActivity
extends AppCompatActivity {

    public static String currentText =
            "FTxT AKTIF";

    public static float currentSize =
            20f;

    public static int currentColor =
            Color.WHITE;

    public static boolean isTouchPassthrough =
            false;

    // FPS state
    public static boolean fpsEnabled = false;
    public static float fpsSize = 14f;
    public static int fpsColor = Color.WHITE;
    public static boolean fpsShadow = false;

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
    protected void onCreate(
            Bundle savedInstanceState){

        boolean isDark = getSharedPreferences("ftxt_prefs", MODE_PRIVATE)
                .getBoolean("theme_dark", false);

        if(isDark){
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES);
        }else{
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(
                savedInstanceState
        );

        setContentView(
                R.layout.activity_main
        );
		Toolbar toolbar =
            findViewById(
                R.id.toolbar
        );

        setSupportActionBar(
        toolbar
        );

        DrawerLayout drawerLayout =
            findViewById(
                R.id.drawerLayout
        );

        ActionBarDrawerToggle toggle =
            new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.nav_open,
                R.string.nav_close
        );

        drawerLayout.addDrawerListener(
            toggle
        );

        toggle.syncState();

        NavigationView navView =
            findViewById(
                R.id.navView
        );

        navView.setCheckedItem(
            R.id.nav_floating_text
        );

        navView
        .setNavigationItemSelectedListener(
            item -> {

                int id = item.getItemId();

                if(id == R.id.nav_floating_text){
                    panelText.setVisibility(View.VISIBLE);
                    panelFps.setVisibility(View.GONE);
                    drawerLayout.closeDrawers();
                    return true;
                }

                if(id == R.id.nav_fps){
                    panelText.setVisibility(View.GONE);
                    panelFps.setVisibility(View.VISIBLE);
                    drawerLayout.closeDrawers();
                    return true;
                }

                if(id == R.id.nav_network
                    || id == R.id.nav_battery
                    || id == R.id.nav_clock){

                    Toast.makeText(
                        this,
                        "Coming Soon",
                        Toast.LENGTH_SHORT
                    ).show();

                    drawerLayout.closeDrawers();
                    return true;

                }

                return false;

        });

        panelText = findViewById(R.id.panel_text);
        panelFps = findViewById(R.id.panel_fps);

        editText =
                findViewById(
                        R.id.editText
                );

        editText.addTextChangedListener(
                new TextWatcher(){

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after){}

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count){
                currentText =
                        s.toString().trim();
                if(currentText.isEmpty()){
                    currentText =
                            "FTxT AKTIF";
                }
                FloatingService
                .updateTextStatic();
            }

            @Override
            public void afterTextChanged(
                    Editable s){}

        });

        seekBar =
                findViewById(
                        R.id.textSizeSeekBar
                );

        colorButton =
                findViewById(
                        R.id.colorButton
                );

        overlaySwitch =
                findViewById(
                        R.id.overlaySwitch
                );

        touchPassthroughSwitch =
                findViewById(
                        R.id.touchPassthroughSwitch
                );

        shadowSwitch =
                findViewById(
                        R.id.shadowSwitch
                );

        // === FPS views ===
        fpsSwitch = findViewById(R.id.fpsSwitch);
        fpsSizeSeekBar = findViewById(R.id.fpsSizeSeekBar);
        fpsColorButton = findViewById(R.id.fpsColorButton);
        fpsShadowSwitch = findViewById(R.id.fpsShadowSwitch);

        // === Load saved FPS state ===
        fpsEnabled = getSharedPreferences("ftxt_prefs", MODE_PRIVATE)
                .getBoolean("fps_enabled", false);
        fpsSize = getSharedPreferences("ftxt_prefs", MODE_PRIVATE)
                .getFloat("fps_size", 14f);
        fpsColor = getSharedPreferences("ftxt_prefs", MODE_PRIVATE)
                .getInt("fps_color", Color.WHITE);
        fpsShadow = getSharedPreferences("ftxt_prefs", MODE_PRIVATE)
                .getBoolean("fps_shadow", false);

        // === TEXT controls ===

        seekBar.setProgress(20);

        seekBar
        .setOnSeekBarChangeListener(
                new SeekBar
                .OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(
                    SeekBar seekBar,
                    int progress,
                    boolean fromUser){

                if(progress < 10){

                    progress = 10;
                    seekBar.setProgress(progress);

                }

                currentSize = progress;

                FloatingService
                .updateTextSizeStatic();

            }

            @Override
            public void onStartTrackingTouch(
                    SeekBar seekBar){}

            @Override
            public void onStopTrackingTouch(
                    SeekBar seekBar){}

        });

        colorButton
        .setOnClickListener(v -> {

            showHSVColorPickerDialog();

        });

        overlaySwitch
        .setOnCheckedChangeListener(
                (buttonView,isChecked)->{

            applySwitchTint(
                    overlaySwitch,
                    isChecked
            );

            if(isChecked){

                currentText =
                        editText
                        .getText()
                        .toString()
                        .trim();

                if(currentText
                        .isEmpty()){

                    currentText =
                            "FTxT AKTIF";

                }

                if(Build.VERSION.SDK_INT
                        >= Build.VERSION_CODES.M){

                    if(!Settings
                            .canDrawOverlays(
                                    this
                            )){

                        Toast.makeText(
                                this,
                                "Izinkan overlay di pengaturan",
                                Toast.LENGTH_LONG
                        ).show();

                        Intent intent =
                                new Intent(

                                        Settings
                                        .ACTION_MANAGE_OVERLAY_PERMISSION,

                                        Uri.parse(
                                                "package:"
                                                + getPackageName()
                                        )
                                );

                        startActivity(
                                intent
                        );

                        overlaySwitch
                        .setChecked(
                                false
                        );

                        return;

                    }

                }

                if(Build.VERSION.SDK_INT
                        >= Build.VERSION_CODES.TIRAMISU){

                    if(ContextCompat
                            .checkSelfPermission(
                                    this,
                                    Manifest.permission
                                    .POST_NOTIFICATIONS
                            ) != PackageManager
                            .PERMISSION_GRANTED){

                        ActivityCompat
                        .requestPermissions(
                                this,
                                new String[]{
                                    Manifest.permission
                                    .POST_NOTIFICATIONS
                                },
                                100
                        );

                        overlaySwitch
                        .setChecked(
                                false
                        );

                        return;

                    }

                }

                if(Build.VERSION.SDK_INT
                        >= Build.VERSION_CODES.M){

                    if(!Settings
                            .canDrawOverlays(
                                    this
                            )){

                        Toast.makeText(
                                this,
                                "Izinkan overlay di pengaturan",
                                Toast.LENGTH_LONG
                        ).show();

                        Intent intent =
                                new Intent(

                                        Settings
                                        .ACTION_MANAGE_OVERLAY_PERMISSION,

                                        Uri.parse(
                                                "package:"
                                                + getPackageName()
                                        )
                                );

                        startActivity(
                                intent
                        );

                        overlaySwitch
                        .setChecked(
                                false
                        );

                        return;

                    }

                }

                if(Build.VERSION.SDK_INT
                        >= Build.VERSION_CODES.M){

                    String packageName =
                            getPackageName();

                    PowerManager pm =
                            (PowerManager)
                            getSystemService(
                                    POWER_SERVICE
                            );

                    if(!pm
                            .isIgnoringBatteryOptimizations(
                                    packageName
                            )){

                        Toast.makeText(
                                this,
                                "Nonaktifkan optimasi baterai",
                                Toast.LENGTH_LONG
                        ).show();

                        Intent intent =
                                new Intent(

                                        Settings
                                        .ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,

                                        Uri.parse(
                                                "package:"
                                                + packageName
                                        )
                                );

                        startActivity(
                                intent
                        );

                    }

                }

                startForegroundService(
                        new Intent(
                                this,
                                FloatingService
                                .class
                        )
                );

            }

            else{

                stopService(
                        new Intent(
                                this,
                                FloatingService
                                .class
                        )
                );

            }

        });

        touchPassthroughSwitch
        .setOnCheckedChangeListener(
                (buttonView,isChecked)->{

            applySwitchTint(
                    touchPassthroughSwitch,
                    isChecked
            );

            isTouchPassthrough = isChecked;

            FloatingService
            .updateTouchFlagsStatic();

        });

        boolean isShadow = getSharedPreferences("ftxt_prefs", MODE_PRIVATE)
                .getBoolean("shadow_enabled", false);

        shadowSwitch.setChecked(isShadow);

        shadowSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

            applySwitchTint(shadowSwitch, isChecked);

            getSharedPreferences("ftxt_prefs", MODE_PRIVATE)
                    .edit()
                    .putBoolean("shadow_enabled", isChecked)
                    .apply();

            FloatingService.updateShadowStatic();

        });

        applySwitchTint(
                overlaySwitch,
                overlaySwitch.isChecked()
        );

        applySwitchTint(
                touchPassthroughSwitch,
                touchPassthroughSwitch.isChecked()
        );

        applySwitchTint(
                shadowSwitch,
                shadowSwitch.isChecked()
        );

        // === FPS controls ===

        fpsSwitch.setChecked(fpsEnabled);
        applySwitchTint(fpsSwitch, fpsEnabled);

        fpsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

            fpsEnabled = isChecked;
            applySwitchTint(fpsSwitch, isChecked);

            getSharedPreferences("ftxt_prefs", MODE_PRIVATE)
                    .edit()
                    .putBoolean("fps_enabled", isChecked)
                    .apply();

            if(isChecked && FloatingService.instance != null){
                FloatingService.startFpsStatic();
            }

            if(!isChecked && FloatingService.instance != null){
                FloatingService.stopFpsStatic();
            }

        });

        fpsSizeSeekBar.setProgress((int) fpsSize);

        fpsSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar sb, int progress, boolean fromUser){
                if(progress < 10){
                    progress = 10;
                    sb.setProgress(progress);
                }
                fpsSize = progress;
                FloatingService.updateFpsSizeStatic();
            }
            @Override public void onStartTrackingTouch(SeekBar sb){}
            @Override public void onStopTrackingTouch(SeekBar sb){}
        });

        fpsColorButton.setOnClickListener(v -> {
            showFpsColorPickerDialog();
        });

        fpsShadowSwitch.setChecked(fpsShadow);
        applySwitchTint(fpsShadowSwitch, fpsShadow);

        fpsShadowSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            fpsShadow = isChecked;
            applySwitchTint(fpsShadowSwitch, isChecked);

            getSharedPreferences("ftxt_prefs", MODE_PRIVATE)
                    .edit()
                    .putBoolean("fps_shadow", isChecked)
                    .apply();

            FloatingService.updateFpsShadowStatic();
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if(id == R.id.action_settings){
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        if(id == R.id.action_theme){
            boolean isDark = getSharedPreferences("ftxt_prefs", MODE_PRIVATE)
                    .getBoolean("theme_dark", false);
            boolean newDark = !isDark;

            getSharedPreferences("ftxt_prefs", MODE_PRIVATE)
                    .edit()
                    .putBoolean("theme_dark", newDark)
                    .apply();

            if(newDark){
                AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_YES);
            }else{
                AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_NO);
            }

            recreate();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults){

        if(requestCode == 100){
            if(grantResults.length > 0
                    && grantResults[0]
                    == PackageManager
                    .PERMISSION_GRANTED){
                Toast.makeText(
                        this,
                        "Izin notifikasi diberikan",
                        Toast.LENGTH_SHORT
                ).show();
            }else{
                Toast.makeText(
                        this,
                        "Izin notifikasi diperlukan",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    private void applySwitchTint(
            Switch sw,
            boolean isChecked){

        if(isChecked){

            sw.setThumbTintList(
                    ColorStateList.valueOf(
                            Color.parseColor(
                                    "#2196F3"
                            )
                    )
            );

            sw.setTrackTintList(
                    ColorStateList.valueOf(
                            Color.parseColor(
                                    "#90CAF9"
                            )
                    )
            );

        }else{

            sw.setThumbTintList(
                    ColorStateList.valueOf(
                            Color.parseColor(
                                    "#E53935"
                            )
                    )
            );

            sw.setTrackTintList(
                    ColorStateList.valueOf(
                            Color.parseColor(
                                    "#EF9A9A"
                            )
                    )
            );

        }

    }

    // === Text Color Picker ===

    private void showHSVColorPickerDialog(){

        AlertDialog.Builder builder =
                new AlertDialog.Builder(
                        this
                );

        View dialogView = getLayoutInflater()
        .inflate(
                R.layout
                .dialog_hsv_color_picker,
                null
        );

        HSVColorPickerView colorPicker =
                dialogView.findViewById(
                R.id.colorPickerView
        );

        SeekBar brightnessSeekBar =
                dialogView.findViewById(
                R.id.brightnessSeekBar
        );

        SeekBar alphaSeekBar =
                dialogView.findViewById(
                R.id.alphaSeekBar
        );

        View colorPreview =
                dialogView.findViewById(
                R.id.colorPreview
        );

        Button okButton =
                dialogView.findViewById(
                R.id.okButton
        );

        Button cancelButton =
                dialogView.findViewById(
                R.id.cancelButton
        );

        float[] hsv = new float[3];
        Color.colorToHSV(
                currentColor,
                hsv
        );

        colorPicker.setColor(currentColor);

        int currentAlpha =
                Color.alpha(currentColor);

        alphaSeekBar.setProgress(
                currentAlpha
        );

        brightnessSeekBar.setProgress(
                (int)(hsv[2] * 100)
        );

        updateColorPreview(
                colorPreview,
                colorPicker,
                brightnessSeekBar,
                alphaSeekBar
        );

        colorPicker
        .setOnColorChangeListener(
                color -> {

            updateColorPreview(
                    colorPreview,
                    colorPicker,
                    brightnessSeekBar,
                    alphaSeekBar
            );

        });

        brightnessSeekBar
        .setOnSeekBarChangeListener(
                new SeekBar
                .OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(
                    SeekBar seekBar,
                    int progress,
                    boolean fromUser){

                updateColorPreview(
                        colorPreview,
                        colorPicker,
                        brightnessSeekBar,
                        alphaSeekBar
                );

            }

            @Override
            public void onStartTrackingTouch(
                    SeekBar seekBar){}

            @Override
            public void onStopTrackingTouch(
                    SeekBar seekBar){}

        });

        alphaSeekBar
        .setOnSeekBarChangeListener(
                new SeekBar
                .OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(
                    SeekBar seekBar,
                    int progress,
                    boolean fromUser){

                updateColorPreview(
                        colorPreview,
                        colorPicker,
                        brightnessSeekBar,
                        alphaSeekBar
                );

            }

            @Override
            public void onStartTrackingTouch(
                    SeekBar seekBar){}

            @Override
            public void onStopTrackingTouch(
                    SeekBar seekBar){}

        });

        builder.setTitle("Pilih Warna");
        builder.setView(dialogView);

        AlertDialog dialog =
                builder.create();

        okButton.setOnClickListener(v -> {

            currentColor =
                    getColorWithAlpha(
                    colorPicker,
                    brightnessSeekBar,
                    alphaSeekBar
            );

            FloatingService
            .updateTextColorStatic();

            dialog.dismiss();

        });

        cancelButton.setOnClickListener(v -> {

            dialog.dismiss();

        });

        dialog.show();

    }

    // === FPS Color Picker ===

    private void showFpsColorPickerDialog(){

        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);

        View dialogView = getLayoutInflater()
                .inflate(R.layout.dialog_hsv_color_picker, null);

        HSVColorPickerView colorPicker =
                dialogView.findViewById(R.id.colorPickerView);

        SeekBar brightnessSeekBar =
                dialogView.findViewById(R.id.brightnessSeekBar);

        SeekBar alphaSeekBar =
                dialogView.findViewById(R.id.alphaSeekBar);

        View colorPreview =
                dialogView.findViewById(R.id.colorPreview);

        Button okButton =
                dialogView.findViewById(R.id.okButton);

        Button cancelButton =
                dialogView.findViewById(R.id.cancelButton);

        float[] hsv = new float[3];
        Color.colorToHSV(fpsColor, hsv);

        colorPicker.setColor(fpsColor);

        int currentAlpha = Color.alpha(fpsColor);

        alphaSeekBar.setProgress(currentAlpha);
        brightnessSeekBar.setProgress((int)(hsv[2] * 100));

        updateColorPreview(colorPreview, colorPicker, brightnessSeekBar, alphaSeekBar);

        colorPicker.setOnColorChangeListener(color -> {
            updateColorPreview(colorPreview, colorPicker, brightnessSeekBar, alphaSeekBar);
        });

        brightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override public void onProgressChanged(SeekBar sb, int p, boolean f){
                updateColorPreview(colorPreview, colorPicker, brightnessSeekBar, alphaSeekBar);
            }
            @Override public void onStartTrackingTouch(SeekBar sb){}
            @Override public void onStopTrackingTouch(SeekBar sb){}
        });

        alphaSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override public void onProgressChanged(SeekBar sb, int p, boolean f){
                updateColorPreview(colorPreview, colorPicker, brightnessSeekBar, alphaSeekBar);
            }
            @Override public void onStartTrackingTouch(SeekBar sb){}
            @Override public void onStopTrackingTouch(SeekBar sb){}
        });

        builder.setTitle("Pilih Warna FPS");
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        okButton.setOnClickListener(v -> {
            fpsColor = getColorWithAlpha(colorPicker, brightnessSeekBar, alphaSeekBar);
            FloatingService.updateFpsColorStatic();

            getSharedPreferences("ftxt_prefs", MODE_PRIVATE)
                    .edit()
                    .putInt("fps_color", fpsColor)
                    .apply();

            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateColorPreview(
            View preview,
            HSVColorPickerView colorPicker,
            SeekBar brightnessSeekBar,
            SeekBar alphaSeekBar){

        int color = getColorWithAlpha(
                colorPicker,
                brightnessSeekBar,
                alphaSeekBar
        );

        preview.setBackgroundColor(color);

    }

    private int getColorWithAlpha(
            HSVColorPickerView colorPicker,
            SeekBar brightnessSeekBar,
            SeekBar alphaSeekBar){

        int baseColor =
                colorPicker.getCurrentColor();

        float brightness =
                brightnessSeekBar.getProgress()
                / 100f;

        int alpha = alphaSeekBar
        .getProgress();

        float[] hsv = new float[3];
        Color.colorToHSV(baseColor, hsv);

        hsv[2] = hsv[2] * brightness;

        int colorWithBrightness =
                Color.HSVToColor(hsv);

        int r = Color.red(
                colorWithBrightness);

        int g = Color.green(
                colorWithBrightness);

        int b = Color.blue(
                colorWithBrightness);

        return Color.argb(
                alpha,
                r,
                g,
                b
        );

    }
}
