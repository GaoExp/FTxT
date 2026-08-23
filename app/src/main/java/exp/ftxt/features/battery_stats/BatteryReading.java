package exp.ftxt.features.battery_stats;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;

import java.lang.reflect.Field;

/**
 * Pembaca metrik baterai tunggal (single source of truth).
 * Overlay Battery Info dan tab Monitor memanggil {@link #read(Context)} —
 * sumber data & konversi satuan hanya ada di satu tempat ini.
 */
public final class BatteryReading {

    private static final IntentFilter BATTERY_FILTER =
            new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

    private BatteryReading() {}

    public static final class Snapshot {
        public final long time;
        public final float tempC;
        public final int percent;
        public final float voltageV;
        public final int currentMa;
        public final double powerW;
        public final long chargeMah;
        public final int cycleCount;
        public final int statusInt;
        public final int pluggedInt;
        public final String technology;

        Snapshot(long time, float tempC, int percent, float voltageV, int currentMa,
                 double powerW, long chargeMah, int cycleCount, int statusInt,
                 int pluggedInt, String technology) {
            this.time = time;
            this.tempC = tempC;
            this.percent = percent;
            this.voltageV = voltageV;
            this.currentMa = currentMa;
            this.powerW = powerW;
            this.chargeMah = chargeMah;
            this.cycleCount = cycleCount;
            this.statusInt = statusInt;
            this.pluggedInt = pluggedInt;
            this.technology = technology;
        }

        static Snapshot empty() {
            return new Snapshot(0, 0f, 0, 0f, 0, 0d, -1L, -1, 0, 0, null);
        }

        public boolean isCharging() {
            return statusInt == BatteryManager.BATTERY_STATUS_CHARGING
                    || statusInt == BatteryManager.BATTERY_STATUS_FULL;
        }

        public String chargingText() {
            if (statusInt == BatteryManager.BATTERY_STATUS_FULL) return "Penuh";
            if (statusInt == BatteryManager.BATTERY_STATUS_CHARGING) {
                switch (pluggedInt) {
                    case BatteryManager.BATTERY_PLUGGED_AC: return "Mengisi (AC)";
                    case BatteryManager.BATTERY_PLUGGED_USB: return "Mengisi (USB)";
                    case BatteryManager.BATTERY_PLUGGED_WIRELESS: return "Mengisi (Wireless)";
                    default: return "Mengisi";
                }
            }
            return "Tidak Mengisi";
        }

        /** 0 = normal, 1 = panas, -1 = dingin */
        public int conditionLevel() {
            if (tempC >= 43f) return 1;
            if (tempC <= 0f) return -1;
            return 0;
        }

        public String conditionText() {
            int level = conditionLevel();
            if (level > 0) return "Panas";
            if (level < 0) return "Dingin";
            return "Normal";
        }
    }

    public static Snapshot read(Context ctx) {
        try {
            if (ctx == null) return Snapshot.empty();
            BatteryManager bm =
                    (BatteryManager) ctx.getSystemService(Context.BATTERY_SERVICE);

            int tempDeci = 0, level = 0, scale = 100, voltageMv = 0, currentMa = 0;
            int status = 0, plugged = 0;
            String tech = null;

            try {
                Intent i = ctx.registerReceiver(null, BATTERY_FILTER);
                if (i != null) {
                    tempDeci = i.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
                    level = i.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                    scale = i.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
                    voltageMv = i.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
                    status = i.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
                    plugged = i.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
                    tech = i.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
                    try {
                        Field field = BatteryManager.class.getField("EXTRA_CURRENT_NOW");
                        String extra = (String) field.get(null);
                        currentMa = i.getIntExtra(extra, 0);
                    } catch (Exception ignored) {}
                }
            } catch (Exception ignored) {}

            if (currentMa == 0 && Build.VERSION.SDK_INT >= 23 && bm != null) {
                try {
                    long c = bm.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
                    if (c != 0) currentMa = (int) (c / 1000);
                } catch (Exception ignored) {}
            }
            if (voltageMv == 0) voltageMv = readSysfs("voltage_now", 1000);
            if (currentMa == 0) currentMa = readSysfs("current_now", 1000);

            long chargeUah = -1;
            if (bm != null) {
                try {
                    long c = bm.getLongProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER);
                    if (c > 0) chargeUah = c;
                } catch (Exception ignored) {}
            }

            int cycles = -1;
            if (bm != null && Build.VERSION.SDK_INT >= 34) {
                try {
                    Field field = BatteryManager.class.getField("BATTERY_PROPERTY_CYCLE_COUNT");
                    int prop = field.getInt(null);
                    int c = bm.getIntProperty(prop);
                    if (c > 0) cycles = c;
                } catch (Exception ignored) {}
            }

            float tempC = tempDeci / 10f;
            int percent = (level > 0 && scale > 0) ? (level * 100) / scale : 0;
            float voltageV = voltageMv / 1000f;
            int absMa = Math.abs(currentMa);
            double powerW = (voltageV > 0 && absMa > 0)
                    ? (voltageV * absMa) / 1000d : 0d;
            long chargeMah = chargeUah > 0 ? chargeUah / 1000L : -1L;

            return new Snapshot(System.currentTimeMillis(), tempC, percent, voltageV,
                    currentMa, powerW, chargeMah, cycles, status, plugged, tech);
        } catch (Exception ignored) {
            return Snapshot.empty();
        }
    }

    private static int readSysfs(String file, int divisor) {
        try {
            java.io.File dir = new java.io.File("/sys/class/power_supply");
            java.io.File[] entries = dir.listFiles();
            if (entries == null) return 0;
            for (java.io.File entry : entries) {
                java.io.File f = new java.io.File(entry, file);
                if (!f.exists()) continue;
                java.io.BufferedReader r = new java.io.BufferedReader(new java.io.FileReader(f));
                String line = r.readLine();
                r.close();
                if (line == null) continue;
                return Integer.parseInt(line.trim()) / divisor;
            }
        } catch (Exception ignored) {}
        return 0;
    }
}
