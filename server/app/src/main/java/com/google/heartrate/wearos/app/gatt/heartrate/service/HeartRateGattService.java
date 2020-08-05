package com.google.heartrate.wearos.app.gatt.heartrate.service;

import android.bluetooth.BluetoothGattService;

import com.google.heartrate.wearos.app.gatt.attributes.GattCharacteristic;
import com.google.heartrate.wearos.app.gatt.attributes.GattService;
import com.google.heartrate.wearos.app.gatt.heartrate.characteristics.HeartRateMeasurementCharacteristic;
import com.google.heartrate.wearos.app.gatt.heartrate.descriptors.ClientCharacteristicConfigurationDescriptor;

import java.util.UUID;

/**
 * HeartRateServiceManager class inherits {@link GattService}
 * and specifies set/get operation for characteristics in Heart Rate service.
 * It provides heart rate, expended energy and other data related to a heart rate sensor.
 * <p>
 * Characteristic included in the service:
 * <table>
 *  <thead>
 *      <tr><th>Characteristic/Descriptors</th><th>Requirement</th><th>Properties</th></tr>
 *  <thead>
 *  <tbody>
 *      <tr><td> Heart Rate Measurement </td><td> Mandatory </td><td> Notify </td></tr>
 *      <tr><td> Client Characteristic Configuration descriptor </td><td> Mandatory </td><td> Read, Write </td></tr>
 *      <tr><td> Body Sensor Location </td><td> Optional </td><td> Read </td></tr>
 *      <tr><td> Heart Rate Control Point </td><td> Optional, Mandatory if Energy Expended </td><td> Write </td></tr>
 *  </tbody>
 * </table>
 * <p>
 * <p>
 * See <a href="https://www.bluetooth.com/wp-content/uploads/Sitecore-Media-Library/Gatt/Xml/Services/org.bluetooth.service.heart_rate.xml">
 * Heart Rate Service</a>.
 */
public class HeartRateGattService extends GattService {
    private static final String TAG = HeartRateGattService.class.getCanonicalName();

    /** Heart Rate service UUID. */
    private static final UUID HEART_RATE_SERVICE_UUID = UUID
            .fromString("0000180d-0000-1000-8000-00805f9b34fb");

    public HeartRateGattService() {
        super(HEART_RATE_SERVICE_UUID,
                BluetoothGattService.SERVICE_TYPE_PRIMARY,
                createCharacteristicManagers());
    }

    /**
     * Create characteristicManagers for Heart Rate service.
     *
     * @return CharacteristicManagers for Heart Rate service
     */
    private static GattCharacteristic[] createCharacteristicManagers() {
        return new GattCharacteristic[]{
                new HeartRateMeasurementCharacteristic()};
    }
}
