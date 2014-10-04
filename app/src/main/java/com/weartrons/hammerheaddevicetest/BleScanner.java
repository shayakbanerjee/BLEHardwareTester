package com.weartrons.hammerheaddevicetest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

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

    public void scan(final Context context, int seconds) {
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
                // Send broadcast to say test is finished
                Intent intent = new Intent(BroadcastKeys.masterMessage);
                intent.putExtra(BroadcastKeys.broadcastMessage, BroadcastKeys.scanFinished);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        }, seconds * 1000);
    }
}
