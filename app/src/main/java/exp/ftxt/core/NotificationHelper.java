package exp.ftxt.core;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import exp.ftxt.R;
import exp.ftxt.features.battery_bar.BatteryBarConfig;
import exp.ftxt.features.battery_stats.BatteryMonitor;
import exp.ftxt.features.battery_stats.BatteryReading;
import exp.ftxt.features.battery_stats.BatteryStatsConfig;
import exp.ftxt.features.clock_module.ClockConfig;
import exp.ftxt.features.crosshair.CrosshairConfig;
import exp.ftxt.features.fps_display.FpsConfig;
import exp.ftxt.features.network_stats.NetworkConfig;
import exp.ftxt.features.memory_stats.MemoryConfig;

public class NotificationHelper {

    public static final String CHANNEL_ID = "ftxt_overlay";
    public static final int NOTIFICATION_ID = 1;
    public static final String PREF_STATUS_BAR_MODE = "status_bar_mode";
    public static final String PREF_STATUS_BAR_SCALE = "status_bar_scale";
    public static final String PREF_STATUS_BAR_SCALE_TEMP = "status_bar_scale_temp";
    public static final String PREF_STATUS_BAR_SCALE_DATE = "status_bar_scale_date";
    public static final String PREF_STATUS_BAR_DATE_FORMAT = "status_bar_date_format";
    public static final String PREF_STATUS_BAR_DATE_LANG = "status_bar_date_lang";
    public static final String PREF_STATUS_BAR_DATE_TOP = "status_bar_date_top_size";
    public static final String PREF_STATUS_BAR_DATE_BOTTOM = "status_bar_date_bottom_size";
    public static final String PREF_STATUS_BAR_PERCENT_RING = "status_bar_percent_ring_size";
    public static final String PREF_STATUS_BAR_PERCENT_NUM = "status_bar_percent_num_size";

    private static Handler iconHandler;
    private static boolean iconCycling = false;

    private static final IntentFilter BATTERY_FILTER = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
    private static Bitmap cachedIconBitmap;
    private static String cachedIconText;
    private static RemoteViews cachedContentView;
    private static String lastSignature;

    private static Bitmap generateIcon(String top, String bottom, float scale,
                                       float topScale, float bottomScale) {
        String cacheKey = (bottom == null || bottom.isEmpty()) ? top : top + "\n" + bottom;
        cacheKey += "@" + String.format(java.util.Locale.US, "%.2f", scale)
                + "t" + String.format(java.util.Locale.US, "%.2f", topScale)
                + "b" + String.format(java.util.Locale.US, "%.2f", bottomScale);
        if (cachedIconBitmap != null && cacheKey.equals(cachedIconText)) {
            return cachedIconBitmap;
        }
        int size = 96;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        bitmap.setDensity(android.util.DisplayMetrics.DENSITY_XXXHIGH);
        Canvas canvas = new Canvas(bitmap);

        Paint topPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        topPaint.setColor(Color.WHITE);
        topPaint.setTypeface(Typeface.DEFAULT_BOLD);
        topPaint.setTextAlign(Paint.Align.CENTER);

        if (bottom == null || bottom.isEmpty()) {
            topPaint.setTextSize(68f * scale * topScale);
            Paint.FontMetrics fm = topPaint.getFontMetrics();
            float y = size / 2f - (fm.ascent + fm.descent) / 2f;
            canvas.drawText(top, size / 2f, y, topPaint);
        } else {
            Paint bottomPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            bottomPaint.setColor(Color.WHITE);
            bottomPaint.setTypeface(Typeface.DEFAULT_BOLD);
            bottomPaint.setTextAlign(Paint.Align.CENTER);

            float gap = 6f * scale;
            topPaint.setTextSize(50f * scale * topScale);
            bottomPaint.setTextSize(30f * scale * bottomScale);

            Rect topBounds = new Rect();
            topPaint.getTextBounds(top, 0, top.length(), topBounds);
            Rect bottomBounds = new Rect();
            bottomPaint.getTextBounds(bottom, 0, bottom.length(), bottomBounds);

            float topH = topBounds.height();
            float bottomH = bottomBounds.height();
            float centerY = size / 2f;
            float blockTop = centerY - (topH + bottomH + gap) / 2f;
            float yTop = blockTop - topBounds.top;
            canvas.drawText(top, size / 2f, yTop, topPaint);

            float yBottom = blockTop + topH + gap - bottomBounds.top;
            canvas.drawText(bottom, size / 2f, yBottom, bottomPaint);
        }

        cachedIconText = cacheKey;
        cachedIconBitmap = bitmap;
        return bitmap;
    }

