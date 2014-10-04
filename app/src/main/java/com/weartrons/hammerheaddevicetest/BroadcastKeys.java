package com.weartrons.hammerheaddevicetest;

/**
 * Created by shayak on 10/4/14.
 */
public class BroadcastKeys {
    private static BroadcastKeys instance = new BroadcastKeys();
    public static String masterMessage = "hammerhead-master";
    public static String broadcastMessage = "broadcast-message";
    public static String testFinished = "test-finished";
    public static String scanFinished = "blescan-finished";

    private BroadcastKeys() { }

}
