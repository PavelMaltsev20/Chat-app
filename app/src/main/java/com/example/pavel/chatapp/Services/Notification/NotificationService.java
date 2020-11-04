package com.example.pavel.chatapp.Services.Notification;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.HashMap;

import static com.example.pavel.chatapp.Services.Notification.App.CHANNEL_ID_1;

public class NotificationService extends IntentService {

    /*
     * Notification background service
     */
    private final String TAG = "NotificationService";
    private DatabaseReference chatsDatabase, userDatabase;
    private ValueEventListener notificationEventListener, userEventListener;
    private final int NOTIFY_ID = 16;
    private MyUser secondUser;

    public NotificationService() {
        super("NotificationService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Notification service created, channel: " + CHANNEL_ID_1);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        checkIfUserHasNotifiedAboutMessage();

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.shared_pref_notification_name), 0); //Service will wait for new notification while user offline
        while (sharedPref.getBoolean(getString(R.string.shared_pref_notification_value), false)) {
            SystemClock.sleep(1000);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (notificationEventListener != null)
            chatsDatabase.removeEventListener(notificationEventListener);
        if (userEventListener != null)
            userDatabase.removeEventListener(userEventListener);
        Log.i(TAG, "Notification service destroyed.");
    }

    //------------------------------------------ Part of getting data for notification ------------------------------------------
    private void checkIfUserHasNotifiedAboutMessage() {
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {

            chatsDatabase = FirebaseDatabase.getInstance().getReference().child("Chats");

            notificationEventListener = chatsDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Message message = snapshot.getValue(Message.class);

                        if (message.getReceiver().equals(currentUser.getUid())) {//Check if current message belong to current user
                            String firebaseValue = snapshot.child("isNotified").getValue().toString();
                            if (firebaseValue.equals("false") && !message.isNotified()) {//CCheck if user was notified about this message
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

        userDatabase = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(message.getSender());

        userEventListener = userDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //Initialize second user and getting data of message
                secondUser = dataSnapshot.getValue(MyUser.class);
                createNotificationWithAction(message.getMessage());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    //------------------------------------------ Part of notification's configuration ------------------------------------------
    private void createNotificationWithAction(String body) {

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);//Creating notification manager
        PendingIntent pendingIntent = PendingIntent.getActivity(this, initRequestId(), openActivityWithCurrentUser(), initFlag());// On notification click will open chat with 'sender' of this the message

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID_1);
        notificationBuilder = setDataToNotification(notificationBuilder, body, pendingIntent);

        manager.notify(NOTIFY_ID, notificationBuilder.build());
    }

    private int initRequestId() {
        return (int) System.currentTimeMillis() / 1000;
    }

    private Intent openActivityWithCurrentUser() {
        Intent intent = new Intent(this, ChatWithUserActivity.class);
        intent.putExtra("userId", secondUser.getId());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    private int initFlag() {
        return PendingIntent.FLAG_CANCEL_CURRENT;
    }
    
    private NotificationCompat.Builder setDataToNotification(NotificationCompat.Builder notificationBuilder, String message, PendingIntent pendingIntent) {
        notificationBuilder.setContentTitle(secondUser.getUsername())
                .setContentText(message)
                .setSmallIcon(R.drawable.message_small_icon)
                .setTicker("New message")
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true);
        return notificationBuilder;
    }
}
