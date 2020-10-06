package com.example.pavel.chatapp.Chat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import com.example.pavel.chatapp.Adapter_Modul.ChatFragmentAdapter;
import com.example.pavel.chatapp.Adapter_Modul.Items.MyUser;
import com.example.pavel.chatapp.Adapter_Modul.SharedPref;
import com.example.pavel.chatapp.Login_Register.ActivityLoginRegister;
import com.example.pavel.chatapp.ProfileActivity;
import com.example.pavel.chatapp.R;
import com.example.pavel.chatapp.Services.MyServiceBinder;
import com.example.pavel.chatapp.Services.NotificationService;
import com.example.pavel.chatapp.SettingsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChatActivity extends AppCompatActivity {

    Context context;

    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    DatabaseReference databaseReference;
    SharedPref sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        updateNewTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentCreating();

        setPointer();
    }


    private void updateNewTheme() {
        sharedPref = new SharedPref(this);
        if (sharedPref.loadNightModeState() == true) {
            setTheme(R.style.AppTheme2);
        } else {
            setTheme(R.style.AppTheme);
        }
    }

    private void setPointer() {
        context = this;
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser.getUid() != null) {
            databaseReference = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(firebaseUser.getUid());
        }

        serviceIntent = new Intent(context, NotificationService.class);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                MyUser myUser = dataSnapshot.getValue(MyUser.class);

                if (myUser != null) {
                    getSupportActionBar().setTitle(myUser.getUsername());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    //Part of creating chat fragments
    private void fragmentCreating() {

        final ViewPager viewPager = findViewById(R.id.viewPagerChat);

        databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ChatFragmentAdapter chatFragmentAdapter = new ChatFragmentAdapter(getSupportFragmentManager());

                chatFragmentAdapter.addFragment(new LastChatsFragment(), "Last Chats");
                chatFragmentAdapter.addFragment(new AllUsersFragment(), "Users");

                viewPager.setAdapter(chatFragmentAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    //Part of creating menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch ((item.getItemId())) {
            case R.id.menuProfile:
                startActivity(new Intent(ChatActivity.this, ProfileActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                return true;
            case R.id.menuSettings:
                startActivity(new Intent(ChatActivity.this, SettingsActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                return true;
            case R.id.menuLogout:
                mAuth.signOut();
                Intent intent1 = new Intent(ChatActivity.this, ActivityLoginRegister.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent1);
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    //For notification
    Intent serviceIntent;
    NotificationService myNotificationService;

    @Override
    protected void onResume() {
        super.onResume();

        try {
            context.unbindService(serviceConnection);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean bindService(Intent service, ServiceConnection conn, int flags) {
        return super.bindService(service, conn, flags);
    }

    //Creating service connection
    ServiceConnection serviceConnection = new ServiceConnection() {
        // method that runs when the service connected
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            myNotificationService = ((MyServiceBinder) binder).service;
        }


        // method that runs when the service disconnected
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };



}

