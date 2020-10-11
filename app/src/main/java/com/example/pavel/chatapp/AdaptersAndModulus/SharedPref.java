package com.example.pavel.chatapp.AdaptersAndModulus;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.pavel.chatapp.R;

public class SharedPref {

    SharedPreferences mySharedPreferences;

    public SharedPref(Context context) {
        mySharedPreferences = context.getSharedPreferences("shared_pref", Context.MODE_PRIVATE);
    }

    public void setNightModeState(Boolean state) {
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putBoolean(String.valueOf(R.string.shared_pref_night_mode), state);
        editor.commit();
    }

    public Boolean loadNightModeState() {
        Boolean state = mySharedPreferences.getBoolean(String.valueOf(R.string.shared_pref_night_mode), false);
        return state;
    }

}
