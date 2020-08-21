package com.google.heartrate.wearos.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.wearable.activity.WearableActivity;
import android.widget.TextView;

import com.google.heartrate.wearos.app.bluetooth.server.BluetoothServer;
import com.google.heartrate.wearos.app.bluetooth.server.handlers.GattRequestHandlerRegistry;
import com.google.heartrate.wearos.app.bluetooth.server.handlers.HeartRateServiceRequestHandler;
import com.google.heartrate.wearos.app.gatt.heartrate.service.HeartRateGattService;
import com.google.heartrate.wearos.app.sensors.HeartRateSensorListener;
import com.google.heartrate.wearos.app.sensors.HeartRateValueSubscriber;
import com.google.heartrate.wearos.app.sensors.SensorException;


/**
 * Application main activity sets up heart rate server and show current heart rate.
 *
 * <p>Used only for testing {@link BluetoothServer} with {@link HeartRateGattService} hosted.
 */
public class MainActivity extends WearableActivity implements HeartRateValueSubscriber {
    private static final String TAG = MainActivity.class.getSimpleName();

    /** {@link TextView} to show current heart rate. */
    private TextView mTextView;

    /** Sensor listener to get heart rate. */
    private HeartRateSensorListener heartRateSensorListener;

    /** {@link HeartRateServiceRequestHandler} to register to {@link GattRequestHandlerRegistry}. */
    private HeartRateServiceRequestHandler heartRateServiceRequestHandler;

    /** {@link ServiceConnection} with {@link HeartRateService}. */
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            HeartRateService.BluetoothServerBinder serverBinder = (HeartRateService.BluetoothServerBinder) service;
            serverBinder.getService().registerGattServiceHandler(heartRateServiceRequestHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName className) { }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = findViewById(R.id.text);

        heartRateSensorListener = new HeartRateSensorListener(this);
        try {
            heartRateSensorListener.startMeasure();
            heartRateServiceRequestHandler = new HeartRateServiceRequestHandler(heartRateSensorListener);

            startForegroundService(new Intent(this, HeartRateService.class));
        } catch (SensorException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        heartRateSensorListener.registerSubscriber(this);
        bindService(new Intent(this, HeartRateService.class), connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();

        heartRateSensorListener.unregisterSubscriber(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unbindService(connection);
        stopService(new Intent(this, HeartRateService.class));
        heartRateSensorListener.stopMeasure();
    }

    @Override
    public void onHeartRateValueChanged(int value) {
        mTextView.setText(String.format("HR: %d", value));
    }
}
