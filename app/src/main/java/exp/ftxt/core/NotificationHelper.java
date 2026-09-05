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
import android.graphics.Typeface;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
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

    private static Handler iconHandler;
    private static boolean iconCycling = false;

    private static final IntentFilter BATTERY_FILTER = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
    private static Bitmap cachedIconBitmap;
    private static String cachedIconText;
    private static RemoteViews cachedContentView;

    private static Bitmap generateIcon(String text) {
        if (cachedIconBitmap != null && text.equals(cachedIconText)) {
            return cachedIconBitmap;
        }
        int size = 96;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        bitmap.setDensity(android.util.DisplayMetrics.DENSITY_XXXHIGH);
        Canvas canvas = new Canvas(bitmap);

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(68);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float y = size / 2f - (fm.ascent + fm.descent) / 2f;
        canvas.drawText(text, size / 2f, y, textPaint);

        cachedIconText = text;
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

    private static Notification buildNotificationDynamic(Context context) {
        String tempValue = getBatteryTemp(context);
        Bitmap iconBitmap = generateIcon(tempValue + "\u00B0");

        boolean allHidden = FloatingService.areAllOverlaysHidden();
        int toggleIcon = allHidden ? R.drawable.ic_notification_invisible : R.drawable.ic_notification_visible;

        String title = getSessionTitle(context);
        RemoteViews contentView = buildContentView(context, toggleIcon, title);

        android.graphics.drawable.Icon smallIcon = android.graphics.drawable.Icon.createWithBitmap(iconBitmap);
        return new Notification.Builder(context, CHANNEL_ID)
                .setSmallIcon(smallIcon)
                .setContentTitle(title)
                .setContentText(getActiveModulesText(context))
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
        iconHandler = new Handler(Looper.getMainLooper());

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(NOTIFICATION_ID, buildNotificationDynamic(context));

        iconHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!iconCycling) return;
                NotificationManager nm2 = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                nm2.notify(NOTIFICATION_ID, buildNotificationDynamic(context));
                iconHandler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    public static void stopIconCycling() {
        iconCycling = false;
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
        if (iconCycling) {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify(NOTIFICATION_ID, buildNotificationDynamic(context));
        } else {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
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
