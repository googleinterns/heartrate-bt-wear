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

public class BluetoothServerCallback extends BluetoothGattServerCallback {
    private static final String TAG = BluetoothServerCallback.class.getSimpleName();

    public final BluetoothServer mBluetoothServer;

    public BluetoothServerCallback(BluetoothServer bluetoothServer) {
        mBluetoothServer = bluetoothServer;
    }

    @Override
    public void onConnectionStateChange(BluetoothDevice device, final int status, int newState) {
        Log.v(TAG, String.format("onConnectionStateChange() - device=%s status=%s state=%s",
                device.getAddress(), status, newState));

        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.d(TAG, "Status success");

            if (newState == BluetoothGatt.STATE_CONNECTED) {
                Log.d(TAG, "State connected");
                for (GattServiceRequestHandler requestHandler : mBluetoothServer.gattRequestHandlerByServiceUuid.values()) {
                    requestHandler.onDeviceConnected(device);
                }
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                Log.d(TAG, "State disconnected");
                for (GattServiceRequestHandler requestHandler : mBluetoothServer.gattRequestHandlerByServiceUuid.values()) {
                    requestHandler.onDeviceDisconnected(device);
                }
            }
        }
    }

    @Override
    public void onServiceAdded(int status, BluetoothGattService bluetoothGattService) {
        Log.v(TAG, String.format("onServiceAdded() - status=%d", status));
        try {
            GattServiceRequestHandler requestHandler = mBluetoothServer.getGattServiceRequestHandler(bluetoothGattService);
            requestHandler.onServiceAdded(this);
        } catch (GattException e) {
            Log.e(TAG, String.format("onServiceAdded() failed with exception %s", e.getMessage()));
        }
    }

    @Override
    public void onNotificationSent(BluetoothDevice device, int status) {
        Log.v(TAG, String.format("onNotificationSent() - status=%d", status));
    }

    @Override
    public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset,
                                            BluetoothGattCharacteristic characteristic) {
        Log.v(TAG, String.format("onCharacteristicReadRequest() - device=%s characteristic=%s",
                characteristic.getUuid(),
                Arrays.toString(characteristic.getValue())));
        try {
            GattServiceRequestHandler requestHandler = mBluetoothServer.getGattServiceRequestHandler(characteristic);
            byte[] value = requestHandler.onCharacteristicRead(device, characteristic, offset);

            mBluetoothServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
        } catch (GattException e) {
            Log.e(TAG, String.format("onCharacteristicReadRequest() failed with exception %s", e.getMessage()));
            mBluetoothServer.sendErrorResponse(device, requestId, e.getStatus());
        }
    }

    @Override
    public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
                                             BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded,
                                             int offset, byte[] value) {
        Log.v(TAG, String.format("onCharacteristicWriteRequest() - device=%s characteristic=%s value=%s",
                characteristic.getUuid(),
                Arrays.toString(characteristic.getValue()),
                Arrays.toString(value)));
        try {
            GattServiceRequestHandler requestHandler = mBluetoothServer.getGattServiceRequestHandler(characteristic);
            requestHandler.onCharacteristicWrite(device, characteristic, offset, value);

            if (responseNeeded) {
                mBluetoothServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
            }
        } catch (GattException e) {
            if (responseNeeded) {
                Log.e(TAG, String.format("onCharacteristicWriteRequest() failed with exception %s", e.getMessage()));
                mBluetoothServer.sendErrorResponse(device, requestId, e.getStatus());
            }
        }
    }

    @Override
    public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset,
                                        BluetoothGattDescriptor descriptor) {
        Log.d(TAG, String.format("onDescriptorReadRequest() device=%s descriptor=%s",
                device.getAddress(), descriptor.getUuid()));
        try {
            GattServiceRequestHandler requestHandler = mBluetoothServer.getGattServiceRequestHandler(descriptor);
            byte[] value = requestHandler.onDescriptorRead(device, descriptor, offset);

            mBluetoothServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
        } catch (GattException e) {
            Log.e(TAG, String.format("onDescriptorReadRequest() failed with exception %s", e.getMessage()));
            mBluetoothServer.sendErrorResponse(device, requestId, e.getStatus());
        }
    }

    @Override
    public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
        Log.d(TAG, String.format("onDescriptorWriteRequest() - device=%s descriptor=%s value=%s",
                device.getAddress(), descriptor.getUuid(), Arrays.toString(value)));

        try {
            GattServiceRequestHandler requestHandler = mBluetoothServer.getGattServiceRequestHandler(descriptor);
            requestHandler.onDescriptorWrite(device, descriptor, offset, value);

            if (responseNeeded) {
                mBluetoothServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
            }
        } catch (GattException e) {
            if (responseNeeded) {
                Log.e(TAG, String.format("onDescriptorWriteRequest() failed with exception %s", e.getMessage()));
                mBluetoothServer.sendErrorResponse(device, requestId, e.getStatus());
            }
        }
    }

    public void onCharacteristicChanged(BluetoothGattCharacteristic characteristic, Set<BluetoothDevice> registeredDevices) {
        Log.d(TAG, "Notify registered devices");
        if (registeredDevices.isEmpty()) {
            Log.i(TAG, "No subscribers registered");
            return;
        }

        Log.v(TAG, "Sending update to " + registeredDevices.size() + " subscribers");
        for (BluetoothDevice registeredDevice : registeredDevices) {
            mBluetoothServer.notifyCharacteristicChanged(registeredDevice, characteristic);
        }
    }
}
