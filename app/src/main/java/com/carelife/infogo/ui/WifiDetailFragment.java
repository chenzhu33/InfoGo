package com.carelife.infogo.ui;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.carelife.infogo.R;
import com.carelife.infogo.dom.WifiModel;
import com.carelife.infogo.ui.adapters.WifiListAdapter;
import com.carelife.infogo.utils.LinkWifi;
import com.carelife.infogo.utils.LocationProducer;

import java.util.ArrayList;
import java.util.List;

public class WifiDetailFragment extends BaseInfoFragment implements View.OnClickListener{

    private WifiManager wifiManager;
    private ListView wifiListView;
    private LinkWifi linkWifi;
    public SetWifiHandler setWifiHandler;
    private List<ScanResult> newWifList = new ArrayList<>();
    private WifiListAdapter wifiListAdapter;
    private Button scanButton;
    private Button saveButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wifiManager = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
        if(!wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(true);
        }
        linkWifi = new LinkWifi(getContext());
        setWifiHandler = new SetWifiHandler(Looper.getMainLooper());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.wifi_page, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        wifiListView = (ListView) view.findViewById(R.id.wifi_list);
        scanButton = (Button) view.findViewById(R.id.scan_button);
        scanButton.setOnClickListener(this);
        saveButton = (Button)view.findViewById(R.id.save_button);
        saveButton.setOnClickListener(this);
        wifiListAdapter = new WifiListAdapter(getContext(), newWifList, setWifiHandler);
        wifiListView.setAdapter(wifiListAdapter);
        regWifiReceiver();
        wifiManager.startScan();
    }

    @Override
    public void onClick(View view) {
        if(view == scanButton){
            if(wifiManager != null) {
                wifiListAdapter.clear();
                wifiManager.startScan();
            }
        }else if(view == saveButton){
            if(newWifList.size() > 0){
                saveToDb(newWifList);
                Toast.makeText(getContext(),"Save successfully",Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(getContext(),"no data",Toast.LENGTH_SHORT).show();
            }
        }
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
                showWifiList();
            }
        }
    };

    private void showWifiList() {
        List<ScanResult> wifiList = wifiManager.getScanResults();

        boolean isAdd;

        if (wifiList != null) {
            for (int i = 0; i < wifiList.size(); i++) {
                isAdd = true;
                for (int j = 0; j < newWifList.size(); j++) {
                    if (newWifList.get(j).SSID.equals(wifiList.get(i).SSID)) {
                        isAdd = false;
                        if (newWifList.get(j).level < wifiList.get(i).level) {
                            newWifList.remove(j);
                            newWifList.add(wifiList.get(i));
                            break;
                        }
                    }
                }
                if (isAdd)
                    newWifList.add(wifiList.get(i));
            }
        }

        int connectedId = wifiManager.getConnectionInfo().getNetworkId();
        ScanResult scanResult = null;
        for(ScanResult result : newWifList){
            if(linkWifi.IsExsits(result.SSID) != null) {
                if (connectedId == linkWifi.IsExsits(result.SSID).networkId)
                    scanResult = result;
            }
        }
        if(scanResult !=null )
            moveConnectedToFirst(scanResult);


    }

    private void saveToDb(List<ScanResult> list){
        StringBuilder sb = new StringBuilder();
        for (ScanResult scanResult : list){
            sb.append(scanResult.BSSID)
                    .append("&&")
                    .append(scanResult.SSID)
                    .append("&&")
                    .append(scanResult.level)
                    .append("&&")
                    .append(scanResult.capabilities)
                    .append("|");
        }
        WifiModel model = new WifiModel();
        model.setData(sb.toString());
        model.setTimestamp(System.currentTimeMillis());
        model.setLatitude(LocationProducer.getInstance(getContext()).getLastKnowLocation().getLatitude());
        model.setLongitude(LocationProducer.getInstance(getContext()).getLastKnowLocation().getLongitude());
        model.save();
    }

    private void moveConnectedToFirst(ScanResult result) {
        newWifList.remove(result);
        newWifList.add(0, result);
        wifiListAdapter.notifyDataSetChanged();
    }

    private void configWifiRelay(final ScanResult wifiInfo) {

        if (linkWifi.IsExsits(wifiInfo.SSID) != null) {
            final int netID = linkWifi.IsExsits(wifiInfo.SSID).networkId;

            String actionStr;
            if (wifiManager.getConnectionInfo().getNetworkId() == netID) {
                actionStr = "disconnect";
            } else {
                actionStr = "connect";
            }

            new AlertDialog.Builder(getContext())
                    .setTitle("Prompt")
                    .setMessage("Please select your action")
                    .setPositiveButton(actionStr,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {

                                    if (wifiManager.getConnectionInfo().getNetworkId() == netID) {
                                        wifiManager.disconnect();
                                    } else {
                                        WifiConfiguration config = linkWifi.IsExsits(wifiInfo.SSID);
                                        linkWifi.setMaxPriority(config);
                                        boolean status = linkWifi.ConnectToNetID(config.networkId);
                                        if(status)
                                            moveConnectedToFirst(wifiInfo);
                                    }

                                }
                            })
                    .setNeutralButton("forget",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    wifiManager.removeNetwork(netID);
                                }
                            })
                    .setNegativeButton("cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.dismiss();
                                }
                            }).show();

            return;
        }

        String capabilities;

        if (wifiInfo.capabilities.contains("WPA2-PSK")) {
            capabilities = "psk2";
        } else if (wifiInfo.capabilities.contains("WPA-PSK")) {
            capabilities = "psk";
        } else if (wifiInfo.capabilities.contains("WPA-EAP")) {
            capabilities = "eap";
        } else if (wifiInfo.capabilities.contains("WEP")) {
            capabilities = "wep";
        } else {
            capabilities = "";
        }

        if (!capabilities.equals("")) {

            LayoutInflater factory = LayoutInflater.from(getContext());
            final View inputPwdView = factory.inflate(R.layout.wifi_input_dialog,null);
            new AlertDialog.Builder(getContext())
                    .setTitle("Please input the password")
                    .setMessage("SSID： " + wifiInfo.SSID)
                    .setView(inputPwdView)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    EditText pwd = (EditText) inputPwdView
                                            .findViewById(R.id.dialog_password);
                                    String wifipwd = pwd.getText().toString();
                                    int netID = linkWifi.CreateWifiInfo2(wifiInfo, wifipwd);
                                    boolean status = linkWifi.ConnectToNetID(netID);
                                    if(status)
                                        moveConnectedToFirst(wifiInfo);
                                }
                            })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).setCancelable(false).show();

        } else {
            new AlertDialog.Builder(getContext())
                    .setTitle("Prompt")
                    .setMessage("Connection to No Password Wifi may have a risk，continue? ")
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    int netID = linkWifi.CreateWifiInfo2(wifiInfo, "");
                                    boolean status = linkWifi.ConnectToNetID(netID);
                                    if(status)
                                        moveConnectedToFirst(wifiInfo);
                                }
                            })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int whichButton) {
                                }
                            }).show();

        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContext().unregisterReceiver(wifiResultChange);
    }

    public class SetWifiHandler extends Handler {
        public SetWifiHandler(Looper mainLooper) {
            super(mainLooper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    ScanResult wifiInfo = (ScanResult) msg.obj;
                    configWifiRelay(wifiInfo);
                    break;

            }
        }
    }
}
