package com.weartrons.hammerheaddevicetest;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Context;
import android.util.Log;
import android.support.v4.content.LocalBroadcastManager;
import android.content.Intent;

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
    private DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    testPassed = true;
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    testPassed = false;
                    break;
            }
            testHasFinished(((Dialog)dialog).getContext());
        }
    };

    public SingleTest() {
        testDescription = "";
        testName = "";
        isInteractive = false;
        testHasRun = false;
    }

    public SingleTest(String name, String description, boolean interactive) {
        testName = name;
        testDescription = description;
        isInteractive = interactive;
        testHasRun = false;
    }

    public String getTestName() { return testName; }

    public boolean didTestPass() { return testPassed; }

    public boolean didTestRun() { return testHasRun; }

    public void runTest(Context context) {
        Log.d(getClass().getName(), "Running test: "+testName);
        if(!isInteractive) {
            testPassed = (new Random()).nextBoolean();
            testHasFinished(context);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(testDescription).setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        }
    }

    private void testHasFinished(Context context) {
        testHasRun = true;
        if (testPassed) { DeviceTestHistory.incrementPassCount(testName); }
        else { DeviceTestHistory.incrementFailCount(testName); }
        // Send broadcast to say test is finished
        Intent intent = new Intent(BroadcastKeys.masterMessage);
        intent.putExtra(BroadcastKeys.broadcastMessage, BroadcastKeys.testFinished);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
