package com.example.altaf.guardianapp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class implements Broadcast receiver, which receives PACKAGE_ADDED broadcast.
 * USE: It is used to update database of The Defender app with basic info of newly installed app.
 * It is also used to mark newly installed app as a SAFE or Dangerous app for device based on
 * global safe and dangerous databases.
 * */
public class BroadcastReceiver_NewInstalled extends android.content.BroadcastReceiver {

    private DBHelper DBHelper;
    Context context;

    //This function executes after receiving broadcast.
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context=context;
        DBHelper =new DBHelper(context);
        //Get newly installed apps package name using Intent and URI.
        Uri data=intent.getData();
        String pkgName=data.getEncodedSchemeSpecificPart();
        //Call function to add apps data into databases.
        addNewInstalledApp(pkgName);
    }

    //This function is used to retrieve apps bsic info and update Defender's database.
    //It is also used to mark app as a SAFE or DANGEROUS based on global databases.
    public void addNewInstalledApp(String PackageName)
    {
        String AppName="";
        int Uid=0;
        String ProcessName="";
        String Permissions="";
        //Get reference to system's Package Manager
        PackageManager pm=context.getPackageManager();
        DBHelper DBHelper =new DBHelper(context);

        List<ApplicationInfo> list=pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for(ApplicationInfo app:list)
        {
            if(app.packageName.equalsIgnoreCase(PackageName))
            {
                //Application name
                AppName=app.loadLabel(pm).toString();
                //Application UID
                Uid=app.uid;
                //Application process name
                ProcessName=app.processName;
                //Application permissions
                Permissions="";

                try {
                    PackageInfo pi=pm.getPackageInfo(app.packageName,PackageManager.GET_PERMISSIONS);
                    //Get application permission in a String array.
                    String[] permissions=pi.requestedPermissions;

                    if(permissions!=null){

                        for(int i=0;i<permissions.length;i++)
                            Permissions+=""+(i+1)+")"+permissions[i]+"\n";
                        //Add newly installed app details into the database.
                        DBHelper.InsertAppDetails(Uid, AppName, PackageName, ProcessName, Permissions);
                    }

                } catch (PackageManager.NameNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                //If app is in the Global Dangerous app list then mark it dangerous for device.
                HashMap<String,String> GlobalDangerousApps= DBHelper.GetGlobalDangerousApp();
                if(GlobalDangerousApps.size()!=0)
                {
                    for(Map.Entry<String,String> App:GlobalDangerousApps.entrySet() )
                    {
                        if(App.getKey().equalsIgnoreCase(PackageName))
                        {
                            //Add app in the dangerous app table.
                            DBHelper.addTempDangerousApp(Uid,AppName,PackageName,1," "," ");
                        }
                    }
                }
                //If app is in the Global Safe app list then mark it safe for device.
                HashMap<String,String> GlobalSafeApps= DBHelper.GetGlobalSafeApp();
                if(GlobalSafeApps.size()!=0)
                {
                    for(Map.Entry<String,String> App:GlobalSafeApps.entrySet() )
                    {
                        //Add app in the safe app table.
                        DBHelper.InsertSafeApp(Uid, PackageName );
                    }
                }
            }
        }
    }
}
