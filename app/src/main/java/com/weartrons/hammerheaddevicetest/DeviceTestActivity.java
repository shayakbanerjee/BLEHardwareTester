package com.weartrons.hammerheaddevicetest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.content.IntentFilter;
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


public class DeviceTestActivity extends Activity {
    private List<SingleTest> deviceTestList = new ArrayList<SingleTest>();
    private ListView deviceTestListView;
    private ArrayAdapter<SingleTest> deviceTestAdapter;
    private int runningTestNum = 0;
    private BleScanner bleScanner = new BleScanner();
    private BluetoothDevice selectedDevice = null;
    private ProgressDialog busyIndicator;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(BroadcastKeys.broadcastMessage);
            if (message.equals(BroadcastKeys.testFinished)) { redraw(); }
            else if (message.equals(BroadcastKeys.scanFinished)) {
                busyIndicator.dismiss();
                listFoundDevices();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_test);
        DeviceTestHistory.setContext(getApplicationContext());
        busyIndicator = new ProgressDialog(this);
        createTestList();
        ImageView titleLabel = (ImageView)findViewById(R.id.hammerheadLogo);
        titleLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { runNextTest(); }
        });
        deviceTestListView = (ListView)findViewById(R.id.deviceTestList);
        deviceTestAdapter = new DeviceTestAdapter(this, R.layout.device_test_adapter, deviceTestList);
        deviceTestListView.setAdapter(deviceTestAdapter);
        Button searchButton = (Button)findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { findDevices(); }
        });
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter(BroadcastKeys.masterMessage));
        redraw();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.device_test, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createTestList() {
        deviceTestList.add(new SingleTest("Accelerometer", "Is accelerometer working?", false));
        deviceTestList.add(new SingleTest("Pressure", "Is pressure sensor working?", false));
        deviceTestList.add(new SingleTest("Fuel Gauge", "Is fuel gauge working?", false));
        deviceTestList.add(new SingleTest("Light Sensor", "Is light sensor working?", false));
        deviceTestList.add(new SingleTest("Headlights", "Are headlights working?", true));
        deviceTestList.add(new SingleTest("Red", "Are red LEDs working?", true));
        deviceTestList.add(new SingleTest("Green", "Are green LEDs working?", true));
        deviceTestList.add(new SingleTest("Blue", "Are blue LEDs working?", true));
    }

    private void runNextTest() {
        if(runningTestNum+1 > deviceTestList.size()) { return; }
        SingleTest deviceTest = deviceTestList.get(runningTestNum);
        deviceTest.runTest(this);
        runningTestNum++;
    }

    public void redraw() {
        TextView numberOfTests = (TextView)findViewById(R.id.testsRunLabel);
        numberOfTests.setText(Integer.toString(runningTestNum));
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
        if (selectedDevice != null) {
            TextView deviceAddress = (TextView)findViewById(R.id.deviceIDLabel);
            deviceAddress.setText(selectedDevice.getAddress());
        }
        deviceTestAdapter.notifyDataSetChanged();
    }

    private void findDevices() {
        busyIndicator.setTitle("Scanning for BLE Devices");
        busyIndicator.setMessage("Please wait while scanning...");
        busyIndicator.show();
        bleScanner.scan(this, 5);
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
                selectedDevice = bleScanner.getDeviceList().get(which);
                redraw();
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
