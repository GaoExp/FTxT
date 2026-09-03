package exp.ftxt.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import exp.ftxt.R;
import exp.ftxt.features.battery_stats.BatteryHistoryDb;

/**
 * Halaman detail satu sesi (pengisian/pengosongan). Struktur tampilannya
 * mengikuti panel "Sesi Berjalan" agar posisi informasi sejajar dan mudah
 * dibaca: badge jenis sesi di header, kartu utama berisi ring level
 * (skema putih–hijau–merah) + ringkasan, lalu kartu status &amp; rincian
 * dengan baris label↔nilai. Data dikirim melalui extra saat membuka halaman.
 */
public class SessionDetailActivity extends AppCompatActivity {

    private static final String E_IS_CHARGE = "isCharge";
    private static final String E_START = "startTime";
    private static final String E_END = "endTime";
    private static final String E_START_PCT = "startPercent";
    private static final String E_END_PCT = "endPercent";
    private static final String E_MAH_COUNTER = "mahCounter";
    private static final String E_MAH_INTEGRAL = "mahIntegral";
    private static final String E_DELTA_MAH = "deltaMah";
    private static final String E_CAPACITY = "capacityMah";
    private static final String E_EFFICIENCY = "efficiencyPercent";
    private static final String E_SCREEN_OFF = "screenOff";
    private static final String E_TEMP_MIN = "tempMin";
    private static final String E_TEMP_MAX = "tempMax";
    private static final String E_TEMP_AVG = "tempAvg";
    private static final String E_SAMPLES = "sampleCount";

    private static final SimpleDateFormat FMT = new SimpleDateFormat("dd/MM/yy HH:mm:ss", Locale.US);

    private int monitorLabelColor;

    public static void start(Context context, BatteryHistoryDb.SessionEntry e) {
        Intent intent = new Intent(context, SessionDetailActivity.class);
        intent.putExtra(E_IS_CHARGE, e.isCharge);
        intent.putExtra(E_START, e.startTime);
        intent.putExtra(E_END, e.endTime);
        intent.putExtra(E_START_PCT, e.startPercent);
        intent.putExtra(E_END_PCT, e.endPercent);
        intent.putExtra(E_MAH_COUNTER, e.mAhCounter);
        intent.putExtra(E_MAH_INTEGRAL, e.mAhIntegral);
        intent.putExtra(E_DELTA_MAH, e.deltaChargeMah);
        intent.putExtra(E_CAPACITY, e.capacityMah);
        intent.putExtra(E_EFFICIENCY, e.efficiencyPercent);
        intent.putExtra(E_SCREEN_OFF, e.screenOffDominant);
        intent.putExtra(E_TEMP_MIN, e.tempMin);
        intent.putExtra(E_TEMP_MAX, e.tempMax);
        intent.putExtra(E_TEMP_AVG, e.tempAvg);
        intent.putExtra(E_SAMPLES, e.sampleCount);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_detail);

        monitorLabelColor = getColor(R.color.bat_monitor_label);

        boolean isCharge = getIntent().getBooleanExtra(E_IS_CHARGE, true);
        long start = getIntent().getLongExtra(E_START, 0L);
        long end = getIntent().getLongExtra(E_END, 0L);
        int startPct = getIntent().getIntExtra(E_START_PCT, -1);
        int endPct = getIntent().getIntExtra(E_END_PCT, -1);
        double mahCounter = getIntent().getDoubleExtra(E_MAH_COUNTER, 0d);
        double mahIntegral = getIntent().getDoubleExtra(E_MAH_INTEGRAL, 0d);
        double deltaMah = getIntent().getDoubleExtra(E_DELTA_MAH, 0d);
        float capacity = getIntent().getFloatExtra(E_CAPACITY, 0f);
        float efficiency = getIntent().getFloatExtra(E_EFFICIENCY, -1f);
        boolean screenOff = getIntent().getBooleanExtra(E_SCREEN_OFF, false);
        float tempMin = getIntent().getFloatExtra(E_TEMP_MIN, 0f);
        float tempMax = getIntent().getFloatExtra(E_TEMP_MAX, 0f);
        float tempAvg = getIntent().getFloatExtra(E_TEMP_AVG, 0f);
        int samples = getIntent().getIntExtra(E_SAMPLES, 0);

        int accent = getColor(isCharge ? R.color.bat_monitor_active : R.color.bat_chart_power);
        String statusWord = isCharge ? "PENGISIAN" : "PENGOSONGAN";
        String statusRing = isCharge ? "Mengisi" : "Menguras";
        String shortWord = isCharge ? "MENGISI" : "MEMAKAI";

        TextView title = findViewById(R.id.sesDetailTitle);
        TextView typeTag = findViewById(R.id.sesDetailTypeTag);
        title.setText(isCharge ? "Detail Pengisian" : "Detail Pengosongan");
        title.setTextColor(accent);
        typeTag.setText(shortWord);
        typeTag.setTextColor(accent);
        typeTag.setBackgroundResource(isCharge
                ? R.drawable.bat_badge_active_bg : R.drawable.bat_badge_stopped_bg);

        // Kartu utama
        TextView statusBig = findViewById(R.id.sesDetailStatusBig);
        statusBig.setText(statusWord);
        statusBig.setTextColor(accent);

        TextView rangeBadge = findViewById(R.id.sesDetailRangeBadge);
        String pct = startPct >= 0 && endPct >= 0
                ? startPct + "% → " + endPct + "%" : pctNone();
        rangeBadge.setText(pct);

