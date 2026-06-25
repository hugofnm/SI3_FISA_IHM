package edu.polytech.filrouge_tp3;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.List;

public class Screen1Fragment extends Fragment implements ViewObserver {
    private final String TAG = "frallo " + getClass().getSimpleName();
    public final static int FRAGMENT_ID = 0;

    private Notifiable notifiable;
    private IssueManager model;
    private Issue displayedIssue;

    public Screen1Fragment() {
        Log.d(TAG, "screenFragment type 1 created");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (requireActivity() instanceof Notifiable) {
            notifiable = (Notifiable) requireActivity();
        } else {
            throw new AssertionError(
                    "Classe " + requireActivity().getClass().getName() + " ne met pas en œuvre Notifiable.");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        notifiable.onFragmentDisplayed(FRAGMENT_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_screen1, container, false);

        model = IssueManager.getInstance();
        model.addViewObserver(this);

        applyIssue(view);
        return view;
    }

    public void setDisplayedIssue(Issue issue) {
        this.displayedIssue = issue;
        View v = getView();
        if (v != null) applyIssue(v);
    }

    public void resetDisplayedIssue() {
        displayedIssue = null;
        View v = getView();
        if (v != null) applyIssue(v);
    }

    private void applyIssue(View view) {
        TextView title               = view.findViewById(R.id.titleText);
        TextView description         = view.findViewById(R.id.descriptionText);
        TextView gravity             = view.findViewById(R.id.gravityText);
        TextView type                = view.findViewById(R.id.typeText);
        TextView injured             = view.findViewById(R.id.injuredText);
        TextView publicationTimeText = view.findViewById(R.id.publicationTimeText);
        View vehiclesRow             = view.findViewById(R.id.vehiclesRow);
        TextView vehiclesText        = view.findViewById(R.id.vehiclesText);
        TextView blockedText         = view.findViewById(R.id.blockedText);
        RatingBar statusRatingBar    = view.findViewById(R.id.statusRatingBar);

        if (displayedIssue != null) {
            title.setText(displayedIssue.getTitle() != null ? displayedIssue.getTitle() : "Sans titre");
            description.setText(displayedIssue.getDescription() != null ? displayedIssue.getDescription() : "");
            gravity.setText(String.format("Gravité : %.0f / 5", displayedIssue.gravity));
            type.setText(displayedIssue.type != null ? displayedIssue.type : "—");
            injured.setText(displayedIssue.nbInjured + " blessé(s)");

            // Heure de publication
            if (displayedIssue.publicationTime != null && !displayedIssue.publicationTime.isEmpty()) {
                publicationTimeText.setText("Publié à " + displayedIssue.publicationTime);
                publicationTimeText.setVisibility(android.view.View.VISIBLE);
            } else {
                publicationTimeText.setVisibility(android.view.View.GONE);
            }

            // Voitures impliquées + route bloquée (visibles seulement si nbVehicles non null)
            if (displayedIssue.nbVehicles != null) {
                vehiclesText.setText(displayedIssue.nbVehicles + " voiture(s)");
                blockedText.setText(displayedIssue.isBlocked ? "Route bloquée" : "Route libre");
                vehiclesRow.setVisibility(android.view.View.VISIBLE);
            } else {
                vehiclesRow.setVisibility(android.view.View.GONE);
            }

            // Statut : star selector
            statusRatingBar.setOnRatingBarChangeListener(null);
            statusRatingBar.setRating(displayedIssue.getStatus() == null
                    ? 0 : displayedIssue.getStatus().getRating());
            statusRatingBar.setOnRatingBarChangeListener((bar, value, fromUser) -> {
                if (fromUser) {
                    int idx = Math.max(0, Math.min(Status.values().length - 1, Math.round(value) - 1));
                    displayedIssue.setStatus(Status.values()[idx]);
                }
            });

            // photo existante (ou null -> placeholder)
            sendPhotoToCamera(displayedIssue.getPhoto());
        } else {
            title.setText("Titre");
            description.setText("");
            gravity.setText("");
            type.setText("");
            injured.setText("");
            publicationTimeText.setVisibility(android.view.View.GONE);
            vehiclesRow.setVisibility(android.view.View.GONE);
            statusRatingBar.setOnRatingBarChangeListener(null);
            statusRatingBar.setRating(0);
            sendPhotoToCamera(null);
        }
    }

    // envoie le chemin au CameraFragment enfant via FragmentResult
    private void sendPhotoToCamera(String photoPath) {
        Bundle bundle = new Bundle();
        bundle.putString(CameraFragment.KEY_PHOTO_PATH, photoPath);
        getChildFragmentManager().setFragmentResult(CameraFragment.CHANNEL_REQUEST, bundle);
    }

    @Override
    public void onModelChanged(List<Issue> issues) {}

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (model != null) model.removeViewObserver(this);
    }
}
