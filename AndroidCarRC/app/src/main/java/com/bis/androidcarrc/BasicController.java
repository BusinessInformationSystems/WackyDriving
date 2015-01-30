package com.bis.androidcarrc;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;

import java.io.IOException;


public class BasicController extends ActionBarActivity implements SeekBar.OnSeekBarChangeListener {

    private final static String TAG = BasicController.class.getSimpleName();

    private SeekBar speedBar;
    private SeekBar turningBar;
    private static int _motorMax = 255;
    private static int _max = 250;
    private static int _middle = _max/2;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_controller);

        speedBar = (SeekBar) findViewById(R.id.speedSeekBar);
        turningBar = (SeekBar) findViewById(R.id.turningSeekBar);

        speedBar.setMax(_max);
        turningBar.setMax(_max);

        speedBar.setProgress(_middle);
        turningBar.setProgress(_middle);

        speedBar.setOnSeekBarChangeListener(this);
        turningBar.setOnSeekBarChangeListener(this);
    }


    public void sendMsg(String msg){
        Log.d(TAG,"Sending "+msg);
        msg = msg + "\n";
        byte[] msgBuffer = msg.getBytes();
        try{
            BTConSingleton.getInstance().outStream.write(msgBuffer);
        } catch (IOException e) {
            Log.d(TAG, "Unable to get in/out stream");
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_basic_controller, menu);
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

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        if (seekBar == speedBar){
            int motorValue = calculateMotorValue(seekBar.getProgress());
            String msg = "MVF 0";
            if(motorValue > 0 && seekBar.getProgress() > _middle) {
                msg = "MVF " + motorValue;
            }else if(motorValue > 0 && seekBar.getProgress() < _middle){
                msg = "MVB " + motorValue;
            }

            sendMsg(msg);
        }else if(seekBar == turningBar){
            int motorValue = calculateMotorValue(seekBar.getProgress());
            String msg = "TWL 0";
            if(motorValue > 0 && seekBar.getProgress() > _middle) {
                msg = "TWR " + motorValue;
            }else if(motorValue > 0 && seekBar.getProgress() < _middle){
                msg = "TWL " + motorValue;
            }
            sendMsg(msg);
        }
    }

    private int calculateMotorValue(int progress) {
        if (progress>(_middle+25) || progress < (_middle -25)){
            if(progress < (_middle -25)){
                return _motorMax-progress;
            }else {
                return _motorMax-(_max-progress);
            }
        }else
            return 0;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (seekBar == speedBar){

        }else if(seekBar == turningBar){

        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (seekBar == speedBar){
            sendMsg("MVF 0");
        }else if(seekBar == turningBar){
            sendMsg("TWL 0");
        }
        seekBar.setProgress(_middle);
    }
}
