package com.example.pavel.chatapp.Adapter_Modul;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.pavel.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;

public class SharedPref {

    SharedPreferences mySharedPreferences;

    public SharedPref(Context context) {
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        mySharedPreferences = context.getSharedPreferences(userEmail + "_shared_pref", Context.MODE_PRIVATE);
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
