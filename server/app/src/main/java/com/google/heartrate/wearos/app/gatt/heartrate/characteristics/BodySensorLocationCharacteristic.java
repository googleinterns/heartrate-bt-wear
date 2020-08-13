package com.google.heartrate.wearos.app.gatt.heartrate.characteristics;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import com.google.heartrate.wearos.app.gatt.FormatUtils;
import com.google.heartrate.wearos.app.gatt.GattException;
import com.google.heartrate.wearos.app.gatt.attributes.GattCharacteristic;

import java.util.UUID;

/**
 * {@link BodySensorLocationCharacteristic} class inherits {@link GattCharacteristic}
 * and specifies set/get operation for Body Sensor Location characteristic.
 * <p>
 * See <a href="https://www.bluetooth.com/wp-content/uploads/Sitecore-Media-Library/Gatt/Xml/Characteristics/org.bluetooth.characteristic.body_sensor_location.xml">
 * Body Sensor Location</a>.
 */
public class BodySensorLocationCharacteristic extends GattCharacteristic {
    private static final String TAG = BodySensorLocationCharacteristic.class.getCanonicalName();

    /** Body Sensor Location characteristic UUID. */
    private static final UUID BODY_SENSOR_LOCATION_UUID = UUID
            .fromString("00002A38-0000-1000-8000-00805f9b34fb");

    /** Offset at which the Body Sensor Location value is stored. */
    private static final int LOCATION_OFFSET = 0;

    /** Possible Body Sensor Location values: 0-6 specified, 7-255 reserved for future use. */
    public static class Locations {
        public final static int OTHER = 0;
        public final static int CHEST = 1;
        public final static int WRIST = 2;
        public final static int FINGER = 3;
        public final static int HAND = 4;
        public final static int EAR_LOBE = 5;
        public final static int FOOT = 6;
    }

    /**
     * Create {@link GattCharacteristic} for Body Sensor Location characteristic.
     * <br>Configure {@link GattCharacteristic} with Body Sensor Location characteristic UUID,
     * property for read and read permissions.
     */
    public BodySensorLocationCharacteristic() {
        super(BODY_SENSOR_LOCATION_UUID,
                BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ);
    }

    /**
     * Set Body Sensor Location value.
     *
     * @param location body sensor location id in format UInt8
     * @throws GattException in case of wrong format or offset for body sensor location characteristic value
     */
    public void setBodySensorLocationValue(int location) throws GattException {
        Log.i(TAG, String.format("Set the body sensor location value to %d", location));

        FormatUtils.assertIsUInt8(location);
        setValue(new byte[]{(byte) location});
    }

    /**
     * Get the Body Sensor Location value.
     *
     * @return current body sensor location id
     * @throws GattException in case of wrong format or offset for body sensor location characteristic value
     */
    public int getBodySensorLocationValue() throws GattException {
        Log.i(TAG, "Get the body sensor location value");

        int location = getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, LOCATION_OFFSET);

        Log.v(TAG, String.format("Body sensor location: value=%d", location));

        return location;
    }
}