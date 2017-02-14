package com.dantonov.wiley.objectscache.impl;

import com.dantonov.wiley.objectscache.CacheHierarchy;
import com.dantonov.wiley.objectscache.CachedObject;
import com.dantonov.wiley.objectscache.ObjectCacheStrategy;
import com.dantonov.wiley.objectscache.ObjectsCache;
import com.dantonov.wiley.objectscache.exceptions.AllocationException;
import com.dantonov.wiley.objectscache.exceptions.AllocationInCacheException;
import com.dantonov.wiley.objectscache.exceptions.ObjectNotFoundInCache;

/**
 * Implementation for {@link ObjectsCache}
 */
public class ObjectCacheImpl implements ObjectsCache {

    private ObjectCacheImpl(CacheHierarchy cacheLevelModel, ObjectCacheStrategy cacheMovingStrategy) {
        this.cacheLevelModel = cacheLevelModel;
        this.cacheMovingStrategy = cacheMovingStrategy;
    }

    /**
     * Builder for {@link ObjectCacheImpl}
     */
    public static class Builder {

        /**
         * Method to set {@link ObjectCacheStrategy} to be used by {@link ObjectCacheImpl} instance
         *
         * @param cacheMovingStrategy
         * @return {@link Builder} to support build chaining
         */
        public Builder setCacheMovingStrategy(ObjectCacheStrategy cacheMovingStrategy) {
            this.cacheMovingStrategy = cacheMovingStrategy;
            return this;
        }

        /**
         * Method to set particular {@link CacheHierarchy} to be used by {@link ObjectCacheImpl} instance
         *
         * @param cacheLevelModel
         * @return {@link Builder} to support build chaining
         */
        public Builder setCacheLevelModel(CacheHierarchy cacheLevelModel) {
            this.cacheLevelModel = cacheLevelModel;
            return this;
        }

        /**
         * Build {@link ObjectCacheImpl} instance
         *
         * @return new {@link ObjectCacheImpl} instance
         */
        public ObjectCacheImpl build() {
            return new ObjectCacheImpl(cacheLevelModel, cacheMovingStrategy);
        }

        private CacheHierarchy cacheLevelModel;
        private ObjectCacheStrategy cacheMovingStrategy;
    }

    @Override
    public CachedObject cacheObject(Object object) throws AllocationException {
        ObjectInCache objectInCache = new ObjectInCache.Builder().setObject(object).setObjectsCache(this).build();
        cacheMovingStrategy.allocateObject(objectInCache, cacheLevelModel);
        cacheMovingStrategy.reallocateObjects(cacheLevelModel);
        return objectInCache;
    }

    @Override
    public void releaseCachedObject(CachedObject cachedObject) {
        cacheMovingStrategy.releaseObject(ObjectInCache.from(cachedObject, this), cacheLevelModel);
        cacheMovingStrategy.reallocateObjects(cacheLevelModel);
    }

    /**
     * Method to retrieve object, referenced by {@link ObjectInCache}, from underlying {@link CacheHierarchy}
     *
     * @param objectInCache {@link ObjectInCache} reference to wanted object
     * @return {@link Object}
     * @throws ObjectNotFoundInCache      in case when object can has not been found
     * @throws AllocationInCacheException in case when object has not been retrieved due to cache
     *                                    internal exception
     */
    public Object findObject(ObjectInCache objectInCache) throws ObjectNotFoundInCache, AllocationInCacheException {
        cacheMovingStrategy.reallocateObjects(cacheLevelModel);
        return cacheLevelModel.getCacheList().parallelStream().filter((cache -> cache.contains(objectInCache))).findFirst().orElseThrow(() -> new ObjectNotFoundInCache(objectInCache)).returnObject(objectInCache).objectRef();
    }

    private CacheHierarchy cacheLevelModel;
    private ObjectCacheStrategy cacheMovingStrategy;
}
