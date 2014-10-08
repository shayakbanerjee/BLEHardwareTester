package com.weartrons.bledevicetest;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by shayak on 10/1/14.
 */
public class DeviceTestHistory {
    private static DeviceTestHistory instance = new DeviceTestHistory();
    private static SharedPreferences sharedPrefs = null;
    private static final String pass = "Pass";
    private static final String fail = "Fail";
    private static final String devicesTested = "DevicesTested";

    private DeviceTestHistory() { }

    public static void setContext(Context context) {
        sharedPrefs = context.getSharedPreferences("com.weartrons.bledevicetest", Context.MODE_PRIVATE);
    }

    private static void incrementCount(String key) {
        int count = sharedPrefs.getInt(key, 0);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putInt(key, count+1);
        editor.apply();
    }

    private static Set<String> getTestedDevices() {
        return sharedPrefs.getStringSet(devicesTested, null);
    }

    public static void addTestedDevice(String mac) {
        Set<String> testedDevs = getTestedDevices();
        if (testedDevs == null) { testedDevs = new HashSet<String>(); }
        testedDevs.add(mac);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putStringSet(devicesTested, testedDevs);
        editor.apply();
    }

    public static void incrementPassCount(String key) {
        incrementCount(key+pass);
    }

    public static void incrementFailCount(String key) {
        incrementCount(key+fail);
    }

    public static int getPassCount(String key) {
        return sharedPrefs.getInt(key+pass, 0);
    }

    public static int getFailCount(String key) {
        return sharedPrefs.getInt(key+fail, 0);
    }

    public static int getNumberOfDevicesTested() {
        Set<String> testedDevices = getTestedDevices();
        return (testedDevices==null)? 0:testedDevices.size();
    }

}
