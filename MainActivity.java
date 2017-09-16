package com.example.altaf.guardianapp;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;

import java.util.ArrayList;
import java.util.Map;


/*************
 * This is the main UI activity class. It starts the main activity which holds three fragments
 * for Basic App Info, Network Info and Result. This is a tabbed activity with 3 tabs.
 * USE: This class is used for creating main UI activity and provide response to user interactions.
 * ********************/
public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    //Create AlertDialouge reference
    private AlertDialog alertDialog;
    //Create DBHelper class reference
    private DBHelper DBHelper;
    //Create ActivityManager class reference
    ActivityManager activityManager;
    String DialougeAppName="";
    String pkgName;

    //Create SectionPagerAdapter class reference
    private SectionsPagerAdapter mSectionsPagerAdapter;
    //Create ViewPager class reference
    private ViewPager mViewPager;
    //Create FloatingActionButton class reference
    private FloatingActionButton MainActionButton;
    //tag associated with the FAB menu button that saves log
    private static final String TAG_SAVELOG = "savelog";
    //tag associated with the FAB menu button that kills app
    private static final String TAG_KILLAPP = "killapp";
    //Create FloatingActionMenu class reference
    private FloatingActionMenu SubMenu;

    //Create a static Toolbar reference
    static Toolbar toolbar;
    //Create a static ActionBar reference
    static android.support.v7.app.ActionBar actionBar;


    /**************
     * This function executes when app starts.
     * **************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar=getSupportActionBar();
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        //Call function SetupTimpestampForServer to set up timestamp for syncing data with server
        SetupTimestampForServer();
        //Call function setupFAB to setup Floating Action Button
        setupFAB();

        //Create a CUSTOME BROADCAST intent
        Intent i=new Intent("com.example.altaf.guardianapp.STARTSERVICE");
        //Send CUSTOM BROADCAST
        sendBroadcast(i);
    }

    /***********
     * This function gets called when app is closed.
     * ***********/
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Create an intent for a BackgroundService class which implements service
        Intent intent=new Intent(this,BackgroundService.class);
        //Start service
        startService(intent);
    }


    /************
     * This function creates option menus.
     * **********/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    /******************
     * This function handles interaction on Option Menu.
     * ********************/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.search)
        {
            //Show application search dialog
            showSearchDialouge();
        }
        if(id==R.id.server_danger)
        {
            //if network is available
            if(isWorkingConnection()) {
                //Sync data with the remote server
                SyncServerData();
            }
            else {
                //Show error message
                Toast.makeText(getApplicationContext(),"Not connected to Internet!",Toast.LENGTH_LONG).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }


    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment f=null;
            switch (position)
            {
                //If tab is tab0 then call for BasicInfoList class
                case 0:
                    f=new BasicInfoList();
                    break;
                //If tab is tab1 then call for NetworkInfo class
                case 1:
                    f=new NetworkInfo();
                    break;
                //If tab is tab2 then call for ResultList class
                case 2:
                    f=new ResultsList();
                    break;
            }
            return f;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        //Assign Titles to each tab.
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Application Info";
                case 1:
                    return "Network Info";
                case 2:
                    return "Killed Applications";

            }
            return null;
        }
    }

    /*********************
     * This function is used to show dialog for killing app.
     * *****************************/
    public void showKillDialouge(final Context context)
    {
        //Obtain AlertDialog.Builder instance
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        //Obatin LayoutInflator instance
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        //Inflate custom layout for alert dialog
        View view = layoutInflater.inflate(R.layout.kill_app_layout,null);
        //Set view as view for builder
        builder.setView(view);

        //Get reference for EditText UI element on alert dialog
        final EditText killAppName=(EditText)view.findViewById(R.id.killApp);
        //Get reference for cancel button UI element on alert dialog
        Button cancelButton=(Button)view.findViewById(R.id.cancelButton);
        //Get reference for kill button UI element on alert dialog
        Button killButton=(Button)view.findViewById(R.id.killButton);

        //Handle cancle button click
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //close dialog
                alertDialog.dismiss();
            }
        });
        //Handle kill button click
        killButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //if entered app name is not blank
                if(!killAppName.getText().toString().equals(""))
                {
                    try{
                        //Obtain DBHelper class object
                        DBHelper = new DBHelper(context);
                        //Get package name of app from application name
                        pkgName = DBHelper.GetPkgNameFromAppName(killAppName.getText().toString());
                        //If package name not found in the database
                        if(pkgName.equals("N/A"))
                        {
                                //Show error message
                                Toast.makeText(getApplicationContext(),"Application not found.", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            //Obatin Activity Manager class instance
                            activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                            //Kill the app
                            activityManager.killBackgroundProcesses(pkgName);
                            //Show success message
                            Toast.makeText(context, "Application Killed! : " + killAppName.getText().toString(), Toast.LENGTH_LONG).show();
                        }
                        //Close alet dialog
                        alertDialog.dismiss();
                    }catch (Exception e)
                    {
                        //If any unexpected error occurs then close diallog
                        alertDialog.dismiss();
                    }
                }
            }
        });
        //Create alert dialog
        alertDialog = builder.create();
        //Show alert dialog
        alertDialog.show();
    }

    /*********************
     * This function is used to show dialog for searching an app and scroll listview.
     * *****************************/
    public void showSearchDialouge()
    {
        //Obtain AlertDialog.Builder instance
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        //Obatin LayoutInflator instance
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        //Inflate custom layout for alert dialog
        View view = layoutInflater.inflate(R.layout.search_box_layout,null);
        //Set view as view for builder
        builder.setView(view);

        //Get reference for EditText UI element on alert dialog
        final EditText searchApp=(EditText)view.findViewById(R.id.searchApp);
        //Get reference for cancel button UI element on alert dialog
        Button cancelB=(Button)view.findViewById(R.id.cancelB);
        //Get reference for search button UI element on alert dialog
        Button searchB=(Button)view.findViewById(R.id.searchB);

        //Handle kill button click
        cancelB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Close alert dialog
                alertDialog.dismiss();
            }
        });
        //Handle search button click
        searchB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Get entered app name
                DialougeAppName=searchApp.getText().toString();
                //Close the alert dialog
                alertDialog.dismiss();
                //If current tab is tab 0
                if(mViewPager.getCurrentItem()==0)
                {
                    //Get list of  basic info of currently executing apps
                    ArrayList<ProcessPojo> p=getProcessDetails.ProcessArray;
                    //Boolean flag
                    Boolean isAppPresent=false;
                    //Iterate through app list
                    for(int i=0;i<p.size();i++) {
                        if (p.get(i).getAPPNAME() != null) {
                            //If app name matches with user entered app name
                            if (p.get(i).getAPPNAME().equalsIgnoreCase(DialougeAppName)) {
                                //Scroll tab 0 list view till searched app card
                                BasicInfoList.mRecyclerView.smoothScrollToPosition(i);
                                //Set flag true
                                isAppPresent=true;
                            }
                        }
                    }
                    if(!isAppPresent)
                    {
                        //If app not present then show error message
                        Toast.makeText(getApplicationContext(),"Application not found.", Toast.LENGTH_SHORT).show();
                    }

                }
                //If current tab is tab 1
                else
                {
                    //Get list of  network info of all installed apps
                    ArrayList<NetworkPojo> p=getNetworkDetails.NetworkArray;
                    //Boolean flag
                    Boolean isAppPresent=false;
                    //Iterate through app list
                    for(int i=0;i<p.size();i++) {
                        if (p.get(i).getAPP_NAME() != null) {
                            //If app name matches with user entered app name
                            if (p.get(i).getAPP_NAME().equalsIgnoreCase(DialougeAppName)) {
                                //Scroll tab 1 list view till searched app card
                                NetworkInfo.mRecyclerView.smoothScrollToPosition(i);
                                //Set flag true
                                isAppPresent=true;
                            }
                        }
                    }
                    if(!isAppPresent)
                    {
                        //If app not present then show error message
                        Toast.makeText(getApplicationContext(),"Application not found.", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

        //Create alert dialog
        alertDialog = builder.create();
        //Show alert dialog
        alertDialog.show();
    }


    /****************
     * This function is used to setup and show Floating action button for saving log and killing an app
     * ***************/
    public void setupFAB()
    {
        //Create an icon
        ImageView MainIcon = new ImageView(this);
        MainIcon.setImageResource(R.drawable.ic_action_new);
        MainActionButton = new FloatingActionButton.Builder(this)
                               .setContentView(MainIcon)
                               .setBackgroundDrawable(R.drawable.selector_button_red)
                               .build();


        //define the icons for the sub action buttons
        ImageView iconSaveLog = new ImageView(this);
        iconSaveLog.setImageResource(R.drawable.savelog);
        ImageView iconKillApp = new ImageView(this);
        iconKillApp.setImageResource(android.R.drawable.ic_delete);

        //set the background for all the sub buttons
        SubActionButton.Builder itemBuilder = new SubActionButton.Builder(this);
        itemBuilder.setBackgroundDrawable(getResources().getDrawable(R.drawable.selector_sub_button_gray));

        //build the sub buttons
        SubActionButton buttonSaveLog = itemBuilder.setContentView(iconSaveLog).build();
        SubActionButton buttonKillApp = itemBuilder.setContentView(iconKillApp).build();

        buttonSaveLog.setOnClickListener(this);
        buttonKillApp.setOnClickListener(this);

        //to determine which button was clicked, set Tags on each button
        buttonSaveLog.setTag(TAG_SAVELOG);
        buttonKillApp.setTag(TAG_KILLAPP);

        //add the sub buttons to the main floating action button
        SubMenu = new FloatingActionMenu.Builder(this)
                    .addSubActionView(buttonSaveLog)
                    .addSubActionView(buttonKillApp)
                    .attachTo(MainActionButton)
                    .build();
    }

    @Override
    public void onClick(View v) {
        //If save_log subbutton clicked
        if(v.getTag().equals(TAG_SAVELOG))
        {
            //Start background task for saving log details
            new SaveLog(getApplicationContext()).execute();
            //Show success message
            Toast.makeText(getApplicationContext(),"Log Saved!", Toast.LENGTH_SHORT).show();

        }
        //If kill_app subbutton is clicked
        if (v.getTag().equals(TAG_KILLAPP))
        {
            //Show alert dialog for killing applications
            showKillDialouge(this);
        }
    }

    /************************
     * This function is used to sync local database with the remote server and vice versa.
     * **************************/
    public void SyncServerData()
    {
        //Create an instance of DBHelper class
        DBHelper =new DBHelper(getApplicationContext());
        //Get UNUPDATED apps from the database
        Map<String,String> map= DBHelper.GetUnupdatedDangerousApps();

        //For every entry in map
        for(Map.Entry<String,String> entry:map.entrySet())
        {
            //If entry is not null
            if(!entry.getKey().toString().equalsIgnoreCase("") || !entry.getValue().equalsIgnoreCase("")) {
                //Create a separate thread to update app details on server
                new Server_AddDangerousApp(getApplicationContext(), entry.getKey().toString(), entry.getValue().toString()).execute();
            }
        }

        //Fetch all dangerous apps from the server
        new Server_FetchDangerousApp(getApplicationContext()).execute();
        //Fetch all safe apps from the server
        new Server_FetchSafeApp(getApplicationContext()).execute();

    }

    /**********************
     * This function is used to keep track of timestamps of server communications.
     * ***************/
    public void SetupTimestampForServer()
    {
        //Create/Obtain SharedPreferences file in PRIVATE mode
        SharedPreferences sharedPreferences=getSharedPreferences("Guardian", Context.MODE_PRIVATE);
        //Obtain editor on SharedPreference file
        SharedPreferences.Editor editor=sharedPreferences.edit();

        //Boolean flag value is obtained from file
        //If flag is not yet set then it will return TRUE
        Boolean isFirstTime=sharedPreferences.getBoolean("isFirstTime", true);
        if(isFirstTime){
            //Set dangerous_timestamp to 0
            editor.putString("dangerous_timestamp", "0");
            //Set safe_timestamp to 0
            editor.putString("safe_timestamp", "0");
            //Set isFirstTime to FALSE
            editor.putBoolean("isFirstTime", false);
            //Commit changes
            editor.commit();
        }
    }

    /***********************
     * This function is used to check is device whether the device has active network connection or not.
     * ****************************/
    public boolean isWorkingConnection()
    {
        boolean hasConnectedWifi = false;
        boolean hasConnectedMobile = false;

        //Obtain ConnectivityManager instance
        ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        //Get Network info
        android.net.NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        //Connected to the internet
        if (activeNetwork != null) {
            //Connected to wifi
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
               //Set flag to TRUE
               hasConnectedWifi=true;
            }
            //Connected to the mobile provider's data plan
            else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                //Set flag to TRUE
               hasConnectedMobile=true;
            }
        }
        return hasConnectedWifi || hasConnectedMobile;
    }
}
