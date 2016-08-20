package com.rui.ble.bluetooth.sensor;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import com.rui.ble.bluetooth.common.BleService;
import com.rui.ble.bluetooth.common.GattInfo;
import com.rui.ble.bluetooth.common.GenericBtProfile;
import com.rui.ble.util.Point3D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by rhuang on 8/19/16.
 */
public class SensorBarometerProfile extends GenericBtProfile {

    private BluetoothGattCharacteristic calibC;
    private boolean isCalibrated;
    private boolean isHeightCalibrated;
    private static final double PA_PER_METER = 12.0;

    public SensorBarometerProfile(Context context, BluetoothDevice device, BluetoothGattService service, BleService controller) {
        super(context, device, service, controller);
        this.tRow =  new SensorBarometerTableRow(context);

        List<BluetoothGattCharacteristic> characteristics = this.mBtGattService.getCharacteristics();

        for (BluetoothGattCharacteristic c : characteristics) {
            if (c.getUuid().toString().equals(SensorGatt.UUID_BAR_DATA.toString())) {
                this.dataC = c;
            }
            if (c.getUuid().toString().equals(SensorGatt.UUID_BAR_CONF.toString())) {
                this.configC = c;
            }
            if (c.getUuid().toString().equals(SensorGatt.UUID_BAR_PERI.toString())) {
                this.periodC = c;
            }
            if (c.getUuid().toString().equals(SensorGatt.UUID_BAR_CALI.toString())) {
                this.calibC = c;
            }
        }
        if (this.mBtDev.getName().equals("CC2650 SensorTag")) {
            this.isCalibrated = true;
        }
        else {
            this.isCalibrated = false;
        }
        this.isHeightCalibrated = false;
        this.tRow.x.autoScale = true;
        this.tRow.x.autoScaleBounceBack = true;
        this.tRow.x.setColor(255, 0, 150, 125);
        this.tRow.setIcon(this.getIconPrefix(), this.dataC.getUuid().toString());

        this.tRow.title.setText(GattInfo.uuidToName(UUID.fromString(this.dataC.getUuid().toString())));
        this.tRow.uuidLabel.setText(this.dataC.getUuid().toString());
        this.tRow.value.setText("0.0mBar, 0.0m");
        this.tRow.periodBar.setProgress(100);
    }

    public static boolean isCorrectService(BluetoothGattService service) {
        if ((service.getUuid().toString().compareTo(SensorGatt.UUID_BAR_SERV.toString())) == 0) {
            return true;
        }
        else return false;
    }
    public void enableService() {

        while (!(mBleService.checkGatt())) {

            mBleService.waitIdle(GATT_TIMEOUT);
        }
        if (!(this.isCalibrated)) {

            // Write the calibration code to the configuration registers
            mBleService.writeCharacteristic(this.configC, Sensor.CALIBRATE_SENSOR_CODE);
            mBleService.waitIdle(GATT_TIMEOUT);
            mBleService.readCharacteristic(this.calibC);
            mBleService.waitIdle(GATT_TIMEOUT);
        }
        else {
            int error = mBleService.writeCharacteristic(this.configC, (byte)0x01);

            if (error != 0) {
                if (this.configC != null) {

                    //Log.d("SensorTagBarometerProfile","Sensor config failed: " + this.configC.getUuid().toString() + " Error: " + error);
                }

            }

            error = this.mBleService.setCharacteristicNotification(this.dataC, true);
            if (error != 0) {
                if (this.dataC != null) {

                    // Log.d("SensorTagBarometerProfile","Sensor notification enable failed: " + this.configC.getUuid().toString() + " Error: " + error);
                }

            }
        }

        this.isEnabled = true;

    }
    @Override
    public void didReadValueForCharacteristic(BluetoothGattCharacteristic c) {

        if (this.calibC != null) {
            if (this.calibC.equals(c)) {
                //We have been calibrated
                // Sanity check
                byte[] value = c.getValue();
                if (value.length != 16) {
                    return;
                }

                // Barometer calibration values are read.
                List<Integer> cal = new ArrayList<Integer>();
                for (int offset = 0; offset < 8; offset += 2) {
                    Integer lowerByte = (int) value[offset] & 0xFF;
                    Integer upperByte = (int) value[offset + 1] & 0xFF;
                    cal.add((upperByte << 8) + lowerByte);
                }

                for (int offset = 8; offset < 16; offset += 2) {
                    Integer lowerByte = (int) value[offset] & 0xFF;
                    Integer upperByte = (int) value[offset + 1];
                    cal.add((upperByte << 8) + lowerByte);
                }

                //Log.d("SensorTagBarometerProfile", "Barometer calibrated !!!!!");
                BarometerCalibrationCoefficients.INSTANCE.barometerCalibrationCoefficients = cal;
                this.isCalibrated = true;
                int error = mBleService.writeCharacteristic(this.configC, (byte)0x01);

                if (error != 0) {
                    if (this.configC != null) {

                        //Log.d("SensorTagBarometerProfile","Sensor config failed: " + this.configC.getUuid().toString() + " Error: " + error);
                    }

                }

                error = this.mBleService.setCharacteristicNotification(this.dataC, true);

                if (error != 0) {
                    if (this.dataC != null) {

                        // Log.d("SensorTagBarometerProfile","Sensor notification enable failed: " + this.configC.getUuid().toString() + " Error: " + error);
                    }

                }
            }
        }
    }

    @Override
    public void didUpdateValueForCharacteristic(BluetoothGattCharacteristic c) {

        byte[] value = c.getValue();
        if (c.equals(this.dataC)){

            Point3D v;
            v = Sensor.BAROMETER.convert(value);
            if (!(this.isHeightCalibrated)) {
                BarometerCalibrationCoefficients.INSTANCE.heightCalibration = v.x;
                //Toast.makeText(this.tRow.getContext(), "Height measurement calibrated",
                //			    Toast.LENGTH_SHORT).show();
                this.isHeightCalibrated = true;
            }
            double h = (v.x - BarometerCalibrationCoefficients.INSTANCE.heightCalibration)
                    / PA_PER_METER;
            h = (double) Math.round(-h * 10.0) / 10.0;
            //msg = decimal.format(v.x / 100.0f) + "\n" + h;


            if (this.tRow.config == false) this.tRow.value.setText(String.format("%.1f mBar %.1f meter", v.x / 100, h));
            this.tRow.x.addValue((float)v.x / 100.0f);
            //mBarValue.setText(msg);
        }
    }

    protected void calibrationButtonTouched() {
        this.isHeightCalibrated = false;
    }

    @Override
    public Map<String,String> getMQTTMap() {
        Point3D v = Sensor.BAROMETER.convert(this.dataC.getValue());
        Map<String,String> map = new HashMap<String, String>();
        map.put("air_pressure",String.format("%.2f",v.x / 100));
        return map;
    }
}
