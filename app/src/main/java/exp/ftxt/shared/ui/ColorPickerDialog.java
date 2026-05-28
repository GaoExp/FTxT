package exp.ftxt.shared.ui;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import exp.ftxt.R;
import exp.ftxt.shared.color.ColorNameResolver;

public class ColorPickerDialog {

    public interface ColorCallback {
        void onColorSelected(int color);
    }

    public static void show(Activity activity, String title, int initialColor, ColorCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View dialogView = activity.getLayoutInflater()
                .inflate(R.layout.dialog_hsv_color_picker, null);

        TextView colorPreview = dialogView.findViewById(R.id.colorPreview);
        TextView hexValue = dialogView.findViewById(R.id.hexValue);
        TextView hsvValue = dialogView.findViewById(R.id.hsvValue);
        TextView rgbValue = dialogView.findViewById(R.id.rgbValue);
        ImageButton hexEditButton = dialogView.findViewById(R.id.hexEditButton);
        SeekBar redSeekBar = dialogView.findViewById(R.id.redSeekBar);
        SeekBar greenSeekBar = dialogView.findViewById(R.id.greenSeekBar);
        SeekBar blueSeekBar = dialogView.findViewById(R.id.blueSeekBar);
        SeekBar alphaSeekBar = dialogView.findViewById(R.id.alphaSeekBar);
        TextView redLabel = dialogView.findViewById(R.id.redLabel);
        TextView greenLabel = dialogView.findViewById(R.id.greenLabel);
        TextView blueLabel = dialogView.findViewById(R.id.blueLabel);
        TextView alphaLabel = dialogView.findViewById(R.id.alphaLabel);
        Button okButton = dialogView.findViewById(R.id.okButton);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);

        redSeekBar.setProgress(Color.red(initialColor));
        greenSeekBar.setProgress(Color.green(initialColor));
        blueSeekBar.setProgress(Color.blue(initialColor));
        alphaSeekBar.setProgress(Color.alpha(initialColor));

        updateDisplay(colorPreview, hexValue, hsvValue, rgbValue,
                redLabel, greenLabel, blueLabel, alphaLabel,
                redSeekBar, greenSeekBar, blueSeekBar, alphaSeekBar);

