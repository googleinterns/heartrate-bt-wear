package com.google.heartrate.wearos.app.gatt.heartrate.characteristics;

import android.os.Build;

import com.google.heartrate.wearos.app.gatt.CharacteristicsArgumentProvider;
import com.google.heartrate.wearos.app.gatt.GattException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.google.heartrate.wearos.app.gatt.CharacteristicsArgumentProvider.Range.IN_UINT16;
import static com.google.heartrate.wearos.app.gatt.CharacteristicsArgumentProvider.Range.IN_UINT8;
import static com.google.heartrate.wearos.app.gatt.CharacteristicsArgumentProvider.Range.OUT_UINT16;
import static com.google.heartrate.wearos.app.gatt.TestUtils.assertNotThrows;
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
    public static void assertSetValuesSuccessful(HeartRateMeasurementCharacteristic characteristic,
                                                 int heartRateValue, int expendedEnergyValue) {
        final int[] currentHeartRateMeasurementValue = new int[1];
        final int[] currentExpendedEnergyValue = new int[1];

        assertNotThrows(() -> characteristic
                .setHeartRateCharacteristicValue(heartRateValue, expendedEnergyValue));
        assertNotThrows(() -> currentHeartRateMeasurementValue[0] = characteristic
                .getHeartRateMeasurementValue());
        assertNotThrows(() -> currentExpendedEnergyValue[0] = characteristic
                .getExpendedEnergyValue());

        assertEquals(heartRateValue, currentHeartRateMeasurementValue[0]);
        assertEquals(expendedEnergyValue, currentExpendedEnergyValue[0]);
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
                                                            int heartRateValue, int expendedEnergyValue) {
        final int[] beforeSetHeartRateMeasurementValue = new int[1];
        final int[] beforeSetExpendedEnergyValue = new int[1];

        final int[] currentHeartRateMeasurementValue = new int[1];
        final int[] currentExpendedEnergyValue = new int[1];

        assertNotThrows(() -> beforeSetHeartRateMeasurementValue[0] = characteristic
                .getHeartRateMeasurementValue());
        assertNotThrows(() -> beforeSetExpendedEnergyValue[0] = characteristic
                .getExpendedEnergyValue());

        assertSetValuesFailed(characteristic, heartRateValue, expendedEnergyValue);

        assertNotThrows(() -> currentHeartRateMeasurementValue[0] = characteristic
                .getHeartRateMeasurementValue());
        assertNotThrows(() -> currentExpendedEnergyValue[0] = characteristic
                .getExpendedEnergyValue());

        assertEquals(beforeSetHeartRateMeasurementValue[0], currentHeartRateMeasurementValue[0]);
        assertEquals(beforeSetExpendedEnergyValue[0], currentExpendedEnergyValue[0]);
    }

    @Test
    public void setHeartRateMeasurementValueOutUInt16AfterUInt16Test() {
        int heartRateUInt16Value = CharacteristicsArgumentProvider.provide(IN_UINT16)[0];
        int expendedEnergyUInt16Value = CharacteristicsArgumentProvider.provide(IN_UINT16)[1];

        assertSetValuesSuccessful(characteristic,
                heartRateUInt16Value, expendedEnergyUInt16Value);

        int heartRateOutUInt16Value = CharacteristicsArgumentProvider.provide(OUT_UINT16)[0];

        assertValuesNotChangedAfterFailedSet(characteristic,
                heartRateOutUInt16Value, expendedEnergyUInt16Value);
    }

    @Test
    public void setExpendedEnergyValueOutUInt16AfterUInt16Test() {
        int heartRateUInt16Value = CharacteristicsArgumentProvider.provide(IN_UINT16)[0];
        int expendedEnergyUInt16Value = CharacteristicsArgumentProvider.provide(IN_UINT16)[1];

        assertSetValuesSuccessful(characteristic,
                heartRateUInt16Value, expendedEnergyUInt16Value);

        int expendedEnergyOutUInt16Value = CharacteristicsArgumentProvider.provide(OUT_UINT16)[0];

        assertValuesNotChangedAfterFailedSet(characteristic,
                heartRateUInt16Value, expendedEnergyOutUInt16Value);
    }

    @Test
    public void getValuesWithoutSettingThrowExceptionTest() {
        assertThrows(GattException.class, () -> characteristic.getHeartRateMeasurementValue());
        assertThrows(GattException.class, () -> characteristic.getExpendedEnergyValue());
    }

    @Test
    public void whenExpendedEnergyNotPresentSetAndGetTest() {
        int heartRateUInt16Value = CharacteristicsArgumentProvider.provide(IN_UINT16)[0];
        int expendedEnergyUInt16Value = CharacteristicsArgumentProvider.provide(IN_UINT16)[1];

        assertSetValuesSuccessful(characteristic,
                heartRateUInt16Value, expendedEnergyUInt16Value);

        int heartRateUInt8Value = CharacteristicsArgumentProvider.provide(IN_UINT8)[0];

        assertSetValuesSuccessful(characteristic,
                heartRateUInt8Value, expendedEnergyUInt16Value);
    }

    @Test
    public void getExpendedEnergyWhenNotPresentTest() {
        int heartRateUInt16Value = CharacteristicsArgumentProvider.provide(IN_UINT16)[0];
        assertNotThrows(() -> characteristic.setHeartRateCharacteristicValue(heartRateUInt16Value));
        assertThrows(GattException.class, () -> characteristic.getExpendedEnergyValue());

        int heartRateUInt8Value = CharacteristicsArgumentProvider.provide(IN_UINT16)[0];
        assertNotThrows(() -> characteristic.setHeartRateCharacteristicValue(heartRateUInt8Value));
        assertThrows(GattException.class, () -> characteristic.getExpendedEnergyValue());
    }
}
