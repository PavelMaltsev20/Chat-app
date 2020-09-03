package com.example.pavel.chatapp.Login_Register;

import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.pavel.chatapp.Chat.ChatActivity;
import com.example.pavel.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginRegisterActivity extends AppCompatActivity {

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);

        //Check if current user is sign in
        context = this;
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            Intent intent = new Intent(context, ChatActivity.class);
            startActivity(intent);
        }

        fragmentsCreating();
    }

    private void fragmentsCreating() {


        ViewPager viewPager = findViewById(R.id.viewPagerLoginRegister);
        LoginRegisterFragmentAdapter viewPagerAdapter = new LoginRegisterFragmentAdapter(getSupportFragmentManager(), context);

        viewPagerAdapter.addFragment(new LoginFragment(), "Login");
        viewPagerAdapter.addFragment(new RegisterFragment(), "Register");

        viewPager.setAdapter(viewPagerAdapter);

    }




}
