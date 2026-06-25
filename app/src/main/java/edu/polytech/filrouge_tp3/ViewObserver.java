package edu.polytech.filrouge_tp3;

import java.util.List;

public interface ViewObserver {
    void onModelChanged(List<Issue> issues);
}
