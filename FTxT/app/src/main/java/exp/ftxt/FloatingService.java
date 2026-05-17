package exp.ftxt;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class FloatingService extends Service {

    private WindowManager windowManager;

    private TextView floatingView;

    private WindowManager.LayoutParams params;

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
            }

            return false;
        }
    };

    @Override
    public void onCreate() {

        super.onCreate();

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

            int overlayType;

            if(Build.VERSION.SDK_INT
                    >= Build.VERSION_CODES.O){

                overlayType =
                        WindowManager
                        .LayoutParams
                        .TYPE_APPLICATION_OVERLAY;

            }else{

                overlayType =
                        WindowManager
                        .LayoutParams
                        .TYPE_PHONE;
            }

            params =
                    new WindowManager
                    .LayoutParams(

                    WindowManager
                    .LayoutParams
                    .WRAP_CONTENT,

                    WindowManager
                    .LayoutParams
                    .WRAP_CONTENT,

                    overlayType,

                    WindowManager
                    .LayoutParams
                    .FLAG_NOT_FOCUSABLE,

                    PixelFormat
                    .TRANSLUCENT
            );

            params.gravity =
                    Gravity.TOP
                    | Gravity.START;

            params.x = 100;
            params.y = 300;

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

    @Override
    public void onDestroy(){

        super.onDestroy();

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