package exp.ftxt.features.battery_current;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.WindowManager;

import java.lang.reflect.Field;

import exp.ftxt.shared.ui.OverlayDragHandler;
import exp.ftxt.shared.ui.OverlayModule;
import exp.ftxt.shared.ui.OverlayShadow;
import exp.ftxt.shared.ui.ShadowTextView;

public class BatteryCurrentModule implements OverlayModule {

    private ShadowTextView view;
    private WindowManager.LayoutParams params;
    private WindowManager wm;
    private Context context;
    private SharedPreferences prefs;
    private boolean running;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public static Runnable onPositionUpdate;
    private String orientationSuffix;

    @Override
    public void setOrientationSuffix(String suffix) {
        this.orientationSuffix = suffix;
    }

    @Override
    public void init(WindowManager windowManager, Context ctx, SharedPreferences sp) {
        wm = windowManager;
        context = ctx;
        prefs = sp;
        orientationSuffix = null;
    }

    @Override
    public void start(WindowManager windowManager, Context ctx) {
        if (running) return;
        wm = windowManager;
        context = ctx;
        prefs = ctx.getSharedPreferences("ftxt_prefs", Context.MODE_PRIVATE);

        view = new ShadowTextView(ctx);
        view.setShadowConfig(BatteryCurrentConfig.shadow);
        view.setTextSize(BatteryCurrentConfig.size);
        applyBackground();
        updateDisplay();

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

    @Override
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
        params = null;
    }

    @Override
    public void updateSize(float size) {
        BatteryCurrentConfig.size = size;
        if (view != null) view.setTextSize(size);
    }

    @Override
    public void updateColor(int color) {
        BatteryCurrentConfig.color = color;
        updateDisplay();
    }

    @Override
    public void updateLabelColor(int color) {
        BatteryCurrentConfig.labelColor = color;
        updateDisplay();
    }

    @Override
    public void updateSeparatorColor(int color) {
        BatteryCurrentConfig.separatorColor = color;
        updateDisplay();
    }

    @Override
    public void updateShadow() {
        if (view != null) view.setShadowConfig(BatteryCurrentConfig.shadow);
        OverlayShadow.apply(view, params, wm, BatteryCurrentConfig.shadow, 4f);
    }

    @Override
    public void updateBackground() {
        applyBackground();
    }

    private String posSuffix() {
        if (orientationSuffix != null) return "_" + orientationSuffix;
        return (context.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE) ? "_land" : "_port";
    }

    public void loadPosition() {
        String sfx = posSuffix();
        BatteryCurrentConfig.posX = prefs.getFloat("batcur_pos_x" + sfx, 0.75f);
        BatteryCurrentConfig.posY = prefs.getFloat("batcur_pos_y" + sfx, 0.85f);
        updatePosition();
    }

    @Override
    public void reloadPosition() {
        orientationSuffix = null;
        loadPosition();
    }

    @Override
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

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void hide() {
        if (view != null) view.setVisibility(android.view.View.GONE);
    }

    @Override
    public void show() {
        if (view != null) view.setVisibility(android.view.View.VISIBLE);
    }

    @Override
    public boolean isHidden() {
        if (view != null) return view.getVisibility() == android.view.View.GONE;
        return false;
    }

    @Override
    public int[] getCurrentPosition() {
        if (params != null) return new int[]{params.x, params.y};
        return null;
    }

    private void applyBackground() {
        if (view == null) return;
        if (BatteryCurrentConfig.bg.enabled) {
            int pad = BatteryCurrentConfig.bg.padding;
            view.setPadding(pad, pad, pad, pad);
        } else {
            view.setPadding(0, 0, 0, 0);
        }
        view.setBgEnabled(BatteryCurrentConfig.bg.enabled);
        view.setBgColor(BatteryCurrentConfig.bg.color);
        view.setBgOffsetX(BatteryCurrentConfig.bg.offsetX);
        view.setBgOffsetY(BatteryCurrentConfig.bg.offsetY);
        view.setBgMargin(BatteryCurrentConfig.bg.margin);
        view.setBgRadius(BatteryCurrentConfig.bg.radius);
    }

    @Override
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

    private void updateDisplay() {
        if (view == null) return;
        if (BatteryCurrentConfig.showOnlyValue) {
            view.setTextColor(BatteryCurrentConfig.color);
            view.setText(getBatteryCurrentValueOnly());
        } else {
            String text = getBatteryCurrentText();
            view.setTextColor(BatteryCurrentConfig.color);
            SpannableString spannable = new SpannableString(text);
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == 'm' || c == 'V' || c == 'A' || c == 'W') {
                    spannable.setSpan(new ForegroundColorSpan(BatteryCurrentConfig.labelColor),
                            i, i + 1, 0);
                } else if (c == '|') {
                    spannable.setSpan(new ForegroundColorSpan(BatteryCurrentConfig.separatorColor),
                            i, i + 1, 0);
                }
            }
            view.setText(spannable);
        }
    }

    private final Runnable tickRunnable = new Runnable() {
        @Override
        public void run() {
            if (!running) return;
            updateDisplay();
            handler.postDelayed(this, (long)(BatteryCurrentConfig.updateInterval * 1000));
        }
    };

    private String getBatteryCurrentValueOnly() {
        int voltage = 0;
        int current = 0;

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

        if (current == 0 && Build.VERSION.SDK_INT >= 28) {
            try {
                BatteryManager bm = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
                long c = bm.getLongProperty(2);
                if (c != 0) current = (int) (c / 1000);
            } catch (Exception ignored) {}
        }

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
            sb.append(String.format("%.1f", voltage / 1000.0));
        }
        if (BatteryCurrentConfig.showCurrent) {
            if (sb.length() > 0) sb.append(" | ");
            sb.append(String.format("%s%d", sign, mA));
        }
        if (BatteryCurrentConfig.showPower) {
            if (sb.length() > 0) sb.append(" | ");
            sb.append(String.format("%.1f", powerW));
        }
        return sb.length() > 0 ? sb.toString() : "N/A";
    }

    private String getBatteryCurrentText() {
        int voltage = 0;
        int current = 0;

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

        if (current == 0 && Build.VERSION.SDK_INT >= 28) {
            try {
                BatteryManager bm = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
                long c = bm.getLongProperty(2);
                if (c != 0) current = (int) (c / 1000);
            } catch (Exception ignored) {}
        }

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
            sb.append(String.format("%.1fV", voltage / 1000.0));
        }
        if (BatteryCurrentConfig.showCurrent) {
            if (sb.length() > 0) sb.append(" | ");
            sb.append(String.format("%s%dmA", sign, mA));
        }
        if (BatteryCurrentConfig.showPower) {
            if (sb.length() > 0) sb.append(" | ");
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
