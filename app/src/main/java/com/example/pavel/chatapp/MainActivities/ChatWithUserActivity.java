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
import com.example.pavel.chatapp.MainActivities.UsersScreens.ActivityUsersContainer;
import com.example.pavel.chatapp.R;
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

public class ChatWithUserActivity extends Activity {

    private DatabaseReference databaseReference;
    private ValueEventListener seenListener;
    private CircleImageView profile_image;
    private MessageAdapter messageAdapter;
    private FloatingActionButton sendBtn;
    private RecyclerView recyclerView;
    private List<Message> messageList;
    private FirebaseUser firebaseUser;
    private EditText message_et;
    private String secondUserIdFromIntent;
    private TextView username;
    private Context context;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ActivityUsersContainer.setTheme(this, initView()));

        initializeObjects();
        findDataOfSecondUser();
        iniListeners();
        //seenMessage(secondUserIdFromIntent);
    }

    public View initView() {
        return LayoutInflater.from(this).inflate(R.layout.activity_chat_with_user, null, false);
    }

    private void initializeObjects() {
        context = this;
        Toolbar toolbar = findViewById(R.id.toolbar);
        sendBtn = findViewById(R.id.chatWithUserFAB);
        message_et = findViewById(R.id.chatWithUserEditText);
        recyclerView = findViewById(R.id.chatWithUserRecyclerView);

        profile_image = toolbar.findViewById(R.id.barImage);
        username = toolbar.findViewById(R.id.barTextView);

        messageList = new ArrayList<>();
        recyclerView.setAdapter(getMessageAdapter());
        recyclerView.setLayoutManager(getLinearLayout());

        intent = getIntent();
        secondUserIdFromIntent = intent.getStringExtra("userId");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(secondUserIdFromIntent);
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
                sendingMessage();
            }
        });
    }

    public void findDataOfSecondUser() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //Looking for user data from firebase
                MyUser myUser = dataSnapshot.getValue(MyUser.class);
                if (myUser.getId().equals(secondUserIdFromIntent)) {
                    setDataOfSecondUser(myUser);
                }

                initMessagesList(firebaseUser.getUid(), secondUserIdFromIntent);
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

    //Get messages from firebase database
    private void initMessagesList(final String myId, final String userId) {

        databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                messageList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    Message message = snapshot.getValue(Message.class);

                    //Looking for messages of out two users
                    if (message.getReceiver().equals(myId) && message.getSender().equals(userId)
                            || message.getReceiver().equals(userId) && message.getSender().equals(myId)) {
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

    private void sendingMessage() {
        String message = message_et.getText().toString();

        if (message.isEmpty()) {
            Toast.makeText(context, "Empty message", Toast.LENGTH_SHORT).show();
        } else {
            sendMessage(firebaseUser.getUid(), secondUserIdFromIntent, message);
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
        hashMap.put("isseen", false);
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

    private void seenMessage(final String userid) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);

                    if (message.getReceiver().equals(firebaseUser.getUid()) && message.getSender().equals(userid)) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen", true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void currentUser(String userId) {
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentuser", userId);
        editor.apply();
    }

    //Check if current user online or offline
    private void status(String status) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        databaseReference.updateChildren(hashMap);
    }

    @Override
    public void onResume() {
        super.onResume();
        status("online");
        currentUser(secondUserIdFromIntent);
    }

    @Override
    public void onPause() {
        super.onPause();
        //databaseReference.removeEventListener(seenListener);
        status("offline");
        currentUser("none");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
















