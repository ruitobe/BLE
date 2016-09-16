package com.rui.ble.bluetooth.common;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TableRow;

import com.rui.ble.bluetooth.util.GenericCharacteristicTableRow;

import java.util.Map;

/**
 * Created by rhuang on 8/7/16.
 */
public class GenericBtProfile {

    protected BluetoothDevice mBtDev;
    protected BluetoothGattService mBtGattService;
    protected GenericCharacteristicTableRow tRow;
    protected BleService mBleService;
    protected BluetoothGattCharacteristic dataC;
    protected BluetoothGattCharacteristic configC;
    protected BluetoothGattCharacteristic periodC;
    protected static final int GATT_TIMEOUT = 250; // milliseconds
    protected Context context;
    protected boolean isRegistered;
    public boolean isConfigured;
    public boolean isEnabled;

    public GenericBtProfile(final Context context, BluetoothDevice dev, BluetoothGattService service, BleService controller) {

        super();
        this.mBtDev = dev;
        this.mBtGattService = service;
        this.mBleService = controller;
        this.tRow =  new GenericCharacteristicTableRow(context);
        this.dataC = null;
        this.periodC = null;
        this.configC = null;
        this.context = context;
        this.isRegistered = false;
    }

    public void onResume() {

        if (this.isRegistered == false) {
            this.context.registerReceiver(guiReceiver, GenericBtProfile.makeFilter());
            this.isRegistered = true;
        }
    }

    public void onPause() {

        if (this.isRegistered == true) {
            this.context.unregisterReceiver(guiReceiver);
            this.isRegistered = false;
        }
    }

    public static boolean isCorrectService(BluetoothGattService service) {
        //Always return false in parent class
        return false;
    }

    public boolean isDataC(BluetoothGattCharacteristic c) {
        if (this.dataC == null) return false;
        if (c.equals(this.dataC)) return true;
        else return false;
    }

    public void configureService() {

        int error = this.mBleService.setCharacteristicNotification(this.dataC, true);

        if (error != 0) {
            if (this.dataC != null)
                printError("Sensor notification enable failed: ",this.dataC,error);
        }
        this.isConfigured = true;
    }

    public void deConfigureService() {

        int error = this.mBleService.setCharacteristicNotification(this.dataC, false);
        if (error != 0) {
            if (this.dataC != null)
                printError("Sensor notification disable failed: ",this.dataC,error);
        }
        this.isConfigured = false;
    }

    public void enableService () {

        int error = mBleService.writeCharacteristic(this.configC, (byte)0x01);
        if (error != 0) {
            if (this.configC != null)
                printError("Sensor enable failed: ",this.configC,error);
        }
        //this.periodWasUpdated(1000);
        this.isEnabled = true;
    }

    public void disableService () {

        int error = mBleService.writeCharacteristic(this.configC, (byte)0x00);
        if (error != 0) {
            if (this.configC != null)
                printError("Sensor disable failed: ",this.configC,error);
        }
        this.isConfigured = false;
    }

    public void didWriteValueForCharacteristic(BluetoothGattCharacteristic c) {

    }

    public void didReadValueForCharacteristic(BluetoothGattCharacteristic c) {
        if (this.periodC != null) {
            if (c.equals(this.periodC)) {
                byte[] value = c.getValue();
                this.periodWasUpdated(value[0] * 10);
            }
        }
    }


    public void didUpdateValueForCharacteristic(BluetoothGattCharacteristic c) {
        /*
		if (c.equals(this.dataC)) {
			byte[] value = c.getValue();
			this.didUpdateValueForCharacteristic(this.dataC.getUuid().toString(), value);
		}
		*/
    }


    public TableRow getTableRow() {

        return this.tRow;
    }

    public String getIconPrefix() {

        String iconPrefix;
        if (this.mBtDev.getName().equals("CC2650 SensorTag")) {

            iconPrefix = "";
        }
        else iconPrefix = "";
        return iconPrefix;
    }

    public boolean isEnabledByPrefs(String prefName) {

        String preferenceKeyString = "pref_"
                + prefName;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mBleService);

        Boolean defaultValue = true;
        return prefs.getBoolean(preferenceKeyString, defaultValue);
    }


    public void periodWasUpdated(int period) {

        if (period > 2450) period = 2450;
        if (period < 100) period = 100;
        byte p = (byte)((period / 10) + 10);
        Log.d("GenericBluetoothProfile","Period characteristic set to :" + period);
        /*
		if (this.mBTLeService.writeCharacteristic(this.periodC, p)) {
			mBTLeService.waitIdle(GATT_TIMEOUT);
		} else {
			Log.d("GenericBluetoothProfile","Sensor period failed: " + this.periodC.getUuid().toString());
		}
		*/


        int error = mBleService.writeCharacteristic(this.periodC, p);

        if (error != 0) {
            if (this.periodC != null)
                printError("Sensor period failed: ",this.periodC,error);
        }
        this.tRow.periodLegend.setText("Sensor period (currently : " + period + "ms)");

    }

    public Map<String,String> getMQTTMap() {
        return null;
    }

    public void onOffWasUpdated(boolean on) {
        Log.d("GenericBluetoothProfile","Config characteristic set to :" + on);
        if (on) {
            this.configureService();
            this.enableService();
            this.tRow.grayedOut(false);
        }
        else {
            this.deConfigureService();
            this.disableService();
            this.tRow.grayedOut(true);
        }

    }

    public void grayOutCell(boolean grayedOut) {

        if (grayedOut == true){
            this.tRow.setAlpha(0.4f);
            this.tRow.onOff.setChecked(false);
        }
        else {
            this.tRow.setAlpha(1.0f);
            this.tRow.onOff.setChecked(true);
        }
    }

    private static IntentFilter makeFilter() {

        final IntentFilter fi = new IntentFilter();
        fi.addAction(GenericCharacteristicTableRow.ACTION_PERIOD_UPDATE);
        fi.addAction(GenericCharacteristicTableRow.ACTION_ONOFF_UPDATE);
        fi.addAction(GenericCharacteristicTableRow.ACTION_CALIBRATE);
        return fi;
    }

    private final BroadcastReceiver guiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            final String uuid = intent.getStringExtra(GenericCharacteristicTableRow.EXTRA_SERVICE_UUID);
            Log.d("Test", uuid + "!=" + tRow.uuidLabel.getText().toString());
            if ((tRow.uuidLabel.getText().toString().compareTo(uuid)) == 0) {
                if ((action.compareTo(GenericCharacteristicTableRow.ACTION_PERIOD_UPDATE) == 0)) {
                    final int period = intent.getIntExtra(GenericCharacteristicTableRow.EXTRA_PERIOD, 1000);
                    periodWasUpdated(period);
                }
                else if ((action.compareTo(GenericCharacteristicTableRow.ACTION_ONOFF_UPDATE) == 0)) {
                    final boolean on = intent.getBooleanExtra(GenericCharacteristicTableRow.EXTRA_ONOFF, false);
                    onOffWasUpdated(on);
                }
                else if ((action.compareTo(GenericCharacteristicTableRow.ACTION_CALIBRATE) == 0)) {
                    calibrationButtonTouched();
                }
            }
        }
    };

    protected void calibrationButtonTouched() {

    }

    public void didUpdateFirmwareRevision(String fwRev) {

    }
    public void printError (String msg, BluetoothGattCharacteristic c, int error) {
        try {
            Log.d("GenericBluetoothProfile", msg + c.getUuid().toString() + " Error: " + error);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
