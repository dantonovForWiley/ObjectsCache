package com.dantonov.wiley.objectscache.impl.storage.exceptions;

import com.dantonov.wiley.objectscache.impl.storage.SerializableStorage;

import java.util.UUID;

/**
 * Exception for case when {@link SerializableStorage} failed to
 * {@link SerializableStorage#store(UUID, byte[])} a data
 */
public class FailedToStoreDataInStorage extends Exception {
    public FailedToStoreDataInStorage(UUID uuid) {
        super(uuid.toString());
    }
}