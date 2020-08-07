package com.google.heartrate.wearos.app.gatt.heartrate.characteristics;

import android.os.Build;

import com.google.heartrate.wearos.app.gatt.GattException;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.O_MR1}, manifest = Config.NONE)
public class HeartRateMeasurementCharacteristicTest {

    private HeartRateMeasurementCharacteristic characteristic;

    @Before
    public void setUp() {
        characteristic = new HeartRateMeasurementCharacteristic();
    }

    /**
     * Assert setting values to given Heart Rate Characteristic was successful.
     * <br/>Assert no exceptions was thrown and after set Heart Rate Value and Expended Energy Value
     * are equal to requested.
     *
     * @param characteristic      characteristic to set value
     * @param heartRateValue      heart rate value to set
     * @param expendedEnergyValue expended energy value to set
     */
    public static void assertSetValuesSuccessful(@NotNull HeartRateMeasurementCharacteristic characteristic,
                                                 int heartRateValue, int expendedEnergyValue) throws GattException {
        characteristic.setHeartRateCharacteristicValue(heartRateValue, expendedEnergyValue);
        int currentHeartRateMeasurementValue = characteristic.getHeartRateMeasurementValue();
        int currentExpendedEnergyValue = characteristic.getExpendedEnergyValue();

        assertEquals(heartRateValue, currentHeartRateMeasurementValue);
        assertEquals(expendedEnergyValue, currentExpendedEnergyValue);
    }

    /**
     * Assert set values to given Heart Rate Characteristic fails with GattException.
     *
     * @param characteristic      characteristic to set value
     * @param heartRateValue      heart rate value to set
     * @param expendedEnergyValue expended energy value to set
     */
    public static void assertSetValuesFailed(HeartRateMeasurementCharacteristic characteristic,
                                             int heartRateValue, int expendedEnergyValue) {
        assertThrows(GattException.class, () -> characteristic
                .setHeartRateCharacteristicValue(heartRateValue, expendedEnergyValue));
    }

    /**
     * Assert failed set values to given Heart Rate Characteristic not change stored values.
     *
     * @param characteristic      characteristic to set value
     * @param heartRateValue      heart rate value to set
     * @param expendedEnergyValue expended energy value to set
     */
    public static void assertValuesNotChangedAfterFailedSet(HeartRateMeasurementCharacteristic characteristic,
                                                            int heartRateValue, int expendedEnergyValue) throws GattException {
        int beforeSetHeartRateMeasurementValue = characteristic.getHeartRateMeasurementValue();
        int beforeSetExpendedEnergyValue = characteristic.getExpendedEnergyValue();

        assertSetValuesFailed(characteristic, heartRateValue, expendedEnergyValue);

        int currentHeartRateMeasurementValue = characteristic.getHeartRateMeasurementValue();
        int currentExpendedEnergyValue = characteristic.getExpendedEnergyValue();

        assertEquals(beforeSetHeartRateMeasurementValue, currentHeartRateMeasurementValue);
        assertEquals(beforeSetExpendedEnergyValue, currentExpendedEnergyValue);
    }

    @Test
    public void setHeartRateMeasurementValueOutUInt16Test() throws GattException {
        int heartRateUInt16Value = 1 << 8 + 5;
        int expendedEnergyUInt16Value = 1 << 8 + 6;

        assertSetValuesSuccessful(characteristic,
                heartRateUInt16Value, expendedEnergyUInt16Value);

        int heartRateOutUInt16Value = 1 << 16 + 5;

        assertValuesNotChangedAfterFailedSet(characteristic,
                heartRateOutUInt16Value, expendedEnergyUInt16Value);
    }

    @Test
    public void setExpendedEnergyValueOutUInt16Test() throws GattException {
        int heartRateUInt16Value = 1 << 8 + 5;
        int expendedEnergyUInt16Value = 1 << 8 + 6;

        assertSetValuesSuccessful(characteristic,
                heartRateUInt16Value, expendedEnergyUInt16Value);

        int expendedEnergyOutUInt16Value = 1 << 16 + 5;

        assertValuesNotChangedAfterFailedSet(characteristic,
                heartRateUInt16Value, expendedEnergyOutUInt16Value);
    }

    @Test
    public void getValuesWithoutSettingThrowExceptionTest() {
        assertThrows(GattException.class, () -> characteristic.getHeartRateMeasurementValue());
        assertThrows(GattException.class, () -> characteristic.getExpendedEnergyValue());
    }

    @Test
    public void setHeartRateMeasurementValueUInt8UInt16Test() throws GattException {
        int heartRateUInt16Value = 1 << 8 + 5;
        int expendedEnergyUInt16Value = 1 << 8 + 6;

        assertSetValuesSuccessful(characteristic,
                heartRateUInt16Value, expendedEnergyUInt16Value);

        int heartRateUInt8Value = 1 << 8 - 5;

        assertSetValuesSuccessful(characteristic,
                heartRateUInt8Value, expendedEnergyUInt16Value);

        assertSetValuesSuccessful(characteristic,
                heartRateUInt16Value, expendedEnergyUInt16Value);
    }

    @Test
    public void getExpendedEnergyWhenNotPresentTest() throws GattException {
        int heartRateUInt16Value = 1 << 8 + 5;
        characteristic.setHeartRateCharacteristicValue(heartRateUInt16Value);
        assertThrows(GattException.class, () -> characteristic.getExpendedEnergyValue());

        int heartRateUInt8Value = 1 << 8 - 5;
        characteristic.setHeartRateCharacteristicValue(heartRateUInt8Value);
        assertThrows(GattException.class, () -> characteristic.getExpendedEnergyValue());
    }
}
