package com.example.altaf.guardianapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/******************
 * This class creates a separate thread for network communication to fetch Malicious apps
 * from the remote server.
 * ***************************/
public class Server_FetchDangerousApp extends AsyncTask {

    Context context;
    Server_FetchDangerousApp(Context c)
    {
        this.context=c;
    }
    //Server script URL
    String SERVER_URL="http://www.guardianapp.16mb.com/RetrieveDangerous.php";

    DBHelper DBHelper;


    @Override
    protected Object doInBackground(Object[] objects) {
        String response=null;

        try {

            URL url=new URL(SERVER_URL);
            //make HttpURLConnection with the URL
            HttpURLConnection connection=(HttpURLConnection)url.openConnection();
            //20 seconds read timeout
            connection.setReadTimeout(20*1000);
            //25 seconds connection timeout
            connection.setConnectTimeout(25 * 1000);
            //HTTP POST method
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            //Write request in output stream
            OutputStream os=new BufferedOutputStream(connection.getOutputStream());
            //Pass the parameter for POST method
            String FORM_VALUES="timestamp="+getTimeStamp();
            //Write parameters to output stream
            os.write(FORM_VALUES.getBytes());
            //Send request
            os.flush();


            StringBuffer buffer = new StringBuffer();
            //Receive response in input stream
            InputStream inputStream = connection.getInputStream();
            //Get server response
            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line=null;
            //Read response line by line
            while((line=bufferedReader.readLine())!=null)
            {
                buffer.append(line+"\n");
            }

            inputStream.close();
            //Disconnect the connection
            connection.disconnect();
            response=buffer.toString();


        } catch (MalformedURLException e) {
            response="ERROR";
        } catch (IOException e) {
            response="ERROR";
        }

        return response ;
    }

    @Override
    protected void onPostExecute(Object o) {

        super.onPostExecute(o);

        String response=(String)o;

        //If response is 'ERROR'
        if(response.equals("ERROR"))
        {
            //Show error message
            Toast.makeText(context,"Unable to fetch dangerous apps data",Toast.LENGTH_LONG).show();
        }
        else {
            String[] array = response.split("\n");
            //Represents number of fetched entries
            int lines = Integer.parseInt(array[4]);
            //If fetched entries are not 0
            if (lines != 0) {
                //For each fetched entry parse string to get app name and package name
                for (int i = 1; i <= lines; i++) {
                    String appPkg = array[4 + i];
                    String[] separated = appPkg.split(":");

                    //App name
                    String pkgName = separated[1];
                    //Package name
                    String appName = separated[0];

                    DBHelper = new DBHelper(context);
                    //Add apps in database
                    DBHelper.AddGlobalDangerousApp(pkgName, appName);

                }
                //Get TimeStamp from the response
                long timestamp = Long.parseLong(array[4 + lines + 1]);
                //Update timestamp
                updateTimeStamp(timestamp + "");
            }
            //Show success message
            Toast.makeText(context, "Dangerous apps data fetched.", Toast.LENGTH_LONG).show();
        }

    }

    //This function used to get timestamp from shared preferences
    public String getTimeStamp()
    {
        SharedPreferences sharedPreferences=context.getSharedPreferences("Guardian",Context.MODE_PRIVATE);
        return sharedPreferences.getString("dangerous_timestamp","-1");
    }
    //This function is used to update timestamp
    public void updateTimeStamp(String new_timestamp)
    {
        SharedPreferences sharedPreferences=context.getSharedPreferences("Guardian",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString("dangerous_timestamp",new_timestamp);
        editor.commit();
    }
}
