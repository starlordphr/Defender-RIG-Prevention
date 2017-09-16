package com.example.altaf.guardianapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by HAWONG on 23-Apr-16.
 */
public class ResultsList extends Fragment {

    //recycler view reference declared as static because searching on recycler view
    //is performed from outside this class(MainActivity).
    static RecyclerView mRecyclerView;

    //RecyclerView adapter reference to provide data for RecyclerView
    private RecyclerView.Adapter mAdapter;

    //RecyclerView layoutManager reference to provide layout bounds for RecylcerView
    private RecyclerView.LayoutManager mLayoutManager;

    //Database helper class reference
    DBHelper DBHelper;

    //Reference of ArrayList containing ProcessPojo objects containing Basic Info details of each running app.
    ArrayList<ResultPojo> listItems;

    //This method is called first to create Basic Info tab.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Creating a new Database helper class.
        DBHelper =new DBHelper(getActivity());

        //Inflate fragment_basic_list.xml and access UI elements
        View rootView= inflater.inflate(R.layout.fragment_result_list,container,false);
        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.resultList);
        mRecyclerView.setHasFixedSize(true);

        //set Layoutmanager of RecyclerView
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        //Create a new object of getProcessDetails class and access its static ProcessArray.
        listItems=new getResults(getActivity()).resultPojos;

        //Setting custom MyRecyclerAdapter adapter for RecyclerView present in fragment.
        //listItems Arraylist and database helper reference is passed to custom
        //adapter class.
        mAdapter = new MyRecyclerAdapterResults(getContext(),listItems, DBHelper);
        mRecyclerView.setAdapter(mAdapter);

        //Fragment View is returned to the MainActivity to show.
        return rootView;

    }


}

class MyRecyclerAdapterResults extends RecyclerView.Adapter<MyRecyclerAdapterResults.ViewHolder>{

    //Context of Application
    Context context;
    //Reference of ArrayList of ProcessPojo Objects
    ArrayList<ResultPojo> resultsArray;
    //Reference of DBHelper class
    DBHelper DBHelper;
    //Reference of AlertDialouge to search application in basic info tab.
    AlertDialog resultDialouge;


    //constructor
    public MyRecyclerAdapterResults(Context c,ArrayList<ResultPojo> resultsArray,DBHelper db) {
        this.context=c;
        this.resultsArray=resultsArray;
        this.DBHelper =db;
    }

