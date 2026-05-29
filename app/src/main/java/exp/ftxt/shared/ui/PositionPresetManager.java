package exp.ftxt.shared.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.text.InputType;
import android.view.MotionEvent;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;

public class PositionPresetManager {

    private final Activity activity;
    private final SharedPreferences prefs;
    private Handler holdHandler;
    private OnPresetActionListener listener;

    private static final int MAX_PRESETS = 10;
    private static final String PREFS_NAME = "ftxt_prefs";
    private static final String KEY_PRESET_COUNT = "preset_count";

    public PositionPresetManager(Activity activity, OnPresetActionListener listener) {
        this.activity = activity;
        this.prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.listener = listener;
    }

    public void showSavePresetDialog(float currentX, float currentY) {
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
            savePreset(name, currentX, currentY);
        });
        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    private void savePreset(String name, float x, float y) {
        int count = prefs.getInt(KEY_PRESET_COUNT, 0);
        int index = count + 1;
        prefs.edit()
                .putInt(KEY_PRESET_COUNT, index)
                .putString("preset_" + index + "_name", name)
                .putFloat("preset_" + index + "_x", x)
                .putFloat("preset_" + index + "_y", y)
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
                                showPresetOptions(pos, names[pos], dialog);
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
            if (listener != null) listener.onPositionLoaded(x, y);
            dialog.dismiss();
        });
    }

    private void showPresetOptions(int position, String name, AlertDialog parentDialog) {
        int idx = position + 1;
        String[] options = {"Muat", "Ganti Nama", "Hapus"};
        new AlertDialog.Builder(activity)
                .setTitle(name)
                .setItems(options, (d, which) -> {
                    switch (which) {
                        case 0:
                            float x = prefs.getFloat("preset_" + idx + "_x", 0.5f);
                            float y = prefs.getFloat("preset_" + idx + "_y", 0.5f);
                            if (listener != null) listener.onPositionLoaded(x, y);
                            parentDialog.dismiss();
                            break;
                        case 1:
                            showRenamePresetDialog(idx, name, parentDialog);
                            break;
                        case 2:
                            confirmDeletePreset(idx, name, parentDialog);
                            break;
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void showRenamePresetDialog(int idx, String currentName, AlertDialog parentDialog) {
        EditText input = new EditText(activity);
        input.setText(currentName);
        input.setSelectAllOnFocus(true);
        new AlertDialog.Builder(activity)
                .setTitle("Ganti Nama Preset")
                .setView(input)
                .setPositiveButton("Simpan", (d, w) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        prefs.edit().putString("preset_" + idx + "_name", newName).apply();
                    }
                    parentDialog.dismiss();
                    showLoadPresetDialog();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void confirmDeletePreset(int idx, String name, AlertDialog parentDialog) {
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

    public void cleanup() {
        if (holdHandler != null) {
            holdHandler.removeCallbacksAndMessages(null);
            holdHandler = null;
        }
    }

    public interface OnPresetActionListener {
        void onPositionLoaded(float x, float y);
    }
}
