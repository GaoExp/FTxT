package exp.ftxt.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import exp.ftxt.MainActivity;
import exp.ftxt.features.battery_stats.BatteryCapacityEstimator;
import exp.ftxt.features.battery_stats.BatteryHistoryDb;
import exp.ftxt.features.battery_stats.BatteryMonitor;
import exp.ftxt.features.battery_stats.BatteryReading;

public class BatterySnapshotExporter {

    private final MainActivity activity;
    private final BatteryMonitorTabController monitor;

    public BatterySnapshotExporter(MainActivity activity, BatteryMonitorTabController monitor) {
        this.activity = activity;
        this.monitor = monitor;
    }

    public void copyToClipboard() {
        if (monitor.batMonitorMetricsText1 == null) return;
        BatteryReading.Snapshot s = BatteryMonitor.getLastSnapshot();
        BatteryCapacityEstimator.HealthResult h = BatteryCapacityEstimator.getResult();
        StringBuilder sb = new StringBuilder();
        sb.append("Baterai Perangkat\n\n");
        sb.append("Level           : ").append(s.percent).append("%\n");
        sb.append("Kapasitas       : ").append(s.chargeMah >= 0 ? s.chargeMah + " mAh" : "—").append("\n");
        sb.append("Status          : ").append(BatteryMonitorTabController.shortChargingStatus(s)).append("\n\n");
        sb.append("Metrik Real-Time\n").append(combineMetricColumns());
        sb.append("\n\nKesehatan Baterai\n");
        sb.append("Estimasi Kapasitas : ").append(healthMedianText(h)).append("\n");
        sb.append("Skor Kesehatan     : ").append(healthScoreText(h)).append("\n");
        sb.append("Sesi Tercatat      : ").append(h.sessionCount).append("\n");
        sb.append("Keyakinan          : ").append(healthConfidenceText(h)).append("\n");
        ClipboardManager cm = (ClipboardManager) activity.getSystemService(android.content.Context.CLIPBOARD_SERVICE);
        if (cm == null) return;
        cm.setPrimaryClip(ClipData.newPlainText("FTxT Monitor Baterai", sb.toString()));
        Toast.makeText(activity, "Disalin ke clipboard", Toast.LENGTH_SHORT).show();
    }

    private String healthMedianText(BatteryCapacityEstimator.HealthResult r) {
        return r.medianMah > 0 ? String.format(Locale.US, "%.0f mAh", r.medianMah) : "—";
    }

    private String healthScoreText(BatteryCapacityEstimator.HealthResult r) {
        if (r.designMah <= 0) return "Isi kapasitas desain";
        if (r.medianMah <= 0) return "Belum ada data";
        return String.format(Locale.US, "%.1f%%", r.medianMah / r.designMah * 100f);
    }

    private String healthConfidenceText(BatteryCapacityEstimator.HealthResult r) {
        if (r.totalSamples <= 0) return "—";
        return r.fromScreenOffSessions
                ? r.totalSamples + " sampel (layar mati)"
                : r.totalSamples + " sampel";
    }

    private String combineMetricColumns() {
        String left = monitor.batMonitorMetricsText1.getText().toString().trim();
        String right = monitor.batMonitorMetricsText2.getText().toString().trim();
        if (left.isEmpty()) return right;
        if (right.isEmpty()) return left;
        return left + "\n" + right;
    }

    public void exportBatterySnapshot() {
        String exportTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                .format(new Date());
        BatteryReading.Snapshot[] history =
                BatteryHistoryDb.get(activity).queryLastSamples(20);
        BatteryCapacityEstimator.HealthResult health = BatteryCapacityEstimator.getResult();
        StringBuilder sb = new StringBuilder();
        sb.append("FTxT - Monitor Baterai (Riwayat 20 Snapshot Terakhir)\n");
        sb.append("Ekspor: ").append(exportTime).append("\n");
        sb.append("Jumlah snapshot: ").append(history.length).append("\n\n");
        sb.append("=== Kesehatan Baterai ===\n");
        sb.append("Estimasi Kapasitas : ").append(healthMedianText(health)).append("\n");
        String scoreText = healthScoreText(health);
        if (health.designMah > 0 && health.medianMah > 0) {
            scoreText += " (kapasitas desain " + health.designMah + " mAh)";
        }
        sb.append("Skor Kesehatan     : ").append(scoreText).append("\n");
        sb.append("Sesi Tercatat      : ").append(health.sessionCount).append("\n");
        sb.append("Keyakinan          : ").append(healthConfidenceText(health)).append("\n\n");
        int index = 1;
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        for (BatteryReading.Snapshot snap : history) {
            String time = timeFormat.format(new Date(snap.time));
            sb.append("--- Snapshot ").append(index++)
                    .append("/").append(history.length)
                    .append(" (").append(time).append(") ---\n");
            sb.append("Persentase       : ").append(snap.percent).append("%\n");
            sb.append("Suhu             : ").append(String.format(Locale.US, "%.1f°C", snap.tempC)).append("\n");
            sb.append("Voltase          : ").append(String.format(Locale.US, "%.3fV", snap.voltageV)).append("\n");
            sb.append("Arus             : ").append(snap.currentMa != 0
                    ? String.format(Locale.US, "%+d mA", snap.currentMa) : "—").append("\n");
            sb.append("Daya             : ").append(snap.powerW > 0
                    ? String.format(Locale.US, "%.2fW", snap.powerW) : "—").append("\n");
            sb.append("Kapasitas Tersisa: ").append(snap.chargeMah >= 0 ? snap.chargeMah + " mAh" : "—").append("\n");
            sb.append("Cycle Count      : ").append(snap.cycleCount >= 0 ? String.valueOf(snap.cycleCount) : "—").append("\n");
            sb.append("Status           : ").append(snap.chargingText()).append("\n");
            sb.append("Teknologi        : ").append(snap.technology != null ? snap.technology : "—").append("\n");
            sb.append("Kondisi          : ").append(snap.conditionText()).append("\n\n");
        }

        String fileName = "FTxT_baterai_" + System.currentTimeMillis() + ".txt";
        try {
            if (writeSnapshotToDownload(sb.toString(), fileName)) {
                Toast.makeText(activity, "Tersimpan: Download/" + fileName +
                        " (" + history.length + " snapshot)", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(activity, "Gagal menyimpan snapshot", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(activity, "Gagal menyimpan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean writeSnapshotToDownload(String content, String fileName) throws Exception {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
            Uri uri = activity.getContentResolver().insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri == null) return false;
            try (OutputStream os = activity.getContentResolver().openOutputStream(uri)) {
                if (os == null) return false;
                os.write(content.getBytes("UTF-8"));
            }
            return true;
        } else {
            File dir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS);
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, fileName);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(content.getBytes("UTF-8"));
            }
            return true;
        }
    }
}
