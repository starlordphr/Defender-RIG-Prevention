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
 * This class is the fragment class used to show fragment of NETWORK INFO tab in MainActivity's
 * tab layout. This fragment uses fragment_network_list.xml for user interface. It has a recycler view,
 * and every item in recycler view is a CardView showing network information of each running app.
 *
 * USE: To create a fragment of Network Information tab and populate Recycler view of UI.
 *
 ********************************************************************************/
public class NetworkInfo extends Fragment {

    //recycler view reference declared as static because searching on recycler view
    //is performed from outside this class(MainActivity)
    static RecyclerView mRecyclerView;
    //RecyclerView adapter reference to provide data for RecyclerView
    private RecyclerView.Adapter mAdapter;
    //RecyclerView layoutManager reference to provide layout bounds for RecylcerView
    private RecyclerView.LayoutManager mLayoutManager;

    //Database helper class reference
    DBHelper DBHelper;
    //Reference of ArrayList containing NetworkPojo objects containing Network Info details of each running app.
    ArrayList<NetworkPojo> listItems;
    //Handler object to make the thread run continuously, refreshing UI after 30 seconds.
    Handler handler=new Handler();

    //This method is called first to create Network Info tab.
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView=inflater.inflate(R.layout.fragment_network_list,container,false);

        //Creating a new Database helper class object.
        DBHelper =new DBHelper(getActivity());

