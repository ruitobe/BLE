package com.rui.ble.bluetooth.util;

/**
 * Created by rhuang on 7/26/16.
 */
public abstract class CustomTimerCallback {

    protected abstract void onTimeout();

    protected abstract void onTick(int i);
}
