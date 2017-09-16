package com.example.altaf.guardianapp;

import android.content.Context;
import android.content.Intent;

/**
 * This class implements  Broadcast receiver, which receives BOOT_COMPLETE broadcast and
 * custom START_SERVICE broadcast.
 * USE: On receiving broadcasts it starts the app in foreground by calling a Service.
 * */

public class BroadcastReceiver extends android.content.BroadcastReceiver {

    //Context reference
    Context context;

    //This function executes after receiving broadacast.
    @Override
    public void onReceive(final Context context, Intent intent) {

            this.context=context;
            //Creating intent of Service Class BackgroundService.class
            Intent i = new Intent(context, BackgroundService.class);
            //Start the service
            context.startService(i);
    }
}
