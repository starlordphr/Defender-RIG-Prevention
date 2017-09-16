package com.example.altaf.guardianapp;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.Map;

/**
 * This class implements Broadcast receiver, which receives PROVIDERS_CHANGED broadcast.
 * USE: On receiving the broadcast it starts searching malicious applications which are
 * currently executing on the device and possibly accessing user's location maliciously, and prevents
 * such apps from accessing user's location.
 * */
public class BroadcastReceiver_GPSReceiver extends android.content.BroadcastReceiver{

    //Create new CPUUsage class object
    private CPUUsage cpuUsage=new CPUUsage();
    //Create DBhelper class reference
    private DBHelper DBHelper;
    //String representing possible threat from detected malicious app.
    private String THREAT="Application can collect GPS location.";
    //String representing dangerous permissions of detected malicious app.
    private String DANGEROUS_PERMISSIONS="1) ACCESS_FINE_LOCATION\n2) ACCESS_COARSE_LOCATION\n3) INTERNET";
    //Create reference of ActivityManager class.
    ActivityManager activityManager;
    //Create new Handler class object for repeatedly calling a thread.
    Handler handler=new Handler();
    //Create LocationManager class reference
    LocationManager locationManager;
    //Boolean flag to check is GPS is ON or OFF
    Boolean isGPSEnabled=false;
    //Activity Context reference
    Context mContext;

    //This function executes after receiving broadcast.
    @Override
    public void onReceive(Context context, Intent intent) {

        mContext=context;
        //Create new object of DBHelper class and assigning it to DBHelper reference.
        DBHelper = new DBHelper(context);
        //Accessing Location service.
        locationManager = (LocationManager) context
                .getSystemService(context.LOCATION_SERVICE);

        // Getting GPS status
        isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);

        //If GPS is ON, then find malicious apps and start a continuous thread for prevention.
       if(isGPSEnabled) {
           //Function for detecting malicious applications.
           findMaliciousApps();
           //Start continuous prevention thread.
           continousThread.run();
        }

    }
    //Create a new Runnable reference and assign work to it. i.e. preventMaliciousApps function.
    Runnable continousThread= new Runnable() {
        @Override
        public void run() {
            preventMaliciousApps(mContext);

        }
    };

    /******
     * This function is used to prevent malicious apps from accessing user's location.
     * It works for every 10 seconds. It stops the continous thread when GPS is turned off.
     * **********/
    public void preventMaliciousApps(final Context context)
    {
        //Get all dangerous applications package names in a array list.
        ArrayList<String> dangerApps = DBHelper.GetAllDangerousApps();
        if (dangerApps.size() != 0) {
            activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            //kill every dangerous app from array.
            for (String pckApp : dangerApps) {
                //Activity manager's function used to kill app using Package Name.
                activityManager.killBackgroundProcesses(pckApp);
            }
        }


            locationManager = (LocationManager) context
                    .getSystemService(context.LOCATION_SERVICE);

            // Get GPS location
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);


            //if GPS is still ON, then again call to Continous Thread after 10 secs.
            // else Stop continuous thread.
            if(isGPSEnabled) {
                //Call to Runnable continousThread after 10 seconds.
                handler.postDelayed(continousThread, 10);
            }
            else
            {
                //Stop Runnable continousThread.
                handler.removeCallbacks(continousThread);
            }
    }

    /***************
     * This function is used to check if application has any dangerous permission.
     * ****************/
    public boolean hasDangerousPermissions(int  uid)
    {
        String Location_One_Permissions="ACCESS_FINE_LOCATION";
        String Location_Two_Permissions="ACCESS_COARSE_LOCATION";
        String InternetPermission="INTERNET";
        //Get list of all permissions granted to the application.
        String permissions= DBHelper.GetPermissionsByUid(uid);
        //If granted permission list containg either of the dangerous permissions then return TRUE else FALSE.
        if((permissions.contains(Location_One_Permissions) || permissions.contains(Location_Two_Permissions)) && permissions.contains(InternetPermission))
            return true;
        else
            return false;
    }

    /***************
     * This function is used to detect malicious application using Heuristics Parameters.
     * **********************/
    public void findMaliciousApps()
    {
        //Get Map of app UID and CPU details of each running app using CPUUsage class's CalUsage function.
        Map<Integer, String>  map = cpuUsage.CalUsage();
        //For every app calculate Current CPU usage, Current scheduling rate and Previous scheduling rate.
        for (Map.Entry entry : map.entrySet()) {
            String usages = entry.getValue().toString();
            String[] array = usages.split(" ");
            double currCpuUsage = Double.parseDouble(array[0]);
            double currShedRate = Double.parseDouble(array[1]);
            Double prevCpuUsage = DBHelper.GetCpuUsage((Integer) entry.getKey());
            Double prevShedRate = DBHelper.GetShedRate((Integer) entry.getKey());
            //Update databse with new usage values.
            DBHelper.UpdateCpuUsageAndSched((Integer) entry.getKey(), currCpuUsage, currShedRate);
            //Calculate difference in current and previous scheduling rates.
            double diffShedRate = currShedRate - prevShedRate;
            //Kill application if it's CPU usage is greater than 0, difference in scheduling rate is greater than 50
            //and app is not marked as a Safe app.
            if (currCpuUsage > 0 && diffShedRate > 50 && hasDangerousPermissions((Integer) entry.getKey()) && !DBHelper.isSafe((Integer) entry.getKey()))
            {
                activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
                String packageName = DBHelper.GetPkgNameFromUid((Integer) entry.getKey());
                //Kill app
                activityManager.killBackgroundProcesses(packageName);

                Log.d("DetectedGPS",DBHelper.GetAppNameFromUid((Integer) entry.getKey())+" --> "+"SR:"+diffShedRate+" CPU:"+currCpuUsage);


                //Add app in the database and mark as a Dangerous App.
                DBHelper.addTempDangerousApp((Integer) entry.getKey(), DBHelper.GetAppNameFromUid((Integer) entry.getKey()), packageName,0,DANGEROUS_PERMISSIONS,THREAT);
            }
        }
    }
}
