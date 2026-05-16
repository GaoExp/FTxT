package exp.ftxt;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (!Settings.canDrawOverlays(this)) {

                Intent intent = new Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName())
                );

                startActivity(intent);

                Toast.makeText(this,
                        "Aktifkan izin overlay lalu buka lagi aplikasi",
                        Toast.LENGTH_LONG).show();

            } else {

                startService(new Intent(this, FloatingService.class));

            }

        } else {

            startService(new Intent(this, FloatingService.class));

        }

        finish();
    }
}