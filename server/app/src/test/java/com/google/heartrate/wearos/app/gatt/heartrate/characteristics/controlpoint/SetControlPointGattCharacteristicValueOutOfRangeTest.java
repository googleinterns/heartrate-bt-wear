package com.google.heartrate.wearos.app.gatt.heartrate.characteristics.controlpoint;

import android.os.Build;

import com.google.heartrate.wearos.app.gatt.CharacteristicsArgumentProvider;
import com.google.heartrate.wearos.app.gatt.heartrate.characteristics.HeartRateControlPointCharacteristic;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Collection;

import static com.google.heartrate.wearos.app.gatt.CharacteristicsArgumentProvider.Range.OUT_UINT8;

@RunWith(ParameterizedRobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.O_MR1}, manifest = Config.NONE)
public class SetControlPointGattCharacteristicValueOutOfRangeTest {

    /** Reset Energy Expended Value to test. */
    @ParameterizedRobolectricTestRunner.Parameter
    public int resetEnergyExpendedValue;

    private HeartRateControlPointCharacteristic characteristic;

    @SuppressWarnings("rawtypes")
    @NotNull
    @ParameterizedRobolectricTestRunner.Parameters(name = "REE out of UInt8 range: setResetEnergyExpendedValue({0})")
    public static Collection provideBothInUInt16RangeArguments() {
        return CharacteristicsArgumentProvider.provideCollection(OUT_UINT8);
    }

    @Before
    public void setUp() {
        characteristic = new HeartRateControlPointCharacteristic();
    }

    @Test
    public void test() {
        HeartRateControlPointCharacteristicTest
                .assertSetValueFailed(characteristic, resetEnergyExpendedValue);
    }
}