package edu.polytech.filrouge_tp3;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.squareup.picasso.Picasso;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Screen6Fragment extends Fragment {
    public final static int FRAGMENT_ID = 5;
    private final String TAG = "frallo " + getClass().getSimpleName();
    private Notifiable notifiable;

    private static final double POLYTECH_LAT = 43.6156;
    private static final double POLYTECH_LNG = 7.0718;
    private static final String DEFAULT_IMG = "placeholder_photo";
    private static final String AUTHORITY = "edu.polytech.filrouge_tp3.fileprovider";
    private static final String STATE_PHOTO_PATH = "screen6_photo_path";
    private static final String STATE_PENDING_PATH = "screen6_pending_path";

    private MapView mapView;
    private Marker marker;
    private GeoPoint selectedPoint;
    private LocationListener locationListener;
    private MaterialButton selectedTypeButton;
    private MaterialButton[] typeButtons;
    private SeekBar seekBar;
    private Spinner spinner;
    private TextInputLayout titleLayout;
    private TextInputLayout descriptionLayout;
    private TextInputEditText editTitle;
    private TextInputEditText editDescription;
    private TextInputEditText currentTargetEditText;

    private ImageView photoPreview;
    private String capturedPhotoPath;
    private String pendingCapturePath;
    private TimePicker timePicker;

    private final ActivityResultLauncher<String> locationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            granted -> {
                if (Boolean.TRUE.equals(granted)) {
                    requestFreshLocation();
                }
            });

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
                }
            });

    public Screen6Fragment() {
        Log.d(TAG, "screenFragment type 6 created");
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
        Configuration.getInstance().load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()));
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
        View view = inflater.inflate(R.layout.fragment_screen6, container, false);

        if (savedInstanceState != null) {
            capturedPhotoPath = savedInstanceState.getString(STATE_PHOTO_PATH);
            pendingCapturePath = savedInstanceState.getString(STATE_PENDING_PATH);
        }

        timePicker = view.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        // on place le marqueur sur la position GPS de l'appareil.
        mapView = view.findViewById(R.id.reportMapView);
        double[] pos = getDeviceLocation();
        selectedPoint = new GeoPoint(pos[0], pos[1]);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(18.0);
        mapView.getController().setCenter(selectedPoint);

        marker = new Marker(mapView);
        marker.setPosition(selectedPoint);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setDraggable(true);
        marker.setTitle("Position de l'incident");
        marker.setOnMarkerClickListener((m, mv) -> true);
        marker.setOnMarkerDragListener(new Marker.OnMarkerDragListener() {
            @Override public void onMarkerDrag(Marker m) {}
            @Override public void onMarkerDragEnd(Marker m) { selectedPoint = m.getPosition(); }
            @Override public void onMarkerDragStart(Marker m) {}
        });
        mapView.getOverlays().add(marker);

        requestFreshLocation();

        // Champs texte avec micro (reconnaissance vocale)
        titleLayout = view.findViewById(R.id.titleLayout);
        descriptionLayout = view.findViewById(R.id.descriptionLayout);
        editTitle = view.findViewById(R.id.editTitle);
        editDescription = view.findViewById(R.id.editDescription);

        titleLayout.setEndIconOnClickListener(v -> startVoiceRecognition(editTitle));
        descriptionLayout.setEndIconOnClickListener(v -> startVoiceRecognition(editDescription));

        // Boutons type
        MaterialButton btnAccident  = view.findViewById(R.id.btnAccident);
        MaterialButton btnObjet     = view.findViewById(R.id.btnObjet);
        MaterialButton btnExplosion = view.findViewById(R.id.btnExplosion);
        typeButtons = new MaterialButton[]{btnAccident, btnObjet, btnExplosion};
        selectTypeButton(btnAccident);
        for (MaterialButton btn : typeButtons) {
            btn.setOnClickListener(v -> selectTypeButton((MaterialButton) v));
        }

        // Spinner blessés
        spinner = view.findViewById(R.id.spinnerBlesss);
        List<String> options = new ArrayList<>();
        for (int i = 0; i <= 9; i++) options.add(String.valueOf(i));
        options.add("10 ou +");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

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

        // Photo
        photoPreview = view.findViewById(R.id.photoPreview);
        view.findViewById(R.id.btnPhoto).setOnClickListener(v -> onPhotoClicked());
        displayPreview(capturedPhotoPath);

        // Bouton envoyer
        view.findViewById(R.id.btnEnvoyer).setOnClickListener(v -> onSend());

        return view;
    }

    private void onSend() {
        String title = editTitle.getText() != null ? editTitle.getText().toString().trim() : "";
        String description = editDescription.getText() != null ? editDescription.getText().toString().trim() : "";
        String type = selectedTypeButton != null ? selectedTypeButton.getText().toString() : "";
        int gravity = seekBar.getProgress();
        String injuredStr = spinner.getSelectedItem().toString();
        int nbInjured = injuredStr.equals("10 ou +") ? 10 : Integer.parseInt(injuredStr);

        // Validation : tous les champs obligatoires
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

        // position choisie sur la carte (GPS de l'appareil, ajustable par l'utilisateur)
        GeoPoint pos = selectedPoint != null ? selectedPoint : new GeoPoint(POLYTECH_LAT, POLYTECH_LNG);

        // création via la factory urbaine
        Issue newIssue = new UrbanFactory().create(
                pos.getLatitude(), pos.getLongitude(),
                title, description,
                image, (float) gravity,
                type, publicationTime, nbInjured
        );

        IssueManager.getInstance().addIssue(newIssue);
        Toast.makeText(requireContext(), "Incident signalé !", Toast.LENGTH_SHORT).show();

        notifiable.onDataChange(FRAGMENT_ID, null, Notifiable.ACTION_SHOW_INSTRUCTIONS,
                new String[]{"temoin", type});
    }

    private void requestFreshLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationManager lm = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        if (lm == null) return;

        Location last = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (last == null) last = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (last != null) updateLocation(last.getLatitude(), last.getLongitude());

        if (locationListener == null) {
            locationListener = new LocationListener() {
                @Override public void onLocationChanged(@NonNull Location location) {
                    updateLocation(location.getLatitude(), location.getLongitude());
                    stopLocationUpdates();
                }
                @Override public void onProviderEnabled(@NonNull String provider) {}
                @Override public void onProviderDisabled(@NonNull String provider) {}
                @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
            };
        }
        try {
            if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, locationListener);
            }
            if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Localisation refusée", e);
        }
    }

    // Recentre la carte et le marqueur sur la position donnée.
    private void updateLocation(double lat, double lng) {
        if (mapView == null || marker == null) return;
        selectedPoint = new GeoPoint(lat, lng);
        marker.setPosition(selectedPoint);
        mapView.getController().setCenter(selectedPoint);
        mapView.invalidate();
    }

    private void stopLocationUpdates() {
        if (locationListener == null) return;
        LocationManager lm = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        if (lm != null) lm.removeUpdates(locationListener);
    }

    private double[] getDeviceLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationManager lm = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
            Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (loc == null) loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (loc != null) return new double[]{loc.getLatitude(), loc.getLongitude()};
        }
        return new double[]{POLYTECH_LAT, POLYTECH_LNG};
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
        if (photoPreview == null) return;
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
        stopLocationUpdates();
        if (mapView != null) mapView.onDetach();
    }
}
