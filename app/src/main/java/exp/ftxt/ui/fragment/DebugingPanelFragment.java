package exp.ftxt.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setVisibility(View.VISIBLE);

        setupIconClicks(view);
    }

    private void setupIconClicks(View view) {
        setupIconClick(view.findViewById(R.id.icon_variant_1), "Variant 1: Material Default");
        setupIconClick(view.findViewById(R.id.icon_variant_2), "Variant 2: Ponsel + Panah");
        setupIconClick(view.findViewById(R.id.icon_variant_3), "Variant 3: Tablet + Panah");
        setupIconClick(view.findViewById(R.id.icon_variant_4), "Variant 4: Panah Sederhana");
        setupIconClick(view.findViewById(R.id.icon_variant_5), "Variant 5: Lingkaran + P");
    }

    private void setupIconClick(ImageView imageView, String variantName) {
        if (imageView != null) {
            imageView.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Dipilih: " + variantName, Toast.LENGTH_SHORT).show();
            });
        }
    }
}