    //This function is used to inflate XML layout for CardView and to create each CardView
    //to be displayed in RecyclerView.
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Inflate XML of CardView(i.e. each item of RecyclerView)
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_view_results, parent, false);

        //Create object of ViewHolder static class, which contain one CardView.
        ViewHolder dataObjectHolder = new ViewHolder(view);
        //return blank CardView
        return dataObjectHolder;

    }

    //This function is used to populate each CardView item with the appropriate
    //data from ArrayList of ProcessPojo objects.
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        //Get ProcessPojo object present at respective position in Array.
        final ResultPojo obj=resultsArray.get(position);

        //Get appName from ProcessPojo object.
        String appName=obj.getAPPNAME();
        if(appName==null) {
            //If app name is null, set it as 'System App Process'
            appName="System App Process";
        }

        if(!DBHelper.isSafe(obj.getUID()))
        {
            holder.switchV.setChecked(false);
            holder.switchV.setText("Untrusted");
        }
        else
        {
            holder.switchV.setChecked(true);
            holder.switchV.setText("Trusted");
        }
        // set text of app name TextView as App name from ProcessPojo object.
        holder.nameV.setText(appName);
        // set text of pid TextView as PID from ProcessPojo object.
        holder.uidV.setText(obj.getUID()+"");

        // set text of memory TextView as Memory usage from ProcessPojo object.
        holder.threatV.setText(obj.getTHREAT());
        //Get Drawble icon from ProcessPojo object.
        String pkgName=DBHelper.GetPkgNameFromUid(obj.getUID());
        Drawable icon=getIconFromUid(pkgName);
        if(icon==null)
        {
            //If App doesn't have any icon, set its icon as custom icon.
            holder.imageViewV.setImageResource(R.drawable.system);
        }
        else
        {
            // set image of icon ImageView as AppIcon from ProcessPojo object.
            holder.imageViewV.setImageDrawable(icon);
        }

        //set onClickListener on infoV TextView.
        holder.permissionsV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //After clicking on infoV TextView, fetch permissions of app and show in an AlertDialouge
                ShowDialouge(obj.getDANGEROUS_PERMISSIONS());
            }
        });

        holder.urlV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowDialouge(DBHelper.GetConnectionsFromUid(obj.getUID()));
            }
        });





        holder.switchV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!holder.switchV.isChecked())
                {
                    holder.switchV.setText("Untrusted");
                    getResults.resultPojos.get(position).setIsTrusted(0);
                    updateList(getResults.resultPojos);
                    MarkAppUntrusted(obj.getUID());

                }
                else
                {
                    holder.switchV.setText("Trusted");
                    getResults.resultPojos.get(position).setIsTrusted(1);
                    updateList(getResults.resultPojos);
                    MarkAppTrusted(obj.getUID());
                }
            }
        });

    }

    // Overridden function which will return total count of CardViews in RecyclerView.
    @Override
    public int getItemCount() {
        return resultsArray.size();
    }

    public void updateList(ArrayList<ResultPojo> data) {
        resultsArray = data;
        notifyDataSetChanged();
    }

    //static class used to hold UI elements of each CardView.
    //It is used to avoid flickering of RecyclerView while scrolling.
    public static class ViewHolder extends RecyclerView.ViewHolder{

        //References of UI elements
        TextView nameV,uidV,permissionsV,urlV,threatV;
        Switch switchV;
        ImageView imageViewV;

        //Constructor of class, used to assign references with UI elements.
        public ViewHolder(View itemView) {
            super(itemView);
            nameV = (TextView) itemView.findViewById(R.id.name_results);
            uidV = (TextView) itemView.findViewById(R.id.uidResults);
            permissionsV = (TextView) itemView.findViewById(R.id.DangPermissions);
            urlV = (TextView) itemView.findViewById(R.id.netUrls);
            threatV = (TextView) itemView.findViewById(R.id.threat);
            switchV=(Switch)itemView.findViewById(R.id.trustSwitch);
            imageViewV=(ImageView)itemView.findViewById(R.id.imageView_result);

        }



    }

    //Fuction used to show permissions the app has.
    //This function receives app name as parameter and fetches the permissions from the
    //database. It shows permissions in an AlertDialouge.
    public void ShowDialouge(String text)
    {

        //Create a AlertDialouge Builder object
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        //Get LayoutInflater Object
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        //Inflate  permissions_result_layout.xml file for custom dialouge.
        View view = layoutInflater.inflate(R.layout.permissions_result_layout, null);
        //Set Custom dialouge view as View of AlertBuilder.
        builder.setView(view);

        //Access reference of TextView for showing permissions on AlertDialouge.
        TextView result = (TextView) view.findViewById(R.id.searchResult);
        //Set permissions as text of result TextView.
        result.setText(text);

        //Access reference of Button for OK Button on AlertDialouge.
        Button okB=(Button)view.findViewById(R.id.okB);

        //Set onClick method for Ok Button
        okB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Close the Dialouge
                resultDialouge.dismiss();
            }
        });

        //Create a dialouge from builder and assign it to AlertDialouge Reference.
        resultDialouge = builder.create();
        //Show Dialouge
        resultDialouge.show();
    }

    /******************
     * This function is used to get app icon from package name
     * *****************/
    public Drawable getIconFromUid(String pkgName)
    {
        Drawable icon=null;
        PackageManager packageManager=context.getPackageManager();
        try {
            ApplicationInfo app = context.getPackageManager().getApplicationInfo(pkgName, 0);
           icon = packageManager.getApplicationIcon(app);

        } catch (PackageManager.NameNotFoundException e) {
            Log.d("Error Icon ", e.getMessage());
        }
        return icon;
    }

    /**************
     * This function is used to mark an app as TRUSTED in database
     * **************/
    public void MarkAppTrusted(int uid)
    {
        String pkgName=DBHelper.GetPkgNameFromUid(uid);
        DBHelper.MarkAppTrusted(uid,pkgName);
    }

    /**************
     * This function is used to mark an app as UNTRUSTED in database
     * **************/
    public void MarkAppUntrusted(int uid)
    {
        DBHelper.MarkAppUntrusted(uid);
    }

}


