package com.google.heartrate.androidos.app.gatt.heartrate;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import com.google.heartrate.androidos.app.gatt.GattException;
import com.google.heartrate.androidos.app.gatt.GattCharacteristicManager;

import java.util.UUID;

/**
 * {@link HeartRateMeasurementCharacteristicManager} is class provides methods for
 * parsing gatt responses with Heart Rate Measurement characteristic data.
 * <p>
 * Fields included in the HeartRateMeasurement characteristic data:
 * <table border="1">
 *  <thead align="center">
 *      <tr><th>Field</th><th>Requirement</th><th>Format</th></tr>
 *  <thead>
 *  <tbody align="center">
 *      <tr><td> Flags </td><td> Mandatory </td><td> UInt8 </td></tr>
 *      <tr><td> Heart Rate Measurement </td><td> Mandatory </td><td> UInt8/UInt16 </td></tr>
 *      <tr><td> Energy Expended </td><td> Optional, Mandatory if Energy Expended flag </td><td> UInt16 </td></tr>
 *      <tr><td> RR-Interval </td><td> Optional, Not supported </td><td> - </td></tr>
 *      <tr><td> Transmission Interval </td><td> Optional, Not supported </td><td> - </td></tr>
 *  </tbody>
 * </table>
 * <p>
 * Flags bits (from right to left) according to characteristic values:
 * <table border="1">
 *  <thead align="center">
 *      <tr><th></th><th>Heart Rate Format</th><th>Sensor Contact Status</th><th>Energy Expended</th><th>RR-Interval</th><th>Unused</th></tr>
 *  <thead>
 *  <tbody align="center">
 *      <tr><td> Value </td>
 *          <td> 0 / 1 </td><td> 00 </td><td> 0 / 1 </td><td> 0 </td><td> 000 </td></tr>
 *      <tr><td> Meaning </td>
 *          <td> UInt8 / UInt16 </td><td> Not Supported  </td><td> Not Present / Present </td>
 *          <td> Not Present </td><td> Unused </td></tr>
 *  </tbody>
 * </table>
 * <p>
 * See <a href="https://www.bluetooth.com/wp-content/uploads/Sitecore-Media-Library/Gatt/Xml/Characteristics/org.bluetooth.characteristic.heart_rate_measurement.xml".>
 * Heart Rate Measurement Characteristic</a>.
 */
public class HeartRateMeasurementCharacteristicManager {
    private static final String TAG = HeartRateMeasurementCharacteristicManager.class.getCanonicalName();

    /** Heart Rate service UUID. */
    private static final UUID HEART_RATE_SERVICE_UUID = UUID
            .fromString("0000180d-0000-1000-8000-00805f9b34fb");

    /** Heart Rate Measurement characteristic UUID. */
    public static final UUID HEART_RATE_MEASUREMENT_UUID = UUID
            .fromString("00002a37-0000-1000-8000-00805f9b34fb");

    /** Client Characteristic Configuration descriptor UUID.  */
    private static final UUID CLIENT_CHARACTERISTIC_CONFIGURATION_UUID = UUID
            .fromString("00002902-0000-1000-8000-00805f9b34fb");

    /** Indicates that the data format of the Heart Rate Measurement value field is in a format of UINT8.  */
    private static final byte HEART_RATE_MEASUREMENT_UINT8_FLAG = 0b00000000;

    /** Indicates that the data format of the Heart Rate Measurement value field is in a format of UINT16. */
    private static final byte HEART_RATE_MEASUREMENT_UINT16_FLAG = 0b00000001;

    /** Indicates that the Energy Expended field is present in the Heart Rate Measurement characteristic. */
    private static final byte EXPENDED_ENERGY_FLAG = 0b00001000;


    /** Offset at which the Heart Rate Measurement characteristic flags are stored. */
    private static final int FLAGS_OFFSET = 0;

    /** Offset at which Heart Rate Measurement value is stored. */
    private static final int HEART_RATE_MEASUREMENT_OFFSET = 1;

    /** Offset at which Expended Energy value is Heart Rate Measurement value field is in a format of UINT8. */
    private static final int EXPENDED_ENERGY_BASE_OFFSET = 2;

    /** Offset at which Expended Energy value is Heart Rate Measurement value field is in a format of UINT16. */
    private static final int EXPENDED_ENERGY_SHIFTED_OFFSET = 3;


    private HeartRateMeasurementCharacteristicManager() {}