    private static Bitmap generatePercentIcon(String percent, float scale,
                                              float ringScale, float numScale) {
        String cacheKey = String.format(java.util.Locale.US, "%s%%@%.2f r%.2f n%.2f",
                percent, scale, ringScale, numScale);
        if (cachedIconBitmap != null && cacheKey.equals(cachedIconText)) {
            return cachedIconBitmap;
        }
        int size = 96;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        bitmap.setDensity(android.util.DisplayMetrics.DENSITY_XXXHIGH);
        Canvas canvas = new Canvas(bitmap);

        float cx = size / 2f;
        float cy = size / 2f;

        int p = 0;
        try {
            p = Integer.parseInt(percent.replace("%", "").trim());
        } catch (Exception ignored) {
        }
        p = Math.max(0, Math.min(100, p));

        float stroke = 10f * scale * ringScale;
        Paint track = new Paint(Paint.ANTI_ALIAS_FLAG);
        track.setStyle(Paint.Style.STROKE);
        track.setStrokeWidth(stroke);
        track.setColor(0x33FFFFFF);
        float radius = size * 0.41f * scale;
        canvas.drawCircle(cx, cy, radius, track);

        Paint progress = new Paint(Paint.ANTI_ALIAS_FLAG);
        progress.setStyle(Paint.Style.STROKE);
        progress.setStrokeWidth(stroke);
        progress.setStrokeCap(Paint.Cap.ROUND);
        progress.setColor(Color.WHITE);
        RectF arc = new RectF(cx - radius, cy - radius, cx + radius, cy + radius);
        canvas.drawArc(arc, -90, p * 3.6f, false, progress);

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(42f * scale * numScale);
        String label = percent.replace("%", "");
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        canvas.drawText(label, cx, cy - (fm.ascent + fm.descent) / 2f, textPaint);

        cachedIconText = cacheKey;
        cachedIconBitmap = bitmap;
        return bitmap;
    }

    private static String getBatteryTemp(Context context) {
        try {
            Intent intent = context.registerReceiver(null, BATTERY_FILTER);
            if (intent == null) return "--";
            int temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
            return String.valueOf(Math.round(temp / 10f));
        } catch (Exception e) {
            return "--";
        }
    }

    private static String getBatteryPercent(Context context) {
        try {
            Intent intent = context.registerReceiver(null, BATTERY_FILTER);
            if (intent == null) return "--";
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            if (level < 0 || scale <= 0) return "--";
            return String.valueOf(Math.round(level * 100f / scale));
        } catch (Exception e) {
            return "--";
        }
    }

    public static String getStatusBarMode(Context context) {
        return context.getSharedPreferences("ftxt_prefs", Context.MODE_PRIVATE)
                .getString(PREF_STATUS_BAR_MODE, "temp");
    }

    public static String getStatusBarScalePref(String mode) {
        if ("date".equals(mode)) return PREF_STATUS_BAR_SCALE_DATE;
        if ("temp".equals(mode)) return PREF_STATUS_BAR_SCALE_TEMP;
        if ("date_top".equals(mode)) return PREF_STATUS_BAR_DATE_TOP;
        if ("date_bottom".equals(mode)) return PREF_STATUS_BAR_DATE_BOTTOM;
        if ("percent_ring".equals(mode)) return PREF_STATUS_BAR_PERCENT_RING;
        if ("percent_num".equals(mode)) return PREF_STATUS_BAR_PERCENT_NUM;
        return PREF_STATUS_BAR_SCALE;
    }

