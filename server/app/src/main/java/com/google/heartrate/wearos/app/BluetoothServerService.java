package com.google.heartrate.wearos.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.heartrate.wearos.app.bluetooth.server.BluetoothServer;
import com.google.heartrate.wearos.app.gatt.GattException;

public class BluetoothServerService extends Service {
    private static final String TAG = BluetoothServerService.class.getSimpleName();
    private static final String CHANNEL_ID = String.format("%sChannelId", TAG);
    private static final String CHANNEL_NAME = String.format("%sChannelName", TAG);

    private final IBinder mBinder = new BluetoothServerBinder();

    /** {@link BluetoothServer} for heart rate service hosting. */
    private BluetoothServer bluetoothServer;

    @Override
    public void onCreate() {
        try {
            createNotificationChannel();
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent
                    .getActivity(this, 0, notificationIntent, 0);
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentIntent(pendingIntent)
                    .build();

            startForeground(1, notification);
            bluetoothServer = new BluetoothServer(this);
        } catch (GattException e) {
            e.printStackTrace();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    public class BluetoothServerBinder extends Binder {
        public BluetoothServer getService() {
            Log.d(TAG, "getService()");
            return bluetoothServer;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        bluetoothServer.start();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind()");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind()");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        bluetoothServer.stop();
    }
}
