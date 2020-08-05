package com.google.heartrate.wearos.app.bluetooth.server;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorEvent;
import android.util.Log;

import com.google.heartrate.wearos.app.GattBluetoothActionsListener;
import com.google.heartrate.wearos.app.bluetooth.BluetoothUtils;
import com.google.heartrate.wearos.app.gatt.GattException;
import com.google.heartrate.wearos.app.gatt.attributes.GattCharacteristic;
import com.google.heartrate.wearos.app.gatt.attributes.GattDescriptor;
import com.google.heartrate.wearos.app.gatt.heartrate.characteristics.HeartRateMeasurementCharacteristic;
import com.google.heartrate.wearos.app.gatt.heartrate.service.HeartRateGattService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * {@link HeartRateServer} is bluetooth gatt peripheral with a GATT Heart Rate service hosted.
 * <p>
 * {@link HeartRateServer} implements delivering heart rate data to multiple central devices
 * using bluetooth GATT protocol.
 */
public class HeartRateServer {
    private static final String TAG = HeartRateServer.class.getSimpleName();

    /** Listener for gatt bluetooth actions proceeded in peripheral. */
    private GattBluetoothActionsListener mGattBluetoothActionsListener;

    /** Application context. */
    private final Context mContext;

    /** Service manager for Heart Rate service. */
    private final HeartRateGattService mHeartRateGattService;

    /** Hear Rate sensor listener. */
    private final HeartRateSensorListener mHeartRateSensorListener;

    /** Set of registered for notifications devices (centrals). */
    private final Set<BluetoothDevice> mRegisteredDevices = new HashSet<>();

    /** {@link BluetoothGattServer} for bluetooth interaction. */
    private final BluetoothGattServer mBluetoothGattServer;

    /** {@link BluetoothAdvertiser} for bluetooth advertising. */
    private final BluetoothAdvertiser mBluetoothAdvertiser;

