package com.google.heartrate.wearos.app.bluetooth.server.notifiers;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.google.heartrate.wearos.app.bluetooth.server.BluetoothDeviceStorage;
import com.google.heartrate.wearos.app.bluetooth.server.BluetoothServerCallback;
import com.google.heartrate.wearos.app.gatt.GattException;
import com.google.heartrate.wearos.app.gatt.heartrate.characteristics.HeartRateMeasurementCharacteristic;
import com.google.heartrate.wearos.app.sensors.HeartRateSensorListener;
import com.google.heartrate.wearos.app.sensors.SensorException;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Class {@link HeartRateCharacteristicNotifier} provides methods to schedule notification operations
 * about Heart Rate Measurement characteristic change.
 */
public class HeartRateCharacteristicNotifier {
    private static final String TAG = HeartRateCharacteristicNotifier.class.getSimpleName();

    /**  Delay before first notification. */
    private static final long NOTIFY_FIRST_WAIT_PERIOD_MS = 1000;

    /** Period between successive notifications. */
    private static final long NOTIFY_WAIT_PERIOD_MS = 30 * 1000;

    /** Instance of {@link ScheduledExecutorService} for notification scheduling. */
    private ScheduledExecutorService notificationExecutor = Executors.newSingleThreadScheduledExecutor();

    /** {@link ScheduledFuture} of notification timer. */
    private ScheduledFuture<?> notificationTimerFuture;

    /** Heart Rate Measurement characteristic to notify about. */
    private final HeartRateMeasurementCharacteristic heartRateMeasurementCharacteristic;

    /** Heart Rate sensor to get Heart Rate Measurement value from. */
    private final HeartRateSensorListener heartRateSensorListener;

    /** Storage for all {@link BluetoothDevice} registered to Heart Rate Measurement characteristic. */
    private final BluetoothDeviceStorage registeredDeviceStorage;

    public HeartRateCharacteristicNotifier(HeartRateMeasurementCharacteristic heartRateMeasurementCharacteristic,
                                           HeartRateSensorListener heartRateSensorListener,
                                           BluetoothDeviceStorage registeredDeviceStorage) {
        this.heartRateMeasurementCharacteristic = heartRateMeasurementCharacteristic;
        this.heartRateSensorListener = heartRateSensorListener;
        this.registeredDeviceStorage = registeredDeviceStorage;
    }

    public void changeCharacteristic() throws GattException {
        try {
            heartRateMeasurementCharacteristic
                    .setHeartRateCharacteristicValue(heartRateSensorListener.getCurrentHeartRateValue(), Optional.empty());
        } catch (SensorException e) {
            Log.e(TAG, String.format("Can not get value from sensor: %s", e.getMessage()));
        }
    }

    private void notifyCharacteristicChanged(BluetoothServerCallback bluetoothServerCallback) {
        bluetoothServerCallback.onCharacteristicChanged(
                heartRateMeasurementCharacteristic.getBluetoothGattCharacteristic(),
                registeredDeviceStorage.getAllDevices());
    }

    /** Start scheduled notification with default parameters. */
    public void start(BluetoothServerCallback bluetoothServerCallback) {
        stop();
        start(NOTIFY_FIRST_WAIT_PERIOD_MS, NOTIFY_WAIT_PERIOD_MS, bluetoothServerCallback);
    }

    /**
     * Start scheduled run with given parameters.
     * @param firstWaitPeriod delay before first notification (seconds)
     * @param waitPeriod period between successive notifications (seconds)
     */
    public void start(long firstWaitPeriod, long waitPeriod, BluetoothServerCallback bluetoothServerCallback) {
        notificationTimerFuture = notificationExecutor.scheduleWithFixedDelay(() -> {
            try {
                changeCharacteristic();
                notifyCharacteristicChanged(bluetoothServerCallback);
            } catch (GattException e) {
                Log.e(TAG, e.getMessage());
            }
        }, firstWaitPeriod, waitPeriod, TimeUnit.MILLISECONDS);
    }

    /** Stop scheduled run. */
    public void stop() {
        if (notificationTimerFuture != null) {
            notificationTimerFuture.cancel(true);
            notificationTimerFuture = null;
        }
    }
}
