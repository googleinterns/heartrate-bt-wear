package com.google.heartrate.wearos.app.gatt.heartrate.characteristics;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import com.google.heartrate.wearos.app.gatt.FormatUtils;
import com.google.heartrate.wearos.app.gatt.GattException;
import com.google.heartrate.wearos.app.gatt.attributes.GattCharacteristic;
import com.google.heartrate.wearos.app.gatt.attributes.GattDescriptor;
import com.google.heartrate.wearos.app.gatt.heartrate.descriptors.ClientCharacteristicConfigurationDescriptor;

import java.util.UUID;

/**
 * {@link HeartRateMeasurementCharacteristic} class inherits {@link GattCharacteristic}
 * and specifies set/get operation for Heart Rate Measurement Characteristic characteristic.
 * <p>
 * Fields included in the characteristic:
 * <table>
 *  <thead>
 *      <tr><th>Field</th><th>Requirement</th><th>Format</th></tr>
 *  <thead>
 *  <tbody>
 *      <tr><td> Flags </td><td> Mandatory </td><td> UInt8 </td></tr>
 *      <tr><td> Heart Rate Measurement </td><td> Mandatory </td><td> UInt8/UInt16 </td></tr>
 *      <tr><td> Energy Expended </td><td> Optional, Mandatory if Energy Expended flag </td><td> UInt16 </td></tr>
 *      <tr><td> RR-Interval </td><td> Optional, Not supported </td><td> - </td></tr>
 *      <tr><td> Transmission Interval </td><td> Optional, Not supported </td><td> - </td></tr>
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


    /** Offset at which the Heart Rate Measurement characteristic flags are stored. */
    private static final int FLAGS_OFFSET = 0;

    /** Offset at which Heart Rate Measurement value is stored. */
    private static final int HEART_RATE_MEASUREMENT_OFFSET = 1;

    /** Offset at which Expended Energy value is Heart Rate Measurement value field is in a format of UINT8. */
    private static final int EXPENDED_ENERGY_BASE_OFFSET = 2;

    /** Offset at which Expended Energy value is Heart Rate Measurement value field is in a format of UINT16. */
    private static final int EXPENDED_ENERGY_SHIFTED_OFFSET = 3;

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
     * Set given Heart Rate Measurement and Expended Energy values to the characteristic.
     * <p>
     * Flags 8bit format 0b00000000 from right to left:
     * <br>Heart Rate Format (0) -> UInt8 Format, (1) -> UInt16 Format
     * <br>Sensor Contact Status (00) -> Not Supported
     * <br>Energy Expended (1) -> Field Present
     * <br>RR-Interval (0) -> Field Not Present
     * <br>Unused (000)
     *
     * @param heartRateMeasurement (bpm) heart rate measurement value in UInt8/UInt16 format
     * @param expendedEnergy       (kiloJoules) expended energy value in UInt16 format
     * @throws GattException in case of wrong arguments format or offset for heart rate characteristic values
     */
    public void setHeartRateCharacteristicValue(int heartRateMeasurement, int expendedEnergy) throws GattException {
        Log.i(TAG, String.format("Set the heart measurements value %d and expended energy value %d",
                heartRateMeasurement, expendedEnergy));

        FormatUtils.assertIsUInt16(heartRateMeasurement);
        FormatUtils.assertIsUInt16(expendedEnergy);

        if (FormatUtils.isInUInt8Range(heartRateMeasurement)) {

            /* Flags(UInt8) + HR(UInt8) + EE(UInt16) */
            setValue(new byte[4]);

            /* EE present and HR UInt8 format => Flags = 0b00001000 */
            setIntValue((byte) (HEART_RATE_MEASUREMENT_UINT8_FLAG | EXPENDED_ENERGY_FLAG),
                    BluetoothGattCharacteristic.FORMAT_UINT8,
                    FLAGS_OFFSET);

            setIntValue(heartRateMeasurement,
                    BluetoothGattCharacteristic.FORMAT_UINT8,
                    HEART_RATE_MEASUREMENT_OFFSET);

            /* HR is in UInt8 format => EE offset is base */
            setIntValue(expendedEnergy,
                    BluetoothGattCharacteristic.FORMAT_UINT16,
                    EXPENDED_ENERGY_BASE_OFFSET);
        } else {

            /* Flags(UInt8) + HR(UInt16) + EE(UInt16) */
            setValue(new byte[5]);

            /* EE present and HR UInt16 format => Flags = 0b00001001 */
            setIntValue((byte) (HEART_RATE_MEASUREMENT_UINT16_FLAG | EXPENDED_ENERGY_FLAG),
                    BluetoothGattCharacteristic.FORMAT_UINT8,
                    FLAGS_OFFSET);

            setIntValue(heartRateMeasurement,
                    BluetoothGattCharacteristic.FORMAT_UINT16,
                    HEART_RATE_MEASUREMENT_OFFSET);

            /* HR is in UInt16 format => EE offset is shifted */
            setIntValue(expendedEnergy,
                    BluetoothGattCharacteristic.FORMAT_UINT16,
                    EXPENDED_ENERGY_SHIFTED_OFFSET);
        }
    }

    /**
     * Set given Heart Rate Measurement value to the characteristic.
     * <p>
     * Flags 8bit format 0b00000000 from right to left:
     * <br>Heart Rate Format (0) -> UInt8 Format, (1) -> UInt16 Format
     * <br>Sensor Contact Status (00) -> Not Supported
     * <br>Energy Expended (0) -> Field not Present
     * <br>RR-Interval (0) -> Field Not Present
     * <br>Unused (000)
     *
     * @param heartRateMeasurement (bpm) heart rate measurement value in UInt8/UInt16 format
     * @throws GattException in case of wrong arguments format or offset for heart rate characteristic values
     */
    public void setHeartRateCharacteristicValue(int heartRateMeasurement) throws GattException {
        Log.i(TAG, String.format("Set the heart measurements value %d", heartRateMeasurement));

        FormatUtils.assertIsUInt16(heartRateMeasurement);

        if (FormatUtils.isInUInt8Range(heartRateMeasurement)) {

            /* Flags(UInt8) + HR(UInt8) */
            setValue(new byte[2]);

            /* EE not present and HR UInt8 format => Flags = 0b00000000 */
            setIntValue(HEART_RATE_MEASUREMENT_UINT8_FLAG,
                    BluetoothGattCharacteristic.FORMAT_UINT8,
                    FLAGS_OFFSET);

            setIntValue(heartRateMeasurement,
                    BluetoothGattCharacteristic.FORMAT_UINT8,
                    HEART_RATE_MEASUREMENT_OFFSET);

        } else {

            /* Flags(UInt8) + HR(UInt16) */
            setValue(new byte[3]);

            /* EE not present and HR UInt16 format => Flags = 0b00000001 */
            setIntValue(HEART_RATE_MEASUREMENT_UINT16_FLAG,
                    BluetoothGattCharacteristic.FORMAT_UINT8,
                    FLAGS_OFFSET);

            setIntValue(heartRateMeasurement,
                    BluetoothGattCharacteristic.FORMAT_UINT16,
                    HEART_RATE_MEASUREMENT_OFFSET);
        }
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
