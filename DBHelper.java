package com.example.altaf.guardianapp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DBHelper extends SQLiteOpenHelper {

    //Database version
    private static final int DATABASE_VERSION=6;
    //Database name
    private static final String DATABASE_NAME="defender.db";

    //This table stores static basic information of apps
    private static final String STATIC_TABLE="" +
            "CREATE TABLE StaticTable"+
            "(uid INTEGER PRIMARY KEY,"+
            "appName TEXT, pkgName TEXT,processName TEXT,permissions TEXT);"
            ;
    //This table stores UID and Package name of safe apps
    private static final String SAFE_APP_TABLE=""+
            "CREATE TABLE SafeApplications" +
            "(uid INTEGER PRIMARY KEY,"+
            "pkgName TEXT);";

    //This table stores Heuristics parameters of each app
    private static final String HEURISTICS_TABLE=""+
            "CREATE TABLE Heuristics"+
            "(uid INTEGER PRIMARY KEY,"+
            "permissions TEXT," +
            "cpuUsage DOUBLE," +
            "schedRate DOUBLE DEFAULT 0"+
            ");";

    //This table stores entries of malicious apps detected on device.
    private static final String TEMP_DANGEROUS_APP_TABLE="" +
            "CREATE TABLE Temp_Danger" +
            "(uid INTEGER PRIMARY KEY," +
            "app_name TEXT,"+
            "pkgName TEXT," +
            "isTrusted INTEGER DEFAULT 1," +
            "dangerousPermissions TEXT," +
            "threat TEXT," +
            "timestamp TEXT DEFAULT 'N/A'," +
            "isUpdated INTEGER DEFAULT 0"+
            ")";
    //This table stores entries of malicious apps detected by community
    private static final String GLOBAL_DANGEROUS_APP_TABLE="" +
            "CREATE TABLE Global_Danger" +
            "(pkgName TEXT PRIMARY KEY," +
            "app_name TEXT"+
            ")";
    //This table stores entries of safe apps globally
    private static final String GLOBAL_SAFE_APP_TABLE="" +
            "CREATE TABLE Global_Safe" +
            "(pkgName TEXT PRIMARY KEY," +
            "app_name TEXT"+
            ")";
    //This table stores URL connections of each app
    private static final String URL_TABLE="" +
            "CREATE TABLE Url_table" +
            "(uid INTEGER," +
            "localAddr TEXT UNIQUE," +
            "remoteAddr TEXT UNIQUE," +
            "connectionType TEXT"+
            ")";


    Context context;
    DBHelper(Context c)
    {
        super(c, DATABASE_NAME, null, DATABASE_VERSION);
        this.context=c;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL(STATIC_TABLE);
        sqLiteDatabase.execSQL(SAFE_APP_TABLE);
        sqLiteDatabase.execSQL(HEURISTICS_TABLE);
        sqLiteDatabase.execSQL(TEMP_DANGEROUS_APP_TABLE);
        sqLiteDatabase.execSQL(GLOBAL_DANGEROUS_APP_TABLE);
        sqLiteDatabase.execSQL(GLOBAL_SAFE_APP_TABLE);
        sqLiteDatabase.execSQL(URL_TABLE);


    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {


        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+STATIC_TABLE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+SAFE_APP_TABLE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+HEURISTICS_TABLE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TEMP_DANGEROUS_APP_TABLE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+GLOBAL_DANGEROUS_APP_TABLE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+GLOBAL_SAFE_APP_TABLE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+URL_TABLE);

        sqLiteDatabase.execSQL(STATIC_TABLE);
        sqLiteDatabase.execSQL(SAFE_APP_TABLE);
        sqLiteDatabase.execSQL(HEURISTICS_TABLE);
        sqLiteDatabase.execSQL(TEMP_DANGEROUS_APP_TABLE);
        sqLiteDatabase.execSQL(GLOBAL_DANGEROUS_APP_TABLE);
        sqLiteDatabase.execSQL(GLOBAL_SAFE_APP_TABLE);
        sqLiteDatabase.execSQL(URL_TABLE);

    }

    //This function inserts basic information of apps in the database
    public void InsertAppDetails(int uid,String appName,String pkgName,String processName,String permissions)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        try {
            db.execSQL("INSERT OR IGNORE INTO StaticTable (uid,appName,pkgName,processName,permissions)" +
                            "VALUES(" + uid + "," +
                            "'" + appName + "'," +
                            "'" + pkgName + "'," +
                            "'" + processName + "'," +
                            "'" + permissions + "'" +
                            ");"
            );

            db.execSQL("INSERT OR IGNORE INTO Heuristics (uid,permissions,cpuUsage)" +
                            "VALUES(" + uid + "," +
                            "'" + permissions + "'," +
                            0 +
                            ");"
            );
        }
        catch (SQLiteException e)
        {
            Log.d("ERROR","UNABLE TO Insert "+e.getMessage());
        }
        db.close();
    }

    //This function inserts safe apps UID and Package name in the database
    public void InsertSafeApp(int uid,String pkgName)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        try {
            db.execSQL("INSERT OR IGNORE INTO SafeApplications (uid,pkgName)" +
                            " VALUES(" + uid + "," +
                            "'" + pkgName + "'" +
                            ");"
            );
        }
        catch (Exception e)
        {
            Log.d("Error: ", e.getMessage());
        }
        db.close();
    }


    //This function accepts app name and returns permissions granted for each app.
    public String GetPermissionsDetails(String appName )
    {
        String op="";
        SQLiteDatabase db=this.getWritableDatabase();

        try {
            Cursor c = db.rawQuery("SELECT * FROM StaticTable WHERE appName='" + appName + "';", null);
            if (c.moveToFirst()) {
                do {
                    op += "UID: " + c.getInt(0) + "\nApp Name: " + c.getString(1) + "\nPackage Name: " + c.getString(2) + "\nProcess Name: " + c.getString(3) + "\nPermissions:\n" + c.getString(4) + "\n";

                } while (c.moveToNext());
            }
        }
        catch (Exception e)
        {
            Log.d("Error: ", e.getMessage());
        }
        db.close();
        return op;
    }
    //This function accepts app UID and returns CPU usage of app.
    public double GetCpuUsage(int uid)
    {
        double cpuUsage=0;
        SQLiteDatabase db=this.getWritableDatabase();

        try {
            Cursor c = db.rawQuery("Select cpuUsage from Heuristics where uid=" + uid + ";", null);
            if (c.moveToFirst()) {
                do {
                    cpuUsage = c.getDouble(0);
                } while (c.moveToNext());
            }
        }
        catch (Exception e)
        {
            Log.d("Error: ", e.getMessage());
        }
        db.close();

        return cpuUsage;
    }
    //This function accepts app UID and returns scheduling rate of app.
    public double GetShedRate(int uid)
    {
        double shedRate=0;
        SQLiteDatabase db=this.getWritableDatabase();

        try {
            Cursor c = db.rawQuery("Select schedRate from Heuristics where uid=" + uid + ";", null);
            if (c.moveToFirst()) {
                do {
                    shedRate = c.getDouble(0);
                } while (c.moveToNext());
            }
        }
        catch (Exception e)
        {
            Log.d("Error: ", e.getMessage());
        }
        db.close();
        return shedRate;
    }
    //This function updates CPU usage and scheduling rate of app.
    public void UpdateCpuUsageAndSched(int uid,double cpuUsage,double sched)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        try {
            db.execSQL("Update Heuristics set cpuUsage=" + cpuUsage + " , schedRate=" + sched + " where uid=" + uid + ";");
        }
        catch (Exception e)
        {
            Log.d("Error: ", e.getMessage());
        }
        db.close();
    }
    //This function accepts app UID and returns permissions granted for each app.
    public String GetPermissionsByUid(int uid)
    {
        String permissions="";
        SQLiteDatabase db=this.getWritableDatabase();

        try {
            Cursor c = db.rawQuery("Select permissions from Heuristics where uid=" + uid + ";", null);
            if (c.moveToFirst()) {
                do {
                    permissions = c.getString(0);
                } while (c.moveToNext());
            }
        }
        catch (Exception e)
        {
            Log.d("Error: ", e.getMessage());
        }
        db.close();
        return permissions;
    }
    //This function accepts app UID and returns app name.
    public String GetAppNameFromUid(int uid)
    {
        String appName="";
        SQLiteDatabase db=this.getWritableDatabase();

        try {
            Cursor c = db.rawQuery("Select appName from StaticTable where uid=" + uid + ";", null);
            if (c.moveToFirst()) {
                do {
                    appName = c.getString(0);
                } while (c.moveToNext());
            }
        }
        catch (Exception e)
        {
            Log.d("Error: ", e.getMessage());
        }
        db.close();
        return appName;
    }

    //This function accepts app UID and returns package name of app.
    public String GetPkgNameFromUid(int uid)
    {
        String pkgName="";
        SQLiteDatabase db=this.getWritableDatabase();

        try {
            Cursor c = db.rawQuery("Select pkgName from StaticTable where uid=" + uid + ";", null);
            if (c.moveToFirst()) {
                do {
                    pkgName = c.getString(0);
                } while (c.moveToNext());
            }
        }
        catch (Exception e)
        {
            Log.d("Error: ", e.getMessage());
        }
        db.close();
        return pkgName;
    }
    //This function accepts app name and returns package name of app.
    public String GetPkgNameFromAppName(String appName)
    {
        String pkgName="N/A";
        SQLiteDatabase db=this.getWritableDatabase();

        try {
            Cursor c = db.rawQuery("Select pkgName from StaticTable where appName='" + appName + "';", null);
            if (c.moveToFirst()) {
                do {
                    pkgName = c.getString(0);
                } while (c.moveToNext());
            }
        }
        catch(SQLiteException e)
        {
            Log.d("ERROR",""+e.getMessage());
        }
        db.close();
        return pkgName;
    }

    //This function inserts details of detected malicious app in the database.
    public void addTempDangerousApp(int uid,String appName,String pkgName,int isTrusted, String dang_permissions,String threat)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        try {

            db.execSQL("INSERT OR IGNORE INTO Temp_Danger (uid,app_name,pkgName,isTrusted,dangerousPermissions,threat)" +
                            " VALUES(" + uid + "," +
                            "'" + appName + "'," +
                            "'" + pkgName + "'," +
                            "" +isTrusted+"," +
                            "'" +dang_permissions+"'," +
                            "'"+threat+"'"+
                            ");"
            );
        }
        catch (Exception e)
        {
            Log.d("Error: ", e.getMessage());
        }
        db.close();
    }
    //This function updates the flag in the database if SYNC details of app changes.
    public void UpdateTempDangerousApp(String pkgName,int isUpdated )
    {
        SQLiteDatabase db=this.getWritableDatabase();
        try {
            db.execSQL("UPDATE Temp_Danger SET isUpdated=" + isUpdated + " where pkgName='" + pkgName + "';");
        }
        catch (Exception e)
        {
            Log.d("Error: ", e.getMessage());
        }

        db.close();
    }

    //This function return a map of all malicious apps which are not synced with the server.
    public Map<String,String> GetUnupdatedDangerousApps()
    {
        Map<String,String> map=new HashMap<String,String>();

        SQLiteDatabase db=this.getWritableDatabase();

        try {
            Cursor c = db.rawQuery("Select app_name,pkgName from Temp_Danger where isUpdated=" + 0 + ";", null);
            if (c.moveToFirst()) {
                do {
                    map.put(c.getString(0), c.getString(1));
                } while (c.moveToNext());
            }
        }
        catch (Exception e)
        {
            Log.d("Error: ", e.getMessage());
        }
        db.close();
        return map;
    }

    //This function return a list of all malicious apps detected by the system
    public ArrayList<String> GetAllDangerousApps()
    {
        ArrayList<String> result=new ArrayList<String>();
        SQLiteDatabase db=this.getWritableDatabase();

        try {
            Cursor c = db.rawQuery("Select * from Temp_Danger where isTrusted=0;", null);   //change
            if (c.moveToFirst()) {
                do {
                    result.add(c.getString(2));
                } while (c.moveToNext());
            }
        }
        catch (Exception e)
        {
            Log.d("Error: ", e.getMessage());
        }
        db.close();
        return  result;
    }

    //This function is used to check if app specified with UID is safe or not.
    public boolean isSafe(int uid)
    {
        int temp=0;
        SQLiteDatabase db=this.getWritableDatabase();

        try {
            Cursor c = db.rawQuery("Select uid from SafeApplications where uid=" + uid + ";", null);
            if (c.moveToFirst()) {
                do {
                    temp = c.getInt(0);
                } while (c.moveToNext());
            }
            if (temp == uid) {
                return true;
            }
        }
        catch (Exception e)
        {
            Log.d("Error: ", e.getMessage());
        }
        db.close();
        return  false;
    }

    //This function is used to insert malicious apps detected by community and fetched by user.
    public void AddGlobalDangerousApp(String pkgName,String app_name)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        try {
            db.execSQL("INSERT OR IGNORE INTO Global_Danger (pkgName,app_Name)" +
                            "VALUES('" + pkgName + "'," +
                            "'" + app_name + "'" +
                            ");"
            );
        }
        catch (Exception e)
        {
            Log.d("Error: ", e.getMessage());
        }
        db.close();
    }

    //This function is used to fetch all malicious apps stored in local database and detected by community.
    public HashMap<String,String> GetGlobalDangerousApp()
    {
        HashMap<String,String> map=new HashMap<String,String>();
        SQLiteDatabase db=this.getWritableDatabase();

        try {
            Cursor c = db.rawQuery("Select * from Global_Danger;", null);
            if (c.moveToFirst()) {
                do {
                    map.put(c.getString(0), c.getString(1));
                } while (c.moveToNext());
            }
        }
        catch (Exception e)
        {
            Log.d("Error: ", e.getMessage());
        }
        db.close();

        return map;
    }
    //This function is used to insert safe apps detected by community and fetched by user.
    public void AddGlobalSafeApp(String pkgName,String app_name)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        try {
            db.execSQL("INSERT OR IGNORE INTO Global_Safe (pkgName,app_Name)" +
                            "VALUES('" + pkgName + "'," +
                            "'" + app_name + "'" +
                            ");"
            );
        }
        catch (Exception e)
        {
            Log.d("Error: ", e.getMessage());
        }
        db.close();
    }
    //This function is used to fetch all safe apps stored in local database and detected by community.
    public HashMap<String,String> GetGlobalSafeApp()
    {
        HashMap<String,String> map=new HashMap<String,String>();
        SQLiteDatabase db=this.getWritableDatabase();

        try {
            Cursor c = db.rawQuery("Select * from Global_Safe;", null);
            if (c.moveToFirst()) {
                do {
                    map.put(c.getString(0), c.getString(1));
                  //  Log.d("Server_Safe", c.getString(0) + "----" + c.getString(1));
                } while (c.moveToNext());
            }
        }
        catch (Exception e)
        {
            Log.d("Error: ", e.getMessage());
        }
        db.close();

        return map;
    }
   //This function is used to insert URL connection details of a app in database
    public void InsertUrl(int uid,String localAddr,String remoteAddr,String connType)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        try {
            String query="INSERT OR IGNORE INTO Url_table (uid,localAddr,remoteAddr,connectionType)" +
                    " VALUES(" + uid + "," +
                    "'" + localAddr + "'," +
                    "'" + remoteAddr + "'," +
                    "'"+connType+"'" +
                    ");";
            db.execSQL(query);

           // Log.d("URL", query);
        }
        catch (Exception e)
        {
            Log.d("Error: ", e.getMessage());
        }
        db.close();
    }

    //This function is used to fetch URL connection details of an app from UID.
    public String GetConnectionsFromUid(int uid)
    {
        String result="";
        String TCP_IPv6="\n";
        String TCP_IPv4="\n";
        String UDP_IPv6="\n";
        String UDP_IPv4="\n";
        SQLiteDatabase db=this.getWritableDatabase();

        try {
            Cursor c = db.rawQuery("Select localAddr,remoteAddr,connectionType from Url_table where uid="+uid+";", null);
            if (c.moveToFirst()) {
                do {
                    if(c.getString(2).equalsIgnoreCase("TCP6")) {
                        TCP_IPv6 += "Local Address:\n" + c.getString(0) + "\nRemote Address:\n" + c.getString(1) + "\n\n";
                    }
                    else if(c.getString(2).equalsIgnoreCase("TCP4")) {
                        TCP_IPv4 += "Local Address:\n" + c.getString(0) + "\nRemote Address:\n" + c.getString(1) + "\n\n";
                    }
                    else if(c.getString(2).equalsIgnoreCase("UDP6")) {
                        UDP_IPv6 += "Local Address:\n" + c.getString(0) + "\nRemote Address:\n" + c.getString(1) + "\n\n";
                    }
                    else if(c.getString(2).equalsIgnoreCase("UDP6")) {
                        UDP_IPv4 += "Local Address:\n" + c.getString(0) + "\nRemote Address:\n" + c.getString(1) + "\n\n";
                    }
                } while (c.moveToNext());
            }
        }
        catch (Exception e)
        {
            Log.d("Error: ", e.getMessage());
        }
        db.close();



        result="TCP IPv6:\n"+TCP_IPv6+"TCP IPv4:\n"+TCP_IPv4+"UDP IPv6:\n"+UDP_IPv6+"UDP IPv4:\n"+UDP_IPv4;
        return result;
    }

    //This function is used create an object array of ResultPojo class from detected malicious apps
    public ArrayList<ResultPojo> GetResults()
    {
        //Create new object array of ResultPojo class
        ArrayList<ResultPojo> resultArray=new ArrayList<>();

        SQLiteDatabase db=this.getWritableDatabase();

        try {
            Cursor c = db.rawQuery("Select * from Temp_Danger;", null);
            if (c.moveToFirst()) {
                do {
                    //Create a new object of ResultPojo class
                    ResultPojo app=new ResultPojo();
                    //Set app UID
                    app.setUID(c.getInt(0));
                    //Set app name
                    app.setAPPNAME(c.getString(1));
                    //Set trust flag
                    app.setIsTrusted(c.getInt(3));
                    //Set app permissions
                    app.setDANGEROUS_PERMISSIONS(c.getString(4));
                    //Set app threat
                    app.setTHREAT(c.getString(5));
                    //Add object in array
                    resultArray.add(app);
                } while (c.moveToNext());
            }
        }
        catch (Exception e)
        {
            Log.d("Error: ", e.getMessage());
        }
        db.close();
        return resultArray;
    }
    //This function is used to mark a malicious app in database as trusted
    public void MarkAppTrusted(int uid,String pkgName)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        try {
            db.execSQL("UPDATE Temp_Danger SET isTrusted=" +1+ " where uid=" + uid + ";");
            db.execSQL("INSERT OR IGNORE INTO SafeApplications (uid,pkgName)" +
                            " VALUES(" + uid + "," +
                            "'" + pkgName + "'" +
                            ");"
            );
        }
        catch (Exception e)
        {
            Log.d("Error: ", e.getMessage());
        }

        db.close();
    }

    //This function is used to mark a malicious app in database as untrusted
    public void MarkAppUntrusted(int uid)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        try {
            db.execSQL("UPDATE Temp_Danger SET isTrusted=" +0+ " where uid=" + uid + ";");
            db.execSQL("DELETE FROM SafeApplications WHERE uid="+uid+";");
        }
        catch (Exception e)
        {
            Log.d("Error: ", e.getMessage());
        }

        db.close();


    }
}
