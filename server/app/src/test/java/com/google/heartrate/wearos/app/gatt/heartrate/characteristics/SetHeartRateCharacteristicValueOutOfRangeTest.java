package com.google.heartrate.wearos.app.gatt.heartrate.characteristics;

import android.os.Build;

import com.google.heartrate.wearos.app.gatt.CharacteristicsArgumentProvider;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Collection;

import static com.google.heartrate.wearos.app.gatt.CharacteristicsArgumentProvider.Range.OUT_UINT16;

@RunWith(ParameterizedRobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.O_MR1}, manifest = Config.NONE)
public class SetHeartRateCharacteristicValueOutOfRangeTest {

    /** Heart Rate Measurement Value to test. */
    @ParameterizedRobolectricTestRunner.Parameter(0)
    public int heartRateMeasurementValue;

    /** Expended Energy Value to test. */
    @ParameterizedRobolectricTestRunner.Parameter(1)
    public int expendedEnergyValue;

    private HeartRateMeasurementCharacteristic characteristic;

    @SuppressWarnings("rawtypes")
    @NotNull
    @ParameterizedRobolectricTestRunner.Parameters(name = "Both out of UInt16 range: setHeartRateCharacteristicValues({0}, {1})")
    public static Collection provideBothOutOfUInt16RangeArguments() {
        return CharacteristicsArgumentProvider.provideCollection(OUT_UINT16, OUT_UINT16);
    }

    @Before
    public void setUp() {
        characteristic = new HeartRateMeasurementCharacteristic();
    }

    @Test
    public void test() {
        HeartRateGattCharacteristicManagerTest.assertSetValuesFailed(characteristic,
                heartRateMeasurementValue, expendedEnergyValue);
    }
}
