package com.weartrons.bledevicetest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by shayak on 10/8/14.
 */
public class ProtectoTestActivity extends DeviceTestActivity {

    @Override
    protected String setTitle() {
        return "PROTECTO";
    }

    @Override
    protected List<SingleTest> createTestList() {
        List<SingleTest> testList = new ArrayList<SingleTest>();
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

        testList.add(batteryTest);
        testList.add(buzzTest);
        return testList;
    }
}
