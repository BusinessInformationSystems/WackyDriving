package com.bis.androidcarrc;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;

/**
 * Created by derrickmilford on 29/01/15.
 */
public class ControllerActivity extends Activity implements View.OnClickListener, SpringListener {

    private static double TENSION = 800;
    private static double DAMPER = 20; //friction

    private ImageView mImageToAnimate;
    private SpringSystem mSpringSystem;
    private Spring mSpring;

    private boolean mMovedUp = false;
    private float mOrigY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);

        mImageToAnimate = (ImageView) findViewById(R.id.imageView);
        mImageToAnimate.setOnClickListener(this);

        mSpringSystem = SpringSystem.create();

        mSpring = mSpringSystem.createSpring();
        mSpring.addListener(this);

        SpringConfig config = new SpringConfig(TENSION, DAMPER);
        mSpring.setSpringConfig(config);
    }

    @Override
    public void onClick(View v) {
        if (mMovedUp) {
            mSpring.setEndValue(mOrigY);
        } else {
            mOrigY = mImageToAnimate.getY();

            mSpring.setEndValue(mOrigY - 300f);
        }

        mMovedUp = !mMovedUp;
    }

    @Override
    public void onSpringUpdate(Spring spring) {
        float value = (float) spring.getCurrentValue();

        mImageToAnimate.setY(value);
    }

    @Override
    public void onSpringAtRest(Spring spring) {

    }

    @Override
    public void onSpringActivate(Spring spring) {

    }

    @Override
    public void onSpringEndStateChange(Spring spring) {

    }
}
