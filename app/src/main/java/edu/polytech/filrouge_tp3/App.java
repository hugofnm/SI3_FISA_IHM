package edu.polytech.filrouge_tp3;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class App extends Application {
    public static final String CHANNEL_URGENT = "urgences";
    public static final String CHANNEL_INFO = "infos";

    @Override
    public void onCreate() {
        super.onCreate();
        // Les canaux doivent exister AVANT toute notif (y compris celles
        // affichées par le système quand l'app est en arrière-plan).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(new NotificationChannel(
                    CHANNEL_URGENT, "Urgences", NotificationManager.IMPORTANCE_HIGH));
            manager.createNotificationChannel(new NotificationChannel(
                    CHANNEL_INFO, "Infos", NotificationManager.IMPORTANCE_DEFAULT));
        }
    }
}
