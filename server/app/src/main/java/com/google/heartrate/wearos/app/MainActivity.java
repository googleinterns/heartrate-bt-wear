package com.google.heartrate.wearos.app;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import com.google.heartrate.wearos.app.bluetooth.server.BluetoothServer;
import com.google.heartrate.wearos.app.bluetooth.server.handlers.GattRequestHandlerRegistry;
import com.google.heartrate.wearos.app.bluetooth.server.handlers.HeartRateServiceRequestHandler;
import com.google.heartrate.wearos.app.gatt.heartrate.service.HeartRateGattService;
import com.google.heartrate.wearos.app.sensors.HeartRateSensorListener;
import com.google.heartrate.wearos.app.sensors.SensorException;

import java.util.ArrayList;
import java.util.List;

import static com.google.heartrate.wearos.app.sensors.HeartRateSensorListener.HEART_RATE_CHANGE;
import static com.google.heartrate.wearos.app.sensors.HeartRateSensorListener.HEART_RATE_VALUE;

/**
 * Application main activity sets up {@link HeartRateGattService} in {@link BluetoothServer}
 * and show current heart rate.
 */
public class MainActivity extends WearableActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    /** {@link TextView} to show current heart rate. */
    private TextView heartRateTextView;

    /** Sensor listener to get heart rate. */
    private HeartRateSensorListener heartRateSensorListener;

    /** {@link HeartRateServiceRequestHandler} to register to {@link GattRequestHandlerRegistry}. */
    private HeartRateServiceRequestHandler heartRateServiceRequestHandler;

    /** {@link ServiceConnection} with {@link BluetoothService}. */
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            BluetoothService.BluetoothServerBinder serverBinder = (BluetoothService.BluetoothServerBinder) service;
            serverBinder.getService().registerGattServiceHandler(heartRateServiceRequestHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName className) { }
    };


    private void requestPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();
        if (checkSelfPermission(Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Body sensors permission is not granted");
            permissionsNeeded.add(Manifest.permission.BODY_SENSORS);
        }
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Location permission is not granted");
            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (permissionsNeeded.size() > 0) {
            ActivityCompat.requestPermissions(this,
                    permissionsNeeded.toArray(new String[]{}),
                    /* application request code */1);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions();

        heartRateTextView = findViewById(R.id.text);

        heartRateSensorListener = new HeartRateSensorListener(this);
        try {
            heartRateSensorListener.startMeasure();
            heartRateServiceRequestHandler = new HeartRateServiceRequestHandler(heartRateSensorListener);

            startForegroundService(new Intent(this, BluetoothService.class));
        } catch (SensorException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        bindService(new Intent(this, BluetoothService.class), connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();

        unbindService(connection);
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter(HEART_RATE_CHANGE);
        registerReceiver(heartRateBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        unregisterReceiver(heartRateBroadcastReceiver);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, BluetoothService.class));
        heartRateSensorListener.stopMeasure();
    }

    /** Receiver to get current heart rate. */
    private BroadcastReceiver heartRateBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            int heartRate = intent.getIntExtra(HEART_RATE_VALUE, 0);
            heartRateTextView.setText(String.format("HR: %d",  heartRate));
        }
    };
}
