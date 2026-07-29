package exp.ftxt.ui.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import exp.ftxt.MainActivity;
import exp.ftxt.R;
import exp.ftxt.ui.BasePanelFragment;
import exp.ftxt.ui.BatteryCurrentPanelController;

public class BatteryCurrentPanelFragment extends BasePanelFragment {
    private BatteryCurrentPanelController controller;

    @Override
    protected int getLayoutResId() {
        return R.layout.panel_battery_current;
    }

    @Override
    protected String getPanelName() {
        return "battery_cur";
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        controller = new BatteryCurrentPanelController((MainActivity) requireActivity(), view);
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
