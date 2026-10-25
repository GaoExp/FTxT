package exp.ftxt.features.network_stats;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.TrafficStats;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

public class DataUsageTracker {

    public interface Listener {
        void onUsageChanged();
    }

    public static class DayUsage {
        public final String date;
        public final long rx;
        public final long tx;
        public final boolean hasData;

        public DayUsage(String date, long rx, long tx, boolean hasData) {
            this.date = date;
            this.rx = rx;
            this.tx = tx;
            this.hasData = hasData;
        }
    }

    private static final String PREFS_NAME = "ftxt_prefs";
    private static final long FLUSH_INTERVAL_MS = 30000L;
    private static final int HISTORY_MAX_DAYS = 30;

    private static DataUsageTracker instance;

    private final SharedPreferences prefs;
    private final List<DayUsage> history = new ArrayList<>();
    private final CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<>();

    private boolean initialized;
    private boolean unsupported;
    private String dayKey;
    private long dayStart;
    private long dayRx;
    private long dayTx;
    private long lastRx;
    private long lastTx;
    private long peakRx;
    private long peakTx;
    private long lastFlushAt;

    private DataUsageTracker(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        load();
    }

    public static DataUsageTracker get(Context context) {
        if (instance == null) {
            synchronized (DataUsageTracker.class) {
                if (instance == null) {
                    instance = new DataUsageTracker(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private void load() {
        String storedKey = prefs.getString("usage_day_key", null);
        long storedLastRx = prefs.getLong("usage_last_rx", -1);
        long storedLastTx = prefs.getLong("usage_last_tx", -1);

        if (isValidDate(storedKey) && storedLastRx >= 0 && storedLastTx >= 0) {
            dayKey = storedKey;
            initialized = true;
            dayStart = prefs.getLong("usage_day_start", System.currentTimeMillis());
            dayRx = prefs.getLong("usage_day_rx", 0);
            dayTx = prefs.getLong("usage_day_tx", 0);
            lastRx = storedLastRx;
            lastTx = storedLastTx;
            peakRx = prefs.getLong("usage_peak_rx", 0);
            peakTx = prefs.getLong("usage_peak_tx", 0);
        }

        history.clear();
        history.addAll(parseHistory(prefs.getString("usage_history", "")));
        trimHistory();
    }

    public synchronized void observe(long rx, long tx) {
        if (rx < 0 || tx < 0) {
            unsupported = true;
            notifyListeners();
            return;
        }
        unsupported = false;

        long now = System.currentTimeMillis();
        String today = LocalDate.now().toString();

        if (!initialized) {
            initialized = true;
            dayKey = today;
            dayStart = now;
            dayRx = 0;
            dayTx = 0;
            peakRx = 0;
            peakTx = 0;
            lastRx = rx;
            lastTx = tx;
            flushInternal(now);
            notifyListeners();
            return;
        }

        boolean rotated = false;
        if (today.compareTo(dayKey) > 0) {
            appendHistory(dayKey, dayRx, dayTx);
            dayKey = today;
            dayStart = now;
            dayRx = 0;
            dayTx = 0;
            peakRx = 0;
            peakTx = 0;
            rotated = true;
        }

        long deltaRx = rx - lastRx;
        long deltaTx = tx - lastTx;
        if (deltaRx < 0) deltaRx = 0;
        if (deltaTx < 0) deltaTx = 0;

        dayRx += deltaRx;
        dayTx += deltaTx;
        lastRx = rx;
        lastTx = tx;

        if (rotated) {
            flushInternal(now);
        } else {
            maybeFlush(now);
        }
        notifyListeners();
    }

    public synchronized void noteSpeed(long rxBytesPerSec, long txBytesPerSec) {
        if (!initialized) return;
        if (rxBytesPerSec < 0) rxBytesPerSec = 0;
        if (txBytesPerSec < 0) txBytesPerSec = 0;

        boolean changed = false;
        if (rxBytesPerSec > peakRx) {
            peakRx = rxBytesPerSec;
            changed = true;
        }
        if (txBytesPerSec > peakTx) {
            peakTx = txBytesPerSec;
            changed = true;
        }
        if (changed) {
            maybeFlush(System.currentTimeMillis());
            notifyListeners();
        }
    }

    public synchronized void resetToday() {
        long rx = TrafficStats.getTotalRxBytes();
        long tx = TrafficStats.getTotalTxBytes();
        long now = System.currentTimeMillis();

        dayKey = LocalDate.now().toString();
        dayStart = now;
        dayRx = 0;
        dayTx = 0;
        peakRx = 0;
        peakTx = 0;

        if (rx >= 0 && tx >= 0) {
            initialized = true;
            unsupported = false;
            lastRx = rx;
            lastTx = tx;
        } else {
            initialized = false;
            unsupported = true;
            lastRx = -1;
            lastTx = -1;
        }

        flushInternal(now);
        notifyListeners();
    }

    public synchronized void flush() {
        if (!initialized) return;
        flushInternal(System.currentTimeMillis());
    }

    private void maybeFlush(long now) {
        if (now - lastFlushAt >= FLUSH_INTERVAL_MS) {
            flushInternal(now);
        }
    }

    private void flushInternal(long now) {
        if (!initialized) return;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < history.size(); i++) {
            if (i > 0) sb.append(';');
            DayUsage d = history.get(i);
            sb.append(d.date).append(':').append(d.rx).append(':').append(d.tx);
        }

        prefs.edit()
                .putString("usage_day_key", dayKey)
                .putLong("usage_day_start", dayStart)
                .putLong("usage_day_rx", dayRx)
                .putLong("usage_day_tx", dayTx)
                .putLong("usage_last_rx", lastRx)
                .putLong("usage_last_tx", lastTx)
                .putLong("usage_peak_rx", peakRx)
                .putLong("usage_peak_tx", peakTx)
                .putString("usage_history", sb.toString())
                .apply();
        lastFlushAt = now;
    }

    private void appendHistory(String key, long rx, long tx) {
        if (key == null) return;
        for (int i = history.size() - 1; i >= 0; i--) {
            if (key.equals(history.get(i).date)) {
                history.remove(i);
            }
        }
        history.add(new DayUsage(key, rx, tx, true));
        history.sort((a, b) -> a.date.compareTo(b.date));
        trimHistory();
    }

    private void trimHistory() {
        while (history.size() > HISTORY_MAX_DAYS) {
            history.remove(0);
        }
    }

    private static List<DayUsage> parseHistory(String raw) {
        List<DayUsage> result = new ArrayList<>();
        if (raw == null || raw.isEmpty()) return result;
        String[] parts = raw.split(";");
        for (String part : parts) {
            if (part.isEmpty()) continue;
            String[] fields = part.split(":");
            if (fields.length != 3) continue;
            if (!isValidDate(fields[0])) continue;
            try {
                long rx = Long.parseLong(fields[1]);
                long tx = Long.parseLong(fields[2]);
                if (rx < 0 || tx < 0) continue;
                result.add(new DayUsage(fields[0], rx, tx, true));
            } catch (NumberFormatException ignored) {
            }
        }
        result.sort((a, b) -> a.date.compareTo(b.date));
        return result;
    }

    private static boolean isValidDate(String value) {
        if (value == null) return false;
        try {
            LocalDate.parse(value);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public synchronized List<DayUsage> getDays(int count) {
        List<DayUsage> result = new ArrayList<>();
        if (count <= 0) return result;

        LocalDate today = LocalDate.now();
        List<DayUsage> merged = new ArrayList<>(history);

        if (initialized && dayKey != null) {
            boolean replaced = false;
            for (int i = 0; i < merged.size(); i++) {
                if (dayKey.equals(merged.get(i).date)) {
                    merged.set(i, new DayUsage(dayKey, dayRx, dayTx, true));
                    replaced = true;
                    break;
                }
            }
            if (!replaced) {
                merged.add(new DayUsage(dayKey, dayRx, dayTx, true));
            }
        }

        for (int i = count - 1; i >= 0; i--) {
            String key = today.minusDays(i).toString();
            DayUsage found = null;
            for (DayUsage d : merged) {
                if (key.equals(d.date)) {
                    found = d;
                    break;
                }
            }
            result.add(found != null ? found : new DayUsage(key, 0, 0, false));
        }
        return result;
    }

    public synchronized boolean isInitialized() {
        return initialized;
    }

    public synchronized boolean isUnsupported() {
        return unsupported;
    }

    public synchronized long getDayRx() {
        return dayRx;
    }

    public synchronized long getDayTx() {
        return dayTx;
    }

    public synchronized long getPeakRx() {
        return peakRx;
    }

    public synchronized long getPeakTx() {
        return peakTx;
    }

    public synchronized long getAverageRxPerHour() {
        return averagePerHour(dayRx);
    }

    public synchronized long getAverageTxPerHour() {
        return averagePerHour(dayTx);
    }

    private long averagePerHour(long total) {
        if (!initialized || dayStart <= 0) return -1;
        long elapsed = System.currentTimeMillis() - dayStart;
        if (elapsed < 3600000L) return -1;
        long hours = Math.max(1L, elapsed / 3600000L);
        return total / hours;
    }

    public void addListener(Listener listener) {
        if (listener != null) listeners.addIfAbsent(listener);
    }

    public void removeListener(Listener listener) {
        if (listener != null) listeners.remove(listener);
    }

    private void notifyListeners() {
        for (Listener listener : listeners) {
            listener.onUsageChanged();
        }
    }

    public static String formatBytes(long bytes) {
        if (bytes < 0) return "—";
        if (bytes < 1024L) return bytes + "B";
        if (bytes < 1048576L) return (bytes / 1024L) + "KB";
        if (bytes < 1073741824L) {
            return trimZeros(String.format(Locale.US, "%.1f", bytes / 1048576.0)) + "MB";
        }
        return trimZeros(String.format(Locale.US, "%.2f", bytes / 1073741824.0)) + "GB";
    }

    public static String formatSpeed(long bytesPerSec) {
        if (bytesPerSec < 0) return "—";
        if (bytesPerSec >= 1048576L) {
            return String.format(Locale.US, "%.2fMB/s", bytesPerSec / 1048576.0);
        }
        return (bytesPerSec / 1024L) + "KB/s";
    }

    private static String trimZeros(String value) {
        if (value.indexOf('.') < 0) return value;
        int end = value.length();
        while (end > 0 && value.charAt(end - 1) == '0') end--;
        if (end > 0 && value.charAt(end - 1) == '.') end--;
        return value.substring(0, end);
    }
}