    /** Receiver to control {@link BluetoothAdapter} state. */
    private final BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
            switch (state) {
                case BluetoothAdapter.STATE_ON:
                    start(mGattBluetoothActionsListener);
                    break;
                case BluetoothAdapter.STATE_OFF:
                    stop();
                    break;
                default:
            }
        }
    };

    /** Callback to control bluetooth incoming requests. */
    private final BluetoothGattServerCallback mBluetoothGattServerCallback = new BluetoothGattServerCallback() {

        @Override
        public void onConnectionStateChange(BluetoothDevice device, final int status, int newState) {
            Log.v(TAG, String.format("onConnectionStateChange() - device=%s status=%s state=%s",
                    device.getAddress(), status, newState));

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Status success");

                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    Log.d(TAG, "State connected");
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    Log.d(TAG, "State disconnected");
                    unregisterDevice(device);
                }
            }
        }

        @Override
        public void onServiceAdded(int status, BluetoothGattService bluetoothGattService) {
            Log.v(TAG, String.format("onServiceAdded() - status=%d", status));
        }

        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            BluetoothGatt
            Log.v(TAG, String.format("onNotificationSent() - status=%d", status));
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset,
                                                BluetoothGattCharacteristic characteristic) {
            Log.v(TAG, String.format("onCharacteristicReadRequest() - device=%s characteristic=%s",
                    characteristic.getUuid(),
                    Arrays.toString(characteristic.getValue())));
            try {
                GattCharacteristic gattCharacteristic = mHeartRateGattService.getCharacteristic(characteristic.getUuid());
                gattCharacteristic.assertCharacteristicReadable();
                byte[] value = gattCharacteristic.read(device, offset);
                mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
            } catch (GattException e) {
                mBluetoothGattServer.sendResponse(device, requestId, e.getStatus(), 0, null);
            }
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
                                                 BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded,
                                                 int offset, byte[] value) {
            Log.v(TAG, String.format("onCharacteristicWriteRequest() - device=%s characteristic=%s value=%s",
                    characteristic.getUuid(),
                    Arrays.toString(characteristic.getValue()),
                    Arrays.toString(value)));
            try {
                GattCharacteristic gattCharacteristic = mHeartRateGattService.getCharacteristic(characteristic.getUuid());
                gattCharacteristic.assertCharacteristicWritable();
                gattCharacteristic.write(device, offset, value);
                if (gattCharacteristic.isWriteResponseRequired()) {
                    mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
                }
            } catch (GattException e) {
                mBluetoothGattServer.sendResponse(device, requestId, e.getStatus(), 0, null);
            }
        }

        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset,
                                            BluetoothGattDescriptor descriptor) {
            Log.d(TAG, String.format("onDescriptorReadRequest() device=%s descriptor=%s",
                    device.getAddress(), descriptor.getUuid()));
            try {
                GattCharacteristic gattCharacteristic = mHeartRateGattService
                        .getCharacteristic(descriptor.getCharacteristic().getUuid());
                GattDescriptor gattDescriptor = gattCharacteristic
                        .getDescriptor(descriptor.getUuid());

                gattDescriptor.assertDescriptorReadable();

                //TODO: move to descriptor
                //and replace with gattDescriptor.read(device, offset);

                byte[] value;
                if (mRegisteredDevices.contains(device)) {
                    value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
                } else {
                    value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
                }
                mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, value);
            } catch (GattException e) {
                Log.w(TAG, String.format("onDescriptorReadRequest() failed with exception %s", e.getMessage()));
                mBluetoothGattServer.sendResponse(device, requestId, e.getStatus(), 0, null);
            }
        }

        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            Log.d(TAG, String.format("onDescriptorWriteRequest() - device=%s descriptor=%s value=%s",
                    device.getAddress(), descriptor.getUuid(), Arrays.toString(value)));

            try {
                GattCharacteristic gattCharacteristic = mHeartRateGattService
                        .getCharacteristic(descriptor.getCharacteristic().getUuid());
                GattDescriptor gattDescriptor = gattCharacteristic
                        .getDescriptor(descriptor.getUuid());

                gattDescriptor.assertDescriptorWritable();

                //TODO: move to descriptor
                //and replace with gattDescriptor.write(device, offset, value);

                if (Arrays.equals(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE, value)) {
                    Log.d(TAG, String.format("Subscribe device %s to notifications", device));
                    registerDevice(device);
                } else if (Arrays.equals(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE, value)) {
                    Log.d(TAG, String.format("Unsubscribe device %s from notifications", device));
                    unregisterDevice(device);
                }

                if (responseNeeded) {
                    mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null);
                }
            } catch (GattException e) {
                if (responseNeeded) {
                    mBluetoothGattServer.sendResponse(device, requestId, e.getStatus(), 0, null);
                }
            }
        }
    };

    public HeartRateServer(Context context) throws GattException {
        mContext = context;

        mHeartRateGattService = new HeartRateGattService();
        mBluetoothAdvertiser = new BluetoothAdvertiser(mContext);
        mBluetoothGattServer = BluetoothUtils.getBluetoothGattServer(mContext, mBluetoothGattServerCallback);

        mHeartRateSensorListener = new HeartRateSensorListener(mContext) {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float mHeartRateFloat = event.values[0];
                int heartRate = Math.round(mHeartRateFloat);
                mGattBluetoothActionsListener.onAction(String.format("HR = %d", heartRate));
                Log.v(TAG, String.format("onSensorChanged() - bmp=%d", heartRate));
                try {
                    HeartRateMeasurementCharacteristic heartRateMeasurementCharacteristicManager
                            = mHeartRateGattService.getHeartRateMeasurementCharacteristic();

                    heartRateMeasurementCharacteristicManager.setHeartRateCharacteristicValues(heartRate);

                    notifyRegisteredDevices(heartRateMeasurementCharacteristicManager.getBluetoothGattCharacteristic());
                } catch (GattException e) {
                    Log.w(TAG, e.getMessage());
                }
            }
        };
    }

    public void start(GattBluetoothActionsListener gattBluetoothActionsListener) {
        Log.v(TAG, "Starting heart rate server");

        mGattBluetoothActionsListener = gattBluetoothActionsListener;
        mBluetoothGattServer.addService(mHeartRateGattService.getBluetoothGattService());
        mBluetoothAdvertiser.start(new UUID[] { mHeartRateGattService.getUUid() });
        mHeartRateSensorListener.startMeasure();
    }

    public void stop() {
        Log.v(TAG, "Stopping heart rate server");

        mHeartRateSensorListener.stopMeasure();
        mBluetoothAdvertiser.stop();
        mBluetoothGattServer.close();
        mRegisteredDevices.clear();
    }

    public void registerReceiver() {
        Log.d(TAG, "Register receiver");
        mContext.registerReceiver(mBluetoothReceiver,
                new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    public void unregisterReceiver() {
        Log.d(TAG, "Unregister receiver");
        mContext.unregisterReceiver(mBluetoothReceiver);
    }

    private void registerDevice(BluetoothDevice device) {
        Log.d(TAG, String.format("Register device %s", device.getAddress()));
        mRegisteredDevices.add(device);
    }

    private void unregisterDevice(BluetoothDevice device) {
        Log.d(TAG, String.format("Unregister device %s", device.getAddress()));
        mRegisteredDevices.remove(device);
    }

    public void notifyRegisteredDevices(BluetoothGattCharacteristic characteristic) {
        Log.d(TAG, "Notify registered devices");

        if (mRegisteredDevices.isEmpty()) {
            Log.i(TAG, "No subscribers registered");
            return;
        }

        Log.v(TAG, "Sending update to " + mRegisteredDevices.size() + " subscribers");
        for (BluetoothDevice device : mRegisteredDevices) {
            mBluetoothGattServer.notifyCharacteristicChanged(device, characteristic, false);
        }
    }
}
