package com.example.pavel.chatapp.MainActivities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.example.pavel.chatapp.Adapter_Modul.SharedPref;
import com.example.pavel.chatapp.MainActivities.Login_Register.ActivityLoginRegisterContainer;
import com.example.pavel.chatapp.MainActivities.UsersScreens.ActivityUsers;
import com.example.pavel.chatapp.R;

public class SettingsActivity extends AppCompatActivity {

    private Switch mySwitch;
    private SharedPref sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ActivityUsers.setTheme(this, initView()));

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
        Intent intent = new Intent(this, ActivityUsers.class);
        if (sharedPreferences.loadNightModeState() == true)
            intent.putExtra("first_theme", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


}
