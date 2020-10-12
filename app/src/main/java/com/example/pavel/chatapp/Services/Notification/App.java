package com.example.pavel.chatapp.Services.Notification;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class App extends Application {

    public static final String CHANEL_ID_1 = "CHANEL_1";
    public static final String CHANEL_ID_2 = "CHANEL_1";

    @Override
    public void onCreate() {
        super.onCreate();

        initNotifications();
        checkMethod();
    }

    private void initNotifications() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel_1 = new NotificationChannel(CHANEL_ID_1, "CHANEL 1", NotificationManager.IMPORTANCE_HIGH);
            NotificationChannel channel_2 = new NotificationChannel(CHANEL_ID_2, "CHANEL 2", NotificationManager.IMPORTANCE_HIGH);

            channel_1.setDescription("New message");
            channel_2.setDescription("Repeated message");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel_1);
            manager.createNotificationChannel(channel_2);
        }

    }

    private void checkMethod() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("status");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue().toString().equals("offline")) {
                    startNotificationService();
                } else {
                    stopNotificationService();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void startNotificationService() {
        Intent serviceIntent = new Intent(this, NotificationService.class);
        startService(serviceIntent);
    }

    private void stopNotificationService() {
        Intent serviceIntent = new Intent(this, NotificationService.class);
        stopService(serviceIntent);
    }
}
