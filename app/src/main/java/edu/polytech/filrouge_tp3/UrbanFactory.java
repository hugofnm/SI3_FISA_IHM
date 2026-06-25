package edu.polytech.filrouge_tp3;

public class UrbanFactory implements AccidentFactory {
    @Override
    public Issue createIssue(String title, String description) {
        UrbanIssue issue = new UrbanIssue(title, description, "medium", 0f);
        issue.addObserver(EmergencyService.getInstance());
        return issue;
    }

    public UrbanIssue create(double latitude, double longitude, String name, String description,
                             String image, float gravity, String type, String publicationTime, int nbInjured) {
        UrbanIssue issue = new UrbanIssue(latitude, longitude, name, description, image, gravity,
                type, publicationTime, nbInjured);
        issue.addObserver(EmergencyService.getInstance());
        return issue;
    }
}

