package exp.ftxt.ui;

import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import exp.ftxt.MainActivity;
import exp.ftxt.R;
import exp.ftxt.features.battery_stats.BatteryCapacityEstimator;
import exp.ftxt.shared.ui.InfoTooltip;

public class BatteryHealthCardController {

    private final MainActivity activity;
    private final ExecutorService healthExecutor = Executors.newSingleThreadExecutor();

    private TextView batHealthText;
    private TextView batHealthDesignText;
    private TextView batHealthSessionBadge;
    private View batHealthResetButton;
    private int monitorLabelColor;

    public BatteryHealthCardController(MainActivity activity, View pageView) {
        this.activity = activity;
        bindViews(pageView);
        batHealthDesignText.setOnClickListener(v -> showDesignCapacityDialog());
        batHealthResetButton.setOnClickListener(v -> showResetConfirmDialog());
        pageView.findViewById(R.id.batHealthInfoButton)
                .setOnClickListener(v -> InfoTooltip.show(activity, v,
                        "Kesehatan Baterai",
                        "Estimasi kapasitas = perkiraan kapasitas penuh baterai, "
                                + "ditampilkan dalam tiga nilai: dari segmen pengisian, "
                                + "dari segmen pengosongan, dan gabungan keduanya. "
                                + "Semua memakai integrasi arus (I) diagregasi median "
                                + "lintas sesi valid.\n\n"
                                + "Skor kesehatan = estimasi GABUNGAN ÷ kapasitas desain. "
                                + "Isi kapasitas desain (mAh) sesuai spesifikasi pabrik "
                                + "agar skor muncul."));
    }

    private void bindViews(View rootView) {
        batHealthText = rootView.findViewById(R.id.batHealthText);
        batHealthDesignText = rootView.findViewById(R.id.batHealthDesignText);
        batHealthSessionBadge = rootView.findViewById(R.id.batHealthSessionBadge);
        batHealthResetButton = rootView.findViewById(R.id.batHealthResetButton);
        monitorLabelColor = activity.getColor(R.color.bat_monitor_label);
    }
    public void refresh() {
        if (batHealthText == null) return;
        healthExecutor.execute(() -> {
            BatteryCapacityEstimator.HealthResult r;
            try {
                r = BatteryCapacityEstimator.getResult();
            } catch (Exception ignored) {
                return;
            }
            activity.runOnUiThread(() -> applyResult(r));
        });
    }

    void applyResult(BatteryCapacityEstimator.HealthResult r) {
        if (batHealthText == null) return;

        float design = r.designMah > 0 ? r.designMah : 0f;

        int scoreColor = monitorLabelColor;
        if (design > 0 && r.medianMah > 0) {
            float pctScore = r.medianMah / design * 100f;
            scoreColor = activity.getColor(pctScore >= 80f ? R.color.bat_monitor_active
                    : pctScore >= 50f ? R.color.bat_monitor_header : R.color.bat_monitor_stop);
        }

        SpannableStringBuilder sb = new SpannableStringBuilder();
        appendLineColored(sb, "Kapasitas Pengisian", capacityWithPct(r.chargeMedianMah, design), null);
        appendLineColored(sb, "Kapasitas Pengosongan", capacityWithPct(r.dischargeMedianMah, design), null);
        appendLineColored(sb, "Kapasitas Gabungan", capacityWithPct(r.medianMah, design), scoreColor);

        appendLineColored(sb, "Sesi Valid Tercatat", String.valueOf(r.sessionCount), null);

        String confidence = r.totalSamples > 0
                ? r.totalSamples + " sampel"
                : "—";
        appendLineColored(sb, "Jumlah Data", confidence,
                r.totalSamples <= 0 ? monitorLabelColor : null);

        batHealthText.setText(sb);
        batHealthSessionBadge.setText(r.sessionCount + " sesi valid");
        batHealthDesignText.setText(r.designMah > 0
                ? "Kapasitas Desain: " + r.designMah + " mAh · Ketuk untuk mengatur"
                : "Kapasitas Desain: Belum diatur · Ketuk untuk mengatur");
    }

    private void appendLineColored(SpannableStringBuilder sb, String label, String value, Integer valueColor) {
        String padded = String.format(Locale.US, "%-20s: ", label);
        int start = sb.length();
        sb.append(padded).append(value).append("\n");
        sb.setSpan(new ForegroundColorSpan(monitorLabelColor),
                start, start + padded.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (valueColor != null) {
            sb.setSpan(new ForegroundColorSpan(valueColor),
                    start + padded.length(), sb.length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private String capacityWithPct(float v, float design) {
        if (v <= 0f) return "—";
        String base = String.format(Locale.US, "%.0f mAh", v);
        if (design > 0f) {
            float pct = v / design * 100f;
            return String.format(Locale.US, "%s (%.1f%%)", base, pct);
        }
        return base;
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

    private void showResetConfirmDialog() {
        BatteryCapacityEstimator.HealthResult r = BatteryCapacityEstimator.getResult();
        if (r.sessionCount == 0) {
            Toast.makeText(activity, "Belum ada data untuk di-reset", Toast.LENGTH_SHORT).show();
            return;
        }

        final EditText input = new EditText(activity);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Ketik RESET di sini");

        FrameLayout container = new FrameLayout(activity);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(48, 16, 48, 0);
        input.setLayoutParams(params);
        container.addView(input);

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle("Reset Data Estimasi")
                .setMessage("Hapus semua " + r.sessionCount + " sesi yang tercatat (pengisian & pengosongan)? "
                        + "Estimasi kapasitas dan skor kesehatan akan dihitung ulang dari nol "
                        + "saat sesi berikutnya berlangsung.\n\n"
                        + "Ketik \"RESET\" untuk melanjutkan.")
                .setView(container)
                .setPositiveButton("Reset", (d, which) -> {
                    BatteryCapacityEstimator.resetEstimationData();
                    refresh();
                    Toast.makeText(activity, "Data estimasi berhasil direset", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Batal", null)
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setEnabled("RESET".equals(s.toString()));
            }
        });
    }
}
