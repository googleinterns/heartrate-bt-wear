package com.google.heartrate.wearos.app;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.widget.TextView;

import com.google.heartrate.wearos.app.bluetooth.server.BluetoothServer;
import com.google.heartrate.wearos.app.bluetooth.server.handlers.HeartRateServiceRequestHandler;
import com.google.heartrate.wearos.app.gatt.GattException;
import com.google.heartrate.wearos.app.gatt.heartrate.service.HeartRateGattService;
import com.google.heartrate.wearos.app.sensors.HeartRateSensorListener;
import com.google.heartrate.wearos.app.sensors.HeartRateValueSubscriber;


/**
 * Application main activity sets up heart rate server and show current heart rate.
 *
 * <p>Used only for testing {@link BluetoothServer} with {@link HeartRateGattService} hosted.
 */
public class MainActivity extends WearableActivity implements HeartRateValueSubscriber {

    /** {@link TextView} to show current heart rate. */
    private TextView mTextView;

    /** {@link BluetoothServer} for heart rate service hosting. */
    private BluetoothServer bluetoothServer;

    /** Sensor listener to get heart rate. */
    private HeartRateSensorListener heartRateSensorListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Not to fall into doze and suspend mode */
        setAmbientEnabled();

        mTextView = findViewById(R.id.text);

        startHeartRateServer();
    }

    /**
     * Start server with heart rate service hosted.
     */
    private void startHeartRateServer() {
        try {
            heartRateSensorListener = new HeartRateSensorListener(this);
            heartRateSensorListener.registerSubscriber(this);

            bluetoothServer = new BluetoothServer(this);

            HeartRateServiceRequestHandler heartRateServiceRequestHandler
                    = new HeartRateServiceRequestHandler(heartRateSensorListener);
            bluetoothServer.registerGattServiceHandler(heartRateServiceRequestHandler);

            bluetoothServer.start();
        } catch (GattException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        bluetoothServer.registerReceiver();
        super.onResume();
    }

    @Override
    protected void onPause() {
        bluetoothServer.unregisterReceiver();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        bluetoothServer.stop();
        heartRateSensorListener.unregisterSubscriber(this);
        super.onDestroy();
    }

    @Override
    public void onHeartRateValueChanged(int value) {
        mTextView.setText(String.format("HR: %d", value));
    }
}
