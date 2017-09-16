package com.example.altaf.guardianapp;

import android.content.Context;

import java.util.ArrayList;

/**
 * This class collects details of killed background apps.
 * USE: This class provides result for prevention process to UI thread.
 */
public class getResults {
    //Create a reference of Context
    Context context;
    //Create a static ResultPojo object array
    static ArrayList<ResultPojo> resultPojos=new ArrayList<>();
    //Create a reference of DBHelepr class
    DBHelper dbHelper;

   //Constructor
    public getResults(Context c)
    {
        context=c;
        dbHelper=new DBHelper(c);
        getKilledApps();
    }
    //This function is used to fetch results from database.
    public void getKilledApps()
    {
     resultPojos=dbHelper.GetResults();
    }
}
