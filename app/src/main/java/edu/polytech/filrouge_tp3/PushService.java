package edu.polytech.filrouge_tp3;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.squareup.picasso.Picasso;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class PushService extends FirebaseMessagingService {
    private static final String TAG = "frallo PushService";

    // Deux canaux : on route la notif selon le payload
    private static final String CHANNEL_URGENT = "urgences";
    private static final String CHANNEL_INFO = "infos";

    // id différent à chaque fois pour empiler les notifs
    private static final AtomicInteger NEXT_ID = new AtomicInteger(0);

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        // en prod on l'enverrait au backend
        Log.d(TAG, "Nouveau token : " + token);
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {
        super.onMessageReceived(message);

        String title = null;
        String body = null;
        String imageUrl = null;
        String channel = null;

        // payload notification
        if (message.getNotification() != null) {
            title = message.getNotification().getTitle();
            body = message.getNotification().getBody();
            if (message.getNotification().getImageUrl() != null) {
                imageUrl = message.getNotification().getImageUrl().toString();
            }
        }

        // payload data (écrase si présent)
        Map<String, String> data = message.getData();
        if (!data.isEmpty()) {
            if (data.get("titre") != null) title = data.get("titre");
            if (data.get("corps") != null) body = data.get("corps");
            if (data.get("image") != null) imageUrl = data.get("image");
            channel = data.get("channel"); // "urgent" ou "info"
        }

        if (title == null) title = "Nouveau signalement";
        if (body == null) body = "";

        showNotification(title, body, imageUrl, channel);
    }

    private void showNotification(String title, String body, String imageUrl, String channel) {
        createChannels();

        boolean urgent = channel == null || !channel.equalsIgnoreCase("info");
        String channelId = urgent ? CHANNEL_URGENT : CHANNEL_INFO;

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(urgent ? NotificationCompat.PRIORITY_HIGH : NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent);

        // grande image dépliée si une URL est fournie
        Bitmap picture = loadBitmap(imageUrl);
        if (picture != null) {
            builder.setLargeIcon(picture);
            builder.setStyle(new NotificationCompat.BigPictureStyle()
                    .bigPicture(picture)
                    .bigLargeIcon((Bitmap) null));
        }

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(NEXT_ID.incrementAndGet(), builder.build());
    }

    private Bitmap loadBitmap(String url) {
        if (url == null || url.isEmpty()) return null;
        try {
            return Picasso.get().load(url).get();
        } catch (Exception e) {
            Log.w(TAG, "Image de notif non chargée : " + url, e);
            return null;
        }
    }

    private void createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(new NotificationChannel(
                    CHANNEL_URGENT, "Urgences", NotificationManager.IMPORTANCE_HIGH));
            manager.createNotificationChannel(new NotificationChannel(
                    CHANNEL_INFO, "Infos", NotificationManager.IMPORTANCE_DEFAULT));
        }
    }
}
