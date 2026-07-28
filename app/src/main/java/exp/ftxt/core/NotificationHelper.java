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
import exp.ftxt.features.battery_current.BatteryCurrentConfig;
import exp.ftxt.features.battery_percentage.BatteryPercentageConfig;
import exp.ftxt.features.battery_temperature.BatteryConfig;
import exp.ftxt.features.clock_module.ClockConfig;
import exp.ftxt.features.fps_display.FpsConfig;
import exp.ftxt.features.network_stats.NetworkConfig;

public class NotificationHelper {

    public static final String CHANNEL_ID = "ftxt_overlay";
    public static final int NOTIFICATION_ID = 1;

    private static Handler iconHandler;
    private static boolean iconCycling = false;

    private static Bitmap generateIcon(String text) {
        int size = 192;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        bitmap.setDensity(android.util.DisplayMetrics.DENSITY_XXXHIGH);
        Canvas canvas = new Canvas(bitmap);

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(140);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float y = size / 2f - (fm.ascent + fm.descent) / 2f;
        canvas.drawText(text, size / 2f, y, textPaint);

        return bitmap;
    }

    private static String getBatteryTemp(Context context) {
        try {
            Intent intent = context.registerReceiver(null,
                    new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
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

        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_custom);
        contentView.setTextViewText(R.id.noti_title, "FTxT  " + tempValue + "\u00B0C");

        Intent toggleIntent = new Intent(context, NotificationActionReceiver.class);
        toggleIntent.setAction(NotificationActionReceiver.ACTION_TOGGLE_OVERLAY);
        PendingIntent togglePending = PendingIntent.getBroadcast(context, 0, toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        contentView.setImageViewResource(R.id.noti_toggle_btn, toggleIcon);
        contentView.setOnClickPendingIntent(R.id.noti_toggle_btn, togglePending);

        Intent killIntent = new Intent(context, NotificationActionReceiver.class);
        killIntent.setAction(NotificationActionReceiver.ACTION_KILL_SERVICE);
        PendingIntent killPending = PendingIntent.getBroadcast(context, 1, killIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        contentView.setImageViewResource(R.id.noti_close_btn, R.drawable.ic_close);
        contentView.setOnClickPendingIntent(R.id.noti_close_btn, killPending);

        Intent openIntent = new Intent(context, NotificationActionReceiver.class);
        openIntent.setAction(NotificationActionReceiver.ACTION_OPEN_APP);
        PendingIntent openPending = PendingIntent.getBroadcast(context, 2, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        contentView.setImageViewResource(R.id.noti_open_btn, R.drawable.ic_notification_open);
        contentView.setOnClickPendingIntent(R.id.noti_open_btn, openPending);

        android.graphics.drawable.Icon smallIcon = android.graphics.drawable.Icon.createWithBitmap(iconBitmap);
        return new Notification.Builder(context, CHANNEL_ID)
                .setSmallIcon(smallIcon)
                .setContentTitle("FTxT " + tempValue + "\u00B0C")
                .setContentText(getActiveModulesText(context))
                .setCustomContentView(contentView)
                .setCustomBigContentView(contentView)
                .setStyle(new Notification.DecoratedCustomViewStyle())
                .setOngoing(true)
                .build();
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
                iconHandler.postDelayed(this, 10000);
            }
        }, 10000);
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

        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_custom);

        Intent toggleIntent = new Intent(context, NotificationActionReceiver.class);
        toggleIntent.setAction(NotificationActionReceiver.ACTION_TOGGLE_OVERLAY);
        PendingIntent togglePending = PendingIntent.getBroadcast(
                context, 0, toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent killIntent = new Intent(context, NotificationActionReceiver.class);
        killIntent.setAction(NotificationActionReceiver.ACTION_KILL_SERVICE);
        PendingIntent killPending = PendingIntent.getBroadcast(
                context, 1, killIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent openIntent = new Intent(context, NotificationActionReceiver.class);
        openIntent.setAction(NotificationActionReceiver.ACTION_OPEN_APP);
        PendingIntent openPending = PendingIntent.getBroadcast(
                context, 2, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        contentView.setImageViewResource(R.id.noti_toggle_btn, toggleIcon);
        contentView.setOnClickPendingIntent(R.id.noti_toggle_btn, togglePending);

        contentView.setImageViewResource(R.id.noti_close_btn, R.drawable.ic_close);
        contentView.setOnClickPendingIntent(R.id.noti_close_btn, killPending);

        contentView.setImageViewResource(R.id.noti_open_btn, R.drawable.ic_notification_open);
        contentView.setOnClickPendingIntent(R.id.noti_open_btn, openPending);

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
        if (BatteryConfig.enabled) return true;
        if (BatteryPercentageConfig.enabled) return true;
        if (BatteryCurrentConfig.enabled) return true;
        if (NetworkConfig.enabled) return true;

        return false;
    }

    private static String getActiveModulesText(Context context) {
        List<String> active = new ArrayList<>();

        SharedPreferences prefs = context.getSharedPreferences("ftxt_prefs", Context.MODE_PRIVATE);
        if (prefs.getBoolean("text_overlay_on", false)) active.add("Text");
        if (FpsConfig.enabled) active.add("FPS");
        if (ClockConfig.enabled) active.add("Clock");
        if (BatteryConfig.enabled) active.add("Battery");
        if (BatteryPercentageConfig.enabled) active.add("Battery%");
        if (BatteryCurrentConfig.enabled) active.add("Current");
        if (NetworkConfig.enabled) active.add("Network");

        if (active.isEmpty()) return "Tidak ada overlay aktif";
        return String.join(", ", active) + " aktif";
    }
}
