package com.dantonov.wiley.objectscache.impl.storage.exceptions;

import com.dantonov.wiley.objectscache.impl.storage.SerializableStorage;

import java.util.UUID;

/**
 * Exception for the case when
 * {@link SerializableStorage} failed to
 * {@link SerializableStorage#retrieve(UUID)} the data
 */
public class FailedToRetrieveStorageData extends Exception {
    public FailedToRetrieveStorageData(UUID uuid) {
        super(uuid.toString());
    }
}
