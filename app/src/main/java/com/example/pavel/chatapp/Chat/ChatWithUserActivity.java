package com.example.pavel.chatapp.Chat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.example.pavel.chatapp.Adapter_Modul.MessageAdapter;
import com.example.pavel.chatapp.Adapter_Modul.MyChat;
import com.example.pavel.chatapp.Adapter_Modul.MyUser;
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

    CircleImageView profile_image;
    TextView username;
    Context context;

    FirebaseUser firebaseUser;
    DatabaseReference databaseReference;

    Intent intent;

    FloatingActionButton sendBtn;
    EditText messageET;

    RecyclerView recyclerView;
    MessageAdapter messageAdapter;
    List<MyChat> myChatList;

    ValueEventListener seenListener;
    String userId;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_with_user);

        context = this;
        Toolbar toolbar = findViewById(R.id.toolbar);
        profile_image = toolbar.findViewById(R.id.barImage);
        username = toolbar.findViewById(R.id.barTextView);

        setPointer();
    }


    private void setPointer() {
        sendBtn = findViewById(R.id.chatWithUserFAB);
        messageET = findViewById(R.id.chatWithUserEditText);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);
        linearLayoutManager.setStackFromEnd(true);

        recyclerView = findViewById(R.id.chatWithUserRecyclerView);

        myChatList = new ArrayList<>();
        messageAdapter = new MessageAdapter(context, myChatList, "");
        recyclerView.setAdapter(messageAdapter);
        recyclerView.setLayoutManager(linearLayoutManager);

        intent = getIntent();
        userId = intent.getStringExtra("userId");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String message = messageET.getText().toString();

                if (message.isEmpty()) {
                    Toast.makeText(context, "Empty message", Toast.LENGTH_SHORT).show();
                } else {
                    sendMessage(firebaseUser.getUid(), userId, message);
                    messageET.setText("");
                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }
            }
        });


        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                MyUser myUser = dataSnapshot.getValue(MyUser.class);
                username.setText(myUser.getUsername());

                if (myUser.getImageURL().equals("default")) {
                    profile_image.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(getBaseContext()).load(myUser.getImageURL()).into(profile_image);
                }

                readMessages(firebaseUser.getUid(), userId, myUser.getImageURL());
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        seenMessage(userId);
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


    //Get messages from firebase database
    private void readMessages(final String myId, final String userId, final String imageUrl) {

        databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                myChatList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    MyChat chat = snapshot.getValue(MyChat.class);

                    if (chat.getReceiver().equals(myId) && chat.getSender().equals(userId)
                            || chat.getReceiver().equals(userId) && chat.getSender().equals(myId)) {
                        myChatList.add(chat);

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
        currentUser(userId);
    }

    @Override
    public void onPause() {
        super.onPause();
        databaseReference.removeEventListener(seenListener);
        status("offline");
        currentUser("none");
    }

    private void seenMessage(final String userid) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MyChat chat = snapshot.getValue(MyChat.class);

                    if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid)) {
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
}
















