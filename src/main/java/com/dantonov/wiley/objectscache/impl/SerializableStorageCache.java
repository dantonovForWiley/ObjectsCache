package com.dantonov.wiley.objectscache.impl;

import com.dantonov.wiley.objectscache.Cache;
import com.dantonov.wiley.objectscache.ChangeableConfigurationValue;
import com.dantonov.wiley.objectscache.ConfigurationValue;
import com.dantonov.wiley.objectscache.exceptions.AllocationInCacheException;
import com.dantonov.wiley.objectscache.exceptions.ObjectNotFoundInCache;
import com.dantonov.wiley.objectscache.impl.storage.SerializableStorage;
import com.dantonov.wiley.objectscache.impl.storage.SerializationUtil;
import com.dantonov.wiley.objectscache.impl.storage.exceptions.DataNotFoundInStorage;
import com.dantonov.wiley.objectscache.impl.storage.exceptions.FailedToDeleteDataInStorage;
import com.dantonov.wiley.objectscache.impl.storage.exceptions.FailedToRetrieveStorageData;
import com.dantonov.wiley.objectscache.impl.storage.exceptions.FailedToStoreDataInStorage;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * {@link Cache} implementation to store object on filesystem<br>
 * <b>NOTE:</b> only serializable objects will be cached for this implementation
 */
public class SerializableStorageCache implements Cache {

    /**
     * {@link SerializableStorageCache} constructor
     *
     * @param serializableStorage underlying {@link SerializableStorage}
     */
    public SerializableStorageCache(SerializableStorage serializableStorage, Long sizeLimitation) {
        this.serializableStorage = serializableStorage;
        storedObjects = new HashSet<>();
        consumingSizeConfigurationValue = new GenericConfigurationValue<>(sizeLimitation, (
                (currentState, specialState) -> currentState < specialState), ConfigurationValue
                .WARN_LEVEL.CRITICAL, value -> String.format("Max allowed occupied size in bytes " +
                "is %s", value), (value -> String.format("Current occupied size in bytes " +
                "is %s", value)));
    }

    @Override
    public Set<ConfigurationValue> getConfigurationValues() {
        return Arrays.asList(consumingSizeConfigurationValue).stream().collect(Collectors.toSet());
    }

    @Override
    public void allocateObject(ObjectInCache cachedObject) throws AllocationInCacheException {
        UUID uuid = cachedObject.getUuid();
        try {
            serializableStorage.store(uuid, SerializationUtil.serializeObject(cachedObject.objectRef()));
            storedObjects.add(uuid);
            consumingSizeConfigurationValue.setCurrentState(serializableStorage.getOccupiedSize());
        } catch (FailedToStoreDataInStorage | IOException e) {
            throw new AllocationInCacheException();
        }
    }

    @Override
    public ObjectInCache freeObject(ObjectInCache cachedObject) throws ObjectNotFoundInCache, AllocationInCacheException {
        return retrieveObject(cachedObject, true);
    }

    @Override
    public ObjectInCache returnObject(ObjectInCache cachedObject) throws ObjectNotFoundInCache, AllocationInCacheException {
        return retrieveObject(cachedObject, false);
    }

    private ObjectInCache retrieveObject(ObjectInCache cachedObject, boolean remove) throws ObjectNotFoundInCache, AllocationInCacheException {
        UUID uuid = cachedObject.getUuid();
        if (!storedObjects.contains(uuid)) {
            throw new ObjectNotFoundInCache(cachedObject);
        }
        try {
            Object object = SerializationUtil.deserializeObject(serializableStorage.retrieve(uuid));
            if (remove) {
                serializableStorage.delete(uuid);
                storedObjects.remove(uuid);
            }
            consumingSizeConfigurationValue.setCurrentState(serializableStorage.getOccupiedSize());
            return ObjectInCache.from(cachedObject, object);
        } catch (IOException | ClassNotFoundException | FailedToRetrieveStorageData |
                DataNotFoundInStorage | FailedToDeleteDataInStorage e) {
            throw new AllocationInCacheException();
        }
    }

    @Override
    public Boolean contains(ObjectInCache objectInCache) {
        return storedObjects.contains
                (objectInCache.getUuid());
    }

    @Override
    public String getCacheName() {
        return FILESYSTEM_CACHE_NAME;
    }

    private SerializableStorage serializableStorage;
    private Set<UUID> storedObjects;
    private ChangeableConfigurationValue<Long> consumingSizeConfigurationValue;
    private static final String FILESYSTEM_CACHE_NAME = "Filesystem cache";
}
