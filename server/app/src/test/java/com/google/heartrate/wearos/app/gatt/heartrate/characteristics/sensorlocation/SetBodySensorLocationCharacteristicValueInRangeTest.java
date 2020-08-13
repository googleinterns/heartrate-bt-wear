package com.google.heartrate.wearos.app.gatt.heartrate.characteristics.sensorlocation;

import android.os.Build;

import com.google.heartrate.wearos.app.gatt.CharacteristicsArgumentProvider;
import com.google.heartrate.wearos.app.gatt.GattException;
import com.google.heartrate.wearos.app.gatt.heartrate.characteristics.BodySensorLocationCharacteristic;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Collection;

import static com.google.heartrate.wearos.app.gatt.CharacteristicsArgumentProvider.Range.IN_UINT8;

@RunWith(ParameterizedRobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.O_MR1}, manifest = Config.NONE)
public class SetBodySensorLocationCharacteristicValueInRangeTest {

    /** Body Sensor Location Value to test. */
    @ParameterizedRobolectricTestRunner.Parameter
    public int resetEnergyExpendedValue;

    private BodySensorLocationCharacteristic characteristic;

    @SuppressWarnings("rawtypes")
    @NotNull
    @ParameterizedRobolectricTestRunner.Parameters(name = "BSL in UInt8 range: setBodySensorLocationValue({0})")
    public static Collection provideInUInt8RangeArguments() {
        return CharacteristicsArgumentProvider.provideCollection(IN_UINT8);
    }

    @Before
    public void setUp() {
        characteristic = new BodySensorLocationCharacteristic();
    }

    @Test
    public void test() throws GattException {
        BodySensorLocationCharacteristicTest
                .assertSetValueSuccessful(characteristic, resetEnergyExpendedValue);
    }
}
