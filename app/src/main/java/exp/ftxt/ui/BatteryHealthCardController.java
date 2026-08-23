package exp.ftxt.ui;

import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.util.Locale;

import exp.ftxt.MainActivity;
import exp.ftxt.R;
import exp.ftxt.features.battery_stats.BatteryCapacityEstimator;

public class BatteryHealthCardController {

    private final MainActivity activity;

    private TextView batHealthText;
    private TextView batHealthDesignText;
    private TextView batHealthSessionBadge;
    private int monitorLabelColor;

    public BatteryHealthCardController(MainActivity activity, View rootView) {
        this.activity = activity;
        bindViews(rootView);
        batHealthDesignText.setOnClickListener(v -> showDesignCapacityDialog());
    }

    private void bindViews(View rootView) {
        batHealthText = rootView.findViewById(R.id.batHealthText);
        batHealthDesignText = rootView.findViewById(R.id.batHealthDesignText);
        batHealthSessionBadge = rootView.findViewById(R.id.batHealthSessionBadge);
        monitorLabelColor = activity.getColor(R.color.bat_monitor_label);
    }

    public void refresh() {
        if (batHealthText == null) return;
        BatteryCapacityEstimator.HealthResult r = BatteryCapacityEstimator.getResult();

        SpannableStringBuilder sb = new SpannableStringBuilder();
        appendLineColored(sb, "Estimasi Kapasitas",
                r.medianMah > 0 ? String.format(Locale.US, "%.0f mAh", r.medianMah) : "—", null);

        int scoreColor;
        String score;
        if (r.designMah <= 0) {
            score = "Isi kapasitas desain";
            scoreColor = monitorLabelColor;
        } else if (r.medianMah <= 0) {
            score = "Belum ada data";
            scoreColor = monitorLabelColor;
        } else {
            float pctScore = r.medianMah / r.designMah * 100f;
            score = String.format(Locale.US, "%.1f%%", pctScore);
            scoreColor = activity.getColor(pctScore >= 80f ? R.color.bat_monitor_active
                    : pctScore >= 50f ? R.color.bat_monitor_header : R.color.bat_monitor_stop);
        }
        appendLineColored(sb, "Skor Kesehatan", score, scoreColor);

        appendLineColored(sb, "Sesi Tercatat", String.valueOf(r.sessionCount), null);
        String confidence = r.totalSamples > 0
                ? (r.fromScreenOffSessions
                        ? r.totalSamples + " sampel (layar mati)"
                        : r.totalSamples + " sampel")
                : "—";
        appendLineColored(sb, "Keyakinan", confidence,
                r.totalSamples <= 0 ? monitorLabelColor : null);

        boolean collecting = BatteryCapacityEstimator.isSegmentActive();
        String status = collecting
                ? "Mengumpulkan saat mengisi…"
                : "Menunggu pengisian daya";
        appendLineColored(sb, "Status", status,
                collecting ? activity.getColor(R.color.bat_monitor_active) : monitorLabelColor);

        batHealthText.setText(sb);
        batHealthSessionBadge.setText(r.sessionCount + " sesi");
        batHealthDesignText.setText(r.designMah > 0
                ? "Kapasitas Desain: " + r.designMah + " mAh · Ketuk untuk mengatur"
                : "Kapasitas Desain: Belum diatur · Ketuk untuk mengatur");
    }

    private void appendLineColored(SpannableStringBuilder sb, String label, String value, Integer valueColor) {
        String padded = String.format(Locale.US, "%-17s", label);
        int start = sb.length();
        sb.append(padded).append(value).append("\n");
        sb.setSpan(new ForegroundColorSpan(monitorLabelColor),
                start, start + padded.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (valueColor != null) {
            sb.setSpan(new ForegroundColorSpan(valueColor),
                    start + padded.length(), sb.length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private void showDesignCapacityDialog() {
        final EditText input = new EditText(activity);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        int current = BatteryCapacityEstimator.getResult().designMah;
        input.setText(current > 0 ? String.valueOf(current) : "");
        input.setHint("mis. 5000");

        new AlertDialog.Builder(activity)
                .setTitle("Kapasitas Desain")
                .setMessage("Masukkan kapasitas desain baterai (mAh) sesuai spesifikasi pabrik. "
                        + "Skor kesehatan hanya dihitung jika kolom ini terisi. Kosongkan untuk menghapus.")
                .setView(input)
                .setPositiveButton("Simpan", (dialog, which) -> {
                    String txt = input.getText().toString().trim();
                    int value = 0;
                    if (!txt.isEmpty()) {
                        try {
                            value = Integer.parseInt(txt);
                        } catch (NumberFormatException ignored) {}
                    }
                    if (value != 0 && (value < 500 || value > 30000)) {
                        Toast.makeText(activity, "Kapasitas harus 500–30000 mAh", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    BatteryCapacityEstimator.setDesignCapacity(value);
                    refresh();
                })
                .setNegativeButton("Batal", null)
                .show();
    }
}
