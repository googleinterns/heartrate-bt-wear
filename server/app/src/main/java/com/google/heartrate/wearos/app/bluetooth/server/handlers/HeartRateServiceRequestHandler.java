package com.google.heartrate.wearos.app.bluetooth.server.handlers;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import com.google.heartrate.wearos.app.bluetooth.server.BluetoothServerCallback;
import com.google.heartrate.wearos.app.gatt.GattException;
import com.google.heartrate.wearos.app.gatt.heartrate.characteristics.HeartRateMeasurementCharacteristic;
import com.google.heartrate.wearos.app.gatt.heartrate.service.HeartRateGattService;
import com.google.heartrate.wearos.app.sensors.HeartRateSensorListener;
import com.google.heartrate.wearos.app.sensors.HeartRateValueSubscriber;

import java.util.Arrays;

public class HeartRateServiceRequestHandler implements GattServiceRequestHandler, HeartRateValueSubscriber {
    private static final String TAG = HeartRateServiceRequestHandler.class.getSimpleName();

    /** Service manager for Heart Rate service. */
    private final HeartRateGattService mHeartRateGattService;


    private final HeartRateSensorListener mHeartRateSensorListener;

    private BluetoothServerCallback mBluetoothServerCallback;

    /** Storage for all devices registered to server. */
    private DeviceHandler mDeviceHandler;

    public HeartRateServiceRequestHandler(Context context) {
        mHeartRateSensorListener = new HeartRateSensorListener(context);
        mHeartRateGattService = new HeartRateGattService();
        mDeviceHandler = new DeviceHandler();
    }

    @Override
    public void onServiceAdded(BluetoothServerCallback bluetoothServerCallback) {
        mBluetoothServerCallback = bluetoothServerCallback;
        mHeartRateSensorListener.registerSubscriber(this);
    }

    @Override
    public void onServiceRemoved() {
        mHeartRateSensorListener.unregisterSubscriber(this);
        mDeviceHandler.unregisterAll();
    }

    @Override
    public void onDeviceDisconnected(BluetoothDevice device) {
        mDeviceHandler.unregisterDevice(device);
    }

    @Override
    public byte[] onDescriptorRead(BluetoothDevice device, BluetoothGattDescriptor descriptor, int offset) {
        if (mDeviceHandler.contains(device)) {
            return BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
        } else {
            return BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
        }
    }

    @Override
    public void onDescriptorWrite(BluetoothDevice device, BluetoothGattDescriptor descriptor, int offset, byte[] value) {
        if (Arrays.equals(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE, value)) {
            Log.d(TAG, String.format("Subscribe device %s to notifications", device));
            mDeviceHandler.registerDevice(device);
        } else if (Arrays.equals(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE, value)) {
            Log.d(TAG, String.format("Unsubscribe device %s from notifications", device));
            mDeviceHandler.unregisterDevice(device);
        }
    }

    @Override
    public void onHeartRateValueChanged(int value) {
        try {
            HeartRateMeasurementCharacteristic heartRateMeasurementCharacteristic
                = mHeartRateGattService.getHeartRateMeasurementCharacteristic();
            heartRateMeasurementCharacteristic.setHeartRateCharacteristicValue(value);

            mBluetoothServerCallback.onCharacteristicChanged(heartRateMeasurementCharacteristic.getBluetoothGattCharacteristic(),
                    mDeviceHandler.getAllRegisteredDevices());

        } catch (GattException e) {
            e.printStackTrace();
        }
    }

    @Override
    public BluetoothGattService getBluetoothGattService() {
        return null;
    }
}
