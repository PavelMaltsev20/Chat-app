package com.example.pavel.chatapp.MainActivities.Login_Register;

import android.content.Context;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.pavel.chatapp.MainActivities.UsersScreens.ActivityUsersContainer;
import com.example.pavel.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ActivityLoginRegisterContainer extends AppCompatActivity {

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);

        //Check if current user is sign in
        context = this;
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            Intent intent = new Intent(context, ActivityUsersContainer.class);
            startActivity(intent);
        }

        fragmentsCreating();
    }

    private void fragmentsCreating() {


        ViewPager viewPager = findViewById(R.id.viewPagerLoginRegister);
        LogRegFragmentAdapter viewPagerAdapter = new LogRegFragmentAdapter(getSupportFragmentManager(), context);

        viewPagerAdapter.addFragment(new FragLogin(), "Login");
        viewPagerAdapter.addFragment(new FragRegister(context), "Register");

        viewPager.setAdapter(viewPagerAdapter);

    }




}
