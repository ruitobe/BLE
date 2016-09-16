package com.rui.ble.bluetooth.sensor;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.rui.ble.bluetooth.util.GenericCharacteristicTableRow;

/**
 * Created by rhuang on 8/20/16.
 */
public class SensorBarometerTableRow extends GenericCharacteristicTableRow {

    public SensorBarometerTableRow(Context context) {

        super(context);
        this.calibrateButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if (v.equals(this.calibrateButton)) {

            this.calibrationButtonTouched();
            return;
        }
        this.config = !this.config;

        Animation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setAnimationListener(this);
        fadeOut.setDuration(500);
        fadeOut.setStartOffset(0);
        Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setAnimationListener(this);
        fadeIn.setDuration(500);
        fadeIn.setStartOffset(250);

        if (this.config == true) {

            this.x.startAnimation(fadeOut);
            if ((this.y.isEnabled()))
                this.y.startAnimation(fadeOut);

            if ((this.z.isEnabled()))
                this.z.startAnimation(fadeOut);

            this.value.startAnimation(fadeOut);
            this.onOffLegend.startAnimation(fadeIn);
            this.onOff.startAnimation(fadeIn);
            this.periodLegend.startAnimation(fadeIn);
            this.periodBar.startAnimation(fadeIn);
            this.calibrateButton.startAnimation(fadeIn);
        }
        else {
            this.x.startAnimation(fadeIn);
            if ((this.y.isEnabled()))
                this.y.startAnimation(fadeIn);
            if ((this.z.isEnabled()))
                this.z.startAnimation(fadeIn);

            this.value.startAnimation(fadeIn);
            this.onOffLegend.startAnimation(fadeOut);
            this.onOff.startAnimation(fadeOut);
            this.periodLegend.startAnimation(fadeOut);
            this.periodBar.startAnimation(fadeOut);
            this.calibrateButton.startAnimation(fadeOut);
        }


    }
    @Override
    public void onAnimationStart (Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {

        if (this.config == true) {

            this.x.setVisibility(View.INVISIBLE);
            if ((this.y.isEnabled()))
                this.y.setVisibility(View.INVISIBLE);

            if ((this.y.isEnabled()))
                this.y.setVisibility(View.INVISIBLE);

            this.onOff.setVisibility(View.VISIBLE);
            this.onOffLegend.setVisibility(View.VISIBLE);
            this.periodBar.setVisibility(View.VISIBLE);
            this.periodLegend.setVisibility(View.VISIBLE);
            this.calibrateButton.setVisibility(View.VISIBLE);
        }
        else {
            this.x.setVisibility(View.VISIBLE);
            if ((this.y.isEnabled()))
                this.y.setVisibility(View.VISIBLE);

            if ((this.z.isEnabled()))
                this.z.setVisibility(View.VISIBLE);

            this.onOff.setVisibility(View.INVISIBLE);
            this.onOffLegend.setVisibility(View.INVISIBLE);
            this.periodBar.setVisibility(View.INVISIBLE);
            this.periodLegend.setVisibility(View.INVISIBLE);
            this.calibrateButton.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
    public void calibrationButtonTouched() {
        final Intent intent = new Intent(ACTION_CALIBRATE);
        intent.putExtra(EXTRA_SERVICE_UUID, this.uuidLabel.getText());
        this.context.sendBroadcast(intent);
    }

    @Override
    public void grayedOut(boolean gray) {
        super.grayedOut(gray);
        if (gray) {
            calibrateButton.setAlpha(0.4f);
        }
        else {
            calibrateButton.setAlpha(1.0f);
        }
    }
}
