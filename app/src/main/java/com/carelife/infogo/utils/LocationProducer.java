package com.carelife.infogo.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import java.util.List;

/**
 * Created by wangrh on 2016/10/21.
 */
public class LocationProducer {

    private Location latestLocation;
    private LocationManager locationManager;
    private String provider;
    private boolean isLocating = false;
    private Context context;

    public LocationProducer(Context context){
        this.context = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        provider = getProvider();
    }

    public boolean isLocating(){
        return isLocating;
    }

    public void start(){
        if (provider.isEmpty()) {
            return;
        }
        if (hasPermission()) {
            isLocating = true;
            locationManager.requestLocationUpdates(provider, Global.LOCATION_REQUEST_TIME, 0, locationListener);
        }
    }

    public void stop(){
        isLocating = false;
        locationManager.removeUpdates(locationListener);
    }

    public Location getLastKnowLocation(){
        return locationManager.getLastKnownLocation(provider);
    }

    public Location getLatestLocation(){
        return latestLocation;
    }

    LocationListener locationListener = new LocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle arg2) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onLocationChanged(Location location) {
            latestLocation = location;
        }
    };

    public String getProvider() {
        List<String> providers = locationManager.getProviders(true);
        String locationProvider = "";
        if (providers.contains(LocationManager.GPS_PROVIDER)) {
            locationProvider = LocationManager.GPS_PROVIDER;
        }
        if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            locationProvider = LocationManager.NETWORK_PROVIDER;
        }
        return locationProvider;
    }

    public boolean hasPermission() {
        return ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private static LocationProducer locationProducer;
    public static LocationProducer getInstance(Context context){
        if(locationProducer == null){
            locationProducer = new LocationProducer(context);
        }
        return locationProducer;
    }
}
