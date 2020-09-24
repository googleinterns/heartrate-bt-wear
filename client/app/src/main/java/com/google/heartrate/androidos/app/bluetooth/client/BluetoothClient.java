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

public class BluetoothClient {
    private static final String TAG = BluetoothClient.class.getSimpleName();

    private final BluetoothScanner bluetoothScanner;
    private final BluetoothHeartRateServerInteractor bluetoothHeartRateServerInteractor;
    private Context context;

    public BluetoothClient(Context context) throws GattException {
        this.context = context;
        bluetoothHeartRateServerInteractor = new BluetoothHeartRateServerInteractor();
        bluetoothScanner = new BluetoothScanner(this.context, scanCallback);
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.d(TAG, "onScanResult()");

            BluetoothDevice bluetoothDevice = result.getDevice();
            stopScan();
            if (!bluetoothHeartRateServerInteractor.isConnected()) {
                connect(bluetoothDevice);
            }
            if (!bluetoothHeartRateServerInteractor.isConnected()) {
                startScan();
            }
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

                bluetoothHeartRateServerInteractor.discoverServices(gatt);

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "state=STATE_DISCONNECTED");

                bluetoothHeartRateServerInteractor.enableCharacteristicNotification(gatt, false);
                startScan();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG, String.format("onServicesDiscovered(): device=%s, status=%d", gatt.getDevice().getAddress(), status));

            bluetoothHeartRateServerInteractor.enableCharacteristicNotification(gatt, true);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, String.format("onCharacteristicChanged(): device=%s, uuid=%s", gatt.getDevice().getAddress(), characteristic.getUuid()));
            bluetoothHeartRateServerInteractor.onCharacteristicChange(characteristic);
        }
    };

    public void start(BluetoothActionsListener bluetoothActionsListener) {
        Log.d(TAG, "Start client");

        bluetoothHeartRateServerInteractor.addListener(bluetoothActionsListener);
        startScan();
    }

    public void stop() {
        Log.d(TAG, "Stop client");

        stopScan();
        disconnect();
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
            bluetoothHeartRateServerInteractor.connect(bluetoothDevice.getAddress(), mBluetoothGattCallback, context);
        } catch (GattException e) {
            Log.e(TAG, String.format("Connecting to server failed: %s", e.getMessage()));
        }
    }

    private void disconnect() {
        Log.d(TAG, "Disconnecting from server");
        bluetoothHeartRateServerInteractor.disconnect();
        bluetoothHeartRateServerInteractor.close();
    }
}
