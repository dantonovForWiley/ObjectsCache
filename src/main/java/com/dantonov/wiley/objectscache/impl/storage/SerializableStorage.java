package com.dantonov.wiley.objectscache.impl.storage;

import com.dantonov.wiley.objectscache.impl.storage.exceptions.DataNotFoundInStorage;
import com.dantonov.wiley.objectscache.impl.storage.exceptions.FailedToDeleteDataInStorage;
import com.dantonov.wiley.objectscache.impl.storage.exceptions.FailedToRetrieveStorageData;
import com.dantonov.wiley.objectscache.impl.storage.exceptions.FailedToStoreDataInStorage;

import java.util.UUID;

/**
 * Models storage to save <b>serializable</b> data
 */
public interface SerializableStorage {

    /**
     * Method to save the binary data
     *
     * @param uuid    identifier for the data
     * @param context binary data to save
     * @throws FailedToStoreDataInStorage in case when storage has failed to save the data
     */
    void store(UUID uuid, byte[] context) throws FailedToStoreDataInStorage;

    /**
     * Method to retrieve stored data
     *
     * @param uuid identifier for the data
     * @return binary data
     * @throws FailedToRetrieveStorageData in case when storage has faile to retrieve the data
     * @throws DataNotFoundInStorage       in case when data has not been found
     */
    byte[] retrieve(UUID uuid) throws FailedToRetrieveStorageData, DataNotFoundInStorage;

    /**
     * Method to delete stored data from storage
     *
     * @param uuid identifier for the data
     * @throws DataNotFoundInStorage       in case when data has not been found
     * @throws FailedToDeleteDataInStorage in case when storage has failed to delete the data
     */
    void delete(UUID uuid) throws DataNotFoundInStorage, FailedToDeleteDataInStorage;

    /**
     * Method to retrieve an occupied data size. Lets suppose it measured in bytes
     * @return occupied place size in bytes
     */
    long getOccupiedSize();
}
