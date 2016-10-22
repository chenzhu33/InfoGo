package com.carelife.infogo.dom;

import java.io.Serializable;

/**
 * Created by wangrh on 2016/10/22.
 */
public class WifiLocationModel implements Serializable{

    private String macAddress;
    private double latitude;
    private double longitude;

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
