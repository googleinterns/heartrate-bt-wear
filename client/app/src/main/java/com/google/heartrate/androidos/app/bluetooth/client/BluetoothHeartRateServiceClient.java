package com.google.heartrate.androidos.app.bluetooth.client;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.util.Log;

import com.google.heartrate.androidos.app.BluetoothActionsListener;
import com.google.heartrate.androidos.app.gatt.GattException;
import com.google.heartrate.androidos.app.gatt.heartrate.HeartRateServiceManager;

import java.util.List;
import java.util.UUID;

public class BluetoothHeartRateServiceClient {
    private static final String TAG = BluetoothHeartRateServiceClient.class.getSimpleName();

    private final BluetoothScanner bluetoothScanner;
    private final BluetoothHeartRateRequestHandler bluetoothHeartRateRequestHandler;
    private Context context;

    public BluetoothHeartRateServiceClient(Context context) throws GattException {
        this.context = context;
        bluetoothHeartRateRequestHandler = new BluetoothHeartRateRequestHandler();
        bluetoothScanner = new BluetoothScanner(this.context, scanCallback);
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.d(TAG, "onScanResult()");
            BluetoothDevice bluetoothDevice = result.getDevice();
            connect(bluetoothDevice);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {

        }

        @Override
        public void onScanFailed(int errorCode) {
            switch (errorCode) {
                case SCAN_FAILED_ALREADY_STARTED:
                    Log.e(TAG, "Fails to start scan as BLE scan with the same settings is already started by the app.");
                    break;
                case SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                    Log.e(TAG, "Fails to start scan as app cannot be registered.");
                    break;
                case SCAN_FAILED_FEATURE_UNSUPPORTED:
                    Log.e(TAG, "Fails to start power optimized scan as this feature is not supported.");
                    break;
                default: // SCAN_FAILED_INTERNAL_ERROR
                    Log.e(TAG, "Fails to start scan due an internal error");
            }
        }
    };

    private BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, String.format("onConnectionStateChange() status=%d, state=%d", status, newState));

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "state=STATE_CONNECTED");
                discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "state=STATE_DISCONNECTED");
                startScan();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG, String.format("onServicesDiscovered(): device=%s, status=%d", gatt.getDevice().getAddress(), status));
            enableNotifications();
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, String.format("onCharacteristicChanged(): device=%s, uuid=%s", gatt.getDevice().getAddress(), characteristic.getUuid()));

            bluetoothHeartRateRequestHandler.onCharacteristicChange(characteristic);
        }
    };

    public void start(BluetoothActionsListener bluetoothActionsListener) {
        Log.d(TAG, "Start client");
        bluetoothHeartRateRequestHandler.addListener(bluetoothActionsListener);
        startScan();
    }

    public void stop() {
        Log.d(TAG, "Stop client");
        disableNotifications();
        disconnect();
        close();
    }

    private void startScan() {
        bluetoothScanner.startScanLeDevice(new UUID[] {HeartRateServiceManager.HEART_RATE_SERVICE_UUID});
    }

    private void stopScan() {
        bluetoothScanner.stopScanLeDevice();
    }

    private void connect(BluetoothDevice bluetoothDevice) {
        Log.d(TAG, String.format("Connecting to server: %s", bluetoothDevice.getAddress()));
        try {
            stopScan();
            bluetoothHeartRateRequestHandler.connect(bluetoothDevice.getAddress(), mBluetoothGattCallback, context);
        } catch (GattException e) {
            Log.e(TAG, String.format("Connecting to server failed: %s", e.getMessage()));
        }
    }

    private void disconnect() {
        Log.d(TAG, "Disconnecting from server");
        bluetoothHeartRateRequestHandler.disconnect();
    }

    private void close() {
        Log.d(TAG, "Closing server");
        bluetoothHeartRateRequestHandler.close();
    }

    private void discoverServices() {
        Log.d(TAG, "Discovering services");
        bluetoothHeartRateRequestHandler.discoverServices();
    }

    private void enableNotifications() {
        Log.d(TAG, "Enabling notifications from server");
        bluetoothHeartRateRequestHandler.setCharacteristicNotification(true);
    }

    private void disableNotifications() {
        Log.d(TAG, "Disabling notifications from server");
        bluetoothHeartRateRequestHandler.setCharacteristicNotification(false);
    }
}
