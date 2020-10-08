package com.example.pavel.chatapp.Services;

import android.app.Application;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

public class App extends   Application{

    NotificationService myNotificationService;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            getBaseContext().unbindService(serviceConnection);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    ServiceConnection serviceConnection = new ServiceConnection() {
        // method that runs when the service connected
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            myNotificationService = ((MyServiceBinder) binder).service;
        }
        // method that runs when the service disconnected
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };
}
