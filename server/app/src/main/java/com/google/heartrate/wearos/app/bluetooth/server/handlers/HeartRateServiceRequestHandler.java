package com.google.heartrate.wearos.app.bluetooth.server.handlers;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.util.Log;

import com.google.heartrate.wearos.app.bluetooth.server.BluetoothDeviceStorage;
import com.google.heartrate.wearos.app.bluetooth.server.BluetoothServerCallback;
import com.google.heartrate.wearos.app.gatt.GattException;
import com.google.heartrate.wearos.app.gatt.attributes.GattCharacteristic;
import com.google.heartrate.wearos.app.gatt.attributes.GattDescriptor;
import com.google.heartrate.wearos.app.gatt.attributes.GattService;
import com.google.heartrate.wearos.app.gatt.heartrate.characteristics.BodySensorLocationCharacteristic;
import com.google.heartrate.wearos.app.gatt.heartrate.characteristics.HeartRateControlPointCharacteristic;
import com.google.heartrate.wearos.app.gatt.heartrate.characteristics.HeartRateMeasurementCharacteristic;
import com.google.heartrate.wearos.app.gatt.heartrate.service.HeartRateGattService;
import com.google.heartrate.wearos.app.sensors.HeartRateSensorListener;
import com.google.heartrate.wearos.app.sensors.HeartRateValueSubscriber;

import java.util.Arrays;

/**
 * {@link GattServiceRequestHandler} for Heart Rate service.
 *
 * <p>Provides methods to handle request from remote devices to Heart Rate service.
 */
public class HeartRateServiceRequestHandler implements GattServiceRequestHandler, HeartRateValueSubscriber {
    private static final String TAG = HeartRateServiceRequestHandler.class.getSimpleName();

    /** {@link GattService} for Heart Rate service. */
    private final HeartRateGattService heartRateGattService;

    /** Sensor listener for heart rate sensor. */
    private final HeartRateSensorListener heartRateSensorListener;

    /** {@link BluetoothServerCallback} to interact with remote device. */
    private BluetoothServerCallback bluetoothServerCallback;

    /** Storage for all {@link BluetoothDevice} registered to Heart Rate Measurement characteristic. */
    private BluetoothDeviceStorage registeredDeviceStorage;

    public HeartRateServiceRequestHandler(HeartRateSensorListener heartRateSensorListener) {
        this.heartRateSensorListener = heartRateSensorListener;
        heartRateGattService = new HeartRateGattService();
        registeredDeviceStorage = new BluetoothDeviceStorage();
    }

    /**
     * {@link GattServiceRequestHandler#onServiceAdded}
     * <p>Register to {@link HeartRateSensorListener} to receive heart rate from sensor.
     */
    @Override
    public void onServiceAdded(BluetoothServerCallback bluetoothServerCallback) {
        this.bluetoothServerCallback = bluetoothServerCallback;
        heartRateSensorListener.registerSubscriber(this);
    }

    /**
     * {@link GattServiceRequestHandler#onServiceAdded}
     * <p>Unregister to {@link HeartRateSensorListener}.
     */
    @Override
    public void onServiceRemoved() {
        heartRateSensorListener.unregisterSubscriber(this);
        registeredDeviceStorage.removeAllDevices();
    }

    /**
     * Unregister device from notifications about heart rate characteristic changed.
     * @param device connected device
     */
    @Override
    public void onDeviceDisconnected(BluetoothDevice device) {
        registeredDeviceStorage.removeDevice(device);
    }

    /**
     * Read {@link BodySensorLocationCharacteristic} value.
     *
     * @param device the remote device that has requested the read operation
     * @param characteristic characteristic to be read
     * @param offset offset into the value of the characteristic
     * @throws GattException when requested not {@link BodySensorLocationCharacteristic} or cannot read it
     */
    @Override
    public byte[] onCharacteristicRead(BluetoothDevice device, BluetoothGattCharacteristic characteristic, int offset) throws GattException {
        GattCharacteristic bodySensorLocationCharacteristic = heartRateGattService
                .getBodySensorLocationCharacteristic();
        if (characteristic.getUuid() != bodySensorLocationCharacteristic.getUUid()) {
            throw new GattException(String.format("Unsupported characteristic %s", characteristic.getUuid()));
        }
        return bodySensorLocationCharacteristic.read(offset);
    }

