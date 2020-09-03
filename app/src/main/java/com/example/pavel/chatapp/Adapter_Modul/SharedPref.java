package com.example.pavel.chatapp.Adapter_Modul;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {

    SharedPreferences mySharedPreferences;

    public SharedPref(Context context) {
        mySharedPreferences = context.getSharedPreferences("filename", Context.MODE_PRIVATE);
    }

    public void setNightModeState(Boolean state) {
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putBoolean("NightMode", state);
        editor.commit();
    }

    public Boolean loadNightModeState() {
        Boolean state = mySharedPreferences.getBoolean("NightMode", false);
        return state;
    }

}
