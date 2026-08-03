package tech.id.kasirapp;

import android.Manifest;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService
        extends FirebaseMessagingService {


    @Override
    public void onMessageReceived(RemoteMessage message) {


        String title =
                message.getNotification().getTitle();


        String body =
                message.getNotification().getBody();


        showNotification(title, body);

    }



    private void showNotification(
            String title,
            String body
    ){

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(
                        this,
                        "kasir_channel"
                )

                        .setSmallIcon(
                                R.drawable.ic_notification
                        )

                        .setContentTitle(title)

                        .setContentText(body)

                        .setPriority(
                                NotificationCompat.PRIORITY_HIGH
                        );


        if (
                ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                )
                        != PackageManager.PERMISSION_GRANTED
        ) {

            return;

        }


        NotificationManagerCompat
                .from(this)
                .notify(
                        1,
                        builder.build()
                );

    }

}
