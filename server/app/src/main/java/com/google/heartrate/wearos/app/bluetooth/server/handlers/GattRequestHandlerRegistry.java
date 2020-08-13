package com.google.heartrate.wearos.app.bluetooth.server.handlers;

public interface GattRequestHandlerRegistry {
    /**
     * Register given gatt service request handler.
     *
     * @param requestHandler gatt service request handler to register
     */
    void registerGattServiceHandler(GattServiceRequestHandler requestHandler);

    /**
     * Unregister given gatt service request handler.
     *
     * @param requestHandler gatt service request handler to unregister
     */
    void unregisterGattServiceHandler(GattServiceRequestHandler requestHandler);
}
