package com.rui.ble.bluetooth.sensor;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

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
public class SensorLuxProfile extends GenericBtProfile {

    public SensorLuxProfile(Context context, BluetoothDevice device, BluetoothGattService service, BleService controller) {
        super(context, device, service, controller);
        this.tRow =  new GenericCharacteristicTableRow(context);

        List<BluetoothGattCharacteristic> characteristics = this.mBtGattService.getCharacteristics();

        for (BluetoothGattCharacteristic c : characteristics) {
            if (c.getUuid().toString().equals(SensorGatt.UUID_OPT_DATA.toString())) {
                this.dataC = c;
            }
            if (c.getUuid().toString().equals(SensorGatt.UUID_OPT_CONF.toString())) {
                this.configC = c;
            }
            if (c.getUuid().toString().equals(SensorGatt.UUID_OPT_PERI.toString())) {
                this.periodC = c;
            }
        }

        this.tRow.x.autoScale = true;
        this.tRow.y.autoScaleBounceBack = true;
        this.tRow.z.setColor(255, 0, 150, 125);
        this.tRow.setIcon(this.getIconPrefix(), this.dataC.getUuid().toString());

        this.tRow.title.setText(GattInfo.uuidToName(UUID.fromString(this.dataC.getUuid().toString())));
        this.tRow.uuidLabel.setText(this.dataC.getUuid().toString());
        this.tRow.value.setText("0.0 Lux");
        this.tRow.periodBar.setProgress(100);
    }

    public static boolean isCorrectService(BluetoothGattService service) {
        if ((service.getUuid().toString().compareTo(SensorGatt.UUID_OPT_SERV.toString())) == 0) {
            return true;
        }
        else return false;
    }
    @Override
    public void didUpdateValueForCharacteristic(BluetoothGattCharacteristic c) {

        byte[] value = c.getValue();

        if (c.equals(this.dataC)){

            Point3D v = Sensor.LUXOMETER.convert(value);
            if (this.tRow.config == false) this.tRow.value.setText(String.format("%.1f Lux", v.x));
            this.tRow.x.addValue((float)v.x);
        }
    }

    @Override
    public Map<String,String> getMQTTMap() {
        Point3D v = Sensor.LUXOMETER.convert(this.dataC.getValue());
        Map<String, String> map = new HashMap<String, String>();
        map.put("light", String.format("%.2f", v.x));
        return map;
    }
}
