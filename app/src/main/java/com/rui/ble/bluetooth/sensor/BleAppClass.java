package com.rui.ble.bluetooth.sensor;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.rui.ble.R;
import com.rui.ble.bluetooth.common.BleService;

/**
 * Created by rhuang on 7/31/16.
 */
public class BleAppClass extends Application {

    private static final String TAG = "BleAppClass";

    private static final int REQ_ENABLE_BT = 0;
    public boolean mBtAdapterEnabled = false;
    public boolean mBleSupported = true;
    private BleService mBleService = null;
    private IntentFilter mIntentFilter;
    public BluetoothAdapter mBtAdapter = null;
    public static BluetoothManager mBluetoothManager;

    @Override
    public void onCreate() {

        super.onCreate();

        Log.e(TAG, "onCreate");
        // Use this check to determine whether BLE is support on the device.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE is not supported", Toast.LENGTH_LONG).show();
            mBleSupported = false;

        }

        // Initializes a Bluetooth adapter. For API level 18 and above, get a
        // reference to BluetoothAdapter through BluetoothManager.
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBtAdapter = mBluetoothManager.getAdapter();


        if(mBtAdapter == null) {
            Toast.makeText(this, "", Toast.LENGTH_LONG).show();
            mBleSupported = false;

        }

        mIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, mIntentFilter);

        if(!mBtAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(enableIntent);
        }

        startBleService();


    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName componentName, IBinder service) {

            Log.e(TAG, "onServiceConnected");

            mBleService = ((BleService.LocalBinder) service).getService();

            Log.e(TAG, "mBleService = " + mBleService);
            if (!mBleService.init()) {

                return;
            }

            final int n = mBleService.numOfConnectedDev();
            if (n > 0) {

                Log.e(TAG, "mBleService.numOfConnectedDev() = " + mBleService.numOfConnectedDev());

            } else {

                Log.e(TAG, "mBleService.numOfConnectedDev() = " + mBleService.numOfConnectedDev());

            }
        }

        public void onServiceDisconnected(ComponentName componentName) {

            Log.e(TAG, "onServiceDisconnected =>  mBleService = null");
            mBleService = null;
        }
    };

    public BleService getBleService() {
        return mBleService;
    }

    private void startBleService() {
        boolean flag;

        Intent bindIntent = new Intent(this, BleService.class);
        startService(bindIntent);

        flag = bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        if (!flag) {
            Toast.makeText(this, R.string.bt_on, Toast.LENGTH_SHORT).show();
        }
    }



    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                // Bluetooth adapter state change
                switch (mBtAdapter.getState()) {

                    case BluetoothAdapter.STATE_ON:
                        //ConnIndex = NO_DEVICE;
                        Log.e(TAG, "BluetoothAdapter.STATE_ON, startBleService()");
                        startBleService();
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        break;
                    default:
                        break;
                }
            }
        }
    };
}
