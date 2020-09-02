package com.google.heartrate.androidos.app;

import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.heartrate.androidos.app.bluetooth.client.BluetoothHeartRateServiceClient;
import com.google.heartrate.androidos.app.gatt.GattException;

public class MainActivity extends AppCompatActivity implements BluetoothActionsListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private PowerManager.WakeLock wakeLock;
    private BluetoothHeartRateServiceClient heartRateServiceClient;
    private TextView bluetoothActionTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothActionTextView = findViewById(R.id.bluetooth_action_textview);

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        wakeLock.acquire();

        try {
            heartRateServiceClient = new BluetoothHeartRateServiceClient(this);
        } catch (GattException e) {
            e.printStackTrace();
        }
        heartRateServiceClient.start(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        heartRateServiceClient.stop();
        wakeLock.release();
    }

    @Override
    public void onAction(String action) {
        runOnUiThread(() -> bluetoothActionTextView.setText(action));
    }
}