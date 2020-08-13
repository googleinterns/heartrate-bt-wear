package com.google.heartrate.wearos.app.gatt.heartrate.characteristics.sensorlocation;

import android.os.Build;

import com.google.heartrate.wearos.app.gatt.GattException;
import com.google.heartrate.wearos.app.gatt.heartrate.characteristics.BodySensorLocationCharacteristic;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.O_MR1}, manifest = Config.NONE)
public class BodySensorLocationCharacteristicTest {

    private BodySensorLocationCharacteristic characteristic;

    @Before
    public void setUp() {
        characteristic = new BodySensorLocationCharacteristic();
    }

    /**
     * Assent setting value to given Body Sensor Location Characteristic was successful.
     * <br/>Assents no exceptions was thrown and after set Body Sensor Location Value is
     * equal to requested.
     *
     * @param characteristic          characteristic to set value
     * @param BodySensorLocationValue body sensor location value to set
     */
    public static void assertSetValueSuccessful(
            BodySensorLocationCharacteristic characteristic, int BodySensorLocationValue) throws GattException {

        characteristic.setBodySensorLocationValue(BodySensorLocationValue);
        int currentBodySensorLocationValue = characteristic.getBodySensorLocationValue();

        assertEquals(BodySensorLocationValue, currentBodySensorLocationValue);
    }

    /**
     * Assent set value to given Heart Body Sensor Location Characteristic fails with GattException.
     *
     * @param characteristic          characteristic to set value
     * @param bodySensorLocationValue body sensor location value to set
     */
    public static void assertSetValueFailed(
            BodySensorLocationCharacteristic characteristic, int bodySensorLocationValue) {
        assertThrows(GattException.class, () -> characteristic
                .setBodySensorLocationValue(bodySensorLocationValue));
    }

    /**
     * Assent failed set values to given Body Sensor Location Characteristic not change stored value.
     *
     * @param characteristic          characteristic to set value
     * @param bodySensorLocationValue body sensor location value to set
     */
    public static void assertValueNotChangedAfterFailedSet(
            BodySensorLocationCharacteristic characteristic, int bodySensorLocationValue) throws GattException {
        int beforeSetBodySensorLocationValue = characteristic
                .getBodySensorLocationValue();

        assertSetValueFailed(characteristic, bodySensorLocationValue);

        int currentBodySensorLocationValue = characteristic
                .getBodySensorLocationValue();

        assertEquals(beforeSetBodySensorLocationValue, currentBodySensorLocationValue);
    }

    @Test
    public void setBodySensorLocationValueInUInt8Test() throws GattException {
        int bodySensorLocation = BodySensorLocationCharacteristic.Locations.CHEST;

        assertSetValueSuccessful(characteristic, bodySensorLocation);
    }

    @Test
    public void setBodySensorLocationValueOutOfUInt8Test() throws GattException {
        int bodySensorLocationUInt8 = BodySensorLocationCharacteristic.Locations.CHEST;

        assertSetValueSuccessful(characteristic, bodySensorLocationUInt8);

        int bodySensorLocationOutUInt8 = 1 << 8 + 5;
        assertValueNotChangedAfterFailedSet(characteristic, bodySensorLocationOutUInt8);
    }
}
