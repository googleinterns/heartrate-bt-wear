package com.google.heartrate.androidos.app.gatt;

import android.bluetooth.BluetoothGatt;

/**
 * GattException class wraps all checked standard Java exception accrued in {@link com.google.heartrate.androidos.app.gatt} unsafe methods.
 */
public class GattException extends Exception {

    /** Status of situation caused the exception. Send it in response message to client. */
    private final int status;

    public GattException(String message, int status) {
        super(message);
        this.status = status;
    }

    public GattException(String message) {
        this(message, BluetoothGatt.GATT_FAILURE);
    }

    public GattException(Exception e, int status) {
        super(e);
        this.status = status;
    }

    public GattException(Exception e) {
        this(e, BluetoothGatt.GATT_FAILURE);
    }

    public int getStatus() {
        return status;
    }
}
