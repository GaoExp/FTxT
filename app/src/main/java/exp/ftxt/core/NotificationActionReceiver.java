package exp.ftxt.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationActionReceiver extends BroadcastReceiver {

    public static final String ACTION_TOGGLE_OVERLAY = "exp.ftxt.ACTION_TOGGLE_OVERLAY";
    public static final String ACTION_KILL_SERVICE = "exp.ftxt.ACTION_KILL_SERVICE";
    public static final String ACTION_OPEN_APP = "exp.ftxt.ACTION_OPEN_APP";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;

        switch (intent.getAction()) {
            case ACTION_TOGGLE_OVERLAY:
                handleToggleOverlay(context);
                break;
            case ACTION_KILL_SERVICE:
                handleKillService(context);
                break;
            case ACTION_OPEN_APP:
                handleOpenApp(context);
                break;
        }
    }

    private void handleToggleOverlay(Context context) {
        if (FloatingService.instance == null) return;

        boolean allHidden = FloatingService.areAllOverlaysHidden();

        if (allHidden) {
            FloatingService.showAllOverlays();
        } else {
            FloatingService.hideAllOverlays();
        }

        FloatingService.updateNotification();
    }

    private void handleKillService(Context context) {
        FloatingService.stopAllModules();
        context.stopService(new Intent(context, FloatingService.class));
    }

    private void handleOpenApp(Context context) {
        Intent launchIntent = context.getPackageManager()
                .getLaunchIntentForPackage(context.getPackageName());
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(launchIntent);
        }
    }
}
