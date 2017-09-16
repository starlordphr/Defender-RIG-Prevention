package com.example.altaf.guardianapp;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


/********************************************************************************
 * This class is the fragment class used to show fragment of BASIC INFO tab in MainActivity's
 * tab layout. This fragment uses fragment_basic_list.xml for user interface. It has a recycler view,
 * and every item in recycler view is a CardView showing basic information of each running app.
 *
 * USE: To create a fragment of Basic Information tab and populate Recycler view of UI.
 *
 ********************************************************************************/
public class BasicInfoList extends Fragment {

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
    ArrayList<ProcessPojo> listItems;

    //Handler object to make the thread run continuously, refreshing UI after 10 seconds.
    Handler handler=new Handler();


    //This method is called first to create Basic Info tab.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Creating a new Database helper class object.
        DBHelper =new DBHelper(getActivity());

        //Inflate fragment_basic_list.xml and access UI elements
        View rootView= inflater.inflate(R.layout.fragment_basic_list,container,false);
        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.basicInfoList);
        mRecyclerView.setHasFixedSize(true);

        //set Layoutmanager of RecyclerView
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        //Create a new object of getProcessDetails class and access its static ProcessArray.
        listItems=new getProcessDetails(getContext()).ProcessArray;

        //Setting custom MyRecyclerAdapter adapter for RecyclerView present in fragment.
        //listItems Arraylist and database helper reference is passed to custom
        //adapter class.
        mAdapter = new MyRecyclerAdapter(getContext(),listItems, DBHelper);
        mRecyclerView.setAdapter(mAdapter);



        //Thread creates a new object of getProcessDetails class and access its static ProcessArray.
        //It sets the custom adapter of RecyclerView. It runs only once to populate RecyclerView for
        //the first time.
        new Runnable() {
            @Override
            public void run() {
                listItems=new getProcessDetails(getContext()).ProcessArray;
                mAdapter = new MyRecyclerAdapter(getContext(),listItems, DBHelper);
                mRecyclerView.setAdapter(mAdapter);
            }
        }.run();

        //This function updates the data of RecyclerView with new updates data of each running app.
        //It gets executed after every 10 seconds.
        UpdateGrid();

        //Fragment View is returned to the MainActivity to show.
        return rootView;

    }


    // Runnable executes UpdateGrid() function on UI thread.
    Runnable run=new Runnable() {
        @Override
        public void run() {
            try{
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        UpdateGrid();
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    //This function fetches the updated ArrayList of basic Info from getProcessDetails class
    //and updates the listItems reference. It also updates the adapter of RecyclerView and after
    //waiting for 10 seconds again calls to Runnable run.
    public void UpdateGrid()
    {
            //Fetch new ArrayList of Basic Info
            ArrayList<ProcessPojo> tempList=new getProcessDetails(getContext()).ProcessArray;
            //Clear listItems
            listItems.clear();
            //Add newly fetched ProcessPojo objects to listItems.
            listItems.addAll(tempList);
            //notify the adapter of RecyclerView that data to be shown has changed, so that it will show
            //updated data in the RecyclerView
            mAdapter.notifyDataSetChanged();
            //wait for 10 seconds and call Runnable run again.
            handler.postDelayed(run,10*1000);
    }

}

/**********************************************************************************
 * This is the Custom Adapter class for RecyclerView. It is used to create CardView items
 * for RecyclerView. It receives Context, ArrayList of ProcessPojo objects and reference of
 * Database helper class.
 *
 * USE: To create an custom adapter to populate RecyclerView.
 **********************************************************************************/
class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.ViewHolder>{

    //Context of Application
    Context context;
    //Reference of ArrayList of ProcessPojo Objects
    ArrayList<ProcessPojo> processArray;
    //Reference of DBHelper class
    DBHelper DBHelper;
    //Reference of AlertDialouge to search application in basic info tab.
    AlertDialog resultDialouge;

    //constructor
    public MyRecyclerAdapter(Context c,ArrayList<ProcessPojo> processArray,DBHelper db) {
        this.context=c;
        this.processArray=processArray;
        this.DBHelper =db;
    }

    //This function is used to inflate XML layout for CardView and to create each CardView
    //to be displayed in RecyclerView.
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Inflate XML of CardView(i.e. each item of RecyclerView)
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_view_basic_info, parent, false);

        //Create object of ViewHolder static class, which contain one CardView.
        ViewHolder dataObjectHolder = new ViewHolder(view);
        //return blank CardView
        return dataObjectHolder;

    }

    //This function is used to populate each CardView item with the appropriate
    //data from ArrayList of ProcessPojo objects.
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        //Get ProcessPojo object present at respective position in Array.
        final ProcessPojo obj=processArray.get(position);

        //Get appName from ProcessPojo object.
        String appName=obj.getAPPNAME();
        if(appName==null) {
            //If app name is null, set it as 'System App Process'
            appName="System App Process";
        }

        // set text of app name TextView as App name from ProcessPojo object.
        holder.nameV.setText(appName);
        // set text of pid TextView as PID from ProcessPojo object.
        holder.pidV.setText(obj.getPID());
        // set text of state TextView as state from ProcessPojo object.
        holder.stateV.setText(obj.getSTATE());
        // set text of cpu TextView as CPU usage from ProcessPojo object.
        holder.cpuV.setText(obj.getCPU());
        // set text of memory TextView as Memory usage from ProcessPojo object.
        holder.memoryV.setText(obj.getMEMORY());
        //Get Drawble icon from ProcessPojo object.
        Drawable icon=obj.getAPP_ICON();
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
        holder.infoV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //After clicking on infoV TextView, fetch permissions of app and show in an AlertDialouge
                ShowPermissions(obj.getAPPNAME());
            }
        });
    }

    // Overridden function which will return total count of CardViews in RecyclerView.
    @Override
    public int getItemCount() {
        return processArray.size();
    }

    //static class used to hold UI elements of each CardView.
    //It is used to avoid flickering of RecyclerView while scrolling.
    public static class ViewHolder extends RecyclerView.ViewHolder{

        //References of UI elements
        TextView nameV,pidV,cpuV,stateV,memoryV,infoV;
        ImageView imageViewV;

        //Constructor of class, used to assign references with UI elements.
        public ViewHolder(View itemView) {
            super(itemView);
            nameV = (TextView) itemView.findViewById(R.id.nameL);
            pidV = (TextView) itemView.findViewById(R.id.pidL);
            stateV = (TextView) itemView.findViewById(R.id.stateL);
            cpuV = (TextView) itemView.findViewById(R.id.cpuL);
            memoryV = (TextView) itemView.findViewById(R.id.memoryL);
            infoV=(TextView)itemView.findViewById(R.id.infoV);
            imageViewV=(ImageView)itemView.findViewById(R.id.imageView);

        }
    }

    //Fuction used to show permissions the app has.
    //This function receives app name as parameter and fetches the permissions from the
    //database. It shows permissions in an AlertDialouge.
    public void ShowPermissions(String appName)
    {
        //receives permissions of app from database
        String permissionDetails= DBHelper.GetPermissionsDetails(appName);

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
        result.setText(permissionDetails);

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


}

