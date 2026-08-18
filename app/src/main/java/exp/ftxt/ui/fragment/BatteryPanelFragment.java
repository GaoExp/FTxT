package exp.ftxt.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import exp.ftxt.MainActivity;
import exp.ftxt.R;
import exp.ftxt.ui.BasePanelFragment;
import exp.ftxt.ui.BatteryBarPanelController;
import exp.ftxt.ui.BatteryPanelController;

public class BatteryPanelFragment extends BasePanelFragment {
    private BatteryPanelController batteryPanelController;
    private BatteryBarPanelController batteryBarPanelController;

    private BottomNavigationView bottomNav;
    private View tabMonitor;
    private View tabOverlay;
    private View tabStrip;
    private int currentTabId = R.id.batTabNavMonitor;

    @Override
    protected int getLayoutResId() {
        return R.layout.panel_battery;
    }

    @Override
    protected String getPanelName() {
        return "battery";
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tabMonitor = view.findViewById(R.id.batTabMonitor);
        tabOverlay = view.findViewById(R.id.batTabOverlay);
        tabStrip = view.findViewById(R.id.batTabStrip);
        bottomNav = view.findViewById(R.id.batBottomNav);

        batteryPanelController = new BatteryPanelController((MainActivity) requireActivity(), view);
        batteryBarPanelController = new BatteryBarPanelController((MainActivity) requireActivity(), view);

        setupBottomNav();
    }

    private void setupBottomNav() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.batTabNavMonitor) {
                showTab(tabMonitor);
                currentTabId = id;
            } else if (id == R.id.batTabNavOverlay) {
                showTab(tabOverlay);
                currentTabId = id;
                if (batteryPanelController != null) batteryPanelController.onPanelShown();
            } else {
                showTab(tabStrip);
                currentTabId = id;
                if (batteryBarPanelController != null) batteryBarPanelController.onPanelShown();
            }
            return true;
        });
        showTab(tabMonitor);
    }

    private void showTab(View tab) {
        tabMonitor.setVisibility(tab == tabMonitor ? View.VISIBLE : View.GONE);
        tabOverlay.setVisibility(tab == tabOverlay ? View.VISIBLE : View.GONE);
        tabStrip.setVisibility(tab == tabStrip ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onPanelShown() {
        if (batteryPanelController != null) batteryPanelController.onPanelShown();
        if (batteryBarPanelController != null) batteryBarPanelController.onPanelShown();
    }

    @Override
    public void showLoadPresetDialog() {
        if (currentTabId == R.id.batTabNavOverlay) {
            if (batteryPanelController != null) batteryPanelController.showLoadPresetDialog();
        } else if (currentTabId == R.id.batTabNavStrip) {
            if (batteryBarPanelController != null) batteryBarPanelController.showLoadPresetDialog();
        } else {
            Toast.makeText(requireContext(),
                    "Pilih tab Overlay atau Battery Strip untuk memuat preset",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        if (batteryPanelController != null) {
            batteryPanelController.cleanup();
            batteryPanelController = null;
        }
        if (batteryBarPanelController != null) {
            batteryBarPanelController.cleanup();
            batteryBarPanelController = null;
        }
        super.onDestroyView();
    }
}
