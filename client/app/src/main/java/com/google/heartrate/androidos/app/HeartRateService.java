package com.google.heartrate.androidos.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.google.heartrate.androidos.app.bluetooth.client.BluetoothClient;
import com.google.heartrate.androidos.app.gatt.GattException;

public class HeartRateService extends Service implements BluetoothActionsListener {
    private static final String TAG = HeartRateService.class.getSimpleName();

    static final public String BLUETOOTH_ACTION = "com.google.heartrate.androidos.app.ACTION";
    static final public String BLUETOOTH_MESSAGE = "com.google.heartrate.androidos.app.MESSAGE";

    private static final String CHANNEL_ID = String.format("%sChannelId", TAG);
    private static final String CHANNEL_NAME = String.format("%sChannelName", TAG);

    /** {@link HeartRateService} for heart rate client hosting. */
    private BluetoothClient bluetoothClient;

    /**
     * Create notification chanel for foreground {@link HeartRateService} service.
     */
    private void createNotificationChannel() {

        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(serviceChannel);
    }

    @Override
    public void onCreate() {
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent
                .getActivity(this, 0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        try {
            bluetoothClient = new BluetoothClient(this);
        } catch (GattException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        bluetoothClient.start(this);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        bluetoothClient.stop();
        stopForeground(true);
    }

    @Override
    public void onAction(String action) {
        Intent intent = new Intent(BLUETOOTH_ACTION);
        if (action != null)
            intent.putExtra(BLUETOOTH_MESSAGE, action);
        sendBroadcast(intent);
    }
}
