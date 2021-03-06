package com.google.heartrate.wearos.app.sensors;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Listener for heart rate sensor.
 */
public class HeartRateSensorListener implements SensorEventListener {
    private static final String TAG = HeartRateSensorListener.class.getSimpleName();

    public static final String HEART_RATE_CHANGE = "com.google.app.androidos.walking.stepcontroller.HEART_RATE_CHANGE";
    public static final String HEART_RATE_VALUE = "com.google.app.androidos.walking.stepcontroller.HEART_RATE_VALUE";

    /** Heart rate sensor data when sensor is not available or started. */
    private static final int NO_VALUE_AVAILABLE = 0;

    /** Sensor manager. */
    private final SensorManager sensorManager;

    /** Heart rate sensor to listen to. */
    private final Sensor heartRateSensor;

    /** Application context. */
    private final Context context;

    /** Heart rate value from last sensor update. */
    private int currentHeartRateValue = NO_VALUE_AVAILABLE;

    public HeartRateSensorListener(Context context) {
        this.context = context;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
    }

    /**
     * Start measurement heart rate data. Register to heart rate sensor changes.
     *
     * @throws SensorException when cannot start measurement
     */
    public void startMeasure() throws SensorException {
        Log.d(TAG, "Start measurement");
        boolean sensorRegistered = sensorManager
                .registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL, 0);
        if (!sensorRegistered) {
            throw new SensorException("Heart rate sensor not registered");
        } else {
            Log.d(TAG, "Heart rate sensor registered");
        }
    }

    /**
     * Stop measurement heart rate data. Unregister from heart rate sensor changes.
     */
    public void stopMeasure() {
        Log.d(TAG, "Stop measurement");
        sensorManager.unregisterListener(this);
    }

    /**
     * Callback invokes when heart rate value has been changed.
     *
     * @param event event from heart rate sensor
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        float heartRateFloat = event.values[0];
        int heartRate = Math.round(heartRateFloat);
        Log.d(TAG, String.format("onSensorChanged() - value=%d", heartRate));

        currentHeartRateValue = heartRate;

        Intent intent = new Intent(HEART_RATE_CHANGE);
        intent.putExtra(HEART_RATE_VALUE, heartRate);

        context.sendBroadcast(intent);
    }

    /**
     * Get last heart rate value got from sensor.
     *
     * @return last heart rate value got from sensor
     * @throws SensorException if cannot get last heart rate value got from sensor
     */
    public int getCurrentHeartRateValue() throws SensorException {
        if (currentHeartRateValue == NO_VALUE_AVAILABLE) {
            throw new SensorException("No data available in heart rate sensor");
        }
        return currentHeartRateValue;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "onAccuracyChanged() unsupported!");
    }
}
