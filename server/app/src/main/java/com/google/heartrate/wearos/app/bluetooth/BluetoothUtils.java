package com.google.heartrate.wearos.app.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.pm.PackageManager;

import com.google.heartrate.wearos.app.gatt.GattException;

import static android.content.Context.BLUETOOTH_SERVICE;

/**
 * Provides bluetooth related objects and checks.
 */
public class BluetoothUtils {
    private static final String TAG = BluetoothUtils.class.getSimpleName();

    /**
     * Check if bluetooth is supported on current device.
     * @param context context of application
     */
    public static void assertBluetoothIsSupported(Context context) throws GattException {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(BLUETOOTH_SERVICE);
        if (bluetoothManager == null) {
            throw new GattException("Bluetooth is not supported. No BluetoothManager found.");
        }

        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            throw new GattException("Bluetooth is not supported. No BluetoothAdapter found.");
        }

        if (!bluetoothAdapter.isEnabled()) {
            throw new GattException("Bluetooth adapter is currently disabled");
        }

        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            throw new GattException("Bluetooth LE  is not supported. No FEATURE_BLUETOOTH_LE found.");
        }
    }

    /**
     * Get bluetooth manager.
     * @param context context of application
     * @return bluetooth manager
     */
    public static BluetoothManager getBluetoothManager(Context context) throws GattException {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null) {
            throw new GattException("Bluetooth is not supported. No BluetoothManager found.");
        }
        return bluetoothManager;
    }

    /**
     * Get bluetooth adapter.
     * @param context context of application
     * @return bluetooth adapter
     */
    public static BluetoothAdapter getBluetoothAdapter(Context context) throws GattException {
        BluetoothManager bluetoothManager = getBluetoothManager(context);
        BluetoothAdapter adapter = bluetoothManager.getAdapter();
        if (adapter == null) {
            throw new GattException("Bluetooth is not supported. No BluetoothAdapter found.");
        }

        return adapter;
    }

    /**
     * Get bluetooth adapter.
     * @param context context of application
     * @return bluetooth adapter
     */
    public static BluetoothLeAdvertiser getBluetoothLeAdvertiser(Context context) throws GattException {
        BluetoothAdapter adapter = getBluetoothAdapter(context);
        BluetoothLeAdvertiser advertiser = adapter.getBluetoothLeAdvertiser();
        if (advertiser == null) {
            throw new GattException("Bluetooth advertising is not supported. Can not get BluetoothLeAdvertiser.");
        }

        return advertiser;
    }

    public static BluetoothGattServer getBluetoothGattServer(Context context, BluetoothGattServerCallback callback) throws GattException {
        BluetoothGattServer server = getBluetoothManager(context)
                .openGattServer(context, callback);

        if (server == null) {
            throw new GattException("Unable to create GATT server");
        }

        return server;
    }
}
