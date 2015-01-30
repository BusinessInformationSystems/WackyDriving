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

    private BluetoothAdapter bluetooth; //Bluetooth radio adapter
    private BluetoothDevice remoteDevice; //remote bluetooth device
    private BluetoothSocket btSocket;
    private OutputStream outStream;
    private InputStream inStream;
    private boolean stopWorker = false;
    private byte delimiter = 10;
    private int readBufferPosition = 0;
    private byte[] readBuffer = new byte[1024];
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the default bluetooth adapter (Bluetooth radio)
        bluetooth = BluetoothAdapter.getDefaultAdapter();

        // Check to see if bluetooth is enabled on the device and request that it be turned on if it's not
        if(!bluetooth.isEnabled())
        {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 1);
        }
        // Or force enable bluetooth
        //bluetooth.enable();
//        Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//        startActivityForResult(enableBluetooth, 0);

        // Get a reference to our Arduino’s bluetooth device
        Set <BluetoothDevice>pairedDevices = bluetooth.getBondedDevices(); //This will connect only with devices to which the phone has been paired before
        if(pairedDevices.size() > 0) {
            for(BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                if(device.getName().equals("WackyDriving")) // Change this to match the name of your device
                {
                    remoteDevice = device;
                    break;
                }
            }
        }
    }

    public void showController(View v){
        // Show Controller activity

        Intent intent = new Intent(this, ControllerActivity.class);
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
            outStream.write(msgBuffer);
        } catch (IOException e) {
            Log.d(TAG, "Unable to get in/out stream");
            e.printStackTrace();
        }
    }

    /*
        Open the connection and gets input and output streams
     */
    public void connect(View view) {
        try {
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID
            // Create an RFCOMM BluetoothSocket ready to start
            // a secure outgoing connection to this remote device using SDP lookup of uuid.
            // As client
            btSocket = remoteDevice.createRfcommSocketToServiceRecord(uuid);
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                Log.d(TAG, "Unable to close the connection");
            }
            Log.d(TAG, "Socket creation failed");
            e.printStackTrace();
        }

        try {
            final TextView textarea = (TextView)findViewById(R.id.textView);
            textarea.setTextSize(20);

            outStream = btSocket.getOutputStream();
            inStream = btSocket.getInputStream();

            Thread workerThread = new Thread(new Runnable()
            {
                public void run()
                {
                    while(!Thread.currentThread().isInterrupted() && !stopWorker)
                    {
                        try
                        {
                            int bytesAvailable = inStream.available();
                            if(bytesAvailable > 0)
                            {
                                byte[] packetBytes = new byte[bytesAvailable];
                                inStream.read(packetBytes);
                                for(int i=0;i<bytesAvailable;i++)
                                {
                                    byte b = packetBytes[i];
                                    if(b == delimiter)
                                    {
                                        byte[] encodedBytes = new byte[readBufferPosition];
                                        System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                        final String data = new String(encodedBytes, "US-ASCII");
                                        readBufferPosition = 0;
                                        handler.post(new Runnable()
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
                                        readBuffer[readBufferPosition++] = b;
                                    }
                                }
                            }
                        }
                        catch (IOException ex)
                        {
                            stopWorker = true;
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
            btSocket.close();
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
