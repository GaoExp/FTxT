package exp.ftxt.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Handler;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import exp.ftxt.R;
import exp.ftxt.core.FloatingService;
import exp.ftxt.features.text.TextConfig;
import exp.ftxt.shared.ui.XyPadView;

public class PositionController {

    private final Activity activity;
    private final SharedPreferences prefs;

    private SeekBar posXSeekBar;
    private SeekBar posYSeekBar;
    private TextView posXLabel;
    private TextView posYLabel;
    private View btnUp, btnDown, btnLeft, btnRight;
    private XyPadView xyPad;
    private View btnPortrait, btnLandscape;
    private String currentOrientation;

    private boolean isUpdating = false;
    private final Handler repeatHandler = new Handler();
    private Runnable repeatRunnable;
    private Handler holdHandler;

    private static final float DPAD_STEP = 0.01f;
    private static final int REPEAT_INTERVAL = 100;

    private static final int MAX_PRESETS = 10;
    private static final String PREFS_NAME = "ftxt_prefs";
    private static final String KEY_PRESET_COUNT = "preset_count";

    public PositionController(Activity activity) {
        this.activity = activity;
        this.prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        int orientation = activity.getResources().getConfiguration().orientation;
        currentOrientation = (orientation == Configuration.ORIENTATION_LANDSCAPE) ? "land" : "port";
        loadPositionFromPrefs(currentOrientation);

        FloatingService.setTextOrientationSuffixStatic(currentOrientation);

        bindViews();
        setupListeners();
        syncAll();
    }

    private void bindViews() {
        posXSeekBar = activity.findViewById(R.id.posXSeekBar);
        posYSeekBar = activity.findViewById(R.id.posYSeekBar);
        posXLabel = activity.findViewById(R.id.posXLabel);
        posYLabel = activity.findViewById(R.id.posYLabel);
        btnUp = activity.findViewById(R.id.btnUp);
        btnDown = activity.findViewById(R.id.btnDown);
        btnLeft = activity.findViewById(R.id.btnLeft);
        btnRight = activity.findViewById(R.id.btnRight);
        xyPad = activity.findViewById(R.id.xyPad);
        btnPortrait = activity.findViewById(R.id.btnPortrait);
        btnLandscape = activity.findViewById(R.id.btnLandscape);
    }

    private void setupListeners() {
        posXSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (isUpdating || !fromUser) return;
                onPositionChanged(progress / 1000f, TextConfig.posY);
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        posYSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (isUpdating || !fromUser) return;
                onPositionChanged(TextConfig.posX, progress / 1000f);
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        xyPad.setOnPositionChangeListener((x, y) -> {
            if (isUpdating) return;
            onPositionChanged(x, y);
        });

        setupDpadRepeat(btnUp, 0, -DPAD_STEP);
        setupDpadRepeat(btnDown, 0, DPAD_STEP);
        setupDpadRepeat(btnLeft, -DPAD_STEP, 0);
        setupDpadRepeat(btnRight, DPAD_STEP, 0);

        View btnSavePreset = activity.findViewById(R.id.btnSavePreset);
        View btnLoadPreset = activity.findViewById(R.id.btnLoadPreset);
        View btnResetPos = activity.findViewById(R.id.btnResetPos);
        if (btnSavePreset != null) {
            btnSavePreset.setOnClickListener(v -> showSavePresetDialog());
        }
        if (btnLoadPreset != null) {
            btnLoadPreset.setOnClickListener(v -> showLoadPresetDialog());
        }
        if (btnResetPos != null) {
            btnResetPos.setOnClickListener(v -> resetPosition());
        }

        btnPortrait.setOnClickListener(v -> setOrientationMode("port"));
        btnLandscape.setOnClickListener(v -> setOrientationMode("land"));
        updateOrientationButtons();
    }

