package exp.ftxt.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.List;

import exp.ftxt.shared.ui.OverlayModule;
import exp.ftxt.shared.ui.SmartPanelTarget;

public class SmartPanelModule implements OverlayModule {

    private PanelRoot view;
    private WindowManager.LayoutParams params;
    private WindowManager wm;
    private Context context;
    private SharedPreferences prefs;
    private boolean running;
    private String orientationSuffix;

    private FrameLayout iconContainer;
    private TextView iconView;
    private LinearLayout contentWrapper;
    private LinearLayout panelView;
    private LinearLayout modulePickerView;
    private TextView moduleLabel;
    private LinearLayout specificsContainer;
    private boolean panelExpanded = false;
    private boolean pickerOpened = false;
    private boolean collapseAnimRunning = false;

    private SmartPanelTarget activeTarget;
    private SmartPanelRegistry.Entry activeEntry;

    private TextView toggleOnOffView;
    private TextView toggleLockView;

    private static final int ICON_SIZE_DP = 48;
    private static final int PANEL_WIDTH_DP = 220;
    private static final int PICKER_ITEM_HEIGHT_DP = 40;
    private static final int PICKER_MAX_VISIBLE = 5;
    private static final int DPAD_TAP_PX = 1;
    private static final int DPAD_HOLD_PX = 10;
    private static final long DPAD_HOLD_DELAY_MS = 400;
    private static final long DPAD_REPEAT_MS = 60;
    private static final long ANIM_EXPAND_MS = 200;
    private static final long ANIM_COLLAPSE_MS = 150;
    private static final long DOUBLE_TAP_MS = 280;

    // Posisi pojok kiri-atas ikon di LAYAR (independen dari ukuran window).
    private int iconScreenX, iconScreenY;
    // Posisi child di dalam window (dihitung ulang tiap relayout).
    private int iconInRootX, iconInRootY;
    private int contentInRootX, contentInRootY;
    private boolean contentOnRight = true;

    private int dragStartWindowX, dragStartWindowY;
    private float dragStartRawX, dragStartRawY;
    private boolean isDragging;

    private final Handler breatheHandler = new Handler();
    private boolean breathing;
    private final Runnable breatheRunnable = new Runnable() {
        private boolean dim;

        @Override
        public void run() {
            if (iconView != null && breathing) {
                dim = !dim;
                iconView.animate().alpha(dim ? 0.75f : 1f).setDuration(900).start();
                breatheHandler.postDelayed(this, 900);
            }
        }
    };

    private void startBreathing() {
        breathing = true;
        breatheHandler.removeCallbacks(breatheRunnable);
        breatheHandler.postDelayed(breatheRunnable, 900);
    }

    private void stopBreathing() {
        breathing = false;
        breatheHandler.removeCallbacksAndMessages(null);
        if (iconView != null) {
            iconView.animate().cancel();
            iconView.setAlpha(1f);
            iconView.setScaleX(1f);
            iconView.setScaleY(1f);
        }
    }

    private final Handler repeatHandler = new Handler();
    private final Handler tapHandler = new Handler();
    private long lastTapTime;
    private final Runnable singleTapAction = this::togglePanel;

    @Override
    public void setOrientationSuffix(String suffix) {
        this.orientationSuffix = suffix;
    }

    @Override
    public void init(WindowManager windowManager, Context ctx, SharedPreferences sp) {
        wm = windowManager;
        context = ctx;
        prefs = sp;
        orientationSuffix = null;
        SmartPanelConfig.iconVisible = sp.getBoolean("smart_panel_icon_visible", true);
        SmartPanelConfig.iconX = sp.getInt("smart_panel_icon_x", 0);
        SmartPanelConfig.iconY = sp.getInt("smart_panel_icon_y", -1);
        SmartPanelConfig.iconEdge = sp.getString("smart_panel_icon_edge", "left");
        SmartPanelConfig.activeModule = sp.getString("smart_panel_active_module", "crosshair");
    }

