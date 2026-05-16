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
    private TextView textView;

    @Override
    public void onCreate() {
        super.onCreate();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        textView = new TextView(this);

        textView.setText("FTxT AKTIF");
        textView.setTextSize(18f);
        textView.setTextColor(Color.WHITE);
        textView.setBackgroundColor(Color.BLACK);

        int type;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            type = WindowManager.LayoutParams.TYPE_PHONE;
        }

        final WindowManager.LayoutParams params =
                new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        type,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT
                );

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 100;
        params.y = 300;

        textView.setOnTouchListener(new View.OnTouchListener() {

            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:

                        initialX = params.x;
                        initialY = params.y;

                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();

                        return true;

                    case MotionEvent.ACTION_MOVE:

                        params.x = initialX +
                                (int) (event.getRawX() - initialTouchX);

                        params.y = initialY +
                                (int) (event.getRawY() - initialTouchY);

                        windowManager.updateViewLayout(textView, params);

                        return true;
                }

                return false;
            }
        });

        windowManager.addView(textView, params);
    }

    @Override
    public void onDestroy() {

        super.onDestroy();

        if (textView != null) {
            windowManager.removeView(textView);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}