package com.google.heartrate.wearos.app.gatt.attributes;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import com.google.heartrate.wearos.app.gatt.GattException;

import java.util.Arrays;
import java.util.UUID;

/**
 * {@link GattDescriptor} is the wrapper over {@link BluetoothGattDescriptor} class.
 * <p>
 * {@link GattDescriptor} provides method which are common for all GATT descriptors.
 * This abstraction makes all operations with {@link BluetoothGattDescriptor}
 * expandable for each specific descriptor type.
 */
public class GattDescriptor implements GattAttribute {

    /** {@link BluetoothGattDescriptor} for descriptor. */
    private final BluetoothGattDescriptor mBluetoothGattDescriptor;

    /**
     * Configure {@link BluetoothGattDescriptor} with given parameters.
     *
     * @param uuid descriptor uuid
     * @param permissions descriptor permissions
     */
    public GattDescriptor(UUID uuid, int permissions) {
        mBluetoothGattDescriptor = new BluetoothGattDescriptor(uuid, permissions);
    }

    /**
     * Process default read descriptor request.
     * If special actions need to be done on read request, please override this method.
     *
     * @param device device from which read request was received
     * @param offset value offset
     * @return current value of descriptor
     * @throws GattException if value can not be read
     */
    public byte[] read(BluetoothDevice device, int offset) throws GattException {
        // TODO: implement getValue with offset, not required for now
        return getValue();
    }

    /**
     * Process default write descriptor request.
     * If special actions need to be done on write request, please override this method.
     *
     * @param device device from which write request was received
     * @param offset value offset
     * @param value value to write
     * @throws GattException if value can not be write
     */
    public void write(BluetoothDevice device, int offset, byte[] value) throws GattException {
        // TODO: implement setValue with offset, not required for now
        setValue(value);
    }

    /**
     * Set value to descriptor.
     *
     * @param value value to set
     * @throws GattException if can not set value to descriptor
     */
    protected void setValue(byte[] value) throws GattException {
        if (!mBluetoothGattDescriptor.setValue(value)) {
            throw new GattException(String.format("Value %s can not be set to descriptor.", Arrays.toString(value)));
        }
    }

    /**
     * Get value from descriptor.
     *
     * @return value from descriptor
     * @throws GattException if can not get value from descriptor
     */
    protected byte[] getValue() throws GattException {
        try {
            return mBluetoothGattDescriptor.getValue();
        } catch (Exception e) {
            throw new GattException(e);
        }
    }

    /**
     * Assert descriptor has read permissions.
     *
     * @throws GattException when descriptor has no read permissions
     */
    public void assertDescriptorReadable() throws GattException {
        if ((mBluetoothGattDescriptor.getPermissions() &
                BluetoothGattCharacteristic.PERMISSION_READ) == 0) {
            throw new GattException(String.format("Descriptor %s has no read permissions", getUUid()),
                    BluetoothGatt.GATT_READ_NOT_PERMITTED);
        }
    }

    /**
     * Assert descriptor has write permissions.
     *
     * @throws GattException when descriptor has no write permissions
     */
    public void assertDescriptorWritable() throws GattException {
        if ((mBluetoothGattDescriptor.getPermissions() &
                BluetoothGattCharacteristic.PERMISSION_WRITE) == 0) {
            throw new GattException(String.format("Descriptor %s has no write permissions", getUUid()),
                    BluetoothGatt.GATT_WRITE_NOT_PERMITTED);
        }
    }

    /**
     * Get {@link BluetoothGattDescriptor} for descriptor.
     *
     * @return {@link BluetoothGattDescriptor} for descriptor
     */
    BluetoothGattDescriptor getBluetoothGattDescriptor() {
        return mBluetoothGattDescriptor;
    }

    @Override
    public UUID getUUid() {
        return mBluetoothGattDescriptor.getUuid();
    }
}