        //Inflate fragment_network_list.xml and access UI elements
        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.basicInfoList);
        mRecyclerView.setHasFixedSize(true);

        //set Layoutmanager of RecyclerView
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        //Create a new object of getNetworkDetails class and access its static NetworkArray.
        listItems=new getNetworkDetails(getActivity()).NetworkArray;

        //Setting custom MyRecyclerAdapter adapter for RecyclerView present in fragment.
        //listItems Arraylist and database helper reference is passed to custom
        //adapter class.
        mAdapter = new MyRecyclerAdapterNetwork(getContext(),listItems, DBHelper);
        mRecyclerView.setAdapter(mAdapter);


        //Thread creates a new object of getNetworkDetails class and access its static NetworkArray.
        //It sets the custom adapter of RecyclerView. It runs only once to populate RecyclerView for
        //the first time.
        new Runnable() {
            @Override
            public void run() {
                listItems=new getNetworkDetails(getActivity()).NetworkArray;
                mAdapter = new MyRecyclerAdapterNetwork(getContext(),listItems, DBHelper);
                mRecyclerView.setAdapter(mAdapter);
            }
        }.run();

        //This function updates the data of RecyclerView with new updates data of each installed app.
        //It gets executed after every 30 seconds.
        UpdateUI();

        //Fragment View is returned to the MainActivity to show.
        return rootView;
    }

    // Runnable executes UpdateUI() function on UI thread.
    Runnable run=new Runnable() {
        @Override
        public void run() {
            try{
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        UpdateUI();
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    //This function fetches the updated ArrayList of network Info from getNetworkDetails class
    //and updates the listItems reference. It also updates the adapter of RecyclerView and after
    //waiting for 30 seconds again calls to Runnable run.
    void UpdateUI()
    {
        //Fetch new ArrayList of Network Info
        ArrayList<NetworkPojo> tempList=new getNetworkDetails(getContext()).NetworkArray;
        //Clear listItems
        listItems.clear();
        //Add newly fetched NetworkPojo objects to listItems.
        listItems.addAll(tempList);
        //notify the adapter of RecyclerView that data to be shown has changed, so that it will show
        //updated data in the RecyclerView
        mAdapter.notifyDataSetChanged();
        //wait for 30 seconds and call Runnable run again.
        handler.postDelayed(run,30*1000);
    }


}

/**********************************************************************************
 * This is the Custom Adapter class for RecyclerView. It is used to create CardView items
 * for RecyclerView. It receives Context, ArrayList of NetworkPojo objects and reference of
 * Database helper class.
 *
 * USE: To create an custom adapter to populate RecyclerView.
 **********************************************************************************/
class MyRecyclerAdapterNetwork extends RecyclerView.Adapter<MyRecyclerAdapterNetwork.ViewHolder>{

    //Context of Application
    Context context;
    //Reference of ArrayList of NetworkPojo Objects
    ArrayList<NetworkPojo> networkArray;
    //Reference of DBHelper class
    DBHelper DBHelper;

    //Reference of AlertDialouge to search application in Network info tab.
    AlertDialog resultDialouge;

    public MyRecyclerAdapterNetwork(Context c,ArrayList<NetworkPojo> networkArray,DBHelper db) {
        this.context=c;
        this.networkArray=networkArray;
        this.DBHelper =db;
    }

    //This function is used to inflate XML layout for CardView and to create each CardView
    //to be displayed in RecyclerView.
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Inflate XML of CardView(i.e. each item of RecyclerView)
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_view_network_info, parent, false);

        //Create object of ViewHolder static class, which contain one CardView.
        ViewHolder viewHolder = new ViewHolder(view);
        //return blank CardView
        return viewHolder;

    }

    //This function is used to populate each CardView item with the appropriate
    //data from ArrayList of NetworkPojo objects.
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        //Get NetworkPojo object present at respective position in Array.
        final NetworkPojo obj=networkArray.get(position);

        //Get appName from NetworkPojo object.
        holder.name.setText(obj.getAPP_NAME());
        // set text of app UID
        holder.uid.setText(obj.getUID());
        // set text of app Transmitted Bytes
        holder.tx_bytes.setText(new String(Double.parseDouble(obj.getTX_BYTES()+"")/(1024*1024)+" ").substring(0,4)+" MB");
        // set text of app Received Bytes
        holder.rx_bytes.setText(new String(Double.parseDouble(obj.getRX_BYTES()+"")/(1024*1024)+" ").substring(0,4)+" MB");
        //Get app icon
        Drawable icon=obj.getAPP_ICON();
        if(icon==null)
        {
            //If App doesn't have any icon, set its icon as custom icon.
            holder.imageView.setImageResource(R.drawable.system);
        }
        else
        {
            // set image of icon ImageView as AppIcon from NetworkPojo object.
            holder.imageView.setImageDrawable(icon);
        }
        //set onClickListener on NETWORK CONNECTIONs TextView.
        holder.connections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Call ShowURL function to show URL connection made by app
                ShowURL(obj.getUID());
            }
        });
    }

    // Overridden function which will return total count of CardViews in RecyclerView.
    @Override
    public int getItemCount() {
        return networkArray.size();
    }

    //static class used to hold UI elements of each CardView.
    //It is used to avoid flickering of RecyclerView while scrolling.
    public static class ViewHolder extends RecyclerView.ViewHolder{

        //References of UI elements
        TextView name,uid,tx_bytes,rx_bytes,connections;
        ImageView imageView;

        //Constructor of class, used to assign references with UI elements.
        public ViewHolder(View itemView) {
            super(itemView);
            name=(TextView)itemView.findViewById(R.id.nameLNet);
            uid=(TextView)itemView.findViewById(R.id.pidL);
            connections=(TextView)itemView.findViewById(R.id.UrlConnections);
            tx_bytes=(TextView)itemView.findViewById(R.id.trnsV);
            rx_bytes=(TextView)itemView.findViewById(R.id.recvV);
            imageView=(ImageView)itemView.findViewById(R.id.imageViewNet);
        }
    }

    /***************************
     * This funtion is used to show Alert dialog containg details of URL connections made by app
     * *****************************************/
    public void ShowURL(String uid)
    {
        //receives permissions of app from database
        String URLDetails= DBHelper.GetConnectionsFromUid(Integer.parseInt(uid));

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
        result.setText(URLDetails);

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
