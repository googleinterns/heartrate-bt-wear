package com.google.heartrate.wearos.app.gatt.heartrate.characteristics.controlpoint;

import android.os.Build;

import com.google.heartrate.wearos.app.gatt.GattException;
import com.google.heartrate.wearos.app.gatt.heartrate.characteristics.HeartRateControlPointCharacteristic;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.O_MR1}, manifest = Config.NONE)
public class HeartRateControlPointCharacteristicTest {

    private HeartRateControlPointCharacteristic characteristic;

    @Before
    public void setUp() {
        characteristic = new HeartRateControlPointCharacteristic();
    }

    /**
     * Assent setting value to given Heart Rate Control Point Characteristic  was successful.
     * <br/>Assents no exceptions was thrown and after set Reset Energy Expended Value is
     * equal to requested.
     *
     * @param characteristic           characteristic to set value
     * @param resetEnergyExpendedValue reset energy expended value to set
     */
    public static void assertSetValueSuccessful(HeartRateControlPointCharacteristic characteristic,
                                                int resetEnergyExpendedValue) throws GattException {

        characteristic.setResetEnergyExpendedValue(resetEnergyExpendedValue);
        int currentResetEnergyExpendedValue = characteristic.getResetEnergyExpendedValue();

        assertEquals(resetEnergyExpendedValue, currentResetEnergyExpendedValue);
    }

    /**
     * Assent set value to given Heart Rate Control Point Characteristic fails with GattException.
     *
     * @param characteristic           characteristic to set value
     * @param resetEnergyExpendedValue reset energy expended value to set
     */
    public static void assertSetValueFailed(HeartRateControlPointCharacteristic characteristic,
                                            int resetEnergyExpendedValue) {
        assertThrows(GattException.class, () -> characteristic
                .setResetEnergyExpendedValue(resetEnergyExpendedValue));
    }

    /**
     * Assent failed set values to given Heart Rate Control Point Characteristic not change stored value.
     *
     * @param characteristic           characteristic to set value
     * @param resetEnergyExpendedValue reset energy expended value to set
     */
    public static void assertValueNotChangedAfterFailedSet(
            HeartRateControlPointCharacteristic characteristic, int resetEnergyExpendedValue) throws GattException {

        int beforeSetResetEnergyExpendedValue = characteristic.getResetEnergyExpendedValue();

        assertSetValueFailed(characteristic, resetEnergyExpendedValue);

        int currentResetEnergyExpendedValue = characteristic.getResetEnergyExpendedValue();

        assertEquals(beforeSetResetEnergyExpendedValue, currentResetEnergyExpendedValue);
    }

    @Test
    public void setResetEnergyExpendedValueInUInt8Test() throws GattException {
        int resetEnergyExpended = 1;

        assertSetValueSuccessful(characteristic, resetEnergyExpended);
    }

    @Test
    public void setResetEnergyExpendedValueOutOfUInt8Test() throws GattException {
        int resetEnergyExpendedUInt8 = 1;

        assertSetValueSuccessful(characteristic, resetEnergyExpendedUInt8);

        int resetEnergyExpendedOutUInt8 = 1 << 8 + 5;
        assertValueNotChangedAfterFailedSet(characteristic, resetEnergyExpendedOutUInt8);
    }
}