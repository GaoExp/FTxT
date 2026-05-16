package exp.ftxt;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    public static String currentText = "FTxT AKTIF";
    public static float currentTextSize = 20f;

    EditText editText;
    Button button;
    SeekBar textSizeSeekBar;
    TextView textSizeValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.editText);
        button = findViewById(R.id.button);
        textSizeSeekBar = findViewById(R.id.textSizeSeekBar);
        textSizeValue = findViewById(R.id.textSizeValue);

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
}