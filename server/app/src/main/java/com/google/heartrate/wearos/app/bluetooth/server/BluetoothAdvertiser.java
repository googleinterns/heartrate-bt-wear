package com.google.heartrate.wearos.app.bluetooth.server;

import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;

import com.google.heartrate.wearos.app.bluetooth.BluetoothUtils;
import com.google.heartrate.wearos.app.gatt.GattException;

import java.util.Set;
import java.util.UUID;

/**
 * {@link BluetoothAdvertiser} is the wrapper over {@link BluetoothLeAdvertiser} class.
 * <p>
 * Advertising allows devices to broadcast information defining their intentions.
 * Server uses advertising to allow clients to find it and, subsequently, to establish a connection with it.
 * <p>
 * A server can use {@link BluetoothAdvertiser} to advertise its existence and show it's complete or partial
 * list of GATT services it offers.
 *
 */
public class BluetoothAdvertiser {
    private static final String TAG = BluetoothAdvertiser.class.getSimpleName();

    /** {@link BluetoothAdvertiser} for advertising process. */
    private final BluetoothLeAdvertiser bluetoothLeAdvertiser;

    /** Advertising callback to receive async responses from {@link BluetoothAdvertiser} . */
    final AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.i(TAG, "BluetoothLE Advertise Started.");
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.e(TAG, String.format("BluetoothLE Advertise Failed: %d",  + errorCode));
        }
    };

    public BluetoothAdvertiser(Context context) throws GattException {
        bluetoothLeAdvertiser = BluetoothUtils.getBluetoothLeAdvertiser(context);
    }

    /**
     * Start advertising to clients about server existence.
     *
     * @param serviceUuids list of GATT services server offers
     */
    public void startAdvertisingServices(Set<UUID> serviceUuids) {
        Log.d(TAG, "Starting advertising");

        AdvertiseSettings.Builder advertiseSettings = new AdvertiseSettings.Builder()
                /* Balanced between advertising frequency and power consumption */
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setTimeout(0)
                /* Medium transmission (TX) power level */
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM);

        AdvertiseData.Builder advertiseData = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .setIncludeTxPowerLevel(false);

        for (UUID serviceUuid : serviceUuids) {
            advertiseData.addServiceUuid(new ParcelUuid(serviceUuid));
        }

        bluetoothLeAdvertiser.startAdvertising(
                advertiseSettings.build(),
                advertiseData.build(),
                advertiseCallback);
    }

    /**
     * Stop advertising to clients about server existence.
     */
    public void stopAdvertisingServices() {
        Log.d(TAG, "Stopping advertising");

        bluetoothLeAdvertiser.stopAdvertising(advertiseCallback);
    }
}
