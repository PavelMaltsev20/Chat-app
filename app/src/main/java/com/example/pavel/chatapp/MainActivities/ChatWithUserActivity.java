package com.example.pavel.chatapp.MainActivities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pavel.chatapp.AdaptersAndModulus.MessageAdapter;
import com.example.pavel.chatapp.AdaptersAndModulus.Items.Message;
import com.example.pavel.chatapp.AdaptersAndModulus.Items.MyUser;
import com.example.pavel.chatapp.MainActivities.UserListsFrag.ActivityUsersContainer;
import com.example.pavel.chatapp.R;
import com.example.pavel.chatapp.Services.Notification.NotificationService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.pavel.chatapp.MainActivities.UserListsFrag.ActivityUsersContainer.status;


public class ChatWithUserActivity extends Activity {

    public static final String TAG = "ChatWithUserActivity";
    private Context context;

    //Firebase objects
    private ValueEventListener isSeenEventListener;
    private DatabaseReference databaseReference;
    private DatabaseReference isSeenReference;
    private FirebaseUser firebaseUser;

    //Helpful objects
    private String currentUserId, secondUserId;
    private Intent intentWithSecondUserId;

    //Objects from main activity
    private CircleImageView profile_image;
    private FloatingActionButton sendBtn;
    private RecyclerView recyclerView;
    private EditText message_et;
    private TextView username;
    private List<Message> messageList;
    private MessageAdapter messageAdapter;
    
    //Notifications objects
    private Intent serviceIntent;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ActivityUsersContainer.setTheme(this, initView()));

        initializeObjects();
        initObjectsForNotifications();
        findDataOfSecondUser();
        iniListeners();
        checkIfCurrentUserSeenMessage();
    }

    private void initObjectsForNotifications() {
        //Intent and sharedPref for create notification class
        serviceIntent = new Intent(context, NotificationService.class);
        sharedPref = getSharedPreferences(getString(R.string.shared_pref_notification_name), 0);
        editor = sharedPref.edit();
    }

    public View initView() {
        return LayoutInflater.from(this).inflate(R.layout.activity_chat_with_user, null, false);
    }

    private void initializeObjects() {
        context = this;

        //Toolbar with main info about second user
        Toolbar toolbar = findViewById(R.id.toolbar);
        profile_image = toolbar.findViewById(R.id.barImage);
        username = toolbar.findViewById(R.id.barTextView);

        recyclerView = findViewById(R.id.chatWithUserRecyclerView);
        message_et = findViewById(R.id.chatWithUserEditText);
        sendBtn = findViewById(R.id.chatWithUserFAB);

        //init recycle view for messages
        messageList = new ArrayList<>();
        recyclerView.setAdapter(getMessageAdapter());
        recyclerView.setLayoutManager(getLinearLayout());

        //Get ids of two user
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = firebaseUser.getUid();
        intentWithSecondUserId = getIntent();
        secondUserId = intentWithSecondUserId.getStringExtra("userId");
    }

    private RecyclerView.Adapter getMessageAdapter() {
        messageAdapter = new MessageAdapter(context, messageList);
        return messageAdapter;
    }

    private RecyclerView.LayoutManager getLinearLayout() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, RecyclerView.VERTICAL, false);
        linearLayoutManager.setStackFromEnd(true);
        return linearLayoutManager;
    }

    private void iniListeners() {
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkIfMessageIsEmpty();
            }
        });
    }

    //---------------------------------------- Set data of second user to toolbar ----------------------------------------
    public void findDataOfSecondUser() {
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(secondUserId);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //Looking for user data from firebase
                MyUser myUser = dataSnapshot.getValue(MyUser.class);
                if (myUser.getId().equals(secondUserId)) {
                    setDataOfSecondUser(myUser);
                }

                initMessagesList();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setDataOfSecondUser(MyUser myUser) {
        username.setText(myUser.getUsername());
        username.setTextColor(Color.WHITE);
        if (myUser.getImageURL().equals("default")) {
            profile_image.setImageResource(R.drawable.ic_user_profile);
        } else {
            Glide.with(getBaseContext()).load(myUser.getImageURL()).into(profile_image);
        }
    }

    //---------------------------------------- Get messages from firebase database----------------------------------------
    private void initMessagesList() {

        databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                messageList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    Message message = snapshot.getValue(Message.class);

                    //Looking for messages of out two users
                    if (checkIfMessageBelongToCurrentUser(message)) {

                        messageList.add(message);

                    }
                }

                messageAdapter.notifyDataSetChanged();
                if (recyclerView.getAdapter().getItemCount() > 1) {
                    recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private boolean checkIfMessageBelongToCurrentUser(Message message) {
        return message.getReceiver().equals(currentUserId) && message.getSender().equals(secondUserId)
                || message.getReceiver().equals(secondUserId) && message.getSender().equals(currentUserId);
    }

    //---------------------------------------- Save message in firebase --------------------------------------------------
    private void checkIfMessageIsEmpty() {
        String message = message_et.getText().toString();

        if (message.isEmpty()) {
            Toast.makeText(context, "Empty message", Toast.LENGTH_SHORT).show();
        } else {
            sendMessage(firebaseUser.getUid(), secondUserId, message);
            message_et.setText("");
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    //Push messages to firebase database
    public void sendMessage(String sender, final String userId, final String message) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", userId);
        hashMap.put("message", message);
        hashMap.put("isSeen", false);
        hashMap.put("isNotified", false);

        reference.child("Chats").push().setValue(hashMap);

        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(firebaseUser.getUid())
                .child(userId);

        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    chatRef.child("id").setValue(userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        final DatabaseReference chatRefReceiver = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(userId)
                .child(firebaseUser.getUid());
        chatRefReceiver.child("id").setValue(firebaseUser.getUid());

    }

    //---------------------------------------- 'isSeen' update values in firebase if user seen message -------------------
    private void checkIfCurrentUserSeenMessage() {
        isSeenReference = FirebaseDatabase.getInstance().getReference().child("Chats");
        isSeenEventListener = isSeenReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    if (checkIfMessageBelongToCurrentUser(message)) {
                        if (checkIfCurrentUserAreReceiver(message)) {
                            updateDataInFirebase(snapshot);

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateDataInFirebase(DataSnapshot snapshot) {
        HashMap hashMap = new HashMap();
        hashMap.put("isSeen", true);
        hashMap.put("isNotified", true);
        snapshot.getRef().updateChildren(hashMap);
    }

    private boolean checkIfCurrentUserAreReceiver(Message message) {
        return message.getReceiver().equals(currentUserId);
    }

    //---------------------------------------- Service part and user status updater(Online offline) -----------------------
    @Override
    protected void onStart() {
        super.onStart();
        stopNotificationService();
    }

    private void stopNotificationService() {
        editor.putBoolean(getString(R.string.shared_pref_notification_value), false);
        editor.commit();
        stopService(serviceIntent);
    }

    @Override
    protected void onStop() {
        isSeenReference.removeEventListener(isSeenEventListener);
        startNotificationService();
        super.onStop();
    }

    private void startNotificationService() {
        startService(serviceIntent);
        editor.putBoolean(getString(R.string.shared_pref_notification_value), true);
        editor.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    public void onPause() {
        super.onPause();
        status("offline");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}






