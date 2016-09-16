package com.rui.ble.bluetooth.sensor;

import android.bluetooth.BluetoothGattCharacteristic;

import static com.rui.ble.bluetooth.sensor.SensorGatt.*;

import com.rui.ble.bluetooth.util.Point3D;

import java.util.List;
import java.util.UUID;

import static java.lang.Math.pow;

/**
 * Created by rhuang on 8/19/16.
 */
public enum Sensor {

    IR_TEMPERATURE(UUID_IRT_SERV, UUID_IRT_DATA, UUID_IRT_CONF) {

        @Override
        public Point3D convert(final byte [] value) {


            // The IR Temperature sensor produces two measurements;
            // Object ( AKA target or IR) Temperature, and Ambient ( AKA die ) temperature.
            // Both need some conversion, and Object temperature is dependent on Ambient temperature.
            // They are stored as [ObjLSB, ObjMSB, AmbLSB, AmbMSB] (4 bytes)
            // Which means we need to shift the bytes around to get the correct values.
            //

            double ambient = extractAmbientTemperature(value);
            double target = extractTargetTemperature(value, ambient);
            double targetNewSensor = extractTargetTemperatureTMP007(value);

            return new Point3D(ambient, target, targetNewSensor);
        }

        private double extractAmbientTemperature(byte [] v) {

            int offset = 2;
            return shortUnsignedAtOffset(v, offset) / 128.0;
        }

        private double extractTargetTemperature(byte [] v, double ambient) {

            Integer twoByteValue = shortSignedAtOffset(v, 0);

            double Vobj2 = twoByteValue.doubleValue();

            Vobj2 *= 0.00000015625;

            double Tdie = ambient + 273.15;

            double S0 = 5.593E-14; // Calibration factor
            double a1 = 1.75E-3;
            double a2 = -1.678E-5;
            double b0 = -2.94E-5;
            double b1 = -5.7E-7;
            double b2 = 4.63E-9;
            double c2 = 13.4;
            double Tref = 298.15;
            double S = S0 * (1 + a1 * (Tdie - Tref) + a2 * pow((Tdie - Tref), 2));
            double Vos = b0 + b1 * (Tdie - Tref) + b2 * pow((Tdie - Tref), 2);
            double fObj = (Vobj2 - Vos) + c2 * pow((Vobj2 - Vos), 2);
            double tObj = pow(pow(Tdie, 4) + (fObj / S), .25);

            return tObj - 273.15;
        }

        private double extractTargetTemperatureTMP007(byte [] v) {
            int offset = 0;
            return shortUnsignedAtOffset(v, offset) / 128.0;
        }
    },

    MOVEMENT_ACC(UUID_MOV_SERV, UUID_MOV_DATA, UUID_MOV_CONF, (byte)3) {

        @Override
        public Point3D convert(final byte[] value) {
            // Range 8G
            final float SCALE = (float) 4096.0;

            int x = (value[7] << 8) + value[6];
            int y = (value[9] << 8) + value[8];
            int z = (value[11] << 8) + value[10];
            return new Point3D(((x / SCALE) * -1), y / SCALE, ((z / SCALE) * -1));
        }
    },


    MOVEMENT_GYRO(UUID_MOV_SERV, UUID_MOV_DATA, UUID_MOV_CONF, (byte)3) {

        @Override
        public Point3D convert(final byte[] value) {

            final float SCALE = (float) 128.0;

            int x = (value[1] << 8) + value[0];
            int y = (value[3] << 8) + value[2];
            int z = (value[5] << 8) + value[4];
            return new Point3D(x / SCALE, y / SCALE, z / SCALE);
        }
    },

    MOVEMENT_MAG(UUID_MOV_SERV, UUID_MOV_DATA, UUID_MOV_CONF, (byte)3) {

        @Override
        public Point3D convert(final byte[] value) {

            final float SCALE = (float) (32768 / 4912);

            if (value.length >= 18) {
                int x = (value[13] << 8) + value[12];
                int y = (value[15] << 8) + value[14];
                int z = (value[17] << 8) + value[16];
                return new Point3D(x / SCALE, y / SCALE, z / SCALE);
            }
            else return new Point3D(0, 0, 0);
        }
    },

