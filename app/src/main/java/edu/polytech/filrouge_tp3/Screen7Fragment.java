package edu.polytech.filrouge_tp3;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Toast;

import androidx.fragment.app.Fragment;


/**
 *  Fragment prêt à remplir
 */
public class Screen7Fragment extends Fragment {
    public final static int FRAGMENT_ID = 6;
    private final String TAG = "frallo "+getClass().getSimpleName();
    private Notifiable notifiable;



    public Screen7Fragment() {
        Log.d(TAG,"screenFragment type 7 created"); // Required empty public constructor
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
            //Log.d(TAG, "Class " + requireActivity().getClass().getSimpleName() + " implements Notifiable.");
        } else {
            throw new AssertionError("Classe " + requireActivity().getClass().getName() + " ne met pas en œuvre Notifiable.");
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_screen7, container, false);

        view.findViewById(R.id.walletBadge).setOnClickListener(v ->
            Toast.makeText(requireContext(), "Et non t'es pas sous IOS", Toast.LENGTH_LONG).show()
        );

        return view;
    }
}