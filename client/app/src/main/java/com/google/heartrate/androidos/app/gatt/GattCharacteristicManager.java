package com.google.heartrate.androidos.app.gatt;

import android.bluetooth.BluetoothGattCharacteristic;

/**
 * {@link GattCharacteristicManager} class provides method for safety exacting byte array data from
 * different {@link BluetoothGattCharacteristic}.
 */
public class GattCharacteristicManager {

    private GattCharacteristicManager() {}

    /**
     * Get value from characteristic.
     *
     * @param characteristic to get value from
     * @return value characteristic
     * @throws GattException if value can not be got
     */
    public static byte[] getValue(BluetoothGattCharacteristic characteristic) throws GattException {
        assertCharacteristicIsReadable(characteristic);

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
     * @param characteristic to get value from
     * @param format format at which the value should be get
     * @param offset offset at which the value should be get
     * @return int value in given format
     * @throws GattException if value can not be got
     */
    public static int getIntValue(BluetoothGattCharacteristic characteristic, int format, int offset) throws GattException {
        assertCharacteristicIsReadable(characteristic);

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

    /**
     * Assert if characteristic has read permissions.
     *
     * @param characteristic characteristic to check
     * @throws GattException if characteristic has no read permissions
     */
    private static void assertCharacteristicIsReadable(BluetoothGattCharacteristic characteristic) throws GattException {
        if ((characteristic.getPermissions() & BluetoothGattCharacteristic.PROPERTY_READ) == 0) {
            throw new GattException(String.format("Given characteristic is not readable " +
                    "got from characteristic %s.", characteristic.getUuid()));
        }
    }
}
