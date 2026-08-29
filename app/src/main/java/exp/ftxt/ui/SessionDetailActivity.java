package exp.ftxt.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import exp.ftxt.R;
import exp.ftxt.features.battery_stats.BatteryHistoryDb;

/**
 * Halaman detail satu sesi (pengisian/pengosongan): waktu, durasi, level,
 * energi mAh, suhu, efisiensi, dan karakteristik sesi. Data dikirim melalui
 * extra saat membuka halaman — tanpa query tambahan.
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
        TextView title = findViewById(R.id.sesDetailTitle);
        TextView typeTag = findViewById(R.id.sesDetailTypeTag);
        title.setText(isCharge ? "Detail Pengisian" : "Detail Pengosongan");
        title.setTextColor(accent);
        typeTag.setText(isCharge ? "MENGISI" : "MEMAKAI");
        typeTag.setTextColor(accent);
        typeTag.setBackgroundResource(isCharge
                ? R.drawable.bat_badge_active_bg : R.drawable.bat_badge_stopped_bg);

        TextView subtitle = findViewById(R.id.sesDetailSubtitle);
        subtitle.setText(FMT.format(new Date(start)) + " – " + FMT.format(new Date(end)));

        setText(R.id.sesStartTime, FMT.format(new Date(start)));
        setText(R.id.sesEndTime, FMT.format(new Date(end)));
        setText(R.id.sesDuration, formatDuration(end - start));

        String pct = startPct >= 0 && endPct >= 0
                ? startPct + "% → " + endPct + "%"
                : pctNone();
        setText(R.id.sesLevel, pct);

        TextView energyTitle = findViewById(R.id.sesEnergyTitle);
        energyTitle.setText(isCharge ? "Energi Masuk" : "Energi Terpakai");
        energyTitle.setTextColor(accent);

        setText(R.id.sesMahIntegral, mahLabel(mahIntegral));
        setText(R.id.sesMahCounter, mahLabel(mahCounter));
        setText(R.id.sesDeltaMah, mahLabel(isCharge ? deltaMah
                : (mahIntegral > 0 ? mahIntegral : mahCounter)));
        setText(R.id.sesEfficiency, efficiency >= 0
                ? String.format(Locale.US, "%.0f%%", efficiency) : "—");

        setText(R.id.sesTempMin, tempLabel(tempMin));
        setText(R.id.sesTempMax, tempLabel(tempMax));
        setText(R.id.sesTempAvg, tempLabel(tempAvg));
        setText(R.id.sesCapacity, capacity > 0
                ? String.format(Locale.US, "%.0f mAh", capacity) : "—");
        setText(R.id.sesScreen, screenOff ? "Layar mati dominan" : "Layar menyala");
        setText(R.id.sesSamples, samples > 0 ? String.valueOf(samples) : "—");

        findViewById(R.id.sesDetailBack).setOnClickListener(v -> finish());
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

    private void setText(int id, String text) {
        TextView tv = findViewById(id);
        if (tv != null) tv.setText(text);
    }
}