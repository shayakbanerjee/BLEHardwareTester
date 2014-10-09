package com.weartrons.bledevicetest;

import android.content.Context;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.FileInputStream;

/**
 * Created by shayak on 10/7/14.
 */
public class DeviceTestLog {
    private static DeviceTestLog instance = new DeviceTestLog();
    private static String logFilename = "ble-test-log.txt";
    private static FileOutputStream outputStream;

    private DeviceTestLog() { }

    public static void openFile(Context context) {
        try {
            outputStream = context.openFileOutput(logFilename, Context.MODE_PRIVATE);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void writeEntry(String entry) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentTime = sdf.format(new Date());
            outputStream.write((currentTime+", ").getBytes());
            outputStream.write((entry+"\n").getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void closeFile() {
        try {
            outputStream.close();
        } catch (Exception e) { e. printStackTrace(); }
    }

    public static String getLogContents(Context context) {
        try
        {
            FileInputStream fis = context.openFileInput(logFilename);
            final StringBuffer fileContent = new StringBuffer();
            if (fis != null)
            {
                byte[] buffer = new byte[1024];
                int n;
                while ((n = fis.read(buffer)) != -1)
                {
                    fileContent.append(new String(buffer, 0, n));
                }
                fis.close();
                return fileContent.toString();
            }
        }
        catch (Exception e) {}
        return "";
    }

    public static void uploadToDropBox() {

    }
}
