package com.example.altaf.guardianapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

/**
 * This class implements Broadcast receiver, which receives PHONE_STATE broadcast.
 * USE: On receiving the broadcast it starts listening to the current state of the incoming and outgoing calls
 * with the help of PhoneStateListener class.
 * */
public class BroadcastReceiver_CallReceiver extends BroadcastReceiver {

    //DbHelper class reference
    DBHelper DBHelper;
    //TelephonyManager class reference
    TelephonyManager telManager;
    //CallListner class reference
    CallListener callListener;

    //This function executes after receiving broadacast.
    @Override
    public void onReceive(Context context, Intent intent) {

        //Assigning TelephonyManager referene with the system service TELEPHONY_SERVICE
        telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        //Creating new CallListener class object and assigning it to CallListener reference
        callListener=new CallListener(context);
        //Start listing the call state.
        telManager.listen(callListener, PhoneStateListener.LISTEN_CALL_STATE);
    }
}

