package com.carelife.infogo.utils;

import com.carelife.infogo.dom.WifiLocationModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangrh on 2016/10/21.
 */
public class Tools {

    private static final double EARTH_RADIUS = 6378137.0;
    public static final String fileFolderPath = "/sdcard/wifiLocation/";

    public static double getDistance(double latitude1, double longitude1,
                                     double latitude2, double longitude2) {
        double Lat1 = rad(latitude1);
        double Lat2 = rad(latitude2);
        double a = Lat1 - Lat2;
        double b = rad(longitude1) - rad(longitude2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(Lat1) * Math.cos(Lat2)
                * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        return s;
    }
    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    public static List<WifiLocationModel> getWifiDatabase(){
        List<WifiLocationModel> wifiLocationModels = new ArrayList<>();
        try {
            File file = new File(fileFolderPath + "wifiLocation.dat");
            if (!file.getParentFile().exists()) {
                return wifiLocationModels;
            }
            if (!file.exists()) {
                return wifiLocationModels;
            }
            FileInputStream fileInputStream = new FileInputStream(file);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            wifiLocationModels = (List<WifiLocationModel>)objectInputStream.readObject();
            return wifiLocationModels;
        } catch (Exception e) {
            e.printStackTrace();
            return wifiLocationModels;
        }
    }
}
