package com.bis.androidcarrc;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends ActionBarActivity {

    private final static String TAG = MainActivity.class.getSimpleName();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the default bluetooth adapter (Bluetooth radio)
        CarARCApp.bluetooth = BluetoothAdapter.getDefaultAdapter();

        // Check to see if bluetooth is enabled on the device and request that it be turned on if it's not
        if(!CarARCApp.bluetooth.isEnabled())
        {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 1);
        }
        // Or force enable bluetooth
        //bluetooth.enable();
//        Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//        startActivityForResult(enableBluetooth, 0);


    }

    public void showController(View v){
        // Show Controller activity

        Intent intent = new Intent(this, BasicController.class);
        startActivity(intent);
    }

    public void sendTest(View view){
        EditText cmdBox = (EditText)findViewById(R.id.cmdBox);
        TextView textarea = (TextView)findViewById(R.id.textView);
        String msg = cmdBox.getText().toString();
        textarea.append("\nSending..."+msg);
        Log.d(TAG,"\nSending..."+msg);

        byte[] msgBuffer = msg.getBytes();
        try{
            CarARCApp.outStream.write(msgBuffer);
        } catch (IOException e) {
            Log.d(TAG, "Unable to get in/out stream");
            e.printStackTrace();
        }
    }

    /*
        Open the connection and gets input and output streams
     */
    public void connect(View view) {
        final TextView textarea = (TextView)findViewById(R.id.textView);
        try {
            // Get a reference to our Arduino’s bluetooth device
            textarea.append("Searching for Device...\n");
            Set <BluetoothDevice>pairedDevices = CarARCApp.bluetooth.getBondedDevices(); //This will connect only with devices to which the phone has been paired before
            if(pairedDevices.size() > 0) {
                for(BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    if(device.getName().equals("WackyDriving")) // Change this to match the name of your device
                    {
                        textarea.append("Found Device.\n");
                        CarARCApp.remoteDevice = device;
                        break;
                    }
                }
            }


            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID
            // Create an RFCOMM BluetoothSocket ready to start
            // a secure outgoing connection to this remote device using SDP lookup of uuid.
            // As client


            textarea.append("Creating Socket...\n");
            CarARCApp.btSocket = CarARCApp.remoteDevice.createRfcommSocketToServiceRecord(uuid);

            textarea.append("Connecting...\n");
            CarARCApp.btSocket.connect();
            textarea.append("Connected.\n");
        } catch (IOException e) {

            textarea.append("Socket creation failed.\n");
            Log.d(TAG, "Socket creation failed");
            e.printStackTrace();
            try {
                CarARCApp.btSocket.close();
                textarea.append("Socket Closed\n");
            } catch (IOException e2) {
                Log.d(TAG, "Unable to close the connection");
            }
        }

        try {

            textarea.setTextSize(20);

            CarARCApp.outStream = CarARCApp.btSocket.getOutputStream();
            CarARCApp.inStream = CarARCApp.btSocket.getInputStream();

            Thread workerThread = new Thread(new Runnable()
            {
                public void run()
                {
                    while(!Thread.currentThread().isInterrupted() && !CarARCApp.stopWorker)
                    {
                        try
                        {
                            int bytesAvailable = CarARCApp.inStream.available();
                            if(bytesAvailable > 0)
                            {
                                byte[] packetBytes = new byte[bytesAvailable];
                                CarARCApp.inStream.read(packetBytes);
                                for(int i=0;i<bytesAvailable;i++)
                                {
                                    byte b = packetBytes[i];
                                    if(b == CarARCApp.delimiter)
                                    {
                                        byte[] encodedBytes = new byte[CarARCApp.readBufferPosition];
                                        System.arraycopy(CarARCApp.readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                        final String data = new String(encodedBytes, "US-ASCII");
                                        CarARCApp.readBufferPosition = 0;
                                        CarARCApp.handler.post(new Runnable()
                                        {
                                            public void run()
                                            {
                                                textarea.append("\nReceiving..." + data);
                                                Log.d(TAG, "\nReceiving..." + data);
                                            }
                                        });
                                    }
                                    else
                                    {
                                        CarARCApp.readBuffer[CarARCApp.readBufferPosition++] = b;
                                    }
                                }
                            }
                        }
                        catch (IOException ex)
                        {
                            CarARCApp.stopWorker = true;
                        }
                    }
                }
            });

            workerThread.start();

        } catch (IOException e) {
            Log.d(TAG, "Unable to get in/out stream");
            e.printStackTrace();
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            CarARCApp.btSocket.close();
        } catch (IOException e) {
            Log.d(TAG, "Unable to close properly the connection");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
