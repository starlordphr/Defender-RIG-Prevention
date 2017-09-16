package com.example.altaf.guardianapp;

import android.graphics.drawable.Drawable;

/************
 * This class is a pojo class containing data members for result fragment in UI.
 * *****************/
public class ResultPojo {
   private String APPNAME,DANGEROUS_PERMISSIONS,THREAT;
   private int UID,isTrusted;
   private Drawable icon;

    public String getAPPNAME() {
        return APPNAME;
    }

    public void setAPPNAME(String APPNAME) {
        this.APPNAME = APPNAME;
    }

    public String getDANGEROUS_PERMISSIONS() {
        return DANGEROUS_PERMISSIONS;
    }

    public void setDANGEROUS_PERMISSIONS(String DANGEROUS_PERMISSIONS) {
        this.DANGEROUS_PERMISSIONS = DANGEROUS_PERMISSIONS;
    }

    public String getTHREAT() {
        return THREAT;
    }

    public void setTHREAT(String THREAT) {
        this.THREAT = THREAT;
    }

    public int getUID() {
        return UID;
    }

    public void setUID(int UID) {
        this.UID = UID;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public int getIsTrusted() {
        return isTrusted;
    }

    public void setIsTrusted(int isTrusted) {
        this.isTrusted = isTrusted;
    }
}
