package exp.ftxt.shared.ui;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import org.json.JSONArray;
import org.json.JSONObject;



public class PositionPresetManager {

    private final Activity activity;
    private final SharedPreferences prefs;
    private Handler holdHandler;
    private OnPresetActionListener listener;
    private OnPresetLoadedListener loadListener;
    private int activePresetIndex = -1;

    private static final int MAX_PRESETS = 50;
    private static final String PREFS_NAME = "ftxt_prefs";
    private static final String KEY_PRESET_COUNT = "preset_count";

    public PositionPresetManager(Activity activity, OnPresetActionListener listener) {
        this.activity = activity;
        this.prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.listener = listener;
    }

    public void setOnPresetLoadedListener(OnPresetLoadedListener loadListener) {
        this.loadListener = loadListener;
    }

    public int getActivePresetIndex() {
        return activePresetIndex;
    }

    public String getActivePresetName() {
        if (activePresetIndex < 0) return null;
        return prefs.getString("preset_" + activePresetIndex + "_name", null);
    }

    public int getPresetCount() {
        return prefs.getInt(KEY_PRESET_COUNT, 0);
    }

    public String getPresetName(int idx) {
        return prefs.getString("preset_" + idx + "_name", "Preset " + idx);
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

        PresetPreviewView preview = new PresetPreviewView(activity);
        preview.setPosition(currentX, currentY);
        preview.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (int) (100 * activity.getResources().getDisplayMetrics().density)));
        preview.setPadding(0, 12, 0, 0);

        LinearLayout container = new LinearLayout(activity);
        container.setOrientation(LinearLayout.VERTICAL);
        container.addView(preview);
        container.addView(input);
        builder.setView(container);

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

    private LinearLayout createContainer() {
        LinearLayout container = new LinearLayout(activity);
        container.setOrientation(LinearLayout.VERTICAL);
        return container;
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
        final float[] xs = new float[count];
        final float[] ys = new float[count];
        for (int i = 0; i < count; i++) {
            int idx = i + 1;
            names[i] = prefs.getString("preset_" + idx + "_name", "Preset " + idx);
            xs[i] = prefs.getFloat("preset_" + idx + "_x", 0.5f);
            ys[i] = prefs.getFloat("preset_" + idx + "_y", 0.5f);
        }

        PresetListAdapter adapter = new PresetListAdapter(activity, names, xs, ys);

        ListView listView = new ListView(activity);
        listView.setAdapter(adapter);

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
                                showPresetOptions(pos + 1, names[pos], dialog);
                            }
                        };
                        holdHandler.postDelayed(holdRunnable[0], 2000);
                    }
                    return false;
                }
                case MotionEvent.ACTION_MOVE:
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (holdRunnable[0] != null) {
                        holdHandler.removeCallbacks(holdRunnable[0]);
                    }
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
            loadPreset(idx, x, y);
            dialog.dismiss();
        });
    }

    public void loadPreset(int idx, float x, float y) {
        if (listener != null) listener.onPositionLoaded(x, y);
        activePresetIndex = idx;
        if (loadListener != null) loadListener.onPresetLoaded(idx);
    }

    private void showPresetOptions(int idx, String name, AlertDialog parentDialog) {
        String[] options = {"Muat", "Visual Editor", "Ganti Nama", "Hapus"};
        new AlertDialog.Builder(activity)
                .setTitle(name)
                .setItems(options, (d, which) -> {
                    switch (which) {
                        case 0:
                            float x = prefs.getFloat("preset_" + idx + "_x", 0.5f);
                            float y = prefs.getFloat("preset_" + idx + "_y", 0.5f);
                            loadPreset(idx, x, y);
                            parentDialog.dismiss();
                            break;
                        case 1:
                            showVisualEditorDialog(idx, name, parentDialog);
                            break;
                        case 2:
                            showRenamePresetDialog(idx, name, parentDialog);
                            break;
                        case 3:
                            confirmDeletePreset(idx, name, parentDialog);
                            break;
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void showVisualEditorDialog(int idx, String currentName, AlertDialog parentDialog) {
        float curX = prefs.getFloat("preset_" + idx + "_x", 0.5f);
        float curY = prefs.getFloat("preset_" + idx + "_y", 0.5f);

        PresetPreviewView editor = new PresetPreviewView(activity);
        editor.setPosition(curX, curY);
        editor.setEditMode(true);
        int size = (int) (250 * activity.getResources().getDisplayMetrics().density);
        editor.setLayoutParams(new ViewGroup.LayoutParams(size, size));

        new AlertDialog.Builder(activity)
                .setTitle("Edit: " + currentName)
                .setView(editor)
                .setPositiveButton("Simpan", (d, w) -> {
                    float newX = curX;
                    float newY = curY;
                    prefs.edit()
                            .putFloat("preset_" + idx + "_x", newX)
                            .putFloat("preset_" + idx + "_y", newY)
                            .apply();
                    if (loadListener != null) loadListener.onPresetChanged();
                    parentDialog.dismiss();
                    showLoadPresetDialog();
                })
                .setNegativeButton("Batal", null)
                .show();

        final float[] lastX = {curX};
        final float[] lastY = {curY};
        editor.setOnPositionChangedListener((x, y) -> {
            lastX[0] = x;
            lastY[0] = y;
        });
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
                    if (activePresetIndex == idx && loadListener != null) {
                        loadListener.onPresetChanged();
                    }
                    parentDialog.dismiss();
                    showLoadPresetDialog();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    public void showExportImportDialog() {
        String[] options = {"Ekspor Preset", "Impor Preset"};
        new AlertDialog.Builder(activity)
                .setTitle("Ekspor / Impor Preset")
                .setItems(options, (d, which) -> {
                    switch (which) {
                        case 0:
                            exportPresets();
                            break;
                        case 1:
                            showImportDialog();
                            break;
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void exportPresets() {
        try {
            int count = prefs.getInt(KEY_PRESET_COUNT, 0);
            JSONArray arr = new JSONArray();
            for (int i = 0; i < count; i++) {
                int idx = i + 1;
                JSONObject obj = new JSONObject();
                obj.put("name", prefs.getString("preset_" + idx + "_name", "Preset " + idx));
                obj.put("x", prefs.getFloat("preset_" + idx + "_x", 0.5f));
                obj.put("y", prefs.getFloat("preset_" + idx + "_y", 0.5f));
                arr.put(obj);
            }
            String json = arr.toString(2);

            ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("FTxT Presets", json);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(activity, "Data preset disalin ke clipboard (" + count + " preset)", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(activity, "Gagal ekspor: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showImportDialog() {
        String[] options = {"Dari Clipboard"};
        new AlertDialog.Builder(activity)
                .setTitle("Impor Preset")
                .setItems(options, (d, which) -> {
                    if (which == 0) importFromClipboard();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void importFromClipboard() {
        ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        if (!clipboard.hasPrimaryClip()) {
            Toast.makeText(activity, "Clipboard kosong", Toast.LENGTH_SHORT).show();
            return;
        }
        ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
        if (item.getText() == null) {
            Toast.makeText(activity, "Clipboard tidak berisi teks", Toast.LENGTH_SHORT).show();
            return;
        }
        importJson(item.getText().toString());
    }

    private void importJson(String json) {
        try {
            JSONArray arr = new JSONArray(json);
            int count = arr.length();
            int existingCount = prefs.getInt(KEY_PRESET_COUNT, 0);

            for (int i = 0; i < count; i++) {
                JSONObject obj = arr.getJSONObject(i);
                String name = obj.getString("name");
                float x = (float) obj.getDouble("x");
                float y = (float) obj.getDouble("y");
                int idx = existingCount + i + 1;
                if (idx > MAX_PRESETS) break;
                prefs.edit()
                        .putString("preset_" + idx + "_name", name)
                        .putFloat("preset_" + idx + "_x", x)
                        .putFloat("preset_" + idx + "_y", y)
                        .apply();
            }

            int newCount = Math.min(existingCount + count, MAX_PRESETS);
            prefs.edit().putInt(KEY_PRESET_COUNT, newCount).apply();

            Toast.makeText(activity, "Berhasil impor " + (newCount - existingCount) + " preset", Toast.LENGTH_SHORT).show();
            if (loadListener != null) loadListener.onPresetChanged();
        } catch (Exception e) {
            Toast.makeText(activity, "Gagal impor: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void confirmDeletePreset(int idx, String name, AlertDialog parentDialog) {
        new AlertDialog.Builder(activity)
                .setTitle("Hapus Preset")
                .setMessage("Hapus preset \"" + name + "\"?")
                .setPositiveButton("Ya", (d, w) -> {
                    deletePreset(idx);
                    if (activePresetIndex == idx) {
                        activePresetIndex = -1;
                        if (loadListener != null) loadListener.onPresetChanged();
                    }
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

    public interface OnPresetLoadedListener {
        void onPresetLoaded(int idx);
        void onPresetChanged();
    }

    private static class PresetListAdapter extends BaseAdapter {
        private final Context context;
        private final String[] names;
        private final float[] xs;
        private final float[] ys;

        PresetListAdapter(Context context, String[] names, float[] xs, float[] ys) {
            this.context = context;
            this.names = names;
            this.xs = xs;
            this.ys = ys;
        }

        @Override
        public int getCount() { return names.length; }

        @Override
        public Object getItem(int position) { return names[position]; }

        @Override
        public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = new LinearLayout(context);
                LinearLayout root = (LinearLayout) convertView;
                root.setOrientation(LinearLayout.HORIZONTAL);
                root.setPadding(8, 8, 8, 8);
                root.setGravity(Gravity.CENTER_VERTICAL);

                // Preview container
                FrameLayout previewContainer = new FrameLayout(context);
                previewContainer.setBackgroundColor(Color.DKGRAY);
                LinearLayout.LayoutParams previewParams = new LinearLayout.LayoutParams(
                        (int)(48 * context.getResources().getDisplayMetrics().density),
                        (int)(48 * context.getResources().getDisplayMetrics().density));
                previewParams.setMarginEnd((int)(12 * context.getResources().getDisplayMetrics().density));
                previewContainer.setLayoutParams(previewParams);
                root.addView(previewContainer, 0);

                // Text container
                LinearLayout textContainer = new LinearLayout(context);
                textContainer.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, 
                        LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                textContainer.setLayoutParams(textParams);

                // Name TextView
                TextView nameView = new TextView(context);
                nameView.setTextSize(14);
                nameView.setTextColor(Color.WHITE);
                nameView.setTypeface(null, Typeface.BOLD);
                textContainer.addView(nameView);

                // Coords TextView
                TextView coordView = new TextView(context);
                coordView.setTextSize(11);
                coordView.setTextColor(0xFF888888);
                textContainer.addView(coordView);

                root.addView(textContainer, 1);

                // Store holder for reuse
                holder = new ViewHolder();
                holder.previewContainer = previewContainer;
                holder.nameView = nameView;
                holder.coordView = coordView;
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.previewContainer.removeAllViews();

            // Create and add PresetPreviewView programmatically
            PresetPreviewView preview = new PresetPreviewView(context);
            preview.setPosition(xs[position], ys[position]);
            holder.previewContainer.addView(preview, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));

            holder.nameView.setText(names[position]);
            holder.coordView.setText(String.format("X: %.2f  Y: %.2f", xs[position], ys[position]));

            return convertView;
        }

        // Inner class for ViewHolder pattern
        private static class ViewHolder {
            FrameLayout previewContainer;
            TextView nameView;
            TextView coordView;
        }
    }
}
