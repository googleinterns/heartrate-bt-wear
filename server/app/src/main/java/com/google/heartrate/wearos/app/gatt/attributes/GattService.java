package com.google.heartrate.wearos.app.gatt.attributes;

import android.bluetooth.BluetoothGattService;

import com.google.heartrate.wearos.app.gatt.GattException;

import java.util.HashMap;
import java.util.UUID;

/**
 * {@link GattService} is the wrapper over {@link BluetoothGattService} class.
 * <p>
 * {@link GattService} provides method which are common for all GATT services.
 * This abstraction makes all operations with {@link BluetoothGattService}
 * expandable for each specific service type.
 */
public class GattService implements GattAttribute {

    /** {@link BluetoothGattService} for service. */
    private final BluetoothGattService mBluetoothGattService;

    /** Characteristics which service include. */
    private final HashMap<UUID, GattCharacteristic> characteristicByUuid = new HashMap<>();

    /**
     * Configure {@link BluetoothGattService} with given parameters.
     * @param uuid service uuid
     * @param type service type
     */
    public GattService(UUID uuid, int type) {
        mBluetoothGattService = new BluetoothGattService(uuid, type);
    }

    /**
     * Configure {@link BluetoothGattService} with given parameters and characteristics.
     * @param uuid service uuid
     * @param type service type
     * @param gattCharacteristics characteristic managers
     */
    public GattService(UUID uuid, int type, GattCharacteristic[] gattCharacteristics) {
        this(uuid, type);

        for (GattCharacteristic gattCharacteristic : gattCharacteristics) {
            characteristicByUuid.put(gattCharacteristic.getUUid(), gattCharacteristic);
            mBluetoothGattService.addCharacteristic(gattCharacteristic.getBluetoothGattCharacteristic());
        }
    }

    /**
     * Determine weather service has characteristic with given uuid or not.
     *
     * @param uuid characteristic uuid to check
     * @return true if service has characteristic, false otherwise
     */
    public boolean hasCharacteristic(UUID uuid) {
        return characteristicByUuid.containsKey(uuid);
    }

    /**
     * Get {@link GattCharacteristic} for characteristic in service with given uuid.
     *
     * @param characteristicUuid characteristic uuid
     * @return {@link GattCharacteristic} for service
     * @throws GattException if service does not support characteristic with given uuid
     */
    public GattCharacteristic getCharacteristic(UUID characteristicUuid) throws GattException {
        GattCharacteristic characteristic = characteristicByUuid.get(characteristicUuid);
        if (characteristic == null) {
            throw new GattException(String.format("Service %s does not have characteristic %s",
                    getUUid(), characteristicUuid));
        }
        return characteristic;
    }

    /**
     * Get {@link BluetoothGattService} for service.
     *
     * @return {@link BluetoothGattService} for service
     */
    public BluetoothGattService getBluetoothGattService() {
        return mBluetoothGattService;
    }

    @Override
    public UUID getUUid() {
        return mBluetoothGattService.getUuid();
    }
}
