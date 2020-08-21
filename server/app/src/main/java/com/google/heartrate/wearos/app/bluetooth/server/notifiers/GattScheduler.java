package com.google.heartrate.wearos.app.bluetooth.server.notifiers;

import android.util.Log;

import com.google.heartrate.wearos.app.gatt.GattException;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Class {@link GattScheduler} provides methods to schedule operations.
 */
public abstract class GattScheduler {
    private static final String TAG = GattScheduler.class.getSimpleName();

    /**  Delay before first notification (seconds). */
    private static final long NOTIFY_FIRST_WAIT_PERIOD = 1000;

    /** Period between successive notifications (seconds). */
    private static final long NOTIFY_WAIT_PERIOD = 1000;

    /** Instance of {@link ScheduledExecutorService} for notification scheduling. */
    private ScheduledExecutorService notificationExecutor = Executors.newSingleThreadScheduledExecutor();

    /** {@link ScheduledFuture} of notification timer. */
    private ScheduledFuture<?> notificationTimerFuture;

    public abstract void runScheduled() throws GattException;

    /** Start scheduled run with default parameters. */
    public void start() {
        stop();
        start(NOTIFY_FIRST_WAIT_PERIOD, NOTIFY_WAIT_PERIOD);
    }

    /**
     * Start scheduled run with given parameters.
     * @param firstWaitPeriod delay before first notification (seconds)
     * @param waitPeriod period between successive notifications (seconds)
     */
    public void start(long firstWaitPeriod, long waitPeriod) {
        stop();
        notificationTimerFuture = notificationExecutor.scheduleWithFixedDelay(() -> {
            try {
                runScheduled();
            } catch (GattException e) {
                Log.e(TAG, e.getMessage());
            }
        }, firstWaitPeriod, waitPeriod, TimeUnit.MILLISECONDS);
    }

    /** Stop scheduled run. */
    public void stop() {
        if (notificationTimerFuture != null) {
            notificationTimerFuture.cancel(true);
            notificationTimerFuture = null;
        }
    }
}
