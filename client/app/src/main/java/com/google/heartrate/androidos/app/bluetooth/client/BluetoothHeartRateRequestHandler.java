package com.google.heartrate.androidos.app.bluetooth.client;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;
import android.util.Log;

import com.google.heartrate.androidos.app.BluetoothActionsListener;
import com.google.heartrate.androidos.app.bluetooth.BluetoothUtils;
import com.google.heartrate.androidos.app.gatt.GattException;
import com.google.heartrate.androidos.app.gatt.heartrate.HeartRateServiceManager;

import java.util.UUID;


public class BluetoothHeartRateRequestHandler {
    private static final String TAG = BluetoothHeartRateRequestHandler.class.getSimpleName();

    private BluetoothGatt mBluetoothGatt;

    private BluetoothActionsListener bluetoothActionsListener;

    public void connect(String address, final BluetoothGattCallback callback, final Context context) throws GattException {
        BluetoothDevice bluetoothDevice = BluetoothUtils.getBluetoothAdapter(context).getRemoteDevice(address);

        // Previously connected device. Try to reconnect.
        if (mBluetoothGatt != null && mBluetoothGatt.getDevice().getAddress().equals(bluetoothDevice.getAddress())) {
            Log.d(TAG, String.format("Reconnecting to server: name=%s, adress=%s", bluetoothDevice.getAddress(), bluetoothDevice.getName()));
            if (mBluetoothGatt.connect()) {
                return;
            }
            Log.w(TAG, "Reconnect to server failed.");
        }

        // Directly connect to the device, setting the auto connect parameter to false.
        Log.d(TAG, String.format("Connecting to server: name=%s, adress=%s", bluetoothDevice.getAddress(), bluetoothDevice.getName()));
        mBluetoothGatt = bluetoothDevice.connectGatt(context, false, callback);
        assertConnected();
    }

    public void discoverServices() {
        if (!isConnected()) {
            Log.w(TAG, "Discovering server failed. No server available.");
            return;
        }
        Log.d(TAG, String.format("Discovering server for device: %s", mBluetoothGatt.getDevice().getAddress()));
        mBluetoothGatt.discoverServices();
    }

    public void disconnect() {
        if (!isConnected()) {
            Log.w(TAG, "Disconnect to server failed. No server available.");
            return;
        }
        Log.d(TAG, String.format("Disconnect to server: %s", mBluetoothGatt.getDevice().getAddress()));
        mBluetoothGatt.disconnect();
    }

    public void close() {
        if (!isConnected()) {
            Log.w(TAG, "Close connection failed. No server available.");
            return;
        }
        Log.d(TAG, String.format("Close connection for server: %s", mBluetoothGatt.getDevice().getAddress()));
        mBluetoothGatt.close();
    }

    private void assertConnected() throws GattException {
        if (!isConnected()) {
            throw new GattException("No server available.");
        }
    }

    public boolean isConnected() {
        return mBluetoothGatt != null;
    }

    public void setCharacteristicNotification(boolean enabled) {
        if (!isConnected()) {
            Log.w(TAG, "Failed to set characteristic notification. No server available.");
            return;
        }
        BluetoothGattCharacteristic characteristic = mBluetoothGatt
                .getService(HeartRateServiceManager.HEART_RATE_SERVICE_UUID)
                .getCharacteristic(HeartRateServiceManager.HEART_RATE_MEASUREMENT_UUID);

        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        BluetoothGattDescriptor descriptor = characteristic
                .getDescriptor(HeartRateServiceManager.CLIENT_CHARACTERISTIC_CONFIGURATION_UUID);

        descriptor.setValue(enabled ?
                BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE :
                BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);

        if (!mBluetoothGatt.writeDescriptor(descriptor)) {
            Log.e(TAG, "Failed to set characteristic notification. Can not write descriptor.");
        }
    }

    public void onCharacteristicChange(BluetoothGattCharacteristic characteristic) {
        Log.d(TAG, String.format("onCharacteristicChange --uuid=%s", characteristic.getUuid()));
        try {
            int heartRateValue = HeartRateServiceManager.getHeartRateMeasurementValue(characteristic);
            //int expendedEnergyValue = HeartRateServiceManager.getExpendedEnergyValue(characteristic);
            Log.d(TAG, String.format("Heart rate = %d", heartRateValue));
            if (bluetoothActionsListener != null) {
                bluetoothActionsListener.onAction(String.format("Heart rate = %d", heartRateValue));
            }
        } catch (GattException e) {
            Log.e(TAG, String.format("Failed to read characteristic: %s", e.getMessage()));
        }
    }

    public void addListener(BluetoothActionsListener bluetoothActionsListener) {
        this.bluetoothActionsListener = bluetoothActionsListener;
    }
}
