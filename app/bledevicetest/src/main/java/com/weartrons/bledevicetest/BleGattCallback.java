package com.weartrons.bledevicetest;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import java.util.List;

/**
 * Created by shayak on 10/5/14.
 */
public class BleGattCallback extends BluetoothGattCallback {
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            gatt.discoverServices();
            BroadcastKeys.sendBLEBroadcast(BroadcastKeys.deviceConnected);
            gatt.readRemoteRssi();
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            BroadcastKeys.sendBLEBroadcast(BroadcastKeys.deviceDisconnected);
        }
    }

    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        if(status == BluetoothGatt.GATT_SUCCESS) {
            BroadcastKeys.sendBLEBroadcastRSSI(rssi);
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if(status==BluetoothGatt.GATT_SUCCESS) {
            for (BluetoothGattService gattService :gatt.getServices()) {
                List<BluetoothGattCharacteristic> gattCharacteristics =gattService.getCharacteristics();
                Log.d(getClass().getName(), "Service: " + gattService.getUuid().toString());
                DeviceTestLog.writeEntry("Service: " + gattService.getUuid().toString());
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    String uuid = gattCharacteristic.getUuid().toString();
                    Log.d(getClass().getName(), "Characteristics :" + uuid);
                    DeviceTestLog.writeEntry("Characteristic :" + uuid);
                }
            }
            BroadcastKeys.sendBLEBroadcast(BroadcastKeys.servicesDiscovered);
            gatt.readRemoteRssi();
        } else {
            // This is unhandled for now
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.d(getClass().getName(), "GATT read success");
            BroadcastKeys.sendBLEBroadcastValue(BroadcastKeys.characteristicRead, characteristic.getStringValue(0));
            gatt.readRemoteRssi();
        } else {
            BroadcastKeys.sendBLEBroadcast(BroadcastKeys.failedRead);
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.d(getClass().getName(), "GATT write success");
            BroadcastKeys.sendBLEBroadcast(BroadcastKeys.characteristicWritten);
            gatt.readRemoteRssi();
        } else {
            BroadcastKeys.sendBLEBroadcast(BroadcastKeys.failedWrite);
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
    }

}
