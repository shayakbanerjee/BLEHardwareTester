package com.weartrons.hammerheaddevicetest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class DeviceTestActivity extends Activity {
    private List<SingleTest> deviceTestList = new ArrayList<SingleTest>();
    private ListView deviceTestListView;
    private ArrayAdapter<SingleTest> deviceTestAdapter;
    private int runningTestNum = 0;
    private BleScanner bleScanner = new BleScanner();
    private BluetoothDevice deviceUnderTest = null;
    private BluetoothGatt connectedGatt = null;
    private BleGattCallback mGattCallback = new BleGattCallback();
    private ProgressDialog busyIndicator;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String gMessage = intent.getStringExtra(BroadcastKeys.generalMessage);
            if (gMessage!=null) {
                if (gMessage.equals(BroadcastKeys.testFinished)) {
                    redraw();
                    runningTestNum++;
                    runNextTest();
                }
                else if (gMessage.equals(BroadcastKeys.bleTestFinished)) {
                    deviceTestList.get(runningTestNum).compileTestResult(DeviceTestActivity.this);
                }
            }
            String bleMessage = intent.getStringExtra(BroadcastKeys.bleMessage);
            if (bleMessage != null) {
                SingleTest runningTest = (runningTestNum < deviceTestList.size())? deviceTestList.get(runningTestNum):null;
                if (bleMessage.equals(BroadcastKeys.scanFinished)) {
                    busyIndicator.dismiss();
                    listFoundDevices();
                } else if (bleMessage.equals(BroadcastKeys.deviceConnected)) {
                    TextView connectionState = (TextView)findViewById(R.id.deviceConnectedLabel);
                    DeviceTestLog.writeEntry("Device Connected");
                    connectionState.setText("Device Connected");
                    connectionState.setTextColor(Color.GREEN);
                } else if (bleMessage.equals(BroadcastKeys.deviceDisconnected)) {
                    TextView connectionState = (TextView)findViewById(R.id.deviceConnectedLabel);
                    connectionState.setText("Device Disconnected");
                    connectionState.setTextColor(Color.RED);
                    TextView rssiLabel = (TextView)findViewById(R.id.rssiLabel);
                    rssiLabel.setText("--");
                    connectedGatt = null;
                } else if (bleMessage.equals(BroadcastKeys.failedWrite) || bleMessage.equals(BroadcastKeys.failedRead)) {
                    runningTest.setBleTestFailed();
                    DeviceTestLog.writeEntry("Write or read failed");
                } else if (bleMessage.equals(BroadcastKeys.characteristicWritten) &&
                        runningTest.getBleTestType() == BLETest.TestType.WRITE_ONLY) {
                    runningTest.compileBleTestResult(null);
                } else if (bleMessage.equals(BroadcastKeys.characteristicRead) &&
                        (runningTest.getBleTestType() == BLETest.TestType.READ_ONLY ||
                        runningTest.getBleTestType() == BLETest.TestType.WRITE_THEN_READ)) {
                    String chValue = intent.getStringExtra(BroadcastKeys.chValueMessage);
                    runningTest.compileBleTestResult(chValue);
                    DeviceTestLog.writeEntry("Reading value from characteristic");
                } else if (bleMessage.equals(BroadcastKeys.rssiRead)) {
                    String rssiVal = intent.getStringExtra(BroadcastKeys.rssiValueMessage);
                    TextView rssiLabel = (TextView)findViewById(R.id.rssiLabel);
                    rssiLabel.setText(rssiVal);
                    DeviceTestLog.writeEntry("Read RSSI: " + rssiVal);
                } else if (bleMessage.equals(BroadcastKeys.servicesDiscovered)) {
                    runNextTest();
                    DeviceTestLog.writeEntry("Discovered device services");
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_test);
        DeviceTestHistory.setContext(getApplicationContext());
        BroadcastKeys.setContext(getApplicationContext());
        busyIndicator = new ProgressDialog(this);
        createTestList();
        /*ImageView titleLabel = (ImageView)findViewById(R.id.hammerheadLogo);
        titleLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { runNextTest(); }
        });*/
        deviceTestListView = (ListView)findViewById(R.id.deviceTestList);
        deviceTestAdapter = new DeviceTestAdapter(this, R.layout.device_test_adapter, deviceTestList);
        deviceTestListView.setAdapter(deviceTestAdapter);
        Button searchButton = (Button)findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearTestList();
                findDevices();
            }
        });
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter(BroadcastKeys.masterMessage));
        redraw();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.device_test, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createTestList() {
        SingleTest batteryTest = new SingleTest("Battery", "Is battery in range?", false);
        batteryTest.setBleTest(new BLETest(UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb"),
                UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb"),
                BLETest.TestType.READ_ONLY) {
            @Override
            protected boolean didTestPass(String chValue) {
                byte[] b = chValue.getBytes();
                int val = b[0] & 0xff;
                if (val > 0 && val <= 100) { return true; }
                return false;
            }
        });

        SingleTest buzzTest = new SingleTest("Beep Device", "Did device beep?", true);
        BLETest buzzBle = new BLETest(UUID.fromString("00001803-0000-1000-8000-00805f9b34fb"),
                UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb"),
                BLETest.TestType.WRITE_ONLY) {
            @Override
            protected boolean didTestPass(String chValue) {
                return true;
            }
        };
        buzzBle.setWriteValue(new byte[] {(byte) 1});
        buzzTest.setBleTest(buzzBle);

        deviceTestList.add(batteryTest);
        deviceTestList.add(buzzTest);

        /*deviceTestList.add(new SingleTest("Accelerometer", "Is accelerometer working?", false));
        deviceTestList.add(new SingleTest("Pressure", "Is pressure sensor working?", false));
        deviceTestList.add(new SingleTest("Fuel Gauge", "Is fuel gauge working?", false));
        deviceTestList.add(new SingleTest("Light Sensor", "Is light sensor working?", false));
        deviceTestList.add(new SingleTest("Headlights", "Are headlights working?", true));
        deviceTestList.add(new SingleTest("Red", "Are red LEDs working?", true));
        deviceTestList.add(new SingleTest("Green", "Are green LEDs working?", true));
        deviceTestList.add(new SingleTest("Blue", "Are blue LEDs working?", true));*/
    }

    private void runNextTest() {
        if (connectedGatt == null) { return; } // No test if device not connected
        if(runningTestNum+1 > deviceTestList.size()) { // Tests are completed
            finishDeviceTesting();
            return;
        }
        // More tests to run otherwise
        deviceTestList.get(runningTestNum).runTest(connectedGatt);
    }

    private void finishDeviceTesting() {
        if(deviceUnderTest != null) {
            DeviceTestHistory.addTestedDevice(deviceUnderTest.getAddress());
            connectedGatt.disconnect();
            DeviceTestLog.writeEntry("Device disconnected");
            DeviceTestLog.closeFile(); // Also need to upload to Dropbox here
            /* Test print the file to see if log was written */
            Log.d(getClass().getName(), "File Contents: "+DeviceTestLog.getLogContents(getApplicationContext()));
        }
    }

    private void clearTestList() {
        deviceTestList.clear();
        createTestList();
        redraw();
    }

    public void redraw() {
        TextView numberOfTests = (TextView)findViewById(R.id.testsRunLabel);
        numberOfTests.setText(Integer.toString(DeviceTestHistory.getNumberOfDevicesTested()));
        TextView totalPassedView = (TextView)findViewById(R.id.totalPassedCount);
        int totalPassed = DeviceTestListHelpers.totalTestsPassed(deviceTestList);
        int totalFailed = DeviceTestListHelpers.totalTestsFailed(deviceTestList);
        totalPassedView.setText(Integer.toString(totalPassed));
        TextView totalFailedView = (TextView)findViewById(R.id.totalFailedCount);
        totalFailedView.setText(Integer.toString(totalFailed));
        if(totalPassed + totalFailed > 0) {
            TextView yieldView = (TextView)findViewById(R.id.yieldCount);
            int yield = (int)((float)totalPassed/(float)(totalPassed+totalFailed)*100);
            yieldView.setText(Integer.toString(yield)+"%");
        }
        TextView deviceAddress = (TextView)findViewById(R.id.deviceIDLabel);
        deviceAddress.setText((deviceUnderTest==null)?"00:00:00:00:00:00":deviceUnderTest.getAddress());
        deviceTestAdapter.notifyDataSetChanged();
    }

    private void findDevices() {
        busyIndicator.setTitle("Scanning for BLE Devices");
        busyIndicator.setMessage("Please wait while scanning...");
        busyIndicator.show();
        bleScanner.scan(5);
    }

    private void listFoundDevices() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setIcon(R.drawable.ic_launcher);
        builderSingle.setTitle("Select a Device");
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item);
        for (BluetoothDevice d:bleScanner.getDeviceList()) {
            arrayAdapter.add(d.getName()+" ("+d.getAddress()+")");
        }
        builderSingle.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deviceUnderTest = bleScanner.getDeviceList().get(which);
                connectedGatt = deviceUnderTest.connectGatt(DeviceTestActivity.this, false, mGattCallback);
                runningTestNum = 0;
                redraw();
                DeviceTestLog.openFile(getApplicationContext());
                dialog.dismiss();  // For now, just dismiss list on click
            }
        });
        builderSingle.show();
    }

    @Override
    protected void onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }
}