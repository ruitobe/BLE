package com.rui.ble.bluetooth.util;

import android.widget.ProgressBar;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by rhuang on 7/26/16.
 */
public class CustomTimer {

    private Timer mTimer;
    private CustomTimerCallback mCb = null;
    private ProgressBar mProgressBar;
    private int mTimeout;

    public CustomTimer(ProgressBar progressBar, int timeout, CustomTimerCallback cb) {

        mTimeout = timeout;
        mProgressBar = progressBar;
        mTimer = new Timer();
        ProgressTask task = new ProgressTask();
        // One second
        mTimer.schedule(task, 0, 1000);
        mCb = cb;
    }

    public void stop() {

        if (mTimer != null) {

            mTimer.cancel();
            mTimer = null;
        }
    }

    private class ProgressTask extends TimerTask {
        int cnt = 0;

        @Override
        public void run() {

            cnt++;
            if(mProgressBar != null)
                mProgressBar.setProgress(cnt);

            if(cnt >= mTimeout) {

                mTimer.cancel();
                mTimer = null;

                if(mCb != null)
                    mCb.onTimeout();
            } else {
                if(mCb != null)
                    mCb.onTick(cnt);
            }
        }
    }
}
