package com.google.heartrate.wearos.app.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class HeartRateSensorListener implements SensorEventListener {

    private static final String TAG = HeartRateSensorListener.class.getSimpleName();

    private final SensorManager mSensorManager;
    private final Sensor mHeartRateSensor;

    private List<HeartRateValueSubscriber> subscribers = new ArrayList<>();

    public HeartRateSensorListener(Context context) {
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
    }

    public void registerSubscriber(HeartRateValueSubscriber subscriber) {
        if (subscribers.size() == 0) {
            startMeasure();
        }
        subscribers.add(subscriber);
    }

    public void unregisterSubscriber(HeartRateValueSubscriber subscriber) {
        subscribers.remove(subscriber);
        if (subscribers.size() == 0) {
            stopMeasure();
        }
    }

    private void startMeasure() {
        boolean sensorRegistered = mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_FASTEST);
        Log.d("Sensor Status:", " Sensor registered: " + (sensorRegistered ? "yes" : "no"));
    }

    private void stopMeasure() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float heartRateFloat = event.values[0];
        int heartRate = Math.round(heartRateFloat);
        Log.v(TAG, String.format("HR: %d", heartRate));

        for (HeartRateValueSubscriber listener : subscribers) {
            listener.onHeartRateValueChanged(heartRate);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
