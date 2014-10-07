package com.weartrons.hammerheaddevicetest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shayak on 10/4/14.
 */
public class BleScanner {
    private List<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>();
    private BluetoothAdapter bAdapter;

    public BleScanner() {
        bAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    private BluetoothAdapter.LeScanCallback mScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if(!deviceList.contains(device)) { deviceList.add(device); }
        }
    };

    public List<BluetoothDevice> getDeviceList() { return deviceList; }

    public void scan(int seconds) {
        deviceList.clear();
        Handler mHandler = new Handler();
        mHandler.post(new Runnable() {
            @Override
            public void run() { bAdapter.startLeScan(mScanCallback); }
        });
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                bAdapter.stopLeScan(mScanCallback);
                BroadcastKeys.sendBLEBroadcast(BroadcastKeys.scanFinished);
            }
        }, seconds * 1000);
    }
}
