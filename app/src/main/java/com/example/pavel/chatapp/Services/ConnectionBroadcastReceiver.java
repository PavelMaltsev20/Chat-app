package com.example.pavel.chatapp.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.example.pavel.chatapp.MainActivities.SupportActivities.NoConnectionNotifier;

public class ConnectionBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())){
            boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            //If phone lost connection it will notify user about it
            if(noConnectivity){
                Intent myIntent = new Intent(context, NoConnectionNotifier.class);
                context.startActivity(myIntent);
            }
        }
    }
}
