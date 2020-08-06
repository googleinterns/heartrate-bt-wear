package com.google.heartrate.wearos.app.sensors;

public interface HeartRateValueSubscriber {
    void onHeartRateValueChanged(int value);
}
