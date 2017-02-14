package com.dantonov.wiley.objectscache.impl;

import com.dantonov.wiley.objectscache.impl.storage.SerializableStorage;

import java.io.Serializable;

/**
 * Serializable object for test {@link SerializableStorage}
 */
public class TestSerializableObject implements Serializable {
    private int intValue;
    private String stringValue;
    private boolean booleanValue;

    public TestSerializableObject(int intValue, String stringValue, boolean booleanValue) {
        this.intValue = intValue;
        this.stringValue = stringValue;
        this.booleanValue = booleanValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestSerializableObject testSerializableObject = (TestSerializableObject) o;

        if (intValue != testSerializableObject.intValue) return false;
        if (booleanValue != testSerializableObject.booleanValue) return false;
        return stringValue != null ? stringValue.equals(testSerializableObject.stringValue) : testSerializableObject.stringValue == null;
    }

    @Override
    public int hashCode() {
        int result = intValue;
        result = 31 * result + (stringValue != null ? stringValue.hashCode() : 0);
        result = 31 * result + (booleanValue ? 1 : 0);
        return result;
    }
}

