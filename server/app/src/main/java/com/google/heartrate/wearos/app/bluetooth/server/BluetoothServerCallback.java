package com.google.heartrate.wearos.app.bluetooth.server;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import com.google.heartrate.wearos.app.bluetooth.server.handlers.GattServiceRequestHandler;
import com.google.heartrate.wearos.app.gatt.GattException;

import java.util.Arrays;
import java.util.Set;

/**
 * Class is used to implement {@link BluetoothServer} callbacks.
 *
 * <p>General-purpose for all read/write services attributes requests.
 * Redirects all request to special handler for each service.
 */
public class BluetoothServerCallback extends BluetoothGattServerCallback {
    private static final String TAG = BluetoothServerCallback.class.getSimpleName();

    /** {@link BluetoothServer} to handle requests received from callback. */
    public final BluetoothServer bluetoothServer;

    public BluetoothServerCallback(BluetoothServer bluetoothServer) {
        this.bluetoothServer = bluetoothServer;
    }

    /**
     * {@link BluetoothGattServerCallback#onConnectionStateChange}
     *
     * <p>Notify all request handlers in service about received device connection state.
     */
    @Override
    public void onConnectionStateChange(BluetoothDevice device, final int status, int newState) {
        Log.v(TAG, String.format("onConnectionStateChange() - device=%s status=%s state=%s",
                device.getAddress(), status, newState));

        if (status != BluetoothGatt.GATT_SUCCESS) {
            Log.d(TAG, "Status %s");
            return;
        }

        if (newState == BluetoothGatt.STATE_CONNECTED) {
            Log.d(TAG, "Status success. State connected");
            for (GattServiceRequestHandler requestHandler : bluetoothServer.gattRequestHandlerByServiceUuid.values()) {
                requestHandler.onDeviceConnected(device);
            }
        } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
            Log.d(TAG, "Status success. State disconnected");
            for (GattServiceRequestHandler requestHandler : bluetoothServer.gattRequestHandlerByServiceUuid.values()) {
                requestHandler.onDeviceDisconnected(device);
            }
        }
    }

    /**
     * {@link BluetoothGattServerCallback#onServiceAdded}
     *
     * <p>Notify request handlers for added service in service about publication to server status.
     */
    @Override
    public void onServiceAdded(int status, BluetoothGattService bluetoothGattService) {
        Log.v(TAG, String.format("onServiceAdded() - status=%d", status));
        try {
            GattServiceRequestHandler requestHandler = bluetoothServer.getGattServiceRequestHandler(bluetoothGattService);
            requestHandler.onServiceAdded(this);
        } catch (GattException e) {
            Log.e(TAG, String.format("onServiceAdded() failed with exception %s", e.getMessage()));
        }
    }

    /**
     * {@link BluetoothGattServerCallback#onNotificationSent}
     */
    @Override
    public void onNotificationSent(BluetoothDevice device, int status) {
        Log.v(TAG, String.format("onNotificationSent() - status=%d", status));
    }

    /**
     * {@link BluetoothGattServerCallback#onCharacteristicReadRequest}
     *
     * <p>Invoke {@link GattServiceRequestHandler#onCharacteristicRead} for given characteristic.
     * If {@link BluetoothServer} has no handler for given characteristic or
     * {@link GattServiceRequestHandler#onCharacteristicRead} failed,
     * invoke {@link BluetoothServer#sendErrorResponse},
     * otherwise {@link BluetoothServer#sendResponse} to send response to client.
     */
    @Override
    public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset,
                                            BluetoothGattCharacteristic characteristic) {
        Log.v(TAG, String.format("onCharacteristicReadRequest() - device=%s characteristic=%s",
                characteristic.getUuid(),
                Arrays.toString(characteristic.getValue())));
        try {
            GattServiceRequestHandler requestHandler = bluetoothServer.getGattServiceRequestHandler(characteristic);
            byte[] value = requestHandler.onCharacteristicRead(device, characteristic, offset);

            bluetoothServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
        } catch (GattException e) {
            Log.e(TAG, String.format("onCharacteristicReadRequest() failed with exception %s", e.getMessage()));
            bluetoothServer.sendErrorResponse(device, requestId, e.getStatus());
        }
    }

    /**
     * {@link BluetoothGattServerCallback#onCharacteristicWriteRequest}
     *
     * <p>Invoke {@link GattServiceRequestHandler#onCharacteristicWrite} for given characteristic.
     * If {@link BluetoothServer} has no handler for given characteristic or
     * {@link GattServiceRequestHandler#onCharacteristicWrite} failed,
     * invoke {@link BluetoothServer#sendErrorResponse},
     * otherwise {@link BluetoothServer#sendResponse} to send response to client.
     */
    @Override
    public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
                                             BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded,
                                             int offset, byte[] value) {
        Log.v(TAG, String.format("onCharacteristicWriteRequest() - device=%s characteristic=%s value=%s",
                characteristic.getUuid(),
                Arrays.toString(characteristic.getValue()),
                Arrays.toString(value)));
        try {
            GattServiceRequestHandler requestHandler = bluetoothServer.getGattServiceRequestHandler(characteristic);
            requestHandler.onCharacteristicWrite(device, characteristic, offset, value);

            if (responseNeeded) {
                bluetoothServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null);
            }
        } catch (GattException e) {
            if (responseNeeded) {
                Log.e(TAG, String.format("onCharacteristicWriteRequest() failed with exception %s", e.getMessage()));
                bluetoothServer.sendErrorResponse(device, requestId, e.getStatus());
            }
        }
    }

    /**
     * {@link BluetoothGattServerCallback#onDescriptorReadRequest}
     *
     * <p>Invoke {@link GattServiceRequestHandler#onDescriptorRead} for given characteristic.
     * If {@link BluetoothServer} has no handler for given descriptor or
     * {@link GattServiceRequestHandler#onDescriptorRead} failed,
     * invoke {@link BluetoothServer#sendErrorResponse},
     * otherwise {@link BluetoothServer#sendResponse} to send response to client.
     */
    @Override
    public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset,
                                        BluetoothGattDescriptor descriptor) {
        Log.d(TAG, String.format("onDescriptorReadRequest() device=%s descriptor=%s",
                device.getAddress(), descriptor.getUuid()));
        try {
            GattServiceRequestHandler requestHandler = bluetoothServer.getGattServiceRequestHandler(descriptor);
            byte[] value = requestHandler.onDescriptorRead(device, descriptor, offset);

            bluetoothServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
        } catch (GattException e) {
            Log.e(TAG, String.format("onDescriptorReadRequest() failed with exception %s", e.getMessage()));
            bluetoothServer.sendErrorResponse(device, requestId, e.getStatus());
        }
    }

    /**
     * {@link BluetoothGattServerCallback#onDescriptorWriteRequest}
     *
     * <p>Invoke {@link GattServiceRequestHandler#onDescriptorWrite} for given characteristic.
     * If {@link BluetoothServer} has no handler for given descriptor or
     * {@link GattServiceRequestHandler#onDescriptorWrite} failed,
     * invoke {@link BluetoothServer#sendErrorResponse},
     * otherwise {@link BluetoothServer#sendResponse} to send response to client.
     */
    @Override
    public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
        Log.d(TAG, String.format("onDescriptorWriteRequest() - device=%s descriptor=%s value=%s",
                device.getAddress(), descriptor.getUuid(), Arrays.toString(value)));

        try {
            GattServiceRequestHandler requestHandler = bluetoothServer.getGattServiceRequestHandler(descriptor);
            requestHandler.onDescriptorWrite(device, descriptor, offset, value);

            if (responseNeeded) {
                bluetoothServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null);
            }
        } catch (GattException e) {
            if (responseNeeded) {
                Log.e(TAG, String.format("onDescriptorWriteRequest() failed with exception %s", e.getMessage()));
                bluetoothServer.sendErrorResponse(device, requestId, e.getStatus());
            }
        }
    }

    /**
     * Callback indicating when a local characteristic was changed.
     *
     * <p>Invoke {@link BluetoothServer#notifyCharacteristicChanged} for given characteristic
     * and every device registered on it's changes.
     *
     * @param characteristic changed characteristic
     * @param registeredDevices devices registered for given characteristic
     */
    public void onCharacteristicChanged(BluetoothGattCharacteristic characteristic, Set<BluetoothDevice> registeredDevices) {
        Log.d(TAG, "Notify registered devices");

        if (registeredDevices.isEmpty()) {
            Log.i(TAG, "No subscribers registered");
            return;
        }

        Log.v(TAG, "Sending update to " + registeredDevices.size() + " subscribers");
        for (BluetoothDevice registeredDevice : registeredDevices) {
            bluetoothServer.notifyCharacteristicChanged(registeredDevice, characteristic);
        }
    }
}
