package com.rui.ble.bluetooth.sensor;

import java.util.List;

/**
 * Created by rhuang on 8/19/16.
 */
public enum BarometerCalibrationCoefficients {
    INSTANCE;
    volatile public List<Integer> barometerCalibrationCoefficients;
    volatile public double heightCalibration;
}
