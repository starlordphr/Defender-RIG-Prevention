package com.example.altaf.guardianapp;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


/**********************************************************************************
 * This class is used to create a service which will start the app in foreground by showing
 * the notification with highest priority. This service gets called whenever device boots
 * or app starts for first time.
 * If service is already running then call to this service will be ignored.
 * Use: Protect DEFENDER app from getting killed by other third party malicious apps.
 * ********************************************************************************/

public class BackgroundService extends Service {



    // This method gets called whenever service is created for the first time.
    @Override
    public void onCreate() {
        super.onCreate();

        //MainActivity activity will be opened after clicking on the notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.defendericon)  // the status icon
                .setTicker("Protection Active")  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle("Protected")  // the label of the entry
                .setContentText("Protected")  // the contents of the entry
                .setContentIntent(contentIntent)// The intent to send when the entry is clicked
                .build();

        // Send the notification in foreground.
        startForeground(999,notification);





    }


    //Method will execute after creation of service i.e after onCreate()
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);

    }



    //Method will get called when stopService(Intent) is called. It will stop the service.
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //This method is used to Bind UI elements with the sevice.
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



}
