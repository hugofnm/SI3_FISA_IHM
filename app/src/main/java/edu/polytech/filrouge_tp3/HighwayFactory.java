package edu.polytech.filrouge_tp3;

public class HighwayFactory implements AccidentFactory {

    @Override
    public Issue createIssue(String title, String description) {
        HighwayIssue issue = new HighwayIssue(title, description, "CRITICAL", 0f);
        issue.addObserver(EmergencyService.getInstance());
        return issue;
    }

    public HighwayIssue create(double latitude, double longitude, String name, String description,
                               String image, float gravity, String type, String publicationTime,
                               int nbInjured, Integer nbVehicles, boolean isBlocked) {
        HighwayIssue issue = new HighwayIssue(latitude, longitude, name, description, image, gravity,
                type, publicationTime, nbInjured, nbVehicles, isBlocked);
        issue.addObserver(EmergencyService.getInstance());
        return issue;
    }
}
