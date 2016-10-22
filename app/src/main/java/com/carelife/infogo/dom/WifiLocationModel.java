package com.carelife.infogo.dom;

import java.io.Serializable;

public class WifiLocationModel implements Serializable{

    private String macAddress;
    private double latitude;
    private double longitude;
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    private String address;
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

    @Override
    public String toString() {
        return "add: "+address+"  mac: "+macAddress+"  lat: "+latitude+"  lon: "+longitude;
    }
}
