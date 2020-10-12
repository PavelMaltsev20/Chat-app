package com.example.pavel.chatapp.MainActivities.UsersScreens;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.pavel.chatapp.AdaptersAndModulus.FragmentAdapter;
import com.example.pavel.chatapp.AdaptersAndModulus.Items.MyUser;
import com.example.pavel.chatapp.AdaptersAndModulus.SharedPref;
import com.example.pavel.chatapp.MainActivities.Login_Register.ActivityLoginRegisterContainer;
import com.example.pavel.chatapp.MainActivities.SupportActivities.ProfileActivity;
import com.example.pavel.chatapp.R;
import com.example.pavel.chatapp.Services.ConnectionBroadcastReceiver;
import com.example.pavel.chatapp.MainActivities.SupportActivities.SettingsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.example.pavel.chatapp.MainActivities.ChatWithUserActivity.status;

public class ActivityUsersContainer extends AppCompatActivity {

    private final String TAG = "ActivityUsersContainer";
    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;
    private FirebaseAuth mAuth;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ActivityUsersContainer.setTheme(this, initView()));

        initFragments();
        initializeObjects();
        initializeReference();
        setTitleWithUserName();
    }

    public static View setTheme(Context context, View view) {
        SharedPref sharedPreferences = new SharedPref(context);
        if (sharedPreferences.loadNightModeState()) {
            context.setTheme(R.style.AppTheme2);
            view.setBackground(AppCompatResources.getDrawable(context, R.drawable.app_background_blue));
        } else {
            context.setTheme(R.style.AppTheme);
            view.setBackground(AppCompatResources.getDrawable(context, R.drawable.app_background_green));
        }
        return view;
    }

    public View initView() {
        return LayoutInflater.from(this).inflate(R.layout.activity_users, null, false);
    }

    //Part of creating chat fragments
    private void initFragments() {
        final ViewPager viewPager = findViewById(R.id.viewPagerChat);
        databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        viewPager.setAdapter(getUsersAdapter());
    }

    private void initializeObjects() {
        context = this;
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
    }

    private void initializeReference() {
        if (firebaseUser != null && firebaseUser.getUid() != null) {
            databaseReference = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(firebaseUser.getUid());
        }
    }

    private void setTitleWithUserName() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                MyUser myUser = dataSnapshot.getValue(MyUser.class);
                if (myUser != null) {
                    getSupportActionBar().setTitle(getUserName(myUser));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private String getUserName(MyUser myUser) {
        String name = myUser.getUsername();
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        return name;
    }


    private PagerAdapter getUsersAdapter() {
        FragmentAdapter fragmentAdapter = new FragmentAdapter(getSupportFragmentManager(), context);
        fragmentAdapter.addFragment(new FragLastChats(), "Last Chats");
        fragmentAdapter.addFragment(new FragSearchUsers(), "Users");
        return fragmentAdapter;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    //----------------------------Menu part ----------------------------------------
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
                startActivity(new Intent(ActivityUsersContainer.this, ProfileActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                return true;
            case R.id.menuSettings:
                startActivity(new Intent(ActivityUsersContainer.this, SettingsActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                return true;
            case R.id.menuLogout:
                mAuth.signOut();
                Intent intent1 = new Intent(ActivityUsersContainer.this, ActivityLoginRegisterContainer.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent1);
                finish();
                return true;
        }
        return false;
    }

    //----------------------------Service part (Broadcast receiver) ----------------------------------------
    private ConnectionBroadcastReceiver receiver = new ConnectionBroadcastReceiver();

    @Override
    public void onResume() {
        super.onResume();
        status("online"); //Static method from 'ChatWithUserActivity'
    }

    @Override
    public void onPause() {
        super.onPause();
        status("offline");//Static method from 'ChatWithUserActivity'
    }

    @Override
    protected void onStart() {
        super.onStart();
        startBroadcastReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopBroadcastReceiver();
    }

    private void startBroadcastReceiver() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver, filter);
    }

    private void stopBroadcastReceiver() {
        unregisterReceiver(receiver);
    }


}

