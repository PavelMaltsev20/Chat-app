package com.example.pavel.chatapp.MainActivities.UsersScreens;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pavel.chatapp.AdaptersAndModulus.Items.ChatList;
import com.example.pavel.chatapp.AdaptersAndModulus.Items.MyUser;
import com.example.pavel.chatapp.AdaptersAndModulus.UserAdapter;
import com.example.pavel.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.example.pavel.chatapp.MainActivities.ChatWithUserActivity.status;

public class FragLastChats extends Fragment {

    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;
    private RecyclerView recyclerView;
    private List<ChatList> usersList;
    private UserAdapter userAdapter;
    private List<MyUser> myUserList;
    private ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_last_chats, container, false);
        initializeObjects(view);
        getListOfLastChats();

        return view;
    }

    private void initializeObjects(View view) {
        recyclerView = view.findViewById(R.id.conversationRV);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Chatlist").child(firebaseUser.getUid());
        progressBar = view.findViewById(R.id.fragLastChats_PB);
        usersList = new ArrayList<>();
        myUserList = new ArrayList<>();
    }

    private void getListOfLastChats() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                usersList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatList chatlist = snapshot.getValue(ChatList.class);
                    usersList.add(chatlist);
                }
                checkIfListIsEmpty();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkIfListIsEmpty() {
        if (usersList.size() == 0) {
            Toast.makeText(getContext(), "You have not conversations with other users", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        } else {
            getDataOfLastCommunicatedUsers();
        }
    }

    //Display users that user chatted with them
    private void getDataOfLastCommunicatedUsers() {

        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myUserList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    MyUser user = snapshot.getValue(MyUser.class);
                    for (ChatList chatlist : usersList) {
                        if (user.getId().equals(chatlist.getId())) {
                            myUserList.add(user);
                        }
                    }

                }
                userAdapter = new UserAdapter(getContext(), myUserList, true);
                recyclerView.setAdapter(userAdapter);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}
