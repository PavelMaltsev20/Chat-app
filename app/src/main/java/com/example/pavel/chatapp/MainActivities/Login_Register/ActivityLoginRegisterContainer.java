package com.example.pavel.chatapp.MainActivities.Login_Register;

import android.content.Context;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.example.pavel.chatapp.Adapter_Modul.FragmentAdapter;
import com.example.pavel.chatapp.Adapter_Modul.SharedPref;
import com.example.pavel.chatapp.MainActivities.UsersScreens.ActivityUsers;
import com.example.pavel.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ActivityLoginRegisterContainer extends AppCompatActivity {

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ActivityUsers.setTheme(this, initView()));

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
            Intent intent = new Intent(context, ActivityUsers.class);
            startActivity(intent);
            finish();
        }
    }

    private void fragmentsCreating() {
        ViewPager viewPager = findViewById(R.id.viewPagerLoginRegister);
        FragmentAdapter viewPagerAdapter = new FragmentAdapter(getSupportFragmentManager(), context);

        viewPagerAdapter.addFragment(new FragLogin(context), "Login");
        viewPagerAdapter.addFragment(new FragRegister(context), "Register");

        viewPager.setAdapter(viewPagerAdapter);
    }


}
