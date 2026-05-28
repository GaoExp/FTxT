package exp.ftxt;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.util.TypedValue;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class DocumentationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documentation);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Dokumentasi");
        toolbar.setNavigationOnClickListener(v -> finish());

        findViewById(R.id.docReadmeButton).setOnClickListener(v -> showDoc("README"));
        findViewById(R.id.docChangelogButton).setOnClickListener(v -> showDoc("CHANGELOG"));
        findViewById(R.id.docPanduanButton).setOnClickListener(v -> showDoc("PANDUAN"));
        findViewById(R.id.docStrukturButton).setOnClickListener(v -> showDoc("STRUKTUR"));
        findViewById(R.id.docDevelopmentButton).setOnClickListener(v -> showDoc("DEVELOPMENT"));
        findViewById(R.id.docTentangButton).setOnClickListener(v -> showDoc("TENTANG"));
    }

    private void showDoc(String name) {
        String content = readAssetFile(name + ".txt");
        showContentDialog(name, content);
    }

    private float currentDocTextSize = 14;

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
        sizeLabel.setText(String.format("%.0f sp", currentDocTextSize));
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
        textView.setTextSize(currentDocTextSize);
        scrollView.addView(textView);

        root.addView(controls);
        root.addView(scrollView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));

        minusBtn.setOnClickListener(v -> {
            if (currentDocTextSize > 4) {
                currentDocTextSize -= 2;
                textView.setTextSize(currentDocTextSize);
                sizeLabel.setText(String.format("%.0f sp", currentDocTextSize));
            }
        });

        plusBtn.setOnClickListener(v -> {
            if (currentDocTextSize < 60) {
                currentDocTextSize += 2;
                textView.setTextSize(currentDocTextSize);
                sizeLabel.setText(String.format("%.0f sp", currentDocTextSize));
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
