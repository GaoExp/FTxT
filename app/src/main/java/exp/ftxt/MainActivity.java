package exp.ftxt;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    public static String currentText = "FTxT AKTIF";

    EditText editText;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.editText);
        button = findViewById(R.id.button);

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