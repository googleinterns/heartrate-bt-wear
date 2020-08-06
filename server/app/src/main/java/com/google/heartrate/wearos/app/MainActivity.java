package com.google.heartrate.wearos.app;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.widget.TextView;

import com.google.heartrate.wearos.app.bluetooth.server.BluetoothServer;
import com.google.heartrate.wearos.app.bluetooth.server.handlers.HeartRateServiceRequestHandler;
import com.google.heartrate.wearos.app.gatt.GattException;
import com.google.heartrate.wearos.app.sensors.HeartRateSensorListener;
import com.google.heartrate.wearos.app.sensors.HeartRateValueSubscriber;


/**
 * Application main activity sets up server and starts it (not supported yet).
 */
public class MainActivity extends WearableActivity implements HeartRateValueSubscriber {

    private TextView mTextView;
    private BluetoothServer mBluetoothServer;
    private HeartRateServiceRequestHandler mHeartRateServiceRequestHandler;
    private HeartRateSensorListener mHeartRateSensorListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();
        try {
            mHeartRateSensorListener = new HeartRateSensorListener(this);
            mHeartRateSensorListener.registerSubscriber(this);

            mBluetoothServer = new BluetoothServer(this);

            mHeartRateServiceRequestHandler = new HeartRateServiceRequestHandler(mHeartRateSensorListener);
            mBluetoothServer.registerGattServiceHandler(mHeartRateServiceRequestHandler);

            mBluetoothServer.start();
        } catch (GattException e) {
            e.printStackTrace();
        }
        mTextView = findViewById(R.id.text);
    }

    @Override
    protected void onResume() {
        mBluetoothServer.registerReceiver();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mBluetoothServer.unregisterReceiver();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mBluetoothServer.stop();
        super.onDestroy();
    }

    @Override
    public void onHeartRateValueChanged(int value) {
        mTextView.setText(String.format("HR: %d", value));
    }
}
