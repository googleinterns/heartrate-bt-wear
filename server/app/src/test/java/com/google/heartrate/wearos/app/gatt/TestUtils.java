package com.google.heartrate.wearos.app.gatt;

import org.junit.function.ThrowingRunnable;

import static org.junit.Assert.fail;

/**
 * Class TestUtils provide useful methods and custom assertions for testing.
 */
public class TestUtils {

    /**
     * Assert given throwing runnable arguments does not throw exception.
     * <br>Fail on runnable arguments throw exception on run().
     *
     * @param throwingRunnable throwing runnable arguments to check
     */
    public static void assertNotThrows(ThrowingRunnable throwingRunnable) {
        try {
            throwingRunnable.run();
        } catch (Throwable e) {
            fail(e.getMessage());
        }
    }
}
