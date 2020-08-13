package com.google.heartrate.wearos.app.gatt.heartrate.characteristics;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import com.google.heartrate.wearos.app.gatt.FormatUtils;
import com.google.heartrate.wearos.app.gatt.GattException;
import com.google.heartrate.wearos.app.gatt.attributes.GattCharacteristic;
import com.google.heartrate.wearos.app.gatt.attributes.GattDescriptor;
import com.google.heartrate.wearos.app.gatt.heartrate.descriptors.ClientCharacteristicConfigurationDescriptor;

import java.util.Optional;
import java.util.UUID;

/**
 * {@link HeartRateMeasurementCharacteristic} class inherits {@link GattCharacteristic}
 * and specifies set/get operation for Heart Rate Measurement Characteristic characteristic.
 * <p>
 * Fields included in the characteristic:
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
public class HeartRateMeasurementCharacteristic extends GattCharacteristic {
    private static final String TAG = HeartRateMeasurementCharacteristic.class.getCanonicalName();

    /** Heart Rate Measurement characteristic UUID. */
    private static final UUID HEART_RATE_MEASUREMENT_UUID = UUID
            .fromString("00002a37-0000-1000-8000-00805f9b34fb");

    /** Indicates that the data format of the Heart Rate Measurement value field is in a format of UINT8.  */
    private static final byte HEART_RATE_MEASUREMENT_UINT8_FLAG = 0b00000000;

    /** Indicates that the data format of the Heart Rate Measurement value field is in a format of UINT16. */
    private static final byte HEART_RATE_MEASUREMENT_UINT16_FLAG = 0b00000001;

    /** Indicates that the Energy Expended field is present in the Heart Rate Measurement characteristic. */
    private static final byte EXPENDED_ENERGY_FLAG = 0b00001000;


    /** Number of bytes in characteristic value byte array containing flags. */
    private static final int FLAGS_SIZE = 1;

    /** Number of bytes in characteristic value byte array containing heart rate measurement in UInt8 format. */
    private static final int HEART_RATE_MEASUREMENT_UINT8_SIZE = 1;

    /** Number of bytes in characteristic value byte array containing heart rate measurement in UInt16 format. */
    private static final int HEART_RATE_MEASUREMENT_UINT16_SIZE = 2;

    /** Number of bytes in characteristic value byte array containing expended energy in UInt16 format. */
    private static final int EXPENDED_ENERGY_SIZE = 2;


    /** Offset at which the Heart Rate Measurement characteristic flags are stored. */
    private static final int FLAGS_OFFSET = 0;

    /** Offset at which Heart Rate Measurement value is stored. */
    private static final int HEART_RATE_MEASUREMENT_OFFSET =
            FLAGS_OFFSET + FLAGS_SIZE;

    /** Offset at which Expended Energy value is Heart Rate Measurement value field is in a format of UINT8. */
    private static final int EXPENDED_ENERGY_BASE_OFFSET =
            HEART_RATE_MEASUREMENT_OFFSET + HEART_RATE_MEASUREMENT_UINT8_SIZE;

    /** Offset at which Expended Energy value is Heart Rate Measurement value field is in a format of UINT16. */
    private static final int EXPENDED_ENERGY_SHIFTED_OFFSET =
            HEART_RATE_MEASUREMENT_OFFSET + HEART_RATE_MEASUREMENT_UINT16_SIZE;


    /**
     * Create {@link GattCharacteristic} for Heart Rate Measurement characteristic.
     * <br>Configure {@link GattCharacteristic} with Heart Rate Measurement characteristic UUID,
     * property for notification, no read/write permissions and Client Characteristic Configuration descriptor.
     */
    public HeartRateMeasurementCharacteristic() {
        super(HEART_RATE_MEASUREMENT_UUID,
                BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                /* No permissions */ 0,
                new GattDescriptor[] {new ClientCharacteristicConfigurationDescriptor()});
    }

    /**
     * Get first byte with flag from current characteristic value.
     *
     * @return flag byte
     * @throws GattException if cannot get flags from value
     */
    public byte getFlags() throws GattException {
        return (byte) getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, FLAGS_OFFSET);
    }

    /**
     * Get heart rate measurement value.
     *
     * @return heart rate measurement value
     * @throws GattException in case of wrong format or offset for heart rate measurement value
     */
    public int getHeartRateMeasurementValue() throws GattException {
        Log.i(TAG, "Get heart rate measurement value");

        int format = isUInt16HeartRateFormat() ?
                BluetoothGattCharacteristic.FORMAT_UINT16 :
                BluetoothGattCharacteristic.FORMAT_UINT8;

        int heartRateMeasurement = getIntValue(format, HEART_RATE_MEASUREMENT_OFFSET);

        Log.v(TAG, String.format("Heart rate format=%d value=%d", format, heartRateMeasurement));

        return heartRateMeasurement;
    }

    /**
     * Get expended energy value.
     *
     * @return expended energy value
     * @throws GattException if expended energy is not present or in case of wrong format or offset for expended energy value
     */
    public int getExpendedEnergyValue() throws GattException {
        Log.i(TAG, "Get expended energy value");

        assertExpendedEnergyIsPresent();

        /* HR is in UInt8 format => EE offset is base */
        /* HR is in UInt16 format => EE offset is shifted */
        int expendedEnergyOffset = isUInt16HeartRateFormat() ?
                EXPENDED_ENERGY_SHIFTED_OFFSET :
                EXPENDED_ENERGY_BASE_OFFSET;

        int expendedEnergy = getIntValue(
                BluetoothGattCharacteristic.FORMAT_UINT16,
                expendedEnergyOffset);

        Log.v(TAG, String.format("Expended energy offset=%d value=%d", expendedEnergyOffset, expendedEnergy));

        return expendedEnergy;
    }

    /**
     * Set given flag info characteristic value byte array.
     *
     * @param flag flag to set
     * @throws GattException if cannot set given flag
     */
    private void setFlag(byte flag) throws GattException {
        setIntValue(getFlags() | flag, BluetoothGattCharacteristic.FORMAT_UINT8, FLAGS_OFFSET);
    }

    /**
     * Set given heart rate measurement value info characteristic value byte array.
     *
     * @param heartRateMeasurement heart rate measurement value to set
     * @throws GattException if cannot set given heart rate measurement value
     */
    private void setHeartRateMeasurementValue(int heartRateMeasurement) throws GattException {
        int format;

        if (FormatUtils.isInUInt8Range(heartRateMeasurement)) {
            format = BluetoothGattCharacteristic.FORMAT_UINT8;

            /* HR UInt8 format => Flags = 0b00000000 */
            setFlag(HEART_RATE_MEASUREMENT_UINT8_FLAG);
        } else {
            format = BluetoothGattCharacteristic.FORMAT_UINT16;

            /* HR UInt16 format => Flags = 0b00000001 */
            setFlag(HEART_RATE_MEASUREMENT_UINT16_FLAG);
        }

        setIntValue(heartRateMeasurement, format, HEART_RATE_MEASUREMENT_OFFSET);
    }

    /**
     * Set given expended energy value info characteristic value byte array.
     *
     * @param expendedEnergy expended energy value to set
     * @throws GattException if cannot set given expended energy value
     */
    private void setExpandedEnergyValue(int expendedEnergy) throws GattException {
        /* EE present => Flags = 0b0000100(0/1) */
        setFlag(EXPENDED_ENERGY_FLAG);

        int expendedEnergyOffset = isUInt16HeartRateFormat() ?
                /* HR is in UInt16 format => EE offset is shifted */
                EXPENDED_ENERGY_SHIFTED_OFFSET :
                /* HR is in UInt8 format => EE offset is base */
                EXPENDED_ENERGY_BASE_OFFSET;

        setIntValue(expendedEnergy, BluetoothGattCharacteristic.FORMAT_UINT16, expendedEnergyOffset);
    }

    /**
     * Set given Heart Rate Measurement and Expended Energy values to the characteristic.
     *
     * @param heartRateMeasurement (bpm) heart rate measurement value in UInt8/UInt16 format
     * @param expendedEnergy       (kiloJoules) expended energy value in UInt16 format
     * @throws GattException in case of wrong arguments format or offset for heart rate characteristic values
     */
    public void setHeartRateCharacteristicValue(int heartRateMeasurement, Optional<Integer> expendedEnergy) throws GattException {

        /* assert that given values are in right bounds. */
        FormatUtils.assertIsUInt16(heartRateMeasurement);
        if (expendedEnergy.isPresent()) {
            FormatUtils.assertIsUInt16(expendedEnergy.get());
        }

        int valueSize = calculateValueSize(heartRateMeasurement, expendedEnergy);
        setValue(new byte[valueSize]);

        setHeartRateMeasurementValue(heartRateMeasurement);

        if (expendedEnergy.isPresent()) {
            setExpandedEnergyValue(expendedEnergy.get());
        }
    }

    /**
     * Calculate size of byte array for characteristic value according to
     * given heart rate measurement and expanded energy values.
     *
     * @param heartRateMeasurement heart rate measurement value
     * @param optionalExpendedEnergy expanded energy value if present
     * @return size of byte array for characteristic value
     */
    private int calculateValueSize(int heartRateMeasurement, Optional<Integer> optionalExpendedEnergy) {
        /* one byte for flags */
        int size = FLAGS_SIZE;

        /* one byte for HR if UInt8, two bytes if UInt16 */
        size += FormatUtils.isInUInt8Range(heartRateMeasurement) ?
                HEART_RATE_MEASUREMENT_UINT8_SIZE :
                HEART_RATE_MEASUREMENT_UINT16_SIZE;

        /* two bytes for EE if present */
        size += optionalExpendedEnergy.isPresent() ?
                EXPENDED_ENERGY_SIZE :
                0;

        return size;
    }

    /**
     * Assert Expended Energy is present.
     *
     * @throws GattException if not present
     */
    private void assertExpendedEnergyIsPresent() throws GattException {
        if (!isExpendedEnergyPresent()) {
            Log.w(TAG, "Expended energy value is not present");
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
    private boolean isUInt16HeartRateFormat() throws GattException {
        int flags = getIntValue(
                BluetoothGattCharacteristic.FORMAT_UINT8,
                FLAGS_OFFSET);
        return (flags & HEART_RATE_MEASUREMENT_UINT16_FLAG) > 0;
    }

    /**
     * Determine whether the Energy Expended Field is present or not.
     * If Energy Expended Status bit in Flags Field is 1, then is present, otherwise not.
     *
     * @return true is UInt16, false if UInt8
     */
    private boolean isExpendedEnergyPresent() throws GattException {
        int flags = getIntValue(
                BluetoothGattCharacteristic.FORMAT_UINT8,
                FLAGS_OFFSET);
        return (flags & EXPENDED_ENERGY_FLAG) > 0;
    }
}
