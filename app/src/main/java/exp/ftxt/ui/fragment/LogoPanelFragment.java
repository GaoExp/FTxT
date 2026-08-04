package exp.ftxt.ui.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import exp.ftxt.R;
import exp.ftxt.ui.BasePanelFragment;

public class LogoPanelFragment extends BasePanelFragment {
    @Override
    protected int getLayoutResId() {
        return R.layout.panel_logo;
    }

    @Override
    protected String getPanelName() {
        return "logo";
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setVisibility(View.VISIBLE);
    }
}
