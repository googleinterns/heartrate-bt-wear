package com.google.heartrate.wearos.app.bluetooth.server.handlers;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.util.Log;

import com.google.heartrate.wearos.app.bluetooth.server.BluetoothServer;
import com.google.heartrate.wearos.app.bluetooth.server.BluetoothServerCallback;
import com.google.heartrate.wearos.app.gatt.GattException;
import com.google.heartrate.wearos.app.gatt.attributes.GattService;

/**
 * Interface for bluetooth gatt service request handler.
 *
 * <p>To create bluetooth gatt service request handler for particular service
 * override necessary requests or use default implementation.
 */
public interface GattServiceRequestHandler {
    String TAG = GattServiceRequestHandler.class.getSimpleName();

    /**
     * Invokes from {@link BluetoothServerCallback#onCharacteristicWriteRequest}
     * when a remote device has requested to write value to service's characteristic.
     *
     * @param device the remote device that has requested the write operation
     * @param characteristic characteristic to be write
     * @param offset offset into the value of the characteristic
     * @param value value the client wants to assign to the characteristic
     * @throws GattException if operation is not supported for current server or can not write to characteristic
     */
    default void onCharacteristicWrite(BluetoothDevice device, BluetoothGattCharacteristic characteristic, int offset, byte[] value) throws GattException {
        Log.w(TAG, "Request onCharacteristicWrite() is not handled, please override it.");
        throw new GattException("Request onCharacteristicWrite() not supported", BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED);
    }

    /**
     * Invokes from {@link BluetoothServerCallback#onCharacteristicReadRequest}
     * when a remote device has requested to read value from service's characteristic.
     *
     * @param device the remote device that has requested the read operation
     * @param characteristic characteristic to be read
     * @param offset offset into the value of the characteristic
     * @throws GattException if operation is not supported for current server or can not read from characteristic
     */
    default byte[] onCharacteristicRead(BluetoothDevice device, BluetoothGattCharacteristic characteristic, int offset) throws GattException {
        Log.w(TAG, "Request onCharacteristicRead() is not handled, please override it.");
        throw new GattException("Request onCharacteristicRead() not supported", BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED);
    }

    /**
     * Invokes from {@link BluetoothServerCallback#onDescriptorWriteRequest}
     * when a remote device has requested to write value to service's descriptor.
     *
     * @param device the remote device that has requested the write operation
     * @param descriptor descriptor to be write
     * @param offset offset into the value of the descriptor
     * @param value value the client wants to assign to the descriptor
     * @throws GattException if operation is not supported for current server or can not write to descriptor
     */
    default void onDescriptorWrite(BluetoothDevice device, BluetoothGattDescriptor descriptor, int offset, byte[] value) throws GattException {
        Log.w(TAG, "Request onDescriptorWrite() is not handled, please override it.");
        throw new GattException("Request onDescriptorWrite() not supported", BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED);
    }

    /**
     * Invokes from {@link BluetoothServerCallback#onDescriptorReadRequest}
     * when a remote device has requested to read value from service's descriptor.
     *
     * @param device the remote device that has requested the read operation
     * @param descriptor descriptor to be read
     * @param offset offset into the value of the descriptor
     * @throws GattException if operation is not supported for current server or can not read from descriptor
     */
    default byte[] onDescriptorRead(BluetoothDevice device, BluetoothGattDescriptor descriptor, int offset) throws GattException {
        Log.w(TAG, "Request onDescriptorRead() is not handled, please override it.");
        throw new GattException("Request onDescriptorRead() not supported", BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED);
    }

    /**
     * Invokes from {@link BluetoothServerCallback#onConnectionStateChange}
     * when device connected to server.
     *
     * @param device connected device
     */
    default void onDeviceConnected(BluetoothDevice device) {
        Log.d(TAG, "Request onDeviceConnected() is not handled, please override it to specify it.");
    }

    /**
     * Invokes from {@link BluetoothServerCallback#onConnectionStateChange}
     * when device disconnected from server.
     *
     * @param device connected device
     */
    default void onDeviceDisconnected(BluetoothDevice device) {
        Log.d(TAG, "Request onDeviceDisconnected() is not handled, please override it to specify it.");
    }

    /**
     * Invokes from {@link BluetoothServerCallback#onServiceAdded}
     * when service added to server.
     *
     * @param bluetoothServerCallback server callback to provide to components,
     *                                which need to interact with remote devices
     */
    void onServiceAdded(BluetoothServerCallback bluetoothServerCallback);

    /**
     * Invokes from {@link BluetoothServer#stop} when server is going to stop and remove services.
     *
     */
    void onServiceRemoved();

    /**
     * Get gatt service for current handler.
     * @return gatt service for current handler
     */
    GattService getGattService();
}