    ACCELEROMETER(UUID_ACC_SERV, UUID_ACC_DATA, UUID_ACC_CONF, (byte)3) {

        @Override
        public Point3D convert(final byte[] value) {

			 // The accelerometer has the range [-2g, 2g] with unit (1/64)g.
			 // To convert from unit (1/64)g to unit g we divide by 64.
			 // (g = 9.81 m/s^2)
			 // The z value is multiplied with -1 to coincide with how we have arbitrarily defined the positive y direction.
			 // (illustrated by the apps accelerometer image)


            DevActivity da = DevActivity.getInstance();

            if (da.isSensor2()) {

                // Range 8G
                final float SCALE = (float) 4096.0;

                int x = (value[0] << 8) + value[1];
                int y = (value[2] << 8) + value[3];
                int z = (value[4] << 8) + value[5];

                return new Point3D(x / SCALE, y / SCALE, z / SCALE);
            } else {

                Point3D v;
                Integer x = (int) value[0];
                Integer y = (int) value[1];
                Integer z = (int) value[2] * -1;

                if (da.firmwareRevision().contains("1.5"))
                {
                    // Range 8G
                    final float SCALE = (float) 64.0;
                    v = new Point3D(x / SCALE, y / SCALE, z / SCALE);
                } else {
                    // Range 2G
                    final float SCALE = (float) 16.0;
                    v = new Point3D(x / SCALE, y / SCALE, z / SCALE);
                }
                return v;
            }

        }
    },

    HUMIDITY(UUID_HUM_SERV, UUID_HUM_DATA, UUID_HUM_CONF) {

        @Override
        public Point3D convert(final byte[] value) {

            int a = shortUnsignedAtOffset(value, 2);
            // bits [1..0] are status bits and need to be cleared according
            // to the user guide, but the iOS code doesn't bother. It should
            // have minimal impact.
            a = a - (a % 4);

            return new Point3D((-6f) + 125f * (a / 65535f), 0, 0);
        }
    },

    MAGNETOMETER(UUID_MAG_SERV, UUID_MAG_DATA, UUID_MAG_CONF) {

        @Override
        public Point3D convert(final byte [] value) {

            Point3D mcal = MagnetometerCalibrationCoefficients.INSTANCE.val;

            // Multiply x and y with -1 so that the values correspond with the image in the app
            float x = shortSignedAtOffset(value, 0) * (2000f / 65536f) * -1;
            float y = shortSignedAtOffset(value, 2) * (2000f / 65536f) * -1;
            float z = shortSignedAtOffset(value, 4) * (2000f / 65536f);

            return new Point3D(x - mcal.x, y - mcal.y, z - mcal.z);
        }
    },

    LUXOMETER(UUID_OPT_SERV, UUID_OPT_DATA, UUID_OPT_CONF) {

        @Override
        public Point3D convert(final byte [] value) {
            int mantissa;
            int exponent;
            Integer sfloat= shortUnsignedAtOffset(value, 0);

            mantissa = sfloat & 0x0FFF;
            exponent = (sfloat >> 12) & 0xFF;

            double output;
            double magnitude = pow(2.0f, exponent);
            output = (mantissa * magnitude);

            return new Point3D(output / 100.0f, 0, 0);
        }
    },

    GYROSCOPE(UUID_GYR_SERV, UUID_GYR_DATA, UUID_GYR_CONF, (byte)7) {

        @Override
        public Point3D convert(final byte [] value) {

            float y = shortSignedAtOffset(value, 0) * (500f / 65536f) * -1;
            float x = shortSignedAtOffset(value, 2) * (500f / 65536f);
            float z = shortSignedAtOffset(value, 4) * (500f / 65536f);

            return new Point3D(x, y, z);
        }
    },

