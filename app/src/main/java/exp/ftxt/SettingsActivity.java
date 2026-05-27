package exp.ftxt;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.view.View;
import android.util.TypedValue;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

/**
 * Activity Pengaturan untuk menampilkan CHANGELOG dan README dalam dialog scrollable.
 *
 * Membaca file dokumentasi langsung dari assets/:
 * - assets/CHANGELOG.txt
 * - assets/README.txt
 *
 * Fitur:
 * - Tombol −/+ untuk zoom ukuran teks (4–60sp)
 * - ScrollView untuk konten panjang
 *
 * Dipanggil oleh:
 * - MainActivity → MainActivity.java (onOptionsItemSelected — action_settings)
 *
 * File dokumentasi dikelola di:
 * - assets/AGENTS.txt   → lihat AGENTS.txt
 * - assets/CHANGELOG.txt → lihat CHANGELOG.txt
 * - assets/README.txt   → lihat README.txt
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        Button changelogButton = findViewById(R.id.changelogButton);
        Button readmeButton = findViewById(R.id.readmeButton);

        changelogButton.setOnClickListener(v -> showChangelogDialog());
        readmeButton.setOnClickListener(v -> showReadmeDialog());
    }

    private void showChangelogDialog() {
        String content = readAssetFile("CHANGELOG.txt");
        showContentDialog("CHANGELOG", content);
    }

    private void showReadmeDialog() {
        String content = readAssetFile("README.txt");
        showContentDialog("README", content);
    }

    private float currentTextSize = 14;

    private void showContentDialog(String title, String content) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);

        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        controls.setPadding(16, 8, 16, 4);

        Button minusBtn = new Button(this);
        minusBtn.setText("−");
        minusBtn.setTextSize(18);
        controls.addView(minusBtn);

        TextView sizeLabel = new TextView(this);
        sizeLabel.setText(String.format("%.0f sp", currentTextSize));
        sizeLabel.setPadding(16, 0, 16, 0);
        sizeLabel.setGravity(android.view.Gravity.CENTER);
        sizeLabel.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        controls.addView(sizeLabel);

        Button plusBtn = new Button(this);
        plusBtn.setText("+");
        plusBtn.setTextSize(18);
        controls.addView(plusBtn);

        ScrollView scrollView = new ScrollView(this);
        TextView textView = new TextView(this);
        textView.setText(content);
        int paddingPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, getResources().getDisplayMetrics());
        textView.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
        textView.setTextSize(currentTextSize);
        scrollView.addView(textView);

        root.addView(controls);
        root.addView(scrollView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));

        minusBtn.setOnClickListener(v -> {
            if (currentTextSize > 4) {
                currentTextSize -= 2;
                textView.setTextSize(currentTextSize);
                sizeLabel.setText(String.format("%.0f sp", currentTextSize));
            }
        });

        plusBtn.setOnClickListener(v -> {
            if (currentTextSize < 60) {
                currentTextSize += 2;
                textView.setTextSize(currentTextSize);
                sizeLabel.setText(String.format("%.0f sp", currentTextSize));
            }
        });

        builder.setView(root);
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private String readAssetFile(String filename) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(getAssets().open(filename)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            content.append("Error reading file: ").append(e.getMessage());
        }
        return content.toString();
    }
}

