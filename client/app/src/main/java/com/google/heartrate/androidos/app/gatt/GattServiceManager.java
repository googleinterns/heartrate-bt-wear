package com.google.heartrate.androidos.app.gatt;

import android.bluetooth.BluetoothGattCharacteristic;

public class GattServiceManager {

    private GattServiceManager() {}

    /**
     * Get value from characteristic.
     *
     * @return value characteristic
     * @throws GattException if value can not be got
     */
    public static byte[] getValue(BluetoothGattCharacteristic characteristic) throws GattException {
        try {
            byte[] value = characteristic.getValue();
            if (value == null) {
                throw new GattException(String.format("Value can not be " +
                        "got from characteristic %s.", characteristic.getUuid()));
            }
            return value;
        } catch (Exception e) {
            throw new GattException(e);
        }
    }

    /**
     * Get int value from characteristic.
     *
     * @param format format at which the value should be get
     * @param offset offset at which the value should be get
     * @return int value in given format
     * @throws GattException if value can not be got
     */
    public static int getIntValue(BluetoothGattCharacteristic characteristic, int format, int offset) throws GattException {
        try {
            Integer value = characteristic.getIntValue(format, offset);
            if (value == null) {
                throw new GattException(String.format("Value in format %d with offset %d can not be " +
                        "got from characteristic %s.", format, offset, characteristic.getUuid()));
            }
            return value;
        } catch (Exception e) {
            throw new GattException(e);
        }
    }
}
