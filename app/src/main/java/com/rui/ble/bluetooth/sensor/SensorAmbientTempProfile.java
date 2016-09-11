package com.rui.ble.bluetooth.sensor;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import com.opencsv.CSVWriter;
import com.rui.ble.bluetooth.common.BleService;
import com.rui.ble.bluetooth.common.GenericBtProfile;
import com.rui.ble.util.GenericCharacteristicTableRow;
import com.rui.ble.util.Point3D;
import com.rui.ble.util.RunningTime;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rhuang on 8/20/16.
 */
public class SensorAmbientTempProfile extends GenericBtProfile {

    // Data file stored
    private File dir = null;
    private File subDir = null;
    private String path = null;
    private StringBuilder fileName = null;
    private String filePath = null;

    private CSVWriter writer = null;

    private List<String[]> data = new ArrayList<String[]>();
    private RunningTime runningTime = new RunningTime();

    public SensorAmbientTempProfile(Context context, BluetoothDevice device, BluetoothGattService service, BleService controller) {

        super(context, device, service, controller);
        this.tRow =  new GenericCharacteristicTableRow(context);

        List<BluetoothGattCharacteristic> characteristics = this.mBtGattService.getCharacteristics();

        for (BluetoothGattCharacteristic c : characteristics) {

            if (c.getUuid().toString().equals(SensorGatt.UUID_IRT_DATA.toString())) {
                this.dataC = c;
            }
            if (c.getUuid().toString().equals(SensorGatt.UUID_IRT_CONF.toString())) {
                this.configC = c;
            }
            if (c.getUuid().toString().equals(SensorGatt.UUID_IRT_PERI.toString())) {
                this.periodC = c;
            }
        }

        this.tRow.x.autoScale = true;

        this.tRow.x.autoScaleBounceBack = true;

        this.tRow.setIcon(this.getIconPrefix(), this.dataC.getUuid().toString(), "ambienttemp");


        this.tRow.title.setText("Ambient Temperature Data");
        this.tRow.title.setTextSize(18);
        this.tRow.uuidLabel.setText(this.dataC.getUuid().toString());
        this.tRow.value.setText("0.0'C");
        this.tRow.periodMinVal = 200;
        this.tRow.periodBar.setMax(255 - (this.tRow.periodMinVal / 10));
        this.tRow.periodBar.setProgress(100);

        // Consider save the new data to local flash
        // Data file stored only if the parent directory exists

        dir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/BLE");
        if (dir.exists()) {
            // create sub dir for this sensor data
            subDir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/BLE/ambient_temp");
            subDir.mkdir();

            path = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/BLE/ambient_temp";
            fileName = new StringBuilder().append("BLE").append("_Ambient_temp").append(runningTime.getDate()).append(".csv");
            filePath = path + File.separator + fileName.toString();
            data.add(new String[] {
                    new StringBuilder().append("Sensor Name").toString(),
                    new StringBuilder().append("Sensor Reading").toString()});
        }
    }


    public void configureService() {

        int error = mBleService.writeCharacteristic(this.configC, (byte)0x01);

        if (error != 0) {
            if (this.configC != null) {
                // Log.d("SensorTagAmbientTemperatureProfile","Sensor config failed: " + this.configC.getUuid().toString() + " Error: " + error);
            }

        }
        error = this.mBleService.setCharacteristicNotification(this.dataC, true);
        if (error != 0) {
            if (this.dataC != null) {
                // Log.d("SensorTagAmbientTemperatureProfile","Sensor notification enable failed: " + this.configC.getUuid().toString() + " Error: " + error);
            }

        }
        /*
		if (mBTLeService.writeCharacteristic(this.configC, (byte)0x01)) {
			mBTLeService.waitIdle(GATT_TIMEOUT);
		} else {
			Log.d("SensorTagAmbientTemperatureProfile","Sensor config failed: " + this.configC.getUuid().toString());
        }
        */

        this.isConfigured = true;
    }

    public void deConfigureService() {

        int error = mBleService.writeCharacteristic(this.configC, (byte)0x00);

        if (error != 0) {
            if (this.configC != null) {

                // Log.d("SensorTagAmbientTemperatureProfile","Sensor config failed: " + this.configC.getUuid().toString() + " Error: " + error);
            }

        }
        error = this.mBleService.setCharacteristicNotification(this.dataC, false);
        if (error != 0) {
            if (this.dataC != null) {

                // Log.d("SensorTagAmbientTemperatureProfile","Sensor notification enable failed: " + this.configC.getUuid().toString() + " Error: " + error);
            }

        }
        this.isConfigured = false;
    }

    @Override
    public void didUpdateValueForCharacteristic(BluetoothGattCharacteristic c) {

        // Save the data for the first 60 counts, then every 60 counts save once.
        int count = 0;

        byte[] value = c.getValue();

        if (c.equals(this.dataC)){

            Point3D v = Sensor.IR_TEMPERATURE.convert(value);

            if (this.tRow.config == false) {

                if ((this.isEnabledByPrefs("imperial")) == true) {
                    this.tRow.value.setText(String.format("%.1f'F", (v.x * 1.8) + 32));

                    if ((count % 60) == 0) {

                        data.add(new String[] {
                                new StringBuilder().append("Sensor Ambient Temp: ").toString(),
                                new StringBuilder().append(String.format("%.1f'F", (v.x * 1.8) + 32)).toString()});
                    }

                }

                else {

                    this.tRow.value.setText(String.format("%.1f'C", v.x));

                    if ((count % 60) == 0) {

                        data.add(new String[] {
                                new StringBuilder().append("Sensor Ambient Temp: ").toString(),
                                new StringBuilder().append(String.format("%.1f'C", v.x)).toString()});
                    }

                }
            }

            this.tRow.x.addValue((float)v.x);

            if ((count % 60) == 0) {
                try {

                    writer = new CSVWriter(new FileWriter(filePath));

                } catch (IOException e) {

                    e.printStackTrace();
                }

                writer.writeAll(data);

                try {

                    writer.close();
                } catch (IOException e) {

                    e.printStackTrace();

                }
            }

            count++;
        }
    }

    public static boolean isCorrectService(BluetoothGattService service) {

        if ((service.getUuid().toString().compareTo(SensorGatt.UUID_IRT_SERV.toString())) == 0) {
            return true;
        }
        else return false;
    }

    @Override
    public Map<String,String> getMQTTMap() {

        Point3D v = Sensor.IR_TEMPERATURE.convert(this.dataC.getValue());
        Map<String,String> map = new HashMap<String, String>();
        map.put("ambient_temp",String.format("%.2f",v.x));
        return map;
    }
}
