package exp.ftxt.shared.preset;

import android.app.Activity;
import android.content.ClipData;
import android.graphics.Color;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.CheckBox;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import exp.ftxt.R;

public class PresetBrowserDialog extends DialogFragment {

    public interface OnPresetSelectedListener {
        void onPresetSelected(String name);
    }

    private final Activity activity;
    private final OnPresetSelectedListener listener;
    private final Runnable onDismissListener;
    private final Consumer<Runnable> onSaveClick;

    private List<Map<String, Object>> fullMetadata;
    private List<Map<String, Object>> filteredMetadata;
    private PresetAdapter adapter;
    private ListView listView;
    private TextView emptyHint;
    private TextInputEditText searchBar;
    private int lastSelectedPos = -1;
    private boolean selectMode;
    private final Set<Integer> checkedSet = new HashSet<>();
    private String draggedName;
    private Button btnTandai;
    private Button btnTandaiSemua;
    private View btnSimpan, btnImport, btnHapus, btnFavorit, btnBagikan, btnEkspor;

    private ActivityResultLauncher<String[]> importLauncher;

    public PresetBrowserDialog(Activity activity, OnPresetSelectedListener listener, Runnable onDismissListener) {
        this(activity, listener, onDismissListener, null);
    }

    public PresetBrowserDialog(Activity activity, OnPresetSelectedListener listener, Runnable onDismissListener, Consumer<Runnable> onSaveClick) {
        this.activity = activity;
        this.listener = listener;
        this.onDismissListener = onDismissListener;
        this.onSaveClick = onSaveClick;
    }

