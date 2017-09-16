package com.example.altaf.guardianapp;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*****************
 * This class collects app process details such as PID, CPU usage, Memory usage, State of each app running on the device.
 * This class is called by UI thread for every 10 SECONDS for updating UI with current process details of the system.
 * USE: This class provides current process details of each app to UI thread.
 * **********************/
public class getProcessDetails {
    //Create a static reference of object array of ProcessPojo class
    static  ArrayList<ProcessPojo> ProcessArray=null;
    //Create a reference of Context
    Context context=null;
    //Variable to keep track of uptime
    double uptime;
    //Create a reference of DBHelper class
    DBHelper DBHelper;
    //Create a HashMap reference to store process specific details
    HashMap<String,String> map=null;

    //Constructor
    public getProcessDetails(Context c)
    {
        this.context=c;
        //Create a new ProcessPojo object array
        ProcessArray=new ArrayList<>();
        //Create a new instance of DBHelper class
        DBHelper =new DBHelper(c);
        //Create an new instance of HashMap
        map=new HashMap<String,String>();
        //Call to the InstalledAppInfo function
        InstalledAppInfo();
        //Call to the getDetails function
        getDetails();
    }

    /**************
    *This function is used to collect new process details of the system by parsing /proc file system
    ***************/
    public void getDetails()
    {
        try {
            //Parse /proc/uptime file
            BufferedReader totalCPU = new BufferedReader(new FileReader("/proc/uptime"));
            String[] upTime = totalCPU.readLine().split(" ");
            uptime = Double.parseDouble(upTime[0]);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //For each folder in /proc directory
        File folder = new File("/proc");
        for (final File fileEntry : folder.listFiles()) {
            //Go inside directories only, skip other files
            if (fileEntry.isDirectory()){
                //Go inside directories with numeric names and numeric name should not be less than 1000(system directories)
                if(isNumeric(fileEntry.getName()) && Integer.parseInt(fileEntry.getName()) > 1000){
                    try {
                        //PID of the Application
                        String PID = fileEntry.getName();

                        //parse /proc/<pid>/stat file
                        BufferedReader procStatus = new BufferedReader(new FileReader("/proc/"+fileEntry.getName()+"/stat"));
                        String[] CPUdetails = procStatus.readLine().split(" ");
                        //App time spent in user mode
                        double utime = Double.parseDouble(CPUdetails[13]);
                        //App time spent in kernel mode
                        double stime = Double.parseDouble(CPUdetails[14]);
                        //App time spent waiting in user mode
                        double cutime = Double.parseDouble(CPUdetails[15]);
                        //App time spent waiting in kernel mode
                        double cstime = Double.parseDouble(CPUdetails[16]);
                        //App starttime
                        double starttime = Double.parseDouble(CPUdetails[21]);

                        double totalTime = utime+stime;
                        totalTime = totalTime+cutime+cstime;
                        //App uptime in seconds
                        double seconds = uptime-(starttime/100);
                        //CPU USAGE
                        double cpuUsage = 100 * ((totalTime/100)/seconds);
                        //Rounding off the CPU usage
                        double roundOffCpu=(double)Math.round(cpuUsage*100)/100;

                        String percentCPU="";
                        //Multithreading cpuusage>100
                        if(roundOffCpu>100)
                        {
                             percentCPU = String.valueOf(roundOffCpu)+"(Multithreading)";
                        }
                        else
                        {
                             percentCPU = String.valueOf(roundOffCpu);
                        }

                        //parse /proc/<pid>/status file
                        procStatus = new BufferedReader(new FileReader("/proc/"+fileEntry.getName()+"/status"));
                        //State of the Application
                        procStatus.readLine();
                        String ReadState = procStatus.readLine().trim();
                        String[] ArrayState = ReadState.split("\t");
                        String State = (ArrayState[1]).trim();

                        //RSS of Application
                        for(int i=1;i<14;i++)
                        {
                            procStatus.readLine();
                        }
                        String ReadRSS = procStatus.readLine().trim();
                        String[] ArrayRSS = ReadRSS.split("\t");
                        String RSS = (ArrayRSS[1]).trim();
                        if(RSS.contains("ff"))
                        {
                            continue;
                        }

                        //Package Name of Application
                        String PackageName;
                        procStatus = new BufferedReader(new FileReader("/proc/"+fileEntry.getName()+"/cmdline"));
                        if(((PackageName = procStatus.readLine()) != null) && !("".equals(PackageName)))
                        {
                            PackageName = PackageName.trim();
                        }

                        //Create a new ProcessPojo object
                        ProcessPojo obj=new ProcessPojo();
                        //Set PID of app
                        obj.setPID(PID);
                        //Get app name by calling GetAppName function
                        String appName=GetAppName(PackageName);
                        //Set app name
                        obj.setAPPNAME(appName);
                        //Set CPU usage of app
                        obj.setCPU(percentCPU);
                        //Set Memory usage of app
                        obj.setMEMORY(RSS);
                        //Set current state of the app process
                        obj.setSTATE(State);
                        //Set app icon
                        obj.setAPP_ICON(GetAppIcon(appName));

                        //Add object in ProcessPojo array
                        ProcessArray.add(obj);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /********
     * This function checks if directory name is Numeric or not.
     * ********/
    public static boolean isNumeric(String str)
    {
        try {
            int d = Integer.parseInt(str);
        }
        catch(NumberFormatException nfe) {
            return false;
        }
        return true;
    }
   /*********
    * This function is used to get app name from package name
    * ***********/
    public String GetAppName(String packageName)
    {
        String appName ="N/A";
        if((packageName != null) && !("".equals(packageName)))
        {
            appName=map.get(packageName);
        }
        return appName;
    }

    /********
     * This function is used to get a map of installed apps' PACKAGE NAME and APP NAME
     * **********/
    public void InstalledAppInfo()
    {
        PackageManager packageManager=context.getPackageManager();
        List<PackageInfo> apps=packageManager.getInstalledPackages(0);
        for(PackageInfo info:apps)
        {
            map.put(info.packageName,info.applicationInfo.loadLabel(context.getPackageManager()).toString());
        }


    }
    //This function is used to get app ICON from app name
    public Drawable GetAppIcon(String appName)
    {
        Drawable icon=null;

        String pkgName= DBHelper.GetPkgNameFromAppName(appName);

        try {
            icon=context.getPackageManager().getApplicationIcon(pkgName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return icon;
    }
}
