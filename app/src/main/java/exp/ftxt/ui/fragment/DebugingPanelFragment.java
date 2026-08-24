package exp.ftxt.ui.fragment;

import exp.ftxt.R;
import exp.ftxt.ui.BasePanelFragment;

public class DebugingPanelFragment extends BasePanelFragment {
    @Override
    protected int getLayoutResId() {
        return R.layout.panel_debuging;
    }

    @Override
    protected String getPanelName() {
        return "debugging";
    }
}