    BAROMETER(SensorGatt.UUID_BAR_SERV, SensorGatt.UUID_BAR_DATA, SensorGatt.UUID_BAR_CONF) {

        @Override
        public Point3D convert(final byte [] value) {

            if (DevActivity.getInstance().isSensor2()) {

                if (value.length > 4) {
                    Integer val = twentyFourBitUnsignedAtOffset(value, 2);
                    return new Point3D((double) val / 100.0, 0, 0);
                }
                else {
                    int mantissa;
                    int exponent;
                    Integer sfloat = shortUnsignedAtOffset(value, 2);

                    mantissa = sfloat & 0x0FFF;
                    exponent = (sfloat >> 12) & 0xFF;

                    double output;
                    double magnitude = pow(2.0f, exponent);
                    output = (mantissa * magnitude);
                    return new Point3D(output / 100.0f, 0, 0);
                }

            } else {

                List<Integer> barometerCalibrationCoefficients = BarometerCalibrationCoefficients.INSTANCE.barometerCalibrationCoefficients;

                if (barometerCalibrationCoefficients == null) {
                    // Log.w("Sensor", "Data notification arrived for barometer before it was calibrated.");
                    return new Point3D(0,0,0);
                }

                // Calibration coefficients
                final int[] c;
                // Temperature raw value from sensor
                final Integer t_r;
                // Pressure raw value from sensor
                final Integer p_r;
                // Interim value in calculation
                final Double S;
                // Interim value in calculation
                final Double O;
                // Pressure actual value in unit Pascal.
                final Double p_a;

                c = new int[barometerCalibrationCoefficients.size()];
                for (int i = 0; i < barometerCalibrationCoefficients.size(); i++) {
                    c[i] = barometerCalibrationCoefficients.get(i);
                }

                t_r = shortSignedAtOffset(value, 0);
                p_r = shortUnsignedAtOffset(value, 2);

                S = c[2] + c[3] * t_r / pow(2, 17) + ((c[4] * t_r / pow(2, 15)) * t_r) / pow(2, 19);
                O = c[5] * pow(2, 14) + c[6] * t_r / pow(2, 3) + ((c[7] * t_r / pow(2, 15)) * t_r) / pow(2, 4);
                p_a = (S * p_r + O) / pow(2, 14);

                return new Point3D(p_a,0,0);
            }
        }
    };


    /**
     * Gyroscope, Magnetometer, Barometer, IR temperature all store 16 bit two's complement values as LSB MSB, which cannot be directly parsed
     * as getIntValue(FORMAT_SINT16, offset) because the bytes are stored as little-endian.
     *
     * This function extracts these 16 bit two's complement values.
     * */
    private static Integer shortSignedAtOffset(byte[] c, int offset) {
        Integer lowerByte = (int) c[offset] & 0xFF;
        Integer upperByte = (int) c[offset+1]; // // Interpret MSB as signed
        return (upperByte << 8) + lowerByte;
    }

    private static Integer shortUnsignedAtOffset(byte[] c, int offset) {
        Integer lowerByte = (int) c[offset] & 0xFF;
        Integer upperByte = (int) c[offset+1] & 0xFF;
        return (upperByte << 8) + lowerByte;
    }
    private static Integer twentyFourBitUnsignedAtOffset(byte[] c, int offset) {
        Integer lowerByte = (int) c[offset] & 0xFF;
        Integer mediumByte = (int) c[offset+1] & 0xFF;
        Integer upperByte = (int) c[offset + 2] & 0xFF;
        return (upperByte << 16) + (mediumByte << 8) + lowerByte;
    }

    public void onCharacteristicChanged(BluetoothGattCharacteristic c) {
        throw new UnsupportedOperationException("Error: the individual enum classes are supposed to override this method.");
    }

    public Point3D convert(byte[] value) {
        throw new UnsupportedOperationException("Error: the individual enum classes are supposed to override this method.");
    }

    private final UUID service, data, config;
    // See getEnableSensorCode for explanation.
    private byte enableCode;
    public static final byte DISABLE_SENSOR_CODE = 0;
    public static final byte ENABLE_SENSOR_CODE = 1;
    public static final byte CALIBRATE_SENSOR_CODE = 2;

    /**
     * Constructor called by the Gyroscope and Accelerometer because it more than a boolean enable
     * code.
     */
    private Sensor(UUID service, UUID data, UUID config, byte enableCode) {
        this.service = service;
        this.data = data;
        this.config = config;
        this.enableCode = enableCode;
    }

    /**
     * Constructor called by all the sensors except Gyroscope
     * */
    private Sensor(UUID service, UUID data, UUID config) {
        this.service = service;
        this.data = data;
        this.config = config;
        // This is the sensor enable code for all sensors except the gyroscope
        this.enableCode = ENABLE_SENSOR_CODE;
    }

    public static final Sensor[] SENSOR_LIST = { IR_TEMPERATURE, ACCELEROMETER, MAGNETOMETER,
            LUXOMETER, GYROSCOPE, HUMIDITY, BAROMETER };

}
