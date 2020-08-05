package com.google.heartrate.wearos.app.gatt.attributes;

import java.util.UUID;

/**
 * {@link GattAttribute} is an interface for all GATT (ATT) protocol attributes.
 * According to GATT, all attributes should have Universally Unique Identifier (UUID),
 * which is a standardized 128-bit format for a string ID used to uniquely identify information.
 */
public interface GattAttribute {

    /**
     * Get attribute UUID.
     *
     * @return attribute UUID
     */
    UUID getUUid();
}
