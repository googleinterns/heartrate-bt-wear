package com.google.heartrate.androidos.app.bluetooth.client;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;

import com.google.heartrate.androidos.app.bluetooth.BluetoothUtils;
import com.google.heartrate.androidos.app.gatt.GattException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class BluetoothScanner {
    private static final String TAG = BluetoothScanner.class.getSimpleName();

    /**
     * Delay before first execution.
     */
    private static final long SCAN_DELAY_MS = 0;
    /**
     * Period between successive executions.
     */
    private static final long SCAN_INTERVAL_MS = 10000;
    /**
     * Scan period.
     */
    private static final long SCAN_PERIOD_MS = 1000;
    /**
     * Instance of ScheduledExecutorService.
     */
    private ScheduledExecutorService mScanExecutor = Executors.newSingleThreadScheduledExecutor();
    /**
     * ScheduledFuture of scan timer.
     */
    private ScheduledFuture<?> mScanTimerFuture;
    private Handler mStopScanHandler = new Handler();
    private boolean mScanning;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private ScanCallback mBleScanCallback;

    public BluetoothScanner(final Context context,
                            ScanCallback bleScanCallback) throws GattException {
        BluetoothUtils.assertBluetoothIsSupported(context);
        mBleScanCallback = bleScanCallback;
        mBluetoothAdapter = BluetoothUtils.getBluetoothAdapter(context);
    }

    public void startScanLeDevice(UUID[] serviceUuids) {
        Log.d(TAG, "Start scanning process");
        Log.v(TAG,  String.format("Delay before first scan = %s ms", SCAN_DELAY_MS));
        Log.v(TAG,  String.format("Interval between two scan periods = %s ms", SCAN_INTERVAL_MS));
        Log.v(TAG,  String.format("Scan period = %s ms", SCAN_PERIOD_MS));

        if (mScanning || mScanTimerFuture != null) {
            Log.d(TAG, "Already scanning.");
            return;
        }

        mScanning = true;
        mScanTimerFuture = mScanExecutor.scheduleAtFixedRate(() -> {
            mStopScanHandler.postDelayed(() -> {
                if (mBluetoothAdapter.isEnabled()) {
                    stopScan();
                } else {
                    mScanTimerFuture.cancel(true);
                }
            }, SCAN_PERIOD_MS);

            if (mBluetoothAdapter.isEnabled()) {
                startScan(serviceUuids);
            } else {
                mScanTimerFuture.cancel(true);
            }
        }, SCAN_DELAY_MS, SCAN_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    public void stopScanLeDevice() {
        Log.d(TAG, "Stop scanning process");
        mScanning = false;
        stopScan();
        mScanExecutor.shutdown();
        if (mScanTimerFuture != null) {
            mScanTimerFuture.cancel(true);
            mScanTimerFuture = null;
        }
    }

    private void startScan(UUID[] serviceUuids) {
        Log.d(TAG, "Start scan");

        final ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        final List<ScanFilter> filters = new ArrayList<>();
        for (UUID serviceUuid : serviceUuids) {
            filters.add(new ScanFilter.Builder()
                    .setServiceUuid(new ParcelUuid(serviceUuid))
                    .build()
            );
        }

        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mBluetoothLeScanner.startScan(filters, settings, mBleScanCallback);
    }

    private void stopScan() {
        Log.d(TAG, "Stop scan");
        mBluetoothLeScanner.stopScan(mBleScanCallback);
    }
}
