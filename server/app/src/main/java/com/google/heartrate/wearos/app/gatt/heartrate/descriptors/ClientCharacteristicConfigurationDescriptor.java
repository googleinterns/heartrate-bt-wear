package com.google.heartrate.wearos.app.gatt.heartrate.descriptors;

import android.bluetooth.BluetoothGattDescriptor;
import android.util.Log;

import com.google.heartrate.wearos.app.gatt.GattException;
import com.google.heartrate.wearos.app.gatt.attributes.GattDescriptor;

import java.util.UUID;

/**
 * ClientCharacteristicConfigurationDescriptorManager class inherits
 * {@link GattDescriptor}
 * and specifies set/get operation for Heart Rate Control Point characteristic.
 */
public class ClientCharacteristicConfigurationDescriptor extends GattDescriptor {
    private static final String TAG = ClientCharacteristicConfigurationDescriptor.class.getCanonicalName();

    /** Client Characteristic Configuration descriptor UUID.  */
    public static final UUID CLIENT_CHARACTERISTIC_CONFIGURATION_UUID = UUID
            .fromString("00002902-0000-1000-8000-00805f9b34fb");

    /**
     * Create {@link GattDescriptor} for Client Characteristic Configuration descriptor.
     * <br>Configure {@link GattDescriptor} with Client Characteristic Configuration UUID,
     * read/write permissions and enabled notifications.
     */
    public ClientCharacteristicConfigurationDescriptor() {
        super(CLIENT_CHARACTERISTIC_CONFIGURATION_UUID,
                (BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE));
        try {
            Log.e(TAG, "Enabling notifications");
            setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        } catch (GattException e) {
            Log.e(TAG, "Can not enable notifications");
        }
    }
}
