package exp.ftxt.shared.ui;

import android.view.View;
import android.widget.TextView;

public class SectionHelper {

    public static void setupCollapsible(TextView header, View container) {
        String clean = header.getText().toString().replaceAll("^[▸▾]\\s*", "");
        header.setText("▾ " + clean);
        header.setTag(Boolean.TRUE);
        header.setOnClickListener(v -> {
            boolean expanded = (boolean) header.getTag();
            container.setVisibility(expanded ? View.GONE : View.VISIBLE);
            header.setText((expanded ? "▸ " : "▾ ") + clean);
            header.setTag(!expanded);
        });
    }
}
