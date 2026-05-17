package exp.ftxt;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
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

            if(isChecked){

                overlaySwitch
                .setText(
                        "Overlay ON"
                );

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

                startService(
                        new Intent(
                                this,
                                FloatingService
                                .class
                        )
                );

            }

            else{

                overlaySwitch
                .setText(
                        "Overlay OFF"
                );

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

            isTouchPassthrough = isChecked;

            FloatingService
            .updateTouchFlagsStatic();

            if(isChecked){

                touchPassthroughSwitch
                .setText(
                        "Teks Terkunci"
                );

            }else{

                touchPassthroughSwitch
                .setText(
                        "Teks Bergerak"
                );

            }

        });

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