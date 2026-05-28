package exp.ftxt.shared.ui;

import android.app.Activity;
import android.text.InputType;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

public class SliderLabelEditor {

    public static void showSliderEditor(Activity activity, String title, SeekBar bar, int max, TextView label, String prefix) {
        EditText input = new EditText(activity);
        input.setText(String.valueOf(bar.getProgress()));
        input.setSelection(input.length());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);

        new AlertDialog.Builder(activity)
                .setTitle("Edit " + title)
                .setView(input)
                .setPositiveButton("OK", (d, w) -> {
                    try {
                        int val = Integer.parseInt(input.getText().toString().trim());
                        if (val < 0 || val > max) {
                            Toast.makeText(activity, "Nilai harus 0-" + max, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        bar.setProgress(val);
                        label.setText(prefix + val);
                    } catch (NumberFormatException e) {
                        Toast.makeText(activity, "Nilai tidak valid", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    public static void showOffsetEditor(Activity activity, String title, SeekBar bar, TextView label, String prefix) {
        EditText input = new EditText(activity);
        int currentOffset = bar.getProgress() - 60;
        input.setText(String.valueOf(currentOffset));
        input.setSelection(input.length());
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);

        new AlertDialog.Builder(activity)
                .setTitle("Edit " + title)
                .setView(input)
                .setPositiveButton("OK", (d, w) -> {
                    try {
                        int val = Integer.parseInt(input.getText().toString().trim());
                        if (val < -60 || val > 60) {
                            Toast.makeText(activity, "Nilai harus -60 hingga 60", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        bar.setProgress(val + 60);
                    } catch (NumberFormatException e) {
                        Toast.makeText(activity, "Nilai tidak valid", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }
}
