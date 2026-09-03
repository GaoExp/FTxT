package exp.ftxt;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import io.noties.markwon.Markwon;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.ext.tasklist.TaskListPlugin;

public class DocumentationActivity extends AppCompatActivity {

    private Markwon markwon;
    private LinearLayout docList;
    private LinearLayout docContent;
    private Toolbar toolbar;
    private TextView docTextView;
    private float currentDocTextSize = 14;
    private View zoomView;
    private TextView zoomValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documentation);

        markwon = Markwon.builder(this)
                .usePlugin(TablePlugin.create(this))
                .usePlugin(TaskListPlugin.create(this))
                .build();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> {
            if (docContent.getVisibility() == View.VISIBLE) {
                showList();
            } else {
                finish();
            }
        });

        docList = findViewById(R.id.docList);
        docContent = findViewById(R.id.docContent);
        docTextView = findViewById(R.id.docTextView);
        docTextView.setMovementMethod(LinkMovementMethod.getInstance());
        docTextView.setLineSpacing(0, 1.15f);

        zoomView = getLayoutInflater().inflate(R.layout.toolbar_zoom, toolbar, false);
        zoomValue = zoomView.findViewById(R.id.toolbarZoomValue);

        zoomView.findViewById(R.id.toolbarZoomOut).setOnClickListener(v -> {
            if (currentDocTextSize > 4) {
                currentDocTextSize -= 2;
                docTextView.setTextSize(currentDocTextSize);
                zoomValue.setText(String.valueOf((int) currentDocTextSize));
            }
        });

        zoomView.findViewById(R.id.toolbarZoomIn).setOnClickListener(v -> {
            if (currentDocTextSize < 60) {
                currentDocTextSize += 2;
                docTextView.setTextSize(currentDocTextSize);
                zoomValue.setText(String.valueOf((int) currentDocTextSize));
            }
        });

        findViewById(R.id.docReadmeButton).setOnClickListener(v -> showDoc("README"));
        findViewById(R.id.docChangelogButton).setOnClickListener(v -> showDoc("CHANGELOG"));
        findViewById(R.id.docOldChangelogButton).setOnClickListener(v -> showDoc("old-CHANGELOG"));
        findViewById(R.id.docPanduanButton).setOnClickListener(v -> showDoc("PANDUAN"));
        findViewById(R.id.docStrukturButton).setOnClickListener(v -> showDoc("STRUKTUR"));
    }

    private void showDoc(String name) {
        String content = readAssetFile(name + ".md");
        markwon.setMarkdown(docTextView, content);
        docTextView.setTextSize(currentDocTextSize);
        zoomValue.setText(String.valueOf((int) currentDocTextSize));
        docList.setVisibility(View.GONE);
        docContent.setVisibility(View.VISIBLE);
        toolbar.setTitle(name);
        toolbar.addView(zoomView, new Toolbar.LayoutParams(
                Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.MATCH_PARENT,
                android.view.Gravity.END));
    }

    private void showList() {
        docContent.setVisibility(View.GONE);
        docList.setVisibility(View.VISIBLE);
        toolbar.setTitle("Dokumentasi");
        toolbar.removeView(zoomView);
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
