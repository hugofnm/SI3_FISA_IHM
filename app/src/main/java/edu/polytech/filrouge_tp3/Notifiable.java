package edu.polytech.filrouge_tp3;

public interface Notifiable {
    int ACTION_SELECT_ISSUE = 1;
    int ACTION_UPDATE_ISSUE_SCORE = 2;
    int ACTION_SHOW_INSTRUCTIONS = 3;

    void onClick(int numFragment);

    void onDataChange(int numFragment, Object object, int actionCode, Object argsAction);

    void onFragmentDisplayed(int fragmentId);
}
