package exp.ftxt.features.battery_current;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.WindowManager;

import java.lang.reflect.Field;

import exp.ftxt.shared.ui.OverlayDragHandler;
import exp.ftxt.shared.ui.OverlayShadow;
import exp.ftxt.shared.ui.ShadowTextView;

public class BatteryCurrentModule {

    private ShadowTextView view;
    private WindowManager.LayoutParams params;
    private WindowManager wm;
    private Context context;
    private boolean running;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public static Runnable onPositionUpdate;
    private String orientationSuffix;

    public void setOrientationSuffix(String suffix) {
        this.orientationSuffix = suffix;
    }

    public void start(WindowManager windowManager, Context ctx) {
        if (running) return;
        wm = windowManager;
        context = ctx;

        view = new ShadowTextView(ctx);
        view.setShadowConfig(BatteryCurrentConfig.shadow);
        view.setText(getBatteryCurrentText());
        view.setTextSize(BatteryCurrentConfig.size);
        view.setTextColor(BatteryCurrentConfig.color);
        applyBackground();

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = (int)(BatteryCurrentConfig.posX * getScreenWidth());
        params.y = (int)(BatteryCurrentConfig.posY * getScreenHeight());

        OverlayShadow.apply(view, params, wm, BatteryCurrentConfig.shadow, 4f);
        updateTouchFlags();

        try {
            wm.addView(view, params);
        } catch (Exception e) {
            e.printStackTrace();
            view = null;
            return;
        }

        view.post(this::updatePosition);
        running = true;
        handler.post(tickRunnable);
    }

    public void stop() {
        running = false;
        handler.removeCallbacks(tickRunnable);
        if (view != null && wm != null) {
            try {
                wm.removeView(view);
            } catch (Exception e) {
                e.printStackTrace();
            }
            view = null;
        }
    }

    public void updateSize(float size) {
        BatteryCurrentConfig.size = size;
        if (view != null) view.setTextSize(size);
    }

    public void updateColor(int color) {
        BatteryCurrentConfig.color = color;
        if (view != null) view.setTextColor(color);
    }

    public void updateShadow() {
        if (view != null) view.setShadowConfig(BatteryCurrentConfig.shadow);
        OverlayShadow.apply(view, params, wm, BatteryCurrentConfig.shadow, 4f);
    }

    public void updateBackground() {
        applyBackground();
    }

    public void updatePosition() {
        if (view != null && params != null && wm != null) {
            params.x = (int)(BatteryCurrentConfig.posX * getScreenWidth());
            params.y = (int)(BatteryCurrentConfig.posY * getScreenHeight());
            if (BatteryCurrentConfig.safeArea && view.getWidth() > 0 && view.getHeight() > 0) {
                int maxX = Math.max(0, getScreenWidth() - view.getWidth());
                int maxY = Math.max(0, getScreenHeight() - view.getHeight());
                params.x = Math.max(0, Math.min(params.x, maxX));
                params.y = Math.max(0, Math.min(params.y, maxY));
            }
            try {
                wm.updateViewLayout(view, params);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (onPositionUpdate != null) onPositionUpdate.run();
        }
    }

    public boolean isRunning() {
        return running;
    }

    public int[] getCurrentPosition() {
        if (params != null) return new int[]{params.x, params.y};
        return null;
    }

    private void applyBackground() {
        if (view == null) return;
        if (BatteryCurrentConfig.bgEnabled) {
            int pad = BatteryCurrentConfig.bgPadding;
            view.setPadding(pad, pad, pad, pad);
        } else {
            view.setPadding(0, 0, 0, 0);
        }
        view.setBgEnabled(BatteryCurrentConfig.bgEnabled);
        view.setBgColor(BatteryCurrentConfig.bgColor);
        view.setBgOffsetX(BatteryCurrentConfig.bgOffsetX);
        view.setBgOffsetY(BatteryCurrentConfig.bgOffsetY);
        view.setBgMargin(BatteryCurrentConfig.bgMargin);
        view.setBgRadius(BatteryCurrentConfig.bgRadius);
    }

    public void updateTouchFlags() {
        if (params == null || view == null || wm == null) return;

        if (BatteryCurrentConfig.touchPassthrough) {
            params.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            view.setOnTouchListener(null);
        } else {
            params.flags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            view.setOnTouchListener(new OverlayDragHandler(params, wm,
                    null,
                    () -> {
                        if (BatteryCurrentConfig.safeArea && view != null && view.getWidth() > 0 && view.getHeight() > 0) {
                            params.x = Math.max(0, Math.min(params.x, getScreenWidth() - view.getWidth()));
                            params.y = Math.max(0, Math.min(params.y, getScreenHeight() - view.getHeight()));
                        }
                        if (params != null) {
                            BatteryCurrentConfig.posX = Math.max(0, Math.min(1, (float) params.x / getScreenWidth()));
                            BatteryCurrentConfig.posY = Math.max(0, Math.min(1, (float) params.y / getScreenHeight()));
                        }
                        if (onPositionUpdate != null) onPositionUpdate.run();
                    }));
        }

        try { wm.updateViewLayout(view, params); } catch (Exception e) { e.printStackTrace(); }
    }

    private int getScreenWidth() {
        android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(metrics);
        return metrics.widthPixels;
    }

    private int getScreenHeight() {
        android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(metrics);
        return metrics.heightPixels;
    }

    private final Runnable tickRunnable = new Runnable() {
        @Override
        public void run() {
            if (!running) return;
            if (view != null) {
                view.setText(getBatteryCurrentText());
            }
            handler.postDelayed(this, 1000);
        }
    };

    private String getBatteryCurrentText() {
        int voltage = 0;
        int current = 0;

        // Source 1: sticky broadcast
        try {
            Intent batteryIntent = context.registerReceiver(null,
                    new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            if (batteryIntent != null) {
                voltage = batteryIntent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
                try {
                    Field field = BatteryManager.class.getField("EXTRA_CURRENT_NOW");
                    String extra = (String) field.get(null);
                    current = batteryIntent.getIntExtra(extra, 0);
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}

        // Source 2: BatteryManager.getLongProperty (API 28+)
        if (current == 0 && Build.VERSION.SDK_INT >= 28) {
            try {
                BatteryManager bm = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
                long c = bm.getLongProperty(2);
                if (c != 0) current = (int) (c / 1000);
            } catch (Exception ignored) {}
        }

        // Source 3: sysfs
        if (voltage == 0) {
            voltage = readSysfs("voltage_now", 1000);
        }
        if (current == 0) {
            current = readSysfs("current_now", 1000);
        }

        String sign = "";
        int mA = current;
        if (current > 0) {
            sign = "+";
        } else if (current < 0) {
            sign = "-";
            mA = -current;
        }

        double powerW = 0;
        if (voltage > 0 && mA > 0) {
            powerW = (voltage / 1000.0) * (mA / 1000.0);
        }

        StringBuilder sb = new StringBuilder();
        if (BatteryCurrentConfig.showVoltage) {
            sb.append(String.format("%dmV", voltage));
        }
        if (BatteryCurrentConfig.showCurrent) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(String.format("%s%dmA", sign, mA));
        }
        if (BatteryCurrentConfig.showPower) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(String.format("%.1fW", powerW));
        }
        return sb.length() > 0 ? sb.toString() : "N/A";
    }

    private int readSysfs(String file, int divisor) {
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
