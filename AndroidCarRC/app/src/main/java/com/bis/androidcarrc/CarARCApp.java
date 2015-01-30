package com.bis.androidcarrc;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by derrickmilford on 30/01/15.
 */
public class CarARCApp extends Application {
    public static BluetoothAdapter bluetooth; //Bluetooth radio adapter
    public static BluetoothDevice remoteDevice; //remote bluetooth device
    public static BluetoothSocket btSocket;
    public static OutputStream outStream;
    public static InputStream inStream;
    public static boolean stopWorker = false;
    public static byte delimiter = 10;
    public static int readBufferPosition = 0;
    public static byte[] readBuffer = new byte[1024];
    public static Handler handler = new Handler();
}
