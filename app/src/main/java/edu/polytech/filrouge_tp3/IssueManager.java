package edu.polytech.filrouge_tp3;

import java.util.ArrayList;
import java.util.List;

public class IssueManager implements ModelObservable {

    private static IssueManager instance;

    private final List<Issue> issues = new ArrayList<>();
    private final List<ViewObserver> observers = new ArrayList<>();

    private static final String IMG = "placeholder_photo";

    private final UrbanFactory urbanFactory = new UrbanFactory();
    private final HighwayFactory highwayFactory = new HighwayFactory();

    private IssueManager() {
        // Création via les factories (qui rattachent EmergencyService comme observer)
        issues.add(urbanFactory.create(43.6156, 7.0718,
                "Accident de la route", "Collision entre deux véhicules rue principale.",
                IMG, 3f, "Accident", "08:30", 2));

        issues.add(urbanFactory.create(43.6162, 7.0725,
                "Panne de voiture", "Véhicule immobilisé sur le bas-côté.",
                IMG, 1f, "Objet route", "09:15", 0));

        issues.add(urbanFactory.create(43.6170, 7.0735,
                "Feu de poubelle", "Incendie de conteneurs à ordures.",
                IMG, 2f, "Explosion", "10:45", 1));

        issues.add(urbanFactory.create(43.6180, 7.0750,
                "Inondation rue", "Chaussée inondée suite à de fortes pluies.",
                IMG, 2f, "Objet route", "11:00", 0));

        issues.add(highwayFactory.create(43.6148, 7.0710,
                "Panne de batterie", "Batterie à plat sur l'autoroute.",
                IMG, 1f, "Accident", "07:50", 0, 1, false));

        issues.add(highwayFactory.create(43.6140, 7.0700,
                "Obstacle sur la chaussée", "Débris encombrant la voie rapide.",
                IMG, 3f, "Objet route", "12:30", 0, 0, true));

        issues.add(highwayFactory.create(43.6130, 7.0695,
                "Accident multi-véhicules", "Carambolage impliquant 3 véhicules.",
                IMG, 5f, "Accident", "13:10", 5, 3, true));
    }

    public static IssueManager getInstance() {
        if (instance == null) {
            instance = new IssueManager();
        }
        return instance;
    }

    public List<Issue> getIssues() {
        return issues;
    }

    public void addIssue(Issue issue) {
        if (issue == null) return;
        issue.addObserver(EmergencyService.getInstance());
        if (issue.latitude == 0 && issue.longitude == 0) {
            issue.latitude = 43.6156 + (Math.random() - 0.5) * 0.01;
            issue.longitude = 7.0718 + (Math.random() - 0.5) * 0.01;
        }
        issues.add(0, issue);
        notifyViewObservers();
    }

    public void setLocation(Issue issue, double latitude, double longitude) {
        issue.latitude = latitude;
        issue.longitude = longitude;
        notifyViewObservers();
    }

    @Override
    public void addViewObserver(ViewObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void removeViewObserver(ViewObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyViewObservers() {
        for (ViewObserver observer : observers) {
            observer.onModelChanged(issues);
        }
    }
}
