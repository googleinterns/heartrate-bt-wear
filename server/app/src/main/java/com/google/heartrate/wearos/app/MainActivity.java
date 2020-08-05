package com.google.heartrate.wearos.app;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.widget.TextView;

import com.google.heartrate.wearos.app.bluetooth.server.HeartRateServer;
import com.google.heartrate.wearos.app.gatt.GattException;


/**
 * Application main activity sets up server and starts it (not supported yet).
 */
public class MainActivity extends WearableActivity implements GattBluetoothActionsListener {

    private TextView mTextView;
    private HeartRateServer heartRateServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();
        try {
            heartRateServer = new HeartRateServer(this);
            heartRateServer.start(this);
        } catch (GattException e) {
            e.printStackTrace();
        }
        mTextView = findViewById(R.id.text);
    }

    @Override
    protected void onResume() {
        heartRateServer.registerReceiver();
        super.onResume();
    }

    @Override
    protected void onPause() {
        heartRateServer.unregisterReceiver();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        heartRateServer.stop();
        super.onDestroy();
    }

    @Override
    public void onAction(String action) {
        mTextView.setText(action);
    }
}
