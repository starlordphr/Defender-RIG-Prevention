package com.example.altaf.guardianapp;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/***************
 * This class is used to parse proc files from path /proc/<pid>/.
 * The files which are parsed are /proc/<pid>/uptime, /proc/<pid>/stat,/proc/<pid>/schedstat,/proc/<pid>/status.
 * Use: Parsed files are used to calculate CPU usage, Scheduling rate of each running app.
 * *******************************/
public class CPUUsage {
    //Create Hashmap object to store UID and CPU details of each running app.
    Map<Integer,String> cpuUsageMap = new HashMap();
    //CPU uptime variable
    double uptime;

   /*************
    * This function calculates CPU details of each running app on the device and return details in a HashMap.
    * *************/
    public Map<Integer,String> CalUsage() {
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
                        //parse /proc/<pid>/stat file
                        BufferedReader procCPU = new BufferedReader(new FileReader("/proc/"+fileEntry.getName()+"/stat"));
                        String[] CPUdetails = procCPU.readLine().split(" ");
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

                        //Parse /proc/<pid>/schedstat file
                        procCPU = new BufferedReader(new FileReader("/proc/"+fileEntry.getName()+"/schedstat"));
                        String[] SHEDdetails = procCPU.readLine().split(" ");
                        //App runtime
                        double run_time = Long.parseLong(SHEDdetails[2]);

                        //parse /proc/<pid>/status file
                        procCPU = new BufferedReader(new FileReader("/proc/"+fileEntry.getName()+"/status"));
                        for(int i=1;i<7;i++)
                        {
                            procCPU.readLine();
                        }
                        String ArrayString=procCPU.readLine().toString();
                        //App UID
                        String[] uid = ArrayString.split("\t");
                        //Add UID and CPU details, runtime in HashMap
                        cpuUsageMap.put(Integer.parseInt(uid[1]),roundOffCpu+" "+run_time);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return cpuUsageMap;
    }



    /********
     * This function checks if directory name is Numeric or not.
     * ********/
    public static boolean isNumeric(String str)
    {
        try {
            int checkExcp = Integer.parseInt(str);
        }
        catch(NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}
