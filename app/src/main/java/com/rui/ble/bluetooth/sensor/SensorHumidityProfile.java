package com.rui.ble.bluetooth.sensor;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import com.opencsv.CSVWriter;
import com.rui.ble.bluetooth.common.BleService;
import com.rui.ble.bluetooth.common.GattInfo;
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
import java.util.UUID;

/**
 * Created by rhuang on 8/19/16.
 */
public class SensorHumidityProfile extends GenericBtProfile {

    // Data file stored
    private File dir = null;
    private File subDir = null;
    private String path = null;
    private StringBuilder fileName = null;
    private String filePath = null;
    private RunningTime runningTime = new RunningTime();

    private CSVWriter writer = null;

    private List<String[]> data = new ArrayList<String[]>();

    public SensorHumidityProfile(Context context, BluetoothDevice device, BluetoothGattService service, BleService controller) {
        super(context, device, service, controller);

        this.tRow =  new GenericCharacteristicTableRow(context);

        List<BluetoothGattCharacteristic> characteristics = this.mBtGattService.getCharacteristics();

        for (BluetoothGattCharacteristic c : characteristics) {

            if (c.getUuid().toString().equals(SensorGatt.UUID_HUM_DATA.toString())) {
                this.dataC = c;
            }
            if (c.getUuid().toString().equals(SensorGatt.UUID_HUM_CONF.toString())) {
                this.configC = c;
            }
            if (c.getUuid().toString().equals(SensorGatt.UUID_HUM_PERI.toString())) {
                this.periodC = c;
            }
        }

        this.tRow.setIcon(this.getIconPrefix(), this.dataC.getUuid().toString(), "humidity");
        this.tRow.title.setTextSize(18);
        this.tRow.title.setText(GattInfo.uuidToName(UUID.fromString(this.dataC.getUuid().toString())));
        this.tRow.uuidLabel.setText(this.dataC.getUuid().toString());
        this.tRow.value.setText("0.0%rH");
        this.tRow.periodBar.setProgress(100);

        dir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/BLE");
        if (dir.exists()) {
            // create sub dir for this sensor data
            subDir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/BLE/humidity");
            subDir.mkdir();

            path = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/BLE/humidity";
            fileName = new StringBuilder().append("BLE").append("_humidity").append(runningTime.getDate()).append(".csv");
            filePath = path + File.separator + fileName.toString();
            data.add(new String[] {
                    new StringBuilder().append("Sensor Name").toString(),
                    new StringBuilder().append("Sensor Reading").toString()});
        }
    }

    public static boolean isCorrectService(BluetoothGattService service) {
        if ((service.getUuid().toString().compareTo(SensorGatt.UUID_HUM_SERV.toString())) == 0) {
            //service.getUuid().toString().compareTo(SensorTagGatt.UUID_HUM_DATA.toString())) {
            Log.d("Test", "Match !");
            return true;
        }
        else return false;
    }

    public void didWriteValueForCharacteristic(BluetoothGattCharacteristic c) {

    }
    public void didReadValueForCharacteristic(BluetoothGattCharacteristic c) {

    }
    @Override
    public void didUpdateValueForCharacteristic(BluetoothGattCharacteristic c) {

        // Save the data for the first 60 counts, then every 60 counts save once.
        int count = 0;

        byte[] value = c.getValue();
        if (c.equals(this.dataC)) {

        }
        Point3D v = Sensor.HUMIDITY.convert(value);

        if (this.tRow.config == false) {

            this.tRow.value.setText(String.format("%.1f %%rH", v.x));
            if ((count % 60) == 0) {

                data.add(new String[] {
                        new StringBuilder().append(String.format("%.1f %%rH", v.x)).toString()});
            }
        }

        this.tRow.x.maxVal = 100;
        this.tRow.y.minVal = 0;
        this.tRow.z.addValue((float)v.x);

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


    @Override
    public Map<String,String> getMQTTMap() {
        Point3D v = Sensor.HUMIDITY.convert(this.dataC.getValue());
        Map<String,String> map = new HashMap<String, String>();
        map.put("humidity",String.format("%.2f",v.x));
        return map;
    }
}
