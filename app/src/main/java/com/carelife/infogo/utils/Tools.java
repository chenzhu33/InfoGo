package com.carelife.infogo.utils;

import com.carelife.infogo.dom.WifiLocationModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

public class Tools {

    private static final double EARTH_RADIUS = 6378137.0;
    public static final String fileFolderPath = "/sdcard/";

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
        List<WifiLocationModel> list = new ArrayList<>();
        String content = readSDFile().trim();
        String [] models = content.split("\\|");
        for (int i=0;i<models.length;i++){
            String modelString = models[i];
            String [] datas = modelString.split("&&");
            WifiLocationModel model = new WifiLocationModel();
            model.setAddress(datas[0]);
            model.setMacAddress(datas[1]);
            model.setLatitude(Double.parseDouble(datas[2]));
            model.setLongitude(Double.parseDouble(datas[3]));
            list.add(model);
        }
        return list;
    }

    private static String readSDFile(){
        String content = "";
        File file = new File(fileFolderPath + "wifiLocation.txt");
        if(!file.exists())
            return "";
        try {
            InputStream inputStream = new FileInputStream(file);
            InputStreamReader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            while (( line = bufferedReader.readLine()) != null) {
                content += line;
            }
            inputStream.close();
            reader.close();
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content;
    }
}
