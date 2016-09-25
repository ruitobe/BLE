package com.rui.ble.bluetooth.common;

import android.bluetooth.BluetoothDevice;

/**
 * Created by ruihuan on 9/24/16.
 */

public class UserInfo {
    // Data
    private String mName;
    private String mPassword;
    private String mEmail;

    public UserInfo(String name, String email, String password) {
        mName = name;
        mEmail = email;
        mPassword = password;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setPassword(String password) {
        mPassword = password;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public String getName() {
        return mName;
    }

    public String getPassword() {
        return mPassword;
    }

    public String getEmail() {
        return mEmail;
    }
}
