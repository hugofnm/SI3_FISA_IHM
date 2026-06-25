package edu.polytech.filrouge_tp3;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class Screen4Fragment extends Fragment {
    public final static int FRAGMENT_ID = 3;
    private final String TAG = "frallo " + getClass().getSimpleName();
    private Notifiable notifiable;

    public Screen4Fragment() {
        Log.d(TAG, "screenFragment type 4 created");
    }

    @Override
    public void onStart() {
        super.onStart();
        notifiable.onFragmentDisplayed(FRAGMENT_ID);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (requireActivity() instanceof Notifiable) {
            notifiable = (Notifiable) requireActivity();
        } else {
            throw new AssertionError("Classe " + requireActivity().getClass().getName() + " ne met pas en œuvre Notifiable.");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_screen4, container, false);

        view.findViewById(R.id.buttonOui).setOnClickListener(v -> {
            if (requireActivity() instanceof Menuable) {
                ((Menuable) requireActivity()).onMenuChange(Screen6Fragment.FRAGMENT_ID);
            }
        });

        view.findViewById(R.id.buttonNon).setOnClickListener(v -> {
            if (requireActivity() instanceof Menuable) {
                ((Menuable) requireActivity()).onMenuChange(Screen3Fragment.FRAGMENT_ID);
            }
        });

        return view;
    }
}
