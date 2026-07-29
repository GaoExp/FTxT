package exp.ftxt.ui.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import exp.ftxt.MainActivity;
import exp.ftxt.R;
import exp.ftxt.ui.BasePanelFragment;
import exp.ftxt.ui.ColorPickerPanelController;

public class ColorPickerPanelFragment extends BasePanelFragment {
    private ColorPickerPanelController controller;

    @Override
    protected int getLayoutResId() {
        return R.layout.panel_color_picker;
    }

    @Override
    protected String getPanelName() {
        return "color_picker";
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        controller = new ColorPickerPanelController((MainActivity) requireActivity(), view);
    }

    @Override
    public void onDestroyView() {
        if (controller != null) {
            controller.cleanup();
            controller = null;
        }
        super.onDestroyView();
    }
}
