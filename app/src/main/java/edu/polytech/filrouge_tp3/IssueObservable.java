package edu.polytech.filrouge_tp3;

public interface IssueObservable {
    void addObserver(IssueObserver observer);

    void removeObserver(IssueObserver observer);

    void notifyObservers();
}
