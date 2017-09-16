package com.example.altaf.guardianapp;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/************
 * This class does a background task of storing log details of the running apps on SD card.
 * *******************************/
public class SaveLog extends AsyncTask {
    //Create Context reference
    Context context;
    //Create DBHelper reference
    DBHelper dbHelper;

    //Constructor
    public SaveLog(Context c) {
        this.context=c;
        dbHelper=new DBHelper(c);
    }
    /******************
     *This function runs in background and stores log
     ***************************/
    @Override
    protected Object doInBackground(Object[] objects) {

        //Create directory reference
        File directory=null;
        //Create a new object of getProcessDetails class and access its static ProcessArray.
        ArrayList<ProcessPojo> processArray=new getProcessDetails(context).ProcessArray;
        //Create a new object of getNetworkDetails class and access its static NetworkArray.
        ArrayList<NetworkPojo> networkArray=new getNetworkDetails(context).NetworkArray;

        //Create a new folder with name 'Defender App' on SD card
        directory = new File(Environment.getExternalStorageDirectory(), "/Defender App");
        //If folder exists
        if(directory.isDirectory())
        {
            //Call SaveLogFile function to write log data into a file in 'Defender App' directory
            SaveLogFile(directory,processArray,networkArray);
        }
        //If folder does not exist
        else
        {
            //Create folder
            directory.mkdir();
            //Call SaveLogFile function to write log data into a file in 'Defender App' directory
            SaveLogFile(directory,processArray,networkArray);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
    }


    /***********************
     * This function is used to write log text into a file on SD card.
     */
    public void SaveLogFile(File directory,ArrayList<ProcessPojo> Data,ArrayList<NetworkPojo> Data2)
    {
        try {

            //Obtain Calendar instance
            Calendar calendar=Calendar.getInstance();
            //Get hours, minutes and seconds of current time
            int hours=calendar.get(Calendar.HOUR_OF_DAY);
            int minutes=calendar.get(Calendar.MINUTE);
            int seconds=calendar.get(Calendar.SECOND);

            String logTime="[LOGTIME"+hours+":"+minutes+":"+seconds+" ]";
            String date= new SimpleDateFormat("ddMMMyyyy").format(new Date());

            //Create unique file name
            File logFile=new File(directory,""+date+calendar.getTimeInMillis());
            FileOutputStream fout=new FileOutputStream(logFile);

            String logText=logTime+"\nPid\tCPU Usage\tMemory Usage\tState\tApp Name\n\n";
            //Access data from ProcessArray.
            for(ProcessPojo process:Data)
            {
                logText+= process.getPID()+"\t"+
                        process.getCPU()+"\t"+
                        process.getMEMORY()+"\t"+
                        process.getSTATE()+"\t"+
                        process.getAPPNAME()+"\n";

            }


            logText+="\n[NETWORK DATA]\nApp Name\tUID\tTx_Bytes\tRx_Bytes\tConnections";
            //Access data from NetworkArray.
            for(NetworkPojo networkPojo:Data2)
            {
                logText+= networkPojo.getAPP_NAME()+"\t"+
                        networkPojo.getUID()+"\t"+
                        networkPojo.getTX_BYTES()+"\t"+
                        networkPojo.getRX_BYTES()+"\nConnections:\n"+
                        dbHelper.GetConnectionsFromUid(Integer.parseInt(networkPojo.getUID()))+"\n" +
                        "**************************************************************\n";
            }


            byte[] buf=logText.getBytes();
            //Write data to file
            fout.write(buf);
            fout.close();
        } catch (IOException e) {
            Log.d("LOG", "unable to save log " + e.getMessage());
    }

    }
}
