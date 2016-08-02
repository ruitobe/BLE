package com.rui.ble.bluetooth.common;

import android.bluetooth.BluetoothDevice;

/**
 * Created by rhuang on 7/26/16.
 */
public class BleDevInfo {

    // Data
    private BluetoothDevice mBtDev;
    private int mRssi;

    public BleDevInfo(BluetoothDevice dev, int rssi) {
        mBtDev = dev;
        mRssi = rssi;
    }

    public BluetoothDevice getBtDev() {
        return mBtDev;
    }

    public int getRssi() {
        return mRssi;
    }

    public void updateRssi(int val) {
        mRssi = val;
    }
}
