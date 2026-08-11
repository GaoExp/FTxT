package exp.ftxt.ui.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import exp.ftxt.MainActivity;
import exp.ftxt.R;
import exp.ftxt.ui.BatteryBarPanelController;
import exp.ftxt.ui.BasePanelFragment;

public class BatteryBarPanelFragment extends BasePanelFragment {
    private BatteryBarPanelController controller;

    @Override
    protected int getLayoutResId() {
        return R.layout.panel_battery_bar;
    }

    @Override
    protected String getPanelName() {
        return "battery_bar";
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        controller = new BatteryBarPanelController((MainActivity) requireActivity(), view);
    }

    @Override
    public void onPanelShown() {
        if (controller != null) controller.onPanelShown();
    }

    @Override
    public void showLoadPresetDialog() {
        if (controller != null) controller.showLoadPresetDialog();
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