    public static float getStatusBarScale(Context context, String mode) {
        float scale = context.getSharedPreferences("ftxt_prefs", Context.MODE_PRIVATE)
                .getFloat(getStatusBarScalePref(mode), 1f);
        return Math.max(0.6f, Math.min(1.6f, scale));
    }

    public static float getStatusBarDateTopScale(Context context) {
        return getStatusBarScale(context, "date_top");
    }

    public static float getStatusBarDateBottomScale(Context context) {
        return getStatusBarScale(context, "date_bottom");
    }

    public static float getStatusBarPercentRingScale(Context context) {
        return getStatusBarScale(context, "percent_ring");
    }

    public static float getStatusBarPercentNumScale(Context context) {
        return getStatusBarScale(context, "percent_num");
    }

    public static int getStatusBarDateFormat(Context context) {
        int fmt = context.getSharedPreferences("ftxt_prefs", Context.MODE_PRIVATE)
                .getInt(PREF_STATUS_BAR_DATE_FORMAT, 0);
        return Math.max(0, Math.min(7, fmt));
    }

    public static String getStatusBarDateLang(Context context) {
        String lang = context.getSharedPreferences("ftxt_prefs", Context.MODE_PRIVATE)
                .getString(PREF_STATUS_BAR_DATE_LANG, "eng");
        return "in".equals(lang) ? "in" : "eng";
    }

    private static String dateDayName(String lang, int dayOfWeek) {
        String[] en = {"", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        String[] id = {"", "Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab"};
        String[] arr = "in".equals(lang) ? id : en;
        if (dayOfWeek < 1 || dayOfWeek > 7) return "";
        return arr[dayOfWeek];
    }

    private static String dateMonthName(String lang, int month) {
        String[] en = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        String[] id = {"Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des"};
        String[] arr = "in".equals(lang) ? id : en;
        if (month < 1 || month > 12) return "";
        return arr[month - 1];
    }

    private static String[] buildDateIconLines(Calendar c, int format, String lang) {
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH) + 1;
        String d = String.valueOf(day);
        String dd = String.format(java.util.Locale.US, "%02d", day);
        String m = String.valueOf(month);
        String mm = String.format(java.util.Locale.US, "%02d", month);
        String dslash = d + "/" + m;
        String ddslash = dd + "/" + mm;
        String dayName = dateDayName(lang, c.get(Calendar.DAY_OF_WEEK));
        String monthName = dateMonthName(lang, month);
        switch (format) {
            case 1: return new String[]{dd, dayName};
            case 2: return new String[]{d, monthName};
            case 3: return new String[]{dd, monthName};
            case 4: return new String[]{dslash, dayName};
            case 5: return new String[]{ddslash, dayName};
            case 6: return new String[]{dayName, dslash};
            case 7: return new String[]{dayName, ddslash};
            case 0:
            default: return new String[]{d, dayName};
        }
    }

    private static String[] getStatusIconLines(Context context, String mode) {
        if ("date".equals(mode)) {
            Calendar c = Calendar.getInstance();
            return buildDateIconLines(c, getStatusBarDateFormat(context), getStatusBarDateLang(context));
        }
        if ("percent".equals(mode)) {
            return new String[]{getBatteryPercent(context) + "%", null};
        }
        return new String[]{getBatteryTemp(context) + "\u00B0", null};
    }

    public static Bitmap buildStatusIconBitmap(Context context, String mode) {
        float scale = getStatusBarScale(context, mode);
        if ("percent".equals(mode)) {
            return generatePercentIcon(getBatteryPercent(context) + "%", scale,
                    getStatusBarPercentRingScale(context), getStatusBarPercentNumScale(context));
        }
        String[] lines = getStatusIconLines(context, mode);
        if ("date".equals(mode)) {
            return generateIcon(lines[0], lines[1], scale,
                    getStatusBarDateTopScale(context), getStatusBarDateBottomScale(context));
        }
        return generateIcon(lines[0], lines[1], scale, 1f, 1f);
    }

