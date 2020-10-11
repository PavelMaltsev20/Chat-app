package com.example.pavel.chatapp.MainActivities.SupportActivities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.example.pavel.chatapp.MainActivities.UsersScreens.ActivityUsersContainer;
import com.example.pavel.chatapp.R;

public class NoConnection extends AppCompatActivity {

    private Button reload_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ActivityUsersContainer.setTheme(this, initView()));

        initializeObjects();
        initListeners();
    }

    private View initView() {
        return LayoutInflater.from(this).inflate(R.layout.activity_no_connection, null, false);
    }

    private void initializeObjects() {
        reload_btn = findViewById(R.id.noConnection_btn_reload);
    }

    private void initListeners() {
             reload_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }


}