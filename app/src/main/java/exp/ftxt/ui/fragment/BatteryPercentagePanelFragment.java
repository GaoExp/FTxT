package exp.ftxt.ui.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import exp.ftxt.MainActivity;
import exp.ftxt.R;
import exp.ftxt.ui.BasePanelFragment;
import exp.ftxt.ui.BatteryPercentagePanelController;

public class BatteryPercentagePanelFragment extends BasePanelFragment {
    private BatteryPercentagePanelController controller;

    @Override
    protected int getLayoutResId() {
        return R.layout.panel_battery_percentage;
    }

    @Override
    protected String getPanelName() {
        return "battery_pct";
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        controller = new BatteryPercentagePanelController((MainActivity) requireActivity(), view);
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
