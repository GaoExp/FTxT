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

            String[] names = {

                "White",
                "Red",
                "Green",
                "Blue",
                "Yellow",
                "Magenta",
                "Cyan"

            };

            int[] colors = {

                Color.WHITE,
                Color.RED,
                Color.GREEN,
                Color.BLUE,
                Color.YELLOW,
                Color.MAGENTA,
                Color.CYAN

            };

            AlertDialog.Builder builder =
                    new AlertDialog.Builder(
                            this
                    );

            builder.setTitle(
                    "Pilih Warna"
            );

            builder.setItems(
                    names,
                    (d,which)->{

                currentColor =
                        colors[which];

            });

            builder.show();

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

}