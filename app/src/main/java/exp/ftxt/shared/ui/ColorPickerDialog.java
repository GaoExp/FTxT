package exp.ftxt.shared.ui;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import androidx.appcompat.app.AlertDialog;

import exp.ftxt.shared.color.HSVColorPickerView;
import exp.ftxt.R;

public class ColorPickerDialog {

    public interface ColorCallback {
        void onColorSelected(int color);
    }

    public static void show(Activity activity, String title, int initialColor, ColorCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View dialogView = activity.getLayoutInflater()
                .inflate(R.layout.dialog_hsv_color_picker, null);

        HSVColorPickerView colorPicker = dialogView.findViewById(R.id.colorPickerView);
        SeekBar brightnessSeekBar = dialogView.findViewById(R.id.brightnessSeekBar);
        SeekBar alphaSeekBar = dialogView.findViewById(R.id.alphaSeekBar);
        View colorPreview = dialogView.findViewById(R.id.colorPreview);
        Button okButton = dialogView.findViewById(R.id.okButton);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);

        float[] hsv = new float[3];
        Color.colorToHSV(initialColor, hsv);
        colorPicker.setColor(initialColor);
        alphaSeekBar.setProgress(Color.alpha(initialColor));
        brightnessSeekBar.setProgress((int) (hsv[2] * 100));

        updatePreview(colorPreview, colorPicker, brightnessSeekBar, alphaSeekBar);

        colorPicker.setOnColorChangeListener(color ->
                updatePreview(colorPreview, colorPicker, brightnessSeekBar, alphaSeekBar));

        brightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int p, boolean f) {
                updatePreview(colorPreview, colorPicker, brightnessSeekBar, alphaSeekBar);
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        alphaSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int p, boolean f) {
                updatePreview(colorPreview, colorPicker, brightnessSeekBar, alphaSeekBar);
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        builder.setTitle(title);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        okButton.setOnClickListener(v -> {
            int color = getFinalColor(colorPicker, brightnessSeekBar, alphaSeekBar);
            callback.onColorSelected(color);
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private static void updatePreview(View preview, HSVColorPickerView picker,
                                       SeekBar brightness, SeekBar alpha) {
        preview.setBackgroundColor(getFinalColor(picker, brightness, alpha));
    }

    private static int getFinalColor(HSVColorPickerView picker,
                                     SeekBar brightness, SeekBar alpha) {
        int base = picker.getCurrentColor();
        float b = brightness.getProgress() / 100f;
        int a = alpha.getProgress();

        float[] hsv = new float[3];
        Color.colorToHSV(base, hsv);
        hsv[2] *= b;

        int cb = Color.HSVToColor(hsv);
        return Color.argb(a, Color.red(cb), Color.green(cb), Color.blue(cb));
    }
}
