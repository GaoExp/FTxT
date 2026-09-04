package exp.ftxt.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import exp.ftxt.R;
import exp.ftxt.features.battery_stats.BatteryHistoryDb;

/**
 * Halaman daftar sesi (pengisian/pengosongan) untuk satu batang periode
 * pada grafik riwayat. Dibuka dari filter button di sub-tab
 * "Kondisi & Riwayat". Pola ini mengikuti fragment replacement
 * ala AccuBucket — daftar sesi berada di layar terpisah, bukan
 * di-append inline ke panel yang sama.
 */
public class SessionListActivity extends AppCompatActivity {

    private static final String E_BUCKET_START = "bucketStartMs";
    private static final String E_BUCKET_END = "bucketEndMs";
    private static final String E_FILTER = "filter";

    public static final int FILTER_ALL = 0;
    public static final int FILTER_CHARGE = 1;
    public static final int FILTER_DISCHARGE = 2;

    private static final SimpleDateFormat FMT =
            new SimpleDateFormat("dd/MM HH:mm", Locale.US);

    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private LinearLayout container;
    private ProgressBar loading;
    private ScrollView scroll;

    public static void start(Context context, long bucketStartMs, long bucketEndMs, int filter) {
        Intent intent = new Intent(context, SessionListActivity.class);
        intent.putExtra(E_BUCKET_START, bucketStartMs);
        intent.putExtra(E_BUCKET_END, bucketEndMs);
        intent.putExtra(E_FILTER, filter);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_list);

        long bucketStart = getIntent().getLongExtra(E_BUCKET_START, 0L);
        long bucketEnd = getIntent().getLongExtra(E_BUCKET_END, 0L);
        int filter = getIntent().getIntExtra(E_FILTER, FILTER_ALL);

        container = findViewById(R.id.sesListContainer);
        loading = findViewById(R.id.sesListLoading);
        scroll = findViewById(R.id.sesListScroll);

        setupHeader(filter);
        loadSessions(bucketStart, bucketEnd, filter);
    }

    private void setupHeader(int filter) {
        TextView back = findViewById(R.id.sesListBack);
        back.setOnClickListener(v -> finish());

        TextView title = findViewById(R.id.sesListTitle);
        TextView badge = findViewById(R.id.sesListBadge);

        String titleText;
        String badgeText;
        int badgeBg;

        switch (filter) {
            case FILTER_CHARGE:
                titleText = "Sesi Pengisian";
                badgeText = "PENGISIAN";
                badgeBg = R.drawable.bat_badge_active_bg;
                break;
            case FILTER_DISCHARGE:
                titleText = "Sesi Pengosongan";
                badgeText = "PENGOSONGAN";
                badgeBg = R.drawable.bat_badge_stopped_bg;
                break;
            default:
                titleText = "Semua Sesi";
                badgeText = "SEMUA";
                badgeBg = R.drawable.bat_badge_active_bg;
                break;
        }

        title.setText(titleText);
        badge.setText(badgeText);
        badge.setBackgroundResource(badgeBg);
    }

    private void loadSessions(long from, long to, int filter) {
        loading.setVisibility(View.VISIBLE);
        scroll.setVisibility(View.GONE);

        executor.execute(() -> {
            final ArrayList<BatteryHistoryDb.SessionEntry> all =
                    BatteryHistoryDb.get(this).querySessionEntries(from, to);

            final ArrayList<BatteryHistoryDb.SessionEntry> filtered = new ArrayList<>();
            for (BatteryHistoryDb.SessionEntry e : all) {
                if (filter == FILTER_ALL) {
                    filtered.add(e);
                } else if (filter == FILTER_CHARGE && e.isCharge) {
                    filtered.add(e);
                } else if (filter == FILTER_DISCHARGE && !e.isCharge) {
                    filtered.add(e);
                }
            }

            uiHandler.post(() -> {
                loading.setVisibility(View.GONE);
                scroll.setVisibility(View.VISIBLE);
                renderSessions(filtered);
            });
        });
    }

    private void renderSessions(ArrayList<BatteryHistoryDb.SessionEntry> entries) {
        container.removeAllViews();

        if (entries.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Tidak ada sesi");
            empty.setTextSize(12);
            empty.setTextColor(getColor(R.color.bat_monitor_label));
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, dp(24), 0, dp(24));
            container.addView(empty);
            return;
        }

        for (BatteryHistoryDb.SessionEntry e : entries) {
            container.addView(createRow(e));
        }
    }

    private View createRow(final BatteryHistoryDb.SessionEntry e) {
        View view = LayoutInflater.from(this)
                .inflate(R.layout.item_session_row, container, false);
        view.setOnClickListener(v -> SessionDetailActivity.start(this, e));

        int accent = getColor(e.isCharge
                ? R.color.bat_monitor_active : R.color.bat_chart_power);
        int headerColor = getColor(R.color.bat_monitor_header);

        View accentBar = view.findViewById(R.id.itemSessionAccent);
        accentBar.setBackgroundColor(accent);

        TextView typeTag = view.findViewById(R.id.itemSessionTypeTag);
        typeTag.setText(e.isCharge ? "PENGISIAN" : "PENGOSONGAN");
        typeTag.setTextColor(accent);
        typeTag.setBackgroundResource(e.isCharge
                ? R.drawable.bat_badge_active_bg : R.drawable.bat_badge_stopped_bg);

        TextView timeText = view.findViewById(R.id.itemSessionTime);
        timeText.setText(FMT.format(new Date(e.startTime))
                + " – " + FMT.format(new Date(e.endTime)));

        TextView levelText = view.findViewById(R.id.itemSessionLevel);
        levelText.setText(String.format(Locale.US, "%d%% → %d%%",
                e.startPercent, e.endPercent));
        levelText.setTextColor(headerColor);

        TextView durationText = view.findViewById(R.id.itemSessionDuration);
        durationText.setText(formatDuration(e.durationMs()));

        TextView mahText = view.findViewById(R.id.itemSessionMah);
        if (e.mAhCounter > 0 || e.mAhIntegral > 0) {
            mahText.setText(String.format(Locale.US, "C %.0f · I %.0f mAh",
                    e.mAhCounter, e.mAhIntegral));
        } else {
            mahText.setVisibility(View.GONE);
        }

        TextView tempText = view.findViewById(R.id.itemSessionTemp);
        if (e.tempMax > 0) {
            tempText.setText(String.format(Locale.US, "%.1f–%.1f°C",
                    e.tempMin, e.tempMax));
        } else {
            tempText.setVisibility(View.GONE);
        }

        TextView effText = view.findViewById(R.id.itemSessionEff);
        if (!e.isCharge && e.efficiencyPercent >= 0) {
            effText.setText(String.format(Locale.US, "%.0f%% efisiensi",
                    e.efficiencyPercent));
        } else {
            effText.setVisibility(View.GONE);
        }

        return view;
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

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        uiHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
