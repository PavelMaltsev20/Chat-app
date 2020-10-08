package com.example.pavel.chatapp.Services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.example.pavel.chatapp.Adapter_Modul.Items.Message;
import com.example.pavel.chatapp.Adapter_Modul.Items.MyUser;
import com.example.pavel.chatapp.MainActivities.ChatWithUserActivity;
import com.example.pavel.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.nio.file.attribute.AclEntry;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class NotificationService extends Service {

    private boolean isDelivered = false;
    private FirebaseUser firebaseUser;

    public NotificationService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        MyServiceBinder binder = new MyServiceBinder();
        binder.service = this;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        notificationListener();
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    private void notificationListener() {

        if (firebaseUser != null) {

            final DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference().child("Chats");
            final String currentUserId = firebaseUser.getUid();

            firebaseDatabase.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {


                    try {
                        Message chat = dataSnapshot.getValue(Message.class);
                        if (chat.getReceiver().equals(currentUserId) && dataSnapshot.child("isNotified").getValue().toString().equals("false")) {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("isNotified", true);
                            dataSnapshot.getRef().updateChildren(hashMap);
                            chat.setNotified(true);
                            waitingForUserData(chat);
                        }
                    } catch (Exception e) {
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
    }

    //Chat get  message of user and service start work in other activities
    private void waitingForUserData(final Message chat) {

        final DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        firebaseDatabase.child(chat.getSender()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final MyUser userSender = dataSnapshot.getValue(MyUser.class);
                firebaseDatabase.child(chat.getReceiver()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        MyUser userReceiver = dataSnapshot.getValue(MyUser.class);
                        createNotificationWithAction(16, userReceiver, userSender, chat.getMessage());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    //Notification method
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationWithAction(int nId, MyUser myCurrentUser, MyUser secondUser, String body) {

        final String NOTIFICATION_CHANNEL_ID = myCurrentUser.getId();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Chat app", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(addDataToNotificationChannel(notificationChannel));
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, initRequestId(), initIntent(secondUser), initFlag());

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        notificationBuilder = initNotificationBuilder(notificationBuilder, secondUser, body, pendingIntent);
        notificationManager.notify(nId, notificationBuilder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private NotificationChannel addDataToNotificationChannel(NotificationChannel notificationChannel) {
        notificationChannel.setDescription("sending notifications");
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.BLUE);
        notificationChannel.enableVibration(true);
        notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
        return null;
    }

    private Intent initIntent(MyUser secondUser) {
        Intent intent = new Intent(getApplicationContext(), ChatWithUserActivity.class);
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

    private NotificationCompat.Builder initNotificationBuilder(NotificationCompat.Builder notificationBuilder, MyUser secondUser, String body, PendingIntent pendingIntent) {
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