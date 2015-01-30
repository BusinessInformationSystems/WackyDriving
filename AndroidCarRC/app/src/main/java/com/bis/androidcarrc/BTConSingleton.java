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
public class BTConSingleton {

    private static BTConSingleton instance;


    public static BTConSingleton getInstance()
    {
        // Return the instance
        if (instance == null)
        {
            // Create the instance
            instance = new BTConSingleton();
        }
        return instance;
    }

    private BTConSingleton()
    {
        // Constructor hidden because this is a singleton
    }

    public BluetoothAdapter bluetooth; //Bluetooth radio adapter
    public BluetoothDevice remoteDevice; //remote bluetooth device
    public BluetoothSocket btSocket;
    public OutputStream outStream;
    public InputStream inStream;
    public boolean stopWorker = false;
    public byte delimiter = 10;
    public int readBufferPosition = 0;
    public byte[] readBuffer = new byte[1024];
    public Handler handler = new Handler();
}
