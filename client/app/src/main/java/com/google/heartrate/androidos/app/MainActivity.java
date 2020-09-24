package com.google.heartrate.androidos.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PowerManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private PowerManager.WakeLock wakeLock;
    private TextView bluetoothActionTextView;

    private BroadcastReceiver updateHeartRateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            bluetoothActionTextView.setText(intent.getStringExtra(HeartRateService.BLUETOOTH_MESSAGE));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothActionTextView = findViewById(R.id.bluetooth_action_textview);

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        wakeLock.acquire();

        startForegroundService(new Intent(this, HeartRateService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();

        IntentFilter filter = new IntentFilter();
        filter.addAction(HeartRateService.BLUETOOTH_ACTION);
        registerReceiver(updateHeartRateReceiver, filter);
    }

    @Override
    protected void onStop() {
        unregisterReceiver(updateHeartRateReceiver);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, HeartRateService.class));
        wakeLock.release();

        super.onDestroy();
    }
}