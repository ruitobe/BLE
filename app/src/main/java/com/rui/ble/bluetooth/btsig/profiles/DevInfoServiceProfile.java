package com.rui.ble.bluetooth.btsig.profiles;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TableRow;

import com.rui.ble.bluetooth.common.BleService;
import com.rui.ble.bluetooth.common.GenericBtProfile;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by rhuang on 8/20/16.
 */
public class DevInfoServiceProfile extends GenericBtProfile {

    private static final String dISService_UUID = "0000180a-0000-1000-8000-00805f9b34fb";
    private static final String dISSystemID_UUID = "00002a23-0000-1000-8000-00805f9b34fb";
    private static final String dISModelNR_UUID = "00002a24-0000-1000-8000-00805f9b34fb";
    private static final String dISSerialNR_UUID = "00002a25-0000-1000-8000-00805f9b34fb";
    private static final String dISFirmwareREV_UUID = "00002a26-0000-1000-8000-00805f9b34fb";
    private static final String dISHardwareREV_UUID = "00002a27-0000-1000-8000-00805f9b34fb";
    private static final String dISSoftwareREV_UUID = "00002a28-0000-1000-8000-00805f9b34fb";
    private static final String dISManifacturerNAME_UUID = "00002a29-0000-1000-8000-00805f9b34fb";

    public final static String ACTION_FW_REV_UPDATED = "com.rui.ble.bluetooth.btsig.ACTION_FW_REV_UPDATED";
    public final static String EXTRA_FW_REV_STRING = "com.rui.ble.bluetooth.btsig.EXTRA_FW_REV_STRING";

    BluetoothGattCharacteristic systemIDc;
    BluetoothGattCharacteristic modelNRc;
    BluetoothGattCharacteristic serialNRc;
    BluetoothGattCharacteristic firmwareREVc;
    BluetoothGattCharacteristic hardwareREVc;
    BluetoothGattCharacteristic softwareREVc;
    BluetoothGattCharacteristic ManifacturerNAMEc;

    DevInfoServiceTableRow tRow;

    public DevInfoServiceProfile(Context context, BluetoothDevice device, BluetoothGattService service, BleService controller) {

        super(context, device, service, controller);

        this.tRow =  new DevInfoServiceTableRow(context);

        List<BluetoothGattCharacteristic> characteristics = this.mBtGattService.getCharacteristics();

        for (BluetoothGattCharacteristic c : characteristics) {
            if (c.getUuid().toString().equals(dISSystemID_UUID)) {
                this.systemIDc = c;
            }
            if (c.getUuid().toString().equals(dISModelNR_UUID)) {
                this.modelNRc = c;
            }
            if (c.getUuid().toString().equals(dISSerialNR_UUID)) {
                this.serialNRc = c;
            }
            if (c.getUuid().toString().equals(dISFirmwareREV_UUID)) {
                this.firmwareREVc = c;
            }
            if (c.getUuid().toString().equals(dISHardwareREV_UUID)) {
                this.hardwareREVc = c;
            }
            if (c.getUuid().toString().equals(dISSoftwareREV_UUID)) {
                this.softwareREVc = c;
            }
            if (c.getUuid().toString().equals(dISManifacturerNAME_UUID)) {
                this.ManifacturerNAMEc = c;
            }
        }

        tRow.title.setText("Device Information Service");
        tRow.title.setTextSize(18);
        tRow.x.setVisibility(View.INVISIBLE);
        this.tRow.setIcon(this.getIconPrefix(), service.getUuid().toString(), "info");
    }
    public static boolean isCorrectService(BluetoothGattService service) {
        if ((service.getUuid().toString().compareTo(dISService_UUID)) == 0) {
            return true;
        }
        else return false;
    }
    @Override
    public void configureService() {
        // Nothing to do here

    }
    @Override
    public void deConfigureService() {
        // Nothing to do here
    }
    @Override
    public void enableService () {

        // Read all values
        this.mBleService.readCharacteristic(this.systemIDc);
        mBleService.waitIdle(GATT_TIMEOUT);
        this.mBleService.readCharacteristic(this.modelNRc);
        mBleService.waitIdle(GATT_TIMEOUT);
        this.mBleService.readCharacteristic(this.serialNRc);
        mBleService.waitIdle(GATT_TIMEOUT);
        this.mBleService.readCharacteristic(this.firmwareREVc);
        mBleService.waitIdle(GATT_TIMEOUT);
        this.mBleService.readCharacteristic(this.hardwareREVc);
        mBleService.waitIdle(GATT_TIMEOUT);
        this.mBleService.readCharacteristic(this.softwareREVc);
        mBleService.waitIdle(GATT_TIMEOUT);
        this.mBleService.readCharacteristic(this.ManifacturerNAMEc);
    }
    @Override
    public void disableService () {
        // Nothing to do here
    }
    @Override
    public void didWriteValueForCharacteristic(BluetoothGattCharacteristic c) {

    }
    @Override
    public void didReadValueForCharacteristic(BluetoothGattCharacteristic c) {
        if (this.systemIDc != null) {
            if (c.equals(this.systemIDc)) {
                String s = "System ID: ";
                for (byte b : c.getValue()) {
                    s+= String.format("%02x:", b);
                }
                this.tRow.SystemIDLabel.setText(s);

            }
        }
        if (this.modelNRc != null) {
            if (c.equals(this.modelNRc)) {
                try {
                    this.tRow.ModelNRLabel.setText("Model NR: " + new String(c.getValue(),"UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        if (this.serialNRc != null) {
            if (c.equals(this.serialNRc)) {
                try {
                    this.tRow.SerialNRLabel.setText("Serial NR: " + new String(c.getValue(),"UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        if (this.firmwareREVc != null) {
            if (c.equals(this.firmwareREVc)) {
                try {
                    String s = new String(c.getValue(),"UTF-8");
                    this.tRow.FirmwareREVLabel.setText("Firmware Revision: " + s);
                    //Post firmware revision to Device activity
                    final Intent intent = new Intent(ACTION_FW_REV_UPDATED);
                    intent.putExtra(EXTRA_FW_REV_STRING, s);
                    context.sendBroadcast(intent);

                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        if (this.hardwareREVc != null) {
            if (c.equals(this.hardwareREVc)) {
                try {
                    this.tRow.HardwareREVLabel.setText("Hardware Revision: " + new String(c.getValue(),"UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        if (this.softwareREVc != null) {
            if (c.equals(this.softwareREVc)) {
                try {
                    this.tRow.SoftwareREVLabel.setText("Software Revision: " + new String(c.getValue(),"UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        if (this.ManifacturerNAMEc != null) {
            if (c.equals(this.ManifacturerNAMEc)) {
                try {
                    this.tRow.ManufacturerNAMELabel.setText("Manifacturer Name: " + new String(c.getValue(),"UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
    public void didUpdateValueForCharacteristic(BluetoothGattCharacteristic c) {

    }
    @Override
    public String getIconPrefix() {
        String iconPrefix;
        if (this.mBtDev.getName().equals("CC2650 SensorTag")) {
            iconPrefix = "";
        }
        else iconPrefix = "";
        return iconPrefix;
    }
    @Override
    public TableRow getTableRow() {
        return this.tRow;
    }
}
