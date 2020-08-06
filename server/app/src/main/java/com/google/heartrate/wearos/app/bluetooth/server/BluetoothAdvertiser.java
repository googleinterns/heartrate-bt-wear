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
 * Peripheral uses advertising to allow centrals to find it and, subsequently, to establish a connection with it.
 * <p>
 * A peripheral can use {@link BluetoothAdvertiser} to advertise its existence and show it's complete or partial
 * list of GATT services it offers.
 *
 */
public class BluetoothAdvertiser {
    private static final String TAG = BluetoothAdvertiser.class.getSimpleName();

    /** {@link BluetoothAdvertiser} for advertising process. */
    private final BluetoothLeAdvertiser mBluetoothLeAdvertiser;

    /** Advertising callback to receive async responses from {@link BluetoothAdvertiser} . */
    final AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
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
        mBluetoothLeAdvertiser = BluetoothUtils.getBluetoothLeAdvertiser(context);
    }

    /**
     * Start advertising centrals about peripheral existence.
     *
     * @param serviceUuids list of GATT services peripheral offers
     */
    public void start(Set<UUID> serviceUuids) {
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

        mBluetoothLeAdvertiser.startAdvertising(
                advertiseSettings.build(),
                advertiseData.build(),
                mAdvertiseCallback);
    }

    /**
     * Stop advertising centrals about peripheral existence.
     */
    public void stop() {
        Log.d(TAG, "Stopping advertising");

        mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
    }
}
