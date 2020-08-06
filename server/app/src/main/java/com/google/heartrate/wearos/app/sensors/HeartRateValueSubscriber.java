package com.google.heartrate.wearos.app.sensors;

/**
 * Interface for components witch want to receive heart rate value after it has been changed.
 */
public interface HeartRateValueSubscriber {

    /**
     * Callback to notify about heart rate value has been changed.
     * @param value new heart rate value
     */
    void onHeartRateValueChanged(int value);
}
