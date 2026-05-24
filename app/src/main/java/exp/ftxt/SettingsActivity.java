package exp.ftxt;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button changelogButton = findViewById(R.id.changelogButton);
        Button readmeButton = findViewById(R.id.readmeButton);

        changelogButton.setOnClickListener(v -> showChangelogDialog());
        readmeButton.setOnClickListener(v -> showReadmeDialog());
    }

    private void showChangelogDialog() {
        String content = readAssetFile("CHANGELOG.md");
        showContentDialog("CHANGELOG", content);
    }

    private void showReadmeDialog() {
        String content = readAssetFile("README.md");
        showContentDialog("README", content);
    }

    private void showContentDialog(String title, String content) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        ScrollView scrollView = new ScrollView(this);
        TextView textView = new TextView(this);
        textView.setText(content);
        textView.setPadding(24, 24, 24, 24);
        textView.setTextSize(12);
        scrollView.addView(textView);

        builder.setView(scrollView);
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

