package com.rui.ble.bluetooth.sensor;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.rui.ble.R;
import com.rui.ble.bluetooth.btsig.profiles.DevInfoServiceProfile;
import com.rui.ble.bluetooth.common.BleService;
import com.rui.ble.bluetooth.common.GattInfo;
import com.rui.ble.bluetooth.common.GenericBtProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by rhuang on 8/4/16.
 */

public class DevActivity extends AppCompatActivity {

    private static String TAG = "DevActivity";

    public static final String EXTRA_DEVICE = "EXTRA_DEVICE";
    private static final int PREF_ACT_REQ = 0;

    public static DevActivity mInstance = null;

    private BleService mBleService = null;
    private BluetoothDevice mBtDev = null;
    private BluetoothGatt mBtGatt = null;
    private List<BluetoothGattService> mServiceList = null;
    private boolean mServiceReady = false;
    private boolean mIsReceiving = false;

    //
    private BluetoothGattService mOadService = null;
    private BluetoothGattService mConnCtrlService = null;
    private BluetoothGattService mTestService = null;
    private boolean mIsSensor2;
    private String mFwRev;

    public ProgressDialog progressDialog;

    // GUI
    private List<GenericBtProfile> mProfiles;
    public boolean isFirst = true;
    private View view;
    private DevView mDevView = null;

    private boolean mBusy;

    public static DevActivity getInstance() {

        return mInstance;
    }

    String firmwareRevision() {
        return mFwRev;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mInstance = this;

        // BLE
        mBleService = ((BleAppClass)getApplicationContext()).getBleService();
        mBtDev = intent.getParcelableExtra(EXTRA_DEVICE);
        mServiceList = new ArrayList<BluetoothGattService>();

        mIsSensor2 = false;

        String devName = mBtDev.getName();
        if((devName.equals("SensorTag2")) || (devName.equals("CC2650 SensorTag"))) {
            mIsSensor2 = true;
        }
        else mIsSensor2 = false;

        // GUI

        setContentView(R.layout.activity_dev);

        mDevView = DevView.mInstance;

        mProfiles = new ArrayList<GenericBtProfile>();

        progressDialog = new ProgressDialog(DevActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(true);
        progressDialog.setTitle("Discovering Services");
        progressDialog.setMessage("");
        progressDialog.setMax(100);
        progressDialog.setProgress(0);
        progressDialog.show();

        Resources resources = getResources();
        XmlResourceParser xrp = resources.getXml(R.xml.gatt_uuid);
        new GattInfo(xrp);

    }

    private static IntentFilter makeGattUpdateIntentFilter() {

        final IntentFilter filter = new IntentFilter();
        filter.addAction(BleService.ACTION_GATT_SERVICES_DISCOVERED);
        filter.addAction(BleService.ACTION_DATA_NOTIFY);
        filter.addAction(BleService.ACTION_DATA_WRITE);
        filter.addAction(BleService.ACTION_DATA_READ);
        filter.addAction(DevInfoServiceProfile.ACTION_FW_REV_UPDATED);
        return filter;
    }

    @Override
    protected void onPostResume() {

        super.onPostResume();

        if (!mIsReceiving) {

            registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
            mIsReceiving = true;
        }

        for (GenericBtProfile profile : mProfiles) {

            if (profile.isConfigured != true)
                profile.configureService();
            if (profile.isEnabled != true)
                profile.enableService();
            profile.onResume();
        }
        this.mBleService.abortTimedDisconnect();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        if(mIsReceiving) {
            unregisterReceiver(mGattUpdateReceiver);
            mIsReceiving = false;
        }
        for(GenericBtProfile profile : mProfiles) {
            profile.onPause();
        }

        if(!this.isEnabledByPrefs("keepAlive")) {
            this.mBleService.timedDisconnect();
        }

        // View should be started again from the scratch

        this.mDevView.first = true;
        this.mProfiles = null;
        this.mDevView.removeRowsFromTable();
        this.mDevView = null;
        finishActivity(PREF_ACT_REQ);

    }

    public boolean isEnabledByPrefs(String prefName) {
        String preferenceKeyString = "pref_"
                + prefName;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mBleService);

        Boolean defaultValue = true;
        return prefs.getBoolean(preferenceKeyString, defaultValue);
    }

