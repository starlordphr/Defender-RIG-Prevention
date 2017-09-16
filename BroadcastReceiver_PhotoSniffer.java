package com.example.altaf.guardianapp;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.Map;

/**
 * This class implements Broadcast receiver, which receives NEW_PICTURE broadcast.
 * USE: On receiving the broadcast it starts searching malicious applications which are
 * currently executing on the device and possibly accessing user's photos, and prevents
 * such apps from accessing user's photos.
 * */
public class BroadcastReceiver_PhotoSniffer extends android.content.BroadcastReceiver {

    //Create new CPUUsage class object
    private CPUUsage cpuUsage=new CPUUsage();
    //Create DBhelper class reference
    private DBHelper DBHelper;
    //String representing possible threat from detected malicious app.
    private String THREAT="Application can collect photos captured using device camera.";
    //String representing dangerous permissions of detected malicious app.
    private String DANGEROUS_PERMISSIONS=" 1) READ_EXTERNAL_STORAGE\n2) INTERNET";
    //Create reference of ActivityManager class.
    ActivityManager activityManager;
    //Activity Context reference
    Context context;

    //This function executes after receiving broadcast.
    @Override
    public void onReceive(Context context, Intent intent) {

        final Context mContext=context;
        this.context=context;
        //Create new object of DBHelper class and assigning it to DBHelper reference.
        DBHelper = new DBHelper(context);
        //Create a new Runnable reference and assign work to it. i.e. preventMaliciousApps function.
        new Runnable() {
            @Override
            public void run() {
                for(int i=0;i<50;i++) {
                    preventMaliciousApps(mContext);
                }
            }
        }.run();
    }

    /******
     * This function is used to prevent malicious apps from accessing user's photos.
     * It works iteratively for 50 times.
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
        //Get Map of app UID and CPU details of each running app using CPUUsage class's CalUsage function.
        Map<Integer, String> map = cpuUsage.CalUsage();
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
            //Kill application if it's CPU usage is greater than 0, difference in scheduling rate is greater than 20
            //and app is not marked as a Safe app.
            if (currCpuUsage > 0 && diffShedRate > 20 && hasDangerousPermissions((Integer) entry.getKey()) && !DBHelper.isSafe((Integer) entry.getKey()))    //means process is using CPU
            {
                activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                String packageName = DBHelper.GetPkgNameFromUid((Integer) entry.getKey());
                //Kill app
                activityManager.killBackgroundProcesses(packageName);

                Log.d("DetectedPhoto",DBHelper.GetAppNameFromUid((Integer) entry.getKey())+" --> "+"SR:"+diffShedRate+" CPU:"+currCpuUsage);

                //Add app in the database and mark as a Dangerous App.
                DBHelper.addTempDangerousApp((Integer) entry.getKey(), DBHelper.GetAppNameFromUid((Integer) entry.getKey()), packageName,0,DANGEROUS_PERMISSIONS,THREAT);
            }
        }
    }
    /***************
     * This function is used to check if application has any dangerous permission.
     * ****************/
    public boolean hasDangerousPermissions(int  uid)
    {
        String SdCardPermissions="READ_EXTERNAL_STORAGE";
        String InternetPermission="INTERNET";
        //Get list of all permissions granted to the application.
        String permissions= DBHelper.GetPermissionsByUid(uid);
        //If granted permission list containg either of the dangerous permissions then return TRUE else FALSE.
        if(permissions.contains(SdCardPermissions) && permissions.contains(InternetPermission))
            return true;
        else
            return false;
    }
}
