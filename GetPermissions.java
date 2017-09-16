package com.example.altaf.guardianapp;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.List;

/*****************
 * This class is called one time only when app starts first time.
 * USE: It is used to fetch basic information and permissions to each app and update the database.
 * *****************/
public class GetPermissions{

    //Create DBHelper referencce
    DBHelper DbHelper=null;

    //Constructor
    public GetPermissions(Context c)
    {
        //Create a new instance of DBHelper class
        DbHelper=new DBHelper(c);

        String AppName="";
        int Uid=0;
        String PackageName="";
        String ProcessName="";
        String Permissions="";

        //Obtain an instance of Package Manager class
        PackageManager pm=c.getPackageManager();
        //Get list of all installed app
        List<ApplicationInfo> list=pm.getInstalledApplications(PackageManager.GET_META_DATA);
        //Iterate through app list
        for(ApplicationInfo app:list)
        {
            //Get app name
            AppName=app.loadLabel(pm).toString();
            //Get app uid
            Uid=app.uid;
            //Get package name
            PackageName=app.packageName;
            //Get process name
            ProcessName=app.processName;
            Permissions="";

            try {
                PackageInfo packageInfo=pm.getPackageInfo(app.packageName,PackageManager.GET_PERMISSIONS);
                //Get app permissions
                String[] permissions=packageInfo.requestedPermissions;

                if(permissions!=null){

                    for(int i=0;i<permissions.length;i++)
                        Permissions+=""+(i+1)+")"+permissions[i]+"\n";
                    //Insert app details in the database
                    DbHelper.InsertAppDetails(Uid,AppName,PackageName,ProcessName,Permissions);
                }
                //If app is a syatem app thea make its entry in the safe applications
                if((app.flags & ApplicationInfo.FLAG_SYSTEM)!=0){
                   DbHelper.InsertSafeApp(app.uid,app.packageName);
                }

            } catch (PackageManager.NameNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