    @Override
    public void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        importLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri == null) return;
                    int count = PresetManager.importFromFile(activity, uri);
                    Toast.makeText(activity, "Berhasil impor " + count + " preset", Toast.LENGTH_SHORT).show();
                    refreshData();
                }
        );
    }

    @Override
    public android.app.Dialog onCreateDialog(android.os.Bundle savedInstanceState) {
        View view = LayoutInflater.from(activity).inflate(R.layout.dialog_preset_browser, null, false);

        searchBar = view.findViewById(R.id.searchBar);
        listView = view.findViewById(R.id.presetList);
        emptyHint = view.findViewById(R.id.emptyHint);
        btnTandai = view.findViewById(R.id.btnTandai);
        btnTandaiSemua = view.findViewById(R.id.btnTandaiSemua);
        btnSimpan = view.findViewById(R.id.btnSimpan);
        btnImport = view.findViewById(R.id.btnImport);
        btnHapus = view.findViewById(R.id.btnHapus);
        btnFavorit = view.findViewById(R.id.btnFavorit);
        btnBagikan = view.findViewById(R.id.btnBagikan);
        btnEkspor = view.findViewById(R.id.btnEkspor);

        fullMetadata = PresetManager.getIndexMetadata(activity);
        filteredMetadata = new ArrayList<>(fullMetadata);

        adapter = new PresetAdapter();
        listView.setAdapter(adapter);

        searchBar.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        listView.setOnItemClickListener((parent, v, position, id) -> {
            if (selectMode) {
                CheckBox chk = v.findViewById(R.id.chkSelect);
                if (chk != null) {
                    chk.setChecked(!chk.isChecked());
                    if (chk.isChecked()) checkedSet.add(position);
                    else checkedSet.remove(position);
                    btnTandai.setText(checkedSet.isEmpty() ? "Tandai" : "Tandai (" + checkedSet.size() + ")");
                }
                return;
            }
            lastSelectedPos = position;
            Map<String, Object> item = filteredMetadata.get(position);
            showItemMenu(v, item);
        });

        listView.setOnItemLongClickListener((parent, v, position, id) -> {
            if (selectMode) return false;
            draggedName = (String) filteredMetadata.get(position).get("name");
            ClipData data = ClipData.newPlainText("preset_name", draggedName);
            v.startDragAndDrop(data, new View.DragShadowBuilder(v), v, 0);
            return true;
        });

        listView.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return true;
                case DragEvent.ACTION_DRAG_LOCATION: {
                    int overPos = listView.pointToPosition((int) event.getX(), (int) event.getY());
                    if (overPos >= 0) {
                        String overName = (String) filteredMetadata.get(overPos).get("name");
                        if (!overName.equals(draggedName)) {
                            PresetManager.reorder(activity, draggedName, overPos);
                            refreshData();
                        }
                    }
                    return true;
                }
                case DragEvent.ACTION_DROP:
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    draggedName = null;
                    return true;
            }
            return false;
        });

        if (onSaveClick != null) {
            btnSimpan.setOnClickListener(v -> onSaveClick.accept(this::refreshData));
        } else {
            btnSimpan.setVisibility(View.GONE);
        }

        btnTandaiSemua.setOnClickListener(v -> {
            boolean allChecked = checkedSet.size() == filteredMetadata.size();
            checkedSet.clear();
            if (!allChecked) {
                for (int i = 0; i < filteredMetadata.size(); i++) checkedSet.add(i);
            }
            updateBottomBar();
            adapter.notifyDataSetChanged();
        });

        btnTandai.setOnClickListener(v -> {
            selectMode = !selectMode;
            if (!selectMode) checkedSet.clear();
            updateBottomBar();
            adapter.notifyDataSetChanged();
        });

        btnHapus.setOnClickListener(v -> {
            if (checkedSet.isEmpty()) return;
            List<String> toDelete = new ArrayList<>();
            for (int pos : checkedSet) {
                if (pos < filteredMetadata.size())
                    toDelete.add((String) filteredMetadata.get(pos).get("name"));
            }
            new AlertDialog.Builder(activity)
                    .setTitle("Hapus Preset")
                    .setMessage("Hapus " + toDelete.size() + " preset terpilih?")
                    .setPositiveButton("Ya", (d, w) -> {
                        PresetManager.deleteMultiple(activity, toDelete);
                        Toast.makeText(activity, toDelete.size() + " preset dihapus", Toast.LENGTH_SHORT).show();
                        exitSelectMode();
                        refreshData();
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });

        btnFavorit.setOnClickListener(v -> {
            if (checkedSet.isEmpty()) return;
            for (int pos : checkedSet) {
                if (pos < filteredMetadata.size()) {
                    Map<String, Object> item = filteredMetadata.get(pos);
                    String name = (String) item.get("name");
                    boolean fav = (Boolean) item.getOrDefault("favorite", false);
                    PresetManager.setFavorite(activity, name, !fav);
                }
            }
            Toast.makeText(activity, "Favorit " + checkedSet.size() + " preset diubah", Toast.LENGTH_SHORT).show();
            exitSelectMode();
            refreshData();
        });

        btnBagikan.setOnClickListener(v -> {
            if (checkedSet.isEmpty()) return;
            for (int pos : checkedSet) {
                if (pos < filteredMetadata.size()) {
                    String name = (String) filteredMetadata.get(pos).get("name");
                    PresetManager.sharePreset(activity, name);
                }
            }
            exitSelectMode();
        });

        btnEkspor.setOnClickListener(v -> {
            if (checkedSet.isEmpty()) return;
            String filename = "ftxt_presets_" + System.currentTimeMillis() + ".txt";
            if (!PresetManager.exportToFile(activity, filename)) {
                Toast.makeText(activity, "Gagal mengekspor", Toast.LENGTH_SHORT).show();
            }
            exitSelectMode();
        });

        btnImport.setOnClickListener(v -> importLauncher.launch(new String[]{"text/plain"}));

        updateEmptyState();

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .create();
        dialog.setView(view, 0, 0, 0, 0);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setOnShowListener(d -> {
            int itemHeight = (int) (activity.getResources().getDisplayMetrics().density * 40 + 0.5f);
            int maxHeight = itemHeight * 7 + 6;
            ViewGroup wrapper = view.findViewById(R.id.listWrapper);
            if (wrapper.getHeight() > maxHeight) {
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) wrapper.getLayoutParams();
                lp.weight = 0;
                lp.height = maxHeight;
                wrapper.setLayoutParams(lp);
            }
        });
        return dialog;
    }

    @Override
    public void onDismiss(android.content.DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) onDismissListener.run();
    }

    private void filter(String query) {
        filteredMetadata.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredMetadata.addAll(fullMetadata);
        } else {
            String q = query.toLowerCase();
            for (Map<String, Object> item : fullMetadata) {
                String name = (String) item.get("name");
                if (name != null && name.toLowerCase().contains(q)) {
                    filteredMetadata.add(item);
                    continue;
                }
                @SuppressWarnings("unchecked")
                List<String> tags = (List<String>) item.get("tags");
                if (tags != null) {
                    for (String tag : tags) {
                        if (tag.toLowerCase().contains(q)) {
                            filteredMetadata.add(item);
                            break;
                        }
                    }
                }
            }
        }
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        boolean empty = filteredMetadata.isEmpty();
        listView.setVisibility(empty ? View.GONE : View.VISIBLE);
        emptyHint.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    private void updateBottomBar() {
        btnTandai.setText(selectMode ? "Tandai (" + checkedSet.size() + ")" : "Tandai");
        btnTandaiSemua.setVisibility(selectMode ? View.VISIBLE : View.GONE);
        btnSimpan.setVisibility(selectMode ? View.GONE : (onSaveClick != null ? View.VISIBLE : View.GONE));
        btnImport.setVisibility(selectMode ? View.GONE : View.VISIBLE);
        btnHapus.setVisibility(selectMode ? View.VISIBLE : View.GONE);
        btnFavorit.setVisibility(selectMode ? View.VISIBLE : View.GONE);
        btnBagikan.setVisibility(selectMode ? View.VISIBLE : View.GONE);
        btnEkspor.setVisibility(selectMode ? View.VISIBLE : View.GONE);
    }

    private void exitSelectMode() {
        selectMode = false;
        checkedSet.clear();
        updateBottomBar();
        adapter.notifyDataSetChanged();
    }

    private void refreshData() {
        fullMetadata = PresetManager.getIndexMetadata(activity);
        filter(searchBar.getText().toString());
    }

    private void showItemMenu(View anchor, Map<String, Object> item) {
        String name = (String) item.get("name");
        boolean favorite = (Boolean) item.getOrDefault("favorite", false);

        PopupMenu popup = new PopupMenu(activity, anchor);
        popup.getMenu().add(0, 7, 0, "Gunakan Preset");
        popup.getMenu().add(0, 1, 0, "Ganti Nama");
        popup.getMenu().add(0, 2, 0, "Hapus");
        popup.getMenu().add(0, 3, 0, favorite ? "Batal Favorite" : "Favorite");
        popup.getMenu().add(0, 4, 0, "Pindah Atas");
        popup.getMenu().add(0, 5, 0, "Pindah Bawah");
        popup.getMenu().add(0, 6, 0, "Bagikan");

        popup.setOnMenuItemClickListener(itemMenu -> {
            switch (itemMenu.getItemId()) {
                case 7:
                    if (listener != null) listener.onPresetSelected(name);
                    dismiss();
                    return true;
                case 1:
                    showRenameDialog(name);
                    return true;
                case 2:
                    showDeleteDialog(name);
                    return true;
                case 3:
                    toggleFavorite(name, !favorite);
                    return true;
                case 4:
                    moveUp(name);
                    return true;
                case 5:
                    moveDown(name);
                    return true;
                case 6:
                    PresetManager.sharePreset(activity, name);
                    return true;
            }
            return false;
        });
        popup.show();
    }

    private void showRenameDialog(String oldName) {
        EditText input = new EditText(activity);
        input.setText(oldName);
        input.setSelection(oldName.length());

        new AlertDialog.Builder(activity)
                .setTitle("Ganti Nama Preset")
                .setView(input)
                .setPositiveButton("Simpan", (d, w) -> {
                    String newName = input.getText().toString().trim();
                    if (newName.isEmpty()) {
                        Toast.makeText(activity, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (PresetManager.rename(activity, oldName, newName)) {
                        Toast.makeText(activity, "Preset diganti nama", Toast.LENGTH_SHORT).show();
                        refreshData();
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void showDeleteDialog(String name) {
        new AlertDialog.Builder(activity)
                .setTitle("Hapus Preset")
                .setMessage("Yakin ingin menghapus preset \"" + name + "\"?")
                .setPositiveButton("Ya", (d, w) -> {
                    PresetManager.delete(activity, name);
                    Toast.makeText(activity, "Preset \"" + name + "\" dihapus", Toast.LENGTH_SHORT).show();
                    refreshData();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void toggleFavorite(String name, boolean fav) {
        PresetManager.setFavorite(activity, name, fav);
        refreshData();
    }

    private void moveUp(String name) {
        if (PresetManager.moveUp(activity, name)) {
            refreshData();
        } else {
            Toast.makeText(activity, "Sudah di posisi teratas", Toast.LENGTH_SHORT).show();
        }
    }

    private void moveDown(String name) {
        if (PresetManager.moveDown(activity, name)) {
            refreshData();
        } else {
            Toast.makeText(activity, "Sudah di posisi terbawah", Toast.LENGTH_SHORT).show();
        }
    }

    // ====================================================================
    // ADAPTER
    // ====================================================================

    private class PresetAdapter extends BaseAdapter {
        @Override
        public int getCount() { return filteredMetadata.size(); }

        @Override
        public Object getItem(int pos) { return filteredMetadata.get(pos); }

        @Override
        public long getItemId(int pos) { return pos; }

        @Override
        public View getView(int pos, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(activity)
                        .inflate(R.layout.preset_browser_item, parent, false);
            }

            Map<String, Object> item = filteredMetadata.get(pos);
            String name = (String) item.get("name");

            View thumb = convertView.findViewById(R.id.thumbColor);
            TextView txtName = convertView.findViewById(R.id.txtName);
            TextView txtTags = convertView.findViewById(R.id.txtTags);
            ImageView imgFav = convertView.findViewById(R.id.imgFavorite);
            CheckBox chk = convertView.findViewById(R.id.chkSelect);

            txtName.setText(name);

            int color = 0;
            if (item.containsKey("color")) {
                color = (Integer) item.get("color");
            }
            thumb.setBackgroundColor(color != 0 ? color : Color.GRAY);

            @SuppressWarnings("unchecked")
            List<String> tags = (List<String>) item.get("tags");
            if (tags != null && !tags.isEmpty()) {
                txtTags.setText(String.join(", ", tags));
                txtTags.setVisibility(View.VISIBLE);
            } else {
                txtTags.setVisibility(View.GONE);
            }

            boolean favorite = false;
            if (item.containsKey("favorite")) {
                favorite = (Boolean) item.get("favorite");
            }
            imgFav.setImageResource(favorite ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);

            chk.setVisibility(selectMode ? View.VISIBLE : View.GONE);
            chk.setChecked(checkedSet.contains(pos));

            return convertView;
        }
    }
}
