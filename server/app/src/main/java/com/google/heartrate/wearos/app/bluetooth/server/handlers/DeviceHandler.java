package com.google.heartrate.wearos.app.bluetooth.server.handlers;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

public class DeviceHandler {
    private static final String TAG = DeviceHandler.class.getSimpleName();

    /** Set of registered for notifications devices (centrals). */
    private final Set<BluetoothDevice> mRegisteredDevices = new HashSet<>();

    public void registerDevice(BluetoothDevice device) {
        Log.d(TAG, String.format("Register device %s", device.getAddress()));
        mRegisteredDevices.add(device);
    }

    public void unregisterDevice(BluetoothDevice device) {
        Log.d(TAG, String.format("Unregister device %s", device.getAddress()));
        mRegisteredDevices.remove(device);
    }

    public void unregisterAll() {
        mRegisteredDevices.clear();
    }

    public Set<BluetoothDevice> getAllRegisteredDevices() {
        return mRegisteredDevices;
    }

    public boolean contains(BluetoothDevice device) {
        return mRegisteredDevices.contains(device);
    }
}
