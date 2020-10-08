package com.example.pavel.chatapp.MainActivities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.example.pavel.chatapp.Adapter_Modul.SharedPref;
import com.example.pavel.chatapp.MainActivities.UsersScreens.ActivityUsersContainer;
import com.example.pavel.chatapp.R;

public class SettingsActivity extends AppCompatActivity {

    private Switch mySwitch;
    private SharedPref sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setupSwitch();
    }

    private void setTheme() {
        sharedPreferences = new SharedPref(this);
        if (sharedPreferences.loadNightModeState() == true) {
            setTheme(R.style.AppTheme2);
        } else {
            setTheme(R.style.AppTheme);
        }
    }

    private void setupSwitch() {
        mySwitch = findViewById(R.id.settingsSwitch);

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
    }


}