    private static Notification buildNotificationDynamic(Context context) {
        String mode = getStatusBarMode(context);
        String[] iconLines = getStatusIconLines(context, mode);
        float scale = getStatusBarScale(context, mode);

        boolean allHidden = FloatingService.areAllOverlaysHidden();
        int toggleIcon = allHidden ? R.drawable.ic_notification_invisible : R.drawable.ic_notification_visible;

        String title = getSessionTitle(context);
        String activeModules = getActiveModulesText(context);

        Bitmap iconBitmap;
        if ("percent".equals(mode)) {
            iconBitmap = generatePercentIcon(getBatteryPercent(context) + "%", scale,
                    getStatusBarPercentRingScale(context), getStatusBarPercentNumScale(context));
        } else {
            iconBitmap = generateIcon(iconLines[0], iconLines[1], scale,
                    "date".equals(mode) ? getStatusBarDateTopScale(context) : 1f,
                    "date".equals(mode) ? getStatusBarDateBottomScale(context) : 1f);
        }

        String iconKey = iconLines[0] + "/" + (iconLines[1] == null ? "-" : iconLines[1]);
        String signature = title + "|" + toggleIcon + "|" + activeModules + "|" + mode + "|" + iconKey
                + "|" + scale + "|t" + getStatusBarScale(context, "date_top")
                + "|b" + getStatusBarScale(context, "date_bottom")
                + "|r" + getStatusBarScale(context, "percent_ring")
                + "|n" + getStatusBarScale(context, "percent_num");
        if (signature.equals(lastSignature)) return null;
        lastSignature = signature;

        RemoteViews contentView = buildContentView(context, toggleIcon, title);

        android.graphics.drawable.Icon smallIcon = android.graphics.drawable.Icon.createWithBitmap(iconBitmap);
        return new Notification.Builder(context, CHANNEL_ID)
                .setSmallIcon(smallIcon)
                .setContentTitle(title)
                .setContentText(activeModules)
                .setCustomContentView(contentView)
                .setStyle(new Notification.DecoratedCustomViewStyle())
                .setOngoing(true)
                .build();
    }

    private static String getSessionTitle(Context context) {
        BatteryReading.Snapshot s = BatteryMonitor.getLastSnapshot();
        if (s == null || s.time == 0) {
            return "FTxT " + getBatteryTemp(context) + "\u00B0C";
        }

        int currentMa = s.currentMa;
        float tempC = s.tempC;
        boolean charging = s.isCharging();

        String tempStr = String.valueOf(Math.round(tempC));
        StringBuilder sb = new StringBuilder();
        sb.append(s.percent).append("%");
        if (s.statusInt == android.os.BatteryManager.BATTERY_STATUS_FULL) {
            sb.append(" \u2705");
        } else if (charging) {
            sb.append(" \u26A1+").append(currentMa).append("mA");
        } else {
            sb.append(" \uD83D\uDD0B").append(currentMa).append("mA");
        }
        sb.append(" | ").append(String.format(java.util.Locale.US, "%.1f", s.voltageV)).append("V");
        if (s.powerW >= 0.1d) {
            sb.append(" | ").append(String.format(java.util.Locale.US, "%.1f", s.powerW)).append("W");
        }
        if (s.chargeMah > 0) {
            sb.append(" | ").append(s.chargeMah).append("mAh");
        }
        sb.append(" | ").append(tempStr).append("\u00B0C");
        return sb.toString();
    }

    private static void ensureCachedViews(Context context) {
        if (cachedContentView != null) return;

        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_custom);

        contentView.setImageViewResource(R.id.noti_close_btn, R.drawable.ic_close);
        contentView.setImageViewResource(R.id.noti_open_btn, R.drawable.ic_notification_open);

        Intent toggleIntent = new Intent(context, NotificationActionReceiver.class);
        toggleIntent.setAction(NotificationActionReceiver.ACTION_TOGGLE_OVERLAY);
        contentView.setOnClickPendingIntent(R.id.noti_toggle_btn, PendingIntent.getBroadcast(
                context, 0, toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));

