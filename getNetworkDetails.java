package com.example.altaf.guardianapp;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.TrafficStats;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


/*****************
 * This class collects network details such as Tx bytes, Rx bytes, URL connections of each app installed on the device.
 * This class is called by UI thread for every 30 SECONDS for updating UI with current network details of the system.
 * USE: This class provides current network details of each app to UI thread.
 * **********************/
public class getNetworkDetails {
    //Create a static reference of object array of NetworkPojo class
    static ArrayList<NetworkPojo> NetworkArray=null;
    //Create a reference of DBHelper class
    DBHelper DBHelper =null;
    //Create a reference of Context
    Context context;

    //Constructor
    public getNetworkDetails(Context c) {
        this.context=c;
        //Create a new ArrayList
        NetworkArray=new ArrayList<>();
        //Create a new instance of DBHelper class
        DBHelper =new DBHelper(c);
        //Call getNetworkDetails function for collecting network snapshot of the device
        getNetworkDetails();
    }

    /**************
     *This function is used to collect new network details of the system
     * **************/
    public void getNetworkDetails()
    {
        //Create a new thread
        new Runnable() {
            @Override
            public void run() {

                //Call getIpMapping function to get URL connections created of apps
                getIpMapping();
                //Obtain an instance of Package Manager class
                PackageManager packageManager=context.getPackageManager();
                //Iterate through each application installed on the device
                for(ApplicationInfo app:packageManager.getInstalledApplications(0)) {
                    //App UID
                    String uid = app.uid + "";
                    //Number of bytes transmitted by an app specified by UID
                    String tx_bytes = TrafficStats.getUidTxBytes(app.uid) + "";
                    //Number of bytes received by an app specified by UID
                    String rx_bytes = TrafficStats.getUidRxBytes(app.uid) + "";
                    //App name
                    String appName = "" + app.loadLabel(packageManager);
                    //Create new object of NetworkPojo class
                    NetworkPojo object = new NetworkPojo();
                    //Set app UID
                    object.setUID(uid);
                    //Set app name
                    object.setAPP_NAME(appName);
                    //Set transmitted bytes
                    object.setTX_BYTES(tx_bytes);
                    //Set received bytes
                    object.setRX_BYTES(rx_bytes);
                    //Set app icon
                    object.setAPP_ICON(GetAppIcon(appName));
                    //Add object in array
                    NetworkArray.add(object);
                }
            }
        }.run();
    }

    /************
     * This function is used to get details about URL connection made by each app.
     * It uses /proc/net/upd, /proc/net/upd6, /proc/net/tcp, /proc/net/tcp6 files to get
     * network connections details.
     * ***************/
    void getIpMapping() throws NumberFormatException
    {
        try {
            //Parse /proc/net/upd file for UDP IPv4 connections
            BufferedReader info3 = new BufferedReader(new FileReader("/proc/net/udp"));
            info3.readLine();

            String details3 = null;
            while ((details3 = info3.readLine()) != null)
            {
                String[] array=details3.split(" ");
                String result=array[4] + "-" + array[5] + "-" + array[10];
                //String is valid only if its length is greater than 31
                if(result.length()>31) {
                    int uid;
                    try {
                     uid=Integer.parseInt(array[10]);
                    }
                    catch (Exception e)
                    {
                        uid=-1;
                    }
                    //Update database
                    DBHelper.InsertUrl(uid, convertHEXtoIPv6(array[4]), convertHEXtoIPv6(array[5]),"UDP6");
                }
            }
            info3.close();

            //Parse /proc/net/upd6 file for UDP IPv4 connections
            BufferedReader info4 = new BufferedReader(new FileReader("/proc/net/udp6"));
            info4.readLine();

            String details4 = null;
            while ((details4 = info4.readLine()) != null)
            {
                String[] array=details4.split(" ");
                String result=array[4] + "-" + array[5] + "-" + array[10];
                //String is valid only if its length is greater than 80
                if(result.length()>80) {
                    //Update database
                    DBHelper.InsertUrl(Integer.parseInt(array[10]), convertHEXtoIPv4(array[4]), convertHEXtoIPv4(array[5]),"UDP4");
                }
            }
            info4.close();

            //Parse /proc/net/tcp file for TCP IPv6 connections
            BufferedReader info = new BufferedReader(new FileReader("/proc/net/tcp6"));
            info.readLine();

            String details = null;
            while ((details = info.readLine()) != null)
            {
                String[] array=details.split(" ");
                String result=array[4] + "-" + array[5] + "-" + array[10];
                //String is valid only if its length is greater than 80
                if(result.length()>80) {
                    //Update database
                    DBHelper.InsertUrl(Integer.parseInt(array[10]), convertHEXtoIPv6(array[4]), convertHEXtoIPv6(array[5]),"TCP6");
                }
            }
            info.close();
            //Parse /proc/net/tcp6 file for TCP IPv4 connections
            BufferedReader info2 = new BufferedReader(new FileReader("/proc/net/tcp"));
            info2.readLine();

            String details2 = null;
            while ((details2 = info2.readLine()) != null)
            {
                String[] array=details2.split(" ");
                String result=array[4] + "-" + array[5] + "-" + array[10];
                //String is valid only if its length is greater than 31
                if(result.length()>31){
                    //Update database
                    DBHelper.InsertUrl(Integer.parseInt(array[10]), convertHEXtoIPv4(array[4]), convertHEXtoIPv4(array[5]),"TCP4");
                }
            }
            info2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //This function is use to convert HEX IP (IPv4) address value to DECIMAL.
    String getDecimalFromHex(String hexValue)
    {
        String ip="";
        for(int i = 0; i < hexValue.length(); i = i + 2) {
            ip = ip + Integer.valueOf(hexValue.substring(i, i + 2), 16) + ".";
        }
        return ip;
    }

    //This function is used to get app icon from app name
    public Drawable GetAppIcon(String appName)
    {
        Drawable icon=null;
        //Retrieve app package name using app name from database
        String pkgName= DBHelper.GetPkgNameFromAppName(appName);
        try {
            icon=context.getPackageManager().getApplicationIcon(pkgName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();

        }
        return icon;
    }

    //This function is use to convert HEX IP address string to IPv6 format.
    String convertHEXtoIPv6(String ip)
    {
        String result="";
        //IP address string
        String ipAddr=ip.substring(0,ip.indexOf(":"));
        //Port number string
        String ipPort=ip.substring(ip.indexOf(":")+1,ip.length());
        try {
            //Convert Hex to decimal
            ipPort = Integer.parseInt(ipPort, 16) + "";
        }
        catch (Exception e)
        {e.printStackTrace();}

        String newIpAddr="";
        int n = ipAddr.length()/4;
        //Insert ":" after 4 characters
        for(int i = 0, x=0, y=4; i<n; i++){
            newIpAddr += ipAddr.substring(x,y);
            if(i+1!=n)
            {
                newIpAddr+=":";
            }
            x += 4;
            y += 4;

        }

        result=newIpAddr+":"+ipPort;
        return result;
    }
    //This function is use to convert HEX IP address string to IPv4 format.
    String convertHEXtoIPv4(String ip)
    {
        String result="";
        //IP address
        String ipAddr = ip.substring(0,ip.indexOf(":"));
        //Convert IP address in HEX to DECIMAL
        ipAddr =getDecimalFromHex(ipAddr);
        //Port number
        String ipPort=ip.substring(ip.indexOf(":")+1,ip.length());
        //Convert port number Hex to Decimal
        ipPort=Integer.parseInt(ipPort, 16)+"";

        result=ipAddr+":"+ipPort;
        return result;
    }
}
