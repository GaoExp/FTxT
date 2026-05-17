package exp.ftxt;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class FloatingService extends Service {

    private WindowManager windowManager;

    private TextView floatingView;

    private WindowManager.LayoutParams params;

    private SharedPreferences prefs;

    public static FloatingService instance;

    private void updateTouchFlags(){

        if(MainActivity
                .isTouchPassthrough){

            params.flags |=
                    WindowManager
                    .LayoutParams
                    .FLAG_NOT_TOUCHABLE;

            floatingView
            .setOnTouchListener(null);

        }else{

            params.flags &=
                    ~WindowManager
                    .LayoutParams
                    .FLAG_NOT_TOUCHABLE;

            floatingView
            .setOnTouchListener(
                    touchListener
            );

        }

        if(windowManager != null
                && floatingView != null){

            try{

                windowManager
                .updateViewLayout(
                        floatingView,
                        params
                );

            }catch(Exception e){

                e.printStackTrace();

            }

        }

    }

    public static void updateTextColorStatic(){

        if(instance != null
                && instance.floatingView != null){

            instance.floatingView
            .setTextColor(
                    MainActivity.currentColor
            );

        }

    }

    public static void updateTextStatic(){

        if(instance != null
                && instance.floatingView != null){

            instance.floatingView
            .setText(
                    MainActivity.currentText
            );

        }

    }

    public static void updateTextSizeStatic(){

        if(instance != null
                && instance.floatingView != null){

            instance.floatingView
            .setTextSize(
                    MainActivity.currentSize
            );

        }

    }

    public static void updateTouchFlagsStatic(){

        if(instance != null){

            instance.updateTouchFlags();

        }

    }

    private void savePosition(){

        prefs.edit()
        .putInt("text_x", params.x)
        .putInt("text_y", params.y)
        .apply();

    }

    private void loadPosition(){

        params.x = prefs.getInt(
                "text_x",
                100
        );

        params.y = prefs.getInt(
                "text_y",
                300
        );

    }

    private View.OnTouchListener touchListener =
            new View.OnTouchListener(){

        int startX;
        int startY;

        float touchX;
        float touchY;

        @Override
        public boolean onTouch(
                View v,
                MotionEvent event){

            switch(
                    event.getAction()
            ){

                case MotionEvent
                        .ACTION_DOWN:

                    startX = params.x;
                    startY = params.y;

                    touchX =
                            event
                            .getRawX();

                    touchY =
                            event
                            .getRawY();

                    return true;

                case MotionEvent
                        .ACTION_MOVE:

                    params.x =
                            startX
                            +
                            (int)(
                            event
                            .getRawX()
                            - touchX
                            );

                    params.y =
                            startY
                            +
                            (int)(
                            event
                            .getRawY()
                            - touchY
                            );

                    windowManager
                    .updateViewLayout(
                            floatingView,
                            params
                    );

                    return true;

                case MotionEvent
                        .ACTION_UP:

                    savePosition();

                    return true;
            }

            return false;
        }
    };

    @Override
    public void onCreate() {

        super.onCreate();

        instance = this;

        prefs = getSharedPreferences(
                "ftxt_prefs",
                MODE_PRIVATE
        );

        createNotificationChannel();
        Notification notification =
                new NotificationCompat
                .Builder(
                        this,
                        "ftxt_overlay"
                )
                .setContentTitle(
                        "FTxT"
                )
                .setContentText(
                        "Overlay sedang aktif"
                )
                .setSmallIcon(
                        android.R.drawable
                        .ic_dialog_info
                )
                .setOngoing(true)
                .build();

        try{
            startForeground(1, notification);
        }catch(Exception e){
            e.printStackTrace();
        }

        try {

            windowManager =
                    (WindowManager)
                    getSystemService(
                            WINDOW_SERVICE
                    );

            floatingView =
                    new TextView(this);

            floatingView.setText(
                    MainActivity.currentText
            );

            floatingView.setTextSize(
                    MainActivity.currentSize
            );

            floatingView.setTextColor(
                    MainActivity.currentColor
            );

            floatingView.setPadding(
                    25,
                    20,
                    25,
                    20
            );

            params =
                    new WindowManager
                    .LayoutParams(

                    WindowManager
                    .LayoutParams
                    .WRAP_CONTENT,

                    WindowManager
                    .LayoutParams
                    .WRAP_CONTENT,

                    WindowManager
                    .LayoutParams
                    .TYPE_APPLICATION_OVERLAY,

                    WindowManager
                    .LayoutParams
                    .FLAG_NOT_FOCUSABLE,

                    PixelFormat
                    .TRANSLUCENT
            );

            params.gravity =
                    Gravity.TOP
                    | Gravity.START;

            loadPosition();

            updateTouchFlags();

            windowManager.addView(
                    floatingView,
                    params
            );

        }

        catch(Exception e){

            e.printStackTrace();

            stopSelf();

        }

    }

    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT
                >= Build.VERSION_CODES.O){
            NotificationChannel channel =
                    new NotificationChannel(
                    "ftxt_overlay",
                    "FTxT Overlay",
                    NotificationManager
                    .IMPORTANCE_LOW
            );
            channel
            .setDescription(
                    "Notifikasi overlay FTxT"
            );
            NotificationManager manager =
                    getSystemService(
                    NotificationManager.class
            );
            manager.createNotificationChannel(
                    channel
            );
        }
    }

    @Override
    public void onDestroy(){

        super.onDestroy();

        savePosition();

        instance = null;

        try{

            if(floatingView != null){

                windowManager
                .removeView(
                        floatingView
                );

            }

        }

        catch(Exception e){

            e.printStackTrace();

        }

    }

    @Override
    public IBinder onBind(
            Intent intent){

        return null;

    }

}