package com.google.heartrate.wearos.app.gatt.heartrate.characteristics;

import android.os.Build;

import com.google.heartrate.wearos.app.gatt.CharacteristicsArgumentProvider;
import com.google.heartrate.wearos.app.gatt.GattException;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Collection;

import static com.google.heartrate.wearos.app.gatt.CharacteristicsArgumentProvider.Range.IN_UINT16;

@RunWith(ParameterizedRobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.O_MR1}, manifest = Config.NONE)
public class SetHeartRateCharacteristicValueInRangeTest {

    /** Heart Rate Measurement Value to test. */
    @ParameterizedRobolectricTestRunner.Parameter(0)
    public int heartRateMeasurementValue;

    /** Expended Energy Value to test. */
    @ParameterizedRobolectricTestRunner.Parameter(1)
    public int expendedEnergyValue;

    private HeartRateMeasurementCharacteristic characteristic;

    @SuppressWarnings("rawtypes")
    @NotNull
    @ParameterizedRobolectricTestRunner.Parameters(name = "Both in UInt16 range: setHeartRateCharacteristicValues({0}, {1})")
    public static Collection provideBothInUInt16RangeArguments() {
        return CharacteristicsArgumentProvider.provideCollection(IN_UINT16, IN_UINT16);
    }

    @Before
    public void setUp() {
        characteristic = new HeartRateMeasurementCharacteristic();
    }

    @Test
    public void test() throws GattException {
        HeartRateMeasurementCharacteristicTest.assertSetValuesSuccessful(characteristic,
                heartRateMeasurementValue, expendedEnergyValue);
    }
}
