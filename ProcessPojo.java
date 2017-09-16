package com.example.altaf.guardianapp;


import android.graphics.drawable.Drawable;

/************
 * This class is a pojo class containing data members for process fragment in UI.
 * *****************/
public class ProcessPojo {
    private String PID,CPU,MEMORY,STATE,APPNAME;
    private Drawable APP_ICON;

    public Drawable getAPP_ICON() {
        return APP_ICON;
    }

    public void setAPP_ICON(Drawable APP_ICON) {
        this.APP_ICON = APP_ICON;
    }

    public String getPID() {
        return PID;
    }

    public void setPID(String PID) {
        this.PID = PID;
    }

    public String getCPU() {
        return CPU;
    }

    public void setCPU(String CPU) {
        this.CPU = CPU;
    }

    public String getMEMORY() {
        return MEMORY;
    }

    public void setMEMORY(String MEMORY) {
        this.MEMORY = MEMORY;
    }

    public String getSTATE() {
        return STATE;
    }

    public void setSTATE(String STATE) {
        this.STATE = STATE;
    }

    public String getAPPNAME() {
        return APPNAME;
    }

    public void setAPPNAME(String APPNAME) {
        this.APPNAME = APPNAME;
    }
}
