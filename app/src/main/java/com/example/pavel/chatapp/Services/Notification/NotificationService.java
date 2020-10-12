package com.example.pavel.chatapp.Services.Notification;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.example.pavel.chatapp.AdaptersAndModulus.Items.Message;
import com.example.pavel.chatapp.AdaptersAndModulus.Items.MyUser;
import com.example.pavel.chatapp.MainActivities.ChatWithUserActivity;
import com.example.pavel.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.HashMap;

public class NotificationService extends IntentService {

    /*
     * Notification background service
     */
    private final String TAG = "NotificationService";
    private DatabaseReference firebaseDatabase;
    private final int NOTIFY_ID = 16;
    private MyUser secondUser;

    public NotificationService() {
        super("NotificationService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Notification service created.");
        checkIfUserHasNotifiedAboutMessage();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        for (int i = 0; i < 10; i += 1) {
            SystemClock.sleep(1000);
            if (i%5 == 0 ) {
                i = 0;
                checkIfUserHasNotifiedAboutMessage();
            }
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        setIntentRedelivery(true);
        Log.i(TAG, "Notification service destroyed.");
    }

    private void checkIfUserHasNotifiedAboutMessage() {
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {

            firebaseDatabase = FirebaseDatabase.getInstance().getReference().child("Chats");

            firebaseDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Message message = snapshot.getValue(Message.class);

                        if (message.getReceiver().equals(currentUser.getUid())) {//Check if current message belong to current user
                            String firebaseValue = snapshot.child("isNotified").getValue().toString();
                            if (firebaseValue.equals("false")) {//CCheck if user was notified about this message
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("isNotified", true);
                                snapshot.getRef().updateChildren(hashMap);
                                message.setNotified(true);
                                getDataOfCurrentMessage(message);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
    }

    private void getDataOfCurrentMessage(final Message message) {

        firebaseDatabase = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(message.getSender());

        firebaseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //Initialize second user and getting data of message
                secondUser = dataSnapshot.getValue(MyUser.class);
                createNotificationWithAction(NOTIFY_ID, message.getMessage());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    //Creating new notification
    private void createNotificationWithAction(int nId, String body) {

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);//Creating notification manager
        PendingIntent pendingIntent = PendingIntent.getActivity(this, initRequestId(), openActivityWithCurrentUser(), initFlag());// On notification click will open chat with sender of the message

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, App.CHANEL_ID_1);
        notificationBuilder = setDataToNotification(notificationBuilder, body, pendingIntent);

        manager.notify(nId, notificationBuilder.build());
    }

    //------------------------------------------Part of notification's configuration ---------------------------------------------------
    //TODO add current method to notification manager
    @RequiresApi(api = Build.VERSION_CODES.O)
    private NotificationChannel setConfigurationOfNotification(NotificationChannel notificationChannel) {
        notificationChannel.setDescription("sending notifications");
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.BLUE);
        notificationChannel.enableVibration(true);
        notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
        return null;
    }

    private Intent openActivityWithCurrentUser() {
        Intent intent = new Intent(this, ChatWithUserActivity.class);
        intent.putExtra("userId", secondUser.getId());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    private int initRequestId() {
        return (int) System.currentTimeMillis() / 1000;
    }

    private int initFlag() {
        return PendingIntent.FLAG_CANCEL_CURRENT;
    }

    private NotificationCompat.Builder setDataToNotification(NotificationCompat.Builder notificationBuilder, String body, PendingIntent pendingIntent) {
        notificationBuilder.setContentTitle(secondUser.getUsername())
                .setContentText(body)
                .setSmallIcon(R.drawable.message_small_icon)
                .setTicker("New message")
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true);
        return notificationBuilder;
    }
}