        Intent killIntent = new Intent(context, NotificationActionReceiver.class);
        killIntent.setAction(NotificationActionReceiver.ACTION_KILL_SERVICE);
        contentView.setOnClickPendingIntent(R.id.noti_close_btn, PendingIntent.getBroadcast(
                context, 1, killIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));

        Intent openIntent = new Intent(context, NotificationActionReceiver.class);
        openIntent.setAction(NotificationActionReceiver.ACTION_OPEN_APP);
        contentView.setOnClickPendingIntent(R.id.noti_open_btn, PendingIntent.getBroadcast(
                context, 2, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));

        cachedContentView = contentView;
    }

    private static RemoteViews buildContentView(Context context, int toggleIcon, String title) {
        ensureCachedViews(context);
        RemoteViews contentView = cachedContentView.clone();
        contentView.setImageViewResource(R.id.noti_toggle_btn, toggleIcon);
        contentView.setTextViewText(R.id.noti_title, title);
        return contentView;
    }

    public static void startIconCycling(Context context) {
        if (iconCycling) return;
        iconCycling = true;
        lastSignature = null;
        iconHandler = new Handler(Looper.getMainLooper());

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification first = buildNotificationDynamic(context);
        if (first != null) nm.notify(NOTIFICATION_ID, first);

        iconHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!iconCycling) return;
                NotificationManager nm2 = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                Notification n = buildNotificationDynamic(context);
                if (n != null) nm2.notify(NOTIFICATION_ID, n);
                iconHandler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    public static void stopIconCycling() {
        iconCycling = false;
        lastSignature = null;
        if (iconHandler != null) {
            iconHandler.removeCallbacksAndMessages(null);
            iconHandler = null;
        }
    }

    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "FTxT Overlay", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Notifikasi overlay FTxT");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    public static Notification buildNotification(Context context) {
        boolean allHidden = FloatingService.areAllOverlaysHidden();
        int toggleIcon = allHidden ? R.drawable.ic_notification_invisible : R.drawable.ic_notification_visible;

        RemoteViews contentView = buildContentView(context, toggleIcon, getSessionTitle(context));

        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_toggle)
                .setCustomContentView(contentView)
                .setOngoing(true)
                .build();
    }

    public static void updateNotification(Context context) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (iconCycling) {
            Notification n = buildNotificationDynamic(context);
            if (n != null) nm.notify(NOTIFICATION_ID, n);
        } else {
            nm.notify(NOTIFICATION_ID, buildNotification(context));
        }
    }

    private static boolean isAnyModuleActive(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("ftxt_prefs", Context.MODE_PRIVATE);

        if (prefs.getBoolean("text_overlay_on", false)) return true;
        if (FpsConfig.enabled) return true;
        if (ClockConfig.enabled) return true;
        if (BatteryStatsConfig.enabled) return true;
        if (NetworkConfig.enabled) return true;
        if (BatteryBarConfig.enabled) return true;
        if (MemoryConfig.enabled) return true;
        if (MemoryConfig.backgroundMonitor) return true;
        if (CrosshairConfig.enabled) return true;

        return false;
    }

    private static String getActiveModulesText(Context context) {
        List<String> active = new ArrayList<>();

        SharedPreferences prefs = context.getSharedPreferences("ftxt_prefs", Context.MODE_PRIVATE);
        if (prefs.getBoolean("text_overlay_on", false)) active.add("Text");
        if (FpsConfig.enabled) active.add("FPS");
        if (ClockConfig.enabled) active.add("Clock");
        if (BatteryStatsConfig.enabled) active.add("Battery");
        if (NetworkConfig.enabled) active.add("Network");
        if (BatteryBarConfig.enabled) active.add("Bar");
        if (MemoryConfig.enabled || MemoryConfig.backgroundMonitor) active.add("Mem");
        if (CrosshairConfig.enabled) active.add("Xhair");

        if (active.isEmpty()) return "Tidak ada overlay aktif";
        return String.join(", ", active) + " aktif";
    }
}
