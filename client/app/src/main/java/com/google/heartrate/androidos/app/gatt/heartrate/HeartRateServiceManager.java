package com.google.heartrate.androidos.app.gatt.heartrate;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import com.google.heartrate.androidos.app.gatt.GattException;
import com.google.heartrate.androidos.app.gatt.GattServiceManager;

import java.util.UUID;

/**
 * {@link HeartRateServiceManager} is class provides methods for
 * parsing gatt responses to HeartRateService data. HeartRateService has three characteristics:
 * Heart Rate Measurement characteristic with Client Characteristic Configuration Descriptor,
 * Heart Rate Control Point Characteristic and Body Sensor Location Characteristic
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
public class HeartRateServiceManager {
    private static final String TAG = HeartRateServiceManager.class.getCanonicalName();

    /** Heart Rate service UUID. */
    public static final UUID HEART_RATE_SERVICE_UUID = UUID
            .fromString("0000180d-0000-1000-8000-00805f9b34fb");

    /** Heart Rate Measurement characteristic UUID. */
    public static final UUID HEART_RATE_MEASUREMENT_UUID = UUID
            .fromString("00002a37-0000-1000-8000-00805f9b34fb");

    /** Client Characteristic Configuration descriptor UUID.  */
    public static final UUID CLIENT_CHARACTERISTIC_CONFIGURATION_UUID = UUID
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


    /**
     * Get heart rate measurement value.
     *
     * @return heart rate measurement value
     * @throws GattException in case of wrong format or offset for heart rate measurement value
     */
    public static int getHeartRateMeasurementValue(BluetoothGattCharacteristic characteristic) throws GattException {
        Log.i(TAG, "Get heart rate measurement value");

        int format = isUInt16HeartRateFormat(characteristic) ?
                BluetoothGattCharacteristic.FORMAT_UINT16 :
                BluetoothGattCharacteristic.FORMAT_UINT8;

        int heartRateMeasurement = GattServiceManager.getIntValue(characteristic, format, HEART_RATE_MEASUREMENT_OFFSET);

        Log.v(TAG, String.format("Heart rate format=%d value=%d", format, heartRateMeasurement));

        return heartRateMeasurement;
    }


    public static int getExpendedEnergyValue(BluetoothGattCharacteristic characteristic) throws GattException {
        Log.i(TAG, "Parse expended energy value");

        assertExpendedEnergyIsPresent(characteristic);

        /* HR is in UInt8 format => EE offset is base */
        /* HR is in UInt16 format => EE offset is shifted */
        int expendedEnergyOffset = isUInt16HeartRateFormat(characteristic) ?
                EXPENDED_ENERGY_SHIFTED_OFFSET :
                EXPENDED_ENERGY_BASE_OFFSET;

        int expendedEnergy = GattServiceManager.getIntValue(
                characteristic,
                BluetoothGattCharacteristic.FORMAT_UINT16,
                expendedEnergyOffset);

        Log.v(TAG, String.format("Expended energy offset=%d value=%d", expendedEnergyOffset, expendedEnergy));

        return expendedEnergy;
    }

    private static void assertHeartRateMeasurementCharacteristic(BluetoothGattCharacteristic characteristic) throws GattException {
        if (characteristic.getUuid() != HEART_RATE_MEASUREMENT_UUID) {
            throw new GattException(String.format("Not a heart rate measurement characteristic: got uuid = %s, expected %s",
                    characteristic.getUuid(), HEART_RATE_MEASUREMENT_UUID),
                    BluetoothGatt.GATT_FAILURE);
        }
    }

    /**
     * Assert Expended Energy is present.
     *
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
     * @return true is UInt16, false if UInt8
     */
    private static boolean isUInt16HeartRateFormat(BluetoothGattCharacteristic characteristic) throws GattException {
        int flags = GattServiceManager.getIntValue(
                characteristic,
                BluetoothGattCharacteristic.FORMAT_UINT8,
                FLAGS_OFFSET);
        return (flags & HEART_RATE_MEASUREMENT_UINT16_FLAG) > 0;
    }

    /**
     * Determine whether the Energy Expended Field is present or not.
     * <p>If Energy Expended Status bit in Flags Field is 1, then is present, otherwise not.
     *
     * @return true is UInt16, false if UInt8
     */
    private static boolean isExpendedEnergyPresent(BluetoothGattCharacteristic characteristic) throws GattException {
        int flags = GattServiceManager.getIntValue(
                characteristic,
                BluetoothGattCharacteristic.FORMAT_UINT8,
                FLAGS_OFFSET);
        return (flags & EXPENDED_ENERGY_FLAG) > 0;
    }
}
