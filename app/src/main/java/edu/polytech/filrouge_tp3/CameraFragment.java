package edu.polytech.filrouge_tp3;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CameraFragment extends Fragment {
    private static final String TAG = "frallo CameraFragment";

    // canal pour recevoir une image existante depuis un autre fragment
    public static final String CHANNEL_REQUEST = "camera_channel";
    public static final String KEY_PHOTO_PATH = "photo_path";

    private static final String STATE_PHOTO_PATH = "state_photo_path";
    private static final String STATE_PENDING_PATH = "state_pending_path";
    private static final String AUTHORITY = "edu.polytech.filrouge_tp3.fileprovider";

    private ImageView preview;
    private Picturable host;

    private String currentPhotoPath;
    private String pendingCapturePath; // photo en cours de capture

    private ActivityResultLauncher<String> permissionLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Picturable) {
            host = (Picturable) context;
        } else {
            throw new AssertionError(context.getClass().getName()
                    + " ne met pas en œuvre Picturable.");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            currentPhotoPath = savedInstanceState.getString(STATE_PHOTO_PATH);
            pendingCapturePath = savedInstanceState.getString(STATE_PENDING_PATH);
        }

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (Boolean.TRUE.equals(granted)) {
                        launchCamera();
                    } else {
                        showPermissionRationale();
                    }
                });

        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    String path = pendingCapturePath;
                    pendingCapturePath = null;
                    if (Boolean.TRUE.equals(success) && path != null) {
                        currentPhotoPath = path;
                        displayPicture(path);
                        if (host != null) {
                            host.onPictureTaken(path);
                        }
                    }
                });

        getParentFragmentManager().setFragmentResultListener(
                CHANNEL_REQUEST, this, (requestKey, bundle) -> {
                    String path = bundle.getString(KEY_PHOTO_PATH);
                    currentPhotoPath = path;
                    displayPicture(path);
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        preview = view.findViewById(R.id.cameraPreview);
        Button capture = view.findViewById(R.id.captureButton);
        capture.setOnClickListener(v -> onCaptureClicked());
        displayPicture(currentPhotoPath);
    }

    private void onCaptureClicked() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void showPermissionRationale() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Permission caméra requise")
                .setMessage("La caméra est nécessaire pour photographier le signalement.")
                .setPositiveButton("Réessayer",
                        (d, w) -> permissionLauncher.launch(Manifest.permission.CAMERA))
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

    private void displayPicture(String path) {
        if (preview == null) {
            return;
        }
        if (path == null || path.isEmpty()) {
            preview.setImageResource(R.drawable.ic_photo_placeholder);
            return;
        }
        if (path.startsWith("content:") || path.startsWith("file:")) {
            load(Uri.parse(path));
        } else if (path.startsWith("/")) {
            load(Uri.fromFile(new File(path)));
        } else {
            // sinon c'est un nom de drawable (données de départ)
            int resId = getResources().getIdentifier(
                    path, "drawable", requireContext().getPackageName());
            if (resId != 0) {
                Picasso.get().load(resId)
                        .placeholder(R.drawable.ic_photo_placeholder)
                        .fit().centerCrop().into(preview);
            } else {
                preview.setImageResource(R.drawable.ic_photo_placeholder);
            }
        }
    }

    private void load(Uri uri) {
        Picasso.get().load(uri)
                .placeholder(R.drawable.ic_photo_placeholder)
                .error(R.drawable.ic_photo_placeholder)
                .fit().centerCrop()
                .into(preview);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // on sauvegarde juste les chemins, pas le bitmap
        outState.putString(STATE_PHOTO_PATH, currentPhotoPath);
        outState.putString(STATE_PENDING_PATH, pendingCapturePath);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        host = null;
    }
}
