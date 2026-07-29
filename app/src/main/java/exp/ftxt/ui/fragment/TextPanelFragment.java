package exp.ftxt.ui.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import exp.ftxt.MainActivity;
import exp.ftxt.R;
import exp.ftxt.ui.BasePanelFragment;
import exp.ftxt.ui.TextPanelController;

public class TextPanelFragment extends BasePanelFragment {
    private TextPanelController controller;

    @Override
    protected int getLayoutResId() {
        return R.layout.panel_text;
    }

    @Override
    protected String getPanelName() {
        return "text";
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        controller = new TextPanelController((MainActivity) requireActivity(), view);
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
