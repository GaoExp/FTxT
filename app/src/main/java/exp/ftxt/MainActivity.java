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
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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

    EditText editText;

    SeekBar seekBar;

    Button colorButton;

    Switch overlaySwitch;

    Switch touchPassthroughSwitch;

    Switch shadowSwitch;

    @Override
    protected void onCreate(
            Bundle savedInstanceState){

        super.onCreate(
                savedInstanceState
        );

        setContentView(
                R.layout.activity_main
        );

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
                            "MTxT AKTIF";

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

        // Load saved shadow preference and apply
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