    private void setupDpadRepeat(View button, float dx, float dy) {
        button.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    onPositionChanged(clamp(TextConfig.posX + dx), clamp(TextConfig.posY + dy));
                    startRepeat(dx, dy);
                    v.setPressed(true);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    stopRepeat();
                    v.setPressed(false);
                    return true;
            }
            return false;
        });
    }

    private void startRepeat(float dx, float dy) {
        stopRepeat();
        repeatRunnable = () -> {
            onPositionChanged(clamp(TextConfig.posX + dx), clamp(TextConfig.posY + dy));
            repeatHandler.postDelayed(this.repeatRunnable, REPEAT_INTERVAL);
        };
        repeatHandler.postDelayed(repeatRunnable, REPEAT_INTERVAL);
    }

    private void stopRepeat() {
        if (repeatRunnable != null) {
            repeatHandler.removeCallbacks(repeatRunnable);
            repeatRunnable = null;
        }
    }

    private static float clamp(float val) {
        return Math.max(0, Math.min(1, val));
    }

    private void onPositionChanged(float x, float y) {
        TextConfig.posX = x;
        TextConfig.posY = y;
        syncAll();
        savePositionToPrefs(currentOrientation);
        FloatingService.updateTextPositionStatic();
    }

    // ====================================================================
    // Preset system
    // ====================================================================

    public void resetPosition() {
        onPositionChanged(0.5f, 0.5f);
    }

    public void showSavePresetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Simpan Preset Posisi");

        int count = prefs.getInt(KEY_PRESET_COUNT, 0);
        if (count >= MAX_PRESETS) {
            builder.setMessage("Maksimal " + MAX_PRESETS + " preset. Hapus preset lama terlebih dahulu.");
            builder.setPositiveButton("OK", null);
            builder.show();
            return;
        }

        EditText input = new EditText(activity);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Nama preset (misal: Pojok Kanan)");
        builder.setView(input);

        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (name.isEmpty()) name = "Preset " + (count + 1);
            savePreset(name);
        });
        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    private void savePreset(String name) {
        int count = prefs.getInt(KEY_PRESET_COUNT, 0);
        int index = count + 1;
        prefs.edit()
                .putInt(KEY_PRESET_COUNT, index)
                .putString("preset_" + index + "_name", name)
                .putFloat("preset_" + index + "_x", TextConfig.posX)
                .putFloat("preset_" + index + "_y", TextConfig.posY)
                .apply();
    }

    public void showLoadPresetDialog() {
        final int count = prefs.getInt(KEY_PRESET_COUNT, 0);
        if (count == 0) {
            new AlertDialog.Builder(activity)
                    .setTitle("Muat Preset")
                    .setMessage("Belum ada preset tersimpan.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        final String[] names = new String[count];
        for (int i = 0; i < count; i++) {
            names[i] = prefs.getString("preset_" + (i + 1) + "_name", "Preset " + (i + 1));
        }

        ListView listView = new ListView(activity);
        listView.setAdapter(new ArrayAdapter<>(activity,
                android.R.layout.simple_list_item_1, names));

        final AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle("Muat Preset")
                .setView(listView)
                .setNegativeButton("Batal", null)
                .show();

        holdHandler = new Handler();
        final int[] heldPos = {-1};
        final boolean[] longPressFired = {false};
        final Runnable[] holdRunnable = {null};

        listView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    int pos = listView.pointToPosition((int) event.getX(), (int) event.getY());
                    if (pos >= 0) {
                        heldPos[0] = pos;
                        holdRunnable[0] = () -> {
                            if (heldPos[0] == pos) {
                                longPressFired[0] = true;
                                confirmDeletePreset(pos, names[pos], dialog);
                            }
                        };
                        holdHandler.postDelayed(holdRunnable[0], 2000);
                    }
                    return false;
                }
                case MotionEvent.ACTION_MOVE:
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    holdHandler.removeCallbacks(holdRunnable[0]);
                    heldPos[0] = -1;
                    return false;
            }
            return false;
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (longPressFired[0]) {
                longPressFired[0] = false;
                return;
            }
            int idx = position + 1;
            float x = prefs.getFloat("preset_" + idx + "_x", 0.5f);
            float y = prefs.getFloat("preset_" + idx + "_y", 0.5f);
            onPositionChanged(x, y);
            dialog.dismiss();
        });
    }

    private void confirmDeletePreset(int position, String name, AlertDialog parentDialog) {
        int idx = position + 1;
        new AlertDialog.Builder(activity)
                .setTitle("Hapus Preset")
                .setMessage("Hapus preset \"" + name + "\"?")
                .setPositiveButton("Ya", (d, w) -> {
                    deletePreset(idx);
                    parentDialog.dismiss();
                    showLoadPresetDialog();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void deletePreset(int idx) {
        int count = prefs.getInt(KEY_PRESET_COUNT, 0);
        for (int i = idx; i < count; i++) {
            int next = i + 1;
            String nextName = prefs.getString("preset_" + next + "_name", "");
            float nextX = prefs.getFloat("preset_" + next + "_x", 0.5f);
            float nextY = prefs.getFloat("preset_" + next + "_y", 0.5f);
            prefs.edit()
                    .putString("preset_" + i + "_name", nextName)
                    .putFloat("preset_" + i + "_x", nextX)
                    .putFloat("preset_" + i + "_y", nextY)
                    .apply();
        }
        prefs.edit()
                .remove("preset_" + count + "_name")
                .remove("preset_" + count + "_x")
                .remove("preset_" + count + "_y")
                .putInt(KEY_PRESET_COUNT, count - 1)
                .apply();
    }

    // ====================================================================
    // Orientation mode — Potret / Lanskap
    // ====================================================================

    private void setOrientationMode(String mode) {
        if (mode.equals(currentOrientation)) return;

        savePositionToPrefs(currentOrientation);

        currentOrientation = mode;
        FloatingService.setTextOrientationSuffixStatic(mode);
        loadPositionFromPrefs(mode);

        syncAll();
        FloatingService.updateTextPositionStatic();
        updateOrientationButtons();
    }

    private void savePositionToPrefs(String orient) {
        String sfx = "_" + orient;
        prefs.edit()
                .putFloat("text_pos_x" + sfx, TextConfig.posX)
                .putFloat("text_pos_y" + sfx, TextConfig.posY)
                .apply();
    }

    private void loadPositionFromPrefs(String orient) {
        String sfx = "_" + orient;
        TextConfig.posX = prefs.getFloat("text_pos_x" + sfx, 0.5f);
        TextConfig.posY = prefs.getFloat("text_pos_y" + sfx, 0.5f);
    }

    private void updateOrientationButtons() {
        btnPortrait.setSelected("port".equals(currentOrientation));
        btnLandscape.setSelected("land".equals(currentOrientation));
    }

    // ====================================================================

    public void cleanup() {
        stopRepeat();
        if (holdHandler != null) {
            holdHandler.removeCallbacksAndMessages(null);
            holdHandler = null;
        }
    }

    public void refresh() {
        syncAll();
    }

    public void syncAll() {
        isUpdating = true;

        int px = (int) (TextConfig.posX * 1000);
        int py = (int) (TextConfig.posY * 1000);
        posXSeekBar.setProgress(px);
        posYSeekBar.setProgress(py);
        posXLabel.setText(String.format("X: %.2f", TextConfig.posX));
        posYLabel.setText(String.format("Y: %.2f", TextConfig.posY));
        xyPad.setPosition(TextConfig.posX, TextConfig.posY);

        isUpdating = false;
    }
}
