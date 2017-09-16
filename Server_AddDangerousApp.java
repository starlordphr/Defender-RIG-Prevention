package com.example.altaf.guardianapp;

import android.content.Context;
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
 * This class creates a separate thread for network communication to send Malicious apps
 * detected to the remote server.
 * ***************************/
public class Server_AddDangerousApp extends AsyncTask {

    String app_name="";
    String pkg_name="";
    Context context;

    Server_AddDangerousApp(Context c,String app_name,String pkg_name)
    {
        this.context=c;
        this.app_name=app_name;
        this.pkg_name=pkg_name;
    }

    //Server script URL
    String SERVER_URL="http://www.guardianapp.16mb.com/InsertDangerous.php";


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

            //Pass the parameters for POST method
            String FORM_VALUES="app_name="+app_name+"&pkg_name="+pkg_name;
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


           //If response is not null
           if (o != null) {
               String response = (String) o;
               Log.d("aaa",response);
               //If response is 'ERROR'
               if(response.equals("ERROR"))
               {
                   //Show error message
                   Toast.makeText(context,"Unable to send dangerous apps data",Toast.LENGTH_LONG).show();
               }
               else {
                   //Show success message
                   Toast.makeText(context, "Dangerous apps data sent.", Toast.LENGTH_LONG).show();
                   String[] array = response.split("\n");
                   //Response code 0=FAILURE 1=SUCCESS;
                   String responseCode = array[4].trim();
                   //Obtain DBHelper instance
                   DBHelper DBHelper = new DBHelper(context);
                   //Mark apps as UPDATED in database
                   DBHelper.UpdateTempDangerousApp(pkg_name, Integer.parseInt(responseCode + ""));
               }
               }
       }
}
