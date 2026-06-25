package edu.polytech.filrouge_tp3;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

public class MenuFragment extends Fragment {
    private final String TAG = "frallo " + getClass().getSimpleName();
    private Menuable menuable;
    private int currentIndex = 0;
    private View layout;

    // les 3 icônes du menu, chacune taggée avec l'id du fragment
    private final int[] ICON_IDS = {R.id.menuReport, R.id.menuList, R.id.menuWallet};

    public MenuFragment() {
    }

    public void setCurrentActivatedIndex(int index) {
        currentIndex = index;
        updateHighlight();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_menu, container, false);

        if (getArguments() != null) {
            currentIndex = getArguments().getInt(getString(R.string.index), 0);
        }

        for (ImageView icon : getIcons()) {
            icon.setOnClickListener(v -> {
                currentIndex = Integer.parseInt(v.getTag().toString());
                Log.d(TAG, "Menu cliqué -> fragment " + currentIndex);
                menuable.onMenuChange(currentIndex);
                updateHighlight();
            });
        }

        menuable.onMenuChange(currentIndex);
        ((TextView) layout.findViewById(R.id.txtFragmentMenu)).setText("Menu");
        updateHighlight();
        return layout;
    }

    private List<ImageView> getIcons() {
        List<ImageView> icons = new ArrayList<>();
        for (int id : ICON_IDS) {
            icons.add(layout.findViewById(id));
        }
        return icons;
    }

    // icône sélectionnée en vert, les autres en gris
    private void updateHighlight() {
        if (layout == null) return;
        int green = ContextCompat.getColor(requireContext(), R.color.green);
        for (ImageView icon : getIcons()) {
            int tag = Integer.parseInt(icon.getTag().toString());
            icon.setColorFilter(tag == currentIndex ? green : Color.GRAY);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (requireActivity() instanceof Menuable) {
            menuable = (Menuable) requireActivity();
        } else {
            throw new AssertionError("Classe " + requireActivity().getClass().getName() + " ne met pas en œuvre Menuable.");
        }
    }
}
