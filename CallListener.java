package com.example.altaf.guardianapp;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Map;

/**
 * This class implements Phone State Listener.
 * USE: It is used to monitor current state of incoming and outgoing calls. If any malicious application
 * is recording calls, then such apps are killed by this class.
 * */
public class CallListener extends PhoneStateListener {

    //Create new CPUUsage class object
    private CPUUsage cpuUsage=new CPUUsage();
    //Create DBhelper class reference
    private DBHelper DBHelper;
    //String representing possible threat from detected malicious app.
    private String THREAT="Application can record incoming and outgoing voice calls.";
    //String representing dangerous permissions of detected malicious app.
    private String DANGEROUS_PERMISSIONS="1) RECORD_AUDIO\n2) READ_PHONE_STATE";
    //Create reference of ActivityManager class.
    ActivityManager am;
    //Create new Handler class object for repeatedly calling a thread.
    Handler handler=new Handler();
    //Activity Context reference
    Context context;
    //Constructor
    public CallListener(Context c) {
        this.context = c;
        //Create new object of DBHelper class and assigning it to DBHelper reference.
        DBHelper = new DBHelper(c);
    }

    //This function executes whenever phones call state changes. .
    @Override
    public void onCallStateChanged(int state, String incomingNumber) {

        switch (state) {
            //Phone is ringing
            case TelephonyManager.CALL_STATE_RINGING:
                break;
            //User received a incoming call or dialed a outgoing call
            case TelephonyManager.CALL_STATE_OFFHOOK:
                //Function for detecting malicious applications.
                findMaliciousApps();
                //Function for preventing malicious applications from recording calls.
                preventMaliciousApps(context);
                break;
            //Call Disconnected.
            case TelephonyManager.CALL_STATE_IDLE:
                //Stop continuous runnig prevention thread.
                handler.removeCallbacks(run);
                break;
        }
    }
    //Create a new Runnable reference and assign work to it. i.e. preventMaliciousApps function.
    Runnable run=  new Runnable() {
        @Override
        public void run() {
            preventMaliciousApps(context);
        }
    };

    /******
     * This function is used to prevent malicious apps from recording calls.
     * It works for every 2 seconds.
     * **********/
    public void preventMaliciousApps(Context context)
    {
        //Get all dangerous applications package names in a array list.
        ArrayList<String> dangerApps = DBHelper.GetAllDangerousApps();
        if (dangerApps.size() != 0) {
            am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            //kill every dangerous app from array.
            for (String pckApp : dangerApps) {
                //Activity manager's function used to kill app using Package Name.
                am.killBackgroundProcesses(pckApp);
            }
        }
        //Call to Runnable run after 2 seconds.
        handler.postDelayed(run,2*1000);
    }

    /***************
     * This function is used to check if application has any dangerous permission.
     * ****************/
    public boolean hasDangerousPermissions(int  uid)
    {
        String DangerousPermissions="RECORD_AUDIO";
        String phoneStatePermission="READ_PHONE_STATE";
        //Get list of all permissions granted to the application.
        String permissions= DBHelper.GetPermissionsByUid(uid);
        //If granted permission list containg either of the dangerous permissions then return TRUE else FALSE.
        if(permissions.contains(DangerousPermissions) || permissions.contains(phoneStatePermission) )
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
            if (currCpuUsage > 0 && diffShedRate > 50 && hasDangerousPermissions((Integer) entry.getKey()) && !DBHelper.isSafe((Integer) entry.getKey()))    //means process is using CPU
            {
                am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                String packageName = DBHelper.GetPkgNameFromUid((Integer) entry.getKey());
                //Kill app
                am.killBackgroundProcesses(packageName);

                Log.d("DetectedCall",DBHelper.GetAppNameFromUid((Integer) entry.getKey())+" --> "+"SR:"+diffShedRate+" CPU:"+currCpuUsage);

                //Add app in the database and mark as a Dangerous App.
                DBHelper.addTempDangerousApp((Integer) entry.getKey(), DBHelper.GetAppNameFromUid((Integer) entry.getKey()), packageName,0,DANGEROUS_PERMISSIONS,THREAT);
            }
        }
    }
}