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

    private BluetoothUtils() {}

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
            throw new GattException("Bluetooth LE is not supported. No FEATURE_BLUETOOTH_LE found.");
        }
    }

    /**
     * Get {@link BluetoothManager}.
     *
     * @param context application context
     * @return {@link BluetoothManager}
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
     * Get {@link BluetoothAdapter}.
     *
     * @param context application context
     * @return {@link BluetoothAdapter}
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
     * Get {@link BluetoothLeAdvertiser}.
     *
     * @param context application context
     * @return {@link BluetoothLeAdvertiser}
     * @throws GattException if bluetooth advertising is not supported
     */
    public static BluetoothLeAdvertiser getBluetoothLeAdvertiser(Context context) throws GattException {
        BluetoothAdapter adapter = getBluetoothAdapter(context);
        BluetoothLeAdvertiser advertiser = adapter.getBluetoothLeAdvertiser();

        if (advertiser == null) {
            throw new GattException("Bluetooth advertising is not supported. Cannot get BluetoothLeAdvertiser.");
        }

        return advertiser;
    }

    /**
     * Get {@link BluetoothGattServer}.
     *
     * @param context application context
     * @param callback {@link BluetoothGattServerCallback} to configure {@link BluetoothGattServer} with
     * @return {@link BluetoothGattServer} configured with given {@link BluetoothGattServerCallback}
     * @throws GattException if unable to create {@link BluetoothGattServer}
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