    @Override
    public void start(WindowManager windowManager, Context ctx) {
        if (running) return;
        wm = windowManager;
        context = ctx;
        prefs = ctx.getSharedPreferences("ftxt_prefs", Context.MODE_PRIVATE);

        view = new PanelRoot(context);
        view.setClipChildren(false);
        view.setOnOutsideTouch(this::onOutsideTouch);

        buildIcon();
        buildPanel();
        resolveActiveTarget();

        params = new WindowManager.LayoutParams(
                dp(ICON_SIZE_DP),
                dp(ICON_SIZE_DP),
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        iconScreenX = resolveIconX();
        iconScreenY = resolveIconY();
        params.x = iconScreenX;
        params.y = iconScreenY;
        iconInRootX = 0;
        iconInRootY = 0;
        contentInRootX = 0;
        contentInRootY = 0;

        try {
            wm.addView(view, params);
        } catch (Exception e) {
            e.printStackTrace();
            view = null;
            return;
        }

        running = true;
        if (SmartPanelConfig.iconVisible) {
            startBreathing();
        } else {
            view.setVisibility(View.GONE);
        }
    }

    private void buildIcon() {
        iconContainer = new FrameLayout(context);

        iconView = new TextView(context);
        iconView.setText("◎");
        iconView.setTextSize(22);
        iconView.setTextColor(Color.WHITE);
        iconView.setGravity(Gravity.CENTER);
        iconView.setContentDescription("Smart Panel");

        GradientDrawable iconBg = new GradientDrawable();
        iconBg.setShape(GradientDrawable.OVAL);
        iconBg.setColor(0x80222B45);
        iconView.setBackground(iconBg);

        iconContainer.addView(iconView, lpMatch());
        iconContainer.setOnTouchListener(new IconDragListener());
    }

    private void buildPanel() {
        contentWrapper = new LinearLayout(context);
        contentWrapper.setOrientation(LinearLayout.VERTICAL);

        panelView = new LinearLayout(context);
        panelView.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable panelBg = new GradientDrawable();
        panelBg.setShape(GradientDrawable.RECTANGLE);
        panelBg.setCornerRadius(dp(16));
        panelBg.setColor(0xE0232A36);
        panelView.setBackground(panelBg);
        panelView.setPadding(dp(12), dp(12), dp(12), dp(12));

        moduleLabel = new TextView(context);
        moduleLabel.setTextSize(14);
        moduleLabel.setTextColor(Color.WHITE);
        moduleLabel.setPadding(0, 0, 0, dp(6));
        moduleLabel.setContentDescription("Pilih modul yang dikontrol");
        moduleLabel.setOnClickListener(v -> showModulePicker());

        LinearLayout dpadRoot = buildDpad();
        LinearLayout toggleRow = buildToggleRow();
        specificsContainer = new LinearLayout(context);
        specificsContainer.setOrientation(LinearLayout.VERTICAL);

        panelView.addView(moduleLabel);
        panelView.addView(dpadRoot);
        panelView.addView(toggleRow);
        panelView.addView(specificsContainer);
        updateModuleTitle();
        refreshSpecifics();

        contentWrapper.addView(panelView, new LinearLayout.LayoutParams(
                dp(PANEL_WIDTH_DP), ViewGroup.LayoutParams.WRAP_CONTENT));

        buildModulePicker();
        contentWrapper.addView(modulePickerView, new LinearLayout.LayoutParams(
                dp(PANEL_WIDTH_DP), ViewGroup.LayoutParams.WRAP_CONTENT));

        view.addView(iconContainer, new FrameLayout.LayoutParams(dp(ICON_SIZE_DP), dp(ICON_SIZE_DP)));
        view.addView(contentWrapper, new FrameLayout.LayoutParams(
                dp(PANEL_WIDTH_DP), ViewGroup.LayoutParams.WRAP_CONTENT));
        panelView.setVisibility(View.GONE);
        modulePickerView.setVisibility(View.GONE);
    }

    private void buildModulePicker() {
        modulePickerView = new LinearLayout(context);
        modulePickerView.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable pickerBg = new GradientDrawable();
        pickerBg.setShape(GradientDrawable.RECTANGLE);
        pickerBg.setCornerRadius(dp(16));
        pickerBg.setColor(0xEE232A36);
        modulePickerView.setBackground(pickerBg);
        modulePickerView.setPadding(dp(8), dp(8), dp(8), dp(8));
    }

    private LinearLayout buildDpad() {
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView btnUp = makeDpadButton("▲");
        TextView btnLeft = makeDpadButton("◄");
        TextView btnCenter = makeDpadButton("Reset");
        TextView btnRight = makeDpadButton("►");
        TextView btnDown = makeDpadButton("▼");
        btnCenter.setTextSize(12);
        btnUp.setContentDescription("Geser modul ke atas");
        btnDown.setContentDescription("Geser modul ke bawah");
        btnLeft.setContentDescription("Geser modul ke kiri");
        btnRight.setContentDescription("Geser modul ke kanan");
        btnCenter.setContentDescription("Reset posisi modul ke tengah layar");

        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(dp(48), dp(40));

        LinearLayout rowUp = new LinearLayout(context);
        rowUp.setGravity(Gravity.CENTER_HORIZONTAL);
        rowUp.addView(btnUp, btnLp);

        LinearLayout rowMid = new LinearLayout(context);
        rowMid.setGravity(Gravity.CENTER_HORIZONTAL);
        rowMid.addView(btnLeft, btnLp);
        rowMid.addView(btnCenter, btnLp);
        rowMid.addView(btnRight, btnLp);

        LinearLayout rowDown = new LinearLayout(context);
        rowDown.setGravity(Gravity.CENTER_HORIZONTAL);
        rowDown.addView(btnDown, btnLp);

        container.addView(rowUp);
        container.addView(rowMid);
        container.addView(rowDown);

        btnUp.setOnTouchListener(createRepeatTouch(0, -1));
        btnDown.setOnTouchListener(createRepeatTouch(0, 1));
        btnLeft.setOnTouchListener(createRepeatTouch(-1, 0));
        btnRight.setOnTouchListener(createRepeatTouch(1, 0));
        btnCenter.setOnClickListener(v -> {
            if (activeTarget != null) activeTarget.resetPosition();
        });

        return container;
    }

    private View.OnTouchListener createRepeatTouch(int dirX, int dirY) {
        return new View.OnTouchListener() {
            private final Runnable holdTrigger = new Runnable() {
                @Override
                public void run() {
                    moveActive(dirX, dirY, DPAD_HOLD_PX);
                    repeatHandler.postDelayed(repeat, DPAD_REPEAT_MS);
                }
            };

            private final Runnable repeat = new Runnable() {
                @Override
                public void run() {
                    moveActive(dirX, dirY, DPAD_HOLD_PX);
                    repeatHandler.postDelayed(this, DPAD_REPEAT_MS);
                }
            };

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        moveActive(dirX, dirY, DPAD_TAP_PX);
                        repeatHandler.postDelayed(holdTrigger, DPAD_HOLD_DELAY_MS);
                        setButtonHighlight(v, true);
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        repeatHandler.removeCallbacks(holdTrigger);
                        repeatHandler.removeCallbacks(repeat);
                        setButtonHighlight(v, false);
                        return true;
                }
                return false;
            }
        };
    }

    private void moveActive(int dirX, int dirY, int stepPx) {
        if (activeTarget == null) return;
        int screenW = screenWidth();
        int screenH = screenHeight();
        activeTarget.moveBy((dirX * stepPx) / (float) screenW, (dirY * stepPx) / (float) screenH);
    }

    private void setButtonHighlight(View v, boolean pressed) {
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(dp(8));
        bg.setColor(pressed ? 0xFF5C6BC0 : 0x443B4658);
        ((TextView) v).setBackground(bg);
        v.setPressed(pressed);
    }

    private LinearLayout buildToggleRow() {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);

        toggleOnOffView = makeToggleButton();
        toggleLockView = makeToggleButton();
        toggleOnOffView.setContentDescription("Aktifkan atau nonaktifkan modul");
        toggleLockView.setContentDescription("Kunci atau buka posisi modul");
        toggleOnOffView.setOnClickListener(v -> onToggleOnOff());
        toggleLockView.setOnClickListener(v -> onToggleLock());

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(dp(2), dp(2), dp(2), dp(2));
        row.addView(toggleOnOffView, lp);
        row.addView(toggleLockView, lp);
        refreshToggleLabels();
        return row;
    }

    private TextView makeToggleButton() {
        TextView tv = new TextView(context);
        tv.setTextSize(13);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(dp(6), dp(8), dp(6), dp(8));
        return tv;
    }

    private void applyToggleStyle(TextView tv, int bgColor, boolean active) {
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(dp(10));
        bg.setStroke(dp(1), active ? 0x99FFFFFF : 0x33FFFFFF);
        bg.setColor(bgColor);
        tv.setBackground(bg);
        tv.setTextColor(Color.WHITE);
    }

    private void onToggleOnOff() {
        if (activeEntry == null) return;
        if (activeTarget != null && activeTarget.isEnabled()) {
            activeEntry.disableAction.run();
        } else {
            activeEntry.enableAction.run();
        }
        refreshToggleLabels();
    }

    private void onToggleLock() {
        if (activeTarget == null) return;
        activeTarget.setPositionLocked(!activeTarget.isPositionLocked());
        refreshToggleLabels();
    }

    private void refreshToggleLabels() {
        if (activeTarget == null) {
            toggleOnOffView.setText("Aktifkan");
            applyToggleStyle(toggleOnOffView, 0xFF616161, false);
            toggleLockView.setText("Kunci Posisi");
            applyToggleStyle(toggleLockView, 0xFF616161, false);
            return;
        }
        boolean on = activeTarget.isEnabled();
        toggleOnOffView.setText(on ? "Nonaktifkan" : "Aktifkan");
        applyToggleStyle(toggleOnOffView, on ? 0xFF2E7D32 : 0xFF616161, on);

        boolean locked = activeTarget.isPositionLocked();
        toggleLockView.setText(locked ? "Buka Kunci" : "Kunci Posisi");
        applyToggleStyle(toggleLockView, locked ? 0xFFE65100 : 0xFF616161, locked);
    }

    // ── Kontrol spesifik (Opacity / Size / Reset) ──

    private void refreshSpecifics() {
        if (specificsContainer == null) return;
        specificsContainer.removeAllViews();
        if (activeTarget == null) return;

        if (activeTarget.canSetOpacity()) {
            specificsContainer.addView(buildOpacityControl());
        }
        if (activeTarget.canSetSize()) {
            specificsContainer.addView(buildSizeControl());
        }
        if (activeTarget.canResetConfig()) {
            specificsContainer.addView(buildResetControl());
        }
    }

    private View buildOpacityControl() {
        LinearLayout wrap = new LinearLayout(context);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(0, dp(4), 0, dp(4));

        TextView label = new TextView(context);
        label.setText("Opacity");
        label.setTextSize(12);
        label.setTextColor(Color.rgb(160, 170, 190));
        wrap.addView(label);

        SeekBar seek = new SeekBar(context);
        seek.setMax(100);
        seek.setProgress(activeTarget.getOpacity());
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar s, int progress, boolean fromUser) {
                if (fromUser) activeTarget.setOpacity(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar s) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar s) {
            }
        });
        wrap.addView(seek);
        return wrap;
    }

    private View buildSizeControl() {
        LinearLayout wrap = new LinearLayout(context);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(0, dp(4), 0, dp(4));

        TextView label = new TextView(context);
        label.setText("Size");
        label.setTextSize(12);
        label.setTextColor(Color.rgb(160, 170, 190));
        wrap.addView(label);

        boolean isCrosshair = activeEntry != null && "crosshair".equals(activeEntry.id);
        float min = isCrosshair ? 4f : 6f;
        float max = isCrosshair ? 160f : 50f;

        SeekBar seek = new SeekBar(context);
        seek.setMax(1000);
        int progress = Math.round((activeTarget.getSize() - min) / (max - min) * 1000);
        seek.setProgress(Math.max(0, Math.min(1000, progress)));
        float finalMin = min;
        float finalMax = max;
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar s, int progress, boolean fromUser) {
                if (fromUser) {
                    float value = finalMin + (progress / 1000f) * (finalMax - finalMin);
                    activeTarget.setSize(value);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar s) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar s) {
            }
        });
        wrap.addView(seek);
        return wrap;
    }

    private View buildResetControl() {
        TextView reset = new TextView(context);
        reset.setText("↺ Reset");
        reset.setTextSize(13);
        reset.setTextColor(Color.RED);
        reset.setGravity(Gravity.CENTER);
        reset.setPadding(0, dp(6), 0, dp(2));
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(dp(8));
        bg.setColor(0x22444444);
        reset.setBackground(bg);
        reset.setContentDescription("Reset pengaturan modul");
        reset.setOnClickListener(v -> {
            if (activeTarget != null) activeTarget.resetConfig();
            refreshSpecifics();
        });
        return reset;
    }

    private TextView makeDpadButton(String label) {
        TextView btn = new TextView(context);
        btn.setText(label);
        btn.setTextSize(16);
        btn.setTextColor(Color.WHITE);
        btn.setGravity(Gravity.CENTER);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(dp(8));
        bg.setColor(0x443B4658);
        btn.setBackground(bg);
        return btn;
    }

    // ── Pilih modul: dropdown di dalam window yang sama ──

    private void showModulePicker() {
        if (collapseAnimRunning) {
            panelView.animate().cancel();
            collapseAnimRunning = false;
            panelExpanded = false;
            panelView.setAlpha(1f);
            panelView.setScaleX(1f);
            panelView.setScaleY(1f);
            panelView.setVisibility(View.GONE);
        }
        if (pickerOpened) return;
        List<SmartPanelRegistry.Entry> entries = SmartPanelRegistry.getEntries();
        if (entries.isEmpty()) return;

        modulePickerView.removeAllViews();
        int visibleCount = Math.min(entries.size(), PICKER_MAX_VISIBLE);

        for (int i = 0; i < entries.size(); i++) {
            final SmartPanelRegistry.Entry e = entries.get(i);
            TextView item = new TextView(context);
            item.setText(e.title);
            item.setTextSize(14);
            item.setTextColor(Color.WHITE);
            item.setSingleLine(true);
            item.setGravity(Gravity.CENTER_VERTICAL);
            item.setPadding(dp(12), 0, dp(12), 0);
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.RECTANGLE);
            bg.setCornerRadius(dp(8));
            bg.setColor(e.id.equals(SmartPanelConfig.activeModule) ? 0xFF2E5B4F : 0x443B4658);
            item.setBackground(bg);
            item.setContentDescription("Pilih modul " + e.title);
            item.setOnClickListener(v -> selectModule(e));

            LinearLayout.LayoutParams itemLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(PICKER_ITEM_HEIGHT_DP));
            itemLp.bottomMargin = dp(4);
            modulePickerView.addView(item, itemLp);
            if (i >= visibleCount) break;
        }

        modulePickerView.setVisibility(View.VISIBLE);
        pickerOpened = true;
        relayoutRoot();
    }

    private void closeModulePicker() {
        if (!pickerOpened) return;
        pickerOpened = false;
        modulePickerView.setVisibility(View.GONE);
        relayoutRoot();
    }

    private void selectModule(SmartPanelRegistry.Entry e) {
        SmartPanelConfig.activeModule = e.id;
        prefs.edit().putString("smart_panel_active_module", e.id).apply();
        activeEntry = e;
        activeTarget = e.targetProvider.get();
        closeModulePicker();
        updateModuleTitle();
        refreshToggleLabels();
        refreshSpecifics();
    }

    private void updateModuleTitle() {
        if (moduleLabel != null) {
            moduleLabel.setText(activeEntry != null ? activeEntry.title + " ▾" : "Smart Panel ▾");
        }
    }

    private void resolveActiveTarget() {
        String id = SmartPanelConfig.activeModule;
        activeEntry = SmartPanelRegistry.find(id);
        if (activeEntry == null && !SmartPanelRegistry.getEntries().isEmpty()) {
            activeEntry = SmartPanelRegistry.getEntries().get(0);
        }
        activeTarget = activeEntry != null ? activeEntry.targetProvider.get() : null;
        updateModuleTitle();
    }

    // ── Buka / tutup panel ──

    private void togglePanel() {
        if (!running || view == null || wm == null) return;
        if (panelExpanded) {
            collapsePanel();
        } else {
            expandPanel();
        }
    }

    private void expandPanel() {
        if (!running || view == null || wm == null) return;
        if (panelExpanded && collapseAnimRunning) {
            panelView.animate().cancel();
            collapseAnimRunning = false;
            panelExpanded = false;
            panelView.setAlpha(1f);
            panelView.setScaleX(1f);
            panelView.setScaleY(1f);
            panelView.setVisibility(View.GONE);
        }
        if (panelExpanded) return;
        panelExpanded = true;
        pickerOpened = false;
        modulePickerView.setVisibility(View.GONE);
        panelView.setVisibility(View.VISIBLE);
        relayoutRoot();
        animatePanelIn();
    }

    private void collapsePanel() {
        if (!running || view == null || wm == null) return;
        if (pickerOpened) {
            closeModulePicker();
        }
        if (!panelExpanded) return;
        collapseAnimRunning = true;
        panelView.animate().cancel();
        panelView.animate().alpha(0f).scaleX(0.85f).scaleY(0.85f)
                .setDuration(ANIM_COLLAPSE_MS)
                .setInterpolator(new AccelerateInterpolator())
                .withEndAction(() -> {
                    collapseAnimRunning = false;
                    panelExpanded = false;
                    if (panelView != null) panelView.setVisibility(View.GONE);
                    relayoutRoot();
                    bounceIcon();
                })
                .start();
    }

    private void onOutsideTouch() {
        if (pickerOpened) {
            closeModulePicker();
        } else {
            collapsePanel();
        }
    }

    private void animatePanelIn() {
        final PanelRoot root = view;
        if (root == null || panelView == null) return;
        root.post(() -> {
            if (panelView == null || panelView.getWidth() <= 0) return;
            float pivotX = contentOnRight ? 0f : panelView.getWidth();
            panelView.setPivotX(pivotX);
            panelView.setPivotY(panelView.getHeight() / 2f);
            panelView.setAlpha(0f);
            panelView.setScaleX(0.85f);
            panelView.setScaleY(0.85f);
            panelView.animate().alpha(1f).scaleX(1f).scaleY(1f)
                    .setDuration(ANIM_EXPAND_MS)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        });
    }

    private void bounceIcon() {
        if (iconView == null) return;
        iconView.animate().cancel();
        iconView.setScaleX(1f);
        iconView.setScaleY(1f);
        iconView.animate().scaleX(0.82f).scaleY(0.82f).setDuration(90)
                .withEndAction(() -> {
                    if (iconView == null) return;
                    iconView.animate().scaleX(1f).scaleY(1f).setDuration(180)
                            .setInterpolator(new OvershootInterpolator(2.0f))
                            .start();
                })
                .start();
    }

    // ── Layout root: ikon & panel diposisikan sendiri-sendiri ──

    private void relayoutRoot() {
        if (view == null || params == null || wm == null) return;

        boolean expanded = panelExpanded || pickerOpened;
        int iconPx = dp(ICON_SIZE_DP);

        if (!expanded) {
            params.width = iconPx;
            params.height = iconPx;
            params.x = iconScreenX;
            params.y = iconScreenY;
            iconInRootX = 0;
            iconInRootY = 0;
            contentInRootX = 0;
            contentInRootY = 0;
            applyChildLayouts();
            try {
                wm.updateViewLayout(view, params);
            } catch (Exception e) {
                e.printStackTrace();
            }
            view.requestLayout();
            return;
        }

        int panelW = dp(PANEL_WIDTH_DP);
        int panelH = measurePanelHeight();
        int pickerH = pickerOpened ? measurePickerHeight() : 0;
        int contentH = panelH + (pickerOpened ? pickerH + dp(4) : 0);

        int windowW = iconPx + panelW;
        int windowH = Math.max(iconPx, contentH);
        int sw = screenWidth();
        int sh = screenHeight();
        if (windowW > sw) windowW = sw;

        int spaceRight = sw - iconScreenX - iconPx;
        int spaceLeft = iconScreenX;
        boolean openRight;
        if (spaceRight >= panelW && spaceLeft >= panelW) {
            openRight = iconScreenX <= sw / 2;
        } else if (spaceRight >= panelW) {
            openRight = true;
        } else if (spaceLeft >= panelW) {
            openRight = false;
        } else {
            openRight = spaceRight >= spaceLeft;
        }
        contentOnRight = openRight;

        int iconX0 = openRight ? 0 : panelW;
        int contentX = openRight ? iconPx : 0;

        int windowX = iconScreenX - iconX0;
        windowX = Math.max(0, Math.min(sw - windowW, windowX));
        iconX0 = iconScreenX - windowX;

        int windowY = iconScreenY - (windowH - iconPx) / 2;
        windowY = Math.max(0, Math.min(sh - windowH, windowY));
        int iconY0 = iconScreenY - windowY;
        int contentY = Math.max(0, Math.min(windowH - contentH,
                iconY0 + iconPx / 2 - contentH / 2));

        iconInRootX = iconX0;
        iconInRootY = iconY0;
        contentInRootX = contentX;
        contentInRootY = contentY;

        params.width = windowW;
        params.height = windowH;
        params.x = windowX;
        params.y = windowY;
        applyChildLayouts();
        try {
            wm.updateViewLayout(view, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        view.requestLayout();
    }

    private void applyChildLayouts() {
        if (iconContainer == null) return;
        FrameLayout.LayoutParams ilp = (FrameLayout.LayoutParams) iconContainer.getLayoutParams();
        ilp.width = dp(ICON_SIZE_DP);
        ilp.height = dp(ICON_SIZE_DP);
        ilp.leftMargin = iconInRootX;
        ilp.topMargin = iconInRootY;
        iconContainer.setLayoutParams(ilp);

        if (contentWrapper == null) return;
        FrameLayout.LayoutParams clp = (FrameLayout.LayoutParams) contentWrapper.getLayoutParams();
        clp.width = dp(PANEL_WIDTH_DP);
        clp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        clp.leftMargin = contentInRootX;
        clp.topMargin = contentInRootY;
        contentWrapper.setLayoutParams(clp);
    }

    private int measurePanelHeight() {
        return measureHeight(panelView, dp(PANEL_WIDTH_DP));
    }

    private int measurePickerHeight() {
        return measureHeight(modulePickerView, dp(PANEL_WIDTH_DP));
    }

    private int measureHeight(View v, int widthPx) {
        if (v == null) return 0;
        int wSpec = View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY);
        int hSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        v.measure(wSpec, hSpec);
        return Math.max(0, v.getMeasuredHeight());
    }

    // ── Drag ikon + tap / double-tap ──

    private class IconDragListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    dragStartRawX = event.getRawX();
                    dragStartRawY = event.getRawY();
                    dragStartWindowX = params.x;
                    dragStartWindowY = params.y;
                    isDragging = false;
                    iconView.animate().scaleX(1.1f).scaleY(1.1f).setDuration(80).start();
                    return true;

                case MotionEvent.ACTION_MOVE:
                    float dx = event.getRawX() - dragStartRawX;
                    float dy = event.getRawY() - dragStartRawY;
                    if (!isDragging && (Math.abs(dx) > 10 || Math.abs(dy) > 10)) {
                        isDragging = true;
                        lastTapTime = 0;
                    }
                    if (isDragging) {
                        params.x = dragStartWindowX + (int) dx;
                        params.y = dragStartWindowY + (int) dy;
                        try {
                            wm.updateViewLayout(view, params);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                    iconView.animate().scaleX(1f).scaleY(1f).setDuration(80).start();
                    if (!isDragging) {
                        handleTap();
                    } else {
                        iconScreenX = params.x + iconInRootX;
                        iconScreenY = params.y + iconInRootY;
                        snapAndSaveIcon();
                        relayoutRoot();
                    }
                    return true;

                case MotionEvent.ACTION_CANCEL:
                    iconView.animate().scaleX(1f).scaleY(1f).setDuration(80).start();
                    return true;
            }
            return false;
        }
    }

    private void handleTap() {
        long now = SystemClock.uptimeMillis();
        if (now - lastTapTime <= DOUBLE_TAP_MS) {
            tapHandler.removeCallbacks(singleTapAction);
            lastTapTime = 0;
            handleDoubleTap();
        } else {
            lastTapTime = now;
            tapHandler.removeCallbacks(singleTapAction);
            tapHandler.postDelayed(singleTapAction, DOUBLE_TAP_MS);
        }
    }

    private void handleDoubleTap() {
        collapsePanel();
        hideIcon();
    }

    private void snapAndSaveIcon() {
        int sw = screenWidth();
        int iconPx = dp(ICON_SIZE_DP);
        if (iconScreenX < sw / 2) {
            SmartPanelConfig.iconEdge = "left";
            SmartPanelConfig.iconX = iconScreenX;
        } else {
            SmartPanelConfig.iconEdge = "right";
            SmartPanelConfig.iconX = sw - iconScreenX - iconPx;
        }
        SmartPanelConfig.iconY = iconScreenY;
        savePosition();
    }

    @Override
    public void stop() {
        running = false;
        repeatHandler.removeCallbacksAndMessages(null);
        tapHandler.removeCallbacksAndMessages(null);
        stopBreathing();
        collapseAnimRunning = false;
        panelExpanded = false;
        pickerOpened = false;
        if (panelView != null) {
            panelView.animate().cancel();
            panelView.setVisibility(View.GONE);
        }
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
    public boolean isRunning() {
        return running;
    }

    @Override
    public void updateSize(float size) {
    }

    @Override
    public void updateColor(int color) {
    }

    @Override
    public void updateLabelColor(int color) {
    }

    @Override
    public void updateShadow() {
    }

    @Override
    public void updateBackground() {
    }

    @Override
    public void updatePosition() {
        iconScreenX = resolveIconX();
        iconScreenY = resolveIconY();
        relayoutRoot();
    }

    @Override
    public void updateTouchFlags() {
        if (params == null || view == null || wm == null) return;
        try {
            wm.updateViewLayout(view, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void reloadPosition() {
        orientationSuffix = null;
        loadPosition();
        updatePosition();
    }

    private void loadPosition() {
        if (prefs == null) return;
        SmartPanelConfig.iconX = prefs.getInt("smart_panel_icon_x", 0);
        SmartPanelConfig.iconY = prefs.getInt("smart_panel_icon_y", -1);
        SmartPanelConfig.iconEdge = prefs.getString("smart_panel_icon_edge", "left");
        SmartPanelConfig.iconVisible = prefs.getBoolean("smart_panel_icon_visible", true);
    }

    private void savePosition() {
        if (prefs == null) return;
        prefs.edit()
                .putInt("smart_panel_icon_x", SmartPanelConfig.iconX)
                .putInt("smart_panel_icon_y", SmartPanelConfig.iconY)
                .putString("smart_panel_icon_edge", SmartPanelConfig.iconEdge)
                .putBoolean("smart_panel_icon_visible", SmartPanelConfig.iconVisible)
                .apply();
    }

    private int resolveIconX() {
        int sw = screenWidth();
        int iconPx = dp(ICON_SIZE_DP);
        if ("right".equals(SmartPanelConfig.iconEdge)) return sw - iconPx - SmartPanelConfig.iconX;
        return SmartPanelConfig.iconX;
    }

    private int resolveIconY() {
        if (SmartPanelConfig.iconY < 0) return (screenHeight() - dp(ICON_SIZE_DP)) / 2;
        return SmartPanelConfig.iconY;
    }

    @Override
    public void hide() {
        if (view != null) view.setVisibility(View.GONE);
    }

    @Override
    public void show() {
        if (view != null) view.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean isHidden() {
        if (view != null) return view.getVisibility() == View.GONE;
        return false;
    }

    @Override
    public int[] getCurrentPosition() {
        if (params != null) return new int[]{iconScreenX, iconScreenY};
        return null;
    }

    // ── Tampilkan/sembunyikan ikon (dari notifikasi) ──

    public void hideIcon() {
        SmartPanelConfig.iconVisible = false;
        savePosition();
        stopBreathing();
        if (view != null) view.setVisibility(View.GONE);
        FloatingService.updateNotification();
    }

    public void showIcon() {
        SmartPanelConfig.iconVisible = true;
        savePosition();
        if (view != null) view.setVisibility(View.VISIBLE);
        if (running) {
            startBreathing();
        }
        FloatingService.updateNotification();
    }

    public void toggleIconVisibility() {
        if (SmartPanelConfig.iconVisible) {
            hideIcon();
        } else {
            showIcon();
        }
    }

    public boolean isIconVisible() {
        return SmartPanelConfig.iconVisible;
    }

    private int screenWidth() {
        DisplayMetrics m = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(m);
        return m.widthPixels;
    }

    private int screenHeight() {
        DisplayMetrics m = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(m);
        return m.heightPixels;
    }

    private int dp(float dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }

    private FrameLayout.LayoutParams lpMatch() {
        return new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    private static class PanelRoot extends FrameLayout {
        private Runnable onOutsideTouch;

        PanelRoot(Context c) {
            super(c);
        }

        void setOnOutsideTouch(Runnable r) {
            onOutsideTouch = r;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (event.getActionMasked() == MotionEvent.ACTION_OUTSIDE && onOutsideTouch != null) {
                onOutsideTouch.run();
            }
            return super.onTouchEvent(event);
        }
    }
}