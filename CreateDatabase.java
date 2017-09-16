package com.example.altaf.guardianapp;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

/*****************************
 * This class implements AsyncTask which runs on a separate thread than UI thread.
 * This class executes only one time when Defender application first time opens.
 * USE: This class is used to create DATABASES and add basic information such as
 * PERMISSIONS, APP NAME, PACKAGE NAME in database.
 * ******************************/
public class CreateDatabase extends AsyncTask {
    //GetPermissions class reference
    GetPermissions getPermissions;
    //Context reference
    Context context;
    //ProgressBar UI element reference
    ProgressBar circularWait;
    //Constructor
    public  CreateDatabase(Context c, ProgressBar p)
    {
        this.context=c;
        this.circularWait=p;
    }

    //This functions runs in background the calls GetPermissions class.
    @Override
    protected Object doInBackground(Object[] objects) {

        getPermissions=new GetPermissions(context);

        return null;
    }

    //This function is used to hide progress bar and start Main Activity after Creating and updating databases.
    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        //Hide progress bar
        circularWait.setVisibility(View.GONE);
        //Create Intent for Main Activity
        Intent mainActivity=new Intent(context,MainActivity.class);
        mainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //Start Main Activity
        context.startActivity(mainActivity);
    }
}
