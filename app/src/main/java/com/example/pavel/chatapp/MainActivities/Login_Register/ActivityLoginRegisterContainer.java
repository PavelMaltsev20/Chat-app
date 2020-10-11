package com.example.pavel.chatapp.MainActivities.Login_Register;

import android.content.Context;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.example.pavel.chatapp.AdaptersAndModulus.FragmentAdapter;
import com.example.pavel.chatapp.MainActivities.UsersScreens.ActivityUsersContainer;
import com.example.pavel.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ActivityLoginRegisterContainer extends AppCompatActivity {

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ActivityUsersContainer.setTheme(this, initView()));

        initializeObjects();
        signInIfUserCreated();
        fragmentsCreating();
    }

    private View initView() {
        return LayoutInflater.from(this).inflate(R.layout.activity_login_register, null, false);
    }

    private void initializeObjects() {
        context = this;
    }

    private void signInIfUserCreated() {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            Intent intent = new Intent(context, ActivityUsersContainer.class);
            startActivity(intent);
            finish();
        }
    }

    private void fragmentsCreating() {
        ViewPager viewPager = findViewById(R.id.viewPagerLoginRegister);
        FragmentAdapter viewPagerAdapter = new FragmentAdapter(getSupportFragmentManager(), context);

        viewPagerAdapter.addFragment(new FragLogin(), "Login");
        viewPagerAdapter.addFragment(new FragRegister(), "Register");

        viewPager.setAdapter(viewPagerAdapter);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
