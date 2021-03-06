package com.google.heartrate.wearos.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.heartrate.wearos.app.bluetooth.server.BluetoothServer;
import com.google.heartrate.wearos.app.bluetooth.server.handlers.GattRequestHandlerRegistry;
import com.google.heartrate.wearos.app.bluetooth.server.handlers.GattServiceRequestHandler;
import com.google.heartrate.wearos.app.gatt.GattException;

/**
 * {@link BluetoothService} is foreground service to run {@link BluetoothServer} in.
 * It starts {@link BluetoothService} and provides binder
 * to add/remove {@link GattServiceRequestHandler} into {@link BluetoothServer}.
 */
public class BluetoothService extends Service {
    private static final String TAG = BluetoothService.class.getSimpleName();
    private static final String CHANNEL_ID = String.format("%sChannelId", TAG);
    private static final String CHANNEL_NAME = String.format("%sChannelName", TAG);

    /** {@link BluetoothServer} for heart rate service hosting. */
    private BluetoothServer bluetoothServer;

    /** WakeLock to prevent sensor go to suspend mode. */
    private PowerManager.WakeLock partialWakeLock;

    /** Binder to {@link BluetoothServer}. */
    private final IBinder mBinder = new BluetoothServerBinder();

    /**
     * Binder class to provide components the access
     * to {@link GattRequestHandlerRegistry} interface of {@link BluetoothServer}
     * in order to registry {@link GattServiceRequestHandler} in {@link BluetoothServer}.
     */
     public class BluetoothServerBinder extends Binder {
        public GattRequestHandlerRegistry getService() {
            Log.d(TAG, "getService()");
            return bluetoothServer;
        }
    }

    /**
     * Create notification chanel for foreground {@link BluetoothService} service.
     */
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

    @Override
    public void onCreate() {
        try {
            createNotificationChannel();
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent
                    .getActivity(this,
                            /* request code */0,
                            notificationIntent,
                            /* no flags */0);
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentIntent(pendingIntent)
                    .build();

            startForeground(
                    /* notification service id*/1,
                    notification);
            bluetoothServer = new BluetoothServer(this);

            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            partialWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
            partialWakeLock.acquire();
        } catch (GattException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        bluetoothServer.start();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        partialWakeLock.release();
        bluetoothServer.stop();
    }
}