    /**
     * Read {@link HeartRateControlPointCharacteristic} value.
     *
     * @param device the remote device that has requested the write operation
     * @param characteristic characteristic to be write
     * @param offset offset into the value of the characteristic
     * @param value value the client wants to assign to the characteristic
     * @throws GattException when requested not {@link HeartRateControlPointCharacteristic} or cannot read it
     */
    @Override
    public void onCharacteristicWrite(BluetoothDevice device, BluetoothGattCharacteristic characteristic, int offset, byte[] value) throws GattException {
        GattCharacteristic heartRateControlPointCharacteristic = heartRateGattService
                .getHeartRateControlPointCharacteristic();
        if (characteristic.getUuid() != heartRateControlPointCharacteristic.getUUid()) {
            throw new GattException(String.format("Unsupported characteristic %s", characteristic.getUuid()));
        }
        heartRateControlPointCharacteristic.write(offset, value);
    }

    /**
     * Determine wether remote device is registered for notifications about Heart Rate Measurement characteristic change or not.
     *
     * <p>Read descriptor request for Heart Rate service is the way for remote device to check status of registration
     * to Heart Rate Measurement characteristic.
     *
     * @param device the remote device that has requested the read operation
     * @param descriptor descriptor to be read
     * @param offset offset into the value of the descriptor
     * @return {@link BluetoothGattDescriptor#ENABLE_NOTIFICATION_VALUE} if device is registered for notifications,
     * {@link BluetoothGattDescriptor#DISABLE_NOTIFICATION_VALUE} otherwise
     */
    @Override
    public byte[] onDescriptorRead(BluetoothDevice device, BluetoothGattDescriptor descriptor, int offset) throws GattException {
        GattDescriptor clientCharacteristicConfigurationDescriptor = heartRateGattService
                .getHeartRateMeasurementCharacteristic()
                .getClientCharacteristicConfigurationDescriptor();
        if (descriptor.getUuid() != clientCharacteristicConfigurationDescriptor.getUUid()) {
            throw new GattException(String.format("Unsupported descriptor %s", descriptor.getUuid()));
        }

        if (registeredDeviceStorage.contains(device)) {
            return BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
        } else {
            return BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
        }
    }

    /**
     * Register (if given value is {@link BluetoothGattDescriptor#ENABLE_NOTIFICATION_VALUE}) or
     * unregister (if given value is {@link BluetoothGattDescriptor#DISABLE_NOTIFICATION_VALUE})
     * remote device for notifications about Heart Rate Measurement characteristic change.
     *
     * <p>Write descriptor request for Heart Rate service is the way for remote device to subscribe/unsubscribe
     * for Heart Rate Measurement characteristic change notification.
     *
     * @param device the remote device that has requested the read operation
     * @param descriptor descriptor to be read
     * @param offset offset into the value of the descriptor
     */
    @Override
    public void onDescriptorWrite(BluetoothDevice device, BluetoothGattDescriptor descriptor, int offset, byte[] value) throws GattException {
        GattDescriptor clientCharacteristicConfigurationDescriptor = heartRateGattService
                .getHeartRateMeasurementCharacteristic()
                .getClientCharacteristicConfigurationDescriptor();
        if (descriptor.getUuid() != clientCharacteristicConfigurationDescriptor.getUUid()) {
            throw new GattException(String.format("Unsupported descriptor %s", descriptor.getUuid()));
        }

        if (Arrays.equals(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE, value)) {
            Log.d(TAG, String.format("Subscribe device %s to notifications", device));
            registeredDeviceStorage.addDevice(device);
        } else if (Arrays.equals(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE, value)) {
            Log.d(TAG, String.format("Unsubscribe device %s from notifications", device));
            registeredDeviceStorage.removeDevice(device);
        }
    }

    /**
     * Callback to listen to {@link HeartRateSensorListener} about heart rate value change.
     * <p>Update Heart Rate measurement characteristic value with given and send notification about
     * Heart Rate measurement characteristic change to server via {@link BluetoothServerCallback}
     * @param value new heart rate value
     */
    @Override
    public void onHeartRateValueChanged(int value) {
        try {
            HeartRateMeasurementCharacteristic heartRateMeasurementCharacteristic
                = heartRateGattService.getHeartRateMeasurementCharacteristic();
            heartRateMeasurementCharacteristic.setHeartRateCharacteristicValue(value , 42); //TODO: remove constant

            bluetoothServerCallback.onCharacteristicChanged(heartRateMeasurementCharacteristic.getBluetoothGattCharacteristic(),
                    registeredDeviceStorage.getAllDevices());

        } catch (GattException e) {
            e.printStackTrace();
        }
    }

    @Override
    public GattService getGattService() {
        return heartRateGattService;
    }
}
