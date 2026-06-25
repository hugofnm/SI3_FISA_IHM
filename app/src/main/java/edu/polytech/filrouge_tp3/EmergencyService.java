package edu.polytech.filrouge_tp3;

import android.util.Log;

public final class EmergencyService implements IssueObserver {
    private static final String TAG = "EmergencyService";
    private static final EmergencyService INSTANCE = new EmergencyService();

    private EmergencyService() {
    }

    public static EmergencyService getInstance() {
        return INSTANCE;
    }

    @Override
    public void onStatusChanged(Issue issue) {
        Log.d(TAG, "Le nouveau statut de l'incident est : " + issue.getStatus());
    }

    @Override
    public void onPriorityChanged(Issue issue) {
    }
}
