package com.google.heartrate.wearos.app.bluetooth.server;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

/**
 * Storage for {@link BluetoothDevice} registered for particular characteristic notification.
 */
public class BluetoothDeviceStorage {
    private static final String TAG = BluetoothDeviceStorage.class.getSimpleName();

    /** Set of stored devices. */
    private final Set<BluetoothDevice> mBluetoothDevices = new HashSet<>();

    /**
     * Save device in storage.
     *
     * @param device registered device
     */
    public void addDevice(BluetoothDevice device) {
        Log.d(TAG, String.format("Add device %s", device.getAddress()));
        mBluetoothDevices.add(device);
    }

    /**
     * Remove device from storage.
     *
     * @param device unregistered device
     */
    public void removeDevice(BluetoothDevice device) {
        Log.d(TAG, String.format("Remove device %s", device.getAddress()));
        mBluetoothDevices.remove(device);
    }

    /**
     * Remove all devices from storage.
     */
    public void removeAllDevices() {
        Log.d(TAG, String.format("Unregister all %d devices", mBluetoothDevices.size()));
        mBluetoothDevices.clear();
    }

    /**
     * Get all devices from storage.
     *
     * @return set of registered devices
     */
    public Set<BluetoothDevice> getAllDevices() {
        Log.d(TAG, String.format("Get %d devices", mBluetoothDevices.size()));
        return mBluetoothDevices;
    }

    /**
     * Determine wether storage contains given device or not.
     *
     * @param device device to check
     * @return true if storage contains given device, otherwise false
     */
    public boolean contains(BluetoothDevice device) {
        return mBluetoothDevices.contains(device);
    }
}
