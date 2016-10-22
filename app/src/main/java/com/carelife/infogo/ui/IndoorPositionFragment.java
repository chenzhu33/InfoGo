package com.carelife.infogo.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.carelife.infogo.R;
import com.carelife.infogo.dom.BaseInfo;
import com.carelife.infogo.dom.WifiLocationModel;
import com.carelife.infogo.dummy.DummyContent;
import com.carelife.infogo.utils.Tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A fragment representing a single Position detail screen.
 * This fragment is either contained in a {@link InfoListActivity}
 * in two-pane mode (on tablets) or a {@link InfoDetailActivity}
 * on handsets.
 */
public class IndoorPositionFragment extends BaseInfoFragment {

    private WifiManager wifiManager;
    private List<ScanResult> newWifList = new ArrayList<>();
    private double latitude;
    private double longitude;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wifiManager = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
        if(!wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(true);
        }
        regWifiReceiver();
        wifiManager.startScan();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.info_detail, container, false);
        return rootView;
    }

    private void regWifiReceiver() {
        IntentFilter labelIntentFilter = new IntentFilter();
        labelIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        labelIntentFilter.addAction("android.net.wifi.STATE_CHANGE");
        labelIntentFilter.setPriority(1000);
        getContext().registerReceiver(wifiResultChange, labelIntentFilter);

    }

    private final BroadcastReceiver wifiResultChange = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
                    || action.equals("android.net.wifi.STATE_CHANGE")) {
                newWifList = wifiManager.getScanResults();
                Collections.sort(newWifList, new Comparator<ScanResult>() {
                    @Override
                    public int compare(ScanResult o1, ScanResult o2) {
                        return o2.level - o1.level;
                    }
                });
                genIndoorPoint();
            }
        }
    };

    private void genIndoorPoint(){
        if(newWifList.size() <= 0){
            Toast.makeText(getContext(),"Can not find available wifi", Toast.LENGTH_SHORT).show();
        }else {
            List<WifiLocationModel> locationModelList = Tools.getWifiDatabase();
            for (WifiLocationModel model : locationModelList){
                String macAdd = newWifList.get(0).BSSID;
                if(macAdd.equals(model.getMacAddress())){
                    latitude = model.getLatitude();
                    longitude = model.getLongitude();
                    markOnMap(latitude, longitude);
                    Toast.makeText(getContext(),"Indoor position successfully", Toast.LENGTH_SHORT).show();
                }
            }
            Toast.makeText(getContext(),"Can not match AP in your database", Toast.LENGTH_SHORT).show();
        }
    }

    private void markOnMap(double latitude, double longitude){

    }
}
