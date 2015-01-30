package com.bis.androidcarrc;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;

import java.io.IOException;

/**
 * Created by derrickmilford on 29/01/15.
 */
public class AdvancedController extends Activity implements View.OnTouchListener{

    private final static String TAG = AdvancedController.class.getSimpleName();

    private View touchView;
    private static int _motorMax = 255;

    private float originX = 0;
    private float originY = 0;

    private boolean isTouching = false;
    private ImageView padBGImageView;
    private ImageView padThumbImageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);
        touchView = findViewById(R.id.touchView);
        touchView.setOnTouchListener(this);
        padBGImageView = (ImageView) findViewById(R.id.padBGImageView);
        padBGImageView.setVisibility(View.INVISIBLE);
        padThumbImageView = (ImageView) findViewById(R.id.padThumbImageView);
        padThumbImageView.setVisibility(View.INVISIBLE);
    }

    public int convertPxToDp(float pixel){
        float scale = getResources().getDisplayMetrics().density;
        int returnValue = (int) (pixel / scale);
        return returnValue;
    }

    public void moveImageToPoint(int id, float xPixel, float yPixel){
        ImageView imageToMove = (ImageView) findViewById(id);
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) imageToMove.getLayoutParams();
        lp.setMargins(((int)xPixel - (lp.width/2)),((int)yPixel - (lp.height/2)),0,0);
        imageToMove.setLayoutParams(lp);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        if(event.getAction() == MotionEvent.ACTION_DOWN){
            originX = x;
            originY = y;
            isTouching = true;
            padBGImageView.setVisibility(View.VISIBLE);
            padThumbImageView.setVisibility(View.VISIBLE);
            moveImageToPoint(R.id.padBGImageView,originX,originY);
            moveImageToPoint(R.id.padThumbImageView,originX,originY);
        }else if (event.getAction() == MotionEvent.ACTION_MOVE){
            moveImageToPoint(R.id.padThumbImageView,x,y);
            if(isTouching){

                // Get the difference of movement from Origin
                float diffY = originY-y;
                float diffX = x - originX;

                if(diffX > 30){ // Right -> scale by X2
                    sendMsg("TWR "+calculateMotorValue(convertPxToDp(diffX)*3));
                }else if(diffX < -30){ // Left -> scale by X2
                    sendMsg("TWL "+calculateMotorValue(convertPxToDp(diffX)*3));
                }else {
                    sendMsg("TWR 0");
                }

                if(diffY > 30){ // Forward
                    sendMsg("MVF "+calculateMotorValue(convertPxToDp(diffY)*3));
                }else if(diffY < -30){ // Backwards
                    sendMsg("MVB "+calculateMotorValue(convertPxToDp(diffY)*3));
                }else {
                    sendMsg("MVF 0");
                }


                // Send cmd to Device
            }
        }else if(event.getAction() == MotionEvent.ACTION_UP){
            originX = 0;
            originY = 0;
            isTouching = false;
            sendMsg("TWR 0");
            sendMsg("MVF 0");
            padBGImageView.setVisibility(View.INVISIBLE);
            padThumbImageView.setVisibility(View.INVISIBLE);
        }
        return true;
    }

    private int calculateMotorValue(int progress) {

        // Get the Positive Int of progress
        if(progress < 0)
            progress = progress * -1;

        if (progress > 30){


            if (progress > 285)
                return _motorMax;
            else {
                return (int) (progress -30);
            }
        }else
            return 0;
    }

    public void sendMsg(String msg){
        Log.d(TAG, "Sending " + msg);
        msg = msg + "\n";
        byte[] msgBuffer = msg.getBytes();
        try{
            BTConSingleton.getInstance().outStream.write(msgBuffer);
        } catch (IOException e) {
            Log.d(TAG, "Unable to get in/out stream");
            e.printStackTrace();
        }
    }
}
