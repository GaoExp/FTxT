package exp.ftxt.features.battery_stats;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
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

public class BatteryStatsModule implements OverlayModule {

    private ShadowTextView view;
    private WindowManager.LayoutParams params;
    private WindowManager wm;
    private Context context;
    private SharedPreferences prefs;
    private boolean running;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private HandlerThread bgThread;
    private Handler bgHandler;
    private String lastRenderedText;
    private volatile boolean bgBusy;

    public static Runnable onPositionUpdate;
    private String orientationSuffix;

    private static final IntentFilter BATTERY_FILTER = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

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
        loadPosition();
    }

    @Override
    public void start(WindowManager windowManager, Context ctx) {
        if (running) return;
        wm = windowManager;
        context = ctx;
        prefs = ctx.getSharedPreferences("ftxt_prefs", Context.MODE_PRIVATE);

        view = new ShadowTextView(ctx);
        view.setShadowConfig(BatteryStatsConfig.shadow);
        view.setTextSize(BatteryStatsConfig.size);
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
        params.x = (int)(BatteryStatsConfig.posX * getScreenWidth());
        params.y = (int)(BatteryStatsConfig.posY * getScreenHeight());

        OverlayShadow.apply(view, params, wm, BatteryStatsConfig.shadow, 4f);
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
        bgThread = new HandlerThread("bat-read");
        bgThread.start();
        bgHandler = new Handler(bgThread.getLooper());
        handler.post(tickRunnable);
    }

    @Override
    public void stop() {
        running = false;
        handler.removeCallbacks(tickRunnable);
        if (bgThread != null) {
            bgThread.quitSafely();
            bgThread = null;
            bgHandler = null;
        }
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
        BatteryStatsConfig.size = size;
        if (view != null) view.setTextSize(size);
    }

    @Override
    public void updateColor(int color) {
        BatteryStatsConfig.color = color;
        updateDisplay();
    }

    @Override
    public void updateLabelColor(int color) {
        BatteryStatsConfig.labelColor = color;
        updateDisplay();
    }

    @Override
    public void updateSeparatorColor(int color) {
        BatteryStatsConfig.separatorColor = color;
        updateDisplay();
    }

    @Override
    public void updateShadow() {
        if (view != null) view.setShadowConfig(BatteryStatsConfig.shadow);
        OverlayShadow.apply(view, params, wm, BatteryStatsConfig.shadow, 4f);
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
        BatteryStatsConfig.posX = prefs.getFloat("battery_pos_x" + sfx, 0.05f);
        BatteryStatsConfig.posY = prefs.getFloat("battery_pos_y" + sfx, 0.8f);
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
            params.x = (int)(BatteryStatsConfig.posX * getScreenWidth());
            params.y = (int)(BatteryStatsConfig.posY * getScreenHeight());
            if (BatteryStatsConfig.safeArea && view.getWidth() > 0 && view.getHeight() > 0) {
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
        if (BatteryStatsConfig.bg.enabled) {
            int pad = BatteryStatsConfig.bg.padding;
            view.setPadding(pad, pad, pad, pad);
        } else {
            view.setPadding(0, 0, 0, 0);
        }
        view.setBgEnabled(BatteryStatsConfig.bg.enabled);
        view.setBgColor(BatteryStatsConfig.bg.color);
        view.setBgOffsetX(BatteryStatsConfig.bg.offsetX);
        view.setBgOffsetY(BatteryStatsConfig.bg.offsetY);
        view.setBgMargin(BatteryStatsConfig.bg.margin);
        view.setBgRadius(BatteryStatsConfig.bg.radius);
    }

    @Override
    public void updateTouchFlags() {
        if (params == null || view == null || wm == null) return;

        if (BatteryStatsConfig.touchPassthrough) {
            params.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            view.setOnTouchListener(null);
        } else {
            params.flags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            view.setOnTouchListener(new OverlayDragHandler(params, wm,
                    null,
                    () -> {
                        if (BatteryStatsConfig.safeArea && view != null && view.getWidth() > 0 && view.getHeight() > 0) {
                            params.x = Math.max(0, Math.min(params.x, getScreenWidth() - view.getWidth()));
                            params.y = Math.max(0, Math.min(params.y, getScreenHeight() - view.getHeight()));
                        }
                        if (params != null) {
                            BatteryStatsConfig.posX = Math.max(0, Math.min(1, (float) params.x / getScreenWidth()));
                            BatteryStatsConfig.posY = Math.max(0, Math.min(1, (float) params.y / getScreenHeight()));
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

    public void refreshDisplay() {
        if (view == null) return;
        lastRenderedText = null;
        updateDisplay();
        updatePosition();
    }

    private void updateDisplay() {
        if (view == null) return;
        if (bgHandler != null) {
            bgHandler.post(() -> {
                BatterySnapshot snap = readBatterySnapshot();
                String text = buildDisplayText(snap);
                handler.post(() -> applyDisplay(text));
            });
        } else {
            BatterySnapshot snap = readBatterySnapshot();
            String text = buildDisplayText(snap);
            applyDisplay(text);
        }
    }

    private static class BatterySnapshot {
        String tempText;
        String pctText;
        String voltText;
        String curText;
        String powerText;
    }

    private String buildItemPart(String id, BatterySnapshot s) {
        if ("temp".equals(id) && BatteryStatsConfig.showTemperature && s.tempText != null) return s.tempText;
        if ("pct".equals(id) && BatteryStatsConfig.showPercentage && s.pctText != null) return s.pctText;
        if ("volt".equals(id) && BatteryStatsConfig.showVoltage && s.voltText != null) return s.voltText;
        if ("cur".equals(id) && BatteryStatsConfig.showCurrent && s.curText != null) return s.curText;
        if ("power".equals(id) && BatteryStatsConfig.showPower && s.powerText != null) return s.powerText;
        return null;
    }

    private BatterySnapshot readBatterySnapshot() {
        BatterySnapshot s = new BatterySnapshot();
        int temp = 0, level = 0, scale = 100, voltage = 0, current = 0;

        try {
            Intent batteryIntent = context.registerReceiver(null, BATTERY_FILTER);
            if (batteryIntent != null) {
                temp = batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
                level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
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

        if (voltage == 0) voltage = readSysfs("voltage_now", 1000);
        if (current == 0) current = readSysfs("current_now", 1000);

        if (temp != 0) {
            String tempText = (temp % 10 == 0)
                    ? String.valueOf(temp / 10)
                    : String.format("%.1f", temp / 10f);
            s.tempText = BatteryStatsConfig.showOnlyValue ? tempText : tempText + "°C";
        }

        if (level > 0 && scale > 0) {
            int percent = (level * 100) / scale;
            s.pctText = BatteryStatsConfig.showOnlyValue
                    ? String.valueOf(percent)
                    : String.format("%d%%", percent);
        }

        String sign = "";
        int mA = current;
        if (current > 0) {
            sign = "+";
        } else if (current < 0) {
            sign = "-";
            mA = -current;
        }

        if (voltage > 0) {
            s.voltText = BatteryStatsConfig.showOnlyValue
                    ? String.format("%.1f", voltage / 1000.0)
                    : String.format("%.1fV", voltage / 1000.0);
        }

        if (current != 0) {
            s.curText = BatteryStatsConfig.showOnlyValue
                    ? String.format("%s%d", sign, mA)
                    : String.format("%s%dmA", sign, mA);
        }

        if (voltage > 0 && mA > 0) {
            double powerW = (voltage / 1000.0) * (mA / 1000.0);
            s.powerText = BatteryStatsConfig.showOnlyValue
                    ? String.format("%.1f", powerW)
                    : String.format("%.1fW", powerW);
        }

        return s;
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

    private final Runnable tickRunnable = new Runnable() {
        @Override
        public void run() {
            if (!running) return;
            if (!bgBusy) {
                bgBusy = true;
                final Runnable self = this;
                bgHandler.post(() -> {
                    try {
                        BatterySnapshot snap = readBatterySnapshot();
                        String text = buildDisplayText(snap);
                        handler.post(() -> {
                            bgBusy = false;
                            if (!running) return;
                            applyDisplay(text);
                            handler.postDelayed(self, (long) (BatteryStatsConfig.updateInterval * 1000));
                        });
                    } catch (Exception e) {
                        bgBusy = false;
                        handler.postDelayed(self, (long) (BatteryStatsConfig.updateInterval * 1000));
                    }
                });
            } else {
                handler.postDelayed(this, (long) (BatteryStatsConfig.updateInterval * 1000));
            }
        }
    };

    private String buildDisplayText(BatterySnapshot snap) {
        StringBuilder sb = new StringBuilder();
        String[] order = BatteryStatsConfig.itemOrder.split(",");
        for (String id : order) {
            String part = buildItemPart(id, snap);
            if (part == null) continue;
            if (sb.length() > 0) sb.append(" | ");
            sb.append(part);
        }
        return sb.length() > 0 ? sb.toString() : "N/A";
    }

    private void applyDisplay(String text) {
        if (view == null) return;
        if (text.equals(lastRenderedText)) return;
        lastRenderedText = text;
        view.setTextColor(BatteryStatsConfig.color);
        if (BatteryStatsConfig.showOnlyValue) {
            view.setText(text);
            return;
        }
        SpannableString spannable = new SpannableString(text);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\u00B0' || c == 'C' || c == '%' || c == 'm' || c == 'V' || c == 'A' || c == 'W') {
                spannable.setSpan(new ForegroundColorSpan(BatteryStatsConfig.labelColor),
                        i, i + 1, 0);
            } else if (c == '|') {
                spannable.setSpan(new ForegroundColorSpan(BatteryStatsConfig.separatorColor),
                        i, i + 1, 0);
            }
        }
        view.setText(spannable);
    }
}
