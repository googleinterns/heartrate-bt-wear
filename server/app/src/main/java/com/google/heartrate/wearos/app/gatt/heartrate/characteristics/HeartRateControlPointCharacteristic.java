package com.google.heartrate.wearos.app.gatt.heartrate.characteristics;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import com.google.heartrate.wearos.app.gatt.FormatUtils;
import com.google.heartrate.wearos.app.gatt.GattException;
import com.google.heartrate.wearos.app.gatt.attributes.GattCharacteristic;

import java.util.UUID;

/**
 * {@link HeartRateControlPointCharacteristic} class inherits {@link GattCharacteristic}
 * and specifies set/get operation for Heart Rate Control Point characteristic.
 * <p>
 * See <a href="https://www.bluetooth.com/wp-content/uploads/Sitecore-Media-Library/Gatt/Xml/Characteristics/org.bluetooth.characteristic.heart_rate_control_point.xml">
 * Heart Rate Control Point</a>.
 */
public class HeartRateControlPointCharacteristic extends GattCharacteristic {
    private static final String TAG = HeartRateControlPointCharacteristic.class.getCanonicalName();

    /** Heart Rate Control Point characteristic UUID. */
    private static final UUID HEART_RATE_CONTROL_POINT_UUID = UUID
            .fromString("00002A39-0000-1000-8000-00805f9b34fb");

    /** Offset at which the Reset Energy Expended value is stored. */
    private static final int RESET_ENERGY_EXPENDED_OFFSET = 0;

    /**
     * Create {@link GattCharacteristic} for Heart Rate Control Point characteristic.
     * <br>Configure {@link GattCharacteristic} with Heart Rate Control Point characteristic UUID,
     * property for write and write permissions.
     */
    public HeartRateControlPointCharacteristic() {
        super(HEART_RATE_CONTROL_POINT_UUID,
                BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE);
    }

    /**
     * Get the Reset Energy Expended value.
     *
     * @return current reset energy expended value
     */
    public int getResetEnergyExpendedValue() throws GattException {
        Log.i(TAG, "Get the reset energy expended value");

        int resetEnergyExpended = getIntValue(
                BluetoothGattCharacteristic.FORMAT_UINT8,
                RESET_ENERGY_EXPENDED_OFFSET);

        Log.v(TAG, String.format("Reset energy expended: value=%d", resetEnergyExpended));

        return resetEnergyExpended;
    }

    /**
     * Set the Reset Energy Expended value.
     *
     * @param resetEnergyExpended reset energy expended value
     */
    public void setResetEnergyExpendedValue(int resetEnergyExpended) throws GattException {
        Log.i(TAG, String.format("Set the reset energy expended value %d", resetEnergyExpended));

        FormatUtils.assertIsUInt8(resetEnergyExpended);
        setValue(new byte[]{(byte) resetEnergyExpended});
    }
}