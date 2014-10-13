package com.weartrons.bledevicetest;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.content.Context;

/**
 * Created by shayak on 10/4/14.
 */
public class BroadcastKeys {
    private static BroadcastKeys instance = new BroadcastKeys();
    public static String masterMessage = "hammerhead-master";
    public static String generalMessage = "general-message";
    public static String testFinished = "test-finished";
    public static String bleTestFinished = "ble-test-finished";

    public static String bleMessage = "ble-message";
    public static String scanFinished = "blescan-finished";
    public static String deviceConnected = "device-connected";
    public static String deviceDisconnected = "device-disconnected";
    public static String servicesDiscovered = "ble-services-discovered";
    public static String rssiRead = "rssi-read";
    public static String characteristicRead = "characteristic-read";
    public static String characteristicWritten = "characteristic-written";
    public static String characteristicChanged = "characteristic-changed";
    public static String failedRead = "characteristic-read-failed";
    public static String failedWrite = "characeristic-write-failed";

    public static String chValueMessage = "characteristic-value-message";
    public static String rssiValueMessage = "rssi-value-message";
    public static LocalBroadcastManager localBroadcastManager = null;

    private BroadcastKeys() { }

    public static void setContext(Context context) {
        localBroadcastManager = LocalBroadcastManager.getInstance(context);
    }

    private static void sendLocalBroadcast(String filter, String message) {
        Intent intent = new Intent(masterMessage);
        intent.putExtra(filter, message);
        localBroadcastManager.sendBroadcast(intent);
    }

    public static void sendGeneralBroadcast(String message) { sendLocalBroadcast(generalMessage, message); }

    public static void sendBLEBroadcast(String message) {
        sendLocalBroadcast(bleMessage, message);
    }

    public static void sendBLEBroadcastValue(String message, String chValue) {
        Intent intent = new Intent(masterMessage);
        intent.putExtra(bleMessage, message);
        intent.putExtra(chValueMessage, chValue);
        localBroadcastManager.sendBroadcast(intent);
    }

    public static void sendBLEBroadcastRSSI(int rssi) {
        Intent intent = new Intent(masterMessage);
        intent.putExtra(bleMessage, rssiRead);
        intent.putExtra(rssiValueMessage, Integer.toString(rssi));
        localBroadcastManager.sendBroadcast(intent);
    }

}
