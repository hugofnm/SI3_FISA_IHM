package edu.polytech.filrouge_tp3;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Screen5Fragment extends Fragment implements ViewObserver, ClickableIssue<Issue> {
    public final static int FRAGMENT_ID = 4;
    private final String TAG = "frallo " + getClass().getSimpleName();
    private Notifiable notifiable;

    private MapView mapView;
    private ListView issueListView;

    private IssueManager model;
    private IssueController controller;
    private IssueAdapter listAdapter;
    private final List<Issue> visibleIssues = new ArrayList<>();

    // Position fixe : Polytech Nice Sophia
    private static final double POLYTECH_LAT = 43.6156;
    private static final double POLYTECH_LNG = 7.0718;

    public Screen5Fragment() {
        Log.d(TAG, "screenFragment type 5 created");
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
    public void onStart() {
        super.onStart();
        notifiable.onFragmentDisplayed(FRAGMENT_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Configuration.getInstance().load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()));
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
        View view = inflater.inflate(R.layout.fragment_screen5, container, false);

        mapView = view.findViewById(R.id.mapView);
        issueListView = view.findViewById(R.id.issueListView);

        setupMap();
        setupMVC();

        return view;
    }

    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(17.0);
        mapView.getController().setCenter(new GeoPoint(POLYTECH_LAT, POLYTECH_LNG));

        mapView.addMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                refreshVisibleList();
                return false;
            }
            @Override
            public boolean onZoom(ZoomEvent event) {
                refreshVisibleList();
                return false;
            }
        });
    }

    private void setupMVC() {
        model = IssueManager.getInstance();
        controller = new IssueController(model);
        model.addViewObserver(this);

        listAdapter = new IssueAdapter(requireContext(), visibleIssues, this);
        issueListView.setAdapter(listAdapter);

        onModelChanged(model.getIssues());
    }

    // --- ClickableIssue : le fragment prévient l'activité du comportement utilisateur ---

    @Override
    public void onClickItem(List<Issue> items, int itemIndex) {
        if (itemIndex < 0 || itemIndex >= items.size()) return;
        notifiable.onDataChange(FRAGMENT_ID, items.get(itemIndex), Notifiable.ACTION_SELECT_ISSUE, null);
    }

    @Override
    public void onRatingBarChange(int itemIndex, float value, IssueAdapter adapter, List<Issue> items) {
        if (itemIndex < 0 || itemIndex >= items.size()) return;
        int idx = Math.max(0, Math.min(Status.values().length - 1, Math.round(value) - 1));
        notifiable.onDataChange(FRAGMENT_ID, items.get(itemIndex),
                Notifiable.ACTION_UPDATE_ISSUE_SCORE, Status.values()[idx]);
    }

    private double distanceKm(Issue issue) {
        float[] results = new float[1];
        Location.distanceBetween(POLYTECH_LAT, POLYTECH_LNG, issue.latitude, issue.longitude, results);
        return results[0] / 1000.0;
    }

    private void refreshVisibleList() {
        if (mapView == null || listAdapter == null) return;
        BoundingBox bbox = mapView.getBoundingBox();

        List<Issue> visible = new ArrayList<>();
        for (Issue issue : model.getIssues()) {
            if (bbox.contains(issue.latitude, issue.longitude)) {
                visible.add(issue);
            }
        }

        visible.sort(Comparator.comparingDouble(this::distanceKm));

        visibleIssues.clear();
        visibleIssues.addAll(visible);
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onModelChanged(List<Issue> issues) {
        if (mapView == null) return;

        mapView.getOverlays().clear();
        List<Marker> markers = new ArrayList<>();
        for (Issue issue : issues) {
            Marker marker = new Marker(mapView);
            marker.setPosition(new GeoPoint(issue.latitude, issue.longitude));
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle(issue.getTitle());
            marker.setSnippet(issue.getDescription());
            marker.setDraggable(true);
            mapView.getOverlays().add(marker);
            markers.add(marker);
        }

        controller.bindMarkers(mapView, markers, new ArrayList<>(issues));
        mapView.invalidate();
        refreshVisibleList();
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
        if (model != null) model.removeViewObserver(this);
        if (mapView != null) mapView.onDetach();
    }
}
