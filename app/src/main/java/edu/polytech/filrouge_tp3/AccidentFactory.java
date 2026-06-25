package edu.polytech.filrouge_tp3;

public interface AccidentFactory {
    Issue createIssue(String title, String description);
    default Issue createIssue(String title, String description, double latitude, double longitude) {
        Issue issue = createIssue(title, description);
        issue.latitude = latitude;
        issue.longitude = longitude;
        return issue;
    }
}

