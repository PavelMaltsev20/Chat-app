package com.example.pavel.chatapp.Services.Notification;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Color;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class App extends Application {

    public static String CHANNEL_ID_1 = "CHANNEL_";

    @Override
    public void onCreate() {
        super.onCreate();
        initChannelOfCurrentUser();
        initNotifications();
    }

    private void initChannelOfCurrentUser() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            CHANNEL_ID_1 = CHANNEL_ID_1 + firebaseUser.getUid();
        }
    }

    private void initNotifications() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel_1 = new NotificationChannel(CHANNEL_ID_1, "CHANNEL 1", NotificationManager.IMPORTANCE_HIGH);

            channel_1.setDescription("New message");
            channel_1.enableLights(true);
            channel_1.setLightColor(Color.BLUE);
            channel_1.enableVibration(true);
            channel_1.setVibrationPattern(new long[]{0, 1000, 500, 1000});

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel_1);
        }

    }

}
