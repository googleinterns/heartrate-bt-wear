package com.google.heartrate.wearos.app.gatt.attributes;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import com.google.heartrate.wearos.app.gatt.GattException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

/**
 * {@link GattCharacteristic} is the wrapper over {@link BluetoothGattCharacteristic} class.
 * <p>
 * {@link GattCharacteristic} provides method which are common for all GATT characteristics.
 * This abstraction makes all operations with {@link BluetoothGattCharacteristic}
 * expandable for each specific characteristic type.
 */
public class GattCharacteristic implements GattAttribute {

    /** {@link BluetoothGattCharacteristic} for characteristic. */
    private final BluetoothGattCharacteristic bluetoothGattCharacteristic;

    /** Descriptors which characteristic include. */
    private final HashMap<UUID, GattDescriptor> descriptorByUuid = new HashMap<>();

    /**
     * Configure {@link BluetoothGattCharacteristic} with given parameters.
     * @param uuid characteristic uuid
     * @param properties characteristic properties
     * @param permissions characteristic permissions
     */
    public GattCharacteristic(UUID uuid, int properties, int permissions) {
        bluetoothGattCharacteristic =
                new BluetoothGattCharacteristic(uuid, properties, permissions);
    }

    /**
     * Configure {@link BluetoothGattCharacteristic} with given parameters and descriptors.
     *
     * @param uuid characteristic uuid
     * @param properties characteristic properties
     * @param permissions characteristic permissions
     * @param gattDescriptors characteristic descriptors
     */
    public GattCharacteristic(UUID uuid, int properties, int permissions, GattDescriptor[] gattDescriptors) {
        this(uuid, properties, permissions);

        for (GattDescriptor gattDescriptor : gattDescriptors) {
            descriptorByUuid.put(gattDescriptor.getUuid(), gattDescriptor);
            bluetoothGattCharacteristic.addDescriptor(gattDescriptor.getBluetoothGattDescriptor());
        }
    }

    /**
     * Determine wether characteristic has descriptor with given uuid or not.
     *
     * @param uuid descriptor uuid to check
     * @return true if characteristic has descriptor, false otherwise
     */
    public boolean hasDescriptor(UUID uuid) {
        return descriptorByUuid.containsKey(uuid);
    }

    /**
     * Get {@link GattDescriptor} for descriptor in characteristic with given uuid.
     *
     * @param descriptorUuid descriptor uuid
     * @return {@link GattDescriptor} for characteristic
     * @throws GattException if characteristic does not support descriptor with given uuid
     */
    public GattDescriptor getDescriptor(UUID descriptorUuid) throws GattException {
        GattDescriptor descriptorManager = descriptorByUuid.get(descriptorUuid);
        if (descriptorManager == null) {
            throw new GattException(String.format("Characteristic %s does not have descriptor %s",
                    getUuid(), descriptorUuid));
        }
        return descriptorManager;
    }

    /**
     * Process default read characteristic request.
     * If special actions need to be done on read request, please override this method.
     *
     * @param device device from which read request was received
     * @param offset value offset
     * @return current value of characteristic
     * @throws GattException if value can not be read
     */
    public byte[] read(BluetoothDevice device, int offset) throws GattException {
        // TODO: implement getValue with offset, not required for now
        return getValue();
    }

    /**
     * Process default write characteristic request.
     * If special actions need to be done on write request, please override this method.
     *
     * @param device device from which write request was received
     * @param offset value offset
     * @throws GattException if value can not be write
     */
    public void write(BluetoothDevice device, int offset, byte[] value) throws GattException {
        // TODO: implement setValue with offset, not required for now
        setValue(value);
    }

    /**
     * Get value from characteristic.
     *
     * @return value characteristic
     * @throws GattException if value can not be got
     */
    protected byte[] getValue() throws GattException {
        try {
            byte[] value = bluetoothGattCharacteristic.getValue();
            if (value == null) {
                throw new GattException(String.format("Value can not be " +
                        "got from characteristic %s.", getUuid()));
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
    protected int getIntValue(int format, int offset) throws GattException {
        try {
            Integer value = bluetoothGattCharacteristic.getIntValue(format, offset);
            if (value == null) {
                throw new GattException(String.format("Value in format %d with offset %d can not be " +
                        "got from characteristic %s.", format, offset, getUuid()));
            }
            return value;
        } catch (Exception e) {
            throw new GattException(e);
        }
    }

    /**
     * Set int value to characteristic.
     *
     * @param value  value to set
     * @param format format at which the value should be set
     * @param offset offset at which the value should be set
     * @throws GattException if value can not be set
     */
    protected void setIntValue(int value, int format, int offset) throws GattException {
        if (!bluetoothGattCharacteristic.setValue(value, format, offset)) {
            throw new GattException(String.format("Value %d in format %d with offset %d has not been " +
                    "set to characteristic %s.", value, format, offset, getUuid()));
        }
    }

    /**
     * Set value to characteristic.
     *
     * @param value value to set
     * @throws GattException if value can not be set
     */
    protected void setValue(byte[] value) throws GattException {
        if (!bluetoothGattCharacteristic.setValue(value)) {
            throw new GattException(String.format("Value %s can not be " +
                    "set to characteristic %s.", Arrays.toString(value), getUuid()));
        }
    }

    /**
     * Assert characteristic has read permissions.
     *
     * @throws GattException when characteristic has no read permissions
     */
    public void assertCharacteristicReadable() throws GattException {
        if ((bluetoothGattCharacteristic.getProperties() &
                (BluetoothGattCharacteristic.PROPERTY_READ)) == 0) {
            throw new GattException(String.format("Characteristic %s has no read permissions", getUuid()),
                    BluetoothGatt.GATT_READ_NOT_PERMITTED);
        }
    }

    /**
     * Assert characteristic has write permissions.
     *
     * @throws GattException when characteristic has no write permissions
     */
    public void assertCharacteristicWritable() throws GattException {
        if ((bluetoothGattCharacteristic.getProperties() &
                (BluetoothGattCharacteristic.PROPERTY_WRITE |
                        BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) == 0) {
            throw new GattException(String.format("Characteristic %s has no write permissions", getUuid()),
                    BluetoothGatt.GATT_WRITE_NOT_PERMITTED);
        }
    }

    /**
     * Assert characteristic has notify permissions.
     *
     * @throws GattException when characteristic has no notify permissions
     */
    public void assertCharacteristicNotifiable() throws GattException {
        if ((bluetoothGattCharacteristic.getProperties() &
                BluetoothGattCharacteristic.PROPERTY_NOTIFY) == 0) {
            throw new GattException(String.format("Characteristic %s has no write permissions", getUuid()),
                    BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED);
        }
    }

    /**
     * Determine wether characteristic requires write response.
     *
     * @return true if property is writable without response, false otherwise
     */
    public boolean isWriteResponseRequired() {
        return (bluetoothGattCharacteristic.getProperties() &
                BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) == 0;
    }

    /**
     * Get {@link BluetoothGattCharacteristic} for characteristic.
     *
     * @return {@link BluetoothGattCharacteristic} for characteristic
     */
    public BluetoothGattCharacteristic getBluetoothGattCharacteristic() {
        return bluetoothGattCharacteristic;
    }

    @Override
    public UUID getUuid() {
        return bluetoothGattCharacteristic.getUuid();
    }
}
