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
import com.google.heartrate.wearos.app.bluetooth.server.handlers.GattRequestHandlerRegistry;
import com.google.heartrate.wearos.app.gatt.GattException;

import java.util.HashMap;
import java.util.UUID;

/**
 * {@link BluetoothServer} is wrapper over {@link BluetoothGattServer}
 * allowing set up {@link BluetoothGattServer}, add services to be hosted in server,
 * listen incoming requests and send responses.
 *
 * <p>{@link BluetoothServer} provides base request handling. To make {@link BluetoothServer}
 * compatible with all service type, all specific request handling process must be implemented in
 * {@link GattServiceRequestHandler}.
 */
public class BluetoothServer implements GattRequestHandlerRegistry {
    private static final String TAG = BluetoothServer.class.getSimpleName();

    /** Application context. */
    private final Context context;

    /** Service request handlers services hosted in bluetooth gatt server. */
    public final HashMap<UUID, GattServiceRequestHandler> gattRequestHandlerByServiceUuid = new HashMap<>();

    /** {@link BluetoothGattServer} for bluetooth interaction. */
    private final BluetoothGattServer bluetoothGattServer;

    /** {@link BluetoothAdvertiser} for bluetooth advertising. */
    private final BluetoothAdvertiser bluetoothAdvertiser;

    /** Receiver to control {@link BluetoothAdapter} state. */
    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
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
        this.context = context;
        bluetoothAdvertiser = new BluetoothAdvertiser(this.context);

        BluetoothServerCallback mBluetoothServerCallback = new BluetoothServerCallback(this);
        bluetoothGattServer = BluetoothUtils.getBluetoothGattServer(this.context, mBluetoothServerCallback);
    }

    /**
     * Get gatt service request handler for given service.
     *
     * @param service to get a request handler for
     * @return gatt service request handler for given service
     * @throws GattException if service not hosted in server
     */
    GattServiceRequestHandler getGattServiceRequestHandler(BluetoothGattService service) throws GattException {
        GattServiceRequestHandler gattService = gattRequestHandlerByServiceUuid.get(service.getUuid());
        if (gattService == null) {
            throw new GattException(String.format("Service %s not supported", service.getUuid()),BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED);
        }
        return gattService;
    }

    /**
     * Get gatt service request handler for given characteristic.
     *
     * @param characteristic to get a request handler for
     * @return gatt service request handler for given characteristic
     * @throws GattException if service for this characteristic not hosted in server
     */
    GattServiceRequestHandler getGattServiceRequestHandler(BluetoothGattCharacteristic characteristic) throws GattException {
        return getGattServiceRequestHandler(characteristic.getService());
    }

    /**
     * Get gatt service request handler for given descriptor.
     *
     * @param descriptor to get a request handler for
     * @return gatt service request handler for given characteristic
     * @throws GattException if service for this descriptor not hosted in server
     */
    GattServiceRequestHandler getGattServiceRequestHandler(BluetoothGattDescriptor descriptor) throws GattException {
        return getGattServiceRequestHandler(descriptor.getCharacteristic());
    }

    /**
     * {@link GattRequestHandlerRegistry#registerGattServiceHandler}
     * Register given gatt service request handler in server.
     * Add handler's service to the {@link BluetoothGattServer} to be hosted.
     *
     * @param requestHandler gatt service request handler to register
     */
    public void registerGattServiceHandler(GattServiceRequestHandler requestHandler) {
        BluetoothGattService gattService = requestHandler.getGattService().getBluetoothGattService();
        bluetoothGattServer.addService(gattService);
        gattRequestHandlerByServiceUuid.put(gattService.getUuid(), requestHandler);

        bluetoothAdvertiser.restartAdvertisingServices(gattRequestHandlerByServiceUuid.keySet());
    }

    /**
     * {@link GattRequestHandlerRegistry#unregisterGattServiceHandler}
     * Remove handler's service from the {@link BluetoothGattServer}.
     *
     * @param requestHandler gatt service request handler to unregister
     */
    public void unregisterGattServiceHandler(GattServiceRequestHandler requestHandler) {
        BluetoothGattService gattService = requestHandler.getGattService().getBluetoothGattService();
        bluetoothGattServer.removeService(gattService);
        gattRequestHandlerByServiceUuid.remove(gattService.getUuid());
    }

    /**
     * Start advertising process to advertise server existence.
     */
    public void start() {
        Log.v(TAG, "Starting heart rate server");

        registerReceiver();
        bluetoothAdvertiser.startAdvertisingServices(gattRequestHandlerByServiceUuid.keySet());
    }

    /**
     * Stop server interaction with all connected clients.
     */
    public void stop() {
        Log.v(TAG, "Stopping heart rate server");

        unregisterReceiver();
        for (GattServiceRequestHandler requestHandler : gattRequestHandlerByServiceUuid.values()) {
            requestHandler.onServiceRemoved();
        }
        bluetoothGattServer.close();
        bluetoothAdvertiser.stopAdvertisingServices();
    }

    /**
     * Register receiver which control {@link BluetoothAdvertiser} state.
     */
    public void registerReceiver() {
        Log.d(TAG, "Register receiver");
        context.registerReceiver(bluetoothReceiver,
                new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    /**
     * Unregister receiver which control {@link BluetoothAdvertiser} state.
     */
    public void unregisterReceiver() {
        Log.d(TAG, "Unregister receiver");
        context.unregisterReceiver(bluetoothReceiver);
    }

    /**
     * Send a response to a read or write request to a remote device.
     *
     * <p>This function must be invoked in when a remote read/write request
     * is received by {@link BluetoothServerCallback} read/write methods.
     *
     * @param device remote device to send this response to
     * @param requestId id of the request that was received with the callback
     * @param status status of the request to be sent to the remote devices
     * @param offset offset for partial read/write response
     * @param value value of the attribute that was read/written
     */
    void sendResponse(BluetoothDevice device, int requestId, int status, int offset, byte[] value) {
        if (!bluetoothGattServer.sendResponse(device, requestId, status, offset, value)) {
            Log.e(TAG, String.format("Send response to device %s has failed!",
                    device.getAddress()));
        }
    }

    /**
     * Send a response about failed read or write request to a remote device.
     *
     * <p>This function must be invoked in when a remote read/write request
     * received by {@link BluetoothServerCallback} failed.
     *
     * @param device remote device to send this response to
     * @param requestId id of the request that was received with the callback
     * @param status status of the request to be sent to the remote devices
     */
    void sendErrorResponse(BluetoothDevice device, int requestId, int status) {
        if (!bluetoothGattServer.sendResponse(device, requestId, status, 0, null)) {
            Log.e(TAG, String.format("Send error response to device %s has failed!",
                    device.getAddress()));
        } 
    }

    /**
     * Send a notification to remote device about characteristic changed.
     *
     * @param device device to notify
     * @param characteristic changed characteristic
     */
    void notifyCharacteristicChanged(BluetoothDevice device, BluetoothGattCharacteristic characteristic) {
        if (!bluetoothGattServer.notifyCharacteristicChanged(device, characteristic, false)) {
            Log.e(TAG, String.format("Characteristic changed notification for device %s has failed!",
                    device.getAddress()));
        }
    }
}
