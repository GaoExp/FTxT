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
import android.os.PowerManager;

import androidx.core.app.NotificationCompat;

import android.view.Choreographer;
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

    private PowerManager.WakeLock wakeLock;

    public static FloatingService instance;

    private boolean shadowEnabled = false;

    // FPS
    private static TextView fpsView;
    private static WindowManager.LayoutParams fpsParams;
    private static boolean fpsInitialized = false;
    private static boolean fpsRunning = false;
    private Choreographer choreographer;
    private long fpsLastFrameTime = 0;
    private int fpsFrameCount = 0;
    private float fpsValue = 0;

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

    public static void updateShadowStatic(){

        if(instance != null){

            instance.readShadowPrefAndApply();

        }

    }

    // === FPS Static Methods ===

    public static void startFpsStatic(){
        if(instance != null){
            instance.startFps();
        }
    }

    public static void stopFpsStatic(){
        if(instance != null){
            instance.stopFps();
        }
    }

    public static void updateFpsColorStatic(){
        if(fpsView != null){
            fpsView.setTextColor(MainActivity.fpsColor);
        }
    }

    public static void updateFpsSizeStatic(){
        if(fpsView != null){
            fpsView.setTextSize(MainActivity.fpsSize);
        }
    }

    public static void updateFpsShadowStatic(){
        if(instance != null){
            instance.applyFpsShadow();
        }
    }

    // === FPS ===

    private void startFps(){
        if(!fpsInitialized){
            createFpsOverlay();
        }
        if(!fpsRunning && fpsView != null){
            fpsRunning = true;
            fpsLastFrameTime = 0;
            fpsFrameCount = 0;
            choreographer = Choreographer.getInstance();
            choreographer.postFrameCallback(fpsFrameCallback);
        }
    }

    private void stopFps(){
        fpsRunning = false;
        removeFpsOverlay();
    }

    private void createFpsOverlay(){
        if(windowManager == null) return;

        fpsView = new TextView(this);
        fpsView.setText("0.0 FPS");
        fpsView.setTextSize(MainActivity.fpsSize);
        fpsView.setTextColor(MainActivity.fpsColor);
        fpsView.setPadding(10, 8, 10, 8);

        applyFpsShadow();

        fpsParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        fpsParams.gravity = Gravity.TOP | Gravity.START;
        fpsParams.x = 0;
        fpsParams.y = 0;

        try{
            windowManager.addView(fpsView, fpsParams);
            fpsInitialized = true;
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void removeFpsOverlay(){
        if(fpsView != null && windowManager != null){
            try{
                windowManager.removeView(fpsView);
            }catch(Exception e){
                e.printStackTrace();
            }
            fpsView = null;
            fpsInitialized = false;
        }
    }

    private void applyFpsShadow(){
        if(fpsView == null) return;
        if(MainActivity.fpsShadow){
            fpsView.setBackgroundColor(0x88000000);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                fpsView.setElevation(4f);
            }
        }else{
            fpsView.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                fpsView.setElevation(0f);
            }
        }
        if(windowManager != null && fpsView != null){
            try{
                windowManager.updateViewLayout(fpsView, fpsParams);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    private Choreographer.FrameCallback fpsFrameCallback =
            new Choreographer.FrameCallback(){
        @Override
        public void doFrame(long frameTimeNanos){
            if(fpsLastFrameTime == 0){
                fpsLastFrameTime = frameTimeNanos;
            }
            fpsFrameCount++;
            long elapsed = (frameTimeNanos - fpsLastFrameTime) / 1000000;
            if(elapsed >= 1000){
                fpsValue = (float) fpsFrameCount * 1000 / elapsed;
                fpsFrameCount = 0;
                fpsLastFrameTime = frameTimeNanos;
                if(fpsView != null){
                    fpsView.setText(String.format("%.1f FPS", fpsValue));
                }
            }
            if(fpsRunning && MainActivity.fpsEnabled){
                choreographer.postFrameCallback(fpsFrameCallback);
            }
        }
    };

    private void readShadowPrefAndApply(){

        shadowEnabled = prefs.getBoolean("shadow_enabled", false);
        applyShadow();

    }

    private void applyShadow(){

        if(floatingView == null) return;

        if(shadowEnabled){
            int bgColor = 0x88000000;
            floatingView.setBackgroundColor(bgColor);
            if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP){
                floatingView.setElevation(8f);
            }
        }else{
            floatingView.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP){
                floatingView.setElevation(0f);
            }
        }

        if(windowManager != null && floatingView != null){
            try{
                windowManager.updateViewLayout(floatingView, params);
            }catch(Exception e){
                e.printStackTrace();
            }
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

        shadowEnabled = prefs.getBoolean("shadow_enabled", false);

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

            applyShadow();

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

            PowerManager pm =
                    (PowerManager)
                    getSystemService(
                            POWER_SERVICE
                    );

            wakeLock = pm.newWakeLock(
                    PowerManager
                    .PARTIAL_WAKE_LOCK,
                    "FTxT:OverlayWakeLock"
            );

            wakeLock.acquire();

        }

        catch(Exception e){

            e.printStackTrace();

            stopSelf();

        }

        // start FPS if enabled
        if(MainActivity.fpsEnabled){
            startFps();
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

        stopFps();

        if(wakeLock != null
                && wakeLock.isHeld()){

            wakeLock.release();

        }

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
