package exp.ftxt.ui;

import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import exp.ftxt.MainActivity;
import exp.ftxt.R;
import exp.ftxt.features.battery_stats.BatteryCapacityEstimator;
import exp.ftxt.features.battery_stats.BatteryHistoryDb;
import exp.ftxt.features.battery_stats.BatteryReading;
import exp.ftxt.features.battery_stats.SessionSegmentBuilder;

/**
 * Panel "Sesi Berjalan": menampilkan satu sesi aktif real-time yang
 * direkonstruksi dari tabel samples mentah (sumber kebenaran). Arah & batas
 * sesi ditentukan oleh pergerakan persen (jendela 10 menit, §7) — bukan status
 * Android. Tata letak menyusul pola AccuBattery: ring besar, estimasi bertahan
 * per kondisi layar, kartu status/penggunaan/kecepatan/rincian dengan progress bar.
 */
public class BatterySessionLiveController {

    private static final long REFRESH_INTERVAL_MS = 1_000L;
    private static final long SAMPLE_WINDOW_MS = 12L * 3600_000L;

    private final MainActivity activity;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private TextView badge;
    private TextView statusText;
    private TextView statusSub;
    private TextView statusDetail;
    private exp.ftxt.features.battery_stats.BatteryRingView ring;
    private View estSection;
    private TextView estOn;
    private TextView estOff;
    private TextView estBoth;
    private LinearLayout content;
    private int monitorLabelColor;

