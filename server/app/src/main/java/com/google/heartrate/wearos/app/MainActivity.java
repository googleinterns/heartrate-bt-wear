package com.google.heartrate.wearos.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.heartrate.wearos.app.bluetooth.server.BluetoothServer;
import com.google.heartrate.wearos.app.bluetooth.server.handlers.HeartRateServiceRequestHandler;
import com.google.heartrate.wearos.app.gatt.heartrate.service.HeartRateGattService;
import com.google.heartrate.wearos.app.sensors.HeartRateSensorListener;
import com.google.heartrate.wearos.app.sensors.HeartRateValueSubscriber;


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

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.d(TAG, "onServiceConnected()");

            BluetoothServerService.BluetoothServerBinder serverBinder = (BluetoothServerService.BluetoothServerBinder) service;
            BluetoothServer bluetoothServer = serverBinder.getService();

            HeartRateServiceRequestHandler heartRateServiceRequestHandler
                    = new HeartRateServiceRequestHandler(heartRateSensorListener);
            bluetoothServer.registerGattServiceHandler(heartRateServiceRequestHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            Log.d(TAG, "onServiceDisconnected()");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Not to fall into doze and suspend mode */
        setAmbientEnabled();

        mTextView = findViewById(R.id.text);

        heartRateSensorListener = new HeartRateSensorListener(this);
        startForegroundService(new Intent(this, BluetoothServerService.class));
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart()");

        super.onStart();
        heartRateSensorListener.registerSubscriber(this);
        bindService(new Intent(this, BluetoothServerService.class), connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop()");

        super.onStop();
        heartRateSensorListener.unregisterSubscriber(this);
        unbindService(connection);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy()");

        super.onDestroy();
        stopService(new Intent(this, BluetoothServerService.class));
    }

    @Override
    public void onHeartRateValueChanged(int value) {
        mTextView.setText(String.format("HR: %d", value));
    }
}
