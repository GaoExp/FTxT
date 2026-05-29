package exp.ftxt.shared.ui;

import android.widget.SeekBar;
import android.widget.TextView;

public class SliderPositionController {

    private SeekBar posXSeekBar;
    private SeekBar posYSeekBar;
    private TextView posXLabel;
    private TextView posYLabel;
    private OnPositionListener listener;
    private boolean isUpdating = false;
    private float currentX, currentY;

    private static final int SCALE = 1000;

    public SliderPositionController(SeekBar posX, SeekBar posY, TextView labelX, TextView labelY, OnPositionListener listener) {
        this.posXSeekBar = posX;
        this.posYSeekBar = posY;
        this.posXLabel = labelX;
        this.posYLabel = labelY;
        this.listener = listener;
        setupListeners();
    }

    private void setupListeners() {
        posXSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (isUpdating || !fromUser) return;
                if (listener != null) listener.onPositionChanged(progress / (float) SCALE, currentY);
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        posYSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (isUpdating || !fromUser) return;
                if (listener != null) listener.onPositionChanged(currentX, progress / (float) SCALE);
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });
    }

    public void sync(float x, float y) {
        isUpdating = true;
        currentX = x;
        currentY = y;
        posXSeekBar.setProgress((int) (x * SCALE));
        posYSeekBar.setProgress((int) (y * SCALE));
        posXLabel.setText(String.format("X: %.2f", x));
        posYLabel.setText(String.format("Y: %.2f", y));
        isUpdating = false;
    }

    public interface OnPositionListener {
        void onPositionChanged(float x, float y);
    }
}
