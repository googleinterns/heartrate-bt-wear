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


/**
 * {@link BluetoothHeartRateServerInteractor} class provides methods for interaction with remote
 * device with Heart Rate service.
 */
public class BluetoothHeartRateServerInteractor {
    private static final String TAG = BluetoothHeartRateServerInteractor.class.getSimpleName();

    private BluetoothGatt bluetoothGatt;
    private BluetoothActionsListener bluetoothActionsListener;

    private boolean isConnected = false;

    public void connect(String address, final BluetoothGattCallback callback, final Context context) throws GattException {
        BluetoothDevice bluetoothDevice = BluetoothUtils.getBluetoothAdapter(context).getRemoteDevice(address);

        if (bluetoothDevice == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return;
        }

        // If previously connected device, try to reconnect.
        if (!isConnected && bluetoothGatt != null && bluetoothGatt.getDevice().getAddress().equals(bluetoothDevice.getAddress())) {
            reconnect(bluetoothDevice);
        }

        // If cannot reconnect, directly connect to the device, setting the auto connect parameter to false.
        if (!isConnected) {
            connect(context, callback, bluetoothDevice);
        }
    }

    private void reconnect(BluetoothDevice bluetoothDevice) {
        Log.d(TAG, String.format("Reconnecting to server: name=%s, address=%s", bluetoothDevice.getAddress(), bluetoothDevice.getName()));
        if (bluetoothGatt.connect()) {
            isConnected = true;
            Log.d(TAG, "Reconnect to server success.");
        } else {
            isConnected = false;
            Log.e(TAG, "Reconnect to server failed.");
        }
    }

    private void connect(Context context, BluetoothGattCallback bluetoothGattCallback, BluetoothDevice bluetoothDevice) {
        Log.d(TAG, String.format("Connecting to server: name=%s, address=%s", bluetoothDevice.getAddress(), bluetoothDevice.getName()));
        bluetoothGatt = bluetoothDevice.connectGatt(context, false, bluetoothGattCallback, BluetoothDevice.TRANSPORT_LE);
        if (bluetoothGatt != null) {
            isConnected = true;
            Log.d(TAG, "Connect to server success.");
        } else {
            isConnected = false;
            Log.d(TAG, "Connect to server failed.");
        }
    }

    public void discoverServices(BluetoothGatt bluetoothGatt) {
        Log.d(TAG, String.format("Discovering server for device: %s", bluetoothGatt.getDevice().getAddress()));

        this.bluetoothGatt = bluetoothGatt;
        bluetoothGatt.discoverServices();
    }

    public void disconnect() {
        Log.d(TAG, String.format("Disconnect to server: %s", bluetoothGatt.getDevice().getAddress()));

        isConnected = false;
        bluetoothGatt.disconnect();
    }

    public void close() {
        Log.d(TAG, String.format("Close connection for server: %s", bluetoothGatt.getDevice().getAddress()));
        bluetoothGatt.close();
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void enableCharacteristicNotification(BluetoothGatt bluetoothGatt, boolean enabled) {
        Log.d(TAG, "Enabling Heart Rate characteristic notification.");

        if (!isConnected) {
            Log.w(TAG, "Failed to set characteristic notification. No server available.");
            return;
        }
        this.bluetoothGatt = bluetoothGatt;

        BluetoothGattCharacteristic characteristic = bluetoothGatt
                .getService(HeartRateServiceManager.HEART_RATE_SERVICE_UUID)
                .getCharacteristic(HeartRateServiceManager.HEART_RATE_MEASUREMENT_UUID);

        bluetoothGatt.readCharacteristic(characteristic);

        if (!bluetoothGatt.setCharacteristicNotification(characteristic, enabled)) {
            Log.e(TAG, "Failed to enable notification.");
        }

        BluetoothGattDescriptor descriptor = characteristic
                .getDescriptor(HeartRateServiceManager.CLIENT_CHARACTERISTIC_CONFIGURATION_UUID);

        descriptor.setValue(enabled ?
                BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE :
                BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);

        if (!bluetoothGatt.writeDescriptor(descriptor)) {
            Log.e(TAG, "Failed to write descriptor.");
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
