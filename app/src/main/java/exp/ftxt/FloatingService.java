package exp.ftxt;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
    private TextView floatingText;
    private WindowManager.LayoutParams params;
    private BroadcastReceiver textUpdateReceiver;

    @Override
    public void onCreate() {
        super.onCreate();

        try {

            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

            floatingText = new TextView(this);

            floatingText.setText(MainActivity.currentText);
            floatingText.setTextSize(MainActivity.currentTextSize);
            floatingText.setTextColor(MainActivity.currentTextColor);
            floatingText.setBackgroundColor(Color.BLACK);

            int layoutType;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                layoutType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                layoutType = WindowManager.LayoutParams.TYPE_PHONE;
            }

            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    layoutType,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
            );

            params.gravity = Gravity.TOP | Gravity.START;
            params.x = 100;
            params.y = 300;

            floatingText.setOnTouchListener(new View.OnTouchListener() {

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

                            windowManager.updateViewLayout(floatingText, params);

                            return true;
                    }

                    return false;
                }
            });

            windowManager.addView(floatingText, params);

            // Register broadcast receiver untuk update teks, ukuran, dan warna
            textUpdateReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction().equals("exp.ftxt.UPDATE_TEXT")) {
                        String newText = intent.getStringExtra("text");
                        if (newText != null && floatingText != null) {
                            floatingText.setText(newText);
                        }
                    } else if (intent.getAction().equals("exp.ftxt.UPDATE_TEXT_SIZE")) {
                        float newSize = intent.getFloatExtra("size", 20f);
                        if (floatingText != null) {
                            floatingText.setTextSize(newSize);
                        }
                    } else if (intent.getAction().equals("exp.ftxt.UPDATE_TEXT_COLOR")) {
                        int newColor = intent.getIntExtra("color", Color.WHITE);
                        if (floatingText != null) {
                            floatingText.setTextColor(newColor);
                        }
                    }
                }
            };

            IntentFilter filter = new IntentFilter();
            filter.addAction("exp.ftxt.UPDATE_TEXT");
            filter.addAction("exp.ftxt.UPDATE_TEXT_SIZE");
            filter.addAction("exp.ftxt.UPDATE_TEXT_COLOR");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(textUpdateReceiver, filter, Context.RECEIVER_EXPORTED);
            } else {
                registerReceiver(textUpdateReceiver, filter);
            }

        } catch (Exception e) {
            e.printStackTrace();
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {

        super.onDestroy();

        try {

            if (textUpdateReceiver != null) {
                unregisterReceiver(textUpdateReceiver);
            }

            if (floatingText != null) {
                windowManager.removeView(floatingText);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}