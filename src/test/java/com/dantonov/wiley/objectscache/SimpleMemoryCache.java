package com.dantonov.wiley.objectscache;

import com.dantonov.wiley.objectscache.exceptions.ObjectNotFoundInCache;
import com.dantonov.wiley.objectscache.impl.GenericConfigurationValue;
import com.dantonov.wiley.objectscache.impl.ObjectInCache;

import java.util.*;

/**
 * Simple implementation for cache. Storing objects in internal Map
 */
public class SimpleMemoryCache implements Cache {

    public SimpleMemoryCache(ChangeableConfigurationValue<Integer> objectCountConfigurationValue) {
        this.objectCountConfigurationValue = objectCountConfigurationValue;
        this.objectCountConfigurationValue.setCurrentState(0);
    }

    public static class Builder {

        public Builder setMaxObjectSize(int maxObjectSize) {
            this.maxObjectSize = maxObjectSize;
            return this;
        }

        public SimpleMemoryCache build() {
            return new SimpleMemoryCache(new GenericConfigurationValue<>(maxObjectSize, ((currentValue, configurationValue) -> currentValue < configurationValue), ConfigurationValue.WARN_LEVEL.WARN, (value -> "Suppose max objects count in the cache: " + value), (value -> String.format("Now in the cache: %s objects", value))));
        }

        private int maxObjectSize = 1;
    }

    @Override
    public Set<ConfigurationValue> getConfigurationValues() {
        return new HashSet<ConfigurationValue>() {{
            add(objectCountConfigurationValue);
        }};
    }

    @Override
    public synchronized void allocateObject(ObjectInCache objectInCache) {
        storedObjects.put(objectInCache.getUuid(), objectInCache.objectRef());
        updateConfigValue();
    }

    @Override
    public ObjectInCache freeObject(ObjectInCache cachedObject) throws ObjectNotFoundInCache {
        if (storedObjects.containsKey(cachedObject.getUuid())) {
            updateConfigValue();
            ObjectInCache objectInCache = ObjectInCache.from(cachedObject, storedObjects.get(cachedObject.getUuid()));
            storedObjects.remove(cachedObject.getUuid());
            return objectInCache;
        } else {
            throw new ObjectNotFoundInCache(cachedObject);
        }
    }

    @Override
    public synchronized ObjectInCache returnObject(ObjectInCache cachedObject) {
        if (storedObjects.containsKey(cachedObject.getUuid())) {
            return ObjectInCache.from(cachedObject, storedObjects.get(cachedObject.getUuid()));
        } else {
            return null;
        }
    }

    @Override
    public synchronized Boolean contains(ObjectInCache objectInCache) {
        return storedObjects.containsKey(objectInCache.getUuid());
    }

    private void updateConfigValue() {
        objectCountConfigurationValue.setCurrentState(storedObjects.size());
    }

    private Map<UUID, Object> storedObjects = new HashMap<>();
    ChangeableConfigurationValue<Integer> objectCountConfigurationValue;
}

