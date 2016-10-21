package com.carelife.infogo.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.carelife.infogo.R;
import com.carelife.infogo.ui.adapters.DeviceAdapter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by chris on 16/10/20.
 */
public class BluetoothDetailFragment extends BaseInfoFragment implements View.OnClickListener{

    public  static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    private ListView pairedListview;
    private ListView availableListview;
    private List<BluetoothDevice> pairedList = new ArrayList<>();
    private List<BluetoothDevice> availableList = new ArrayList<>();
    private Button scanButton;
    private BluetoothAdapter bluetoothAdapter;
    private DeviceAdapter pairedAdapter;
    private DeviceAdapter availableAdapter;
    public  BluetoothSocket bluetoothSocket;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(!bluetoothAdapter.isEnabled()){
            bluetoothAdapter.enable();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bluetooth_page, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        availableListview = (ListView)view.findViewById(R.id.available_list);
        availableAdapter = new DeviceAdapter(getContext(), availableList);
        availableListview.setAdapter(availableAdapter);
        pairedListview = (ListView)view.findViewById(R.id.paired_list);
        pairedAdapter = new DeviceAdapter(getContext(), pairedList);
        pairedListview.setAdapter(pairedAdapter);
        scanButton = (Button)view.findViewById(R.id.scan_button);
        scanButton.setOnClickListener(this);
        registerBluetoothReceiver();
        getPairedDevices();
        scanDevices();

        pairedListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(bluetoothAdapter.isDiscovering())
                    bluetoothAdapter.cancelDiscovery();
                String address = pairedList.get(position).getAddress();
                BluetoothDevice btDev = bluetoothAdapter.getRemoteDevice(address);
                try {
                    connect(btDev);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        availableListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(bluetoothAdapter.isDiscovering())
                    bluetoothAdapter.cancelDiscovery();
                String address = availableList.get(position).getAddress();
                BluetoothDevice btDev = bluetoothAdapter.getRemoteDevice(address);
                try {
                    Boolean returnValue = false;
                    Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
                    returnValue = (Boolean) createBondMethod.invoke(btDev);
                    if(returnValue){
                        getPairedDevices();
                        scanDevices();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void connect(BluetoothDevice btDev) {
        UUID uuid = UUID.fromString(SPP_UUID);
        try {
            bluetoothSocket = btDev.createRfcommSocketToServiceRecord(uuid);
            bluetoothSocket.connect();
            if(bluetoothSocket.isConnected()){
                Toast.makeText(getContext(),"Connect successful",Toast.LENGTH_SHORT).show();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void registerBluetoothReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getContext().registerReceiver(receiver, filter);
    }

    @Override
    public void onClick(View view) {
        if(view == scanButton){
            scanDevices();
        }
    }

    private void getPairedDevices() {

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter
                .getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (Iterator<BluetoothDevice> it = pairedDevices.iterator(); it.hasNext();) {
                BluetoothDevice device = it.next();
                pairedList.add(device);
            }
        }
        pairedAdapter.notifyDataSetChanged();
    }

    private void scanDevices() {
        availableList.clear();
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED && !TextUtils.isEmpty(device.getName())) {
                    availableList.add(device);
                }
                availableAdapter.notifyDataSetChanged();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {


            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {

            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                switch (bluetoothAdapter.getState()) {
                    case BluetoothAdapter.STATE_ON:
                        break;

                    case BluetoothAdapter.STATE_OFF:

                        break;

                }
            }

        }

    };
}