    void onViewInflated(View view) {

        setBusy(true);

        // Set title bar to device name
        //setTitle(mBtDev.getName());

        // Create GATT object
        mBtGatt = BleService.getBtGatt();
        // Start service discovery
        if (!mServiceReady && mBtGatt != null) {

            if (mBleService.getNumServices() == 0) {

                discoverServices();
            }


            else {

            }
        }
    }

    boolean isSensor2() {

        return mIsSensor2;
    }

    BluetoothGattService getConnCtrlService() {
        return mConnCtrlService;
    }

    private void discoverServices() {

        if (mBtGatt.discoverServices()) {

            mServiceList.clear();
            setBusy(true);

        } else {

        }
    }

    private void setBusy(boolean busy) {

        mBusy = busy;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            default:
                break;
        }
    }

    private void setError(String string) {
        setBusy(false);
        Toast.makeText(this, string, Toast.LENGTH_LONG).show();
    }

    private void setStatus(String string) {

        Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
    }


    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {

        List <BluetoothGattService> serviceList;
        List <BluetoothGattCharacteristic> charList = new ArrayList<BluetoothGattCharacteristic>();

        @Override
        public void onReceive(final Context context, Intent intent) {

            final String action = intent.getAction();
            final int status = intent.getIntExtra(BleService.EXTRA_STATUS,
                    BluetoothGatt.GATT_SUCCESS);

            if (BleService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                if (status == BluetoothGatt.GATT_SUCCESS) {

                    serviceList = mBleService.getSupportedGattServices();

                    if (serviceList.size() > 0) {

                        for (int ii = 0; ii < serviceList.size(); ii++) {

                            BluetoothGattService service = serviceList.get(ii);
                            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();

                            if (characteristics.size() > 0) {
                                for (int jj = 0; jj < characteristics.size(); jj++) {
                                    charList.add(characteristics.get(jj));
                                }
                            }
                        }
                    }

                    Log.d("DeviceActivity", "Total characteristics " + charList.size());
                    Thread worker = new Thread(new Runnable() {
                        @Override
                        public void run() {

                            //Iterate through the services and add GenericBluetoothServices for each service
                            int nrNotificationsOn = 0;
                            int maxNotifications;
                            int servicesDiscovered = 0;
                            int totalCharacteristics = 0;

                            for (BluetoothGattService service : serviceList) {
                                List<BluetoothGattCharacteristic> chars = service.getCharacteristics();
                                totalCharacteristics += chars.size();
                            }

                            // TODO: cloud service for future

                            if (totalCharacteristics == 0) {
                                //Something bad happened, we have a problem
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.hide();
                                        progressDialog.dismiss();
                                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                        alertDialogBuilder.setTitle("Error !");
                                        alertDialogBuilder.setMessage(serviceList.size() + " Services found, but no characteristics found, device will be disconnected...");
                                        alertDialogBuilder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                mBleService.refreshDevCache(mBtGatt);
                                                //Try again
                                                discoverServices();
                                            }
                                        });

                                        alertDialogBuilder.setNegativeButton("Disconnect",new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                mBleService.disconnect(mBtDev.getAddress());
                                            }
                                        });
                                        AlertDialog a = alertDialogBuilder.create();
                                        a.show();
                                    }
                                });
                                return;
                            }

                            final int final_totalCharacteristics = totalCharacteristics;

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.setIndeterminate(false);
                                    progressDialog.setTitle("Creating Services For You");
                                    progressDialog.setMessage("Having a total of " + serviceList.size() + " services with a total of " + final_totalCharacteristics + " characteristics on this device" );

                                }
                            });

                            if (Build.VERSION.SDK_INT > 18)
                                maxNotifications = 7;
                            else {
                                maxNotifications = 4;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(context, "Android version 4.3 detected, max 4 notifications enabled", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }

                            for (int ii = 0; ii < serviceList.size(); ii++) {

                                BluetoothGattService service = serviceList.get(ii);
                                List<BluetoothGattCharacteristic> chars = service.getCharacteristics();

                                if (chars.size() == 0) {

                                    Log.d("DeviceActivity", "No characteristics found for this service !!!");
                                    //return;
                                }

                                else {
                                    servicesDiscovered++;
                                }


                                final float serviceDiscoveredcalc = (float)servicesDiscovered;
                                final float serviceTotalcalc = (float)serviceList.size();

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.setProgress((int)((serviceDiscoveredcalc / (serviceTotalcalc - 1)) * 100));
                                    }
                                });


                                Log.d("DeviceActivity", "Configuring service with uuid : " + service.getUuid().toString());


                                if (SensorHumidityProfile.isCorrectService(service)) {

                                    SensorHumidityProfile hum = new SensorHumidityProfile(context, mBtDev, service, mBleService);

                                    mProfiles.add(hum);

                                    if (nrNotificationsOn < maxNotifications) {
                                        hum.configureService();
                                        nrNotificationsOn++;
                                    }
                                    else {
                                        hum.grayOutCell(true);
                                    }
                                    Log.d("DeviceActivity","Found Humidity !");
                                }

                                if (SensorLuxProfile.isCorrectService(service)) {
                                    SensorLuxProfile lux = new SensorLuxProfile(context, mBtDev, service, mBleService);

                                    mProfiles.add(lux);

                                    if (nrNotificationsOn < maxNotifications) {
                                        lux.configureService();
                                        nrNotificationsOn++;
                                    }
                                    else {
                                        lux.grayOutCell(true);
                                    }
                                }

                                if (SensorKeysProfile.isCorrectService(service)) {
                                    SensorKeysProfile key = new SensorKeysProfile(context, mBtDev, service, mBleService);

                                    mProfiles.add(key);
                                    if (nrNotificationsOn < maxNotifications) {
                                        key.configureService();
                                        nrNotificationsOn++;
                                    }
                                    else {
                                        key.grayOutCell(true);
                                    }
                                    Log.d("DeviceActivity","Found Simple Keys !");
                                }

                                if (SensorBarometerProfile.isCorrectService(service)) {
                                    SensorBarometerProfile baro = new SensorBarometerProfile(context, mBtDev, service, mBleService);

                                    mProfiles.add(baro);
                                    if (nrNotificationsOn < maxNotifications) {
                                        baro.configureService();
                                        nrNotificationsOn++;
                                    }
                                    else {
                                        baro.grayOutCell(true);
                                    }
                                    Log.d("DeviceActivity","Found Barometer !");
                                }

                                if (SensorAmbientTempProfile.isCorrectService(service)) {

                                    SensorAmbientTempProfile AmTemp = new SensorAmbientTempProfile(context, mBtDev, service, mBleService);

                                    mProfiles.add(AmTemp);
                                    if (nrNotificationsOn < maxNotifications) {
                                        AmTemp.configureService();
                                        nrNotificationsOn++;
                                    }
                                    else {
                                        AmTemp.grayOutCell(true);
                                    }
                                    Log.d("DeviceActivity","Found Ambient Temperature !");
                                }

                                if (SensorIRTempProfile.isCorrectService(service)) {

                                    SensorIRTempProfile irTemp = new SensorIRTempProfile(context, mBtDev, service, mBleService);

                                    mProfiles.add(irTemp);

                                    if (nrNotificationsOn < maxNotifications) {
                                        irTemp.configureService();
                                    }
                                    else {
                                        irTemp.grayOutCell(true);
                                    }
                                    //No notifications add here because it is already enabled above ..
                                    Log.d("DeviceActivity","Found IR Temperature !");
                                }

                                if (SensorMovementProfile.isCorrectService(service)) {

                                    SensorMovementProfile mov = new SensorMovementProfile(context, mBtDev, service, mBleService);

                                    mProfiles.add(mov);
                                    if (nrNotificationsOn < maxNotifications) {
                                        mov.configureService();
                                        nrNotificationsOn++;
                                    }
                                    else {
                                        mov.grayOutCell(true);
                                    }
                                    Log.d("DeviceActivity","Found Motion !");
                                }

                                if (SensorAccProfile.isCorrectService(service)) {

                                    SensorAccProfile acc = new SensorAccProfile(context, mBtDev, service, mBleService);

                                    mProfiles.add(acc);
                                    if (nrNotificationsOn < maxNotifications) {
                                        acc.configureService();
                                        nrNotificationsOn++;
                                    }
                                    else {
                                        acc.grayOutCell(true);
                                    }
                                    Log.d("DeviceActivity","Found Motion !");

                                }



                                if (DevInfoServiceProfile.isCorrectService(service)) {

                                    DevInfoServiceProfile devInfo = new DevInfoServiceProfile(context, mBtDev, service, mBleService);
                                    mProfiles.add(devInfo);

                                    devInfo.configureService();
                                    Log.d("DeviceActivity","Found Device Information Service");
                                }

                                if ((service.getUuid().toString().compareTo("f000ccc0-0451-4000-b000-000000000000")) == 0) {

                                    mConnCtrlService = service;
                                }

                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.setTitle("Enabling Services");
                                    progressDialog.setMax(mProfiles.size());
                                    progressDialog.setProgress(0);
                                }
                            });

                            for (final GenericBtProfile profile : mProfiles) {

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mDevView == null)
                                        Log.e("AddRowToTable: ", "mDevView is null!");

                                        mDevView.addRowToTable(profile.getTableRow());

                                        profile.enableService();

                                        progressDialog.setProgress(progressDialog.getProgress() + 1);
                                    }
                                });

                                profile.onResume();
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.hide();
                                    progressDialog.dismiss();
                                }
                            });
                        }
                    });
                    worker.start();

                } else {
                    Toast.makeText(getApplication(), "Service discovery failed",
                            Toast.LENGTH_LONG).show();
                    return;
                }
            } else if (BleService.ACTION_DATA_NOTIFY.equals(action)) {

                // Notification
                byte[] value = intent.getByteArrayExtra(BleService.EXTRA_DATA);
                String uuidStr = intent.getStringExtra(BleService.EXTRA_UUID);
                //Log.d("DeviceActivity","Got Characteristic : " + uuidStr);

                for (int ii = 0; ii < charList.size(); ii++) {

                    BluetoothGattCharacteristic tempC = charList.get(ii);

                    if ((tempC.getUuid().toString().equals(uuidStr))) {

                        for (int jj = 0; jj < mProfiles.size(); jj++) {

                            GenericBtProfile profile = mProfiles.get(jj);
                            if (profile.isDataC(tempC)) {
                                profile.didUpdateValueForCharacteristic(tempC);

                                //Do MQTT
                                Map<String,String> map = profile.getMQTTMap();
                                if (map != null) {
                                    for (Map.Entry<String, String> e : map.entrySet()) {

                                        //if (mqttProfile != null)
                                        //    mqttProfile.addSensorValueToPendingMessage(e);
                                    }
                                }
                            }
                        }
                        //Log.d("DeviceActivity","Got Characteristic : " + tempC.getUuid().toString());
                        break;
                    }
                }

            } else if (BleService.ACTION_DATA_WRITE.equals(action)) {

                // Data written
                byte[] value = intent.getByteArrayExtra(BleService.EXTRA_DATA);

                String uuidStr = intent.getStringExtra(BleService.EXTRA_UUID);

                for (int ii = 0; ii < charList.size(); ii++) {

                    BluetoothGattCharacteristic tempC = charList.get(ii);

                    if ((tempC.getUuid().toString().equals(uuidStr))) {

                        for (int jj = 0; jj < mProfiles.size(); jj++) {

                            GenericBtProfile profile = mProfiles.get(jj);
                            profile.didWriteValueForCharacteristic(tempC);
                        }
                        //Log.d("DeviceActivity","Got Characteristic : " + tempC.getUuid().toString());
                        break;
                    }
                }
            } else if(BleService.ACTION_DATA_READ.equals(action)) {
                // Data read
                byte[] value = intent.getByteArrayExtra(BleService.EXTRA_DATA);
                String uuidStr = intent.getStringExtra(BleService.EXTRA_UUID);

                for (int ii = 0; ii < charList.size(); ii++) {

                    BluetoothGattCharacteristic tempC = charList.get(ii);

                    if ((tempC.getUuid().toString().equals(uuidStr))) {

                        for (int jj = 0; jj < mProfiles.size(); jj++) {

                            GenericBtProfile profile = mProfiles.get(jj);
                            profile.didReadValueForCharacteristic(tempC);
                        }
                        //Log.d("DeviceActivity","Got Characteristic : " + tempC.getUuid().toString());
                        break;
                    }
                }
            }

            if (status != BluetoothGatt.GATT_SUCCESS) {
                setError("GATT error code: " + status);
            }
        }
    };



}
