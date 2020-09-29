package com.google.heartrate.androidos.app.gatt;

/**
 * FormatUtils provides methods for value format checking.
 */
public class FormatUtils {

    /** Minimum value for UInt16 values. */
    private static final int MIN_UINT = 0;

    /** Maximum value for UInt8 values. */
    private static final int MAX_UINT8 = (1 << 8) - 1;

    /** Maximum value for UInt16 values. */
    private static final int MAX_UINT16 = (1 << 16) - 1;

    private FormatUtils() {}

    /**
     * Determine whether the given value is in UInt8 range or not.
     *
     * @param value value to check
     * @return true if in UInt8 range, false otherwise
     */
    public static boolean isInUInt8Range(int value) {
        return value >= MIN_UINT && value <= MAX_UINT8;
    }

    /**
     * Determine whether the given value is in UInt16 range or not.
     *
     * @param value value to check
     * @return true if in UInt16 range, false otherwise
     */
    public static boolean isInUInt16Range(int value) {
        return value >= MIN_UINT && value <= MAX_UINT16;
    }

    /**
     * Asserts that the given argument is in UInt8 format.
     *
     * @param value value to check
     * @throws GattException if not in UInt8 format.
     */
    public static void assertIsUInt8(int value) throws GattException {
        if (!isInUInt8Range(value)) {
            throw new GattException(String.format("Value %d is out of bounds. Expected UINT8.", value));
        }
    }

    /**
     * Asserts that the given argument is in UInt16 format.
     *
     * @param value value to check
     * @throws GattException if not in UInt16 format.
     */
    public static void assertIsUInt16(int value) throws GattException {
        if (!isInUInt16Range(value)) {
            throw new GattException(String.format("Value %d is out of bounds. Expected UINT16.", value));
        }
    }
}
