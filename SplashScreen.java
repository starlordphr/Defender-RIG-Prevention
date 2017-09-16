package com.example.altaf.guardianapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;

/******************
 * This Activity class is used to show flash screen when DEFENDER is run for the first time.
 * USE: Databases are created in background while splash screen is visible.
 * *********************/
public class SplashScreen extends Activity {

    //Create a progress bar reference
    ProgressBar circularWait;
    //SharedPreference file name
    static final String sharedPrefFile="MyPref";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Full screen activity
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        //Assign reference to Progress bar UI element
        circularWait=(ProgressBar)findViewById(R.id.progressBar);
        //Start showing circular progress bar
        circularWait.setVisibility(View.VISIBLE);

        //Check is database is created or not
        if(!isDatabaseUpdated())
        {
            SplashScreen.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //Call background thread to create database
                    new CreateDatabase(getApplication(), circularWait).execute();
                }
            });

        }
        //If databases are already created
        else
        {
            //Stop showing circular progress bar
            circularWait.setVisibility(View.GONE);
            //Call openMainActivity function to start MAIN ACTIVITY
            openMainActivity();
            //Close splash screen activity
            finish();
        }



    }
   //This function is used to start MAIN ACTIVITY
    public void openMainActivity()
    {
        //Create intent for MAIN ACTIVITY
        Intent i=new Intent(SplashScreen.this,MainActivity.class);
        //Start MAIN ACTIVITY
        startActivity(i);
        //Close splash screen activity
        finish();
    }

    // this function is used to check if database is created or not using shared preferences
    public boolean isDatabaseUpdated()
    {
        SharedPreferences sharedPreferences=getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE);
        String isUpdated=sharedPreferences.getString("isUpdated","false");

        if(isUpdated.equals("false"))
        {
            SharedPreferences.Editor editor=sharedPreferences.edit();
            editor.putString("isUpdated","true");
            editor.commit();
            return false;
        }
        else
        {
            return true;
        }
    }
}