        exp.ftxt.features.battery_stats.BatteryRingView ring =
                findViewById(R.id.sesDetailRing);
        ring.setSessionData(
                endPct >= 0 ? endPct : 0,
                startPct >= 0 ? startPct : 0,
                isCharge,
                mahLabel(isCharge ? mahIntegral : altIntegral(mahIntegral, mahCounter)),
                statusRing);

        setText(R.id.sesDetailRange, FMT.format(new Date(start)) + " – " + FMT.format(new Date(end)));
        setText(R.id.sesDetailDuration, formatDuration(end - start));
        setText(R.id.sesDetailRate, String.format(Locale.US, "%+.1f %%/jam", ratePct(startPct, endPct, end - start)));

        // Kartu Status / Energi
        TextView statusTitle = findViewById(R.id.sesDetailStatusTitle);
        statusTitle.setText(isCharge ? "Status pengisian" : "Status pelepasan");
        statusTitle.setTextColor(accent);

        TextView statusSubtitle = findViewById(R.id.sesDetailStatusSubtitle);
        statusSubtitle.setText(isCharge
                ? "Detail energi yang masuk selama pengisian"
                : "Detail energi yang terpakai selama pengosongan");

        LinearLayout statusRows = findViewById(R.id.sesDetailStatusRows);
        statusRows.removeAllViews();
        double energyIntegral = isCharge ? mahIntegral : altIntegral(mahIntegral, mahCounter);
        double energyCoulomb = mahCounter;
        addValueRow(statusRows, isCharge ? "Energi masuk" : "Energi terpakai",
                mahLabel(energyIntegral), accent);
        addValueRow(statusRows, "mAh (Coulomb)", mahLabel(energyCoulomb));
        if (isCharge) {
            addValueRow(statusRows, "Delta mAh", mahLabel(deltaMah));
        }
        addValueRow(statusRows, "Efisiensi", efficiency >= 0
                ? String.format(Locale.US, "%.0f%%", efficiency) : "—");

        // Kartu Rincian sesi
        TextView detailTitle = findViewById(R.id.sesDetailDetailTitle);
        detailTitle.setText("Rincian sesi");
        detailTitle.setTextColor(accent);

        LinearLayout detailRows = findViewById(R.id.sesDetailDetailRows);
        detailRows.removeAllViews();
        addValueRow(detailRows, "Suhu",
                String.format(Locale.US, "%.1f–%.1f°C (%.1f)", tempMin, tempMax, tempAvg),
                monitorLabelColor);
        addValueRow(detailRows, "Min", tempLabel(tempMin));
        addValueRow(detailRows, "Maks", tempLabel(tempMax));
        addValueRow(detailRows, "Rata²", tempLabel(tempAvg));
        addValueRow(detailRows, "Kapasitas terestimasi",
                capacity > 0 ? String.format(Locale.US, "%.0f mAh", capacity) : "—");
        addValueRow(detailRows, "Kondisi layar",
                screenOff ? "Layar mati dominan" : "Layar menyala");
        addValueRow(detailRows, "Sampel", samples > 0 ? String.valueOf(samples) : "—");

        findViewById(R.id.sesDetailBack).setOnClickListener(v -> finish());
    }

    /** Untuk pengosongan, ambil integral; fallback counter bila integral 0. */
    private double altIntegral(double mahIntegral, double mahCounter) {
        return mahIntegral > 0 ? mahIntegral : mahCounter;
    }

    private float ratePct(int startPct, int endPct, long durMs) {
        if (startPct < 0 || endPct < 0 || durMs <= 0) return 0f;
        float delta = endPct - startPct;
        float hours = durMs / 3600000f;
        if (hours <= 0.01f) return 0f;
        return delta / hours;
    }

    private String pctNone() {
        return "—";
    }

    private String mahLabel(double v) {
        return v > 0 ? String.format(Locale.US, "%.0f mAh", v) : "—";
    }

    private String tempLabel(float v) {
        return v > 0 ? String.format(Locale.US, "%.1f°C", v) : "—";
    }

    private String formatDuration(long ms) {
        if (ms <= 0) return "—";
        long totalMin = (ms + 59_000L) / 60_000L;
        if (totalMin < 1) return "<1 mnt";
        if (totalMin < 60) return totalMin + " mnt";
        long jam = totalMin / 60;
        long sisaMin = totalMin % 60;
        if (sisaMin == 0) return jam + " jam";
        return jam + "j " + sisaMin + "m";
    }

    private void addValueRow(LinearLayout container, String label, String value) {
        addValueRow(container, label, value, getColor(R.color.bat_monitor_header));
    }

    private void addValueRow(LinearLayout container, String label, String value, int valueColor) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        rowLp.topMargin = dp(4);
        row.setLayoutParams(rowLp);

        TextView l = new TextView(this);
        l.setText(label);
        l.setTextSize(12);
        l.setTextColor(monitorLabelColor);
        l.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(l);

        TextView v = new TextView(this);
        v.setText(value);
        v.setTextSize(12);
        v.setTypeface(Typeface.DEFAULT_BOLD);
        v.setTextColor(valueColor);
        v.setGravity(Gravity.END);
        row.addView(v);
        container.addView(row);
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private void setText(int id, String text) {
        TextView tv = findViewById(id);
        if (tv != null) tv.setText(text);
    }
}
