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
     * Assert that bluetooth is supported in current device.
     *
     * @param context application context
     * @throws GattException if bluetooth is not supported
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
            throw new GattException("Bluetooth adapter is currently disabled.");
        }

        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            throw new GattException("Bluetooth LE  is not supported. No FEATURE_BLUETOOTH_LE found.");
        }
    }

    /**
     * Get bluetooth manager.
     *
     * @param context application context
     * @return bluetooth manager
     * @throws GattException if bluetooth is not supported
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
     *
     * @param context application context
     * @return bluetooth adapter
     * @throws GattException if bluetooth is not supported
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
     * Get bluetooth advertiser.
     *
     * @param context application context
     * @return bluetooth advertiser
     * @throws GattException if bluetooth advertising is not supported
     */
    public static BluetoothLeAdvertiser getBluetoothLeAdvertiser(Context context) throws GattException {
        BluetoothAdapter adapter = getBluetoothAdapter(context);
        BluetoothLeAdvertiser advertiser = adapter.getBluetoothLeAdvertiser();

        if (advertiser == null) {
            throw new GattException("Bluetooth advertising is not supported. Can not get BluetoothLeAdvertiser.");
        }

        return advertiser;
    }

    /**
     * Get bluetooth gatt server.
     *
     * @param context application context
     * @param callback server callback
     * @return bluetooth gatt server
     * @throws GattException if unable to create gatt server
     */
    public static BluetoothGattServer getBluetoothGattServer(Context context, BluetoothGattServerCallback callback) throws GattException {
        BluetoothManager bluetoothManager = getBluetoothManager(context);
        BluetoothGattServer server = bluetoothManager.openGattServer(context, callback);

        if (server == null) {
            throw new GattException("Unable to create GATT server.");
        }

        return server;
    }
}
