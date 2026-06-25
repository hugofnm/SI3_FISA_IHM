package edu.polytech.filrouge_tp3;

public interface ModelObservable {
    void addViewObserver(ViewObserver observer);
    void removeViewObserver(ViewObserver observer);
    void notifyViewObservers();
}
