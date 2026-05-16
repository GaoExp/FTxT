package exp.ftxt;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

public class MainActivity extends AppCompatActivity {

    public static String currentText = "FTxT AKTIF";
    public static float currentTextSize = 20f;
    public static int currentTextColor = Color.WHITE;

    EditText editText;
    Button button;
    SeekBar textSizeSeekBar;
    TextView textSizeValue;
    Button colorPickerButton;
    TextView colorValueText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.editText);
        button = findViewById(R.id.button);
        textSizeSeekBar = findViewById(R.id.textSizeSeekBar);
        textSizeValue = findViewById(R.id.textSizeValue);
        colorPickerButton = findViewById(R.id.colorPickerButton);
        colorValueText = findViewById(R.id.colorValueText);

        // Update color button background dengan warna awal
        updateColorButtonBackground(currentTextColor);

        // Color picker button listener
        colorPickerButton.setOnClickListener(v -> showColorPicker());

        // SeekBar listener untuk update ukuran teks
        textSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentTextSize = progress;
                textSizeValue.setText(String.valueOf(progress));

                // Kirim broadcast update ukuran teks
                Intent updateSizeIntent = new Intent("exp.ftxt.UPDATE_TEXT_SIZE");
                updateSizeIntent.putExtra("size", currentTextSize);
                sendBroadcast(updateSizeIntent);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        button.setOnClickListener(v -> {

            currentText = editText.getText().toString();

            Intent updateIntent = new Intent("exp.ftxt.UPDATE_TEXT");
            updateIntent.putExtra("text", currentText);
            sendBroadcast(updateIntent);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                if (!Settings.canDrawOverlays(this)) {

                    Intent intent = new Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName())
                    );

                    startActivity(intent);

                } else {

                    startService(new Intent(this, FloatingService.class));
                }

            } else {

                startService(new Intent(this, FloatingService.class));
            }
        });
    }

    private void showColorPicker() {
        // Predefined colors untuk dipilih
        String[] colorNames = {"Putih", "Merah", "Hijau", "Biru", "Kuning", "Magenta", "Cyan", "Hitam"};
        int[] colors = {
            Color.WHITE,
            Color.RED,
            Color.GREEN,
            Color.BLUE,
            Color.YELLOW,
            Color.MAGENTA,
            Color.CYAN,
            Color.BLACK
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih Warna Teks");
        builder.setItems(colorNames, (dialog, which) -> {
            currentTextColor = colors[which];
            updateColorButtonBackground(currentTextColor);
            colorValueText.setText(String.format("#%06X", (0xFFFFFF & currentTextColor)));
            colorValueText.setTextColor(currentTextColor);

            // Kirim broadcast update warna teks
            Intent updateColorIntent = new Intent("exp.ftxt.UPDATE_TEXT_COLOR");
            updateColorIntent.putExtra("color", currentTextColor);
            sendBroadcast(updateColorIntent);
        });
        builder.show();
    }

    private void updateColorButtonBackground(int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(color);
        drawable.setStroke(3, Color.BLACK);
        drawable.setCornerRadius(5f);
        colorPickerButton.setBackground(drawable);
    }
}