package exp.ftxt.features.battery_stats;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Logika pemisahan sesi baterai.
 *
 * Arah sesi berjalan diputuskan dari pergantian status Android (colok/cabut) —
 * yang lebih baru & lebih nyata daripada pergerakan persen. Jendela 10 menit = 1%
 * tetap dipakai saat status & pergerakan persen konsisten, serta untuk merekonstruksi
 * segmen riwayat (buildSegments): arah bergeser bila dalam jendela persen berubah
 * ≥1% (naik → charging, turun → discharge); persen diam bukan sesi baru.
 *
 * Seluruh metode bekerja pada larik sampel terurut MENURUN waktu (indeks 0 =
 * sampel terbaru) — konsisten dengan {@link BatteryHistoryDb#queryLastSamples}.
 */
public final class SessionSegmentBuilder {

    /** Jendela pengamatan arah: 10 menit. */
    public static final long WINDOW_MS = 600_000L;
    /** Pergeseran persen minimal untuk menyatakan arah berubah. */
    public static final int MIN_MOVE_PCT = 1;
    /** Jeda antar sampel yang memisahkan dua sesi berbeda. */
    public static final long SESSION_GAP_MS = 900_000L;

    public enum Direction { CHARGE, DISCHARGE, FLAT }

    /** Menjawab apakah layar menyala pada suatu waktu (dari activity log). */
    public interface ScreenOnOracle {
        boolean isScreenOn(long timeMs);
    }

    /** Satu segmen sesi yang direkonstruksi dari sampel mentah. */
    public static final class Segment {
        public final Direction direction;
        public final long startTime;
        public final long endTime;
        public final int startPercent;
        public final int endPercent;
        public final double mAhIntegral;
        public final double deltaChargeMah;
        public final long screenOnMs;
        public final int sampleCount;
        public final float tempMin;
        public final float tempMax;
        public final float tempAvg;

        Segment(Direction direction, long startTime, long endTime,
                int startPercent, int endPercent, double mAhIntegral,
                double deltaChargeMah, long screenOnMs, int sampleCount,
                float tempMin, float tempMax, float tempAvg) {
            this.direction = direction;
            this.startTime = startTime;
            this.endTime = endTime;
            this.startPercent = startPercent;
            this.endPercent = endPercent;
            this.mAhIntegral = mAhIntegral;
            this.deltaChargeMah = deltaChargeMah;
            this.screenOnMs = screenOnMs;
            this.sampleCount = sampleCount;
            this.tempMin = tempMin;
            this.tempMax = tempMax;
            this.tempAvg = tempAvg;
        }

        public long durationMs() {
            return Math.max(0L, endTime - startTime);
        }
    }

    /** Hasil scan sesi berjalan. */
    public static final class LivingSession {
        public final int startIndex;
        public final Direction direction;
        public final boolean truncated;

        LivingSession(int startIndex, Direction direction, boolean truncated) {
            this.startIndex = startIndex;
            this.direction = direction;
            this.truncated = truncated;
        }
    }

    private SessionSegmentBuilder() {}

    // ---------- arah ----------

    /**
     * Arah pergerakan persen pada indeks {@code idx}, dilihat dari jendela
     * pengamatan yang berakhir di sampel tersebut.
     */
    static Direction directionAt(SnapshotHolder holder, int idx, int[] lowHolder) {
        int low = holder.lowIndex(idx, lowHolder);
        int dp = holder.desc[idx].percent - holder.desc[low].percent;
        if (dp >= MIN_MOVE_PCT) return Direction.CHARGE;
        if (-dp >= MIN_MOVE_PCT) return Direction.DISCHARGE;
        return Direction.FLAT;
    }

    /**
     * Arah sesi berjalan: pergantian status Android (colok/cabut) lebih baru
     * & lebih nyata daripada pergerakan persen di jendela. Bila status charging
     * namun persen belum berbalik (baru dicolok), atau sebaliknya persen masih
     * menanjak sesaat setelah kabel dicabut (settling), status yang menang.
     * Pergerakan persen tetap dipakai saat hasil keduanya konsisten.
     */
    static Direction resolveDirection(boolean nowCharging, Direction windowDir) {
        Direction statusDir = nowCharging ? Direction.CHARGE : Direction.DISCHARGE;
        if (windowDir == Direction.FLAT || windowDir != statusDir) return statusDir;
        return windowDir;
    }

    /** Pemeran data sampel DESC + pointer jendela yang bergerak monoton. */
    private static final class SnapshotHolder {
        final BatteryReading.Snapshot[] desc;
        final long windowMs;

        SnapshotHolder(BatteryReading.Snapshot[] desc, long windowMs) {
            this.desc = desc;
            this.windowMs = windowMs;
        }

        /** Indeks sampel tertua yang masih berada dalam jendela berakhir di {@code idx}. */
        int lowIndex(int idx, int[] lowHolder) {
            int low = Math.max(lowHolder[0], idx);
            long oldest = desc[idx].time - windowMs;
            while (low + 1 < desc.length && desc[low + 1].time >= oldest) low++;
            lowHolder[0] = low;
            return low;
        }
    }

    // ---------- sesi berjalan ----------

    /**
     * Mencari titik mulai sesi yang sedang berjalan (berisi sampel terbaru).
     * Sesi berjalan berakhir mundur ketika: ada gap antar sampel >
     * {@link #SESSION_GAP_MS} (perangkat mati lama / sesi usang), arah jendela
     * berbalik stabil, atau status Android berubah (colok/cabut) — titik status
     * berubah tersebut diambil sebagai batas mulai sesi.
     */
    public static LivingSession livingSession(BatteryReading.Snapshot[] desc) {
        return livingSession(desc, WINDOW_MS, SESSION_GAP_MS);
    }

    public static LivingSession livingSession(BatteryReading.Snapshot[] desc,
                                              long windowMs, long gapMs) {
        if (desc == null || desc.length == 0) return new LivingSession(0, Direction.FLAT, true);
        int[] low = new int[]{0};
        SnapshotHolder h = new SnapshotHolder(desc, windowMs);

        boolean nowCharging = desc[0].isCharging();
        Direction dir = resolveDirection(nowCharging, directionAt(h, 0, low));
        if (dir == Direction.FLAT) return new LivingSession(0, Direction.FLAT, false);

        int statusStart = -1;
        int k = 0;
        while (k + 1 < desc.length) {
            if (desc[k].time - desc[k + 1].time > gapMs) break;
            if (desc[k + 1].isCharging() != nowCharging) {
                statusStart = k + 1;
                break;
            }
            k++;
        }

        int i = 0;
        while (i + 1 < desc.length) {
            if (desc[i].time - desc[i + 1].time > gapMs) break;
            Direction d = directionAt(h, i + 1, low);
            if (d != Direction.FLAT && d != dir) break;
            i++;
        }
        int startIndex = i + 1;
        if (statusStart >= 0 && statusStart < startIndex) startIndex = statusStart;
        return new LivingSession(startIndex, dir, startIndex >= desc.length);
    }

    // ---------- rekonstruksi segmen ----------

    /**
     * Membangun daftar segmen sesi dari sampel mentah terurut menurun.
     * Hasil dikembalikan urut waktu naik (kronologis). Jeda palsu dalam
     * pengisian (persen diam / status berubah sesaat) TIDAK memecah segmen —
     * hanya gap > {@link #SESSION_GAP_MS} atau arah jendela yang berbalik stabil.
     */
    public static ArrayList<Segment> buildSegments(BatteryReading.Snapshot[] desc) {
        return buildSegments(desc, WINDOW_MS, SESSION_GAP_MS, null);
    }

    public static ArrayList<Segment> buildSegments(BatteryReading.Snapshot[] desc,
                                                   long windowMs, long gapMs,
                                                   ScreenOnOracle oracle) {
        ArrayList<Segment> out = new ArrayList<>();
        if (desc == null || desc.length == 0) return out;

        int[] low = new int[]{0};
        SnapshotHolder h = new SnapshotHolder(desc, windowMs);

        int i = 0;
        while (i < desc.length) {
            Direction dir = directionAt(h, i, low);
            if (dir == Direction.FLAT) {
                i++;
                continue;
            }
            int j = i;
            while (j + 1 < desc.length) {
                if (desc[j].time - desc[j + 1].time > gapMs) break;
                Direction d = directionAt(h, j + 1, low);
                if (d == Direction.FLAT || d == dir) j++;
                else break;
            }
            // segmen = sampel [i..j] (desc), j lebih tua.
            out.add(materialize(desc, i, j, dir, oracle));
            i = j + 1;
        }
        Collections.reverse(out);
        return out;
    }

    private static Segment materialize(BatteryReading.Snapshot[] desc, int newest, int oldest,
                                       Direction dir, ScreenOnOracle oracle) {
        // desc: indeks lebih kecil = lebih baru; segmen terbentang [newest..oldest].
        float tMin = Float.MAX_VALUE;
        float tMax = Float.MIN_VALUE;
        double tSum = 0;
        int tCount = 0;
        for (int k = oldest; k >= newest; k--) {
            BatteryReading.Snapshot s = desc[k];
            if (s.tempC > 0f) {
                tSum += s.tempC;
                tCount++;
                if (s.tempC < tMin) tMin = s.tempC;
                if (s.tempC > tMax) tMax = s.tempC;
            }
        }
        double integral = 0.0;
        long screenOn = 0;
        for (int k = oldest; k > newest; k--) {
            long dt = desc[k - 1].time - desc[k].time;
            if (dt > 0 && dt < 5000) {
                double mah = Math.abs(desc[k - 1].currentMa) * (dt / 3600000.0);
                integral += mah;
                if (oracle != null && oracle.isScreenOn(desc[k - 1].time)) screenOn += dt;
            }
        }
        long startTime = desc[oldest].time;
        long endTime = desc[newest].time;
        double deltaChargeMah = 0.0;
        long sc = desc[oldest].chargeMah;
        long ec = desc[newest].chargeMah;
        if (sc > 0 && ec > 0) deltaChargeMah = (double) (ec - sc);
        return new Segment(dir, startTime, endTime,
                desc[oldest].percent, desc[newest].percent,
                integral, deltaChargeMah, screenOn, oldest - newest + 1,
                tMin == Float.MAX_VALUE ? 0f : tMin,
                tMax == Float.MIN_VALUE ? 0f : tMax,
                tCount > 0 ? (float) (tSum / tCount) : 0f);
    }

    // ---------- bantuan ----------

    /** Mengubah larik urut naik menjadi menurun (indeks 0 = terbaru). */
    public static BatteryReading.Snapshot[] toDesc(BatteryReading.Snapshot[] asc) {
        int n = asc.length;
        BatteryReading.Snapshot[] out = new BatteryReading.Snapshot[n];
        for (int i = 0; i < n; i++) out[i] = asc[n - 1 - i];
        return out;
    }

    /**
     * Membangun oracle layar-nyala dari daftar waktu & status activity log
     * (waktu urut naik; state yang berlaku = entri terakhir ≤ t).
     */
    public static ScreenOnOracle screenOnOracle(final long[] times, final boolean[] screenOn) {
        return t -> {
            if (times == null || times.length == 0) return false;
            int lo = 0;
            int hi = times.length - 1;
            int ans = -1;
            while (lo <= hi) {
                int mid = (lo + hi) >>> 1;
                if (times[mid] <= t) {
                    ans = mid;
                    lo = mid + 1;
                } else {
                    hi = mid - 1;
                }
            }
            return ans >= 0 && ans < screenOn.length && screenOn[ans];
        };
    }
}