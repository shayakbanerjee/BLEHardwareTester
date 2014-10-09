package com.weartrons.bledevicetest;

import java.util.List;

/**
 * Created by shayak on 10/4/14.
 */
public class DeviceTestListHelpers {
    private static DeviceTestListHelpers instance = new DeviceTestListHelpers();

    private DeviceTestListHelpers() { }


    public static int totalTestsRun(List<SingleTest> deviceTestList) {
        int totalTestsRun = 0;
        for (SingleTest dTest: deviceTestList) {
            totalTestsRun += (dTest.didTestRun())?1:0;
        }
        return totalTestsRun;
    }

    public static int totalTestsPassed(List<SingleTest> deviceTestList) {
        int totalTestsPassed = 0;
        for (SingleTest dTest: deviceTestList) {
            if(!dTest.didTestRun()) { continue; }
            totalTestsPassed += (dTest.didTestPass())?1:0;
        }
        return totalTestsPassed;
    }

    public static int totalTestsFailed(List<SingleTest> deviceTestList) {
        return totalTestsRun(deviceTestList) - totalTestsPassed(deviceTestList);
    }
}
