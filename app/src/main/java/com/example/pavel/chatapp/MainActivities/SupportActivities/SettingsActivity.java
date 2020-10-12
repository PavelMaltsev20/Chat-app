package com.example.pavel.chatapp.MainActivities.SupportActivities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.example.pavel.chatapp.AdaptersAndModulus.SharedPref;
import com.example.pavel.chatapp.MainActivities.UsersScreens.ActivityUsersContainer;
import com.example.pavel.chatapp.R;

import static com.example.pavel.chatapp.MainActivities.ChatWithUserActivity.status;

public class SettingsActivity extends AppCompatActivity {

    private Switch mySwitch;
    private SharedPref sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ActivityUsersContainer.setTheme(this, initView()));

        initializeObjects();
        setupSwitch();
    }

    public View initView() {
        return LayoutInflater.from(this).inflate(R.layout.activity_settings, null, false);
    }

    private void initializeObjects() {
        mySwitch = findViewById(R.id.settingsSwitch);
        sharedPreferences = new SharedPref(this);
    }

    private void setupSwitch() {

        if (sharedPreferences.loadNightModeState() == true) {
            mySwitch.setChecked(true);
        }

        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sharedPreferences.setNightModeState(true);
                    reloadActivity();
                } else {
                    sharedPreferences.setNightModeState(false);
                    reloadActivity();
                }
            }
        });
    }

    private void reloadActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, ActivityUsersContainer.class);
        if (sharedPreferences.loadNightModeState() == true)
            intent.putExtra("first_theme", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

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


}