    private boolean running = false;
    private boolean queryInFlight = false;
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (!running) return;
            refresh();
            uiHandler.postDelayed(this, REFRESH_INTERVAL_MS);
        }
    };

    public BatterySessionLiveController(MainActivity activity, View pageView) {
        this.activity = activity;
        bindViews(pageView);
    }

    private void bindViews(View rootView) {
        badge = rootView.findViewById(R.id.batLiveBadge);
        statusText = rootView.findViewById(R.id.batLiveStatusText);
        statusSub = rootView.findViewById(R.id.batLiveStatusSub);
        statusDetail = rootView.findViewById(R.id.batLiveStatusDetail);
        ring = rootView.findViewById(R.id.batLiveRing);
        estSection = rootView.findViewById(R.id.batLiveEstSection);
        estOn = rootView.findViewById(R.id.batLiveEstOn);
        estOff = rootView.findViewById(R.id.batLiveEstOff);
        estBoth = rootView.findViewById(R.id.batLiveEstBoth);
        content = rootView.findViewById(R.id.batLiveContent);
        monitorLabelColor = activity.getColor(R.color.bat_monitor_label);
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
        if (content == null || queryInFlight) return;
        queryInFlight = true;
        final long now = System.currentTimeMillis();
        executor.execute(() -> {
            final BatteryHistoryDb db = BatteryHistoryDb.get(activity);
            final BatteryReading.Snapshot[] samples;
            try {
                samples = db.querySamples(now - SAMPLE_WINDOW_MS);
            } catch (Exception e) {
                uiHandler.post(() -> { queryInFlight = false; renderEmpty(); });
                return;
            }
            if (samples == null || samples.length == 0) {
                uiHandler.post(() -> { queryInFlight = false; renderEmpty(); });
                return;
            }
            final LiveData data = compute(db, samples, now);
            uiHandler.post(() -> { queryInFlight = false; render(data); });
        });
    }

    // ---------- data ----------

    private static final class LiveData {
        boolean charging;
        int percent;
        int startPercent;
        int deltaPct;
        long startTime;
        long durMs;
        double totalMah;
        double screenOnMah;
        double screenOffMah;
        long screenOnMs;
        long screenOffMs;
        float tempMin;
        float tempMax;
        float tempAvg;
        int currentMa;
        double powerW;
        float voltageV;
        int cycleCount;
        int pluggedInt;
        float ratePctPerHour;
        long remainingMs;
        long estOnMs = -1;
        long estOffMs = -1;
        long estBothMs = -1;
    }

    private LiveData compute(BatteryHistoryDb db, BatteryReading.Snapshot[] asc, long now) {
        BatteryReading.Snapshot[] desc = SessionSegmentBuilder.toDesc(asc);
        BatteryReading.Snapshot last = desc[0];
        SessionSegmentBuilder.ScreenOnOracle oracle =
                db.screenOnOracle(now - SAMPLE_WINDOW_MS, now);

        SessionSegmentBuilder.LivingSession living = SessionSegmentBuilder.livingSession(desc);
        SessionSegmentBuilder.Direction dir = living.direction;
        boolean charging = dir == SessionSegmentBuilder.Direction.CHARGE;
        if (dir == SessionSegmentBuilder.Direction.FLAT) {
            charging = last.isCharging();
        }
        int startIdx = living.truncated ? desc.length - 1 : living.startIndex;
        if (dir == SessionSegmentBuilder.Direction.FLAT) startIdx = 0;

        BatteryReading.Snapshot start = desc[startIdx];
        LiveData d = new LiveData();
        d.charging = charging;
        d.percent = last.percent;
        d.startPercent = start.percent;
        d.deltaPct = charging
                ? last.percent - start.percent
                : start.percent - last.percent;
        d.deltaPct = Math.max(0, d.deltaPct);
        d.startTime = start.time;
        d.durMs = Math.max(0L, last.time - start.time);

        double tSum = 0;
        int tCount = 0;
        float tMin = Float.MAX_VALUE;
        float tMax = Float.MIN_VALUE;
        double powerSum = 0;
        int powerN = 0;
        for (int k = startIdx; k >= 0; k--) {
            BatteryReading.Snapshot s = desc[k];
            if (s.tempC > 0f) {
                tSum += s.tempC;
                tCount++;
                if (s.tempC < tMin) tMin = s.tempC;
                if (s.tempC > tMax) tMax = s.tempC;
            }
            if (k > 0) {
                long dt = desc[k - 1].time - desc[k].time;
                if (dt > 0 && dt < 5000) {
                    double mah = Math.abs(desc[k].currentMa) * (dt / 3600000.0);
                    d.totalMah += mah;
                    if (oracle.isScreenOn(desc[k].time)) {
                        d.screenOnMah += mah;
                        d.screenOnMs += dt;
                    } else {
                        d.screenOffMah += mah;
                        d.screenOffMs += dt;
                    }
                }
            }
            if (s.powerW > 0d) {
                powerSum += s.powerW;
                powerN++;
            }
        }
        d.tempMin = tMin == Float.MAX_VALUE ? 0f : tMin;
        d.tempMax = tMax == Float.MIN_VALUE ? 0f : tMax;
        d.tempAvg = tCount > 0 ? (float) (tSum / tCount) : 0f;
        d.currentMa = last.currentMa;
        d.powerW = powerN > 0 ? powerSum / powerN : last.powerW;
        d.voltageV = last.voltageV;
        d.cycleCount = last.cycleCount;
        d.pluggedInt = last.pluggedInt;

        float hours = (float) (d.durMs / 3600000.0);
        d.ratePctPerHour = hours > 0.01f ? d.deltaPct / hours : 0f;

        long remaining = db.estimateTimeRemaining(charging);
        d.remainingMs = remaining;

        BatteryCapacityEstimator.HealthResult health = BatteryCapacityEstimator.getResult();
        float capacityMah = health.medianMah > 0f ? health.medianMah : health.designMah;

        if (!charging && capacityMah > 0f && d.totalMah > 0d && d.durMs > 0L) {
            double remainingMah = (d.percent / 100f) * capacityMah;
            double rateAllPerMs = d.totalMah / d.durMs;
            d.estBothMs = (long) (remainingMah / rateAllPerMs);
            if (d.screenOnMah > 0d && d.screenOnMs > 0L) {
                d.estOnMs = (long) (remainingMah / (d.screenOnMah / d.screenOnMs));
            }
            if (d.screenOffMah > 0d && d.screenOffMs > 0L) {
                d.estOffMs = (long) (remainingMah / (d.screenOffMah / d.screenOffMs));
            }
        }
        return d;
    }

    // ---------- render ----------

    private void render(LiveData d) {
        if (content == null) return;
        int accent = activity.getColor(d.charging
                ? R.color.bat_monitor_header : R.color.bat_monitor_stop);

        statusText.setText(d.charging ? "PENGISIAN" : "PENGOSONGAN");
        statusText.setTextColor(accent);
        badge.setText("● MONITORING");

        statusSub.setText(d.charging
                ? String.format(Locale.US, "Mengisi · %d%% selesai", d.deltaPct)
                : String.format(Locale.US, "Menguras · %d%% habis", d.deltaPct));
        statusSub.setTextColor(accent);

        StringBuilder detail = new StringBuilder();
        detail.append("Mulai ").append(fmtTime(d.startTime))
                .append(" · Durasi ").append(fmtDur(d.durMs));
        detail.append('\n').append(String.format(Locale.US, "Kecepatan %+.1f %%/jam",
                d.ratePctPerHour));
        if (d.currentMa != 0) {
            detail.append(String.format(Locale.US, " · Arus %+d mA", d.currentMa));
        }
        statusDetail.setText(detail);

        ring.setSessionData(d.percent, d.startPercent, d.charging,
                fmtMah(d.totalMah), d.charging ? "Mengisi" : "Menguras");

        boolean showEst = !d.charging && d.durMs > 30_000L;
        estSection.setVisibility(showEst ? View.VISIBLE : View.GONE);
        if (showEst) {
            estOn.setText(fmtEst(d.estOnMs));
            estOff.setText(fmtEst(d.estOffMs));
            estBoth.setText(fmtEst(d.estBothMs));
        }

        content.removeAllViews();
        if (d.charging) renderChargeCards(d, accent);
        else renderDischargeCards(d, accent);
    }

    private void renderEmpty() {
        if (content == null) return;
        statusText.setText("SESI BERJALAN");
        statusText.setTextColor(activity.getColor(R.color.bat_monitor_header));
        badge.setText("● MONITORING");
        statusSub.setText("—");
        statusDetail.setText("Tidak ada data sampel tersedia.");
        ring.setSessionData(0, 0, true, "—", "—");
        estSection.setVisibility(View.GONE);
        content.removeAllViews();
    }

    // ---------- kartu discharge ----------

    private void renderDischargeCards(LiveData d, int accent) {
        LinearLayout statusCard = card();
        addHeader(statusCard, "Status pelepasan", accent);
        addKv(statusCard, "Masa pakai",
                String.format(Locale.US, "%.1f W · %d%%", d.powerW, d.percent));
        addKv(statusCard, "Penggunaan rata-rata",
                String.format(Locale.US, "%.1f %%/jam", d.ratePctPerHour));
        addKv(statusCard, "Temperatur", fmtTemp(d));
        addKv(statusCard, "Tegangan", String.format(Locale.US, "%.2f V", d.voltageV));
        content.addView(statusCard);

        LinearLayout usedCard = card();
        addHeader(usedCard, "Penggunaan baterai", accent);
        content.addView(usedCard);
        LinearLayout row1 = twoColRow(usedCard);
        addValueBlock(row1, "Total", String.format(Locale.US, "%.1f%%", (float) d.deltaPct),
                accent, false);
        addValueBlock(row1, "Terpakai sesi ini",
                String.format(Locale.US, "-%d%% / %s", d.deltaPct, fmtMah(d.totalMah)),
                accent, false);
        LinearLayout row2 = twoColRow(usedCard);
        double sharesOn = d.totalMah > 0d ? d.screenOnMah / d.totalMah : 0d;
        double sharesOff = d.totalMah > 0d ? d.screenOffMah / d.totalMah : 0d;
        addValueBlock(row2, "Layar hidup",
                String.format(Locale.US, "%.1f%% / %s", sharesOn * 100d, fmtMah(d.screenOnMah)),
                activity.getColor(R.color.bat_chart_power), false);
        addValueBlock(row2, "Layar mati",
                String.format(Locale.US, "%.1f%% / %s", sharesOff * 100d, fmtMah(d.screenOffMah)),
                activity.getColor(R.color.bat_chart_current), false);
        addKv(usedCard, "Kecepatan",
                String.format(Locale.US, "%.1f %%/jam", d.ratePctPerHour));
        addKv(usedCard, "Arus", d.currentMa != 0
                ? String.format(Locale.US, "%+d mA", d.currentMa) : "—");

        content.addView(detailCard(d, accent, true));
    }

    // ---------- kartu charge ----------

    private void renderChargeCards(LiveData d, int accent) {
        LinearLayout statusCard = card();
        addHeader(statusCard, "Status pengisian", accent);
        content.addView(statusCard);
        addMeter(statusCard, "Arus pengisian",
                clamp((float) d.powerW / 22f),
                String.format(Locale.US, "%.1f W / %d mA", d.powerW, d.currentMa),
                activity.getColor(R.color.bat_chart_power));
        addMeter(statusCard, "Tegangan",
                (float) d.voltageV / 5f,
                String.format(Locale.US, "%.0f mV", d.voltageV * 1000f),
                activity.getColor(R.color.bat_chart_voltage));
        addMeter(statusCard, "Kecepatan rata-rata",
                Math.min(1f, d.ratePctPerHour / 60f),
                String.format(Locale.US, "%+.1f %%/jam", d.ratePctPerHour),
                activity.getColor(R.color.bat_chart_percent));
        addMeter(statusCard, "Temperatur",
                tempRatio(d.tempAvg),
                fmtTemp(d),
                activity.getColor(R.color.bat_chart_temp));

        LinearLayout etaCard = card();
        addHeader(etaCard, "Perkiraan waktu pengisian", accent);
        content.addView(etaCard);
        int remainingPct = Math.max(0, 100 - d.percent);
        addMeter(etaCard, "Sisa baterai",
                d.percent / 100f,
                String.format(Locale.US, "%d%% tersisa", remainingPct),
                activity.getColor(R.color.bat_monitor_active));
        addMeter(etaCard, "Waktu hingga 100%",
                d.remainingMs >= 0 ? 1f : 0f,
                d.remainingMs >= 0 ? fmtEst(d.remainingMs) : "—",
                accent);

        LinearLayout gainedCard = card();
        addHeader(gainedCard, "Telah terisi", accent);
        content.addView(gainedCard);
        LinearLayout gainedRow = twoColRow(gainedCard);
        addValueBlock(gainedRow, "Total",
                String.format(Locale.US, "+%d%%", d.deltaPct),
                accent, false);
        addValueBlock(gainedRow, "Sedang diisi", fmtMah(d.totalMah), accent, false);

        LinearLayout speedCard = card();
        addHeader(speedCard, "Kecepatan pengisian", accent);
        content.addView(speedCard);
        LinearLayout speedRow = twoColRow(speedCard);
        addValueBlock(speedRow, "Rata-rata",
                String.format(Locale.US, "%+.1f %%/jam", d.ratePctPerHour),
                accent, false);
        addValueBlock(speedRow, "Sekarang",
                d.currentMa != 0 ? String.format(Locale.US, "%d mA", d.currentMa) : "—",
                activity.getColor(R.color.bat_chart_power), false);

        content.addView(detailCard(d, accent, false));
    }

    // ---------- detail ----------

    private LinearLayout detailCard(LiveData d, int accent, boolean discharge) {
        LinearLayout ll = card();
        addHeader(ll, "Rincian sesi berjalan", accent);
        LinearLayout row = twoColRow(ll);
        addValueBlock(row, "Mulai", fmtTime(d.startTime), accent, false);
        String trend = discharge ? "turun" : "naik";
        addValueBlock(row, "Durasi",
                fmtDur(d.durMs) + " (" + trend + ")", accent, false);

        if (d.powerW > 0d) {
            addMeter(ll, "Daya / Tegangan",
                    clamp((float) (d.powerW / 10f)),
                    String.format(Locale.US, "%.1f W / %.2f V", d.powerW, d.voltageV),
                    activity.getColor(R.color.bat_chart_power));
        }
        float rateAbs = Math.abs(d.ratePctPerHour);
        addMeter(ll, "Kecepatan",
                rateAbs > 0f ? Math.min(1f, rateAbs / 30f) : 0f,
                String.format(Locale.US, "%+.1f %%/jam", d.ratePctPerHour),
                activity.getColor(R.color.bat_chart_current));
        if (d.tempAvg > 0f) {
            addMeter(ll, "Suhu", tempRatio(d.tempAvg), fmtTemp(d),
                    activity.getColor(R.color.bat_chart_temp));
        }
        addKv(ll, "Siklus", d.cycleCount > 0 ? String.valueOf(d.cycleCount) : "—");
        addKv(ll, "Colokan", pluggedText(d) + " · Layar " + screenNowText());
        return ll;
    }

    private String screenNowText() {
        try {
            PowerManager pm = activity.getSystemService(PowerManager.class);
            return pm != null && pm.isInteractive() ? "Nyala" : "Mati";
        } catch (Exception ignored) {
            return "—";
        }
    }

    // ---------- builder UI ----------

    private LinearLayout card() {
        LinearLayout ll = new LinearLayout(activity);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setBackgroundResource(R.drawable.bat_card_bg);
        ll.setPadding(dp(12), dp(12), dp(12), dp(12));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(10);
        ll.setLayoutParams(lp);
        return ll;
    }

    private void addHeader(LinearLayout card, String title, int accent) {
        LinearLayout head = new LinearLayout(activity);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams headLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        headLp.bottomMargin = dp(8);
        head.setLayoutParams(headLp);

        View bar = new View(activity);
        bar.setBackgroundColor(accent);
        bar.setLayoutParams(new LinearLayout.LayoutParams(dp(3), dp(14)));
        head.addView(bar);

        TextView tv = new TextView(activity);
        tv.setText(title);
        tv.setTextSize(13);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setTextColor(accent);
        LinearLayout.LayoutParams tvLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        tvLp.setMarginStart(dp(8));
        tv.setLayoutParams(tvLp);
        head.addView(tv);
        card.addView(head);
    }

    private void addKv(LinearLayout card, String label, String value) {
        addKv(card, label, value, activity.getColor(R.color.bat_monitor_header));
    }

    private void addKv(LinearLayout card, String label, String value, int valueColor) {
        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        rowLp.topMargin = dp(4);
        row.setLayoutParams(rowLp);

        TextView l = new TextView(activity);
        l.setText(label);
        l.setTextSize(12);
        l.setTextColor(monitorLabelColor);
        l.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(l);

        TextView v = new TextView(activity);
        v.setText(value);
        v.setTextSize(12);
        v.setTypeface(Typeface.DEFAULT_BOLD);
        v.setTextColor(valueColor);
        v.setGravity(Gravity.END);
        row.addView(v);
        card.addView(row);
    }

    private void addMeter(LinearLayout card, String label, float ratio, String value, int color) {
        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        rowLp.topMargin = dp(4);
        row.setLayoutParams(rowLp);

        TextView l = new TextView(activity);
        l.setText(label);
        l.setTextSize(12);
        l.setTextColor(monitorLabelColor);
        l.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(l);

        TextView v = new TextView(activity);
        v.setText(value);
        v.setTextSize(12);
        v.setTypeface(Typeface.DEFAULT_BOLD);
        v.setTextColor(color);
        v.setGravity(Gravity.END);
        row.addView(v);
        card.addView(row);

        LinearLayout track = new LinearLayout(activity);
        track.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams trackLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(4));
        trackLp.topMargin = dp(3);
        track.setBackgroundResource(R.drawable.bat_badge_stopped_bg);
        track.setLayoutParams(trackLp);

        View fill = new View(activity);
        int pct = (int) Math.round(clamp(ratio) * 100f);
        fill.setBackgroundColor(color);
        fill.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.MATCH_PARENT, pct == 0 ? 0f : pct));
        if (pct == 100) {
            fill.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
        }
        track.addView(fill);
        if (pct < 100) {
            View rest = new View(activity);
            rest.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.MATCH_PARENT, 100f - pct));
            track.addView(rest);
        }
        card.addView(track);
    }

    private LinearLayout twoColRow(LinearLayout card) {
        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(4);
        row.setLayoutParams(lp);
        card.addView(row);
        return row;
    }

    private void addValueBlock(LinearLayout row, String label, String value,
                               int valueColor, boolean last) {
        LinearLayout col = new LinearLayout(activity);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView l = new TextView(activity);
        l.setText(label);
        l.setTextSize(11);
        l.setTextColor(monitorLabelColor);
        col.addView(l);

        TextView v = new TextView(activity);
        v.setText(value);
        v.setTextSize(14);
        v.setTypeface(Typeface.DEFAULT_BOLD);
        v.setTextColor(valueColor);
        col.addView(v);
        row.addView(col);
    }

    // ---------- bantuan format ----------

    private float clamp(float v) {
        return v < 0f ? 0f : Math.min(1f, v);
    }

    private float tempRatio(float avg) {
        if (avg <= 0f) return 0f;
        return clamp((avg - 10f) / 35f);
    }

    private String fmtTemp(LiveData d) {
        if (d.tempAvg <= 0f) return "—";
        return String.format(Locale.US, "%.0f–%.0f°C (%.1f)",
                d.tempMin, d.tempMax, d.tempAvg);
    }

    private String fmtMah(double v) {
        return String.format(Locale.US, "%.0f mAh", v);
    }

    private String fmtEst(long ms) {
        if (ms < 0) return "—";
        long totalMin = ms / 60_000L;
        if (totalMin < 1) return "<1m";
        long h = totalMin / 60;
        long m = totalMin % 60;
        if (h == 0) return m + "m";
        return h + "j " + m + "m";
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

    private String pluggedText(LiveData d) {
        if (d.pluggedInt <= 0) return "Tidak";
        switch (d.pluggedInt) {
            case 1: return "USB";
            case 2: return "AC";
            case 4: return "Wireless";
            default: return String.valueOf(d.pluggedInt);
        }
    }

    private int dp(float value) {
        return (int) (value * activity.getResources().getDisplayMetrics().density);
    }
}