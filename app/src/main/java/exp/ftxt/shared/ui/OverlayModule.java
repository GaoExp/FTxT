package exp.ftxt.shared.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.WindowManager;

public interface OverlayModule {
    void init(WindowManager wm, Context ctx, SharedPreferences prefs);
    void start(WindowManager wm, Context ctx);
    void stop();
    boolean isRunning();
    void updateSize(float size);
    void updateColor(int color);
    void updateLabelColor(int color);
    default void updateSeparatorColor(int color) {}
    void updateShadow();
    void updateBackground();
    void updatePosition();
    void updateTouchFlags();
    void setOrientationSuffix(String suffix);
    void reloadPosition();
    int[] getCurrentPosition();
    void hide();
    void show();
    boolean isHidden();
}
