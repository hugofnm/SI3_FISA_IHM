package edu.polytech.filrouge_tp3;

public interface IssueObserver {
    void onStatusChanged(Issue issue);

    void onPriorityChanged(Issue issue);

    default void onPictureChanged(Issue issue) {
    }
}
