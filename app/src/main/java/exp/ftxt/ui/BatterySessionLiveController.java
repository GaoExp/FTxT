package exp.ftxt.ui;

import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import exp.ftxt.MainActivity;
import exp.ftxt.R;
import exp.ftxt.features.battery_stats.BatteryHistoryDb;
import exp.ftxt.features.battery_stats.BatteryReading;

/**
 * Panel "Sesi Berjalan": menampilkan satu sesi aktif real-time (pengosongan
 * atau pengisian) yang direkonstruksi langsung dari tabel samples mentah,
 * sehingga tahan terhadap proses yang dibunuh. Interval diperbarui bisa
 * dipilih (1s / 2s / 5s).
 */
public class BatterySessionLiveController {

    private static final long[] INTERVALS_MS = {1_000L, 2_000L, 5_000L};

    private final MainActivity activity;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private TextView[] intervalTabs;
    private TextView badge;
    private TextView statusText;
    private TextView percent;
    private TextView range;
    private TextView body;
    private int monitorLabelColor;

    private int intervalIndex = 0;
    private boolean running = false;
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (!running) return;
            refresh();
            uiHandler.postDelayed(this, INTERVALS_MS[intervalIndex]);
        }
    };

    public BatterySessionLiveController(MainActivity activity, View rootView) {
        this.activity = activity;
        bindViews(rootView);
        bindIntervalTabs();
        updateIntervalState();
    }

    private void bindViews(View rootView) {
        intervalTabs = new TextView[]{
                rootView.findViewById(R.id.batLiveInterval1),
                rootView.findViewById(R.id.batLiveInterval2),
                rootView.findViewById(R.id.batLiveInterval5)};
        badge = rootView.findViewById(R.id.batLiveBadge);
        statusText = rootView.findViewById(R.id.batLiveStatusText);
        percent = rootView.findViewById(R.id.batLivePercent);
        range = rootView.findViewById(R.id.batLiveRange);
        body = rootView.findViewById(R.id.batLiveBody);
        monitorLabelColor = activity.getColor(R.color.bat_monitor_label);
    }

    private void bindIntervalTabs() {
        for (int i = 0; i < intervalTabs.length; i++) {
            final int idx = i;
            intervalTabs[i].setOnClickListener(v -> {
                intervalIndex = idx;
                updateIntervalState();
            });
        }
    }

    private void updateIntervalState() {
        for (int i = 0; i < intervalTabs.length; i++) {
            boolean active = i == intervalIndex;
            intervalTabs[i].setTextColor(activity.getColor(active
                    ? R.color.bat_monitor_header : R.color.bat_monitor_label));
            intervalTabs[i].setTypeface(active ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        }
    }

    public void onPanelShown() {
        if (running) return;
        running = true;
        uiHandler.post(refreshRunnable);
    }

    public void onPanelHidden() {
        running = false;
        uiHandler.removeCallbacks(refreshRunnable);
    }

    public void cleanup() {
        running = false;
        uiHandler.removeCallbacks(refreshRunnable);
        executor.shutdownNow();
    }

    public void refreshNow() {
        refresh();
    }

    private void refresh() {
        if (body == null) return;
        final long now = System.currentTimeMillis();
        executor.execute(() -> {
            final BatteryHistoryDb db = BatteryHistoryDb.get(activity);
            final BatteryReading.Snapshot[] samples;
            try {
                samples = db.queryLastSamples(4096);
            } catch (Exception e) {
                uiHandler.post(() -> renderEmpty());
                return;
            }
            if (samples == null || samples.length == 0) {
                uiHandler.post(() -> renderEmpty());
                return;
            }
            final LiveData data = compute(db, samples, now);
            uiHandler.post(() -> render(data));
        });
    }

    private static final class LiveData {
        boolean charging;
        String text;
        int percent;
        int deltaPct;
    }

    private LiveData compute(BatteryHistoryDb db, BatteryReading.Snapshot[] samples, long now) {
        BatteryReading.Snapshot last = samples[0];

        boolean charging = last.isCharging();
        boolean wantCharging = charging;
        int endIdx = 0;
        for (int i = 0; i < samples.length; i++) {
            if (samples[i].isCharging() != wantCharging) break;
            endIdx = i;
        }
        BatteryReading.Snapshot first = samples[endIdx];
        long durMs = Math.max(0L, last.time - first.time);

        double integral = 0.0;
        double tempSum = 0;
        int tempN = 0;
        float tempMin = Float.MAX_VALUE;
        float tempMax = Float.MIN_VALUE;
        for (int i = 0; i <= endIdx; i++) {
            BatteryReading.Snapshot s = samples[i];
            if (s.tempC > 0f) {
                tempSum += s.tempC;
                tempN++;
                if (s.tempC < tempMin) tempMin = s.tempC;
                if (s.tempC > tempMax) tempMax = s.tempC;
            }
        }
        for (int i = 1; i <= endIdx; i++) {
            long dt = samples[i - 1].time - samples[i].time;
            if (dt > 0 && dt < 5000) {
                integral += Math.abs(samples[i - 1].currentMa) * (dt / 3600000.0);
            }
        }

        int deltaPct = charging
                ? last.percent - first.percent
                : first.percent - last.percent;
        float hours = (float) (durMs / 3600000.0);
        float ratePctPerHour = hours > 0.01f ? deltaPct / hours : 0f;
        int currentMa = last.currentMa;
        double powerW = last.powerW;
        float vol = last.voltageV;
        int cycle = last.cycleCount;
        float tempAvg = tempN > 0 ? (float) (tempSum / tempN) : 0f;
        long remaining = db.estimateTimeRemaining(charging);

        StringBuilder sb = new StringBuilder();
        appendKv(sb, "Jenis sesi", charging ? "Pengisian" : "Pengosongan");
        appendKv(sb, "Mulai", fmtTime(first.time));
        appendKv(sb, "Durasi", fmtDur(durMs));
        appendKv(sb, "Baterai", first.percent + "%  \u2192  " + last.percent + "%  ("
                + (charging ? "+" : "") + deltaPct + "%)");
        appendKv(sb, "Arus", currentMa + " mA");
        appendKv(sb, (charging ? "Terisi" : "Terpakai") + " (mAh)", fmtMah(integral));
        appendKv(sb, "Kecepatan", String.format(Locale.US, "%+.1f %%/jam", ratePctPerHour));
        if (remaining >= 0) appendKv(sb, "Sisa waktu", fmtDur(remaining));
        appendKv(sb, "Daya", String.format(Locale.US, "%.1f W", powerW));
        appendKv(sb, "Voltase", String.format(Locale.US, "%.2f V", vol));
        appendKv(sb, "Suhu", tempN > 0
                ? String.format(Locale.US, "%.0f\u2013%.0f\u00b0C (%.1f)", tempMin, tempMax, tempAvg)
                : "\u2014");
        appendKv(sb, "Colokan", pluggedText(last));
        appendKv(sb, "Siklus", cycle > 0 ? String.valueOf(cycle) : "\u2014");

        LiveData d = new LiveData();
        d.charging = charging;
        d.text = sb.toString();
        d.percent = last.percent;
        d.deltaPct = deltaPct;
        return d;
    }

    private void render(LiveData d) {
        if (body == null) return;
        badge.setText(d.charging ? "PENGISIAN" : "PENGOSONGAN");
        badge.setTextColor(activity.getColor(d.charging
                ? R.color.bat_monitor_header : R.color.bat_monitor_header));
        statusText.setText(d.charging ? "Mengisi" : "Menguras");
        statusText.setTextColor(activity.getColor(d.charging
                ? R.color.bat_monitor_header : R.color.bat_monitor_stop));
        percent.setText(d.percent + "%");
        range.setText((d.charging ? "+" : "") + d.deltaPct + "%");
        body.setText(d.text);
    }

    private void renderEmpty() {
        if (body == null) return;
        badge.setText("SESI BERJALAN");
        statusText.setText("—");
        percent.setText("--%");
        range.setText("");
        body.setText("Tidak ada sesi berjalan. Data sampel belum tersedia.");
    }

    private void appendKv(StringBuilder sb, String label, String value) {
        sb.append(pad(label, 14)).append(value).append('\n');
    }

    private String pad(String s, int width) {
        StringBuilder b = new StringBuilder(s);
        while (b.length() < width) b.append(' ');
        return b.toString();
    }

    private String fmtMah(double v) {
        return String.format(Locale.US, "%.0f mAh", v);
    }

    private String fmtDur(long ms) {
        long sec = ms / 1000;
        long h = sec / 3600;
        long m = (sec % 3600) / 60;
        long s = sec % 60;
        if (h > 0) return h + "j " + m + "m " + s + "s";
        if (m > 0) return m + "m " + s + "s";
        return s + "s";
    }

    private String fmtTime(long ms) {
        return new SimpleDateFormat("dd/MM HH:mm:ss", Locale.getDefault())
                .format(new Date(ms));
    }

    private String pluggedText(BatteryReading.Snapshot s) {
        if (s.pluggedInt <= 0) return "Tidak";
        switch (s.pluggedInt) {
            case 1: return "USB";
            case 2: return "AC";
            case 4: return "Wireless";
            default: return String.valueOf(s.pluggedInt);
        }
    }
}
