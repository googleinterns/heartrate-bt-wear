package com.google.heartrate.wearos.app.bluetooth.server.handlers;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import com.google.heartrate.wearos.app.bluetooth.server.BluetoothServerCallback;
import com.google.heartrate.wearos.app.gatt.GattException;

public interface GattServiceRequestHandler {
    String TAG = GattServiceRequestHandler.class.getSimpleName();

    default void onCharacteristicWrite(BluetoothDevice device, BluetoothGattCharacteristic characteristic, int offset, byte[] value) throws GattException {
        Log.w(TAG, "Request onCharacteristicWrite() not supported, please override it.");
        throw new GattException("Request onCharacteristicWrite() not supported", BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED);
    }

    default byte[] onCharacteristicRead(BluetoothDevice device, BluetoothGattCharacteristic characteristic, int offset) throws GattException {
        Log.w(TAG, "Request onCharacteristicRead() not supported, please override it.");
        throw new GattException("Request onCharacteristicRead() not supported", BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED);
    }

    default void onDescriptorWrite(BluetoothDevice device, BluetoothGattDescriptor descriptor, int offset, byte[] value) throws GattException {
        Log.w(TAG, "Request onDescriptorWrite() not supported, please override it.");
        throw new GattException("Request onDescriptorWrite() not supported", BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED);
    }

    default byte[] onDescriptorRead(BluetoothDevice device, BluetoothGattDescriptor descriptor, int offset) throws GattException {
        Log.w(TAG, "Request onDescriptorRead() not supported, please override it.");
        throw new GattException("Request onDescriptorRead() not supported", BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED);
    }

    default void onDeviceConnected(BluetoothDevice device) {
        Log.d(TAG, "Request onDeviceConnected() is empty, please override it to specify it.");
    }

    default void onDeviceDisconnected(BluetoothDevice device) {
        Log.d(TAG, "Request onDeviceDisconnected() is empty, please override it to specify it.");
    }

    void onServiceAdded(BluetoothServerCallback bluetoothServerCallback);

    void onServiceRemoved();

    BluetoothGattService getBluetoothGattService();
}
