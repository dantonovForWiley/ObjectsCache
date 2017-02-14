package com.dantonov.wiley.objectscache;

import com.dantonov.wiley.objectscache.exceptions.AllocationException;

/**
 * Top level abstraction for cache representation
 */
public interface ObjectsCache {

    /**
     * Method to cache {@link Object} object
     *
     * @param object {@link Object} objec to cache
     * @return {@link CachedObject} reference to cached object
     * @throws AllocationException in case when object has not been cached
     */
    CachedObject cacheObject(Object object) throws AllocationException;

    /**
     * Method to release {@link CachedObject} referenced to cached object<br>
     * Calling means cached objects is not more needed to be stored
     *
     * @param cachedObject {@link CachedObject} reference to cached object
     */
    void releaseCachedObject(CachedObject cachedObject);
}
