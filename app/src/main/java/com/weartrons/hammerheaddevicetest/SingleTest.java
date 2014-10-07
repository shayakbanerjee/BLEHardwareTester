package com.weartrons.hammerheaddevicetest;

import android.app.AlertDialog;
import android.bluetooth.BluetoothGatt;
import android.content.DialogInterface;
import android.content.Context;

import java.util.Random;

/**
 * Created by shayak on 10/2/14.
 */
public class SingleTest {
    private String testDescription = null; // This is the question asked on dialog
    private String testName = null;
    private boolean isInteractive = false;
    private boolean testHasRun = false;
    private boolean testPassed;
    private BLETest bleTest = null;

    private DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                testPassed = true;
                testHasFinished();
            } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                testPassed = false;
                testHasFinished();
            }
        }
    };

    public SingleTest(String name, String description, boolean interactive) {
        testName = name;
        testDescription = description;
        isInteractive = interactive;
        testHasRun = false;
    }

    public String getTestName() { return testName; }

    public void setBleTest(BLETest b) { bleTest = b; }

    public boolean didTestPass() { return testPassed; }

    public boolean didTestRun() { return testHasRun; }

    public void runTest(BluetoothGatt gatt) { bleTest.runTest(gatt); }

    public void compileTestResult(Context context) {
        if(!isInteractive) {
            testPassed = bleTest.getTestStatus();
            //testPassed = (new Random()).nextBoolean();   // This is to fake out the BLE test
            testHasFinished();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(testDescription).setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        }
    }

    private void testHasFinished() {
        testHasRun = true;
        if (testPassed) { DeviceTestHistory.incrementPassCount(testName); }
        else { DeviceTestHistory.incrementFailCount(testName); }
        BroadcastKeys.sendGeneralBroadcast(BroadcastKeys.testFinished);
    }

    public void setBleTestFailed() { bleTest.setTestFailed(); }

    public void compileBleTestResult(String readVal) { bleTest.compileResult(readVal); }

    public BLETest.TestType getBleTestType() { return bleTest.getTestType(); }
}
