package edu.polytech.filrouge_tp3;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IssueController {

    private final IssueManager model;
    private final Map<Marker, Issue> markerIssueMap = new HashMap<>();

    public IssueController(IssueManager model) {
        this.model = model;
    }

    public void bindMarkers(MapView mapView, List<Marker> markers, List<Issue> issues) {
        markerIssueMap.clear();
        for (int i = 0; i < markers.size(); i++) {
            Marker marker = markers.get(i);
            Issue issue = issues.get(i);
            markerIssueMap.put(marker, issue);

            marker.setOnMarkerClickListener((m, map) -> {
                if (m.isInfoWindowShown()) {
                    m.closeInfoWindow();
                } else {
                    m.showInfoWindow();
                }
                return true;
            });

            marker.setOnMarkerDragListener(new Marker.OnMarkerDragListener() {
                @Override
                public void onMarkerDrag(Marker m) {}

                @Override
                public void onMarkerDragEnd(Marker m) {
                    Issue dragged = markerIssueMap.get(m);
                    if (dragged != null) {
                        model.setLocation(dragged, m.getPosition().getLatitude(), m.getPosition().getLongitude());
                    }
                }

                @Override
                public void onMarkerDragStart(Marker m) {}
            });
        }
    }
}
