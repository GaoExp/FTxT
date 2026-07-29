package exp.ftxt.ui;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.HashMap;
import java.util.Map;

import exp.ftxt.ui.fragment.BatteryCurrentPanelFragment;
import exp.ftxt.ui.fragment.BatteryPanelFragment;
import exp.ftxt.ui.fragment.BatteryPercentagePanelFragment;
import exp.ftxt.ui.fragment.ClockPanelFragment;
import exp.ftxt.ui.fragment.ColorPickerPanelFragment;
import exp.ftxt.ui.fragment.FpsPanelFragment;
import exp.ftxt.ui.fragment.NetworkPanelFragment;
import exp.ftxt.ui.fragment.TextPanelFragment;

public class PanelManager {
    private final FragmentManager fragmentManager;
    private final int containerId;
    private String currentPanel = null;

    private final Map<String, Class<? extends BasePanelFragment>> panelMap = new HashMap<>();

    public PanelManager(FragmentActivity activity, int containerId) {
        this.fragmentManager = activity.getSupportFragmentManager();
        this.containerId = containerId;

        panelMap.put("text", TextPanelFragment.class);
        panelMap.put("fps", FpsPanelFragment.class);
        panelMap.put("clock", ClockPanelFragment.class);
        panelMap.put("battery", BatteryPanelFragment.class);
        panelMap.put("battery_pct", BatteryPercentagePanelFragment.class);
        panelMap.put("battery_cur", BatteryCurrentPanelFragment.class);
        panelMap.put("network", NetworkPanelFragment.class);
        panelMap.put("color_picker", ColorPickerPanelFragment.class);
    }

    public void showPanel(String name) {
        if (name.equals(currentPanel)) return;

        FragmentTransaction ft = fragmentManager.beginTransaction();

        for (Fragment f : fragmentManager.getFragments()) {
            if (f instanceof BasePanelFragment) {
                ft.hide(f);
            }
        }

        Fragment target = fragmentManager.findFragmentByTag(name);
        if (target != null) {
            ft.show(target);
        } else {
            Class<? extends BasePanelFragment> cls = panelMap.get(name);
            if (cls != null) {
                try {
                    target = cls.newInstance();
                    ft.add(containerId, target, name);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        ft.commit();
        currentPanel = name;
    }

    public String getCurrentPanel() {
        return currentPanel;
    }

    public boolean hasPanel(String name) {
        return panelMap.containsKey(name);
    }

    public void onPanelShown() {
        Fragment f = fragmentManager.findFragmentByTag(currentPanel);
        if (f instanceof BasePanelFragment) {
            ((BasePanelFragment) f).onPanelShown();
        }
    }

    public void showLoadPresetDialog() {
        Fragment f = fragmentManager.findFragmentByTag(currentPanel);
        if (f instanceof BasePanelFragment) {
            ((BasePanelFragment) f).showLoadPresetDialog();
        }
    }
}
