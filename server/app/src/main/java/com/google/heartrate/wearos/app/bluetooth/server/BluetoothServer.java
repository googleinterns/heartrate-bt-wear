package com.google.heartrate.wearos.app.bluetooth.server;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.google.heartrate.wearos.app.bluetooth.BluetoothUtils;
import com.google.heartrate.wearos.app.bluetooth.server.handlers.GattServiceRequestHandler;
import com.google.heartrate.wearos.app.gatt.GattException;

import java.util.HashMap;
import java.util.UUID;

/**
 * {@link BluetoothServer} is bluetooth gatt peripheral with a GATT Heart Rate service hosted.
 * <p>
 * {@link BluetoothServer} implements delivering heart rate data to multiple central devices
 * using bluetooth GATT protocol.
 */
public class BluetoothServer {
    private static final String TAG = BluetoothServer.class.getSimpleName();

    /** Application context. */
    private final Context mContext;

    /** Service manager for Heart Rate service. */
    public final HashMap<UUID, GattServiceRequestHandler> gattRequestHandlerByServiceUuid = new HashMap<>();

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
                    start();
                    break;
                case BluetoothAdapter.STATE_OFF:
                    stop();
                    break;
                default:
            }
        }
    };

    public BluetoothServer(Context context) throws GattException {
        mContext = context;
        mBluetoothAdvertiser = new BluetoothAdvertiser(mContext);

        BluetoothServerCallback mBluetoothServerCallback = new BluetoothServerCallback(this);
        mBluetoothGattServer = BluetoothUtils.getBluetoothGattServer(mContext, mBluetoothServerCallback);
    }

    GattServiceRequestHandler getGattServiceRequestHandler(BluetoothGattService service) throws GattException {
        GattServiceRequestHandler gattService = gattRequestHandlerByServiceUuid.get(service.getUuid());
        if (gattService == null) {
            throw new GattException(String.format("Service %s not supported", service.getUuid()),BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED);
        }
        return gattService;
    }

    GattServiceRequestHandler getGattServiceRequestHandler(BluetoothGattCharacteristic characteristic) throws GattException {
        return getGattServiceRequestHandler(characteristic.getService());
    }

    GattServiceRequestHandler getGattServiceRequestHandler(BluetoothGattDescriptor descriptor) throws GattException {
        return getGattServiceRequestHandler(descriptor.getCharacteristic());
    }

    public void registerGattServiceHandler(GattServiceRequestHandler requestHandler) {
        BluetoothGattService gattService = requestHandler.getBluetoothGattService();
        mBluetoothGattServer.addService(gattService);
        gattRequestHandlerByServiceUuid.put(gattService.getUuid(), requestHandler);
    }

    public void unregisterGattServiceHandler(GattServiceRequestHandler requestHandler) {
        BluetoothGattService gattService = requestHandler.getBluetoothGattService();
        mBluetoothGattServer.removeService(gattService);
        gattRequestHandlerByServiceUuid.remove(gattService.getUuid());
    }

    public void start() {
        Log.v(TAG, "Starting heart rate server");

        mBluetoothAdvertiser.start(gattRequestHandlerByServiceUuid.keySet());
    }

    public void stop() {
        Log.v(TAG, "Stopping heart rate server");

        for (GattServiceRequestHandler requestHandler : gattRequestHandlerByServiceUuid.values()) {
            requestHandler.onServiceRemoved();
        }
        mBluetoothGattServer.close();
        mBluetoothAdvertiser.stop();
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

    public void sendResponse(BluetoothDevice device, int requestId, int status, int offset, byte[] value) {
        mBluetoothGattServer.sendResponse(device, requestId, status, offset, value);
    }

    public void sendErrorResponse(BluetoothDevice device, int requestId, int status) {
        mBluetoothGattServer.sendResponse(device, requestId, status, 0, null);
    }

    public void notifyCharacteristicChanged(BluetoothDevice device, BluetoothGattCharacteristic characteristic) {
        mBluetoothGattServer.notifyCharacteristicChanged(device, characteristic, false);
    }
}
