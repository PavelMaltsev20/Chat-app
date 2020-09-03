package com.example.pavel.chatapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pavel.chatapp.Adapter_Modul.SharedPref;
import com.example.pavel.chatapp.Chat.ChatActivity;

public class SettingsActivity extends AppCompatActivity {

    Switch mySwitch;
    Context context;
    SharedPref sharedPreferences;


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

    private void setupSwitch(){
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
        Intent intent = new Intent(this,SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void sendingMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("I glad you to helping me to improve my app, thank you")
                .setPositiveButton("close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder.show();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, ChatActivity.class);
        if(sharedPreferences.loadNightModeState() == true)
        intent.putExtra("first_theme", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


}
