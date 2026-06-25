package edu.polytech.filrouge_tp3;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.speech.RecognizerIntent;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Screen3Fragment extends Fragment {
    public final static int FRAGMENT_ID = 2;
    private final String TAG = "frallo " + getClass().getSimpleName();
    private Notifiable notifiable;

    private static final double POLYTECH_LAT = 43.6156;
    private static final double POLYTECH_LNG = 7.0718;
    private static final String DEFAULT_IMG = "istockphoto1455492016612x612";
    private static final String AUTHORITY = "edu.polytech.filrouge_tp3.fileprovider";
    private static final String STATE_PHOTO_PATH = "screen3_photo_path";
    private static final String STATE_PENDING_PATH = "screen3_pending_path";

    private ImageView photoPreview;
    private String capturedPhotoPath;
    private String pendingCapturePath;
    private TimePicker timePicker;

    private MapView mapView;
    private TextInputLayout titleLayout;
    private TextInputLayout descriptionLayout;
    private TextInputEditText editTitle;
    private TextInputEditText editDescription;
    private MaterialButton selectedTypeButton;
    private MaterialButton[] typeButtons;
    private SeekBar seekBar;
    private Spinner spinnerBlesss;
    private Spinner spinnerVoitures;
    private RadioGroup radioRouteBloquee;
    private TextInputEditText currentTargetEditText;

    private final ActivityResultLauncher<Intent> voiceLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    ArrayList<String> matches = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (matches != null && !matches.isEmpty() && currentTargetEditText != null) {
                        currentTargetEditText.setText(matches.get(0));
                    }
                }
            });

    private final ActivityResultLauncher<String> cameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            granted -> {
                if (Boolean.TRUE.equals(granted)) {
                    launchCamera();
                } else {
                    showPermissionRationale();
                }
            });

    private final ActivityResultLauncher<Uri> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                String path = pendingCapturePath;
                pendingCapturePath = null;
                if (Boolean.TRUE.equals(success) && path != null) {
                    capturedPhotoPath = path;
                    displayPreview(path);
                } else {
                    Log.d(TAG, "Capture annulée ou échouée");
                }
            });

    public Screen3Fragment() {
        Log.d(TAG, "screenFragment type 3 created");
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
        Configuration.getInstance().load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()));
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
        View view = inflater.inflate(R.layout.fragment_screen3, container, false);

        if (savedInstanceState != null) {
            capturedPhotoPath = savedInstanceState.getString(STATE_PHOTO_PATH);
            pendingCapturePath = savedInstanceState.getString(STATE_PENDING_PATH);
        }

        timePicker = view.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);

        // Champs texte avec micro
        titleLayout = view.findViewById(R.id.titleLayout);
        descriptionLayout = view.findViewById(R.id.descriptionLayout);
        editTitle = view.findViewById(R.id.editTitle);
        editDescription = view.findViewById(R.id.editDescription);

        titleLayout.setEndIconOnClickListener(v -> startVoiceRecognition(editTitle));
        descriptionLayout.setEndIconOnClickListener(v -> startVoiceRecognition(editDescription));

        // Carte
        mapView = view.findViewById(R.id.reportMapView);
        GeoPoint incidentPos = new GeoPoint(POLYTECH_LAT, POLYTECH_LNG);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(18.0);
        mapView.getController().setCenter(incidentPos);
        Marker marker = new Marker(mapView);
        marker.setPosition(incidentPos);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setDraggable(false);
        marker.setOnMarkerClickListener((m, mv) -> true);
        mapView.getOverlays().add(marker);

        // Boutons type (6)
        MaterialButton btnAccident  = view.findViewById(R.id.btnAccident);
        MaterialButton btnObjet     = view.findViewById(R.id.btnObjet);
        MaterialButton btnExplosion = view.findViewById(R.id.btnExplosion);
        MaterialButton btnIncendie  = view.findViewById(R.id.btnIncendie);
        MaterialButton btnInondation = view.findViewById(R.id.btnInondation);
        MaterialButton btnAutre     = view.findViewById(R.id.btnAutre);
        typeButtons = new MaterialButton[]{btnAccident, btnObjet, btnExplosion, btnIncendie, btnInondation, btnAutre};
        selectTypeButton(btnAccident);
        for (MaterialButton btn : typeButtons) {
            btn.setOnClickListener(v -> selectTypeButton((MaterialButton) v));
        }

        // SeekBar gravité
        TextView seekValue = view.findViewById(R.id.seekBarValue);
        seekBar = view.findViewById(R.id.seekBarGravite);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                seekValue.setText(progress + " / 5");
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        // Spinner blessés
        spinnerBlesss = view.findViewById(R.id.spinnerBlesss);
        List<String> optionsBlesss = new ArrayList<>();
        for (int i = 0; i <= 9; i++) optionsBlesss.add(String.valueOf(i));
        optionsBlesss.add("10 ou +");
        ArrayAdapter<String> adapterBlesss = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, optionsBlesss);
        adapterBlesss.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBlesss.setAdapter(adapterBlesss);

        // Spinner voitures
        spinnerVoitures = view.findViewById(R.id.spinnerVoitures);
        List<String> optionsVoitures = new ArrayList<>();
        for (int i = 0; i <= 9; i++) optionsVoitures.add(String.valueOf(i));
        optionsVoitures.add("10 ou +");
        ArrayAdapter<String> adapterVoitures = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, optionsVoitures);
        adapterVoitures.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVoitures.setAdapter(adapterVoitures);

        // RadioGroup route bloquée
        radioRouteBloquee = view.findViewById(R.id.radioRouteBloquee);

        // Photo
        photoPreview = view.findViewById(R.id.photoPreview);
        view.findViewById(R.id.btnPhoto).setOnClickListener(v -> onPhotoClicked());
        displayPreview(capturedPhotoPath);

        // Bouton envoyer
        view.findViewById(R.id.btnEnvoyer).setOnClickListener(v -> onEnvoyer());

        return view;
    }

    private void onEnvoyer() {
        String title = editTitle.getText() != null ? editTitle.getText().toString().trim() : "";
        String description = editDescription.getText() != null ? editDescription.getText().toString().trim() : "";
        String type = selectedTypeButton != null ? selectedTypeButton.getText().toString() : "";
        int gravity = seekBar.getProgress();
        String injuredStr = spinnerBlesss.getSelectedItem().toString();
        int nbInjured = injuredStr.equals("10 ou +") ? 10 : Integer.parseInt(injuredStr);
        String voituresStr = spinnerVoitures.getSelectedItem().toString();
        int nbVoitures = voituresStr.equals("10 ou +") ? 10 : Integer.parseInt(voituresStr);
        boolean isBlocked = radioRouteBloquee.getCheckedRadioButtonId() == R.id.radioPouji;

        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Le titre est obligatoire.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (description.isEmpty()) {
            Toast.makeText(requireContext(), "La description est obligatoire.", Toast.LENGTH_SHORT).show();
            return;
        }
        String publicationTime = String.format(Locale.FRANCE, "%02d:%02d",
                timePicker.getHour(), timePicker.getMinute());

        String image = capturedPhotoPath != null ? capturedPhotoPath : DEFAULT_IMG;

        // création via la factory autoroute
        Issue newIssue = new HighwayFactory().create(
                POLYTECH_LAT, POLYTECH_LNG,
                title, description,
                image, (float) gravity,
                type, publicationTime,
                nbInjured, nbVoitures, isBlocked
        );

        IssueManager.getInstance().addIssue(newIssue);
        Toast.makeText(requireContext(), "Incident signalé !", Toast.LENGTH_SHORT).show();
        notifiable.onDataChange(FRAGMENT_ID, null, Notifiable.ACTION_SHOW_INSTRUCTIONS,
                new String[]{"implique", type});
    }

    private void selectTypeButton(MaterialButton selected) {
        selectedTypeButton = selected;
        for (MaterialButton btn : typeButtons) {
            if (btn == selected) {
                btn.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.blue));
                btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            } else {
                btn.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), android.R.color.transparent));
                btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue));
            }
        }
    }

    private void startVoiceRecognition(TextInputEditText target) {
        currentTargetEditText = target;
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Parlez pour remplir le champ...");
        try {
            voiceLauncher.launch(intent);
        } catch (Exception e) {
            Log.e(TAG, "Reconnaissance vocale non supportée sur cet appareil.", e);
        }
    }

    private void onPhotoClicked() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void showPermissionRationale() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Permission caméra requise")
                .setMessage("La caméra est nécessaire pour joindre une photo au signalement.")
                .setPositiveButton("Réessayer",
                        (d, w) -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA))
                .setNegativeButton("Continuer sans photo", (d, w) -> d.dismiss())
                .show();
    }

    private void launchCamera() {
        try {
            File photoFile = createImageFile();
            pendingCapturePath = photoFile.getAbsolutePath();
            Uri uri = FileProvider.getUriForFile(requireContext(), AUTHORITY, photoFile);
            takePictureLauncher.launch(uri);
        } catch (IOException e) {
            Log.e(TAG, "Création du fichier impossible", e);
        }
    }

    private File createImageFile() throws IOException {
        File dir = new File(requireContext().getCacheDir(), "images");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("dossier cache non créé");
        }
        String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        return File.createTempFile("IMG_" + stamp + "_", ".jpg", dir);
    }

    private void displayPreview(String path) {
        if (photoPreview == null) {
            return;
        }
        if (path == null || path.isEmpty()) {
            photoPreview.setImageResource(R.drawable.ic_photo_placeholder);
            return;
        }
        Uri uri = (path.startsWith("content:") || path.startsWith("file:"))
                ? Uri.parse(path) : Uri.fromFile(new File(path));
        Picasso.get().load(uri)
                .placeholder(R.drawable.ic_photo_placeholder)
                .error(R.drawable.ic_photo_placeholder)
                .fit().centerCrop()
                .into(photoPreview);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_PHOTO_PATH, capturedPhotoPath);
        outState.putString(STATE_PENDING_PATH, pendingCapturePath);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mapView != null) mapView.onDetach();
    }
}
