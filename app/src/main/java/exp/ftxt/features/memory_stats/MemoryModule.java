package exp.ftxt.features.memory_stats;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.WindowManager;

import exp.ftxt.shared.ui.OverlayDragHandler;
import exp.ftxt.shared.ui.OverlayModule;
import exp.ftxt.shared.ui.OverlayShadow;
import exp.ftxt.shared.ui.ShadowTextView;

public class MemoryModule implements OverlayModule {

    private ShadowTextView view;
    private WindowManager.LayoutParams params;
    private WindowManager wm;
    private Context context;
    private SharedPreferences prefs;
    private boolean running;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public static Runnable onPositionUpdate;
    private String orientationSuffix;

    private String lastRenderedText;

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
        lastRenderedText = null;
        view.setShadowConfig(MemoryConfig.shadow);
        view.setTextSize(MemoryConfig.size);
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
        params.x = (int)(MemoryConfig.posX * getScreenWidth());
        params.y = (int)(MemoryConfig.posY * getScreenHeight());

        OverlayShadow.apply(view, params, wm, MemoryConfig.shadow, 4f);
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
        MemoryConfig.size = size;
        if (view != null) view.setTextSize(size);
    }

    @Override
    public void updateColor(int color) {
        MemoryConfig.color = color;
        lastRenderedText = null;
        updateDisplay();
    }

    @Override
    public void updateLabelColor(int color) {
        MemoryConfig.labelColor = color;
        lastRenderedText = null;
        updateDisplay();
    }

    @Override
    public void updateSeparatorColor(int color) {
        MemoryConfig.separatorColor = color;
        lastRenderedText = null;
        updateDisplay();
    }

    @Override
    public void updateShadow() {
        if (view != null) view.setShadowConfig(MemoryConfig.shadow);
        OverlayShadow.apply(view, params, wm, MemoryConfig.shadow, 4f);
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
        MemoryConfig.posX = prefs.getFloat("mem_pos_x" + sfx, 0.05f);
        MemoryConfig.posY = prefs.getFloat("mem_pos_y" + sfx, 0.6f);
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
            params.x = (int)(MemoryConfig.posX * getScreenWidth());
            params.y = (int)(MemoryConfig.posY * getScreenHeight());
            if (MemoryConfig.safeArea && view.getWidth() > 0 && view.getHeight() > 0) {
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
        if (MemoryConfig.bg.enabled) {
            int pad = MemoryConfig.bg.padding;
            view.setPadding(pad, pad, pad, pad);
        } else {
            view.setPadding(0, 0, 0, 0);
        }
        view.setBgEnabled(MemoryConfig.bg.enabled);
        view.setBgColor(MemoryConfig.bg.color);
        view.setBgOffsetX(MemoryConfig.bg.offsetX);
        view.setBgOffsetY(MemoryConfig.bg.offsetY);
        view.setBgMargin(MemoryConfig.bg.margin);
        view.setBgRadius(MemoryConfig.bg.radius);
    }

    @Override
    public void updateTouchFlags() {
        if (params == null || view == null || wm == null) return;

        if (MemoryConfig.touchPassthrough) {
            params.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            view.setOnTouchListener(null);
        } else {
            params.flags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            view.setOnTouchListener(new OverlayDragHandler(params, wm,
                    null,
                    () -> {
                        if (MemoryConfig.safeArea && view != null && view.getWidth() > 0 && view.getHeight() > 0) {
                            params.x = Math.max(0, Math.min(params.x, getScreenWidth() - view.getWidth()));
                            params.y = Math.max(0, Math.min(params.y, getScreenHeight() - view.getHeight()));
                        }
                        if (params != null) {
                            MemoryConfig.posX = Math.max(0, Math.min(1, (float) params.x / getScreenWidth()));
                            MemoryConfig.posY = Math.max(0, Math.min(1, (float) params.y / getScreenHeight()));
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
        MemorySnapshot snap = readMemorySnapshot();
        StringBuilder sb = new StringBuilder();
        String[] order = MemoryConfig.itemOrder.split(",");
        for (String id : order) {
            String part = buildItemPart(id, snap);
            if (part == null) continue;
            if (sb.length() > 0) sb.append(" | ");
            sb.append(part);
        }
        String text = sb.length() > 0 ? sb.toString() : "N/A";
        if (text.equals(lastRenderedText)) return;
        lastRenderedText = text;
        view.setTextColor(MemoryConfig.color);
        if (MemoryConfig.showOnlyValue) {
            view.setText(text);
            return;
        }
        SpannableString spannable = new SpannableString(text);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == 'M' || c == 'B' || c == 'J' || c == 'N' || c == 'G' || c == 'T') {
                spannable.setSpan(new ForegroundColorSpan(MemoryConfig.labelColor),
                        i, i + 1, 0);
            } else if (c == '|') {
                spannable.setSpan(new ForegroundColorSpan(MemoryConfig.separatorColor),
                        i, i + 1, 0);
            }
        }
        view.setText(spannable);
    }

    private static class MemorySnapshot {
        String javaText;
        String nativeText;
        String graphicsText;
        String totalText;
    }

    private String buildItemPart(String id, MemorySnapshot s) {
        if ("java".equals(id) && MemoryConfig.showJavaHeap && s.javaText != null)
            return MemoryConfig.showOnlyValue ? s.javaText : "J " + s.javaText;
        if ("native".equals(id) && MemoryConfig.showNativeHeap && s.nativeText != null)
            return MemoryConfig.showOnlyValue ? s.nativeText : "N " + s.nativeText;
        if ("graphics".equals(id) && MemoryConfig.showGraphics && s.graphicsText != null)
            return MemoryConfig.showOnlyValue ? s.graphicsText : "G " + s.graphicsText;
        if ("total".equals(id) && MemoryConfig.showTotal && s.totalText != null)
            return MemoryConfig.showOnlyValue ? s.totalText : "T " + s.totalText;
        return null;
    }

    private MemorySnapshot readMemorySnapshot() {
        MemorySnapshot s = new MemorySnapshot();
        try {
            Debug.MemoryInfo info = new Debug.MemoryInfo();
            Debug.getMemoryInfo(info);
            s.javaText = formatMb(info.dalvikPss);
            s.nativeText = formatMb(info.nativePss);
            s.graphicsText = formatMb(graphicsPssKb(info));
            s.totalText = formatMb(info.getTotalPss());
        } catch (Exception ignored) {}
        return s;
    }

    private String formatMb(int kb) {
        return String.format(java.util.Locale.US, "%.1fMB", kb / 1024f);
    }

    private int graphicsPssKb(Debug.MemoryInfo info) {
        String stat = info.getMemoryStat("summary.graphics");
        if (stat == null) return 0;
        try {
            int end = stat.indexOf(' ');
            if (end > 0) {
                return Integer.parseInt(stat.substring(0, end));
            }
            return Integer.parseInt(stat.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private final Runnable tickRunnable = new Runnable() {
        @Override
        public void run() {
            if (!running) return;
            updateDisplay();
            handler.postDelayed(this, (long)(MemoryConfig.updateInterval * 1000));
        }
    };

}
