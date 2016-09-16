package com.rui.ble.bluetooth.util;

import android.content.Context;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.rui.ble.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by rhuang on 8/19/16.
 */
public class SensorKeysTableRow extends GenericCharacteristicTableRow {

    public byte lastKeys;
    public ImageView leftKeyPressStateImage;
    public ImageView rightKeyPressStateImage;
    public ImageView reedStateImage;
    public updateSparkLinesTimerTask sparkLineUpdateTask;
    public Timer sparkLineUpdateTimer;

    public SensorKeysTableRow(Context con) {
        super(con);
        this.periodBar.setEnabled(false);
        this.periodLegend.setText("Sensor period (\"Notification\")");
        this.x.maxVal = 1.0f;
        this.x.setColor(255, 255, 0, 0);
        this.y.maxVal = 1.0f;
        this.y.setColor(255, 17, 136, 153);
        this.y.setVisibility(View.VISIBLE);
        this.z.maxVal = 1.0f;
        this.z.setColor(255, 0, 0, 0);
        this.z.setVisibility(View.VISIBLE);
        this.y.setEnabled(true);
        this.z.setEnabled(true);
        this.value.setVisibility(View.INVISIBLE);
        final int nextGuiId = this.z.getId();
        this.leftKeyPressStateImage = new ImageView(con) {
            {
                setId(nextGuiId + 1);
            }
        };
        this.leftKeyPressStateImage.setImageResource(R.drawable.leftkeyoff_300);
        this.rightKeyPressStateImage = new ImageView(con) {
            {
                setId(nextGuiId + 2);
            }
        };
        this.rightKeyPressStateImage.setImageResource(R.drawable.rightkeyoff_300);
        this.reedStateImage = new ImageView(con) {
            {
                setId(nextGuiId + 3);
            }
        };
        this.reedStateImage.setImageResource(R.drawable.reedrelayoff_300);



        //Setup layout for all cell elements
        RelativeLayout.LayoutParams iconItemParams = new RelativeLayout.LayoutParams(
                210,
                180) {
            {
                addRule(RelativeLayout.RIGHT_OF,
                        icon.getId());
                addRule(RelativeLayout.BELOW, title.getId());
            }

        };
        leftKeyPressStateImage.setLayoutParams(iconItemParams);
        leftKeyPressStateImage.setPadding(20, 20, 20, 20);

        iconItemParams = new RelativeLayout.LayoutParams(
                160,
                160) {
            {
                addRule(RelativeLayout.RIGHT_OF,
                        leftKeyPressStateImage.getId());
                addRule(RelativeLayout.BELOW, title.getId());
            }

        };
        rightKeyPressStateImage.setPadding(10, 10, 10, 10);
        rightKeyPressStateImage.setLayoutParams(iconItemParams);
        iconItemParams = new RelativeLayout.LayoutParams(
                160,
                160) {
            {
                addRule(RelativeLayout.RIGHT_OF,
                        rightKeyPressStateImage.getId());
                addRule(RelativeLayout.BELOW, title.getId());
            }

        };
        reedStateImage.setLayoutParams(iconItemParams);
        reedStateImage.setPadding(10, 10, 10, 10);


        //Move sparkLines below the state images

        iconItemParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT) {
            {
                addRule(RelativeLayout.RIGHT_OF,
                        icon.getId());
                addRule(RelativeLayout.BELOW, reedStateImage.getId());
            }

        };

        this.x.setLayoutParams(iconItemParams);
        this.y.setLayoutParams(iconItemParams);
        this.z.setLayoutParams(iconItemParams);


        this.rowLayout.addView(leftKeyPressStateImage);
        this.rowLayout.addView(rightKeyPressStateImage);
        this.rowLayout.addView(reedStateImage);

        this.sparkLineUpdateTimer = new Timer();
        this.sparkLineUpdateTask = new updateSparkLinesTimerTask(this);
        this.sparkLineUpdateTimer.scheduleAtFixedRate(this.sparkLineUpdateTask, 1000, 100);

    }
    @Override
    public void onClick(View v) {
        super.onClick(v);
        Animation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setAnimationListener(this);
        fadeOut.setDuration(500);
        fadeOut.setStartOffset(0);
        Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setAnimationListener(this);
        fadeIn.setDuration(500);
        fadeIn.setStartOffset(250);
        if (this.config == true) {
            this.leftKeyPressStateImage.startAnimation(fadeOut);
            this.rightKeyPressStateImage.startAnimation(fadeOut);
            this.reedStateImage.startAnimation(fadeOut);
        }
        else {
            this.leftKeyPressStateImage.startAnimation(fadeIn);
            this.rightKeyPressStateImage.startAnimation(fadeIn);
            this.reedStateImage.startAnimation(fadeIn);
        }
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        super.onAnimationEnd(animation);
        if (this.config == true) {
            this.leftKeyPressStateImage.setVisibility(View.INVISIBLE);
            this.rightKeyPressStateImage.setVisibility(View.INVISIBLE);
            this.reedStateImage.setVisibility(View.INVISIBLE);

        }
        else {
            this.leftKeyPressStateImage.setVisibility(View.VISIBLE);
            this.rightKeyPressStateImage.setVisibility(View.VISIBLE);
            this.reedStateImage.setVisibility(View.VISIBLE);
        }
    }
    class updateSparkLinesTimerTask extends TimerTask {
        SensorKeysTableRow param;

        public updateSparkLinesTimerTask(SensorKeysTableRow param) {
            this.param = param;
        }

        @Override
        public void run() {
            this.param.post(new Runnable() {
                @Override
                public void run() {

                    if ((param.lastKeys & 0x1) == 0x1) {
                        param.x.addValue(1);
                    }
                    else param.x.addValue(0);
                    if ((param.lastKeys & 0x2) == 0x2) {
                        param.y.addValue(1);
                    }
                    else param.y.addValue(0);
                    if ((param.lastKeys & 0x4) == 0x4) {
                        param.z.addValue(1);
                    }
                    else param.z.addValue(0);
                }
            });
        }
    }
    @Override
    public void grayedOut(boolean gray) {
        super.grayedOut(gray);
        if (gray) {
            this.leftKeyPressStateImage.setAlpha(0.4f);
            this.rightKeyPressStateImage.setAlpha(0.4f);
            this.reedStateImage.setAlpha(0.4f);
            this.z.setAlpha(0.2f);
        }
        else {
            this.leftKeyPressStateImage.setAlpha(1.0f);
            this.rightKeyPressStateImage.setAlpha(1.0f);
            this.reedStateImage.setAlpha(1.0f);
            this.z.setAlpha(1.0f);
        }
    }
}
