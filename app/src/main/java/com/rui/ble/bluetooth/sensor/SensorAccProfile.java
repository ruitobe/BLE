package com.rui.ble.bluetooth.sensor;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.view.View;

import com.rui.ble.bluetooth.common.BleService;
import com.rui.ble.bluetooth.common.GattInfo;
import com.rui.ble.bluetooth.common.GenericBtProfile;
import com.rui.ble.util.GenericCharacteristicTableRow;
import com.rui.ble.util.Point3D;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by rhuang on 8/19/16.
 */
public class SensorAccProfile extends GenericBtProfile {

    public SensorAccProfile(Context con, BluetoothDevice device, BluetoothGattService service, BleService controller) {

        super(con,device,service,controller);
        this.tRow =  new GenericCharacteristicTableRow(con);

        List<BluetoothGattCharacteristic> characteristics = this.mBtGattService.getCharacteristics();
        for (BluetoothGattCharacteristic c : characteristics) {

            if (c.getUuid().toString().equals(SensorGatt.UUID_ACC_DATA.toString())) {
                this.dataC = c;
            }
            if (c.getUuid().toString().equals(SensorGatt.UUID_ACC_CONF.toString())) {
                this.configC = c;
            }
            if (c.getUuid().toString().equals(SensorGatt.UUID_ACC_PERI.toString())) {
                this.periodC = c;
            }
        }

        this.tRow.x.autoScale = true;
        this.tRow.x.autoScaleBounceBack = true;

        this.tRow.y.autoScale = true;
        this.tRow.y.autoScaleBounceBack = true;
        this.tRow.y.setColor(255, 0, 150, 125);
        this.tRow.y.setVisibility(View.VISIBLE);
        this.tRow.y.setEnabled(true);

        this.tRow.z.autoScale = true;
        this.tRow.z.autoScaleBounceBack = true;
        this.tRow.z.setColor(255, 0, 0, 0);
        this.tRow.z.setVisibility(View.VISIBLE);
        this.tRow.z.setEnabled(true);

        this.tRow.setIcon(this.getIconPrefix(), this.dataC.getUuid().toString());

        this.tRow.title.setText(GattInfo.uuidToName(UUID.fromString(this.dataC.getUuid().toString())));
        this.tRow.uuidLabel.setText(this.dataC.getUuid().toString());
        this.tRow.value.setText("X:0.00G, Y:0.00G, Z:0.00G");

    }

    public static boolean isCorrectService(BluetoothGattService service) {
        if ((service.getUuid().toString().compareTo(SensorGatt.UUID_ACC_SERV.toString())) == 0) {
            return true;
        }
        else return false;
    }

    @Override
    public void didUpdateValueForCharacteristic(BluetoothGattCharacteristic c) {

        if (c.equals(this.dataC)){

            Point3D v = Sensor.ACCELEROMETER.convert(this.dataC.getValue());
            if (this.tRow.config == false)
                this.tRow.value.setText(String.format("X:%.2fG, Y:%.2fG, Z:%.2fG", v.x, v.y, v.z));

            this.tRow.x.addValue((float)v.x);
            this.tRow.y.addValue((float)v.y);
            this.tRow.z.addValue((float)v.z);
        }
    }
    @Override
    public Map<String,String> getMQTTMap() {
        Point3D v = Sensor.ACCELEROMETER.convert(this.dataC.getValue());
        Map<String,String> map = new HashMap<String, String>();
        map.put("acc_x", String.format("%.2f", v.x));
        map.put("acc_y",String.format("%.2f",v.y));
        map.put("acc_z",String.format("%.2f",v.z));
        return map;
    }

}
