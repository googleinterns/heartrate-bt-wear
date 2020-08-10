package com.google.heartrate.wearos.app.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Listener for heart rate sensor.
 */
public class HeartRateSensorListener implements SensorEventListener {
    private static final String TAG = HeartRateSensorListener.class.getSimpleName();

    /** Sensor manager. */
    private final SensorManager mSensorManager;

    /** Heart rate sensor to listen to. */
    private final Sensor mHeartRateSensor;

    /** Subscriber for heart rate value updates. */
    private List<HeartRateValueSubscriber> subscribers = new ArrayList<>();

    public HeartRateSensorListener(Context context) {
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
    }

    /**
     * Register subscriber.
     * @param subscriber subscriber to register.
     */
    public void registerSubscriber(HeartRateValueSubscriber subscriber) {
        Log.d(TAG, "Register subscriber");
        if (subscribers.size() == 0) {
            startMeasure();
        }
        subscribers.add(subscriber);
    }

    /**
     * Unregister subscriber.
     * @param subscriber subscriber to unregister.
     */
    public void unregisterSubscriber(HeartRateValueSubscriber subscriber) {
        Log.d(TAG, "Unregister subscriber");
        subscribers.remove(subscriber);
        if (subscribers.size() == 0) {
            stopMeasure();
        }
    }

    /**
     * Start measurement heart rate data. Register to heart rate sensor changes.
     */
    private void startMeasure() {
        Log.d(TAG, "Start measurement");
        boolean sensorRegistered = mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_FASTEST);
        if (!sensorRegistered) {
            Log.e(TAG, "Heart rate sensor not registered");
        } else {
            Log.d(TAG, "Heart rate sensor registered");
        }

    }

    /**
     * Stop measurement heart rate data. Unregister from heart rate sensor changes.
     */
    private void stopMeasure() {
        Log.d(TAG, "Stop measurement");
        mSensorManager.unregisterListener(this);
    }

    /**
     * Callback invokes when heart rate value has been changed.
     * @param event event from heart rate sensor
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        float heartRateFloat = event.values[0];
        int heartRate = Math.round(heartRateFloat);
        Log.v(TAG, String.format("onSensorChanged() - value=%d", heartRate));

        for (HeartRateValueSubscriber listener : subscribers) {
            listener.onHeartRateValueChanged(heartRate);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "onAccuracyChanged() unsupported!");
    }
}
