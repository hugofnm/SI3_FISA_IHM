package edu.polytech.filrouge_tp3;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Screen2Fragment extends Fragment {
    public final static int FRAGMENT_ID = 1;
    private static final String TAG = "frallo " + Screen2Fragment.class.getSimpleName();
    private Notifiable notifiable;

    private String pendingType;
    private String pendingProfile;
    private ListView instructionsList;

    public Screen2Fragment() {
        Log.d(TAG, "screenFragment type 2 created");
    }

    public void setInstructions(String type, String profile) {
        this.pendingType = type;
        this.pendingProfile = profile;
        if (instructionsList != null) applyInstructions();
    }

    @Override
    public void onStart() {
        super.onStart();
        notifiable.onFragmentDisplayed(FRAGMENT_ID);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (requireActivity() instanceof Notifiable) {
            notifiable = (Notifiable) requireActivity();
        } else {
            throw new AssertionError("Classe " + requireActivity().getClass().getName() + " ne met pas en œuvre Notifiable.");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_screen2, container, false);
        instructionsList = view.findViewById(R.id.instructionsList);

        view.findViewById(R.id.btnAccord).setOnClickListener(v -> requireActivity().finish());

        applyInstructions();
        return view;
    }

    private void applyInstructions() {
        if (instructionsList == null) return;
        List<String> items = loadInstructions(pendingType, pendingProfile);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, items);
        instructionsList.setAdapter(adapter);
    }

    private List<String> loadInstructions(String type, String profile) {
        List<String> result = new ArrayList<>();
        if (type == null || profile == null) return result;
        try {
            InputStream is = requireContext().getAssets().open("instructions.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONObject root = new JSONObject(json);
            JSONObject instructions = root.getJSONObject("instructions");
            JSONObject typeObj = instructions.optJSONObject(type);
            if (typeObj == null) typeObj = instructions.optJSONObject("Autre");
            if (typeObj != null) {
                JSONArray arr = typeObj.optJSONArray(profile);
                if (arr == null) arr = typeObj.optJSONArray("temoin");
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) {
                        result.add("• " + arr.getString(i));
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur lecture instructions.json", e);
        }
        return result;
    }
}
