package com.example.altaf.guardianapp;

import android.graphics.drawable.Drawable;

/************
 * This class is a pojo class containing data members for network fragment in UI.
 * *****************/
public class NetworkPojo {
    private String APP_NAME,UID,TX_BYTES,RX_BYTES;

    public Drawable getAPP_ICON() {
        return APP_ICON;
    }

    public void setAPP_ICON(Drawable APP_ICON) {
        this.APP_ICON = APP_ICON;
    }

    private Drawable APP_ICON;

    public String getAPP_NAME() {
        return APP_NAME;
    }

    public void setAPP_NAME(String APP_NAME) {
        this.APP_NAME = APP_NAME;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getTX_BYTES() {
        return TX_BYTES;
    }

    public void setTX_BYTES(String TX_BYTES) {
        this.TX_BYTES = TX_BYTES;
    }

    public String getRX_BYTES() {
        return RX_BYTES;
    }

    public void setRX_BYTES(String RX_BYTES) {
        this.RX_BYTES = RX_BYTES;
    }
}
