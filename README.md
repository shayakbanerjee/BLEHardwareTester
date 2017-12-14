# Bluetooth Low Energy (BLE) Hardware Tester

This is an Android library for quickly creating a set of BLE device tests for end-of-line testing. 

## Code Organization
The library is contained in the folder 'bledevicetest', while the wrapper around it is in the form of an example app.

## Code Samples
BLE Tests are generically specified with a test name, description and a set of BLE commands to send (reads or writes). It also requires defining a test criterion - which determines whether the test passed or failed based on the output obtained from the BLE device.
Here's an example:
```java
BLETest bTest = new BLETest(UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb"),
                UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb"),
                BLETest.TestType.READ_ONLY) {
                  @Override
                  protected boolean didTestPass(String chValue) {
                      byte[] b = chValue.getBytes();
                      int val = b[0] & 0xff;
                      if (val > 0 && val <= 100) { return true; }
                      return false;
                }
        }
```

For UI purposes, a class `SingleTest` has also been defined which needs to be initialized with a name, description and whether it is interactive i.e. requires user input to confirm that the test passed.

```java
SingleTest batteryTest = new SingleTest("Battery", "Is battery in range?", false);
batteryTest.setBleTest(bTest);
```

The library displays a view which lists pass/fail of tests for a single device. It also maintains a count of failures across tests.

## The Example App - Protecto
An example app has been put together here (see `ProtectoTestActivity`). It extends the `DeviceTestActivity` class (defined in the library) to define 2 sample tests:
1. Did a buzzer on the device beep when the command was sent?
1. Was the battery level obtainable from the BLE device, and was the value between 0 - 100?
