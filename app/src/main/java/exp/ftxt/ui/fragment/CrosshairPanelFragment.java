package exp.ftxt.ui.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import exp.ftxt.R;
import exp.ftxt.ui.BasePanelFragment;

public class CrosshairPanelFragment extends BasePanelFragment {
    @Override
    protected int getLayoutResId() {
        return R.layout.panel_crosshair;
    }

    @Override
    protected String getPanelName() {
        return "crosshair";
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setVisibility(View.VISIBLE);
    }
}
