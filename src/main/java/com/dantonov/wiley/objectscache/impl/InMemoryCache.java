package com.dantonov.wiley.objectscache.impl;

import com.dantonov.wiley.objectscache.Cache;
import com.dantonov.wiley.objectscache.ChangeableConfigurationValue;
import com.dantonov.wiley.objectscache.ConfigurationValue;
import com.dantonov.wiley.objectscache.ObjectCacheStrategy;
import com.dantonov.wiley.objectscache.exceptions.AllocationInCacheException;
import com.dantonov.wiley.objectscache.exceptions.ObjectNotFoundInCache;

import java.util.*;
import java.util.stream.Collectors;

/**
 * {@link Cache} implementation to keep objects in memory.<br>
 * It has max cache size attribute, configuring via {@link ConfigurationValue} property.<br>
 * <b>Note:</b> Cache is allowed to put new objects even if max cache size exceeded.<br>
 * {@link ObjectCacheStrategy} should manage objects load via
 * underlying caches to satisfy appropriate {@link ConfigurationValue} responding.<br>
 * Cache also has a percent loading {@link ConfigurationValue}.
 */
public class InMemoryCache implements Cache {

    private InMemoryCache(int maxCacheSize, int dangerLoad) {
        objectsInCacheConfigurationValue = new GenericConfigurationValue<>(maxCacheSize,
                (currentState, specialState) -> currentState <= specialState, ConfigurationValue
                .WARN_LEVEL.CRITICAL, maxSize -> String.format("Max cache size = %s", maxSize),
                currentSize -> String.format("Current amount of objects in cache = %s",
                        currentSize));
        objectsInCacheConfigurationValue.setCurrentState(0);
        percentLoadConfigurationValue = new GenericConfigurationValue<>(
                dangerLoad, (
                (currentState, specialState) -> {
                    if (specialState == 0) {
                        return currentState == 0;
                    }
                    return (((double)currentState * 100) / maxCacheSize) <= specialState;
                }),
                ConfigurationValue.WARN_LEVEL.WARN, maxPercent -> String.format("Cache load " +
                "should not exceed %s%", maxPercent), current -> {

            String currentLoad;
            if (dangerLoad == 0) {
                currentLoad = "Since danger load is set to 0, it is not possible to calculate " +
                        "current load in percents.";
            } else {
                Double currentLoadValue = ((double) current / dangerLoad) * 100;
                currentLoad = String.format("Current load is %d%s", currentLoadValue);
            }
            return currentLoad;
        });
        percentLoadConfigurationValue.setCurrentState(0);

        cacheStorage = new HashMap<>();
    }

    /**
     * Builder for {@link InMemoryCache} instance<br>
     * <ul>Max cache size and {@code dangerLoad} value may be configured:
     * <li>max cache size is a critical {@link ConfigurationValue}</li>
     * <li>danger load means max cache occupancy considered as normal for this cache. If
     * current load is greater, appropriate {@link ConfigurationValue} responds warning</li>
     * </ul>
     */
    public static class Builder {

        /**
         * Configure max cache size
         *
         * @param maxCacheSize max amount of objects for this cache
         * @return Builder to support builder chaining
         * @throws UnacceptableCacheBuildParameter in case then provided parameter is unacceptable
         */
        public Builder setMaxCacheSize(int maxCacheSize) throws UnacceptableCacheBuildParameter {
            if (maxCacheSize <= 0) {
                throw new UnacceptableCacheBuildParameter(String.format("Max cache size parameter" +
                        "can not be less or equals 0. Provided value is %s", maxCacheSize));
            }
            this.maxCacheSize = maxCacheSize;
            return this;
        }

        /**
         * Configure dangerous loading for the cache
         *
         * @param dangerLoad integer value mentioning max suitable cache occupancy in percents
         * @return Builder to support builder chaining
         * @throws UnacceptableCacheBuildParameter in case then provided parameter is unacceptable
         */
        public Builder setDangerLoad(int dangerLoad) throws UnacceptableCacheBuildParameter {
            if (dangerLoad < 0 || dangerLoad > 100) {
                throw new UnacceptableCacheBuildParameter(String.format("Danger load is a percent" +
                        " value. It can not be out of [0,100] range" +
                        "Provided value is %s", dangerLoad));
            }
            this.dangerLoad = dangerLoad;
            return this;
        }

        /**
         * Build {@link InMemoryCache} instance
         *
         * @return new {@link InMemoryCache} instance
         */
        public InMemoryCache build() {
            return new InMemoryCache(maxCacheSize, dangerLoad);
        }

        private int maxCacheSize = 100;
        private int dangerLoad = 80;

    }

    @Override
    public Set<ConfigurationValue> getConfigurationValues() {
        return Arrays.asList(objectsInCacheConfigurationValue, percentLoadConfigurationValue)
                .stream().collect(Collectors.toSet());
    }

    @Override
    public void allocateObject(ObjectInCache cachedObject) throws AllocationInCacheException {
        cacheStorage.put(cachedObject.getUuid(), cachedObject.objectRef());
        cachedObject.clearObjectRef();
        updateCacheSizeInConfigurationValues();
    }

    @Override
    public ObjectInCache freeObject(ObjectInCache cachedObject) throws ObjectNotFoundInCache {
        return getObject(cachedObject, true);
    }

    @Override
    public ObjectInCache returnObject(ObjectInCache cachedObject) throws ObjectNotFoundInCache {
        return getObject(cachedObject, false);
    }

    private void updateCacheSizeInConfigurationValues(){
        int currentCacheSize = cacheStorage.size();
        percentLoadConfigurationValue.setCurrentState(currentCacheSize);
        objectsInCacheConfigurationValue.setCurrentState(currentCacheSize);
    }

    private ObjectInCache getObject(ObjectInCache cachedObject, boolean remove) throws ObjectNotFoundInCache {
        UUID uuid = cachedObject.getUuid();
        if (!cacheStorage.containsKey(uuid)) {
            throw new ObjectNotFoundInCache(cachedObject);
        } else {
            Object object;
            if (remove) {
                object = cacheStorage.remove(cachedObject.getUuid());
                updateCacheSizeInConfigurationValues();
            } else {
                object = cacheStorage.get(cachedObject.getUuid());
            }
            return ObjectInCache.from(cachedObject, object);
        }
    }

    @Override
    public Boolean contains(ObjectInCache objectInCache) {
        return cacheStorage.containsKey(objectInCache.getUuid());
    }

    @Override
    public String getCacheName() {
        return IN_MEMORY_CACHE_NAME;
    }

    private static final String IN_MEMORY_CACHE_NAME = "In-memory cache";

    // configuration value to check amount of stored objects
    private ChangeableConfigurationValue<Integer> objectsInCacheConfigurationValue;

    // configuration value to present current load in percents
    private ChangeableConfigurationValue<Integer> percentLoadConfigurationValue;

    // objects store
    private Map<UUID, Object> cacheStorage;
}
