package exp.ftxt.features.crosshair;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.ImageView;

import exp.ftxt.shared.config.BackgroundConfig;
import exp.ftxt.shared.ui.OverlayDragHandler;
import exp.ftxt.shared.ui.OverlayModule;
import exp.ftxt.shared.ui.ShadowImageView;

public class CrosshairModule implements OverlayModule {

    private ShadowImageView view;
    private WindowManager.LayoutParams params;
    private WindowManager wm;
    private Context context;
    private SharedPreferences prefs;
    private boolean running = false;
    private int screenWidth;
    private int screenHeight;
    private String orientationSuffix;

    public static Runnable onPositionUpdate;

    @Override
    public void setOrientationSuffix(String suffix) {
        this.orientationSuffix = suffix;
    }

    @Override
    public void init(WindowManager windowManager, Context ctx,
                     SharedPreferences sp) {
        wm = windowManager;
        context = ctx;
        prefs = sp;
        orientationSuffix = null;
        refreshScreenSize();
        loadPosition(prefs);
    }

    @Override
    public void start(WindowManager windowManager, Context ctx) {
        if (running) return;
        wm = windowManager;
        context = ctx;
        refreshScreenSize();

        view = new ShadowImageView(ctx);
        view.setImageResource(styleResId(ctx));
        view.setScaleType(ImageView.ScaleType.FIT_XY);
        view.setAlpha(CrosshairConfig.opacity / 100f);
        view.setRotation(CrosshairConfig.rotation);
        applyColor();
        applyBackground();

        int sizePx = dpToPx(CrosshairConfig.size);
        params = new WindowManager.LayoutParams(
                sizePx,
                sizePx,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;

        updateTouchFlags();

        try {
            wm.addView(view, params);
        } catch (Exception e) {
            e.printStackTrace();
            view = null;
            return;
        }

        view.post(this::updatePosition);
        running = true;
    }

    @Override
    public void stop() {
        running = false;
        savePosition();
        if (view != null && wm != null) {
            try {
                wm.removeView(view);
            } catch (Exception e) {
                e.printStackTrace();
            }
            view = null;
        }
        params = null;
    }

    @Override
    public void updateSize(float size) {
        CrosshairConfig.size = size;
        if (view == null || params == null || wm == null) return;
        int sizePx = dpToPx(size);
        params.width = sizePx;
        params.height = sizePx;
        try {
            wm.updateViewLayout(view, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        updatePosition();
    }

    @Override
    public void updateColor(int color) {
        applyColor();
    }

    @Override
    public void updateLabelColor(int color) {
    }

    @Override
    public void updateShadow() {
    }

    @Override
    public void updateBackground() {
        applyBackground();
    }

    /** Terapkan tint warna mengikuti CrosshairConfig.colorEnabled & CrosshairConfig.color. */
    public void applyColor() {
        if (view == null) return;
        view.setTintEnabled(CrosshairConfig.colorEnabled);
        view.setTintColor(CrosshairConfig.color);
        view.invalidate();
    }

    /** Terapkan background mengikuti CrosshairConfig.bg (padding jarak gambar ke kotak). */
    public void applyBackground() {
        if (view == null) return;
        BackgroundConfig bg = CrosshairConfig.bg;
        int pad = bg.enabled ? bg.padding : 0;
        view.setPadding(pad, pad, pad, pad);
        view.setBgEnabled(bg.enabled);
        view.setBgColor(bg.color);
        view.setBgOffsetX(bg.offsetX);
        view.setBgOffsetY(bg.offsetY);
        view.setBgMargin(bg.margin);
        view.setBgRadius(bg.radius);
        view.invalidate();
    }

    /** Ganti gambar bidikan mengikuti CrosshairConfig.styleIndex. */
    public void applyStyle() {
        if (view != null && context != null) {
            int res = styleResId(context);
            if (res != 0) view.setImageResource(res);
        }
    }

    /** Terapkan opasitas mengikuti CrosshairConfig.opacity. */
    public void applyOpacity() {
        if (view != null) view.setAlpha(CrosshairConfig.opacity / 100f);
    }

    /** Terapkan rotasi mengikuti CrosshairConfig.rotation. */
    public void applyRotation() {
        if (view != null) view.setRotation(CrosshairConfig.rotation);
    }

    @Override
    public void updatePosition() {
        if (view == null || params == null || wm == null) return;
        refreshScreenSize();
        applyParamsPosition();
        try {
            wm.updateViewLayout(view, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (onPositionUpdate != null) onPositionUpdate.run();
    }

    @Override
    public void updateTouchFlags() {
        if (params == null || view == null || wm == null) return;

        if (CrosshairConfig.touchPassthrough) {
            params.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            view.setOnTouchListener(null);
        } else {
            params.flags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            view.setOnTouchListener(new OverlayDragHandler(params, wm,
                    this::savePosition,
                    () -> {
                        if (CrosshairConfig.safeArea && view != null && view.getWidth() > 0 && view.getHeight() > 0) {
                            int maxX = Math.max(0, screenWidth - view.getWidth());
                            int maxY = Math.max(0, screenHeight - view.getHeight());
                            params.x = Math.max(0, Math.min(params.x, maxX));
                            params.y = Math.max(0, Math.min(params.y, maxY));
                        }
                        if (params != null) {
                            CrosshairConfig.posX = (params.x + view.getWidth() / 2f) / screenWidth;
                            CrosshairConfig.posY = (params.y + view.getHeight() / 2f) / screenHeight;
                        }
                        if (onPositionUpdate != null) onPositionUpdate.run();
                    }, CrosshairConfig.safeArea));
        }

        try { wm.updateViewLayout(view, params); } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public void reloadPosition() {
        orientationSuffix = null;
        loadPosition(prefs);
        updatePosition();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void hide() {
        if (view != null) view.setVisibility(android.view.View.GONE);
    }

    @Override
    public void show() {
        if (view != null) view.setVisibility(android.view.View.VISIBLE);
    }

    @Override
    public boolean isHidden() {
        if (view != null) return view.getVisibility() == android.view.View.GONE;
        return false;
    }

    @Override
    public int[] getCurrentPosition() {
        if (params != null) return new int[]{params.x, params.y};
        return null;
    }

    private int styleResId(Context ctx) {
        String name = "crosshair_" + Math.max(1, Math.min(44, CrosshairConfig.styleIndex));
        return ctx.getResources().getIdentifier(name, "drawable", ctx.getPackageName());
    }

    private void applyParamsPosition() {
        int vw = view.getWidth() > 0 ? view.getWidth() : params.width;
        int vh = view.getHeight() > 0 ? view.getHeight() : params.height;
        params.x = (int) (CrosshairConfig.posX * screenWidth) - vw / 2;
        params.y = (int) (CrosshairConfig.posY * screenHeight) - vh / 2;
        if (CrosshairConfig.safeArea) {
            int maxX = Math.max(0, screenWidth - vw);
            int maxY = Math.max(0, screenHeight - vh);
            params.x = Math.max(0, Math.min(params.x, maxX));
            params.y = Math.max(0, Math.min(params.y, maxY));
        }
    }

    private void refreshScreenSize() {
        if (wm == null) return;
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;
    }

    private int dpToPx(float dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }

    private String posSuffix() {
        if (orientationSuffix != null) return "_" + orientationSuffix;
        return (context.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE) ? "_land" : "_port";
    }

    public void loadPosition(SharedPreferences prefs) {
        String sfx = posSuffix();
        CrosshairConfig.posX = prefs.getFloat("crosshair_pos_x" + sfx, 0.5f);
        CrosshairConfig.posY = prefs.getFloat("crosshair_pos_y" + sfx, 0.5f);
    }

    private void savePosition() {
        if (params != null && prefs != null && view != null) {
            refreshScreenSize();
            if (CrosshairConfig.safeArea && view.getWidth() > 0 && view.getHeight() > 0) {
                int maxX = Math.max(0, screenWidth - view.getWidth());
                int maxY = Math.max(0, screenHeight - view.getHeight());
                params.x = Math.max(0, Math.min(params.x, maxX));
                params.y = Math.max(0, Math.min(params.y, maxY));
            }
            CrosshairConfig.posX = (params.x + view.getWidth() / 2f) / screenWidth;
            CrosshairConfig.posY = (params.y + view.getHeight() / 2f) / screenHeight;
            String sfx = posSuffix();
            prefs.edit()
                    .putFloat("crosshair_pos_x" + sfx, CrosshairConfig.posX)
                    .putFloat("crosshair_pos_y" + sfx, CrosshairConfig.posY)
                    .apply();
        }
    }
}
