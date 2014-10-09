package com.weartrons.bledevicetest;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import java.util.UUID;

/**
 * Created by shayak on 10/6/14.
 */
public abstract class BLETest {
    private UUID readService = null;
    private UUID readCharacteristic = null;
    private UUID writeService = null;
    private UUID writeCharacteristic = null;
    public enum TestType {
        READ_ONLY,
        WRITE_ONLY,
        WRITE_THEN_READ
    }
    private TestType testType = TestType.READ_ONLY;
    private byte[] writeValue = null;
    private boolean testPassed = false;

    public BLETest(UUID sv, UUID ch, TestType tt) {
        testType = tt;
        if (tt == TestType.READ_ONLY) {
            readService = sv;
            readCharacteristic = ch;
            writeService = null;
            writeCharacteristic = null;
            Log.d(getClass().getName(), "Read service: "+readService.toString()+" char: "+readCharacteristic.toString());
        } else if (tt == TestType.WRITE_ONLY) {
            writeService = sv;
            writeCharacteristic = ch;
            readService = null;
            readCharacteristic = null;
            Log.d(getClass().getName(), "Write service: "+writeService.toString()+" char: "+writeCharacteristic.toString());
        } else if (tt == TestType.WRITE_THEN_READ) {
            Log.e(getClass().getName(), "Wrong constructor used. Must specify both read and write characteristics");
        }
    }

    public BLETest(UUID readSv, UUID readCh, UUID writeSv, UUID writeCh, TestType tt) {
        testType = tt;
        readService = readSv;
        readCharacteristic = readCh;
        writeService = writeSv;
        writeCharacteristic = writeCh;
    }

    public void setWriteValue(byte[] wV) { writeValue = wV; }

    public TestType getTestType() { return testType; }

    public void runTest(BluetoothGatt gatt) {
        if (testType == TestType.READ_ONLY) {
            Log.d(getClass().getName(), "Read service: "+readService.toString()+" char: "+readCharacteristic.toString());
            DeviceTestLog.writeEntry("Reading from: "+readService.toString()+" Char: "+readCharacteristic.toString());
            BluetoothGattCharacteristic ch = gatt.getService(readService).getCharacteristic(readCharacteristic);
            boolean status = gatt.readCharacteristic(ch);
        } else if (testType == TestType.WRITE_ONLY || testType == TestType.WRITE_THEN_READ) {
            if (writeValue == null) {
                Log.e(getClass().getName(), "Must specify write value for WRITE_ONLY test");
                return;
            }
            DeviceTestLog.writeEntry("Writing to: "+writeService.toString()+" Char: "+writeCharacteristic.toString());
            BluetoothGattCharacteristic ch = gatt.getService(writeService).getCharacteristic(writeCharacteristic);
            ch.setValue(writeValue);
            if (testType == TestType.WRITE_THEN_READ) {
                BluetoothGattCharacteristic rch = gatt.getService(readService).getCharacteristic(readCharacteristic);
                gatt.setCharacteristicNotification(rch, true);
            }
            boolean status = gatt.writeCharacteristic(ch);
        }
    }

    // Override this method to write your custom test on the value read
    protected abstract boolean didTestPass(String chValue);

    public boolean getTestStatus() { return testPassed; }

    private void testFinished() {
        DeviceTestLog.writeEntry("BLE Test completed");
        BroadcastKeys.sendGeneralBroadcast(BroadcastKeys.bleTestFinished);
    }

    public void setTestFailed() {
        testPassed = false;
        DeviceTestLog.writeEntry("BLE Test failed");
        testFinished();
    }

    public void compileResult(String readVal) {
        testPassed = didTestPass(readVal);
        DeviceTestLog.writeEntry("BLE Test passed: "+Boolean.toString(testPassed));
        testFinished();
    }
}
