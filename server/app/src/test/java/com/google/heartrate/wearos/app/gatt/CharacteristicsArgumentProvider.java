package com.google.heartrate.wearos.app.gatt;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * CharacteristicsArgumentProvider provides argument to test set/get operation in Characteristics.
 * <p>
 * Provides arrays of different type arguments with extreme values, in/out of type range.
 */
public class CharacteristicsArgumentProvider {

    private static int[] outUInt8RangeValues = new int[]{
            FormatUtils.MIN_UINT - 1, FormatUtils.MAX_UINT8 + 1, FormatUtils.MAX_UINT16};

    private static int[] inUInt8RangeValues = new int[]{
            FormatUtils.MIN_UINT, FormatUtils.MIN_UINT + 1,
            FormatUtils.MAX_UINT8 - 1, FormatUtils.MAX_UINT8};

    private static int[] outUInt16RangeValues = new int[]{
            FormatUtils.MIN_UINT - 1, FormatUtils.MAX_UINT16 + 1,
            Integer.MAX_VALUE, Integer.MIN_VALUE,
            -FormatUtils.MAX_UINT16};

    private static int[] inUInt16RangeValues = IntStream
            .rangeClosed(0, 16)
            .map(i -> (1 << i) - 1)
            .toArray();

    /**
     * Build cartesian product of two given arrays.
     *
     * @param firstArray  array first arguments in product
     * @param secondArray array of second arguments in product
     * @return cartesian product of two given arrays
     */
    private static List<Object[]> getCartesianProduct(int[] firstArray, int[] secondArray) {
        List<Object[]> params = new ArrayList<>();
        for (int firstValue : firstArray) {
            for (int secondValue : secondArray) {
                params.add(new Object[]{firstValue, secondValue});
            }
        }
        return params;
    }

    /**
     * Provide collection of cartesian product of two array of arguments in given range.
     *
     * @param firstRange  range for first argument in cartesian product
     * @param secondRange range for second argument in cartesian product
     * @return cartesian product of two array of arguments in given range
     */
    public static List<Object[]> provideCollection(Range firstRange, Range secondRange) {
        return getCartesianProduct(provide(firstRange), provide(secondRange));
    }

    /**
     * Provide collection of arguments in given range.
     *
     * @param range range in arguments should be
     * @return collection of arguments in given range
     */
    public static List<Object[]> provideCollection(Range range) {
        return IntStream.of(provide(range))
                .mapToObj(arg -> new Object[]{arg})
                .collect(Collectors.toList());
    }

    /**
     * Provide array of arguments in given range.
     *
     * @param range range in witch provide arguments
     * @return arguments in given range
     */
    public static int[] provide(Range range) {
        switch (range) {
            case OUT_UINT8:
                return outUInt8RangeValues;
            case OUT_UINT16:
                return outUInt16RangeValues;
            case IN_UINT8:
                return inUInt8RangeValues;
            case IN_UINT16:
                return inUInt16RangeValues;
        }
        throw new AssertionError("Format not supported");
    }

    public enum Range {
        IN_UINT8,
        IN_UINT16,
        OUT_UINT8,
        OUT_UINT16
    }
}
