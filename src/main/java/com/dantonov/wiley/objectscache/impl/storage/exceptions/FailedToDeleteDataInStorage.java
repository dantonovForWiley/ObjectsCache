package com.dantonov.wiley.objectscache.impl.storage.exceptions;

import com.dantonov.wiley.objectscache.impl.storage.SerializableStorage;

import java.util.UUID;

/**
 * Exception for the case when
 * {@link SerializableStorage} failed to
 * {@link SerializableStorage#delete(UUID)} the data from storage
 */
public class FailedToDeleteDataInStorage extends Exception {
    public FailedToDeleteDataInStorage(UUID uuid) {
        super(uuid.toString());
    }
}
