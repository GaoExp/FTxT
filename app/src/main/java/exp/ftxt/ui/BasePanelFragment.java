package exp.ftxt.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public abstract class BasePanelFragment extends Fragment {
    protected abstract int getLayoutResId();
    protected abstract String getPanelName();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(getLayoutResId(), container, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void onPanelShown() {}

    public void onPanelHidden() {}

    public void showLoadPresetDialog() {}
}