    /**
     * Get heart rate measurement value.
     *
     * @return heart rate measurement value
     * @throws GattException in case of wrong format or offset for heart rate measurement value
     */
    public static int getHeartRateMeasurementValue(BluetoothGattCharacteristic characteristic) throws GattException {
        Log.i(TAG, "Get heart rate measurement value");

        assertIsHeartRateMeasurementCharacteristic(characteristic);
        int format = isUInt16HeartRateFormat(characteristic) ?
                BluetoothGattCharacteristic.FORMAT_UINT16 :
                BluetoothGattCharacteristic.FORMAT_UINT8;

        int heartRateMeasurement = GattCharacteristicManager.getIntValue(characteristic, format, HEART_RATE_MEASUREMENT_OFFSET);

        Log.v(TAG, String.format("Heart rate format=%d value=%d", format, heartRateMeasurement));

        return heartRateMeasurement;
    }

    /**
     * Get expended energy value from characteristic.
     *
     * @param characteristic characteristic to get expended energy value from
     * @return expended energy value
     * @throws GattException if cannot get expended energy value
     */
    public static int getExpendedEnergyValue(BluetoothGattCharacteristic characteristic) throws GattException {
        Log.i(TAG, "Parse expended energy value");

        assertIsHeartRateMeasurementCharacteristic(characteristic);
        assertExpendedEnergyIsPresent(characteristic);

        /* HR is in UInt8 format => EE offset is base */
        /* HR is in UInt16 format => EE offset is shifted */
        int expendedEnergyOffset = isUInt16HeartRateFormat(characteristic) ?
                EXPENDED_ENERGY_SHIFTED_OFFSET :
                EXPENDED_ENERGY_BASE_OFFSET;

        int expendedEnergy = GattCharacteristicManager.getIntValue(
                characteristic,
                BluetoothGattCharacteristic.FORMAT_UINT16,
                expendedEnergyOffset);

        Log.v(TAG, String.format("Expended energy offset=%d value=%d", expendedEnergyOffset, expendedEnergy));

        return expendedEnergy;
    }

    /**
     * Get flags from Heart Rate Measurement characteristic.
     *
     * @param characteristic Heart Rate Measurement characteristic to get flags from
     * @return Heart Rate Measurement characteristic flags
     * @throws GattException if cannot get flags from given characteristic
     */
    private static int getFlags(BluetoothGattCharacteristic characteristic) throws GattException {
        return GattCharacteristicManager.getIntValue(
                characteristic,
                BluetoothGattCharacteristic.FORMAT_UINT8,
                FLAGS_OFFSET);
    }

    /**
     * Assert given characteristic is Heart Rate characteristic.
     *
     * @param characteristic characteristic ti check
     * @throws GattException if given characteristic not a Heart Rate characteristic
     */
    private static void assertIsHeartRateMeasurementCharacteristic(BluetoothGattCharacteristic characteristic) throws GattException {
        if (characteristic.getUuid() != HEART_RATE_MEASUREMENT_UUID) {
            throw new GattException(String.format("Not a heart rate measurement characteristic: got uuid = %s, expected %s",
                    characteristic.getUuid(), HEART_RATE_MEASUREMENT_UUID),
                    BluetoothGatt.GATT_FAILURE);
        }
    }

    /**
     * Assert Expended Energy is present.
     *
     * @param characteristic characteristic to check
     * @throws GattException if not present
     */
    private static void assertExpendedEnergyIsPresent(BluetoothGattCharacteristic characteristic) throws GattException {
        if (!isExpendedEnergyPresent(characteristic)) {
            throw new GattException("Expended energy value is not present",
                    BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED);
        }
    }

    /**
     * Determine whether the Heart Rate value is in UInt16 format or not.
     * <br>If Heart Rate Value Format bit in Flags Field is 1, then UInt16 format, otherwise UInt8.
     *
     * @param characteristic characteristic to check
     * @return true is UInt16, false if UInt8
     */
    private static boolean isUInt16HeartRateFormat(BluetoothGattCharacteristic characteristic) throws GattException {
        int flags = getFlags(characteristic);
        return (flags & HEART_RATE_MEASUREMENT_UINT16_FLAG) > 0;
    }

    /**
     * Determine whether the Energy Expended Field is present or not.
     * <p>If Energy Expended Status bit in Flags Field is 1, then is present, otherwise not.
     *
     * @param characteristic characteristic to check
     * @return true is UInt16, false if UInt8
     */
    private static boolean isExpendedEnergyPresent(BluetoothGattCharacteristic characteristic) throws GattException {
        int flags = getFlags(characteristic);
        return (flags & EXPENDED_ENERGY_FLAG) > 0;
    }

    /**
     * Get Heart Rate service UUID.
     *
     * @return Heart Rate service UUID
     */
    public static UUID getServiceUuid() {
        return HEART_RATE_SERVICE_UUID;
    }

    /**
     * Get Client Characteristic Configuration descriptor service UUID.
     *
     * @return Client Characteristic Configuration descriptor UUID
     */
    public static UUID getDescriptorUuid() {
        return CLIENT_CHARACTERISTIC_CONFIGURATION_UUID;
    }
}