        SeekBar.OnSeekBarChangeListener listener = new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int p, boolean f) {
                updateDisplay(colorPreview, hexValue, hsvValue, rgbValue,
                        redLabel, greenLabel, blueLabel, alphaLabel,
                        redSeekBar, greenSeekBar, blueSeekBar, alphaSeekBar);
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        };

        redSeekBar.setOnSeekBarChangeListener(listener);
        greenSeekBar.setOnSeekBarChangeListener(listener);
        blueSeekBar.setOnSeekBarChangeListener(listener);
        alphaSeekBar.setOnSeekBarChangeListener(listener);

        hexEditButton.setOnClickListener(v -> {
            String currentHex = String.format("#%02X%02X%02X%02X",
                    alphaSeekBar.getProgress(), redSeekBar.getProgress(),
                    greenSeekBar.getProgress(), blueSeekBar.getProgress());

            EditText input = new EditText(activity);
            input.setText(currentHex);
            input.setSelection(input.length());

            new AlertDialog.Builder(activity)
                    .setTitle("Edit HEX")
                    .setView(input)
                    .setPositiveButton("OK", (d, w) -> {
                        try {
                            int color = parseHex(input.getText().toString().trim());
                            alphaSeekBar.setProgress(Color.alpha(color));
                            redSeekBar.setProgress(Color.red(color));
                            greenSeekBar.setProgress(Color.green(color));
                            blueSeekBar.setProgress(Color.blue(color));
                            updateDisplay(colorPreview, hexValue, hsvValue, rgbValue,
                                    redLabel, greenLabel, blueLabel, alphaLabel,
                                    redSeekBar, greenSeekBar, blueSeekBar, alphaSeekBar);
                        } catch (IllegalArgumentException e) {
                            Toast.makeText(activity, "HEX tidak valid", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });

        hexValue.setOnLongClickListener(v -> {
            copyToClipboard(activity, String.format("#%02X%02X%02X%02X",
                    alphaSeekBar.getProgress(), redSeekBar.getProgress(),
                    greenSeekBar.getProgress(), blueSeekBar.getProgress()));
            Toast.makeText(activity, "HEX disalin", Toast.LENGTH_SHORT).show();
            return true;
        });

        hsvValue.setOnLongClickListener(v -> {
            copyToClipboard(activity, hsvValue.getText().toString());
            Toast.makeText(activity, "HSV disalin", Toast.LENGTH_SHORT).show();
            return true;
        });

        rgbValue.setOnLongClickListener(v -> {
            copyToClipboard(activity, rgbValue.getText().toString());
            Toast.makeText(activity, "ARGB disalin", Toast.LENGTH_SHORT).show();
            return true;
        });

        redLabel.setOnClickListener(v -> showValueEditor(activity, "R", redSeekBar, 255,
                colorPreview, hexValue, hsvValue, rgbValue,
                redLabel, greenLabel, blueLabel, alphaLabel,
                redSeekBar, greenSeekBar, blueSeekBar, alphaSeekBar));

        greenLabel.setOnClickListener(v -> showValueEditor(activity, "G", greenSeekBar, 255,
                colorPreview, hexValue, hsvValue, rgbValue,
                redLabel, greenLabel, blueLabel, alphaLabel,
                redSeekBar, greenSeekBar, blueSeekBar, alphaSeekBar));

        blueLabel.setOnClickListener(v -> showValueEditor(activity, "B", blueSeekBar, 255,
                colorPreview, hexValue, hsvValue, rgbValue,
                redLabel, greenLabel, blueLabel, alphaLabel,
                redSeekBar, greenSeekBar, blueSeekBar, alphaSeekBar));

        alphaLabel.setOnClickListener(v -> showValueEditor(activity, "A", alphaSeekBar, 255,
                colorPreview, hexValue, hsvValue, rgbValue,
                redLabel, greenLabel, blueLabel, alphaLabel,
                redSeekBar, greenSeekBar, blueSeekBar, alphaSeekBar));

        builder.setTitle(title);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        okButton.setOnClickListener(v -> {
            int color = Color.argb(alphaSeekBar.getProgress(),
                    redSeekBar.getProgress(),
                    greenSeekBar.getProgress(), blueSeekBar.getProgress());
            callback.onColorSelected(color);
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private static int parseHex(String hex) {
        if (hex.startsWith("#")) hex = hex.substring(1);
        if (hex.length() == 6) hex = "FF" + hex;
        if (hex.length() != 8) throw new IllegalArgumentException();
        return (int) Long.parseLong(hex, 16);
    }

    private static void copyToClipboard(Context context, String text) {
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("color", text));
    }

    private static void showValueEditor(Activity activity, String label, SeekBar bar, int max,
                                          TextView colorPreview, TextView hexValue, TextView hsvValue, TextView rgbValue,
                                          TextView redLabel, TextView greenLabel, TextView blueLabel, TextView alphaLabel,
                                          SeekBar redSeekBar, SeekBar greenSeekBar, SeekBar blueSeekBar, SeekBar alphaSeekBar) {
        EditText input = new EditText(activity);
        input.setText(String.valueOf(bar.getProgress()));
        input.setSelection(input.length());
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

        new AlertDialog.Builder(activity)
                .setTitle("Edit " + label)
                .setView(input)
                .setPositiveButton("OK", (d, w) -> {
                    try {
                        int val = Integer.parseInt(input.getText().toString().trim());
                        if (val < 0 || val > max) {
                            Toast.makeText(activity, "Nilai harus 0-" + max, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        bar.setProgress(val);
                        updateDisplay(colorPreview, hexValue, hsvValue, rgbValue,
                                redLabel, greenLabel, blueLabel, alphaLabel,
                                redSeekBar, greenSeekBar, blueSeekBar, alphaSeekBar);
                    } catch (NumberFormatException e) {
                        Toast.makeText(activity, "Nilai tidak valid", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private static void updateDisplay(TextView preview, TextView hex, TextView hsv, TextView rgb,
                                       TextView rLabel, TextView gLabel, TextView bLabel, TextView aLabel,
                                       SeekBar rBar, SeekBar gBar, SeekBar bBar, SeekBar aBar) {
        int a = aBar.getProgress();
        int r = rBar.getProgress();
        int g = gBar.getProgress();
        int b = bBar.getProgress();
        int color = Color.argb(a, r, g, b);

        preview.setBackgroundColor(color);
        preview.setText(ColorNameResolver.getName(color));

        int textColor = (r * 0.299 + g * 0.587 + b * 0.114) > 128 ? Color.BLACK : Color.WHITE;
        preview.setTextColor(textColor);

        hex.setText(String.format("HEX: #%02X%02X%02X%02X", a, r, g, b));

        float[] hsvArr = new float[3];
        Color.colorToHSV(color, hsvArr);
        hsv.setText(String.format("HSV (%.0f\u00B0, %.0f%%, %.0f%%)",
                hsvArr[0], hsvArr[1] * 100, hsvArr[2] * 100));

        rgb.setText(String.format("ARGB (%d, %d, %d, %d)", a, r, g, b));

        rLabel.setText(String.format("R:%d", r));
        gLabel.setText(String.format("G:%d", g));
        bLabel.setText(String.format("B:%d", b));
        aLabel.setText(String.format("A:%d", a));
    }
}
